// components/modals/ApiGenerationModal.js - COMPLETE FIXED VERSION WITH LOADING STATE
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
  Edit, Edit3, Asterisk, Table, ChevronLeft, Wand2
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
} from "../../controllers/APIGenerationEngineController.js";

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
      { id: 'core-api-enquiry', name: 'Enquiry Services', description: 'Balance enquiries, statement enquiries' }
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
  { value: 'form-data', label: 'Form Data (multipart/form-data)', icon: <Upload className="h-4 w-4" /> },
  { value: 'urlencoded', label: 'URL Encoded (application/x-www-form-urlencoded)', icon: <Link className="h-4 w-4" /> },
  { value: 'raw', label: 'Raw Text (text/plain)', icon: <FileText className="h-4 w-4" /> }
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

// Oracle Data Types
const ORACLE_DATA_TYPES = [
  'VARCHAR2', 'NUMBER', 'DATE', 'TIMESTAMP', 'TIMESTAMP WITH TIME ZONE',
  'TIMESTAMP WITH LOCAL TIME ZONE', 'INTERVAL YEAR TO MONTH', 'INTERVAL DAY TO SECOND',
  'RAW', 'LONG RAW', 'CHAR', 'NCHAR', 'NVARCHAR2', 'CLOB', 'NCLOB', 'BLOB', 'JSONB', 'SYS_REFCURSOR',
  'BFILE', 'ROWID', 'UROWID', 'AUTOGENERATE'
];

// API Data Types
const API_DATA_TYPES = [
  'string', 'integer', 'number', 'boolean', 'array', 'object', 'null'
];

// Parameter Modes for procedures/functions
const PARAMETER_MODES = ['IN', 'OUT', 'IN/OUT'];

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
// In ApiGenerationModal.jsx, update the ObjectSelectorModal component
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
          databaseType: 'Oracle',
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

