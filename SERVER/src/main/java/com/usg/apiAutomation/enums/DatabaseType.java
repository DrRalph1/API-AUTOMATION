// enums/DatabaseType.java
package com.usg.apiAutomation.enums;

import lombok.Getter;

@Getter
public enum DatabaseType {
    ORACLE("oracle"),
    POSTGRESQL("postgresql"),
    MYSQL("mysql"),
    SQL_SERVER("sqlserver"),
    MONGODB("mongodb");

    private final String value;

    DatabaseType(String value) {
        this.value = value;
    }

    public static DatabaseType fromString(String text) {
        for (DatabaseType type : DatabaseType.values()) {
            if (type.value.equalsIgnoreCase(text) || type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        return ORACLE; // Default
    }

    public boolean isPostgreSQL() {
        return this == POSTGRESQL;
    }

    public boolean isOracle() {
        return this == ORACLE;
    }

    public boolean isMySQL() {
        return this == MYSQL;
    }

    public boolean isSqlServer() {
        return this == SQL_SERVER;
    }

    public boolean isMongoDB() {
        return this == MONGODB;
    }
}