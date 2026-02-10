package com.usg.apiAutomation.dtos.collections;

import lombok.Data;
import java.util.List;

@Data
public class HeaderDto {
    private String id;
    private String key;
    private String value;
    private String description;
    private boolean enabled;
}