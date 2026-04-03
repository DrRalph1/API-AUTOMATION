// PreviewDDLModal.js (Complete working version with Generate API button - SUPPORTS ALL QUERY TYPES)
import React, { useState, useEffect, useRef, useCallback } from 'react';
import ReactDOM from 'react-dom';
import { 
  X, Play, Save, Copy, Download, Upload, Hash, Terminal, AlertCircle, 
  CheckCircle, Loader, Maximize2, Minimize2, Search, Replace, Undo, Redo,
  FileText, Code, Database, Layers, Eye, EyeOff, ChevronRight, Info,
  Check, Zap, Sparkles, Folder, FolderOpen, Settings, Wrench, Table,
  View, Wrench as FunctionIcon, Package, Link as LinkIcon, GitBranch,
  GripHorizontal, GripVertical, Maximize, Beaker, ArrowLeft, Wand2 
} from 'lucide-react';
import ApiGenerationModal from './ApiGenerationModal.js';

// Database type configurations
const DATABASE_CONFIGS = {
  postgresql: {
    name: 'PostgreSQL',
    driver: 'org.postgresql.Driver',
    defaultPort: 5432,
    defaultSchema: 'public',
    quoteIdentifier: (name) => `"${name}"`,
    getDDLQuery: (objectType, objectName, schema) => {
      switch(objectType?.toUpperCase()) {
        case 'TABLE':
          return `
            SELECT 
              'CREATE TABLE ${schema || 'public'}.${objectName} (' || E'\\n' ||
              string_agg('  ' || column_name || ' ' || data_type || 
                CASE WHEN is_nullable = 'NO' THEN ' NOT NULL' ELSE '' END || 
                CASE WHEN column_default IS NOT NULL THEN ' DEFAULT ' || column_default ELSE '' END, 
                ',' || E'\\n') || 
              E'\\n);' AS ddl
            FROM information_schema.columns 
            WHERE table_schema = COALESCE($1, 'public') 
              AND table_name = $2
            GROUP BY table_name;
          `;
        case 'VIEW':
          return `
            SELECT 
              'CREATE OR REPLACE VIEW ${schema || 'public'}.${objectName} AS ' || 
              pg_get_viewdef('${schema || 'public'}.${objectName}'::regclass, true) AS ddl;
          `;
        case 'FUNCTION':
        case 'PROCEDURE':
          return `
            SELECT 
              pg_get_functiondef(p.oid) AS ddl
            FROM pg_proc p
            JOIN pg_namespace n ON p.pronamespace = n.oid
            WHERE n.nspname = COALESCE($1, 'public')
              AND p.proname = $2;
          `;
        default:
          return `SELECT '-- DDL not available for ${objectType}' AS ddl`;
      }
    },
    getObjectDDL: async (authToken, params) => {
      const { getObjectDDL } = await import('../../controllers/PostgreSQLSchemaController.js');
      return getObjectDDL(authToken, params);
    },
    executeSQL: async (authToken, params) => {
      const { executeSQL } = await import('../../controllers/PostgreSQLSchemaController.js');
      return executeSQL(authToken, params);
    }
  },
  oracle: {
    name: 'Oracle',
    driver: 'oracle.jdbc.OracleDriver',
    defaultPort: 1521,
    defaultSchema: 'HR',
    quoteIdentifier: (name) => `"${name.toUpperCase()}"`,
    getDDLQuery: (objectType, objectName, schema) => {
      switch(objectType?.toUpperCase()) {
        case 'TABLE':
          return `
            SELECT DBMS_METADATA.GET_DDL('TABLE', '${objectName}', '${schema || 'HR'}') AS ddl FROM DUAL;
          `;
        case 'VIEW':
          return `
            SELECT DBMS_METADATA.GET_DDL('VIEW', '${objectName}', '${schema || 'HR'}') AS ddl FROM DUAL;
          `;
        case 'FUNCTION':
        case 'PROCEDURE':
          return `
            SELECT DBMS_METADATA.GET_DDL('${objectType.toUpperCase()}', '${objectName}', '${schema || 'HR'}') AS ddl FROM DUAL;
          `;
        case 'PACKAGE':
          return `
            SELECT DBMS_METADATA.GET_DDL('PACKAGE', '${objectName}', '${schema || 'HR'}') AS ddl FROM DUAL;
          `;
        case 'TRIGGER':
          return `
            SELECT DBMS_METADATA.GET_DDL('TRIGGER', '${objectName}', '${schema || 'HR'}') AS ddl FROM DUAL;
          `;
        default:
          return `SELECT '-- DDL not available for ${objectType}' AS ddl FROM DUAL`;
      }
    },
    getObjectDDL: async (authToken, params) => {
      const { getObjectDDL } = await import('../../controllers/OracleSchemaController.js');
      return getObjectDDL(authToken, params);
    },
    executeSQL: async (authToken, params) => {
      const { executeSQL } = await import('../../controllers/OracleSchemaController.js');
      return executeSQL(authToken, params);
    }
  },
};

// Object type templates
const OBJECT_TEMPLATES = {
  postgresql: {
    TABLE: (name, schema) => `-- DDL for table ${schema || 'public'}.${name}
CREATE TABLE ${schema || 'public'}.${name} (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- Add your columns here:
-- ALTER TABLE ${schema || 'public'}.${name} ADD COLUMN column_name VARCHAR(255);
`,
    VIEW: (name, schema) => `-- View definition for ${schema || 'public'}.${name}
CREATE OR REPLACE VIEW ${schema || 'public'}.${name} AS
SELECT 
  id,
  created_at,
  updated_at
FROM your_table
WHERE condition = true;
`,
    FUNCTION: (name, schema) => `-- Function definition for ${schema || 'public'}.${name}
CREATE OR REPLACE FUNCTION ${schema || 'public'}.${name}()
RETURNS TABLE(id INTEGER, result TEXT) AS $$
BEGIN
  RETURN QUERY
  SELECT 1 AS id, 'Sample result' AS result;
END;
$$ LANGUAGE plpgsql;
`,
    PROCEDURE: (name, schema) => `-- Procedure definition for ${schema || 'public'}.${name}
CREATE OR REPLACE PROCEDURE ${schema || 'public'}.${name}(
  p_param1 INTEGER,
  p_param2 TEXT
)
LANGUAGE plpgsql
AS $$
BEGIN
  -- Your procedure logic here
  RAISE NOTICE 'Procedure executed with param1: %, param2: %', p_param1, p_param2;
END;
$$;
`,
    PACKAGE: (name, schema) => `-- PostgreSQL doesn't have packages, using schema instead
-- You can group functions in a schema: ${schema || 'public'}.${name}
-- Or use extensions like postgresql-plpgsql
`,
    TRIGGER: (name, schema) => `-- Trigger definition for ${schema || 'public'}.${name}
CREATE OR REPLACE FUNCTION ${schema || 'public'}.trigger_function_${name}()
RETURNS TRIGGER AS $$
BEGIN
  -- Trigger logic here
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER ${name}_trigger
  BEFORE UPDATE ON your_table
  FOR EACH ROW
  EXECUTE FUNCTION ${schema || 'public'}.trigger_function_${name}();
`
  },
  oracle: {
    TABLE: (name, schema) => `-- DDL for table ${schema || 'HR'}.${name}
CREATE TABLE ${schema || 'HR'}.${name} (
  id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  created_at DATE DEFAULT SYSDATE,
  updated_at DATE DEFAULT SYSDATE
);

-- Add your columns here:
-- ALTER TABLE ${schema || 'HR'}.${name} ADD (column_name VARCHAR2(255));
`,
    VIEW: (name, schema) => `-- View definition for ${schema || 'HR'}.${name}
CREATE OR REPLACE VIEW ${schema || 'HR'}.${name} AS
SELECT 
  id,
  created_at,
  updated_at
FROM your_table
WHERE condition = 1;
`,
    FUNCTION: (name, schema) => `-- Function definition for ${schema || 'HR'}.${name}
CREATE OR REPLACE FUNCTION ${schema || 'HR'}.${name}(
  p_param1 IN NUMBER
) RETURN VARCHAR2
IS
  v_result VARCHAR2(100);
BEGIN
  -- Your function logic here
  v_result := 'Result: ' || TO_CHAR(p_param1);
  RETURN v_result;
END ${name};
/
`,
    PROCEDURE: (name, schema) => `-- Procedure definition for ${schema || 'HR'}.${name}
CREATE OR REPLACE PROCEDURE ${schema || 'HR'}.${name}(
  p_param1 IN NUMBER,
  p_param2 IN VARCHAR2,
  p_result OUT VARCHAR2
)
IS
BEGIN
  -- Your procedure logic here
  p_result := 'Procedure executed with param1: ' || TO_CHAR(p_param1) || ', param2: ' || p_param2;
  DBMS_OUTPUT.PUT_LINE(p_result);
END ${name};
/
`,
    PACKAGE: (name, schema) => `-- Package specification for ${schema || 'HR'}.${name}
CREATE OR REPLACE PACKAGE ${schema || 'HR'}.${name} IS
  -- Public constants
  c_version CONSTANT VARCHAR2(10) := '1.0.0';
  
  -- Public types
  TYPE t_cursor IS REF CURSOR;
  
  -- Public procedures and functions
  PROCEDURE init;
  FUNCTION get_data(p_id IN NUMBER) RETURN VARCHAR2;
END ${name};
/

-- Package body
CREATE OR REPLACE PACKAGE BODY ${schema || 'HR'}.${name} IS
  PROCEDURE init IS
  BEGIN
    DBMS_OUTPUT.PUT_LINE('Package initialized');
  END init;
  
  FUNCTION get_data(p_id IN NUMBER) RETURN VARCHAR2 IS
    v_result VARCHAR2(100);
  BEGIN
    SELECT 'Data for ID: ' || TO_CHAR(p_id) INTO v_result FROM DUAL;
    RETURN v_result;
  END get_data;
END ${name};
/
`,
    TRIGGER: (name, schema) => `-- Trigger definition for ${schema || 'HR'}.${name}
CREATE OR REPLACE TRIGGER ${schema || 'HR'}.${name}
  BEFORE UPDATE ON your_table
  FOR EACH ROW
BEGIN
  :NEW.updated_at := SYSDATE;
END ${name};
/
`
  },
};

// Helper function to extract parameters from SQL query (same as QueryEditorModal)
const extractQueryParameters = (sqlQuery) => {
  const paramRegex = /:(\w+)/g;
  const matches = [];
  let match;
  while ((match = paramRegex.exec(sqlQuery)) !== null) {
    if (!matches.includes(match[1])) {
      matches.push(match[1]);
    }
  }
  
  return matches.map((param, index) => ({
    key: param,
    parameterName: param,
    dataType: 'VARCHAR2',
    required: true,
    description: `Parameter: ${param}`,
    parameterLocation: 'query',
    position: index
  }));
};

