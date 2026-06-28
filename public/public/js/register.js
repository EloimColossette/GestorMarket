/**
 * register.js
 * Handles new user registration.
 */

async function register() {
    const firstName      = document.getElementById("firstName").value.trim();
    const lastName       = document.getElementById("lastName").value.trim();
    const cpf            = document.getElementById("cpf").value.trim();
    const phoneNumber    = document.getElementById("phoneNumber").value.trim();
    const email          = document.getElementById("email").value.trim();
    const password       = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const btn            = document.getElementById("btnRegister");
    const msg            = document.getElementById("mensagem");

    msg.innerText = "";
    msg.className = "";

    // ── VALIDATION ───────────────────────────────────────
    if (!firstName || !lastName) {
        msg.innerText = "First name and last name are required";
        msg.className = "erro";
        return;
    }

    if (!cpf) {
        msg.innerText = "CPF is required";
        msg.className = "erro";
        return;
    }

    if (!phoneNumber) {
        msg.innerText = "Phone number is required";
        msg.className = "erro";
        return;
    }

    if (!email) {
        msg.innerText = "Email is required";
        msg.className = "erro";
        return;
    }

    if (!password) {
        msg.innerText = "Password is required";
        msg.className = "erro";
        return;
    }

    if (password !== confirmPassword) {
        msg.innerText = "Passwords do not match";
        msg.className = "erro";
        return;
    }

    btn.innerText = "Registering...";
    btn.disabled  = true;

    try {
        const response = await fetch("http://localhost:8080/users", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ firstName, lastName, cpf, phoneNumber, email, password })
        });

        const data = await response.json();

        if (response.ok) {
            msg.innerText = "Registration successful!";
            msg.className = "sucesso";

            setTimeout(() => {
                window.location.href = "login.html";
            }, 2000);

        } else {
            msg.innerText = data.message;
            msg.className = "erro";
        }

    } catch (error) {
        msg.innerText = "Error connecting to server";
        msg.className = "erro";

    } finally {
        btn.innerText = "Register";
        btn.disabled  = false;
    }
}
