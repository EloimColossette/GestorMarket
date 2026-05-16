async function cadastrar() {
    const firstName      = document.getElementById("firstName").value.trim();
    const lastName       = document.getElementById("lastName").value.trim();
    const cpf            = document.getElementById("cpf").value.trim();
    const phoneNumber    = document.getElementById("phoneNumber").value.trim();
    const email          = document.getElementById("email").value.trim();
    const password       = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const btn = document.getElementById("btnCadastrar");
    const msg = document.getElementById("mensagem");

    msg.innerText = "";
    msg.className = "";

    // Validações no front
    if (!firstName || !lastName) {
        msg.innerText = "Nome e sobrenome são obrigatórios";
        msg.className = "erro";
        return;
    }

    if (!cpf) {
        msg.innerText = "CPF é obrigatório";
        msg.className = "erro";
        return;
    }

    if (!phoneNumber) {
        msg.innerText = "Telefone é obrigatório";
        msg.className = "erro";
        return;
    }

    if (!email) {
        msg.innerText = "Email é obrigatório";
        msg.className = "erro";
        return;
    }

    if (!password) {
        msg.innerText = "Senha é obrigatória";
        msg.className = "erro";
        return;
    }

    if (password !== confirmPassword) {
        msg.innerText = "As senhas não coincidem";
        msg.className = "erro";
        return;
    }

    btn.innerText = "Cadastrando...";
    btn.disabled = true;

    try {
        const response = await fetch("http://localhost:8080/usuarios", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                firstName,
                lastName,
                cpf,
                phoneNumber,
                email,
                password
            })
        });

        const data = await response.json();

        if (response.ok) {
            msg.innerText = "Cadastro realizado com sucesso!";
            msg.className = "sucesso";

            setTimeout(() => {
                window.location.href = "/html/login.html";
            }, 2000);

        } else {
            msg.innerText = data.message;
            msg.className = "erro";
        }

    } catch (error) {
        msg.innerText = "Erro ao conectar com o servidor";
        msg.className = "erro";
    } finally {
        btn.innerText = "Cadastrar";
        btn.disabled = false;
    }

}