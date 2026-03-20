import React, { useState, useEffect, useCallback, useRef } from 'react';
import { 
  Search,
  Download,
  Copy,
  X,
  Check,
  AlertCircle,
  Clock,
  Activity,
  Loader,
  FileCode,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  Database as DatabaseIcon,
  DownloadCloud,
  Filter,
  Calendar,
  TrendingUp,
  TrendingDown,
  Clock as ClockIcon,
  CheckCircle,
  XCircle,
  Eye,
  Trash2,
  FileText,
  Home,
  LayoutDashboard,
  Server,
  Zap,
  BarChart3,
  ListFilter,
  Grid3x3
} from 'lucide-react';

// Import APIRequestController functions
import {
  searchRequests,
  getRequestById,
  deleteRequest,
  exportRequests,
  getRequestDashboardStats,
  getSystemStatistics
} from "../controllers/APIRequestController.js";

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
        // Simple JSON highlighting
        highlightedLine = highlightedLine.replace(/"([^"\\]*(\\.[^"\\]*)*)"/g, 
          '<span class="text-green-400">"$1"</span>');
        highlightedLine = highlightedLine.replace(/\b(\d+)\b/g, 
          '<span class="text-blue-400">$1</span>');
        highlightedLine = highlightedLine.replace(/(true|false|null)/g, 
          '<span class="text-purple-400">$1</span>');
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

// Request Details Modal
const RequestDetailsModal = ({ request, colors, isOpen, onClose }) => {
  if (!isOpen || !request) return null;

  const [activeTab, setActiveTab] = useState('request');

  const formatExecutionTime = (ms) => {
    if (!ms) return 'N/A';
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`;
    return `${(ms / 60000).toFixed(2)}m`;
  };

  const getStatusColor = (status) => {
    const colors = {
      'SUCCESS': '#10b981',
      'FAILED': '#ef4444',
      'TIMEOUT': '#f59e0b',
      'PENDING': '#3b82f6'
    };
    return colors[status] || '#6b7280';
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50">
      <div className="absolute inset-0 bg-black/70 backdrop-blur-sm" onClick={onClose} />
      <div className="relative w-4/5 max-w-5xl max-h-[85vh] rounded-xl overflow-hidden" style={{ 
        backgroundColor: colors.bg,
        border: `1px solid ${colors.border}`
      }}>
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b" style={{ borderColor: colors.border }}>
          <div className="flex items-center gap-3">
            <h2 className="text-lg font-semibold" style={{ color: colors.text }}>Request Details</h2>
            <div className="flex items-center gap-2">
              <span className="text-xs px-2 py-1 rounded-full font-medium" style={{ 
                backgroundColor: getStatusColor(request.requestStatus),
                color: 'white'
              }}>
                {request.requestStatus || 'PENDING'}
              </span>
              <span className="text-xs px-2 py-1 rounded-full font-medium" style={{ 
                backgroundColor: request.responseStatusCode >= 200 && request.responseStatusCode < 300 ? '#10b981' : '#ef4444',
                color: 'white'
              }}>
                {request.responseStatusCode || 'N/A'}
              </span>
            </div>
          </div>
          <button onClick={onClose} className="p-1 rounded hover:bg-opacity-10 hover:bg-white transition-colors">
            <X size={18} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        {/* Tabs */}
        <div className="flex items-center px-6 border-b gap-2" style={{ borderColor: colors.border }}>
          {['request', 'response', 'headers', 'timeline'].map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-2 text-sm font-medium capitalize border-b-2 transition-all`}
              style={{ 
                borderBottomColor: activeTab === tab ? colors.primary : 'transparent',
                color: activeTab === tab ? colors.primary : colors.textSecondary
              }}
            >
              {tab}
            </button>
          ))}
        </div>

        {/* Content */}
        <div className="p-6 overflow-auto max-h-[calc(85vh-120px)]">
          {activeTab === 'request' && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="p-3 rounded-lg" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Request Name</div>
                  <div className="text-sm font-medium" style={{ color: colors.text }}>{request.requestName || 'N/A'}</div>
                </div>
                <div className="p-3 rounded-lg" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>HTTP Method</div>
                  <div className="text-sm font-medium" style={{ color: colors.text }}>{request.httpMethod || 'N/A'}</div>
                </div>
                <div className="p-3 rounded-lg col-span-2" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>URL</div>
                  <div className="text-xs font-mono break-all" style={{ color: colors.text }}>{request.url || 'N/A'}</div>
                </div>
              </div>
              
              {request.requestBody && (
                <div>
                  <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Request Body</h3>
                  <div className="p-3 rounded-lg max-h-80 overflow-auto" style={{ backgroundColor: colors.codeBg }}>
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
                <div className="p-3 rounded-lg" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Status Code</div>
                  <div className="text-sm font-medium" style={{ color: colors.text }}>{request.responseStatusCode || 'N/A'}</div>
                </div>
                <div className="p-3 rounded-lg" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Duration</div>
                  <div className="text-sm font-medium" style={{ color: colors.text }}>{formatExecutionTime(request.executionDurationMs)}</div>
                </div>
                <div className="p-3 rounded-lg" style={{ backgroundColor: colors.hover }}>
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Response Size</div>
                  <div className="text-sm font-medium" style={{ color: colors.text }}>
                    {request.responseSizeBytes ? `${(request.responseSizeBytes / 1024).toFixed(2)} KB` : 'N/A'}
                  </div>
                </div>
              </div>

              {request.responseBody && (
                <div>
                  <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Response Body</h3>
                  <div className="p-3 rounded-lg max-h-96 overflow-auto" style={{ backgroundColor: colors.codeBg }}>
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
                  <div className="space-y-2">
                    {Object.entries(request.headers).map(([key, value]) => (
                      <div key={key} className="flex items-start gap-2 p-2 rounded-lg" style={{ backgroundColor: colors.hover }}>
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
                  <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Created</div>
                  <div className="text-sm" style={{ color: colors.text }}>{new Date(request.createdAt).toLocaleString()}</div>
                </div>
                {request.requestTimestamp && (
                  <div className="relative">
                    <div className="absolute left-[-24px] top-0 w-3 h-3 rounded-full" style={{ backgroundColor: colors.primary }} />
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Request Sent</div>
                    <div className="text-sm" style={{ color: colors.text }}>{new Date(request.requestTimestamp).toLocaleString()}</div>
                  </div>
                )}
                {request.responseTimestamp && (
                  <div className="relative">
                    <div className="absolute left-[-24px] top-0 w-3 h-3 rounded-full" style={{ backgroundColor: '#10b981' }} />
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Response Received</div>
                    <div className="text-sm" style={{ color: colors.text }}>{new Date(request.responseTimestamp).toLocaleString()}</div>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-2 px-6 py-3 border-t" style={{ borderColor: colors.border }}>
          <button
            onClick={() => navigator.clipboard.writeText(JSON.stringify(request, null, 2))}
            className="px-3 py-1.5 rounded-lg text-sm font-medium transition-colors flex items-center gap-2"
            style={{ backgroundColor: colors.hover, color: colors.text }}
          >
            <Copy size={12} />
            Copy JSON
          </button>
          <button
            onClick={onClose}
            className="px-3 py-1.5 rounded-lg text-sm font-medium transition-colors"
            style={{ backgroundColor: colors.primary, color: 'white' }}
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

// Stats Cards
const StatsCards = ({ stats, colors }) => {
  const formatTime = (ms) => {
    if (!ms) return 'N/A';
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
    return `${(ms / 60000).toFixed(1)}m`;
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div className="p-4 rounded-xl border" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
        <div className="flex items-center justify-between mb-2">
          <span className="text-sm font-medium" style={{ color: colors.textSecondary }}>Total Requests</span>
          <Activity size={18} style={{ color: colors.primary }} />
        </div>
        <div className="text-2xl font-bold" style={{ color: colors.text }}>
          {stats?.totalRequests?.toLocaleString() || 0}
        </div>
      </div>

      <div className="p-4 rounded-xl border" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
        <div className="flex items-center justify-between mb-2">
          <span className="text-sm font-medium" style={{ color: colors.textSecondary }}>Success Rate</span>
          <CheckCircle size={18} style={{ color: '#10b981' }} />
        </div>
        <div className="text-2xl font-bold" style={{ color: colors.text }}>
          {stats?.successRate ? stats.successRate.toFixed(1) : 0}%
        </div>
      </div>

      <div className="p-4 rounded-xl border" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
        <div className="flex items-center justify-between mb-2">
          <span className="text-sm font-medium" style={{ color: colors.textSecondary }}>Avg Response Time</span>
          <ClockIcon size={18} style={{ color: '#f59e0b' }} />
        </div>
        <div className="text-2xl font-bold" style={{ color: colors.text }}>
          {formatTime(stats?.averageResponseTime || 0)}
        </div>
      </div>

      <div className="p-4 rounded-xl border" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
        <div className="flex items-center justify-between mb-2">
          <span className="text-sm font-medium" style={{ color: colors.textSecondary }}>Failed Requests</span>
          <XCircle size={18} style={{ color: '#ef4444' }} />
        </div>
        <div className="text-2xl font-bold" style={{ color: colors.text }}>
          {stats?.failedRequests?.toLocaleString() || 0}
        </div>
      </div>
    </div>
  );
};

// Loading Overlay
const LoadingOverlay = ({ isLoading, colors }) => {
  if (!isLoading) return null;
  
  return (
    <div className="fixed inset-0 flex items-center justify-center z-50 bg-black/50 backdrop-blur-sm">
      <div className="text-center">
        <Loader className="animate-spin mx-auto mb-4" size={48} style={{ color: colors.primary }} />
        <p className="text-sm" style={{ color: colors.text }}>Loading...</p>
      </div>
    </div>
  );
};

const APIRequest = ({ isDark, authToken }) => {
  // Color scheme
  const colors = isDark ? {
    bg: '#0f172a',
    card: '#1e293b',
    text: '#f1f5f9',
    textSecondary: '#94a3b8',
    border: '#334155',
    hover: '#334155',
    primary: '#3b82f6',
    success: '#10b981',
    error: '#ef4444',
    warning: '#f59e0b',
    codeBg: '#0f172a'
  } : {
    bg: '#f8fafc',
    card: '#ffffff',
    text: '#0f172a',
    textSecondary: '#64748b',
    border: '#e2e8f0',
    hover: '#f1f5f9',
    primary: '#3b82f6',
    success: '#10b981',
    error: '#ef4444',
    warning: '#f59e0b',
    codeBg: '#f1f5f9'
  };

  // State
  const [requests, setRequests] = useState([]);
  const [apiList, setApiList] = useState([]);
  const [selectedApi, setSelectedApi] = useState(null);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [dateRange, setDateRange] = useState({
    fromDate: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16),
    toDate: new Date().toISOString().slice(0, 16)
  });
  const [pagination, setPagination] = useState({ page: 0, size: 10, total: 0, totalPages: 0 });
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [toast, setToast] = useState(null);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  // Show toast
  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  // Format time
  const formatTime = (ms) => {
    if (!ms) return 'N/A';
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`;
    return `${(ms / 60000).toFixed(2)}m`;
  };

  // Format date
  const formatDate = (timestamp) => {
    if (!timestamp) return 'N/A';
    return new Date(timestamp).toLocaleString();
  };

  // Load requests with filters
  const loadRequests = useCallback(async () => {
    if (!authToken) return;
    
    setLoading(true);
    try {
      const filter = {
        page: pagination.page,
        size: pagination.size,
        fromDate: dateRange.fromDate,
        toDate: dateRange.toDate,
        search: searchQuery || undefined,
        apiId: selectedApi?.apiId || undefined
      };
      
      Object.keys(filter).forEach(key => filter[key] === undefined && delete filter[key]);
      
      const response = await searchRequests(authToken, filter);
      
      if (response?.responseCode === 200) {
        const data = response.data;
        setRequests(data.content || []);
        setApiList(data.apiSummaries || []);
        setPagination(prev => ({
          ...prev,
          total: data.totalElements || 0,
          totalPages: data.totalPages || 0
        }));
      }
    } catch (error) {
      console.error('Error loading requests:', error);
      showToast('Failed to load requests', 'error');
    } finally {
      setLoading(false);
    }
  }, [authToken, pagination.page, pagination.size, dateRange, searchQuery, selectedApi]);

  // Load statistics
  const loadStats = useCallback(async () => {
    if (!authToken) return;
    
    try {
      const response = await getRequestDashboardStats(authToken);
      if (response?.responseCode === 200) {
        setStats(response.data);
      }
    } catch (error) {
      console.error('Error loading stats:', error);
    }
  }, [authToken]);

  // Handle API selection
  const handleApiSelect = (api) => {
    setSelectedApi(api);
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  // Handle clear API filter
  const handleClearApiFilter = () => {
    setSelectedApi(null);
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  // Handle view details
  const handleViewDetails = async (request) => {
    if (!authToken || !request?.id) return;
    
    try {
      const response = await getRequestById(authToken, request.id);
      if (response?.responseCode === 200) {
        setSelectedRequest(response.data);
        setShowDetailsModal(true);
      }
    } catch (error) {
      console.error('Error loading details:', error);
      showToast('Failed to load request details', 'error');
    }
  };

  // Handle delete
  const handleDelete = async (requestId) => {
    if (!authToken || !confirm('Are you sure you want to delete this request?')) return;
    
    try {
      const response = await deleteRequest(authToken, requestId);
      if (response?.responseCode === 200) {
        showToast('Request deleted successfully', 'success');
        loadRequests();
      }
    } catch (error) {
      console.error('Error deleting request:', error);
      showToast('Failed to delete request', 'error');
    }
  };

  // Handle export
  const handleExport = async () => {
    if (!authToken) return;
    
    try {
      const response = await exportRequests(
        authToken,
        selectedApi?.apiId || 'all',
        dateRange.fromDate,
        dateRange.toDate,
        'JSON',
        {}
      );
      
      if (response?.responseCode === 200 && response.data?.downloadUrl) {
        window.open(response.data.downloadUrl, '_blank');
        showToast('Export started', 'success');
      }
    } catch (error) {
      console.error('Error exporting:', error);
      showToast('Failed to export', 'error');
    }
  };

  // Initial load
  useEffect(() => {
    if (authToken) {
      loadRequests();
      loadStats();
    }
  }, [authToken, loadRequests, loadStats]);

  // Reload when filters change
  useEffect(() => {
    if (authToken) {
      loadRequests();
    }
  }, [pagination.page, dateRange, searchQuery, selectedApi]);

  // Debounced search
  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  // Get status color
  const getStatusColor = (status) => {
    const colors = {
      'SUCCESS': '#10b981',
      'FAILED': '#ef4444',
      'TIMEOUT': '#f59e0b',
      'PENDING': '#3b82f6'
    };
    return colors[status] || '#6b7280';
  };

  // Get method color
  const getMethodColor = (method) => {
    const colors = {
      'GET': '#10b981',
      'POST': '#3b82f6',
      'PUT': '#f59e0b',
      'DELETE': '#ef4444',
      'PATCH': '#8b5cf6'
    };
    return colors[method] || '#6b7280';
  };

  return (
    <div className="flex h-screen overflow-hidden" style={{ backgroundColor: colors.bg, color: colors.text }}>
      {/* Toast */}
      {toast && (
        <div className="fixed bottom-4 right-4 z-50 px-4 py-2 rounded-lg shadow-lg animate-fade-in-up"
          style={{ 
            backgroundColor: toast.type === 'error' ? colors.error : 
                           toast.type === 'success' ? colors.success : 
                           colors.primary,
            color: 'white'
          }}>
          {toast.message}
        </div>
      )}

      {/* Loading Overlay */}
      <LoadingOverlay isLoading={loading} colors={colors} />

      {/* Details Modal */}
      <RequestDetailsModal
        request={selectedRequest}
        colors={colors}
        isOpen={showDetailsModal}
        onClose={() => setShowDetailsModal(false)}
      />

      {/* Sidebar */}
      <div 
        className={`border-r transition-all duration-300 flex flex-col ${sidebarCollapsed ? 'w-16' : 'w-80'}`}
        style={{ backgroundColor: colors.card, borderColor: colors.border }}
      >
        {/* Sidebar Header */}
        <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
          {!sidebarCollapsed && (
            <div>
              <h2 className="text-lg font-bold" style={{ color: colors.text }}>API Monitor</h2>
              <p className="text-xs mt-1" style={{ color: colors.textSecondary }}>Request Tracker</p>
            </div>
          )}
          <button
            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
            className="p-2 rounded-lg hover:bg-opacity-10 hover:bg-white transition-colors ml-auto"
          >
            <LayoutDashboard size={18} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        {/* All Requests Button */}
        <div className="p-3">
          <button
            onClick={handleClearApiFilter}
            className={`w-full flex items-center gap-3 p-3 rounded-xl transition-all ${
              !selectedApi ? 'ring-2' : ''
            }`}
            style={{ 
              backgroundColor: !selectedApi ? colors.primary : colors.hover,
              color: !selectedApi ? 'white' : colors.text
            }}
          >
            <Home size={18} />
            {!sidebarCollapsed && (
              <>
                <span className="flex-1 text-left">All Requests</span>
                <span className="text-xs px-2 py-1 rounded-full" style={{ backgroundColor: 'rgba(255,255,255,0.2)' }}>
                  {apiList.reduce((sum, api) => sum + api.totalRequests, 0)}
                </span>
              </>
            )}
          </button>
        </div>

        {/* API List */}
        <div className="flex-1 overflow-auto px-3">
          {!sidebarCollapsed && (
            <div className="flex items-center justify-between mb-3 px-2">
              <span className="text-xs font-semibold uppercase tracking-wider" style={{ color: colors.textSecondary }}>
                APIs ({apiList.length})
              </span>
            </div>
          )}
          
          <div className="space-y-2">
            {apiList.map(api => (
              <button
                key={api.apiId}
                onClick={() => handleApiSelect(api)}
                className={`w-full flex items-start gap-3 p-3 rounded-xl transition-all text-left ${
                  selectedApi?.apiId === api.apiId ? 'ring-2' : ''
                }`}
                style={{ 
                  backgroundColor: selectedApi?.apiId === api.apiId ? colors.primary : 'transparent',
                  color: selectedApi?.apiId === api.apiId ? 'white' : colors.text
                }}
              >
                <DatabaseIcon size={18} className="mt-0.5" />
                {!sidebarCollapsed && (
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium truncate">{api.apiName}</span>
                      <span className="text-xs px-1.5 py-0.5 rounded-full" style={{ 
                        backgroundColor: selectedApi?.apiId === api.apiId ? 'rgba(255,255,255,0.2)' : colors.hover 
                      }}>
                        {api.totalRequests}
                      </span>
                    </div>
                    <div className="text-xs mt-1 font-mono" style={{ 
                      color: selectedApi?.apiId === api.apiId ? 'rgba(255,255,255,0.7)' : colors.textSecondary 
                    }}>
                      {api.apiCode}
                    </div>
                    <div className="flex items-center gap-2 mt-1">
                      <span className="text-xs">✓ {api.successCount}</span>
                      <span className="text-xs">✗ {api.failedCount}</span>
                      <span className="text-xs">{api.successRate.toFixed(0)}%</span>
                    </div>
                  </div>
                )}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
          <div className="flex items-center gap-3">
            <h1 className="text-xl font-bold" style={{ color: colors.text }}>
              {selectedApi ? selectedApi.apiName : 'All Requests'}
            </h1>
            {selectedApi && (
              <span className="text-xs px-2 py-1 rounded-full" style={{ backgroundColor: colors.hover, color: colors.textSecondary }}>
                {selectedApi.apiCode}
              </span>
            )}
          </div>
          
          <div className="flex items-center gap-2">
            {/* Search */}
            <div className="relative">
              <Search size={16} className="absolute left-3 top-1/2 transform -translate-y-1/2" style={{ color: colors.textSecondary }} />
              <input
                type="text"
                placeholder="Search requests..."
                value={searchQuery}
                onChange={handleSearchChange}
                className="pl-9 pr-3 py-2 rounded-lg text-sm w-64"
                style={{ backgroundColor: colors.bg, border: `1px solid ${colors.border}`, color: colors.text }}
              />
            </div>
            
            {/* Export */}
            <button
              onClick={handleExport}
              className="p-2 rounded-lg transition-colors"
              style={{ backgroundColor: colors.hover }}
            >
              <DownloadCloud size={18} style={{ color: colors.textSecondary }} />
            </button>
            
            {/* Refresh */}
            <button
              onClick={() => loadRequests()}
              className="p-2 rounded-lg transition-colors"
              style={{ backgroundColor: colors.hover }}
            >
              <RefreshCw size={18} style={{ color: colors.textSecondary }} />
            </button>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="p-4">
          <StatsCards stats={stats} colors={colors} />
        </div>

        {/* Date Range Filter */}
        <div className="px-4 pb-4">
          <div className="flex items-center gap-3 p-3 rounded-lg" style={{ backgroundColor: colors.card, border: `1px solid ${colors.border}` }}>
            <Calendar size={16} style={{ color: colors.textSecondary }} />
            <span className="text-sm" style={{ color: colors.textSecondary }}>Date Range:</span>
            <input
              type="datetime-local"
              value={dateRange.fromDate}
              onChange={(e) => setDateRange({ ...dateRange, fromDate: e.target.value })}
              className="px-2 py-1 rounded text-sm"
              style={{ backgroundColor: colors.bg, border: `1px solid ${colors.border}`, color: colors.text }}
            />
            <span style={{ color: colors.textSecondary }}>to</span>
            <input
              type="datetime-local"
              value={dateRange.toDate}
              onChange={(e) => setDateRange({ ...dateRange, toDate: e.target.value })}
              className="px-2 py-1 rounded text-sm"
              style={{ backgroundColor: colors.bg, border: `1px solid ${colors.border}`, color: colors.text }}
            />
            <button
              onClick={() => setPagination(prev => ({ ...prev, page: 0 }))}
              className="px-3 py-1 rounded-lg text-sm font-medium"
              style={{ backgroundColor: colors.primary, color: 'white' }}
            >
              Apply
            </button>
          </div>
        </div>

        {/* Requests Table */}
        <div className="flex-1 overflow-auto px-4 pb-4">
          <div className="rounded-lg border overflow-hidden" style={{ borderColor: colors.border }}>
            <table className="w-full">
              <thead className="sticky top-0" style={{ backgroundColor: colors.card }}>
                <tr style={{ borderBottom: `1px solid ${colors.border}` }}>
                  <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Status</th>
                  <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Method</th>
                  <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Request Name</th>
                  <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>API</th>
                  <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Status Code</th>
                  <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Duration</th>
                  <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Timestamp</th>
                  <th className="text-left py-3 px-4 text-xs font-medium" style={{ color: colors.textSecondary }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {requests.map((request, index) => (
                  <tr 
                    key={request.id || index}
                    className="cursor-pointer transition-colors hover:bg-opacity-50"
                    style={{ borderBottom: `1px solid ${colors.border}`, backgroundColor: 'transparent' }}
                    onClick={() => handleViewDetails(request)}
                  >
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        <div className="w-2 h-2 rounded-full" style={{ backgroundColor: getStatusColor(request.requestStatus) }} />
                        <span className="text-xs font-medium" style={{ color: getStatusColor(request.requestStatus) }}>
                          {request.requestStatus || 'PENDING'}
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
                      <span className="text-sm font-medium" style={{ 
                        color: request.responseStatusCode >= 200 && request.responseStatusCode < 300 ? colors.success : colors.error 
                      }}>
                        {request.responseStatusCode || '-'}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <span className="text-sm" style={{ color: colors.text }}>
                        {formatTime(request.executionDurationMs)}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <span className="text-xs" style={{ color: colors.textSecondary }}>
                        {formatDate(request.requestTimestamp)}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleViewDetails(request);
                          }}
                          className="p-1.5 rounded-lg transition-colors"
                          style={{ backgroundColor: colors.hover }}
                        >
                          <Eye size={14} style={{ color: colors.textSecondary }} />
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDelete(request.id);
                          }}
                          className="p-1.5 rounded-lg transition-colors"
                          style={{ backgroundColor: colors.hover }}
                        >
                          <Trash2 size={14} style={{ color: colors.error }} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {/* Empty State */}
            {requests.length === 0 && !loading && (
              <div className="text-center py-12">
                <FileText size={48} className="mx-auto mb-4 opacity-50" style={{ color: colors.textSecondary }} />
                <p className="text-lg mb-2" style={{ color: colors.text }}>No Requests Found</p>
                <p className="text-sm" style={{ color: colors.textSecondary }}>
                  {selectedApi ? `No requests found for ${selectedApi.apiName}` : 'No requests found for the selected filters'}
                </p>
              </div>
            )}

            {/* Pagination */}
            {pagination.totalPages > 1 && (
              <div className="flex items-center justify-between px-4 py-3 border-t" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
                <div className="text-sm" style={{ color: colors.textSecondary }}>
                  Showing {pagination.page * pagination.size + 1} - {Math.min((pagination.page + 1) * pagination.size, pagination.total)} of {pagination.total}
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setPagination(prev => ({ ...prev, page: prev.page - 1 }))}
                    disabled={pagination.page === 0}
                    className="p-2 rounded-lg transition-colors disabled:opacity-50"
                    style={{ backgroundColor: colors.hover }}
                  >
                    <ChevronLeft size={16} style={{ color: colors.textSecondary }} />
                  </button>
                  <span className="text-sm" style={{ color: colors.text }}>
                    Page {pagination.page + 1} of {pagination.totalPages}
                  </span>
                  <button
                    onClick={() => setPagination(prev => ({ ...prev, page: prev.page + 1 }))}
                    disabled={pagination.page >= pagination.totalPages - 1}
                    className="p-2 rounded-lg transition-colors disabled:opacity-50"
                    style={{ backgroundColor: colors.hover }}
                  >
                    <ChevronRight size={16} style={{ color: colors.textSecondary }} />
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      <style>{`
        @keyframes fade-in-up {
          from {
            opacity: 0;
            transform: translateY(10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        
        .animate-fade-in-up {
          animation: fade-in-up 0.2s ease-out;
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
          background: ${colors.textSecondary};
          border-radius: 4px;
        }
        
        ::-webkit-scrollbar-thumb:hover {
          background: ${colors.text};
        }
      `}</style>
    </div>
  );
};

export default APIRequest;