// Helper function to get statement type (same as QueryEditorModal)
const getStatementType = (sql) => {
  const trimmed = sql.trim().toUpperCase();
  if (/^SELECT\b/i.test(trimmed)) return 'SELECT';
  if (/^INSERT\b/i.test(trimmed)) return 'INSERT';
  if (/^UPDATE\b/i.test(trimmed)) return 'UPDATE';
  if (/^DELETE\b/i.test(trimmed)) return 'DELETE';
  if (/^CREATE\b/i.test(trimmed)) return 'DDL';
  if (/^ALTER\b/i.test(trimmed)) return 'DDL';
  if (/^DROP\b/i.test(trimmed)) return 'DDL';
  if (/^TRUNCATE\b/i.test(trimmed)) return 'DDL';
  if (/^CALL\b/i.test(trimmed)) return 'CALL';
  if (/^EXEC(UTE)?\b/i.test(trimmed)) return 'EXECUTE';
  if (/^BEGIN\b/i.test(trimmed)) return 'PLSQL';
  if (/^DECLARE\b/i.test(trimmed)) return 'PLSQL';
  return 'UNKNOWN';
};

// Extract parameter names from procedure DDL
const extractProcedureParams = (ddl, procedureName) => {
  const params = {
    input: [],
    output: []
  };
  
  try {
    const procPattern = new RegExp(`PROCEDURE\\s+${procedureName}\\s*\\(([^)]+)\\)`, 'i');
    const match = ddl.match(procPattern);
    
    if (match && match[1]) {
      const paramStr = match[1];
      const lines = paramStr.split('\n');
      
      for (let line of lines) {
        const inParamMatch = line.match(/(\w+)\s+IN\s+(\w+)/i);
        const outParamMatch = line.match(/(\w+)\s+OUT\s+(\w+)/i);
        const inOutParamMatch = line.match(/(\w+)\s+IN\s+OUT\s+(\w+)/i);
        
        if (inParamMatch) {
          params.input.push({ name: inParamMatch[1], type: inParamMatch[2] });
        } else if (outParamMatch) {
          params.output.push({ name: outParamMatch[1], type: outParamMatch[2] });
        } else if (inOutParamMatch) {
          params.input.push({ name: inOutParamMatch[1], type: inOutParamMatch[2] });
          params.output.push({ name: inOutParamMatch[1], type: inOutParamMatch[2] });
        }
      }
    }
  } catch (e) {
    console.warn('Could not parse procedure parameters', e);
  }
  
  return params;
};

const generateProcedureTestCode = (objectName, schema, databaseType, currentDate, originalDDL = '') => {
  const params = extractProcedureParams(originalDDL, objectName);
  let inputDeclarations = '';
  let outputDeclarations = '';
  let callParams = '';
  
  if (databaseType === 'oracle') {
    const inputParams = params.input.length > 0 ? params.input : [
      { name: 'acct_link_vvv', type: 'VARCHAR2(20)' },
      { name: 'amt', type: 'NUMBER' },
      { name: 'narration', type: 'VARCHAR2(200)' },
      { name: 'doc_ref', type: 'VARCHAR2(50)' },
      { name: 'batch_no', type: 'VARCHAR2(20)' },
      { name: 'post_by', type: 'VARCHAR2(30)' },
      { name: 'app_by', type: 'VARCHAR2(30)' },
      { name: 'post_terminal', type: 'VARCHAR2(30)' },
      { name: 'cust_tel', type: 'VARCHAR2(20)' },
      { name: 'trans_by', type: 'VARCHAR2(30)' },
      { name: 'trans_type', type: 'VARCHAR2(10)' },
      { name: 'db_acct_link_vvv', type: 'VARCHAR2(20)' },
      { name: 'channel_code_v', type: 'VARCHAR2(20)' },
      { name: 'api_secret_v', type: 'VARCHAR2(50)' }
    ];
    
    const outputParams = params.output.length > 0 ? params.output : [
      { name: 'mess', type: 'VARCHAR2(4000)' },
      { name: 'response_code', type: 'VARCHAR2(10)' },
      { name: 'batchNumber', type: 'VARCHAR2(20)' }
    ];
    
    inputDeclarations = inputParams.map(p => 
      `    v_${p.name.padEnd(25)} ${p.type} := ${p.name.includes('amt') ? '1000.00' : p.name.includes('doc_ref') ? "'DOC_' || TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS')" : p.name.includes('link') ? "'000221059466'" : p.name.includes('post_by') ? "'UNIONADMIN'" : p.name.includes('trans_type') ? "'FTR'" : p.name.includes('channel') ? "'MOB'" : p.name.includes('api_secret') ? "'testPC'" : p.name.includes('tel') ? "'233123456789'" : p.name.includes('trans_by') ? "'CUSTOMER001'" : p.name.includes('batch') ? 'NULL' : p.name.includes('terminal') ? "'API'" : `'TEST_VALUE'`};`
    ).join('\n');
    
    outputDeclarations = outputParams.map(p => 
      `    v_${p.name.padEnd(25)} ${p.type};`
    ).join('\n');
    
    callParams = inputParams.map(p => `        v_${p.name}`).join(',\n        ');
    callParams = callParams + (callParams ? ',\n        ' : '') + outputParams.map(p => `v_${p.name}`).join(',\n        ');
    
    return `DECLARE
    -- Input parameters - REPLACE with actual values from your database
${inputDeclarations}
    
    -- Output parameters
${outputDeclarations}
BEGIN
    -- Log what we're using (for debugging)
    DBMS_OUTPUT.PUT_LINE('=== INPUT PARAMETERS ===');
${inputParams.map(p => `    DBMS_OUTPUT.PUT_LINE('${p.name}: ' || v_${p.name});`).join('\n')}
    DBMS_OUTPUT.PUT_LINE('========================');
    
    ${schema}.${objectName}(
${callParams}
    );
    
    DBMS_OUTPUT.PUT_LINE('=== OUTPUT RESULTS ===');
${outputParams.map(p => `    DBMS_OUTPUT.PUT_LINE('${p.name}: ' || v_${p.name});`).join('\n')}
    
    IF v_response_code = '000' THEN
        DBMS_OUTPUT.PUT_LINE('✓ Transaction posted successfully!');
    ELSE
        DBMS_OUTPUT.PUT_LINE('✗ Transaction failed with code: ' || v_response_code);
    END IF;
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Error Code: ' || SQLCODE);
END;`;
  } else if (databaseType === 'postgresql') {
    return `DO $$
DECLARE
    -- Input parameters - REPLACE with actual values
    v_param1 VARCHAR(50) := 'TEST_VALUE_1';
    v_param2 INTEGER := 100;
    v_param3 DATE := CURRENT_DATE;
    
    -- Output parameters
    v_result VARCHAR(4000);
    v_status VARCHAR(10);
BEGIN
    -- Call the procedure
    CALL ${schema}.${objectName}(v_param1, v_param2, v_param3, v_result, v_status);
    
    -- Display results
    RAISE NOTICE '=== OUTPUT RESULTS ===';
    RAISE NOTICE 'Status: %', v_status;
    RAISE NOTICE 'Result: %', v_result;
    
    IF v_status = '000' THEN
        RAISE NOTICE '✓ Procedure executed successfully!';
    ELSE
        RAISE NOTICE '✗ Procedure failed with status: %', v_status;
    END IF;
    
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Error: %', SQLERRM;
END $$;`;
  }
  return `-- Test code for ${objectName} procedure`;
};

const generateFunctionTestCode = (objectName, schema, databaseType, currentDate) => {
  if (databaseType === 'oracle') {
    return `DECLARE
    -- Input parameters - REPLACE with actual values
    v_param1     VARCHAR2(50) := 'TEST_VALUE_1';
    v_param2     NUMBER := 100;
    
    -- Return value
    v_result     VARCHAR2(4000);
BEGIN
    -- Call the function
    v_result := ${schema}.${objectName}(
        p_param1 => v_param1,
        p_param2 => v_param2
    );
    
    -- Display result
    DBMS_OUTPUT.PUT_LINE('=== FUNCTION RESULT ===');
    DBMS_OUTPUT.PUT_LINE('Result: ' || v_result);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Error Code: ' || SQLCODE);
END;`;
  } else if (databaseType === 'postgresql') {
    return `DO $$
DECLARE
    -- Input parameters - REPLACE with actual values
    v_param1 VARCHAR(50) := 'TEST_VALUE_1';
    v_param2 INTEGER := 100;
    
    -- Return value
    v_result VARCHAR(4000);
BEGIN
    -- Call the function
    v_result := ${schema}.${objectName}(v_param1, v_param2);
    
    -- Display result
    RAISE NOTICE '=== FUNCTION RESULT ===';
    RAISE NOTICE 'Result: %', v_result;
    
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Error: %', SQLERRM;
END $$;`;
  }
  return `-- Test code for ${objectName} function`;
};

const extractTableColumns = (ddl) => {
  const columns = [];
  
  try {
    const createTableMatch = ddl.match(/CREATE\s+TABLE\s+[\w.]+[\s\S]*?\(([\s\S]*?)\)\s*;/i);
    
    if (createTableMatch && createTableMatch[1]) {
      const columnDefinitions = createTableMatch[1].split(',');
      
      for (let colDef of columnDefinitions) {
        colDef = colDef.trim();
        
        if (colDef.toUpperCase().startsWith('PRIMARY KEY') ||
            colDef.toUpperCase().startsWith('FOREIGN KEY') ||
            colDef.toUpperCase().startsWith('CONSTRAINT') ||
            colDef.toUpperCase().startsWith('UNIQUE') ||
            colDef.toUpperCase().startsWith('CHECK')) {
          continue;
        }
        
        let columnName = '';
        const quotedMatch = colDef.match(/^"([^"]+)"/);
        const unquotedMatch = colDef.match(/^([a-zA-Z_][a-zA-Z0-9_]*)/);
        
        if (quotedMatch) {
          columnName = quotedMatch[1];
        } else if (unquotedMatch) {
          columnName = unquotedMatch[1];
        }
        
        if (columnName) {
          let dataType = 'unknown';
          const typeMatch = colDef.match(/(?:VARCHAR|CHAR|TEXT|INTEGER|BIGINT|BOOLEAN|TIMESTAMP|DATE|JSONB|DOUBLE PRECISION)/i);
          if (typeMatch) {
            dataType = typeMatch[0].toUpperCase();
          }
          
          columns.push({
            name: columnName,
            dataType: dataType,
            isNullable: !colDef.toUpperCase().includes('NOT NULL'),
            hasDefault: colDef.toUpperCase().includes('DEFAULT')
          });
        }
      }
    }
  } catch (e) {
    console.warn('Could not extract table columns from DDL', e);
  }
  
  return columns;
};

const generateTestValue = (column, index) => {
  const dataType = column.dataType;
  const name = column.name.toLowerCase();
  
  if (name.includes('id') && !name.includes('_at')) {
    return `'TEST_${Math.random().toString(36).substring(7).toUpperCase()}'`;
  }
  if (name.includes('name') || name.includes('title') || name.includes('description')) {
    return `'Test ${column.name.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())}'`;
  }
  if (name.includes('email')) {
    return `'test@example.com'`;
  }
  if (name.includes('status')) {
    return `'ACTIVE'`;
  }
  if (name.includes('type')) {
    return `'STANDARD'`;
  }
  if (name.includes('date') || name.includes('created_at') || name.includes('updated_at')) {
    if (column.hasDefault) {
      return `DEFAULT`;
    }
    return `CURRENT_TIMESTAMP`;
  }
  if (name.includes('count') || name.includes('version') || name.includes('score')) {
    return `1`;
  }
  if (dataType.includes('BOOLEAN')) {
    return `false`;
  }
  if (dataType.includes('JSON')) {
    return `'{"test": "data"}'::jsonb`;
  }
  if (dataType.includes('DOUBLE') || dataType.includes('FLOAT')) {
    return `0.0`;
  }
  if (dataType.includes('INT')) {
    return `0`;
  }
  if (dataType.includes('TIMESTAMP')) {
    if (column.hasDefault) {
      return `DEFAULT`;
    }
    return `CURRENT_TIMESTAMP`;
  }
  if (dataType.includes('VARCHAR') || dataType.includes('CHAR') || dataType.includes('TEXT')) {
    return `'Test value for ${column.name}'`;
  }
  
  return `'test_value_${index + 1}'`;
};

