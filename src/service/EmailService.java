package service;

import envloader.EnvLoader;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

public class EmailService {

    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    private final String smtpHost;
    private final String smtpPort;
    private final String smtpUser;
    private final String smtpPassword;
    private final String emailFrom;
    private final String emailFromName;
    private final String baseUrl;

    public EmailService() {
        this.smtpHost      = EnvLoader.get("SMTP_HOST");
        this.smtpPort      = EnvLoader.get("SMTP_PORT");
        this.smtpUser      = EnvLoader.get("SMTP_USER");
        this.smtpPassword  = EnvLoader.get("SMTP_PASSWORD");
        this.emailFrom     = EnvLoader.get("EMAIL_FROM");
        this.emailFromName = EnvLoader.get("EMAIL_FROM_NAME");
        this.baseUrl       = EnvLoader.get("APP_BASE_URL");
        validarConfiguracao();
    }

    private void validarConfiguracao() {
        if (smtpHost == null || smtpPort == null || smtpUser == null || smtpPassword == null) {
            throw new RuntimeException("SMTP não configurado corretamente");
        }
    }

    public void enviarEmailRecuperacao(String destinatario, String token) {
        try {
            String link = baseUrl + "/html/reset-password.html?token=" + token;

            Properties props = new Properties();
            props.put("mail.smtp.host",              smtpHost);
            props.put("mail.smtp.port",              smtpPort);
            props.put("mail.smtp.auth",              "true");
            props.put("mail.smtp.starttls.enable",   "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout",           "10000");
            props.put("mail.smtp.writetimeout",      "10000");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPassword);
                }
            });

            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(emailFrom, emailFromName, "UTF-8"));
            message.setReplyTo(InternetAddress.parse(emailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("Redefinição de senha - GestorMarket");
            message.setSentDate(new Date());

            message.setHeader("X-Mailer",       "GestorMarket Mailer 1.0");
            message.setHeader("X-Priority",     "3");
            message.setHeader("Precedence",      "bulk");
            message.setHeader("Auto-Submitted",  "auto-generated");
            message.setHeader("Message-ID",
                    "<" + System.currentTimeMillis() + ".gestormarket@gmail.com>"
            );

            // ── TEXTO SIMPLES ──
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(
                    "Olá,\n\n" +
                            "Recebemos uma solicitação para redefinir sua senha do GestorMarket.\n\n" +
                            "Acesse o link abaixo para criar sua nova senha:\n" +
                            link + "\n\n" +
                            "Ou copie e cole o token abaixo na tela de redefinição:\n" +
                            token + "\n\n" +
                            "Este link expira em 10 minutos.\n\n" +
                            "Se você não solicitou esta alteração, ignore este email com segurança.\n\n" +
                            "Atenciosamente,\n" +
                            "Equipe GestorMarket",
                    "UTF-8"
            );

            // ── HTML ──
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(buildHtml(link, token), "text/html; charset=UTF-8");

            Multipart multipart = new MimeMultipart("alternative");
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);

            Transport.send(message);
            logger.info("Email de recuperação enviado para: " + destinatario);

        } catch (Exception e) {
            logger.severe("Erro ao enviar email: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String buildHtml(String link, String token) {
        return "<!DOCTYPE html>" +
                "<html lang='pt-BR'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Redefinição de senha</title>" +
                "</head>" +
                "<body style='margin:0;padding:0;background-color:#f4f4f4;" +
                "font-family:Arial,Helvetica,sans-serif'>" +

                "<table role='presentation' width='100%' cellpadding='0' cellspacing='0'>" +
                "<tr><td align='center' style='padding:40px 20px'>" +

                "<table role='presentation' width='600' cellpadding='0' cellspacing='0' " +
                "style='background:#ffffff;border-radius:12px;" +
                "box-shadow:0 4px 16px rgba(0,0,0,0.08);overflow:hidden'>" +

                // header
                "<tr><td style='background:linear-gradient(135deg,#ff7a18,#2ecc71);" +
                "padding:32px 40px;text-align:center'>" +
                "<h1 style='margin:0;color:#ffffff;font-size:22px;" +
                "font-weight:700;letter-spacing:-0.5px'>GestorMarket</h1>" +
                "<p style='margin:6px 0 0;color:rgba(255,255,255,0.85);font-size:13px'>" +
                "Sistema de Gestão</p>" +
                "</td></tr>" +

                // corpo
                "<tr><td style='padding:40px'>" +

                "<h2 style='margin:0 0 16px;color:#1a1a1a;font-size:20px'>" +
                "Redefinição de senha</h2>" +

                "<p style='margin:0 0 16px;font-size:15px;line-height:1.6;color:#444444'>" +
                "Recebemos uma solicitação para redefinir a senha da sua conta no " +
                "<strong>GestorMarket</strong>.</p>" +

                "<p style='margin:0 0 28px;font-size:15px;line-height:1.6;color:#444444'>" +
                "Clique no botão abaixo para criar sua nova senha. " +
                "Este link é válido por <strong>10 minutos</strong>.</p>" +

                // botão
                "<table role='presentation' cellpadding='0' cellspacing='0' width='100%'>" +
                "<tr><td align='center' style='padding:0 0 32px'>" +
                "<a href='" + link + "' " +
                "style='background:linear-gradient(135deg,#ff7a18,#2ecc71);" +
                "color:#ffffff;padding:14px 32px;text-decoration:none;" +
                "border-radius:8px;display:inline-block;font-weight:700;" +
                "font-size:15px;letter-spacing:0.3px'>" +
                "Redefinir minha senha</a>" +
                "</td></tr>" +
                "</table>" +

                // token
                "<p style='margin:0 0 10px;font-size:14px;color:#555555'>" +
                "Ou use o token abaixo diretamente na tela de redefinição:</p>" +

                "<div style='background:#f8f8f8;border:1px solid #e0e0e0;" +
                "border-radius:8px;padding:16px;text-align:center;margin-bottom:32px'>" +
                "<code style='font-size:16px;font-weight:700;color:#1a1a1a;" +
                "letter-spacing:1px;word-break:break-all'>" + token + "</code>" +
                "</div>" +

                "<p style='margin:0;font-size:13px;color:#888888;line-height:1.6'>" +
                "Se você não solicitou esta alteração, ignore este email com segurança. " +
                "Nenhuma alteração será feita na sua conta.</p>" +

                "</td></tr>" +

                // footer
                "<tr><td style='background:#f8f8f8;padding:20px 40px;" +
                "border-top:1px solid #eeeeee;text-align:center'>" +
                "<p style='margin:0;font-size:12px;color:#aaaaaa'>" +
                "&copy; 2025 GestorMarket &bull; Este é um email automático, não responda." +
                "</p>" +
                "</td></tr>" +

                "</table>" +
                "</td></tr>" +
                "</table>" +

                "</body></html>";
    }
}