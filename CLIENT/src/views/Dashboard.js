// Dashboard.jsx - COMPLETE FIXED VERSION WITH SIMPLE SEARCH
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import {
  Database, FileCode, Activity, Zap, Settings,
  Search, RefreshCw, Plus, CheckCircle, AlertCircle, Users,
  Shield, Download, Edit, Trash2, X, AlertTriangle, Edit2, Copy,
  Table, Code, Loader, BookOpen, UserCog, Rocket,
  Home, ChevronLeft, ChevronRight as ChevronRightIcon, ChevronRight,
  LayoutDashboard, Sliders, Sparkles, Wand2, Zap as ZapIcon,
  SearchIcon, Database as DatabaseIcon, FileCode as FileCodeIcon,
  ShieldCheck as ShieldCheckIcon, Sparkles as SparklesIcon
} from "lucide-react";

// Import DashboardController
import {
  getComprehensiveDashboard,
  handleDashboardResponse
} from "../controllers/DashboardController.js";
import ApiGenerationModal from '@/components/modals/ApiGenerationModal.js';

const Dashboard = ({ theme, isDark, customTheme, toggleTheme, navigateTo, setActiveTab, authToken }) => {
  // Loading states
  const [loading, setLoading] = useState({
    initialLoad: true,
    refresh: false
  });
  
  // Simple search state
  const [apiSearchQuery, setApiSearchQuery] = useState('');

  // Modal states
  const [showApiModal, setShowApiModal] = useState(false);
  const [selectedForApiGeneration, setSelectedForApiGeneration] = useState(null);

  // Pagination for API endpoints
  const [apiPage, setApiPage] = useState(1);
  const [apisPerPage, setApisPerPage] = useState(5);
  
  const [dashboardData, setDashboardData] = useState({
    stats: {
      totalConnections: 0,
      activeConnections: 0,
      totalApis: 0,
      activeApis: 0,
      totalCalls: 0,
      avgLatency: "0ms",
      successRate: "0%",
      uptime: "0%"
    },
    connections: [],
    apis: [],
    schemaStats: {
      tables: 0,
      views: 0,
      procedures: 0,
      functions: 0,
      totalObjects: 0,
      databaseSize: "0 MB",
      databaseName: "",
      version: "",
      monthlyGrowth: 0,
      totalObjectsChange: 0,
      tableChange: 0,
      viewChange: 0,
      procedureChange: 0,
      functionChange: 0
    },
    codeGenerationSummary: {},
    systemHealth: {},
    lastUpdated: null,
    generatedFor: ''
  });

  const [error, setError] = useState(null);

  // Mobile state
  const [isRightSidebarVisible, setIsRightSidebarVisible] = useState(false);
  const [isMobileSearchOpen, setIsMobileSearchOpen] = useState(false);

  // Modal stack
  const [modalStack, setModalStack] = useState([]);

  // Handle API generation
  const handleApiGeneration = useCallback(() => {
    setSelectedForApiGeneration(null);
    setShowApiModal(true);
  }, []);

  // Handle edit API
  // Handle edit API
const handleEditApi = useCallback((api) => {
  const apiForEditing = {
    id: api.id,
    name: api.name,
    type: 'API',
    method: api.method,
    description: api.description,
    url: api.url,
    collectionName: api.collectionName,
    collectionId: api.collectionId,
    folderName: api.folderName,
    
    // Parameters and response mappings
    parameters: api.parameters || [],
    responseMappings: api.responseMappings || [],
    
    // API details
    apiName: api.name,
    apiCode: api.id || `API_${api.name.replace(/\s+/g, '_').toUpperCase()}`,
    httpMethod: api.method,
    endpointPath: api.url,
    version: api.version || '1.0.0',
    status: api.status?.toUpperCase() === 'ACTIVE' ? 'ACTIVE' : 'DRAFT',
    owner: api.owner || 'HR',
    tags: api.tags || ['default'],
    
    // Request/Response configs
    requestBody: api.requestBody || {
      bodyType: 'json',
      sample: '{\n  "success": true,\n  "data": {}\n}',
      requiredFields: [],
      validateSchema: true,
      maxSize: 1048576,
      allowedMediaTypes: ['application/json']
    },
    
    responseBody: api.responseBody || {
      successSchema: '{\n  "success": true,\n  "data": {},\n  "message": "Request processed successfully"\n}',
      errorSchema: '{\n  "success": false,\n  "error": {\n    "code": "ERROR_CODE",\n    "message": "Error description",\n    "details": {}\n  }\n}',
      includeMetadata: true,
      metadataFields: ['timestamp', 'apiVersion', 'requestId'],
      contentType: 'application/json',
      compression: 'gzip'
    },
    
    // Auth config
    authConfig: api.authConfig || {
      authType: 'none',
      apiKeyHeader: 'X-API-Key',
      apiSecretHeader: 'X-API-Secret',
      jwtIssuer: 'api.example.com',
      rateLimitRequests: 100,
      rateLimitPeriod: 'minute',
      enableRateLimiting: false,
      corsOrigins: ['*'],
      auditLevel: 'standard'
    },
    
    // Headers
    headers: api.headers || [
      { id: '1', key: 'Content-Type', value: 'application/json', required: true, description: 'Response content type' },
      { id: '2', key: 'Cache-Control', value: 'no-cache', required: false, description: 'Cache control header' }
    ],
    
    // Tests
    tests: api.tests || {
      testConnection: true,
      testObjectAccess: true,
      testPrivileges: true,
      testDataTypes: true,
      testNullConstraints: true,
      testUniqueConstraints: false,
      testForeignKeyReferences: false,
      testQueryPerformance: true,
      performanceThreshold: 1000,
      testWithSampleData: true,
      sampleDataRows: 10,
      testProcedureExecution: true,
      testFunctionReturn: true,
      testExceptionHandling: true,
      testSQLInjection: true,
      testAuthentication: true,
      testAuthorization: true,
      testData: '',
      testQueries: []
    },
    
    // Settings
    settings: api.settings || {
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
    },
    
    // Collection info
    collectionInfo: {
      collectionName: api.collectionName,
      collectionId: api.collectionId,
      folderName: api.folderName
    },
    
    // Schema config
    schemaConfig: api.schemaConfig || {
      schemaName: 'HR',
      objectType: 'TABLE',
      objectName: api.name?.replace(/\s+/g, '_').toUpperCase() || 'OBJECT',
      operation: api.method === 'GET' ? 'SELECT' : 
                api.method === 'POST' ? 'INSERT' :
                api.method === 'PUT' ? 'UPDATE' :
                api.method === 'DELETE' ? 'DELETE' : 'SELECT',
      primaryKeyColumn: '',
      sequenceName: '',
      enablePagination: true,
      pageSize: 10,
      enableSorting: true,
      defaultSortColumn: '',
      defaultSortDirection: 'ASC'
    }
  };

  setSelectedForApiGeneration(apiForEditing);
  setShowApiModal(true);
}, []);

  // Handle generate from modal
  const handleGenerateAPIFromModal = useCallback(async () => {
    setShowApiModal(false);
    setSelectedForApiGeneration(null);
    return { success: true };
  }, []);

  // Color scheme
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
    success: 'rgb(52 211 153)',
    warning: 'rgb(251 191 36)',
    error: 'rgb(248 113 113)',
    info: 'rgb(96 165 250)',
    tabActive: 'rgb(96 165 250)',
    tabInactive: 'rgb(148 163 184)',
    sidebarActive: 'rgb(96 165 250)',
    sidebarhover: 'rgb(45 46 72 / 33%)',
    inputBg: 'rgb(41 53 72 / 19%)',
    inputborder: 'rgb(51 65 85 / 19%)',
    tableHeader: 'rgb(41 53 72 / 19%)',
    tableRow: 'rgb(41 53 72 / 19%)',
    tableRowhover: 'rgb(45 46 72 / 33%)',
    dropdownBg: 'rgb(41 53 72 / 19%)',
    dropdownborder: 'rgb(51 65 85 / 19%)',
    modalBg: 'rgb(41 53 72 / 19%)',
    modalborder: 'rgb(51 65 85 / 19%)',
    codeBg: 'rgb(41 53 72 / 19%)',
    connectionOnline: 'rgb(52 211 153)',
    connectionOffline: 'rgb(248 113 113)',
    connectionIdle: 'rgb(251 191 36)',
    accentPurple: 'rgb(167 139 250)',
    accentPink: 'rgb(244 114 182)',
    accentCyan: 'rgb(34 211 238)',
    gradient: 'from-blue-500/20 via-violet-500/20 to-orange-500/20'
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
    codeBg: '#f1f5f9',
    connectionOnline: '#10b981',
    connectionOffline: '#ef4444',
    connectionIdle: '#f59e0b',
    accentPurple: '#a78bfa',
    accentPink: '#f472b6',
    accentCyan: '#22d3ee',
    gradient: 'from-blue-400/20 via-violet-400/20 to-orange-400/20'
  };

  // Modal functions
  const openModal = (type, data) => {
    setModalStack(prev => [...prev, { type, data }]);
  };

  const closeModal = () => {
    setModalStack(prev => prev.slice(0, -1));
  };

  // Get time ago
  const getTimeAgo = (date) => {
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) return `${diffInSeconds} seconds ago`;
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} minutes ago`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} hours ago`;
    return `${Math.floor(diffInSeconds / 86400)} days ago`;
  };

  // Transform API data
  const transformApiData = (apiData) => {
    if (!apiData || !apiData.data) return dashboardData;
    
    const data = apiData.data;
    const stats = data.stats || {};
    const collections = data.collections?.collections || [];
    
    const connectionsData = collections.map(collection => ({
      id: collection.id,
      name: collection.name,
      type: 'REST API',
      status: collection.favorite ? 'active' : 'idle',
      version: 'v1',
      endpoints: collection.requestsCount || 0,
      folders: collection.folderCount || 0,
      owner: collection.owner || 'System',
      lastUpdated: collection.lastUpdated || new Date().toISOString(),
      description: collection.description,
      tags: collection.favorite ? ['favorite'] : []
    }));
    
    const endpoints = data.endpoints?.endpoints || [];
    const now = new Date();
    
    const apisData = endpoints.map((endpoint, index) => ({
      id: endpoint.id,
      name: endpoint.name,
      description: endpoint.description,
      method: endpoint.method,
      url: endpoint.url,
      status: 'active',
      version: 'v1',
      calls: Math.floor(Math.random() * 1000) + 100,
      latency: '42ms',
      successRate: '98.5%',
      errors: Math.floor(Math.random() * 10),
      avgResponseTime: '42ms',
      owner: endpoint.owner || 'System',
      collectionId: endpoint.collectionId,
      collectionName: endpoint.collectionName,
      folderName: endpoint.folderName,
      lastUpdated: new Date(now.getTime() - (index * 3600000)).toISOString(),
      timeAgo: getTimeAgo(new Date(now.getTime() - (index * 3600000))),
      parameters: endpoint.parameters || [],
      responseMappings: endpoint.responseMappings || [],
      schemaConfig: endpoint.schemaConfig,
      tags: endpoint.tags || []
    }))
    .sort((a, b) => new Date(b.lastUpdated) - new Date(a.lastUpdated));
    
    const totalApis = endpoints.length;
    const totalCollections = collections.length;
    const totalDocumentationEndpoints = stats.totalDocumentationEndpoints || data.codeGenerationSummary?.totalDocumentationEndpoints || 0;
    
    return {
      stats: {
        totalConnections: totalCollections,
        activeConnections: collections.filter(c => c.favorite).length,
        totalApis: totalApis,
        totalDocumentationEndpoints: totalDocumentationEndpoints,
        activeApis: endpoints.filter(e => e.method).length,
        totalCalls: stats.totalApis || totalApis * 500,
        avgLatency: data.loadBalancers?.performance?.avgResponseTime || '42ms',
        successRate: '98.5%',
        uptime: data.loadBalancers?.performance?.uptime || '99.9%',
        totalCollections: totalCollections
      },
      connections: connectionsData,
      apis: apisData,
      codeGenerationSummary: data.codeGenerationSummary || {},
      systemHealth: data.loadBalancers?.performance || {},
      lastUpdated: data.generatedAt || new Date().toISOString(),
      generatedFor: data.generatedFor || 'User'
    };
  };

  // Fetch dashboard data
  const fetchDashboardData = useCallback(async () => {
    if (!authToken) {
      setError('Authentication required');
      setLoading(prev => ({ ...prev, initialLoad: false }));
      return;
    }

    setLoading(prev => ({ ...prev, initialLoad: true }));
    setError(null);
    
    try {
      const response = await getComprehensiveDashboard(authToken);
      const processedResponse = handleDashboardResponse(response);
      const transformedData = transformApiData(processedResponse);
      
      setDashboardData(prev => ({
        ...prev,
        ...transformedData
      }));
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      setError(`Failed to load dashboard: ${error.message}`);
    } finally {
      setLoading(prev => ({ ...prev, initialLoad: false }));
    }
  }, [authToken]);

  // Refresh dashboard
  const handleRefresh = async () => {
    if (!authToken) return;
    
    setLoading(prev => ({ ...prev, refresh: true }));
    try {
      const response = await getComprehensiveDashboard(authToken);
      const processedResponse = handleDashboardResponse(response);
      
      if (processedResponse.data) {
        const transformedData = transformApiData(processedResponse);
        setDashboardData(prev => ({
          ...prev,
          ...transformedData
        }));
      }
    } catch (error) {
      console.error('Error refreshing dashboard:', error);
      setError(`Refresh failed: ${error.message}`);
    } finally {
      setLoading(prev => ({ ...prev, refresh: false }));
    }
  };

  // Simple search handler
  const handleSearchChange = (e) => {
    setApiSearchQuery(e.target.value);
    setApiPage(1); // Reset to first page when searching
  };

  // Clear search
  const clearSearch = () => {
    setApiSearchQuery('');
    setApiPage(1);
  };

  // Initialize data
  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

  // Navigation handlers
  const handleNavigateToSchemaBrowser = () => {
    setActiveTab('schema-browser');
  };

  const handleNavigateToAPICollection = () => {
    setActiveTab('api-collections');
  };

  const handleNavigateToDocumentation = () => {
    setActiveTab('api-docs');
  };

  const handleNavigateToCodeBase = () => {
    setActiveTab('code-base');
  };

  const handleNavigateToAPISecurity = () => {
    setActiveTab('security');
  };

  const handleNavigateToUserManagement = () => {
    setActiveTab('user-mgt');
  };

  const handleCollectionsClick = () => {
    setActiveTab('api-collections');
  };

  const handleCodeBaseClick = () => {
    setActiveTab('code-base');
  };

  const handleApiCallsClick = () => {
    const apiCallsData = dashboardData.apis?.map(api => ({
      id: api.id,
      name: api.name,
      calls: api.calls || 0,
      latency: api.latency || '0ms',
      successRate: api.successRate || '0%',
      errors: api.errors || 0,
      avgResponseTime: api.avgResponseTime || 'N/A',
      lastUpdated: api.lastUpdated,
      owner: api.owner || 'N/A'
    })) || [];
    
    openModal('apiCalls', {
      title: 'API Calls Analytics',
      data: apiCallsData,
      totalItems: apiCallsData.length
    });
  };

  const handleViewAllConnections = () => {
    setActiveTab('api-collections');
  };

  // Pagination handlers
  const handlePrevApiPage = () => {
    if (apiPage > 1) {
      setApiPage(apiPage - 1);
    }
  };

  const handleNextApiPage = () => {
    const totalApiPages = Math.ceil(filteredApis.length / apisPerPage);
    if (apiPage < totalApiPages) {
      setApiPage(apiPage + 1);
    }
  };

  // Get filtered APIs based on search query - SIMPLE FILTERING
  const filteredApis = useMemo(() => {
    if (!apiSearchQuery.trim()) {
      return dashboardData.apis;
    }
    
    const query = apiSearchQuery.toLowerCase().trim();
    return dashboardData.apis.filter(api => 
      (api.name && api.name.toLowerCase().includes(query)) ||
      (api.description && api.description.toLowerCase().includes(query)) ||
      (api.method && api.method.toLowerCase().includes(query)) ||
      (api.collectionName && api.collectionName.toLowerCase().includes(query)) ||
      (api.url && api.url.toLowerCase().includes(query)) ||
      (api.owner && api.owner.toLowerCase().includes(query))
    );
  }, [dashboardData.apis, apiSearchQuery]);

  // Get current page of filtered APIs
  const getCurrentPageApis = () => {
    const startIndex = (apiPage - 1) * apisPerPage;
    const endIndex = startIndex + apisPerPage;
    return filteredApis.slice(startIndex, endIndex);
  };

  // Helper functions
  const getStatusColor = (status) => {
    switch(status) {
      case 'connected':
      case 'active':
      case 'success':
        return colors.success;
      case 'warning':
      case 'testing':
      case 'pending':
      case 'idle':
        return colors.warning;
      case 'error':
      case 'failed':
      case 'offline':
      case 'disconnected':
        return colors.error;
      default:
        return colors.textSecondary;
    }
  };

  const getDatabaseIcon = (type) => {
    const iconProps = { size: 14, style: { color: colors.textSecondary } };
    return <FileCode {...iconProps} />;
  };

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

  // Stat Card Component
  const StatCard = ({ title, value, icon: Icon, change, color, onClick }) => {
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
          {change && (
            <div className={`text-xs px-2 py-1 rounded-full shrink-0 ${change > 0 ? 'text-green-600 bg-green-100' : 'text-red-600 bg-red-100'}`}>
              {change > 0 ? '+' : ''}{change}%
            </div>
          )}
        </div>
      </div>
    );
  };

  // Connection Card
  const ConnectionCard = ({ connection }) => {
    return (
      <div 
        className="border rounded-xl p-3 hover-lift cursor-pointer transition-all duration-200"
        onClick={handleNavigateToAPICollection}
        style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card,
        }}
      >
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2 min-w-0">
            {getDatabaseIcon(connection.type)}
            <span className="text-sm font-medium truncate" style={{ color: colors.text }}>
              {connection.name}
            </span>
          </div>
          <div className="flex items-center gap-1 shrink-0">
            <div className="w-2 h-2 rounded-full" style={{ backgroundColor: getStatusColor(connection.status) }} />
          </div>
        </div>
        
        <div className="grid grid-cols-2 gap-1 text-xs mb-2">
          <div style={{ color: colors.textSecondary }}>
            Type: <span style={{ color: colors.text }}>{connection.type}</span>
          </div>
          <div style={{ color: colors.textSecondary }}>
            Endpoints: <span style={{ color: colors.text }}>{connection.endpoints}</span>
          </div>
        </div>
        
        <div className="flex items-center justify-between text-xs">
          <span style={{ color: colors.textSecondary }}>
            Owner: <span style={{ color: colors.text }}>{connection.owner}</span>
          </span>
        </div>
      </div>
    );
  };

  // API Endpoint Item
