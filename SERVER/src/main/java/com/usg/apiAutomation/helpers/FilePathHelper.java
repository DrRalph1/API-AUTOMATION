package com.usg.apiAutomation.helpers;

import java.io.File;

public class FilePathHelper {

    /**
     * Returns the log file path based on the operating systemActivities.
     *
     * @param filename The name of the file (e.g., log file name).
     * @return The full path to the log file.
     */
    public static String getLogFilePath(String filename) {
        String userHome = System.getProperty("userManagement.home");
        String os = System.getProperty("os.name").toLowerCase();
        String separator = File.separator;

        // Adjust file path based on the OS
        if (os.contains("win")) {
            // For Windows, the path is typically something like C:\Users\<userManagement>\masterAPIGateway
            return userHome + separator + "apiAutomation" + separator + filename;
        } else {
            // For Linux, use the same home directory approach but adjust for typical Linux
            // paths
            return "/opt/apiAutomation" + separator + filename;
        }
    }
}
