import React, { useState, useEffect, useRef, useCallback } from 'react';
import { 
  ChevronRight, ChevronDown, Search, Plus, Play, MoreVertical, Download, Share2,
  Eye, EyeOff, Copy, Trash2, Edit2, Settings, Globe, Lock, FileText, Code, GitBranch,
  History, Zap, Filter, Folder, FolderOpen, Star, ExternalLink, Upload, Users, Bell,
  HelpCircle, User, Moon, Sun, X, Menu, Check, AlertCircle, Clock, Activity, Database,
  Shield, Key, Hash, Bold, Loader, Italic, Link, Image, Table, Terminal, BookOpen,
  LayoutDashboard, ShieldCheck, DownloadCloud, UploadCloud, UserCheck, Home, Cloud,
  Save, Printer, Inbox, Archive, Trash, UserPlus, RefreshCw, ChevronLeft, ChevronUp,
  Minimize2, Maximize2, MoreHorizontal, Send, CheckCircle, XCircle, Info, Layers,
  Package, Box, FolderPlus, FilePlus, Wifi, Server, HardDrive, Network, Cpu, BarChart,
  PieChart, LineChart, Smartphone, Monitor, Bluetooth, Command, Circle, Dot, List,
  Type, FileCode, ChevronsLeft, ChevronsRight, GripVertical, Coffee, Eye as EyeIcon,
  FileArchive as FileBinary, Database as DatabaseIcon, ChevronsUpDown, Book, File,
  MessageSquare, Tag, Calendar, Hash as HashIcon, Link as LinkIcon, Eye as EyeOpenIcon,
  Clock as ClockIcon, Users as UsersIcon, Database as DatabaseIcon2, Code as CodeIcon2,
  Terminal as TerminalIcon, ExternalLink as ExternalLinkIcon, Copy as CopyIcon,
  Check as CheckIcon, X as XIcon, AlertCircle as AlertCircleIcon, Info as InfoIcon,
  HelpCircle as HelpCircleIcon, Star as StarIcon, Book as BookIcon, Zap as ZapIcon
} from 'lucide-react';

// Import DocumentationController functions
import {
  getAPICollections,
  getAPIEndpoints,
  getFolders,
  getEndpointDetails,
  getCodeExamples,
  searchDocumentation,
  publishDocumentation,
  getDocumentationEnvironments,
  getDocumentationNotifications,
  getChangelog,
  generateMockServer,
  clearDocumentationCache,
  handleDocumentationResponse,
  extractAPICollections,
  extractAPIEndpoints,
  extractEndpointDetails,
  extractCodeExamples,
  extractSearchResults,
  extractPublishResults,
  extractDocumentationEnvironments,
  extractNotifications,
  extractChangelog,
  extractMockServerResults,
  validatePublishDocumentation,
  validateGenerateMockServer,
  validateSearchDocumentation,
  formatDocumentationCollection,
  formatDocumentationEndpoint,
  formatEndpointDetails,
  formatSearchResult,
  getMethodColor,
  getTagColor,
  getCollectionColor,
  getRelevanceColor,
  getStatusCodeBadge,
  formatRateLimit,
  formatJsonExample,
  getTimeAgo,
  formatDateForDisplay,
  getInitials,
  generateDocumentationUrl,
  cacheDocumentationData,
  getCachedDocumentationData,
  clearCachedDocumentationData,
  getSupportedLanguages,
  getSupportedAPITypes,
  getSupportedHTTPMethods,
  getSupportedContentTypes,
  getSupportedVisibilityOptions,
  getSupportedEnvironmentTypes,
  getSupportedDocumentationFormats,
  getCollectionDetailsWithEndpoints,
  extractCollectionDetailsWithEndpoints,
} from "../controllers/DocumentationController.js";

// Also import apiCall for debug purposes
import { apiCall } from "@/helpers/APIHelper.js";

// ==================== PROTOCOL-SPECIFIC COMPONENTS ====================

// Protocol Badge Component
const ProtocolBadge = ({ protocol, size = 'sm', colors }) => {
  const getProtocolConfig = () => {
    switch (protocol?.toLowerCase()) {
      case 'soap':
        return {
          icon: Send,
          label: 'SOAP',
          bgColor: `${colors.info}20`,
          textColor: colors.info,
        };
      case 'graphql':
        return {
          icon: GitBranch,
          label: 'GraphQL',
          bgColor: `${colors.success}20`,
          textColor: colors.success,
        };
      case 'rest':
      default:
        return {
          icon: Code,
          label: 'REST',
          bgColor: `${colors.primary}20`,
          textColor: colors.primary,
        };
    }
  };
  
  const config = getProtocolConfig();
  const Icon = config.icon;
  const sizeClasses = size === 'sm' ? 'text-xs px-2 py-0.5 gap-1' : 'text-sm px-3 py-1 gap-1.5';
  const iconSize = size === 'sm' ? 12 : 14;
  
  return (
    <span 
      className={`inline-flex items-center rounded-full ${sizeClasses}`}
      style={{ 
        backgroundColor: config.bgColor,
        color: config.textColor,
      }}
    >
      <Icon size={iconSize} />
      <span>{config.label}</span>
    </span>
  );
};

// Protocol-Specific Request Body Renderer
const ProtocolRequestBody = ({ endpointDetails, colors, onCopy }) => {
  const { protocolType, requestBodyExample, requestBodyType, bodyParameters, method } = endpointDetails;
  
  // Methods that typically don't have a request body
  const methodsWithoutBody = ['GET', 'DELETE', 'HEAD', 'OPTIONS'];
  
  // For REST, check if this method should have a body
  if (protocolType !== 'soap' && protocolType !== 'graphql') {
    if (methodsWithoutBody.includes(method?.toUpperCase())) {
      return (
        <div className="text-center py-4" style={{ color: colors.textSecondary }}>
          <Info size={20} className="mx-auto mb-2 opacity-50" />
          <p className="text-sm">
            {method} requests typically do not have a request body.
            {bodyParameters && bodyParameters.length > 0 && 
              ` However, this endpoint has ${bodyParameters.length} body parameter(s).`
            }
          </p>
        </div>
      );
    }
  }
  
  // Helper function to generate a sample request body from body parameters
  const generateRequestBodyFromParameters = (params) => {
    if (!params || params.length === 0) return null;
    
    const body = {};
    params.forEach(param => {
      if (param.example) {
        try {
          body[param.name] = JSON.parse(param.example);
        } catch {
          body[param.name] = param.example;
        }
      } else if (param.defaultValue) {
        try {
          body[param.name] = JSON.parse(param.defaultValue);
        } catch {
          body[param.name] = param.defaultValue;
        }
      } else {
        // Generate default values based on type
        switch (param.type?.toLowerCase()) {
          case 'string':
            body[param.name] = "string";
            break;
          case 'number':
          case 'integer':
            body[param.name] = 0;
            break;
          case 'boolean':
            body[param.name] = true;
            break;
          case 'array':
            body[param.name] = [];
            break;
          case 'object':
            body[param.name] = {};
            break;
          default:
            body[param.name] = `{${param.name}}`;
        }
      }
    });
    return body;
  };
  
  // Get the request body content
  let requestBodyContent = null;
  let language = 'json';
  let contentType = 'application/json';
  
  // Check if we have a request body example
  if (requestBodyExample) {
    requestBodyContent = requestBodyExample;
  } 
  // If no example but we have body parameters, generate a sample
  else if (bodyParameters && bodyParameters.length > 0) {
    requestBodyContent = generateRequestBodyFromParameters(bodyParameters);
  }
  
  if (!requestBodyContent) {
    return (
      <div className="text-center py-4" style={{ color: colors.textSecondary }}>
        <Info size={20} className="mx-auto mb-2 opacity-50" />
        <p className="text-sm">No request body example available</p>
        {bodyParameters && bodyParameters.length === 0 && method && !methodsWithoutBody.includes(method?.toUpperCase()) && (
          <p className="text-xs mt-2 opacity-70">
            This {method} endpoint doesn't have any body parameters configured.
          </p>
        )}
      </div>
    );
  }
  
  let formattedBody = '';
  
  // Process based on protocol type
  if (protocolType === 'soap') {
    // SOAP: Extract XML from the requestBodyExample object
    if (typeof requestBodyContent === 'object' && requestBodyContent.xml) {
      formattedBody = requestBodyContent.xml;
    } else if (typeof requestBodyContent === 'string') {
      try {
        const parsed = JSON.parse(requestBodyContent);
        formattedBody = parsed.xml || requestBodyContent;
      } catch {
        formattedBody = requestBodyContent;
      }
    } else {
      formattedBody = JSON.stringify(requestBodyContent, null, 2);
    }
    language = 'xml';
    contentType = 'application/soap+xml';
  } 
  else if (protocolType === 'graphql') {
    // GraphQL: Extract query and variables
    if (typeof requestBodyContent === 'object') {
      if (requestBodyContent.query) {
        let graphQLString = `${requestBodyContent.operationType || 'query'} ${requestBodyContent.operationName || ''} ${requestBodyContent.query}`;
        if (requestBodyContent.variables && Object.keys(requestBodyContent.variables).length > 0) {
          graphQLString += `\n\nVariables:\n${JSON.stringify(requestBodyContent.variables, null, 2)}`;
        }
        formattedBody = graphQLString;
      } else {
        formattedBody = JSON.stringify(requestBodyContent, null, 2);
      }
    } else if (typeof requestBodyContent === 'string') {
      try {
        const parsed = JSON.parse(requestBodyContent);
        if (parsed.query) {
          let graphQLString = `${parsed.operationType || 'query'} ${parsed.operationName || ''} ${parsed.query}`;
          if (parsed.variables && Object.keys(parsed.variables).length > 0) {
            graphQLString += `\n\nVariables:\n${JSON.stringify(parsed.variables, null, 2)}`;
          }
          formattedBody = graphQLString;
        } else {
          formattedBody = requestBodyContent;
        }
      } catch {
        formattedBody = requestBodyContent;
      }
    }
    language = 'graphql';
    contentType = 'application/graphql+json';
  }
  else {
    // REST: Standard JSON or other format
    if (typeof requestBodyContent === 'object') {
      formattedBody = JSON.stringify(requestBodyContent, null, 2);
    } else if (typeof requestBodyContent === 'string') {
      // Try to parse as JSON first
      try {
        const parsed = JSON.parse(requestBodyContent);
        formattedBody = JSON.stringify(parsed, null, 2);
      } catch {
        // If not JSON, use as is
        formattedBody = requestBodyContent;
        language = requestBodyType === 'xml' ? 'xml' : 'text';
      }
    }
    contentType = requestBodyType === 'xml' ? 'application/xml' : 'application/json';
  }
  
  return (
    <div className="border rounded-lg overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
      <div className="px-4 py-2 border-b flex items-center justify-between" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.tableHeader
      }}>
        <div className="flex items-center gap-2">
          {protocolType === 'soap' ? <Send size={12} style={{ color: colors.info }} /> :
           protocolType === 'graphql' ? <GitBranch size={12} style={{ color: colors.success }} /> :
           <Code size={12} style={{ color: colors.primary }} />}
          <span className="text-xs font-mono" style={{ color: colors.textSecondary }}>
            Request Body ({contentType})
          </span>
        </div>
        <button 
          className="p-1 rounded hover:bg-opacity-50 transition-all hover-lift"
          style={{ backgroundColor: colors.hover }}
          onClick={() => onCopy(formattedBody)}
        >
          <Copy size={10} style={{ color: colors.textSecondary }} />
        </button>
      </div>
      <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
        <SyntaxHighlighter language={language} code={formattedBody} />
      </div>
    </div>
  );
};

