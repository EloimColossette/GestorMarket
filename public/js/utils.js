// =========================
// MOSTRAR / ESCONDER SENHA
// =========================
const EYE_OPEN = `
<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
  <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
</svg>`;

const EYE_CLOSED = `
<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
  <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.477 0-8.268-2.943-9.542-7a9.97 9.97 0 012.17-3.7M6.53 6.53A9.97 9.97 0 0112 5c4.477 0 8.268 2.943 9.542 7a9.97 9.97 0 01-4.073 5.27M6.53 6.53L3 3m3.53 3.53l11 11M17.47 17.47L21 21"/>
</svg>`;

function togglePassword(inputId, iconId) {
    const input = document.getElementById(inputId);
    const icon  = document.getElementById(iconId);
    if (!input || !icon) return;

    if (input.type === "password") {
        input.type  = "text";
        icon.innerHTML = EYE_CLOSED;
    } else {
        input.type  = "password";
        icon.innerHTML = EYE_OPEN;
    }
}

// inicializa todos os toggles com o ícone SVG
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".toggle-password").forEach(el => {
        el.innerHTML = EYE_OPEN;
    });
});

// =========================
// MÁSCARA CPF
// =========================
function aplicarMascaraCPF(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;

    input.addEventListener("input", (e) => {
        let pos = input.selectionStart;
        let raw = input.value.replace(/\D/g, "").substring(0, 11);

        let formatted = "";
        if (raw.length <= 3) {
            formatted = raw;
        } else if (raw.length <= 6) {
            formatted = `${raw.slice(0,3)}.${raw.slice(3)}`;
        } else if (raw.length <= 9) {
            formatted = `${raw.slice(0,3)}.${raw.slice(3,6)}.${raw.slice(6)}`;
        } else {
            formatted = `${raw.slice(0,3)}.${raw.slice(3,6)}.${raw.slice(6,9)}-${raw.slice(9)}`;
        }

        // conta quantos separadores havia antes do cursor
        const before = input.value.slice(0, pos).replace(/\D/g, "").length;
        input.value = formatted;

        // reposiciona o cursor corretamente
        let newPos = 0;
        let digits = 0;
        for (let i = 0; i < formatted.length; i++) {
            if (/\d/.test(formatted[i])) digits++;
            if (digits === before) { newPos = i + 1; break; }
        }
        input.setSelectionRange(newPos, newPos);
    });
}

// =========================
// MÁSCARA TELEFONE
// =========================
function aplicarMascaraTelefone(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;

    const PREFIX = "+55 ";

    function format(raw) {
        raw = raw.replace(/\D/g, "").substring(0, 11);
        if (raw.length === 0) return PREFIX;

        let out = PREFIX + "(";
        out += raw.substring(0, 2);

        if (raw.length <= 2) return out;
        out += ") ";

        if (raw.length <= 7) {
            out += raw.substring(2);
        } else {
            out += raw.substring(2, 7) + "-" + raw.substring(7);
        }
        return out;
    }

    input.value = PREFIX;

    input.addEventListener("focus", () => {
        if (input.value.replace(/\D/g, "") === "55" || input.value.trim() === "") {
            input.value = PREFIX;
        }
        // manda cursor pro fim
        setTimeout(() => {
            input.setSelectionRange(input.value.length, input.value.length);
        }, 0);
    });

    input.addEventListener("keydown", (e) => {
        // protege o prefixo +55
        const minPos = PREFIX.length;
        if (
            input.selectionStart <= minPos &&
            input.selectionEnd <= minPos &&
            (e.key === "Backspace" || e.key === "Delete")
        ) {
            e.preventDefault();
        }
    });

    input.addEventListener("input", () => {
        let raw = input.value.replace(/\D/g, "");

        // garante que o 55 do prefixo não some
        if (!raw.startsWith("55")) raw = "55" + raw;
        raw = raw.substring(2); // remove o 55 pra formatar só o número

        const formatted = format(raw);
        input.value = formatted;

        // cursor sempre no fim
        input.setSelectionRange(formatted.length, formatted.length);
    });

    input.addEventListener("click", () => {
        // impede o usuário de clicar dentro do prefixo
        if (input.selectionStart < PREFIX.length) {
            input.setSelectionRange(PREFIX.length, PREFIX.length);
        }
    });
}