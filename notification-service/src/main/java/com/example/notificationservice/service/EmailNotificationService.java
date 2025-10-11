package com.example.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Async("notificationExecutor")
    public boolean sendEmail(String to, String subject, String body) {
        try {
            log.info("Sending email to: {}", to);

            // Simulate email sending for development
            // In production, uncomment the following:
            /*
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@smartdelivery.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            */

            // Simulated delay
            Thread.sleep(500);

            log.info("Email sent successfully to: {}", to);
            return true;

        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            return false;
        }
    }

    @Async("notificationExecutor")
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            log.info("Sending HTML email to: {}", to);

            // Simulate HTML email sending
            // In production, use MimeMessageHelper
            Thread.sleep(500);

            log.info("HTML email sent successfully to: {}", to);
            return true;

        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}", to, e);
            return false;
        }
    }
}

// ============================================
// 17. SmsNotificationService.java
// ============================================
package com.delivery.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsNotificationService {

    @Async("notificationExecutor")
    public boolean sendSms(String phoneNumber, String message) {
        try {
            log.info("Sending SMS to: {}", phoneNumber);

            // Simulate SMS sending
            // In production, integrate with Twilio, AWS SNS, or similar service
            /*
            Example Twilio integration:
            Message sms = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(twilioPhoneNumber),
                message
            ).create();
            */

            Thread.sleep(300);

            log.info("SMS sent successfully to: {} - Message: {}", phoneNumber, message);
            return true;

        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
            return false;
        }
    }

    @Async("notificationExecutor")
    public boolean sendBulkSms(java.util.List<String> phoneNumbers, String message) {
        try {
            log.info("Sending bulk SMS to {} recipients", phoneNumbers.size());

            for (String phoneNumber : phoneNumbers) {
                sendSms(phoneNumber, message);
            }

            log.info("Bulk SMS sent successfully");
            return true;

        } catch (Exception e) {
            log.error("Failed to send bulk SMS", e);
            return false;
        }
    }
}