// SOAP Response Renderer - ensures XML response
const SoapResponseRenderer = ({ responseExample, colors, onCopy }) => {
  let soapXml = '';
  let contentType = 'application/soap+xml';
  
  if (responseExample) {
    // Extract XML from various possible formats
    if (typeof responseExample === 'object' && responseExample.xml) {
      soapXml = responseExample.xml;
    } else if (typeof responseExample === 'string') {
      // If it's a JSON string containing XML, parse it
      if (responseExample.includes('"xml"')) {
        try {
          const parsed = JSON.parse(responseExample);
          soapXml = parsed.xml || responseExample;
        } catch {
          soapXml = responseExample;
        }
      } else {
        soapXml = responseExample;
      }
    } else if (typeof responseExample === 'object') {
      // For SOAP, response should be XML, so if we have an object, convert it to proper SOAP XML
      soapXml = `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <Response>
      ${Object.entries(responseExample).map(([key, value]) => `<${key}>${typeof value === 'object' ? JSON.stringify(value) : value}</${key}>`).join('\n      ')}
    </Response>
  </soap:Body>
</soap:Envelope>`;
    }
  }
  
  const formattedXml = soapXml.replace(/\\n/g, '\n').replace(/\\"/g, '"').replace(/\\t/g, '  ');
  
  return (
    <div className="border rounded-lg overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
      <div className="px-4 py-2 border-b flex items-center justify-between" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.tableHeader
      }}>
        <div className="flex items-center gap-2">
          <Send size={12} style={{ color: colors.info }} />
          <span className="text-xs font-mono" style={{ color: colors.textSecondary }}>SOAP Response (XML)</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xs" style={{ color: colors.textTertiary }}>{contentType}</span>
          <button 
            className="p-1 rounded hover:bg-opacity-50 transition-all hover-lift"
            style={{ backgroundColor: colors.hover }}
            onClick={() => onCopy(formattedXml)}>
            <Copy size={10} style={{ color: colors.textSecondary }} />
          </button>
        </div>
      </div>
      <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
        <pre className="text-xs font-mono whitespace-pre-wrap break-all" style={{ color: colors.text }}>
          {formattedXml || '<!-- No SOAP response data available -->'}
        </pre>
      </div>
    </div>
  );
};

// GraphQL Response Renderer
const GraphQLResponseRenderer = ({ responseExample, colors, onCopy }) => {
  let graphqlData = null;
  let formattedData = '';
  
  if (responseExample) {
    if (typeof responseExample === 'object') {
      graphqlData = responseExample;
      formattedData = JSON.stringify(responseExample, null, 2);
    } else if (typeof responseExample === 'string') {
      try {
        graphqlData = JSON.parse(responseExample);
        formattedData = JSON.stringify(graphqlData, null, 2);
      } catch (e) {
        formattedData = responseExample;
      }
    }
  }
  
  const hasData = graphqlData?.data;
  const hasErrors = graphqlData?.errors && graphqlData.errors.length > 0;
  
  return (
    <div className="border rounded-lg overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
      <div className="px-4 py-2 border-b flex items-center justify-between" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.tableHeader
      }}>
        <div className="flex items-center gap-2">
          <GitBranch size={12} style={{ color: colors.success }} />
          <span className="text-xs font-mono" style={{ color: colors.textSecondary }}>GraphQL Response</span>
        </div>
        <button 
          className="p-1 rounded hover:bg-opacity-50 transition-all hover-lift"
          style={{ backgroundColor: colors.hover }}
          onClick={() => onCopy(formattedData)}>
          <Copy size={10} style={{ color: colors.textSecondary }} />
        </button>
      </div>
      <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
        {hasErrors && (
          <div className="mb-3 p-2 rounded text-xs" style={{ backgroundColor: `${colors.error}20`, color: colors.error }}>
            <strong>Errors:</strong>
            {graphqlData.errors.map((err, idx) => (
              <div key={idx} className="mt-1">{err.message}</div>
            ))}
          </div>
        )}
        {hasData && (
          <div>
            <div className="text-xs mb-2" style={{ color: colors.textTertiary }}>Data:</div>
            <pre className="text-xs font-mono whitespace-pre-wrap break-all" style={{ color: colors.text }}>
              {JSON.stringify(graphqlData.data, null, 2)}
            </pre>
          </div>
        )}
        {!hasData && !hasErrors && formattedData && (
          <pre className="text-xs font-mono whitespace-pre-wrap break-all" style={{ color: colors.text }}>
            {formattedData}
          </pre>
        )}
        {!formattedData && (
          <div className="text-center py-4" style={{ color: colors.textSecondary }}>
            <Info size={24} className="mx-auto mb-2 opacity-50" />
            <p className="text-sm">No response data available</p>
          </div>
        )}
      </div>
    </div>
  );
};

// REST Response Renderer
const RestResponseRenderer = ({ responseExample, colors, onCopy, contentType = 'application/json' }) => {
  let formattedData = '';
  let language = 'json';
  
  if (responseExample) {
    if (typeof responseExample === 'object') {
      formattedData = JSON.stringify(responseExample, null, 2);
      language = 'json';
    } else if (typeof responseExample === 'string') {
      if (contentType.includes('xml')) {
        formattedData = responseExample.replace(/\\n/g, '\n').replace(/\\"/g, '"');
        language = 'xml';
      } else {
        try {
          const parsed = JSON.parse(responseExample);
          formattedData = JSON.stringify(parsed, null, 2);
          language = 'json';
        } catch (e) {
          formattedData = responseExample;
          language = 'text';
        }
      }
    }
  }
  
  return (
    <div className="border rounded-lg overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
      <div className="px-4 py-2 border-b flex items-center justify-between" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.tableHeader
      }}>
        <div className="flex items-center gap-2">
          <Code size={12} style={{ color: colors.primary }} />
          <span className="text-xs font-mono" style={{ color: colors.textSecondary }}>Response Body</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xs" style={{ color: colors.textTertiary }}>{contentType}</span>
          {formattedData && (
            <button 
              className="p-1 rounded hover:bg-opacity-50 transition-all hover-lift"
              style={{ backgroundColor: colors.hover }}
              onClick={() => onCopy(formattedData)}>
              <Copy size={10} style={{ color: colors.textSecondary }} />
            </button>
          )}
        </div>
      </div>
      <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
        {formattedData ? (
          <SyntaxHighlighter language={language} code={formattedData} />
        ) : (
          <div className="text-center py-4" style={{ color: colors.textSecondary }}>
            <Info size={24} className="mx-auto mb-2 opacity-50" />
            <p className="text-sm">No response data available</p>
          </div>
        )}
      </div>
    </div>
  );
};

// Protocol-Specific Response Examples Component
const ProtocolResponseExamples = ({ endpointDetails, colors, onCopy }) => {
  const { responseExamples, protocolType } = endpointDetails;
  
  if (!responseExamples || responseExamples.length === 0) {
    return (
      <div className="text-center py-8" style={{ color: colors.textSecondary }}>
        <Info size={32} className="mx-auto mb-2 opacity-50" />
        <p className="text-sm">No response examples available</p>
      </div>
    );
  }
  
  const getResponseRenderer = (example, idx) => {
    const commonProps = { colors, onCopy };
    // Extract the actual example content
    let exampleContent = example.example || example;
    
    // For string examples that might be JSON strings, parse them
    if (typeof exampleContent === 'string' && (exampleContent.startsWith('{') || exampleContent.startsWith('['))) {
      try {
        exampleContent = JSON.parse(exampleContent);
      } catch (e) {
        // Keep as string if parsing fails
      }
    }
    
    switch (protocolType?.toLowerCase()) {
      case 'soap':
        return <SoapResponseRenderer key={idx} responseExample={exampleContent} {...commonProps} />;
      case 'graphql':
        return <GraphQLResponseRenderer key={idx} responseExample={exampleContent} {...commonProps} />;
      default:
        return (
          <RestResponseRenderer 
            key={idx} 
            responseExample={exampleContent} 
            contentType={example.contentType || 'application/json'}
            {...commonProps}
          />
        );
    }
  };
  
  return (
    <div className="space-y-4">
      {responseExamples.map((example, idx) => (
        <div key={idx} className="border rounded-xl overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
          <div className="px-4 py-3 border-b flex items-center justify-between" style={{ 
            borderColor: colors.border,
            backgroundColor: colors.tableHeader
          }}>
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-2 px-3 py-1.5 rounded text-sm font-medium" style={{ 
                backgroundColor: example.statusCode >= 200 && example.statusCode < 300 ? `${colors.success}20` : `${colors.error}20`,
                color: example.statusCode >= 200 && example.statusCode < 300 ? colors.success : colors.error
              }}>
                {example.statusCode >= 200 && example.statusCode < 300 ? <CheckCircle size={12} /> : <XCircle size={12} />}
                {example.statusCode || (example.statusBadge?.text || 'Response')}
              </div>
              <span className="text-sm" style={{ color: colors.textSecondary }}>{example.description}</span>
            </div>
            <ProtocolBadge protocol={protocolType} size="sm" colors={colors} />
          </div>
          <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
            {getResponseRenderer(example, idx)}
          </div>
        </div>
      ))}
    </div>
  );
};