const ApiEndpointItem = ({ api }) => {
  const methodColors = {
    GET: 'text-emerald-600 bg-emerald-50 dark:bg-emerald-950/30 dark:text-emerald-400',
    POST: 'text-blue-600 bg-blue-50 dark:bg-blue-950/30 dark:text-blue-400',
    PUT: 'text-amber-600 bg-amber-50 dark:bg-amber-950/30 dark:text-amber-400',
    DELETE: 'text-rose-600 bg-rose-50 dark:bg-rose-950/30 dark:text-rose-400'
  };

  return (
    <div 
      className="group p-3 cursor-pointer transition-all hover:bg-gray-50 dark:hover:bg-gray-800/50 rounded-lg"
      onClick={() => handleEditApi(api)}
    >
      <div className="flex items-start gap-3">
        <span className={`px-2 py-1 text-xs font-mono rounded-md ${methodColors[api.method] || methodColors.GET}`}>
          {api.method}
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
};

  // API Generation Card - IMPROVED DESIGN - Positioned on left side only
  const ApiGenerationCard = () => {
    return (
      <div className="mb-2 w-full lg:w-full">
        <div 
          className="relative overflow-hidden border rounded-xl cursor-pointer group transition-all duration-200 hover-lift"
          onClick={(e) => {
                e.stopPropagation();
                handleApiGeneration();
              }}
          style={{ 
            borderColor: colors.border,
            backgroundColor: colors.bg,
          }}
        >
          {/* Simple gradient background - adjusted for light/dark mode */}
          <div className="absolute inset-0 bg-gradient-to-r from-blue-500/5 to-purple-500/5 dark:from-blue-500/10 dark:to-purple-500/10"></div>
          
          {/* Content */}
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
                  Create APIs from your database in seconds
                </p>
              </div>
            </div>
            
            <button
              onClick={(e) => {
                e.stopPropagation();
                handleApiGeneration();
              }}
              className="bg-gradient-to-r from-blue-500 to-purple-600 text-sm font-medium text-white px-4 py-2 rounded-lg transition-all hover:scale-105"
            >
              Generate
            </button>
          </div>
          
          {/* Simple features row */}
          <div className="px-4 py-2 border-t flex gap-4 text-xs" style={{ borderColor: colors.border }}>
            <span style={{ color: colors.textSecondary }}>✓ REST & GraphQL</span>
            <span style={{ color: colors.textSecondary }}>✓ Auto-documentation</span>
            <span style={{ color: colors.textSecondary }}>✓ Built-in security</span>
          </div>
        </div>
      </div>
    );
};

  // API Pagination Component
  const ApiPagination = () => {
    const totalApiPages = Math.ceil(filteredApis.length / apisPerPage);
    
    if (totalApiPages <= 1) return null;
    
    return (
      <div className="flex items-center justify-between p-3 border-t" style={{ borderColor: colors.border }}>
        <div className="text-xs" style={{ color: colors.textSecondary }}>
          Showing {((apiPage - 1) * apisPerPage) + 1} - {Math.min(apiPage * apisPerPage, filteredApis.length)} of {filteredApis.length}
        </div>
        <div className="flex items-center gap-1">
          <button
            onClick={handlePrevApiPage}
            disabled={apiPage === 1}
            className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50"
            style={{ 
              backgroundColor: apiPage === 1 ? 'transparent' : colors.hover,
              color: colors.text,
            }}
          >
            <ChevronLeft size={14} />
          </button>
          
          <span className="text-xs px-2" style={{ color: colors.text }}>
            {apiPage} / {totalApiPages}
          </span>
          
          <button
            onClick={handleNextApiPage}
            disabled={apiPage === totalApiPages}
            className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50"
            style={{ 
              backgroundColor: apiPage === totalApiPages ? 'transparent' : colors.hover,
              color: colors.text,
            }}
          >
            <ChevronRightIcon size={14} />
          </button>
        </div>
      </div>
    );
  };

  // Right Sidebar
  const RightSidebar = () => (
  <div className={`w-full md:w-80 border-l flex flex-col fixed md:relative inset-y-0 right-0 z-40 transform transition-transform duration-300 ease-in-out ${
    isRightSidebarVisible ? 'translate-x-0' : 'translate-x-full md:translate-x-0'
  }`} style={{ 
    backgroundColor: colors.sidebar,
    borderColor: colors.border,
    height: '100vh',
    top: 0,
    backdropFilter: isDark ? 'blur(10px)' : 'none',
    boxShadow: isDark ? '-4px 0 20px rgba(0, 0, 0, 0.3)' : '-4px 0 20px rgba(0, 0, 0, 0.05)'
  }}>
    {/* Header with gradient */}
    <div className="p-4 border-b flex items-center justify-between" style={{ 
      borderColor: colors.border,
      background: `linear-gradient(90deg, ${colors.primary}10, transparent)`
    }}>
      <div className="flex items-center gap-2">
        <div className="p-1.5 rounded-lg" style={{ backgroundColor: `${colors.primary}20` }}>
          <Sliders size={14} style={{ color: colors.primary }} />
        </div>
        <h3 className="text-sm font-semibold" style={{ color: colors.text }}>
          Quick Actions
        </h3>
      </div>
      <button 
        onClick={() => setIsRightSidebarVisible(false)} 
        className="md:hidden p-1.5 rounded-lg hover:bg-opacity-50 transition-colors"
        style={{ backgroundColor: colors.hover }}
      >
        <X size={16} style={{ color: colors.text }} />
      </button>
    </div>

    <div className="flex-1 overflow-auto">
      <div className="p-4">
        {/* Main Actions */}
        <div className="space-y-1">
          <div className="text-xs font-medium mb-2 px-2" style={{ color: colors.textSecondary }}>
            NAVIGATION
          </div>
          
          <button 
            onClick={handleNavigateToSchemaBrowser} 
            className="w-full px-3 py-2.5 rounded-lg text-sm flex items-center gap-3 transition-all duration-200 hover:translate-x-1 group"
            style={{ 
              color: colors.text,
              backgroundColor: colors.hover,
            }}
          >
            <div className="p-1.5 rounded-md" style={{ backgroundColor: `${colors.primary}20` }}>
              <FileCode size={14} style={{ color: colors.primary }} />
            </div>
            <span className="flex-1 text-left">Schema Browser</span>
            <ChevronRight size={14} className="opacity-0 group-hover:opacity-100 transition-opacity" style={{ color: colors.textSecondary }} />
          </button>

          <button 
            onClick={handleNavigateToAPICollection} 
            className="w-full px-3 py-2.5 rounded-lg text-sm flex items-center gap-3 transition-all duration-200 hover:translate-x-1 group"
            style={{ 
              color: colors.text,
              backgroundColor: colors.hover,
            }}
          >
            <div className="p-1.5 rounded-md" style={{ backgroundColor: `${colors.success}20` }}>
              <Database size={14} style={{ color: colors.success }} />
            </div>
            <span className="flex-1 text-left">API Collections</span>
            <ChevronRight size={14} className="opacity-0 group-hover:opacity-100 transition-opacity" style={{ color: colors.textSecondary }} />
          </button>

          <button 
            onClick={handleNavigateToDocumentation} 
            className="w-full px-3 py-2.5 rounded-lg text-sm flex items-center gap-3 transition-all duration-200 hover:translate-x-1 group"
            style={{ 
              color: colors.text,
              backgroundColor: colors.hover,
            }}
          >
            <div className="p-1.5 rounded-md" style={{ backgroundColor: `${colors.info}20` }}>
              <BookOpen size={14} style={{ color: colors.info }} />
            </div>
            <span className="flex-1 text-left">API Documentation</span>
            <ChevronRight size={14} className="opacity-0 group-hover:opacity-100 transition-opacity" style={{ color: colors.textSecondary }} />
          </button>

          <button 
            onClick={handleNavigateToCodeBase} 
            className="w-full px-3 py-2.5 rounded-lg text-sm flex items-center gap-3 transition-all duration-200 hover:translate-x-1 group"
            style={{ 
              color: colors.text,
              backgroundColor: colors.hover,
            }}
          >
            <div className="p-1.5 rounded-md" style={{ backgroundColor: `${colors.warning}20` }}>
              <Code size={14} style={{ color: colors.warning }} />
            </div>
            <span className="flex-1 text-left">API Code Base</span>
            <ChevronRight size={14} className="opacity-0 group-hover:opacity-100 transition-opacity" style={{ color: colors.textSecondary }} />
          </button>

          <button 
            onClick={handleNavigateToAPISecurity} 
            className="w-full px-3 py-2.5 rounded-lg text-sm flex items-center gap-3 transition-all duration-200 hover:translate-x-1 group"
            style={{ 
              color: colors.text,
              backgroundColor: colors.hover,
            }}
          >
            <div className="p-1.5 rounded-md" style={{ backgroundColor: `${colors.error}20` }}>
              <Shield size={14} style={{ color: colors.error }} />
            </div>
            <span className="flex-1 text-left">API Security</span>
            <ChevronRight size={14} className="opacity-0 group-hover:opacity-100 transition-opacity" style={{ color: colors.textSecondary }} />
          </button>

          <button 
            onClick={handleNavigateToUserManagement} 
            className="w-full px-3 py-2.5 rounded-lg text-sm flex items-center gap-3 transition-all duration-200 hover:translate-x-1 group"
            style={{ 
              color: colors.text,
              backgroundColor: colors.hover,
            }}
          >
            <div className="p-1.5 rounded-md" style={{ backgroundColor: `${colors.accentPurple}20` }}>
              <UserCog size={14} style={{ color: colors.accentPurple }} />
            </div>
            <span className="flex-1 text-left">User Management</span>
            <ChevronRight size={14} className="opacity-0 group-hover:opacity-100 transition-opacity" style={{ color: colors.textSecondary }} />
          </button>
        </div>

        {/* Recent Activity */}
        <div className="mt-6 pt-4 border-t" style={{ borderColor: colors.border }}>
          <div className="flex items-center justify-between mb-3 px-2">
            <span className="text-xs font-medium" style={{ color: colors.textSecondary }}>RECENT ACTIVITY</span>
            <RefreshCw size={12} style={{ color: colors.textTertiary }} />
          </div>
          
          <div className="space-y-3">
            {[
              { action: 'API Generated', name: 'User API', time: '2 min ago', icon: Zap, color: colors.success },
              { action: 'Collection Updated', name: 'Payment API', time: '15 min ago', icon: Database, color: colors.info },
              { action: 'Security Scan', name: 'Completed', time: '1 hour ago', icon: Shield, color: colors.accentPurple }
            ].map((item, index) => {
              const Icon = item.icon;
              return (
                <div key={index} className="flex items-start gap-3 px-3 py-2 rounded-lg hover-lift cursor-pointer transition-all" style={{ backgroundColor: colors.hover }}>
                  <div className="p-1.5 rounded-md shrink-0" style={{ backgroundColor: `${item.color}20` }}>
                    <Icon size={12} style={{ color: item.color }} />
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

        {/* Quick Generate Button */}
        {/* <div className="mt-6 pt-4 border-t" style={{ borderColor: colors.border }}>
          <button
            onClick={handleApiGeneration}
            className="w-full px-4 py-3 rounded-lg text-sm font-medium flex items-center justify-center gap-2 transition-all duration-300 hover-lift relative overflow-hidden group"
            style={{ 
              background: `linear-gradient(135deg, ${colors.primary}, ${colors.accentPurple})`,
              color: 'white'
            }}
          >
            <span className="absolute inset-0 bg-white/20 transform -translate-x-full group-hover:translate-x-full transition-transform duration-700"></span>
            <Zap size={14} />
            <span>Generate New API</span>
            <Sparkles size={12} className="opacity-0 group-hover:opacity-100 transition-opacity" />
          </button>
        </div> */}
      </div>
    </div>
  </div>
);

  // Error state
  if (error) {
    return (
      <div className="p-4 border rounded-xl" style={{ borderColor: colors.error, backgroundColor: `${colors.error}20` }}>
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

  const hasData = dashboardData.stats.totalConnections > 0 || dashboardData.apis?.length > 0;
  const currentPageApis = getCurrentPageApis();

  return (
    <div className="flex flex-col h-screen relative overflow-hidden" style={{ backgroundColor: colors.bg, color: colors.text }}>
      
      <LoadingOverlay />

      <style>{`
        .hover-lift:hover {
          transform: translateY(-2px);
          transition: transform 0.2s ease;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        ::-webkit-scrollbar { width: 6px; height: 6px; }
        ::-webkit-scrollbar-track { background: ${isDark ? 'rgb(51 65 85)' : '#e2e8f0'}; }
        ::-webkit-scrollbar-thumb { background: ${isDark ? 'rgb(100 116 139)' : '#94a3b8'}; border-radius: 4px; }
      `}</style>

      <div className="flex-1 overflow-hidden flex z-20 relative">
        {isRightSidebarVisible && (
          <div className="fixed inset-0 bg-black bg-opacity-50 z-30 md:hidden" onClick={() => setIsRightSidebarVisible(false)} />
        )}

        <RightSidebar />

        <div className="flex-1 overflow-auto p-4 h-full relative z-10">
          <div className="max-w-9xl mx-auto px-4">
            {/* Header */}
            <div className="flex items-center justify-between mb-6">
              <div>
                <h1 className="text-xl font-bold" style={{ color: colors.text }}>Dashboard</h1>
                <p className="text-sm" style={{ color: colors.textSecondary }}>Overview of your API platform</p>
              </div>
              <div className="flex items-center gap-3">
                {/* <button onClick={handleRefresh} className="p-2 rounded-lg" style={{ backgroundColor: colors.hover }} disabled={loading.refresh}>
                  <RefreshCw size={16} className={loading.refresh ? 'animate-spin' : ''} style={{ color: colors.text }} />
                </button> */}
                <button onClick={handleNavigateToSchemaBrowser} className="px-3 py-2 bg-gradient-to-r from-blue-500 to-purple-600 rounded text-sm font-medium text-white hover-lift">
                  Schema Browser
                </button>
              </div>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
              <StatCard title="Total APIs" value={dashboardData.stats.totalApis} icon={Database} change={5} color={colors.success} onClick={handleViewAllConnections} />
              <StatCard title="API Documentation" value={dashboardData.stats.totalDocumentationEndpoints} icon={FileCode} change={12} color={colors.info} onClick={handleCodeBaseClick} />
              <StatCard title="API Requests" value={dashboardData.stats.totalCalls.toLocaleString()} icon={Activity} change={8.5} color={colors.primaryDark} onClick={handleApiCallsClick} />
              <StatCard title="API Collections" value={dashboardData.stats.totalCollections} icon={FileCode} change={12} color={colors.info} onClick={handleCollectionsClick} />
            </div>

            {hasData ? (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Left Column - Contains API Generation Card and API Collections */}
                <div className="flex flex-col gap-6">
                  
                  {/* API Generation Card - Positioned on left side only */}
                  <ApiGenerationCard />

                  {/* API Collections */}
                  {dashboardData.connections?.length > 0 && (
                    <div className="border rounded-xl" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
                      <div className="p-4 border-b" style={{ borderColor: colors.border }}>
                        <div className="flex items-center justify-between">
                          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>API Collections</h3>
                          <span className="text-xs" style={{ color: colors.textSecondary }}>{dashboardData.connections.length} collections</span>
                        </div>
                      </div>
                      <div className="p-4">
                        <div className="space-y-3">
                          {dashboardData.connections.slice(0, 3).map(conn => (
                            <ConnectionCard key={conn.id} connection={conn} />
                          ))}
                        </div>
                      </div>
                    </div>
                  )}
                </div>

                {/* Right Column - Recently Generated APIs with Search */}
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
                            <option value={5}>5 per page</option>
                            <option value={8}>8 per page</option>
                            <option value={10}>10 per page</option>
                          </select>
                        </div>
                        
                        {/* SIMPLE SEARCH INPUT - Just a plain input field */}
                        <div className="flex items-center gap-2 px-3 py-2 rounded-lg border" style={{ borderColor: colors.border, backgroundColor: colors.inputBg }}>
                          <SearchIcon size={14} style={{ color: colors.textSecondary }} />
                          <input
                            type="text"
                            placeholder="Search APIs..."
                            className="flex-1 bg-transparent outline-none text-sm"
                            style={{ color: colors.text }}
                            value={apiSearchQuery}
                            onChange={handleSearchChange}
                          />
                          {apiSearchQuery && (
                            <button onClick={clearSearch} className="p-1 rounded" style={{ backgroundColor: colors.hover }}>
                              <X size={12} style={{ color: colors.textSecondary }} />
                            </button>
                          )}
                        </div>
                      </div>
                    </div>
                    
                    <div>
                      {currentPageApis.length > 0 ? (
                        <div className="p-4 space-y-2">
                          {currentPageApis.map(api => (
                            <ApiEndpointItem key={api.id} api={api} />
                          ))}
                        </div>
                      ) : (
                        <div className="p-8 text-center">
                          <SearchIcon size={32} style={{ color: colors.textTertiary }} className="mx-auto mb-2" />
                          <div className="text-sm font-medium mb-1" style={{ color: colors.text }}>
                            {apiSearchQuery ? 'No APIs found' : 'No API endpoints available'}
                          </div>
                          {apiSearchQuery && (
                            <button onClick={clearSearch} className="mt-2 px-3 py-1.5 rounded text-xs" style={{ backgroundColor: colors.hover, color: colors.text }}>
                              Clear search
                            </button>
                          )}
                        </div>
                      )}
                    </div>
                    
                    {filteredApis.length > 0 && <ApiPagination />}
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
          <button onClick={handleNavigateToSchemaBrowser} className="flex flex-col items-center p-1" style={{ color: colors.textSecondary }}>
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
        <ApiGenerationModal
          isOpen={showApiModal}
          onClose={() => {
            setShowApiModal(false);
            setSelectedForApiGeneration(null);
          }}
          selectedObject={selectedForApiGeneration}
          colors={colors}
          theme={theme}
          onGenerateAPI={handleGenerateAPIFromModal}
          authToken={authToken}
          isEditing={!!selectedForApiGeneration?.id}
        />
      )}
    </div>
  );
};

export default Dashboard;