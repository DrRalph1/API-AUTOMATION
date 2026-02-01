package com.usg.apiAutomation.helpers;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateTimeHelper {  // Renamed the class to avoid conflict

    public static String formatDateTime(Instant timestamp) {
        if (timestamp == null) {
            return "Invalid Date";
        }
        try {
            return timestamp.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern(
                            "EEEE, MMMM d, yyyy 'at' hh:mm:ss a", Locale.US));
        } catch (Exception e) {
            return "Invalid Date";
        }
    }

    public static String formatDateTime(long timestamp) {
        try {
            return Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern(
                            "EEEE, MMMM d, yyyy 'at' hh:mm:ss a", Locale.US));
        } catch (Exception e) {
            return "Invalid Date";
        }
    }
}