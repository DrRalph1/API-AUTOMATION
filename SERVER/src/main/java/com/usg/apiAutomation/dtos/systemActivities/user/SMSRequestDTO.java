package com.usg.apiAutomation.dtos.systemActivities.user;

public class SMSRequestDTO {
        private String to;
        private String message;

        // Getters and setters
        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
