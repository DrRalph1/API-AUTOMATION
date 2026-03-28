import React, { useState, useEffect, useCallback, useRef } from 'react';
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
  Loader,
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

// Enhanced SyntaxHighlighter Component with safe handling
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
      
      if (lang === 'java') {
        // Handle multi-line comment boundaries
        if (highlightedLine.includes('/*') || highlightedLine.includes('*/')) {
          // For lines with comment markers, highlight the whole line as comment
          // to avoid breaking the HTML structure
          return (
            <div key={lineIndex} className="text-gray-500">
              {highlightedLine}
            </div>
          );
        }
        
        // Highlight strings (double quotes) - do this first
        highlightedLine = highlightedLine.replace(/"([^"\\]*(\\.[^"\\]*)*)"/g, 
          '<span class="text-green-400">"$1"</span>');
        
        // Highlight characters (single quotes)
        highlightedLine = highlightedLine.replace(/'([^'\\]*(\\.[^'\\]*)*)'/g, 
          '<span class="text-green-400">\'$1\'</span>');
        
        // Highlight single-line comments (but not inside strings)
        // Only apply if the line doesn't contain a string that might have //
        if (!highlightedLine.includes('class="text-green-400"')) {
          highlightedLine = highlightedLine.replace(/(\/\/.*)/g, 
            '<span class="text-gray-500">$1</span>');
        }
        
        // Highlight annotations
        highlightedLine = highlightedLine.replace(/(@\w+)/g, 
          '<span class="text-blue-400">$1</span>');
        
        // Highlight keywords - expanded list
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
          // Use negative lookbehind to avoid matching inside existing spans
          // But since not all browsers support lookbehind, we'll use a simpler approach
          // Only replace if not inside an HTML tag
          const keywordRegex = new RegExp('\\b(' + keyword + ')\\b(?!([^<]*>|[^>]*<\\/))', 'g');
          highlightedLine = highlightedLine.replace(keywordRegex, 
            '<span class="text-purple-400">$1</span>');
        });
        
        // Highlight numbers - FIXED: Don't apply to numbers that are part of class names like "400"
        if (!highlightedLine.includes('class="text-green-400"') && 
            !highlightedLine.includes('class="text-gray-500"')) {
          // Only highlight numbers that are standalone and not part of a class attribute
          highlightedLine = highlightedLine.replace(/\b(\d+[lLfFdD]?)\b(?![^<]*>|[^>]*<\/)/g, 
            '<span class="text-blue-400">$1</span>');
        }
        
        // Fix any malformed spans - CRITICAL FIX
        // This cleans up any incorrectly formatted span tags
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
  
  // Ref to track if this is the first load
  const isFirstLoad = useRef(true);
  // Ref for global loading state
  const globalLoadingRef = useRef(false);
  // Add loading state for overlay
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

  // Enhanced loading overlay component matching UserManagement style
  const LoadingOverlay = () => {
    if (!globalLoading) return null;
    
    return (
      <div className="fixed inset-0 flex items-center justify-center z-50" style={{ backgroundColor: colors.bg }}>
        <div className="text-center">
          <div className="relative">
            <Loader className="animate-spin mx-auto mb-6" size={64} style={{ color: colors.primary }} />
            <div className="absolute inset-0 flex items-center justify-center">
              <FileCode size={32} style={{ color: colors.primary, opacity: 0.3 }} />
            </div>
          </div>
          <h3 className="text-xl font-semibold mb-2" style={{ color: colors.text }}>
            Loading Code Base
          </h3>
          <p className="text-sm mb-2" style={{ color: colors.textSecondary }}>
            Please wait while we prepare your collections and implementations
          </p>
          <p className="text-xs" style={{ color: colors.textTertiary }}>
            Fetching API collections and generating code...
          </p>
        </div>
      </div>
    );
  };

  // Wrapper for async operations with global loading
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
      
      // Format and sort collections alphabetically
      const formattedCollections = collectionsData.map(collection => ({
        ...collection,
        folders: [],
        requests: [],
        isExpanded: false,
        isFavorite: collection.isFavorite || false
      }));
      
      // Sort collections alphabetically by name
      const sortedCollections = sortAlphabetically(formattedCollections, 'name');
      
      setCollections(sortedCollections);
      
      // Cache the data if we have userId
      const userId = extractUserIdFromToken(authToken);
      if (userId) {
        cacheCodebaseData(userId, 'collections', sortedCollections);
      }
      
      // In fetchCollectionsList, when auto-selecting first collection:
      if (sortedCollections.length > 0) {
        // Load details for ALL collections first
        await loadAllCollectionDetails(sortedCollections);
        
        // After details are loaded, the auto-selection will happen inside loadAllCollectionDetails
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


  // Load details for all collections (similar to Documentation component)
const loadAllCollectionDetails = useCallback(async (basicCollections) => {
  console.log('📡 [CodeBase] Loading details for all collections...');
  
  if (!authToken) {
    console.log('❌ No auth token for loadAllCollectionDetails');
    return;
  }

  try {
    setIsLoading(prev => ({ ...prev, folders: true }));
    
    const collectionsWithDetails = await Promise.all(
      basicCollections.map(async (collection) => {
        try {
          console.log(`📡 [CodeBase] Fetching details for collection: ${collection.name}`);
          const response = await getCollectionDetailsFromCodebase(authToken, collection.id);
          const handledResponse = handleCodebaseResponse(response);
          const details = extractCodebaseCollectionDetails(handledResponse);
          
          if (details) {
            // Process folders with request counts
            const processedFolders = (details.folders || []).map(folder => ({
              id: folder.id || folder.folderId,
              name: folder.name || folder.folderName,
              description: folder.description || '',
              collectionId: folder.collectionId || collection.id,
              requests: folder.requests || [],
              requestCount: folder.requests ? folder.requests.length : 0,
              hasRequests: (folder.requests && folder.requests.length > 0),
              isLoading: false,
              error: null
            }));
            
            // Sort folders alphabetically by name
            const sortedFolders = sortAlphabetically(processedFolders, 'name');
            
            // Sort requests within each folder alphabetically
            const foldersWithSortedRequests = sortedFolders.map(folder => ({
              ...folder,
              requests: sortAlphabetically(folder.requests || [], 'name')
            }));
            
            // Store folder requests in state for quick access
            const folderRequestsMap = {};
            foldersWithSortedRequests.forEach(folder => {
              if (folder.requests.length > 0) {
                folderRequestsMap[folder.id] = folder.requests;
              }
            });
            
            if (Object.keys(folderRequestsMap).length > 0) {
              setFolderRequests(prev => ({
                ...prev,
                ...folderRequestsMap
              }));
            }
            
            return {
              ...collection,
              folders: foldersWithSortedRequests,
              totalFolders: details.totalFolders || sortedFolders.length,
              totalEndpoints: details.totalEndpoints || 
                foldersWithSortedRequests.reduce((sum, f) => sum + f.requests.length, 0)
            };
          }
          
          return collection;
          
        } catch (error) {
          console.error(`❌ Error loading details for collection ${collection.id}:`, error);
          return collection;
        }
      })
    );
    
    // Sort collections again after adding details
    const sortedCollections = sortAlphabetically(collectionsWithDetails, 'name');
    console.log('📊 [CodeBase] Collections with details (sorted):', 
      sortedCollections.map(c => ({ name: c.name, totalEndpoints: c.totalEndpoints })));
    
    setCollections(sortedCollections);
    
    // Auto-select first collection and its first folder/request
    if (sortedCollections.length > 0) {
      const firstCollection = sortedCollections[0];
      setSelectedCollection(firstCollection);
      setExpandedCollections([firstCollection.id]);
      
      const firstFolderWithRequests = firstCollection.folders?.find(f => f.requests && f.requests.length > 0);
      if (firstFolderWithRequests) {
        setExpandedFolders([firstFolderWithRequests.id]);
        
        if (firstFolderWithRequests.requests.length > 0 && !selectedRequest) {
          const firstEndpoint = firstFolderWithRequests.requests[0];
          console.log(`🎯 [CodeBase] Auto-selecting first endpoint: ${firstEndpoint.name}`);
          setSelectedRequest(firstEndpoint);
          await fetchRequestDetails(firstCollection.id, firstEndpoint.id);
          await fetchAllImplementations(firstCollection.id, firstEndpoint.id);
          showToast(`Viewing implementation for ${firstEndpoint.name}`, 'info');
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
}, [authToken, selectedRequest, fetchRequestDetails, fetchAllImplementations, showToast]);


  // DEBUG - Monitor selectedCollection.folders changes
useEffect(() => {
  if (selectedCollection?.folders?.length > 0) {
    console.log('🔥🔥🔥 [CodeBase] FOLDERS ARE NOW AVAILABLE!', {
      folders: selectedCollection.folders.map(f => f.name),
      count: selectedCollection.folders.length
    });
  }
}, [selectedCollection?.folders]);

// DEBUG - Track selectedCollection folders changes
useEffect(() => {
  console.log('🔍 [CodeBase] selectedCollection UPDATED:', {
    id: selectedCollection?.id,
    name: selectedCollection?.name,
    foldersCount: selectedCollection?.folders?.length,
    folders: selectedCollection?.folders?.map(f => f.name),
    hasFolders: selectedCollection?.folders?.length > 0
  });
  
  // If folders are now available, this should trigger auto-expand
  if (selectedCollection?.folders?.length > 0) {
    console.log('🚀 [CodeBase] Folders are NOW AVAILABLE! Auto-expand should trigger...');
  }
}, [selectedCollection]);

  // Add this ref near your other refs
const selectedCollectionRef = useRef(null);

// Update the ref whenever selectedCollection changes
useEffect(() => {
  selectedCollectionRef.current = selectedCollection;
}, [selectedCollection]);

// Load collection details - FINAL FIX with ref
// Load collection details - MODIFIED to extract requests directly from folders
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
      // Process folders and sort them alphabetically
      const foldersWithRequests = (details.folders || []).map(folder => ({
        id: folder.id || folder.folderId,
        name: folder.name || folder.folderName,
        description: folder.description,
        hasRequests: folder.requests ? folder.requests.length > 0 : false,
        requestCount: folder.requests ? folder.requests.length : 0,
        subfolders: folder.subfolders || []
      }));
      
      // Sort folders alphabetically by name
      const sortedFolders = sortAlphabetically(foldersWithRequests, 'name');
      
      console.log('📁 [CodeBase] Creating sorted folders with requests:', 
        sortedFolders.map(f => ({ 
          name: f.name, 
          requestCount: f.requestCount,
          hasRequests: f.hasRequests 
        })));
      
      // Update collections state with sorted folders
      setCollections(prevCollections => {
        const updated = prevCollections.map(collection => {
          if (collection.id === collectionId) {
            return { 
              ...collection, 
              ...details,
              folders: sortedFolders
            };
          }
          return collection;
        });
        return updated;
      });
      
      // CRITICAL: Store the requests from the folders in folderRequests state
      if (details.folders) {
        const folderRequestsMap = {};
        details.folders.forEach(folder => {
          if (folder.requests && folder.requests.length > 0) {
            // Sort requests within each folder alphabetically by name
            const sortedRequests = sortAlphabetically(folder.requests, 'name');
            folderRequestsMap[folder.id] = sortedRequests;
            console.log(`📦 [CodeBase] Storing ${sortedRequests.length} sorted requests for folder ${folder.id}`);
          }
        });
        
        if (Object.keys(folderRequestsMap).length > 0) {
          setFolderRequests(prev => ({
            ...prev,
            ...folderRequestsMap
          }));
        }
      }
      
      // CRITICAL: Update selectedCollection using the ref to get the latest value
      if (selectedCollectionRef.current?.id === collectionId) {
        console.log('🔄 [CodeBase] Scheduling selectedCollection update with sorted folders');
        
        // Use setTimeout to ensure this runs after the current render cycle
        setTimeout(() => {
          console.log('🔄 [CodeBase] EXECUTING selectedCollection update NOW');
          
          // Use the ref to get the latest selectedCollection
          const currentSelected = selectedCollectionRef.current;
          
          if (currentSelected?.id === collectionId) {
            const updatedCollection = {
              ...currentSelected,
              ...details,
              folders: [...sortedFolders] // New array reference with sorted folders
            };
            
            console.log('📁 [CodeBase] Setting selectedCollection with sorted folders:', 
              updatedCollection.folders.map(f => ({ 
                name: f.name, 
                requestCount: f.requestCount 
              })));
            
            setSelectedCollection(updatedCollection);
          }
        }, 10); // Small delay to ensure it runs after render
      }
    }
    
  } catch (error) {
    console.error('❌ [CodeBase] Error loading collection details:', error);
    showToast(`Failed to load collection details: ${error.message}`, 'error');
  }
}, [authToken]);

// Load requests for a specific folder - MODIFIED to avoid duplicate API calls
const fetchFolderRequests = useCallback(async (collectionId, folderId) => {
  console.log(`📡 [CodeBase] Fetching requests for folder ${folderId} in collection ${collectionId}`);
  
  if (!authToken || !collectionId || !folderId) {
    console.log('Missing params for fetchFolderRequests');
    return [];
  }

  // Check if we already have requests for this folder
  if (folderRequests[folderId] && folderRequests[folderId].length > 0) {
    console.log(`✅ [CodeBase] Using cached requests for folder ${folderId}: ${folderRequests[folderId].length} requests`);
    return folderRequests[folderId];
  }

  // Check if the folder in selectedCollection already has requests
  if (selectedCollection?.folders) {
    const folder = selectedCollection.folders.find(f => f.id === folderId);
    if (folder && folder.requests && folder.requests.length > 0) {
      const sortedRequests = sortAlphabetically(folder.requests, 'name');
      console.log(`✅ [CodeBase] Using sorted requests from collection for folder ${folderId}: ${sortedRequests.length} requests`);
      setFolderRequests(prev => ({
        ...prev,
        [folderId]: sortedRequests
      }));
      return sortedRequests;
    }
  }

  // Set loading state for this specific folder
  setIsLoading(prev => ({ 
    ...prev, 
    folderRequests: { ...prev.folderRequests, [folderId]: true }
  }));

  let requests = [];

  try {
    const response = await getFolderRequestsFromCodebase(authToken, collectionId, folderId);
    console.log('📦 [CodeBase] Folder requests response:', response);
    
    const handledResponse = handleCodebaseResponse(response);
    const folderDetails = extractFolderRequests(handledResponse);
    
    console.log('📊 [CodeBase] Extracted folder requests:', folderDetails);
    
    if (folderDetails) {
      requests = folderDetails.requests || [];
      // Sort requests alphabetically by name
      const sortedRequests = sortAlphabetically(requests, 'name');
      
      // Update folder requests in state
      setFolderRequests(prev => ({
        ...prev,
        [folderId]: sortedRequests
      }));
      
      console.log(`📊 [CodeBase] Loaded ${sortedRequests.length} sorted requests for folder ${folderId}`);
      
      // Update the folder with actual count and sorted requests
      setCollections(prevCollections => 
        prevCollections.map(collection => {
          if (collection.id === collectionId) {
            return {
              ...collection,
              folders: collection.folders?.map(folder => 
                folder.id === folderId 
                  ? { 
                      ...folder, 
                      requestCount: sortedRequests.length,
                      hasRequests: sortedRequests.length > 0,
                      requests: sortedRequests // Store sorted requests directly in folder
                    }
                  : folder
              ) || []
            };
          }
          return collection;
        })
      );
      
      // Also update the selected collection if needed
      if (selectedCollection?.id === collectionId) {
        setSelectedCollection(prev => ({
          ...prev,
          folders: prev.folders?.map(folder => 
            folder.id === folderId 
              ? { 
                  ...folder, 
                  requestCount: sortedRequests.length,
                  hasRequests: sortedRequests.length > 0,
                  requests: sortedRequests // Store sorted requests directly in folder
                }
              : folder
          ) || []
        }));
      }
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
  
  return requests;
}, [authToken, selectedCollection, folderRequests]);

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

  // Load implementation details - FIXED to use allImplementations as primary source
const fetchImplementationDetails = useCallback(async (collectionId, requestId, language, component) => {
  console.log(`📡 [CodeBase] Fetching implementation for ${language}/${component}`);
  
  if (!authToken || !collectionId || !requestId || !language || !component) {
    console.log('Missing params for fetchImplementationDetails');
    return;
  }

  setIsLoading(prev => ({ ...prev, implementationDetails: true }));
  
  try {
    // First check if we already have this implementation in allImplementations
    const existingImpl = allImplementations[language]?.[component];
    
    if (existingImpl) {
      console.log(`✅ [CodeBase] Using existing implementation from allImplementations for ${language}/${component}`);
      setCurrentImplementation(prev => ({
        ...prev,
        [language]: {
          ...(prev[language] || {}),
          [component]: existingImpl
        }
      }));
      return;
    }

    // If not in allImplementations, try to fetch from API
    const response = await getImplementationDetails(authToken, collectionId, requestId, language, component);
    console.log('📦 [CodeBase] Implementation details response:', response);
    
    const handledResponse = handleCodebaseResponse(response);
    const details = extractImplementationDetails(handledResponse);
    
    console.log('📊 [CodeBase] Extracted implementation details:', details);
    
    // Check if implementation was found
    if (details && !details.notFound && details.code) {
      setCurrentImplementation(prev => ({
        ...prev,
        [language]: {
          ...(prev[language] || {}),
          [component]: details.code
        }
      }));
      
      // Cache the implementation
      const userId = extractUserIdFromToken(authToken);
      if (userId) {
        cacheCodebaseData(userId, `${requestId}_${language}_${component}`, details.code);
      }
    } else {
      console.log(`⚠️ [CodeBase] Implementation details API returned notFound for ${language}/${component}`);
      
      // Try to get from allImplementations cache again (in case it was loaded after we checked)
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
        // Set default message
        setCurrentImplementation(prev => ({
          ...prev,
          [language]: {
            ...prev[language],
            [component]: `// No implementation available for ${component} in ${language}\n// Please generate implementation first.`
          }
        }));
      }
    }
    
  } catch (error) {
    console.error('❌ [CodeBase] Error loading implementation details:', error);
    
    // Try to get from cache or allImplementations
    const userId = extractUserIdFromToken(authToken);
    const cachedCode = userId ? getCachedCodebaseData(userId, `${requestId}_${language}_${component}`) : null;
    const allImpl = allImplementations[language]?.[component];
    
    if (cachedCode) {
      setCurrentImplementation(prev => ({
        ...prev,
        [language]: {
          ...prev[language],
          [component]: cachedCode
        }
      }));
      console.log(`📦 [CodeBase] Using cached implementation for ${language}/${component}`);
    } else if (allImpl) {
      setCurrentImplementation(prev => ({
        ...prev,
        [language]: {
          ...prev[language],
          [component]: allImpl
        }
      }));
      console.log(`📦 [CodeBase] Using allImplementations for ${language}/${component}`);
    }
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
    
    console.log('📊 [CodeBase] Extracted all implementations data:', allImplData);
    
    if (allImplData && allImplData.implementations) {
      const transformedImplementations = {};
      
      Object.entries(allImplData.implementations).forEach(([language, implData]) => {
        console.log(`🔄 [CodeBase] Processing language: ${language}`, implData);
        transformedImplementations[language] = {};
        
        if (typeof implData === 'string') {
          // Single string implementation - treat as controller
          transformedImplementations[language]['controller'] = implData;
        } 
        else if (typeof implData === 'object' && implData !== null) {
          Object.entries(implData).forEach(([componentKey, code]) => {
            // PRESERVE EXISTING BEHAVIOR: If it's already a standard component name, keep it
            const standardComponents = ['controller', 'service', 'repository', 'model', 'dto', 
                                        'routes', 'config', 'handler', 'schemas', 'services'];
            
            let standardComponent = componentKey;
            
            // ONLY map if it's NOT already a standard component name
            // AND if it contains "api_" (which indicates it's from an updated endpoint)
            if (!standardComponents.includes(componentKey.toLowerCase()) && componentKey.includes('api_')) {
              // This is an updated endpoint with generated component name
              // Map it to "controller" since it's likely the main implementation
              standardComponent = 'controller';
              console.log(`📝 [CodeBase] Updated endpoint detected: mapping "${componentKey}" -> "controller" for ${language}`);
            } 
            // Keep existing component names as-is (for freshly created endpoints)
            else {
              console.log(`✅ [CodeBase] Keeping original component name: ${componentKey} for ${language}`);
            }
            
            transformedImplementations[language][standardComponent] = code;
          });
        }
      });
      
      console.log('✅ [CodeBase] Final implementations:', 
        Object.keys(transformedImplementations).map(lang => ({
          language: lang,
          components: Object.keys(transformedImplementations[lang])
        }))
      );
      
      setAllImplementations(transformedImplementations);
      
      // Update current implementation if we have data for selected language
      if (selectedLanguage && transformedImplementations[selectedLanguage]) {
        setCurrentImplementation(prev => ({
          ...prev,
          [selectedLanguage]: transformedImplementations[selectedLanguage]
        }));
        
        // Check if we have controller implementation (either from mapping or original)
        if (transformedImplementations[selectedLanguage]['controller']) {
          console.log(`✅ [CodeBase] Controller implementation available for ${selectedLanguage}`);
        } else {
          // If no controller, use the first available component
          const firstComponent = Object.keys(transformedImplementations[selectedLanguage])[0];
          if (firstComponent) {
            console.log(`⚠️ [CodeBase] No controller, using ${firstComponent} instead`);
            // Optionally map the first component to controller for display
            setCurrentImplementation(prev => ({
              ...prev,
              [selectedLanguage]: {
                ...prev[selectedLanguage],
                controller: transformedImplementations[selectedLanguage][firstComponent]
              }
            }));
          }
        }
      }
    }
    
  } catch (error) {
    console.error('❌ [CodeBase] Error loading all implementations:', error);
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

  // Add this helper function for default languages
const getDefaultSupportedProgrammingLanguages = () => {
  return [
    { value: 'java', label: 'Java', framework: 'Spring Boot', color: '#f89820' },
    { value: 'javascript', label: 'JavaScript', framework: 'Node.js/Express', color: '#f7df1e' },
    { value: 'python', label: 'Python', framework: 'FastAPI/Flask', color: '#3572A5' },
    { value: 'csharp', label: 'C#', framework: '.NET Core', color: '#178600' },
    { value: 'php', label: 'PHP', framework: 'Laravel/Symfony', color: '#4F5D95' },
    { value: 'go', label: 'Go', framework: 'Gin/Echo', color: '#00ADD8' },
    { value: 'ruby', label: 'Ruby', framework: 'Rails/Sinatra', color: '#701516' },
    { value: 'kotlin', label: 'Kotlin', framework: 'Spring Boot', color: '#B125EA' },
    { value: 'swift', label: 'Swift', framework: 'Vapor', color: '#F05138' },
    { value: 'rust', label: 'Rust', framework: 'Rocket/Actix', color: '#dea584' }
  ];
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
        
        // Sort languages alphabetically by name
        const sortedLanguages = sortAlphabetically(formattedLanguages, 'name');
        setAvailableLanguages(sortedLanguages);
        console.log('📊 [CodeBase] Loaded and sorted languages:', sortedLanguages.length);
        
        // Set default language if not set
        if (!selectedLanguage && sortedLanguages.length > 0) {
          setSelectedLanguage(sortedLanguages[0].id);
        }
      } else {
        // Fallback to default languages
        const defaultLanguages = getDefaultSupportedProgrammingLanguages();
        const sortedDefaultLanguages = sortAlphabetically(defaultLanguages, 'label');
        setAvailableLanguages(sortedDefaultLanguages);
        if (!selectedLanguage && sortedDefaultLanguages.length > 0) {
          setSelectedLanguage(sortedDefaultLanguages[0].value);
        }
      }
      
    } catch (error) {
      console.error('❌ [CodeBase] Error loading languages:', error);
      // Fallback to default languages
      const defaultLanguages = getDefaultSupportedProgrammingLanguages();
      const sortedDefaultLanguages = sortAlphabetically(defaultLanguages, 'label');
      setAvailableLanguages(sortedDefaultLanguages);
      if (!selectedLanguage && sortedDefaultLanguages.length > 0) {
        setSelectedLanguage(sortedDefaultLanguages[0].value);
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
  // CRITICAL: During initial load, do NOTHING at all
  // This prevents any folder expansion state changes
  if (isFirstLoad.current) {
    console.log(`⏭️ [CodeBase] Initial load in progress, completely skipping toggleFolder for ${folderId}`);
    return; // Return immediately, don't even toggle the UI
  }
  
  const isExpanding = !expandedFolders.includes(folderId);
  const isFirstFolder = selectedCollection?.folders[0]?.id === folderId;
  
  setExpandedFolders(prev =>
    prev.includes(folderId)
      ? prev.filter(id => id !== folderId)
      : [...prev, folderId]
  );
  
  // If expanding and we have a selected collection, load folder requests
  if (isExpanding && selectedCollection) {
    // Skip fetching during first load for the first folder
    if (isFirstLoad.current && isFirstFolder) {
      console.log(`⏭️ [CodeBase] Skipping fetch for first folder during first load - already fetched by auto-expand`);
      return;
    }
    
    console.log(`📁 [CodeBase] Expanding folder ${folderId}, loading requests...`);
    try {
      await fetchFolderRequests(selectedCollection.id, folderId);
    } catch (error) {
      console.error(`Error loading folder ${folderId} requests:`, error);
    }
  }
};

  // Add this state to track the current request ID
const [currentRequestId, setCurrentRequestId] = useState(null);

// Update handleSelectRequest to set the current request ID
const handleSelectRequest = async (request, collection, folder) => {
  console.log('🎯 [CodeBase] Selecting request:', request.name);
  
  // Set the current request ID FIRST - this will clear the implementation
  setCurrentRequestId(request.id);
  setSelectedRequest(request);
  setSelectedCollection(collection);
  setSelectedComponent('controller');
  
  // Clear current implementation for this language/component when switching requests
  setCurrentImplementation(prev => ({
    ...prev,
    [selectedLanguage]: {
      ...(prev[selectedLanguage] || {}),
      [selectedComponent]: null // Clear it
    }
  }));
  
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
  let componentsFromImpl = Object.keys(implementation);
  
  // If we have components from implementation, use them
  if (componentsFromImpl.length > 0) {
    console.log(`📦 [CodeBase] Available components from implementation:`, componentsFromImpl);
    return componentsFromImpl;
  }
  
  // Otherwise, use supported components for the language
  const supportedComponents = getSupportedComponents(selectedLanguage);
  
  if (supportedComponents && supportedComponents.length > 0) {
    return supportedComponents.map(comp => comp.value);
  }
  
  // Fallback components
  return ['controller', 'service', 'model'];
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

  // Get requests for a specific folder - now returns sorted requests
  const getFolderRequests = (folderId) => {
    const requests = folderRequests[folderId] || [];
    // Ensure requests are sorted (just in case)
    return sortAlphabetically(requests, 'name');
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

  // DEBUG - Monitor currentImplementation changes
useEffect(() => {
  if (selectedLanguage && selectedComponent) {
    const code = currentImplementation[selectedLanguage]?.[selectedComponent];
    if (code) {
      console.log('📝 [CodeBase] Current implementation code:', {
        language: selectedLanguage,
        component: selectedComponent,
        length: code.length,
        preview: code.substring(0, 100) + '...'
      });
    }
  }
}, [currentImplementation, selectedLanguage, selectedComponent]);

  
// Initialize data - updated to load all collection details
useEffect(() => {
  console.log('🚀 [CodeBase] Component mounted, fetching data...');
  
  if (authToken) {
    const extractedUserId = extractUserIdFromToken(authToken);
    setUserId(extractedUserId);
    
    // Clear cache to force fresh API call
    clearCachedCodebaseData(extractedUserId);
    
    // Wrap initial data fetch with global loading
    withGlobalLoading(async () => {
      await fetchCollectionsList();
      // fetchCollectionsList will now call loadAllCollectionDetails internally
      await fetchLanguages();
    });
    
  } else {
    console.log('🔒 [CodeBase] No auth token, skipping fetch');
  }
}, [authToken, fetchCollectionsList, fetchLanguages]);


// Add this ref near your other refs
const autoExpandTriggered = useRef(false);

// ==================== AUTO-EXPAND ONLY FIRST FOLDER & SELECT FIRST ENDPOINT ====================
useEffect(() => {
  // Only run on first load and if not already triggered
  if (!isFirstLoad.current || autoExpandTriggered.current) {
    return;
  }
  
  // Need selected collection
  if (!selectedCollection) {
    console.log('⏳ [CodeBase] Waiting for selectedCollection...');
    return;
  }
  
  // Need folders to be loaded
  if (!selectedCollection.folders || selectedCollection.folders.length === 0) {
    console.log('⏳ [CodeBase] Waiting for folders to load...');
    return;
  }
  
  // Mark as triggered immediately to prevent multiple calls
  autoExpandTriggered.current = true;
  
  console.log('🔥🔥🔥 [CodeBase] AUTO-EXPAND TRIGGERED - FIRST FOLDER ONLY', {
    collection: selectedCollection.name,
    firstFolder: selectedCollection.folders[0]?.name
  });
  
  const autoExpandAndSelect = async () => {
    // ONLY expand the first folder
    const firstFolder = selectedCollection.folders[0];
    
    if (!firstFolder) {
      console.log('❌ [CodeBase] No first folder found');
      return;
    }
    
    console.log(`📁 [CodeBase] Auto-expanding first folder: ${firstFolder.id} - ${firstFolder.name}`);
    
    // 1. Clear any existing expanded folders and ONLY expand the first one
    setExpandedFolders([firstFolder.id]);
    
    // 2. Fetch requests ONLY for the first folder
    console.log(`📡 [CodeBase] Auto-fetching requests for first folder: ${firstFolder.id}`);
    const requests = await fetchFolderRequests(selectedCollection.id, firstFolder.id);
    console.log(`📊 [CodeBase] Auto-fetch complete - loaded ${requests?.length || 0} requests`);
    
    // 3. Select the first request if available
    if (requests && requests.length > 0 && !selectedRequest) {
      const firstRequest = requests[0];
      console.log(`🎯 [CodeBase] Auto-selecting first request: ${firstRequest.name}`);
      
      setSelectedRequest(firstRequest);
      setSelectedComponent('controller');
      
      await fetchRequestDetails(selectedCollection.id, firstRequest.id);
      await fetchAllImplementations(selectedCollection.id, firstRequest.id);
      
      showToast(`Viewing implementation for ${firstRequest.name}`, 'info');
    }
    
    // Mark that first load is complete
    isFirstLoad.current = false;
    console.log('✅ [CodeBase] Auto-expand completed - only first folder expanded');
  };
  
  autoExpandAndSelect();
  
}, [
  selectedCollection, 
  selectedCollection?.folders,
  selectedCollection?.folders?.length,
  selectedRequest,
  fetchFolderRequests,
  fetchRequestDetails,
  fetchAllImplementations,
  showToast
]);

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
        </div>
      </div>
    );
  };

  // Add this function to your CodeBase component to regenerate implementation with all headers
const regenerateImplementationWithAllHeaders = async () => {
  if (!selectedCollection || !selectedRequest) {
    showToast('Please select a request first', 'warning');
    return;
  }

  setIsGeneratingCode(true);
  
  try {
    // Create a generate request that includes all headers
    const generateRequest = {
      requestId: selectedRequest.id,
      collectionId: selectedCollection.id,
      language: selectedLanguage,
      components: ['controller', 'service', 'model'],
      includeAllHeaders: true, // Add this flag to tell backend to include all headers
      headers: selectedRequest.headers // Pass the actual headers from the request
    };
    
    const results = await generateImplementationAPI(generateRequest);
    
    if (results?.success) {
      showToast('Implementation regenerated with all headers!', 'success');
      // Reload implementation details
      await fetchImplementationDetails(
        selectedCollection.id, 
        selectedRequest.id, 
        selectedLanguage, 
        selectedComponent
      );
      
      // Also reload all implementations to update the cache
      await fetchAllImplementations(selectedCollection.id, selectedRequest.id);
    } else {
      showToast('Failed to regenerate implementation', 'error');
    }
  } catch (error) {
    console.error('Error regenerating implementation:', error);
    showToast(error.message || 'Failed to regenerate implementation', 'error');
  } finally {
    setIsGeneratingCode(false);
  }
};

// Also update the renderImplementationContent function to show headers
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
              <h1 className="text-2xl font-semibold mb-4" style={{ color: colors.text }}>
                {selectedRequest.name}
              </h1>
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
              
              <div className="flex flex-wrap items-center gap-4 text-sm mb-4 mt-4">
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
              
              {/* <p className="text-base mb-4 mt-4" style={{ color: colors.textSecondary }}>
                {selectedRequest.description || 'No description available'}
              </p> */}
              
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

          {/* Display Headers */}
              {selectedRequest.headers && selectedRequest.headers.length > 0 && (
                <div className="mb-6 p-4 rounded-lg" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
                  <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Request Headers</h3>
                  <div className="space-y-2">
                    {selectedRequest.headers.map((header, index) => (
                      <div key={index} className="flex items-start gap-2 text-sm">
                        <span className="font-medium" style={{ color: colors.textSecondary, minWidth: '120px' }}>
                          {header.key}:
                        </span>
                        <span style={{ color: colors.text }}>
                          {header.value}
                          {header.disabled && (
                            <span className="ml-2 text-xs px-1.5 py-0.5 rounded" style={{ backgroundColor: colors.error + '20', color: colors.error }}>
                              Disabled
                            </span>
                          )}
                          {header.required && (
                            <span className="ml-2 text-xs px-1.5 py-0.5 rounded" style={{ backgroundColor: colors.primary + '20', color: colors.primary }}>
                              Required
                            </span>
                          )}
                        </span>
                        {header.description && (
                          <span className="text-xs" style={{ color: colors.textTertiary }}>
                            - {header.description}
                          </span>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}
              
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
                    {/* Add regenerate button */}
                    <button
                      onClick={regenerateImplementationWithAllHeaders}
                      disabled={isGeneratingCode}
                      className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift"
                      style={{ backgroundColor: colors.hover, color: colors.text }}
                    >
                      {isGeneratingCode ? (
                        <RefreshCw size={12} className="animate-spin" />
                      ) : (
                        <RefreshCw size={12} />
                      )}
                      Regenerate
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
                        components: ['controller', 'service', 'model'],
                        includeAllHeaders: true,
                        headers: selectedRequest.headers
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

      {/* Loading Overlay */}
      <LoadingOverlay />

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
                await withGlobalLoading(async () => {
                  await fetchCollectionsList();
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
                {/* Add Refresh Button */}
                <button 
                  className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  onClick={async () => {
                    try {
                      await withGlobalLoading(async () => {
                        await fetchCollectionsList();
                      });
                      showToast('Collections refreshed', 'success');
                    } catch (error) {
                      showToast('Failed to refresh collections', 'error');
                    }
                  }}
                  disabled={isLoading.collections}
                  style={{ backgroundColor: colors.hover }}
                >
                  <RefreshCw 
                    size={12} 
                    style={{ 
                      color: colors.textSecondary,
                      animation: isLoading.collections ? 'spin 1s linear infinite' : 'none'
                    }} 
                  />
                </button>
                
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
            await withGlobalLoading(async () => {
              await fetchCollectionsList();
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
        // Calculate total endpoints in this collection
        const totalEndpoints = collection.folders?.reduce((sum, folder) => 
          sum + (folder.requestCount || 0), 0) || 0;
        
        return (
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
              
              {/* Collection count badge - show total endpoints in collection */}
              {totalEndpoints > 0 && (
                <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                  backgroundColor: colors.primaryDark,
                  color: 'white'
                }}>
                  {totalEndpoints}
                </span>
              )}
              
              {/* {collection.version && (
                <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                  backgroundColor: colors.hover,
                  color: colors.textSecondary
                }}>
                  {collection.version}
                </span>
              )} */}
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
                      
                      {/* Folder count badge - show number of endpoints in this folder */}
                      {folder.requestCount > 0 && (
                        <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                          backgroundColor: colors.primaryDark,
                          color: 'white'
                        }}>
                          {folder.requestCount}
                        </span>
                      )}

                      {/* If still loading, show a subtle spinner */}
                      {folder.requestCount === null && isLoading.folderRequests[folder.id] && (
                        <RefreshCw size={10} className="animate-spin" style={{ color: colors.textTertiary }} />
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
                                {folder.hasRequests === false && (
                                  <button 
                                    className="mt-1 px-2 py-1 text-xs rounded hover:bg-opacity-50 transition-colors hover-lift"
                                    onClick={() => fetchFolderRequests(collection.id, folder.id)}
                                    style={{ backgroundColor: colors.hover, color: colors.text }}>
                                    <RefreshCw size={10} className="inline mr-1" />
                                    Retry
                                  </button>
                                )}
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
            
            {/* Show "No folders" message when collection is expanded but has no folders */}
            {expandedCollections.includes(collection.id) && (!collection.folders || collection.folders.length === 0) && (
              <div className="ml-4 py-2 text-center">
                <p className="text-xs" style={{ color: colors.textTertiary }}>No folders in this collection</p>
              </div>
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