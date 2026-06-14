const API_URL = "http://localhost:8080";

// ==========================
// AUTH (JWT)
// ==========================
function getToken() {
  return localStorage.getItem("token");
}

function checkAuth() {
  const token = getToken();

  if (!token) {
    window.location.href = "login.html";
    return;
  }
}

// ==========================
// FETCH COM JWT
// ==========================
async function apiFetch(url, options = {}) {
  const token = getToken();

  return fetch(API_URL + url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer " + token,
      ...(options.headers || {})
    }
  });
}

// ==========================
// NAVEGAÇÃO
// ==========================
function novaCompra() {
  window.location.href = "nova-compra.html";
}

function relatorios() {
  window.location.href = "relatorios.html";
}

// ==========================
// COMPRAS
// ==========================
async function createPurchase() {
  const res = await apiFetch("/purchases", {
    method: "POST",
    body: JSON.stringify({
      purchaseDate: new Date().toISOString()
    })
  });

  return await res.json();
}

async function addPurchaseItem(item) {
  const res = await apiFetch("/purchase-items", {
    method: "POST",
    body: JSON.stringify(item)
  });

  return await res.json();
}

// ==========================
// CARRINHO
// ==========================
let cart = [];

function addItem(productName, quantity, unitPrice) {

  cart.push({
    productName,
    quantity,
    unitPrice
  });

  console.log(cart);
}

function getTotal() {

  return cart.reduce((total, item) => {

    return total + (
      Number(item.unitPrice) *
      Number(item.quantity)
    );

  }, 0);
}

// ==========================
// FINALIZAR COMPRA
// ==========================
async function finalizePurchase() {

  try {

    const purchase =
      await createPurchase();

    for (const item of cart) {

      await addPurchaseItem({
        purchaseId: purchase.purchasesId,
        productName: item.productName,
        quantity: item.quantity,
        unitPrice: item.unitPrice,
        promotionActive: false,
        promotionType: null,
        promotionDescription: null
      });
    }

    alert(
      "Compra finalizada com sucesso!"
    );

    cart = [];

  } catch (err) {

    console.error(
      "Erro ao finalizar compra:",
      err
    );

    alert(
      "Erro ao finalizar compra."
    );
  }
}

// ==========================
// INIT
// ==========================
document.addEventListener(
  "DOMContentLoaded",
  () => {

    checkAuth();

    console.log(
      "Gestão de supermercado carregada."
    );
  }
);