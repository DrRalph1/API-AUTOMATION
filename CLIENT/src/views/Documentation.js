import React, { useState, useEffect, useRef, useCallback } from 'react';
import { 
  ChevronRight, 
  ChevronDown,
  Search,
  Plus,
  Play,
  MoreVertical,
  Download,
  Share2,
  Eye,
  EyeOff,
  Copy,
  Trash2,
  Edit2,
  Settings,
  Globe,
  Lock,
  FileText,
  Code,
  GitBranch,
  History,
  Zap,
  Filter,
  Folder,
  FolderOpen,
  Star,
  ExternalLink,
  Upload,
  Users,
  Bell,
  HelpCircle,
  User,
  Moon,
  Sun,
  X,
  Menu,
  Check,
  AlertCircle,
  Clock,
  Activity,
  Database,
  Shield,
  Key,
  Hash,
  Bold,
  Loader,
  Italic,
  Link,
  Image,
  Table,
  Terminal,
  BookOpen,
  LayoutDashboard,
  ShieldCheck,
  DownloadCloud,
  UploadCloud,
  UserCheck,
  Home,
  Cloud,
  Save,
  Printer,
  Inbox,
  Archive,
  Trash,
  UserPlus,
  RefreshCw,
  ChevronLeft,
  ChevronUp,
  Minimize2,
  Maximize2,
  MoreHorizontal,
  Send,
  CheckCircle,
  XCircle,
  Info,
  Layers,
  Package,
  Box,
  FolderPlus,
  FilePlus,
  Wifi,
  Server,
  HardDrive,
  Network,
  Cpu,
  BarChart,
  PieChart,
  LineChart,
  Smartphone,
  Monitor,
  Bluetooth,
  Command,
  Circle,
  Dot,
  List,
  Type,
  FileCode,
  ChevronsLeft,
  ChevronsRight,
  GripVertical,
  Coffee,
  Eye as EyeIcon,
  FileArchive as FileBinary,
  Database as DatabaseIcon,
  ChevronsUpDown,
  Book,
  File,
  MessageSquare,
  Tag,
  Calendar,
  Hash as HashIcon,
  Link as LinkIcon,
  Eye as EyeOpenIcon,
  Clock as ClockIcon,
  Users as UsersIcon,
  Database as DatabaseIcon2,
  Code as CodeIcon2,
  Terminal as TerminalIcon,
  ExternalLink as ExternalLinkIcon,
  Copy as CopyIcon,
  Check as CheckIcon,
  X as XIcon,
  AlertCircle as AlertCircleIcon,
  Info as InfoIcon,
  HelpCircle as HelpCircleIcon,
  Star as StarIcon,
  Book as BookIcon,
  Zap as ZapIcon
} from 'lucide-react';

// Import DocumentationController functions
import {
  getAPICollections,
  getAPIEndpoints,
  getFolders,
  getEndpointDetails,
  getCodeExamples,
  searchDocumentation,
  publishDocumentation,
  getDocumentationEnvironments,
  getDocumentationNotifications,
  getChangelog,
  generateMockServer,
  clearDocumentationCache,
  handleDocumentationResponse,
  extractAPICollections,
  extractAPIEndpoints,
  extractEndpointDetails,
  extractCodeExamples,
  extractSearchResults,
  extractPublishResults,
  extractDocumentationEnvironments,
  extractNotifications,
  extractChangelog,
  extractMockServerResults,
  validatePublishDocumentation,
  validateGenerateMockServer,
  validateSearchDocumentation,
  formatDocumentationCollection,
  formatDocumentationEndpoint,
  formatEndpointDetails,
  formatSearchResult,
  getMethodColor,
  getTagColor,
  getCollectionColor,
  getRelevanceColor,
  getStatusCodeBadge,
  formatRateLimit,
  formatJsonExample,
  getTimeAgo,
  formatDateForDisplay,
  getInitials,
  generateDocumentationUrl,
  cacheDocumentationData,
  getCachedDocumentationData,
  clearCachedDocumentationData,
  getSupportedLanguages,
  getSupportedAPITypes,
  getSupportedHTTPMethods,
  getSupportedContentTypes,
  getSupportedVisibilityOptions,
  getSupportedEnvironmentTypes,
  getSupportedDocumentationFormats,
  getCollectionDetailsWithEndpoints,
  extractCollectionDetailsWithEndpoints,
} from "../controllers/DocumentationController.js";

// Also import apiCall for debug purposes
import { apiCall } from "@/helpers/APIHelper.js";