// SyntaxHighlighter Component
const SyntaxHighlighter = ({ language, code }) => {
  if (!code) return <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed">// No code available</pre>;
  
  const highlightCode = (code, lang) => {
    const lines = String(code).split('\n');
    
    return lines.map((line, lineIndex) => {
      let highlightedLine = line
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
      
      if (lang === 'json') {
        highlightedLine = highlightedLine.replace(/"([^"\\]*(\\.[^"\\]*)*)"(\s*:)/g, 
          '<span class="text-blue-400">"$1"</span>$3');
        highlightedLine = highlightedLine.replace(/:(\s*)"([^"\\]*(\\.[^"\\]*)*)"/g, 
          ':<span class="text-green-400">"$2"</span>');
        highlightedLine = highlightedLine.replace(/:(\s*)(\d+)([,\n])/g, 
          ':<span class="text-orange-400">$2</span>$3');
        highlightedLine = highlightedLine.replace(/\b(true|false|null)\b/g, 
          '<span class="text-purple-400">$1</span>');
      } else if (lang === 'xml') {
        highlightedLine = highlightedLine.replace(/(&lt;\/?[a-zA-Z:][^&gt;]*&gt;)/g, 
          '<span class="text-blue-400">$1</span>');
        highlightedLine = highlightedLine.replace(/\b([a-zA-Z:]+)=/g, 
          '<span class="text-orange-400">$1</span>=');
        highlightedLine = highlightedLine.replace(/=("([^"]*)")/g, 
          '=<span class="text-green-400">$1</span>');
      } else if (lang === 'graphql') {
        // Simple GraphQL highlighting
        highlightedLine = highlightedLine.replace(/(query|mutation|subscription|fragment|on|type|interface|enum|scalar|input|extend|directive|schema)\b/g, 
          '<span class="text-purple-400">$1</span>');
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)")/g, 
          '<span class="text-green-400">$1</span>');
        highlightedLine = highlightedLine.replace(/\b(true|false|null)\b/g, 
          '<span class="text-orange-400">$1</span>');
      } else if (lang === 'javascript' || lang === 'nodejs') {
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)"|'([^'\\]*(\\.[^'\\]*)*)')/g, 
          '<span class="text-green-400">$1</span>');
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(\/\/.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        const jsKeywords = ['function', 'const', 'let', 'var', 'if', 'else', 'for', 'while', 'return', 'class', 'import', 'export', 'from', 'default', 'async', 'await', 'try', 'catch', 'finally', 'throw', 'new', 'this', 'true', 'false', 'null', 'undefined'];
        jsKeywords.forEach(keyword => {
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, '<span class="text-purple-400">$1</span>');
        });
      } else if (lang === 'python') {
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)"|'([^'\\]*(\\.[^'\\]*)*)')/g, 
          '<span class="text-green-400">$1</span>');
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(#.*)/g, '<span class="text-gray-500">$1</span>');
        }
        const pythonKeywords = ['def', 'class', 'import', 'from', 'if', 'elif', 'else', 'for', 'while', 'try', 'except', 'finally', 'with', 'as', 'return', 'yield', 'async', 'await', 'lambda', 'in', 'is', 'not', 'and', 'or', 'True', 'False', 'None'];
        pythonKeywords.forEach(keyword => {
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, '<span class="text-purple-400">$1</span>');
        });
      } else if (lang === 'bash' || lang === 'curl') {
        highlightedLine = highlightedLine.replace(/(-[A-Za-z]|--[a-z-]+)/g, 
          '<span class="text-blue-400">$1</span>');
        highlightedLine = highlightedLine.replace(/("[^"]*")/g, 
          '<span class="text-green-400">$1</span>');
      }
      
      return <div key={lineIndex} dangerouslySetInnerHTML={{ __html: highlightedLine || '&nbsp;' }} />;
    });
  };

  return (
    <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed">
      {highlightCode(code, language)}
    </pre>
  );
};

// Loading Overlay Component
const LoadingOverlay = ({ isLoading, colors, loadingText }) => {
  if (!isLoading) return null;
  
  return (
    <div className="fixed inset-0 flex items-center justify-center z-50" style={{ backgroundColor: colors.bg }}>
      <div className="text-center">
        <div className="relative">
          <Loader className="animate-spin mx-auto mb-6" size={64} style={{ color: colors.primary }} />
          <div className="absolute inset-0 flex items-center justify-center">
            <BookOpen size={32} style={{ color: colors.primary, opacity: 0.3 }} />
          </div>
        </div>
        <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>
          Loading Documentation
        </h3>
        <p className="text-xs mb-2" style={{ color: colors.textSecondary }}>
          {loadingText || 'Please wait while we load your documentation data'}
        </p>
      </div>
    </div>
  );
};

// Helper function for alphabetical sorting
const sortAlphabetically = (items, key = 'name') => {
  if (!items || !Array.isArray(items)) return [];
  return [...items].sort((a, b) => {
    const nameA = (a[key] || '').toLowerCase();
    const nameB = (b[key] || '').toLowerCase();
    if (nameA < nameB) return -1;
    if (nameA > nameB) return 1;
    return 0;
  });
};

