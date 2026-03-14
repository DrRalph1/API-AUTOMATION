// Dashboard.jsx - FIXED VERSION
import React, { useState, useEffect, useCallback, useMemo, lazy, Suspense } from 'react';
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

// Import only what's needed
import { getComprehensiveDashboard } from "../controllers/DashboardController.js";

// ============ CONSTANTS & CONFIG ============
const STAT_CARDS = [
  { key: 'totalApis', icon: Database, label: 'Total APIs', colorKey: 'success' },
  { key: 'totalDocumentationEndpoints', icon: FileCode, label: 'API Documentation', colorKey: 'info' },
  { key: 'totalCalls', icon: Activity, label: 'API Requests', colorKey: 'primaryDark' },
  { key: 'totalCollections', icon: FileCode, label: 'API Collections', colorKey: 'info' }
];

const API_PAGINATION_OPTIONS = [5, 8, 10, 15, 20];

const RECENT_ACTIVITY_ITEMS = [
  { action: 'API Generated', name: 'User API', time: '2 min ago', icon: Zap, colorKey: 'success' },
  { action: 'Collection Updated', name: 'Payment API', time: '15 min ago', icon: Database, colorKey: 'info' },
  { action: 'Security Scan', name: 'Completed', time: '1 hour ago', icon: Shield, colorKey: 'accentPurple' }
];

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

// Stat Card - Memoized - FIXED: Added colors prop
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


// Connection Card - Memoized
const ConnectionCard = React.memo(({ connection, colors, onClick }) => {
  const getStatusColor = (status) => {
    const statusMap = {
      active: colors.connectionOnline,
      idle: colors.warning,
      offline: colors.connectionOffline
    };
    return statusMap[status] || colors.textSecondary;
  };

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
            {connection.name || 'Unnamed Connection'}
          </span>
        </div>
        <div className="w-2 h-2 rounded-full shrink-0" style={{ backgroundColor: getStatusColor(connection.status) }} />
      </div>
      
      <div className="grid grid-cols-2 gap-1 text-xs mb-2">
        <div style={{ color: colors.textSecondary }}>
          Type: <span style={{ color: colors.text }}>{connection.type || 'N/A'}</span>
        </div>
        <div style={{ color: colors.textSecondary }}>
          Endpoints: <span style={{ color: colors.text }}>{connection.endpoints || 0}</span>
        </div>
      </div>
      
      <div className="flex items-center justify-between text-xs">
        <span style={{ color: colors.textSecondary }}>
          Owner: <span style={{ color: colors.text }}>{connection.owner || 'System'}</span>
        </span>
      </div>
    </div>
  );
});

// API Endpoint Item - Memoized
const ApiEndpointItem = React.memo(({ api, colors, isDark, onEdit }) => {
  const methodColorClass = getMethodColor(api?.method, isDark);
  
  return (
    <div 
      className="group p-3 cursor-pointer transition-all hover:bg-gray-50 dark:hover:bg-gray-800/50 rounded-lg"
      onClick={() => onEdit(api)}
    >
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
  );
});

