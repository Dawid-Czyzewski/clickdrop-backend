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
        String activationLink = frontendBaseUrl + "verify-email?token=" + rawToken;

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
}
