// views/index.js
import React, { useState, useEffect, useCallback } from "react";
import { useTheme } from "@/context/ThemeContext.js";
import { useAuth } from "@/context/AuthContext"; // Import auth context
import { Button } from "@/components/ui/button";
import { 
  Database,
  Server,
  FileCode,
  Activity,
  Zap,
  Settings,
  Search,
  Bell,
  ChevronDown,
  MoreVertical,
  RefreshCw,
  Plus,
  CheckCircle,
  AlertCircle,
  TrendingUp,
  Users,
  Shield,
  Download,
  Filter,
  Eye,
  Edit,
  Trash2,
  Copy,
  Globe,
  Cpu,
  HardDrive,
  Network,
  Lock,
  Key,
  Sun,
  Moon,
  BarChart3,
  LineChart,
  Calendar,
  Clock,
  X,
  AlertTriangle,
  Database as DatabaseIcon,
  Folder,
  FolderOpen,
  FileText,
  Code,
  Cloud,
  ShieldCheck,
  CreditCard,
  Package,
  PieChart,
  Table,
  Grid,
  List,
  MessageSquare,
  Mail,
  Layers,
  GitMerge,
  BarChart,
  LineChart as LineChartIcon,
  Terminal,
  Cpu as CpuIcon,
  FileJson,
  BookOpen,
  Share2,
  Upload,
  EyeOff,
  Type,
  Palette,
  Contrast,
  VolumeX,
  ZapOff,
  GitPullRequest,
  ShieldAlert,
  CalendarDays,
  DatabaseZap,
  Network as NetworkIcon,
  FileOutput,
  Code2,
  Search as SearchIcon,
  DownloadCloud,
  UploadCloud,
  UserCheck,
  KeyRound,
  FolderTree,
  FolderTree as FolderTreeIcon,
  BookMarked,
  LayoutDashboard,
  Sliders,
  ChevronRight,
  ArrowUpRight,
  ArrowDownRight,
  BarChart2,
  Shield as ShieldIcon,
  Database as DatabaseIcon2,
  Cpu as CpuIcon2,
  Globe as GlobeIcon,
  Server as ServerIcon,
  Clock as ClockIcon,
  Wifi,
  GitBranch,
  ExternalLink,
  Menu,
  X as XIcon,
  Home,
  Smartphone,
  Zap as ZapIcon,
  Shield as ShieldIcon2,
  Cpu as CpuIcon3,
  Globe as GlobeIcon2,
  Users as UsersIcon,
  File as FileIcon,
  Terminal as TerminalIcon,
  Layers as LayersIcon,
  GitBranch as GitBranchIcon,
  Wifi as WifiIcon,
  BarChart as BarChartIcon,
  Activity as ActivityIcon,
  ChevronLeft,
  ChevronUp,
  ChevronRight as ChevronRightIcon,
  Play,
  Pause,
  StopCircle,
  Battery,
  BatteryCharging,
  Radio,
  CloudRain,
  Wind,
  Thermometer,
  Droplets,
  Gauge,
  Workflow,
  GitCompare,
  GitCommit,
  GitPullRequest as GitPullRequestIcon,
  GitMerge as GitMergeIcon,
  GitFork,
  GitGraph,
  GitBranchPlus,
  CodeXml,
  ServerCog,
  DatabaseBackup,
  ServerCrash,
  ServerOff,
  ServerIcon as ServerIcon2,
  HardDrive as HardDriveIcon,
  MemoryStick,
  Cpu as CpuIcon4,
  Network as NetworkIcon2,
  Wifi as WifiIcon2,
  ShieldCheck as ShieldCheckIcon,
  Lock as LockIcon,
  Key as KeyIcon,
  Fingerprint,
  Scan,
  QrCode,
  ScanFace,
  ShieldHalf,
  ShieldQuestion,
  ShieldX,
  ShieldPlus,
  ShieldMinus,
  ShieldEllipsis,
  BellRing,
  BellOff,
  MessageCircle,
  MessageSquare as MessageSquareIcon,
  MessageCircleWarning,
  MessageCircleQuestion,
  AlertOctagon,
  AlertHexagon,
  AlertDiamond,
  TriangleAlert,
  CircleAlert,
  OctagonAlert,
  HexagonAlert,
  DiamondAlert,
  SquareAlert,
  Circle,
  Square,
  Triangle,
  Hexagon,
  Octagon,
  Diamond,
  Star,
  Sparkles,
  Rocket,
  Satellite,
  SatelliteDish,
  Orbit,
  Planet,
  Comet,
  Meteor,
  Moon as MoonIcon,
  Sun as SunIcon,
  CloudSun,
  CloudMoon,
  CloudLightning,
  CloudSnow,
  CloudFog,
  Tornado,
  Hurricane,
  Earthquake,
  Volcano,
  Snowflake,
  Wind as WindIcon,
  Waves,
  Droplet,
  ThermometerSun,
  ThermometerSnowflake,
  Fire,
  Sparkle,
  Atom,
  Beaker,
  FlaskRound,
  Microscope,
  Telescope,
  Satellite as SatelliteIcon,
  Cpu as CpuIcon5,
  Brain,
  BrainCircuit,
  BrainCog,
  Cogs,
  Cog,
  Settings as SettingsIcon,
  SlidersHorizontal,
  ToggleLeft,
  ToggleRight,
  SwitchCamera,
  SwitchCamera as SwitchCameraIcon,
  ToggleLeft as ToggleLeftIcon,
  ToggleRight as ToggleRightIcon,
  Power,
  PowerOff,
  BatteryFull,
  BatteryLow,
  BatteryMedium,
  BatteryWarning,
  Plug,
  PlugZap,
  PlugZap2,
  Zap as ZapIcon2,
  LightningBolt,
  Energy,
  Fuel,
  Oil,
  Gas,
  Water,
  Fire as FireIcon,
  Sparkles as SparklesIcon,
  Magic,
  Wand,
  WandSparkles,
  HatWizard,
  CrystalBall,
  Scroll,
  BookOpenCheck,
  BookOpenText,
  BookKey,
  BookLock,
  BookMarked as BookMarkedIcon,
  BookPlus,
  BookMinus,
  BookX,
  BookOpen as BookOpenIcon,
  Book as BookIcon,
  Notebook,
  NotebookText,
  NotebookPen,
  PenTool,
  PenLine,
  PenSquare,
  PenBox,
  Pen as PenIcon,
  Highlighter,
  Pencil,
  PencilLine,
  PencilRuler,
  Ruler,
  SquarePen,
  Edit2,
  Edit3,
  Eraser,
  Paintbrush,
  Paintbrush2,
  Palette as PaletteIcon,
  Dropper,
  Contrast as ContrastIcon,
  Image,
  Images,
  Camera,
  Video,
  Film,
  Music,
  Headphones,
  Speaker,
  Volume2,
  Mic,
  Mic2,
  MicOff,
  VideoOff,
  CameraOff,
  Phone,
  PhoneCall,
  PhoneForwarded,
  PhoneIncoming,
  PhoneMissed,
  PhoneOff,
  PhoneOutgoing,
  Voicemail,
  MessageCircle as MessageCircleIcon,
  MessageSquare as MessageSquareIcon2,
  MessageCirclePlus,
  MessageSquarePlus,
  MessageCircleDashed,
  MessageSquareDashed,
  Mail as MailIcon,
  MailOpen,
  MailPlus,
  MailMinus,
  MailX,
  MailWarning,
  MailCheck,
  MailSearch,
  Inbox,
  Send,
  Archive,
  ArchiveRestore,
  InboxFull,
  Mailbox,
  Package as PackageIcon,
  Package2,
  PackageCheck,
  PackageX,
  PackageSearch,
  Box,
  Cube,
  Cuboid,
  Cylinder,
  Cone,
  Pyramid,
  Sphere,
  Torus,
  Dodecahedron,
  Icosahedron,
  Octahedron,
  Tetrahedron,
  Diamond as DiamondIcon,
  Hexagon as HexagonIcon,
  Octagon as OctagonIcon,
  Triangle as TriangleIcon,
  Circle as CircleIcon,
  Square as SquareIcon,
  RectangleHorizontal,
  RectangleVertical,
  Ellipse,
  Scissors,
  ScissorsLineDashed,
  Crop,
  Crop as CropIcon,
  Frame,
  Grid2X2,
  Grid3X3,
  Columns,
  Rows,
  PanelLeft,
  PanelRight,
  PanelTop,
  PanelBottom,
  Sidebar,
  SidebarClose,
  SidebarOpen,
  LayoutGrid,
  LayoutList,
  LayoutTemplate,
  LayoutDashboard as LayoutDashboardIcon,
  Columns as ColumnsIcon,
  Rows as RowsIcon,
  Split,
  Combine,
  AlignLeft,
  AlignCenter,
  AlignRight,
  AlignJustify,
  AlignVerticalJustifyStart,
  AlignVerticalJustifyCenter,
  AlignVerticalJustifyEnd,
  AlignHorizontalJustifyStart,
  AlignHorizontalJustifyCenter,
  AlignHorizontalJustifyEnd,
  AlignHorizontalSpaceBetween,
  AlignVerticalSpaceBetween,
  AlignHorizontalSpaceAround,
  AlignVerticalSpaceAround,
  AlignStartVertical,
  AlignStartHorizontal,
  AlignEndVertical,
  AlignEndHorizontal,
  AlignCenterVertical,
  AlignCenterHorizontal,
  Space,
  Indent,
  Outdent,
  List as ListIcon,
  ListChecks,
  ListTodo,
  ListX,
  ListPlus,
  ListMinus,
  ListOrdered,
  ListRestart,
  ListTree,
  ListFilter,
  ListVideo,
  ListMusic,
  ListImage,
  ListEnd,
  ListStart,
  ListCollapse,
  ListExpand,
  ListCheck,
  ListCheck2,
  ListCheck3,
  Checklist,
  CheckSquare,
  SquareCheck,
  CheckCircle2,
  CircleCheck,
  Check,
  X as XIcon2,
  XCircle,
  XSquare,
  XOctagon,
  AlertCircle as AlertCircleIcon,
  AlertTriangle as AlertTriangleIcon,
  AlertOctagon as AlertOctagonIcon,
  AlertHexagon as AlertHexagonIcon,
  AlertDiamond as AlertDiamondIcon,
  TriangleAlert as TriangleAlertIcon,
  CircleAlert as CircleAlertIcon,
  OctagonAlert as OctagonAlertIcon,
  HexagonAlert as HexagonAlertIcon,
  DiamondAlert as DiamondAlertIcon,
  SquareAlert as SquareAlertIcon,
  Info,
  HelpCircle,
  QuestionMark,
  CircleHelp,
  SquareHelp,
  TriangleHelp,
  HexagonHelp,
  OctagonHelp,
  DiamondHelp,
  Brain as BrainIcon,
  BrainCog as BrainCogIcon,
  BrainCircuit as BrainCircuitIcon,
  Cpu as CpuIcon6,
  Server as ServerIcon3,
  Database as DatabaseIcon3,
  Network as NetworkIcon3,
  Shield as ShieldIcon3,
  Zap as ZapIcon3,
  Users as UsersIcon2,
  File as FileIcon2,
  Folder as FolderIcon,
  Globe as GlobeIcon3,
  Clock as ClockIcon2,
  Calendar as CalendarIcon,
  Bell as BellIcon,
  Settings as SettingsIcon2,
  Search as SearchIcon2,
  Menu as MenuIcon,
  User as UserIcon,
  LogOut,
  LogIn,
  UserPlus,
  UserMinus,
  UserX,
  UserCheck as UserCheckIcon,
  UserCog,
  UsersCog,
  UserCircle,
  UserCircle2,
  UserSquare,
  UserSquare2,
  CircleUser,
  SquareUser,
  TriangleUser,
  HexagonUser,
  OctagonUser,
  DiamondUser,
  Crown,
  Award,
  Trophy,
  Medal,
  Star as StarIcon,
  Heart,
  Gem,
  Diamond as DiamondIcon2,
  Sparkle as SparkleIcon,
  Target,
  Crosshair,
  Crosshair2,
  Focus,
  Scan as ScanIcon,
  ScanLine,
  ScanText,
  ScanEye,
  ScanFace as ScanFaceIcon,
  QrCode as QrCodeIcon,
  Barcode,
  Waves as WavesIcon,
  Radio as RadioIcon,
  Satellite as SatelliteIcon2,
  SatelliteDish as SatelliteDishIcon,
  Broadcast,
  Antenna,
  TowerControl,
  Radar,
  Sonar,
  Wifi as WifiIcon3,
  Bluetooth,
  Nfc,
  Signal,
  SignalHigh,
  SignalMedium,
  SignalLow,
  SignalZero,
  WifiOff,
  BluetoothOff,
  NfcOff,
  SignalOff,
  Router,
  RouterIcon,
  Modem,
  Switch,
  Hub,
  ServerRack,
  Rack,
  Database as DatabaseIcon4,
  HardDrive as HardDriveIcon2,
  Cpu as CpuIcon7,
  MemoryStick as MemoryStickIcon,
  Motherboard,
  CircuitBoard,
  Chip,
  Microchip,
  Processor,
  Cpu as CpuIcon8,
  Brain as BrainIcon2
} from "lucide-react";

