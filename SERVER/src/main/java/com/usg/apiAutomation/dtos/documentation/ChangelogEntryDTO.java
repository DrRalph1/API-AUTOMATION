package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangelogEntryDTO {
    private String version;
    private String date;
    private List<String> changes;
    private String type; // major, minor, patch
    private String author;

    // Constructor for single change
    public ChangelogEntryDTO(String version, String date, String change) {
        this.version = version;
        this.date = date;
        this.changes = Arrays.asList(change);
        this.type = determineVersionType(version);
    }

    // Constructor for list of changes
    public ChangelogEntryDTO(String version, String date, List<String> changes) {
        this.version = version;
        this.date = date;
        this.changes = changes;
        this.type = determineVersionType(version);
    }

    // Constructor with type
    public ChangelogEntryDTO(String version, String date, List<String> changes, String type) {
        this.version = version;
        this.date = date;
        this.changes = changes;
        this.type = type;
    }

    private String determineVersionType(String version) {
        if (version.startsWith("v0.")) return "experimental";
        if (version.contains("alpha") || version.contains("beta")) return "pre-release";
        String[] parts = version.split("\\.");
        if (parts.length > 1) {
            int major = Integer.parseInt(parts[0].replace("v", ""));
            if (major == 0) return "experimental";
            return "stable";
        }
        return "stable";
    }
}