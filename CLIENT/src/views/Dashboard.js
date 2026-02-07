import React, { useState, useEffect } from 'react';
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
  CalendarDays, DatabaseZap, Network as NetworkIcon, FileOutput,
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

const Dashboard = ({ theme, isDark, customTheme, toggleTheme, navigateTo }) => {
  const [loading, setLoading] = useState(false);
  const [timeRange, setTimeRange] = useState("24h");
  const [stats, setStats] = useState({
    totalConnections: 0,
    activeConnections: 0,
    totalApis: 0,
    activeApis: 0,
    totalCalls: 0,
    avgLatency: "0ms",
    successRate: "0%",
    uptime: "0%"
  });

  const [connections, setConnections] = useState([]);
  const [apis, setApis] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [apiPerformance, setApiPerformance] = useState([]);
  const [schemaData, setSchemaData] = useState({});
  const [recentActivity, setRecentActivity] = useState([]);
  const [systemHealth, setSystemHealth] = useState({
    cpu: 24,
    memory: 65,
    disk: 42,
    network: 78
  });
  const [codeGenerationStats, setCodeGenerationStats] = useState({
    java: 45,
    javascript: 32,
    python: 18,
    csharp: 5
  });

  // Modal states - now using a stack to handle nested modals
  const [modalStack, setModalStack] = useState([]);

  // Pagination for recent activities
  const [activityPage, setActivityPage] = useState(1);
  const [activitiesPerPage, setActivitiesPerPage] = useState(6);
  
  // Pagination for API modals
  const [apiStatsPage, setApiStatsPage] = useState(1);
  const [apiCallsPage, setApiCallsPage] = useState(1);
  const [itemsPerModalPage, setItemsPerModalPage] = useState(5);

  // Mobile state
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isRightSidebarVisible, setIsRightSidebarVisible] = useState(false);
  const [isMobileSearchOpen, setIsMobileSearchOpen] = useState(false);

  // Responsive icon size function
  const getResponsiveIconSize = () => {
    if (typeof window !== 'undefined') {
      if (window.innerWidth < 480) return 12;
      if (window.innerWidth < 768) return 14;
      return 14;
    }
    return 14;
  };

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
    connectionIdle: '#f59e0b'
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

  // Initialize dashboard data
  useEffect(() => {
    const fetchDashboardData = () => {
      // Sample connections
      const sampleConnections = [
        {
          id: 'conn-1',
          name: 'CBX_DMX',
          description: 'Development Database',
          host: 'db.unionsg.com',
          port: '1521',
          service: 'ORCL',
          username: 'HR',
          password: '********',
          status: 'connected',
          color: colors.connectionOnline,
          type: 'oracle',
          latency: '12ms',
          uptime: '99.9%',
          lastConnected: '2024-01-15T10:30:00Z',
          driver: 'Oracle JDBC',
          version: '19c',
          maxConnections: 50,
          currentConnections: 12,
          databaseSize: '2.4 GB',
          tablespaceUsed: '65%'
        }
      ];

      // Sample APIs
      const sampleApis = [
        {
          id: 'api-1',
          name: 'User Management API',
          description: 'Complete user authentication and management',
          version: 'v2.1',
          status: 'active',
          endpointCount: 12,
          lastUpdated: '2024-01-15T10:30:00Z',
          calls: 1250,
          latency: '45ms',
          successRate: '99.8%',
          baseUrl: 'https://api.example.com/v2.1/users',
          documentation: 'https://docs.example.com/api/v2.1',
          supportedMethods: ['GET', 'POST', 'PUT', 'DELETE'],
          security: 'JWT Authentication',
          rateLimit: '1000 requests/hour',
          errors: 5,
          avgResponseTime: '42ms',
          uptime: '99.9%',
          lastDeployed: '2024-01-14T08:00:00Z',
          owner: 'John Doe',
          category: 'Authentication'
        },
        {
          id: 'api-2',
          name: 'Payment Processing API',
          description: 'Secure payment processing',
          version: 'v1.5',
          status: 'active',
          endpointCount: 8,
          lastUpdated: '2024-01-14T14:20:00Z',
          calls: 892,
          latency: '32ms',
          successRate: '99.9%',
          baseUrl: 'https://api.example.com/v1.5/payments',
          documentation: 'https://docs.example.com/api/v1.5',
          supportedMethods: ['POST'],
          security: 'API Key + SSL',
          rateLimit: '500 requests/hour',
          errors: 2,
          avgResponseTime: '30ms',
          uptime: '100%',
          lastDeployed: '2024-01-13T10:00:00Z',
          owner: 'Jane Smith',
          category: 'Payments'
        },
        {
          id: 'api-3',
          name: 'Inventory Management API',
          description: 'Manage product inventory and stock levels',
          version: 'v1.2',
          status: 'active',
          endpointCount: 15,
          lastUpdated: '2024-01-13T09:15:00Z',
          calls: 2100,
          latency: '38ms',
          successRate: '99.5%',
          baseUrl: 'https://api.example.com/v1.2/inventory',
          documentation: 'https://docs.example.com/api/v1.2',
          supportedMethods: ['GET', 'POST', 'PUT', 'PATCH'],
          security: 'OAuth 2.0',
          rateLimit: '2000 requests/hour',
          errors: 12,
          avgResponseTime: '35ms',
          uptime: '99.7%',
          lastDeployed: '2024-01-12T14:30:00Z',
          owner: 'Bob Johnson',
          category: 'Inventory'
        },
        {
          id: 'api-4',
          name: 'Order Processing API',
          description: 'Handle customer orders and order tracking',
          version: 'v2.0',
          status: 'active',
          endpointCount: 10,
          lastUpdated: '2024-01-12T16:45:00Z',
          calls: 1850,
          latency: '50ms',
          successRate: '99.7%',
          baseUrl: 'https://api.example.com/v2.0/orders',
          documentation: 'https://docs.example.com/api/v2.0',
          supportedMethods: ['GET', 'POST', 'PUT'],
          security: 'API Key + JWT',
          rateLimit: '1500 requests/hour',
          errors: 8,
          avgResponseTime: '48ms',
          uptime: '99.8%',
          lastDeployed: '2024-01-11T09:00:00Z',
          owner: 'Alice Brown',
          category: 'Orders'
        },
        {
          id: 'api-5',
          name: 'Customer Support API',
          description: 'Customer support ticket management',
          version: 'v1.0',
          status: 'active',
          endpointCount: 6,
          lastUpdated: '2024-01-11T11:20:00Z',
          calls: 750,
          latency: '28ms',
          successRate: '99.9%',
          baseUrl: 'https://api.example.com/v1.0/support',
          documentation: 'https://docs.example.com/api/v1.0',
          supportedMethods: ['GET', 'POST', 'PUT', 'DELETE'],
          security: 'API Key',
          rateLimit: '800 requests/hour',
          errors: 3,
          avgResponseTime: '25ms',
          uptime: '100%',
          lastDeployed: '2024-01-10T13:45:00Z',
          owner: 'Charlie Wilson',
          category: 'Support'
        },
        {
          id: 'api-6',
          name: 'Analytics API',
          description: 'Data analytics and reporting endpoints',
          version: 'v3.1',
          status: 'active',
          endpointCount: 20,
          lastUpdated: '2024-01-10T15:30:00Z',
          calls: 3100,
          latency: '65ms',
          successRate: '99.3%',
          baseUrl: 'https://api.example.com/v3.1/analytics',
          documentation: 'https://docs.example.com/api/v3.1',
          supportedMethods: ['GET', 'POST'],
          security: 'JWT + IP Whitelist',
          rateLimit: '3000 requests/hour',
          errors: 22,
          avgResponseTime: '60ms',
          uptime: '99.5%',
          lastDeployed: '2024-01-09T10:15:00Z',
          owner: 'David Lee',
          category: 'Analytics'
        },
        {
          id: 'api-7',
          name: 'Notification API',
          description: 'Send email and push notifications',
          version: 'v1.3',
          status: 'active',
          endpointCount: 5,
          lastUpdated: '2024-01-09T14:00:00Z',
          calls: 4200,
          latency: '25ms',
          successRate: '99.6%',
          baseUrl: 'https://api.example.com/v1.3/notifications',
          documentation: 'https://docs.example.com/api/v1.3',
          supportedMethods: ['POST'],
          security: 'API Key',
          rateLimit: '5000 requests/hour',
          errors: 17,
          avgResponseTime: '22ms',
          uptime: '99.9%',
          lastDeployed: '2024-01-08T08:30:00Z',
          owner: 'Emma Davis',
          category: 'Notifications'
        },
        {
          id: 'api-8',
          name: 'Content Management API',
          description: 'Manage website content and media',
          version: 'v2.2',
          status: 'active',
          endpointCount: 14,
          lastUpdated: '2024-01-08T12:45:00Z',
          calls: 1650,
          latency: '40ms',
          successRate: '99.8%',
          baseUrl: 'https://api.example.com/v2.2/content',
          documentation: 'https://docs.example.com/api/v2.2',
          supportedMethods: ['GET', 'POST', 'PUT', 'DELETE'],
          security: 'JWT Authentication',
          rateLimit: '1200 requests/hour',
          errors: 9,
          avgResponseTime: '38ms',
          uptime: '99.9%',
          lastDeployed: '2024-01-07T11:00:00Z',
          owner: 'Frank Miller',
          category: 'Content'
        }
      ];

      // Sample schema data
      const sampleSchemaData = {
        totalObjects: 156,
        tables: 45,
        views: 12,
        procedures: 23,
        packages: 8,
        functions: 15,
        triggers: 9,
        indexes: 44,
        sequences: 5,
        materializedViews: 3,
        partitions: 21,
        databaseName: 'HR_DEV',
        databaseSize: '2.4 GB',
        version: '1.2.3',
        monthlyGrowth: 12,
        tableChange: 3,
        viewChange: 1,
        procedureChange: 2,
        functionChange: 4,
        packageChange: 0,
        triggerChange: 1,
        indexChange: 5,
        sequenceChange: 0,
        materializedViewChange: 0,
        totalObjectsChange: 16
      };

      // Generate sample recent activities
      const generateSampleActivities = () => {
        const activities = [];
        const actions = ['API Generated', 'Database Connected', 'Code Generated', 'Schema Updated', 
                        'User Login', 'Configuration Updated', 'Backup Created', 'Test Executed',
                        'Deployment Completed', 'Security Scan', 'Performance Test', 'Bug Fixed'];
        const users = ['Admin', 'System', 'Developer', 'DBA', 'Tester', 'DevOps', 'Security Analyst'];
        const icons = ['api', 'database', 'code', 'schema', 'user', 'settings', 'backup', 'test'];
        
        for (let i = 1; i <= 25; i++) {
          const action = actions[Math.floor(Math.random() * actions.length)];
          const user = users[Math.floor(Math.random() * users.length)];
          const icon = icons[Math.floor(Math.random() * icons.length)];
          const hoursAgo = Math.floor(Math.random() * 24 * 7);
          const apiName = action.includes('API') ? `API-${Math.floor(Math.random() * 100)}` : null;
          const databaseName = action.includes('Database') ? `DB-${Math.floor(Math.random() * 100)}` : null;
          
          activities.push({
            id: `act-${i}`,
            action: action,
            description: `${action} for ${user}'s task #${i}`,
            user: user,
            time: hoursAgo === 0 ? 'Just now' : 
                  hoursAgo < 24 ? `${hoursAgo} hour${hoursAgo > 1 ? 's' : ''} ago` :
                  `${Math.floor(hoursAgo / 24)} day${Math.floor(hoursAgo / 24) > 1 ? 's' : ''} ago`,
            timestamp: new Date(Date.now() - hoursAgo * 3600000).toISOString(),
            icon: icon,
            priority: Math.random() > 0.7 ? 'high' : Math.random() > 0.4 ? 'medium' : 'low',
            details: `Detailed information about ${action}. This activity involved ${user} working on task #${i}.`,
            affectedResource: apiName || databaseName || `RES-${i}`,
            actionType: action.toLowerCase().includes('api') ? 'api' : 
                       action.toLowerCase().includes('database') ? 'database' : 
                       action.toLowerCase().includes('code') ? 'code' : 'system'
          });
        }
        return activities;
      };

      const sampleStats = {
        totalConnections: sampleConnections.length,
        activeConnections: sampleConnections.filter(c => c.status === 'connected').length,
        totalApis: sampleApis.length,
        activeApis: sampleApis.filter(a => a.status === 'active').length,
        totalCalls: sampleApis.reduce((sum, api) => sum + api.calls, 0),
        avgLatency: "48ms",
        successRate: "99.8%",
        uptime: "99.9%"
      };

      setConnections(sampleConnections);
      setApis(sampleApis);
      setSchemaData(sampleSchemaData);
      setStats(sampleStats);
      setRecentActivity(generateSampleActivities());
    };

    fetchDashboardData();
  }, []);

  // Pagination logic
  const totalActivityPages = Math.ceil(recentActivity.length / activitiesPerPage);
  const paginatedActivities = recentActivity.slice(
    (activityPage - 1) * activitiesPerPage,
    activityPage * activitiesPerPage
  );

  const handlePrevPage = () => {
    if (activityPage > 1) {
      setActivityPage(activityPage - 1);
    }
  };

  const handleNextPage = () => {
    if (activityPage < totalActivityPages) {
      setActivityPage(activityPage + 1);
    }
  };

  const handleRefresh = () => {
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      console.log('Dashboard refreshed');
    }, 1000);
  };

  // Navigation handlers
  const handleNavigateToSchemaBrowser = () => {
    console.log('Navigating to Schema Browser');
    closeAllModals();
    if (navigateTo) {
      navigateTo('schema-browser');
    } else {
      window.location.href = '#/schema-browser';
      alert('Navigate to Schema Browser (simulated)');
    }
  };

  const handleNavigateToApiBuilder = () => {
    console.log('Navigating to API Builder');
    closeAllModals();
    if (navigateTo) {
      navigateTo('api-builder');
    } else {
      window.location.href = '#/api-builder';
      alert('Navigate to API Builder (simulated)');
    }
  };

  const handleNavigateToCodeBase = () => {
    console.log('Navigating to Code Base');
    closeAllModals();
    if (navigateTo) {
      navigateTo('code-base');
    } else {
      window.location.href = '#/code-base';
      alert('Navigate to Code Base (simulated)');
    }
  };

  const handleNavigateToDocumentation = () => {
    console.log('Navigating to Documentation');
    closeAllModals();
    if (navigateTo) {
      navigateTo('documentation');
    } else {
      window.open('https://docs.example.com', '_blank');
      alert('Opening documentation (simulated)');
    }
  };

  const handleNewConnection = () => {
    console.log('Opening New Connection Form');
    closeAllModals();
    if (navigateTo) {
      navigateTo('connections');
    } else {
      alert('Open New Connection Form (simulated)');
    }
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

  const handleApiGeneration = () => {
    console.log('Starting API Generation process');
    closeAllModals();
    if (navigateTo) {
      navigateTo('api-generator');
    } else {
      alert('Starting API Generation (simulated)');
    }
  };

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

  // Add new handlers for API card clicks
  const handleApiStatsClick = () => {
    console.log('Opening API Stats modal');
    openModal('apiStats', {
      title: 'Active APIs',
      data: apis.filter(api => api.status === 'active'),
      totalItems: apis.filter(api => api.status === 'active').length
    });
  };

  const handleApiCallsClick = () => {
    console.log('Opening API Calls modal');
    openModal('apiCalls', {
      title: 'API Calls Analytics',
      data: apis.map(api => ({
        id: api.id,
        name: api.name,
        calls: api.calls,
        latency: api.latency,
        successRate: api.successRate,
        errors: api.errors || 0,
        avgResponseTime: api.avgResponseTime || 'N/A',
        lastUpdated: api.lastUpdated,
        owner: api.owner || 'N/A'
      })),
      totalItems: apis.length
    });
  };

  // Add pagination handlers for modals
  const handleApiStatsPageChange = (newPage) => {
    setApiStatsPage(newPage);
  };

  const handleApiCallsPageChange = (newPage) => {
    setApiCallsPage(newPage);
  };

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
        return colors.warning;
      case 'error':
      case 'failed':
      case 'offline':
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

  // Stat Card Component
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

  // Connection Card
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

  // Activity Item
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
              {activity.time}
            </span>
          </div>
          <p className="text-xs mt-0.5 sm:mt-1 truncate" style={{ color: colors.textSecondary }}>
            {activity.description}
          </p>
          <div className="flex items-center justify-between mt-0.5 sm:mt-1 gap-1 sm:gap-2">
            <div className="text-xs truncate" style={{ color: colors.textTertiary }}>
              by {activity.user}
            </div>
            {activity.priority !== 'low' && (
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

  // Schema Stats Card
  const SchemaStatsCard = () => {
    const iconSize = getResponsiveIconSize();
    const schemaCategories = [
      { 
        name: 'Tables & Views', 
        items: [
          { 
            key: 'tables', 
            value: schemaData.tables, 
            icon: <Table size={Math.max(iconSize - 2, 10)} />,
            change: schemaData.tableChange || 0,
            description: 'Data tables'
          },
          { 
            key: 'views', 
            value: schemaData.views, 
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
            value: schemaData.procedures, 
            icon: <FileCode size={Math.max(iconSize - 2, 10)} />,
            change: schemaData.procedureChange || 0,
            description: 'Stored procedures'
          },
          { 
            key: 'functions', 
            value: schemaData.functions, 
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
                  backgroundColor: schemaData.totalObjectsChange >= 0 ? `${colors.success}20` : `${colors.error}20`,
                  color: schemaData.totalObjectsChange >= 0 ? colors.success : colors.error
                }}>
                <TrendingUp size={10} />
                <span>{Math.abs(schemaData.totalObjectsChange || 0)}</span>
              </div>
            </div>
            <div className="flex flex-wrap items-center gap-1 sm:gap-2">
              <p className="text-xs truncate" style={{ color: colors.textSecondary }}>
                {schemaData.totalObjects} objects
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
                      {((categoryTotal / schemaData.totalObjects) * 100).toFixed(1)}%
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

  // Activity Pagination Component
  const ActivityPagination = () => {
    const iconSize = getResponsiveIconSize();
    return (
      <div className="flex flex-col sm:flex-row items-center justify-between gap-2 sm:gap-3 p-3 sm:p-4 border-t" style={{ borderColor: colors.border }}>
        <div className="text-xs text-center sm:text-left" style={{ color: colors.textSecondary }}>
          Showing {((activityPage - 1) * activitiesPerPage) + 1} - {Math.min(activityPage * activitiesPerPage, recentActivity.length)} of {recentActivity.length}
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

  // Mobile Search Bar
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

  // Mobile Modal Component
  const MobileModal = ({ children, title, onClose, showBackButton = false, onBack }) => {
    const iconSize = getResponsiveIconSize();
    const modalCount = modalStack.length;
    const zIndex = 1000 + (modalCount * 10);
    
    return (
      <div 
        className="fixed inset-0 bg-black bg-opacity-50 backdrop-blur-sm flex items-center justify-center z-50 p-2 sm:p-4"
        style={{ zIndex: zIndex - 5 }}
        onClick={onClose}
      >
        <div 
          className="border rounded-xl w-[55rem] max-h-[90vh] overflow-auto animate-fade-in"
          onClick={(e) => e.stopPropagation()}
          style={{ 
            backgroundColor: colors.bg,
            borderColor: colors.modalBorder,
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

  // API Stats Modal Component
  const ApiStatsModal = ({ data }) => {
    const totalPages = Math.ceil(data.data.length / itemsPerModalPage);
    const startIndex = (apiStatsPage - 1) * itemsPerModalPage;
    const endIndex = startIndex + itemsPerModalPage;
    const currentPageData = data.data.slice(startIndex, endIndex);

    const handleApiClick = (apiId) => {
      const fullApi = apis.find(a => a.id === apiId);
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
          <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Total APIs</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {data.totalItems}
              </div>
            </div>
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Total Endpoints</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {data.data.reduce((sum, api) => sum + api.endpointCount, 0)}
              </div>
            </div>
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Avg Success Rate</div>
              <div className="text-lg font-bold" style={{ color: colors.success }}>
                {(
                  data.data.reduce((sum, api) => {
                    const rate = parseFloat(api.successRate);
                    return sum + (isNaN(rate) ? 0 : rate);
                  }, 0) / data.data.length
                ).toFixed(1)}%
              </div>
            </div>
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border }}>
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
                        backgroundColor: index % 2 === 0 ? colors.tableRow : colors.tableRowHover
                      }}
                      onClick={() => handleApiClick(api.id)}
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
                          {api.calls.toLocaleString()}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="flex items-center gap-1">
                          <div className="text-sm font-medium" style={{ 
                            color: parseFloat(api.successRate) >= 99 ? colors.success : 
                                  parseFloat(api.successRate) >= 95 ? colors.warning : colors.error 
                          }}>
                            {api.successRate}
                          </div>
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm" style={{ color: colors.text }}>
                          {api.latency}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm" style={{ color: colors.textSecondary }}>
                          {new Date(api.lastUpdated).toLocaleDateString()}
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
                onClick={() => handleApiStatsPageChange(apiStatsPage - 1)}
                disabled={apiStatsPage === 1}
                className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
                style={{ 
                  backgroundColor: apiStatsPage === 1 ? 'transparent' : colors.hover,
                  color: colors.text,
                  cursor: apiStatsPage === 1 ? 'not-allowed' : 'pointer'
                }}
              >
                <ChevronLeft size={14} />
              </button>
              
              <div className="flex items-center gap-1">
                {Array.from({ length: Math.min(3, totalPages) }, (_, i) => {
                  let pageNum;
                  if (totalPages <= 3) {
                    pageNum = i + 1;
                  } else if (apiStatsPage === 1) {
                    pageNum = i + 1;
                  } else if (apiStatsPage === totalPages) {
                    pageNum = totalPages - 2 + i;
                  } else {
                    pageNum = apiStatsPage - 1 + i;
                  }
                  
                  if (pageNum > totalPages) return null;
                  
                  return (
                    <button
                      key={pageNum}
                      onClick={() => handleApiStatsPageChange(pageNum)}
                      className="w-6 h-6 rounded text-xs font-medium transition-colors"
                      style={{ 
                        backgroundColor: apiStatsPage === pageNum ? colors.selected : 'transparent',
                        color: apiStatsPage === pageNum ? colors.primaryDark : colors.textSecondary
                      }}
                    >
                      {pageNum}
                    </button>
                  );
                })}
                
                {totalPages > 3 && apiStatsPage < totalPages - 1 && (
                  <>
                    <span className="text-xs" style={{ color: colors.textSecondary }}>...</span>
                    <button
                      onClick={() => handleApiStatsPageChange(totalPages)}
                      className="w-6 h-6 rounded text-xs font-medium transition-colors"
                      style={{ 
                        backgroundColor: apiStatsPage === totalPages ? colors.selected : 'transparent',
                        color: apiStatsPage === totalPages ? colors.primaryDark : colors.textSecondary
                      }}
                    >
                      {totalPages}
                    </button>
                  </>
                )}
              </div>
              
              <button
                onClick={() => handleApiStatsPageChange(apiStatsPage + 1)}
                disabled={apiStatsPage === totalPages}
                className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
                style={{ 
                  backgroundColor: apiStatsPage === totalPages ? 'transparent' : colors.hover,
                  color: colors.text,
                  cursor: apiStatsPage === totalPages ? 'not-allowed' : 'pointer'
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
                  closeAllModals();
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
    const totalPages = Math.ceil(data.data.length / itemsPerModalPage);
    const startIndex = (apiCallsPage - 1) * itemsPerModalPage;
    const endIndex = startIndex + itemsPerModalPage;
    const currentPageData = data.data.slice(startIndex, endIndex);

    const handleApiClick = (apiId) => {
      const fullApi = apis.find(a => a.id === apiId);
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
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Total Calls</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {data.data.reduce((sum, api) => sum + api.calls, 0).toLocaleString()}
              </div>
            </div>
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Total Errors</div>
              <div className="text-lg font-bold" style={{ color: colors.error }}>
                {data.data.reduce((sum, api) => sum + api.errors, 0).toLocaleString()}
              </div>
            </div>
            <div className="text-center p-3 rounded border" style={{ borderColor: colors.border }}>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Avg Response Time</div>
              <div className="text-lg font-bold" style={{ color: colors.text }}>
                {(
                  data.data.reduce((sum, api) => {
                    const time = parseInt(api.avgResponseTime) || 0;
                    return sum + time;
                  }, 0) / data.data.length
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
                        backgroundColor: index % 2 === 0 ? colors.tableRow : colors.tableRowHover
                      }}
                      onClick={() => handleApiClick(api.id)}
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
                          {api.calls.toLocaleString()}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="flex items-center gap-1">
                          <div className="text-sm font-medium" style={{ 
                            color: api.errors === 0 ? colors.success : 
                                  api.errors < 10 ? colors.warning : colors.error 
                          }}>
                            {api.errors}
                          </div>
                          {api.errors > 0 && (
                            <AlertCircle size={10} style={{ color: colors.warning }} />
                          )}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm" style={{ color: colors.text }}>
                          {api.avgResponseTime}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm font-medium" style={{ 
                          color: parseFloat(api.successRate) >= 99 ? colors.success : 
                                parseFloat(api.successRate) >= 95 ? colors.warning : colors.error 
                        }}>
                          {api.successRate}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm" style={{ color: colors.textSecondary }}>
                          {api.owner}
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
                onClick={() => handleApiCallsPageChange(apiCallsPage - 1)}
                disabled={apiCallsPage === 1}
                className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
                style={{ 
                  backgroundColor: apiCallsPage === 1 ? 'transparent' : colors.hover,
                  color: colors.text,
                  cursor: apiCallsPage === 1 ? 'not-allowed' : 'pointer'
                }}
              >
                <ChevronLeft size={14} />
              </button>
              
              <div className="flex items-center gap-1">
                {Array.from({ length: Math.min(3, totalPages) }, (_, i) => {
                  let pageNum;
                  if (totalPages <= 3) {
                    pageNum = i + 1;
                  } else if (apiCallsPage === 1) {
                    pageNum = i + 1;
                  } else if (apiCallsPage === totalPages) {
                    pageNum = totalPages - 2 + i;
                  } else {
                    pageNum = apiCallsPage - 1 + i;
                  }
                  
                  if (pageNum > totalPages) return null;
                  
                  return (
                    <button
                      key={pageNum}
                      onClick={() => handleApiCallsPageChange(pageNum)}
                      className="w-6 h-6 rounded text-xs font-medium transition-colors"
                      style={{ 
                        backgroundColor: apiCallsPage === pageNum ? colors.selected : 'transparent',
                        color: apiCallsPage === pageNum ? colors.primaryDark : colors.textSecondary
                      }}
                    >
                      {pageNum}
                    </button>
                  );
                })}
                
                {totalPages > 3 && apiCallsPage < totalPages - 1 && (
                  <>
                    <span className="text-xs" style={{ color: colors.textSecondary }}>...</span>
                    <button
                      onClick={() => handleApiCallsPageChange(totalPages)}
                      className="w-6 h-6 rounded text-xs font-medium transition-colors"
                      style={{ 
                        backgroundColor: apiCallsPage === totalPages ? colors.selected : 'transparent',
                        color: apiCallsPage === totalPages ? colors.primaryDark : colors.textSecondary
                      }}
                    >
                      {totalPages}
                    </button>
                  </>
                )}
              </div>
              
              <button
                onClick={() => handleApiCallsPageChange(apiCallsPage + 1)}
                disabled={apiCallsPage === totalPages}
                className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
                style={{ 
                  backgroundColor: apiCallsPage === totalPages ? 'transparent' : colors.hover,
                  color: colors.text,
                  cursor: apiCallsPage === totalPages ? 'not-allowed' : 'pointer'
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
            <div className="text-sm truncate" style={{ color: colors.text }}>{data?.time}</div>
          </div>
          <div>
            <div className="text-xs font-medium mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>Priority</div>
            <div className="text-sm capitalize truncate" style={{ color: getPriorityColor(data?.priority) }}>
              {data?.priority}
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

  const ConnectionModal = ({ data }) => (
    <MobileModal 
      title="Connection Details" 
      onClose={closeModal}
      showBackButton={modalStack.length > 1}
      onBack={closeModal}
    >
      <div className="space-y-3 sm:space-y-4">
        <div className="flex items-center gap-2 sm:gap-3">
          <div className="p-2 sm:p-3 rounded" style={{ backgroundColor: colors.hover }}>
            <Database size={20} style={{ color: colors.primary }} />
          </div>
          <div className="min-w-0">
            <h4 className="text-sm sm:text-lg font-semibold truncate" style={{ color: colors.text }}>
              {data?.name}
            </h4>
            <p className="text-xs sm:text-sm truncate" style={{ color: colors.textSecondary }}>
              {data?.description}
            </p>
          </div>
        </div>
        
        <div className="grid grid-cols-2 gap-2 sm:gap-4">
          <div>
            <div className="text-xs font-medium mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>Host</div>
            <div className="text-sm font-mono truncate" style={{ color: colors.text }}>{data?.host}</div>
          </div>
          <div>
            <div className="text-xs font-medium mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>Port</div>
            <div className="text-sm truncate" style={{ color: colors.text }}>{data?.port}</div>
          </div>
          <div>
            <div className="text-xs font-medium mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>Service</div>
            <div className="text-sm truncate" style={{ color: colors.text }}>{data?.service}</div>
          </div>
          <div>
            <div className="text-xs font-medium mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>Username</div>
            <div className="text-sm truncate" style={{ color: colors.text }}>{data?.username}</div>
          </div>
          <div>
            <div className="text-xs font-medium mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>Status</div>
            <div className="flex items-center gap-1 sm:gap-2">
              <div className="w-2 h-2 rounded-full" style={{ backgroundColor: getStatusColor(data?.status) }} />
              <span className="text-sm capitalize truncate" style={{ color: colors.text }}>{data?.status}</span>
            </div>
          </div>
          <div>
            <div className="text-xs font-medium mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>Type</div>
            <div className="text-sm capitalize truncate" style={{ color: colors.text }}>{data?.type}</div>
          </div>
        </div>
        
        <div className="pt-3 sm:pt-4 border-t" style={{ borderColor: colors.border }}>
          <div className="text-xs font-medium mb-1" style={{ color: colors.textSecondary }}>Performance</div>
          <div className="grid grid-cols-3 gap-2 sm:gap-4">
            <div className="text-center p-2 sm:p-3 rounded border" style={{ borderColor: colors.border }}>
              <div className="text-xs mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>Latency</div>
              <div className="text-sm sm:text-lg font-semibold" style={{ color: colors.text }}>{data?.latency}</div>
            </div>
            <div className="text-center p-2 sm:p-3 rounded border" style={{ borderColor: colors.border }}>
              <div className="text-xs mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>Uptime</div>
              <div className="text-sm sm:text-lg font-semibold" style={{ color: colors.text }}>{data?.uptime}</div>
            </div>
            <div className="text-center p-2 sm:p-3 rounded border" style={{ borderColor: colors.border }}>
              <div className="text-xs mb-0.5 sm:mb-1" style={{ color: colors.textSecondary }}>Connections</div>
              <div className="text-sm sm:text-lg font-semibold" style={{ color: colors.text }}>
                {data?.currentConnections}/{data?.maxConnections}
              </div>
            </div>
          </div>
        </div>
        
        <div className="pt-3 sm:pt-4 border-t" style={{ borderColor: colors.border }}>
          <div className="flex flex-col gap-2">
            <button 
              onClick={() => {
                console.log('Test connection:', data?.name);
                alert('Testing connection...');
              }}
              className="px-3 py-2 rounded text-sm font-medium transition-colors"
              style={{ 
                backgroundColor: colors.info,
                color: 'white'
              }}
            >
              Test Connection
            </button>
            <button 
              onClick={() => {
                console.log('Edit connection:', data?.name);
                closeAllModals();
                if (navigateTo) navigateTo('connections');
              }}
              className="px-3 py-2 rounded text-sm font-medium transition-colors"
              style={{ 
                backgroundColor: colors.warning,
                color: 'white'
              }}
            >
              Edit Connection
            </button>
            <button 
              onClick={() => {
                console.log('Browse schema for:', data?.name);
                closeAllModals();
                handleNavigateToSchemaBrowser();
              }}
              className="px-3 py-2 rounded text-sm font-medium transition-colors"
              style={{ 
                backgroundColor: colors.primaryDark,
                color: 'white'
              }}
            >
              Browse Schema
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
            <div className="text-sm font-medium" style={{ color: colors.text }}>{data?.calls?.toLocaleString()}</div>
          </div>
          <div>
            <div className="text-xs font-medium mb-1" style={{ color: colors.textSecondary }}>Success Rate</div>
            <div className="text-sm font-medium" style={{ 
              color: parseFloat(data?.successRate) >= 99 ? colors.success : 
                    parseFloat(data?.successRate) >= 95 ? colors.warning : colors.error 
            }}>
              {data?.successRate}
            </div>
          </div>
        </div>
        
        <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
          <div className="flex flex-col gap-2">
            <button 
              onClick={() => {
                closeAllModals();
                handleNavigateToApiBuilder();
              }}
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
      borderColor: colors.border
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
          <h3 className="text-sm font-semibold mb-4 mt-1 hidden md:block" style={{ color: colors.text }}>
            Quick Actions
          </h3>
          <div className="space-y-6 sm:space-y-6 ">
            <button 
              onClick={handleNewConnection}
              className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ backgroundColor: colors.hover, color: colors.text }}
            >
              <Database size={14} />
              <span className="truncate">New Database Connection</span>
            </button>
            <button 
              onClick={handleApiGeneration}
              className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ backgroundColor: colors.hover, color: colors.text }}
            >
              <FileCode size={14} />
              <span className="truncate">Generate New API</span>
            </button>
            <button 
              onClick={handleNavigateToCodeBase}
              className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ backgroundColor: colors.hover, color: colors.text }}
            >
              <Code size={14} />
              <span className="truncate">View Code Base</span>
            </button>
            <button 
              onClick={handleNavigateToDocumentation}
              className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift cursor-pointer"
              style={{ backgroundColor: colors.hover, color: colors.text }}
            >
              <BookOpen size={14} />
              <span className="truncate">View Documentation</span>
            </button>
          </div>
        </div>

        {/* Recent Deployments */}
        <div className="border-t p-3 md:p-4 hidden md:block" style={{ borderColor: colors.border }}>
          <div className="flex items-center justify-between mb-6 mt-6">
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>
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
                onClick={() => handleNavigateToApiBuilder()}
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

  return (
    <div className="flex flex-col h-full" style={{ 
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
        
        /* Custom scrollbar */
        ::-webkit-scrollbar {
          width: 6px;
          height: 6px;
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
        <div className="flex-1 overflow-auto p-2 sm:p-3 md:p-4">
          <div className="max-w-9xl mx-auto px-1 sm:px-2 md:pl-8 md:pr-8">
            {/* Desktop Header */}
            <div className="hidden md:flex items-center justify-between mb-4 md:mb-6">
              <div>
                <h1 className="text-xl md:text-2xl font-bold" style={{ color: colors.text }}>
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
                  onClick={handleRefresh}
                  className="p-2 rounded-lg hover-lift transition-all duration-200"
                  style={{ 
                    backgroundColor: colors.hover,
                    color: colors.text
                  }}
                >
                  <RefreshCw size={18} className={loading ? 'animate-spin' : ''} />
                </button>
              </div>
            </div>

            {/* Key Metrics */}
            <div className="grid grid-cols-2 md:grid-cols-2 lg:grid-cols-4 gap-1.5 sm:gap-2 md:gap-4 mb-3 sm:mb-4 md:mb-6">
              <StatCard
                title="Connections"
                value={stats.totalConnections}
                icon={Database}
                change={+5}
                color={colors.success}
                onClick={() => console.log('View all connections')}
              />
              <StatCard
                title="Active APIs"
                value={stats.activeApis}
                icon={FileCode}
                change={+12}
                color={colors.info}
                onClick={handleApiStatsClick}
              />
              <StatCard
                title="API Calls"
                value={stats.totalCalls.toLocaleString()}
                icon={Activity}
                change={+8.5}
                color={colors.primaryDark}
                onClick={handleApiCallsClick}
              />
              <StatCard
                title="Success Rate"
                value={stats.successRate}
                icon={CheckCircle}
                change={+0.2}
                color={colors.success}
                onClick={() => console.log('View performance metrics')}
              />
            </div>

            {/* Main Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-3 sm:gap-4 md:gap-6">
              {/* Left Column */}
              <div className="flex flex-col gap-3 sm:gap-4 md:gap-6">
                {/* Active Connections */}
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
                          {connections.length} connections
                        </span>
                        <button 
                          onClick={handleNewConnection}
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
                      {connections.map(conn => (
                        <ConnectionCard key={conn.id} connection={conn} />
                      ))}
                    </div>
                  </div>
                </div>
                <SchemaStatsCard />
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
                    {paginatedActivities.map(activity => (
                      <ActivityItem key={activity.id} activity={activity} />
                    ))}
                  </div>
                  <ActivityPagination />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Mobile Bottom Navigation */}
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