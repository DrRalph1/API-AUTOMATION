// components/modals/AutoAPIGeneratorModal.js - COMPLETE FIXED VERSION WITH PROTOCOL SELECTOR (REST/SOAP/GraphQL)
import React, { useState, useEffect, useCallback, useRef } from 'react';
import ReactDOM from 'react-dom';
import { 
  X, Plus, Trash2, Save, Copy, Code, Globe, Lock, FileText, 
  Settings, Database, Map, FileJson, TestTube, Wrench, 
  RefreshCw, Eye, EyeOff, Download, Upload, Play, Key, XCircle,
  Shield, Hash, Calendar, Clock, Type, List, Link, ExternalLink,
  Check, AlertCircle, Star, Zap, Terminal, Package, Bookmark,
  CheckCircle, ChevronRight, Info, Layers, Cpu, Sparkles,
  Loader, Search, Filter, User, Users, Bell, ShieldCheck, Unlock,
  BellOff, ShieldOff, Clock as ClockIcon, BarChart, Cpu as CpuIcon,
  Server, Cloud, CloudOff, FileCode, BookOpen, FileKey, GitBranch,
  Folder, FolderOpen, FolderTree, Layers as LayersIcon, Archive,
  Edit, Edit3, Asterisk, Table, ChevronLeft, Wand2, File,
  // SOAP & GraphQL icons
  Send, Wifi, Network, Box, GitMerge, PieChart, Activity
} from 'lucide-react';

// Import the API Generation Engine controller functions
import {
  generateApi,
  validateSourceObject,
  checkApiCodeAvailability,
  getApiCategories,
  extractApiData,
  getHttpMethodColor,
  getStatusColor,
  canExecuteApi,
  canEditApi,
  downloadGeneratedFile,
  updateApi
} from "../../controllers/AutoAPIGeneratorEngineController.js";

import * as OracleSchemaController from '../../controllers/OracleSchemaController.js';
import * as PostgreSQLSchemaController from '../../controllers/PostgreSQLSchemaController.js';

// Mock collections data for banking system
const MOCK_COLLECTIONS = [
  {
    id: 'core-api',
    name: 'Core Banking API Collection',
    description: 'Core banking system APIs for loans, accounts, and customer management',
    type: 'core',
    folders: [
      { id: 'core-api-loans', name: 'Loans', description: 'Loan origination, servicing, and management' },
      { id: 'core-api-investments', name: 'Investments', description: 'Investment products, portfolio management, and trading operations'},
      { id: 'core-api-accounts', name: 'Accounts', description: 'Deposit accounts, savings, and current accounts' },
      { id: 'core-api-customers', name: 'Customers', description: 'Customer onboarding and profile management' },
      { id: 'core-api-notifications', name: 'Notifications', description: 'Send and manage user notifications, alerts, and messages' },
      { id: 'core-api-transactions', name: 'Transactions', description: 'Financial transactions and history' },
      { id: 'core-api-cards', name: 'Cards', description: 'Debit and credit card management' },
      { id: 'core-api-trade-finance', name: 'Trade Finance', description: 'Letters of credit, guarantees, and collections' },
      { id: 'core-api-treasury', name: 'Treasury', description: 'Treasury operations and foreign exchange' },
      { id: 'core-api-enquiry', name: 'Enquiry Services', description: 'Balance enquiries, statement enquiries' },
      { 
        "id": "core-api-security", 
        "name": "Security", 
        "description": "Authentication, authorization, fraud prevention, and API protection services" 
      },
      { 
        "id": "core-api-settings", 
        "name": "Settings", 
        "description": "System configuration, user preferences, limits, and parameter management" 
      }
    ]
  },
  {
    id: 'customer-channel',
    name: 'Customer Channel API Collection',
    description: 'APIs for mobile banking, internet banking, and customer portals',
    type: 'channel',
    folders: [
      { id: 'customer-channel-mobile-banking', name: 'Mobile Banking', description: 'Mobile app specific APIs' },
      { id: 'customer-channel-internet-banking', name: 'Internet Banking', description: 'Web banking portal APIs' },
      { id: 'customer-channel-atm', name: 'ATM Services', description: 'ATM and CDM related services' },
      { id: 'customer-channel-ussd', name: 'USSD Banking', description: 'USSD banking services' },
      { id: 'customer-channel-notifications', name: 'Notifications', description: 'SMS, email, and push notifications' }
    ]
  },
  {
    id: 'payment-gateway',
    name: 'Payment Gateway API Collection',
    description: 'Payment processing, transfers, and gateway services',
    type: 'payment',
    folders: [
      { id: 'payment-gateway-transfers', name: 'Fund Transfers', description: 'Internal and external transfers' },
      { id: 'payment-gateway-rtgs-neft', name: 'RTGS/NEFT', description: 'Interbank payment systems' },
      { id: 'payment-gateway-upi', name: 'UPI Services', description: 'Unified Payments Interface' },
      { id: 'payment-gateway-imps', name: 'IMPS', description: 'Immediate Payment Service' },
      { id: 'payment-gateway-bill-payments', name: 'Bill Payments', description: 'Utility bill payments' },
      { id: 'payment-gateway-recurring', name: 'Recurring Payments', description: 'Standing instructions and mandates' }
    ]
  },
  {
    id: 'self-service',
    name: 'Self Service API Collection',
    description: 'APIs for customer self-service operations, transactions, and account management',
    type: 'selfservice',
    folders: [
      { id: 'self-service-profile-management', name: 'Profile Management', description: 'Update personal information, change password, manage communication preferences, update contact details' },
      { id: 'self-service-account-services', name: 'Account Services', description: 'Open new accounts, close accounts, manage account settings, link/delink accounts' },
      { id: 'self-service-card-services', name: 'Card Services', description: 'Block/unblock cards, report lost/stolen, request replacement, set limits, manage card controls' },
      { id: 'self-service-transactions', name: 'Transactions', description: 'View transaction history, search transactions, export transactions, filter by date/type/amount' },
      { id: 'self-service-funds-transfer', name: 'Funds Transfer', description: 'Initiate transfers, schedule future transfers, manage transfer templates, quick pay' },
      { id: 'self-service-reporting', name: 'Reporting', description: 'Generate custom reports, download statements, spending analysis, tax reports, interest certificates' },
      { id: 'self-service-cheque-services', name: 'Cheque Services', description: 'Request cheque books, stop cheque payments, track cheque status, view cheque images' },
      { id: 'self-service-standing-instructions', name: 'Standing Instructions', description: 'Create, modify, and cancel standing instructions and auto-payments, view scheduled payments' },
      { id: 'self-service-beneficiary-management', name: 'Beneficiary Management', description: 'Add, edit, and delete beneficiary accounts, manage beneficiary limits, verify beneficiaries' },
      { id: 'self-service-statement-requests', name: 'Statement Requests', description: 'Generate account statements, download e-statements, request physical statements, historical statements' },
      { id: 'self-service-pin-management', name: 'PIN Management', description: 'Generate/change ATM PIN, set transaction limits, manage PIN for cards' },
      { id: 'self-service-service-requests', name: 'Service Requests', description: 'Raise and track service requests, complaints, inquiries, view request history' },
      { id: 'self-service-document-upload', name: 'Document Upload', description: 'Upload KYC documents, submit supporting documents, view uploaded documents' },
      { id: 'self-service-alerts-notifications', name: 'Alerts & Notifications', description: 'Manage alert preferences, view notifications, setup transaction alerts' },
      { id: 'self-service-tax-services', name: 'Tax Services', description: 'View tax statements, download tax certificates, interest certificates for IT returns' },
      { id: 'self-service-investment-services', name: 'Investment Services', description: 'View fixed deposits, recurring deposits, open new deposits, premature withdrawal' },
      { id: 'self-service-loan-services', name: 'Loan Services', description: 'View loan details, check outstanding, request statements, part-payment, foreclosure' },
      { id: 'self-service-forex-services', name: 'Forex Services', description: 'View exchange rates, travel card loading, forex bookings' }
    ]
  },
  {
    id: 'third-party',
    name: 'Third Party API Collection',
    description: 'APIs for third-party integration and open banking',
    type: 'thirdparty',
    folders: [
      { id: 'third-party-open-banking', name: 'Open Banking', description: 'PSD2 and open banking APIs' },
      { id: 'third-party-aggregators', name: 'Account Aggregators', description: 'Account aggregator framework' },
      { id: 'third-party-partner-integration', name: 'Partner Integration', description: 'Third-party partner APIs' },
      { id: 'third-party-credit-bureau', name: 'Credit Bureau', description: 'Credit bureau integration' },
      { id: 'third-party-kyc', name: 'KYC Services', description: 'KYC verification services' }
    ]
  },
  {
    id: 'admin-operations',
    name: 'Administrative API Collection',
    description: 'Internal administrative and operational APIs',
    type: 'admin',
    folders: [
      { id: 'admin-operations-user-admin', name: 'User Administration', description: 'User management and roles' },
      { id: 'admin-operations-audit', name: 'Audit Services', description: 'Audit log and compliance' },
      { id: 'admin-operations-reports', name: 'Reports', description: 'Reporting and analytics' },
      { id: 'admin-operations-system-monitoring', name: 'System Monitoring', description: 'System health and monitoring' },
      { id: 'admin-operations-configuration', name: 'Configuration', description: 'System configuration' }
    ]
  },
  {
    id: 'miscellaneous',
    name: 'Miscellaneous API Collection',
    description: 'Utility and miscellaneous services',
    type: 'misc',
    folders: [
      { id: 'miscellaneous-utilities', name: 'Utilities', description: 'Utility services' },
      { id: 'miscellaneous-reference-data', name: 'Reference Data', description: 'Reference and master data' },
      { id: 'miscellaneous-file-services', name: 'File Services', description: 'File upload and download' },
      { id: 'miscellaneous-document-management', name: 'Document Management', description: 'Document storage and retrieval' },
      { id: 'miscellaneous-other', name: 'Other Services', description: 'Miscellaneous services' }
    ]
  },
  {
    id: 'internet-banking',
    name: 'Internet Banking API Collection',
    description: 'APIs for internet banking portal, user management, and digital banking services',
    type: 'channel',
    folders: [
      { id: 'internet-banking-users', name: 'Users', description: 'User management, profiles, roles, permissions, and access control' },
      { id: 'internet-banking-authentication', name: 'Authentication', description: 'Login, logout, MFA, password management, session handling' },
      { id: 'internet-banking-dashboard', name: 'Dashboard', description: 'Account summaries, balances, widgets, and quick actions' },
      { id: 'internet-banking-accounts', name: 'Accounts', description: 'Account details, statements, and account management' },
      { id: 'internet-banking-transactions', name: 'Transactions', description: 'Transaction history, search, filters, and export' },
      { id: 'internet-banking-funds-transfer', name: 'Funds Transfer', description: 'Own account transfers, third-party transfers, IMPS, NEFT, RTGS' },
      { id: 'internet-banking-beneficiaries', name: 'Beneficiaries', description: 'Add, edit, delete beneficiaries, manage limits, approvals' },
      { id: 'internet-banking-bill-payments', name: 'Bill Payments', description: 'Utility bills, credit card bills, recurring payments' },
      { id: 'internet-banking-cards', name: 'Cards', description: 'Card management, block/unblock, limits, PIN generation' },
      { id: 'internet-banking-loans', name: 'Loans', description: 'Loan details, repayment schedule, part-payment, foreclosure' },
      { id: 'internet-banking-deposits', name: 'Deposits', description: 'Fixed deposits, recurring deposits, premature withdrawal' },
      { id: 'internet-banking-cheques', name: 'Cheques', description: 'Cheque book request, stop payment, cheque status tracking' },
      { id: 'internet-banking-statements', name: 'Statements', description: 'Account statements, tax certificates, interest certificates' },
      { id: 'internet-banking-service-requests', name: 'Service Requests', description: 'Raise requests, track status, complaint management' },
      { id: 'internet-banking-notifications', name: 'Notifications', description: 'Alerts, messages, email/SMS preferences' },
      { id: 'internet-banking-documents', name: 'Documents', description: 'Document upload, download, KYC submission' },
      { id: 'internet-banking-investments', name: 'Investments', description: 'Mutual funds, insurance, wealth management' },
      { id: 'internet-banking-forex', name: 'Forex', description: 'Exchange rates, travel cards, forex bookings' },
      { id: 'internet-banking-tax', name: 'Tax Services', description: 'Tax payments, TDS certificates, Form 16' },
      { id: 'internet-banking-security', name: 'Security', description: 'Security settings, device management, login history' },
      { id: 'internet-banking-reports', name: 'Reports', description: 'Custom reports, spending analysis, financial insights' },
      { id: 'internet-banking-preferences', name: 'Preferences', description: 'Language, theme, dashboard customization' }
    ]
  }
];

// Protocol types
const PROTOCOL_TYPES = [
  { value: 'rest', label: 'REST API', icon: <Globe className="h-4 w-4" />, description: 'Standard RESTful API with HTTP methods' },
  { value: 'soap', label: 'SOAP Web Service', icon: <Send className="h-4 w-4" />, description: 'SOAP XML-based web service' },
  { value: 'graphql', label: 'GraphQL API', icon: <GitMerge className="h-4 w-4" />, description: 'GraphQL query language API' }
];

// SOAP versions
const SOAP_VERSIONS = [
  { value: '1.1', label: 'SOAP 1.1' },
  { value: '1.2', label: 'SOAP 1.2' }
];

// SOAP binding types
const SOAP_BINDINGS = [
  { value: 'document', label: 'Document' },
  { value: 'rpc', label: 'RPC' }
];

// SOAP encoding styles
const SOAP_ENCODING_STYLES = [
  { value: 'literal', label: 'Literal' },
  { value: 'encoded', label: 'Encoded' }
];

// GraphQL operation types
const GRAPHQL_OPERATION_TYPES = [
  { value: 'query', label: 'Query (Read)', icon: <Search className="h-4 w-4" /> },
  { value: 'mutation', label: 'Mutation (Write)', icon: <Edit className="h-4 w-4" /> },
  { value: 'subscription', label: 'Subscription (Real-time)', icon: <Activity className="h-4 w-4" /> }
];

// GraphQL scalar types
const GRAPHQL_SCALAR_TYPES = [
  'String', 'Int', 'Float', 'Boolean', 'ID', 'DateTime', 'JSON', 'Date', 'Time'
];

// Parameter location types with detailed options
const PARAMETER_LOCATIONS = [
  { 
    value: 'query', 
    label: 'Query Parameter', 
    icon: <Hash className="h-4 w-4" />,
    description: 'Appended to URL after ? (e.g., ?key=value)',
    example: '/users?page=1&limit=10'
  },
  { 
    value: 'path', 
    label: 'Path Parameter', 
    icon: <Link className="h-4 w-4" />,
    description: 'Part of the URL path (e.g., /users/{id})',
    example: '/users/123'
  },
  { 
    value: 'header', 
    label: 'Header Parameter', 
    icon: <Bookmark className="h-4 w-4" />,
    description: 'HTTP headers sent with request',
    example: 'X-User-ID: 123'
  },
  { 
    value: 'body', 
    label: 'Body Parameter', 
    icon: <FileText className="h-4 w-4" />,
    description: 'Parameter inside request body',
    example: 'JSON, XML, Form Data'
  }
];

// Body content types - UPDATED to include 'none'
const BODY_TYPES = [
  { value: 'none', label: 'No Body', icon: <FileText className="h-4 w-4" /> },
  { value: 'json', label: 'JSON (application/json)', icon: <FileJson className="h-4 w-4" /> },
  { value: 'xml', label: 'XML (application/xml)', icon: <Code className="h-4 w-4" /> },
  { value: 'soap', label: 'SOAP Envelope (text/xml)', icon: <Send className="h-4 w-4" /> },
  { value: 'graphql', label: 'GraphQL Query', icon: <GitMerge className="h-4 w-4" /> },
  { value: 'form-data', label: 'Form Data (multipart/form-data)', icon: <Upload className="h-4 w-4" /> },
  { value: 'urlencoded', label: 'URL Encoded (application/x-www-form-urlencoded)', icon: <Link className="h-4 w-4" /> },
  { value: 'raw', label: 'Raw Text (text/plain)', icon: <FileText className="h-4 w-4" /> },
  { value: 'binary', label: 'Binary File (application/octet-stream)', icon: <File className="h-4 w-4" /> }
];

// Authentication types (simplified to just the 4 requested)
const AUTH_TYPES = [
  { 
    value: 'none', 
    label: 'No Authentication', 
    icon: <Unlock className="h-4 w-4" />,
    description: 'Public API - no authentication required'
  },
  { 
    value: 'apiKey', 
    label: 'API Key + Secret', 
    icon: <Key className="h-4 w-4" />,
    description: 'Two-factor API key authentication',
    hasSecret: true
  },
  { 
    value: 'bearer', 
    label: 'Bearer Token (JWT)', 
    icon: <Shield className="h-4 w-4" />,
    description: 'JWT Bearer token authentication',
    hasSecret: true
  },
  { 
    value: 'basic', 
    label: 'Basic Auth', 
    icon: <Lock className="h-4 w-4" />,
    description: 'Username and password (Base64 encoded)',
    hasSecret: true
  }
];

// HTTP Methods
const HTTP_METHODS = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS'];

// SOAP Actions
const SOAP_ACTIONS = [
  'request', 'response', 'fault'
];

// Oracle Data Types
const ORACLE_DATA_TYPES = [
  'VARCHAR2', 'NUMBER', 'DATE', 'TEXT', 'TIMESTAMP', 'TIMESTAMP WITH TIME ZONE',
  'TIMESTAMP WITH LOCAL TIME ZONE', 'INTERVAL YEAR TO MONTH', 'INTERVAL DAY TO SECOND',
  'RAW', 'LONG RAW', 'CHAR', 'NCHAR', 'NVARCHAR2', 'CLOB', 'NCLOB', 'BLOB', 'BYTEA', 'JSONB', 'SYS_REFCURSOR',
  'BFILE', 'ROWID', 'UROWID', 'AUTOGENERATE',
  'FILE', 'MULTIPART_FILE', 'BINARY'
];

// API Data Types
const API_DATA_TYPES = [
  'STRING', 'INTEGER', 'NUMBER', 'BOOLEAN', 'ARRAY', 'OBJECT',
  'FILE', 'BINARY', 'NULL'
];

// Parameter Modes for procedures/functions
const PARAMETER_MODES = ['IN', 'OUT', 'IN/OUT', 'IN_OUT'];

// Required fields configuration
const REQUIRED_FIELDS = {
  apiDetails: ['apiName', 'apiCode', 'endpointPath'],
  collection: ['collectionId', 'folderId'],
  schemaConfig: ['schemaName', 'objectName'],
  authConfig: {
    apiKey: ['apiKeyHeader', 'apiSecretHeader'],
    bearer: ['jwtIssuer'],
    basic: ['basicUsername', 'basicPassword']
  }
};

// ==================== OBJECT SELECTOR MODAL ====================
const ObjectSelectorModal = ({ isOpen, onClose, onSelect, colors, authToken, databaseType }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState(null);

  // Database type configurations with direct controller references
  const databaseConfigs = {
    oracle: {
      name: 'Oracle',
      color: '#ef4444',
      bgColor: '#ef444420',
      controller: OracleSchemaController,
      processResult: (item) => {
        const objectType = (item.object_type || item.type || item.OBJECT_TYPE || '').toUpperCase();
        const objectName = item.name || item.OBJECT_NAME || item.TABLE_NAME || item.PROCEDURE_NAME || '';
        const owner = item.owner || item.OWNER;
        
        return objectName ? {
          id: `oracle_${owner || 'PUBLIC'}_${objectName}`,
          name: objectName,
          owner: owner || 'PUBLIC',
          type: objectType,
          databaseType: 'Oracle',  // Fixed: should be 'Oracle', not 'PostgreSQL'
          isSynonym: objectType === 'SYNONYM',
          targetType: item.targetType || item.TARGET_TYPE,
          targetName: item.targetName || item.TARGET_NAME,
          status: item.status || item.STATUS || 'VALID'
        } : null;
      }
    },
    postgresql: {
      name: 'PostgreSQL',
      color: '#3b82f6',
      bgColor: '#3b82f620',
      controller: PostgreSQLSchemaController,
      processResult: (item) => {
        const objectType = (item.object_type || item.type || item.OBJECT_TYPE || '').toUpperCase();
        const objectName = item.name || item.OBJECT_NAME || item.TABLE_NAME || item.PROCEDURE_NAME || '';
        const owner = item.schema || item.owner || item.OWNER || 'public';
        
        return objectName ? {
          id: `postgres_${owner}_${objectName}`,
          name: objectName,
          owner: owner,
          type: objectType,
          databaseType: 'PostgreSQL',
          isSynonym: false,
          targetType: null,
          targetName: null,
          status: item.status || 'VALID',
          schema: item.schema || owner,
          tableSpace: item.tableSpace,
          rowCount: item.rowCount
        } : null;
      }
    },
  };

  // List of databases to search (can be made configurable via props)
  const activeDatabases = ['oracle', 'postgresql']; // Add more as needed: 'mysql', 'sqlserver', 'mongodb'

  // Perform search across all active databases
  const performSearch = async () => {
    if (!authToken || searchTerm.length < 2) {
      setSearchResults([]);
      return;
    }

    setSearching(true);
    setError(null);

    try {
      const searchPromises = activeDatabases.map(async (dbKey) => {
        const config = databaseConfigs[dbKey];
        if (!config || !config.controller) {
          console.warn(`No configuration or controller found for database: ${dbKey}`);
          return { items: [], count: 0 };
        }
        
        try {
          // Use the controller directly (no dynamic import needed)
          const response = await config.controller.searchObjectsPaginated(authToken, {
            query: searchTerm,
            page: 1,
            pageSize: 50
          });
          
          const data = response?.data || {};
          const items = data.items || data.results || [];
          
          // Process results for this database
          const processedItems = items
            .map(item => config.processResult(item))
            .filter(item => item !== null);
          
          console.log(`${config.name}: Found ${processedItems.length} items`);
          
          return {
            databaseType: dbKey,
            items: processedItems,
            count: processedItems.length
          };
        } catch (err) {
          console.warn(`${config.name} search failed:`, err.message);
          return {
            databaseType: dbKey,
            items: [],
            count: 0,
            error: err.message
          };
        }
      });

      const results = await Promise.all(searchPromises);
      
      // Combine all results
      const allResults = results.flatMap(result => result.items);
      
      // Sort results by name
      allResults.sort((a, b) => a.name.localeCompare(b.name));
      
      // Log statistics
      const stats = results.map(r => {
        const config = databaseConfigs[r.databaseType];
        return `${config?.name || r.databaseType}: ${r.count}`;
      }).join(', ');
      console.log(`Search results: ${allResults.length} total (${stats})`);
      
      setSearchResults(allResults);
    } catch (err) {
      console.error('Error searching objects:', err);
      setError(err.message);
    } finally {
      setSearching(false);
    }
  };

  // Debounced search
  useEffect(() => {
    let timeoutId;

    if (searchTerm.length >= 2) {
      timeoutId = setTimeout(performSearch, 500);
    } else {
      setSearchResults([]);
    }

    return () => clearTimeout(timeoutId);
  }, [searchTerm, authToken]);

  if (!isOpen) return null;

  const getObjectIcon = (type) => {
    switch(type) {
      case 'TABLE': return <Table size={16} style={{ color: colors.objectType?.table || colors.primary }} />;
      case 'VIEW': return <FileText size={16} style={{ color: colors.objectType?.view || colors.success }} />;
      case 'MATERIALIZED VIEW': return <Layers size={16} style={{ color: colors.objectType?.view || colors.success }} />;
      case 'PROCEDURE': return <Terminal size={16} style={{ color: colors.objectType?.procedure || colors.warning }} />;
      case 'FUNCTION': return <Code size={16} style={{ color: colors.objectType?.function || colors.info }} />;
      case 'PACKAGE': return <Package size={16} style={{ color: colors.objectType?.package || colors.textSecondary }} />;
      case 'SYNONYM': return <Link size={16} style={{ color: colors.objectType?.synonym || colors.accentCyan }} />;
      case 'SEQUENCE': return <Hash size={16} style={{ color: colors.objectType?.sequence || colors.textTertiary }} />;
      case 'TRIGGER': return <Zap size={16} style={{ color: colors.objectType?.trigger || colors.error }} />;
      case 'COLLECTION': return <Database size={16} style={{ color: colors.objectType?.collection || colors.primary }} />;
      default: return <Database size={16} style={{ color: colors.textSecondary }} />;
    }
  };

  const getDatabaseBadge = (databaseType) => {
    // Find the database config by name (case-insensitive)
    const dbConfig = Object.values(databaseConfigs).find(
      config => config.name.toLowerCase() === databaseType?.toLowerCase()
    );
    
    const bgColor = dbConfig?.bgColor || '#6b728020';
    const textColor = dbConfig?.color || '#6b7280';
    
    return (
      <span className="text-xs px-2 py-0.5 rounded-full ml-2" style={{ 
        backgroundColor: bgColor,
        color: textColor
      }}>
        {databaseType}
      </span>
    );
  };

  // Get the list of database names for the placeholder
  const getDatabaseNames = () => {
    return activeDatabases.map(db => databaseConfigs[db]?.name).filter(Boolean).join(', ');
  };

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-[1600] p-4">
      <div className="rounded-xl shadow-2xl w-3xl max-w-3xl max-h-[80vh] flex flex-col" style={{ 
        backgroundColor: colors.bg,
        width: "650px",
        border: `1px solid ${colors.modalBorder || colors.border}`
      }}>
        {/* Header */}
        <div className="px-6 py-4 border-b flex items-center justify-between" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg" style={{ backgroundColor: colors.primary + '20' }}>
              <Database className="h-5 w-5" style={{ color: colors.primary }} />
            </div>
            <div>
              <h2 className="text-lg font-bold" style={{ color: colors.text }}>
                Select Database Object
              </h2>
              <p className="text-xs" style={{ color: colors.textSecondary }}>
                Search across {getDatabaseNames()} databases
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-lg transition-colors hover-lift"
            style={{ backgroundColor: colors.hover, color: colors.textSecondary }}
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Search Input */}
        <div className="p-4" style={{ borderColor: colors.border }}>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2" size={16} style={{ color: colors.textSecondary }} />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-3 rounded-lg text-sm"
              style={{ 
                backgroundColor: colors.inputBg,
                color: colors.text
              }}
              placeholder={`Search across multiple databases such as ${getDatabaseNames()}, etc...`}
              autoFocus
            />
            {searching && (
              <Loader className="absolute right-3 top-1/2 transform -translate-y-1/2 animate-spin" size={16} style={{ color: colors.primary }} />
            )}
          </div>
          {searchTerm.length < 2 && searchTerm.length > 0 && (
            <p className="text-xs mt-2" style={{ color: colors.warning }}>
              Enter at least 2 characters to search
            </p>
          )}
          {error && (
            <p className="text-xs mt-2 flex items-center gap-1" style={{ color: colors.error }}>
              <AlertCircle size={12} />
              {error}
            </p>
          )}
        </div>

        {/* Results */}
        <div className="flex-1 overflow-auto p-4">
          {searching ? (
            <div className="flex flex-col items-center justify-center py-12">
              <Loader className="animate-spin mb-4" size={32} style={{ color: colors.primary }} />
              <p className="text-sm" style={{ color: colors.text }}>
                Searching across {getDatabaseNames()} databases...
              </p>
            </div>
          ) : searchResults.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12">
              <Database size={48} style={{ color: colors.textTertiary, opacity: 0.5 }} />
              <p className="text-sm mt-4" style={{ color: colors.textSecondary }}>
                {searchTerm.length >= 2 ? 'No objects found across any database' : 'Type to search across databases'}
              </p>
            </div>
          ) : (
            <div className="space-y-2">
              {searchResults.map(obj => (
                <button
                  key={obj.id}
                  onClick={() => onSelect(obj)}
                  className="w-full p-4 rounded-lg border text-left hover:bg-opacity-50 transition-colors"
                  style={{ 
                    backgroundColor: colors.card,
                    borderColor: colors.border
                  }}
                >
                  <div className="flex items-center gap-3">
                    {getObjectIcon(obj.type)}
                    
                    <div className="flex-1">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="font-medium" style={{ color: colors.text }}>{obj.name}</span>
                        {getDatabaseBadge(obj.databaseType)}
                        <span className="text-xs px-2 py-0.5 rounded-full" style={{ 
                          backgroundColor: colors.objectType?.[obj.type?.toLowerCase()] + '20' || colors.info + '20',
                          color: colors.objectType?.[obj.type?.toLowerCase()] || colors.info
                        }}>
                          {obj.type}
                        </span>
                        {obj.isSynonym && (
                          <span className="text-xs px-2 py-0.5 rounded-full" style={{ 
                            backgroundColor: colors.info + '20',
                            color: colors.info
                          }}>
                            → {obj.targetType}: {obj.targetName}
                          </span>
                        )}
                      </div>
                      <div className="flex items-center gap-3 mt-1 text-xs" style={{ color: colors.textSecondary }}>
                        <span>{obj.owner}</span>
                        {obj.status && obj.status !== 'VALID' && (
                          <span className="px-2 py-0.5 rounded-full" style={{ 
                            backgroundColor: colors.error + '20',
                            color: colors.error
                          }}>
                            {obj.status}
                          </span>
                        )}
                      </div>
                    </div>
                    <ChevronRight size={16} style={{ color: colors.textTertiary }} />
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Footer with stats */}
        <div className="px-6 py-4 border-t" style={{ borderColor: colors.border }}>
          <div className="flex items-center justify-between">
            <div className="text-xs" style={{ color: colors.textSecondary }}>
              Found {searchResults.length} object(s)
            </div>
            <button
              onClick={onClose}
              className="px-4 py-2 border rounded-lg transition-colors hover-lift"
              style={{ 
                backgroundColor: colors.hover,
                borderColor: colors.border,
                color: colors.text
              }}
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};


// New component for the preview modal - UPDATED WITH PROTOCOL-SPECIFIC PREVIEWS
function ApiPreviewModal({ 
  isOpen, 
  onClose, 
  onConfirm,
  apiData,
  colors = {},
  theme = 'dark' 
}) {
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
    white: '#ffffff'
  };

  if (!isOpen || !apiData) return null;

  // Helper to get parameter location icon
  const getParamLocationIcon = (location) => {
    switch(location) {
      case 'query': return <Hash className="h-3 w-3" />;
      case 'path': return <Link className="h-3 w-3" />;
      case 'header': return <Bookmark className="h-3 w-3" />;
      case 'body': return <FileText className="h-3 w-3" />;
      default: return <Hash className="h-3 w-3" />;
    }
  };

  // Filter parameters to only show IN parameters (not OUT)
  const inParameters = (apiData.parameters || []).filter(p => 
    !p.paramMode || p.paramMode === 'IN'
  );

  // Filter response mappings to only show OUT parameters
  const outMappings = (apiData.responseMappings || []).filter(m => 
    (m.paramMode === 'OUT' || m.paramMode === 'IN/OUT' || m.paramMode === 'IN_OUT' || !m.paramMode)
  );

  // ============ PROTOCOL-SPECIFIC GENERATION FUNCTIONS ============
  
  // Generate SOAP Request Envelope
  const generateSoapRequestEnvelope = () => {
    const soapConfig = apiData.soapConfig || {};
    const soapVersion = soapConfig.version === '1.2' ? 'http://www.w3.org/2003/05/soap-envelope' : 'http://schemas.xmlsoap.org/soap/envelope/';
    const operationName = soapConfig.soapAction || apiData.apiCode || 'ProcessRequest';
    const namespace = soapConfig.namespace || 'http://tempuri.org/';
    const bindingStyle = soapConfig.bindingStyle || 'document';
    
    let bodyContent = '';
    
    if (bindingStyle === 'rpc') {
      // RPC style - operation name as element
      bodyContent = `<${operationName}>
${inParameters.map(p => `        <${p.key}>${p.example || ''}</${p.key}>`).join('\n')}
      </${operationName}>`;
    } else {
      // Document style - wrapper element
      bodyContent = `<${operationName} xmlns="${namespace}">
${inParameters.map(p => `        <${p.key}>${p.example || ''}</${p.key}>`).join('\n')}
      </${operationName}>`;
    }
    
    return `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="${soapVersion}"${soapConfig.version === '1.2' ? '' : ' xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"'}>
  <soap:Header>
    <!-- Optional SOAP headers -->
  </soap:Header>
  <soap:Body>
    ${bodyContent}
  </soap:Body>
</soap:Envelope>`;
  };

// Generate SOAP Response Envelope - FIXED WITH PROPER ARRAY/OBJECT HANDLING
const generateSoapResponseEnvelope = () => {
  const soapConfig = apiData.soapConfig || {};
  const operationName = soapConfig.soapAction || apiData.apiCode || 'ProcessRequest';
  const namespace = soapConfig.namespace || 'http://tempuri.org/';
  const bindingStyle = soapConfig.bindingStyle || 'document';
  
  // Use ALL outMappings (not just slice)
  const allMappings = outMappings;
  
  console.log('📊 SOAP Response - Total mappings:', allMappings.length);
  console.log('📊 SOAP Response mapping types:', allMappings.map(m => ({ field: m.apiField, type: m.apiType, example: m.example })));
  
  const buildMappingXml = (m) => {
    // Determine the value based on apiType
    let sampleValue = m.example;
    const apiType = m.apiType?.toLowerCase();
    
    if (!sampleValue || sampleValue === '') {
      // Fallback based on apiType
      switch(apiType) {
        case 'integer':
          sampleValue = '123';
          break;
        case 'number':
          sampleValue = '123.45';
          break;
        case 'boolean':
          sampleValue = 'true';
          break;
        case 'array':
          sampleValue = '';  // Empty array in XML - no content between tags
          break;
        case 'object':
          sampleValue = '';  // Empty object in XML - no content between tags
          break;
        default:
          sampleValue = 'value';
      }
    } else if (apiType === 'boolean') {
      // Ensure boolean values are 'true' or 'false' strings for XML
      sampleValue = (sampleValue === 'true' || sampleValue === true) ? 'true' : 'false';
    } else if (apiType === 'array') {
      // For arrays in SOAP, if it's empty, use empty tag; otherwise try to format
      if (sampleValue === '[]' || sampleValue === '') {
        sampleValue = '';
      } else {
        // If it looks like a JSON array, extract the first item or use as is
        try {
          const parsed = JSON.parse(sampleValue);
          if (Array.isArray(parsed) && parsed.length > 0) {
            sampleValue = String(parsed[0]);
          } else {
            sampleValue = '';
          }
        } catch {
          sampleValue = sampleValue;
        }
      }
    } else if (apiType === 'object') {
      // For objects in SOAP, use empty tag
      if (sampleValue === '{}' || sampleValue === '') {
        sampleValue = '';
      }
    }
    
    // If sampleValue is empty string, return self-closing tag
    if (sampleValue === '') {
      return `<${m.apiField}/>`;
    }
    return `<${m.apiField}>${sampleValue}</${m.apiField}>`;
  };
  
  let responseContent = '';
  
  if (bindingStyle === 'rpc') {
    responseContent = `<${operationName}Response>
      <success>true</success>
      <message>Request processed successfully</message>
      ${allMappings.map(m => `        ${buildMappingXml(m)}`).join('\n')}
    </${operationName}Response>`;
  } else {
    responseContent = `<${operationName}Response xmlns="${namespace}">
      <success>true</success>
      <message>Request processed successfully</message>
      ${allMappings.map(m => `        ${buildMappingXml(m)}`).join('\n')}
    </${operationName}Response>`;
  }
  
  // Wrap in SOAP envelope
  const soapVersion = soapConfig.version === '1.2' ? 'http://www.w3.org/2003/05/soap-envelope' : 'http://schemas.xmlsoap.org/soap/envelope/';
  return `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="${soapVersion}">
  <soap:Header/>
  <soap:Body>
    ${responseContent}
  </soap:Body>
</soap:Envelope>`;
};

// Generate GraphQL Response Example - FIXED WITH PROPER ARRAY/OBJECT HANDLING
const generateGraphQLResponseExample = () => {
  const graphqlConfigData = apiData.graphqlConfig || {};
  const operationName = graphqlConfigData.operationName || 'query';
  
  // Use ALL outMappings (not just slice)
  const allMappings = outMappings;
  
  console.log('📊 GraphQL Response - Total mappings:', allMappings.length);
  console.log('📊 GraphQL Response mapping types:', allMappings.map(m => ({ field: m.apiField, type: m.apiType, example: m.example })));
  
  const responseData = {};
  allMappings.forEach(mapping => {
    let exampleValue = mapping.example;
    const apiType = mapping.apiType?.toLowerCase();
    
    // Use example if available, otherwise generate from apiType
    if (exampleValue && exampleValue !== '') {
      if (apiType === 'integer') {
        const parsed = parseInt(exampleValue);
        responseData[mapping.apiField] = isNaN(parsed) ? 123 : parsed;
      } else if (apiType === 'number') {
        const parsed = parseFloat(exampleValue);
        responseData[mapping.apiField] = isNaN(parsed) ? 123.45 : parsed;
      } else if (apiType === 'boolean') {
        responseData[mapping.apiField] = exampleValue === 'true' || exampleValue === true;
      } else if (apiType === 'array') {
        // Handle array properly - parse JSON or use empty array
        if (exampleValue === '[]' || exampleValue === '') {
          responseData[mapping.apiField] = [];
        } else {
          try {
            responseData[mapping.apiField] = JSON.parse(exampleValue);
          } catch {
            responseData[mapping.apiField] = [];
          }
        }
      } else if (apiType === 'object') {
        // Handle object properly - parse JSON or use empty object
        if (exampleValue === '{}' || exampleValue === '') {
          responseData[mapping.apiField] = {};
        } else {
          try {
            responseData[mapping.apiField] = JSON.parse(exampleValue);
          } catch {
            responseData[mapping.apiField] = {};
          }
        }
      } else {
        responseData[mapping.apiField] = exampleValue;
      }
    } else {
      // Fallback based on apiType
      switch(apiType) {
        case 'integer':
          responseData[mapping.apiField] = 123;
          break;
        case 'number':
          responseData[mapping.apiField] = 123.45;
          break;
        case 'boolean':
          responseData[mapping.apiField] = true;
          break;
        case 'array':
          responseData[mapping.apiField] = [];
          break;
        case 'object':
          responseData[mapping.apiField] = {};
          break;
        default:
          if (mapping.format === 'date-time') {
            responseData[mapping.apiField] = '2024-01-01T00:00:00Z';
          } else if (mapping.apiField === 'id') {
            responseData[mapping.apiField] = 1;
          } else {
            responseData[mapping.apiField] = 'sample';
          }
      }
    }
  });
  
  return JSON.stringify({
    data: {
      [operationName]: responseData
    }
  }, null, 2);
};


// Generate REST JSON Response Example - FIXED WITH PROPER ARRAY/OBJECT HANDLING
const generateRestResponseExample = () => {
  const responseData = {};
  outMappings.forEach(mapping => {
    let exampleValue = mapping.example;
    const apiType = mapping.apiType?.toLowerCase();
    
    // Use example if available, otherwise generate from apiType
    if (exampleValue && exampleValue !== '') {
      if (apiType === 'integer') {
        const parsed = parseInt(exampleValue);
        responseData[mapping.apiField] = isNaN(parsed) ? 123 : parsed;
      } else if (apiType === 'number') {
        const parsed = parseFloat(exampleValue);
        responseData[mapping.apiField] = isNaN(parsed) ? 123.45 : parsed;
      } else if (apiType === 'boolean') {
        responseData[mapping.apiField] = exampleValue === 'true' || exampleValue === true;
      } else if (apiType === 'array') {
        // Handle array properly - parse JSON or use empty array
        if (exampleValue === '[]' || exampleValue === '') {
          responseData[mapping.apiField] = [];
        } else {
          try {
            responseData[mapping.apiField] = JSON.parse(exampleValue);
          } catch {
            responseData[mapping.apiField] = [];
          }
        }
      } else if (apiType === 'object') {
        // Handle object properly - parse JSON or use empty object
        if (exampleValue === '{}' || exampleValue === '') {
          responseData[mapping.apiField] = {};
        } else {
          try {
            responseData[mapping.apiField] = JSON.parse(exampleValue);
          } catch {
            responseData[mapping.apiField] = {};
          }
        }
      } else {
        responseData[mapping.apiField] = exampleValue;
      }
    } else {
      // Fallback based on apiType
      switch(apiType) {
        case 'integer':
          responseData[mapping.apiField] = 123;
          break;
        case 'number':
          responseData[mapping.apiField] = 123.45;
          break;
        case 'boolean':
          responseData[mapping.apiField] = true;
          break;
        case 'array':
          responseData[mapping.apiField] = [];
          break;
        case 'object':
          responseData[mapping.apiField] = {};
          break;
        default:
          if (mapping.format === 'date-time') {
            responseData[mapping.apiField] = '2024-01-01T00:00:00Z';
          } else if (mapping.apiField === 'id') {
            responseData[mapping.apiField] = 1;
          } else {
            responseData[mapping.apiField] = 'sample';
          }
      }
    }
  });
  
  return JSON.stringify({
    success: true,
    data: responseData,
    message: "Success"
  }, null, 2);
};


// Generate GraphQL Query Example - MODIFIED TO SHOW ALL MAPPINGS IN RESPONSE
const generateGraphQLQueryExample = () => {
  // Get config from apiData instead of undefined variable
  const graphqlConfigData = apiData.graphqlConfig || {};
  const operationType = graphqlConfigData.operationType || 'query';
  const operationName = graphqlConfigData.operationName || 'query';
  
  // Get parameters from apiData (filtered to IN params)
  const inParameters = (apiData.parameters || []).filter(p => 
    !p.paramMode || p.paramMode === 'IN'
  );
  
  // Get ALL response fields from apiData (not just slice)
  const outMappingsData = (apiData.responseMappings || []).filter(m => 
    (m.paramMode === 'OUT' || m.paramMode === 'IN/OUT' || m.paramMode === 'IN_OUT' || !m.paramMode)
  );
  
  console.log('📊 generateGraphQLQueryExample - Parameters:', inParameters.length);
  console.log('📊 generateGraphQLQueryExample - Mappings (ALL):', outMappingsData.length);
  console.log('📊 Mapping fields:', outMappingsData.map(m => m.apiField));
  
  // Separate parameters by location
  const queryParams = inParameters.filter(p => p.parameterLocation !== 'body');
  const inputParams = inParameters.filter(p => p.parameterLocation === 'body');
  
  let variableDefinitions = '';
  let variableUsages = '';
  let variablesObject = {};
  
  if (operationType === 'query' && queryParams.length > 0) {
    // Build variable definitions with proper GraphQL types
    variableDefinitions = '(' + queryParams.map(p => {
      const graphqlType = getGraphQLTypeForModal(p.oracleType, !p.required);
      return `$${p.key}: ${graphqlType}`;
    }).join(', ') + ')';
    
    // Use variables in query
    variableUsages = '(' + queryParams.map(p => `${p.key}: $${p.key}`).join(', ') + ')';
    
    // Build variables object with proper typed values
    queryParams.forEach(p => {
      let val = p.example;
      if (!val || val === '') {
        if (p.apiType === 'integer') val = 123;
        else if (p.apiType === 'number') val = 123.45;
        else if (p.apiType === 'boolean') val = true;
        else val = 'sample';
      } else {
        if (p.apiType === 'integer') {
          val = parseInt(val, 10);
          if (isNaN(val)) val = 123;
        } else if (p.apiType === 'number') {
          val = parseFloat(val);
          if (isNaN(val)) val = 123.45;
        } else if (p.apiType === 'boolean') {
          val = val === 'true' || val === '1' || val === 'yes';
        }
      }
      variablesObject[p.key] = val;
    });
  }
  
  if (operationType === 'mutation' && inputParams.length > 0) {
    variableDefinitions = `($input: ${operationName}Input!)`;
    variableUsages = '(input: $input)';
    
    const inputObject = {};
    inputParams.forEach(p => {
      let val = p.example;
      if (!val || val === '') {
        if (p.apiType === 'integer') val = 123;
        else if (p.apiType === 'number') val = 123.45;
        else if (p.apiType === 'boolean') val = true;
        else val = 'sample';
      } else {
        if (p.apiType === 'integer') {
          val = parseInt(val, 10);
          if (isNaN(val)) val = 123;
        } else if (p.apiType === 'number') {
          val = parseFloat(val);
          if (isNaN(val)) val = 123.45;
        } else if (p.apiType === 'boolean') {
          val = val === 'true' || val === '1' || val === 'yes';
        }
      }
      inputObject[p.key] = val;
    });
    variablesObject = { input: inputObject };
  }
  
  // Build the query string with ALL response fields (mappings)
  let queryString = '';
  if (operationType === 'query') {
    if (queryParams.length > 0) {
      queryString = `${operationType} ${operationName}${variableDefinitions} {\n  ${operationName}${variableUsages} {\n`;
      // Show ALL response fields
      if (outMappingsData.length > 0) {
        queryString += outMappingsData.map(m => `    ${m.apiField}`).join('\n');
        if (outMappingsData.length > 10) {
          queryString += `\n    # ... total ${outMappingsData.length} fields`;
        }
      } else {
        queryString += `    id\n    createdAt\n    updatedAt`;
      }
      queryString += `\n  }\n}`;
    } else {
      queryString = `${operationType} {\n  ${operationName} {\n`;
      if (outMappingsData.length > 0) {
        queryString += outMappingsData.map(m => `    ${m.apiField}`).join('\n');
        if (outMappingsData.length > 10) {
          queryString += `\n    # ... total ${outMappingsData.length} fields`;
        }
      } else {
        queryString += `    id\n    createdAt\n    updatedAt`;
      }
      queryString += `\n  }\n}`;
    }
  } else if (operationType === 'mutation') {
    if (inputParams.length > 0) {
      queryString = `${operationType} ${operationName}${variableDefinitions} {\n  ${operationName}${variableUsages} {\n    success\n    message\n    data {\n`;
      if (outMappingsData.length > 0) {
        queryString += outMappingsData.map(m => `      ${m.apiField}`).join('\n');
        if (outMappingsData.length > 10) {
          queryString += `\n      # ... total ${outMappingsData.length} fields`;
        }
      } else {
        queryString += `      id\n      createdAt`;
      }
      queryString += `\n    }\n  }\n}`;
    } else if (queryParams.length > 0) {
      queryString = `${operationType} ${operationName}${variableDefinitions} {\n  ${operationName}${variableUsages} {\n    success\n    message\n  }\n}`;
    } else {
      queryString = `${operationType} {\n  ${operationName} {\n    success\n    message\n  }\n}`;
    }
  }
  
  const finalRequest = JSON.stringify({
    query: queryString,
    variables: Object.keys(variablesObject).length > 0 ? variablesObject : {}
  }, null, 2);
  
  console.log('📝 Final GraphQL request with ALL mappings:', finalRequest);
  
  return finalRequest;
};


  // Add this helper function inside ApiPreviewModal component
const getGraphQLTypeForModal = (oracleType, isNullable = false) => {
  let graphqlType = 'String';
  const type = oracleType?.toUpperCase() || 'VARCHAR2';
  
  switch(type) {
    case 'NUMBER':
    case 'INTEGER':
    case 'FLOAT':
    case 'DECIMAL':
      graphqlType = 'Int';
      break;
    case 'DATE':
    case 'TIMESTAMP':
    case 'TIMESTAMP WITH TIME ZONE':
      graphqlType = 'DateTime';
      break;
    case 'BOOLEAN':
      graphqlType = 'Boolean';
      break;
    case 'JSON':
    case 'JSONB':
      graphqlType = 'JSON';
      break;
    case 'CLOB':
    case 'TEXT':
      graphqlType = 'String';
      break;
    default:
      graphqlType = 'String';
  }
  
  return isNullable ? graphqlType : `${graphqlType}!`;
};


  // Generate REST Request Body Example
  const generateRestRequestBodyExample = () => {
    const bodyParams = inParameters.filter(p => p.parameterLocation === 'body');
    if (bodyParams.length === 0) return '{}';
    
    const requestBody = {};
    bodyParams.forEach(param => {
      if (param.apiType === 'integer') {
        requestBody[param.key] = parseInt(param.example) || 123;
      } else if (param.apiType === 'boolean') {
        requestBody[param.key] = param.example === 'true' || false;
      } else {
        requestBody[param.key] = param.example || `sample_${param.key}`;
      }
    });
    
    return JSON.stringify(requestBody, null, 2);
  };

  // Get sample request based on protocol
  const getSampleRequest = () => {
    const protocol = apiData.protocolType || 'rest';
    
    switch(protocol) {
      case 'soap':
        return generateSoapRequestEnvelope();
      case 'graphql':
        return generateGraphQLQueryExample();
      default:
        return generateRestRequestBodyExample();
    }
  };

  // Get sample response based on protocol
  const getSampleResponse = () => {
    const protocol = apiData.protocolType || 'rest';
    
    switch(protocol) {
      case 'soap':
        return generateSoapResponseEnvelope();
      case 'graphql':
        return generateGraphQLResponseExample();
      default:
        return generateRestResponseExample();
    }
  };

  // Get content type label
  const getContentType = () => {
    const protocol = apiData.protocolType || 'rest';
    switch(protocol) {
      case 'soap':
        return 'XML (text/xml)';
      case 'graphql':
        return 'JSON (application/json)';
      default:
        return apiData.requestBody?.bodyType === 'json' ? 'JSON (application/json)' : 
               apiData.requestBody?.bodyType === 'xml' ? 'XML (application/xml)' : 
               'JSON (application/json)';
    }
  };

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-[1560] p-4" style={{ zIndex: 1560 }}>
      <div className="rounded-xl shadow-2xl w-full max-w-5xl max-h-[90vh] overflow-hidden flex flex-col" style={{ 
        backgroundColor: themeColors.bg,
        border: `1px solid ${themeColors.modalBorder}`
      }}>
        {/* Header */}
        <div className="px-6 py-4 border-b flex items-center justify-between" style={{ 
          borderColor: themeColors.border,
          backgroundColor: themeColors.card
        }}>
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.warning + '20' }}>
              <Eye className="h-6 w-6" style={{ color: themeColors.warning }} />
            </div>
            <div>
              <h2 className="text-lg font-bold" style={{ color: themeColors.text }}>
                {apiData.isEditing ? 'API Update Preview' : 'API Generation Preview'}
              </h2>
              <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                {apiData.isEditing ? 'Review your API changes before updating' : 'Review your API configuration before generation'}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-lg transition-colors hover-lift"
            style={{ backgroundColor: themeColors.hover, color: themeColors.textSecondary }}
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          <div className="space-y-6">
            {/* Protocol Badge */}
            <div className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium" style={{ 
              backgroundColor: apiData.protocolType === 'soap' ? themeColors.info + '20' : 
                             apiData.protocolType === 'graphql' ? themeColors.success + '20' : 
                             themeColors.primary + '20',
              color: apiData.protocolType === 'soap' ? themeColors.info : 
                    apiData.protocolType === 'graphql' ? themeColors.success : 
                    themeColors.primary
            }}>
              {apiData.protocolType === 'soap' && <Send className="h-3 w-3 mr-1" />}
              {apiData.protocolType === 'graphql' && <GitMerge className="h-3 w-3 mr-1" />}
              {apiData.protocolType === 'rest' && <Globe className="h-3 w-3 mr-1" />}
              {apiData.protocolType === 'soap' ? 'SOAP Web Service' : 
               apiData.protocolType === 'graphql' ? 'GraphQL API' : 'REST API'}
            </div>

            {/* API Summary */}
            <div className="p-4 rounded-lg border" style={{ 
              borderColor: themeColors.info + '40',
              backgroundColor: themeColors.info + '10'
            }}>
              <div className="flex items-start justify-between">
                <div className="space-y-2">
                  <h3 className="font-semibold flex items-center gap-2" style={{ color: themeColors.info }}>
                    <Zap className="h-5 w-5" />
                    {apiData.apiName}
                  </h3>
                  <div className="grid grid-cols-2 gap-4 text-xs">
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>API Code:</span>
                      <span className="ml-2 font-medium font-mono" style={{ color: themeColors.text }}>
                        {apiData.apiCode}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Version:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {apiData.version}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Status:</span>
                      <span className="ml-2 px-2 py-1 rounded text-xs font-medium" style={{ 
                        backgroundColor: apiData.status === 'ACTIVE' ? themeColors.success + '30' : 
                                       apiData.status === 'DRAFT' ? themeColors.warning + '30' : 
                                       themeColors.error + '30',
                        color: apiData.status === 'DRAFT' ? themeColors.warning : 
                               apiData.status === 'ACTIVE' ? themeColors.success : 
                               themeColors.error
                      }}>
                        {apiData.status}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Protocol:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {apiData.protocolType?.toUpperCase()}
                      </span>
                    </div>
                  </div>
                </div>
                <div className="text-right">
                  <div className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium" style={{ 
                    backgroundColor: themeColors.info + '20',
                    color: themeColors.info
                  }}>
                    {apiData.protocolType === 'soap' ? <Send className="h-4 w-4 mr-1" /> :
                     apiData.protocolType === 'graphql' ? <GitMerge className="h-4 w-4 mr-1" /> :
                     <Globe className="h-4 w-4 mr-1" />}
                    Endpoint
                  </div>
                  <div className="mt-2 font-mono text-xs" style={{ color: themeColors.text }}>
                    {apiData.protocolType === 'soap' ? `SOAP ${apiData.soapConfig?.version || '1.1'}` :
                     apiData.protocolType === 'graphql' ? 'GraphQL' :
                     `${apiData.httpMethod} ${apiData.basePath}${apiData.endpointPath}`}
                  </div>
                </div>
              </div>
            </div>

            {/* Collection & Folder Info */}
            {apiData.collectionInfo && (
              <div className="p-4 rounded-lg border" style={{ 
                borderColor: themeColors.success + '40',
                backgroundColor: themeColors.success + '10'
              }}>
                <h4 className="font-semibold flex items-center gap-2 mb-3" style={{ color: themeColors.success }}>
                  <Layers className="h-5 w-5" />
                  API Organization
                </h4>
                <div className="grid grid-cols-2 gap-4 text-xs">
                  <div>
                    <span style={{ color: themeColors.textSecondary }}>Collection:</span>
                    <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                      {apiData.collectionInfo.collectionName}
                    </span>
                  </div>
                  <div>
                    <span style={{ color: themeColors.textSecondary }}>Folder:</span>
                    <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                      {apiData.collectionInfo.folderName}
                    </span>
                  </div>
                </div>
              </div>
            )}

            {/* Source Object - Only show if it's a database object */}
            {apiData.schemaConfig && apiData.schemaConfig.objectName && (
              <div className="space-y-4">
                <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                  <Database className="h-5 w-5" />
                  Source Object
                </h4>
                <div className="p-4 rounded-lg border" style={{ 
                  borderColor: themeColors.border,
                  backgroundColor: themeColors.card
                }}>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs">
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Schema:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {apiData.schemaConfig.schemaName}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Object:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {apiData.schemaConfig.objectName}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Type:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {apiData.schemaConfig.objectType}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Operation:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {apiData.schemaConfig.operation}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* SOAP Specific Info */}
            {apiData.protocolType === 'soap' && apiData.soapConfig && (
              <div className="space-y-4">
                <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                  <Send className="h-5 w-5" />
                  SOAP Configuration
                </h4>
                <div className="p-4 rounded-lg border" style={{ 
                  borderColor: themeColors.info + '40',
                  backgroundColor: themeColors.info + '10'
                }}>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs">
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>SOAP Version:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {apiData.soapConfig.version}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Binding Style:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {apiData.soapConfig.bindingStyle}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Encoding Style:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {apiData.soapConfig.encodingStyle}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>SOAP Action:</span>
                      <span className="ml-2 font-medium font-mono" style={{ color: themeColors.text }}>
                        {apiData.soapConfig.soapAction}
                      </span>
                    </div>
                  </div>
                  {apiData.soapConfig.wsdlUrl && (
                    <div className="mt-3 pt-3 border-t" style={{ borderColor: themeColors.border }}>
                      <span style={{ color: themeColors.textSecondary }}>WSDL URL:</span>
                      <span className="ml-2 font-mono text-xs" style={{ color: themeColors.info }}>
                        {apiData.soapConfig.wsdlUrl}
                      </span>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* GraphQL Specific Info */}
            {apiData.protocolType === 'graphql' && apiData.graphqlConfig && (
              <div className="space-y-4">
                <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                  <GitMerge className="h-5 w-5" />
                  GraphQL Configuration
                </h4>
                <div className="p-4 rounded-lg border" style={{ 
                  borderColor: themeColors.success + '40',
                  backgroundColor: themeColors.success + '10'
                }}>
                  <div className="grid grid-cols-2 gap-4 text-xs">
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Operation Type:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {apiData.graphqlConfig.operationType}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Operation Name:</span>
                      <span className="ml-2 font-medium font-mono" style={{ color: themeColors.text }}>
                        {apiData.graphqlConfig.operationName}
                      </span>
                    </div>
                  </div>
                  {apiData.graphqlConfig.schema && (
                    <div className="mt-3 pt-3 border-t" style={{ borderColor: themeColors.border }}>
                      <span style={{ color: themeColors.textSecondary }}>GraphQL Schema Preview:</span>
                      <pre className="mt-2 p-2 rounded text-xs font-mono overflow-x-auto" style={{ 
                        backgroundColor: themeColors.hover,
                        maxHeight: '100px'
                      }}>
                        {apiData.graphqlConfig.schema.substring(0, 200)}...
                      </pre>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Parameters Summary */}
            <div className="space-y-4">
              <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                <Hash className="h-5 w-5" />
                Input Parameters ({inParameters.length})
              </h4>
              <div className="p-4 rounded-lg border" style={{ 
                borderColor: themeColors.border,
                backgroundColor: themeColors.card
              }}>
                {inParameters.length > 0 ? (
                  <div className="space-y-3">
                    {['query', 'path', 'header', 'body'].map(location => {
                      const locationParams = inParameters.filter(p => p.parameterLocation === location);
                      if (locationParams.length === 0) return null;
                      
                      return (
                        <div key={location} className="space-y-2">
                          <h5 className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.info }}>
                            {getParamLocationIcon(location)}
                            {location.charAt(0).toUpperCase() + location.slice(1)} Parameters ({locationParams.length})
                          </h5>
                          <div className="grid grid-cols-1 gap-2 pl-4">
                            {locationParams.map((param, index) => (
                              <div key={index} className="flex items-center justify-between text-xs">
                                <div>
                                  <span className="font-medium" style={{ color: themeColors.text }}>
                                    {param.key}
                                  </span>
                                  <span className="ml-2 text-xs px-2 py-0.5 rounded" style={{ 
                                    backgroundColor: themeColors.info + '20',
                                    color: themeColors.info
                                  }}>
                                    {param.oracleType}
                                  </span>
                                  {param.isPrimaryKey && (
                                    <span className="ml-2 text-xs px-2 py-0.5 rounded" style={{ 
                                      backgroundColor: themeColors.warning + '20',
                                      color: themeColors.warning
                                    }}>
                                      PK
                                    </span>
                                  )}
                                </div>
                                <div style={{ color: themeColors.textSecondary }}>
                                  {param.required ? 'Required' : 'Optional'} • {param.apiType}
                                  {param.defaultValue && <span> • Default: {param.defaultValue}</span>}
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                    No input parameters defined
                  </p>
                )}
              </div>
            </div>

            {/* Response Fields */}
            <div className="space-y-4">
              <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                <Map className="h-5 w-5" />
                Response Fields ({outMappings.length})
              </h4>
              <div className="p-4 rounded-lg border" style={{ 
                borderColor: themeColors.border,
                backgroundColor: themeColors.card
              }}>
                {outMappings.length > 0 ? (
                  <div className="grid grid-cols-2 gap-4 text-xs mb-4">
                    {outMappings.slice(0, 30).map((mapping, index) => (
                      <div key={index}>
                        <div className="font-medium" style={{ color: themeColors.text }}>
                          {mapping.apiField}
                        </div>
                        <div className="text-xs" style={{ color: themeColors.textSecondary }}>
                          {mapping.oracleType}
                          {mapping.nullable && <span className="ml-1">(nullable)</span>}
                        </div>
                      </div>
                    ))}
                    {outMappings.length > 30 && (
                      <div className="col-span-2 text-center pt-2">
                        <span className="text-xs" style={{ color: themeColors.textSecondary }}>
                          + {outMappings.length - 30} more fields
                        </span>
                      </div>
                    )}
                  </div>
                ) : (
                  <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                    No response fields defined
                  </p>
                )}
              </div>
            </div>

            {/* ============ PROTOCOL-SPECIFIC SAMPLE REQUEST & RESPONSE ============ */}
            <div className="space-y-4">
              <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                <FileText className="h-5 w-5" />
                Sample {apiData.protocolType === 'soap' ? 'SOAP' : apiData.protocolType === 'graphql' ? 'GraphQL' : 'REST'} Request
              </h4>
              <div className="border rounded-lg overflow-hidden" style={{ 
                borderColor: themeColors.border,
                backgroundColor: themeColors.card
              }}>
                <div className="px-4 py-2 border-b flex items-center justify-between" style={{ borderColor: themeColors.border }}>
                  <span className="text-xs font-medium" style={{ color: themeColors.text }}>
                    {apiData.protocolType === 'soap' ? 'SOAP Envelope' : 
                     apiData.protocolType === 'graphql' ? 'GraphQL Query' : 
                     apiData.httpMethod === 'GET' ? 'Query Parameters' : 'Request Body'} ({getContentType()})
                  </span>
                  <button
                    onClick={() => navigator.clipboard.writeText(getSampleRequest())}
                    className="p-1 rounded transition-colors hover-lift"
                    style={{ backgroundColor: themeColors.hover, color: themeColors.textSecondary }}
                    title="Copy to clipboard"
                  >
                    <Copy className="h-3 w-3" />
                  </button>
                </div>
                <pre className="w-full max-h-64 px-4 py-3 overflow-auto text-xs font-mono" style={{ 
                  backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                  color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                }}>
                  {getSampleRequest()}
                </pre>
              </div>
            </div>

            <div className="space-y-4">
              <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                <FileText className="h-5 w-5" />
                Sample {apiData.protocolType === 'soap' ? 'SOAP' : apiData.protocolType === 'graphql' ? 'GraphQL' : 'REST'} Response
              </h4>
              <div className="border rounded-lg overflow-hidden" style={{ 
                borderColor: themeColors.border,
                backgroundColor: themeColors.card
              }}>
                <div className="px-4 py-2 border-b flex items-center justify-between" style={{ borderColor: themeColors.border }}>
                  <span className="text-xs font-medium" style={{ color: themeColors.text }}>
                    {apiData.protocolType === 'soap' ? 'SOAP Response Envelope' : 
                     apiData.protocolType === 'graphql' ? 'GraphQL Response' : 
                     'Success Response (200 OK)'} ({apiData.protocolType === 'soap' ? 'XML' : 'JSON'})
                  </span>
                  <button
                    onClick={() => navigator.clipboard.writeText(getSampleResponse())}
                    className="p-1 rounded transition-colors hover-lift"
                    style={{ backgroundColor: themeColors.hover, color: themeColors.textSecondary }}
                    title="Copy to clipboard"
                  >
                    <Copy className="h-3 w-3" />
                  </button>
                </div>
                <pre className="w-full max-h-64 px-4 py-3 overflow-auto text-xs font-mono" style={{ 
                  backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                  color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                }}>
                  {getSampleResponse()}
                </pre>
              </div>
            </div>

            {/* Authentication Summary */}
            <div className="space-y-4">
              <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                <Lock className="h-5 w-5" />
                Authentication
              </h4>
              <div className="p-4 rounded-lg border" style={{ 
                borderColor: themeColors.border,
                backgroundColor: themeColors.card
              }}>
                <div className="flex items-center gap-3">
                  {apiData.authConfig?.authType === 'none' && (
                    <>
                      <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.warning + '20' }}>
                        <Unlock className="h-5 w-5" style={{ color: themeColors.warning }} />
                      </div>
                      <div>
                        <div className="font-medium text-sm" style={{ color: themeColors.warning }}>
                          No Authentication (Public API)
                        </div>
                        <div className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>
                          This API will be publicly accessible
                        </div>
                      </div>
                    </>
                  )}
                  
                  {apiData.authConfig?.authType === 'apiKey' && (
                    <>
                      <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.info + '20' }}>
                        <Key className="h-5 w-5" style={{ color: themeColors.info }} />
                      </div>
                      <div>
                        <div className="font-medium text-sm" style={{ color: themeColors.info }}>
                          API Key + Secret
                        </div>
                        <div className="text-xs mt-1 grid grid-cols-2 gap-2">
                          <div>
                            <span style={{ color: themeColors.textSecondary }}>Key Header:</span>
                            <span className="ml-1 font-mono" style={{ color: themeColors.text }}>
                              {apiData.authConfig.apiKeyHeader || 'X-API-Key'}
                            </span>
                          </div>
                          <div>
                            <span style={{ color: themeColors.textSecondary }}>Secret Header:</span>
                            <span className="ml-1 font-mono" style={{color:themeColors.text }}>
                              {apiData.authConfig.apiSecretHeader || 'X-API-Secret'}
                            </span>
                          </div>
                        </div>
                      </div>
                    </>
                  )}
                  
                  {apiData.authConfig?.authType === 'bearer' && (
                    <>
                      <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.info + '20' }}>
                        <Shield className="h-5 w-5" style={{ color: themeColors.info }} />
                      </div>
                      <div>
                        <div className="font-medium text-sm" style={{ color: themeColors.info }}>
                          Bearer Token (JWT)
                        </div>
                        <div className="text-xs mt-1">
                          <span style={{ color: themeColors.textSecondary }}>Header:</span>
                          <span className="ml-1 font-mono" style={{ color: themeColors.text }}>
                            Authorization: Bearer {'{token}'}
                          </span>
                        </div>
                      </div>
                    </>
                  )}
                  
                  {apiData.authConfig?.authType === 'basic' && (
                    <>
                      <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.info + '20' }}>
                        <Lock className="h-5 w-5" style={{ color: themeColors.info }} />
                      </div>
                      <div>
                        <div className="font-medium text-sm" style={{ color: themeColors.info }}>
                          Basic Authentication
                        </div>
                        <div className="text-xs mt-1">
                          <span style={{ color: themeColors.textSecondary }}>Header:</span>
                          <span className="ml-1 font-mono" style={{ color: themeColors.text }}>
                            Authorization: Basic base64(username:password)
                          </span>
                        </div>
                      </div>
                    </>
                  )}
                </div>
              </div>
            </div>

            {/* Files to be Generated */}
            <div className="space-y-4">
              <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                <Layers className="h-5 w-5" />
                Files to be Generated
              </h4>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {apiData.protocolType === 'soap' ? (
                  <>
                    <div className="p-3 rounded-lg border" style={{ 
                      borderColor: themeColors.border,
                      backgroundColor: themeColors.card
                    }}>
                      <div className="flex items-center gap-3">
                        <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.info + '20' }}>
                          <Code className="h-4 w-4" style={{ color: themeColors.info }} />
                        </div>
                        <div>
                          <h5 className="font-medium text-xs" style={{ color: themeColors.text }}>SOAP Envelope</h5>
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>XML Request/Response</p>
                        </div>
                      </div>
                    </div>
                    <div className="p-3 rounded-lg border" style={{ 
                      borderColor: themeColors.border,
                      backgroundColor: themeColors.card
                    }}>
                      <div className="flex items-center gap-3">
                        <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.success + '20' }}>
                          <FileCode className="h-4 w-4" style={{ color: themeColors.success }} />
                        </div>
                        <div>
                          <h5 className="font-medium text-xs" style={{ color: themeColors.text }}>WSDL File</h5>
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>Service Definition</p>
                        </div>
                      </div>
                    </div>
                    <div className="p-3 rounded-lg border" style={{ 
                      borderColor: themeColors.border,
                      backgroundColor: themeColors.card
                    }}>
                      <div className="flex items-center gap-3">
                        <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.warning + '20' }}>
                          <Database className="h-4 w-4" style={{ color: themeColors.warning }} />
                        </div>
                        <div>
                          <h5 className="font-medium text-xs" style={{ color: themeColors.text }}>Postman Collection</h5>
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>API Testing</p>
                        </div>
                      </div>
                    </div>
                  </>
                ) : apiData.protocolType === 'graphql' ? (
                  <>
                    <div className="p-3 rounded-lg border" style={{ 
                      borderColor: themeColors.border,
                      backgroundColor: themeColors.card
                    }}>
                      <div className="flex items-center gap-3">
                        <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.info + '20' }}>
                          <GitMerge className="h-4 w-4" style={{ color: themeColors.info }} />
                        </div>
                        <div>
                          <h5 className="font-medium text-xs" style={{ color: themeColors.text }}>GraphQL Schema</h5>
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>Type Definitions</p>
                        </div>
                      </div>
                    </div>
                    <div className="p-3 rounded-lg border" style={{ 
                      borderColor: themeColors.border,
                      backgroundColor: themeColors.card
                    }}>
                      <div className="flex items-center gap-3">
                        <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.success + '20' }}>
                          <Code className="h-4 w-4" style={{ color: themeColors.success }} />
                        </div>
                        <div>
                          <h5 className="font-medium text-xs" style={{ color: themeColors.text }}>Resolvers</h5>
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>Implementation</p>
                        </div>
                      </div>
                    </div>
                    <div className="p-3 rounded-lg border" style={{ 
                      borderColor: themeColors.border,
                      backgroundColor: themeColors.card
                    }}>
                      <div className="flex items-center gap-3">
                        <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.warning + '20' }}>
                          <Database className="h-4 w-4" style={{ color: themeColors.warning }} />
                        </div>
                        <div>
                          <h5 className="font-medium text-xs" style={{ color: themeColors.text }}>Postman Collection</h5>
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>API Testing</p>
                        </div>
                      </div>
                    </div>
                  </>
                ) : (
                  <>
                    <div className="p-3 rounded-lg border" style={{ 
                      borderColor: themeColors.border,
                      backgroundColor: themeColors.card
                    }}>
                      <div className="flex items-center gap-3">
                        <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.info + '20' }}>
                          <Code className="h-4 w-4" style={{ color: themeColors.info }} />
                        </div>
                        <div>
                          <h5 className="font-medium text-xs" style={{ color: themeColors.text }}>PL/SQL Package</h5>
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>Oracle Database</p>
                        </div>
                      </div>
                    </div>
                    <div className="p-3 rounded-lg border" style={{ 
                      borderColor: themeColors.border,
                      backgroundColor: themeColors.card
                    }}>
                      <div className="flex items-center gap-3">
                        <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.success + '20' }}>
                          <FileJson className="h-4 w-4" style={{ color: themeColors.success }} />
                        </div>
                        <div>
                          <h5 className="font-medium text-xs" style={{ color: themeColors.text }}>OpenAPI Spec</h5>
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>API Documentation</p>
                        </div>
                      </div>
                    </div>
                    <div className="p-3 rounded-lg border" style={{ 
                      borderColor: themeColors.border,
                      backgroundColor: themeColors.card
                    }}>
                      <div className="flex items-center gap-3">
                        <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.warning + '20' }}>
                          <Database className="h-4 w-4" style={{ color: themeColors.warning }} />
                        </div>
                        <div>
                          <h5 className="font-medium text-xs" style={{ color: themeColors.text }}>Postman Collection</h5>
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>API Testing</p>
                        </div>
                      </div>
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t flex items-center justify-between" style={{ 
          borderColor: themeColors.border,
          backgroundColor: themeColors.card
        }}>
          <div className="text-xs" style={{ color: themeColors.textSecondary }}>
            {apiData.isEditing ? 'Review all changes before updating the API' : 'Review all details carefully before generating the API'}
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
              onClick={onConfirm}
              className="px-4 py-2 rounded-lg flex items-center gap-2 transition-colors hover-lift"
              style={{ backgroundColor: themeColors.success, color: themeColors.white }}
            >
              <Sparkles className="h-4 w-4" />
              {apiData.isEditing ? 'Confirm & Update API' : 'Confirm & Generate API'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

// New component for the loading modal
function ApiLoadingModal({ 
  isOpen, 
  colors = {},
  theme = 'dark' 
}) {
  const themeColors = colors || {
    bg: theme === 'dark' ? 'rgb(1 14 35)' : '#f8fafc',
    text: theme === 'dark' ? '#E8ECF1' : '#1e293b',
    textSecondary: theme === 'dark' ? 'rgb(168 178 192)' : '#64748b',
    modalBg: theme === 'dark' ? '#010e23' : '#ffffff',
    modalBorder: theme === 'dark' ? 'rgb(61 73 92)' : '#e2e8f0',
    info: theme === 'dark' ? 'rgb(59 130 246)' : '#3b82f6',
    white: '#ffffff'
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-[1570]" style={{ zIndex: 1570 }}>
      <div className="rounded-xl shadow-2xl w-full max-w-md p-6" style={{ 
        backgroundColor: themeColors.bg,
        border: `1px solid ${themeColors.modalBorder}`
      }}>
        <div className="text-center space-y-4">
          <div className="inline-flex">
            <Loader className="h-12 w-12 animate-spin" style={{ color: themeColors.info }} />
          </div>
          <div>
            <h3 className="text-lg font-bold mb-2" style={{ color: themeColors.text }}>
              Processing Your Request
            </h3>
            <p className="text-xs" style={{ color: themeColors.textSecondary }}>
              Please wait while we process your request...
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

// Updated confirmation modal that shows the actual API generation result
function ApiConfirmationModal({ 
  isOpen, 
  onClose, 
  apiData,
  apiResponse,
  colors = {},
  theme = 'dark' 
}) {
  const [showLoader, setShowLoader] = useState(true);
  const [showSuccess, setShowSuccess] = useState(false);
  const [error, setError] = useState(null);

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
    white: '#ffffff'
  };

  useEffect(() => {
    if (isOpen && apiResponse) {
      setShowLoader(true);
      setShowSuccess(false);
      setError(null);
      
      // Check if the API generation was successful
      const timer = setTimeout(() => {
        setShowLoader(false);
        if (apiResponse.responseCode >= 200 && apiResponse.responseCode < 300) {
          setShowSuccess(true);
        } else {
          setError(apiResponse.message || 'Failed to process request');
        }
      }, 2000);
      
      return () => clearTimeout(timer);
    }
  }, [isOpen, apiResponse]);

  if (!isOpen || !apiData) return null;

  // Show loader
  if (showLoader) {
    return (
      <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center [z-1550]" style={{ zIndex: 1550 }}>
        <div className="rounded-xl shadow-2xl w-full max-w-md p-6" style={{ 
          backgroundColor: themeColors.bg,
          border: `1px solid ${themeColors.modalBorder}`
        }}>
          <div className="text-center space-y-4">
            <div className="inline-flex">
              <Loader className="h-12 w-12 animate-spin" style={{ color: themeColors.success }} />
            </div>
            <div>
              <h3 className="text-lg font-bold mb-2" style={{ color: themeColors.text }}>
                {apiData.isEditing ? 'Updating API...' : 'Finalizing API Generation...'}
              </h3>
              <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                {apiData.isEditing ? 'Processing API update...' : 'Processing API response from server...'}
              </p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Show error
  if (error) {
    return (
      <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center [z-1565] p-4" style={{ zIndex: 1565 }}>
        <div className="rounded-xl shadow-2xl w-full max-w-md p-6" style={{ 
          backgroundColor: themeColors.bg,
          border: `1px solid ${themeColors.modalBorder}`
        }}>
          <div className="text-center space-y-4">
            <div className="inline-flex p-3 rounded-full" style={{ backgroundColor: themeColors.error + '20' }}>
              <XCircle className="h-12 w-12" style={{ color: themeColors.error }} />
            </div>
            <div>
              <h3 className="text-lg font-bold mb-2" style={{ color: themeColors.text }}>
                {apiData.isEditing ? 'API Update Failed' : 'API Generation Failed'}
              </h3>
              <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                {error}
              </p>
            </div>
            <button
              onClick={onClose}
              className="px-4 py-2 rounded-lg mt-4"
              style={{ backgroundColor: themeColors.info, color: themeColors.white }}
            >
              Close
            </button>
          </div>
        </div>
      </div>
    );
  }

  // Show success with actual API response data
  if (showSuccess) {
    const transformedData = apiResponse.data || {};
    
    const formatDate = (dateString) => {
      if (!dateString) return 'N/A';
      return new Date(dateString).toLocaleString();
    };

    const copyApiDetails = () => {
      navigator.clipboard.writeText(JSON.stringify(apiResponse, null, 2));
    };

    const downloadGeneratedFiles = () => {
      if (apiResponse.data?.generatedFiles) {
        const files = apiResponse.data.generatedFiles;
        
        if (files.plsql) {
          downloadGeneratedFile(
            files.plsql, 
            `${apiData.apiCode}_package.sql`, 
            'text/plain'
          );
        }
        
        if (files.openapi) {
          downloadGeneratedFile(
            files.openapi, 
            `${apiData.apiCode}_openapi.json`, 
            'application/json'
          );
        }
        
        if (files.postman) {
          downloadGeneratedFile(
            files.postman, 
            `${apiData.apiCode}_postman.json`, 
            'application/json'
          );
        }
        
        if (files.wsdl) {
          downloadGeneratedFile(
            files.wsdl, 
            `${apiData.apiCode}_service.wsdl`, 
            'application/xml'
          );
        }
        
        if (files.graphqlSchema) {
          downloadGeneratedFile(
            files.graphqlSchema, 
            `${apiData.apiCode}_schema.graphql`, 
            'text/plain'
          );
        }
      } else {
        // Fallback to JSON config
        const blob = new Blob([JSON.stringify(apiResponse, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${apiData.apiCode || 'api'}_response.json`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      }
    };

    return (
      <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-[1565] p-4" style={{ zIndex: 1565 }}>
        <div className="rounded-xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col" style={{ 
          backgroundColor: themeColors.bg,
          border: `1px solid ${themeColors.modalBorder}`
        }}>
          {/* Header */}
          <div className="px-6 py-4 border-b flex items-center justify-between" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.card
          }}>
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.success + '20' }}>
                <CheckCircle className="h-6 w-6" style={{ color: themeColors.success }} />
              </div>
              <div>
                <h2 className="text-lg font-bold" style={{ color: themeColors.text }}>
                  {apiData.isEditing ? 'API Updated Successfully!' : 'API Generated Successfully!'}
                </h2>
                <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                  Request ID: {apiResponse.requestId}
                </p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-2 rounded-lg transition-colors hover-lift"
              style={{ backgroundColor: themeColors.hover, color: themeColors.textSecondary }}
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Content */}
          <div className="flex-1 overflow-y-auto p-6">
            <div className="space-y-6">
              {/* Protocol Badge */}
              <div className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium" style={{ 
                backgroundColor: apiData.protocolType === 'soap' ? themeColors.info + '20' : 
                               apiData.protocolType === 'graphql' ? themeColors.success + '20' : 
                               themeColors.primary + '20',
                color: apiData.protocolType === 'soap' ? themeColors.info : 
                      apiData.protocolType === 'graphql' ? themeColors.success : 
                      themeColors.primary
              }}>
                {apiData.protocolType === 'soap' && <Send className="h-3 w-3 mr-1" />}
                {apiData.protocolType === 'graphql' && <GitMerge className="h-3 w-3 mr-1" />}
                {apiData.protocolType === 'rest' && <Globe className="h-3 w-3 mr-1" />}
                {apiData.protocolType === 'soap' ? 'SOAP Web Service' : 
                 apiData.protocolType === 'graphql' ? 'GraphQL API' : 'REST API'}
              </div>

              {/* API Summary */}
              <div className="p-4 rounded-lg border" style={{ 
                borderColor: themeColors.success + '40',
                backgroundColor: themeColors.success + '10'
              }}>
                <div className="flex items-start justify-between">
                  <div className="space-y-2">
                    <h3 className="font-semibold flex items-center gap-2" style={{ color: themeColors.success }}>
                      <Sparkles className="h-5 w-5" />
                      {transformedData.apiName || apiData.apiName}
                    </h3>
                    <div className="grid grid-cols-2 gap-4 text-xs">
                      <div>
                        <span style={{ color: themeColors.textSecondary }}>API Code:</span>
                        <span className="ml-2 font-medium font-mono" style={{ color: themeColors.text }}>
                          {transformedData.apiCode || apiData.apiCode}
                        </span>
                      </div>
                      <div>
                        <span style={{ color: themeColors.textSecondary }}>Version:</span>
                        <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                          {transformedData.version || apiData.version}
                        </span>
                      </div>
                      <div>
                        <span style={{ color: themeColors.textSecondary }}>Status:</span>
                        <span className={`ml-2 px-2 py-1 rounded text-xs font-medium`} style={{ 
                          backgroundColor: (transformedData.status || apiData.status) === 'ACTIVE' ? themeColors.success + '30' : 
                                         (transformedData.status || apiData.status) === 'DRAFT' ? themeColors.warning + '30' : 
                                         themeColors.error + '30',
                          color: (transformedData.status || apiData.status) === 'DRAFT' ? themeColors.warning : 
                                (transformedData.status || apiData.status) === 'ACTIVE' ? themeColors.success : 
                                themeColors.error
                        }}>
                          {transformedData.status || apiData.status || 'DRAFT'}
                        </span>
                      </div>
                      <div>
                        <span style={{ color: themeColors.textSecondary }}>Created:</span>
                        <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                          {formatDate(transformedData.createdAt)}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium" style={{ 
                      backgroundColor: themeColors.info + '20',
                      color: themeColors.info
                    }}>
                      {apiData.protocolType === 'soap' ? <Send className="h-4 w-4 mr-1" /> :
                       apiData.protocolType === 'graphql' ? <GitMerge className="h-4 w-4 mr-1" /> :
                       <Globe className="h-4 w-4 mr-1" />}
                      Endpoint
                    </div>
                    <div className="mt-2 font-mono text-xs" style={{ color: themeColors.text }}>
                      {apiData.protocolType === 'soap' ? `SOAP ${apiData.soapVersion || '1.1'}` :
                       apiData.protocolType === 'graphql' ? 'GraphQL' :
                       `${transformedData.httpMethod || apiData.httpMethod} ${transformedData.fullEndpoint || `${apiData.basePath}${apiData.endpointPath}`}`}
                    </div>
                  </div>
                </div>
              </div>

              {/* Collection Info */}
              {(transformedData.collectionInfo || apiData.collectionInfo) && (
                <div className="p-4 rounded-lg border" style={{ 
                  borderColor: themeColors.success + '40',
                  backgroundColor: themeColors.success + '10'
                }}>
                  <h4 className="font-semibold flex items-center gap-2 mb-3" style={{ color: themeColors.success }}>
                    <Layers className="h-5 w-5" />
                    API Organization
                  </h4>
                  <div className="grid grid-cols-2 gap-4 text-xs">
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Collection:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {transformedData.collectionInfo?.collectionName || apiData.collectionInfo?.collectionName}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Folder:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {transformedData.collectionInfo?.folderName || apiData.collectionInfo?.folderName}
                      </span>
                    </div>
                  </div>
                </div>
              )}

              {/* Generated Files */}
              {transformedData.generatedFiles && (
                <div className="space-y-4">
                  <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                    <Layers className="h-5 w-5" />
                    Generated Files
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    {transformedData.generatedFiles.plsql && (
                      <div className="p-4 rounded-lg border" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <div className="flex items-center gap-3 mb-3">
                          <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.info + '20' }}>
                            <Code className="h-5 w-5" style={{ color: themeColors.info }} />
                          </div>
                          <div>
                            <h5 className="font-medium" style={{ color: themeColors.text }}>PL/SQL Package</h5>
                            <p className="text-xs" style={{ color: themeColors.textSecondary }}>Oracle Database</p>
                          </div>
                        </div>
                        <div className="text-xs font-mono p-2 rounded border" style={{ 
                          backgroundColor: themeColors.hover,
                          borderColor: themeColors.border,
                          color: themeColors.textSecondary
                        }}>
                          {transformedData.schemaConfig?.schemaName || 'CBXDMX'}_{apiData.apiCode}_PKG.sql
                        </div>
                      </div>
                    )}

                    {transformedData.generatedFiles.openapi && (
                      <div className="p-4 rounded-lg border" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <div className="flex items-center gap-3 mb-3">
                          <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.success + '20' }}>
                            <FileJson className="h-5 w-5" style={{ color: themeColors.success }} />
                          </div>
                          <div>
                            <h5 className="font-medium" style={{ color: themeColors.text }}>OpenAPI Spec</h5>
                            <p className="text-xs" style={{ color: themeColors.textSecondary }}>API Documentation</p>
                          </div>
                        </div>
                        <div className="text-xs font-mono p-2 rounded border" style={{ 
                          backgroundColor: themeColors.hover,
                          borderColor: themeColors.border,
                          color: themeColors.textSecondary
                        }}>
                          {apiData.apiCode}_openapi.json
                        </div>
                      </div>
                    )}

                    {transformedData.generatedFiles.postman && (
                      <div className="p-4 rounded-lg border" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <div className="flex items-center gap-3 mb-3">
                          <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.warning + '20' }}>
                            <Database className="h-5 w-5" style={{ color: themeColors.warning }} />
                          </div>
                          <div>
                            <h5 className="font-medium" style={{ color: themeColors.text }}>Postman Collection</h5>
                            <p className="text-xs" style={{ color: themeColors.textSecondary }}>API Testing</p>
                          </div>
                        </div>
                        <div className="text-xs font-mono p-2 rounded border" style={{ 
                          backgroundColor: themeColors.hover,
                          borderColor: themeColors.border,
                          color: themeColors.textSecondary
                        }}>
                          {apiData.apiCode}_postman.json
                        </div>
                      </div>
                    )}

                    {transformedData.generatedFiles.wsdl && (
                      <div className="p-4 rounded-lg border" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <div className="flex items-center gap-3 mb-3">
                          <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.info + '20' }}>
                            <FileCode className="h-5 w-5" style={{ color: themeColors.info }} />
                          </div>
                          <div>
                            <h5 className="font-medium" style={{ color: themeColors.text }}>WSDL File</h5>
                            <p className="text-xs" style={{ color: themeColors.textSecondary }}>SOAP Service Definition</p>
                          </div>
                        </div>
                        <div className="text-xs font-mono p-2 rounded border" style={{ 
                          backgroundColor: themeColors.hover,
                          borderColor: themeColors.border,
                          color: themeColors.textSecondary
                        }}>
                          {apiData.apiCode}_service.wsdl
                        </div>
                      </div>
                    )}

                    {transformedData.generatedFiles.graphqlSchema && (
                      <div className="p-4 rounded-lg border" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <div className="flex items-center gap-3 mb-3">
                          <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.success + '20' }}>
                            <GitMerge className="h-5 w-5" style={{ color: themeColors.success }} />
                          </div>
                          <div>
                            <h5 className="font-medium" style={{ color: themeColors.text }}>GraphQL Schema</h5>
                            <p className="text-xs" style={{ color: themeColors.textSecondary }}>GraphQL Type Definitions</p>
                          </div>
                        </div>
                        <div className="text-xs font-mono p-2 rounded border" style={{ 
                          backgroundColor: themeColors.hover,
                          borderColor: themeColors.border,
                          color: themeColors.textSecondary
                        }}>
                          {apiData.apiCode}_schema.graphql
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Footer */}
          <div className="px-6 py-4 border-t flex items-center justify-between" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.card
          }}>
            <div className="flex items-center gap-3">
              <button
                onClick={copyApiDetails}
                className="px-3 py-2 border rounded-lg flex items-center gap-2 transition-colors hover-lift"
                style={{ 
                  backgroundColor: themeColors.hover,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
              >
                <Copy className="h-4 w-4" />
                Copy Details
              </button>
              <button
                onClick={downloadGeneratedFiles}
                className="px-3 py-2 border rounded-lg flex items-center gap-2 transition-colors hover-lift"
                style={{ 
                  backgroundColor: themeColors.info,
                  borderColor: themeColors.info,
                  color: themeColors.white
                }}
              >
                <Download className="h-4 w-4" />
                Download Files
              </button>
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
                Close
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return null;
}

// New component for folder selection modal (when adding new folder)
function AddFolderModal({ 
  isOpen, 
  onClose, 
  onConfirm,
  selectedCollection,
  colors = {},
  theme = 'dark' 
}) {
  const [folderName, setFolderName] = useState('');
  const [folderDescription, setFolderDescription] = useState('');

  const themeColors = colors || {
    bg: theme === 'dark' ? 'rgb(1 14 35)' : '#f8fafc',
    text: theme === 'dark' ? '#E8ECF1' : '#1e293b',
    textSecondary: theme === 'dark' ? 'rgb(168 178 192)' : '#64748b',
    border: theme === 'dark' ? 'rgb(61 73 92)' : '#e2e8f0',
    hover: theme === 'dark' ? 'rgb(51 63 82)' : '#f1f5f9',
    card: theme === 'dark' ? '#010e23' : '#ffffff',
    modalBorder: theme === 'dark' ? 'rgb(61 73 92)' : '#e2e8f0',
    success: theme === 'dark' ? 'rgb(16 185 129)' : '#10b981',
    white: '#ffffff'
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-50 p-4" style={{ zIndex: 1004 }}>
      <div className="rounded-xl shadow-2xl w-full max-w-md p-6" style={{ 
        backgroundColor: themeColors.bg,
        border: `1px solid ${themeColors.modalBorder}`
      }}>
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.success + '20' }}>
              <Folder className="h-5 w-5" style={{ color: themeColors.success }} />
            </div>
            <h3 className="text-lg font-bold" style={{ color: themeColors.text }}>
              Add New Folder
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-lg transition-colors hover-lift"
            style={{ backgroundColor: themeColors.hover, color: themeColors.textSecondary }}
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="space-y-4">
          <div className="space-y-2">
            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
              Collection
            </label>
            <div className="p-3 rounded-lg border" style={{ 
              borderColor: themeColors.border,
              backgroundColor: themeColors.card
            }}>
              <div className="font-medium text-sm" style={{ color: themeColors.text }}>
                {selectedCollection?.name}
              </div>
              <div className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>
                {selectedCollection?.description}
              </div>
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
              Folder Name *
              <Asterisk className="h-3 w-3" style={{ color: themeColors.error }} />
            </label>
            <input
              type="text"
              value={folderName}
              onChange={(e) => setFolderName(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
              style={{ 
                backgroundColor: themeColors.card,
                borderColor: folderName.trim() ? themeColors.success : themeColors.border,
                color: themeColors.text
              }}
              placeholder="e.g., Loan Origination"
              autoFocus
            />
            {!folderName.trim() && (
              <p className="text-xs mt-1" style={{ color: themeColors.error }}>
                Folder name is required
              </p>
            )}
          </div>

          <div className="space-y-2">
            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
              Description
            </label>
            <textarea
              value={folderDescription}
              onChange={(e) => setFolderDescription(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
              style={{ 
                backgroundColor: themeColors.card,
                borderColor: themeColors.border,
                color: themeColors.text
              }}
              rows="3"
              placeholder="Describe the purpose of this folder..."
            />
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 mt-6">
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
            onClick={() => onConfirm({ name: folderName, description: folderDescription })}
            disabled={!folderName.trim()}
            className="px-4 py-2 rounded-lg flex items-center gap-2 transition-colors hover-lift"
            style={{ 
              backgroundColor: folderName.trim() ? themeColors.success : themeColors.textSecondary,
              color: themeColors.white,
              opacity: folderName.trim() ? 1 : 0.5,
              cursor: folderName.trim() ? 'pointer' : 'not-allowed'
            }}
          >
            <Check className="h-4 w-4" />
            Add Folder
          </button>
        </div>
      </div>
    </div>
  );
}

export default function AutoAPIGeneratorModal({
  isOpen,
  onClose,
  onSave,
  onGenerateAPI,
  selectedObject = null,
  colors = {},
  obType,
  theme = 'dark',
  databaseType,
  authToken = null,
  isEditing = false,
  fromDashboard = false,
  // NEW: Custom query mode props
  customQueryText = '',
  extractedParams = []
}) {

  
  const [isCustomQuery, setIsCustomQuery] = useState(isCustomQuery || false);

  // Add this state to track source type
  const [sourceType, setSourceType] = useState(isCustomQuery ? 'custom_query' : 'database_object');
  const [customQuery, setCustomQuery] = useState(customQueryText);
  
  // Add a state to track if we're editing a custom query
  const [isEditingCustomQuery, setIsEditingCustomQuery] = useState(false);
  const [originalCustomQuery, setOriginalCustomQuery] = useState('');

  const [responseSchemaVersion, setResponseSchemaVersion] = useState(0);
  const [errorSchemaVersion, setErrorSchemaVersion] = useState(0);

  // ============ NEW: Protocol Selection State ============
  const [protocolType, setProtocolType] = useState('rest'); // 'rest', 'soap', 'graphql'

  const [requestBodyVersion, setRequestBodyVersion] = useState(0);
  const [requestSchemaVersion, setRequestSchemaVersion] = useState(0);
  
  // SOAP Configuration State
  const [soapConfig, setSoapConfig] = useState({
    version: '1.1',
    bindingStyle: 'document',
    encodingStyle: 'literal',
    soapAction: '',
    wsdlUrl: '',
    namespace: 'http://tempuri.org/',
    serviceName: '',
    portName: '',
    useAsyncPattern: false,
    includeMtom: false,
    soapHeaderElements: []
  });
  
  // GraphQL Configuration State
  const [graphqlConfig, setGraphqlConfig] = useState({
    operationType: 'query', // 'query', 'mutation', 'subscription'
    operationName: '',
    schema: '',
    enableIntrospection: true,
    enablePersistedQueries: false,
    maxQueryDepth: 10,
    enableBatching: false,
    subscriptionsEnabled: false,
    customDirectives: []
  });

  // Add this with your other useState declarations
  const [selectedFile, setSelectedFile] = useState(null);
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [fileUploadConfig, setFileUploadConfig] = useState({
    maxFileSize: 10485760, // 10MB default
    allowedFileTypes: ['*/*'],
    multipleFiles: false,
    fileParameterName: 'file'
  });


// Add helper function for GraphQL type mapping in preview
const getGraphQLTypeForPreview = (oracleType, isNullable = false) => {
  let graphqlType = 'String';
  const type = oracleType?.toUpperCase() || 'VARCHAR2';
  
  switch(type) {
    case 'NUMBER':
    case 'INTEGER':
    case 'FLOAT':
    case 'DECIMAL':
      graphqlType = 'Int';
      break;
    case 'DATE':
    case 'TIMESTAMP':
      graphqlType = 'DateTime';
      break;
    case 'BOOLEAN':
      graphqlType = 'Boolean';
      break;
    default:
      graphqlType = 'String';
  }
  
  return isNullable ? graphqlType : `${graphqlType}!`;
};


// Add this useEffect near your other sync effects
useEffect(() => {
  if (protocolType === 'soap') {
    const soapAction = soapConfig.soapAction?.toUpperCase() || 'SELECT';
    let operationForSchema = 'SELECT';
    
    switch(soapAction) {
      case 'SELECT':
        operationForSchema = 'SELECT';
        break;
      case 'INSERT':
        operationForSchema = 'INSERT';
        break;
      case 'UPDATE':
        operationForSchema = 'UPDATE';
        break;
      case 'DELETE':
        operationForSchema = 'DELETE';
        break;
      case 'EXECUTE':
        operationForSchema = 'EXECUTE';
        break;
      default:
        operationForSchema = 'SELECT';
    }
    
    setSchemaConfig(prev => ({
      ...prev,
      operation: operationForSchema
    }));
    
    console.log('🔄 SOAP Action changed to:', soapAction, '- Setting operation to:', operationForSchema);
  }
}, [soapConfig.soapAction, protocolType]);


// Protect body type when SOAP or GraphQL protocol is selected
useEffect(() => {
  // For SOAP protocol, force body type to 'soap'
  if (protocolType === 'soap' && requestBody.bodyType !== 'soap') {
    console.log('🔄 SOAP protocol requires SOAP envelope body type, setting bodyType to "soap"');
    setRequestBody(prev => ({ ...prev, bodyType: 'soap' }));
  }
  // For GraphQL protocol, force body type to 'graphql'
  else if (protocolType === 'graphql' && requestBody.bodyType !== 'graphql') {
    console.log('🔄 GraphQL protocol requires GraphQL query body type, setting bodyType to "graphql"');
    setRequestBody(prev => ({ ...prev, bodyType: 'graphql' }));
  }
}, [protocolType]);



// Add this near your other helper functions (around where generateGraphQLSchemaFromObject is defined)
const generateGraphQLSchemaFromCustomQuery = useCallback(() => {
  const inParams = getInParameters();
  const outMappings = getOutMappings();
  const operationName = graphqlConfig.operationName || 'customQuery';
  const operationType = graphqlConfig.operationType || 'query';
  
  // Extract table name from custom query
  let tableName = 'CustomData';
  if (customQuery && customQuery.trim()) {
    const fromMatch = customQuery.match(/FROM\s+([^\s,;]+)/i);
    if (fromMatch && fromMatch[1]) {
      tableName = fromMatch[1];
      if (tableName.includes('.')) {
        tableName = tableName.split('.').pop();
      }
      tableName = tableName.charAt(0).toUpperCase() + tableName.slice(1);
    }
  }
  
  // Build the main type from response mappings
  let mainType = '';
  if (outMappings.length > 0) {
    mainType = `type ${tableName} {
${outMappings.map(m => `  ${m.apiField}: ${getGraphQLType(m.oracleType, m.nullable)}`).join('\n')}
}`;
  } else {
    mainType = `type ${tableName} {
  id: ID!
  createdAt: DateTime!
  updatedAt: DateTime!
}`;
  }
  
  // Build input type from parameters
  let inputType = '';
  if (inParams.length > 0) {
    inputType = `input ${tableName}Input {
${inParams.map(p => `  ${p.key}: ${getGraphQLType(p.oracleType, !p.required)}`).join('\n')}
}`;
  }
  
  // Build the operation
  let operationField = '';
  if (operationType === 'query') {
    operationField = `  ${operationName}(
    ${inParams.filter(p => p.parameterLocation !== 'body').slice(0, 3).map(p => `${p.key}: ${getGraphQLType(p.oracleType, true)}`).join('\n    ')}
  ): ${tableName}Result`;
  } else {
    operationField = `  ${operationName}(
    input: ${tableName}Input!
  ): ${tableName}Payload`;
  }
  
  return `# ============================================================================
# GraphQL Schema for Custom Query
# Generated from custom SQL query
# Generated: ${new Date().toISOString()}
# ============================================================================

# Scalar definitions
scalar DateTime
scalar JSON
scalar Date

# Main types
${mainType}

${inputType ? inputType + '\n' : ''}

# Result types
type ${tableName}Result {
  success: Boolean!
  message: String
  data: ${tableName}
  errors: [ErrorDetail]
}

type ${tableName}Payload {
  success: Boolean!
  message: String
  data: ${tableName}
  errors: [ErrorDetail]
}

type ErrorDetail {
  field: String
  message: String
  code: String
}

# Query and Mutation definitions
type ${operationType === 'query' ? 'Query' : 'Mutation'} {
${operationField}
}

# Example query:
# ${operationType === 'query' ? 'query' : 'mutation'} {
#   ${operationName}(${inParams.slice(0, 2).map(p => `${p.key}: "example"`).join(', ')}) {
#     success
#     message
#     data {
#       ${outMappings.slice(0, 3).map(m => m.apiField).join('\n#       ')}
#     }
#   }
# }`;
}, [graphqlConfig.operationName, graphqlConfig.operationType, customQuery, getInParameters, getOutMappings]);

  // Add this function near your other helper functions
const generateGraphQLSchemaFromObject = useCallback(() => {
  const inParams = getInParameters();
  const outMappings = getOutMappings();
  const objectName = schemaConfig.objectName || selectedDbObject?.name || 'Unknown';
  const objectType = schemaConfig.objectType || selectedDbObject?.type || 'TABLE';
  
  // Use the current graphqlConfig values
  const currentOperationType = graphqlConfig.operationType;
  const currentOperationName = graphqlConfig.operationName;
  
  console.log('📝 Generating GraphQL schema with:', {
    objectName,
    objectType,
    operationType: currentOperationType,
    operationName: currentOperationName,
    inParamsCount: inParams.length,
    outMappingsCount: outMappings.length
  });
  
  // Generate the main type based on the database object
  let mainType = '';
  let inputType = '';
  let operationField = '';
  
  // Build the main object type from response mappings
  if (outMappings.length > 0) {
    mainType = `type ${objectName} {
${outMappings.map(m => `  ${m.apiField}: ${getGraphQLType(m.oracleType, m.nullable)}`).join('\n')}
}`;
  } else {
    mainType = `type ${objectName} {
  id: ID!
  createdAt: DateTime!
  updatedAt: DateTime!
}`;
  }
  
  // Build input type from parameters
  if (inParams.length > 0) {
    inputType = `input ${objectName}Input {
${inParams.map(p => `  ${p.key}: ${getGraphQLType(p.oracleType, !p.required)}`).join('\n')}
}`;
  }
  
  // Generate appropriate operations based on object type and HTTP method
  switch(objectType) {
    case 'TABLE':
      if (apiDetails.httpMethod === 'GET' || schemaConfig.operation === 'SELECT') {
        operationField = `  ${objectName.toLowerCase()}s(
    page: Int = 1
    limit: Int = 10
    filter: ${objectName}Filter
    sortBy: String
    sortOrder: SortOrder
  ): ${objectName}Connection!`;
        
        operationField += `\n  \n  ${objectName.toLowerCase()}(
    id: ID!
  ): ${objectName}`;
      }
      if (apiDetails.httpMethod === 'POST' || schemaConfig.operation === 'INSERT') {
        operationField += `\n  \n  create${objectName}(
    input: ${objectName}Input!
  ): ${objectName}Payload!`;
      }
      if (apiDetails.httpMethod === 'PUT' || schemaConfig.operation === 'UPDATE') {
        operationField += `\n  \n  update${objectName}(
    id: ID!
    input: ${objectName}Input!
  ): ${objectName}Payload!`;
      }
      if (apiDetails.httpMethod === 'DELETE' || schemaConfig.operation === 'DELETE') {
        operationField += `\n  \n  delete${objectName}(
    id: ID!
  ): DeletePayload!`;
      }
      break;
      
    case 'PROCEDURE':
      operationField = `  execute${objectName}(
${inParams.map(p => `    ${p.key}: ${getGraphQLType(p.oracleType, !p.required)}`).join('\n')}
  ): ProcedureResult!`;
      break;
      
    case 'FUNCTION':
      operationField = `  ${objectName.toLowerCase()}(
${inParams.map(p => `    ${p.key}: ${getGraphQLType(p.oracleType, !p.required)}`).join('\n')}
  ): ${getGraphQLType(outMappings[0]?.oracleType || 'VARCHAR2', false)}`;
      break;
      
    default:
      operationField = `  ${objectName.toLowerCase()}(
${inParams.map(p => `    ${p.key}: ${getGraphQLType(p.oracleType, !p.required)}`).join('\n')}
  ): ${objectName}Result`;
  }
  
  // Build the complete schema
  return `# ============================================================================
# GraphQL Schema for ${apiDetails.apiName}
# Generated from: ${schemaConfig.schemaName}.${objectName} (${objectType})
# Generated: ${new Date().toISOString()}
# ============================================================================

# Scalar definitions
scalar DateTime
scalar JSON
scalar Date

# Enums
enum SortOrder {
  ASC
  DESC
}

# Pagination types
type PageInfo {
  hasNextPage: Boolean!
  hasPreviousPage: Boolean!
  startCursor: String
  endCursor: String
  totalCount: Int!
}

type ${objectName}Edge {
  node: ${objectName}!
  cursor: String!
}

type ${objectName}Connection {
  edges: [${objectName}Edge!]!
  pageInfo: PageInfo!
}

# Main types
${mainType}

${inputType ? inputType + '\n' : ''}
# Filter input type
input ${objectName}Filter {
  ${inParams.filter(p => p.parameterLocation === 'query').map(p => `  ${p.key}: ${getGraphQLType(p.oracleType, true)}`).join('\n')}
}

# Payload types
type ${objectName}Payload {
  success: Boolean!
  message: String
  ${objectName.toLowerCase()}: ${objectName}
  errors: [ErrorDetail]
}

type DeletePayload {
  success: Boolean!
  message: String
  deletedId: ID
}

type ProcedureResult {
  success: Boolean!
  message: String
  result: JSON
  executionTime: Float
}

type ${objectName}Result {
  success: Boolean!
  data: ${objectName}
  message: String
}

type ErrorDetail {
  field: String
  message: String
  code: String
}

# Query and Mutation definitions
type Query {
${graphqlConfig.operationType === 'query' ? operationField : '  _health: Boolean!'}
}

type Mutation {
${graphqlConfig.operationType === 'mutation' ? operationField : '  _empty: Boolean'}
}

type Subscription {
${graphqlConfig.subscriptionsEnabled && graphqlConfig.operationType === 'subscription' ? `  ${objectName.toLowerCase()}Changed: ${objectName}!` : '  _empty: Boolean'}
}

# Example queries
# ============================================================================
# Query example:
# query {
#   ${objectName.toLowerCase()}s(page: 1, limit: 10) {
#     edges {
#       node {
#         ${outMappings.slice(0, 3).map(m => m.apiField).join('\n#         ')}
#       }
#     }
#     pageInfo {
#       totalCount
#       hasNextPage
#     }
#   }
# }
#
# Mutation example:
# mutation {
#   create${objectName}(input: {
#     ${inParams.slice(0, 3).map(p => `${p.key}: "example"`).join('\n#     ')}
#   }) {
#     success
#     message
#     ${objectName.toLowerCase()} {
#       id
#     }
#   }
# }`;
}, [schemaConfig, apiDetails, getInParameters, getOutMappings, graphqlConfig]);



// Add this helper function to map database types to GraphQL types
const getGraphQLType = (oracleType, isNullable = false) => {
  let graphqlType = 'String';
  const type = oracleType?.toUpperCase() || 'VARCHAR2';
  
  switch(type) {
    case 'NUMBER':
    case 'INTEGER':
    case 'FLOAT':
    case 'DECIMAL':
      graphqlType = 'Int';
      break;
    case 'DATE':
    case 'TIMESTAMP':
    case 'TIMESTAMP WITH TIME ZONE':
      graphqlType = 'DateTime';
      break;
    case 'BOOLEAN':
      graphqlType = 'Boolean';
      break;
    case 'JSON':
    case 'JSONB':
      graphqlType = 'JSON';
      break;
    case 'CLOB':
    case 'TEXT':
      graphqlType = 'String';
      break;
    default:
      graphqlType = 'String';
  }
  
  return isNullable ? graphqlType : `${graphqlType}!`;
};


useEffect(() => {
  if (protocolType === 'graphql' && !isEditing && selectedDbObject) {
    const autoSchema = generateGraphQLSchemaFromObject();
    if (autoSchema && (!graphqlConfig.schema || graphqlConfig.schema === '')) {
      setGraphqlConfig(prev => ({ ...prev, schema: autoSchema }));
    }
  }
}, [protocolType, selectedDbObject, parameters, responseMappings, schemaConfig]);


// Auto-set GraphQL operation name from database object
useEffect(() => {
  if (protocolType === 'graphql' && !isEditing && selectedDbObject) {
    const objectName = schemaConfig.objectName || selectedDbObject.name;
    const defaultOperationName = graphqlConfig.operationType === 'query' 
      ? `get${objectName}${schemaConfig.objectType === 'TABLE' ? 's' : ''}`
      : graphqlConfig.operationType === 'mutation'
        ? `create${objectName}`
        : `${objectName}Changed`;
    
    if (!graphqlConfig.operationName) {
      setGraphqlConfig(prev => ({ ...prev, operationName: defaultOperationName }));
    }
  }
}, [protocolType, selectedDbObject]);


  // Add this function near your other helper functions
const getDefaultSoapAction = useCallback(() => {
  // For REST mode, map HTTP method to SOAP Action
  if (protocolType === 'rest' && apiDetails.httpMethod) {
    switch(apiDetails.httpMethod) {
      case 'GET': return 'SELECT';
      case 'POST': return schemaConfig.objectType === 'PROCEDURE' ? 'EXECUTE' : 'INSERT';
      case 'PUT': return 'UPDATE';
      case 'PATCH': return 'UPDATE';
      case 'DELETE': return 'DELETE';
      default: return 'PROCESS';
    }
  }
  
  // For database object types
  switch(schemaConfig.objectType) {
    case 'TABLE':
      switch(schemaConfig.operation) {
        case 'SELECT': return 'SELECT';
        case 'INSERT': return 'INSERT';
        case 'UPDATE': return 'UPDATE';
        case 'DELETE': return 'DELETE';
        default: return 'PROCESS';
      }
    case 'VIEW':
      return 'SELECT';
    case 'PROCEDURE':
      return 'EXECUTE';
    case 'FUNCTION':
      return 'EXECUTE';
    case 'PACKAGE':
      return 'EXECUTE_PROCEDURE';
    default:
      return 'PROCESS';
  }
}, [protocolType]);


// Auto-set SOAP Action when object type or operation changes
useEffect(() => {
  if (protocolType === 'soap' && !isEditing) {
    const defaultAction = getDefaultSoapAction();
    if (defaultAction && soapConfig.soapAction !== defaultAction) {
      setSoapConfig(prev => ({ ...prev, soapAction: defaultAction }));
    }
  }
}, [protocolType]);

  // ============ DETECT CUSTOM QUERY IN EDIT MODE ============
  useEffect(() => {
    if (isEditing && selectedObject && !isCustomQuery) {
      // Check if the selected object has custom query data
      const sourceObj = selectedObject.sourceObject || selectedObject;
      const apiData = selectedObject.data || selectedObject;
      
      // Check various places where custom query might be stored
      const hasCustomQuery = 
        sourceObj?.type === 'CUSTOM_QUERY' ||
        sourceObj?.isCustomQuery === true ||
        selectedObject?.isCustomQuery === true ||
        selectedObject?.useCustomQuery === true ||
        selectedObject?.customSelectStatement ||
        sourceObj?.query ||
        selectedObject?.sourceObject?.customSelectStatement ||
        selectedObject?.isCustomQuery === 'true' ||
        selectedObject?.data?.isCustomQuery === true ||
        apiData?.isCustomQuery === true ||
        apiData?.useCustomQuery === true;
      
      if (hasCustomQuery) {
        console.log('🔍 Editing existing custom query API detected');
        setIsEditingCustomQuery(true);
        setSourceType('custom_query');
        setIsCustomQuery(true);  // <-- ADD THIS LINE
        
        // Extract the custom query text from various possible locations
        let extractedQuery = 
          selectedObject?.customSelectStatement ||
          selectedObject?.sourceObject?.customSelectStatement ||
          sourceObj?.customSelectStatement ||
          sourceObj?.query ||
          selectedObject?.customQueryText ||
          selectedObject?.data?.customSelectStatement ||
          selectedObject?.data?.sourceObject?.customSelectStatement ||
          '';
        
        setOriginalCustomQuery(extractedQuery);
        setCustomQuery(extractedQuery);
        
        console.log('📝 Extracted custom query:', extractedQuery.substring(0, 100));
        
        // Extract database type from the API data
        const dbType = selectedObject?.databaseType || 
                       selectedObject?.data?.databaseType ||
                       selectedObject?.sourceObject?.databaseType ||
                       sourceObj?.databaseType ||
                       databaseType || 
                       'oracle';
        
        // Set the current database type for custom query
        setCurrentDatabaseType(dbType);
        console.log('📦 Database type for custom query:', dbType);
        
        // Extract parameters if available
        if (selectedObject?.parameters && selectedObject.parameters.length > 0) {
          const extractedParamsList = selectedObject.parameters.map((param, index) => ({
            id: param.id || `param-${Date.now()}-${index}`,
            key: param.key,
            dbColumn: param.dbColumn || param.key,
            oracleType: param.oracleType || 'VARCHAR2',
            apiType: param.apiType || 'string',
            parameterLocation: param.parameterLocation || 'query',
            required: param.required !== undefined ? param.required : true,
            description: param.description || `Parameter: ${param.key}`,
            example: param.example || '',
            validationPattern: param.validationPattern || '',
            defaultValue: param.defaultValue || '',
            inBody: param.parameterLocation === 'body',
            isPrimaryKey: param.isPrimaryKey || false,
            paramMode: param.paramMode || 'IN'
          }));
          setParameters(extractedParamsList);
        } else if (selectedObject?.data?.parameters && selectedObject.data.parameters.length > 0) {
          const extractedParamsList = selectedObject.data.parameters.map((param, index) => ({
            id: param.id || `param-${Date.now()}-${index}`,
            key: param.key,
            dbColumn: param.dbColumn || param.key,
            oracleType: param.oracleType || 'VARCHAR2',
            apiType: param.apiType || 'string',
            parameterLocation: param.parameterLocation || 'query',
            required: param.required !== undefined ? param.required : true,
            description: param.description || `Parameter: ${param.key}`,
            example: param.example || '',
            validationPattern: param.validationPattern || '',
            defaultValue: param.defaultValue || '',
            inBody: param.parameterLocation === 'body',
            isPrimaryKey: param.isPrimaryKey || false,
            paramMode: param.paramMode || 'IN'
          }));
          setParameters(extractedParamsList);
        }
        
        // Auto-generate API name from query if not set
        if (!apiDetails.apiName && extractedQuery) {
          const generatedName = generateApiNameFromQuery(extractedQuery);
          setApiDetails(prev => ({
            ...prev,
            apiName: generatedName,
            description: `API generated from custom query: ${extractedQuery.substring(0, 100)}...`
          }));
        }
        
        // Also populate response mappings if they exist
        if (selectedObject?.responseMappings && selectedObject.responseMappings.length > 0) {
          setResponseMappings(selectedObject.responseMappings);
        } else if (selectedObject?.data?.responseMappings && selectedObject.data.responseMappings.length > 0) {
          setResponseMappings(selectedObject.data.responseMappings);
        }
        
        // Check for protocol type
        if (apiData.protocolType) {
          setProtocolType(apiData.protocolType);
        }
        if (apiData.soapConfig) {
          setSoapConfig(apiData.soapConfig);
        }
        if (apiData.graphqlConfig) {
          setGraphqlConfig(apiData.graphqlConfig);
        }
      }
    }
  }, [isEditing, selectedObject, isCustomQuery, databaseType]);

  // Initialize extracted parameters when in custom query mode (for new APIs)
  useEffect(() => {
    if (isCustomQuery && extractedParams.length > 0) {
      // Convert extracted params to the parameter format expected by the modal
      const paramsWithIds = extractedParams.map((param, index) => ({
        id: `param-${Date.now()}-${index}`,
        key: param.key || param.parameterName,
        dbColumn: param.dbColumn || param.parameterName,
        oracleType: param.dataType || 'VARCHAR2',
        apiType: 'string',
        parameterLocation: param.parameterLocation || 'query',
        required: param.required !== undefined ? param.required : true,
        description: param.description || `Parameter: ${param.key || param.parameterName}`,
        example: param.example || '',
        validationPattern: param.validationPattern || '',
        defaultValue: param.defaultValue || '',
        inBody: false,
        isPrimaryKey: false,
        paramMode: 'IN'
      }));
      setParameters(paramsWithIds);
      
      // Auto-generate API name from query
      const generatedName = generateApiNameFromQuery(customQuery);
      setApiDetails(prev => ({
        ...prev,
        apiName: generatedName,
        apiCode: generatedName.toUpperCase().replace(/\s+/g, '_'),
        description: `API generated from custom query: ${customQuery.substring(0, 100)}...`
      }));
    }
  }, [isCustomQuery, extractedParams, customQuery]);

  // Helper function to generate API name from query
  const generateApiNameFromQuery = (sqlQuery) => {
    // Try to extract table names from the query
    const fromMatch = sqlQuery?.match(/FROM\s+([^\s,]+)/i);
    if (fromMatch) {
      const tableName = fromMatch[1];
      return `Get ${tableName} Data`;
    }
    return 'Custom Query API';
  };

  // Update the validateRequiredFields function (around line 1945) to handle mode-specific validation
const validateRequiredFields = () => {
  const errors = {};

  // Check collection and folder
  if (!selectedCollection) {
    errors.collection = 'API Collection is required';
  }
  if (!selectedFolder) {
    errors.folder = 'API Folder is required';
  }

  // Check API details
  if (!apiDetails.apiName?.trim()) {
    errors.apiName = 'API Name is required';
  }
  if (!apiDetails.apiCode?.trim()) {
    errors.apiCode = 'API Code is required';
  }
  if (protocolType === 'rest' && !apiDetails.endpointPath?.trim()) {
    errors.endpointPath = 'Endpoint Path is required';
  }

  // ============ CRITICAL FIX: Check source type correctly ============
  // Determine if we're in custom query mode OR editing a custom query
  const isCustomQueryMode = isCustomQuery || sourceType === 'custom_query' || isEditingCustomQuery;
  
  // For custom queries, check that there's a query
  if (isCustomQueryMode && !customQuery?.trim()) {
    errors.customQuery = 'Custom SELECT statement is required';
  }

  // For database objects (not custom queries), check schema config
  // IMPORTANT: Also check if we're NOT in edit mode OR if we're in edit mode but not a custom query
  if (!isCustomQueryMode && !isEditingCustomQuery && !isEditing && selectedDbObject) {
    if (!schemaConfig.schemaName?.trim()) {
      errors.schemaName = 'Schema Name is required';
    }
    if (!schemaConfig.objectName?.trim()) {
      errors.objectName = 'Object Name is required';
    }
  }
  
  // For editing mode with database object, also validate schema config
  if (isEditing && !isCustomQueryMode && !sourceType === 'custom_query') {
    if (!schemaConfig.schemaName?.trim() && selectedDbObject) {
      errors.schemaName = 'Schema Name is required';
    }
    if (!schemaConfig.objectName?.trim() && selectedDbObject) {
      errors.objectName = 'Object Name is required';
    }
  }
  
  // SOAP specific validation
  if (protocolType === 'soap') {
    if (!soapConfig.soapAction?.trim()) {
      errors.soapAction = 'SOAP Action is required';
    }
    if (!soapConfig.serviceName?.trim()) {
      errors.serviceName = 'Service Name is required';
    }
  }
  
  // GraphQL specific validation
  if (protocolType === 'graphql') {
    if (!graphqlConfig.operationName?.trim()) {
      errors.operationName = 'Operation Name is required';
    }
    if (!graphqlConfig.schema?.trim()) {
      errors.graphqlSchema = 'GraphQL Schema is required';
    }
  }

  // Check authentication specific required fields
  if (authConfig.authType === 'apiKey') {
    if (!authConfig.apiKeyHeader?.trim()) {
      errors.apiKeyHeader = 'API Key Header is required';
    }
    if (!authConfig.apiSecretHeader?.trim()) {
      errors.apiSecretHeader = 'API Secret Header is required';
    }
  } else if (authConfig.authType === 'bearer') {
    if (!authConfig.jwtIssuer?.trim()) {
      errors.jwtIssuer = 'JWT Issuer is required';
    }
  } else if (authConfig.authType === 'basic') {
    if (!authConfig.basicUsername?.trim()) {
      errors.basicUsername = 'Username is required';
    }
    if (!authConfig.basicPassword?.trim()) {
      errors.basicPassword = 'Password is required';
    }
  }

  setValidationErrors(errors);
  return Object.keys(errors).length === 0;
};

  // Update the handleSave function to handle custom query mode (including editing) and protocol type
  const handleSave = () => {
    // Validate all required fields
    if (!validateRequiredFields()) {
      // Show first tab with errors
      if (validationErrors.collection || validationErrors.folder) {
        setActiveTab('definition');
      } else if (validationErrors.apiName || validationErrors.apiCode || validationErrors.endpointPath) {
        setActiveTab('definition');
      } else if (validationErrors.customQuery) {
        setActiveTab('request');
      } else if (!isCustomQuery && !isEditingCustomQuery && !isEditing && (validationErrors.schemaName || validationErrors.objectName)) {
        setActiveTab('schema');
      } else if (validationErrors.soapAction || validationErrors.serviceName) {
        setActiveTab('soap');
      } else if (validationErrors.operationName || validationErrors.graphqlSchema) {
        setActiveTab('graphql');
      } else if (authConfig.authType !== 'none') {
        setActiveTab('auth');
      }
      
      alert('Please fill in all required fields marked with *');
      return;
    }

    if (!isEditing && apiCodeExists) {
      alert(`❌ Cannot generate API\n\nAn API with code "${apiDetails.apiCode}" already exists.\nPlease choose a different API code to continue.`);
      setActiveTab('definition');
      return;
    }

    if (!selectedCollection || !selectedFolder) {
      alert('Please select both a collection and folder');
      setActiveTab('definition');
      return;
    }

    // Determine if this is a custom query
    const isCustomQueryMode = isCustomQuery || sourceType === 'custom_query' || isEditingCustomQuery;
    
    // Get the API ID for editing mode
    let apiId = `api-${Date.now()}`;
    if (isEditing) {
      // Try to get the ID from various possible locations
      apiId = selectedObject?.id || 
              selectedObject?.data?.id || 
              selectedObject?.apiId ||
              selectedObject?._id ||
              `api-${Date.now()}`;
    }

    // Prepare the API data object
    const apiData = {
      id: apiId,
      ...apiDetails,
      
      // Protocol type
      protocolType: protocolType,
      
      // SOAP configuration
      soapConfig: protocolType === 'soap' ? soapConfig : null,
      
      // GraphQL configuration
      graphqlConfig: protocolType === 'graphql' ? graphqlConfig : null,
      
      // For custom query mode, include the query and flag
      isCustomQuery: isCustomQueryMode,
      customSelectStatement: isCustomQueryMode ? customQuery : null,
      useCustomQuery: isCustomQueryMode,
      
      // For database objects (not custom queries)
      schemaConfig: (!isCustomQueryMode && sourceType === 'database_object') ? {
        ...schemaConfig,
        databaseType: currentDatabaseType
      } : null,
      
      // Collection and folder information
      collectionInfo: {
        collectionId: selectedCollection.id,
        collectionName: selectedCollection.name,
        collectionType: selectedCollection.type,
        isNewCollection: isAddingNewCollection,
        folderId: selectedFolder.id,
        folderName: selectedFolder.name,
        isNewFolder: selectedFolder.id?.startsWith('new-folder-')
      },
      
      // Parameters - only IN parameters (including IN/OUT)
      parameters: getInParameters().map(p => ({
          ...p,
          id: p.id || `param-${Date.now()}-${Math.random()}`,
          inBody: p.parameterLocation === 'body',
          paramMode: p.paramMode || 'IN',
          key: p.key,
          dbColumn: p.dbColumn,
          // ============ CRITICAL FIX: Normalize apiType before saving ============
          apiType: (() => {
              const type = (p.apiType || 'string').toLowerCase();
              // Map to standard uppercase values for database storage
              if (type === 'str' || type === 'text' || type === 'varchar' || type === 'varchar2' || type === 'string') return 'STRING';
              if (type === 'int' || type === 'integer') return 'INTEGER';
              if (type === 'number' || type === 'numeric' || type === 'decimal') return 'NUMBER';
              if (type === 'float' || type === 'double') return 'NUMBER';
              if (type === 'bool' || type === 'boolean') return 'BOOLEAN';
              if (type === 'arr' || type === 'array') return 'ARRAY';
              if (type === 'obj' || type === 'object') return 'OBJECT';
              if (type === 'file') return 'FILE';
              return type.toUpperCase();
          })(),
          // ============ CRITICAL FIX: Normalize example based on apiType ============
          example: (() => {
              const apiType = (p.apiType || 'string').toLowerCase();
              const currentExample = p.example;
              
              // If example is empty, generate appropriate example
              if (!currentExample || currentExample === '') {
                  if (apiType === 'integer') return '123';
                  if (apiType === 'number') return '123.45';
                  if (apiType === 'boolean') return 'true';
                  if (apiType === 'array') return '[]';
                  if (apiType === 'object') return '{}';
                  if (apiType === 'file') return 'Select a file...';
                  return 'sample';
              }
              
              // Validate and convert example to match type
              if (apiType === 'integer') {
                  const parsed = parseInt(currentExample, 10);
                  return isNaN(parsed) ? '123' : parsed.toString();
              }
              if (apiType === 'number') {
                  const parsed = parseFloat(currentExample);
                  return isNaN(parsed) ? '123.45' : parsed.toString();
              }
              if (apiType === 'boolean') {
                  const isTrue = currentExample === 'true' || currentExample === '1' || currentExample === 'yes';
                  const isFalse = currentExample === 'false' || currentExample === '0' || currentExample === 'no';
                  if (isTrue) return 'true';
                  if (isFalse) return 'false';
                  return 'true';
              }
              if (apiType === 'array') {
                  if (currentExample === '[]' || currentExample.startsWith('[')) return currentExample;
                  return '[]';
              }
              if (apiType === 'object') {
                  if (currentExample === '{}' || currentExample.startsWith('{')) return currentExample;
                  return '{}';
              }
              
              return currentExample;
          })()
      })),
      
      // Response mappings - OUT parameters and response fields
      responseMappings: getOutMappings().map(m => ({
        ...m,
        id: m.id || `mapping-${Date.now()}-${Math.random()}`,
        includeInResponse: m.includeInResponse !== undefined ? m.includeInResponse : true,
        inResponse: m.inResponse !== undefined ? m.inResponse : true,
        paramMode: m.paramMode || 'OUT'
      })),
      
      // Request body configuration
      requestBody: {
        ...requestBody,
        bodyType: protocolType === 'soap' ? 'soap' : 
                  protocolType === 'graphql' ? 'graphql' : 
                  requestBody.bodyType || 'none'
      },
      
      // Response body configuration
      responseBody: {
        ...responseBody,
        successSchema: responseBody.successSchema || '{\n  "success": true,\n  "data": {},\n  "message": "Request processed successfully"\n}',
        errorSchema: responseBody.errorSchema || '{\n  "success": false,\n  "error": {\n    "code": "ERROR_CODE",\n    "message": "Error description",\n    "details": {}\n  }\n}'
      },
      
      // Authentication configuration
      authConfig: {
        ...authConfig,
        authType: authConfig.authType || 'none'
      },
      
      // Headers
      headers: headers.map(h => ({
        ...h,
        id: h.id || `header-${Date.now()}-${Math.random()}`
      })),
      
      // Settings
      settings: {
        ...settings,
        timeout: settings.timeout || 30000,
        maxRecords: settings.maxRecords || 1000,
        enableLogging: settings.enableLogging !== undefined ? settings.enableLogging : true,
        logLevel: settings.logLevel || 'INFO'
      },
      
      // Tests configuration
      tests: {
        ...tests,
        testData: tests.testData || {},
        testQueries: tests.testQueries || []
      },
      
      // Source object information
      sourceObject: isCustomQueryMode ? {
        type: 'CUSTOM_QUERY',
        query: customQuery,
        databaseType: currentDatabaseType,
        isCustomQuery: true
      } : {
        type: schemaConfig.objectType,
        name: schemaConfig.objectName,
        owner: schemaConfig.schemaName,
        databaseType: currentDatabaseType,
        isSynonym: sourceObjectInfo.isSynonym,
        targetType: sourceObjectInfo.targetType,
        targetName: sourceObjectInfo.targetName,
        targetOwner: sourceObjectInfo.targetOwner
      },
      
      // Validation result
      validation: validationResult,
      
      // Flags
      isEditing: isEditing,
      customQueryText: isCustomQueryMode ? customQuery : null,
      
      // Timestamps
      createdAt: isEditing ? (selectedObject?.createdAt || new Date().toISOString()) : new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      
      // Database type
      databaseType: currentDatabaseType,
      
      // Original custom query (for reference when editing)
      originalCustomQuery: isEditingCustomQuery ? originalCustomQuery : null,
      
      // File upload config
      fileUploadConfig: fileUploadConfig
    };
    
    console.log('📦 Prepared API data for preview:', {
      id: apiData.id,
      name: apiData.apiName,
      code: apiData.apiCode,
      protocolType: apiData.protocolType,
      isCustomQuery: apiData.isCustomQuery,
      isEditing: apiData.isEditing,
      customQueryLength: apiData.customSelectStatement?.length || 0,
      parametersCount: apiData.parameters.length,
      mappingsCount: apiData.responseMappings.length
    });
    
    setNewApiData(apiData);
    setPreviewOpen(true);
  };

const handlePreviewConfirm = async () => {
    setPreviewOpen(false);
    setLoadingOpen(true);
    
    try {
        if (!selectedCollection || !selectedFolder) {
            throw new Error('Collection and folder are required');
        }

        const isCustomQueryMode = isCustomQuery || sourceType === 'custom_query' || isEditingCustomQuery;
        
        // CRITICAL FIX: For custom queries, use currentDatabaseType instead of selectedDbObject
        let actualDatabaseType;
        if (isCustomQueryMode) {
            // Use the database type selected in the UI for custom queries
            actualDatabaseType = currentDatabaseType;
            console.log('🎯 Using custom query database type:', actualDatabaseType);
        } else {
            // For database objects, use the object's database type
            actualDatabaseType = selectedDbObject?.databaseType || databaseType || 'oracle';
            console.log('🎯 Using database object type:', actualDatabaseType);
        }
        
        console.log('🚀 Generating/Updating API for:', isCustomQueryMode ? 'Custom Query' : 'Database Object');
        console.log('📦 Database type:', actualDatabaseType);
        console.log('📝 isEditing:', isEditing);
        console.log('🔌 Protocol type:', protocolType);
        console.log('📋 SOAP Config:', soapConfig);
        console.log('📋 GraphQL Config:', graphqlConfig);
        
        // ============ PREPARE PARAMETERS ============
        const inParameters = getInParameters();
        console.log('📤 Parameters being sent:', inParameters.map(p => ({ 
            key: p.key, 
            oracleType: p.oracleType,
            parameterLocation: p.parameterLocation 
        })));
        
        // ============ PREPARE RESPONSE MAPPINGS ============
        const outMappings = getOutMappings();
        console.log('📥 Response mappings being sent:', outMappings.length);
        
        // ============ PREPARE THE REQUEST OBJECT ============
        const generateRequest = {
            // Basic API Info
            apiName: apiDetails.apiName,
            apiCode: apiDetails.apiCode,
            apiName: (apiDetails.apiName || '').toUpperCase(),
            apiCode: (apiDetails.apiCode || '').toUpperCase().replace(/\s+/g, '_'),
            description: apiDetails.description,
            databaseType: actualDatabaseType, // Use the correctly determined database type
            version: apiDetails.version,
            httpMethod: apiDetails.httpMethod,
            basePath: apiDetails.basePath,
            endpointPath: apiDetails.endpointPath,
            category: apiDetails.category,
            owner: apiDetails.owner,
            status: apiDetails.status,
            tags: apiDetails.tags,
            
            // ============ PROTOCOL TYPE ============
            protocolType: protocolType,
            
            // ============ SOAP CONFIGURATION ============
            soapConfig: protocolType === 'soap' ? {
                version: soapConfig.version,
                bindingStyle: soapConfig.bindingStyle,
                encodingStyle: soapConfig.encodingStyle,
                soapAction: soapConfig.soapAction,
                wsdlUrl: soapConfig.wsdlUrl,
                namespace: soapConfig.namespace,
                serviceName: soapConfig.serviceName,
                portName: soapConfig.portName,
                useAsyncPattern: soapConfig.useAsyncPattern,
                includeMtom: soapConfig.includeMtom,
                soapHeaderElements: soapConfig.soapHeaderElements
            } : null,
            
            // ============ GRAPHQL CONFIGURATION ============
            graphqlConfig: protocolType === 'graphql' ? {
                operationType: graphqlConfig.operationType,
                operationName: graphqlConfig.operationName,
                schema: graphqlConfig.schema,
                enableIntrospection: graphqlConfig.enableIntrospection,
                enablePersistedQueries: graphqlConfig.enablePersistedQueries,
                maxQueryDepth: graphqlConfig.maxQueryDepth,
                enableBatching: graphqlConfig.enableBatching,
                subscriptionsEnabled: graphqlConfig.subscriptionsEnabled,
                customDirectives: graphqlConfig.customDirectives
            } : null,
            
            // ============ FILE UPLOAD CONFIGURATION ============
            fileUploadConfig: {
                maxFileSize: fileUploadConfig.maxFileSize,
                allowedFileTypes: fileUploadConfig.allowedFileTypes,
                multipleFiles: fileUploadConfig.multipleFiles,
                fileParameterName: fileUploadConfig.fileParameterName
            },
            
            // ============ COLLECTION INFO ============
            collectionInfo: {
                collectionId: selectedCollection.id,
                collectionName: selectedCollection.name,
                collectionType: selectedCollection.type,
                folderId: selectedFolder.id,
                folderName: selectedFolder.name
            },
            
            // ============ SOURCE OBJECT / CUSTOM QUERY ============
            useCustomQuery: isCustomQueryMode,
            customSelectStatement: isCustomQueryMode ? customQuery : null,
            
            // For database objects
            sourceObject: (!isCustomQueryMode && sourceType === 'database_object') ? {
                schemaName: schemaConfig.schemaName,
                objectType: schemaConfig.objectType,
                objectName: schemaConfig.objectName,
                operation: schemaConfig.operation,
                databaseType: actualDatabaseType,
                primaryKeyColumn: schemaConfig.primaryKeyColumn,
                sequenceName: schemaConfig.sequenceName,
                enablePagination: schemaConfig.enablePagination,
                pageSize: schemaConfig.pageSize,
                enableSorting: schemaConfig.enableSorting,
                defaultSortColumn: schemaConfig.defaultSortColumn,
                defaultSortDirection: schemaConfig.defaultSortDirection
            } : null,
            
            // For backward compatibility
            schemaConfig: (!isCustomQueryMode && sourceType === 'database_object') ? {
                ...schemaConfig,
                databaseType: actualDatabaseType
            } : null,
            
            // ============ PARAMETERS ============
            parameters: inParameters.map(p => ({
                id: p.id,
                key: p.key,
                dbColumn: p.dbColumn,
                oracleType: p.oracleType,
                apiType: p.apiType,
                parameterLocation: p.parameterLocation,
                required: p.required,
                description: p.description,
                example: p.example,
                validationPattern: p.validationPattern,
                defaultValue: p.defaultValue,
                inBody: p.parameterLocation === 'body',
                isPrimaryKey: p.isPrimaryKey || false,
                paramMode: p.paramMode || 'IN',
                position: p.position || 0,
                bodyFormat: p.parameterLocation === 'body' ? requestBody.bodyType : null
            })),
            
            // ============ RESPONSE MAPPINGS ============
            responseMappings: outMappings.map(m => ({
                id: m.id,
                apiField: m.apiField,
                dbColumn: m.dbColumn,
                oracleType: m.oracleType,
                apiType: m.apiType,
                format: m.format,
                nullable: m.nullable,
                isPrimaryKey: m.isPrimaryKey || false,
                includeInResponse: m.includeInResponse !== false,
                paramMode: m.paramMode || 'OUT',
                position: m.position || 0
            })),
            
            // ============ REQUEST BODY ============
            requestBody: {
                bodyType: protocolType === 'soap' ? 'soap' : 
                          protocolType === 'graphql' ? 'graphql' : 
                          requestBody.bodyType || 'json',
                sample: requestBody.sample,
                requiredFields: requestBody.requiredFields,
                validateSchema: requestBody.validateSchema,
                maxSize: requestBody.maxSize,
                allowedMediaTypes: requestBody.allowedMediaTypes
            },
            
            // ============ RESPONSE BODY ============
            responseBody: {
                successSchema: responseBody.successSchema,
                errorSchema: responseBody.errorSchema,
                includeMetadata: responseBody.includeMetadata,
                metadataFields: responseBody.metadataFields,
                contentType: responseBody.contentType,
                compression: responseBody.compression
            },
            
            // ============ AUTHENTICATION ============
            authConfig: {
                authType: authConfig.authType,
                apiKeyHeader: authConfig.apiKeyHeader,
                apiKeyValue: authConfig.apiKeyValue,
                apiSecretHeader: authConfig.apiSecretHeader,
                apiSecretValue: authConfig.apiSecretValue,
                jwtToken: authConfig.jwtToken,
                jwtIssuer: authConfig.jwtIssuer,
                basicUsername: authConfig.basicUsername,
                basicPassword: authConfig.basicPassword,
                ipWhitelist: authConfig.ipWhitelist,
                rateLimitRequests: authConfig.rateLimitRequests,
                rateLimitPeriod: authConfig.rateLimitPeriod,
                enableRateLimiting: authConfig.enableRateLimiting,
                corsOrigins: authConfig.corsOrigins,
                auditLevel: authConfig.auditLevel
            },
            
            // ============ HEADERS ============
            headers: headers.map(h => ({
                id: h.id,
                key: h.key,
                value: h.value,
                required: h.required || false,
                description: h.description || '',
                isRequestHeader: true
            })),
            
            // ============ TESTS ============
            tests: {
                testConnection: tests.testConnection,
                testObjectAccess: tests.testObjectAccess,
                testPrivileges: tests.testPrivileges,
                testDataTypes: tests.testDataTypes,
                testNullConstraints: tests.testNullConstraints,
                testUniqueConstraints: tests.testUniqueConstraints,
                testForeignKeyReferences: tests.testForeignKeyReferences,
                testQueryPerformance: tests.testQueryPerformance,
                performanceThreshold: tests.performanceThreshold,
                testWithSampleData: tests.testWithSampleData,
                sampleDataRows: tests.sampleDataRows,
                testProcedureExecution: tests.testProcedureExecution,
                testFunctionReturn: tests.testFunctionReturn,
                testExceptionHandling: tests.testExceptionHandling,
                testSQLInjection: tests.testSQLInjection,
                testAuthentication: tests.testAuthentication,
                testAuthorization: tests.testAuthorization,
                testData: tests.testData || {},
                testQueries: tests.testQueries || []
            },
            
            // ============ SETTINGS ============
            settings: {
                timeout: settings.timeout,
                maxRecords: settings.maxRecords,
                enableLogging: settings.enableLogging,
                logLevel: settings.logLevel,
                enableCaching: settings.enableCaching,
                cacheTtl: settings.cacheTtl,
                generateSwagger: settings.generateSwagger,
                generatePostman: settings.generatePostman,
                generateClientSDK: settings.generateClientSDK,
                enableMonitoring: settings.enableMonitoring,
                enableAlerts: settings.enableAlerts,
                alertEmail: settings.alertEmail,
                enableTracing: settings.enableTracing,
                corsEnabled: settings.corsEnabled
            },
            
            // ============ CONTROL FLAGS ============
            regenerateComponents: true,
            isEditing: isEditing
        };

        console.log('📡 Sending request with protocol:', generateRequest.protocolType);
        console.log('📡 Database type being sent:', generateRequest.databaseType);
        console.log('📡 SOAP Config present:', !!generateRequest.soapConfig);
        console.log('📡 GraphQL Config present:', !!generateRequest.graphqlConfig);
        console.log('📡 File Upload Config present:', !!generateRequest.fileUploadConfig);
        
        let response;
        
        if (isEditing && (selectedObject?.id || selectedObject?.data?.id)) {
            const apiId = selectedObject.data?.id || selectedObject.id;
            console.log('📡 Updating API with ID:', apiId);
            response = await updateApi(authToken, apiId, generateRequest);
        } else {
            console.log('📡 Generating new API');
            response = await generateApi(authToken, generateRequest);
        }
        
        console.log('📥 API response:', response);

        setApiResponse(response);

        if (response.responseCode >= 200 && response.responseCode < 300) {
            const enrichedApiData = {
                ...newApiData,
                ...response.data,
                generatedFiles: response.data?.generatedFiles || newApiData?.generatedFiles,
                isEditing: isEditing,
                // Preserve protocol info in the response
                protocolType: protocolType,
                soapConfig: protocolType === 'soap' ? soapConfig : null,
                graphqlConfig: protocolType === 'graphql' ? graphqlConfig : null,
                fileUploadConfig: fileUploadConfig,
                // CRITICAL: Preserve the database type in the response
                databaseType: actualDatabaseType
            };
            setNewApiData(enrichedApiData);
            
            if (onSave) {
                onSave(enrichedApiData, response);
            }
            
            if (onGenerateAPI) {
                onGenerateAPI(enrichedApiData, response);
            }
        } else {
            console.error('API operation failed:', response);
        }

    } catch (error) {
        console.error('❌ Error:', error);
        setApiResponse({
            responseCode: 500,
            message: error.message || 'Failed to process API request',
            data: null
        });
    } finally {
        setLoadingOpen(false);
        setConfirmationOpen(true);
    }
};

  const [activeTab, setActiveTab] = useState('definition');
  const [loading, setLoading] = useState(false);
  const [validating, setValidating] = useState(false);
  const [validationResult, setValidationResult] = useState(null);

  const [showApiSecret, setShowApiSecret] = useState(false);
  
  // NEW: Track if API code already exists - only relevant for new APIs
  const [apiCodeExists, setApiCodeExists] = useState(false);
  
  // Collection and Folder state
  const [collections, setCollections] = useState(MOCK_COLLECTIONS);
  const [selectedCollection, setSelectedCollection] = useState(null);
  const [folders, setFolders] = useState([]);
  const [selectedFolder, setSelectedFolder] = useState(null);
  const [isAddingNewCollection, setIsAddingNewCollection] = useState(false);
  const [newCollectionName, setNewCollectionName] = useState('');
  const [newCollectionDescription, setNewCollectionDescription] = useState('');
  const [newCollectionType, setNewCollectionType] = useState('core');
  const [showAddFolderModal, setShowAddFolderModal] = useState(false);

  // NEW: Object selector state for dashboard generation
  const [showObjectSelector, setShowObjectSelector] = useState(false);
  const [selectedDbObject, setSelectedDbObject] = useState(null);
  const [objectSelectorError, setObjectSelectorError] = useState(null);

  // Validation errors state
  const [validationErrors, setValidationErrors] = useState({});

  const [showBasicPassword, setShowBasicPassword] = useState(false);

  const [apiDetails, setApiDetails] = useState({
    apiName: '',
    apiCode: '',
    description: '',
    version: '1.0.0',
    status: 'ACTIVE',
    httpMethod: 'GET',
    basePath: '/api/v1',
    endpointPath: '',
    tags: ['default'],
    category: 'general',
    owner: 'CBXDMX',
  });

  // For React 18+, you can also use a ref to ensure body exists
    const [mounted, setMounted] = useState(false);

    useEffect(() => {
      setMounted(true);
      return () => setMounted(false);
    }, []);

  // Add this with your other useState declarations
  const [codeCheckTimeout, setCodeCheckTimeout] = useState(null);

  // Schema & Object Configuration
  const [schemaConfig, setSchemaConfig] = useState({
    schemaName: '',
    objectType: '',
    objectName: '',
    operation: 'SELECT',
    primaryKeyColumn: '',
    sequenceName: '',
    enablePagination: true,
    pageSize: 10,
    enableSorting: true,
    defaultSortColumn: '',
    defaultSortDirection: 'ASC'
  });

  const [parameters, setParameters] = useState([]);
  const [responseMappings, setResponseMappings] = useState([]);
  const [sourceObjectInfo, setSourceObjectInfo] = useState({
    isSynonym: false,
    targetType: null,
    targetName: null,
    targetOwner: null
  });

  // New state for request body configuration
  const [requestBody, setRequestBody] = useState({
    bodyType: 'none',
    sample: null,
    requiredFields: [],
    validateSchema: true,
    maxSize: 1048576, // 1MB
    allowedMediaTypes: ['application/json']
  });

  // New state for response body configuration
  const [responseBody, setResponseBody] = useState({
    successSchema: '',  // Change from the default JSON to empty string
    errorSchema: '{\n  "success": false,\n  "error": {\n    "code": "ERROR_CODE",\n    "message": "Error description",\n    "details": {}\n  }\n}',
    includeMetadata: true,
    metadataFields: ['timestamp', 'apiVersion', 'requestId'],
    contentType: 'application/json',
    compression: 'gzip'
  });

  // Add a state to track the actual database type being used
  const [currentDatabaseType, setCurrentDatabaseType] = useState(databaseType || 'Oracle');



  // Auto-generate request body sample when body parameters change
useEffect(() => {
  // Only auto-generate for REST and GraphQL protocols
  if (protocolType === 'rest' || protocolType === 'graphql') {
    // Don't auto-generate if body type is 'none' or 'binary' or 'soap'
    if (requestBody.bodyType === 'none' || requestBody.bodyType === 'binary' || requestBody.bodyType === 'soap') {
      return;
    }
    
    // Get body parameters
    const bodyParams = getInParameters().filter(p => p.parameterLocation === 'body');
    
    // Don't auto-generate if there are no body parameters
    if (bodyParams.length === 0) {
      // If no body params, set empty sample based on content type
      if (!requestBody.sample || requestBody.sample === '') {
        if (requestBody.bodyType === 'json') {
          handleRequestBodyChange('sample', '{}');
        } else if (requestBody.bodyType === 'xml') {
          handleRequestBodyChange('sample', '<request/>');
        } else if (requestBody.bodyType === 'graphql') {
          const operationName = graphqlConfig.operationName || 'query';
          handleRequestBodyChange('sample', JSON.stringify({
            query: `${graphqlConfig.operationType} {\n  ${operationName} {\n    id\n  }\n}`
          }, null, 2));
        } else if (requestBody.bodyType === 'form-data') {
          handleRequestBodyChange('sample', '');
        } else if (requestBody.bodyType === 'urlencoded') {
          handleRequestBodyChange('sample', '');
        }
      }
      return;
    }
    
    // Check if current sample is empty or default
    const currentSample = requestBody.sample;
    const isDefaultOrEmpty = !currentSample || 
      currentSample === '' ||
      currentSample === '{}' ||
      currentSample === '<request/>' ||
      currentSample === '{\n  "key": "value"\n}';
    
    // Only auto-generate if it's the default/empty sample (not user-modified)
    if (isDefaultOrEmpty) {
      console.log('🔄 Auto-generating request body sample from body parameters...');
      
      if (protocolType === 'graphql') {
        // GraphQL request generation
        const operationName = graphqlConfig.operationName || 'query';
        const operationType = graphqlConfig.operationType || 'query';
        
        let queryString = '';
        if (operationType === 'query') {
          const queryParams = bodyParams.filter(p => p.parameterLocation !== 'body');
          const paramsString = queryParams.length > 0 
            ? `(${queryParams.map(p => `${p.key}: "${p.example || 'value'}"`).join(', ')})` 
            : '';
          
          queryString = `${operationType} {\n  ${operationName}${paramsString} {\n    ${getOutMappings().slice(0, 3).map(m => m.apiField).join('\n    ')}\n  }\n}`;
        } else {
          const inputParams = bodyParams.length > 0 ? bodyParams : getInParameters();
          queryString = `${operationType} {\n  ${operationName}(input: {\n    ${inputParams.slice(0, 3).map(p => `${p.key}: "${p.example || 'value'}"`).join('\n    ')}\n  }) {\n    success\n    message\n    data {\n      ${getOutMappings().slice(0, 3).map(m => m.apiField).join('\n      ')}\n    }\n  }\n}`;
        }
        
        const graphqlRequest = JSON.stringify({
          query: queryString,
          variables: {}
        }, null, 2);
        
        handleRequestBodyChange('sample', graphqlRequest);
        
      } else if (requestBody.bodyType === 'json') {
        // JSON request body generation
        const requestBodyObj = {};
        bodyParams.forEach(param => {
          if (param.apiType === 'integer') {
            requestBodyObj[param.key] = parseInt(param.example) || 123;
          } else if (param.apiType === 'boolean') {
            requestBodyObj[param.key] = param.example === 'true' || false;
          } else if (param.apiType === 'array') {
            requestBodyObj[param.key] = [];
          } else if (param.apiType === 'object') {
            requestBodyObj[param.key] = {};
          } else {
            requestBodyObj[param.key] = param.example || `sample_${param.key}`;
          }
        });
        
        const jsonSample = JSON.stringify(requestBodyObj, null, 2);
        handleRequestBodyChange('sample', jsonSample);
        
      } else if (requestBody.bodyType === 'xml') {
        // XML request body generation
        let xmlSample = `<?xml version="1.0" encoding="UTF-8"?>\n<request>\n`;
        bodyParams.forEach(param => {
          xmlSample += `  <${param.key}>${param.example || `sample_${param.key}`}</${param.key}>\n`;
        });
        xmlSample += `</request>`;
        handleRequestBodyChange('sample', xmlSample);
        
      } else if (requestBody.bodyType === 'form-data') {
        // Form data doesn't need a sample JSON, but we can log
        console.log('Form data request body will be built from parameters');
        
      } else if (requestBody.bodyType === 'urlencoded') {
        // URL encoded doesn't need a sample JSON
        console.log('URL encoded request body will be built from parameters');
      }
    }
  }
}, [parameters, requestBody.bodyType, protocolType, graphqlConfig.operationName, graphqlConfig.operationType]);


// Regenerate request body when parameter apiType or example changes
useEffect(() => {
  // Only run for REST and GraphQL protocols
  if (protocolType === 'rest' || protocolType === 'graphql') {
    // Skip for body types that don't need sample generation
    if (requestBody.bodyType === 'none' || requestBody.bodyType === 'binary' || requestBody.bodyType === 'soap') {
      return;
    }
    
    // Get body parameters (for REST) or all parameters (for GraphQL)
    const bodyParams = protocolType === 'graphql' 
      ? getInParameters()  // GraphQL uses all parameters as arguments
      : getInParameters().filter(p => p.parameterLocation === 'body');
    
    if (bodyParams.length === 0) {
      // No parameters, set empty sample
      if (requestBody.bodyType === 'json') {
        handleRequestBodyChange('sample', '{}');
      } else if (requestBody.bodyType === 'xml') {
        handleRequestBodyChange('sample', '<request/>');
      } else if (requestBody.bodyType === 'graphql') {
        // Keep existing GraphQL sample or generate minimal
        if (!requestBody.sample || requestBody.sample === '') {
          const operationName = graphqlConfig.operationName || 'query';
          const operationType = graphqlConfig.operationType || 'query';
          const queryString = `${operationType} {\n  ${operationName} {\n    id\n  }\n}`;
          handleRequestBodyChange('sample', JSON.stringify({ query: queryString, variables: {} }, null, 2));
        }
      }
      return;
    }
    
    // Check if we need to regenerate (if sample is empty or default)
    const currentSample = requestBody.sample;
    const needsRegeneration = !currentSample || 
      currentSample === '' ||
      currentSample === '{}' ||
      currentSample === '<request/>' ||
      currentSample === '{\n  "key": "value"\n}' ||
      currentSample === '{\n  "query": "...",\n  "variables": {}\n}';
    
    if (needsRegeneration) {
      console.log('🔄 Regenerating request body due to parameter type/example changes...');
      
      if (protocolType === 'graphql') {
        // GraphQL request generation with proper type handling
        const operationName = graphqlConfig.operationName || 'query';
        const operationType = graphqlConfig.operationType || 'query';
        const inParams = getInParameters();
        const outMappingsForQuery = getOutMappings();
        
        let variableDefinitions = '';
        let variableUsages = '';
        let variablesObject = {};
        
        if (inParams.length > 0) {
          // Build variable definitions with proper GraphQL types based on apiType
          variableDefinitions = '(' + inParams.map(p => {
            let graphqlType = 'String';
            switch(p.apiType?.toLowerCase()) {
              case 'integer':
                graphqlType = 'Int';
                break;
              case 'number':
                graphqlType = 'Float';
                break;
              case 'boolean':
                graphqlType = 'Boolean';
                break;
              default:
                graphqlType = 'String';
            }
            if (p.required) {
              graphqlType = `${graphqlType}!`;
            }
            return `$${p.key}: ${graphqlType}`;
          }).join(', ') + ')';
          
          // Use variables in query
          variableUsages = '(' + inParams.map(p => `${p.key}: $${p.key}`).join(', ') + ')';
          
          // Build variables object with properly typed values based on apiType
          inParams.forEach(p => {
            let val = p.example;
            
            // Generate default value if example is empty
            if (!val || val === '') {
              switch(p.apiType?.toLowerCase()) {
                case 'integer':
                  val = 123;
                  break;
                case 'number':
                  val = 123.45;
                  break;
                case 'boolean':
                  val = true;
                  break;
                default:
                  val = 'sample';
              }
            } else {
              // Parse value based on apiType
              switch(p.apiType?.toLowerCase()) {
                case 'integer':
                  val = parseInt(val, 10);
                  if (isNaN(val)) val = 123;
                  break;
                case 'number':
                  val = parseFloat(val);
                  if (isNaN(val)) val = 123.45;
                  break;
                case 'boolean':
                  val = val === 'true' || val === '1' || val === 'yes';
                  break;
                default:
                  // Keep as string
              }
            }
            variablesObject[p.key] = val;
          });
        }
        
        // Build query string
        let queryString = '';
        if (operationType === 'query') {
          if (inParams.length > 0) {
            queryString = `${operationType} ${operationName}${variableDefinitions} {\n  ${operationName}${variableUsages} {\n`;
            if (outMappingsForQuery.length > 0) {
              queryString += outMappingsForQuery.slice(0, 20).map(m => `    ${m.apiField}`).join('\n');
              if (outMappingsForQuery.length > 20) {
                queryString += `\n    # ... ${outMappingsForQuery.length - 20} more fields`;
              }
            } else {
              queryString += `    id\n    createdAt\n    updatedAt`;
            }
            queryString += `\n  }\n}`;
          } else {
            queryString = `${operationType} {\n  ${operationName} {\n`;
            if (outMappingsForQuery.length > 0) {
              queryString += outMappingsForQuery.slice(0, 20).map(m => `    ${m.apiField}`).join('\n');
              if (outMappingsForQuery.length > 20) {
                queryString += `\n    # ... ${outMappingsForQuery.length - 20} more fields`;
              }
            } else {
              queryString += `    id\n    createdAt\n    updatedAt`;
            }
            queryString += `\n  }\n}`;
          }
        } else {
          queryString = `${operationType} ${operationName}${variableDefinitions} {\n  ${operationName}${variableUsages} {\n    success\n    message\n    data {\n`;
          if (outMappingsForQuery.length > 0) {
            queryString += outMappingsForQuery.slice(0, 20).map(m => `      ${m.apiField}`).join('\n');
            if (outMappingsForQuery.length > 20) {
              queryString += `\n      # ... ${outMappingsForQuery.length - 20} more fields`;
            }
          } else {
            queryString += `      id\n      createdAt`;
          }
          queryString += `\n    }\n  }\n}`;
        }
        
        const graphqlRequest = JSON.stringify({
          query: queryString,
          variables: variablesObject
        }, null, 2);
        
        if (requestBody.sample !== graphqlRequest) {
          console.log('✅ Updating GraphQL request body with new parameter types');
          handleRequestBodyChange('sample', graphqlRequest);
        }
        
      } else if (requestBody.bodyType === 'json') {
        // JSON request body generation with proper type handling
        const requestBodyObj = {};
        bodyParams.forEach(p => {
          let value;
          
          if (p.example && p.example !== '') {
            // Parse based on apiType
            switch(p.apiType?.toLowerCase()) {
              case 'integer':
                value = parseInt(p.example, 10);
                if (isNaN(value)) value = 123;
                break;
              case 'number':
                value = parseFloat(p.example);
                if (isNaN(value)) value = 123.45;
                break;
              case 'boolean':
                value = p.example === 'true' || p.example === '1' || p.example === 'yes';
                break;
              case 'array':
                value = [];
                break;
              case 'object':
                value = {};
                break;
              default:
                value = p.example;
            }
          } else {
            // Generate default based on apiType
            switch(p.apiType?.toLowerCase()) {
              case 'integer':
                value = 123;
                break;
              case 'number':
                value = 123.45;
                break;
              case 'boolean':
                value = true;
                break;
              case 'array':
                value = [];
                break;
              case 'object':
                value = {};
                break;
              default:
                value = `sample_${p.key}`;
            }
          }
          requestBodyObj[p.key] = value;
        });
        
        const jsonSample = JSON.stringify(requestBodyObj, null, 2);
        if (requestBody.sample !== jsonSample) {
          console.log('✅ Updating JSON request body with new parameter types');
          handleRequestBodyChange('sample', jsonSample);
        }
        
      } else if (requestBody.bodyType === 'xml') {
        // XML request body generation
        let xmlSample = `<?xml version="1.0" encoding="UTF-8"?>\n<request>\n`;
        bodyParams.forEach(p => {
          let value = p.example || `sample_${p.key}`;
          // For numeric types, don't wrap in quotes
          if (p.apiType === 'integer' || p.apiType === 'number') {
            xmlSample += `  <${p.key}>${value}</${p.key}>\n`;
          } else {
            xmlSample += `  <${p.key}>${value}</${p.key}>\n`;
          }
        });
        xmlSample += `</request>`;
        
        if (requestBody.sample !== xmlSample) {
          console.log('✅ Updating XML request body with new parameter types');
          handleRequestBodyChange('sample', xmlSample);
        }
      }
    }
  }
}, [parameters, requestBody.bodyType, protocolType, graphqlConfig.operationName, graphqlConfig.operationType, getInParameters, getOutMappings]);


// Also auto-generate when switching to Request tab for the first time
const requestTabRef = useRef(false);


// Add this temporarily for debugging
useEffect(() => {
  console.log('🔍 Auto-generation check:', {
    parametersCount: parameters.length,
    responseMappingsCount: responseMappings.length,
    bodyParams: getInParameters().filter(p => p.parameterLocation === 'body').length,
    outMappingsCount: getOutMappings().length,
    protocolType,
    requestBodyBodyType: requestBody.bodyType,
    responseBodySuccessSchema: responseBody.successSchema?.substring(0, 50)
  });
}, [parameters, responseMappings, protocolType]);

useEffect(() => {
  // When switching to Request tab and we haven't auto-generated yet
  if (activeTab === 'request' && !requestTabRef.current && (protocolType === 'rest' || protocolType === 'graphql')) {
    requestTabRef.current = true;
    
    // Skip for body types that don't need sample generation
    if (requestBody.bodyType === 'none' || requestBody.bodyType === 'binary' || requestBody.bodyType === 'soap') {
      return;
    }
    
    // Get body parameters
    const bodyParams = getInParameters().filter(p => p.parameterLocation === 'body');
    
    // Check if we need to generate
    const currentSample = requestBody.sample;
    const isDefaultOrEmpty = !currentSample || 
      currentSample === '' ||
      currentSample === '{}' ||
      currentSample === '<request/>';
    
    if (isDefaultOrEmpty) {
      console.log('🔄 Auto-generating request body sample on Request tab activation...');
      
      if (protocolType === 'graphql') {
        // FIXED: Generate proper GraphQL request with variables
        const inParams = getInParameters();
        const outMappingsForQuery = getOutMappings();
        const operationName = graphqlConfig.operationName || 'query';
        const operationType = graphqlConfig.operationType || 'query';
        
        // Treat ALL parameters as query parameters for GraphQL queries
        const queryParams = inParams;
        let variableDefinitions = '';
        let variableUsages = '';
        let variablesObject = {};
        
        if (queryParams.length > 0) {
          // Build variable definitions
          variableDefinitions = '(' + queryParams.map(p => {
            const graphqlType = getGraphQLType(p.oracleType, !p.required);
            return `$${p.key}: ${graphqlType}`;
          }).join(', ') + ')';
          
          // Use variables in query
          variableUsages = '(' + queryParams.map(p => `${p.key}: $${p.key}`).join(', ') + ')';
          
          // Build variables object
          queryParams.forEach(p => {
            let val = p.example;
            if (!val) {
              if (p.apiType === 'integer') val = 123;
              else if (p.apiType === 'boolean') val = true;
              else val = 'sample';
            }
            if (p.apiType === 'integer') val = parseInt(val) || 123;
            if (p.apiType === 'boolean') val = val === 'true';
            variablesObject[p.key] = val;
          });
        }
        
        // Build query string with ALL response fields
        let queryString = '';
        if (operationType === 'query') {
          if (queryParams.length > 0) {
            queryString = `${operationType} ${operationName}${variableDefinitions} {\n  ${operationName}${variableUsages} {\n`;
            if (outMappingsForQuery.length > 0) {
              queryString += outMappingsForQuery.slice(0, 20).map(m => `    ${m.apiField}`).join('\n');
              if (outMappingsForQuery.length > 20) {
                queryString += `\n    # ... ${outMappingsForQuery.length - 20} more fields`;
              }
            } else {
              queryString += `    id\n    createdAt\n    updatedAt`;
            }
            queryString += `\n  }\n}`;
          } else {
            queryString = `${operationType} {\n  ${operationName} {\n`;
            if (outMappingsForQuery.length > 0) {
              queryString += outMappingsForQuery.slice(0, 20).map(m => `    ${m.apiField}`).join('\n');
              if (outMappingsForQuery.length > 20) {
                queryString += `\n    # ... ${outMappingsForQuery.length - 20} more fields`;
              }
            } else {
              queryString += `    id\n    createdAt\n    updatedAt`;
            }
            queryString += `\n  }\n}`;
          }
        }
        
        const graphqlRequest = JSON.stringify({
          query: queryString,
          variables: variablesObject
        }, null, 2);
        
        console.log('📝 Auto-generated GraphQL request with variables and all mappings:', graphqlRequest);
        handleRequestBodyChange('sample', graphqlRequest);
        
      } else if (requestBody.bodyType === 'json' && bodyParams.length > 0) {
        const requestBodyObj = {};
        bodyParams.forEach(param => {
          if (param.apiType === 'integer') {
            requestBodyObj[param.key] = parseInt(param.example) || 123;
          } else if (param.apiType === 'boolean') {
            requestBodyObj[param.key] = param.example === 'true' || false;
          } else {
            requestBodyObj[param.key] = param.example || `sample_${param.key}`;
          }
        });
        handleRequestBodyChange('sample', JSON.stringify(requestBodyObj, null, 2));
        
      } else if (requestBody.bodyType === 'xml' && bodyParams.length > 0) {
        let xmlSample = `<?xml version="1.0" encoding="UTF-8"?>\n<request>\n`;
        bodyParams.forEach(param => {
          xmlSample += `  <${param.key}>${param.example || `sample_${param.key}`}</${param.key}>\n`;
        });
        xmlSample += `</request>`;
        handleRequestBodyChange('sample', xmlSample);
      }
    }
  }
}, [activeTab, parameters, requestBody.bodyType, protocolType, graphqlConfig.operationName, graphqlConfig.operationType]);



useEffect(() => {
  // CRITICAL: Skip database type syncing when editing an existing API
  // This prevents overwriting the API's stored database type
  if (isEditing) {
    console.log('ℹ️ Edit mode - skipping database type sync from selected object');
    return;
  }
  
  if (selectedDbObject?.databaseType) {
    const dbType = selectedDbObject.databaseType === 'postgresql' ? 'postgresql' : 'oracle';
    if (currentDatabaseType !== dbType) {
      console.log('🔄 Syncing database type from selected object:', dbType);
      setCurrentDatabaseType(dbType);
    }
  }
}, [selectedDbObject, isEditing]);


// Reset the request tab ref when the modal closes
useEffect(() => {
  if (!isOpen) {
    requestTabRef.current = false;
  }
}, [isOpen]);

// Also auto-generate when parameters are loaded from an object (for request body)
useEffect(() => {
  // When we've loaded parameters/mappings from a database object (not editing)
  if ((parameters.length > 0) && !requestTabRef.current) {
    // Small delay to ensure everything is loaded
    const timer = setTimeout(() => {
      // Only auto-generate for body types that support samples
      if (requestBody.bodyType === 'none' || requestBody.bodyType === 'binary' || requestBody.bodyType === 'soap') {
        return;
      }
      
      const bodyParams = getInParameters().filter(p => p.parameterLocation === 'body');
      
      if (bodyParams.length > 0) {
        const currentSample = requestBody.sample;
        const isDefaultOrEmpty = !currentSample || 
          currentSample === '' ||
          currentSample === '{}' ||
          currentSample === '<request/>';
        
        if (isDefaultOrEmpty) {
          console.log('🔄 Auto-generating request body sample after object load...');
          
          if (protocolType === 'graphql') {
            const operationName = graphqlConfig.operationName || 'query';
            const operationType = graphqlConfig.operationType || 'query';
            
            let queryString = '';
            if (operationType === 'query') {
              const queryParams = bodyParams.filter(p => p.parameterLocation !== 'body');
              const paramsString = queryParams.length > 0 
                ? `(${queryParams.map(p => `${p.key}: "${p.example || 'value'}"`).join(', ')})` 
                : '';
              
              queryString = `${operationType} {\n  ${operationName}${paramsString} {\n    ${getOutMappings().slice(0, 3).map(m => m.apiField).join('\n    ')}\n  }\n}`;
            } else {
              const inputParams = bodyParams.length > 0 ? bodyParams : getInParameters();
              queryString = `${operationType} {\n  ${operationName}(input: {\n    ${inputParams.slice(0, 3).map(p => `${p.key}: "${p.example || 'value'}"`).join('\n    ')}\n  }) {\n    success\n    message\n    data {\n      ${getOutMappings().slice(0, 3).map(m => m.apiField).join('\n      ')}\n    }\n  }\n}`;
            }
            
            const graphqlRequest = JSON.stringify({
              query: queryString,
              variables: {}
            }, null, 2);
            
            handleRequestBodyChange('sample', graphqlRequest);
            
          } else if (requestBody.bodyType === 'json') {
            const requestBodyObj = {};
            bodyParams.forEach(param => {
              if (param.apiType === 'integer') {
                requestBodyObj[param.key] = parseInt(param.example) || 123;
              } else if (param.apiType === 'boolean') {
                requestBodyObj[param.key] = param.example === 'true' || false;
              } else {
                requestBodyObj[param.key] = param.example || `sample_${param.key}`;
              }
            });
            handleRequestBodyChange('sample', JSON.stringify(requestBodyObj, null, 2));
            
          } else if (requestBody.bodyType === 'xml') {
            let xmlSample = `<?xml version="1.0" encoding="UTF-8"?>\n<request>\n`;
            bodyParams.forEach(param => {
              xmlSample += `  <${param.key}>${param.example || `sample_${param.key}`}</${param.key}>\n`;
            });
            xmlSample += `</request>`;
            handleRequestBodyChange('sample', xmlSample);
          }
        }
      }
    }, 500);
    
    return () => clearTimeout(timer);
  }
}, [isEditing, parameters.length, requestBody.bodyType, protocolType]);


// Add this useEffect to reset validation when switching source types
useEffect(() => {
  // Clear validation errors when source type changes
  setValidationErrors(prev => ({
    ...prev,
    customQuery: null,
    schemaName: null,
    objectName: null
  }));
}, [sourceType, isCustomQuery, isEditingCustomQuery]);

// ============ FIX: Initialize modal when opened with custom query data ============
useEffect(() => {
  if (isOpen && customQueryText && customQueryText.trim() !== '') {
    console.log('🚀 AutoAPIGeneratorModal - Initializing with custom query data');
    console.log('📝 Custom query length:', customQueryText.length);
    console.log('📊 Extracted params count:', extractedParams.length);
    
    // Set source type to custom_query
    setSourceType('custom_query');
    setIsCustomQuery(true);
    setIsEditingCustomQuery(true);
    setCustomQuery(customQueryText);
    setOriginalCustomQuery(customQueryText);
    
    // Set database type - use the one passed from QueryEditorModal or default to PostgreSQL
    const dbType = databaseType || 'postgresql';
    const normalizedDbType = dbType.toLowerCase() === 'postgresql' ? 'postgresql' : 'oracle';
    setCurrentDatabaseType(normalizedDbType);
    console.log('📦 Database type set to:', normalizedDbType);
    
    // Set extracted parameters if provided
    if (extractedParams && extractedParams.length > 0) {
      console.log('📊 Setting extracted parameters:', extractedParams);
      
      const paramsWithIds = extractedParams.map((param, index) => ({
        id: `param-${Date.now()}-${index}`,
        key: param.key || param.parameterName,
        dbColumn: param.dbColumn || param.key || param.parameterName,
        oracleType: param.dataType || 'VARCHAR2',
        apiType: 'string',
        parameterLocation: param.parameterLocation || 'query',
        required: param.required !== undefined ? param.required : true,
        description: param.description || `Parameter: ${param.key || param.parameterName}`,
        example: param.example || '',
        validationPattern: param.validationPattern || '',
        defaultValue: param.defaultValue || '',
        inBody: false,
        isPrimaryKey: false,
        paramMode: 'IN'
      }));
      
      setParameters(paramsWithIds);
      console.log('✅ Set parameters count:', paramsWithIds.length);
    } else {
      // Try to extract parameters from the custom query
      console.log('📊 No extracted params provided, attempting to extract from query');
      const paramMatches = customQueryText.match(/:\w+/g) || [];
      const uniqueParams = [...new Set(paramMatches)];
      
      if (uniqueParams.length > 0) {
        const extractedParamList = uniqueParams.map((param, idx) => ({
          id: `param-${Date.now()}-${idx}`,
          key: param.substring(1),
          dbColumn: param.substring(1),
          oracleType: 'VARCHAR2',
          apiType: 'string',
          parameterLocation: 'query',
          required: true,
          description: `Parameter: ${param.substring(1)}`,
          example: '',
          validationPattern: '',
          defaultValue: '',
          inBody: false,
          isPrimaryKey: false,
          paramMode: 'IN'
        }));
        setParameters(extractedParamList);
        console.log('✅ Extracted and set parameters from query:', extractedParamList.length);
      }
    }
    
    // Generate a meaningful API name from the query
    let generatedApiName = 'Custom Query API';
    let generatedApiCode = 'CUSTOM_QUERY_API';
    
    // Try to extract table name from FROM clause
    const fromMatch = customQueryText.match(/FROM\s+([^\s,;]+)/i);
    if (fromMatch && fromMatch[1]) {
      let tableName = fromMatch[1];
      // Remove schema prefix if present
      if (tableName.includes('.')) {
        tableName = tableName.split('.').pop();
      }
      // Convert to proper case
      tableName = tableName.charAt(0).toUpperCase() + tableName.slice(1).toLowerCase();
      generatedApiName = `Get ${tableName} Data`;
      generatedApiCode = `GET_${tableName.toUpperCase().replace(/[^A-Z0-9]/g, '_')}`;
    }
    
    // Determine query type
    const queryLower = customQueryText.toLowerCase();
    if (queryLower.includes('select')) {
      generatedApiName = generatedApiName || 'Query Data';
    } else if (queryLower.includes('insert')) {
      generatedApiName = 'Insert Data';
      generatedApiCode = 'INSERT_DATA';
    } else if (queryLower.includes('update')) {
      generatedApiName = 'Update Data';
      generatedApiCode = 'UPDATE_DATA';
    } else if (queryLower.includes('delete')) {
      generatedApiName = 'Delete Data';
      generatedApiCode = 'DELETE_DATA';
    }
    
    // Set API details
    setApiDetails(prev => ({
      ...prev,
      apiName: prev.apiName || generatedApiName,
      apiCode: prev.apiCode || generatedApiCode,
      description: prev.description || `API generated from custom query: ${customQueryText.substring(0, 100)}...`,
      httpMethod: 'POST', // Custom queries typically use POST
      endpointPath: prev.endpointPath || `/${generatedApiCode.toLowerCase().replace(/_/g, '-')}`
    }));
    
    // Clear any validation errors
    setValidationErrors(prev => ({ ...prev, customQuery: null }));
    
    console.log('✅ AutoAPIGeneratorModal initialization complete for custom query');
  }
}, [isOpen, customQueryText, extractedParams, databaseType]);



// Single useEffect for response schema regeneration
useEffect(() => {
  // Only auto-generate for REST and GraphQL protocols
  if (protocolType === 'rest' || protocolType === 'graphql') {
    const outMappings = getOutMappings();
    if (outMappings.length === 0) return;
    
    console.log('🔄 Response mappings changed, regenerating schema...');
    console.log('📊 Mappings count:', outMappings.length);
    console.log('📊 Mapping types:', outMappings.map(m => ({ field: m.apiField, type: m.apiType })));
    
    // Call the regenerateResponseSchema function
    regenerateResponseSchema();
  }
}, [responseMappings, protocolType, graphqlConfig.operationName, apiDetails.version]);


// Add a ref for request tab
const requestTabInitializedRef = useRef(false);

// Force refresh when switching to Request tab (similar to Response tab)
useEffect(() => {
  if (activeTab === 'request' && !requestTabInitializedRef.current) {
    requestTabInitializedRef.current = true;
    
    console.log('🔄 Request tab activated - forcing request body refresh');
    
    if (protocolType === 'rest') {
      const bodyParams = getInParameters().filter(p => p.parameterLocation === 'body');
      if (bodyParams.length > 0 && requestBody.bodyType !== 'none' && requestBody.bodyType !== 'binary') {
        if (requestBody.bodyType === 'json') {
          const requestBodyObj = {};
          bodyParams.forEach(param => {
            let val = param.example;
            if (!val || val === '') {
              switch(param.apiType?.toLowerCase()) {
                case 'integer': val = 123; break;
                case 'boolean': val = true; break;
                default: val = `sample_${param.key}`;
              }
            }
            requestBodyObj[param.key] = val;
          });
          const freshJson = JSON.stringify(requestBodyObj, null, 2);
          if (requestBody.sample !== freshJson) {
            handleRequestBodyChange('sample', freshJson);
            setRequestSchemaVersion(prev => prev + 1); // ADD THIS LINE
          }
        }
      }
    }
  }
}, [activeTab, protocolType, parameters, getInParameters, requestBody.bodyType]);


// Reset request tab ref when modal closes
useEffect(() => {
  if (!isOpen) {
    requestTabInitializedRef.current = false;
  }
}, [isOpen]);



// Track if parameters have been auto-extracted
const parametersExtractedRef = useRef(false);

// Auto-extract parameters when parameters tab is first activated and we have a database object
useEffect(() => {
  // ============ CRITICAL FIX: Skip if editing mode ============
  if (isEditing) {
    console.log('ℹ️ Edit mode detected - skipping auto-extract on Parameters tab');
    return;
  }
  
  if (activeTab === 'parameters' && !parametersExtractedRef.current && selectedDbObject) {
    parametersExtractedRef.current = true;
    
    console.log('🔄 Parameters tab activated - ensuring parameters are extracted from object');
    
    // If parameters are empty or very few compared to expected, re-extract
    const expectedParamCount = selectedDbObject.parameters?.length || selectedDbObject.columns?.length || 0;
    
    if (parameters.length === 0 && expectedParamCount > 0) {
      console.log('📊 Parameters missing, extracting from object...');
      populateFormFromObject(selectedDbObject, true);
    }
    
    // Also extract response mappings if they're missing
    if (responseMappings.length === 0 && (selectedDbObject.columns?.length > 0 || selectedDbObject.parameters?.length > 0)) {
      console.log('📊 Response mappings missing, extracting from object...');
      populateFormFromObject(selectedDbObject, true);
    }
  }
}, [activeTab, selectedDbObject, parameters.length, responseMappings.length, isEditing]); // Add isEditing to dependencies

// Reset parameters extraction ref when modal closes or object changes
useEffect(() => {
  if (!isOpen || selectedDbObject) {
    // Only reset if we're not opening with the same object
    parametersExtractedRef.current = false;
  }
}, [isOpen, selectedDbObject?.id]);


// Force SOAP request body to update when parameters change
useEffect(() => {
  if (protocolType === 'soap' && parameters.length > 0) {
    // Generate fresh SOAP envelope from current parameters
    const freshSoapEnvelope = generateSOAPEnvelope();
    
    // Update the request body sample
    if (requestBody.sample !== freshSoapEnvelope) {
      console.log('🔄 Updating SOAP request body due to parameter changes');
      handleRequestBodyChange('sample', freshSoapEnvelope);
    }
  }
}, [parameters, protocolType, soapConfig.version, soapConfig.bindingStyle, soapConfig.namespace, soapConfig.soapAction]);


// Force REST request body to update when parameters change
useEffect(() => {
  if (protocolType === 'rest') {
    // Skip if body type is 'none' or 'binary' or 'soap' or 'graphql'
    if (requestBody.bodyType === 'none' || requestBody.bodyType === 'binary') {
      return;
    }
    
    // Determine which parameters to use
    let paramsToUse;
    
    // For custom queries (or when body type is not 'none'), use ALL parameters
    // This ensures query parameters are included in the request body for custom queries
    const isCustomQueryMode = sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery;
    
    if (isCustomQueryMode || requestBody.bodyType === 'json' || requestBody.bodyType === 'xml') {
      // For custom queries, use ALL parameters since they'll be sent in the request body
      paramsToUse = getInParameters();
      console.log('📊 Using ALL parameters for custom query request body:', paramsToUse.length);
    } else {
      // For regular APIs, only use body parameters
      paramsToUse = getInParameters().filter(p => p.parameterLocation === 'body');
    }
    
    // Only regenerate if there are parameters
    if (paramsToUse.length > 0) {
      let freshRequestBody = '';
      
      if (requestBody.bodyType === 'json') {
        const requestBodyObj = {};
        paramsToUse.forEach(param => {
          let value;
          const example = param.example;
          
          if (example && example !== '') {
            // Parse based on apiType
            switch(param.apiType?.toLowerCase()) {
              case 'integer':
                value = parseInt(example, 10);
                if (isNaN(value)) value = 123;
                break;
              case 'number':
                value = parseFloat(example);
                if (isNaN(value)) value = 123.45;
                break;
              case 'boolean':
                value = example === 'true' || example === '1' || example === 'yes';
                break;
              case 'array':
                try {
                  const parsed = JSON.parse(example);
                  value = Array.isArray(parsed) ? parsed : [];
                } catch {
                  value = [];
                }
                break;
              case 'object':
                try {
                  const parsed = JSON.parse(example);
                  value = typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {};
                } catch {
                  value = {};
                }
                break;
              default:
                value = example;
            }
          } else {
            // Generate default based on apiType
            switch(param.apiType?.toLowerCase()) {
              case 'integer':
                value = 123;
                break;
              case 'number':
                value = 123.45;
                break;
              case 'boolean':
                value = true;
                break;
              case 'array':
                value = [];
                break;
              case 'object':
                value = {};
                break;
              default:
                value = `sample_${param.key}`;
            }
          }
          requestBodyObj[param.key] = value;
        });
        freshRequestBody = JSON.stringify(requestBodyObj, null, 2);
      } else if (requestBody.bodyType === 'xml') {
        let xmlSample = `<?xml version="1.0" encoding="UTF-8"?>\n<request>\n`;
        paramsToUse.forEach(param => {
          let value = param.example || `sample_${param.key}`;
          if (param.apiType === 'array' || param.apiType === 'object') {
            value = '';
          }
          xmlSample += `  <${param.key}>${value}</${param.key}>\n`;
        });
        xmlSample += `</request>`;
        freshRequestBody = xmlSample;
      }
      
      // Update the request body sample
      if (freshRequestBody && requestBody.sample !== freshRequestBody) {
        console.log('🔄 Updating REST request body due to parameter API type/example change', {
          bodyType: requestBody.bodyType,
          paramsCount: paramsToUse.length,
          sample: freshRequestBody.substring(0, 100)
        });
        handleRequestBodyChange('sample', freshRequestBody);
        // YOU NEED TO ADD THIS LINE:
        setRequestSchemaVersion(prev => prev + 1);
      }
    } else if (paramsToUse.length === 0 && requestBody.bodyType !== 'none' && requestBody.bodyType !== 'binary') {
      // No parameters, set empty sample
      let emptySample = '';
      if (requestBody.bodyType === 'json') {
        emptySample = '{}';
      } else if (requestBody.bodyType === 'xml') {
        emptySample = '<request/>';
      }
      
      if (requestBody.sample !== emptySample) {
        console.log('🔄 Setting empty REST request body (no parameters)');
        handleRequestBodyChange('sample', emptySample);
      }
    }
  }
}, [parameters, getInParameters, protocolType, requestBody.bodyType, sourceType, isCustomQuery, isEditingCustomQuery]);



// Force GraphQL request body to update when parameters or mappings change
useEffect(() => {
  if (protocolType === 'graphql') {
    console.log('🔄 Generating GraphQL request body for UI display...');
    
    const operationName = graphqlConfig.operationName || 'query';
    const operationType = graphqlConfig.operationType || 'query';
    
    // Get IN parameters directly from the parameters state
    const inParams = parameters.filter(p => !p.paramMode || p.paramMode === 'IN');
    // Get OUT mappings directly from responseMappings state
    const outMappingsForQuery = responseMappings.filter(m => 
      (m.paramMode === 'OUT' || m.paramMode === 'IN/OUT' || m.paramMode === 'IN_OUT' || !m.paramMode)
    );
    
    console.log('📊 IN params count:', inParams.length);
    console.log('📊 OUT mappings count:', outMappingsForQuery.length);
    console.log('📊 IN params details:', inParams.map(p => ({ key: p.key, apiType: p.apiType, example: p.example })));
    console.log('📊 OUT mappings details:', outMappingsForQuery.map(m => m.apiField));
    
    // For GraphQL queries, treat ALL parameters as query parameters
    const queryParams = inParams;
    
    let variableDefinitions = '';
    let variableUsages = '';
    let variablesObject = {};
    
    if (queryParams.length > 0) {
      // Build variable definitions
      variableDefinitions = '(' + queryParams.map(p => {
        const graphqlType = getGraphQLType(p.oracleType, !p.required);
        return `$${p.key}: ${graphqlType}`;
      }).join(', ') + ')';
      
      // Use variables in query
      variableUsages = '(' + queryParams.map(p => `${p.key}: $${p.key}`).join(', ') + ')';
      
      // Build variables object from parameter examples
      queryParams.forEach(p => {
        let val = p.example;
        if (!val || val === '') {
          if (p.apiType === 'integer') val = 123;
          else if (p.apiType === 'boolean') val = true;
          else val = 'sample';
        }
        if (p.apiType === 'integer') val = parseInt(val) || 123;
        if (p.apiType === 'boolean') val = val === 'true';
        variablesObject[p.key] = val;
      });
      
      console.log('📊 Variables object:', variablesObject);
    }
    
    // Build the query string with ALL response fields
    let queryString = '';
    if (operationType === 'query') {
      if (queryParams.length > 0) {
        queryString = `${operationType} ${operationName}${variableDefinitions} {\n  ${operationName}${variableUsages} {\n`;
        if (outMappingsForQuery.length > 0) {
          queryString += outMappingsForQuery.map(m => `    ${m.apiField}`).join('\n');
        } else {
          queryString += `    id\n    createdAt\n    updatedAt`;
        }
        queryString += `\n  }\n}`;
      } else {
        queryString = `${operationType} {\n  ${operationName} {\n`;
        if (outMappingsForQuery.length > 0) {
          queryString += outMappingsForQuery.map(m => `    ${m.apiField}`).join('\n');
        } else {
          queryString += `    id\n    createdAt\n    updatedAt`;
        }
        queryString += `\n  }\n}`;
      }
    }
    
    const freshGraphQLRequest = JSON.stringify({
      query: queryString,
      variables: Object.keys(variablesObject).length > 0 ? variablesObject : {}
    }, null, 2);
    
    console.log('📝 Generated GraphQL request for UI:', freshGraphQLRequest);
    
    // DIRECTLY update the requestBody sample
    // This bypasses handleRequestBodyChange to ensure it updates
    setRequestBody(prev => {
      if (prev.sample !== freshGraphQLRequest) {
        console.log('✅ Updating requestBody.sample');
        return { ...prev, sample: freshGraphQLRequest };
      }
      return prev;
    });
  }
}, [parameters, responseMappings, protocolType, graphqlConfig.operationName, graphqlConfig.operationType]);

// Also auto-generate when switching to Response tab for the first time
const responseTabRef = useRef(false);

useEffect(() => {
  // When switching to Response tab and we haven't auto-generated yet
  if (activeTab === 'response' && !responseTabRef.current && (protocolType === 'rest' || protocolType === 'graphql')) {
    responseTabRef.current = true;
    
    // Check if we need to generate
    const currentSchema = responseBody.successSchema;
    const isDefaultOrEmpty = !currentSchema || 
      currentSchema === '{\n  "success": true,\n  "data": {},\n  "message": "Request processed successfully"\n}' ||
      currentSchema === '';
    
    if (isDefaultOrEmpty && getOutMappings().length > 0) {
      console.log('🔄 Auto-generating sample response on Response tab activation...');
      
      if (protocolType === 'graphql') {
        const operationName = graphqlConfig.operationName || 'operation';
        const responseData = {};
        getOutMappings().slice(0, 50).forEach(mapping => {
          if (mapping.apiType === 'integer') {
            responseData[mapping.apiField] = 123;
          } else if (mapping.apiType === 'boolean') {
            responseData[mapping.apiField] = true;
          } else if (mapping.format === 'date-time') {
            responseData[mapping.apiField] = '2024-01-01T00:00:00Z';
          } else {
            responseData[mapping.apiField] = mapping.apiField === 'id' ? 1 : 'sample';
          }
        });
        
        const graphqlSuccess = JSON.stringify({
          data: { [operationName]: responseData }
        }, null, 2);
        
        handleResponseBodyChange('successSchema', graphqlSuccess);
      } else {
        const sampleData = {};
        getOutMappings().slice(0, 50).forEach(mapping => {
          if (mapping.apiType === 'integer') {
            sampleData[mapping.apiField] = 123;
          } else if (mapping.apiType === 'boolean') {
            sampleData[mapping.apiField] = true;
          } else if (mapping.format === 'date-time') {
            sampleData[mapping.apiField] = '2024-01-01T00:00:00Z';
          } else if (mapping.apiType === 'array') {
            sampleData[mapping.apiField] = [];
          } else if (mapping.apiType === 'object') {
            sampleData[mapping.apiField] = {};
          } else {
            sampleData[mapping.apiField] = mapping.apiField === 'id' ? 1 : 'sample';
          }
        });
        
        const restSuccess = JSON.stringify({
          success: true,
          data: sampleData,
          message: 'Request processed successfully',
          metadata: {
            timestamp: '{{timestamp}}',
            apiVersion: apiDetails.version,
            requestId: '{{requestId}}'
          }
        }, null, 2);
        
        console.log('📊 Setting success schema to:', restSuccess);
        console.log('📊 Current success schema is:', responseBody.successSchema);
        console.log('📊 Are they different?', restSuccess !== responseBody.successSchema);

        setResponseBody(prev => ({ ...prev, successSchema: restSuccess }));
        setResponseSchemaVersion(prev => prev + 1);
        
        handleResponseBodyChange('successSchema', restSuccess);
      }
    }
  }
}, [activeTab, responseMappings, protocolType, graphqlConfig.operationName, apiDetails.version]);

// Reset the response tab ref when the modal closes
useEffect(() => {
  if (!isOpen) {
    responseTabRef.current = false;
  }
}, [isOpen]);

// Also auto-generate when parameters or mappings are loaded from an object
useEffect(() => {
  // When we've loaded parameters/mappings from a database object (not editing)
  if ((parameters.length > 0 || responseMappings.length > 0) && !responseTabRef.current) {
    // Small delay to ensure everything is loaded
    const timer = setTimeout(() => {
      if (getOutMappings().length > 0) {
        const currentSchema = responseBody.successSchema;
        const isDefaultOrEmpty = !currentSchema || 
          currentSchema === '{\n  "success": true,\n  "data": {},\n  "message": "Request processed successfully"\n}' ||
          currentSchema === '';
        
        if (isDefaultOrEmpty) {
          console.log('🔄 Auto-generating sample response after object load...');
          
          if (protocolType === 'graphql') {
            const operationName = graphqlConfig.operationName || 'operation';
            const responseData = {};
            getOutMappings().slice(0, 50).forEach(mapping => {
              if (mapping.apiType === 'integer') {
                responseData[mapping.apiField] = 123;
              } else if (mapping.apiType === 'boolean') {
                responseData[mapping.apiField] = true;
              } else if (mapping.format === 'date-time') {
                responseData[mapping.apiField] = '2024-01-01T00:00:00Z';
              } else {
                responseData[mapping.apiField] = mapping.apiField === 'id' ? 1 : 'sample';
              }
            });
            
            const graphqlSuccess = JSON.stringify({
              data: { [operationName]: responseData }
            }, null, 2);
            
            handleResponseBodyChange('successSchema', graphqlSuccess);
          } else {
            const sampleData = {};
            getOutMappings().slice(0, 50).forEach(mapping => {
              if (mapping.apiType === 'integer') {
                sampleData[mapping.apiField] = 123;
              } else if (mapping.apiType === 'boolean') {
                sampleData[mapping.apiField] = true;
              } else if (mapping.format === 'date-time') {
                sampleData[mapping.apiField] = '2024-01-01T00:00:00Z';
              } else if (mapping.apiType === 'array') {
                sampleData[mapping.apiField] = [];
              } else if (mapping.apiType === 'object') {
                sampleData[mapping.apiField] = {};
              } else {
                sampleData[mapping.apiField] = mapping.apiField === 'id' ? 1 : 'sample';
              }
            });
            
            const restSuccess = JSON.stringify({
              success: true,
              data: sampleData,
              message: 'Request processed successfully',
              metadata: {
                timestamp: '{{timestamp}}',
                apiVersion: apiDetails.version,
                requestId: '{{requestId}}'
              }
            }, null, 2);
            
            handleResponseBodyChange('successSchema', restSuccess);
          }
        }
      }
    }, 500);
    
    return () => clearTimeout(timer);
  }
}, [isEditing, parameters.length, responseMappings.length]);


  // Update currentDatabaseType when selectedDbObject changes
  useEffect(() => {
    if (selectedDbObject?.databaseType) {
      setCurrentDatabaseType(selectedDbObject.databaseType);
    } else if (databaseType) {
      setCurrentDatabaseType(databaseType);
    }
  }, [selectedDbObject, databaseType]);

  // Simplified authentication configuration (only 4 types)
  const [authConfig, setAuthConfig] = useState({
    authType: 'none',
    
    // API Key + Secret fields
    apiKeyHeader: 'X-API-Key',
    apiKeyValue: '',
    apiSecretHeader: 'X-API-Secret',
    apiSecretValue: '',
    
    // JWT fields
    jwtToken: '',
    jwtIssuer: 'api.example.com',
    
    // Basic Auth fields
    basicUsername: '',
    basicPassword: '',
    
    // Common fields
    ipWhitelist: '',
    rateLimitRequests: 100,
    rateLimitPeriod: 'minute',
    enableRateLimiting: false,
    corsOrigins: ['*'],
    auditLevel: 'standard'
  });

  // Headers
  const [headers, setHeaders] = useState([
    { id: '1', key: 'Content-Type', value: 'application/json', required: true, description: 'Response content type' },
    { id: '2', key: 'Cache-Control', value: 'no-cache', required: false, description: 'Cache control header' }
  ]);

  // Database-focused test configuration
  const [tests, setTests] = useState({
    // Database connectivity tests
    testConnection: true,
    testObjectAccess: true,
    testPrivileges: true,
    
    // Data validation tests
    testDataTypes: true,
    testNullConstraints: true,
    testUniqueConstraints: false,
    testForeignKeyReferences: false,
    
    // Performance tests
    testQueryPerformance: true,
    performanceThreshold: 1000,
    testWithSampleData: true,
    sampleDataRows: 10,
    
    // PL/SQL specific tests (for procedures/functions)
    testProcedureExecution: true,
    testFunctionReturn: true,
    testExceptionHandling: true,
    
    // Security tests
    testSQLInjection: true,
    testAuthentication: true,
    testAuthorization: true,
    
    testData: {},
    testQueries: []
  });

  const [settings, setSettings] = useState({
    timeout: 30000,
    maxRecords: 1000,
    enableLogging: true,
    logLevel: 'INFO',
    enableCaching: false,
    cacheTtl: 300,
    generateSwagger: true,
    generatePostman: true,
    generateClientSDK: true,
    enableMonitoring: true,
    enableAlerts: false,
    alertEmail: '',
    enableTracing: false,
    corsEnabled: true
  });

  const [generatedCode, setGeneratedCode] = useState('');
  const [previewMode, setPreviewMode] = useState('json');

  // State for modals
  const [previewOpen, setPreviewOpen] = useState(false);
  const [loadingOpen, setLoadingOpen] = useState(false);
  const [confirmationOpen, setConfirmationOpen] = useState(false);
  const [newApiData, setNewApiData] = useState(null);
  const [apiResponse, setApiResponse] = useState(null);

  // ==================== HANDLER FUNCTIONS ====================

  // Add this with your other useState declarations (around line 2200)
const [protocolConfigs, setProtocolConfigs] = useState({
  rest: {
    httpMethod: 'GET',
    bodyType: 'none',
    endpointPath: '',
    basePath: '/api/v1',
    // Add any other REST-specific fields
  },
  soap: {
    httpMethod: 'POST',
    bodyType: 'soap',
    soapConfig: {
      version: '1.1',
      bindingStyle: 'document',
      encodingStyle: 'literal',
      soapAction: '',
      wsdlUrl: '',
      namespace: 'http://tempuri.org/',
      serviceName: '',
      portName: '',
      useAsyncPattern: false,
      includeMtom: false,
      soapHeaderElements: []
    }
  },
  graphql: {
    httpMethod: 'POST',
    bodyType: 'graphql',
    graphqlConfig: {
      operationType: 'query',
      operationName: '',
      schema: '',
      enableIntrospection: true,
      enablePersistedQueries: false,
      maxQueryDepth: 10,
      enableBatching: false,
      subscriptionsEnabled: false,
      customDirectives: []
    }
  }
});


const handleProtocolChange = (protocol) => {
  // Step 1: Save current configuration for the current protocol
  const currentConfig = {
    httpMethod: apiDetails.httpMethod,
    bodyType: requestBody.bodyType,
    endpointPath: apiDetails.endpointPath,
    basePath: apiDetails.basePath,
    ...(protocolType === 'soap' && { soapConfig }),
    ...(protocolType === 'graphql' && { graphqlConfig })
  };
  
  // Save to protocolConfigs
  setProtocolConfigs(prev => ({
    ...prev,
    [protocolType]: {
      ...prev[protocolType],
      ...currentConfig
    }
  }));
  
  // Step 2: Load configuration for the new protocol
  const loadConfigForProtocol = () => {
    setProtocolConfigs(prev => {
      const savedConfig = prev[protocol];
      
      // Apply the saved config
      setApiDetails(prevDetails => ({
        ...prevDetails,
        httpMethod: savedConfig?.httpMethod || (protocol === 'rest' ? 'GET' : 'POST'),
        endpointPath: savedConfig?.endpointPath || (protocol === 'rest' ? prevDetails.endpointPath : ''),
        basePath: savedConfig?.basePath || prevDetails.basePath
      }));
      
      setRequestBody(prevBody => ({
        ...prevBody,
        bodyType: savedConfig?.bodyType || (protocol === 'soap' ? 'soap' : protocol === 'graphql' ? 'graphql' : 'none')
      }));
      
      if (protocol === 'soap' && savedConfig?.soapConfig) {
        setSoapConfig(savedConfig.soapConfig);
      } else if (protocol === 'graphql' && savedConfig?.graphqlConfig) {
        setGraphqlConfig(savedConfig.graphqlConfig);
      }
      
      setValidationErrors(prev => ({ 
        ...prev, 
        soapAction: null, 
        serviceName: null, 
        operationName: null, 
        graphqlSchema: null 
      }));
      
      return prev;
    });
  };
  
  // Step 3: Switch protocol and load config
  setProtocolType(protocol);
  loadConfigForProtocol();
  
  // Step 4: Immediately switch to Definition tab
  setActiveTab('definition');
  
  // Step 5: If switching to SOAP protocol, set default SELECT operation
  if (protocol === 'soap') {
    // CRITICAL FIX: Set schemaConfig operation to SELECT for SOAP
    setSchemaConfig(prev => ({
      ...prev,
      operation: 'SELECT'  // Force SELECT for SOAP
    }));
    
    // Set SOAP Action to SELECT by default
    setSoapConfig(prev => ({ 
      ...prev, 
      soapAction: 'SELECT',
      serviceName: generateServiceNameFromSource()
    }));
    
    console.log('🔧 SOAP protocol - operation set to SELECT, not derived from HTTP method');
  }
  
  // Step 6: If switching to GraphQL protocol, set appropriate operation
  if (protocol === 'graphql') {
    // For GraphQL, operation is determined by operation type, not HTTP method
    const graphqlOpType = graphqlConfig.operationType || 'query';
    let operation = 'SELECT';
    if (graphqlOpType === 'query') {
      operation = 'SELECT';
    } else if (graphqlOpType === 'mutation') {
      operation = 'EXECUTE';
    }
    
    setSchemaConfig(prev => ({
      ...prev,
      operation: operation
    }));
    
    console.log('🔧 GraphQL protocol - operation set based on operation type:', operation);
  }
  
  console.log('🔄 Protocol changed to:', protocol, '- Switched to Definition tab');
};

// Helper function to generate service name
const generateServiceNameFromSource = () => {
  if (selectedDbObject?.name) {
    let name = selectedDbObject.name.charAt(0).toUpperCase() + 
               selectedDbObject.name.slice(1).toLowerCase();
    name = name.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
    name = name.replace(/[^a-zA-Z0-9]/g, '');
    if (!name.toLowerCase().endsWith('service')) {
      name = name + 'Service';
    }
    return name;
  }
  if (schemaConfig.objectName) {
    let name = schemaConfig.objectName.charAt(0).toUpperCase() + 
               schemaConfig.objectName.slice(1).toLowerCase();
    name = name.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
    name = name.replace(/[^a-zA-Z0-9]/g, '');
    if (!name.toLowerCase().endsWith('service')) {
      name = name + 'Service';
    }
    return name;
  }
  return 'APIService';
};


// Helper function to get default SOAP action based on object type
const getDefaultSoapActionForProtocol = useCallback(() => {
  // For database object types
  switch(schemaConfig.objectType) {
    case 'TABLE':
      switch(schemaConfig.operation) {
        case 'SELECT': return 'SELECT';
        case 'INSERT': return 'INSERT';
        case 'UPDATE': return 'UPDATE';
        case 'DELETE': return 'DELETE';
        default: return 'SELECT';
      }
    case 'VIEW':
      return 'SELECT';
    case 'PROCEDURE':
      return 'EXECUTE';
    case 'FUNCTION':
      return 'EXECUTE';
    case 'PACKAGE':
      return 'EXECUTE_PROCEDURE';
    default:
      return 'PROCESS';
  }
}, [schemaConfig.objectType, schemaConfig.operation]);

  // Handle SOAP config change
  const handleSoapConfigChange = (field, value) => {
    setSoapConfig(prev => ({ ...prev, [field]: value }));
    setValidationErrors(prev => ({ ...prev, [field]: null }));
  };

  // Handle GraphQL config change
  const handleGraphqlConfigChange = (field, value) => {
    setGraphqlConfig(prev => ({ ...prev, [field]: value }));
    setValidationErrors(prev => ({ ...prev, [field]: null }));
  };

  // Handle collection change
  const handleCollectionChange = (collectionId) => {
    if (collectionId === 'new') {
      setIsAddingNewCollection(true);
      setSelectedCollection(null);
      setFolders([]);
      setSelectedFolder(null);
    } else {
      const collection = collections.find(c => c.id === collectionId);
      setSelectedCollection(collection);
      setFolders(collection.folders);
      setSelectedFolder(null);
      setIsAddingNewCollection(false);
    }
    // Clear validation error for collection
    setValidationErrors(prev => ({ ...prev, collection: null }));
  };

  // Handle folder change
  const handleFolderChange = (folderId) => {
    if (folderId === 'new') {
      if (!selectedCollection) {
        alert('Please select a collection first');
        return;
      }
      setShowAddFolderModal(true);
    } else {
      const folder = folders.find(f => f.id === folderId);
      setSelectedFolder(folder);
      // Clear validation error for folder
      setValidationErrors(prev => ({ ...prev, folder: null }));
    }
  };

  // Handle adding new collection
  const handleAddNewCollection = () => {
    if (!newCollectionName.trim()) return;

    const newCollection = {
      id: `new-${Date.now()}`,
      name: newCollectionName,
      description: newCollectionDescription || `Collection for ${newCollectionName}`,
      type: newCollectionType,
      folders: []
    };

    setCollections([...collections, newCollection]);
    setSelectedCollection(newCollection);
    setFolders([]);
    setSelectedFolder(null);
    setIsAddingNewCollection(false);
    setNewCollectionName('');
    setNewCollectionDescription('');
    setNewCollectionType('core');
    // Clear validation error for collection
    setValidationErrors(prev => ({ ...prev, collection: null }));
  };

  // Handle adding new folder
  const handleAddNewFolder = (folderData) => {
    const newFolder = {
      id: `new-folder-${Date.now()}`,
      name: folderData.name,
      description: folderData.description || `Folder for ${folderData.name}`
    };

    // Update folders in state
    const updatedFolders = [...folders, newFolder];
    setFolders(updatedFolders);
    
    // Update collection's folders
    const updatedCollections = collections.map(c => {
      if (c.id === selectedCollection.id) {
        return { ...c, folders: updatedFolders };
      }
      return c;
    });
    setCollections(updatedCollections);
    
    setSelectedFolder(newFolder);
    setShowAddFolderModal(false);
    // Clear validation error for folder
    setValidationErrors(prev => ({ ...prev, folder: null }));
  };


  useEffect(() => {
  // Only check code availability for new APIs when the form loads
  // AND only if we're not in editing mode
  const checkInitialCode = async () => {
    if (isOpen && !isEditing && apiDetails.apiCode && apiDetails.apiCode.length >= 3) {
      try {
        console.log('🔍 Checking initial API code availability:', apiDetails.apiCode);
        await checkCodeAvailability(apiDetails.apiCode);
      } catch (error) {
        console.error('Error checking code availability on load:', error);
      }
    }
  };
  
  checkInitialCode();
}, [isOpen, isEditing]); // Remove apiDetails.apiCode from dependencies to prevent loops

  // Update the handleApiDetailChange function (around line 1535)
const handleApiDetailChange = (field, value) => {
  let processedValue = value;
    
  if (field === 'apiName') {
    processedValue = value.toUpperCase();
  } else if (field === 'apiCode') {
    // Convert to uppercase AND replace spaces with underscores
    processedValue = value.toUpperCase().replace(/\s+/g, '_');
    
    // Clear previous timeout
    if (codeCheckTimeout) {
      clearTimeout(codeCheckTimeout);
    }
    
    // Check code availability with debouncing
    if (processedValue.length >= 3 && !isEditing) {
      const timerId = setTimeout(async () => {
        console.log('🔍 Checking API code availability after change:', processedValue);
        await checkCodeAvailability(processedValue);
      }, 500);
      setCodeCheckTimeout(timerId);
    } else if (processedValue.length < 3) {
      setApiCodeExists(false);
      setValidationErrors(prev => ({ ...prev, apiCode: null }));
    }
  }
  
  setApiDetails(prev => ({ ...prev, [field]: processedValue }));
  
  // Clear validation error for this field
  if (field !== 'apiCode') {
    setValidationErrors(prev => ({ ...prev, [field]: null }));
  }
};

  // Handle schema configuration with validation
  const handleSchemaConfigChange = (field, value) => {
    const updatedConfig = { ...schemaConfig, [field]: value };
    setSchemaConfig(updatedConfig);
    
    // Clear validation error for this field
    setValidationErrors(prev => ({ ...prev, [field]: null }));
  };

  // Helper function to check if a parameter is a file upload type
  const isFileParameter = (param) => {
    const fileTypes = ['FILE', 'BLOB', 'BYTEA', 'MULTIPART_FILE', 'BINARY'];
    return fileTypes.includes(param.oracleType?.toUpperCase()) ||
           param.parameterType?.toUpperCase() === 'FILE' ||
           param.apiType?.toUpperCase() === 'FILE';
  };

  // Helper function to get all file parameters
  const getFileParameters = () => {
    return getInParameters().filter(p => isFileParameter(p));
  };

  // Helper function to get current timestamp in numeric format (YYYYMMDDHHMMSS)
  const getCurrentTimestamp = () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');
    
    return `${year}${month}${day}${hours}${minutes}${seconds}`;
  };

  // Add this function to manage URL endpoint path based on parameters
  const updateEndpointPathFromParameters = useCallback((paramsList) => {
    // Only for REST protocol
    if (protocolType !== 'rest') return;
    
    // Get all path parameters (those with parameterLocation === 'path')
    const pathParams = paramsList.filter(p => p.parameterLocation === 'path' && p.key && p.key.trim());
    
    // Get current endpoint path
    let currentPath = apiDetails.endpointPath;
    
    // Remove all existing path parameter placeholders from the URL
    // Pattern matches {paramName} anywhere in the path
    let cleanedPath = currentPath.replace(/\{[^}]+\}/g, '');
    
    // Remove any double slashes that might have been created
    cleanedPath = cleanedPath.replace(/\/+/g, '/');
    
    // Remove trailing slash if present
    if (cleanedPath.endsWith('/')) {
      cleanedPath = cleanedPath.slice(0, -1);
    }
    
    // Add the path parameters in order
    let newPath = cleanedPath;
    for (const param of pathParams) {
      // Ensure the path parameter is properly formatted with curly braces
      const paramPlaceholder = `{${param.key}}`;
      
      // Add the parameter to the path (you can customize the order/location)
      // This adds them in sequence at the end of the path
      newPath = newPath + `/${paramPlaceholder}`;
    }
    
    // If the path is empty, set to root
    if (!newPath) {
      newPath = '/';
    }
    
    // Update the endpoint path if it changed
    if (newPath !== currentPath) {
      setApiDetails(prev => ({
        ...prev,
        endpointPath: newPath
      }));
    }
  }, [apiDetails.endpointPath, setApiDetails, protocolType]);



// Force GraphQL request body to update when parameter API types or examples change
useEffect(() => {
  if (protocolType === 'graphql') {
    const inParams = getInParameters();
    const outMappings = getOutMappings();
    const operationName = graphqlConfig.operationName || 'query';
    const operationType = graphqlConfig.operationType || 'query';
    
    let variableDefinitions = '';
    let variableUsages = '';
    let variablesObject = {};
    
    if (inParams.length > 0) {
      // Build variable definitions with proper types
      variableDefinitions = '(' + inParams.map(p => {
        let graphqlType = 'String';
        switch(p.apiType?.toLowerCase()) {
          case 'integer':
            graphqlType = 'Int';
            break;
          case 'number':
            graphqlType = 'Float';
            break;
          case 'boolean':
            graphqlType = 'Boolean';
            break;
          default:
            graphqlType = 'String';
        }
        if (p.required) graphqlType = `${graphqlType}!`;
        return `$${p.key}: ${graphqlType}`;
      }).join(', ') + ')';
      
      variableUsages = '(' + inParams.map(p => `${p.key}: $${p.key}`).join(', ') + ')';
      
      // Build variables with proper type conversion
      inParams.forEach(p => {
        let val = p.example;
        if (!val || val === '') {
          if (p.apiType === 'integer') val = 123;
          else if (p.apiType === 'number') val = 123.45;
          else if (p.apiType === 'boolean') val = true;
          else if (p.apiType === 'array') val = [];
          else if (p.apiType === 'object') val = {};
          else val = 'sample';
        } else {
          if (p.apiType === 'integer') {
            val = parseInt(val, 10);
            if (isNaN(val)) val = 123;
          } else if (p.apiType === 'number') {
            val = parseFloat(val);
            if (isNaN(val)) val = 123.45;
          } else if (p.apiType === 'boolean') {
            val = val === 'true' || val === '1' || val === 'yes';
          } else if (p.apiType === 'array') {
            try { val = JSON.parse(val); } catch { val = []; }
          } else if (p.apiType === 'object') {
            try { val = JSON.parse(val); } catch { val = {}; }
          }
        }
        variablesObject[p.key] = val;
      });
    }
    
    // Build query string
    let queryString = '';
    if (operationType === 'query') {
      if (inParams.length > 0) {
        queryString = `${operationType} ${operationName}${variableDefinitions} {\n  ${operationName}${variableUsages} {\n`;
        if (outMappings.length > 0) {
          queryString += outMappings.slice(0, 20).map(m => `    ${m.apiField}`).join('\n');
        } else {
          queryString += `    id\n    createdAt\n    updatedAt`;
        }
        queryString += `\n  }\n}`;
      } else {
        queryString = `${operationType} {\n  ${operationName} {\n`;
        if (outMappings.length > 0) {
          queryString += outMappings.slice(0, 20).map(m => `    ${m.apiField}`).join('\n');
        } else {
          queryString += `    id\n    createdAt\n    updatedAt`;
        }
        queryString += `\n  }\n}`;
      }
    }
    
    const graphqlRequest = JSON.stringify({
      query: queryString,
      variables: variablesObject
    }, null, 2);
    
    if (requestBody.sample !== graphqlRequest) {
      handleRequestBodyChange('sample', graphqlRequest);
    }
  }
}, [parameters, responseMappings, protocolType, graphqlConfig.operationName, graphqlConfig.operationType, getInParameters, getOutMappings]);



// Update the useEffect that regenerates request body when parameters change
useEffect(() => {
    if (parameters.length === 0) return;
    
    if (protocolType === 'rest' && requestBody.bodyType === 'json') {
        const isCustomQueryMode = sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery;
        const paramsToUse = isCustomQueryMode ? getInParameters() : getInParameters().filter(p => p.parameterLocation === 'body');
        
        if (paramsToUse.length > 0) {
            // Check if current sample is the default/empty one
            const currentSample = requestBody.sample;
            const isDefaultOrEmpty = !currentSample || 
                currentSample === '' ||
                currentSample === '{}' ||
                currentSample === '{\n  "key": "value"\n}';
            
            // Only auto-regenerate if the sample is empty/default (not user-modified)
            if (isDefaultOrEmpty) {
                const requestBodyObj = {};
                paramsToUse.forEach(param => {
                    requestBodyObj[param.key] = parseValueByType(param.example, param.apiType);
                });
                
                const jsonSample = JSON.stringify(requestBodyObj, null, 2);
                if (requestBody.sample !== jsonSample) {
                    console.log('✅ Auto-updating JSON request body with proper types');
                    handleRequestBodyChange('sample', jsonSample);
                    setRequestSchemaVersion(prev => prev + 1);
                }
            }
        }
    }
}, [parameters.map(p => `${p.id}-${p.apiType}`).join(','), protocolType, requestBody.bodyType]);


// Force regenerate request body after parameters are loaded with correct types
useEffect(() => {
  // Wait for parameters to be loaded and have correct types
  if (parameters.length > 0 && protocolType === 'rest' && requestBody.bodyType === 'json') {
    const timer = setTimeout(() => {
      console.log('🔄 Force regenerating request body after parameter load...');
      
      const isCustomQueryMode = sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery;
      const paramsToUse = isCustomQueryMode ? getInParameters() : getInParameters().filter(p => p.parameterLocation === 'body');
      
      if (paramsToUse.length > 0) {
        const requestBodyObj = {};
        paramsToUse.forEach(param => {
          const apiType = (param.apiType || 'string').toLowerCase();
          let val = param.example;
          
          switch(apiType) {
            case 'integer':
              const intVal = parseInt(val, 10);
              requestBodyObj[param.key] = isNaN(intVal) ? 123 : intVal;
              break;
            case 'number':
              const floatVal = parseFloat(val);
              requestBodyObj[param.key] = isNaN(floatVal) ? 123.45 : floatVal;
              break;
            case 'boolean':
              if (val === 'true' || val === '1' || val === 'yes' || val === true) {
                requestBodyObj[param.key] = true;
              } else if (val === 'false' || val === '0' || val === 'no' || val === false) {
                requestBodyObj[param.key] = false;
              } else {
                requestBodyObj[param.key] = true;
              }
              break;
            case 'array':
              try {
                const parsed = JSON.parse(val);
                requestBodyObj[param.key] = Array.isArray(parsed) ? parsed : [];
              } catch {
                requestBodyObj[param.key] = [];
              }
              break;
            case 'object':
              try {
                const parsed = JSON.parse(val);
                requestBodyObj[param.key] = (typeof parsed === 'object' && !Array.isArray(parsed)) ? parsed : {};
              } catch {
                requestBodyObj[param.key] = {};
              }
              break;
            default:
              requestBodyObj[param.key] = val || `sample_${param.key}`;
          }
        });
        
        const jsonSample = JSON.stringify(requestBodyObj, null, 2);
        
        console.log('✅ Force updating request body with proper types');
        console.log('📝 New sample:', jsonSample);
        
        // Update both the requestBody.sample and increment the version to force re-render
        setRequestBody(prev => ({ ...prev, sample: jsonSample }));
        setRequestSchemaVersion(prev => prev + 1); // This is the key line!
      }
    }, 200);
    
    return () => clearTimeout(timer);
  }
}, [parameters.length, parameters.map(p => `${p.id}-${p.apiType}`).join(',')]);

// Force request body regeneration when parameter apiType or example changes
useEffect(() => {
  // Skip if we're in the middle of loading or if there are no parameters
  if (parameters.length === 0) return;
  
  if (protocolType === 'rest' && requestBody.bodyType === 'json') {
    const isCustomQueryMode = sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery;
    const paramsToUse = isCustomQueryMode ? getInParameters() : getInParameters().filter(p => p.parameterLocation === 'body');
    
    if (paramsToUse.length > 0) {
      console.log('🔄 Regenerating request body due to parameter changes...');
      const requestBodyObj = {};
      
      paramsToUse.forEach(param => {
        const apiType = (param.apiType || 'string').toLowerCase();
        let val = param.example;
        
        // CRITICAL: Parse the value correctly based on type
        switch(apiType) {
          case 'integer':
            const intVal = parseInt(val, 10);
            requestBodyObj[param.key] = isNaN(intVal) ? 123 : intVal;
            break;
          case 'number':
            const floatVal = parseFloat(val);
            requestBodyObj[param.key] = isNaN(floatVal) ? 123.45 : floatVal;
            break;
          case 'boolean':
            if (val === 'true' || val === '1' || val === 'yes' || val === true) {
              requestBodyObj[param.key] = true;
            } else if (val === 'false' || val === '0' || val === 'no' || val === false) {
              requestBodyObj[param.key] = false;
            } else {
              requestBodyObj[param.key] = true;
            }
            break;
          case 'array':
            try {
              const parsed = JSON.parse(val);
              requestBodyObj[param.key] = Array.isArray(parsed) ? parsed : [];
            } catch {
              requestBodyObj[param.key] = [];
            }
            break;
          case 'object':
            try {
              const parsed = JSON.parse(val);
              requestBodyObj[param.key] = (typeof parsed === 'object' && !Array.isArray(parsed)) ? parsed : {};
            } catch {
              requestBodyObj[param.key] = {};
            }
            break;
          default:
            // string - keep as string
            requestBodyObj[param.key] = val || `sample_${param.key}`;
        }
      });
      
      const jsonSample = JSON.stringify(requestBodyObj, null, 2);
      // Only update if the sample has actually changed
      if (requestBody.sample !== jsonSample) {
        console.log('✅ Updating JSON request body sample with proper types');
        handleRequestBodyChange('sample', jsonSample);
        setRequestSchemaVersion(prev => prev + 1);
      }
    }
  }
}, [parameters.map(p => `${p.id}-${p.apiType}`).join(','), protocolType, requestBody.bodyType]); // Remove example from dependency to prevent loops



// Add this helper function near your other helper functions (around line 2000)
const parseValueByType = (value, apiType) => {
  const type = (apiType || 'string').toLowerCase();
  
  // Handle null/undefined/empty values
  if (value === null || value === undefined || value === '') {
    switch(type) {
      case 'integer': return 123;
      case 'number': return 123.45;
      case 'boolean': return true;
      case 'array': return [];
      case 'object': return {};
      default: return 'sample';
    }
  }
  
  // Parse based on type
  switch(type) {
    case 'integer':
      const intVal = parseInt(value, 10);
      return isNaN(intVal) ? 123 : intVal;
      
    case 'number':
      const floatVal = parseFloat(value);
      return isNaN(floatVal) ? 123.45 : floatVal;
      
    case 'boolean':
      if (value === true || value === false) return value;
      const strVal = String(value).toLowerCase();
      if (strVal === 'true' || strVal === '1' || strVal === 'yes') return true;
      if (strVal === 'false' || strVal === '0' || strVal === 'no') return false;
      return true;
      
    case 'array':
      // If it's already an array, return it
      if (Array.isArray(value)) return value;
      // Try to parse JSON array
      if (typeof value === 'string') {
        try {
          const parsed = JSON.parse(value);
          return Array.isArray(parsed) ? parsed : [];
        } catch {
          return [];
        }
      }
      return [];
      
    case 'object':
      // If it's already an object, return it
      if (typeof value === 'object' && value !== null && !Array.isArray(value)) return value;
      // Try to parse JSON object
      if (typeof value === 'string') {
        try {
          const parsed = JSON.parse(value);
          return (typeof parsed === 'object' && !Array.isArray(parsed)) ? parsed : {};
        } catch {
          return {};
        }
      }
      return {};
      
    default: // string
      return String(value);
  }
};


// Update the regenerateRequestBody function
const regenerateRequestBody = useCallback(() => {
    if (protocolType === 'rest' && requestBody.bodyType === 'json') {
        const isCustomQueryMode = sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery;
        const paramsToUse = isCustomQueryMode ? getInParameters() : getInParameters().filter(p => p.parameterLocation === 'body');
        
        if (paramsToUse.length > 0) {
            const requestBodyObj = {};
            paramsToUse.forEach(param => {
                // Use the helper function to parse the value properly
                requestBodyObj[param.key] = parseValueByType(param.example, param.apiType);
            });
            
            const updatedSample = JSON.stringify(requestBodyObj, null, 2);
            if (requestBody.sample !== updatedSample) {
                console.log('✅ Regenerating request body with proper types');
                handleRequestBodyChange('sample', updatedSample);
                setRequestSchemaVersion(prev => prev + 1);
            }
        }
    }
}, [protocolType, requestBody.bodyType, sourceType, isCustomQuery, isEditingCustomQuery, getInParameters, requestBody.sample]);


  const handleParameterChange = (id, field, value) => {
  // Handle API Type change specially - update both type and example
  if (field === 'apiType') {
    const normalizedType = value.toLowerCase();
    
    // Use the helper to get appropriate example based on type
    let newExample = '';
    switch(normalizedType) {
        case 'integer':
            newExample = '123';
            break;
        case 'number':
            newExample = '123.45';
            break;
        case 'boolean':
            newExample = 'true';
            break;
        case 'array':
            newExample = '[]';
            break;
        case 'object':
            newExample = '{}';
            break;
        default:
            newExample = 'sample';
    }
    
    setParameters(prevParams => prevParams.map(param => {
        if (param.id !== id) return param;
        return { 
            ...param, 
            apiType: normalizedType,
            example: newExample
        };
    }));
    
    // Regenerate request body after type change
    setTimeout(() => {
        if (protocolType === 'rest' && requestBody.bodyType === 'json') {
            regenerateRequestBody();
        }
    }, 100);
    return;
}
  
  // Handle regular field changes (non-apiType)
  setParameters(prevParams => {
    const currentParam = prevParams.find(p => p.id === id);
    
    let updatedParams = prevParams.map(param => {
      if (param.id !== id) return param;
      return { ...param, [field]: value };
    });
    
    // Handle AUTOGENERATE special cases
    if (field === 'oracleType' && value === 'AUTOGENERATE') {
      const timestamp = getCurrentTimestamp();
      updatedParams = updatedParams.map(param => {
        if (param.id !== id) return param;
        return { 
          ...param, 
          required: false,
          example: timestamp,
          defaultValue: timestamp,
          apiType: 'string'
        };
      });
    }
    
    if (field === 'oracleType' && currentParam?.oracleType === 'AUTOGENERATE' && value !== 'AUTOGENERATE') {
      let defaultExample = 'sample';
      if (value === 'NUMBER') defaultExample = '123';
      else if (value === 'DATE') defaultExample = '2024-01-01';
      else if (value === 'BOOLEAN') defaultExample = 'true';
      
      updatedParams = updatedParams.map(param => {
        if (param.id !== id) return param;
        return { 
          ...param, 
          required: true,
          example: defaultExample,
          defaultValue: '',
          apiType: value === 'NUMBER' ? 'integer' : 'string'
        };
      });
    }
    
    // Handle path parameter location
    if (field === 'parameterLocation') {
      updatedParams = updatedParams.map(param => {
        if (param.id !== id) return param;
        return { 
          ...param,
          inBody: value === 'body',
          required: value === 'path' ? true : param.required,
          _isPathParam: value === 'path'
        };
      });
      setTimeout(() => updateEndpointPathFromParameters(updatedParams), 0);
    }
    
    // Handle example change - ensure it's stored as a string but will be parsed later
if (field === 'example' && currentParam) {
  const apiType = (currentParam.apiType || 'string').toLowerCase();
  let processedValue = value;
  
  // Store as string, but validate that it can be parsed correctly
  if (apiType === 'integer') {
    const parsed = parseInt(value, 10);
    processedValue = isNaN(parsed) ? '123' : parsed.toString();
  } else if (apiType === 'number') {
    const parsed = parseFloat(value);
    processedValue = isNaN(parsed) ? '123.45' : parsed.toString();
  } else if (apiType === 'boolean') {
    // Keep as string 'true' or 'false'
    processedValue = value === 'true' || value === 'false' ? value : 'true';
  } else if (apiType === 'array') {
    // Try to validate it's valid JSON array
    try {
      const parsed = JSON.parse(value);
      if (Array.isArray(parsed)) {
        processedValue = value;
      } else {
        processedValue = '[]';
      }
    } catch {
      processedValue = '[]';
    }
  } else if (apiType === 'object') {
    // Try to validate it's valid JSON object
    try {
      const parsed = JSON.parse(value);
      if (typeof parsed === 'object' && !Array.isArray(parsed)) {
        processedValue = value;
      } else {
        processedValue = '{}';
      }
    } catch {
      processedValue = '{}';
    }
  }
  
  updatedParams = updatedParams.map(param => {
    if (param.id !== id) return param;
    return { ...param, example: processedValue };
  });
}
    
    return updatedParams;
  });
  
  // Force request body regeneration when example changes (but not for apiType since we already handled that)
  if (field === 'example') {
    setTimeout(() => {
      if (protocolType === 'rest' && (requestBody.bodyType === 'json' || requestBody.bodyType === 'xml')) {
        const isCustomQueryMode = sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery;
        const paramsToUse = isCustomQueryMode ? getInParameters() : getInParameters().filter(p => p.parameterLocation === 'body');
        
        if (paramsToUse.length > 0 && requestBody.bodyType === 'json') {
          const requestBodyObj = {};
          paramsToUse.forEach(param => {
            let val = param.example;
            const apiType = (param.apiType || 'string').toLowerCase();
            
            switch(apiType) {
              case 'integer':
                val = val ? parseInt(val, 10) : 123;
                if (isNaN(val)) val = 123;
                break;
              case 'number':
                val = val ? parseFloat(val) : 123.45;
                if (isNaN(val)) val = 123.45;
                break;
              case 'boolean':
                val = val === 'true' || val === '1' || val === 'yes' || val === true;
                break;
              case 'array':
                try {
                  val = val && val !== '' ? JSON.parse(val) : [];
                } catch {
                  val = [];
                }
                break;
              case 'object':
                try {
                  val = val && val !== '' ? JSON.parse(val) : {};
                } catch {
                  val = {};
                }
                break;
              default:
                val = val || `sample_${param.key}`;
            }
            requestBodyObj[param.key] = val;
          });
          
          const jsonSample = JSON.stringify(requestBodyObj, null, 2);
          if (requestBody.sample !== jsonSample) {
            console.log('✅ Updating JSON request body due to example change');
            handleRequestBodyChange('sample', jsonSample);
            setRequestSchemaVersion(prev => prev + 1);
          }
        } else if (paramsToUse.length > 0 && requestBody.bodyType === 'xml') {
          let xmlSample = `<?xml version="1.0" encoding="UTF-8"?>\n<request>\n`;
          paramsToUse.forEach(param => {
            let val = param.example || `sample_${param.key}`;
            const apiType = (param.apiType || 'string').toLowerCase();
            if (apiType === 'array' || apiType === 'object') {
              val = '';
            }
            xmlSample += `  <${param.key}>${val}</${param.key}>\n`;
          });
          xmlSample += `</request>`;
          if (requestBody.sample !== xmlSample) {
            console.log('✅ Updating XML request body due to example change');
            handleRequestBodyChange('sample', xmlSample);
            setRequestSchemaVersion(prev => prev + 1);
          }
        }
      }
    }, 50);
  }
};



  // Auto-populate SOAP service name on initial load and for custom queries
useEffect(() => {
  if (protocolType === 'soap' && !isEditing) {
    let generatedServiceName = '';
    
    // Case 1: Database object exists
    if (selectedDbObject?.name) {
      generatedServiceName = selectedDbObject.name.charAt(0).toUpperCase() + 
                             selectedDbObject.name.slice(1).toLowerCase();
      generatedServiceName = generatedServiceName.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
      generatedServiceName = generatedServiceName.replace(/[^a-zA-Z0-9]/g, '');
      if (!generatedServiceName.toLowerCase().endsWith('service')) {
        generatedServiceName = generatedServiceName + 'Service';
      }
    } 
    // Case 2: Custom query mode
    else if (sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery) {
      // Generate from custom query or use a default
      if (customQuery && customQuery.trim()) {
        // Try to extract table name from query
        const fromMatch = customQuery.match(/FROM\s+([^\s,;]+)/i);
        if (fromMatch && fromMatch[1]) {
          let tableName = fromMatch[1];
          // Remove schema prefix if present (e.g., "HR.EMPLOYEES" -> "EMPLOYEES")
          if (tableName.includes('.')) {
            tableName = tableName.split('.').pop();
          }
          generatedServiceName = tableName.charAt(0).toUpperCase() + tableName.slice(1).toLowerCase();
          generatedServiceName = generatedServiceName.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
          generatedServiceName = generatedServiceName.replace(/[^a-zA-Z0-9]/g, '');
        } else {
          generatedServiceName = 'CustomQuery';
        }
      } else {
        generatedServiceName = 'CustomQuery';
      }
      
      if (!generatedServiceName.toLowerCase().endsWith('service')) {
        generatedServiceName = generatedServiceName + 'Service';
      }
    }
    // Case 3: API name exists
    else if (apiDetails.apiName) {
      generatedServiceName = apiDetails.apiName.replace(/\s+/g, '');
      if (!generatedServiceName.toLowerCase().endsWith('service')) {
        generatedServiceName = generatedServiceName + 'Service';
      }
    }
    // Case 4: Ultimate fallback
    else {
      generatedServiceName = 'APIService';
    }
    
    // Only update if service name is empty
    if (!soapConfig.serviceName && generatedServiceName) {
      console.log('🔧 Auto-populating SOAP service name on load:', generatedServiceName);
      setSoapConfig(prev => ({ ...prev, serviceName: generatedServiceName }));
    }
  }
}, [protocolType, selectedDbObject, sourceType, isCustomQuery, isEditingCustomQuery, customQuery, apiDetails.apiName, soapConfig.serviceName, isEditing]);


// ============ FULLY DYNAMIC AUTO-EXTRACT PARAMETERS AND MAPPINGS ==========
useEffect(() => {
  // Only run in custom query mode
    if ((sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery) && 
      customQuery && customQuery.trim() && 
      !isEditing) { 
    console.log('🔍 Dynamic extraction from custom query');
    
    // ============ 1. DYNAMIC PARAMETER EXTRACTION ==========
    let allParams = [];
    
    // Extract :paramName but exclude PostgreSQL casts (::)
    const colonMatches = customQuery.match(/(?<![:\w]):(\w+)/g) || [];
    colonMatches.forEach(match => {
      const paramName = match.substring(1);
      const looksLikeDataType = /^(jsonb?|text|varchar|char|int|integer|bigint|smallint|decimal|numeric|float|double|bool|boolean|date|time|timestamp|bytea|clob|blob)$/i.test(paramName);
      if (!looksLikeDataType) {
        allParams.push(paramName);
      }
    });
    
    // Extract ${paramName}
    const bracketMatches = customQuery.match(/\$\{(\w+)\}/g) || [];
    bracketMatches.forEach(match => {
      allParams.push(match.substring(2, match.length - 1));
    });
    
    // Extract @paramName
    const atMatches = customQuery.match(/@(\w+)/g) || [];
    atMatches.forEach(match => {
      allParams.push(match.substring(1));
    });
    
    const uniqueParams = [...new Set(allParams)];
    console.log(`📊 Found ${uniqueParams.length} dynamic parameters:`, uniqueParams);
    
    if (uniqueParams.length > 0) {
      const dynamicParams = uniqueParams.map((paramName, idx) => {
        // Check if parameter already exists and is from custom query
        const existingParam = parameters.find(p => p.key === paramName && p._isFromCustomQuery);
        if (existingParam) return existingParam;
        
        let oracleType = 'VARCHAR2';
        let apiType = 'string';
        let example = 'example';
        const paramLower = paramName.toLowerCase();
        
        if (paramLower.match(/limit|page|offset|size|count|per_page/)) {
          oracleType = 'NUMBER';
          apiType = 'integer';
          example = '10';
        } else if (paramLower.match(/id$|_id|^id/)) {
          oracleType = 'VARCHAR2';
          apiType = 'string';
          example = '123';
        }
        
        return {
          id: `param-${Date.now()}-${idx}`,
          key: paramName,
          dbColumn: paramName,
          oracleType: oracleType,
          apiType: apiType,
          parameterLocation: 'query',
          required: true,
          description: `Parameter: ${paramName}`,
          example: example,
          validationPattern: '',
          defaultValue: '',
          inBody: false,
          isPrimaryKey: false,
          paramMode: 'IN',
          _isPathParam: false,
          _isFromCustomQuery: true  // Mark as custom query parameter
        };
      });
      setParameters(dynamicParams);
    } else {
      // If no parameters found, clear them (but only if we're in custom query mode)
      if (sourceType === 'custom_query') {
        setParameters([]);
      }
    }
    
    // ============ 2. RESPONSE MAPPINGS EXTRACTION ==========
    // Find the SELECT keyword position
    const selectIndex = customQuery.toUpperCase().indexOf('SELECT');
    if (selectIndex === -1) {
      setResponseMappings([]);
      return;
    }
    
    // Find the FROM keyword that belongs to the main query
    let fromIndex = -1;
    let depth = 0;
    let inString = false;
    let stringChar = '';
    let inComment = false;
    
    for (let i = selectIndex + 6; i < customQuery.length; i++) {
      const char = customQuery[i];
      const prevChar = customQuery[i - 1];
      const nextThreeChars = customQuery.substring(i, i + 4).toUpperCase();
      
      // Handle comments
      if (!inString && char === '-' && customQuery[i + 1] === '-') {
        inComment = true;
        i++;
        continue;
      }
      if (inComment && char === '\n') {
        inComment = false;
        continue;
      }
      
      if (!inComment) {
        // Handle string literals
        if ((char === "'" || char === '"') && prevChar !== '\\') {
          if (!inString) {
            inString = true;
            stringChar = char;
          } else if (char === stringChar) {
            inString = false;
          }
        }
        
        if (!inString) {
          // Track nested parentheses
          if (char === '(') depth++;
          if (char === ')') depth--;
          
          // Look for FROM at root level (depth === 0)
          if (nextThreeChars === 'FROM' && depth === 0) {
            fromIndex = i;
            console.log(`Found FROM at index: ${fromIndex}`);
            break;
          }
        }
      }
    }
    
    if (fromIndex === -1) {
      console.log('Could not find FROM keyword');
      setResponseMappings([]);
      return;
    }
    
    // Extract only the SELECT clause (between SELECT and FROM)
    const selectClause = customQuery.substring(selectIndex + 6, fromIndex).trim();
    
    console.log('📝 SELECT clause length:', selectClause.length);
    console.log('📝 SELECT clause full text:', selectClause);
    
    // Remove comments from select clause
    let cleanedClause = selectClause.replace(/--.*$/gm, '');
    
    // Parse columns
    const columns = [];
    let currentColumn = '';
    let parenDepth = 0;
    let inStringLocal = false;
    let stringCharLocal = '';
    let inCommentLocal = false;
    
    for (let i = 0; i < cleanedClause.length; i++) {
      const char = cleanedClause[i];
      const prevChar = cleanedClause[i - 1];
      
      // Handle comments
      if (!inStringLocal && char === '-' && cleanedClause[i + 1] === '-') {
        inCommentLocal = true;
        i++;
        continue;
      }
      if (inCommentLocal && char === '\n') {
        inCommentLocal = false;
        continue;
      }
      
      if (!inCommentLocal) {
        // Handle string literals
        if ((char === "'" || char === '"') && prevChar !== '\\') {
          if (!inStringLocal) {
            inStringLocal = true;
            stringCharLocal = char;
          } else if (char === stringCharLocal) {
            inStringLocal = false;
          }
        }
        
        if (!inStringLocal) {
          // Track parentheses depth
          if (char === '(') parenDepth++;
          if (char === ')') parenDepth--;
        }
        
        // Split on commas at root level
        if (char === ',' && parenDepth === 0 && !inStringLocal && !inCommentLocal) {
          if (currentColumn.trim()) {
            columns.push(currentColumn.trim());
          }
          currentColumn = '';
          continue;
        }
      }
      
      currentColumn += char;
    }
    
    // Add the last column
    if (currentColumn.trim()) {
      columns.push(currentColumn.trim());
    }
    
    console.log(`📊 Found ${columns.length} columns in SELECT clause`);
    columns.forEach((col, idx) => {
      console.log(`Column ${idx + 1}:`, col.substring(0, 150));
    });
    
    // Extract column names
    const columnNames = [];  // <-- FIX: Declare columnNames here
    const sqlKeywords = new Set([
      'select', 'from', 'where', 'join', 'left', 'right', 'inner', 'outer',
      'group', 'by', 'order', 'having', 'limit', 'offset', 'case', 'when',
      'then', 'else', 'end', 'and', 'or', 'not', 'in', 'exists', 'between',
      'like', 'is', 'null', 'true', 'false', 'as', 'on'
    ]);
    
    for (let col of columns) {
      let columnName = null;
      col = col.replace(/--.*$/gm, '').trim();
      
      // Look for AS alias
      let asMatch = col.match(/\s+AS\s+([^\s,]+)$/i);
      if (asMatch && asMatch[1]) {
        columnName = asMatch[1].replace(/['"]/g, '');
        if (sqlKeywords.has(columnName.toLowerCase())) {
          columnName = null;
        }
      }
      
      // For CASE statements
      if (!columnName && col.toUpperCase().includes('CASE')) {
        const thenMatch = col.match(/THEN\s+'([^']+)'/i);
        if (thenMatch && thenMatch[1]) {
          columnName = thenMatch[1].toLowerCase().replace(/[^a-z]/g, '_');
        } else {
          const whenMatch = col.match(/WHEN\s+(\w+)/i);
          if (whenMatch && whenMatch[1]) {
            columnName = `${whenMatch[1]}_type`;
          } else {
            columnName = 'calculated_field';
          }
        }
      }
      
      // Fallback to last word
      if (!columnName) {
        const words = col.split(/\s+/);
        for (let i = words.length - 1; i >= 0; i--) {
          const word = words[i].replace(/[(),]/g, '');
          if (word && !sqlKeywords.has(word.toLowerCase())) {
            columnName = word;
            break;
          }
        }
      }
      
      if (columnName) {
        columnName = columnName.replace(/[^\w]/g, '_').replace(/^_+|_+$/g, '').toLowerCase();
        if (columnName && !columnNames.includes(columnName) && !sqlKeywords.has(columnName)) {
          columnNames.push(columnName);
        }
      }
    }
    
    console.log(`📊 Extracted ${columnNames.length} response columns:`, columnNames);
    
    // Create response mappings
    if (columnNames.length > 0) {
      const dynamicMappings = columnNames.map((fieldName, idx) => {
        let oracleType = 'VARCHAR2';
        let apiType = 'string';
        let format = '';
        const fieldLower = fieldName.toLowerCase();
        
        if (fieldLower.match(/id$|^id|_id$/)) {
          oracleType = 'VARCHAR2';
          apiType = 'string';
        } else if (fieldLower.match(/timestamp|date|time|_at$/)) {
          oracleType = 'TIMESTAMP';
          apiType = 'string';
          format = 'date-time';
        } else if (fieldLower.match(/duration|_ms$|milliseconds|seconds?$/)) {
          oracleType = 'NUMBER';
          apiType = 'integer';
        } else if (fieldLower.match(/count|total|amount|price|cost|fee/)) {
          oracleType = 'NUMBER';
          apiType = 'number';
        } else if (fieldLower.match(/status|type|method|mode|level/)) {
          oracleType = 'VARCHAR2';
          apiType = 'string';
        } else if (fieldLower.match(/body|payload|content|data/)) {
          oracleType = 'CLOB';
          apiType = 'string';
        }
        
        return {
          id: `mapping-dynamic-${Date.now()}-${idx}`,
          apiField: fieldName,
          dbColumn: fieldName,
          oracleType: oracleType,
          apiType: apiType,
          format: format,
          nullable: true,
          isPrimaryKey: fieldName === 'id',
          includeInResponse: true,
          inResponse: true,
          paramMode: 'OUT',
          _fromCustomQuery: true  // Mark as custom query mapping
        };
      });
      
      console.log(`✅ Dynamically generated ${dynamicMappings.length} response mappings`);
      setResponseMappings(dynamicMappings);
    } else {
      setResponseMappings([]);
    }
    
  } else if (sourceType === 'database_object' && selectedDbObject) {
    // We're in database object mode but the useEffect might have been triggered
    // This ensures we don't accidentally clear database object data
    console.log('ℹ️ In database object mode, skipping custom query extraction');
  }
}, [customQuery, sourceType, isCustomQuery, isEditingCustomQuery, protocolType]);

// ============ AUTO-GENERATE API NAME FROM CUSTOM QUERY ============
useEffect(() => {
  // Only run in custom query mode and not editing
  if ((sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery) && 
      customQuery && customQuery.trim() && 
      (!apiDetails.apiName || apiDetails.apiName === '' || apiDetails.apiName === 'Custom Query API')) {
    
    // Try to extract table name from the query
    let tableName = 'CustomData';
    const fromMatch = customQuery.match(/FROM\s+([^\s,;]+)/i);
    if (fromMatch && fromMatch[1]) {
      tableName = fromMatch[1];
      // Remove schema prefix if present
      if (tableName.includes('.')) {
        tableName = tableName.split('.').pop();
      }
      // Convert to proper case
      tableName = tableName.charAt(0).toUpperCase() + tableName.slice(1).toLowerCase();
    }
    
    // Generate API name based on operation type
    let operationPrefix = '';
    const queryLower = customQuery.toLowerCase();
    if (queryLower.includes('select')) {
      operationPrefix = 'Get';
    } else if (queryLower.includes('insert')) {
      operationPrefix = 'Create';
    } else if (queryLower.includes('update')) {
      operationPrefix = 'Update';
    } else if (queryLower.includes('delete')) {
      operationPrefix = 'Delete';
    } else {
      operationPrefix = 'Execute';
    }
    
    const generatedName = `${operationPrefix} ${tableName} Data`;
    const generatedCode = `${operationPrefix.toUpperCase()}_${tableName.toUpperCase().replace(/\s+/g, '_')}`;
    
    console.log('📝 Auto-generating API name:', generatedName);
    console.log('📝 Auto-generating API code:', generatedCode);
    
    setApiDetails(prev => ({
      ...prev,
      apiName: prev.apiName === '' || prev.apiName === 'Custom Query API' ? generatedName : prev.apiName,
      apiCode: prev.apiCode === '' ? generatedCode : prev.apiCode,
      description: prev.description === '' ? `API generated from custom query: ${customQuery.substring(0, 100)}...` : prev.description
    }));
  }
}, [customQuery, sourceType, isCustomQuery, isEditingCustomQuery, apiDetails.apiName, apiDetails.apiCode]);



// ============ CLEAR PARAMETERS WHEN CUSTOM QUERY IS CLEARED ============
useEffect(() => {
  // If custom query mode is active but query is empty, clear parameters
  if ((sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery) && 
      (!customQuery || customQuery.trim() === '')) {
    if (parameters.length > 0) {
      console.log('🧹 Clearing parameters because custom query is empty');
      setParameters([]);
    }
  }
}, [customQuery, sourceType, isCustomQuery, isEditingCustomQuery]);

// ============ CLEAR MAPPINGS WHEN CUSTOM QUERY IS CLEARED ============
useEffect(() => {
  // If custom query mode is active but query is empty, clear response mappings too
  if ((sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery) && 
      (!customQuery || customQuery.trim() === '')) {
    if (responseMappings.length > 0) {
      console.log('🧹 Clearing response mappings because custom query is empty');
      setResponseMappings([]);
    }
  }
}, [customQuery, sourceType, isCustomQuery, isEditingCustomQuery, responseMappings.length]);


// ============ RESTORE PARAMETERS AND MAPPINGS WHEN SWITCHING BACK TO DATABASE OBJECT MODE ============
useEffect(() => {
  // When switching back to database object mode from custom query mode
  if (sourceType === 'database_object' && selectedDbObject && (isCustomQuery || isEditingCustomQuery)) {
    console.log('🔄 Switching back to database object mode - restoring parameters and mappings from:', selectedDbObject.name);
    
    // Reset the custom query flags
    setIsCustomQuery(false);
    setIsEditingCustomQuery(false);
    
    // Repopulate from the selected database object
    // Use preserveExistingApiDetails = true to keep any API details the user might have changed
    populateFormFromObject(selectedDbObject, true);
  }
}, [sourceType, selectedDbObject]);

// ============ CLEAR PARAMETERS AND MAPPINGS WHEN SWITCHING TO CUSTOM QUERY MODE ============
useEffect(() => {
  // When switching to custom query mode, clear both parameters and mappings
  if (sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery) {
    // Clear parameters if we're just switching to custom query mode (and not editing an existing one)
    const isJustSwitching = !isEditingCustomQuery && !originalCustomQuery && !customQuery;
    
    if (isJustSwitching) {
      if (parameters.length > 0) {
        console.log('🧹 Clearing parameters when switching to custom query mode');
        setParameters([]);
      }
      if (responseMappings.length > 0) {
        console.log('🧹 Clearing response mappings when switching to custom query mode');
        setResponseMappings([]);
      }
    }
  }
}, [sourceType, isCustomQuery, isEditingCustomQuery]);


  // Auto-populate SOAP service name from database object
// Update SOAP service name when source changes (database object OR custom query)
useEffect(() => {
  if (protocolType === 'soap' && !isEditing) {
    let generatedServiceName = '';
    
    // Case 1: Database object changed
    if (selectedDbObject?.name) {
      generatedServiceName = selectedDbObject.name.charAt(0).toUpperCase() + 
                             selectedDbObject.name.slice(1).toLowerCase();
      generatedServiceName = generatedServiceName.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
      generatedServiceName = generatedServiceName.replace(/[^a-zA-Z0-9]/g, '');
      if (!generatedServiceName.toLowerCase().endsWith('service')) {
        generatedServiceName = generatedServiceName + 'Service';
      }
    }
    // Case 2: Custom query mode with extracted query
    else if ((sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery) && customQuery) {
      const fromMatch = customQuery.match(/FROM\s+([^\s,;]+)/i);
      if (fromMatch && fromMatch[1]) {
        let tableName = fromMatch[1];
        if (tableName.includes('.')) {
          tableName = tableName.split('.').pop();
        }
        generatedServiceName = tableName.charAt(0).toUpperCase() + tableName.slice(1).toLowerCase();
        generatedServiceName = generatedServiceName.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
        generatedServiceName = generatedServiceName.replace(/[^a-zA-Z0-9]/g, '');
      } else {
        generatedServiceName = 'CustomQuery';
      }
      if (!generatedServiceName.toLowerCase().endsWith('service')) {
        generatedServiceName = generatedServiceName + 'Service';
      }
    }
    
    // Only update if the current service name is empty or matches a previously auto-generated name
    if (generatedServiceName) {
      const currentName = soapConfig.serviceName;
      const isCurrentNameAutoGenerated = !currentName || 
                                         currentName === 'APIService' ||
                                         currentName === 'CustomQueryService' ||
                                         (selectedDbObject?.name && currentName === (selectedDbObject.name + 'Service'));
      
      if (isCurrentNameAutoGenerated && currentName !== generatedServiceName) {
        console.log('🔄 Updating SOAP service name from new source:', generatedServiceName);
        setSoapConfig(prev => ({ ...prev, serviceName: generatedServiceName }));
      }
    }
  }
}, [protocolType, selectedDbObject, sourceType, isCustomQuery, isEditingCustomQuery, customQuery, isEditing, soapConfig.serviceName]);

// Update SOAP service name when selected database object changes (in SOAP mode)
useEffect(() => {
  if (protocolType === 'soap' && selectedDbObject?.name && !isEditing) {
    let generatedServiceName = selectedDbObject.name.charAt(0).toUpperCase() + 
                               selectedDbObject.name.slice(1).toLowerCase();
    generatedServiceName = generatedServiceName.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
    
    if (!generatedServiceName.toLowerCase().endsWith('service')) {
      generatedServiceName = generatedServiceName + 'Service';
    }
    
    // Only update if the current service name is empty or matches a previously auto-generated name
    // This prevents overwriting user edits
    const currentName = soapConfig.serviceName;
    const isCurrentNameAutoGenerated = !currentName || 
                                       currentName === (selectedDbObject.name + 'Service') ||
                                       currentName === (selectedDbObject.name.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase()) + 'Service');
    
    if (isCurrentNameAutoGenerated && currentName !== generatedServiceName) {
      console.log('🔄 Updating SOAP service name from new object:', generatedServiceName);
      setSoapConfig(prev => ({ ...prev, serviceName: generatedServiceName }));
    }
  }
}, [protocolType, selectedDbObject, isEditing, soapConfig.serviceName]);

  // Add this effect to update URL when parameters are loaded initially or when parameters array changes
  useEffect(() => {
    if (parameters.length > 0 && protocolType === 'rest') {
      updateEndpointPathFromParameters(parameters);
    }
  }, [parameters, updateEndpointPathFromParameters, protocolType]);

  // Update the handleAddParameter function to check for path parameters
  const handleAddParameter = () => {
  const timestamp = getCurrentTimestamp();
  const defaultLocation = protocolType === 'soap' ? 'body' : 
                         protocolType === 'graphql' ? 'query' : 'query';
  const isPathParam = defaultLocation === 'path';
  
  const newParam = {
    id: `param-${Date.now()}`,
    key: '',
    dbColumn: '',
    oracleType: 'VARCHAR2',
    apiType: 'string',
    parameterLocation: defaultLocation,
    required: isPathParam ? true : true, // Path params are required by default
    description: '',
    example: 'sample',
    validationPattern: '',
    defaultValue: '',
    inBody: false,
    isPrimaryKey: false,
    paramMode: 'IN',
    _isPathParam: isPathParam
  };
  
  setParameters([...parameters, newParam]);
};

  // Update the handleRemoveParameter function to update URL when removing a path parameter
  const handleRemoveParameter = (id) => {
    const paramToRemove = parameters.find(p => p.id === id);
    const wasPathParam = paramToRemove?.parameterLocation === 'path';
    
    setParameters(parameters.filter(param => param.id !== id));
    
    // If removing a path parameter, update the URL
    if (wasPathParam && protocolType === 'rest') {
      setTimeout(() => {
        const remainingParams = parameters.filter(param => param.id !== id);
        updateEndpointPathFromParameters(remainingParams);
      }, 0);
    }
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
      includeInResponse: true,
      inResponse: true,
      paramMode: 'OUT'
    };
    
    // If oracleType is AUTOGENERATE, set nullable to false and add example
    if (newMapping.oracleType === 'AUTOGENERATE') {
      newMapping.nullable = false;
      newMapping.example = getCurrentTimestamp();
    }
    
    setResponseMappings([...responseMappings, newMapping]);
  };

  
const handleResponseMappingChange = (id, field, value) => {
  console.log(`📝 Changing mapping ${id} field ${field} to ${value} for protocol: ${protocolType}`);
  
  setResponseMappings(prevMappings => {
    const updatedMappings = prevMappings.map(mapping => {
      if (mapping.id !== id) return mapping;
      
      const updated = { ...mapping, [field]: value };
      
      if (field === 'apiType') {
        console.log(`Setting apiType to: ${value}`);
        
        // Update the example based on the new apiType
        switch(value) {
          case 'integer':
            updated.example = '123';
            break;
          case 'number':
            updated.example = '123.45';
            break;
          case 'boolean':
            updated.example = 'true';
            break;
          case 'array':
            updated.example = '[]';
            break;
          case 'object':
            updated.example = '{}';
            break;
          default:
            updated.example = 'sample';
        }
      }
      
      if (field === 'oracleType' && value === 'AUTOGENERATE') {
        updated.nullable = false;
        updated.example = getCurrentTimestamp();
        updated.description = updated.description || 'Auto-generated timestamp field';
      }
      
      if (field === 'oracleType' && mapping.oracleType === 'AUTOGENERATE' && value !== 'AUTOGENERATE') {
        updated.nullable = true;
        updated.description = updated.description?.replace('Auto-generated timestamp field', '') || '';
      }
      
      return updated;
    });
    
    // Get OUT mappings with proper example values
    const outMappings = updatedMappings.filter(m => 
      (m.paramMode === 'OUT' || m.paramMode === 'IN/OUT' || m.paramMode === 'IN_OUT' || !m.paramMode)
    );
    
    // Generate response based on protocol type
    if (protocolType === 'soap') {
  const operationName = soapConfig.soapAction || apiDetails.apiCode || 'Operation';
  const soapVersion = soapConfig.version === '1.2' ? 'http://www.w3.org/2003/05/soap-envelope' : 'http://schemas.xmlsoap.org/soap/envelope/';
  
  let bodyContent = '';
  if (outMappings.length > 0) {
    bodyContent = `<${operationName}Response xmlns="${soapConfig.namespace}">
      <success>true</success>
      <message>Request processed successfully</message>
      ${outMappings.map(m => {
        // Get the appropriate example value based on apiType
        let sampleValue = m.example;
        const apiType = m.apiType?.toLowerCase();
        
        if (!sampleValue || sampleValue === '') {
          switch(apiType) {
            case 'integer':
              sampleValue = '123';
              break;
            case 'number':
              sampleValue = '123.45';
              break;
            case 'boolean':
              sampleValue = 'true';
              break;
            case 'array':
              sampleValue = '';  // Empty array -> self-closing tag
              break;
            case 'object':
              sampleValue = '';  // Empty object -> self-closing tag
              break;
            default:
              sampleValue = 'value';
          }
        } else if (apiType === 'boolean') {
          sampleValue = sampleValue === 'true' || sampleValue === true ? 'true' : 'false';
        } else if (apiType === 'array') {
          // For arrays, if example is '[]' or empty, use self-closing tag
          if (sampleValue === '[]' || sampleValue === '') {
            sampleValue = '';
          }
        } else if (apiType === 'object') {
          // For objects, if example is '{}' or empty, use self-closing tag
          if (sampleValue === '{}' || sampleValue === '') {
            sampleValue = '';
          }
        }
        
        // Return self-closing tag if sampleValue is empty string
        if (sampleValue === '') {
          return `<${m.apiField}/>`;
        }
        return `<${m.apiField}>${sampleValue}</${m.apiField}>`;
      }).join('\n      ')}
    </${operationName}Response>`;
  } else {
    bodyContent = `<${operationName}Response xmlns="${soapConfig.namespace}">
      <success>true</success>
      <message>Request processed successfully</message>
    </${operationName}Response>`;
  }
  
  const soapSuccess = `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="${soapVersion}">
  <soap:Header/>
  <soap:Body>
    ${bodyContent}
  </soap:Body>
</soap:Envelope>`;
  
  setResponseBody(prev => ({ 
    ...prev, 
    successSchema: soapSuccess,
    errorSchema: prev.errorSchema
  }));
  setResponseSchemaVersion(prev => prev + 1);
} else if (protocolType === 'graphql') {
  const operationName = graphqlConfig.operationName || 'operation';
  const responseData = {};
  
  outMappings.forEach(mapping => {
    let exampleValue = mapping.example;
    const apiType = mapping.apiType?.toLowerCase();
    
    if (exampleValue && exampleValue !== '') {
      if (apiType === 'integer') {
        responseData[mapping.apiField] = parseInt(exampleValue) || 123;
      } else if (apiType === 'number') {
        responseData[mapping.apiField] = parseFloat(exampleValue) || 123.45;
      } else if (apiType === 'boolean') {
        responseData[mapping.apiField] = exampleValue === 'true' || exampleValue === true;
      } else if (apiType === 'array') {
        // Handle array properly - parse JSON or use empty array
        if (exampleValue === '[]' || exampleValue === '') {
          responseData[mapping.apiField] = [];
        } else {
          try {
            const parsed = JSON.parse(exampleValue);
            responseData[mapping.apiField] = Array.isArray(parsed) ? parsed : [];
          } catch {
            responseData[mapping.apiField] = [];
          }
        }
      } else if (apiType === 'object') {
        // Handle object properly - parse JSON or use empty object
        if (exampleValue === '{}' || exampleValue === '') {
          responseData[mapping.apiField] = {};
        } else {
          try {
            const parsed = JSON.parse(exampleValue);
            responseData[mapping.apiField] = typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {};
          } catch {
            responseData[mapping.apiField] = {};
          }
        }
      } else {
        responseData[mapping.apiField] = exampleValue;
      }
    } else {
      // Fallback to type-based default
      switch(apiType) {
        case 'integer':
          responseData[mapping.apiField] = 123;
          break;
        case 'number':
          responseData[mapping.apiField] = 123.45;
          break;
        case 'boolean':
          responseData[mapping.apiField] = true;
          break;
        case 'array':
          responseData[mapping.apiField] = [];
          break;
        case 'object':
          responseData[mapping.apiField] = {};
          break;
        default:
          responseData[mapping.apiField] = 'sample';
      }
    }
  });
  
  const graphqlSuccess = JSON.stringify({
    data: { [operationName]: responseData }
  }, null, 2);
  
  setResponseBody(prev => ({ 
    ...prev, 
    successSchema: graphqlSuccess,
    errorSchema: prev.errorSchema
  }));
  setResponseSchemaVersion(prev => prev + 1);
} else {
  // REST response generation - FIXED FOR ARRAY/OBJECT
  const sampleData = {};
  
  outMappings.forEach(mapping => {
    // Get the appropriate example value based on apiType
    let exampleValue = mapping.example;
    const apiType = mapping.apiType?.toLowerCase();
    
    if (exampleValue && exampleValue !== '') {
      if (apiType === 'integer') {
        const parsed = parseInt(exampleValue);
        sampleData[mapping.apiField] = isNaN(parsed) ? 123 : parsed;
      } else if (apiType === 'number') {
        const parsed = parseFloat(exampleValue);
        sampleData[mapping.apiField] = isNaN(parsed) ? 123.45 : parsed;
      } else if (apiType === 'boolean') {
        sampleData[mapping.apiField] = exampleValue === 'true' || exampleValue === true;
      } else if (apiType === 'array') {
        // Handle array properly - parse JSON or use empty array
        if (exampleValue === '[]' || exampleValue === '') {
          sampleData[mapping.apiField] = [];
        } else {
          try {
            const parsed = JSON.parse(exampleValue);
            sampleData[mapping.apiField] = Array.isArray(parsed) ? parsed : [];
          } catch {
            sampleData[mapping.apiField] = [];
          }
        }
      } else if (apiType === 'object') {
        // Handle object properly - parse JSON or use empty object
        if (exampleValue === '{}' || exampleValue === '') {
          sampleData[mapping.apiField] = {};
        } else {
          try {
            const parsed = JSON.parse(exampleValue);
            sampleData[mapping.apiField] = typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {};
          } catch {
            sampleData[mapping.apiField] = {};
          }
        }
      } else {
        sampleData[mapping.apiField] = exampleValue;
      }
    } else {
      // Fallback to type-based default
      switch(apiType) {
        case 'integer':
          sampleData[mapping.apiField] = 123;
          break;
        case 'number':
          sampleData[mapping.apiField] = 123.45;
          break;
        case 'boolean':
          sampleData[mapping.apiField] = true;
          break;
        case 'array':
          sampleData[mapping.apiField] = [];
          break;
        case 'object':
          sampleData[mapping.apiField] = {};
          break;
        default:
          if (mapping.format === 'date-time') {
            sampleData[mapping.apiField] = '2024-01-01T00:00:00Z';
          } else if (mapping.apiField === 'id') {
            sampleData[mapping.apiField] = 1;
          } else {
            sampleData[mapping.apiField] = 'sample';
          }
      }
    }
  });
  
  const restSuccess = JSON.stringify({
    success: true,
    data: sampleData,
    message: "Success"
  }, null, 2);
  
  setResponseBody(prev => ({ 
    ...prev, 
    successSchema: restSuccess,
    errorSchema: prev.errorSchema
  }));
  setResponseSchemaVersion(prev => prev + 1);
}
    
    return updatedMappings;
  });
};


useEffect(() => {
  // Only regenerate when parameters change AND we have parameters
  if (parameters.length === 0) return;
  
  console.log('🔄 Parameters changed, regenerating request body sample...');
  
  // For REST protocol with JSON/XML body
  if (protocolType === 'rest') {
    // Skip if body type is none or binary
    if (requestBody.bodyType === 'none' || requestBody.bodyType === 'binary') {
      return;
    }
    
    // Get ALL parameters (not just body parameters)
    const allParams = parameters;
    
    if (allParams.length > 0) {
      if (requestBody.bodyType === 'json') {
        const requestBodyObj = {};
        allParams.forEach(p => {
          let val = p.example;
          
          if (!val || val === '') {
            switch(p.apiType?.toLowerCase()) {
              case 'integer':
                val = 123;
                break;
              case 'number':
                val = 123.45;
                break;
              case 'boolean':
                val = true;
                break;
              case 'array':
                val = [];
                break;
              case 'object':
                val = {};
                break;
              default:
                val = `sample_${p.key}`;
            }
          } else {
            switch(p.apiType?.toLowerCase()) {
              case 'integer':
                val = parseInt(val, 10);
                if (isNaN(val)) val = 123;
                break;
              case 'number':
                val = parseFloat(val);
                if (isNaN(val)) val = 123.45;
                break;
              case 'boolean':
                val = val === 'true' || val === '1' || val === 'yes';
                break;
              case 'array':
                try {
                  const parsed = JSON.parse(val);
                  val = Array.isArray(parsed) ? parsed : [];
                } catch {
                  val = [];
                }
                break;
              case 'object':
                try {
                  const parsed = JSON.parse(val);
                  val = typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {};
                } catch {
                  val = {};
                }
                break;
              default:
                val = val;
            }
          }
          requestBodyObj[p.key] = val;
        });
        const jsonSample = JSON.stringify(requestBodyObj, null, 2);
        if (requestBody.sample !== jsonSample) {
          console.log('✅ Updating JSON request body sample from parameters useEffect');
          handleRequestBodyChange('sample', jsonSample);
        }
        
      } else if (requestBody.bodyType === 'xml') {
        let xmlSample = `<?xml version="1.0" encoding="UTF-8"?>\n<request>\n`;
        allParams.forEach(p => {
          let val = p.example || `sample_${p.key}`;
          if (p.apiType?.toLowerCase() === 'array' || p.apiType?.toLowerCase() === 'object') {
            val = '';
          }
          xmlSample += `  <${p.key}>${val}</${p.key}>\n`;
        });
        xmlSample += `</request>`;
        if (requestBody.sample !== xmlSample) {
          console.log('✅ Updating XML request body sample from parameters useEffect');
          handleRequestBodyChange('sample', xmlSample);
        }
      }
    }
  }
  
  // For GraphQL protocol
  if (protocolType === 'graphql') {
    const inParams = parameters.filter(p => !p.paramMode || p.paramMode === 'IN');
    const outMappingsForQuery = responseMappings.filter(m => 
      (m.paramMode === 'OUT' || m.paramMode === 'IN/OUT' || m.paramMode === 'IN_OUT' || !m.paramMode)
    );
    const operationName = graphqlConfig.operationName || 'query';
    const operationType = graphqlConfig.operationType || 'query';
    
    let variableDefinitions = '';
    let variableUsages = '';
    let variablesObject = {};
    
    if (inParams.length > 0) {
      variableDefinitions = '(' + inParams.map(p => {
        let graphqlType = 'String';
        switch(p.apiType?.toLowerCase()) {
          case 'integer':
            graphqlType = 'Int';
            break;
          case 'number':
            graphqlType = 'Float';
            break;
          case 'boolean':
            graphqlType = 'Boolean';
            break;
          default:
            graphqlType = 'String';
        }
        if (p.required) graphqlType = `${graphqlType}!`;
        return `$${p.key}: ${graphqlType}`;
      }).join(', ') + ')';
      
      variableUsages = '(' + inParams.map(p => `${p.key}: $${p.key}`).join(', ') + ')';
      
      inParams.forEach(p => {
        let val = p.example;
        if (!val || val === '') {
          switch(p.apiType?.toLowerCase()) {
            case 'integer':
              val = 123;
              break;
            case 'number':
              val = 123.45;
              break;
            case 'boolean':
              val = true;
              break;
            case 'array':
              val = [];
              break;
            case 'object':
              val = {};
              break;
            default:
              val = 'sample';
          }
        } else {
          switch(p.apiType?.toLowerCase()) {
            case 'integer':
              val = parseInt(val, 10);
              if (isNaN(val)) val = 123;
              break;
            case 'number':
              val = parseFloat(val);
              if (isNaN(val)) val = 123.45;
              break;
            case 'boolean':
              val = val === 'true' || val === '1' || val === 'yes';
              break;
            case 'array':
              try { val = JSON.parse(val); } catch { val = []; }
              break;
            case 'object':
              try { val = JSON.parse(val); } catch { val = {}; }
              break;
            default:
              val = val;
          }
        }
        variablesObject[p.key] = val;
      });
    }
    
    let queryString = '';
    if (operationType === 'query') {
      if (inParams.length > 0) {
        queryString = `${operationType} ${operationName}${variableDefinitions} {\n  ${operationName}${variableUsages} {\n`;
        if (outMappingsForQuery.length > 0) {
          queryString += outMappingsForQuery.slice(0, 20).map(m => `    ${m.apiField}`).join('\n');
        } else {
          queryString += `    id\n    createdAt\n    updatedAt`;
        }
        queryString += `\n  }\n}`;
      } else {
        queryString = `${operationType} {\n  ${operationName} {\n`;
        if (outMappingsForQuery.length > 0) {
          queryString += outMappingsForQuery.slice(0, 20).map(m => `    ${m.apiField}`).join('\n');
        } else {
          queryString += `    id\n    createdAt\n    updatedAt`;
        }
        queryString += `\n  }\n}`;
      }
    }
    
    const graphqlRequest = JSON.stringify({
      query: queryString,
      variables: variablesObject
    }, null, 2);
    
    if (requestBody.sample !== graphqlRequest) {
      console.log('✅ Updating GraphQL request sample from parameters useEffect');
      handleRequestBodyChange('sample', graphqlRequest);
    }
  }
  
// For SOAP protocol
if (protocolType === 'soap') {
    const freshSoapEnvelope = generateSOAPEnvelope();
    if (requestBody.sample !== freshSoapEnvelope) {
      console.log('✅ Updating SOAP request body sample from parameters useEffect');
      handleRequestBodyChange('sample', freshSoapEnvelope);
      setRequestSchemaVersion(prev => prev + 1);
    }
}
}, [parameters, protocolType, requestBody.bodyType, graphqlConfig.operationName, graphqlConfig.operationType]);


// Add this useEffect right after your parameters useEffect
// This initializes the request body sample when parameters are first loaded
useEffect(() => {
  // Only run when parameters are first loaded and we have parameters
  if (parameters.length > 0 && (!requestBody.sample || requestBody.sample === '' || requestBody.sample === '{}')) {
    console.log('🔄 Initializing request body sample from parameters');
    
    if (protocolType === 'rest') {
      if (requestBody.bodyType === 'json') {
        const requestBodyObj = {};
        parameters.forEach(p => {
          let val = p.example;
          if (!val || val === '') {
            if (p.apiType === 'integer') val = 123;
            else if (p.apiType === 'boolean') val = true;
            else if (p.apiType === 'array') val = [];
            else if (p.apiType === 'object') val = {};
            else val = `sample_${p.key}`;
          }
          requestBodyObj[p.key] = val;
        });
        handleRequestBodyChange('sample', JSON.stringify(requestBodyObj, null, 2));
      }
    }
  }
}, [parameters.length]); // Only runs when parameters length changes, not on every parameters change


// Add this function before the useEffect
const regenerateResponseSchema = useCallback((mappings = null) => {
  // ALWAYS use the passed mappings if provided
  let outMappings = mappings;
  
  // If no mappings were passed, get from current state (FALLBACK only)
  if (!outMappings || outMappings.length === 0) {
    outMappings = responseMappings.filter(m => 
      (m.paramMode === 'OUT' || m.paramMode === 'IN/OUT' || m.paramMode === 'IN_OUT' || !m.paramMode)
    );
    console.log('⚠️ Using fallback mappings from responseMappings state:', outMappings.length);
  }
  
  console.log('🔄 Manually regenerating response schema with mappings length:', outMappings?.length);
  console.log('📊 Mapping details:', outMappings?.map(m => ({ 
    field: m.apiField, 
    type: m.apiType,
    example: m.example
  })));
  
  if (!outMappings || outMappings.length === 0) {
    console.log('⚠️ No mappings found, skipping regeneration');
    return;
  }
  
  if (protocolType === 'graphql') {
    const operationName = graphqlConfig.operationName || 'operation';
    const responseData = {};
    
    outMappings.forEach(mapping => {
      const apiType = mapping.apiType?.toUpperCase();
      
      switch(apiType) {
        case 'INTEGER':
          responseData[mapping.apiField] = 123;
          break;
        case 'NUMBER':
          responseData[mapping.apiField] = 123.45;
          break;
        case 'BOOLEAN':
          responseData[mapping.apiField] = true;
          break;
        case 'ARRAY':
          responseData[mapping.apiField] = [];
          break;
        case 'OBJECT':
          responseData[mapping.apiField] = {};
          break;
        case 'STRING':
        default:
          if (mapping.format === 'date-time') {
            responseData[mapping.apiField] = '2024-01-01T00:00:00Z';
          } else if (mapping.apiField === 'id') {
            responseData[mapping.apiField] = 1;
          } else {
            responseData[mapping.apiField] = 'sample';
          }
          break;
      }
    });
    
    const graphqlSuccess = JSON.stringify({
      data: { [operationName]: responseData }
    }, null, 2);
    
    setResponseBody(prev => ({ ...prev, successSchema: graphqlSuccess }));
    setResponseSchemaVersion(prev => prev + 1);
    
  } else {
    // REST response generation
    const sampleData = {};
    
    outMappings.forEach(mapping => {
      const apiType = mapping.apiType?.toUpperCase();
      
      console.log(`📊 Processing field ${mapping.apiField} with type ${apiType}`);
      
      switch(apiType) {
        case 'INTEGER':
          sampleData[mapping.apiField] = 123;
          break;
        case 'NUMBER':
          sampleData[mapping.apiField] = 123.45;
          break;
        case 'BOOLEAN':
          sampleData[mapping.apiField] = true;
          break;
        case 'ARRAY':
          sampleData[mapping.apiField] = [];
          break;
        case 'OBJECT':
          sampleData[mapping.apiField] = {};
          break;
        case 'STRING':
        default:
          if (mapping.format === 'date-time') {
            sampleData[mapping.apiField] = '2024-01-01T00:00:00Z';
          } else if (mapping.apiField === 'id') {
            sampleData[mapping.apiField] = 1;
          } else {
            sampleData[mapping.apiField] = 'sample';
          }
          break;
      }
    });
    
    console.log('📊 Generated sample data with types:', sampleData);
    
    const restSuccess = JSON.stringify({
      success: true,
      data: sampleData,
      message: 'Request processed successfully',
      metadata: {
        timestamp: '{{timestamp}}',
        apiVersion: apiDetails.version,
        requestId: '{{requestId}}'
      }
    }, null, 2);
    
    console.log('📊 Setting success schema to:', restSuccess.substring(0, 200));
    setResponseBody(prev => ({ ...prev, successSchema: restSuccess }));
    setResponseSchemaVersion(prev => prev + 1);
  }
}, [protocolType, graphqlConfig.operationName, apiDetails.version, responseMappings]);


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

  // Handle auth configuration changes with validation
  const handleAuthConfigChange = (field, value) => {
    setAuthConfig(prev => ({ ...prev, [field]: value }));
    
    // Clear validation error for this field
    setValidationErrors(prev => ({ ...prev, [field]: null }));
  };


  // Force textarea to update when requestBody.sample changes
useEffect(() => {
  // This forces a re-render of the textarea by incrementing a counter
  // when the sample changes in a way that React might not detect
  if (requestBody.sample) {
    setRequestSchemaVersion(prev => prev + 1);
  }
}, [requestBody.sample]);


  // Handle request body configuration
  const handleRequestBodyChange = (field, value) => {
  setRequestBody(prev => ({ ...prev, [field]: value }));
  if (field === 'sample') {
    // Force a re-render by incrementing the version
    setRequestSchemaVersion(prev => prev + 1);
  }
};

  // Handle response body configuration
  const handleResponseBodyChange = (field, value) => {
    setResponseBody(prev => ({ ...prev, [field]: value }));
  };

  // Handle tests configuration
  const handleTestsChange = (field, value) => {
    if (field === 'testData') {
      if (value === '' || value === null || value === undefined) {
        setTests(prev => ({ ...prev, [field]: {} }));
      } else if (typeof value === 'string') {
        const trimmed = value.trim();
        if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
          try {
            const parsed = JSON.parse(value);
            setTests(prev => ({ ...prev, [field]: parsed }));
          } catch (e) {
            setTests(prev => ({ ...prev, [field]: { value } }));
          }
        } else if (trimmed !== '') {
          setTests(prev => ({ ...prev, [field]: { value } }));
        } else {
          setTests(prev => ({ ...prev, [field]: {} }));
        }
      } else if (typeof value === 'object' && value !== null) {
        setTests(prev => ({ ...prev, [field]: value }));
      } else {
        setTests(prev => ({ ...prev, [field]: {} }));
      }
    } else if (field === 'testQueries') {
      if (Array.isArray(value)) {
        setTests(prev => ({ ...prev, [field]: value }));
      } else if (typeof value === 'string') {
        const lines = value.split('\n').filter(line => line.trim() !== '');
        setTests(prev => ({ ...prev, [field]: lines }));
      } else {
        setTests(prev => ({ ...prev, [field]: [] }));
      }
    } else {
      setTests(prev => ({ ...prev, [field]: value }));
    }
  };

  // Handle settings configuration
  const handleSettingsChange = (field, value) => {
    setSettings(prev => ({ ...prev, [field]: value }));
  };

  // ==================== OBJECT SELECTOR FUNCTIONS ====================

  // Update loadSelectedObjectDetails to close modal first and handle both database types
const loadSelectedObjectDetails = useCallback(async (object) => {
  if (!authToken || !object) return;

  console.log('🔍 Selected object details:', {
    name: object.name,
    type: object.type,
    databaseType: object.databaseType,
    owner: object.owner
  });

  // Close the selector modal immediately
  setShowObjectSelector(false);
  
  // Show loading in main modal
  setLoading(true);
  setObjectSelectorError(null);

  try {
    let response;
    
    // Determine if this is PostgreSQL (case-insensitive comparison)
    const isPostgreSQL = object.databaseType?.toLowerCase() === 'postgresql';
    
    console.log(`📡 Using ${isPostgreSQL ? 'PostgreSQL' : 'Oracle'} controller for:`, object.name);
    
    // Choose the correct controller based on the object's database type
    if (isPostgreSQL) {
      // Use PostgreSQL controller
      const { getObjectDetails } = await import('../../controllers/PostgreSQLSchemaController.js');
      response = await getObjectDetails(authToken, {
        objectType: object.type,
        objectName: object.name,
        owner: object.owner,
        schema: object.owner
      });
    } else {
      // Default to Oracle controller
      const { getObjectDetails } = await import('../../controllers/OracleSchemaController.js');
      response = await getObjectDetails(authToken, {
        objectType: object.type,
        objectName: object.name,
        owner: object.owner
      });
    }

    console.log('📦 getObjectDetails response:', response);

    const responseData = response?.data || {};
    let parameters = [];
    let columns = [];

    // Extract parameters for procedures/functions
    if (object.type === 'PROCEDURE' || object.type === 'FUNCTION') {
      if (responseData.parameters && Array.isArray(responseData.parameters)) {
        parameters = responseData.parameters;
      } else if (responseData.arguments && Array.isArray(responseData.arguments)) {
        parameters = responseData.arguments;
      } else if (responseData.details?.parameters && Array.isArray(responseData.details.parameters)) {
        parameters = responseData.details.parameters;
      }
    }

    // Extract columns for tables/views
    if (object.type === 'TABLE' || object.type === 'VIEW') {
      if (responseData.columns && Array.isArray(responseData.columns)) {
        columns = responseData.columns;
      } else if (responseData.targetObjectDetails?.columns) {
        columns = responseData.targetObjectDetails.columns;
      } else if (responseData.details?.columns && Array.isArray(responseData.details.columns)) {
        columns = responseData.details.columns;
      }
    }

    // Determine the normalized database type for the radio buttons
    const normalizedDbType = object.databaseType?.toLowerCase() === 'postgresql' ? 'postgresql' : 'oracle';
    
    // CRITICAL FIX: Set current database type BEFORE creating the detailed object
    setCurrentDatabaseType(normalizedDbType);
    console.log('🎯 Set currentDatabaseType to:', normalizedDbType);

    // Create a selected object with all details
    const detailedObject = {
      ...object,
      ...responseData,
      parameters: parameters,
      columns: columns,
      comment: responseData.comment || responseData.COMMENTS,
      databaseType: normalizedDbType,
      details: {
        ...responseData.details,
        parameters: parameters,
        columns: columns
      }
    };

    // Set the selected object and populate form
    setSelectedDbObject(detailedObject);
    
    // Set source type to database_object
    setSourceType('database_object');
    setIsEditingCustomQuery(false);
    
    // ============ CRITICAL FIX: Pass forceRefresh = true to force reload ============
    await populateFormFromObject(detailedObject, true, true);  // true for preserveExistingApiDetails, true for forceRefresh
    
    console.log('✅ Object details loaded and form populated successfully');
    
  } catch (error) {
    console.error('❌ Error loading object details:', error);
    setObjectSelectorError('Failed to load object details: ' + error.message);
  } finally {
    setLoading(false);
  }
}, [authToken, populateFormFromObject]);


// ============ COMPLETE FIXED populateFormFromApiData ============
const populateFormFromApiData = useCallback(async (apiData) => {
    console.log('📝 populateFormFromApiData called with:', apiData);
    
    // Extract from nested data if present
    const sourceData = apiData.data || apiData;
    
    // ============ CRITICAL FIX: Extract and preserve database type FIRST ============
    let extractedDatabaseType = null;
    
    // Priority order for database type extraction
    if (sourceData.databaseType) {
        extractedDatabaseType = sourceData.databaseType;
        console.log('📦 Found databaseType at sourceData.databaseType:', extractedDatabaseType);
    } else if (sourceData.sourceObject?.databaseType) {
        extractedDatabaseType = sourceData.sourceObject.databaseType;
        console.log('📦 Found databaseType at sourceObject.databaseType:', extractedDatabaseType);
    } else if (sourceData.schemaConfig?.databaseType) {
        extractedDatabaseType = sourceData.schemaConfig.databaseType;
        console.log('📦 Found databaseType at schemaConfig.databaseType:', extractedDatabaseType);
    } else if (apiData.databaseType) {
        extractedDatabaseType = apiData.databaseType;
        console.log('📦 Found databaseType at apiData.databaseType:', extractedDatabaseType);
    } else if (apiData.sourceObject?.databaseType) {
        extractedDatabaseType = apiData.sourceObject.databaseType;
        console.log('📦 Found databaseType at apiData.sourceObject.databaseType:', extractedDatabaseType);
    }
    
    // ============ FIX FOR REST APIS: Check the selectedObject prop ============
    if (!extractedDatabaseType && isEditing && selectedObject) {
        console.log('🔍 Checking selectedObject prop for database type (REST API fix)');
        
        // Check all possible locations in selectedObject
        if (selectedObject.databaseType) {
            extractedDatabaseType = selectedObject.databaseType;
            console.log('📦 Found databaseType in selectedObject.databaseType:', extractedDatabaseType);
        } else if (selectedObject.data?.databaseType) {
            extractedDatabaseType = selectedObject.data.databaseType;
            console.log('📦 Found databaseType in selectedObject.data.databaseType:', extractedDatabaseType);
        } else if (selectedObject.sourceObject?.databaseType) {
            extractedDatabaseType = selectedObject.sourceObject.databaseType;
            console.log('📦 Found databaseType in selectedObject.sourceObject.databaseType:', extractedDatabaseType);
        } else if (selectedObject.schemaConfig?.databaseType) {
            extractedDatabaseType = selectedObject.schemaConfig.databaseType;
            console.log('📦 Found databaseType in selectedObject.schemaConfig.databaseType:', extractedDatabaseType);
        } else if (selectedObject.apiDetails?.databaseType) {
            extractedDatabaseType = selectedObject.apiDetails.databaseType;
            console.log('📦 Found databaseType in selectedObject.apiDetails.databaseType:', extractedDatabaseType);
        }
        
        // Also check if the API name or code indicates PostgreSQL
        if (!extractedDatabaseType) {
            const apiName = (selectedObject.name || selectedObject.apiName || '').toLowerCase();
            const apiCode = (selectedObject.apiCode || '').toLowerCase();
            
            if (apiName.includes('postgres') || apiCode.includes('pg_') || apiCode.includes('postgres')) {
                extractedDatabaseType = 'postgresql';
                console.log('🔍 Inferred PostgreSQL from API name/code:', apiName || apiCode);
            } else if (apiName.includes('oracle') || apiCode.includes('ora_')) {
                extractedDatabaseType = 'oracle';
                console.log('🔍 Inferred Oracle from API name/code:', apiName || apiCode);
            }
        }
    }
    
    // ============ Also check the databaseType prop passed to the modal ============
    if (!extractedDatabaseType && databaseType) {
        extractedDatabaseType = databaseType;
        console.log('📦 Using databaseType from modal prop:', extractedDatabaseType);
    }
    
    // Normalize the database type
    let normalizedDbType = 'oracle';
    if (extractedDatabaseType) {
        const dbTypeLower = extractedDatabaseType.toString().toLowerCase();
        if (dbTypeLower === 'postgresql' || dbTypeLower === 'postgres') {
            normalizedDbType = 'postgresql';
            console.log('🎯 Normalized database type to: postgresql');
        } else if (dbTypeLower === 'oracle') {
            normalizedDbType = 'oracle';
            console.log('🎯 Normalized database type to: oracle');
        }
    }
    
    // CRITICAL: Set currentDatabaseType BEFORE anything else
    setCurrentDatabaseType(normalizedDbType);
    console.log('✅ Set currentDatabaseType to:', normalizedDbType);
    
    // ============ CHECK FOR CUSTOM QUERY FIRST ============
    const isCustomQueryApi = 
        sourceData?.isCustomQuery === true || 
        sourceData?.useCustomQuery === true ||
        sourceData?.sourceObject?.type === 'CUSTOM_QUERY' ||
        sourceData?.customSelectStatement ||
        sourceData?.sourceObject?.customSelectStatement ||
        sourceData?.sourceObject?.query ||
        sourceData?.customQueryText ||
        sourceData?.isCustomQuery === 'true' ||
        sourceData?.useCustomQuery === 'true' ||
        (sourceData?.sourceObject && sourceData.sourceObject.type === 'CUSTOM_QUERY') ||
        (apiData?.isCustomQuery === true) ||
        (apiData?.useCustomQuery === true);
    
    console.log('🔍 Custom query detection:', {
        isCustomQueryApi,
        hasCustomSelectStatement: !!sourceData?.customSelectStatement,
        hasSourceObjectQuery: !!sourceData?.sourceObject?.query,
        hasCustomQueryText: !!sourceData?.customQueryText,
        sourceObjectType: sourceData?.sourceObject?.type,
        databaseType: normalizedDbType
    });
    
    if (isCustomQueryApi) {
        console.log('✅ Detected custom query API in populateFormFromApiData');
        setIsEditingCustomQuery(true);
        setSourceType('custom_query');
        setIsCustomQuery(true);
        
        let customQueryText = 
            sourceData?.customSelectStatement ||
            sourceData?.sourceObject?.customSelectStatement ||
            sourceData?.sourceObject?.query ||
            sourceData?.sourceObject?.customQuery ||
            sourceData?.customQueryText ||
            '';
        
        if (!customQueryText && apiData?.customSelectStatement) {
            customQueryText = apiData.customSelectStatement;
        }
        if (!customQueryText && apiData?.sourceObject?.customSelectStatement) {
            customQueryText = apiData.sourceObject.customSelectStatement;
        }
        if (!customQueryText && apiData?.sourceObject?.query) {
            customQueryText = apiData.sourceObject.query;
        }
        
        if (customQueryText) {
            setCustomQuery(customQueryText);
            setOriginalCustomQuery(customQueryText);
            console.log('📝 Set custom query:', customQueryText.substring(0, 100));
        } else {
            console.warn('⚠️ Custom query API detected but no query text found');
        }
        
        // Use the already extracted database type for custom query
        console.log('📦 Database type for custom query:', normalizedDbType);
        
    } else {
        setIsEditingCustomQuery(false);
        setSourceType('database_object');
        setCustomQuery('');
        setOriginalCustomQuery('');
        setIsCustomQuery(false);
        
        // Use the already extracted database type for database object
        console.log('📦 Database type for database object:', normalizedDbType);
    }
    
    // ============ SET PROTOCOL TYPE ============
    let protocol = 'rest';
    if (sourceData.protocolType) {
        protocol = sourceData.protocolType;
    } else if (sourceData.data?.protocolType) {
        protocol = sourceData.data.protocolType;
    } else if (apiData.protocolType) {
        protocol = apiData.protocolType;
    }
    setProtocolType(protocol);
    console.log('🔌 Protocol type set to:', protocol);
    
    // ============ SET SOAP CONFIG ============
    let soapConfigData = null;
    if (sourceData.soapConfig) {
        soapConfigData = sourceData.soapConfig;
    } else if (sourceData.data?.soapConfig) {
        soapConfigData = sourceData.data.soapConfig;
    } else if (apiData.soapConfig) {
        soapConfigData = apiData.soapConfig;
    }
    
    if (soapConfigData) {
        setSoapConfig({
            version: soapConfigData.version || '1.1',
            bindingStyle: soapConfigData.bindingStyle || 'document',
            encodingStyle: soapConfigData.encodingStyle || 'literal',
            soapAction: soapConfigData.soapAction || '',
            wsdlUrl: soapConfigData.wsdlUrl || '',
            namespace: soapConfigData.namespace || 'http://tempuri.org/',
            serviceName: soapConfigData.serviceName || '',
            portName: soapConfigData.portName || '',
            useAsyncPattern: soapConfigData.useAsyncPattern || false,
            includeMtom: soapConfigData.includeMtom || false,
            soapHeaderElements: soapConfigData.soapHeaderElements || []
        });
        console.log('📦 SOAP config loaded with service name:', soapConfigData.serviceName);
    }
    
    // ============ SET GRAPHQL CONFIG ============
    let graphqlConfigData = null;
    if (sourceData.graphqlConfig) {
        graphqlConfigData = sourceData.graphqlConfig;
    } else if (sourceData.data?.graphqlConfig) {
        graphqlConfigData = sourceData.data.graphqlConfig;
    } else if (apiData.graphqlConfig) {
        graphqlConfigData = apiData.graphqlConfig;
    }
    
    // Determine if this is a GraphQL query (not mutation)
    const isGraphQLQuery = protocol === 'graphql' && graphqlConfigData?.operationType === 'query';
    
    if (graphqlConfigData) {
        setGraphqlConfig({
            operationType: graphqlConfigData.operationType || 'query',
            operationName: graphqlConfigData.operationName || '',
            schema: graphqlConfigData.schema || '',
            enableIntrospection: graphqlConfigData.enableIntrospection !== false,
            enablePersistedQueries: graphqlConfigData.enablePersistedQueries || false,
            maxQueryDepth: graphqlConfigData.maxQueryDepth || 10,
            enableBatching: graphqlConfigData.enableBatching || false,
            subscriptionsEnabled: graphqlConfigData.subscriptionsEnabled || false,
            customDirectives: graphqlConfigData.customDirectives || []
        });
        console.log('📦 GraphQL config loaded, operation type:', graphqlConfigData.operationType);
    }
    
    // ============ SET FILE UPLOAD CONFIG ============
    let fileUploadConfigData = null;
    if (sourceData.fileUploadConfig) {
        fileUploadConfigData = sourceData.fileUploadConfig;
    } else if (sourceData.data?.fileUploadConfig) {
        fileUploadConfigData = sourceData.data.fileUploadConfig;
    } else if (apiData.fileUploadConfig) {
        fileUploadConfigData = apiData.fileUploadConfig;
    }
    
    if (fileUploadConfigData) {
        setFileUploadConfig({
            maxFileSize: fileUploadConfigData.maxFileSize || 10485760,
            allowedFileTypes: fileUploadConfigData.allowedFileTypes || ['*/*'],
            multipleFiles: fileUploadConfigData.multipleFiles || false,
            fileParameterName: fileUploadConfigData.fileParameterName || 'file'
        });
        console.log('📦 File upload config loaded');
    }
    
    // ============ SET API DETAILS ============
    setApiDetails({
        apiName: (sourceData.apiName || '').toUpperCase(),
        apiCode: (sourceData.apiCode || '').toUpperCase().replace(/\s+/g, '_'),
        description: sourceData.description || '',
        version: sourceData.version || '1.0.0',
        status: sourceData.status || 'ACTIVE',
        httpMethod: sourceData.httpMethod || 'GET',
        basePath: sourceData.basePath || '/api/v1',
        endpointPath: sourceData.endpointPath || '',
        tags: sourceData.tags || ['default'],
        category: sourceData.category || 'general',
        owner: sourceData.owner || 'CBXDMX',
    });
    
    // ============ SET COLLECTION INFO ============
    if (sourceData.collectionInfo) {
        const collection = collections.find(c => c.id === sourceData.collectionInfo.collectionId);
        if (collection) {
            setSelectedCollection(collection);
            setFolders(collection.folders || []);
            
            if (sourceData.collectionInfo.folderId) {
                const folder = collection.folders?.find(f => f.id === sourceData.collectionInfo.folderId);
                setSelectedFolder(folder || null);
            }
        }
    }
    
    // ============ SET SCHEMA CONFIG ============
    // IMPORTANT: Also preserve database type in schemaConfig
    if (sourceData.schemaConfig && !isCustomQueryApi) {
        setSchemaConfig({
            schemaName: sourceData.schemaConfig.schemaName || '',
            objectType: sourceData.schemaConfig.objectType || '',
            objectName: sourceData.schemaConfig.objectName || '',
            operation: sourceData.schemaConfig.operation || 'SELECT',
            primaryKeyColumn: sourceData.schemaConfig.primaryKeyColumn || '',
            sequenceName: sourceData.schemaConfig.sequenceName || '',
            enablePagination: sourceData.schemaConfig.enablePagination !== undefined ? sourceData.schemaConfig.enablePagination : true,
            pageSize: sourceData.schemaConfig.pageSize || 10,
            enableSorting: sourceData.schemaConfig.enableSorting !== undefined ? sourceData.schemaConfig.enableSorting : true,
            defaultSortColumn: sourceData.schemaConfig.defaultSortColumn || '',
            defaultSortDirection: sourceData.schemaConfig.defaultSortDirection || 'ASC'
        });
    } else if (!isCustomQueryApi && sourceData.sourceObject && sourceData.sourceObject.type !== 'CUSTOM_QUERY') {
        setSchemaConfig({
            schemaName: sourceData.sourceObject.owner || '',
            objectType: sourceData.sourceObject.type || '',
            objectName: sourceData.sourceObject.name || '',
            operation: sourceData.operation || 'SELECT',
            primaryKeyColumn: sourceData.primaryKeyColumn || '',
            sequenceName: sourceData.sequenceName || '',
            enablePagination: sourceData.enablePagination !== undefined ? sourceData.enablePagination : true,
            pageSize: sourceData.pageSize || 10,
            enableSorting: sourceData.enableSorting !== undefined ? sourceData.enableSorting : true,
            defaultSortColumn: sourceData.defaultSortColumn || '',
            defaultSortDirection: sourceData.defaultSortDirection || 'ASC'
        });
    }
    
    // ============ SET SOURCE OBJECT INFO ============
    if (sourceData.sourceObject) {
        setSourceObjectInfo({
            isSynonym: sourceData.sourceObject.isSynonym || false,
            targetType: sourceData.sourceObject.targetType || null,
            targetName: sourceData.sourceObject.targetName || null,
            targetOwner: sourceData.sourceObject.targetOwner || null
        });
    }
    
    // ============ SET SELECTED DB OBJECT ============
    // CRITICAL: Use the extracted database type for the selected db object
    if (!isCustomQueryApi && sourceData.sourceObject && sourceData.sourceObject.type !== 'CUSTOM_QUERY') {
        setSelectedDbObject({
            name: sourceData.sourceObject.name || sourceData.schemaConfig?.objectName,
            owner: sourceData.sourceObject.owner || sourceData.schemaConfig?.schemaName,
            type: sourceData.sourceObject.type || sourceData.schemaConfig?.objectType,
            databaseType: normalizedDbType,  // Use the normalized database type
            isSynonym: sourceData.sourceObject.isSynonym || false,
            targetType: sourceData.sourceObject.targetType,
            targetName: sourceData.sourceObject.targetName,
            targetOwner: sourceData.sourceObject.targetOwner,
            columns: sourceData.sourceObject.columns || sourceData.columns,
            parameters: sourceData.sourceObject.parameters || sourceData.parameters
        });
        console.log('📦 Set selectedDbObject with databaseType:', normalizedDbType);
    }
    
    // ============ SET PARAMETERS - WITH IMMEDIATE TYPE NORMALIZATION ============
    if (sourceData.parameters && Array.isArray(sourceData.parameters)) {
        const paramsWithIds = sourceData.parameters.map((p, idx) => {
            const isPathParam = p.parameterLocation === 'path';
            
            let location = p.parameterLocation;
            
            if (!location) {
                if (p.inBody === true || p.inBody === 'true') {
                    location = 'body';
                } else if (p.paramMode === 'OUT' || p.paramMode === 'IN/OUT') {
                    location = 'body';
                } else {
                    location = 'query';
                }
            }
            
            // ============ CRITICAL FIX: Normalize API Type on Load ============
            let normalizedApiType = (p.apiType || 'string').toLowerCase();
            
            // Map common type names from database to our standard types
            const typeMap = {
                'str': 'string',
                'text': 'string', 
                'varchar': 'string',
                'varchar2': 'string',
                'int': 'integer',
                'integer': 'integer',
                'number': 'integer',
                'numeric': 'integer', 
                'decimal': 'integer',
                'float': 'number',
                'double': 'number',
                'bool': 'boolean',
                'boolean': 'boolean',
                'arr': 'array',
                'array': 'array',
                'obj': 'object',
                'object': 'object',
                'file': 'file',
                'binary': 'file'
            };
            
            if (typeMap[normalizedApiType]) {
                normalizedApiType = typeMap[normalizedApiType];
                console.log(`🔄 Normalized parameter "${p.key}" type from "${p.apiType}" to "${normalizedApiType}"`);
            }
            
            // ============ FIX EXAMPLE BASED ON CORRECTED TYPE ============
            let normalizedExample = p.example;
            
            if (normalizedApiType === 'integer') {
                const parsed = parseInt(normalizedExample, 10);
                if (isNaN(parsed)) {
                    normalizedExample = '123';
                    console.log(`🔄 Fixed example for "${p.key}" from "${p.example}" to "${normalizedExample}" (integer)`);
                }
            } else if (normalizedApiType === 'number') {
                const parsed = parseFloat(normalizedExample);
                if (isNaN(parsed)) {
                    normalizedExample = '123.45';
                    console.log(`🔄 Fixed example for "${p.key}" from "${p.example}" to "${normalizedExample}" (number)`);
                }
            } else if (normalizedApiType === 'boolean') {
                const isValidBoolean = normalizedExample === 'true' || normalizedExample === 'false' || 
                                       normalizedExample === '1' || normalizedExample === '0' ||
                                       normalizedExample === 'yes' || normalizedExample === 'no';
                if (!isValidBoolean) {
                    normalizedExample = 'true';
                    console.log(`🔄 Fixed example for "${p.key}" from "${p.example}" to "${normalizedExample}" (boolean)`);
                } else {
                    normalizedExample = (normalizedExample === 'true' || normalizedExample === '1' || normalizedExample === 'yes') ? 'true' : 'false';
                }
            } else if (normalizedApiType === 'array') {
                if (!normalizedExample || normalizedExample === '' || normalizedExample === 'null') {
                    normalizedExample = '[]';
                    console.log(`🔄 Fixed example for "${p.key}" to "[]" (array)`);
                }
            } else if (normalizedApiType === 'object') {
                if (!normalizedExample || normalizedExample === '' || normalizedExample === 'null') {
                    normalizedExample = '{}';
                    console.log(`🔄 Fixed example for "${p.key}" to "{}" (object)`);
                }
            } else if (normalizedApiType === 'string') {
                if (!normalizedExample || normalizedExample === '' || normalizedExample === 'null') {
                    normalizedExample = 'sample';
                    console.log(`🔄 Fixed example for "${p.key}" to "sample" (string)`);
                }
            }
            
            // For GraphQL queries, all parameters MUST be in 'query' location
            if (isGraphQLQuery && location === 'body') {
                location = 'query';
                console.log(`🔄 GraphQL Query: Converting parameter "${p.key || p.parameterName}" from 'body' to 'query' location`);
            }
            
            let inBodyValue = p.inBody;
            if (inBodyValue === undefined) {
                inBodyValue = location === 'body';
            }
            
            if (isGraphQLQuery) {
                inBodyValue = false;
            }
            
            return {
                ...p,
                id: p.id || `param-${Date.now()}-${idx}`,
                key: p.key || p.parameterName,
                dbColumn: p.dbColumn || p.key,
                oracleType: p.oracleType || p.dataType || 'VARCHAR2',
                apiType: normalizedApiType,
                parameterLocation: location,
                required: isPathParam ? true : (p.required !== undefined ? p.required : true),
                description: p.description || `Parameter: ${p.key || p.parameterName}`,
                example: normalizedExample,
                validationPattern: p.validationPattern || '',
                defaultValue: p.defaultValue || '',
                inBody: inBodyValue,
                isPrimaryKey: p.isPrimaryKey || false,
                paramMode: p.paramMode || (isCustomQueryApi ? 'IN' : 'IN'),
                _isPathParam: isPathParam
            };
        });
        
        setParameters(paramsWithIds);
        console.log('📦 Loaded parameters from API data:', paramsWithIds.length);
    }
    
    // ============ SET RESPONSE MAPPINGS ============
    if (sourceData.responseMappings && Array.isArray(sourceData.responseMappings)) {
        const mappingsWithIds = sourceData.responseMappings.map((m, idx) => ({
            ...m,
            id: m.id || `mapping-${Date.now()}-${idx}`,
            apiField: m.apiField || m.fieldName,
            dbColumn: m.dbColumn || m.columnName,
            oracleType: m.oracleType || m.dataType || 'VARCHAR2',
            apiType: m.apiType || 'string',
            format: m.format || '',
            nullable: m.nullable !== undefined ? m.nullable : true,
            isPrimaryKey: m.isPrimaryKey || false,
            includeInResponse: m.includeInResponse !== undefined ? m.includeInResponse : true,
            inResponse: m.inResponse !== undefined ? m.inResponse : true,
            paramMode: m.paramMode || 'OUT'
        }));
        setResponseMappings(mappingsWithIds);
        console.log('📦 Loaded response mappings from API data:', mappingsWithIds.length);
    } else if (isCustomQueryApi) {
        setResponseMappings([]);
    }
    
    // ============ SET REQUEST BODY ============
    if (sourceData.requestBody) {
        let bodyType = sourceData.requestBody.bodyType || (isCustomQueryApi ? 'none' : 'json');
        
        if (protocol === 'graphql') {
            bodyType = 'graphql';
        }
        
        setRequestBody({
            bodyType: bodyType,
            sample: sourceData.requestBody.sample || '',
            requiredFields: sourceData.requestBody.requiredFields || [],
            validateSchema: sourceData.requestBody.validateSchema !== undefined ? sourceData.requestBody.validateSchema : true,
            maxSize: sourceData.requestBody.maxSize || 1048576,
            allowedMediaTypes: sourceData.requestBody.allowedMediaTypes || ['application/json']
        });
    } else {
        setRequestBody({
            bodyType: protocol === 'graphql' ? 'graphql' : (isCustomQueryApi ? 'none' : 'json'),
            sample: null,
            requiredFields: [],
            validateSchema: true,
            maxSize: 1048576,
            allowedMediaTypes: ['application/json']
        });
    }
    
    // ============ SET RESPONSE BODY ============
    if (sourceData.responseBody) {
        setResponseBody({
            successSchema: sourceData.responseBody.successSchema || '{\n  "success": true,\n  "data": {},\n  "message": "Request processed successfully"\n}',
            errorSchema: sourceData.responseBody.errorSchema || '{\n  "success": false,\n  "error": {\n    "code": "ERROR_CODE",\n    "message": "Error description",\n    "details": {}\n  }\n}',
            includeMetadata: sourceData.responseBody.includeMetadata !== undefined ? sourceData.responseBody.includeMetadata : true,
            metadataFields: sourceData.responseBody.metadataFields || ['timestamp', 'apiVersion', 'requestId'],
            contentType: sourceData.responseBody.contentType || 'application/json',
            compression: sourceData.responseBody.compression || 'gzip'
        });
    }
    
    // ============ SET AUTH CONFIG ============
    if (sourceData.authConfig) {
        setAuthConfig({
            authType: sourceData.authConfig.authType || 'none',
            apiKeyHeader: sourceData.authConfig.apiKeyHeader || 'X-API-Key',
            apiKeyValue: sourceData.authConfig.apiKeyValue || '',
            apiSecretHeader: sourceData.authConfig.apiSecretHeader || 'X-API-Secret',
            apiSecretValue: sourceData.authConfig.apiSecretValue || '',
            jwtToken: sourceData.authConfig.jwtToken || '',
            jwtIssuer: sourceData.authConfig.jwtIssuer || 'api.example.com',
            basicUsername: sourceData.authConfig.basicUsername || '',
            basicPassword: sourceData.authConfig.basicPassword || '',
            ipWhitelist: sourceData.authConfig.ipWhitelist || '',
            rateLimitRequests: sourceData.authConfig.rateLimitRequests || 100,
            rateLimitPeriod: sourceData.authConfig.rateLimitPeriod || 'minute',
            enableRateLimiting: sourceData.authConfig.enableRateLimiting || false,
            corsOrigins: sourceData.authConfig.corsOrigins || ['*'],
            auditLevel: sourceData.authConfig.auditLevel || 'standard'
        });
    }
    
    // ============ SET HEADERS ============
    if (sourceData.headers && Array.isArray(sourceData.headers)) {
        const headersWithIds = sourceData.headers.map((h, idx) => ({
            ...h,
            id: h.id || `header-${Date.now()}-${idx}`,
            key: h.key || h.name,
            value: h.value || '',
            required: h.required !== undefined ? h.required : false,
            description: h.description || ''
        }));
        setHeaders(headersWithIds);
    }
    
    // ============ SET SETTINGS ============
    if (sourceData.settings) {
        setSettings({
            timeout: sourceData.settings.timeout || 30000,
            maxRecords: sourceData.settings.maxRecords || 1000,
            enableLogging: sourceData.settings.enableLogging !== undefined ? sourceData.settings.enableLogging : true,
            logLevel: sourceData.settings.logLevel || 'INFO',
            enableCaching: sourceData.settings.enableCaching || false,
            cacheTtl: sourceData.settings.cacheTtl || 300,
            generateSwagger: sourceData.settings.generateSwagger !== undefined ? sourceData.settings.generateSwagger : true,
            generatePostman: sourceData.settings.generatePostman !== undefined ? sourceData.settings.generatePostman : true,
            generateClientSDK: sourceData.settings.generateClientSDK !== undefined ? sourceData.settings.generateClientSDK : true,
            enableMonitoring: sourceData.settings.enableMonitoring !== undefined ? sourceData.settings.enableMonitoring : true,
            enableAlerts: sourceData.settings.enableAlerts || false,
            alertEmail: sourceData.settings.alertEmail || '',
            enableTracing: sourceData.settings.enableTracing || false,
            corsEnabled: sourceData.settings.corsEnabled !== undefined ? sourceData.settings.corsEnabled : true
        });
    }
    
    // ============ SET TESTS ============
    if (sourceData.tests) {
        setTests({
            testConnection: sourceData.tests.testConnection !== undefined ? sourceData.tests.testConnection : true,
            testObjectAccess: sourceData.tests.testObjectAccess !== undefined ? sourceData.tests.testObjectAccess : true,
            testPrivileges: sourceData.tests.testPrivileges !== undefined ? sourceData.tests.testPrivileges : true,
            testDataTypes: sourceData.tests.testDataTypes !== undefined ? sourceData.tests.testDataTypes : true,
            testNullConstraints: sourceData.tests.testNullConstraints !== undefined ? sourceData.tests.testNullConstraints : true,
            testUniqueConstraints: sourceData.tests.testUniqueConstraints || false,
            testForeignKeyReferences: sourceData.tests.testForeignKeyReferences || false,
            testQueryPerformance: sourceData.tests.testQueryPerformance !== undefined ? sourceData.tests.testQueryPerformance : true,
            performanceThreshold: sourceData.tests.performanceThreshold || 1000,
            testWithSampleData: sourceData.tests.testWithSampleData !== undefined ? sourceData.tests.testWithSampleData : true,
            sampleDataRows: sourceData.tests.sampleDataRows || 10,
            testProcedureExecution: sourceData.tests.testProcedureExecution !== undefined ? sourceData.tests.testProcedureExecution : true,
            testFunctionReturn: sourceData.tests.testFunctionReturn !== undefined ? sourceData.tests.testFunctionReturn : true,
            testExceptionHandling: sourceData.tests.testExceptionHandling !== undefined ? sourceData.tests.testExceptionHandling : true,
            testSQLInjection: sourceData.tests.testSQLInjection !== undefined ? sourceData.tests.testSQLInjection : true,
            testAuthentication: sourceData.tests.testAuthentication !== undefined ? sourceData.tests.testAuthentication : true,
            testAuthorization: sourceData.tests.testAuthorization !== undefined ? sourceData.tests.testAuthorization : true,
            testData: sourceData.tests.testData || {},
            testQueries: sourceData.tests.testQueries || []
        });
    }
    
    // ============ LOG COMPLETION ============
    console.log('✅ Form populated from API data', {
        isCustomQuery: isCustomQueryApi,
        sourceType: isCustomQueryApi ? 'custom_query' : 'database_object',
        protocolType: protocol,
        isGraphQLQuery: isGraphQLQuery,
        apiName: sourceData.apiName,
        apiCode: sourceData.apiCode,
        parametersCount: sourceData.parameters?.length || 0,
        responseMappingsCount: sourceData.responseMappings?.length || 0,
        databaseType: normalizedDbType,
        hasSoapConfig: !!soapConfigData,
        hasGraphqlConfig: !!graphqlConfigData
    });
    
}, [collections, databaseType]);  // Remove currentDatabaseType from dependencies



// Function to populate form from selected object - FIXED FOR SOAP/GRAPHQL
const populateFormFromObject = useCallback((object, preserveExistingApiDetails = false, forceRefresh = false) => {
  // ============ CRITICAL FIX: Force refresh when manually selecting a new object ============
  // If forceRefresh is true, we should populate regardless of existing parameters
  if (!forceRefresh) {
    // Skip if editing API with existing parameters
    if (isEditing && parameters.length > 0 && object.type === 'PROCEDURE') {
      console.log('⚠️ Skipping populateFormFromObject - Already editing API with existing parameters');
      console.log('📊 Existing parameters count:', parameters.length);
      console.log('📊 Object being passed:', object.name);
      return;
    }
    
    // Also skip if we're preserving existing API details AND we already have parameters
    if (preserveExistingApiDetails && parameters?.length > 0) {
      console.log('⚠️ Skipping populateFormFromObject - Preserving existing parameters');
      return;
    }
  } else {
    console.log('🔄 Force refresh mode - clearing existing data and reloading from new object:', object.name);
    // Clear existing data when force refreshing
    setParameters([]);
    setResponseMappings([]);
    setSourceObjectInfo({
      isSynonym: false,
      targetType: null,
      targetName: null,
      targetOwner: null
    });
  }
  
  console.log('📝 populateFormFromObject called with object:', object);
  
  // Ensure source type is database_object
  setSourceType('database_object');
  setIsEditingCustomQuery(false);
  
  // Set the database type for radio buttons
  const normalizedDbType = object.databaseType?.toLowerCase() === 'postgresql' ? 'postgresql' : 'oracle';
  setCurrentDatabaseType(normalizedDbType);
  console.log('🎯 Set currentDatabaseType to:', normalizedDbType);
  
  const objectType = object.type?.toUpperCase() || object.objectType?.toUpperCase();
  const baseName = object.name?.toLowerCase() || object.objectName?.toLowerCase() || '';
  const endpointPath = baseName ? `/${baseName.replace(/_/g, '-').toLowerCase()}` : '';

  // ============ FIX: Determine operation based on PROTOCOL TYPE ============
  let operation = 'SELECT';
  let httpMethod = apiDetails.httpMethod;
  
  // For SOAP protocol, ALWAYS use the SOAP Action to determine operation
  // NEVER derive operation from HTTP method
  if (protocolType === 'soap') {
    // Use the SOAP Action to determine operation
    if (soapConfig.soapAction) {
      const soapAction = soapConfig.soapAction.toUpperCase();
      if (soapAction === 'SELECT') {
        operation = 'SELECT';
      } else if (soapAction === 'INSERT') {
        operation = 'INSERT';
      } else if (soapAction === 'UPDATE') {
        operation = 'UPDATE';
      } else if (soapAction === 'DELETE') {
        operation = 'DELETE';
      } else if (soapAction === 'EXECUTE') {
        operation = 'EXECUTE';
      } else {
        operation = 'SELECT';
      }
    } else {
      // Default for SOAP is SELECT (not INSERT)
      operation = 'SELECT';
    }
    // SOAP always uses POST
    httpMethod = 'POST';
    console.log('🔧 SOAP protocol - operation determined by SOAP Action:', operation);
  }
  // For GraphQL protocol, operation type is determined by GraphQL config
  else if (protocolType === 'graphql') {
    // GraphQL operation type comes from graphqlConfig.operationType
    const graphqlOpType = graphqlConfig.operationType || 'query';
    if (graphqlOpType === 'query') {
      operation = 'SELECT';
    } else if (graphqlOpType === 'mutation') {
      // Mutations could be INSERT, UPDATE, or DELETE based on naming
      const opName = (graphqlConfig.operationName || '').toLowerCase();
      if (opName.includes('create')) {
        operation = 'INSERT';
      } else if (opName.includes('update')) {
        operation = 'UPDATE';
      } else if (opName.includes('delete')) {
        operation = 'DELETE';
      } else {
        operation = 'EXECUTE';
      }
    } else {
      operation = 'SELECT';
    }
    // GraphQL always uses POST
    httpMethod = 'POST';
    console.log('🔧 GraphQL protocol - operation determined by GraphQL operation type:', operation);
  }
  // For procedures/functions/packages in REST, always use EXECUTE
  else if (objectType === 'PROCEDURE' || objectType === 'FUNCTION' || objectType === 'PACKAGE') {
    operation = 'EXECUTE';
    httpMethod = 'POST';
  } 
  // For REST API tables/views, map HTTP method to operation
  else {
    switch(httpMethod) {
      case 'POST':
        operation = 'INSERT';
        break;
      case 'PUT':
      case 'PATCH':
        operation = 'UPDATE';
        break;
      case 'DELETE':
        operation = 'DELETE';
        break;
      case 'GET':
      default:
        operation = 'SELECT';
        break;
    }
  }

  // ONLY update API details if NOT preserving existing values
  if (!preserveExistingApiDetails) {
    setApiDetails(prev => ({
      ...prev,
      apiName: object.name || object.objectName ? `${object.name || object.objectName} API` : 'New API',
      apiCode: objectType ? `${objectType.slice(0, 3)}_${object.name || object.objectName || 'API'}` : 'API',
      description: object.comment || (object.name || object.objectName ? `API for ${object.name || object.objectName}` : ''),
      endpointPath: endpointPath,
      owner: object.owner || 'CBXDMX',
      httpMethod: httpMethod
    }));
  } else {
    // Only update the fields that should be updated from the object
    setApiDetails(prev => ({
      ...prev,
      endpointPath: prev.endpointPath || endpointPath,
      owner: prev.owner || object.owner || 'CBXDMX',
      httpMethod: prev.httpMethod || httpMethod
    }));
  }

  // Set schema config with proper operation (not derived from HTTP method for SOAP/GraphQL)
  setSchemaConfig(prev => ({
    ...prev,
    schemaName: object.owner || 'CBXDMX',
    objectType: objectType || 'TABLE',
    objectName: object.name || object.objectName || '',
    operation: operation,
    primaryKeyColumn: ''
  }));


    // ============ AUTO-POPULATE SOAP SERVICE NAME ============
// Generate service name from database object OR custom query when in SOAP mode
if (protocolType === 'soap') {
  let generatedServiceName = '';
  
  // Case 1: Database object exists
  if (object.name && object.type !== 'CUSTOM_QUERY') {
    generatedServiceName = object.name.charAt(0).toUpperCase() + 
                           object.name.slice(1).toLowerCase();
    generatedServiceName = generatedServiceName.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
    generatedServiceName = generatedServiceName.replace(/[^a-zA-Z0-9]/g, '');
    if (!generatedServiceName.toLowerCase().endsWith('service')) {
      generatedServiceName = generatedServiceName + 'Service';
    }
  }
  // Case 2: Custom query mode
  else if (sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery) {
    if (customQuery && customQuery.trim()) {
      const fromMatch = customQuery.match(/FROM\s+([^\s,;]+)/i);
      if (fromMatch && fromMatch[1]) {
        let tableName = fromMatch[1];
        if (tableName.includes('.')) {
          tableName = tableName.split('.').pop();
        }
        generatedServiceName = tableName.charAt(0).toUpperCase() + tableName.slice(1).toLowerCase();
        generatedServiceName = generatedServiceName.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
        generatedServiceName = generatedServiceName.replace(/[^a-zA-Z0-9]/g, '');
      } else {
        generatedServiceName = 'CustomQuery';
      }
    } else {
      generatedServiceName = 'CustomQuery';
    }
    if (!generatedServiceName.toLowerCase().endsWith('service')) {
      generatedServiceName = generatedServiceName + 'Service';
    }
  }
  
  // Only set if service name is empty or if we're not preserving existing details
  if (generatedServiceName) {
    setSoapConfig(prev => ({ 
      ...prev, 
      serviceName: (!preserveExistingApiDetails || !prev.serviceName) ? generatedServiceName : prev.serviceName 
    }));
    console.log('🔧 Auto-populated SOAP service name:', generatedServiceName);
  }
}

    // Generate parameters and response mappings - ALWAYS regenerate from the object
    // This ensures parameters and mappings are updated when changing the database object
    const newParameters = [];
    const newMappings = [];

    // Handle parameters - look in both object.parameters and object.details.parameters
    let parameters = [];
    if (object.parameters && Array.isArray(object.parameters)) {
      parameters = object.parameters;
      console.log('📦 Found parameters in object.parameters:', parameters);
    } else if (object.details?.parameters && Array.isArray(object.details.parameters)) {
      parameters = object.details.parameters;
      console.log('📦 Found parameters in object.details.parameters:', parameters);
    }

    // Handle columns - look in object.columns and object.details.columns
    let columns = [];
    if (object.columns && Array.isArray(object.columns)) {
      columns = object.columns;
      console.log('📦 Found columns in object.columns:', columns);
    } else if (object.details?.columns && Array.isArray(object.details.columns)) {
      columns = object.details.columns;
      console.log('📦 Found columns in object.details.columns:', columns);
    }

    // Process parameters if we have any
    if (parameters.length > 0) {
  console.log('📦 Processing parameters (count: ' + parameters.length + ')');
  
  parameters.forEach((param, index) => {
    // Extract parameter data with fallbacks
    const paramName = param.key || 
                      param.ARGUMENT_NAME || 
                      param.argument_name || 
                      param.name || 
                      param.NAME || 
                      `param_${index + 1}`;
    
    const paramDataType = param.oracleType || 
                          param.DATA_TYPE || 
                          param.data_type || 
                          param.type || 
                          param.TYPE || 
                          param.udt_name ||  // PostgreSQL specific
                          'VARCHAR2';
    
    const paramMode = param.paramMode || 
                      param.IN_OUT || 
                      param.in_out || 
                      param.mode || 
                      param.MODE || 
                      'IN';
    
    // Normalize the mode
    let normalizedMode = paramMode?.toString().toUpperCase().replace(/\s+/g, '_') || 'IN';
    if (normalizedMode === 'INOUT' || normalizedMode === 'IN_OUT') {
      normalizedMode = 'IN/OUT';
    }
    
    // ============ IMPROVED TYPE MAPPING ============
    const isOutParam = normalizedMode === 'OUT' || normalizedMode === 'IN/OUT' || normalizedMode === 'IN_OUT';
    const dbType = object.databaseType?.toLowerCase() || 'oracle';
    const typeLower = paramDataType.toLowerCase();
    const typeUpper = paramDataType.toUpperCase();
    
    let oracleType = 'VARCHAR2';
    let apiType = 'string';
    let format = '';
    
    // PostgreSQL Type Mapping
    if (dbType === 'postgresql') {
      // Numeric types
      if (typeLower.match(/^int|integer|bigint|smallint|serial|bigserial|smallserial$/)) {
        oracleType = 'NUMBER';
        apiType = 'integer';
      }
      else if (typeLower.match(/^decimal|numeric|real|double|float|money$/)) {
        oracleType = 'NUMBER';
        apiType = 'number';
      }
      // Character types
      else if (typeLower.match(/^varchar|char|text|citext|bpchar|character/)) {
        oracleType = 'VARCHAR2';
        apiType = 'string';
      }
      // Date/Time types
      else if (typeLower.match(/^date|time|timetz|timestamp|timestamptz|interval$/)) {
        oracleType = 'TIMESTAMP';
        apiType = 'string';
        format = 'date-time';
      }
      // Boolean
      else if (typeLower === 'boolean' || typeLower === 'bool') {
        oracleType = 'BOOLEAN';
        apiType = 'boolean';
      }
      // ============ FIX: JSON types - use 'array' for API type ============
      else if (typeLower === 'json' || typeLower === 'jsonb') {
        oracleType = 'JSONB';
        
        // ============ COMPREHENSIVE JSONB TYPE DETECTION ============
        const paramNameLower = paramName.toLowerCase();
        
        // Patterns that suggest ARRAY type
        const arrayPatterns = [
          /_list$/, /_array$/, /_items$/, /_collection$/, /_set$/,
          /_questions$/, /_answers$/, /_responses$/, /_entries$/,
          /_records$/, /_details$/, /_lines$/, /_rows$/,
          /batch/, /multiple/, /all_/, /many_/, /group_/, /collection/, /array/,
        ];
        
        // Patterns that suggest OBJECT type
        const objectPatterns = [
          /_config$/, /_settings$/, /_profile$/, /_metadata$/, /_info$/,
          /_detail$/, /_data$/, /_payload$/, /_body$/, /_attributes$/,
          /_properties$/, /_params$/, /_context$/, /_state$/, /_result$/, /_response$/,
        ];
        
        // Check for explicit array patterns first
        let isArray = false;
        for (const pattern of arrayPatterns) {
          if (pattern.test(paramNameLower)) {
            isArray = true;
            console.log(`📊 JSONB parameter "${paramName}" matched ARRAY pattern: ${pattern}`);
            break;
          }
        }
        
        // If not array, check for object patterns
        let isObject = false;
        if (!isArray) {
          for (const pattern of objectPatterns) {
            if (pattern.test(paramNameLower)) {
              isObject = true;
              console.log(`📊 JSONB parameter "${paramName}" matched OBJECT pattern: ${pattern}`);
              break;
            }
          }
        }
        
        // ============ CRITICAL FIX: Update the OUTER apiType variable ============
        // DO NOT declare a new variable - update the existing one
        if (isArray) {
          apiType = 'array';
        } else if (isObject) {
          apiType = 'object';
        } else {
          // Fallback: check if plural
          const isPlural = paramNameLower.endsWith('s') && 
                          !paramNameLower.endsWith('ss') &&
                          !paramNameLower.endsWith('us') &&
                          !paramNameLower.match(/(ics|is)$/);
          apiType = isPlural ? 'array' : 'object';
          console.log(`📊 JSONB parameter "${paramName}" fell back to ${apiType} (plural: ${isPlural})`);
        }
        
        console.log(`📊 JSONB parameter ${paramName} detected as ${apiType}`);
      }
      // Array types - CHECK FOR [] SUFFIX OR ARRAY PREFIX
      else if (typeLower.endsWith('[]') || typeLower.startsWith('_') || typeLower.includes('array')) {
        oracleType = 'JSONB';
        apiType = 'array';
        const baseType = typeLower.replace(/\[\]$/, '').replace(/^_/, '');
        console.log(`📦 Array parameter detected: ${paramName} with base type: ${baseType}`);
      }
      // Binary types
      else if (typeLower.match(/^bytea|blob|binary|oid$/)) {
        oracleType = 'BLOB';
        apiType = 'file';
      }
      // UUID
      else if (typeLower === 'uuid') {
        oracleType = 'VARCHAR2';
        apiType = 'string';
      }
      // Geometric types
      else if (typeLower.match(/^point|line|lseg|box|path|polygon|circle$/)) {
        oracleType = 'JSONB';
        apiType = 'array';
      }
      // Network types
      else if (typeLower.match(/^cidr|inet|macaddr$/)) {
        oracleType = 'VARCHAR2';
        apiType = 'string';
      }
      // Range types
      else if (typeLower.match(/^int4range|int8range|numrange|tsrange|tstzrange|daterange$/)) {
        oracleType = 'JSONB';
        apiType = 'array';
      }
      // XML
      else if (typeLower === 'xml') {
        oracleType = 'CLOB';
        apiType = 'string';
      }
      // Default
      else {
        oracleType = 'VARCHAR2';
        apiType = 'string';
      }
    } 
    // Oracle Type Mapping
    else {
      // Numeric types
      if (typeUpper.match(/^NUMBER|INTEGER|INT|SMALLINT|FLOAT|DECIMAL|NUMERIC|BINARY_FLOAT|BINARY_DOUBLE$/)) {
        oracleType = 'NUMBER';
        apiType = isOutParam ? 'number' : 'integer';
      }
      // Character types
      else if (typeUpper.match(/^VARCHAR2|VARCHAR|CHAR|NCHAR|NVARCHAR2|CLOB|NCLOB|LONG$/)) {
        oracleType = typeUpper.match(/^CLOB|NCLOB|LONG/) ? 'CLOB' : 'VARCHAR2';
        apiType = 'string';
      }
      // Date/Time types
      else if (typeUpper.match(/^DATE|TIMESTAMP|TIMESTAMP WITH TIME ZONE|TIMESTAMP WITH LOCAL TIME ZONE|INTERVAL/)) {
        oracleType = 'TIMESTAMP';
        apiType = 'string';
        format = 'date-time';
      }
      // Boolean
      else if (typeUpper === 'BOOLEAN') {
        oracleType = 'BOOLEAN';
        apiType = 'boolean';
      }
      // ============ FIX: JSON types - use 'array' for API type ============
      else if (typeUpper.match(/^JSON|JSONB$/)) {
        oracleType = 'JSONB';
        apiType = 'array';  // Changed from 'object' to 'array'
      }
      // Binary types
      else if (typeUpper.match(/^BLOB|RAW|LONG RAW|BFILE$/)) {
        oracleType = 'BLOB';
        apiType = 'file';
      }
      // XML Type
      else if (typeUpper === 'XMLTYPE') {
        oracleType = 'CLOB';
        apiType = 'string';
      }
      // Cursor type (for OUT parameters)
      else if (typeUpper === 'SYS_REFCURSOR') {
        oracleType = 'SYS_REFCURSOR';
        apiType = 'array';
      }
      // Rowid types
      else if (typeUpper.match(/^ROWID|UROWID$/)) {
        oracleType = 'VARCHAR2';
        apiType = 'string';
      }
      // Auto-generate (special case)
      else if (typeUpper === 'AUTOGENERATE') {
        oracleType = 'AUTOGENERATE';
        apiType = 'string';
      }
      // Default
      else {
        oracleType = 'VARCHAR2';
        apiType = 'string';
      }
    }
    
    // Special handling for array types - ensure they get proper API type
    if (typeLower.includes('array') || typeLower.endsWith('[]') || typeLower.startsWith('_')) {
      oracleType = 'JSONB';
      apiType = 'array';
      console.log(`📊 Parameter ${paramName} detected as ARRAY type!`);
    }
    
    console.log(`🔍 Processing param ${index}:`, { 
      paramName, 
      originalType: paramDataType,
      mappedOracleType: oracleType,
      mappedApiType: apiType,
      format: format,
      normalizedMode
    });
    
    // Generate a clean key name
    let cleanKey = paramName;
    if (typeof paramName === 'string') {
      if (param.key) {
        cleanKey = param.key;
      } else if (param.dbColumn) {
        cleanKey = param.dbColumn;
      } else {
        cleanKey = paramName.replace(/^p_/i, '').toLowerCase();
      }
    } else {
      cleanKey = `param_${index + 1}`;
    }
    
    // Determine parameter location
    let parameterLocation = param.parameterLocation;
    
    if (!parameterLocation) {
      const isInParam = normalizedMode === 'IN';
      
      if (param.inBody === true || param.inBody === 'true' || param.paramType === 'body') {
        parameterLocation = 'body';
      }
      else if (param.isPathParam === true || param._isPathParam === true) {
        parameterLocation = 'path';
      }
      else if (isInParam && (apiDetails.httpMethod === 'POST' || apiDetails.httpMethod === 'PUT' || apiDetails.httpMethod === 'PATCH')) {
        parameterLocation = 'body';
      }
      else if (isInParam && apiDetails.httpMethod === 'GET') {
        parameterLocation = 'query';
      }
      else {
        parameterLocation = 'query';
      }
    }
    
    const isInParam = normalizedMode === 'IN';
    const isPathParam = parameterLocation === 'path';
    
    // Generate example based on API type
    let example = param.example || '';
    if (!example || example === '') {
      switch(apiType) {
        case 'integer':
          example = '123';
          break;
        case 'number':
          example = '123.45';
          break;
        case 'boolean':
          example = 'true';
          break;
        case 'array':
          example = '[]';
          break;
        case 'object':
          example = '{}';
          break;
        case 'file':
          example = 'Select a file...';
          break;
        default:
          example = 'sample';
      }
    }
    
    // Only add to parameters array for IN and IN/OUT parameters
    if (isInParam || normalizedMode === 'IN/OUT') {
      newParameters.push({
        id: param.id || `proc-param-${Date.now()}-${index}`,
        key: cleanKey,
        dbColumn: param.dbColumn || paramName,
        oracleType: oracleType,
        apiType: apiType,
        format: format,
        parameterLocation: parameterLocation,
        required: isPathParam ? true : (param.required !== undefined ? param.required : true),
        description: param.description || `${paramName} (${normalizedMode})`,
        example: example,
        validationPattern: param.validationPattern || '',
        defaultValue: param.defaultValue || param.DATA_DEFAULT || '',
        inBody: parameterLocation === 'body',
        isPrimaryKey: param.isPrimaryKey || false,
        paramMode: normalizedMode,
        _isPathParam: isPathParam,
        _originalDataType: paramDataType
      });
      console.log(`✅ Added to PARAMETERS tab: ${cleanKey} (${normalizedMode}) with type: ${oracleType}/${apiType}`);
    }
    
    // Only add to response mappings for OUT and IN/OUT parameters
    if (isOutParam || normalizedMode === 'IN/OUT') {
      newMappings.push({
        id: param.id || `mapping-out-${Date.now()}-${index}`,
        apiField: cleanKey,
        dbColumn: param.dbColumn || paramName,
        oracleType: oracleType,
        apiType: apiType,
        format: format,
        nullable: param.nullable !== undefined ? param.nullable : true,
        isPrimaryKey: param.isPrimaryKey || false,
        includeInResponse: param.includeInResponse !== undefined ? param.includeInResponse : true,
        inResponse: param.inResponse !== undefined ? param.inResponse : true,
        paramMode: normalizedMode,
        example: example,
        _originalDataType: paramDataType
      });
    }
  });
}

    // Process columns for tables/views
    if (columns.length > 0 && parameters.length === 0) {
      console.log('📦 Processing columns (count: ' + columns.length + ')');
      
      columns.forEach((col, index) => {
        const colName = col.name || col.COLUMN_NAME || col.column_name;
        const colType = col.type || col.DATA_TYPE || col.data_type || 'VARCHAR2';
        const colNullable = col.nullable || col.NULLABLE || 'Y';
        const isPrimaryKey = col.key === 'PK' || col.CONSTRAINT_TYPE === 'P' || col.isPrimaryKey;
        
        if (colName) {
          const cleanKey = typeof colName === 'string' ? colName.toLowerCase() : `column_${index + 1}`;
          const dbType = object.databaseType?.toLowerCase() || 'oracle';
          
          let oracleType = 'VARCHAR2';
          
          if (dbType === 'postgresql') {
            if (colType.includes('INT') || colType.includes('BIGINT') || colType.includes('SMALLINT') || 
                colType.includes('DECIMAL') || colType.includes('NUMERIC') || colType.includes('FLOAT') ||
                colType.includes('DOUBLE') || colType.includes('REAL')) {
              oracleType = 'NUMBER';
            } else if (colType.includes('DATE') || colType.includes('TIME') || colType.includes('TIMESTAMP')) {
              oracleType = 'DATE';
            } else if (colType.includes('BOOLEAN')) {
              oracleType = 'VARCHAR2';
            } else if (colType.includes('VARCHAR') || colType.includes('CHAR') || colType.includes('TEXT')) {
              oracleType = 'VARCHAR2';
            } else {
              oracleType = 'VARCHAR2';
            }
          } else {
            if (colType.includes('NUMBER') || colType.includes('INT') || colType.includes('FLOAT')) {
              oracleType = 'NUMBER';
            } else if (colType.includes('DATE')) {
              oracleType = 'DATE';
            } else if (colType.includes('TIMESTAMP')) {
              oracleType = 'TIMESTAMP';
            } else if (colType.includes('VARCHAR') || colType.includes('CHAR')) {
              oracleType = 'VARCHAR2';
            } else if (colType.includes('CLOB')) {
              oracleType = 'CLOB';
            } else {
              oracleType = 'VARCHAR2';
            }
          }

          let apiType = 'string';
          if (oracleType === 'NUMBER') {
            apiType = 'integer';
          }

          let parameterLocation = 'query';
          if (isPrimaryKey && (httpMethod === 'GET' || httpMethod === 'PUT' || httpMethod === 'DELETE')) {
            parameterLocation = 'path';
          } else if (httpMethod === 'POST' || httpMethod === 'PUT' || httpMethod === 'PATCH') {
            parameterLocation = 'body';
          }

          const isPathParam = parameterLocation === 'path';

          newParameters.push({
            id: `param-col-${Date.now()}-${index}`,  // ✅ Use 'col' reference
            key: cleanKey,
            dbColumn: colName,  // ✅ Use the column name
            oracleType: oracleType,
            apiType: apiType,
            parameterLocation: parameterLocation,
            required: isPathParam ? true : (isPrimaryKey || colNullable === 'N'),
            description: col.comment || col.COMMENTS || `From ${object.name || object.objectName}.${colName}`,
            example: colName.includes('ID') ? '1' : 
                    colName.includes('DATE') ? '2024-01-01' :
                    colName.includes('NAME') ? 'Sample' : '',
            validationPattern: '',
            defaultValue: col.DATA_DEFAULT || col.defaultValue || '',
            inBody: parameterLocation === 'body',
            isPrimaryKey: isPrimaryKey,
            paramMode: null,
            _isPathParam: isPathParam,
            _isFromDatabaseObject: true  // ✅ Add this flag
          });

          // When creating mappings:
          newMappings.push({
            id: `mapping-col-${Date.now()}-${index}`,  // ✅ Use col reference
            apiField: cleanKey,
            dbColumn: colName,  // ✅ Use column name
            oracleType: oracleType,
            apiType: apiType,
            format: oracleType === 'DATE' ? 'date-time' : '',
            nullable: colNullable === 'Y',
            isPrimaryKey: isPrimaryKey,
            includeInResponse: true,
            inResponse: true,
            paramMode: null,
            _fromDatabaseObject: true
          });
        }
      });
    }

    console.log('📊 Final results:', {
      parametersCount: newParameters.length,
      mappingsCount: newMappings.length,
      inCount: newParameters.filter(p => p.paramMode === 'IN' || p.paramMode === null).length,
      outCount: newMappings.length
    });

    // ALWAYS update parameters and mappings regardless of preserveExistingApiDetails
    // This ensures when you change the database object, the parameters and mappings update
    setParameters(newParameters);
    setResponseMappings(newMappings);
    
    // Generate sample response based on mappings
    if (newMappings.length > 0) {
      const sampleData = {};
      newMappings.slice(0, 50).forEach(mapping => {
        if (mapping.apiType === 'integer') {
          sampleData[mapping.apiField] = 123;
        } else if (mapping.apiType === 'string') {
          if (mapping.format === 'date-time') {
            sampleData[mapping.apiField] = '2024-01-01T00:00:00Z';
          } else {
            sampleData[mapping.apiField] = mapping.apiField.includes('id') ? 1 : 'sample';
          }
        } else if (mapping.apiType === 'boolean') {
          sampleData[mapping.apiField] = true;
        }
      });
      
      const successSchema = JSON.stringify({
        success: true,
        data: sampleData,
        message: 'Request processed successfully',
        metadata: {
          timestamp: '{{timestamp}}',
          apiVersion: apiDetails.version,
          requestId: '{{requestId}}'
        }
      }, null, 2);
      
      setResponseBody(prev => ({
        ...prev,
        successSchema
      }));
    }

    // Set body type based on HTTP method
    if (httpMethod === 'POST' || httpMethod === 'PUT' || httpMethod === 'PATCH') {
      setRequestBody(prev => ({
        ...prev,
        bodyType: 'json'
      }));
    } else {
      setRequestBody(prev => ({
        ...prev,
        bodyType: 'none'
      }));
    }

    console.log(`✅ Form populated for ${objectType} with operation: ${operation} (HTTP ${httpMethod})`);
    console.log(`📊 Parameters (IN/IN OUT): ${newParameters.length}, Mappings (OUT/IN OUT): ${newMappings.length}`);
    console.log(`📊 Database type: ${object.databaseType}`);
  }, [apiDetails.version, apiDetails.httpMethod, setApiDetails, setSchemaConfig, setParameters, setResponseMappings, setRequestBody, setResponseBody]);


  useEffect(() => {
    const timer = setTimeout(() => {
      if (responseMappings.length > 0) {
        console.log(`🔄 Forcing ${protocolType} response schema regeneration after form load...`);
        
        const outMappings = responseMappings.filter(m => 
          (m.paramMode === 'OUT' || m.paramMode === 'IN/OUT' || m.paramMode === 'IN_OUT' || !m.paramMode)
        );
        
        if (protocolType === 'soap') {
          const operationName = soapConfig.soapAction || apiDetails.apiCode || 'Operation';
          const soapVersion = soapConfig.version === '1.2' ? 'http://www.w3.org/2003/05/soap-envelope' : 'http://schemas.xmlsoap.org/soap/envelope/';
          
          let bodyContent = outMappings.length > 0 ? `<${operationName}Response xmlns="${soapConfig.namespace}">
        <success>true</success>
        <message>Request processed successfully</message>
        ${outMappings.map(m => {
          const apiType = m.apiType?.toUpperCase();
          let sampleValue = 'value';
          if (apiType === 'INTEGER') sampleValue = '123';
          else if (apiType === 'NUMBER') sampleValue = '123.45';
          else if (apiType === 'BOOLEAN') sampleValue = 'true';
          return `<${m.apiField}>${sampleValue}</${m.apiField}>`;
        }).join('\n      ')}
      </${operationName}Response>` : `<${operationName}Response xmlns="${soapConfig.namespace}">
        <success>true</success>
        <message>Request processed successfully</message>
      </${operationName}Response>`;
          
          const soapSuccess = `<?xml version="1.0" encoding="UTF-8"?>
  <soap:Envelope xmlns:soap="${soapVersion}">
    <soap:Header/>
    <soap:Body>
      ${bodyContent}
    </soap:Body>
  </soap:Envelope>`;
          
          setResponseBody(prev => ({ ...prev, successSchema: soapSuccess }));
          
        } else if (protocolType === 'graphql') {
          const operationName = graphqlConfig.operationName || 'operation';
          const responseData = {};
          outMappings.forEach(m => {
            const type = m.apiType?.toUpperCase();
            responseData[m.apiField] = type === 'INTEGER' ? 123 : type === 'BOOLEAN' ? true : 'sample';
          });
          setResponseBody(prev => ({ ...prev, successSchema: JSON.stringify({ data: { [operationName]: responseData } }, null, 2) }));
          
        } else {
          const sampleData = {};
          outMappings.forEach(m => {
            const type = m.apiType?.toUpperCase();
            if (type === 'INTEGER') sampleData[m.apiField] = 123;
            else if (type === 'BOOLEAN') sampleData[m.apiField] = true;
            else if (type === 'ARRAY') sampleData[m.apiField] = [];
            else sampleData[m.apiField] = 'sample';
          });
          setResponseBody(prev => ({ ...prev, successSchema: JSON.stringify({ success: true, data: sampleData, message: 'Success' }, null, 2) }));
        }
        
        setResponseSchemaVersion(prev => prev + 1);
      }
    }, 500);
    
    return () => clearTimeout(timer);
  }, [responseMappings.length, protocolType, soapConfig, graphqlConfig, activeTab]);


  // ==================== VALIDATION FUNCTIONS ====================

  // Helper function to check if a field is required
  const isFieldRequired = (fieldName) => {
    if (REQUIRED_FIELDS.apiDetails.includes(fieldName)) {
      return true;
    }
    if (authConfig.authType === 'apiKey' && REQUIRED_FIELDS.authConfig.apiKey.includes(fieldName)) {
      return true;
    }
    if (authConfig.authType === 'bearer' && REQUIRED_FIELDS.authConfig.bearer.includes(fieldName)) {
      return true;
    }
    if (authConfig.authType === 'basic' && REQUIRED_FIELDS.authConfig.basic.includes(fieldName)) {
      return true;
    }
    return false;
  };

  // Helper function to filter parameters to only show IN parameters
  const getInParameters = useCallback(() => {
    const filtered = parameters.filter(p => 
      !p.paramMode || p.paramMode === 'IN'
    );
    console.log('📊 getInParameters - Total parameters:', parameters.length, 'Filtered IN:', filtered.length, filtered.map(p => ({ key: p.key, paramMode: p.paramMode })));
    return filtered;
  }, [parameters]);

  // Helper function to filter response mappings to only show OUT parameters and other mappings
  const getOutMappings = useCallback(() => {
    // Don't filter - include ALL response mappings for the Response tab
    // The paramMode filtering should only apply to procedure/function parameters
    // For custom queries and tables, all response fields should be shown
    const filtered = responseMappings.filter(m => {
      // If paramMode is explicitly set to 'OUT' or 'IN/OUT', include it
      if (m.paramMode === 'OUT' || m.paramMode === 'IN/OUT' || m.paramMode === 'IN_OUT') {
        return true;
      }
      // If paramMode is not set or is null, also include it (for custom queries and tables)
      if (!m.paramMode || m.paramMode === 'null' || m.paramMode === 'undefined') {
        return true;
      }
      // Default to including (don't exclude anything)
      return true;
    });
    console.log('📊 getOutMappings - Total mappings:', responseMappings.length, 'Filtered OUT:', filtered.length);
    return filtered;
  }, [responseMappings]);

  // Validate source object with the API Generation Engine - FIXED to resolve synonyms
  const validateObject = async (object, type) => {
    // Skip validation for API objects (when editing)
    if (isEditing || type === 'API' || type === 'CONNECTION' || !type || type === 'API' || object?.type === 'API') {
      console.log('ℹ️ Skipping validation for API object');
      setValidationResult({ valid: true, message: 'API object - validation skipped' });
      return { valid: true, data: { details: {} } };
    }

    if (!authToken || !object || !object.name || !type) {
      console.log('❌ Cannot validate: missing required data');
      return null;
    }

    setValidating(true);
    setValidationResult(null);

    try {
      // Check if this is a synonym and resolve it first
      let targetOwner = object.owner;
      let targetName = object.name;
      let targetType = type;
      
      // If the object is a synonym, resolve it to get the target object
      if (object.isSynonym || object.targetType) {
        console.log('🔍 Object is a synonym, resolving to target:', {
          targetOwner: object.targetOwner,
          targetName: object.targetName,
          targetType: object.targetType
        });
        
        targetOwner = object.targetOwner;
        targetName = object.targetName;
        targetType = object.targetType;
        
        // Update sourceObjectInfo to show it's a synonym
        setSourceObjectInfo({
          isSynonym: true,
          targetType: object.targetType,
          targetName: object.targetName,
          targetOwner: object.targetOwner
        });
      }

      console.log('🔍 Validating source object:', { 
        objectName: targetName, 
        objectType: targetType, 
        owner: targetOwner 
      });
      
      // Validate the target object (not the synonym)
      const response = await validateSourceObject(authToken, {
        objectName: targetName,
        objectType: targetType,
        owner: targetOwner
      });

      console.log('📦 Validation response:', response);
      
      // Store the validation result
      setValidationResult(response.data);
      
      // Return the full response data for use in initialization
      return response;
    } catch (error) {
      console.error('❌ Error validating source object:', error);
      setValidationResult({ valid: false, message: error.message });
      return null;
    } finally {
      setValidating(false);
    }
  };

 // Check if API code is available - FIXED: Only check for new APIs, not when editing
const checkCodeAvailability = async (code) => {
  // Skip check when editing - we're editing an existing API, so the code should be allowed
  if (isEditing) {
    console.log('ℹ️ Skipping code availability check for editing mode');
    setApiCodeExists(false);
    return true;
  }

  if (!authToken) {
    console.log('⚠️ Cannot check code availability: authToken is missing');
    setApiCodeExists(false);
    return true;
  }
  
  if (!code || code.length < 3) {
    setApiCodeExists(false);
    // Clear validation error when code is too short
    setValidationErrors(prev => ({ ...prev, apiCode: null }));
    return true;
  }

  try {
    console.log(`🔍 Checking availability for API code: ${code}`);
    const response = await checkApiCodeAvailability(authToken, code);
    
    console.log('📦 Code availability response:', response);
    
    let available = true;
    
    // Handle different response formats
    if (response) {
      if (typeof response.data === 'boolean') {
        available = response.data;
      } else if (response.data && response.data.available !== undefined) {
        available = response.data.available;
      } else if (typeof response === 'boolean') {
        available = response;
      } else if (response.available !== undefined) {
        available = response.available;
      }
    }
    
    // Set the apiCodeExists state (opposite of available)
    const exists = !available;
    setApiCodeExists(exists);
    
    // Update validation error
    if (exists) {
      setValidationErrors(prev => ({ 
        ...prev, 
        apiCode: `API code "${code}" already exists. Please choose a different code.` 
      }));
    } else {
      // CRITICAL FIX: Clear validation error when code becomes available
      setValidationErrors(prev => ({ ...prev, apiCode: null }));
    }
    
    return available;
  } catch (error) {
    console.error('Error checking code availability:', error);
    setApiCodeExists(false);
    // On error, don't block the user, just log
    setValidationErrors(prev => ({ ...prev, apiCode: null }));
    return true;
  }
};


  // Auto-switch to Definition tab when protocol type changes
  useEffect(() => {
    // When protocol type changes, switch to the Definition tab
    if (protocolType) {
      setActiveTab('definition');
      console.log('🔄 Protocol changed to:', protocolType, '- Switching to Definition tab');
    }
  }, [protocolType]);

  // Sync operation when HTTP method changes - MODIFIED to skip SOAP and GraphQL
useEffect(() => {
  // Skip for SOAP and GraphQL protocols - operation should not be tied to HTTP method
  if (protocolType === 'soap' || protocolType === 'graphql') {
    return;
  }
  
  // Skip for procedures/functions/packages
  const objectType = selectedDbObject?.type?.toUpperCase() || selectedObject?.type?.toUpperCase();
  
  if (objectType === 'PROCEDURE' || objectType === 'FUNCTION' || objectType === 'PACKAGE') {
    // For procedures/functions, operation should always be EXECUTE
    setSchemaConfig(prev => ({
      ...prev,
      operation: 'EXECUTE'
    }));
  } else {
    // For tables/views, map HTTP method to operation
    let operation = 'SELECT';
    switch(apiDetails.httpMethod) {
      case 'POST':
        operation = 'INSERT';
        break;
      case 'PUT':
      case 'PATCH':
        operation = 'UPDATE';
        break;
      case 'DELETE':
        operation = 'DELETE';
        break;
      case 'GET':
      default:
        operation = 'SELECT';
        break;
    }
    
    setSchemaConfig(prev => ({
      ...prev,
      operation: operation
    }));
  }
}, [apiDetails.httpMethod, selectedDbObject, selectedObject, protocolType]); // Added protocolType to dependencies

  // Add this effect to protect against invalid body type configurations
  useEffect(() => {
    // Skip for SOAP and GraphQL protocols - they have their own body type requirements
    if (protocolType === 'soap' || protocolType === 'graphql') {
      return;
    }
    
    const method = apiDetails.httpMethod;
    const currentBodyType = requestBody.bodyType;
    
    // Methods that should NEVER have a body
    const methodsWithoutBody = ['GET', 'DELETE', 'HEAD', 'OPTIONS'];
    
    if (methodsWithoutBody.includes(method) && currentBodyType !== 'none') {
      // Force body type to 'none' for these methods
      console.log(`🔄 HTTP method ${method} does not support request body, setting bodyType to 'none'`);
      setRequestBody(prev => ({
        ...prev,
        bodyType: 'none'
      }));
    }
    
    // For methods that SHOULD have a body (POST, PUT, PATCH), ensure body type is not 'none'
    const methodsWithBody = ['POST', 'PUT', 'PATCH'];
    if (methodsWithBody.includes(method) && currentBodyType === 'none') {
      // Set body type to 'json' for methods that should have a body
      console.log(`🔄 HTTP method ${method} supports request body, setting bodyType to 'json'`);
      setRequestBody(prev => ({
        ...prev,
        bodyType: 'json'
      }));
    }
  }, [protocolType, apiDetails.httpMethod, requestBody.bodyType]);

  // Add cleanup in useEffect
  useEffect(() => {
    return () => {
      if (codeCheckTimeout) {
        clearTimeout(codeCheckTimeout);
      }
    };
  }, [codeCheckTimeout]);

  // ==================== INITIALIZATION EFFECTS ====================

  // Handle loading state when opening from dashboard with incomplete data
  useEffect(() => {
    if (isOpen && fromDashboard && selectedObject?.loading) {
      // Don't do anything yet - just show the loading state in the modal
      console.log('⏳ Dashboard object loading in progress...');
    }
  }, [isOpen, fromDashboard, selectedObject]);

  // Show object selector when modal opens from dashboard AND no object is selected
  // Add this debug log
  useEffect(() => {
    console.log('🔍 Object selector check:', {
      isOpen,
      fromDashboard,
      selectedObject: selectedObject,
      selectedObjectIsNull: !selectedObject,
      isEditing,
      isEditingValue: isEditing,
      notEditing: !isEditing,
      selectedDbObject,
      selectedDbObjectIsNull: !selectedDbObject,
      shouldShow: isOpen && fromDashboard && !selectedObject && !isEditing && !selectedDbObject
    });
    
    if (isOpen && fromDashboard && !selectedObject && !isEditing && !selectedDbObject) {
      console.log('🎯 Showing object selector modal');
      setShowObjectSelector(true);
    }
  }, [isOpen, fromDashboard, selectedObject, isEditing, selectedDbObject]);

  // Add a ref to track if we're currently editing the same API
  const currentEditingApiIdRef = useRef(null);
  const isInitializedRef = useRef(false);

  // Modify your initialization useEffect
  useEffect(() => {
    const initializeFromObject = async () => {
      // If we're on dashboard and have selectedDbObject AND we're NOT editing an existing API
      if (fromDashboard && selectedDbObject && !isEditing) {
        console.log('📝 Using dashboard-selected object for NEW API:', selectedDbObject);
        await populateFormFromObject(selectedDbObject, false, false);  // forceRefresh = false
        return;
      }

      if (!selectedObject) {
        console.log('ℹ️ AutoAPIGeneratorModal - No selected object provided, showing empty form');
        return;
      }

      setLoading(true);
      
      // Check if we're in edit mode - IMPROVED DETECTION
      const isEditMode = isEditing || (selectedObject?.data && selectedObject.data.id);
      
      // Get the API ID we're editing
      const editingApiId = isEditMode ? (selectedObject?.data?.id || selectedObject?.id) : null;
      
      // If we're reopening the same API we were just editing, preserve form state
      if (isEditMode && currentEditingApiIdRef.current === editingApiId && isInitializedRef.current) {
        console.log('🔄 Reopening same API - preserving form state');
        setLoading(false);
        return;
      }
      
      // Store the current API ID
      if (isEditMode && editingApiId) {
        currentEditingApiIdRef.current = editingApiId;
      }
      
      console.log('🔍 AutoAPIGeneratorModal - Initializing with selected object:', {
        selectedObject: selectedObject,
        isEditing: isEditing,
        isEditMode: isEditMode,
        editingApiId: editingApiId,
        previousId: currentEditingApiIdRef.current
      });

      try {
        // FOR EDIT MODE: Load from API data only
        if (isEditMode) {
          console.log('ℹ️ EDIT MODE: Loading API object for edit');
          
          // Extract the API data - handle both wrapped and unwrapped formats
          let apiData = selectedObject;
          
          // If it's wrapped in a 'data' property (from API response), extract it
          if (selectedObject.data && selectedObject.data.id) {
            apiData = selectedObject.data;
          }
          
          console.log('📦 Extracted API data for edit:', apiData);
          
          // ONLY populate from API data - DO NOT call populateFormFromObject
          await populateFormFromApiData(apiData);
          
          // Mark as initialized
          isInitializedRef.current = true;
          
          setLoading(false);
          return; // IMPORTANT: Exit here, don't continue to database object logic
        }

        // FOR NEW API MODE (not editing) - Validate and populate from database object
        console.log('🆕 NEW API MODE: Starting validation for object:', selectedObject.name);
        
        // Validate the source object (now resolves synonyms)
        const validationResponse = await validateObject(selectedObject, selectedObject.type);
        console.log('📦 Validation response received:', validationResponse);
        
        if (validationResponse && validationResponse.data) {
          console.log('📦 Using validation data to populate form');
          
          const validationData = validationResponse.data;
          
          // Extract parameters from the response
          let parameters = [];
          let columns = [];
          
          if (validationData.details?.parameters && Array.isArray(validationData.details.parameters)) {
            parameters = validationData.details.parameters;
            console.log('📦 Found parameters in validationData.details.parameters:', parameters.length);
          }
          
          if (validationData.details?.columns && Array.isArray(validationData.details.columns)) {
            columns = validationData.details.columns;
            console.log('📦 Found columns in validationData.details.columns:', columns.length);
          }
          
          // Create a combined object with all the data
          const combinedObject = {
            ...selectedObject,
            details: validationData.details || {},
            parameters: parameters,
            columns: columns,
            owner: validationData.owner || selectedObject.targetOwner || selectedObject.owner,
            name: validationData.objectName || selectedObject.targetName || selectedObject.name,
            type: validationData.objectType || selectedObject.targetType || selectedObject.type
          };
          
          console.log('📦 Combined object for population:', {
            owner: combinedObject.owner,
            name: combinedObject.name,
            type: combinedObject.type,
            hasParameters: combinedObject.parameters?.length > 0,
            parametersCount: combinedObject.parameters?.length
          });
          
          // Populate the form from the database object
          await populateFormFromObject(combinedObject, false);
        } else {
          console.log('⚠️ No validation data received');
        }
        
        // Mark as initialized
        isInitializedRef.current = true;
        
      } catch (error) {
        console.error('❌ Error initializing modal:', error);
      } finally {
        setLoading(false);
      }
    };

    if (isOpen) {
      initializeFromObject();
    } else {
      // Reset refs when modal closes
      currentEditingApiIdRef.current = null;
      isInitializedRef.current = false;
    }
  }, [selectedObject, isOpen, authToken, obType, isEditing, collections, fromDashboard, selectedDbObject]);

  // Add this useEffect after your initialization useEffect - FIXED VERSION
  useEffect(() => {
    // Only check code availability for new APIs when the form loads
    // AND only if we're not in editing mode
    const checkInitialCode = async () => {
      if (isOpen && !isEditing && apiDetails.apiCode && apiDetails.apiCode.length >= 3) {
        try {
          console.log('🔍 Checking initial API code availability:', apiDetails.apiCode);
          const available = await checkCodeAvailability(apiDetails.apiCode);
          console.log('📊 Initial code availability result:', available);
          
          if (!available) {
            setValidationErrors(prev => ({ 
              ...prev, 
              apiCode: `API code "${apiDetails.apiCode}" is not available` 
            }));
          } else {
            // Clear any existing error if it becomes available
            setValidationErrors(prev => ({ ...prev, apiCode: null }));
          }
        } catch (error) {
          console.error('Error checking code availability on load:', error);
        }
      }
    };
    
    checkInitialCode();
  }, [isOpen, isEditing, apiDetails.apiCode]); // Remove authToken from dependencies to prevent re-run


  // Auto-populate SOAP service name when switching to SOAP protocol (including edit mode)
useEffect(() => {
  if (protocolType === 'soap') {
    let generatedServiceName = '';
    
    // Don't overwrite if there's already a service name and we're editing an existing API
    // But if it's empty, populate it
    if (soapConfig.serviceName) {
      return; // Already has a service name, don't overwrite
    }
    
    // Case 1: Database object exists
    if (selectedDbObject?.name) {
      generatedServiceName = selectedDbObject.name.charAt(0).toUpperCase() + 
                             selectedDbObject.name.slice(1).toLowerCase();
      generatedServiceName = generatedServiceName.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
      generatedServiceName = generatedServiceName.replace(/[^a-zA-Z0-9]/g, '');
      if (!generatedServiceName.toLowerCase().endsWith('service')) {
        generatedServiceName = generatedServiceName + 'Service';
      }
    } 
    // Case 2: Schema config object name exists
    else if (schemaConfig.objectName) {
      generatedServiceName = schemaConfig.objectName.charAt(0).toUpperCase() + 
                             schemaConfig.objectName.slice(1).toLowerCase();
      generatedServiceName = generatedServiceName.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
      generatedServiceName = generatedServiceName.replace(/[^a-zA-Z0-9]/g, '');
      if (!generatedServiceName.toLowerCase().endsWith('service')) {
        generatedServiceName = generatedServiceName + 'Service';
      }
    }
    // Case 3: API name exists
    else if (apiDetails.apiName) {
      generatedServiceName = apiDetails.apiName.replace(/\s+/g, '');
      if (!generatedServiceName.toLowerCase().endsWith('service')) {
        generatedServiceName = generatedServiceName + 'Service';
      }
    }
    // Case 4: Ultimate fallback
    else {
      generatedServiceName = 'APIService';
    }
    
    if (generatedServiceName && !soapConfig.serviceName) {
      console.log('🔧 Setting SOAP service name on protocol activation:', generatedServiceName);
      setSoapConfig(prev => ({ ...prev, serviceName: generatedServiceName }));
    }
  }
}, [protocolType, selectedDbObject, schemaConfig.objectName, apiDetails.apiName, soapConfig.serviceName]);


// Auto-set body type and HTTP method based on protocol - but preserve saved configs
useEffect(() => {
  
  const savedConfig = protocolConfigs[protocolType];
  
  if (protocolType === 'soap') {
    // Use saved config or defaults
    if (savedConfig?.httpMethod) {
      if (apiDetails.httpMethod !== savedConfig.httpMethod) {
        setApiDetails(prev => ({ ...prev, httpMethod: savedConfig.httpMethod }));
      }
    } else if (apiDetails.httpMethod !== 'POST') {
      setApiDetails(prev => ({ ...prev, httpMethod: 'POST' }));
    }
    
    if (savedConfig?.bodyType) {
      if (requestBody.bodyType !== savedConfig.bodyType) {
        setRequestBody(prev => ({ ...prev, bodyType: savedConfig.bodyType }));
      }
    } else if (requestBody.bodyType !== 'soap') {
      setRequestBody(prev => ({ ...prev, bodyType: 'soap' }));
    }
    return;
  }
  
  if (protocolType === 'graphql') {
    if (savedConfig?.httpMethod) {
      if (apiDetails.httpMethod !== savedConfig.httpMethod) {
        setApiDetails(prev => ({ ...prev, httpMethod: savedConfig.httpMethod }));
      }
    } else if (apiDetails.httpMethod !== 'POST') {
      setApiDetails(prev => ({ ...prev, httpMethod: 'POST' }));
    }
    
    if (savedConfig?.bodyType) {
      if (requestBody.bodyType !== savedConfig.bodyType) {
        setRequestBody(prev => ({ ...prev, bodyType: savedConfig.bodyType }));
      }
    } else if (requestBody.bodyType !== 'graphql') {
      setRequestBody(prev => ({ ...prev, bodyType: 'graphql' }));
    }
    return;
  }
  
  // For REST protocol, restore saved method
  if (protocolType === 'rest' && savedConfig?.httpMethod) {
    if (apiDetails.httpMethod !== savedConfig.httpMethod) {
      setApiDetails(prev => ({ ...prev, httpMethod: savedConfig.httpMethod }));
    }
    if (savedConfig?.bodyType && requestBody.bodyType !== savedConfig.bodyType) {
      setRequestBody(prev => ({ ...prev, bodyType: savedConfig.bodyType }));
    }
  }
}, [protocolType]); // Only run when protocol changes




// Auto-update GraphQL operation name when operation type changes
useEffect(() => {
  if (protocolType === 'graphql' && !isEditing && !graphqlConfig.operationName) {
    let generatedOperationName = '';
    
    if (selectedDbObject?.name) {
      const objectName = selectedDbObject.name;
      const objectType = selectedDbObject.type || schemaConfig.objectType;
      
      if (graphqlConfig.operationType === 'query') {
        generatedOperationName = `get${objectName.charAt(0).toUpperCase() + objectName.slice(1)}${objectType === 'TABLE' ? 's' : ''}`;
      } else if (graphqlConfig.operationType === 'mutation') {
        const mutationType = apiDetails.httpMethod === 'POST' ? 'create' :
                            apiDetails.httpMethod === 'PUT' ? 'update' :
                            apiDetails.httpMethod === 'DELETE' ? 'delete' : 'create';
        generatedOperationName = `${mutationType}${objectName.charAt(0).toUpperCase() + objectName.slice(1)}`;
      } else {
        generatedOperationName = `${objectName.charAt(0).toUpperCase() + objectName.slice(1)}Changed`;
      }
      
      generatedOperationName = generatedOperationName.replace(/[^a-zA-Z0-9]/g, '');
      generatedOperationName = generatedOperationName.charAt(0).toLowerCase() + generatedOperationName.slice(1);
      
      if (generatedOperationName && !graphqlConfig.operationName) {
        setGraphqlConfig(prev => ({ ...prev, operationName: generatedOperationName }));
      }
    }
  }
}, [protocolType, graphqlConfig.operationType, selectedDbObject, schemaConfig.objectType, apiDetails.httpMethod, isEditing, graphqlConfig.operationName]);



// Auto-populate GraphQL operation name and schema on initial load and for custom queries (including edit mode)
useEffect(() => {
  if (protocolType === 'graphql') {  // Remove the && !isEditing condition
    let generatedOperationName = '';
    let generatedSchema = '';
    
    // Case 1: Database object exists
    if (selectedDbObject?.name) {
      const objectName = selectedDbObject.name;
      const objectType = selectedDbObject.type || schemaConfig.objectType;
      
      // Generate operation name based on operation type
      if (graphqlConfig.operationType === 'query') {
        generatedOperationName = `get${objectName.charAt(0).toUpperCase() + objectName.slice(1)}${objectType === 'TABLE' ? 's' : ''}`;
      } else if (graphqlConfig.operationType === 'mutation') {
        const mutationType = apiDetails.httpMethod === 'POST' ? 'create' :
                            apiDetails.httpMethod === 'PUT' ? 'update' :
                            apiDetails.httpMethod === 'DELETE' ? 'delete' : 'create';
        generatedOperationName = `${mutationType}${objectName.charAt(0).toUpperCase() + objectName.slice(1)}`;
      } else if (graphqlConfig.operationType === 'subscription') {
        generatedOperationName = `${objectName.charAt(0).toUpperCase() + objectName.slice(1)}Changed`;
      }
      
      generatedOperationName = generatedOperationName.replace(/[^a-zA-Z0-9]/g, '');
      generatedOperationName = generatedOperationName.charAt(0).toLowerCase() + generatedOperationName.slice(1);
      
      generatedSchema = generateGraphQLSchemaFromObject();
    } 
    // Case 2: Custom query mode
    else if (sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery) {
      if (customQuery && customQuery.trim()) {
        const fromMatch = customQuery.match(/FROM\s+([^\s,;]+)/i);
        if (fromMatch && fromMatch[1]) {
          let tableName = fromMatch[1];
          if (tableName.includes('.')) {
            tableName = tableName.split('.').pop();
          }
          generatedOperationName = graphqlConfig.operationType === 'query' 
            ? `get${tableName.charAt(0).toUpperCase() + tableName.slice(1)}Data`
            : `execute${tableName.charAt(0).toUpperCase() + tableName.slice(1)}`;
        } else {
          generatedOperationName = graphqlConfig.operationType === 'query' ? 'getData' : 'executeQuery';
        }
      } else {
        generatedOperationName = graphqlConfig.operationType === 'query' ? 'getData' : 'executeQuery';
      }
      
      generatedSchema = generateGraphQLSchemaFromCustomQuery();
    }
    // Case 3: API name exists
    else if (apiDetails.apiName && !graphqlConfig.operationName) {
      generatedOperationName = apiDetails.apiName.replace(/\s+/g, '').charAt(0).toLowerCase() + 
                               apiDetails.apiName.replace(/\s+/g, '').slice(1);
    }
    // Case 4: Ultimate fallback
    else if (!graphqlConfig.operationName) {
      generatedOperationName = graphqlConfig.operationType === 'query' ? 'getData' : 'executeMutation';
    }
    
    // Only update if operation name is empty
    if (generatedOperationName && !graphqlConfig.operationName) {
      console.log('🔧 Auto-populating GraphQL operation name on load:', generatedOperationName);
      setGraphqlConfig(prev => ({ ...prev, operationName: generatedOperationName }));
    }
    
    // Auto-generate schema if empty
    if (generatedSchema && (!graphqlConfig.schema || graphqlConfig.schema === '')) {
      console.log('🔧 Auto-populating GraphQL schema on load');
      setGraphqlConfig(prev => ({ ...prev, schema: generatedSchema }));
    }
  }
}, [protocolType, selectedDbObject, sourceType, isCustomQuery, isEditingCustomQuery, customQuery, apiDetails.apiName, graphqlConfig.operationName, graphqlConfig.schema, graphqlConfig.operationType]); // Removed isEditing from dependencies



// Force GraphQL request body to update when parameters or mappings change
useEffect(() => {
  if (protocolType === 'graphql') {
    console.log('🔄 Generating GraphQL request body...');
    console.log('📊 Current parameters state:', parameters);
    console.log('📊 Current responseMappings state:', responseMappings);
    
    const operationName = graphqlConfig.operationName || 'query';
    const operationType = graphqlConfig.operationType || 'query';
    
    // Get IN parameters directly from the parameters state
    const inParams = parameters.filter(p => !p.paramMode || p.paramMode === 'IN');
    const outMappingsForQuery = responseMappings.filter(m => 
      (m.paramMode === 'OUT' || m.paramMode === 'IN/OUT' || m.paramMode === 'IN_OUT' || !m.paramMode)
    );
    
    console.log('📊 IN parameters count (direct):', inParams.length, inParams.map(p => ({ key: p.key, location: p.parameterLocation, apiType: p.apiType, example: p.example })));
    console.log('📊 OUT mappings count (direct):', outMappingsForQuery.length);
    
    // For GraphQL queries, parameters should be in the query location
    let queryParams = [];
    let inputParams = [];
    
    if (operationType === 'query') {
      // For queries, treat ALL parameters as query parameters
      queryParams = inParams;
      inputParams = [];
      console.log('📊 GraphQL Query mode - treating all parameters as query arguments:', queryParams.map(p => p.key));
    } else if (operationType === 'mutation') {
      // For mutations, parameters can be in body as input object
      queryParams = inParams.filter(p => p.parameterLocation !== 'body');
      inputParams = inParams.filter(p => p.parameterLocation === 'body');
      console.log('📊 GraphQL Mutation mode - query params:', queryParams.map(p => p.key));
      console.log('📊 GraphQL Mutation mode - input params:', inputParams.map(p => p.key));
    }
    
    let variableDefinitions = '';
    let variableUsages = '';
    let variablesObject = {};
    
    // Build variables for query parameters
    if (queryParams.length > 0) {
      console.log('📊 Building variables from params:', queryParams.map(p => p.key));
      
      // Build variable definitions with proper GraphQL types
      variableDefinitions = '(' + queryParams.map(p => {
        const graphqlType = getGraphQLType(p.oracleType, !p.required);
        return `$${p.key}: ${graphqlType}`;
      }).join(', ') + ')';
      
      // Use variables in query
      variableUsages = '(' + queryParams.map(p => `${p.key}: $${p.key}`).join(', ') + ')';
      
      // Build variables object with CORRECTLY PARSED example values
      queryParams.forEach(p => {
        let val = p.example;
        
        // If no example provided, generate based on type
        if (!val || val === '') {
          if (p.apiType === 'integer') {
            val = 123;
          } else if (p.apiType === 'number') {
            val = 123.45;
          } else if (p.apiType === 'boolean') {
            val = true;
          } else {
            val = 'sample';
          }
        } else {
          // Parse the value based on its type
          if (p.apiType === 'integer') {
            val = parseInt(val, 10);
            if (isNaN(val)) val = 123;
          } else if (p.apiType === 'number') {
            val = parseFloat(val);
            if (isNaN(val)) val = 123.45;
          } else if (p.apiType === 'boolean') {
            val = val === 'true' || val === '1' || val === 'yes';
          }
          // For string, keep as is
        }
        
        variablesObject[p.key] = val;
      });
      
      console.log('📊 Variables object built:', variablesObject);
    }
    
    // Build variables for mutations with input object
    let mutationVariableDef = '';
    let mutationVariableUsage = '';
    let mutationVariablesObject = {};
    
    if (operationType === 'mutation' && inputParams.length > 0) {
      console.log('📊 Building mutation input from:', inputParams.map(p => p.key));
      
      mutationVariableDef = `($input: ${operationName}Input!)`;
      mutationVariableUsage = '(input: $input)';
      
      const inputObject = {};
      inputParams.forEach(p => {
        let val = p.example;
        
        if (!val || val === '') {
          if (p.apiType === 'integer') {
            val = 123;
          } else if (p.apiType === 'number') {
            val = 123.45;
          } else if (p.apiType === 'boolean') {
            val = true;
          } else {
            val = 'sample';
          }
        } else {
          if (p.apiType === 'integer') {
            val = parseInt(val, 10);
            if (isNaN(val)) val = 123;
          } else if (p.apiType === 'number') {
            val = parseFloat(val);
            if (isNaN(val)) val = 123.45;
          } else if (p.apiType === 'boolean') {
            val = val === 'true' || val === '1' || val === 'yes';
          }
        }
        
        inputObject[p.key] = val;
      });
      mutationVariablesObject = { input: inputObject };
    }
    
    // Build the query string with response fields
    let queryString = '';
    if (operationType === 'query') {
      if (queryParams.length > 0) {
        queryString = `${operationType} ${operationName}${variableDefinitions} {\n  ${operationName}${variableUsages} {\n`;
        if (outMappingsForQuery.length > 0) {
          queryString += outMappingsForQuery.slice(0, 20).map(m => `    ${m.apiField}`).join('\n');
          if (outMappingsForQuery.length > 20) {
            queryString += `\n    # ... ${outMappingsForQuery.length - 20} more fields`;
          }
        } else {
          queryString += `    id\n    createdAt\n    updatedAt`;
        }
        queryString += `\n  }\n}`;
      } else {
        queryString = `${operationType} {\n  ${operationName} {\n`;
        if (outMappingsForQuery.length > 0) {
          queryString += outMappingsForQuery.slice(0, 20).map(m => `    ${m.apiField}`).join('\n');
          if (outMappingsForQuery.length > 20) {
            queryString += `\n    # ... ${outMappingsForQuery.length - 20} more fields`;
          }
        } else {
          queryString += `    id\n    createdAt\n    updatedAt`;
        }
        queryString += `\n  }\n}`;
      }
    } else if (operationType === 'mutation') {
      if (inputParams.length > 0) {
        queryString = `${operationType} ${operationName}${mutationVariableDef} {\n  ${operationName}${mutationVariableUsage} {\n    success\n    message\n    data {\n`;
        if (outMappingsForQuery.length > 0) {
          queryString += outMappingsForQuery.slice(0, 20).map(m => `      ${m.apiField}`).join('\n');
          if (outMappingsForQuery.length > 20) {
            queryString += `\n      # ... ${outMappingsForQuery.length - 20} more fields`;
          }
        } else {
          queryString += `      id\n      createdAt`;
        }
        queryString += `\n    }\n  }\n}`;
      } else if (queryParams.length > 0) {
        queryString = `${operationType} ${operationName}${variableDefinitions} {\n  ${operationName}${variableUsages} {\n    success\n    message\n  }\n}`;
      } else {
        queryString = `${operationType} {\n  ${operationName} {\n    success\n    message\n  }\n}`;
      }
    }
    
    // Use the appropriate variables based on operation type
    const finalVariables = operationType === 'mutation' && inputParams.length > 0 
      ? mutationVariablesObject 
      : variablesObject;
    
    const freshGraphQLRequest = JSON.stringify({
      query: queryString,
      variables: Object.keys(finalVariables).length > 0 ? finalVariables : {}
    }, null, 2);
    
    console.log('📝 Generated GraphQL request:', freshGraphQLRequest);
    
    // Update the request body sample
    if (requestBody.sample !== freshGraphQLRequest) {
      handleRequestBodyChange('sample', freshGraphQLRequest);
    }
  }
}, [parameters, responseMappings, protocolType, graphqlConfig.operationName, graphqlConfig.operationType, requestBody.bodyType]);



const generateSOAPEnvelope = useCallback(() => {
  const inParams = getInParameters();
  const soapVersion = soapConfig.version === '1.2' ? 'http://www.w3.org/2003/05/soap-envelope' : 'http://schemas.xmlsoap.org/soap/envelope/';
  const operationName = soapConfig.soapAction || (apiDetails.apiCode || 'ProcessRequest');
  
  let bodyContent = '';
  
  if (soapConfig.bindingStyle === 'rpc') {
    if (inParams.length > 0) {
      bodyContent = `<${operationName}>
${inParams.map(p => `        <${p.key}>${p.example || ''}</${p.key}>`).join('\n')}
      </${operationName}>`;
    } else {
      bodyContent = `<${operationName}/>`;
    }
  } else {
    if (inParams.length > 0) {
      bodyContent = `<${operationName} xmlns="${soapConfig.namespace}">
${inParams.map(p => `        <${p.key}>${p.example || ''}</${p.key}>`).join('\n')}
      </${operationName}>`;
    } else {
      bodyContent = `<${operationName} xmlns="${soapConfig.namespace}"/>`;
    }
  }
  
  return `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="${soapVersion}"${soapConfig.version === '1.2' ? '' : ' xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"'}>
  <soap:Header>
    <!-- Optional SOAP headers -->
  </soap:Header>
  <soap:Body>
    ${bodyContent}
  </soap:Body>
</soap:Envelope>`;
}, [soapConfig.version, soapConfig.bindingStyle, soapConfig.namespace, soapConfig.soapAction, apiDetails.apiCode, getInParameters]);


// Refresh request body when switching to Request tab (for all protocols)
useEffect(() => {
  if (activeTab === 'request') {
    if (protocolType === 'soap') {
      const freshSoapEnvelope = generateSOAPEnvelope();
      if (requestBody.sample !== freshSoapEnvelope) {
        console.log('🔄 Refreshing SOAP request body on Request tab activation');
        handleRequestBodyChange('sample', freshSoapEnvelope);
      }
    } else if (protocolType === 'graphql') {
      const operationName = graphqlConfig.operationName || 'query';
      const operationType = graphqlConfig.operationType || 'query';
      const inParams = getInParameters();
      const outMappings = getOutMappings();
      
      let queryString = '';
      if (operationType === 'query') {
        const queryParams = inParams.filter(p => p.parameterLocation !== 'body');
        const paramsString = queryParams.length > 0 
          ? `(${queryParams.slice(0, 50).map(p => `${p.key}: "${p.example || 'value'}"`).join(', ')})` 
          : '';
        queryString = `${operationType} {\n  ${operationName}${paramsString} {\n    ${outMappings.slice(0, 50).map(m => m.apiField).join('\n    ')}\n  }\n}`;
      } else {
        const inputParams = inParams.length > 0 ? inParams : [];
        queryString = `${operationType} {\n  ${operationName}(input: {\n    ${inputParams.slice(0, 50).map(p => `${p.key}: "${p.example || 'value'}"`).join('\n    ')}\n  }) {\n    success\n    message\n    data {\n      ${outMappings.slice(0, 50).map(m => m.apiField).join('\n      ')}\n    }\n  }\n}`;
      }
      
      const freshGraphQLRequest = JSON.stringify({ query: queryString, variables: {} }, null, 2);
      if (requestBody.sample !== freshGraphQLRequest) {
        console.log('🔄 Refreshing GraphQL request body on Request tab activation');
        handleRequestBodyChange('sample', freshGraphQLRequest);
      }
    } else if (protocolType === 'rest' && requestBody.bodyType !== 'none' && requestBody.bodyType !== 'binary') {
      const bodyParams = getInParameters().filter(p => p.parameterLocation === 'body');
      if (bodyParams.length > 0) {
        let freshRequestBody = '';
        if (requestBody.bodyType === 'json') {
          const requestBodyObj = {};
          bodyParams.forEach(param => {
            requestBodyObj[param.key] = param.example || `sample_${param.key}`;
          });
          freshRequestBody = JSON.stringify(requestBodyObj, null, 2);
        } else if (requestBody.bodyType === 'xml') {
          let xmlSample = `<?xml version="1.0" encoding="UTF-8"?>\n<request>\n`;
          bodyParams.forEach(param => {
            xmlSample += `  <${param.key}>${param.example || `sample_${param.key}`}</${param.key}>\n`;
          });
          xmlSample += `</request>`;
          freshRequestBody = xmlSample;
        }
        
        if (freshRequestBody && requestBody.sample !== freshRequestBody) {
          console.log('🔄 Refreshing REST request body on Request tab activation');
          handleRequestBodyChange('sample', freshRequestBody);
        }
      }
    }
  }
}, [activeTab, protocolType, parameters, responseMappings, getInParameters, getOutMappings, graphqlConfig.operationName, graphqlConfig.operationType, requestBody.bodyType, requestBody.sample]);

// Add this effect to protect against invalid body type configurations
useEffect(() => {
  const method = apiDetails.httpMethod;
  const currentBodyType = requestBody.bodyType;
  
  // Methods that should NEVER have a body
  const methodsWithoutBody = ['GET', 'DELETE', 'HEAD', 'OPTIONS'];
  
  if (methodsWithoutBody.includes(method) && currentBodyType !== 'none') {
    // Force body type to 'none' for these methods
    console.log(`🔄 HTTP method ${method} does not support request body, setting bodyType to 'none'`);
    setRequestBody(prev => ({
      ...prev,
      bodyType: 'none'
    }));
  }
}, [apiDetails.httpMethod, requestBody.bodyType]);

// Auto-generate request and response samples when protocol type changes OR when mappings change
useEffect(() => {
  if (protocolType === 'soap') {
    // Generate SOAP request envelope using current parameters
    const soapEnvelope = generateSOAPEnvelope();
    if (requestBody.sample !== soapEnvelope) {
      handleRequestBodyChange('sample', soapEnvelope);
    }
    
    // Generate SOAP response envelope - use the most up-to-date out mappings
    const operationName = soapConfig.soapAction || apiDetails.apiCode || 'Operation';
    const outMappings = getOutMappings();
    
    const soapSuccess = `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="${soapConfig.version === '1.2' ? 'http://www.w3.org/2003/05/soap-envelope' : 'http://schemas.xmlsoap.org/soap/envelope/'}">
  <soap:Body>
    <${operationName}Response xmlns="${soapConfig.namespace}">
      <success>true</success>
      <message>Request processed successfully</message>
      ${outMappings.slice(0, 50).map(m =>
        `<${m.apiField}>${m.apiType === 'integer' ? '123' : m.apiType === 'boolean' ? 'true' : 'value'}</${m.apiField}>`
      ).join('\n      ')}
    </${operationName}Response>
  </soap:Body>
</soap:Envelope>`;
    handleResponseBodyChange('successSchema', soapSuccess);
    
    const soapFault = `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="${soapConfig.version === '1.2' ? 'http://www.w3.org/2003/05/soap-envelope' : 'http://schemas.xmlsoap.org/soap/envelope/'}">
  <soap:Body>
    <soap:Fault>
      <faultcode>soap:Server</faultcode>
      <faultstring>An error occurred processing the request</faultstring>
      <detail>
        <errorCode>ERROR_CODE</errorCode>
        <errorMessage>Detailed error description</errorMessage>
      </detail>
    </soap:Fault>
  </soap:Body>
</soap:Envelope>`;
  
 handleResponseBodyChange('errorSchema', soapFault);
    
  } else if (protocolType === 'graphql') {
    // Generate GraphQL query sample
    const operationName = graphqlConfig.operationName || 'query';
    const operationType = graphqlConfig.operationType || 'query';
    
    let queryString = '';
    const inParams = getInParameters();
    const outMappings = getOutMappings();
    
    if (operationType === 'query') {
      const queryParams = inParams.filter(p => p.parameterLocation !== 'body');
      const paramsString = queryParams.length > 0 
        ? `(${queryParams.map(p => `${p.key}: "${p.example || 'value'}"`).join(', ')})` 
        : '';
      
      queryString = `${operationType} {\n  ${operationName}${paramsString} {\n    ${outMappings.slice(0, 3).map(m => m.apiField).join('\n    ')}\n  }\n}`;
    } else {
      const inputParams = inParams.length > 0 ? inParams : [];
      queryString = `${operationType} {\n  ${operationName}(input: {\n    ${inputParams.slice(0, 3).map(p => `${p.key}: "${p.example || 'value'}"`).join('\n    ')}\n  }) {\n    success\n    message\n    data {\n      ${outMappings.slice(0, 3).map(m => m.apiField).join('\n      ')}\n    }\n  }\n}`;
    }
    
    const graphqlRequest = JSON.stringify({
      query: queryString,
      variables: {}
    }, null, 2);
    handleRequestBodyChange('sample', graphqlRequest);
    
    // Generate GraphQL response
    const responseData = {};
    outMappings.slice(0, 50).forEach(mapping => {
      if (mapping.apiType === 'integer') {
        responseData[mapping.apiField] = 123;
      } else if (mapping.apiType === 'boolean') {
        responseData[mapping.apiField] = true;
      } else if (mapping.format === 'date-time') {
        responseData[mapping.apiField] = '2024-01-01T00:00:00Z';
      } else {
        responseData[mapping.apiField] = mapping.apiField === 'id' ? 1 : 'sample';
      }
    });
    
    const graphqlSuccess = JSON.stringify({
      data: { [operationName]: responseData }
    }, null, 2);
    handleResponseBodyChange('successSchema', graphqlSuccess);
    
    const graphqlError = JSON.stringify({
      errors: [{
        message: 'Error description',
        locations: [{ line: 1, column: 1 }],
        path: [operationName],
        extensions: { code: 'ERROR_CODE' }
      }]
    }, null, 2);
    handleResponseBodyChange('errorSchema', graphqlError);
    
  } else if (protocolType === 'rest') {
    // Generate REST request body sample
    const bodyParams = getInParameters().filter(p => p.parameterLocation === 'body');
    
    if (bodyParams.length > 0 && requestBody.bodyType !== 'none') {
      if (requestBody.bodyType === 'json') {
        const requestBodyObj = {};
        bodyParams.forEach(param => {
          if (param.apiType === 'integer') {
            requestBodyObj[param.key] = parseInt(param.example) || 123;
          } else if (param.apiType === 'boolean') {
            requestBodyObj[param.key] = param.example === 'true' || false;
          } else {
            requestBodyObj[param.key] = param.example || `sample_${param.key}`;
          }
        });
        handleRequestBodyChange('sample', JSON.stringify(requestBodyObj, null, 2));
      } else if (requestBody.bodyType === 'xml') {
        let xmlSample = `<?xml version="1.0" encoding="UTF-8"?>\n<request>\n`;
        bodyParams.forEach(param => {
          xmlSample += `  <${param.key}>${param.example || `sample_${param.key}`}</${param.key}>\n`;
        });
        xmlSample += `</request>`;
        handleRequestBodyChange('sample', xmlSample);
      }
    } else if (requestBody.bodyType === 'none') {
      handleRequestBodyChange('sample', '');
    }
    
    // Generate REST response sample
    const outMappings = getOutMappings();
    if (outMappings.length > 0) {
      const sampleData = {};
      outMappings.slice(0, 50).forEach(mapping => {
        if (mapping.apiType === 'integer') {
          sampleData[mapping.apiField] = 123;
        } else if (mapping.apiType === 'boolean') {
          sampleData[mapping.apiField] = true;
        } else if (mapping.format === 'date-time') {
          sampleData[mapping.apiField] = '2024-01-01T00:00:00Z';
        } else if (mapping.apiType === 'array') {
          sampleData[mapping.apiField] = [];
        } else if (mapping.apiType === 'object') {
          sampleData[mapping.apiField] = {};
        } else {
          sampleData[mapping.apiField] = mapping.apiField === 'id' ? 1 : 'sample';
        }
      });
      
      const restSuccess = JSON.stringify({
        success: true,
        data: sampleData,
        message: 'Request processed successfully',
        metadata: {
          timestamp: '{{timestamp}}',
          apiVersion: apiDetails.version,
          requestId: '{{requestId}}'
        }
      }, null, 2);
      
      handleResponseBodyChange('successSchema', restSuccess);
      
      const restError = JSON.stringify({
        success: false,
        error: {
          code: 'ERROR_CODE',
          message: 'Error description',
          details: {}
        }
      }, null, 2);
      handleResponseBodyChange('errorSchema', restError);
      setErrorSchemaVersion(prev => prev + 1);
    }
  }
}, [protocolType, soapConfig.version, soapConfig.namespace, soapConfig.soapAction, graphqlConfig.operationName, graphqlConfig.operationType, apiDetails.version, requestBody.bodyType, responseMappings]); // Added responseMappings as dependency


// Regenerate SOAP response when response mappings change
useEffect(() => {
  if (protocolType === 'soap' && responseMappings.length > 0) {
    const operationName = soapConfig.soapAction || apiDetails.apiCode || 'Operation';
    const outMappings = getOutMappings();
    
    const soapSuccess = `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="${soapConfig.version === '1.2' ? 'http://www.w3.org/2003/05/soap-envelope' : 'http://schemas.xmlsoap.org/soap/envelope/'}">
  <soap:Body>
    <${operationName}Response xmlns="${soapConfig.namespace}">
      <success>true</success>
      <message>Request processed successfully</message>
      ${outMappings.slice(0, 50).map(m =>
        `<${m.apiField}>${m.apiType === 'integer' ? '123' : m.apiType === 'boolean' ? 'true' : 'value'}</${m.apiField}>`
      ).join('\n      ')}
    </${operationName}Response>
  </soap:Body>
</soap:Envelope>`;
    
    // Only update if the current success schema is the default one (not user-modified)
    const currentSchema = responseBody.successSchema;
    const isDefaultSchema = !currentSchema || 
      currentSchema.includes('Request processed successfully') ||
      currentSchema === '';
    
    if (isDefaultSchema) {
      handleResponseBodyChange('successSchema', soapSuccess);
    }
  }
}, [responseMappings, protocolType, soapConfig.soapAction, apiDetails.apiCode, soapConfig.version, soapConfig.namespace]);

  // ==================== GENERATION FUNCTIONS ====================

  // Generate PL/SQL code (for preview only)
  const generatePLSQLCode = () => {
    const inParams = getInParameters();
    const outMappings = getOutMappings();
    
    const paramList = inParams.map(p => 
      `${p.key} IN ${p.oracleType}${p.required ? ' NOT NULL' : ''} DEFAULT ${p.defaultValue || 'NULL'}`
    ).join(',\n    ');
    
    const mappingList = outMappings
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
-- Protocol: ${protocolType.toUpperCase()}
-- Source Object: ${schemaConfig.schemaName}.${schemaConfig.objectName} (${schemaConfig.objectType})
-- Collection: ${selectedCollection?.name || 'Not assigned'}
-- Folder: ${selectedFolder?.name || 'Not assigned'}
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


  // Update the generateWSDL function (if you have one)
const generateWSDL = () => {
  const operationName = soapConfig.soapAction || (apiDetails.apiCode || 'ProcessRequest');
  
  return `<?xml version="1.0" encoding="UTF-8"?>
<definitions name="${soapConfig.serviceName || apiDetails.apiName}"
             targetNamespace="${soapConfig.namespace}"
             xmlns:tns="${soapConfig.namespace}"
             xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
             xmlns="http://schemas.xmlsoap.org/wsdl/">
  
  <types>
    <schema targetNamespace="${soapConfig.namespace}">
      <!-- Input message schema -->
      <element name="${operationName}">
        <complexType>
          <sequence>
            ${getInParameters().map(p => `
            <element name="${p.key}" type="xsd:${p.oracleType === 'NUMBER' ? 'decimal' : 'string'}" minOccurs="${p.required ? '1' : '0'}"/>`).join('')}
          </sequence>
        </complexType>
      </element>
      
      <!-- Output message schema -->
      <element name="${operationName}Response">
        <complexType>
          <sequence>
            ${getOutMappings().map(m => `
            <element name="${m.apiField}" type="xsd:${m.oracleType === 'NUMBER' ? 'decimal' : 'string'}"/>`).join('')}
          </sequence>
        </complexType>
      </element>
    </schema>
  </types>
  
  <message name="${operationName}Request">
    <part name="parameters" element="tns:${operationName}"/>
  </message>
  
  <message name="${operationName}Response">
    <part name="parameters" element="tns:${operationName}Response"/>
  </message>
  
  <portType name="${soapConfig.serviceName || apiDetails.apiName}PortType">
    <operation name="${operationName}">
      <input message="tns:${operationName}Request"/>
      <output message="tns:${operationName}Response"/>
    </operation>
  </portType>
  
  <binding name="${soapConfig.serviceName || apiDetails.apiName}Binding" type="tns:${soapConfig.serviceName || apiDetails.apiName}PortType">
    <soap:binding transport="http://schemas.xmlsoap.org/soap/http" style="${soapConfig.bindingStyle}"/>
    <operation name="${operationName}">
      <soap:operation soapAction="${soapConfig.soapAction}" style="${soapConfig.bindingStyle}"/>
      <input>
        <soap:body use="${soapConfig.encodingStyle}" namespace="${soapConfig.namespace}"/>
      </input>
      <output>
        <soap:body use="${soapConfig.encodingStyle}" namespace="${soapConfig.namespace}"/>
      </output>
    </operation>
  </binding>
  
  <service name="${soapConfig.serviceName || apiDetails.apiName}">
    <port name="${soapConfig.portName || 'ServicePort'}" binding="tns:${soapConfig.serviceName || apiDetails.apiName}Binding">
      <soap:address location="${apiDetails.basePath}${apiDetails.endpointPath}"/>
    </port>
  </service>
</definitions>`;
};

  // Generate GraphQL Schema (for preview only)
 const generateGraphQLSchema = () => {
  // If user has custom schema, use it; otherwise auto-generate
  if (graphqlConfig.schema && graphqlConfig.schema.trim() !== '') {
    return graphqlConfig.schema;
  }
  return generateGraphQLSchemaFromObject();
};

  // Generate OpenAPI specification (for preview only)
  const generateOpenAPISpec = () => {
    const inParams = getInParameters();
    const outMappings = getOutMappings();
    
    // Group parameters by location
    const pathParams = inParams.filter(p => p.parameterLocation === 'path');
    const queryParams = inParams.filter(p => p.parameterLocation === 'query');
    const headerParams = inParams.filter(p => p.parameterLocation === 'header');
    const bodyParams = inParams.filter(p => p.parameterLocation === 'body');
    
    const spec = {
      openapi: '3.0.0',
      info: {
        title: apiDetails.apiName,
        description: apiDetails.description,
        version: apiDetails.version,
        contact: {
          name: apiDetails.owner,
          email: `${apiDetails.owner.toLowerCase()}@example.com`
        },
        'x-collection': selectedCollection ? {
          name: selectedCollection.name,
          id: selectedCollection.id,
          type: selectedCollection.type
        } : undefined,
        'x-folder': selectedFolder ? {
          name: selectedFolder.name,
          id: selectedFolder.id
        } : undefined,
        'x-protocol': protocolType
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
      tags: [
        {
          name: selectedCollection?.name || 'General',
          description: selectedCollection?.description || 'General APIs'
        }
      ],
      paths: {
        [apiDetails.endpointPath + (pathParams.length > 0 ? pathParams.map(p => `/{${p.key}}`).join('') : '')]: {
          [apiDetails.httpMethod.toLowerCase()]: {
            summary: apiDetails.apiName,
            description: apiDetails.description,
            tags: [selectedCollection?.name || 'General'],
            operationId: apiDetails.apiCode.toLowerCase(),
            parameters: [
              ...pathParams.map(p => ({
                name: p.key,
                in: 'path',
                description: p.description,
                required: true,
                schema: {
                  type: p.apiType,
                  example: p.example,
                  pattern: p.validationPattern,
                  default: p.defaultValue,
                },
              })),
              ...queryParams.map(p => ({
                name: p.key,
                in: 'query',
                description: p.description,
                required: p.required,
                schema: {
                  type: p.apiType,
                  example: p.example,
                  pattern: p.validationPattern,
                  default: p.defaultValue,
                },
              })),
              ...headerParams.map(p => ({
                name: p.key,
                in: 'header',
                description: p.description,
                required: p.required,
                schema: {
                  type: p.apiType,
                  example: p.example,
                  pattern: p.validationPattern,
                  default: p.defaultValue,
                },
              })),
            ],
            ...(requestBody.bodyType !== 'none' && bodyParams.length > 0 && {
                requestBody: {
                  description: 'Request body parameters',
                  required: bodyParams.some(p => p.required),
                  content: {
                    [requestBody.bodyType === 'json' ? 'application/json' : 
                      requestBody.bodyType === 'xml' ? 'application/xml' :
                      requestBody.bodyType === 'form-data' ? 'multipart/form-data' :
                      requestBody.bodyType === 'urlencoded' ? 'application/x-www-form-urlencoded' :
                      'text/plain']: {
                      schema: {
                        type: 'object',
                        properties: bodyParams.reduce((acc, p) => ({
                          ...acc,
                          [p.key]: {
                            type: p.apiType,
                            description: p.description,
                            nullable: !p.required,
                            example: p.example,
                          }
                        }), {}),
                        required: bodyParams.filter(p => p.required).map(p => p.key)
                      }
                    }
                  }
                }
              }),
            responses: {
              '200': {
                description: 'Successful response',
                content: {
                  'application/json': {
                    schema: {
                      type: 'object',
                      properties: {
                        success: { type: 'boolean', example: true },
                        data: {
                          type: 'object',
                          properties: outMappings.reduce((acc, mapping) => ({
                            ...acc,
                            [mapping.apiField]: {
                              type: mapping.apiType,
                              description: `Maps to ${mapping.dbColumn} (${mapping.oracleType})`,
                              nullable: mapping.nullable,
                              format: mapping.format,
                            },
                          }), {}),
                        },
                        message: { type: 'string', example: 'Request processed successfully' },
                        metadata: {
                          type: 'object',
                          properties: {
                            timestamp: { type: 'string', format: 'date-time' },
                            apiVersion: { type: 'string' },
                            requestId: { type: 'string' }
                          }
                        }
                      },
                    },
                    example: JSON.parse(responseBody.successSchema)
                  },
                },
              },
              '400': {
                description: 'Bad Request',
                content: {
                  'application/json': {
                    schema: {
                      type: 'object',
                      properties: {
                        success: { type: 'boolean', example: false },
                        error: {
                          type: 'object',
                          properties: {
                            code: { type: 'string' },
                            message: { type: 'string' },
                            details: { type: 'object' }
                          }
                        }
                      }
                    },
                    example: JSON.parse(responseBody.errorSchema)
                  }
                }
              },
              '401': {
                description: 'Unauthorized',
                content: {
                  'application/json': {
                    schema: {
                      type: 'object',
                      properties: {
                        success: { type: 'boolean', example: false },
                        error: {
                          type: 'object',
                          properties: {
                            code: { type: 'string', example: 'UNAUTHORIZED' },
                            message: { type: 'string', example: 'Authentication required' }
                          }
                        }
                      }
                    }
                  }
                }
              },
              '500': {
                description: 'Internal Server Error'
              }
            },
            security: authConfig.authType !== 'none' ? [{ [authConfig.authType]: [] }] : [],
          },
        },
      },
      components: {
        securitySchemes: {
          apiKey: {
            type: 'apiKey',
            name: authConfig.apiKeyHeader || 'X-API-Key',
            in: 'header',
            description: 'API Key authentication'
          },
          bearer: {
            type: 'http',
            scheme: 'bearer',
            bearerFormat: 'JWT',
            description: 'JWT Bearer token authentication'
          },
          basic: {
            type: 'http',
            scheme: 'basic',
            description: 'Basic authentication'
          }
        }
      }
    };
    
    return JSON.stringify(spec, null, 2);
  };

  // Generate Postman collection (for preview only)
  const generatePostmanCollection = () => {
    const inParams = getInParameters();
    const outMappings = getOutMappings();
    
    // Group parameters by location
    const pathParams = inParams.filter(p => p.parameterLocation === 'path');
    const queryParams = inParams.filter(p => p.parameterLocation === 'query');
    const headerParams = inParams.filter(p => p.parameterLocation === 'header');
    const bodyParams = inParams.filter(p => p.parameterLocation === 'body');
    
    // Build URL with path parameters
    let urlPath = apiDetails.endpointPath;
    if (pathParams.length > 0) {
      pathParams.forEach(p => {
        urlPath = urlPath.replace(`{${p.key}}`, `:${p.key}`);
      });
    }
    
    // Build request body if there are body parameters
    let body = null;
    if (requestBody.bodyType !== 'none' && bodyParams.length > 0) {
      const bodyObject = {};
      bodyParams.forEach(p => {
        bodyObject[p.key] = p.example || (p.apiType === 'integer' ? 123 : 'sample');
      });
      
      body = {
        mode: requestBody.bodyType === 'json' ? 'raw' : 
              requestBody.bodyType === 'form-data' ? 'formdata' :
              requestBody.bodyType === 'urlencoded' ? 'urlencoded' : 'raw',
        raw: requestBody.bodyType === 'json' ? JSON.stringify(bodyObject, null, 2) : JSON.stringify(bodyObject),
        options: {
          raw: {
            language: requestBody.bodyType === 'json' ? 'json' : 'text'
          }
        }
      };
      
      if (requestBody.bodyType === 'form-data') {
        body.formdata = bodyParams.map(p => ({
          key: p.key,
          value: p.example || '',
          type: 'text',
          description: p.description
        }));
      } else if (requestBody.bodyType === 'urlencoded') {
        body.urlencoded = bodyParams.map(p => ({
          key: p.key,
          value: p.example || '',
          description: p.description
        }));
      }
    }
    
    // Build auth based on selected type
    let auth = null;
    if (authConfig.authType === 'apiKey') {
      auth = {
        type: 'apikey',
        apikey: [
          {
            key: 'key',
            value: authConfig.apiKeyHeader,
            type: 'string'
          },
          {
            key: 'value',
            value: authConfig.apiKeyValue || '{{apiKey}}',
            type: 'string'
          },
          {
            key: 'in',
            value: 'header',
            type: 'string'
          }
        ]
      };
      
      // Add secret as additional header if provided
      if (authConfig.apiSecretHeader && authConfig.apiSecretValue) {
        headers.push({
          key: authConfig.apiSecretHeader,
          value: authConfig.apiSecretValue || '{{apiSecret}}',
          description: 'API Secret'
        });
      }
    } else if (authConfig.authType === 'bearer') {
      auth = {
        type: 'bearer',
        bearer: [
          {
            key: 'token',
            value: authConfig.jwtToken || '{{jwtToken}}',
            type: 'string'
          }
        ]
      };
    } else if (authConfig.authType === 'basic') {
      auth = {
        type: 'basic',
        basic: [
          {
            key: 'username',
            value: authConfig.basicUsername || '{{username}}',
            type: 'string'
          },
          {
            key: 'password',
            value: authConfig.basicPassword || '{{password}}',
            type: 'string'
          }
        ]
      };
    }
    
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
            header: [
              ...headers.map(h => ({
                key: h.key,
                value: h.value,
                description: h.description,
              })),
              ...headerParams.map(p => ({
                key: p.key,
                value: p.example || '',
                description: p.description,
              }))
            ],
            url: {
              raw: `{{baseUrl}}${apiDetails.basePath}${urlPath}`,
              host: ['{{baseUrl}}'],
              path: urlPath.split('/').filter(Boolean),
              query: queryParams.map(p => ({
                key: p.key,
                value: p.example || '',
                description: p.description,
              })),
              variable: pathParams.map(p => ({
                key: p.key,
                value: p.example || '',
                description: p.description,
              }))
            },
            ...(body && { body }),
            ...(auth && { auth }),
            description: apiDetails.description,
          },
          response: [
            {
              name: 'Success Response',
              originalRequest: {
                method: apiDetails.httpMethod,
                header: [],
                url: {
                  raw: `{{baseUrl}}${apiDetails.basePath}${urlPath}`
                }
              },
              status: 'OK',
              code: 200,
              header: [
                {
                  key: 'Content-Type',
                  value: 'application/json'
                }
              ],
              body: responseBody.successSchema
            },
            {
              name: 'Error Response',
              originalRequest: {
                method: apiDetails.httpMethod,
                header: [],
                url: {
                  raw: `{{baseUrl}}${apiDetails.basePath}${urlPath}`
                }
              },
              status: 'Bad Request',
              code: 400,
              header: [
                {
                  key: 'Content-Type',
                  value: 'application/json'
                }
              ],
              body: responseBody.errorSchema
            }
          ]
        },
      ],
      variable: [
        {
          key: 'baseUrl',
          value: 'https://api.example.com',
          type: 'string',
        },
        ...(authConfig.authType === 'apiKey' ? [
          {
            key: 'apiKey',
            value: authConfig.apiKeyValue || 'your-api-key',
            type: 'string'
          },
          {
            key: 'apiSecret',
            value: authConfig.apiSecretValue || 'your-api-secret',
            type: 'string'
          }
        ] : []),
        ...(authConfig.authType === 'bearer' ? [
          {
            key: 'jwtToken',
            value: authConfig.jwtToken || 'your-jwt-token',
            type: 'string'
          }
        ] : []),
        ...(authConfig.authType === 'basic' ? [
          {
            key: 'username',
            value: authConfig.basicUsername || 'username',
            type: 'string'
          },
          {
            key: 'password',
            value: authConfig.basicPassword || 'password',
            type: 'string'
          }
        ] : [])
      ],
    };
    
    return JSON.stringify(collection, null, 2);
  };

  // Add a new function to generate SQL INSERT statements for the API configuration
  const generateSQLInsertStatements = () => {
    const inParams = getInParameters();
    const outMappings = getOutMappings();
    
    // Generate a unique API ID if not present
    const apiId = isEditing ? (selectedObject?.id || `api-${Date.now()}`) : `api-${Date.now()}`;
    const now = new Date().toISOString();
    
    // Build the API configuration object
    const apiConfig = {
      id: apiId,
      api_name: apiDetails.apiName,
      api_code: apiDetails.apiCode,
      description: apiDetails.description,
      version: apiDetails.version,
      status: apiDetails.status,
      http_method: apiDetails.httpMethod,
      base_path: apiDetails.basePath,
      endpoint_path: apiDetails.endpointPath,
      category: apiDetails.category,
      owner: apiDetails.owner,
      tags: apiDetails.tags.join(','),
      protocol_type: protocolType,
      
      // Collection info
      collection_id: selectedCollection?.id || null,
      collection_name: selectedCollection?.name || null,
      collection_type: selectedCollection?.type || null,
      folder_id: selectedFolder?.id || null,
      folder_name: selectedFolder?.name || null,
      
      // Schema config
      schema_name: schemaConfig.schemaName,
      object_type: schemaConfig.objectType,
      object_name: schemaConfig.objectName,
      operation: schemaConfig.operation,
      primary_key_column: schemaConfig.primaryKeyColumn,
      sequence_name: schemaConfig.sequenceName,
      enable_pagination: schemaConfig.enablePagination ? 'Y' : 'N',
      page_size: schemaConfig.pageSize,
      enable_sorting: schemaConfig.enableSorting ? 'Y' : 'N',
      default_sort_column: schemaConfig.defaultSortColumn,
      default_sort_direction: schemaConfig.defaultSortDirection,
      
      // SOAP config
      soap_version: soapConfig.version,
      soap_binding_style: soapConfig.bindingStyle,
      soap_encoding_style: soapConfig.encodingStyle,
      soap_action: soapConfig.soapAction,
      soap_namespace: soapConfig.namespace,
      soap_service_name: soapConfig.serviceName,
      soap_port_name: soapConfig.portName,
      soap_use_async_pattern: soapConfig.useAsyncPattern ? 'Y' : 'N',
      soap_include_mtom: soapConfig.includeMtom ? 'Y' : 'N',
      soap_header_elements: soapConfig.soapHeaderElements.join(','),
      
      // GraphQL config
      graphql_operation_type: graphqlConfig.operationType,
      graphql_operation_name: graphqlConfig.operationName,
      graphql_schema: graphqlConfig.schema,
      graphql_enable_introspection: graphqlConfig.enableIntrospection ? 'Y' : 'N',
      graphql_enable_persisted_queries: graphqlConfig.enablePersistedQueries ? 'Y' : 'N',
      graphql_max_query_depth: graphqlConfig.maxQueryDepth,
      graphql_enable_batching: graphqlConfig.enableBatching ? 'Y' : 'N',
      graphql_subscriptions_enabled: graphqlConfig.subscriptionsEnabled ? 'Y' : 'N',
      graphql_custom_directives: graphqlConfig.customDirectives.join(','),
      
      // Source object info
      is_synonym: sourceObjectInfo.isSynonym ? 'Y' : 'N',
      synonym_target_type: sourceObjectInfo.targetType,
      synonym_target_name: sourceObjectInfo.targetName,
      synonym_target_owner: sourceObjectInfo.targetOwner,
      
      // Request body config
      request_body_type: requestBody.bodyType,
      request_body_sample: requestBody.sample,
      request_validate_schema: requestBody.validateSchema ? 'Y' : 'N',
      request_max_size: requestBody.maxSize,
      request_allowed_media_types: requestBody.allowedMediaTypes.join(','),
      request_required_fields: requestBody.requiredFields.join(','),
      
      // Response body config
      response_success_schema: responseBody.successSchema,
      response_error_schema: responseBody.errorSchema,
      response_include_metadata: responseBody.includeMetadata ? 'Y' : 'N',
      response_metadata_fields: responseBody.metadataFields.join(','),
      response_content_type: responseBody.contentType,
      response_compression: responseBody.compression,
      
      // Auth config
      auth_type: authConfig.authType,
      auth_api_key_header: authConfig.apiKeyHeader,
      auth_api_key_value: authConfig.apiKeyValue,
      auth_api_secret_header: authConfig.apiSecretHeader,
      auth_api_secret_value: authConfig.apiSecretValue,
      auth_jwt_token: authConfig.jwtToken,
      auth_jwt_issuer: authConfig.jwtIssuer,
      auth_basic_username: authConfig.basicUsername,
      auth_basic_password: authConfig.basicPassword,
      auth_ip_whitelist: authConfig.ipWhitelist,
      auth_rate_limit_requests: authConfig.rateLimitRequests,
      auth_rate_limit_period: authConfig.rateLimitPeriod,
      auth_enable_rate_limiting: authConfig.enableRateLimiting ? 'Y' : 'N',
      auth_cors_origins: authConfig.corsOrigins?.join(',') || '*',
      auth_audit_level: authConfig.auditLevel,
      
      // Settings
      settings_timeout: settings.timeout,
      settings_max_records: settings.maxRecords,
      settings_enable_logging: settings.enableLogging ? 'Y' : 'N',
      settings_log_level: settings.logLevel,
      settings_enable_caching: settings.enableCaching ? 'Y' : 'N',
      settings_cache_ttl: settings.cacheTtl,
      settings_generate_swagger: settings.generateSwagger ? 'Y' : 'N',
      settings_generate_postman: settings.generatePostman ? 'Y' : 'N',
      settings_generate_client_sdk: settings.generateClientSDK ? 'Y' : 'N',
      settings_enable_monitoring: settings.enableMonitoring ? 'Y' : 'N',
      settings_enable_alerts: settings.enableAlerts ? 'Y' : 'N',
      settings_alert_email: settings.alertEmail,
      settings_enable_tracing: settings.enableTracing ? 'Y' : 'N',
      settings_cors_enabled: settings.corsEnabled ? 'Y' : 'N',
      
      // Timestamps
      created_at: now,
      updated_at: now,
      created_by: authConfig.basicUsername || apiDetails.owner || 'SYSTEM',
      updated_by: authConfig.basicUsername || apiDetails.owner || 'SYSTEM',
      is_editing: isEditing ? 'Y' : 'N'
    };
    
    // Escape single quotes for SQL
    const escapeSql = (str) => {
      if (str === null || str === undefined) return 'NULL';
      if (typeof str === 'string') {
        // Handle empty strings
        if (str === '') return "''";
        // Escape single quotes by doubling them
        return "'" + str.replace(/'/g, "''") + "'";
      }
      return "'" + String(str).replace(/'/g, "''") + "'";
    };
    
    // Generate the main INSERT statement
    const columns = Object.keys(apiConfig);
    const values = columns.map(col => {
      const val = apiConfig[col];
      if (val === null || val === undefined) return 'NULL';
      if (typeof val === 'boolean') return val ? "'Y'" : "'N'";
      if (typeof val === 'number') return String(val);
      return escapeSql(val);
    });
    
    const insertStatement = `INSERT INTO api_configurations (${columns.join(', ')})
VALUES (${values.join(', ')});`;
    
    // Generate INSERT statements for parameters
    const parameterInserts = inParams.map((param, idx) => {
      const paramConfig = {
        id: `param-${apiId}-${idx}`,
        api_id: apiId,
        param_key: param.key,
        db_column: param.dbColumn,
        oracle_type: param.oracleType,
        api_type: param.apiType,
        parameter_location: param.parameterLocation,
        is_required: param.required ? 'Y' : 'N',
        description: param.description,
        example_value: param.example,
        validation_pattern: param.validationPattern,
        default_value: param.defaultValue,
        is_primary_key: param.isPrimaryKey ? 'Y' : 'N',
        param_mode: param.paramMode || 'IN',
        sort_order: idx,
        created_at: now
      };
      
      const paramColumns = Object.keys(paramConfig);
      const paramValues = paramColumns.map(col => {
        const val = paramConfig[col];
        if (val === null || val === undefined) return 'NULL';
        if (typeof val === 'boolean') return val ? "'Y'" : "'N'";
        if (typeof val === 'number') return String(val);
        return escapeSql(val);
      });
      
      return `INSERT INTO api_parameters (${paramColumns.join(', ')})
VALUES (${paramValues.join(', ')});`;
    });
    
    // Generate INSERT statements for response mappings
    const mappingInserts = outMappings.map((mapping, idx) => {
      const mappingConfig = {
        id: `mapping-${apiId}-${idx}`,
        api_id: apiId,
        api_field: mapping.apiField,
        db_column: mapping.dbColumn,
        oracle_type: mapping.oracleType,
        api_type: mapping.apiType,
        format_pattern: mapping.format,
        is_nullable: mapping.nullable ? 'Y' : 'N',
        is_primary_key: mapping.isPrimaryKey ? 'Y' : 'N',
        include_in_response: mapping.includeInResponse !== false ? 'Y' : 'N',
        param_mode: mapping.paramMode || 'OUT',
        sort_order: idx,
        created_at: now
      };
      
      const mappingColumns = Object.keys(mappingConfig);
      const mappingValues = mappingColumns.map(col => {
        const val = mappingConfig[col];
        if (val === null || val === undefined) return 'NULL';
        if (typeof val === 'boolean') return val ? "'Y'" : "'N'";
        if (typeof val === 'number') return String(val);
        return escapeSql(val);
      });
      
      return `INSERT INTO api_response_mappings (${mappingColumns.join(', ')})
VALUES (${mappingValues.join(', ')});`;
    });
    
    // Generate INSERT statements for headers
    const headerInserts = headers.map((header, idx) => {
      if (!header.key) return null;
      
      const headerConfig = {
        id: `header-${apiId}-${idx}`,
        api_id: apiId,
        header_key: header.key,
        header_value: header.value,
        is_required: header.required ? 'Y' : 'N',
        description: header.description,
        sort_order: idx,
        created_at: now
      };
      
      const headerColumns = Object.keys(headerConfig);
      const headerValues = headerColumns.map(col => {
        const val = headerConfig[col];
        if (val === null || val === undefined) return 'NULL';
        if (typeof val === 'boolean') return val ? "'Y'" : "'N'";
        return escapeSql(val);
      });
      
      return `INSERT INTO api_headers (${headerColumns.join(', ')})
VALUES (${headerValues.join(', ')});`;
    }).filter(Boolean);
    
    // Generate INSERT statements for tests
    const testsConfig = {
      id: `tests-${apiId}`,
      api_id: apiId,
      test_connection: tests.testConnection ? 'Y' : 'N',
      test_object_access: tests.testObjectAccess ? 'Y' : 'N',
      test_privileges: tests.testPrivileges ? 'Y' : 'N',
      test_data_types: tests.testDataTypes ? 'Y' : 'N',
      test_null_constraints: tests.testNullConstraints ? 'Y' : 'N',
      test_unique_constraints: tests.testUniqueConstraints ? 'Y' : 'N',
      test_foreign_key_refs: tests.testForeignKeyReferences ? 'Y' : 'N',
      test_query_performance: tests.testQueryPerformance ? 'Y' : 'N',
      performance_threshold: tests.performanceThreshold,
      test_with_sample_data: tests.testWithSampleData ? 'Y' : 'N',
      sample_data_rows: tests.sampleDataRows,
      test_procedure_execution: tests.testProcedureExecution ? 'Y' : 'N',
      test_function_return: tests.testFunctionReturn ? 'Y' : 'N',
      test_exception_handling: tests.testExceptionHandling ? 'Y' : 'N',
      test_sql_injection: tests.testSQLInjection ? 'Y' : 'N',
      test_authentication: tests.testAuthentication ? 'Y' : 'N',
      test_authorization: tests.testAuthorization ? 'Y' : 'N',
      test_data_json: tests.testData ? JSON.stringify(tests.testData) : null,
      test_queries_json: tests.testQueries ? JSON.stringify(tests.testQueries) : null,
      created_at: now
    };
    
    const testsColumns = Object.keys(testsConfig);
    const testsValues = testsColumns.map(col => {
      const val = testsConfig[col];
      if (val === null || val === undefined) return 'NULL';
      if (typeof val === 'boolean') return val ? "'Y'" : "'N'";
      if (typeof val === 'number') return String(val);
      return escapeSql(val);
    });
    
    const testsInsert = `INSERT INTO api_tests_config (${testsColumns.join(', ')})
VALUES (${testsValues.join(', ')});`;
    
    // Generate the complete SQL script
    let sql = `-- ============================================================================
-- API Configuration INSERT Statements
-- Generated: ${new Date().toISOString()}
-- API Name: ${apiDetails.apiName}
-- API Code: ${apiDetails.apiCode}
-- Protocol: ${protocolType.toUpperCase()}
-- ============================================================================

-- 1. Insert the main API configuration
-- ============================================================================
${insertStatement}

-- 2. Insert API Parameters (IN/IN OUT parameters)
-- ============================================================================
${parameterInserts.length > 0 ? parameterInserts.join('\n\n') : '-- No parameters defined'}

-- 3. Insert API Response Mappings (OUT/IN OUT parameters and response fields)
-- ============================================================================
${mappingInserts.length > 0 ? mappingInserts.join('\n\n') : '-- No response mappings defined'}

-- 4. Insert API Headers
-- ============================================================================
${headerInserts.length > 0 ? headerInserts.join('\n\n') : '-- No headers defined'}

-- 5. Insert API Tests Configuration
-- ============================================================================
${testsInsert}

-- ============================================================================
-- COMMIT to save all changes
-- ============================================================================
COMMIT;

-- ============================================================================
-- Verification Queries
-- ============================================================================
-- View the inserted API configuration:
-- SELECT * FROM api_configurations WHERE id = '${apiId}';

-- View the parameters for this API:
-- SELECT * FROM api_parameters WHERE api_id = '${apiId}' ORDER BY sort_order;

-- View the response mappings for this API:
-- SELECT * FROM api_response_mappings WHERE api_id = '${apiId}' ORDER BY sort_order;

-- View the headers for this API:
-- SELECT * FROM api_headers WHERE api_id = '${apiId}' ORDER BY sort_order;

-- View the tests configuration for this API:
-- SELECT * FROM api_tests_config WHERE api_id = '${apiId}';
`;

    // Add a note about required tables
    sql += `

-- ============================================================================
-- NOTES:
-- ============================================================================
-- This script assumes the following tables exist:
--   1. api_configurations - Main API configuration table
--   2. api_parameters - API parameters table
--   3. api_response_mappings - API response mappings table
--   4. api_headers - API headers table
--   5. api_tests_config - API tests configuration table
--
-- If these tables don't exist, create them using the schema definition:
-- 
-- CREATE TABLE api_configurations (
--   id VARCHAR2(100) PRIMARY KEY,
--   api_name VARCHAR2(200) NOT NULL,
--   api_code VARCHAR2(100) NOT NULL UNIQUE,
--   description CLOB,
--   version VARCHAR2(20),
--   status VARCHAR2(20),
--   http_method VARCHAR2(10),
--   base_path VARCHAR2(200),
--   endpoint_path VARCHAR2(200),
--   category VARCHAR2(50),
--   owner VARCHAR2(100),
--   tags VARCHAR2(500),
--   protocol_type VARCHAR2(20),
--   -- ... (other columns from above)
--   created_at TIMESTAMP,
--   updated_at TIMESTAMP,
--   created_by VARCHAR2(100),
--   updated_by VARCHAR2(100)
-- );
-- ============================================================================
`;

    return sql;
  };

  // Update the useEffect that generates preview code
  useEffect(() => {
    switch(previewMode) {
      case 'plsql':
        setGeneratedCode(generatePLSQLCode());
        break;
      case 'soap':
        setGeneratedCode(generateSOAPEnvelope());
        break;
      case 'graphql':
        setGeneratedCode(generateGraphQLSchema());
        break;
      case 'openapi':
        setGeneratedCode(generateOpenAPISpec());
        break;
      case 'postman':
        setGeneratedCode(generatePostmanCollection());
        break;
      case 'sql_insert':
        setGeneratedCode(generateSQLInsertStatements());
        break;
      default:
        setGeneratedCode(JSON.stringify({
          apiDetails,
          schemaConfig,
          protocolType,
          soapConfig: protocolType === 'soap' ? soapConfig : null,
          graphqlConfig: protocolType === 'graphql' ? graphqlConfig : null,
          collectionInfo: {
            collectionId: selectedCollection?.id,
            collectionName: selectedCollection?.name,
            collectionType: selectedCollection?.type,
            folderId: selectedFolder?.id,
            folderName: selectedFolder?.name
          },
          // Only include IN parameters in the config
          parameters: getInParameters().map(p => ({
            key: p.key,
            dbColumn: p.dbColumn,
            type: p.apiType,
            oracleType: p.oracleType,
            required: p.required,
            parameterLocation: p.parameterLocation,
            description: p.description,
            example: p.example,
            defaultValue: p.defaultValue,
            isPrimaryKey: p.isPrimaryKey,
            paramMode: p.paramMode
          })),
          // Include all response mappings (including OUT parameters)
          responseMappings: getOutMappings().map(m => ({
            apiField: m.apiField,
            dbColumn: m.dbColumn,
            oracleType: m.oracleType,
            apiType: m.apiType,
            nullable: m.nullable,
            isPrimaryKey: m.isPrimaryKey,
            paramMode: m.paramMode
          })),
          requestBody,
          responseBody,
          authConfig,
          settings,
          sourceObjectInfo,
          tests,
          validation: validationResult,
          isEditing
        }, null, 2));
    }
  }, [previewMode, apiDetails, schemaConfig, parameters, responseMappings, requestBody, responseBody, authConfig, settings, sourceObjectInfo, tests, validationResult, selectedCollection, selectedFolder, isEditing, protocolType, soapConfig, graphqlConfig]);

  // Handle confirmation modal close
  const handleConfirmationClose = () => {
    setConfirmationOpen(false);
    
    // Also refresh when closing the success modal (for extra safety)
    if (onGenerateAPI && apiResponse?.responseCode >= 200 && apiResponse?.responseCode < 300) {
      console.log('🔄 Triggering dashboard refresh on confirmation close');
      onGenerateAPI();
    }
    
    onClose(); // Close the generation modal too
  };

  // Copy generated code
  const copyGeneratedCode = () => {
    try {
      // Get the code content
      const codeContent = generatedCode;
      
      // Create a temporary textarea element
      const textarea = document.createElement('textarea');
      textarea.value = codeContent;
      textarea.style.position = 'fixed';
      textarea.style.left = '-999999px';
      textarea.style.top = '-999999px';
      document.body.appendChild(textarea);
      
      // Select and copy the text
      textarea.select();
      textarea.setSelectionRange(0, codeContent.length);
      
      // Execute copy command
      const successful = document.execCommand('copy');
      
      // Remove the textarea
      document.body.removeChild(textarea);
      
      // Show a temporary notification
      if (successful) {
        // Create notification element
        const notification = document.createElement('div');
        notification.textContent = '✓ Copied to clipboard!';
        notification.style.position = 'fixed';
        notification.style.bottom = '20px';
        notification.style.right = '20px';
        notification.style.padding = '8px 16px';
        notification.style.backgroundColor = themeColors.success;
        notification.style.color = '#ffffff';
        notification.style.borderRadius = '8px';
        notification.style.fontSize = '12px';
        notification.style.zIndex = '10000';
        notification.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
        
        document.body.appendChild(notification);
        
        // Remove notification after 2 seconds
        setTimeout(() => {
          notification.remove();
        }, 2000);
      } else {
        // Fallback for browsers that don't support execCommand
        navigator.clipboard.writeText(codeContent).catch(err => {
          console.error('Failed to copy: ', err);
          alert('Failed to copy to clipboard. Please select and copy manually.');
        });
      }
    } catch (err) {
      console.error('Copy failed: ', err);
      // Fallback using modern clipboard API
      navigator.clipboard.writeText(generatedCode).catch(() => {
        alert('Failed to copy to clipboard. Please select and copy manually.');
      });
    }
  };

  // Download generated code
  const downloadGeneratedCode = () => {
    let extension = 'json';
    let filename = '';
    
    switch(previewMode) {
      case 'plsql':
        extension = 'sql';
        filename = `${schemaConfig.schemaName}_${apiDetails.apiCode}_PKG.sql`;
        break;
      case 'soap':
        extension = 'xml';
        filename = `${apiDetails.apiCode}_soap_envelope.xml`;
        break;
      case 'graphql':
        extension = 'graphql';
        filename = `${apiDetails.apiCode}_schema.graphql`;
        break;
      case 'openapi':
        extension = 'json';
        filename = `${apiDetails.apiCode}_openapi.json`;
        break;
      case 'postman':
        extension = 'json';
        filename = `${apiDetails.apiCode}_postman.json`;
        break;
      case 'sql_insert':
        extension = 'sql';
        filename = `${apiDetails.apiCode}_insert_statements.sql`;
        break;
      default:
        extension = 'json';
        filename = `${apiDetails.apiCode}_configuration.json`;
    }
    
    const blob = new Blob([generatedCode], { type: extension === 'sql' ? 'text/plain' : extension === 'xml' ? 'application/xml' : extension === 'graphql' ? 'text/plain' : 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
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
    white: '#ffffff',
    objectType: {
      table: theme === 'dark' ? 'rgb(96 165 250)' : '#3b82f6',
      view: theme === 'dark' ? 'rgb(52 211 153)' : '#10b981',
      procedure: theme === 'dark' ? 'rgb(167 139 250)' : '#8b5cf6',
      function: theme === 'dark' ? 'rgb(251 191 36)' : '#f59e0b',
      package: theme === 'dark' ? 'rgb(148 163 184)' : '#6b7280',
      sequence: theme === 'dark' ? 'rgb(100 116 139)' : '#64748b',
      synonym: theme === 'dark' ? 'rgb(34 211 238)' : '#06b6d4',
      type: theme === 'dark' ? 'rgb(139 92 246)' : '#6366f1',
      trigger: theme === 'dark' ? 'rgb(244 114 182)' : '#ec4899'
    }
  };

  // Tab definitions - updated with SOAP and GraphQL tabs
  const tabs = [
    { id: 'definition', label: 'Definition', icon: <FileText className="h-4 w-4" /> },
    { id: 'schema', label: 'Schema', icon: <Database className="h-4 w-4" /> },
    { id: 'parameters', label: 'Parameters', icon: <Hash className="h-4 w-4" /> },
    { id: 'mapping', label: 'Mapping', icon: <Map className="h-4 w-4" /> },
    ...(protocolType === 'soap' ? [{ id: 'soap', label: 'SOAP Config', icon: <Send className="h-4 w-4" /> }] : []),
    ...(protocolType === 'graphql' ? [{ id: 'graphql', label: 'GraphQL Config', icon: <GitMerge className="h-4 w-4" /> }] : []),
    { id: 'auth', label: 'Authentication', icon: <Lock className="h-4 w-4" /> },
    { id: 'request', label: 'Request', icon: <Upload className="h-4 w-4" /> },
    { id: 'response', label: 'Response', icon: <Download className="h-4 w-4" /> },
    { id: 'tests', label: 'Database Tests', icon: <Database className="h-4 w-4" /> },
    { id: 'settings', label: 'Settings', icon: <Settings className="h-4 w-4" /> },
    { id: 'preview', label: 'Preview', icon: <Eye className="h-4 w-4" /> },
  ];

  // Render input field with required indicator
  const renderRequiredInput = (field, label, value, onChange, placeholder, type = 'text', disabled = false) => {
    const isRequired = isFieldRequired(field);
    const hasError = validationErrors[field];
    
    return (
      <div className="space-y-2">
        <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
          {label}
          {isRequired && <Asterisk className="h-3 w-3" style={{ color: themeColors.error }} />}
        </label>
        <input
          type={type}
          value={value}
          onChange={(e) => onChange(field, e.target.value)}
          className={`w-full px-3 py-2 border rounded-lg text-xs hover-lift ${hasError ? 'border-red-500' : ''}`}
          style={{ 
            backgroundColor: themeColors.card,
            borderColor: hasError ? themeColors.error : (value && isRequired ? themeColors.success : themeColors.border),
            color: themeColors.text
          }}
          placeholder={placeholder}
          disabled={disabled || loading || validating}
        />
        {hasError && (
          <p className="text-xs mt-1" style={{ color: themeColors.error }}>
            {hasError}
          </p>
        )}
      </div>
    );
  };

  if (!isOpen) return null;

  // For SSR compatibility, return null if not in browser
  if (typeof window === 'undefined') {
    return null;
  }

  if (!mounted) return null;

  return ReactDOM.createPortal(
    <>
      {/* Main Modal */}
      <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-[1500] p-4">
        <div className="rounded-xl shadow-2xl w-5xl max-w-5xl max-h-[90vh] overflow-y-scroll flex flex-col" style={{ 
          backgroundColor: themeColors.bg,
          border: `1px solid ${themeColors.modalBorder}`
        }}>
          {/* Header - MODIFIED to show loading state and protocol selector */}
          <div className="px-6 py-4 border-b flex items-center justify-between" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.card
          }}>
            <div className="flex items-center gap-3">
              {selectedObject?.loading ? (
                // Show loading state in header
                <div className="p-2 rounded-lg animate-pulse" style={{ backgroundColor: themeColors.info + '40' }}>
                  <Loader className="h-6 w-6 animate-spin" style={{ color: themeColors.info }} />
                </div>
              ) : isEditing ? (
                <Edit className="h-6 w-6" style={{ color: themeColors.warning }} />
              ) : fromDashboard && !selectedDbObject ? (
                <Search className="h-6 w-6" style={{ color: themeColors.info }} />
              ) : selectedObject?.type === 'TABLE' ? (
                <Database className="h-6 w-6" style={{ color: themeColors.info }} />
              ) : selectedObject?.type === 'VIEW' ? (
                <FileText className="h-6 w-6" style={{ color: themeColors.success }} />
              ) : selectedObject?.type === 'PROCEDURE' ? (
                <Terminal className="h-6 w-6" style={{ color: themeColors.info }} />
              ) : selectedObject?.type === 'FUNCTION' ? (
                <Code className="h-6 w-6" style={{ color: themeColors.warning }} />
              ) : selectedObject?.type === 'PACKAGE' ? (
                <Package className="h-6 w-6" style={{ color: themeColors.textSecondary }} />
              ) : selectedObject?.type === 'SYNONYM' ? (
                <Link className="h-6 w-6" style={{ color: themeColors.info }} />
              ) : (
                <Globe className="h-6 w-6" style={{ color: themeColors.info }} />
              )}
              
              <div>
                <h2 className="text-lg font-bold" style={{ color: themeColors.text }}>
                  {selectedObject?.loading ? 'Loading API Details...' : 
                   isEditing ? 'Edit API' : 
                   fromDashboard && !selectedDbObject ? 'Generate API - Select Object' :
                   fromDashboard && selectedDbObject ? 'Generate API from Selected Object' :
                   'Generate API'} 
                  {!selectedObject?.loading && selectedObject?.name && !isEditing && !fromDashboard ? 
                    ' from ' + (selectedObject?.type || 'Object') + ': ' + selectedObject?.name : ''}
                </h2>
                
                {selectedObject?.loading ? (
                  <p className="text-xs flex items-center gap-2 mt-1" style={{ color: themeColors.info }}>
                    <Loader className="h-3 w-3 animate-spin" />
                    Fetching API details from server...
                  </p>
                ) : selectedObject?.error ? (
                  <p className="text-xs flex items-center gap-1 mt-1" style={{ color: themeColors.error }}>
                    <AlertCircle className="h-3 w-3" />
                    Error loading API details: {selectedObject.message}
                  </p>
                ) : selectedObject?.name && !isEditing && (
                  <>
                    <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                      {selectedObject?.owner}.{selectedObject?.name} • {selectedObject?.type}
                      {sourceObjectInfo.isSynonym && (
                        <span className="ml-1 text-yellow-500">
                          (points to {sourceObjectInfo.targetType}: {sourceObjectInfo.targetOwner}.{sourceObjectInfo.targetName})
                        </span>
                      )}
                    </p>
                    {validating && (
                      <p className="text-xs mt-1 flex items-center gap-1" style={{ color: themeColors.warning }}>
                        <Loader className="h-3 w-3 animate-spin" />
                        Validating source object...
                      </p>
                    )}
                  </>
                )}

                {/* Dashboard selected object indicator - UPDATED with database type badge */}
                {fromDashboard && selectedDbObject && (
                  <div className="flex items-center gap-2 mt-1">
                    <p className="text-xs" style={{ color: themeColors.success }}>
                      <CheckCircle className="h-3 w-3 inline mr-1" />
                      Selected: {selectedDbObject.owner}.{selectedDbObject.name} ({selectedDbObject.type})
                    </p>
                    {/* Database Type Badge - FIXED to show currentDatabaseType */}
                    <span className="text-xs px-2 py-0.5 rounded-full" style={{ 
                      backgroundColor: currentDatabaseType === 'postgresql' ? '#3b82f620' : '#ef444420',
                      color: currentDatabaseType === 'postgresql' ? '#3b82f6' : '#ef4444'
                    }}>
                      {currentDatabaseType === 'postgresql' ? 'PostgreSQL' : 'Oracle'}
                    </span>
                    <button
                      onClick={() => setShowObjectSelector(true)}
                      className="text-xs underline hover:no-underline"
                      style={{ color: themeColors.info }}
                    >
                      Change object
                    </button>
                  </div>
                )}

                {/* UPDATED: API Code Already Exists Warning */}
                {!isEditing && apiCodeExists && (
                  <div className="mt-2 p-2 rounded-lg border flex items-center gap-2" style={{ 
                    backgroundColor: themeColors.error + '20',
                    borderColor: themeColors.error,
                  }}>
                    <AlertCircle className="h-4 w-4 flex-shrink-0" style={{ color: themeColors.error }} />
                    <div>
                      <p className="text-xs font-medium" style={{ color: themeColors.error }}>
                        ⚠️ API Already Exists
                      </p>
                      <p className="text-xs mt-0.5" style={{ color: themeColors.textSecondary }}>
                        An API with code "{apiDetails.apiCode}" already exists. 
                        You must choose a different API code to continue.
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </div>
            
            <div className="flex items-center gap-2">
              <button
                onClick={onClose}
                className="p-2 rounded-lg transition-colors hover-lift"
                style={{ backgroundColor: themeColors.hover, color: themeColors.textSecondary }}
                disabled={loading || validating}
              >
                <X className="h-5 w-5" />
              </button>
            </div>
          </div>

          {/* Protocol Selector Section - NEW */}
          <div className="px-6 py-4 border-b" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.modalBg
          }}>
            <div className="space-y-2">
              <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                <Globe className="h-4 w-4" />
                API Protocol Type
              </label>
              <div className="flex gap-4">
                {PROTOCOL_TYPES.map(protocol => (
                  <button
                    key={protocol.value}
                    onClick={() => handleProtocolChange(protocol.value)}
                    className={`flex-1 px-4 py-3 rounded-lg border-2 transition-all hover-lift ${
                      protocolType === protocol.value ? 'ring-2 ring-offset-2' : ''
                    }`}
                    style={{
                      backgroundColor: protocolType === protocol.value ? themeColors.info + '20' : themeColors.card,
                      borderColor: protocolType === protocol.value ? themeColors.info : themeColors.border,
                      color: themeColors.text
                    }}
                  >
                    <div className="flex items-center justify-center gap-2">
                      {protocol.icon}
                      <span className="text-sm font-medium">{protocol.label}</span>
                    </div>
                    <p className="text-xs mt-1 text-center" style={{ color: themeColors.textSecondary }}>
                      {protocol.description}
                    </p>
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Collection & Folder Selection Section */}
          <div className="px-6 py-4 border-b" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.modalBg
          }}>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Collection Selection */}
              <div className="space-y-2">
                <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                  <Layers className="h-4 w-4" />
                  API Collection *
                  <Asterisk className="h-3 w-3" style={{ color: themeColors.error }} />
                </label>
                <div className="flex gap-2">
                  <select
                    value={selectedCollection?.id || ''}
                    onChange={(e) => handleCollectionChange(e.target.value)}
                    className={`flex-1 px-3 py-2 border rounded-lg text-xs hover-lift ${validationErrors.collection ? 'border-red-500' : ''}`}
                    style={{ 
                      backgroundColor: themeColors.bg,
                      borderColor: validationErrors.collection ? themeColors.error : (selectedCollection ? themeColors.success : themeColors.border),
                      color: themeColors.text
                    }}
                  >
                    <option value="">Select a collection</option>
                    {collections.map(collection => (
                      <option key={collection.id} value={collection.id}>
                        {collection.name}
                      </option>
                    ))}
                    <option value="new" style={{ color: themeColors.success }}>➕ Add New Collection...</option>
                  </select>
                  
                  {isAddingNewCollection && (
                    <button
                      onClick={() => setIsAddingNewCollection(false)}
                      className="px-2 py-2 border rounded-lg transition-colors hover-lift"
                      style={{ 
                        backgroundColor: themeColors.error + '20',
                        borderColor: themeColors.error,
                        color: themeColors.error
                      }}
                      title="Cancel adding new collection"
                    >
                      <X className="h-4 w-4" />
                    </button>
                  )}
                </div>
                {validationErrors.collection && (
                  <p className="text-xs mt-1" style={{ color: themeColors.error }}>
                    {validationErrors.collection}
                  </p>
                )}

                {/* New Collection Input */}
                {isAddingNewCollection && (
                  <div className="mt-3 p-3 rounded-lg border space-y-3" style={{ 
                    borderColor: themeColors.success,
                    backgroundColor: themeColors.success + '10'
                  }}>
                    <div className="space-y-2">
                      <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                        Collection Name *
                        <Asterisk className="h-3 w-3" style={{ color: themeColors.error }} />
                      </label>
                      <input
                        type="text"
                        value={newCollectionName}
                        onChange={(e) => setNewCollectionName(e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                        style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: newCollectionName.trim() ? themeColors.success : themeColors.border,
                          color: themeColors.text
                        }}
                        placeholder="e.g., Core Banking API Collection"
                        autoFocus
                      />
                    </div>
                    
                    <div className="space-y-2">
                      <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                        Collection Type
                      </label>
                      <select
                        value={newCollectionType}
                        onChange={(e) => setNewCollectionType(e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                        style={{ 
                          backgroundColor: themeColors.bg,
                          borderColor: themeColors.border,
                          color: themeColors.text
                        }}
                      >
                        <option value="core">Core Banking</option>
                        <option value="channel">Customer Channel</option>
                        <option value="payment">Payment Gateway</option>
                        <option value="thirdparty">Third Party</option>
                        <option value="admin">Administrative</option>
                        <option value="misc">Miscellaneous</option>
                      </select>
                    </div>
                    
                    <div className="space-y-2">
                      <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                        Description
                      </label>
                      <textarea
                        value={newCollectionDescription}
                        onChange={(e) => setNewCollectionDescription(e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                        style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: themeColors.border,
                          color: themeColors.text
                        }}
                        rows="2"
                        placeholder="Describe the purpose of this collection..."
                      />
                    </div>
                    
                    <button
                      onClick={handleAddNewCollection}
                      disabled={!newCollectionName.trim()}
                      className="w-full px-3 py-2 rounded-lg flex items-center justify-center gap-2 text-xs transition-colors hover-lift"
                      style={{ 
                        backgroundColor: newCollectionName.trim() ? themeColors.success : themeColors.textSecondary,
                        color: themeColors.white,
                        opacity: newCollectionName.trim() ? 1 : 0.5,
                        cursor: newCollectionName.trim() ? 'pointer' : 'not-allowed'
                      }}
                    >
                      <Check className="h-4 w-4" />
                      Create Collection
                    </button>
                  </div>
                )}
              </div>

              {/* Folder Selection */}
              <div className="space-y-2">
                <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                  <Folder className="h-4 w-4" />
                  API Folder *
                  <Asterisk className="h-3 w-3" style={{ color: themeColors.error }} />
                </label>
                <div className="flex gap-2">
                  <select
                    value={selectedFolder?.id || ''}
                    onChange={(e) => handleFolderChange(e.target.value)}
                    className={`flex-1 px-3 py-2 border rounded-lg text-xs hover-lift ${validationErrors.folder ? 'border-red-500' : ''}`}
                    style={{ 
                      backgroundColor: themeColors.bg,
                      borderColor: validationErrors.folder ? themeColors.error : (selectedFolder ? themeColors.success : themeColors.border),
                      color: themeColors.text
                    }}
                    disabled={!selectedCollection && !isAddingNewCollection}
                  >
                    <option value="">{selectedCollection ? 'Select a folder' : 'Select a collection first'}</option>
                    {folders.map(folder => (
                      <option key={folder.id} value={folder.id}>
                        📁 {folder.name}
                      </option>
                    ))}
                    {selectedCollection && (
                      <option value="new" style={{ color: themeColors.success }}>➕ Add New Folder...</option>
                    )}
                  </select>
                  
                  {selectedFolder && (
                    <button
                      onClick={() => setSelectedFolder(null)}
                      className="px-2 py-2 border rounded-lg transition-colors hover-lift"
                      style={{ 
                        backgroundColor: themeColors.hover,
                        borderColor: themeColors.border,
                        color: themeColors.textSecondary
                      }}
                      title="Clear folder selection"
                    >
                      <X className="h-4 w-4" />
                    </button>
                  )}
                </div>
                {validationErrors.folder && (
                  <p className="text-xs mt-1" style={{ color: themeColors.error }}>
                    {validationErrors.folder}
                  </p>
                )}
              </div>
            </div>
          </div>

          {/* API Details Section */}
          <div className="px-6 py-4 border-b" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.modalBg
          }}>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              {renderRequiredInput(
                'apiName',
                'API Name',
                apiDetails.apiName,
                handleApiDetailChange,
                'Users API'
              )}

              {renderRequiredInput(
                'apiCode',
                'API Code',
                apiDetails.apiCode,
                handleApiDetailChange,
                'GET_USERS',
                'text',
                isEditing
              )}

              {/* Add this right after the API Code input field */}
              {/* {!isEditing && apiCodeExists && (
                <div className="mt-2 p-2 rounded-lg border flex items-center gap-2" style={{ 
                  backgroundColor: themeColors.error + '20',
                  borderColor: themeColors.error,
                }}>
                  <AlertCircle className="h-4 w-4 flex-shrink-0" style={{ color: themeColors.error }} />
                  <div className="flex-1">
                    <p className="text-xs font-medium" style={{ color: themeColors.error }}>
                      ⚠️ API Code Already Exists
                    </p>
                    <p className="text-xs mt-0.5" style={{ color: themeColors.textSecondary }}>
                      An API with code "{apiDetails.apiCode}" already exists. 
                      You must choose a different API code to continue.
                    </p>
                  </div>
                </div>
              )} */}

              {/* Only show HTTP Method for REST protocol */}
              {protocolType === 'rest' && (
                <div className="space-y-2">
                  <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                    HTTP Method
                  </label>
                  <select
                    value={apiDetails.httpMethod}
                    onChange={(e) => handleApiDetailChange('httpMethod', e.target.value)}
                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                    style={{ 
                      backgroundColor: themeColors.bg,
                      borderColor: themeColors.border,
                      color: themeColors.text
                    }}
                    disabled={loading || validating}
                  >
                    {HTTP_METHODS.map(method => (
                      <option key={method} value={method}>{method}</option>
                    ))}
                  </select>
                </div>
              )}

              {/* For SOAP and GraphQL, show a disabled POST method indicator */}
              {(protocolType === 'soap' || protocolType === 'graphql') && (
                <div className="space-y-2">
                  <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                    HTTP Method
                  </label>
                  <div className="w-full px-3 py-2 border rounded-lg text-xs" style={{ 
                    backgroundColor: themeColors.bg,
                    borderColor: themeColors.border,
                    color: themeColors.text,
                    opacity: 0.7
                  }}>
                    POST (required for {protocolType === 'soap' ? 'SOAP' : 'GraphQL'})
                  </div>
                </div>
              )}

              {/* For SOAP, show SOAP version instead */}
              {protocolType === 'soap' && (
                <div className="space-y-2">
                  <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                    SOAP Version
                  </label>
                  <select
                    value={soapConfig.version}
                    onChange={(e) => handleSoapConfigChange('version', e.target.value)}
                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                    style={{ 
                      backgroundColor: themeColors.bg,
                      borderColor: themeColors.border,
                      color: themeColors.text
                    }}
                  >
                    {SOAP_VERSIONS.map(v => (
                      <option key={v.value} value={v.value}>{v.label}</option>
                    ))}
                  </select>
                </div>
              )}

              {/* For GraphQL, show operation type */}
              {protocolType === 'graphql' && (
                <div className="space-y-2">
                  <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                    Operation Type
                  </label>
                  <select
                    value={graphqlConfig.operationType}
                    onChange={(e) => handleGraphqlConfigChange('operationType', e.target.value)}
                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                    style={{ 
                      backgroundColor: themeColors.bg,
                      borderColor: themeColors.border,
                      color: themeColors.text
                    }}
                  >
                    {GRAPHQL_OPERATION_TYPES.map(op => (
                      <option key={op.value} value={op.value}>{op.label}</option>
                    ))}
                  </select>
                </div>
              )}

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Status
                </label>
                <select
                  value={apiDetails.status}
                  onChange={(e) => handleApiDetailChange('status', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift uppercase"
                  style={{ 
                    backgroundColor: themeColors.bg,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  disabled={loading || validating}
                >
                  <option value="ACTIVE" style={{ color: 'green' }}>Active</option>
                  <option value="DRAFT" style={{ color: 'blue' }}>Draft</option>
                  <option value="DEPRECATED" style={{ color: 'goldenrod' }}>Deprecated</option>
                  <option value="ARCHIVED" style={{ color: 'gray' }}>Archived</option>
                  <option value="INACTIVE" style={{ color: 'red' }}>Inactive</option>
                  <option value="PENDING" style={{ color: 'orange' }}>Pending</option>
                  <option value="SUSPENDED" style={{ color: 'purple' }}>Suspended</option>
                </select>
              </div>

              {/* Base Path only for REST */}
              {protocolType === 'rest' && (
                <div className="space-y-2">
                  <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                    Base Path
                  </label>
                  <input
                    type="text"
                    value={apiDetails.basePath}
                    onChange={(e) => handleApiDetailChange('basePath', e.target.value)}
                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                    style={{ 
                      backgroundColor: themeColors.card,
                      borderColor: themeColors.border,
                      color: themeColors.text
                    }}
                    placeholder="/api/v1"
                    disabled={loading || validating}
                  />
                </div>
              )}

              {/* Endpoint Path only for REST */}
              {protocolType === 'rest' && renderRequiredInput(
                'endpointPath',
                'Endpoint Path',
                apiDetails.endpointPath,
                (field, value) => handleApiDetailChange(field, value),
                '/users'
              )}

              {/* For SOAP, show SOAP Action */}
              {/* Dynamic SOAP Action based on database object type */}
              {protocolType === 'soap' && (
                <div className="space-y-2">
                  <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                    SOAP Action (Operation) *
                    <Asterisk className="h-3 w-3" style={{ color: themeColors.error }} />
                  </label>
                  <select
                    value={soapConfig.soapAction}
                    onChange={(e) => handleSoapConfigChange('soapAction', e.target.value)}
                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                    style={{ 
                      backgroundColor: themeColors.bg,
                      borderColor: validationErrors.soapAction ? themeColors.error : themeColors.border,
                      color: themeColors.text
                    }}
                  >
                    {/* Options based on object type */}
                    {schemaConfig.objectType === 'TABLE' && (
                      <>
                        <option value="" disabled selected>--- Select SOAP Action ---</option>
                        <option value="SELECT">SELECT - Query records from table</option>
                        <option value="INSERT">INSERT - Create new record in table</option>
                        <option value="UPDATE">UPDATE - Modify existing record</option>
                        <option value="DELETE">DELETE - Remove record from table</option>
                        <option value="UPSERT">UPSERT - Insert or update record</option>
                        <option value="BULK_INSERT">BULK_INSERT - Insert multiple records</option>
                        <option value="BULK_UPDATE">BULK_UPDATE - Update multiple records</option>
                        <option value="SEARCH">SEARCH - Advanced search with filters</option>
                        <option value="COUNT">COUNT - Get record count</option>
                        <option value="EXISTS">EXISTS - Check if record exists</option>
                      </>
                    )}
                    
                    {schemaConfig.objectType === 'VIEW' && (
                <>
                  <option value="" disabled selected>--- Select SOAP Action ---</option>
                  <option value="SELECT">SELECT - Query records from view</option>
                  <option value="SELECT_ONE">SELECT_ONE - Get single record</option>
                  <option value="SEARCH">SEARCH - Search view with filters</option>
                  <option value="COUNT">COUNT - Get record count</option>
                  <option value="AGGREGATE">AGGREGATE - Get aggregations (SUM/AVG/COUNT)</option>
                  <option value="EXPORT">EXPORT - Export view data</option>
                  <option value="PAGINATE">PAGINATE - Get paginated results</option>
                </>
              )}
              
              {schemaConfig.objectType === 'PROCEDURE' && (
                <>
                  <option value="" disabled selected>--- Select SOAP Action ---</option>
                  <option value="EXECUTE">EXECUTE - Execute stored procedure</option>
                  <option value="VALIDATE">VALIDATE - Validate parameters only</option>
                  <option value="DRY_RUN">DRY_RUN - Preview what would happen</option>
                  <option value="SCHEDULE">SCHEDULE - Schedule for later execution</option>
                  <option value="ASYNC">ASYNC - Execute asynchronously</option>
                  <option value="DEBUG">DEBUG - Execute with debug output</option>
                  <option value="PROFILE">PROFILE - Execute with performance profiling</option>
                </>
              )}
              
              {schemaConfig.objectType === 'FUNCTION' && (
                <>
                  <option value="" disabled selected>--- Select SOAP Action ---</option>
                  <option value="EXECUTE">EXECUTE - Execute function and return value</option>
                  <option value="VALIDATE">VALIDATE - Test function with sample inputs</option>
                  <option value="PROFILE">PROFILE - Execute with performance metrics</option>
                </>
              )}
              
              {schemaConfig.objectType === 'PACKAGE' && (
                <>
                  <option value="" disabled selected>--- Select SOAP Action ---</option>
                  <option value="EXECUTE_PROCEDURE">EXECUTE_PROCEDURE - Execute package procedure</option>
                  <option value="EXECUTE_FUNCTION">EXECUTE_FUNCTION - Execute package function</option>
                  <option value="GET_STATE">GET_STATE - Get package state</option>
                  <option value="RESET">RESET - Reset package state</option>
                </>
              )}
              
              {/* Custom Query options */}
              {sourceType === 'custom_query' && (
                <>
                  <option value="" disabled selected>--- Select SOAP Action ---</option>
                  <option value="QUERY">QUERY - Execute custom query</option>
                  <option value="EXPORT">EXPORT - Export query results</option>
                  <option value="ANALYZE">ANALYZE - Analyze query performance</option>
                  <option value="EXPLAIN">EXPLAIN - Get query execution plan</option>
                </>
              )}
              
              {/* Fallback generic options */}
              {!schemaConfig.objectType && sourceType !== 'custom_query' && (
                <>
                  <option value="" disabled selected>--- Select SOAP Action ---</option>
                  <option value="PROCESS">PROCESS - Process request</option>
                  <option value="VALIDATE">VALIDATE - Validate request</option>
                </>
              )}
            </select>
            <p className="text-xs" style={{ color: themeColors.textSecondary }}>
              {schemaConfig.objectType === 'TABLE' && 'Select the CRUD operation to perform on the table'}
              {schemaConfig.objectType === 'VIEW' && 'Select the query operation to perform on the view'}
              {schemaConfig.objectType === 'PROCEDURE' && 'Select how to execute the stored procedure'}
              {schemaConfig.objectType === 'FUNCTION' && 'Select how to execute the function'}
              {schemaConfig.objectType === 'PACKAGE' && 'Select which package operation to execute'}
              {sourceType === 'custom_query' && 'Select how to execute your custom SQL query'}
            </p>
            {validationErrors.soapAction && (
              <p className="text-xs mt-1" style={{ color: themeColors.error }}>
                {validationErrors.soapAction}
              </p>
            )}
          </div>
          )}

              {/* For GraphQL, show Operation Name */}
              {protocolType === 'graphql' && (
                <div className="space-y-2">
                  <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                    Operation Name *
                    <Asterisk className="h-3 w-3" style={{ color: themeColors.error }} />
                  </label>
                  <input
                    type="text"
                    value={graphqlConfig.operationName}
                    onChange={(e) => handleGraphqlConfigChange('operationName', e.target.value)}
                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                    style={{ 
                      backgroundColor: themeColors.card,
                      borderColor: validationErrors.operationName ? themeColors.error : themeColors.border,
                      color: themeColors.text
                    }}
                    placeholder="getUsers"
                  />
                  {validationErrors.operationName && (
                    <p className="text-xs mt-1" style={{ color: themeColors.error }}>
                      {validationErrors.operationName}
                    </p>
                  )}
                </div>
              )}

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Version
                </label>
                <input
                  type="text"
                  value={apiDetails.version}
                  onChange={(e) => handleApiDetailChange('version', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="1.0.0"
                  disabled={loading || validating}
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Owner
                </label>
                <input
                  type="text"
                  value={apiDetails.owner}
                  onChange={(e) => handleApiDetailChange('owner', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="CBXDMX"
                  disabled
                />
              </div>

              <div className="space-y-2 md:col-span-2 lg:col-span-4">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Description
                </label>
                <textarea
                  value={apiDetails.description}
                  onChange={(e) => handleApiDetailChange('description', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  rows="2"
                  placeholder="API description..."
                  disabled={loading || validating}
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
                  className={`px-3 py-2 flex items-center gap-2 border-b-2 text-xs font-medium transition-colors whitespace-nowrap hover-lift ${
                    activeTab === tab.id
                      ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === tab.id ? themeColors.info : 'transparent',
                    color: activeTab === tab.id ? themeColors.info : themeColors.textSecondary,
                    backgroundColor: 'transparent'
                  }}
                  disabled={loading || validating}
                >
                  {tab.icon}
                  {tab.label}
                </button>
              ))}
            </div>
          </div>

          {/* Tab Content - ADD LOADING STATE AT TOP */}
          <div className="flex-1 overflow-y-auto">
            {selectedObject?.loading ? (
              <div className="h-full flex items-center justify-center p-12">
                <div className="text-center">
                  <div className="relative">
                    <Loader className="animate-spin mx-auto mb-6" size={48} style={{ color: themeColors.primary }} />
                    <div className="absolute inset-0 flex items-center justify-center">
                      <div className="w-3 h-3 rounded-full" style={{ backgroundColor: themeColors.primary }}></div>
                    </div>
                  </div>
                  <h3 className="text-lg font-semibold mb-2" style={{ color: themeColors.text }}>
                    Loading API Details
                  </h3>
                  <p className="text-sm mb-2" style={{ color: themeColors.textSecondary }}>
                    Please wait while we fetch the complete API configuration
                  </p>
                  <div className="w-48 h-1 mx-auto mt-4 rounded-full overflow-hidden" style={{ backgroundColor: themeColors.border }}>
                    <div className="h-full animate-pulse" style={{ 
                      width: '60%', 
                      backgroundColor: themeColors.primary,
                      animation: 'progress 2s ease-in-out infinite'
                    }}></div>
                  </div>
                  <style jsx>{`
                    @keyframes progress {
                      0% { width: 0%; margin-left: 0%; }
                      50% { width: 100%; margin-left: 0%; }
                      100% { width: 0%; margin-left: 100%; }
                    }
                  `}</style>
                </div>
              </div>
            ) : loading ? (
              <div className="h-full flex items-center justify-center p-8">
                <div className="text-center">
                  <Loader className="h-12 w-12 animate-spin mx-auto mb-4" style={{ color: themeColors.info }} />
                  <p className="text-sm" style={{ color: themeColors.text }}>
                    {isEditing ? 'Loading API data...' : 'Fetching object details...'}
                  </p>
                  <p className="text-xs mt-2" style={{ color: themeColors.textSecondary }}>
                    {sourceObjectInfo.isSynonym ? 
                      `Resolving synonym to ${sourceObjectInfo.targetType}...` : 
                      'Loading parameters and mappings...'}
                  </p>
                </div>
              </div>
            ) : validating ? (
              <div className="h-full flex items-center justify-center p-8">
                <div className="text-center">
                  <Loader className="h-12 w-12 animate-spin mx-auto mb-4" style={{ color: themeColors.warning }} />
                  <p className="text-sm" style={{ color: themeColors.text }}>Validating source object...</p>
                  <p className="text-xs mt-2" style={{ color: themeColors.textSecondary }}>
                    Checking object compatibility for API generation
                  </p>
                </div>
              </div>
            ) : (
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
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Category
                          </label>
                          <select
                            value={apiDetails.category}
                            onChange={(e) => handleApiDetailChange('category', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.bg,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          >
                            <option value="general">General</option>
                            <option value="data">Data Access</option>
                            <option value="oracle">Administrative</option>
                            <option value="report">Reporting</option>
                            <option value="integration">Integration</option>
                          </select>
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Tags
                          </label>
                          <input
                            type="text"
                            value={apiDetails.tags.join(', ')}
                            onChange={(e) => handleApiDetailChange('tags', e.target.value.split(',').map(tag => tag.trim()))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
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
                          {protocolType === 'soap' ? <Send className="h-4 w-4" /> :
                           protocolType === 'graphql' ? <GitMerge className="h-4 w-4" /> :
                           <Globe className="h-4 w-4" />}
                          API Endpoint Preview
                        </h4>
                        <div className="font-mono text-xs p-3 rounded border" style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: themeColors.border
                        }}>
                          {protocolType === 'soap' ? (
                            <>
                              <div style={{ color: themeColors.textSecondary }}>
                                SOAP {soapConfig.version} • {soapConfig.bindingStyle} / {soapConfig.encodingStyle}
                              </div>
                              <div className="mt-2">
                                <span style={{ color: themeColors.textSecondary }}>SOAP Action:</span>
                                <span className="ml-2 font-medium" style={{ color: themeColors.info }}>
                                  {soapConfig.soapAction || 'Not set'}
                                </span>
                              </div>
                              <div className="mt-1">
                                <span style={{ color: themeColors.textSecondary }}>Namespace:</span>
                                <span className="ml-2 font-medium" style={{ color: themeColors.info }}>
                                  {soapConfig.namespace}
                                </span>
                              </div>
                            </>
                          ) : protocolType === 'graphql' ? (
                            <>
                              <div style={{ color: themeColors.textSecondary }}>
                                GraphQL • {graphqlConfig.operationType}
                              </div>
                              <div className="mt-2">
                                <span style={{ color: themeColors.textSecondary }}>Operation:</span>
                                <span className="ml-2 font-medium" style={{ color: themeColors.info }}>
                                  {graphqlConfig.operationName || 'Not set'}
                                </span>
                              </div>
                              <div className="mt-1">
                                <span style={{ color: themeColors.textSecondary }}>Max Depth:</span>
                                <span className="ml-2 font-medium" style={{ color: themeColors.info }}>
                                  {graphqlConfig.maxQueryDepth}
                                </span>
                              </div>
                            </>
                          ) : (
                            <>
                              <div style={{ color: themeColors.textSecondary }}>
                                {apiDetails.httpMethod} {apiDetails.basePath}{apiDetails.endpointPath}
                              </div>
                              {getInParameters().filter(p => p.parameterLocation === 'query').length > 0 && (
                                <div className="mt-2 text-xs" style={{ color: themeColors.textSecondary }}>
                                  Query Parameters: {getInParameters().filter(p => p.parameterLocation === 'query').map(p => p.key).join(', ')}
                                </div>
                              )}
                            </>
                          )}
                          <div className="mt-2 text-xs" style={{ color: themeColors.textSecondary }}>
                            Source: {schemaConfig.schemaName}.{schemaConfig.objectName} ({schemaConfig.objectType})
                          </div>
                          <div className="mt-2 text-xs flex items-center gap-2" style={{ color: themeColors.success }}>
                            <Layers className="h-3 w-3" />
                            {selectedCollection?.name || 'No collection'} 
                            {selectedFolder && ` › ${selectedFolder.name}`}
                          </div>
                          {sourceObjectInfo.isSynonym && (
                            <div className="mt-1 text-xs" style={{ color: themeColors.warning }}>
                              <Link className="h-3 w-3 inline mr-1" />
                              Synonym → {sourceObjectInfo.targetType}: {sourceObjectInfo.targetOwner}.{sourceObjectInfo.targetName}
                            </div>
                          )}
                          {validationResult && validationResult.valid && (
                            <div className="mt-1 text-xs" style={{ color: themeColors.success }}>
                              <Check className="h-3 w-3 inline mr-1" />
                              Object validated successfully
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Schema Tab - Only show for new API generation, not editing */}
                {activeTab === 'schema' && !isEditing && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      {isEditing ? 'Database Schema Configuration' : 'Source Configuration'}
                    </h3>

                    {/* Source Type Selection - Show for both new and edit mode */}
                    <div className="mb-6">
                      <label className="text-xs font-medium flex items-center gap-2 mb-2" style={{ color: themeColors.text }}>
                        <Database className="h-4 w-4" />
                        Source Type
                      </label>
                      <div className="flex gap-4">
                        <label className="flex items-center gap-2">
                          <input
                            type="radio"
                            value="database_object"
                            checked={sourceType === 'database_object'}
                            onChange={async (e) => {
                              console.log('🔄 Switching to Database Object mode');
                              
                              setSourceType(e.target.value);
                              setIsCustomQuery(false);
                              setIsEditingCustomQuery(false);
                              
                              // If we have a selected database object, load its parameters
                              if (selectedDbObject) {
                                console.log('📦 Loading parameters from database object:', selectedDbObject.name);
                                setLoading(true);
                                try {
                                  // ============ CRITICAL FIX: Pass forceRefresh = true ============
                                  await populateFormFromObject(selectedDbObject, true, true);
                                } catch (error) {
                                  console.error('❌ Error loading database object:', error);
                                } finally {
                                  setLoading(false);
                                }
                              } else if (!isEditing) {
                                // No object selected, show the object selector
                                setShowObjectSelector(true);
                              }
                              
                              // Clear validation errors
                              setValidationErrors(prev => ({ 
                                ...prev, 
                                customQuery: null, 
                                schemaName: null, 
                                objectName: null 
                              }));
                            }}
                            className="h-4 w-4"
                            style={{ accentColor: themeColors.info }}
                          />
                          <span className="text-sm" style={{ color: themeColors.text }}>Database Object</span>
                        </label>
                        
                        <label className="flex items-center gap-2">
                          <input
                            type="radio"
                            value="custom_query"
                            checked={sourceType === 'custom_query'}
                            onChange={(e) => {
                              console.log('🔄 Switching to Custom Query mode');
                              
                              setSourceType(e.target.value);
                              setIsCustomQuery(true);
                              setIsEditingCustomQuery(true);
                              
                              // Clear database object parameters - we'll extract from query
                              setParameters([]);
                              setResponseMappings([]);
                              
                              // Get custom query text
                              let existingCustomQuery = customQuery;
                              
                              if (!existingCustomQuery && selectedObject) {
                                const sourceObj = selectedObject.sourceObject || selectedObject;
                                const apiData = selectedObject.data || selectedObject;
                                
                                existingCustomQuery = 
                                  selectedObject?.customSelectStatement ||
                                  selectedObject?.sourceObject?.customSelectStatement ||
                                  sourceObj?.customSelectStatement ||
                                  sourceObj?.query ||
                                  selectedObject?.customQueryText ||
                                  apiData?.customSelectStatement ||
                                  apiData?.sourceObject?.customSelectStatement ||
                                  '';
                                  
                                if (existingCustomQuery) {
                                  setCustomQuery(existingCustomQuery);
                                  setOriginalCustomQuery(existingCustomQuery);
                                }
                              }
                              
                              // Extract parameters from the query (default to body location for POST)
                              if (existingCustomQuery && existingCustomQuery.trim()) {
                                console.log('📊 Extracting parameters from custom query');
                                
                                // Extract :paramName style parameters
                                const paramMatches = existingCustomQuery.match(/:\w+/g) || [];
                                const uniqueParams = [...new Set(paramMatches)];
                                
                                if (uniqueParams.length > 0) {
                                  const extractedParams = uniqueParams.map((param, idx) => ({
                                    id: `param-${Date.now()}-${idx}`,
                                    key: param.substring(1),
                                    dbColumn: param.substring(1),
                                    oracleType: 'VARCHAR2',
                                    apiType: 'string',
                                    parameterLocation: 'body', // Default to body for custom queries with POST
                                    required: true,
                                    description: `Parameter: ${param.substring(1)}`,
                                    example: '',
                                    validationPattern: '',
                                    defaultValue: '',
                                    inBody: true,
                                    isPrimaryKey: false,
                                    paramMode: 'IN',
                                    _isFromCustomQuery: true
                                  }));
                                  setParameters(extractedParams);
                                  console.log(`✅ Extracted ${extractedParams.length} parameters from query`);
                                }
                                
                                // Extract response columns from SELECT clause
                                const selectMatch = existingCustomQuery.match(/SELECT\s+(.*?)\s+FROM/i);
                                if (selectMatch && selectMatch[1]) {
                                  const selectClause = selectMatch[1];
                                  const columnMatches = selectClause.match(/(?:[^,\s]+\s+AS\s+)?(\w+)/gi) || [];
                                  const uniqueColumns = [...new Set(columnMatches.map(col => {
                                    const asMatch = col.match(/AS\s+(\w+)/i);
                                    return asMatch ? asMatch[1].toLowerCase() : col.toLowerCase();
                                  }))];
                                  
                                  if (uniqueColumns.length > 0) {
                                    const extractedMappings = uniqueColumns.slice(0, 20).map((fieldName, idx) => ({
                                      id: `mapping-${Date.now()}-${idx}`,
                                      apiField: fieldName,
                                      dbColumn: fieldName,
                                      oracleType: fieldName.includes('id') ? 'NUMBER' : 'VARCHAR2',
                                      apiType: fieldName.includes('id') ? 'integer' : 'string',
                                      format: '',
                                      nullable: true,
                                      isPrimaryKey: fieldName === 'id',
                                      includeInResponse: true,
                                      inResponse: true,
                                      paramMode: 'OUT',
                                      _fromCustomQuery: true
                                    }));
                                    setResponseMappings(extractedMappings);
                                    console.log(`✅ Extracted ${extractedMappings.length} response mappings from query`);
                                  }
                                }
                              }
                              
                              // Clear validation errors
                              setValidationErrors(prev => ({ 
                                ...prev, 
                                customQuery: null, 
                                schemaName: null, 
                                objectName: null 
                              }));
                            }}
                            className="h-4 w-4"
                            style={{ accentColor: themeColors.info }}
                          />
                          <span className="text-sm" style={{ color: themeColors.text }}>Custom SQL Query</span>
                        </label>
                      </div>
                    </div>

                    {/* Database Object Selection Section */}
                    {sourceType === 'database_object' && (
                      <>
                        {/* Object Selector Button */}
                        <div className="mb-6">
                          <button
                            onClick={() => setShowObjectSelector(true)}
                            className="px-4 py-2 rounded-lg flex items-center gap-2 text-sm transition-colors hover-lift"
                            style={{ backgroundColor: themeColors.info, color: themeColors.white }}
                          >
                            <Search className="h-4 w-4" />
                            {selectedDbObject ? 'Change Database Object' : 'Select Database Object'}
                          </button>
                          
                          {selectedDbObject && (
                            <div className="mt-3 p-3 rounded-lg border" style={{ 
                              borderColor: themeColors.success + '40',
                              backgroundColor: themeColors.success + '10'
                            }}>
                              <div className="flex items-center justify-between">
                                <div>
                                  <div className="font-medium text-sm" style={{ color: themeColors.text }}>
                                    {selectedDbObject.name}
                                  </div>
                                  <div className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>
                                    {selectedDbObject.owner}.{selectedDbObject.name} ({selectedDbObject.type})
                                  </div>
                                  <div className="flex items-center gap-2 mt-2">
                                    <span className="text-xs px-2 py-0.5 rounded-full" style={{ 
                                      backgroundColor: selectedDbObject.databaseType === 'PostgreSQL' ? '#3b82f620' : '#ef444420',
                                      color: selectedDbObject.databaseType === 'PostgreSQL' ? '#3b82f6' : '#ef4444'
                                    }}>
                                      {selectedDbObject.databaseType === 'PostgreSQL' ? 'PostgreSQL' : 'Oracle'}
                                    </span>
                                    {sourceObjectInfo.isSynonym && (
                                      <span className="text-xs px-2 py-0.5 rounded-full" style={{ 
                                        backgroundColor: themeColors.warning + '20',
                                        color: themeColors.warning
                                      }}>
                                        Synonym → {sourceObjectInfo.targetType}
                                      </span>
                                    )}
                                  </div>
                                </div>
                                <button
                                  onClick={() => {
                                    setSelectedDbObject(null);
                                    setSchemaConfig({
                                      schemaName: '',
                                      objectType: '',
                                      objectName: '',
                                      operation: 'SELECT',
                                      primaryKeyColumn: '',
                                      sequenceName: '',
                                      enablePagination: true,
                                      pageSize: 10,
                                      enableSorting: true,
                                      defaultSortColumn: '',
                                      defaultSortDirection: 'ASC'
                                    });
                                    setParameters([]);
                                    setResponseMappings([]);
                                  }}
                                  className="p-1.5 rounded transition-colors hover-lift"
                                  style={{ backgroundColor: themeColors.error + '20', color: themeColors.error }}
                                  title="Clear selection"
                                >
                                  <X className="h-4 w-4" />
                                </button>
                              </div>
                            </div>
                          )}
                          
                          {/* Show manual schema config when no object selected */}
                          {!selectedDbObject && (
                            <div className="mt-4 text-center p-4 rounded-lg border border-dashed" style={{ 
                              borderColor: themeColors.info + '40',
                              backgroundColor: themeColors.info + '10'
                            }}>
                              <Database className="h-8 w-8 mx-auto mb-2" style={{ color: themeColors.info }} />
                              <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                                No database object selected. Click the button above to select one.
                              </p>
                            </div>
                          )}
                        </div>

                        {/* Manual Schema Configuration Form - Only show when object is selected */}
                        {selectedDbObject && (
                          <>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                              {/* ... existing schema config form ... */}
                            </div>
                          </>
                        )}
                      </>
                    )}

                    {/* Custom Query Section - Show for both new and edit mode when sourceType is custom_query */}
                    {sourceType === 'custom_query' && (
                      <div className="space-y-4">
                        <div className="flex items-center justify-between">
                          <label className="text-xs font-medium flex items-center gap-2" style={{ color: themeColors.text }}>
                            <Wand2 size={14} style={{ color: themeColors.info }} />
                            Custom SQL Statement
                            {!customQuery?.trim() && validationErrors.customQuery && (
                              <span className="text-xs" style={{ color: themeColors.error }}>(Required)</span>
                            )}
                          </label>
                          
                          {isEditingCustomQuery && (
                            <span className="text-xs px-2 py-1 rounded-full" style={{
                              backgroundColor: themeColors.warning + '20',
                              color: themeColors.warning
                            }}>
                              <Edit className="h-3 w-3 inline mr-1" />
                              Editing Mode
                            </span>
                          )}
                        </div>
                        
                        <textarea
                          value={customQuery}
                          onChange={(e) => setCustomQuery(e.target.value)}
                          className="w-full px-3 py-3 rounded-lg text-sm font-mono focus:outline-none focus:ring-2 focus:ring-opacity-50"
                          style={{ 
                            backgroundColor: themeColors.codeBg || (theme === 'dark' ? '#1a202c' : '#f8fafc'),
                            border: `1px solid ${validationErrors.customQuery ? themeColors.error : themeColors.border}`,
                            color: themeColors.text,
                            fontFamily: 'monospace',
                            fontSize: '13px',
                            lineHeight: '1.5',
                            minHeight: '200px'
                          }}
                          placeholder="SELECT * FROM your_table WHERE column = :paramName"
                          spellCheck={false}
                        />
                        
                        <div className="flex items-center justify-between">
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                            Use :paramName syntax for parameters (e.g., WHERE id = :userId)
                          </p>
                          
                          <button
                            onClick={() => {
                              // Extract parameters from the custom query
                              const paramMatches = customQuery.match(/:\w+/g) || [];
                              const uniqueParams = [...new Set(paramMatches)];
                              const newParams = uniqueParams.map((param, idx) => ({
                                id: `param-${Date.now()}-${idx}`,
                                key: param.substring(1),
                                dbColumn: param.substring(1),
                                oracleType: 'VARCHAR2',
                                apiType: 'string',
                                parameterLocation: 'query',
                                required: true,
                                description: `Parameter: ${param.substring(1)}`,
                                example: '',
                                validationPattern: '',
                                defaultValue: '',
                                inBody: false,
                                isPrimaryKey: false,
                                paramMode: 'IN'
                              }));
                              setParameters(newParams);
                            }}
                            className="text-xs px-3 py-1 rounded-lg flex items-center gap-1 transition-colors hover-lift"
                            style={{ 
                              backgroundColor: themeColors.info + '20',
                              color: themeColors.info,
                              border: `1px solid ${themeColors.info + '40'}`
                            }}
                          >
                            <RefreshCw className="h-3 w-3" />
                            Extract Parameters
                          </button>
                        </div>
                        
                        {/* Query Preview / Analysis */}
                        {customQuery && customQuery.trim() !== '' && (
                          <div className="p-3 rounded-lg border" style={{ 
                            borderColor: themeColors.success + '40',
                            backgroundColor: themeColors.success + '10'
                          }}>
                            <div className="flex items-center gap-2 mb-2">
                              <CheckCircle className="h-3 w-3" style={{ color: themeColors.success }} />
                              <span className="text-xs font-medium" style={{ color: themeColors.success }}>Query Analysis</span>
                            </div>
                            <div className="grid grid-cols-2 gap-2 text-xs">
                              <div>
                                <span style={{ color: themeColors.textSecondary }}>Tables referenced:</span>
                                <span className="ml-2 font-mono" style={{ color: themeColors.text }}>
                                  {(customQuery.match(/FROM\s+([^\s,;]+)/i) || []).slice(1).join(', ') || 'Unknown'}
                                </span>
                              </div>
                              <div>
                                <span style={{ color: themeColors.textSecondary }}>Parameters found:</span>
                                <span className="ml-2 font-mono" style={{ color: themeColors.text }}>
                                  {(customQuery.match(/:\w+/g) || []).length}
                                </span>
                              </div>
                              <div className="col-span-2">
                                <span style={{ color: themeColors.textSecondary }}>Estimated complexity:</span>
                                <span className="ml-2 font-mono" style={{ color: themeColors.text }}>
                                  {customQuery.split(/\s+/).length > 50 ? 'Complex' : 
                                  customQuery.split(/\s+/).length > 20 ? 'Moderate' : 'Simple'}
                                </span>
                              </div>
                            </div>
                          </div>
                        )}
                        
                        {/* Database Type for Custom Query */}
                        <div className="mt-4 p-4 rounded-lg border" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <label className="text-xs font-medium flex items-center gap-2 mb-3" style={{ color: themeColors.text }}>
                            <Database className="h-4 w-4" />
                            Database Type for Custom Query
                          </label>
                          <div className="flex gap-4">
                            <label className="flex items-center gap-2">
                              <input
                                type="radio"
                                value="oracle"
                                checked={currentDatabaseType === 'oracle'}
                                onChange={(e) => setCurrentDatabaseType(e.target.value)}
                                className="h-4 w-4"
                                style={{ accentColor: '#ef4444' }}
                              />
                              <span className="text-sm" style={{ color: themeColors.text }}>Oracle</span>
                            </label>
                            <label className="flex items-center gap-2">
                              <input
                                type="radio"
                                value="postgresql"
                                checked={currentDatabaseType === 'postgresql'}
                                onChange={(e) => setCurrentDatabaseType(e.target.value)}
                                className="h-4 w-4"
                                style={{ accentColor: '#3b82f6' }}
                              />
                              <span className="text-sm" style={{ color: themeColors.text }}>PostgreSQL</span>
                            </label>
                          </div>
                          <p className="text-xs mt-2" style={{ color: themeColors.textSecondary }}>
                            {currentDatabaseType === 'postgresql' 
                              ? 'Query will be executed against PostgreSQL database' 
                              : 'Query will be executed against Oracle database'}
                          </p>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* Schema Tab placeholder for editing mode */}
                {activeTab === 'schema' && isEditing && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      Database Schema Configuration
                    </h3>

                    {/* Source Type Selection */}
                    <div className="mb-6">
                      <label className="text-xs font-medium flex items-center gap-2 mb-2" style={{ color: themeColors.text }}>
                        <Database className="h-4 w-4" />
                        Source Type
                      </label>
                      <div className="flex gap-4">
                        <label className="flex items-center gap-2">
                          <input
                            type="radio"
                            value="database_object"
                            checked={sourceType === 'database_object'}
                            onChange={async (e) => {
                              console.log('🔄 Switching to Database Object mode');
                              
                              setSourceType(e.target.value);
                              setIsCustomQuery(false);
                              setIsEditingCustomQuery(false);
                              
                              // If we have a selected database object, load its parameters
                              if (selectedDbObject) {
                                console.log('📦 Loading parameters from database object:', selectedDbObject.name);
                                setLoading(true);
                                try {
                                  // This will populate parameters with the correct locations
                                  // based on the object type, HTTP method, etc.
                                  await populateFormFromObject(selectedDbObject, true);
                                } catch (error) {
                                  console.error('❌ Error loading database object:', error);
                                } finally {
                                  setLoading(false);
                                }
                              } else if (!isEditing) {
                                // No object selected, show the object selector
                                setShowObjectSelector(true);
                              }
                              
                              // Clear validation errors
                              setValidationErrors(prev => ({ 
                                ...prev, 
                                customQuery: null, 
                                schemaName: null, 
                                objectName: null 
                              }));
                            }}
                            className="h-4 w-4"
                            style={{ accentColor: themeColors.info }}
                          />
                          <span className="text-sm" style={{ color: themeColors.text }}>Database Object</span>
                        </label>
                        
                        <label className="flex items-center gap-2">
                          <input
                            type="radio"
                            value="custom_query"
                            checked={sourceType === 'custom_query'}
                            onChange={(e) => {
                              console.log('🔄 Switching to Custom Query mode');
                              
                              setSourceType(e.target.value);
                              setIsCustomQuery(true);
                              setIsEditingCustomQuery(true);
                              
                              // Clear database object parameters - we'll extract from query
                              setParameters([]);
                              setResponseMappings([]);
                              
                              // Get custom query text
                              let existingCustomQuery = customQuery;
                              
                              if (!existingCustomQuery && selectedObject) {
                                const sourceObj = selectedObject.sourceObject || selectedObject;
                                const apiData = selectedObject.data || selectedObject;
                                
                                existingCustomQuery = 
                                  selectedObject?.customSelectStatement ||
                                  selectedObject?.sourceObject?.customSelectStatement ||
                                  sourceObj?.customSelectStatement ||
                                  sourceObj?.query ||
                                  selectedObject?.customQueryText ||
                                  apiData?.customSelectStatement ||
                                  apiData?.sourceObject?.customSelectStatement ||
                                  '';
                                  
                                if (existingCustomQuery) {
                                  setCustomQuery(existingCustomQuery);
                                  setOriginalCustomQuery(existingCustomQuery);
                                }
                              }
                              
                              // Extract parameters from the query (default to body location for POST)
                              if (existingCustomQuery && existingCustomQuery.trim()) {
                                console.log('📊 Extracting parameters from custom query');
                                
                                // Extract :paramName style parameters
                                const paramMatches = existingCustomQuery.match(/:\w+/g) || [];
                                const uniqueParams = [...new Set(paramMatches)];
                                
                                if (uniqueParams.length > 0) {
                                  const extractedParams = uniqueParams.map((param, idx) => ({
                                    id: `param-${Date.now()}-${idx}`,
                                    key: param.substring(1),
                                    dbColumn: param.substring(1),
                                    oracleType: 'VARCHAR2',
                                    apiType: 'string',
                                    parameterLocation: 'body', // Default to body for custom queries with POST
                                    required: true,
                                    description: `Parameter: ${param.substring(1)}`,
                                    example: '',
                                    validationPattern: '',
                                    defaultValue: '',
                                    inBody: true,
                                    isPrimaryKey: false,
                                    paramMode: 'IN',
                                    _isFromCustomQuery: true
                                  }));
                                  setParameters(extractedParams);
                                  console.log(`✅ Extracted ${extractedParams.length} parameters from query`);
                                }
                                
                                // Extract response columns from SELECT clause
                                const selectMatch = existingCustomQuery.match(/SELECT\s+(.*?)\s+FROM/i);
                                if (selectMatch && selectMatch[1]) {
                                  const selectClause = selectMatch[1];
                                  const columnMatches = selectClause.match(/(?:[^,\s]+\s+AS\s+)?(\w+)/gi) || [];
                                  const uniqueColumns = [...new Set(columnMatches.map(col => {
                                    const asMatch = col.match(/AS\s+(\w+)/i);
                                    return asMatch ? asMatch[1].toLowerCase() : col.toLowerCase();
                                  }))];
                                  
                                  if (uniqueColumns.length > 0) {
                                    const extractedMappings = uniqueColumns.slice(0, 20).map((fieldName, idx) => ({
                                      id: `mapping-${Date.now()}-${idx}`,
                                      apiField: fieldName,
                                      dbColumn: fieldName,
                                      oracleType: fieldName.includes('id') ? 'NUMBER' : 'VARCHAR2',
                                      apiType: fieldName.includes('id') ? 'integer' : 'string',
                                      format: '',
                                      nullable: true,
                                      isPrimaryKey: fieldName === 'id',
                                      includeInResponse: true,
                                      inResponse: true,
                                      paramMode: 'OUT',
                                      _fromCustomQuery: true
                                    }));
                                    setResponseMappings(extractedMappings);
                                    console.log(`✅ Extracted ${extractedMappings.length} response mappings from query`);
                                  }
                                }
                              }
                              
                              // Clear validation errors
                              setValidationErrors(prev => ({ 
                                ...prev, 
                                customQuery: null, 
                                schemaName: null, 
                                objectName: null 
                              }));
                            }}
                            className="h-4 w-4"
                            style={{ accentColor: themeColors.info }}
                          />
                          <span className="text-sm" style={{ color: themeColors.text }}>Custom SQL Query</span>
                        </label>
                      </div>
                    </div>

                    {/* Database Object Selection Section */}
                    {sourceType === 'database_object' && (
                      <>
                        {/* Object Selector Button */}
                        <div className="mb-6">
                          <button
                            onClick={() => setShowObjectSelector(true)}
                            className="px-4 py-2 rounded-lg flex items-center gap-2 text-sm transition-colors hover-lift"
                            style={{ backgroundColor: themeColors.info, color: themeColors.white }}
                          >
                            <Search className="h-4 w-4" />
                            {selectedDbObject ? 'Change Database Object' : 'Select Database Object'}
                          </button>
                          
                          {selectedDbObject && (
                            <div className="mt-3 p-3 rounded-lg border" style={{ 
                              borderColor: themeColors.success + '40',
                              backgroundColor: themeColors.success + '10'
                            }}>
                              <div className="flex items-center justify-between">
                                <div>
                                  <div className="font-medium text-sm" style={{ color: themeColors.text }}>
                                    {selectedDbObject.name}
                                  </div>
                                  <div className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>
                                    {selectedDbObject.owner}.{selectedDbObject.name} ({selectedDbObject.type})
                                  </div>
                                  <div className="flex items-center gap-2 mt-2">
                                    <span className="text-xs px-2 py-0.5 rounded-full" style={{ 
                                      backgroundColor: selectedDbObject.databaseType === 'PostgreSQL' ? '#3b82f620' : '#ef444420',
                                      color: selectedDbObject.databaseType === 'PostgreSQL' ? '#3b82f6' : '#ef4444'
                                    }}>
                                      {selectedDbObject.databaseType === 'PostgreSQL' ? 'PostgreSQL' : 'Oracle'}
                                    </span>
                                    {sourceObjectInfo.isSynonym && (
                                      <span className="text-xs px-2 py-0.5 rounded-full" style={{ 
                                        backgroundColor: themeColors.warning + '20',
                                        color: themeColors.warning
                                      }}>
                                        Synonym → {sourceObjectInfo.targetType}
                                      </span>
                                    )}
                                  </div>
                                </div>
                                <button
                                  onClick={() => {
                                    setSelectedDbObject(null);
                                    setSchemaConfig({
                                      schemaName: '',
                                      objectType: '',
                                      objectName: '',
                                      operation: 'SELECT',
                                      primaryKeyColumn: '',
                                      sequenceName: '',
                                      enablePagination: true,
                                      pageSize: 10,
                                      enableSorting: true,
                                      defaultSortColumn: '',
                                      defaultSortDirection: 'ASC'
                                    });
                                    setParameters([]);
                                    setResponseMappings([]);
                                  }}
                                  className="p-1.5 rounded transition-colors hover-lift"
                                  style={{ backgroundColor: themeColors.error + '20', color: themeColors.error }}
                                  title="Clear selection"
                                >
                                  <X className="h-4 w-4" />
                                </button>
                              </div>
                            </div>
                          )}
                          
                          {/* Show manual schema config when no object selected */}
                          {!selectedDbObject && (
                            <div className="mt-4 text-center p-4 rounded-lg border border-dashed" style={{ 
                              borderColor: themeColors.info + '40',
                              backgroundColor: themeColors.info + '10'
                            }}>
                              <Database className="h-8 w-8 mx-auto mb-2" style={{ color: themeColors.info }} />
                              <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                                No database object selected. Click the button above to select one.
                              </p>
                            </div>
                          )}
                        </div>

                        {/* Manual Schema Configuration Form - Only show when object is selected */}
                        {selectedDbObject && (
                          <>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                              <div className="space-y-4">
                                <div className="space-y-2">
                                  <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                                    Schema Name
                                    <Asterisk className="h-3 w-3" style={{ color: themeColors.error }} />
                                  </label>
                                  <input
                                    type="text"
                                    value={schemaConfig.schemaName}
                                    onChange={(e) => handleSchemaConfigChange('schemaName', e.target.value)}
                                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.card,
                                      borderColor: validationErrors.schemaName ? themeColors.error : themeColors.border,
                                      color: themeColors.text
                                    }}
                                    placeholder="CBXDMX"
                                    readOnly
                                  />
                                </div>

                                <div className="space-y-2">
                                  <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                                    Object Type
                                  </label>
                                  <select
                                    value={schemaConfig.objectType}
                                    onChange={(e) => handleSchemaConfigChange('objectType', e.target.value)}
                                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                    disabled
                                  >
                                    <option value="">Select type</option>
                                    <option value="TABLE">Table</option>
                                    <option value="VIEW">View</option>
                                    <option value="PROCEDURE">Procedure</option>
                                    <option value="FUNCTION">Function</option>
                                    <option value="PACKAGE">Package</option>
                                  </select>
                                </div>

                                <div className="space-y-2">
                                  <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                                    Object Name
                                    <Asterisk className="h-3 w-3" style={{ color: themeColors.error }} />
                                  </label>
                                  <input
                                    type="text"
                                    value={schemaConfig.objectName}
                                    onChange={(e) => handleSchemaConfigChange('objectName', e.target.value)}
                                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.card,
                                      borderColor: validationErrors.objectName ? themeColors.error : themeColors.border,
                                      color: themeColors.text
                                    }}
                                    placeholder="EMPLOYEES"
                                    readOnly
                                  />
                                </div>

                                <div className="space-y-2">
                                  <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                    Operation
                                  </label>
                                  <select
                                    value={schemaConfig.operation}
                                    onChange={(e) => handleSchemaConfigChange('operation', e.target.value)}
                                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
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
                                  <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                    Primary Key Column
                                  </label>
                                  <input
                                    type="text"
                                    value={schemaConfig.primaryKeyColumn}
                                    onChange={(e) => handleSchemaConfigChange('primaryKeyColumn', e.target.value)}
                                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.card,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                    placeholder="ID"
                                  />
                                </div>

                                <div className="space-y-2">
                                  <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                    Sequence Name (for INSERT)
                                  </label>
                                  <input
                                    type="text"
                                    value={schemaConfig.sequenceName}
                                    onChange={(e) => handleSchemaConfigChange('sequenceName', e.target.value)}
                                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
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
                                    <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                      Enable Pagination
                                    </label>
                                    <div className="flex items-center">
                                      <input
                                        type="checkbox"
                                        checked={schemaConfig.enablePagination}
                                        onChange={(e) => handleSchemaConfigChange('enablePagination', e.target.checked)}
                                        className="h-4 w-4 rounded"
                                        style={{ accentColor: themeColors.info }}
                                      />
                                      <span className="ml-2 text-xs" style={{ color: themeColors.textSecondary }}>Yes</span>
                                    </div>
                                  </div>

                                  <div className="space-y-2">
                                    <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                      Page Size
                                    </label>
                                    <input
                                      type="number"
                                      value={schemaConfig.pageSize}
                                      onChange={(e) => handleSchemaConfigChange('pageSize', parseInt(e.target.value))}
                                      className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                      style={{ 
                                        backgroundColor: themeColors.card,
                                        borderColor: themeColors.border,
                                        color: themeColors.text
                                      }}
                                      min="1"
                                      max="1000"
                                    />
                                  </div>

                                  <div className="space-y-2">
                                    <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                      Enable Sorting
                                    </label>
                                    <div className="flex items-center">
                                      <input
                                        type="checkbox"
                                        checked={schemaConfig.enableSorting}
                                        onChange={(e) => handleSchemaConfigChange('enableSorting', e.target.checked)}
                                        className="h-4 w-4 rounded"
                                        style={{ accentColor: themeColors.info }}
                                      />
                                      <span className="ml-2 text-xs" style={{ color: themeColors.textSecondary }}>Yes</span>
                                    </div>
                                  </div>

                                  <div className="space-y-2">
                                    <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                      Default Sort Column
                                    </label>
                                    <input
                                      type="text"
                                      value={schemaConfig.defaultSortColumn}
                                      onChange={(e) => handleSchemaConfigChange('defaultSortColumn', e.target.value)}
                                      className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                      style={{ 
                                        backgroundColor: themeColors.card,
                                        borderColor: themeColors.border,
                                        color: themeColors.text
                                      }}
                                      placeholder="CREATED_DATE"
                                    />
                                  </div>
                                </div>

                                <div className="space-y-2">
                                  <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                    Default Sort Direction
                                  </label>
                                  <select
                                    value={schemaConfig.defaultSortDirection}
                                    onChange={(e) => handleSchemaConfigChange('defaultSortDirection', e.target.value)}
                                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                  >
                                    <option value="ASC">Ascending (ASC)</option>
                                    <option value="DESC">Descending (DESC)</option>
                                  </select>
                                </div>
                              </div>
                            </div>

                            {/* Database Type Selector - Pre-populated from selected object */}
                            <div className="mt-6 p-4 rounded-lg border" style={{ 
                              borderColor: themeColors.border,
                              backgroundColor: themeColors.card
                            }}>
                              <label className="text-xs font-medium flex items-center gap-2 mb-3" style={{ color: themeColors.text }}>
                                <Database className="h-4 w-4" />
                                Database Type
                              </label>
                              <div className="flex gap-4">
                                <label className="flex items-center gap-2">
                                  <input
                                    type="radio"
                                    value="oracle"
                                    checked={currentDatabaseType === 'oracle'}
                                    onChange={(e) => {
                                      setCurrentDatabaseType(e.target.value);
                                      // Also update the selectedDbObject's database type if it exists
                                      if (selectedDbObject) {
                                        setSelectedDbObject(prev => ({ ...prev, databaseType: e.target.value }));
                                      }
                                    }}
                                    className="h-4 w-4"
                                    style={{ accentColor: '#ef4444' }}
                                  />
                                  <span className="text-sm" style={{ color: themeColors.text }}>Oracle</span>
                                </label>
                                <label className="flex items-center gap-2">
                                  <input
                                    type="radio"
                                    value="postgresql"
                                    checked={currentDatabaseType === 'postgresql'}
                                    onChange={(e) => {
                                      setCurrentDatabaseType(e.target.value);
                                      // Also update the selectedDbObject's database type if it exists
                                      if (selectedDbObject) {
                                        setSelectedDbObject(prev => ({ ...prev, databaseType: e.target.value }));
                                      }
                                    }}
                                    className="h-4 w-4"
                                    style={{ accentColor: '#3b82f6' }}
                                  />
                                  <span className="text-sm" style={{ color: themeColors.text }}>PostgreSQL</span>
                                </label>
                              </div>
                              <p className="text-xs mt-2" style={{ color: themeColors.textSecondary }}>
                                {currentDatabaseType === 'postgresql' 
                                  ? 'API will be optimized for PostgreSQL syntax and features' 
                                  : 'API will be optimized for Oracle PL/SQL syntax and features'}
                              </p>
                            </div>

                            {/* Object Info Summary */}
                            <div className="mt-4 p-3 rounded-lg border" style={{ 
                              borderColor: themeColors.info + '40',
                              backgroundColor: themeColors.info + '10'
                            }}>
                              <h4 className="text-xs font-medium mb-2 flex items-center gap-2" style={{ color: themeColors.info }}>
                                <Info className="h-3 w-3" />
                                Object Summary
                              </h4>
                              <div className="grid grid-cols-2 gap-2 text-xs">
                                <div>
                                  <span style={{ color: themeColors.textSecondary }}>Full Path:</span>
                                  <span className="ml-2 font-mono" style={{ color: themeColors.text }}>
                                    {schemaConfig.schemaName}.{schemaConfig.objectName}
                                  </span>
                                </div>
                                <div>
                                  <span style={{ color: themeColors.textSecondary }}>Operation:</span>
                                  <span className="ml-2 font-mono" style={{ color: themeColors.text }}>
                                    {schemaConfig.operation}
                                  </span>
                                </div>
                                {schemaConfig.primaryKeyColumn && (
                                  <div>
                                    <span style={{ color: themeColors.textSecondary }}>Primary Key:</span>
                                    <span className="ml-2 font-mono" style={{ color: themeColors.text }}>
                                      {schemaConfig.primaryKeyColumn}
                                    </span>
                                  </div>
                                )}
                                {schemaConfig.enablePagination && (
                                  <div>
                                    <span style={{ color: themeColors.textSecondary }}>Pagination:</span>
                                    <span className="ml-2 font-mono" style={{ color: themeColors.text }}>
                                      Page Size: {schemaConfig.pageSize}
                                    </span>
                                  </div>
                                )}
                              </div>
                            </div>
                          </>
                        )}
                      </>
                    )}

                    {/* Custom Query Section */}
                    {sourceType === 'custom_query' && (
                      <div className="space-y-4">
                        <div className="flex items-center justify-between">
                          <label className="text-xs font-medium flex items-center gap-2" style={{ color: themeColors.text }}>
                            <Wand2 size={14} style={{ color: themeColors.info }} />
                            Custom SQL Statement
                            {!customQuery?.trim() && validationErrors.customQuery && (
                              <span className="text-xs" style={{ color: themeColors.error }}>(Required)</span>
                            )}
                          </label>
                          
                          {isEditingCustomQuery && (
                            <span className="text-xs px-2 py-1 rounded-full" style={{
                              backgroundColor: themeColors.warning + '20',
                              color: themeColors.warning
                            }}>
                              <Edit className="h-3 w-3 inline mr-1" />
                              Editing Mode
                            </span>
                          )}
                        </div>
                        
                        <textarea
                          value={customQuery}
                          onChange={(e) => setCustomQuery(e.target.value)}
                          className="w-full px-3 py-3 rounded-lg text-sm font-mono focus:outline-none focus:ring-2 focus:ring-opacity-50"
                          style={{ 
                            backgroundColor: themeColors.codeBg || (theme === 'dark' ? '#1a202c' : '#f8fafc'),
                            border: `1px solid ${validationErrors.customQuery ? themeColors.error : themeColors.border}`,
                            color: themeColors.text,
                            fontFamily: 'monospace',
                            fontSize: '13px',
                            lineHeight: '1.5',
                            minHeight: '120px'
                          }}
                          placeholder="SELECT * FROM your_table WHERE column = :paramName"
                          spellCheck={false}
                        />
                        
                        <div className="flex items-center justify-between">
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                            Use :paramName syntax for parameters (e.g., WHERE id = :userId)
                          </p>
                          
                          <button
                            onClick={() => {
                              // Extract parameters from the custom query
                              const paramMatches = customQuery.match(/:\w+/g) || [];
                              const uniqueParams = [...new Set(paramMatches)];
                              const newParams = uniqueParams.map((param, idx) => ({
                                id: `param-${Date.now()}-${idx}`,
                                key: param.substring(1),
                                dbColumn: param.substring(1),
                                oracleType: 'VARCHAR2',
                                apiType: 'string',
                                parameterLocation: 'query',
                                required: true,
                                description: `Parameter: ${param.substring(1)}`,
                                example: '',
                                validationPattern: '',
                                defaultValue: '',
                                inBody: false,
                                isPrimaryKey: false,
                                paramMode: 'IN'
                              }));
                              setParameters(newParams);
                            }}
                            className="text-xs px-3 py-1 rounded-lg flex items-center gap-1 transition-colors hover-lift"
                            style={{ 
                              backgroundColor: themeColors.info + '20',
                              color: themeColors.info,
                              border: `1px solid ${themeColors.info + '40'}`
                            }}
                          >
                            <RefreshCw className="h-3 w-3" />
                            Extract Parameters
                          </button>
                        </div>
                        
                        {/* Query Preview */}
                        {customQuery && (
                          <div className="p-3 rounded-lg border" style={{ 
                            borderColor: themeColors.success + '40',
                            backgroundColor: themeColors.success + '10'
                          }}>
                            <div className="flex items-center gap-2 mb-2">
                              <CheckCircle className="h-3 w-3" style={{ color: themeColors.success }} />
                              <span className="text-xs font-medium" style={{ color: themeColors.success }}>Query Analysis</span>
                            </div>
                            <div className="grid grid-cols-2 gap-2 text-xs">
                              <div>
                                <span style={{ color: themeColors.textSecondary }}>Tables referenced:</span>
                                <span className="ml-2 font-mono" style={{ color: themeColors.text }}>
                                  {(customQuery.match(/FROM\s+(\w+)/i) || []).slice(1).join(', ') || 'Unknown'}
                                </span>
                              </div>
                              <div>
                                <span style={{ color: themeColors.textSecondary }}>Parameters found:</span>
                                <span className="ml-2 font-mono" style={{ color: themeColors.text }}>
                                  {(customQuery.match(/:\w+/g) || []).length}
                                </span>
                              </div>
                              <div className="col-span-2">
                                <span style={{ color: themeColors.textSecondary }}>Estimated complexity:</span>
                                <span className="ml-2 font-mono" style={{ color: themeColors.text }}>
                                  {customQuery.split(/\s+/).length > 50 ? 'Complex' : 
                                  customQuery.split(/\s+/).length > 20 ? 'Moderate' : 'Simple'}
                                </span>
                              </div>
                            </div>
                          </div>
                        )}
                        
                        {/* Database Type for Custom Query */}
                        <div className="mt-4 p-4 rounded-lg border" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <label className="text-xs font-medium flex items-center gap-2 mb-3" style={{ color: themeColors.text }}>
                            <Database className="h-4 w-4" />
                            Database Type for Custom Query
                          </label>
                          <div className="flex gap-4">
                            <label className="flex items-center gap-2">
                              <input
                                type="radio"
                                value="oracle"
                                checked={currentDatabaseType === 'oracle'}
                                onChange={(e) => setCurrentDatabaseType(e.target.value)}
                                className="h-4 w-4"
                                style={{ accentColor: '#ef4444' }}
                              />
                              <span className="text-sm" style={{ color: themeColors.text }}>Oracle</span>
                            </label>
                            <label className="flex items-center gap-2">
                              <input
                                type="radio"
                                value="postgresql"
                                checked={currentDatabaseType === 'postgresql'}
                                onChange={(e) => setCurrentDatabaseType(e.target.value)}
                                className="h-4 w-4"
                                style={{ accentColor: '#3b82f6' }}
                              />
                              <span className="text-sm" style={{ color: themeColors.text }}>PostgreSQL</span>
                            </label>
                          </div>
                          <p className="text-xs mt-2" style={{ color: themeColors.textSecondary }}>
                            {currentDatabaseType === 'postgresql' 
                              ? 'Query will be executed against PostgreSQL database' 
                              : 'Query will be executed against Oracle database'}
                          </p>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* Parameters Tab - Show only IN parameters */}
                {activeTab === 'parameters' && (
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                        Input Parameters ({getInParameters().length})
                      </h3>
                      <button
                        onClick={handleAddParameter}
                        className="px-3 py-1.5 rounded-lg flex items-center gap-2 text-xs transition-colors hover-lift"
                        style={{ backgroundColor: themeColors.info, color: themeColors.white }}
                      >
                        <Plus className="h-4 w-4" />
                        Add Parameter
                      </button>
                    </div>

                    {getInParameters().length === 0 ? (
                      <div className="text-center py-8 border rounded-lg" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <Code className="h-12 w-12 mx-auto mb-3" style={{ color: themeColors.textSecondary }} />
                        <p style={{ color: themeColors.textSecondary }}>
                          No input parameters defined. Add parameters or they will be auto-generated from the selected object.
                        </p>
                        {parameters.some(p => p.paramMode === 'OUT') && (
                          <p className="text-xs mt-2" style={{ color: themeColors.info }}>
                            Note: OUT parameters are not shown here - they appear in the Mapping tab.
                          </p>
                        )}
                      </div>
                    ) : (
                      <div className="overflow-x-auto border rounded-lg" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <table className="w-full min-w-[1200px]">
                          <thead>
                            <tr style={{ backgroundColor: themeColors.hover }}>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>Parameter Key</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>DB Column</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>DB Data Type</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>API Data Type</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>Location</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>Required</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>Example</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>Mode</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>Actions</th>
                            </tr>
                          </thead>
                          <tbody>
                            {getInParameters().map((param, index) => (
                              <tr key={param.id} style={{ 
                                backgroundColor: index % 2 === 0 ? themeColors.card : themeColors.hover,
                                borderBottom: `1px solid ${themeColors.border}`
                              }}>
                                <td className="px-3 py-2">
                                  <input
                                    type="text"
                                    value={param.key}
                                    onChange={(e) => handleParameterChange(param.id, 'key', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.card,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                    placeholder="parameter_key"
                                  />
                                </td>
                                <td className="px-3 py-2">
                                  <input
                                    type="text"
                                    value={param.dbColumn || ''}
                                    onChange={(e) => handleParameterChange(param.id, 'dbColumn', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.card,
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
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                  >
                                    {ORACLE_DATA_TYPES.map(type => (
                                      <option key={type} value={type}>{type}</option>
                                    ))}
                                  </select>
                                </td>
                                <td className="px-3 py-2">
                                  <select
                                    value={param.apiType || 'string'}
                                    onChange={(e) => handleParameterChange(param.id, 'apiType', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                  >
                                    {API_DATA_TYPES.map(type => (
                                      <option key={type} value={type.toLowerCase()}>
                                        {type}
                                      </option>
                                    ))}
                                  </select>
                                </td>
                                <td className="px-3 py-2">
                                  <select
                                    value={param.parameterLocation}
                                    onChange={(e) => handleParameterChange(param.id, 'parameterLocation', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                  >
                                    {protocolType === 'soap' ? (
                                      // SOAP: everything goes in the envelope body
                                      <option value="body">Body (Envelope)</option>
                                    ) : protocolType === 'graphql' ? (
                                      // GraphQL: arguments inline or input object
                                      <>
                                        <option value="query">Argument (inline)</option>
                                        <option value="body">Input Object</option>
                                      </>
                                    ) : (
                                      // REST: all 4 locations
                                      PARAMETER_LOCATIONS.map(loc => (
                                        <option key={loc.value} value={loc.value}>
                                          {loc.label}
                                        </option>
                                      ))
                                    )}
                                  </select>
                                </td>
                                <td className="px-3 py-2 text-center">
                                  <input
                                    type="checkbox"
                                    checked={param.required}
                                    onChange={(e) => handleParameterChange(param.id, 'required', e.target.checked)}
                                    className="h-4 w-4 rounded"
                                    style={{ accentColor: themeColors.info }}
                                    disabled={param.parameterLocation === 'path'}
                                  />
                                  {param.parameterLocation === 'path' && (
                                    <div className="text-xs" style={{ color: themeColors.textSecondary }}>(always required)</div>
                                  )}
                                </td>
                                <td className="px-3 py-2">
                                  <input
                                    type="text"
                                    value={param.example || ''}
                                    onChange={(e) => handleParameterChange(param.id, 'example', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{
                                      backgroundColor: themeColors.card,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                    placeholder={param.parameterLocation === 'path' ? 'e.g., 123' : 'Example'}
                                  />
                                </td>
                                <td className="px-3 py-2">
                                  <select
                                    value={param.paramMode === 'IN' ? 'IN' : 'IN OUT'}
                                    onChange={(e) => handleParameterChange(param.id, 'paramMode', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                  >
                                    <option value="IN">IN</option>
                                    <option value="IN OUT">IN OUT</option>
                                  </select>
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

                {/* Mapping Tab - Show all response fields including OUT parameters */}
                {activeTab === 'mapping' && (
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                        Response Field Mapping ({getOutMappings().length})
                      </h3>
                      <button
                        onClick={handleAddResponseMapping}
                        className="px-3 py-1.5 rounded-lg flex items-center gap-2 text-xs transition-colors hover-lift"
                        style={{ backgroundColor: themeColors.info, color: themeColors.white }}
                      >
                        <Plus className="h-4 w-4" />
                        Add Mapping
                      </button>
                    </div>

                    {getOutMappings().length === 0 ? (
                      <div className="text-center py-8 border rounded-lg" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <Map className="h-12 w-12 mx-auto mb-3" style={{ color: themeColors.textSecondary }} />
                        <p style={{ color: themeColors.textSecondary }}>
                          No response fields defined. They will be auto-generated from the selected object's columns/OUT parameters.
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
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>API Field</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>DB Column</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Data Type</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>API Type</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Nullable</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>PK</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Mode</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Actions</th>
                            </tr>
                          </thead>
                          <tbody>
                            {getOutMappings().map((mapping, index) => (
                              <tr key={mapping.id} style={{ 
                                backgroundColor: index % 2 === 0 ? themeColors.card : themeColors.hover,
                                borderBottom: `1px solid ${themeColors.border}`
                              }}>
                                <td className="px-3 py-2">
                                  <input
                                    type="text"
                                    value={mapping.apiField}
                                    onChange={(e) => handleResponseMappingChange(mapping.id, 'apiField', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.card,
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
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.card,
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
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                  >
                                    {ORACLE_DATA_TYPES.map(type => (
                                      <option key={type} value={type}>{type}</option>
                                    ))}
                                  </select>
                                </td>
                                <td className="px-3 py-2">
                                  <select
                                    value={mapping.apiType}
                                    onChange={(e) => handleResponseMappingChange(mapping.id, 'apiType', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                  >
                                    {API_DATA_TYPES.map(type => (
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
                                <td className="px-3 py-2 text-center">
                                  <input
                                    type="checkbox"
                                    checked={mapping.isPrimaryKey}
                                    onChange={(e) => handleResponseMappingChange(mapping.id, 'isPrimaryKey', e.target.checked)}
                                    className="h-4 w-4 rounded"
                                    style={{ accentColor: themeColors.warning }}
                                  />
                                </td>
                                <td className="px-3 py-2">
                                  <select
                                    value={mapping.paramMode === 'OUT' ? 'OUT' : 'IN OUT'}
                                    onChange={(e) => handleResponseMappingChange(mapping.id, 'paramMode', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                  >
                                    <option value="">-</option>
                                    <option value="OUT">OUT</option>
                                    <option value="IN OUT">IN OUT</option>
                                  </select>
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
                    
                    {/* Show note about IN parameters */}
                    {getInParameters().length > 0 && (
                      <div className="mb-3 p-2 rounded-lg text-xs" style={{ 
                        backgroundColor: themeColors.info + '10',
                        borderLeft: `3px solid ${themeColors.info}`
                      }}>
                        <span className="font-medium" style={{ color: themeColors.info }}>Note:</span>
                        <span style={{ color: themeColors.textSecondary }}>
                          Path parameters (location = "path") are automatically marked as required and cannot be changed.
                        </span>
                      </div>
                    )}
                  </div>
                )}

                {/* SOAP Configuration Tab */}
                {activeTab === 'soap' && protocolType === 'soap' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      SOAP Web Service Configuration
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                            Binding Style
                          </label>
                          <select
                            value={soapConfig.bindingStyle}
                            onChange={(e) => handleSoapConfigChange('bindingStyle', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.bg,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          >
                            {SOAP_BINDINGS.map(b => (
                              <option key={b.value} value={b.value}>{b.label}</option>
                            ))}
                          </select>
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                            Document style uses XML schema, RPC style uses method signature
                          </p>
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                            Encoding Style
                          </label>
                          <select
                            value={soapConfig.encodingStyle}
                            onChange={(e) => handleSoapConfigChange('encodingStyle', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.bg,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          >
                            {SOAP_ENCODING_STYLES.map(e => (
                              <option key={e.value} value={e.value}>{e.label}</option>
                            ))}
                          </select>
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                            Literal uses XML schema types, Encoded uses SOAP encoding rules
                          </p>
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                            Service Name *
                            <Asterisk className="h-3 w-3" style={{ color: themeColors.error }} />
                            <span className="text-xs ml-2 px-1.5 py-0.5 rounded" style={{ 
                              backgroundColor: themeColors.info + '20',
                              color: themeColors.info
                            }}>
                              Auto-generated from object
                            </span>
                          </label>
                          <input
                            type="text"
                            value={soapConfig.serviceName}
                            onChange={(e) => handleSoapConfigChange('serviceName', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: validationErrors.serviceName ? themeColors.error : themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="UserService"
                          />
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                            Service name is auto-generated from your database object. You can edit it as needed.
                          </p>
                          {validationErrors.serviceName && (
                            <p className="text-xs mt-1" style={{ color: themeColors.error }}>
                              {validationErrors.serviceName}
                            </p>
                          )}
                        </div>
                      </div>

                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Port Name
                          </label>
                          <input
                            type="text"
                            value={soapConfig.portName}
                            onChange={(e) => handleSoapConfigChange('portName', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="UserServicePort"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Namespace
                          </label>
                          <input
                            type="text"
                            value={soapConfig.namespace}
                            onChange={(e) => handleSoapConfigChange('namespace', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="http://tempuri.org/"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            WSDL URL
                          </label>
                          <input
                            type="text"
                            value={soapConfig.wsdlUrl}
                            onChange={(e) => handleSoapConfigChange('wsdlUrl', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="https://api.example.com/service?wsdl"
                          />
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                            URL where the WSDL will be published
                          </p>
                        </div>

                        <div className="space-y-3">
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={soapConfig.useAsyncPattern}
                              onChange={(e) => handleSoapConfigChange('useAsyncPattern', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Use Async Pattern
                            </span>
                          </div>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={soapConfig.includeMtom}
                              onChange={(e) => handleSoapConfigChange('includeMtom', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Include MTOM (Binary Attachments)
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* SOAP Envelope Preview */}
                    <div className="space-y-4">
                      <h4 className="font-semibold" style={{ color: themeColors.text }}>
                        SOAP Envelope Preview
                      </h4>
                      <div className="border rounded-lg" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <pre className="w-full h-64 px-4 py-3 overflow-auto text-xs font-mono" style={{ 
                          backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                          color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                        }}>
                          {generateSOAPEnvelope()}
                        </pre>
                      </div>
                    </div>
                  </div>
                )}

                {/* GraphQL Configuration Tab */}
                {activeTab === 'graphql' && protocolType === 'graphql' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      GraphQL API Configuration
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                            Operation Name *
                            <Asterisk className="h-3 w-3" style={{ color: themeColors.error }} />
                            <span className="text-xs ml-2 px-1.5 py-0.5 rounded" style={{ 
                              backgroundColor: themeColors.info + '20',
                              color: themeColors.info
                            }}>
                              Auto-generated from object
                            </span>
                          </label>
                          <input
                            type="text"
                            value={graphqlConfig.operationName}
                            onChange={(e) => handleGraphqlConfigChange('operationName', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: validationErrors.operationName ? themeColors.error : themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="getUsers"
                          />
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                            Operation name is auto-generated from your database object. You can edit it as needed.
                          </p>
                          {validationErrors.operationName && (
                            <p className="text-xs mt-1" style={{ color: themeColors.error }}>
                              {validationErrors.operationName}
                            </p>
                          )}
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Max Query Depth
                          </label>
                          <input
                            type="number"
                            value={graphqlConfig.maxQueryDepth}
                            onChange={(e) => handleGraphqlConfigChange('maxQueryDepth', parseInt(e.target.value))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            min="1"
                            max="50"
                          />
                          <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                            Maximum nesting depth to prevent complex queries
                          </p>
                        </div>

                        <div className="space-y-3">
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={graphqlConfig.enableIntrospection}
                              onChange={(e) => handleGraphqlConfigChange('enableIntrospection', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Introspection
                            </span>
                          </div>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={graphqlConfig.enablePersistedQueries}
                              onChange={(e) => handleGraphqlConfigChange('enablePersistedQueries', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Persisted Queries
                            </span>
                          </div>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={graphqlConfig.enableBatching}
                              onChange={(e) => handleGraphqlConfigChange('enableBatching', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Request Batching
                            </span>
                          </div>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={graphqlConfig.subscriptionsEnabled}
                              onChange={(e) => handleGraphqlConfigChange('subscriptionsEnabled', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Subscriptions (WebSocket)
                            </span>
                          </div>
                        </div>
                      </div>

                      {/* GraphQL Schema with auto-generation option */}
                      <div className="space-y-2">
                        <div className="flex items-center justify-between">
                          <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                            GraphQL Schema *
                            <Asterisk className="h-3 w-3" style={{ color: themeColors.error }} />
                          </label>
                          <button
                            onClick={() => {
                              const autoSchema = generateGraphQLSchemaFromObject();
                              setGraphqlConfig(prev => ({ ...prev, schema: autoSchema }));
                            }}
                            className="text-xs px-2 py-1 rounded flex items-center gap-1 transition-colors hover-lift"
                            style={{ 
                              backgroundColor: themeColors.info + '20',
                              color: themeColors.info,
                              border: `1px solid ${themeColors.info + '40'}`
                            }}
                          >
                            <Sparkles className="h-3 w-3" />
                            Auto-Generate from Object
                          </button>
                        </div>
                        <textarea
                          key={`graphql-request-${parameters.length}-${responseMappings.length}`}
                          value={(() => {
                            // Force regeneration when parameters or mappings change
                            if (protocolType === 'graphql') {
                              const inParams = parameters.filter(p => !p.paramMode || p.paramMode === 'IN');
                              const outMappingsForQuery = responseMappings.filter(m => 
                                (m.paramMode === 'OUT' || m.paramMode === 'IN/OUT' || m.paramMode === 'IN_OUT' || !m.paramMode)
                              );
                              
                              const queryParams = inParams;
                              let variableDefinitions = '';
                              let variableUsages = '';
                              let variablesObject = {};
                              
                              if (queryParams.length > 0) {
                                variableDefinitions = '(' + queryParams.map(p => {
                                  const graphqlType = getGraphQLType(p.oracleType, !p.required);
                                  return `$${p.key}: ${graphqlType}`;
                                }).join(', ') + ')';
                                
                                variableUsages = '(' + queryParams.map(p => `${p.key}: $${p.key}`).join(', ') + ')';
                                
                                queryParams.forEach(p => {
                                  let val = p.example;
                                  if (!val) {
                                    if (p.apiType === 'integer') val = 123;
                                    else if (p.apiType === 'boolean') val = true;
                                    else val = 'sample';
                                  }
                                  if (p.apiType === 'integer') val = parseInt(val) || 123;
                                  if (p.apiType === 'boolean') val = val === 'true';
                                  variablesObject[p.key] = val;
                                });
                              }
                              
                              let queryString = `${graphqlConfig.operationType || 'query'} ${graphqlConfig.operationName || 'query'}${variableDefinitions} {\n  ${graphqlConfig.operationName || 'query'}${variableUsages} {\n`;
                              if (outMappingsForQuery.length > 0) {
                                queryString += outMappingsForQuery.slice(0, 15).map(m => `    ${m.apiField}`).join('\n');
                                if (outMappingsForQuery.length > 15) {
                                  queryString += `\n    # ... ${outMappingsForQuery.length - 15} more fields`;
                                }
                              } else {
                                queryString += `    id\n    createdAt\n    updatedAt`;
                              }
                              queryString += `\n  }\n}`;
                              
                              return JSON.stringify({
                                query: queryString,
                                variables: variablesObject
                              }, null, 2);
                            }
                            return requestBody.sample || '';
                          })()}
                          onChange={(e) => handleRequestBodyChange('sample', e.target.value)}
                          className="w-full h-96 px-4 py-3 text-xs font-mono resize-none focus:outline-none"
                          style={{ 
                            backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                            color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                          }}
                        />
                        <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                          Define your GraphQL schema using SDL (Schema Definition Language). 
                          Click "Auto-Generate" to create a schema from your database object.
                        </p>
                        {validationErrors.graphqlSchema && (
                          <p className="text-xs mt-1" style={{ color: themeColors.error }}>
                            {validationErrors.graphqlSchema}
                          </p>
                        )}
                      </div>
                    </div>

                    {/* GraphQL Schema Preview */}
                    <div className="space-y-4">
                      <h4 className="font-semibold" style={{ color: themeColors.text }}>
                        Generated GraphQL Schema
                      </h4>
                      <div className="border rounded-lg" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <pre className="w-full h-64 px-4 py-3 overflow-auto text-xs font-mono" style={{ 
                          backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                          color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                        }}>
                          {generateGraphQLSchema()}
                        </pre>
                      </div>
                    </div>
                  </div>
                )}

                {/* Authentication Tab - Simplified to 4 types */}
                {activeTab === 'auth' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      Authentication
                    </h3>

                    {/* Auth Type Selection */}
                    <div className="mb-6">
                      <label className="block text-sm font-small mb-2" style={{ color: themeColors.text }}>
                        Authentication Type
                      </label>
                      <div className="relative">
                        <select
                          value={authConfig.authType}
                          onChange={(e) => handleAuthConfigChange('authType', e.target.value)}
                          className="w-full text-sm p-3 rounded-lg border-2 appearance-none cursor-pointer"
                          style={{
                            backgroundColor: themeColors.bg,
                            borderColor: themeColors.border,
                            color: themeColors.text,
                            paddingRight: '2.5rem'
                          }}
                        >
                          {AUTH_TYPES.map(type => (
                            <option key={type.value} value={type.value}>
                              {type.label} - {type.description}
                            </option>
                          ))}
                        </select>
                      </div>
                    </div>

                    {/* No Auth - Public API */}
                    {authConfig.authType === 'none' && (
                      <div className="p-4 rounded-lg border" style={{ 
                        borderColor: themeColors.warning + '40',
                        backgroundColor: themeColors.warning + '10'
                      }}>
                        <div className="flex items-start gap-3">
                          <Unlock className="h-5 w-5" style={{ color: themeColors.warning }} />
                          <div>
                            <h4 className="font-medium" style={{ color: themeColors.warning }}>
                              Public API - No Authentication
                            </h4>
                            <p className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>
                              This API will be publicly accessible without any authentication. 
                              Only use for non-sensitive data.
                            </p>
                          </div>
                        </div>
                      </div>
                    )}

                    {/* API Key + Secret */}
                    {authConfig.authType === 'apiKey' && (
                      <div className="p-4 rounded-lg border" style={{ 
                        borderColor: themeColors.info + '40',
                        backgroundColor: themeColors.info + '10'
                      }}>
                        <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.info }}>
                          <Key className="h-5 w-5" />
                          API Key + Secret Configuration
                        </h4>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                          <div className="space-y-4">
                            {renderRequiredInput(
                              'apiKeyHeader',
                              'API Key Header',
                              authConfig.apiKeyHeader,
                              handleAuthConfigChange,
                              'X-API-Key'
                            )}
                            <div className="space-y-2">
                              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                API Key Value (for testing)
                              </label>
                              <input
                                type="text"
                                value={authConfig.apiKeyValue}
                                onChange={(e) => handleAuthConfigChange('apiKeyValue', e.target.value)}
                                className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.card,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                                placeholder="your-api-key"
                              />
                              <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                                This will be included in generated Postman collection
                              </p>
                            </div>
                          </div>
                          <div className="space-y-4">
                            {renderRequiredInput(
                              'apiSecretHeader',
                              'API Secret Header',
                              authConfig.apiSecretHeader,
                              handleAuthConfigChange,
                              'X-API-Secret'
                            )}
                            <div className="space-y-2">
                              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                API Secret Value (for testing)
                              </label>
                              <div className="relative">
                                <input
                                  type={showApiSecret ? 'text' : 'password'}
                                  value={authConfig.apiSecretValue}
                                  onChange={(e) => handleAuthConfigChange('apiSecretValue', e.target.value)}
                                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift pr-10"
                                  style={{ 
                                    backgroundColor: themeColors.card,
                                    borderColor: themeColors.border,
                                    color: themeColors.text
                                  }}
                                  placeholder="your-api-secret"
                                />
                                <button
                                  type="button"
                                  onClick={() => setShowApiSecret(!showApiSecret)}
                                  className="absolute right-2 top-1/2 transform -translate-y-1/2 p-1 rounded transition-colors hover-lift"
                                  style={{ 
                                    backgroundColor: 'transparent',
                                    color: themeColors.textSecondary
                                  }}
                                  title={showApiSecret ? 'Hide secret' : 'Show secret'}
                                >
                                  {showApiSecret ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                                </button>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Bearer Token (JWT) */}
                    {authConfig.authType === 'bearer' && (
                      <div className="p-4 rounded-lg border" style={{ 
                        borderColor: themeColors.info + '40',
                        backgroundColor: themeColors.info + '10'
                      }}>
                        <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.info }}>
                          <Shield className="h-5 w-5" />
                          Bearer Token (JWT) Configuration
                        </h4>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                          <div className="space-y-4">
                            <div className="space-y-2">
                              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                JWT Token (for testing)
                              </label>
                              <textarea
                                value={authConfig.jwtToken}
                                onChange={(e) => handleAuthConfigChange('jwtToken', e.target.value)}
                                className="w-full px-3 py-2 border rounded-lg text-xs font-mono hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.card,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                                rows="3"
                                placeholder="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                              />
                              <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                                This will be included in generated Postman collection
                              </p>
                            </div>
                          </div>
                          <div className="space-y-4">
                            {renderRequiredInput(
                              'jwtIssuer',
                              'JWT Issuer',
                              authConfig.jwtIssuer,
                              handleAuthConfigChange,
                              'api.example.com'
                            )}
                            <div className="text-xs p-3 rounded" style={{ backgroundColor: themeColors.hover }}>
                              <p style={{ color: themeColors.textSecondary }}>
                                <strong>Header format:</strong> Authorization: Bearer {'{token}'}
                              </p>
                            </div>
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Basic Auth */}
                    {authConfig.authType === 'basic' && (
                      <div className="p-4 rounded-lg border" style={{ 
                        borderColor: themeColors.info + '40',
                        backgroundColor: themeColors.info + '10'
                      }}>
                        <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.info }}>
                          <Lock className="h-5 w-5" />
                          Basic Authentication Configuration
                        </h4>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                          <div className="space-y-4">
                            <div className="space-y-2">
                              <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                                Username <span style={{ color: "red" }}>*</span>
                              </label>
                              <input
                                type="text"
                                value={authConfig.basicUsername}
                                onChange={(e) => handleAuthConfigChange('basicUsername', e.target.value)}
                                className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.card,
                                  borderColor: authConfig.basicUsername ? themeColors.success : themeColors.border,
                                  color: themeColors.text
                                }}
                                placeholder="Enter username"
                              />
                              <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                                Will be used for Basic authentication
                              </p>
                            </div>
                          </div>
                          <div className="space-y-4">
                            <div className="space-y-2">
                              <label className="text-xs font-medium flex items-center gap-1" style={{ color: themeColors.text }}>
                                Password <span style={{ color: "red" }}>*</span>
                              </label>
                              <div className="relative">
                                <input
                                  type={showBasicPassword ? 'text' : 'password'}
                                  value={authConfig.basicPassword}
                                  onChange={(e) => handleAuthConfigChange('basicPassword', e.target.value)}
                                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift pr-10"
                                  style={{ 
                                    backgroundColor: themeColors.card,
                                    borderColor: authConfig.basicPassword ? themeColors.success : themeColors.border,
                                    color: themeColors.text
                                  }}
                                  placeholder="••••••••"
                                />
                                <button
                                  type="button"
                                  onClick={() => setShowBasicPassword(!showBasicPassword)}
                                  className="absolute right-2 top-1/2 transform -translate-y-1/2 p-1 rounded transition-colors hover-lift"
                                  style={{ 
                                    backgroundColor: 'transparent',
                                    color: themeColors.textSecondary
                                  }}
                                  title={showBasicPassword ? 'Hide password' : 'Show password'}
                                >
                                  {showBasicPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                                </button>
                              </div>
                              <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                                Will be sent as: Basic base64(username:password)
                              </p>
                            </div>
                          </div>
                        </div>
                        <div className="mt-4 p-3 rounded" style={{ backgroundColor: themeColors.hover }}>
                          <p className="text-xs" style={{ color: themeColors.warning }}>
                            <AlertCircle className="h-3 w-3 inline mr-1" />
                            Basic Auth sends credentials in plaintext. Always use HTTPS.
                          </p>
                          <p className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>
                            Header format: Authorization: Basic base64(username:password)
                          </p>
                        </div>
                      </div>
                    )}

                    {/* Common Security Settings (for all auth types) */}
                    {/* <div className="p-4 rounded-lg border mt-6" style={{ 
                      borderColor: themeColors.border,
                      backgroundColor: themeColors.card
                    }}>
                      <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.text }}>
                        <Shield className="h-5 w-5" />
                        Additional Security Settings
                      </h4>
                      
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="space-y-4">
                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              IP Whitelist
                            </label>
                            <textarea
                              value={authConfig.ipWhitelist}
                              onChange={(e) => handleAuthConfigChange('ipWhitelist', e.target.value)}
                              className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                              rows="2"
                              placeholder="192.168.1.0/24&#10;10.0.0.1"
                            />
                            <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                              One CIDR or IP per line (leave empty to allow all)
                            </p>
                          </div>

                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Rate Limiting
                            </label>
                            <div className="flex items-center mb-2">
                              <input
                                type="checkbox"
                                checked={authConfig.enableRateLimiting}
                                onChange={(e) => handleAuthConfigChange('enableRateLimiting', e.target.checked)}
                                className="h-4 w-4 rounded mr-2"
                                style={{ accentColor: themeColors.info }}
                              />
                              <span className="text-xs" style={{ color: themeColors.text }}>
                                Enable Rate Limiting
                              </span>
                            </div>
                            {authConfig.enableRateLimiting && (
                              <div className="grid grid-cols-2 gap-2">
                                <input
                                  type="number"
                                  placeholder="Requests"
                                  value={authConfig.rateLimitRequests}
                                  onChange={(e) => handleAuthConfigChange('rateLimitRequests', parseInt(e.target.value))}
                                  className="px-3 py-2 border rounded-lg text-xs hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.card,
                                    borderColor: themeColors.border,
                                    color: themeColors.text
                                  }}
                                  min="1"
                                />
                                <select
                                  value={authConfig.rateLimitPeriod}
                                  onChange={(e) => handleAuthConfigChange('rateLimitPeriod', e.target.value)}
                                  className="px-3 py-2 border rounded-lg text-xs hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.bg,
                                    borderColor: themeColors.border,
                                    color: themeColors.text
                                  }}
                                >
                                  <option value="second">per second</option>
                                  <option value="minute">per minute</option>
                                  <option value="hour">per hour</option>
                                  <option value="day">per day</option>
                                </select>
                              </div>
                            )}
                          </div>
                        </div>

                        <div className="space-y-4">
                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              CORS Origins
                            </label>
                            <input
                              type="text"
                              placeholder="https://example.com, https://api.example.com"
                              value={authConfig.corsOrigins?.join(', ') || '*'}
                              onChange={(e) => handleAuthConfigChange('corsOrigins', e.target.value.split(',').map(o => o.trim()))}
                              className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                            />
                          </div>

                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Audit Level
                            </label>
                            <select
                              value={authConfig.auditLevel}
                              onChange={(e) => handleAuthConfigChange('auditLevel', e.target.value)}
                              className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                              style={{ 
                                backgroundColor: themeColors.bg,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                            >
                              <option value="none">None</option>
                              <option value="errors">Only Errors</option>
                              <option value="standard">Standard (Auth attempts)</option>
                              <option value="detailed">Detailed (All requests)</option>
                            </select>
                          </div>
                        </div>
                      </div>
                    </div> */}
                  </div>
                )}

                {/* Request Tab - UPDATED WITH FILE UPLOAD SUPPORT */}
                {activeTab === 'request' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      Request Configuration
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        {/* Body Type Selector - Updated for SOAP/GraphQL */}
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Body Type
                          </label>
                          <select
                            value={requestBody.bodyType}
                            onChange={(e) => handleRequestBodyChange('bodyType', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.bg,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            disabled={protocolType === 'soap' || protocolType === 'graphql' || ['GET', 'DELETE', 'HEAD', 'OPTIONS'].includes(apiDetails.httpMethod)}
                          >
                            {BODY_TYPES.map(type => (
                              <option key={type.value} value={type.value}>{type.label}</option>
                            ))}
                          </select>
                          
                          {/* SOAP-specific message */}
                          {protocolType === 'soap' && (
                            <div className="mt-2 p-2 rounded-lg flex items-start gap-2" style={{ 
                              backgroundColor: themeColors.info + '20',
                              border: `1px solid ${themeColors.info}`
                            }}>
                              <Send className="h-4 w-4 flex-shrink-0 mt-0.5" style={{ color: themeColors.info }} />
                              <div>
                                <p className="text-xs font-medium" style={{ color: themeColors.info }}>
                                  SOAP Web Service - Request Body Fixed to SOAP Envelope
                                </p>
                                <p className="text-xs mt-0.5" style={{ color: themeColors.textSecondary }}>
                                  SOAP APIs use XML envelopes. Parameters will be sent inside the SOAP body.
                                </p>
                              </div>
                            </div>
                          )}
                          
                          {/* GraphQL-specific message */}
                          {protocolType === 'graphql' && (
                            <div className="mt-2 p-2 rounded-lg flex items-start gap-2" style={{ 
                              backgroundColor: themeColors.success + '20',
                              border: `1px solid ${themeColors.success}`
                            }}>
                              <GitMerge className="h-4 w-4 flex-shrink-0 mt-0.5" style={{ color: themeColors.success }} />
                              <div>
                                <p className="text-xs font-medium" style={{ color: themeColors.success }}>
                                  GraphQL API - Request Body Fixed to GraphQL Query
                                </p>
                                <p className="text-xs mt-0.5" style={{ color: themeColors.textSecondary }}>
                                  GraphQL APIs use JSON with a "query" field containing the GraphQL operation.
                                </p>
                              </div>
                            </div>
                          )}
                          
                          {/* Show warning for GET/DELETE methods (only for REST) */}
                          {protocolType === 'rest' && ['GET', 'DELETE', 'HEAD', 'OPTIONS'].includes(apiDetails.httpMethod) && (
                            <div className="mt-2 p-2 rounded-lg flex items-start gap-2" style={{ 
                              backgroundColor: themeColors.warning + '20',
                              border: `1px solid ${themeColors.warning}`
                            }}>
                              <AlertCircle className="h-4 w-4 flex-shrink-0 mt-0.5" style={{ color: themeColors.warning }} />
                              <div>
                                <p className="text-xs font-medium" style={{ color: themeColors.warning }}>
                                  {apiDetails.httpMethod} requests do not support request bodies
                                </p>
                                <p className="text-xs mt-0.5" style={{ color: themeColors.textSecondary }}>
                                  Body type is locked to "No Body" for this HTTP method. Parameters will be sent as query parameters or path parameters.
                                </p>
                              </div>
                            </div>
                          )}
                          
                          {/* Show info for POST/PUT/PATCH methods (only for REST) */}
                          {protocolType === 'rest' && ['POST', 'PUT', 'PATCH'].includes(apiDetails.httpMethod) && requestBody.bodyType === 'none' && (
                            <div className="mt-2 p-2 rounded-lg flex items-start gap-2" style={{ 
                              backgroundColor: themeColors.info + '20',
                              border: `1px solid ${themeColors.info}`
                            }}>
                              <Info className="h-4 w-4 flex-shrink-0 mt-0.5" style={{ color: themeColors.info }} />
                              <div>
                                <p className="text-xs font-medium" style={{ color: themeColors.info }}>
                                  {apiDetails.httpMethod} requests typically include a request body
                                </p>
                                <p className="text-xs mt-0.5" style={{ color: themeColors.textSecondary }}>
                                  Consider setting a body type (JSON, XML, etc.) for your API.
                                </p>
                              </div>
                            </div>
                          )}
                        </div>

                        {/* Only show these fields if body type is not 'none' and not 'binary' */}
                        {requestBody.bodyType !== 'none' && requestBody.bodyType !== 'binary' && requestBody.bodyType !== 'soap' && requestBody.bodyType !== 'graphql' && (
                          <>
                            <div className="space-y-2">
                              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                Max Request Size (bytes)
                              </label>
                              <input
                                type="number"
                                value={requestBody.maxSize}
                                onChange={(e) => handleRequestBodyChange('maxSize', parseInt(e.target.value))}
                                className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.card,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                                min="1024"
                                max="10485760"
                              />
                            </div>

                            <div className="space-y-2">
                              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                Validate Schema
                              </label>
                              <div className="flex items-center">
                                <input
                                  type="checkbox"
                                  checked={requestBody.validateSchema}
                                  onChange={(e) => handleRequestBodyChange('validateSchema', e.target.checked)}
                                  className="h-4 w-4 rounded"
                                  style={{ accentColor: themeColors.info }}
                                />
                                <span className="ml-2 text-xs" style={{ color: themeColors.textSecondary }}>
                                  Validate request body against schema
                                </span>
                              </div>
                            </div>
                          </>
                        )}
                      </div>

                      {requestBody.bodyType !== 'none' && requestBody.bodyType !== 'binary' && requestBody.bodyType !== 'soap' && requestBody.bodyType !== 'graphql' && (
                        <div className="space-y-4">
                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Allowed Media Types
                            </label>
                            <input
                              type="text"
                              value={requestBody.allowedMediaTypes.join(', ')}
                              onChange={(e) => handleRequestBodyChange('allowedMediaTypes', e.target.value.split(',').map(type => type.trim()))}
                              className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                              placeholder="application/json, application/xml"
                            />
                          </div>

                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Required Fields (in body)
                            </label>
                            <input
                              type="text"
                              value={requestBody.requiredFields.join(', ')}
                              onChange={(e) => handleRequestBodyChange('requiredFields', e.target.value.split(',').map(field => field.trim()))}
                              className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                              placeholder="id, name, email"
                            />
                          </div>
                        </div>
                      )}

                      {/* File Upload Configuration - NEW SECTION */}
                      {requestBody.bodyType === 'binary' && (
                        <div className="space-y-4">
                          <div className="p-4 rounded-lg border" style={{ 
                            borderColor: themeColors.info + '40',
                            backgroundColor: themeColors.info + '10'
                          }}>
                            <h4 className="font-medium mb-3 flex items-center gap-2" style={{ color: themeColors.info }}>
                              <Upload className="h-4 w-4" />
                              File Upload Configuration
                            </h4>
                            
                            <div className="space-y-3">
                              <div className="space-y-2">
                                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                  Max File Size (bytes)
                                </label>
                                <input
                                  type="number"
                                  value={fileUploadConfig.maxFileSize}
                                  onChange={(e) => setFileUploadConfig(prev => ({ ...prev, maxFileSize: parseInt(e.target.value) }))}
                                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.card,
                                    borderColor: themeColors.border,
                                    color: themeColors.text
                                  }}
                                  min="1024"
                                  max="104857600"
                                />
                                <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                                  Maximum allowed file size in bytes (e.g., 10MB = 10485760)
                                </p>
                              </div>

                              <div className="space-y-2">
                                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                  Allowed File Types
                                </label>
                                <input
                                  type="text"
                                  value={fileUploadConfig.allowedFileTypes.join(', ')}
                                  onChange={(e) => setFileUploadConfig(prev => ({ 
                                    ...prev, 
                                    allowedFileTypes: e.target.value.split(',').map(t => t.trim())
                                  }))}
                                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.card,
                                    borderColor: themeColors.border,
                                    color: themeColors.text
                                  }}
                                  placeholder="image/*, application/pdf, .docx"
                                />
                                <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                                  Comma-separated list of MIME types or extensions
                                </p>
                              </div>

                              <div className="flex items-center">
                                <input
                                  type="checkbox"
                                  checked={fileUploadConfig.multipleFiles}
                                  onChange={(e) => setFileUploadConfig(prev => ({ ...prev, multipleFiles: e.target.checked }))}
                                  className="h-4 w-4 rounded mr-2"
                                  style={{ accentColor: themeColors.info }}
                                />
                                <span className="text-xs" style={{ color: themeColors.text }}>
                                  Allow multiple file uploads
                                </span>
                              </div>

                              <div className="space-y-2">
                                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                  File Parameter Name
                                </label>
                                <input
                                  type="text"
                                  value={fileUploadConfig.fileParameterName}
                                  onChange={(e) => setFileUploadConfig(prev => ({ ...prev, fileParameterName: e.target.value }))}
                                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.card,
                                    borderColor: themeColors.border,
                                    color: themeColors.text
                                  }}
                                  placeholder="file"
                                />
                                <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                                  Name of the parameter that will receive the file
                                </p>
                              </div>
                            </div>
                          </div>
                        </div>
                      )}
                    </div>

                    {/* Request Body Sample - Only show if body type is not 'none' and not 'binary' */}
                    {requestBody.bodyType !== 'none' && requestBody.bodyType !== 'binary' && (
                      <div className="space-y-4">
                        <h4 className="font-semibold" style={{ color: themeColors.text }}>
                          Request Body Sample
                        </h4>
                        <div className="border rounded-lg" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <div className="px-4 py-2 border-b flex items-center justify-between" style={{ borderColor: themeColors.border }}>
                            <span className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Sample {requestBody.bodyType.toUpperCase()}
                            </span>
                            {(requestBody.bodyType === 'graphql' || requestBody.bodyType === 'json' || requestBody.bodyType === 'xml') && (
                            <button
                                onClick={() => {
                                    if (requestBody.bodyType === 'json') {
                                        const sample = {};
                                        const isCustomQueryMode = sourceType === 'custom_query' || isCustomQuery || isEditingCustomQuery;
                                        const paramsToUse = isCustomQueryMode ? getInParameters() : getInParameters().filter(p => p.parameterLocation === 'body');
                                        
                                        paramsToUse.forEach(p => {
                                            sample[p.key] = parseValueByType(p.example, p.apiType);
                                        });
                                        
                                        const updatedSample = JSON.stringify(sample, null, 2);
                                        if (requestBody.sample !== updatedSample) {
                                            console.log('✅ Generating request body with proper JSON types');
                                            handleRequestBodyChange('sample', updatedSample);
                                            setRequestSchemaVersion(prev => prev + 1);
                                        }
                                    } else if (requestBody.bodyType === 'xml') {
                                        // XML generation with proper types...
                                    }
                                }}
                                className="px-3 py-1 text-xs rounded border transition-colors hover-lift"
                                style={{ backgroundColor: themeColors.info, borderColor: themeColors.info, color: themeColors.white }}
                            >
                                <RefreshCw className="h-3 w-3 inline mr-1" />
                                Generate from Parameters
                            </button>
                            )}
                          </div>
                          <textarea
                            key={`request-sample-${requestSchemaVersion}`}
                            value={requestBody.sample || ''}
                            onChange={(e) => handleRequestBodyChange('sample', e.target.value)}
                            className="w-full h-96 px-4 py-3 text-xs font-mono resize-none focus:outline-none"
                            style={{ 
                              backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                              color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                            }}
                            placeholder={requestBody.bodyType === 'json' ? '{\n  "key": "value"\n}' : 'Enter request body...'}
                          />
                        </div>
                      </div>
                    )}

                    {/* Binary File Upload Preview - NEW SECTION */}
                    {requestBody.bodyType === 'binary' && (
                      <div className="space-y-4">
                        <h4 className="font-semibold" style={{ color: themeColors.text }}>
                          File Upload Preview
                        </h4>
                        <div className="border rounded-lg p-6 text-center" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <div className="p-8 border-2 border-dashed rounded-lg" style={{ 
                            borderColor: themeColors.info + '40',
                            backgroundColor: themeColors.info + '10'
                          }}>
                            <Upload className="h-12 w-12 mx-auto mb-3" style={{ color: themeColors.info }} />
                            <p className="text-sm" style={{ color: themeColors.text }}>
                              File Upload API
                            </p>
                            <p className="text-xs mt-2" style={{ color: themeColors.textSecondary }}>
                              This API will accept binary file uploads via {fileUploadConfig.multipleFiles ? 'multipart/form-data (multiple files)' : 'multipart/form-data (single file)'}
                            </p>
                            <div className="mt-4 text-left text-xs space-y-1" style={{ color: themeColors.textSecondary }}>
                              <div><strong>Parameter Name:</strong> {fileUploadConfig.fileParameterName}</div>
                              <div><strong>Max File Size:</strong> {(fileUploadConfig.maxFileSize / 1024 / 1024).toFixed(2)} MB</div>
                              <div><strong>Allowed Types:</strong> {fileUploadConfig.allowedFileTypes.join(', ')}</div>
                              <div><strong>Multiple Files:</strong> {fileUploadConfig.multipleFiles ? 'Yes' : 'No'}</div>
                            </div>
                          </div>
                        </div>
                      </div>
                    )}

                    {/* SOAP Envelope Preview */}
                    {requestBody.bodyType === 'soap' && (
                      <div className="space-y-4">
                        <h4 className="font-semibold" style={{ color: themeColors.text }}>
                          SOAP Envelope Preview
                        </h4>
                        <div className="border rounded-lg" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <pre className="w-full h-64 px-4 py-3 overflow-auto text-xs font-mono" style={{ 
                            backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                            color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                          }}>
                            {generateSOAPEnvelope()}
                          </pre>
                        </div>
                      </div>
                    )}

                    {/* GraphQL Query Preview */}
                    {requestBody.bodyType === 'graphql' && (
                      <div className="space-y-4">
                        <h4 className="font-semibold" style={{ color: themeColors.text }}>
                          GraphQL Query Preview
                        </h4>
                        <div className="border rounded-lg" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <pre className="w-full h-64 px-4 py-3 overflow-auto text-xs font-mono" style={{ 
                            backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                            color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                          }}>
                            {generateGraphQLSchema()}
                          </pre>
                        </div>
                      </div>
                    )}

                    {/* Form Data with Files - Enhanced Section */}
                    {requestBody.bodyType === 'form-data' && (
                      <div className="space-y-4">
                        <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                          <Upload className="h-4 w-4" />
                          Form Data Fields (with File Support)
                        </h4>
                        <div className="border rounded-lg overflow-hidden" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <div className="px-4 py-2 border-b" style={{ borderColor: themeColors.border }}>
                            <span className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Form fields will be auto-generated from body parameters with location 'body'
                            </span>
                          </div>
                          <div className="p-4">
                            {getInParameters().filter(p => p.parameterLocation === 'body').length === 0 ? (
                              <div className="text-center py-4">
                                <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                                  No body parameters defined. Add parameters with location 'body' in the Parameters tab.
                                </p>
                              </div>
                            ) : (
                              <div className="space-y-3">
                                {getInParameters().filter(p => p.parameterLocation === 'body').map((param, idx) => (
                                  <div key={idx} className="flex items-center gap-3 p-2 rounded" style={{ backgroundColor: themeColors.hover }}>
                                    <div className="flex-1">
                                      <div className="flex items-center gap-2">
                                        <span className="text-sm font-medium" style={{ color: themeColors.text }}>
                                          {param.key}
                                        </span>
                                        {param.required && (
                                          <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                                            backgroundColor: themeColors.error + '20',
                                            color: themeColors.error
                                          }}>
                                            Required
                                          </span>
                                        )}
                                        {param.oracleType === 'FILE' || param.oracleType === 'BLOB'  || param.oracleType === 'BYTEA' || param.oracleType === 'MULTIPART_FILE' ? (
                                          <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                                            backgroundColor: themeColors.info + '20',
                                            color: themeColors.info
                                          }}>
                                            <Upload className="h-3 w-3 inline mr-1" />
                                            File Upload
                                          </span>
                                        ) : (
                                          <span className="text-xs" style={{ color: themeColors.textSecondary }}>
                                            ({param.oracleType} → {param.apiType})
                                          </span>
                                        )}
                                      </div>
                                      <p className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>
                                        {param.description}
                                      </p>
                                      {param.example && (
                                        <p className="text-xs mt-1 font-mono" style={{ color: themeColors.info }}>
                                          Example: {param.example}
                                        </p>
                                      )}
                                    </div>
                                    {param.oracleType === 'FILE' || param.oracleType === 'BLOB' || param.oracleType === 'BYTEA' || param.oracleType === 'MULTIPART_FILE' ? (
                                      <div className="p-2 rounded" style={{ backgroundColor: themeColors.card }}>
                                        <File className="h-5 w-5" style={{ color: themeColors.info }} />
                                      </div>
                                    ) : (
                                      <div className="p-2 rounded" style={{ backgroundColor: themeColors.card }}>
                                        <FileText className="h-5 w-5" style={{ color: themeColors.textSecondary }} />
                                      </div>
                                    )}
                                  </div>
                                ))}
                                <p className="text-xs mt-2" style={{ color: themeColors.info }}>
                                  <Info className="h-3 w-3 inline mr-1" />
                                  For file uploads, set the parameter's Oracle Type to 'FILE', 'BLOB', 'BYTEA' or 'MULTIPART_FILE' in the Parameters tab.
                                </p>
                              </div>
                            )}
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Show message when no body */}
                    {requestBody.bodyType === 'none' && (
                      <div className="p-8 text-center border rounded-lg" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.hover
                      }}>
                        <FileText className="h-12 w-12 mx-auto mb-3" style={{ color: themeColors.textSecondary }} />
                        <p style={{ color: themeColors.textSecondary }}>
                          No request body will be sent with this API.
                        </p>
                        <p className="text-xs mt-2" style={{ color: themeColors.info }}>
                          This is typical for GET and DELETE operations.
                        </p>
                      </div>
                    )}
                  </div>
                )}

                {/* Response Tab */}
                {/* Response Tab */}
{activeTab === 'response' && (
  <div className="space-y-6">
    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
      Response Configuration
    </h3>

    {/* Protocol-aware info banner */}
    {protocolType === 'soap' && (
      <div className="p-3 rounded-lg border flex items-center gap-2" style={{
        borderColor: themeColors.info + '40',
        backgroundColor: themeColors.info + '10'
      }}>
        <Send className="h-4 w-4 flex-shrink-0" style={{ color: themeColors.info }} />
        <div>
          <span className="text-xs font-medium" style={{ color: themeColors.info }}>SOAP Response</span>
          <p className="text-xs mt-0.5" style={{ color: themeColors.textSecondary }}>
            SOAP responses are XML envelopes. The success/error schemas below define the inner payload structure.
          </p>
        </div>
      </div>
    )}

    {protocolType === 'graphql' && (
      <div className="p-3 rounded-lg border flex items-center gap-2" style={{
        borderColor: themeColors.success + '40',
        backgroundColor: themeColors.success + '10'
      }}>
        <GitMerge className="h-4 w-4 flex-shrink-0" style={{ color: themeColors.success }} />
        <div>
          <span className="text-xs font-medium" style={{ color: themeColors.success }}>GraphQL Response</span>
          <p className="text-xs mt-0.5" style={{ color: themeColors.textSecondary }}>
            GraphQL responses always use JSON with a top-level <code className="font-mono">data</code> or <code className="font-mono">errors</code> field.
          </p>
        </div>
      </div>
    )}

    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div className="space-y-4">
        <div className="space-y-2">
          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
            {protocolType === 'soap' ? 'SOAP Success Response Body' :
             protocolType === 'graphql' ? 'GraphQL Success Response (data field)' :
             'Success Schema'}
          </label>
          <div className="border rounded-lg" style={{
            borderColor: themeColors.border,
            backgroundColor: themeColors.card
          }}>
            <div className="px-3 py-2 border-b flex items-center justify-between" style={{ borderColor: themeColors.border }}>
              <span className="text-xs" style={{ color: themeColors.textSecondary }}>
                {protocolType === 'soap' ? 'text/xml' :
                 protocolType === 'graphql' ? 'application/json' :
                 'application/json'}
              </span>
              {protocolType === 'soap' && (
                <button
                  onClick={() => {
                    const operationName = soapConfig.soapAction || apiDetails.apiCode || 'Operation';
                    const soapSuccess = `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <${operationName}Response>
      <success>true</success>
      <message>Request processed successfully</message>
      ${getOutMappings().slice(0, 3).map(m => `<${m.apiField}>${m.apiType === 'integer' ? '123' : 'value'}</${m.apiField}>`).join('\n      ')}
    </${operationName}Response>
  </soap:Body>
</soap:Envelope>`;
                    handleResponseBodyChange('successSchema', soapSuccess);
                  }}
                  className="text-xs px-2 py-1 rounded transition-colors hover-lift"
                  style={{ backgroundColor: themeColors.hover, color: themeColors.textSecondary }}
                >
                  Insert SOAP Template
                </button>
              )}
              {protocolType === 'graphql' && (
                <button
                  onClick={() => {
                    const operationName = graphqlConfig.operationName || 'query';
                    const gqlSuccess = JSON.stringify({
                      data: {
                        [operationName]: getOutMappings().slice(0, 50).reduce((acc, m) => {
                          acc[m.apiField] = m.apiType === 'integer' ? 123 : m.apiType === 'boolean' ? true : 'value';
                          return acc;
                        }, {})
                      }
                    }, null, 2);
                    handleResponseBodyChange('successSchema', gqlSuccess);
                  }}
                  className="text-xs px-2 py-1 rounded transition-colors hover-lift"
                  style={{ backgroundColor: themeColors.hover, color: themeColors.textSecondary }}
                >
                  Insert GraphQL Template
                </button>
              )}
            </div>
            <textarea
                key={`success-schema-${responseSchemaVersion}`}
                value={responseBody.successSchema}
                onChange={(e) => handleResponseBodyChange('successSchema', e.target.value)}
                className="w-full h-40 px-4 py-3 text-xs font-mono resize-none focus:outline-none"
                style={{
                  backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                  color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                }}
                placeholder={
                protocolType === 'soap'
                  ? `<?xml version="1.0" encoding="UTF-8"?>\n<soap:Envelope ...>\n  <soap:Body>\n    <OperationResponse>\n      <success>true</success>\n    </OperationResponse>\n  </soap:Body>\n</soap:Envelope>`
                  : protocolType === 'graphql'
                  ? `{\n  "data": {\n    "${graphqlConfig.operationName || 'operation'}": {\n      "id": 1,\n      "field": "value"\n    }\n  }\n}`
                  : `{\n  "success": true,\n  "data": {},\n  "message": "Request processed successfully"\n}`
              }
            />
          </div>
        </div>

        {/* Metadata - only for REST */}
        {protocolType === 'rest' && (
          <div className="space-y-2">
            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
              Include Metadata Fields
            </label>
            <input
              type="text"
              value={responseBody.metadataFields.join(', ')}
              onChange={(e) => handleResponseBodyChange('metadataFields', e.target.value.split(',').map(field => field.trim()))}
              className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
              style={{
                backgroundColor: themeColors.card,
                borderColor: themeColors.border,
                color: themeColors.text
              }}
              placeholder="timestamp, apiVersion, requestId"
            />
          </div>
        )}
      </div>

      <div className="space-y-4">
        <div className="space-y-2">
          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
            {protocolType === 'soap' ? 'SOAP Fault Response' :
             protocolType === 'graphql' ? 'GraphQL Error Response (errors field)' :
             'Error Schema'}
          </label>
          <div className="border rounded-lg" style={{
            borderColor: themeColors.border,
            backgroundColor: themeColors.card
          }}>
            <div className="px-3 py-2 border-b" style={{ borderColor: themeColors.border }}>
              <span className="text-xs" style={{ color: themeColors.textSecondary }}>
                {protocolType === 'soap' ? 'SOAP Fault Envelope' :
                 protocolType === 'graphql' ? 'GraphQL Errors Array' :
                 'Error Response Body'}
              </span>
            </div>
            <textarea
                key={`error-schema-${errorSchemaVersion}`}
                value={responseBody.errorSchema}
                onChange={(e) => handleResponseBodyChange('errorSchema', e.target.value)}
                className="w-full h-40 px-4 py-3 text-xs font-mono resize-none focus:outline-none"
                style={{
                  backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                  color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                }}
                placeholder={
                protocolType === 'soap'
                  ? `<?xml version="1.0" encoding="UTF-8"?>\n<soap:Envelope ...>\n  <soap:Body>\n    <soap:Fault>\n      <faultcode>Server</faultcode>\n      <faultstring>Error description</faultstring>\n    </soap:Fault>\n  </soap:Body>\n</soap:Envelope>`
                  : protocolType === 'graphql'
                  ? `{\n  "errors": [\n    {\n      "message": "Error description",\n      "locations": [{ "line": 1, "column": 1 }],\n      "path": ["fieldName"],\n      "extensions": { "code": "ERROR_CODE" }\n    }\n  ]\n}`
                  : `{\n  "success": false,\n  "error": {\n    "code": "ERROR_CODE",\n    "message": "Error description"\n  }\n}`
              }
            />
          </div>
        </div>

        {/* Compression - REST and GraphQL only (SOAP has its own encoding) */}
        {protocolType !== 'soap' && (
          <div className="space-y-2">
            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
              Compression
            </label>
            <select
              value={responseBody.compression}
              onChange={(e) => handleResponseBodyChange('compression', e.target.value)}
              className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
              style={{
                backgroundColor: themeColors.bg,
                borderColor: themeColors.border,
                color: themeColors.text
              }}
            >
              <option value="none">None</option>
              <option value="gzip">Gzip</option>
              <option value="deflate">Deflate</option>
            </select>
          </div>
        )}

        {/* SOAP-specific: MTOM for binary attachments */}
        {protocolType === 'soap' && (
          <div className="p-3 rounded-lg border" style={{
            borderColor: themeColors.border,
            backgroundColor: themeColors.hover
          }}>
            <h5 className="text-xs font-medium mb-2" style={{ color: themeColors.text }}>SOAP Response Format</h5>
            <div className="space-y-1 text-xs" style={{ color: themeColors.textSecondary }}>
              <div>• Version: SOAP {soapConfig.version}</div>
              <div>• Encoding: {soapConfig.encodingStyle}</div>
              <div>• Binding: {soapConfig.bindingStyle}</div>
              <div>• Content-Type: text/xml; charset=utf-8</div>
              {soapConfig.includeMtom && <div>• MTOM: Enabled (binary attachments)</div>}
            </div>
          </div>
        )}

        {/* GraphQL-specific: extensions info */}
        {protocolType === 'graphql' && (
          <div className="p-3 rounded-lg border" style={{
            borderColor: themeColors.border,
            backgroundColor: themeColors.hover
          }}>
            <h5 className="text-xs font-medium mb-2" style={{ color: themeColors.text }}>GraphQL Response Format</h5>
            <div className="space-y-1 text-xs" style={{ color: themeColors.textSecondary }}>
              <div>• Content-Type: application/json</div>
              <div>• Always returns HTTP 200 (even for errors)</div>
              <div>• Success: <code className="font-mono">{'{ "data": { ... } }'}</code></div>
              <div>• Error: <code className="font-mono">{'{ "errors": [ ... ] }'}</code></div>
              {graphqlConfig.enableBatching && <div>• Batching: Enabled (returns array)</div>}
            </div>
          </div>
        )}
      </div>
    </div>

    {/* Generate Sample button - protocol-aware */}
    <div className="mt-4">
      <button
        onClick={() => {
          if (protocolType === 'soap') {
            const operationName = soapConfig.soapAction || apiDetails.apiCode || 'Operation';
            const soapSuccess = `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="${soapConfig.version === '1.2' ? 'http://www.w3.org/2003/05/soap-envelope' : 'http://schemas.xmlsoap.org/soap/envelope/'}">
  <soap:Body>
    <${operationName}Response xmlns="${soapConfig.namespace}">
      <success>true</success>
      <message>Request processed successfully</message>
      ${getOutMappings().slice(0, 50).map(m =>
        `<${m.apiField}>${m.apiType === 'integer' ? '123' : m.apiType === 'boolean' ? 'true' : 'value'}</${m.apiField}>`
      ).join('\n      ')}
    </${operationName}Response>
  </soap:Body>
</soap:Envelope>`;
            handleResponseBodyChange('successSchema', soapSuccess);

            const soapFault = `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="${soapConfig.version === '1.2' ? 'http://www.w3.org/2003/05/soap-envelope' : 'http://schemas.xmlsoap.org/soap/envelope/'}">
  <soap:Body>
    <soap:Fault>
      <faultcode>soap:Server</faultcode>
      <faultstring>An error occurred processing the request</faultstring>
      <detail>
        <errorCode>ERROR_CODE</errorCode>
        <errorMessage>Detailed error description</errorMessage>
      </detail>
    </soap:Fault>
  </soap:Body>
</soap:Envelope>`;
            handleResponseBodyChange('errorSchema', soapFault);

          } else if (protocolType === 'graphql') {
            const operationName = graphqlConfig.operationName || 'operation';
            const responseData = getOutMappings().slice(0, 50).reduce((acc, m) => {
              acc[m.apiField] = m.apiType === 'integer' ? 123 : m.apiType === 'boolean' ? true : 'value';
              return acc;
            }, {});
            handleResponseBodyChange('successSchema', JSON.stringify({
              data: { [operationName]: responseData }
            }, null, 2));
            handleResponseBodyChange('errorSchema', JSON.stringify({
              errors: [{
                message: 'Error description',
                locations: [{ line: 1, column: 1 }],
                path: [operationName],
                extensions: { code: 'ERROR_CODE' }
              }]
            }, null, 2));

          } else {
            // REST
            const sampleData = {};
          getOutMappings().slice(0, 50).forEach(mapping => {
            if (mapping.apiType === 'integer') sampleData[mapping.apiField] = 123;
            else if (mapping.apiType === 'boolean') sampleData[mapping.apiField] = true;
            else if (mapping.format === 'date-time') sampleData[mapping.apiField] = '2024-01-01T00:00:00Z';
            else sampleData[mapping.apiField] = mapping.apiField === 'id' ? 1 : 'sample';
          });
          handleResponseBodyChange('successSchema', JSON.stringify({
            success: true,
            data: sampleData,
            message: 'Request processed successfully',
            metadata: {
              timestamp: '{{timestamp}}',
              apiVersion: apiDetails.version,
              requestId: '{{requestId}}'
            }
          }, null, 2));
          setResponseSchemaVersion(prev => prev + 1); // Add this line
          }
        }}
        className="px-3 py-1.5 rounded-lg flex items-center gap-2 text-xs transition-colors hover-lift"
        style={{ backgroundColor: themeColors.info, color: themeColors.white }}
      >
        <Sparkles className="h-4 w-4" />
        Generate {protocolType === 'soap' ? 'SOAP' : protocolType === 'graphql' ? 'GraphQL' : 'REST'} Sample from Mappings
      </button>
    </div>

    {/* HTTP Status Codes - REST and GraphQL only */}
    {protocolType !== 'soap' && (
      <div className="space-y-4">
        <h4 className="font-semibold" style={{ color: themeColors.text }}>
          {protocolType === 'graphql' ? 'GraphQL HTTP Behavior' : 'HTTP Status Codes'}
        </h4>
        {protocolType === 'graphql' ? (
          <div className="p-4 rounded-lg border" style={{
            borderColor: themeColors.info + '40',
            backgroundColor: themeColors.info + '10'
          }}>
            <p className="text-xs" style={{ color: themeColors.textSecondary }}>
              GraphQL always returns <strong style={{ color: themeColors.text }}>HTTP 200</strong> for both success and error responses.
              Errors are indicated by the presence of an <code className="font-mono" style={{ color: themeColors.info }}>errors</code> array in the response body,
              not by HTTP status codes.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[
              { code: '200', label: 'Success', color: themeColors.success },
              { code: '400', label: 'Bad Request', color: themeColors.error },
              { code: '401', label: 'Unauthorized', color: themeColors.error },
              { code: '500', label: 'Server Error', color: themeColors.error },
            ].map(({ code, label, color }) => (
              <div key={code} className="p-3 rounded-lg border text-center" style={{
                borderColor: color + '40',
                backgroundColor: color + '10'
              }}>
                <div className="text-lg font-bold" style={{ color }}>{code}</div>
                <div className="text-xs" style={{ color: themeColors.textSecondary }}>{label}</div>
              </div>
            ))}
          </div>
        )}
      </div>
    )}

    {/* SOAP HTTP notes */}
    {protocolType === 'soap' && (
      <div className="space-y-4">
        <h4 className="font-semibold" style={{ color: themeColors.text }}>SOAP HTTP Behavior</h4>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
          {[
            { code: '200', label: 'Success / SOAP Fault', color: themeColors.success, note: 'Both success and business faults' },
            { code: '400', label: 'Malformed Request', color: themeColors.warning, note: 'Invalid XML or SOAP envelope' },
            { code: '500', label: 'Server Error', color: themeColors.error, note: 'Critical server-side failure' },
          ].map(({ code, label, color, note }) => (
            <div key={code} className="p-3 rounded-lg border" style={{
              borderColor: color + '40',
              backgroundColor: color + '10'
            }}>
              <div className="text-lg font-bold" style={{ color }}>{code}</div>
              <div className="text-xs font-medium" style={{ color: themeColors.text }}>{label}</div>
              <div className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>{note}</div>
            </div>
          ))}
        </div>
      </div>
    )}
  </div>
)}

                {/* Database Tests Tab */}
                {activeTab === 'tests' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      Database Object Tests
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        <div className="p-4 rounded-lg border" style={{ 
                          borderColor: themeColors.info + '40',
                          backgroundColor: themeColors.info + '10'
                        }}>
                          <h4 className="font-medium mb-3 flex items-center gap-2" style={{ color: themeColors.info }}>
                            <Database className="h-4 w-4" />
                            Database Connectivity Tests
                          </h4>
                          <div className="space-y-3">
                            <div className="flex items-center justify-between">
                              <span className="text-xs" style={{ color: themeColors.text }}>Test Database Connection</span>
                              <input
                                type="checkbox"
                                checked={tests.testConnection}
                                onChange={(e) => handleTestsChange('testConnection', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.info }}
                              />
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-xs" style={{ color: themeColors.text }}>Test Object Access</span>
                              <input
                                type="checkbox"
                                checked={tests.testObjectAccess}
                                onChange={(e) => handleTestsChange('testObjectAccess', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.info }}
                              />
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-xs" style={{ color: themeColors.text }}>Test User Privileges</span>
                              <input
                                type="checkbox"
                                checked={tests.testPrivileges}
                                onChange={(e) => handleTestsChange('testPrivileges', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.info }}
                              />
                            </div>
                          </div>
                        </div>

                        <div className="p-4 rounded-lg border" style={{ 
                          borderColor: themeColors.success + '40',
                          backgroundColor: themeColors.success + '10'
                        }}>
                          <h4 className="font-medium mb-3 flex items-center gap-2" style={{ color: themeColors.success }}>
                            <FileText className="h-4 w-4" />
                            Data Validation Tests
                          </h4>
                          <div className="space-y-3">
                            <div className="flex items-center justify-between">
                              <span className="text-xs" style={{ color: themeColors.text }}>Test Data Types</span>
                              <input
                                type="checkbox"
                                checked={tests.testDataTypes}
                                onChange={(e) => handleTestsChange('testDataTypes', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.success }}
                              />
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-xs" style={{ color: themeColors.text }}>Test NULL Constraints</span>
                              <input
                                type="checkbox"
                                checked={tests.testNullConstraints}
                                onChange={(e) => handleTestsChange('testNullConstraints', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.success }}
                              />
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-xs" style={{ color: themeColors.text }}>Test Unique Constraints</span>
                              <input
                                type="checkbox"
                                checked={tests.testUniqueConstraints}
                                onChange={(e) => handleTestsChange('testUniqueConstraints', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.success }}
                              />
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-xs" style={{ color: themeColors.text }}>Test Foreign Key References</span>
                              <input
                                type="checkbox"
                                checked={tests.testForeignKeyReferences}
                                onChange={(e) => handleTestsChange('testForeignKeyReferences', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.success }}
                              />
                            </div>
                          </div>
                        </div>
                      </div>

                      <div className="space-y-4">
                        <div className="p-4 rounded-lg border" style={{ 
                          borderColor: themeColors.warning + '40',
                          backgroundColor: themeColors.warning + '10'
                        }}>
                          <h4 className="font-medium mb-3 flex items-center gap-2" style={{ color: themeColors.warning }}>
                            <Zap className="h-4 w-4" />
                            Performance Tests
                          </h4>
                          <div className="space-y-3">
                            <div className="flex items-center justify-between">
                              <span className="text-xs" style={{ color: themeColors.text }}>Test Query Performance</span>
                              <input
                                type="checkbox"
                                checked={tests.testQueryPerformance}
                                onChange={(e) => handleTestsChange('testQueryPerformance', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.warning }}
                              />
                            </div>
                            <div className="space-y-2">
                              <label className="text-xs" style={{ color: themeColors.text }}>
                                Performance Threshold (ms)
                              </label>
                              <input
                                type="number"
                                value={tests.performanceThreshold}
                                onChange={(e) => handleTestsChange('performanceThreshold', parseInt(e.target.value))}
                                className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.card,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                                min="100"
                                max="10000"
                              />
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-xs" style={{ color: themeColors.text }}>Test with Sample Data</span>
                              <input
                                type="checkbox"
                                checked={tests.testWithSampleData}
                                onChange={(e) => handleTestsChange('testWithSampleData', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.warning }}
                              />
                            </div>
                            {tests.testWithSampleData && (
                              <div className="space-y-2">
                                <label className="text-xs" style={{ color: themeColors.text }}>
                                  Sample Data Rows
                                </label>
                                <input
                                  type="number"
                                  value={tests.sampleDataRows}
                                  onChange={(e) => handleTestsChange('sampleDataRows', parseInt(e.target.value))}
                                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.card,
                                    borderColor: themeColors.border,
                                    color: themeColors.text
                                  }}
                                  min="1"
                                  max="1000"
                                />
                              </div>
                            )}
                          </div>
                        </div>

                        <div className="p-4 rounded-lg border" style={{ 
                          borderColor: themeColors.error + '40',
                          backgroundColor: themeColors.error + '10'
                        }}>
                          <h4 className="font-medium mb-3 flex items-center gap-2" style={{ color: themeColors.error }}>
                            <Shield className="h-4 w-4" />
                            Security Tests
                          </h4>
                          <div className="space-y-3">
                            <div className="flex items-center justify-between">
                              <span className="text-xs" style={{ color: themeColors.text }}>Test SQL Injection</span>
                              <input
                                type="checkbox"
                                checked={tests.testSQLInjection}
                                onChange={(e) => handleTestsChange('testSQLInjection', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.error }}
                              />
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-xs" style={{ color: themeColors.text }}>Test Authentication</span>
                              <input
                                type="checkbox"
                                checked={tests.testAuthentication}
                                onChange={(e) => handleTestsChange('testAuthentication', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.error }}
                              />
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-xs" style={{ color: themeColors.text }}>Test Authorization</span>
                              <input
                                type="checkbox"
                                checked={tests.testAuthorization}
                                onChange={(e) => handleTestsChange('testAuthorization', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.error }}
                              />
                            </div>
                          </div>
                        </div>

                        {/* PL/SQL Specific Tests */}
                        {(schemaConfig.objectType === 'PROCEDURE' || schemaConfig.objectType === 'FUNCTION' || schemaConfig.objectType === 'PACKAGE') && (
                          <div className="p-4 rounded-lg border" style={{ 
                            borderColor: themeColors.info + '40',
                            backgroundColor: themeColors.info + '10'
                          }}>
                            <h4 className="font-medium mb-3 flex items-center gap-2" style={{ color: themeColors.info }}>
                              <Code className="h-4 w-4" />
                              PL/SQL Execution Tests
                            </h4>
                            <div className="space-y-3">
                              <div className="flex items-center justify-between">
                                <span className="text-xs" style={{ color: themeColors.text }}>Test Procedure Execution</span>
                                <input
                                  type="checkbox"
                                  checked={tests.testProcedureExecution}
                                  onChange={(e) => handleTestsChange('testProcedureExecution', e.target.checked)}
                                  className="h-4 w-4 rounded"
                                  style={{ accentColor: themeColors.info }}
                                />
                              </div>
                              <div className="flex items-center justify-between">
                                <span className="text-xs" style={{ color: themeColors.text }}>Test Function Return</span>
                                <input
                                  type="checkbox"
                                  checked={tests.testFunctionReturn}
                                  onChange={(e) => handleTestsChange('testFunctionReturn', e.target.checked)}
                                  className="h-4 w-4 rounded"
                                  style={{ accentColor: themeColors.info }}
                                />
                              </div>
                              <div className="flex items-center justify-between">
                                <span className="text-xs" style={{ color: themeColors.text }}>Test Exception Handling</span>
                                <input
                                  type="checkbox"
                                  checked={tests.testExceptionHandling}
                                  onChange={(e) => handleTestsChange('testExceptionHandling', e.target.checked)}
                                  className="h-4 w-4 rounded"
                                  style={{ accentColor: themeColors.info }}
                                />
                              </div>
                            </div>
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Test SQL Preview */}
                    <div className="space-y-4">
                      <h4 className="font-semibold" style={{ color: themeColors.text }}>
                        Test SQL Preview
                      </h4>
                      <div className="border rounded-lg" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <div className="px-4 py-2 border-b flex items-center justify-between" style={{ borderColor: themeColors.border }}>
                          <span className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Generated Test Queries
                          </span>
                        </div>
                        <pre className="w-full h-48 px-4 py-3 overflow-auto text-xs font-mono" style={{ 
                          backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                          color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                        }}>
{`-- Database Object Tests for ${schemaConfig.schemaName}.${schemaConfig.objectName}
-- Generated: ${new Date().toISOString()}
-- Protocol: ${protocolType.toUpperCase()}

-- 1. Connection Test
SELECT 'Database Connection Successful' AS test_result FROM DUAL;

-- 2. Object Access Test
SELECT COUNT(*) AS object_exists 
FROM ALL_OBJECTS 
WHERE OWNER = '${schemaConfig.schemaName}' 
  AND OBJECT_NAME = '${schemaConfig.objectName}'
  AND OBJECT_TYPE = '${schemaConfig.objectType}';

-- 3. ${schemaConfig.objectType} Structure Test
${schemaConfig.objectType === 'TABLE' || schemaConfig.objectType === 'VIEW' ? 
  `SELECT COLUMN_NAME, DATA_TYPE, NULLABLE 
   FROM ALL_TAB_COLUMNS 
   WHERE OWNER = '${schemaConfig.schemaName}' 
     AND TABLE_NAME = '${schemaConfig.objectName}'
   ORDER BY COLUMN_ID;` : 
  `SELECT ARGUMENT_NAME, DATA_TYPE, IN_OUT 
   FROM ALL_ARGUMENTS 
   WHERE OWNER = '${schemaConfig.schemaName}' 
     AND OBJECT_NAME = '${schemaConfig.objectName}'
   ORDER BY POSITION;`}

${tests.testWithSampleData ? `-- 4. Sample Data Query
SELECT * FROM ${schemaConfig.schemaName}.${schemaConfig.objectName} 
WHERE ROWNUM <= ${tests.sampleDataRows};` : ''}

${tests.testQueryPerformance ? `-- 5. Performance Test
SET TIMING ON;
SELECT /*+ GATHER_PLAN_STATISTICS */ * 
FROM ${schemaConfig.schemaName}.${schemaConfig.objectName} 
WHERE ROWNUM <= 100;` : ''}`}
                        </pre>
                      </div>
                    </div>
                  </div>
                )}

                {/* Settings Tab */}
                {activeTab === 'settings' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      API Settings
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Timeout (ms)
                          </label>
                          <input
                            type="number"
                            value={settings.timeout}
                            onChange={(e) => handleSettingsChange('timeout', parseInt(e.target.value))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            min="1000"
                            max="60000"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Max Records
                          </label>
                          <input
                            type="number"
                            value={settings.maxRecords}
                            onChange={(e) => handleSettingsChange('maxRecords', parseInt(e.target.value))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            min="1"
                            max="10000"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Log Level
                          </label>
                          <select
                            value={settings.logLevel}
                            onChange={(e) => handleSettingsChange('logLevel', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.bg,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          >
                            <option value="DEBUG">DEBUG</option>
                            <option value="INFO">INFO</option>
                            <option value="WARN">WARN</option>
                            <option value="ERROR">ERROR</option>
                          </select>
                        </div>

                        <div className="space-y-3">
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.enableLogging}
                              onChange={(e) => handleSettingsChange('enableLogging', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Logging
                            </span>
                          </div>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.enableMonitoring}
                              onChange={(e) => handleSettingsChange('enableMonitoring', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Monitoring
                            </span>
                          </div>
                        </div>
                      </div>

                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Cache TTL (seconds)
                          </label>
                          <input
                            type="number"
                            value={settings.cacheTtl}
                            onChange={(e) => handleSettingsChange('cacheTtl', parseInt(e.target.value))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            min="0"
                            max="86400"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Alert Email
                          </label>
                          <input
                            type="email"
                            value={settings.alertEmail}
                            onChange={(e) => handleSettingsChange('alertEmail', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="admin@example.com"
                          />
                        </div>

                        <div className="space-y-3">
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.enableCaching}
                              onChange={(e) => handleSettingsChange('enableCaching', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Caching
                            </span>
                          </div>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.corsEnabled}
                              onChange={(e) => handleSettingsChange('corsEnabled', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable CORS
                            </span>
                          </div>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.enableAlerts}
                              onChange={(e) => handleSettingsChange('enableAlerts', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Email Alerts
                            </span>
                          </div>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.enableTracing}
                              onChange={(e) => handleSettingsChange('enableTracing', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Distributed Tracing
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Generation Options */}
                    <div className="space-y-4">
                      <h4 className="font-semibold" style={{ color: themeColors.text }}>
                        Generation Options
                      </h4>
                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        {protocolType === 'rest' && (
                          <div className="p-3 rounded-lg border flex items-center" style={{ 
                            borderColor: themeColors.border,
                            backgroundColor: themeColors.card
                          }}>
                            <input
                              type="checkbox"
                              checked={settings.generateSwagger}
                              onChange={(e) => handleSettingsChange('generateSwagger', e.target.checked)}
                              className="h-4 w-4 rounded mr-3"
                              style={{ accentColor: themeColors.info }}
                            />
                            <div>
                              <div className="font-medium text-xs" style={{ color: themeColors.text }}>OpenAPI Spec</div>
                              <div className="text-xs" style={{ color: themeColors.textSecondary }}>Generate Swagger documentation</div>
                            </div>
                          </div>
                        )}
                        <div className="p-3 rounded-lg border flex items-center" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <input
                            type="checkbox"
                            checked={settings.generatePostman}
                            onChange={(e) => handleSettingsChange('generatePostman', e.target.checked)}
                            className="h-4 w-4 rounded mr-3"
                            style={{ accentColor: themeColors.info }}
                          />
                          <div>
                            <div className="font-medium text-xs" style={{ color: themeColors.text }}>Postman Collection</div>
                            <div className="text-xs" style={{ color: themeColors.textSecondary }}>Generate Postman tests</div>
                          </div>
                        </div>
                        <div className="p-3 rounded-lg border flex items-center" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <input
                            type="checkbox"
                            checked={settings.generateClientSDK}
                            onChange={(e) => handleSettingsChange('generateClientSDK', e.target.checked)}
                            className="h-4 w-4 rounded mr-3"
                            style={{ accentColor: themeColors.info }}
                          />
                          <div>
                            <div className="font-medium text-xs" style={{ color: themeColors.text }}>Client SDK</div>
                            <div className="text-xs" style={{ color: themeColors.textSecondary }}>Generate client libraries</div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Preview Tab */}
                {activeTab === 'preview' && (
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                        Generated Code Preview
                      </h3>
                      <div className="flex items-center gap-2">
                        <select
                          value={previewMode}
                          onChange={(e) => setPreviewMode(e.target.value)}
                          className="px-3 py-1.5 border rounded-lg text-xs hover-lift"
                          style={{ 
                            backgroundColor: themeColors.bg,
                            borderColor: themeColors.border,
                            color: themeColors.text
                          }}
                        >
                          <option value="json">Configuration JSON</option>
                          {protocolType === 'rest' && <option value="plsql">PL/SQL Package</option>}
                          {protocolType === 'rest' && <option value="openapi">OpenAPI Spec</option>}
                          {protocolType === 'soap' && <option value="soap">SOAP Envelope</option>}
                          {protocolType === 'graphql' && <option value="graphql">GraphQL Schema</option>}
                          <option value="postman">Postman Collection</option>
                          <option value="sql_insert">SQL INSERT Statements</option>
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
                        <span className="text-xs font-medium" style={{ color: themeColors.text }}>
                          {previewMode === 'plsql' ? 'PL/SQL Package' : 
                           previewMode === 'soap' ? 'SOAP Envelope' :
                           previewMode === 'graphql' ? 'GraphQL Schema' :
                           previewMode === 'openapi' ? 'OpenAPI Specification' :
                           previewMode === 'postman' ? 'Postman Collection' : 
                           previewMode === 'sql_insert' ? 'SQL INSERT Statements' : 'Configuration'}
                        </span>
                        <span className="text-xs font-mono" style={{ color: themeColors.textSecondary }}>
                          {previewMode === 'plsql' ? '.sql' : 
                           previewMode === 'soap' ? '.xml' :
                           previewMode === 'graphql' ? '.graphql' :
                           previewMode === 'sql_insert' ? '.sql' : '.json'}
                        </span>
                      </div>
                      <pre 
                        id="code-preview-content"
                        className="w-full h-[400px] px-4 py-3 overflow-auto text-xs" 
                        style={{ 
                          backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                          color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                        }}
                      >
                        {generatedCode}
                      </pre>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Footer */}
          <div className="px-6 py-4 border-t flex items-center justify-between" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.card
          }}>
            <div className="text-xs" style={{ color: themeColors.textSecondary }}>
              {protocolType === 'rest' ? (
                <>Endpoint: <span className="font-mono font-medium" style={{ color: themeColors.text }}>
                  {apiDetails.httpMethod} {apiDetails.basePath}{apiDetails.endpointPath}
                </span></>
              ) : protocolType === 'soap' ? (
                <>SOAP Service: <span className="font-mono font-medium" style={{ color: themeColors.text }}>
                  {soapConfig.serviceName || 'Not set'} ({soapConfig.version})
                </span></>
              ) : (
                <>GraphQL API: <span className="font-mono font-medium" style={{ color: themeColors.text }}>
                  {graphqlConfig.operationType}: {graphqlConfig.operationName || 'Not set'}
                </span></>
              )}
              {validationResult && validationResult.valid && !isEditing && (
                <span className="text-xs block mt-1" style={{ color: themeColors.success }}>
                  <Check className="h-3 w-3 inline mr-1" />
                  Object validated successfully
                </span>
              )}
              <span className="text-xs block mt-1" style={{ color: themeColors.info }}>
                IN Parameters: {getInParameters().length} | Response Fields: {getOutMappings().length}
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
                disabled={loading || validating || selectedObject?.loading}
              >
                Cancel
              </button>
              <button
                onClick={handleSave}
                className="px-4 py-2 rounded-lg flex items-center gap-2 transition-colors hover-lift"
                style={{ 
                  backgroundColor: (loading || validating || selectedObject?.loading || (!isEditing && apiCodeExists)) 
                    ? themeColors.textSecondary 
                    : themeColors.success, 
                  color: themeColors.white,
                  opacity: (!isEditing && apiCodeExists) ? 0.6 : 1,
                  cursor: (!isEditing && apiCodeExists) ? 'not-allowed' : 'pointer'
                }}
                disabled={loading || validating || selectedObject?.loading || (!isEditing && apiCodeExists)}
              >
                {selectedObject?.loading ? (
                  <>
                    <Loader className="h-4 w-4 animate-spin" />
                    Loading...
                  </>
                ) : loading || validating ? (
                  <>
                    <Loader className="h-4 w-4 animate-spin" />
                    {validating ? 'Validating...' : 'Loading...'}
                  </>
                ) : (
                  <>
                    <Save className="h-4 w-4" />
                    {isEditing ? 'Update API' : 'Generate & Save API'}
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Add the object selector modal - ONLY for dashboard generation */}
      {fromDashboard && (
        <ObjectSelectorModal
          isOpen={showObjectSelector}
          onClose={() => {
            setShowObjectSelector(false);
            if (!selectedDbObject) {
              onClose(); // Close the main modal if no object selected
            }
          }}
          onSelect={(obj) => {
            loadSelectedObjectDetails(obj);
          }}
          colors={themeColors}
          authToken={authToken}
          databaseType={databaseType}
        />
      )}

      {/* Add the preview modal */}
      <ApiPreviewModal
        key={`preview-${responseSchemaVersion}-${responseMappings.length}`}
        isOpen={previewOpen}
        onClose={() => setPreviewOpen(false)}
        onConfirm={handlePreviewConfirm}
        apiData={newApiData}
        colors={colors}
        theme={theme}
      />

      {/* Add the loading modal */}
      <ApiLoadingModal
        isOpen={loadingOpen}
        colors={colors}
        theme={theme}
      />

      {/* Add the confirmation modal with actual API response */}
      <ApiConfirmationModal
        isOpen={confirmationOpen}
        onClose={handleConfirmationClose}
        apiData={newApiData}
        apiResponse={apiResponse}
        colors={colors}
        theme={theme}
      />

      {/* Add folder modal */}
      <AddFolderModal
        isOpen={showAddFolderModal}
        onClose={() => setShowAddFolderModal(false)}
        onConfirm={handleAddNewFolder}
        selectedCollection={selectedCollection}
        colors={colors}
        theme={theme}
      />
    </>,
    document.body
  );
}