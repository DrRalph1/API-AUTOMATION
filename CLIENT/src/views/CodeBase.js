import React, { useState, useEffect, useCallback } from 'react';
import { 
  ChevronRight, 
  ChevronDown,
  Search,
  Plus,
  Download,
  Copy,
  Settings,
  Code,
  Star,
  X,
  Check,
  AlertCircle,
  Clock,
  Activity,
  Folder,
  FolderOpen,
  FileCode,
  RefreshCw,
  Coffee,
  Box,
  Package,
  Terminal,
  Server,
  Cpu,
  Monitor,
  HardDrive,
  ShieldCheck,
  Layers,
  Zap,
  BookOpen,
  History,
  Database as DatabaseIcon,
  ExternalLink as ExternalLinkIcon,
  DownloadCloud,
  UploadCloud
} from 'lucide-react';

// Import CodeBaseController functions
import {
  getCollectionsListFromCodebase,
  getCollectionDetailsFromCodebase,
  getRequestDetailsFromCodebase,
  getImplementationDetails,
  generateImplementation,
  exportImplementation,
  getLanguages,
  searchImplementations,
  importSpecification,
  clearCodebaseCache,
  getAllImplementations,
  validateImplementation,
  testImplementation,
  handleCodebaseResponse,
  extractCodebaseCollectionsList,
  extractCodebaseCollectionDetails,
  extractCodebaseRequestDetails,
  extractImplementationDetails,
  extractGenerateResults,
  extractExportResults,
  extractLanguages,
  extractSearchResults,
  extractImportSpecResults,
  extractValidationResults,
  extractTestResults,
  validateGenerateImplementation,
  validateExportImplementation,
  validateSearchImplementation,
  validateImportSpecification,
  validateImplementationValidation,
  validateTestImplementation,
  getSupportedProgrammingLanguages,
  getSupportedComponents,
  getQuickStartGuide,
  getLanguageColor,
  formatCodeForDisplay,
  getFileExtension,
  getDefaultImplementationOptions,
  cacheCodebaseData,
  getCachedCodebaseData,
  clearCachedCodebaseData,
  getFolderRequestsFromCodebase,
  extractFolderRequests,
  extractAllImplementations,
  extractUserIdFromToken,
  extractSupportedProgrammingLanguages,
  extractQuickStartGuide
} from "../controllers/CodeBaseController.js";

