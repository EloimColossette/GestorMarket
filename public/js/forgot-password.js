/**
 * forgot-password.js
 * Sends a password recovery link to the user's email.
 */

async function forgotPassword() {
    const email = document.getElementById("email").value.trim();
    const btn   = document.getElementById("btnSend");
    const msg   = document.getElementById("mensagem");

    msg.innerText = "";
    msg.className = "";

    if (!email) {
        msg.innerText = "Please enter your email";
        msg.className = "erro";
        return;
    }

    btn.innerText = "Sending...";
    btn.disabled  = true;

    try {
        const response = await fetch("http://localhost:8080/password/forgot-password", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email })
        });

        const data = await response.json();

        if (response.ok) {
            msg.innerText = "Email sent! Check your inbox.";
            msg.className = "sucesso";
        } else {
            msg.innerText = data.message;
            msg.className = "erro";
        }

    } catch (error) {
        msg.innerText = "Error connecting to server";
        msg.className = "erro";

    } finally {
        btn.innerText = "Send link";
        btn.disabled  = false;
    }
}
