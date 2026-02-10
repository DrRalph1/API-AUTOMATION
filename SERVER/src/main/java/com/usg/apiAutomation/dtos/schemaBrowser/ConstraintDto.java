package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConstraintDto {
    private String name;
    private String type;
    private String columns;
    private String status;
    private String validated;
    private String generated;
    private String bad;
    private String rely;
    private String lastChange;
    private String indexOwner;
    private String indexName;
    private String invalid;
    private String viewRelated;
    private String searchCondition;
    private String rOwner;
    private String rConstraintName;
    private String deleteRule;
    private String deferrable;
    private String deferred;
}