/**
 * reset-password.js
 * Validates the token from the email link and sets a new password.
 */

// ── AUTO-FILL TOKEN FROM URL ─────────────────────────────
window.onload = function () {
    const params = new URLSearchParams(window.location.search);
    const token  = params.get("token");
    if (token) {
        document.getElementById("token").value = token;
    }
};

// ── RESET PASSWORD ───────────────────────────────────────
async function resetPassword() {
    const token           = document.getElementById("token").value.trim();
    const newPassword     = document.getElementById("newPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const btn             = document.getElementById("btnReset");
    const msg             = document.getElementById("mensagem");

    msg.innerText = "";
    msg.className = "";

    if (!token) {
        msg.innerText = "Invalid or missing token";
        msg.className = "erro";
        return;
    }

    if (!newPassword || !confirmPassword) {
        msg.innerText = "Please fill in all fields";
        msg.className = "erro";
        return;
    }

    if (newPassword !== confirmPassword) {
        msg.innerText = "Passwords do not match";
        msg.className = "erro";
        return;
    }

    btn.innerText = "Sending...";
    btn.disabled  = true;

    try {
        const response = await fetch("http://localhost:8080/password/reset-password", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ token, newPassword })
        });

        const data = await response.json();

        if (response.ok) {
            msg.innerText = "Password reset successfully!";
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
        btn.innerText = "Reset password";
        btn.disabled  = false;
    }
}
