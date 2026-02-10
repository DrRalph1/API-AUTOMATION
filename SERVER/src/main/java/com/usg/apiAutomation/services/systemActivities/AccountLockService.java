package com.usg.apiAutomation.services.systemActivities;

import com.usg.apiAutomation.entities.UserEntity;
import com.usg.apiAutomation.repositories.AppUserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AccountLockService {

    @Value("${account.lockout.max.attempts:3}")
    private int maxAttempts;

    @Value("${account.lockout.duration.hours:24}")
    private int lockoutDurationHours;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SMSService smsService;

    @Transactional
    public void handleFailedLoginAttempt(String userId) throws MessagingException {
        UserEntity user = appUserRepository.findByUserIdIgnoreCase(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getAccountLockedUntil() != null) {
            return;
        }

        int newAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(newAttempts);

        if (newAttempts >= maxAttempts) {
            lockAccount(user);
        } else {
            appUserRepository.save(user);
        }
    }

    @Transactional
    public void lockAccount(UserEntity user) throws MessagingException {
        Instant lockUntil = Instant.now().plus(lockoutDurationHours, ChronoUnit.HOURS);
        appUserRepository.lockUserAccount(user.getUserId(), lockUntil, maxAttempts);

        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
            smsService.sendAccountLockedSms(user.getPhoneNumber(), user.getFullName());
        }

        // Commented out email sending temporarily
        // sendAccountLockedEmail(user);
    }


    @Transactional
    public void unlockAccount(String userId, String name, String email) throws MessagingException {
        appUserRepository.findByUserIdIgnoreCase(userId).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            appUserRepository.save(user);

            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
                smsService.sendAccountUnlockedSms(user.getPhoneNumber(), name);
            }

            // Commented out email sending temporarily
            // sendAccountUnlockedEmail(userId, name, email);
        });
    }



    public boolean isAccountLocked(String userId) {
        return appUserRepository.findByUserIdIgnoreCase(userId)
                .map(user -> user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(Instant.now()))
                .orElse(false);
    }

    public String getLockStatus(String userId) {
        return appUserRepository.findByUserIdIgnoreCase(userId)
                .map(user -> {
                    if (user.getAccountLockedUntil() == null) {
                        return "UNLOCKED";
                    } else if (user.getAccountLockedUntil().isAfter(Instant.now())) {
                        return "TEMPORARILY_LOCKED";
                    } else {
                        return "LOCK_EXPIRED";
                    }
                })
                .orElse("USER_NOT_FOUND");
    }

    private void sendAccountLockedEmail(UserEntity user) throws MessagingException {
        String subject = "Account Locked - Security Alert";
        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head><style>body{font-family:Arial,sans-serif;line-height:1.6}</style></head>" +
                "<body>" +
                "<p>Dear " + user.getFullName() + ",</p>" +
                "<p>Your account has been locked due to multiple failed login attempts on the Master API Gateway.</p>" +
                "<p>The lock will be automatically removed after " + lockoutDurationHours + " hours.</p>" +
                "<p>If you believe this was in error, please contact the infosec support team.</p>" +
                "<br/>" +
                "<p>Best Regards,<br/>Security Team</p>" +
                "</body>" +
                "</html>";

        emailService.sendEmail(user.getEmailAddress(), subject, htmlContent);
    }

    private void sendAccountUnlockedEmail(String userId, String name, String email) throws MessagingException {
        String subject = "Account Unlocked - Access Restored";
        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head><style>body{font-family:Arial,sans-serif;line-height:1.6}</style></head>" +
                "<body>" +
                "<p>Dear " + name + ",</p>" +
                "<p>Your account has been successfully unlocked and you may now log in again.</p>" +
                "<p>For your security, we recommend:</p>" +
                "<ul>" +
                "<li>Ensuring your password is strong and unique</li>" +
                "<li>Changing your password if you suspect unauthorized access</li>" +
                "<li>Enabling two-factor authentication if available</li>" +
                "</ul>" +
                "<p>If you did not request this unlock or need any assistance, please contact the infosec support team immediately.</p>" +
                "<br/>" +
                "<p>Best Regards,<br/>Security Team</p>" +
                "</body>" +
                "</html>";

        emailService.sendEmail(email, subject, htmlContent);
    }

    @Transactional
    public void resetFailedAttempts(String userId) {
        appUserRepository.findByUserIdIgnoreCase(userId).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            appUserRepository.save(user);
        });
    }
}