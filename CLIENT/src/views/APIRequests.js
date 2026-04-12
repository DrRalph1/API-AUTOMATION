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
  Sidebar,
  LayoutDashboard
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

// ============ SYNTAX HIGHLIGHTER COMPONENT ============
const SyntaxHighlighter = ({ language, code }) => {
  if (!code) return <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed">// No code available</pre>;
  
  const highlightCode = (code, lang) => {
    const lines = String(code).split('\n');
    
    return lines.map((line, lineIndex) => {
      let highlightedLine = line
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
      
      if (lang === 'java') {
        if (highlightedLine.includes('/*') || highlightedLine.includes('*/')) {
          return (
            <div key={lineIndex} className="text-gray-500">
              {highlightedLine}
            </div>
          );
        }
        
        highlightedLine = highlightedLine.replace(/"([^"\\]*(\\.[^"\\]*)*)"/g, 
          '<span class="text-green-400">"$1"</span>');
        
        highlightedLine = highlightedLine.replace(/'([^'\\]*(\\.[^'\\]*)*)'/g, 
          '<span class="text-green-400">\'$1\'</span>');
        
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(\/\/.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        
        highlightedLine = highlightedLine.replace(/(@\w+)/g, 
          '<span class="text-blue-400">$1</span>');
        
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
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, 
            '<span class="text-purple-400">$1</span>');
        });
        
        if (!highlightedLine.includes('class="text-green-400"') && 
            !highlightedLine.includes('class="text-gray-500"')) {
          highlightedLine = highlightedLine.replace(/\b(\d+[lLfFdD]?)\b(?![^<]*>|[^>]*<\/)/g, 
            '<span class="text-blue-400">$1</span>');
        }
        
        highlightedLine = highlightedLine.replace(/class="([^"]*)"([^>]*?)>/g, 'class="$1"$2>');
        
      } else if (lang === 'javascript' || lang === 'nodejs') {
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)"|'([^'\\]*(\\.[^'\\]*)*)')/g, 
          '<span class="text-green-400">$1</span>');
        
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(\/\/.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        
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

// ============ REQUEST DETAILS MODAL COMPONENT ============
const RequestDetailsModal = ({ request, colors, isOpen, onClose, onRefresh, getStatusText, getStatusCodeColorHelper }) => {
  if (!isOpen || !request) return null;

  const [activeTab, setActiveTab] = useState('request');
  const [toast, setToast] = useState(null);

  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const isRequestSuccessfulHelper = (request) => {
    return request?.responseStatusCode >= 200 && request?.responseStatusCode < 300;
  };

  const formatRequestTimestampHelper = (timestamp) => {
    if (!timestamp) return 'N/A';
    try {
      return new Date(timestamp).toLocaleString();
    } catch (e) {
      return timestamp;
    }
  };

  const formatExecutionTimeHelper = (ms) => {
    if (ms === null || ms === undefined) return 'N/A';

    const time = Number(ms);

    if (isNaN(time)) return 'N/A';

    if (time < 1000) return `${time.toFixed(2)}ms`;
    if (time < 60000) return `${(time / 1000).toFixed(2)}s`;
    return `${(time / 60000).toFixed(2)}m`;
  };

  const copyToClipboard = (text, type = 'json') => {
    if (!text) {
      showToast(`Nothing to copy`, 'warning');
      return;
    }
    
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text)
        .then(() => {
          showToast(`Copied ${type} to clipboard!`, 'success');
        })
        .catch((err) => {
          console.error('Clipboard write failed:', err);
          fallbackCopy(text, type);
        });
    } else {
      fallbackCopy(text, type);
    }
    
    function fallbackCopy(text, type) {
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
          showToast(`Copied ${type} to clipboard!`, 'success');
        } else {
          showToast(`Failed to copy ${type}`, 'error');
        }
      } catch (err) {
        console.error('Fallback copy failed:', err);
        showToast(`Failed to copy ${type}`, 'error');
      }
      
      document.body.removeChild(textarea);
    }
  };

  // Helper functions to get request and response data
  const getRequestData = () => {
    const requestData = {
      method: request.httpMethod,
      url: request.url,
      headers: request.headers,
      body: request.requestBody,
      correlationId: request.correlationId,
      requestName: request.requestName,
      timestamp: request.requestTimestamp || request.createdAt
    };
    return JSON.stringify(requestData, null, 2);
  };

  const getResponseData = () => {
    const responseData = {
      statusCode: request.responseStatusCode,
      statusMessage: request.responseStatusMessage,
      body: request.responseBody,
      headers: request.responseHeaders,
      size: request.responseSizeBytes,
      timestamp: request.responseTimestamp,
      duration: request.executionDurationMs
    };
    return JSON.stringify(responseData, null, 2);
  };

  const getFullJsonData = () => {
    return JSON.stringify(request, null, 2);
  };

  // Get button color based on response status code
  const getResponseButtonColor = () => {
    const statusCode = request.responseStatusCode;
    if (!statusCode) return colors.textSecondary;
    if (statusCode >= 200 && statusCode < 300) return colors.success;
    if (statusCode >= 300 && statusCode < 400) return colors.info;
    if (statusCode >= 400 && statusCode < 500) return colors.warning;
    if (statusCode >= 500) return colors.error;
    return colors.textSecondary;
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50">
      <div 
        className="absolute inset-0 backdrop-blur-2xl bg-black/70"
        onClick={onClose} 
      />
      <div className="relative w-3/4 max-w-4xl max-h-[80vh] rounded-lg overflow-hidden" style={{ 
        backgroundColor: colors.bg,
        border: `1px solid ${colors.border}`
      }}>
        <div className="flex items-center justify-between px-6 py-4 border-b" style={{ borderColor: colors.border }}>
          <div className="flex items-center gap-3">
            <h2 className="text-lg font-semibold" style={{ color: colors.text }}>
              Request Details
            </h2>
            <div className="flex items-center gap-2">
              <span className="text-xs px-2 py-1 rounded" style={{ 
                backgroundColor: getStatusCodeColorHelper(request.responseStatusCode),
                color: 'white'
              }}>
                {request.responseStatusCode || 'Pending'}
              </span>
              <span className="text-xs px-2 py-1 rounded" style={{ 
                backgroundColor: `${getStatusCodeColorHelper(request.responseStatusCode)}20`,
                color: getStatusCodeColorHelper(request.responseStatusCode)
              }}>
                {getStatusText(request.responseStatusCode)}
              </span>
            </div>
          </div>
          <button onClick={onClose} className="p-1 rounded hover:bg-opacity-50 transition-colors">
            <X size={18} style={{ color: colors.textSecondary }} />
          </button>
        </div>

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

        <div className="p-6 overflow-auto max-h-[calc(80vh-120px)]">
          {activeTab === 'request' && (
            <div className="space-y-4">
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
                </div>
              </div>

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
            </div>
          )}

          {activeTab === 'headers' && (
            <div className="space-y-4">
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
            </div>
          )}

          {activeTab === 'timeline' && (
            <div className="space-y-4">
              <div className="relative pl-8 space-y-6">
                <div className="relative">
                  <div className="absolute left-[-24px] top-0 w-3 h-3 rounded-full" style={{ backgroundColor: colors.info }} />
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Request Created</div>
                  <div className="text-sm" style={{ color: colors.text }}>{formatRequestTimestampHelper(request.createdAt)}</div>
                </div>

                {request.requestTimestamp && (
                  <div className="relative">
                    <div className="absolute left-[-24px] top-0 w-3 h-3 rounded-full" style={{ backgroundColor: colors.primary }} />
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Request Sent</div>
                    <div className="text-sm" style={{ color: colors.text }}>{formatRequestTimestampHelper(request.requestTimestamp)}</div>
                  </div>
                )}

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
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Avg Response Time</div>
                  <div className="text-2xl font-semibold" style={{ color: colors.text }}>
                    {formatExecutionTimeHelper(request.summary.averageResponseTime || 0)}
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        <div className="flex items-center justify-end gap-2 px-6 py-3 border-t" style={{ borderColor: colors.border }}>
          {activeTab === 'request' && (
            <>
              <button
                onClick={() => copyToClipboard(getFullJsonData(), 'JSON')}
                className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                <Copy size={12} />
                Copy JSON
              </button>
              <button
                onClick={() => copyToClipboard(getRequestData(), 'Request')}
                className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2"
                style={{ backgroundColor: colors.info, color: 'white' }}
              >
                <Copy size={12} />
                Copy Request
              </button>
            </>
          )}
          
          {activeTab === 'response' && (
            <>
              <button
                onClick={() => copyToClipboard(getFullJsonData(), 'JSON')}
                className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                <Copy size={12} />
                Copy JSON
              </button>
              <button
                onClick={() => copyToClipboard(getResponseData(), 'Response')}
                className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2"
                style={{ backgroundColor: getResponseButtonColor(), color: 'white' }}
              >
                <Copy size={12} />
                Copy Response
              </button>
            </>
          )}
          
          {activeTab !== 'request' && activeTab !== 'response' && (
            <button
              onClick={() => copyToClipboard(getFullJsonData(), 'JSON')}
              className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2"
              style={{ backgroundColor: colors.hover, color: colors.text }}
            >
              <Copy size={12} />
              Copy JSON
            </button>
          )}
          
          <button
            onClick={onClose}
            className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.primaryDark, color: 'white' }}
          >
            Close
          </button>
        </div>
      </div>

      {/* Toast notification */}
      {toast && (
        <div className="fixed bottom-4 right-4 px-4 py-2 rounded text-sm font-medium z-[60] animate-fade-in-up"
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

// ============ FILTER MODAL COMPONENT ============
const FilterModal = ({ filters, colors, isOpen, onClose, onApply, onExpandDateRange }) => {
  const [localFilters, setLocalFilters] = useState(filters || {});

  useEffect(() => {
    setLocalFilters(filters || {});
  }, [filters]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50">
      <div className="absolute inset-0 bg-black bg-opacity-50" onClick={onClose} />
      <div className="relative w-96 rounded-lg overflow-hidden" style={{ 
        backgroundColor: colors.bg,
        border: `1px solid ${colors.border}`
      }}>
        <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Filter Requests</h3>
          <button onClick={onClose} className="p-1 rounded hover:bg-opacity-50 transition-colors">
            <X size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        <div className="p-4 space-y-4 max-h-96 overflow-auto">
          <div>
            <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>Status</label>
            <select
              value={localFilters.requestStatus || ''}
              onChange={(e) => setLocalFilters({ ...localFilters, requestStatus: e.target.value || undefined })}
              className="w-full px-3 py-2 rounded text-sm"
              style={{ backgroundColor: colors.bg, border: `1px solid ${colors.border}`, color: colors.text }}
            >
              <option value="">All Statuses</option>
              <option value="SUCCESS">Success</option>
              <option value="FAILED">Failed</option>
              <option value="TIMEOUT">Timeout</option>
              <option value="PENDING">Pending</option>
            </select>
          </div>

          <div>
            <label className="text-xs mb-1 block" style={{ color: colors.textSecondary }}>HTTP Method</label>
            <select
              value={localFilters.httpMethod || ''}
              onChange={(e) => setLocalFilters({ ...localFilters, httpMethod: e.target.value || undefined })}
              className="w-full px-3 py-2 rounded text-sm"
              style={{ backgroundColor: colors.bg, border: `1px solid ${colors.border}`, color: colors.text }}
            >
              <option value="">All Methods</option>
              <option value="GET">GET</option>
              <option value="POST">POST</option>
              <option value="PUT">PUT</option>
              <option value="DELETE">DELETE</option>
              <option value="PATCH">PATCH</option>
            </select>
          </div>

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

          <div className="space-y-2">
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={localFilters.hasError || false}
                onChange={(e) => setLocalFilters({ ...localFilters, hasError: e.target.checked })}
              />
              <span style={{ color: colors.text }}>Has Error</span>
            </label>
          </div>

          {/* Quick Date Range Buttons */}
          <div>
            <label className="text-xs mb-2 block" style={{ color: colors.textSecondary }}>Quick Date Ranges</label>
            <div className="flex gap-2">
              <button
                onClick={() => onExpandDateRange(7)}
                className="flex-1 px-2 py-1 rounded text-xs font-medium hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                Last 7 days
              </button>
              <button
                onClick={() => onExpandDateRange(30)}
                className="flex-1 px-2 py-1 rounded text-xs font-medium hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                Last 30 days
              </button>
              <button
                onClick={() => onExpandDateRange(365)}
                className="flex-1 px-2 py-1 rounded text-xs font-medium hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                Last year
              </button>
            </div>
          </div>
        </div>


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
        // Clean up filters before applying
        const cleanFilters = {};
        
        if (localFilters.requestStatus && localFilters.requestStatus !== '') {
          cleanFilters.requestStatus = localFilters.requestStatus;
        }
        if (localFilters.httpMethod && localFilters.httpMethod !== '') {
          cleanFilters.httpMethod = localFilters.httpMethod;
        }
        if (localFilters.responseStatusCode) {
          cleanFilters.responseStatusCode = localFilters.responseStatusCode;
        }
        if (localFilters.apiId && localFilters.apiId !== '') {
          cleanFilters.apiId = localFilters.apiId;
        }
        if (localFilters.hasError) {
          cleanFilters.hasError = true;
        }
        
        onApply(cleanFilters);
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

// ============ EXPORT MODAL COMPONENT ============
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
      <div className="relative w-96 rounded-lg overflow-hidden" style={{ 
        backgroundColor: colors.bg,
        border: `1px solid ${colors.border}`
      }}>
        <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Export Requests</h3>
          <button onClick={onClose} className="p-1 rounded hover:bg-opacity-50 transition-colors">
            <X size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        <div className="p-4 space-y-4 max-h-96 overflow-auto">
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
            </div>
          </div>
        </div>

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

// ============ STATS CARDS COMPONENT ============
const StatsCards = ({ statistics, systemStats, colors, onRefresh, formatExecutionTimeHelper }) => {
  const stats = systemStats || statistics || {};

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div className="p-4 rounded-lg border hover:shadow-lg transition-all" style={{ 
        backgroundColor: colors.card, 
        borderColor: colors.border 
      }}>
        <div className="flex items-center justify-between mb-2">
          <span className="text-sm font-medium" style={{ color: colors.textSecondary }}>Total Requests</span>
          <Activity size={18} style={{ color: colors.info }} />
        </div>
        <div className="text-2xl font-bold" style={{ color: colors.text }}>
          {stats.totalRequests?.toLocaleString() || 0}
        </div>
        <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
          Last 7 days
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
          {stats.successRate ? stats.successRate.toFixed(1) : 0}%
        </div>
        <div className="text-xs mt-1 flex items-center gap-1">
          <span style={{ color: colors.textSecondary }}>vs last period</span>
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
          {formatExecutionTimeHelper(stats.averageResponseTime || 0)}
        </div>
        <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
          Average response time
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
          {stats.failedRequests?.toLocaleString() || 0}
        </div>
        <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
          Failed requests
        </div>
      </div>
    </div>
  );
};

// ============ LOADING OVERLAY COMPONENT ============
const LoadingOverlay = ({ isLoading, loadingType, colors }) => {
  if (!isLoading) return null;
  
  const getLoadingMessage = () => {
    if (loadingType === 'initialLoad') return 'Loading Request Monitor';
    if (loadingType === 'refresh') return 'Refreshing Request Data';
    if (loadingType === 'export') return 'Exporting Requests';
    if (loadingType === 'delete') return 'Deleting Requests';
    return 'Loading...';
  };
  
  const getLoadingDescription = () => {
    if (loadingType === 'initialLoad') return 'Please wait while we load your request data';
    if (loadingType === 'refresh') return 'Fetching the latest request information';
    if (loadingType === 'export') return 'Preparing your export for download';
    if (loadingType === 'delete') return 'Processing your request';
    return 'Please wait...';
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50" style={{ backgroundColor: colors.bg }}>
      <div className="text-center">
        <div className="relative">
          <Loader className="animate-spin mx-auto mb-6" size={64} style={{ color: colors.primary }} />
          <div className="absolute inset-0 flex items-center justify-center">
            <LayoutDashboard size={32} style={{ color: colors.primary, opacity: 0.3 }} />
          </div>
        </div>
        <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>
          {getLoadingMessage()}
        </h3>
        <p className="text-xs mb-2" style={{ color: colors.textSecondary }}>
          {getLoadingDescription()}
        </p>
      </div>
    </div>
  );
};

// ============ MAIN API REQUEST COMPONENT ============
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
  const [filteredApiSummaries, setFilteredApiSummaries] = useState([]);
  const [statistics, setStatistics] = useState(null);
  const [systemStats, setSystemStats] = useState(null);
  
  const [sidebarPage, setSidebarPage] = useState(0);
  const [sidebarItemsPerPage, setSidebarItemsPerPage] = useState(6);
  const [sidebarTotalItems, setSidebarTotalItems] = useState(0);
  
  const [loading, setLoading] = useState({ 
    initialLoad: true, 
    refresh: false, 
    export: false, 
    delete: false,
    statistics: false
  });
  
  const [toast, setToast] = useState(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 11,
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
  const [selectedApiData, setSelectedApiData] = useState(null);
  const [fullApiListLoaded, setFullApiListLoaded] = useState(false);

  const searchTimer = useRef(null);
  const isInitialMount = useRef(true);
  
  // ============ FIX: Add refs for pagination debouncing and request cancellation ============
  const abortControllerRef = useRef(null);
  const paginationDebounceRef = useRef(null);
  const isPaginationLoadingRef = useRef(false);
  const lastPaginationRequestRef = useRef(null);

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

  // Helper functions
  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

   const copyToClipboard = (text) => {
  if (!text) return;
  
  // Modern clipboard API
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(text)
      .then(() => {
        // Optional: show success feedback
        console.log('Copied to clipboard!');
      })
      .catch((err) => {
        console.error('Clipboard write failed:', err);
        fallbackCopy(text);
      });
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
    textarea.focus();
    textarea.select();
    
    try {
      document.execCommand('copy');
    } catch (err) {
      console.error('Fallback copy failed:', err);
    }
    
    document.body.removeChild(textarea);
  }
};

 // Enhanced getStatusText function with more status codes
const getStatusText = (statusCode) => {
  if (!statusCode) return 'PENDING';
  
  const statusMessages = {
    // 1xx Informational
    100: 'Continue',
    101: 'Switching Protocols',
    102: 'Processing',
    103: 'Early Hints',
    
    // 2xx Success
    200: 'OK',
    201: 'Created',
    202: 'Accepted',
    203: 'Non-Authoritative Information',
    204: 'No Content',
    205: 'Reset Content',
    206: 'Partial Content',
    207: 'Multi-Status',
    208: 'Already Reported',
    226: 'IM Used',
    
    // 3xx Redirection
    300: 'Multiple Choices',
    301: 'Moved Permanently',
    302: 'Found',
    303: 'See Other',
    304: 'Not Modified',
    305: 'Use Proxy',
    307: 'Temporary Redirect',
    308: 'Permanent Redirect',
    
    // 4xx Client Errors
    400: 'Bad Request',
    401: 'Unauthorized',
    402: 'Payment Required',
    403: 'Forbidden',
    404: 'Not Found',
    405: 'Method Not Allowed',
    406: 'Not Acceptable',
    407: 'Proxy Authentication Required',
    408: 'Request Timeout',
    409: 'Conflict',
    410: 'Gone',
    411: 'Length Required',
    412: 'Precondition Failed',
    413: 'Payload Too Large',
    414: 'URI Too Long',
    415: 'Unsupported Media Type',
    416: 'Range Not Satisfiable',
    417: 'Expectation Failed',
    418: "I'm a teapot",
    421: 'Misdirected Request',
    422: 'Unprocessable Entity',
    423: 'Locked',
    424: 'Failed Dependency',
    425: 'Too Early',
    426: 'Upgrade Required',
    428: 'Precondition Required',
    429: 'Too Many Requests',
    431: 'Request Header Fields Too Large',
    451: 'Unavailable For Legal Reasons',
    
    // 5xx Server Errors
    500: 'Internal Server Error',
    501: 'Not Implemented',
    502: 'Bad Gateway',
    503: 'Service Unavailable',
    504: 'Gateway Timeout',
    505: 'HTTP Version Not Supported',
    506: 'Variant Also Negotiates',
    507: 'Insufficient Storage',
    508: 'Loop Detected',
    510: 'Not Extended',
    511: 'Network Authentication Required'
  };
  
  if (statusMessages[statusCode]) {
    return statusMessages[statusCode];
  }
  
  if (statusCode >= 200 && statusCode < 300) return 'Success';
  if (statusCode >= 300 && statusCode < 400) return 'Redirect';
  if (statusCode >= 400 && statusCode < 500) return 'Client Error';
  if (statusCode >= 500) return 'Server Error';
  
  return 'Unknown';
};

 const getMethodColor = useCallback((method) => {
  return colors.method[method] || colors.textSecondary;
}, [colors.method, colors.textSecondary]);

  const formatTimestamp = (timestamp) => {
    if (!timestamp) return '';
    try {
      const date = new Date(timestamp);
      return date.toLocaleString();
    } catch (e) {
      return timestamp;
    }
  };
  
  const formatExecutionTimeHelper = (ms) => {
  if (ms === null || ms === undefined) return 'N/A';

  const time = Number(ms); // 🔥 force conversion

  if (isNaN(time)) return 'N/A';

  if (time < 1000) return `${time.toFixed(2)}ms`;
  if (time < 60000) return `${(time / 1000).toFixed(2)}s`;
  return `${(time / 60000).toFixed(2)}m`;
};

 const getStatusCodeColorHelper = useCallback((code) => {
  if (!code) return colors.textSecondary;
  if (code >= 200 && code < 300) return colors.success;
  if (code >= 300 && code < 400) return colors.info;
  if (code >= 400 && code < 500) return colors.warning;
  if (code >= 500) return colors.error;
  return colors.textSecondary;
}, [colors.success, colors.info, colors.warning, colors.error, colors.textSecondary]);



  // ============ FIX: Add function to cancel ongoing requests ============
  const cancelOngoingRequest = useCallback(() => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
      abortControllerRef.current = null;
    }
  }, []);

  // ============ FIX: Debounced pagination handler ============
  const handlePaginationChange = useCallback((newPage) => {
    // Clear any pending pagination request
    if (paginationDebounceRef.current) {
      clearTimeout(paginationDebounceRef.current);
    }
    
    // Don't allow rapid consecutive pagination requests
    const now = Date.now();
    if (lastPaginationRequestRef.current && (now - lastPaginationRequestRef.current) < 500) {
      // Too fast, debounce
      paginationDebounceRef.current = setTimeout(() => {
        if (!isPaginationLoadingRef.current) {
          setPagination(prev => ({ ...prev, page: newPage }));
          lastPaginationRequestRef.current = Date.now();
        }
        paginationDebounceRef.current = null;
      }, 300);
      return;
    }
    
    // Update immediately if not loading
    if (!isPaginationLoadingRef.current) {
      setPagination(prev => ({ ...prev, page: newPage }));
      lastPaginationRequestRef.current = now;
    }
  }, []);

  // Add this useEffect to debug date range changes
  useEffect(() => {
    console.log('Date range changed:', {
      fromDate: dateRange.fromDate,
      toDate: dateRange.toDate
    });
  }, [dateRange.fromDate, dateRange.toDate]);

  // Expand date range function
  const expandDateRange = (days) => {
    const now = new Date();
    const fromDate = new Date(Date.now() - days * 24 * 60 * 60 * 1000);
    
    const formatDateForInput = (date) => {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day}T${hours}:${minutes}`;
    };
    
    const formattedFromDate = formatDateForInput(fromDate);
    const formattedToDate = formatDateForInput(now);
    
    console.log('Expanding date range to', days, 'days:', formattedFromDate, 'to', formattedToDate);
    
    setDateRange({
      fromDate: formattedFromDate,
      toDate: formattedToDate
    });
    
    setPagination(prev => ({ ...prev, page: 0 }));
    
    setTimeout(() => {
      loadRequests(true);
    }, 100);
  };

  // Load full API list from the server
  const loadFullApiList = useCallback(async () => {
    if (!authToken) return;

    try {
      const filter = {
        page: 0,
        size: 100,
        fromDate: dateRange.fromDate,
        toDate: dateRange.toDate
      };

      const response = await searchRequests(authToken, filter);
      
      if (response?.responseCode === 200) {
        let apiList = response.data?.apiSummaries || [];
        
        apiList = apiList.sort((a, b) => {
          const timeA = a.lastRequestTime ? new Date(a.lastRequestTime).getTime() : 0;
          const timeB = b.lastRequestTime ? new Date(b.lastRequestTime).getTime() : 0;
          return timeB - timeA;
        });
        
        setApiSummaries(apiList);
        setSidebarTotalItems(apiList.length);
        setFullApiListLoaded(true);
        console.log('Full API list loaded and sorted by last request time:', apiList.length, 'APIs');
      }
    } catch (error) {
      console.error('Error loading full API list:', error);
    }
  }, [authToken, dateRange.fromDate, dateRange.toDate]);

  // ============ FIX: Updated loadRequests with abort signal support ============
  const loadRequests = useCallback(async (isRefresh = false) => {
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }

    // Cancel any ongoing request
    cancelOngoingRequest();
    
    // Set loading flag to prevent multiple simultaneous requests
    isPaginationLoadingRef.current = true;
    
    // Create new abort controller
    abortControllerRef.current = new AbortController();

    if (isRefresh) {
      setLoading(prev => ({ ...prev, refresh: true }));
    }

    try {
      const filter = {
        page: pagination.page,
        size: pagination.size,
        ...(selectedApiId && { apiId: selectedApiId }),
        ...(filters.requestStatus && { requestStatus: filters.requestStatus }),
        ...(filters.httpMethod && { httpMethod: filters.httpMethod }),
        ...(filters.responseStatusCode && { responseStatusCode: filters.responseStatusCode }),
        ...(filters.hasError && { hasError: filters.hasError }),
        sort: 'createdAt,desc',
        sortBy: 'createdAt',
        sortDirection: 'DESC'
      };

      if (dateRange.fromDate) {
        try {
          const fromDateObj = new Date(dateRange.fromDate);
          if (!isNaN(fromDateObj.getTime())) {
            filter.fromDate = fromDateObj.toISOString();
          }
        } catch (e) {
          console.error('Invalid fromDate:', dateRange.fromDate);
        }
      }
      
      if (dateRange.toDate) {
        try {
          const toDateObj = new Date(dateRange.toDate);
          if (!isNaN(toDateObj.getTime())) {
            toDateObj.setHours(23, 59, 59, 999);
            filter.toDate = toDateObj.toISOString();
          }
        } catch (e) {
          console.error('Invalid toDate:', dateRange.toDate);
        }
      }

      if (searchQuery && searchQuery.trim()) {
        filter.search = searchQuery.trim();
      }

      Object.keys(filter).forEach(key => filter[key] === undefined && delete filter[key]);

      console.log('Loading requests with params:', filter);

      // Pass abort signal to the API call if your API function supports it
      const response = await searchRequests(authToken, filter);
      
      // Check if this request was aborted
      if (abortControllerRef.current?.signal.aborted) {
        console.log('Request was aborted, ignoring response');
        return;
      }
      
      if (response?.responseCode === 200) {
        const responseData = response.data;
        
        let requestsArray = [];
        let totalElements = 0;
        let currentPage = 0;
        let totalPages = 0;
        let apiSummariesArray = [];
        
        if (responseData.content && Array.isArray(responseData.content)) {
          requestsArray = responseData.content;
          totalElements = responseData.totalElements || 0;
          currentPage = responseData.currentPage || 0;
          totalPages = responseData.totalPages || 0;
          apiSummariesArray = responseData.apiSummaries || [];
        } else if (responseData.requests && Array.isArray(responseData.requests)) {
          requestsArray = responseData.requests;
          totalElements = responseData.total || 0;
          currentPage = responseData.page || 0;
          totalPages = responseData.pages || 0;
          apiSummariesArray = responseData.apiSummaries || [];
        } else if (Array.isArray(responseData)) {
          requestsArray = responseData;
          totalElements = requestsArray.length;
          currentPage = 0;
          totalPages = 1;
        } else if (responseData.data && Array.isArray(responseData.data)) {
          requestsArray = responseData.data;
          totalElements = responseData.total || requestsArray.length;
          currentPage = responseData.page || 0;
          totalPages = responseData.pages || 1;
        } else {
          console.error('Unknown response structure:', Object.keys(responseData));
        }
        
        const sortedRequests = [...requestsArray].sort((a, b) => {
          const timeA = new Date(a.requestTimestamp || a.createdAt || 0).getTime();
          const timeB = new Date(b.requestTimestamp || b.createdAt || 0).getTime();
          return timeB - timeA;
        });
        
        console.log('Extracted requests:', sortedRequests.length, 'requests');
        
        setRequests(sortedRequests);
        
        if (apiSummariesArray.length > 0) {
          const sortedApiSummaries = [...apiSummariesArray].sort((a, b) => {
            const timeA = a.lastRequestTime ? new Date(a.lastRequestTime).getTime() : 0;
            const timeB = b.lastRequestTime ? new Date(b.lastRequestTime).getTime() : 0;
            return timeB - timeA;
          });
          setFilteredApiSummaries(sortedApiSummaries);
          if (!searchQuery && !selectedApiId && Object.keys(filters).length === 0) {
            setApiSummaries(sortedApiSummaries);
            setSidebarTotalItems(sortedApiSummaries.length);
          }
        }
        
        setPagination(prev => ({
          ...prev,
          page: currentPage,
          totalElements: totalElements,
          totalPages: totalPages
        }));

        if (sortedRequests.length === 0) {
          if (searchQuery && searchQuery.trim()) {
            showToast(`No requests found matching "${searchQuery}"`, 'info');
          } else if (selectedApiId) {
            const selectedApi = apiSummaries.find(api => api.apiId === selectedApiId);
            if (selectedApi && selectedApi.totalRequests > 0) {
              showToast(`No requests found for ${selectedApi.apiName} in the selected date range. Try expanding the date range.`, 'info');
            }
          } else if (Object.keys(filters).length > 0) {
            showToast('No requests match the selected filters', 'info');
          } else {
            showToast(`No requests found in the selected date range`, 'info');
          }
        } else {
          if (searchQuery && searchQuery.trim()) {
            showToast(`Found ${sortedRequests.length} requests matching "${searchQuery}"`, 'success');
          }
        }
      } else {
        console.error('Failed to load requests:', response?.message);
        showToast(response?.message || 'Failed to load requests', 'error');
      }
    } catch (error) {
      // Don't show error for aborted requests
      if (error.name === 'AbortError' || abortControllerRef.current?.signal.aborted) {
        console.log('Request was aborted');
        return;
      }
      console.error('Error loading requests:', error);
      showToast(error.message || 'Failed to load requests', 'error');
    } finally {
      isPaginationLoadingRef.current = false;
      if (isRefresh) {
        setLoading(prev => ({ ...prev, refresh: false }));
      }
      // Clear abort controller
      if (abortControllerRef.current?.signal.aborted === false) {
        abortControllerRef.current = null;
      }
    }
  }, [authToken, filters, pagination.page, pagination.size, dateRange.fromDate, dateRange.toDate, searchQuery, selectedApiId, apiSummaries, cancelOngoingRequest]);

  const handleFilterApply = (newFilters) => {
    setFilters(newFilters);
    setPagination(prev => ({ ...prev, page: 0 }));
    setTimeout(() => {
      loadRequests(true);
    }, 100);
  };

  // Load statistics
  const loadStatistics = useCallback(async () => {
    if (!authToken) return;

    setLoading(prev => ({ ...prev, statistics: true }));

    try {
      const systemResponse = await getSystemStatistics(
        authToken,
        dateRange.fromDate,
        dateRange.toDate
      );
      
      if (systemResponse?.responseCode === 200) {
        setSystemStats(systemResponse.data);
      }

      const dashboardResponse = await getRequestDashboardStats(authToken);
      
      if (dashboardResponse?.responseCode === 200) {
        setStatistics(dashboardResponse.data);
      }
    } catch (error) {
      console.error('Error loading statistics:', error);
    } finally {
      setLoading(prev => ({ ...prev, statistics: false }));
    }
  }, [authToken, dateRange.fromDate, dateRange.toDate]);

  // Load recent requests
  const loadRecentRequests = useCallback(async () => {
    if (!authToken) return;

    try {
      const response = await getRecentRequests(authToken, 10);
      if (response?.responseCode === 200) {
        setRequests(response.data || []);
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

    setLoading(prev => ({ ...prev, export: true }));

    try {
      const apiIdForExport = selectedApiId || 'all';
      
      const response = await exportRequests(
        authToken,
        apiIdForExport,
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
      setLoading(prev => ({ ...prev, export: false }));
    }
  };

  // Handle delete request
  const handleDeleteRequest = async (requestId) => {
    if (!authToken || !requestId) return;

    if (!window.confirm('Are you sure you want to delete this request?')) {
      return;
    }

    setLoading(prev => ({ ...prev, delete: true }));

    try {
      const response = await deleteRequest(authToken, requestId);
      
      if (response?.responseCode === 200) {
        showToast('Request deleted successfully', 'success');
        loadRequests(true);
      } else {
        showToast(response?.message || 'Failed to delete request', 'error');
      }
    } catch (error) {
      console.error('Error deleting request:', error);
      showToast(error.message || 'Failed to delete request', 'error');
    } finally {
      setLoading(prev => ({ ...prev, delete: false }));
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
    console.log('Selecting API:', apiId);
    
    if (apiId !== selectedApiId) {
      setRequests([]);
    }
    
    setSelectedApiId(apiId);
    setPagination(prev => ({ ...prev, page: 0 }));
    
    if (apiId && apiSummaries.length > 0) {
      const summary = apiSummaries.find(api => api.apiId === apiId || api.apiCode === apiId);
      setSelectedApiSummary(summary || null);
      setSelectedApiData(summary || null);
      console.log('Found summary for selected API:', summary);
      
      if (summary && summary.totalRequests === 0) {
        showToast(`No requests found for ${summary.apiName} at all.`, 'warning');
      } else if (summary && summary.totalRequests > 0) {
        showToast(`Loading ${summary.totalRequests} requests for ${summary.apiName}...`, 'info');
      }
    } else if (!apiId) {
      setSelectedApiSummary(null);
      setSelectedApiData(null);
      showToast('Loading all requests...', 'info');
    }
  };

  // Handle sidebar pagination navigation
  const handleSidebarPrevPage = () => {
    if (sidebarPage > 0) {
      setSidebarPage(sidebarPage - 1);
    }
  };

  const handleSidebarNextPage = () => {
    const maxPage = Math.max(0, Math.ceil(sidebarTotalItems / sidebarItemsPerPage) - 1);
    if (sidebarPage < maxPage) {
      setSidebarPage(sidebarPage + 1);
    }
  };

  // Get paginated API summaries for sidebar
  const getPaginatedApiSummaries = () => {
    const startIndex = sidebarPage * sidebarItemsPerPage;
    const endIndex = startIndex + sidebarItemsPerPage;
    return apiSummaries.slice(startIndex, endIndex);
  };

  // Handle search with debounce
  const handleSearchChange = (e) => {
    const query = e.target.value;
    setSearchQuery(query);
    setPagination(prev => ({ ...prev, page: 0 }));
    
    if (searchTimer.current) {
      clearTimeout(searchTimer.current);
    }
    
    if (!query || query.trim() === '') {
      console.log('Search cleared, loading all requests');
      loadRequests(true);
      return;
    }
    
    searchTimer.current = setTimeout(() => {
      if (activeTab === 'all') {
        console.log('Executing search with query:', query);
        loadRequests(true);
      }
    }, 500);
  };

  // Handle refresh button click
  const handleRefresh = useCallback(() => {
    if (activeTab === 'statistics') {
      loadStatistics();
    } else if (activeTab === 'all') {
      loadRequests(true);
    } else if (activeTab === 'recent') {
      loadRecentRequests();
    }
  }, [activeTab, loadStatistics, loadRequests, loadRecentRequests]);

  // ============ FIX: Clean up on unmount ============
  useEffect(() => {
    return () => {
      // Cancel any ongoing request
      cancelOngoingRequest();
      // Clear any pending timers
      if (searchTimer.current) {
        clearTimeout(searchTimer.current);
      }
      if (paginationDebounceRef.current) {
        clearTimeout(paginationDebounceRef.current);
      }
    };
  }, [cancelOngoingRequest]);

  // Initial load
  useEffect(() => {
    if (authToken && isInitialMount.current) {
      isInitialMount.current = false;
      console.log('Initial load');
      loadStatistics();
      loadFullApiList();
      
      if (activeTab === 'all') {
        loadRequests().finally(() => {
          setLoading(prev => ({ ...prev, initialLoad: false }));
        });
      } else if (activeTab === 'recent') {
        loadRecentRequests().finally(() => {
          setLoading(prev => ({ ...prev, initialLoad: false }));
        });
      } else {
        setLoading(prev => ({ ...prev, initialLoad: false }));
      }
    }

    return () => {
      if (searchTimer.current) {
        clearTimeout(searchTimer.current);
      }
    };
  }, []);

  // Handle tab changes
  useEffect(() => {
    if (!authToken || isInitialMount.current) return;

    console.log('Tab changed to:', activeTab);
    
    if (activeTab === 'statistics') {
      loadStatistics();
    } else if (activeTab === 'all') {
      loadRequests();
    } else if (activeTab === 'recent') {
      loadRecentRequests();
    }
  }, [activeTab]);

  // Handle filter changes
  useEffect(() => {
    if (!authToken || isInitialMount.current || activeTab !== 'all') return;
    
    console.log('Filters changed, reloading...', { 
      selectedApiId, 
      paginationPage: pagination.page,
      dateRange,
      searchQuery
    });
    
    loadRequests();
  }, [filters, pagination.page, pagination.size, dateRange.fromDate, dateRange.toDate, searchQuery, selectedApiId]);

  // Update selected API summary when apiSummaries change
  useEffect(() => {
    if (selectedApiId && apiSummaries.length > 0) {
      const summary = apiSummaries.find(api => api.apiId === selectedApiId || api.apiCode === selectedApiId);
      if (summary && (!selectedApiData || selectedApiData.apiId !== summary.apiId)) {
        console.log('Updating selected API data from apiSummaries:', summary);
        setSelectedApiSummary(summary);
        setSelectedApiData(summary);
      }
    }
  }, [selectedApiId, apiSummaries]);

  // Render sidebar
  const renderSidebar = () => {
    const totalRequests = apiSummaries.reduce((sum, api) => sum + (api.totalRequests || 0), 0);
    
    const paginatedApis = getPaginatedApiSummaries();
    const totalPages = Math.max(1, Math.ceil(sidebarTotalItems / sidebarItemsPerPage));
    const startIndex = sidebarPage * sidebarItemsPerPage;
    const endIndex = Math.min(startIndex + sidebarItemsPerPage, sidebarTotalItems);

    return (
      <div 
        className="h-full border-r transition-all duration-300 flex flex-col"
        style={{ 
          width: sidebarCollapsed ? '60px' : '350px',
          backgroundColor: colors.sidebar,
          borderColor: colors.border
        }}
      >
        <div className="flex items-center justify-between p-3 border-b" style={{ borderColor: colors.border }}>
          {!sidebarCollapsed && (
            <span className="text-sm font-semibold uppercase" style={{ color: colors.textSecondary }}>API Request Monitor</span>
          )}
          <button
            // onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
            onClick={() => {}}
            className="p-1 rounded hover:bg-opacity-50 transition-colors ml-auto"
            style={{ backgroundColor: colors.hover }}
          >
            <Sidebar size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        <div className="flex-1 overflow-auto p-2">
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

          {apiSummaries.length > 0 ? (
            <>
              <div className="mt-4 mb-2">
                {!sidebarCollapsed && (
                  <div className="flex items-center justify-between px-1 mb-2">
                    <span className="text-xs font-medium" style={{ color: colors.textSecondary }}>
                      APIs ({sidebarTotalItems})
                    </span>
                    {totalPages > 1 && (
                      <span className="text-xs" style={{ color: colors.textTertiary }}>
                        {startIndex + 1}-{endIndex} of {sidebarTotalItems}
                      </span>
                    )}
                  </div>
                )}
                
                {paginatedApis.map(api => (
                  <div
                    key={api.apiId || api.apiCode}
                    onClick={() => handleApiSelect(api.apiId || api.apiCode)}
                    className={`flex items-start gap-2 p-2 rounded cursor-pointer transition-colors mb-2 ${
                      selectedApiId === (api.apiId || api.apiCode) ? 'ring-1' : ''
                    }`}
                    style={{ 
                      backgroundColor: selectedApiId === (api.apiId || api.apiCode) ? colors.selected : 'transparent',
                      color: selectedApiId === (api.apiId || api.apiCode) ? colors.primary : colors.text,
                      borderColor: selectedApiId === (api.apiId || api.apiCode) ? colors.primary : 'transparent'
                    }}
                  >
                    <DatabaseIcon size={14} style={{ color: selectedApiId === (api.apiId || api.apiCode) ? colors.primary : colors.textSecondary }} className="mt-1" />
                    {!sidebarCollapsed && (
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <span className="text-sm font-medium truncate" title={api.apiName}>
                            {api.apiName}
                          </span>
                          <span className="text-xs px-1.5 py-0.5 rounded ml-2" style={{ backgroundColor: colors.hover }}>
                            {api.totalRequests || 0}
                          </span>
                        </div>
                        
                        <div className="text-xs mt-1 font-mono" style={{ color: colors.textTertiary }}>
                          {api.apiCode}
                        </div>
                        
                        <div className="flex items-center gap-2 mt-1">
                          <span className="text-xs" style={{ color: colors.success }}>✓ {api.successCount || 0}</span>
                          <span className="text-xs" style={{ color: colors.error }}>✗ {api.failedCount || 0}</span>
                          <span className="text-xs" style={{ color: colors.warning }}>
                            {(api.successRate || 0).toFixed(1)}%
                          </span>
                        </div>
                        
                        {api.lastRequestTime && (
                          <div className="flex items-center gap-1 mt-1 text-xs" style={{ color: colors.textTertiary }}>
                            <Clock size={10} />
                            <span className="truncate">{new Date(api.lastRequestTime).toLocaleString()}</span>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                ))}
              </div>
              
              {!sidebarCollapsed && totalPages > 1 && (
                <div className="flex items-center justify-between px-2 py-2 mt-2 border-t" style={{ borderColor: colors.border }}>
                  <button
                    onClick={handleSidebarPrevPage}
                    disabled={sidebarPage === 0}
                    className="p-1.5 rounded hover:bg-opacity-50 transition-colors disabled:opacity-50"
                    style={{ backgroundColor: colors.hover }}
                  >
                    <ChevronLeft size={14} style={{ color: colors.textSecondary }} />
                  </button>
                  <span className="text-xs" style={{ color: colors.textSecondary }}>
                    Page {sidebarPage + 1} of {totalPages}
                  </span>
                  <button
                    onClick={handleSidebarNextPage}
                    disabled={sidebarPage >= totalPages - 1}
                    className="p-1.5 rounded hover:bg-opacity-50 transition-colors disabled:opacity-50"
                    style={{ backgroundColor: colors.hover }}
                  >
                    <ChevronRight size={14} style={{ color: colors.textSecondary }} />
                  </button>
                </div>
              )}
            </>
          ) : (
            !sidebarCollapsed && (
              <div className="text-center p-4" style={{ color: colors.textSecondary }}>
                <Loader size={20} className="animate-spin mx-auto mb-2" />
                <p className="text-xs">Loading APIs...</p>
              </div>
            )
          )}
        </div>
      </div>
    );
  };

  // Add this useEffect right after your state declarations
  useEffect(() => {
    console.log('🔄 Requests state updated:', requests.length, 'requests');
    console.log('First request:', requests[0]);
  }, [requests]);

  // Render requests table - FIXED VERSION with consistent colors
const renderRequestsTable = () => {
  console.log('Rendering table with requests:', requests.length, 'requests');
  console.log('Loading states:', loading);
  
  return (
    <div className="flex-1 flex flex-col overflow-hidden pl-2 pr-2">
      <div className="flex-1 overflow-auto p-4">
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
              const statusCode = request.responseStatusCode;
              // Use the same color function for both
              const statusColor = getStatusCodeColorHelper(statusCode);
              const statusText = getStatusText(statusCode);
              
              return (
                <tr 
                  key={`${request.id || request.requestId || index}`}
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
                      <span className={`status-badge ${
                        !statusCode ? 'status-badge-default' :
                        statusCode >= 200 && statusCode < 300 ? 'status-badge-success' :
                        statusCode >= 300 && statusCode < 400 ? 'status-badge-info' :
                        statusCode >= 400 && statusCode < 500 ? 'status-badge-warning' :
                        statusCode >= 500 ? 'status-badge-error' : 'status-badge-default'
                      }`}>
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
                    <span className="text-xs" style={{ color: colors.textSecondary }}>{request.apiCode || request.apiName || 'N/A'}</span>
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
                      {formatTimestamp(request.requestTimestamp || request.createdAt)}
                    </span>
                  </td>
                  <td className="py-3 px-4">
                    <span className="text-xs font-mono" style={{ color: colors.textSecondary }}>
                      {request.correlationId?.substring(0, 8) || 'N/A'}...
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
                          copyToClipboard(getFullJsonData(), 'JSON');
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
            
            {/* Empty state */}
            {requests.length === 0 && !loading.initialLoad && !loading.refresh && !loading.export && (
              <tr>
                <td colSpan="10" className="text-center py-12">
                  <FileText size={48} className="mx-auto mb-4 opacity-50" style={{ color: colors.textSecondary }} />
                  <p className="text-lg mb-2" style={{ color: colors.text }}>No Requests Found</p>
                  <p className="text-sm" style={{ color: colors.textSecondary }}>
                    {searchQuery ? `No requests matching "${searchQuery}"` : 'Try adjusting your filters or date range'}
                  </p>
                </td>
              </tr>
            )}

            {/* Loading state */}
            {(loading.refresh || loading.initialLoad) && requests.length === 0 && (
              <tr>
                <td colSpan="10" className="text-center py-12">
                  <RefreshCw size={32} className="animate-spin mx-auto mb-4" style={{ color: colors.textSecondary }} />
                  <p className="text-sm" style={{ color: colors.textSecondary }}>Loading requests...</p>
                </td>
              </tr>
            )}
          </tbody>
        </table>

        {/* Pagination - Always show if there are requests or totalElements > 0 */}
        {pagination.totalPages > 0 && (
          <div className="flex items-center justify-between mt-4 py-3 px-4" style={{ backgroundColor: colors.card, borderTop: `1px solid ${colors.border}` }}>
            <div className="text-sm" style={{ color: colors.textSecondary }}>
              Showing {requests.length > 0 ? pagination.page * pagination.size + 1 : 0} - {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of {pagination.totalElements}
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={() => {
                  if (pagination.page > 0) {
                    handlePaginationChange(pagination.page - 1);
                  }
                }}
                disabled={pagination.page === 0 || isPaginationLoadingRef.current}
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
                  if (pagination.page < pagination.totalPages - 1) {
                    handlePaginationChange(pagination.page + 1);
                  }
                }}
                disabled={pagination.page >= pagination.totalPages - 1 || isPaginationLoadingRef.current}
                className="px-3 py-1 rounded text-sm font-medium hover:bg-opacity-50 transition-colors disabled:opacity-50 flex items-center gap-1"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                Next
                <ChevronRight size={14} />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

  const showFullOverlay = loading.initialLoad || loading.export || loading.delete;
  const loadingType = loading.initialLoad ? 'initialLoad' : 
                     loading.export ? 'export' : 
                     loading.delete ? 'delete' : 
                     loading.refresh ? 'refresh' : '';

  return (
    <div className="flex h-screen overflow-hidden" style={{ 
      backgroundColor: colors.bg,
      color: colors.text,
      fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif',
      fontSize: '13px'
    }}>
      <style key={`theme-styles-${isDark}`}>{`
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

        .status-badge {
          display: inline-block;
          padding: 0.125rem 0.5rem;
          border-radius: 0.25rem;
          font-size: 0.75rem;
          font-weight: 500;
          transition: all 0.2s ease;
        }
        
        .status-badge-success { 
          background-color: ${isDark ? 'rgba(52, 211, 153, 0.12)' : '#10b98120'} !important; 
          color: ${colors.success} !important;
        }
        
        .status-badge-info { 
          background-color: ${isDark ? 'rgba(96, 165, 250, 0.12)' : '#3b82f620'} !important; 
          color: ${colors.info} !important;
        }
        
        .status-badge-warning { 
          background-color: ${isDark ? 'rgba(251, 191, 36, 0.12)' : '#f59e0b20'} !important; 
          color: ${colors.warning} !important;
        }
        
        .status-badge-error { 
          background-color: ${isDark ? 'rgba(248, 113, 113, 0.12)' : '#ef444420'} !important; 
          color: ${colors.error} !important;
        }
        
        .status-badge-default { 
          background-color: ${colors.hover} !important; 
          color: ${colors.textSecondary} !important;
        }
      `}</style>

      <LoadingOverlay 
        isLoading={showFullOverlay} 
        loadingType={loadingType} 
        colors={colors} 
      />

      {renderSidebar()}

      <div className="flex-1 flex flex-col overflow-hidden">
        
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
            <div className="relative">
              <Search size={14} className="absolute left-2 top-1/2 transform -translate-y-1/2" style={{ color: colors.textSecondary }} />
              <input
                type="text"
                placeholder="Search by request name, correlation ID, or URL..."
                value={searchQuery}
                onChange={handleSearchChange}
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    if (searchTimer.current) {
                      clearTimeout(searchTimer.current);
                    }
                    loadRequests(true);
                  }
                }}
                className="pl-8 pr-3 py-1.5 rounded text-sm w-64"
                style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
              />
            </div>

            <button 
              onClick={() => setShowFilterModal(true)}
              className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}
              disabled={loading.initialLoad || loading.refresh}
            >
              <Filter size={14} style={{ color: colors.textSecondary }} />
            </button>

            <button 
              onClick={() => setShowExportModal(true)}
              className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}
              disabled={loading.initialLoad || loading.refresh}
            >
              <DownloadCloud size={14} style={{ color: colors.textSecondary }} />
            </button>

            <button 
              className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
              onClick={handleRefresh}
              disabled={loading.initialLoad || loading.refresh}
              style={{ backgroundColor: colors.hover }}
            >
              <RefreshCw size={14} className={loading.refresh ? 'animate-spin' : ''} style={{ color: colors.textSecondary }} />
            </button>
          </div>
        </div>

        <div className="w-full p-4 mt-2 -mb-4 space-y-6">
          <StatsCards 
            statistics={statistics}
            systemStats={systemStats}
            colors={colors}
            onRefresh={loadStatistics}
            formatExecutionTimeHelper={formatExecutionTimeHelper}
          />
        </div>

        <div className="flex items-center gap-4 px-4 py-2 border-b pl-2 pr-2" style={{ 
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
            onChange={(e) => {
              const newFromDate = e.target.value;
              setDateRange(prev => ({ ...prev, fromDate: newFromDate }));
            }}
            className="px-2 py-1 rounded text-sm"
            style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
            disabled={loading.initialLoad || loading.refresh}
          />
          <span style={{ color: colors.textSecondary }}>to</span>
          <input
            type="datetime-local"
            value={dateRange.toDate}
            onChange={(e) => {
              const newToDate = e.target.value;
              setDateRange(prev => ({ ...prev, toDate: newToDate }));
            }}
            className="px-2 py-1 rounded text-sm"
            style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
            disabled={loading.initialLoad || loading.refresh}
          />
          <button
            onClick={async () => {
              console.log('Apply button clicked - current date range:', dateRange);
              setPagination(prev => ({ ...prev, page: 0 }));
              setTimeout(() => {
                loadRequests(true);
              }, 100);
            }}
            className="px-3 py-1 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.primaryDark, color: 'white' }}
            disabled={loading.initialLoad || loading.refresh}
          >
            Apply
          </button>
        </div>

        {renderRequestsTable()}

      </div>

      <RequestDetailsModal
        request={selectedRequest}
        colors={colors}
        isOpen={showDetailsModal}
        onClose={() => setShowDetailsModal(false)}
        onRefresh={loadRequests}
        getStatusText={getStatusText}  // Add this line
        getStatusCodeColorHelper={getStatusCodeColorHelper}
      />

      <FilterModal
        filters={filters}
        colors={colors}
        isOpen={showFilterModal}
        onClose={() => setShowFilterModal(false)}
        onApply={handleFilterApply}
        onExpandDateRange={expandDateRange}
      />

      <ExportModal
        colors={colors}
        isOpen={showExportModal}
        onClose={() => setShowExportModal(false)}
        onExport={handleExport}
      />

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