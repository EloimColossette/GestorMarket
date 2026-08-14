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
    carregarSupermercados();
    carregarProdutosParaAutocomplete();
    loadDashboard();
});

// ── CARREGA LISTA DE SUPERMERCADOS PRO FILTRO ────────────
async function carregarSupermercados() {
    try {
        const response = await fetch(`${ANALYTICS_API_URL}/api/analytics/supermercados`, {
            headers: analyticsAuthHeaders()
        });
        if (!response.ok) return;

        const data = await response.json();
        const select = document.getElementById("filterSupermercado");

        (data.supermercados || []).forEach(s => {
            const option = document.createElement("option");
            option.value = s.id;
            option.textContent = s.nome; // textContent -> nunca interpreta HTML/JS
            select.appendChild(option);
        });
    } catch (err) {
        console.error("Erro ao carregar supermercados:", err);
    }
}

// ── CARREGA PRODUTOS JÁ COMPRADOS PRO AUTOCOMPLETE ───────
async function carregarProdutosParaAutocomplete() {
    try {
        const response = await fetch(`${ANALYTICS_API_URL}/api/analytics/produtos`, {
            headers: analyticsAuthHeaders()
        });
        if (!response.ok) return;

        const data = await response.json();
        const datalist = document.getElementById("produtoSuggestions");
        datalist.innerHTML = "";

        (data.produtos || []).forEach(nome => {
            const option = document.createElement("option");
            option.value = nome;
            datalist.appendChild(option);
        });
    } catch (err) {
        console.error("Erro ao carregar produtos:", err);
    }
}

// ── LIMPAR FILTROS ────────────────────────────────────────
window.limparFiltros = function () {
    document.getElementById("filterSupermercado").value = "";
    document.getElementById("filterProduto").value = "";
    document.getElementById("filterDataInicio").value = "";
    document.getElementById("filterDataFim").value = "";
    document.getElementById("filterMeses").value = "6";
    document.getElementById("filterMesesFuturos").value = "3";
    document.getElementById("filterTopProdutos").value = "10";
    loadDashboard();
};

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
    const supermercadoId = document.getElementById("filterSupermercado").value;
    const produto = document.getElementById("filterProduto").value.trim();
    const dataInicio = document.getElementById("filterDataInicio").value;
    const dataFim = document.getElementById("filterDataFim").value;

    const params = new URLSearchParams({
        meses: meses,
        meses_futuros: mesesFuturos,
        top_produtos: topProdutos
    });

    if (supermercadoId) params.set("supermercado_id", supermercadoId);
    if (produto)         params.set("produto", produto);
    if (dataInicio)      params.set("data_inicio", dataInicio);
    if (dataFim)          params.set("data_fim", dataFim);

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

        const temGastoMensal = data.gasto_mensal && data.gasto_mensal.labels && data.gasto_mensal.labels.length > 0;
        const temHistoricoProduto = data.historico_produto && data.historico_produto.labels && data.historico_produto.labels.length > 0;

        if (!temGastoMensal && !temHistoricoProduto) {
            setState("empty");
            return;
        }

        renderStats(data);
        renderCharts(data.graficos_base64 || {});
        renderHistoricoProduto(data.historico_produto);
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
            sub: `Últimos ${gastoMensal.labels ? gastoMensal.labels.length : 0} meses`
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

    cards.forEach(card => appendStatCard(grid, card));
}

// Constrói o card via DOM (não innerHTML com dado dinâmico) — todos os
// valores aqui já são numéricos/formatados internamente, mas mantemos o
// mesmo padrão seguro usado no resto do front.
function appendStatCard(grid, card) {
    const div = document.createElement("div");
    div.className = "stat-card";

    const label = document.createElement("div");
    label.className = "stat-label";
    label.textContent = card.label;

    const value = document.createElement("div");
    value.className = `stat-value ${card.className || ""}`;
    value.textContent = card.value;

    const sub = document.createElement("div");
    sub.className = "stat-sub";
    sub.textContent = card.sub || "";

    div.appendChild(label);
    div.appendChild(value);
    div.appendChild(sub);
    grid.appendChild(div);
}

function formatTendencia(tendencia) {
    switch (tendencia) {
        case "subindo": return { label: "📈 Subindo", className: "trend-up" };
        case "caindo":  return { label: "📉 Caindo", className: "trend-down" };
        case "estavel": return { label: "➡️ Estável", className: "trend-flat" };
        default:        return { label: "—", className: "" };
    }
}

// ── HISTÓRICO DE PREÇO DO PRODUTO FILTRADO ───────────────
function renderHistoricoProduto(historico) {
    const section = document.getElementById("section-historico_produto");
    const cardsContainer = document.getElementById("produtoResumoCards");
    cardsContainer.innerHTML = "";

    if (!historico || !historico.labels || historico.labels.length === 0) {
        section.style.display = "none";
        return;
    }

    const resumo = historico.resumo;

    const cards = [
        { label: "Preço médio", value: formatCurrency(resumo.preco_medio), sub: `${resumo.qtd_compras} compra(s)` },
        { label: "Preço mínimo", value: formatCurrency(resumo.preco_minimo), sub: "" },
        { label: "Preço máximo", value: formatCurrency(resumo.preco_maximo), sub: "" },
        {
            label: "Variação desde a 1ª compra",
            value: `${resumo.variacao_pct >= 0 ? "+" : ""}${resumo.variacao_pct}%`,
            sub: "",
            className: resumo.variacao_pct > 0 ? "trend-up" : (resumo.variacao_pct < 0 ? "trend-down" : "trend-flat")
        },
    ];

    cards.forEach(card => appendStatCard(cardsContainer, card));

    const img = document.getElementById("img-historico_produto");
    // gráfico já vem pronto do backend (base64); se não veio (sem dados
    // suficientes), esconde a seção.
    section.style.display = "block";
}

// ── GRÁFICOS (IMAGENS BASE64) ────────────────────────────────
function renderCharts(graficos) {
    const mapping = {
        gasto_mensal_previsao: "gasto_mensal_previsao",
        por_supermercado: "por_supermercado",
        por_produto: "por_produto",
        promocoes: "promocoes",
        historico_produto: "historico_produto"
    };

    Object.keys(mapping).forEach(key => {
        const section = document.getElementById(`section-${key}`);
        const img = document.getElementById(`img-${key}`);
        if (!section || !img) return;

        if (graficos[key]) {
            img.src = `data:image/png;base64,${graficos[key]}`;
            // a seção de histórico do produto é controlada por renderHistoricoProduto
            if (key !== "historico_produto") {
                section.style.display = "block";
            }
        } else if (key !== "historico_produto") {
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