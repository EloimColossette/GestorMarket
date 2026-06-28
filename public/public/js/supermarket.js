/**
 * supermarket.js
 * Supermarket module — navigation, modals and reports.
 */

const API_BASE = "http://localhost:8080";

let chartInstance = null;

// ── AUTH HEADER HELPER ───────────────────────────────────
function authHeaders() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + token
    };
}

// ── NAVIGATION ───────────────────────────────────────────
function newPurchase() {
    window.location.href = "new-purchase.html";
}

// ── SUPERMARKET MODAL ────────────────────────────────────
function openSupermarketModal() {
    document.getElementById("supermarketModal").classList.remove("hidden");
}

function closeSupermarketModal() {
    document.getElementById("supermarketModal").classList.add("hidden");
}

async function saveSupermarket() {
    const name    = document.getElementById("name").value;
    const address = document.getElementById("address").value;

    await fetch(API_BASE + "/supermarkets", {
        method: "POST",
        headers: authHeaders(),
        body: JSON.stringify({ name, address })
    });

    alert("Supermarket registered!");
    closeSupermarketModal();
}

// ── REPORT + CHART ───────────────────────────────────────
async function loadReport() {
    const res  = await fetch(API_BASE + "/purchases", {
        headers: authHeaders()
    });
    const data = await res.json();

    const labels = data.map(p => p.purchaseDate);
    const values = data.map(p => p.total || 0);
    const ctx    = document.getElementById("purchaseChart");

    if (chartInstance) {
        chartInstance.destroy();
    }

    chartInstance = new Chart(ctx, {
        type: "bar",
        data: {
            labels,
            datasets: [{
                label: "Total Purchases",
                data: values
            }]
        }
    });

    const container     = document.getElementById("reportList");
    container.innerHTML = "";

    data.forEach(item => {
        const div       = document.createElement("div");
        div.className   = "card";
        div.innerHTML   = `
            📅 ${item.purchaseDate} <br/>
            💰 Total: R$ ${item.total || 0}
        `;
        container.appendChild(div);
    });
}
