// Dashboard.jsx - CLEANED UP VERSION
import React, { useState, useEffect, useCallback } from 'react';
import {
  Database, FileCode, Activity, Zap, Settings,
  Search, ChevronDown, MoreVertical, RefreshCw,
  Plus, CheckCircle, AlertCircle, TrendingUp, Users,
  Shield, Download, Filter, Eye, Edit, Trash2,
  Globe, Cpu, HardDrive, Network, Lock, Key, Sun, Moon,
  BarChart3, Calendar, Clock, X, AlertTriangle,
  Database as DatabaseIcon, Folder, FolderOpen, FileText,
  Code, Cloud, ShieldCheck, CreditCard, Package, PieChart,
  Table, Grid, List, Mail, Layers, GitMerge,
  BarChart, Terminal, Cpu as CpuIcon, Loader,
  FileJson, BookOpen, Share2, Upload, EyeOff, Type, Palette, TrendingDown,
  Contrast, VolumeX, ZapOff, GitPullRequest, ShieldAlert,
  CalendarDays, DatabaseZap, Network as NetworkIcon, FileOutput, TestTube,
  Code2, Search as SearchIcon, DownloadCloud, UploadCloud,
  UserCheck, KeyRound, FolderTree, BookMarked, LayoutDashboard, Sliders, ChevronRight,
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
  getComprehensiveDashboard,
  handleDashboardResponse
} from "../controllers/DashboardController.js";
import ApiGenerationModal from '@/components/modals/ApiGenerationModal.js';