// SyntaxHighlighter Component
const SyntaxHighlighter = ({ language, code }) => {
  if (!code) return <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed">// No code available</pre>;
  
  // Simple syntax highlighting using spans
  const highlightCode = (code, lang) => {
    const lines = String(code).split('\n');
    
    return lines.map((line, lineIndex) => {
      // First escape HTML entities to prevent injection
      let highlightedLine = line
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
      
      if (lang === 'json') {
        // Highlight JSON keys
        highlightedLine = highlightedLine.replace(/"([^"\\]*(\\.[^"\\]*)*)"(\s*:)/g, 
          '<span class="text-blue-400">"$1"</span>$3');
        
        // Highlight string values
        highlightedLine = highlightedLine.replace(/:(\s*)"([^"\\]*(\\.[^"\\]*)*)"/g, 
          ':<span class="text-green-400">"$2"</span>');
        
        // Highlight numbers
        highlightedLine = highlightedLine.replace(/:(\s*)(\d+)([,\n])/g, 
          ':<span class="text-orange-400">$2</span>$3');
        
        // Highlight booleans and null
        highlightedLine = highlightedLine.replace(/\b(true|false|null)\b/g, 
          '<span class="text-purple-400">$1</span>');
      } else if (lang === 'java') {
        // Handle multi-line comment boundaries
        if (highlightedLine.includes('/*') || highlightedLine.includes('*/')) {
          return (
            <div key={lineIndex} className="text-gray-500">
              {highlightedLine}
            </div>
          );
        }
        
        // Highlight strings (double quotes)
        highlightedLine = highlightedLine.replace(/"([^"\\]*(\\.[^"\\]*)*)"/g, 
          '<span class="text-green-400">"$1"</span>');
        
        // Highlight characters (single quotes)
        highlightedLine = highlightedLine.replace(/'([^'\\]*(\\.[^'\\]*)*)'/g, 
          '<span class="text-green-400">\'$1\'</span>');
        
        // Highlight single-line comments
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(\/\/.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        
        // Highlight annotations
        highlightedLine = highlightedLine.replace(/(@\w+)/g, 
          '<span class="text-blue-400">$1</span>');
        
        // Highlight keywords
        const keywords = [
          'public', 'private', 'protected', 'class', 'interface', 
          'extends', 'implements', 'static', 'final', 'void', 
          'return', 'new', 'if', 'else', 'for', 'while', 
          'switch', 'case', 'break', 'continue', 'throw', 
          'throws', 'try', 'catch', 'finally', 'import', 
          'package', 'abstract', 'assert', 'boolean', 'byte',
          'char', 'double', 'enum', 'float', 'int', 'long',
          'short', 'super', 'synchronized', 'this', 'transient',
          'volatile', 'instanceof', 'true', 'false', 'null'
        ];
        
        keywords.forEach(keyword => {
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, 
            '<span class="text-purple-400">$1</span>');
        });
        
        // Highlight numbers
        if (!highlightedLine.includes('class="text-green-400"') && 
            !highlightedLine.includes('class="text-gray-500"')) {
          highlightedLine = highlightedLine.replace(/\b(\d+[lLfFdD]?)\b(?![^<]*>|[^>]*<\/)/g, 
            '<span class="text-blue-400">$1</span>');
        }
        
        // Fix any malformed spans
        highlightedLine = highlightedLine.replace(/class="([^"]*)"([^>]*?)>/g, 'class="$1"$2>');
        
      } else if (lang === 'javascript' || lang === 'nodejs') {
        // JavaScript highlighting
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)"|'([^'\\]*(\\.[^'\\]*)*)')/g, 
          '<span class="text-green-400">$1</span>');
        
        // Comments
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(\/\/.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        
        // Keywords
        const jsKeywords = [
          'function', 'const', 'let', 'var', 'if', 'else', 
          'for', 'while', 'return', 'class', 'import', 
          'export', 'from', 'default', 'async', 'await', 
          'try', 'catch', 'finally', 'throw', 'new', 'this',
          'true', 'false', 'null', 'undefined', 'typeof',
          'instanceof', 'in', 'of', 'yield', 'delete'
        ];
        
        jsKeywords.forEach(keyword => {
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, 
            '<span class="text-purple-400">$1</span>');
        });
      } else if (lang === 'python') {
        // Python highlighting
        highlightedLine = highlightedLine.replace(/("([^"\\]*(\\.[^"\\]*)*)"|'([^'\\]*(\\.[^'\\]*)*)')/g, 
          '<span class="text-green-400">$1</span>');
        
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(#.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        
        const pythonKeywords = [
          'def', 'class', 'import', 'from', 'if', 'elif', 
          'else', 'for', 'while', 'try', 'except', 'finally', 
          'with', 'as', 'return', 'yield', 'async', 'await', 
          'lambda', 'in', 'is', 'not', 'and', 'or', 'True', 
          'False', 'None', 'pass', 'break', 'continue', 'raise'
        ];
        
        pythonKeywords.forEach(keyword => {
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, 
            '<span class="text-purple-400">$1</span>');
        });
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

// Loading Overlay Component
const LoadingOverlay = ({ isLoading, colors, loadingText }) => {
  if (!isLoading) return null;
  
  return (
    <div className="fixed inset-0 flex items-center justify-center z-50" style={{ backgroundColor: colors.bg }}>
      <div className="text-center">
        <div className="relative">
          <Loader className="animate-spin mx-auto mb-6" size={64} style={{ color: colors.primary }} />
          <div className="absolute inset-0 flex items-center justify-center">
            <BookOpen size={32} style={{ color: colors.primary, opacity: 0.3 }} />
          </div>
        </div>
        <h3 className="text-xl font-semibold mb-2" style={{ color: colors.text }}>
          Loading Documentation
        </h3>
        <p className="text-sm mb-2" style={{ color: colors.textSecondary }}>
          {loadingText || 'Please wait while we load your documentation data'}
        </p>
      </div>
    </div>
  );
};

// Helper function for alphabetical sorting
const sortAlphabetically = (items, key = 'name') => {
  if (!items || !Array.isArray(items)) return [];
  return [...items].sort((a, b) => {
    const nameA = (a[key] || '').toLowerCase();
    const nameB = (b[key] || '').toLowerCase();
    if (nameA < nameB) return -1;
    if (nameA > nameB) return 1;
    return 0;
  });
};

// Enhanced Parameter Table Component
const ParameterTable = ({ parameters, colors, onCopy }) => {
  if (!parameters || parameters.length === 0) return null;
  
  return (
    <div className="border rounded overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
      <table className="w-full">
        <thead style={{ backgroundColor: colors.tableHeader }}>
          <tr>
            <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Name</th>
            <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Type</th>
            <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Required</th>
            <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Description</th>
            <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Validation</th>
          </tr>
        </thead>
        <tbody>
          {parameters.map((param, index) => (
            <tr key={param.id || index} className="border-b last:border-b-0" style={{ 
              borderColor: colors.borderLight,
              backgroundColor: colors.tableRow
            }}>
              <td className="px-4 py-3 font-medium" style={{ color: colors.text }}>
                <code>{param.name}</code>
                {param.deprecated && (
                  <span className="ml-2 text-xs px-1.5 py-0.5 rounded" style={{ 
                    backgroundColor: `${colors.warning}20`,
                    color: colors.warning
                  }}>
                    deprecated
                  </span>
                )}
              </td>
              <td className="px-4 py-3">
                <div className="flex flex-col gap-1">
                  <span className="text-sm" style={{ color: colors.textSecondary }}>{param.type}</span>
                  {param.format && (
                    <span className="text-xs" style={{ color: colors.textTertiary }}>
                      format: {param.format}
                    </span>
                  )}
                  {param.enum && (
                    <span className="text-xs" style={{ color: colors.textTertiary }}>
                      enum: {param.enum.join(', ')}
                    </span>
                  )}
                </div>
              </td>
              <td className="px-4 py-3">
                <span className="text-xs px-2 py-1 rounded" style={{ 
                  backgroundColor: param.required ? `${colors.error}20` : `${colors.success}20`,
                  color: param.required ? colors.error : colors.success
                }}>
                  {param.requiredBadge}
                </span>
              </td>
              <td className="px-4 py-3 text-sm" style={{ color: colors.textSecondary }}>
                {param.description}
                {param.example && (
                  <div className="mt-1">
                    <span className="text-xs" style={{ color: colors.textTertiary }}>
                      Example: <code className="text-xs">{param.example}</code>
                    </span>
                    <button 
                      onClick={() => onCopy(param.example)}
                      className="ml-2 p-1 rounded hover:bg-opacity-50 transition-colors"
                      style={{ backgroundColor: colors.hover }}
                    >
                      <Copy size={10} style={{ color: colors.textSecondary }} />
                    </button>
                  </div>
                )}
                {param.defaultValue && (
                  <span className="block mt-1 text-xs" style={{ color: colors.textTertiary }}>
                    Default: <code>{param.defaultValue}</code>
                  </span>
                )}
              </td>
              <td className="px-4 py-3 text-xs" style={{ color: colors.textTertiary }}>
                {param.pattern && (
                  <div className="mb-1">
                    <span>Pattern: </span>
                    <code className="text-xs">{param.pattern}</code>
                  </div>
                )}
                {param.minLength && (
                  <div>Min length: {param.minLength}</div>
                )}
                {param.maxLength && (
                  <div>Max length: {param.maxLength}</div>
                )}
                {param.minimum !== null && (
                  <div>Min: {param.minimum}</div>
                )}
                {param.maximum !== null && (
                  <div>Max: {param.maximum}</div>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

// Main component
const Documentation = ({ theme, isDark, customTheme, toggleTheme, authToken }) => {
  const [activeTab, setActiveTab] = useState('documentation');
  const [showCodePanel, setShowCodePanel] = useState(true);
  const [selectedLanguage, setSelectedLanguage] = useState('curl');
  const [showLanguageDropdown, setShowLanguageDropdown] = useState(false);
  const [toast, setToast] = useState(null);
  const [showPublishModal, setShowPublishModal] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [globalSearchQuery, setGlobalSearchQuery] = useState('');
  const [selectedCollection, setSelectedCollection] = useState(null);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [environments, setEnvironments] = useState([]);
  const [activeEnvironment, setActiveEnvironment] = useState('');
  const [publishUrl, setPublishUrl] = useState('');
  const [isGeneratingDocs, setIsGeneratingDocs] = useState(false);
  const [collections, setCollections] = useState([]);
  const [expandedCollections, setExpandedCollections] = useState([]);
  const [expandedFolders, setExpandedFolders] = useState([]);
  const [activeMainTab, setActiveMainTab] = useState('Collections');
  const [showImportModal, setShowImportModal] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [showWorkspaceSwitcher, setShowWorkspaceSwitcher] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showEnvironmentMenu, setShowEnvironmentMenu] = useState(false);
  
  const [folderEndpoints, setFolderEndpoints] = useState({});
  const [folderLoading, setFolderLoading] = useState({});

  const [isLoading, setIsLoading] = useState({
    collections: false,
    environments: false,
    notifications: false,
    endpointDetails: false,
    endpoints: false,
    folders: false,
    initialLoad: true
  });
  
  const [endpointDetails, setEndpointDetails] = useState(null);
  const [codeExamples, setCodeExamples] = useState({});
  const [changelog, setChangelog] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [userId, setUserId] = useState('');
  
  const isFirstLoad = useRef(true);
  const autoExpandTriggered = useRef(false);
  const selectedCollectionRef = useRef(null);
  const globalLoadingRef = useRef(false);
  const [globalLoading, setGlobalLoading] = useState(false);

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

  // ==================== API METHODS ====================

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
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  useEffect(() => {
    selectedCollectionRef.current = selectedCollection;
  }, [selectedCollection]);

  const withGlobalLoading = async (asyncFn) => {
    if (globalLoadingRef.current) return;
    
    try {
      globalLoadingRef.current = true;
      setGlobalLoading(true);
      await asyncFn();
    } finally {
      globalLoadingRef.current = false;
      setGlobalLoading(false);
    }
  };

  // Fetch API collections
  const fetchAPICollections = useCallback(async () => {
    console.log('🔥 [Documentation] fetchAPICollections called');
    
    if (!authToken) {
      console.log('❌ No auth token available');
      showToast('Authentication required. Please login.', 'error');
      setIsLoading(prev => ({ ...prev, collections: false, initialLoad: false }));
      return;
    }

    setIsLoading(prev => ({ ...prev, collections: true }));
    console.log('📡 [Documentation] Fetching API collections...');

    try {
      const response = await getAPICollections(authToken);
      console.log('📦 [Documentation] API response:', response);
      
      if (!response) {
        throw new Error('No response from documentation service');
      }
      
      const handledResponse = handleDocumentationResponse(response);
      const collectionsData = extractAPICollections(handledResponse);
      
      console.log('📊 [Documentation] Extracted collections data:', collectionsData.length, 'collections');

      const formattedCollections = collectionsData.map(collection => {
        const formatted = formatDocumentationCollection(collection);
        formatted.folders = [];
        return formatted;
      });
      
      const sortedCollections = sortAlphabetically(formattedCollections, 'name');
      setCollections(sortedCollections);
      console.log('📊 [Documentation] Sorted collections:', sortedCollections);
      
      const userId = extractUserIdFromToken(authToken);
      if (userId) {
        cacheDocumentationData(userId, 'collections', sortedCollections);
      }
      
      await loadAllCollectionDetails(sortedCollections);
      
      showToast('Collections loaded successfully', 'success');
      
    } catch (error) {
      console.error('❌ [Documentation] Error fetching API collections:', error);
      showToast(`Failed to load collections: ${error.message}`, 'error');
      setCollections([]);
    } finally {
      setIsLoading(prev => ({ ...prev, collections: false, initialLoad: false }));
      console.log('🏁 [Documentation] fetchAPICollections completed');
    }
  }, [authToken]);

  // Load all collection details
  const loadAllCollectionDetails = useCallback(async (basicCollections) => {
    console.log('📡 [Documentation] Loading details for all collections...');
    
    try {
      setIsLoading(prev => ({ ...prev, folders: true }));
      
      const collectionsWithDetails = await Promise.all(
        basicCollections.map(async (collection) => {
          try {
            const response = await getCollectionDetailsWithEndpoints(authToken, collection.id);
            const handledResponse = handleDocumentationResponse(response);
            const collectionDetails = extractCollectionDetailsWithEndpoints(handledResponse);
            
            if (collectionDetails) {
              let processedFolders = (collectionDetails.folders || []).map(folder => ({
                id: folder.id,
                name: folder.name,
                description: folder.description || '',
                collectionId: folder.collectionId || collection.id,
                requests: folder.endpoints || [],
                requestCount: folder.endpointCount || (folder.endpoints ? folder.endpoints.length : 0),
                hasRequests: (folder.endpointCount || (folder.endpoints ? folder.endpoints.length : 0)) > 0,
                isLoading: false,
                error: null
              }));
              
              processedFolders = sortAlphabetically(processedFolders, 'name');
              
              processedFolders = processedFolders.map(folder => ({
                ...folder,
                requests: sortAlphabetically(folder.requests || [], 'name')
              }));
              
              const folderEndpointsMap = {};
              processedFolders.forEach(folder => {
                if (folder.requests.length > 0) {
                  folderEndpointsMap[folder.id] = folder.requests;
                }
              });
              
              if (Object.keys(folderEndpointsMap).length > 0) {
                setFolderEndpoints(prev => ({
                  ...prev,
                  ...folderEndpointsMap
                }));
              }
              
              return {
                ...collection,
                folders: processedFolders,
                totalFolders: collectionDetails.totalFolders || processedFolders.length,
                totalEndpoints: collectionDetails.totalEndpoints || 
                  processedFolders.reduce((sum, f) => sum + f.requests.length, 0)
              };
            }
            
            return collection;
            
          } catch (error) {
            console.error(`❌ Error loading details for collection ${collection.id}:`, error);
            return collection;
          }
        })
      );
      
      const sortedCollections = sortAlphabetically(collectionsWithDetails, 'name');
      console.log('📊 [Documentation] Collections with details (sorted):', sortedCollections);
      setCollections(sortedCollections);
      
      if (sortedCollections.length > 0) {
        const firstCollection = sortedCollections[0];
        setSelectedCollection(firstCollection);
        setExpandedCollections([firstCollection.id]);
        
        const firstFolderWithRequests = firstCollection.folders?.find(f => f.requests && f.requests.length > 0);
        if (firstFolderWithRequests) {
          setExpandedFolders([firstFolderWithRequests.id]);
          
          if (firstFolderWithRequests.requests.length > 0 && !selectedRequest) {
            const firstEndpoint = firstFolderWithRequests.requests[0];
            console.log(`🎯 [Documentation] Auto-selecting first endpoint: ${firstEndpoint.name}`);
            setSelectedRequest(firstEndpoint);
            await fetchEndpointDetails(firstCollection.id, firstEndpoint.id);
            showToast(`Viewing documentation for ${firstEndpoint.name}`, 'info');
          }
        } else if (firstCollection.folders && firstCollection.folders.length > 0) {
          setExpandedFolders([firstCollection.folders[0].id]);
        }
      }
      
    } catch (error) {
      console.error('❌ Error loading collection details:', error);
    } finally {
      setIsLoading(prev => ({ ...prev, folders: false }));
    }
  }, [authToken, selectedRequest]);

  // Fetch endpoint details - UPDATED WITH IMPROVED BODY DETECTION
  // Updated fetchEndpointDetails function with proper body detection
const fetchEndpointDetails = useCallback(async (collectionId, endpointId) => {
  console.log(`📡 [Documentation] Fetching details for endpoint ${endpointId}`);
  
  if (!authToken || !collectionId || !endpointId) {
    console.log('Missing params for fetchEndpointDetails');
    return;
  }

  try {
    setIsLoading(prev => ({ ...prev, endpointDetails: true }));
    const response = await getEndpointDetails(authToken, collectionId, endpointId);
    console.log('📦 [Documentation] Raw endpoint details response:', response);
    
    const handledResponse = handleDocumentationResponse(response);
    console.log('🔄 [Documentation] Handled response:', handledResponse);
    
    let endpointData = null;
    
    if (handledResponse && handledResponse.data) {
      endpointData = handledResponse.data;
    } else if (handledResponse && handledResponse.endpoint) {
      endpointData = handledResponse.endpoint;
    } else if (handledResponse && typeof handledResponse === 'object') {
      endpointData = handledResponse;
    }
    
    console.log('📊 [Documentation] Extracted endpoint data:', endpointData);
    
    if (endpointData) {
      const pathParams = [];
      const queryParams = [];
      const headerParams = [];
      const bodyParams = [];
      const allHeaders = [];
      
      // Track if we have any body parameters
      let hasBodyParams = false;
      let requestBodyType = 'none';
      let requestBodySchema = null;
      let requestBodyExample = null;
      
      // Check for requestBody in the response
      if (endpointData.requestBody) {
        console.log('📦 Found requestBody in endpoint data:', endpointData.requestBody);
        hasBodyParams = true;
        
        if (typeof endpointData.requestBody === 'object') {
          if (endpointData.requestBody.content) {
            const contentTypes = Object.keys(endpointData.requestBody.content);
            if (contentTypes.includes('application/json')) {
              requestBodyType = 'json';
              requestBodySchema = endpointData.requestBody.content['application/json'].schema;
              requestBodyExample = endpointData.requestBody.content['application/json'].example;
            } else if (contentTypes.includes('multipart/form-data')) {
              requestBodyType = 'form-data';
              requestBodySchema = endpointData.requestBody.content['multipart/form-data'].schema;
              requestBodyExample = endpointData.requestBody.content['multipart/form-data'].example;
            } else if (contentTypes.includes('application/x-www-form-urlencoded')) {
              requestBodyType = 'urlencoded';
              requestBodySchema = endpointData.requestBody.content['application/x-www-form-urlencoded'].schema;
              requestBodyExample = endpointData.requestBody.content['application/x-www-form-urlencoded'].example;
            }
          } else if (endpointData.requestBody.type) {
            requestBodyType = endpointData.requestBody.type;
            requestBodySchema = endpointData.requestBody.schema;
            requestBodyExample = endpointData.requestBody.example;
          } else if (endpointData.requestBody.schema) {
            requestBodyType = 'json';
            requestBodySchema = endpointData.requestBody.schema;
            requestBodyExample = endpointData.requestBody.example;
          } else if (endpointData.requestBody.example) {
            requestBodyType = 'json';
            requestBodyExample = endpointData.requestBody.example;
          }
        }
      }
      
      // Check for consumes property
      if (endpointData.consumes && Array.isArray(endpointData.consumes) && endpointData.consumes.length > 0) {
        hasBodyParams = true;
        if (endpointData.consumes.includes('application/json')) requestBodyType = 'json';
        else if (endpointData.consumes.includes('multipart/form-data')) requestBodyType = 'form-data';
        else if (endpointData.consumes.includes('application/x-www-form-urlencoded')) requestBodyType = 'urlencoded';
      }
      
      // Process all parameters
      if (endpointData.parameters && Array.isArray(endpointData.parameters)) {
        console.log(`📊 Processing ${endpointData.parameters.length} parameters`);
        
        endpointData.parameters.forEach((param, idx) => {
          // Check all possible location indicators
          const location = (param.parameterLocation || param.in || '').toLowerCase();
          const isBodyParam = param.bodyParameter === true || 
                             param.inBody === true ||
                             location === 'body' ||
                             location === 'formdata' ||
                             location === 'form-data';
          
          console.log(`Param ${idx}: ${param.name} - location: ${location}, isBodyParam: ${isBodyParam}`);
          
          const formattedParam = {
            ...param,
            id: param.id || param.name,
            name: param.name || param.key || '',
            key: param.key || param.name || '',
            type: param.type || param.parameterType || param.apiType || 'string',
            required: param.required || false,
            requiredBadge: param.required ? 'Required' : 'Optional',
            description: param.description || '',
            defaultValue: param.defaultValue || '',
            example: param.example || '',
            format: param.format || null,
            validationPattern: param.validationPattern || '',
            position: param.position || idx,
            in: location,
            encoding: param.encoding || null,
            contentType: param.contentType || null,
            minLength: param.minLength || null,
            maxLength: param.maxLength || null,
            minimum: param.minimum || null,
            maximum: param.maximum || null,
            pattern: param.pattern || null,
            enum: param.enum || null,
            deprecated: param.deprecated || false
          };
          
          // Categorize based on location
          if (isBodyParam) {
            bodyParams.push(formattedParam);
            hasBodyParams = true;
            console.log(`✓ Added to bodyParams: ${param.name}`);
          } else if (location === 'path') {
            pathParams.push(formattedParam);
          } else if (location === 'query') {
            queryParams.push(formattedParam);
          } else if (location === 'header') {
            headerParams.push(formattedParam);
            allHeaders.push({
              key: formattedParam.key,
              value: formattedParam.example || formattedParam.defaultValue || '',
              description: formattedParam.description,
              required: formattedParam.required,
              type: formattedParam.type,
              source: 'parameter'
            });
          } else {
            // Default to query if location is not recognized
            queryParams.push(formattedParam);
          }
        });
      }
      
      // If we have body parameters but no body type set, default to JSON
      if (hasBodyParams && requestBodyType === 'none') {
        requestBodyType = 'json';
        console.log('📊 Setting body type to JSON because body parameters exist');
      }
      
      // Handle request body parameters from requestBody schema
      if (requestBodySchema && requestBodySchema.properties) {
        console.log('📊 Processing request body schema properties:', requestBodySchema.properties);
        const bodyParamsFromSchema = Object.entries(requestBodySchema.properties).map(([name, prop]) => ({
          id: name,
          name: name,
          key: name,
          type: prop.type || 'string',
          required: requestBodySchema.required?.includes(name) || false,
          requiredBadge: requestBodySchema.required?.includes(name) ? 'Required' : 'Optional',
          description: prop.description || '',
          defaultValue: prop.default || '',
          example: prop.example || prop.default || '',
          format: prop.format || null,
          in: 'body',
          enum: prop.enum || null,
          minimum: prop.minimum || null,
          maximum: prop.maximum || null,
          pattern: prop.pattern || null
        }));
        bodyParams.push(...bodyParamsFromSchema);
        hasBodyParams = true;
        console.log('📊 Added body params from schema:', bodyParamsFromSchema.length);
      }
      
      // Add headers from endpoint data
      if (endpointData.headers && Array.isArray(endpointData.headers)) {
        endpointData.headers.forEach(header => {
          allHeaders.push({
            key: header.key,
            value: header.value || '',
            description: header.description || '',
            required: header.required || false,
            type: header.type || 'string',
            source: 'header'
          });
        });
      }
      
      // Process auth config
      const authConfigFromEndpoint = endpointData.authConfig || endpointData.auth || {};
      
      if (authConfigFromEndpoint && Object.keys(authConfigFromEndpoint).length > 0) {
        const authType = authConfigFromEndpoint.type || authConfigFromEndpoint.authType || 'noauth';
        
        if (authType === 'apikey' || authType === 'apiKey') {
          const key = authConfigFromEndpoint.key || 
                    authConfigFromEndpoint.apiKey || 
                    authConfigFromEndpoint.apiKeyHeader || '';
          const value = authConfigFromEndpoint.value || 
                      authConfigFromEndpoint.apiSecret || 
                      authConfigFromEndpoint.apiKeyValue || 
                      authConfigFromEndpoint.secret || '';
          
          if (key && value) {
            allHeaders.push({
              key: key,
              value: value,
              description: 'API Key authentication',
              required: true,
              type: 'string',
              source: 'auth'
            });
          }
          
          if (authConfigFromEndpoint.apiSecretHeader && authConfigFromEndpoint.apiSecretValue) {
            allHeaders.push({
              key: authConfigFromEndpoint.apiSecretHeader,
              value: authConfigFromEndpoint.apiSecretValue,
              description: 'API Secret authentication',
              required: true,
              type: 'string',
              source: 'auth'
            });
          }
        } 
        else if (authType === 'bearer') {
          const token = authConfigFromEndpoint.token || authConfigFromEndpoint.bearerToken || '';
          const tokenType = authConfigFromEndpoint.tokenType || 'Bearer';
          if (token) {
            allHeaders.push({
              key: 'Authorization',
              value: `${tokenType} ${token}`,
              description: 'Bearer token authentication',
              required: true,
              type: 'string',
              source: 'auth'
            });
          }
        }
        else if (authType === 'basic') {
          const username = authConfigFromEndpoint.username || '';
          const password = authConfigFromEndpoint.password || '';
          if (username && password) {
            const credentials = btoa(`${username}:${password}`);
            allHeaders.push({
              key: 'Authorization',
              value: `Basic ${credentials}`,
              description: 'Basic authentication',
              required: true,
              type: 'string',
              source: 'auth'
            });
          }
        }
        else if (authType === 'oauth2') {
          const token = authConfigFromEndpoint.token || '';
          if (token) {
            allHeaders.push({
              key: 'Authorization',
              value: `Bearer ${token}`,
              description: 'OAuth2 token authentication',
              required: true,
              type: 'string',
              source: 'auth'
            });
          }
        }
      }
      
      // Deduplicate headers
      const uniqueHeaders = allHeaders.reduce((acc, current) => {
        const exists = acc.some(h => h.key.toLowerCase() === current.key.toLowerCase());
        if (!exists) {
          acc.push(current);
        }
        return acc;
      }, []);
      
      // Sort parameters by position
      const sortByPosition = (a, b) => (a.position || 0) - (b.position || 0);
      pathParams.sort(sortByPosition);
      queryParams.sort(sortByPosition);
      headerParams.sort(sortByPosition);
      bodyParams.sort(sortByPosition);
      
      // Generate request body example based on type
      let formattedRequestBodyExample = requestBodyExample;
      
      // If no example but we have body params, generate one
      if (!formattedRequestBodyExample && bodyParams.length > 0) {
        console.log('📊 Generating example from body parameters');
        if (requestBodyType === 'json') {
          const jsonExample = {};
          bodyParams.forEach(param => {
            jsonExample[param.name] = param.example || param.defaultValue || 
              (param.type === 'number' ? 0 : param.type === 'boolean' ? false : '');
          });
          formattedRequestBodyExample = JSON.stringify(jsonExample, null, 2);
        } else if (requestBodyType === 'form-data') {
          formattedRequestBodyExample = bodyParams.map(param => 
            `${param.name}: ${param.example || param.defaultValue || ''}`
          ).join('\n');
        } else if (requestBodyType === 'urlencoded') {
          formattedRequestBodyExample = bodyParams.map(param => 
            `${encodeURIComponent(param.name)}=${encodeURIComponent(param.example || param.defaultValue || '')}`
          ).join('&');
        }
      }
      
      // Generate example URL with path parameters
      let exampleUrl = endpointData.url || endpointData.path || '';
      pathParams.forEach(param => {
        const placeholder = `{${param.name}}`;
        const exampleValue = param.example || param.defaultValue || `example_${param.name}`;
        exampleUrl = exampleUrl.replace(placeholder, exampleValue);
      });
      
      // Generate query string example
      let queryStringExample = '';
      if (queryParams.length > 0) {
        const queryParts = queryParams.map(param => {
          const value = param.example || param.defaultValue || `example_${param.name}`;
          return `${encodeURIComponent(param.name)}=${encodeURIComponent(value)}`;
        });
        queryStringExample = '?' + queryParts.join('&');
      }
      
      const formattedDetails = {
        id: endpointData.endpointId || endpointData.id,
        name: endpointData.name || '',
        method: endpointData.method || 'GET',
        url: endpointData.url || '',
        path: endpointData.url || endpointData.path || '',
        exampleUrl: exampleUrl,
        queryStringExample: queryStringExample,
        fullExampleUrl: exampleUrl + queryStringExample,
        description: endpointData.description || '',
        category: endpointData.category || 'general',
        tags: endpointData.tags || [],
        formattedTags: (endpointData.tags || []).map(tag => ({
          name: tag,
          color: getTagColor(tag)
        })),
        lastModified: endpointData.lastModified,
        timeAgo: getTimeAgo(endpointData.lastModified),
        version: endpointData.version || '1.0.0',
        requiresAuthentication: endpointData.requiresAuthentication || false,
        rateLimit: endpointData.rateLimit || null,
        formattedRateLimit: endpointData.formattedRateLimit || 
          (endpointData.rateLimit ? formatRateLimit(endpointData.rateLimit) : 'Not rate limited'),
        deprecated: endpointData.deprecated || false,
        headers: uniqueHeaders,
        pathParameters: pathParams,
        queryParameters: queryParams,
        headerParameters: headerParams,
        bodyParameters: bodyParams,
        parameters: endpointData.parameters || [],
        responseExamples: Array.isArray(endpointData.responseExamples) ? 
          endpointData.responseExamples.map(example => ({
            ...example,
            statusBadge: getStatusCodeBadge(example.statusCode),
            formattedExample: example.example ? formatJsonExample(example.example) : '{}'
          })) : [],
        requestBodyExample: formattedRequestBodyExample,
        requestBodyType: requestBodyType,
        requestBodySchema: requestBodySchema,
        changelog: Array.isArray(endpointData.changelog) ? endpointData.changelog : [],
        rateLimitInfo: endpointData.rateLimitInfo || null
      };
      
      console.log('📊 [Documentation] Final formatted details:', {
        requestBodyType: formattedDetails.requestBodyType,
        bodyParametersCount: formattedDetails.bodyParameters.length,
        hasRequestBodyExample: !!formattedDetails.requestBodyExample,
        pathParametersCount: formattedDetails.pathParameters.length,
        queryParametersCount: formattedDetails.queryParameters.length
      });
      
      setEndpointDetails(formattedDetails);
      
      if (formattedDetails.changelog && formattedDetails.changelog.length > 0) {
        setChangelog(formattedDetails.changelog);
      } else {
        await fetchChangelog(collectionId);
      }
      
      await fetchCodeExamples(endpointId, selectedLanguage);
    }
    
  } catch (error) {
    console.error('❌ [Documentation] Error loading endpoint details:', error);
    showToast(`Failed to load endpoint details: ${error.message}`, 'error');
  } finally {
    setIsLoading(prev => ({ ...prev, endpointDetails: false }));
  }
}, [authToken, selectedLanguage]);

  // Fetch code examples
  const fetchCodeExamples = useCallback(async (endpointId, language) => {
    console.log(`📡 [Documentation] Fetching code examples for endpoint ${endpointId}, language ${language}`);
    
    if (!authToken || !endpointId || !language) {
      console.log('Missing params for fetchCodeExamples');
      return;
    }

    try {
      const response = await getCodeExamples(authToken, endpointId, language);
      const handledResponse = handleDocumentationResponse(response);
      const examples = extractCodeExamples(handledResponse);
      
      if (examples) {
        setCodeExamples(prev => ({
          ...prev,
          [endpointId]: {
            ...prev[endpointId],
            [language]: examples
          }
        }));
      }
      
    } catch (error) {
      console.error('❌ [Documentation] Error loading code examples:', error);
    }
  }, [authToken]);

  // Fetch changelog
  const fetchChangelog = useCallback(async (collectionId) => {
    console.log(`📡 [Documentation] Fetching changelog for collection ${collectionId}`);
    
    if (!authToken || !collectionId) {
      console.log('Missing params for fetchChangelog');
      return;
    }

    try {
      const response = await getChangelog(authToken, collectionId);
      const handledResponse = handleDocumentationResponse(response);
      const changelogData = extractChangelog(handledResponse);
      
      setChangelog(changelogData);
      console.log('📊 [Documentation] Loaded changelog entries:', changelogData.length);
      
    } catch (error) {
      console.error('❌ [Documentation] Error loading changelog:', error);
    }
  }, [authToken]);

  // Fetch environments
  const fetchEnvironments = useCallback(async () => {
    console.log('📡 [Documentation] Fetching environments...');
    
    if (!authToken) {
      console.log('❌ No auth token for fetchEnvironments');
      return;
    }

    try {
      setIsLoading(prev => ({ ...prev, environments: true }));
      const response = await getDocumentationEnvironments(authToken);
      const handledResponse = handleDocumentationResponse(response);
      const envs = extractDocumentationEnvironments(handledResponse);
      
      console.log('📊 [Documentation] Loaded environments:', envs.length);
      
      setEnvironments(envs);
      
      const userId = extractUserIdFromToken(authToken);
      if (userId) {
        cacheDocumentationData(userId, 'environments', envs);
      }
      
      if (envs.length > 0) {
        const activeEnv = envs.find(e => e.isActive) || envs[0];
        setActiveEnvironment(activeEnv.id || '');
      }
      
    } catch (error) {
      console.error('❌ [Documentation] Error loading environments:', error);
      showToast(`Failed to load environments: ${error.message}`, 'error');
    } finally {
      setIsLoading(prev => ({ ...prev, environments: false }));
    }
  }, [authToken]);

  // Fetch notifications
  const fetchNotifications = useCallback(async () => {
    console.log('📡 [Documentation] Fetching notifications...');
    
    if (!authToken) {
      console.log('❌ No auth token for fetchNotifications');
      return;
    }

    try {
      setIsLoading(prev => ({ ...prev, notifications: true }));
      const response = await getDocumentationNotifications(authToken);
      const handledResponse = handleDocumentationResponse(response);
      const notifs = extractNotifications(handledResponse);
      
      console.log('📊 [Documentation] Loaded notifications:', notifs.length);
      
      setNotifications(notifs);
      
      const userId = extractUserIdFromToken(authToken);
      if (userId) {
        cacheDocumentationData(userId, 'notifications', notifs);
      }
      
    } catch (error) {
      console.error('❌ [Documentation] Error loading notifications:', error);
      showToast(`Failed to load notifications: ${error.message}`, 'error');
    } finally {
      setIsLoading(prev => ({ ...prev, notifications: false }));
    }
  }, [authToken]);

  // Toggle collection
  const toggleCollection = (collectionId) => {
    console.log(`📂 [Documentation] Toggling collection ${collectionId}`);
    
    setExpandedCollections(prev =>
      prev.includes(collectionId)
        ? prev.filter(id => id !== collectionId)
        : [...prev, collectionId]
    );
  };

  // Toggle folder
  const toggleFolder = (folderId) => {
    if (isFirstLoad.current) {
      console.log(`⏭️ [Documentation] Initial load in progress, completely skipping toggleFolder for ${folderId}`);
      return;
    }
    
    setExpandedFolders(prev =>
      prev.includes(folderId)
        ? prev.filter(id => id !== folderId)
        : [...prev, folderId]
    );
  };

  // Handle select request
  const handleSelectRequest = async (request, collectionId, folderId) => {
    console.log('🎯 [Documentation] Selecting request:', request.name);
    
    const collection = collections.find(c => c.id === collectionId);
    if (collection) {
      setSelectedCollection(collection);
    }
    
    setSelectedRequest(request);
    showToast(`Viewing documentation for ${request.name}`, 'info');
    
    const endpointId = request.id || request.endpointId;
    await fetchEndpointDetails(collectionId, endpointId);
  };

  // Handle environment change
  const handleEnvironmentChange = (envId) => {
    setActiveEnvironment(envId);
    setEnvironments(envs => envs.map(env => ({
      ...env,
      isActive: env.id === envId
    })));
    showToast(`Switched to ${environments.find(e => e.id === envId)?.name} environment`, 'success');
    setShowEnvironmentMenu(false);
  };

  // Generate code example
  const generateCodeExample = () => {
    if (!selectedRequest) {
      return '// Select an endpoint to view code examples';
    }
    
    const currentExamples = codeExamples[selectedRequest.id]?.[selectedLanguage];
    if (currentExamples?.code) {
      return currentExamples.code;
    }
    
    const baseUrl = environments.find(e => e.id === activeEnvironment)?.baseUrl || '';
    const url = selectedRequest.url || selectedRequest.path || '';
    
    const examples = {
      curl: `curl -X ${selectedRequest.method} "${url}" \\
  -H "Content-Type: application/json" \\
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"`,
      javascript: `fetch('${url}', {
  method: '${selectedRequest.method}',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'
  }
})`,
      python: `import requests

headers = {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'
}

response = requests.${selectedRequest.method.toLowerCase()}('${url}', 
                        headers=headers)`,
      nodejs: `const https = require('https');

const options = {
  hostname: '${baseUrl.replace('https://', '').split('/')[0] || 'api.fintech.com'}',
  path: '${url}',
  method: '${selectedRequest.method}',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'
  }
};

const req = https.request(options, (res) => {
  console.log(res.statusCode);
});

req.end();`
    };
    
    return examples[selectedLanguage] || '// No example available';
  };

  // Copy to clipboard
  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    showToast('Copied to clipboard!', 'success');
  };

  // Get method color
  const getMethodColor = (method) => {
    return colors.method[method] || colors.textSecondary;
  };

  // Filter collections based on search
  const filteredCollections = collections.filter(collection => {
    if (!searchQuery) return true;
    const query = searchQuery.toLowerCase();
    return (
      collection.name?.toLowerCase().includes(query) ||
      collection.description?.toLowerCase().includes(query) ||
      collection.folders?.some(folder => 
        folder.name?.toLowerCase().includes(query) ||
        folder.description?.toLowerCase().includes(query) ||
        folder.requests?.some(request => 
          request.name?.toLowerCase().includes(query) ||
          request.description?.toLowerCase().includes(query)
        )
      )
    );
  });

  // Get endpoints for a folder
  const getFolderEndpoints = (folderId) => {
    return folderEndpoints[folderId] || [];
  };

  // ==================== AUTO-EXPAND ONLY FIRST FOLDER & SELECT FIRST ENDPOINT ====================
  useEffect(() => {
    if (!isFirstLoad.current || autoExpandTriggered.current) {
      return;
    }
    
    if (!selectedCollection) {
      console.log('⏳ [Documentation] Waiting for selectedCollection...');
      return;
    }
    
    if (!selectedCollection.folders || selectedCollection.folders.length === 0) {
      console.log('⏳ [Documentation] Waiting for folders to load...');
      return;
    }
    
    autoExpandTriggered.current = true;
    
    console.log('🔥🔥🔥 [Documentation] AUTO-EXPAND TRIGGERED - FIRST FOLDER ONLY', {
      collection: selectedCollection.name,
      firstFolder: selectedCollection.folders[0]?.name
    });
    
    const autoExpandAndSelect = async () => {
      const firstFolder = selectedCollection.folders[0];
      
      if (!firstFolder) {
        console.log('❌ [Documentation] No first folder found');
        return;
      }
      
      console.log(`📁 [Documentation] Auto-expanding first folder: ${firstFolder.id} - ${firstFolder.name}`);
      
      setExpandedFolders([firstFolder.id]);
      
      if (firstFolder.requests && firstFolder.requests.length > 0 && !selectedRequest) {
        const firstEndpoint = firstFolder.requests[0];
        console.log(`🎯 [Documentation] Auto-selecting first endpoint: ${firstEndpoint.name}`);
        
        setSelectedRequest(firstEndpoint);
        await fetchEndpointDetails(selectedCollection.id, firstEndpoint.id);
        
        showToast(`Viewing documentation for ${firstEndpoint.name}`, 'info');
      }
      
      isFirstLoad.current = false;
      console.log('✅ [Documentation] Auto-expand completed - only first folder expanded');
    };
    
    autoExpandAndSelect();
    
  }, [
    selectedCollection, 
    selectedCollection?.folders,
    selectedCollection?.folders?.length,
    selectedRequest,
    fetchEndpointDetails
  ]);

  // Initialize data
  useEffect(() => {
    console.log('🚀 [Documentation] Component mounted, fetching data...');
    
    const initializeData = async () => {
      setIsLoading(prev => ({ ...prev, initialLoad: true }));
      
      if (authToken) {
        const extractedUserId = extractUserIdFromToken(authToken);
        setUserId(extractedUserId);
        
        clearCachedDocumentationData(extractedUserId);
        
        try {
          await Promise.all([
            fetchAPICollections(),
            fetchEnvironments(),
            fetchNotifications()
          ]);
        } catch (error) {
          console.error('Error during initial data load:', error);
        } finally {
          setIsLoading(prev => ({ ...prev, initialLoad: false }));
        }
      } else {
        console.log('🔒 [Documentation] No auth token, skipping fetch');
        setIsLoading(prev => ({ ...prev, initialLoad: false }));
      }
    };
    
    initializeData();
  }, [authToken, fetchAPICollections, fetchEnvironments, fetchNotifications]);

  // Render code panel
  const renderCodePanel = () => {
    const languages = getSupportedLanguages ? getSupportedLanguages() : [
      { value: 'curl', name: 'cURL', label: 'cURL' },
      { value: 'javascript', name: 'JavaScript', label: 'JavaScript' },
      { value: 'python', name: 'Python', label: 'Python' },
      { value: 'nodejs', name: 'Node.js', label: 'Node.js' },
      { value: 'java', name: 'Java', label: 'Java' }
    ];
    
    const currentLanguage = languages.find(lang => lang.value === selectedLanguage);
    
    return (
      <div className="w-80 border-l flex flex-col" style={{ 
        backgroundColor: colors.card,
        borderColor: colors.border
      }}>
        <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Code Examples</h3>
          <button onClick={() => setShowCodePanel(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}>
            <X size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        <div className="px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <div className="mb-2">
            <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Language</div>
            <div className="relative">
              <button
                onClick={() => setShowLanguageDropdown(!showLanguageDropdown)}
                className="w-full px-3 py-2 rounded text-sm font-medium flex items-center justify-between hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                <div className="flex items-center gap-2">
                  <Terminal size={14} />
                  <span>{currentLanguage?.label || currentLanguage?.name || 'Select Language'}</span>
                </div>
                <ChevronDown size={14} style={{ color: colors.textSecondary }} />
              </button>

              {showLanguageDropdown && (
                <div className="absolute left-0 right-0 top-full mt-1 py-2 rounded shadow-lg z-50 border"
                  style={{ 
                    backgroundColor: colors.bg,
                    borderColor: colors.border
                  }}>
                  {languages.map(lang => (
                    <button
                      key={lang.value}
                      onClick={() => {
                        setSelectedLanguage(lang.value);
                        setShowLanguageDropdown(false);
                        if (selectedRequest) {
                          fetchCodeExamples(selectedRequest.id, lang.value);
                        }
                      }}
                      className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors"
                      style={{ 
                        backgroundColor: selectedLanguage === lang.value ? colors.selected : 'transparent',
                        color: selectedLanguage === lang.value ? colors.primary : colors.text
                      }}
                    >
                      <Terminal size={14} />
                      {lang.label || lang.name}
                      {selectedLanguage === lang.value && <Check size={14} className="ml-auto" />}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="flex-1 overflow-auto">
          <div className="p-4 border-b flex items-center justify-between" style={{ borderColor: colors.border }}>
            <div className="flex items-center gap-2">
              <FileCode size={12} style={{ color: colors.textSecondary }} />
              <span className="text-sm font-medium" style={{ color: colors.text }}>
                Example
              </span>
            </div>
            <button 
              onClick={() => {
                const example = generateCodeExample();
                copyToClipboard(example);
              }}
              className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors flex items-center gap-1 hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}
            >
              <Copy size={10} />
              Copy
            </button>
          </div>
          
          <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
            {isLoading.endpointDetails ? (
              <div className="text-center py-8">
                <RefreshCw size={16} className="animate-spin mx-auto mb-2" style={{ color: colors.textSecondary }} />
                <p className="text-sm" style={{ color: colors.textSecondary }}>Loading code examples...</p>
              </div>
            ) : (
              <SyntaxHighlighter 
                language={selectedLanguage === 'curl' ? 'bash' : selectedLanguage}
                code={generateCodeExample()}
              />
            )}
          </div>
        </div>

        <div className="p-4 border-t" style={{ borderColor: colors.border }}>
          <button 
            className="w-full py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center justify-center gap-2 hover-lift"
            onClick={() => {
              const example = generateCodeExample();
              copyToClipboard(example);
            }}
            style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
            <Copy size={12} />
            Copy Code
          </button>
        </div>
      </div>
    );
  };

  // Render documentation content - UPDATED WITH PROPER JSON DISPLAY
  const renderDocumentationContent = () => {
    if (!selectedRequest || !endpointDetails) {
      return (
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center" style={{ color: colors.textSecondary }}>
            <BookOpen size={48} className="mx-auto mb-4 opacity-50" />
            <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>Select an API Endpoint</h3>
            <p>Choose an endpoint from the left sidebar to view its documentation</p>
          </div>
        </div>
      );
    }
    
    return (
      <div className="flex-1 overflow-auto p-8">
        <div className="max-w-6xl mx-auto">
          {/* Header with Example URL */}
          <div className="mb-8">
            <h1 className="text-2xl font-semibold mb-4" style={{ color: colors.text }}>
              {selectedRequest.name}
            </h1>
            <div className="flex items-center gap-3 mb-2">
              <div className="px-3 py-1 rounded text-sm font-medium" style={{ 
                backgroundColor: getMethodColor(selectedRequest.method),
                color: 'white'
              }}>
                {selectedRequest.method}
              </div>
              <code className="text-lg font-mono" style={{ color: colors.text }}>
                {selectedRequest.path || selectedRequest.url || ''}
              </code>
            </div>
            
            {/* Example URL Section */}
            {/* {(endpointDetails.exampleUrl || endpointDetails.queryStringExample) && (
              <div className="mt-4 p-4 rounded-lg border" style={{ 
                backgroundColor: colors.codeBg,
                borderColor: colors.border
              }}>
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <LinkIcon size={14} style={{ color: colors.textSecondary }} />
                    <span className="text-sm font-medium" style={{ color: colors.text }}>Example Request URL</span>
                  </div>
                  <button 
                    onClick={() => copyToClipboard(endpointDetails.fullExampleUrl)}
                    className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                    style={{ backgroundColor: colors.hover }}
                  >
                    <Copy size={12} style={{ color: colors.textSecondary }} />
                  </button>
                </div>
                <code className="text-sm font-mono break-all" style={{ color: colors.primary }}>
                  {endpointDetails.fullExampleUrl}
                </code>
              </div>
            )} */}
            
            <div className="flex flex-wrap items-center gap-4 text-sm mb-4 mt-4">
              <div style={{ color: colors.textTertiary }}>
                <Folder size={12} className="inline mr-1" style={{ color: colors.textTertiary }} />
                {selectedCollection?.name} › {selectedRequest.category || 'API'}
              </div>
              <div className="flex items-center gap-2">
                {endpointDetails.formattedTags?.map((tag, index) => (
                  <span key={index} className="text-xs px-2 py-1 rounded" style={{ 
                    backgroundColor: `${tag.color}20`,
                    color: tag.color
                  }}>
                    {tag.name}
                  </span>
                ))}
              </div>
              <div style={{ color: colors.textTertiary }}>
                <Clock size={12} className="inline mr-1" style={{ color: colors.textTertiary }} />
                Last updated: {endpointDetails.timeAgo || 'Unknown'}
              </div>
              {endpointDetails.requiresAuthentication && (
                <div className="flex items-center gap-1 text-xs px-2 py-1 rounded" style={{ 
                  backgroundColor: `${colors.warning}20`,
                  color: colors.warning
                }}>
                  <Lock size={10} />
                  Requires Auth
                </div>
              )}
              {endpointDetails.deprecated && (
                <div className="flex items-center gap-1 text-xs px-2 py-1 rounded" style={{ 
                  backgroundColor: `${colors.error}20`,
                  color: colors.error
                }}>
                  <AlertCircle size={10} />
                  Deprecated
                </div>
              )}
            </div>
          </div>

          {/* Request Details */}
          <div className="space-y-8">
            <section>
              <h2 className="text-xl font-semibold mb-6 pb-2 border-b" style={{ 
                color: colors.text,
                borderColor: colors.border
              }}>
                Request Details
              </h2>
              
              {/* Headers Section */}
              {endpointDetails.headers && endpointDetails.headers.length > 0 && (
                <div className="mb-8 p-6 rounded-xl border" style={{ 
                  backgroundColor: colors.card,
                  borderColor: colors.border
                }}>
                  <h3 className="text-lg font-medium mb-4" style={{ color: colors.text }}>
                    Headers
                    <span className="ml-2 text-xs px-2 py-1 rounded" style={{ 
                      backgroundColor: colors.primaryDark,
                      color: 'white'
                    }}>
                      {endpointDetails.headers.length}
                    </span>
                  </h3>
                  <div className="border rounded overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
                    <table className="w-full">
                      <thead style={{ backgroundColor: colors.tableHeader }}>
                        <tr>
                          <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Key</th>
                          <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Value</th>
                          <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Source</th>
                          <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Description</th>
                        </tr>
                      </thead>
                      <tbody>
                        {endpointDetails.headers.map((header, index) => (
                          <tr key={index} className="border-b last:border-b-0" style={{ 
                            borderColor: colors.borderLight,
                            backgroundColor: colors.tableRow
                          }}>
                            <td className="px-4 py-3 font-medium" style={{ color: colors.text }}>
                              <code>{header.key}</code>
                              {header.required && (
                                <span className="ml-2 text-xs px-1.5 py-0.5 rounded" style={{ 
                                  backgroundColor: `${colors.error}20`,
                                  color: colors.error
                                }}>
                                  required
                                </span>
                              )}
                            </td>
                            <td className="px-4 py-3 font-mono text-sm" style={{ color: colors.textSecondary }}>
                              <code className="break-all">{header.value || '(empty)'}</code>
                            </td>
                            <td className="px-4 py-3 text-sm">
                              <span className="text-xs px-2 py-1 rounded" style={{ 
                                backgroundColor: header.source === 'auth' ? `${colors.warning}20` : 
                                              header.source === 'parameter' ? `${colors.info}20` : 
                                              `${colors.success}20`,
                                color: header.source === 'auth' ? colors.warning : 
                                      header.source === 'parameter' ? colors.info : 
                                      colors.success
                              }}>
                                {header.source === 'auth' ? 'Auth' : 
                                 header.source === 'parameter' ? 'Header Param' : 
                                 'Standard'}
                              </span>
                            </td>
                            <td className="px-4 py-3 text-sm" style={{ color: colors.textSecondary }}>
                              {header.description}
                              {header.type && header.type !== 'string' && (
                                <span className="block mt-1 text-xs" style={{ color: colors.textTertiary }}>
                                  Type: {header.type}
                                </span>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* Path Parameters */}
              {endpointDetails.pathParameters && endpointDetails.pathParameters.length > 0 && (
                <div className="mb-8 p-6 rounded-xl border" style={{ 
                  backgroundColor: colors.card,
                  borderColor: colors.border
                }}>
                  <h3 className="text-lg font-medium mb-4" style={{ color: colors.text }}>
                    Path Parameters
                    <span className="ml-2 text-xs px-2 py-1 rounded" style={{ 
                      backgroundColor: colors.primaryDark,
                      color: 'white'
                    }}>
                      {endpointDetails.pathParameters.length}
                    </span>
                  </h3>
                  <ParameterTable 
                    parameters={endpointDetails.pathParameters} 
                    colors={colors}
                    onCopy={copyToClipboard}
                  />
                  
                  {/* Show how parameters appear in URL */}
                  <div className="mt-4 p-3 rounded border" style={{ 
                    backgroundColor: colors.codeBg,
                    borderColor: colors.border
                  }}>
                    <div className="flex items-center gap-2 mb-2">
                      <Info size={12} style={{ color: colors.textSecondary }} />
                      <span className="text-xs font-medium" style={{ color: colors.textSecondary }}>URL Pattern:</span>
                    </div>
                    <code className="text-xs font-mono break-all" style={{ color: colors.text }}>
                      {endpointDetails.path}
                    </code>
                  </div>
                </div>
              )}

              {/* Query Parameters */}
              {endpointDetails.queryParameters && endpointDetails.queryParameters.length > 0 && (
                <div className="mb-8 p-6 rounded-xl border" style={{ 
                  backgroundColor: colors.card,
                  borderColor: colors.border
                }}>
                  <h3 className="text-lg font-medium mb-4" style={{ color: colors.text }}>
                    Query Parameters
                    <span className="ml-2 text-xs px-2 py-1 rounded" style={{ 
                      backgroundColor: colors.primaryDark,
                      color: 'white'
                    }}>
                      {endpointDetails.queryParameters.length}
                    </span>
                  </h3>
                  <ParameterTable 
                    parameters={endpointDetails.queryParameters} 
                    colors={colors}
                    onCopy={copyToClipboard}
                  />
                  
                  {/* Example Query String */}
                  {endpointDetails.queryStringExample && (
                    <div className="mt-4 p-3 rounded border" style={{ 
                      backgroundColor: colors.codeBg,
                      borderColor: colors.border
                    }}>
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-2">
                          <LinkIcon size={12} style={{ color: colors.textSecondary }} />
                          <span className="text-xs font-medium" style={{ color: colors.textSecondary }}>Example Query String:</span>
                        </div>
                        <button 
                          onClick={() => copyToClipboard(endpointDetails.queryStringExample)}
                          className="p-1 rounded hover:bg-opacity-50 transition-colors"
                          style={{ backgroundColor: colors.hover }}
                        >
                          <Copy size={10} style={{ color: colors.textSecondary }} />
                        </button>
                      </div>
                      <code className="text-xs font-mono break-all" style={{ color: colors.primary }}>
                        {endpointDetails.queryStringExample}
                      </code>
                    </div>
                  )}
                </div>
              )}

              {/* Body Parameters - UPDATED WITH JSON SYNTAX HIGHLIGHTING */}
              {endpointDetails.bodyParameters && endpointDetails.bodyParameters.length > 0 && (
                <div className="mb-8 p-6 rounded-xl border" style={{ 
                  backgroundColor: colors.card,
                  borderColor: colors.border
                }}>
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-lg font-medium" style={{ color: colors.text }}>
                      Request Body
                      <span className="ml-2 text-xs px-2 py-1 rounded" style={{ 
                        backgroundColor: colors.primaryDark,
                        color: 'white'
                      }}>
                        {endpointDetails.bodyParameters.length}
                      </span>
                    </h3>
                    {endpointDetails.requestBodyType && endpointDetails.requestBodyType !== 'none' && (
                      <div className="flex items-center gap-2">
                        <span className="text-xs px-2 py-1 rounded" style={{
                          backgroundColor: `${colors.info}20`,
                          color: colors.info
                        }}>
                          {endpointDetails.requestBodyType === 'json' && 'application/json'}
                          {endpointDetails.requestBodyType === 'form-data' && 'multipart/form-data'}
                          {endpointDetails.requestBodyType === 'urlencoded' && 'application/x-www-form-urlencoded'}
                          {endpointDetails.requestBodyType === 'text' && 'text/plain'}
                          {endpointDetails.requestBodyType === 'xml' && 'application/xml'}
                        </span>
                      </div>
                    )}
                  </div>
                  
                  {/* JSON Body - Display with syntax highlighting like response examples */}
                  {endpointDetails.requestBodyType === 'json' && (
                    <div className="space-y-4">
                      <div className="border rounded overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
                        <div className="px-4 py-2 border-b flex items-center justify-between" style={{ 
                          borderColor: colors.border,
                          backgroundColor: colors.tableHeader
                        }}>
                          <span className="text-sm font-medium" style={{ color: colors.text }}>JSON Schema</span>
                          <button 
                            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                            style={{ backgroundColor: colors.hover }}
                            onClick={() => copyToClipboard(JSON.stringify(endpointDetails.bodyParameters, null, 2))}>
                            <Copy size={12} style={{ color: colors.textSecondary }} />
                          </button>
                        </div>
                        <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
                          <ParameterTable 
                            parameters={endpointDetails.bodyParameters} 
                            colors={colors}
                            onCopy={copyToClipboard}
                          />
                        </div>
                      </div>
                      
                      {endpointDetails.requestBodyExample && (
                        <div className="border rounded overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
                          <div className="px-4 py-2 border-b flex items-center justify-between" style={{ 
                            borderColor: colors.border,
                            backgroundColor: colors.tableHeader
                          }}>
                            <span className="text-sm font-medium" style={{ color: colors.text }}>Example Request Body</span>
                            <button 
                              className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                              style={{ backgroundColor: colors.hover }}
                              onClick={() => copyToClipboard(endpointDetails.requestBodyExample)}>
                              <Copy size={12} style={{ color: colors.textSecondary }} />
                            </button>
                          </div>
                          <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
                            <SyntaxHighlighter 
                              language="json"
                              code={endpointDetails.requestBodyExample}
                            />
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                  
                  {/* Form Data Body */}
                  {endpointDetails.requestBodyType === 'form-data' && (
                    <div className="space-y-4">
                      <div className="border rounded overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
                        <div className="px-4 py-2 border-b" style={{ 
                          borderColor: colors.border,
                          backgroundColor: colors.tableHeader
                        }}>
                          <span className="text-sm font-medium" style={{ color: colors.text }}>Form Fields</span>
                        </div>
                        <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
                          <ParameterTable 
                            parameters={endpointDetails.bodyParameters} 
                            colors={colors}
                            onCopy={copyToClipboard}
                          />
                        </div>
                      </div>
                      
                      {endpointDetails.requestBodyExample && (
                        <div className="border rounded overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
                          <div className="px-4 py-2 border-b flex items-center justify-between" style={{ 
                            borderColor: colors.border,
                            backgroundColor: colors.tableHeader
                          }}>
                            <span className="text-sm font-medium" style={{ color: colors.text }}>Example (multipart/form-data)</span>
                            <button 
                              className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                              style={{ backgroundColor: colors.hover }}
                              onClick={() => copyToClipboard(endpointDetails.requestBodyExample)}>
                              <Copy size={12} style={{ color: colors.textSecondary }} />
                            </button>
                          </div>
                          <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
                            <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed" style={{ color: colors.text }}>
                              {endpointDetails.requestBodyExample}
                            </pre>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                  
                  {/* URL Encoded Body */}
                  {endpointDetails.requestBodyType === 'urlencoded' && (
                    <div className="space-y-4">
                      <div className="border rounded overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
                        <div className="px-4 py-2 border-b" style={{ 
                          borderColor: colors.border,
                          backgroundColor: colors.tableHeader
                        }}>
                          <span className="text-sm font-medium" style={{ color: colors.text }}>URL-Encoded Parameters</span>
                        </div>
                        <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
                          <ParameterTable 
                            parameters={endpointDetails.bodyParameters} 
                            colors={colors}
                            onCopy={copyToClipboard}
                          />
                        </div>
                      </div>
                      
                      {endpointDetails.requestBodyExample && (
                        <div className="border rounded overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
                          <div className="px-4 py-2 border-b flex items-center justify-between" style={{ 
                            borderColor: colors.border,
                            backgroundColor: colors.tableHeader
                          }}>
                            <span className="text-sm font-medium" style={{ color: colors.text }}>Example (URL-Encoded)</span>
                            <button 
                              className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                              style={{ backgroundColor: colors.hover }}
                              onClick={() => copyToClipboard(endpointDetails.requestBodyExample)}>
                              <Copy size={12} style={{ color: colors.textSecondary }} />
                            </button>
                          </div>
                          <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
                            <code className="text-xs font-mono break-all" style={{ color: colors.text }}>
                              {endpointDetails.requestBodyExample}
                            </code>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                  
                  {/* Text Body */}
                  {endpointDetails.requestBodyType === 'text' && (
                    <div className="space-y-4">
                      <div className="border rounded overflow-hidden hover-lift" style={{ borderColor: colors.border }}>
                        <div className="px-4 py-2 border-b flex items-center justify-between" style={{ 
                          borderColor: colors.border,
                          backgroundColor: colors.tableHeader
                        }}>
                          <span className="text-sm font-medium" style={{ color: colors.text }}>Plain Text Body</span>
                          {endpointDetails.requestBodyExample && (
                            <button 
                              className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                              style={{ backgroundColor: colors.hover }}
                              onClick={() => copyToClipboard(endpointDetails.requestBodyExample)}>
                              <Copy size={12} style={{ color: colors.textSecondary }} />
                            </button>
                          )}
                        </div>
                        <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
                          {endpointDetails.requestBodyExample ? (
                            <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed" style={{ color: colors.text }}>
                              {endpointDetails.requestBodyExample}
                            </pre>
                          ) : (
                            <p className="text-sm" style={{ color: colors.textSecondary }}>No text body example available</p>
                          )}
                        </div>
                      </div>
                    </div>
                  )}
                  
                  {(!endpointDetails.requestBodyType || endpointDetails.requestBodyType === 'none') && (
                    <div className="text-center py-8" style={{ color: colors.textSecondary }}>
                      <Info size={48} className="mx-auto mb-4 opacity-50" />
                      <p>This endpoint does not require a request body</p>
                    </div>
                  )}
                </div>
              )}

              {/* Only show "no body" message if there are no body parameters */}
              {/* {(!endpointDetails.bodyParameters || endpointDetails.bodyParameters.length === 0) && (
                <div className="mb-8 p-6 rounded-xl border" style={{ 
                  backgroundColor: colors.card,
                  borderColor: colors.border
                }}>
                  <div className="text-center py-8" style={{ color: colors.textSecondary }}>
                    <Info size={48} className="mx-auto mb-4 opacity-50" />
                    <p>This endpoint does not require a request body</p>
                  </div>
                </div>
              )} */}
            </section>

            {/* Response Examples */}
            <section>
              <h2 className="text-xl font-semibold mb-6 pb-2 border-b" style={{ 
                color: colors.text,
                borderColor: colors.border
              }}>
                Response Examples
              </h2>
              
              <div className="space-y-6">
                {endpointDetails.responseExamples && endpointDetails.responseExamples.length > 0 ? (
                  endpointDetails.responseExamples.map((example, index) => (
                    <div key={index} className="border rounded-xl overflow-hidden hover-lift" style={{ 
                      borderColor: colors.border,
                      backgroundColor: colors.card
                    }}>
                      <div className="px-4 py-3 border-b flex items-center justify-between" style={{ 
                        borderColor: colors.border,
                        backgroundColor: colors.tableHeader
                      }}>
                        <div className="flex items-center gap-3">
                          <div className="flex items-center gap-2 px-3 py-1.5 rounded text-sm font-medium" style={{ 
                            backgroundColor: example.statusCode >= 200 && example.statusCode < 300 ? `${colors.success}20` : `${colors.error}20`,
                            color: example.statusCode >= 200 && example.statusCode < 300 ? colors.success : colors.error
                          }}>
                            {example.statusCode >= 200 && example.statusCode < 300 ? <CheckCircle size={12} /> : <XCircle size={12} />}
                            {example.statusBadge?.text || `Response ${example.statusCode}`}
                          </div>
                          <span className="text-sm" style={{ color: colors.textSecondary }}>{example.description}</span>
                        </div>
                        <button 
                          className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                          style={{ backgroundColor: colors.hover }}
                          onClick={() => copyToClipboard(example.formattedExample || example.example)}>
                          <Copy size={12} style={{ color: colors.textSecondary }} />
                        </button>
                      </div>
                      <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
                        <SyntaxHighlighter 
                          language="json"
                          code={example.formattedExample || example.example || '{}'}
                        />
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-12" style={{ color: colors.textSecondary }}>
                    <Info size={48} className="mx-auto mb-4 opacity-50" />
                    <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>No Response Examples</h3>
                    <p>No response examples available for this endpoint.</p>
                  </div>
                )}
              </div>
            </section>
          </div>
        </div>
      </div>
    );
  };

  // Render main content
  const renderMainContent = () => {
    switch (activeTab) {
      case 'documentation':
      default:
        return (
          <div className="flex-1 flex flex-col overflow-hidden">
            {/* Documentation Tabs */}
            <div className="flex items-center border-b h-9" style={{ 
              backgroundColor: colors.card,
              borderColor: colors.border
            }}>
              <div className="flex items-center px-2">
                <button
                  onClick={() => setActiveTab('documentation')}
                  className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${
                    activeTab === 'documentation' ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === 'documentation' ? colors.primary : 'transparent',
                    color: activeTab === 'documentation' ? colors.primary : colors.textSecondary,
                    backgroundColor: 'transparent'
                  }}>
                  Documentation
                </button>
                
                <button
                  onClick={() => {
                    setActiveTab('changelog');
                    if (selectedCollection) {
                      fetchChangelog(selectedCollection.id);
                    } else {
                      showToast('Please select a collection first', 'warning');
                    }
                  }}
                  className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${
                    activeTab === 'changelog' ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === 'changelog' ? colors.primary : 'transparent',
                    color: activeTab === 'changelog' ? colors.primary : colors.textSecondary,
                    backgroundColor: 'transparent'
                  }}>
                  Changelog
                </button>
              </div>
            </div>

            {/* Documentation Content */}
            {renderDocumentationContent()}
          </div>
        );
        
      case 'changelog':
        return (
          <div className="flex-1 flex flex-col overflow-hidden">
            {/* Changelog Tabs */}
            <div className="flex items-center border-b h-9" style={{ 
              backgroundColor: colors.card,
              borderColor: colors.border
            }}>
              <div className="flex items-center px-2">
                <button
                  onClick={() => setActiveTab('documentation')}
                  className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${
                    activeTab === 'documentation' ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === 'documentation' ? colors.primary : 'transparent',
                    color: activeTab === 'documentation' ? colors.primary : colors.textSecondary,
                    backgroundColor: 'transparent'
                  }}>
                  Documentation
                </button>
                
                <button
                  onClick={() => setActiveTab('changelog')}
                  className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${
                    activeTab === 'changelog' ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === 'changelog' ? colors.primary : 'transparent',
                    color: activeTab === 'changelog' ? colors.primary : colors.textSecondary,
                    backgroundColor: 'transparent'
                  }}>
                  Changelog
                </button>
              </div>
            </div>

            <div className="flex-1 overflow-auto p-8">
              <div className="max-w-4xl mx-auto">
                <h2 className="text-2xl font-semibold mb-6" style={{ color: colors.text }}>API Changelog</h2>
                {changelog.length === 0 ? (
                  <div className="text-center py-12" style={{ color: colors.textSecondary }}>
                    <History size={48} className="mx-auto mb-4 opacity-50" />
                    <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>No Changelog Entries</h3>
                    <p className="mb-4">No changelog entries available for this collection.</p>
                    {selectedCollection && (
                      <button 
                        className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                        onClick={() => fetchChangelog(selectedCollection.id)}
                        style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                        Refresh Changelog
                      </button>
                    )}
                  </div>
                ) : (
                  <div className="space-y-6">
                    {changelog.map((entry, index) => (
                      <div key={index} className="border rounded-xl p-6 hover:border-opacity-50 transition-colors hover-lift cursor-pointer"
                        style={{ 
                          borderColor: colors.border,
                          backgroundColor: colors.card
                        }}
                        onClick={() => showToast(`Viewing ${entry.version} release notes`, 'info')}>
                        <div className="flex items-center justify-between mb-3">
                          <h3 className="text-lg font-semibold" style={{ color: colors.text }}>Version {entry.version}</h3>
                          <span className="text-sm" style={{ color: colors.textSecondary }}>{entry.date}</span>
                        </div>
                        <p className="text-sm mb-4" style={{ color: colors.textSecondary }}>{entry.description}</p>
                        {entry.changes && Array.isArray(entry.changes) && (
                          <ul className="space-y-2">
                            {entry.changes.map((change, idx) => (
                              <li key={idx} className="flex items-start gap-2 text-sm" style={{ color: colors.textSecondary }}>
                                <Check size={12} className="mt-0.5 flex-shrink-0" style={{ color: colors.success }} />
                                {change}
                              </li>
                            ))}
                          </ul>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        );
    }
  };

  // Render toast
  const renderToast = () => {
    if (!toast) return null;
    
    return (
      <div className="fixed bottom-4 right-4 px-4 py-2 rounded text-sm font-medium z-50 animate-fade-in-up"
        style={{ 
          backgroundColor: toast.type === 'error' ? colors.error : 
                        toast.type === 'success' ? colors.success : 
                        toast.type === 'warning' ? colors.warning : 
                        colors.info,
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
        
        /* Custom scrollbar */
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
        
        /* Focus styles */
        input:focus, button:focus {
          outline: 2px solid ${colors.primary}40;
          outline-offset: 2px;
        }
        
        /* Hover effects */
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
      <LoadingOverlay 
        isLoading={globalLoading || isLoading.initialLoad || isLoading.collections} 
        colors={colors} 
        loadingText={
          isLoading.collections ? 'Loading collections...' :
          isLoading.environments ? 'Loading environments...' :
          isLoading.folders ? 'Loading folders...' :
          'Please wait while we load your documentation data'
        }
      />

      {/* TOP NAVIGATION */}
      <div className="flex items-center justify-between h-10 px-4 border-b" style={{ 
        backgroundColor: colors.header,
        borderColor: colors.border
      }}>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-1 -ml-3 text-nowrap">
            <span className="px-3 py-1.5 text-sm font-medium rounded transition-colors uppercase" style={{ color: colors.text }}>
              API Documentation
            </span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Environment Selector */}
          {environments.length > 0 && (
            <div className="relative">
              <button className="flex items-center gap-2 px-3 py-1.5 rounded text-sm hover:bg-opacity-50 transition-colors hover-lift"
                onClick={() => setShowEnvironmentMenu(!showEnvironmentMenu)}
                style={{ backgroundColor: colors.hover }}>
                <Globe size={12} style={{ color: colors.textSecondary }} />
                <span style={{ color: colors.text }}>{environments.find(e => e.isActive)?.name || 'Select Env'}</span>
                <ChevronDown size={12} style={{ color: colors.textSecondary }} />
              </button>

              {showEnvironmentMenu && (
                <>
                  <div className="fixed inset-0 z-40" onClick={() => setShowEnvironmentMenu(false)} />
                  <div className="absolute top-full right-0 mt-1 py-2 rounded shadow-lg z-50 border min-w-48"
                    style={{ 
                      backgroundColor: colors.bg,
                      borderColor: colors.border
                    }}>
                    {environments.map(env => (
                      <button
                        key={env.id}
                        onClick={() => handleEnvironmentChange(env.id)}
                        className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors hover-lift"
                        style={{ 
                          backgroundColor: env.isActive ? colors.selected : 'transparent',
                          color: env.isActive ? colors.primary : colors.text
                        }}
                      >
                        <div className="w-2 h-2 rounded-full" style={{ 
                          backgroundColor: env.isActive ? colors.success : colors.textSecondary 
                        }}></div>
                        {env.name}
                        {env.isActive && <Check size={14} className="ml-auto" />}
                      </button>
                    ))}
                  </div>
                </>
              )}
            </div>
          )}

          <div className="w-px h-4" style={{ backgroundColor: colors.border }}></div>

          {/* Code Panel Toggle */}
          <button onClick={() => setShowCodePanel(!showCodePanel)} 
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: showCodePanel ? colors.selected : colors.hover }}>
            <Code size={14} style={{ color: showCodePanel ? colors.primary : colors.textSecondary }} />
          </button>

          {/* Publish Button */}
          <button onClick={() => {
            if (!selectedCollection) {
              showToast('Please select a collection first', 'warning');
              return;
            }
            setShowPublishModal(true);
          }} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}>
            <ExternalLink size={14} style={{ color: colors.textSecondary }} />
          </button>

          {/* Refresh Button */}
          <button 
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            onClick={async () => {
              try {
                await withGlobalLoading(async () => {
                  await fetchAPICollections();
                });
                showToast('Collections refreshed', 'success');
              } catch (error) {
                showToast('Failed to refresh collections', 'error');
              }
            }}
            style={{ backgroundColor: colors.hover }}>
            <RefreshCw size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>
      </div>

      {/* MAIN CONTENT */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left Sidebar - Collections */}
        <div className="w-80 border-r flex flex-col" style={{ 
          borderColor: colors.border
        }}>
          <div className="p-4 border-b" style={{ borderColor: colors.border }}>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Collections</h3>
              <div className="flex gap-1">
                <button className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  onClick={async () => {
                    try {
                      await fetchAPICollections();
                      showToast('Collections refreshed', 'success');
                    } catch (error) {
                      showToast('Failed to refresh collections', 'error');
                    }
                  }}
                  style={{ backgroundColor: colors.hover }}>
                  <RefreshCw size={12} style={{ color: colors.textSecondary }} />
                </button>
              </div>
            </div>
            <div className="relative">
              <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
              <input 
                type="text" 
                placeholder="Search collections..."
                value={searchQuery}
                onChange={(e) => {
                  setSearchQuery(e.target.value);
                  if (e.target.value.trim()) {
                    searchDocumentationAPI(e.target.value);
                  } else {
                    setSearchResults([]);
                  }
                }}
                className="w-full pl-8 pr-3 py-2 rounded text-sm focus:outline-none hover-lift"
                style={{ 
                  backgroundColor: colors.inputBg, 
                  border: `1px solid ${colors.border}`, 
                  color: colors.text 
                }} 
              />
              {searchQuery && (
                <div className="absolute right-2 top-1/2 transform -translate-y-1/2">
                  <button onClick={() => {
                    setSearchQuery('');
                    setSearchResults([]);
                  }} className="p-0.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                    style={{ backgroundColor: colors.hover }}>
                    <X size={12} style={{ color: colors.textSecondary }} />
                  </button>
                </div>
              )}
            </div>
          </div>

          <div className="flex-1 overflow-auto p-2">
            {isLoading.collections && !isLoading.initialLoad ? (
              <div className="text-center py-8" style={{ color: colors.textSecondary }}>
                <RefreshCw size={16} className="animate-spin mx-auto mb-2" />
                <p className="text-sm">Loading collections...</p>
              </div>
            ) : filteredCollections.length === 0 && !isLoading.initialLoad ? (
              <div className="text-center p-4" style={{ color: colors.textSecondary }}>
                <Book size={20} className="mx-auto mb-2 opacity-50" />
                <p className="text-sm">No collections available</p>
                <button className="mt-4 px-3 py-1.5 text-xs rounded hover:bg-opacity-50 transition-colors hover-lift"
                  onClick={async () => {
                    try {
                      await withGlobalLoading(async () => {
                        await fetchAPICollections();
                      });
                    } catch (error) {
                      showToast('Failed to load collections', 'error');
                    }
                  }}
                  style={{ backgroundColor: colors.hover, color: colors.text }}>
                  Load Collections
                </button>
              </div>
            ) : (
              <>
                {filteredCollections.map(collection => {
                  const totalEndpoints = collection.folders?.reduce((sum, folder) => 
                    sum + (folder.requests?.length || 0), 0) || collection.totalEndpoints || 0;
                  
                  return (
                    <div key={collection.id} className="mb-3">
                      {/* Collection Header */}
                      <div className="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-opacity-50 transition-colors mb-1.5 cursor-pointer hover-lift"
                        onClick={() => toggleCollection(collection.id)}
                        style={{ backgroundColor: colors.hover }}>
                        {expandedCollections.includes(collection.id) ? (
                          <ChevronDown size={12} style={{ color: colors.textSecondary }} />
                        ) : (
                          <ChevronRight size={12} style={{ color: colors.textSecondary }} />
                        )}
                        <button onClick={(e) => {
                          e.stopPropagation();
                          const newCollections = collections.map(c => 
                            c.id === collection.id ? { ...c, isFavorite: !c.isFavorite } : c
                          );
                          setCollections(newCollections);
                          showToast(collection.isFavorite ? 'Removed from favorites' : 'Added to favorites', 'success');
                        }}>
                          {collection.isFavorite ? (
                            <Star size={12} fill="#FFB300" style={{ color: '#FFB300' }} />
                          ) : (
                            <Star size={12} style={{ color: colors.textSecondary }} />
                          )}
                        </button>
                        
                        <span className="text-sm font-medium flex-1 truncate" style={{ color: colors.text }}>
                          {collection.name}
                        </span>
                        
                        {totalEndpoints > 0 && (
                          <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                            backgroundColor: colors.primaryDark,
                            color: 'white'
                          }}>
                            {totalEndpoints}
                          </span>
                        )}
                        
                        {(!collection.folders || collection.folders.length === 0) && isLoading.folders && (
                          <RefreshCw size={10} className="animate-spin" style={{ color: colors.textSecondary }} />
                        )}
                      </div>

                      {/* Folders */}
                      {expandedCollections.includes(collection.id) && collection.folders && collection.folders.length > 0 && (
                        <>
                          {collection.folders.map(folder => (
                            <div key={folder.id} className="ml-4 mb-2">
                              {/* Folder Header */}
                              <div 
                                className="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-opacity-50 transition-colors mb-1.5 cursor-pointer hover-lift"
                                onClick={() => toggleFolder(folder.id)}
                                style={{ backgroundColor: colors.hover }}
                              >
                                {expandedFolders.includes(folder.id) ? (
                                  <ChevronDown size={11} style={{ color: colors.textSecondary }} />
                                ) : (
                                  <ChevronRight size={11} style={{ color: colors.textSecondary }} />
                                )}
                                <FolderOpen size={11} style={{ color: colors.textSecondary }} />
                                
                                <span className="text-sm flex-1 truncate" style={{ color: colors.text }}>
                                  {folder.name}
                                </span>
                                
                                {(folder.requests?.length > 0 || folder.requestCount > 0) && (
                                  <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                                    backgroundColor: colors.primaryDark,
                                    color: 'white'
                                  }}>
                                    {folder.requests?.length || folder.requestCount || 0}
                                  </span>
                                )}
                                
                                {folderLoading[folder.id] && (
                                  <RefreshCw size={10} className="animate-spin" style={{ color: colors.textSecondary }} />
                                )}
                              </div>

                              {/* Endpoints - Show when folder is expanded */}
                              {expandedFolders.includes(folder.id) && (
                                <div className="ml-6 mt-1 space-y-1">
                                  {folderLoading[folder.id] ? (
                                    <div className="py-2 text-center">
                                      <RefreshCw size={12} className="animate-spin mx-auto mb-1" style={{ color: colors.textSecondary }} />
                                      <p className="text-xs" style={{ color: colors.textTertiary }}>Loading endpoints...</p>
                                    </div>
                                  ) : folder.error ? (
                                    <div className="py-2 text-center">
                                      <p className="text-xs" style={{ color: colors.error }}>{folder.error}</p>
                                      <button 
                                        className="text-xs mt-1 px-2 py-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                                        onClick={() => fetchAPIEndpoints(collection.id, folder.id)}
                                        style={{ backgroundColor: colors.hover, color: colors.text }}
                                      >
                                        Retry
                                      </button>
                                    </div>
                                  ) : (
                                    <>
                                      {folder.requests && folder.requests.length > 0 ? (
                                        folder.requests.map(request => (
                                          <div key={request.id} className="flex items-center gap-2 group">
                                            <button
                                              onClick={() => handleSelectRequest(request, collection.id, folder.id)}
                                              className="flex items-center gap-2 text-sm text-left transition-colors flex-1 px-2 py-1.5 rounded hover:bg-opacity-50 hover-lift"
                                              style={{ 
                                                color: selectedRequest?.id === request.id ? colors.primary : colors.text,
                                                backgroundColor: selectedRequest?.id === request.id ? colors.selected : 'transparent'
                                              }}>
                                              <div className="w-2 h-2 rounded-full flex-shrink-0" style={{ 
                                                backgroundColor: getMethodColor(request.method)
                                              }} />
                                              
                                              <span className="truncate">{request.name}</span>
                                            </button>
                                          </div>
                                        ))
                                      ) : (
                                        <div className="py-2 text-center">
                                          <p className="text-xs" style={{ color: colors.textTertiary }}>No endpoints available</p>
                                        </div>
                                      )}
                                    </>
                                  )}
                                </div>
                              )}
                            </div>
                          ))}
                        </>
                      )}
                    </div>
                  );
                })}
              </>
            )}
            
            {filteredCollections.length === 0 && searchQuery && (
              <div className="text-center p-4" style={{ color: colors.textSecondary }}>
                <Search size={20} className="mx-auto mb-2 opacity-50" />
                <p className="text-sm">No collections found for "{searchQuery}"</p>
                <button className="mt-2 px-3 py-1.5 text-xs rounded hover:bg-opacity-50 transition-colors hover-lift"
                  onClick={() => setSearchQuery('')}
                  style={{ backgroundColor: colors.hover, color: colors.text }}>
                  Clear Search
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Main Content Area */}
        {renderMainContent()}

        {/* Right Code Panel */}
        {showCodePanel && renderCodePanel()}
      </div>

      {/* TOAST */}
      {renderToast()}
    </div>
  );
};

export default Documentation;