// Import your empty components
import CodeBase from "./CodeBase.js";
import Collections from "./Collections.js";
import APISecurity from "./APISecurity.js";
import Dashboard from "./Dashboard.js";
import Documentation from "./Documentation.js";
import SchemaBrowser from "./SchemaBrowser.js";

import ConnectionDetailsModal from "@/components/modals/ConnectionDetailsModal";
import APIDetailsModal from "@/components/modals/APIDetailsModal";
import ReportDetailsModal from "@/components/modals/ReportDetailsModal";
import SchemaDetailsModal from "@/components/modals/SchemaDetailsModal";
import UserDetailsModal from "@/components/modals/UserDetailsModal";
import NotificationDetailsModal from "@/components/modals/NotificationDetailsModal";
import PerformanceMetricsModal from "@/components/modals/PerformanceMetricsModal";

import { useNavigate } from "react-router-dom"; // Import useNavigate

export default function EntryPage() {
  const { theme, toggle, customTheme, setCustomTheme } = useTheme();
  const { logout, user } = useAuth(); // Get auth context
  const navigate = useNavigate(); // Get navigate function
  
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [activeTab, setActiveTab] = useState("overview");
  const [showConnectionWizard, setShowConnectionWizard] = useState(false);
  const [showApiWizard, setShowApiWizard] = useState(false);
  const [showNotificationCenter, setShowNotificationCenter] = useState(false);
  const [showThemeCustomizer, setShowThemeCustomizer] = useState(false);
  const [loading, setLoading] = useState(false);
  const [timeRange, setTimeRange] = useState("24h");
  const [searchQuery, setSearchQuery] = useState("");
  const [notifications, setNotifications] = useState([]);
  const [activeConnections, setActiveConnections] = useState(0);
  const [totalApiCalls, setTotalApiCalls] = useState(0);
  const [apiPerformance, setApiPerformance] = useState([]);
  const [schemaData, setSchemaData] = useState({});
  const [databaseStats, setDatabaseStats] = useState({});
  const [isMobile, setIsMobile] = useState(false);

  const [showConnectionDetails, setShowConnectionDetails] = useState(false);
  const [showAPIDetails, setShowAPIDetails] = useState(false);
  const [showReportDetails, setShowReportDetails] = useState(false);
  const [showSchemaDetails, setShowSchemaDetails] = useState(false);
  const [showUserDetails, setShowUserDetails] = useState(false);
  const [showNotificationDetails, setShowNotificationDetails] = useState(false);
  const [showPerformanceMetrics, setShowPerformanceMetrics] = useState(false);

  const [selectedConnection, setSelectedConnection] = useState(null);
  const [selectedAPI, setSelectedAPI] = useState(null);
  const [selectedReport, setSelectedReport] = useState(null);
  const [selectedSchema, setSelectedSchema] = useState(null);
  const [selectedUser, setSelectedUser] = useState(null);
  const [selectedNotification, setSelectedNotification] = useState(null);
  const [selectedMetric, setSelectedMetric] = useState(null);

  // Session management states
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false);
  const [sessionExpired, setSessionExpired] = useState(false);

  const isDark = theme === 'dark';

  // Check screen size on mount and resize
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
      if (window.innerWidth >= 768) {
        setSidebarOpen(true);
      } else {
        setSidebarOpen(false);
        setMobileMenuOpen(false);
      }
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  // Close user menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (userMenuOpen && !event.target.closest('.user-menu-container')) {
        setUserMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [userMenuOpen]);

  // Initialize comprehensive data
  useEffect(() => {
    // Generate dummy notifications
    const dummyNotifications = [
      {
        id: 1,
        type: 'success',
        title: 'API Generated Successfully',
        message: 'Customer Orders API has been deployed to production',
        time: '2 min ago',
        read: false,
        action: 'view'
      },
      {
        id: 2,
        type: 'warning',
        title: 'Database Connection Issue',
        message: 'MySQL connection showing high latency (15ms)',
        time: '15 min ago',
        read: false,
        action: 'retry'
      },
      {
        id: 3,
        type: 'info',
        title: 'System Update Available',
        message: 'New version 2.4.0 is ready for deployment',
        time: '1 hour ago',
        read: true,
        action: 'update'
      }
    ];

    // Generate API performance data
    const dummyApiPerformance = [
      { name: 'Get Customer Orders', latency: 45, calls: 1250, successRate: 99.8, endpoint: '/api/v1/customers/{id}/orders' },
      { name: 'Create User Session', latency: 32, calls: 892, successRate: 99.9, endpoint: '/api/v1/sessions' },
      { name: 'Update Inventory', latency: 67, calls: 156, successRate: 98.5, endpoint: '/api/v1/inventory/{productId}' },
      { name: 'Generate Report', latency: 125, calls: 342, successRate: 99.2, endpoint: '/api/v1/reports' }
    ];

    // Generate schema data
    const dummySchemaData = {
      oracle: {
        tables: 45,
        views: 12,
        procedures: 23,
        packages: 8,
        functions: 15
      },
      postgresql: {
        tables: 32,
        views: 18,
        procedures: 9,
        functions: 21,
        materializedViews: 4
      },
      mysql: {
        tables: 28,
        views: 8,
        procedures: 12,
        functions: 7,
        events: 3
      }
    };

    // Generate database statistics
    const dummyDatabaseStats = {
      totalConnections: 8,
      activeConnections: 6,
      totalApis: 24,
      activeApis: 18,
      totalCalls: "12,540",
      avgLatency: "48ms",
      successRate: "99.8%",
      uptime: "99.9%"
    };

    setNotifications(dummyNotifications);
    setApiPerformance(dummyApiPerformance);
    setSchemaData(dummySchemaData);
    setDatabaseStats(dummyDatabaseStats);
    setActiveConnections(6);
    setTotalApiCalls(12540);
  }, []);

  // Database connections with more details
  const connections = [
    {
      id: "conn_1",
      name: "Oracle Production",
      type: "oracle",
      host: "oracle-prod.internal:1521",
      status: "active",
      latency: "12ms",
      uptime: "99.9%",
      connections: 45,
      lastSync: "2024-01-15T10:30:00Z",
      color: "bg-blue-500",
      ssl: true,
      poolSize: 50,
      database: "ORCL",
      schema: "APP_SCHEMA",
      version: "19c"
    },
    {
      id: "conn_2",
      name: "PostgreSQL Analytics",
      type: "postgresql",
      host: "postgres-analytics.internal:5432",
      status: "active",
      latency: "8ms",
      uptime: "99.8%",
      connections: 32,
      lastSync: "2024-01-15T09:15:00Z",
      color: "bg-green-500",
      ssl: true,
      poolSize: 40,
      database: "analytics_db",
      schema: "public",
      version: "15"
    },
    {
      id: "conn_3",
      name: "MySQL Users",
      type: "mysql",
      host: "mysql-users.internal:3306",
      status: "warning",
      latency: "15ms",
      uptime: "98.5%",
      connections: 28,
      lastSync: "2024-01-14T16:45:00Z",
      color: "bg-orange-500",
      ssl: false,
      poolSize: 30,
      database: "user_db",
      schema: "app_data",
      version: "8.0"
    }
  ];

  // API configurations with more details
  const apis = [
    {
      id: "api_1",
      name: "Get Customer Orders",
      endpoint: "/api/v1/customers/{id}/orders",
      method: "GET",
      database: "oracle",
      calls: 1250,
      latency: "45ms",
      status: "active",
      lastCall: "2024-01-15T10:25:00Z",
      auth: "API Key",
      rateLimit: "1000/hour",
      description: "Retrieve customer order history"
    },
    {
      id: "api_2",
      name: "Create User Session",
      endpoint: "/api/v1/sessions",
      method: "POST",
      database: "postgresql",
      calls: 892,
      latency: "32ms",
      status: "active",
      lastCall: "2024-01-15T09:30:00Z",
      auth: "JWT",
      rateLimit: "500/hour",
      description: "Create new user authentication session"
    },
    {
      id: "api_3",
      name: "Update Inventory",
      endpoint: "/api/v1/inventory/{productId}",
      method: "PUT",
      database: "mysql",
      calls: 156,
      latency: "67ms",
      status: "testing",
      lastCall: "2024-01-15T08:15:00Z",
      auth: "API Key",
      rateLimit: "200/hour",
      description: "Update product inventory levels"
    }
  ];

  // Dashboard stats
  const stats = {
    totalConnections: connections.length,
    activeConnections: connections.filter(c => c.status === 'active').length,
    totalApis: apis.length,
    activeApis: apis.filter(a => a.status === 'active').length,
    totalCalls: apis.reduce((sum, api) => sum + api.calls, 0).toLocaleString(),
    avgLatency: "48ms",
    successRate: "99.8%",
    uptime: "99.9%",
    sslConnections: connections.filter(c => c.ssl).length,
    poolUtilization: "78%"
  };

  // Navigation items with icons
  const navItems = [
    { id: "overview", label: "Dashboard", icon: LayoutDashboard, component: <Dashboard setActiveTab={setActiveTab} /> },
    { id: "schema-browser", label: "Schema Browser", icon: DatabaseBackup, component: <SchemaBrowser /> },
    { id: "api-collections", label: "Collections", icon: FileCode, component: <Collections /> },
    { id: "api-docs", label: "Documenation", icon: Activity, component: <Documentation /> },
    { id: "code-base", label: "Code Base", icon: Code, component: <CodeBase /> },
    { id: "security", label: "API Security", icon: Shield, component: <APISecurity /> },
  ];

  // Quick actions
  const quickActions = [
    { id: "new-connection", label: "New Connection", icon: Database, color: "bg-blue-500", onClick: () => setShowConnectionWizard(true) },
    { id: "generate-api", label: "Generate API", icon: Zap, color: "bg-orange-500", onClick: () => setShowApiWizard(true) },
    { id: "run-test", label: "Run Test", icon: Play, color: "bg-green-500", onClick: () => alert("Running system test...") },
    { id: "export-data", label: "Export Data", icon: Download, color: "bg-purple-500", onClick: () => handleExportData('all') }
  ];

  // Status badge component
  const StatusBadge = ({ status, size = "sm" }) => {
    const config = {
      active: { 
        color: "text-green-700 bg-green-100 dark:bg-green-900/30 dark:text-green-400", 
        icon: CheckCircle,
        text: "Active"
      },
      warning: { 
        color: "text-amber-700 bg-amber-100 dark:bg-amber-900/30 dark:text-amber-400", 
        icon: AlertCircle,
        text: "Warning"
      },
      testing: { 
        color: "text-blue-700 bg-blue-100 dark:bg-blue-900/30 dark:text-blue-400", 
        icon: Clock,
        text: "Testing"
      }
    };

    const { color, icon: Icon, text } = config[status] || config.active;
    const sizeClasses = size === "sm" ? "px-2 py-1 text-xs" : "px-3 py-1.5 text-sm";

    return (
      <div className={`inline-flex items-center gap-1.5 rounded-full ${sizeClasses} font-medium ${color}`}>
        <Icon className="h-3 w-3" />
        <span>{text}</span>
      </div>
    );
  };

  // Method badge component
  const MethodBadge = ({ method }) => {
    const colors = {
      GET: "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400",
      POST: "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400",
      PUT: "bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400",
      DELETE: "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400"
    };

    return (
      <span className={`px-2.5 py-1 rounded-md text-xs font-medium ${colors[method] || 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-400'}`}>
        {method}
      </span>
    );
  };

  // Database type badge
  const DatabaseBadge = ({ type }) => {
    const config = {
      oracle: { color: "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400", icon: Globe },
      postgresql: { color: "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400", icon: Server },
      mysql: { color: "bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400", icon: Database }
    };

    const { color, icon: Icon } = config[type] || config.mysql;

    return (
      <div className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-medium ${color}`}>
        <Icon className="h-3 w-3" />
        <span className="capitalize">{type}</span>
      </div>
    );
  };

  // Handler functions
  const handleRefresh = () => {
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      setTotalApiCalls(prev => prev + Math.floor(Math.random() * 100));
    }, 1000);
  };

  const handleExportData = (type) => {
    alert(`Exporting ${type} data...`);
  };

  const handleTestConnection = (connectionId) => {
    setLoading(true);
    setTimeout(() => {
      const connection = connections.find(c => c.id === connectionId);
      if (connection) {
        alert(`Testing connection to ${connection.name}...\nConnection test successful.`);
      }
      setLoading(false);
    }, 1500);
  };

  const handleAPIClick = (api) => {
    setSelectedAPI(api);
    setShowAPIDetails(true);
  };

  const handleConnectionClick = (connection) => {
    setSelectedConnection(connection);
    setShowConnectionDetails(true);
  };

  const handleReportClick = (report) => {
    setSelectedReport(report);
    setShowReportDetails(true);
  };

  const handleTabChange = (tabId) => {
    setActiveTab(tabId);
    if (isMobile) {
      setMobileMenuOpen(false);
    }
  };

  // UPDATED: Handle logout using auth context
  const handleManualLogout = () => {
    setUserMenuOpen(false);
    setShowLogoutConfirm(true);
  };

  // UPDATED: Confirm logout
  const confirmLogout = useCallback(() => {
    setShowLogoutConfirm(false);
    setSessionExpired('manual');
    
    // Use the auth context logout
    setTimeout(() => {
      logout(); // This should handle clearing tokens and redirecting
      navigate('/login', { replace: true });
    }, 1000);
  }, [logout, navigate]);

  // Get current active component
  const getActiveComponent = () => {
    const activeNavItem = navItems.find(item => item.id === activeTab);
    
    if (activeNavItem) {
      // Pass theme and other context props to each component
      return React.cloneElement(activeNavItem.component, {
        theme: theme,
        isDark: isDark,
        customTheme: customTheme,
        toggleTheme: toggle // from useTheme()
      });
    }
    return null;
  };

  // Mobile Navigation Menu
  const MobileNavigationMenu = () => {
    if (!mobileMenuOpen) return null;

    return (
      <div className="fixed inset-0 z-50 lg:hidden">
        <div className="fixed inset-0 bg-black/50" onClick={() => setMobileMenuOpen(false)} />
        <div className={`fixed left-0 top-0 bottom-0 w-72 ${isDark ? 'bg-gray-900' : 'bg-white'} shadow-xl`}>
          <div className="p-6">
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center gap-3">
                <div className={`p-2 rounded-xl ${isDark ? 'bg-orange-500/20' : 'bg-orange-500/10'}`}>
                  <DatabaseZap className={`h-6 w-6 ${isDark ? 'text-orange-400' : 'text-orange-600'}`} />
                </div>
                <div>
                  <h2 className={`text-lg font-bold ${isDark ? 'text-white' : 'text-gray-900'}`}>
                    API Platform
                  </h2>
                  <p className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-600'}`}>
                    Dashboard
                  </p>
                </div>
              </div>
              <button
                onClick={() => setMobileMenuOpen(false)}
                className={`p-2 rounded-lg ${isDark ? 'hover:bg-gray-800' : 'hover:bg-gray-100'}`}
              >
                <X className={`h-5 w-5 ${isDark ? 'text-gray-300' : 'text-gray-700'}`} />
              </button>
            </div>

            {/* Mobile Navigation */}
            <nav className="space-y-1 mb-8">
              {navItems.map(item => {
                const Icon = item.icon;
                const isActive = activeTab === item.id;
                return (
                  <button
                    key={item.id}
                    onClick={() => handleTabChange(item.id)}
                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm transition-all ${
                      isActive
                        ? isDark
                          ? 'bg-orange-500/20 text-orange-400 border border-orange-500/30'
                          : 'bg-orange-50 text-orange-600 border border-orange-200'
                        : isDark
                          ? 'text-gray-400 hover:text-gray-300 hover:bg-gray-800'
                          : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                    }`}
                  >
                    <Icon className="h-5 w-5" />
                    <span>{item.label}</span>
                  </button>
                );
              })}
            </nav>

            {/* Quick Actions */}
            <div className="mb-8">
              <h3 className={`text-sm font-semibold ${isDark ? 'text-gray-400' : 'text-gray-500'} mb-3 px-4`}>
                Quick Actions
              </h3>
              <div className="space-y-2">
                <button
                  onClick={() => {
                    setShowConnectionWizard(true);
                    setMobileMenuOpen(false);
                  }}
                  className={`w-full flex items-center gap-3 px-4 py-3 text-sm rounded-xl border transition-colors ${
                    isDark
                      ? 'border-gray-800 hover:border-gray-700 hover:bg-gray-800'
                      : 'border-gray-300 hover:border-gray-400 hover:bg-gray-50'
                  }`}
                >
                  <Database className="h-4 w-4" />
                  <span>New Connection</span>
                </button>
                <button
                  onClick={() => {
                    setShowApiWizard(true);
                    setMobileMenuOpen(false);
                  }}
                  className={`w-full flex items-center gap-3 px-4 py-3 text-sm rounded-xl border transition-colors ${
                    isDark
                      ? 'border-gray-800 hover:border-gray-700 hover:bg-gray-800'
                      : 'border-gray-300 hover:border-gray-400 hover:bg-gray-50'
                  }`}
                >
                  <Zap className="h-4 w-4" />
                  <span>Generate API</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  // Database Connection Wizard
  const DatabaseConnectionWizard = () => {
    const [step, setStep] = useState(1);
    const [dbType, setDbType] = useState('oracle');

    const handleClose = () => {
      setShowConnectionWizard(false);
      setStep(1);
    };

    if (!showConnectionWizard) return null;

    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className={`rounded-2xl ${isDark ? 'bg-gray-900' : 'bg-white'} w-full max-w-md shadow-xl`}>
          <div className="p-6 border-b dark:border-gray-800">
            <div className="flex items-center justify-between">
              <div>
                <h3 className={`text-lg font-semibold ${isDark ? 'text-white' : 'text-gray-900'}`}>
                  New Connection
                </h3>
                <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-600'} mt-1`}>
                  Connect to your database
                </p>
              </div>
              <button
                onClick={handleClose}
                className={`p-2 rounded-lg ${isDark ? 'hover:bg-gray-800' : 'hover:bg-gray-100'}`}
              >
                <X className={`h-5 w-5 ${isDark ? 'text-gray-400' : 'text-gray-500'}`} />
              </button>
            </div>
          </div>
          
          <div className="p-6">
            <div className="space-y-6">
              <div>
                <label className={`block text-sm font-medium mb-3 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                  Database Type
                </label>
                <div className="grid grid-cols-3 gap-3">
                  {['oracle', 'postgresql', 'mysql'].map(type => (
                    <button
                      key={type}
                      onClick={() => setDbType(type)}
                      className={`p-4 rounded-xl border flex flex-col items-center transition-all ${
                        dbType === type
                          ? isDark
                            ? 'border-orange-500 bg-orange-500/10'
                            : 'border-orange-500 bg-orange-50'
                          : isDark
                          ? 'border-gray-800 hover:border-gray-700'
                          : 'border-gray-300 hover:border-gray-400'
                      }`}
                    >
                      <DatabaseBadge type={type} />
                      <span className="text-xs mt-2 capitalize">{type}</span>
                    </button>
                  ))}
                </div>
              </div>

              <div className="space-y-4">
                <div>
                  <label className={`block text-sm font-medium mb-2 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                    Connection Name
                  </label>
                  <input
                    type="text"
                    placeholder="My Database"
                    className={`w-full px-4 py-3 rounded-xl border transition-colors outline-none ${
                      isDark 
                        ? 'bg-gray-800 border-gray-700 text-white placeholder-gray-500 focus:border-orange-500' 
                        : 'bg-white border-gray-300 text-gray-900 placeholder-gray-400 focus:border-orange-500'
                    }`}
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className={`block text-sm font-medium mb-2 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                      Host
                    </label>
                    <input
                      type="text"
                      placeholder="localhost"
                      className={`w-full px-4 py-3 rounded-xl border transition-colors outline-none ${
                        isDark 
                          ? 'bg-gray-800 border-gray-700 text-white placeholder-gray-500 focus:border-orange-500' 
                          : 'bg-white border-gray-300 text-gray-900 placeholder-gray-400 focus:border-orange-500'
                    }`}
                    />
                  </div>
                  <div>
                    <label className={`block text-sm font-medium mb-2 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                      Port
                    </label>
                    <input
                      type="text"
                      placeholder={dbType === 'oracle' ? '1521' : dbType === 'postgresql' ? '5432' : '3306'}
                      className={`w-full px-4 py-3 rounded-xl border transition-colors outline-none ${
                        isDark 
                          ? 'bg-gray-800 border-gray-700 text-white placeholder-gray-500 focus:border-orange-500' 
                          : 'bg-white border-gray-300 text-gray-900 placeholder-gray-400 focus:border-orange-500'
                    }`}
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="p-6 border-t dark:border-gray-800">
            <div className="flex items-center gap-3">
              <Button
                onClick={handleClose}
                variant="outline"
                className={`flex-1 ${isDark ? 'border-gray-700 hover:bg-gray-800' : ''}`}
              >
                Cancel
              </Button>
              <Button
                onClick={() => {
                  alert('Connection created!');
                  handleClose();
                }}
                className="flex-1 bg-orange-600 hover:bg-orange-700 text-white"
              >
                Create Connection
              </Button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  // API Generation Wizard
  const ApiGenerationWizard = () => {
    const handleClose = () => {
      setShowApiWizard(false);
    };

    if (!showApiWizard) return null;

    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className={`rounded-2xl ${isDark ? 'bg-gray-900' : 'bg-white'} w-full max-w-md shadow-xl`}>
          <div className="p-6 border-b dark:border-gray-800">
            <div className="flex items-center justify-between">
              <div>
                <h3 className={`text-lg font-semibold ${isDark ? 'text-white' : 'text-gray-900'}`}>
                  Generate API
                </h3>
                <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-600'} mt-1`}>
                  Create new API from database
                </p>
              </div>
              <button
                onClick={handleClose}
                className={`p-2 rounded-lg ${isDark ? 'hover:bg-gray-800' : 'hover:bg-gray-100'}`}
              >
                <X className={`h-5 w-5 ${isDark ? 'text-gray-400' : 'text-gray-500'}`} />
              </button>
            </div>
          </div>
          
          <div className="p-6">
            <div className="space-y-6">
              <div>
                <label className={`block text-sm font-medium mb-3 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                  Select Database
                </label>
                <select className={`w-full px-4 py-3 rounded-xl border transition-colors outline-none ${
                  isDark 
                    ? 'bg-gray-800 border-gray-700 text-white focus:border-orange-500' 
                    : 'bg-white border-gray-300 text-gray-900 focus:border-orange-500'
                }`}>
                  <option>Oracle Production</option>
                  <option>PostgreSQL Analytics</option>
                  <option>MySQL Users</option>
                </select>
              </div>

              <div>
                <label className={`block text-sm font-medium mb-3 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                  API Name
                </label>
                <input
                  type="text"
                  placeholder="Customer Orders API"
                  className={`w-full px-4 py-3 rounded-xl border transition-colors outline-none ${
                    isDark 
                      ? 'bg-gray-800 border-gray-700 text-white placeholder-gray-500 focus:border-orange-500' 
                      : 'bg-white border-gray-300 text-gray-900 placeholder-gray-400 focus:border-orange-500'
                  }`}
                />
              </div>

              <div>
                <label className={`block text-sm font-medium mb-3 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                  Endpoint
                </label>
                <div className="flex items-center">
                  <span className={`px-4 py-3 rounded-l-xl border-y border-l ${
                    isDark ? 'bg-gray-800 border-gray-700 text-gray-300' : 'bg-gray-100 border-gray-300 text-gray-600'
                  }`}>
                    /api/v1
                  </span>
                  <input
                    type="text"
                    placeholder="/customers/{id}/orders"
                    className={`flex-1 px-4 py-3 rounded-r-xl border transition-colors outline-none ${
                      isDark 
                        ? 'bg-gray-800 border-gray-700 text-white placeholder-gray-500 focus:border-orange-500' 
                        : 'bg-white border-gray-300 text-gray-900 placeholder-gray-400 focus:border-orange-500'
                    }`}
                  />
                </div>
              </div>
            </div>
          </div>

          <div className="p-6 border-t dark:border-gray-800">
            <div className="flex items-center gap-3">
              <Button
                onClick={handleClose}
                variant="outline"
                className={`flex-1 ${isDark ? 'border-gray-700 hover:bg-gray-800' : ''}`}
              >
                Cancel
              </Button>
              <Button
                onClick={() => {
                  alert('API generated successfully!');
                  handleClose();
                }}
                className="flex-1 bg-orange-600 hover:bg-orange-700 text-white"
              >
                Generate API
              </Button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  // Logout Confirmation Modal
  const LogoutConfirmationModal = () => {
    if (!showLogoutConfirm) return null;

    return (
      <>
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50" onClick={() => setShowLogoutConfirm(false)} />
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className={`w-full max-w-md rounded-2xl shadow-2xl ${
            isDark ? 'bg-gray-900/95 backdrop-blur-lg border-gray-800' : 'bg-white/95 backdrop-blur-lg border-gray-200'
          } border ${isDark ? 'text-white' : 'text-gray-900'}`}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="relative p-6">
              <button
                onClick={() => setShowLogoutConfirm(false)}
                className="absolute right-4 top-4 p-2 rounded-full hover:bg-gray-500/20 transition-colors"
              >
                <X size={20} />
              </button>

              <div className="text-center space-y-4">
                <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-500/20 mb-2">
                  <Shield className={isDark ? "text-red-400" : "text-red-500"} size={32} />
                </div>
                
                <h2 className="text-xl font-bold">Confirm Logout</h2>
                
                <p className={isDark ? "text-gray-400" : "text-gray-600"}>
                  Are you sure you want to logout from the API Platform? 
                  Any unsaved changes will be lost.
                </p>

                <div className={`rounded-lg p-3 ${
                  isDark ? "bg-red-500/10 border-red-500/20" : "bg-red-50 border-red-200"
                } border`}>
                  <p className={`text-sm ${isDark ? "text-red-400" : "text-red-600"}`}>
                    <span className="font-semibold">Security Note:</span> This will end your current session and clear all temporary data.
                  </p>
                </div>

                <div className="flex gap-3 pt-6">
                  <button
                    onClick={() => setShowLogoutConfirm(false)}
                    className={`flex-1 py-3 px-4 rounded-lg border ${
                      isDark 
                        ? "border-gray-700 hover:bg-gray-800" 
                        : "border-gray-300 hover:bg-gray-100"
                    } transition-colors font-medium`}
                  >
                    Cancel
                  </button>
                  <button
                    onClick={confirmLogout}
                    className={`flex-1 py-3 px-4 rounded-lg ${
                      isDark ? "bg-red-600 hover:bg-red-700" : "bg-red-500 hover:bg-red-600"
                    } text-white font-medium transition-colors`}
                  >
                    Logout Now
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </>
    );
  };

  // Session Expired Overlay
  const SessionExpiredOverlay = () => {
  if (!sessionExpired) return null;

  return (
    <>
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50" />
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div className={`w-full max-w-md rounded-2xl shadow-2xl ${
          isDark ? 'bg-gray-900/95 backdrop-blur-lg border-gray-800' : 'bg-white/95 backdrop-blur-lg border-gray-200'
        } border ${isDark ? 'text-white' : 'text-gray-900'}`}>
          <div className="relative p-6">
            <div className="text-center space-y-4">
              <div className={`inline-flex items-center justify-center w-16 h-16 rounded-full mb-2 ${
                sessionExpired === 'manual'
                  ? isDark ? 'bg-red-500/20' : 'bg-red-500/20'
                  : isDark ? 'bg-blue-500/20' : 'bg-blue-500/20'
              }`}>
                <AlertTriangle
                  size={32}
                  className={
                    sessionExpired === 'manual'
                      ? isDark ? "text-red-400" : "text-red-500"
                      : isDark ? "text-blue-400" : "text-blue-500"
                  }
                />
              </div>
              
              <h2 className="text-xl font-bold">
                {sessionExpired === 'manual' ? 'Logging Out' : 'Session Expired'}
              </h2>
              
              <p className={isDark ? "text-gray-400" : "text-gray-600"}>
                {sessionExpired === "manual"
                  ? "You are being logged out..."
                  : "Your session has expired due to inactivity. Redirecting to login..."}
              </p>

              {sessionExpired === 'manual' && (
                <div className={`rounded-lg p-3 ${
                  isDark ? "bg-red-500/10 border-red-500/20" : "bg-red-50 border-red-200"
                } border`}>
                  <p className={`text-sm ${isDark ? "text-red-400" : "text-red-600"}`}>
                    <span className="font-semibold">Security Note:</span> This will end your current session and clear all temporary data.
                  </p>
                </div>
              )}

              <div className="flex justify-center pt-4">
                <div className={`w-8 h-8 border-2 rounded-full animate-spin ${
                  sessionExpired === 'manual'
                    ? isDark ? 'border-red-400 border-t-transparent' : 'border-red-500 border-t-transparent'
                    : isDark ? 'border-blue-400 border-t-transparent' : 'border-blue-500 border-t-transparent'
                }`}></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

  return (
    <div className={`min-h-screen ${isDark ? 'bg-gray-950' : 'bg-gray-50'} transition-colors`}>
      
      {/* Session Expired Overlay */}
      <SessionExpiredOverlay />
      
      {/* Logout Confirmation Modal */}
      <LogoutConfirmationModal />

      {/* Top Navigation - Modern Design */}
      <div className={`sticky top-0 z-40 ${isDark ? 'bg-gray-900/80 backdrop-blur-xl border-gray-800' : 'bg-white/80 backdrop-blur-xl border-gray-300'} border-b`}>
        <div className="px-4 sm:px-6 py-3">
          <div className="flex items-center justify-between">
            {/* Left */}
            <div className="flex items-center gap-4">
              <button
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                className={`p-2 rounded-xl lg:hidden transition-colors ${
                  isDark ? 'hover:bg-gray-800' : 'hover:bg-gray-100'
                }`}
              >
                <Menu className={`h-5 w-5 ${isDark ? 'text-gray-300' : 'text-gray-700'}`} />
              </button>
              <div className="hidden lg:flex items-center gap-3">
                <div className={`p-2 rounded-xl ${isDark ? 'bg-orange-500/20' : 'bg-orange-500/10'}`}>
                  <DatabaseZap className={`h-6 w-6 ${isDark ? 'text-orange-400' : 'text-orange-600'}`} />
                </div>
                <div>
                  <h1 className={`text-lg font-bold ${isDark ? 'text-white' : 'text-gray-900'}`}>
                    API Automation Platform
                  </h1>
                  <p className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-600'}`}>
                    Oracle Schema to API Endpoint
                  </p>
                </div>
              </div>
            </div>

            {/* Center Navigation - Desktop */}
            <div className="hidden lg:flex items-center gap-1">
              {navItems.map(item => {
                const Icon = item.icon;
                const isActive = activeTab === item.id;
                return (
                  <button
                    key={item.id}
                    onClick={() => setActiveTab(item.id)}
                    className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm transition-all ${
                      isActive
                        ? isDark
                          ? 'bg-orange-500/20 text-orange-400'
                          : 'bg-orange-50 text-orange-600'
                        : isDark
                          ? 'text-gray-400 hover:text-gray-300 hover:bg-gray-800'
                          : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                    }`}
                  >
                    <Icon className="h-4 w-4" />
                    <span>{item.label}</span>
                  </button>
                );
              })}
            </div>

            {/* Right */}
            <div className="flex items-center gap-2">
              {/* Theme toggle */}
              <button
                onClick={toggle}
                className={`p-2 rounded-xl transition-colors ${
                  isDark ? 'hover:bg-gray-800' : 'hover:bg-gray-100'
                }`}
                title={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
              >
                {isDark ? (
                  <Sun className={`h-5 w-5 ${isDark ? 'text-gray-300' : 'text-gray-700'}`} />
                ) : (
                  <Moon className={`h-5 w-5 ${isDark ? 'text-gray-300' : 'text-gray-700'}`} />
                )}
              </button>
              
              <div className="h-6 w-px mx-2 bg-gray-300 dark:bg-gray-700"></div>
              
              {/* UPDATED USER SECTION WITH DROPDOWN */}
              <div className="relative user-menu-container">
                <button
                  onClick={() => setUserMenuOpen(!userMenuOpen)}
                  className="flex items-center gap-3 p-1 rounded-xl hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                >
                  <div className={`w-9 h-9 rounded-xl ${isDark ? 'bg-gray-800' : 'bg-gray-100'} flex items-center justify-center`}>
                    <UserCheck className={`h-5 w-5 ${isDark ? 'text-gray-300' : 'text-gray-600'}`} />
                  </div>
                  <div className="hidden sm:block text-left">
                    <p className={`text-sm font-medium ${isDark ? 'text-white' : 'text-gray-900'}`}>
                      {user?.name || "Admin User"}
                    </p>
                    <p className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-600'}`}>
                      {user?.email || "admin@example.com"}
                    </p>
                  </div>
                  <ChevronDown className={`h-4 w-4 transition-transform ${userMenuOpen ? 'rotate-180' : ''} ${isDark ? 'text-gray-400' : 'text-gray-500'}`} />
                </button>

                {/* User Dropdown Menu */}
                {userMenuOpen && (
                  <>
                    <div 
                      className="fixed inset-0 z-50" 
                      onClick={() => setUserMenuOpen(false)}
                    />
                    <div className={`absolute right-0 top-full mt-2 w-64 rounded-xl shadow-lg z-60 ${
                      isDark ? 'bg-gray-900 border border-gray-800' : 'bg-white border border-gray-200'
                    }`}>
                      {/* User Info Section */}
                      <div className={`p-4 border-b ${isDark ? 'border-gray-800' : 'border-gray-100'}`}>
                        <div className="flex items-center gap-3 mb-3">
                          {/* <div className={`w-12 h-12 rounded-full ${isDark ? 'bg-gray-800' : 'bg-gray-100'} flex items-center justify-center`}>
                            <UserCheck className={`h-6 w-6 ${isDark ? 'text-gray-300' : 'text-gray-600'}`} />
                          </div> */}
                          <div>
                            <h3 className={`font-semibold ${isDark ? 'text-white' : 'text-gray-900'}`}>
                              {user?.name || "Admin User"}
                            </h3>
                            <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-600'}`}>
                              {user?.email || "admin@example.com"}
                            </p>
                          </div>
                        </div>
                        {/* Status can be added here if needed */}
                      </div>

                      {/* Actions Section */}
                      <div className="p-2">
                        <button
                          onClick={() => {
                            console.log('View Profile clicked');
                            setUserMenuOpen(false);
                          }}
                          className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors ${
                            isDark 
                              ? 'hover:bg-gray-800 text-gray-300' 
                              : 'hover:bg-gray-100 text-gray-700'
                          }`}
                        >
                          <UserCircle className="h-4 w-4" />
                          <span>View Profile</span>
                        </button>
                        
                        <button
                          onClick={() => {
                            console.log('Account Settings clicked');
                            setUserMenuOpen(false);
                          }}
                          className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors ${
                            isDark 
                              ? 'hover:bg-gray-800 text-gray-300' 
                              : 'hover:bg-gray-100 text-gray-700'
                          }`}
                        >
                          <Settings className="h-4 w-4" />
                          <span>Account Settings</span>
                        </button>

                        <div className={`h-px my-1 ${isDark ? 'bg-gray-800' : 'bg-gray-200'}`} />

                        <button
                          onClick={handleManualLogout}
                          className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors ${
                            isDark 
                              ? 'hover:bg-red-900/30 text-red-400' 
                              : 'hover:bg-red-50 text-red-600'
                          }`}
                        >
                          <LogOut className="h-4 w-4" />
                          <span>Logout</span>
                        </button>
                      </div>
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex">
        {/* Main Content Area */}
        <div className="flex-1">
          {getActiveComponent()}
        </div>
      </div>

      {/* Mobile Navigation Menu */}
      <MobileNavigationMenu />

      {/* Modals */}
      <DatabaseConnectionWizard />
      <ApiGenerationWizard />

      {/* Imported Modals */}
      <ConnectionDetailsModal
        showConnectionDetails={showConnectionDetails}
        setShowConnectionDetails={setShowConnectionDetails}
        selectedConnection={selectedConnection}
        handleTestConnection={handleTestConnection}
        isMobile={isMobile}
      />

      <APIDetailsModal
        showAPIDetails={showAPIDetails}
        setShowAPIDetails={setShowAPIDetails}
        selectedAPI={selectedAPI}
        isMobile={isMobile}
      />

      {/* Mobile Bottom Navigation */}
      {isMobile && (
        <div className={`fixed bottom-0 left-0 right-0 ${isDark ? 'bg-gray-900/95 backdrop-blur-xl' : 'bg-white/95 backdrop-blur-xl'} border-t ${isDark ? 'border-gray-800' : 'border-gray-300'} z-30`}>
          <div className="flex items-center justify-around py-2">
            {navItems.slice(0, 4).map(item => {
              const Icon = item.icon;
              const isActive = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => setActiveTab(item.id)}
                  className={`flex flex-col items-center p-2 rounded-xl transition-all ${
                    isActive
                      ? isDark
                        ? 'text-orange-400 bg-orange-500/20'
                        : 'text-orange-600 bg-orange-50'
                      : isDark
                      ? 'text-gray-400 hover:text-gray-300'
                      : 'text-gray-600 hover:text-gray-900'
                  }`}
                >
                  <Icon className="h-5 w-5" />
                  <span className="text-xs mt-1">{item.label}</span>
                </button>
              );
            })}
            <button
              onClick={() => setMobileMenuOpen(true)}
              className={`flex flex-col items-center p-2 rounded-xl transition-colors ${
                isDark ? 'text-gray-400 hover:text-gray-300' : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <Menu className="h-5 w-5" />
              <span className="text-xs mt-1">More</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
}