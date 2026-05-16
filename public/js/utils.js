// =========================
// MOSTRAR / ESCONDER SENHA
// =========================

function togglePassword(inputId, iconId) {

    const input =
        document.getElementById(inputId);

    const icon =
        document.getElementById(iconId);

    if (!input || !icon) {
        return;
    }

    if (input.type === "password") {

        input.type = "text";

        icon.innerText = "🚫👁️";;

    } else {

        input.type = "password";

        icon.innerText = "👁️";
    }
}

// =========================
// MÁSCARA CPF
// =========================

function aplicarMascaraCPF(inputId) {

    const input =
        document.getElementById(inputId);

    if (!input) {
        return;
    }

    input.addEventListener("input", (e) => {

        let value =
            e.target.value.replace(/\D/g, "");

        value = value.substring(0, 11);

        value = value.replace(
            /^(\d{3})(\d)/,
            "$1.$2"
        );

        value = value.replace(
            /^(\d{3})\.(\d{3})(\d)/,
            "$1.$2.$3"
        );

        value = value.replace(
            /\.(\d{3})(\d)/,
            ".$1-$2"
        );

        e.target.value = value;
    });
}

// =========================
// MÁSCARA TELEFONE
// =========================

function aplicarMascaraTelefone(inputId) {

    const input =
        document.getElementById(inputId);

    if (!input) {
        return;
    }

    // valor inicial
    input.value = "+55 ";

    input.addEventListener("focus", () => {

        if (input.value.trim() === "") {
            input.value = "+55 ";
        }
    });

    input.addEventListener("input", (e) => {

        let value =
            e.target.value.replace(/\D/g, "");

        // remove o 55 se usuário digitar
        if (value.startsWith("55")) {
            value = value.substring(2);
        }

        // máximo 11 números
        value = value.substring(0, 11);

        let formatted = "+55 ";

        if (value.length > 0) {

            formatted += "(" +
                value.substring(0, 2);

            if (value.length >= 2) {
                formatted += ") ";
            }

            if (value.length > 2) {

                formatted += value.substring(2, 7);

                if (value.length > 7) {

                    formatted += "-" +
                        value.substring(7, 11);
                }
            }
        }

        e.target.value = formatted;
    });

    input.addEventListener("keydown", (e) => {

        // impede apagar o +55
        if (
            input.selectionStart <= 4 &&
            (e.key === "Backspace" || e.key === "Delete")
        ) {
            e.preventDefault();
        }
    });
}