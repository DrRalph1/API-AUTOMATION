package com.usg.apiAutomation.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PageableConverter {

    public Pageable convert(Pageable pageable, Class<?> entityClass) {
        if (!pageable.getSort().isSorted()) {
            return pageable;
        }

        List<Sort.Order> orders = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            String property = sanitizeProperty(order.getProperty());
            String mappedProperty = mapProperty(property, entityClass);

            if (isValidProperty(mappedProperty, entityClass)) {
                orders.add(new Sort.Order(order.getDirection(), mappedProperty));
            } else {
                log.warn("Invalid property '{}' mapped to '{}' will be ignored",
                        property, mappedProperty);
            }
        }

        if (orders.isEmpty()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(orders)
        );
    }

    private String sanitizeProperty(String property) {
        if (property == null) return null;
        return property.replaceAll("[\\[\\]\"]", "").trim();
    }

    private String mapProperty(String property, Class<?> entityClass) {
        // Map common incorrect property names
        if ("name".equalsIgnoreCase(property) ||
                "string".equalsIgnoreCase(property)) {
            return "roleName";
        }
        return property;
    }

    private boolean isValidProperty(String property, Class<?> entityClass) {
        try {
            // Check if property exists in entity class
            entityClass.getDeclaredField(property);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}