// New component for the preview modal
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
    !p.paramMode || p.paramMode === 'IN' || p.paramMode === 'IN/OUT'
  );

  // Filter response mappings to only show OUT parameters (and any other response fields)
  const outMappings = (apiData.responseMappings || []).filter(m => 
    (m.paramMode === 'OUT' || m.paramMode === 'IN/OUT' || !m.paramMode)
  );

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-[1560] p-4" style={{ zIndex: 1560 }}>
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
                      <span style={{ color: themeColors.textSecondary }}>HTTP Method:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {apiData.httpMethod}
                      </span>
                    </div>
                  </div>
                </div>
                <div className="text-right">
                  <div className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium" style={{ 
                    backgroundColor: themeColors.info + '20',
                    color: themeColors.info
                  }}>
                    <Globe className="h-4 w-4 mr-1" />
                    API Endpoint
                  </div>
                  <div className="mt-2 font-mono text-xs" style={{ color: themeColors.text }}>
                    {apiData.httpMethod} {apiData.basePath}{apiData.endpointPath}
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

            {/* Parameters Summary with Locations - ONLY SHOW IN PARAMETERS */}
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
                    {/* Group by location */}
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
                                  {param.paramMode && param.paramMode !== 'IN' && (
                                    <span className="ml-2 text-xs px-2 py-0.5 rounded" style={{ 
                                      backgroundColor: themeColors.warning + '20',
                                      color: themeColors.warning
                                    }}>
                                      {param.paramMode}
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
                    
                    {/* Body Type if any body parameters */}
                    {apiData.requestBody && apiData.requestBody.bodyType && inParameters.some(p => p.parameterLocation === 'body') && (
                      <div className="mt-3 pt-3 border-t" style={{ borderColor: themeColors.border }}>
                        <div className="flex items-center gap-2 text-xs">
                          <FileJson className="h-4 w-4" style={{ color: themeColors.success }} />
                          <span style={{ color: themeColors.textSecondary }}>Body Format:</span>
                          <span className="font-medium" style={{ color: themeColors.success }}>
                            {apiData.requestBody.bodyType.toUpperCase()}
                          </span>
                          {apiData.requestBody.bodyType === 'json' && (
                            <span className="text-xs" style={{ color: themeColors.textSecondary }}>
                              (application/json)
                            </span>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                ) : (
                  <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                    No input parameters defined
                  </p>
                )}
              </div>
            </div>

            {/* Response Mappings with Sample Response - SHOW OUT PARAMETERS + OTHER MAPPINGS */}
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
                  <>
                    <div className="grid grid-cols-2 gap-4 text-xs mb-4">
                      {outMappings.slice(0, 50).map((mapping, index) => (
                        <div key={index}>
                          <div className="font-medium" style={{ color: themeColors.text }}>
                            {mapping.apiField}
                          </div>
                          <div className="text-xs" style={{ color: themeColors.textSecondary }}>
                            → {mapping.dbColumn} ({mapping.oracleType})
                            {mapping.nullable && <span className="ml-1">(nullable)</span>}
                            {mapping.paramMode && <span className="ml-1">[{mapping.paramMode}]</span>}
                          </div>
                        </div>
                      ))}
                      {outMappings.length > 50 && (
                        <div className="col-span-2 text-center pt-2">
                          <span className="text-xs" style={{ color: themeColors.textSecondary }}>
                            + {outMappings.length - 50} more fields
                          </span>
                        </div>
                      )}
                    </div>

                    {/* Sample Success Response */}
                    {apiData.responseBody && apiData.responseBody.successSchema && (
                      <div className="mt-4 pt-4 border-t" style={{ borderColor: themeColors.border }}>
                        <h5 className="text-xs font-medium mb-2 flex items-center gap-1" style={{ color: themeColors.success }}>
                          <CheckCircle className="h-3 w-3" />
                          Sample Success Response (200 OK)
                        </h5>
                        <pre className="p-3 rounded text-xs font-mono overflow-x-auto" style={{ 
                          backgroundColor: themeColors.hover,
                          border: `1px solid ${themeColors.border}`,
                          color: themeColors.text,
                          maxHeight: '150px'
                        }}>
                          {apiData.responseBody.successSchema}
                        </pre>
                      </div>
                    )}
                  </>
                ) : (
                  <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                    No response fields defined
                  </p>
                )}
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
                            <span className="ml-1 font-mono" style={{ color:themeColors.text }}>
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
                      <Globe className="h-4 w-4 mr-1" />
                      API Endpoint
                    </div>
                    <div className="mt-2 font-mono text-xs" style={{ color: themeColors.text }}>
                      {transformedData.httpMethod || apiData.httpMethod} {transformedData.fullEndpoint || `${apiData.basePath}${apiData.endpointPath}`}
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
                          {transformedData.schemaConfig?.schemaName || 'HR'}_{apiData.apiCode}_PKG.sql
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

export default function ApiGenerationModal({
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
  isCustomQuery = false,
  customQueryText = '',
  extractedParams = []
}) {

  // Add this state to track source type
  const [sourceType, setSourceType] = useState(isCustomQuery ? 'custom_query' : 'database_object');
  const [customQuery, setCustomQuery] = useState(customQueryText);
  
  // Add a state to track if we're editing a custom query
  const [isEditingCustomQuery, setIsEditingCustomQuery] = useState(false);
  const [originalCustomQuery, setOriginalCustomQuery] = useState('');

  // ============ DETECT CUSTOM QUERY IN EDIT MODE ============
// This effect runs when editing an existing API to check if it's a custom query
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
      setSourceType('custom_query'); // <-- CRITICAL: Set sourceType here
      
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

  // Update the validateRequiredFields function to handle custom query mode
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
    if (!apiDetails.endpointPath?.trim()) {
      errors.endpointPath = 'Endpoint Path is required';
    }

    // For custom queries, check that there's a query
    if ((isCustomQuery || sourceType === 'custom_query' || isEditingCustomQuery) && !customQuery?.trim()) {
      errors.customQuery = 'Custom SELECT statement is required';
    }

    // For database objects (not custom queries), check schema config
    if (!isCustomQuery && !isEditingCustomQuery && !isEditing && selectedDbObject) {
      if (!schemaConfig.schemaName?.trim()) {
        errors.schemaName = 'Schema Name is required';
      }
      if (!schemaConfig.objectName?.trim()) {
        errors.objectName = 'Object Name is required';
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

  // Update the handleSave function to handle custom query mode (including editing)
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
      key: p.key, // Make sure key is preserved
      dbColumn: p.dbColumn // Make sure dbColumn is preserved
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
      bodyType: requestBody.bodyType || 'none'
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
    originalCustomQuery: isEditingCustomQuery ? originalCustomQuery : null
  };
  
  console.log('📦 Prepared API data for preview:', {
    id: apiData.id,
    name: apiData.apiName,
    code: apiData.apiCode,
    isCustomQuery: apiData.isCustomQuery,
    isEditing: apiData.isEditing,
    customQueryLength: apiData.customSelectStatement?.length || 0,
    parametersCount: apiData.parameters.length,
    mappingsCount: apiData.responseMappings.length
  });
  
  setNewApiData(apiData);
  setPreviewOpen(true);
};

  // Update the handlePreviewConfirm to send custom query data to the API (including edits)
  const handlePreviewConfirm = async () => {
    setPreviewOpen(false);
    setLoadingOpen(true);
    
    try {
      if (!selectedCollection || !selectedFolder) {
        throw new Error('Collection and folder are required');
      }

      const actualDatabaseType = selectedDbObject?.databaseType || databaseType || 'oracle';
      
      const isCustomQueryMode = isCustomQuery || sourceType === 'custom_query' || isEditingCustomQuery;
      
      console.log('🚀 Generating/Updating API for:', isCustomQueryMode ? 'Custom Query' : 'Database Object');
      console.log('📦 Database type:', actualDatabaseType);
      console.log('📝 isEditing:', isEditing);
      
      // Prepare the request object
      const generateRequest = {
        apiName: apiDetails.apiName,
        apiCode: apiDetails.apiCode,
        description: apiDetails.description,
        databaseType: actualDatabaseType,
        version: apiDetails.version,
        httpMethod: apiDetails.httpMethod,
        basePath: apiDetails.basePath,
        endpointPath: apiDetails.endpointPath,
        category: apiDetails.category,
        owner: apiDetails.owner,
        status: apiDetails.status,
        tags: apiDetails.tags,
        collectionInfo: {
          collectionId: selectedCollection.id,
          collectionName: selectedCollection.name,
          collectionType: selectedCollection.type,
          folderId: selectedFolder.id,
          folderName: selectedFolder.name
        },
        // Handle custom query
        useCustomQuery: isCustomQueryMode,
        customSelectStatement: isCustomQueryMode ? customQuery : null,
        // For database objects, include sourceObject
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
        schemaConfig: (!isCustomQueryMode && sourceType === 'database_object') ? {
          ...schemaConfig,
          databaseType: actualDatabaseType
        } : null,
        parameters: getInParameters().map(p => ({
          ...p,
          bodyFormat: p.parameterLocation === 'body' ? requestBody.bodyType : null
        })),
        responseMappings: getOutMappings(),
        requestBody,
        responseBody,
        authConfig,
        headers,
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
        settings,
        regenerateComponents: true
      };

      console.log('📤 Sending parameters:', getInParameters().map(p => ({ 
        key: p.key, 
        oracleType: p.oracleType,  // Should show JSONB after update
        parameterLocation: p.parameterLocation 
      })));

      console.log('📡 Sending request:', generateRequest);
      
      let response;
      
      if (isEditing && (selectedObject?.id || selectedObject?.data?.id)) {
        const apiId = selectedObject.data?.id || selectedObject.id;
        console.log('📡 Updating API with ID:', apiId);
        response = await updateApi(authToken, apiId, generateRequest);
      } else {
        response = await generateApi(authToken, generateRequest);
      }
      
      console.log('📥 API response:', response);

      setApiResponse(response);

      if (response.responseCode >= 200 && response.responseCode < 300) {
        const enrichedApiData = {
          ...newApiData,
          ...response.data,
          generatedFiles: response.data?.generatedFiles || newApiData?.generatedFiles,
          isEditing: isEditing
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

  // console.log("selectedObject::::::::" + JSON.stringify(selectedObject));

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
    owner: 'HR',
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
    successSchema: '{\n  "success": true,\n  "data": {},\n  "message": "Request processed successfully"\n}',
    errorSchema: '{\n  "success": false,\n  "error": {\n    "code": "ERROR_CODE",\n    "message": "Error description",\n    "details": {}\n  }\n}',
    includeMetadata: true,
    metadataFields: ['timestamp', 'apiVersion', 'requestId'],
    contentType: 'application/json',
    compression: 'gzip'
  });

  // Add a state to track the actual database type being used
  const [currentDatabaseType, setCurrentDatabaseType] = useState(databaseType || 'Oracle');


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

  // Handle API detail change
  const handleApiDetailChange = (field, value) => {
    setApiDetails(prev => ({ ...prev, [field]: value }));
    
    // Clear validation error for this field
    setValidationErrors(prev => ({ ...prev, [field]: null }));
    
    // Check code availability when API code changes (only for new APIs)
    if (field === 'apiCode' && value.length >= 3 && !isEditing) {
      // Clear previous timeout
      if (codeCheckTimeout) {
        clearTimeout(codeCheckTimeout);
      }
      
      // Set new timeout for debouncing
      const timerId = setTimeout(async () => {
        console.log('🔍 Checking API code availability after change:', value);
        const available = await checkCodeAvailability(value);
        console.log('📊 Code availability result:', available);
        
        if (!available) {
          setValidationErrors(prev => ({ 
            ...prev, 
            apiCode: `API code "${value}" is not available` 
          }));
        }
      }, 500); // Wait 500ms after user stops typing
      
      setCodeCheckTimeout(timerId);
    }
  };

  // Handle schema configuration with validation
  const handleSchemaConfigChange = (field, value) => {
    const updatedConfig = { ...schemaConfig, [field]: value };
    setSchemaConfig(updatedConfig);
    
    // Clear validation error for this field
    setValidationErrors(prev => ({ ...prev, [field]: null }));
  };

  // Handle parameter operations
 const handleAddParameter = () => {
  const timestamp = getCurrentTimestamp();
  const newParam = {
    id: `param-${Date.now()}`,
    key: '',
    dbColumn: '',
    oracleType: 'VARCHAR2', // Default is VARCHAR2, not AUTOGENERATE
    apiType: 'string',
    parameterLocation: 'query',
    required: true, // Default to required for non-AUTOGENERATE
    description: '',
    example: 'sample', // Default sample value
    validationPattern: '',
    defaultValue: '',
    inBody: false,
    isPrimaryKey: false,
    paramMode: 'IN'
  };
  
  // Only if the default is AUTOGENERATE (which it's not), set special values
  // This is handled by the change handler if user selects AUTOGENERATE
  
  setParameters([...parameters, newParam]);
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

const handleParameterChange = (id, field, value) => {
  setParameters(prevParams => {
    // First, find the current parameter to check its type
    const currentParam = prevParams.find(p => p.id === id);
    
    // Update the parameter
    const updatedParams = prevParams.map(param => 
      param.id === id ? { ...param, [field]: value } : param
    );
    
    // If changing the oracleType to AUTOGENERATE
    if (field === 'oracleType' && value === 'AUTOGENERATE') {
      const timestamp = getCurrentTimestamp();
      return updatedParams.map(param => 
        param.id === id ? { 
          ...param, 
          required: false,
          example: timestamp,
          defaultValue: timestamp,
          description: param.description || 'Auto-generated timestamp field'
        } : param
      );
    }
    
    // If changing the oracleType FROM AUTOGENERATE to something else
    if (field === 'oracleType' && currentParam?.oracleType === 'AUTOGENERATE' && value !== 'AUTOGENERATE') {
      // Generate a default sample value based on the new data type
      let defaultExample = 'sample';
      let defaultDescription = currentParam.description?.replace('Auto-generated timestamp field', '') || '';
      
      // Set example based on the new data type
      if (value === 'NUMBER') {
        defaultExample = '123';
      } else if (value === 'DATE') {
        defaultExample = '2024-01-01';
      } else if (value === 'TIMESTAMP') {
        defaultExample = '2024-01-01 12:00:00';
      } else if (value === 'VARCHAR2') {
        defaultExample = 'sample';
      } else if (value === 'CLOB') {
        defaultExample = 'Long text content...';
      } else if (value === 'BLOB') {
        defaultExample = 'binary_data';
      }
      
      return updatedParams.map(param => 
        param.id === id ? { 
          ...param, 
          required: true, // Reset required to true for non-AUTOGENERATE fields
          example: defaultExample,
          defaultValue: '',
          description: defaultDescription.trim() || `${param.key || 'Parameter'} field`,
          _autoGenerated: false
        } : param
      );
    }
    
    // If changing location to/from body, update inBody flag for that parameter
    if (field === 'parameterLocation') {
      return updatedParams.map(param => 
        param.id === id ? { ...param, inBody: value === 'body' } : param
      );
    }
    
    return updatedParams;
  });
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
  setResponseMappings(responseMappings.map(mapping => {
    // Find the current mapping
    const currentMapping = responseMappings.find(m => m.id === id);
    const updated = { ...mapping, [field]: value };
    
    // If changing oracleType to AUTOGENERATE
    if (field === 'oracleType' && value === 'AUTOGENERATE') {
      updated.nullable = false;
      if (updated.example !== undefined) {
        updated.example = getCurrentTimestamp();
      }
      updated.description = updated.description || 'Auto-generated timestamp field';
    }
    
    // If changing oracleType FROM AUTOGENERATE to something else
    if (field === 'oracleType' && currentMapping?.oracleType === 'AUTOGENERATE' && value !== 'AUTOGENERATE') {
      updated.nullable = true;
      updated.description = updated.description?.replace('Auto-generated timestamp field', '') || '';
      
      // Reset example if it exists
      if (updated.example !== undefined) {
        // Set example based on the new data type
        if (value === 'NUMBER') {
          updated.example = '123';
        } else if (value === 'DATE') {
          updated.example = '2024-01-01';
        } else if (value === 'TIMESTAMP') {
          updated.example = '2024-01-01T12:00:00Z';
        } else {
          updated.example = 'sample';
        }
      }
    }
    
    return updated;
  }));
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

  // Handle auth configuration changes with validation
  const handleAuthConfigChange = (field, value) => {
    setAuthConfig(prev => ({ ...prev, [field]: value }));
    
    // Clear validation error for this field
    setValidationErrors(prev => ({ ...prev, [field]: null }));
  };

  // Handle request body configuration
  const handleRequestBodyChange = (field, value) => {
    setRequestBody(prev => ({ ...prev, [field]: value }));
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
        schema: object.owner // PostgreSQL uses schema instead of owner
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
        console.log('📦 Found parameters in responseData.parameters:', parameters.length);
      } else if (responseData.arguments && Array.isArray(responseData.arguments)) {
        parameters = responseData.arguments;
        console.log('📦 Found parameters in responseData.arguments:', parameters.length);
      } else if (responseData.details?.parameters && Array.isArray(responseData.details.parameters)) {
        parameters = responseData.details.parameters;
        console.log('📦 Found parameters in responseData.details.parameters:', parameters.length);
      }
    }

    // Extract columns for tables/views
    if (object.type === 'TABLE' || object.type === 'VIEW') {
      if (responseData.columns && Array.isArray(responseData.columns)) {
        columns = responseData.columns;
        console.log('📦 Found columns in responseData.columns:', columns.length);
      } else if (responseData.targetObjectDetails?.columns) {
        columns = responseData.targetObjectDetails.columns;
        console.log('📦 Found columns in responseData.targetObjectDetails.columns:', columns.length);
      } else if (responseData.details?.columns && Array.isArray(responseData.details.columns)) {
        columns = responseData.details.columns;
        console.log('📦 Found columns in responseData.details.columns:', columns.length);
      }
    }

    // Log what we found
    console.log('📊 Extracted data:', {
      parametersCount: parameters.length,
      columnsCount: columns.length,
      objectName: object.name,
      objectType: object.type,
      databaseType: object.databaseType
    });

    // Determine the normalized database type for the radio buttons
    const normalizedDbType = object.databaseType?.toLowerCase() === 'postgresql' ? 'postgresql' : 'oracle';
    
    // Set current database type for the radio buttons
    setCurrentDatabaseType(normalizedDbType);
    console.log('🎯 Set currentDatabaseType to:', normalizedDbType);

    // Create a selected object with all details
    const detailedObject = {
      ...object,
      ...responseData,
      parameters: parameters,
      columns: columns,
      comment: responseData.comment || responseData.COMMENTS,
      databaseType: object.databaseType, // Preserve the database type from the selected object
      details: {
        ...responseData.details,
        parameters: parameters,
        columns: columns
      }
    };

    console.log('📦 Detailed object created:', {
      name: detailedObject.name,
      type: detailedObject.type,
      databaseType: detailedObject.databaseType,
      normalizedDbType: normalizedDbType,
      parametersCount: detailedObject.parameters?.length,
      columnsCount: detailedObject.columns?.length
    });

    // Set the selected object and populate form
    setSelectedDbObject(detailedObject);
    
    // Set source type to database_object (since we selected a database object)
    setSourceType('database_object');
    setIsEditingCustomQuery(false);
    
    // Populate the form with the detailed object
    await populateFormFromObject(detailedObject, true);
    
    console.log('✅ Object details loaded and form populated successfully');
    
  } catch (error) {
    console.error('❌ Error loading object details:', error);
    setObjectSelectorError('Failed to load object details: ' + error.message);
  } finally {
    setLoading(false);
  }
}, [authToken, populateFormFromObject]);

const populateFormFromApiData = useCallback(async (apiData) => {
  console.log('📝 populateFormFromApiData called with:', apiData);
  
  // ============ CHECK FOR CUSTOM QUERY FIRST ============
  const isCustomQueryApi = apiData?.isCustomQuery === true || 
                          apiData?.useCustomQuery === true ||
                          apiData?.sourceObject?.type === 'CUSTOM_QUERY' ||
                          apiData?.customSelectStatement ||
                          apiData?.sourceObject?.customSelectStatement ||
                          apiData?.sourceObject?.query;
  
  if (isCustomQueryApi) {
    console.log('✅ Detected custom query API in populateFormFromApiData');
    setIsEditingCustomQuery(true);
    setSourceType('custom_query');
    
    // Extract custom query text from various possible locations
    const customQueryText = apiData?.customSelectStatement || 
                           apiData?.sourceObject?.customSelectStatement ||
                           apiData?.sourceObject?.query ||
                           apiData?.sourceObject?.customQuery ||
                           apiData?.customQueryText ||
                           '';
    
    if (customQueryText) {
      setCustomQuery(customQueryText);
      setOriginalCustomQuery(customQueryText);
      console.log('📝 Set custom query:', customQueryText.substring(0, 100));
    }
    
    // Set database type for custom query
    const customQueryDbType = apiData?.databaseType || 
                              apiData?.sourceObject?.databaseType ||
                              databaseType || 
                              'oracle';
    // Normalize database type to lowercase for radio buttons
    const normalizedDbType = customQueryDbType.toLowerCase() === 'postgresql' ? 'postgresql' : 'oracle';
    setCurrentDatabaseType(normalizedDbType);
    console.log('📦 Database type for custom query:', normalizedDbType);
  } else {
    // Reset custom query states if not a custom query
    setIsEditingCustomQuery(false);
    setSourceType('database_object');
    setCustomQuery('');
    setOriginalCustomQuery('');
    
    // Set database type for database object
    const dbType = apiData?.databaseType || 
                   apiData?.sourceObject?.databaseType ||
                   apiData?.schemaConfig?.databaseType ||
                   databaseType || 
                   'oracle';
    // Normalize database type to lowercase for radio buttons
    const normalizedDbType = dbType.toLowerCase() === 'postgresql' ? 'postgresql' : 'oracle';
    setCurrentDatabaseType(normalizedDbType);
    console.log('📦 Database type for database object:', normalizedDbType);
  }
  
  // ============ SET API DETAILS ============
  setApiDetails({
    apiName: apiData.apiName || '',
    apiCode: apiData.apiCode || '',
    description: apiData.description || '',
    version: apiData.version || '1.0.0',
    status: apiData.status || 'ACTIVE',
    httpMethod: apiData.httpMethod || 'GET',
    basePath: apiData.basePath || '/api/v1',
    endpointPath: apiData.endpointPath || '',
    tags: apiData.tags || ['default'],
    category: apiData.category || 'general',
    owner: apiData.owner || 'HR',
  });

  // ============ SET COLLECTION INFO ============
  if (apiData.collectionInfo) {
    const collection = collections.find(c => c.id === apiData.collectionInfo.collectionId);
    if (collection) {
      setSelectedCollection(collection);
      setFolders(collection.folders || []);
      
      if (apiData.collectionInfo.folderId) {
        const folder = collection.folders?.find(f => f.id === apiData.collectionInfo.folderId);
        setSelectedFolder(folder || null);
      }
    }
  }

  // ============ SET SCHEMA CONFIG (for both custom query and database object) ============
  if (apiData.schemaConfig) {
    setSchemaConfig({
      schemaName: apiData.schemaConfig.schemaName || '',
      objectType: apiData.schemaConfig.objectType || '',
      objectName: apiData.schemaConfig.objectName || '',
      operation: apiData.schemaConfig.operation || 'SELECT',
      primaryKeyColumn: apiData.schemaConfig.primaryKeyColumn || '',
      sequenceName: apiData.schemaConfig.sequenceName || '',
      enablePagination: apiData.schemaConfig.enablePagination !== undefined ? apiData.schemaConfig.enablePagination : true,
      pageSize: apiData.schemaConfig.pageSize || 10,
      enableSorting: apiData.schemaConfig.enableSorting !== undefined ? apiData.schemaConfig.enableSorting : true,
      defaultSortColumn: apiData.schemaConfig.defaultSortColumn || '',
      defaultSortDirection: apiData.schemaConfig.defaultSortDirection || 'ASC'
    });
  } else if (!isCustomQueryApi && apiData.sourceObject && apiData.sourceObject.type !== 'CUSTOM_QUERY') {
    // If no schemaConfig but we have sourceObject (legacy format)
    setSchemaConfig({
      schemaName: apiData.sourceObject.owner || '',
      objectType: apiData.sourceObject.type || '',
      objectName: apiData.sourceObject.name || '',
      operation: apiData.operation || 'SELECT',
      primaryKeyColumn: apiData.primaryKeyColumn || '',
      sequenceName: apiData.sequenceName || '',
      enablePagination: apiData.enablePagination !== undefined ? apiData.enablePagination : true,
      pageSize: apiData.pageSize || 10,
      enableSorting: apiData.enableSorting !== undefined ? apiData.enableSorting : true,
      defaultSortColumn: apiData.defaultSortColumn || '',
      defaultSortDirection: apiData.defaultSortDirection || 'ASC'
    });
  }

  // ============ SET SOURCE OBJECT INFO ============
  if (apiData.sourceObject) {
    setSourceObjectInfo({
      isSynonym: apiData.sourceObject.isSynonym || false,
      targetType: apiData.sourceObject.targetType || null,
      targetName: apiData.sourceObject.targetName || null,
      targetOwner: apiData.sourceObject.targetOwner || null
    });
  }

  // ============ SET SELECTED DB OBJECT (for editing mode) ============
  if (!isCustomQueryApi && apiData.sourceObject && apiData.sourceObject.type !== 'CUSTOM_QUERY') {
    // Determine database type for the selected object
    const objDbType = apiData.databaseType || 
                      apiData.sourceObject.databaseType || 
                      apiData.schemaConfig?.databaseType ||
                      databaseType || 
                      'oracle';
    
    setSelectedDbObject({
      name: apiData.sourceObject.name || apiData.schemaConfig?.objectName,
      owner: apiData.sourceObject.owner || apiData.schemaConfig?.schemaName,
      type: apiData.sourceObject.type || apiData.schemaConfig?.objectType,
      databaseType: objDbType,
      isSynonym: apiData.sourceObject.isSynonym || false,
      targetType: apiData.sourceObject.targetType,
      targetName: apiData.sourceObject.targetName,
      targetOwner: apiData.sourceObject.targetOwner,
      columns: apiData.sourceObject.columns || apiData.columns,
      parameters: apiData.sourceObject.parameters || apiData.parameters
    });
  }

  // ============ SET PARAMETERS ============
  if (apiData.parameters && Array.isArray(apiData.parameters)) {
  const paramsWithIds = apiData.parameters.map((p, idx) => ({
    ...p,  // Spread all properties from the saved parameter (including oracleType)
    id: p.id || `param-${Date.now()}-${idx}`,
    key: p.key || p.parameterName,
    dbColumn: p.dbColumn || p.key,
    // IMPORTANT: Preserve the stored oracleType exactly as saved
    oracleType: p.oracleType || p.dataType || 'VARCHAR2',
    apiType: p.apiType || 'string',
    parameterLocation: p.parameterLocation || (isCustomQueryApi ? 'query' : 'body'),
    required: p.required !== undefined ? p.required : true,
    description: p.description || `Parameter: ${p.key || p.parameterName}`,
    example: p.example || '',
    validationPattern: p.validationPattern || '',
    defaultValue: p.defaultValue || '',
    inBody: p.inBody !== undefined ? p.inBody : (p.parameterLocation === 'body'),
    isPrimaryKey: p.isPrimaryKey || false,
    paramMode: p.paramMode || (isCustomQueryApi ? 'IN' : 'IN')
  }));
  setParameters(paramsWithIds);
  console.log('📦 Loaded parameters from API data:', paramsWithIds.map(p => ({ 
    key: p.key, 
    oracleType: p.oracleType,
    originalType: p.oracleType  // Log the actual type being set
  })));
}


  console.log('📦 Loaded parameters from API data:', {
    count: apiData.parameters?.length,
    firstParam: apiData.parameters?.[0],
    rawData: apiData.parameters
  });

  // ============ SET RESPONSE MAPPINGS ============
  if (apiData.responseMappings && Array.isArray(apiData.responseMappings)) {
    const mappingsWithIds = apiData.responseMappings.map((m, idx) => ({
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
  } else if (isCustomQueryApi) {
    // For custom queries with no response mappings, initialize empty array
    setResponseMappings([]);
  }

  // ============ SET REQUEST BODY ============
  if (apiData.requestBody) {
    setRequestBody({
      bodyType: apiData.requestBody.bodyType || (isCustomQueryApi ? 'none' : 'json'),
      sample: apiData.requestBody.sample || '',
      requiredFields: apiData.requestBody.requiredFields || [],
      validateSchema: apiData.requestBody.validateSchema !== undefined ? apiData.requestBody.validateSchema : true,
      maxSize: apiData.requestBody.maxSize || 1048576,
      allowedMediaTypes: apiData.requestBody.allowedMediaTypes || ['application/json']
    });
  } else {
    setRequestBody({
      bodyType: isCustomQueryApi ? 'none' : 'json',
      sample: null,
      requiredFields: [],
      validateSchema: true,
      maxSize: 1048576,
      allowedMediaTypes: ['application/json']
    });
  }

  // ============ SET RESPONSE BODY ============
  if (apiData.responseBody) {
    setResponseBody({
      successSchema: apiData.responseBody.successSchema || '{\n  "success": true,\n  "data": {},\n  "message": "Request processed successfully"\n}',
      errorSchema: apiData.responseBody.errorSchema || '{\n  "success": false,\n  "error": {\n    "code": "ERROR_CODE",\n    "message": "Error description",\n    "details": {}\n  }\n}',
      includeMetadata: apiData.responseBody.includeMetadata !== undefined ? apiData.responseBody.includeMetadata : true,
      metadataFields: apiData.responseBody.metadataFields || ['timestamp', 'apiVersion', 'requestId'],
      contentType: apiData.responseBody.contentType || 'application/json',
      compression: apiData.responseBody.compression || 'gzip'
    });
  }

  // ============ SET AUTH CONFIG ============
  if (apiData.authConfig) {
    setAuthConfig({
      authType: apiData.authConfig.authType || 'none',
      apiKeyHeader: apiData.authConfig.apiKeyHeader || 'X-API-Key',
      apiKeyValue: apiData.authConfig.apiKeyValue || '',
      apiSecretHeader: apiData.authConfig.apiSecretHeader || 'X-API-Secret',
      apiSecretValue: apiData.authConfig.apiSecretValue || '',
      jwtToken: apiData.authConfig.jwtToken || '',
      jwtIssuer: apiData.authConfig.jwtIssuer || 'api.example.com',
      basicUsername: apiData.authConfig.basicUsername || '',
      basicPassword: apiData.authConfig.basicPassword || '',
      ipWhitelist: apiData.authConfig.ipWhitelist || '',
      rateLimitRequests: apiData.authConfig.rateLimitRequests || 100,
      rateLimitPeriod: apiData.authConfig.rateLimitPeriod || 'minute',
      enableRateLimiting: apiData.authConfig.enableRateLimiting || false,
      corsOrigins: apiData.authConfig.corsOrigins || ['*'],
      auditLevel: apiData.authConfig.auditLevel || 'standard'
    });
  }

  // ============ SET HEADERS ============
  if (apiData.headers && Array.isArray(apiData.headers)) {
    const headersWithIds = apiData.headers.map((h, idx) => ({
      ...h,
      id: h.id || `header-${Date.now()}-${idx}`,
      key: h.key || h.name,
      value: h.value || '',
      required: h.required !== undefined ? h.required : false,
      description: h.description || ''
    }));
    setHeaders(headersWithIds);
  } else {
    // Set default headers if none exist
    setHeaders([
      { id: '1', key: 'Content-Type', value: 'application/json', required: true, description: 'Response content type' },
      { id: '2', key: 'Cache-Control', value: 'no-cache', required: false, description: 'Cache control header' }
    ]);
  }

  // ============ SET SETTINGS ============
  if (apiData.settings) {
    setSettings({
      timeout: apiData.settings.timeout || 30000,
      maxRecords: apiData.settings.maxRecords || 1000,
      enableLogging: apiData.settings.enableLogging !== undefined ? apiData.settings.enableLogging : true,
      logLevel: apiData.settings.logLevel || 'INFO',
      enableCaching: apiData.settings.enableCaching || false,
      cacheTtl: apiData.settings.cacheTtl || 300,
      generateSwagger: apiData.settings.generateSwagger !== undefined ? apiData.settings.generateSwagger : true,
      generatePostman: apiData.settings.generatePostman !== undefined ? apiData.settings.generatePostman : true,
      generateClientSDK: apiData.settings.generateClientSDK !== undefined ? apiData.settings.generateClientSDK : true,
      enableMonitoring: apiData.settings.enableMonitoring !== undefined ? apiData.settings.enableMonitoring : true,
      enableAlerts: apiData.settings.enableAlerts || false,
      alertEmail: apiData.settings.alertEmail || '',
      enableTracing: apiData.settings.enableTracing || false,
      corsEnabled: apiData.settings.corsEnabled !== undefined ? apiData.settings.corsEnabled : true
    });
  }

  // ============ SET TESTS ============
  if (apiData.tests) {
    setTests({
      testConnection: apiData.tests.testConnection !== undefined ? apiData.tests.testConnection : true,
      testObjectAccess: apiData.tests.testObjectAccess !== undefined ? apiData.tests.testObjectAccess : true,
      testPrivileges: apiData.tests.testPrivileges !== undefined ? apiData.tests.testPrivileges : true,
      testDataTypes: apiData.tests.testDataTypes !== undefined ? apiData.tests.testDataTypes : true,
      testNullConstraints: apiData.tests.testNullConstraints !== undefined ? apiData.tests.testNullConstraints : true,
      testUniqueConstraints: apiData.tests.testUniqueConstraints || false,
      testForeignKeyReferences: apiData.tests.testForeignKeyReferences || false,
      testQueryPerformance: apiData.tests.testQueryPerformance !== undefined ? apiData.tests.testQueryPerformance : true,
      performanceThreshold: apiData.tests.performanceThreshold || 1000,
      testWithSampleData: apiData.tests.testWithSampleData !== undefined ? apiData.tests.testWithSampleData : true,
      sampleDataRows: apiData.tests.sampleDataRows || 10,
      testProcedureExecution: apiData.tests.testProcedureExecution !== undefined ? apiData.tests.testProcedureExecution : true,
      testFunctionReturn: apiData.tests.testFunctionReturn !== undefined ? apiData.tests.testFunctionReturn : true,
      testExceptionHandling: apiData.tests.testExceptionHandling !== undefined ? apiData.tests.testExceptionHandling : true,
      testSQLInjection: apiData.tests.testSQLInjection !== undefined ? apiData.tests.testSQLInjection : true,
      testAuthentication: apiData.tests.testAuthentication !== undefined ? apiData.tests.testAuthentication : true,
      testAuthorization: apiData.tests.testAuthorization !== undefined ? apiData.tests.testAuthorization : true,
      testData: apiData.tests.testData || {},
      testQueries: apiData.tests.testQueries || []
    });
  }

  // ============ SET CURRENT DATABASE TYPE (already set at top) ============
  // Ensure currentDatabaseType is properly set for database objects
  if (!isCustomQueryApi && (!currentDatabaseType || currentDatabaseType === 'oracle')) {
    const dbType = apiData?.databaseType || 
                   apiData?.sourceObject?.databaseType ||
                   apiData?.schemaConfig?.databaseType ||
                   databaseType || 
                   'oracle';
    const normalizedDbType = dbType.toLowerCase() === 'postgresql' ? 'postgresql' : 'oracle';
    setCurrentDatabaseType(normalizedDbType);
  }

  // ============ LOG COMPLETION ============
  console.log('✅ Form populated from API data', {
    isCustomQuery: isCustomQueryApi,
    sourceType: isCustomQueryApi ? 'custom_query' : 'database_object',
    apiName: apiData.apiName,
    apiCode: apiData.apiCode,
    parametersCount: apiData.parameters?.length || 0,
    responseMappingsCount: apiData.responseMappings?.length || 0,
    databaseType: currentDatabaseType
  });
  
}, [collections, databaseType]);

  // Function to populate form from selected object - FIXED VERSION WITH PROPER MODE FILTERING
const populateFormFromObject = useCallback((object, preserveExistingApiDetails = false) => {
  console.log('📝 populateFormFromObject called with object:', object);
  console.log('📝 preserveExistingApiDetails:', preserveExistingApiDetails);
  console.log('📝 Object database type:', object.databaseType);
  console.log('📝 Object type:', object.type);
  console.log('📝 Object name:', object.name);
  
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

  // Get HTTP method from existing state or default based on object type
  let httpMethod = apiDetails.httpMethod;
  
  // For procedures/functions, force POST
  if (objectType === 'PROCEDURE' || objectType === 'FUNCTION' || objectType === 'PACKAGE') {
    httpMethod = 'POST';
  } else {
    httpMethod = httpMethod || 'GET';
  }

  // Determine operation based on HTTP method and object type
  let operation = 'SELECT';
  
  // For procedures/functions/packages, always use EXECUTE
  if (objectType === 'PROCEDURE' || objectType === 'FUNCTION' || objectType === 'PACKAGE') {
    operation = 'EXECUTE';
  } else {
    // For tables/views, map HTTP method to operation
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
      owner: object.owner || 'HR',
      httpMethod: httpMethod
    }));
  } else {
    // Only update the fields that should be updated from the object
    setApiDetails(prev => ({
      ...prev,
      // Only update these if they are empty or if we specifically want to
      endpointPath: prev.endpointPath || endpointPath,
      owner: prev.owner || object.owner || 'HR',
      httpMethod: prev.httpMethod || httpMethod
    }));
  }

  // Set schema config with proper operation based on HTTP method
  setSchemaConfig(prev => ({
    ...prev,
    schemaName: object.owner || 'HR',
    objectType: objectType || 'TABLE',
    objectName: object.name || object.objectName || '',
    operation: operation,
    primaryKeyColumn: ''
  }));

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
    // Extract parameter data with fallbacks for different naming conventions
    // PRIORITIZE: key, then ARGUMENT_NAME, then argument_name, then name, then NAME
    const paramName = param.key || 
                      param.ARGUMENT_NAME || 
                      param.argument_name || 
                      param.name || 
                      param.NAME || 
                      `param_${index + 1}`;
    
    const paramType = param.oracleType || 
                      param.DATA_TYPE || 
                      param.data_type || 
                      param.type || 
                      param.TYPE || 
                      'VARCHAR2';
    
    const paramMode = param.paramMode || 
                      param.IN_OUT || 
                      param.in_out || 
                      param.mode || 
                      param.MODE || 
                      'IN';
    
    // Normalize the mode
    let normalizedMode = paramMode?.toString().toUpperCase().replace(/\s+/g, '_') || 'IN';
    if (normalizedMode === 'INOUT') {
      normalizedMode = 'IN/OUT';
    }
    
    console.log(`🔍 Processing param ${index}:`, { 
      paramName, 
      paramType, 
      paramMode: normalizedMode,
      original: paramMode,
      originalKey: param.key,
      originalDbColumn: param.dbColumn
    });
    
    // Generate a clean key name - preserve the original if it exists
    let cleanKey = paramName;
    if (typeof paramName === 'string') {
      // Only clean if it's a generated name, not if it's from the original data
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
    
    // Determine parameter location based on mode and HTTP method
    let parameterLocation = param.parameterLocation || 'query';
    
    const isInParam = normalizedMode === 'IN' || normalizedMode === 'IN/OUT';
    const isOutParam = normalizedMode === 'OUT' || normalizedMode === 'IN/OUT';
    
    // If parameter location is not set, determine from HTTP method
    if (!param.parameterLocation) {
      if (isInParam && (httpMethod === 'POST' || httpMethod === 'PUT' || httpMethod === 'PATCH')) {
        parameterLocation = 'body';
      } else if (isInParam && httpMethod === 'GET') {
        parameterLocation = 'query';
      }
    }

    // Determine database type for type mapping
    const dbType = object.databaseType?.toLowerCase() || 'oracle';
    
    // Determine type based on database type
    let oracleType = 'VARCHAR2';
    let apiType = 'string';
    
    if (dbType === 'postgresql') {
      if (paramType.includes('VARCHAR') || paramType.includes('CHAR') || paramType.includes('TEXT')) {
        oracleType = 'VARCHAR2';
        apiType = 'string';
      } else if (paramType.includes('INT') || paramType.includes('BIGINT') || paramType.includes('SMALLINT') || 
                 paramType.includes('DECIMAL') || paramType.includes('NUMERIC') || paramType.includes('FLOAT') ||
                 paramType.includes('DOUBLE') || paramType.includes('REAL')) {
        oracleType = 'NUMBER';
        apiType = 'integer';
      } else if (paramType.includes('DATE') || paramType.includes('TIME') || paramType.includes('TIMESTAMP')) {
        oracleType = 'DATE';
        apiType = 'string';
      } else if (paramType.includes('BOOLEAN')) {
        oracleType = 'VARCHAR2';
        apiType = 'boolean';
      } else {
        oracleType = 'VARCHAR2';
        apiType = 'string';
      }
    } else {
      if (paramType.includes('VARCHAR') || paramType.includes('CHAR')) {
        oracleType = 'VARCHAR2';
        apiType = 'string';
      } else if (paramType.includes('NUMBER') || paramType.includes('INT') || paramType.includes('FLOAT')) {
        oracleType = 'NUMBER';
        apiType = 'integer';
      } else if (paramType.includes('DATE')) {
        oracleType = 'DATE';
        apiType = 'string';
      } else if (paramType.includes('TIMESTAMP')) {
        oracleType = 'TIMESTAMP';
        apiType = 'string';
      } else if (paramType.includes('CLOB')) {
        oracleType = 'CLOB';
        apiType = 'string';
      } else {
        oracleType = 'VARCHAR2';
        apiType = 'string';
      }
    }

    // Preserve existing values if available
    const required = param.required !== undefined ? param.required : isInParam;
    const description = param.description || `${paramName} (${normalizedMode})`;
    const example = param.example || (oracleType === 'NUMBER' ? '1000' : 
                                      oracleType === 'DATE' ? '2024-01-01' : 
                                      oracleType === 'CLOB' ? '{...}' : 'sample');
    const defaultValue = param.defaultValue || param.DATA_DEFAULT || '';
    const inBody = param.inBody !== undefined ? param.inBody : (parameterLocation === 'body');

    // Only add to parameters array for IN and IN/OUT parameters
    if (isInParam) {
      // Check if the parameter type is AUTOGENERATE
      const isAutoGenerate = oracleType === 'AUTOGENERATE';
      
      newParameters.push({
        id: param.id || `proc-param-${Date.now()}-${index}`,
        key: cleanKey,
        dbColumn: param.dbColumn || paramName,
        oracleType: oracleType,
        apiType: apiType,
        parameterLocation: parameterLocation,
        required: isAutoGenerate ? false : required, // AUTO GENERATE = NOT required
        description: description,
        example: isAutoGenerate ? 'Auto-generated timestamp' : example,
        validationPattern: param.validationPattern || '',
        defaultValue: isAutoGenerate ? getCurrentTimestamp() : defaultValue, // Set timestamp default
        inBody: inBody,
        isPrimaryKey: param.isPrimaryKey || false,
        paramMode: normalizedMode
      });
      console.log(`✅ Added to PARAMETERS tab: ${cleanKey} (${normalizedMode})`);
    }

    // Only add to response mappings for OUT and IN/OUT parameters
    if (isOutParam) {
      const isAutoGenerate = oracleType === 'AUTOGENERATE';
      
      newMappings.push({
        id: param.id || `mapping-out-${Date.now()}-${index}`,
        apiField: cleanKey,
        dbColumn: param.dbColumn || paramName,
        oracleType: oracleType,
        apiType: apiType,
        format: param.format || (oracleType === 'DATE' ? 'date-time' : ''),
        nullable: isAutoGenerate ? false : (param.nullable !== undefined ? param.nullable : true),
        isPrimaryKey: param.isPrimaryKey || false,
        includeInResponse: param.includeInResponse !== undefined ? param.includeInResponse : true,
        inResponse: param.inResponse !== undefined ? param.inResponse : true,
        paramMode: normalizedMode
      });
    }
  });

    // For functions, handle return type
    const returnType = object.RETURN_TYPE || object.return_type || object.returnType || object.details?.returnType;
    if (returnType && objectType === 'FUNCTION') {
      const dbType = object.databaseType?.toLowerCase() || 'oracle';
      let oracleType = 'VARCHAR2';
      
      if (dbType === 'postgresql') {
        if (returnType.includes('INT') || returnType.includes('BIGINT') || returnType.includes('NUMERIC')) {
          oracleType = 'NUMBER';
        } else if (returnType.includes('DATE') || returnType.includes('TIMESTAMP')) {
          oracleType = 'DATE';
        } else if (returnType.includes('BOOLEAN')) {
          oracleType = 'VARCHAR2';
        } else {
          oracleType = 'VARCHAR2';
        }
      } else {
        if (returnType.includes('NUMBER') || returnType.includes('INT') || returnType.includes('FLOAT')) {
          oracleType = 'NUMBER';
        } else if (returnType.includes('DATE')) {
          oracleType = 'DATE';
        } else if (returnType.includes('VARCHAR') || returnType.includes('CHAR')) {
          oracleType = 'VARCHAR2';
        } else {
          oracleType = 'VARCHAR2';
        }
      }

      let apiType = 'string';
      if (oracleType === 'NUMBER') {
        apiType = 'integer';
      }

      newMappings.push({
        id: `mapping-return-${Date.now()}`,
        apiField: 'result',
        dbColumn: 'RETURN_VALUE',
        oracleType: oracleType,
        apiType: apiType,
        format: oracleType === 'DATE' ? 'date-time' : '',
        nullable: false,
        isPrimaryKey: false,
        includeInResponse: true,
        inResponse: true,
        paramMode: 'OUT'
      });
      console.log('✅ Added function return mapping');
    }
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
        
        newParameters.push({
          id: `param-col-${Date.now()}-${index}`,
          key: cleanKey,
          dbColumn: colName,
          oracleType: oracleType,
          apiType: apiType,
          parameterLocation: parameterLocation,
          required: isPrimaryKey || colNullable === 'N',
          description: col.comment || col.COMMENTS || `From ${object.name || object.objectName}.${colName}`,
          example: colName.includes('ID') ? '1' : 
                  colName.includes('DATE') ? '2024-01-01' :
                  colName.includes('NAME') ? 'Sample' : '',
          validationPattern: '',
          defaultValue: col.DATA_DEFAULT || col.defaultValue || '',
          inBody: parameterLocation === 'body',
          isPrimaryKey: isPrimaryKey,
          paramMode: null
        });

        newMappings.push({
          id: `mapping-col-${Date.now()}-${index}`,
          apiField: cleanKey,
          dbColumn: colName,
          oracleType: oracleType,
          apiType: apiType,
          format: oracleType === 'DATE' ? 'date-time' : '',
          nullable: colNullable === 'Y',
          isPrimaryKey: isPrimaryKey,
          includeInResponse: true,
          inResponse: true,
          paramMode: null
        });
      }
    });
  }

  console.log('📊 Final results:', {
    parametersCount: newParameters.length,
    mappingsCount: newMappings.length,
    inCount: newParameters.filter(p => p.paramMode === 'IN' || p.paramMode === 'IN/OUT' || p.paramMode === null).length,
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
  const getInParameters = () => {
    return parameters.filter(p => 
      !p.paramMode || p.paramMode === 'IN' || p.paramMode === 'IN/OUT'
    );
  };

  // Helper function to filter response mappings to only show OUT parameters and other mappings
  const getOutMappings = () => {
    return responseMappings.filter(m => 
      (m.paramMode === 'OUT' || m.paramMode === 'IN/OUT' || !m.paramMode)
    );
  };

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
      setApiCodeExists(!available);
      
      return available;
    } catch (error) {
      console.error('Error checking code availability:', error);
      setApiCodeExists(false);
      return true;
    }
  };

  // Sync operation when HTTP method changes
  useEffect(() => {
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
  }, [apiDetails.httpMethod, selectedDbObject, selectedObject]);

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
      await populateFormFromObject(selectedDbObject, false);
      return;
    }

    if (!selectedObject) {
      console.log('ℹ️ ApiGenerationModal - No selected object provided, showing empty form');
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
    
    console.log('🔍 ApiGenerationModal - Initializing with selected object:', {
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

  // Auto-set body type based on HTTP method
  useEffect(() => {
    // Skip for editing mode - preserve existing body type
    if (isEditing) return;
    
    const method = apiDetails.httpMethod;
    
    if (method === 'GET' || method === 'DELETE') {
      // For GET and DELETE, set body type to 'none'
      setRequestBody(prev => ({
        ...prev,
        bodyType: 'none'
      }));
    } else {
      // For POST, PUT, PATCH, etc., set body type to 'json'
      setRequestBody(prev => ({
        ...prev,
        bodyType: 'json'
      }));
    }
  }, [apiDetails.httpMethod, isEditing]); // Run whenever HTTP method changes, skip for editing

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
        } : undefined
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
}, [previewMode, apiDetails, schemaConfig, parameters, responseMappings, requestBody, responseBody, authConfig, settings, sourceObjectInfo, tests, validationResult, selectedCollection, selectedFolder, isEditing]);


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
  
  const blob = new Blob([generatedCode], { type: extension === 'sql' ? 'text/plain' : 'application/json' });
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

  // Tab definitions
  const tabs = [
    { id: 'definition', label: 'Definition', icon: <FileText className="h-4 w-4" /> },
    { id: 'schema', label: 'Schema', icon: <Database className="h-4 w-4" /> },
    { id: 'parameters', label: 'Parameters', icon: <Hash className="h-4 w-4" /> },
    { id: 'mapping', label: 'Mapping', icon: <Map className="h-4 w-4" /> },
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
          {/* Header - MODIFIED to show loading state */}
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
                
                {/* {isEditing && !selectedObject?.loading && (
                  <p className="text-xs mt-1" style={{ color: themeColors.success }}>
                    <CheckCircle className="h-3 w-3 inline mr-1" />
                    Editing existing API
                  </p>
                )} */}

                {/* Dashboard selected object indicator - UPDATED with database type badge */}
                {fromDashboard && selectedDbObject && (
                  <div className="flex items-center gap-2 mt-1">
                    <p className="text-xs" style={{ color: themeColors.success }}>
                      <CheckCircle className="h-3 w-3 inline mr-1" />
                      Selected: {selectedDbObject.owner}.{selectedDbObject.name} ({selectedDbObject.type})
                    </p>
                    {/* Database Type Badge */}
                    <span className="text-xs px-2 py-0.5 rounded-full" style={{ 
                      backgroundColor: selectedDbObject.databaseType === 'PostgreSQL' ? '#3b82f620' : '#ef444420',
                      color: selectedDbObject.databaseType === 'PostgreSQL' ? '#3b82f6' : '#ef4444'
                    }}>
                      {selectedDbObject.databaseType === 'PostgreSQL' ? 'PostgreSQL' : 'Oracle'}
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

                {/* Database Type Indicator - ADD THIS NEW SECTION */}
                {/* {(currentDatabaseType || selectedDbObject?.databaseType || databaseType) && (
                  <div className="mt-2 p-2 rounded-lg border" style={{ 
                    backgroundColor: currentDatabaseType === 'PostgreSQL' ? '#3b82f610' : '#ef444410',
                    borderColor: currentDatabaseType === 'PostgreSQL' ? '#3b82f640' : '#ef444440'
                  }}>
                    <div className="flex items-center gap-2">
                      <Database size={14} style={{ 
                        color: currentDatabaseType === 'PostgreSQL' ? '#3b82f6' : '#ef4444' 
                      }} />
                      <span className="text-xs font-medium" style={{ color: themeColors.text }}>
                        Generating API for:
                      </span>
                      <span className="text-xs font-semibold" style={{ 
                        color: currentDatabaseType === 'PostgreSQL' ? '#3b82f6' : '#ef4444' 
                      }}>
                        {currentDatabaseType === 'PostgreSQL' ? 'PostgreSQL Database' : 'Oracle Database'}
                      </span>
                    </div>
                    <p className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>
                      {currentDatabaseType === 'PostgreSQL' 
                        ? 'API will be optimized for PostgreSQL syntax and features' 
                        : 'API will be optimized for Oracle PL/SQL syntax and features'}
                    </p>
                  </div>
                )} */}

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

                {/* Selected Collection Info */}
                {/* {selectedCollection && !isAddingNewCollection && (
                  <div className="mt-2 p-2 rounded text-xs" style={{ 
                    backgroundColor: themeColors.hover
                  }}>
                    <div className="flex items-center justify-between">
                      <span style={{ color: themeColors.textSecondary }}>Collection:</span>
                      <span className="font-medium" style={{ color: themeColors.text }}>{selectedCollection.name}</span>
                    </div>
                    <div className="flex items-center justify-between mt-1">
                      <span style={{ color: themeColors.textSecondary }}>Type:</span>
                      <span style={{ color: themeColors.info }}>{selectedCollection.type}</span>
                    </div>
                  </div>
                )} */}
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
                (field, value) => handleApiDetailChange(field, value),
                'Users API'
              )}

              {renderRequiredInput(
                'apiCode',
                'API Code',
                apiDetails.apiCode,
                (field, value) => handleApiDetailChange(field, value),
                'GET_USERS',
                'text',
                isEditing // Disable code editing when editing
              )}

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

              {renderRequiredInput(
                'endpointPath',
                'Endpoint Path',
                apiDetails.endpointPath,
                (field, value) => handleApiDetailChange(field, value),
                '/users'
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
                  placeholder="HR"
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
                          <Globe className="h-4 w-4" />
                          API Endpoint Preview
                        </h4>
                        <div className="font-mono text-xs p-3 rounded border" style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: themeColors.border
                        }}>
                          <div style={{ color: themeColors.textSecondary }}>
                            {apiDetails.httpMethod} {apiDetails.basePath}{apiDetails.endpointPath}
                          </div>
                          {getInParameters().filter(p => p.parameterLocation === 'query').length > 0 && (
                            <div className="mt-2 text-xs" style={{ color: themeColors.textSecondary }}>
                              Query Parameters: {getInParameters().filter(p => p.parameterLocation === 'query').map(p => p.key).join(', ')}
                            </div>
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
                      <div className="grid grid-cols-2 gap-4 text-xs">
                        <div>
                          <span style={{ color: themeColors.textSecondary }}>Object:</span>
                          <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                            {selectedDbObject?.owner || selectedObject?.owner}.{selectedDbObject?.name || selectedObject?.name}
                          </span>
                        </div>
                        <div>
                          <span style={{ color: themeColors.textSecondary }}>Type:</span>
                          <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                            {selectedDbObject?.type || selectedObject?.type}
                          </span>
                        </div>
                        {selectedDbObject?.columns && (
                          <div>
                            <span style={{ color: themeColors.textSecondary }}>Columns:</span>
                            <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                              {selectedDbObject.columns.length}
                            </span>
                          </div>
                        )}
                        {selectedDbObject?.parameters && (
                          <div>
                            <span style={{ color: themeColors.textSecondary }}>Parameters:</span>
                            <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                              {selectedDbObject.parameters.length}
                            </span>
                          </div>
                        )}
                        {validationResult && validationResult.valid && (
                          <div className="col-span-2">
                            <span className="text-xs px-2 py-1 rounded" style={{ 
                              backgroundColor: themeColors.success + '20',
                              color: themeColors.success
                            }}>
                              ✓ Valid for API Generation
                            </span>
                          </div>
                        )}
                      </div>

                      {/* Show parameters from selected object - only show IN parameters in this preview */}
                      {selectedDbObject?.parameters && selectedDbObject.parameters.length > 0 && (
                        <div className="mt-4">
                          <h5 className="text-xs font-medium mb-2" style={{ color: themeColors.text }}>
                            Object Parameters (IN/IN OUT only):
                          </h5>
                          <div className="space-y-2 max-h-48 overflow-y-auto pr-2">
                            {selectedDbObject.parameters
                              .filter(p => {
                                const mode = p.mode || p.IN_OUT || p.in_out;
                                return !mode || mode === 'IN' || mode === 'IN/OUT';
                              })
                              .map((param, index) => {
                                const mode = param.mode || param.IN_OUT || param.in_out || 'IN';
                                return (
                                  <div key={index} className="flex items-center justify-between text-xs p-2 rounded" 
                                    style={{ backgroundColor: themeColors.hover }}>
                                    <div>
                                      <span className="font-medium" style={{ color: themeColors.text }}>
                                        {param.name || param.ARGUMENT_NAME}
                                      </span>
                                      <span className="ml-2 text-xs px-2 py-0.5 rounded" style={{ 
                                        backgroundColor: mode === 'IN' ? themeColors.info + '20' : 
                                                      mode === 'OUT' ? themeColors.success + '20' : 
                                                      themeColors.warning + '20',
                                        color: mode === 'IN' ? themeColors.info : 
                                              mode === 'OUT' ? themeColors.success : 
                                              themeColors.warning
                                      }}>
                                        {mode}
                                      </span>
                                    </div>
                                    <div style={{ color: themeColors.textSecondary }}>
                                      {param.type || param.DATA_TYPE}
                                      {(param.defaultValue || param.DATA_DEFAULT) && (
                                        <span className="ml-2 text-xs">
                                          (Default: {param.defaultValue || param.DATA_DEFAULT})
                                        </span>
                                      )}
                                    </div>
                                  </div>
                                );
                              })}
                          </div>
                          
                          {/* Show OUT parameters separately in a note */}
                          {selectedDbObject.parameters.some(p => {
                            const mode = p.mode || p.IN_OUT || p.in_out;
                            return mode === 'OUT' || mode === 'IN/OUT';
                          }) && (
                            <p className="text-xs mt-2" style={{ color: themeColors.info }}>
                              Note: OUT/IN OUT parameters will appear in the Response Mapping tab.
                            </p>
                          )}
                        </div>
                      )}

                      {/* Show columns from selected object */}
                      {selectedDbObject?.columns && selectedDbObject.columns.length > 0 && (
                        <div className="mt-4">
                          <h5 className="text-xs font-medium mb-2" style={{ color: themeColors.text }}>
                            Object Columns (Auto-generated as parameters):
                          </h5>
                          <div className="space-y-2 max-h-48 overflow-y-auto pr-2">
                            {selectedDbObject.columns.slice(0, 8).map((col, index) => (
                              <div key={index} className="flex items-center justify-between text-xs p-2 rounded" 
                                style={{ backgroundColor: themeColors.hover }}>
                                <div>
                                  <span className="font-medium" style={{ color: themeColors.text }}>
                                    {col.name || col.COLUMN_NAME}
                                  </span>
                                  {((col.key === 'PK') || (col.CONSTRAINT_TYPE === 'P')) && (
                                    <span className="ml-2 text-xs px-2 py-0.5 rounded" style={{ 
                                      backgroundColor: themeColors.success + '20',
                                      color: themeColors.success
                                    }}>
                                      PK
                                    </span>
                                  )}
                                </div>
                                <div className="flex items-center gap-2">
                                  <span style={{ color: themeColors.textSecondary }}>
                                    {col.type || col.DATA_TYPE}
                                  </span>
                                  <span className="text-xs px-2 py-0.5 rounded" style={{ 
                                    backgroundColor: (col.nullable === 'Y' || col.nullable === true || col.NULLABLE === 'Y') ? themeColors.warning + '20' : themeColors.error + '20',
                                    color: (col.nullable === 'Y' || col.nullable === true || col.NULLABLE === 'Y') ? themeColors.warning : themeColors.error
                                  }}>
                                    {(col.nullable === 'Y' || col.nullable === true || col.NULLABLE === 'Y') ? 'NULL' : 'NOT NULL'}
                                  </span>
                                </div>
                              </div>
                            ))}
                            {selectedDbObject.columns.length > 8 && (
                              <div className="text-center pt-2">
                                <span className="text-xs" style={{ color: themeColors.textSecondary }}>
                                  + {selectedDbObject.columns.length - 8} more columns
                                </span>
                              </div>
                            )}
                          </div>
                          <p className="text-xs mt-2" style={{ color: themeColors.textSecondary }}>
                            These columns will be auto-generated as API parameters and response fields.
                          </p>
                        </div>
                      )}

                      {sourceObjectInfo.isSynonym && (
                        <div className="mt-4 p-3 rounded" style={{ backgroundColor: themeColors.warning + '20' }}>
                          <p className="text-xs flex items-center gap-2" style={{ color: themeColors.warning }}>
                            <Link className="h-4 w-4" />
                            This is a synonym pointing to {sourceObjectInfo.targetType}: {sourceObjectInfo.targetOwner}.{sourceObjectInfo.targetName}
                          </p>
                          <p className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>
                            Parameters and response fields are generated from the target object.
                          </p>
                        </div>
                      )}
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        {renderRequiredInput(
                          'schemaName',
                          'Schema Name',
                          schemaConfig.schemaName,
                          handleSchemaConfigChange,
                          'HR'
                        )}

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
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
                          >
                            <option value="TABLE">Table</option>
                            <option value="VIEW">View</option>
                            <option value="PROCEDURE">Procedure</option>
                            <option value="FUNCTION">Function</option>
                            <option value="PACKAGE">Package</option>
                            <option value="TRIGGER">Trigger</option>
                            <option value="SEQUENCE">Sequence</option>
                            <option value="TYPE">Type</option>
                            <option value="SYNONYM">Synonym</option>
                          </select>
                        </div>

                        {renderRequiredInput(
                          'objectName',
                          'Object Name',
                          schemaConfig.objectName,
                          handleSchemaConfigChange,
                          'EMPLOYEES'
                        )}

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
                                style={{ 
                                  accentColor: themeColors.info,
                                  backgroundColor: themeColors.card
                                }}
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
                                style={{ 
                                  accentColor: themeColors.info,
                                  backgroundColor: themeColors.card
                                }}
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
            onChange={(e) => {
              setSourceType(e.target.value);
              if (isEditingCustomQuery) {
                setIsEditingCustomQuery(false);
                setCustomQuery('');
              }
              // Clear validation error
              setValidationErrors(prev => ({ ...prev, customQuery: null }));
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
              setSourceType(e.target.value);
              if (!isEditingCustomQuery) {
                setIsEditingCustomQuery(true);
              }
              // Clear validation error
              setValidationErrors(prev => ({ ...prev, customQuery: null }));
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
                    placeholder="HR"
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
                              }}>Data Type</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>API Type</th>
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
                                    value={param.apiType}
                                    onChange={(e) => handleParameterChange(param.id, 'apiType', e.target.value)}
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
                                    {PARAMETER_LOCATIONS.map(loc => (
                                      <option key={loc.value} value={loc.value}>{loc.label}</option>
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
                                    placeholder="Example"
                                  />
                                </td>
                                <td className="px-3 py-2">
                                  <select
                                    value={param.paramMode || 'IN'}
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
                                    value={mapping.paramMode || ''}
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
                      <p className="text-xs mt-2" style={{ color: themeColors.info }}>
                        Note: Input parameters (IN/IN OUT) are shown in the Parameters tab.
                      </p>
                    )}
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
                    <div className="p-4 rounded-lg border mt-6" style={{ 
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
                    </div>
                  </div>
                )}

                {/* Request Tab */}
                {activeTab === 'request' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      Request Configuration
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
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
                          >
                            {BODY_TYPES.map(type => (
                              <option key={type.value} value={type.value}>{type.label}</option>
                            ))}
                          </select>
                          {requestBody.bodyType === 'none' && (
                            <p className="text-xs mt-2" style={{ color: themeColors.warning }}>
                              <AlertCircle className="h-3 w-3 inline mr-1" />
                              No request body will be sent. Only GET and DELETE methods typically use no body.
                            </p>
                          )}
                        </div>

                        {/* Only show these fields if body type is not 'none' */}
                        {requestBody.bodyType !== 'none' && (
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

                      {requestBody.bodyType !== 'none' && (
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
                    </div>

                    {/* Request Body Sample - Only show if body type is not 'none' */}
                    {requestBody.bodyType !== 'none' && (
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
                            <button
                              onClick={() => {
                                const sample = {};
                                const bodyParams = getInParameters().filter(p => p.parameterLocation === 'body');
                                bodyParams.forEach(p => {
                                  sample[p.key] = p.example || (p.apiType === 'integer' ? 123 : 'sample');
                                });
                                handleRequestBodyChange('sample', JSON.stringify(sample, null, 2));
                              }}
                              className="px-3 py-1 text-xs rounded border transition-colors hover-lift"
                              style={{ 
                                backgroundColor: themeColors.hover,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                            >
                              Generate from Parameters
                            </button>
                          </div>
                          <textarea
                            value={requestBody.sample || ''}
                            onChange={(e) => handleRequestBodyChange('sample', e.target.value)}
                            className="w-full h-48 px-4 py-3 text-xs font-mono resize-none focus:outline-none"
                            style={{ 
                              backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                              color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                            }}
                            placeholder={requestBody.bodyType === 'json' ? '{\n  "key": "value"\n}' : 
                                       requestBody.bodyType === 'xml' ? '<request>\n  <key>value</key>\n</request>' :
                                       'Enter request body...'}
                          />
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
                {activeTab === 'response' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      Response Configuration
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Success Schema
                          </label>
                          <div className="border rounded-lg" style={{ 
                            borderColor: themeColors.border,
                            backgroundColor: themeColors.card
                          }}>
                            <textarea
                              value={responseBody.successSchema}
                              onChange={(e) => handleResponseBodyChange('successSchema', e.target.value)}
                              className="w-full h-40 px-4 py-3 text-xs font-mono resize-none focus:outline-none"
                              style={{ 
                                backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                                color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                              }}
                            />
                          </div>
                        </div>

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
                      </div>

                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Error Schema
                          </label>
                          <div className="border rounded-lg" style={{ 
                            borderColor: themeColors.border,
                            backgroundColor: themeColors.card
                          }}>
                            <textarea
                              value={responseBody.errorSchema}
                              onChange={(e) => handleResponseBodyChange('errorSchema', e.target.value)}
                              className="w-full h-40 px-4 py-3 text-xs font-mono resize-none focus:outline-none"
                              style={{ 
                                backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                                color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                              }}
                            />
                          </div>
                        </div>

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
                      </div>
                    </div>

                    {/* Generate Sample from Mappings */}
                    <div className="mt-4">
                      <button
                        onClick={() => {
                          const sampleData = {};
                          getOutMappings().slice(0, 50).forEach(mapping => {
                            if (mapping.apiType === 'integer') {
                              sampleData[mapping.apiField] = 123;
                            } else if (mapping.apiType === 'string') {
                              if (mapping.format === 'date-time') {
                                sampleData[mapping.apiField] = '2024-01-01T00:00:00Z';
                              } else {
                                sampleData[mapping.apiField] = mapping.apiField === 'id' ? 1 : 'sample';
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
                          
                          handleResponseBodyChange('successSchema', successSchema);
                        }}
                        className="px-3 py-1.5 rounded-lg flex items-center gap-2 text-xs transition-colors hover-lift"
                        style={{ backgroundColor: themeColors.info, color: themeColors.white }}
                      >
                        <Sparkles className="h-4 w-4" />
                        Generate Success Sample from Mappings
                      </button>
                    </div>

                    {/* HTTP Status Codes */}
                    <div className="space-y-4">
                      <h4 className="font-semibold" style={{ color: themeColors.text }}>
                        HTTP Status Codes
                      </h4>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <div className="p-3 rounded-lg border text-center" style={{ 
                          borderColor: themeColors.success + '40',
                          backgroundColor: themeColors.success + '10'
                        }}>
                          <div className="text-lg font-bold" style={{ color: themeColors.success }}>200</div>
                          <div className="text-xs" style={{ color: themeColors.textSecondary }}>Success</div>
                        </div>
                        <div className="p-3 rounded-lg border text-center" style={{ 
                          borderColor: themeColors.error + '40',
                          backgroundColor: themeColors.error + '10'
                        }}>
                          <div className="text-lg font-bold" style={{ color: themeColors.error }}>400</div>
                          <div className="text-xs" style={{ color: themeColors.textSecondary }}>Bad Request</div>
                        </div>
                        <div className="p-3 rounded-lg border text-center" style={{ 
                          borderColor: themeColors.error + '40',
                          backgroundColor: themeColors.error + '10'
                        }}>
                          <div className="text-lg font-bold" style={{ color: themeColors.error }}>401</div>
                          <div className="text-xs" style={{ color: themeColors.textSecondary }}>Unauthorized</div>
                        </div>
                        <div className="p-3 rounded-lg border text-center" style={{ 
                          borderColor: themeColors.error + '40',
                          backgroundColor: themeColors.error + '10'
                        }}>
                          <div className="text-lg font-bold" style={{ color: themeColors.error }}>500</div>
                          <div className="text-xs" style={{ color: themeColors.textSecondary }}>Server Error</div>
                        </div>
                      </div>
                    </div>
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
                          <option value="plsql">PL/SQL Package</option>
                          <option value="openapi">OpenAPI Spec</option>
                          <option value="postman">Postman Collection</option>
                          <option value="sql_insert">SQL INSERT Statements</option> {/* NEW */}
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
                          previewMode === 'openapi' ? 'OpenAPI Specification' :
                          previewMode === 'postman' ? 'Postman Collection' : 
                          previewMode === 'sql_insert' ? 'SQL INSERT Statements' : 'Configuration'}
                        </span>
                        <span className="text-xs font-mono" style={{ color: themeColors.textSecondary }}>
                          {previewMode === 'plsql' ? '.sql' : 
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
              Endpoint: <span className="font-mono font-medium" style={{ color: themeColors.text }}>
                {apiDetails.httpMethod} {apiDetails.basePath}{apiDetails.endpointPath}
              </span>
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
                  backgroundColor: (loading || validating || selectedObject?.loading || (!isEditing && apiCodeExists)) ? themeColors.textSecondary : themeColors.success, 
                  color: themeColors.white 
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
          databaseType={databaseType}  // <-- ADD THIS LINE
        />
      )}

      {/* Add the preview modal */}
      <ApiPreviewModal
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