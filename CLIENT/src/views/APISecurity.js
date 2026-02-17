import React, { useState, useEffect, useCallback } from 'react';
import {
  Shield, ShieldCheck, ShieldAlert, ShieldOff, ShieldPlus, ShieldMinus, ShieldEllipsis,
  Lock, LockOpen, Key, KeyRound, Fingerprint, Scan, QrCode, ScanFace, Eye, EyeOff,
  Users, UserCheck, UserCog, UserX, UserMinus, UserPlus,
  Network, Server, ServerCog, ServerCrash, ServerOff,
  Globe, GlobeLock, Earth, EarthLock,
  Activity, Zap, ZapOff, Battery, BatteryCharging,
  Filter, FilterX, Funnel, FunnelX,
  Cpu, HardDrive, MemoryStick, BarChart3, PieChart,
  Clock, Calendar, CalendarDays, Timer, TimerReset,
  AlertCircle, AlertTriangle, AlertOctagon, CheckCircle, XCircle, Info,
  Settings, Sliders, ToggleLeft, ToggleRight, Power, PowerOff,
  Download, Upload, RefreshCw, Plus, Minus, X, Check, Copy,
  Search, Filter as FilterIcon, MoreVertical, Edit2, Trash2,
  ExternalLink, Link, Link2, Unlink,
  MessageSquare, MessageCircle, Bell, BellRing, BellOff,
  FileText, FileCode, Database, Layers,
  ChevronDown, ChevronRight, ChevronUp, ChevronLeft,
  Home, Cloud, Wifi, Bluetooth, Radio, Satellite,
  TrendingUp, TrendingDown, TrendingUpDown,
  BarChart, LineChart, AreaChart,
  Hash, HashIcon, Type, Code, Terminal,
  Sun, Moon, Settings as SettingsIcon,
  Shield as ShieldIcon,
  ShieldHalf, ShieldQuestion,
  Bell as BellIcon,
  Database as DatabaseIcon,
  Cpu as CpuIcon,
  Network as NetworkIcon,
  Users as UsersIcon,
  Clock as ClockIcon,
  Calendar as CalendarIcon,
  Activity as ActivityIcon,
  Zap as ZapIcon,
  Lock as LockIcon,
  Key as KeyIcon,
  Filter as FilterIcon2,
  Globe as GlobeIcon,
  Server as ServerIcon,
  FileText as FileTextIcon,
  Layers as LayersIcon,
  BarChart as BarChartIcon,
  LineChart as LineChartIcon,
  AreaChart as AreaChartIcon,
  LayoutDashboard,
  Shield as ShieldCheckIcon,
  DownloadCloud,
  UploadCloud,
  Printer,
  Inbox,
  Archive,
  Trash,
  RefreshCw as RefreshIcon,
  ChevronsLeft,
  ChevronsRight,
  GripVertical,
  Coffee,
  Eye as EyeIcon,
  FileArchive as FileBinary,
  Database as DatabaseIcon2,
  ChevronsUpDown,
  Book,
  File,
  MessageSquare as MessageSquareIcon,
  Tag,
  Calendar as CalendarIcon2,
  Hash as HashIcon2,
  Link as LinkIcon,
  Eye as EyeOpenIcon,
  Clock as ClockIcon2,
  Users as UsersIcon2,
  Database as DatabaseIcon3,
  Code as CodeIcon2,
  Terminal as TerminalIcon2,
  ExternalLink as ExternalLinkIcon,
  Copy as CopyIcon,
  Check as CheckIcon,
  X as XIcon,
  AlertCircle as AlertCircleIcon,
  Info as InfoIcon,
  HelpCircle as HelpCircleIcon,
  Star as StarIcon,
  Book as BookIcon,
  Zap as ZapIcon2
} from 'lucide-react';

// Import APISecurityController functions
import {
  getRateLimitRules,
  getIPWhitelist,
  getLoadBalancers,
  getSecurityEvents,
  getSecuritySummary,
  addRateLimitRule,
  addIPWhitelistEntry,
  addLoadBalancer,
  updateRuleStatus,
  updateIPWhitelistEntry,
  deleteRule,
  generateSecurityReport,
  runSecurityScan,
  getSecurityConfiguration,
  updateSecurityConfiguration,
  getSecurityAlerts,
  exportSecurityData,
  handleSecurityResponse,
  extractRateLimitRules,
  extractIPWhitelist,
  extractLoadBalancers,
  extractSecurityEvents,
  extractSecuritySummary,
  extractSecurityScanResults,
  extractUpdateIPWhitelistResponse,
  extractSecurityConfiguration,
  extractSecurityAlerts,
  extractSecurityReportResults,
  extractExportSecurityResults,
  validateUpdateIPWhitelistEntry,
  validateAddRateLimitRule,
  validateAddIPWhitelistEntry,
  validateAddLoadBalancer,
  validateGenerateSecurityReport,
  validateUpdateRuleStatus,
  validateExportSecurityData,
  getSecurityScoreColor,
  getSecurityScoreLabel,
  getSeverityColor,
  formatIPRange,
  calculateTotalSecurityRequests,
  getEndpointProtectionPercentage,
  filterSecurityEventsBySeverity,
  sortSecurityEventsByDate,
  getRecentSecurityAlerts,
  getUnreadSecurityAlertsCount,
  markAlertAsRead,
  markAllAlertsAsRead,
  getDefaultSecurityConfiguration,
  getSecurityEventTypes,
  getLoadBalancingAlgorithms,
  cacheSecurityData,
  getCachedSecurityData,
  clearCachedSecurityData,
  updateRateLimitRule,
  extractUpdateRateLimitRuleResponse,
  validateUpdateRateLimitRule
} from "@/controllers/APISecurityController.js";

