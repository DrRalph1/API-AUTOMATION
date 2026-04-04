package com.usg.apiGeneration.dtos.apiSecurity;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.stream.Collectors;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddIPEntryRequestDTO {
    private String name;
    private String ipRange;
    private String description;
    private String endpoints;
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