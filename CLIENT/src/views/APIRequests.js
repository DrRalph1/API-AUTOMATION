import React, { useState, useEffect, useCallback, useRef } from 'react';
import { 
  ChevronDown,
  Search,
  Plus,
  Download,
  Copy,
  Settings,
  Code,
  Star,
  X,
  Check,
  AlertCircle,
  Clock,
  Activity,
  Folder,
  FolderOpen,
  Loader,
  FileCode,
  RefreshCw,
  Coffee,
  Box,
  ChevronLeft,
  ChevronRight,
  Package,
  Terminal,
  Server,
  Cpu,
  Monitor,
  HardDrive,
  ShieldCheck,
  Layers,
  Zap,
  BookOpen,
  History,
  Database as DatabaseIcon,
  ExternalLink as ExternalLinkIcon,
  DownloadCloud,
  UploadCloud,
  Filter,
  Calendar,
  BarChart,
  PieChart,
  TrendingUp,
  TrendingDown,
  Clock as ClockIcon,
  CheckCircle,
  XCircle,
  AlertTriangle,
  Info,
  Eye,
  EyeOff,
  Trash2,
  Archive,
  FileText,
  FileJson,
  FileSpreadsheet,
  Mail,
  MessageSquare,
  Bell,
  Globe,
  Map,
  Grid,
  List,
  Table,
  Play,
  Pause,
  StopCircle,
  SkipForward,
  SkipBack,
  RotateCcw,
  Maximize2,
  Minimize2,
  Home,
  BarChart2,
  Users,
  Shield,
  Settings as SettingsIcon,
  LogOut,
  Menu,
  Sidebar
} from 'lucide-react';

// Import APIRequestController functions
import {
  captureRequest,
  captureRequestFromExecution,
  updateRequestWithResponse,
  updateRequestWithError,
  batchUpdateResponses,
  getRequestById,
  getRequestByCorrelationId,
  getRequestsByApiId,
  searchRequests,
  getRequestStatistics,
  getSystemStatistics,
  getDailyBreakdown,
  deleteRequest,
  cleanupOldRequests,
  exportRequests,
  getRequestDashboardStats,
  getRecentRequests,
  getRequestHealth,
  extractRequestData,
  extractRequestsList,
  extractSearchResults,
  extractStatistics,
  extractDailyBreakdown,
  extractExportData,
  getRequestStatusColor,
  getStatusCodeColor,
  formatExecutionTime,
  formatRequestTimestamp,
  getRequestSummary,
  isRequestSuccessful,
  isRequestFailed,
  downloadExportedRequests,
  buildFilterFromQuery
} from "../controllers/APIRequestController.js";

// Enhanced SyntaxHighlighter Component (reusing from CodeBase)
const SyntaxHighlighter = ({ language, code }) => {
  if (!code) return <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed">// No code available</pre>;
  
  // Simple syntax highlighting using spans
  const highlightCode = (code, lang) => {
    const lines = String(code).split('\n');
    
    return lines.map((line, lineIndex) => {
      // First escape HTML entities to prevent injection
      let highlightedLine = line
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
      
      if (lang === 'java') {
        // Handle multi-line comment boundaries
        if (highlightedLine.includes('/*') || highlightedLine.includes('*/')) {
          // For lines with comment markers, highlight the whole line as comment
          // to avoid breaking the HTML structure
          return (
            <div key={lineIndex} className="text-gray-500">
              {highlightedLine}
            </div>
          );
        }
        
        // Highlight strings (double quotes) - do this first
        highlightedLine = highlightedLine.replace(/"([^"\\]*(\\.[^"\\]*)*)"/g, 
          '<span class="text-green-400">"$1"</span>');
        
        // Highlight characters (single quotes)
        highlightedLine = highlightedLine.replace(/'([^'\\]*(\\.[^'\\]*)*)'/g, 
          '<span class="text-green-400">\'$1\'</span>');
        
        // Highlight single-line comments (but not inside strings)
        // Only apply if the line doesn't contain a string that might have //
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(\/\/.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        
        // Highlight annotations
        highlightedLine = highlightedLine.replace(/(@\w+)/g, 
          '<span class="text-blue-400">$1</span>');
        
        // Highlight keywords - expanded list
        const keywords = [
          'public', 'private', 'protected', 'class', 'interface', 
          'extends', 'implements', 'static', 'final', 'void', 
          'return', 'new', 'if', 'else', 'for', 'while', 
          'switch', 'case', 'break', 'continue', 'throw', 
          'throws', 'try', 'catch', 'finally', 'import', 
          'package', 'abstract', 'assert', 'boolean', 'byte',
          'char', 'double', 'enum', 'float', 'int', 'long',
          'short', 'super', 'synchronized', 'this', 'transient',
          'volatile', 'instanceof', 'true', 'false', 'null'
        ];
        
        keywords.forEach(keyword => {
          // Use negative lookbehind to avoid matching inside existing spans
          // But since not all browsers support lookbehind, we'll use a simpler approach
          // Only replace if not inside an HTML tag
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, 
            '<span class="text-purple-400">$1</span>');
        });
        
        // Highlight numbers - FIXED: Don't apply to numbers that are part of class names like "400"
        if (!highlightedLine.includes('class="text-green-400"') && 
            !highlightedLine.includes('class="text-gray-500"')) {
          // Only highlight numbers that are standalone and not part of a class attribute
          highlightedLine = highlightedLine.replace(/\b(\d+[lLfFdD]?)\b(?![^<]*>|[^>]*<\/)/g, 
            '<span class="text-blue-400">$1</span>');
        }
        
        // Fix any malformed spans - CRITICAL FIX
        // This cleans up any incorrectly formatted span tags
        highlightedLine = highlightedLine.replace(/class="([^"]*)"([^>]*?)>/g, 'class="$1"$2>');
        
      } else if (lang === 'javascript' || lang === 'nodejs') {
        // JavaScript highlighting
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)"|'([^'\\]*(\\.[^'\\]*)*)')/g, 
          '<span class="text-green-400">$1</span>');
        
        // Comments
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(\/\/.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        
        // Keywords
        const jsKeywords = [
          'function', 'const', 'let', 'var', 'if', 'else', 
          'for', 'while', 'return', 'class', 'import', 
          'export', 'from', 'default', 'async', 'await', 
          'try', 'catch', 'finally', 'throw', 'new', 'this',
          'true', 'false', 'null', 'undefined', 'typeof',
          'instanceof', 'in', 'of', 'yield', 'delete'
        ];
        
        jsKeywords.forEach(keyword => {
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, 
            '<span class="text-purple-400">$1</span>');
        });
      } else if (lang === 'python') {
        // Python highlighting
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)"|'([^'\\]*(\\.[^'\\]*)*)')/g, 
          '<span class="text-green-400">$1</span>');
        
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(#.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        
        const pythonKeywords = [
          'def', 'class', 'import', 'from', 'if', 'elif', 
          'else', 'for', 'while', 'try', 'except', 'finally', 
          'with', 'as', 'return', 'yield', 'async', 'await', 
          'lambda', 'in', 'is', 'not', 'and', 'or', 'True', 
          'False', 'None', 'pass', 'break', 'continue', 'raise'
        ];
        
        pythonKeywords.forEach(keyword => {
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, 
            '<span class="text-purple-400">$1</span>');
        });
      }
      
      return (
        <div 
          key={lineIndex} 
          dangerouslySetInnerHTML={{ 
            __html: highlightedLine || '&nbsp;' 
          }} 
        />
      );
    });
  };

  return (
    <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed">
      {highlightCode(code, language)}
    </pre>
  );
};

