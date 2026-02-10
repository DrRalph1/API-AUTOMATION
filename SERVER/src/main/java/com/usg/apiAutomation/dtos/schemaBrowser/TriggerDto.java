package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriggerDto {
    private String name;
    private String type;
    private String triggeringEvent;
    private String tableOwner;
    private String tableName;
    private String status;
    private String description;
    private String actionType;
    private String triggerType;
    private String firingEvent;
    private String baseObjectType;
    private String columnName;
    private String referencingNames;
    private String whenClause;
    private String crossEdition;
    private String insteadOf;
    private String enable;
    private String generated;
}