const Dashboard = ({ theme, isDark, customTheme, toggleTheme, navigateTo, setActiveTab, authToken }) => {
  // Loading states
  const [loading, setLoading] = useState({
    initialLoad: true,
    refresh: false,
    search: false
  });
  

  // Add this state near other state declarations (around line where other modals are declared)
  const [showApiModal, setShowApiModal] = useState(false);
  const [selectedForApiGeneration, setSelectedForApiGeneration] = useState(null);

  // Replace the handleApiGeneration function (around line where other handlers are)
  const handleApiGeneration = useCallback(() => {
    setSelectedForApiGeneration(null); // No specific object selected from dashboard
    setShowApiModal(true);
  }, []);

  // If you want to generate API from a specific connection/API, you can add:
  const handleGenerateApiFromConnection = useCallback((connection) => {
    setSelectedForApiGeneration({
      name: connection.name,
      type: 'CONNECTION',
      id: connection.id
    });
    setShowApiModal(true);
  }, []);


  // Pagination for API endpoints
  const [apiPage, setApiPage] = useState(1);
  const [apisPerPage, setApisPerPage] = useState(6);
  
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

  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState(null);
  
  // Modal states
  const [modalStack, setModalStack] = useState([]);

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

  // Add this with other handlers
const handleGenerateAPIFromModal = useCallback(async (objectType, objectName, apiType, options) => {
  try {
    // You can implement the actual API generation logic here
    // This might call a controller function similar to the SchemaBrowser
    console.log('Generating API from dashboard:', { objectType, objectName, apiType, options });
    
    // After successful generation, you might want to show a success message
    // and optionally navigate to the API collections
    setShowApiModal(false);
    
    // Optional: Navigate to API collections after generation
    // setActiveTab('api-collections');
    
    return { success: true };
  } catch (error) {
    console.error('API generation failed:', error);
    throw error;
  }
}, [authToken]);

  // Color scheme - EXACT MATCH from Login component
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

  // Transform API data function
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
      owner: endpoint.collectionName,
      collectionId: endpoint.collectionId,
      collectionName: endpoint.collectionName,
      lastUpdated: new Date(now.getTime() - (index * 3600000)).toISOString(),
      timeAgo: getTimeAgo(new Date(now.getTime() - (index * 3600000)))
    }))
    .sort((a, b) => new Date(b.lastUpdated) - new Date(a.lastUpdated));
    
    const totalApis = endpoints.length;
    const totalCollections = collections.length;
    const totalCodeImplementations = stats.totalCodeImplementations || data.codeGenerationSummary?.totalImplementations || 0;
    const supportedLanguages = stats.supportedLanguages || data.codeGenerationSummary?.supportedLanguages || 0;
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
        totalCodeImplementations: totalCodeImplementations,
        totalCollections: totalCollections,
        supportedLanguages: supportedLanguages
      },
      connections: connectionsData,
      apis: apisData,
      codeGenerationSummary: data.codeGenerationSummary || {},
      systemHealth: data.loadBalancers?.performance || {},
      securitySummary: data.securitySummary || {},
      users: data.users || {},
      lastUpdated: data.generatedAt || new Date().toISOString(),
      generatedFor: data.generatedFor || 'User'
    };
  };

  // Helper function to format time ago
  const getTimeAgo = (date) => {
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) return `${diffInSeconds} seconds ago`;
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} minutes ago`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} hours ago`;
    return `${Math.floor(diffInSeconds / 86400)} days ago`;
  };

  // Fetch dashboard data from API
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

  // Search handler
  const handleSearch = async () => {
    if (!authToken || !searchQuery.trim()) return;
    
    setLoading(prev => ({ ...prev, search: true }));
    try {
      const filteredApis = dashboardData.apis.filter(api => 
        api.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        api.description?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        api.collectionName?.toLowerCase().includes(searchQuery.toLowerCase())
      );
      
      const filteredConnections = dashboardData.connections.filter(conn => 
        conn.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        conn.description?.toLowerCase().includes(searchQuery.toLowerCase())
      );
      
      setSearchResults({
        apis: filteredApis,
        connections: filteredConnections,
        total: filteredApis.length + filteredConnections.length
      });
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setLoading(prev => ({ ...prev, search: false }));
    }
  };

  // Initialize and fetch data
  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

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
    closeAllModals();
    setActiveTab('api-collections');
  };


  // Export data should navigate to Documentation
  const handleExportData = () => {
    closeAllModals();
    handleNavigateToDocumentation();
  };

  // View all connections should navigate to Connections
  const handleViewAllConnections = () => {
    closeAllModals();
    handleNavigateToConnections();
  };

  // View generated code base
  const handleCollectionsClick = () => {
    closeAllModals();
    setActiveTab('api-collections');
  };

  // View generated code base
  const handleCodeBaseClick = () => {
    closeAllModals();
    setActiveTab('code-base');
  };

  // View API stats should show modal
  const handleApiStatsClick = () => {
    const activeApis = dashboardData.apis?.filter(api => api.status === 'active') || [];
    openModal('apiStats', {
      title: 'Active APIs',
      data: activeApis,
      totalItems: activeApis.length
    });
  };

  // View API calls should show modal
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

  const handleNavigateToAPICollection = () => {
    closeAllModals();
    setActiveTab('api-collections');
  };

  const handleViewConnectionDetails = (connection) => {
    closeAllModals();
    setActiveTab('api-collections');
  };

  const handleSchemaItemClick = (itemType, count) => {
    openModal('schemaItem', { type: itemType, count });
  };

  // Test connection
  const handleTestConnection = (connection) => {
    openModal('testConnection', {
      connection: connection,
      status: 'testing',
      progress: 0
    });
    
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

  // API Pagination handlers
  const handlePrevApiPage = () => {
    if (apiPage > 1) {
      setApiPage(apiPage - 1);
    }
  };

  const handleNextApiPage = () => {
    const totalApiPages = Math.ceil(dashboardData.apis.length / apisPerPage);
    if (apiPage < totalApiPages) {
      setApiPage(apiPage + 1);
    }
  };

  // Time range change handler
  const handleTimeRangeChange = (range) => {
    setTimeRange(range);
  };

  // Get current page of APIs
  const getCurrentPageApis = () => {
    const startIndex = (apiPage - 1) * apisPerPage;
    const endIndex = startIndex + apisPerPage;
    return dashboardData.apis.slice(startIndex, endIndex);
  };

  // Helper functions
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
      case 'REST API': return <FileCode {...iconProps} />;
      case 'GraphQL': return <GitGraph {...iconProps} />;
      case 'WebSocket': return <Wifi {...iconProps} />;
      case 'gRPC': return <GitBranch {...iconProps} />;
      default: return <FileCode {...iconProps} />;
    }
  };

  // Loading Overlay Component
  const LoadingOverlay = () => {
  const isLoading = loading.initialLoad || loading.refresh;
  
  const getLoadingMessage = () => {
    if (loading.initialLoad) return 'Initializing Dashboard...';
    if (loading.refresh) return 'Refreshing dashboard data...';
    return 'Please wait while we load your dashboard';
  };

  const getLoadingTip = () => {
    if (loading.initialLoad) {
      return `Loading ${dashboardData.stats.totalApis || ''} APIs and collections...`;
    }
    if (loading.refresh) {
      return 'Updating metrics and recent activity...';
    }
    return 'This won\'t take long';
  };

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
          {getLoadingMessage()}
        </h3>
        <p className="text-sm mb-2" style={{ color: colors.textSecondary }}>
          {getLoadingTip()}
        </p>
        <p className="text-xs" style={{ color: colors.textTertiary }}>
          This won't take long
        </p>
      </div>
    </div>
  );
};

  // Stat Card Component
  const StatCard = ({ title, value, icon: Icon, change, color, onClick }) => {
    const iconSize = getResponsiveIconSize();
    return (
      <div 
        className="border rounded-xl p-2 sm:p-3 md:p-4 hover-lift cursor-pointer transition-all duration-200"
        onClick={onClick}
        style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card,
          backdropFilter: isDark ? 'blur(10px)' : 'none'
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

  // Connection Card
  const ConnectionCard = ({ connection }) => {
    const iconSize = getResponsiveIconSize();
    return (
      <div 
        className="border rounded-xl p-2 sm:p-3 hover-lift cursor-pointer transition-all duration-200"
        onClick={() => handleViewConnectionDetails(connection)}
        style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card,
          backdropFilter: isDark ? 'blur(10px)' : 'none'
        }}
      >
        <div className="flex items-center justify-between mb-2">
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
        
        <div className="grid grid-cols-2 gap-1 text-xs mb-2">
          <div style={{ color: colors.textSecondary }}>
            Type: <span style={{ color: colors.text }}>{connection.type}</span>
          </div>
          <div style={{ color: colors.textSecondary }}>
            Version: <span style={{ color: colors.text }}>{connection.version}</span>
          </div>
          <div style={{ color: colors.textSecondary }}>
            Endpoints: <span style={{ color: colors.text }}>{connection.endpoints}</span>
          </div>
          <div style={{ color: colors.textSecondary }}>
            Folders: <span style={{ color: colors.text }}>{connection.folders}</span>
          </div>
        </div>
        
        <div className="flex items-center justify-between text-xs">
          <span style={{ color: colors.textSecondary }}>
            Owner: <span style={{ color: colors.text }}>{connection.owner}</span>
          </span>
          <span style={{ color: colors.textSecondary }}>
            Updated: <span style={{ color: colors.text }}>{new Date(connection.lastUpdated).toLocaleDateString()}</span>
          </span>
        </div>
      </div>
    );
  };

  // API Endpoint Item
  const ApiEndpointItem = ({ api }) => {
    return (
      <div 
        className="p-3 hover:bg-opacity-50 cursor-pointer transition-colors border-b last:border-b-0"
        onClick={() => openModal('api', api)}
        style={{ borderColor: colors.border }}
      >
        <div className="flex items-start justify-between mb-2">
          <div className="flex items-center gap-2">
            <span className="px-1.5 py-0.5 text-xs font-mono rounded" style={{
              backgroundColor: api.method === 'GET' ? colors.success + '20' :
                            api.method === 'POST' ? colors.info + '20' :
                            api.method === 'PUT' ? colors.warning + '20' :
                            colors.error + '20',
              color: api.method === 'GET' ? colors.success :
                    api.method === 'POST' ? colors.info :
                    api.method === 'PUT' ? colors.warning :
                    colors.error
            }}>
              {api.method}
            </span>
            <span className="text-xs font-medium truncate" style={{ color: colors.text }}>
              {api.name}
            </span>
          </div>
          <span className="text-xs" style={{ color: colors.textSecondary }}>
            {api.timeAgo}
          </span>
        </div>
        
        <div className="text-xs mb-2 leading-relaxed" style={{ color: colors.textSecondary, lineHeight: '1.5' }}>
          {api.description || api.url}
        </div>
        
        <div className="flex items-center gap-3 text-xs">
          <span style={{ color: colors.textSecondary }}>
            Collection: <span style={{ color: colors.text }}>{api.collectionName}</span>
          </span>
          <span style={{ color: colors.textSecondary }}>
            Calls: <span style={{ color: colors.text }}>{api.calls.toLocaleString()}</span>
          </span>
        </div>
      </div>
    );
  };

  // API Pagination Component
  const ApiPagination = () => {
    const totalApiPages = Math.ceil(dashboardData.apis.length / apisPerPage);
    const iconSize = getResponsiveIconSize();
    
    if (totalApiPages <= 1) return null;
    
    return (
      <div className="flex items-center justify-between p-3 border-t" style={{ borderColor: colors.border }}>
        <div className="text-xs" style={{ color: colors.textSecondary }}>
          Showing {((apiPage - 1) * apisPerPage) + 1} - {Math.min(apiPage * apisPerPage, dashboardData.apis.length)} of {dashboardData.apis.length}
        </div>
        <div className="flex items-center gap-1">
          <button
            onClick={handlePrevApiPage}
            disabled={apiPage === 1}
            className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
            style={{ 
              backgroundColor: apiPage === 1 ? 'transparent' : colors.hover,
              color: colors.text,
              cursor: apiPage === 1 ? 'not-allowed' : 'pointer'
            }}
          >
            <ChevronLeft size={iconSize} />
          </button>
          
          <div className="flex items-center gap-1">
            {Array.from({ length: Math.min(3, totalApiPages) }, (_, i) => {
              let pageNum;
              if (totalApiPages <= 3) {
                pageNum = i + 1;
              } else if (apiPage === 1) {
                pageNum = i + 1;
              } else if (apiPage === totalApiPages) {
                pageNum = totalApiPages - 2 + i;
              } else {
                pageNum = apiPage - 1 + i;
              }
              
              if (pageNum > totalApiPages) return null;
              
              return (
                <button
                  key={pageNum}
                  onClick={() => setApiPage(pageNum)}
                  className="w-6 h-6 rounded text-xs font-medium transition-colors"
                  style={{ 
                    backgroundColor: apiPage === pageNum ? colors.selected : 'transparent',
                    color: apiPage === pageNum ? colors.primaryDark : colors.textSecondary
                  }}
                >
                  {pageNum}
                </button>
              );
            })}
            
            {totalApiPages > 3 && apiPage < totalApiPages - 1 && (
              <>
                <span className="text-xs" style={{ color: colors.textSecondary }}>...</span>
                <button
                  onClick={() => setApiPage(totalApiPages)}
                  className="w-6 h-6 rounded text-xs font-medium transition-colors"
                  style={{ 
                    backgroundColor: apiPage === totalApiPages ? colors.selected : 'transparent',
                    color: apiPage === totalApiPages ? colors.primaryDark : colors.textSecondary
                  }}
                >
                  {totalApiPages}
                </button>
              </>
            )}
          </div>
          
          <button
            onClick={handleNextApiPage}
            disabled={apiPage === totalApiPages}
            className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
            style={{ 
              backgroundColor: apiPage === totalApiPages ? 'transparent' : colors.hover,
              color: colors.text,
              cursor: apiPage === totalApiPages ? 'not-allowed' : 'pointer'
            }}
          >
            <ChevronRightIcon size={iconSize} />
          </button>
        </div>
      </div>
    );
  };

  // Code Generation Stats Card
  const CodeGenerationStatsCard = () => {
    const iconSize = getResponsiveIconSize();
    const codeData = dashboardData.codeGenerationSummary || {};
    const stats = dashboardData.stats || {};
    
    const totalImplementations = codeData.totalImplementations || stats.totalCodeImplementations || 0;
    const supportedLanguages = codeData.supportedLanguages || stats.supportedLanguages || 0;
    const languageDistribution = codeData.languageDistribution || {};
    const validationSuccessRate = codeData.validationSuccessRate || '98.5%';
    const avgGenerationTime = codeData.averageGenerationTime || '2.3s';

    return (
      <div className="border rounded-xl p-3 sm:p-4 hover-lift transition-all duration-200" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.card,
        backdropFilter: isDark ? 'blur(10px)' : 'none',
        boxShadow: '0 1px 3px rgba(0,0,0,0.05)'
      }}>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 sm:gap-3 mb-3 sm:mb-4">
          <div className="flex-1 min-w-0">
            <div className="flex flex-wrap items-center gap-1 sm:gap-2 mb-0.5 sm:mb-1">
              <h3 className="text-sm font-semibold truncate" style={{ color: colors.text }}>
                Code Generation Statistics
              </h3>
              <div className="flex items-center gap-1 px-1.5 sm:px-2 py-0.5 rounded-full text-xs" 
                style={{ 
                  backgroundColor: `${colors.success}20`,
                  color: colors.success
                }}>
                <Code size={10} />
                <span>Active</span>
              </div>
            </div>
            <div className="flex flex-wrap items-center gap-1 sm:gap-2">
              <p className="text-xs truncate" style={{ color: colors.textSecondary }}>
                {totalImplementations} total implementations
              </p>
              <div className="text-xs flex items-center gap-1" style={{ color: colors.textTertiary }}>
                <Globe size={10} />
                <span className="truncate">{supportedLanguages} languages</span>
              </div>
            </div>
          </div>
          <div 
            className="flex items-center gap-1 sm:gap-2 shrink-0 cursor-pointer hover-lift p-1.5 sm:p-2 rounded-lg mt-2 sm:mt-0"
            onClick={handleCodeBaseClick}
            style={{ backgroundColor: `${colors.primaryDark}15` }}
          >
            <div className="p-1 sm:p-2 rounded-lg" style={{ backgroundColor: `${colors.primaryDark}15` }}>
              <Code size={Math.max(iconSize + 4, 16)} style={{ color: colors.primaryDark }} />
            </div>
            <div className="text-right hidden sm:block">
              <div className="text-xs" style={{ color: colors.textSecondary }}>View All</div>
              {/* <div className="text-sm font-semibold" style={{ color: colors.text }}>
                Code Base
              </div> */}
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-2 sm:gap-3 mb-3 sm:mb-4">
          <div className="border rounded-lg p-2 sm:p-3" style={{ borderColor: colors.borderLight }}>
            <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Total Implementations</div>
            <div className="text-xl sm:text-2xl font-bold" style={{ color: colors.text }}>
              {totalImplementations}
            </div>
            <div className="flex items-center gap-1 mt-1">
              <TrendingUp size={10} style={{ color: colors.success }} />
              <span className="text-xs" style={{ color: colors.success }}>+12% this month</span>
            </div>
          </div>
          
          <div className="border rounded-lg p-2 sm:p-3" style={{ borderColor: colors.borderLight }}>
            <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Supported Languages</div>
            <div className="text-xl sm:text-2xl font-bold" style={{ color: colors.text }}>
              {supportedLanguages}
            </div>
            <div className="flex items-center gap-1 mt-1">
              <Globe size={10} style={{ color: colors.info }} />
              <span className="text-xs" style={{ color: colors.info }}>4 frameworks</span>
            </div>
          </div>
        </div>

        {Object.keys(languageDistribution).length > 0 && (
          <div className="space-y-2 mb-3 sm:mb-4">
            <div className="flex items-center justify-between">
              <span className="text-xs font-medium" style={{ color: colors.textSecondary }}>Language Distribution</span>
              <span className="text-xs" style={{ color: colors.textTertiary }}>implementations</span>
            </div>
            <div className="space-y-1.5">
              {Object.entries(languageDistribution).map(([language, count]) => {
                const percentage = totalImplementations ? ((count / totalImplementations) * 100).toFixed(0) : 0;
                return (
                  <div key={language} className="flex items-center gap-2">
                    <span className="text-xs w-16 sm:w-20 truncate" style={{ color: colors.text }}>{language}</span>
                    <div className="flex-1 h-1.5 rounded-full" style={{ backgroundColor: colors.hover }}>
                      <div 
                        className="h-full rounded-full" 
                        style={{ 
                          width: `${percentage}%`,
                          backgroundColor: language === 'Java' ? '#f89820' :
                                        language === 'JavaScript' ? '#f0db4f' :
                                        language === 'Python' ? '#3776ab' :
                                        language === 'C#' ? '#9b4993' : colors.primaryDark
                        }}
                      />
                    </div>
                    <span className="text-xs font-medium" style={{ color: colors.text }}>{count}</span>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        <div className="grid grid-cols-2 gap-2 sm:gap-3 pt-3 border-t" style={{ borderColor: colors.border }}>
          <div>
            <div className="text-xs" style={{ color: colors.textSecondary }}>Success Rate</div>
            <div className="text-sm font-medium flex items-center gap-1" style={{ color: colors.success }}>
              <CheckCircle size={12} />
              {validationSuccessRate}
            </div>
          </div>
          <div>
            <div className="text-xs" style={{ color: colors.textSecondary }}>Avg Generation</div>
            <div className="text-sm font-medium flex items-center gap-1" style={{ color: colors.text }}>
              <Clock size={12} />
              {avgGenerationTime}
            </div>
          </div>
        </div>

        <div className="mt-3 pt-2 flex items-center justify-between text-xs" style={{ borderTop: `1px solid ${colors.border}` }}>
          <span style={{ color: colors.textTertiary }}>
            Last generated: {new Date().toLocaleDateString()}
          </span>
          <button 
            onClick={handleCodeBaseClick}
            className="flex items-center gap-1 hover-lift px-2 py-1 rounded"
            style={{ color: colors.primaryDark }}
          >
            <span>View Code Base</span>
            <ChevronRight size={10} />
          </button>
        </div>
      </div>
    );
  };

  // Mobile Search Bar
  const MobileSearchBar = () => (
    <div className={`md:hidden p-3 border-b transition-all duration-300 ${isMobileSearchOpen ? 'block' : 'hidden'}`} 
      style={{ borderColor: colors.border, backgroundColor: colors.header }}>
      <div className="flex items-center gap-2">
        <Search size={16} style={{ color: colors.textSecondary }} />
        <input
          type="text"
          placeholder="Search connections, APIs..."
          className="flex-1 bg-transparent outline-none text-sm"
          style={{ color: colors.text }}
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
        />
        {loading.search && <RefreshCw size={14} className="animate-spin" style={{ color: colors.textSecondary }} />}
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
            backdropFilter: isDark ? 'blur(10px)' : 'none',
            zIndex: zIndex
          }}
        >
          <div className="sticky top-0 p-3 sm:p-4 border-b flex items-center justify-between backdrop-blur-sm" style={{ 
            borderColor: colors.border,
            backgroundColor: colors.modalBg,
            backdropFilter: isDark ? 'blur(10px)' : 'none'
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
            <div className="text-xs font-medium mb-1" style={{ color: colors.textSecondary }}>Method</div>
            <div className="text-sm" style={{ color: colors.text }}>{data?.method}</div>
          </div>
          <div>
            <div className="text-xs font-medium mb-1" style={{ color: colors.textSecondary }}>Status</div>
            <div className="flex items-center gap-1">
              <div className="w-2 h-2 rounded-full" style={{ backgroundColor: getStatusColor(data?.status) }} />
              <span className="text-sm capitalize" style={{ color: colors.text }}>{data?.status}</span>
            </div>
          </div>
          <div>
            <div className="text-xs font-medium mb-1" style={{ color: colors.textSecondary }}>Collection</div>
            <div className="text-sm" style={{ color: colors.text }}>{data?.collectionName || 'N/A'}</div>
          </div>
          <div>
            <div className="text-xs font-medium mb-1" style={{ color: colors.textSecondary }}>URL</div>
            <div className="text-sm truncate" style={{ color: colors.textSecondary }}>{data?.url}</div>
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
          <div>
            <div className="text-xs font-medium mb-1" style={{ color: colors.textSecondary }}>Generated</div>
            <div className="text-sm" style={{ color: colors.text }}>{data?.timeAgo || 'N/A'}</div>
          </div>
        </div>
        
        <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
          <div className="flex flex-row gap-2">
            <button 
              onClick={handleCollectionsClick}
              className="flex-1 px-3 py-2 rounded text-sm font-medium transition-colors hover-lift"
              style={{ 
                backgroundColor: colors.primaryDark,
                color: 'white'
              }}
            >
              Preview API
            </button>
            <button 
              onClick={closeModal}
              className="flex-1 px-3 py-2 rounded text-sm font-medium transition-colors hover-lift"
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
    const itemsPerModalPage = 6;
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
          <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Total APIs</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {data.totalItems}
              </div>
            </div>
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Methods</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {data.data.reduce((sum, api) => sum + (api.method ? 1 : 0), 0)}
              </div>
            </div>
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Avg Success Rate</div>
              <div className="text-lg font-bold" style={{ color: colors.success }}>
                {(
                  data.data.reduce((sum, api) => {
                    const rate = parseFloat(api.successRate) || 98.5;
                    return sum + rate;
                  }, 0) / Math.max(data.data.length, 1)
                ).toFixed(1)}%
              </div>
            </div>
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Avg Latency</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                42ms
              </div>
            </div>
          </div>

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
                      Method
                    </th>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      Collection
                    </th>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      Calls
                    </th>
                    <th className="text-left p-3 text-xs font-medium" style={{ color: colors.textSecondary }}>
                      Success Rate
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
                          {api.method}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm" style={{ color: colors.text }}>
                          {api.collectionName || 'N/A'}
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
                            {api.successRate || '98.5%'}
                          </div>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {totalPages > 1 && (
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
          )}

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
    const itemsPerModalPage = 6;
    const totalPages = Math.ceil(data.data.length / itemsPerModalPage);
    const startIndex = (localApiCallsPage - 1) * itemsPerModalPage;
    const endIndex = startIndex + itemsPerModalPage;
    const currentPageData = data.data.slice(startIndex, endIndex);

    const handleApiCallsPageChange = (newPage) => {
      setLocalApiCallsPage(newPage);
    };

    const handleViewApiDetailsFromCalls = (apiData) => {
      const fullApi = dashboardData.apis?.find(a => a.id === apiData.id);
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
                42ms
              </div>
            </div>
          </div>

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
                        <div className="text-sm font-medium" style={{ 
                          color: parseFloat(api.successRate) >= 99 ? colors.success : 
                                parseFloat(api.successRate) >= 95 ? colors.warning : colors.error 
                        }}>
                          {api.successRate || '98.5%'}
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

          {totalPages > 1 && (
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
          )}

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
              {itemName}s in Platform
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

  // Right Sidebar Component
  const RightSidebar = () => (
    <div className={`w-full md:w-80 border-l flex flex-col fixed md:relative inset-y-0 right-0 z-40 transform transition-transform duration-300 ease-in-out ${
      isRightSidebarVisible ? 'translate-x-0' : 'translate-x-full md:translate-x-0'
    }`} style={{ 
      backgroundColor: colors.sidebar,
      borderColor: colors.border,
      height: '100vh',
      maxHeight: '100vh',
      top: 0,
      backdropFilter: isDark ? 'blur(10px)' : 'none'
    }}>
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
        <div className="p-3 md:p-4">
          <h3 className="text-sm uppercase font-semibold mb-5 mt-1 hidden md:block" style={{ color: colors.text }}>
            Quick Actions
          </h3>
          <div className="space-y-6 sm:space-y-6 ">
            <button 
              onClick={handleApiGeneration}
              className="w-full px-3 py-2 rounded text-sm font-medium transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ 
                color: colors.text 
              }}
            >
              <FileCode size={14} />
              <span className="truncate">Generate New API</span>
            </button>
            <button 
              onClick={handleNavigateToAPICollection}
              className="w-full px-3 py-2 rounded text-sm font-medium transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ 
                color: colors.text 
              }}
            >
              <Database size={14} />
              <span className="truncate">API Collections</span>
            </button>
            <button 
              onClick={handleNavigateToDocumentation}
              className="w-full px-3 py-2 rounded text-sm font-medium transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ 
                color: colors.text 
              }}
            >
              <BookOpen size={14} />
              <span className="truncate">API Documentation</span>
            </button>
            <button 
              onClick={handleNavigateToCodeBase}
              className="w-full px-3 py-2 rounded text-sm font-medium transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ 
                color: colors.text 
              }}
            >
              <Code size={14} />
              <span className="truncate">API Code Base</span>
            </button>
            <button 
              onClick={handleNavigateToAPISecurity}
              className="w-full px-3 py-2 rounded text-sm font-medium transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ 
                color: colors.text 
              }}
            >
              <Shield size={14} />
              <span className="truncate">API Security</span>
            </button>
            <button 
              onClick={handleNavigateToUserManagement}
              className="w-full px-3 py-2 rounded text-sm font-medium transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ 
                color: colors.text 
              }}
            >
              <UserCog size={14} />
              <span className="truncate">User Management</span>
            </button>
          </div>
        </div>

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
  if (error) {
    return renderErrorState();
  }

  const hasData = dashboardData.stats.totalConnections > 0 || 
                  dashboardData.apis?.length > 0;

  const currentPageApis = getCurrentPageApis();

  return (
    <div className="flex flex-col h-screen min-h-screen relative overflow-hidden" style={{ 
      backgroundColor: colors.bg,
      color: colors.text,
      fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif',
      fontSize: '13px'
    }}>
      
      <LoadingOverlay />

      <div className="absolute inset-0 overflow-hidden">
        <div className={`absolute -top-40 -right-40 w-80 h-80 rounded-full bg-gradient-to-br ${colors.gradient} blur-3xl animate-pulse`}></div>
        <div className={`absolute -bottom-40 -left-40 w-80 h-80 rounded-full bg-gradient-to-br ${colors.gradient} blur-3xl animate-pulse delay-1000`}></div>
      </div>

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
        
        @keyframes pulse {
          0%, 100% {
            opacity: 0.3;
            transform: scale(1);
          }
          50% {
            opacity: 0.5;
            transform: scale(1.05);
          }
        }
        
        .animate-pulse {
          animation: pulse 4s cubic-bezier(0.4, 0, 0.6, 1) infinite;
        }
        
        .delay-1000 {
          animation-delay: 1s;
        }
        
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

        * {
          transition: background-color 0.3s ease, border-color 0.3s ease, color 0.3s ease;
        }
      `}</style>

      <MobileSearchBar />
      
      <ModalRenderer />

      <div className="flex-1 overflow-hidden flex z-20 relative">
        {isRightSidebarVisible && (
          <div 
            className="fixed inset-0 bg-black bg-opacity-50 z-30 md:hidden"
            onClick={() => setIsRightSidebarVisible(false)}
          />
        )}

        <RightSidebar />

        <div className="flex-1 overflow-auto p-2 sm:p-3 md:p-4 h-full relative z-10 mb-5">
          <div className="max-w-9xl mx-auto px-1 sm:px-2 md:pl-5 md:pr-5">
            <div className="hidden md:flex items-center justify-between mb-4 md:mb-6">
              <div>
                <h1 className="text-xl md:text-xl font-bold" style={{ color: colors.text }}>
                  Dashboard
                </h1>
                <p className="text-xs md:text-sm" style={{ color: colors.textSecondary }}>
                  Overview of your API platform
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
                  className="px-3 py-2 bg-gradient-to-r from-blue-500 via-violet-500 to-blue-500 rounded text-sm font-medium transition-all duration-300 hover-lift hover:shadow-lg flex items-center gap-2 cursor-pointer relative overflow-hidden group"
                  style={{ 
                    color: 'white'
                  }}
                >
                  <div className="absolute inset-0 bg-white/20 transform -translate-x-full group-hover:translate-x-full transition-transform duration-700"></div>
                  <FileCode size={14} className="relative z-10" />
                  <span className="truncate relative z-10">Generate New API</span>
                </button>
              </div>
            </div>

            <div className="md:hidden flex items-center justify-between mb-3">
              <h1 className="text-base font-bold" style={{ color: colors.text }}>Dashboard</h1>
              <div className="flex items-center gap-2">
                <button 
                  onClick={toggleMobileSearch}
                  className="p-1.5 rounded-lg transition-all duration-200"
                  style={{ backgroundColor: colors.hover }}
                >
                  <Search size={16} style={{ color: colors.text }} />
                </button>
                <button 
                  onClick={handleRefresh}
                  className="p-1.5 rounded-lg transition-all duration-200"
                  style={{ backgroundColor: colors.hover }}
                  disabled={loading.refresh}
                >
                  <RefreshCw size={16} className={loading.refresh ? 'animate-spin' : ''} style={{ color: colors.text }} />
                </button>
              </div>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-2 lg:grid-cols-4 gap-1.5 sm:gap-2 md:gap-4 mb-3 sm:mb-4 md:mb-6">
              <StatCard
                title="Total APIs"
                value={dashboardData.stats.totalApis}
                icon={Database}
                change={dashboardData.stats.totalApis > 0 ? +5 : 0}
                color={colors.success}
                onClick={handleViewAllConnections}
              />
              <StatCard
                title="Total API Documentations"
                value={dashboardData.stats.totalDocumentationEndpoints}
                icon={FileCode}
                change={dashboardData.stats.totalDocumentationEndpoints > 0 ? +12 : 0}
                color={colors.info}
                onClick={handleCodeBaseClick}
              />
              <StatCard
                title="Total API Requests"
                value={dashboardData.stats.totalCalls.toLocaleString()}
                icon={Activity}
                change={dashboardData.stats.totalCalls > 0 ? +8.5 : 0}
                color={colors.primaryDark}
                onClick={handleApiCallsClick}
              />
               <StatCard
                title="API Collections"
                value={dashboardData.stats.totalCollections}
                icon={FileCode}
                change={dashboardData.stats.totalCollections > 0 ? +12 : 0}
                color={colors.info}
                onClick={handleCollectionsClick}
              />
            </div>

            {hasData ? (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-3 sm:gap-4 md:gap-6">
                <div className="flex flex-col gap-3 sm:gap-4 md:gap-6">
                  {<CodeGenerationStatsCard />}
                  {dashboardData.connections?.length > 0 && (
                    <div className="border rounded-xl" style={{ 
                      borderColor: colors.border,
                      backgroundColor: colors.card,
                      backdropFilter: isDark ? 'blur(10px)' : 'none'
                    }}>
                      <div className="p-2 sm:p-3 md:p-4 border-b" style={{ borderColor: colors.border }}>
                        <div className="flex items-center justify-between">
                          <h3 className="text-xs sm:text-sm font-semibold truncate" style={{ color: colors.text }}>
                            API Collections
                          </h3>
                          <div className="flex items-center gap-1 sm:gap-2">
                            <span className="text-xs hidden sm:inline" style={{ color: colors.textSecondary }}>
                              {dashboardData.connections.length} collections
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
                          {dashboardData.connections.slice(0, 3).map(conn => (
                            <ConnectionCard key={conn.id} connection={conn} />
                          ))}
                        </div>
                      </div>
                    </div>
                  )}
                </div>

                <div className="flex flex-col gap-3 sm:gap-4 md:gap-6">
                  <div className="border rounded-xl flex flex-col h-full" style={{ 
                    borderColor: colors.border,
                    backgroundColor: colors.card,
                    backdropFilter: isDark ? 'blur(10px)' : 'none'
                  }}>
                    <div className="p-2 sm:p-3 md:p-4 border-b flex-shrink-0" style={{ borderColor: colors.border }}>
                      <div className="flex items-center justify-between">
                        <h3 className="text-xs sm:text-sm font-semibold" style={{ color: colors.text }}>
                          Recently Generated APIs
                        </h3>
                        <div className="flex items-center gap-1 sm:gap-2">
                          <select
                            value={apisPerPage}
                            onChange={(e) => {
                              setApisPerPage(Number(e.target.value));
                              setApiPage(1);
                            }}
                            className="text-xs px-1 sm:px-2 py-0.5 sm:py-1 rounded border bg-transparent hidden sm:block"
                            style={{ 
                              borderColor: colors.bg,
                              color: colors.text
                            }}
                          >
                            <option value={5}>5 per page</option>
                            <option value={8}>8 per page</option>
                            <option value={10}>10 per page</option>
                          </select>
                          <Zap size={12} style={{ color: colors.textSecondary }} />
                        </div>
                      </div>
                    </div>
                    <div className="flex-1 overflow-auto min-h-0">
                      {currentPageApis.length > 0 ? (
                        <div className="space-y-5" style={{ borderColor: colors.border }}>
                          {currentPageApis.map(api => (
                            <ApiEndpointItem key={api.id} api={api} />
                          ))}
                        </div>
                      ) : (
                        <div className="p-4 text-center">
                          <div className="text-sm" style={{ color: colors.textSecondary }}>No API endpoints</div>
                        </div>
                      )}
                    </div>
                    <ApiPagination />
                  </div>
                </div>

                 <br /><br />
              </div>
            ) : (
              renderEmptyState()
            )}
          </div>
        </div>
      </div>

      <div className="md:hidden fixed bottom-0 left-0 right-0 border-t" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.header,
        backdropFilter: isDark ? 'blur(10px)' : 'none',
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
      
      <div className="md:hidden h-16"></div>

       {/* Add this with the other modal rendering */}
        {showApiModal && (
          <ApiGenerationModal
            isOpen={showApiModal}
            onClose={() => setShowApiModal(false)}
            selectedObject={selectedForApiGeneration}
            colors={colors}
            theme={theme}
            onGenerateAPI={handleGenerateAPIFromModal} // You'll need to define this
            authToken={authToken}
          />
        )}
    </div>
  );
};

export default Dashboard;