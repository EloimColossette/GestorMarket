/**
 * register-supermarket.js
 * Handles supermarket registration and listing.
 */

const API_URL = "http://localhost:8080";

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
    console.log("System started");
});

// ── SHOW LIST SCREEN ─────────────────────────────────────
window.showListScreen = function () {
    document.getElementById("registerScreen").style.display = "none";
    document.getElementById("listScreen").style.display     = "block";
    loadSupermarkets();
};

// ── SHOW REGISTER SCREEN ─────────────────────────────────
window.showRegisterScreen = function () {
    document.getElementById("listScreen").style.display     = "none";
    document.getElementById("registerScreen").style.display = "block";
};

// ── SAVE SUPERMARKET ─────────────────────────────────────
window.saveSupermarket = async function () {
    const name = document.getElementById("name").value.trim();

    if (!name) {
        alert("Please enter the supermarket name!");
        return;
    }

    try {
        const response = await fetch(API_URL + "/supermarkets", {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({ name })
        });

        if (!response.ok) {
            throw new Error("Error saving supermarket");
        }

        alert("Supermarket registered successfully!");
        document.getElementById("name").value = "";
        loadSupermarkets();

    } catch (err) {
        console.error("Error saving supermarket:", err);
        alert("Error registering supermarket");
    }
};

// ── LOAD SUPERMARKETS ────────────────────────────────────
async function loadSupermarkets() {
    try {
        const response = await fetch(API_URL + "/supermarkets", {
            headers: authHeaders()
        });

        if (!response.ok) {
            throw new Error("Error loading supermarkets");
        }

        const data      = await response.json();
        const container = document.getElementById("supermarketList");

        container.innerHTML = "";

        if (data.length === 0) {
            container.innerHTML = `<div class="glass-card">No supermarkets registered</div>`;
            return;
        }

        data.forEach(item => {
            const div       = document.createElement("div");
            div.className   = "glass-card";
            div.innerHTML   = `
                <div class="card-name">
                    <span class="market-icon">🏪</span>
                    <span>${item.name}</span>
                </div>
                <div class="card-id">ID: ${item.id}</div>
            `;
            container.appendChild(div);
        });

    } catch (err) {
        console.error("Error loading supermarkets:", err);
        alert("Error loading supermarkets");
    }
}

// ── FILTER SUPERMARKETS ──────────────────────────────────
window.filterSupermarkets = function () {
    const input = document.getElementById("searchInput").value.toLowerCase();
    const cards = document.querySelectorAll(".glass-card");

    cards.forEach(card => {
        const text = card.innerText.toLowerCase();
        card.style.display = text.includes(input) ? "flex" : "none";
    });
};

// ── GO BACK ──────────────────────────────────────────────
window.goBack = function () {
    window.location.href = "../html/supermarket.html";
};
