// Dashboard.jsx - FIXED VERSION WITH WORKING PAGINATION AND LOADING INDICATOR
import React, { useState, useEffect, useCallback, useMemo, lazy, Suspense, useRef } from 'react';
import {
  Database, FileCode, Activity, Zap, Settings,
  Search, RefreshCw, Plus, CheckCircle, AlertCircle, Users, Minus,
  Shield, Download, Edit, Trash2, X, AlertTriangle, Edit2, Copy,
  Table, Code, Loader, BookOpen, UserCog, Rocket, TrendingDown, TrendingUp,
  Home, ChevronLeft, ChevronRight as ChevronRightIcon, ChevronRight,
  LayoutDashboard, Sliders, Sparkles, Wand2, Zap as ZapIcon,
  SearchIcon, Database as DatabaseIcon, FileCode as FileCodeIcon,
  ShieldCheck as ShieldCheckIcon, Sparkles as SparklesIcon
} from "lucide-react";

// Lazy load modal to reduce initial bundle size
const ApiGenerationModal = lazy(() => import('@/components/modals/ApiGenerationModal.js'));

// Import the updated dashboard controller methods
import { 
  getDashboardStats, 
  getDashboardCollections, 
  getDashboardEndpoints, 
  getGeneratedApiDetails,
  handleDashboardResponse 
} from "../controllers/DashboardController.js";

// ============ CONSTANTS & CONFIG ============
const STAT_CARDS = [
  { key: 'totalApis', icon: Database, label: 'Total APIs', colorKey: 'success' },
  { key: 'totalDocumentationEndpoints', icon: FileCode, label: 'API Documentation', colorKey: 'info' },
  { key: 'totalCalls', icon: Activity, label: 'API Requests', colorKey: 'primaryDark' },
  { key: 'totalCollections', icon: FileCode, label: 'API Collections', colorKey: 'info' }
];

const PAGINATION_OPTIONS = [5, 8, 10, 15, 20];
const DEFAULT_PAGE_SIZE = 5;
const DEFAULT_PAGE = 0; // 0-based for backend

// ============ COLOR SCHEME FACTORY ============
const getColorScheme = (isDark) => ({
  bg: isDark ? 'rgb(1 14 35)' : '#f8fafc',
  white: isDark ? '#FFFFFF' : '#f8fafc',
  sidebar: isDark ? 'rgb(41 53 72 / 19%)' : '#ffffff',
  main: isDark ? 'rgb(1 14 35)' : '#f8fafc',
  header: isDark ? 'rgb(20 26 38)' : '#ffffff',
  card: isDark ? 'rgb(41 53 72 / 19%)' : '#ffffff',
  text: isDark ? '#F1F5F9' : '#1e293b',
  textSecondary: isDark ? 'rgb(148 163 184)' : '#64748b',
  textTertiary: isDark ? 'rgb(100 116 139)' : '#94a3b8',
  border: isDark ? 'rgb(51 65 85 / 19%)' : '#e2e8f0',
  borderLight: isDark ? 'rgb(45 55 72)' : '#f1f5f9',
  borderDark: isDark ? 'rgb(71 85 105)' : '#cbd5e1',
  hover: isDark ? 'rgb(45 46 72 / 33%)' : '#f1f5f9',
  active: isDark ? 'rgb(59 74 99)' : '#e2e8f0',
  selected: isDark ? 'rgb(44 82 130)' : '#dbeafe',
  primary: isDark ? 'rgb(96 165 250)' : '#1e293b',
  primaryLight: isDark ? 'rgb(147 197 253)' : '#60a5fa',
  primaryDark: isDark ? 'rgb(37 99 235)' : '#2563eb',
  success: isDark ? 'rgb(52 211 153)' : '#10b981',
  warning: isDark ? 'rgb(251 191 36)' : '#f59e0b',
  error: isDark ? 'rgb(248 113 113)' : '#ef4444',
  info: isDark ? 'rgb(96 165 250)' : '#3b82f6',
  tabActive: isDark ? 'rgb(96 165 250)' : '#3b82f6',
  tabInactive: isDark ? 'rgb(148 163 184)' : '#64748b',
  sidebarActive: isDark ? 'rgb(96 165 250)' : '#3b82f6',
  sidebarHover: isDark ? 'rgb(45 46 72 / 33%)' : '#f1f5f9',
  inputBg: isDark ? 'rgb(41 53 72 / 19%)' : '#ffffff',
  inputBorder: isDark ? 'rgb(51 65 85 / 19%)' : '#e2e8f0',
  tableHeader: isDark ? 'rgb(41 53 72 / 19%)' : '#f8fafc',
  tableRow: isDark ? 'rgb(41 53 72 / 19%)' : '#ffffff',
  tableRowHover: isDark ? 'rgb(45 46 72 / 33%)' : '#f8fafc',
  dropdownBg: isDark ? 'rgb(41 53 72 / 19%)' : '#ffffff',
  dropdownBorder: isDark ? 'rgb(51 65 85 / 19%)' : '#e2e8f0',
  modalBg: isDark ? 'rgb(41 53 72 / 19%)' : '#ffffff',
  modalBorder: isDark ? 'rgb(51 65 85 / 19%)' : '#e2e8f0',
  codeBg: isDark ? 'rgb(41 53 72 / 19%)' : '#f1f5f9',
  connectionOnline: isDark ? 'rgb(52 211 153)' : '#10b981',
  connectionOffline: isDark ? 'rgb(248 113 113)' : '#ef4444',
  connectionIdle: isDark ? 'rgb(251 191 36)' : '#f59e0b',
  accentPurple: isDark ? 'rgb(167 139 250)' : '#a78bfa',
  accentPink: isDark ? 'rgb(244 114 182)' : '#f472b6',
  accentCyan: isDark ? 'rgb(34 211 238)' : '#22d3ee',
  gradient: isDark ? 'from-blue-500/20 via-violet-500/20 to-orange-500/20' : 'from-blue-400/20 via-violet-400/20 to-orange-400/20'
});

// ============ UTILITY FUNCTIONS ============
const formatTimeAgo = (date) => {
  if (!date) return 'N/A';
  try {
    const now = Date.now();
    const diffInSeconds = Math.floor((now - new Date(date).getTime()) / 1000);
    
    if (diffInSeconds < 60) return `${diffInSeconds}s ago`;
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m ago`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h ago`;
    return `${Math.floor(diffInSeconds / 86400)}d ago`;
  } catch (e) {
    return 'Invalid date';
  }
};

