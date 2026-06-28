/**
 * login.js
 * Handles user authentication.
 */

// ── SIGN IN ─────────────────────────────────────────────
async function login(event) {


    const email    = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const btn      = document.getElementById("btnLogin");
    const msg      = document.getElementById("mensagem");

    msg.innerText  = "";
    msg.className  = "";

    btn.innerText  = "Signing in...";
    btn.disabled   = true;

    try {

        const response = await fetch("http://localhost:8080/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email,
                password
            })
        });

        const data = await response.json();

        if (response.ok && data.success) {

            localStorage.setItem("token", data.data);

            msg.innerText = "Login successful!";
            msg.className = "sucesso";

            setTimeout(() => {
                window.location.href = "home.html";
            }, 1500);

        } else {

            msg.innerText = data.message || "Incorrect email or password";
            msg.className = "erro";

        }

    } catch (error) {

        msg.innerText = "Error connecting to server";
        msg.className = "erro";

    } finally {

        btn.innerText = "Entrar";
        btn.disabled  = false;

    }
}

// ADICIONADO
document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("email").addEventListener("keydown", (e) => {
        if (e.key === "Enter") login();
    });
    document.getElementById("password").addEventListener("keydown", (e) => {
        if (e.key === "Enter") login();
    });
});

// ── REDIRECT TO FORGOT PASSWORD ─────────────────────────
function forgotPassword() {
    window.location.href = "forgot-password.html";
}
