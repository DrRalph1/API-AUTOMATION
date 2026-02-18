package com.usg.apiAutomation.helpers;

import com.google.i18n.phonenumbers.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
public class SMSRequestHelper {

    @Autowired
    private LoggerUtil loggerUtil;


    // FOR ROKEL (LIVE)
    public Map<String, Object> sendSMSRCBank(String destination, String message) {
        try {
            System.out.println("[SMSRequestHelper] Starting submitMessageTruInfoBIBRCB");
            loggerUtil.log(Level.INFO, "api-automation", "SMSRequestHelper.submitMessageTruInfoBIBRCB",
                    "Submitting message to Infobip API (SierraHive): " + message);

            int referenceNo = new Random().nextInt();
            String encodedMessage = URLEncoder.encode(message, "UTF-8");

            String queryParams = "clientid=" + URLEncoder.encode("ef21abfe83d3", "UTF-8") +
                    "&clientsecret=" + URLEncoder.encode("a83b6ed00f9f42b8ac665b6cf2992eec", "UTF-8") +
                    "&token=" + URLEncoder.encode("0a2f6b1b60104851855ea99b7e3a4cbb", "UTF-8") +
                    "&from=" + URLEncoder.encode("RCBank", "UTF-8") +
                    "&to=" + URLEncoder.encode(destination, "UTF-8") +
                    "&reference=" + URLEncoder.encode(String.valueOf(referenceNo), "UTF-8") +
                    "&content=" + encodedMessage +
                    "&callbackUrl=" + URLEncoder.encode("http://mycompany.com/sms/callback", "UTF-8");

            URL sendUrl = new URL("https://plx/api/.sierrahive.com/v1/messages/sms?" + queryParams);
            System.out.println("[SMSRequestHelper] Created URL: " + sendUrl);

            HttpURLConnection httpConnection = (HttpURLConnection) sendUrl.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setDoInput(true);
            httpConnection.setUseCaches(false);

            StringBuilder dataFromUrl = new StringBuilder();
            try (BufferedReader dataStreamFromUrl = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()))) {
                String line;
                while ((line = dataStreamFromUrl.readLine()) != null) {
                    dataFromUrl.append(line);
                }
            }

            System.out.println("[SMSRequestHelper] Response received: " + dataFromUrl);
            loggerUtil.log(Level.INFO, "api-automation", "SMSRequestHelper.submitMessageTruInfoBIBRCB",
                    "Message submitted successfully: " + dataFromUrl);

