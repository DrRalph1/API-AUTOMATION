package com.usg.apiAutomation.dtos.collections;

import lombok.Data;
import java.util.List;

@Data
public class VariableDto {
    private String id;
    private String key;
    private String value;
    private String type;
    private boolean enabled;
}