// Enhanced Parameter Table Component
const ParameterTable = ({ parameters, colors, onCopy }) => {
  if (!parameters || parameters.length === 0) return null;
  
  return (
    <div className="border rounded overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
      <table className="w-full">
        <thead style={{ backgroundColor: colors.tableHeader }}>
          <tr>
            <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Name</th>
            <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Type</th>
            <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Required</th>
            <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Description</th>
            <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Example</th>
          </tr>
        </thead>
        <tbody>
          {parameters.map((param, index) => (
            <tr key={param.id || index} className="border-b last:border-b-0" style={{ 
              borderColor: colors.borderLight,
              backgroundColor: colors.tableRow
            }}>
              <td className="px-4 py-3 font-medium" style={{ color: colors.text }}>
                <code>{param.name}</code>
                {param.deprecated && (
                  <span className="ml-2 text-xs px-1.5 py-0.5 rounded" style={{ 
                    backgroundColor: `${colors.warning}20`,
                    color: colors.warning
                  }}>deprecated</span>
                )}
               </td>
              <td className="px-4 py-3">
                <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                  backgroundColor: `${colors.info}20`,
                  color: colors.info
                }}>{param.type || 'string'}</span>
                {param.format && <div className="text-xs mt-1" style={{ color: colors.textTertiary }}>format: {param.format}</div>}
               </td>
              <td className="px-4 py-3">
                <span className="text-xs px-2 py-1 rounded" style={{ 
                  backgroundColor: param.required ? `${colors.error}20` : `${colors.success}20`,
                  color: param.required ? colors.error : colors.success
                }}>
                  {param.required ? 'Required' : 'Optional'}
                </span>
               </td>
              <td className="px-4 py-3 text-sm" style={{ color: colors.textSecondary }}>
                {param.description || ''}
               </td>
              <td className="px-4 py-3">
                {param.example ? (
                  <div className="flex items-center gap-1">
                    <code className="text-xs" style={{ color: colors.primary }}>{param.example}</code>
                    <button onClick={() => onCopy(param.example)} className="p-1 rounded hover:bg-opacity-50 hover-lift">
                      <Copy size={10} style={{ color: colors.textSecondary }} />
                    </button>
                  </div>
                ) : (
                  <span className="text-xs" style={{ color: colors.textTertiary }}>—</span>
                )}
               </td>
              </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

// Main component
const Documentation = ({ theme, isDark, customTheme, toggleTheme, authToken }) => {
  const [activeTab, setActiveTab] = useState('documentation');
  const [showCodePanel, setShowCodePanel] = useState(true);
  const [selectedLanguage, setSelectedLanguage] = useState('curl');
  const [showLanguageDropdown, setShowLanguageDropdown] = useState(false);
  const [toast, setToast] = useState(null);
  const [showPublishModal, setShowPublishModal] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [globalSearchQuery, setGlobalSearchQuery] = useState('');
  const [selectedCollection, setSelectedCollection] = useState(null);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [environments, setEnvironments] = useState([]);
  const [activeEnvironment, setActiveEnvironment] = useState('');
  const [publishUrl, setPublishUrl] = useState('');
  const [isGeneratingDocs, setIsGeneratingDocs] = useState(false);
  const [collections, setCollections] = useState([]);
  const [expandedCollections, setExpandedCollections] = useState([]);
  const [expandedFolders, setExpandedFolders] = useState([]);
  const [activeMainTab, setActiveMainTab] = useState('Collections');
  const [showImportModal, setShowImportModal] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [showWorkspaceSwitcher, setShowWorkspaceSwitcher] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showEnvironmentMenu, setShowEnvironmentMenu] = useState(false);
  
  const [folderEndpoints, setFolderEndpoints] = useState({});
  const [folderLoading, setFolderLoading] = useState({});

  const [isLoading, setIsLoading] = useState({
    collections: false,
    environments: false,
    notifications: false,
    endpointDetails: false,
    endpoints: false,
    folders: false,
    initialLoad: true
  });
  
  const [endpointDetails, setEndpointDetails] = useState(null);
  const [codeExamples, setCodeExamples] = useState({});
  const [changelog, setChangelog] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [userId, setUserId] = useState('');
  
  const isFirstLoad = useRef(true);
  const autoExpandTriggered = useRef(false);
  const selectedCollectionRef = useRef(null);
  const globalLoadingRef = useRef(false);
  const [globalLoading, setGlobalLoading] = useState(false);

  // Color scheme - Updated to match the reference design
  const colors = isDark ? {
    bg: 'rgb(1 14 35)',
    white: '#FFFFFF',
    sidebar: 'rgb(41 53 72 / 19%)',
    main: 'rgb(1 14 35)',
    header: 'rgb(20 26 38)',
    card: 'rgb(41 53 72 / 19%)',
    text: '#F1F5F9',
    textSecondary: 'rgb(148 163 184)',
    textTertiary: 'rgb(100 116 139)',
    border: 'rgb(51 65 85 / 19%)',
    borderLight: 'rgb(45 55 72)',
    borderDark: 'rgb(71 85 105)',
    hover: 'rgb(45 46 72 / 33%)',
    active: 'rgb(59 74 99)',
    selected: 'rgb(44 82 130)',
    primary: 'rgb(96 165 250)',
    primaryLight: 'rgb(147 197 253)',
    primaryDark: 'rgb(37 99 235)',
    method: {
      GET: 'rgb(52 211 153)',
      POST: 'rgb(96 165 250)',
      PUT: 'rgb(251 191 36)',
      DELETE: 'rgb(248 113 113)',
      PATCH: 'rgb(167 139 250)',
      HEAD: 'rgb(148 163 184)',
      OPTIONS: 'rgb(167 139 250)',
      LINK: 'rgb(34 211 238)',
      UNLINK: 'rgb(251 191 36)'
    },
    success: 'rgb(52 211 153)',
    warning: 'rgb(251 191 36)',
    error: 'rgb(248 113 113)',
    info: 'rgb(96 165 250)',
    tabActive: 'rgb(96 165 250)',
    tabInactive: 'rgb(148 163 184)',
    sidebarActive: 'rgb(96 165 250)',
    sidebarHover: 'rgb(45 46 72 / 33%)',
    inputBg: 'rgb(41 53 72 / 19%)',
    inputBorder: 'rgb(51 65 85 / 19%)',
    tableHeader: 'rgb(41 53 72 / 19%)',
    tableRow: 'rgb(41 53 72 / 19%)',
    tableRowHover: 'rgb(45 46 72 / 33%)',
    dropdownBg: 'rgb(41 53 72 / 19%)',
    dropdownBorder: 'rgb(51 65 85 / 19%)',
    modalBg: 'rgb(41 53 72 / 19%)',
    modalBorder: 'rgb(51 65 85 / 19%)',
    codeBg: 'rgb(41 53 72 / 19%)',
    connectionOnline: 'rgb(52 211 153)',
    connectionOffline: 'rgb(248 113 113)',
    connectionIdle: 'rgb(251 191 36)',
    accentPurple: 'rgb(167 139 250)',
    accentPink: 'rgb(244 114 182)',
    accentCyan: 'rgb(34 211 238)'
  } : {
    bg: '#f8fafc',
    white: '#f8fafc',
    sidebar: '#ffffff',
    main: '#f8fafc',
    header: '#ffffff',
    card: '#ffffff',
    text: '#1e293b',
    textSecondary: '#64748b',
    textTertiary: '#94a3b8',
    border: '#e2e8f0',
    borderLight: '#f1f5f9',
    borderDark: '#cbd5e1',
    hover: '#f1f5f9',
    active: '#e2e8f0',
    selected: '#dbeafe',
    primary: '#1e293b',
    primaryLight: '#60a5fa',
    primaryDark: '#2563eb',
    method: {
      GET: '#10b981',
      POST: '#3b82f6',
      PUT: '#f59e0b',
      DELETE: '#ef4444',
      PATCH: '#8b5cf6',
      HEAD: '#6b7280',
      OPTIONS: '#8b5cf6',
      LINK: '#06b6d4',
      UNLINK: '#f97316'
    },
    success: '#10b981',
    warning: '#f59e0b',
    error: '#ef4444',
    info: '#3b82f6',
    tabActive: '#3b82f6',
    tabInactive: '#64748b',
    sidebarActive: '#3b82f6',
    sidebarHover: '#f1f5f9',
    inputBg: '#ffffff',
    inputBorder: '#e2e8f0',
    tableHeader: '#f8fafc',
    tableRow: '#ffffff',
    tableRowHover: '#f8fafc',
    dropdownBg: '#ffffff',
    dropdownBorder: '#e2e8f0',
    modalBg: '#ffffff',
    modalBorder: '#e2e8f0',
    codeBg: '#f1f5f9'
  };

  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const copyToClipboard = useCallback((text) => {
    if (!text) {
      showToast('Nothing to copy', 'warning');
      return;
    }
    
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text)
        .then(() => showToast('Copied to clipboard!', 'success'))
        .catch(() => fallbackCopy(text));
    } else {
      fallbackCopy(text);
    }
    
    function fallbackCopy(text) {
      const textarea = document.createElement('textarea');
      textarea.value = text;
      textarea.style.position = 'fixed';
      textarea.style.top = '-9999px';
      textarea.style.left = '-9999px';
      document.body.appendChild(textarea);
      textarea.select();
      try {
        document.execCommand('copy');
        showToast('Copied to clipboard!', 'success');
      } catch (err) {
        showToast('Failed to copy', 'error');
      }
      document.body.removeChild(textarea);
    }
  }, []);

  const hasEndpoints = useCallback((item) => {
    if (item.requests && Array.isArray(item.requests) && item.requests.length > 0) return true;
    if (item.folders && Array.isArray(item.folders)) {
      return item.folders.some(folder => hasEndpoints(folder));
    }
    return false;
  }, []);

  const extractUserIdFromToken = (token) => {
    if (!token) return '';
    try {
      const parts = token.split('.');
      if (parts.length === 3) {
        const payload = JSON.parse(atob(parts[1]));
        return payload.sub || payload.userId || '';
      }
    } catch (e) {
      console.error('Error extracting userId from token:', e);
    }
    return '';
  };

  useEffect(() => {
    selectedCollectionRef.current = selectedCollection;
  }, [selectedCollection]);

  const withGlobalLoading = async (asyncFn) => {
    if (globalLoadingRef.current) return;
    try {
      globalLoadingRef.current = true;
      setGlobalLoading(true);
      await asyncFn();
    } finally {
      globalLoadingRef.current = false;
      setGlobalLoading(false);
    }
  };

  // Fetch API collections
  const fetchAPICollections = useCallback(async () => {
    if (!authToken) {
      showToast('Authentication required. Please login.', 'error');
      setIsLoading(prev => ({ ...prev, collections: false, initialLoad: false }));
      return;
    }

    setIsLoading(prev => ({ ...prev, collections: true }));

    try {
      const response = await getAPICollections(authToken);
      const handledResponse = handleDocumentationResponse(response);
      const collectionsData = extractAPICollections(handledResponse);

      const formattedCollections = collectionsData.map(collection => {
        const formatted = formatDocumentationCollection(collection);
        formatted.folders = [];
        return formatted;
      });
      
      const sortedCollections = sortAlphabetically(formattedCollections, 'name');
      setCollections(sortedCollections);
      
      await loadAllCollectionDetails(sortedCollections);
      showToast('Collections loaded successfully', 'success');
      
    } catch (error) {
      console.error('Error fetching API collections:', error);
      showToast(`Failed to load collections: ${error.message}`, 'error');
      setCollections([]);
    } finally {
      setIsLoading(prev => ({ ...prev, collections: false, initialLoad: false }));
    }
  }, [authToken]);

  // Load all collection details
  const loadAllCollectionDetails = useCallback(async (basicCollections) => {
    try {
      setIsLoading(prev => ({ ...prev, folders: true }));
      
      const collectionsWithDetails = await Promise.all(
        basicCollections.map(async (collection) => {
          try {
            const response = await getCollectionDetailsWithEndpoints(authToken, collection.id);
            const handledResponse = handleDocumentationResponse(response);
            const collectionDetails = extractCollectionDetailsWithEndpoints(handledResponse);
            
            if (collectionDetails) {
              let processedFolders = (collectionDetails.folders || []).map(folder => ({
                id: folder.id,
                name: folder.name,
                description: folder.description || '',
                collectionId: folder.collectionId || collection.id,
                requests: (folder.endpoints || []).map(endpoint => ({
                  ...endpoint,
                  protocolType: endpoint.protocolType || folder.protocolType || 'rest'
                })),
                requestCount: folder.endpointCount || (folder.endpoints ? folder.endpoints.length : 0),
                hasRequests: (folder.endpointCount || (folder.endpoints ? folder.endpoints.length : 0)) > 0,
                isLoading: false,
                error: null,
                protocolType: folder.protocolType || 'rest'
              }));
              
              processedFolders = sortAlphabetically(processedFolders, 'name');
              
              processedFolders = processedFolders.map(folder => ({
                ...folder,
                requests: sortAlphabetically(folder.requests || [], 'name')
              }));
              
              const folderEndpointsMap = {};
              processedFolders.forEach(folder => {
                if (folder.requests.length > 0) {
                  folderEndpointsMap[folder.id] = folder.requests;
                }
              });
              
              if (Object.keys(folderEndpointsMap).length > 0) {
                setFolderEndpoints(prev => ({ ...prev, ...folderEndpointsMap }));
              }
              
              return {
                ...collection,
                folders: processedFolders,
                totalFolders: collectionDetails.totalFolders || processedFolders.length,
                totalEndpoints: collectionDetails.totalEndpoints || 
                  processedFolders.reduce((sum, f) => sum + f.requests.length, 0)
              };
            }
            return collection;
          } catch (error) {
            console.error(`Error loading details for collection ${collection.id}:`, error);
            return collection;
          }
        })
      );
      
      const sortedCollections = sortAlphabetically(collectionsWithDetails, 'name');
      setCollections(sortedCollections);
      
      if (sortedCollections.length > 0) {
        const firstCollection = sortedCollections[0];
        setSelectedCollection(firstCollection);
        setExpandedCollections([firstCollection.id]);
        
        const firstFolderWithRequests = firstCollection.folders?.find(f => f.requests && f.requests.length > 0);
        if (firstFolderWithRequests) {
          setExpandedFolders([firstFolderWithRequests.id]);
          if (firstFolderWithRequests.requests.length > 0 && !selectedRequest) {
            const firstEndpoint = firstFolderWithRequests.requests[0];
            setSelectedRequest(firstEndpoint);
            await fetchEndpointDetails(firstCollection.id, firstEndpoint.id);
          }
        }
      }
    } catch (error) {
      console.error('Error loading collection details:', error);
    } finally {
      setIsLoading(prev => ({ ...prev, folders: false }));
    }
  }, [authToken, selectedRequest]);

  // Fetch endpoint details - with protocol-specific response extraction
  const fetchEndpointDetails = useCallback(async (collectionId, endpointId) => {
  if (!authToken || !collectionId || !endpointId) return;

  try {
    setIsLoading(prev => ({ ...prev, endpointDetails: true }));
    const response = await getEndpointDetails(authToken, collectionId, endpointId);
    const handledResponse = handleDocumentationResponse(response);
    
    let endpointData = null;
    if (handledResponse && handledResponse.data) {
      endpointData = handledResponse.data;
    } else if (handledResponse && handledResponse.endpoint) {
      endpointData = handledResponse.endpoint;
    } else if (handledResponse && typeof handledResponse === 'object') {
      endpointData = handledResponse;
    }
    
    if (endpointData) {
      const protocolType = endpointData.protocolType || endpointData.metadata?.protocolType || 'rest';
      
      // Process parameters
      const pathParams = [];
      const queryParams = [];
      const bodyParams = [];
      const allHeaders = [];
      
      if (endpointData.parameters && Array.isArray(endpointData.parameters)) {
        endpointData.parameters.forEach((param) => {
          const location = (param.parameterLocation || param.in || '').toLowerCase();
          const isBodyParam = param.bodyParameter === true || param.inBody === true || location === 'body';
          
          if (isBodyParam) {
            bodyParams.push(param);
          } else if (location === 'path') {
            pathParams.push(param);
          } else if (location === 'query') {
            queryParams.push(param);
          } else if (location === 'header') {
            allHeaders.push({
              key: param.key || param.name,
              value: param.example || param.defaultValue || '',
              description: param.description,
              required: param.required,
              type: param.type
            });
          }
        });
      }
      
      if (endpointData.headers && Array.isArray(endpointData.headers)) {
        endpointData.headers.forEach(header => {
          if (!allHeaders.some(h => h.key === header.key)) {
            allHeaders.push(header);
          }
        });
      }
      
      // ENHANCED: Try multiple sources for request body example
      let requestBodyExample = null;
      let requestBodyType = endpointData.requestBodyType || endpointData.bodyType || 'json';
      
      // Try to get request body from various possible fields
      const possibleBodyFields = [
        'requestBodyExample',
        'requestBody',
        'bodyExample',
        'body',
        'exampleRequestBody',
        'requestBodySchema',
        'bodySchema',
        'schema'
      ];
      
      for (const field of possibleBodyFields) {
        if (endpointData[field]) {
          requestBodyExample = endpointData[field];
          console.log(`Found request body from field: ${field}`, requestBodyExample);
          break;
        }
      }
      
      // If still no example but we have body parameters, create a sample
      if (!requestBodyExample && bodyParams.length > 0) {
        console.log('No request body example found, creating from body parameters:', bodyParams);
        const sampleBody = {};
        bodyParams.forEach(param => {
          if (param.example) {
            try {
              sampleBody[param.name] = JSON.parse(param.example);
            } catch {
              sampleBody[param.name] = param.example;
            }
          } else if (param.defaultValue) {
            sampleBody[param.name] = param.defaultValue;
          } else {
            // Generate based on type
            switch (param.type?.toLowerCase()) {
              case 'string':
                sampleBody[param.name] = "string";
                break;
              case 'number':
              case 'integer':
                sampleBody[param.name] = 0;
                break;
              case 'boolean':
                sampleBody[param.name] = true;
                break;
              case 'array':
                sampleBody[param.name] = [];
                break;
              case 'object':
                sampleBody[param.name] = {};
                break;
              default:
                sampleBody[param.name] = param.name;
            }
          }
        });
        requestBodyExample = sampleBody;
        console.log('Generated sample body from parameters:', requestBodyExample);
      }
      
      // If requestBodyExample is a string, try to parse it
      if (requestBodyExample && typeof requestBodyExample === 'string') {
        try {
          if (requestBodyExample.trim().startsWith('{') || requestBodyExample.trim().startsWith('[')) {
            requestBodyExample = JSON.parse(requestBodyExample);
          }
        } catch (e) {
          console.warn('Failed to parse request body JSON:', e);
        }
      }
      
      // For SOAP, ensure request body has proper XML structure
      if (protocolType === 'soap' && requestBodyExample && typeof requestBodyExample === 'object' && !requestBodyExample.xml) {
        requestBodyExample = { xml: JSON.stringify(requestBodyExample, null, 2) };
      }
      
      // Process response examples
      let responseExamples = [];
      
      if (endpointData.responseExamples && Array.isArray(endpointData.responseExamples)) {
        responseExamples = endpointData.responseExamples.map(example => {
          let processedExample = example.example;
          
          if (typeof processedExample === 'string' && (processedExample.startsWith('{') || processedExample.startsWith('['))) {
            try {
              processedExample = JSON.parse(processedExample);
            } catch (e) {}
          }
          
          return {
            statusCode: example.statusCode,
            description: example.description,
            example: processedExample,
            contentType: example.contentType || 'application/json',
            statusBadge: example.statusCode >= 200 && example.statusCode < 300 ? 'success' : 'error'
          };
        });
      } else if (endpointData.responseExample) {
        let processedExample = endpointData.responseExample;
        
        if (typeof processedExample === 'string' && (processedExample.startsWith('{') || processedExample.startsWith('['))) {
          try {
            processedExample = JSON.parse(processedExample);
          } catch (e) {}
        }
        
        responseExamples = [{
          statusCode: 200,
          description: 'Success response',
          example: processedExample,
          contentType: 'application/json',
          statusBadge: 'success'
        }];
      }
      
      const formattedDetails = {
        id: endpointData.endpointId || endpointData.id,
        name: endpointData.name || '',
        method: endpointData.method || 'GET',
        url: endpointData.url || endpointData.path || '',
        description: endpointData.description || '',
        category: endpointData.category || 'general',
        tags: endpointData.tags || [],
        lastModified: endpointData.lastModified,
        timeAgo: getTimeAgo(endpointData.lastModified),
        version: endpointData.version || '1.0.0',
        requiresAuthentication: endpointData.requiresAuthentication || false,
        deprecated: endpointData.deprecated || false,
        headers: allHeaders,
        pathParameters: pathParams,
        queryParameters: queryParams,
        bodyParameters: bodyParams,
        requestBodyExample: requestBodyExample,
        requestBodyType: requestBodyType,
        protocolType: protocolType,
        metadata: endpointData.metadata || {},
        responseExamples: responseExamples,
        changelog: endpointData.changelog || []
      };
      
      console.log('Final formatted details - request body:', formattedDetails.requestBodyExample);
      
      setEndpointDetails(formattedDetails);
      
      if (formattedDetails.changelog && formattedDetails.changelog.length > 0) {
        setChangelog(formattedDetails.changelog);
      }
    }
  } catch (error) {
    console.error('Error loading endpoint details:', error);
    showToast(`Failed to load endpoint details: ${error.message}`, 'error');
  } finally {
    setIsLoading(prev => ({ ...prev, endpointDetails: false }));
  }
}, [authToken]);

  // Fetch code examples
  const fetchCodeExamples = useCallback(async (endpointId, language) => {
    if (!authToken || !endpointId || !language) return;
    try {
      const response = await getCodeExamples(authToken, endpointId, language);
      const handledResponse = handleDocumentationResponse(response);
      const examples = extractCodeExamples(handledResponse);
      if (examples) {
        setCodeExamples(prev => ({ ...prev, [endpointId]: { ...prev[endpointId], [language]: examples } }));
      }
    } catch (error) {
      console.error('Error loading code examples:', error);
    }
  }, [authToken]);

  // Fetch environments
  const fetchEnvironments = useCallback(async () => {
    if (!authToken) return;
    try {
      setIsLoading(prev => ({ ...prev, environments: true }));
      const response = await getDocumentationEnvironments(authToken);
      const handledResponse = handleDocumentationResponse(response);
      const envs = extractDocumentationEnvironments(handledResponse);
      setEnvironments(envs);
      if (envs.length > 0) {
        const activeEnv = envs.find(e => e.isActive) || envs[0];
        setActiveEnvironment(activeEnv.id || '');
      }
    } catch (error) {
      console.error('Error loading environments:', error);
    } finally {
      setIsLoading(prev => ({ ...prev, environments: false }));
    }
  }, [authToken]);

  // Fetch notifications
  const fetchNotifications = useCallback(async () => {
    if (!authToken) return;
    try {
      setIsLoading(prev => ({ ...prev, notifications: true }));
      const response = await getDocumentationNotifications(authToken);
      const handledResponse = handleDocumentationResponse(response);
      const notifs = extractNotifications(handledResponse);
      setNotifications(notifs);
    } catch (error) {
      console.error('Error loading notifications:', error);
    } finally {
      setIsLoading(prev => ({ ...prev, notifications: false }));
    }
  }, [authToken]);

  // Fetch changelog
  const fetchChangelog = useCallback(async (collectionId) => {
    if (!authToken || !collectionId) return;
    try {
      const response = await getChangelog(authToken, collectionId);
      const handledResponse = handleDocumentationResponse(response);
      const changelogData = extractChangelog(handledResponse);
      setChangelog(changelogData);
    } catch (error) {
      console.error('Error loading changelog:', error);
    }
  }, [authToken]);

  // Toggle collection
  const toggleCollection = (collectionId) => {
    setExpandedCollections(prev =>
      prev.includes(collectionId) ? prev.filter(id => id !== collectionId) : [...prev, collectionId]
    );
  };

  // Toggle folder
  const toggleFolder = (folderId) => {
    if (isFirstLoad.current) return;
    setExpandedFolders(prev =>
      prev.includes(folderId) ? prev.filter(id => id !== folderId) : [...prev, folderId]
    );
  };

  // Handle select request
  const handleSelectRequest = async (request, collectionId, folderId) => {
    const collection = collections.find(c => c.id === collectionId);
    if (collection) setSelectedCollection(collection);
    setSelectedRequest(request);
    const endpointId = request.id || request.endpointId;
    await fetchEndpointDetails(collectionId, endpointId);
    if (endpointId) {
      await fetchCodeExamples(endpointId, selectedLanguage);
    }
  };

  // Handle environment change
  const handleEnvironmentChange = (envId) => {
    setActiveEnvironment(envId);
    setEnvironments(envs => envs.map(env => ({ ...env, isActive: env.id === envId })));
    showToast(`Switched to ${environments.find(e => e.id === envId)?.name} environment`, 'success');
    setShowEnvironmentMenu(false);
  };

  // Generate code example
  const generateCodeExample = () => {
    if (!selectedRequest || !endpointDetails) {
      return '// Select an endpoint to view code examples';
    }
    
    const currentExamples = codeExamples[selectedRequest.id]?.[selectedLanguage];
    if (currentExamples?.code) {
      return currentExamples.code;
    }
    
    const baseUrl = environments.find(e => e.id === activeEnvironment)?.baseUrl || '';
    const url = endpointDetails.url || selectedRequest.url || '';
    const method = endpointDetails.method || selectedRequest.method || 'GET';
    const protocolType = endpointDetails.protocolType || 'rest';
    
    // Build headers
    let headers = {};
    if (endpointDetails.headers) {
      endpointDetails.headers.forEach(header => {
        if (header.key && header.value) {
          headers[header.key] = header.value;
        }
      });
    }
    
    // For SOAP, ensure Content-Type is text/xml
    if (protocolType === 'soap' && !headers['Content-Type']) {
      headers['Content-Type'] = 'text/xml; charset=utf-8';
    }
    // For GraphQL, ensure Content-Type is application/json
    else if (protocolType === 'graphql' && !headers['Content-Type']) {
      headers['Content-Type'] = 'application/json';
    }
    // For REST with JSON body
    else if (!headers['Content-Type'] && endpointDetails.requestBodyType === 'json') {
      headers['Content-Type'] = 'application/json';
    }
    
    // Build body based on protocol
    let body = null;
    let rawBody = null;
    
    if (protocolType === 'soap') {
      // SOAP: Extract XML from requestBodyExample
      if (endpointDetails.requestBodyExample) {
        if (typeof endpointDetails.requestBodyExample === 'object' && endpointDetails.requestBodyExample.xml) {
          rawBody = endpointDetails.requestBodyExample.xml;
        } else if (typeof endpointDetails.requestBodyExample === 'string') {
          rawBody = endpointDetails.requestBodyExample;
        } else {
          rawBody = JSON.stringify(endpointDetails.requestBodyExample, null, 2);
        }
        body = rawBody;
      }
    } 
    else if (protocolType === 'graphql') {
      // GraphQL: Build query and variables
      if (endpointDetails.requestBodyExample) {
        let graphqlQuery = '';
        let graphqlVariables = {};
        
        if (typeof endpointDetails.requestBodyExample === 'object') {
          graphqlQuery = endpointDetails.requestBodyExample.query || '';
          graphqlVariables = endpointDetails.requestBodyExample.variables || {};
        } else if (typeof endpointDetails.requestBodyExample === 'string') {
          try {
            const parsed = JSON.parse(endpointDetails.requestBodyExample);
            graphqlQuery = parsed.query || '';
            graphqlVariables = parsed.variables || {};
          } catch (e) {
            graphqlQuery = endpointDetails.requestBodyExample;
          }
        }
        
        body = JSON.stringify({ query: graphqlQuery, variables: graphqlVariables }, null, 2);
        rawBody = body;
      }
    }
    else {
      // REST: Standard body
      if (endpointDetails.requestBodyExample) {
        if (typeof endpointDetails.requestBodyExample === 'object') {
          body = JSON.stringify(endpointDetails.requestBodyExample, null, 2);
          rawBody = body;
        } else {
          rawBody = endpointDetails.requestBodyExample;
          body = endpointDetails.requestBodyExample;
        }
      }
    }
    
    // Build URL with query parameters
    let fullUrl = url;
    if (endpointDetails.queryParameters && endpointDetails.queryParameters.length > 0 && protocolType === 'rest') {
      const queryParams = endpointDetails.queryParameters
        .filter(p => p.example)
        .map(p => `${encodeURIComponent(p.name)}=${encodeURIComponent(p.example)}`)
        .join('&');
      if (queryParams) {
        fullUrl += (fullUrl.includes('?') ? '&' : '?') + queryParams;
      }
    }
    
    // Replace baseUrl placeholder if needed
    if (fullUrl.includes('{{baseUrl}}') && baseUrl) {
      fullUrl = fullUrl.replace('{{baseUrl}}', baseUrl);
    }
    
    // Protocol-specific example generation
    if (protocolType === 'soap') {
      const soapAction = endpointDetails.headers?.find(h => h.key?.toLowerCase() === 'soapaction')?.value || '';
      
      switch (selectedLanguage) {
        case 'curl':
          return `curl -X POST "${fullUrl}" \\\n  -H "Content-Type: text/xml; charset=utf-8" \\\n${soapAction ? `  -H "SOAPAction: ${soapAction}" \\\n` : ''}  -d '${(body || '').replace(/'/g, "'\\''")}'`;
        case 'javascript':
          return `fetch('${fullUrl}', {\n  method: 'POST',\n  headers: {\n    'Content-Type': 'text/xml; charset=utf-8',\n${soapAction ? `    'SOAPAction': '${soapAction}',\n` : ''}  },\n  body: \`${(body || '').replace(/`/g, '\\`')}\`\n})`;
        case 'python':
          return `import requests\n\nheaders = {\n    'Content-Type': 'text/xml; charset=utf-8',\n${soapAction ? `    'SOAPAction': '${soapAction}',\n` : ''}}\n\nxml_body = """${body || ''}"""\n\nresponse = requests.post('${fullUrl}', headers=headers, data=xml_body)`;
        case 'nodejs':
          return `const https = require('https');\n\nconst xmlBody = \`${(body || '').replace(/`/g, '\\`')}\`;\n\nconst options = {\n  hostname: '${fullUrl.replace(/^https?:\/\//, '').split('/')[0]}',\n  path: '${fullUrl.replace(/^https?:\/\/[^\/]+/, '')}',\n  method: 'POST',\n  headers: {\n    'Content-Type': 'text/xml; charset=utf-8',\n${soapAction ? `    'SOAPAction': '${soapAction}',\n` : ''}    'Content-Length': xmlBody.length\n  }\n};\n\nconst req = https.request(options, (res) => {\n  console.log(res.statusCode);\n});\n\nreq.write(xmlBody);\nreq.end();`;
        default:
          return `// SOAP Request\nPOST ${fullUrl}\n${soapAction ? `SOAPAction: ${soapAction}\n` : ''}Content-Type: text/xml\n\n${body || ''}`;
      }
    }
    
    if (protocolType === 'graphql') {
      switch (selectedLanguage) {
        case 'curl':
          return `curl -X POST "${fullUrl}" \\\n  -H "Content-Type: application/json" \\\n  -d '${(body || '').replace(/'/g, "'\\''")}'`;
        case 'javascript':
          return `fetch('${fullUrl}', {\n  method: 'POST',\n  headers: {\n    'Content-Type': 'application/json'\n  },\n  body: ${body || 'null'}\n})`;
        case 'python':
          return `import requests\n\nresponse = requests.post('${fullUrl}', json=${body || 'null'})`;
        case 'nodejs':
          const dataLength = body ? body.length : 0;
          return `const https = require('https');\n\nconst data = ${body || 'null'};\n\nconst options = {\n  hostname: '${fullUrl.replace(/^https?:\/\//, '').split('/')[0]}',\n  path: '${fullUrl.replace(/^https?:\/\/[^\/]+/, '')}',\n  method: 'POST',\n  headers: {\n    'Content-Type': 'application/json',\n    'Content-Length': ${dataLength}\n  }\n};\n\nconst req = https.request(options, (res) => {\n  console.log(res.statusCode);\n});\n\nreq.write(JSON.stringify(data));\nreq.end();`;
        default:
          return `// GraphQL Request\nPOST ${fullUrl}\nContent-Type: application/json\n\n${body || ''}`;
      }
    }
    
    // REST API examples
    const headerString = Object.entries(headers)
      .map(([key, value]) => `  -H "${key}: ${value}"`)
      .join(' \\\n');
    
    const bodyString = body ? ` \\\n  -d '${body.replace(/'/g, "'\\''")}'` : '';
    
    switch (selectedLanguage) {
      case 'curl':
        return `curl -X ${method} "${fullUrl}" \\\n${headerString}${bodyString}`;
      
      case 'javascript':
        const fetchOptions = {
          method,
          headers,
          ...(body && { body: typeof body === 'string' ? body : JSON.stringify(body) })
        };
        return `fetch('${fullUrl}', ${JSON.stringify(fetchOptions, null, 2)})`;
      
      case 'python':
        let pythonCode = `import requests\n\n`;
        if (Object.keys(headers).length > 0) {
          pythonCode += `headers = ${JSON.stringify(headers, null, 2)}\n\n`;
        }
        if (body) {
          if (headers['Content-Type'] === 'application/json') {
            try {
              const jsonBody = JSON.parse(body);
              pythonCode += `json_data = ${JSON.stringify(jsonBody, null, 2)}\n\nresponse = requests.${method.toLowerCase()}('${fullUrl}', headers=headers, json=json_data)`;
            } catch {
              pythonCode += `data = ${body}\n\nresponse = requests.${method.toLowerCase()}('${fullUrl}', headers=headers, data=data)`;
            }
          } else {
            pythonCode += `data = ${JSON.stringify(body)}\n\nresponse = requests.${method.toLowerCase()}('${fullUrl}', headers=headers, data=data)`;
          }
        } else {
          pythonCode += `response = requests.${method.toLowerCase()}('${fullUrl}', headers=headers)`;
        }
        return pythonCode;
      
      case 'nodejs':
        let nodeCode = `const https = require('https');\n\n`;
        if (body) {
          nodeCode += `const data = ${body};\n\n`;
        }
        nodeCode += `const options = {\n  hostname: '${fullUrl.replace(/^https?:\/\//, '').split('/')[0]}',\n  path: '${fullUrl.replace(/^https?:\/\/[^\/]+/, '')}',\n  method: '${method}',\n  headers: ${JSON.stringify(headers, null, 2)}\n};\n\nconst req = https.request(options, (res) => {\n  console.log(res.statusCode);\n});\n\n${body ? 'req.write(JSON.stringify(data));\n' : ''}req.end();`;
        return nodeCode;
      
      default:
        return `// ${method} request to ${fullUrl}\n${Object.entries(headers).map(([k, v]) => `${k}: ${v}`).join('\n')}\n\n${body || ''}`;
    }
  };

  // Get method color
  const getMethodColor = (method) => {
    return colors.method[method] || colors.textSecondary;
  };

  // Filter collections based on search
  const filteredCollections = collections.filter(collection => {
    if (!searchQuery) return true;
    const query = searchQuery.toLowerCase();
    return (
      collection.name?.toLowerCase().includes(query) ||
      collection.folders?.some(folder => 
        folder.name?.toLowerCase().includes(query) ||
        folder.requests?.some(request => request.name?.toLowerCase().includes(query))
      )
    );
  });

  // Auto-expand first folder
  useEffect(() => {
    if (!isFirstLoad.current || autoExpandTriggered.current) return;
    if (!selectedCollection) return;
    if (!selectedCollection.folders || selectedCollection.folders.length === 0) return;
    
    autoExpandTriggered.current = true;
    
    const autoExpandAndSelect = async () => {
      const firstFolder = selectedCollection.folders[0];
      if (!firstFolder) return;
      
      setExpandedFolders([firstFolder.id]);
      
      if (firstFolder.requests && firstFolder.requests.length > 0 && !selectedRequest) {
        const firstEndpoint = firstFolder.requests[0];
        setSelectedRequest(firstEndpoint);
        await fetchEndpointDetails(selectedCollection.id, firstEndpoint.id);
      }
      
      isFirstLoad.current = false;
    };
    
    autoExpandAndSelect();
  }, [selectedCollection, selectedCollection?.folders, selectedRequest, fetchEndpointDetails]);

  // Initialize data
  useEffect(() => {
    const initializeData = async () => {
      setIsLoading(prev => ({ ...prev, initialLoad: true }));
      if (authToken) {
        const extractedUserId = extractUserIdFromToken(authToken);
        setUserId(extractedUserId);
        clearCachedDocumentationData(extractedUserId);
        try {
          await Promise.all([
            fetchAPICollections(),
            fetchEnvironments(),
            fetchNotifications()
          ]);
        } catch (error) {
          console.error('Error during initial data load:', error);
        } finally {
          setIsLoading(prev => ({ ...prev, initialLoad: false }));
        }
      } else {
        setIsLoading(prev => ({ ...prev, initialLoad: false }));
      }
    };
    initializeData();
  }, [authToken, fetchAPICollections, fetchEnvironments, fetchNotifications]);

  // Render documentation content with protocol-specific sections
  const renderDocumentationContent = () => {
    if (!selectedRequest || !endpointDetails) {
      return (
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center" style={{ color: colors.textSecondary }}>
            <BookOpen size={48} className="mx-auto mb-4 opacity-50" />
            <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>Select an API Endpoint</h3>
            <p>Choose an endpoint from the left sidebar to view its documentation</p>
          </div>
        </div>
      );
    }
    
    const protocolType = endpointDetails.protocolType || 'rest';
    // For SOAP and GraphQL, only show headers and request/response bodies
    const isProtocolSimplified = protocolType === 'soap' || protocolType === 'graphql';
    
    return (
      <div className="flex-1 overflow-auto p-8">
        <div className="max-w-6xl mx-auto">
          {/* Header */}
          <div className="mb-8">
            
            <h1 className="text-xl font-semibold mb-4 uppercase" style={{ color: colors.text }}>
              {selectedRequest.name}
            </h1>
            
            <div className="flex flex-wrap items-center gap-4 text-xs mb-4">
              {selectedCollection && (
                <div style={{ color: colors.textTertiary }}>
                  <Folder size={12} className="inline mr-1" />
                  {selectedCollection.name}
                </div>
              )}
              <div style={{ color: colors.textTertiary }}>
                <Clock size={12} className="inline mr-1" />
                Last updated: {endpointDetails.timeAgo || 'Unknown'}
              </div>
            </div>
            
            {endpointDetails.description && (
              <div className="p-4 rounded-lg" style={{ backgroundColor: colors.codeBg, border: `1px solid ${colors.border}` }}>
                <div className="flex items-center gap-3 mb-1">
                  <ProtocolBadge protocol={protocolType} size="md" colors={colors} />
                  {endpointDetails.deprecated && (
                    <span className="text-xs px-2 py-1 rounded-full flex items-center gap-1" style={{ 
                      backgroundColor: `${colors.error}20`,
                      color: colors.error
                    }}>
                      <AlertCircle size={12} /> Deprecated
                    </span>
                  )}
                  {endpointDetails.requiresAuthentication && (
                    <span className="text-xs px-2 py-1 rounded-full flex items-center gap-1" style={{ 
                      backgroundColor: `${colors.warning}20`,
                      color: colors.warning
                    }}>
                      <Lock size={12} /> Requires Auth
                    </span>
                  )}
                  <span className="text-xs px-2 py-1 rounded-full flex items-center gap-1" style={{ 
                      color: colors.textSecondary
                    }}>
                      <Info size={12} /> {endpointDetails.description}
                    </span>
                </div>
              </div>
            )}
          </div>

          {/* Request Details - Protocol Specific */}
          <div className="space-y-8">
            {/* Headers Section - Show for all protocols */}
            {endpointDetails.headers && endpointDetails.headers.length > 0 && (
              <div className="rounded-xl border hover-lift" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
                <div className="px-6 py-4 border-b" style={{ borderColor: colors.border }}>
                  <h3 className="text-lg font-medium" style={{ color: colors.text }}>
                    Headers
                    <span className="ml-2 text-xs px-2 py-1 rounded" style={{ backgroundColor: colors.primaryDark, color: 'white' }}>
                      {endpointDetails.headers.length}
                    </span>
                  </h3>
                </div>
                <div className="p-5">
                  <table className="w-full">
                    <thead>
                      <tr className="text-left text-xs" style={{ color: colors.textSecondary }}>
                        <th className="pb-2">Key</th>
                        <th className="pb-2">Value</th>
                        <th className="pb-2">Required</th>
                        <th className="pb-2">Description</th>
                      </tr>
                    </thead>
                    <tbody>
                      {endpointDetails.headers.map((header, idx) => (
                        <tr key={idx} className="border-t" style={{ borderColor: colors.borderLight }}>
                          <td className="py-2"><code className="text-xs">{header.key}</code></td>
                          <td className="py-2"><code className="text-xs break-all">{header.value || '(empty)'}</code></td>
                          <td className="py-2">{header.required ? 'Yes' : 'No'}</td>
                          <td className="py-2 text-xs" style={{ color: colors.textSecondary }}>{header.description}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* For REST only: Show Path Parameters, Query Parameters, and Body Parameters */}
            {!isProtocolSimplified && (
              <>
                {/* Path Parameters */}
                {endpointDetails.pathParameters && endpointDetails.pathParameters.length > 0 && (
                  <div className="rounded-xl border hover-lift" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
                    <div className="px-6 py-4 border-b" style={{ borderColor: colors.border }}>
                      <h3 className="text-lg font-medium" style={{ color: colors.text }}>
                        Path Parameters
                        <span className="ml-2 text-xs px-2 py-1 rounded" style={{ backgroundColor: colors.primaryDark, color: 'white' }}>
                          {endpointDetails.pathParameters.length}
                        </span>
                      </h3>
                    </div>
                    <div className="p-5">
                      <ParameterTable parameters={endpointDetails.pathParameters} colors={colors} onCopy={copyToClipboard} />
                    </div>
                  </div>
                )}

                {/* Query Parameters */}
                {endpointDetails.queryParameters && endpointDetails.queryParameters.length > 0 && (
                  <div className="rounded-xl border hover-lift" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
                    <div className="px-6 py-4 border-b" style={{ borderColor: colors.border }}>
                      <h3 className="text-lg font-medium" style={{ color: colors.text }}>
                        Query Parameters
                        <span className="ml-2 text-xs px-2 py-1 rounded" style={{ backgroundColor: colors.primaryDark, color: 'white' }}>
                          {endpointDetails.queryParameters.length}
                        </span>
                      </h3>
                    </div>
                    <div className="p-5">
                      <ParameterTable parameters={endpointDetails.queryParameters} colors={colors} onCopy={copyToClipboard} />
                    </div>
                  </div>
                )}

                {/* Body Parameters - Only for REST when there are body parameters */}
                {endpointDetails.bodyParameters && endpointDetails.bodyParameters.length > 0 && (
                  <div className="rounded-xl border hover-lift" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
                    <div className="px-6 py-4 border-b" style={{ borderColor: colors.border }}>
                      <h3 className="text-lg font-medium" style={{ color: colors.text }}>
                        Body Parameters
                        <span className="ml-2 text-xs px-2 py-1 rounded" style={{ backgroundColor: colors.primaryDark, color: 'white' }}>
                          {endpointDetails.bodyParameters.length}
                        </span>
                      </h3>
                    </div>
                    <div className="p-5">
                      <ParameterTable parameters={endpointDetails.bodyParameters} colors={colors} onCopy={copyToClipboard} />
                    </div>
                  </div>
                )}
              </>
            )}

            {/* Request Body Section - FIXED: Show if there's an example OR body parameters for POST/PUT/PATCH */}
            {(() => {
              const hasRequestBodyExample = endpointDetails.requestBodyExample;
              const hasBodyParameters = endpointDetails.bodyParameters && endpointDetails.bodyParameters.length > 0;
              const isBodyMethod = ['POST', 'PUT', 'PATCH'].includes(endpointDetails.method?.toUpperCase());
              const shouldShowRequestBody = hasRequestBodyExample || (hasBodyParameters && isBodyMethod);
              
              if (!shouldShowRequestBody) return null;
              
              return (
                <div className="rounded-xl border hover-lift" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
                  <div className="px-6 py-4 border-b" style={{ borderColor: colors.border }}>
                    <h3 className="text-lg font-medium" style={{ color: colors.text }}>
                      Request Body
                    </h3>
                  </div>
                  <div className="p-5">
                    <ProtocolRequestBody 
                      endpointDetails={endpointDetails} 
                      colors={colors} 
                      onCopy={copyToClipboard}
                    />
                  </div>
                </div>
              );
            })()}

            {/* Response Examples - Protocol Specific */}
            <div className="rounded-xl border hover-lift" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
              <div className="px-6 py-4 border-b" style={{ borderColor: colors.border }}>
                <h3 className="text-lg font-medium" style={{ color: colors.text }}>Response Examples</h3>
              </div>
              <div className="p-5">
                <ProtocolResponseExamples 
                  endpointDetails={endpointDetails} 
                  colors={colors} 
                  onCopy={copyToClipboard}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  // Render code panel
  const renderCodePanel = () => {
    const languages = [
      { value: 'curl', name: 'cURL', label: 'cURL' },
      { value: 'javascript', name: 'JavaScript', label: 'JavaScript' },
      { value: 'python', name: 'Python', label: 'Python' },
      { value: 'nodejs', name: 'Node.js', label: 'Node.js' }
    ];
    
    const currentLanguage = languages.find(lang => lang.value === selectedLanguage);
    
    return (
      <div className="w-80 border-l flex flex-col" style={{ 
        backgroundColor: colors.card,
        borderColor: colors.border
      }}>
        <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Code Examples</h3>
          <button onClick={() => setShowCodePanel(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}>
            <X size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        <div className="px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <div className="mb-2">
            <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Language</div>
            <div className="relative">
              <button
                onClick={() => setShowLanguageDropdown(!showLanguageDropdown)}
                className="w-full px-3 py-2 rounded text-sm font-medium flex items-center justify-between hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                <div className="flex items-center gap-2">
                  <Terminal size={14} />
                  <span>{currentLanguage?.label || currentLanguage?.name || 'Select Language'}</span>
                </div>
                <ChevronDown size={14} style={{ color: colors.textSecondary }} />
              </button>

              {showLanguageDropdown && (
                <div className="absolute left-0 right-0 top-full mt-1 py-2 rounded shadow-lg z-50 border"
                  style={{ 
                    backgroundColor: colors.bg,
                    borderColor: colors.border
                  }}>
                  {languages.map(lang => (
                    <button
                      key={lang.value}
                      onClick={() => {
                        setSelectedLanguage(lang.value);
                        setShowLanguageDropdown(false);
                        if (selectedRequest) {
                          fetchCodeExamples(selectedRequest.id, lang.value);
                        }
                      }}
                      className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors"
                      style={{ 
                        backgroundColor: selectedLanguage === lang.value ? colors.selected : 'transparent',
                        color: selectedLanguage === lang.value ? colors.primary : colors.text
                      }}
                    >
                      <Terminal size={14} />
                      {lang.label || lang.name}
                      {selectedLanguage === lang.value && <Check size={14} className="ml-auto" />}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="flex-1 overflow-auto">
          <div className="p-4 border-b flex items-center justify-between" style={{ borderColor: colors.border }}>
            <div className="flex items-center gap-2">
              <FileCode size={12} style={{ color: colors.textSecondary }} />
              <span className="text-sm font-medium" style={{ color: colors.text }}>Example</span>
            </div>
            <button 
              onClick={() => {
                const example = generateCodeExample();
                copyToClipboard(example);
              }}
              className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors flex items-center gap-1 hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}
            >
              <Copy size={10} />
              Copy
            </button>
          </div>
          
          <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
            {isLoading.endpointDetails ? (
              <div className="text-center py-8">
                <RefreshCw size={16} className="animate-spin mx-auto mb-2" style={{ color: colors.textSecondary }} />
                <p className="text-sm" style={{ color: colors.textSecondary }}>Loading code examples...</p>
              </div>
            ) : (
              <SyntaxHighlighter 
                language={selectedLanguage === 'curl' ? 'bash' : selectedLanguage === 'nodejs' ? 'javascript' : selectedLanguage}
                code={generateCodeExample()}
              />
            )}
          </div>
        </div>

        <div className="p-4 border-t" style={{ borderColor: colors.border }}>
          <button 
            className="w-full py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center justify-center gap-2 hover-lift"
            onClick={() => {
              const example = generateCodeExample();
              copyToClipboard(example);
            }}
            style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
            <Copy size={12} />
            Copy Code
          </button>
        </div>
      </div>
    );
  };

  // Render main content
  const renderMainContent = () => {
    switch (activeTab) {
      case 'documentation':
        return (
          <div className="flex-1 flex flex-col overflow-hidden">
            <div className="flex items-center border-b h-9" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
              <div className="flex items-center px-2">
                <button onClick={() => setActiveTab('documentation')} className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${activeTab === 'documentation' ? '' : 'hover:bg-opacity-50'}`} style={{ borderBottomColor: activeTab === 'documentation' ? colors.primary : 'transparent', color: activeTab === 'documentation' ? colors.primary : colors.textSecondary }}>
                  Documentation
                </button>
                <button onClick={() => { setActiveTab('changelog'); if (selectedCollection) fetchChangelog(selectedCollection.id); }} className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${activeTab === 'changelog' ? '' : 'hover:bg-opacity-50'}`} style={{ borderBottomColor: activeTab === 'changelog' ? colors.primary : 'transparent', color: activeTab === 'changelog' ? colors.primary : colors.textSecondary }}>
                  Changelog
                </button>
              </div>
            </div>
            {renderDocumentationContent()}
          </div>
        );
      case 'changelog':
        return (
          <div className="flex-1 flex flex-col overflow-hidden">
            <div className="flex items-center border-b h-9" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
              <div className="flex items-center px-2">
                <button onClick={() => setActiveTab('documentation')} className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${activeTab === 'documentation' ? '' : 'hover:bg-opacity-50'}`} style={{ borderBottomColor: activeTab === 'documentation' ? colors.primary : 'transparent', color: activeTab === 'documentation' ? colors.primary : colors.textSecondary }}>
                  Documentation
                </button>
                <button onClick={() => setActiveTab('changelog')} className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${activeTab === 'changelog' ? '' : 'hover:bg-opacity-50'}`} style={{ borderBottomColor: activeTab === 'changelog' ? colors.primary : 'transparent', color: activeTab === 'changelog' ? colors.primary : colors.textSecondary }}>
                  Changelog
                </button>
              </div>
            </div>
            <div className="flex-1 overflow-auto p-8">
              <div className="max-w-4xl mx-auto">
                <h2 className="text-2xl font-semibold mb-6" style={{ color: colors.text }}>API Changelog</h2>
                {changelog.length === 0 ? (
                  <div className="text-center py-12" style={{ color: colors.textSecondary }}>
                    <History size={48} className="mx-auto mb-4 opacity-50" />
                    <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>No Changelog Entries</h3>
                    <p>No changelog entries available for this collection.</p>
                  </div>
                ) : (
                  <div className="space-y-6">
                    {changelog.map((entry, index) => (
                      <div key={index} className="border rounded-xl p-6 hover-lift cursor-pointer" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
                        <div className="flex items-center justify-between mb-3">
                          <h3 className="text-lg font-semibold" style={{ color: colors.text }}>Version {entry.version}</h3>
                          <span className="text-sm" style={{ color: colors.textSecondary }}>{entry.date}</span>
                        </div>
                        <p className="text-sm mb-4" style={{ color: colors.textSecondary }}>{entry.description}</p>
                        {entry.changes && Array.isArray(entry.changes) && (
                          <ul className="space-y-2">
                            {entry.changes.map((change, idx) => (
                              <li key={idx} className="flex items-start gap-2 text-sm" style={{ color: colors.textSecondary }}>
                                <Check size={12} className="mt-0.5 flex-shrink-0" style={{ color: colors.success }} />
                                {change}
                              </li>
                            ))}
                          </ul>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="flex flex-col h-screen overflow-hidden" style={{ backgroundColor: colors.bg, color: colors.text, fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif', fontSize: '13px' }}>
      <style>{`
        @keyframes fadeInUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
        @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .animate-fade-in-up { animation: fadeInUp 0.2s ease-out; }
        .animate-spin { animation: spin 1s linear infinite; }
        .text-blue-400 { color: #60a5fa; }
        .text-green-400 { color: #34d399; }
        .text-purple-400 { color: #a78bfa; }
        .text-orange-400 { color: #fb923c; }
        .text-gray-500 { color: #9ca3af; }
        ::-webkit-scrollbar { width: 8px; height: 8px; }
        ::-webkit-scrollbar-track { background: ${colors.border}; border-radius: 4px; }
        ::-webkit-scrollbar-thumb { background: ${colors.textTertiary}; border-radius: 4px; }
        ::-webkit-scrollbar-thumb:hover { background: ${colors.textSecondary}; }
        .hover-lift:hover { transform: translateY(-2px); transition: transform 0.2s ease; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }
      `}</style>

      <LoadingOverlay isLoading={globalLoading || isLoading.initialLoad || isLoading.collections} colors={colors} />

      {/* Top Navigation */}
      <div className="flex items-center justify-between h-10 px-4 border-b" style={{ backgroundColor: colors.header, borderColor: colors.border }}>
        <div className="flex items-center gap-4">
          <span className="px-3 py-1.5 text-sm font-medium uppercase" style={{ color: colors.text }}>API Documentation</span>
        </div>
        <div className="flex items-center gap-2">
          {environments.length > 0 && (
            <div className="relative">
              <button className="flex items-center gap-2 px-3 py-1.5 rounded text-sm hover:bg-opacity-50 transition-colors hover-lift" onClick={() => setShowEnvironmentMenu(!showEnvironmentMenu)} style={{ backgroundColor: colors.hover }}>
                <Globe size={12} /><span>{environments.find(e => e.isActive)?.name || 'Select Env'}</span><ChevronDown size={12} />
              </button>
              {showEnvironmentMenu && (
                <div className="absolute top-full right-0 mt-1 py-2 rounded shadow-lg z-50 border min-w-48" style={{ backgroundColor: colors.bg, borderColor: colors.border }}>
                  {environments.map(env => (
                    <button key={env.id} onClick={() => handleEnvironmentChange(env.id)} className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors hover-lift" style={{ backgroundColor: env.isActive ? colors.selected : 'transparent', color: env.isActive ? colors.primary : colors.text }}>
                      <div className="w-2 h-2 rounded-full" style={{ backgroundColor: env.isActive ? colors.success : colors.textSecondary }} />
                      {env.name}
                      {env.isActive && <Check size={14} className="ml-auto" />}
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}
          <div className="w-px h-4" style={{ backgroundColor: colors.border }} />
          <button onClick={() => setShowCodePanel(!showCodePanel)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift" style={{ backgroundColor: showCodePanel ? colors.selected : colors.hover }}>
            <Code size={14} style={{ color: showCodePanel ? colors.primary : colors.textSecondary }} />
          </button>
          <button onClick={() => setShowPublishModal(true)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift" style={{ backgroundColor: colors.hover }}>
            <ExternalLink size={14} style={{ color: colors.textSecondary }} />
          </button>
          <button onClick={async () => { await withGlobalLoading(async () => { await fetchAPICollections(); }); showToast('Collections refreshed', 'success'); }} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift" style={{ backgroundColor: colors.hover }}>
            <RefreshCw size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left Sidebar */}
        <div className="w-80 border-r flex flex-col" style={{ borderColor: colors.border }}>
          <div className="p-4 border-b" style={{ borderColor: colors.border }}>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Collections</h3>
            </div>
            <div className="relative">
              <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
              <input type="text" placeholder="Search collections..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} className="w-full pl-8 pr-3 py-2 rounded text-sm focus:outline-none hover-lift" style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }} />
            </div>
          </div>

          <div className="flex-1 overflow-auto p-2">
            {isLoading.collections && !isLoading.initialLoad ? (
              <div className="text-center py-8"><RefreshCw size={16} className="animate-spin mx-auto mb-2" /><p className="text-sm">Loading collections...</p></div>
            ) : (
              (() => {
                const collectionsWithEndpoints = filteredCollections.filter(collection => hasEndpoints(collection));
                if (collectionsWithEndpoints.length === 0 && !isLoading.initialLoad) {
                  return <div className="text-center p-4"><Book size={20} className="mx-auto mb-2 opacity-50" /><p className="text-sm">No collections with endpoints found</p></div>;
                }
                return (
                  <>
                    {collectionsWithEndpoints.map(collection => {
                      const foldersWithEndpoints = (collection.folders || []).filter(folder => hasEndpoints(folder));
                      if (foldersWithEndpoints.length === 0) return null;
                      return (
                        <div key={collection.id} className="mb-3">
                          <div className="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-opacity-50 transition-colors mb-1.5 cursor-pointer hover-lift" onClick={() => toggleCollection(collection.id)} style={{ backgroundColor: colors.hover }}>
                            {expandedCollections.includes(collection.id) ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
                            <span className="text-sm font-medium flex-1 truncate">{collection.name}</span>
                          </div>
                          {expandedCollections.includes(collection.id) && foldersWithEndpoints.length > 0 && (
                            <>
                              {foldersWithEndpoints.map(folder => {
                                if (!folder.requests || folder.requests.length === 0) return null;
                                return (
                                  <div key={folder.id} className="ml-4 mb-2">
                                    <div className="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-opacity-50 transition-colors mb-1.5 cursor-pointer hover-lift" onClick={() => toggleFolder(folder.id)} style={{ backgroundColor: colors.hover }}>
                                      {expandedFolders.includes(folder.id) ? <ChevronDown size={11} /> : <ChevronRight size={11} />}
                                      <FolderOpen size={11} />
                                      <span className="text-sm flex-1 truncate">{folder.name}</span>
                                    </div>
                                    {expandedFolders.includes(folder.id) && (
                                      <div className="ml-6 mt-1 space-y-1">
                                        {folder.requests.map(request => (
                                          <button key={request.id} onClick={() => handleSelectRequest(request, collection.id, folder.id)} className="flex items-center gap-2 text-sm text-left w-full px-2 py-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift" style={{ color: selectedRequest?.id === request.id ? colors.primary : colors.text, backgroundColor: selectedRequest?.id === request.id ? colors.selected : 'transparent' }}>
                                            <div className="w-2 h-2 rounded-full" style={{ backgroundColor: colors.method[request.method] || colors.textSecondary }} />
                                            <span className="truncate flex-1">{request.name}</span>
                                            {request.protocolType && request.protocolType !== 'rest' && <ProtocolBadge protocol={request.protocolType} size="sm" colors={colors} />}
                                          </button>
                                        ))}
                                      </div>
                                    )}
                                  </div>
                                );
                              })}
                            </>
                          )}
                        </div>
                      );
                    })}
                  </>
                );
              })()
            )}
          </div>
        </div>

        {/* Main Content Area */}
        {renderMainContent()}

        {/* Right Code Panel */}
        {showCodePanel && renderCodePanel()}
      </div>

      {/* Toast */}
      {toast && (
        <div className="fixed bottom-4 right-4 px-4 py-2 rounded text-sm font-medium z-50 animate-fade-in-up" style={{ backgroundColor: toast.type === 'error' ? colors.error : toast.type === 'success' ? colors.success : toast.type === 'warning' ? colors.warning : colors.info, color: 'white' }}>
          {toast.message}
        </div>
      )}
    </div>
  );
};

export default Documentation;