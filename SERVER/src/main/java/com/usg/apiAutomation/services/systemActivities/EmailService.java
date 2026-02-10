package com.usg.apiAutomation.services.systemActivities;

import com.usg.apiAutomation.helpers.ErrorHandlingHelper;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    @Value("${app.email.address}")
    private String emailAddress;

    @Value("${app.email.password}")
    private String emailPassword;

    @Value("${app.email.host}")
    private String emailHost;

    @Value("${app.email.port}")
    private String emailPort;

    @Value("${app.email.bank-name}")
    private String bankName;

    @Value("${app.email.support}")
    private String supportEmail;

    @Value("${app.email.login-url}")
    private String loginUrl;

    @Autowired
    private LoggerUtil loggerUtil;

    @Autowired
    private ErrorHandlingHelper errorHandlingHelper;

    @Autowired
    private Environment environment;

    /**
     * Send an HTML email
     */
    public void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        int port = Integer.parseInt(emailPort.trim());

        if (port == 587 || port == 25) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", emailHost);
            props.put("mail.smtp.port", emailPort);
            props.put("mail.smtp.ssl.trust", emailHost);
        } else if (port == 465) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.socketFactory.port", emailPort);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.host", emailHost);
            props.put("mail.smtp.port", emailPort);
            props.put("mail.smtp.ssl.trust", emailHost);
        } else {
            throw new IllegalArgumentException("Unsupported SMTP port: " + port);
        }

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailAddress, emailPassword);
            }
        });

        session.setDebug(true);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html");

            loggerUtil.log("email-service",
                    "Sending email from " + emailAddress + " to " + to +
                            " via " + emailHost + ":" + emailPort);

            Transport.send(message);
        } catch (MessagingException ex) {
            loggerUtil.log("email-service", "Failed to send email: " + ex.getMessage());
            throw new MessagingException("Failed to send email: " + ex.getMessage(), ex);
        }
    }

    /**
     * Generate a welcome email template
     */
    public String createWelcomeEmail(String username, String tempPassword) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; }" +
                "        .header { background-color: #1a365d; padding: 20px; text-align: center; color: white; }" +
                "        .content { padding: 20px; background-color: #f9f9f9; }" +
                "        .footer { padding: 20px; text-align: center; font-size: 12px; color: #777; }" +
                "        .button { background-color: #1a365d; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"header\">" +
                "        <h1>" + bankName + " Smart Banking</h1>" +
                "    </div>" +
                "    <div class=\"content\">" +
                "        <h2>Welcome, " + username + "!</h2>" +
                "        <p>Your account has been successfully created with " + bankName + "'s Smart Banking platform.</p>" +
                "        <p><strong>Temporary Password:</strong> " + tempPassword + "</p>" +
                "        <p>For security reasons, you will be required to change this password when you first log in.</p>" +
                "        <p style=\"text-align: center; margin: 30px 0;\">" +
                "            <a href=\"" + loginUrl + "\" class=\"button\">Login to Your Account</a>" +
                "        </p>" +
                "        <p>If you did not request this account, please contact the support team immediately at " + supportEmail + ".</p>" +
                "    </div>" +
                "    <div class=\"footer\">" +
                "        <p>© 2025 " + bankName + ". All rights reserved.</p>" +
                "        <p>This is an automated message. Please do not reply.</p>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
