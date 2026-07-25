/**
 * new-purchase.js
 * Módulo "Nova Compra" — carrega os supermercados já cadastrados
 * na lista suspensa, permite adicionar vários itens (produtos)
 * e salva a compra completa (cabeçalho + itens).
 */

const API_URL = "http://localhost:8080";

// itens adicionados localmente antes de salvar a compra
let purchaseItems = [];

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

    document.getElementById("purchaseDate").value =
        new Date().toISOString().split("T")[0];

    loadSupermarkets();
    renderItems();
});

// ── CARREGA OS SUPERMERCADOS JÁ CADASTRADOS NO <select> ──
async function loadSupermarkets() {

    const select = document.getElementById("supermarketSelect");

    try {
        const response = await fetch(API_URL + "/supermarkets", {
            headers: authHeaders()
        });

        if (!response.ok) {
            throw new Error("Erro ao carregar supermercados");
        }

        const supermarkets = await response.json();

        select.innerHTML = "";

        if (supermarkets.length === 0) {
            select.innerHTML = `<option value="">Nenhum supermercado cadastrado</option>`;
            return;
        }

        select.innerHTML = `<option value="">Selecione o supermercado...</option>`;

        supermarkets.forEach(item => {
            const option = document.createElement("option");
            option.value = item.id;
            option.textContent = item.name;
            select.appendChild(option);
        });

    } catch (err) {
        console.error("Erro ao carregar supermercados:", err);
        select.innerHTML = `<option value="">Erro ao carregar supermercados</option>`;
    }
}

// ── MOSTRA/ESCONDE OS CAMPOS DE PROMOÇÃO ─────────────────
window.togglePromotionFields = function () {
    const checked = document.getElementById("promotionActive").checked;
    document.getElementById("promotionFields").classList.toggle("hidden", !checked);
};

// ── ADICIONA UM ITEM À LISTA LOCAL ───────────────────────
window.addItem = function () {

    const productName  = document.getElementById("productName").value.trim();
    const quantity      = parseInt(document.getElementById("quantity").value, 10);
    const unitPrice     = parseFloat(document.getElementById("unitPrice").value);
    const promotionActive = document.getElementById("promotionActive").checked;
    const promotionType   = document.getElementById("promotionType").value.trim();
    const promotionDescription = document.getElementById("promotionDescription").value.trim();

    if (!productName) {
        alert("Informe o nome do produto!");
        return;
    }

    if (!quantity || quantity <= 0) {
        alert("Informe uma quantidade válida!");
        return;
    }

    if (!unitPrice || unitPrice <= 0) {
        alert("Informe um preço unitário válido!");
        return;
    }

    const subtotal = quantity * unitPrice;

    purchaseItems.push({
        productName,
        quantity,
        unitPrice,
        promotionActive,
        promotionType: promotionActive ? (promotionType || null) : null,
        promotionDescription: promotionActive ? (promotionDescription || null) : null,
        subtotal
    });

    document.getElementById("productName").value = "";
    document.getElementById("quantity").value = "";
    document.getElementById("unitPrice").value = "";
    document.getElementById("promotionActive").checked = false;
    document.getElementById("promotionType").value = "";
    document.getElementById("promotionDescription").value = "";
    togglePromotionFields();

    renderItems();
};

// ── REMOVE UM ITEM DA LISTA LOCAL ────────────────────────
window.removeItem = function (index) {
    purchaseItems.splice(index, 1);
    renderItems();
};

// ── RENDERIZA A LISTA DE ITENS E O TOTAL ─────────────────
function renderItems() {

    const container = document.getElementById("itemsList");
    container.innerHTML = "";

    let total = 0;

    purchaseItems.forEach((item, index) => {

        total += item.subtotal;

        const div = document.createElement("div");
        div.className = "item-card";

        div.innerHTML = `
            <div class="item-card-info">
                <span>${item.productName} — ${item.quantity}x R$ ${item.unitPrice.toFixed(2)}</span>
                ${item.promotionActive
                    ? `<span class="item-card-promo">🏷️ ${item.promotionType || "Promoção"}${item.promotionDescription ? " — " + item.promotionDescription : ""}</span>`
                    : ""}
            </div>
            <div class="item-card-subtotal">R$ ${item.subtotal.toFixed(2)}</div>
            <button class="remove-item-btn" onclick="removeItem(${index})">✕</button>
        `;

        container.appendChild(div);
    });

    document.getElementById("totalDisplay").textContent =
        "Total: R$ " + total.toFixed(2);
}

// ── SALVA A NOVA COMPRA (CABEÇALHO + ITENS) ──────────────
window.savePurchase = async function () {

    const supermarketId = document.getElementById("supermarketSelect").value;
    const purchaseDate  = document.getElementById("purchaseDate").value;

    if (!supermarketId) {
        alert("Selecione um supermercado!");
        return;
    }

    if (purchaseItems.length === 0) {
        alert("Adicione pelo menos um item à compra!");
        return;
    }

    try {
        const response = await fetch(API_URL + "/purchases", {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({
                supermarketId: Number(supermarketId),
                purchaseDate: purchaseDate || null,
                items: purchaseItems
            })
        });

        if (!response.ok) {
            throw new Error("Erro ao salvar compra");
        }

        alert("Compra registrada com sucesso!");
        window.location.href = "supermarket.html";

    } catch (err) {
        console.error("Erro ao salvar compra:", err);
        alert("Erro ao registrar compra");
    }
};

// ── VOLTAR ─────────────────────────────────────────────────
window.goBack = function () {
    window.location.href = "../html/supermarket.html";
};