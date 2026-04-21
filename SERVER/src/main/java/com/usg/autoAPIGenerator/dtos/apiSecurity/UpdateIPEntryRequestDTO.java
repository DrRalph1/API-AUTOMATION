package com.usg.autoAPIGenerator.dtos.apiSecurity;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.stream.Collectors;
import java.util.List;

@Data
public class UpdateIPEntryRequestDTO {

    private String name;

    // Updated regex to properly handle wildcard patterns
   // @Pattern(regexp = "^(([0-9]{1,3}\\.){3}[0-9]{1,3}(\\/[0-9]{1,2})?|([0-9]{1,3}\\.){1,3}\\*|([0-9]{1,3}\\.){0,3}\\*|([0-9a-fA-F:]+)(\\/[0-9]{1,3})?|([0-9]{1,3}\\.){1,3}[0-9]{1,3}(\\.[0-9]{1,3})?(\\.[0-9]{1,3})?)$",
           // message = "Invalid IP range format. Use formats like: 192.168.1.0/24, 192.168.1.100, 192.168.1.*, or 192.168.*.*")
    private String ipRange;

    private String description;

    private String endpoints;

    @Pattern(regexp = "^(active|inactive|pending)$",
            message = "Status must be active, inactive, or pending")
    private String status;

    // Add this custom setter to handle both String and Array
    @JsonSetter
    public void setEndpoints(Object endpointsObject) {
        if (endpointsObject == null) {
            this.endpoints = null;
        } else if (endpointsObject instanceof String) {
            // If it's a string, use as is
            this.endpoints = (String) endpointsObject;
        } else if (endpointsObject instanceof List) {
            // If it's an array/list, join with commas
            List<?> list = (List<?>) endpointsObject;
            this.endpoints = list.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        } else if (endpointsObject instanceof Object[]) {
            // If it's an array, join with commas
            Object[] array = (Object[]) endpointsObject;
            this.endpoints = java.util.Arrays.stream(array)
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        } else {
            this.endpoints = endpointsObject.toString();
        }
    }
}