package com.usg.apiGeneration.enums;

import lombok.Getter;

@Getter
public enum PostgreSQLSqlStatementTypeEnum {
    SELECT("SELECT"),
    WITH("WITH"),
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    MERGE("MERGE"),
    PROCEDURE("PROCEDURE"),
    FUNCTION("FUNCTION"),
    PACKAGE("PACKAGE"),
    PLSQL_BLOCK("PL/SQL BLOCK"),
    ANONYMOUS_BLOCK("ANONYMOUS BLOCK"),
    CREATE_VIEW("CREATE VIEW"),
    CREATE_TABLE("CREATE TABLE"),
    CREATE_INDEX("CREATE INDEX"),
    CREATE_TRIGGER("CREATE TRIGGER"),
    CREATE_SEQUENCE("CREATE SEQUENCE"),
    CREATE_SYNONYM("CREATE SYNONYM"),
    CREATE_TYPE("CREATE TYPE"),
    DDL("DDL"),
    EXPLAIN_PLAN("EXPLAIN PLAN"),
    CALL("CALL"),
    EXECUTE("EXECUTE"),
    VIEW_QUERY("VIEW QUERY"),
    UNKNOWN("UNKNOWN");

    private final String displayName;

    PostgreSQLSqlStatementTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

}