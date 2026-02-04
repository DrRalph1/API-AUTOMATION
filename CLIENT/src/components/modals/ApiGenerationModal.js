// components/modals/ApiGenerationModal.js
import React, { useState, useEffect } from 'react';
import { 
  X, Plus, Trash2, Save, Copy, Code, Globe, Lock, FileText, 
  Settings, Database, Map, FileJson, TestTube, Wrench, 
  RefreshCw, Eye, EyeOff, Download, Upload, Play, Key, 
  Shield, Hash, Calendar, Clock, Type, List, Link, ExternalLink,
  Check, AlertCircle, Star, Zap, Terminal, Package
} from 'lucide-react';

export default function ApiGenerationModal({
  isOpen,
  onClose,
  onSave,
  selectedObject = null,
  colors = {},
  theme = 'dark'
}) {
  const [activeTab, setActiveTab] = useState('definition');
  const [apiDetails, setApiDetails] = useState({
    apiName: '',
    apiCode: '',
    description: '',
    version: '1.0.0',
    status: 'DRAFT',
    httpMethod: 'GET',
    basePath: '/api/v1',
    endpointPath: '',
    tags: ['default'],
    category: 'general',
    owner: 'HR',
  });

  // Schema & Object Configuration
  const [schemaConfig, setSchemaConfig] = useState({
    schemaName: '',
    objectType: '',
    objectName: '',
    operation: 'SELECT',
    primaryKeyColumn: '',
    sequenceName: '',
    enablePagination: true,
    pageSize: 50,
    enableSorting: true,
    defaultSortColumn: '',
    defaultSortDirection: 'ASC'
  });

  const [parameters, setParameters] = useState([]);
  const [responseMappings, setResponseMappings] = useState([]);

  // Initialize parameters and mappings based on selected object
  useEffect(() => {
    if (selectedObject) {
      console.log('Initializing modal with selected object:', {
        name: selectedObject.name,
        type: selectedObject.type,
        owner: selectedObject.owner,
        columns: selectedObject.columns?.length,
        parameters: selectedObject.parameters?.length
      });

      // Set API details based on selected object
      const baseName = selectedObject.name.toLowerCase();
      const endpointPath = `/${baseName.replace(/_/g, '-').toLowerCase()}`;
      
      // Determine HTTP method based on object type
      let httpMethod = 'GET';
      let operation = 'SELECT';
      
      if (selectedObject.type === 'TABLE' || selectedObject.type === 'VIEW') {
        httpMethod = 'GET';
        operation = 'SELECT';
      } else if (selectedObject.type === 'PROCEDURE' || selectedObject.type === 'FUNCTION' || selectedObject.type === 'PACKAGE') {
        httpMethod = 'POST';
        operation = 'EXECUTE';
      }

      setApiDetails(prev => ({
        ...prev,
        apiName: `${selectedObject.name} API`,
        apiCode: `${selectedObject.type.slice(0, 3)}_${selectedObject.name}`,
        description: selectedObject.comment || `API for ${selectedObject.name} ${selectedObject.type.toLowerCase()}`,
        endpointPath: endpointPath,
        owner: selectedObject.owner || 'HR',
        httpMethod: httpMethod
      }));

      // Set schema config
      setSchemaConfig(prev => ({
        ...prev,
        schemaName: selectedObject.owner || 'HR',
        objectType: selectedObject.type || 'TABLE',
        objectName: selectedObject.name || '',
        operation: operation,
        primaryKeyColumn: selectedObject.columns?.find(col => col.key === 'PK')?.name || ''
      }));

      // Clear existing parameters and mappings
      const newParameters = [];
      const newMappings = [];

      // Auto-generate parameters from columns if it's a table or view
      if (selectedObject.columns && selectedObject.columns.length > 0) {
        console.log('Generating parameters from columns:', selectedObject.columns);
        
        selectedObject.columns.forEach((col, index) => {
          const isPrimaryKey = col.key === 'PK';
          const parameterType = isPrimaryKey ? 'path' : 'query';
          
          newParameters.push({
            id: `param-${Date.now()}-${index}`,
            key: col.name.toLowerCase(),
            dbColumn: col.name,
            oracleType: col.type.includes('VARCHAR') ? 'VARCHAR2' : 
                      col.type.includes('NUMBER') ? 'NUMBER' :
                      col.type.includes('DATE') ? 'DATE' : 'VARCHAR2',
            apiType: col.type.includes('NUMBER') ? 'integer' : 'string',
            parameterType: parameterType,
            required: isPrimaryKey || col.nullable === 'N',
            description: col.comment || `From ${selectedObject.name}.${col.name}`,
            example: col.name === 'EMPLOYEE_ID' ? '100' : 
                    col.name.includes('DATE') ? '2024-01-01' :
                    col.name.includes('NAME') ? 'John' : '',
            validationPattern: '',
            defaultValue: col.defaultValue || ''
          });

          // Add response mapping
          newMappings.push({
            id: `mapping-${Date.now()}-${index}`,
            apiField: col.name.toLowerCase(),
            dbColumn: col.name,
            oracleType: col.type.includes('VARCHAR') ? 'VARCHAR2' : 
                      col.type.includes('NUMBER') ? 'NUMBER' :
                      col.type.includes('DATE') ? 'DATE' : 'VARCHAR2',
            apiType: col.type.includes('NUMBER') ? 'integer' : 'string',
            format: col.type.includes('DATE') ? 'date-time' : '',
            nullable: col.nullable === 'Y',
            isPrimaryKey: isPrimaryKey,
            includeInResponse: true
          });
        });
      } else if (selectedObject.parameters && selectedObject.parameters.length > 0) {
        // For procedures/functions, extract parameters
        console.log('Generating parameters from procedure/function:', selectedObject.parameters);
        
        selectedObject.parameters.forEach((param, index) => {
          newParameters.push({
            id: `proc-param-${Date.now()}-${index}`,
            key: param.name.replace('p_', '').toLowerCase(),
            dbParameter: param.name,
            oracleType: param.datatype,
            apiType: param.datatype.includes('NUMBER') ? 'integer' : 'string',
            parameterType: 'query',
            required: param.type === 'IN',
            description: param.name,
            example: '',
            validationPattern: '',
            defaultValue: param.defaultValue || ''
          });
        });

        // If there's a return type for functions
        if (selectedObject.returnType) {
          newMappings.push({
            id: `mapping-${Date.now()}-return`,
            apiField: 'result',
            dbColumn: 'RETURN_VALUE',
            oracleType: selectedObject.returnType,
            apiType: selectedObject.returnType.includes('NUMBER') ? 'integer' : 'string',
            format: '',
            nullable: false,
            isPrimaryKey: false,
            includeInResponse: true
          });
        }
      } else {
        // For other object types (packages, triggers, etc.)
        console.log('No columns or parameters found for object type:', selectedObject.type);
      }

      setParameters(newParameters);
      setResponseMappings(newMappings);
    } else {
      console.log('No selected object provided to modal');
    }
  }, [selectedObject]);

  const [authConfig, setAuthConfig] = useState({
    authType: 'ORACLE_ROLES',
    requiredRoles: [],
    customAuthFunction: '',
    validateSession: true,
    checkObjectPrivileges: true
  });

  const [headers, setHeaders] = useState([
    { id: '1', key: 'Content-Type', value: 'application/json', required: true, description: 'Response content type' },
    { id: '2', key: 'Cache-Control', value: 'no-cache', required: false, description: 'Cache control header' }
  ]);

  const [requestBody, setRequestBody] = useState({
    schemaType: 'JSON',
    sample: '{}',
    validationRules: [],
    requiredFields: []
  });

  const [responseBody, setResponseBody] = useState({
    successSchema: '{\n  "success": true,\n  "data": {},\n  "message": ""\n}',
    errorSchema: '{\n  "success": false,\n  "error": {\n    "code": "",\n    "message": ""\n  }\n}',
    includeMetadata: true,
    metadataFields: ['timestamp', 'apiVersion', 'requestId']
  });

  const [tests, setTests] = useState({
    unitTests: '',
    integrationTests: '',
    testData: '',
    assertions: []
  });

  const [settings, setSettings] = useState({
    timeout: 30000,
    maxRecords: 1000,
    enableLogging: true,
    logLevel: 'INFO',
    enableCaching: false,
    cacheTtl: 300,
    enableRateLimiting: false,
    rateLimit: 100,
    enableAudit: true,
    auditLevel: 'ALL',
    generateSwagger: true,
    generatePostman: true,
    generateClientSDK: true
  });

  const [generatedCode, setGeneratedCode] = useState('');
  const [previewMode, setPreviewMode] = useState('json');

  // Tab definitions
  const tabs = [
    { id: 'definition', label: 'Definition', icon: <FileText className="h-4 w-4" /> },
    { id: 'schema', label: 'Schema', icon: <Database className="h-4 w-4" /> },
    { id: 'parameters', label: 'Parameters', icon: <Hash className="h-4 w-4" /> },
    { id: 'mapping', label: 'Mapping', icon: <Map className="h-4 w-4" /> },
    { id: 'auth', label: 'Authentication', icon: <Lock className="h-4 w-4" /> },
    { id: 'request', label: 'Request', icon: <Upload className="h-4 w-4" /> },
    { id: 'response', label: 'Response', icon: <Download className="h-4 w-4" /> },
    { id: 'tests', label: 'Tests', icon: <TestTube className="h-4 w-4" /> },
    { id: 'settings', label: 'Settings', icon: <Settings className="h-4 w-4" /> },
    { id: 'preview', label: 'Preview', icon: <Eye className="h-4 w-4" /> },
  ];

  // Oracle Data Types
  const oracleDataTypes = [
    'VARCHAR2', 'NUMBER', 'DATE', 'TIMESTAMP', 'TIMESTAMP WITH TIME ZONE',
    'TIMESTAMP WITH LOCAL TIME ZONE', 'INTERVAL YEAR TO MONTH', 'INTERVAL DAY TO SECOND',
    'RAW', 'LONG RAW', 'CHAR', 'NCHAR', 'NVARCHAR2', 'CLOB', 'NCLOB', 'BLOB',
    'BFILE', 'ROWID', 'UROWID'
  ];

  const apiDataTypes = [
    'string', 'integer', 'number', 'boolean', 'array', 'object', 'null'
  ];

  const parameterTypes = [
    { value: 'path', label: 'Path Parameter', description: 'Part of URL path' },
    { value: 'query', label: 'Query Parameter', description: 'URL query string' },
    { value: 'header', label: 'Header Parameter', description: 'HTTP header' },
    { value: 'body', label: 'Body Parameter', description: 'Request body' }
  ];

  // Get HTTP method based on operation type
  const getHttpMethodFromOperation = (operation) => {
    switch(operation) {
      case 'SELECT': return 'GET';
      case 'INSERT': return 'POST';
      case 'UPDATE': return 'PUT';
      case 'DELETE': return 'DELETE';
      case 'EXECUTE': return 'POST';
      default: return 'GET';
    }
  };

  // Handle API details changes
  const handleApiDetailChange = (field, value) => {
    setApiDetails(prev => ({ ...prev, [field]: value }));
  };

  // Handle schema configuration
  const handleSchemaConfigChange = (field, value) => {
    const updatedConfig = { ...schemaConfig, [field]: value };
    setSchemaConfig(updatedConfig);
    
    // Update HTTP method based on operation
    if (field === 'operation') {
      const httpMethod = getHttpMethodFromOperation(value);
      handleApiDetailChange('httpMethod', httpMethod);
    }
  };

  // Handle parameter operations
  const handleAddParameter = () => {
    const newParam = {
      id: `param-${Date.now()}`,
      key: '',
      dbColumn: '',
      oracleType: 'VARCHAR2',
      apiType: 'string',
      parameterType: 'query',
      required: false,
      description: '',
      example: '',
      validationPattern: '',
      defaultValue: ''
    };
    setParameters([...parameters, newParam]);
  };

  const handleParameterChange = (id, field, value) => {
    setParameters(parameters.map(param => 
      param.id === id ? { ...param, [field]: value } : param
    ));
  };

  const handleRemoveParameter = (id) => {
    setParameters(parameters.filter(param => param.id !== id));
  };

  // Handle response mapping operations
  const handleAddResponseMapping = () => {
    const newMapping = {
      id: `mapping-${Date.now()}`,
      apiField: '',
      dbColumn: '',
      oracleType: 'VARCHAR2',
      apiType: 'string',
      format: '',
      nullable: true,
      isPrimaryKey: false,
      includeInResponse: true
    };
    setResponseMappings([...responseMappings, newMapping]);
  };

  const handleResponseMappingChange = (id, field, value) => {
    setResponseMappings(responseMappings.map(mapping => 
      mapping.id === id ? { ...mapping, [field]: value } : mapping
    ));
  };

  const handleRemoveResponseMapping = (id) => {
    setResponseMappings(responseMappings.filter(mapping => mapping.id !== id));
  };

  // Handle header operations
  const handleAddHeader = () => {
    const newHeader = {
      id: `header-${Date.now()}`,
      key: '',
      value: '',
      required: false,
      description: ''
    };
    setHeaders([...headers, newHeader]);
  };

  const handleHeaderChange = (id, field, value) => {
    setHeaders(headers.map(header => 
      header.id === id ? { ...header, [field]: value } : header
    ));
  };

  const handleRemoveHeader = (id) => {
    setHeaders(headers.filter(header => header.id !== id));
  };

  // Generate PL/SQL code
  const generatePLSQLCode = () => {
    const paramList = parameters.map(p => 
      `${p.key} IN ${p.oracleType}${p.required ? ' NOT NULL' : ''} DEFAULT ${p.defaultValue || 'NULL'}`
    ).join(',\n    ');
    
    const mappingList = responseMappings
      .filter(m => m.includeInResponse)
      .map(m => m.dbColumn)
      .join(', ');

    const operationMap = {
      'SELECT': `OPEN v_cursor FOR\n      SELECT ${mappingList}\n      FROM ${schemaConfig.schemaName}.${schemaConfig.objectName}\n      WHERE 1=1`,
      'INSERT': `INSERT INTO ${schemaConfig.schemaName}.${schemaConfig.objectName} (...) VALUES (...)`,
      'UPDATE': `UPDATE ${schemaConfig.schemaName}.${schemaConfig.objectName} SET ...`,
      'DELETE': `DELETE FROM ${schemaConfig.schemaName}.${schemaConfig.objectName} WHERE ...`,
      'EXECUTE': `-- Execute ${schemaConfig.objectType} ${schemaConfig.schemaName}.${schemaConfig.objectName}`
    };

    return `-- ============================================================================
-- API Package: ${apiDetails.apiName}
-- Generated: ${new Date().toISOString()}
-- Version: ${apiDetails.version}
-- Source Object: ${schemaConfig.schemaName}.${schemaConfig.objectName} (${schemaConfig.objectType})
-- ============================================================================

CREATE OR REPLACE PACKAGE ${schemaConfig.schemaName}_${apiDetails.apiCode || 'API'}_PKG AS
  -- ${apiDetails.apiName}
  -- Generated from: ${schemaConfig.schemaName}.${schemaConfig.objectName}
  -- Object Type: ${schemaConfig.objectType}
  -- Operation: ${schemaConfig.operation}
  
  PROCEDURE ${apiDetails.apiCode || 'PROCESS_REQUEST'} (
    ${paramList || '-- No parameters defined'}
  );
  
  -- Helper functions
  FUNCTION validate_parameters RETURN BOOLEAN;
  FUNCTION format_response RETURN CLOB;
  
END ${schemaConfig.schemaName}_${apiDetails.apiCode || 'API'}_PKG;
/

CREATE OR REPLACE PACKAGE BODY ${schemaConfig.schemaName}_${apiDetails.apiCode || 'API'}_PKG AS

  g_api_version CONSTANT VARCHAR2(10) := '${apiDetails.version}';
  g_api_name    CONSTANT VARCHAR2(100) := '${apiDetails.apiName}';
  
  PROCEDURE ${apiDetails.apiCode || 'PROCESS_REQUEST'} (
    ${paramList || '-- No parameters defined'}
  ) IS
    v_cursor SYS_REFCURSOR;
    v_start_time TIMESTAMP := SYSTIMESTAMP;
  BEGIN
    -- Log request
    DBMS_OUTPUT.PUT_LINE('API Request: ' || g_api_name || ' - ' || SYSTIMESTAMP);
    
    -- Validate parameters
    IF NOT validate_parameters THEN
      RAISE_APPLICATION_ERROR(-20001, 'Invalid parameters');
    END IF;
    
    -- Execute operation
    ${operationMap[schemaConfig.operation] || '-- Operation not specified'}
    
    -- Log execution time
    DBMS_OUTPUT.PUT_LINE('Execution time: ' || (SYSTIMESTAMP - v_start_time));
    
    -- Return cursor for SELECT operations
    IF '${schemaConfig.operation}' = 'SELECT' THEN
      DBMS_SQL.RETURN_RESULT(v_cursor);
    END IF;
    
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('API Error: ' || SQLERRM);
      RAISE_APPLICATION_ERROR(-20001, 'API Error: ' || SQLERRM);
  END ${apiDetails.apiCode || 'PROCESS_REQUEST'};
  
  FUNCTION validate_parameters RETURN BOOLEAN IS
  BEGIN
    -- Add parameter validation logic here
    RETURN TRUE;
  END validate_parameters;
  
  FUNCTION format_response RETURN CLOB IS
    v_response CLOB;
  BEGIN
    -- Add response formatting logic here
    RETURN v_response;
  END format_response;

END ${schemaConfig.schemaName}_${apiDetails.apiCode || 'API'}_PKG;
/`;
  };

  // Generate OpenAPI specification
  const generateOpenAPISpec = () => {
    const spec = {
      openapi: '3.0.0',
      info: {
        title: apiDetails.apiName,
        description: apiDetails.description,
        version: apiDetails.version,
        contact: {
          name: apiDetails.owner,
          email: `${apiDetails.owner.toLowerCase()}@example.com`
        }
      },
      servers: [
        {
          url: '{baseUrl}' + apiDetails.basePath,
          variables: {
            baseUrl: {
              default: 'https://api.example.com',
            },
          },
        },
      ],
      paths: {
        [apiDetails.endpointPath]: {
          [apiDetails.httpMethod.toLowerCase()]: {
            summary: apiDetails.apiName,
            description: apiDetails.description,
            tags: apiDetails.tags,
            operationId: apiDetails.apiCode.toLowerCase(),
            parameters: parameters.map(p => ({
              name: p.key,
              in: p.parameterType,
              description: p.description,
              required: p.required,
              schema: {
                type: p.apiType,
                example: p.example,
                pattern: p.validationPattern,
                default: p.defaultValue,
              },
            })),
            responses: {
              '200': {
                description: 'Successful response',
                content: {
                  'application/json': {
                    schema: {
                      type: 'object',
                      properties: responseMappings.reduce((acc, mapping) => ({
                        ...acc,
                        [mapping.apiField]: {
                          type: mapping.apiType,
                          description: `Maps to ${mapping.dbColumn} (${mapping.oracleType})`,
                          nullable: mapping.nullable,
                          format: mapping.format,
                        },
                      }), {}),
                    },
                  },
                },
              },
              '400': {
                description: 'Bad Request'
              },
              '401': {
                description: 'Unauthorized'
              },
              '500': {
                description: 'Internal Server Error'
              }
            },
            security: authConfig.authType !== 'NONE' ? [{ [authConfig.authType.toLowerCase()]: [] }] : [],
          },
        },
      },
      components: {
        securitySchemes: {
          [authConfig.authType.toLowerCase()]: {
            type: 'http',
            scheme: 'bearer'
          }
        }
      }
    };
    
    return JSON.stringify(spec, null, 2);
  };

  // Generate Postman collection
  const generatePostmanCollection = () => {
    const collection = {
      info: {
        name: apiDetails.apiName,
        description: apiDetails.description,
        schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
      },
      item: [
        {
          name: apiDetails.apiName,
          request: {
            method: apiDetails.httpMethod,
            header: headers.map(h => ({
              key: h.key,
              value: h.value,
              description: h.description,
            })),
            url: {
              raw: `{{baseUrl}}${apiDetails.basePath}${apiDetails.endpointPath}`,
              host: ['{{baseUrl}}'],
              path: apiDetails.endpointPath.split('/').filter(Boolean),
              query: parameters
                .filter(p => p.parameterType === 'query')
                .map(p => ({
                  key: p.key,
                  value: p.example || '',
                  description: p.description,
                })),
            },
            description: apiDetails.description,
          },
          response: [],
        },
      ],
      variable: [
        {
          key: 'baseUrl',
          value: 'https://api.example.com',
          type: 'string',
        },
      ],
    };
    
    return JSON.stringify(collection, null, 2);
  };

  // Update preview based on mode
  useEffect(() => {
    switch(previewMode) {
      case 'plsql':
        setGeneratedCode(generatePLSQLCode());
        break;
      case 'openapi':
        setGeneratedCode(generateOpenAPISpec());
        break;
      case 'postman':
        setGeneratedCode(generatePostmanCollection());
        break;
      default:
        setGeneratedCode(JSON.stringify({
          apiDetails,
          schemaConfig,
          parameters: parameters.map(p => ({
            key: p.key,
            dbColumn: p.dbColumn,
            type: p.apiType,
            required: p.required,
            parameterType: p.parameterType
          })),
          responseMappings: responseMappings.map(m => ({
            apiField: m.apiField,
            dbColumn: m.dbColumn,
            type: m.apiType,
            nullable: m.nullable
          })),
          authConfig,
          settings
        }, null, 2));
    }
  }, [previewMode, apiDetails, schemaConfig, parameters, responseMappings, authConfig, settings]);

  // Handle save
  const handleSave = () => {
    const apiData = {
      id: `api-${Date.now()}`,
      ...apiDetails,
      schemaConfig,
      parameters,
      responseMappings,
      authConfig,
      headers,
      requestBody,
      responseBody,
      tests,
      settings,
      generatedCode: {
        plsql: generatePLSQLCode(),
        openapi: generateOpenAPISpec(),
        postman: generatePostmanCollection()
      },
      createdAt: new Date().toISOString(),
      sourceObject: {
        name: selectedObject?.name,
        type: selectedObject?.type,
        owner: selectedObject?.owner,
        columns: selectedObject?.columns?.length,
        parameters: selectedObject?.parameters?.length
      }
    };
    
    if (onSave) {
      onSave(apiData);
    }
    onClose();
  };

  // Copy generated code to clipboard
  const copyGeneratedCode = () => {
    navigator.clipboard.writeText(generatedCode);
    // You can add a toast notification here
  };

  // Download generated code
  const downloadGeneratedCode = () => {
    const blob = new Blob([generatedCode], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${apiDetails.apiCode || 'api'}_${previewMode}.${previewMode === 'json' ? 'json' : 'sql'}`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  if (!isOpen) return null;

  // Use colors from parent or default
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
  };

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4" style={{ zIndex: 1000 }}>
      <div className="rounded-xl shadow-2xl w-full max-w-6xl max-h-[90vh] overflow-hidden flex flex-col" style={{ 
        backgroundColor: themeColors.modalBg,
        border: `1px solid ${themeColors.modalBorder}`
      }}>
        {/* Header */}
        <div className="px-6 py-4 border-b flex items-center justify-between" style={{ 
          borderColor: themeColors.border,
          backgroundColor: themeColors.card
        }}>
          <div className="flex items-center gap-3">
            {selectedObject?.type === 'TABLE' ? <Database className="h-6 w-6" style={{ color: themeColors.info }} /> :
             selectedObject?.type === 'VIEW' ? <FileText className="h-6 w-6" style={{ color: themeColors.success }} /> :
             selectedObject?.type === 'PROCEDURE' ? <Terminal className="h-6 w-6" style={{ color: themeColors.info }} /> :
             selectedObject?.type === 'FUNCTION' ? <Code className="h-6 w-6" style={{ color: themeColors.warning }} /> :
             selectedObject?.type === 'PACKAGE' ? <Package className="h-6 w-6" style={{ color: themeColors.textSecondary }} /> :
             selectedObject?.type === 'TRIGGER' ? <Zap className="h-6 w-6" style={{ color: themeColors.error }} /> :
             <Globe className="h-6 w-6" style={{ color: themeColors.info }} />}
            <div>
              <h2 className="text-xl font-bold" style={{ color: themeColors.text }}>
                Generate API from {selectedObject?.type || 'Object'}: {selectedObject?.name || ''}
              </h2>
              <p className="text-sm" style={{ color: themeColors.textSecondary }}>
                {selectedObject?.owner}.{selectedObject?.name} • {selectedObject?.type} • 
                {selectedObject?.columns ? ` ${selectedObject.columns.length} columns` : ''}
                {selectedObject?.parameters ? ` ${selectedObject.parameters.length} parameters` : ''}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={onClose}
              className="p-2 rounded-lg transition-colors hover-lift"
              style={{ backgroundColor: themeColors.hover, color: themeColors.textSecondary }}
            >
              <X className="h-5 w-5" />
            </button>
          </div>
        </div>

        {/* API Details Section */}
        <div className="px-6 py-4 border-b" style={{ 
          borderColor: themeColors.border,
          backgroundColor: themeColors.modalBg
        }}>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                API Name *
              </label>
              <input
                type="text"
                value={apiDetails.apiName}
                onChange={(e) => handleApiDetailChange('apiName', e.target.value)}
                className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                style={{ 
                  backgroundColor: themeColors.card,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
                placeholder="Users API"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                API Code *
              </label>
              <input
                type="text"
                value={apiDetails.apiCode}
                onChange={(e) => handleApiDetailChange('apiCode', e.target.value)}
                className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                style={{ 
                  backgroundColor: themeColors.card,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
                placeholder="GET_USERS"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                HTTP Method
              </label>
              <select
                value={apiDetails.httpMethod}
                onChange={(e) => handleApiDetailChange('httpMethod', e.target.value)}
                className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                style={{ 
                  backgroundColor: themeColors.card,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
              >
                <option value="GET">GET</option>
                <option value="POST">POST</option>
                <option value="PUT">PUT</option>
                <option value="DELETE">DELETE</option>
                <option value="PATCH">PATCH</option>
              </select>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                Status
              </label>
              <select
                value={apiDetails.status}
                onChange={(e) => handleApiDetailChange('status', e.target.value)}
                className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                style={{ 
                  backgroundColor: themeColors.card,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
              >
                <option value="DRAFT">Draft</option>
                <option value="ACTIVE">Active</option>
                <option value="DEPRECATED">Deprecated</option>
                <option value="ARCHIVED">Archived</option>
              </select>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                Base Path
              </label>
              <input
                type="text"
                value={apiDetails.basePath}
                onChange={(e) => handleApiDetailChange('basePath', e.target.value)}
                className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                style={{ 
                  backgroundColor: themeColors.card,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
                placeholder="/api/v1"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                Endpoint Path *
              </label>
              <input
                type="text"
                value={apiDetails.endpointPath}
                onChange={(e) => handleApiDetailChange('endpointPath', e.target.value)}
                className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                style={{ 
                  backgroundColor: themeColors.card,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
                placeholder="/users"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                Version
              </label>
              <input
                type="text"
                value={apiDetails.version}
                onChange={(e) => handleApiDetailChange('version', e.target.value)}
                className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                style={{ 
                  backgroundColor: themeColors.card,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
                placeholder="1.0.0"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                Owner
              </label>
              <input
                type="text"
                value={apiDetails.owner}
                onChange={(e) => handleApiDetailChange('owner', e.target.value)}
                className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                style={{ 
                  backgroundColor: themeColors.card,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
                placeholder="HR"
                disabled
              />
            </div>

            <div className="space-y-2 md:col-span-2 lg:col-span-4">
              <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                Description
              </label>
              <textarea
                value={apiDetails.description}
                onChange={(e) => handleApiDetailChange('description', e.target.value)}
                className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                style={{ 
                  backgroundColor: themeColors.card,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
                rows="2"
                placeholder="API description..."
              />
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="border-b" style={{ 
          borderColor: themeColors.border,
          backgroundColor: themeColors.card
        }}>
          <div className="px-6 flex space-x-1 overflow-x-auto">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`px-3 py-2 flex items-center gap-2 border-b-2 text-sm font-medium transition-colors whitespace-nowrap hover-lift ${
                  activeTab === tab.id
                    ? '' : 'hover:bg-opacity-50'
                }`}
                style={{ 
                  borderBottomColor: activeTab === tab.id ? themeColors.info : 'transparent',
                  color: activeTab === tab.id ? themeColors.info : themeColors.textSecondary,
                  backgroundColor: 'transparent'
                }}
              >
                {tab.icon}
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {/* Tab Content */}
        <div className="flex-1 overflow-y-auto">
          <div className="p-4 md:p-6">
            {/* Definition Tab */}
            {activeTab === 'definition' && (
              <div className="space-y-6">
                <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                  API Definition
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <div className="space-y-2">
                      <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                        Category
                      </label>
                      <select
                        value={apiDetails.category}
                        onChange={(e) => handleApiDetailChange('category', e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                        style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: themeColors.border,
                          color: themeColors.text
                        }}
                      >
                        <option value="general">General</option>
                        <option value="data">Data Access</option>
                        <option value="admin">Administrative</option>
                        <option value="report">Reporting</option>
                        <option value="integration">Integration</option>
                      </select>
                    </div>

                    <div className="space-y-2">
                      <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                        Tags
                      </label>
                      <input
                        type="text"
                        value={apiDetails.tags.join(', ')}
                        onChange={(e) => handleApiDetailChange('tags', e.target.value.split(',').map(tag => tag.trim()))}
                        className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                        style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: themeColors.border,
                          color: themeColors.text
                        }}
                        placeholder="users, data, public"
                      />
                    </div>
                  </div>

                  <div className="p-4 rounded-lg border" style={{ 
                    borderColor: themeColors.info + '40',
                    backgroundColor: themeColors.info + '10'
                  }}>
                    <h4 className="font-medium mb-2 flex items-center gap-2" style={{ color: themeColors.info }}>
                      <Globe className="h-4 w-4" />
                      API Endpoint Preview
                    </h4>
                    <div className="font-mono text-sm p-3 rounded border" style={{ 
                      backgroundColor: themeColors.card,
                      borderColor: themeColors.border
                    }}>
                      <div style={{ color: themeColors.textSecondary }}>
                        {apiDetails.httpMethod} {apiDetails.basePath}{apiDetails.endpointPath}
                      </div>
                      {parameters.filter(p => p.parameterType === 'path').length > 0 && (
                        <div className="mt-2 text-sm" style={{ color: themeColors.textTertiary }}>
                          Path Parameters: {parameters.filter(p => p.parameterType === 'path').map(p => `{${p.key}}`).join('/')}
                        </div>
                      )}
                      <div className="mt-2 text-sm" style={{ color: themeColors.textTertiary }}>
                        Source: {schemaConfig.schemaName}.{schemaConfig.objectName} ({schemaConfig.objectType})
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Schema Tab */}
            {activeTab === 'schema' && (
              <div className="space-y-6">
                <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                  Oracle Schema Configuration
                </h3>

                <div className="mb-4 p-4 rounded-lg border" style={{ 
                  borderColor: themeColors.info + '40',
                  backgroundColor: themeColors.info + '10'
                }}>
                  <h4 className="font-medium mb-2 flex items-center gap-2" style={{ color: themeColors.info }}>
                    <Database className="h-4 w-4" />
                    Selected Object Information
                  </h4>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Object:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {selectedObject?.owner}.{selectedObject?.name}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Type:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {selectedObject?.type}
                      </span>
                    </div>
                    {selectedObject?.columns && (
                      <div>
                        <span style={{ color: themeColors.textSecondary }}>Columns:</span>
                        <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                          {selectedObject.columns.length}
                        </span>
                      </div>
                    )}
                    {selectedObject?.parameters && (
                      <div>
                        <span style={{ color: themeColors.textSecondary }}>Parameters:</span>
                        <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                          {selectedObject.parameters.length}
                        </span>
                      </div>
                    )}
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <div className="space-y-2">
                      <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                        Schema Name *
                      </label>
                      <input
                        type="text"
                        value={schemaConfig.schemaName}
                        onChange={(e) => handleSchemaConfigChange('schemaName', e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                        style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: themeColors.border,
                          color: themeColors.text
                        }}
                        placeholder="HR"
                      />
                    </div>

                    <div className="space-y-2">
                      <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                        Object Type
                      </label>
                      <select
                        value={schemaConfig.objectType}
                        onChange={(e) => handleSchemaConfigChange('objectType', e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                        style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: themeColors.border,
                          color: themeColors.text
                        }}
                      >
                        <option value="TABLE">Table</option>
                        <option value="VIEW">View</option>
                        <option value="PROCEDURE">Procedure</option>
                        <option value="FUNCTION">Function</option>
                        <option value="PACKAGE">Package</option>
                        <option value="TRIGGER">Trigger</option>
                      </select>
                    </div>

                    <div className="space-y-2">
                      <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                        Object Name *
                      </label>
                      <input
                        type="text"
                        value={schemaConfig.objectName}
                        onChange={(e) => handleSchemaConfigChange('objectName', e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                        style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: themeColors.border,
                          color: themeColors.text
                        }}
                        placeholder="EMPLOYEES"
                      />
                    </div>

                    <div className="space-y-2">
                      <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                        Operation
                      </label>
                      <select
                        value={schemaConfig.operation}
                        onChange={(e) => handleSchemaConfigChange('operation', e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                        style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: themeColors.border,
                          color: themeColors.text
                        }}
                      >
                        <option value="SELECT">SELECT (Read)</option>
                        <option value="INSERT">INSERT (Create)</option>
                        <option value="UPDATE">UPDATE (Update)</option>
                        <option value="DELETE">DELETE (Delete)</option>
                        <option value="EXECUTE">EXECUTE (Procedure/Function)</option>
                      </select>
                    </div>
                  </div>

                  <div className="space-y-4">
                    <div className="space-y-2">
                      <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                        Primary Key Column
                      </label>
                      <input
                        type="text"
                        value={schemaConfig.primaryKeyColumn}
                        onChange={(e) => handleSchemaConfigChange('primaryKeyColumn', e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                        style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: themeColors.border,
                          color: themeColors.text
                        }}
                        placeholder="ID"
                      />
                    </div>

                    <div className="space-y-2">
                      <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                        Sequence Name (for INSERT)
                      </label>
                      <input
                        type="text"
                        value={schemaConfig.sequenceName}
                        onChange={(e) => handleSchemaConfigChange('sequenceName', e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                        style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: themeColors.border,
                          color: themeColors.text
                        }}
                        placeholder="SEQ_TABLE_NAME"
                      />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Enable Pagination
                        </label>
                        <div className="flex items-center">
                          <input
                            type="checkbox"
                            checked={schemaConfig.enablePagination}
                            onChange={(e) => handleSchemaConfigChange('enablePagination', e.target.checked)}
                            className="h-4 w-4 rounded"
                            style={{ 
                              accentColor: themeColors.info,
                              backgroundColor: themeColors.card
                            }}
                          />
                          <span className="ml-2 text-sm" style={{ color: themeColors.textSecondary }}>Yes</span>
                        </div>
                      </div>

                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Page Size
                        </label>
                        <input
                          type="number"
                          value={schemaConfig.pageSize}
                          onChange={(e) => handleSchemaConfigChange('pageSize', parseInt(e.target.value))}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
                            borderColor: themeColors.border,
                            color: themeColors.text
                          }}
                          min="1"
                          max="1000"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Parameters Tab - Show auto-generated parameters */}
            {activeTab === 'parameters' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                    API Parameters ({parameters.length})
                    {selectedObject?.columns && (
                      <span className="text-sm font-normal ml-2" style={{ color: themeColors.textSecondary }}>
                        (Auto-generated from {selectedObject.columns.length} columns)
                      </span>
                    )}
                    {selectedObject?.parameters && (
                      <span className="text-sm font-normal ml-2" style={{ color: themeColors.textSecondary }}>
                        (Auto-generated from {selectedObject.parameters.length} parameters)
                      </span>
                    )}
                  </h3>
                  <button
                    onClick={handleAddParameter}
                    className="px-3 py-1.5 rounded-lg flex items-center gap-2 text-sm transition-colors hover-lift"
                    style={{ backgroundColor: themeColors.info, color: themeColors.white }}
                  >
                    <Plus className="h-4 w-4" />
                    Add Parameter
                  </button>
                </div>

                {parameters.length === 0 ? (
                  <div className="text-center py-8 border rounded-lg" style={{ 
                    borderColor: themeColors.border,
                    backgroundColor: themeColors.card
                  }}>
                    <Code className="h-12 w-12 mx-auto mb-3" style={{ color: themeColors.textSecondary }} />
                    <p style={{ color: themeColors.textSecondary }}>
                      No parameters defined. Add parameters or they will be auto-generated from the selected object.
                    </p>
                  </div>
                ) : (
                  <div className="overflow-x-auto border rounded-lg" style={{ 
                    borderColor: themeColors.border,
                    backgroundColor: themeColors.card
                  }}>
                    <table className="w-full min-w-[1000px]">
                      <thead>
                        <tr style={{ backgroundColor: themeColors.hover }}>
                          <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                            borderColor: themeColors.border,
                            color: themeColors.textSecondary
                          }}>Parameter</th>
                          <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                            borderColor: themeColors.border,
                            color: themeColors.textSecondary
                          }}>DB Column</th>
                          <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                            borderColor: themeColors.border,
                            color: themeColors.textSecondary
                          }}>Type</th>
                          <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                            borderColor: themeColors.border,
                            color: themeColors.textSecondary
                          }}>Parameter Type</th>
                          <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                            borderColor: themeColors.border,
                            color: themeColors.textSecondary
                          }}>Required</th>
                          <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                            borderColor: themeColors.border,
                            color: themeColors.textSecondary
                          }}>Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {parameters.map((param, index) => (
                          <tr key={param.id} style={{ 
                            backgroundColor: index % 2 === 0 ? themeColors.card : themeColors.hover,
                            borderBottom: `1px solid ${themeColors.border}`
                          }}>
                            <td className="px-3 py-2">
                              <input
                                type="text"
                                value={param.key}
                                onChange={(e) => handleParameterChange(param.id, 'key', e.target.value)}
                                className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.modalBg,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                                placeholder="parameter_key"
                              />
                            </td>
                            <td className="px-3 py-2">
                              <input
                                type="text"
                                value={param.dbColumn}
                                onChange={(e) => handleParameterChange(param.id, 'dbColumn', e.target.value)}
                                className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.modalBg,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                                placeholder="DB_COLUMN"
                              />
                            </td>
                            <td className="px-3 py-2">
                              <select
                                value={param.oracleType}
                                onChange={(e) => handleParameterChange(param.id, 'oracleType', e.target.value)}
                                className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.modalBg,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                              >
                                {oracleDataTypes.map(type => (
                                  <option key={type} value={type}>{type}</option>
                                ))}
                              </select>
                            </td>
                            <td className="px-3 py-2">
                              <select
                                value={param.parameterType}
                                onChange={(e) => handleParameterChange(param.id, 'parameterType', e.target.value)}
                                className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.modalBg,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                              >
                                {parameterTypes.map(type => (
                                  <option key={type.value} value={type.value}>{type.label}</option>
                                ))}
                              </select>
                            </td>
                            <td className="px-3 py-2 text-center">
                              <input
                                type="checkbox"
                                checked={param.required}
                                onChange={(e) => handleParameterChange(param.id, 'required', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.info }}
                              />
                            </td>
                            <td className="px-3 py-2">
                              <button
                                onClick={() => handleRemoveParameter(param.id)}
                                className="p-1.5 rounded transition-colors hover-lift"
                                style={{ backgroundColor: themeColors.error + '20', color: themeColors.error }}
                                title="Delete parameter"
                              >
                                <Trash2 className="h-4 w-4" />
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            )}

            {/* Mapping Tab */}
            {activeTab === 'mapping' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                    Response Field Mapping ({responseMappings.length})
                    {selectedObject?.columns && (
                      <span className="text-sm font-normal ml-2" style={{ color: themeColors.textSecondary }}>
                        (Auto-generated from {selectedObject.columns.length} columns)
                      </span>
                    )}
                  </h3>
                  <button
                    onClick={handleAddResponseMapping}
                    className="px-3 py-1.5 rounded-lg flex items-center gap-2 text-sm transition-colors hover-lift"
                    style={{ backgroundColor: themeColors.info, color: themeColors.white }}
                  >
                    <Plus className="h-4 w-4" />
                    Add Mapping
                  </button>
                </div>

                {responseMappings.length === 0 ? (
                  <div className="text-center py-8 border rounded-lg" style={{ 
                    borderColor: themeColors.border,
                    backgroundColor: themeColors.card
                  }}>
                    <Map className="h-12 w-12 mx-auto mb-3" style={{ color: themeColors.textSecondary }} />
                    <p style={{ color: themeColors.textSecondary }}>
                      No response mappings defined. They will be auto-generated from the selected object's columns.
                    </p>
                  </div>
                ) : (
                  <div className="overflow-x-auto border rounded-lg" style={{ 
                    borderColor: themeColors.border,
                    backgroundColor: themeColors.card
                  }}>
                    <table className="w-full min-w-[800px]">
                      <thead>
                        <tr style={{ backgroundColor: themeColors.hover }}>
                          <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>API Field</th>
                          <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>DB Column</th>
                          <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Oracle Type</th>
                          <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>API Type</th>
                          <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Nullable</th>
                          <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {responseMappings.map((mapping, index) => (
                          <tr key={mapping.id} style={{ 
                            backgroundColor: index % 2 === 0 ? themeColors.card : themeColors.hover,
                            borderBottom: `1px solid ${themeColors.border}`
                          }}>
                            <td className="px-3 py-2">
                              <input
                                type="text"
                                value={mapping.apiField}
                                onChange={(e) => handleResponseMappingChange(mapping.id, 'apiField', e.target.value)}
                                className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.modalBg,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                                placeholder="fieldName"
                              />
                            </td>
                            <td className="px-3 py-2">
                              <input
                                type="text"
                                value={mapping.dbColumn}
                                onChange={(e) => handleResponseMappingChange(mapping.id, 'dbColumn', e.target.value)}
                                className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.modalBg,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                                placeholder="DB_COLUMN"
                              />
                            </td>
                            <td className="px-3 py-2">
                              <select
                                value={mapping.oracleType}
                                onChange={(e) => handleResponseMappingChange(mapping.id, 'oracleType', e.target.value)}
                                className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.modalBg,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                              >
                                {oracleDataTypes.map(type => (
                                  <option key={type} value={type}>{type}</option>
                                ))}
                              </select>
                            </td>
                            <td className="px-3 py-2">
                              <select
                                value={mapping.apiType}
                                onChange={(e) => handleResponseMappingChange(mapping.id, 'apiType', e.target.value)}
                                className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.modalBg,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                              >
                                {apiDataTypes.map(type => (
                                  <option key={type} value={type}>{type}</option>
                                ))}
                              </select>
                            </td>
                            <td className="px-3 py-2 text-center">
                              <input
                                type="checkbox"
                                checked={mapping.nullable}
                                onChange={(e) => handleResponseMappingChange(mapping.id, 'nullable', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.info }}
                              />
                            </td>
                            <td className="px-3 py-2">
                              <button
                                onClick={() => handleRemoveResponseMapping(mapping.id)}
                                className="p-1.5 rounded transition-colors hover-lift"
                                style={{ backgroundColor: themeColors.error + '20', color: themeColors.error }}
                                title="Delete mapping"
                              >
                                <Trash2 className="h-4 w-4" />
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            )}

            {/* Preview Tab */}
            {activeTab === 'preview' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                    Generated Code Preview
                    <span className="text-sm font-normal ml-2" style={{ color: themeColors.textSecondary }}>
                      (Based on {selectedObject?.name || 'selected object'})
                    </span>
                  </h3>
                  <div className="flex items-center gap-2">
                    <select
                      value={previewMode}
                      onChange={(e) => setPreviewMode(e.target.value)}
                      className="px-3 py-1.5 border rounded-lg text-sm hover-lift"
                      style={{ 
                        backgroundColor: themeColors.card,
                        borderColor: themeColors.border,
                        color: themeColors.text
                      }}
                    >
                      <option value="json">Configuration JSON</option>
                      <option value="plsql">PL/SQL Package</option>
                      <option value="openapi">OpenAPI Spec</option>
                      <option value="postman">Postman Collection</option>
                    </select>
                    <button
                      onClick={copyGeneratedCode}
                      className="px-3 py-1.5 rounded-lg flex items-center gap-2 transition-colors hover-lift"
                      style={{ backgroundColor: themeColors.hover, color: themeColors.text }}
                    >
                      <Copy className="h-4 w-4" />
                      Copy
                    </button>
                    <button
                      onClick={downloadGeneratedCode}
                      className="px-3 py-1.5 rounded-lg flex items-center gap-2 transition-colors hover-lift"
                      style={{ backgroundColor: themeColors.info, color: themeColors.white }}
                    >
                      <Download className="h-4 w-4" />
                      Download
                    </button>
                  </div>
                </div>

                <div className="border rounded-lg" style={{ 
                  borderColor: themeColors.border,
                  backgroundColor: themeColors.card
                }}>
                  <div className="px-4 py-2 border-b flex items-center justify-between" style={{ borderColor: themeColors.border }}>
                    <span className="text-sm font-medium" style={{ color: themeColors.text }}>
                      {previewMode === 'plsql' ? 'PL/SQL Package' : 
                       previewMode === 'openapi' ? 'OpenAPI Specification' :
                       previewMode === 'postman' ? 'Postman Collection' : 'Configuration'}
                      <span className="ml-2 text-xs" style={{ color: themeColors.textSecondary }}>
                        (Source: {schemaConfig.schemaName}.{schemaConfig.objectName})
                      </span>
                    </span>
                    <span className="text-xs font-mono" style={{ color: themeColors.textSecondary }}>
                      {previewMode === 'plsql' ? '.sql' : '.json'}
                    </span>
                  </div>
                  <pre className="w-full h-[400px] px-4 py-3 overflow-auto text-sm" style={{ 
                    backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                    color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                  }}>
                    {generatedCode}
                  </pre>
                </div>
              </div>
            )}

            {/* Other tabs content can be added similarly... */}
            {activeTab === 'auth' && (
              <div className="space-y-6">
                <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                  Authentication & Authorization
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <div className="space-y-2">
                      <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                        Authentication Type
                      </label>
                      <select
                        value={authConfig.authType}
                        onChange={(e) => setAuthConfig(prev => ({ ...prev, authType: e.target.value }))}
                        className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                        style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: themeColors.border,
                          color: themeColors.text
                        }}
                      >
                        <option value="NONE">None (Public)</option>
                        <option value="ORACLE_ROLES">Oracle Database Roles</option>
                        <option value="API_KEY">API Key</option>
                        <option value="JWT">JWT Token</option>
                        <option value="OAUTH2">OAuth 2.0</option>
                      </select>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {['request', 'response', 'tests', 'settings'].includes(activeTab) && (
              <div className="text-center py-12">
                <div className="inline-flex items-center justify-center w-16 h-16 rounded-full mb-4" style={{ backgroundColor: themeColors.info + '20' }}>
                  <Settings className="h-8 w-8" style={{ color: themeColors.info }} />
                </div>
                <h3 className="text-lg font-semibold mb-2" style={{ color: themeColors.text }}>
                  {activeTab.charAt(0).toUpperCase() + activeTab.slice(1)} Configuration
                </h3>
                <p style={{ color: themeColors.textSecondary }}>
                  This section will be implemented in the next phase.
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t flex flex-col sm:flex-row items-center justify-between gap-4" style={{ 
          borderColor: themeColors.border,
          backgroundColor: themeColors.card
        }}>
          <div className="text-sm" style={{ color: themeColors.textSecondary }}>
            Endpoint: <span className="font-mono font-medium" style={{ color: themeColors.text }}>
              {apiDetails.httpMethod} {apiDetails.basePath}{apiDetails.endpointPath}
            </span>
            <br />
            <span className="text-xs" style={{ color: themeColors.textTertiary }}>
              Source: {schemaConfig.schemaName}.{schemaConfig.objectName} ({schemaConfig.objectType})
            </span>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={onClose}
              className="px-4 py-2 border rounded-lg transition-colors hover-lift"
              style={{ 
                backgroundColor: themeColors.hover,
                borderColor: themeColors.border,
                color: themeColors.text
              }}
            >
              Cancel
            </button>
            <button
              onClick={handleSave}
              className="px-4 py-2 rounded-lg flex items-center gap-2 transition-colors hover-lift"
              style={{ backgroundColor: themeColors.success, color: themeColors.white }}
            >
              <Save className="h-4 w-4" />
              Generate & Save API
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}