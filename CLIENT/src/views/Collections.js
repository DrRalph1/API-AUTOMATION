import React, { useState, useEffect, useRef } from 'react';
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
  File
} from 'lucide-react';

// Add syntax highlighting components
const SyntaxHighlighter = ({ language, code }) => {
  const highlightSyntax = (code, lang) => {
    if (lang === 'json') {
      return code
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, (match) => {
          let cls = 'text-blue-400'; // default for numbers
          if (/^"/.test(match)) {
            if (/:$/.test(match)) {
              cls = 'text-purple-400'; // keys
            } else {
              cls = 'text-green-400'; // strings
            }
          } else if (/true|false/.test(match)) {
            cls = 'text-orange-400'; // booleans
          } else if (/null/.test(match)) {
            cls = 'text-red-400'; // null
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

const Collections = ({ theme, isDark, customTheme, toggleTheme }) => {

  // Matching the exact color scheme from the first component
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
    
    // Method colors - EXACTLY matching Dashboard
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
    
    // Accent colors - EXACTLY matching Dashboard
    accentPurple: 'rgb(167 139 250)',
    accentPink: 'rgb(244 114 182)',
    accentCyan: 'rgb(34 211 238)',
    
    // Gradient - updated to match the new color scheme
    gradient: 'from-blue-500/20 via-violet-500/20 to-orange-500/20'
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
    codeBg: '#f1f5f9',
    
    // Connection status for light mode
    connectionOnline: '#10b981',
    connectionOffline: '#ef4444',
    connectionIdle: '#f59e0b',
    
    // Accent colors for light mode
    accentPurple: '#8b5cf6',
    accentPink: '#ec4899',
    accentCyan: '#06b6d4',
    
    gradient: 'from-blue-400/20 via-violet-400/20 to-orange-400/20'
  };

  // State
  const [collections, setCollections] = useState([
    {
      id: 'col-1',
      name: 'E-Commerce API',
      description: 'Complete e-commerce platform endpoints',
      isExpanded: true,
      isFavorite: true,
      isEditing: false,
      createdAt: '2024-01-15T10:30:00Z',
      requestsCount: 12,
      variables: [
        { id: 'var-1', key: 'baseUrl', value: '{{base_url}}', type: 'string', enabled: true }
      ],
      folders: [
        {
          id: 'folder-1',
          name: 'Authentication',
          description: 'User authentication and authorization',
          isExpanded: true,
          isEditing: false,
          requests: [
            {
              id: 'req-1',
              name: 'Login',
              method: 'POST',
              url: '{{baseUrl}}/api/v1/auth/login',
              description: 'Authenticate user with email and password',
              isEditing: false,
              status: 'saved',
              lastModified: '2024-01-15T09:45:00Z',
              auth: { type: 'noauth' },
              params: [
                { id: 'p-1', key: 'test_param', value: 'test_value', description: 'Test parameter', enabled: true }
              ],
              headers: [
                { id: 'h-1', key: 'Content-Type', value: 'application/json', enabled: true, description: '' }
              ],
              body: JSON.stringify({
                email: 'user@example.com',
                password: 'password123'
              }, null, 2),
              tests: "pm.test('Status code is 200', function() {\n  pm.response.to.have.status(200);\n});",
              preRequestScript: '',
              isSaved: true
            },
            {
              id: 'req-2',
              name: 'Refresh Token',
              method: 'POST',
              url: '{{baseUrl}}/api/v1/auth/refresh',
              description: 'Refresh access token',
              isEditing: false,
              status: 'saved',
              lastModified: '2024-01-14T14:20:00Z',
              auth: { type: 'bearer', token: '{{access_token}}' },
              params: [],
              headers: [
                { id: 'h-2', key: 'Content-Type', value: 'application/json', enabled: true, description: '' }
              ],
              body: JSON.stringify({
                refresh_token: '{{refresh_token}}'
              }, null, 2),
              tests: '',
              preRequestScript: '',
              isSaved: true
            }
          ]
        },
        {
          id: 'folder-2',
          name: 'Products',
          description: 'Product management endpoints',
          isExpanded: true,
          isEditing: false,
          requests: [
            {
              id: 'req-3',
              name: 'Get Products',
              method: 'GET',
              url: '{{baseUrl}}/api/v1/products',
              description: 'Retrieve list of products',
              isEditing: false,
              status: 'saved',
              lastModified: '2024-01-15T08:15:00Z',
              auth: { type: 'bearer', token: '{{access_token}}' },
              params: [
                { id: 'p-1', key: 'page', value: '1', description: 'Page number', enabled: true },
                { id: 'p-2', key: 'limit', value: '20', description: 'Items per page', enabled: true },
                { id: 'p-3', key: 'category', value: '', description: 'Filter by category', enabled: false }
              ],
              headers: [
                { id: 'h-3', key: 'Authorization', value: 'Bearer {{access_token}}', enabled: true, description: '' }
              ],
              body: '',
              tests: "pm.test('Status code is 200', function() {\n  pm.response.to.have.status(200);\n});",
              preRequestScript: '',
              isSaved: true
            },
            {
              id: 'req-4',
              name: 'Create Product',
              method: 'POST',
              url: '{{baseUrl}}/api/v1/products',
              description: 'Create a new product',
              isEditing: false,
              status: 'saved',
              lastModified: '2024-01-14T16:45:00Z',
              auth: { type: 'bearer', token: '{{access_token}}' },
              params: [],
              headers: [
                { id: 'h-4', key: 'Authorization', value: 'Bearer {{access_token}}', enabled: true, description: '' },
                { id: 'h-5', key: 'Content-Type', value: 'application/json', enabled: true, description: '' }
              ],
              body: JSON.stringify({
                name: 'New Product',
                price: 99.99,
                category: 'electronics'
              }, null, 2),
              tests: "pm.test('Status code is 201', function() {\n  pm.response.to.have.status(201);\n});",
              preRequestScript: '',
              isSaved: true
            }
          ]
        }
      ]
    },
    {
      id: 'col-2',
      name: 'Social Media API',
      description: 'Social media platform endpoints',
      isExpanded: false,
      isFavorite: false,
      isEditing: false,
      createdAt: '2024-01-10T14:20:00Z',
      requestsCount: 8,
      variables: [
        { id: 'var-2', key: 'apiUrl', value: '{{api_url}}', type: 'string', enabled: true }
      ],
      folders: [
        {
          id: 'folder-3',
          name: 'Posts',
          description: 'Post management endpoints',
          isExpanded: false,
          isEditing: false,
          requests: [
            {
              id: 'req-5',
              name: 'Create Post',
              method: 'POST',
              url: '{{apiUrl}}/api/v1/posts',
              description: 'Create a new post',
              isEditing: false,
              status: 'saved',
              lastModified: '2024-01-12T11:30:00Z',
              auth: { type: 'bearer', token: '{{access_token}}' },
              params: [],
              headers: [
                { id: 'h-6', key: 'Content-Type', value: 'application/json', enabled: true, description: '' }
              ],
              body: JSON.stringify({
                content: 'Hello world!',
                media_urls: ['https://example.com/image.jpg']
              }, null, 2),
              tests: "pm.test('Status code is 201', function() {\n  pm.response.to.have.status(201);\n});",
              preRequestScript: '',
              isSaved: true
            }
          ]
        }
      ]
    }
  ]);

  const [selectedRequest, setSelectedRequest] = useState(null);
  const [activeTab, setActiveTab] = useState('params');
  const [requestTabs, setRequestTabs] = useState([
    { id: 'req-1', name: 'Login', method: 'POST', collectionId: 'col-1', folderId: 'folder-1', isActive: true }
  ]);
  
  // Right panel states
  const [showCodePanel, setShowCodePanel] = useState(true);
  const [showMockServers, setShowMockServers] = useState(false);
  const [showMonitors, setShowMonitors] = useState(false);
  const [showEnvironments, setShowEnvironments] = useState(false);
  const [showAPIs, setShowAPIs] = useState(false);
  
  // Code panel state
  const [selectedLanguage, setSelectedLanguage] = useState('curl');
  const [showLanguageDropdown, setShowLanguageDropdown] = useState(false);
  
  const [environments, setEnvironments] = useState([
    { id: 'env-1', name: 'No Environment', isActive: true, variables: [] },
    { id: 'env-2', name: 'Development', isActive: false, variables: [
      { id: 'env-var-1', key: 'base_url', value: 'https://api.dev.example.com', enabled: true },
      { id: 'env-var-2', key: 'access_token', value: 'dev_token_123', enabled: true }
    ]},
    { id: 'env-3', name: 'Production', isActive: false, variables: [
      { id: 'env-var-3', key: 'base_url', value: 'https://api.example.com', enabled: true },
      { id: 'env-var-4', key: 'access_token', value: 'prod_token_456', enabled: true }
    ]}
  ]);
  const [activeEnvironment, setActiveEnvironment] = useState('env-1');
  
  const [requestMethod, setRequestMethod] = useState('POST');
  const [requestUrl, setRequestUrl] = useState('{{baseUrl}}/api/v1/auth/login');
  const [requestParams, setRequestParams] = useState([
    { id: 'p-1', key: 'test_param', value: 'test_value', description: 'Test parameter', enabled: true }
  ]);
  const [requestHeaders, setRequestHeaders] = useState([
    { id: 'h-1', key: 'Content-Type', value: 'application/json', enabled: true, description: '' }
  ]);
  const [requestBody, setRequestBody] = useState(JSON.stringify({
    email: 'user@example.com',
    password: 'password123'
  }, null, 2));
  const [requestBodyType, setRequestBodyType] = useState('raw');
  const [rawBodyType, setRawBodyType] = useState('json');
  const [formData, setFormData] = useState([
    { id: 'form-1', key: 'username', value: 'testuser', type: 'text', enabled: true },
    { id: 'form-2', key: 'profile_pic', value: '', type: 'file', enabled: true }
  ]);
  const [urlEncodedData, setUrlEncodedData] = useState([
    { id: 'url-1', key: 'grant_type', value: 'password', description: 'OAuth grant type', enabled: true }
  ]);
  const [binaryFile, setBinaryFile] = useState(null);
  const [graphqlQuery, setGraphqlQuery] = useState('query {\n  getUser(id: 1) {\n    id\n    name\n    email\n  }\n}');
  const [graphqlVariables, setGraphqlVariables] = useState('{\n  "id": 1\n}');
  const [response, setResponse] = useState(null);
  const [isSending, setIsSending] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState([
    { id: 'notif-1', title: 'Collection Updated', message: 'E-Commerce API was updated by you', time: '2 hours ago', read: false },
    { id: 'notif-2', title: 'Monitor Alert', message: 'API monitor detected slow response time', time: '1 day ago', read: false },
    { id: 'notif-3', title: 'Team Invite', message: 'John Doe invited you to collaborate', time: '2 days ago', read: true }
  ]);
  const [authType, setAuthType] = useState('noauth');
  const [showAuthDropdown, setShowAuthDropdown] = useState(false);
  const [authConfig, setAuthConfig] = useState({ 
    token: '', 
    username: '', 
    password: '', 
    key: '', 
    value: '', 
    addTo: 'header',
    tokenType: 'Bearer'
  });
  
  // Resizable response panel
  const [responseHeight, setResponseHeight] = useState(300);
  const [isResizing, setIsResizing] = useState(false);
  const [responseView, setResponseView] = useState('raw');
  const responseRef = useRef(null);

  // Modals state
  const [showImportModal, setShowImportModal] = useState(false);
  const [showShareModal, setShowShareModal] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [globalSearchQuery, setGlobalSearchQuery] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showToken, setShowToken] = useState(false);

  // Toast state
  const [toast, setToast] = useState(null);

  // Initialize
  useEffect(() => {
    if (collections[0]?.folders[0]?.requests[0]) {
      handleSelectRequest(collections[0].folders[0].requests[0], 'col-1', 'folder-1');
    }
  }, []);

  // Handle mouse move for resizing
  useEffect(() => {
    const handleMouseMove = (e) => {
      if (!isResizing) return;
      
      const containerHeight = responseRef.current?.parentElement?.parentElement?.offsetHeight || 600;
      const newHeight = containerHeight - e.clientY;
      const minHeight = 100;
      const maxHeight = containerHeight - 100;
      
      if (newHeight >= minHeight && newHeight <= maxHeight) {
        setResponseHeight(newHeight);
      }
    };

    const handleMouseUp = () => {
      setIsResizing(false);
    };

    if (isResizing) {
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
    }

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };
  }, [isResizing]);

  // Show toast
  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 2000);
  };

  // Get method color
  const getMethodColor = (method) => {
    return colors.method[method] || colors.textSecondary;
  };

  // Select request
  const handleSelectRequest = (request, collectionId, folderId) => {
    const requestWithContext = { ...request, collectionId, folderId };
    setSelectedRequest(requestWithContext);
    setRequestMethod(request.method);
    setRequestUrl(request.url);
    setRequestBody(request.body || '');
    setRequestParams(request.params || []);
    setRequestHeaders(request.headers || []);
    setAuthType(request.auth?.type || 'noauth');
    setAuthConfig(request.auth || {});
    setResponse(null);
    
    // Update tabs
    setRequestTabs(tabs => {
      const existingTab = tabs.find(t => t.id === request.id);
      if (existingTab) {
        return tabs.map(t => ({ ...t, isActive: t.id === request.id }));
      } else {
        return tabs.map(t => ({ ...t, isActive: false }))
          .concat({ id: request.id, name: request.name, method: request.method, collectionId, folderId, isActive: true });
      }
    });
  };

  // Save request changes
  const saveRequestChanges = () => {
    if (!selectedRequest) return;
    
    const { collectionId, folderId, id: requestId } = selectedRequest;
    
    setCollections(collections.map(col => 
      col.id === collectionId ? {
        ...col,
        folders: col.folders.map(folder =>
          folder.id === folderId ? {
            ...folder,
            requests: folder.requests.map(req =>
              req.id === requestId ? {
                ...req,
                method: requestMethod,
                url: requestUrl,
                params: requestParams,
                headers: requestHeaders,
                body: requestBody,
                auth: { type: authType, ...authConfig },
                lastModified: new Date().toISOString(),
                isSaved: true
              } : req
            )
          } : folder
        )
      } : col
    ));
    
    showToast('Request saved successfully', 'success');
  };

  // Update collection name
  const updateCollectionName = (collectionId, newName) => {
    if (!newName.trim()) return;
    setCollections(collections.map(col => 
      col.id === collectionId ? { ...col, name: newName, isEditing: false } : col
    ));
    showToast('Collection name updated', 'success');
  };

  // Update folder name
  const updateFolderName = (collectionId, folderId, newName) => {
    if (!newName.trim()) return;
    setCollections(collections.map(col => 
      col.id === collectionId ? {
        ...col,
        folders: col.folders.map(folder =>
          folder.id === folderId ? { ...folder, name: newName, isEditing: false } : folder
        )
      } : col
    ));
    showToast('Folder name updated', 'success');
  };

  // Update request name
  const updateRequestName = (collectionId, folderId, requestId, newName) => {
    if (!newName.trim()) return;
    setCollections(collections.map(col => 
      col.id === collectionId ? {
        ...col,
        folders: col.folders.map(folder =>
          folder.id === folderId ? {
            ...folder,
            requests: folder.requests.map(req =>
              req.id === requestId ? { ...req, name: newName, isEditing: false } : req
            )
          } : folder
        )
      } : col
    ));
    
    // Update active tab name
    setRequestTabs(tabs => tabs.map(tab =>
      tab.id === requestId ? { ...tab, name: newName } : tab
    ));
    
    showToast('Request name updated', 'success');
  };

  // Toggle collection expansion
  const toggleCollection = (collectionId) => {
    setCollections(collections.map(col => 
      col.id === collectionId ? { ...col, isExpanded: !col.isExpanded } : col
    ));
  };

  // Toggle folder expansion
  const toggleFolder = (collectionId, folderId) => {
    setCollections(collections.map(col => 
      col.id === collectionId ? {
        ...col,
        folders: col.folders.map(folder =>
          folder.id === folderId ? { ...folder, isExpanded: !folder.isExpanded } : folder
        )
      } : col
    ));
  };

  // Toggle favorite
  const toggleFavorite = (collectionId) => {
    setCollections(collections.map(col => 
      col.id === collectionId ? { ...col, isFavorite: !col.isFavorite } : col
    ));
    const collection = collections.find(c => c.id === collectionId);
    showToast(collection.isFavorite ? 'Removed from favorites' : 'Added to favorites', 'success');
  };

  // Filter collections based on search
  const filteredCollections = collections.filter(collection => {
    if (!searchQuery) return true;
    
    const query = searchQuery.toLowerCase();
    return (
      collection.name.toLowerCase().includes(query) ||
      collection.description?.toLowerCase().includes(query) ||
      collection.folders.some(folder => 
        folder.name.toLowerCase().includes(query) ||
        folder.description?.toLowerCase().includes(query) ||
        folder.requests.some(request => 
          request.name.toLowerCase().includes(query) ||
          request.description?.toLowerCase().includes(query) ||
          request.url.toLowerCase().includes(query)
        )
      )
    );
  });

  // Add new collection
  const addNewCollection = (name, description = '') => {
    const newCollection = {
      id: `col-${Date.now()}`,
      name: name || 'New Collection',
      description,
      isExpanded: true,
      isFavorite: false,
      isEditing: false,
      createdAt: new Date().toISOString(),
      requestsCount: 0,
      variables: [],
      folders: []
    };
    
    setCollections([...collections, newCollection]);
    setShowCreateModal(false);
    showToast('Collection created successfully', 'success');
  };

  // Add new folder
  const addNewFolder = (collectionId) => {
    const newFolder = {
      id: `folder-${Date.now()}`,
      name: 'New Folder',
      description: '',
      isExpanded: true,
      isEditing: false,
      requests: []
    };
    
    setCollections(collections.map(col => 
      col.id === collectionId ? { ...col, folders: [...col.folders, newFolder] } : col
    ));
    
    showToast('Folder added', 'success');
  };

  // Add new request
  const addNewRequest = (collectionId, folderId) => {
    const newRequest = {
      id: `req-${Date.now()}`,
      name: 'New Request',
      method: 'GET',
      url: '',
      description: '',
      isEditing: false,
      status: 'unsaved',
      lastModified: new Date().toISOString(),
      auth: { type: 'noauth' },
      params: [],
      headers: [],
      body: '',
      tests: '',
      preRequestScript: '',
      isSaved: false
    };
    
    setCollections(collections.map(col => 
      col.id === collectionId ? {
        ...col,
        folders: col.folders.map(folder =>
          folder.id === folderId ? { ...folder, requests: [...folder.requests, newRequest] } : folder
        )
      } : col
    ));
    
    handleSelectRequest(newRequest, collectionId, folderId);
    showToast('Request added', 'success');
  };

  // Add param
  const addParam = () => {
    const newParam = { 
      id: `param-${Date.now()}`, 
      key: '', 
      value: '', 
      description: '', 
      enabled: true 
    };
    setRequestParams([...requestParams, newParam]);
  };

  // Update param
  const updateParam = (id, field, value) => {
    setRequestParams(params => 
      params.map(param => 
        param.id === id ? { ...param, [field]: value } : param
      )
    );
  };

  // Delete param
  const deleteParam = (id) => {
    setRequestParams(params => params.filter(param => param.id !== id));
  };

  // Add header
  const addHeader = () => {
    const newHeader = { 
      id: `header-${Date.now()}`, 
      key: '', 
      value: '', 
      description: '', 
      enabled: true 
    };
    setRequestHeaders([...requestHeaders, newHeader]);
  };

  // Update header
  const updateHeader = (id, field, value) => {
    setRequestHeaders(headers => 
      headers.map(header => 
        header.id === id ? { ...header, [field]: value } : header
      )
    );
  };

  // Delete header
  const deleteHeader = (id) => {
    setRequestHeaders(headers => headers.filter(header => header.id !== id));
  };

  // Send request
  const sendRequest = () => {
    setIsSending(true);
    setResponse(null);
    
    // Validate URL
    if (!requestUrl.trim()) {
      showToast('Please enter a URL', 'error');
      setIsSending(false);
      return;
    }
    
    // Simulate API call
    setTimeout(() => {
      setResponse({
        status: 200,
        statusText: 'OK',
        time: `${Math.floor(Math.random() * 200) + 100}ms`,
        size: `${(Math.random() * 3 + 1).toFixed(1)}KB`,
        headers: [
          { key: 'Content-Type', value: 'application/json' },
          { key: 'X-RateLimit-Limit', value: '1000' },
          { key: 'X-RateLimit-Remaining', value: '999' },
          { key: 'X-Powered-By', value: 'Express' },
          { key: 'Date', value: new Date().toUTCString() }
        ],
        body: JSON.stringify({
          success: true,
          data: {
            token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
            user: { 
              id: 1, 
              email: 'user@example.com',
              name: 'John Doe',
              role: 'admin'
            }
          },
          message: 'Request successful',
          timestamp: new Date().toISOString()
        }, null, 2)
      });
      setIsSending(false);
      showToast('Request completed successfully', 'success');
    }, 1000);
  };

  // Generate code snippet
  const generateCodeSnippet = () => {
    const languages = {
      curl: `curl -X ${requestMethod} "${requestUrl}" \\\n` +
            requestHeaders.map(h => `  -H "${h.key}: ${h.value}"`).join(' \\\n') +
            (requestBody && requestMethod !== 'GET' ? ` \\\n  -d '${requestBody}'` : ''),
      
      javascript: `fetch("${requestUrl}", {\n` +
                 `  method: "${requestMethod}",\n` +
                 `  headers: {\n` +
                 requestHeaders.map(h => `    "${h.key}": "${h.value}"`).join(',\n') + '\n' +
                 `  },\n` +
                 (requestBody && requestMethod !== 'GET' ? `  body: ${requestBody}\n` : '') +
                 `})\n.then(response => response.json())\n.then(data => console.log(data))\n.catch(error => console.error('Error:', error));`,
      
      python: `import requests\n\n` +
              `headers = {\n` +
              requestHeaders.map(h => `    "${h.key}": "${h.value}"`).join(',\n') + '\n' +
              `}\n\n` +
              (requestBody && requestMethod !== 'GET' ? `data = ${requestBody}\n\n` : '') +
              `response = requests.${requestMethod.toLowerCase()}("${requestUrl}", ` +
              (requestBody && requestMethod !== 'GET' ? `json=data, ` : '') +
              `headers=headers)\n` +
              `print(response.json())`,
      
      nodejs: `const https = require('https');\n\n` +
              `const options = {\n` +
              `  hostname: 'api.example.com',\n` +
              `  port: 443,\n` +
              `  path: '/api/v1/auth/login',\n` +
              `  method: '${requestMethod}',\n` +
              `  headers: {\n` +
              requestHeaders.map(h => `    "${h.key}": "${h.value}"`).join(',\n') + '\n' +
              `  }\n` +
              `};\n\n` +
              `const req = https.request(options, (res) => {\n` +
              `  let data = '';\n` +
              `  res.on('data', (chunk) => {\n` +
              `    data += chunk;\n` +
              `  });\n` +
              `  res.on('end', () => {\n` +
              `    console.log(JSON.parse(data));\n` +
              `  });\n` +
              `});\n\n` +
              `req.on('error', (error) => {\n` +
              `  console.error(error);\n` +
              `});\n\n` +
              (requestBody && requestMethod !== 'GET' ? `req.write(${requestBody});\n` : '') +
              `req.end();`,
      
      php: `<?php\n\n` +
           `$ch = curl_init();\n\n` +
           `curl_setopt($ch, CURLOPT_URL, "${requestUrl}");\n` +
           `curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);\n` +
           `curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "${requestMethod}");\n\n` +
           `$headers = [\n` +
           requestHeaders.map(h => `  "${h.key}: ${h.value}"`).join(',\n') + '\n' +
           `];\n` +
           `curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);\n\n` +
           (requestBody && requestMethod !== 'GET' ? `curl_setopt($ch, CURLOPT_POSTFIELDS, ${requestBody});\n\n` : '') +
           `$response = curl_exec($ch);\n` +
           `curl_close($ch);\n\n` +
           `echo $response;\n` +
           `?>`,
      
      ruby: `require 'net/http'\n` +
            `require 'uri'\n` +
            `require 'json'\n\n` +
            `uri = URI.parse("${requestUrl}")\n\n` +
            `http = Net::HTTP.new(uri.host, uri.port)\n` +
            `http.use_ssl = true if uri.scheme == 'https'\n\n` +
            `request = Net::HTTP::${requestMethod.charAt(0) + requestMethod.slice(1).toLowerCase()}.new(uri.request_uri)\n\n` +
            requestHeaders.map(h => `request["${h.key}"] = "${h.value}"`).join('\n') + '\n\n' +
            (requestBody && requestMethod !== 'GET' ? `request.body = ${requestBody}.to_json\n\n` : '') +
            `response = http.request(request)\n` +
            `puts response.body`,
      
      java: `import java.net.HttpURLConnection;\n` +
            `import java.net.URL;\n` +
            `import java.io.BufferedReader;\n` +
            `import java.io.InputStreamReader;\n` +
            `import java.io.OutputStream;\n\n` +
            `public class Main {\n` +
            `  public static void main(String[] args) throws Exception {\n` +
            `    URL url = new URL("${requestUrl}");\n` +
            `    HttpURLConnection conn = (HttpURLConnection) url.openConnection();\n` +
            `    conn.setRequestMethod("${requestMethod}");\n\n` +
            requestHeaders.map(h => `    conn.setRequestProperty("${h.key}", "${h.value}");`).join('\n') + '\n\n' +
            (requestBody && requestMethod !== 'GET' ? `    conn.setDoOutput(true);\n` +
            `    try(OutputStream os = conn.getOutputStream()) {\n` +
            `      byte[] input = ${requestBody}.getBytes("utf-8");\n` +
            `      os.write(input, 0, input.length);\n` +
            `    }\n\n` : '') +
            `    try(BufferedReader br = new BufferedReader(\n` +
            `      new InputStreamReader(conn.getInputStream(), "utf-8"))) {\n` +
            `      StringBuilder response = new StringBuilder();\n` +
            `      String responseLine;\n` +
            `      while ((responseLine = br.readLine()) != null) {\n` +
            `        response.append(responseLine.trim());\n` +
            `      }\n` +
            `      System.out.println(response.toString());\n` +
            `    }\n` +
            `  }\n` +
            `}`
    };
    
    return languages[selectedLanguage] || languages.curl;
  };

  // Render Code panel with syntax highlighting
  const renderCodePanel = () => {
    const languages = [
      { id: 'curl', name: 'cURL', icon: <Terminal size={14} /> },
      { id: 'javascript', name: 'JavaScript', icon: <FileCode size={14} /> },
      { id: 'python', name: 'Python', icon: <Code size={14} /> },
      { id: 'nodejs', name: 'Node.js', icon: <Server size={14} /> },
      { id: 'php', name: 'PHP', icon: <Box size={14} /> },
      { id: 'ruby', name: 'Ruby', icon: <Package size={14} /> },
      { id: 'java', name: 'Java', icon: <Coffee size={14} /> },
      { id: 'csharp', name: 'C#', icon: <Hash size={14} /> },
      { id: 'go', name: 'Go', icon: <Terminal size={14} /> },
      { id: 'swift', name: 'Swift', icon: <Terminal size={14} /> },
      { id: 'kotlin', name: 'Kotlin', icon: <Terminal size={14} /> }
    ];

    const currentLanguage = languages.find(lang => lang.id === selectedLanguage);
    
    return (
      <div className="w-80 border-l flex flex-col" style={{ 
        backgroundColor: colors.sidebar,
        borderColor: colors.border
      }}>
        <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Code</h3>
          <button onClick={() => setShowCodePanel(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}>
            <X size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        <div className="relative px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <button
            onClick={() => setShowLanguageDropdown(!showLanguageDropdown)}
            className="w-full px-3 py-2 rounded text-sm font-medium flex items-center justify-between hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover, color: colors.text }}
          >
            <div className="flex items-center gap-2">
              {currentLanguage?.icon}
              <span>{currentLanguage?.name}</span>
            </div>
            <ChevronDown size={14} style={{ color: colors.textSecondary }} />
          </button>

          {showLanguageDropdown && (
            <div className="absolute left-4 right-4 top-full mt-1 py-2 rounded shadow-lg z-50 border"
              style={{ 
                backgroundColor: colors.dropdownBg,
                borderColor: colors.border,
                maxHeight: '300px',
                overflowY: 'auto'
              }}>
              {languages.map(lang => (
                <button
                  key={lang.id}
                  onClick={() => {
                    setSelectedLanguage(lang.id);
                    setShowLanguageDropdown(false);
                  }}
                  className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ 
                    backgroundColor: selectedLanguage === lang.id ? colors.selected : 'transparent',
                    color: selectedLanguage === lang.id ? colors.primary : colors.text
                  }}
                >
                  {lang.icon}
                  {lang.name}
                  {selectedLanguage === lang.id && <Check size={14} className="ml-auto" />}
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="flex-1 overflow-auto p-4">
          <SyntaxHighlighter 
            language={selectedLanguage === 'curl' ? 'bash' : selectedLanguage}
            code={generateCodeSnippet()}
          />
        </div>

        <div className="p-4 border-t" style={{ borderColor: colors.border }}>
          <button 
            className="w-full py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center justify-center gap-2 hover-lift"
            onClick={() => {
              navigator.clipboard.writeText(generateCodeSnippet());
              showToast('Copied to clipboard!', 'success');
            }}
            style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
            <Copy size={12} />
            Copy to Clipboard
          </button>
        </div>
      </div>
    );
  };

  // Render Authorization Tab
  const renderAuthTab = () => {
    const authTypes = [
      { id: 'noauth', name: 'No Auth', icon: <Globe size={16} />, description: 'No authorization required' },
      { id: 'bearer', name: 'Bearer Token', icon: <Key size={16} />, description: 'Token-based authentication' },
      { id: 'basic', name: 'Basic Auth', icon: <Shield size={16} />, description: 'Username and password' },
      { id: 'apikey', name: 'API Key', icon: <Key size={16} />, description: 'API key authentication' },
      { id: 'oauth2', name: 'OAuth 2.0', icon: <ShieldCheck size={16} />, description: 'OAuth 2.0 protocol' }
    ];

    const currentAuthType = authTypes.find(type => type.id === authType);

    return (
      <div className="p-4 h-full overflow-auto">
        <div className="mb-4">
          <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
            Type
          </label>
          <div className="relative">
            <button
              onClick={() => setShowAuthDropdown(!showAuthDropdown)}
              className="w-full px-3 py-2 rounded text-sm flex items-center justify-between hover:bg-opacity-50 transition-colors border hover-lift"
              style={{ 
                backgroundColor: colors.inputBg,
                borderColor: colors.border,
                color: colors.text
              }}>
              <div className="flex items-center gap-2">
                {currentAuthType?.icon}
                <span>{currentAuthType?.name}</span>
              </div>
              <ChevronDown size={14} style={{ color: colors.textSecondary }} />
            </button>

            {showAuthDropdown && (
              <div className="absolute left-0 right-0 top-full mt-1 py-2 rounded shadow-lg z-50 border"
                style={{ 
                  backgroundColor: colors.dropdownBg,
                  borderColor: colors.border,
                  maxHeight: '300px',
                  overflowY: 'auto'
                }}>
                {authTypes.map(type => (
                  <button
                    key={type.id}
                    onClick={() => {
                      setAuthType(type.id);
                      setShowAuthDropdown(false);
                      setAuthConfig({ type: type.id });
                    }}
                    className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors hover-lift"
                    style={{ 
                      backgroundColor: authType === type.id ? colors.selected : 'transparent',
                      color: authType === type.id ? colors.primary : colors.text
                    }}
                  >
                    {type.icon}
                    {type.name}
                    {authType === type.id && <Check size={14} className="ml-auto" />}
                  </button>
                ))}
              </div>
            )}
          </div>
          {currentAuthType && (
            <p className="text-xs mt-1" style={{ color: colors.textSecondary }}>
              {currentAuthType.description}
            </p>
          )}
        </div>

        {/* Dynamic auth forms */}
        <div className="mt-6">
          {authType === 'bearer' && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
                  Token
                </label>
                <div className="relative">
                  <input
                    type={showToken ? "text" : "password"}
                    value={authConfig.token || ''}
                    onChange={(e) => setAuthConfig({ ...authConfig, token: e.target.value })}
                    className="w-full px-3 py-2 border rounded text-sm focus:outline-none pr-10 hover-lift"
                    style={{
                      backgroundColor: colors.inputBg,
                      borderColor: colors.border,
                      color: colors.text
                    }}
                    placeholder="Enter bearer token"
                  />
                  <button
                    type="button"
                    onClick={() => setShowToken(!showToken)}
                    className="absolute right-2 top-1/2 transform -translate-y-1/2 p-1 hover-lift"
                    style={{ color: colors.textSecondary }}
                  >
                    {showToken ? <EyeOff size={16} /> : <EyeIcon size={16} />}
                  </button>
                </div>
                <div className="mt-3">
                  <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
                    Prefix
                  </label>
                  <select
                    value={authConfig.tokenType || 'Bearer'}
                    onChange={(e) => setAuthConfig({ ...authConfig, tokenType: e.target.value })}
                    className="w-full px-3 py-2 border rounded text-sm focus:outline-none hover-lift"
                    style={{
                      backgroundColor: colors.inputBg,
                      borderColor: colors.border,
                      color: colors.text
                    }}>
                    <option value="Bearer">Bearer</option>
                    <option value="Token">Token</option>
                    <option value="JWT">JWT</option>
                    <option value="Basic">Basic</option>
                  </select>
                </div>
                <p className="text-xs mt-2" style={{ color: colors.textSecondary }}>
                  Will be sent as: {authConfig.tokenType || 'Bearer'} [your_token]
                </p>
              </div>
            </div>
          )}

          {authType === 'basic' && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
                  Username
                </label>
                <input
                  type="text"
                  value={authConfig.username || ''}
                  onChange={(e) => setAuthConfig({ ...authConfig, username: e.target.value })}
                  className="w-full px-3 py-2 border rounded text-sm focus:outline-none hover-lift"
                  style={{
                    backgroundColor: colors.inputBg,
                    borderColor: colors.border,
                    color: colors.text
                  }}
                  placeholder="Enter username"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
                  Password
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? "text" : "password"}
                    value={authConfig.password || ''}
                    onChange={(e) => setAuthConfig({ ...authConfig, password: e.target.value })}
                    className="w-full px-3 py-2 border rounded text-sm focus:outline-none pr-10 hover-lift"
                    style={{
                      backgroundColor: colors.inputBg,
                      borderColor: colors.border,
                      color: colors.text
                    }}
                    placeholder="Enter password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-2 top-1/2 transform -translate-y-1/2 p-1 hover-lift"
                    style={{ color: colors.textSecondary }}
                  >
                    {showPassword ? <EyeOff size={16} /> : <EyeIcon size={16} />}
                  </button>
                </div>
              </div>
              <div className="flex items-center gap-2 mt-4">
                <input
                  type="checkbox"
                  id="show-header"
                  className="rounded-sm hover-lift"
                  style={{
                    borderColor: colors.border,
                    backgroundColor: colors.inputBg
                  }}
                />
                <label htmlFor="show-header" className="text-sm" style={{ color: colors.text }}>
                  Show header in request
                </label>
              </div>
            </div>
          )}

          {authType === 'apikey' && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
                    Key
                  </label>
                  <input
                    type="text"
                    value={authConfig.key || ''}
                    onChange={(e) => setAuthConfig({ ...authConfig, key: e.target.value })}
                    className="w-full px-3 py-2 border rounded text-sm focus:outline-none hover-lift"
                    style={{
                      backgroundColor: colors.inputBg,
                      borderColor: colors.border,
                      color: colors.text
                    }}
                    placeholder="e.g., X-API-Key"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
                    Value
                  </label>
                  <div className="relative">
                    <input
                      type={showToken ? "text" : "password"}
                      value={authConfig.value || ''}
                      onChange={(e) => setAuthConfig({ ...authConfig, value: e.target.value })}
                      className="w-full px-3 py-2 border rounded text-sm focus:outline-none pr-10 hover-lift"
                      style={{
                        backgroundColor: colors.inputBg,
                        borderColor: colors.border,
                        color: colors.text
                      }}
                      placeholder="Enter API key"
                    />
                    <button
                      type="button"
                      onClick={() => setShowToken(!showToken)}
                      className="absolute right-2 top-1/2 transform -translate-y-1/2 p-1 hover-lift"
                      style={{ color: colors.textSecondary }}
                    >
                      {showToken ? <EyeOff size={16} /> : <EyeIcon size={16} />}
                    </button>
                  </div>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
                  Add to
                </label>
                <select
                  value={authConfig.addTo || 'header'}
                  onChange={(e) => setAuthConfig({ ...authConfig, addTo: e.target.value })}
                  className="w-full px-3 py-2 border rounded text-sm focus:outline-none hover-lift"
                  style={{
                    backgroundColor: colors.inputBg,
                    borderColor: colors.border,
                    color: colors.text
                  }}>
                  <option value="header">Header</option>
                  <option value="queryParams">Query Params</option>
                </select>
              </div>
            </div>
          )}

          {authType === 'oauth2' && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
                  Access Token
                </label>
                <div className="relative">
                  <input
                    type={showToken ? "text" : "password"}
                    value={authConfig.token || ''}
                    onChange={(e) => setAuthConfig({ ...authConfig, token: e.target.value })}
                    className="w-full px-3 py-2 border rounded text-sm focus:outline-none pr-10 hover-lift"
                    style={{
                      backgroundColor: colors.inputBg,
                      borderColor: colors.border,
                      color: colors.text
                    }}
                    placeholder="Enter OAuth 2.0 token"
                  />
                  <button
                    type="button"
                    onClick={() => setShowToken(!showToken)}
                    className="absolute right-2 top-1/2 transform -translate-y-1/2 p-1 hover-lift"
                    style={{ color: colors.textSecondary }}
                  >
                    {showToken ? <EyeOff size={16} /> : <EyeIcon size={16} />}
                  </button>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="auto-refresh"
                  className="rounded-sm hover-lift"
                  style={{
                    borderColor: colors.border,
                    backgroundColor: colors.inputBg
                  }}
                />
                <label htmlFor="auto-refresh" className="text-sm" style={{ color: colors.text }}>
                  Auto-refresh access token
                </label>
              </div>
            </div>
          )}

          {authType === 'noauth' && (
            <div className="text-center py-8">
              <Globe size={48} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mx-auto mb-4" />
              <h3 className="text-sm font-semibold mb-2" style={{ color: colors.text }}>No Authorization</h3>
              <p className="text-sm max-w-sm mx-auto" style={{ color: colors.textSecondary }}>
                This request does not use any authorization.
              </p>
            </div>
          )}
        </div>
      </div>
    );
  };

  // Render Params Tab
  const renderParamsTab = () => {
    const hasParams = requestParams.length > 0;
    
    return (
      <div className="flex flex-col h-full">
        <div className="flex justify-between items-center p-4">
          <div className="flex items-center gap-4">
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Query Parameters</h3>
            {hasParams && (
              <div className="flex items-center gap-2">
                <button className="text-xs px-2 py-1 rounded hover-lift" style={{ 
                  backgroundColor: colors.hover,
                  color: colors.textSecondary
                }}
                onClick={() => {
                  const text = requestParams.map(p => `${p.key}=${p.value}`).join('\n');
                  navigator.clipboard.writeText(text);
                  showToast('Parameters copied as text', 'success');
                }}>
                  Bulk Edit
                </button>
              </div>
            )}
          </div>
          <button onClick={addParam} className="px-3 py-1.5 rounded text-sm font-medium flex items-center gap-2 hover:opacity-90 transition-colors hover-lift"
            style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
            <Plus size={12} />
            Add
          </button>
        </div>

        <div className="flex-1 overflow-hidden">
          {hasParams ? (
            <div className="h-full overflow-auto">
              <table className="w-full" style={{ borderCollapse: 'collapse' }}>
                <thead style={{ backgroundColor: colors.tableHeader, position: 'sticky', top: 0 }}>
                  <tr>
                    <th className="w-12 p-0">
                      <div className="p-3">
                        <input 
                          type="checkbox" 
                          className="rounded-sm hover-lift"
                          checked={requestParams.every(p => p.enabled)}
                          onChange={() => {
                            const allEnabled = requestParams.every(p => p.enabled);
                            setRequestParams(requestParams.map(p => ({ ...p, enabled: !allEnabled })));
                          }}
                          style={{ 
                            borderColor: colors.border,
                            backgroundColor: colors.card,
                            cursor: 'pointer'
                          }}
                        />
                      </div>
                    </th>
                    <th className="text-left p-3 text-sm font-medium" style={{ color: colors.textSecondary }}>KEY</th>
                    <th className="text-left p-3 text-sm font-medium" style={{ color: colors.textSecondary }}>VALUE</th>
                    <th className="text-left p-3 text-sm font-medium" style={{ color: colors.textSecondary }}>DESCRIPTION</th>
                    <th className="w-16 p-3"></th>
                  </tr>
                </thead>
                <tbody>
                  {requestParams.map(param => (
                    <tr key={param.id} className="hover:bg-opacity-50 transition-colors hover-lift" 
                      style={{ backgroundColor: colors.tableRow }}>
                      <td className="p-0">
                        <div className="p-3">
                          <input
                            type="checkbox"
                            checked={param.enabled}
                            onChange={() => updateParam(param.id, 'enabled', !param.enabled)}
                            className="rounded-sm hover-lift"
                            style={{ 
                              borderColor: colors.border,
                              backgroundColor: colors.card,
                              cursor: 'pointer'
                            }}
                          />
                        </div>
                      </td>
                      <td className="p-3">
                        <input
                          type="text"
                          value={param.key}
                          onChange={(e) => updateParam(param.id, 'key', e.target.value)}
                          className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                          style={{ 
                            borderColor: colors.border,
                            color: colors.text,
                            backgroundColor: colors.inputBg
                          }}
                          placeholder="Key"
                        />
                      </td>
                      <td className="p-3">
                        <input
                          type="text"
                          value={param.value}
                          onChange={(e) => updateParam(param.id, 'value', e.target.value)}
                          className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                          style={{ 
                            borderColor: colors.border,
                            color: colors.text,
                            backgroundColor: colors.inputBg
                          }}
                          placeholder="Value"
                        />
                      </td>
                      <td className="p-3">
                        <input
                          type="text"
                          value={param.description || ''}
                          onChange={(e) => updateParam(param.id, 'description', e.target.value)}
                          className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                          style={{ 
                            borderColor: colors.border,
                            color: colors.text,
                            backgroundColor: colors.inputBg
                          }}
                          placeholder="Description"
                        />
                      </td>
                      <td className="p-3">
                        <button
                          onClick={() => deleteParam(param.id)}
                          className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                          style={{ backgroundColor: colors.hover }}>
                          <Trash2 size={13} style={{ color: colors.textSecondary }} />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center h-full p-8 text-center">
              <Hash size={48} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mb-4" />
              <h3 className="text-sm font-semibold mb-2" style={{ color: colors.text }}>No Query Parameters</h3>
              <p className="text-sm mb-4 max-w-sm" style={{ color: colors.textSecondary }}>
                Query parameters are appended to the URL in the form of key=value pairs, separated by &.
              </p>
              <button
                onClick={addParam}
                className="px-3 py-1.5 text-sm font-medium rounded flex items-center gap-2 hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                <Plus size={13} />
                Add Parameter
              </button>
            </div>
          )}
        </div>
      </div>
    );
  };

  // Render Headers Tab
  const renderHeadersTab = () => {
    return (
      <div className="flex flex-col h-full">
        <div className="flex justify-between items-center p-4">
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Headers</h3>
          <div className="flex items-center gap-2">
            <button className="text-xs px-2 py-1 rounded hover-lift" style={{ 
              backgroundColor: colors.hover,
              color: colors.textSecondary
            }}
            onClick={() => {
              const text = requestHeaders.map(h => `${h.key}: ${h.value}`).join('\n');
              navigator.clipboard.writeText(text);
              showToast('Headers copied as text', 'success');
            }}>
              Bulk Edit
            </button>
            <button
              onClick={addHeader}
              className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 hover-lift"
              style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
              <Plus size={13} />
              Add Header
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-auto">
          <table className="w-full">
            <thead style={{ backgroundColor: colors.tableHeader }}>
              <tr>
                <th className="w-12 px-4 py-3">
                  <div className="flex items-center">
                    <input 
                      type="checkbox" 
                      className="rounded-sm hover-lift"
                      checked={requestHeaders.every(h => h.enabled)}
                      onChange={() => {
                        const allEnabled = requestHeaders.every(h => h.enabled);
                        setRequestHeaders(requestHeaders.map(h => ({ ...h, enabled: !allEnabled })));
                      }}
                      style={{ 
                        borderColor: colors.border,
                        backgroundColor: colors.card,
                        cursor: 'pointer'
                      }}
                    />
                  </div>
                </th>
                <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>KEY</th>
                <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>VALUE</th>
                <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>DESCRIPTION</th>
                <th className="w-20 px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {requestHeaders.map((header) => (
                <tr key={header.id} className="hover:bg-opacity-50 transition-colors hover-lift" 
                  style={{ backgroundColor: colors.tableRow }}>
                  <td className="px-4 py-3">
                    <div className="flex items-center">
                      <input
                        type="checkbox"
                        checked={header.enabled}
                        onChange={() => updateHeader(header.id, 'enabled', !header.enabled)}
                        className="rounded-sm hover-lift"
                        style={{ 
                          borderColor: colors.border,
                          backgroundColor: colors.card,
                          cursor: 'pointer'
                        }}
                      />
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <input
                      type="text"
                      value={header.key}
                      onChange={(e) => updateHeader(header.id, 'key', e.target.value)}
                      className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                      style={{ 
                        borderColor: colors.border,
                        color: colors.text,
                        backgroundColor: colors.inputBg
                      }}
                      placeholder="Header name"
                    />
                  </td>
                  <td className="px-4 py-3">
                    <input
                      type="text"
                      value={header.value}
                      onChange={(e) => updateHeader(header.id, 'value', e.target.value)}
                      className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                      style={{ 
                        borderColor: colors.border,
                        color: colors.text,
                        backgroundColor: colors.inputBg
                      }}
                      placeholder="Header value"
                    />
                  </td>
                  <td className="px-4 py-3">
                    <input
                      type="text"
                      value={header.description || ''}
                      onChange={(e) => updateHeader(header.id, 'description', e.target.value)}
                      className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                      style={{ 
                        borderColor: colors.border,
                        color: colors.text,
                        backgroundColor: colors.inputBg
                      }}
                      placeholder="Optional description"
                    />
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => deleteHeader(header.id)}
                      className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                      style={{ backgroundColor: colors.hover }}>
                      <Trash2 size={13} style={{ color: colors.textSecondary }} />
                    </button>
                  </td>
                </tr>
              ))}
              
              {requestHeaders.length === 0 && (
                <tr>
                  <td colSpan="5" className="px-4 py-8 text-center" style={{ color: colors.textSecondary }}>
                    <div className="flex flex-col items-center gap-2">
                      <Layers size={24} style={{ opacity: 0.5 }} />
                      <p className="text-sm">No headers added</p>
                      <button
                        onClick={addHeader}
                        className="mt-2 px-3 py-1.5 text-sm font-medium rounded flex items-center gap-2 hover:opacity-90 transition-colors hover-lift"
                        style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                        <Plus size={13} />
                        Add your first header
                      </button>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  // Render Body Tab with all options
  const renderBodyTab = () => {
    const renderBodyContent = () => {
      switch (requestBodyType) {
        case 'form-data':
          return (
            <div className="border rounded overflow-hidden" style={{ borderColor: colors.border }}>
              <table className="w-full">
                <thead style={{ backgroundColor: colors.tableHeader }}>
                  <tr>
                    <th className="w-12 px-4 py-3">
                      <div className="flex items-center">
                        <input 
                          type="checkbox" 
                          className="rounded-sm hover-lift"
                          checked={formData.every(f => f.enabled)}
                          onChange={() => {
                            const allEnabled = formData.every(f => f.enabled);
                            setFormData(formData.map(f => ({ ...f, enabled: !allEnabled })));
                          }}
                          style={{ 
                            borderColor: colors.border,
                            backgroundColor: colors.card,
                            cursor: 'pointer'
                          }}
                        />
                      </div>
                    </th>
                    <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>KEY</th>
                    <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>VALUE</th>
                    <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>TYPE</th>
                    <th className="w-20 px-4 py-3">
                      <button
                        onClick={() => setFormData([...formData, { id: `form-${Date.now()}`, key: '', value: '', type: 'text', enabled: true }])}
                        className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                        style={{ backgroundColor: colors.hover }}>
                        <Plus size={14} style={{ color: colors.textSecondary }} />
                      </button>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {formData.map((item, index) => (
                    <tr key={item.id} className="border-b last:border-b-0 hover-lift" style={{ borderColor: colors.border }}>
                      <td className="px-4 py-3">
                        <div className="flex items-center">
                          <input
                            type="checkbox"
                            checked={item.enabled}
                            onChange={() => {
                              const newData = [...formData];
                              newData[index].enabled = !newData[index].enabled;
                              setFormData(newData);
                            }}
                            className="rounded-sm hover-lift"
                            style={{ 
                              borderColor: colors.border,
                              backgroundColor: colors.card,
                              cursor: 'pointer'
                            }}
                          />
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <input
                          type="text"
                          value={item.key}
                          onChange={(e) => {
                            const newData = [...formData];
                            newData[index].key = e.target.value;
                            setFormData(newData);
                          }}
                          className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                          style={{ 
                            borderColor: colors.border,
                            color: colors.text,
                            backgroundColor: colors.inputBg
                          }}
                          placeholder="Key"
                        />
                      </td>
                      <td className="px-4 py-3">
                        {item.type === 'text' ? (
                          <input
                            type="text"
                            value={item.value}
                            onChange={(e) => {
                              const newData = [...formData];
                              newData[index].value = e.target.value;
                              setFormData(newData);
                            }}
                            className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                            style={{ 
                              borderColor: colors.border,
                              color: colors.text,
                              backgroundColor: colors.inputBg
                            }}
                            placeholder="Value"
                          />
                        ) : (
                          <button className="w-full px-2 py-1.5 border rounded-sm text-sm text-left hover:bg-opacity-50 transition-colors hover-lift"
                            style={{ 
                              borderColor: colors.border,
                              color: colors.textSecondary,
                              backgroundColor: colors.inputBg
                            }}
                            onClick={() => {
                              const input = document.createElement('input');
                              input.type = 'file';
                              input.onchange = (e) => {
                                const file = e.target.files[0];
                                if (file) {
                                  const newData = [...formData];
                                  newData[index].value = file.name;
                                  setFormData(newData);
                                  showToast(`File selected: ${file.name}`, 'success');
                                }
                              };
                              input.click();
                            }}>
                            Select File
                          </button>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <select
                          value={item.type}
                          onChange={(e) => {
                            const newData = [...formData];
                            newData[index].type = e.target.value;
                            setFormData(newData);
                          }}
                          className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                          style={{ 
                            borderColor: colors.border,
                            color: colors.text,
                            backgroundColor: colors.inputBg
                          }}>
                          <option value="text">Text</option>
                          <option value="file">File</option>
                        </select>
                      </td>
                      <td className="px-4 py-3">
                        <button
                          onClick={() => {
                            const newData = formData.filter((_, i) => i !== index);
                            setFormData(newData);
                          }}
                          className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                          style={{ backgroundColor: colors.hover }}>
                          <Trash2 size={13} style={{ color: colors.textSecondary }} />
                        </button>
                      </td>
                    </tr>
                  ))}
                  
                  {formData.length === 0 && (
                    <tr>
                      <td colSpan="5" className="px-4 py-8 text-center" style={{ color: colors.textSecondary }}>
                        <div className="flex flex-col items-center gap-2">
                          <FileText size={24} style={{ opacity: 0.5 }} />
                          <p className="text-sm">No form data</p>
                          <button
                            onClick={() => setFormData([...formData, { id: `form-${Date.now()}`, key: '', value: '', type: 'text', enabled: true }])}
                            className="mt-2 px-3 py-1.5 text-sm font-medium rounded flex items-center gap-2 hover:opacity-90 transition-colors hover-lift"
                            style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                            <Plus size={13} />
                            Add form data
                          </button>
                        </div>
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          );

        case 'x-www-form-urlencoded':
          return (
            <div className="border rounded overflow-hidden" style={{ borderColor: colors.border }}>
              <table className="w-full">
                <thead style={{ backgroundColor: colors.tableHeader }}>
                  <tr>
                    <th className="w-12 px-4 py-3">
                      <div className="flex items-center">
                        <input 
                          type="checkbox" 
                          className="rounded-sm hover-lift"
                          checked={urlEncodedData.every(u => u.enabled)}
                          onChange={() => {
                            const allEnabled = urlEncodedData.every(u => u.enabled);
                            setUrlEncodedData(urlEncodedData.map(u => ({ ...u, enabled: !allEnabled })));
                          }}
                          style={{ 
                            borderColor: colors.border,
                            backgroundColor: colors.card,
                            cursor: 'pointer'
                          }}
                        />
                      </div>
                    </th>
                    <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>KEY</th>
                    <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>VALUE</th>
                    <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>DESCRIPTION</th>
                    <th className="w-20 px-4 py-3">
                      <button
                        onClick={() => setUrlEncodedData([...urlEncodedData, { id: `url-${Date.now()}`, key: '', value: '', description: '', enabled: true }])}
                        className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                        style={{ backgroundColor: colors.hover }}>
                        <Plus size={14} style={{ color: colors.textSecondary }} />
                      </button>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {urlEncodedData.map((item, index) => (
                    <tr key={item.id} className="border-b last:border-b-0 hover-lift" style={{ borderColor: colors.border }}>
                      <td className="px-4 py-3">
                        <div className="flex items-center">
                          <input
                            type="checkbox"
                            checked={item.enabled}
                            onChange={() => {
                              const newData = [...urlEncodedData];
                              newData[index].enabled = !newData[index].enabled;
                              setUrlEncodedData(newData);
                            }}
                            className="rounded-sm hover-lift"
                            style={{ 
                              borderColor: colors.border,
                              backgroundColor: colors.card,
                              cursor: 'pointer'
                            }}
                          />
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <input
                          type="text"
                          value={item.key}
                          onChange={(e) => {
                            const newData = [...urlEncodedData];
                            newData[index].key = e.target.value;
                            setUrlEncodedData(newData);
                          }}
                          className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                          style={{ 
                            borderColor: colors.border,
                            color: colors.text,
                            backgroundColor: colors.inputBg
                          }}
                          placeholder="Key"
                        />
                      </td>
                      <td className="px-4 py-3">
                        <input
                          type="text"
                          value={item.value}
                          onChange={(e) => {
                            const newData = [...urlEncodedData];
                            newData[index].value = e.target.value;
                            setUrlEncodedData(newData);
                          }}
                          className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                          style={{ 
                            borderColor: colors.border,
                            color: colors.text,
                            backgroundColor: colors.inputBg
                          }}
                          placeholder="Value"
                        />
                      </td>
                      <td className="px-4 py-3">
                        <input
                          type="text"
                          value={item.description || ''}
                          onChange={(e) => {
                            const newData = [...urlEncodedData];
                            newData[index].description = e.target.value;
                            setUrlEncodedData(newData);
                          }}
                          className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                          style={{ 
                            borderColor: colors.border,
                            color: colors.text,
                            backgroundColor: colors.inputBg
                          }}
                          placeholder="Description"
                        />
                      </td>
                      <td className="px-4 py-3">
                        <button
                          onClick={() => {
                            const newData = urlEncodedData.filter((_, i) => i !== index);
                            setUrlEncodedData(newData);
                          }}
                          className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                          style={{ backgroundColor: colors.hover }}>
                          <Trash2 size={13} style={{ color: colors.textSecondary }} />
                        </button>
                      </td>
                    </tr>
                  ))}
                  
                  {urlEncodedData.length === 0 && (
                    <tr>
                      <td colSpan="5" className="px-4 py-8 text-center" style={{ color: colors.textSecondary }}>
                        <div className="flex flex-col items-center gap-2">
                          <Hash size={24} style={{ opacity: 0.5 }} />
                          <p className="text-sm">No url-encoded data</p>
                          <button
                            onClick={() => setUrlEncodedData([...urlEncodedData, { id: `url-${Date.now()}`, key: '', value: '', description: '', enabled: true }])}
                            className="mt-2 px-3 py-1.5 text-sm font-medium rounded flex items-center gap-2 hover:opacity-90 transition-colors hover-lift"
                            style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                            <Plus size={13} />
                            Add parameter
                          </button>
                        </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      );

    case 'raw':
      return (
        <div className="border rounded overflow-hidden" style={{ borderColor: colors.border }}>
          <div className="flex items-center justify-between px-3 py-2 border-b" style={{ 
            backgroundColor: colors.tableHeader,
            borderColor: colors.border
          }}>
            <div className="flex items-center gap-2">
              <select
                value={rawBodyType}
                onChange={(e) => setRawBodyType(e.target.value)}
                className="px-2 py-1 rounded text-sm focus:outline-none transition-colors hover-lift"
                style={{ 
                  backgroundColor: colors.card,
                  color: colors.text,
                  border: `1px solid ${colors.border}`,
                  cursor: 'pointer'
                }}>
                <option value="json">JSON</option>
                <option value="text">Text</option>
                <option value="javascript">JavaScript</option>
                <option value="html">HTML</option>
                <option value="xml">XML</option>
              </select>
              <button 
                className="px-2 py-1 text-sm rounded hover:bg-opacity-50 transition-colors hover-lift" 
                style={{ 
                  backgroundColor: colors.hover,
                  color: colors.textSecondary
                }}
                onClick={() => {
                  try {
                    const parsed = JSON.parse(requestBody);
                    setRequestBody(JSON.stringify(parsed, null, 2));
                    showToast('JSON beautified!', 'success');
                  } catch (e) {
                    showToast('Not valid JSON', 'error');
                  }
                }}>
                Beautify
              </button>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-xs" style={{ color: colors.textSecondary }}>
                {requestBody.length} characters
              </span>
              <button 
                className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift" 
                style={{ backgroundColor: colors.hover }}
                onClick={() => {
                  navigator.clipboard.writeText(requestBody);
                  showToast('Copied to clipboard!', 'success');
                }}>
                <Copy size={13} style={{ color: colors.textSecondary }} />
              </button>
            </div>
          </div>
          <textarea
            value={requestBody}
            onChange={(e) => setRequestBody(e.target.value)}
            className="w-full h-64 font-mono text-sm p-4 resize-none focus:outline-none hover-lift"
            style={{
              backgroundColor: colors.card,
              color: colors.text,
              lineHeight: '1.5'
            }}
            placeholder={rawBodyType === 'json' ? '{\n  "key": "value"\n}' : 'Enter text here...'}
            spellCheck="false"
          />
        </div>
      );

    case 'binary':
      return (
        <div className="border rounded p-8 text-center" style={{ borderColor: colors.border }}>
          <FileBinary size={48} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mx-auto mb-4" />
          <p className="text-sm mb-2" style={{ color: colors.text }}>Upload a file</p>
          <p className="text-xs mb-6 max-w-sm mx-auto" style={{ color: colors.textSecondary }}>
            Select a file to send as the request body. Files are sent as-is without any processing.
          </p>
          <button className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 mx-auto hover-lift"
            style={{ backgroundColor: colors.primaryDark, color: colors.white }}
            onClick={() => {
              const input = document.createElement('input');
              input.type = 'file';
              input.accept = '*/*';
              input.onchange = (e) => {
                const file = e.target.files[0];
                if (file) {
                  setBinaryFile(file);
                  showToast(`File selected: ${file.name}`, 'success');
                }
              };
              input.click();
            }}>
            <Upload size={14} />
            Choose File
          </button>
          {binaryFile && (
            <div className="mt-4 p-3 rounded hover-lift" style={{ backgroundColor: colors.hover }}>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <File size={14} style={{ color: colors.textSecondary }} />
                  <span className="text-sm" style={{ color: colors.text }}>{binaryFile.name}</span>
                </div>
                <button onClick={() => setBinaryFile(null)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.card }}>
                  <X size={12} style={{ color: colors.textSecondary }} />
                </button>
              </div>
              <p className="text-xs mt-1" style={{ color: colors.textSecondary }}>
                Size: {(binaryFile.size / 1024).toFixed(2)} KB
              </p>
            </div>
          )}
        </div>
      );

    case 'graphql':
      return (
        <div className="space-y-4">
          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="text-sm font-medium" style={{ color: colors.text }}>Query</label>
              <button className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.textSecondary }}
                onClick={() => {
                  setGraphqlQuery('query {\n  getUser(id: 1) {\n    id\n    name\n    email\n  }\n}');
                  showToast('Sample query loaded', 'success');
                }}>
                Sample
              </button>
            </div>
            <textarea
              value={graphqlQuery}
              onChange={(e) => setGraphqlQuery(e.target.value)}
              className="w-full h-48 font-mono text-sm p-4 border rounded resize-none focus:outline-none hover-lift"
              style={{
                backgroundColor: colors.card,
                borderColor: colors.border,
                color: colors.text,
                lineHeight: '1.5'
              }}
              placeholder="query {\n  getUser(id: 1) {\n    id\n    name\n    email\n  }\n}"
              spellCheck="false"
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Variables</label>
            <textarea
              value={graphqlVariables}
              onChange={(e) => setGraphqlVariables(e.target.value)}
              className="w-full h-32 font-mono text-sm p-4 border rounded resize-none focus:outline-none hover-lift"
              style={{
                backgroundColor: colors.card,
                borderColor: colors.border,
                color: colors.text,
                lineHeight: '1.5'
              }}
              placeholder='{\n  "id": 1\n}'
              spellCheck="false"
            />
          </div>
        </div>
      );

    default:
      return (
        <div className="border rounded p-8 text-center" style={{ borderColor: colors.border }}>
          <FileText size={48} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mx-auto mb-4" />
          <p className="text-sm" style={{ color: colors.text }}>
            This request does not have a body
          </p>
        </div>
      );
  }
};

return (
  <div className="p-4">
    <div className="flex justify-between items-center mb-4">
      <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Body</h3>
      <div className="flex gap-2">
        {['none', 'form-data', 'x-www-form-urlencoded', 'raw', 'binary', 'graphql'].map(type => (
          <button
            key={type}
            onClick={() => setRequestBodyType(type)}
            className={`px-3 py-1.5 rounded text-sm font-medium capitalize transition-colors hover-lift ${
              requestBodyType === type ? '' : 'hover:bg-opacity-50'
            }`}
            style={{ 
              backgroundColor: requestBodyType === type ? colors.primaryDark : colors.hover,
              color: requestBodyType === type ? 'white' : colors.textSecondary
            }}>
            {type === 'x-www-form-urlencoded' ? 'x-www-form' : type}
          </button>
        ))}
      </div>
    </div>
    
    {renderBodyContent()}
  </div>
);
  };

  // Render Response Panel with view options
  const renderResponsePanel = () => {
    const renderResponseContent = () => {
      if (!response) {
        return (
          <div className="h-full flex flex-col items-center justify-center text-center p-8">
            <Send size={32} style={{ color: colors.textSecondary }} className="mb-4 opacity-50" />
            <h3 className="text-sm font-semibold mb-2" style={{ color: colors.text }}>No Response</h3>
            <p className="text-sm max-w-sm" style={{ color: colors.textSecondary }}>
              Send a request to see the response here.
            </p>
          </div>
        );
      }

      switch (responseView) {
        case 'raw':
          return (
            <pre className="border rounded p-4 overflow-auto text-sm font-mono whitespace-pre-wrap hover-lift"
              style={{ 
                backgroundColor: colors.codeBg,
                borderColor: colors.border,
                color: colors.text,
                height: 'calc(100% - 60px)'
              }}>
              {response.body}
            </pre>
          );
        
        case 'preview':
          try {
            const parsed = JSON.parse(response.body);
            return (
              <div className="border rounded p-4 overflow-auto text-sm hover-lift"
                style={{ 
                  backgroundColor: colors.codeBg,
                  borderColor: colors.border,
                  color: colors.text,
                  height: 'calc(100% - 60px)'
                }}>
                <SyntaxHighlighter language="json" code={JSON.stringify(parsed, null, 2)} />
              </div>
            );
          } catch (e) {
            return (
              <div className="border rounded p-4 overflow-auto text-sm font-mono whitespace-pre-wrap hover-lift"
                style={{ 
                  backgroundColor: colors.codeBg,
                  borderColor: colors.border,
                  color: colors.text,
                  height: 'calc(100% - 60px)'
                }}>
                {response.body}
              </div>
            );
          }
        
        case 'headers':
          return (
            <div className="border rounded overflow-hidden hover-lift"
              style={{ 
                backgroundColor: colors.codeBg,
                borderColor: colors.border,
                height: 'calc(100% - 60px)'
              }}>
              <table className="w-full">
                <thead style={{ backgroundColor: colors.tableHeader }}>
                  <tr>
                    <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Header</th>
                    <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Value</th>
                  </tr>
                </thead>
                <tbody>
                  {response.headers.map((header, index) => (
                    <tr key={index} className="border-b last:border-b-0 hover-lift" style={{ borderColor: colors.border }}>
                      <td className="px-4 py-3 font-medium" style={{ color: colors.text }}>{header.key}</td>
                      <td className="px-4 py-3" style={{ color: colors.textSecondary }}>{header.value}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          );
        
        default:
          return (
            <pre className="border rounded p-4 overflow-auto text-sm font-mono whitespace-pre-wrap hover-lift"
              style={{ 
                backgroundColor: colors.codeBg,
                borderColor: colors.border,
                color: colors.text,
                height: 'calc(100% - 60px)'
              }}>
              {response.body}
            </pre>
          );
      }
    };

    return (
      <div 
        className="flex flex-col border-t"
        style={{ 
          borderColor: colors.border,
          height: `${responseHeight}px`,
          minHeight: '100px'
        }}
        ref={responseRef}
      >
        <div 
          className="h-2 cursor-row-resize flex items-center justify-center hover:bg-opacity-50 transition-colors hover-lift"
          style={{ backgroundColor: colors.border }}
          onMouseDown={() => setIsResizing(true)}
        >
          <GripVertical size={12} style={{ color: colors.textSecondary }} />
        </div>

        <div className="flex items-center justify-between px-4 py-3 border-b" style={{ 
          backgroundColor: colors.card,
          borderColor: colors.border
        }}>
          <div className="flex items-center gap-4">
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Response</h3>
            {response && (
              <div className="flex items-center gap-1">
                {['raw', 'preview', 'headers'].map(view => (
                  <button key={view}
                    onClick={() => setResponseView(view)}
                    className={`px-3 py-1 rounded text-xs font-medium capitalize transition-colors hover-lift ${
                      responseView === view ? '' : 'hover:bg-opacity-50'
                    }`}
                    style={{ 
                      backgroundColor: responseView === view ? colors.primaryDark : colors.hover,
                      color: responseView === view ? 'white' : colors.textSecondary
                    }}>
                    {view}
                  </button>
                ))}
              </div>
            )}
          </div>
          {response && (
            <div className="flex items-center gap-2">
              <button className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.textSecondary }}
                onClick={() => {
                  try {
                    const parsed = JSON.parse(response.body);
                    setResponse({
                      ...response,
                      body: JSON.stringify(parsed, null, 2)
                    });
                    showToast('Response beautified!', 'success');
                  } catch (e) {
                    showToast('Not valid JSON', 'error');
                  }
                }}>
                Beautify
              </button>
              <button className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}
                onClick={() => {
                  navigator.clipboard.writeText(response.body);
                  showToast('Copied to clipboard!', 'success');
                }}>
                <Copy size={12} style={{ color: colors.textSecondary }} />
              </button>
            </div>
          )}
        </div>
        
        <div className="flex-1 overflow-auto p-4" style={{ backgroundColor: colors.card }}>
          {response && (
            <div className="space-y-4">
              <div className="flex items-center gap-3">
                <div className={`px-3 py-1.5 rounded text-sm font-medium flex items-center gap-2 hover-lift ${
                  response.status === 200 ? 'bg-green-500/10 text-green-500' : 'bg-red-500/10 text-red-500'
                }`}>
                  {response.status === 200 ? <CheckCircle size={12} /> : <XCircle size={12} />}
                  {response.status} {response.statusText}
                </div>
                <div className="text-sm" style={{ color: colors.textSecondary }}>
                  Time: {response.time} • Size: {response.size}
                </div>
              </div>
              {renderResponseContent()}
            </div>
          )}
        </div>
      </div>
    );
  };

  // Render other right panels
  const renderAPIsPanel = () => (
    <div className="w-80 border-l flex flex-col" style={{ 
      backgroundColor: colors.sidebar,
      borderColor: colors.border
    }}>
      <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
        <h3 className="text-sm font-semibold" style={{ color: colors.text }}>APIs</h3>
        <button onClick={() => setShowAPIs(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
          style={{ backgroundColor: colors.hover }}>
          <X size={14} style={{ color: colors.textSecondary }} />
        </button>
      </div>
      <div className="p-4">
        <p className="text-sm mb-4" style={{ color: colors.textSecondary }}>No APIs created</p>
        <button className="w-full py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
          style={{ backgroundColor: colors.primaryDark, color: colors.white }}
          onClick={() => showToast('Create API feature would open', 'info')}>
          <Plus size={12} className="inline mr-2" />
          Create API
        </button>
      </div>
    </div>
  );

  const renderEnvironmentsPanel = () => (
    <div className="w-80 border-l flex flex-col" style={{ 
      backgroundColor: colors.sidebar,
      borderColor: colors.border
    }}>
      <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
        <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Environments</h3>
        <button onClick={() => setShowEnvironments(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
          style={{ backgroundColor: colors.hover }}>
          <X size={14} style={{ color: colors.textSecondary }} />
        </button>
      </div>
      <div className="p-4 space-y-3">
        {environments.map(env => (
          <button key={env.id}
            onClick={() => {
              setActiveEnvironment(env.id);
              setEnvironments(envs => envs.map(e => ({ ...e, isActive: e.id === env.id })));
              showToast(`Switched to ${env.name}`, 'success');
            }}
            className={`w-full px-3 py-2 rounded text-sm text-left transition-colors hover-lift ${
              activeEnvironment === env.id ? '' : 'hover:bg-opacity-50'
            }`}
            style={{ 
              backgroundColor: activeEnvironment === env.id ? colors.selected : 'transparent',
              color: activeEnvironment === env.id ? colors.primary : colors.text
            }}>
            {env.name}
            {activeEnvironment === env.id && <Check size={14} className="float-right" />}
          </button>
        ))}
        <button className="w-full px-3 py-2 rounded text-sm text-left hover:bg-opacity-50 transition-colors hover-lift"
          style={{ backgroundColor: colors.hover, color: colors.primary }}
          onClick={() => showToast('Create Environment feature would open', 'info')}>
          <Plus size={12} className="inline mr-2" />
          Create Environment
        </button>
      </div>
    </div>
  );

  const renderMockServersPanel = () => (
    <div className="w-80 border-l flex flex-col" style={{ 
      backgroundColor: colors.sidebar,
      borderColor: colors.border
    }}>
      <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
        <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Mock Servers</h3>
        <button onClick={() => setShowMockServers(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
          style={{ backgroundColor: colors.hover }}>
          <X size={14} style={{ color: colors.textSecondary }} />
        </button>
      </div>
      <div className="p-4">
        <p className="text-sm mb-4" style={{ color: colors.textSecondary }}>No mock servers created</p>
        <button className="w-full py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
          style={{ backgroundColor: colors.primaryDark, color: colors.white }}
          onClick={() => showToast('Create Mock Server feature would open', 'info')}>
          <Plus size={12} className="inline mr-2" />
          Create Mock Server
        </button>
      </div>
    </div>
  );

  const renderMonitorsPanel = () => (
    <div className="w-80 border-l flex flex-col" style={{ 
      backgroundColor: colors.sidebar,
      borderColor: colors.border
    }}>
      <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
        <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Monitors</h3>
        <button onClick={() => setShowMonitors(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
          style={{ backgroundColor: colors.hover }}>
          <X size={14} style={{ color: colors.textSecondary }} />
        </button>
      </div>
      <div className="p-4">
        <p className="text-sm mb-4" style={{ color: colors.textSecondary }}>No monitors created</p>
        <button className="w-full py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
          style={{ backgroundColor: colors.primaryDark, color: colors.white }}
          onClick={() => showToast('Create Monitor feature would open', 'info')}>
          <Plus size={12} className="inline mr-2" />
          Create Monitor
        </button>
      </div>
    </div>
  );

  // Determine which right panel to show
  const renderRightPanel = () => {
    if (showCodePanel) return renderCodePanel();
    if (showAPIs) return renderAPIsPanel();
    if (showEnvironments) return renderEnvironmentsPanel();
    if (showMockServers) return renderMockServersPanel();
    if (showMonitors) return renderMonitorsPanel();
    return null;
  };

  // Render Toast
  const renderToast = () => {
    if (!toast) return null;
    
    const bgColor = toast.type === 'error' ? colors.error : 
                   toast.type === 'success' ? colors.success : 
                   toast.type === 'warning' ? colors.warning : 
                   colors.info;
    
    return (
      <div className="fixed bottom-4 right-4 px-4 py-2 rounded text-sm font-medium z-50 animate-fade-in-up"
        style={{ 
          backgroundColor: bgColor,
          color: 'white'
        }}>
        {toast.message}
      </div>
    );
  };

  // Render Import Modal
  const renderImportModal = () => {
    if (!showImportModal) return null;
    
    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="rounded-lg w-full max-w-md" style={{ 
          backgroundColor: colors.modalBg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Import</h3>
            <button onClick={() => setShowImportModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}>
              <X size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          <div className="p-4 space-y-4">
            <div className="text-center p-8 border-2 border-dashed rounded hover-lift" style={{ borderColor: colors.border }}>
              <Upload size={32} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mx-auto mb-4" />
              <p className="text-sm mb-2" style={{ color: colors.text }}>Drag and drop files here</p>
              <p className="text-xs" style={{ color: colors.textSecondary }}>Supports: Postman collections, OpenAPI, etc.</p>
              <button className="mt-4 px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
                onClick={() => {
                  showToast('Import feature would open file dialog', 'info');
                  setTimeout(() => setShowImportModal(false), 1500);
                }}>
                Browse Files
              </button>
            </div>
            <div className="space-y-2">
              <button className="w-full px-4 py-3 rounded text-sm text-left hover:bg-opacity-50 transition-colors flex items-center gap-3 hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.text }}
                onClick={() => {
                  showToast('Import from link dialog would open', 'info');
                  setTimeout(() => setShowImportModal(false), 1500);
                }}>
                <Globe size={14} />
                Import from Link
              </button>
              <button className="w-full px-4 py-3 rounded text-sm text-left hover:bg-opacity-50 transition-colors flex items-center gap-3 hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.text }}
                onClick={() => {
                  showToast('Paste raw text dialog would open', 'info');
                  setTimeout(() => setShowImportModal(false), 1500);
                }}>
                <Code size={14} />
                Paste Raw Text
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderShareModal = () => {
    if (!showShareModal) return null;
    
    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="rounded-lg w-full max-w-md" style={{ 
          backgroundColor: colors.modalBg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Share Collection</h3>
            <button onClick={() => setShowShareModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}>
              <X size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          <div className="p-4 space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Share Link</label>
              <div className="flex gap-2">
                <input
                  type="text"
                  readOnly
                  value="https://postman.com/collection/abc123"
                  className="flex-1 px-3 py-2 border rounded text-sm focus:outline-none hover-lift"
                  style={{
                    backgroundColor: colors.inputBg,
                    borderColor: colors.border,
                    color: colors.text
                  }}
                />
                <button className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                  onClick={() => {
                    navigator.clipboard.writeText('https://postman.com/collection/abc123');
                    showToast('Link copied to clipboard!', 'success');
                  }}
                  style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                  Copy
                </button>
              </div>
            </div>
            <div className="space-y-2">
              <label className="block text-sm font-medium" style={{ color: colors.text }}>Invite People</label>
              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="Enter email addresses"
                  className="flex-1 px-3 py-2 border rounded text-sm focus:outline-none hover-lift"
                  style={{
                    backgroundColor: colors.inputBg,
                    borderColor: colors.border,
                    color: colors.text
                  }}
                />
                <button className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                  onClick={() => showToast('Invitation sent!', 'success')}
                  style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                  Invite
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderSettingsModal = () => {
    if (!showSettingsModal) return null;
    
    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="rounded-lg w-full max-w-lg" style={{ 
          backgroundColor: colors.modalBg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Settings</h3>
            <button onClick={() => setShowSettingsModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}>
              <X size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          <div className="p-4 space-y-4">
            <div>
              <h4 className="text-sm font-medium mb-3" style={{ color: colors.text }}>General</h4>
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-sm" style={{ color: colors.text }}>Trim keys and values in request body</span>
                  <input type="checkbox" defaultChecked className="rounded hover-lift" />
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm" style={{ color: colors.text }}>Language detection</span>
                  <input type="checkbox" defaultChecked className="rounded hover-lift" />
                </div>
              </div>
            </div>
            <div>
              <h4 className="text-sm font-medium mb-3" style={{ color: colors.text }}>Request</h4>
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-sm" style={{ color: colors.text }}>Send no-cache header</span>
                  <input type="checkbox" className="rounded hover-lift" />
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm" style={{ color: colors.text }}>Request timeout (ms)</span>
                  <input type="number" defaultValue="0" className="w-24 px-2 py-1 border rounded text-sm hover-lift"
                    style={{ backgroundColor: colors.inputBg, borderColor: colors.border, color: colors.text }} />
                </div>
              </div>
            </div>
          </div>
          <div className="p-4 border-t" style={{ borderColor: colors.border }}>
            <button className="w-full py-2.5 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
              onClick={() => {
                showToast('Settings saved!', 'success');
                setShowSettingsModal(false);
              }}
              style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
              Save Settings
            </button>
          </div>
        </div>
      </div>
    );
  };

  const renderCreateModal = () => {
    const [newCollectionName, setNewCollectionName] = useState('');
    const [newCollectionDescription, setNewCollectionDescription] = useState('');
    
    return (
      showCreateModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="rounded-lg w-full max-w-md" style={{ 
            backgroundColor: colors.modalBg,
            border: `1px solid ${colors.modalBorder}`
          }}>
            <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Create Collection</h3>
              <button onClick={() => setShowCreateModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}>
                <X size={14} style={{ color: colors.textSecondary }} />
              </button>
            </div>
            <div className="p-4 space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Name</label>
                <input
                  type="text"
                  value={newCollectionName}
                  onChange={(e) => setNewCollectionName(e.target.value)}
                  placeholder="My Collection"
                  className="w-full px-3 py-2 border rounded text-sm focus:outline-none hover-lift"
                  style={{
                    backgroundColor: colors.inputBg,
                    borderColor: colors.border,
                    color: colors.text
                  }}
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Description</label>
                <textarea
                  value={newCollectionDescription}
                  onChange={(e) => setNewCollectionDescription(e.target.value)}
                  placeholder="Description of your collection"
                  className="w-full px-3 py-2 border rounded text-sm focus:outline-none h-24 resize-none hover-lift"
                  style={{
                    backgroundColor: colors.inputBg,
                    borderColor: colors.border,
                    color: colors.text
                  }}
                />
              </div>
              <button className="w-full py-2.5 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                onClick={() => {
                  if (!newCollectionName.trim()) {
                    showToast('Please enter a collection name', 'error');
                    return;
                  }
                  addNewCollection(newCollectionName, newCollectionDescription);
                  setNewCollectionName('');
                  setNewCollectionDescription('');
                }}
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                Create Collection
              </button>
            </div>
          </div>
        </div>
      )
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
          background: linear-gradient(135deg, ${colors.primary}20 0%, ${colors.info}20 50%, ${colors.warning}20 100%);
        }
      `}</style>

      {/* TOP NAVIGATION */}
      <div className="flex items-center justify-between h-10 px-4 border-b" style={{ 
        backgroundColor: colors.header,
        borderColor: colors.border
      }}>
        <div className="flex items-center gap-4 -ml-4 text-nowrap uppercase">
          <span className={`px-3 py-1.5 text-sm font-medium rounded transition-colors hover-lift`} style={{ color: colors.text }}>API Collections</span>
        </div>

        <div className="flex items-center gap-2">
          {/* Environment Selector */}
          <div className="relative">
            <button className="flex items-center gap-2 px-3 py-1.5 rounded text-sm hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}
              onClick={() => {
                const nextIndex = (environments.findIndex(e => e.isActive) + 1) % environments.length;
                const nextEnv = environments[nextIndex];
                setActiveEnvironment(nextEnv.id);
                setEnvironments(envs => envs.map(e => ({ ...e, isActive: e.id === nextEnv.id })));
                showToast(`Switched to ${nextEnv.name}`, 'success');
              }}>
              <Globe size={12} style={{ color: colors.textSecondary }} />
              <span style={{ color: colors.text }}>{environments.find(e => e.id === activeEnvironment)?.name}</span>
              <ChevronDown size={12} style={{ color: colors.textSecondary }} />
            </button>
          </div>

          <div className="w-px h-4" style={{ backgroundColor: colors.border }}></div>

          {/* Global Search */}
          {/* <div className="relative">
            <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
            <input 
              type="text" 
              placeholder="Search"
              value={globalSearchQuery}
              onChange={(e) => setGlobalSearchQuery(e.target.value)}
              className="pl-8 pr-3 py-1.5 rounded text-sm focus:outline-none w-48 hover-lift"
              style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }} 
            />
            {globalSearchQuery && (
              <div className="absolute right-2 top-1/2 transform -translate-y-1/2">
                <button onClick={() => setGlobalSearchQuery('')} className="p-0.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}>
                  <X size={12} style={{ color: colors.textSecondary }} />
                </button>
              </div>
            )}
          </div> */}

          {/* Code Panel Toggle */}
          <button onClick={() => {setShowCodePanel(!showCodePanel); setShowAPIs(false); setShowEnvironments(false); setShowMockServers(false); setShowMonitors(false);}} 
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: showCodePanel ? colors.selected : colors.hover }}>
            <Code size={14} style={{ color: showCodePanel ? colors.primary : colors.textSecondary }} />
          </button>

          {/* Share Button */}
          <button onClick={() => setShowShareModal(true)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}>
            <Share2 size={14} style={{ color: colors.textSecondary }} />
          </button>

          {/* Settings */}
          <button onClick={() => setShowSettingsModal(true)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}>
            <Settings size={14} style={{ color: colors.textSecondary }} />
          </button>

        </div>
      </div>

      {/* MAIN CONTENT */}
      <div className="flex flex-1 overflow-hidden">
        {/* LEFT SIDEBAR - Collections */}
        <div className="w-80 border-r flex flex-col" style={{ 
          // backgroundColor: colors.sidebar,
          borderColor: colors.border
        }}>
          <div className="p-3 border-b" style={{ borderColor: colors.border }}>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Collections</h3>
              <div className="flex gap-1">
                <button onClick={() => setShowCreateModal(true)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}>
                  <Plus size={12} style={{ color: colors.textSecondary }} />
                </button>
                <button className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}
                  onClick={() => showToast('More options menu would open', 'info')}>
                  <MoreVertical size={12} style={{ color: colors.textSecondary }} />
                </button>
              </div>
            </div>
            <div className="relative mb-3">
              <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
              <input 
                type="text" 
                placeholder="Search collections"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-8 pr-3 py-2 rounded text-sm focus:outline-none hover-lift"
                style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }} 
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

          <div className="flex-1 overflow-auto p-2">
            {filteredCollections.map(collection => (
              <div key={collection.id} className="mb-3">
                <div className="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-opacity-50 transition-colors mb-1.5 cursor-pointer group hover-lift"
                  onClick={() => toggleCollection(collection.id)}
                  style={{ backgroundColor: colors.hover }}>
                  {collection.isExpanded ? (
                    <ChevronDown size={12} style={{ color: colors.textSecondary }} />
                  ) : (
                    <ChevronRight size={12} style={{ color: colors.textSecondary }} />
                  )}
                  <button onClick={(e) => {
                    e.stopPropagation();
                    toggleFavorite(collection.id);
                  }}>
                    {collection.isFavorite ? (
                      <Star size={12} fill="#FFB300" style={{ color: '#FFB300' }} />
                    ) : (
                      <Star size={12} style={{ color: colors.textSecondary }} />
                    )}
                  </button>
                  
                  {collection.isEditing ? (
                    <input
                      type="text"
                      defaultValue={collection.name}
                      onBlur={(e) => updateCollectionName(collection.id, e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          updateCollectionName(collection.id, e.target.value);
                        } else if (e.key === 'Escape') {
                          setCollections(cols => cols.map(col => 
                            col.id === collection.id ? { ...col, isEditing: false } : col
                          ));
                        }
                      }}
                      className="flex-1 text-sm font-medium bg-transparent border-none outline-none"
                      style={{ color: colors.text }}
                      autoFocus
                    />
                  ) : (
                    <span className="text-sm font-medium flex-1" style={{ color: colors.text }}>
                      {collection.name}
                    </span>
                  )}
                  
                  <button 
                    onClick={(e) => {
                      e.stopPropagation();
                      setCollections(cols => cols.map(col => 
                        col.id === collection.id ? { ...col, isEditing: true } : col
                      ));
                    }}
                    className="p-1 rounded opacity-0 group-hover:opacity-100 hover:bg-opacity-50 transition-all hover-lift"
                    style={{ backgroundColor: colors.card }}>
                    <Edit2 size={11} style={{ color: colors.textSecondary }} />
                  </button>
                </div>

                {collection.isExpanded && (
                  <>
                    {collection.folders.map(folder => (
                      <div key={folder.id} className="ml-4 mb-2">
                        <div className="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-opacity-50 transition-colors mb-1.5 cursor-pointer group hover-lift"
                          onClick={() => toggleFolder(collection.id, folder.id)}
                          style={{ backgroundColor: colors.hover }}>
                          {folder.isExpanded ? (
                            <ChevronDown size={11} style={{ color: colors.textSecondary }} />
                          ) : (
                            <ChevronRight size={11} style={{ color: colors.textSecondary }} />
                          )}
                          <FolderOpen size={11} style={{ color: colors.textSecondary }} />
                          
                          {folder.isEditing ? (
                            <input
                              type="text"
                              defaultValue={folder.name}
                              onBlur={(e) => updateFolderName(collection.id, folder.id, e.target.value)}
                              onKeyDown={(e) => {
                                if (e.key === 'Enter') {
                                  updateFolderName(collection.id, folder.id, e.target.value);
                                } else if (e.key === 'Escape') {
                                  setCollections(cols => cols.map(col => ({
                                    ...col,
                                    folders: col.folders.map(f => 
                                      f.id === folder.id ? { ...f, isEditing: false } : f
                                    )
                                  })));
                                }
                              }}
                              className="flex-1 text-sm bg-transparent border-none outline-none"
                              style={{ color: colors.text }}
                              autoFocus
                            />
                          ) : (
                            <span className="text-sm flex-1" style={{ color: colors.text }}>
                              {folder.name}
                            </span>
                          )}
                          
                          <button 
                            onClick={(e) => {
                              e.stopPropagation();
                              setCollections(cols => cols.map(col => ({
                                ...col,
                                folders: col.folders.map(f => 
                                  f.id === folder.id ? { ...f, isEditing: true } : f
                                )
                              })));
                            }}
                            className="p-1 rounded opacity-0 group-hover:opacity-100 hover:bg-opacity-50 transition-all hover-lift"
                            style={{ backgroundColor: colors.card }}>
                            <Edit2 size={10} style={{ color: colors.textSecondary }} />
                          </button>
                        </div>

                        {folder.isExpanded && (
                          <>
                            {folder.requests.map(request => (
                              <div key={request.id} className="flex items-center gap-2 ml-6 mb-1.5 group">
                                <button
                                  onClick={() => handleSelectRequest(request, collection.id, folder.id)}
                                  className="flex items-center gap-2 text-sm text-left transition-colors hover:text-opacity-80 flex-1 px-2 py-1.5 rounded hover:bg-opacity-50 hover-lift"
                                  style={{ 
                                    color: selectedRequest?.id === request.id ? colors.primary : colors.text,
                                    backgroundColor: selectedRequest?.id === request.id ? colors.selected : 'transparent'
                                  }}>
                                  <div className="w-2 h-2 rounded-full flex-shrink-0" style={{ 
                                    backgroundColor: getMethodColor(request.method)
                                  }} />
                                  
                                  {request.isEditing ? (
                                    <input
                                      type="text"
                                      defaultValue={request.name}
                                      onBlur={(e) => updateRequestName(collection.id, folder.id, request.id, e.target.value)}
                                      onKeyDown={(e) => {
                                        if (e.key === 'Enter') {
                                          updateRequestName(collection.id, folder.id, request.id, e.target.value);
                                        } else if (e.key === 'Escape') {
                                          setCollections(cols => cols.map(col => ({
                                            ...col,
                                            folders: col.folders.map(f => 
                                              f.id === folder.id ? {
                                                ...f,
                                                requests: f.requests.map(r => 
                                                  r.id === request.id ? { ...r, isEditing: false } : r
                                                )
                                              } : f
                                            )
                                          })));
                                        }
                                      }}
                                      className="flex-1 bg-transparent border-none outline-none"
                                      style={{ color: selectedRequest?.id === request.id ? colors.primary : colors.text }}
                                      autoFocus
                                    />
                                  ) : (
                                    <span className="truncate">{request.name}</span>
                                  )}
                                </button>
                                
                                {!request.isEditing && (
                                  <button 
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      setCollections(cols => cols.map(col => ({
                                        ...col,
                                        folders: col.folders.map(f => 
                                          f.id === folder.id ? {
                                            ...f,
                                            requests: f.requests.map(r => 
                                              r.id === request.id ? { ...r, isEditing: true } : r
                                            )
                                          } : f
                                        )
                                      })));
                                    }}
                                    className="p-1 rounded opacity-0 group-hover:opacity-100 hover:bg-opacity-50 transition-all mr-2 hover-lift"
                                    style={{ backgroundColor: colors.card }}>
                                    <Edit2 size={10} style={{ color: colors.textSecondary }} />
                                  </button>
                                )}
                              </div>
                            ))}
                            <button
                              onClick={() => addNewRequest(collection.id, folder.id)}
                              className="ml-6 px-3 py-1.5 text-xs rounded hover:bg-opacity-50 transition-colors flex items-center gap-1.5 mt-1 hover-lift"
                              style={{ backgroundColor: colors.hover, color: colors.textSecondary }}>
                              <Plus size={10} />
                              Add Request
                            </button>
                          </>
                        )}
                      </div>
                    ))}
                    <button
                      onClick={() => addNewFolder(collection.id)}
                      className="ml-4 px-3 py-1.5 text-xs rounded hover:bg-opacity-50 transition-colors flex items-center gap-1.5 mt-1 hover-lift"
                      style={{ backgroundColor: colors.hover, color: colors.textSecondary }}>
                      <Plus size={10} />
                      Add Folder
                    </button>
                  </>
                )}
              </div>
            ))}
            
            {filteredCollections.length === 0 && searchQuery && (
              <div className="text-center p-4" style={{ color: colors.textSecondary }}>
                <Search size={20} className="mx-auto mb-2 opacity-50" />
                <p className="text-sm">No collections found for "{searchQuery}"</p>
              </div>
            )}
          </div>
        </div>

        {/* MAIN WORKSPACE */}
        <div className="flex-1 flex flex-col overflow-hidden">
          {/* REQUEST TABS */}
          <div className="flex items-center border-b h-9" style={{ 
            backgroundColor: colors.card,
            borderColor: colors.border
          }}>
            <div className="flex items-center flex-1 overflow-x-auto px-2">
              {requestTabs.map(tab => (
                <div key={tab.id}
                  className={`flex items-center gap-2 px-3 py-2 border-r cursor-pointer min-w-32 max-w-48 hover-lift ${
                    tab.isActive ? '' : 'hover:bg-opacity-50 transition-colors'
                  }`}
                  style={{ 
                    backgroundColor: tab.isActive ? colors.card : colors.sidebar,
                    borderRightColor: colors.border,
                    borderTop: tab.isActive ? `2px solid ${colors.primary}` : '2px solid transparent'
                  }}
                  onClick={() => {
                    const collection = collections.find(c => c.id === tab.collectionId);
                    const folder = collection?.folders.find(f => f.id === tab.folderId);
                    const request = folder?.requests.find(r => r.id === tab.id);
                    if (request) {
                      handleSelectRequest(request, tab.collectionId, tab.folderId);
                    }
                  }}>
                  <div className="flex items-center gap-1.5 flex-1 min-w-0">
                    <div className="w-1.5 h-1.5 rounded-full flex-shrink-0" style={{ 
                      backgroundColor: getMethodColor(tab.method)
                    }} />
                    <span className="text-sm truncate" style={{ 
                      color: tab.isActive ? colors.text : colors.textSecondary
                    }}>
                      {tab.name}
                    </span>
                  </div>
                  <button onClick={(e) => {
                    e.stopPropagation();
                    if (requestTabs.length > 1) {
                      setRequestTabs(tabs => tabs.filter(t => t.id !== tab.id));
                      if (tab.isActive) {
                        const remainingTabs = requestTabs.filter(t => t.id !== tab.id);
                        if (remainingTabs.length > 0) {
                          const nextTab = remainingTabs[0];
                          const collection = collections.find(c => c.id === nextTab.collectionId);
                          const folder = collection?.folders.find(f => f.id === nextTab.folderId);
                          const request = folder?.requests.find(r => r.id === nextTab.id);
                          if (request) {
                            handleSelectRequest(request, nextTab.collectionId, nextTab.folderId);
                          }
                        }
                      }
                    } else {
                      showToast('Cannot close the last tab', 'error');
                    }
                  }} className="p-0.5 rounded opacity-0 hover:opacity-100 hover:bg-opacity-50 transition-colors hover-lift"
                    style={{ backgroundColor: colors.hover }}>
                    <X size={12} style={{ color: colors.textSecondary }} />
                  </button>
                </div>
              ))}
              <button
                onClick={() => {
                  const newRequest = {
                    id: `req-${Date.now()}`,
                    name: 'New Request',
                    method: 'GET',
                    url: '',
                    description: '',
                    status: 'unsaved',
                    lastModified: new Date().toISOString(),
                    auth: { type: 'noauth' },
                    params: [],
                    headers: [],
                    body: '',
                    tests: '',
                    preRequestScript: '',
                    isSaved: false
                  };
                  handleSelectRequest(newRequest, '', '');
                }}
                className="px-3 py-2 border-r hover:bg-opacity-50 transition-colors hover-lift"
                style={{ borderRightColor: colors.border, backgroundColor: colors.hover }}>
                <Plus size={12} style={{ color: colors.textSecondary }} />
              </button>
            </div>
          </div>

          {/* REQUEST BUILDER */}
          <div className="flex-1 overflow-hidden flex flex-col">
            {/* URL BAR */}
            <div className="flex items-center gap-2 p-4" style={{ 
              backgroundColor: colors.card
            }}>
              <select value={requestMethod} onChange={(e) => setRequestMethod(e.target.value)}
                className="px-3 py-2 rounded text-sm font-medium focus:outline-none hover-lift"
                style={{ 
                  backgroundColor: colors.inputBg,
                  color: getMethodColor(requestMethod),
                  border: `1px solid ${colors.inputBorder}`,
                  width: '100px'
                }}>
                {['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS'].map(method => (
                  <option key={method} value={method}>{method}</option>
                ))}
              </select>
              
              <div className="flex-1 flex items-center rounded overflow-hidden hover-lift" style={{ 
                border: `1px solid ${colors.inputBorder}`
              }}>
                <input type="text" value={requestUrl} onChange={(e) => setRequestUrl(e.target.value)}
                  className="flex-1 px-3 py-2 text-sm focus:outline-none min-w-0"
                  style={{ backgroundColor: colors.inputBg, color: colors.text }}
                  placeholder="Enter request URL" />
              </div>
              
              <button onClick={sendRequest} disabled={isSending}
                className={`px-4 py-2 rounded text-sm font-medium flex items-center gap-2 transition-colors min-w-32 hover-lift ${
                  isSending ? 'opacity-50 cursor-not-allowed' : 'hover:opacity-90'
                }`}
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                {isSending ? (
                  <>
                    <RefreshCw size={12} className="animate-spin" />
                    Sending...
                  </>
                ) : (
                  <>
                    <Send size={12} />
                    Send
                  </>
                )}
              </button>
              
              <button className="px-3 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
                onClick={saveRequestChanges}>
                Save
              </button>
            </div>

            {/* REQUEST TABS */}
            <div className="flex items-center border-t border-b" style={{ 
              backgroundColor: colors.card,
              borderColor: colors.border
            }}>
              {['Params', 'Authorization', 'Headers', 'Body', 'Pre-request Script', 'Tests', 'Settings'].map(tab => {
                const tabId = tab.toLowerCase().replace(' ', '-');
                return (
                  <button key={tabId} onClick={() => setActiveTab(tabId)}
                    className={`px-4 py-2.5 text-sm font-medium border-b-2 transition-colors hover-lift ${
                      activeTab === tabId ? '' : 'hover:bg-opacity-50'
                    }`}
                    style={{ 
                      borderBottomColor: activeTab === tabId ? colors.primary : 'transparent',
                      color: activeTab === tabId ? colors.primary : colors.textSecondary,
                      backgroundColor: 'transparent'
                    }}>
                    {tab}
                  </button>
                );
              })}
            </div>

            {/* REQUEST CONTENT */}
            <div className="flex-1 overflow-auto" style={{ backgroundColor: colors.card }}>
              {activeTab === 'params' && renderParamsTab()}
              {activeTab === 'authorization' && renderAuthTab()}
              {activeTab === 'headers' && renderHeadersTab()}
              {activeTab === 'body' && renderBodyTab()}
              {activeTab === 'tests' && (
                <div className="p-4">
                  <h3 className="text-sm font-semibold mb-4" style={{ color: colors.text }}>Tests</h3>
                  <textarea 
                    className="w-full h-64 font-mono text-sm p-4 border rounded resize-none focus:outline-none hover-lift"
                    style={{ 
                      backgroundColor: colors.inputBg,
                      borderColor: colors.border,
                      color: colors.text
                    }}
                    placeholder="pm.test('Status code is 200', function() {\n  pm.response.to.have.status(200);\n});"
                    onChange={(e) => {
                      // You can save this to state if needed
                    }}
                  />
                </div>
              )}
              {activeTab === 'settings' && (
                <div className="p-4">
                  <h3 className="text-sm font-semibold mb-4" style={{ color: colors.text }}>Settings</h3>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm" style={{ color: colors.text }}>Follow redirects</span>
                      <input type="checkbox" defaultChecked className="rounded hover-lift" />
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm" style={{ color: colors.text }}>Request timeout (ms)</span>
                      <input type="number" defaultValue="0" className="w-24 px-2 py-1 border rounded text-sm focus:outline-none hover-lift"
                        style={{ backgroundColor: colors.inputBg, borderColor: colors.border, color: colors.text }} />
                    </div>
                  </div>
                </div>
              )}
              {activeTab === 'pre-request-script' && (
                <div className="p-4">
                  <h3 className="text-sm font-semibold mb-4" style={{ color: colors.text }}>Pre-request Script</h3>
                  <textarea className="w-full h-64 font-mono text-sm p-4 border rounded resize-none focus:outline-none hover-lift"
                    style={{ 
                      backgroundColor: colors.inputBg,
                      borderColor: colors.border,
                      color: colors.text
                    }}
                    placeholder="// Write pre-request script here"
                  />
                </div>
              )}
            </div>
          </div>

          {/* RESIZABLE RESPONSE PANEL */}
          {renderResponsePanel()}
        </div>

        {/* RIGHT PANELS */}
        {renderRightPanel()}
      </div>

      {/* TOAST */}
      {renderToast()}

      {/* MODALS */}
      {renderImportModal()}
      {renderShareModal()}
      {renderSettingsModal()}
      {renderCreateModal()}
    </div>
  );
};

export default Collections;