// Enhanced SyntaxHighlighter Component with safe handling
const SyntaxHighlighter = ({ language, code }) => {
  const highlightSyntax = (code, lang) => {
    if (!code) return '// No code available';
    
    const codeString = String(code);
    
    if (lang === 'json') {
      return codeString
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
      return codeString
        .replace(/(\b(?:function|const|let|var|if|else|for|while|return|class|import|export|from|default|async|await|try|catch|finally|throw|new|this)\b)/g, '<span class="text-purple-400">$1</span>')
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"|'(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\'])*')/g, '<span class="text-green-400">$1</span>')
        .replace(/(\/\/.*)/g, '<span class="text-gray-500">$1</span>')
        .replace(/(\b\d+\b)/g, '<span class="text-blue-400">$1</span>');
    }
    
    if (lang === 'python') {
      return codeString
        .replace(/(\b(?:def|class|import|from|if|elif|else|for|while|try|except|finally|with|as|return|yield|async|await|lambda|in|is|not|and|or|True|False|None)\b)/g, '<span class="text-purple-400">$1</span>')
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"|'(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\'])*')/g, '<span className="text-green-400">$1</span>')
        .replace(/(#.*)/g, '<span className="text-gray-500">$1</span>');
    }
    
    if (lang === 'java') {
      return codeString
        .replace(/(\b(?:public|private|protected|class|interface|extends|implements|static|final|void|return|new|if|else|for|while|switch|case|break|continue|throw|throws|try|catch|finally|import|package)\b)/g, '<span class="text-purple-400">$1</span>')
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"|'(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\'])*')/g, '<span class="text-green-400">$1</span>')
        .replace(/(\/\/.*|\/\*[\s\S]*?\*\/)/g, '<span class="text-gray-500">$1</span>')
        .replace(/(@\w+)/g, '<span class="text-blue-400">$1</span>');
    }
    
    if (lang === 'csharp') {
      return codeString
        .replace(/(\b(?:public|private|protected|internal|class|interface|namespace|using|static|void|return|new|if|else|for|while|switch|case|break|continue|throw|try|catch|finally|async|await|var|dynamic|object|string|int|bool|double|decimal)\b)/g, '<span class="text-purple-400">$1</span>')
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"|'(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\'])*')/g, '<span class="text-green-400">$1</span>')
        .replace(/(\/\/.*|\/\*[\s\S]*?\*\/)/g, '<span class="text-gray-500">$1</span>')
        .replace(/(\b\d+\b)/g, '<span class="text-blue-400">$1</span>');
    }
    
    return codeString
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  };

  return (
    <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed" 
      dangerouslySetInnerHTML={{ 
        __html: highlightSyntax(code, language) || '// No code available'
      }} 
    />
  );
};

const CodeBase = ({ theme, isDark, customTheme, toggleTheme, authToken }) => {
  const [activeTab, setActiveTab] = useState('implementations');
  const [showCodePanel, setShowCodePanel] = useState(true);
  const [selectedLanguage, setSelectedLanguage] = useState('java');
  const [showLanguageDropdown, setShowLanguageDropdown] = useState(false);
  const [toast, setToast] = useState(null);
  const [showPublishModal, setShowPublishModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCollection, setSelectedCollection] = useState(null);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [isGeneratingCode, setIsGeneratingCode] = useState(false);
  const [collections, setCollections] = useState([]);
  const [expandedCollections, setExpandedCollections] = useState([]);
  const [expandedFolders, setExpandedFolders] = useState([]);
  const [showImportModal, setShowImportModal] = useState(false);
  const [selectedComponent, setSelectedComponent] = useState('controller');
  const [showAllFiles, setShowAllFiles] = useState(false);
  const [isLoading, setIsLoading] = useState({
    collections: false,
    requestDetails: false,
    implementationDetails: false,
    generateImplementation: false,
    searchImplementations: false,
    folderRequests: {} // Track loading state per folder
  });
  const [currentImplementation, setCurrentImplementation] = useState({});
  const [availableLanguages, setAvailableLanguages] = useState([]);
  const [allImplementations, setAllImplementations] = useState({});
  const [folderRequests, setFolderRequests] = useState({}); // Store requests per folder
  const [userId, setUserId] = useState('');

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
    inputBg: 'rgb(41 53 72 / 19%)',
    dropdownBg: 'rgb(41 53 72 / 19%)',
    modalBg: 'rgb(41 53 72 / 19%)',
    codeBg: 'rgb(41 53 72 / 19%)',
    method: {
      GET: 'rgb(52 211 153)',
      POST: 'rgb(96 165 250)',
      PUT: 'rgb(251 191 36)',
      DELETE: 'rgb(248 113 113)',
      PATCH: 'rgb(167 139 250)',
      HEAD: 'rgb(148 163 184)',
      OPTIONS: 'rgb(167 139 250)'
    }
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
    inputBg: '#ffffff',
    dropdownBg: '#ffffff',
    modalBg: '#ffffff',
    codeBg: '#f1f5f9',
    method: {
      GET: '#10b981',
      POST: '#3b82f6',
      PUT: '#f59e0b',
      DELETE: '#ef4444',
      PATCH: '#8b5cf6',
      HEAD: '#6b7280',
      OPTIONS: '#8b5cf6'
    }
  };

  // ==================== API METHODS ====================

  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  // Load collections from codebase
   const fetchCollectionsList = useCallback(async () => {
    console.log('🔥 [CodeBase] fetchCollectionsList called');
    
    if (!authToken) {
      console.log('❌ No auth token available');
      showToast('Authentication required. Please login.', 'error');
      return;
    }

    setIsLoading(prev => ({ ...prev, collections: true }));
    console.log('📡 [CodeBase] Fetching collections list...');

    try {
      const response = await getCollectionsListFromCodebase(authToken);
      console.log('📦 [CodeBase] Collections API response:', response);
      
      if (!response) {
        throw new Error('No response from codebase service');
      }
      
      const handledResponse = handleCodebaseResponse(response);
      const collectionsData = extractCodebaseCollectionsList(handledResponse);
      
      console.log('📊 [CodeBase] Extracted collections data:', collectionsData);
      
      // Format collections
      const formattedCollections = collectionsData.map(collection => ({
        ...collection,
        folders: [],
        requests: [],
        isExpanded: false,
        isFavorite: collection.isFavorite || false
      }));
      
      setCollections(formattedCollections);
      
      // Cache the data if we have userId
      const userId = extractUserIdFromToken(authToken);
      if (userId) {
        cacheCodebaseData(userId, 'collections', formattedCollections);
      }
      
      // Auto-select and load first collection if available
      if (formattedCollections.length > 0) {
        const firstCollection = formattedCollections[0];
        setSelectedCollection(firstCollection);
        
        // Expand and load details for first collection
        setExpandedCollections([firstCollection.id]);
        await fetchCollectionDetails(firstCollection.id);
      }
      
      showToast('Collections loaded successfully', 'success');
      
    } catch (error) {
      console.error('❌ [CodeBase] Error fetching collections:', error);
      showToast(`Failed to load collections: ${error.message}`, 'error');
      setCollections([]);
    } finally {
      setIsLoading(prev => ({ ...prev, collections: false }));
      console.log('🏁 [CodeBase] fetchCollectionsList completed');
    }
  }, [authToken]);

  // Load collection details
  const fetchCollectionDetails = useCallback(async (collectionId) => {
    console.log(`📡 [CodeBase] Fetching details for collection ${collectionId}`);
    
    if (!authToken || !collectionId) {
      console.log('Missing params for fetchCollectionDetails');
      return;
    }

    try {
      const response = await getCollectionDetailsFromCodebase(authToken, collectionId);
      console.log('📦 [CodeBase] Collection details response:', response);
      
      const handledResponse = handleCodebaseResponse(response);
      const details = extractCodebaseCollectionDetails(handledResponse);
      
      console.log('📊 [CodeBase] Extracted collection details:', details);
      
      if (details) {
        // Update the collection in state with folders
        setCollections(prevCollections => 
          prevCollections.map(collection => {
            if (collection.id === collectionId) {
              const updatedCollection = { 
                ...collection, 
                ...details,
                folders: (details.folders || []).map(folder => ({
                  id: folder.id || folder.folderId,
                  name: folder.name || folder.folderName,
                  description: folder.description,
                  hasRequests: folder.hasRequests || (folder.requestCount > 0) || false,
                  requestCount: folder.requestCount || folder.totalRequests || 0,
                  subfolders: folder.subfolders || []
                }))
              };
              
              return updatedCollection;
            }
            return collection;
          })
        );
        
        // Update selected collection if it's the one we're viewing
        if (selectedCollection?.id === collectionId) {
          setSelectedCollection(prev => ({ 
            ...prev, 
            ...details,
            folders: updatedCollection.folders
          }));
        }
      }
      
    } catch (error) {
      console.error('❌ [CodeBase] Error loading collection details:', error);
      showToast(`Failed to load collection details: ${error.message}`, 'error');
    }
  }, [authToken, selectedCollection]);

  // Load requests for a specific folder
const fetchFolderRequests = useCallback(async (collectionId, folderId) => {
  console.log(`📡 [CodeBase] Fetching requests for folder ${folderId} in collection ${collectionId}`);
  
  if (!authToken || !collectionId || !folderId) {
    console.log('Missing params for fetchFolderRequests');
    return;
  }

  // Set loading state for this specific folder
  setIsLoading(prev => ({ 
    ...prev, 
    folderRequests: { ...prev.folderRequests, [folderId]: true }
  }));

  try {
    const response = await getFolderRequestsFromCodebase(authToken, collectionId, folderId);
    console.log('📦 [CodeBase] Folder requests response:', response);
    
    const handledResponse = handleCodebaseResponse(response);
    const folderDetails = extractFolderRequests(handledResponse);
    
    console.log('📊 [CodeBase] Extracted folder requests:', folderDetails);
    
    if (folderDetails) {
      // Update folder requests in state
      setFolderRequests(prev => ({
        ...prev,
        [folderId]: folderDetails.requests || []
      }));
      
      console.log(`📊 [CodeBase] Loaded ${folderDetails.requests?.length || 0} requests for folder ${folderId}`);
      
      // Update the folder info in collections
      setCollections(prevCollections => 
        prevCollections.map(collection => {
          if (collection.id === collectionId) {
            return {
              ...collection,
              folders: collection.folders?.map(folder => 
                folder.id === folderId 
                  ? { ...folder, requestCount: folderDetails.requests?.length || 0 }
                  : folder
              ) || []
            };
          }
          return collection;
        })
      );
    }
    
  } catch (error) {
    console.error(`❌ [CodeBase] Error loading requests for folder ${folderId}:`, error);
    showToast(`Failed to load folder requests: ${error.message}`, 'error');
    
    // Set empty array if API fails
    setFolderRequests(prev => ({
      ...prev,
      [folderId]: []
    }));
  } finally {
    // Clear loading state for this folder
    setIsLoading(prev => ({ 
      ...prev, 
      folderRequests: { ...prev.folderRequests, [folderId]: false }
    }));
  }
}, [authToken]);

  // Load request details
  const fetchRequestDetails = useCallback(async (collectionId, requestId) => {
    console.log(`📡 [CodeBase] Fetching details for request ${requestId}`);
    
    if (!authToken || !collectionId || !requestId) {
      console.log('Missing params for fetchRequestDetails');
      return;
    }

    setIsLoading(prev => ({ ...prev, requestDetails: true }));
    
    try {
      const response = await getRequestDetailsFromCodebase(authToken, collectionId, requestId);
      console.log('📦 [CodeBase] Request details response:', response);
      
      const handledResponse = handleCodebaseResponse(response);
      const details = extractCodebaseRequestDetails(handledResponse);
      
      console.log('📊 [CodeBase] Extracted request details:', details);
      
      if (details) {
        setSelectedRequest(details);
        
        // Load implementation details for current language
        await fetchImplementationDetails(collectionId, requestId, selectedLanguage, selectedComponent);
      }
      
      showToast(`Loaded details for ${details?.name}`, 'success');
      
    } catch (error) {
      console.error('❌ [CodeBase] Error loading request details:', error);
      showToast(`Failed to load request details: ${error.message}`, 'error');
    } finally {
      setIsLoading(prev => ({ ...prev, requestDetails: false }));
    }
  }, [authToken, selectedLanguage, selectedComponent]);

  // Load implementation details
const fetchImplementationDetails = useCallback(async (collectionId, requestId, language, component) => {
  console.log(`📡 [CodeBase] Fetching implementation for ${language}/${component}`);
  
  if (!authToken || !collectionId || !requestId || !language || !component) {
    console.log('Missing params for fetchImplementationDetails');
    return;
  }

  setIsLoading(prev => ({ ...prev, implementationDetails: true }));
  
  try {
    const response = await getImplementationDetails(authToken, collectionId, requestId, language, component);
    console.log('📦 [CodeBase] Implementation details response:', response);
    
    const handledResponse = handleCodebaseResponse(response);
    const details = extractImplementationDetails(handledResponse);
    
    console.log('📊 [CodeBase] Extracted implementation details:', details);
    
    if (details?.code) {
      setCurrentImplementation(prev => ({
        ...prev,
        [language]: {
          ...prev[language],
          [component]: details.code
        }
      }));
      
      // Cache the implementation
      const userId = extractUserIdFromToken(authToken);
      if (userId) {
        cacheCodebaseData(userId, `${requestId}_${language}_${component}`, details.code);
      }
    } else {
      // Try to get from allImplementations cache
      const allImpl = allImplementations[language]?.[component];
      if (allImpl) {
        setCurrentImplementation(prev => ({
          ...prev,
          [language]: {
            ...prev[language],
            [component]: allImpl
          }
        }));
      } else {
        // Set empty if no implementation found
        setCurrentImplementation(prev => ({
          ...prev,
          [language]: {
            ...prev[language],
            [component]: `// No implementation available for ${component} in ${language}`
          }
        }));
      }
    }
    
  } catch (error) {
    console.error('❌ [CodeBase] Error loading implementation details:', error);
    
    // Try to get from cache
    const userId = extractUserIdFromToken(authToken);
    if (userId) {
      const cachedCode = getCachedCodebaseData(userId, `${requestId}_${language}_${component}`);
      if (cachedCode) {
        setCurrentImplementation(prev => ({
          ...prev,
          [language]: {
            ...prev[language],
            [component]: cachedCode
          }
        }));
      }
    }
    
    // Don't show toast for this as it's not critical
  } finally {
    setIsLoading(prev => ({ ...prev, implementationDetails: false }));
  }
}, [authToken, allImplementations]);


// Fetch quick start guide for language
const fetchQuickStartGuide = useCallback(async (language) => {
  console.log(`📡 [CodeBase] Fetching quick start guide for ${language}`);
  
  if (!authToken || !language) {
    console.log('Missing params for fetchQuickStartGuide');
    return null;
  }

  try {
    const response = await getQuickStartGuide(authToken, language);
    const handledResponse = handleCodebaseResponse(response);
    const guide = extractQuickStartGuide(handledResponse);
    
    return guide || getDefaultQuickStartGuide(language);
  } catch (error) {
    console.error('❌ [CodeBase] Error loading quick start guide:', error);
    return getDefaultQuickStartGuide(language);
  }
}, [authToken]);

  // Load all implementations for a request
const fetchAllImplementations = useCallback(async (collectionId, requestId) => {
  console.log(`📡 [CodeBase] Fetching all implementations for request ${requestId}`);
  
  if (!authToken || !collectionId || !requestId) {
    console.log('Missing params for fetchAllImplementations');
    return;
  }

  try {
    const response = await getAllImplementations(authToken, collectionId, requestId);
    console.log('📦 [CodeBase] All implementations response:', response);
    
    const handledResponse = handleCodebaseResponse(response);
    const allImplData = extractAllImplementations(handledResponse);
    
    if (allImplData) {
      setAllImplementations(allImplData.implementations || {});
      console.log('📊 [CodeBase] Loaded all implementations:', Object.keys(allImplData.implementations || {}));
      
      // Update current implementation if we have data for selected language
      if (selectedLanguage && allImplData.implementations?.[selectedLanguage]) {
        setCurrentImplementation(prev => ({
          ...prev,
          [selectedLanguage]: allImplData.implementations[selectedLanguage]
        }));
      }
    }
    
  } catch (error) {
    console.error('❌ [CodeBase] Error loading all implementations:', error);
    // Don't show toast for this as it's not critical
  }
}, [authToken, selectedLanguage]);

  // Generate implementation
  const generateImplementationAPI = async (generateRequest) => {
    console.log('📡 [CodeBase] Generating implementation...');
    
    if (!authToken) {
      console.log('❌ No auth token for generateImplementationAPI');
      throw new Error('Authentication required');
    }

    setIsLoading(prev => ({ ...prev, generateImplementation: true }));
    
    try {
      const errors = validateGenerateImplementation(generateRequest);
      if (errors.length > 0) {
        throw new Error(errors.join(', '));
      }
      
      const response = await generateImplementation(authToken, generateRequest);
      const handledResponse = handleCodebaseResponse(response);
      const results = extractGenerateResults(handledResponse);
      
      console.log('✅ [CodeBase] Implementation generated:', results);
      return results;
      
    } catch (error) {
      console.error('❌ [CodeBase] Error generating implementation:', error);
      throw error;
    } finally {
      setIsLoading(prev => ({ ...prev, generateImplementation: false }));
    }
  };

  // Export implementation
  const exportImplementationAPI = async (exportRequest) => {
    console.log('📡 [CodeBase] Exporting implementation...');
    
    if (!authToken) {
      console.log('❌ No auth token for exportImplementationAPI');
      throw new Error('Authentication required');
    }

    try {
      const errors = validateExportImplementation(exportRequest);
      if (errors.length > 0) {
        throw new Error(errors.join(', '));
      }
      
      const response = await exportImplementation(authToken, exportRequest);
      const handledResponse = handleCodebaseResponse(response);
      const results = extractExportResults(handledResponse);
      
      console.log('✅ [CodeBase] Implementation exported:', results);
      return results;
      
    } catch (error) {
      console.error('❌ [CodeBase] Error exporting implementation:', error);
      throw error;
    }
  };

  // Load available languages
const fetchLanguages = useCallback(async () => {
  console.log('📡 [CodeBase] Fetching languages...');
  
  if (!authToken) {
    console.log('❌ No auth token for fetchLanguages');
    return;
  }

  try {
    const response = await getLanguages(authToken);
    console.log('📦 [CodeBase] Languages response:', response);
    
    const handledResponse = handleCodebaseResponse(response);
    const languagesData = extractLanguages(handledResponse);
    
    if (languagesData && languagesData.length > 0) {
      const formattedLanguages = languagesData.map(lang => ({
        id: lang.value || lang.id || lang.language,
        name: lang.label || lang.name || lang.language,
        framework: lang.framework || lang.primaryFramework,
        color: lang.color || getLanguageColor(lang.value || lang.id || lang.language),
        icon: lang.icon || null,
        command: lang.command || lang.runCommand
      }));
      
      setAvailableLanguages(formattedLanguages);
      console.log('📊 [CodeBase] Loaded languages:', formattedLanguages.length);
      
      // Set default language if not set
      if (!selectedLanguage && formattedLanguages.length > 0) {
        setSelectedLanguage(formattedLanguages[0].id);
      }
    } else {
      // Fallback to default languages
      const defaultLanguages = getDefaultSupportedProgrammingLanguages();
      setAvailableLanguages(defaultLanguages);
      if (!selectedLanguage && defaultLanguages.length > 0) {
        setSelectedLanguage(defaultLanguages[0].value);
      }
    }
    
  } catch (error) {
    console.error('❌ [CodeBase] Error loading languages:', error);
    // Fallback to default languages
    const defaultLanguages = getDefaultSupportedProgrammingLanguages();
    setAvailableLanguages(defaultLanguages);
    if (!selectedLanguage && defaultLanguages.length > 0) {
      setSelectedLanguage(defaultLanguages[0].value);
    }
  }
}, [authToken]);

  const getMethodColor = (method) => {
    return colors.method[method] || colors.textSecondary;
  };

  const toggleCollection = async (collectionId) => {
    const isExpanding = !expandedCollections.includes(collectionId);
    
    setExpandedCollections(prev =>
      prev.includes(collectionId)
        ? prev.filter(id => id !== collectionId)
        : [...prev, collectionId]
    );
    
    // If expanding, load collection details
    if (isExpanding) {
      await fetchCollectionDetails(collectionId);
    }
  };

  const toggleFolder = async (folderId) => {
  const isExpanding = !expandedFolders.includes(folderId);
  
  setExpandedFolders(prev =>
    prev.includes(folderId)
      ? prev.filter(id => id !== folderId)
      : [...prev, folderId]
  );
  
  // If expanding and we have a selected collection, load folder requests
  if (isExpanding && selectedCollection) {
    console.log(`📁 [CodeBase] Expanding folder ${folderId}, loading requests...`);
    try {
      await fetchFolderRequests(selectedCollection.id, folderId);
    } catch (error) {
      console.error(`Error loading folder ${folderId} requests:`, error);
      // Keep folder expanded but show error in UI
    }
  }
};

  const handleSelectRequest = async (request, collection, folder) => {
  console.log('🎯 [CodeBase] Selecting request:', request.name);
  
  setSelectedRequest(request);
  setSelectedCollection(collection);
  setSelectedComponent('controller'); // Reset to controller when selecting new request
  
  // Fetch request details
  if (collection && request.id) {
    await fetchRequestDetails(collection.id, request.id);
    
    // Also fetch all implementations for this request
    await fetchAllImplementations(collection.id, request.id);
  }
  
  showToast(`Viewing implementation for ${request.name}`, 'info');
};

  const copyToClipboard = (text) => {
    if (!text) {
      showToast('No code to copy', 'warning');
      return;
    }
    
    navigator.clipboard.writeText(text);
    showToast('Copied to clipboard!', 'success');
  };

  const generateDownloadPackage = async () => {
    if (!selectedRequest || !selectedLanguage) {
      showToast('Please select a request and language first', 'warning');
      return;
    }
    
    setIsGeneratingCode(true);
    
    try {
      const exportRequest = {
        language: selectedLanguage,
        format: showAllFiles ? 'complete' : 'single',
        requestId: selectedRequest.id,
        collectionId: selectedCollection?.id
      };
      
      const results = await exportImplementationAPI(exportRequest);
      
      if (results?.success) {
        showToast('Package ready for download!', 'success');
      } else {
        showToast('Failed to generate package', 'error');
      }
    } catch (error) {
      console.error('Error generating download package:', error);
      showToast(error.message || 'Failed to generate package', 'error');
    } finally {
      setIsGeneratingCode(false);
    }
  };

  const getCurrentImplementation = () => {
    return currentImplementation[selectedLanguage] || {};
  };

  const getCurrentCode = () => {
    const implementation = getCurrentImplementation();
    return implementation[selectedComponent] || '// Implementation not available for this component';
  };

  const getAvailableComponents = () => {
  // First check components from current implementation
  const implementation = getCurrentImplementation();
  const componentsFromImpl = Object.keys(implementation);
  
  if (componentsFromImpl.length > 0) {
    return componentsFromImpl;
  }
  
  // Use controller's getSupportedComponents for selected language
  const supportedComponents = getSupportedComponents(selectedLanguage);
  
  if (supportedComponents && supportedComponents.length > 0) {
    return supportedComponents.map(comp => comp.value);
  }
  
  // Fallback to common components based on language
  const componentMap = {
    java: ['controller', 'service', 'repository', 'model', 'dto'],
    javascript: ['controller', 'service', 'model', 'routes'],
    python: ['fastapi', 'schemas', 'models', 'routes'],
    csharp: ['controller', 'service', 'model', 'repository'],
    php: ['controller', 'service', 'model'],
    go: ['handler', 'service', 'model'],
    ruby: ['controller', 'service', 'model'],
    kotlin: ['controller', 'service', 'model'],
    swift: ['controller', 'service', 'model'],
    rust: ['handler', 'service', 'model']
  };
  
  return componentMap[selectedLanguage] || ['controller', 'service', 'model'];
};

  const getLanguageIcon = (language) => {
    const icons = {
      java: <Coffee size={14} />,
      javascript: <FileCode size={14} />,
      python: <Code size={14} />,
      csharp: <Box size={14} />,
      php: <Package size={14} />,
      go: <Terminal size={14} />,
      ruby: <Server size={14} />,
      kotlin: <Cpu size={14} />,
      swift: <Monitor size={14} />,
      rust: <HardDrive size={14} />
    };
    return icons[language] || <Code size={14} />;
  };

  // Get requests for a specific folder
  const getFolderRequests = (folderId) => {
    return folderRequests[folderId] || [];
  };

  // Filter collections based on search
  const filteredCollections = collections.filter(collection => {
    if (!searchQuery) return true;
    const query = searchQuery.toLowerCase();
    return (
      collection.name?.toLowerCase().includes(query) ||
      collection.description?.toLowerCase().includes(query)
    );
  });

  // Initialize data
  useEffect(() => {
  console.log('🚀 [CodeBase] Component mounted, fetching data...');
  
  if (authToken) {
    // Use controller's function
    const extractedUserId = extractUserIdFromToken(authToken);
    setUserId(extractedUserId);
    
    // Clear cache to force fresh API call
    clearCachedCodebaseData(extractedUserId);
    
    // Fetch fresh data
    fetchCollectionsList().catch(error => {
      console.error('Error in fetchCollectionsList:', error);
    });
    
    fetchLanguages().catch(error => {
      console.error('Error in fetchLanguages:', error);
    });
  } else {
    console.log('🔒 [CodeBase] No auth token, skipping fetch');
  }
}, [authToken, fetchCollectionsList, fetchLanguages]);

  const renderCodePanel = () => {
    const currentLanguage = availableLanguages.find(lang => lang.id === selectedLanguage);
    const availableComponents = getAvailableComponents();
    const currentCode = getCurrentCode();
    
    return (
      <div className="w-80 border-l flex flex-col" style={{ 
        backgroundColor: colors.card,
        borderColor: colors.border
      }}>
        <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Code Implementation</h3>
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
                  {getLanguageIcon(selectedLanguage)}
                  <span>{currentLanguage?.name || 'Java'}</span>
                  <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                    backgroundColor: `${currentLanguage?.color || '#f89820'}20`,
                    color: currentLanguage?.color || '#f89820'
                  }}>
                    {currentLanguage?.framework || 'Spring Boot'}
                  </span>
                </div>
                <ChevronDown size={14} style={{ color: colors.textSecondary }} />
              </button>

              {showLanguageDropdown && (
                <div className="absolute left-0 right-0 top-full mt-1 py-2 rounded shadow-lg z-50 border"
                  style={{ 
                    backgroundColor: colors.bg,
                    borderColor: colors.border
                  }}>
                  {availableLanguages.map(lang => (
                    <button
                      key={lang.id}
                      onClick={() => {
                        setSelectedLanguage(lang.id);
                        setShowLanguageDropdown(false);
                        setSelectedComponent('controller');
                        
                        // Load implementation for the new language
                        if (selectedCollection && selectedRequest) {
                          fetchImplementationDetails(
                            selectedCollection.id, 
                            selectedRequest.id, 
                            lang.id, 
                            'controller'
                          );
                        }
                      }}
                      className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors"
                      style={{ 
                        backgroundColor: selectedLanguage === lang.id ? colors.selected : 'transparent',
                        color: selectedLanguage === lang.id ? colors.primary : colors.text
                      }}
                    >
                      {getLanguageIcon(lang.id)}
                      {lang.name}
                      <span className="text-xs ml-auto px-1.5 py-0.5 rounded" style={{ 
                        backgroundColor: `${lang.color}20`,
                        color: lang.color
                      }}>
                        {lang.framework}
                      </span>
                      {selectedLanguage === lang.id && <Check size={14} className="ml-2" style={{ color: colors.primary }} />}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

          {availableComponents.length > 0 && (
            <div>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Components</div>
              <div className="flex flex-wrap gap-1">
                {availableComponents.map(component => (
                  <button
                    key={component}
                    onClick={() => {
                      setSelectedComponent(component);
                      
                      // Load implementation for this component
                      if (selectedCollection && selectedRequest) {
                        fetchImplementationDetails(
                          selectedCollection.id, 
                          selectedRequest.id, 
                          selectedLanguage, 
                          component
                        );
                      }
                    }}
                    className={`px-2 py-1 text-xs rounded capitalize hover-lift ${
                      selectedComponent === component ? '' : 'hover:bg-opacity-50'
                    }`}
                    style={{ 
                      backgroundColor: selectedComponent === component ? colors.primaryDark : colors.hover,
                      color: selectedComponent === component ? 'white' : colors.text
                    }}
                  >
                    {component}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="flex-1 overflow-auto">
          <div className="p-4 border-b flex items-center justify-between" style={{ borderColor: colors.border }}>
            <div className="flex items-center gap-2">
              <FileCode size={12} style={{ color: colors.textSecondary }} />
              <span className="text-sm font-medium capitalize" style={{ color: colors.text }}>
                {selectedComponent.replace(/([A-Z])/g, ' $1').trim()}
              </span>
            </div>
            <button 
              onClick={() => copyToClipboard(currentCode)}
              className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors flex items-center gap-1 hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}
            >
              <Copy size={10} />
              Copy
            </button>
          </div>
          
          <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
            {isLoading.implementationDetails ? (
              <div className="text-center py-8">
                <RefreshCw size={16} className="animate-spin mx-auto mb-2" style={{ color: colors.textSecondary }} />
                <p className="text-sm" style={{ color: colors.textSecondary }}>Loading implementation...</p>
              </div>
            ) : (
              <SyntaxHighlighter 
                language={selectedLanguage}
                code={currentCode}
              />
            )}
          </div>
        </div>

        <div className="p-4 border-t space-y-2" style={{ borderColor: colors.border }}>
          <button 
            className="w-full py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center justify-center gap-2 hover-lift"
            onClick={() => copyToClipboard(currentCode)}
            style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
            <Copy size={12} />
            Copy Code
          </button>
          {/* <button 
            className="w-full py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center justify-center gap-2 hover-lift"
            onClick={generateDownloadPackage}
            disabled={isGeneratingCode}
            style={{ 
              backgroundColor: isGeneratingCode ? colors.textTertiary : colors.hover,
              color: isGeneratingCode ? colors.white : colors.text
            }}>
            {isGeneratingCode ? (
              <>
                <RefreshCw size={12} className="animate-spin" />
                Generating...
              </>
            ) : (
              <>
                <Download size={12} />
                Download Package
              </>
            )}
          </button> */}
        </div>
      </div>
    );
  };

  const renderImplementationContent = () => {
    const currentLanguage = availableLanguages.find(lang => lang.id === selectedLanguage);
    const implementation = getCurrentImplementation();
    const hasImplementation = implementation && Object.keys(implementation).length > 0;
    
    return (
      <div className="flex-1 overflow-auto p-8">
        <div className="max-w-6xl mx-auto">
          {/* Header */}
          <div className="mb-8">
            {selectedRequest ? (
              <>
                <div className="flex items-center gap-3 mb-2">
                  {selectedRequest.method && (
                    <div className="px-3 py-1 rounded text-sm font-medium" style={{ 
                      backgroundColor: getMethodColor(selectedRequest.method),
                      color: 'white'
                    }}>
                      {selectedRequest.method}
                    </div>
                  )}
                  <code className="text-lg font-mono" style={{ color: colors.text }}>
                    {selectedRequest.url || ''}
                  </code>
                </div>
                <h1 className="text-2xl font-semibold mb-4" style={{ color: colors.text }}>
                  {selectedRequest.name}
                </h1>
                <p className="text-base mb-6" style={{ color: colors.textSecondary }}>
                  {selectedRequest.description || 'No description available'}
                </p>
                
                <div className="flex flex-wrap items-center gap-4 text-sm mb-6">
                  {selectedCollection && (
                    <div style={{ color: colors.textTertiary }}>
                      <Folder size={12} className="inline mr-1" style={{ color: colors.textTertiary }} />
                      {selectedCollection.name}
                    </div>
                  )}
                  {selectedRequest.tags && selectedRequest.tags.length > 0 && (
                    <div className="flex items-center gap-2">
                      {selectedRequest.tags.map(tag => (
                        <span key={tag} className="text-xs px-2 py-1 rounded" style={{ 
                          backgroundColor: colors.hover,
                          color: colors.textSecondary
                        }}>
                          {tag}
                        </span>
                      ))}
                    </div>
                  )}
                  {selectedRequest.lastModified && (
                    <div style={{ color: colors.textTertiary }}>
                      <Clock size={12} className="inline mr-1" style={{ color: colors.textTertiary }} />
                      Last updated: {selectedRequest.lastModified}
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="text-center py-12">
                <Code size={48} className="mx-auto mb-4 opacity-50" style={{ color: colors.textSecondary }} />
                <h2 className="text-2xl font-semibold mb-4" style={{ color: colors.text }}>Select an API Endpoint</h2>
                <p className="text-base mb-6" style={{ color: colors.textSecondary }}>
                  Choose an endpoint from the left sidebar to view its implementation
                </p>
              </div>
            )}
          </div>

          {selectedRequest && (
            <>
              {/* Language & Framework Selection */}
              <div className="mb-8 p-6 rounded-xl border hover-lift" style={{ 
                backgroundColor: colors.card,
                borderColor: colors.border
              }}>
                <h2 className="text-lg font-semibold mb-4" style={{ color: colors.text }}>Select Implementation Language</h2>
                <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
                  {availableLanguages.map(lang => (
                    <button
                      key={lang.id}
                      onClick={() => {
                        setSelectedLanguage(lang.id);
                        setSelectedComponent('controller');
                        
                        // Load implementation for the new language
                        if (selectedCollection && selectedRequest) {
                          fetchImplementationDetails(
                            selectedCollection.id, 
                            selectedRequest.id, 
                            lang.id, 
                            'controller'
                          );
                        }
                      }}
                      className={`p-4 rounded-xl text-sm text-center hover-lift transition-all ${
                        selectedLanguage === lang.id ? 'ring-2 ring-offset-1' : ''
                      }`}
                      style={{ 
                        backgroundColor: selectedLanguage === lang.id ? colors.selected : colors.hover,
                        border: `1px solid ${selectedLanguage === lang.id ? colors.primary : colors.border}`,
                        color: colors.text,
                        boxShadow: selectedLanguage === lang.id ? `0 0 0 2px ${colors.primary}40` : 'none'
                      }}
                    >
                      <div className="flex flex-col items-center">
                        {getLanguageIcon(lang.id)}
                        <span className="mt-2 font-medium">{lang.name}</span>
                        <span className="text-xs mt-1" style={{ color: colors.textSecondary }}>{lang.framework}</span>
                        {selectedLanguage === lang.id && (
                          <Check size={16} className="mt-2" style={{ color: colors.primary }} />
                        )}
                      </div>
                    </button>
                  ))}
                </div>
              </div>

              {/* Implementation Content */}
              {hasImplementation ? (
                <div className="border rounded-xl overflow-hidden hover-lift" style={{ 
                  borderColor: colors.border,
                  backgroundColor: colors.card
                }}>
                  <div className="px-4 py-3 flex items-center justify-between" style={{ 
                    borderBottomColor: colors.border
                  }}>
                    <div className="flex items-center gap-3">
                      <div className="flex items-center gap-2">
                        {getLanguageIcon(selectedLanguage)}
                        <span className="font-medium" style={{ color: colors.text }}>
                          {currentLanguage?.name || 'Java'} Implementation
                        </span>
                        <span className="text-xs px-2 py-0.5 rounded" style={{ 
                          backgroundColor: `${currentLanguage?.color || '#f89820'}20`,
                          color: currentLanguage?.color || '#f89820'
                        }}>
                          {currentLanguage?.framework || 'Spring Boot'}
                        </span>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => copyToClipboard(getCurrentCode())}
                        className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift"
                        style={{ backgroundColor: colors.hover, color: colors.text }}
                      >
                        <Copy size={12} />
                        Copy
                      </button>
                    </div>
                  </div>
                  <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
                    <SyntaxHighlighter 
                      language={selectedLanguage}
                      code={getCurrentCode()}
                    />
                  </div>
                </div>
              ) : (
                <div className="text-center p-12" style={{ color: colors.textSecondary }}>
                  <Code size={48} className="mx-auto mb-4 opacity-50" />
                  <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>Implementation Not Available</h3>
                  <p className="mb-6">Complete implementation for {selectedLanguage} is not yet available for this endpoint.</p>
                  <button 
                    className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                    onClick={async () => {
                      if (!selectedCollection || !selectedRequest) {
                        showToast('Please select a collection and request first', 'warning');
                        return;
                      }
                      
                      try {
                        setIsGeneratingCode(true);
                        const generateRequest = {
                          requestId: selectedRequest.id,
                          collectionId: selectedCollection.id,
                          language: selectedLanguage,
                          components: ['controller', 'service', 'model']
                        };
                        
                        const results = await generateImplementationAPI(generateRequest);
                        
                        if (results?.success) {
                          showToast('Implementation generated successfully!', 'success');
                          // Reload implementation details
                          await fetchImplementationDetails(
                            selectedCollection.id, 
                            selectedRequest.id, 
                            selectedLanguage, 
                            selectedComponent
                          );
                        } else {
                          showToast('Failed to generate implementation', 'error');
                        }
                      } catch (error) {
                        console.error('Error generating implementation:', error);
                        showToast(error.message || 'Failed to generate implementation', 'error');
                      } finally {
                        setIsGeneratingCode(false);
                      }
                    }}
                    disabled={isGeneratingCode}
                    style={{ 
                      backgroundColor: isGeneratingCode ? colors.textTertiary : colors.primaryDark, 
                      color: colors.white 
                    }}>
                    {isGeneratingCode ? 'Generating...' : 'Generate Implementation'}
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    );
  };

  const renderMainContent = () => {
    switch (activeTab) {
      case 'generate':
        return (

          <div className="flex-1 flex flex-col overflow-hidden">
            {/* Implementation Tabs */}
            <div className="flex items-center border-b h-9" style={{ 
              backgroundColor: colors.card,
              borderColor: colors.border
            }}>
              <div className="flex items-center px-2">
                <button
                  onClick={() => setActiveTab('implementations')}
                  className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${
                    activeTab === 'implementations' ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === 'implementations' ? colors.primary : 'transparent',
                    color: activeTab === 'implementations' ? colors.primary : colors.textSecondary,
                    backgroundColor: 'transparent'
                  }}>
                  Implementations
                </button>
                
                <button
                  onClick={() => setActiveTab('generate')}
                  className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${
                    activeTab === 'generate' ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === 'generate' ? colors.primary : 'transparent',
                    color: activeTab === 'generate' ? colors.primary : colors.textSecondary,
                    backgroundColor: 'transparent'
                  }}>
                  Generate Code
                </button>
              </div>
            </div>

          
            <div className="max-w-4xl mx-auto">
              <br /><br />
              <h2 className="text-2xl font-semibold mb-6" style={{ color: colors.text }}>Generate Code</h2>
              <p className="mb-6" style={{ color: colors.textSecondary }}>Generate complete implementations for your APIs in any language.</p>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
                {[
                  { title: 'From OpenAPI Spec', desc: 'Import OpenAPI/Swagger specification', icon: <FileCode size={24} /> },
                  { title: 'From Postman', desc: 'Import Postman collection', icon: <Package size={24} /> },
                  { title: 'From cURL', desc: 'Convert cURL command to code', icon: <Terminal size={24} /> },
                  { title: 'Custom Template', desc: 'Use custom code templates', icon: <Code size={24} /> }
                ].map(item => (
                  <button key={item.title} className="border rounded-xl p-6 text-left hover:border-opacity-50 transition-colors hover-lift"
                    onClick={() => setShowImportModal(true)}
                    style={{ 
                      borderColor: colors.border,
                      backgroundColor: colors.card
                    }}>
                    <div className="flex items-center gap-3 mb-3">
                      <div className="p-2 rounded" style={{ backgroundColor: `${colors.primary}20` }}>
                        {item.icon}
                      </div>
                      <h3 className="font-semibold" style={{ color: colors.text }}>{item.title}</h3>
                    </div>
                    <p className="text-sm" style={{ color: colors.textSecondary }}>{item.desc}</p>
                  </button>
                ))}
              </div>
            </div>
          </div>
        );
        
      case 'implementations':
      default:
        return (
          <div className="flex-1 flex flex-col overflow-hidden">
            {/* Implementation Tabs */}
            <div className="flex items-center border-b h-9" style={{ 
              backgroundColor: colors.card,
              borderColor: colors.border
            }}>
              <div className="flex items-center px-2">
                <button
                  onClick={() => setActiveTab('implementations')}
                  className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${
                    activeTab === 'implementations' ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === 'implementations' ? colors.primary : 'transparent',
                    color: activeTab === 'implementations' ? colors.primary : colors.textSecondary,
                    backgroundColor: 'transparent'
                  }}>
                  Implementations
                </button>
                
                <button
                  onClick={() => setActiveTab('generate')}
                  className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${
                    activeTab === 'generate' ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === 'generate' ? colors.primary : 'transparent',
                    color: activeTab === 'generate' ? colors.primary : colors.textSecondary,
                    backgroundColor: 'transparent'
                  }}>
                  Generate Code
                </button>
              </div>
            </div>

            {/* Implementation Content */}
            {renderImplementationContent()}
          </div>
        );
    }
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
        
        .hover-lift:hover {
          transform: translateY(-2px);
          transition: transform 0.2s ease;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
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
      `}</style>

      {/* TOP NAVIGATION */}
      <div className="flex items-center justify-between h-10 px-4 border-b" style={{ 
        backgroundColor: colors.header,
        borderColor: colors.border
      }}>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-1 -ml-3 text-nowrap">
            <span className="px-3 py-1.5 text-sm font-medium rounded transition-colors uppercase" style={{ color: colors.text }}>
              API Code Base
            </span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Code Panel Toggle */}
          <button onClick={() => setShowCodePanel(!showCodePanel)} 
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: showCodePanel ? colors.selected : colors.hover }}>
            <Code size={14} style={{ color: showCodePanel ? colors.primary : colors.textSecondary }} />
          </button>

          {/* Refresh Button */}
          <button 
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            onClick={async () => {
              try {
                await fetchCollectionsList();
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
                  onClick={() => setShowImportModal(true)}
                  style={{ backgroundColor: colors.hover }}>
                  <Plus size={12} style={{ color: colors.textSecondary }} />
                </button>
              </div>
            </div>
            <div className="relative">
              <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
              <input 
                type="text" 
                placeholder="Search collections..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-8 pr-3 py-2 rounded text-sm focus:outline-none hover-lift"
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

          <div className="flex-1 overflow-auto p-2">
            {isLoading.collections ? (
              <div className="text-center py-8" style={{ color: colors.textSecondary }}>
                <RefreshCw size={16} className="animate-spin mx-auto mb-2" />
                <p className="text-sm">Loading collections...</p>
              </div>
            ) : filteredCollections.length === 0 ? (
              <div className="text-center p-4" style={{ color: colors.textSecondary }}>
                <DatabaseIcon size={20} className="mx-auto mb-2 opacity-50" />
                <p className="text-sm">No collections available</p>
                <button className="mt-4 px-3 py-1.5 text-xs rounded hover:bg-opacity-50 transition-colors hover-lift"
                  onClick={async () => {
                    try {
                      await fetchCollectionsList();
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
                        // Toggle favorite
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
                      
                      {collection.version && (
                        <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                          backgroundColor: colors.hover,
                          color: colors.textSecondary
                        }}>
                          {collection.version}
                        </span>
                      )}
                    </div>

                    {expandedCollections.includes(collection.id) && collection.folders && collection.folders.length > 0 && (
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
                              
                              {folder.hasRequests && (
                                <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                                  backgroundColor: `${colors.success}20`,
                                  color: colors.success
                                }}>
                                  {folder.requestCount || '?'}
                                </span>
                              )}
                            </div>

                           {expandedFolders.includes(folder.id) && (
                              <div className="ml-6">
                                {isLoading.folderRequests[folder.id] ? (
                                  <div className="py-2 text-center">
                                    <RefreshCw size={12} className="animate-spin mx-auto mb-1" style={{ color: colors.textSecondary }} />
                                    <p className="text-xs" style={{ color: colors.textTertiary }}>Loading endpoints...</p>
                                  </div>
                                ) : (
                                  <>
                                    {getFolderRequests(folder.id).length > 0 ? (
                                      getFolderRequests(folder.id).map(request => {
                                        // Check if request has the expected structure
                                        const requestId = request.id || request.requestId;
                                        const requestName = request.name || request.requestName;
                                        const requestMethod = request.method;
                                        
                                        if (!requestId || !requestName) return null;
                                        
                                        return (
                                          <div key={requestId} className="flex items-center gap-2 mb-1.5 group">
                                            <button
                                              onClick={() => handleSelectRequest(request, collection, folder)}
                                              className="flex items-center gap-2 text-sm text-left transition-colors flex-1 px-2 py-1.5 rounded hover:bg-opacity-50 hover-lift"
                                              style={{ 
                                                color: selectedRequest?.id === requestId ? colors.primary : colors.text,
                                                backgroundColor: selectedRequest?.id === requestId ? colors.selected : 'transparent'
                                              }}>
                                              {requestMethod && (
                                                <div className="w-2 h-2 rounded-full flex-shrink-0" style={{ 
                                                  backgroundColor: getMethodColor(requestMethod)
                                                }} />
                                              )}
                                              
                                              <span className="truncate">{requestName}</span>
                                            </button>
                                          </div>
                                        );
                                      })
                                    ) : (
                                      <div className="py-2 text-center">
                                        <p className="text-xs" style={{ color: colors.textTertiary }}>
                                          {folder.hasRequests ? 'No endpoints available' : 'No endpoints in this folder'}
                                        </p>
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
                ))}
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
      {toast && (
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
      )}
    </div>
  );
};

export default CodeBase;