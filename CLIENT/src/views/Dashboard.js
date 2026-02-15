// Dashboard.jsx - UPDATED COLOR SCHEME
import React, { useState, useEffect, useCallback } from 'react';
import {
  Database, Server, FileCode, Activity, Zap, Settings,
  Search, Bell, ChevronDown, MoreVertical, RefreshCw,
  Plus, CheckCircle, AlertCircle, TrendingUp, Users,
  Shield, Download, Filter, Eye, Edit, Trash2, Copy,
  Globe, Cpu, HardDrive, Network, Lock, Key, Sun, Moon,
  BarChart3, LineChart, Calendar, Clock, X, AlertTriangle,
  Database as DatabaseIcon, Folder, FolderOpen, FileText,
  Code, Cloud, ShieldCheck, CreditCard, Package, PieChart,
  Table, Grid, List, MessageSquare, Mail, Layers, GitMerge,
  BarChart, LineChart as LineChartIcon, Terminal, Cpu as CpuIcon,
  FileJson, BookOpen, Share2, Upload, EyeOff, Type, Palette, TrendingDown,
  Contrast, VolumeX, ZapOff, GitPullRequest, ShieldAlert,
  CalendarDays, DatabaseZap, Network as NetworkIcon, FileOutput, TestTube,
  Code2, Search as SearchIcon, DownloadCloud, UploadCloud,
  UserCheck, KeyRound, FolderTree, FolderTree as FolderTreeIcon,
  BookMarked, LayoutDashboard, Sliders, ChevronRight,
  ArrowUpRight, ArrowDownRight, BarChart2, Shield as ShieldIcon,
  Database as DatabaseIcon2, Cpu as CpuIcon2, Globe as GlobeIcon,
  Server as ServerIcon, Clock as ClockIcon, Wifi, GitBranch,
  ExternalLink, Menu, X as XIcon, Home, Smartphone, Zap as ZapIcon,
  Shield as ShieldIcon2, Cpu as CpuIcon3, Globe as GlobeIcon2,
  Users as UsersIcon, File as FileIcon, Terminal as TerminalIcon,
  Layers as LayersIcon, GitBranch as GitBranchIcon, Wifi as WifiIcon,
  BarChart as BarChartIcon, Activity as ActivityIcon, ChevronLeft,
  ChevronUp, ChevronRight as ChevronRightIcon, Play, Pause,
  StopCircle, Battery, BatteryCharging, Radio, CloudRain, Wind,
  Thermometer, Droplets, Gauge, Workflow, GitCompare, GitCommit,
  GitPullRequest as GitPullRequestIcon, GitMerge as GitMergeIcon,
  GitFork, GitGraph, GitBranchPlus, CodeXml, ServerCog,
  DatabaseBackup, ServerCrash, ServerOff, ServerIcon as ServerIcon2,
  HardDrive as HardDriveIcon, MemoryStick, Cpu as CpuIcon4,
  Network as NetworkIcon2, Wifi as WifiIcon2, ShieldCheck as ShieldCheckIcon,
  Lock as LockIcon, Key as KeyIcon, Fingerprint, Scan, QrCode,
  ScanFace, ShieldHalf, ShieldQuestion, ShieldX, ShieldPlus,
  ShieldMinus, ShieldEllipsis, BellRing, BellOff, MessageCircle,
  MessageSquare as MessageSquareIcon, MessageCircleWarning,
  MessageCircleQuestion, AlertOctagon, AlertHexagon, AlertDiamond,
  TriangleAlert, CircleAlert, OctagonAlert, HexagonAlert,
  DiamondAlert, SquareAlert, Circle, Square, Triangle, Hexagon,
  Octagon, Diamond, Star, Sparkles, Rocket, Satellite,
  SatelliteDish, Orbit, Planet, Comet, Meteor, Moon as MoonIcon,
  Sun as SunIcon, CloudSun, CloudMoon, CloudLightning, CloudSnow,
  CloudFog, Tornado, Hurricane, Earthquake, Volcano, Snowflake,
  Wind as WindIcon, Waves, Droplet, ThermometerSun, ThermometerSnowflake,
  Fire, Sparkle, Atom, Beaker, FlaskRound, Microscope, Telescope,
  Satellite as SatelliteIcon, Cpu as CpuIcon5, Brain, BrainCircuit,
  BrainCog, Cogs, Cog, Settings as SettingsIcon, SlidersHorizontal,
  ToggleLeft, ToggleRight, SwitchCamera, SwitchCamera as SwitchCameraIcon,
  ToggleLeft as ToggleLeftIcon, ToggleRight as ToggleRightIcon, Power,
  PowerOff, BatteryFull, BatteryLow, BatteryMedium, BatteryWarning,
  Plug, PlugZap, PlugZap2, Zap as ZapIcon2, LightningBolt, Energy,
  Fuel, Oil, Gas, Water, Fire as FireIcon, Sparkles as SparklesIcon,
  Magic, Wand, WandSparkles, HatWizard, CrystalBall, Scroll,
  BookOpenCheck, BookOpenText, BookKey, BookLock, BookMarked as BookMarkedIcon,
  BookPlus, BookMinus, BookX, BookOpen as BookOpenIcon, Book as BookIcon,
  Notebook, NotebookText, NotebookPen, PenTool, PenLine, PenSquare,
  PenBox, Pen as PenIcon, Highlighter, Pencil, PencilLine, PencilRuler,
  Ruler, SquarePen, Edit2, Edit3, Eraser, Paintbrush, Paintbrush2,
  Palette as PaletteIcon, Dropper, Contrast as ContrastIcon, Image,
  Images, Camera, Video, Film, Music, Headphones, Speaker, Volume2,
  Mic, Mic2, MicOff, VideoOff, CameraOff, Phone, PhoneCall,
  PhoneForwarded, PhoneIncoming, PhoneMissed, PhoneOff, PhoneOutgoing,
  Voicemail, MessageCircle as MessageCircleIcon,
  MessageSquare as MessageSquareIcon2, MessageCirclePlus,
  MessageSquarePlus, MessageCircleDashed, MessageSquareDashed,
  Mail as MailIcon, MailOpen, MailPlus, MailMinus, MailX, MailWarning,
  MailCheck, MailSearch, Inbox, Send, Archive, ArchiveRestore,
  InboxFull, Mailbox, Package as PackageIcon, Package2, PackageCheck,
  PackageX, PackageSearch, Box, Cube, Cuboid, Cylinder, Cone, Pyramid,
  Sphere, Torus, Dodecahedron, Icosahedron, Octahedron, Tetrahedron,
  Diamond as DiamondIcon, Hexagon as HexagonIcon, Octagon as OctagonIcon,
  Triangle as TriangleIcon, Circle as CircleIcon, Square as SquareIcon,
  RectangleHorizontal, RectangleVertical, Ellipse, Scissors,
  ScissorsLineDashed, Crop, Crop as CropIcon, Frame, Grid2X2, Grid3X3,
  Columns, Rows, PanelLeft, PanelRight, PanelTop, PanelBottom, Sidebar,
  SidebarClose, SidebarOpen, LayoutGrid, LayoutList, LayoutTemplate,
  LayoutDashboard as LayoutDashboardIcon, Columns as ColumnsIcon,
  Rows as RowsIcon, Split, Combine, AlignLeft, AlignCenter, AlignRight,
  AlignJustify, AlignVerticalJustifyStart, AlignVerticalJustifyCenter,
  AlignVerticalJustifyEnd, AlignHorizontalJustifyStart,
  AlignHorizontalJustifyCenter, AlignHorizontalJustifyEnd,
  AlignHorizontalSpaceBetween, AlignVerticalSpaceBetween,
  AlignHorizontalSpaceAround, AlignVerticalSpaceAround,
  AlignStartVertical, AlignStartHorizontal, AlignEndVertical,
  AlignEndHorizontal, AlignCenterVertical, AlignCenterHorizontal,
  Space, Indent, Outdent, List as ListIcon, ListChecks, ListTodo,
  ListX, ListPlus, ListMinus, ListOrdered, ListRestart, ListTree,
  ListFilter, ListVideo, ListMusic, ListImage, ListEnd, ListStart,
  ListCollapse, ListExpand, ListCheck, ListCheck2, ListCheck3,
  Checklist, CheckSquare, SquareCheck, CheckCircle2, CircleCheck,
  Check, X as XIcon2, XCircle, XSquare, XOctagon,
  AlertCircle as AlertCircleIcon, AlertTriangle as AlertTriangleIcon,
  AlertOctagon as AlertOctagonIcon, AlertHexagon as AlertHexagonIcon,
  AlertDiamond as AlertDiamondIcon, TriangleAlert as TriangleAlertIcon,
  CircleAlert as CircleAlertIcon, OctagonAlert as OctagonAlertIcon,
  HexagonAlert as HexagonAlertIcon, DiamondAlert as DiamondAlertIcon,
  SquareAlert as SquareAlertIcon, Info, HelpCircle, QuestionMark,
  CircleHelp, SquareHelp, TriangleHelp, HexagonHelp, OctagonHelp,
  DiamondHelp, Brain as BrainIcon, BrainCog as BrainCogIcon,
  BrainCircuit as BrainCircuitIcon, Cpu as CpuIcon6, Server as ServerIcon3,
  Database as DatabaseIcon3, Network as NetworkIcon3, Shield as ShieldIcon3,
  Zap as ZapIcon3, Users as UsersIcon2, File as FileIcon2,
  Folder as FolderIcon, Globe as GlobeIcon3, Clock as ClockIcon2,
  Calendar as CalendarIcon, Bell as BellIcon, Settings as SettingsIcon2,
  Search as SearchIcon2, Menu as MenuIcon, User as UserIcon, LogOut,
  LogIn, UserPlus, UserMinus, UserX, UserCheck as UserCheckIcon,
  UserCog, UsersCog, UserCircle, UserCircle2, UserSquare, UserSquare2,
  CircleUser, SquareUser, TriangleUser, HexagonUser, OctagonUser,
  DiamondUser, Crown, Award, Trophy, Medal, Star as StarIcon, Heart,
  Gem, Diamond as DiamondIcon2, Sparkle as SparkleIcon, Target,
  Crosshair, Crosshair2, Focus, Scan as ScanIcon, ScanLine, ScanText,
  ScanEye, ScanFace as ScanFaceIcon, QrCode as QrCodeIcon, Barcode,
  Waves as WavesIcon, Radio as RadioIcon, Satellite as SatelliteIcon2,
  SatelliteDish as SatelliteDishIcon, Broadcast, Antenna,
  TowerControl, Radar, Sonar, Wifi as WifiIcon3, Bluetooth, Nfc,
  Signal, SignalHigh, SignalMedium, SignalLow, SignalZero, WifiOff,
  BluetoothOff, NfcOff, SignalOff, Router, RouterIcon, Modem, Switch,
  Hub, ServerRack, Rack, Database as DatabaseIcon4,
  HardDrive as HardDriveIcon2, Cpu as CpuIcon7, MemoryStick as MemoryStickIcon,
  Motherboard, CircuitBoard, Chip, Microchip, Processor, Cpu as CpuIcon8,
  Brain as BrainIcon2
} from "lucide-react";

// Import DashboardController
import {
  getDashboardStats,
  getDashboardConnections,
  getDashboardApis,
  getDashboardActivities,
  getDashboardSchemaStats,
  getCodeGenerationStats,
  getComprehensiveDashboard,
  searchDashboardActivities,
  refreshDashboard,
  exportDashboardData,
  monitorDashboardMetrics,
  handleDashboardResponse,
  formatDashboardData,
  validateDashboardSearchCriteria,
  buildDashboardSearchDTO,
  buildDashboardPaginationParams
} from "../controllers/DashboardController.js";

