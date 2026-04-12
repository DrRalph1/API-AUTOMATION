package com.usg.autoAPIGenerator.dtos.collections;

import lombok.Data;

@Data
public class ImportRequestDTO {
    private String source;
    private String type; // postman, openapi, etc.
    private String content;
}