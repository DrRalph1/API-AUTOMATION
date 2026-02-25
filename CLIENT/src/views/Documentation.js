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
  getSupportedDocumentationFormats
} from "../controllers/DocumentationController.js";

// Also import apiCall for debug purposes
import { apiCall } from "@/helpers/APIHelper.js";

// SyntaxHighlighter Component
const SyntaxHighlighter = ({ language, code }) => {
  const highlightSyntax = (code, lang) => {
    if (lang === 'json') {
      return code
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, (match) => {
          let cls = 'text-blue-400';
          if (/^"/.test(match)) {
            if (/:$/.test(match)) {
              cls = 'text-purple-400';
            } else {
              cls = 'text-green-400';
            }
          } else if (/true|false/.test(match)) {
            cls = 'text-orange-400';
          } else if (/null/.test(match)) {
            cls = 'text-red-400';
          }
          return `<span class="${cls}">${match}</span>`;
        });
    }
    
    if (lang === 'javascript' || lang === 'nodejs') {
      return code
        .replace(/(\b(?:function|const|let|var|if|else|for|while|return|class|import|export|from|default|async|await|try|catch|finally|throw|new|this)\b)/g, '<span class="text-purple-400">$1</span>')
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"|'(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\'])*')/g, '<span class="text-green-400">$1</span>')
        .replace(/(\/\/.*)/g, '<span class="text-gray-500">$1</span>')
        .replace(/(\b\d+\b)/g, '<span class="text-blue-400">$1</span>');
    }
    
    if (lang === 'python') {
      return code
        .replace(/(\b(?:def|class|import|from|if|elif|else|for|while|try|except|finally|with|as|return|yield|async|await|lambda|in|is|not|and|or|True|False|None)\b)/g, '<span class="text-purple-400">$1</span>')
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"|'(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\'])*')/g, '<span class="text-green-400">$1</span>')
        .replace(/(#.*)/g, '<span class="text-gray-500">$1</span>');
    }
    
    return code;
  };

  return (
    <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed" 
      dangerouslySetInnerHTML={{ __html: highlightSyntax(code, language) }} />
  );
};

// Loading Overlay Component - Exactly matching APISecurity pattern
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
        <p className="text-xs" style={{ color: colors.textTertiary }}>
          This won't take long
        </p>
      </div>
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
  
  // Updated loading state to match APISecurity pattern
  const [isLoading, setIsLoading] = useState({
    collections: false,
    environments: false,
    notifications: false,
    endpointDetails: false,
    endpoints: false,
    folders: false,
    initialLoad: true // Add initial load state
  });
  
  const [endpointDetails, setEndpointDetails] = useState(null);
  const [codeExamples, setCodeExamples] = useState({});
  const [changelog, setChangelog] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [userId, setUserId] = useState('');

  // Updated color scheme to match CodeBase
  const colors = isDark ? {
    // Using your shade as base - EXACTLY matching Dashboard
    bg: 'rgb(1 14 35)',
    white: '#FFFFFF',
    sidebar: 'rgb(41 53 72 / 19%)',
    main: 'rgb(1 14 35)',
    header: 'rgb(20 26 38)',
    card: 'rgb(41 53 72 / 19%)',
    
    // Text - coordinating grays - EXACTLY matching Dashboard
    text: '#F1F5F9',
    textSecondary: 'rgb(148 163 184)',
    textTertiary: 'rgb(100 116 139)',
    
    // Borders - variations of your shade - EXACTLY matching Dashboard
    border: 'rgb(51 65 85 / 19%)',
    borderLight: 'rgb(45 55 72)',
    borderDark: 'rgb(71 85 105)',
    
    // Interactive - layered transparency - EXACTLY matching Dashboard
    hover: 'rgb(45 46 72 / 33%)',
    active: 'rgb(59 74 99)',
    selected: 'rgb(44 82 130)',
    
    // Primary colors - EXACTLY matching Dashboard
    primary: 'rgb(96 165 250)',
    primaryLight: 'rgb(147 197 253)',
    primaryDark: 'rgb(37 99 235)',
    
    // Status colors - EXACTLY matching Dashboard
    success: 'rgb(52 211 153)',
    warning: 'rgb(251 191 36)',
    error: 'rgb(248 113 113)',
    info: 'rgb(96 165 250)',
    
    // UI Components - EXACTLY matching Dashboard
    tabActive: 'rgb(96 165 250)',
    tabInactive: 'rgb(148 163 184)',
    sidebarActive: 'rgb(96 165 250)',
    sidebarHover: 'rgb(45 46 72 / 33%)',
    inputBg: 'rgb(41 53 72 / 19%)',
    inputborder: 'rgb(51 65 85 / 19%)',
    tableHeader: 'rgb(41 53 72 / 19%)',
    tableRow: 'rgb(41 53 72 / 19%)',
    tableRowHover: 'rgb(45 46 72 / 33%)',
    dropdownBg: 'rgb(41 53 72 / 19%)',
    dropdownborder: 'rgb(51 65 85 / 19%)',
    modalBg: 'rgb(41 53 72 / 19%)',
    modalborder: 'rgb(51 65 85 / 19%)',
    codeBg: 'rgb(41 53 72 / 19%)',
    
    // Connection status - EXACTLY matching Dashboard
    connectionOnline: 'rgb(52 211 153)',
    connectionOffline: 'rgb(248 113 113)',
    connectionIdle: 'rgb(251 191 36)',
    
    // Method colors (specific to API docs) - using Dashboard's color palette
    method: {
      GET: 'rgb(52 211 153)',      // success color
      POST: 'rgb(96 165 250)',     // info/primary color
      PUT: 'rgb(251 191 36)',      // warning color
      DELETE: 'rgb(248 113 113)',  // error color
      PATCH: 'rgb(167 139 250)',   // accentPurple from Dashboard
      HEAD: 'rgb(148 163 184)',    // textSecondary
      OPTIONS: 'rgb(167 139 250)', // accentPurple
      LINK: 'rgb(34 211 238)',     // accentCyan from Dashboard
      UNLINK: 'rgb(251 191 36)'    // warning color
    },
    
    // Accent colors - EXACTLY matching Dashboard
    accentPurple: 'rgb(167 139 250)',
    accentPink: 'rgb(244 114 182)',
    accentCyan: 'rgb(34 211 238)'
  } : {
    // LIGHT MODE - EXACTLY matching Dashboard's light mode
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
    
    // Method colors for light mode
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

  // Get userId from authToken (simplified - extract from token)
  const extractUserIdFromToken = (token) => {
    if (!token) return '';
    try {
      // Simple extraction - in real app, you'd decode JWT
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

  // Add this debug function
  const debugDatabaseContents = async () => {
    console.log('🔍 DEBUG: Checking database contents');
    
    // Check each collection
    for (const collection of collections) {
      console.log(`\n📁 Collection: ${collection.name} (${collection.id})`);
      console.log(`   Total endpoints according to API: ${collection.totalEndpoints || 0}`);
      
      // Try to fetch endpoints for this collection
      try {
        const response = await getAPIEndpoints(authToken, collection.id, collection.id);
        console.log(`   API Response for ${collection.id}:`, response);
        
        if (response.data && response.data.endpoints) {
          console.log(`   Actual endpoints found: ${response.data.endpoints.length}`);
          if (response.data.endpoints.length > 0) {
            console.log('   First endpoint:', response.data.endpoints[0]);
          }
        }
      } catch (error) {
        console.error(`   Error fetching endpoints for ${collection.id}:`, error.message);
      }
    }
    
    // Also check if there are any endpoints at all in the database
    try {
      // You might need to add this endpoint to your backend
      // For now, we'll just check the first collection again
      console.log('\n📊 Summary: No endpoints found in database');
    } catch (error) {
      console.error('Error checking database:', error);
    }
    
    showToast('Check console for database debug info', 'info');
  };

  // Fetch folders for a collection - FIXED: Return a folder using collection ID
  const fetchFolders = useCallback(async (collectionId) => {
    console.log(`📁 [Documentation] Fetching folders for collection: ${collectionId}`);
    
    if (!authToken || !collectionId) {
      return [];
    }

    try {
      setIsLoading(prev => ({ ...prev, folders: true }));
      
      // Since your backend doesn't have folders, create a virtual folder
      // using the collection ID as the folder ID
      const virtualFolder = {
        id: collectionId, // Use collection ID as folder ID
        name: "Endpoints",
        description: "All endpoints",
        collectionId: collectionId,
        requests: [],
        isLoading: false,
        error: null
      };
      
      setFolders(prev => ({
        ...prev,
        [collectionId]: [virtualFolder]
      }));
      
      return [virtualFolder];
      
    } catch (error) {
      console.error('Error fetching folders:', error);
      return [];
    } finally {
      setIsLoading(prev => ({ ...prev, folders: false }));
    }
  }, [authToken]);

  // Fetch folders for a specific collection and check all for endpoints
  const fetchFoldersForCollection = useCallback(async (collectionId) => {
    console.log(`📁 [Documentation] Fetching folders for collection: ${collectionId}`);
    
    if (!authToken || !collectionId) {
      return;
    }

    try {
      setIsLoading(prev => ({ ...prev, folders: true }));
      
      // Make API call to get folders
      const response = await getFolders(authToken, collectionId);
      console.log(`📦 [Documentation] Folders response for ${collectionId}:`, response);
      
      if (response && response.data && response.data.folders) {
        const foldersData = response.data.folders;
        console.log(`📁 Found ${foldersData.length} folders`);
        
        // Update the collection with its folders
        setCollections(prevCollections => 
          prevCollections.map(collection => {
            if (collection.id === collectionId) {
              return {
                ...collection,
                folders: foldersData.map(folder => ({
                  id: folder.id,
                  name: folder.name,
                  description: folder.description,
                  requests: [],
                  isLoading: false,
                  error: null
                }))
              };
            }
            return collection;
          })
        );
        
        // Check each folder for endpoints
        for (const folder of foldersData) {
          console.log(`🔍 Checking folder: ${folder.name} (${folder.id})`);
          try {
            const endpointResponse = await getAPIEndpoints(authToken, collectionId, folder.id);
            console.log(`   Endpoints in ${folder.name}:`, endpointResponse?.data?.totalEndpoints || 0);
          } catch (error) {
            console.error(`   Error checking folder ${folder.name}:`, error.message);
          }
        }
        
        // Also try fetching endpoints without a folder ID (directly under collection)
        try {
          console.log(`🔍 Checking endpoints directly under collection (no folder)`);
          // You might need a different endpoint for this
          // This depends on your backend implementation
        } catch (error) {
          console.log('No direct collection endpoints');
        }
        
        // Auto-expand first folder and load its endpoints if it has any
        if (foldersData.length > 0) {
          const firstFolder = foldersData[0];
          setExpandedFolders(prev => [...prev, firstFolder.id]);
          await fetchAPIEndpoints(collectionId, firstFolder.id);
        }
      }
      
    } catch (error) {
      console.error('❌ Error fetching folders:', error);
    } finally {
      setIsLoading(prev => ({ ...prev, folders: false }));
    }
  }, [authToken]);

  // Load API collections and their folders
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
      // Get collections from API
      const response = await getAPICollections(authToken);
      console.log('📦 [Documentation] API response:', response);
      
      if (!response) {
        throw new Error('No response from documentation service');
      }
      
      const handledResponse = handleDocumentationResponse(response);
      const collectionsData = extractAPICollections(handledResponse);
      
      console.log('📊 [Documentation] Extracted collections data:', collectionsData.length, 'collections');

      // Format collections with empty folders array
      const formattedCollections = collectionsData.map(collection => {
        const formatted = formatDocumentationCollection(collection);
        formatted.folders = []; // Will be populated by folder fetch
        return formatted;
      });
      
      setCollections(formattedCollections);
      console.log('📊 [Documentation] Formatted collections:', formattedCollections);
      
      // Cache the data if we have userId
      const userId = extractUserIdFromToken(authToken);
      if (userId) {
        cacheDocumentationData(userId, 'collections', formattedCollections);
      }
      
      // Fetch folders for the first collection
      if (formattedCollections.length > 0) {
        const firstCollection = formattedCollections[0];
        setSelectedCollection(firstCollection);
        setExpandedCollections([firstCollection.id]);
        
        // Fetch folders for the first collection
        await fetchFoldersForCollection(firstCollection.id);
      }
      
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

  // Load API endpoints for a folder - with better debugging
  const fetchAPIEndpoints = useCallback(async (collectionId, folderId) => {
    console.log(`📡 [Documentation] Fetching endpoints for collection ${collectionId}, folder ${folderId}`);
    
    if (!authToken || !collectionId || !folderId) {
      console.log('Missing params for fetchAPIEndpoints');
      return;
    }

    // Set loading state for this specific folder
    setCollections(prevCollections => {
      const updatedCollections = prevCollections.map(collection => {
        if (collection.id === collectionId) {
          const updatedFolders = (collection.folders || []).map(folder => {
            if (folder.id === folderId) {
              return { ...folder, isLoading: true, error: null };
            }
            return folder;
          });
          return { ...collection, folders: updatedFolders };
        }
        return collection;
      });
      return updatedCollections;
    });

    setIsLoading(prev => ({ ...prev, endpoints: true }));
    
    try {
      console.log(`🔍 Making API call to get endpoints for folder: ${folderId}`);
      const response = await getAPIEndpoints(authToken, collectionId, folderId);
      console.log('📦 [Documentation] Raw endpoints response:', JSON.stringify(response, null, 2));
      
      // Check if the response has the expected structure
      console.log('🔍 Response structure:', {
        hasData: !!response?.data,
        dataType: response?.data ? typeof response.data : 'undefined',
        dataKeys: response?.data ? Object.keys(response.data) : [],
        responseCode: response?.responseCode
      });
      
      const handledResponse = handleDocumentationResponse(response);
      console.log('🔄 Handled response:', handledResponse);
      
      const endpoints = extractAPIEndpoints(handledResponse);
      console.log(`📊 Extracted ${endpoints.length} endpoints:`, endpoints);
      
      // Format endpoints for display
      const formattedEndpoints = endpoints.map(endpoint => 
        formatDocumentationEndpoint(endpoint)
      );
      
      console.log('📊 Formatted endpoints:', formattedEndpoints);
      
      // Update collection with endpoints
      setCollections(prevCollections => {
        const updatedCollections = prevCollections.map(collection => {
          if (collection.id === collectionId) {
            const updatedFolders = (collection.folders || []).map(folder => {
              if (folder.id === folderId) {
                return { 
                  ...folder, 
                  requests: formattedEndpoints,
                  isLoading: false,
                  error: null
                };
              }
              return folder;
            });
            
            return { 
              ...collection, 
              folders: updatedFolders,
              lastFetched: new Date().toISOString()
            };
          }
          return collection;
        });
        
        console.log('📊 Updated collections:', updatedCollections);
        return updatedCollections;
      });
      
      if (formattedEndpoints.length > 0) {
        console.log('🎯 Auto-selecting first endpoint');
        const firstEndpoint = formattedEndpoints[0];
        
        setSelectedRequest(firstEndpoint);
        
        const collection = collections.find(c => c.id === collectionId);
        if (collection) {
          setSelectedCollection(collection);
        }
        
        await fetchEndpointDetails(collectionId, firstEndpoint.id);
        showToast(`Loaded ${formattedEndpoints.length} endpoints`, 'success');
      } else {
        console.log('⚠️ No endpoints found for this folder');
        showToast('No endpoints found in this folder', 'info');
      }
      
    } catch (error) {
      console.error('❌ [Documentation] Error loading API endpoints:', error);
      console.error('Error details:', {
        message: error.message,
        stack: error.stack,
        response: error.response
      });
      
      setCollections(prevCollections => {
        const updatedCollections = prevCollections.map(collection => {
          if (collection.id === collectionId) {
            const updatedFolders = (collection.folders || []).map(folder => {
              if (folder.id === folderId) {
                return { 
                  ...folder, 
                  requests: [],
                  error: error.message,
                  isLoading: false
                };
              }
              return folder;
            });
            return { ...collection, folders: updatedFolders };
          }
          return collection;
        });
        return updatedCollections;
      });
      
      showToast(`Failed to load endpoints: ${error.message}`, 'error');
    } finally {
      setIsLoading(prev => ({ ...prev, endpoints: false }));
    }
  }, [authToken, selectedRequest, collections]);

  // Load endpoint details
  const fetchEndpointDetails = useCallback(async (collectionId, endpointId) => {
    console.log(`📡 [Documentation] Fetching details for endpoint ${endpointId}`);
    
    if (!authToken || !collectionId || !endpointId) {
      console.log('Missing params for fetchEndpointDetails');
      return;
    }

    try {
      setIsLoading(prev => ({ ...prev, endpointDetails: true }));
      const response = await getEndpointDetails(authToken, collectionId, endpointId);
      const handledResponse = handleDocumentationResponse(response);
      const details = extractEndpointDetails(handledResponse);
      
      if (details) {
        const formattedDetails = formatEndpointDetails(details);
        setEndpointDetails(formattedDetails);
        
        // Load changelog for this endpoint
        await fetchChangelog(collectionId);
        
        // Load code examples for the selected language
        await fetchCodeExamples(endpointId, selectedLanguage);
      }
      
    } catch (error) {
      console.error('❌ [Documentation] Error loading endpoint details:', error);
      showToast(`Failed to load endpoint details: ${error.message}`, 'error');
    } finally {
      setIsLoading(prev => ({ ...prev, endpointDetails: false }));
    }
  }, [authToken, selectedLanguage]);

  // Load code examples
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
      // Don't show toast for this as it's not critical
    }
  }, [authToken]);

  // Search documentation
  const searchDocumentationAPI = useCallback(async (query) => {
    console.log(`🔍 [Documentation] Searching for: "${query}"`);
    
    if (!authToken || !query.trim()) {
      setSearchResults([]);
      return;
    }

    try {
      setIsSearching(true);
      const response = await searchDocumentation(authToken, query, {
        type: 'all',
        maxResults: 10
      });
      const handledResponse = handleDocumentationResponse(response);
      const results = extractSearchResults(handledResponse);
      
      const formattedResults = results.map(result => 
        formatSearchResult(result)
      );
      
      setSearchResults(formattedResults);
      console.log(`🔍 [Documentation] Found ${formattedResults.length} results`);
      
    } catch (error) {
      console.error('❌ [Documentation] Error searching documentation:', error);
      showToast(`Search failed: ${error.message}`, 'error');
      setSearchResults([]);
    } finally {
      setIsSearching(false);
    }
  }, [authToken]);

  // Load environments
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

  // Load notifications
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

  // Load changelog
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

  // Publish documentation
  const publishDocumentationAPI = async (publishData) => {
    console.log('📡 [Documentation] Publishing documentation...');
    
    if (!authToken) {
      console.log('❌ No auth token for publishDocumentationAPI');
      throw new Error('Authentication required');
    }

    try {
      const errors = validatePublishDocumentation(publishData);
      if (errors.length > 0) {
        throw new Error(errors.join(', '));
      }
      
      const response = await publishDocumentation(authToken, publishData);
      const handledResponse = handleDocumentationResponse(response);
      const results = extractPublishResults(handledResponse);
      
      console.log('✅ [Documentation] Published successfully:', results);
      return results;
      
    } catch (error) {
      console.error('❌ [Documentation] Error publishing documentation:', error);
      throw error;
    }
  };

  // Generate mock server
  const generateMockServerAPI = async (mockRequest) => {
    console.log('📡 [Documentation] Generating mock server...');
    
    if (!authToken) {
      console.log('❌ No auth token for generateMockServerAPI');
      throw new Error('Authentication required');
    }

    try {
      const errors = validateGenerateMockServer(mockRequest);
      if (errors.length > 0) {
        throw new Error(errors.join(', '));
      }
      
      const response = await generateMockServer(authToken, mockRequest);
      const handledResponse = handleDocumentationResponse(response);
      const results = extractMockServerResults(handledResponse);
      
      console.log('✅ [Documentation] Mock server generated:', results);
      return results;
      
    } catch (error) {
      console.error('❌ [Documentation] Error generating mock server:', error);
      throw error;
    }
  };

  // Clear documentation cache
  const clearDocumentationCacheAPI = async () => {
    console.log('🗑️ [Documentation] Clearing cache...');
    
    if (!authToken) {
      console.log('❌ No auth token for clearDocumentationCacheAPI');
      return;
    }

    try {
      await clearDocumentationCache(authToken);
      const userId = extractUserIdFromToken(authToken);
      clearCachedDocumentationData(userId);
      showToast('Documentation cache cleared', 'success');
      console.log('✅ [Documentation] Cache cleared');
      
    } catch (error) {
      console.error('❌ [Documentation] Error clearing documentation cache:', error);
      showToast(`Failed to clear cache: ${error.message}`, 'error');
    }
  };

  // ==================== UI HELPER FUNCTIONS ====================

  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const toggleCollection = async (collectionId) => {
    console.log(`📂 [Documentation] Toggling collection ${collectionId}`);
    
    const isExpanding = !expandedCollections.includes(collectionId);
    
    setExpandedCollections(prev =>
      prev.includes(collectionId)
        ? prev.filter(id => id !== collectionId)
        : [...prev, collectionId]
    );
    
    // Fetch folders if expanding and collection has no folders yet
    if (isExpanding) {
      const collection = collections.find(c => c.id === collectionId);
      if (collection && (!collection.folders || collection.folders.length === 0)) {
        await fetchFoldersForCollection(collectionId);
      }
    }
  };

  // FIXED: toggleFolder function
  const toggleFolder = async (folderId) => {
    console.log(`📁 [Documentation] Toggling folder ${folderId}`);
    
    const isExpanding = !expandedFolders.includes(folderId);
    
    setExpandedFolders(prev =>
      prev.includes(folderId)
        ? prev.filter(id => id !== folderId)
        : [...prev, folderId]
    );
    
    // Load endpoints for the folder if expanding
    if (isExpanding && selectedCollection) {
      console.log(`📁 [Documentation] Fetching endpoints for folder ${folderId}`);
      
      const collection = collections.find(c => c.id === selectedCollection.id);
      const folder = collection?.folders?.find(f => f.id === folderId);
      
      if (folder && (!folder.requests || folder.requests.length === 0) && !folder.isLoading) {
        await fetchAPIEndpoints(selectedCollection.id, folderId);
      }
    }
  };

  const handleSelectRequest = async (request, collectionId, folderId) => {
    console.log('🎯 [Documentation] Selecting request:', request.name);
    
    const collection = collections.find(c => c.id === collectionId);
    if (collection) {
      setSelectedCollection(collection);
    }
    
    setSelectedRequest(request);
    
    showToast(`Viewing documentation for ${request.name}`, 'info');
    
    await fetchEndpointDetails(collectionId, request.id);
  };

  const handleEnvironmentChange = (envId) => {
    setActiveEnvironment(envId);
    setEnvironments(envs => envs.map(env => ({
      ...env,
      isActive: env.id === envId
    })));
    showToast(`Switched to ${environments.find(e => e.id === envId)?.name} environment`, 'success');
    setShowEnvironmentMenu(false);
  };

  const generatePublishUrl = async () => {
    if (!selectedCollection) return;
    
    setIsGeneratingDocs(true);
    try {
      const publishData = {
        collectionId: selectedCollection.id,
        title: `${selectedCollection.name} Documentation`,
        visibility: 'public',
        customDomain: ''
      };
      
      const results = await publishDocumentationAPI(publishData);
      
      if (results?.success) {
        const url = generateDocumentationUrl(publishData);
        setPublishUrl(results.publishedUrl || url);
        showToast('Documentation published successfully!', 'success');
        setShowPublishModal(false);
      } else {
        showToast('Failed to publish documentation', 'error');
      }
    } catch (error) {
      console.error('Error generating publish URL:', error);
      showToast(error.message || 'Failed to publish documentation', 'error');
    } finally {
      setIsGeneratingDocs(false);
    }
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    showToast('Copied to clipboard!', 'success');
  };

  const generateCodeExample = () => {
    if (!selectedRequest) {
      return '// Select an endpoint to view code examples';
    }
    
    const currentExamples = codeExamples[selectedRequest.id]?.[selectedLanguage];
    if (currentExamples?.code) {
      return currentExamples.code;
    }
    
    const baseUrl = getActiveBaseUrl();
    const url = selectedRequest.url || selectedRequest.path || '';
    
    const examples = {
      curl: `curl -X ${selectedRequest.method} "${baseUrl}${url}" \\
  -H "Content-Type: application/json" \\
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"`,
      javascript: `fetch('${baseUrl}${url}', {
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

response = requests.${selectedRequest.method.toLowerCase()}('${baseUrl}${url}', 
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

  const getActiveBaseUrl = () => {
    return environments.find(e => e.id === activeEnvironment)?.baseUrl || '';
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
  }, [authToken]);

  // Update the debug button in the sidebar
  const renderDebugButton = () => {
    return (
      <button 
        className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
        onClick={async () => {
          console.log('🔍 DEBUG: Checking all folders for endpoints');
          
          for (const collection of collections) {
            console.log(`\n📁 Collection: ${collection.name} (${collection.id})`);
            console.log(`   Total endpoints: ${collection.totalEndpoints}`);
            
            if (collection.folders && collection.folders.length > 0) {
              console.log(`   Folders: ${collection.folders.length}`);
              
              for (const folder of collection.folders) {
                console.log(`\n   📂 Checking folder: ${folder.name} (${folder.id})`);
                try {
                  const response = await getAPIEndpoints(authToken, collection.id, folder.id);
                  console.log(`      ✅ Response:`, response);
                  if (response.data && response.data.endpoints) {
                    console.log(`      📊 Endpoints: ${response.data.endpoints.length}`);
                    if (response.data.endpoints.length > 0) {
                      console.log(`      First endpoint:`, response.data.endpoints[0]);
                    }
                  }
                } catch (error) {
                  console.error(`      ❌ Error:`, error.message);
                }
              }
            } else {
              console.log(`   No folders found`);
            }
          }
          
          showToast('Check console for debug info', 'info');
        }}
        style={{ backgroundColor: colors.hover }}
        title="Debug All Folders"
      >
        <Database size={12} style={{ color: colors.textSecondary }} />
      </button>
    );
  };

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
        backgroundColor: colors.sidebar,
        borderColor: colors.border
      }}>
        <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Code</h3>
          <button onClick={() => setShowCodePanel(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.hover }}>
            <X size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        <div className="relative px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <button
            onClick={() => setShowLanguageDropdown(!showLanguageDropdown)}
            className="w-full px-3 py-2 rounded text-sm font-medium flex items-center justify-between hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.hover, color: colors.text }}
          >
            <div className="flex items-center gap-2">
              <Terminal size={14} />
              <span>{currentLanguage?.label || currentLanguage?.name || 'Select Language'}</span>
            </div>
            <ChevronDown size={14} style={{ color: colors.textSecondary }} />
          </button>

          {showLanguageDropdown && (
            <>
              <div className="fixed inset-0 z-40" onClick={() => setShowLanguageDropdown(false)} />
              <div className="absolute left-4 right-4 top-full mt-1 py-2 rounded shadow-lg z-50 border"
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
            </>
          )}
        </div>

        <div className="flex-1 overflow-auto p-4">
          <div className="mb-4 flex justify-between items-center">
            <h4 className="text-sm font-medium" style={{ color: colors.text }}>Code Example</h4>
            <button 
              onClick={() => {
                const example = generateCodeExample();
                copyToClipboard(example);
              }}
              className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors flex items-center gap-1"
              style={{ backgroundColor: colors.hover, color: colors.text }}
            >
              <Copy size={10} />
              Copy
            </button>
          </div>
          
          {isLoading.endpointDetails ? (
            <div className="text-center py-8" style={{ color: colors.textSecondary }}>
              <RefreshCw size={16} className="animate-spin mx-auto mb-2" />
              <p className="text-sm">Loading code examples...</p>
            </div>
          ) : (
            <SyntaxHighlighter 
              language={selectedLanguage === 'curl' ? 'bash' : selectedLanguage}
              code={generateCodeExample()}
            />
          )}
        </div>

        <div className="p-4 border-t" style={{ borderColor: colors.border }}>
          <button 
            className="w-full py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center justify-center gap-2"
            onClick={() => {
              const example = generateCodeExample();
              copyToClipboard(example);
            }}
            style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
            <Copy size={12} />
            Copy to Clipboard
          </button>
        </div>
      </div>
    );
  };

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
    
    const activeEnv = environments.find(e => e.id === activeEnvironment);
    
    return (
      <div className="flex-1 overflow-auto p-8">
        <div className="max-w-6xl mx-auto">
          {/* Header */}
          <div className="mb-8">
            <div className="flex items-center gap-3 mb-2">
              <div className="px-3 py-1 rounded text-sm font-medium" style={{ 
                backgroundColor: getMethodColor(selectedRequest.method),
                color: 'white'
              }}>
                {selectedRequest.method}
              </div>
              <code className="text-lg font-mono" style={{ color: colors.text }}>
                {activeEnv?.baseUrl || ''}{selectedRequest.path || selectedRequest.url || ''}
              </code>
            </div>
            <h1 className="text-2xl font-semibold mb-4" style={{ color: colors.text }}>
              {selectedRequest.name}
            </h1>
            <p className="text-base mb-6" style={{ color: colors.textSecondary }}>
              {selectedRequest.description || endpointDetails.description}
            </p>
            
            <div className="flex flex-wrap items-center gap-4 text-sm mb-6">
              <div style={{ color: colors.textTertiary }}>
                <Folder size={12} className="inline mr-1" />
                {selectedCollection?.name} › {selectedRequest.category || 'API'}
              </div>
              <div className="flex items-center gap-2">
                {selectedRequest.formattedTags?.map((tag, index) => (
                  <span key={index} className="text-xs px-2 py-1 rounded" style={{ 
                    backgroundColor: `${tag.color}20`,
                    color: tag.color
                  }}>
                    {tag.name}
                  </span>
                ))}
              </div>
              <div style={{ color: colors.textTertiary }}>
                <Clock size={12} className="inline mr-1" />
                Last updated: {selectedRequest.timeAgo || 'Unknown'}
              </div>
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
              
              {endpointDetails.headers && endpointDetails.headers.length > 0 && (
                <div className="mb-8">
                  <h3 className="text-lg font-medium mb-4" style={{ color: colors.text }}>Headers</h3>
                  <div className="border rounded overflow-hidden" style={{ borderColor: colors.border }}>
                    <table className="w-full">
                      <thead style={{ backgroundColor: colors.tableHeader }}>
                        <tr>
                          <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Key</th>
                          <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Value</th>
                          <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Description</th>
                        </tr>
                      </thead>
                      <tbody>
                        {endpointDetails.headers.map((header, index) => (
                          <tr key={index} className="border-b last:border-b-0" style={{ 
                            borderColor: colors.borderLight,
                            backgroundColor: colors.tableRow
                          }}>
                            <td className="px-4 py-3 font-medium" style={{ color: colors.text }}>{header.key}</td>
                            <td className="px-4 py-3 font-mono text-sm" style={{ color: colors.textSecondary }}>
                              <code>{header.value}</code>
                            </td>
                            <td className="px-4 py-3 text-sm" style={{ color: colors.textSecondary }}>
                              {header.requiredBadge === 'Required' ? 'Required header' : 'Optional header'}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {endpointDetails.parameters && endpointDetails.parameters.length > 0 && (
                <div className="mb-8">
                  <h3 className="text-lg font-medium mb-4" style={{ color: colors.text }}>Parameters</h3>
                  <div className="border rounded overflow-hidden" style={{ borderColor: colors.border }}>
                    <table className="w-full">
                      <thead style={{ backgroundColor: colors.tableHeader }}>
                        <tr>
                          <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Name</th>
                          <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Type</th>
                          <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Required</th>
                          <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Description</th>
                        </tr>
                      </thead>
                      <tbody>
                        {endpointDetails.parameters.map((param, index) => (
                          <tr key={index} className="border-b last:border-b-0" style={{ 
                            borderColor: colors.borderLight,
                            backgroundColor: colors.tableRow
                          }}>
                            <td className="px-4 py-3 font-medium" style={{ color: colors.text }}>{param.name}</td>
                            <td className="px-4 py-3 text-sm" style={{ color: colors.textSecondary }}>{param.type}</td>
                            <td className="px-4 py-3">
                              <span className="text-xs px-2 py-1 rounded" style={{ 
                                backgroundColor: param.required ? `${colors.error}20` : `${colors.success}20`,
                                color: param.required ? colors.error : colors.success
                              }}>
                                {param.requiredBadge}
                              </span>
                            </td>
                            <td className="px-4 py-3 text-sm" style={{ color: colors.textSecondary }}>{param.description}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              <div>
                <h3 className="text-lg font-medium mb-4" style={{ color: colors.text }}>Description</h3>
                <div className="prose max-w-none" style={{ color: colors.textSecondary }}>
                  <p>
                    {endpointDetails.description || 'No description available.'}
                  </p>
                  {endpointDetails.requiresAuthentication && (
                    <p className="mt-3">
                      <strong>Authentication Required:</strong> This endpoint requires authentication.
                    </p>
                  )}
                  {endpointDetails.rateLimit && (
                    <p className="mt-3">
                      <strong>Rate Limits:</strong> {endpointDetails.formattedRateLimit}
                    </p>
                  )}
                  {endpointDetails.deprecated && (
                    <p className="mt-3">
                      <strong>Deprecated:</strong> This endpoint is deprecated and may be removed in future versions.
                    </p>
                  )}
                </div>
              </div>
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
                    <div key={index}>
                      <div className="flex items-center gap-3 mb-4">
                        <div className="flex items-center gap-2 px-3 py-1.5 rounded text-sm font-medium" style={{ 
                          backgroundColor: example.statusCode >= 200 && example.statusCode < 300 ? `${colors.success}20` : `${colors.error}20`,
                          color: example.statusCode >= 200 && example.statusCode < 300 ? colors.success : colors.error
                        }}>
                          {example.statusCode >= 200 && example.statusCode < 300 ? <CheckCircle size={12} /> : <XCircle size={12} />}
                          {example.statusBadge?.text || `Response ${example.statusCode}`}
                        </div>
                      </div>
                      
                      <div className="border rounded overflow-hidden" style={{ borderColor: colors.border }}>
                        <div className="p-4 border-b flex items-center justify-between" style={{ 
                          backgroundColor: colors.tableHeader,
                          borderColor: colors.border
                        }}>
                          <span className="text-sm font-medium" style={{ color: colors.text }}>JSON Response</span>
                          <button className="p-1.5 rounded hover:bg-opacity-50 transition-colors"
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
                    </div>
                  ))
                ) : (
                  <div className="text-center py-8" style={{ color: colors.textSecondary }}>
                    <Info size={24} className="mx-auto mb-4 opacity-50" />
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

      {/* Loading Overlay - Exactly matching APISecurity pattern */}
      <LoadingOverlay 
        isLoading={isLoading.initialLoad || isLoading.collections} 
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
          <div className="relative">
            <span className={`px-3 py-1.5 text-sm font-medium rounded transition-colors hover-lift`}>API Documentation</span>
          </div>

          <div className="flex items-center gap-1 -ml-7 text-nowrap">
            <span className={`px-3 py-1.5 text-sm font-medium rounded transition-colors hover-lift`}>API Documentation</span>
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
        </div>
      </div>

      {/* MAIN CONTENT */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left Sidebar - Collections */}
        {activeMainTab === 'Collections' && (
          <div className="w-80 border-r flex flex-col" style={{ 
            borderColor: colors.border
          }}>
            <div className="p-4 border-b" style={{ borderColor: colors.border }}>
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Collections</h3>
                <div className="flex gap-1">
                  {renderDebugButton()}
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
                        await fetchAPICollections();
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
                  {filteredCollections.map(collection => (
                    <div key={collection.id} className="mb-3">
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
                        
                        {(!collection.folders || collection.folders.length === 0) && isLoading.folders && (
                          <RefreshCw size={10} className="animate-spin" style={{ color: colors.textSecondary }} />
                        )}
                      </div>

                      {expandedCollections.includes(collection.id) && collection.folders && (
                        <>
                          {collection.folders.map(folder => (
                            <div key={folder.id} className="ml-4 mb-2">
                              <div className="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-opacity-50 transition-colors mb-1.5 cursor-pointer hover-lift"
                                onClick={() => toggleFolder(folder.id)}
                                style={{ backgroundColor: colors.hover }}>
                                {expandedFolders.includes(folder.id) ? (
                                  <ChevronDown size={11} style={{ color: colors.textSecondary }} />
                                ) : (
                                  <ChevronRight size={11} style={{ color: colors.textSecondary }} />
                                )}
                                <FolderOpen size={11} style={{ color: colors.textSecondary }} />
                                
                                <span className="text-sm flex-1 truncate" style={{ color: colors.text }}>
                                  {folder.name}
                                </span>
                                
                                {folder.isLoading && (
                                  <RefreshCw size={10} className="animate-spin" style={{ color: colors.textSecondary }} />
                                )}
                              </div>

                              {expandedFolders.includes(folder.id) && (
                                <div className="ml-6">
                                  {folder.isLoading ? (
                                    <div className="py-2 text-center">
                                      <RefreshCw size={12} className="animate-spin mx-auto mb-1" style={{ color: colors.textSecondary }} />
                                      <p className="text-xs" style={{ color: colors.textTertiary }}>Loading endpoints...</p>
                                    </div>
                                  ) : folder.error ? (
                                    <div className="py-2 text-center">
                                      <p className="text-xs" style={{ color: colors.error }}>{folder.error}</p>
                                      <button 
                                        className="text-xs mt-1 px-2 py-1 rounded hover:bg-opacity-50 transition-colors"
                                        onClick={() => fetchAPIEndpoints(collection.id, folder.id)}
                                        style={{ backgroundColor: colors.hover, color: colors.text }}
                                      >
                                        Retry
                                      </button>
                                    </div>
                                  ) : folder.requests && folder.requests.length > 0 ? (
                                    <>
                                      {folder.requests.map(request => (
                                        <div key={request.id} className="flex items-center gap-2 mb-1.5 group">
                                          <button
                                            onClick={() => handleSelectRequest(request, collection.id, folder.id)}
                                            className="flex items-center gap-2 text-sm text-left transition-colors flex-1 px-2 py-1.5 rounded hover:bg-opacity-50 hover-lift"
                                            style={{ 
                                              color: selectedRequest?.id === request.id ? colors.primary : colors.text,
                                              backgroundColor: selectedRequest?.id === request.id ? colors.selected : 'transparent'
                                            }}>
                                            <div className="w-2 h-2 rounded-full flex-shrink-0" style={{ 
                                              backgroundColor: colors.method[request.method] || colors.textSecondary
                                            }} />
                                            
                                            <span className="truncate">{request.name}</span>
                                          </button>
                                        </div>
                                      ))}
                                    </>
                                  ) : (
                                    <div className="py-2 text-center">
                                      <p className="text-xs" style={{ color: colors.textTertiary }}>No endpoints available</p>
                                    </div>
                                  )}
                                </div>
                              )}
                            </div>
                          ))}
                        </>
                      )}
                    </div>
                  ))}
                </>
              )}
            </div>
          </div>
        )}

        {/* Main Content Area */}
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
          {activeTab === 'documentation' && renderDocumentationContent()}
          
          {activeTab === 'changelog' && (
            <div className="flex-1 p-8">
              <div className="max-w-4xl mx-auto">
                <h2 className="text-2xl font-semibold mb-6" style={{ color: colors.text }}>API Changelog</h2>
                {changelog.length === 0 ? (
                  <div className="text-center py-8" style={{ color: colors.textSecondary }}>
                    <History size={48} className="mx-auto mb-4 opacity-50" />
                    <p>No changelog entries available.</p>
                    <p className="text-sm mt-2">Select a collection to view its changelog.</p>
                  </div>
                ) : (
                  <div className="space-y-6">
                    {changelog.map((entry, index) => (
                      <div key={index} className="border rounded p-6 hover:border-opacity-50 transition-colors hover-lift cursor-pointer"
                        style={{ borderColor: colors.border }}
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
          )}
        </div>

        {/* Right Code Panel */}
        {showCodePanel && renderCodePanel()}
      </div>

      {/* TOAST */}
      {renderToast()}
    </div>
  );
};

export default Documentation;