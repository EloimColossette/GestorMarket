const API_URL = API_BASE_URL;

// itens adicionados localmente antes de salvar a compra
let purchaseItems = [];

// controla se a lista de itens está expandida ou recolhida
let itemsListOpen = false;

// ── AUTH GUARD ───────────────────────────────────────────
(function checkAuth() {
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "login.html";
    }
})();

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

    // sugere a data de hoje já preenchida
    document.getElementById("purchaseDate").value =
        new Date().toISOString().split("T")[0];

    loadSupermarkets();
    loadProductNames();
    renderItems();
});

// ── PADRONIZA O NOME DO PRODUTO (mesma regra usada no backend) ──
// trim + colapsa espaços duplicados + Title Case ("coca  cola" -> "Coca Cola")
// Faz isso no front só para o usuário já ver o valor final antes de salvar;
// quem garante o padrão de verdade é o backend (service).
function normalizeProductName(value) {
    if (!value) return value;

    const collapsed = value.trim().replace(/\s+/g, " ");
    let result = "";
    let capitalizeNext = true;

    for (const char of collapsed) {
        if (/\p{L}/u.test(char)) {
            result += capitalizeNext ? char.toUpperCase() : char.toLowerCase();
            capitalizeNext = false;
        } else {
            result += char;
            // depois de espaço ou hífen, a próxima letra também fica maiúscula
            capitalizeNext = (char === " " || char === "-");
        }
    }

    return result;
}

// ── CARREGA OS NOMES DE PRODUTO JÁ USADOS (PARA O AUTOCOMPLETE) ──
async function loadProductNames() {

    const datalist = document.getElementById("productNameOptions");

    try {
        const response = await fetch(API_URL + "/purchase-items/product-names", {
            headers: authHeaders()
        });

        if (!response.ok) return; // autocomplete é só um "extra", não trava a tela

        const productNames = await response.json();

        datalist.innerHTML = "";
        productNames.forEach(name => {
            const option = document.createElement("option");
            option.value = name;
            datalist.appendChild(option);
        });

    } catch (err) {
        console.error("Erro ao carregar sugestões de produto:", err);
    }
}

