package com.chat_app.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailServiceImpl implements EmailService{
    @Value("${spring.sendgrid.from-email}")
    private String from;

    @Value("${spring.sendgrid.template-id}")
    private String verifyTemplateId;

    @Value("${spring.sendgrid.verification-link}")
    private String verificationLink;

    private final SendGrid sendGrid;

    /**
     * Send email by SendGrid
     * @param to send email to someone
     * @param subject email subject
     * @param text email content
     */
    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        Content content = new Content("text/plain", text);
        Mail mail = new Mail(new Email(from), subject, new Email(to), content);

        try {
            Request request = buildSendGridRequest(mail);
            Response response = sendGrid.api(request);
        } catch (IOException e) {
            log.info("error");
        }
    }

    /**
     * Send Email with SendGrid Template
     * @param to receiver's email address
     * @param subject Template email's subject
     * @param templateId SendGrid template's id
     * @param map variables to fill the dynamic template
     */
    @Override
    public void sendTemplateEmail(String to, String subject, String templateId, Map<String, Object> map) {
        Mail mail = new Mail();
        mail.setFrom(new Email(from));
        mail.setSubject(subject);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(to));
        map.forEach(personalization::addDynamicTemplateData);

        mail.addPersonalization(personalization);
        mail.setTemplateId(templateId);

        try {
            Request request = buildSendGridRequest(mail);
            Response response = sendGrid.api(request);
        } catch (IOException e) {
            log.error("Lỗi gửi email xác nhận tới {}: {}", to, e.getMessage());
        }
    }

    /**
     *  Send Verification Email for Register
     * @param userFullname receiver's fullname
     * @param userEmail receiver's email
     */
    @Override
    public void sendVerifyEmail(String userFullname, String userEmail) {
        String path = verificationLink + "?secretCode=" + UUID.randomUUID();
        sendTemplateEmail(userEmail,
                "Xac thuc Email",
                verifyTemplateId,
                Map.of("username", userFullname, "verification_link", path));
    }

    private Request buildSendGridRequest(Mail mail) throws IOException {
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        return request;
    }
}
