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

const Dashboard = ({ theme, isDark, customTheme, toggleTheme }) => {
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

  // Pagination for recent activities
  const [activityPage, setActivityPage] = useState(1);
  const [activitiesPerPage, setActivitiesPerPage] = useState(6);

  // Mobile state
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isRightSidebarVisible, setIsRightSidebarVisible] = useState(false);

  // Color scheme matching your other components
  const colors = isDark ? {
    // Using your shade as base
    bg: 'rgb(1 14 35)',
    white: '#FFFFFF',
    sidebar: 'rgb(41 53 72 / 39%)',
    main: 'rgb(1 14 35)',
    header: 'rgb(20 26 38)',
    card: 'rgb(41 53 72 / 39%)',
    
    // Text - coordinating grays
    text: '#F1F5F9',
    textSecondary: 'rgb(148 163 184)',
    textTertiary: 'rgb(100 116 139)',
    
    // Borders - variations of your shade
    border: 'rgb(51 65 85)',
    borderLight: 'rgb(45 55 72)',
    borderDark: 'rgb(71 85 105)',
    
    // Interactive - layered transparency
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
    
    // UI Components
    tabActive: 'rgb(96 165 250)',
    tabInactive: 'rgb(148 163 184)',
    sidebarActive: 'rgb(96 165 250)',
    sidebarhover: 'rgb(45 46 72 / 33%)',
    inputBg: 'rgb(41 53 72 / 39%)',
    inputBorder: 'rgb(51 65 85)',
    tableHeader: 'rgb(41 53 72 / 39%)',
    tableRow: 'rgb(41 53 72 / 39%)',
    tableRowhover: 'rgb(45 46 72 / 33%)',
    dropdownBg: 'rgb(41 53 72 / 39%)',
    dropdownBorder: 'rgb(51 65 85)',
    modalBg: 'rgb(41 53 72 / 39%)',
    modalBorder: 'rgb(51 65 85)',
    codeBg: 'rgb(41 53 72 / 39%)',
    
    // Connection status
    connectionOnline: 'rgb(52 211 153)',
    connectionOffline: 'rgb(248 113 113)',
    connectionIdle: 'rgb(251 191 36)',
    
    // Accent colors
    accentPurple: 'rgb(167 139 250)',
    accentPink: 'rgb(244 114 182)',
    accentCyan: 'rgb(34 211 238)'
  } : {
    // KEEP THE ORIGINAL LIGHT MODE
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

  // Update useEffect to use props if needed
  useEffect(() => {
    // You can use the theme prop here if needed
    console.log('Current theme:', theme);
  }, [theme]);

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
          status: 'connected',
          color: colors.connectionOnline,
          type: 'oracle',
          latency: '12ms',
          uptime: '99.9%'
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
          successRate: '99.8%'
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
          successRate: '99.9%'
        },
        {
          id: 'api-3',
          name: 'Inventory API',
          description: 'Product inventory management',
          version: 'v1.2',
          status: 'testing',
          endpointCount: 6,
          lastUpdated: '2024-01-13T16:45:00Z',
          calls: 156,
          latency: '67ms',
          successRate: '98.5%'
        }
      ];

      // Sample notifications
      const sampleNotifications = [
        {
          id: 'notif-1',
          title: 'New API Implementation',
          message: 'Complete Java Spring Boot implementation added',
          time: '10 minutes ago',
          read: false,
          type: 'success'
        },
        {
          id: 'notif-2',
          title: 'Database Connection Issue',
          message: 'MySQL connection showing high latency (15ms)',
          time: '15 minutes ago',
          read: false,
          type: 'warning'
        },
        {
          id: 'notif-3',
          title: 'Code Generation Complete',
          message: 'Python FastAPI code generated successfully',
          time: '2 hours ago',
          read: true,
          type: 'info'
        }
      ];

      // Sample API performance
      const sampleApiPerformance = [
        { name: 'Get Customer Orders', latency: 45, calls: 1250, successRate: 99.8 },
        { name: 'Create User Session', latency: 32, calls: 892, successRate: 99.9 },
        { name: 'Update Inventory', latency: 67, calls: 156, successRate: 98.5 },
        { name: 'Generate Report', latency: 125, calls: 342, successRate: 99.2 }
      ];

      // Sample schema data with more detailed information
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
        partitions: 21
      };

      // Generate more sample recent activities for pagination
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
          const hoursAgo = Math.floor(Math.random() * 24 * 7); // Up to 1 week ago
          
          activities.push({
            id: `act-${i}`,
            action: action,
            description: `${action} for ${user}'s task #${i}`,
            user: user,
            time: hoursAgo === 0 ? 'Just now' : 
                  hoursAgo < 24 ? `${hoursAgo} hour${hoursAgo > 1 ? 's' : ''} ago` :
                  `${Math.floor(hoursAgo / 24)} day${Math.floor(hoursAgo / 24) > 1 ? 's' : ''} ago`,
            icon: icon,
            priority: Math.random() > 0.7 ? 'high' : Math.random() > 0.4 ? 'medium' : 'low'
          });
        }
        return activities;
      };

      // Calculate stats
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
      setNotifications(sampleNotifications);
      setApiPerformance(sampleApiPerformance);
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
      // Refresh logic here
    }, 1000);
  };

  const getIconForActivity = (icon) => {
    const iconProps = { size: 14, style: { color: colors.textSecondary } };
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
        return colors.success;
      case 'warning':
        return colors.warning;
      case 'error':
      case 'failed':
        return colors.error;
      default:
        return colors.textSecondary;
    }
  };

  const getDatabaseIcon = (type) => {
    const iconProps = { size: 14, style: { color: colors.textSecondary } };
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
  const StatCard = ({ title, value, icon: Icon, change, color }) => (
    <div className="border rounded-xl p-3 sm:p-4 hover-lift" style={{ 
      borderColor: colors.border,
      backgroundColor: colors.card
    }}>
      <div className="flex items-center justify-between mb-2">
        <div className="text-xs sm:text-sm font-medium" style={{ color: colors.textSecondary }}>
          {title}
        </div>
        <div className="p-1.5 sm:p-2 rounded-lg" style={{ backgroundColor: `${color}20` }}>
          <Icon size={14} style={{ color }} />
        </div>
      </div>
      <div className="flex items-end justify-between">
        <div className="text-lg sm:text-xl md:text-2xl font-bold" style={{ color: colors.text }}>
          {value}
        </div>
        {change && (
          <div className={`text-xs px-1.5 sm:px-2 py-0.5 sm:py-1 rounded-full ${change > 0 ? 'text-green-600 bg-green-100' : 'text-red-600 bg-red-100'}`}>
            {change > 0 ? '+' : ''}{change}%
          </div>
        )}
      </div>
    </div>
  );

  // Connection Status Card
  const ConnectionCard = ({ connection }) => (
    <div className="border rounded-xl p-3 hover-lift" style={{ 
      borderColor: colors.border,
      backgroundColor: colors.card
    }}>
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center gap-2">
          {getDatabaseIcon(connection.type)}
          <span className="text-sm font-medium truncate" style={{ color: colors.text }}>
            {connection.name}
          </span>
        </div>
        <div className="flex items-center gap-1 shrink-0">
          <div className="w-2 h-2 rounded-full" style={{ backgroundColor: getStatusColor(connection.status) }} />
          <span className="text-xs" style={{ color: colors.textSecondary }}>
            {connection.status}
          </span>
        </div>
      </div>
      <div className="text-xs mb-2 truncate" style={{ color: colors.textSecondary }}>
        {connection.host}:{connection.port}/{connection.service}
      </div>
      <div className="flex items-center justify-between text-xs">
        <span style={{ color: colors.textSecondary }}>
          Latency: <span style={{ color: colors.text }}>{connection.latency}</span>
        </span>
        <span style={{ color: colors.textSecondary }}>
          Uptime: <span style={{ color: colors.text }}>{connection.uptime}</span>
        </span>
      </div>
    </div>
  );

  // Activity Item
  const ActivityItem = ({ activity }) => (
    <div className="flex items-start cursor-pointer gap-3 p-3 border-b last:border-b-0 hover:bg-opacity-50 transition-colors hover-lift"
      style={{ borderColor: colors.border }}>
      <div className="relative shrink-0">
        <div className="p-1.5 rounded" style={{ backgroundColor: colors.hover }}>
          {getIconForActivity(activity.icon)}
        </div>
        {activity.priority === 'high' && (
          <div className="absolute -top-1 -right-1 w-2 h-2 rounded-full" style={{ backgroundColor: getPriorityColor(activity.priority) }} />
        )}
      </div>
      <div className="flex-1 min-w-0 overflow-hidden">
        <div className="flex items-center justify-between gap-2">
          <span className="text-sm font-medium truncate" style={{ color: colors.text }}>
            {activity.action}
          </span>
          <span className="text-xs shrink-0" style={{ color: colors.textSecondary }}>
            {activity.time}
          </span>
        </div>
        <p className="text-xs mt-1 truncate" style={{ color: colors.textSecondary }}>
          {activity.description}
        </p>
        <div className="flex items-center justify-between mt-1 gap-2">
          <div className="text-xs truncate" style={{ color: colors.textTertiary }}>
            by {activity.user}
          </div>
          {activity.priority !== 'low' && (
            <div className={`text-xs px-2 py-0.5 rounded-full capitalize shrink-0 ${
              activity.priority === 'high' ? 'bg-red-500/10 text-red-500' : 'bg-yellow-500/10 text-yellow-500'
            }`}>
              {activity.priority}
            </div>
          )}
        </div>
      </div>
    </div>
  );

  // Enhanced Schema Stats Card
  const SchemaStatsCard = () => {
    const schemaCategories = [
      { 
        name: 'Tables & Views', 
        items: [
          { 
            key: 'tables', 
            value: schemaData.tables, 
            icon: <Table size={12} />,
            change: schemaData.tableChange || 0,
            description: 'Data storage structures'
          },
          { 
            key: 'views', 
            value: schemaData.views, 
            icon: <Eye size={12} />,
            change: schemaData.viewChange || 0,
            description: 'Virtual tables'
          },
          { 
            key: 'materializedViews', 
            value: schemaData.materializedViews || 0, 
            icon: <Database size={12} />,
            change: schemaData.materializedViewChange || 0,
            description: 'Pre-computed views'
          }
        ],
        color: colors.info,
        icon: <Table size={14} />
      },
      { 
        name: 'Program Units', 
        items: [
          { 
            key: 'procedures', 
            value: schemaData.procedures, 
            icon: <FileCode size={12} />,
            change: schemaData.procedureChange || 0,
            description: 'Stored procedures'
          },
          { 
            key: 'functions', 
            value: schemaData.functions, 
            icon: <Code size={12} />,
            change: schemaData.functionChange || 0,
            description: 'Return-value routines'
          },
          { 
            key: 'packages', 
            value: schemaData.packages, 
            icon: <Package size={12} />,
            change: schemaData.packageChange || 0,
            description: 'Logical groupings'
          }
        ],
        color: colors.success,
        icon: <Code size={14} />
      },
      // { 
      //   name: 'Database Objects', 
      //   items: [
      //     { 
      //       key: 'triggers', 
      //       value: schemaData.triggers, 
      //       icon: <Zap size={12} />,
      //       change: schemaData.triggerChange || 0,
      //       description: 'Event-driven actions'
      //     },
      //     { 
      //       key: 'indexes', 
      //       value: schemaData.indexes, 
      //       icon: <BarChart3 size={12} />,
      //       change: schemaData.indexChange || 0,
      //       description: 'Query performance'
      //     },
      //     { 
      //       key: 'sequences', 
      //       value: schemaData.sequences || 0, 
      //       icon: <TrendingUp size={12} />,
      //       change: schemaData.sequenceChange || 0,
      //       description: 'Auto-increment values'
      //     }
      //   ],
      //   color: colors.warning,
      //   icon: <Database size={14} />
      // }
    ];

    return (
      <div className="border rounded-xl p-4 hover-lift transition-all duration-300" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.card,
        boxShadow: '0 1px 3px rgba(0,0,0,0.05)'
      }}>
        {/* Header with more stats */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
          <div className="flex-1 min-w-0">
            <div className="flex flex-wrap items-center gap-2 mb-1">
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>
                Schema Statistics
              </h3>
              <div className="flex items-center gap-1 px-2 py-0.5 rounded-full text-xs" 
                style={{ 
                  backgroundColor: schemaData.totalObjectsChange >= 0 ? `${colors.success}20` : `${colors.error}20`,
                  color: schemaData.totalObjectsChange >= 0 ? colors.success : colors.error
                }}>
                <TrendingUp size={10} />
                <span>{Math.abs(schemaData.totalObjectsChange || 0)}</span>
              </div>
            </div>
            <div className="flex flex-wrap items-center gap-2 sm:gap-4">
              <p className="text-xs" style={{ color: colors.textSecondary }}>
                {schemaData.totalObjects} total objects
              </p>
              <div className="text-xs flex items-center gap-1" style={{ color: colors.textTertiary }}>
                <Database size={10} />
                <span>{schemaData.databaseName || 'Main Database'}</span>
              </div>
            </div>
          </div>
          <div className="flex items-center gap-2 shrink-0">
            <div className="p-2 rounded-lg" style={{ backgroundColor: `${colors.primaryDark}15` }}>
              <Database size={18} style={{ color: colors.primaryDark }} />
            </div>
            <div className="text-right">
              <div className="text-xs" style={{ color: colors.textSecondary }}>Size</div>
              <div className="text-sm font-semibold" style={{ color: colors.text }}>
                {schemaData.databaseSize || '2.4 GB'}
              </div>
            </div>
          </div>
        </div>

        {/* Category Overview Bar */}
        {/* <div className="mb-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-3">
            <span className="text-xs font-medium" style={{ color: colors.text }}>
              Object Distribution
            </span>
            <div className="flex flex-wrap items-center gap-2">
              {schemaCategories.map((cat, idx) => (
                <div key={idx} className="flex items-center gap-1">
                  <div className="w-2 h-2 rounded-full" style={{ backgroundColor: cat.color }} />
                  <span className="text-xs" style={{ color: colors.textSecondary }}>
                    {cat.name}
                  </span>
                </div>
              ))}
            </div>
          </div>
          <div className="h-2 rounded-full overflow-hidden flex">
            {schemaCategories.map((cat, idx) => (
              <div 
                key={idx}
                className="h-full transition-all duration-300"
                style={{ 
                  width: `${(cat.items.reduce((sum, item) => sum + item.value, 0) / schemaData.totalObjects) * 100}%`,
                  backgroundColor: cat.color,
                  marginRight: idx < schemaCategories.length - 1 ? '2px' : '0'
                }}
              />
            ))}
          </div>
        </div> */}

        {/* Schema categories with enhanced details */}
        <div className="space-y-4">
          {schemaCategories.map((category, index) => {
            const categoryTotal = category.items.reduce((sum, item) => sum + item.value, 0);
            
            return (
              <div key={index} className="space-y-2 p-3 rounded-lg hover-lift" 
                style={{ backgroundColor: colors.hover }}>
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <div className="p-1 rounded" style={{ backgroundColor: `${category.color}20` }}>
                      {category.icon}
                    </div>
                    <div className="min-w-0">
                      <span className="text-xs font-semibold truncate" style={{ color: colors.text }}>
                        {category.name}
                      </span>
                      <div className="flex items-center gap-2">
                        <span className="text-xs" style={{ color: colors.textSecondary }}>
                          {categoryTotal} objects
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="text-right shrink-0">
                    <div className="text-xs" style={{ color: colors.textSecondary }}>
                      {((categoryTotal / schemaData.totalObjects) * 100).toFixed(1)}% of total
                    </div>
                    <div className="text-xs font-medium" style={{ color: category.color }}>
                      Δ {category.items.reduce((sum, item) => sum + item.change, 0)} this month
                    </div>
                  </div>
                </div>
                
                <div className="grid grid-cols-3 gap-2">
                  {category.items.map((item, idx) => (
                    <div key={idx} 
                      className="border rounded-lg p-2 text-center hover-lift transition-all duration-200"
                      style={{ 
                        borderColor: colors.borderLight,
                        backgroundColor: colors.card,
                      }}>
                      <div className="flex items-center justify-between mb-1">
                        <div className="flex items-center gap-1">
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
                      <div className="text-xs opacity-75 truncate" style={{ color: colors.textSecondary }}>
                        {item.description}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            );
          })}
        </div>

        {/* Enhanced Footer with more metrics */}
        <div className="mt-6 pt-4 border-t" style={{ borderColor: colors.border }}>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <div className="text-xs" style={{ color: colors.textSecondary }}>Last Updated</div>
              <div className="text-sm font-medium flex items-center gap-1 truncate" style={{ color: colors.text }}>
                <Clock size={12} />
                Today, 10:30 AM
              </div>
            </div>
            <div>
              <div className="text-xs" style={{ color: colors.textSecondary }}>Growth Rate</div>
              <div className="text-sm font-medium flex items-center gap-1" style={{ color: colors.success }}>
                <TrendingUp size={12} />
                +{schemaData.monthlyGrowth || 12}%
              </div>
            </div>
            <div>
              <div className="text-xs" style={{ color: colors.textSecondary }}>Schema Version</div>
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
  const ActivityPagination = () => (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-3 p-4 border-t" style={{ borderColor: colors.border }}>
      <div className="text-xs" style={{ color: colors.textSecondary }}>
        Showing {((activityPage - 1) * activitiesPerPage) + 1} - {Math.min(activityPage * activitiesPerPage, recentActivity.length)} of {recentActivity.length} activities
      </div>
      <div className="flex items-center gap-2">
        <button
          onClick={handlePrevPage}
          disabled={activityPage === 1}
          className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
          style={{ 
            backgroundColor: activityPage === 1 ? 'transparent' : colors.hover,
            color: colors.text,
            cursor: activityPage === 1 ? 'not-allowed' : 'pointer'
          }}
        >
          <ChevronLeft size={14} />
        </button>
        
        <div className="flex items-center gap-1">
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
                className="w-6 h-6 rounded text-xs font-medium transition-colors"
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
                className="w-6 h-6 rounded text-xs font-medium transition-colors"
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
          className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
          style={{ 
            backgroundColor: activityPage === totalActivityPages ? 'transparent' : colors.hover,
            color: colors.text,
            cursor: activityPage === totalActivityPages ? 'not-allowed' : 'pointer'
          }}
        >
          <ChevronRightIcon size={14} />
        </button>
      </div>
    </div>
  );

  // Right Sidebar Component
  const RightSidebar = () => (
    <div className={`w-full md:w-80 border-l flex flex-col absolute md:relative inset-y-0 right-0 z-30 transform transition-transform duration-300 ease-in-out ${
      isRightSidebarVisible ? 'translate-x-0' : 'translate-x-full md:translate-x-0'
    }`} style={{ 
      backgroundColor: colors.sidebar,
      borderColor: colors.border
    }}>
      {/* Mobile sidebar header */}
      <div className="flex items-center justify-between p-4 border-b md:hidden" style={{ borderColor: colors.border }}>
        <h3 className="text-sm font-semibold" style={{ color: colors.text }}>
          Side Panel
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
        {/* System Health */}
        {/* <div className="border-b p-4" style={{ borderColor: colors.border }}>
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>
              System Health
            </h3>
            <Activity size={14} style={{ color: colors.textSecondary }} />
          </div>
          <div className="space-y-3">
            {[
              { label: 'CPU Usage', value: systemHealth.cpu, icon: <Cpu size={12} /> },
              { label: 'Memory', value: systemHealth.memory, icon: <HardDrive size={12} /> },
              { label: 'Disk I/O', value: systemHealth.disk, icon: <Database size={12} /> },
              { label: 'Network', value: systemHealth.network, icon: <Network size={12} /> }
            ].map((metric, index) => (
              <div key={index} className="space-y-1">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2 text-xs" style={{ color: colors.textSecondary }}>
                    {metric.icon}
                    {metric.label}
                  </div>
                  <span className="text-xs font-medium" style={{ 
                    color: metric.value > 80 ? colors.error : 
                          metric.value > 60 ? colors.warning : 
                          colors.success 
                  }}>
                    {metric.value}%
                  </span>
                </div>
                <div className="h-1 rounded-full overflow-hidden" style={{ backgroundColor: colors.border }}>
                  <div 
                    className="h-full rounded-full transition-all duration-300"
                    style={{ 
                      width: `${metric.value}%`,
                      backgroundColor: metric.value > 80 ? colors.error : 
                                    metric.value > 60 ? colors.warning : 
                                    colors.success
                    }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div> */}

        {/* Code Generation Stats */}
        {/* <div className="border-b p-4" style={{ borderColor: colors.border }}>
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>
              Code Generation
            </h3>
            <Code size={14} style={{ color: colors.textSecondary }} />
          </div>
          <div className="space-y-2">
            {Object.entries(codeGenerationStats).map(([lang, count]) => (
              <div key={lang} className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-medium capitalize" style={{ color: colors.text }}>
                    {lang}
                  </span>
                </div>
                <span className="text-xs" style={{ color: colors.textSecondary }}>
                  {count} APIs
                </span>
              </div>
            ))}
          </div>
          <div className="mt-3 h-2 rounded-full overflow-hidden flex">
            {Object.entries(codeGenerationStats).map(([lang, count], index) => {
              const total = Object.values(codeGenerationStats).reduce((a, b) => a + b, 0);
              const percentage = (count / total) * 100;
              const colorsMap = {
                java: '#f89820',
                javascript: '#f0db4f',
                python: '#3776ab',
                csharp: '#9b4993'
              };
              
              return (
                <div
                  key={lang}
                  className="h-full"
                  style={{ 
                    width: `${percentage}%`,
                    backgroundColor: colorsMap[lang] || colors.textSecondary
                  }}
                  title={`${lang}: ${count} APIs (${percentage.toFixed(1)}%)`}
                />
              );
            })}
          </div>
        </div> */}

        {/* Recent Deployments */}
        <div className="border-b p-4 mt-3" style={{ borderColor: colors.border }}>
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>
              Recent Activities
            </h3>
            <Rocket size={14} style={{ color: colors.textSecondary }} />
          </div>
          <div className="space-y-4">
            {[
              { name: 'User API v2.1', env: 'Production', status: 'success', time: '2 hours ago' },
              { name: 'Payment API v1.5', env: 'Staging', status: 'success', time: '1 day ago' },
              { name: 'Inventory API', env: 'Development', status: 'pending', time: '2 days ago' }
            ].map((deployment, index) => (
              <div key={index} className="flex items-center justify-between p-2 rounded hover-lift"
                style={{ backgroundColor: colors.hover }}>
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

        {/* Quick Actions */}
        <div className="p-4">
          <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>
            Quick Actions
          </h3>
          <div className="space-y-4">
            <button className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}>
              <Database size={14} />
              <span className="truncate">New Database Connection</span>
            </button>
            <button className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}>
              <FileCode size={14} />
              <span className="truncate">Generate New API</span>
            </button>
            <button className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}>
              <Code size={14} />
              <span className="truncate">View Code Base</span>
            </button>
            <button className="w-full px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}>
              <BookOpen size={14} />
              <span className="truncate">View Documentation</span>
            </button>
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
        .text-lg { font-size: 16px; }
        .text-xl { font-size: 18px; }
        .text-2xl { font-size: 20px; }
      }
    `}</style>

    {/* Main Content */}
    <div className="flex-1 overflow-hidden flex">
      {/* Mobile sidebar overlay */}
      {isRightSidebarVisible && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 z-30 md:hidden"
          onClick={() => setIsRightSidebarVisible(false)}
        />
      )}

      {/* Main content area */}
      <div className="flex-1 overflow-auto p-3 md:p-4">
        <div className="max-w-9xl mt-2 mx-auto pl-0 md:pl-8 pr-0 md:pr-8">
          {/* Key Metrics */}
          <div className="grid grid-cols-2 md:grid-cols-2 lg:grid-cols-4 gap-2 md:gap-4 mb-4 md:mb-6">
            <StatCard
              title="Total Connections"
              value={stats.totalConnections}
              icon={Database}
              change={+5}
              color={colors.success}
            />
            <StatCard
              title="Active APIs"
              value={stats.activeApis}
              icon={FileCode}
              change={+12}
              color={colors.info}
            />
            <StatCard
              title="Total API Calls"
              value={stats.totalCalls.toLocaleString()}
              icon={Activity}
              change={+8.5}
              color={colors.primaryDark}
            />
            <StatCard
              title="Success Rate"
              value={stats.successRate}
              icon={CheckCircle}
              change={+0.2}
              color={colors.success}
            />
          </div>

          {/* Main Grid - Use flex for better height distribution */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 md:gap-6 min-h-[calc(100vh-200px)]">
            {/* Left Column - Schema Statistics */}
            <div className="flex flex-col gap-4 md:gap-6">
              {/* Active Connections */}
              <div className="border rounded-xl" style={{ 
                borderColor: colors.border,
                backgroundColor: colors.card
              }}>
                <div className="p-3 md:p-4 border-b" style={{ borderColor: colors.border }}>
                  <div className="flex items-center justify-between">
                    <h3 className="text-sm font-semibold" style={{ color: colors.text }}>
                      Active Database Connections
                    </h3>
                    <div className="flex items-center gap-2">
                      <span className="text-xs hidden sm:inline" style={{ color: colors.textSecondary }}>
                        {connections.length} connections
                      </span>
                      <button className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                        style={{ backgroundColor: colors.hover }}>
                        <Plus size={14} style={{ color: colors.textSecondary }} />
                      </button>
                    </div>
                  </div>
                </div>
                <div className="p-3 md:p-4">
                  <div className="space-y-3">
                    {connections.map(conn => (
                      <ConnectionCard key={conn.id} connection={conn} />
                    ))}
                  </div>
                </div>
              </div>
             <SchemaStatsCard />
            </div>

            {/* Right Column - Connections and Activity */}
            <div className="flex flex-col gap-4 md:gap-6">
               {/* Recent Activity */}
              <div className="border rounded-xl flex flex-col" style={{ 
                borderColor: colors.border,
                backgroundColor: colors.card
              }}>
                <div className="p-3 md:p-4 border-b flex-shrink-0" style={{ borderColor: colors.border }}>
                  <div className="flex items-center justify-between">
                    <h3 className="text-sm font-semibold" style={{ color: colors.text }}>
                      Recent Activity
                    </h3>
                    <div className="flex items-center gap-2">
                      <select
                        value={activitiesPerPage}
                        onChange={(e) => setActivitiesPerPage(Number(e.target.value))}
                        className="text-xs px-2 py-1 rounded border bg-transparent hidden sm:block"
                        style={{ 
                          borderColor: colors.border,
                          color: colors.text
                        }}
                      >
                        <option value={5}>5 per page</option>
                        <option value={10}>10 per page</option>
                        <option value={15}>15 per page</option>
                      </select>
                      <Clock size={14} style={{ color: colors.textSecondary }} />
                    </div>
                  </div>
                </div>
                <div className="flex-1 overflow-auto min-h-0 space-y-3">
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

        {/* Right Sidebar */}
        <RightSidebar />
      </div>
    </div>
  );
};

export default Dashboard;