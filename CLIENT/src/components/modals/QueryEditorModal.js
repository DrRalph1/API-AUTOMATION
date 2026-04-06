// QueryEditorModal.js
import React, { useState, useEffect, useRef, useCallback } from 'react';
import ReactDOM from 'react-dom';
import { 
  X, Play, Save, Copy, Download, Upload, Hash, Terminal, AlertCircle, 
  CheckCircle, Loader, Maximize2, Minimize2, Search, Replace, Undo, Redo,
  Database, Layers, Eye, EyeOff, ChevronRight, Info,
  Check, Zap, Sparkles, Folder, FolderOpen, Settings, Wrench,
  GripHorizontal, GripVertical, Maximize, History, BookOpen, Trash2,
  FileCode, Brain, Wind, Coffee, Server, Cloud, ChevronDown
} from 'lucide-react';

// IMPORTANT: Add the import for ApiGenerationModal
import ApiGenerationModal from './ApiGenerationModal.js';

// Database type configurations
const DATABASE_CONFIGS = {
  postgresql: {
    name: 'PostgreSQL',
    displayName: 'PostgreSQL',
    driver: 'org.postgresql.Driver',
    defaultPort: 5432,
    defaultSchema: 'public',
    quoteIdentifier: (name) => `"${name}"`,
    executeSQL: async (authToken, params) => {
      const { executeSQL } = await import('../../controllers/PostgreSQLSchemaController.js');
      return executeSQL(authToken, params);
    }
  },
  oracle: {
    name: 'Oracle',
    displayName: 'Oracle',
    driver: 'oracle.jdbc.OracleDriver',
    defaultPort: 1521,
    defaultSchema: 'HR',
    quoteIdentifier: (name) => `"${name.toUpperCase()}"`,
    executeSQL: async (authToken, params) => {
      const { executeSQL } = await import('../../controllers/OracleSchemaController.js');
      return executeSQL(authToken, params);
    }
  },
  all: {
    name: 'All',
    displayName: 'Multi-Database',
    driver: null,
    defaultPort: null,
    defaultSchema: null,
    quoteIdentifier: (name) => name,
    executeSQL: async (authToken, params) => {
      throw new Error('Please select a specific database type to execute queries');
    }
  }
};


// Helper function to get database display name
const getDatabaseDisplayName = (databaseType) => {
  const config = DATABASE_CONFIGS[databaseType];
  return config ? config.displayName : 'Database';
};

