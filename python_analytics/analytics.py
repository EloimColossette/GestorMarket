from datetime import date

import numpy as np
import pandas as pd


# ────────────────────────────────────────────────────────────
# 1) GASTO POR MES (historico + mes atual)
# ────────────────────────────────────────────────────────────
def gasto_mensal(purchases_df: pd.DataFrame, meses: int = 6, limitar: bool = True) -> dict:
    """
    Agrupa o total gasto por mes (ano-mes) e separa o mes atual dos meses
    anteriores. Retorna os ultimos `meses` meses com dado (incluindo o atual).

    `limitar=False` é usado quando o próprio filtro de data (data_inicio/
    data_fim) já delimitou o período no banco — nesse caso mostramos o
    período inteiro, sem cortar para os últimos N meses.
    """
    if purchases_df.empty:
        return {"labels": [], "gastos": [], "mes_atual": None, "media_mensal": 0.0}

    df = purchases_df.copy()
    df["ano_mes"] = df["purchase_date"].dt.to_period("M")

    resumo = df.groupby("ano_mes")["total"].sum().sort_index()
    if limitar:
        resumo = resumo.tail(meses)

    hoje_periodo = pd.Period(date.today(), freq="M")

    labels = [str(p) for p in resumo.index]
    gastos = [round(float(v), 2) for v in resumo.values]

    gasto_mes_atual = float(resumo.get(hoje_periodo, 0.0))

    return {
        "labels": labels,  # ex: ["2026-03", "2026-04", ..., "2026-08"]
        "gastos": gastos,
        "mes_atual": {
            "periodo": str(hoje_periodo),
            "total": round(gasto_mes_atual, 2),
        },
        "media_mensal": round(float(resumo.mean()), 2) if len(resumo) else 0.0,
    }


# ────────────────────────────────────────────────────────────
# 2) PREVISAO DE GASTO FUTURO (regressao linear simples)
# ────────────────────────────────────────────────────────────
def previsao_gastos(purchases_df: pd.DataFrame, meses_futuros: int = 3) -> dict:
    """
    Usa regressao linear (numpy.polyfit) sobre o total gasto por mes para
    projetar os proximos `meses_futuros` meses.

    Com poucos dados (< 3 meses), cai para uma previsao simples pela media.
    """
    if purchases_df.empty:
        return {"labels": [], "previsao": [], "metodo": "sem_dados", "confiabilidade": "baixa"}

    df = purchases_df.copy()
    df["ano_mes"] = df["purchase_date"].dt.to_period("M")
    resumo = df.groupby("ano_mes")["total"].sum().sort_index()

    n = len(resumo)

    if n < 3:
        media = float(resumo.mean())
        ultimo_periodo = resumo.index[-1]
        labels = []
        valores = []
        for i in range(1, meses_futuros + 1):
            periodo = ultimo_periodo + i
            labels.append(str(periodo))
            valores.append(round(media, 2))
        return {
            "labels": labels,
            "previsao": valores,
            "metodo": "media_simples",
            "confiabilidade": "baixa",
            "observacao": "Poucos meses de historico; previsao baseada na media.",
        }

    x = np.arange(n)
    y = resumo.values.astype(float)

    # Regressao linear grau 1: gasto = a*x + b
    coeficientes = np.polyfit(x, y, 1)
    a, b = coeficientes

    ultimo_periodo = resumo.index[-1]
    labels = []
    valores = []
    for i in range(1, meses_futuros + 1):
        x_futuro = n - 1 + i
        valor_previsto = a * x_futuro + b
        valor_previsto = max(0.0, float(valor_previsto))  # gasto nao pode ser negativo
        periodo = ultimo_periodo + i
        labels.append(str(periodo))
        valores.append(round(valor_previsto, 2))

    tendencia = "subindo" if a > 1 else ("caindo" if a < -1 else "estavel")

    return {
        "labels": labels,
        "previsao": valores,
        "metodo": "regressao_linear",
        "tendencia": tendencia,
        "variacao_mensal_estimada": round(float(a), 2),
        "confiabilidade": "media" if n < 6 else "alta",
    }


# ────────────────────────────────────────────────────────────
# 3) GASTO POR SUPERMERCADO
# ────────────────────────────────────────────────────────────
def gasto_por_supermercado(purchases_df: pd.DataFrame) -> list:
    if purchases_df.empty:
        return []

    resumo = (
        purchases_df.groupby("supermarket_name")
        .agg(total_gasto=("total", "sum"), qtd_compras=("purchases_id", "count"))
        .reset_index()
    )
    resumo["ticket_medio"] = resumo["total_gasto"] / resumo["qtd_compras"]
    resumo = resumo.sort_values("total_gasto", ascending=False)

    return [
        {
            "supermercado": row["supermarket_name"],
            "total_gasto": round(float(row["total_gasto"]), 2),
            "qtd_compras": int(row["qtd_compras"]),
            "ticket_medio": round(float(row["ticket_medio"]), 2),
        }
        for _, row in resumo.iterrows()
    ]