            if (dataFromUrl.toString().toLowerCase().contains("pending")) {
                System.out.println("[SMSRequestHelper] Message submission accepted");
                return Map.of("responseCode", "receivedResponseFromProvider", "message", "SMS sent to customer successfully");
            } else {
                System.out.println("[SMSRequestHelper] Message submitted but not pending");
                return Map.of("responseCode", "smsSentToProvider", "message", "SMS sent to provider successfully, but not confirmed as pending");
            }

        } catch (Exception ex) {
            System.out.println("[SMSRequestHelper] Exception in submitMessageTruInfoBIBRCB: " + ex.getMessage());
            saveMessagetoFile(destination + message);
            loggerUtil.log(Level.SEVERE, "api-automation", "SMSRequestHelper.submitMessageTruInfoBIBRCB",
                    "Error submitting message to SMS provider: " + ex.getMessage());
            loggerUtil.log(Level.SEVERE, "api-automation", "SMSRequestHelper.submitMessageTruInfoBIBRCB",
                    "Exception stack trace: " + Arrays.toString(ex.getStackTrace()));

            return Map.of("responseCode", "failedToSendSMSToProvider", "message", ex.getMessage());
        }
    }



    // FOR UNION (TESTING ONLY)
    public Map<String, Object> sendSMSUNION(String destination, String message) {
        try {
            System.out.println("[SMSRequestHelper] Starting submitMessageToInfoBib");
            loggerUtil.log(Level.INFO, "api-automation", "SMSRequestHelper.submitMessageToInfoBib",
                    "Submitting message to Infobip: " + message);

            URL sendUrl = new URL("https://....api.infobip.com/sms/1/text/single");
            System.out.println("[SMSRequestHelper] Created URL: " + sendUrl);

            HttpURLConnection httpConnection = (HttpURLConnection) sendUrl.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.setUseCaches(false);

            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.setRequestProperty("Authorization", "Basic VW5pb25TeXN0ZW1zOnUkZ0AxbkYweDFvbw==");
            System.out.println("[SMSRequestHelper] HTTP headerEntities set");

            String jsonPayload = String.format(
                    "{\"from\": \"%s\", \"to\": \"%s\", \"text\": \"%s\"}",
                    "UNION",
                    destination,
                    message
            );
            System.out.println("[SMSRequestHelper] JSON payload prepared: " + jsonPayload);

            try (OutputStream os = httpConnection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
                System.out.println("[SMSRequestHelper] JSON payload sent");
            }

            StringBuilder dataFromUrl = new StringBuilder();
            try (BufferedReader dataStreamFromUrl = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()))) {
                String line;
                while ((line = dataStreamFromUrl.readLine()) != null) {
                    dataFromUrl.append(line);
                }
            }
            System.out.println("[SMSRequestHelper] Response received: " + dataFromUrl);

            loggerUtil.log(Level.INFO, "api-automation", "SMSRequestHelper.submitMessageToInfoBib",
                    "Message submitted successfully: " + dataFromUrl);

            if (dataFromUrl.toString().contains("Accepted")) {
                System.out.println("[SMSRequestHelper] Message submission accepted");
                loggerUtil.log(Level.INFO, "api-automation", "SMSRequestHelper.submitMessageToInfoBib",
                        "Message successfully submitted to Infobip.");
                return Map.of("responseCode", "receivedResponseFromProvider", "message", "SMS sent to customer successfully");
            } else {
                System.out.println("[SMSRequestHelper] Message submitted but no success acceptance");
                loggerUtil.log(Level.WARNING, "api-automation", "SMSRequestHelper.submitMessageToInfoBib",
                        "Message was submitted, but no success response from Infobip. Response: " + dataFromUrl);
                return Map.of("responseCode", "smsSentToProvider", "message", "SMS sent to provider successfully");
            }

        } catch (Exception ex) {
            System.out.println("[SMSRequestHelper] Exception in submitMessageToInfoBib: " + ex.getMessage());
            saveMessagetoFile(destination + message);
            loggerUtil.log(Level.SEVERE, "api-automation", "SMSRequestHelper.submitMessageToInfoBib",
                    "Error submitting message to Infobip: " + ex.getMessage());
            loggerUtil.log(Level.SEVERE, "api-automation", "SMSRequestHelper.submitMessageToInfoBib",
                    "Exception stack trace: " + Arrays.toString(ex.getStackTrace()));

            return Map.of("responseCode", "failedToSendSMSToProvider", "message", ex.getMessage());
        }
    }



    // FOR SIB (LIVE)
    public Map<String, Object> sendSMSSIB(String destination, String message) {
        HttpURLConnection connection = null;
        try {
            System.out.println("[SMSRequestHelper] Starting submitSMS4LIBERIA");
            final String BASE_URL = "https://tester.com/http/";
            final String USERNAME = "siberrrr";
            final String PASSWORD = "44333";
            final String SENDER_ID = "sisisis";

            String formattedPhone = destination;
            System.out.println("[SMSRequestHelper] Formatted phone number for Liberia: " + formattedPhone);

            String query = String.format("username=%s&password=%s&phone=%s&senderid=%s&message=%s",
                    USERNAME,
                    PASSWORD,
                    formattedPhone,
                    SENDER_ID,
                    URLEncoder.encode(message, "UTF-8"));
            System.out.println("[SMSRequestHelper] Query string prepared: " + query);

            URL url = new URL(BASE_URL + "?" + query);
            System.out.println("[SMSRequestHelper] Full URL: " + url);

            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            System.out.println("[SMSRequestHelper] HTTP connection configured for GET");

            int responseCode = connection.getResponseCode();
            System.out.println("[SMSRequestHelper] HTTP response code: " + responseCode);

            String response;
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 200 && responseCode < 300 ?
                                    connection.getInputStream() :
                                    connection.getErrorStream()))) {
                response = in.lines().collect(Collectors.joining());
            }
            System.out.println("[SMSRequestHelper] Response body: " + response);

            loggerUtil.log(Level.INFO, "api-automation", "SMSRequestHelper.submitSMS4LIBERIA",
                    "Response: " + response);

            if (response.toLowerCase().contains("\"status\":\"sent\"")) {
                System.out.println("[SMSRequestHelper] Message sent successfully");
                return Map.of(
                        "responseCode", "receivedResponseFromProvider",
                        "message", "SMS sent to customer successfully",
                        "providerResponse", response
                );
            } else if (response.toLowerCase().contains("insufficient credit")) {
                System.out.println("[SMSRequestHelper] Insufficient credit detected");
                return Map.of(
                        "responseCode", "insuffientCredit",
                        "message", "Insufficient credit",
                        "providerResponse", response
                );
            } else {
                System.out.println("[SMSRequestHelper] Message sent to provider but unclear status");
                return Map.of(
                        "responseCode", "smsSentToProvider",
                        "message", "SMS sent to provider successfully",
                        "providerResponse", response
                );
            }

        } catch (MalformedURLException e) {
            System.out.println("[SMSRequestHelper] MalformedURLException: " + e.getMessage());
            loggerUtil.log(Level.SEVERE, "api-automation", "Invalid URL format", e);
            return Map.of(
                    "responseCode", "failedToSendSMSToProvider",
                    "message", "Invalid API tpartyAPI service"
            );
        } catch (SocketTimeoutException e) {
            System.out.println("[SMSRequestHelper] SocketTimeoutException: " + e.getMessage());
            loggerUtil.log(Level.WARNING, "api-automation", "Connection timeout", e);
            return Map.of(
                    "responseCode", "failedToSendSMSToProvider",
                    "message", "Connection timeout with SMS provider"
            );
        } catch (IOException e) {
            System.out.println("[SMSRequestHelper] IOException: " + e.getMessage());
            loggerUtil.log(Level.SEVERE, "api-automation", "Communication error", e);
            return Map.of(
                    "responseCode", "failedToSendSMSToProvider",
                    "message", "Failed to communicate with SMS provider"
            );
        } catch (Exception e) {
            System.out.println("[SMSRequestHelper] General Exception: " + e.getMessage());
            loggerUtil.log(Level.SEVERE, "api-automation", "Unknown error", e);
            return Map.of(
                    "responseCode", "failedToSendSMSToProvider",
                    "message", "Failed to communicate with SMS provider"
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
                System.out.println("[SMSRequestHelper] HTTP connection disconnected");
            }
        }
    }


    // FOR SLCB (LIVE)
    public Map<String, Object> sendSMSSLCB(String destination, String message) {
        try {
            System.out.println("[SMSRequestHelper] Starting submitMessageTrudiafaanServer");
            URL sendUrl = new URL("http://10.100.60.8:9501/http/send-message?");
            System.out.println("[SMSRequestHelper] Created URL: " + sendUrl);

            HttpURLConnection httpConnection = (HttpURLConnection) sendUrl.openConnection();

            httpConnection.setRequestMethod("POST");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.setUseCaches(false);
            System.out.println("[SMSRequestHelper] HTTP connection configured for POST");

            String formattedMessage = message.replace("\\n", System.lineSeparator());
            System.out.println("[SMSRequestHelper] Formatted message with line separators");

            String postData = "username=" + URLEncoder.encode("admin", "UTF-8") +
                    "&password=" + URLEncoder.encode("askme", "UTF-8") +
                    "&to=" + URLEncoder.encode(destination, "UTF-8") +
                    "&message-type=" + URLEncoder.encode("sms.automatic", "UTF-8") +
                    "&message=" + URLEncoder.encode(formattedMessage, "UTF-8") +
                    "&from=" + URLEncoder.encode("SLCBSMS", "UTF-8");
            System.out.println("[SMSRequestHelper] Post data prepared");

            try (DataOutputStream dataStreamToServer = new DataOutputStream(httpConnection.getOutputStream())) {
                dataStreamToServer.writeBytes(postData);
                dataStreamToServer.flush();
                System.out.println("[SMSRequestHelper] Post data sent to server");
            }

            StringBuilder dataFromUrl = new StringBuilder();
            try (BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()))) {
                String line;
                while ((line = responseReader.readLine()) != null) {
                    dataFromUrl.append(line);
                }
            }
            System.out.println("[SMSRequestHelper] Response received: " + dataFromUrl.toString());

            loggerUtil.log(Level.INFO, "api-automation", "SMSRequestHelper.submitMessageTrudiafaanServer",
                    "Response from provider: " + dataFromUrl.toString());

            if (dataFromUrl.toString().contains("Success")) {
                System.out.println("[SMSRequestHelper] Message submission success");
                loggerUtil.log(Level.INFO, "api-automation", "SMSRequestHelper.submitMessageTrudiafaanServer",
                        "Message successfully submitted to SMS service provider.");
                return Map.of("responseCode", "receivedResponseFromProvider", "message", "SMS sent to customer successfully");
            } else {
                System.out.println("[SMSRequestHelper] Message submission no success response");
                loggerUtil.log(Level.WARNING, "api-automation", "SMSRequestHelper.submitMessageTrudiafaanServer",
                        "Message was submitted, but no success response from provider. Response: " + dataFromUrl.toString());
                return Map.of("responseCode", "smsSentToProvider", "message", "SMS sent to provider successfully");
            }

        } catch (Exception ex) {
            System.out.println("[SMSRequestHelper] Exception in submitMessageTrudiafaanServer: " + ex.getMessage());
            saveMessagetoFile(destination + message);
            loggerUtil.log(Level.SEVERE, "api-automation", "SMSRequestHelper.submitMessageTrudiafaanServer",
                    "Could not submit message to SMS service provider. Saved in offline data file. Error: " + ex.getMessage());
            loggerUtil.log(Level.SEVERE, "api-automation", "SMSRequestHelper.submitMessageTrudiafaanServer",
                    "Exception stack trace: " + Arrays.toString(ex.getStackTrace()));

            return Map.of("responseCode", "failedToSendSMSToProvider", "message", ex.getMessage());
        }
    }



    // FOR UTB (LIVE) - Using SierraHive API
    public Map<String, Object> sendSMSUTB(String destination, String message) {
        try {
            System.out.println("[SMSRequestHelper] Starting sendSMSUTB");
            loggerUtil.log(Level.INFO, "api-gateway", "SMSRequestHelper.sendSMSUTB",
                    "Submitting message to SierraHive API for UTB: " + message);

            // Generate random reference number similar to the original method
            int referenceNo = new Random().nextInt(10000);
            String formattedReference = String.format("%05d", referenceNo);

            System.out.println("[SMSRequestHelper] Generated reference: " + formattedReference);
            System.out.println("[SMSRequestHelper] Destination: " + destination + ", Source: UTBSMS");

            // Create JSON payload - NOTE: Using "UTBSMS" as sender ID based on gateway code
            String jsonPayload = String.format(
                    "{\"From\":\"%s\",\"To\":\"%s\",\"Content\":\"%s\",\"Reference\":\"%s\"}",
                    "UTBSMS",  // Sender ID - CORRECTED from "UTB" to "UTBSMS"
                    destination,
                    message.replace("\"", "\\\""),  // Escape quotes in message
                    formattedReference
            );

            System.out.println("[SMSRequestHelper] JSON payload: " + jsonPayload);

            URL sendUrl = new URL("https://plx/api/.sierrahive.com/v1/messages/sms");
            System.out.println("[SMSRequestHelper] Created URL: " + sendUrl);

            HttpURLConnection httpConnection = (HttpURLConnection) sendUrl.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.setUseCaches(false);
            httpConnection.setConnectTimeout(10000);
            httpConnection.setReadTimeout(10000);

            // Set headerEntities as in the original method
            httpConnection.setRequestProperty("X-Wallet", "Token e62c9cc1868a49d1a36c5490d326a111");
            httpConnection.setRequestProperty("Authorization", "Basic ZDUyYTI4MTg6OGIxYWYzMDE3YmRkNDRiMWFlNmQ1YTNiMGM3MDZhNDQ=");
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.setRequestProperty("accept", "application/json");

            System.out.println("[SMSRequestHelper] HTTP headerEntities set");

            // Send JSON payload
            try (OutputStream os = httpConnection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
                System.out.println("[SMSRequestHelper] JSON payload sent");
            }

            // Get response
            StringBuilder dataFromUrl = new StringBuilder();
            int responseCode = httpConnection.getResponseCode();
            System.out.println("[SMSRequestHelper] HTTP response code: " + responseCode);

            try (BufferedReader dataStreamFromUrl = new BufferedReader(
                    new InputStreamReader(responseCode >= 200 && responseCode < 300 ?
                            httpConnection.getInputStream() :
                            httpConnection.getErrorStream()))) {
                String line;
                while ((line = dataStreamFromUrl.readLine()) != null) {
                    dataFromUrl.append(line);
                }
            }

            String responseString = dataFromUrl.toString();
            System.out.println("[SMSRequestHelper] Response received: " + responseString);
            loggerUtil.log(Level.INFO, "api-gateway", "SMSRequestHelper.sendSMSUTB",
                    "Message submitted successfully: " + responseString);

            // Check response
            if (responseString.toLowerCase().contains("pending") ||
                    responseString.toLowerCase().contains("success") ||
                    responseString.toLowerCase().contains("accepted") ||
                    responseString.toLowerCase().contains("\"status\":\"sent\"")) {
                System.out.println("[SMSRequestHelper] Message submission accepted");
                return Map.of(
                        "responseCode", "receivedResponseFromProvider",
                        "message", "SMS sent to customer successfully",
                        "providerResponse", responseString
                );
            } else {
                System.out.println("[SMSRequestHelper] Message submitted but unclear status");
                return Map.of(
                        "responseCode", "smsSentToProvider",
                        "message", "SMS sent to provider successfully",
                        "providerResponse", responseString
                );
            }

        } catch (Exception ex) {
            System.out.println("[SMSRequestHelper] Exception in sendSMSUTB: " + ex.getMessage());
            saveMessagetoFile("UTB_" + destination + "_" + message);
            loggerUtil.log(Level.SEVERE, "api-gateway", "SMSRequestHelper.sendSMSUTB",
                    "Error submitting message to SMS provider: " + ex.getMessage());
            loggerUtil.log(Level.SEVERE, "api-gateway", "SMSRequestHelper.sendSMSUTB",
                    "Exception stack trace: " + Arrays.toString(ex.getStackTrace()));

            return Map.of(
                    "responseCode", "failedToSendSMSToProvider",
                    "message", ex.getMessage()
            );
        }
    }



    // Save Unsuccessful SMS's To File
    public void saveMessagetoFile(String message) {
        try {
            System.out.println("[SMSRequestHelper] Saving message to offline file");
            String logFilePath = FilePathHelper.getLogFilePath("smsofflinedata.txt");
            try (FileWriter file = new FileWriter(logFilePath, true)) {
                file.write(message);
                file.write(System.lineSeparator());
                file.flush();
            }
            System.out.println("[SMSRequestHelper] Message saved to offline data file: " + logFilePath);
            loggerUtil.log(Level.INFO, "ap-gateway", "SMSRequestHelper.saveMessagetoFile", "Message saved to offline data file");
        } catch (IOException e) {
            System.out.println("[SMSRequestHelper] Error saving message to offline file: " + e.getMessage());
            loggerUtil.log(Level.INFO, "api-automation", "SMSRequestHelper.saveMessagetoFile",
                    "Error saving message to offline file: " + e.getMessage());
        }
    }



    // Format Number for Seira Leone
    public static String formatPhoneNumberSL(String phoneNumber) {
        System.out.println("[SMSRequestHelper] Formatting phone number: " + phoneNumber);
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            String normalizedNumber = phoneNumber.trim().replaceAll("\\s+", "");
            System.out.println("[SMSRequestHelper] Normalized number: " + normalizedNumber);

            if (normalizedNumber.startsWith("0")) {
                normalizedNumber = "+232" + normalizedNumber.substring(1);
            } else if (!normalizedNumber.startsWith("+232") && !normalizedNumber.startsWith("232")) {
                normalizedNumber = "+232" + normalizedNumber;
            } else if (!normalizedNumber.startsWith("+")) {
                normalizedNumber = "+" + normalizedNumber;
            }
            System.out.println("[SMSRequestHelper] Adjusted number with country code: " + normalizedNumber);

            Phonenumber.PhoneNumber parsedNumber = phoneUtil.parse(normalizedNumber, "SL");
            String formatted = phoneUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            System.out.println("[SMSRequestHelper] Formatted phone number E164: " + formatted);
            return formatted;

        } catch (NumberParseException e) {
            System.out.println("[SMSRequestHelper] NumberParseException: " + e.getMessage());
            return phoneNumber;
        }
    }


    // Format Number for Liberia
    public static String formatPhoneNumberLIB(String phoneNumber) {
        System.out.println("[SMSRequestHelper] Formatting phone number for SIB: " + phoneNumber);
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            String normalizedNumber = phoneNumber.trim().replaceAll("\\s+", "");
            System.out.println("[SMSRequestHelper] Normalized number: " + normalizedNumber);

            if (normalizedNumber.startsWith("0")) {
                normalizedNumber = "+231" + normalizedNumber.substring(1);
            } else if (!normalizedNumber.startsWith("+231") && !normalizedNumber.startsWith("231")) {
                normalizedNumber = "+231" + normalizedNumber;
            } else if (normalizedNumber.startsWith("231") && !normalizedNumber.startsWith("+")) {
                normalizedNumber = "+" + normalizedNumber;
            }
            System.out.println("[SMSRequestHelper] Adjusted number with country code: " + normalizedNumber);

            Phonenumber.PhoneNumber parsedNumber = phoneUtil.parse(normalizedNumber, "LR");
            String formatted = phoneUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            System.out.println("[SMSRequestHelper] Formatted phone number E164: " + formatted);
            return formatted;

        } catch (NumberParseException e) {
            System.out.println("[SMSRequestHelper] NumberParseException in SIB format: " + e.getMessage());
            String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");
            if (digitsOnly.startsWith("0")) {
                String formatted = "231" + digitsOnly.substring(1);
                System.out.println("[SMSRequestHelper] Digits only formatted number: " + formatted);
                return formatted;
            } else if (!digitsOnly.startsWith("231")) {
                String formatted = "231" + digitsOnly;
                System.out.println("[SMSRequestHelper] Digits only formatted number: " + formatted);
                return formatted;
            } else {
                System.out.println("[SMSRequestHelper] Digits only formatted number (unchanged): " + digitsOnly);
                return digitsOnly;
            }
        }
    }
}