// API Generation Card - Simplified
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
const SearchInput = React.memo(({ value, onChange, onClear, colors }) => (
  <div className="flex items-center gap-2 px-3 py-2 rounded-lg border" style={{ borderColor: colors.border, backgroundColor: colors.inputBg }}>
    <SearchIcon size={14} style={{ color: colors.textSecondary }} />
    <input
      type="text"
      placeholder="Search APIs..."
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
const RightSidebar = React.memo(({ colors, isDark, isVisible, onClose, onNavigate, onGenerate }) => {
  const navigationItems = useMemo(() => [
    { label: 'Schema Browser', icon: FileCode, onClick: () => onNavigate('schema-browser'), color: colors.primary },
    { label: 'API Collections', icon: Database, onClick: () => onNavigate('api-collections'), color: colors.success },
    { label: 'API Documentation', icon: BookOpen, onClick: () => onNavigate('api-docs'), color: colors.info },
    { label: 'API Code Base', icon: Code, onClick: () => onNavigate('code-base'), color: colors.warning },
    { label: 'API Security', icon: Shield, onClick: () => onNavigate('security'), color: colors.error },
    { label: 'User Management', icon: UserCog, onClick: () => onNavigate('user-mgt'), color: colors.accentPurple }
  ], [colors, onNavigate]);

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
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Quick Actions</h3>
        </div>
        <button onClick={onClose} className="md:hidden p-1.5 rounded-lg hover:bg-opacity-50 transition-colors" style={{ backgroundColor: colors.hover }}>
          <X size={16} style={{ color: colors.text }} />
        </button>
      </div>

      <div className="flex-1 overflow-auto">
        <div className="p-4">
          <div className="space-y-1">
            <div className="text-xs font-medium mb-2 px-2" style={{ color: colors.textSecondary }}>
              NAVIGATION
            </div>
            
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
          </div>

          <div className="mt-6 pt-4 border-t" style={{ borderColor: colors.border }}>
            <div className="flex items-center justify-between mb-3 px-2">
              <span className="text-xs font-medium" style={{ color: colors.textSecondary }}>RECENT ACTIVITY</span>
              <RefreshCw size={12} style={{ color: colors.textTertiary }} />
            </div>
            
            <div className="space-y-3">
              {RECENT_ACTIVITY_ITEMS.map((item, index) => {
                const Icon = item.icon;
                const color = colors[item.colorKey];
                return (
                  <div key={index} className="flex items-start gap-3 px-3 py-2 rounded-lg hover:translate-y-[-2px] transition-transform cursor-pointer" style={{ backgroundColor: colors.hover }}>
                    <div className="p-1.5 rounded-md shrink-0" style={{ backgroundColor: `${color}20` }}>
                      <Icon size={12} style={{ color }} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-medium truncate" style={{ color: colors.text }}>{item.action}</span>
                        <span className="text-xs shrink-0" style={{ color: colors.textTertiary }}>{item.time}</span>
                      </div>
                      <span className="text-xs truncate" style={{ color: colors.textSecondary }}>{item.name}</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
});

// ============ MAIN COMPONENT ============
const Dashboard = ({ theme, isDark, toggleTheme, navigateTo, setActiveTab, authToken }) => {
  // State with safe initial values
  const [loading, setLoading] = useState({ initialLoad: true, refresh: false });
  const [apiSearchQuery, setApiSearchQuery] = useState('');
  const [showApiModal, setShowApiModal] = useState(false);
  const [selectedForApiGeneration, setSelectedForApiGeneration] = useState(null);
  const [apiPage, setApiPage] = useState(1);
  const [apisPerPage, setApisPerPage] = useState(5);
  const [dashboardData, setDashboardData] = useState({
    stats: { 
      totalConnections: 0, 
      activeConnections: 0, 
      totalApis: 0, 
      totalDocumentationEndpoints: 0, 
      totalCalls: 0, 
      totalCollections: 0 
    },
    connections: [],
    apis: [],
    lastUpdated: null,
    generatedFor: ''
  });
  const [error, setError] = useState(null);
  const [isRightSidebarVisible, setIsRightSidebarVisible] = useState(false);

  const colors = useMemo(() => getColorScheme(isDark), [isDark]);


  // Loading Overlay - UPDATED with improved design from sample
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
          {/* <p className="text-xs" style={{ color: colors.textTertiary }}>
            {loading.initialLoad 
              ? 'Fetching API collections, endpoints, and system metrics...' 
              : 'Updating dashboard with latest information...'}
          </p> */}
        </div>
      </div>
    );
  };
  

  // ============ DATA TRANSFORMATION ============
  const transformApiData = useCallback((apiData) => {
    if (!apiData?.data) return dashboardData;
    
    const data = apiData.data;
    const stats = data.stats || {};
    const collections = data.collections?.collections || [];
    
    const connectionsData = (collections || []).map(collection => ({
      id: collection.id,
      name: collection.name || 'Unnamed Collection',
      type: 'REST API',
      status: collection.favorite ? 'active' : 'idle',
      endpoints: collection.requestsCount || 0,
      folders: collection.folderCount || 0,
      owner: collection.owner || 'System',
      lastUpdated: collection.lastUpdated
    }));
    
    const apisData = (data.endpoints?.endpoints || []).map(endpoint => ({
      id: endpoint.id,
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
    
    return {
      stats: {
        totalConnections: collections.length,
        activeConnections: collections.filter(c => c.favorite).length,
        totalApis: apisData.length,
        totalDocumentationEndpoints: stats.totalDocumentationEndpoints || 0,
        totalCalls: stats.totalApis || apisData.length * 500,
        totalCollections: collections.length
      },
      connections: connectionsData,
      apis: apisData,
      lastUpdated: data.generatedAt || new Date().toISOString(),
      generatedFor: data.generatedFor || 'User'
    };
  }, []);

  // ============ DATA FETCHING ============
  const fetchDashboardData = useCallback(async () => {
    if (!authToken) {
      setError('Authentication required');
      setLoading(prev => ({ ...prev, initialLoad: false }));
      return;
    }

    try {
      const response = await getComprehensiveDashboard(authToken);
      if (response?.responseCode === 200) {
        const transformedData = transformApiData(response);
        setDashboardData(transformedData);
      } else {
        throw new Error(response?.message || 'Failed to load dashboard');
      }
    } catch (error) {
      console.error('Error fetching dashboard:', error);
      setError(error.message);
    } finally {
      setLoading(prev => ({ ...prev, initialLoad: false }));
    }
  }, [authToken, transformApiData]);

  // Refresh handler
  const handleRefresh = useCallback(async () => {
    if (!authToken) return;
    
    setLoading(prev => ({ ...prev, refresh: true }));
    try {
      const response = await getComprehensiveDashboard(authToken);
      if (response?.responseCode === 200) {
        const transformedData = transformApiData(response);
        setDashboardData(transformedData);
      }
    } catch (error) {
      console.error('Error refreshing:', error);
    } finally {
      setLoading(prev => ({ ...prev, refresh: false }));
    }
  }, [authToken, transformApiData]);

  // ============ EVENT HANDLERS ============
  const handleApiGeneration = useCallback(() => {
    setSelectedForApiGeneration(null);
    setShowApiModal(true);
  }, []);

  const handleEditApi = useCallback((api) => {
    setSelectedForApiGeneration({
      id: api.id,
      name: api.name || 'Unnamed API',
      method: api.method || 'GET',
      description: api.description || '',
      url: api.url || '',
      collectionName: api.collectionName || '',
      collectionId: api.collectionId || '',
      folderName: api.folderName || '',
      parameters: (api.parameters || []).map(p => ({ ...p, id: p.id || `param-${Date.now()}-${Math.random()}` })),
      responseMappings: (api.responseMappings || []).map(m => ({ ...m, id: m.id || `mapping-${Date.now()}-${Math.random()}` })),
      tags: (api.tags || []).map(t => typeof t === 'string' ? { id: `tag-${Date.now()}`, name: t, value: t } : t),
      headers: api.headers || []
    });
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

  // Search handlers
  const handleSearchChange = useCallback((e) => {
    setApiSearchQuery(e.target.value);
    setApiPage(1);
  }, []);

  const handleClearSearch = useCallback(() => {
    setApiSearchQuery('');
    setApiPage(1);
  }, []);

  // ============ MEMOIZED VALUES ============
  const filteredApis = useMemo(() => {
    const apis = dashboardData.apis || [];
    if (!apiSearchQuery.trim()) return apis;
    
    const query = apiSearchQuery.toLowerCase().trim();
    return apis.filter(api => 
      (api.name?.toLowerCase().includes(query)) ||
      (api.description?.toLowerCase().includes(query)) ||
      (api.method?.toLowerCase().includes(query)) ||
      (api.collectionName?.toLowerCase().includes(query)) ||
      (api.url?.toLowerCase().includes(query)) ||
      (api.owner?.toLowerCase().includes(query))
    );
  }, [dashboardData.apis, apiSearchQuery]);

  const currentPageApis = useMemo(() => {
    const startIndex = (apiPage - 1) * apisPerPage;
    return (filteredApis || []).slice(startIndex, startIndex + apisPerPage);
  }, [filteredApis, apiPage, apisPerPage]);

  const totalApiPages = useMemo(() => 
    Math.ceil((filteredApis?.length || 0) / apisPerPage), 
    [filteredApis?.length, apisPerPage]
  );

  const hasData = useMemo(() => 
    (dashboardData.stats?.totalConnections || 0) > 0 || (dashboardData.apis?.length || 0) > 0,
    [dashboardData.stats?.totalConnections, dashboardData.apis?.length]
  );

  // ============ EFFECTS ============
  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

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
        <button onClick={fetchDashboardData} className="mt-2 px-3 py-1 rounded text-sm" style={{ backgroundColor: colors.hover, color: colors.text }}>
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-screen relative overflow-hidden" style={{ backgroundColor: colors.bg, color: colors.text }}>
      
      <LoadingOverlay isLoading={loading.initialLoad} colors={colors} />

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
                  value={dashboardData.stats?.[key] || 0}
                  change={dashboardData.stats?.[key] || 0}
                  icon={icon}
                  color={colors[colorKey]}
                  onClick={() => key === 'totalApis' && handleNavigate('api-collections') || 
                    key === 'totalCollections' && handleNavigate('api-collections') || 
                    key === 'totalCalls' && handleNavigate('code-base') || 
                    key === 'totalDocumentationEndpoints' && handleNavigate('api-docs')}
                  colors={colors}
                />
              ))}
            </div>

            {hasData ? (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Left Column */}
                <div className="flex flex-col gap-6">
                  <ApiGenerationCard colors={colors} onGenerate={handleApiGeneration} />

                  {(dashboardData.connections?.length || 0) > 0 && (
                    <div className="border rounded-xl" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
                      <div className="p-4 border-b" style={{ borderColor: colors.border }}>
                        <div className="flex items-center justify-between">
                          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>API Collections</h3>
                          <span className="text-xs" style={{ color: colors.textSecondary }}>{dashboardData.connections?.length || 0} collections</span>
                        </div>
                      </div>
                      <div className="p-4">
                        <div className="space-y-3">
                          {(dashboardData.connections || []).slice(0, 3).map(conn => (
                            <ConnectionCard 
                              key={conn.id} 
                              connection={conn} 
                              colors={colors}
                              onClick={() => handleNavigate('api-collections')}
                            />
                          ))}
                        </div>
                      </div>
                    </div>
                  )}
                </div>

                {/* Right Column */}
                <div className="flex flex-col gap-6">
                  <div className="border rounded-xl" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
                    <div className="p-4 border-b" style={{ borderColor: colors.border }}>
                      <div className="flex flex-col gap-3">
                        <div className="flex items-center justify-between">
                          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Recently Generated APIs</h3>
                          <select
                            value={apisPerPage}
                            onChange={(e) => {
                              setApisPerPage(Number(e.target.value));
                              setApiPage(1);
                            }}
                            className="text-xs px-2 py-1 rounded border bg-transparent"
                            style={{ borderColor: colors.border, background: colors.bg, color: colors.text }}
                          >
                            {API_PAGINATION_OPTIONS.map(opt => (
                              <option key={opt} value={opt}>{opt} per page</option>
                            ))}
                          </select>
                        </div>
                        
                        <SearchInput
                          value={apiSearchQuery}
                          onChange={handleSearchChange}
                          onClear={handleClearSearch}
                          colors={colors}
                        />
                      </div>
                    </div>
                    
                    <div>
                      {currentPageApis.length > 0 ? (
                        <div className="p-4 space-y-2">
                          {currentPageApis.map(api => (
                            <ApiEndpointItem 
                              key={api.id} 
                              api={api} 
                              colors={colors}
                              isDark={isDark}
                              onEdit={handleEditApi}
                            />
                          ))}
                        </div>
                      ) : (
                        <div className="p-8 text-center">
                          <SearchIcon size={32} style={{ color: colors.textTertiary }} className="mx-auto mb-2" />
                          <div className="text-sm font-medium mb-1" style={{ color: colors.text }}>
                            {apiSearchQuery ? 'No APIs found' : 'No API endpoints available'}
                          </div>
                          {apiSearchQuery && (
                            <button onClick={handleClearSearch} className="mt-2 px-3 py-1.5 rounded text-xs" style={{ backgroundColor: colors.hover, color: colors.text }}>
                              Clear search
                            </button>
                          )}
                        </div>
                      )}
                    </div>
                    
                    {filteredApis.length > 0 && totalApiPages > 1 && (
                      <div className="flex items-center justify-between p-3 border-t" style={{ borderColor: colors.border }}>
                        <div className="text-xs" style={{ color: colors.textSecondary }}>
                          Showing {((apiPage - 1) * apisPerPage) + 1} - {Math.min(apiPage * apisPerPage, filteredApis.length)} of {filteredApis.length}
                        </div>
                        <div className="flex items-center gap-1">
                          <button
                            onClick={() => setApiPage(p => Math.max(1, p - 1))}
                            disabled={apiPage === 1}
                            className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50"
                            style={{ backgroundColor: apiPage === 1 ? 'transparent' : colors.hover, color: colors.text }}
                          >
                            <ChevronLeft size={14} />
                          </button>
                          
                          <span className="text-xs px-2" style={{ color: colors.text }}>
                            {apiPage} / {totalApiPages}
                          </span>
                          
                          <button
                            onClick={() => setApiPage(p => Math.min(totalApiPages, p + 1))}
                            disabled={apiPage === totalApiPages}
                            className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50"
                            style={{ backgroundColor: apiPage === totalApiPages ? 'transparent' : colors.hover, color: colors.text }}
                          >
                            <ChevronRightIcon size={14} />
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

      {/* API Generation Modal - Lazy Loaded */}
      {showApiModal && (
        <Suspense fallback={null}>
          <ApiGenerationModal
            isOpen={showApiModal}
            onClose={handleCloseModal}
            selectedObject={selectedForApiGeneration}
            colors={colors}
            theme={theme}
            onGenerateAPI={() => Promise.resolve({ success: true })}
            authToken={authToken}
            isEditing={!!selectedForApiGeneration?.id}
          />
        </Suspense>
      )}
    </div>
  );
};

export default Dashboard;