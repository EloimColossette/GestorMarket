function checkAuth() {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "login.html";
    }
}

checkAuth();

function openModule(module) {
    const routes = {
        financeira: "financeira.html",
        supermercado: "supermercado.html",
        compras: "compras.html"
    };

    window.location.href = routes[module] || "#";
}

function logout() {
    localStorage.removeItem("token");
    window.location.href = "login.html";
}