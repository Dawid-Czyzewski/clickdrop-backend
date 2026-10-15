package pl.clickdrop.auth.application.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    public void sendAccountActivation(String toEmail, String username, String rawToken) throws MessagingException {
        String activationLink = buildActivationLink(rawToken);

        Context ctx = new Context();
        ctx.setVariable("username", username);
        ctx.setVariable("activationLink", activationLink);

        String html = templateEngine.process("mail/account-activation", ctx);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setFrom(from);
        helper.setTo(toEmail);
        helper.setSubject("Confirm your ClickDrop account");
        helper.setText(html, true);

        mailSender.send(message);
    }

    public void sendPasswordReset(String toEmail, String username, String rawToken) throws MessagingException {
        String activationLink = buildPasswordResetLink(rawToken);

        Context ctx = new Context();
        ctx.setVariable("username", username);
        ctx.setVariable("activationLink", activationLink);

        String html = templateEngine.process("mail/password-reset", ctx);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setFrom(from);
        helper.setTo(toEmail);
        helper.setSubject("Reset your ClickDrop password");
        helper.setText(html, true);

        mailSender.send(message);
    }

    public String buildPasswordResetLink(String rawToken) {
        String base = frontendBaseUrl.endsWith("/") ? frontendBaseUrl : frontendBaseUrl + "/";
        String encodedToken = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        return base + "reset-password?token=" + encodedToken;
    }

    public String buildActivationLink(String rawToken) {
        String base = frontendBaseUrl.endsWith("/") ? frontendBaseUrl : frontendBaseUrl + "/";
        String encodedToken = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        return base + "verify-email?token=" + encodedToken;
    }
}
