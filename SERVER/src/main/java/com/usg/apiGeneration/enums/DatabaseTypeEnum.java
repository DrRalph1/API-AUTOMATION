// enums/DatabaseType.java
package com.usg.apiGeneration.enums;

import lombok.Getter;

@Getter
public enum DatabaseTypeEnum {
    ORACLE("oracle"),
    POSTGRESQL("postgresql"),
    MYSQL("mysql"),
    SQL_SERVER("sqlserver"),
    MONGODB("mongodb");

    private final String value;

    DatabaseTypeEnum(String value) {
        this.value = value;
    }

    public static DatabaseTypeEnum fromString(String text) {
        for (DatabaseTypeEnum type : DatabaseTypeEnum.values()) {
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