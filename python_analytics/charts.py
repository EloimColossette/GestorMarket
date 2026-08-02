

import base64
import io

import matplotlib
matplotlib.use("Agg")  # renderiza sem precisar de tela (servidor)
import matplotlib.pyplot as plt


def _fig_to_base64(fig) -> str:
    buffer = io.BytesIO()
    fig.savefig(buffer, format="png", bbox_inches="tight", dpi=130)
    plt.close(fig)
    buffer.seek(0)
    return base64.b64encode(buffer.read()).decode("utf-8")


def grafico_gasto_mensal_com_previsao(
    labels_historico, valores_historico, labels_previsao, valores_previsao
) -> str:
    fig, ax = plt.subplots(figsize=(8, 4.5))

    ax.plot(labels_historico, valores_historico, marker="o", label="Gasto real", color="#4C6EF5")

    if labels_previsao:
        # conecta o ultimo ponto real ao primeiro ponto previsto
        labels_ligacao = [labels_historico[-1]] + list(labels_previsao)
        valores_ligacao = [valores_historico[-1]] + list(valores_previsao)
        ax.plot(
            labels_ligacao,
            valores_ligacao,
            marker="o",
            linestyle="--",
            label="Previsao",
            color="#F76707",
        )

    ax.set_title("Gasto mensal: historico e previsao")
    ax.set_ylabel("R$")
    ax.legend()
    ax.grid(alpha=0.3)
    plt.xticks(rotation=45, ha="right")
    fig.tight_layout()

    return _fig_to_base64(fig)


def grafico_por_supermercado(dados: list) -> str:
    fig, ax = plt.subplots(figsize=(7, 4.5))

    nomes = [d["supermercado"] for d in dados]
    valores = [d["total_gasto"] for d in dados]

    ax.bar(nomes, valores, color="#4C6EF5")
    ax.set_title("Gasto total por supermercado")
    ax.set_ylabel("R$")
    plt.xticks(rotation=30, ha="right")
    ax.grid(axis="y", alpha=0.3)
    fig.tight_layout()

    return _fig_to_base64(fig)


def grafico_por_produto(dados: list) -> str:
    fig, ax = plt.subplots(figsize=(7, 5))

    produtos = [d["produto"] for d in dados][::-1]
    valores = [d["total_gasto"] for d in dados][::-1]

    ax.barh(produtos, valores, color="#12B886")
    ax.set_title("Top produtos por gasto")
    ax.set_xlabel("R$")
    ax.grid(axis="x", alpha=0.3)
    fig.tight_layout()

    return _fig_to_base64(fig)


def grafico_promocoes(dados: list) -> str:
    fig, ax = plt.subplots(figsize=(7, 4.5))

    nomes = [d["supermercado"] for d in dados]
    percentuais = [d["percentual_promocao"] for d in dados]

    ax.bar(nomes, percentuais, color="#F76707")
    ax.set_title("% de itens em promocao por supermercado")
    ax.set_ylabel("%")
    plt.xticks(rotation=30, ha="right")
    ax.grid(axis="y", alpha=0.3)
    fig.tight_layout()

    return _fig_to_base64(fig)