# ────────────────────────────────────────────────────────────
# 4) GASTO POR PRODUTO
# ────────────────────────────────────────────────────────────
def gasto_por_produto(items_df: pd.DataFrame, limite: int = 10) -> list:
    if items_df.empty:
        return []

    resumo = (
        items_df.groupby("product_name")
        .agg(
            total_gasto=("subtotal", "sum"),
            quantidade_total=("quantity", "sum"),
            preco_medio=("unit_price", "mean"),
            qtd_compras=("purchase_id", "nunique"),
        )
        .reset_index()
        .sort_values("total_gasto", ascending=False)
        .head(limite)
    )

    return [
        {
            "produto": row["product_name"],
            "total_gasto": round(float(row["total_gasto"]), 2),
            "quantidade_total": int(row["quantidade_total"]),
            "preco_medio": round(float(row["preco_medio"]), 2),
            "qtd_compras": int(row["qtd_compras"]),
        }
        for _, row in resumo.iterrows()
    ]


# ────────────────────────────────────────────────────────────
# 5) PROMOCOES POR SUPERMERCADO
# ────────────────────────────────────────────────────────────
def promocoes_por_supermercado(items_df: pd.DataFrame) -> list:
    """
    Para cada supermercado: quantos itens tinham promocao ativa, o percentual
    sobre o total de itens comprados la, e uma estimativa de quanto foi
    "gasto em itens promocionais" (subtotal desses itens).
    """
    if items_df.empty:
        return []

    total_itens = items_df.groupby("supermarket_name").size().rename("total_itens")
    itens_promo = (
        items_df[items_df["promotion_active"]]
        .groupby("supermarket_name")
        .agg(itens_promocao=("purchase_items_id", "count"), gasto_em_promocao=("subtotal", "sum"))
    )

    resumo = pd.concat([total_itens, itens_promo], axis=1).fillna(0)
    resumo["itens_promocao"] = resumo["itens_promocao"].astype(int)
    resumo["gasto_em_promocao"] = resumo["gasto_em_promocao"].astype(float)
    resumo["percentual_promocao"] = (
            resumo["itens_promocao"] / resumo["total_itens"] * 100
    ).round(1)

    resumo = resumo.sort_values("percentual_promocao", ascending=False).reset_index()

    return [
        {
            "supermercado": row["supermarket_name"],
            "total_itens": int(row["total_itens"]),
            "itens_promocao": int(row["itens_promocao"]),
            "percentual_promocao": float(row["percentual_promocao"]),
            "gasto_em_promocao": round(float(row["gasto_em_promocao"]), 2),
        }
        for _, row in resumo.iterrows()
    ]


# ────────────────────────────────────────────────────────────
# 6) HISTORICO DE PRECO DE UM PRODUTO ESPECIFICO
# ────────────────────────────────────────────────────────────
def historico_produto(items_df: pd.DataFrame) -> dict:
    """
    Para um `items_df` já filtrado por um produto (via product_name no
    database.py), monta a evolução do preço unitário compra a compra e
    um resumo (preço mínimo, máximo, médio e variação desde a 1a compra).
    """
    if items_df.empty:
        return {"labels": [], "precos": [], "resumo": None}

    df = items_df.sort_values("purchase_date").copy()

    labels = [d.strftime("%d/%m/%Y") for d in df["purchase_date"]]
    precos = [round(float(v), 2) for v in df["unit_price"]]
    supermercados = list(df["supermarket_name"])

    preco_inicial = precos[0]
    preco_atual = precos[-1]
    variacao_pct = (
        round(((preco_atual - preco_inicial) / preco_inicial) * 100, 1)
        if preco_inicial > 0 else 0.0
    )

    resumo = {
        "produto": df["product_name"].iloc[0],
        "preco_minimo": round(float(df["unit_price"].min()), 2),
        "preco_maximo": round(float(df["unit_price"].max()), 2),
        "preco_medio": round(float(df["unit_price"].mean()), 2),
        "preco_atual": preco_atual,
        "variacao_pct": variacao_pct,
        "qtd_compras": int(len(df)),
        "mercado_mais_barato": df.loc[df["unit_price"].idxmin(), "supermarket_name"],
    }

    return {
        "labels": labels,
        "precos": precos,
        "supermercados": supermercados,
        "resumo": resumo,
    }