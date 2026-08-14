import base64
import io

import matplotlib
matplotlib.use("Agg")  # renderiza sem precisar de tela (servidor)
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker


# ────────────────────────────────────────────────────────────
# ESTILO GLOBAL — paleta e tipografia consistentes em todos os gráficos
# ────────────────────────────────────────────────────────────
COR_PRIMARIA = "#4C6EF5"   # azul (gasto / neutro)
COR_SECUNDARIA = "#F76707"  # laranja (previsão / alerta)
COR_POSITIVA = "#12B886"    # verde (economia / promoção)
COR_TEXTO = "#2E3440"
COR_GRID = "#E9ECEF"
COR_FUNDO = "#FFFFFF"

PALETA_CATEGORICA = [
    "#4C6EF5", "#12B886", "#F76707", "#BE4BDB",
    "#FA5252", "#15AABF", "#FAB005", "#7048E8",
]

plt.rcParams.update({
    "font.family": "DejaVu Sans",
    "font.size": 11,
    "text.color": COR_TEXTO,
    "axes.edgecolor": COR_GRID,
    "axes.labelcolor": COR_TEXTO,
    "xtick.color": COR_TEXTO,
    "ytick.color": COR_TEXTO,
    "axes.titleweight": "bold",
    "axes.titlesize": 13,
    "figure.facecolor": COR_FUNDO,
    "axes.facecolor": COR_FUNDO,
    "savefig.facecolor": COR_FUNDO,
})


def _fig_to_base64(fig) -> str:
    buffer = io.BytesIO()
    fig.savefig(buffer, format="png", bbox_inches="tight", dpi=150)
    plt.close(fig)
    buffer.seek(0)
    return base64.b64encode(buffer.read()).decode("utf-8")


def _estilizar_eixos(ax, grid_axis="y"):
    """Remove bordas desnecessárias e deixa o grid discreto — visual mais limpo."""
    for spine in ("top", "right", "left" if grid_axis == "y" else "bottom"):
        ax.spines[spine].set_visible(False)
    ax.grid(axis=grid_axis, color=COR_GRID, linewidth=0.8, zorder=0)
    ax.set_axisbelow(True)


def _formatar_reais(ax, eixo="y"):
    formatter = mticker.FuncFormatter(lambda v, _: f"R$ {v:,.0f}".replace(",", "."))
    if eixo == "y":
        ax.yaxis.set_major_formatter(formatter)
    else:
        ax.xaxis.set_major_formatter(formatter)


def grafico_gasto_mensal_com_previsao(
        labels_historico, valores_historico, labels_previsao, valores_previsao
) -> str:
    fig, ax = plt.subplots(figsize=(9, 4.8))

    ax.plot(
        labels_historico, valores_historico,
        marker="o", markersize=6, linewidth=2.4,
        label="Gasto real", color=COR_PRIMARIA, zorder=3,
    )
    ax.fill_between(
        range(len(labels_historico)), valores_historico,
        color=COR_PRIMARIA, alpha=0.08, zorder=1,
    )

    if labels_previsao:
        labels_ligacao = [labels_historico[-1]] + list(labels_previsao)
        valores_ligacao = [valores_historico[-1]] + list(valores_previsao)
        ax.plot(
            labels_ligacao, valores_ligacao,
            marker="o", markersize=6, linewidth=2.2, linestyle="--",
            label="Previsão", color=COR_SECUNDARIA, zorder=3,
        )

    for x, y in zip(labels_historico, valores_historico):
        ax.annotate(f"R$ {y:,.0f}".replace(",", "."), (x, y),
                    textcoords="offset points", xytext=(0, 10),
                    ha="center", fontsize=8.5, color=COR_TEXTO)

    ax.set_title("Gasto mensal — histórico e previsão", loc="left", pad=14)
    _formatar_reais(ax)
    _estilizar_eixos(ax)
    ax.legend(frameon=False, loc="upper left")
    plt.xticks(rotation=40, ha="right")
    fig.tight_layout()

    return _fig_to_base64(fig)


