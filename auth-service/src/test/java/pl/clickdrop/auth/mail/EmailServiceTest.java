package pl.clickdrop.auth.mail;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.clickdrop.auth.application.mail.EmailService;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    JavaMailSender mailSender;

    @Mock
    SpringTemplateEngine templateEngine;

    EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, templateEngine);
        ReflectionTestUtils.setField(emailService, "from", "ClickDrop <noreply@clickdrop.pl>");
        ReflectionTestUtils.setField(emailService, "frontendBaseUrl", "http://localhost:5173/");
    }

    @Test
    void sendAccountActivation_sendsHtmlMail() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("mail/account-activation"), any()))
                .thenReturn("<html>ok</html>");

        emailService.sendAccountActivation("user@clickdrop.pl", "tester", "abc123");

        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("mail/account-activation"), any());
    }

    @Test
    void buildActivationLink_addsSlashAndEncodesToken() {
        ReflectionTestUtils.setField(emailService, "frontendBaseUrl", "http://localhost:5173");

        String link = emailService.buildActivationLink("abc+123/x");

        assertThat(link).isEqualTo("http://localhost:5173/verify-email?token=abc%2B123%2Fx");
    }

    @Test
    void buildPasswordResetLink_addsSlashAndEncodesToken() {
        ReflectionTestUtils.setField(emailService, "frontendBaseUrl", "http://localhost:5173");

        String link = emailService.buildPasswordResetLink("abc+123/x");

        assertThat(link).isEqualTo("http://localhost:5173/reset-password?token=abc%2B123%2Fx");
    }
}
