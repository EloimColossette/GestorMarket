const API_URL = API_BASE_URL;

// ── AUTH HEADER HELPER ───────────────────────────────────
function authHeaders() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + token
    };
}

// ── INIT ─────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
    loadSupermarketOptions();
    showEmptyState();
});

function showEmptyState() {
    document.getElementById("summaryList").innerHTML =
        `<div class="glass-card">Escolha um filtro e clique em "Filtrar" para ver o relatório</div>`;
}

// ── POPULA O DROPDOWN COM OS SUPERMERCADOS CADASTRADOS ───
async function loadSupermarketOptions() {
    try {
        const response = await fetch(`${API_URL}/supermarkets`, {
            headers: authHeaders()
        });

        if (!response.ok) {
            throw new Error("Erro ao carregar supermercados");
        }

        const supermarkets = await response.json();
        const select = document.getElementById("filterSupermarket");

        supermarkets.forEach(s => {
            const option = document.createElement("option");
            option.value = s.id;
            option.textContent = s.name;
            select.appendChild(option);
        });

    } catch (err) {
        console.error("Erro ao carregar supermercados:", err);
    }
}

// ── NÍVEL 1: RESUMO POR SUPERMERCADO ──────────────────────
window.loadSummary = async function () {

    const supermarketId = document.getElementById("filterSupermarket").value;
    const date = document.getElementById("filterDate").value;

    const params = new URLSearchParams();
    if (supermarketId) params.append("supermarketId", supermarketId);
    if (date)          params.append("date", date);

    const url = params.toString()
        ? `${API_URL}/purchases/summary?${params.toString()}`
        : `${API_URL}/purchases/summary`;

    try {
        const response = await fetch(url, { headers: authHeaders() });

        if (!response.ok) {
            throw new Error("Erro ao carregar resumo");
        }

        const data = await response.json();
        renderSummary(data, date);

    } catch (err) {
        console.error("Erro ao carregar resumo:", err);
        alert("Erro ao carregar relatório");
    }
};

function renderSummary(summary, date) {

    const container = document.getElementById("summaryList");
    container.innerHTML = "";

    if (!summary || summary.length === 0) {
        container.innerHTML = `<div class="glass-card">Nenhuma compra encontrada</div>`;
        return;
    }

    summary.forEach(item => {

        const div = document.createElement("div");
        div.className = "menu-card";
        div.style.cursor = "pointer";
        div.onclick = () => openDetail(item.supermarketId, item.supermarketName, date);

        div.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:center;">
                <span>🏪 ${item.supermarketName}</span>
                <strong>${formatCurrency(item.total)}</strong>
            </div>
        `;

        container.appendChild(div);
    });
}

// ── NÍVEL 2: DETALHE (PRODUTOS + PROMOÇÃO) ───────────────
async function openDetail(supermarketId, supermarketName, date) {

    const params = new URLSearchParams();
    params.append("supermarketId", supermarketId);
    if (date) params.append("date", date);

    try {
        const response = await fetch(`${API_URL}/purchases/detail?${params.toString()}`, {
            headers: authHeaders()
        });

        if (!response.ok) {
            throw new Error("Erro ao carregar detalhe");
        }

        const purchases = await response.json();
        renderDetail(purchases, supermarketName);

        document.getElementById("summaryScreen").style.display = "none";
        document.getElementById("detailScreen").style.display  = "block";

        // "Voltar" agora fecha o detalhe, em vez de sair da tela de relatório
        document.getElementById("backBtn").onclick = closeDetail;

    } catch (err) {
        console.error("Erro ao carregar detalhe:", err);
        alert("Erro ao carregar detalhe da compra");
    }
}

function renderDetail(purchases, supermarketName) {

    document.getElementById("detailTitle").innerText = supermarketName;

    const container = document.getElementById("detailList");
    const totalEl    = document.getElementById("detailTotal");

    container.innerHTML = "";

    if (!purchases || purchases.length === 0) {
        container.innerHTML = `<div class="glass-card">Nenhuma compra encontrada</div>`;
        totalEl.innerText = "";
        return;
    }

    let grandTotal = 0;

    purchases.forEach(purchase => {

        grandTotal += Number(purchase.total);

        const purchaseHeader = document.createElement("div");
        purchaseHeader.className = "section-title";
        purchaseHeader.innerText = `${formatDate(purchase.purchaseDate)} — Total: ${formatCurrency(purchase.total)}`;
        container.appendChild(purchaseHeader);

        if (!purchase.items || purchase.items.length === 0) {
            const empty = document.createElement("div");
            empty.className = "glass-card";
            empty.innerText = "Nenhum produto registrado nessa compra";
            container.appendChild(empty);
            return;
        }

        purchase.items.forEach(item => {

            const div = document.createElement("div");
            div.className = "item-card";

            const promoBadge = item.promotionActive
                ? `<div class="item-card-promo">🏷️ ${item.promotionType || "Promoção ativa"}</div>`
                : `<div class="item-card-promo">Sem promoção</div>`;

            div.innerHTML = `
                <div class="item-card-info">
                    <span>${item.productName} — ${item.quantity}x ${formatCurrency(item.unitPrice)}</span>
                    ${promoBadge}
                </div>
                <div class="item-card-subtotal">${formatCurrency(item.subtotal)}</div>
            `;

            container.appendChild(div);
        });
    });

    totalEl.innerText = `Total geral: ${formatCurrency(grandTotal)}`;
}

function closeDetail() {
    document.getElementById("detailScreen").style.display  = "none";
    document.getElementById("summaryScreen").style.display = "block";

    // "Voltar" volta a sair da tela de relatório
    document.getElementById("backBtn").onclick = goBack;
}

// ── CLEAR FILTERS ─────────────────────────────────────────
window.clearFilters = function () {
    document.getElementById("filterSupermarket").value = "";
    document.getElementById("filterDate").value = "";
    showEmptyState();
};

// ── GO BACK ────────────────────────────────────────────────
window.goBack = function () {
    window.location.href = "reports-menu.html";
};

// ── HELPERS ───────────────────────────────────────────────
function formatDate(isoDate) {
    const [year, month, day] = isoDate.split("-");
    return `${day}/${month}/${year}`;
}

function formatCurrency(value) {
    return Number(value).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}