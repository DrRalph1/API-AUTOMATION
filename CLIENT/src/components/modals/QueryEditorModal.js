// QueryEditorModal.js (With Fully Resizable Modal)
import React, { useState, useEffect, useRef, useCallback } from 'react';
import ReactDOM from 'react-dom';
import { 
  X, Play, Save, Copy, Download, Upload, Hash, Terminal, AlertCircle, 
  CheckCircle, Loader, Maximize2, Minimize2, Search, Replace, Undo, Redo,
  FileText, Code, Database, Layers, Eye, EyeOff, ChevronRight, Info,
  Check, Zap, Sparkles, Folder, FolderOpen, Settings, Wrench, Table,
  View, Wrench as FunctionIcon, Package, Link as LinkIcon, GitBranch,
  GripHorizontal, GripVertical, Maximize
} from 'lucide-react';

// Database type configurations (same as before)
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
  }
};

// Object type templates (same as before)
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

// Get object type icon
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

const QueryEditorModal = ({ 
  isOpen, 
  onClose, 
  selectedObject, 
  colors, 
  theme,
  authToken,
  databaseType = 'postgresql'
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
  
  // Modal resize state
  const [modalSize, setModalSize] = useState({
    width: 1200,
    height: 700
  });
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
  
  const editorRef = useRef(null);
  const lineNumbersRef = useRef(null);
  const findInputRef = useRef(null);
  const textareaRef = useRef(null);
  const selectionStartRef = useRef(0);
  const selectionEndRef = useRef(0);
  const modalRef = useRef(null);
  
  // Get database configuration
  const dbConfig = DATABASE_CONFIGS[databaseType] || DATABASE_CONFIGS.postgresql;
  
  // Get object templates for current database
  const objectTemplates = OBJECT_TEMPLATES[databaseType] || OBJECT_TEMPLATES.postgresql;
  
  // Sample SQL snippets
  const getSqlSnippets = () => {
    const baseSnippets = {
      'SELECT': 'SELECT * FROM table_name WHERE condition;',
      'INSERT': 'INSERT INTO table_name (column1, column2) VALUES (value1, value2);',
      'UPDATE': 'UPDATE table_name SET column1 = value1 WHERE condition;',
      'DELETE': 'DELETE FROM table_name WHERE condition;',
    };
    
    const dbSpecificSnippets = {
      postgresql: {
        'CREATE TABLE': `CREATE TABLE table_name (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);`,
        'CREATE FUNCTION': `CREATE OR REPLACE FUNCTION function_name(param1 INTEGER)
RETURNS INTEGER AS $$
BEGIN
  RETURN param1 * 2;
END;
$$ LANGUAGE plpgsql;`,
        'CREATE PROCEDURE': `CREATE OR REPLACE PROCEDURE procedure_name(param1 INTEGER)
LANGUAGE plpgsql
AS $$
BEGIN
  RAISE NOTICE 'Parameter: %', param1;
END;
$$;`,
        'CREATE VIEW': 'CREATE VIEW view_name AS\nSELECT column1, column2\nFROM table_name\nWHERE condition;'
      },
      oracle: {
        'CREATE TABLE': `CREATE TABLE table_name (
  id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name VARCHAR2(255) NOT NULL,
  created_at DATE DEFAULT SYSDATE
);`,
        'CREATE FUNCTION': `CREATE OR REPLACE FUNCTION function_name(p_param1 IN NUMBER)
RETURN VARCHAR2
IS
  v_result VARCHAR2(100);
BEGIN
  v_result := 'Result: ' || TO_CHAR(p_param1);
  RETURN v_result;
END;`,
        'CREATE PROCEDURE': `CREATE OR REPLACE PROCEDURE procedure_name(p_param1 IN NUMBER)
IS
BEGIN
  DBMS_OUTPUT.PUT_LINE('Parameter: ' || TO_CHAR(p_param1));
END;`,
        'CREATE PACKAGE': `CREATE OR REPLACE PACKAGE package_name IS
  PROCEDURE init;
  FUNCTION get_data RETURN VARCHAR2;
END package_name;
/
CREATE OR REPLACE PACKAGE BODY package_name IS
  PROCEDURE init IS
  BEGIN
    DBMS_OUTPUT.PUT_LINE('Initialized');
  END init;
  
  FUNCTION get_data RETURN VARCHAR2 IS
  BEGIN
    RETURN 'Data';
  END get_data;
END package_name;
/`
      },
      mysql: {
        'CREATE TABLE': `CREATE TABLE table_name (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;`,
        'CREATE FUNCTION': `DELIMITER $$
CREATE FUNCTION function_name(p_param1 INT)
RETURNS VARCHAR(100)
DETERMINISTIC
BEGIN
  DECLARE v_result VARCHAR(100);
  SET v_result = CONCAT('Result: ', CAST(p_param1 AS CHAR));
  RETURN v_result;
END$$
DELIMITER ;`,
        'CREATE PROCEDURE': `DELIMITER $$
CREATE PROCEDURE procedure_name(IN p_param1 INT)
BEGIN
  SELECT CONCAT('Parameter: ', CAST(p_param1 AS CHAR)) AS result;
END$$
DELIMITER ;`
      },
      sqlserver: {
        'CREATE TABLE': `CREATE TABLE table_name (
  id INT IDENTITY(1,1) PRIMARY KEY,
  name NVARCHAR(255) NOT NULL,
  created_at DATETIME2 DEFAULT GETDATE()
);`,
        'CREATE FUNCTION': `CREATE FUNCTION function_name(@p_param1 INT)
RETURNS NVARCHAR(100)
AS
BEGIN
  DECLARE @v_result NVARCHAR(100);
  SET @v_result = 'Result: ' + CAST(@p_param1 AS NVARCHAR(10));
  RETURN @v_result;
END;`,
        'CREATE PROCEDURE': `CREATE PROCEDURE procedure_name
  @p_param1 INT
AS
BEGIN
  PRINT 'Parameter: ' + CAST(@p_param1 AS NVARCHAR(10));
END;`
      }
    };
    
    return { ...baseSnippets, ...(dbSpecificSnippets[databaseType] || {}) };
  };
  
  const sqlSnippets = getSqlSnippets();
  
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
  
  // Handle modal resize start
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
  
  // Handle modal resize move
  const handleResizeMove = useCallback((e) => {
    if (!isResizing) return;
    
    const deltaX = e.clientX - resizeStartRef.current.x;
    const deltaY = e.clientY - resizeStartRef.current.y;
    
    let newWidth = resizeStartRef.current.width;
    let newHeight = resizeStartRef.current.height;
    
    // Handle different resize directions
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
  
  // Handle modal resize end
  const handleResizeEnd = useCallback(() => {
    setIsResizing(false);
    setResizeDirection(null);
  }, []);
  
  // Add/remove resize event listeners
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
  
  // Handle response panel drag start
  const handleResponseDragStart = useCallback((e) => {
    e.preventDefault();
    setIsDraggingResponse(true);
    dragStartYRef.current = e.clientY;
    dragStartHeightRef.current = responseHeight;
  }, [responseHeight]);
  
  // Handle response panel drag move
  const handleResponseDragMove = useCallback((e) => {
    if (!isDraggingResponse) return;
    
    const deltaY = dragStartYRef.current - e.clientY;
    const newHeight = dragStartHeightRef.current + deltaY;
    const constrainedHeight = Math.min(Math.max(newHeight, 100), modalSize.height - 300);
    setResponseHeight(constrainedHeight);
  }, [isDraggingResponse, modalSize.height]);
  
  // Handle response panel drag end
  const handleResponseDragEnd = useCallback(() => {
    setIsDraggingResponse(false);
  }, []);
  
  // Add/remove response panel drag event listeners
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
  
  // Initialize history when content loads
  useEffect(() => {
    if (editorContent && !isUndoRedoAction) {
      addToHistory(editorContent);
    }
  }, [editorContent]);
  
  // Reset history when new object is loaded
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
  
  // Load object DDL when modal opens
  useEffect(() => {
    if (isOpen && selectedObject) {
      loadObjectDDL();
    }
  }, [isOpen, selectedObject, databaseType]);
  
  // Handle fullscreen
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape' && isFullscreen) {
        setIsFullscreen(false);
      }
    };
    window.addEventListener('keydown', handleEsc);
    return () => window.removeEventListener('keydown', handleEsc);
  }, [isFullscreen]);
  
  // Update line numbers when content changes
  useEffect(() => {
    if (showLineNumbers) {
      updateLineNumbers();
    }
  }, [editorContent, showLineNumbers, editorFontSize]);
  
  // Handle search
  useEffect(() => {
    if (findText) {
      performSearch();
    } else {
      setSearchMatches([]);
      setSearchIndex(-1);
    }
  }, [findText, editorContent, matchCase]);
  
  // Add to history
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
  
  // Undo action
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
  
  // Redo action
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
  
  // Handle content change
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
  
  // Handle Tab key press
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
  
  const performSearch = () => {
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
      highlightMatch(0);
    } else {
      setSearchIndex(-1);
    }
  };
  
  const highlightMatch = (index) => {
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
  
  const findNext = () => {
    if (searchMatches.length === 0) return;
    const nextIndex = (searchIndex + 1) % searchMatches.length;
    setSearchIndex(nextIndex);
    highlightMatch(nextIndex);
  };
  
  const findPrevious = () => {
    if (searchMatches.length === 0) return;
    const prevIndex = searchIndex - 1 < 0 ? searchMatches.length - 1 : searchIndex - 1;
    setSearchIndex(prevIndex);
    highlightMatch(prevIndex);
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
      performSearch();
      restoreSelection();
    }, 100);
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
    
    setTimeout(() => {
      performSearch();
      restoreSelection();
    }, 100);
  };
  
  const loadObjectDDL = async () => {
    if (!authToken || !selectedObject) return;
    
    setIsLoading(true);
    setObjectInfo(null);
    
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
      } else if (databaseType === 'mysql') {
        if (response?.data && response.data[0] && response.data[0]['Create Table']) {
          ddl = response.data[0]['Create Table'];
        } else if (response?.data && typeof response.data === 'string') {
          ddl = response.data;
        } else if (typeof response === 'string') {
          ddl = response;
        }
      } else if (databaseType === 'sqlserver') {
        if (response?.data?.ddl) {
          ddl = response.data.ddl;
        } else if (typeof response === 'string') {
          ddl = response;
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
        databaseName: dbConfig.name
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
  
  const handleCompile = async () => {
  if (!authToken || !editorContent.trim()) return;
  
  setIsCompiling(true);
  setCompilationResult(null);
  
  try {
    const executeSQL = dbConfig.executeSQL;
    let sqlToExecute = editorContent;
    let trimmedSql = editorContent.trim();
    
    // Check if this is a procedure/function definition (CREATE OR REPLACE)
    const isObjectDefinition = /^\s*CREATE\s+(OR\s+REPLACE\s+)?(PROCEDURE|FUNCTION|PACKAGE|TYPE|TRIGGER|VIEW|MATERIALIZED\s+VIEW)/i.test(trimmedSql);
    
    // Check if this is a PL/SQL block (starts with DECLARE or BEGIN)
    const isPLSQLBlock = /^\s*(DECLARE|BEGIN)/i.test(trimmedSql);
    
    // Check if this is a DDL statement
    const isDDL = /^\s*(CREATE|ALTER|DROP|TRUNCATE|GRANT|REVOKE)/i.test(trimmedSql);
    
    // Check if this is a procedure/function without CREATE (just the body)
    const isProcedureBody = /^\s*PROCEDURE\s+\w+/i.test(trimmedSql) && 
                            !/^\s*CREATE/i.test(trimmedSql);
    
    // For procedure/function definitions without CREATE, add CREATE OR REPLACE
    if (isProcedureBody && selectedObject?.type === 'PROCEDURE') {
      sqlToExecute = `CREATE OR REPLACE ${editorContent}`;
    }
    
    // For PL/SQL blocks, ensure they are properly terminated
    if (isPLSQLBlock && !trimmedSql.endsWith('/') && !trimmedSql.endsWith(';')) {
      sqlToExecute = editorContent + ';\n/';
    }
    
    console.log('Executing SQL type:', {
      isObjectDefinition,
      isPLSQLBlock,
      isDDL,
      isProcedureBody,
      sqlPreview: sqlToExecute.substring(0, 200)
    });
    
    const response = await executeSQL(authToken, {
      sql: sqlToExecute,
      objectType: selectedObject?.type,
      objectName: selectedObject?.name,
      owner: selectedObject?.owner,
      schema: selectedObject?.schema || selectedObject?.owner,
      databaseType: databaseType,
      readOnly: false  // Always false for compilations
    });
    
    // Process response...
    let success = false;
    let message = '';
    let output = '';
    let error = null;
    
    if (databaseType === 'oracle') {
      success = response?.responseCode === 200 || response?.success;
      message = response?.message || (success ? 'Execution completed successfully' : 'Execution failed');
      
      if (response?.data) {
        if (Array.isArray(response.data)) {
          output = JSON.stringify(response.data, null, 2);
        } else if (typeof response.data === 'object') {
          output = JSON.stringify(response.data, null, 2);
        } else {
          output = String(response.data);
        }
      }
      
      error = response?.error || null;
    }
    
    setCompilationResult({
      success: success,
      message: message,
      output: output || (success ? 'Statement executed successfully' : ''),
      error: error
    });
    
  } catch (error) {
    setCompilationResult({
      success: false,
      message: 'Execution failed',
      error: error.message || String(error),
      output: ''
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
  
  const insertSnippet = (snippet) => {
    const textarea = textareaRef.current;
    if (textarea) {
      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      
      const newContent = editorContent.substring(0, start) + snippet + editorContent.substring(end);
      setEditorContent(newContent);
      addToHistory(newContent);
      
      setTimeout(() => {
        if (textareaRef.current) {
          textareaRef.current.focus();
          const newCursorPosition = start + snippet.length;
          textareaRef.current.setSelectionRange(newCursorPosition, newCursorPosition);
          
          const lines = newContent.substring(0, newCursorPosition).split('\n');
          const lineNumber = lines.length;
          const lineHeight = editorFontSize * 1.5;
          textareaRef.current.scrollTop = Math.max(0, (lineNumber - 5) * lineHeight);
        }
      }, 0);
    } else {
      const newContent = editorContent + '\n' + snippet;
      setEditorContent(newContent);
      addToHistory(newContent);
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
    } else if (databaseType === 'mysql') {
      formatted = formatted
        .replace(/\bLIMIT\b/gi, '\nLIMIT')
        .replace(/\bOFFSET\b/gi, '\nOFFSET');
    } else if (databaseType === 'sqlserver') {
      formatted = formatted
        .replace(/\bTOP\b/gi, '\nTOP')
        .replace(/\bWITH\b/gi, '\nWITH');
    }
    
    setEditorContent(formatted);
    addToHistory(formatted);
    setTimeout(() => restoreSelection(), 0);
  };
  
  // For SSR compatibility
  if (typeof window === 'undefined') return null;
  if (!mounted) return null;
  if (!isOpen) return null;
  
  const modalClasses = isFullscreen 
    ? 'fixed inset-0 z-[1100] flex items-center justify-center'
    : 'fixed inset-0 z-[1100] flex items-center justify-center p-4';
  
  const containerClasses = isFullscreen
    ? 'w-full h-full flex flex-col'
    : 'flex flex-col rounded-lg shadow-xl';
  
  // Calculate editor height based on response panel height
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
            {/* Right edge handle */}
            <div
              className="absolute right-0 top-0 w-1 h-full cursor-ew-resize hover:bg-blue-500 transition-colors"
              style={{ backgroundColor: isResizing && resizeDirection === 'right' ? themeColors.info : 'transparent' }}
              onMouseDown={(e) => handleResizeStart(e, 'right')}
            />
            {/* Bottom edge handle */}
            <div
              className="absolute bottom-0 left-0 w-full h-1 cursor-ns-resize hover:bg-blue-500 transition-colors"
              style={{ backgroundColor: isResizing && resizeDirection === 'bottom' ? themeColors.info : 'transparent' }}
              onMouseDown={(e) => handleResizeStart(e, 'bottom')}
            />
            {/* Bottom-right corner handle */}
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
                    onChange={(e) => setFindText(e.target.value)}
                    className="flex-1 px-3 py-2 rounded-lg text-sm focus:outline-none hover-lift"
                    style={{ 
                      backgroundColor: themeColors.card,
                      border: `1px solid ${themeColors.border}`,
                      color: themeColors.text
                    }}
                  />
                  <input
                    type="text"
                    placeholder="Replace with..."
                    value={replaceText}
                    onChange={(e) => setReplaceText(e.target.value)}
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
        
        {/* SQL Snippets Bar */}
        <div 
          className="flex items-center gap-2 px-6 py-3 border-b overflow-x-auto shrink-0"
          style={{ borderColor: themeColors.border, backgroundColor: themeColors.hover }}
        >
          <span className="text-xs font-medium" style={{ color: themeColors.textSecondary }}>Snippets:</span>
          {Object.keys(sqlSnippets).map(snippet => (
            <button
              key={snippet}
              onClick={() => insertSnippet(sqlSnippets[snippet])}
              className="px-3 py-1.5 rounded-lg text-xs hover-lift transition-colors whitespace-nowrap"
              style={{ backgroundColor: themeColors.card, color: themeColors.text, border: `1px solid ${themeColors.border}` }}
            >
              {snippet}
            </button>
          ))}
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
                {/* Line Numbers */}
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
                
                {/* Text Editor */}
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
              {/* Drag Handle */}
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
              
              {/* Response Content */}
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
            <button
              onClick={formatSQL}
              className="p-2 rounded-lg hover-lift transition-colors"
              style={{ backgroundColor: themeColors.hover }}
              title="Format SQL"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M4 4h16v2H4V4zm0 4h16v2H4V8zm0 4h10v2H4v-2zm12 0h4v2h-4v-2zm-12 4h16v2H4v-2z" fill="currentColor"/>
              </svg>
            </button>
          </div>
          
          <div className="flex items-center gap-2">
            <button
              onClick={handleDownload}
              className="px-4 py-2 rounded-lg text-sm hover-lift transition-colors flex items-center gap-2"
              style={{ backgroundColor: themeColors.hover, color: themeColors.text }}
            >
              <Download size={14} />
              Download
            </button>
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
    </div>,
    document.body
  );
};

export default QueryEditorModal;