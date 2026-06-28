/**
 * home.js
 * Auth guard and module navigation.
 */

// ── AUTH GUARD ──────────────────────────────────────────
(function checkAuth() {
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "login.html";
    }
})();

// ── MODULE NAVIGATION ───────────────────────────────────
function openModule(module) {
    const routes = {
        financeira:   "financeira.html",
        supermercado: "supermarket.html",
        compras:      "shopping-list.html"
    };
    window.location.href = routes[module] || "#";
}

// ── LOGOUT ──────────────────────────────────────────────
function logout() {
    localStorage.removeItem("token");
    window.location.href = "login.html";
}
