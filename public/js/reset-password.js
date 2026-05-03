// Preenche o token automaticamente se o usuário clicou no link do email
window.onload = function () {
    const params = new URLSearchParams(window.location.search);
    const token = params.get("token");
    if (token) {
        document.getElementById("token").value = token;
    }
}

async function resetPassword() {
    const token = document.getElementById("token").value.trim();
    const newPassword = document.getElementById("newPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const btn = document.getElementById("btnReset");
    const msg = document.getElementById("mensagem");

    msg.innerText = "";
    msg.className = "";

    if (!token) {
        msg.innerText = "Token inválido ou ausente";
        msg.className = "erro";
        return;
    }

    if (!newPassword || !confirmPassword) {
        msg.innerText = "Preencha todos os campos";
        msg.className = "erro";
        return;
    }

    if (newPassword !== confirmPassword) {
        msg.innerText = "As senhas não coincidem";
        msg.className = "erro";
        return;
    }

    btn.innerText = "Enviando...";
    btn.disabled = true;

    try {
        const response = await fetch("http://localhost:8080/password/reset-password", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ token, newPassword })
        });

        const data = await response.json();

        if (response.ok) {
            msg.innerText = "Senha redefinida com sucesso!";
            msg.className = "sucesso";

            setTimeout(() => {
                window.location.href = "/html/login.html";
            }, 2000);

        } else {
            msg.innerText = data.message;
            msg.className = "erro";
        }

    } catch (error) {
        msg.innerText = "Erro ao conectar com servidor";
        msg.className = "erro";
    } finally {
        btn.innerText = "Redefinir senha";
        btn.disabled = false;
    }
}