const generateTableTestCode = (objectName, schema, databaseType, currentDate, originalDDL = '') => {
  const columns = extractTableColumns(originalDDL);
  const hasColumns = columns.length > 0;
  const columnList = hasColumns ? columns.map(col => col.name).join(', ') : '*';
  
  let insertColumns = '';
  let insertValues = '';
  
  if (hasColumns) {
    const insertableColumns = columns.filter(col => {
      const nameLower = col.name.toLowerCase();
      return !col.hasDefault && 
             !nameLower.includes('id') && 
             !nameLower.includes('created_at') && 
             !nameLower.includes('updated_at') &&
             !nameLower.includes('version');
    });
    
    if (insertableColumns.length > 0) {
      const sampleColumns = insertableColumns.slice(0, 5);
      insertColumns = sampleColumns.map(col => col.name).join(', ');
      insertValues = sampleColumns.map((col, idx) => generateTestValue(col, idx)).join(', ');
    } else {
      const sampleColumns = columns.slice(0, 3);
      insertColumns = sampleColumns.map(col => col.name).join(', ');
      insertValues = sampleColumns.map((col, idx) => generateTestValue(col, idx)).join(', ');
    }
  }
  
  let updateColumn = '';
  let updateValue = '';
  
  if (hasColumns) {
    const updateableColumn = columns.find(col => {
      const nameLower = col.name.toLowerCase();
      return !nameLower.includes('id') && 
             !nameLower.includes('created_at') && 
             !nameLower.includes('updated_at') &&
             !nameLower.includes('version');
    });
    
    if (updateableColumn) {
      updateColumn = updateableColumn.name;
      updateValue = generateTestValue(updateableColumn, 0);
    } else if (columns.length > 0) {
      updateColumn = columns[0].name;
      updateValue = generateTestValue(columns[0], 0);
    }
  }
  
  let whereColumn = 'id';
  let whereValue = "'some_id'";
  
  if (hasColumns) {
    const idColumn = columns.find(col => col.name.toLowerCase() === 'id');
    if (idColumn) {
      whereColumn = idColumn.name;
      whereValue = "'TEST_ID_001'";
    } else {
      const firstColumn = columns[0];
      if (firstColumn) {
        whereColumn = firstColumn.name;
        whereValue = generateTestValue(firstColumn, 0);
      }
    }
  }
  
  if (databaseType === 'oracle') {
    return `-- Test queries for table ${schema}.${objectName}

-- 1. View all records (first 10 rows)
SELECT ${columnList}
FROM ${schema}.${objectName}
WHERE ROWNUM <= 10;

-- 2. Get record count
SELECT COUNT(*) AS total_records 
FROM ${schema}.${objectName};

-- 3. View table structure
DESC ${schema}.${objectName};

-- 4. Insert test record (modify as needed)
-- INSERT INTO ${schema}.${objectName} (${insertColumns}) 
-- VALUES (${insertValues});

-- 5. Update test record
-- UPDATE ${schema}.${objectName} 
-- SET ${updateColumn} = ${updateValue}
-- WHERE ${whereColumn} = ${whereValue};

-- 6. Delete test record
-- DELETE FROM ${schema}.${objectName} 
-- WHERE ${whereColumn} = ${whereValue};

-- 7. Sample queries based on actual table structure:
${hasColumns ? `-- Available columns: ${columns.map(c => c.name).join(', ')}` : '-- Use DESCRIBE to see table structure'}

-- Example: Select specific columns
-- SELECT ${hasColumns ? columns.slice(0, 3).map(c => c.name).join(', ') : 'column1, column2'} 
-- FROM ${schema}.${objectName};

-- Example: Filter by condition
-- SELECT * FROM ${schema}.${objectName} 
-- WHERE ${hasColumns ? columns.find(c => c.name.toLowerCase().includes('status'))?.name || columns[0]?.name || 'column_name' : 'column_name'} = 'VALUE';`;
  } else {
    return `-- Test queries for table ${schema}.${objectName}

-- 1. View all records (first 10 rows)
SELECT ${columnList}
FROM ${schema}.${objectName}
LIMIT 10;

-- 2. Get record count
SELECT COUNT(*) AS total_records 
FROM ${schema}.${objectName};

-- 3. View table structure
\\d ${schema}.${objectName}

-- 4. Insert test record (modify as needed)
-- INSERT INTO ${schema}.${objectName} (${insertColumns}) 
-- VALUES (${insertValues});

-- 5. Update test record
-- UPDATE ${schema}.${objectName} 
-- SET ${updateColumn} = ${updateValue}
-- WHERE ${whereColumn} = ${whereValue};

-- 6. Delete test record
-- DELETE FROM ${schema}.${objectName} 
-- WHERE ${whereColumn} = ${whereValue};

-- 7. Sample queries based on actual table structure:
${hasColumns ? `-- Available columns: ${columns.map(c => c.name).join(', ')}` : '-- Use \\d to see table structure'}

-- Example: Select specific columns
-- SELECT ${hasColumns ? columns.slice(0, 3).map(c => c.name).join(', ') : 'column1, column2'} 
-- FROM ${schema}.${objectName};

-- Example: Filter by condition
-- SELECT * FROM ${schema}.${objectName} 
-- WHERE ${hasColumns ? columns.find(c => c.name.toLowerCase().includes('status'))?.name || columns[0]?.name || 'column_name' : 'column_name'} = 'VALUE';

-- Example: Order by date
-- SELECT * FROM ${schema}.${objectName} 
-- ORDER BY ${hasColumns ? columns.find(c => c.name.toLowerCase().includes('created_at'))?.name || columns.find(c => c.name.toLowerCase().includes('date'))?.name || 'created_at' : 'created_at'} DESC
-- LIMIT 10;`;
  }
};

const generateViewTestCode = (objectName, schema, databaseType, currentDate) => {
  return `-- Test queries for view ${schema}.${objectName}

-- View all data
SELECT * FROM ${schema}.${objectName} WHERE ROWNUM <= 10;

-- Get record count
SELECT COUNT(*) AS total_records FROM ${schema}.${objectName};

-- View view definition
${databaseType === 'oracle' ? `SELECT TEXT FROM ALL_VIEWS WHERE VIEW_NAME = '${objectName}' AND OWNER = '${schema}';` : 
databaseType === 'postgresql' ? `\\d+ ${schema}.${objectName}` : 
`SHOW CREATE VIEW ${schema}.${objectName};`}`;
};

const generatePackageTestCode = (objectName, schema, databaseType, currentDate) => {
  if (databaseType === 'oracle') {
    return `-- Test package ${schema}.${objectName}

-- Initialize package
BEGIN
    ${schema}.${objectName}.init;
END;
/

-- Call package function
DECLARE
    v_result VARCHAR2(4000);
BEGIN
    v_result := ${schema}.${objectName}.get_data(1);
    DBMS_OUTPUT.PUT_LINE('Result: ' || v_result);
END;
/

-- View package specification
SELECT TEXT FROM ALL_SOURCE WHERE NAME = '${objectName}' AND TYPE = 'PACKAGE' ORDER BY LINE;`;
  } else {
    return `-- Package test for ${objectName}
-- Note: ${databaseType} doesn't support packages directly
-- Use schemas to organize related objects: ${schema}.${objectName}`;
  }
};

const generateTriggerTestCode = (objectName, schema, databaseType, currentDate) => {
  return `-- Test trigger ${schema}.${objectName}

-- View trigger information
${databaseType === 'oracle' ? `SELECT TRIGGER_NAME, TRIGGER_TYPE, TRIGGERING_EVENT, STATUS 
FROM ALL_TRIGGERS WHERE TRIGGER_NAME = '${objectName}' AND OWNER = '${schema}';` : 
databaseType === 'postgresql' ? `\\d ${objectName}` : 
`SHOW TRIGGERS FROM ${schema};`}

-- To test the trigger, perform an operation on the associated table
-- For example, if the trigger is on UPDATE:
-- UPDATE associated_table SET column = 'test' WHERE id = 1;

-- Check trigger execution
-- SELECT * FROM audit_table WHERE operation = 'UPDATE';`;
};

const generateGenericTestCode = (objectName, schema, databaseType, currentDate) => {
  return `-- Test for ${objectName} (${schema})

-- View object information
${databaseType === 'oracle' ? `SELECT OBJECT_NAME, OBJECT_TYPE, STATUS, CREATED, LAST_DDL_TIME
FROM ALL_OBJECTS WHERE OBJECT_NAME = '${objectName}' AND OWNER = '${schema}';` : 
databaseType === 'postgresql' ? `\\d+ ${schema}.${objectName}` : 
`SELECT * FROM information_schema.tables WHERE table_name = '${objectName}';`}

-- Write your test queries here`;
};

const generateTestCode = (objectType, objectName, schema, databaseType, originalDDL = '') => {
  const dbConfig = DATABASE_CONFIGS[databaseType] || DATABASE_CONFIGS.postgresql;
  const schemaName = schema || dbConfig.defaultSchema;
  const currentDate = new Date().toISOString().replace(/[-:]/g, '').replace(/\..+/, '').replace('T', '');
  
  switch(objectType?.toUpperCase()) {
    case 'PROCEDURE':
      return generateProcedureTestCode(objectName, schemaName, databaseType, currentDate, originalDDL);
    case 'FUNCTION':
      return generateFunctionTestCode(objectName, schemaName, databaseType, currentDate);
    case 'TABLE':
      return generateTableTestCode(objectName, schemaName, databaseType, currentDate, originalDDL);
    case 'VIEW':
      return generateViewTestCode(objectName, schemaName, databaseType, currentDate);
    case 'PACKAGE':
      return generatePackageTestCode(objectName, schemaName, databaseType, currentDate);
    case 'TRIGGER':
      return generateTriggerTestCode(objectName, schemaName, databaseType, currentDate);
    default:
      return generateGenericTestCode(objectName, schemaName, databaseType, currentDate);
  }
};

const getObjectIcon = (type, size = 16) => {
  switch(type?.toUpperCase()) {
    case 'TABLE': return <Table size={size} />;
    case 'VIEW': return <View size={size} />;
    case 'FUNCTION': return <FunctionIcon size={size} />;
    case 'PROCEDURE': return <Terminal size={size} />;
    case 'PACKAGE': return <Package size={size} />;
    case 'TRIGGER': return <Zap size={size} />;
    case 'SYNONYM': return <LinkIcon size={size} />;
    default: return <Database size={size} />;
  }
};

