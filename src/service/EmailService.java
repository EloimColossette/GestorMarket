package service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import envloader.EnvLoader;

import java.io.IOException;
import java.util.logging.Logger;

public class EmailService {

    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    private final String apiKey;
    private final String emailFrom;
    private final String emailFromName;
    private final String baseUrl;

    public EmailService() {
        this.apiKey = EnvLoader.get("EMAIL_API_KEY");
        this.emailFrom = EnvLoader.get("EMAIL_FROM");
        this.emailFromName = EnvLoader.get("EMAIL_FROM_NAME"); // novo
        this.baseUrl = EnvLoader.get("APP_BASE_URL"); // novo

        validarConfiguracao();
    }

    private void validarConfiguracao() {
        if (apiKey == null || emailFrom == null) {
            throw new RuntimeException("Configuração de email inválida no .env");
        }
    }

    public void enviarEmailRecuperacao(String destinatario, String token) throws IOException {

        String link = baseUrl + "/html/reset-password.html?token=" + token;

        logger.info("[EMAIL] Enviando para: " + destinatario);

        Email from = new Email(emailFrom, emailFromName != null ? emailFromName : "Sistema Compras");
        Email to = new Email(destinatario);

        String subject = "Recuperação de senha";

        // =========================
        // TEXTO (anti-spam)
        // =========================
        Content textContent = new Content(
                "text/plain",
                "Olá,\n\n" +
                        "Recebemos uma solicitação para redefinir sua senha.\n\n" +
                        "Acesse o link abaixo:\n" +
                        link + "\n\n" +
                        "Ou use o token:\n" +
                        token + "\n\n" +
                        "Se você não solicitou, ignore este email."
        );

        // =========================
        // HTML (melhorado)
        // =========================
        Content htmlContent = new Content(
                "text/html",
                "<html>" +
                        "<body style='font-family: Arial; padding:20px'>" +
                        "<p>Olá,</p>" +
                        "<p>Recebemos uma solicitação para redefinir sua senha.</p>" +

                        "<a href='" + link + "' " +
                        "style='background:#4F46E5;color:white;padding:12px 20px;" +
                        "text-decoration:none;border-radius:6px;display:inline-block;margin:15px 0'>" +
                        "Redefinir senha</a>" +

                        "<p>Ou utilize o token:</p>" +
                        "<p><b>" + token + "</b></p>" +

                        "<p style='color:#888;font-size:12px'>" +
                        "Este link expira em 15 minutos.</p>" +

                        "</body></html>"
        );

        Mail mail = new Mail(from, subject, to, textContent);
        mail.addContent(htmlContent);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            logger.info("[EMAIL] Status: " + response.getStatusCode());

            if (response.getStatusCode() >= 400) {
                logger.severe("[EMAIL] ERRO: " + response.getBody());
                throw new RuntimeException("Erro ao enviar email");
            }

        } catch (IOException ex) {
            logger.severe("[EMAIL] Falha na requisição: " + ex.getMessage());
            throw ex;
        }
    }
}