// Request Details Modal Component
const RequestDetailsModal = ({ request, colors, isOpen, onClose, onRefresh }) => {
  if (!isOpen || !request) return null;

  const [activeTab, setActiveTab] = useState('request');

  // Helper function for status code color
  const getStatusCodeColorHelper = (code, colors) => {
    if (!code) return colors.textSecondary;
    if (code >= 200 && code < 300) return colors.success;
    if (code >= 300 && code < 400) return colors.info;
    if (code >= 400 && code < 500) return colors.warning;
    if (code >= 500) return colors.error;
    return colors.textSecondary;
  };

  // Helper function for request status color
  const getRequestStatusColorHelper = (status, colors) => {
    const statusMap = {
      'SUCCESS': colors.success,
      'FAILED': colors.error,
      'TIMEOUT': colors.warning,
      'PENDING': colors.info,
      'ERROR': colors.error,
      'CANCELLED': colors.textSecondary,
      'RETRY': colors.warning
    };
    return statusMap[status] || colors.textSecondary;
  };

  // Helper function to check if request was successful
  const isRequestSuccessfulHelper = (request) => {
    return request?.responseStatusCode >= 200 && request?.responseStatusCode < 300;
  };

  // Helper function to format request timestamp
  const formatRequestTimestampHelper = (timestamp) => {
    if (!timestamp) return 'N/A';
    try {
      return new Date(timestamp).toLocaleString();
    } catch (e) {
      return timestamp;
    }
  };

  // Helper function to format execution time
  const formatExecutionTimeHelper = (ms) => {
    if (!ms) return 'N/A';
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`;
    return `${(ms / 60000).toFixed(2)}m`;
  };

  // Helper function to copy to clipboard
  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50">
      {/* Blurred Backdrop */}
      <div 
        className="absolute inset-0 backdrop-blur-sm bg-black/30" 
        onClick={onClose} 
      />
      <div className="relative w-3/4 max-w-4xl max-h-[80vh] rounded-lg overflow-hidden" style={{ 
        backgroundColor: colors.bg,
        border: `1px solid ${colors.border}`
      }}>
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b" style={{ borderColor: colors.border }}>
          <div className="flex items-center gap-3">
            <h2 className="text-lg font-semibold" style={{ color: colors.text }}>
              Request Details
            </h2>
            <div className="flex items-center gap-2">
              <span className="text-xs px-2 py-1 rounded" style={{ 
                backgroundColor: getStatusCodeColorHelper(request.responseStatusCode, colors),
                color: 'white'
              }}>
                {request.responseStatusCode || 'Pending'}
              </span>
              <span className="text-xs px-2 py-1 rounded" style={{ 
                backgroundColor: `${getRequestStatusColorHelper(request.requestStatus, colors)}20`,
                color: getRequestStatusColorHelper(request.requestStatus, colors)
              }}>
                {request.requestStatus || 'PENDING'}
              </span>
            </div>
          </div>
          <button onClick={onClose} className="p-1 rounded hover:bg-opacity-50 transition-colors">
            <X size={18} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        {/* Modal Tabs */}
        <div className="flex items-center px-6 border-b" style={{ borderColor: colors.border }}>
          {['request', 'response', 'headers', 'timeline', 'summary'].map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-2 text-sm font-medium capitalize border-b-2 transition-colors`}
              style={{ 
                borderBottomColor: activeTab === tab ? colors.primary : 'transparent',
                color: activeTab === tab ? colors.primary : colors.textSecondary
              }}
            >
              {tab}
            </button>
          ))}
        </div>

        {/* Modal Content */}
        <div className="p-6 overflow-auto max-h-[calc(80vh-120px)]">
          {activeTab === 'request' && (
            <div className="space-y-4">
              {/* Request Info */}
              <div>
                <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Request Information</h3>
                <div className="grid grid-cols-2 gap-4">
                  <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Request Name</div>
                    <div className="text-sm font-medium" style={{ color: colors.text }}>{request.requestName || 'N/A'}</div>
                  </div>
                  <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Correlation ID</div>
                    <div className="text-xs font-mono break-all" style={{ color: colors.text }}>{request.correlationId || 'N/A'}</div>
                  </div>
                  <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>HTTP Method</div>
                    <div className="text-sm font-medium" style={{ color: colors.text }}>{request.httpMethod || 'N/A'}</div>
                  </div>
                  <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>URL</div>
                    <div className="text-xs font-mono break-all" style={{ color: colors.text }}>{request.url || 'N/A'}</div>
                  </div>
                  <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>API Name</div>
                    <div className="text-sm" style={{ color: colors.text }}>{request.apiName || 'N/A'}</div>
                  </div>
                  <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>API Code</div>
                    <div className="text-sm font-mono" style={{ color: colors.text }}>{request.apiCode || 'N/A'}</div>
                  </div>
                </div>
              </div>

              {/* Path Parameters */}
              {request.pathParameters && Object.keys(request.pathParameters).length > 0 && (
                <div>
                  <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Path Parameters</h3>
                  <div className="space-y-2">
                    {Object.entries(request.pathParameters).map(([key, value]) => (
                      <div key={key} className="flex items-start gap-2 p-2 rounded" style={{ backgroundColor: colors.hover }}>
                        <span className="text-xs font-medium min-w-[120px]" style={{ color: colors.textSecondary }}>{key}:</span>
                        <span className="text-xs font-mono" style={{ color: colors.text }}>{String(value)}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Query Parameters */}
              {request.queryParameters && Object.keys(request.queryParameters).length > 0 && (
                <div>
                  <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Query Parameters</h3>
                  <div className="space-y-2">
                    {Object.entries(request.queryParameters).map(([key, value]) => (
                      <div key={key} className="flex items-start gap-2 p-2 rounded" style={{ backgroundColor: colors.hover }}>
                        <span className="text-xs font-medium min-w-[120px]" style={{ color: colors.textSecondary }}>{key}:</span>
                        <span className="text-xs font-mono" style={{ color: colors.text }}>{String(value)}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Request Body */}
              {request.requestBody && (
                <div>
                  <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Request Body</h3>
                  <div className="p-3 rounded max-h-60 overflow-auto" style={{ backgroundColor: colors.codeBg }}>
                    <SyntaxHighlighter 
                      language="json"
                      code={typeof request.requestBody === 'object' ? JSON.stringify(request.requestBody, null, 2) : String(request.requestBody)}
                    />
                  </div>
                </div>
              )}
            </div>
          )}

          {activeTab === 'response' && (
            <div className="space-y-4">
              {/* Response Info */}
              <div className="grid grid-cols-3 gap-4">
                <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Status Code</div>
                  <div className="text-sm font-medium" style={{ color: colors.text }}>{request.responseStatusCode || 'N/A'}</div>
                </div>
                <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Status Message</div>
                  <div className="text-sm" style={{ color: colors.text }}>{request.responseStatusMessage || 'N/A'}</div>
                </div>
                <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Response Size</div>
                  <div className="text-sm" style={{ color: colors.text }}>
                    {request.responseSizeBytes ? `${(request.responseSizeBytes / 1024).toFixed(2)} KB` : 'N/A'}
                  </div>
                </div>
              </div>

              {/* Response Body */}
              {request.responseBody && (
                <div>
                  <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Response Body</h3>
                  <div className="p-3 rounded max-h-96 overflow-auto" style={{ backgroundColor: colors.codeBg }}>
                    <SyntaxHighlighter 
                      language="json"
                      code={typeof request.responseBody === 'object' ? JSON.stringify(request.responseBody, null, 2) : String(request.responseBody)}
                    />
                  </div>
                </div>
              )}

              {/* Error Message */}
              {request.errorMessage && (
                <div className="p-3 rounded" style={{ backgroundColor: `${colors.error}20` }}>
                  <div className="flex items-center gap-2 mb-2">
                    <AlertCircle size={14} style={{ color: colors.error }} />
                    <span className="text-sm font-medium" style={{ color: colors.error }}>Error</span>
                  </div>
                  <p className="text-sm" style={{ color: colors.text }}>{request.errorMessage}</p>
                </div>
              )}
            </div>
          )}

          {activeTab === 'headers' && (
            <div className="space-y-4">
              {/* Request Headers */}
              {request.headers && Object.keys(request.headers).length > 0 && (
                <div>
                  <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Request Headers</h3>
                  <div className="space-y-2 max-h-60 overflow-auto">
                    {Object.entries(request.headers).map(([key, value]) => (
                      <div key={key} className="flex items-start gap-2 p-2 rounded" style={{ backgroundColor: colors.hover }}>
                        <span className="text-xs font-medium min-w-[150px]" style={{ color: colors.textSecondary }}>{key}:</span>
                        <span className="text-xs font-mono break-all" style={{ color: colors.text }}>{String(value)}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Response Headers */}
              {request.responseHeaders && Object.keys(request.responseHeaders).length > 0 && (
                <div>
                  <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Response Headers</h3>
                  <div className="space-y-2 max-h-60 overflow-auto">
                    {Object.entries(request.responseHeaders).map(([key, value]) => (
                      <div key={key} className="flex items-start gap-2 p-2 rounded" style={{ backgroundColor: colors.hover }}>
                        <span className="text-xs font-medium min-w-[150px]" style={{ color: colors.textSecondary }}>{key}:</span>
                        <span className="text-xs font-mono break-all" style={{ color: colors.text }}>{String(value)}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Auth Type */}
              {request.authType && (
                <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Authentication Type</div>
                  <div className="text-sm font-medium" style={{ color: colors.text }}>{request.authType}</div>
                </div>
              )}

              {/* Curl Command */}
              {request.curlCommand && (
                <div>
                  <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>CURL Command</h3>
                  <div className="p-3 rounded max-h-40 overflow-auto" style={{ backgroundColor: colors.codeBg }}>
                    <pre className="text-xs font-mono whitespace-pre-wrap break-all" style={{ color: colors.text }}>
                      {request.curlCommand}
                    </pre>
                  </div>
                </div>
              )}
            </div>
          )}

          {activeTab === 'timeline' && (
            <div className="space-y-4">
              {/* Timeline */}
              <div className="relative pl-8 space-y-6">
                {/* Request Created */}
                <div className="relative">
                  <div className="absolute left-[-24px] top-0 w-3 h-3 rounded-full" style={{ backgroundColor: colors.info }} />
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Request Created</div>
                  <div className="text-sm" style={{ color: colors.text }}>{formatRequestTimestampHelper(request.createdAt)}</div>
                </div>

                {/* Request Sent */}
                {request.requestTimestamp && (
                  <div className="relative">
                    <div className="absolute left-[-24px] top-0 w-3 h-3 rounded-full" style={{ backgroundColor: colors.primary }} />
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Request Sent</div>
                    <div className="text-sm" style={{ color: colors.text }}>{formatRequestTimestampHelper(request.requestTimestamp)}</div>
                  </div>
                )}

                {/* Response Received */}
                {request.responseTimestamp && (
                  <div className="relative">
                    <div className="absolute left-[-24px] top-0 w-3 h-3 rounded-full" style={{ 
                      backgroundColor: isRequestSuccessfulHelper(request) ? colors.success : colors.error 
                    }} />
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Response Received</div>
                    <div className="text-sm" style={{ color: colors.text }}>{formatRequestTimestampHelper(request.responseTimestamp)}</div>
                  </div>
                )}
              </div>

              {/* Duration */}
              {request.executionDurationMs && (
                <div className="p-4 rounded" style={{ backgroundColor: colors.hover }}>
                  <div className="flex items-center justify-between">
                    <span className="text-sm" style={{ color: colors.textSecondary }}>Total Duration:</span>
                    <span className="text-xl font-semibold" style={{ color: colors.text }}>
                      {formatExecutionTimeHelper(request.executionDurationMs)}
                    </span>
                  </div>
                </div>
              )}

              {/* Retry Count */}
              {request.retryCount > 0 && (
                <div className="p-3 rounded" style={{ backgroundColor: `${colors.warning}20` }}>
                  <div className="flex items-center gap-2">
                    <RotateCcw size={14} style={{ color: colors.warning }} />
                    <span className="text-sm" style={{ color: colors.text }}>
                      Retried {request.retryCount} {request.retryCount === 1 ? 'time' : 'times'}
                    </span>
                  </div>
                </div>
              )}

              {/* Client Info */}
              <div className="grid grid-cols-2 gap-4">
                {request.clientIpAddress && (
                  <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Client IP</div>
                    <div className="text-sm font-mono" style={{ color: colors.text }}>{request.clientIpAddress}</div>
                  </div>
                )}
                {request.userAgent && (
                  <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>User Agent</div>
                    <div className="text-xs break-all" style={{ color: colors.text }}>{request.userAgent}</div>
                  </div>
                )}
              </div>
            </div>
          )}

          {activeTab === 'summary' && request.summary && (
            <div className="space-y-4">
              <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>API Summary Statistics</h3>
              
              <div className="grid grid-cols-2 gap-4">
                <div className="p-4 rounded" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Total Requests (API)</div>
                  <div className="text-2xl font-semibold" style={{ color: colors.text }}>{request.summary.totalRequestsForApi || 0}</div>
                </div>
                <div className="p-4 rounded" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Successful</div>
                  <div className="text-2xl font-semibold" style={{ color: colors.success }}>{request.summary.successfulRequests || 0}</div>
                </div>
                <div className="p-4 rounded" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Failed</div>
                  <div className="text-2xl font-semibold" style={{ color: colors.error }}>{request.summary.failedRequests || 0}</div>
                </div>
                <div className="p-4 rounded" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Today's Count</div>
                  <div className="text-2xl font-semibold" style={{ color: colors.text }}>{request.summary.requestCountToday || 0}</div>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-4">
                <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Avg Response Time</div>
                  <div className="text-lg font-semibold" style={{ color: colors.text }}>
                    {formatExecutionTimeHelper(request.summary.averageResponseTime || 0)}
                  </div>
                </div>
                <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Min Response Time</div>
                  <div className="text-lg font-semibold" style={{ color: colors.text }}>
                    {formatExecutionTimeHelper(request.summary.minResponseTime || 0)}
                  </div>
                </div>
                <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Max Response Time</div>
                  <div className="text-lg font-semibold" style={{ color: colors.text }}>
                    {formatExecutionTimeHelper(request.summary.maxResponseTime || 0)}
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div className="flex items-center justify-end gap-2 px-6 py-3 border-t" style={{ borderColor: colors.border }}>
          <button
            onClick={() => {
              copyToClipboard(JSON.stringify(request, null, 2));
            }}
            className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2"
            style={{ backgroundColor: colors.hover, color: colors.text }}
          >
            <Copy size={12} />
            Copy JSON
          </button>
          <button
            onClick={onClose}
            className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.primaryDark, color: 'white' }}
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

// Filter Modal Component
const FilterModal = ({ filters, colors, isOpen, onClose, onApply }) => {
  const [localFilters, setLocalFilters] = useState(filters || {});

  // Update local filters when prop changes
  useEffect(() => {
    setLocalFilters(filters || {});
  }, [filters]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50">
      <div className="absolute inset-0 bg-black bg-opacity-50" onClick={onClose} />
      <div 
        className="absolute inset-0 backdrop-blur-sm bg-black/30" 
        onClick={onClose} 
      />
      <div className="relative w-96 rounded-lg overflow-hidden" style={{ 
        backgroundColor: colors.bg,
        border: `1px solid ${colors.border}`
      }}>
        {/* Modal Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Filter Requests</h3>
          <button onClick={onClose} className="p-1 rounded hover:bg-opacity-50 transition-colors">
            <X size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        {/* Modal Content */}
        <div className="p-4 space-y-4 max-h-96 overflow-auto">
          {/* Status Filter */}
          <div>
            <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>Status</label>
            <select
              value={localFilters.requestStatus || ''}
              onChange={(e) => setLocalFilters({ ...localFilters, requestStatus: e.target.value || undefined })}
              className="w-full px-3 py-2 rounded text-sm"
              style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
            >
              <option value="">All Statuses</option>
              <option value="SUCCESS">Success</option>
              <option value="FAILED">Failed</option>
              <option value="TIMEOUT">Timeout</option>
              <option value="PENDING">Pending</option>
            </select>
          </div>

          {/* HTTP Method Filter */}
          <div>
            <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>HTTP Method</label>
            <select
              value={localFilters.httpMethod || ''}
              onChange={(e) => setLocalFilters({ ...localFilters, httpMethod: e.target.value || undefined })}
              className="w-full px-3 py-2 rounded text-sm"
              style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
            >
              <option value="">All Methods</option>
              <option value="GET">GET</option>
              <option value="POST">POST</option>
              <option value="PUT">PUT</option>
              <option value="DELETE">DELETE</option>
              <option value="PATCH">PATCH</option>
              <option value="HEAD">HEAD</option>
              <option value="OPTIONS">OPTIONS</option>
            </select>
          </div>

          {/* Status Code Filter */}
          <div>
            <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>Status Code</label>
            <input
              type="number"
              value={localFilters.responseStatusCode || ''}
              onChange={(e) => setLocalFilters({ ...localFilters, responseStatusCode: e.target.value ? parseInt(e.target.value) : undefined })}
              placeholder="e.g., 200, 404, 500"
              className="w-full px-3 py-2 rounded text-sm"
              style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
            />
          </div>

          {/* API ID Filter */}
          <div>
            <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>API ID</label>
            <input
              type="text"
              value={localFilters.apiId || ''}
              onChange={(e) => setLocalFilters({ ...localFilters, apiId: e.target.value || undefined })}
              placeholder="Filter by API ID"
              className="w-full px-3 py-2 rounded text-sm"
              style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
            />
          </div>

          {/* Correlation ID Filter */}
          <div>
            <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>Correlation ID</label>
            <input
              type="text"
              value={localFilters.correlationId || ''}
              onChange={(e) => setLocalFilters({ ...localFilters, correlationId: e.target.value || undefined })}
              placeholder="Filter by Correlation ID"
              className="w-full px-3 py-2 rounded text-sm"
              style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
            />
          </div>

          {/* Date Range */}
          <div>
            <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>Date Range</label>
            <div className="grid grid-cols-2 gap-2">
              <input
                type="datetime-local"
                value={localFilters.fromDate || ''}
                onChange={(e) => setLocalFilters({ ...localFilters, fromDate: e.target.value || undefined })}
                className="px-3 py-2 rounded text-sm"
                style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
              />
              <input
                type="datetime-local"
                value={localFilters.toDate || ''}
                onChange={(e) => setLocalFilters({ ...localFilters, toDate: e.target.value || undefined })}
                className="px-3 py-2 rounded text-sm"
                style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
              />
            </div>
          </div>

          {/* Duration Range */}
          <div>
            <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>Duration (ms)</label>
            <div className="grid grid-cols-2 gap-2">
              <input
                type="number"
                value={localFilters.minDuration || ''}
                onChange={(e) => setLocalFilters({ ...localFilters, minDuration: e.target.value ? parseInt(e.target.value) : undefined })}
                placeholder="Min"
                className="px-3 py-2 rounded text-sm"
                style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
              />
              <input
                type="number"
                value={localFilters.maxDuration || ''}
                onChange={(e) => setLocalFilters({ ...localFilters, maxDuration: e.target.value ? parseInt(e.target.value) : undefined })}
                placeholder="Max"
                className="px-3 py-2 rounded text-sm"
                style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
              />
            </div>
          </div>

          {/* Client Info */}
          <div>
            <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>Client IP</label>
            <input
              type="text"
              value={localFilters.clientIpAddress || ''}
              onChange={(e) => setLocalFilters({ ...localFilters, clientIpAddress: e.target.value || undefined })}
              placeholder="e.g., 192.168.1.1"
              className="w-full px-3 py-2 rounded text-sm"
              style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
            />
          </div>

          <div>
            <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>Source Application</label>
            <input
              type="text"
              value={localFilters.sourceApplication || ''}
              onChange={(e) => setLocalFilters({ ...localFilters, sourceApplication: e.target.value || undefined })}
              placeholder="e.g., web, mobile, api"
              className="w-full px-3 py-2 rounded text-sm"
              style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
            />
          </div>

          <div>
            <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>Requested By</label>
            <input
              type="text"
              value={localFilters.requestedBy || ''}
              onChange={(e) => setLocalFilters({ ...localFilters, requestedBy: e.target.value || undefined })}
              placeholder="Username"
              className="w-full px-3 py-2 rounded text-sm"
              style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
            />
          </div>

          {/* Checkboxes */}
          <div className="space-y-2">
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={localFilters.hasError || false}
                onChange={(e) => setLocalFilters({ ...localFilters, hasError: e.target.checked })}
              />
              <span style={{ color: colors.text }}>Has Error</span>
            </label>
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={localFilters.isMockRequest || false}
                onChange={(e) => setLocalFilters({ ...localFilters, isMockRequest: e.target.checked })}
              />
              <span style={{ color: colors.text }}>Mock Request</span>
            </label>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="flex items-center justify-between gap-2 px-4 py-3 border-t" style={{ borderColor: colors.border }}>
          <button
            onClick={() => {
              setLocalFilters({});
              onApply({});
            }}
            className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.hover, color: colors.text }}
          >
            Clear All
          </button>
          <div className="flex items-center gap-2">
            <button
              onClick={onClose}
              className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.hover, color: colors.text }}
            >
              Cancel
            </button>
            <button
              onClick={() => {
                onApply(localFilters);
                onClose();
              }}
              className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.primaryDark, color: 'white' }}
            >
              Apply Filters
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

// Export Modal Component
const ExportModal = ({ colors, isOpen, onClose, onExport }) => {
  const [exportConfig, setExportConfig] = useState({
    format: 'JSON',
    includeRequestHeaders: true,
    includeResponseHeaders: true,
    includeRequestBody: true,
    includeResponseBody: true,
    includeMetadata: true,
    maskSensitiveData: true,
    dateFormat: 'ISO',
    delimiter: ',',
    flattenNestedObjects: false,
    prettyPrint: true
  });

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50">
      <div className="absolute inset-0 bg-black bg-opacity-50" onClick={onClose} />
      <div 
        className="absolute inset-0 backdrop-blur-sm bg-black/30" 
        onClick={onClose} 
      />
      <div className="relative w-96 rounded-lg overflow-hidden" style={{ 
        backgroundColor: colors.bg,
        border: `1px solid ${colors.border}`
      }}>
        {/* Modal Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Export Requests</h3>
          <button onClick={onClose} className="p-1 rounded hover:bg-opacity-50 transition-colors">
            <X size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        {/* Modal Content */}
        <div className="p-4 space-y-4 max-h-96 overflow-auto">
          {/* Format Selection */}
          <div>
            <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>Export Format</label>
            <div className="grid grid-cols-2 gap-2">
              {['JSON', 'CSV', 'XML', 'EXCEL'].map(format => (
                <button
                  key={format}
                  onClick={() => setExportConfig({ ...exportConfig, format })}
                  className="px-3 py-2 rounded text-sm font-medium transition-colors"
                  style={{ 
                    backgroundColor: exportConfig.format === format ? colors.selected : colors.hover,
                    color: exportConfig.format === format ? colors.primary : colors.text,
                    border: `1px solid ${exportConfig.format === format ? colors.primary : colors.border}`
                  }}
                >
                  {format}
                </button>
              ))}
            </div>
          </div>

          {/* Include Options */}
          <div>
            <h4 className="text-xs font-semibold mb-2" style={{ color: colors.text }}>Include in Export</h4>
            <div className="space-y-2">
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={exportConfig.includeRequestHeaders}
                  onChange={(e) => setExportConfig({ ...exportConfig, includeRequestHeaders: e.target.checked })}
                />
                <span style={{ color: colors.text }}>Request Headers</span>
              </label>
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={exportConfig.includeResponseHeaders}
                  onChange={(e) => setExportConfig({ ...exportConfig, includeResponseHeaders: e.target.checked })}
                />
                <span style={{ color: colors.text }}>Response Headers</span>
              </label>
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={exportConfig.includeRequestBody}
                  onChange={(e) => setExportConfig({ ...exportConfig, includeRequestBody: e.target.checked })}
                />
                <span style={{ color: colors.text }}>Request Body</span>
              </label>
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={exportConfig.includeResponseBody}
                  onChange={(e) => setExportConfig({ ...exportConfig, includeResponseBody: e.target.checked })}
                />
                <span style={{ color: colors.text }}>Response Body</span>
              </label>
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={exportConfig.includeMetadata}
                  onChange={(e) => setExportConfig({ ...exportConfig, includeMetadata: e.target.checked })}
                />
                <span style={{ color: colors.text }}>Metadata</span>
              </label>
            </div>
          </div>

          {/* Additional Options */}
          <div>
            <h4 className="text-xs font-semibold mb-2" style={{ color: colors.text }}>Additional Options</h4>
            <div className="space-y-2">
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={exportConfig.maskSensitiveData}
                  onChange={(e) => setExportConfig({ ...exportConfig, maskSensitiveData: e.target.checked })}
                />
                <span style={{ color: colors.text }}>Mask Sensitive Data</span>
              </label>
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={exportConfig.flattenNestedObjects}
                  onChange={(e) => setExportConfig({ ...exportConfig, flattenNestedObjects: e.target.checked })}
                />
                <span style={{ color: colors.text }}>Flatten Nested Objects (CSV only)</span>
              </label>
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={exportConfig.prettyPrint}
                  onChange={(e) => setExportConfig({ ...exportConfig, prettyPrint: e.target.checked })}
                />
                <span style={{ color: colors.text }}>Pretty Print (JSON/XML only)</span>
              </label>
            </div>
          </div>

          {/* CSV Delimiter (if CSV selected) */}
          {exportConfig.format === 'CSV' && (
            <div>
              <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>CSV Delimiter</label>
              <select
                value={exportConfig.delimiter}
                onChange={(e) => setExportConfig({ ...exportConfig, delimiter: e.target.value })}
                className="w-full px-3 py-2 rounded text-sm"
                style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
              >
                <option value=",">Comma (,)</option>
                <option value=";">Semicolon (;)</option>
                <option value="\t">Tab</option>
                <option value="|">Pipe (|)</option>
              </select>
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div className="flex items-center justify-end gap-2 px-4 py-3 border-t" style={{ borderColor: colors.border }}>
          <button
            onClick={onClose}
            className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.hover, color: colors.text }}
          >
            Cancel
          </button>
          <button
            onClick={() => {
              onExport(exportConfig);
              onClose();
            }}
            className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.primaryDark, color: 'white' }}
          >
            Export
          </button>
        </div>
      </div>
    </div>
  );
};

// Stats Cards Component - Now using data from search API
const StatsCards = ({ requests, apiSummaries, colors }) => {
  // Calculate stats from the available data
  const totalRequests = requests?.length || 0;
  
  // Calculate success rate from current page data (simplified)
  const successfulRequests = requests?.filter(r => 
    r.responseStatusCode >= 200 && r.responseStatusCode < 300
  ).length || 0;
  
  const successRate = totalRequests > 0 ? (successfulRequests / totalRequests * 100) : 0;
  
  // Calculate average response time
  const totalDuration = requests?.reduce((sum, r) => sum + (r.executionDurationMs || 0), 0) || 0;
  const avgResponseTime = totalRequests > 0 ? totalDuration / totalRequests : 0;
  
  // Count failed requests
  const failedRequests = requests?.filter(r => 
    r.responseStatusCode >= 400 || r.requestStatus === 'FAILED'
  ).length || 0;
  
  // Format execution time
  const formatExecutionTimeHelper = (ms) => {
    if (!ms) return 'N/A';
    if (ms < 1000) return `${Math.round(ms)}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`;
    return `${(ms / 60000).toFixed(2)}m`;
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div className="p-4 rounded-lg border hover:shadow-lg transition-all" style={{ 
        backgroundColor: colors.card, 
        borderColor: colors.border 
      }}>
        <div className="flex items-center justify-between mb-2">
          <span className="text-sm font-medium" style={{ color: colors.textSecondary }}>Total Requests (Page)</span>
          <Activity size={18} style={{ color: colors.info }} />
        </div>
        <div className="text-2xl font-bold" style={{ color: colors.text }}>
          {totalRequests.toLocaleString()}
        </div>
        <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
          Current page
        </div>
      </div>

      <div className="p-4 rounded-lg border hover:shadow-lg transition-all" style={{ 
        backgroundColor: colors.card, 
        borderColor: colors.border 
      }}>
        <div className="flex items-center justify-between mb-2">
          <span className="text-sm font-medium" style={{ color: colors.textSecondary }}>Success Rate</span>
          <CheckCircle size={18} style={{ color: colors.success }} />
        </div>
        <div className="text-2xl font-bold" style={{ color: colors.text }}>
          {successRate.toFixed(1)}%
        </div>
        <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
          Current page
        </div>
      </div>

      <div className="p-4 rounded-lg border hover:shadow-lg transition-all" style={{ 
        backgroundColor: colors.card, 
        borderColor: colors.border 
      }}>
        <div className="flex items-center justify-between mb-2">
          <span className="text-sm font-medium" style={{ color: colors.textSecondary }}>Avg Response Time</span>
          <ClockIcon size={18} style={{ color: colors.warning }} />
        </div>
        <div className="text-2xl font-bold" style={{ color: colors.text }}>
          {formatExecutionTimeHelper(avgResponseTime)}
        </div>
        <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
          Current page
        </div>
      </div>

      <div className="p-4 rounded-lg border hover:shadow-lg transition-all" style={{ 
        backgroundColor: colors.card, 
        borderColor: colors.border 
      }}>
        <div className="flex items-center justify-between mb-2">
          <span className="text-sm font-medium" style={{ color: colors.textSecondary }}>Failed Requests</span>
          <XCircle size={18} style={{ color: colors.error }} />
        </div>
        <div className="text-2xl font-bold" style={{ color: colors.text }}>
          {failedRequests.toLocaleString()}
        </div>
        <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
          Current page
        </div>
      </div>
    </div>
  );
};

const APIRequest = ({ theme, isDark, customTheme, toggleTheme, authToken }) => {
  const [activeTab, setActiveTab] = useState('all');
  const [showFilterModal, setShowFilterModal] = useState(false);
  const [showExportModal, setShowExportModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [filters, setFilters] = useState({});
  const [requests, setRequests] = useState([]);
  const [apiSummaries, setApiSummaries] = useState([]);
  const [isLoading, setIsLoading] = useState({
    requests: false,
    export: false,
    delete: false
  });
  const [toast, setToast] = useState(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 12,
    totalElements: 0,
    totalPages: 0
  });
  const [dateRange, setDateRange] = useState({
    fromDate: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16),
    toDate: new Date().toISOString().slice(0, 16)
  });
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [selectedApiId, setSelectedApiId] = useState(null);
  const [selectedApiSummary, setSelectedApiSummary] = useState(null);

  const searchTimer = useRef(null);
  const initialLoadDone = useRef(false);

  // Color scheme (same as CodeBase)
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
    hover: 'rgb(45 46 72 / 33%)',
    active: 'rgb(59 74 99)',
    selected: 'rgb(44 82 130)',
    primary: 'rgb(96 165 250)',
    primaryLight: 'rgb(147 197 253)',
    primaryDark: 'rgb(37 99 235)',
    success: 'rgb(52 211 153)',
    warning: 'rgb(251 191 36)',
    error: 'rgb(248 113 113)',
    info: 'rgb(96 165 250)',
    tabActive: 'rgb(96 165 250)',
    tabInactive: 'rgb(148 163 184)',
    inputBg: 'rgb(41 53 72 / 19%)',
    dropdownBg: 'rgb(41 53 72 / 19%)',
    modalBg: 'rgb(41 53 72 / 19%)',
    codeBg: 'rgb(41 53 72 / 19%)',
    method: {
      GET: 'rgb(52 211 153)',
      POST: 'rgb(96 165 250)',
      PUT: 'rgb(251 191 36)',
      DELETE: 'rgb(248 113 113)',
      PATCH: 'rgb(167 139 250)',
      HEAD: 'rgb(148 163 184)',
      OPTIONS: 'rgb(167 139 250)'
    }
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
    hover: '#f1f5f9',
    active: '#e2e8f0',
    selected: '#dbeafe',
    primary: '#1e293b',
    primaryLight: '#60a5fa',
    primaryDark: '#2563eb',
    success: '#10b981',
    warning: '#f59e0b',
    error: '#ef4444',
    info: '#3b82f6',
    tabActive: '#3b82f6',
    tabInactive: '#64748b',
    inputBg: '#ffffff',
    dropdownBg: '#ffffff',
    modalBg: '#ffffff',
    codeBg: '#f1f5f9',
    method: {
      GET: '#10b981',
      POST: '#3b82f6',
      PUT: '#f59e0b',
      DELETE: '#ef4444',
      PATCH: '#8b5cf6',
      HEAD: '#6b7280',
      OPTIONS: '#8b5cf6'
    }
  };

  // Show toast message
  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  // Copy to clipboard
  const copyToClipboard = (text) => {
    if (!text) {
      showToast('No data to copy', 'warning');
      return;
    }
    navigator.clipboard.writeText(text);
    showToast('Copied to clipboard!', 'success');
  };

  // Get status color and text
  const getStatusColor = (status) => {
    const colors_map = {
      'SUCCESS': colors.success,
      'FAILED': colors.error,
      'TIMEOUT': colors.warning,
      'PENDING': colors.info,
      'ERROR': colors.error,
      'CANCELLED': colors.textSecondary,
      'RETRY': colors.warning
    };
    return colors_map[status] || colors.textSecondary;
  };

  const getStatusText = (status) => {
    if (!status) return 'UNKNOWN';
    // Convert to proper case (e.g., 'SUCCESS' -> 'Success', 'TIMEOUT' -> 'Timeout')
    return status.charAt(0) + status.slice(1).toLowerCase();
  };

  // Get method color
  const getMethodColor = (method) => {
    return colors.method[method] || colors.textSecondary;
  };

  // Format timestamp
  const formatTimestamp = (timestamp) => {
    if (!timestamp) return '';
    try {
      const date = new Date(timestamp);
      return date.toLocaleString();
    } catch (e) {
      return timestamp;
    }
  };

  // Check if request was successful
  const isRequestSuccessfulHelper = (request) => {
    return request?.responseStatusCode >= 200 && request?.responseStatusCode < 300;
  };

  // Format execution time
  const formatExecutionTimeHelper = (ms) => {
    if (!ms) return 'N/A';
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`;
    return `${(ms / 60000).toFixed(2)}m`;
  };

  // Get status code color
  const getStatusCodeColorHelper = (code) => {
    if (!code) return colors.textSecondary;
    if (code >= 200 && code < 300) return colors.success;
    if (code >= 300 && code < 400) return colors.info;
    if (code >= 400 && code < 500) return colors.warning;
    if (code >= 500) return colors.error;
    return colors.textSecondary;
  };

  // Load requests with filters - ONLY SEARCH API
  const loadRequests = useCallback(async (isInitialLoad = false) => {
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }

    // Prevent multiple loads
    if (isInitialLoad && initialLoadDone.current) {
      return;
    }

    setIsLoading(prev => ({ ...prev, requests: true }));

    try {
      const filter = {
        ...filters,
        page: pagination.page,
        size: pagination.size,
        fromDate: dateRange.fromDate,
        toDate: dateRange.toDate,
        search: searchQuery || undefined,
        apiId: selectedApiId || undefined
      };

      // Remove undefined values
      Object.keys(filter).forEach(key => filter[key] === undefined && delete filter[key]);

      console.log('Loading requests with filter:', filter);
      const response = await searchRequests(authToken, filter);
      
      if (response?.responseCode === 200) {
        const responseData = response.data;
        
        // Extract data correctly from the response structure
        setRequests(responseData.content || []);
        setApiSummaries(responseData.apiSummaries || []);
        
        setPagination({
          page: responseData.currentPage || 0,
          size: responseData.pageSize || 15,
          totalElements: responseData.totalElements || 0,
          totalPages: responseData.totalPages || 0
        });

        // If an API is selected, update its summary
        if (selectedApiId && responseData.apiSummaries) {
          const summary = responseData.apiSummaries.find(api => api.apiId === selectedApiId);
          setSelectedApiSummary(summary);
        }
        
        if (isInitialLoad) {
          initialLoadDone.current = true;
        }
      } else {
        showToast(response?.message || 'Failed to load requests', 'error');
      }
    } catch (error) {
      console.error('Error loading requests:', error);
      showToast(error.message || 'Failed to load requests', 'error');
    } finally {
      setIsLoading(prev => ({ ...prev, requests: false }));
    }
  }, [authToken, filters, pagination.page, pagination.size, dateRange.fromDate, dateRange.toDate, searchQuery, selectedApiId]);

  // Load recent requests - using search API with sorting
  const loadRecentRequests = useCallback(async () => {
    if (!authToken) return;

    try {
      const filter = {
        page: 0,
        size: 10,
        sortBy: 'requestTimestamp',
        sortDirection: 'DESC'
      };

      const response = await searchRequests(authToken, filter);
      if (response?.responseCode === 200) {
        setRequests(response.data?.content || []);
      }
    } catch (error) {
      console.error('Error loading recent requests:', error);
    }
  }, [authToken]);

  // Handle export
  const handleExport = async (config) => {
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }

    setIsLoading(prev => ({ ...prev, export: true }));

    try {
      const response = await exportRequests(
        authToken,
        filters.apiId || selectedApiId || 'all',
        dateRange.fromDate,
        dateRange.toDate,
        config.format,
        config
      );

      if (response?.responseCode === 200) {
        const exportData = response.data;
        if (exportData.downloadUrl) {
          window.open(exportData.downloadUrl, '_blank');
          showToast('Export started successfully', 'success');
        } else {
          showToast('Export completed', 'success');
        }
      } else {
        showToast(response?.message || 'Failed to export requests', 'error');
      }
    } catch (error) {
      console.error('Error exporting requests:', error);
      showToast(error.message || 'Failed to export requests', 'error');
    } finally {
      setIsLoading(prev => ({ ...prev, export: false }));
    }
  };

  // Handle delete request
  const handleDeleteRequest = async (requestId) => {
    if (!authToken || !requestId) return;

    if (!window.confirm('Are you sure you want to delete this request?')) {
      return;
    }

    setIsLoading(prev => ({ ...prev, delete: true }));

    try {
      const response = await deleteRequest(authToken, requestId);
      
      if (response?.responseCode === 200) {
        showToast('Request deleted successfully', 'success');
        loadRequests();
      } else {
        showToast(response?.message || 'Failed to delete request', 'error');
      }
    } catch (error) {
      console.error('Error deleting request:', error);
      showToast(error.message || 'Failed to delete request', 'error');
    } finally {
      setIsLoading(prev => ({ ...prev, delete: false }));
    }
  };

  // Handle cleanup old requests
  const handleCleanup = async () => {
    if (!authToken) return;

    const date = prompt('Enter date to delete requests older than (YYYY-MM-DD):');
    if (!date) return;

    setIsLoading(prev => ({ ...prev, delete: true }));

    try {
      const response = await cleanupOldRequests(authToken, date);
      
      if (response?.responseCode === 200) {
        showToast(`Deleted ${response.data?.deletedCount || 0} old requests`, 'success');
        loadRequests();
      } else {
        showToast(response?.message || 'Failed to cleanup requests', 'error');
      }
    } catch (error) {
      console.error('Error cleaning up requests:', error);
      showToast(error.message || 'Failed to cleanup requests', 'error');
    } finally {
      setIsLoading(prev => ({ ...prev, delete: false }));
    }
  };

  // Handle view request details
  const handleViewDetails = async (request) => {
    if (!authToken || !request?.id) return;

    try {
      const response = await getRequestById(authToken, request.id);
      
      if (response?.responseCode === 200) {
        setSelectedRequest(response.data);
        setShowDetailsModal(true);
      } else {
        showToast(response?.message || 'Failed to load request details', 'error');
      }
    } catch (error) {
      console.error('Error loading request details:', error);
      showToast(error.message || 'Failed to load request details', 'error');
    }
  };

  // Handle API selection from sidebar
  const handleApiSelect = (apiId) => {
    setSelectedApiId(apiId);
    setPagination(prev => ({ ...prev, page: 0 }));
    loadRequests(); // This will reload with the selected API ID
    
    // Find and set the API summary for the selected API
    if (apiId && apiSummaries.length > 0) {
      const summary = apiSummaries.find(api => api.apiId === apiId);
      setSelectedApiSummary(summary || null);
    } else {
      setSelectedApiSummary(null);
    }
  };

  // Handle search with debounce
  const handleSearchChange = (e) => {
    const query = e.target.value;
    setSearchQuery(query);
    setPagination(prev => ({ ...prev, page: 0 }));
    
    if (searchTimer.current) {
      clearTimeout(searchTimer.current);
    }
    
    searchTimer.current = setTimeout(() => {
      loadRequests();
    }, 500);
  };

  // SINGLE LOAD - Only on initial mount
  useEffect(() => {
    if (authToken && !initialLoadDone.current) {
      console.log('Initial page load - calling search API once');
      loadRequests(true);
    }

    return () => {
      if (searchTimer.current) {
        clearTimeout(searchTimer.current);
      }
    };
  }, [authToken]); // Only run when authToken changes

  // Handle tab changes - update the view but don't reload data unnecessarily
  useEffect(() => {
    if (!authToken) return;

    // Only load data if we're switching to a tab that needs data
    // and we don't already have data
    if (activeTab === 'all') {
      if (requests.length === 0) {
        loadRequests();
      }
    } else if (activeTab === 'recent') {
      if (requests.length === 0) {
        loadRecentRequests();
      }
    }
  }, [activeTab, authToken]); // Remove dependencies that cause reloads

  // Handle filter changes - only when filters actually change and not on initial load
  useEffect(() => {
    if (authToken && activeTab === 'all' && initialLoadDone.current) {
      // Don't reload on initial filter setup
      const hasFilters = Object.keys(filters).length > 0;
      if (hasFilters || pagination.page > 0) {
        loadRequests();
      }
    }
  }, [filters, pagination.page, pagination.size, dateRange.fromDate, dateRange.toDate, searchQuery, selectedApiId]);

  // Update selected API summary when apiSummaries change
  useEffect(() => {
    if (selectedApiId && apiSummaries.length > 0) {
      const summary = apiSummaries.find(api => api.apiId === selectedApiId);
      setSelectedApiSummary(summary || null);
    } else if (!selectedApiId) {
      setSelectedApiSummary(null);
    }
  }, [selectedApiId, apiSummaries]);

  // Render sidebar
  const renderSidebar = () => {
    // Calculate totals from apiSummaries
    const totalRequests = apiSummaries.reduce((sum, api) => sum + api.totalRequests, 0);
    const totalSuccess = apiSummaries.reduce((sum, api) => sum + api.successCount, 0);
    const totalFailed = apiSummaries.reduce((sum, api) => sum + api.failedCount, 0);

    return (
      <div 
        className="h-full border-r transition-all duration-300 flex flex-col"
        style={{ 
          width: sidebarCollapsed ? '60px' : '350px',
          backgroundColor: colors.sidebar,
          borderColor: colors.border
        }}
      >
        {/* Sidebar Header */}
        <div className="flex items-center justify-between p-3 border-b" style={{ borderColor: colors.border }}>
          {!sidebarCollapsed && (
            <span className="text-xs font-semibold uppercase" style={{ color: colors.textSecondary }}>API Request Monitor</span>
          )}
          <button
            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
            className="p-1 rounded hover:bg-opacity-50 transition-colors ml-auto"
            style={{ backgroundColor: colors.hover }}
          >
            <Sidebar size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        {/* Sidebar Content */}
        <div className="flex-1 overflow-auto p-2">
          {/* All Requests Option */}
          <div
            onClick={() => handleApiSelect(null)}
            className={`flex items-center gap-2 p-2 rounded cursor-pointer transition-colors mb-2 ${
              !selectedApiId ? 'ring-1' : ''
            }`}
            style={{ 
              backgroundColor: !selectedApiId ? colors.selected : 'transparent',
              color: !selectedApiId ? colors.primary : colors.text,
              borderColor: !selectedApiId ? colors.primary : 'transparent'
            }}
          >
            <Home size={16} style={{ color: !selectedApiId ? colors.primary : colors.textSecondary }} />
            {!sidebarCollapsed && (
              <>
                <span className="text-sm flex-1">All Requests</span>
                <span className="text-xs px-1.5 py-0.5 rounded" style={{ backgroundColor: colors.hover }}>
                  {totalRequests}
                </span>
              </>
            )}
          </div>

          {/* API List from apiSummaries */}
          {apiSummaries.length > 0 ? (
            apiSummaries.map(api => (
              <div
                key={api.apiId}
                onClick={() => handleApiSelect(api.apiId)}
                className={`flex items-start gap-2 p-2 rounded cursor-pointer transition-colors mb-2 ${
                  selectedApiId === api.apiId ? 'ring-1' : ''
                }`}
                style={{ 
                  backgroundColor: selectedApiId === api.apiId ? colors.selected : 'transparent',
                  color: selectedApiId === api.apiId ? colors.primary : colors.text,
                  borderColor: selectedApiId === api.apiId ? colors.primary : 'transparent'
                }}
              >
                <DatabaseIcon size={14} style={{ color: selectedApiId === api.apiId ? colors.primary : colors.textSecondary }} className="mt-1" />
                {!sidebarCollapsed && (
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium truncate" title={api.apiName}>
                        {api.apiName}
                      </span>
                      <span className="text-xs px-1.5 py-0.5 rounded ml-2" style={{ backgroundColor: colors.hover }}>
                        {api.totalRequests}
                      </span>
                    </div>
                    
                    {/* API Code */}
                    <div className="text-xs mt-1 font-mono" style={{ color: colors.textTertiary }}>
                      {api.apiCode}
                    </div>
                    
                    {/* Success/Failed counts */}
                    <div className="flex items-center gap-2 mt-1">
                      <span className="text-xs" style={{ color: colors.success }}>✓ {api.successCount}</span>
                      <span className="text-xs" style={{ color: colors.error }}>✗ {api.failedCount}</span>
                      <span className="text-xs" style={{ color: colors.warning }}>
                        {api.successRate.toFixed(1)}%
                      </span>
                    </div>
                    
                    {/* Last request info */}
                    {api.lastRequestTime && (
                      <div className="flex items-center gap-1 mt-1 text-xs" style={{ color: colors.textTertiary }}>
                        <Clock size={10} />
                        <span className="truncate">{new Date(api.lastRequestTime).toLocaleString()}</span>
                        <span className="ml-1 px-1 rounded text-[10px]" style={{ 
                          backgroundColor: api.lastRequestStatus === 'SUCCESS' ? `${colors.success}20` : `${colors.error}20`,
                          color: api.lastRequestStatus === 'SUCCESS' ? colors.success : colors.error
                        }}>
                          {api.lastRequestStatus}
                        </span>
                      </div>
                    )}
                    
                    {/* Average response time */}
                    {api.averageResponseTimeMs && (
                      <div className="flex items-center gap-1 mt-1 text-xs" style={{ color: colors.textTertiary }}>
                        <ClockIcon size={10} />
                        <span>Avg: {formatExecutionTimeHelper(api.averageResponseTimeMs)}</span>
                      </div>
                    )}
                  </div>
                )}
              </div>
            ))
          ) : (
            !sidebarCollapsed && (
              <div className="text-center p-4" style={{ color: colors.textSecondary }}>
                <DatabaseIcon size={20} className="mx-auto mb-2 opacity-50" />
                <p className="text-xs">No APIs found</p>
              </div>
            )
          )}
        </div>
      </div>
    );
  };

  // Render requests table
  const renderRequestsTable = () => {
    return (
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Table container with scroll */}
        <div className="flex-1 overflow-auto">
          <table className="w-full" style={{ borderCollapse: 'collapse' }}>
            <thead className="sticky top-0" style={{ backgroundColor: colors.card, zIndex: 10 }}>
              <tr style={{ borderBottom: `1px solid ${colors.border}` }}>
                <th className="text-left py-3 px-4 text-xs font-medium w-12" style={{ color: colors.textSecondary }}>#</th>
                <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Status</th>
                <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Method</th>
                <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Request Name</th>
                <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>API</th>
                <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Status Code</th>
                <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Duration</th>
                <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Timestamp</th>
                <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Correlation ID</th>
                <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {requests.map((request, index) => {
                const sequentialNumber = (pagination.page * pagination.size) + index + 1;
                const statusColor = getStatusColor(request.requestStatus);
                const statusText = getStatusText(request.requestStatus);
                
                return (
                  <tr 
                    key={request.id || index}
                    className="hover:bg-opacity-50 transition-colors cursor-pointer"
                    style={{ borderBottom: `1px solid ${colors.border}`, backgroundColor: 'transparent' }}
                    onClick={() => handleViewDetails(request)}
                  >
                    <td className="py-3 px-4">
                      <span className="text-sm font-mono" style={{ color: colors.textSecondary }}>
                        {sequentialNumber}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        <div className="w-2 h-2 rounded-full" style={{ backgroundColor: statusColor }} />
                        <span className="text-xs font-medium" style={{ color: statusColor }}>
                          {statusText}
                        </span>
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <span className="text-xs font-medium px-2 py-1 rounded" style={{ 
                        backgroundColor: getMethodColor(request.httpMethod),
                        color: 'white'
                      }}>
                        {request.httpMethod}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <span className="text-sm" style={{ color: colors.text }}>{request.requestName || 'N/A'}</span>
                    </td>
                    <td className="py-3 px-4">
                      <span className="text-xs" style={{ color: colors.textSecondary }}>{request.apiCode || 'N/A'}</span>
                    </td>
                    <td className="py-3 px-4">
                      <span className="text-sm font-medium" style={{ color: getStatusCodeColorHelper(request.responseStatusCode) }}>
                        {request.responseStatusCode || '-'}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <span className="text-sm" style={{ color: colors.text }}>
                        {formatExecutionTimeHelper(request.executionDurationMs)}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <span className="text-xs" style={{ color: colors.textSecondary }}>
                        {formatTimestamp(request.requestTimestamp)}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <span className="text-xs font-mono" style={{ color: colors.textSecondary }}>
                        {request.correlationId?.substring(0, 8)}...
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleViewDetails(request);
                          }}
                          className="p-1 rounded hover:bg-opacity-50 transition-colors"
                          style={{ backgroundColor: colors.hover }}
                        >
                          <Eye size={12} style={{ color: colors.textSecondary }} />
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            copyToClipboard(JSON.stringify(request, null, 2));
                          }}
                          className="p-1 rounded hover:bg-opacity-50 transition-colors"
                          style={{ backgroundColor: colors.hover }}
                        >
                          <Copy size={12} style={{ color: colors.textSecondary }} />
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDeleteRequest(request.id);
                          }}
                          className="p-1 rounded hover:bg-opacity-50 transition-colors"
                          style={{ backgroundColor: colors.hover }}
                        >
                          <Trash2 size={12} style={{ color: colors.error }} />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
              
              {/* Pagination row */}
              {pagination.totalPages > 0 && (
                <tr style={{ backgroundColor: colors.card }}>
                  <td colSpan="10" className="py-3 px-4">
                    <div className="flex items-center justify-between">
                      <div className="text-sm" style={{ color: colors.textSecondary }}>
                        Showing {requests.length > 0 ? pagination.page * pagination.size + 1 : 0} - {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of {pagination.totalElements}
                      </div>
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => {
                            setPagination(prev => ({ ...prev, page: prev.page - 1 }));
                          }}
                          disabled={pagination.page === 0}
                          className="px-3 py-1 rounded text-sm font-medium hover:bg-opacity-50 transition-colors disabled:opacity-50 flex items-center gap-1"
                          style={{ backgroundColor: colors.hover, color: colors.text }}
                        >
                          <ChevronLeft size={14} />
                          Previous
                        </button>
                        <div className="flex items-center gap-1 px-2">
                          <span className="text-sm font-medium" style={{ color: colors.text }}>
                            {pagination.page + 1}
                          </span>
                          <span className="text-sm" style={{ color: colors.textSecondary }}>
                            of {pagination.totalPages}
                          </span>
                        </div>
                        <button
                          onClick={() => {
                            setPagination(prev => ({ ...prev, page: prev.page + 1 }));
                          }}
                          disabled={pagination.page >= pagination.totalPages - 1}
                          className="px-3 py-1 rounded text-sm font-medium hover:bg-opacity-50 transition-colors disabled:opacity-50 flex items-center gap-1"
                          style={{ backgroundColor: colors.hover, color: colors.text }}
                        >
                          Next
                          <ChevronRight size={14} />
                        </button>
                      </div>
                    </div>
                  </td>
                </tr>
              )}

              {/* Empty state row */}
              {requests.length === 0 && !isLoading.requests && (
                <tr>
                  <td colSpan="10" className="text-center py-12">
                    <FileText size={48} className="mx-auto mb-4 opacity-50" style={{ color: colors.textSecondary }} />
                    <p className="text-lg mb-2" style={{ color: colors.text }}>No Requests Found</p>
                    <p className="text-sm" style={{ color: colors.textSecondary }}>
                      Try adjusting your filters or date range
                    </p>
                  </td>
                </tr>
              )}

              {/* Loading state row */}
              {isLoading.requests && (
                <tr>
                  <td colSpan="10" className="text-center py-12">
                    <RefreshCw size={32} className="animate-spin mx-auto mb-4" style={{ color: colors.textSecondary }} />
                    <p className="text-sm" style={{ color: colors.textSecondary }}>Loading requests...</p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  return (
    <div className="flex h-screen overflow-hidden" style={{ 
      backgroundColor: colors.bg,
      color: colors.text,
      fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif',
      fontSize: '13px'
    }}>
      <style>{`
        @keyframes fadeInUp {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }
        
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
        
        .animate-fade-in-up {
          animation: fadeInUp 0.2s ease-out;
        }
        
        .animate-spin {
          animation: spin 1s linear infinite;
        }
        
        .hover-lift:hover {
          transform: translateY(-2px);
          transition: transform 0.2s ease;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        /* Custom scrollbar */
        ::-webkit-scrollbar {
          width: 8px;
          height: 8px;
        }
        
        ::-webkit-scrollbar-track {
          background: ${colors.border};
          border-radius: 4px;
        }
        
        ::-webkit-scrollbar-thumb {
          background: ${colors.textTertiary};
          border-radius: 4px;
        }
        
        ::-webkit-scrollbar-thumb:hover {
          background: ${colors.textSecondary};
        }
      `}</style>

      {/* Sidebar */}
      {renderSidebar()}

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        
        {/* TOP NAVIGATION */}
        <div className="flex items-center justify-between h-10 px-4 border-b" style={{ 
          backgroundColor: colors.header,
          borderColor: colors.border
        }}>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-1 -ml-3 text-nowrap">
              <span className="px-3 py-1.5 text-sm font-medium rounded transition-colors uppercase" style={{ color: colors.text }}>
                &nbsp;
              </span>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {/* Search Input */}
            <div className="relative">
              <Search size={14} className="absolute left-2 top-1/2 transform -translate-y-1/2" style={{ color: colors.textSecondary }} />
              <input
                type="text"
                placeholder="Search requests..."
                value={searchQuery}
                onChange={handleSearchChange}
                className="pl-8 pr-3 py-1.5 rounded text-sm w-64"
                style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
              />
            </div>

            {/* Filter Button */}
            <button 
              onClick={() => setShowFilterModal(true)}
              className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}>
              <Filter size={14} style={{ color: colors.textSecondary }} />
            </button>

            {/* Export Button */}
            <button 
              onClick={() => setShowExportModal(true)}
              className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}>
              <DownloadCloud size={14} style={{ color: colors.textSecondary }} />
            </button>

            {/* Refresh Button */}
            <button 
              className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
              onClick={loadRequests}
              disabled={isLoading.requests}
              style={{ backgroundColor: colors.hover }}>
              <RefreshCw size={14} className={isLoading.requests ? 'animate-spin' : ''} style={{ color: colors.textSecondary }} />
            </button>
          </div>
        </div>

        {/* Stats Cards - Now using data from the current requests */}
        <div className="w-full p-4 mt-2 -mb-2 space-y-6">
          <StatsCards 
            requests={requests}
            apiSummaries={apiSummaries}
            colors={colors}
          />
        </div>

        {/* API Summary Bar (if API selected) */}
        {selectedApiId && selectedApiSummary && (
          <div className="flex items-center gap-4 px-4 py-2 border-b" style={{ 
            borderColor: colors.border, 
            backgroundColor: colors.hover
          }}>
            <span className="text-xs font-medium" style={{ color: colors.textSecondary }}>API Summary:</span>
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-1">
                <CheckCircle size={12} style={{ color: colors.success }} />
                <span className="text-xs" style={{ color: colors.text }}>Success: {selectedApiSummary.successCount || 0}</span>
              </div>
              <div className="flex items-center gap-1">
                <XCircle size={12} style={{ color: colors.error }} />
                <span className="text-xs" style={{ color: colors.text }}>Failed: {selectedApiSummary.failedCount || 0}</span>
              </div>
              <div className="flex items-center gap-1">
                <ClockIcon size={12} style={{ color: colors.warning }} />
                <span className="text-xs" style={{ color: colors.text }}>Avg: {formatExecutionTimeHelper(selectedApiSummary.averageResponseTimeMs)}</span>
              </div>
              <div className="flex items-center gap-1">
                <Activity size={12} style={{ color: colors.info }} />
                <span className="text-xs" style={{ color: colors.text }}>Total: {selectedApiSummary.totalRequests || 0}</span>
              </div>
              {selectedApiSummary.successRate !== undefined && (
                <div className="flex items-center gap-1">
                  <PieChart size={12} style={{ color: colors.primary }} />
                  <span className="text-xs" style={{ color: colors.text }}>Success Rate: {selectedApiSummary.successRate.toFixed(1)}%</span>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Date Range Bar */}
        <div className="flex items-center gap-4 px-4 py-2 border-b" style={{ 
          borderColor: colors.border, 
          backgroundColor: colors.card
        }}>
          <div className="flex items-center gap-2">
            <Calendar size={14} style={{ color: colors.textSecondary }} />
            <span className="text-sm" style={{ color: colors.textSecondary }}>Date Range:</span>
          </div>
          <input
            type="datetime-local"
            value={dateRange.fromDate}
            onChange={(e) => setDateRange({ ...dateRange, fromDate: e.target.value })}
            className="px-2 py-1 rounded text-sm"
            style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
          />
          <span style={{ color: colors.textSecondary }}>to</span>
          <input
            type="datetime-local"
            value={dateRange.toDate}
            onChange={(e) => setDateRange({ ...dateRange, toDate: e.target.value })}
            className="px-2 py-1 rounded text-sm"
            style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
          />
          <button
            onClick={() => {
              setPagination(prev => ({ ...prev, page: 0 }));
              loadRequests();
            }}
            className="px-3 py-1 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.primaryDark, color: 'white' }}
          >
            Apply
          </button>
        </div>

        {/* MAIN CONTENT */}
        {renderRequestsTable()}

      </div>

      {/* Modals */}
      <RequestDetailsModal
        request={selectedRequest}
        colors={colors}
        isOpen={showDetailsModal}
        onClose={() => setShowDetailsModal(false)}
        onRefresh={loadRequests}
      />

      <FilterModal
        filters={filters}
        colors={colors}
        isOpen={showFilterModal}
        onClose={() => setShowFilterModal(false)}
        onApply={(newFilters) => {
          setFilters(newFilters);
          setPagination(prev => ({ ...prev, page: 0 }));
        }}
      />

      <ExportModal
        colors={colors}
        isOpen={showExportModal}
        onClose={() => setShowExportModal(false)}
        onExport={handleExport}
      />

      {/* TOAST */}
      {toast && (
        <div className="fixed bottom-4 right-4 px-4 py-2 rounded text-sm font-medium z-50 animate-fade-in-up"
          style={{ 
            backgroundColor: toast.type === 'error' ? colors.error : 
                          toast.type === 'success' ? colors.success : 
                          toast.type === 'warning' ? colors.warning : 
                          colors.info,
            color: 'white'
          }}>
          {toast.message}
        </div>
      )}
    </div>
  );
};

export default APIRequest;