const getMethodColor = (method, isDark) => {
  const colors = {
    GET: isDark ? 'text-emerald-400 bg-emerald-950/30' : 'text-emerald-600 bg-emerald-50',
    POST: isDark ? 'text-blue-400 bg-blue-950/30' : 'text-blue-600 bg-blue-50',
    PUT: isDark ? 'text-amber-400 bg-amber-950/30' : 'text-amber-600 bg-amber-50',
    DELETE: isDark ? 'text-rose-400 bg-rose-950/30' : 'text-rose-600 bg-rose-50'
  };
  return colors[method] || colors.GET;
};

// ============ COMPONENTS ============

// Loading Spinner Component for Table
const TableLoader = ({ colors }) => (
  <div className="flex flex-col items-center justify-center py-12">
    <div className="relative">
      <Loader className="animate-spin" size={40} style={{ color: colors.primary }} />
      <div className="absolute inset-0 flex items-center justify-center">
        <div className="w-2 h-2 rounded-full" style={{ backgroundColor: colors.primary }}></div>
      </div>
    </div>
    <p className="text-sm mt-4" style={{ color: colors.textSecondary }}>Loading endpoints...</p>
  </div>
);

// Stat Card - Memoized
const StatCard = React.memo(({ title, value, icon: Icon, change, color, onClick, colors }) => {
  const formattedValue = typeof value === 'number' ? value.toLocaleString() : value || '0';
  
 return (
      <div 
        className="border rounded-xl p-3 md:p-4 hover-lift cursor-pointer transition-all duration-200"
        onClick={onClick}
        style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card,
        }}
      >
        <div className="flex items-center justify-between mb-2">
          <div className="text-xs sm:text-sm font-medium truncate" style={{ color: colors.textSecondary }}>
            {title}
          </div>
          <div className="p-2 rounded-lg shrink-0" style={{ backgroundColor: `${color}20` }}>
            <Icon size={14} style={{ color }} />
          </div>
        </div>
        <div className="flex items-end justify-between">
          <div className="text-lg sm:text-xl md:text-2xl font-bold truncate" style={{ color: colors.text }}>
            {value}
          </div>
         <div className={`text-xs px-2 py-1 rounded-full shrink-0 flex items-center gap-0.5 ${
  change > 0 
    ? 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-950/30' 
    : change < 0 
      ? 'text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-950/30'
      : 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-950/30'
}`}>
  {change > 0 ? (
    <TrendingUp size={12} className="mr-0.5" />
  ) : change < 0 ? (
    <TrendingDown size={12} className="mr-0.5" />
  ) : (
    <Minus size={12} className="mr-0.5" />
  )}
  {Math.abs(change)}%
</div>
        </div>
      </div>
    );
});

// Collection Card - For top collections by endpoints
const CollectionCard = React.memo(({ collection, colors, onClick }) => {
  return (
    <div 
      className="border rounded-xl p-3 hover:translate-y-[-2px] transition-transform duration-200 cursor-pointer"
      onClick={onClick}
      style={{ borderColor: colors.border, backgroundColor: colors.card }}
    >
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center gap-2 min-w-0">
          <FileCode size={14} style={{ color: colors.textSecondary }} />
          <span className="text-sm font-medium truncate" style={{ color: colors.text }}>
            {collection.name || 'Unnamed Collection'}
          </span>
        </div>
        {collection.favorite && (
          <span className="text-xs" style={{ color: colors.warning }}>★</span>
        )}
      </div>
      
      <div className="grid grid-cols-2 gap-1 text-xs mb-2">
        <div style={{ color: colors.textSecondary }}>
          Type: <span style={{ color: colors.text }}>REST API</span>
        </div>
        <div style={{ color: colors.textSecondary }}>
          Endpoints: <span style={{ color: colors.text }}>{collection.requestsCount || 0}</span>
        </div>
      </div>
      
      <div className="flex items-center justify-between text-xs">
        <span style={{ color: colors.textSecondary }}>
          Owner: <span style={{ color: colors.text }}>{collection.owner || 'System'}</span>
        </span>
      </div>
    </div>
  );
});

// API Endpoint Item
const ApiEndpointItem = React.memo(({ api, colors, isDark, onClick }) => {
  const methodColorClass = getMethodColor(api?.method, isDark);
  
  return (
    <div 
      className="group p-3 cursor-pointer transition-all hover:bg-gray-50 dark:hover:bg-gray-800/50 rounded-lg"
      onClick={() => onClick(api)}
    >
      <div className="flex items-start gap-3">
        <span className={`px-2 py-1 rounded text-xs font-medium ${methodColorClass}`}>
          {api.method || 'GET'}
        </span>
        <div className="flex-1 min-w-0">
          <div className="flex items-center justify-between">
            <h4 className="text-sm font-medium truncate" style={{ color: colors.text }}>
              {api.name}
            </h4>
            <span className="text-xs ml-2" style={{ color: colors.textTertiary }}>
              {api.folderName}
            </span>
          </div>
          
          <p className="text-xs mt-0.5 truncate" style={{ color: colors.textSecondary }}>
            {api.description || api.url}
          </p>
          
          <div className="text-xs mt-1.5" style={{ color: colors.textTertiary }}>
            {api.collectionName}
          </div>
        </div>
      </div>
    </div>
  );
});

// API Generation Card
const ApiGenerationCard = React.memo(({ colors, onGenerate }) => (
  <div className="mb-2 w-full lg:w-full">
    <div 
      className="relative overflow-hidden border rounded-xl cursor-pointer group transition-all duration-200 hover:translate-y-[-2px]"
      onClick={onGenerate}
      style={{ borderColor: colors.border, backgroundColor: colors.bg }}
    >
      <div className="absolute inset-0 bg-gradient-to-r from-blue-500/5 to-purple-500/5 dark:from-blue-500/10 dark:to-purple-500/10" />
      
      <div className="relative p-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-lg bg-gradient-to-br from-blue-500 to-purple-600">
            <ZapIcon size={20} className="text-white" />
          </div>
          <div>
            <h3 className="text-base font-bold mb-0.5" style={{ color: colors.text }}>
              Generate New API
            </h3>
            <p className="text-xs" style={{ color: colors.textSecondary }}>
              Create APIs from your database
            </p>
          </div>
        </div>
        
        <button
          onClick={(e) => { e.stopPropagation(); onGenerate(); }}
          className="bg-gradient-to-r from-blue-500 to-purple-600 text-sm font-medium text-white px-4 py-2 rounded-lg transition-all hover:scale-105"
        >
          Generate
        </button>
      </div>
      
      <div className="px-4 py-2 border-t flex gap-4 text-xs" style={{ borderColor: colors.border }}>
        <span style={{ color: colors.textSecondary }}>✓ REST & GraphQL</span>
        <span style={{ color: colors.textSecondary }}>✓ Auto-documentation</span>
        <span style={{ color: colors.textSecondary }}>✓ Built-in security</span>
      </div>
    </div>
  </div>
));

