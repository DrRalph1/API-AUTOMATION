package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangelogResponseDTO {
    private List<ChangelogEntryDTO> entries;
    private String collectionId;
    private String timestamp;

    public ChangelogResponseDTO(List<ChangelogEntryDTO> entries, String collectionId) {
        this.entries = entries;
        this.collectionId = collectionId;
        this.timestamp = LocalDateTime.now().toString();
    }
}