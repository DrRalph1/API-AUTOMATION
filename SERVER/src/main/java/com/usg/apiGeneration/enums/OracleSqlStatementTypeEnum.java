package com.usg.apiGeneration.enums; // Adjust the package to match your project structure

import lombok.Getter;

/**
 * Enum for SQL statement types
 */
@Getter
public enum OracleSqlStatementTypeEnum {
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
    CREATE_PROCEDURE("CREATE PROCEDURE"),
    CREATE_FUNCTION("CREATE FUNCTION"),
    CREATE_PACKAGE("CREATE PACKAGE"),
    CREATE_VIEW("CREATE VIEW"),
    CREATE_TABLE("CREATE TABLE"),
    CREATE_INDEX("CREATE INDEX"),
    CREATE_TRIGGER("CREATE TRIGGER"),
    CREATE_SEQUENCE("CREATE SEQUENCE"),
    CREATE_SYNONYM("CREATE SYNONYM"),
    CREATE_TYPE("CREATE TYPE"),
    ALTER("ALTER"),
    DROP("DROP"),
    TRUNCATE("TRUNCATE"),
    GRANT("GRANT"),
    REVOKE("REVOKE"),
    COMMENT("COMMENT"),
    RENAME("RENAME"),
    DDL("DDL"),
    EXPLAIN_PLAN("EXPLAIN PLAN"),
    CALL("CALL"),
    EXECUTE("EXECUTE"),
    VIEW_QUERY("VIEW QUERY"),
    UNKNOWN("UNKNOWN");

    private final String displayName;

    OracleSqlStatementTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Check if this statement type is a DDL statement
     */
    public boolean isDDL() {
        return this == CREATE_PROCEDURE || this == CREATE_FUNCTION ||
                this == CREATE_PACKAGE || this == CREATE_VIEW ||
                this == CREATE_TABLE || this == CREATE_INDEX ||
                this == CREATE_TRIGGER || this == CREATE_SEQUENCE ||
                this == CREATE_SYNONYM || this == CREATE_TYPE ||
                this == ALTER || this == DROP || this == TRUNCATE ||
                this == GRANT || this == REVOKE || this == COMMENT ||
                this == RENAME || this == DDL;
    }

    /**
     * Check if this statement type is a DML statement
     */
    public boolean isDML() {
        return this == INSERT || this == UPDATE || this == DELETE || this == MERGE;
    }

    /**
     * Check if this statement type is a query (SELECT, WITH, etc.)
     */
    public boolean isQuery() {
        return this == SELECT || this == WITH || this == VIEW_QUERY || this == EXPLAIN_PLAN;
    }

    /**
     * Check if this statement type is a PL/SQL block
     */
    public boolean isPLSQLBlock() {
        return this == PLSQL_BLOCK || this == ANONYMOUS_BLOCK;
    }

    /**
     * Check if this statement type is a procedure/function call
     */
    public boolean isProcedureCall() {
        return this == CALL || this == EXECUTE;
    }

    /**
     * Check if this statement type is a creation statement
     */
    public boolean isCreation() {
        return this == CREATE_PROCEDURE || this == CREATE_FUNCTION ||
                this == CREATE_PACKAGE || this == CREATE_VIEW ||
                this == CREATE_TABLE || this == CREATE_INDEX ||
                this == CREATE_TRIGGER || this == CREATE_SEQUENCE ||
                this == CREATE_SYNONYM || this == CREATE_TYPE;
    }
}