// SQL Templates for quick access
const SQL_TEMPLATES = {
  postgresql: [
    { name: 'SELECT All Records', sql: 'SELECT * FROM your_table_name LIMIT 100;' },
    { name: 'Count Records', sql: 'SELECT COUNT(*) as total_count FROM your_table_name;' },
    { name: 'Table Structure', sql: "SELECT \n  column_name, \n  data_type, \n  is_nullable,\n  column_default\nFROM information_schema.columns \nWHERE table_schema = 'public' \n  AND table_name = 'your_table_name'\nORDER BY ordinal_position;" },
    { name: 'List All Tables', sql: "SELECT \n  table_schema,\n  table_name,\n  table_type\nFROM information_schema.tables \nWHERE table_schema NOT IN ('information_schema', 'pg_catalog')\nORDER BY table_schema, table_name;" },
    { name: 'Current Database Info', sql: "SELECT \n  current_database() as database_name,\n  current_schema() as current_schema,\n  current_user as current_user,\n  version() as postgres_version;" },
    { name: 'Analyze Table', sql: "ANALYZE your_table_name;" },
    { name: 'Vacuum Analyze', sql: "VACUUM ANALYZE your_table_name;" }
  ],
  oracle: [
    { name: 'SELECT All Records', sql: 'SELECT * FROM your_table_name WHERE ROWNUM <= 100;' },
    { name: 'Count Records', sql: 'SELECT COUNT(*) as total_count FROM your_table_name;' },
    { name: 'Table Structure', sql: "SELECT \n  column_name,\n  data_type,\n  data_length,\n  nullable\nFROM user_tab_columns \nWHERE table_name = 'YOUR_TABLE_NAME'\nORDER BY column_id;" },
    { name: 'List All Tables', sql: "SELECT \n  table_name,\n  tablespace_name,\n  num_rows\nFROM user_tables\nORDER BY table_name;" },
    { name: 'Current Database Info', sql: "SELECT \n  sys_context('USERENV', 'DB_NAME') as database_name,\n  sys_context('USERENV', 'CURRENT_SCHEMA') as current_schema,\n  sys_context('USERENV', 'SESSION_USER') as current_user,\n  banner as oracle_version\nFROM v$version WHERE ROWNUM = 1;" },
    { name: 'Show Sessions', sql: "SELECT \n  sid,\n  serial#,\n  username,\n  status,\n  machine,\n  program\nFROM v$session\nWHERE username IS NOT NULL;" }
  ],
  mysql: [
    { name: 'SELECT All Records', sql: 'SELECT * FROM your_table_name LIMIT 100;' },
    { name: 'Count Records', sql: 'SELECT COUNT(*) as total_count FROM your_table_name;' },
    { name: 'Table Structure', sql: "DESCRIBE your_table_name;" },
    { name: 'List All Tables', sql: "SHOW TABLES;" },
    { name: 'Current Database Info', sql: "SELECT \n  DATABASE() as database_name,\n  USER() as current_user,\n  VERSION() as mysql_version;" },
    { name: 'Show Processlist', sql: "SHOW PROCESSLIST;" },
    { name: 'Show Status', sql: "SHOW STATUS;" }
  ],
  sqlserver: [
    { name: 'SELECT All Records', sql: 'SELECT TOP 100 * FROM your_table_name;' },
    { name: 'Count Records', sql: 'SELECT COUNT(*) as total_count FROM your_table_name;' },
    { name: 'Table Structure', sql: "SELECT \n  COLUMN_NAME,\n  DATA_TYPE,\n  IS_NULLABLE,\n  COLUMN_DEFAULT\nFROM INFORMATION_SCHEMA.COLUMNS \nWHERE TABLE_NAME = 'your_table_name'\nORDER BY ORDINAL_POSITION;" },
    { name: 'List All Tables', sql: "SELECT \n  TABLE_SCHEMA,\n  TABLE_NAME\nFROM INFORMATION_SCHEMA.TABLES \nWHERE TABLE_TYPE = 'BASE TABLE'\nORDER BY TABLE_SCHEMA, TABLE_NAME;" },
    { name: 'Current Database Info', sql: "SELECT \n  DB_NAME() as database_name,\n  SUSER_NAME() as current_user,\n  @@VERSION as sql_version;" }
  ]
};

// SQL Formatter function
const formatSQL = (sql) => {
  let formatted = sql;
  
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
    .replace(/\bINSERT\b/gi, '\nINSERT')
    .replace(/\bUPDATE\b/gi, '\nUPDATE')
    .replace(/\bDELETE\b/gi, '\nDELETE')
    .replace(/\bCREATE\b/gi, '\nCREATE')
    .replace(/\bALTER\b/gi, '\nALTER')
    .replace(/\bDROP\b/gi, '\nDROP')
    .replace(/^\s+/, '')
    .trim();
  
  return formatted;
};

// Helper function to get icon based on database type
const getDatabaseIcon = (databaseType, size = 20) => {
  switch(databaseType) {
    case 'postgresql':
      return <Database size={size} style={{ color: '#336791' }} />;
    case 'oracle':
      return <Database size={size} style={{ color: '#F80000' }} />;
    case 'mysql':
      return <Database size={size} style={{ color: '#00758F' }} />;
    case 'sqlserver':
      return <Database size={size} style={{ color: '#CC2927' }} />;
    case 'all':
      return <Cloud size={size} style={{ color: '#6B7280' }} />;
    default:
      return <Database size={size} style={{ color: '#3b82f6' }} />;
  }
};

// Helper function to extract parameters from SQL query
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

// Helper function to get statement type
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

