package com.usg.apiAutomation.services.systemActivities;

import com.usg.apiAutomation.entities.postgres.UserOtpEntity;
import com.usg.apiAutomation.repositories.postgres.AppUserRepository;
import com.usg.apiAutomation.repositories.postgres.OtpRepository;
import com.usg.apiAutomation.helpers.DateTimeHelper;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_MINUTES = 5;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SMSService smsService;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateOtp(String userId) {
        int otpNumber = secureRandom.nextInt(900_000) + 100_000; // 6-digit OTP
        return String.valueOf(otpNumber);
    }

    public UserOtpEntity saveOtp(String userId, String otpCode) {
        Instant expiration = Instant.now().plus(OTP_EXPIRATION_MINUTES, ChronoUnit.MINUTES);
        UserOtpEntity otpEntity = new UserOtpEntity(userId, otpCode, expiration);
        return otpRepository.save(otpEntity);
    }

    public void sendOtp(String userId, String name, String toEmail, String otpCode) throws MessagingException {
        String subject = "Your One-Time Password (OTP)";
        String htmlContent = "<p>Dear " + name + ",</p>" +
                "<p>Your OTP for the API Automation login is: <strong>" + otpCode + "</strong></p>" +
                "<p>This code will expire in " + OTP_EXPIRATION_MINUTES + " minutes.</p>" +
                "<p>If you did not requestEntity this, please contact support immediately.</p>" +
                "<br/><p>Best Regards,<br/>Digital Support Team</p>";
        emailService.sendEmail(toEmail, subject, htmlContent);
    }

    /**
     * Generates, saves, and sends OTP via email and SMS.
     */
    public void generateAndSendOtp(String userId, String name, String email, String phoneNumber) throws MessagingException {
        String otp = generateOtp(userId);
        UserOtpEntity savedOtp = saveOtp(userId, "000000");

        System.out.println("Your OTP is: " + otp);

        // Send OTP via SMS
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            smsService.sendSms(phoneNumber,
                    "Dear " + name + ", your OTP for the API Automation login is: " + otp +
                            ". It expires in " + OTP_EXPIRATION_MINUTES + " minutes.");
        }

        // Temporarily disabled email sending
        // sendOtp(userId, name, email, otp);
    }


    public boolean verifyOtp(String userId, String otpCode) {
        Optional<UserOtpEntity> otpOptional = otpRepository.findTopByUserIdAndVerifiedFalseOrderByCreatedAtDesc(userId);
        if (otpOptional.isEmpty()) {
            return false;
        }

        UserOtpEntity otpEntity = otpOptional.get();

        if (otpEntity.getExpirationTime().isBefore(Instant.now())) {
            return false;
        }

        if (!otpEntity.getOtpCode().equals(otpCode)) {
            return false;
        }

        otpEntity.setVerified(true);
        otpRepository.save(otpEntity);

        // Send login success notification asynchronously
        new Thread(() -> {
//    try {
            String username = appUserRepository.getUsernameByUserId(userId);
            String email = appUserRepository.getEmailAddressByUserId(userId);
            String phone = appUserRepository.getPhoneNumberByUserId(userId);

            if (phone != null && !phone.isBlank()) {
                smsService.sendSms(phone,
                        "Hi " + username + ", your login to the API Automation was successful on " +
                                DateTimeHelper.formatDateTime(Instant.now()) + ". If this wasn't you, contact support.");
            }

//        sendLoginSuccessEmail(userId, username, email);

//    } catch (MessagingException e) {
//        System.err.println("Failed to send login success notification: " + e.getMessage());
//    }
        }).start();


        return true;
    }



    private void sendLoginSuccessEmail(String userId, String username, String toEmail) throws MessagingException {
        String subject = "Successful Login Notification";
        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head><style>body{font-family:Arial,sans-serif;line-height:1.6}</style></head>" +
                "<body>" +
                "<p>Dear " + username + ",</p>" +
                "<p>Your login to the API Automation was successful on " + DateTimeHelper.formatDateTime(Instant.now()) + ".</p>" +
                "<p><strong>Login Details:</strong></p>" +
                "<ul>" +
                "<li>User ID: " + userId + "</li>" +
                "<li>Time: " + DateTimeHelper.formatDateTime(Instant.now()) + "</li>" +
                "</ul>" +
                "<p>If you did not perform this login, please contact the infosec team immediately.</p>" +
                "<br/>" +
                "<p>Best Regards,<br/>Security Team</p>" +
                "</body>" +
                "</html>";

        emailService.sendEmail(toEmail, subject, htmlContent);
    }
}