const Dashboard = ({ theme, isDark, customTheme, toggleTheme, navigateTo, setActiveTab, authToken }) => {
  const [loading, setLoading] = useState(true);
  const [refreshLoading, setRefreshLoading] = useState(false);
  const [timeRange, setTimeRange] = useState("24h");
  
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
    connections: {
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 0,
      number: 0,
      first: true,
      last: true,
      empty: true,
      numberOfElements: 0
    },
    apis: {
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 0,
      number: 0,
      first: true,
      last: true,
      empty: true,
      numberOfElements: 0
    },
    recentActivities: {
      content: [],
      totalPages: 1,
      totalElements: 0,
      size: 7,
      number: 0,
      first: true,
      last: true,
      empty: true,
      numberOfElements: 0
    },
    schemaStats: {},
    codeGenerationStats: {},
    systemHealth: {},
    lastUpdated: null,
    generatedFor: ''
  });

  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState(null);
  const [searchLoading, setSearchLoading] = useState(false);
  
  // Modal states
  const [modalStack, setModalStack] = useState([]);

  // Pagination
  const [activityPage, setActivityPage] = useState(1);
  const [activitiesPerPage, setActivitiesPerPage] = useState(7);
  
  // Pagination for API modals
  const [apiStatsPage, setApiStatsPage] = useState(1);
  const [apiCallsPage, setApiCallsPage] = useState(1);
  const [itemsPerModalPage, setItemsPerModalPage] = useState(6);
  
  // Mobile state
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isRightSidebarVisible, setIsRightSidebarVisible] = useState(false);
  const [isMobileSearchOpen, setIsMobileSearchOpen] = useState(false);

  // Error state
  const [error, setError] = useState(null);

  // Mobile menu handlers
  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  const toggleRightSidebar = () => {
    setIsRightSidebarVisible(!isRightSidebarVisible);
  };

  const toggleMobileSearch = () => {
    setIsMobileSearchOpen(!isMobileSearchOpen);
  };

  // Responsive icon size function
  const getResponsiveIconSize = () => {
    if (typeof window !== 'undefined') {
      if (window.innerWidth < 480) return 12;
      if (window.innerWidth < 768) return 14;
      return 14;
    }
    return 14;
  };

  // Color scheme - EXACT MATCH from Login component
  const colors = isDark ? {
    // Base colors
    bg: 'rgb(1 14 35)',
    white: '#FFFFFF',
    sidebar: 'rgb(41 53 72 / 19%)',
    main: 'rgb(1 14 35)',
    header: 'rgb(20 26 38)',
    card: 'rgb(41 53 72 / 19%)',
    
    // Text colors
    text: '#F1F5F9',
    textSecondary: 'rgb(148 163 184)',
    textTertiary: 'rgb(100 116 139)',
    
    // Border colors
    border: 'rgb(51 65 85 / 19%)',
    borderLight: 'rgb(45 55 72)',
    borderDark: 'rgb(71 85 105)',
    
    // Interactive states
    hover: 'rgb(45 46 72 / 33%)',
    active: 'rgb(59 74 99)',
    selected: 'rgb(44 82 130)',
    
    // Primary colors
    primary: 'rgb(96 165 250)',
    primaryLight: 'rgb(147 197 253)',
    primaryDark: 'rgb(37 99 235)',
    
    // Status colors
    success: 'rgb(52 211 153)',
    warning: 'rgb(251 191 36)',
    error: 'rgb(248 113 113)',
    info: 'rgb(96 165 250)',
    
    // UI Component colors
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
    
    // Connection status colors
    connectionOnline: 'rgb(52 211 153)',
    connectionOffline: 'rgb(248 113 113)',
    connectionIdle: 'rgb(251 191 36)',
    
    // Accent colors
    accentPurple: 'rgb(167 139 250)',
    accentPink: 'rgb(244 114 182)',
    accentCyan: 'rgb(34 211 238)',
    
    // Gradient
    gradient: 'from-blue-500/20 via-violet-500/20 to-orange-500/20'
  } : {
    // Light mode colors
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

  // Modal management functions
  const openModal = (type, data) => {
    setModalStack(prev => [...prev, { type, data }]);
  };

  const closeModal = () => {
    setModalStack(prev => prev.slice(0, -1));
  };

  const closeAllModals = () => {
    setModalStack([]);
  };

  const getCurrentModal = () => {
    if (modalStack.length === 0) return null;
    return modalStack[modalStack.length - 1];
  };

  // Transform API data to match component structure
  const transformApiData = (apiData) => {
    if (!apiData || !apiData.data) return {};
    
    const data = apiData.data;
    
    // Transform connections data
    const connectionsData = data.connections?.connections || [];
    const connections = {
      content: connectionsData,
      totalPages: Math.ceil(connectionsData.length / 10),
      totalElements: connectionsData.length,
      size: 10,
      number: 0,
      first: true,
      last: connectionsData.length <= 10,
      empty: connectionsData.length === 0,
      numberOfElements: Math.min(connectionsData.length, 10)
    };
    
    // Transform APIs data
    const apisData = data.apis?.apis || [];
    const apis = {
      content: apisData,
      totalPages: Math.ceil(apisData.length / 10),
      totalElements: apisData.length,
      size: 10,
      number: 0,
      first: true,
      last: apisData.length <= 10,
      empty: apisData.length === 0,
      numberOfElements: Math.min(apisData.length, 10)
    };
    
    // Transform recent activities data
    const activitiesData = data.recentActivities?.activities || [];
    const recentActivities = {
      content: activitiesData,
      totalPages: data.recentActivities?.totalPages || 1,
      totalElements: data.recentActivities?.totalItems || activitiesData.length,
      size: activitiesPerPage,
      number: activityPage - 1,
      first: activityPage === 1,
      last: activityPage === (data.recentActivities?.totalPages || 1),
      empty: activitiesData.length === 0,
      numberOfElements: activitiesData.length
    };
    
    // Transform schema stats
    const schemaStats = data.schemaStats || {};
    
    // Transform stats
    const stats = {
      totalConnections: data.stats?.totalConnections || connectionsData.length,
      activeConnections: data.stats?.activeConnections || connectionsData.filter(c => c.status === 'connected').length,
      totalApis: data.stats?.totalApis || apisData.length,
      activeApis: data.stats?.activeApis || apisData.filter(a => a.status === 'active').length,
      totalCalls: data.stats?.totalCalls || apisData.reduce((sum, api) => sum + (api.calls || 0), 0),
      avgLatency: data.stats?.avgLatency || "0ms",
      successRate: data.stats?.successRate || "0%",
      uptime: data.stats?.uptime || "0%"
    };
    
    return {
      stats,
      connections,
      apis,
      recentActivities,
      schemaStats,
      codeGenerationStats: data.codeGenerationStats || {},
      systemHealth: data.systemHealth || {},
      lastUpdated: data.lastUpdated || new Date().toISOString(),
      generatedFor: data.generatedFor || 'User'
    };
  };

  // Fetch dashboard data from API
  const fetchDashboardData = useCallback(async () => {
    if (!authToken) {
      setError('Authentication required');
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    
    try {
      // Get comprehensive data
      const response = await getComprehensiveDashboard(authToken);
      const processedResponse = handleDashboardResponse(response);
      
      // Transform the data to match component structure
      const transformedData = transformApiData(processedResponse);
      
      setDashboardData(prev => ({
        ...prev,
        ...transformedData
      }));

    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      setError(`Failed to load dashboard: ${error.message}`);
      
      // Try individual endpoints as fallback
      try {
        await fetchIndividualData();
      } catch (fallbackError) {
        console.error('Fallback also failed:', fallbackError);
        setError('Unable to load dashboard data. Please try again later.');
      }
    } finally {
      setLoading(false);
    }
  }, [authToken, activityPage, activitiesPerPage]);

  // Fetch individual data components
  const fetchIndividualData = async () => {
    if (!authToken) return;

    try {
      const [
        statsResponse,
        connectionsResponse,
        apisResponse,
        activitiesResponse,
        schemaResponse
      ] = await Promise.all([
        getDashboardStats(authToken),
        getDashboardConnections(authToken),
        getDashboardApis(authToken),
        getDashboardActivities(authToken, { page: activityPage, size: activitiesPerPage }),
        getDashboardSchemaStats(authToken)
      ]);

      const stats = handleDashboardResponse(statsResponse);
      const connections = handleDashboardResponse(connectionsResponse);
      const apis = handleDashboardResponse(apisResponse);
      const activities = handleDashboardResponse(activitiesResponse);
      const schema = handleDashboardResponse(schemaResponse);

      // Transform individual responses
      const transformedData = transformApiData({
        data: {
          stats: stats.data,
          connections: connections.data,
          apis: apis.data,
          recentActivities: activities.data,
          schemaStats: schema.data,
          lastUpdated: new Date().toISOString()
        }
      });

      setDashboardData(prev => ({
        ...prev,
        ...transformedData
      }));

    } catch (error) {
      throw new Error(`Failed to fetch individual components: ${error.message}`);
    }
  };

  // Fetch activities with pagination
  const fetchActivitiesWithPagination = async (page) => {
    if (!authToken) return;
    
    try {
      const response = await getDashboardActivities(authToken, { 
        page: page, 
        size: activitiesPerPage 
      });
      const processedResponse = handleDashboardResponse(response);
      
      if (processedResponse.data?.activities) {
        const activitiesData = processedResponse.data.activities;
        setDashboardData(prev => ({
          ...prev,
          recentActivities: {
            content: activitiesData,
            totalPages: processedResponse.data.totalPages || 1,
            totalElements: processedResponse.data.totalItems || activitiesData.length,
            size: activitiesPerPage,
            number: page - 1,
            first: page === 1,
            last: page === (processedResponse.data.totalPages || 1),
            empty: activitiesData.length === 0,
            numberOfElements: activitiesData.length
          }
        }));
      }
    } catch (error) {
      console.error('Error fetching activities page:', error);
    }
  };

  // Refresh dashboard
  const handleRefresh = async () => {
    if (!authToken) return;
    
    setRefreshLoading(true);
    try {
      const response = await getComprehensiveDashboard(authToken);
      const processedResponse = handleDashboardResponse(response);
      const transformedData = transformApiData(processedResponse);
      
      setDashboardData(prev => ({
        ...prev,
        ...transformedData
      }));
    } catch (error) {
      console.error('Error refreshing dashboard:', error);
      setError(`Refresh failed: ${error.message}`);
    } finally {
      setRefreshLoading(false);
    }
  };

  // Initialize and fetch data
  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

  // Update activities when page changes
  useEffect(() => {
    if (authToken && activityPage > 0) {
      fetchActivitiesWithPagination(activityPage);
    }
  }, [activityPage, activitiesPerPage, authToken]);

  // Navigation handlers
  const handleNavigateToSchemaBrowser = () => {
    closeAllModals();
    setActiveTab('schema-browser');
  };

  const handleNavigateToApiBuilder = () => {
    closeAllModals();
    setActiveTab('api-collections');
  };

  const handleNavigateToCodeBase = () => {
    closeAllModals();
    setActiveTab('code-base');
  };

  const handleNavigateToDocumentation = () => {
    closeAllModals();
    setActiveTab('api-docs');
  };

  const handleNavigateToAPISecurity = () => {
    closeAllModals();
    setActiveTab('security');
  };

  const handleNavigateToUserManagement = () => {
    closeAllModals();
    setActiveTab('user-mgt');
  };

  const handleNavigateToConnections = () => {
    console.log('Navigating to Connections');
    closeAllModals();
    setActiveTab('api-collections');
  };

  // Generate API should navigate to Schema Browser
  const handleApiGeneration = () => {
    console.log('Generating API - Navigating to Schema Browser');
    closeAllModals();
    handleNavigateToSchemaBrowser();
  };

  // Export data should navigate to Documentation
  const handleExportData = () => {
    console.log('Exporting data - Navigating to Documentation');
    closeAllModals();
    handleNavigateToDocumentation();
  };

  // View all connections should navigate to Connections
  const handleViewAllConnections = () => {
    console.log('Viewing all connections - Navigating to Connections');
    closeAllModals();
    handleNavigateToConnections();
  };

  // View performance metrics should navigate to Collections
  const handleViewPerformanceMetrics = () => {
    console.log('Viewing performance metrics - Navigating to Collections');
    closeAllModals();
    handleNavigateToApiBuilder();
  };

  // View API stats should show modal
  const handleApiStatsClick = () => {
    console.log('Opening API Stats modal');
    const activeApis = dashboardData.apis?.content?.filter(api => api.status === 'active') || [];
    openModal('apiStats', {
      title: 'Active APIs',
      data: activeApis,
      totalItems: activeApis.length
    });
  };

  // View API calls should show modal
  const handleApiCallsClick = () => {
    console.log('Opening API Calls modal');
    const apiCallsData = dashboardData.apis?.content?.map(api => ({
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

  const handleNavigateToAPICollection = () => {
    console.log('Creating new connection - Navigating to Schema Browser');
    closeAllModals();
    setActiveTab('api-collections');
  };

  const handleViewConnectionDetails = (connection) => {
    openModal('connection', connection);
    console.log('Viewing connection details:', connection.name);
  };

  const handleActivityClick = (activity) => {
    openModal('activity', activity);
    console.log('Viewing activity:', activity.action);
  };

  const handleSchemaItemClick = (itemType, count) => {
    openModal('schemaItem', { type: itemType, count });
    console.log('Viewing schema item:', itemType);
  };

  // Test connection - keep as modal since it's an action
  const handleTestConnection = (connection) => {
    console.log('Testing connection:', connection?.name);
    openModal('testConnection', {
      connection: connection,
      status: 'testing',
      progress: 0
    });
    
    // Simulate testing progress
    let progress = 0;
    const interval = setInterval(() => {
      progress += 25;
      if (progress >= 100) {
        clearInterval(interval);
        setTimeout(() => {
          closeModal();
          openModal('testResults', {
            connection: connection,
            status: 'success',
            message: 'Connection test successful!'
          });
        }, 500);
      }
    }, 300);
  };

  // Pagination handlers
  const handlePrevPage = () => {
    if (activityPage > 1) {
      setActivityPage(activityPage - 1);
    }
  };

  const handleNextPage = () => {
    if (activityPage < (dashboardData.recentActivities.totalPages || 1)) {
      setActivityPage(activityPage + 1);
    }
  };

  const handleApiStatsPageChange = (newPage) => {
    setApiStatsPage(newPage);
  };

  const handleApiCallsPageChange = (newPage) => {
    setApiCallsPage(newPage);
  };

  // Helper functions from static version
  const getIconForActivity = (icon) => {
    const iconSize = getResponsiveIconSize();
    const iconProps = { size: iconSize, style: { color: colors.textSecondary } };
    switch(icon) {
      case 'api': return <FileCode {...iconProps} />;
      case 'database': return <Database {...iconProps} />;
      case 'code': return <Code {...iconProps} />;
      case 'schema': return <Table {...iconProps} />;
      case 'user': return <Users {...iconProps} />;
      case 'settings': return <Settings {...iconProps} />;
      case 'backup': return <DatabaseBackup {...iconProps} />;
      case 'test': return <Beaker {...iconProps} />;
      default: return <Activity {...iconProps} />;
    }
  };

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
    const iconSize = getResponsiveIconSize();
    const iconProps = { size: iconSize, style: { color: colors.textSecondary } };
    switch(type) {
      case 'oracle': return <Database {...iconProps} />;
      case 'postgresql': return <Server {...iconProps} />;
      case 'mysql': return <DatabaseIcon {...iconProps} />;
      case 'mongodb': return <Database {...iconProps} />;
      case 'redis': return <Database {...iconProps} />;
      default: return <Database {...iconProps} />;
    }
  };

  const getPriorityColor = (priority) => {
    switch(priority) {
      case 'high': return colors.error;
      case 'medium': return colors.warning;
      case 'low': return colors.success;
      default: return colors.textSecondary;
    }
  };

  // Stat Card Component - EXACT MATCH from static version
  const StatCard = ({ title, value, icon: Icon, change, color, onClick }) => {
    const iconSize = getResponsiveIconSize();
    return (
      <div 
        className="border rounded-xl p-2 sm:p-3 md:p-4 hover-lift cursor-pointer transition-all duration-200"
        onClick={onClick}
        style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}
      >
        <div className="flex items-center justify-between mb-1 sm:mb-2">
          <div className="text-xs sm:text-sm font-medium truncate" style={{ color: colors.textSecondary }}>
            {title}
          </div>
          <div className="p-1 sm:p-2 rounded-lg shrink-0" style={{ backgroundColor: `${color}20` }}>
            <Icon size={iconSize} style={{ color }} />
          </div>
        </div>
        <div className="flex items-end justify-between">
          <div className="text-lg sm:text-xl md:text-2xl font-bold truncate" style={{ color: colors.text }}>
            {value}
          </div>
          {change && (
            <div className={`text-xs px-1 sm:px-2 py-0.5 sm:py-1 rounded-full shrink-0 ${change > 0 ? 'text-green-600 bg-green-100' : 'text-red-600 bg-red-100'}`}>
              {change > 0 ? '+' : ''}{change}%
            </div>
          )}
        </div>
      </div>
    );
  };

  // Connection Card - EXACT MATCH from static version
  const ConnectionCard = ({ connection }) => {
    const iconSize = getResponsiveIconSize();
    return (
      <div 
        className="border rounded-xl p-2 sm:p-3 hover-lift cursor-pointer transition-all duration-200"
        onClick={() => handleViewConnectionDetails(connection)}
        style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}
      >
        <div className="flex items-center justify-between mb-1">
          <div className="flex items-center gap-1 sm:gap-2 min-w-0">
            {getDatabaseIcon(connection.type)}
            <span className="text-xs sm:text-sm font-medium truncate" style={{ color: colors.text }}>
              {connection.name}
            </span>
          </div>
          <div className="flex items-center gap-1 shrink-0">
            <div className="w-2 h-2 rounded-full" style={{ backgroundColor: getStatusColor(connection.status) }} />
            <span className="text-xs hidden sm:inline" style={{ color: colors.textSecondary }}>
              {connection.status}
            </span>
          </div>
        </div>
        <div className="text-xs mb-1 truncate" style={{ color: colors.textSecondary }}>
          {connection.host}:{connection.port}
        </div>
        <div className="flex items-center justify-between text-xs">
          <span style={{ color: colors.textSecondary }}>
            Latency: <span style={{ color: colors.text }}>{connection.latency}</span>
          </span>
          <span className="hidden sm:inline" style={{ color: colors.textSecondary }}>
            Uptime: <span style={{ color: colors.text }}>{connection.uptime}</span>
          </span>
        </div>
      </div>
    );
  };

  // Activity Item - EXACT MATCH from static version
  const ActivityItem = ({ activity }) => {
    const iconSize = getResponsiveIconSize();
    return (
      <div 
        className="flex items-start cursor-pointer gap-2 sm:gap-3 p-2 sm:p-3 border-b last:border-b-0 hover:bg-opacity-50 transition-colors hover-lift"
        onClick={() => handleActivityClick(activity)}
        style={{ borderColor: colors.border }}
      >
        <div className="relative shrink-0">
          <div className="p-1 sm:p-1.5 rounded" style={{ backgroundColor: colors.hover }}>
            {getIconForActivity(activity.icon)}
          </div>
          {activity.priority === 'high' && (
            <div className="absolute -top-0.5 -right-0.5 w-1.5 h-1.5 rounded-full" style={{ backgroundColor: getPriorityColor(activity.priority) }} />
          )}
        </div>
        <div className="flex-1 min-w-0 overflow-hidden">
          <div className="flex items-center justify-between gap-1 sm:gap-2">
            <span className="text-xs sm:text-sm font-medium truncate" style={{ color: colors.text }}>
              {activity.action}
            </span>
            <span className="text-xs shrink-0" style={{ color: colors.textSecondary }}>
              {new Date(activity.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </span>
          </div>
          <p className="text-xs mt-0.5 sm:mt-1 truncate" style={{ color: colors.textSecondary }}>
            {activity.description}
          </p>
          <div className="flex items-center justify-between mt-0.5 sm:mt-1 gap-1 sm:gap-2">
            <div className="text-xs truncate" style={{ color: colors.textTertiary }}>
              by {activity.user}
            </div>
            {activity.priority && activity.priority !== 'low' && (
              <div className={`text-xs px-1 sm:px-2 py-0.5 rounded-full capitalize shrink-0 ${
                activity.priority === 'high' ? 'bg-red-500/10 text-red-500' : 'bg-yellow-500/10 text-yellow-500'
              }`}>
                {activity.priority}
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };

  // Schema Stats Card - EXACT MATCH from static version
  const SchemaStatsCard = () => {
    const iconSize = getResponsiveIconSize();
    const schemaData = dashboardData.schemaStats || {};
    
    const schemaCategories = [
      { 
        name: 'Tables & Views', 
        items: [
          { 
            key: 'tables', 
            value: schemaData.tables || 0, 
            icon: <Table size={Math.max(iconSize - 2, 10)} />,
            change: schemaData.tableChange || 0,
            description: 'Data tables'
          },
          { 
            key: 'views', 
            value: schemaData.views || 0, 
            icon: <Eye size={Math.max(iconSize - 2, 10)} />,
            change: schemaData.viewChange || 0,
            description: 'Virtual tables'
          }
        ],
        color: colors.info,
        icon: <Table size={Math.max(iconSize, 12)} />
      },
      { 
        name: 'Program Units', 
        items: [
          { 
            key: 'procedures', 
            value: schemaData.procedures || 0, 
            icon: <FileCode size={Math.max(iconSize - 2, 10)} />,
            change: schemaData.procedureChange || 0,
            description: 'Stored procedures'
          },
          { 
            key: 'functions', 
            value: schemaData.functions || 0, 
            icon: <Code size={Math.max(iconSize - 2, 10)} />,
            change: schemaData.functionChange || 0,
            description: 'Functions'
          }
        ],
        color: colors.success,
        icon: <Code size={Math.max(iconSize, 12)} />
      },
    ];

    return (
      <div className="border rounded-xl p-3 sm:p-4 hover-lift transition-all duration-200" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.card,
        boxShadow: '0 1px 3px rgba(0,0,0,0.05)'
      }}>
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 sm:gap-3 mb-3 sm:mb-4">
          <div className="flex-1 min-w-0">
            <div className="flex flex-wrap items-center gap-1 sm:gap-2 mb-0.5 sm:mb-1">
              <h3 className="text-sm font-semibold truncate" style={{ color: colors.text }}>
                Schema Statistics
              </h3>
              <div className="flex items-center gap-1 px-1.5 sm:px-2 py-0.5 rounded-full text-xs" 
                style={{ 
                  backgroundColor: (schemaData.totalObjectsChange || 0) >= 0 ? `${colors.success}20` : `${colors.error}20`,
                  color: (schemaData.totalObjectsChange || 0) >= 0 ? colors.success : colors.error
                }}>
                <TrendingUp size={10} />
                <span>{Math.abs(schemaData.totalObjectsChange || 0)}</span>
              </div>
            </div>
            <div className="flex flex-wrap items-center gap-1 sm:gap-2">
              <p className="text-xs truncate" style={{ color: colors.textSecondary }}>
                {schemaData.totalObjects || 0} objects
              </p>
              <div className="text-xs flex items-center gap-1" style={{ color: colors.textTertiary }}>
                <Database size={10} />
                <span className="truncate">{schemaData.databaseName || 'Main DB'}</span>
              </div>
            </div>
          </div>
          <div 
            className="flex items-center gap-1 sm:gap-2 shrink-0 cursor-pointer hover-lift p-1.5 sm:p-2 rounded-lg mt-2 sm:mt-0"
            onClick={handleNavigateToSchemaBrowser}
            style={{ backgroundColor: `${colors.primaryDark}15` }}
          >
            <div className="p-1 sm:p-2 rounded-lg" style={{ backgroundColor: `${colors.primaryDark}15` }}>
              <Database size={Math.max(iconSize + 4, 16)} style={{ color: colors.primaryDark }} />
            </div>
            <div className="text-right hidden sm:block">
              <div className="text-xs" style={{ color: colors.textSecondary }}>Size</div>
              <div className="text-sm font-semibold" style={{ color: colors.text }}>
                {schemaData.databaseSize || '2.4 GB'}
              </div>
            </div>
          </div>
        </div>

        {/* Schema categories */}
        <div className="space-y-3 sm:space-y-4">
          {schemaCategories.map((category, index) => {
            const categoryTotal = category.items.reduce((sum, item) => sum + item.value, 0);
            
            return (
              <div key={index} className="space-y-1.5 sm:space-y-2 p-2 sm:p-3 rounded-lg hover-lift" 
                style={{ backgroundColor: colors.hover }}>
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-1 sm:gap-2">
                  <div className="flex items-center gap-1 sm:gap-2">
                    <div className="p-0.5 sm:p-1 rounded" style={{ backgroundColor: `${category.color}20` }}>
                      {category.icon}
                    </div>
                    <div className="min-w-0">
                      <span className="text-xs font-semibold truncate" style={{ color: colors.text }}>
                        {category.name}
                      </span>
                      <div className="flex items-center gap-1 sm:gap-2">
                        <span className="text-xs" style={{ color: colors.textSecondary }}>
                          {categoryTotal} objects
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="text-right shrink-0">
                    <div className="text-xs" style={{ color: colors.textSecondary }}>
                      {schemaData.totalObjects ? ((categoryTotal / schemaData.totalObjects) * 100).toFixed(1) : '0.0'}%
                    </div>
                    <div className="text-xs font-medium hidden sm:block" style={{ color: category.color }}>
                      Δ {category.items.reduce((sum, item) => sum + item.change, 0)} this month
                    </div>
                  </div>
                </div>
                
                <div className="grid grid-cols-2 gap-1.5 sm:gap-2">
                  {category.items.map((item, idx) => (
                    <div 
                      key={idx} 
                      className="border rounded-lg p-1.5 sm:p-2 text-center hover-lift transition-all duration-200 cursor-pointer"
                      onClick={() => handleSchemaItemClick(item.key, item.value)}
                      style={{ 
                        borderColor: colors.borderLight,
                        backgroundColor: colors.card,
                      }}
                    >
                      <div className="flex items-center justify-between mb-0.5 sm:mb-1">
                        <div className="flex items-center gap-0.5 sm:gap-1">
                          {item.icon}
                          <span className="text-xs font-bold" style={{ color: colors.text }}>
                            {item.value}
                          </span>
                        </div>
                        {item.change !== 0 && (
                          <div className={`text-xs ${item.change > 0 ? 'text-green-600' : 'text-red-600'}`}>
                            {item.change > 0 ? '+' : ''}{item.change}
                          </div>
                        )}
                      </div>
                      <div className="text-xs font-medium capitalize mb-0.5 truncate" style={{ color: colors.text }}>
                        {item.key.replace(/([A-Z])/g, ' $1').trim()}
                      </div>
                      <div className="text-xs opacity-75 truncate hidden sm:block" style={{ color: colors.textSecondary }}>
                        {item.description}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            );
          })}
        </div>

        {/* Footer */}
        <div className="mt-4 sm:mt-6 pt-3 sm:pt-4 border-t" style={{ borderColor: colors.border }}>
          <div className="grid grid-cols-3 gap-2 sm:gap-4">
            <div>
              <div className="text-xs" style={{ color: colors.textSecondary }}>Updated</div>
              <div className="text-sm font-medium flex items-center gap-0.5 sm:gap-1 truncate" style={{ color: colors.text }}>
                <Clock size={10} />
                <span className="hidden sm:inline">Today, 10:30 AM</span>
                <span className="sm:hidden">Today</span>
              </div>
            </div>
            <div>
              <div className="text-xs" style={{ color: colors.textSecondary }}>Growth</div>
              <div className="text-sm font-medium flex items-center gap-0.5 sm:gap-1" style={{ color: colors.success }}>
                <TrendingUp size={10} />
                +{schemaData.monthlyGrowth || 12}%
              </div>
            </div>
            <div>
              <div className="text-xs" style={{ color: colors.textSecondary }}>Version</div>
              <div className="text-sm font-medium truncate" style={{ color: colors.text }}>
                v{schemaData.version || '1.2.3'}
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  // Activity Pagination Component - EXACT MATCH from static version
  const ActivityPagination = () => {
    const iconSize = getResponsiveIconSize();
    const totalActivityPages = dashboardData.recentActivities.totalPages || 1;
    const totalElements = dashboardData.recentActivities.totalElements || 0;
    
    return (
      <div className="flex flex-col sm:flex-row items-center justify-between gap-2 sm:gap-3 p-3 sm:p-4 border-t" style={{ borderColor: colors.border }}>
        <div className="text-xs text-center sm:text-left" style={{ color: colors.textSecondary }}>
          Showing {((activityPage - 1) * activitiesPerPage) + 1} - {Math.min(activityPage * activitiesPerPage, totalElements)} of {totalElements}
        </div>
        <div className="flex items-center gap-1 sm:gap-2">
          <button
            onClick={handlePrevPage}
            disabled={activityPage === 1}
            className="p-1 sm:p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
            style={{ 
              backgroundColor: activityPage === 1 ? 'transparent' : colors.hover,
              color: colors.text,
              cursor: activityPage === 1 ? 'not-allowed' : 'pointer'
            }}
          >
            <ChevronLeft size={iconSize} />
          </button>
          
          <div className="flex items-center gap-0.5 sm:gap-1">
            {Array.from({ length: Math.min(3, totalActivityPages) }, (_, i) => {
              let pageNum;
              if (totalActivityPages <= 3) {
                pageNum = i + 1;
              } else if (activityPage === 1) {
                pageNum = i + 1;
              } else if (activityPage === totalActivityPages) {
                pageNum = totalActivityPages - 2 + i;
              } else {
                pageNum = activityPage - 1 + i;
              }
              
              if (pageNum > totalActivityPages) return null;
              
              return (
                <button
                  key={pageNum}
                  onClick={() => setActivityPage(pageNum)}
                  className="w-5 h-5 sm:w-6 sm:h-6 rounded text-xs font-medium transition-colors"
                  style={{ 
                    backgroundColor: activityPage === pageNum ? colors.selected : 'transparent',
                    color: activityPage === pageNum ? colors.primaryDark : colors.textSecondary
                  }}
                >
                  {pageNum}
                </button>
              );
            })}
            
            {totalActivityPages > 3 && activityPage < totalActivityPages - 1 && (
              <>
                <span className="text-xs" style={{ color: colors.textSecondary }}>...</span>
                <button
                  onClick={() => setActivityPage(totalActivityPages)}
                  className="w-5 h-5 sm:w-6 sm:h-6 rounded text-xs font-medium transition-colors"
                  style={{ 
                    backgroundColor: activityPage === totalActivityPages ? colors.selected : 'transparent',
                    color: activityPage === totalActivityPages ? colors.primaryDark : colors.textSecondary
                  }}
                >
                  {totalActivityPages}
                </button>
              </>
            )}
          </div>
          
          <button
            onClick={handleNextPage}
            disabled={activityPage === totalActivityPages}
            className="p-1 sm:p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
            style={{ 
              backgroundColor: activityPage === totalActivityPages ? 'transparent' : colors.hover,
              color: colors.text,
              cursor: activityPage === totalActivityPages ? 'not-allowed' : 'pointer'
            }}
          >
            <ChevronRightIcon size={iconSize} />
          </button>
        </div>
      </div>
    );
  };

  // Mobile Search Bar - EXACT MATCH from static version
  const MobileSearchBar = () => (
    <div className={`md:hidden p-3 border-b transition-all duration-300 ${isMobileSearchOpen ? 'block' : 'hidden'}`} 
      style={{ borderColor: colors.border, backgroundColor: colors.header }}>
      <div className="flex items-center gap-2">
        <Search size={16} style={{ color: colors.textSecondary }} />
        <input
          type="text"
          placeholder="Search connections, APIs, activities..."
          className="flex-1 bg-transparent outline-none text-sm"
          style={{ color: colors.text }}
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
        <button
          onClick={() => setIsMobileSearchOpen(false)}
          className="p-1 rounded hover:bg-opacity-50 transition-colors"
          style={{ backgroundColor: colors.hover }}
        >
          <X size={16} style={{ color: colors.text }} />
        </button>
      </div>
    </div>
  );

  // Modal Components
  const MobileModal = ({ children, title, onClose, showBackButton = false, onBack }) => {
    const iconSize = getResponsiveIconSize();
    const modalCount = modalStack.length;
    const zIndex = 1000 + (modalCount * 10);
    
    return (
      <div 
        className="fixed inset-0 bg-black bg-opacity-50 backdrop-blur-sm flex items-center justify-center z-50 p-2 sm:p-4"
        style={{ 
          zIndex: zIndex - 5,
          width: '100vw',
          height: '100vh',
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0
        }}
        onClick={onClose}
      >
        <div 
          className="border rounded-xl w-[55rem] max-h-[90vh] overflow-auto animate-fade-in"
          onClick={(e) => e.stopPropagation()}
          style={{ 
            backgroundColor: colors.bg,
            borderColor: colors.modalborder,
            zIndex: zIndex
          }}
        >
          <div className="sticky top-0 p-3 sm:p-4 border-b flex items-center justify-between backdrop-blur-sm" style={{ 
            borderColor: colors.border,
            backgroundColor: colors.modalBg
          }}>
            <div className="flex items-center gap-2">
              {showBackButton && (
                <button 
                  onClick={onBack}
                  className="p-1 sm:p-1.5 rounded hover:bg-opacity-50 transition-colors shrink-0"
                  style={{ backgroundColor: colors.hover }}
                >
                  <ChevronLeft size={16} style={{ color: colors.text }} />
                </button>
              )}
              <h3 className="text-base sm:text-lg font-semibold truncate" style={{ color: colors.text }}>
                {title}
              </h3>
            </div>
            <button 
              onClick={onClose}
              className="p-1 sm:p-1.5 rounded hover:bg-opacity-50 transition-colors shrink-0"
              style={{ backgroundColor: colors.hover }}
            >
              <X size={18} style={{ color: colors.text }} />
            </button>
          </div>
          <div className="p-3 sm:p-4 overflow-auto">
            {children}
          </div>
        </div>
      </div>
    );
  };

  // Activity Modal
  const ActivityModal = ({ data }) => (
    <MobileModal 
      title="Activity Details" 
      onClose={closeModal}
      showBackButton={modalStack.length > 1}
      onBack={closeModal}
    >
      <div className="space-y-3 sm:space-y-4">
        <div className="flex items-center gap-2 sm:gap-3">
          <div className="p-1.5 sm:p-2 rounded" style={{ backgroundColor: colors.hover }}>
            {getIconForActivity(data?.icon)}
          </div>
          <div className="min-w-0">
            <h4 className="text-sm sm:text-lg font-semibold truncate" style={{ color: colors.text }}>
              {data?.action}
            </h4>
            <p className="text-xs sm:text-sm truncate" style={{ color: colors.textSecondary }}>
              {data?.description}
            </p>
          </div>
        </div>
        
        <div className="grid grid-cols-2 gap-2 sm:gap-4">
          <div>
            <div className="text-xs font-medium mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>User</div>
            <div className="text-sm truncate" style={{ color: colors.text }}>{data?.user}</div>
          </div>
          <div>
            <div className="text-xs font-medium mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>Time</div>
            <div className="text-sm truncate" style={{ color: colors.text }}>{new Date(data?.timestamp).toLocaleString()}</div>
          </div>
          <div>
            <div className="text-xs font-medium mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>Priority</div>
            <div className="text-sm capitalize truncate" style={{ color: getPriorityColor(data?.priority) }}>
              {data?.priority || 'low'}
            </div>
          </div>
          <div>
            <div className="text-xs font-medium mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>Resource</div>
            <div className="text-sm truncate" style={{ color: colors.text }}>{data?.affectedResource}</div>
          </div>
        </div>
        
        <div>
          <div className="text-xs font-medium mb-1" style={{ color: colors.textSecondary }}>Details</div>
          <div className="p-2 sm:p-3 rounded border text-sm max-h-32 overflow-y-auto" style={{ 
            backgroundColor: colors.codeBg,
            borderColor: colors.border,
            color: colors.text
          }}>
            {data?.details || 'No additional details available.'}
          </div>
        </div>
        
        <div className="pt-3 sm:pt-4 border-t" style={{ borderColor: colors.border }}>
          <div className="flex flex-col sm:flex-row gap-2">
            <button 
              onClick={() => {
                console.log('View related resources for:', data?.action);
                closeAllModals();
              }}
              className="px-3 py-2 rounded text-sm font-medium transition-colors flex-1"
              style={{ 
                backgroundColor: colors.primaryDark,
                color: 'white'
              }}
            >
              View Related
            </button>
            <button 
              onClick={closeModal}
              className="px-3 py-2 rounded text-sm font-medium transition-colors flex-1"
              style={{ 
                backgroundColor: colors.hover,
                color: colors.text
              }}
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </MobileModal>
  );

  // Connection Modal
  const ConnectionModal = ({ data }) => (
    <MobileModal 
      title="Connection Details" 
      onClose={closeModal}
      showBackButton={modalStack.length > 1}
      onBack={closeModal}
    >
      <div className="space-y-4 sm:space-y-6">
        {/* Header Section */}
        <div className="flex items-center gap-3 sm:gap-4 p-3 sm:p-4 rounded-xl" style={{ 
          backgroundColor: colors.hover,
          border: `1px solid ${colors.border}`
        }}>
          <div className="flex-shrink-0 p-3 rounded-lg" style={{ 
            backgroundColor: colors.primary + '15',
            border: `1px solid ${colors.primary}20`
          }}>
            <Database size={22} style={{ color: colors.primary }} />
          </div>
          <div className="min-w-0 flex-1">
            <h4 className="text-base sm:text-lg font-bold truncate" style={{ color: colors.text }}>
              {data?.name}
            </h4>
            <p className="text-xs sm:text-sm text-gray-500 truncate mt-0.5">
              {data?.description || 'No description provided'}
            </p>
          </div>
          <div className="flex items-center gap-2 px-2 py-1 rounded-full text-xs font-medium" style={{ 
            backgroundColor: getStatusColor(data?.status) + '20',
            color: getStatusColor(data?.status),
            border: `1px solid ${getStatusColor(data?.status)}30`
          }}>
            <div className="w-1.5 h-1.5 rounded-full" style={{ backgroundColor: getStatusColor(data?.status) }} />
            <span className="capitalize">{data?.status}</span>
          </div>
        </div>

        {/* Connection Details Grid */}
        <div className="grid grid-cols-2 gap-3 sm:gap-4 p-4 rounded-xl" style={{ 
          backgroundColor: colors.hover + '40',
          border: `1px solid ${colors.border}`
        }}>
          <div className="space-y-1">
            <div className="text-xs font-medium uppercase tracking-wider" style={{ color: colors.textSecondary }}>
              Host
            </div>
            <div className="text-sm font-mono truncate p-1.5 rounded bg-white/10" style={{ color: colors.text }}>
              {data?.host}
            </div>
          </div>
          <div className="space-y-1">
            <div className="text-xs font-medium uppercase tracking-wider" style={{ color: colors.textSecondary }}>
              Port
            </div>
            <div className="text-sm truncate p-1.5 rounded bg-white/10" style={{ color: colors.text }}>
              {data?.port}
            </div>
          </div>
          <div className="space-y-1">
            <div className="text-xs font-medium uppercase tracking-wider" style={{ color: colors.textSecondary }}>
              Service
            </div>
            <div className="text-sm truncate p-1.5 rounded bg-white/10" style={{ color: colors.text }}>
              {data?.service || 'N/A'}
            </div>
          </div>
          <div className="space-y-1">
            <div className="text-xs font-medium uppercase tracking-wider" style={{ color: colors.textSecondary }}>
              Username
            </div>
            <div className="text-sm truncate p-1.5 rounded bg-white/10" style={{ color: colors.text }}>
              {data?.username}
            </div>
          </div>
          <div className="space-y-1">
            <div className="text-xs font-medium uppercase tracking-wider" style={{ color: colors.textSecondary }}>
              Type
            </div>
            <div className="text-sm truncate p-1.5 rounded bg-white/10" style={{ color: colors.text }}>
              {data?.type}
            </div>
          </div>
          <div className="space-y-1">
            <div className="text-xs font-medium uppercase tracking-wider" style={{ color: colors.textSecondary }}>
              Status
            </div>
            <div className="text-sm truncate p-1.5 rounded bg-white/10" style={{ color: colors.text }}>
              {data?.status}
            </div>
          </div>
        </div>

        {/* Performance Metrics */}
        <div className="p-4 rounded-xl" style={{ 
          backgroundColor: colors.hover + '40',
          border: `1px solid ${colors.border}`
        }}>
          <h5 className="text-sm font-semibold mb-3 flex items-center gap-2" style={{ color: colors.text }}>
            <Activity size={16} />
            Performance Metrics
          </h5>
          <div className="grid grid-cols-3 gap-3">
            <div className="text-center p-3 rounded-lg border" style={{ 
              borderColor: colors.border,
              backgroundColor: colors.card
            }}>
              <div className="text-xs font-medium mb-1.5" style={{ color: colors.textSecondary }}>Latency</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {data?.latency ? `${data.latency}` : 'N/A'}
              </div>
            </div>
            <div className="text-center p-3 rounded-lg border" style={{ 
              borderColor: colors.border,
              backgroundColor: colors.card
            }}>
              <div className="text-xs font-medium mb-1.5" style={{ color: colors.textSecondary }}>Uptime</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {data?.uptime ? `${data.uptime}` : 'N/A'}
              </div>
            </div>
            <div className="text-center p-3 rounded-lg border" style={{ 
              borderColor: colors.border,
              backgroundColor: colors.card
            }}>
              <div className="text-xs font-medium mb-1.5" style={{ color: colors.textSecondary }}>Connections</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {data?.currentConnections || 0}/{data?.maxConnections || 0}
              </div>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="pt-2">
          <div className="grid grid-cols-3 gap-2 sm:gap-3">
            <button 
              onClick={() => handleTestConnection(data)}
              className="flex flex-col items-center justify-center p-3 rounded-lg transition-all duration-200 hover:scale-[1.02] active:scale-[0.98]"
              style={{ 
                backgroundColor: colors.info,
                color: 'white'
              }}
            >
              <div className="flex items-center gap-1.5">
                <TestTube size={16} />
                <span className="text-xs font-medium">Test</span>
              </div>
              <div className="text-[10px] opacity-90 mt-0.5">Connection</div>
            </button>
            
            <button 
              onClick={() => {
                console.log('Edit connection:', data?.name);
                closeAllModals();
                handleNavigateToSchemaBrowser();
              }}
              className="flex flex-col items-center justify-center p-3 rounded-lg transition-all duration-200 hover:scale-[1.02] active:scale-[0.98]"
              style={{ 
                backgroundColor: colors.warning,
                color: 'white'
              }}
            >
              <div className="flex items-center gap-1.5">
                <Edit size={16} />
                <span className="text-xs font-medium">Edit</span>
              </div>
              <div className="text-[10px] opacity-90 mt-0.5">Settings</div>
            </button>
            
            <button 
              onClick={handleNavigateToSchemaBrowser}
              className="flex flex-col items-center justify-center p-3 rounded-lg transition-all duration-200 hover:scale-[1.02] active:scale-[0.98]"
              style={{ 
                backgroundColor: colors.primaryDark,
                color: 'white'
              }}
            >
              <div className="flex items-center gap-1.5">
                <Database size={16} />
                <span className="text-xs font-medium">Browse</span>
              </div>
              <div className="text-[10px] opacity-90 mt-0.5">Schema</div>
            </button>
          </div>
          
          <div className="mt-3 pt-3 border-t" style={{ borderColor: colors.border }}>
            <button 
              onClick={() => console.log('Export connection config')}
              className="w-full py-2 px-4 rounded text-sm font-medium transition-colors flex items-center justify-center gap-2"
              style={{ 
                backgroundColor: colors.hover,
                color: colors.textSecondary,
                border: `1px solid ${colors.border}`
              }}
            >
              <Download size={14} />
              Export Configuration
            </button>
          </div>
        </div>
      </div>
    </MobileModal>
  );

  // API Detail Modal
  const ApiDetailModal = ({ data }) => (
    <MobileModal 
      title="API Details" 
      onClose={closeModal}
      showBackButton={modalStack.length > 1}
      onBack={closeModal}
    >
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          <div className="p-2 rounded" style={{ backgroundColor: colors.hover }}>
            <FileCode size={20} style={{ color: colors.primary }} />
          </div>
          <div className="min-w-0">
            <h4 className="text-lg font-semibold truncate" style={{ color: colors.text }}>
              {data?.name}
            </h4>
            <p className="text-sm truncate" style={{ color: colors.textSecondary }}>
              {data?.description}
            </p>
          </div>
        </div>
        
        <div className="grid grid-cols-2 gap-3">
          <div>
            <div className="text-xs font-medium mb-1" style={{ color: colors.textSecondary }}>Version</div>
            <div className="text-sm" style={{ color: colors.text }}>{data?.version}</div>
          </div>
          <div>
            <div className="text-xs font-medium mb-1" style={{ color: colors.textSecondary }}>Status</div>
            <div className="flex items-center gap-1">
              <div className="w-2 h-2 rounded-full" style={{ backgroundColor: getStatusColor(data?.status) }} />
              <span className="text-sm capitalize" style={{ color: colors.text }}>{data?.status}</span>
            </div>
          </div>
          <div>
            <div className="text-xs font-medium mb-1" style={{ color: colors.textSecondary }}>Total Calls</div>
            <div className="text-sm font-medium" style={{ color: colors.text }}>{data?.calls?.toLocaleString() || 0}</div>
          </div>
          <div>
            <div className="text-xs font-medium mb-1" style={{ color: colors.textSecondary }}>Success Rate</div>
            <div className="text-sm font-medium" style={{ 
              color: parseFloat(data?.successRate) >= 99 ? colors.success : 
                    parseFloat(data?.successRate) >= 95 ? colors.warning : colors.error 
            }}>
              {data?.successRate || '0%'}
            </div>
          </div>
        </div>
        
        <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
          <div className="flex flex-col gap-2">
            <button 
              onClick={handleNavigateToApiBuilder}
              className="px-3 py-2 rounded text-sm font-medium transition-colors hover-lift"
              style={{ 
                backgroundColor: colors.primaryDark,
                color: 'white'
              }}
            >
              Edit API
            </button>
            <button 
              onClick={closeModal}
              className="px-3 py-2 rounded text-sm font-medium transition-colors hover-lift"
              style={{ 
                backgroundColor: colors.hover,
                color: colors.text
              }}
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </MobileModal>
  );

  // API Stats Modal Component
  const ApiStatsModal = ({ data }) => {
    const [localApiStatsPage, setLocalApiStatsPage] = useState(1);
    const totalPages = Math.ceil(data.data.length / itemsPerModalPage);
    const startIndex = (localApiStatsPage - 1) * itemsPerModalPage;
    const endIndex = startIndex + itemsPerModalPage;
    const currentPageData = data.data.slice(startIndex, endIndex);

    const handleApiStatsPageChange = (newPage) => {
      setLocalApiStatsPage(newPage);
    };

    const handleViewApiDetails = (api) => {
      openModal('api', api);
    };

    return (
      <MobileModal 
        title={data.title} 
        onClose={closeModal}
        showBackButton={modalStack.length > 1}
        onBack={closeModal}
      >
        <div className="space-y-4">
          {/* Summary */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Total APIs</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {data.totalItems}
              </div>
            </div>
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Total Endpoints</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {data.data.reduce((sum, api) => sum + (api.endpointCount || 0), 0)}
              </div>
            </div>
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Avg Success Rate</div>
              <div className="text-lg font-bold" style={{ color: colors.success }}>
                {(
                  data.data.reduce((sum, api) => {
                    const rate = parseFloat(api.successRate) || 0;
                    return sum + (isNaN(rate) ? 0 : rate);
                  }, 0) / data.data.length
                ).toFixed(1)}%
              </div>
            </div>
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Avg Latency</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {(
                  data.data.reduce((sum, api) => {
                    const latency = parseInt(api.latency) || 0;
                    return sum + latency;
                  }, 0) / data.data.length
                ).toFixed(0)}ms
              </div>
            </div>
          </div>

          {/* API Table */}
          <div className="border rounded-lg overflow-hidden" style={{ borderColor: colors.border }}>
            <div className="overflow-x-auto">
              <table className="w-full" style={{ borderColor: colors.border }}>
                <thead>
                  <tr style={{ backgroundColor: colors.tableHeader }}>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      <div className="flex items-center gap-1">
                        <FileCode size={12} />
                        API Name
                      </div>
                    </th>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      Version
                    </th>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      Calls
                    </th>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      Success Rate
                    </th>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      Latency
                    </th>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      Last Updated
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {currentPageData.map((api, index) => (
                    <tr 
                      key={api.id}
                      className="border-t hover-lift cursor-pointer transition-colors"
                      style={{ 
                        borderColor: colors.border,
                        backgroundColor: index % 2 === 0 ? colors.tableRow : colors.tableRowhover
                      }}
                      onClick={() => handleViewApiDetails(api)}
                    >
                      <td className="p-4">
                        <div className="flex items-center gap-2">
                          <div className="w-2 h-2 rounded-full" style={{ backgroundColor: getStatusColor(api.status) }} />
                          <div className="min-w-0">
                            <div className="text-sm font-medium truncate" style={{ color: colors.text }}>
                              {api.name}
                            </div>
                            <div className="text-sm truncate" style={{ color: colors.textSecondary }}>
                              {api.description}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm font-mono" style={{ color: colors.text }}>
                          {api.version}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm font-medium" style={{ color: colors.text }}>
                          {(api.calls || 0).toLocaleString()}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="flex items-center gap-1">
                          <div className="text-sm font-medium" style={{ 
                            color: parseFloat(api.successRate) >= 99 ? colors.success : 
                                  parseFloat(api.successRate) >= 95 ? colors.warning : colors.error 
                          }}>
                            {api.successRate || '0%'}
                          </div>
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm" style={{ color: colors.text }}>
                          {api.latency || '0ms'}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm" style={{ color: colors.textSecondary }}>
                          {api.lastUpdated ? new Date(api.lastUpdated).toLocaleDateString() : 'N/A'}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Pagination */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-2 pt-4 border-t" style={{ borderColor: colors.border }}>
            <div className="text-xs" style={{ color: colors.textSecondary }}>
              Showing {startIndex + 1} - {Math.min(endIndex, data.data.length)} of {data.data.length} APIs
            </div>
            <div className="flex items-center gap-1">
              <button
                onClick={() => handleApiStatsPageChange(localApiStatsPage - 1)}
                disabled={localApiStatsPage === 1}
                className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
                style={{ 
                  backgroundColor: localApiStatsPage === 1 ? 'transparent' : colors.hover,
                  color: colors.text,
                  cursor: localApiStatsPage === 1 ? 'not-allowed' : 'pointer'
                }}
              >
                <ChevronLeft size={14} />
              </button>
              
              <div className="flex items-center gap-1">
                {Array.from({ length: Math.min(3, totalPages) }, (_, i) => {
                  let pageNum;
                  if (totalPages <= 3) {
                    pageNum = i + 1;
                  } else if (localApiStatsPage === 1) {
                    pageNum = i + 1;
                  } else if (localApiStatsPage === totalPages) {
                    pageNum = totalPages - 2 + i;
                  } else {
                    pageNum = localApiStatsPage - 1 + i;
                  }
                  
                  if (pageNum > totalPages) return null;
                  
                  return (
                    <button
                      key={pageNum}
                      onClick={() => handleApiStatsPageChange(pageNum)}
                      className="w-6 h-6 rounded text-xs font-medium transition-colors"
                      style={{ 
                        backgroundColor: localApiStatsPage === pageNum ? colors.selected : 'transparent',
                        color: localApiStatsPage === pageNum ? colors.primaryDark : colors.textSecondary
                      }}
                    >
                      {pageNum}
                    </button>
                  );
                })}
                
                {totalPages > 3 && localApiStatsPage < totalPages - 1 && (
                  <>
                    <span className="text-xs" style={{ color: colors.textSecondary }}>...</span>
                    <button
                      onClick={() => handleApiStatsPageChange(totalPages)}
                      className="w-6 h-6 rounded text-xs font-medium transition-colors"
                      style={{ 
                        backgroundColor: localApiStatsPage === totalPages ? colors.selected : 'transparent',
                        color: localApiStatsPage === totalPages ? colors.primaryDark : colors.textSecondary
                      }}
                    >
                      {totalPages}
                    </button>
                  </>
                )}
              </div>
              
              <button
                onClick={() => handleApiStatsPageChange(localApiStatsPage + 1)}
                disabled={localApiStatsPage === totalPages}
                className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
                style={{ 
                  backgroundColor: localApiStatsPage === totalPages ? 'transparent' : colors.hover,
                  color: colors.text,
                  cursor: localApiStatsPage === totalPages ? 'not-allowed' : 'pointer'
                }}
              >
                <ChevronRightIcon size={14} />
              </button>
            </div>
          </div>

          {/* Actions */}
          <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
            <div className="flex flex-col sm:flex-row gap-2">
              <button 
                onClick={() => {
                  handleNavigateToApiBuilder();
                  closeModal();
                }}
                className="px-3 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift"
                style={{ 
                  backgroundColor: colors.primaryDark,
                  color: 'white'
                }}
              >
                <div className="flex items-center justify-center gap-1">
                  <FileCode size={14} />
                  <span>Go to API Builder</span>
                </div>
              </button>
              <button 
                onClick={closeModal}
                className="px-3 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift"
                style={{ 
                  backgroundColor: colors.hover,
                  color: colors.text
                }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      </MobileModal>
    );
  };

  // API Calls Modal Component
  const ApiCallsModal = ({ data }) => {
    const [localApiCallsPage, setLocalApiCallsPage] = useState(1);
    const totalPages = Math.ceil(data.data.length / itemsPerModalPage);
    const startIndex = (localApiCallsPage - 1) * itemsPerModalPage;
    const endIndex = startIndex + itemsPerModalPage;
    const currentPageData = data.data.slice(startIndex, endIndex);

    const handleApiCallsPageChange = (newPage) => {
      setLocalApiCallsPage(newPage);
    };

    const handleViewApiDetailsFromCalls = (apiData) => {
      const fullApi = dashboardData.apis?.content?.find(a => a.id === apiData.id);
      if (fullApi) {
        openModal('api', fullApi);
      }
    };

    return (
      <MobileModal 
        title={data.title} 
        onClose={closeModal}
        showBackButton={modalStack.length > 1}
        onBack={closeModal}
      >
        <div className="space-y-4">
          {/* Summary */}
          <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Total Calls</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {data.data.reduce((sum, api) => sum + (api.calls || 0), 0).toLocaleString()}
              </div>
            </div>
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Total Errors</div>
              <div className="text-lg font-bold" style={{ color: colors.error }}>
                {data.data.reduce((sum, api) => sum + (api.errors || 0), 0).toLocaleString()}
              </div>
            </div>
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Avg Response Time</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {(
                  data.data.reduce((sum, api) => {
                    const time = parseInt(api.avgResponseTime) || 0;
                    return sum + time;
                  }, 0) / Math.max(data.data.length, 1)
                ).toFixed(0)}ms
              </div>
            </div>
          </div>

          {/* API Calls Table */}
          <div className="border rounded-lg overflow-hidden" style={{ borderColor: colors.border }}>
            <div className="overflow-x-auto">
              <table className="w-full" style={{ borderColor: colors.border }}>
                <thead>
                  <tr style={{ backgroundColor: colors.tableHeader }}>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      <div className="flex items-center gap-1">
                        <Activity size={12} />
                        API Name
                      </div>
                    </th>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      Calls
                    </th>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      Errors
                    </th>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      Avg Response
                    </th>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      Success Rate
                    </th>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      Owner
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {currentPageData.map((api, index) => (
                    <tr 
                      key={api.id}
                      className="border-t hover-lift cursor-pointer transition-colors"
                      style={{ 
                        borderColor: colors.border,
                        backgroundColor: index % 2 === 0 ? colors.tableRow : colors.tableRowhover
                      }}
                      onClick={() => handleViewApiDetailsFromCalls(api)}
                    >
                      <td className="p-4">
                        <div className="min-w-0">
                          <div className="text-sm font-medium truncate" style={{ color: colors.text }}>
                            {api.name}
                          </div>
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm font-medium" style={{ color: colors.primaryDark }}>
                          {(api.calls || 0).toLocaleString()}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="flex items-center gap-1">
                          <div className="text-sm font-medium" style={{ 
                            color: (api.errors || 0) === 0 ? colors.success : 
                                  (api.errors || 0) < 10 ? colors.warning : colors.error 
                          }}>
                            {api.errors || 0}
                          </div>
                          {(api.errors || 0) > 0 && (
                            <AlertCircle size={10} style={{ color: colors.warning }} />
                          )}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm" style={{ color: colors.text }}>
                          {api.avgResponseTime || 'N/A'}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm font-medium" style={{ 
                          color: parseFloat(api.successRate) >= 99 ? colors.success : 
                                parseFloat(api.successRate) >= 95 ? colors.warning : colors.error 
                        }}>
                          {api.successRate || '0%'}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm" style={{ color: colors.textSecondary }}>
                          {api.owner || 'N/A'}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Pagination */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-2 pt-4 border-t" style={{ borderColor: colors.border }}>
            <div className="text-xs" style={{ color: colors.textSecondary }}>
              Showing {startIndex + 1} - {Math.min(endIndex, data.data.length)} of {data.data.length} APIs
            </div>
            <div className="flex items-center gap-1">
              <button
                onClick={() => handleApiCallsPageChange(localApiCallsPage - 1)}
                disabled={localApiCallsPage === 1}
                className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
                style={{ 
                  backgroundColor: localApiCallsPage === 1 ? 'transparent' : colors.hover,
                  color: colors.text,
                  cursor: localApiCallsPage === 1 ? 'not-allowed' : 'pointer'
                }}
              >
                <ChevronLeft size={14} />
              </button>
              
              <div className="flex items-center gap-1">
                {Array.from({ length: Math.min(3, totalPages) }, (_, i) => {
                  let pageNum;
                  if (totalPages <= 3) {
                    pageNum = i + 1;
                  } else if (localApiCallsPage === 1) {
                    pageNum = i + 1;
                  } else if (localApiCallsPage === totalPages) {
                    pageNum = totalPages - 2 + i;
                  } else {
                    pageNum = localApiCallsPage - 1 + i;
                  }
                  
                  if (pageNum > totalPages) return null;
                  
                  return (
                    <button
                      key={pageNum}
                      onClick={() => handleApiCallsPageChange(pageNum)}
                      className="w-6 h-6 rounded text-xs font-medium transition-colors"
                      style={{ 
                        backgroundColor: localApiCallsPage === pageNum ? colors.selected : 'transparent',
                        color: localApiCallsPage === pageNum ? colors.primaryDark : colors.textSecondary
                      }}
                    >
                      {pageNum}
                    </button>
                  );
                })}
                
                {totalPages > 3 && localApiCallsPage < totalPages - 1 && (
                  <>
                    <span className="text-xs" style={{ color: colors.textSecondary }}>...</span>
                    <button
                      onClick={() => handleApiCallsPageChange(totalPages)}
                      className="w-6 h-6 rounded text-xs font-medium transition-colors"
                      style={{ 
                        backgroundColor: localApiCallsPage === totalPages ? colors.selected : 'transparent',
                        color: localApiCallsPage === totalPages ? colors.primaryDark : colors.textSecondary
                      }}
                    >
                      {totalPages}
                    </button>
                  </>
                )}
              </div>
              
              <button
                onClick={() => handleApiCallsPageChange(localApiCallsPage + 1)}
                disabled={localApiCallsPage === totalPages}
                className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
                style={{ 
                  backgroundColor: localApiCallsPage === totalPages ? 'transparent' : colors.hover,
                  color: colors.text,
                  cursor: localApiCallsPage === totalPages ? 'not-allowed' : 'pointer'
                }}
              >
                <ChevronRightIcon size={14} />
              </button>
            </div>
          </div>

          {/* Actions */}
          <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
            <div className="flex flex-col sm:flex-row gap-2">
              <button 
                onClick={() => {
                  console.log('Exporting API calls data');
                  alert('Export functionality would be implemented here');
                }}
                className="px-3 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift"
                style={{ 
                  backgroundColor: colors.info,
                  color: 'white'
                }}
              >
                <div className="flex items-center justify-center gap-1">
                  <Download size={14} />
                  <span>Export Data</span>
                </div>
              </button>
              <button 
                onClick={closeModal}
                className="px-3 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift"
                style={{ 
                  backgroundColor: colors.hover,
                  color: colors.text
                }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      </MobileModal>
    );
  };

  // Test Connection Modal
  const TestConnectionModal = ({ data }) => {
    const [progress, setProgress] = useState(data.progress);
    
    useEffect(() => {
      if (data.status === 'testing') {
        const interval = setInterval(() => {
          setProgress(prev => {
            if (prev >= 100) {
              clearInterval(interval);
              setTimeout(() => {
                closeModal();
                openModal('testResults', {
                  connection: data.connection,
                  status: 'success',
                  message: 'Connection test successful!'
                });
              }, 500);
              return 100;
            }
            return prev + 25;
          });
        }, 300);
        
        return () => clearInterval(interval);
      }
    }, [data.status]);

    return (
      <MobileModal 
        title="Testing Connection" 
        onClose={closeModal}
        showBackButton={modalStack.length > 1}
        onBack={closeModal}
      >
        <div className="space-y-4">
          <div className="text-center">
            <div className="w-16 h-16 mx-auto mb-4 relative">
              <div className="absolute inset-0 flex items-center justify-center">
                <Database size={24} style={{ color: colors.primary }} />
              </div>
            </div>
            <div className="text-sm font-medium mb-2" style={{ color: colors.text }}>
              Testing connection to {data.connection?.name}
            </div>
            <div className="text-xs" style={{ color: colors.textSecondary }}>
              Checking connectivity, authentication, and permissions...
            </div>
          </div>

          <div className="space-y-2">
            <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
              <div 
                className="h-full bg-green-500 transition-all duration-300"
                style={{ width: `${progress}%` }}
              />
            </div>
            <div className="text-xs text-center" style={{ color: colors.textSecondary }}>
              {progress}% complete
            </div>
          </div>

          <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
            <button 
              onClick={closeModal}
              className="w-full px-4 py-2 rounded text-sm font-medium transition-colors hover-lift"
              style={{ 
                backgroundColor: colors.hover,
                color: colors.text
              }}
            >
              Cancel Test
            </button>
          </div>
        </div>
      </MobileModal>
    );
  };

  // Test Results Modal
  const TestResultsModal = ({ data }) => {
    return (
      <MobileModal 
        title="Test Results" 
        onClose={closeModal}
        showBackButton={modalStack.length > 1}
        onBack={closeModal}
      >
        <div className="space-y-4">
          <div className="text-center">
            <div className={`w-16 h-16 mx-auto mb-4 rounded-full flex items-center justify-center ${colors.success}20`}>
              <CheckCircle size={32} style={{ color: colors.success }} />
            </div>
            <div className="text-sm font-medium mb-2" style={{ color: colors.text }}>
              {data.message}
            </div>
            <div className="text-xs" style={{ color: colors.textSecondary }}>
              Connection to {data.connection?.name} was successful
            </div>
          </div>

          <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
            <button 
              onClick={closeModal}
              className="w-full px-4 py-2 rounded text-sm font-medium transition-colors hover-lift"
              style={{ 
                backgroundColor: colors.success,
                color: 'white'
              }}
            >
              OK
            </button>
          </div>
        </div>
      </MobileModal>
    );
  };

  // Schema Item Modal
  const SchemaItemModal = ({ data }) => {
    const itemName = data.type.replace(/([A-Z])/g, ' $1').trim();
    
    return (
      <MobileModal 
        title={itemName} 
        onClose={closeModal}
        showBackButton={modalStack.length > 1}
        onBack={closeModal}
      >
        <div className="space-y-4">
          <div className="text-center p-4 rounded border" style={{ borderColor: colors.border }}>
            <div className="text-2xl font-bold" style={{ color: colors.text }}>
              {data.count}
            </div>
            <div className="text-sm capitalize" style={{ color: colors.textSecondary }}>
              {itemName}s in Database
            </div>
          </div>

          <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
            <div className="flex flex-col sm:flex-row gap-2">
              <button 
                onClick={handleNavigateToSchemaBrowser}
                className="px-4 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift"
                style={{ 
                  backgroundColor: colors.primaryDark,
                  color: 'white'
                }}
              >
                Browse {itemName}s
              </button>
              <button 
                onClick={closeModal}
                className="px-4 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift"
                style={{ 
                  backgroundColor: colors.hover,
                  color: colors.text
                }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      </MobileModal>
    );
  };

  // Modal Renderer Component
  const ModalRenderer = () => {
    if (modalStack.length === 0) return null;
    
    return modalStack.map((modal, index) => {
      const isActive = index === modalStack.length - 1;
      if (!isActive) return null;
      
      switch(modal.type) {
        case 'activity':
          return <ActivityModal key={index} data={modal.data} />;
        case 'connection':
          return <ConnectionModal key={index} data={modal.data} />;
        case 'api':
          return <ApiDetailModal key={index} data={modal.data} />;
        case 'apiStats':
          return <ApiStatsModal key={index} data={modal.data} />;
        case 'apiCalls':
          return <ApiCallsModal key={index} data={modal.data} />;
        case 'testConnection':
          return <TestConnectionModal key={index} data={modal.data} />;
        case 'testResults':
          return <TestResultsModal key={index} data={modal.data} />;
        case 'schemaItem':
          return <SchemaItemModal key={index} data={modal.data} />;
        default:
          return null;
      }
    });
  };

  // Right Sidebar Component - EXACT MATCH from static version
  const RightSidebar = () => (
    <div className={`w-full md:w-80 border-l flex flex-col fixed md:relative inset-y-0 right-0 z-40 transform transition-transform duration-300 ease-in-out ${
      isRightSidebarVisible ? 'translate-x-0' : 'translate-x-full md:translate-x-0'
    }`} style={{ 
      backgroundColor: colors.sidebar,
      borderColor: colors.border,
      height: '100vh',
      maxHeight: '100vh',
      top: 0
    }}>
      {/* Mobile sidebar header */}
      <div className="flex items-center justify-between p-3 border-b md:hidden mb-1 mt-2" style={{ borderColor: colors.border }}>
        <h3 className="text-sm font-semibold" style={{ color: colors.text }}>
          Quick Actions
        </h3>
        <button 
          onClick={() => setIsRightSidebarVisible(false)}
          className="p-1.5 rounded hover:bg-opacity-50 transition-colors"
          style={{ backgroundColor: colors.hover }}
        >
          <X size={16} style={{ color: colors.text }} />
        </button>
      </div>

      <div className="flex-1 overflow-auto">
        {/* Quick Actions */}
        <div className="p-3 md:p-4">
          <h3 className="text-sm uppercase font-semibold mb-5 mt-1 hidden md:block" style={{ color: colors.text }}>
            Quick Actions
          </h3>
          <div className="space-y-6 sm:space-y-6 ">
            <button 
              onClick={handleApiGeneration}
              className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ 
                // backgroundColor: colors.card,
                color: colors.text 
              }}
            >
              <FileCode size={14} />
              <span className="truncate">Generate New API</span>
            </button>
            <button 
              onClick={handleNavigateToAPICollection}
              className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ 
                // backgroundColor: colors.card,
                color: colors.text 
              }}
            >
              <Database size={14} />
              <span className="truncate">API Collections</span>
            </button>
            <button 
              onClick={handleNavigateToDocumentation}
              className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ 
                // backgroundColor: colors.card,
                color: colors.text 
              }}
            >
              <BookOpen size={14} />
              <span className="truncate">API Documentation</span>
            </button>
            <button 
              onClick={handleNavigateToCodeBase}
              className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ 
                // backgroundColor: colors.card,
                color: colors.text 
              }}
            >
              <Code size={14} />
              <span className="truncate">API Code Base</span>
            </button>
            <button 
              onClick={handleNavigateToAPISecurity}
              className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ 
                // backgroundColor: colors.card,
                color: colors.text 
              }}
            >
              <Shield size={14} />
              <span className="truncate">API Security</span>
            </button>
            <button 
              onClick={handleNavigateToUserManagement}
              className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ 
                // backgroundColor: colors.card,
                color: colors.text 
              }}
            >
              <UserCog size={14} />
              <span className="truncate">User Management</span>
            </button>
          </div>
        </div>

        {/* Recent Deployments */}
        <div className="border-t p-3 md:p-4 hidden md:block" style={{ borderColor: colors.border }}>
          <div className="flex items-center justify-between mb-7 mt-2">
            <h3 className="text-sm font-semibold uppercase" style={{ color: colors.text }}>
              Recent Deployments
            </h3>
            <Rocket size={14} style={{ color: colors.textSecondary }} />
          </div>
          <div className="space-y-5 mt-2">
            {[
              { name: 'User API v2.1', env: 'Production', status: 'success', time: '2 hours ago' },
              { name: 'Payment API v1.5', env: 'Staging', status: 'success', time: '1 day ago' },
              { name: 'Inventory API', env: 'Development', status: 'pending', time: '2 days ago' }
            ].map((deployment, index) => (
              <div 
                key={index} 
                className="flex items-center justify-between p-2 rounded hover-lift cursor-pointer"
                onClick={handleNavigateToApiBuilder}
                style={{ backgroundColor: colors.hover }}
              >
                <div className="min-w-0 flex-1">
                  <div className="text-xs font-medium truncate" style={{ color: colors.text }}>{deployment.name}</div>
                  <div className="text-xs truncate" style={{ color: colors.textSecondary }}>{deployment.env}</div>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <div className={`w-2 h-2 rounded-full ${
                    deployment.status === 'success' ? 'bg-green-500' :
                    deployment.status === 'pending' ? 'bg-yellow-500' : 'bg-red-500'
                  }`} />
                  <span className="text-xs" style={{ color: colors.textSecondary }}>{deployment.time}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );

  // Loading state
  const renderLoadingState = () => (
    <div className="flex items-center justify-center h-64">
      <div className="text-center">
        <RefreshCw className="animate-spin mx-auto mb-4" size={24} style={{ color: colors.textSecondary }} />
        <div style={{ color: colors.text }}>Loading dashboard data...</div>
      </div>
    </div>
  );

  // Error state
  const renderErrorState = () => (
    <div className="p-4 border rounded-xl" style={{ 
      borderColor: colors.error,
      backgroundColor: `${colors.error}20`
    }}>
      <div className="flex items-center gap-2">
        <AlertCircle size={16} style={{ color: colors.error }} />
        <div style={{ color: colors.error }}>{error}</div>
      </div>
      <button 
        onClick={fetchDashboardData}
        className="mt-2 px-3 py-1 rounded text-sm font-medium transition-colors"
        style={{ 
          backgroundColor: colors.hover,
          color: colors.text
        }}
      >
        Retry
      </button>
    </div>
  );

  // Empty state
  const renderEmptyState = () => (
    <div className="text-center p-8">
      <Database size={48} className="mx-auto mb-4" style={{ color: colors.textSecondary }} />
      <div className="text-sm" style={{ color: colors.text }}>No dashboard data available</div>
      <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
        Connect to a database or create APIs to see data here
      </div>
    </div>
  );

  // Main render
  if (loading) {
    return renderLoadingState();
  }

  if (error) {
    return renderErrorState();
  }

  const hasData = dashboardData.stats.totalConnections > 0 || 
                  dashboardData.apis.content?.length > 0 ||
                  dashboardData.recentActivities.content?.length > 0;

  return (
    <div className="flex flex-col h-screen min-h-screen" style={{ 
      backgroundColor: colors.bg,
      color: colors.text,
      fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif',
      fontSize: '13px'
    }}>
      <style>{`
        .hover-lift:hover {
          transform: translateY(-2px);
          transition: transform 0.2s ease;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        .animate-fade-in {
          animation: fadeIn 0.2s ease-out;
        }
        
        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        
        /* Custom scrollbar - Updated to match Login component */
        ::-webkit-scrollbar {
          width: 6px;
          height: 6px;
        }
        
        ::-webkit-scrollbar-track {
          background: ${isDark ? 'rgb(51 65 85)' : '#e2e8f0'};
          border-radius: 4px;
        }
        
        ::-webkit-scrollbar-thumb {
          background: ${isDark ? 'rgb(100 116 139)' : '#94a3b8'};
          border-radius: 4px;
        }
        
        ::-webkit-scrollbar-thumb:hover {
          background: ${isDark ? 'rgb(148 163 184)' : '#64748b'};
        }
        
        /* Mobile optimizations */
        @media (max-width: 640px) {
          .text-xs { font-size: 11px; }
          .text-sm { font-size: 12px; }
          .text-base { font-size: 14px; }
          .text-lg { font-size: 16px; }
          .text-xl { font-size: 18px; }
          .text-2xl { font-size: 20px; }
          
          ::-webkit-scrollbar {
            width: 4px;
            height: 4px;
          }
        }
      `}</style>

      <MobileSearchBar />
      
      {/* Render all active modals */}
      <ModalRenderer />

      {/* Main Content */}
      <div className="flex-1 overflow-hidden flex z-20">
        {/* Mobile sidebar overlay */}
        {isRightSidebarVisible && (
          <div 
            className="fixed inset-0 bg-black bg-opacity-50 z-30 md:hidden"
            onClick={() => setIsRightSidebarVisible(false)}
          />
        )}

        {/* Right Sidebar */}
        <RightSidebar />

        {/* Main content area */}
        <div className="flex-1 overflow-auto p-2 sm:p-3 md:p-4 h-[calc(100vh-4rem)]">
          <div className="max-w-9xl mx-auto px-1 sm:px-2 md:pl-5 md:pr-5">
            {/* Desktop Header */}
            <div className="hidden md:flex items-center justify-between mb-4 md:mb-6">
              <div>
                <h1 className="text-xl md:text-xl font-bold" style={{ color: colors.text }}>
                  Dashboard
                </h1>
                <p className="text-xs md:text-sm" style={{ color: colors.textSecondary }}>
                  Overview of your API connections and activity
                </p>
              </div>
              <div className="flex items-center gap-3">
                <button 
                  onClick={toggleRightSidebar}
                  className="p-2 rounded-lg hover-lift transition-all duration-200 md:hidden"
                  style={{ 
                    backgroundColor: colors.hover,
                    color: colors.text
                  }}
                >
                  <SlidersHorizontal size={18} />
                </button>
                <button 
                  onClick={handleApiGeneration}
                  className="w-full px-3 py-2 bg-gradient-to-r from-blue-500 via-violet-500 to-blue-500 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift cursor-pointer"
                  style={{ 
                // backgroundColor: colors.card,
                color: 'white' 
              }}
                >
                  <FileCode size={14} />
                  <span className="truncate">Generate New API</span>
                </button>
                <button 
                  onClick={handleRefresh}
                  className="p-2 rounded-lg hover-lift transition-all duration-200"
                  style={{ 
                    backgroundColor: colors.hover,
                    color: colors.text
                  }}
                  disabled={refreshLoading}
                >
                  <RefreshCw size={18} className={refreshLoading ? 'animate-spin' : ''} />
                </button>
              </div>
            </div>

            {/* Key Metrics */}
            <div className="grid grid-cols-2 md:grid-cols-2 lg:grid-cols-4 gap-1.5 sm:gap-2 md:gap-4 mb-3 sm:mb-4 md:mb-6">
              <StatCard
                title="Connections"
                value={dashboardData.stats.totalConnections}
                icon={Database}
                change={+5}
                color={colors.success}
                onClick={handleViewAllConnections}
              />
              <StatCard
                title="Active APIs"
                value={dashboardData.stats.activeApis}
                icon={FileCode}
                change={+12}
                color={colors.info}
                onClick={handleApiStatsClick}
              />
              <StatCard
                title="API Calls"
                value={dashboardData.stats.totalCalls.toLocaleString()}
                icon={Activity}
                change={+8.5}
                color={colors.primaryDark}
                onClick={handleApiCallsClick}
              />
              <StatCard
                title="Success Rate"
                value={dashboardData.stats.successRate}
                icon={CheckCircle}
                change={+0.2}
                color={colors.success}
                onClick={handleViewPerformanceMetrics}
              />
            </div>

            {/* Main Grid */}
            {hasData ? (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-3 sm:gap-4 md:gap-6">
                {/* Left Column */}
                <div className="flex flex-col gap-3 sm:gap-4 md:gap-6">
                  {/* Active Connections */}
                  {dashboardData.connections.content?.length > 0 && (
                    <div className="border rounded-xl" style={{ 
                      borderColor: colors.border,
                      backgroundColor: colors.card
                    }}>
                      <div className="p-2 sm:p-3 md:p-4 border-b" style={{ borderColor: colors.border }}>
                        <div className="flex items-center justify-between">
                          <h3 className="text-xs sm:text-sm font-semibold truncate" style={{ color: colors.text }}>
                            Active Connections
                          </h3>
                          <div className="flex items-center gap-1 sm:gap-2">
                            <span className="text-xs hidden sm:inline" style={{ color: colors.textSecondary }}>
                              {dashboardData.connections.content.length} connections
                            </span>
                            <button 
                              onClick={handleNavigateToAPICollection}
                              className="p-0.5 sm:p-1 rounded hover:bg-opacity-50 transition-colors hover-lift shrink-0"
                              style={{ backgroundColor: colors.hover }}
                            >
                              <Plus size={12} style={{ color: colors.textSecondary }} />
                            </button>
                          </div>
                        </div>
                      </div>
                      <div className="p-2 sm:p-3 md:p-4">
                        <div className="space-y-2 sm:space-y-3">
                          {dashboardData.connections.content.slice(0, 3).map(conn => (
                            <ConnectionCard key={conn.id} connection={conn} />
                          ))}
                        </div>
                      </div>
                    </div>
                  )}

                  {/* Schema Statistics */}
                  {dashboardData.schemaStats && Object.keys(dashboardData.schemaStats).length > 0 && (
                    <SchemaStatsCard />
                  )}
                </div>

                {/* Right Column */}
                <div className="flex flex-col gap-3 sm:gap-4 md:gap-6">
                  <div className="border rounded-xl flex flex-col h-full" style={{ 
                    borderColor: colors.border,
                    backgroundColor: colors.card
                  }}>
                    <div className="p-2 sm:p-3 md:p-4 border-b flex-shrink-0" style={{ borderColor: colors.border }}>
                      <div className="flex items-center justify-between">
                        <h3 className="text-xs sm:text-sm font-semibold" style={{ color: colors.text }}>
                          Recent Activity
                        </h3>
                        <div className="flex items-center gap-1 sm:gap-2">
                          <select
                            value={activitiesPerPage}
                            onChange={(e) => setActivitiesPerPage(Number(e.target.value))}
                            className="text-xs px-1 sm:px-2 py-0.5 sm:py-1 rounded border bg-transparent hidden sm:block"
                            style={{ 
                              borderColor: colors.border,
                              color: colors.text
                            }}
                          >
                            <option value={5}>5 per page</option>
                            <option value={10}>10 per page</option>
                            <option value={15}>15 per page</option>
                          </select>
                          <Clock size={12} style={{ color: colors.textSecondary }} />
                        </div>
                      </div>
                    </div>
                    <div className="flex-1 overflow-auto min-h-0 space-y-1.5 sm:space-y-3">
                      {dashboardData.recentActivities.content?.length > 0 ? (
                        dashboardData.recentActivities.content.map(activity => (
                          <ActivityItem key={activity.id} activity={activity} />
                        ))
                      ) : (
                        <div className="p-4 text-center">
                          <div className="text-sm" style={{ color: colors.textSecondary }}>No recent activity</div>
                        </div>
                      )}
                    </div>
                    <ActivityPagination />
                  </div>
                </div>
              </div>
            ) : (
              renderEmptyState()
            )}
          </div>
        </div>
      </div>

      {/* Mobile Bottom Navigation - EXACT MATCH from static version */}
      <div className="md:hidden fixed bottom-0 left-0 right-0 border-t" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.header,
        zIndex: 40
      }}>
        <div className="grid grid-cols-4 p-2">
          <button 
            onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
            className="flex flex-col items-center p-1 rounded transition-colors"
            style={{ color: colors.textSecondary }}
          >
            <Home size={16} />
            <span className="text-xs mt-0.5">Home</span>
          </button>
          <button 
            onClick={handleNavigateToSchemaBrowser}
            className="flex flex-col items-center p-1 rounded transition-colors"
            style={{ color: colors.textSecondary }}
          >
            <Database size={16} />
            <span className="text-xs mt-0.5">Schema</span>
          </button>
          <button 
            onClick={handleApiGeneration}
            className="flex flex-col items-center p-1 rounded transition-colors"
            style={{ color: colors.textSecondary }}
          >
            <FileCode size={16} />
            <span className="text-xs mt-0.5">APIs</span>
          </button>
          <button 
            onClick={toggleRightSidebar}
            className="flex flex-col items-center p-1 rounded transition-colors"
            style={{ color: colors.textSecondary }}
          >
            <Settings size={16} />
            <span className="text-xs mt-0.5">More</span>
          </button>
        </div>
      </div>
      
      {/* Add padding at bottom for mobile nav */}
      <div className="md:hidden h-16"></div>
    </div>
  );
};

export default Dashboard;