const APISecurity = ({ theme, isDark, customTheme, toggleTheme, authToken }) => {
  const [activeTab, setActiveTab] = useState('ip-whitelist');
  const [searchQuery, setSearchQuery] = useState('');
  
  // Pagination states for IP whitelist
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(5);
  const [totalPages, setTotalPages] = useState(1);
  
  // Modal states
  const [showAddRuleModal, setShowAddRuleModal] = useState(false);
  const [showSecurityReportModal, setShowSecurityReportModal] = useState(false);
  const [showIPWhitelistModal, setShowIPWhitelistModal] = useState(false);
  const [showLoadBalancerModal, setShowLoadBalancerModal] = useState(false);
  const [showScanModal, setShowScanModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [modalDetails, setModalDetails] = useState(null);
  
  const [toast, setToast] = useState(null);
  const [expandedSections, setExpandedSections] = useState(['rate-limits', 'ip-whitelist']);
  const [showNotifications, setShowNotifications] = useState(false);

  const [editingIPEntry, setEditingIPEntry] = useState(null);
  const [showEditIPWhitelistModal, setShowEditIPWhitelistModal] = useState(false);
  
  // New state for editing rate limit rules
  const [editingRule, setEditingRule] = useState(null);
  const [showEditRuleModal, setShowEditRuleModal] = useState(false);
  
  // API States
  const [loading, setLoading] = useState({
    rateLimits: false,
    ipWhitelist: false,
    loadBalancers: false,
    securityEvents: false,
    summary: false,
    report: false,
    scan: false,
    togglingIP: false,
    downloadingPDF: false,
    initialLoad: true // Add initial load state
  });
  
  const [error, setError] = useState({
    rateLimits: null,
    ipWhitelist: null,
    loadBalancers: null,
    securityEvents: null,
    summary: null,
    report: null
  });

  // Colors (same as your original code)
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
    security: {
      high: 'rgb(52 211 153)',
      medium: 'rgb(251 191 36)',
      low: 'rgb(248 113 113)',
      critical: 'rgb(220 38 38)'
    },
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
    security: {
      high: '#10b981',
      medium: '#f59e0b',
      low: '#ef4444',
      critical: '#dc2626'
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

  // State for API data
  const [rateLimitRules, setRateLimitRules] = useState([]);
  const [ipWhitelist, setIpWhitelist] = useState([]);
  const [loadBalancers, setLoadBalancers] = useState([]);
  const [securityEvents, setSecurityEvents] = useState([]);
  const [securitySummary, setSecuritySummary] = useState({
    totalEndpoints: 0,
    securedEndpoints: 0,
    vulnerableEndpoints: 0,
    blockedRequests: 0,
    throttledRequests: 0,
    avgResponseTime: '0ms',
    securityScore: 0,
    lastScan: ''
  });
  const [securityAlerts, setSecurityAlerts] = useState([]);
  const [securityConfig, setSecurityConfig] = useState(null);
  const [scanResults, setScanResults] = useState(null);
  const [reportResults, setReportResults] = useState(null);
  
  // Form states
  const [newRuleData, setNewRuleData] = useState({
    name: '',
    description: '',
    endpoint: '',
    method: 'GET',
    limit: 100,
    window: '1m',
    burst: 20,
    action: 'throttle',
    options: {}
  });
  
  const [newIPEntryData, setNewIPEntryData] = useState({
    name: '',
    ipRange: '',
    description: '',
    endpoints: '',
    options: {}
  });
  
  const [newLoadBalancerData, setNewLoadBalancerData] = useState({
    name: '',
    algorithm: 'round_robin',
    healthCheck: '',
    healthCheckInterval: '30s',
    servers: [],
    options: {}
  });
  
  const [userId, setUserId] = useState('');

  // Extract userId from token
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

  const showToast = (message, type = 'info') => {
    console.log(`[APISecurity] Toast: ${message} (${type})`);
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  // Initialize data on component mount
  useEffect(() => {
    console.log('🔥 [APISecurity] Component mounted, authToken:', !!authToken);
    
    const initializeData = async () => {
      setLoading(prev => ({ ...prev, initialLoad: true }));
      
      if (authToken) {
        const extractedUserId = extractUserIdFromToken(authToken);
        setUserId(extractedUserId);
        
        // Clear cache to force fresh API call
        clearCachedSecurityData(extractedUserId);
        
        try {
          // Fetch all data in parallel for initial load
          await Promise.all([
            fetchSecuritySummary(),
            fetchSecurityAlerts(),
            fetchSecurityConfiguration(),
            fetchRateLimitRules(),
            fetchIPWhitelist(),
            fetchLoadBalancers(),
            fetchSecurityEvents()
          ]);
        } catch (error) {
          console.error('Error during initial data load:', error);
        } finally {
          setLoading(prev => ({ ...prev, initialLoad: false }));
        }
      } else {
        console.log('🔒 [APISecurity] No auth token, skipping fetch');
        showToast('Authentication required. Please login.', 'warning');
        setLoading(prev => ({ ...prev, initialLoad: false }));
      }
    };
    
    initializeData();
  }, [authToken]);

  // Fetch data when active tab changes
  useEffect(() => {
    console.log(`🔄 [APISecurity] Active tab changed to: ${activeTab}, authToken:`, !!authToken);
    
    if (!authToken) return;
    
    const loadTabData = async () => {
      switch (activeTab) {
        case 'rate-limits':
          await fetchRateLimitRules();
          break;
        case 'ip-whitelist':
          await fetchIPWhitelist();
          break;
        case 'load-balancers':
          await fetchLoadBalancers();
          break;
        case 'security-events':
          await fetchSecurityEvents();
          break;
      }
    };
    
    loadTabData();
    
    // Reset to first page when tab changes
    setCurrentPage(1);
  }, [activeTab, authToken]);

  // Update total pages when ipWhitelist changes
  useEffect(() => {
    setTotalPages(Math.ceil(ipWhitelist.length / itemsPerPage));
    if (currentPage > Math.ceil(ipWhitelist.length / itemsPerPage) && ipWhitelist.length > 0) {
      setCurrentPage(Math.ceil(ipWhitelist.length / itemsPerPage));
    }
  }, [ipWhitelist, itemsPerPage]);

  // Handle IP Whitelist status toggle
  const handleToggleIPWhitelistStatus = async (entry) => {
    console.log(`📡 [APISecurity] Toggling IP whitelist entry ${entry.id} status from ${entry.status} to ${entry.status === 'active' ? 'inactive' : 'active'}...`);
    
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }
    
    const newStatus = entry.status === 'active' ? 'inactive' : 'active';
    
    setLoading(prev => ({ ...prev, togglingIP: true }));
    
    try {
      const updateData = { status: newStatus };
      
      const validationErrors = validateUpdateIPWhitelistEntry(updateData);
      if (validationErrors.length > 0) {
        showToast(`Validation errors: ${validationErrors.join(', ')}`, 'error');
        return;
      }
      
      const response = await updateIPWhitelistEntry(authToken, entry.id, updateData);
      const handledResponse = handleSecurityResponse(response);
      const updatedEntry = extractUpdateIPWhitelistResponse(handledResponse);
      
      if (updatedEntry && updatedEntry.success) {
        const updatedIpWhitelist = ipWhitelist.map(item => 
          item.id === entry.id 
            ? { 
                ...item, 
                status: newStatus,
                updatedAt: new Date().toISOString()
              }
            : item
        );
        
        setIpWhitelist(updatedIpWhitelist);
        showToast(`IP whitelist entry ${newStatus === 'active' ? 'activated' : 'deactivated'} successfully!`, 'success');
        
        if (userId) {
          clearCachedSecurityData(userId);
        }
        
        await refreshIPWhitelist();
      } else {
        showToast('Failed to update IP entry status: Invalid response from server', 'error');
      }
      
    } catch (error) {
      console.error('❌ [APISecurity] Error toggling IP whitelist entry status:', error);
      
      let errorMessage = 'Failed to update IP entry status';
      
      if (error.message) {
        if (error.message.includes('401') || error.message.includes('Unauthorized')) {
          errorMessage = 'Your session has expired. Please log in again.';
        } else if (error.message.includes('403') || error.message.includes('Forbidden')) {
          errorMessage = 'You do not have permission to update IP whitelist entries';
        } else if (error.message.includes('404') || error.message.includes('Not Found')) {
          errorMessage = 'The IP whitelist entry was not found. It may have been deleted.';
          const updatedIpWhitelist = ipWhitelist.filter(item => item.id !== entry.id);
          setIpWhitelist(updatedIpWhitelist);
        } else if (error.message.includes('409') || error.message.includes('Conflict')) {
          errorMessage = 'The update conflicts with an existing entry';
        } else {
          errorMessage = error.message;
        }
      }
      
      showToast(errorMessage, 'error');
    } finally {
      setLoading(prev => ({ ...prev, togglingIP: false }));
    }
  };

  // Handle Edit IP Whitelist Entry
  const handleEditIPWhitelistEntry = (entry) => {
    console.log('📝 [APISecurity] Editing IP whitelist entry:', entry);
    setEditingIPEntry(entry);
    setNewIPEntryData({
      name: entry.name || '',
      ipRange: entry.ipRange || '',
      description: entry.description || '',
      endpoints: Array.isArray(entry.endpoints) ? entry.endpoints.join(', ') : (entry.endpoints || ''),
      options: entry.options || {}
    });
    setShowEditIPWhitelistModal(true);
  };

  const handleUpdateIPWhitelistEntry = async () => {
    console.log('📡 [APISecurity] Updating IP whitelist entry...');
    
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }
    
    if (!editingIPEntry || !editingIPEntry.id) {
      showToast('No entry selected for update', 'error');
      return;
    }
    
    const updateData = {};
    
    if (newIPEntryData.name !== editingIPEntry.name) {
      updateData.name = newIPEntryData.name;
    }
    
    if (newIPEntryData.ipRange !== editingIPEntry.ipRange) {
      updateData.ipRange = newIPEntryData.ipRange;
    }
    
    if (newIPEntryData.description !== editingIPEntry.description) {
      updateData.description = newIPEntryData.description;
    }
    
    const originalEndpoints = Array.isArray(editingIPEntry.endpoints) 
      ? editingIPEntry.endpoints 
      : (editingIPEntry.endpoints ? editingIPEntry.endpoints.split(',').map(e => e.trim()) : []);
    
    const newEndpoints = Array.isArray(newIPEntryData.endpoints)
      ? newIPEntryData.endpoints
      : (newIPEntryData.endpoints ? newIPEntryData.endpoints.split(',').map(e => e.trim()) : []);
    
    if (JSON.stringify(originalEndpoints) !== JSON.stringify(newEndpoints)) {
      updateData.endpoints = newEndpoints;
    }
    
    if (Object.keys(updateData).length === 0) {
      showToast('No changes detected to update', 'warning');
      return;
    }
    
    const validationErrors = validateUpdateIPWhitelistEntry(updateData);
    if (validationErrors.length > 0) {
      showToast(`Validation errors: ${validationErrors.join(', ')}`, 'error');
      return;
    }
    
    try {
      setLoading(prev => ({ ...prev, ipWhitelist: true }));
      
      const response = await updateIPWhitelistEntry(authToken, editingIPEntry.id, updateData);
      const handledResponse = handleSecurityResponse(response);
      const updatedEntry = extractUpdateIPWhitelistResponse(handledResponse);
      
      if (updatedEntry && updatedEntry.success) {
        const updatedIpWhitelist = ipWhitelist.map(entry => 
          entry.id === editingIPEntry.id 
            ? { 
                ...entry, 
                ...updatedEntry,
                createdAt: entry.createdAt,
                createdBy: entry.createdBy
              }
            : entry
        );
        
        setIpWhitelist(updatedIpWhitelist);
        
        setEditingIPEntry(null);
        setNewIPEntryData({
          name: '',
          ipRange: '',
          description: '',
          endpoints: '',
          status: 'active',
          options: {}
        });
        setShowEditIPWhitelistModal(false);
        
        showToast(updatedEntry.message || 'IP whitelist entry updated successfully!', 'success');
        
        if (userId) {
          clearCachedSecurityData(userId);
        }
        
        await refreshIPWhitelist();
      } else {
        showToast('Failed to update IP entry: Invalid response from server', 'error');
      }
      
    } catch (error) {
      console.error('❌ [APISecurity] Error updating IP whitelist entry:', error);
      
      let errorMessage = 'Failed to update IP entry';
      
      if (error.message) {
        if (error.message.includes('401') || error.message.includes('Unauthorized')) {
          errorMessage = 'Your session has expired. Please log in again.';
        } else if (error.message.includes('403') || error.message.includes('Forbidden')) {
          errorMessage = 'You do not have permission to update IP whitelist entries';
        } else if (error.message.includes('404') || error.message.includes('Not Found')) {
          errorMessage = 'The IP whitelist entry was not found. It may have been deleted.';
          const updatedIpWhitelist = ipWhitelist.filter(entry => entry.id !== editingIPEntry.id);
          setIpWhitelist(updatedIpWhitelist);
          setShowEditIPWhitelistModal(false);
        } else if (error.message.includes('409') || error.message.includes('Conflict')) {
          errorMessage = 'The IP range conflicts with an existing entry';
        } else {
          errorMessage = error.message;
        }
      }
      
      showToast(errorMessage, 'error');
    } finally {
      setLoading(prev => ({ ...prev, ipWhitelist: false }));
    }
  };

  const refreshIPWhitelist = async () => {
    try {
      if (authToken) {
        const response = await getIPWhitelist(authToken);
        const handledResponse = handleSecurityResponse(response);
        const whitelistData = extractIPWhitelist(handledResponse);
        setIpWhitelist(whitelistData);
        
        if (userId) {
          cacheSecurityData(userId, 'ip_whitelist', whitelistData, 5);
        }
      }
    } catch (refreshError) {
      console.warn('⚠️ [APISecurity] Failed to refresh IP whitelist after update:', refreshError);
    }
  };

  const fetchRateLimitRules = useCallback(async () => {
    console.log('📡 [APISecurity] Fetching rate limit rules...');
    
    if (!authToken) {
      console.log('❌ No auth token for fetchRateLimitRules');
      showToast('Authentication required', 'error');
      return;
    }
    
    setLoading(prev => ({ ...prev, rateLimits: true }));
    setError(prev => ({ ...prev, rateLimits: null }));
    
    try {
      const response = await getRateLimitRules(authToken);
      console.log('📦 [APISecurity] Rate limit rules response:', response);
      
      if (!response) {
        throw new Error('No response from security service');
      }
      
      const handledResponse = handleSecurityResponse(response);
      const rules = extractRateLimitRules(handledResponse);
      
      console.log('📊 [APISecurity] Extracted rate limit rules:', rules.length);
      
      setRateLimitRules(rules);
      
      if (userId) {
        cacheSecurityData(userId, 'rateLimitRules', rules, 15);
      }
      
      showToast(`Loaded ${rules.length} rate limit rules`, 'success');
      
    } catch (error) {
      console.error('❌ [APISecurity] Error fetching rate limit rules:', error);
      setError(prev => ({ ...prev, rateLimits: error.message }));
      showToast(`Failed to load rate limit rules: ${error.message}`, 'error');
      
      if (userId) {
        const cached = getCachedSecurityData(userId, 'rateLimitRules');
        if (cached) {
          console.log('📂 [APISecurity] Using cached rate limit rules');
          setRateLimitRules(cached);
        }
      }
    } finally {
      setLoading(prev => ({ ...prev, rateLimits: false }));
      console.log('🏁 [APISecurity] fetchRateLimitRules completed');
    }
  }, [authToken, userId]);

  // Handle Edit Rate Limit Rule
  const handleEditRateLimitRule = (rule) => {
    console.log('📝 [APISecurity] Editing rate limit rule:', rule);
    setEditingRule(rule);
    setNewRuleData({
      name: rule.name || '',
      description: rule.description || '',
      endpoint: rule.endpoint || '',
      method: rule.method || 'GET',
      limit: rule.limit || 100,
      window: rule.window || '1m',
      burst: rule.burst || 20,
      action: rule.action || 'throttle',
      options: rule.options || {}
    });
    setShowEditRuleModal(true);
  };

  // Handle Update Rate Limit Rule
  const handleUpdateRateLimitRule = async () => {
    console.log('📡 [APISecurity] Updating rate limit rule...');
    
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }
    
    if (!editingRule || !editingRule.id) {
      showToast('No rule selected for update', 'error');
      return;
    }
    
    const updateData = {};
    
    if (newRuleData.name !== editingRule.name) {
      updateData.name = newRuleData.name;
    }
    
    if (newRuleData.description !== editingRule.description) {
      updateData.description = newRuleData.description;
    }
    
    if (newRuleData.endpoint !== editingRule.endpoint) {
      updateData.endpoint = newRuleData.endpoint;
    }
    
    if (newRuleData.method !== editingRule.method) {
      updateData.method = newRuleData.method;
    }
    
    if (newRuleData.limit !== editingRule.limit) {
      updateData.limit = newRuleData.limit;
    }
    
    if (newRuleData.window !== editingRule.window) {
      updateData.window = newRuleData.window;
    }
    
    if (newRuleData.burst !== editingRule.burst) {
      updateData.burst = newRuleData.burst;
    }
    
    if (newRuleData.action !== editingRule.action) {
      updateData.action = newRuleData.action;
    }
    
    if (Object.keys(updateData).length === 0) {
      showToast('No changes detected to update', 'warning');
      return;
    }
    
    const validationErrors = validateUpdateRateLimitRule(updateData);
    if (validationErrors.length > 0) {
      showToast(`Validation errors: ${validationErrors.join(', ')}`, 'error');
      return;
    }
    
    try {
      setLoading(prev => ({ ...prev, rateLimits: true }));
      
      const response = await updateRateLimitRule(authToken, editingRule.id, updateData);
      const handledResponse = handleSecurityResponse(response);
      const updatedRule = extractUpdateRateLimitRuleResponse(handledResponse);
      
      if (updatedRule && updatedRule.success) {
        const updatedRules = rateLimitRules.map(rule => 
          rule.id === editingRule.id 
            ? { 
                ...rule, 
                ...updatedRule,
                createdAt: rule.createdAt,
                createdBy: rule.createdBy
              }
            : rule
        );
        
        setRateLimitRules(updatedRules);
        
        setEditingRule(null);
        setNewRuleData({
          name: '',
          description: '',
          endpoint: '',
          method: 'GET',
          limit: 100,
          window: '1m',
          burst: 20,
          action: 'throttle',
          options: {}
        });
        setShowEditRuleModal(false);
        
        showToast(updatedRule.message || 'Rate limit rule updated successfully!', 'success');
        
        if (userId) {
          clearCachedSecurityData(userId);
        }
      } else {
        showToast('Failed to update rate limit rule: Invalid response from server', 'error');
      }
      
    } catch (error) {
      console.error('❌ [APISecurity] Error updating rate limit rule:', error);
      
      let errorMessage = 'Failed to update rate limit rule';
      
      if (error.message) {
        if (error.message.includes('401') || error.message.includes('Unauthorized')) {
          errorMessage = 'Your session has expired. Please log in again.';
        } else if (error.message.includes('403') || error.message.includes('Forbidden')) {
          errorMessage = 'You do not have permission to update rate limit rules';
        } else if (error.message.includes('404') || error.message.includes('Not Found')) {
          errorMessage = 'The rate limit rule was not found. It may have been deleted.';
          const updatedRules = rateLimitRules.filter(rule => rule.id !== editingRule.id);
          setRateLimitRules(updatedRules);
          setShowEditRuleModal(false);
        } else {
          errorMessage = error.message;
        }
      }
      
      showToast(errorMessage, 'error');
    } finally {
      setLoading(prev => ({ ...prev, rateLimits: false }));
    }
  };

  const fetchIPWhitelist = useCallback(async () => {
    console.log('📡 [APISecurity] Fetching IP whitelist...');
    
    if (!authToken) {
      console.log('❌ No auth token for fetchIPWhitelist');
      showToast('Authentication required', 'error');
      return;
    }
    
    setLoading(prev => ({ ...prev, ipWhitelist: true }));
    setError(prev => ({ ...prev, ipWhitelist: null }));
    
    try {
      const response = await getIPWhitelist(authToken);
      console.log('📦 [APISecurity] IP whitelist response:', response);
      
      if (!response) {
        throw new Error('No response from security service');
      }
      
      const handledResponse = handleSecurityResponse(response);
      const whitelist = extractIPWhitelist(handledResponse);
      
      console.log('📊 [APISecurity] Extracted IP whitelist:', whitelist.length);
      
      setIpWhitelist(whitelist);
      
      if (userId) {
        cacheSecurityData(userId, 'ipWhitelist', whitelist, 15);
      }
      
      showToast(`Loaded ${whitelist.length} IP whitelist entries`, 'success');
      
    } catch (error) {
      console.error('❌ [APISecurity] Error fetching IP whitelist:', error);
      setError(prev => ({ ...prev, ipWhitelist: error.message }));
      showToast(`Failed to load IP whitelist: ${error.message}`, 'error');
      
      if (userId) {
        const cached = getCachedSecurityData(userId, 'ipWhitelist');
        if (cached) {
          console.log('📂 [APISecurity] Using cached IP whitelist');
          setIpWhitelist(cached);
        }
      }
    } finally {
      setLoading(prev => ({ ...prev, ipWhitelist: false }));
      console.log('🏁 [APISecurity] fetchIPWhitelist completed');
    }
  }, [authToken, userId]);

  const fetchLoadBalancers = useCallback(async () => {
    console.log('📡 [APISecurity] Fetching load balancers...');
    
    if (!authToken) {
      console.log('❌ No auth token for fetchLoadBalancers');
      showToast('Authentication required', 'error');
      return;
    }
    
    setLoading(prev => ({ ...prev, loadBalancers: true }));
    setError(prev => ({ ...prev, loadBalancers: null }));
    
    try {
      const response = await getLoadBalancers(authToken);
      console.log('📦 [APISecurity] Load balancers response:', response);
      
      if (!response) {
        throw new Error('No response from security service');
      }
      
      const handledResponse = handleSecurityResponse(response);
      const balancers = extractLoadBalancers(handledResponse);
      
      console.log('📊 [APISecurity] Extracted load balancers:', balancers.length);
      
      setLoadBalancers(balancers);
      
      if (userId) {
        cacheSecurityData(userId, 'loadBalancers', balancers, 15);
      }
      
      showToast(`Loaded ${balancers.length} load balancers`, 'success');
      
    } catch (error) {
      console.error('❌ [APISecurity] Error fetching load balancers:', error);
      setError(prev => ({ ...prev, loadBalancers: error.message }));
      showToast(`Failed to load load balancers: ${error.message}`, 'error');
      
      if (userId) {
        const cached = getCachedSecurityData(userId, 'loadBalancers');
        if (cached) {
          console.log('📂 [APISecurity] Using cached load balancers');
          setLoadBalancers(cached);
        }
      }
    } finally {
      setLoading(prev => ({ ...prev, loadBalancers: false }));
      console.log('🏁 [APISecurity] fetchLoadBalancers completed');
    }
  }, [authToken, userId]);

  const fetchSecurityEvents = useCallback(async () => {
    console.log('📡 [APISecurity] Fetching security events...');
    
    if (!authToken) {
      console.log('❌ No auth token for fetchSecurityEvents');
      showToast('Authentication required', 'error');
      return;
    }
    
    setLoading(prev => ({ ...prev, securityEvents: true }));
    setError(prev => ({ ...prev, securityEvents: null }));
    
    try {
      const response = await getSecurityEvents(authToken);
      console.log('📦 [APISecurity] Security events response:', response);
      
      if (!response) {
        throw new Error('No response from security service');
      }
      
      const handledResponse = handleSecurityResponse(response);
      const events = extractSecurityEvents(handledResponse);
      
      console.log('📊 [APISecurity] Extracted security events:', events.length);
      
      setSecurityEvents(events);
      
      if (userId) {
        cacheSecurityData(userId, 'securityEvents', events, 5);
      }
      
      showToast(`Loaded ${events.length} security events`, 'success');
      
    } catch (error) {
      console.error('❌ [APISecurity] Error fetching security events:', error);
      setError(prev => ({ ...prev, securityEvents: error.message }));
      showToast(`Failed to load security events: ${error.message}`, 'error');
      
      if (userId) {
        const cached = getCachedSecurityData(userId, 'securityEvents');
        if (cached) {
          console.log('📂 [APISecurity] Using cached security events');
          setSecurityEvents(cached);
        }
      }
    } finally {
      setLoading(prev => ({ ...prev, securityEvents: false }));
      console.log('🏁 [APISecurity] fetchSecurityEvents completed');
    }
  }, [authToken, userId]);

  const fetchSecuritySummary = useCallback(async () => {
    console.log('📡 [APISecurity] Fetching security summary...');
    
    if (!authToken) {
      console.log('❌ No auth token for fetchSecuritySummary');
      return;
    }
    
    setLoading(prev => ({ ...prev, summary: true }));
    setError(prev => ({ ...prev, summary: null }));
    
    try {
      const response = await getSecuritySummary(authToken);
      console.log('📦 [APISecurity] Security summary response:', response);
      
      if (!response) {
        throw new Error('No response from security service');
      }
      
      const handledResponse = handleSecurityResponse(response);
      const summary = extractSecuritySummary(handledResponse);
      
      console.log('📊 [APISecurity] Extracted security summary:', summary);
      
      if (summary) {
        setSecuritySummary(summary);
      }
      
      if (userId) {
        cacheSecurityData(userId, 'securitySummary', summary, 10);
      }
      
    } catch (error) {
      console.error('❌ [APISecurity] Error fetching security summary:', error);
      setError(prev => ({ ...prev, summary: error.message }));
      
      if (userId) {
        const cached = getCachedSecurityData(userId, 'securitySummary');
        if (cached) {
          console.log('📂 [APISecurity] Using cached security summary');
          setSecuritySummary(cached);
        }
      }
    } finally {
      setLoading(prev => ({ ...prev, summary: false }));
      console.log('🏁 [APISecurity] fetchSecuritySummary completed');
    }
  }, [authToken, userId]);

  const fetchSecurityAlerts = useCallback(async () => {
    console.log('📡 [APISecurity] Fetching security alerts...');
    
    if (!authToken) {
      console.log('❌ No auth token for fetchSecurityAlerts');
      return;
    }
    
    try {
      const response = await getSecurityAlerts(authToken);
      console.log('📦 [APISecurity] Security alerts response:', response);
      
      if (!response) {
        throw new Error('No response from security service');
      }
      
      const handledResponse = handleSecurityResponse(response);
      const alerts = extractSecurityAlerts(handledResponse);
      
      console.log('📊 [APISecurity] Extracted security alerts:', alerts.length);
      
      setSecurityAlerts(alerts);
      
      if (userId) {
        cacheSecurityData(userId, 'securityAlerts', alerts, 5);
      }
      
    } catch (error) {
      console.error('❌ [APISecurity] Error fetching security alerts:', error);
    }
  }, [authToken, userId]);

  const fetchSecurityConfiguration = useCallback(async () => {
    console.log('📡 [APISecurity] Fetching security configuration...');
    
    if (!authToken) {
      console.log('❌ No auth token for fetchSecurityConfiguration');
      return;
    }
    
    try {
      const response = await getSecurityConfiguration(authToken);
      console.log('📦 [APISecurity] Security configuration response:', response);
      
      if (!response) {
        throw new Error('No response from security service');
      }
      
      const handledResponse = handleSecurityResponse(response);
      const config = extractSecurityConfiguration(handledResponse);
      
      console.log('📊 [APISecurity] Extracted security configuration:', config);
      
      setSecurityConfig(config);
      
      if (userId) {
        cacheSecurityData(userId, 'securityConfig', config, 30);
      }
      
    } catch (error) {
      console.error('❌ [APISecurity] Error fetching security configuration:', error);
    }
  }, [authToken, userId]);

  const handleAddRateLimitRule = async () => {
    console.log('📡 [APISecurity] Adding rate limit rule...');
    
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }
    
    const validationErrors = validateAddRateLimitRule(newRuleData);
    if (validationErrors.length > 0) {
      showToast(`Validation errors: ${validationErrors.join(', ')}`, 'error');
      return;
    }
    
    try {
      const response = await addRateLimitRule(authToken, newRuleData);
      console.log('📦 [APISecurity] Add rate limit rule response:', response);
      
      const handledResponse = handleSecurityResponse(response);
      
      await fetchRateLimitRules();
      
      setNewRuleData({
        name: '',
        description: '',
        endpoint: '',
        method: 'GET',
        limit: 100,
        window: '1m',
        burst: 20,
        action: 'throttle',
        options: {}
      });
      setShowAddRuleModal(false);
      
      showToast('Rate limit rule added successfully!', 'success');
      
    } catch (error) {
      console.error('❌ [APISecurity] Error adding rate limit rule:', error);
      showToast(`Failed to add rule: ${error.message}`, 'error');
    }
  };

  const handleAddIPWhitelistEntry = async () => {
    console.log('📡 [APISecurity] Adding IP whitelist entry...');
    
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }
    
    const validationErrors = validateAddIPWhitelistEntry(newIPEntryData);
    if (validationErrors.length > 0) {
      showToast(`Validation errors: ${validationErrors.join(', ')}`, 'error');
      return;
    }
    
    try {
      const response = await addIPWhitelistEntry(authToken, newIPEntryData);
      console.log('📦 [APISecurity] Add IP whitelist entry response:', response);
      
      const handledResponse = handleSecurityResponse(response);
      
      await fetchIPWhitelist();
      
      setNewIPEntryData({
        name: '',
        ipRange: '',
        description: '',
        endpoints: '',
        options: {}
      });
      setShowIPWhitelistModal(false);
      
      showToast('IP whitelist entry added successfully!', 'success');
      
    } catch (error) {
      console.error('❌ [APISecurity] Error adding IP whitelist entry:', error);
      showToast(`Failed to add IP entry: ${error.message}`, 'error');
    }
  };

  const handleAddLoadBalancer = async () => {
    console.log('📡 [APISecurity] Adding load balancer...');
    
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }
    
    const validationErrors = validateAddLoadBalancer(newLoadBalancerData);
    if (validationErrors.length > 0) {
      showToast(`Validation errors: ${validationErrors.join(', ')}`, 'error');
      return;
    }
    
    try {
      const response = await addLoadBalancer(authToken, newLoadBalancerData);
      console.log('📦 [APISecurity] Add load balancer response:', response);
      
      const handledResponse = handleSecurityResponse(response);
      
      await fetchLoadBalancers();
      
      setNewLoadBalancerData({
        name: '',
        algorithm: 'round_robin',
        healthCheck: '',
        healthCheckInterval: '30s',
        servers: [],
        options: {}
      });
      setShowLoadBalancerModal(false);
      
      showToast('Load balancer added successfully!', 'success');
      
    } catch (error) {
      console.error('❌ [APISecurity] Error adding load balancer:', error);
      showToast(`Failed to add load balancer: ${error.message}`, 'error');
    }
  };

  const handleUpdateRuleStatus = async (ruleId, newStatus) => {
    console.log(`📡 [APISecurity] Updating rule ${ruleId} status to ${newStatus}...`);
    
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }
    
    const updateRequest = { status: newStatus };
    const validationErrors = validateUpdateRuleStatus(updateRequest);
    if (validationErrors.length > 0) {
      showToast(`Validation errors: ${validationErrors.join(', ')}`, 'error');
      return;
    }
    
    try {
      const response = await updateRuleStatus(authToken, ruleId, updateRequest);
      console.log('📦 [APISecurity] Update rule status response:', response);
      
      const handledResponse = handleSecurityResponse(response);
      
      await fetchRateLimitRules();
      
      showToast(`Rule status updated to ${newStatus}`, 'success');
      
    } catch (error) {
      console.error('❌ [APISecurity] Error updating rule status:', error);
      showToast(`Failed to update rule status: ${error.message}`, 'error');
    }
  };

  const handleDeleteRule = async (ruleId) => {
    console.log(`📡 [APISecurity] Deleting rule ${ruleId}...`);
    
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }
    
    if (!window.confirm('Are you sure you want to delete this rule?')) {
      return;
    }
    
    try {
      const response = await deleteRule(authToken, ruleId);
      console.log('📦 [APISecurity] Delete rule response:', response);
      
      const handledResponse = handleSecurityResponse(response);
      
      await fetchRateLimitRules();
      
      showToast('Rule deleted successfully', 'success');
      
    } catch (error) {
      console.error('❌ [APISecurity] Error deleting rule:', error);
      showToast(`Failed to delete rule: ${error.message}`, 'error');
    }
  };

  // Generate comprehensive security recommendations
  const generateSecurityRecommendations = () => {
    const recommendations = [];
    
    // Rate limit recommendations
    if (rateLimitRules.length === 0) {
      recommendations.push({
        severity: 'high',
        message: 'No rate limiting rules configured. Add rate limiting to prevent API abuse and DDoS attacks.'
      });
    } else {
      const activeRules = rateLimitRules.filter(r => r.status === 'active').length;
      if (activeRules === 0) {
        recommendations.push({
          severity: 'medium',
          message: 'Rate limiting rules are configured but inactive. Activate them to protect your APIs.'
        });
      }
      if (rateLimitRules.length < 3) {
        recommendations.push({
          severity: 'low',
          message: 'Consider adding more granular rate limiting rules for different endpoint categories.'
        });
      }
    }
    
    // IP whitelist recommendations
    if (ipWhitelist.length === 0) {
      recommendations.push({
        severity: 'medium',
        message: 'No IP whitelist entries configured. Consider whitelisting trusted IP ranges for sensitive endpoints.'
      });
    } else {
      const activeIPs = ipWhitelist.filter(ip => ip.status === 'active').length;
      if (activeIPs === 0) {
        recommendations.push({
          severity: 'medium',
          message: 'IP whitelist entries exist but are inactive. Activate them to restrict access to trusted IPs.'
        });
      }
    }
    
    // Load balancer recommendations
    if (loadBalancers.length === 0) {
      recommendations.push({
        severity: 'low',
        message: 'No load balancers configured. Load balancing improves availability and distributes traffic.'
      });
    } else {
      loadBalancers.forEach(lb => {
        if (lb.servers && lb.servers.length === 0) {
          recommendations.push({
            severity: 'high',
            message: `Load balancer "${lb.name}" has no backend servers configured. Add servers to enable traffic distribution.`
          });
        }
        const healthyServers = lb.servers?.filter(s => s.status === 'healthy').length || 0;
        if (healthyServers === 0 && lb.servers?.length > 0) {
          recommendations.push({
            severity: 'critical',
            message: `All backend servers in load balancer "${lb.name}" are unhealthy. Check server health immediately.`
          });
        }
      });
    }
    
    // Security events recommendations
    const criticalEvents = securityEvents.filter(e => e.severity === 'critical' || e.severity === 'high').length;
    if (criticalEvents > 10) {
      recommendations.push({
        severity: 'critical',
        message: `${criticalEvents} critical security events detected in the last period. Investigate immediately.`
      });
    } else if (criticalEvents > 0) {
      recommendations.push({
        severity: 'high',
        message: `${criticalEvents} high-severity security events detected. Review and take appropriate action.`
      });
    }
    
    // Security score recommendations
    if (securitySummary.securityScore < 50) {
      recommendations.push({
        severity: 'critical',
        message: `Security score is very low (${securitySummary.securityScore}%). Urgent security improvements needed.`
      });
    } else if (securitySummary.securityScore < 70) {
      recommendations.push({
        severity: 'high',
        message: `Security score is ${securitySummary.securityScore}%. Implement recommended security measures.`
      });
    } else if (securitySummary.securityScore < 85) {
      recommendations.push({
        severity: 'medium',
        message: `Security score is ${securitySummary.securityScore}%. Consider additional security hardening.`
      });
    }
    
    // Vulnerable endpoints recommendations
    if (securitySummary.vulnerableEndpoints > 0) {
      recommendations.push({
        severity: securitySummary.vulnerableEndpoints > 5 ? 'critical' : 'high',
        message: `${securitySummary.vulnerableEndpoints} vulnerable endpoints detected. Address vulnerabilities promptly.`
      });
    }
    
    // Blocked requests recommendations
    if (securitySummary.blockedRequests > 1000) {
      recommendations.push({
        severity: 'medium',
        message: `${securitySummary.blockedRequests.toLocaleString()} requests blocked. Review if rules need adjustment.`
      });
    }
    
    return recommendations;
  };

  const handleGenerateSecurityReport = async () => {
    console.log('📡 [APISecurity] Generating security report...');
    
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }
    
    const reportRequest = {
      reportType: 'comprehensive',
      format: 'pdf',
      startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      endDate: new Date().toISOString().split('T')[0],
      options: {}
    };
    
    const validationErrors = validateGenerateSecurityReport(reportRequest);
    if (validationErrors.length > 0) {
      showToast(`Validation errors: ${validationErrors.join(', ')}`, 'error');
      return;
    }
    
    setLoading(prev => ({ ...prev, report: true }));
    
    try {
      const response = await generateSecurityReport(authToken, reportRequest);
      console.log('📦 [APISecurity] Generate security report response:', response);
      
      const handledResponse = handleSecurityResponse(response);
      let results = extractSecurityReportResults(handledResponse);
      
      // If no recommendations from API, generate our own
      if (!results.recommendations || results.recommendations.length === 0) {
        results = {
          ...results,
          recommendations: generateSecurityRecommendations()
        };
      }
      
      // Add stats if not present
      if (!results.stats) {
        results.stats = {
          totalEvents: securityEvents.length,
          blockedRequests: securitySummary.blockedRequests || 0,
          throttledRequests: securitySummary.throttledRequests || 0,
          activeRules: rateLimitRules.filter(r => r.status === 'active').length,
          activeIPEntries: ipWhitelist.filter(ip => ip.status === 'active').length,
          totalLoadBalancers: loadBalancers.length
        };
      }
      
      console.log('📊 [APISecurity] Security report results with recommendations:', results);
      
      if (results && results.success) {
        setReportResults(results);
        setShowSecurityReportModal(true);
        showToast('Security report generated successfully!', 'success');
      } else {
        showToast('Failed to generate security report', 'error');
      }
      
    } catch (error) {
      console.error('❌ [APISecurity] Error generating security report:', error);
      showToast(`Failed to generate report: ${error.message}`, 'error');
    } finally {
      setLoading(prev => ({ ...prev, report: false }));
    }
  };

  // Handle PDF download
  const handleDownloadPDF = async () => {
    console.log('📡 [APISecurity] Downloading PDF report...');
    
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }
    
    if (!reportResults) {
      showToast('No report results to download', 'error');
      return;
    }
    
    setLoading(prev => ({ ...prev, downloadingPDF: true }));
    
    try {
      // Generate HTML content for PDF
      const htmlContent = generatePDFHTML(reportResults, securitySummary, rateLimitRules, ipWhitelist, loadBalancers, securityEvents);
      
      // In a real application, you would send this HTML to a PDF generation service
      // For demo purposes, we'll create a blob and trigger download
      const blob = new Blob([htmlContent], { type: 'text/html' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `security-report-${new Date().toISOString().split('T')[0]}.html`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      showToast('Report downloaded successfully!', 'success');
      
    } catch (error) {
      console.error('❌ [APISecurity] Error downloading PDF:', error);
      showToast(`Failed to download report: ${error.message}`, 'error');
    } finally {
      setLoading(prev => ({ ...prev, downloadingPDF: false }));
    }
  };

  // Generate HTML for PDF (simplified version - in production you'd use a proper PDF library)
  const generatePDFHTML = (results, summary, rules, whitelist, balancers, events) => {
    const recommendations = results.recommendations || generateSecurityRecommendations();
    const stats = results.stats || {
      totalEvents: events.length,
      blockedRequests: summary.blockedRequests || 0,
      throttledRequests: summary.throttledRequests || 0,
      activeRules: rules.filter(r => r.status === 'active').length,
      activeIPEntries: whitelist.filter(ip => ip.status === 'active').length,
      totalLoadBalancers: balancers.length
    };
    
    return `
      <!DOCTYPE html>
      <html>
      <head>
        <title>API Security Report</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 40px; color: #333; }
          h1 { color: #2563eb; border-bottom: 2px solid #2563eb; padding-bottom: 10px; }
          h2 { color: #1e293b; margin-top: 30px; }
          .summary { background: #f8fafc; padding: 20px; border-radius: 8px; margin: 20px 0; }
          .stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin: 20px 0; }
          .stat-card { background: white; padding: 15px; border-radius: 6px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
          .stat-label { color: #64748b; font-size: 12px; text-transform: uppercase; }
          .stat-value { font-size: 24px; font-weight: bold; color: #1e293b; }
          .recommendation { padding: 15px; margin: 10px 0; border-radius: 6px; }
          .critical { background: #fee2e2; border-left: 4px solid #dc2626; }
          .high { background: #ffedd5; border-left: 4px solid #f97316; }
          .medium { background: #fef9c3; border-left: 4px solid #eab308; }
          .low { background: #e0f2fe; border-left: 4px solid #0ea5e9; }
          .severity { display: inline-block; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }
          .table { width: 100%; border-collapse: collapse; margin: 15px 0; }
          .table th { background: #f1f5f9; padding: 10px; text-align: left; }
          .table td { padding: 10px; border-bottom: 1px solid #e2e8f0; }
          .footer { margin-top: 40px; color: #64748b; font-size: 12px; text-align: center; }
        </style>
      </head>
      <body>
        <h1>API Security Report</h1>
        <p>Generated on: ${new Date().toLocaleString()}</p>
        
        <div class="summary">
          <h2>Security Summary</h2>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-label">Security Score</div>
              <div class="stat-value">${summary.securityScore}%</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">Total Endpoints</div>
              <div class="stat-value">${summary.totalEndpoints}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">Vulnerable Endpoints</div>
              <div class="stat-value">${summary.vulnerableEndpoints}</div>
            </div>
          </div>
        </div>
        
        <h2>Security Recommendations (${recommendations.length})</h2>
        ${recommendations.map(rec => `
          <div class="recommendation ${rec.severity}">
            <span class="severity" style="background: ${
              rec.severity === 'critical' ? '#dc2626' : 
              rec.severity === 'high' ? '#f97316' : 
              rec.severity === 'medium' ? '#eab308' : '#0ea5e9'
            }; color: white;">${rec.severity.toUpperCase()}</span>
            <p style="margin-top: 10px;">${rec.message}</p>
          </div>
        `).join('')}
        
        <h2>Statistics</h2>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-label">Security Events</div>
            <div class="stat-value">${stats.totalEvents}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">Blocked Requests</div>
            <div class="stat-value">${stats.blockedRequests.toLocaleString()}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">Throttled Requests</div>
            <div class="stat-value">${stats.throttledRequests.toLocaleString()}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">Active Rate Limits</div>
            <div class="stat-value">${stats.activeRules}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">Active IP Whitelist</div>
            <div class="stat-value">${stats.activeIPEntries}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">Load Balancers</div>
            <div class="stat-value">${stats.totalLoadBalancers}</div>
          </div>
        </div>
        
        ${rateLimitRules.length > 0 ? `
          <h2>Rate Limit Rules</h2>
          <table class="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Endpoint</th>
                <th>Limit</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              ${rateLimitRules.slice(0, 5).map(rule => `
                <tr>
                  <td>${rule.name}</td>
                  <td>${rule.endpoint}</td>
                  <td>${rule.limit} req/${rule.window}</td>
                  <td>${rule.status}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        ` : ''}
        
        <div class="footer">
          <p>This report was generated by the API Security Dashboard</p>
        </div>
      </body>
      </html>
    `;
  };

  const handleRunSecurityScan = async () => {
    console.log('📡 [APISecurity] Running security scan...');
    
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }
    
    setLoading(prev => ({ ...prev, scan: true }));
    
    try {
      const response = await runSecurityScan(authToken);
      console.log('📦 [APISecurity] Security scan response:', response);
      
      const handledResponse = handleSecurityResponse(response);
      const results = extractSecurityScanResults(handledResponse);
      
      console.log('📊 [APISecurity] Security scan results:', results);
      
      if (results && results.success) {
        setScanResults(results);
        setShowScanModal(true);
        
        await Promise.all([
          fetchSecuritySummary(),
          fetchSecurityEvents()
        ]);
        
        showToast(`Security scan completed. Found ${results.totalFindings} issues.`, 
          results.criticalFindings > 0 ? 'warning' : 'success');
      } else {
        showToast('Security scan failed', 'error');
      }
      
    } catch (error) {
      console.error('❌ [APISecurity] Error running security scan:', error);
      showToast(`Failed to run security scan: ${error.message}`, 'error');
    } finally {
      setLoading(prev => ({ ...prev, scan: false }));
    }
  };

  const handleExportSecurityData = async (format, dataType) => {
    console.log(`📡 [APISecurity] Exporting security data as ${format}...`);
    
    if (!authToken) {
      showToast('Authentication required', 'error');
      return;
    }
    
    const exportRequest = {
      format: format || 'json',
      dataType: dataType || 'all',
      startDate: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      endDate: new Date().toISOString().split('T')[0],
      options: {}
    };
    
    const validationErrors = validateExportSecurityData(exportRequest);
    if (validationErrors.length > 0) {
      showToast(`Validation errors: ${validationErrors.join(', ')}`, 'error');
      return;
    }
    
    try {
      const response = await exportSecurityData(authToken, exportRequest);
      console.log('📦 [APISecurity] Export security data response:', response);
      
      const handledResponse = handleSecurityResponse(response);
      const exportResults = extractExportSecurityResults(handledResponse);
      
      console.log('📊 [APISecurity] Export results:', exportResults);
      
      if (exportResults && exportResults.success) {
        showToast(`Security data exported successfully as ${format.toUpperCase()}`, 'success');
      } else {
        showToast('Failed to export security data', 'error');
      }
      
    } catch (error) {
      console.error('❌ [APISecurity] Error exporting security data:', error);
      showToast(`Failed to export data: ${error.message}`, 'error');
    }
  };

  const handleShowDetails = (item, type) => {
    setModalDetails({ item, type });
    setShowDetailsModal(true);
  };

  const goToPage = (page) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  const goToPreviousPage = () => {
    if (currentPage > 1) {
      setCurrentPage(currentPage - 1);
    }
  };

  const goToNextPage = () => {
    if (currentPage < totalPages) {
      setCurrentPage(currentPage + 1);
    }
  };

  const getCurrentPageItems = () => {
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    return ipWhitelist.slice(startIndex, endIndex);
  };

  const getSecurityScoreColorValue = (score) => {
    const colorClass = getSecurityScoreColor(score);
    switch(colorClass) {
      case 'success': return colors.success;
      case 'warning': return colors.warning;
      case 'danger': return colors.error;
      default: return colors.info;
    }
  };

  const getSecurityScoreLabelValue = (score) => {
    return getSecurityScoreLabel(score);
  };

  const getSeverityColorValue = (severity) => {
    const colorClass = getSeverityColor(severity);
    switch(colorClass) {
      case 'danger': return colors.error;
      case 'warning': return colors.warning;
      case 'info': return colors.info;
      case 'secondary': return colors.textSecondary;
      default: return colors.textSecondary;
    }
  };

  const toggleSection = (sectionId) => {
    setExpandedSections(prev =>
      prev.includes(sectionId)
        ? prev.filter(id => id !== sectionId)
        : [...prev, sectionId]
    );
  };

  const renderPagination = () => {
    if (ipWhitelist.length === 0) return null;
    
    return (
      <div className="flex items-center justify-between px-4 py-3 border-t" style={{ borderColor: colors.border }}>
        <div className="text-sm" style={{ color: colors.textSecondary }}>
          Showing {((currentPage - 1) * itemsPerPage) + 1} to {Math.min(currentPage * itemsPerPage, ipWhitelist.length)} of {ipWhitelist.length} entries
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={goToPreviousPage}
            disabled={currentPage === 1}
            className="px-3 py-1 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift disabled:opacity-50 disabled:cursor-not-allowed"
            style={{ backgroundColor: colors.hover, color: colors.text }}
          >
            <ChevronLeft size={16} />
          </button>
          
          <div className="flex items-center gap-1">
            {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
              let pageNum;
              if (totalPages <= 5) {
                pageNum = i + 1;
              } else if (currentPage <= 3) {
                pageNum = i + 1;
              } else if (currentPage >= totalPages - 2) {
                pageNum = totalPages - 4 + i;
              } else {
                pageNum = currentPage - 2 + i;
              }
              
              return (
                <button
                  key={pageNum}
                  onClick={() => goToPage(pageNum)}
                  className={`w-8 h-8 rounded text-sm font-medium transition-colors hover-lift ${
                    currentPage === pageNum ? 'bg-opacity-100' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    backgroundColor: currentPage === pageNum ? colors.primaryDark : colors.hover,
                    color: currentPage === pageNum ? colors.white : colors.text
                  }}
                >
                  {pageNum}
                </button>
              );
            })}
          </div>
          
          <button
            onClick={goToNextPage}
            disabled={currentPage === totalPages}
            className="px-3 py-1 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift disabled:opacity-50 disabled:cursor-not-allowed"
            style={{ backgroundColor: colors.hover, color: colors.text }}
          >
            <ChevronRight size={16} />
          </button>
        </div>
      </div>
    );
  };

  // Loading Overlay Component
  const LoadingOverlay = () => {
    // Check if any loading state is active
    const isLoading = loading.initialLoad || 
                     loading.rateLimits || 
                     loading.ipWhitelist || 
                     loading.loadBalancers || 
                     loading.securityEvents || 
                     loading.summary || 
                     loading.report || 
                     loading.scan || 
                     loading.downloadingPDF;
    
    if (!isLoading) return null;
    
    return (
      <div className="fixed inset-0 z-[9999] flex items-center justify-center">
        {/* Full-page backdrop */}
        <div className="absolute inset-0 bg-white/95 dark:bg-gray-950/95 backdrop-blur-sm transition-colors duration-300" />
        
        {/* Centered Loading Content */}
        <div className="relative flex flex-col items-center gap-6 p-8 max-w-md w-full">
          {/* Main Spinner */}
          <div className="relative">
            {/* Outer ring */}
            <div className="w-20 h-20 rounded-full border-4 border-gray-100 dark:border-gray-800 animate-pulse" />
            
            {/* Inner spinning ring */}
            <div 
              className="absolute top-0 left-0 w-20 h-20 rounded-full border-4 border-t-transparent border-l-transparent animate-spin"
              style={{ 
                borderColor: `${colors.primary} transparent transparent transparent`,
                filter: 'drop-shadow(0 0 6px rgba(59, 130, 246, 0.3))'
              }}
            />
            
            {/* Center dot */}
            <div 
              className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-3 h-3 rounded-full"
              style={{ backgroundColor: colors.primary }}
            />
          </div>
          
          {/* Loading Text */}
          <div className="text-center space-y-2">
            <h3 className="text-xl font-semibold text-gray-800 dark:text-gray-100">
              Loading Security Data
            </h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              {loading.initialLoad ? 'Initializing security dashboard...' :
               loading.rateLimits ? 'Loading rate limit rules...' :
               loading.ipWhitelist ? 'Loading IP whitelist...' :
               loading.loadBalancers ? 'Loading load balancers...' :
               loading.securityEvents ? 'Loading security events...' :
               loading.summary ? 'Loading security summary...' :
               loading.report ? 'Generating security report...' :
               loading.scan ? 'Running security scan...' :
               loading.downloadingPDF ? 'Preparing PDF download...' :
               'Please wait while we load your data'}
            </p>
          </div>
          
          {/* Progress Bar */}
          <div className="w-64 h-2 bg-gray-100 dark:bg-gray-800 rounded-full overflow-hidden">
            <div 
              className="h-full rounded-full animate-pulse"
              style={{ 
                width: '70%', 
                backgroundColor: colors.primary,
                opacity: 0.8
              }}
            />
          </div>
          
          {/* Optional loading tips */}
          <p className="text-xs text-gray-400 dark:text-gray-500 mt-4">
            This won't take long
          </p>
        </div>
      </div>
    );
  };

  // Render Rate Limits Tab
  const renderRateLimitsTab = () => (
    <div className="space-y-6 mt-1">
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <h2 className="text-lg font-semibold" style={{ color: colors.text }}>Rate Limiting Rules</h2>
          <p className="text-xs" style={{ color: colors.textSecondary }}>
            Configure request rate limits for API endpoints
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button 
            onClick={fetchRateLimitRules}
            disabled={loading.rateLimits}
            className="px-3 py-1.5 rounded text-xs font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift disabled:opacity-50"
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            <RefreshCw size={12} className={loading.rateLimits ? 'animate-spin' : ''} />
            Refresh
          </button>
          <button 
            onClick={() => setShowAddRuleModal(true)}
            className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 hover-lift"
            style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
            <Plus size={14} />
            Add Rule
          </button>
        </div>
      </div>

      {error.rateLimits && (
        <div className="p-4 rounded-lg" style={{ 
          backgroundColor: colors.error + '10',
          border: `1px solid ${colors.error}20`
        }}>
          <div className="flex items-center gap-2">
            <AlertCircle size={16} style={{ color: colors.error }} />
            <span className="text-sm" style={{ color: colors.error }}>
              Error loading rate limit rules: {error.rateLimits}
            </span>
          </div>
          <button 
            onClick={fetchRateLimitRules}
            className="mt-2 px-3 py-1 text-xs rounded hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.error, color: colors.white }}>
            Retry
          </button>
        </div>
      )}

      {loading.rateLimits && rateLimitRules.length === 0 && (
        <div className="text-center py-8" style={{ color: colors.textSecondary }}>
          <RefreshCw size={24} className="animate-spin mx-auto mb-2" />
          <p>Loading rate limit rules...</p>
        </div>
      )}

      {!loading.rateLimits && rateLimitRules.length > 0 && (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {rateLimitRules.map(rule => (
              <div 
                key={rule.id} 
                className="border rounded-lg p-4 hover-lift" 
                style={{ 
                  borderColor: colors.border,
                  backgroundColor: colors.card
                }}
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <div className={`w-2 h-2 rounded-full ${
                      rule.status === 'active' ? 'bg-green-500' : 'bg-red-500'
                    }`} />
                    <h3 className="text-sm font-semibold" style={{ color: colors.text }}>{rule.name}</h3>
                  </div>
                  <div className="flex gap-1">
                    <button 
                      className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleEditRateLimitRule(rule);
                      }}
                      style={{ backgroundColor: colors.hover }}>
                      <Edit2 size={12} style={{ color: colors.textSecondary }} />
                    </button>
                    <button 
                      className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleUpdateRuleStatus(rule.id, 
                          rule.status === 'active' ? 'inactive' : 'active');
                      }}
                      style={{ backgroundColor: colors.hover }}>
                      <Power size={12} style={{ color: rule.status === 'active' ? colors.success : colors.error }} />
                    </button>
                    <button 
                      className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDeleteRule(rule.id);
                      }}
                      style={{ backgroundColor: colors.hover }}>
                      <Trash2 size={12} style={{ color: colors.error }} />
                    </button>
                  </div>
                </div>
                
                <div className="space-y-3">
                  <div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Endpoint</div>
                    <div className="text-sm font-mono truncate" style={{ color: colors.text }}>{rule.endpoint}</div>
                  </div>
                  
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <div className="text-xs" style={{ color: colors.textSecondary }}>Limit</div>
                      <div className="text-sm font-semibold" style={{ color: colors.text }}>
                        {rule.limit} req/{rule.window}
                      </div>
                    </div>
                    <div>
                      <div className="text-xs" style={{ color: colors.textSecondary }}>Burst</div>
                      <div className="text-sm font-semibold" style={{ color: colors.text }}>
                        {rule.burst || 0} req
                      </div>
                    </div>
                  </div>
                  
                  <div className="flex items-center justify-between text-xs">
                    <span style={{ color: colors.textSecondary }}>Action:</span>
                    <span className={`px-2 py-1 rounded ${
                      rule.action === 'block' ? 'bg-red-500/10 text-red-500' : 'bg-blue-500/10 text-blue-500'
                    }`}>
                      {rule.action}
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="border rounded-lg p-6" style={{ 
            borderColor: colors.border,
            backgroundColor: colors.card
          }}>
            <h3 className="text-lg font-semibold mb-4" style={{ color: colors.text }}>Rate Limit Statistics</h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {[
                { 
                  label: 'Total Blocked Requests', 
                  value: securitySummary.blockedRequests?.toLocaleString() || '0', 
                  icon: <ShieldAlert size={20} />, 
                  color: colors.error 
                },
                { 
                  label: 'Total Throttled Requests', 
                  value: securitySummary.throttledRequests?.toLocaleString() || '0', 
                  icon: <Activity size={20} />, 
                  color: colors.warning 
                },
                { 
                  label: 'Active Rules', 
                  value: rateLimitRules.filter(r => r.status === 'active').length.toString(), 
                  icon: <Filter size={20} />, 
                  color: colors.info 
                },
                { 
                  label: 'Covered Endpoints', 
                  value: securitySummary.securedEndpoints?.toString() || '0', 
                  icon: <ShieldCheck size={20} />, 
                  color: colors.success 
                }
              ].map(stat => (
                <div key={stat.label} className="text-center p-4 rounded-lg hover-lift" style={{ 
                  backgroundColor: `${stat.color}10`,
                  border: `1px solid ${stat.color}20`
                }}>
                  <div className="flex justify-center mb-2">
                    <div className="p-2 rounded-full" style={{ backgroundColor: `${stat.color}20` }}>
                      {React.cloneElement(stat.icon, { size: 20, style: { color: stat.color } })}
                    </div>
                  </div>
                  <div className="text-2xl font-bold mb-1" style={{ color: colors.text }}>{stat.value}</div>
                  <div className="text-sm" style={{ color: colors.textSecondary }}>{stat.label}</div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}

      {!loading.rateLimits && rateLimitRules.length === 0 && !error.rateLimits && (
        <div className="text-center py-12" style={{ color: colors.textSecondary }}>
          <ShieldAlert size={48} className="mx-auto mb-4 opacity-50" />
          <h3 className="text-lg font-medium mb-2" style={{ color: colors.text }}>No Rate Limit Rules</h3>
          <p className="mb-4">You haven't configured any rate limiting rules yet.</p>
          <button 
            onClick={() => setShowAddRuleModal(true)}
            className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 mx-auto hover-lift"
            style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
            <Plus size={14} />
            Create Your First Rule
          </button>
        </div>
      )}
    </div>
  );

  // Render IP Whitelist Tab
  const renderIPWhitelistTab = () => {
    const currentItems = getCurrentPageItems();
    
    return (
      <div className="space-y-8">
        <div className="flex items-center justify-between">
          <div className="space-y-2">
            <h2 className="text-lg font-semibold" style={{ color: colors.text }}>IP Whitelisting</h2>
            <p className="text-xs" style={{ color: colors.textSecondary }}>
              Manage IP addresses and ranges allowed to access your APIs
            </p>
          </div>
          <div className="flex items-center gap-2">
            <button 
              onClick={fetchIPWhitelist}
              disabled={loading.ipWhitelist}
              className="px-3 py-1.5 rounded text-xs font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift disabled:opacity-50"
              style={{ backgroundColor: colors.hover, color: colors.text }}>
              <RefreshCw size={12} className={loading.ipWhitelist ? 'animate-spin' : ''} />
              Refresh
            </button>
            <button 
              onClick={() => setShowIPWhitelistModal(true)}
              className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 hover-lift"
              style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
              <Plus size={14} />
              Add IP Range
            </button>
          </div>
        </div>

        {error.ipWhitelist && (
          <div className="p-4 rounded-lg" style={{ 
            backgroundColor: colors.error + '10',
            border: `1px solid ${colors.error}20`
          }}>
            <div className="flex items-center gap-2">
              <AlertCircle size={16} style={{ color: colors.error }} />
              <span className="text-sm" style={{ color: colors.error }}>
                Error loading IP whitelist: {error.ipWhitelist}
              </span>
            </div>
            <button 
              onClick={fetchIPWhitelist}
              className="mt-2 px-3 py-1 text-xs rounded hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.error, color: colors.white }}>
              Retry
            </button>
          </div>
        )}

        {loading.ipWhitelist && ipWhitelist.length === 0 && (
          <div className="text-center py-8" style={{ color: colors.textSecondary }}>
            <RefreshCw size={24} className="animate-spin mx-auto mb-2" />
            <p>Loading IP whitelist...</p>
          </div>
        )}

        {!loading.ipWhitelist && ipWhitelist.length > 0 && (
          <>
            <div className="border rounded-lg overflow-hidden" style={{ 
              borderColor: colors.border,
              backgroundColor: colors.card
            }}>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead style={{ backgroundColor: colors.tableHeader }}>
                    <tr>
                      <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>#</th>
                      <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Name</th>
                      <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>IP Range</th>
                      <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Endpoints</th>
                      <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Status</th>
                      <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentItems.map((item, index) => {
                      const actualIndex = ((currentPage - 1) * itemsPerPage) + index;
                      return (
                        <tr 
                          key={item.id}
                          className="hover:bg-opacity-50 transition-colors cursor-pointer"
                          onClick={() => handleShowDetails(item, 'ip-whitelist')}
                          style={{ 
                            backgroundColor: actualIndex % 2 === 0 ? colors.tableRow : colors.tableRowHover,
                            borderBottom: `1px solid ${colors.border}`
                          }}
                        >
                          <td className="p-4">
                            <div className="text-sm font-mono truncate" style={{ color: colors.text }}>{actualIndex + 1}</div>
                          </td>
                          <td className="p-4">
                            <div className="flex items-center gap-2">
                              <Globe size={14} style={{ color: colors.textSecondary }} />
                              <span className="text-sm font-medium" style={{ color: colors.text }}>{item.name}</span>
                            </div>
                          </td>
                          <td className="p-4">
                            <div className="text-sm font-mono" style={{ color: colors.text }}>{item.ipRange}</div>
                            <div className="text-xs" style={{ color: colors.textSecondary }}>{item.description}</div>
                          </td>
                          <td className="p-4">
                            <div className="text-sm font-mono truncate" style={{ color: colors.text }}>
                              {Array.isArray(item.endpoints) ? item.endpoints.join(', ') : item.endpoints}
                            </div>
                          </td>
                          <td className="p-4">
                            <div className="flex items-center gap-2">
                              <div className={`w-2 h-2 rounded-full ${
                                item.status === 'active' ? 'bg-green-500' : 'bg-red-500'
                              }`} />
                              <span className={`text-xs px-2 py-1 rounded ${
                                item.status === 'active' ? 'bg-green-500/10 text-green-500' : 'bg-red-500/10 text-red-500'
                              }`}>
                                {item.status}
                              </span>
                            </div>
                          </td>
                          <td className="p-4" onClick={(e) => e.stopPropagation()}>
                            <div className="flex items-center gap-2">
                              <button 
                                className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleEditIPWhitelistEntry(item);
                                }}
                                style={{ backgroundColor: colors.hover }}
                                disabled={loading.togglingIP}
                              >
                                <Edit2 size={12} style={{ color: colors.textSecondary }} />
                              </button>
                              
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleToggleIPWhitelistStatus(item);
                                }}
                                disabled={loading.togglingIP}
                                className="relative inline-flex h-5 w-9 items-center rounded-full transition-colors focus:outline-none disabled:opacity-50"
                                style={{
                                  backgroundColor: item.status === 'active' ? colors.success : colors.error,
                                  opacity: 0.8
                                }}
                              >
                                <span
                                  className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                                    item.status === 'active' ? 'translate-x-5' : 'translate-x-0.5'
                                  }`}
                                />
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
              
              {renderPagination()}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="border rounded-lg p-6 hover-lift cursor-pointer" 
                onClick={() => handleShowDetails({ title: 'IP Security Status' }, 'stats')}
                style={{ 
                  borderColor: colors.border,
                  backgroundColor: colors.card
                }}>
                <div className="flex items-center gap-3 mb-4">
                  <div className="p-2 rounded" style={{ backgroundColor: `${colors.info}20` }}>
                    <Shield size={20} style={{ color: colors.info }} />
                  </div>
                  <div>
                    <h3 className="text-sm font-semibold" style={{ color: colors.text }}>IP Security Status</h3>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Current whitelist coverage</div>
                  </div>
                </div>
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-sm" style={{ color: colors.textSecondary }}>Total IP Ranges</span>
                    <span className="text-sm font-semibold" style={{ color: colors.text }}>{ipWhitelist.length}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm" style={{ color: colors.textSecondary }}>Active Ranges</span>
                    <span className="text-sm font-semibold" style={{ color: colors.success }}>
                      {ipWhitelist.filter(ip => ip.status === 'active').length}
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm" style={{ color: colors.textSecondary }}>Protected Endpoints</span>
                    <span className="text-sm font-semibold" style={{ color: colors.text }}>
                      {getEndpointProtectionPercentage(securitySummary)}%
                    </span>
                  </div>
                </div>
              </div>

              <div className="border rounded-lg p-6 hover-lift cursor-pointer" 
                onClick={() => handleShowDetails({ title: 'Recent IP Blocks' }, 'stats')}
                style={{ 
                  borderColor: colors.border,
                  backgroundColor: colors.card
                }}>
                <div className="flex items-center gap-3 mb-4">
                  <div className="p-2 rounded" style={{ backgroundColor: `${colors.success}20` }}>
                    <Activity size={20} style={{ color: colors.success }} />
                  </div>
                  <div>
                    <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Recent IP Blocks</h3>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Last 24 hours</div>
                  </div>
                </div>
                <div className="space-y-2">
                  {securityEvents
                    .filter(event => event.type === 'ip_blocked')
                    .slice(0, 3)
                    .map((event, index) => (
                      <div key={index} className="flex items-center justify-between p-2 rounded hover-lift cursor-pointer"
                        style={{ backgroundColor: colors.hover }}
                        onClick={() => handleShowDetails(event, 'event')}>
                        <div>
                          <div className="text-xs font-medium" style={{ color: colors.text }}>{event.sourceIp}</div>
                          <div className="text-xs" style={{ color: colors.textSecondary }}>{event.endpoint}</div>
                        </div>
                        <span className="text-xs px-2 py-1 rounded-full" style={{ 
                          backgroundColor: colors.error + '20',
                          color: colors.error
                        }}>
                          Blocked
                        </span>
                      </div>
                    ))}
                  {securityEvents.filter(event => event.type === 'ip_blocked').length === 0 && (
                    <div className="text-center py-4" style={{ color: colors.textSecondary }}>
                      No IP blocks in the last 24 hours
                    </div>
                  )}
                </div>
              </div>
            </div>
          </>
        )}

        {!loading.ipWhitelist && ipWhitelist.length === 0 && !error.ipWhitelist && (
          <div className="text-center py-12" style={{ color: colors.textSecondary }}>
            <Globe size={48} className="mx-auto mb-4 opacity-50" />
            <h3 className="text-lg font-medium mb-2" style={{ color: colors.text }}>No IP Whitelist Entries</h3>
            <p className="mb-4">You haven't configured any IP whitelist entries yet.</p>
            <button 
              onClick={() => setShowIPWhitelistModal(true)}
              className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 mx-auto hover-lift"
              style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
              <Plus size={14} />
              Add Your First IP Range
            </button>
          </div>
        )}
      </div>
    );
  };

  // Render Load Balancers Tab
  const renderLoadBalancersTab = () => (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <h2 className="text-lg font-semibold" style={{ color: colors.text }}>Load Balancers</h2>
          <p className="text-xs" style={{ color: colors.textSecondary }}>
            Manage API load balancing and traffic distribution
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button 
            onClick={fetchLoadBalancers}
            disabled={loading.loadBalancers}
            className="px-3 py-1.5 rounded text-xs font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift disabled:opacity-50"
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            <RefreshCw size={12} className={loading.loadBalancers ? 'animate-spin' : ''} />
            Refresh
          </button>
          <button 
            onClick={() => setShowLoadBalancerModal(true)}
            className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 hover-lift"
            style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
            <Plus size={14} />
            Add Load Balancer
          </button>
        </div>
      </div>

      {error.loadBalancers && (
        <div className="p-4 rounded-lg" style={{ 
          backgroundColor: colors.error + '10',
          border: `1px solid ${colors.error}20`
        }}>
          <div className="flex items-center gap-2">
            <AlertCircle size={16} style={{ color: colors.error }} />
            <span className="text-sm" style={{ color: colors.error }}>
              Error loading load balancers: {error.loadBalancers}
            </span>
          </div>
          <button 
            onClick={fetchLoadBalancers}
            className="mt-2 px-3 py-1 text-xs rounded hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.error, color: colors.white }}>
            Retry
          </button>
        </div>
      )}

      {loading.loadBalancers && loadBalancers.length === 0 && (
        <div className="text-center py-8" style={{ color: colors.textSecondary }}>
          <RefreshCw size={24} className="animate-spin mx-auto mb-2" />
          <p>Loading load balancers...</p>
        </div>
      )}

      {!loading.loadBalancers && loadBalancers.length > 0 && (
        <>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {loadBalancers.map(lb => (
              <div 
                key={lb.id} 
                className="border rounded-lg p-6 hover-lift cursor-pointer" 
                onClick={() => handleShowDetails(lb, 'load-balancer')}
                style={{ 
                  borderColor: colors.border,
                  backgroundColor: colors.card
                }}
              >
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded" style={{ backgroundColor: `${colors.success}20` }}>
                      <Server size={20} style={{ color: colors.success }} />
                    </div>
                    <div>
                      <h3 className="text-sm font-semibold" style={{ color: colors.text }}>{lb.name}</h3>
                      <div className="text-xs" style={{ color: colors.textSecondary }}>
                        Algorithm: <span className="font-medium">{lb.algorithm}</span>
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className={`text-xs px-2 py-1 rounded ${
                      lb.status === 'active' ? 'bg-green-500/10 text-green-500' : 'bg-red-500/10 text-red-500'
                    }`}>
                      {lb.status}
                    </span>
                  </div>
                </div>

                {lb.healthCheck && (
                  <div className="mb-6 p-3 rounded-lg" style={{ backgroundColor: colors.hover }}>
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-xs font-medium" style={{ color: colors.text }}>Health Check</span>
                      <span className="text-xs px-2 py-1 rounded" style={{ 
                        backgroundColor: colors.success + '20',
                        color: colors.success
                      }}>
                        Active
                      </span>
                    </div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>
                      Endpoint: <span className="font-mono">{lb.healthCheck}</span>
                    </div>
                    <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
                      Interval: {lb.healthCheckInterval || '30s'}
                    </div>
                  </div>
                )}

                <div className="space-y-3">
                  <div className="text-sm font-medium" style={{ color: colors.text }}>Backend Servers</div>
                  {lb.servers && lb.servers.length > 0 ? (
                    lb.servers.map(server => (
                      <div key={server.id} className="flex items-center justify-between p-3 rounded-lg hover-lift cursor-pointer"
                        style={{ backgroundColor: colors.hover }}
                        onClick={() => handleShowDetails(server, 'server')}>
                        <div className="flex items-center gap-3">
                          <div className={`w-2 h-2 rounded-full ${
                            server.status === 'healthy' ? 'bg-green-500' :
                            server.status === 'degraded' ? 'bg-yellow-500' : 'bg-red-500'
                          }`} />
                          <div>
                            <div className="text-xs font-medium" style={{ color: colors.text }}>{server.name}</div>
                            <div className="text-xs font-mono" style={{ color: colors.textSecondary }}>{server.address}</div>
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="text-xs font-semibold" style={{ color: colors.text }}>{server.connections || 0}</div>
                          <div className="text-xs" style={{ color: colors.textSecondary }}>connections</div>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="text-center py-4 text-sm" style={{ color: colors.textSecondary }}>
                      No servers configured
                    </div>
                  )}
                </div>

                <div className="mt-6 pt-4 border-t flex items-center justify-between" style={{ borderColor: colors.border }}>
                  <div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Total Connections</div>
                    <div className="text-lg font-bold" style={{ color: colors.text }}>
                      {lb.totalConnections || lb.servers?.reduce((sum, s) => sum + (s.connections || 0), 0) || 0}
                    </div>
                  </div>
                  <div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Healthy Servers</div>
                    <div className="text-lg font-bold" style={{ color: colors.success }}>
                      {lb.servers?.filter(s => s.status === 'healthy').length || 0}/{lb.servers?.length || 0}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="border rounded-lg p-6 cursor-pointer hover-lift" 
            onClick={() => handleShowDetails({ title: 'Load Balancing Performance' }, 'stats')}
            style={{ 
              borderColor: colors.border,
              backgroundColor: colors.card
            }}>
            <h3 className="text-lg font-semibold mb-4" style={{ color: colors.text }}>Load Balancing Performance</h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {[
                { label: 'Total Load Balancers', value: loadBalancers.length.toString(), icon: <Server size={20} /> },
                { label: 'Active Servers', value: loadBalancers.reduce((sum, lb) => sum + (lb.servers?.filter(s => s.status === 'healthy').length || 0), 0).toString(), icon: <CheckCircle size={20} /> },
                { label: 'Total Connections', value: loadBalancers.reduce((sum, lb) => sum + (lb.totalConnections || lb.servers?.reduce((sSum, s) => sSum + (s.connections || 0), 0) || 0), 0).toLocaleString(), icon: <Network size={20} /> },
                { label: 'Avg Response Time', value: securitySummary.avgResponseTime || '0ms', icon: <Timer size={20} /> }
              ].map(stat => (
                <div key={stat.label} className="text-center p-4 rounded-lg hover-lift" style={{ 
                  backgroundColor: colors.hover,
                  border: `1px solid ${colors.border}`
                }}>
                  <div className="flex justify-center mb-2">
                    {React.cloneElement(stat.icon, { size: 20, style: { color: colors.textSecondary } })}
                  </div>
                  <div className="text-2xl font-bold mb-1" style={{ color: colors.text }}>{stat.value}</div>
                  <div className="text-sm mb-1" style={{ color: colors.textSecondary }}>{stat.label}</div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}

      {!loading.loadBalancers && loadBalancers.length === 0 && !error.loadBalancers && (
        <div className="text-center py-12" style={{ color: colors.textSecondary }}>
          <Server size={48} className="mx-auto mb-4 opacity-50" />
          <h3 className="text-lg font-medium mb-2" style={{ color: colors.text }}>No Load Balancers</h3>
          <p className="mb-4">You haven't configured any load balancers yet.</p>
          <button 
            onClick={() => setShowLoadBalancerModal(true)}
            className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 mx-auto hover-lift"
            style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
            <Plus size={14} />
            Add Your First Load Balancer
          </button>
        </div>
      )}
    </div>
  );

  // Render Security Events Tab
  const renderSecurityEventsTab = () => (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <h2 className="text-lg font-semibold" style={{ color: colors.text }}>Security Events</h2>
          <p className="text-xs" style={{ color: colors.textSecondary }}>
            Monitor and analyze security events in real-time
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button 
            onClick={fetchSecurityEvents}
            disabled={loading.securityEvents}
            className="px-3 py-1.5 rounded text-xs font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift disabled:opacity-50"
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            <RefreshCw size={12} className={loading.securityEvents ? 'animate-spin' : ''} />
            Refresh
          </button>
          <button 
            onClick={() => handleExportSecurityData('csv', 'events')}
            className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            <Download size={14} />
            Export
          </button>
        </div>
      </div>

      {error.securityEvents && (
        <div className="p-4 rounded-lg" style={{ 
          backgroundColor: colors.error + '10',
          border: `1px solid ${colors.error}20`
        }}>
          <div className="flex items-center gap-2">
            <AlertCircle size={16} style={{ color: colors.error }} />
            <span className="text-sm" style={{ color: colors.error }}>
              Error loading security events: {error.securityEvents}
            </span>
          </div>
          <button 
            onClick={fetchSecurityEvents}
            className="mt-2 px-3 py-1 text-xs rounded hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.error, color: colors.white }}>
            Retry
          </button>
        </div>
      )}

      {loading.securityEvents && securityEvents.length === 0 && (
        <div className="text-center py-8" style={{ color: colors.textSecondary }}>
          <RefreshCw size={24} className="animate-spin mx-auto mb-2" />
          <p>Loading security events...</p>
        </div>
      )}

      {!loading.securityEvents && securityEvents.length > 0 && (
        <>
          <div className="border rounded-lg overflow-hidden" style={{ 
            borderColor: colors.border,
            backgroundColor: colors.card
          }}>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead style={{ backgroundColor: colors.tableHeader }}>
                  <tr>
                    <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Severity</th>
                    <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Type</th>
                    <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Source IP</th>
                    <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Endpoint</th>
                    <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Message</th>
                    <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Time</th>
                  </tr>
                </thead>
                <tbody>
                  {sortSecurityEventsByDate(securityEvents, 'desc').map((event, index) => (
                    <tr 
                      key={event.id || index}
                      className="hover:bg-opacity-50 transition-colors cursor-pointer"
                      onClick={() => handleShowDetails(event, 'event')}
                      style={{ 
                        backgroundColor: index % 2 === 0 ? colors.tableRow : colors.tableRowHover,
                        borderBottom: `1px solid ${colors.border}`
                      }}
                    >
                      <td className="p-4">
                        <div className="flex items-center gap-2">
                          <div className={`w-2 h-2 rounded-full`} 
                            style={{ backgroundColor: getSeverityColorValue(event.severity) }} />
                          <span className={`text-xs px-2 py-1 rounded`}
                            style={{ 
                              backgroundColor: getSeverityColorValue(event.severity) + '20',
                              color: getSeverityColorValue(event.severity)
                            }}>
                            {event.severity}
                          </span>
                        </div>
                      </td>
                      <td className="p-4">
                        <div className="flex items-center gap-2">
                          {event.type === 'rate_limit_exceeded' && <Activity size={14} style={{ color: colors.textSecondary }} />}
                          {event.type === 'ip_blocked' && <ShieldAlert size={14} style={{ color: colors.textSecondary }} />}
                          {event.type === 'suspicious_activity' && <AlertTriangle size={14} style={{ color: colors.textSecondary }} />}
                          {event.type === 'ddos_protection' && <Zap size={14} style={{ color: colors.textSecondary }} />}
                          <span className="text-sm capitalize" style={{ color: colors.text }}>
                            {event.type ? event.type.replace(/_/g, ' ') : 'Unknown'}
                          </span>
                        </div>
                      </td>
                      <td className="p-4">
                        <div className="text-sm font-mono" style={{ color: colors.text }}>{event.sourceIp || 'N/A'}</div>
                      </td>
                      <td className="p-4">
                        <div className="text-sm font-mono truncate" style={{ color: colors.text }}>{event.endpoint || 'N/A'}</div>
                        <div className="text-xs" style={{ color: colors.textSecondary }}>{event.method || ''}</div>
                      </td>
                      <td className="p-4">
                        <div className="text-sm truncate" style={{ color: colors.text }} title={event.message}>
                          {event.message || 'No message'}
                        </div>
                      </td>
                      <td className="p-4">
                        {event.timestamp ? (
                          <>
                            <div className="text-xs" style={{ color: colors.textSecondary }}>
                              {new Date(event.timestamp).toLocaleDateString()}
                            </div>
                            <div className="text-xs" style={{ color: colors.textTertiary }}>
                              {new Date(event.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </div>
                          </>
                        ) : (
                          <div className="text-xs" style={{ color: colors.textSecondary }}>N/A</div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="border rounded-lg p-6 hover-lift cursor-pointer" 
              onClick={() => handleShowDetails({ title: 'Threat Level' }, 'stats')}
              style={{ 
                borderColor: colors.border,
                backgroundColor: colors.card
              }}>
              <div className="flex items-center gap-3 mb-4">
                <div className="p-2 rounded" style={{ backgroundColor: `${colors.error}20` }}>
                  <AlertTriangle size={20} style={{ color: colors.error }} />
                </div>
                <div>
                  <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Threat Level</h3>
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Current security status</div>
                </div>
              </div>
              <div className="text-center py-4">
                <div className="text-4xl font-bold mb-2" 
                  style={{ color: getSecurityScoreColorValue(securitySummary.securityScore) }}>
                  {getSecurityScoreLabelValue(securitySummary.securityScore)}
                </div>
                <div className="text-sm" style={{ color: colors.textSecondary }}>
                  Based on recent security events
                </div>
              </div>
            </div>

            <div className="border rounded-lg p-6 hover-lift cursor-pointer" 
              onClick={() => handleShowDetails({ title: 'Event Trends' }, 'stats')}
              style={{ 
                borderColor: colors.border,
                backgroundColor: colors.card
              }}>
              <div className="flex items-center gap-3 mb-4">
                <div className="p-2 rounded" style={{ backgroundColor: `${colors.info}20` }}>
                  <BarChart3 size={20} style={{ color: colors.info }} />
                </div>
                <div>
                  <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Event Trends</h3>
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Last 7 days</div>
                </div>
              </div>
              <div className="space-y-3">
                {(() => {
                  const eventTypes = securityEvents.reduce((acc, event) => {
                    if (event.type) {
                      acc[event.type] = (acc[event.type] || 0) + 1;
                    }
                    return acc;
                  }, {});
                  
                  const sortedTypes = Object.entries(eventTypes)
                    .sort((a, b) => b[1] - a[1])
                    .slice(0, 3);
                  
                  return sortedTypes.map(([type, count], index) => (
                    <div key={index} className="flex items-center justify-between">
                      <span className="text-sm capitalize truncate" style={{ color: colors.textSecondary }}>
                        {type.replace(/_/g, ' ')}
                      </span>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-semibold" style={{ color: colors.text }}>{count}</span>
                      </div>
                    </div>
                  ));
                })()}
              </div>
            </div>

            <div className="border rounded-lg p-6 hover-lift cursor-pointer" 
              onClick={() => handleShowDetails({ title: 'Security Score' }, 'stats')}
              style={{ 
                borderColor: colors.border,
                backgroundColor: colors.card
              }}>
              <div className="flex items-center gap-3 mb-4">
                <div className="p-2 rounded" style={{ backgroundColor: `${colors.success}20` }}>
                  <ShieldCheck size={20} style={{ color: colors.success }} />
                </div>
                <div>
                  <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Security Score</h3>
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Overall API security</div>
                </div>
              </div>
              <div className="text-center py-4">
                <div className="text-4xl font-bold mb-2" 
                  style={{ color: getSecurityScoreColorValue(securitySummary.securityScore) }}>
                  {securitySummary.securityScore}/100
                </div>
                <div className="text-sm" style={{ color: colors.textSecondary }}>
                  {securitySummary.lastScan ? `Last scan: ${new Date(securitySummary.lastScan).toLocaleDateString()}` : 'No scan yet'}
                </div>
              </div>
            </div>
          </div>
        </>
      )}

      {!loading.securityEvents && securityEvents.length === 0 && !error.securityEvents && (
        <div className="text-center py-12" style={{ color: colors.textSecondary }}>
          <AlertTriangle size={48} className="mx-auto mb-4 opacity-50" />
          <h3 className="text-lg font-medium mb-2" style={{ color: colors.text }}>No Security Events</h3>
          <p className="mb-4">No security events have been detected yet.</p>
          <button 
            onClick={handleRunSecurityScan}
            className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 mx-auto hover-lift"
            style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
            <Shield size={14} />
            Run Security Scan
          </button>
        </div>
      )}
    </div>
  );

  // Render Sidebar
  const renderSidebar = () => (
    <div className="w-80 border-r p-4" style={{ 
      borderColor: colors.border,
      backgroundColor: colors.sidebar
    }}>
      <div className="mb-6">
        <div className="space-y-4">
          {[
            { id: 'ip-whitelist', label: 'IP Whitelist', icon: <Globe size={16} /> },
            { id: 'rate-limits', label: 'Rate Limits', icon: <Filter size={16} /> },
            { id: 'load-balancers', label: 'Load Balancers', icon: <Server size={16} /> },
            { id: 'security-events', label: 'Security Events', icon: <AlertTriangle size={16} /> },
          ].map(item => (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className={`w-full text-left px-3 py-2 rounded flex items-center gap-3 transition-colors ${
                activeTab === item.id ? 'font-medium' : ''
              } hover-lift`}
              style={{
                backgroundColor: activeTab === item.id ? colors.selected : 'transparent',
                color: activeTab === item.id ? colors.primary : colors.text
              }}
            >
              {item.icon}
              {item.label}
              {activeTab === item.id && <ChevronRight size={14} className="ml-auto" />}
            </button>
          ))}
        </div>
      </div>

      <div className="border rounded-lg p-4 mb-6 cursor-pointer hover-lift" 
        onClick={() => handleShowDetails({ title: 'Security Summary' }, 'stats')}
        style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
        <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Security Summary</h3>
        {loading.summary ? (
          <div className="text-center py-4">
            <RefreshCw size={16} className="animate-spin mx-auto" style={{ color: colors.textSecondary }} />
          </div>
        ) : (
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs" style={{ color: colors.textSecondary }}>Total Endpoints</span>
              <span className="text-sm font-semibold" style={{ color: colors.text }}>{securitySummary.totalEndpoints}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-xs" style={{ color: colors.textSecondary }}>Secured</span>
              <span className="text-sm font-semibold" style={{ color: colors.success }}>{securitySummary.securedEndpoints}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-xs" style={{ color: colors.textSecondary }}>Vulnerable</span>
              <span className="text-sm font-semibold" style={{ color: colors.error }}>{securitySummary.vulnerableEndpoints}</span>
            </div>
            <div className="pt-3 border-t" style={{ borderColor: colors.border }}>
              <div className="flex items-center justify-between">
                <span className="text-xs" style={{ color: colors.textSecondary }}>Security Score</span>
                <div className="flex items-center gap-2">
                  <span className="text-sm font-bold" 
                    style={{ color: getSecurityScoreColorValue(securitySummary.securityScore) }}>
                    {securitySummary.securityScore}%
                  </span>
                  <div className="w-16 h-2 rounded-full overflow-hidden" style={{ backgroundColor: colors.border }}>
                    <div 
                      className="h-full rounded-full" 
                      style={{ 
                        width: `${securitySummary.securityScore}%`,
                        backgroundColor: getSecurityScoreColorValue(securitySummary.securityScore)
                      }}
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      <div>
        <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Quick Actions</h3>
        <div className="space-y-4">
          <button 
            onClick={handleGenerateSecurityReport}
            disabled={loading.report}
            className="w-full text-left px-3 py-2 rounded flex items-center gap-3 text-sm hover-lift disabled:opacity-50"
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            <FileText size={14} />
            {loading.report ? 'Generating...' : 'Generate Report'}
          </button>
          <button 
            onClick={handleRunSecurityScan}
            disabled={loading.scan}
            className="w-full text-left px-3 py-2 rounded flex items-center gap-3 text-sm hover-lift disabled:opacity-50"
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            <ShieldCheck size={14} />
            {loading.scan ? 'Scanning...' : 'Run Security Scan'}
          </button>
          <button 
            onClick={() => {
              if (userId) {
                clearCachedSecurityData(userId);
                showToast('Cache cleared successfully', 'success');
              } else {
                showToast('No user ID found', 'warning');
              }
            }}
            className="w-full text-left px-3 py-2 rounded flex items-center gap-3 text-sm hover-lift"
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            <RefreshCw size={14} />
            Clear Cache
          </button>
        </div>
      </div>

      {securityAlerts.length > 0 && (
        <div className="mt-4 pt-4 border-t" style={{ borderColor: colors.border }}>
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-medium" style={{ color: colors.text }}>Recent Alerts</span>
            <span className="text-xs px-2 py-1 rounded-full" style={{ 
              backgroundColor: colors.error + '20',
              color: colors.error
            }}>
              {getUnreadSecurityAlertsCount(securityAlerts)} new
            </span>
          </div>
          <div className="space-y-1">
            {getRecentSecurityAlerts(securityAlerts, 3).map((alert, index) => (
              <div key={alert.id || index} className="flex items-center gap-2 p-2 rounded text-xs hover-lift cursor-pointer"
                style={{ backgroundColor: colors.hover }}
                onClick={() => {
                  const updatedAlerts = markAlertAsRead(securityAlerts, alert.id);
                  setSecurityAlerts(updatedAlerts);
                  handleShowDetails(alert, 'alert');
                }}>
                <div className={`w-1.5 h-1.5 rounded-full ${!alert.read ? 'bg-red-500' : 'bg-green-500'}`} />
                <span className="truncate" style={{ color: colors.textSecondary }}>{alert.message}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );

  const renderMainContent = () => {
    switch (activeTab) {
      case 'rate-limits':
        return renderRateLimitsTab();
      case 'ip-whitelist':
        return renderIPWhitelistTab();
      case 'load-balancers':
        return renderLoadBalancersTab();
      case 'security-events':
        return renderSecurityEventsTab();
      default:
        return renderRateLimitsTab();
    }
  };

  // Add Rule Modal
  const renderAddRuleModal = () => (
    showAddRuleModal && (
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div className="border rounded-lg max-w-md w-full max-h-[90vh] overflow-y-auto" style={{ 
          borderColor: colors.modalBorder,
          backgroundColor: colors.bg
        }}>
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold" style={{ color: colors.text }}>Add Rate Limit Rule</h3>
              <button 
                onClick={() => setShowAddRuleModal(false)}
                className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}
              >
                <X size={20} style={{ color: colors.text }} />
              </button>
            </div>
            
            <div className="space-y-8">
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Rule Name *</label>
                <input 
                  type="text" 
                  value={newRuleData.name}
                  onChange={(e) => setNewRuleData({...newRuleData, name: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="e.g., API Rate Limit for Public Endpoints"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Description</label>
                <input 
                  type="text" 
                  value={newRuleData.description}
                  onChange={(e) => setNewRuleData({...newRuleData, description: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="Optional description"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Endpoint Pattern *</label>
                <input 
                  type="text" 
                  value={newRuleData.endpoint}
                  onChange={(e) => setNewRuleData({...newRuleData, endpoint: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm font-mono focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="/api/v1/**"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>HTTP Method *</label>
                <select 
                  value={newRuleData.method}
                  onChange={(e) => setNewRuleData({...newRuleData, method: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.bg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                >
                  <option value="ALL">ALL Methods</option>
                  <option value="GET">GET</option>
                  <option value="POST">POST</option>
                  <option value="PUT">PUT</option>
                  <option value="DELETE">DELETE</option>
                  <option value="PATCH">PATCH</option>
                </select>
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Limit *</label>
                  <input 
                    type="number" 
                    value={newRuleData.limit}
                    onChange={(e) => setNewRuleData({...newRuleData, limit: parseInt(e.target.value) || 100})}
                    className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                    style={{ 
                      backgroundColor: colors.inputBg,
                      border: `1px solid ${colors.inputBorder}`,
                      color: colors.text
                    }}
                    placeholder="100"
                    min="1"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Window *</label>
                  <select 
                    value={newRuleData.window}
                    onChange={(e) => setNewRuleData({...newRuleData, window: e.target.value})}
                    className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                    style={{ 
                      backgroundColor: colors.inputBg,
                      border: `1px solid ${colors.inputBorder}`,
                      color: colors.text
                    }}
                  >
                    <option value="1s">1 Second</option>
                    <option value="10s">10 Seconds</option>
                    <option value="1m">1 Minute</option>
                    <option value="5m">5 Minutes</option>
                    <option value="1h">1 Hour</option>
                  </select>
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Burst Limit</label>
                <input 
                  type="number" 
                  value={newRuleData.burst}
                  onChange={(e) => setNewRuleData({...newRuleData, burst: parseInt(e.target.value) || 20})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="20"
                  min="0"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Action *</label>
                <select 
                  value={newRuleData.action}
                  onChange={(e) => setNewRuleData({...newRuleData, action: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                >
                  <option value="throttle">Throttle (429)</option>
                  <option value="block">Block (403)</option>
                  <option value="log">Log Only</option>
                </select>
              </div>
            </div>
            
            <div className="flex items-center justify-end gap-3 mt-8">
              <button 
                onClick={() => setShowAddRuleModal(false)}
                className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                Cancel
              </button>
              <button 
                onClick={handleAddRateLimitRule}
                className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                Add Rule
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  );

  // Edit Rule Modal
  const renderEditRuleModal = () => (
    showEditRuleModal && (
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div className="border rounded-lg max-w-md w-full max-h-[90vh] overflow-y-auto" style={{ 
          borderColor: colors.modalBorder,
          backgroundColor: colors.bg
        }}>
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold" style={{ color: colors.text }}>Edit Rate Limit Rule</h3>
              <button 
                onClick={() => {
                  setShowEditRuleModal(false);
                  setEditingRule(null);
                  setNewRuleData({
                    name: '',
                    description: '',
                    endpoint: '',
                    method: 'GET',
                    limit: 100,
                    window: '1m',
                    burst: 20,
                    action: 'throttle',
                    options: {}
                  });
                }}
                className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}
              >
                <X size={20} style={{ color: colors.text }} />
              </button>
            </div>
            
            <div className="space-y-8">
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Rule Name *</label>
                <input 
                  type="text" 
                  value={newRuleData.name}
                  onChange={(e) => setNewRuleData({...newRuleData, name: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="e.g., API Rate Limit for Public Endpoints"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Description</label>
                <input 
                  type="text" 
                  value={newRuleData.description}
                  onChange={(e) => setNewRuleData({...newRuleData, description: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="Optional description"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Endpoint Pattern *</label>
                <input 
                  type="text" 
                  value={newRuleData.endpoint}
                  onChange={(e) => setNewRuleData({...newRuleData, endpoint: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm font-mono focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="/api/v1/**"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>HTTP Method *</label>
                <select 
                  value={newRuleData.method}
                  onChange={(e) => setNewRuleData({...newRuleData, method: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                >
                  <option value="ALL">ALL Methods</option>
                  <option value="GET">GET</option>
                  <option value="POST">POST</option>
                  <option value="PUT">PUT</option>
                  <option value="DELETE">DELETE</option>
                  <option value="PATCH">PATCH</option>
                </select>
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Limit *</label>
                  <input 
                    type="number" 
                    value={newRuleData.limit}
                    onChange={(e) => setNewRuleData({...newRuleData, limit: parseInt(e.target.value) || 100})}
                    className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                    style={{ 
                      backgroundColor: colors.inputBg,
                      border: `1px solid ${colors.inputBorder}`,
                      color: colors.text
                    }}
                    placeholder="100"
                    min="1"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Window *</label>
                  <select 
                    value={newRuleData.window}
                    onChange={(e) => setNewRuleData({...newRuleData, window: e.target.value})}
                    className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                    style={{ 
                      backgroundColor: colors.inputBg,
                      border: `1px solid ${colors.inputBorder}`,
                      color: colors.text
                    }}
                  >
                    <option value="1s">1 Second</option>
                    <option value="10s">10 Seconds</option>
                    <option value="1m">1 Minute</option>
                    <option value="5m">5 Minutes</option>
                    <option value="1h">1 Hour</option>
                  </select>
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Burst Limit</label>
                <input 
                  type="number" 
                  value={newRuleData.burst}
                  onChange={(e) => setNewRuleData({...newRuleData, burst: parseInt(e.target.value) || 20})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="20"
                  min="0"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Action *</label>
                <select 
                  value={newRuleData.action}
                  onChange={(e) => setNewRuleData({...newRuleData, action: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                >
                  <option value="throttle">Throttle (429)</option>
                  <option value="block">Block (403)</option>
                  <option value="log">Log Only</option>
                </select>
              </div>
            </div>
            
            <div className="flex items-center justify-end gap-3 mt-8">
              <button 
                onClick={() => {
                  setShowEditRuleModal(false);
                  setEditingRule(null);
                  setNewRuleData({
                    name: '',
                    description: '',
                    endpoint: '',
                    method: 'GET',
                    limit: 100,
                    window: '1m',
                    burst: 20,
                    action: 'throttle',
                    options: {}
                  });
                }}
                className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                Cancel
              </button>
              <button 
                onClick={handleUpdateRateLimitRule}
                disabled={loading.rateLimits}
                className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift disabled:opacity-50"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                {loading.rateLimits ? 'Updating...' : 'Update Rule'}
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  );

  // Add IP Whitelist Modal
  const renderIPWhitelistModal = () => (
    showIPWhitelistModal && (
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div className="border rounded-lg max-w-md w-full max-h-[90vh] overflow-y-auto" style={{ 
          borderColor: colors.modalBorder,
          backgroundColor: colors.bg
        }}>
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold" style={{ color: colors.text }}>Add IP Whitelist Entry</h3>
              <button 
                onClick={() => setShowIPWhitelistModal(false)}
                className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}
              >
                <X size={20} style={{ color: colors.text }} />
              </button>
            </div>
            
            <div className="space-y-8">
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Entry Name *</label>
                <input 
                  type="text" 
                  value={newIPEntryData.name}
                  onChange={(e) => setNewIPEntryData({...newIPEntryData, name: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="e.g., Office Network"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>IP Range/CIDR *</label>
                <input 
                  type="text" 
                  value={newIPEntryData.ipRange}
                  onChange={(e) => setNewIPEntryData({...newIPEntryData, ipRange: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm font-mono focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="192.168.1.0/24 or 10.0.0.1"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Description</label>
                <input 
                  type="text" 
                  value={newIPEntryData.description}
                  onChange={(e) => setNewIPEntryData({...newIPEntryData, description: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="Optional description"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Protected Endpoints</label>
                <input 
                  type="text" 
                  value={newIPEntryData.endpoints}
                  onChange={(e) => setNewIPEntryData({...newIPEntryData, endpoints: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm font-mono focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="/api/v1/** (leave empty for all endpoints)"
                />
              </div>
            </div>
            
            <div className="flex items-center justify-end gap-3 mt-8">
              <button 
                onClick={() => setShowIPWhitelistModal(false)}
                className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                Cancel
              </button>
              <button 
                onClick={handleAddIPWhitelistEntry}
                className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                Add Entry
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  );

  // Add Load Balancer Modal
  const renderLoadBalancerModal = () => (
    showLoadBalancerModal && (
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div className="border rounded-lg max-w-md w-full max-h-[90vh] overflow-y-auto" style={{ 
          borderColor: colors.modalBorder,
          backgroundColor: colors.bg
        }}>
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold" style={{ color: colors.text }}>Add Load Balancer</h3>
              <button 
                onClick={() => setShowLoadBalancerModal(false)}
                className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}
              >
                <X size={20} style={{ color: colors.text }} />
              </button>
            </div>
            
            <div className="space-y-6">
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Name *</label>
                <input 
                  type="text" 
                  value={newLoadBalancerData.name}
                  onChange={(e) => setNewLoadBalancerData({...newLoadBalancerData, name: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="e.g., Primary API Cluster"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Algorithm *</label>
                <select 
                  value={newLoadBalancerData.algorithm}
                  onChange={(e) => setNewLoadBalancerData({...newLoadBalancerData, algorithm: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.bg,
                    border: `1px ${colors.inputBorder}`,
                    color: colors.text
                  }}
                >
                  {getLoadBalancingAlgorithms().map(alg => (
                    <option key={alg.value} value={alg.value}>{alg.label}</option>
                  ))}
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Health Check Endpoint</label>
                <input 
                  type="text" 
                  value={newLoadBalancerData.healthCheck}
                  onChange={(e) => setNewLoadBalancerData({...newLoadBalancerData, healthCheck: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="/api/v1/health"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Health Check Interval</label>
                <select 
                  value={newLoadBalancerData.healthCheckInterval}
                  onChange={(e) => setNewLoadBalancerData({...newLoadBalancerData, healthCheckInterval: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                >
                  <option value="5s">5 Seconds</option>
                  <option value="15s">15 Seconds</option>
                  <option value="30s">30 Seconds</option>
                  <option value="1m">1 Minute</option>
                  <option value="5m">5 Minutes</option>
                </select>
              </div>
            </div>
            
            <div className="flex items-center justify-end gap-3 mt-8">
              <button 
                onClick={() => setShowLoadBalancerModal(false)}
                className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                Cancel
              </button>
              <button 
                onClick={handleAddLoadBalancer}
                className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                Add Load Balancer
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  );

  // Security Scan Modal
  const renderScanModal = () => (
    showScanModal && scanResults && (
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div className="border rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto" style={{ 
          borderColor: colors.modalBorder,
          backgroundColor: colors.bg
        }}>
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold" style={{ color: colors.text }}>Security Scan Results</h3>
              <button 
                onClick={() => setShowScanModal(false)}
                className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}
              >
                <X size={20} style={{ color: colors.text }} />
              </button>
            </div>
            
            <div className="space-y-8">
              <div className="p-4 rounded-lg" style={{ backgroundColor: colors.hover }}>
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <div className="text-sm font-semibold" style={{ color: colors.text }}>Scan Summary</div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>
                      Completed on {new Date().toLocaleString()}
                    </div>
                  </div>
                  <div className={`px-3 py-1 rounded-full ${
                    scanResults.criticalFindings > 0 ? 'bg-red-500/20 text-red-500' : 
                    scanResults.warningFindings > 0 ? 'bg-yellow-500/20 text-yellow-500' : 
                    'bg-green-500/20 text-green-500'
                  }`}>
                    <span className="text-xs font-medium">
                      {scanResults.criticalFindings > 0 ? 'CRITICAL ISSUES' :
                       scanResults.warningFindings > 0 ? 'WARNINGS' : 'CLEAN'}
                    </span>
                  </div>
                </div>
                
                <div className="grid grid-cols-3 gap-4">
                  <div className="text-center">
                    <div className="text-2xl font-bold" style={{ color: colors.text }}>
                      {scanResults.totalFindings || 0}
                    </div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Total Issues</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold" style={{ color: colors.error }}>
                      {scanResults.criticalFindings || 0}
                    </div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Critical</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold" style={{ color: colors.warning }}>
                      {scanResults.warningFindings || 0}
                    </div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Warnings</div>
                  </div>
                </div>
              </div>
              
              {scanResults.findings && scanResults.findings.length > 0 && (
                <div>
                  <h4 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Detailed Findings</h4>
                  <div className="space-y-2">
                    {scanResults.findings.map((finding, index) => (
                      <div key={index} className="p-3 rounded-lg" style={{ backgroundColor: colors.hover }}>
                        <div className="flex items-start gap-3">
                          <div className={`mt-1 w-2 h-2 rounded-full ${
                            finding.severity === 'critical' ? 'bg-red-500' :
                            finding.severity === 'high' ? 'bg-orange-500' :
                            finding.severity === 'medium' ? 'bg-yellow-500' : 'bg-blue-500'
                          }`} />
                          <div className="flex-1">
                            <div className="flex items-center justify-between mb-1">
                              <span className="text-sm font-medium" style={{ color: colors.text }}>
                                {finding.title}
                              </span>
                              <span className={`text-xs px-2 py-1 rounded ${
                                finding.severity === 'critical' ? 'bg-red-500/20 text-red-500' :
                                finding.severity === 'high' ? 'bg-orange-500/20 text-orange-500' :
                                finding.severity === 'medium' ? 'bg-yellow-500/20 text-yellow-500' :
                                'bg-blue-500/20 text-blue-500'
                              }`}>
                                {finding.severity}
                              </span>
                            </div>
                            <p className="text-xs" style={{ color: colors.textSecondary }}>
                              {finding.description}
                            </p>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
            
            <div className="flex items-center justify-end gap-3 mt-8">
              <button 
                onClick={() => handleExportSecurityData('json', 'scan-results')}
                className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift flex items-center gap-2"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                <Download size={14} />
                Export Results
              </button>
              <button 
                onClick={() => setShowScanModal(false)}
                className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  );

  // Security Report Modal
  const renderSecurityReportModal = () => (
    showSecurityReportModal && reportResults && (
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div className="border rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto" style={{ 
          borderColor: colors.modalBorder,
          backgroundColor: colors.bg
        }}>
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold" style={{ color: colors.text }}>Security Report</h3>
              <button 
                onClick={() => setShowSecurityReportModal(false)}
                className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}
              >
                <X size={20} style={{ color: colors.text }} />
              </button>
            </div>
            
            <div className="space-y-8">
              <div className="p-4 rounded-lg" style={{ backgroundColor: colors.hover }}>
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <div className="text-sm font-semibold" style={{ color: colors.text }}>Report Summary</div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>
                      Generated on {new Date().toLocaleDateString()}
                    </div>
                  </div>
                  <div className="px-3 py-1 rounded-full" style={{ 
                    backgroundColor: getSecurityScoreColorValue(securitySummary.securityScore) + '20',
                    color: getSecurityScoreColorValue(securitySummary.securityScore)
                  }}>
                    <span className="text-xs font-medium">
                      {getSecurityScoreLabelValue(securitySummary.securityScore).toUpperCase()}
                    </span>
                  </div>
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Security Score</div>
                    <div className="text-lg font-bold" style={{ color: getSecurityScoreColorValue(securitySummary.securityScore) }}>
                      {securitySummary.securityScore}/100
                    </div>
                  </div>
                  <div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Vulnerable Endpoints</div>
                    <div className="text-lg font-bold" style={{ color: colors.error }}>
                      {securitySummary.vulnerableEndpoints}
                    </div>
                  </div>
                </div>
              </div>
              
              {(reportResults.recommendations && reportResults.recommendations.length > 0) && (
                <div>
                  <h4 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Security Recommendations</h4>
                  <div className="space-y-2">
                    {reportResults.recommendations.map((rec, index) => (
                      <div key={index} className="flex items-start gap-2 p-3 rounded-lg hover-lift"
                        style={{ backgroundColor: colors.hover }}>
                        <div className={`p-1 rounded-full ${
                          rec.severity === 'critical' ? 'bg-red-500/20' :
                          rec.severity === 'high' ? 'bg-orange-500/20' :
                          rec.severity === 'medium' ? 'bg-yellow-500/20' : 'bg-blue-500/20'
                        }`}>
                          <AlertTriangle size={12} style={{ 
                            color: rec.severity === 'critical' ? colors.error :
                                   rec.severity === 'high' ? colors.warning :
                                   rec.severity === 'medium' ? colors.warning : colors.info
                          }} />
                        </div>
                        <span className="text-sm" style={{ color: colors.text }}>{rec.message}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
              
              {reportResults.stats && (
                <div className="grid grid-cols-2 gap-4">
                  <div className="p-3 rounded-lg" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Total Events</div>
                    <div className="text-xl font-bold" style={{ color: colors.text }}>{reportResults.stats.totalEvents}</div>
                  </div>
                  <div className="p-3 rounded-lg" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Blocked Requests</div>
                    <div className="text-xl font-bold" style={{ color: colors.text }}>{reportResults.stats.blockedRequests}</div>
                  </div>
                </div>
              )}
            </div>
            
            <div className="flex items-center justify-end gap-3 mt-8">
              <button 
                onClick={handleDownloadPDF}
                disabled={loading.downloadingPDF}
                className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift flex items-center gap-2 disabled:opacity-50"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                <Download size={14} />
                {loading.downloadingPDF ? 'Downloading...' : 'Download PDF'}
              </button>
              <button 
                onClick={() => setShowSecurityReportModal(false)}
                className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  );

  // Details Modal
  const renderDetailsModal = () => (
    showDetailsModal && modalDetails && (
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div className="border rounded-lg max-w-lg w-full max-h-[90vh] overflow-y-auto" style={{ 
          borderColor: colors.modalBorder,
          backgroundColor: colors.bg
        }}>
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold" style={{ color: colors.text }}>
                {modalDetails.item.title || modalDetails.item.name || 'Details'}
              </h3>
              <button 
                onClick={() => setShowDetailsModal(false)}
                className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}
              >
                <X size={20} style={{ color: colors.text }} />
              </button>
            </div>
            
            <div className="space-y-4">
              {Object.entries(modalDetails.item).map(([key, value]) => {
                if (key === 'id' || key === '__v' || typeof value === 'object') return null;
                return (
                  <div key={key} className="flex items-start justify-between border-b pb-2" style={{ borderColor: colors.border }}>
                    <span className="text-sm capitalize" style={{ color: colors.textSecondary }}>
                      {key.replace(/([A-Z])/g, ' $1').trim()}:
                    </span>
                    <span className="text-sm font-medium" style={{ color: colors.text }}>
                      {value?.toString() || 'N/A'}
                    </span>
                  </div>
                );
              })}
            </div>
            
            <div className="flex items-center justify-end gap-3 mt-8">
              <button 
                onClick={() => setShowDetailsModal(false)}
                className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  );

  // Edit IP Whitelist Modal
  const renderEditIPWhitelistModal = () => (
    showEditIPWhitelistModal && (
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div className="border rounded-lg max-w-md w-full max-h-[90vh] overflow-y-auto" style={{ 
          borderColor: colors.modalBorder,
          backgroundColor: colors.bg
        }}>
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold" style={{ color: colors.text }}>Edit IP Whitelist Entry</h3>
              <button 
                onClick={() => {
                  setShowEditIPWhitelistModal(false);
                  setEditingIPEntry(null);
                  setNewIPEntryData({
                    name: '',
                    ipRange: '',
                    description: '',
                    endpoints: '',
                    options: {}
                  });
                }}
                className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}
              >
                <X size={20} style={{ color: colors.text }} />
              </button>
            </div>
            
            <div className="space-y-8">
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Entry Name *</label>
                <input 
                  type="text" 
                  value={newIPEntryData.name}
                  onChange={(e) => setNewIPEntryData({...newIPEntryData, name: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="e.g., Office Network"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>IP Range/CIDR *</label>
                <input 
                  type="text" 
                  value={newIPEntryData.ipRange}
                  onChange={(e) => setNewIPEntryData({...newIPEntryData, ipRange: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm font-mono focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="192.168.1.0/24 or 10.0.0.1"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Description</label>
                <input 
                  type="text" 
                  value={newIPEntryData.description}
                  onChange={(e) => setNewIPEntryData({...newIPEntryData, description: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="Optional description"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Protected Endpoints</label>
                <input 
                  type="text" 
                  value={newIPEntryData.endpoints}
                  onChange={(e) => setNewIPEntryData({...newIPEntryData, endpoints: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg text-sm font-mono focus:outline-none"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="/api/v1/** (leave empty for all endpoints)"
                />
              </div>
            </div>
            
            <div className="flex items-center justify-end gap-3 mt-8">
              <button 
                onClick={() => {
                  setShowEditIPWhitelistModal(false);
                  setEditingIPEntry(null);
                  setNewIPEntryData({
                    name: '',
                    ipRange: '',
                    description: '',
                    endpoints: '',
                    options: {}
                  });
                }}
                className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                Cancel
              </button>
              <button 
                onClick={handleUpdateIPWhitelistEntry}
                disabled={loading.ipWhitelist}
                className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift disabled:opacity-50"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                {loading.ipWhitelist ? 'Updating...' : 'Update Entry'}
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  );

  const renderToast = () => {
    if (!toast) return null;
    
    const bgColor = toast.type === 'error' ? colors.error : 
                   toast.type === 'success' ? colors.success : 
                   toast.type === 'warning' ? colors.warning : 
                   colors.info;
    
    return (
      <div className="fixed bottom-4 right-4 px-4 py-2 rounded text-sm font-medium z-50 animate-fade-in-up shadow-lg"
        style={{ 
          backgroundColor: bgColor,
          color: 'white'
        }}>
        {toast.message}
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
        
        .text-blue-400 { color: #60a5fa; }
        .text-green-400 { color: #34d399; }
        .text-purple-400 { color: #a78bfa; }
        .text-orange-400 { color: #fb923c; }
        .text-red-400 { color: #f87171; }
        .text-gray-500 { color: #9ca3af; }
        
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
        
        .prose {
          color: ${colors.textSecondary};
          line-height: 1.6;
        }
        
        .prose p {
          margin-bottom: 1em;
        }
        
        .prose strong {
          color: ${colors.text};
          font-weight: 600;
        }
        
        code {
          font-family: 'SF Mono', Monaco, 'Cascadia Mono', 'Segoe UI Mono', 'Roboto Mono', monospace;
          font-size: 0.875em;
        }
        
        input:focus, button:focus, select:focus {
          outline: 2px solid ${colors.primary}40;
          outline-offset: 2px;
        }
        
        .hover-lift:hover {
          transform: translateY(-2px);
          transition: transform 0.2s ease;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        .gradient-bg {
          background: linear-gradient(135deg, ${colors.primary}20 0%, ${colors.info}20 100%);
        }
      `}</style>

      {/* Loading Overlay */}
      <LoadingOverlay />

      {/* TOP NAVIGATION */}
      <div className="flex items-center justify-between h-10 px-4 border-b" style={{ 
        backgroundColor: colors.header,
        borderColor: colors.border
      }}>
        <div className="flex items-center gap-2">
          <span className="px-3 py-1.5 text-sm font-medium -ml-3 uppercase">API Security</span>
        </div>

        <div className="flex items-center gap-2">
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
            <input 
              type="text" 
              placeholder="Search security events..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-8 pr-3 py-1.5 rounded text-xs focus:outline-none w-64 hover-lift"
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
                  <X size={12} style={{ color: colors.textSecondary }} />
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* MAIN CONTENT */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left Sidebar */}
        {renderSidebar()}

        {/* Main Content Area */}
        <div className="flex-1 overflow-auto p-6">
          <div className="max-w-8xl mx-auto ml-2 mr-2">
            {/* Security Overview */}
            <div className="mb-8">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <h1 className="text-xl font-semibold" style={{ color: colors.text }}>API Security</h1>
                  <p className="text-sm" style={{ color: colors.textSecondary }}>
                    Monitor and manage security policies for your APIs
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <button 
                    onClick={handleRunSecurityScan}
                    disabled={loading.scan}
                    className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift disabled:opacity-50"
                    style={{ backgroundColor: colors.hover, color: colors.text }}>
                    <ShieldCheck size={14} className="inline mr-2" />
                    {loading.scan ? 'Scanning...' : 'Run Scan'}
                  </button>
                  <button 
                    onClick={handleGenerateSecurityReport}
                    disabled={loading.report}
                    className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift disabled:opacity-50"
                    style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                    {loading.report ? 'Generating...' : 'Generate Report'}
                  </button>
                </div>
              </div>

              {/* Stats Overview */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
                {[
                  { 
                    label: 'Security Score', 
                    value: `${securitySummary.securityScore}%`, 
                    icon: <ShieldCheck size={20} />, 
                    color: getSecurityScoreColorValue(securitySummary.securityScore), 
                    change: '' 
                  },
                  { 
                    label: 'Active Threats', 
                    value: securityEvents.filter(e => e.severity === 'critical' || e.severity === 'high').length.toString(), 
                    icon: <AlertTriangle size={20} />, 
                    color: colors.warning, 
                    change: '' 
                  },
                  { 
                    label: 'Protected Endpoints', 
                    value: `${securitySummary.securedEndpoints}/${securitySummary.totalEndpoints}`, 
                    icon: <Lock size={20} />, 
                    color: colors.info, 
                    change: '' 
                  },
                  { 
                    label: 'Avg Response Time', 
                    value: securitySummary.avgResponseTime || '0ms', 
                    icon: <Activity size={20} />, 
                    color: colors.primary, 
                    change: '' 
                  }
                ].map((stat, index) => (
                  <div key={index} className="border rounded-lg p-4 hover-lift cursor-pointer" 
                    onClick={() => handleShowDetails({ title: stat.label, value: stat.value }, 'stats')}
                    style={{ 
                      borderColor: colors.border,
                      backgroundColor: colors.card
                    }}>
                    <div className="flex items-center justify-between mb-2">
                      <div className="p-2 rounded" style={{ backgroundColor: `${stat.color}20` }}>
                        {React.cloneElement(stat.icon, { size: 16, style: { color: stat.color } })}
                      </div>
                      {stat.change && (
                        <span className={`text-xs ${stat.change.startsWith('+') || stat.change.includes('ms') ? 'text-green-500' : 'text-red-500'}`}>
                          {stat.change}
                        </span>
                      )}
                    </div>
                    <div className="text-2xl font-bold mb-1" style={{ color: colors.text }}>{stat.value}</div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>{stat.label}</div>
                  </div>
                ))}
              </div>
            </div>

            {/* Main Content */}
            {!authToken ? (
              <div className="text-center py-12">
                <Shield size={48} className="mx-auto mb-4 opacity-50" style={{ color: colors.textSecondary }} />
                <h3 className="text-lg font-medium mb-2" style={{ color: colors.text }}>Authentication Required</h3>
                <p className="mb-4" style={{ color: colors.textSecondary }}>
                  Please log in to view and manage API security settings.
                </p>
              </div>
            ) : (
              renderMainContent()
            )}
          </div>
        </div>
      </div>

      {/* MODALS */}
      {renderAddRuleModal()}
      {renderEditRuleModal()}
      {renderIPWhitelistModal()}
      {renderLoadBalancerModal()}
      {renderScanModal()}
      {renderSecurityReportModal()}
      {renderDetailsModal()}
      {renderEditIPWhitelistModal()}

      {/* TOAST */}
      {renderToast()}
    </div>
  );
};

export default APISecurity;