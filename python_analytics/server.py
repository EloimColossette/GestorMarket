import json
import re
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlsplit, parse_qs

import analytics
import charts
import config
import database
from auth import AuthError, get_user_id_from_headers


# ────────────────────────────────────────────────────────────
# ROTEAMENTO: um dicionario simples de path -> funcao
# (equivalente "na unha" ao que um framework faria por baixo dos panos)
# ────────────────────────────────────────────────────────────
ROUTES = {}
ROTAS_SEM_AUTENTICACAO = {"/api/analytics/health"}

_DATA_REGEX = re.compile(r"^\d{4}-\d{2}-\d{2}$")


def route(path: str):
    def decorator(func):
        ROUTES[path] = func
        return func
    return decorator


def _query_int(query: dict, nome: str, padrao):
    valor = query.get(nome, [None])[0]
    if valor is None or valor == "":
        return padrao
    try:
        return int(valor)
    except ValueError:
        return padrao


def _query_str(query: dict, nome: str):
    valor = query.get(nome, [None])[0]
    if valor is None or valor.strip() == "":
        return None
    return valor.strip()


def _query_date(query: dict, nome: str):
    """Só aceita datas no formato YYYY-MM-DD; qualquer outra coisa é ignorada
    (evita passar lixo para a query SQL e trava tentativa de injeção)."""
    valor = _query_str(query, nome)
    if valor is None or not _DATA_REGEX.match(valor):
        return None
    return valor


def _filtros_comuns(query: dict) -> dict:
    return {
        "start_date": _query_date(query, "data_inicio"),
        "end_date": _query_date(query, "data_fim"),
        "supermarket_id": _query_int(query, "supermercado_id", None),
    }


@route("/api/analytics/health")
def rota_health(user_id, query):
    return {"status": "ok"}


@route("/api/analytics/supermercados")
def rota_supermercados(user_id, query):
    """Lista de supermercados do usuário, para popular o filtro no front."""
    return {"supermercados": database.get_supermarkets(user_id)}


@route("/api/analytics/produtos")
def rota_produtos(user_id, query):
    """Lista/busca de produtos já comprados, para autocomplete do filtro."""
    busca = _query_str(query, "busca")
    return {"produtos": database.get_product_names(user_id, search=busca)}


@route("/api/analytics/gasto-mensal")
def rota_gasto_mensal(user_id, query):
    meses = _query_int(query, "meses", 6)
    filtros = _filtros_comuns(query)
    df = database.get_purchases_df(user_id, **filtros)
    # se veio um intervalo de data explícito, mostra o período inteiro
    limitar = not (filtros["start_date"] or filtros["end_date"])
    return analytics.gasto_mensal(df, meses=meses, limitar=limitar)


@route("/api/analytics/previsao")
def rota_previsao(user_id, query):
    meses_futuros = _query_int(query, "meses_futuros", 3)
    filtros = _filtros_comuns(query)
    df = database.get_purchases_df(user_id, **filtros)
    return analytics.previsao_gastos(df, meses_futuros=meses_futuros)


@route("/api/analytics/por-supermercado")
def rota_por_supermercado(user_id, query):
    filtros = _filtros_comuns(query)
    filtros.pop("supermarket_id", None)  # não faz sentido filtrar por mercado num gráfico "por mercado"
    df = database.get_purchases_df(user_id, **filtros)
    return analytics.gasto_por_supermercado(df)


@route("/api/analytics/por-produto")
def rota_por_produto(user_id, query):
    limite = _query_int(query, "limite", 10)
    filtros = _filtros_comuns(query)
    df = database.get_purchase_items_df(user_id, **filtros)
    return analytics.gasto_por_produto(df, limite=limite)


@route("/api/analytics/promocoes")
def rota_promocoes(user_id, query):
    filtros = _filtros_comuns(query)
    df = database.get_purchase_items_df(user_id, **filtros)
    return analytics.promocoes_por_supermercado(df)


@route("/api/analytics/produto/historico")
def rota_produto_historico(user_id, query):
    produto = _query_str(query, "produto")
    if not produto:
        return {"erro_validacao": "Informe o parâmetro 'produto'."}

    filtros = _filtros_comuns(query)
    df = database.get_purchase_items_df(user_id, product_name=produto, **filtros)

    resultado = analytics.historico_produto(df)
    if resultado["labels"]:
        resultado["grafico_base64"] = charts.grafico_historico_produto(
            resultado["labels"], resultado["precos"], resultado["resumo"]["produto"]
        )
    return resultado