// ── CARREGA OS SUPERMERCADOS JÁ CADASTRADOS NO <select> ──
async function loadSupermarkets() {

    const select = document.getElementById("supermarketSelect");

    try {
        const response = await fetch(API_URL + "/supermarkets", {
            headers: authHeaders()
        });

        if (!response.ok) {

            const errorText = await response.text();

            // token ausente/expirado -> manda de volta pro login
            if (response.status === 401) {
                alert("Sua sessão expirou. Faça login novamente.");
                window.location.href = "login.html";
                return;
            }

            throw new Error(
                `Erro ao carregar supermercados (HTTP ${response.status}): ${errorText}`
            );
        }

        const supermarkets = await response.json();

        select.innerHTML = "";

        if (supermarkets.length === 0) {
            select.innerHTML = `<option value="">Nenhum supermercado cadastrado</option>`;
            return;
        }

        // opção inicial vazia (obriga o usuário a escolher)
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

// ── MOSTRA/ESCONDE O BLOCO DE PROMOÇÃO (checkbox) ────────
window.togglePromotionFields = function () {
    const checked = document.getElementById("promotionActive").checked;
    document.getElementById("promotionFields").classList.toggle("hidden", !checked);

    if (!checked) {
        document.getElementById("promotionType").value = "";
        togglePromotionTypeFields();
    }
};

// ── MOSTRA SÓ OS CAMPOS DO TIPO DE PROMOÇÃO ESCOLHIDO ────
window.togglePromotionTypeFields = function () {

    const type = document.getElementById("promotionType").value;

    document.getElementById("levePagueFields")
        .classList.toggle("hidden", type !== "leve_pague");

    document.getElementById("promotionPercent")
        .classList.toggle("hidden", type !== "percentual");

    document.getElementById("promotionDiscountValue")
        .classList.toggle("hidden", type !== "valor_fixo");
};

// ── CALCULA O SUBTOTAL DE ACORDO COM O TIPO DE PROMOÇÃO ──
function calculateSubtotal({
    quantity,
    unitPrice,
    promotionActive,
    promotionType,
    promotionBuyQuantity,
    promotionPayQuantity,
    promotionPercent,
    promotionDiscountValue
}) {

    const fullPrice = quantity * unitPrice;

    if (!promotionActive || !promotionType) {
        return fullPrice;
    }

    if (promotionType === "leve_pague") {

        const fullGroups = Math.floor(quantity / promotionBuyQuantity);
        const remainder = quantity % promotionBuyQuantity;
        const payableUnits = (fullGroups * promotionPayQuantity) + remainder;

        return payableUnits * unitPrice;
    }

    if (promotionType === "percentual") {
        return fullPrice * (1 - (promotionPercent / 100));
    }

    if (promotionType === "valor_fixo") {
        const result = fullPrice - promotionDiscountValue;
        return result < 0 ? 0 : result;
    }

    return fullPrice;
}

// ── ADICIONA UM ITEM À LISTA LOCAL ───────────────────────
window.addItem = function () {

    const productName = normalizeProductName(document.getElementById("productName").value);
    const quantity     = parseInt(document.getElementById("quantity").value, 10);
    const unitPrice    = parseFloat(document.getElementById("unitPrice").value);
    const promotionActive = document.getElementById("promotionActive").checked;
    const promotionType   = document.getElementById("promotionType").value;
    const promotionDescription = document.getElementById("promotionDescription").value.trim();

    const promotionBuyQuantity = parseInt(document.getElementById("promotionBuyQuantity").value, 10);
    const promotionPayQuantity = parseInt(document.getElementById("promotionPayQuantity").value, 10);
    const promotionPercent = parseFloat(document.getElementById("promotionPercent").value);
    const promotionDiscountValue = parseFloat(document.getElementById("promotionDiscountValue").value);

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

    if (promotionActive && !promotionType) {
        alert("Selecione o tipo da promoção!");
        return;
    }

    if (promotionActive && promotionType === "leve_pague") {

        if (!promotionBuyQuantity || !promotionPayQuantity) {
            alert("Informe os valores de 'Leve' e 'Pague'!");
            return;
        }

        if (promotionPayQuantity > promotionBuyQuantity) {
            alert("'Pague' não pode ser maior que 'Leve'!");
            return;
        }
    }

    if (promotionActive && promotionType === "percentual") {

        if (!promotionPercent || promotionPercent <= 0 || promotionPercent > 100) {
            alert("Informe um percentual de desconto válido (1 a 100)!");
            return;
        }
    }

    if (promotionActive && promotionType === "valor_fixo") {

        if (!promotionDiscountValue || promotionDiscountValue <= 0) {
            alert("Informe um valor de desconto válido!");
            return;
        }
    }

    const item = {
        productName,
        quantity,
        unitPrice,
        promotionActive,
        promotionType: promotionActive ? promotionType : null,
        promotionDescription: promotionActive ? (promotionDescription || null) : null,
        promotionBuyQuantity: promotionActive && promotionType === "leve_pague" ? promotionBuyQuantity : null,
        promotionPayQuantity: promotionActive && promotionType === "leve_pague" ? promotionPayQuantity : null,
        promotionPercent: promotionActive && promotionType === "percentual" ? promotionPercent : null,
        promotionDiscountValue: promotionActive && promotionType === "valor_fixo" ? promotionDiscountValue : null
    };

    item.subtotal = calculateSubtotal(item);

    purchaseItems.push(item);

    // limpa o formulário de item
    document.getElementById("productName").value = "";
    document.getElementById("quantity").value = "";
    document.getElementById("unitPrice").value = "";
    document.getElementById("promotionActive").checked = false;
    document.getElementById("promotionType").value = "";
    document.getElementById("promotionDescription").value = "";
    document.getElementById("promotionBuyQuantity").value = "";
    document.getElementById("promotionPayQuantity").value = "";
    document.getElementById("promotionPercent").value = "";
    document.getElementById("promotionDiscountValue").value = "";
    togglePromotionFields();

    renderItems();
};

// ── REMOVE UM ITEM DA LISTA LOCAL ────────────────────────
window.removeItem = function (index) {
    purchaseItems.splice(index, 1);
    renderItems();
};

// ── TEXTO AMIGÁVEL DA PROMOÇÃO (só para exibição) ────────
function promotionLabel(item) {

    if (item.promotionType === "leve_pague") {
        return `Leve ${item.promotionBuyQuantity} Pague ${item.promotionPayQuantity}`;
    }

    if (item.promotionType === "percentual") {
        return `${item.promotionPercent}% de desconto`;
    }

    if (item.promotionType === "valor_fixo") {
        return `Desconto de R$ ${item.promotionDiscountValue.toFixed(2)}`;
    }

    return "Promoção";
}

// quantidade máxima de itens exibidos na lista inline;
// acima disso, o botão abre o modal com a lista completa
const INLINE_ITEMS_LIMIT = 5;

// ── MONTA O HTML DE UM CARD DE ITEM ──────────────────────
// Monta o card do item como elementos DOM reais, em vez de template string.
// Nome do produto e descrição da promoção vêm do usuário, então usamos
// sempre textContent para eles — nunca innerHTML/onclick com esses valores,
// o que evita XSS armazenado (ex.: produto com nome "<img onerror=...>").
function buildItemCardElement(item, index) {

    const card = document.createElement("div");
    card.className = "item-card";

    const info = document.createElement("div");
    info.className = "item-card-info";

    const infoLine = document.createElement("span");
    infoLine.textContent = `${item.productName} — ${item.quantity}x R$ ${item.unitPrice.toFixed(2)}`;
    info.appendChild(infoLine);

    if (item.promotionActive) {
        const promo = document.createElement("span");
        promo.className = "item-card-promo";
        promo.textContent = "🏷️ " + promotionLabel(item)
            + (item.promotionDescription ? " — " + item.promotionDescription : "");
        info.appendChild(promo);
    }

    const subtotal = document.createElement("div");
    subtotal.className = "item-card-subtotal";
    subtotal.textContent = `R$ ${item.subtotal.toFixed(2)}`;

    const removeBtn = document.createElement("button");
    removeBtn.className = "remove-item-btn";
    removeBtn.textContent = "✕";
    removeBtn.addEventListener("click", () => removeItem(index));

    card.appendChild(info);
    card.appendChild(subtotal);
    card.appendChild(removeBtn);

    return card;
}

// ── RENDERIZA A LISTA DE ITENS E O TOTAL ─────────────────
function renderItems() {

    let total = 0;

    const itemsList = document.getElementById("itemsList");
    const itemsListModal = document.getElementById("itemsListModal");
    itemsList.innerHTML = "";
    itemsListModal.innerHTML = "";

    purchaseItems.forEach((item, index) => {
        total += item.subtotal;
        // um node por container (não dá pra reaproveitar o mesmo elemento em dois lugares)
        itemsList.appendChild(buildItemCardElement(item, index));
        itemsListModal.appendChild(buildItemCardElement(item, index));
    });

    document.getElementById("totalDisplay").textContent =
        "Total: R$ " + total.toFixed(2);

    document.getElementById("totalDisplayModal").textContent =
        "Total: R$ " + total.toFixed(2);

    updateToggleItemsLabel(total);

    // se a lista diminuiu para dentro do limite, fecha o modal
    // (se estiver aberto) e volta pro comportamento normal
    if (purchaseItems.length <= INLINE_ITEMS_LIMIT) {
        closeItemsModal();
    }
}

// ── BOTÃO: ABRE A LISTA INLINE OU O MODAL, DEPENDENDO DA QUANTIDADE ──
window.toggleItemsList = function () {

    if (purchaseItems.length > INLINE_ITEMS_LIMIT) {
        openItemsModal();
        return;
    }

    itemsListOpen = !itemsListOpen;

    document.getElementById("itemsList").classList.toggle("hidden", !itemsListOpen);
    document.getElementById("toggleItemsArrow").classList.toggle("open", itemsListOpen);
};

// ── ABRE/FECHA O MODAL COM A LISTA COMPLETA ──────────────
window.openItemsModal = function () {
    document.getElementById("itemsList").classList.add("hidden");
    itemsListOpen = false;
    document.getElementById("toggleItemsArrow").classList.remove("open");
    document.getElementById("itemsModalOverlay").classList.remove("hidden");
};

window.closeItemsModal = function () {
    document.getElementById("itemsModalOverlay").classList.add("hidden");
};

// fecha o modal ao clicar fora da caixa (no fundo escurecido)
window.closeItemsModalOnOverlay = function (event) {
    if (event.target.id === "itemsModalOverlay") {
        closeItemsModal();
    }
};

// ── ATUALIZA O TEXTO DO BOTÃO (quantidade de itens) ──────
function updateToggleItemsLabel(total) {

    const label = document.getElementById("toggleItemsLabel");
    const arrow = document.getElementById("toggleItemsArrow");
    const count = purchaseItems.length;

    if (count === 0) {
        label.textContent = "Nenhum item adicionado";
        arrow.textContent = "▼";
        return;
    }

    const plural = count === 1 ? "item" : "itens";
    label.textContent = `${count} ${plural} adicionado(s) — R$ ${total.toFixed(2)}`;

    // acima do limite, o ícone vira uma "lupa" indicando que abre o modal
    arrow.textContent = count > INLINE_ITEMS_LIMIT ? "🔍" : "▼";
    arrow.classList.remove("open");
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
            const errorText = await response.text();
            throw new Error(errorText || "Erro ao salvar compra");
        }

        alert("Compra registrada com sucesso!");
        window.location.href = "supermarket.html";

    } catch (err) {
        console.error("Erro ao salvar compra:", err);
        alert(err.message || "Erro ao registrar compra");
    }
};

// ── VOLTAR ─────────────────────────────────────────────────
window.goBack = function () {
    window.location.href = "../html/supermarket.html";
};