const QueryEditorModal = ({ 
  isOpen, 
  onClose, 
  colors, 
  theme,
  authToken,
  databaseType = 'postgresql',
  selectedDatabaseType: propSelectedDatabaseType,  // ← ADD THIS
  onDatabaseTypeChange,  // ← ADD THIS
  initialQuery = '',
  onQueryExecute,
  onGenerateApiFromQuery,
  showGenerateApiButton = true,
  onRefreshApis
}) => {
  const [editorContent, setEditorContent] = useState(initialQuery || '');
  const [isCompiling, setIsCompiling] = useState(false);
  const [compilationResult, setCompilationResult] = useState(null);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [showFindReplace, setShowFindReplace] = useState(false);
  const [showTemplates, setShowTemplates] = useState(false);
  const [findText, setFindText] = useState('');
  const [replaceText, setReplaceText] = useState('');
  const [matchCase, setMatchCase] = useState(false);
  const [searchIndex, setSearchIndex] = useState(-1);
  const [searchMatches, setSearchMatches] = useState([]);
  const [editorFontSize, setEditorFontSize] = useState(13);
  const [showLineNumbers, setShowLineNumbers] = useState(true);
  const [mounted, setMounted] = useState(false);
  const [isSearchInputFocused, setIsSearchInputFocused] = useState(false);
  const [queryHistory, setQueryHistory] = useState([]);
  const [showHistory, setShowHistory] = useState(false);
  const [selectedTemplate, setSelectedTemplate] = useState(null);
  
  // State for API Generation Modal
  const [showApiModal, setShowApiModal] = useState(false);
  const [customQueryForApi, setCustomQueryForApi] = useState('');
  const [extractedParamsForApi, setExtractedParamsForApi] = useState([]);
  const [currentSqlForApi, setCurrentSqlForApi] = useState('');
  
  const [showDatabaseSelector, setShowDatabaseSelector] = useState(false);

  // Only show database selector when databaseType is 'all'
    const [selectedDatabaseType, setSelectedDatabaseType] = useState(
    propSelectedDatabaseType || (databaseType === 'all' ? 'postgresql' : databaseType)
  );
  
  // Update the handleDatabaseTypeChange to call the parent callback
  const handleDatabaseTypeChange = (newType) => {
    setSelectedDatabaseType(newType);
    if (onDatabaseTypeChange) {
      onDatabaseTypeChange(newType);
    }
    
    // Clear any previous compilation result
    setCompilationResult(null);
  };
  
  // Determine the actual database type to use for queries
  const activeDatabaseType = databaseType === 'all' ? selectedDatabaseType : databaseType;
  
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
  
  // Refs
  const lineNumbersRef = useRef(null);
  const findInputRef = useRef(null);
  const replaceInputRef = useRef(null);
  const textareaRef = useRef(null);
  const selectionStartRef = useRef(0);
  const selectionEndRef = useRef(0);
  const modalRef = useRef(null);
  const searchTimeoutRef = useRef(null);
  
  // Get database configuration for the active database type
  const dbConfig = DATABASE_CONFIGS[activeDatabaseType] || DATABASE_CONFIGS.postgresql;
  
  // Get templates for the active database type
  const templates = SQL_TEMPLATES[activeDatabaseType] || SQL_TEMPLATES.postgresql;
  
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


  // Add toast state
  const [toast, setToast] = useState(null);
  
  // Show toast function (inside component)
  const showToast = useCallback((message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 2000);
  }, []);


  // Add this utility function for copying text
