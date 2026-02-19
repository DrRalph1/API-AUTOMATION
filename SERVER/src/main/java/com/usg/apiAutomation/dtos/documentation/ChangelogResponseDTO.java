package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangelogResponse {
    private List<ChangelogEntryDto> entries;
    private String collectionId;
    private String timestamp;

    public ChangelogResponse(List<ChangelogEntryDto> entries, String collectionId) {
        this.entries = entries;
        this.collectionId = collectionId;
        this.timestamp = LocalDateTime.now().toString();
    }
}