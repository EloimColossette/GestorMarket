// ── AUTH HEADER HELPER ───────────────────────────────────
function analyticsAuthHeaders() {
    const token = localStorage.getItem("token");
    return {
        "Authorization": "Bearer " + token
    };
}

const ANALYTICS_API_URL = API_BASE_URL;

// ── INIT ─────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
    loadDashboard();
});

// ── ESTADOS DA TELA ───────────────────────────────────────
function setState(state) {
    document.getElementById("loadingState").style.display   = state === "loading" ? "block" : "none";
    document.getElementById("errorState").style.display      = state === "error"   ? "block" : "none";
    document.getElementById("emptyState").style.display       = state === "empty"   ? "block" : "none";
    document.getElementById("dashboardContent").style.display = state === "content" ? "block" : "none";
}

// ── CARREGA O DASHBOARD ────────────────────────────────────
window.loadDashboard = async function () {

    setState("loading");

    const meses = document.getElementById("filterMeses").value;
    const mesesFuturos = document.getElementById("filterMesesFuturos").value;
    const topProdutos = document.getElementById("filterTopProdutos").value;

    const params = new URLSearchParams({
        meses: meses,
        meses_futuros: mesesFuturos,
        top_produtos: topProdutos
    });

    try {
        const response = await fetch(`${ANALYTICS_API_URL}/api/analytics/dashboard?${params.toString()}`, {
            headers: analyticsAuthHeaders()
        });

        if (response.status === 401) {
            localStorage.removeItem("token");
            window.location.href = "login.html";
            return;
        }

        if (!response.ok) {
            const body = await response.json().catch(() => ({}));
            throw new Error(body.erro || "Erro ao carregar análises");
        }

        const data = await response.json();

        if (!data.gasto_mensal || !data.gasto_mensal.labels || data.gasto_mensal.labels.length === 0) {
            setState("empty");
            return;
        }

        renderStats(data);
        renderCharts(data.graficos_base64 || {});
        setState("content");

    } catch (err) {
        console.error("Erro ao carregar análises:", err);
        document.getElementById("errorState").innerText =
            "Não foi possível carregar as análises. Verifique se o serviço de analytics está rodando.";
        setState("error");
    }
};

// ── CARDS DE RESUMO ────────────────────────────────────────
function renderStats(data) {
    const grid = document.getElementById("statsGrid");
    grid.innerHTML = "";

    const gastoMensal = data.gasto_mensal || {};
    const previsao = data.previsao || {};

    const mesAtualTotal = gastoMensal.mes_atual ? gastoMensal.mes_atual.total : 0;
    const mediaMensal = gastoMensal.media_mensal || 0;

    const tendenciaInfo = formatTendencia(previsao.tendencia);
    const proximaPrevisao = (previsao.previsao && previsao.previsao.length > 0)
        ? previsao.previsao[0]
        : null;

    const cards = [
        {
            label: "Gasto no mês atual",
            value: formatCurrency(mesAtualTotal),
            sub: gastoMensal.mes_atual ? gastoMensal.mes_atual.periodo : ""
        },
        {
            label: "Média mensal",
            value: formatCurrency(mediaMensal),
            sub: `Últimos ${gastoMensal.labels.length} meses`
        },
        {
            label: "Tendência",
            value: tendenciaInfo.label,
            sub: previsao.variacao_mensal_estimada != null
                ? `${previsao.variacao_mensal_estimada >= 0 ? "+" : ""}${formatCurrency(previsao.variacao_mensal_estimada)}/mês`
                : "",
            className: tendenciaInfo.className
        },
        {
            label: "Previsão próximo mês",
            value: proximaPrevisao != null ? formatCurrency(proximaPrevisao) : "—",
            sub: previsao.confiabilidade ? `Confiabilidade: ${previsao.confiabilidade}` : ""
        }
    ];

    cards.forEach(card => {
        const div = document.createElement("div");
        div.className = "stat-card";
        div.innerHTML = `
            <div class="stat-label">${card.label}</div>
            <div class="stat-value ${card.className || ""}">${card.value}</div>
            <div class="stat-sub">${card.sub || ""}</div>
        `;
        grid.appendChild(div);
    });
}

function formatTendencia(tendencia) {
    switch (tendencia) {
        case "subindo": return { label: "📈 Subindo", className: "trend-up" };
        case "caindo":  return { label: "📉 Caindo", className: "trend-down" };
        case "estavel": return { label: "➡️ Estável", className: "trend-flat" };
        default:        return { label: "—", className: "" };
    }
}

// ── GRÁFICOS (IMAGENS BASE64) ────────────────────────────────
function renderCharts(graficos) {
    const mapping = {
        gasto_mensal_previsao: "gasto_mensal_previsao",
        por_supermercado: "por_supermercado",
        por_produto: "por_produto",
        promocoes: "promocoes"
    };

    Object.keys(mapping).forEach(key => {
        const section = document.getElementById(`section-${key}`);
        const img = document.getElementById(`img-${key}`);

        if (graficos[key]) {
            img.src = `data:image/png;base64,${graficos[key]}`;
            section.style.display = "block";
        } else {
            section.style.display = "none";
        }
    });
}

// ── HELPERS ───────────────────────────────────────────────
function formatCurrency(value) {
    return Number(value || 0).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}