const PreviewDDLModal = ({ 
  isOpen, 
  onClose, 
  selectedObject, 
  colors, 
  theme,
  authToken,
  databaseType = 'postgresql',
  onRefreshApis  // ← ADD THIS prop for refreshing APIs after generation
}) => {
  const [editorContent, setEditorContent] = useState('');
  const [originalContent, setOriginalContent] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isCompiling, setIsCompiling] = useState(false);
  const [compilationResult, setCompilationResult] = useState(null);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [showFindReplace, setShowFindReplace] = useState(false);
  const [findText, setFindText] = useState('');
  const [replaceText, setReplaceText] = useState('');
  const [matchCase, setMatchCase] = useState(false);
  const [searchIndex, setSearchIndex] = useState(-1);
  const [searchMatches, setSearchMatches] = useState([]);
  const [editorFontSize, setEditorFontSize] = useState(13);
  const [showLineNumbers, setShowLineNumbers] = useState(true);
  const [mounted, setMounted] = useState(false);
  const [objectInfo, setObjectInfo] = useState(null);
  const [isTestMode, setIsTestMode] = useState(false);
  const [isSearchInputFocused, setIsSearchInputFocused] = useState(false);

  // API Generation modal state
  const [showApiModal, setShowApiModal] = useState(false);
  const [showCustomQueryApiModal, setShowCustomQueryApiModal] = useState(false);
  const [customQueryForApi, setCustomQueryForApi] = useState('');
  const [extractedParamsForApi, setExtractedParamsForApi] = useState([]);
  
  // Modal resize state
  const [modalSize, setModalSize] = useState({ width: 1200, height: 700 });
  const [isResizing, setIsResizing] = useState(false);
  const [resizeDirection, setResizeDirection] = useState(null);
  const resizeStartRef = useRef({ x: 0, y: 0, width: 0, height: 0 });
  
  // Response panel resize state
  const [responseHeight, setResponseHeight] = useState(200);
  const [isDraggingResponse, setIsDraggingResponse] = useState(false);
  const dragStartYRef = useRef(0);
  const dragStartHeightRef = useRef(0);
  
  // Undo/Redo state
  const [history, setHistory] = useState([]);
  const [historyIndex, setHistoryIndex] = useState(-1);
  const [isUndoRedoAction, setIsUndoRedoAction] = useState(false);
  
  // Refs
  const lineNumbersRef = useRef(null);
  const findInputRef = useRef(null);
  const replaceInputRef = useRef(null);
  const textareaRef = useRef(null);
  const selectionStartRef = useRef(0);
  const selectionEndRef = useRef(0);
  const modalRef = useRef(null);
  const searchTimeoutRef = useRef(null);
  
  // Get database configuration
  const dbConfig = DATABASE_CONFIGS[databaseType] || DATABASE_CONFIGS.postgresql;
  const objectTemplates = OBJECT_TEMPLATES[databaseType] || OBJECT_TEMPLATES.postgresql;
  
  // Theme colors
  const themeColors = colors || {
    bg: theme === 'dark' ? 'rgb(1 14 35)' : '#f8fafc',
    text: theme === 'dark' ? '#E8ECF1' : '#1e293b',
    textSecondary: theme === 'dark' ? 'rgb(168 178 192)' : '#64748b',
    border: theme === 'dark' ? 'rgb(61 73 92)' : '#e2e8f0',
    hover: theme === 'dark' ? 'rgb(51 63 82)' : '#f1f5f9',
    primary: theme === 'dark' ? '#E8ECF1' : '#1e293b',
    primaryDark: theme === 'dark' ? 'rgb(37 99 235)' : '#2563eb',
    card: theme === 'dark' ? '#010e23' : '#ffffff',
    modalBg: theme === 'dark' ? '#010e23' : '#ffffff',
    modalBorder: theme === 'dark' ? 'rgb(61 73 92)' : '#e2e8f0',
    error: theme === 'dark' ? 'rgb(239 68 68)' : '#ef4444',
    success: theme === 'dark' ? 'rgb(16 185 129)' : '#10b981',
    warning: theme === 'dark' ? 'rgb(245 158 11)' : '#f59e0b',
    info: theme === 'dark' ? 'rgb(59 130 246)' : '#3b82f6',
    white: '#ffffff',
    codeBg: theme === 'dark' ? 'rgb(13 17 23)' : '#f1f5f9'
  };

  // UPDATED: Function to handle generating API from the current query (SUPPORTS ALL QUERY TYPES)
  const handleGenerateApiFromQuery = () => {
    const currentQuery = getSQLToExecute();
    
    if (!currentQuery || !currentQuery.trim()) {
      setCompilationResult({
        success: false,
        message: 'Cannot generate API',
        error: 'No SQL query found. Please enter a query in the editor.',
        output: ''
      });
      setTimeout(() => setCompilationResult(null), 3000);
      return;
    }
    
    const trimmedQuery = currentQuery.trim();
    const queryType = getStatementType(trimmedQuery);
    
    // Allow ALL valid SQL types - just reject UNKNOWN (same as QueryEditorModal)
    if (queryType === 'UNKNOWN') {
      setCompilationResult({
        success: false,
        message: 'Cannot generate API',
        error: 'Invalid or unsupported SQL statement. Please enter a valid SQL query (SELECT, INSERT, UPDATE, DELETE, DDL, CALL, EXECUTE, or PL/SQL block).',
        output: ''
      });
      setTimeout(() => setCompilationResult(null), 3000);
      return;
    }
    
    // Extract parameters from the query
    const params = extractQueryParameters(trimmedQuery);
    
    setCustomQueryForApi(trimmedQuery);
    setExtractedParamsForApi(params);
    setShowCustomQueryApiModal(true);
  };
  
  // Function to handle generating API from the selected database object
  const handleGenerateApiFromObject = () => {
    setShowApiModal(true);
  };
  
  // Refs
  const editorRef = useRef(null);
  
  // Helper function to stop propagation for editor-specific keys
  const stopPropagationForEditorKeys = (e) => {
    const editorKeys = ['Tab', 'Enter', 'Escape', 'ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'Home', 'End', 'PageUp', 'PageDown'];
    if (editorKeys.includes(e.key)) {
      e.stopPropagation();
    }
  };
  
  const performSearch = useCallback((shouldHighlightFirstMatch = true) => {
    if (!findText) {
      setSearchMatches([]);
      setSearchIndex(-1);
      return;
    }
    
    const content = editorContent;
    let regex;
    
    try {
      regex = new RegExp(findText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), matchCase ? 'g' : 'gi');
    } catch (e) {
      setSearchMatches([]);
      setSearchIndex(-1);
      return;
    }
    
    const matches = [];
    let match;
    
    while ((match = regex.exec(content)) !== null) {
      matches.push({
        start: match.index,
        end: match.index + match[0].length,
        text: match[0]
      });
    }
    
    setSearchMatches(matches);
    if (matches.length > 0) {
      setSearchIndex(0);
      if (shouldHighlightFirstMatch && !isSearchInputFocused) {
        highlightMatchWithoutFocus(0);
      }
    } else {
      setSearchIndex(-1);
    }
  }, [findText, editorContent, matchCase, isSearchInputFocused]);
  
  const highlightMatchWithoutFocus = (index) => {
    if (index < 0 || index >= searchMatches.length) return;
    
    const match = searchMatches[index];
    if (textareaRef.current) {
      textareaRef.current.setSelectionRange(match.start, match.end);
      
      const lines = editorContent.substring(0, match.start).split('\n');
      const lineNumber = lines.length;
      const lineHeight = editorFontSize * 1.5;
      textareaRef.current.scrollTop = (lineNumber - 3) * lineHeight;
    }
  };
  
  const highlightMatchWithFocus = (index) => {
    if (index < 0 || index >= searchMatches.length) return;
    
    const match = searchMatches[index];
    if (textareaRef.current) {
      textareaRef.current.focus();
      textareaRef.current.setSelectionRange(match.start, match.end);
      
      const lines = editorContent.substring(0, match.start).split('\n');
      const lineNumber = lines.length;
      const lineHeight = editorFontSize * 1.5;
      textareaRef.current.scrollTop = (lineNumber - 3) * lineHeight;
    }
  };
  
  const debouncedSearch = useCallback(() => {
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }
    searchTimeoutRef.current = setTimeout(() => {
      performSearch(false);
    }, 300);
  }, [performSearch]);
  
  const handleFindTextChange = (e) => {
    const newValue = e.target.value;
    setFindText(newValue);
    debouncedSearch();
  };
  
  const handleReplaceTextChange = (e) => {
    setReplaceText(e.target.value);
  };
  
  const findNext = () => {
    if (searchMatches.length === 0) return;
    const nextIndex = (searchIndex + 1) % searchMatches.length;
    setSearchIndex(nextIndex);
    highlightMatchWithFocus(nextIndex);
  };
  
  const findPrevious = () => {
    if (searchMatches.length === 0) return;
    const prevIndex = searchIndex - 1 < 0 ? searchMatches.length - 1 : searchIndex - 1;
    setSearchIndex(prevIndex);
    highlightMatchWithFocus(prevIndex);
  };
  
  const replaceCurrent = () => {
    if (searchIndex < 0 || searchIndex >= searchMatches.length) return;
    
    saveSelection();
    const match = searchMatches[searchIndex];
    const before = editorContent.substring(0, match.start);
    const after = editorContent.substring(match.end);
    const newContent = before + replaceText + after;
    
    setEditorContent(newContent);
    addToHistory(newContent);
    
    setTimeout(() => {
      setSearchMatches([]);
      setSearchIndex(-1);
      
      if (findText) {
        let regex;
        try {
          regex = new RegExp(findText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), matchCase ? 'g' : 'gi');
        } catch (e) {
          return;
        }
        
        const newMatches = [];
        let m;
        const content = newContent;
        
        while ((m = regex.exec(content)) !== null) {
          newMatches.push({
            start: m.index,
            end: m.index + m[0].length,
            text: m[0]
          });
        }
        
        setSearchMatches(newMatches);
        if (newMatches.length > 0) {
          setSearchIndex(0);
          if (!isSearchInputFocused) {
            highlightMatchWithoutFocus(0);
          }
        }
      }
      
      restoreSelection();
    }, 50);
  };

  const replaceAll = () => {
    if (!findText) return;
    
    saveSelection();
    let regex;
    try {
      regex = new RegExp(findText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), matchCase ? 'g' : 'gi');
    } catch (e) {
      return;
    }
    
    const newContent = editorContent.replace(regex, replaceText);
    setEditorContent(newContent);
    addToHistory(newContent);
    
    setSearchMatches([]);
    setSearchIndex(-1);
    
    setTimeout(() => {
      if (findText) {
        let newRegex;
        try {
          newRegex = new RegExp(findText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), matchCase ? 'g' : 'gi');
        } catch (e) {
          return;
        }
        
        const newMatches = [];
        let m;
        
        while ((m = newRegex.exec(newContent)) !== null) {
          newMatches.push({
            start: m.index,
            end: m.index + m[0].length,
            text: m[0]
          });
        }
        
        setSearchMatches(newMatches);
        if (newMatches.length > 0) {
          setSearchIndex(0);
          if (!isSearchInputFocused) {
            highlightMatchWithoutFocus(0);
          }
        }
      }
      
      restoreSelection();
    }, 50);
  };
  
  const handleResizeStart = (e, direction) => {
    e.preventDefault();
    e.stopPropagation();
    setIsResizing(true);
    setResizeDirection(direction);
    resizeStartRef.current = {
      x: e.clientX,
      y: e.clientY,
      width: modalSize.width,
      height: modalSize.height
    };
  };
  
  const handleResizeMove = useCallback((e) => {
    if (!isResizing) return;
    
    const deltaX = e.clientX - resizeStartRef.current.x;
    const deltaY = e.clientY - resizeStartRef.current.y;
    
    let newWidth = resizeStartRef.current.width;
    let newHeight = resizeStartRef.current.height;
    
    if (resizeDirection === 'right') {
      newWidth = Math.min(Math.max(resizeStartRef.current.width + deltaX, 800), window.innerWidth - 100);
    } else if (resizeDirection === 'bottom') {
      newHeight = Math.min(Math.max(resizeStartRef.current.height + deltaY, 400), window.innerHeight - 100);
    } else if (resizeDirection === 'bottom-right') {
      newWidth = Math.min(Math.max(resizeStartRef.current.width + deltaX, 800), window.innerWidth - 100);
      newHeight = Math.min(Math.max(resizeStartRef.current.height + deltaY, 400), window.innerHeight - 100);
    }
    
    setModalSize({ width: newWidth, height: newHeight });
  }, [isResizing, resizeDirection]);
  
  const handleResizeEnd = useCallback(() => {
    setIsResizing(false);
    setResizeDirection(null);
  }, []);
  
  useEffect(() => {
    if (isResizing) {
      window.addEventListener('mousemove', handleResizeMove);
      window.addEventListener('mouseup', handleResizeEnd);
      return () => {
        window.removeEventListener('mousemove', handleResizeMove);
        window.removeEventListener('mouseup', handleResizeEnd);
      };
    }
  }, [isResizing, handleResizeMove, handleResizeEnd]);
  
  const handleResponseDragStart = useCallback((e) => {
    e.preventDefault();
    setIsDraggingResponse(true);
    dragStartYRef.current = e.clientY;
    dragStartHeightRef.current = responseHeight;
  }, [responseHeight]);
  
  const handleResponseDragMove = useCallback((e) => {
    if (!isDraggingResponse) return;
    
    const deltaY = dragStartYRef.current - e.clientY;
    const newHeight = dragStartHeightRef.current + deltaY;
    const constrainedHeight = Math.min(Math.max(newHeight, 100), modalSize.height - 300);
    setResponseHeight(constrainedHeight);
  }, [isDraggingResponse, modalSize.height]);
  
  const handleResponseDragEnd = useCallback(() => {
    setIsDraggingResponse(false);
  }, []);
  
  useEffect(() => {
    if (isDraggingResponse) {
      window.addEventListener('mousemove', handleResponseDragMove);
      window.addEventListener('mouseup', handleResponseDragEnd);
      return () => {
        window.removeEventListener('mousemove', handleResponseDragMove);
        window.removeEventListener('mouseup', handleResponseDragEnd);
      };
    }
  }, [isDraggingResponse, handleResponseDragMove, handleResponseDragEnd]);
  
  useEffect(() => {
    if (editorContent && !isUndoRedoAction) {
      addToHistory(editorContent);
    }
  }, [editorContent]);
  
  useEffect(() => {
    if (originalContent && !isUndoRedoAction) {
      setHistory([originalContent]);
      setHistoryIndex(0);
    }
  }, [originalContent]);
  
  useEffect(() => {
    setMounted(true);
    return () => setMounted(false);
  }, []);
  
  useEffect(() => {
    if (isOpen && selectedObject) {
      loadObjectDDL();
    }
  }, [isOpen, selectedObject, databaseType]);
  
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape' && isFullscreen) {
        setIsFullscreen(false);
      }
    };
    window.addEventListener('keydown', handleEsc);
    return () => window.removeEventListener('keydown', handleEsc);
  }, [isFullscreen]);
  
  useEffect(() => {
    if (showLineNumbers) {
      updateLineNumbers();
    }
  }, [editorContent, showLineNumbers, editorFontSize]);
  
  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, []);
  
  const addToHistory = (content) => {
    if (isUndoRedoAction) {
      setIsUndoRedoAction(false);
      return;
    }
    
    if (history[historyIndex] === content) {
      return;
    }
    
    const newHistory = history.slice(0, historyIndex + 1);
    newHistory.push(content);
    
    if (newHistory.length > 100) {
      newHistory.shift();
    } else {
      setHistory(newHistory);
      setHistoryIndex(newHistory.length - 1);
    }
  };
  
  const handleUndo = () => {
    if (historyIndex > 0) {
      setIsUndoRedoAction(true);
      const newIndex = historyIndex - 1;
      const newContent = history[newIndex];
      
      saveSelection();
      setEditorContent(newContent);
      setHistoryIndex(newIndex);
      
      setTimeout(() => {
        restoreSelection();
        if (textareaRef.current) {
          const currentLength = newContent.length;
          const start = Math.min(selectionStartRef.current, currentLength);
          const end = Math.min(selectionEndRef.current, currentLength);
          textareaRef.current.setSelectionRange(start, end);
        }
      }, 0);
    }
  };
  
  const handleRedo = () => {
    if (historyIndex < history.length - 1) {
      setIsUndoRedoAction(true);
      const newIndex = historyIndex + 1;
      const newContent = history[newIndex];
      
      saveSelection();
      setEditorContent(newContent);
      setHistoryIndex(newIndex);
      
      setTimeout(() => {
        restoreSelection();
        if (textareaRef.current) {
          const currentLength = newContent.length;
          const start = Math.min(selectionStartRef.current, currentLength);
          const end = Math.min(selectionEndRef.current, currentLength);
          textareaRef.current.setSelectionRange(start, end);
        }
      }, 0);
    }
  };
  
  const handleContentChange = (e) => {
    const newContent = e.target.value;
    setEditorContent(newContent);
    addToHistory(newContent);
  };
  
  const saveSelection = () => {
    if (textareaRef.current) {
      selectionStartRef.current = textareaRef.current.selectionStart;
      selectionEndRef.current = textareaRef.current.selectionEnd;
    }
  };
  
  const restoreSelection = () => {
    if (textareaRef.current) {
      textareaRef.current.focus();
      const start = Math.min(selectionStartRef.current, editorContent.length);
      const end = Math.min(selectionEndRef.current, editorContent.length);
      textareaRef.current.setSelectionRange(start, end);
    }
  };
  
  const updateLineNumbers = () => {
    if (!lineNumbersRef.current) return;
    
    const lines = editorContent.split('\n');
    const lineCount = lines.length;
    
    const lineNumbersHtml = Array.from({ length: lineCount }, (_, i) => i + 1)
      .map(num => `<div style="padding: 4px 8px; font-size: ${editorFontSize}px; line-height: 1.5; color: ${themeColors.textSecondary};">${num}</div>`)
      .join('');
    
    lineNumbersRef.current.innerHTML = lineNumbersHtml;
    
    if (textareaRef.current && lineNumbersRef.current) {
      lineNumbersRef.current.scrollTop = textareaRef.current.scrollTop;
    }
  };
  
  const handleScroll = () => {
    if (lineNumbersRef.current && textareaRef.current) {
      lineNumbersRef.current.scrollTop = textareaRef.current.scrollTop;
    }
  };
  
  const handleKeyDown = (e) => {
    if (e.key === 'Tab') {
      e.preventDefault();
      saveSelection();
      
      const textarea = textareaRef.current;
      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      
      const tab = '  ';
      const newContent = editorContent.substring(0, start) + tab + editorContent.substring(end);
      
      setEditorContent(newContent);
      addToHistory(newContent);
      
      setTimeout(() => {
        if (textareaRef.current) {
          textareaRef.current.focus();
          textareaRef.current.setSelectionRange(start + tab.length, start + tab.length);
        }
      }, 0);
    }
  };
  
  const loadObjectDDL = async () => {
    if (!authToken || !selectedObject) return;
    
    setIsLoading(true);
    setObjectInfo(null);
    setIsTestMode(false);
    
    try {
      const controller = dbConfig.getObjectDDL;
      
      const response = await controller(authToken, {
        objectType: selectedObject.type,
        objectName: selectedObject.name,
        owner: selectedObject.owner,
        schema: selectedObject.schema || selectedObject.owner || dbConfig.defaultSchema
      });
      
      let ddl = '';
      
      if (databaseType === 'postgresql') {
        if (response?.data?.ddl) {
          ddl = response.data.ddl;
        } else if (response?.ddl) {
          ddl = response.ddl;
        } else if (typeof response === 'string') {
          ddl = response;
        }
      } else if (databaseType === 'oracle') {
        if (response?.data?.ddl) {
          ddl = response.data.ddl;
        } else if (response?.ddl) {
          ddl = response.ddl;
        } else if (response?.data && typeof response.data === 'string') {
          ddl = response.data;
        }
      }
      
      if (!ddl || ddl.trim() === '') {
        const template = objectTemplates[selectedObject.type?.toUpperCase()];
        if (template) {
          ddl = template(selectedObject.name, selectedObject.owner || selectedObject.schema);
        } else {
          ddl = `-- ${selectedObject.type} definition for ${selectedObject.name}
-- Database: ${dbConfig.name}
-- Schema: ${selectedObject.owner || selectedObject.schema || dbConfig.defaultSchema}
-- Generated: ${new Date().toLocaleString()}

-- Write your SQL here
`;
        }
      }
      
      setObjectInfo({
        name: selectedObject.name,
        type: selectedObject.type,
        owner: selectedObject.owner,
        schema: selectedObject.schema || selectedObject.owner,
        databaseType: databaseType,
        databaseName: dbConfig.name,
        ddl: ddl
      });
      
      setEditorContent(ddl);
      setOriginalContent(ddl);
      setHistory([ddl]);
      setHistoryIndex(0);
      
    } catch (error) {
      console.error('Error loading DDL:', error);
      const errorContent = `-- Error loading DDL for ${selectedObject.name}
-- Database: ${dbConfig.name}
-- Error: ${error.message}

-- You can write your SQL query here:
`;
      setEditorContent(errorContent);
      setHistory([errorContent]);
      setHistoryIndex(0);
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleViewSource = () => {
    if (objectInfo?.ddl) {
      setEditorContent(objectInfo.ddl);
      addToHistory(objectInfo.ddl);
      setIsTestMode(false);
      
      setCompilationResult({
        success: true,
        message: `✓ Switched back to source code for ${selectedObject?.type}: ${selectedObject?.name}`,
        output: 'You can now edit the source code.',
        error: null
      });
      
      setTimeout(() => {
        setCompilationResult(null);
      }, 2000);
    }
  };
  
  const handleTryItOut = () => {
    if (!selectedObject) return;
    
    const testCode = generateTestCode(
      selectedObject.type,
      selectedObject.name,
      selectedObject.schema || selectedObject.owner,
      databaseType,
      objectInfo?.ddl || ''
    );
    
    saveSelection();
    setEditorContent(testCode);
    addToHistory(testCode);
    setIsTestMode(true);
    
    setCompilationResult({
      success: true,
      message: `✓ Test code generated for ${selectedObject.type}: ${selectedObject.name}`,
      output: 'You can now edit the parameters and execute the code. Click "View Source" to go back.',
      error: null
    });
    
    setTimeout(() => {
      setCompilationResult(null);
    }, 3000);
  };
  
  const getSQLToExecute = () => {
    if (textareaRef.current && 
        textareaRef.current.selectionStart !== textareaRef.current.selectionEnd) {
      
      const start = textareaRef.current.selectionStart;
      const end = textareaRef.current.selectionEnd;
      const selectedText = editorContent.substring(start, end);
      
      if (selectedText && selectedText.trim()) {
        setCompilationResult({
          success: true,
          message: `ℹ️ Executing selected text (${selectedText.split('\n').length} line(s))`,
          output: '',
          error: null
        });
        
        setTimeout(() => {
          setCompilationResult(prev => prev?.message?.includes('Executing selected') ? null : prev);
        }, 1500);
        
        return selectedText;
      }
    }
    
    return editorContent;
  };
  
  // UPDATED: handleCompile - Now supports ALL SQL operations including CALL/EXECUTE (same as QueryEditorModal)
  const handleCompile = async () => {
    if (!authToken) return;
    
    const sqlToExecute = getSQLToExecute();
    
    if (!sqlToExecute.trim()) {
      setCompilationResult({
        success: false,
        message: 'Execution failed',
        error: 'No SQL statement to execute. Please select text or ensure the editor has content.',
        output: ''
      });
      return;
    }
    
    setIsCompiling(true);
    setCompilationResult(null);
    
    try {
      const executeSQL = dbConfig.executeSQL;
      let trimmedSql = sqlToExecute.trim();
      
      const statementType = getStatementType(trimmedSql);
      const isSelectQuery = statementType === 'SELECT';
      const isCallStatement = statementType === 'CALL' || statementType === 'EXECUTE';
      const isPLSQLBlock = statementType === 'PLSQL';
      const isDML = statementType === 'INSERT' || statementType === 'UPDATE' || statementType === 'DELETE';
      const isDDL = statementType === 'DDL';
      
      let finalSql = sqlToExecute;
      
      // Oracle specific formatting
      if (isPLSQLBlock && databaseType === 'oracle' && !trimmedSql.endsWith('/') && !trimmedSql.endsWith(';')) {
        finalSql = sqlToExecute + ';\n/';
      }
      
      const response = await executeSQL(authToken, {
        sql: finalSql,
        objectType: selectedObject?.type,
        objectName: selectedObject?.name,
        owner: selectedObject?.owner,
        schema: selectedObject?.schema || selectedObject?.owner,
        databaseType: databaseType,
        readOnly: false
      });
      
      let success = false;
      let message = '';
      let output = '';
      let error = null;
      
      console.log('Full response:', response);
      
      if (response && typeof response === 'object') {
        // Handle CALL/EXECUTE statement responses (has statementType === 'CALL')
        if (response.statementType === 'CALL' || isCallStatement) {
          success = response.responseCode === 200 || response.success === true;
          message = response.message || (success ? 'Procedure/Function executed successfully' : 'Execution failed');
          
          if (response.data) {
            if (typeof response.data === 'string') {
              output = response.data;
            } else if (response.data.error || response.data.message || response.data.traceId) {
              output = JSON.stringify(response.data, null, 2);
            } else if (Array.isArray(response.data)) {
              output = response.data.length > 0 ? JSON.stringify(response.data, null, 2) : 'No data returned.';
            } else if (Object.keys(response.data).length > 0) {
              output = JSON.stringify(response.data, null, 2);
            } else {
              output = 'Statement executed successfully';
            }
          } else {
            output = 'Procedure/Function executed successfully';
          }
          error = response.error || null;
        }
        // Handle SELECT query responses with rows
        else if (response.data && response.data.rows !== undefined) {
          success = response.responseCode === 200 || response.success === true;
          message = response.message || (success ? 'Query executed successfully' : 'Execution failed');
          
          if (response.data.rows && response.data.rows.length > 0) {
            output = JSON.stringify(response.data.rows, null, 2);
          } else {
            output = 'No rows returned.';
          }
          error = null;
        }
        // Handle DML (INSERT/UPDATE/DELETE) with rowsAffected
        else if (isDML && response.data && response.data.rowsAffected !== undefined) {
          success = response.responseCode === 200 || response.success === true;
          message = response.message || (success ? `${statementType} executed successfully` : 'Execution failed');
          output = `${response.data.rowsAffected} row(s) affected.`;
          error = null;
        }
        // Handle DDL (CREATE/ALTER/DROP/TRUNCATE)
        else if (isDDL) {
          success = response.responseCode === 200 || response.success === true;
          message = response.message || (success ? 'DDL statement executed successfully' : 'DDL execution failed');
          output = 'Statement executed successfully.';
          error = null;
        }
        // Handle response with 'success' property
        else if (response.hasOwnProperty('success')) {
          success = response.success === true;
          message = response.message || (success ? 'Query executed successfully' : 'Execution failed');
          
          if (isSelectQuery && response.data && response.data.rows) {
            if (response.data.rows.length > 0) {
              output = JSON.stringify(response.data.rows, null, 2);
            } else {
              output = 'No rows returned.';
            }
          } else if (response.data) {
            if (typeof response.data === 'string') {
              output = response.data;
            } else if (response.data.output) {
              output = response.data.output;
            } else if (response.data.rowsAffected !== undefined) {
              output = `${response.data.rowsAffected} row(s) affected`;
            } else if (response.data.data) {
              if (typeof response.data.data === 'object') {
                output = JSON.stringify(response.data.data, null, 2);
              } else {
                output = String(response.data.data);
              }
            } else {
              output = JSON.stringify(response.data, null, 2);
            }
          } else if (response.output) {
            output = response.output;
          } else {
            output = success ? 'Statement executed successfully' : '';
          }
          error = response.error || null;
        }
        // Handle response with 'responseCode' property
        else if (response.hasOwnProperty('responseCode')) {
          success = response.responseCode === 200 || response.success === true;
          message = response.message || (success ? 'Query executed successfully' : 'Execution failed');
          
          if (response.data) {
            if (typeof response.data === 'string') {
              output = response.data;
            } else if (response.data.rows && response.data.rows.length > 0) {
              output = JSON.stringify(response.data.rows, null, 2);
            } else if (response.data.rows && response.data.rows.length === 0) {
              output = 'No rows returned.';
            } else if (response.data.output) {
              output = response.data.output;
            } else if (response.data.rowsAffected !== undefined) {
              output = `${response.data.rowsAffected} row(s) affected`;
            } else if (response.data.data) {
              if (typeof response.data.data === 'object') {
                output = JSON.stringify(response.data.data, null, 2);
              } else {
                output = String(response.data.data);
              }
            } else {
              output = JSON.stringify(response.data, null, 2);
            }
          } else {
            output = message;
          }
          error = response.error || null;
        }
        // Handle response with 'data' property directly
        else if (response.hasOwnProperty('data')) {
          success = true;
          message = 'Query executed successfully';
          
          if (response.data.rows && response.data.rows.length > 0) {
            output = JSON.stringify(response.data.rows, null, 2);
          } else if (response.data.rows && response.data.rows.length === 0) {
            output = 'No rows returned.';
          } else if (response.data.output) {
            output = response.data.output;
          } else if (response.data.rowsAffected !== undefined) {
            output = `${response.data.rowsAffected} row(s) affected`;
          } else if (response.data.data) {
            output = JSON.stringify(response.data.data, null, 2);
          } else {
            output = JSON.stringify(response.data, null, 2);
          }
          error = null;
        }
        // Default case - treat as successful response
        else {
          success = true;
          message = 'Statement executed successfully';
          
          if (typeof response === 'string') {
            output = response;
          } else if (response.rows && response.rows.length > 0) {
            output = JSON.stringify(response.rows, null, 2);
          } else {
            output = JSON.stringify(response, null, 2);
          }
          error = null;
        }
      } 
      // Handle string response
      else if (typeof response === 'string') {
        success = true;
        message = 'Statement executed successfully';
        output = response;
        error = null;
      } 
      // Handle other response types
      else {
        success = true;
        message = 'Statement executed successfully';
        output = String(response);
        error = null;
      }
      
      // Ensure output is never empty for successful operations
      if (success && !output) {
        output = 'Statement executed successfully';
      }
      
      setCompilationResult({
        success: success,
        message: message,
        output: output,
        error: error
      });
      
    } catch (error) {
      console.error('Execution error:', error);
      
      let errorMessage = error.message || String(error);
      let errorDetail = null;
      
      if (error.response && error.response.data) {
        if (error.response.data.message) {
          errorMessage = error.response.data.message;
          errorDetail = error.response.data.error || error.response.data.detail;
        } else if (error.response.data.error) {
          errorMessage = error.response.data.error;
        }
      }
      
      setCompilationResult({
        success: false,
        message: 'Execution failed',
        error: errorMessage,
        output: errorDetail || ''
      });
    } finally {
      setIsCompiling(false);
    }
  };
  
  const handleSave = async () => {
    if (!authToken || !selectedObject) {
      handleCopy();
      return;
    }
    
    setIsCompiling(true);
    
    try {
      let updateObjectDDL;
      try {
        if (databaseType === 'postgresql') {
          const { updateObjectDDL: updateFn } = await import('../../controllers/PostgreSQLSchemaController.js');
          updateObjectDDL = updateFn;
        } else if (databaseType === 'oracle') {
          const { updateObjectDDL: updateFn } = await import('../../controllers/OracleSchemaController.js');
          updateObjectDDL = updateFn;
        }
      } catch (err) {
        console.warn('updateObjectDDL not available for', databaseType);
      }
      
      if (updateObjectDDL) {
        const response = await updateObjectDDL(authToken, {
          objectType: selectedObject.type,
          objectName: selectedObject.name,
          owner: selectedObject.owner,
          schema: selectedObject.schema || selectedObject.owner,
          ddl: editorContent,
          databaseType: databaseType
        });
        
        setCompilationResult({
          success: response.success || false,
          message: response.message || (response.success ? 'Object saved successfully' : 'Save failed'),
          output: response.output || '',
          error: response.error || null
        });
        
        if (response.success) {
          setOriginalContent(editorContent);
          if (objectInfo) {
            setObjectInfo({ ...objectInfo, ddl: editorContent });
          }
        }
      } else {
        setTimeout(() => {
          setCompilationResult({
            success: true,
            message: `Changes saved to ${dbConfig.name} (simulation)`,
            output: 'Object definition updated successfully',
            error: null
          });
          setOriginalContent(editorContent);
          if (objectInfo) {
            setObjectInfo({ ...objectInfo, ddl: editorContent });
          }
        }, 500);
      }
      
    } catch (error) {
      setCompilationResult({
        success: false,
        message: 'Save failed',
        error: error.message,
        output: ''
      });
    } finally {
      setIsCompiling(false);
    }
  };
  
  const handleCopy = () => {
    navigator.clipboard.writeText(editorContent);
    setCompilationResult({
      success: true,
      message: 'Copied to clipboard',
      output: '',
      error: null
    });
    setTimeout(() => setCompilationResult(null), 2000);
  };
  
  const handleDownload = () => {
    const extension = databaseType === 'oracle' ? 'sql' : 'sql';
    const filename = `${selectedObject?.name || 'query'}.${extension}`;
    
    const blob = new Blob([editorContent], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };
  
  const handleUpload = (event) => {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        saveSelection();
        const newContent = e.target.result;
        setEditorContent(newContent);
        addToHistory(newContent);
        setTimeout(() => restoreSelection(), 0);
      };
      reader.readAsText(file);
    }
  };
  
  const formatSQL = () => {
    saveSelection();
    let formatted = editorContent;
    
    formatted = formatted
      .replace(/\bSELECT\b/gi, '\nSELECT')
      .replace(/\bFROM\b/gi, '\nFROM')
      .replace(/\bWHERE\b/gi, '\nWHERE')
      .replace(/\bJOIN\b/gi, '\n  JOIN')
      .replace(/\bLEFT JOIN\b/gi, '\n  LEFT JOIN')
      .replace(/\bRIGHT JOIN\b/gi, '\n  RIGHT JOIN')
      .replace(/\bINNER JOIN\b/gi, '\n  INNER JOIN')
      .replace(/\bORDER BY\b/gi, '\nORDER BY')
      .replace(/\bGROUP BY\b/gi, '\nGROUP BY')
      .replace(/\bHAVING\b/gi, '\nHAVING')
      .replace(/\bAND\b/gi, '\n  AND')
      .replace(/\bOR\b/gi, '\n  OR')
      .replace(/^\s+/, '')
      .trim();
    
    if (databaseType === 'oracle') {
      formatted = formatted
        .replace(/\bFROM DUAL\b/gi, '\nFROM DUAL')
        .replace(/\bCONNECT BY\b/gi, '\nCONNECT BY')
        .replace(/\bSTART WITH\b/gi, '\nSTART WITH');
    }
    
    setEditorContent(formatted);
    addToHistory(formatted);
    setTimeout(() => restoreSelection(), 0);
  };
  
  // UPDATED: Check if current query is a valid SQL statement (any type except UNKNOWN)
  const trimmedQuery = editorContent.trim();
  // const queryRegex = /^(SELECT|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP|TRUNCATE|CALL|EXEC(UTE)?|BEGIN|DECLARE)\b/i;
  const queryRegex = /^(SELECT|INSERT|UPDATE|DELETE|CALL|EXEC(UTE)?)\b/i;
  const isQuery = queryRegex.test(trimmedQuery);
  
  if (typeof window === 'undefined') return null;
  if (!mounted) return null;
  if (!isOpen) return null;
  
  const modalClasses = isFullscreen 
    ? 'fixed inset-0 z-[1100] flex items-center justify-center'
    : 'fixed inset-0 z-[1100] flex items-center justify-center p-4';
  
  const containerClasses = isFullscreen
    ? 'w-full h-full flex flex-col'
    : 'flex flex-col rounded-lg shadow-xl';
  
  const editorContainerStyle = compilationResult ? {
    height: `calc(100% - ${responseHeight}px)`
  } : {
    height: '100%'
  };
  
  return ReactDOM.createPortal(
    <div className={modalClasses} style={{ backgroundColor: 'rgba(0, 0, 0, 0.7)', backdropFilter: 'blur(8px)' }}>
      <div 
        ref={modalRef}
        className={containerClasses}
        style={{ 
          backgroundColor: themeColors.bg,
          border: `1px solid ${themeColors.modalBorder || themeColors.border}`,
          borderRadius: isFullscreen ? 0 : '0.75rem',
          width: isFullscreen ? '100%' : `${modalSize.width}px`,
          height: isFullscreen ? '100%' : `${modalSize.height}px`,
          position: 'relative',
          resize: 'both',
          overflow: 'hidden'
        }}
      >
        {/* Resize Handles */}
        {!isFullscreen && (
          <>
            <div
              className="absolute right-0 top-0 w-1 h-full cursor-ew-resize hover:bg-blue-500 transition-colors"
              style={{ backgroundColor: isResizing && resizeDirection === 'right' ? themeColors.info : 'transparent' }}
              onMouseDown={(e) => handleResizeStart(e, 'right')}
            />
            <div
              className="absolute bottom-0 left-0 w-full h-1 cursor-ns-resize hover:bg-blue-500 transition-colors"
              style={{ backgroundColor: isResizing && resizeDirection === 'bottom' ? themeColors.info : 'transparent' }}
              onMouseDown={(e) => handleResizeStart(e, 'bottom')}
            />
            <div
              className="absolute bottom-0 right-0 w-4 h-4 cursor-nw-resize hover:bg-blue-500 transition-colors rounded-bl"
              style={{ backgroundColor: isResizing && resizeDirection === 'bottom-right' ? themeColors.info : 'transparent' }}
              onMouseDown={(e) => handleResizeStart(e, 'bottom-right')}
            >
              <Maximize size={12} className="absolute bottom-1 right-1" style={{ color: themeColors.textSecondary }} />
            </div>
          </>
        )}
        
        {/* Header */}
        <div 
          className="flex items-center justify-between px-6 py-4 border-b shrink-0"
          style={{ borderColor: themeColors.border, backgroundColor: themeColors.card }}
        >
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.primary + '20' }}>
              {getObjectIcon(selectedObject?.type, 20)}
            </div>
            <div>
              <h2 className="text-lg font-bold" style={{ color: themeColors.text }}>
                {dbConfig.name} Query Editor
              </h2>
              {selectedObject && (
                <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                  {selectedObject.owner || selectedObject.schema || dbConfig.defaultSchema}.{selectedObject.name} 
                  <span className="mx-1">•</span>
                  <span className="px-1.5 py-0.5 rounded text-xs" style={{ 
                    backgroundColor: themeColors.info + '20',
                    color: themeColors.info
                  }}>
                    {selectedObject.type}
                  </span>
                  {isTestMode && (
                    <span className="ml-2 px-1.5 py-0.5 rounded text-xs" style={{ 
                      backgroundColor: themeColors.warning + '20',
                      color: themeColors.warning
                    }}>
                      TEST MODE
                    </span>
                  )}
                </p>
              )}
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setShowLineNumbers(!showLineNumbers)}
              className="p-2 rounded-lg hover-lift transition-colors"
              style={{ backgroundColor: themeColors.hover }}
              title="Toggle line numbers"
            >
              <Hash size={16} style={{ color: themeColors.textSecondary }} />
            </button>
            <button
              onClick={() => setShowFindReplace(!showFindReplace)}
              className="p-2 rounded-lg hover-lift transition-colors"
              style={{ backgroundColor: themeColors.hover }}
              title="Find and Replace"
            >
              <Search size={16} style={{ color: themeColors.textSecondary }} />
            </button>
            <button
              onClick={() => setEditorFontSize(prev => Math.max(8, prev - 1))}
              className="p-2 rounded-lg hover-lift transition-colors"
              style={{ backgroundColor: themeColors.hover }}
              title="Decrease font size"
            >
              <span style={{ fontSize: 12, color: themeColors.textSecondary }}>A-</span>
            </button>
            <button
              onClick={() => setEditorFontSize(prev => Math.min(24, prev + 1))}
              className="p-2 rounded-lg hover-lift transition-colors"
              style={{ backgroundColor: themeColors.hover }}
              title="Increase font size"
            >
              <span style={{ fontSize: 16, color: themeColors.textSecondary }}>A+</span>
            </button>
            <button
              onClick={() => setIsFullscreen(!isFullscreen)}
              className="p-2 rounded-lg hover-lift transition-colors"
              style={{ backgroundColor: themeColors.hover }}
              title={isFullscreen ? "Exit fullscreen" : "Fullscreen"}
            >
              {isFullscreen ? <Minimize2 size={16} style={{ color: themeColors.textSecondary }} /> : <Maximize2 size={16} style={{ color: themeColors.textSecondary }} />}
            </button>
            <button
              onClick={onClose}
              className="p-2 rounded-lg hover-lift transition-colors"
              style={{ backgroundColor: themeColors.hover }}
            >
              <X size={20} style={{ color: themeColors.textSecondary }} />
            </button>
          </div>
        </div>
        
        {/* Find/Replace Panel */}
        {showFindReplace && (
          <div 
            className="p-4 border-b"
            style={{ borderColor: themeColors.border, backgroundColor: themeColors.hover }}
          >
            <div className="flex flex-col gap-3">
              <div className="flex gap-2">
                <div className="flex-1 flex gap-2">
                  <input
                    ref={findInputRef}
                    type="text"
                    placeholder="Find..."
                    value={findText}
                    onChange={handleFindTextChange}
                    onKeyDown={stopPropagationForEditorKeys}
                    onFocus={() => setIsSearchInputFocused(true)}
                    onBlur={() => setIsSearchInputFocused(false)}
                    className="flex-1 px-3 py-2 rounded-lg text-sm focus:outline-none hover-lift"
                    style={{ 
                      backgroundColor: themeColors.card,
                      border: `1px solid ${themeColors.border}`,
                      color: themeColors.text
                    }}
                  />
                  <input
                    ref={replaceInputRef}
                    type="text"
                    placeholder="Replace with..."
                    value={replaceText}
                    onChange={handleReplaceTextChange}
                    onKeyDown={stopPropagationForEditorKeys}
                    onFocus={() => setIsSearchInputFocused(true)}
                    onBlur={() => setIsSearchInputFocused(false)}
                    className="flex-1 px-3 py-2 rounded-lg text-sm focus:outline-none hover-lift"
                    style={{ 
                      backgroundColor: themeColors.card,
                      border: `1px solid ${themeColors.border}`,
                      color: themeColors.text
                    }}
                  />
                </div>
                <div className="flex gap-1">
                  <button
                    onClick={findPrevious}
                    disabled={!findText || searchMatches.length === 0}
                    className="px-3 py-2 rounded-lg text-sm hover-lift transition-colors disabled:opacity-50"
                    style={{ backgroundColor: themeColors.border, color: themeColors.text }}
                    title="Previous match"
                  >
                    ↑
                  </button>
                  <button
                    onClick={findNext}
                    disabled={!findText || searchMatches.length === 0}
                    className="px-3 py-2 rounded-lg text-sm hover-lift transition-colors disabled:opacity-50"
                    style={{ backgroundColor: themeColors.border, color: themeColors.text }}
                    title="Next match"
                  >
                    ↓
                  </button>
                  <button
                    onClick={replaceCurrent}
                    disabled={!findText || searchMatches.length === 0}
                    className="px-3 py-2 rounded-lg text-sm hover-lift transition-colors disabled:opacity-50"
                    style={{ backgroundColor: themeColors.border, color: themeColors.text }}
                  >
                    Replace
                  </button>
                  <button
                    onClick={replaceAll}
                    disabled={!findText}
                    className="px-3 py-2 rounded-lg text-sm hover-lift transition-colors disabled:opacity-50"
                    style={{ backgroundColor: themeColors.border, color: themeColors.text }}
                  >
                    Replace All
                  </button>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <label className="flex items-center gap-2 text-xs" style={{ color: themeColors.textSecondary }}>
                  <input
                    type="checkbox"
                    checked={matchCase}
                    onChange={(e) => setMatchCase(e.target.checked)}
                    className="rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  Match case
                </label>
                {searchMatches.length > 0 && (
                  <span className="text-xs" style={{ color: themeColors.textSecondary }}>
                    {searchIndex + 1} of {searchMatches.length} matches
                  </span>
                )}
              </div>
            </div>
          </div>
        )}
        
        {/* Try It Out Button Bar */}
        <div 
          className="flex items-center justify-between px-6 py-3 border-b overflow-x-auto shrink-0"
          style={{ borderColor: themeColors.border, backgroundColor: themeColors.hover }}
        >
          <div className="flex items-center gap-2">
            {!isTestMode && (
              <>
                <Beaker size={16} style={{ color: themeColors.info }} />
                <span className="text-xs font-medium" style={{ color: themeColors.textSecondary }}>Try It Out:</span>
                <button
                  onClick={handleTryItOut}
                  className="px-4 py-1.5 rounded-lg text-sm font-medium hover-lift transition-colors flex items-center gap-2"
                  style={{ 
                    backgroundColor: themeColors.info + '20',
                    color: themeColors.info,
                    border: `1px solid ${themeColors.info}40`
                  }}
                >
                  <Zap size={14} />
                  Generate Test Code
                </button>
              </>
            )}
            {isTestMode && (
              <button
                onClick={handleViewSource}
                className="px-4 py-1.5 rounded-lg text-sm font-medium hover-lift transition-colors flex items-center gap-2"
                style={{ 
                  backgroundColor: themeColors.primary + '20',
                  color: themeColors.primary,
                  border: `1px solid ${themeColors.primary}40`
                }}
              >
                <ArrowLeft size={14} />
                View Source
              </button>
            )}
            <span className="text-xs ml-2" style={{ color: themeColors.textSecondary }}>
              {isTestMode 
                ? `Currently viewing test code for this ${selectedObject?.type?.toLowerCase()}`
                : `Generate a ready-to-run test block for this ${selectedObject?.type?.toLowerCase()}`
              }
            </span>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={formatSQL}
              className="px-3 py-1.5 rounded-lg text-xs hover-lift transition-colors flex items-center gap-1"
              style={{ backgroundColor: themeColors.card, color: themeColors.text, border: `1px solid ${themeColors.border}` }}
            >
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M4 4h16v2H4V4zm0 4h16v2H4V8zm0 4h10v2H4v-2zm12 0h4v2h-4v-2zm-12 4h16v2H4v-2z" fill="currentColor"/>
              </svg>
              Format
            </button>
            <button
              onClick={handleCopy}
              className="px-3 py-1.5 rounded-lg text-xs hover-lift transition-colors flex items-center gap-1"
              style={{ backgroundColor: themeColors.card, color: themeColors.text, border: `1px solid ${themeColors.border}` }}
            >
              <Copy size={12} />
              Copy
            </button>
          </div>
        </div>
        
        {/* Editor Area */}
        <div className="flex-1 flex flex-col overflow-hidden relative">
          <div style={editorContainerStyle} className="overflow-hidden">
            {isLoading ? (
              <div className="h-full flex items-center justify-center">
                <div className="text-center">
                  <Loader className="animate-spin mx-auto mb-4" size={40} style={{ color: themeColors.primary }} />
                  <p className="text-sm" style={{ color: themeColors.text }}>Loading {selectedObject?.type} DDL...</p>
                  <p className="text-xs mt-2" style={{ color: themeColors.textSecondary }}>
                    Fetching from {dbConfig.name} database
                  </p>
                </div>
              </div>
            ) : (
              <div className="flex h-full">
                {showLineNumbers && (
                  <div
                    ref={lineNumbersRef}
                    className="overflow-hidden select-none"
                    style={{
                      width: '60px',
                      backgroundColor: themeColors.codeBg,
                      borderRight: `1px solid ${themeColors.border}`,
                      fontFamily: 'monospace',
                      fontSize: editorFontSize,
                      lineHeight: 1.5,
                      overflowY: 'auto'
                    }}
                  />
                )}
                
                <textarea
                  ref={textareaRef}
                  value={editorContent}
                  onChange={handleContentChange}
                  onScroll={handleScroll}
                  onKeyDown={handleKeyDown}
                  onBlur={saveSelection}
                  onFocus={() => {
                    if (textareaRef.current && (selectionStartRef.current !== 0 || selectionEndRef.current !== 0)) {
                      textareaRef.current.setSelectionRange(selectionStartRef.current, selectionEndRef.current);
                    }
                  }}
                  className="flex-1 p-4 outline-none resize-none"
                  style={{
                    backgroundColor: themeColors.codeBg,
                    color: themeColors.text,
                    fontFamily: 'monospace',
                    fontSize: editorFontSize,
                    lineHeight: 1.5,
                    border: 'none'
                  }}
                  spellCheck={false}
                />
              </div>
            )}
          </div>
          
          {/* Resizable Response Panel */}
          {compilationResult && (
            <>
              <div
                className="flex items-center justify-center cursor-ns-resize hover:bg-opacity-20 transition-colors"
                style={{
                  height: '8px',
                  backgroundColor: themeColors.border,
                  cursor: 'ns-resize',
                  position: 'relative'
                }}
                onMouseDown={handleResponseDragStart}
              >
                <div
                  className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2"
                  style={{ color: themeColors.textSecondary }}
                >
                  <GripHorizontal size={16} />
                </div>
              </div>
              
              <div
                style={{
                  height: `${responseHeight}px`,
                  overflow: 'auto',
                  borderTop: `1px solid ${themeColors.border}`,
                  backgroundColor: compilationResult.success ? `${themeColors.success}10` : `${themeColors.error}10`
                }}
                className="shrink-0"
              >
                <div className="p-4">
                  <div className="flex items-start gap-3">
                    {compilationResult.success ? (
                      <CheckCircle size={18} style={{ color: themeColors.success }} className="mt-0.5 flex-shrink-0" />
                    ) : (
                      <AlertCircle size={18} style={{ color: themeColors.error }} className="mt-0.5 flex-shrink-0" />
                    )}
                    <div className="flex-1">
                      <div className="text-sm font-medium" style={{ color: compilationResult.success ? themeColors.success : themeColors.error }}>
                        {compilationResult.message}
                      </div>
                      {compilationResult.error && (
                        <pre className="text-xs mt-1 whitespace-pre-wrap" style={{ color: themeColors.textSecondary }}>
                          {compilationResult.error}
                        </pre>
                      )}
                      {compilationResult.output && (
                        <pre className="text-xs mt-1 whitespace-pre-wrap overflow-x-auto" style={{ color: themeColors.textSecondary }}>
                          {compilationResult.output}
                        </pre>
                      )}
                    </div>
                    <button
                      onClick={() => setCompilationResult(null)}
                      className="p-1 rounded hover-lift transition-colors"
                      style={{ backgroundColor: themeColors.hover }}
                    >
                      <X size={14} style={{ color: themeColors.textSecondary }} />
                    </button>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>
        
        {/* Action Buttons */}
        <div 
          className="flex items-center justify-between px-6 py-4 border-t shrink-0"
          style={{ borderColor: themeColors.border, backgroundColor: themeColors.card }}
        >
          {/* Left side — undo / redo / upload */}
          <div className="flex items-center gap-2">
            <button
              onClick={handleUndo}
              disabled={historyIndex <= 0}
              className="p-2 rounded-lg hover-lift transition-colors disabled:opacity-50"
              style={{ backgroundColor: themeColors.hover }}
              title="Undo"
            >
              <Undo size={16} style={{ color: themeColors.textSecondary }} />
            </button>
            <button
              onClick={handleRedo}
              disabled={historyIndex >= history.length - 1}
              className="p-2 rounded-lg hover-lift transition-colors disabled:opacity-50"
              style={{ backgroundColor: themeColors.hover }}
              title="Redo"
            >
              <Redo size={16} style={{ color: themeColors.textSecondary }} />
            </button>
            <div className="w-px h-6 mx-1" style={{ backgroundColor: themeColors.border }} />
            <label
              className="p-2 rounded-lg hover-lift transition-colors cursor-pointer"
              style={{ backgroundColor: themeColors.hover }}
              title="Upload SQL file"
            >
              <Upload size={16} style={{ color: themeColors.textSecondary }} />
              <input
                type="file"
                accept=".sql,.txt"
                onChange={handleUpload}
                className="hidden"
              />
            </label>
          </div>
        
          {/* Right side — Action Buttons */}
          <div className="flex items-center gap-2">
            {/* Generate API from Database Object button - only show if an object is selected */}
            {selectedObject && (
              <button
                onClick={handleGenerateApiFromObject}
                className="px-5 py-2 rounded-lg text-sm font-medium hover-lift transition-colors flex items-center gap-2"
                style={{
                  background: 'linear-gradient(to right, #3b82f6, #8b5cf6)',
                  color: '#ffffff'
                }}
                title="Generate API from this database object"
              >
                <Sparkles size={14} />
                Generate API from Object
              </button>
            )}
            
            {/* Generate API from Custom Query button - only show if editor contains a valid SQL query (any type except UNKNOWN) */}
            {isQuery && editorContent.trim() && (
              <button
                onClick={handleGenerateApiFromQuery}
                className="px-5 py-2 rounded-lg text-sm font-medium hover-lift transition-colors flex items-center gap-2"
                style={{
                  background: 'linear-gradient(to right, #10b981, #3b82f6)',
                  color: '#ffffff'
                }}
                title="Generate API from this SQL query"
              >
                <Wand2 size={14} />
                Generate API from Query
              </button>
            )}
            
            <button
              onClick={handleCompile}
              disabled={isCompiling}
              className="px-5 py-2 rounded-lg text-sm font-medium hover-lift transition-colors flex items-center gap-2"
              style={{ backgroundColor: themeColors.primary, color: themeColors.white }}
            >
              {isCompiling ? <Loader size={14} className="animate-spin" /> : <Play size={14} />}
              Execute
            </button>
            
            <button
              onClick={onClose}
              className="px-5 py-2 rounded-lg text-sm font-medium hover-lift transition-colors flex items-center gap-2"
              style={{ backgroundColor: themeColors.error, color: themeColors.white }}
            >
              <X size={14} />
              Close
            </button>
          </div>
        </div>
      </div>

      {/* API Generation Modal for Database Object */}
      {showApiModal && (
        <ApiGenerationModal
          isOpen={showApiModal}
          onClose={() => {
            setShowApiModal(false);
            if (onRefreshApis) {
              onRefreshApis();
            }
          }}
          selectedObject={selectedObject}
          colors={themeColors}
          theme={theme}
          authToken={authToken}
          databaseType={databaseType}
          obType={selectedObject?.type}
          isEditing={false}
          onGenerateAPI={(apiData) => {
            console.log('API generated from object:', apiData);
            setShowApiModal(false);
            if (onRefreshApis) {
              onRefreshApis();
            }
          }}
        />
      )}

      {/* For Custom Query API Generation - NOW SUPPORTS ALL QUERY TYPES */}
      {showCustomQueryApiModal && (
        <ApiGenerationModal
          isOpen={showCustomQueryApiModal}
          onClose={() => {
            setShowCustomQueryApiModal(false);
            if (onRefreshApis) {
              onRefreshApis();
            }
          }}
          colors={themeColors}
          theme={theme}
          authToken={authToken}
          databaseType={databaseType}
          isCustomQuery={true}
          customQueryText={customQueryForApi}
          extractedParams={extractedParamsForApi}
          onGenerateAPI={(apiData, response) => {
            console.log('API generated from custom query:', apiData);
            setShowCustomQueryApiModal(false);
            if (onRefreshApis) {
              onRefreshApis();
            }
          }}
        />
      )}
    </div>,
    document.body
  );
};

export default PreviewDDLModal;