const copyToClipboard = useCallback((text) => {
  if (!text) {
    showToast('Nothing to copy', 'warning');
    return;
  }
  
  // Modern clipboard API
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(text)
      .then(() => {
        showToast('Copied to clipboard!', 'success');
      })
      .catch((err) => {
        console.error('Clipboard write failed:', err);
        // Fallback to older method
        fallbackCopy(text);
      });
  } else {
    // Fallback for older browsers
    fallbackCopy(text);
  }
  
  function fallbackCopy(text) {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.top = '-9999px';
    textarea.style.left = '-9999px';
    document.body.appendChild(textarea);
    textarea.focus();
    textarea.select();
    
    try {
      const successful = document.execCommand('copy');
      if (successful) {
        showToast('Copied to clipboard!', 'success');
      } else {
        showToast('Failed to copy to clipboard', 'error');
      }
    } catch (err) {
      console.error('Fallback copy failed:', err);
      showToast('Failed to copy to clipboard', 'error');
    }
    
    document.body.removeChild(textarea);
  }
}, [showToast]);
  
  // Load query history from localStorage based on active database type
  useEffect(() => {
    const savedHistory = localStorage.getItem(`sql_history_${activeDatabaseType}`);
    if (savedHistory) {
      try {
        setQueryHistory(JSON.parse(savedHistory).slice(0, 50));
      } catch (e) {
        console.warn('Failed to load query history', e);
      }
    }
  }, [activeDatabaseType]);
  
  // Save query to history
  const saveToHistory = (query) => {
    if (!query || query.trim() === '') return;
    
    const newHistory = [
      { query, timestamp: new Date().toISOString(), databaseType: activeDatabaseType },
      ...queryHistory.filter(h => h.query !== query)
    ].slice(0, 100);
    
    setQueryHistory(newHistory);
    localStorage.setItem(`sql_history_${activeDatabaseType}`, JSON.stringify(newHistory));
  };
  
  // Clear history
  const clearHistory = () => {
    setQueryHistory([]);
    localStorage.removeItem(`sql_history_${activeDatabaseType}`);
    setShowHistory(false);
    setCompilationResult({
      success: true,
      message: 'Query history cleared',
      output: '',
      error: null
    });
    setTimeout(() => setCompilationResult(null), 2000);
  };
  
  // Load query from history
  const loadFromHistory = (query) => {
    setEditorContent(query);
    addToHistory(query);
    setShowHistory(false);
  };
  
  // Apply template
  const applyTemplate = (template) => {
    setEditorContent(template.sql);
    addToHistory(template.sql);
    setSelectedTemplate(template.name);
    setShowTemplates(false);
  };
  
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
    setMounted(true);
    return () => setMounted(false);
  }, []);
  
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
    
    // Calculate exact line height
    const lineHeight = editorFontSize * 1.5;
    
    // Generate line numbers using Array.from with map
    const lineNumbersHtml = Array.from({ length: lineCount }, (_, index) => {
      const lineNumber = index + 1;
      return `<div style="height: ${lineHeight}px; line-height: ${lineHeight}px; padding: 0 8px 0 0; text-align: right; font-size: ${editorFontSize}px; color: ${themeColors.textSecondary};">${lineNumber}</div>`;
    }).join('');
    
    lineNumbersRef.current.innerHTML = lineNumbersHtml;
    
    // Sync scroll position
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
  
  // NEW: Generate API from current query
  const handleGenerateApiFromQuery = () => {
  const sqlToGenerate = getSQLToExecute().trim();
  
  if (!sqlToGenerate) {
    setCompilationResult({
      success: false,
      message: 'Cannot generate API',
      error: 'No SQL statement to generate API from. Please enter a query or select text.',
      output: ''
    });
    setTimeout(() => setCompilationResult(null), 3000);
    return;
  }
  
  const trimmed = sqlToGenerate.trim();
  const queryType = getStatementType(trimmed);
  
  // Log which database type will be used
  console.log('📦 Generating API for database type:', activeDatabaseType);
  console.log('📝 Query type:', queryType);
  console.log('📝 SQL:', trimmed.substring(0, 200));
  
  // Allow ALL valid SQL types - just reject UNKNOWN
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
  const params = extractQueryParameters(trimmed);
  
  setCurrentSqlForApi(trimmed);
  setCustomQueryForApi(trimmed);
  setExtractedParamsForApi(params);
  setShowApiModal(true);
};
  
  // FIXED: handleExecute - Now supports ALL SQL operations including EXECUTE/CALL
  const handleExecute = async () => {
    if (!authToken) {
      setCompilationResult({
        success: false,
        message: 'Execution failed',
        error: 'Authentication required. Please log in.',
        output: ''
      });
      return;
    }
    
    const sqlToExecute = getSQLToExecute();
    
    if (!sqlToExecute.trim()) {
      setCompilationResult({
        success: false,
        message: 'Execution failed',
        error: 'No SQL statement to execute. Please enter a query or select text.',
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
      if (isPLSQLBlock && activeDatabaseType === 'oracle' && !trimmedSql.endsWith('/') && !trimmedSql.endsWith(';')) {
        finalSql = sqlToExecute + ';\n/';
      }
      
      const response = await executeSQL(authToken, {
        sql: finalSql,
        databaseType: activeDatabaseType,
        readOnly: false
      });
      
      let success = false;
      let message = '';
      let output = '';
      let error = null;
      
      console.log('Full response:', response);
      
      if (response && typeof response === 'object') {
        // Handle CALL/EXECUTE statement responses
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
      
      if (success && sqlToExecute.trim()) {
        saveToHistory(sqlToExecute);
        if (onQueryExecute) {
          onQueryExecute(sqlToExecute, response);
        }
      }
      
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
  
  const handleCopy = () => {
    copyToClipboard(editorContent);
  };
  
  const handleDownload = () => {
    const extension = 'sql';
    const filename = `${activeDatabaseType}_query_${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.${extension}`;
    
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
  
  const handleFormat = () => {
    saveSelection();
    const formatted = formatSQL(editorContent);
    setEditorContent(formatted);
    addToHistory(formatted);
    setTimeout(() => restoreSelection(), 0);
  };
  
  const handleClear = () => {
    if (editorContent.trim() !== '') {
      if (window.confirm('Clear the editor?')) {
        setEditorContent('');
        addToHistory('');
      }
    }
  };
  
  // Get the appropriate console title based on database type
  const getConsoleTitle = () => {
    if (databaseType === 'all') {
      return 'Multi-Database SQL Console';
    }
    const displayName = getDatabaseDisplayName(activeDatabaseType);
    return `${displayName} SQL Console`;
  };
  
  // Get the appropriate subtitle based on database type
  const getConsoleSubtitle = () => {
    if (databaseType === 'all') {
      return 'Select a database type from the dropdown to start writing queries';
    }
    const displayName = getDatabaseDisplayName(activeDatabaseType);
    return `Free SQL query editor • Execute ${displayName} statements`;
  };
  
  // Get placeholder text based on database type
  const getPlaceholderText = () => {
    if (databaseType === 'all') {
      return `-- Multi-Database SQL Console
-- Select a database type from the dropdown above to get started
-- Available: PostgreSQL, Oracle, MySQL, SQL Server

-- Example for PostgreSQL:
-- SELECT * FROM your_table_name LIMIT 10;

-- Example for Oracle:
-- SELECT * FROM your_table_name WHERE ROWNUM <= 10;
-- EXECUTE your_procedure_name(param1, param2);
-- BEGIN your_procedure_name(param1, param2); END; /

-- Once you select a database type, templates and history will be available`;
    }
    
    const displayName = getDatabaseDisplayName(activeDatabaseType);
    return `-- ${displayName} SQL Console
-- Write your SQL queries here
-- Supports: SELECT, INSERT, UPDATE, DELETE, DDL, CALL/EXECUTE, PL/SQL
-- Use Ctrl/Cmd + Enter to execute

SELECT * FROM your_table_name LIMIT 10;`;
  };
  
  // Check if current query is a SELECT statement
  const trimmed = editorContent.trim();

  // const queryRegex = /^(SELECT|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP|TRUNCATE|CALL|EXEC(UTE)?|BEGIN|DECLARE)\b/i;
  const queryRegex = /^(SELECT|INSERT|UPDATE|DELETE|CALL|EXEC(UTE)?)\b/i;

  const isQuery = queryRegex.test(trimmed);
  
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
    <>
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
                {getDatabaseIcon(activeDatabaseType, 20)}
              </div>
              <div>
                <h2 className="text-lg font-bold" style={{ color: themeColors.text }}>
                  {getConsoleTitle()}
                </h2>
                <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                  {getConsoleSubtitle()}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              {/* Only show database selector when databaseType is 'all' */}
              {databaseType === 'all' && (
                <div className="relative">
                  <button
                    onClick={() => setShowDatabaseSelector(!showDatabaseSelector)}
                    className="px-3 py-1.5 rounded-lg text-xs hover-lift transition-colors flex items-center gap-2"
                    style={{ 
                      backgroundColor: themeColors.info + '20',
                      border: `1px solid ${themeColors.info}40`,
                      color: themeColors.info
                    }}
                  >
                    {getDatabaseIcon(selectedDatabaseType, 14)}
                    <span>{getDatabaseDisplayName(selectedDatabaseType)}</span>
                    <ChevronDown size={12} />
                  </button>
                  
                  {showDatabaseSelector && (
                    <div 
                      className="absolute top-full right-0 mt-1 rounded-lg shadow-lg z-50 min-w-[160px]"
                      style={{ 
                        backgroundColor: themeColors.card,
                        border: `1px solid ${themeColors.border}`
                      }}
                    >
                      {Object.keys(DATABASE_CONFIGS).filter(dbType => dbType !== 'all').map(dbType => (
                        <button
                          key={dbType}
                          onClick={() => {
                            handleDatabaseTypeChange(dbType);
                            setShowDatabaseSelector(false);
                          }}
                          className={`w-full px-3 py-2 text-left text-sm hover-lift transition-colors flex items-center gap-2 ${
                            selectedDatabaseType === dbType ? 'bg-opacity-20' : ''
                          }`}
                          style={{ 
                            backgroundColor: selectedDatabaseType === dbType ? `${themeColors.info}20` : 'transparent',
                            color: themeColors.text
                          }}
                        >
                          {getDatabaseIcon(dbType, 14)}
                          <span>{DATABASE_CONFIGS[dbType]?.displayName || dbType.toUpperCase()}</span>
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              )}
              
              {/* Only show database badge when NOT in 'all' mode */}
              {databaseType !== 'all' && (
                <span className="text-xs px-2 py-1 rounded" style={{ 
                  backgroundColor: themeColors.info + '20',
                  color: themeColors.info
                }}>
                  {getDatabaseDisplayName(activeDatabaseType)}
                </span>
              )}
              
              <button
                onClick={() => setShowTemplates(!showTemplates)}
                className="p-2 rounded-lg hover-lift transition-colors"
                style={{ backgroundColor: themeColors.hover }}
                title="SQL Templates"
              >
                <FileCode size={16} style={{ color: themeColors.textSecondary }} />
              </button>
              <button
                onClick={() => setShowHistory(!showHistory)}
                className="p-2 rounded-lg hover-lift transition-colors"
                style={{ backgroundColor: themeColors.hover }}
                title="Query History"
              >
                <History size={16} style={{ color: themeColors.textSecondary }} />
              </button>
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
          
          {/* Templates Panel */}
          {showTemplates && (
            <div 
              className="p-4 border-b"
              style={{ borderColor: themeColors.border, backgroundColor: themeColors.hover }}
            >
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-sm font-medium" style={{ color: themeColors.text }}>
                  <FileCode size={14} className="inline mr-2" />
                  SQL Templates ({getDatabaseDisplayName(activeDatabaseType)})
                </h3>
                <button
                  onClick={() => setShowTemplates(false)}
                  className="p-1 rounded hover-lift transition-colors"
                  style={{ color: themeColors.textSecondary }}
                >
                  <X size={14} />
                </button>
              </div>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                {templates.map((template, idx) => (
                  <button
                    key={idx}
                    onClick={() => applyTemplate(template)}
                    className="px-3 py-2 rounded-lg text-left text-sm hover-lift transition-colors"
                    style={{ 
                      backgroundColor: themeColors.card,
                      border: `1px solid ${themeColors.border}`,
                      color: themeColors.text
                    }}
                  >
                    {template.name}
                  </button>
                ))}
              </div>
            </div>
          )}
          
          {/* History Panel */}
          {showHistory && (
            <div 
              className="p-4 border-b"
              style={{ borderColor: themeColors.border, backgroundColor: themeColors.hover }}
            >
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-sm font-medium" style={{ color: themeColors.text }}>
                  <History size={14} className="inline mr-2" />
                  Query History ({getDatabaseDisplayName(activeDatabaseType)})
                </h3>
                <div className="flex gap-2">
                  {queryHistory.length > 0 && (
                    <button
                      onClick={clearHistory}
                      className="px-2 py-1 rounded text-xs hover-lift transition-colors flex items-center gap-1"
                      style={{ 
                        backgroundColor: themeColors.error + '20',
                        color: themeColors.error
                      }}
                    >
                      <Trash2 size={12} />
                      Clear All
                    </button>
                  )}
                  <button
                    onClick={() => setShowHistory(false)}
                    className="p-1 rounded hover-lift transition-colors"
                    style={{ color: themeColors.textSecondary }}
                  >
                    <X size={14} />
                  </button>
                </div>
              </div>
              {queryHistory.length === 0 ? (
                <p className="text-sm text-center py-4" style={{ color: themeColors.textSecondary }}>
                  No query history yet. Execute some queries to see them here.
                </p>
              ) : (
                <div className="max-h-64 overflow-y-auto space-y-2">
                  {queryHistory.map((item, idx) => (
                    <div
                      key={idx}
                      onClick={() => loadFromHistory(item.query)}
                      className="p-2 rounded-lg cursor-pointer hover-lift transition-colors"
                      style={{ 
                        backgroundColor: themeColors.card,
                        border: `1px solid ${themeColors.border}`
                      }}
                    >
                      <pre className="text-xs truncate" style={{ color: themeColors.text }}>
                        {item.query.length > 200 ? item.query.substring(0, 200) + '...' : item.query}
                      </pre>
                      <div className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>
                        {new Date(item.timestamp).toLocaleString()}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
          
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
          
          {/* Toolbar */}
          <div 
            className="flex items-center justify-between px-6 py-3 border-b overflow-x-auto shrink-0"
            style={{ borderColor: themeColors.border, backgroundColor: themeColors.hover }}
          >
            <div className="flex items-center gap-2">
              <button
                onClick={handleFormat}
                className="px-3 py-1.5 rounded-lg text-xs hover-lift transition-colors flex items-center gap-1"
                style={{ backgroundColor: themeColors.card, color: themeColors.text, border: `1px solid ${themeColors.border}` }}
              >
                <Wind size={12} />
                Format
              </button>
              <button
                onClick={handleClear}
                className="px-3 py-1.5 rounded-lg text-xs hover-lift transition-colors flex items-center gap-1"
                style={{ backgroundColor: themeColors.card, color: themeColors.text, border: `1px solid ${themeColors.border}` }}
              >
                <Trash2 size={12} />
                Clear
              </button>
              <div className="w-px h-6 mx-1" style={{ backgroundColor: themeColors.border }} />
              <label
                className="px-3 py-1.5 rounded-lg text-xs hover-lift transition-colors cursor-pointer flex items-center gap-1"
                style={{ backgroundColor: themeColors.card, color: themeColors.text, border: `1px solid ${themeColors.border}` }}
              >
                <Upload size={12} />
                Load File
                <input
                  type="file"
                  accept=".sql,.txt"
                  onChange={handleUpload}
                  className="hidden"
                />
              </label>
              <button
                onClick={handleCopy}
                className="px-3 py-1.5 rounded-lg text-xs hover-lift transition-colors flex items-center gap-1"
                style={{ backgroundColor: themeColors.card, color: themeColors.text, border: `1px solid ${themeColors.border}` }}
              >
                <Copy size={12} />
                Copy
              </button>
              <button
                onClick={handleDownload}
                className="px-3 py-1.5 rounded-lg text-xs hover-lift transition-colors flex items-center gap-1"
                style={{ backgroundColor: themeColors.card, color: themeColors.text, border: `1px solid ${themeColors.border}` }}
              >
                <Download size={12} />
                Export
              </button>
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={handleUndo}
                disabled={historyIndex <= 0}
                className="p-1.5 rounded-lg hover-lift transition-colors disabled:opacity-50"
                style={{ backgroundColor: themeColors.card }}
                title="Undo"
              >
                <Undo size={14} style={{ color: themeColors.textSecondary }} />
              </button>
              <button
                onClick={handleRedo}
                disabled={historyIndex >= history.length - 1}
                className="p-1.5 rounded-lg hover-lift transition-colors disabled:opacity-50"
                style={{ backgroundColor: themeColors.card }}
                title="Redo"
              >
                <Redo size={14} style={{ color: themeColors.textSecondary }} />
              </button>
            </div>
          </div>
          
          {/* Editor Area */}
          <div className="flex-1 flex flex-col overflow-hidden relative">
          <div style={editorContainerStyle} className="overflow-hidden">
            <div className="flex h-full relative">
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
                    overflowY: 'auto',
                    // CRITICAL: Match textarea padding exactly
                    paddingTop: '16px',
                    paddingBottom: '16px',
                    paddingLeft: '8px',
                    paddingRight: '0'
                  }}
                />
              )}
              
              <div className="flex-1 relative overflow-hidden">
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
                  className="absolute inset-0 w-full h-full p-4 outline-none resize-none"
                  style={{
                    backgroundColor: themeColors.codeBg,
                    color: themeColors.text,
                    fontFamily: 'monospace',
                    fontSize: editorFontSize,
                    lineHeight: 1.5,
                    border: 'none',
                    margin: 0,
                    boxSizing: 'border-box'
                  }}
                  spellCheck={false}
                  placeholder={getPlaceholderText()}
                />
              </div>
            </div>
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
                      <div className="flex items-center gap-2">
                        {/* Add Copy Button - show when there's output OR error */}
                        {(compilationResult.output || compilationResult.error) && (
                          <button
                            onClick={() => {
                              const contentToCopy = compilationResult.output || compilationResult.error || compilationResult.message;
                              copyToClipboard(contentToCopy);
                            }}
                            className="p-1.5 rounded hover-lift transition-colors"
                            style={{ backgroundColor: themeColors.hover }}
                            title="Copy to clipboard"
                          >
                            <Copy size={14} style={{ color: themeColors.textSecondary }} />
                          </button>
                        )}
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
                </div>
              </>
            )}
          </div>
          
          {/* Action Buttons */}
          <div 
            className="flex items-center justify-between px-6 py-4 border-t shrink-0"
            style={{ borderColor: themeColors.border, backgroundColor: themeColors.card }}
          >
            <div className="text-xs" style={{ color: themeColors.textSecondary }}>
              <span className="font-mono">Ctrl/Cmd + Enter</span> to execute • 
              <span className="font-mono ml-2">Ctrl/Cmd + Z</span> undo • 
              <span className="font-mono ml-2">Tab</span> insert spaces
            </div>
            
            <div className="flex items-center gap-2">
              {/* Generate API button - only show for SELECT queries */}
              {isQuery && (
                <button
                  onClick={handleGenerateApiFromQuery}
                  className="px-5 py-2 rounded-lg text-sm font-medium hover-lift transition-colors flex items-center gap-2"
                  style={{ 
                    background: 'linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%)',
                    color: '#ffffff'
                  }}
                  title="Generate API from this SELECT query"
                >
                  <Sparkles size={14} />
                  Generate API
                </button>
              )}
              <button
                onClick={handleExecute}
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
      </div>

      <style>{`
        @keyframes fadeInUp {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }
        
        .animate-fade-in-up {
          animation: fadeInUp 0.2s ease-out;
        }
      `}</style>

      {/* Toast */}
      {toast && (
        <div className="fixed bottom-4 right-4 px-4 py-2 rounded text-sm font-medium z-50 animate-fade-in-up"
          style={{ 
            backgroundColor: toast.type === 'error' ? themeColors.error : 
                          toast.type === 'success' ? themeColors.success : 
                          toast.type === 'warning' ? themeColors.warning : 
                          themeColors.info,
            color: '#ffffff',
            zIndex: 1500,
          }}>
          {toast.message}
        </div>
      )}
      
      {/* API Generation Modal */}
       {showApiModal && (
          <ApiGenerationModal
            isOpen={showApiModal}
            onClose={() => {
              setShowApiModal(false);
              // Refresh the dashboard when modal closes after successful generation
              if (onRefreshApis) {
                console.log('🔄 Refreshing dashboard after API generation');
                onRefreshApis();
              }
            }}
            colors={themeColors}
            theme={theme}
            authToken={authToken}
            databaseType={activeDatabaseType}  // ← CHANGE: Use activeDatabaseType instead of the prop
            isCustomQuery={true}
            customQueryText={customQueryForApi}
            extractedParams={extractedParamsForApi}
            onGenerateAPI={(apiData, response) => {
              console.log('API generated:', apiData);
              setShowApiModal(false);
              
              // Refresh the dashboard after successful API generation
              if (onRefreshApis) {
                console.log('🔄 Refreshing dashboard after API generation');
                onRefreshApis();
              }
              
              if (onQueryExecute && currentSqlForApi) {
                onQueryExecute(currentSqlForApi, response);
              }
            }}
          />
        )}
    </>,
    document.body
  );
};

export default QueryEditorModal;