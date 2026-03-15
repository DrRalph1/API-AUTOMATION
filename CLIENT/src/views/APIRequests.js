import React, { useState, useEffect, useCallback, useRef } from 'react';
import { 
  ChevronRight, 
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
  Minimize2
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
  if (!code) return <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed">// No data available</pre>;
  
  const highlightCode = (code, lang) => {
    const lines = String(code).split('\n');
    
    return lines.map((line, lineIndex) => {
      let highlightedLine = line
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
      
      if (lang === 'json') {
        // Highlight JSON
        highlightedLine = highlightedLine.replace(/"([^"\\]*(\\.[^"\\]*)*)"/g, 
          '<span class="text-green-400">"$1"</span>');
        
        highlightedLine = highlightedLine.replace(/\b(true|false)\b/g, 
          '<span class="text-blue-400">$1</span>');
        
        highlightedLine = highlightedLine.replace(/\b(null)\b/g, 
          '<span class="text-gray-500">$1</span>');
        
        highlightedLine = highlightedLine.replace(/\b(\d+)\b/g, 
          '<span class="text-orange-400">$1</span>');
        
        // Highlight keys
        highlightedLine = highlightedLine.replace(/([a-zA-Z_][a-zA-Z0-9_]*)\s*:/g, 
          '<span class="text-purple-400">$1</span>:');
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

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50">
      <div className="absolute inset-0 bg-black bg-opacity-50" onClick={onClose} />
      <div className="relative w-3/4 max-w-4xl max-h-[80vh] rounded-lg overflow-hidden" style={{ 
        backgroundColor: colors.modalBg,
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
                backgroundColor: getStatusCodeColor(request.responseStatusCode, colors),
                color: 'white'
              }}>
                {request.responseStatusCode || 'Pending'}
              </span>
              <span className="text-xs px-2 py-1 rounded" style={{ 
                backgroundColor: `${getRequestStatusColor(request.requestStatus, colors)}20`,
                color: getRequestStatusColor(request.requestStatus, colors)
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
          {['request', 'response', 'headers', 'timeline'].map(tab => (
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
                    <div className="text-sm font-medium" style={{ color: colors.text }}>{request.requestName}</div>
                  </div>
                  <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Correlation ID</div>
                    <div className="text-sm font-mono" style={{ color: colors.text }}>{request.correlationId}</div>
                  </div>
                  <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>HTTP Method</div>
                    <div className="text-sm font-medium" style={{ color: colors.text }}>{request.httpMethod}</div>
                  </div>
                  <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>URL</div>
                    <div className="text-sm font-mono truncate" style={{ color: colors.text }}>{request.url}</div>
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
                        <span className="text-xs font-mono" style={{ color: colors.text }}>{JSON.stringify(value)}</span>
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
                        <span className="text-xs font-mono" style={{ color: colors.text }}>{JSON.stringify(value)}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Request Body */}
              {request.requestBody && (
                <div>
                  <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Request Body</h3>
                  <div className="p-3 rounded" style={{ backgroundColor: colors.codeBg }}>
                    <SyntaxHighlighter 
                      language="json"
                      code={JSON.stringify(request.requestBody, null, 2)}
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
                  <div className="p-3 rounded" style={{ backgroundColor: colors.codeBg }}>
                    <SyntaxHighlighter 
                      language="json"
                      code={JSON.stringify(request.responseBody, null, 2)}
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
                  <div className="space-y-2">
                    {Object.entries(request.headers).map(([key, value]) => (
                      <div key={key} className="flex items-start gap-2 p-2 rounded" style={{ backgroundColor: colors.hover }}>
                        <span className="text-xs font-medium min-w-[150px]" style={{ color: colors.textSecondary }}>{key}:</span>
                        <span className="text-xs font-mono" style={{ color: colors.text }}>{value}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Response Headers */}
              {request.responseHeaders && Object.keys(request.responseHeaders).length > 0 && (
                <div>
                  <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Response Headers</h3>
                  <div className="space-y-2">
                    {Object.entries(request.responseHeaders).map(([key, value]) => (
                      <div key={key} className="flex items-start gap-2 p-2 rounded" style={{ backgroundColor: colors.hover }}>
                        <span className="text-xs font-medium min-w-[150px]" style={{ color: colors.textSecondary }}>{key}:</span>
                        <span className="text-xs font-mono" style={{ color: colors.text }}>{value}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {activeTab === 'timeline' && (
            <div className="space-y-4">
              {/* Timeline */}
              <div className="relative pl-8 space-y-4">
                {/* Request Created */}
                <div className="relative">
                  <div className="absolute left-[-24px] top-0 w-2 h-2 rounded-full" style={{ backgroundColor: colors.success }} />
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Request Created</div>
                  <div className="text-sm" style={{ color: colors.text }}>{formatRequestTimestamp(request.createdAt)}</div>
                </div>

                {/* Request Sent */}
                {request.requestTimestamp && (
                  <div className="relative">
                    <div className="absolute left-[-24px] top-0 w-2 h-2 rounded-full" style={{ backgroundColor: colors.info }} />
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Request Sent</div>
                    <div className="text-sm" style={{ color: colors.text }}>{formatRequestTimestamp(request.requestTimestamp)}</div>
                  </div>
                )}

                {/* Response Received */}
                {request.responseTimestamp && (
                  <div className="relative">
                    <div className="absolute left-[-24px] top-0 w-2 h-2 rounded-full" style={{ backgroundColor: colors.primary }} />
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Response Received</div>
                    <div className="text-sm" style={{ color: colors.text }}>{formatRequestTimestamp(request.responseTimestamp)}</div>
                  </div>
                )}
              </div>

              {/* Duration */}
              {request.executionDurationMs && (
                <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
                  <div className="flex items-center justify-between">
                    <span className="text-sm" style={{ color: colors.textSecondary }}>Total Duration:</span>
                    <span className="text-lg font-semibold" style={{ color: colors.text }}>
                      {formatExecutionTime(request.executionDurationMs)}
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
  const [localFilters, setLocalFilters] = useState(filters);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50">
      <div className="absolute inset-0 bg-black bg-opacity-50" onClick={onClose} />
      <div className="relative w-96 rounded-lg overflow-hidden" style={{ 
        backgroundColor: colors.modalBg,
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
              onChange={(e) => setLocalFilters({ ...localFilters, requestStatus: e.target.value })}
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
              onChange={(e) => setLocalFilters({ ...localFilters, httpMethod: e.target.value })}
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
              onChange={(e) => setLocalFilters({ ...localFilters, responseStatusCode: e.target.value ? parseInt(e.target.value) : null })}
              placeholder="e.g., 200, 404, 500"
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
                onChange={(e) => setLocalFilters({ ...localFilters, fromDate: e.target.value })}
                className="px-3 py-2 rounded text-sm"
                style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
              />
              <input
                type="datetime-local"
                value={localFilters.toDate || ''}
                onChange={(e) => setLocalFilters({ ...localFilters, toDate: e.target.value })}
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
                onChange={(e) => setLocalFilters({ ...localFilters, minDuration: e.target.value ? parseInt(e.target.value) : null })}
                placeholder="Min"
                className="px-3 py-2 rounded text-sm"
                style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
              />
              <input
                type="number"
                value={localFilters.maxDuration || ''}
                onChange={(e) => setLocalFilters({ ...localFilters, maxDuration: e.target.value ? parseInt(e.target.value) : null })}
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
              onChange={(e) => setLocalFilters({ ...localFilters, clientIpAddress: e.target.value })}
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
              onChange={(e) => setLocalFilters({ ...localFilters, sourceApplication: e.target.value })}
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
              onChange={(e) => setLocalFilters({ ...localFilters, requestedBy: e.target.value })}
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
      <div className="relative w-96 rounded-lg overflow-hidden" style={{ 
        backgroundColor: colors.modalBg,
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

const APIRequest = ({ theme, isDark, customTheme, toggleTheme, authToken }) => {
  const [activeTab, setActiveTab] = useState('all');
  const [showFilterModal, setShowFilterModal] = useState(false);
  const [showExportModal, setShowExportModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [filters, setFilters] = useState({});
  const [requests, setRequests] = useState([]);
  const [statistics, setStatistics] = useState(null);
  const [systemStats, setSystemStats] = useState(null);
  const [isLoading, setIsLoading] = useState({
    requests: false,
    statistics: false,
    export: false,
    delete: false
  });
  const [toast, setToast] = useState(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0
  });
  const [dateRange, setDateRange] = useState({
    fromDate: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16),
    toDate: new Date().toISOString().slice(0, 16)
  });

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

  // Get status color
  const getStatusColor = (status) => {
    const colors_map = {
      'SUCCESS': colors.success,
      'FAILED': colors.error,
      'TIMEOUT': colors.warning,
      'PENDING': colors.info
    };
    return colors_map[status] || colors.textSecondary;
  };

  // Get method color
  const getMethodColor = (method) => {
    return colors.method[method] || colors.textSecondary;
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

  // Load requests with filters
  const loadRequests = useCallback(async () => {
    if (!authToken) {
      showToast('Authentication required', 'error');
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
        search: searchQuery
      };

      const response = await searchRequests(authToken, filter);
      
      if (response?.responseCode === 200) {
        const data = extractSearchResults(response);
        setRequests(data.content || []);
        setPagination({
          page: data.currentPage || 0,
          size: data.pageSize || 20,
          totalElements: data.totalElements || 0,
          totalPages: data.totalPages || 0
        });
      } else {
        showToast(response?.message || 'Failed to load requests', 'error');
      }
    } catch (error) {
      console.error('Error loading requests:', error);
      showToast(error.message || 'Failed to load requests', 'error');
    } finally {
      setIsLoading(prev => ({ ...prev, requests: false }));
    }
  }, [authToken, filters, pagination.page, pagination.size, dateRange.fromDate, dateRange.toDate, searchQuery]);

  // Load statistics
  const loadStatistics = useCallback(async () => {
    if (!authToken) return;

    setIsLoading(prev => ({ ...prev, statistics: true }));

    try {
      // Load system statistics
      const systemResponse = await getSystemStatistics(
        authToken,
        dateRange.fromDate,
        dateRange.toDate
      );
      
      if (systemResponse?.responseCode === 200) {
        setSystemStats(extractStatistics(systemResponse));
      }

      // Load dashboard stats
      const dashboardResponse = await getRequestDashboardStats(authToken);
      
      if (dashboardResponse?.responseCode === 200) {
        setStatistics(dashboardResponse.data);
      }
    } catch (error) {
      console.error('Error loading statistics:', error);
    } finally {
      setIsLoading(prev => ({ ...prev, statistics: false }));
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

    setIsLoading(prev => ({ ...prev, export: true }));

    try {
      const response = await exportRequests(
        authToken,
        filters.apiId || 'all',
        dateRange.fromDate,
        dateRange.toDate,
        config.format,
        config
      );

      if (response?.responseCode === 200) {
        const exportData = extractExportData(response);
        if (exportData.downloadUrl) {
          downloadExportedRequests(exportData);
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

  // Initial load
  useEffect(() => {
    if (authToken) {
      if (activeTab === 'all') {
        loadRequests();
      } else if (activeTab === 'statistics') {
        loadStatistics();
      } else if (activeTab === 'recent') {
        loadRecentRequests();
      }
    }
  }, [authToken, activeTab, loadRequests, loadStatistics, loadRecentRequests]);

  // Render statistics tab
  const renderStatistics = () => {
    const stats = systemStats || statistics || {};

    return (
      <div className="flex-1 overflow-auto p-6">
        <div className="max-w-6xl mx-auto space-y-6">
          {/* Date Range Selector */}
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold" style={{ color: colors.text }}>Request Statistics</h2>
            <div className="flex items-center gap-3">
              <input
                type="datetime-local"
                value={dateRange.fromDate}
                onChange={(e) => setDateRange({ ...dateRange, fromDate: e.target.value })}
                className="px-3 py-1.5 rounded text-sm"
                style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
              />
              <span style={{ color: colors.textSecondary }}>to</span>
              <input
                type="datetime-local"
                value={dateRange.toDate}
                onChange={(e) => setDateRange({ ...dateRange, toDate: e.target.value })}
                className="px-3 py-1.5 rounded text-sm"
                style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }}
              />
              <button
                onClick={loadStatistics}
                className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.primaryDark, color: 'white' }}
              >
                Apply
              </button>
            </div>
          </div>

          {/* Summary Cards */}
          <div className="grid grid-cols-4 gap-4">
            <div className="p-4 rounded-lg" style={{ backgroundColor: colors.card, border: `1px solid ${colors.border}` }}>
              <div className="flex items-center justify-between mb-2">
                <span style={{ color: colors.textSecondary }}>Total Requests</span>
                <Activity size={16} style={{ color: colors.info }} />
              </div>
              <div className="text-2xl font-semibold" style={{ color: colors.text }}>{stats.totalRequests || 0}</div>
            </div>

            <div className="p-4 rounded-lg" style={{ backgroundColor: colors.card, border: `1px solid ${colors.border}` }}>
              <div className="flex items-center justify-between mb-2">
                <span style={{ color: colors.textSecondary }}>Success Rate</span>
                <CheckCircle size={16} style={{ color: colors.success }} />
              </div>
              <div className="text-2xl font-semibold" style={{ color: colors.text }}>
                {stats.successRate ? stats.successRate.toFixed(1) : 0}%
              </div>
            </div>

            <div className="p-4 rounded-lg" style={{ backgroundColor: colors.card, border: `1px solid ${colors.border}` }}>
              <div className="flex items-center justify-between mb-2">
                <span style={{ color: colors.textSecondary }}>Avg Response</span>
                <ClockIcon size={16} style={{ color: colors.warning }} />
              </div>
              <div className="text-2xl font-semibold" style={{ color: colors.text }}>
                {formatExecutionTime(stats.averageResponseTime || 0)}
              </div>
            </div>

            <div className="p-4 rounded-lg" style={{ backgroundColor: colors.card, border: `1px solid ${colors.border}` }}>
              <div className="flex items-center justify-between mb-2">
                <span style={{ color: colors.textSecondary }}>Failed</span>
                <XCircle size={16} style={{ color: colors.error }} />
              </div>
              <div className="text-2xl font-semibold" style={{ color: colors.text }}>{stats.failedRequests || 0}</div>
            </div>
          </div>

          {/* Status Distribution */}
          <div className="grid grid-cols-2 gap-4">
            <div className="p-4 rounded-lg" style={{ backgroundColor: colors.card, border: `1px solid ${colors.border}` }}>
              <h3 className="text-sm font-semibold mb-4" style={{ color: colors.text }}>Status Distribution</h3>
              <div className="space-y-2">
                {stats.statusDistribution && Object.entries(stats.statusDistribution).map(([status, count]) => (
                  <div key={status} className="flex items-center gap-2">
                    <div className="w-2 h-2 rounded-full" style={{ backgroundColor: getStatusColor(status) }} />
                    <span className="text-sm flex-1" style={{ color: colors.text }}>{status}</span>
                    <span className="text-sm font-medium" style={{ color: colors.text }}>{count}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="p-4 rounded-lg" style={{ backgroundColor: colors.card, border: `1px solid ${colors.border}` }}>
              <h3 className="text-sm font-semibold mb-4" style={{ color: colors.text }}>Method Distribution</h3>
              <div className="space-y-2">
                {stats.methodDistribution && Object.entries(stats.methodDistribution).map(([method, count]) => (
                  <div key={method} className="flex items-center gap-2">
                    <div className="w-2 h-2 rounded-full" style={{ backgroundColor: getMethodColor(method) }} />
                    <span className="text-sm flex-1" style={{ color: colors.text }}>{method}</span>
                    <span className="text-sm font-medium" style={{ color: colors.text }}>{count}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Status Code Distribution */}
          <div className="p-4 rounded-lg" style={{ backgroundColor: colors.card, border: `1px solid ${colors.border}` }}>
            <h3 className="text-sm font-semibold mb-4" style={{ color: colors.text }}>Status Code Distribution</h3>
            <div className="space-y-2">
              {stats.statusCodeDistribution && Object.entries(stats.statusCodeDistribution).map(([code, count]) => (
                <div key={code} className="flex items-center gap-2">
                  <div className="w-2 h-2 rounded-full" style={{ backgroundColor: getStatusCodeColor(parseInt(code), colors) }} />
                  <span className="text-sm flex-1" style={{ color: colors.text }}>{code}</span>
                  <span className="text-sm font-medium" style={{ color: colors.text }}>{count}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Time Series Data */}
          {stats.timeSeriesData && stats.timeSeriesData.length > 0 && (
            <div className="p-4 rounded-lg" style={{ backgroundColor: colors.card, border: `1px solid ${colors.border}` }}>
              <h3 className="text-sm font-semibold mb-4" style={{ color: colors.text }}>Requests Over Time</h3>
              <div className="space-y-2 max-h-60 overflow-auto">
                {stats.timeSeriesData.map((point, index) => (
                  <div key={index} className="flex items-center gap-4 text-sm">
                    <span style={{ color: colors.textSecondary, minWidth: '150px' }}>
                      {new Date(point.timestamp).toLocaleString()}
                    </span>
                    <div className="flex-1 h-2 rounded-full" style={{ backgroundColor: colors.hover }}>
                      <div 
                        className="h-2 rounded-full" 
                        style={{ 
                          width: `${(point.requestCount / Math.max(...stats.timeSeriesData.map(p => p.requestCount))) * 100}%`,
                          backgroundColor: colors.primary
                        }}
                      />
                    </div>
                    <span style={{ color: colors.text, minWidth: '50px', textAlign: 'right' }}>
                      {point.requestCount}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    );
  };

  // Render requests table
  const renderRequestsTable = () => {
    return (
      <div className="flex-1 overflow-auto">
        <table className="w-full" style={{ borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: `1px solid ${colors.border}` }}>
              <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Status</th>
              <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Method</th>
              <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Request Name</th>
              <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>URL</th>
              <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Status Code</th>
              <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Duration</th>
              <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Timestamp</th>
              <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Requested By</th>
              <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {requests.map((request, index) => (
              <tr 
                key={request.id || index}
                className="hover:bg-opacity-50 transition-colors cursor-pointer"
                style={{ borderBottom: `1px solid ${colors.border}`, backgroundColor: 'transparent' }}
                onClick={() => handleViewDetails(request)}
              >
                <td className="py-3 px-4">
                  <div className="w-2 h-2 rounded-full" style={{ backgroundColor: getStatusColor(request.requestStatus) }} />
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
                  <span className="text-sm" style={{ color: colors.text }}>{request.requestName}</span>
                </td>
                <td className="py-3 px-4">
                  <span className="text-xs font-mono truncate max-w-xs" style={{ color: colors.textSecondary }}>
                    {request.url}
                  </span>
                </td>
                <td className="py-3 px-4">
                  <span className="text-sm" style={{ color: getStatusCodeColor(request.responseStatusCode, colors) }}>
                    {request.responseStatusCode || '-'}
                  </span>
                </td>
                <td className="py-3 px-4">
                  <span className="text-sm" style={{ color: colors.text }}>
                    {formatExecutionTime(request.executionDurationMs)}
                  </span>
                </td>
                <td className="py-3 px-4">
                  <span className="text-sm" style={{ color: colors.textSecondary }}>
                    {formatRequestTimestamp(request.requestTimestamp)}
                  </span>
                </td>
                <td className="py-3 px-4">
                  <span className="text-sm" style={{ color: colors.text }}>{request.requestedBy || '-'}</span>
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
            ))}
          </tbody>
        </table>

        {requests.length === 0 && !isLoading.requests && (
          <div className="text-center py-12">
            <FileText size={48} className="mx-auto mb-4 opacity-50" style={{ color: colors.textSecondary }} />
            <p className="text-lg mb-2" style={{ color: colors.text }}>No Requests Found</p>
            <p className="text-sm" style={{ color: colors.textSecondary }}>
              Try adjusting your filters or date range
            </p>
          </div>
        )}

        {isLoading.requests && (
          <div className="text-center py-12">
            <RefreshCw size={32} className="animate-spin mx-auto mb-4" style={{ color: colors.textSecondary }} />
            <p className="text-sm" style={{ color: colors.textSecondary }}>Loading requests...</p>
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="flex flex-col h-screen overflow-hidden" style={{ 
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

      {/* TOP NAVIGATION */}
      <div className="flex items-center justify-between h-10 px-4 border-b" style={{ 
        backgroundColor: colors.header,
        borderColor: colors.border
      }}>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-1 -ml-3 text-nowrap">
            <span className="px-3 py-1.5 text-sm font-medium rounded transition-colors uppercase" style={{ color: colors.text }}>
              API Request Monitor
            </span>
          </div>
        </div>

        <div className="flex items-center gap-2">
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

          {/* Cleanup Button */}
          <button 
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            onClick={handleCleanup}
            style={{ backgroundColor: colors.hover }}>
            <Trash2 size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>
      </div>

      {/* TABS */}
      <div className="flex items-center border-b h-9" style={{ 
        backgroundColor: colors.card,
        borderColor: colors.border
      }}>
        <div className="flex items-center px-2">
          {['all', 'recent', 'statistics'].map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift capitalize ${
                activeTab === tab ? '' : 'hover:bg-opacity-50'
              }`}
              style={{ 
                borderBottomColor: activeTab === tab ? colors.primary : 'transparent',
                color: activeTab === tab ? colors.primary : colors.textSecondary,
                backgroundColor: 'transparent'
              }}>
              {tab} {tab === 'all' && `(${pagination.totalElements})`}
            </button>
          ))}
        </div>

        {/* Search Bar */}
        <div className="flex-1 px-4">
          <div className="relative max-w-md ml-auto">
            <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
            <input 
              type="text" 
              placeholder="Search requests..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && loadRequests()}
              className="w-full pl-8 pr-3 py-1.5 rounded text-sm focus:outline-none hover-lift"
              style={{ 
                backgroundColor: colors.inputBg, 
                border: `1px solid ${colors.border}`, 
                color: colors.text 
              }} 
            />
            {searchQuery && (
              <div className="absolute right-2 top-1/2 transform -translate-y-1/2">
                <button onClick={() => setSearchQuery('')} className="p-0.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}>
                  <X size={10} style={{ color: colors.textSecondary }} />
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* MAIN CONTENT */}
      {activeTab === 'statistics' ? renderStatistics() : (
        <div className="flex-1 flex flex-col overflow-hidden">
          {/* Date Range Bar */}
          <div className="flex items-center gap-4 px-4 py-2 border-b" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
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
              onClick={loadRequests}
              className="px-3 py-1 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.primaryDark, color: 'white' }}
            >
              Apply
            </button>
          </div>

          {/* Requests Table */}
          {renderRequestsTable()}

          {/* Pagination */}
          {pagination.totalPages > 1 && (
            <div className="flex items-center justify-between px-4 py-3 border-t" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="text-sm" style={{ color: colors.textSecondary }}>
                Showing {pagination.page * pagination.size + 1} - {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of {pagination.totalElements}
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setPagination({ ...pagination, page: pagination.page - 1 })}
                  disabled={pagination.page === 0}
                  className="px-3 py-1 rounded text-sm font-medium hover:bg-opacity-50 transition-colors disabled:opacity-50"
                  style={{ backgroundColor: colors.hover, color: colors.text }}
                >
                  Previous
                </button>
                <span className="text-sm" style={{ color: colors.text }}>
                  Page {pagination.page + 1} of {pagination.totalPages}
                </span>
                <button
                  onClick={() => setPagination({ ...pagination, page: pagination.page + 1 })}
                  disabled={pagination.page >= pagination.totalPages - 1}
                  className="px-3 py-1 rounded text-sm font-medium hover:bg-opacity-50 transition-colors disabled:opacity-50"
                  style={{ backgroundColor: colors.hover, color: colors.text }}
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      )}

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
          setPagination({ ...pagination, page: 0 });
          loadRequests();
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