def grafico_por_supermercado(dados: list) -> str:
    fig, ax = plt.subplots(figsize=(8, 4.8))

    nomes = [d["supermercado"] for d in dados]
    valores = [d["total_gasto"] for d in dados]
    cores = [PALETA_CATEGORICA[i % len(PALETA_CATEGORICA)] for i in range(len(nomes))]

    barras = ax.bar(nomes, valores, color=cores, zorder=3, width=0.6)
    ax.bar_label(
        barras,
        labels=[f"R$ {v:,.0f}".replace(",", ".") for v in valores],
        padding=4, fontsize=9,
    )

    ax.set_title("Gasto total por supermercado", loc="left", pad=14)
    _formatar_reais(ax)
    _estilizar_eixos(ax)
    plt.xticks(rotation=25, ha="right")
    ax.margins(y=0.15)
    fig.tight_layout()

    return _fig_to_base64(fig)


def grafico_por_produto(dados: list) -> str:
    fig, ax = plt.subplots(figsize=(8, max(4.5, 0.42 * len(dados) + 1.5)))

    produtos = [d["produto"] for d in dados][::-1]
    valores = [d["total_gasto"] for d in dados][::-1]

    barras = ax.barh(produtos, valores, color=COR_POSITIVA, zorder=3, height=0.6)
    ax.bar_label(
        barras,
        labels=[f"R$ {v:,.0f}".replace(",", ".") for v in valores],
        padding=4, fontsize=9,
    )

    ax.set_title("Top produtos por gasto", loc="left", pad=14)
    _formatar_reais(ax, eixo="x")
    _estilizar_eixos(ax, grid_axis="x")
    ax.margins(x=0.12)
    fig.tight_layout()

    return _fig_to_base64(fig)


def grafico_promocoes(dados: list) -> str:
    fig, ax = plt.subplots(figsize=(8, 4.8))

    nomes = [d["supermercado"] for d in dados]
    percentuais = [d["percentual_promocao"] for d in dados]

    barras = ax.bar(nomes, percentuais, color=COR_SECUNDARIA, zorder=3, width=0.6)
    ax.bar_label(barras, labels=[f"{v:.1f}%" for v in percentuais], padding=4, fontsize=9)

    ax.set_title("% de itens em promoção por supermercado", loc="left", pad=14)
    ax.set_ylabel("%")
    _estilizar_eixos(ax)
    plt.xticks(rotation=25, ha="right")
    ax.margins(y=0.15)
    fig.tight_layout()

    return _fig_to_base64(fig)


def grafico_historico_produto(labels: list, precos: list, produto: str) -> str:
    """Evolução do preço unitário de um produto específico, compra a compra."""
    fig, ax = plt.subplots(figsize=(9, 4.8))

    ax.plot(
        labels, precos, marker="o", markersize=6, linewidth=2.2,
        color=COR_PRIMARIA, zorder=3,
    )
    ax.fill_between(range(len(labels)), precos, color=COR_PRIMARIA, alpha=0.08, zorder=1)

    media = sum(precos) / len(precos)
    ax.axhline(media, color=COR_SECUNDARIA, linestyle="--", linewidth=1.4, zorder=2,
               label=f"Média: R$ {media:,.2f}".replace(",", "."))

    for x, y in zip(labels, precos):
        ax.annotate(f"R$ {y:,.2f}".replace(",", "."), (x, y),
                    textcoords="offset points", xytext=(0, 10),
                    ha="center", fontsize=8, color=COR_TEXTO)

    ax.set_title(f"Evolução de preço — {produto}", loc="left", pad=14)
    _formatar_reais(ax)
    _estilizar_eixos(ax)
    ax.legend(frameon=False, loc="upper left")
    plt.xticks(rotation=40, ha="right")
    fig.tight_layout()

    return _fig_to_base64(fig)