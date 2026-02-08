package com.usg.apiAutomation.services.system;

import com.usg.apiAutomation.helpers.SMSRequestHelper;
import jakarta.transaction.Transactional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SMSService {

    private final Environment environment;
    private final SMSRequestHelper smsRequestHelper;

    public SMSService(Environment environment, SMSRequestHelper smsRequestHelper) {
        this.environment = environment;
        this.smsRequestHelper = smsRequestHelper;
        System.out.println("[SMSService] Initialized.");
    }

    @Transactional
    public void sendAccountLockedSms(String phoneNumber, String name) {
        String message = String.format(
                "Dear %s, your account has been locked due to multiple failed login attempts. It will be unlocked after the configured duration.",
                name
        );
        sendSms(phoneNumber, message);
    }

    @Transactional
    public void sendAccountUnlockedSms(String phoneNumber, String name) {
        String message = String.format(
                "Dear %s, your account has been unlocked. You can now log in again. Contact the InfoSec team if you didn't request this.",
                name
        );
        sendSms(phoneNumber, message);
    }

    @Transactional
    public void sendGenericSms(String phoneNumber, String message) {
        sendSms(phoneNumber, message);
    }

    /**
     * Core SMS sending logic
     */
    public void sendSms(String destination, String message) {
        System.out.println("[SMSService] Preparing to send SMS to: " + destination);

        try {
            Map<String, Object> result = smsRequestHelper.sendSMSUNION(destination, message);
            System.out.println("[SMSService] SMS send result: " + result);
        } catch (Exception e) {
            System.err.println("[SMSService] Failed to send SMS: " + e.getMessage());
            smsRequestHelper.saveMessagetoFile(destination + " " + message);
        }
    }
}