@route("/api/analytics/dashboard")
def rota_dashboard(user_id, query):
    meses = _query_int(query, "meses", 6)
    meses_futuros = _query_int(query, "meses_futuros", 3)
    top_produtos = _query_int(query, "top_produtos", 10)
    produto_foco = _query_str(query, "produto")

    filtros = _filtros_comuns(query)
    limitar_meses = not (filtros["start_date"] or filtros["end_date"])

    purchases_df = database.get_purchases_df(user_id, **filtros)

    filtros_supermercado = dict(filtros)
    filtros_sem_supermercado = dict(filtros)
    filtros_sem_supermercado.pop("supermarket_id", None)

    items_df = database.get_purchase_items_df(user_id, **filtros_supermercado)

    gasto_mensal = analytics.gasto_mensal(purchases_df, meses=meses, limitar=limitar_meses)
    previsao = analytics.previsao_gastos(purchases_df, meses_futuros=meses_futuros)
    por_supermercado = analytics.gasto_por_supermercado(
        database.get_purchases_df(user_id, **filtros_sem_supermercado)
    )
    por_produto = analytics.gasto_por_produto(items_df, limite=top_produtos)
    promocoes = analytics.promocoes_por_supermercado(items_df)

    graficos = {}
    if gasto_mensal["labels"]:
        graficos["gasto_mensal_previsao"] = charts.grafico_gasto_mensal_com_previsao(
            gasto_mensal["labels"],
            gasto_mensal["gastos"],
            previsao["labels"],
            previsao["previsao"],
        )
    if por_supermercado:
        graficos["por_supermercado"] = charts.grafico_por_supermercado(por_supermercado)
    if por_produto:
        graficos["por_produto"] = charts.grafico_por_produto(por_produto)
    if promocoes:
        graficos["promocoes"] = charts.grafico_promocoes(promocoes)

    resultado = {
        "gasto_mensal": gasto_mensal,
        "previsao": previsao,
        "por_supermercado": por_supermercado,
        "por_produto": por_produto,
        "promocoes": promocoes,
        "graficos_base64": graficos,
    }

    # Se o usuário escolheu um produto específico no filtro, inclui também
    # o histórico de preço dele (série + gráfico dedicado).
    if produto_foco:
        produto_df = database.get_purchase_items_df(
            user_id, product_name=produto_foco, **filtros_supermercado
        )
        historico = analytics.historico_produto(produto_df)
        if historico["labels"]:
            graficos["historico_produto"] = charts.grafico_historico_produto(
                historico["labels"], historico["precos"], historico["resumo"]["produto"]
            )
        resultado["historico_produto"] = historico

    return resultado


# ────────────────────────────────────────────────────────────
# HANDLER HTTP (so biblioteca padrao)
# ────────────────────────────────────────────────────────────
class AnalyticsRequestHandler(BaseHTTPRequestHandler):

    def _send_cors_headers(self):
        allowed = config.ALLOWED_ORIGINS
        request_origin = self.headers.get("Origin")

        if allowed == ["*"]:
            origin_to_send = "*"
        elif request_origin and request_origin in allowed:
            origin_to_send = request_origin
        else:
            origin_to_send = allowed[0]

        self.send_header("Access-Control-Allow-Origin", origin_to_send)
        self.send_header("Vary", "Origin")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        self.send_header("Access-Control-Allow-Methods", "GET, OPTIONS")

    def _send_json(self, status: int, payload):
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self._send_cors_headers()
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_OPTIONS(self):
        # Resposta ao "preflight" que o navegador manda antes do GET
        self.send_response(204)
        self._send_cors_headers()
        self.end_headers()

    def do_GET(self):
        parsed = urlsplit(self.path)
        path = parsed.path
        query = parse_qs(parsed.query)

        handler_func = ROUTES.get(path)
        if handler_func is None:
            self._send_json(404, {"erro": "Rota nao encontrada"})
            return

        try:
            if path in ROTAS_SEM_AUTENTICACAO:
                user_id = None
            else:
                user_id = get_user_id_from_headers(self.headers)

            resultado = handler_func(user_id, query)
            self._send_json(200, resultado)

        except AuthError as e:
            self._send_json(e.status_code, {"erro": e.message})
        except Exception as e:
            # Não expor detalhe interno (str(e)) pro cliente — só loga no servidor.
            print(f"[python-analytics] ERRO em {path}: {e}")
            self._send_json(500, {"erro": "Erro interno no servico de analytics."})

    def log_message(self, format, *args):
        print("[python-analytics] " + (format % args))


if __name__ == "__main__":
    servidor = ThreadingHTTPServer(("0.0.0.0", config.ANALYTICS_SERVICE_PORT), AnalyticsRequestHandler)
    print(f"[python-analytics] Rodando em http://0.0.0.0:{config.ANALYTICS_SERVICE_PORT}")
    servidor.serve_forever()