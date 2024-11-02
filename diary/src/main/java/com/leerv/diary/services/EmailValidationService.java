package com.leerv.diary.services;

import com.leerv.diary.entities.ActivationCode;
import com.leerv.diary.entities.User;
import com.leerv.diary.repositories.ActivationCodeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailValidationService {
    @Autowired
    private final JavaMailSender mailSender;
    @Autowired
    private final SpringTemplateEngine templateEngine;
    private final ActivationCodeRepository activationCodeRepository;

    @Value("${application.mailing.backend.sender-email}")
    private String senderEmail;
    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;
    @Value("${application.mailing.backend.activation-length}")
    private int activationLength;

    public void sendValidationEmail(User user) throws MessagingException {
        String activationCode = buildActivationCode(user);
        sendEmail(
                user.getEmail(),
                user.getUsername(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl + activationCode,
                activationCode,
                "Account activation"
        );
    }

    private String buildActivationCode(User user) {
        String code = generateActivationCode();
        ActivationCode activationCode = ActivationCode.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .code(code)
                .build();
        activationCodeRepository.save(activationCode);
        return code;
    }

    private String generateActivationCode() {
        String characters = "1234567890";
        StringBuilder builder = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < this.activationLength; i++) {
            int randomIndex = random.nextInt(characters.length());
            builder.append(randomIndex);
        }
        return builder.toString();
    }

    @Async
    private void sendEmail(
            String receiver,
            String username,
            EmailTemplateName emailTemplate,
            String confirmationUrl,
            String activationCode,
            String subject
    ) throws MessagingException {
        String templateName = emailTemplate == null ? "confirm-email" : emailTemplate.name();

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        mimeMessageHelper = new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name()
        );
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activationCode", activationCode);

        Context context = new Context();
        context.setVariables(properties);

        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setTo(receiver);
        mimeMessageHelper.setFrom(senderEmail);

        String template = templateEngine.process(templateName, context);
        mimeMessageHelper.setText(template, true);
        mailSender.send(mimeMessage);
    }
}