// Search Input Component
const SearchInput = React.memo(({ value, onChange, onClear, colors, placeholder = "Search..." }) => (
  <div className="flex items-center gap-2 px-3 py-2 rounded-lg border" style={{ borderColor: colors.border, backgroundColor: colors.inputBg }}>
    <SearchIcon size={14} style={{ color: colors.textSecondary }} />
    <input
      type="text"
      placeholder={placeholder}
      className="flex-1 bg-transparent outline-none text-sm"
      style={{ color: colors.text }}
      value={value}
      onChange={onChange}
    />
    {value && (
      <button onClick={onClear} className="p-1 rounded" style={{ backgroundColor: colors.hover }}>
        <X size={12} style={{ color: colors.textSecondary }} />
      </button>
    )}
  </div>
));

// Right Sidebar Component
const RightSidebar = React.memo(({ colors, isDark, isVisible, onClose, onNavigate, onGenerate, statsData }) => {
  const navigationItems = useMemo(() => [
    { label: 'Schema Browser', icon: FileCode, onClick: () => onNavigate('schema-browser'), color: colors.primary },
    { label: 'API Collections', icon: Database, onClick: () => onNavigate('api-collections'), color: colors.success },
    { label: 'API Documentation', icon: BookOpen, onClick: () => onNavigate('api-docs'), color: colors.info },
    { label: 'API Code Base', icon: Code, onClick: () => onNavigate('code-base'), color: colors.warning },
    { label: 'API Security', icon: Shield, onClick: () => onNavigate('security'), color: colors.error },
    { label: 'User Management', icon: UserCog, onClick: () => onNavigate('user-mgt'), color: colors.accentPurple },
  ], [colors, onNavigate]);

  // Format stats data for display
  const stats = useMemo(() => {
    if (!statsData?.data) return null;
    return statsData.data;
  }, [statsData]);

  // Format date for display
  const formattedDate = useMemo(() => {
    if (!stats?.lastUpdated) return 'N/A';
    try {
      const date = new Date(stats.lastUpdated);
      return date.toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return 'N/A';
    }
  }, [stats?.lastUpdated]);

  return (
    <div className={`w-full md:w-80 border-l flex flex-col fixed md:relative inset-y-0 right-0 z-40 transform transition-transform duration-300 ease-in-out ${
      isVisible ? 'translate-x-0' : 'translate-x-full md:translate-x-0'
    }`} style={{ 
      backgroundColor: colors.sidebar,
      borderColor: colors.border,
      height: '100vh',
      top: 0,
      backdropFilter: isDark ? 'blur(10px)' : 'none',
      boxShadow: isDark ? '-4px 0 20px rgba(0, 0, 0, 0.3)' : '-4px 0 20px rgba(0, 0, 0, 0.05)'
    }}>
      <div className="p-4 border-b flex items-center justify-between" style={{ 
        borderColor: colors.border,
        background: `linear-gradient(90deg, ${colors.primary}10, transparent)`
      }}>
        <div className="flex items-center gap-2">
          <div className="p-1.5 rounded-lg" style={{ backgroundColor: `${colors.primary}20` }}>
            <Sliders size={14} style={{ color: colors.primary }} />
          </div>
          <h3 className="text-sm font-semibold uppercase" style={{ color: colors.text }}>Quick Actions</h3>
        </div>
        <button onClick={onClose} className="md:hidden p-1.5 rounded-lg hover:bg-opacity-50 transition-colors" style={{ backgroundColor: colors.hover }}>
          <X size={16} style={{ color: colors.text }} />
        </button>
      </div>

      <div className="flex-1 overflow-auto">
        <div className="p-4">
          <div className="space-y-3">
            {/* Navigation Items */}
            {navigationItems.map((item, index) => (
              <button 
                key={index}
                onClick={item.onClick} 
                className="w-full px-3 py-2.5 rounded-lg text-sm flex items-center gap-3 transition-all duration-200 hover:translate-x-1 group"
                style={{ color: colors.text, backgroundColor: colors.hover }}
              >
                <div className="p-1.5 rounded-md" style={{ backgroundColor: `${item.color}20` }}>
                  <item.icon size={14} style={{ color: item.color }} />
                </div>
                <span className="flex-1 text-left">{item.label}</span>
                <ChevronRight size={14} className="opacity-0 group-hover:opacity-100 transition-opacity" style={{ color: colors.textSecondary }} />
              </button>
            ))}

            <br />

            {/* Stats Card - appears right after navigation items */}
            {stats && (
              <div className="mt-6 border-t">
                {/* Simple header */}
                <div className="pb-4 pt-4 pr-4 border-b flex items-center justify-between" style={{ 
                  borderColor: colors.border,
                  background: `linear-gradient(90deg, ${colors.primary}10, transparent)`
                }}>
                  <div className="flex items-center gap-2">
                    <div className="p-1.5 rounded-lg" style={{ backgroundColor: `${colors.primary}20` }}>
                      <Sliders size={14} style={{ color: colors.primary }} />
                    </div>
                    <h3 className="text-sm font-semibold uppercase" style={{ color: colors.text }}>System Statistics</h3>
                  </div>
                  <button onClick={onClose} className="md:hidden p-1.5 rounded-lg hover:bg-opacity-50 transition-colors" style={{ backgroundColor: colors.hover }}>
                    <X size={16} style={{ color: colors.text }} />
                  </button>
                </div>
                
                {/* Stats Card */}
                <div 
                  className="border rounded-xl overflow-hidden"
                  style={{ borderColor: colors.border, backgroundColor: colors.card }}
                >
                  <div className="p-4 space-y-4">
                    {/* Stats rows - each on its own line */}
                    <div className="space-y-2">
                      {/* Security */}
                      <div className="flex items-center justify-between py-1.5">
                        <div className="flex items-center gap-2">
                          <Shield size={14} style={{ color: colors.warning }} />
                          <span className="text-xs" style={{ color: colors.textSecondary }}>Security Rules</span>
                        </div>
                        <span className="text-xs" style={{ color: colors.text }}>
                            <span style={{ color: colors.success }}>{stats.totalIpWhitelistEntries || 0}</span> IP Whitelisted
                          </span>
                      </div>

                      {/* Security Alert - only if needed */}
                      {stats.unreadSecurityAlerts > 0 && (
                        <div className="flex items-center gap-2 py-1.5 px-2 rounded-lg" style={{ backgroundColor: `${colors.error}15` }}>
                          <AlertCircle size={12} style={{ color: colors.error }} />
                          <span className="text-xs" style={{ color: colors.error }}>
                            {stats.unreadSecurityAlerts} unread alert{stats.unreadSecurityAlerts > 1 ? 's' : ''}
                          </span>
                        </div>
                      )}

                      {/* Code Base */}
                      <div className="flex items-center justify-between py-1.5">
                        <div className="flex items-center gap-2">
                          <Code size={14} style={{ color: colors.info }} />
                          <span className="text-xs" style={{ color: colors.textSecondary }}>Code Base</span>
                        </div>
                        <span className="text-xs" style={{ color: colors.text }}>
                          <span style={{ color: colors.success }}>{stats.totalCodeImplementations?.toLocaleString() || 0}</span> Implementations
                        </span>
                      </div>

                      {/* Users */}
                      <div className="flex items-center justify-between py-1.5">
                        <div className="flex items-center gap-2">
                          <Users size={14} style={{ color: colors.text }} />
                          <span className="text-xs" style={{ color: colors.textSecondary }}>Users</span>
                        </div>
                        <span className="text-xs" style={{ color: colors.text }}>
                          <span style={{ color: colors.success }}>{stats.activeUsers || 0}</span> Active Users
                        </span>
                      </div>

                      {/* Documentation */}
                      <div className="flex items-center justify-between py-1.5">
                        <div className="flex items-center gap-2">
                          <BookOpen size={14} style={{ color: colors.info }} />
                          <span className="text-xs" style={{ color: colors.textSecondary }}>Documentation</span>
                        </div>
                        <span className="text-xs" style={{ color: colors.text }}>
                          <span style={{ color: colors.success }}>{stats.publishedDocumentation || 0}</span> Published
                        </span>
                      </div>
                    </div>

                    {/* Last Updated - subtle separator */}
                    <div className="pt-2 mt-1 border-t" style={{ borderColor: colors.border }}>
                      <div className="flex items-center justify-between">
                        <span className="text-xs" style={{ color: colors.textTertiary }}>Last updated</span>
                        <span className="text-xs" style={{ color: colors.textSecondary }}>{formattedDate}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
});

// ============ MAIN COMPONENT ============
const Dashboard = ({ theme, isDark, toggleTheme, navigateTo, setActiveTab, authToken }) => {
  // State
  const [loading, setLoading] = useState({ initialLoad: true, refresh: false, endpointDetails: false });
  const [tableLoading, setTableLoading] = useState(false); // New state for table loading
  const [apiSearchQuery, setApiSearchQuery] = useState('');
  const [showApiModal, setShowApiModal] = useState(false);
  const [selectedForApiGeneration, setSelectedForApiGeneration] = useState(null);
  
  // Stats state
  const [stats, setStats] = useState({
    totalApis: 0,
    totalDocumentationEndpoints: 0,
    totalCalls: 0,
    totalCollections: 0
  });

  // Add a new state for complete stats data:
  const [completeStats, setCompleteStats] = useState(null);
  
  // Top collections state (first 3 by endpoints)
  const [topCollections, setTopCollections] = useState([]);
  
  // Endpoints pagination state
  const [endpointPage, setEndpointPage] = useState(DEFAULT_PAGE);
  const [endpointsPerPage, setEndpointsPerPage] = useState(DEFAULT_PAGE_SIZE);
  const [endpointFilters, setEndpointFilters] = useState({
    collectionId: null,
    method: null,
    search: ''
  });
  
  // Store paginated endpoint data
  const [endpointData, setEndpointData] = useState({
    content: [],
    pageNumber: 0,
    pageSize: DEFAULT_PAGE_SIZE,
    totalElements: 0,
    totalPages: 0,
    last: true
  });
  
  const [error, setError] = useState(null);
  const [isRightSidebarVisible, setIsRightSidebarVisible] = useState(false);

  // Debounce timer
  const endpointSearchTimer = useRef(null);
  // Flag to prevent multiple simultaneous fetches
  const isFetchingEndpoints = useRef(false);

  const colors = useMemo(() => getColorScheme(isDark), [isDark]);

  // Loading Overlay
  const LoadingOverlay = () => {
    const isLoading = loading.initialLoad || loading.refresh;
    
    if (!isLoading) return null;
    
    return (
      <div className="fixed inset-0 flex items-center justify-center z-50" style={{ backgroundColor: colors.bg }}>
        <div className="text-center">
          <div className="relative">
            <Loader className="animate-spin mx-auto mb-6" size={64} style={{ color: colors.primary }} />
            <div className="absolute inset-0 flex items-center justify-center">
              <LayoutDashboard size={32} style={{ color: colors.primary, opacity: 0.3 }} />
            </div>
          </div>
          <h3 className="text-xl font-semibold mb-2" style={{ color: colors.text }}>
            {loading.initialLoad ? 'Loading Dashboard' : 'Refreshing Dashboard'}
          </h3>
          <p className="text-sm mb-2" style={{ color: colors.textSecondary }}>
            Please wait while we prepare your dashboard data
          </p>
        </div>
      </div>
    );
  };
  
  // ============ DATA FETCHING FUNCTIONS ============

  // Fetch stats
  const fetchStats = useCallback(async () => {
    if (!authToken) return null;

    try {
      const response = await getDashboardStats(authToken);
      const handledResponse = handleDashboardResponse(response);
      
      if (handledResponse?.responseCode === 200 && handledResponse.data) {
        // Store the complete response for the sidebar
        setCompleteStats(handledResponse);
        
        // Store the 4 main stats for the cards
        setStats({
          totalApis: handledResponse.data.totalApis || 0,
          totalDocumentationEndpoints: handledResponse.data.totalDocumentationEndpoints || 0,
          totalCalls: handledResponse.data.totalApiRequests || 0,
          totalCollections: handledResponse.data.totalCollections || 0
        });
      }
    } catch (error) {
      console.error('Error fetching stats:', error);
    }
  }, [authToken]);

  // Fetch top 3 collections by endpoints
  const fetchTopCollections = useCallback(async () => {
    if (!authToken) return;

    try {
      // Get first page with 10 items to find top 3 by endpoints
      const params = {
        page: 0,
        size: 10,
        sortBy: 'name',
        sortDir: 'asc'
      };

      const response = await getDashboardCollections(authToken, params);
      const handledResponse = handleDashboardResponse(response);
      
      if (handledResponse?.responseCode === 200 && handledResponse.data?.content) {
        // Sort by requestsCount (endpoints) in descending order and take top 3
        const sorted = [...handledResponse.data.content]
          .sort((a, b) => (b.requestsCount || 0) - (a.requestsCount || 0))
          .slice(0, 3)
          .map(collection => ({
            id: collection.id,
            name: collection.name || 'Unnamed Collection',
            description: collection.description,
            type: 'REST API',
            favorite: collection.favorite || false,
            requestsCount: collection.requestsCount || 0,
            folderCount: collection.folderCount || 0,
            owner: collection.owner || 'System',
            lastUpdated: collection.lastUpdated
          }));

        setTopCollections(sorted);
      }
    } catch (error) {
      console.error('Error fetching top collections:', error);
    }
  }, [authToken]);

  // Fetch paginated endpoints - FIXED with race condition prevention and loading state
  const fetchEndpoints = useCallback(async (page, size, filters) => {
    if (!authToken) return;
    
    // Prevent multiple simultaneous fetches
    if (isFetchingEndpoints.current) {
      console.log('Already fetching endpoints, skipping...');
      return;
    }

    // Set table loading to true when starting a new fetch (not on initial load)
    if (!loading.initialLoad) {
      setTableLoading(true);
    }

    try {
      isFetchingEndpoints.current = true;
      
      const params = {
        page,
        size,
        sortBy: 'lastUpdated',
        sortDir: 'desc',
        search: filters.search || undefined
      };
      
      if (filters.collectionId) params.collectionId = filters.collectionId;
      if (filters.method) params.method = filters.method;

      console.log(`Fetching endpoints - Page: ${page}, Size: ${size}, Search: ${filters.search}`);
      
      const response = await getDashboardEndpoints(authToken, params);
      const handledResponse = handleDashboardResponse(response);

      // Add this right after setting endpointData in fetchEndpoints (around line 330)
      console.log('API Response - last:', handledResponse.data.last);
      console.log('API Response - totalPages:', handledResponse.data.totalPages);
      console.log('API Response - pageNumber:', handledResponse.data.pageNumber);
      
      if (handledResponse?.responseCode === 200 && handledResponse.data) {
        const transformedContent = (handledResponse.data.content || []).map(endpoint => ({
          id: endpoint.id,
          apiId: endpoint.apiId,
          name: endpoint.name || 'Unnamed API',
          description: endpoint.description,
          method: endpoint.method || 'GET',
          url: endpoint.url,
          status: 'active',
          owner: endpoint.owner || 'System',
          collectionId: endpoint.collectionId,
          collectionName: endpoint.collectionName,
          folderName: endpoint.folderName,
          lastUpdated: endpoint.lastUpdated,
          parameters: endpoint.parameters || [],
          responseMappings: endpoint.responseMappings || [],
          tags: endpoint.tags || [],
          headers: endpoint.headers || []
        }));

        setEndpointData({
          content: transformedContent,
          pageNumber: handledResponse.data.pageNumber || 0,
          pageSize: handledResponse.data.pageSize || size,
          totalElements: handledResponse.data.totalElements || 0,
          totalPages: handledResponse.data.totalPages || 0,
          last: handledResponse.data.last || false // Change this from true to false
        });
      }
    } catch (error) {
      console.error('Error fetching endpoints:', error);
    } finally {
      isFetchingEndpoints.current = false;
      setTableLoading(false);
    }
  }, [authToken, loading.initialLoad]);

  // FIXED: Previous page handler - DIRECT FETCH WITHOUT STATE DEPENDENCY
  const handlePreviousPage = useCallback(() => {
    if (endpointPage > 0) {
      const newPage = endpointPage - 1;
      console.log('Going to previous page:', newPage);
      
      // Update page state first
      setEndpointPage(newPage);
      
      // Then fetch data with new page (using current filters and size)
      fetchEndpoints(newPage, endpointsPerPage, endpointFilters);
    }
  }, [endpointPage, endpointsPerPage, endpointFilters, fetchEndpoints]);

  // FIXED: Next page handler - DIRECT FETCH WITHOUT STATE DEPENDENCY
  const handleNextPage = useCallback(() => {
    if (!endpointData.last) {
      const newPage = endpointPage + 1;
      console.log('Going to next page:', newPage);
      
      // Update page state first
      setEndpointPage(newPage);
      
      // Then fetch data with new page (using current filters and size)
      fetchEndpoints(newPage, endpointsPerPage, endpointFilters);
    }
  }, [endpointPage, endpointsPerPage, endpointFilters, endpointData.last, fetchEndpoints]);

  // FIXED: Per page change handler
  const handleEndpointsPerPageChange = useCallback((newSize) => {
    console.log('Changing per page to:', newSize);
    
    // Reset to first page
    setEndpointPage(DEFAULT_PAGE);
    setEndpointsPerPage(newSize);
    
    // Fetch with new size and reset page
    fetchEndpoints(DEFAULT_PAGE, newSize, endpointFilters);
  }, [endpointFilters, fetchEndpoints]);

  // FIXED: Search handlers with debounce
  const handleEndpointSearchChange = useCallback((e) => {
    const query = e.target.value;
    setApiSearchQuery(query);
    
    if (endpointSearchTimer.current) {
      clearTimeout(endpointSearchTimer.current);
    }
    
    endpointSearchTimer.current = setTimeout(() => {
      console.log('Searching with query:', query);
      
      // Update filters
      const newFilters = { ...endpointFilters, search: query };
      setEndpointFilters(newFilters);
      
      // Reset to first page
      setEndpointPage(DEFAULT_PAGE);
      
      // Fetch with new filters
      fetchEndpoints(DEFAULT_PAGE, endpointsPerPage, newFilters);
    }, 500);
  }, [endpointFilters, endpointsPerPage, fetchEndpoints]);

  const handleEndpointSearchClear = useCallback(() => {
    setApiSearchQuery('');
    
    const newFilters = { ...endpointFilters, search: '' };
    setEndpointFilters(newFilters);
    setEndpointPage(DEFAULT_PAGE);
    
    fetchEndpoints(DEFAULT_PAGE, endpointsPerPage, newFilters);
  }, [endpointFilters, endpointsPerPage, fetchEndpoints]);

  // FIXED: Refresh handler
  const handleRefresh = useCallback(async () => {
    if (!authToken) return;
    
    setLoading(prev => ({ ...prev, refresh: true }));
    try {
      await Promise.all([
        fetchStats(),
        fetchTopCollections(),
        fetchEndpoints(endpointPage, endpointsPerPage, endpointFilters)
      ]);
    } catch (error) {
      console.error('Error refreshing:', error);
    } finally {
      setLoading(prev => ({ ...prev, refresh: false }));
    }
  }, [authToken, fetchStats, fetchTopCollections, endpointPage, endpointsPerPage, endpointFilters, fetchEndpoints]);

  // FIXED: Load all dashboard data
  const loadAllDashboardData = useCallback(async () => {
    if (!authToken) {
      setError('Authentication required');
      setLoading(prev => ({ ...prev, initialLoad: false }));
      return;
    }

    try {
      await Promise.all([
        fetchStats(),
        fetchTopCollections(),
        fetchEndpoints(DEFAULT_PAGE, endpointsPerPage, endpointFilters)
      ]);
    } catch (error) {
      console.error('Error loading dashboard data:', error);
      setError(error.message);
    } finally {
      setLoading(prev => ({ ...prev, initialLoad: false }));
    }
  }, [authToken, fetchStats, fetchTopCollections, endpointsPerPage, endpointFilters, fetchEndpoints]);

  // REMOVED the problematic useEffect that was causing loops
  // The fetch is now triggered directly by user actions

  // NEW: Function to refresh endpoints table (called after updates)
  const refreshEndpointsTable = useCallback(() => {
    // Keep the current page and filters, just refresh the data
    fetchEndpoints(endpointPage, endpointsPerPage, endpointFilters);
    
    // Also refresh stats and collections to keep everything in sync
    fetchStats();
    fetchTopCollections();
  }, [endpointPage, endpointsPerPage, endpointFilters, fetchEndpoints, fetchStats, fetchTopCollections]);

  // Fetch endpoint details by ID
  const fetchEndpointDetails = useCallback(async (endpointId) => {
    if (!authToken || !endpointId) return null;

    setLoading(prev => ({ ...prev, endpointDetails: true }));
    try {
      // Get all endpoints with a larger page size and filter by ID
      const params = {
        page: 0,
        size: 100,
        sortBy: 'lastUpdated',
        sortDir: 'desc'
      };

      const response = await getDashboardEndpoints(authToken, params);
      const handledResponse = handleDashboardResponse(response);
      
      if (handledResponse?.responseCode === 200 && handledResponse.data?.content) {
        const endpoint = handledResponse.data.content.find(e => e.id === endpointId);
        if (endpoint) {
          return {
            id: endpoint.id,
            name: endpoint.name || 'Unnamed API',
            method: endpoint.method || 'GET',
            description: endpoint.description || '',
            url: endpoint.url || '',
            collectionName: endpoint.collectionName || '',
            collectionId: endpoint.collectionId || '',
            folderName: endpoint.folderName || '',
            parameters: (endpoint.parameters || []).map(p => ({ 
              ...p, 
              id: p.id || `param-${Date.now()}-${Math.random()}` 
            })),
            responseMappings: (endpoint.responseMappings || []).map(m => ({ 
              ...m, 
              id: m.id || `mapping-${Date.now()}-${Math.random()}` 
            })),
            tags: (endpoint.tags || []).map(t => 
              typeof t === 'string' ? { id: `tag-${Date.now()}`, name: t, value: t } : t
            ),
            headers: endpoint.headers || []
          };
        }
      }
      return null;
    } catch (error) {
      console.error('Error fetching endpoint details:', error);
      return null;
    } finally {
      setLoading(prev => ({ ...prev, endpointDetails: false }));
    }
  }, [authToken]);

  // NEW: Handle API update (called from modal after successful update)
  const handleApiUpdate = useCallback(() => {
    // Trigger endpoints table refresh
    refreshEndpointsTable();
  }, [refreshEndpointsTable]);

  const handleEndpointClick = useCallback(async (endpoint) => {
  console.log("endpoint:::::::" + JSON.stringify(endpoint));
  
  // Use the updated function name and pass both authToken and the API ID
  const details = await getGeneratedApiDetails(authToken, endpoint.apiId);
  
  if (details) {
    // The API response has the data wrapped in a 'data' property
    // We need to pass the full response to maintain the structure
    console.log('📦 Received API details for editing:', details);
    setSelectedForApiGeneration(details);  // This already has {data: {...}} structure
  } else {
    // Fallback - but we need to wrap it in a data property to match the API response structure
    setSelectedForApiGeneration({ 
      data: endpoint,  // Wrap in data property to match API response
      requestId: 'local',
      message: 'Local endpoint data',
      responseCode: 200 
    });
  }
  setShowApiModal(true);
}, [authToken]);

  // Handle collection click
  const handleCollectionClick = useCallback((collection) => {
    handleNavigate('api-collections');
  }, [handleNavigate]);

  const handleApiGeneration = useCallback(() => {
    setSelectedForApiGeneration(null);
    setShowApiModal(true);
  }, []);

  const handleCloseModal = useCallback(() => {
    setShowApiModal(false);
    setSelectedForApiGeneration(null);
  }, []);

  const handleNavigate = useCallback((tab) => {
    setActiveTab(tab);
    setIsRightSidebarVisible(false);
  }, [setActiveTab]);

  // ============ MEMOIZED VALUES ============
  const currentPageApis = useMemo(() => endpointData.content || [], [endpointData.content]);

  const totalApiPages = useMemo(() => endpointData.totalPages || 0, [endpointData.totalPages]);

  const startItem = useMemo(() => {
    if (endpointData.totalElements === 0) return 0;
    // Calculate based on current page and page size
    return (endpointPage * endpointsPerPage) + 1;
  }, [endpointPage, endpointsPerPage, endpointData.totalElements]);

  const endItem = useMemo(() => {
    if (endpointData.totalElements === 0) return 0;
    // Calculate based on current page, page size, and total elements
    const calculatedEnd = (endpointPage + 1) * endpointsPerPage;
    return Math.min(calculatedEnd, endpointData.totalElements);
  }, [endpointPage, endpointsPerPage, endpointData.totalElements]);

  const hasData = useMemo(() => 
    stats.totalCollections > 0 || endpointData.totalElements > 0,
    [stats.totalCollections, endpointData.totalElements]
  );


  // Add this to see what values are being used (for debugging)
  useEffect(() => {
    console.log('Pagination info:', {
      endpointPage,
      endpointsPerPage,
      totalElements: endpointData.totalElements,
      startItem,
      endItem,
      last: endpointData.last
    });
  }, [endpointPage, endpointsPerPage, endpointData.totalElements, endpointData.last, startItem, endItem]);

  // ============ EFFECTS ============
  useEffect(() => {
    loadAllDashboardData();
    
    return () => {
      if (endpointSearchTimer.current) clearTimeout(endpointSearchTimer.current);
    };
  }, []); // Empty dependency array - only run once on mount

  // Add this near your other useEffect hooks (around line 550)
useEffect(() => {
  console.log('endpointData updated:', {
    last: endpointData.last,
    pageNumber: endpointData.pageNumber,
    totalPages: endpointData.totalPages,
    totalElements: endpointData.totalElements
  });
}, [endpointData]);

  // Add scrollbar styles
  useEffect(() => {
    const style = document.createElement('style');
    style.innerHTML = `
      ::-webkit-scrollbar { width: 6px; height: 6px; }
      ::-webkit-scrollbar-track { background: ${isDark ? 'rgb(51 65 85)' : '#e2e8f0'}; }
      ::-webkit-scrollbar-thumb { background: ${isDark ? 'rgb(100 116 139)' : '#94a3b8'}; border-radius: 4px; }
    `;
    document.head.appendChild(style);
    return () => style.remove();
  }, [isDark]);

  // ============ RENDER ============
  if (error) {
    return (
      <div className="p-4 border rounded-xl m-4" style={{ borderColor: colors.error, backgroundColor: `${colors.error}20` }}>
        <div className="flex items-center gap-2">
          <AlertCircle size={16} style={{ color: colors.error }} />
          <div style={{ color: colors.error }}>{error}</div>
        </div>
        <button onClick={loadAllDashboardData} className="mt-2 px-3 py-1 rounded text-sm" style={{ backgroundColor: colors.hover, color: colors.text }}>
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-screen relative overflow-hidden" style={{ backgroundColor: colors.bg, color: colors.text }}>
      
      <LoadingOverlay />

      <div className="flex-1 overflow-hidden flex z-20 relative">
        {isRightSidebarVisible && (
          <div className="fixed inset-0 bg-black bg-opacity-50 z-30 md:hidden" onClick={() => setIsRightSidebarVisible(false)} />
        )}

        <RightSidebar 
          colors={colors}
          isDark={isDark}
          isVisible={isRightSidebarVisible}
          onClose={() => setIsRightSidebarVisible(false)}
          onNavigate={handleNavigate}
          onGenerate={handleApiGeneration}
          statsData={completeStats} // Pass the complete stats, not the filtered stats
        />

        <div className="flex-1 overflow-auto p-4 h-full relative z-10">
          <div className="max-w-9xl mx-auto px-4">
            {/* Header */}
            <div className="flex items-center justify-between mb-6">
              <div>
                <h1 className="text-xl font-bold" style={{ color: colors.text }}>Dashboard</h1>
                <p className="text-sm" style={{ color: colors.textSecondary }}>Overview of your API platform</p>
              </div>
              <div className="flex items-center gap-3">
                <button 
                  onClick={handleRefresh} 
                  className="p-2 rounded-lg transition-colors hover:bg-opacity-50"
                  style={{ backgroundColor: colors.hover }} 
                  disabled={loading.refresh}
                >
                  <RefreshCw size={16} className={loading.refresh ? 'animate-spin' : ''} style={{ color: colors.text }} />
                </button>
                <button 
                  onClick={() => handleNavigate('schema-browser')} 
                  className="px-3 py-2 bg-gradient-to-r from-blue-500 to-purple-600 rounded text-sm font-medium text-white hover:translate-y-[-2px] transition-transform"
                >
                  Schema Browser
                </button>
              </div>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
              {STAT_CARDS.map(({ key, icon, label, colorKey }) => (
                <StatCard
                  key={key}
                  title={label}
                  value={stats[key] || 0}
                  change={stats[key] || 0}
                  icon={icon}
                  color={colors[colorKey]}
                  onClick={() => {
                    if (key === 'totalCollections' || key === 'totalApis') {
                      handleNavigate('api-collections');
                    } else if (key === 'totalCalls') {
                      handleNavigate('api-requests');
                    } else if (key === 'totalDocumentationEndpoints') {
                      handleNavigate('api-docs');
                    }
                  }}
                  colors={colors}
                />
              ))}
            </div>

            {hasData ? (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Left Column - Top Collections */}
                <div className="flex flex-col gap-6">
                  <ApiGenerationCard colors={colors} onGenerate={handleApiGeneration} />

                  <div className="border rounded-xl" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
                    <div className="p-4 border-b" style={{ borderColor: colors.border }}>
                      <h3 className="text-sm font-semibold" style={{ color: colors.text }}>
                        Top 3 Collections by Endpoints
                      </h3>
                    </div>
                    
                    <div>
                      {topCollections.length > 0 ? (
                        <div className="p-4 space-y-3">
                          {topCollections.map(collection => (
                            <CollectionCard 
                              key={collection.id} 
                              collection={collection} 
                              colors={colors}
                              onClick={() => handleCollectionClick(collection)}
                            />
                          ))}
                        </div>
                      ) : (
                        <div className="p-8 text-center">
                          <DatabaseIcon size={32} style={{ color: colors.textTertiary }} className="mx-auto mb-2" />
                          <div className="text-sm font-medium mb-1" style={{ color: colors.text }}>
                            No collections available
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                </div>

                {/* Right Column - Endpoints */}
                <div className="flex flex-col gap-6">
                  <div className="border rounded-xl" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
                    <div className="p-4 border-b" style={{ borderColor: colors.border }}>
                      <div className="flex flex-col gap-3">
                        <div className="flex items-center justify-between">
                          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>
                            Recently Generated Endpoints
                          </h3>
                          <select
                            value={endpointsPerPage}
                            onChange={(e) => handleEndpointsPerPageChange(Number(e.target.value))}
                            className="text-xs px-2 py-1 rounded border bg-transparent"
                            style={{ borderColor: colors.border, background: colors.bg, color: colors.text }}
                          >
                            {PAGINATION_OPTIONS.map(opt => (
                              <option key={opt} value={opt}>{opt} per page</option>
                            ))}
                          </select>
                        </div>
                        
                        <SearchInput
                          value={apiSearchQuery}
                          onChange={handleEndpointSearchChange}
                          onClear={handleEndpointSearchClear}
                          colors={colors}
                          placeholder="Search endpoints..."
                        />
                      </div>
                    </div>
                    
                    <div>
                      {tableLoading ? (
                        <TableLoader colors={colors} />
                      ) : currentPageApis.length > 0 ? (
                        <div className="p-4 space-y-2">
                          {currentPageApis.map(api => (
                            <ApiEndpointItem 
                              key={api.id} 
                              api={api} 
                              colors={colors}
                              isDark={isDark}
                              onClick={handleEndpointClick}
                            />
                          ))}
                        </div>
                      ) : (
                        <div className="p-8 text-center">
                          <SearchIcon size={32} style={{ color: colors.textTertiary }} className="mx-auto mb-2" />
                          <div className="text-sm font-medium mb-1" style={{ color: colors.text }}>
                            {apiSearchQuery ? 'No endpoints found' : 'No endpoints available'}
                          </div>
                          {apiSearchQuery && (
                            <button onClick={handleEndpointSearchClear} className="mt-2 px-3 py-1.5 rounded text-xs" style={{ backgroundColor: colors.hover, color: colors.text }}>
                              Clear search
                            </button>
                          )}
                        </div>
                      )}
                    </div>
                    
                    {endpointData.totalElements > 0 && (
                      <div className="flex items-center justify-between p-3 border-t" style={{ borderColor: colors.border }}>
                        <div className="text-xs" style={{ color: colors.textSecondary }}>
                          Showing {startItem} - {endItem} of {endpointData.totalElements}
                        </div>
                        <div className="flex items-center gap-2">
                          <button
                            onClick={handlePreviousPage}
                            disabled={endpointPage === 0 || tableLoading}
                            className="px-3 py-1.5 rounded text-sm font-medium disabled:opacity-30 hover:bg-opacity-50 transition-colors flex items-center gap-1"
                            style={{ 
                              backgroundColor: endpointPage === 0 || tableLoading ? 'transparent' : colors.hover, 
                              color: colors.text,
                              cursor: endpointPage === 0 || tableLoading ? 'not-allowed' : 'pointer'
                            }}
                          >
                            <ChevronLeft size={16} />
                            <span>Prev</span>
                          </button>

                          <span className="text-sm px-3 py-1.5 rounded" style={{ backgroundColor: colors.hover, color: colors.text }}>
                            {endpointPage + 1} / {totalApiPages}
                          </span>

                          <button
                            onClick={handleNextPage}
                            disabled={endpointData.last || tableLoading}
                            className="px-3 py-1.5 rounded text-sm font-medium disabled:opacity-30 hover:bg-opacity-50 transition-colors flex items-center gap-1"
                            style={{ 
                              backgroundColor: endpointData.last || tableLoading ? 'transparent' : colors.hover, 
                              color: colors.text,
                              cursor: endpointData.last || tableLoading ? 'not-allowed' : 'pointer'
                            }}
                          >
                            <span>Next</span>
                            <ChevronRightIcon size={16} />
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-center p-8 border rounded-xl" style={{ borderColor: colors.border }}>
                <Database size={48} className="mx-auto mb-4" style={{ color: colors.textSecondary }} />
                <div className="text-sm" style={{ color: colors.text }}>No dashboard data available</div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Mobile Bottom Nav */}
      <div className="md:hidden fixed bottom-0 left-0 right-0 border-t p-2" style={{ borderColor: colors.border, backgroundColor: colors.header, zIndex: 40 }}>
        <div className="grid grid-cols-4">
          <button onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })} className="flex flex-col items-center p-1" style={{ color: colors.textSecondary }}>
            <Home size={16} /><span className="text-xs mt-0.5">Home</span>
          </button>
          <button onClick={() => handleNavigate('schema-browser')} className="flex flex-col items-center p-1" style={{ color: colors.textSecondary }}>
            <Database size={16} /><span className="text-xs mt-0.5">Schema</span>
          </button>
          <button onClick={handleApiGeneration} className="flex flex-col items-center p-1" style={{ color: colors.textSecondary }}>
            <FileCode size={16} /><span className="text-xs mt-0.5">APIs</span>
          </button>
          <button onClick={() => setIsRightSidebarVisible(true)} className="flex flex-col items-center p-1" style={{ color: colors.textSecondary }}>
            <Settings size={16} /><span className="text-xs mt-0.5">More</span>
          </button>
        </div>
      </div>
      <div className="md:hidden h-16"></div>

      {/* API Generation Modal */}
      {showApiModal && (
        <Suspense fallback={null}>
          <ApiGenerationModal
            isOpen={showApiModal}
            onClose={handleCloseModal}
            selectedObject={selectedForApiGeneration}
            colors={colors}
            theme={theme}
            fromDashboard={true}
            onGenerateAPI={handleApiUpdate}
            authToken={authToken}
            isEditing={!!selectedForApiGeneration?.data?.id} // Check for data.id, not just id
          />
        </Suspense>
      )}
    </div>
  );
};

export default Dashboard;