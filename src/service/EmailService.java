package service;

import envloader.EnvLoader;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.util.Properties;
import java.util.logging.Logger;

public class EmailService {

    private static final Logger logger =
            Logger.getLogger(EmailService.class.getName());

    private final String smtpHost;
    private final String smtpPort;
    private final String smtpUser;
    private final String smtpPassword;

    private final String emailFrom;
    private final String emailFromName;
    private final String baseUrl;

    public EmailService() {

        this.smtpHost =
                EnvLoader.get("SMTP_HOST");

        this.smtpPort =
                EnvLoader.get("SMTP_PORT");

        this.smtpUser =
                EnvLoader.get("SMTP_USER");

        this.smtpPassword =
                EnvLoader.get("SMTP_PASSWORD");

        this.emailFrom =
                EnvLoader.get("EMAIL_FROM");

        this.emailFromName =
                EnvLoader.get("EMAIL_FROM_NAME");

        this.baseUrl =
                EnvLoader.get("APP_BASE_URL");

        validarConfiguracao();
    }

    private void validarConfiguracao() {

        if (
                smtpHost == null ||
                        smtpPort == null ||
                        smtpUser == null ||
                        smtpPassword == null
        ) {

            throw new RuntimeException(
                    "SMTP não configurado"
            );
        }
    }

    public void enviarEmailRecuperacao(
            String destinatario,
            String token
    ) {

        try {

            String link =
                    baseUrl +
                            "/html/reset-password.html?token=" +
                            token;

            Properties props =
                    new Properties();

            props.put(
                    "mail.smtp.host",
                    smtpHost
            );

            props.put(
                    "mail.smtp.port",
                    smtpPort
            );

            props.put(
                    "mail.smtp.auth",
                    "true"
            );

            props.put(
                    "mail.smtp.starttls.enable",
                    "true"
            );

            Session session =
                    Session.getInstance(
                            props,
                            new Authenticator() {

                                @Override
                                protected PasswordAuthentication
                                getPasswordAuthentication() {

                                    return new PasswordAuthentication(
                                            smtpUser,
                                            smtpPassword
                                    );
                                }
                            }
                    );

            Message message =
                    new MimeMessage(session);

            message.setFrom(
                    new InternetAddress(
                            emailFrom,
                            emailFromName
                    )
            );

            message.setReplyTo(
                    InternetAddress.parse(emailFrom)
            );

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(destinatario)
            );

            message.setSubject(
                    "GestorMarket - Redefinição de senha"
            );

            String html =
                    "<html>" +

                            "<body style='margin:0;" +
                            "padding:20px;" +
                            "background:#f4f4f4;" +
                            "font-family:Arial,sans-serif'>" +

                            "<table width='100%' role='presentation'>" +
                            "<tr>" +
                            "<td align='center'>" +

                            "<table width='600' role='presentation' " +
                            "style='background:#ffffff;" +
                            "border-radius:10px;" +
                            "padding:30px;" +
                            "box-shadow:0 2px 8px rgba(0,0,0,0.08)'>" +

                            "<tr>" +
                            "<td>" +

                            "<h2 style='color:#111827;" +
                            "margin-top:0;" +
                            "margin-bottom:20px'>" +

                            "Recuperação de senha" +

                            "</h2>" +

                            "<p style='font-size:15px;" +
                            "line-height:1.6;" +
                            "color:#374151;" +
                            "margin-bottom:20px'>" +

                            "Recebemos uma solicitação " +
                            "para redefinir sua senha." +

                            "</p>" +

                            "</td>" +
                            "</tr>" +

                            "<tr>" +
                            "<td align='center' style='padding:30px 0'>" +

                            "<a href='" + link + "' " +

                            "style='background:#4F46E5;" +
                            "color:#ffffff;" +
                            "padding:14px 24px;" +
                            "text-decoration:none;" +
                            "border-radius:8px;" +
                            "display:inline-block;" +
                            "font-weight:bold;" +
                            "font-size:14px'>" +

                            "Redefinir senha" +

                            "</a>" +

                            "</td>" +
                            "</tr>" +

                            "<tr>" +
                            "<td>" +

                            "<p style='font-size:14px;" +
                            "color:#374151;" +
                            "margin-bottom:10px'>" +

                            "Ou utilize o token abaixo:" +

                            "</p>" +

                            "<p style='font-size:18px;" +
                            "font-weight:bold;" +
                            "color:#111827;" +
                            "word-break:break-word'>" +

                            token +

                            "</p>" +

                            "<p style='margin-top:30px;" +
                            "font-size:12px;" +
                            "color:#6B7280;" +
                            "line-height:1.5'>" +

                            "Se você não solicitou " +
                            "esta alteração, ignore este email." +

                            "</p>" +

                            "</td>" +
                            "</tr>" +

                            "</table>" +

                            "</td>" +
                            "</tr>" +
                            "</table>" +

                            "</body>" +
                            "</html>";

            MimeBodyPart textPart =
                    new MimeBodyPart();

            textPart.setText(
                    "Olá,\n\n" +

                            "Recebemos uma solicitação " +
                            "para redefinir sua senha.\n\n" +

                            "Acesse o link abaixo:\n" +

                            link +

                            "\n\n" +

                            "Ou utilize o token:\n" +

                            token +

                            "\n\n" +

                            "Se você não solicitou, " +
                            "ignore este email.",

                    "UTF-8"
            );

            MimeBodyPart htmlPart =
                    new MimeBodyPart();

            htmlPart.setContent(
                    html,
                    "text/html; charset=UTF-8"
            );

            Multipart multipart =
                    new MimeMultipart("alternative");

            multipart.addBodyPart(textPart);

            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);

            Transport.send(message);

            logger.info(
                    "Email enviado com sucesso"
            );

        } catch (Exception e) {

            logger.severe(
                    "Erro ao enviar email: " +
                            e.getMessage()
            );

            throw new RuntimeException(e);
        }
    }
}