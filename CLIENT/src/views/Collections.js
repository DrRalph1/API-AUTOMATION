import React, { useState, useEffect, useRef, useCallback, useMemo } from 'react';
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
  Loader,
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

// Import CollectionsController
import {
  getCollectionsList,
  getCollectionDetails,
  getRequestDetails,
  saveRequest,
  createCollection,
  generateCodeSnippet,
  getEnvironments,
  importCollection,
  executeRequest,
  handleCollectionsResponse,
  extractCollectionsList,
  extractCollectionDetails,
  extractRequestDetails,
  extractSaveRequestResults,
  extractCreateCollectionResults,
  extractCodeSnippetResults,
  extractEnvironments,
  extractImportResults,
  extractExecuteResults,
  validateSaveRequest,
  validateCreateCollection,
  validateCodeSnippetRequest,
  validateImportRequest,
  validateExecuteRequest,
  formatCollection,
  formatRequest
} from "../controllers/CollectionsController.js";

// Syntax highlighter component
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

// Code Panel Component - Extracted to prevent re-renders
const CodePanel = React.memo(({ 
  selectedLanguage, 
  setSelectedLanguage, 
  showLanguageDropdown, 
  setShowLanguageDropdown,
  requestMethod,
  requestUrl,
  requestHeaders,
  requestBody,
  authToken,
  loading,
  setLoading,
  showToast,
  colors,
  setShowCodePanel,
  selectedRequest // Add this prop
}) => {
  const [codeSnippet, setCodeSnippet] = useState('');
  const abortControllerRef = useRef(null);
  const isMounted = useRef(true);
  const lastGeneratedUrlRef = useRef('');
  const activeRequestIdRef = useRef(null); // <-- ADD THIS REF HERE
  
  console.log('🎨 CodePanel rendered with language:', selectedLanguage, 'and request:', selectedRequest?.id);

  const languages = [
    { id: 'curl', name: 'cURL', icon: <Terminal size={14} /> },
    { id: 'javascript', name: 'JavaScript', icon: <FileCode size={14} /> },
    { id: 'python', name: 'Python', icon: <Code size={14} /> },
    { id: 'nodejs', name: 'Node.js', icon: <Server size={14} /> },
    { id: 'php', name: 'PHP', icon: <Box size={14} /> },
    { id: 'ruby', name: 'Ruby', icon: <Package size={14} /> },
    { id: 'java', name: 'Java', icon: <Coffee size={14} /> }
  ];

  const currentLanguage = languages.find(lang => lang.id === selectedLanguage);

  // Add mount/unmount handling
  useEffect(() => {
    isMounted.current = true;
    return () => {
      console.log('💀 CodePanel UNMOUNTED');
      isMounted.current = false;
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, []);

  // SINGLE SOURCE OF TRUTH for snippet generation
  useEffect(() => {
    // Create a stable reference to the current URL to prevent race conditions
    const currentUrl = requestUrl;
    const currentLanguage = selectedLanguage;
    const currentRequestId = selectedRequest?.id;
    
    // Clear snippet if no URL
    if (!currentUrl) {
      setCodeSnippet('');
      return;
    }
    
    // Only generate if this is a new URL (not the same as last generated)
    const urlKey = `${currentRequestId}-${currentLanguage}-${currentUrl}`;
    
    if (urlKey !== lastGeneratedUrlRef.current) {
      console.log('🎯 CodePanel: New URL detected, generating snippet', { 
        urlKey, 
        lastKey: lastGeneratedUrlRef.current 
      });
      
      lastGeneratedUrlRef.current = urlKey;
      generateSnippet();
    }
  }, [selectedLanguage, requestUrl, selectedRequest?.id]); // Remove generateSnippet from deps

  // Generate code snippet when request changes OR language changes
  const generateSnippet = useCallback(async () => {
    // Cancel any ongoing request
    if (abortControllerRef.current) {
      console.log('🛑 Cancelling previous snippet request');
      abortControllerRef.current.abort();
    }

    // Create new abort controller
    abortControllerRef.current = new AbortController();

    const requestId = Date.now();
    const currentRequestId = selectedRequest?.id;
    const currentUrl = requestUrl;
    
    // Set the active request ID
    activeRequestIdRef.current = currentRequestId; // <-- SET IT HERE
    
    console.log(`📝 [CodePanel] Generating snippet for request ${requestId}`, {
      language: selectedLanguage,
      method: requestMethod,
      url: currentUrl,
      requestId: currentRequestId
    });

    if (!currentUrl) {
      if (isMounted.current) {
        setCodeSnippet('// Enter a URL to generate code snippet');
      }
      return;
    }

    if (!authToken) {
      if (isMounted.current) {
        setCodeSnippet('// Authentication required. Please login.');
      }
      return;
    }

    setLoading(prev => ({ ...prev, generateSnippet: true }));

    try {
      // SAFETY FIX: Ensure requestHeaders exists and is an array
      const safeHeaders = Array.isArray(requestHeaders) ? requestHeaders : [];
      
      const snippetRequest = {
        language: selectedLanguage,
        method: requestMethod,
        url: currentUrl,
        headers: safeHeaders
          .filter(h => h && h.enabled)
          .map(h => ({
            key: h?.key || '',
            value: h?.value || '',
            enabled: true
          })),
        body: requestBody || ''
      };

      console.log(`📡 [CodePanel] Making API call for request ${requestId}`);
      const response = await generateCodeSnippet(authToken, snippetRequest);
      
      // Check if component is still mounted AND this request is still the active one
      // <-- ADD THIS CHECK HERE
      if (!isMounted.current || activeRequestIdRef.current !== selectedRequest?.id || requestUrl !== currentUrl) {
        console.log(`⏭️ Response for ${requestId} ignored - request changed`);
        return;
      }

      const processedResponse = handleCollectionsResponse(response);
      const snippetResults = extractCodeSnippetResults(processedResponse);
      
      console.log(`✅ [CodePanel] Snippet loaded for request ${requestId}`);
      
      if (isMounted.current) {
        if (snippetResults && snippetResults.code) {
          setCodeSnippet(snippetResults.code);
        } else {
          setCodeSnippet('// Unable to generate code snippet');
        }
      }
    } catch (error) {
      // Don't show error for aborted requests
      if (error.name === 'AbortError' || error.message === 'canceled') {
        console.log('Snippet request cancelled');
        return;
      }
      
      console.error('Error loading code snippet:', error);
      if (isMounted.current) {
        setCodeSnippet(`// Error loading code snippet: ${error.message}`);
        showToast(`Failed to generate snippet: ${error.message}`, 'error');
      }
    } finally {
      if (isMounted.current) {
        setLoading(prev => ({ ...prev, generateSnippet: false }));
      }
    }
  }, [selectedLanguage, requestMethod, requestUrl, requestHeaders, requestBody, authToken, showToast, setLoading, selectedRequest?.id]);

  // Get placeholder text based on language
  const getPlaceholderSnippet = () => {
    const placeholders = {
      curl: `# Example cURL command
curl -X GET "https://api.example.com/users" \\
  -H "Content-Type: application/json"`,
      javascript: `// Example JavaScript (Fetch API)
fetch('https://api.example.com/users', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));`,
      python: `# Example Python (requests)
import requests

response = requests.get(
    'https://api.example.com/users',
    headers={'Content-Type': 'application/json'}
)
print(response.json())`,
      nodejs: `// Example Node.js (axios)
const axios = require('axios');

axios.get('https://api.example.com/users', {
  headers: {
    'Content-Type': 'application/json'
  }
})
.then(response => console.log(response.data))
.catch(error => console.error(error));`,
      php: `<?php
// Example PHP (cURL)
$ch = curl_init('https://api.example.com/users');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Content-Type: application/json'
]);
$response = curl_exec($ch);
curl_close($ch);
echo $response;
?>`,
      ruby: `# Example Ruby (net/http)
require 'net/http'
require 'json'

uri = URI('https://api.example.com/users')
response = Net::HTTP.get(uri)
puts JSON.parse(response)`,
      java: `// Example Java (OkHttp)
OkHttpClient client = new OkHttpClient();

Request request = new Request.Builder()
    .url("https://api.example.com/users")
    .addHeader("Content-Type", "application/json")
    .build();

try (Response response = client.newCall(request).execute()) {
    System.out.println(response.body().string());
}`
    };
    return placeholders[selectedLanguage] || placeholders.curl;
  };

  return (
    <div className="w-80 border-l flex flex-col" style={{ 
      backgroundColor: colors.sidebar,
      borderColor: colors.border
    }}>
      {/* ... rest of your JSX remains exactly the same ... */}
      <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
        <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Code</h3>
        <button type="button" onClick={() => setShowCodePanel(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
          style={{ backgroundColor: colors.hover }}>
          <X size={14} style={{ color: colors.textSecondary }} />
        </button>
      </div>

      <div className="relative px-4 py-3 border-b space-y-2" style={{ borderColor: colors.border }}>
        <button
          type="button"
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

        {/* Generate Button */}
        <button
          type="button"
          onClick={generateSnippet}
          disabled={!requestUrl || loading.generateSnippet}
          className="w-full px-3 py-2 rounded text-sm font-medium flex items-center justify-center gap-2 hover:opacity-90 transition-colors hover-lift"
          style={{ 
            backgroundColor: colors.primaryDark, 
            color: colors.white,
            opacity: (!requestUrl || loading.generateSnippet) ? 0.5 : 1
          }}
        >
          {loading.generateSnippet ? (
            <>
              <RefreshCw size={12} className="animate-spin" />
              Generating...
            </>
          ) : (
            <>
              <Zap size={12} />
              Generate Code
            </>
          )}
        </button>

        {showLanguageDropdown && (
          <>
            <div className="fixed inset-0 z-40" onClick={() => setShowLanguageDropdown(false)} />
            <div className="absolute left-4 right-4 top-full mt-1 py-2 rounded shadow-lg z-50 border"
              style={{ 
                backgroundColor: colors.bg,
                borderColor: colors.border,
                maxHeight: '300px',
                overflowY: 'auto'
              }}>
              {languages.map(lang => (
                <button
                  key={lang.id}
                  type="button"
                  onClick={() => {
                    console.log(`🔤 Language changed to: ${lang.id}`);
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
          </>
        )}
      </div>

      <div className="flex-1 overflow-auto p-4">
        {loading.generateSnippet ? (
          <div className="flex items-center justify-center h-full">
            <RefreshCw className="animate-spin" size={16} style={{ color: colors.textSecondary }} />
          </div>
        ) : (
          <>
            {codeSnippet && !codeSnippet.includes('Enter a URL') && !codeSnippet.includes('Authentication required') ? (
              <SyntaxHighlighter 
                language={selectedLanguage === 'curl' ? 'bash' : selectedLanguage}
                code={codeSnippet}
              />
            ) : (
              <div className="text-center py-8">
                <Code size={32} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mx-auto mb-4" />
                <p className="text-sm mb-2" style={{ color: colors.text }}>No code generated yet</p>
                <p className="text-xs" style={{ color: colors.textSecondary }}>
                  {requestUrl 
                    ? 'Click "Generate Code" to create a snippet' 
                    : 'Enter a URL and click Generate'}
                </p>
              </div>
            )}
          </>
        )}
      </div>

      <div className="p-4 border-t space-y-2" style={{ borderColor: colors.border }}>
        {codeSnippet && !codeSnippet.includes('Enter a URL') && !codeSnippet.includes('Authentication required') && (
          <button 
            type="button"
            className="w-full py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center justify-center gap-2 hover-lift"
            onClick={() => {
              navigator.clipboard.writeText(codeSnippet);
              showToast('Copied to clipboard!', 'success');
            }}
            disabled={loading.generateSnippet}
            style={{ backgroundColor: colors.primaryDark, color: colors.white, opacity: loading.generateSnippet ? 0.5 : 1 }}>
            <Copy size={12} />
            Copy to Clipboard
          </button>
        )}
        
        {/* Show a nice default template when no code is generated */}
        {(!codeSnippet || codeSnippet.includes('Enter a URL') || codeSnippet.includes('Authentication required')) && !loading.generateSnippet && (
          <div className="text-xs p-3 rounded" style={{ backgroundColor: colors.codeBg, color: colors.textSecondary }}>
            <pre className="whitespace-pre-wrap font-mono">
              {getPlaceholderSnippet()}
            </pre>
          </div>
        )}
      </div>
    </div>
  );
}, (prevProps, nextProps) => {
  // Custom comparison - only re-render when these change
  return (
    prevProps.selectedLanguage === nextProps.selectedLanguage &&
    prevProps.showLanguageDropdown === nextProps.showLanguageDropdown &&
    prevProps.requestMethod === nextProps.requestMethod &&
    prevProps.requestUrl === nextProps.requestUrl &&
    prevProps.authToken === nextProps.authToken &&
    prevProps.selectedRequest?.id === nextProps.selectedRequest?.id &&
    prevProps.colors === nextProps.colors &&
    JSON.stringify(prevProps.requestHeaders) === JSON.stringify(nextProps.requestHeaders) &&
    prevProps.requestBody === nextProps.requestBody
  );
});

const Collections = ({ theme, isDark, customTheme, toggleTheme, authToken }) => {
  // Add refs to track mount state and previous authToken
  const isMounted = useRef(true);
  const prevAuthTokenRef = useRef(authToken);
  const initialDataLoaded = useRef(false);
  const fetchInProgressRef = useRef(false);
  const lastRebuiltUrlRef = useRef('');

  // Add these memoized callbacks near the top of the Collections component
  const memoizedSetSelectedLanguage = useCallback((lang) => {
    setSelectedLanguage(lang);
  }, []);

  const memoizedSetShowLanguageDropdown = useCallback((show) => {
    setShowLanguageDropdown(show);
  }, []);

  const memoizedSetLoading = useCallback((updater) => {
    setLoading(updater);
  }, []);

  const memoizedShowToast = useCallback((message, type) => {
    showToast(message, type);
  }, []);

  const memoizedSetShowCodePanel = useCallback((show) => {
    setShowCodePanel(show);
  }, []);

  // Also memoize the requestHeaders to prevent unnecessary re-renders
  const memoizedRequestHeaders = useMemo(() => requestHeaders, [JSON.stringify(requestHeaders)]);

  // Debugging refs
  useEffect(() => {
    console.log('🔥 Collections MOUNTED');
    isMounted.current = true;
    return () => {
      console.log('💀 Collections UNMOUNTED');
      isMounted.current = false;
    };
  }, []);

  useEffect(() => {
    console.log('📝 authToken changed:', authToken ? 'present' : 'missing');
  }, [authToken]);

  // Matching the exact color scheme from the second component
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
    inputborder: 'rgb(51 65 85 / 19%)',
    tableHeader: 'rgb(41 53 72 / 19%)',
    tableRow: 'rgb(41 53 72 / 19%)',
    tableRowHover: 'rgb(45 46 72 / 33%)',
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
    connectionOnline: '#10b981',
    connectionOffline: '#ef4444',
    connectionIdle: '#f59e0b',
    accentPurple: '#8b5cf6',
    accentPink: '#ec4899',
    accentCyan: '#06b6d4',
    gradient: 'from-blue-400/20 via-violet-400/20 to-orange-400/20'
  };

  // State
  const [collections, setCollections] = useState([]);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [activeTab, setActiveTab] = useState('params');
  const [requestTabs, setRequestTabs] = useState([]);
  
  // Right panel states
  const [showCodePanel, setShowCodePanel] = useState(true);
  const [showMockServers, setShowMockServers] = useState(false);
  const [showMonitors, setShowMonitors] = useState(false);
  const [showEnvironments, setShowEnvironments] = useState(false);
  const [showAPIs, setShowAPIs] = useState(false);
  
  // Code panel state
  const [selectedLanguage, setSelectedLanguage] = useState('java');
  const [showLanguageDropdown, setShowLanguageDropdown] = useState(false);
  
  const [environments, setEnvironments] = useState([]);
  const [activeEnvironment, setActiveEnvironment] = useState(null);
  
  const [requestMethod, setRequestMethod] = useState('GET');
  const [requestUrl, setRequestUrl] = useState('');
  const [requestParams, setRequestParams] = useState([]);
  const [requestHeaders, setRequestHeaders] = useState([]);
  const [requestBody, setRequestBody] = useState('');
  const [requestBodyType, setRequestBodyType] = useState('raw');
  const [rawBodyType, setRawBodyType] = useState('json');
  const [formData, setFormData] = useState([]);
  const [urlEncodedData, setUrlEncodedData] = useState([]);
  const [binaryFile, setBinaryFile] = useState(null);
  const [graphqlQuery, setGraphqlQuery] = useState('');
  const [graphqlVariables, setGraphqlVariables] = useState('');
  const [response, setResponse] = useState(null);
  const [isSending, setIsSending] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showImportModal, setShowImportModal] = useState(false);
  const [showShareModal, setShowShareModal] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [authType, setAuthType] = useState('noauth');
  const [showAuthDropdown, setShowAuthDropdown] = useState(false);
  const [authConfig, setAuthConfig] = useState({ 
    type: 'noauth',
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

  // UI states
  const [showPassword, setShowPassword] = useState(false);
  const [showToken, setShowToken] = useState(false);
  const [toast, setToast] = useState(null);
  const [loading, setLoading] = useState({
    initialLoad: false,
    collections: false,
    environments: false,
    request: false,
    execute: false,
    save: false,
    create: false,
    import: false,
    generateSnippet: false
  });
  const [error, setError] = useState(null);

  const [newCollectionName, setNewCollectionName] = useState('');
  const [newCollectionDescription, setNewCollectionDescription] = useState('');

  // Add a new state to store the template URL with placeholders
  const [templateUrl, setTemplateUrl] = useState('');

  // Add this to your state declarations (around line 400)
  const [requestPathParams, setRequestPathParams] = useState([]);

// Add these functions with your other CRUD operations (around line 900)
// Add path param
const addPathParam = () => {
  const newParam = { 
    id: `path-${Date.now()}`, 
    key: '', 
    value: '', 
    description: '', 
    enabled: true,
    required: false
  };
  setRequestPathParams([...requestPathParams, newParam]);
};


// Add these functions with your other CRUD operations (around line 400)
// Add query param
const addQueryParam = () => {
  const newParam = { 
    id: `query-${Date.now()}`, 
    key: '', 
    value: '', 
    description: '', 
    enabled: true 
  };
  setRequestParams([...requestParams, newParam]);
};

// Update query param and URL automatically
const updateQueryParam = (id, field, value) => {
  console.log('🟢 updateQueryParam called:', { id, field, value });
  
  // First update the state with the new value
  setRequestParams(params => {
    console.log('📦 Current query params before update:', params);
    
    const updatedParams = params.map(param => 
      param.id === id ? { ...param, [field]: value } : param
    );
    
    console.log('📦 Updated query params after update:', updatedParams);
    
    // Then update URL with all query params
    if (field === 'value' || field === 'key' || field === 'enabled') {
      console.log('🔍 Field is value/key/enabled, proceeding with URL update');
      
      // Get base URL without query string
      const baseUrl = requestUrl.split('?')[0];
      
      // Build query string from enabled params with keys and values
      const queryParams = updatedParams
        .filter(p => p.enabled && p.key && p.key.trim() !== '')
        .map(p => `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value || '')}`)
        .join('&');
      
      // Construct new URL
      const newUrl = queryParams ? `${baseUrl}?${queryParams}` : baseUrl;
      
      console.log('✅ Final new URL:', newUrl);
      
      // Update URL immediately
      setRequestUrl(newUrl);
    }
    
    return updatedParams;
  });
};

// Delete query param and update URL
const deleteQueryParam = (id) => {
  setRequestParams(params => {
    const remainingParams = params.filter(param => param.id !== id);
    
    // Update URL after deletion
    const baseUrl = requestUrl.split('?')[0];
    const queryParams = remainingParams
      .filter(p => p.enabled && p.key && p.key.trim() !== '')
      .map(p => `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value || '')}`)
      .join('&');
    
    const newUrl = queryParams ? `${baseUrl}?${queryParams}` : baseUrl;
    setRequestUrl(newUrl);
    
    return remainingParams;
  });
};

// Parse URL and update query params
const parseUrlAndUpdateQueryParams = (url) => {
  try {
    const urlObj = new URL(url);
    const params = [];
    
    // Parse query parameters
    urlObj.searchParams.forEach((value, key) => {
      params.push({
        id: `query-${Date.now()}-${Math.random()}`,
        key: key,
        value: value,
        description: '',
        enabled: true
      });
    });
    
    return params;
  } catch (e) {
    // Not a valid URL or no query string
    return [];
  }
};


// Add this function to clean URL-encoded placeholders and remove duplicates
const cleanUrlEncodedPlaceholders = (url) => {
  if (!url) return url;
  
  // Skip if no encoded placeholders and no visible placeholders
  const hasEncoded = url.includes('%7B') || url.includes('%7D');
  const hasVisiblePlaceholders = url.includes('{') && url.includes('}');
  
  if (!hasEncoded && !hasVisiblePlaceholders) {
    return url;
  }
  
  console.log('🧹 Cleaning URL-encoded placeholders from:', url);
  
  // First, decode any URL-encoded curly braces
  let cleanedUrl = url
    .replace(/%7B/g, '{')
    .replace(/%7D/g, '}');
  
  // Now check for duplicate placeholders in the path
  // Split the URL into base and query parts
  const [basePath, queryString] = cleanedUrl.split('?');
  
  // Split the path into segments
  const segments = basePath.split('/');
  const seenPlaceholders = new Map(); // key -> count
  const cleanedSegments = [];
  
  // First pass: count occurrences of each placeholder key
  segments.forEach(segment => {
    if (segment.startsWith('{') && segment.endsWith('}')) {
      const key = segment.substring(1, segment.length - 1);
      seenPlaceholders.set(key, (seenPlaceholders.get(key) || 0) + 1);
    }
  });
  
  // Check if any key appears more than once
  const hasDuplicates = Array.from(seenPlaceholders.values()).some(count => count > 1);
  
  if (hasDuplicates) {
    console.log('⚠️ Found duplicate placeholders, removing extras...');
    
    const processedKeys = new Set();
    
    // Second pass: only keep first occurrence of each placeholder
    segments.forEach(segment => {
      if (segment.startsWith('{') && segment.endsWith('}')) {
        const key = segment.substring(1, segment.length - 1);
        
        // Only keep the first occurrence
        if (!processedKeys.has(key)) {
          cleanedSegments.push(segment);
          processedKeys.add(key);
        } else {
          console.log(`  🗑️ Removing duplicate placeholder: ${segment}`);
          // Skip this segment (remove it)
        }
      } else {
        cleanedSegments.push(segment);
      }
    });
    
    // Rebuild the path
    let newBasePath = cleanedSegments.join('/');
    
    // Remove any double slashes
    newBasePath = newBasePath.replace(/\/+/g, '/');
    
    // Remove trailing slash if it exists (but preserve protocol slashes)
    if (newBasePath.endsWith('/') && !newBasePath.endsWith('://')) {
      newBasePath = newBasePath.slice(0, -1);
    }
    
    // Rebuild the full URL with query string
    cleanedUrl = queryString ? `${newBasePath}?${queryString}` : newBasePath;
    
    console.log('✅ Cleaned URL:', cleanedUrl);
  }
  
  return cleanedUrl;
};


// Replace your parseUrlIntoTemplateAndParams function
const parseUrlIntoTemplateAndParams = useCallback((url, existingPathParams = []) => {
  if (!url) return { templateUrl: '', pathParams: [], queryParams: [], envVarNames: [] };
  
  console.log('🔍 Parsing URL:', url);
  console.log('📋 Existing path params:', existingPathParams);
  
  try {
    // First, decode the URL to handle encoded characters
    let decodedUrl = url;
    try {
      decodedUrl = decodeURIComponent(url);
    } catch (e) {
      // If decoding fails, use original
      decodedUrl = url;
    }
    
    // Parse the URL
    let urlObj;
    let baseUrl = '';
    let path = '';
    let search = '';
    let isEnvVarUrl = false;
    
    try {
      urlObj = new URL(decodedUrl);
      baseUrl = `${urlObj.protocol}//${urlObj.host}`;
      path = urlObj.pathname;
      search = urlObj.search;
    } catch (e) {
      // If it's not a valid full URL, it might be a template with environment variables
      // Try to extract path and query parts
      const [urlPath, queryString] = decodedUrl.split('?');
      path = urlPath;
      search = queryString ? `?${queryString}` : '';
      baseUrl = ''; // No base URL for templates
      
      // Check if this is an environment variable URL (contains {{ }})
      if (decodedUrl.includes('{{') && decodedUrl.includes('}}')) {
        isEnvVarUrl = true;
        console.log('🌍 Detected environment variable URL');
      }
    }

    // CRITICAL FIX: Clean the path of any URL-encoded placeholders
    path = cleanUrlEncodedPlaceholders(path);

    // CRITICAL FIX: Clean URL-encoded placeholders
    const cleanedPath = cleanUrlEncodedPlaceholders(path);
    if (cleanedPath !== path) {
      console.log('🧹 Path cleaned from:', path, 'to:', cleanedPath);
      path = cleanedPath;
    }
    
    // Extract path segments
    const pathSegments = path.split('/').filter(segment => segment);
    
    // Detect path parameters (single curly braces) and environment variables (double curly braces)
    const pathParams = [];
    const templateSegments = [];
    const envVarNames = [];
    
    // First pass: Identify all placeholders and their positions
    const placeholderPositions = [];
    
    pathSegments.forEach((segment, index) => {
      // Check for environment variables (double curly braces) first
      if (segment.startsWith('{{') && segment.endsWith('}}')) {
        // This is an environment variable, keep it as-is in the template
        const envVarName = segment.substring(2, segment.length - 2);
        envVarNames.push(envVarName);
        templateSegments.push(segment);
        console.log(`🌍 Found environment variable: ${envVarName}`);
      }
      // Check for path parameters (single curly braces) in decoded form
      else if (segment.startsWith('{') && segment.endsWith('}')) {
        // Make sure it's not actually a double curly brace that we missed
        if (!segment.startsWith('{{')) {
          // It's a path parameter, extract the key
          const key = segment.substring(1, segment.length - 1);
          placeholderPositions.push({ index, key, type: 'placeholder' });
          templateSegments.push(`{${key}}`);
        } else {
          templateSegments.push(segment);
        }
      }
      // Check for URL-encoded path parameters (%7Bkey%7D)
      else if (segment.includes('%7B') && segment.includes('%7D')) {
        // This is an encoded path parameter
        try {
          const decodedSegment = decodeURIComponent(segment);
          if (decodedSegment.startsWith('{') && decodedSegment.endsWith('}')) {
            const key = decodedSegment.substring(1, decodedSegment.length - 1);
            placeholderPositions.push({ index, key, type: 'placeholder' });
            templateSegments.push(`{${key}}`);
          } else {
            templateSegments.push(segment);
          }
        } catch {
          templateSegments.push(segment);
        }
      }
      // Check for partially encoded segments
      else if (segment.includes('{') || segment.includes('}') || segment.includes('%7B') || segment.includes('%7D')) {
        // Try to extract if it contains curly braces in any form
        const openBraceMatch = segment.match(/(.*?)(?:{|%7B)(.*?)(?:}|%7D)(.*)/);
        if (openBraceMatch) {
          const prefix = openBraceMatch[1];
          const key = openBraceMatch[2];
          const suffix = openBraceMatch[3];
          placeholderPositions.push({ index, key, type: 'placeholder' });
          templateSegments.push(prefix + `{${key}}` + suffix);
        } else {
          templateSegments.push(segment);
        }
      }
      else if (segment.startsWith(':')) {
        // Colon-style placeholder
        const key = segment.substring(1);
        placeholderPositions.push({ index, key, type: 'placeholder' });
        templateSegments.push(`{${key}}`);
      }
      else {
        // Regular path segment
        templateSegments.push(segment);
      }
    });
    
    // Second pass: Create path params from placeholder positions
    // CRITICAL: Match with existing path params by position, not just by key
    placeholderPositions.forEach(({ index, key }) => {
      // Try to find an existing param with the same key AND same position?
      // For now, we'll create a new param for each placeholder position
      const existingParam = existingPathParams.find(p => 
        p.key === key && p.position === index
      );
      
      // Also check if any existing param has this key and we can use its value
      const anyExistingWithKey = existingPathParams.find(p => p.key === key);
      
      pathParams.push({
        id: existingParam?.id || anyExistingWithKey?.id || `path-${Date.now()}-${index}-${Math.random()}`,
        key: key,
        value: existingParam?.value || anyExistingWithKey?.value || '',
        description: existingParam?.description || anyExistingWithKey?.description || '',
        enabled: true,
        required: true,
        position: index // Store the position for future reference
      });
    });
    
    // Rebuild template URL
    let templateBase = baseUrl;
    if (templateSegments.length > 0) {
      if (templateBase && !templateBase.endsWith('/')) {
        templateBase += '/';
      }
      templateBase += templateSegments.join('/');
    }
    
    // Parse query parameters
    const queryParams = [];
    if (search) {
      const queryString = search.startsWith('?') ? search.substring(1) : search;
      if (queryString) {
        queryString.split('&').forEach(pair => {
          if (pair) {
            const [key, value] = pair.split('=').map(decodeURIComponent);
            queryParams.push({
              id: `query-${Date.now()}-${key}-${Math.random()}`,
              key: key || '',
              value: value || '',
              description: '',
              enabled: true
            });
          }
        });
      }
    }
    
    console.log('✅ Parsed path params:', pathParams);
    console.log('✅ Parsed query params:', queryParams);
    console.log('✅ Template URL:', templateBase || url);
    console.log('✅ Environment variables found:', envVarNames);
    
    return {
      templateUrl: templateBase || url,
      pathParams: pathParams,
      queryParams: queryParams,
      envVarNames: envVarNames
    };
  } catch (e) {
    console.error('Error parsing URL:', e);
    return { templateUrl: url, pathParams: [], queryParams: [], envVarNames: [] };
  }
}, []);



// Add these refs near your other refs
const isUpdatingFromInput = useRef(false);
const urlInputRef = useRef(null);

const handleUrlChange = useCallback((e) => {
  // Get the current cursor position
  const cursorPos = e.target.selectionStart;
  
  // Set flag that this update is from user input
  isUpdatingFromInput.current = true;
  
  const newUrl = e.target.value;
  
  // Only update if different from current URL
  if (newUrl !== requestUrl) {
    setRequestUrl(newUrl);
    
    // Parse the URL to extract parameters
    const parsed = parseUrlIntoTemplateAndParams(newUrl, requestPathParams);
    
    // Update query params
    if (parsed.queryParams.length > 0) {
      setRequestParams(prevParams => {
        const existingParamsMap = new Map(
          prevParams.filter(p => p.key).map(p => [p.key, p])
        );
        
        const mergedParams = parsed.queryParams.map(parsedParam => {
          const existing = existingParamsMap.get(parsedParam.key);
          if (existing) {
            return {
              ...existing,
              value: parsedParam.value,
              key: parsedParam.key
            };
          }
          return parsedParam;
        });
        
        return mergedParams;
      });
    } else {
      setRequestParams([]);
    }
    
    // Handle path params
    if (parsed.pathParams.length > 0) {
      setRequestPathParams(prevParams => {
        const mergedParams = parsed.pathParams.map(newParam => {
          const existing = prevParams.find(p => p.key === newParam.key);
          if (existing) {
            return {
              ...newParam,
              value: existing.value || newParam.value
            };
          }
          return newParam;
        });
        
        console.log('📋 Updated path params from URL:', mergedParams);
        return mergedParams;
      });
      setTemplateUrl(parsed.templateUrl);
    } else {
      setRequestPathParams([]);
      setTemplateUrl(newUrl);
    }
  }
  
  console.log('📝 URL changed:', newUrl);
  
  // Restore cursor position after React renders
  requestAnimationFrame(() => {
    if (urlInputRef.current && isUpdatingFromInput.current) {
      urlInputRef.current.selectionStart = cursorPos;
      urlInputRef.current.selectionEnd = cursorPos;
      isUpdatingFromInput.current = false;
    }
  });
  
}, [parseUrlIntoTemplateAndParams, requestPathParams, requestUrl]);

// Replace your existing handleUrlPaste function
const handleUrlPaste = useCallback((e) => {
  // Prevent the default paste to handle it ourselves
  e.preventDefault();
  
  // Get pasted text
  const pastedText = e.clipboardData.getData('text');
  
  // Get current cursor position and selection
  const start = e.target.selectionStart;
  const end = e.target.selectionEnd;
  const currentUrl = requestUrl;
  
  // Insert pasted text at cursor position, replacing any selected text
  const newUrl = currentUrl.substring(0, start) + pastedText + currentUrl.substring(end);
  
  // Set flag that this is a paste operation
  isUpdatingFromInput.current = true;
  
  // Update URL with pasted content
  setRequestUrl(newUrl);
  
  // Parse the URL after paste
  setTimeout(() => {
    const parsed = parseUrlIntoTemplateAndParams(newUrl, requestPathParams);
    
    // Update query params if needed
    if (parsed.queryParams.length > 0) {
      setRequestParams(prevParams => {
        const existingParamsMap = new Map(
          prevParams.filter(p => p.key).map(p => [p.key, p])
        );
        
        const mergedParams = parsed.queryParams.map(parsedParam => {
          const existing = existingParamsMap.get(parsedParam.key);
          if (existing) {
            return {
              ...existing,
              value: parsedParam.value,
              key: parsedParam.key
            };
          }
          return parsedParam;
        });
        
        return mergedParams;
      });
    }
    
    // Update path params
    if (parsed.pathParams.length > 0) {
      setRequestPathParams(parsed.pathParams);
      setTemplateUrl(parsed.templateUrl);
    }
    
    // Show toast with parse results
    let message = '';
    if (parsed.queryParams.length > 0 && parsed.pathParams.length > 0) {
      message = `Parsed ${parsed.pathParams.length} path param(s) and ${parsed.queryParams.length} query param(s)`;
    } else if (parsed.queryParams.length > 0) {
      message = `Parsed ${parsed.queryParams.length} query parameter(s)`;
    } else if (parsed.pathParams.length > 0) {
      message = `Parsed ${parsed.pathParams.length} path parameter(s)`;
    }
    
    if (message) {
      showToast(message, 'success');
    }
    
    // Force cursor position after all updates
    requestAnimationFrame(() => {
      if (urlInputRef.current) {
        const newCursorPos = start + pastedText.length;
        urlInputRef.current.selectionStart = newCursorPos;
        urlInputRef.current.selectionEnd = newCursorPos;
      }
      isUpdatingFromInput.current = false;
    });
  }, 0); // Use 0ms timeout to let React finish rendering
  
}, [requestUrl, requestPathParams, parseUrlIntoTemplateAndParams, showToast]);

// In updatePathParam, when updating, preserve position
const updatePathParam = (id, field, value) => {
  console.log('🟢 updatePathParam called:', { id, field, value });
  
  // Set flag that this is an internal update
  isUpdatingFromInput.current = true;
  
  // First update the state with the new value
  setRequestPathParams(params => {
    const updatedParams = params.map(param => 
      param.id === id ? { ...param, [field]: value } : param
    );
    
    // If the key changed, update the template URL
    if (field === 'key' && templateUrl) {
      const oldParam = params.find(p => p.id === id);
      if (oldParam && oldParam.key && oldParam.key !== value) {
        const oldPlaceholder = `{${oldParam.key}}`;
        const newPlaceholder = `{${value}}`;
        const newTemplateUrl = templateUrl.replace(oldPlaceholder, newPlaceholder);
        setTemplateUrl(newTemplateUrl);
      }
    }
    
    // If value changed, update the URL immediately
    if (field === 'value' && templateUrl) {
      const param = updatedParams.find(p => p.id === id);
      if (param && param.key) {
        const placeholder = `{${param.key}}`;
        if (templateUrl.includes(placeholder)) {
          // CRITICAL FIX: Only replace if the value doesn't contain other placeholders
          const newValue = value && value.trim() !== '' && !value.includes('{') && !value.includes('}') 
            ? value 
            : placeholder;
          
          let newUrl = templateUrl.replace(placeholder, newValue);
          
          // Add back any query parameters
          if (requestUrl.includes('?')) {
            const queryString = requestUrl.split('?')[1];
            if (queryString) {
              newUrl = newUrl.split('?')[0] + '?' + queryString;
            }
          }
          
          console.log('Updating URL to:', newUrl);
          setRequestUrl(newUrl);
        }
      }
    }
    
    // Reset flag after a short delay
    setTimeout(() => {
      isUpdatingFromInput.current = false;
    }, 200);
    
    return updatedParams;
  });
};

useEffect(() => {
  // Don't do anything if we're in the middle of user input
  if (isUpdatingFromInput.current) {
    return;
  }

  // Create a stable reference to all current values
  const currentUrl = requestUrl;
  const currentPathParams = requestPathParams;
  const currentTemplateUrl = templateUrl;
  const currentRequestParams = requestParams;

  // Use a single timeout to batch all updates
  const timeoutId = setTimeout(() => {
    // Step 1: Clean any encoded placeholders in the URL
    let workingUrl = currentUrl;
    
    // Check if URL has encoded placeholders
    if (workingUrl && (workingUrl.includes('%7B') || workingUrl.includes('%7D'))) {
      try {
        workingUrl = decodeURIComponent(workingUrl);
      } catch {
        // Ignore decode errors
      }
      workingUrl = cleanUrlEncodedPlaceholders(workingUrl);
    }

    // Step 2: Clean path param values (remove any placeholder text)
    let needsPathParamUpdate = false;
    const cleanedPathParams = currentPathParams.map(param => {
      const value = param.value || '';
      const hasPlaceholder = value.includes('{') || value.includes('}') || 
                            value.includes('%7B') || value.includes('%7D');
      if (hasPlaceholder) {
        needsPathParamUpdate = true;
        return { ...param, value: '' };
      }
      return param;
    });

    if (needsPathParamUpdate) {
      setRequestPathParams(cleanedPathParams);
    }

    // Step 3: Rebuild URL with path params if we have a template
    if (currentTemplateUrl && currentPathParams.length > 0) {
      const cleanTemplateUrl = cleanUrlEncodedPlaceholders(currentTemplateUrl);
      let updatedUrl = cleanTemplateUrl;
      
      // Create a map of placeholder to value
      const paramMap = new Map();
      cleanedPathParams.forEach(param => {
        if (param.key) {
          paramMap.set(param.key, param.value && param.value.trim() !== '' ? param.value : null);
        }
      });
      
      // Sort placeholders by length
      const placeholders = Array.from(paramMap.keys()).sort((a, b) => b.length - a.length);
      
      // Replace each placeholder
      placeholders.forEach(key => {
        const value = paramMap.get(key);
        const placeholder = `{${key}}`;
        const colonPlaceholder = `:${key}`;
        
        if (updatedUrl.includes(placeholder)) {
          const replacementValue = value && !value.includes('{') && !value.includes('}') ? value : placeholder;
          const regex = new RegExp(placeholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g');
          updatedUrl = updatedUrl.replace(regex, replacementValue);
        }
        
        if (updatedUrl.includes(colonPlaceholder)) {
          const replacementValue = value && !value.includes('{') && !value.includes('}') ? value : colonPlaceholder;
          const regex = new RegExp(colonPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g');
          updatedUrl = updatedUrl.replace(regex, replacementValue);
        }
      });
      
      // Add query string
      const queryString = currentRequestParams
        .filter(p => p.enabled && p.key && p.key.trim() !== '')
        .map(p => `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value || '')}`)
        .join('&');
      
      const finalUrl = queryString ? `${updatedUrl}?${queryString}` : updatedUrl;
      workingUrl = finalUrl;
    }

    // Step 4: Only update if the URL actually changed
    if (workingUrl !== currentUrl && workingUrl !== lastRebuiltUrlRef.current) {
      console.log('🔄 Final URL after all processing:', workingUrl);
      lastRebuiltUrlRef.current = workingUrl;
      setRequestUrl(workingUrl);
    }

  }, 250); // Single debounce for all operations

  return () => clearTimeout(timeoutId);
}, [
  requestUrl, 
  JSON.stringify(requestPathParams.map(p => ({ key: p.key, value: p.value, enabled: p.enabled }))),
  templateUrl,
  JSON.stringify(requestParams.map(p => ({ key: p.key, value: p.value, enabled: p.enabled })))
]);

// Fix 2: Update the determineActiveTab function to be more robust
const determineActiveTab = useCallback(() => {
  // Check each tab in order of preference
  if (requestPathParams && requestPathParams.length > 0) {
    return 'path-params';
  }
  if (requestParams && requestParams.length > 0) {
    return 'query-params';
  }
  // Check if body has actual content (not just empty object or whitespace)
  if (requestBody && requestBody.trim() !== '' && 
      requestBody !== '{}' && 
      requestBody !== '{\n  \n}' && 
      requestBody !== '{\n  \n}') {
    return 'body';
  }
  if (requestHeaders && requestHeaders.length > 0) {
    return 'headers';
  }
  if (authType && authType !== 'noauth') {
    return 'authorization';
  }
  // Default to path params if nothing has content
  return 'path-params';
}, [requestPathParams, requestParams, requestBody, requestHeaders, authType]);

// Fix 3: Update the URL update function to handle the URL correctly
const updateUrlWithPathParam = (key, value) => {
  setRequestUrl(prevUrl => {
    if (!prevUrl) return prevUrl;
    
    // Check if the parameter exists in the URL as a placeholder
    const hasPlaceholder = prevUrl.includes(`{${key}}`) || prevUrl.includes(`:${key}`);
    
    if (hasPlaceholder) {
      // Replace placeholder with value or keep placeholder if empty
      const placeholderPatterns = [
        new RegExp(`{${key}}`, 'g'),
        new RegExp(`:${key}`, 'g')
      ];
      
      let newUrl = prevUrl;
      placeholderPatterns.forEach(pattern => {
        if (pattern.test(newUrl)) {
          const replacementValue = value && value.trim() !== '' ? value : `{${key}}`;
          newUrl = newUrl.replace(pattern, replacementValue);
        }
      });
      return newUrl;
    }
    return prevUrl;
  });
};



// Fix 4: Add effect to update URL when path params change (as a backup)
useEffect(() => {
  if (selectedRequest && requestPathParams.length > 0) {
    // This will run after all path param updates are complete
    // It ensures the URL stays in sync with the path params
    let updatedUrl = requestUrl;
    requestPathParams.forEach(param => {
      if (param.key) {
        updatedUrl = updateUrlWithPathParamSync(updatedUrl, param.key, param.value);
      }
    });
    if (updatedUrl !== requestUrl) {
      setRequestUrl(updatedUrl);
    }
  }
}, [requestPathParams.map(p => `${p.key}:${p.value}`).join('|')]); // Only run when key:value pairs change

// Helper function for sync URL update
const updateUrlWithPathParamSync = (url, key, value) => {
  if (!url) return url;
  
  const hasPlaceholder = url.includes(`{${key}}`) || url.includes(`:${key}`);
  
  if (hasPlaceholder) {
    const placeholderPatterns = [
      new RegExp(`{${key}}`, 'g'),
      new RegExp(`:${key}`, 'g')
    ];
    
    let newUrl = url;
    placeholderPatterns.forEach(pattern => {
      if (pattern.test(newUrl)) {
        const replacementValue = value && value.trim() !== '' ? value : `{${key}}`;
        newUrl = newUrl.replace(pattern, replacementValue);
      }
    });
    return newUrl;
  }
  return url;
};



// Delete path param
const deletePathParam = (id) => {
  setRequestPathParams(params => {
    const remainingParams = params.filter(param => param.id !== id);
    
    // Rebuild URL with remaining params
    setRequestUrl(prevUrl => {
      if (!prevUrl) return prevUrl;
      
      // Extract base URL (everything before the first path param)
      const baseUrl = prevUrl.split('/').slice(0, 3).join('/'); // Assumes format: protocol://domain/base
      
      // Rebuild URL with remaining params
      let newUrl = baseUrl;
      remainingParams.forEach((param, index) => {
        const value = param.value && param.value.trim() !== '' ? param.value : `{${param.key}}`;
        newUrl = newUrl + '/' + value;
      });
      
      return newUrl;
    });
    
    return remainingParams;
  });
};

  // Add URL parsing function
  const parseUrlParams = useCallback((url) => {
    try {
      // Check if it's a valid URL
      const urlObj = new URL(url);
      const params = [];
      
      // Parse query parameters
      urlObj.searchParams.forEach((value, key) => {
        params.push({
          id: `param-${Date.now()}-${Math.random()}`,
          key: key,
          value: value,
          description: '',
          enabled: true
        });
      });
      
      return params;
    } catch (e) {
      // Not a valid URL or no query string
      return [];
    }
  }, []);

  

  // ==================== API METHODS ====================

  
  // Update the transformCollectionsData function
const transformCollectionsData = (apiData) => {
  console.log('🔄 [Transform] Input:', apiData);
  
  if (!apiData) {
    console.warn('⚠️ [Transform] No data provided');
    return [];
  }
  
  let collectionsArray = [];
  
  if (Array.isArray(apiData)) {
    collectionsArray = apiData;
  } else if (apiData.collections && Array.isArray(apiData.collections)) {
    collectionsArray = apiData.collections;
  } else {
    console.warn('⚠️ [Transform] Unknown data structure:', apiData);
    return [];
  }
  
  console.log(`📊 [Transform] Processing ${collectionsArray.length} collections`);
  
  return collectionsArray.map((collection, collectionIndex) => {
    // Process folders and their requests
    const folders = (collection.folders || []).map((folder, folderIndex) => {
      // Process requests within folder
      const requests = (folder.requests || []).map((request, requestIndex) => {
        // Separate parameters based on parameterLocation
        const queryParams = [];
        const pathParams = [];
        const headerParams = []; // This will ONLY contain parameters with location='header'
        const bodyParams = [];
        
        console.log(`📋 [Transform] Processing request: ${request.name || 'Unnamed'}`, {
          parameters: request.parameters?.length || 0,
          requestBody: request.requestBody ? 'present' : 'absent'
        });
        
        // Process parameters with location info
        (request.parameters || []).forEach((param, paramIdx) => {
  if (param && param.key) {
    const paramId = param.id || `param-${Date.now()}-${Math.random()}-${paramIdx}`;
    
    // CRITICAL FIX: Clean the value at source
    let paramValue = param.value || '';
    
    // If the value contains placeholder patterns, set it to empty string
    const hasPlaceholder = paramValue.includes('{') || paramValue.includes('}') || 
                          paramValue.includes('%7B') || paramValue.includes('%7D');
    
    if (hasPlaceholder) {
      console.log(`🧹 Cleaning placeholder value for param ${param.key}: ${paramValue} -> ''`);
      paramValue = '';
    }
    
    const paramObject = {
      id: paramId,
      key: param.key,
      value: paramValue, // Use cleaned value
      description: param.description || '',
      enabled: param.enabled !== false,
      required: param.required || false,
      type: param.type || 'string',
      parameterLocation: param.parameterLocation || 'query',
      paramMode: param.paramMode || 'IN',
      dbColumn: param.dbColumn || null,
      oracleType: param.oracleType || null,
      validationPattern: param.validationPattern || '',
      defaultValue: param.defaultValue || '',
      example: param.example || null,
      bodyFormat: param.bodyFormat || null
    };
    
    console.log(`📍 [Transform] Parameter ${param.key}: location = ${param.parameterLocation}, value = ${paramValue}`);
    
    // Sort by location - CRITICAL for path params
    switch(param.parameterLocation?.toLowerCase()) {
      case 'query':
        queryParams.push(paramObject);
        break;
      case 'path':
        pathParams.push(paramObject);
        console.log(`🛣️ [Transform] Added to PATH params: ${param.key} = ${paramValue}`);
        break;
      case 'header':
        headerParams.push(paramObject);
        console.log(`📌 [Transform] Added to HEADER params: ${param.key}`);
        break;
      case 'body':
        bodyParams.push(paramObject);
        console.log(`📦 [Transform] Added to BODY params: ${param.key}`);
        break;
      default:
        // Default to query if location not specified
        queryParams.push(paramObject);
    }
  }
});
        
        // Process headers separately (these are HTTP headers, NOT parameters)
        const headers = (request.headers || []).map((header, headerIdx) => ({
          id: header.id || `header-${Date.now()}-${Math.random()}-${headerIdx}`,
          key: header.key,
          value: header.value || '',
          description: header.description || '',
          enabled: header.enabled !== false,
          required: header.required || false
        }));
        
        console.log(`📊 [Transform] Request "${request.name || 'Unnamed'}" - Query: ${queryParams.length}, Path: ${pathParams.length}, Header Params: ${headerParams.length}, Body: ${bodyParams.length}, Regular Headers: ${headers.length}`);
        
        // Log path params specifically
        if (pathParams.length > 0) {
          console.log(`🛣️ [Transform] Path params for ${request.name}:`, 
            pathParams.map(p => ({ key: p.key, value: p.value })));
        }
        
        // Log header params specifically
        if (headerParams.length > 0) {
          console.log(`📌 [Transform] Header params for ${request.name}:`, 
            headerParams.map(p => ({ key: p.key, value: p.value })));
        }
        
        // Log body params specifically
        if (bodyParams.length > 0) {
          console.log(`📦 [Transform] Body params for ${request.name}:`, 
            bodyParams.map(p => ({ key: p.key, value: p.value, bodyFormat: p.bodyFormat })));
        }
        
        // ============== FIXED: Extract auth info properly ==============
        console.log('🔐 [Transform] Raw auth data:', {
          authType: request.authType,
          authConfig: request.authConfig,
          auth: request.auth
        });

        const authType = request.authType || request.auth?.type || 'noauth';

        // Process auth config based on auth type
        let processedAuthConfig = { type: authType };

        if (authType === 'apikey') {
          // Extract API Key specific fields from all possible sources
          const sourceConfig = request.authConfig || request.auth || {};
          processedAuthConfig = {
            type: 'apikey',
            key: sourceConfig.key || sourceConfig.apiKey || sourceConfig.apiKeyHeader || '',
            value: sourceConfig.value || sourceConfig.apiSecret || sourceConfig.apiKeyValue || sourceConfig.secret || '',
            addTo: sourceConfig.addTo || 'header'
          };
          console.log('🔐 [Transform] Processed API Key config:', processedAuthConfig);
        } 
        else if (authType === 'bearer') {
          const sourceConfig = request.authConfig || request.auth || {};
          processedAuthConfig = {
            type: 'bearer',
            token: sourceConfig.token || sourceConfig.bearerToken || '',
            tokenType: sourceConfig.tokenType || 'Bearer'
          };
          console.log('🔐 [Transform] Processed Bearer config:', processedAuthConfig);
        } 
        else if (authType === 'basic') {
          const sourceConfig = request.authConfig || request.auth || {};
          processedAuthConfig = {
            type: 'basic',
            username: sourceConfig.username || '',
            password: sourceConfig.password || ''
          };
          console.log('🔐 [Transform] Processed Basic config:', processedAuthConfig);
        } 
        else if (authType === 'oauth2') {
          const sourceConfig = request.authConfig || request.auth || {};
          processedAuthConfig = {
            type: 'oauth2',
            token: sourceConfig.token || ''
          };
          console.log('🔐 [Transform] Processed OAuth2 config:', processedAuthConfig);
        }
        else {
          processedAuthConfig = { type: 'noauth' };
        }
        // ============== END FIX ==============
        
        // Process body content
        let bodyContent = request.body || '';
        let requestBodyData = request.requestBody || null;

        // If there's a requestBody object with sample, use that
        if (requestBodyData && requestBodyData.sample) {
          bodyContent = requestBodyData.sample;
        }

        // If there are body parameters and it's XML, try to build a sample XML
        if (bodyParams.length > 0 && requestBodyData?.bodyType === 'xml') {
          try {
            const xmlParts = ['<?xml version="1.0" encoding="UTF-8"?>', '<request>'];
            bodyParams.forEach(param => {
              if (param.key) {
                xmlParts.push(`  <${param.key}>${param.value || param.defaultValue || param.example || ''}</${param.key}>`);
              }
            });
            xmlParts.push('</request>');
            
            // Only set if we have valid XML and no other body content
            if (xmlParts.length > 2 && !bodyContent) {
              bodyContent = xmlParts.join('\n');
            }
          } catch (e) {
            console.warn('⚠️ [Transform] Error building XML from body params:', e);
          }
        }
        
        // CRITICAL FIX: Construct URL with path parameter placeholders
        let requestUrl = request.url || '';
        
        // If we have path params, we need to ensure the URL has placeholders for them
        if (pathParams.length > 0) {
          console.log(`🛣️ [Transform] Building URL with ${pathParams.length} path params`);
          
          // Check if the URL already has placeholders
          const hasPlaceholders = pathParams.some(p => 
            requestUrl.includes(`{${p.key}}`) || requestUrl.includes(`:${p.key}`)
          );
          
          if (!hasPlaceholders) {
            // The URL doesn't have placeholders, so we need to add them
            
            // Check if the URL ends with a slash
            if (!requestUrl.endsWith('/')) {
              requestUrl += '/';
            }
            
            // Add each path param placeholder
            pathParams.forEach((param, index) => {
              if (index === 0) {
                // For the first param, just add it
                requestUrl += `{${param.key}}`;
              } else {
                // For subsequent params, add slash then placeholder
                requestUrl += `/{${param.key}}`;
              }
            });
            
            console.log(`✅ [Transform] Added placeholders to URL: ${requestUrl}`);
          } else {
            console.log(`✅ [Transform] URL already has placeholders: ${requestUrl}`);
          }
        }
        
        return {
          id: request.id || `req-${Date.now()}-${Math.random()}-${requestIndex}`,
          name: request.name || 'New Request',
          method: request.method || 'GET',
          url: requestUrl,
          description: request.description || '',
          isEditing: false,
          status: request.status || 'saved',
          lastModified: request.lastModified || new Date().toISOString(),
          auth: processedAuthConfig,
          authType: authType,
          authConfig: processedAuthConfig,
          // Regular headers only - these are actual HTTP headers like Content-Type
          headers: cleanHeaders([...(request.headers || [])]),
          // Parameters separated by location - these will be used in handleSelectRequest
          queryParams: queryParams,
          pathParams: pathParams,
          headerParams: headerParams, // These will be added to headers in handleSelectRequest
          bodyParams: bodyParams,
          allParams: request.parameters || [],
          body: bodyContent,
          requestBody: requestBodyData,
          tests: request.tests || '',
          preRequestScript: request.preRequestScript || '',
          isSaved: request.saved !== false,
          collectionId: request.collectionId || collection.id,
          folderId: request.folderId || folder.id,
          apiCode: request.apiCode || null,
          apiName: request.apiName || null
        };
      });

      return {
        id: folder.id || `folder-${Date.now()}-${Math.random()}-${folderIndex}`,
        name: folder.name || 'New Folder',
        description: folder.description || '',
        isExpanded: false,
        isEditing: false,
        requestCount: folder.requestCount || requests.length,
        requests: requests
      };
    });

    const totalRequests = folders.reduce((sum, folder) => sum + (folder.requests?.length || 0), 0);

    return {
      id: collection.id || `col-${Date.now()}-${Math.random()}-${collectionIndex}`,
      name: collection.name || `Collection ${collectionIndex + 1}`,
      description: collection.description || '',
      isExpanded: collectionIndex === 0,
      isFavorite: collection.favorite || false,
      isEditing: false,
      createdAt: collection.createdAt || new Date().toISOString(),
      requestsCount: totalRequests,
      folderCount: folders.length,
      variables: (collection.variables || []).map((v, varIndex) => ({
        id: v.id || `var-${Date.now()}-${Math.random()}-${varIndex}`,
        key: v.key,
        value: v.value,
        type: v.type || 'text',
        enabled: v.enabled !== false
      })),
      folders: folders
    };
  });
};

// Complete updated handleSelectRequest function
const handleSelectRequest = useCallback(async (request, collectionId, folderId) => {
  console.log('🎯 [handleSelectRequest] Selected request:', {
    id: request.id,
    name: request.name,
    url: request.url,
    authType: request.authType,
    authConfig: request.authConfig,
    pathParams: request.pathParams,
    queryParams: request.queryParams,
    hasRequestBody: !!request.requestBody
  });

  // Check if this is a new/empty request
  const isNewRequest = request.id?.startsWith('req-') || !request.url;
  
  if (isNewRequest) {
    console.log('🆕 New request detected - resetting all form fields');
    
    // Reset all form states to empty
    setRequestMethod('GET');
    setRequestUrl('');
    setTemplateUrl('');
    setRequestBody('');
    setRequestParams([]);
    setRequestHeaders([]);
    setRequestPathParams([]);
    setFormData([]);
    setUrlEncodedData([]);
    setBinaryFile(null);
    setGraphqlQuery('');
    setGraphqlVariables('');
    setAuthType('noauth');
    setAuthConfig({ type: 'noauth', token: '', username: '', password: '', key: '', value: '', addTo: 'header', tokenType: 'Bearer' });
    setResponse(null);
    
    // Reset body type to default
    setRequestBodyType('raw');
    setRawBodyType('json');
    
    // Set the selected request
    const newRequestWithContext = { ...request, collectionId, folderId };
    setSelectedRequest(newRequestWithContext);
    
    // Update tabs
    setRequestTabs(tabs => {
      const existingTab = tabs.find(t => t.id === request.id);
      if (existingTab) {
        return tabs.map(t => ({ ...t, isActive: t.id === request.id }));
      } else {
        return tabs.map(t => ({ ...t, isActive: false }))
          .concat({ 
            id: request.id, 
            name: request.name || 'New Request', 
            method: 'GET', 
            collectionId, 
            folderId, 
            isActive: true 
          });
      }
    });
    
    setActiveTab('path-params');
    return;
  }

  // First, set the initial request data from the tree
  console.log('📥 Setting initial request data from tree');
  
  setRequestMethod(request.method || 'GET');

  // Store the template URL - DECODE IT FIRST to handle encoded curly braces
  let initialTemplateUrl = request.url || '';
  console.log('📋 Original template URL:', initialTemplateUrl);

  // DECODE the URL first to handle any encoded curly braces
  try {
    const decodedUrl = decodeURIComponent(initialTemplateUrl);
    if (decodedUrl !== initialTemplateUrl) {
      console.log('📋 Decoded URL:', decodedUrl);
      initialTemplateUrl = decodedUrl;
    }
  } catch (e) {
    // If decoding fails, use original
    console.log('📋 URL decoding failed, using original:', initialTemplateUrl);
  }

  // CRITICAL FIX: Clean URL-encoded placeholders
  const cleanedTemplateUrl = cleanUrlEncodedPlaceholders(initialTemplateUrl);
  if (cleanedTemplateUrl !== initialTemplateUrl) {
    console.log('🧹 Cleaned template URL:', cleanedTemplateUrl);
    initialTemplateUrl = cleanedTemplateUrl;
  }

  setTemplateUrl(initialTemplateUrl);
  setRequestUrl(initialTemplateUrl);
  
  // Set request body and other params
  setRequestBody(request.body || '');
  setRequestParams(request.queryParams || []);
  
  // IMPORTANT: Reset headers - we'll add them properly from API details
  setRequestHeaders([]);
  
  // Set path params
  const initialPathParams = request.pathParams || [];
  setRequestPathParams(initialPathParams);
  
  // Process path params immediately to update URL with values
if (initialPathParams.length > 0 && initialTemplateUrl) {
  console.log('🛣️ Processing path params for URL:', { initialTemplateUrl, initialPathParams });
  
  let updatedUrl = initialTemplateUrl;
  initialPathParams.forEach(param => {
    if (param.key && param.value && param.value.trim() !== '') {
      const placeholder = `{${param.key}}`;
      if (updatedUrl.includes(placeholder)) {
        // CRITICAL FIX: Only encode when actually sending the request
        // For display, keep the value as-is
        updatedUrl = updatedUrl.replace(new RegExp(placeholder, 'g'), param.value);
        console.log(`  🔄 Replaced ${placeholder} with ${param.value}`);
      } else {
        const colonPlaceholder = `:${param.key}`;
        if (updatedUrl.includes(colonPlaceholder)) {
          updatedUrl = updatedUrl.replace(new RegExp(colonPlaceholder, 'g'), param.value);
          console.log(`  🔄 Replaced ${colonPlaceholder} with ${param.value}`);
        }
      }
    }
  });
  
  console.log('✅ Updated URL with path params:', updatedUrl);
  setRequestUrl(updatedUrl);
}
  
  // ============== FIXED: Set auth with proper config ==============
  console.log('🔐 Setting auth from request:', {
    authType: request.authType,
    authConfig: request.authConfig,
    auth: request.auth
  });

  // Get the auth config from the most appropriate source
  const authConfigFromRequest = request.authConfig || request.auth || {};
  const authTypeFromRequest = request.authType || request.auth?.type || 'noauth';

  // Process based on auth type
  let processedAuthConfig = { type: authTypeFromRequest };

  if (authTypeFromRequest === 'apikey') {
    // For API Key, we need key and value
    processedAuthConfig = {
      type: 'apikey', // Keep the original type for saving
      key: authConfigFromRequest.key || authConfigFromRequest.apiKey || '',
      value: authConfigFromRequest.value || authConfigFromRequest.apiSecret || authConfigFromRequest.secret || '',
      addTo: authConfigFromRequest.addTo || 'header'
    };
    console.log('🔐 Processing API Key config:', {
      type: processedAuthConfig.type,
      key: processedAuthConfig.key,
      value: processedAuthConfig.value ? '***' : '(empty)',
      addTo: processedAuthConfig.addTo
    });
    
    // For API Key, set the Authorization tab to "No Auth" since API keys go in headers
    setAuthType('noauth');
    
    // Add API Key headers to Headers tab
    const headersToAdd = [];
    
    if (processedAuthConfig.key && processedAuthConfig.value) {
      headersToAdd.push({
        id: `auth-header-${Date.now()}-1`,
        key: processedAuthConfig.key,
        value: processedAuthConfig.value,
        description: 'API Key authentication',
        enabled: true,
        required: true
      });
    }
    
    // Add all headers at once
    if (headersToAdd.length > 0) {
      setRequestHeaders(headersToAdd);
      console.log('✅ Added API Key headers:', headersToAdd.map(h => h.key));
    }
  } 
  else if (authTypeFromRequest === 'bearer') {
    processedAuthConfig = {
      type: 'bearer',
      token: authConfigFromRequest.token || authConfigFromRequest.bearerToken || '',
      tokenType: authConfigFromRequest.tokenType || 'Bearer'
    };
    console.log('🔐 Setting Bearer config (initial):', processedAuthConfig);
    setAuthType('bearer');
    
    // Add Authorization header for Bearer token
    if (processedAuthConfig.token) {
      setRequestHeaders([{
        id: `auth-header-${Date.now()}`,
        key: 'Authorization',
        value: `${processedAuthConfig.tokenType} ${processedAuthConfig.token}`,
        description: 'Bearer token authentication',
        enabled: true,
        required: true
      }]);
    }
  }
  else if (authTypeFromRequest === 'basic') {
    processedAuthConfig = {
      type: 'basic',
      username: authConfigFromRequest.username || '',
      password: authConfigFromRequest.password || ''
    };
    console.log('🔐 Setting Basic config (initial):', processedAuthConfig);
    setAuthType('basic');
    
    // Add Authorization header for Basic auth
    if (processedAuthConfig.username && processedAuthConfig.password) {
      const credentials = btoa(`${processedAuthConfig.username}:${processedAuthConfig.password}`);
      setRequestHeaders([{
        id: `auth-header-${Date.now()}`,
        key: 'Authorization',
        value: `Basic ${credentials}`,
        description: 'Basic authentication',
        enabled: true,
        required: true
      }]);
    }
  }
  else if (authTypeFromRequest === 'oauth2') {
    processedAuthConfig = {
      type: 'oauth2',
      token: authConfigFromRequest.token || ''
    };
    console.log('🔐 Setting OAuth2 config (initial):', processedAuthConfig);
    setAuthType('oauth2');
    
    // Add Authorization header for OAuth2
    if (processedAuthConfig.token) {
      setRequestHeaders([{
        id: `auth-header-${Date.now()}`,
        key: 'Authorization',
        value: `Bearer ${processedAuthConfig.token}`,
        description: 'OAuth2 token authentication',
        enabled: true,
        required: true
      }]);
    }
  }
  else {
    // No auth
    processedAuthConfig = { type: 'noauth' };
    setAuthType('noauth');
    setRequestHeaders([]);
  }

  // Set the auth config in state
  setAuthConfig(processedAuthConfig);
  // ============== END FIX ==============
  
  setResponse(null);
  
  // Set the selected request
  const requestWithContext = { ...request, collectionId, folderId };
  setSelectedRequest(requestWithContext);
  
  // Update tabs
  setRequestTabs(tabs => {
    const existingTab = tabs.find(t => t.id === request.id);
    if (existingTab) {
      return tabs.map(t => ({ ...t, isActive: t.id === request.id }));
    } else {
      return tabs.map(t => ({ ...t, isActive: false }))
        .concat({ 
          id: request.id, 
          name: request.name, 
          method: request.method, 
          collectionId, 
          folderId, 
          isActive: true 
        });
    }
  });
  
  // Parse URL for query params
  if (request.url && request.url.includes('?')) {
    const parsed = parseUrlIntoTemplateAndParams(request.url);
    if (parsed.queryParams.length > 0) {
      setRequestParams(prev => {
        const merged = [...prev];
        parsed.queryParams.forEach(p => {
          if (!merged.some(m => m.key === p.key)) {
            merged.push(p);
          }
        });
        return merged;
      });
    }
  }
  
  // Then fetch additional details from API
  if (authToken && request.id && collectionId) {
    setLoading(prev => ({ ...prev, request: true }));
    try {
      const response = await getRequestDetails(authToken, collectionId, request.id);
      if (!isMounted.current) return;
      
      const processedResponse = handleCollectionsResponse(response);
      const details = extractRequestDetails(processedResponse);
      
      if (details) {
        console.log('📦 [API Details] Received:', {
          hasRequestBody: !!details.requestBody,
          bodyType: details.requestBody?.bodyType,
          parametersCount: details.parameters?.length,
          authType: details.authType,
          authConfig: details.authConfig
        });
        
        // ============== FIXED: Update auth from API details ==============
        if (details.authConfig) {
          console.log('🔐 Updating auth config from API:', details.authConfig);
          
          const apiAuthType = details.authType || authTypeFromRequest;
          
          if (apiAuthType === 'apikey' || details.authConfig.authType === 'apiKey') {
            // Handle API Key from API - check for both formats
            const apiProcessedConfig = {
              type: 'apikey',
              key: details.authConfig.key || 
                    details.authConfig.apiKey || 
                    details.authConfig.apiKeyHeader || 
                    processedAuthConfig.key || '',
              value: details.authConfig.value || 
                     details.authConfig.apiSecret || 
                     details.authConfig.apiKeyValue || 
                     details.authConfig.secret || 
                     processedAuthConfig.value || '',
              addTo: details.authConfig.addTo || processedAuthConfig.addTo || 'header'
            };
            console.log('🔐 Processing API Key config from API:', apiProcessedConfig);
            
            // Keep authType as 'noauth' for the UI (API keys go in headers)
            setAuthType('noauth');
            
            // Update headers for API Key - handle both key and secret
            const headersToAdd = [];
            
            // Add API Key header if present
            if (apiProcessedConfig.key && apiProcessedConfig.value) {
              headersToAdd.push({
                id: `auth-header-${Date.now()}-key`,
                key: apiProcessedConfig.key,
                value: apiProcessedConfig.value,
                description: 'API Key authentication',
                enabled: true,
                required: true
              });
            }
            
            // Check for API Secret (if different from key)
            if (details.authConfig.apiSecretHeader && details.authConfig.apiSecretValue) {
              headersToAdd.push({
                id: `auth-header-${Date.now()}-secret`,
                key: details.authConfig.apiSecretHeader,
                value: details.authConfig.apiSecretValue,
                description: 'API Secret authentication',
                enabled: true,
                required: true
              });
            }
            
            // Add all headers - REPLACE existing headers with just these
            if (headersToAdd.length > 0) {
              setRequestHeaders(headersToAdd);
              console.log('✅ Updated API Key headers from API:', headersToAdd.map(h => h.key));
            }
            
            // Update the auth config in state
            setAuthConfig(apiProcessedConfig);
          } 
          else if (apiAuthType === 'bearer') {
            const apiProcessedConfig = {
              type: 'bearer',
              token: details.authConfig.token || details.authConfig.bearerToken || processedAuthConfig.token || '',
              tokenType: details.authConfig.tokenType || processedAuthConfig.tokenType || 'Bearer'
            };
            console.log('🔐 Setting Bearer config from API:', apiProcessedConfig);
            
            setAuthType('bearer');
            setAuthConfig(apiProcessedConfig);
            
            // Add Authorization header for Bearer token
            if (apiProcessedConfig.token) {
              setRequestHeaders([{
                id: `auth-header-${Date.now()}`,
                key: 'Authorization',
                value: `${apiProcessedConfig.tokenType} ${apiProcessedConfig.token}`,
                description: 'Bearer token authentication',
                enabled: true,
                required: true
              }]);
            }
          }
          else if (apiAuthType === 'basic') {
            const apiProcessedConfig = {
              type: 'basic',
              username: details.authConfig.username || processedAuthConfig.username || '',
              password: details.authConfig.password || processedAuthConfig.password || ''
            };
            console.log('🔐 Setting Basic config from API:', apiProcessedConfig);
            
            setAuthType('basic');
            setAuthConfig(apiProcessedConfig);
            
            // Add Authorization header for Basic auth
            if (apiProcessedConfig.username && apiProcessedConfig.password) {
              const credentials = btoa(`${apiProcessedConfig.username}:${apiProcessedConfig.password}`);
              setRequestHeaders([{
                id: `auth-header-${Date.now()}`,
                key: 'Authorization',
                value: `Basic ${credentials}`,
                description: 'Basic authentication',
                enabled: true,
                required: true
              }]);
            }
          }
          else if (apiAuthType === 'oauth2') {
            const apiProcessedConfig = {
              type: 'oauth2',
              token: details.authConfig.token || processedAuthConfig.token || ''
            };
            console.log('🔐 Setting OAuth2 config from API:', apiProcessedConfig);
            
            setAuthType('oauth2');
            setAuthConfig(apiProcessedConfig);
            
            // Add Authorization header for OAuth2
            if (apiProcessedConfig.token) {
              setRequestHeaders([{
                id: `auth-header-${Date.now()}`,
                key: 'Authorization',
                value: `Bearer ${apiProcessedConfig.token}`,
                description: 'OAuth2 token authentication',
                enabled: true,
                required: true
              }]);
            }
          }
        }
        // ============== END FIX ==============
        
        // Log body parameters specifically
        const bodyParams = details.parameters?.filter(p => 
          p.parameterLocation?.toLowerCase() === 'body'
        ) || [];
        console.log('📦 Body parameters found:', bodyParams.length, bodyParams);
        
        // IMPORTANT: Process body type and parameters from API details
        if (details.requestBody && details.requestBody.bodyType) {
          const bodyType = details.requestBody.bodyType;
          console.log(`📦 Setting body type from API details: ${bodyType}`);
          
          // Get all body parameters
          const bodyParams = details.parameters?.filter(p => 
            p.parameterLocation?.toLowerCase() === 'body'
          ) || [];
          
          console.log(`📦 Found ${bodyParams.length} body parameters for ${bodyType}`);
          
          if (bodyType === 'form-data') {
            setRequestBodyType('form-data');
            if (bodyParams.length > 0) {
              const formDataArray = bodyParams.map((param, index) => ({
                id: param.id || `form-${Date.now()}-${index}-${Math.random()}`,
                key: param.key || '',
                value: param.value || param.defaultValue || param.example || '',
                type: param.bodyFormat === 'file' ? 'file' : 'text',
                enabled: true,
                description: param.description || '',
                required: param.required || false
              }));
              setFormData(formDataArray);
            }
          } 
          else if (bodyType === 'x-www-form-urlencoded' || bodyType === 'urlencoded') {
            setRequestBodyType('x-www-form-urlencoded');
            if (bodyParams.length > 0) {
              const urlEncodedArray = bodyParams.map((param, index) => ({
                id: param.id || `url-${Date.now()}-${index}-${Math.random()}`,
                key: param.key || '',
                value: param.value || param.defaultValue || param.example || '',
                description: param.description || '',
                enabled: true,
                required: param.required || false
              }));
              setUrlEncodedData(urlEncodedArray);
            }
          } 
          else if (bodyType === 'json' || bodyType === 'raw') {
            setRequestBodyType('raw');
            setRawBodyType('json');
            
            if (bodyParams.length > 0) {
              const jsonObject = {};
              bodyParams.forEach(param => {
                if (param.key) {
                  let value = param.value || param.defaultValue || param.example || '';
                  if (param.type === 'integer' || param.type === 'number') {
                    const num = Number(value);
                    if (!isNaN(num)) value = num;
                  } else if (param.type === 'boolean') {
                    value = value === 'true' || value === true;
                  }
                  jsonObject[param.key] = value;
                }
              });
              
              if (details.requestBody.sample) {
                try {
                  const existingBody = JSON.parse(details.requestBody.sample);
                  setRequestBody(JSON.stringify({ ...existingBody, ...jsonObject }, null, 2));
                } catch {
                  setRequestBody(JSON.stringify(jsonObject, null, 2));
                }
              } else {
                setRequestBody(JSON.stringify(jsonObject, null, 2));
              }
            } else if (details.requestBody.sample) {
              setRequestBody(details.requestBody.sample);
            }
          }
        }
        
        if (details.method && details.method !== request.method) {
          setRequestMethod(details.method);
        }
        
        // Separate parameters based on location
        if (details.parameters) {
          const queryParams = [];
          const pathParams = [];
          const headerParams = []; // These are parameters with location='header'
          const bodyParams = [];
          
          console.log('📊 Processing parameters from API:', details.parameters.length);
          
          details.parameters.forEach(param => {
            if (param && param.key) {
              const paramObject = {
                id: param.id || `${param.key}-${Date.now()}-${Math.random()}`,
                key: param.key,
                value: param.value || '',
                description: param.description || '',
                enabled: param.enabled !== false,
                required: param.required || false,
                parameterLocation: param.parameterLocation || 'query',
                bodyFormat: param.bodyFormat || null,
                type: param.type || 'string',
                defaultValue: param.defaultValue || '',
                example: param.example || ''
              };
              
              console.log(`📍 [API] Parameter ${param.key}: location = ${param.parameterLocation}`);
              
              const location = (param.parameterLocation || '').toLowerCase();
              switch(location) {
                case 'query':
                  queryParams.push(paramObject);
                  break;
                case 'path':
                  pathParams.push(paramObject);
                  console.log(`🛣️ Added to PATH params: ${param.key}`);
                  break;
                case 'header':
                  headerParams.push(paramObject);
                  console.log(`📌 Added to HEADER params (will be added to headers): ${param.key}`);
                  break;
                case 'body':
                  bodyParams.push(paramObject);
                  console.log(`📦 Body parameter: ${param.key}`);
                  break;
                default:
                  if (initialTemplateUrl && initialTemplateUrl.includes(`{${param.key}}`)) {
                    pathParams.push(paramObject);
                    console.log(`🛣️ Defaulted to PATH param: ${param.key}`);
                  } else {
                    queryParams.push(paramObject);
                    console.log(`🔍 Defaulted to QUERY param: ${param.key}`);
                  }
              }
            }
          });
          
          console.log('📊 [Separated Parameters]', {
            query: queryParams.length,
            path: pathParams.length,
            header: headerParams.length,
            body: bodyParams.length
          });
          
          if (queryParams.length > 0) {
            console.log('📝 Setting query params:', queryParams);
            setRequestParams(queryParams);
          }
          
          if (pathParams.length > 0) {
            console.log('🛣️ Setting path params:', pathParams);
            
            // Add position information based on order
            const pathParamsWithPosition = pathParams.map((param, index) => ({
              ...param,
              position: index,
              id: param.id || `path-${Date.now()}-${index}-${Math.random()}`
            }));
            
            // CRITICAL FIX: Filter out any path params that have placeholder values
            const cleanedPathParams = pathParamsWithPosition.map(param => {
              // Check if the value contains any placeholder patterns
              const value = param.value || '';
              
              // If the value contains {, }, or %7B, %7D (encoded curly braces), treat it as empty
              const hasPlaceholder = value.includes('{') || value.includes('}') || 
                                    value.includes('%7B') || value.includes('%7D');
              
              return {
                ...param,
                // Only keep the value if it's a real value (not a placeholder)
                value: hasPlaceholder ? '' : value
              };
            });
            
            console.log('🧹 Cleaned path params with position:', cleanedPathParams);
            setRequestPathParams(cleanedPathParams);
            
            let newTemplateUrl = initialTemplateUrl;
            
            const hasPlaceholders = pathParams.some(p => 
              newTemplateUrl.includes(`{${p.key}}`) || newTemplateUrl.includes(`:${p.key}`)
            );
            
            if (!hasPlaceholders && pathParams.length > 0) {
              if (!newTemplateUrl.endsWith('/')) {
                newTemplateUrl += '/';
              }
              pathParams.forEach((param, index) => {
                if (index === 0) {
                  newTemplateUrl += `{${param.key}}`;
                } else {
                  newTemplateUrl += `/{${param.key}}`;
                }
              });
              console.log('🛣️ Added placeholders to template URL from API:', newTemplateUrl);
              setTemplateUrl(newTemplateUrl);
            }
            
            // Build the URL with path params, but don't encode them yet
            let updatedUrl = newTemplateUrl;
            
            // Create a map of placeholders to values - only include real values
            const paramValueMap = new Map();
            pathParams.forEach(param => {
              const value = param.value || '';
              const hasPlaceholder = value.includes('{') || value.includes('}') || 
                                    value.includes('%7B') || value.includes('%7D');
              
              // Only add to map if it's a real value (not a placeholder)
              if (param.key && value && value.trim() !== '' && !hasPlaceholder) {
                paramValueMap.set(param.key, value);
              }
            });
            
            // Replace placeholders with values, but ensure each placeholder is replaced only once
            const placeholders = Array.from(paramValueMap.keys()).sort((a, b) => b.length - a.length);
            
            placeholders.forEach(key => {
              const value = paramValueMap.get(key);
              const placeholder = `{${key}}`;
              const colonPlaceholder = `:${key}`;
              
              if (updatedUrl.includes(placeholder)) {
                const regex = new RegExp(placeholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g');
                updatedUrl = updatedUrl.replace(regex, value);
              }
              
              if (updatedUrl.includes(colonPlaceholder)) {
                const regex = new RegExp(colonPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g');
                updatedUrl = updatedUrl.replace(regex, value);
              }
            });
            
            const queryString = queryParams
              .filter(p => p.enabled && p.key && p.key.trim() !== '')
              .map(p => `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value || '')}`)
              .join('&');
            
            const finalUrl = queryString ? `${updatedUrl}?${queryString}` : updatedUrl;
            
            console.log('✅ Updated URL with API path params and query params:', finalUrl);
            setRequestUrl(finalUrl);
          }
          
          // ============== FIXED: Handle headers from API details ==============
          // Collect all headers to add
          const headersToAdd = [];
          
          // 1. Add header parameters (parameters with location='header')
          if (headerParams.length > 0) {
            console.log('📌 Adding header parameters to headers:', headerParams);
            
            headerParams.forEach(param => {
              headersToAdd.push({
                id: param.id || `header-${Date.now()}-${Math.random()}`,
                key: param.key,
                value: param.value || '',
                description: param.description || '',
                enabled: param.enabled !== false,
                required: param.required || false
              });
            });
          }
          
          // 2. Add regular headers from details
          if (details.headers && details.headers.length > 0) {
            console.log('📌 Adding regular headers from details:', details.headers);
            
            details.headers.forEach((header, idx) => {
              // Skip if it's an auth header (already handled)
              const key = header.key?.toLowerCase() || '';
              if (key.includes('authorization') || key.includes('api-key') || key.includes('api-secret')) {
                console.log(`⏭️ Skipping auth header: ${header.key}`);
                return;
              }
              
              headersToAdd.push({
                id: header.id || `header-${Date.now()}-${idx}-${Math.random()}`,
                key: header.key,
                value: header.value || '',
                description: header.description || '',
                enabled: header.enabled !== false,
                required: header.required || false
              });
            });
          }
          
          // Remove duplicates (by key) - keep the first occurrence
          const uniqueHeaders = headersToAdd.reduce((acc, current) => {
            const exists = acc.some(h => h.key.toLowerCase() === current.key.toLowerCase());
            if (!exists) {
              acc.push(current);
            }
            return acc;
          }, []);
          
          // Merge with existing headers (auth headers might already be set)
          if (uniqueHeaders.length > 0) {
            setRequestHeaders(prevHeaders => {
              const currentHeaders = Array.isArray(prevHeaders) ? prevHeaders : [];
              
              // Combine and remove duplicates
              const allHeaders = [...currentHeaders, ...uniqueHeaders];
              const finalHeaders = allHeaders.reduce((acc, current) => {
                const exists = acc.some(h => h.key.toLowerCase() === current.key.toLowerCase());
                if (!exists) {
                  acc.push(current);
                }
                return acc;
              }, []);
              
              console.log('📌 Final headers after merge:', finalHeaders);
              return finalHeaders;
            });
          }
          // ============== END FIX ==============
          
          if (bodyParams.length > 0) {
            console.log('📦 Body parameters found:', bodyParams);
            
            if (details.requestBody && details.requestBody.bodyType === 'xml') {
              setRequestBodyType('xml');
              
              if (bodyParams.length > 0) {
                const xmlBuilder = ['<?xml version="1.0" encoding="UTF-8"?>', '<request>'];
                bodyParams.forEach(param => {
                  if (param.key) {
                    xmlBuilder.push(`  <${param.key}>${param.value || param.defaultValue || ''}</${param.key}>`);
                  }
                });
                xmlBuilder.push('</request>');
                setRequestBody(xmlBuilder.join('\n'));
              } else if (details.requestBody.sample) {
                setRequestBody(details.requestBody.sample);
              }
            }
            else if (details.requestBody && details.requestBody.bodyType === 'json') {
              setRequestBodyType('raw');
              setRawBodyType('json');
              
              if (bodyParams.length > 0) {
                const jsonObject = {};
                bodyParams.forEach(param => {
                  if (param.key) {
                    let value = param.value || param.defaultValue || param.example || '';
                    if (param.type === 'integer' || param.type === 'number') {
                      const num = Number(value);
                      if (!isNaN(num)) value = num;
                    } else if (param.type === 'boolean') {
                      value = value === 'true' || value === true;
                    }
                    jsonObject[param.key] = value;
                  }
                });
                
                if (Object.keys(jsonObject).length > 0) {
                  setRequestBody(JSON.stringify(jsonObject, null, 2));
                } else if (details.requestBody.sample) {
                  setRequestBody(details.requestBody.sample);
                }
              }
            }
            else if (details.requestBody && details.requestBody.bodyType === 'form-data') {
              setRequestBodyType('form-data');
              if (bodyParams.length > 0) {
                const formDataArray = bodyParams.map((param, index) => ({
                  id: param.id || `form-${Date.now()}-${index}-${Math.random()}`,
                  key: param.key || '',
                  value: param.value || param.defaultValue || param.example || '',
                  type: param.bodyFormat === 'file' ? 'file' : 'text',
                  enabled: true,
                  description: param.description || '',
                  required: param.required || false
                }));
                setFormData(formDataArray);
              }
            }
            else if (details.requestBody && details.requestBody.bodyType === 'x-www-form-urlencoded') {
              setRequestBodyType('x-www-form-urlencoded');
              if (bodyParams.length > 0) {
                const urlEncodedArray = bodyParams.map((param, index) => ({
                  id: param.id || `url-${Date.now()}-${index}-${Math.random()}`,
                  key: param.key || '',
                  value: param.value || param.defaultValue || param.example || '',
                  description: param.description || '',
                  enabled: true,
                  required: param.required || false
                }));
                setUrlEncodedData(urlEncodedArray);
              }
            }
          }
        }
        
        // Determine which tab should be active
        const newActiveTab = determineActiveTab();
        if (newActiveTab !== activeTab) {
          setActiveTab(newActiveTab);
        }
      }
    } catch (apiError) {
      console.error('Error fetching request details from API:', apiError);
    } finally {
      if (isMounted.current) {
        setLoading(prev => ({ ...prev, request: false }));
      }
    }
  }
}, [authToken, determineActiveTab, requestUrl, authType, authConfig, activeTab, requestBodyType, formData.length, urlEncodedData.length]);


// Update the addNewRequest function to properly create a new request
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
    authType: 'noauth',
    authConfig: { type: 'noauth' },
    params: [],
    headers: [],
    body: '', // Ensure this is empty string, not '{\n  \n}'
    tests: '',
    preRequestScript: '',
    isSaved: false,
    collectionId,
    folderId
  };
  
  setCollections(collections.map(col => 
    col.id === collectionId ? {
      ...col,
      folders: (col.folders || []).map(folder =>
        folder.id === folderId ? { 
          ...folder, 
          requests: [...(folder.requests || []), newRequest] 
        } : folder
      )
    } : col
  ));
  
  // Pass the new request to handleSelectRequest - it will detect it's new and reset everything
  handleSelectRequest(newRequest, collectionId, folderId);
  showToast('Request added', 'success');
};


  const transformEnvironmentsData = (apiData) => {
  console.log('🔄 [Transform Environments] Input:', apiData);
  
  if (!apiData) {
    console.warn('⚠️ [Transform Environments] No data provided');
    return [];
  }
  
  // Handle the nested data structure
  let environmentsArray = [];
  
  if (apiData.data && apiData.data.environments && Array.isArray(apiData.data.environments)) {
    environmentsArray = apiData.data.environments;
  } else if (apiData.environments && Array.isArray(apiData.environments)) {
    environmentsArray = apiData.environments;
  } else if (Array.isArray(apiData)) {
    environmentsArray = apiData;
  } else {
    console.warn('⚠️ [Transform Environments] Unknown data structure:', apiData);
    return [];
  }
  
  console.log(`📊 [Transform Environments] Processing ${environmentsArray.length} environments`);
  
  return environmentsArray.map(env => ({
    id: env.id || `env-${Date.now()}`,
    name: env.name || 'Environment',
    isActive: env.active || false,
    variables: (env.variables || []).map(v => ({
      id: v.id || `var-${Date.now()}`,
      key: v.key,
      value: v.value,
      type: v.type || 'text',
      enabled: v.enabled !== false
    }))
  }));
};


const cleanHeaders = (headers) => {
  if (!Array.isArray(headers)) return [];
  
  return headers.filter(h => {
    // Skip if no key
    if (!h || !h.key) return false;
    
    const key = h.key.toLowerCase();
    
    // Skip if it looks like a query parameter
    if (key.includes('?') || 
        key.includes('=') || 
        key.startsWith('param') ||
        key === 'query' ||
        key === 'parameter') {
      console.log('🧹 Filtering out param-looking header:', h.key);
      return false;
    }
    
    return true;
  });
};

  const fetchCollections = useCallback(async () => {
    console.log('🔥 [Collections] fetchCollections called');
    
    if (!authToken) {
      console.log('❌ No auth token available');
      setError('Authentication required. Please login.');
      setLoading(prev => ({ ...prev, initialLoad: false, collections: false }));
      return;
    }

    // Prevent concurrent fetches
    if (fetchInProgressRef.current) {
      console.log('⏭️ Fetch already in progress, skipping');
      return;
    }

    // Check if component is still mounted
    if (!isMounted.current) {
      console.log('Component unmounted, aborting fetch');
      return;
    }

    fetchInProgressRef.current = true;
    setLoading(prev => ({ ...prev, initialLoad: true, collections: true }));
    setError(null);
    console.log('📡 [Collections] Fetching from API...');

    try {
      const response = await getCollectionsList(authToken);
      
      // Check if component is still mounted
      if (!isMounted.current) return;
      
      console.log('📦 [Collections] API response:', response);
      
      if (!response) {
        throw new Error('No response from server');
      }
      
      const processedResponse = handleCollectionsResponse(response);
      const collectionsData = extractCollectionsList(processedResponse);
      
      const basicCollections = transformCollectionsData(collectionsData);
      
      console.log('🔄 [Collections] Fetching details for each collection...');
      const collectionsWithDetails = await Promise.all(
        basicCollections.map(async (collection) => {
          try {
            const detailsResponse = await getCollectionDetails(authToken, collection.id);
            
            // Check if component is still mounted
            if (!isMounted.current) return collection;
            
            const processedDetails = handleCollectionsResponse(detailsResponse);
            const collectionDetails = extractCollectionDetails(processedDetails);
            
            console.log(`📁 [Collections] Got details for ${collection.name}:`, 
              collectionDetails?.folders?.length || 0, 'folders');
            
            return {
              ...collection,
              folders: collectionDetails?.folders || collection.folders,
              requestsCount: collectionDetails?.totalRequests || collection.requestsCount || 0,
              folderCount: collectionDetails?.totalFolders || collection.folderCount || 0
            };
            
          } catch (error) {
            console.error(`❌ [Collections] Error fetching details for collection ${collection.id}:`, error);
            return collection;
          }
        })
      );

      // Check if component is still mounted
      if (!isMounted.current) return;

      console.log('📊 [Collections] Final collections with details:', collectionsWithDetails);
      
      if (!collectionsWithDetails || collectionsWithDetails.length === 0) {
        console.warn('⚠️ [Collections] No collections after fetching details');
        setCollections([]);
        setError('No collections found');
      } else {
        setCollections(collectionsWithDetails);
        
        // Auto-select first request if available
        if (collectionsWithDetails.length > 0 && !selectedRequest) {
          const firstCollection = collectionsWithDetails[0];
          setCollections(prev => prev.map((col, idx) => 
            idx === 0 ? { ...col, isExpanded: true } : col
          ));
          
          if (firstCollection.folders && firstCollection.folders.length > 0) {
            const firstFolder = firstCollection.folders[0];
            setCollections(prev => prev.map(col => 
              col.id === firstCollection.id ? {
                ...col,
                folders: col.folders.map((folder, idx) => 
                  idx === 0 ? { ...folder, isExpanded: true } : folder
                )
              } : col
            ));
            
            if (firstFolder.requests && firstFolder.requests.length > 0) {
              const firstRequest = firstFolder.requests[0];
              console.log('🎯 [Collections] Auto-selecting first request:', firstRequest.name);
              handleSelectRequest(firstRequest, firstCollection.id, firstFolder.id);
            }
          }
        }
        
        console.log('✅ [Collections] Successfully loaded', collectionsWithDetails.length, 'collections');
      }

    } catch (error) {
      console.error('❌ [Collections] Error fetching collections:', error);
      if (isMounted.current) {
        setError(`Failed to load collections: ${error.message}`);
        setCollections([]);
        showToast(`Error loading collections: ${error.message}`, 'error');
      }
    } finally {
      if (isMounted.current) {
        setLoading(prev => ({ ...prev, initialLoad: false, collections: false }));
      }
      fetchInProgressRef.current = false;
      console.log('🏁 [Collections] fetchCollections completed');
    }
  }, [authToken, selectedRequest, handleSelectRequest]);

  const fetchEnvironments = useCallback(async () => {
  console.log('🔥 [Environments] fetchEnvironments called');
  
  if (!authToken) {
    console.log('❌ No auth token available');
    setEnvironments([]);
    setActiveEnvironment(null);
    return;
  }

  // Check if component is still mounted
  if (!isMounted.current) {
    console.log('Component unmounted, aborting fetch');
    return;
  }

  setLoading(prev => ({ ...prev, environments: true }));
  console.log('📡 [Environments] Fetching from API...');

  try {
    const response = await getEnvironments(authToken);
    
    // Check if component is still mounted
    if (!isMounted.current) return;
    
    console.log('📦 [Environments] Raw API response:', response);
    
    const processedResponse = handleCollectionsResponse(response);
    console.log('🔄 [Environments] Processed response:', processedResponse);
    
    const environmentsData = extractEnvironments(processedResponse);
    console.log('📊 [Environments] Extracted data:', environmentsData);
    
    const transformedEnvs = transformEnvironmentsData(environmentsData);
    console.log('✅ [Environments] Transformed environments:', transformedEnvs);
    
    setEnvironments(transformedEnvs);
    
    // Set active environment
    if (transformedEnvs.length > 0) {
      const activeEnv = transformedEnvs.find(e => e.isActive);
      if (activeEnv) {
        console.log('🎯 [Environments] Setting active environment:', activeEnv.name);
        setActiveEnvironment(activeEnv.id);
      } else {
        // Default to first environment if none is marked active
        console.log('🎯 [Environments] No active environment found, defaulting to first');
        setActiveEnvironment(transformedEnvs[0].id);
        // Update the local state to reflect this
        setEnvironments(envs => envs.map((e, idx) => ({ 
          ...e, 
          isActive: idx === 0 
        })));
      }
    } else {
      setActiveEnvironment(null);
    }
    
  } catch (error) {
    console.error('❌ [Environments] Error fetching environments:', error);
    if (isMounted.current) {
      setEnvironments([]);
      setActiveEnvironment(null);
      showToast(`Failed to load environments: ${error.message}`, 'error');
    }
  } finally {
    if (isMounted.current) {
      setLoading(prev => ({ ...prev, environments: false }));
    }
    console.log('🏁 [Environments] fetchEnvironments completed');
  }
}, [authToken]);



// Utility function to intelligently separate params and headers
const separateParamsAndHeaders = (items) => {
  if (!Array.isArray(items)) return { params: [], headers: [] };
  
  const params = [];
  const headers = [];
  
  // Common patterns
  const queryParamPatterns = [
    /^[?&]/,
    /=/,
    /^(page|limit|offset|sort|order|filter|search|q|id|userId|_id)$/i,
    /^[a-z]+_[a-z]+$/i  // snake_case often indicates params
  ];
  
  const headerPatterns = [
    /^content-/i,
    /^accept/i,
    /^authorization/i,
    /^x-/i,
    /^cookie/i,
    /^user-agent/i,
    /^host$/i,
    /^origin$/i,
    /^referer$/i
  ];
  
  items.forEach(item => {
    if (!item || !item.key) return;
    
    const key = item.key;
    const value = item.value || '';
    const desc = (item.description || '').toLowerCase();
    
    // Check if it's clearly a query parameter
    let isParam = queryParamPatterns.some(pattern => 
      pattern.test(key) || pattern.test(value) || desc.includes('query') || desc.includes('parameter')
    );
    
    // Check if it's clearly a header
    let isHeader = headerPatterns.some(pattern => 
      pattern.test(key) || desc.includes('header')
    );
    
    // If both are true, use description as tiebreaker
    if (isParam && isHeader) {
      if (desc.includes('query') || desc.includes('parameter')) {
        isHeader = false;
      } else if (desc.includes('header')) {
        isParam = false;
      }
    }
    
    if (isParam) {
      params.push(item);
      console.log('📌 Param:', key);
    } else if (isHeader) {
      headers.push(item);
      console.log('📌 Header:', key);
    } else {
      // Default: if it has a value that looks like a URL parameter, put in params
      if (value.includes('?') || value.includes('=') || key.match(/^[a-z_]+$/i)) {
        params.push(item);
        console.log('📌 Default param:', key);
      } else {
        headers.push(item);
        console.log('📌 Default header:', key);
      }
    }
  });
  
  return { params, headers };
};



  const handleSaveRequest = useCallback(async () => {
    if (!selectedRequest) {
      showToast('No request selected', 'error');
      return;
    }

    const validationErrors = validateSaveRequest({
      collectionId: selectedRequest.collectionId,
      name: selectedRequest.name || 'New Request',
      method: requestMethod,
      url: requestUrl
    });

    if (validationErrors.length > 0) {
      showToast(validationErrors[0], 'error');
      return;
    }

    setLoading(prev => ({ ...prev, save: true }));

    try {
      // Build request DTO matching backend SaveRequestDTO structure
      const saveRequestData = {
        collectionId: selectedRequest.collectionId,
        requestId: selectedRequest.id,
        folderId: selectedRequest.folderId,
        request: {
          id: selectedRequest.id,
          name: selectedRequest.name,
          method: requestMethod,
          url: requestUrl,
          description: selectedRequest.description || '',
          headers: requestHeaders.filter(h => h.enabled).map(h => ({
            key: h.key,
            value: h.value,
            description: h.description || '',
            enabled: h.enabled
          })),
          params: requestParams.filter(p => p.enabled).map(p => ({
            key: p.key,
            value: p.value,
            description: p.description || '',
            enabled: p.enabled
          })),
          body: requestBody,
          auth: {
            type: authType,
            token: authConfig.token || '',
            tokenType: authConfig.tokenType || 'Bearer',
            username: authConfig.username || '',
            password: authConfig.password || '',
            key: authConfig.key || '',
            value: authConfig.value || '',
            addTo: authConfig.addTo || 'header'
          },
          tests: selectedRequest.tests || '',
          preRequestScript: selectedRequest.preRequestScript || '',
          saved: true,
          collectionId: selectedRequest.collectionId,
          folderId: selectedRequest.folderId
        }
      };

      if (authToken) {
        const response = await saveRequest(authToken, saveRequestData);
        const processedResponse = handleCollectionsResponse(response);
        const saveResults = extractSaveRequestResults(processedResponse);
        
        if (saveResults && saveResults.success) {
          showToast('Request saved successfully', 'success');
        } else {
          showToast('Failed to save request', 'error');
        }
      }

      // Update local state
      setCollections(prev => prev.map(col => 
        col.id === selectedRequest.collectionId ? {
          ...col,
          folders: col.folders.map(folder =>
            folder.id === selectedRequest.folderId ? {
              ...folder,
              requests: folder.requests.map(req =>
                req.id === selectedRequest.id ? { 
                  ...req, 
                  method: requestMethod,
                  url: requestUrl,
                  headers: requestHeaders,
                  params: requestParams,
                  body: requestBody,
                  authType: authType,
                  authConfig: authConfig,
                  lastModified: new Date().toISOString(),
                  isSaved: true 
                } : req
              )
            } : folder
          )
        } : col
      ));

      setSelectedRequest(prev => ({ 
        ...prev, 
        method: requestMethod,
        url: requestUrl,
        headers: requestHeaders,
        params: requestParams,
        body: requestBody,
        authType: authType,
        authConfig: authConfig,
        lastModified: new Date().toISOString(),
        isSaved: true 
      }));

    } catch (error) {
      console.error('Error saving request:', error);
      showToast(`Failed to save request: ${error.message}`, 'error');
    } finally {
      setLoading(prev => ({ ...prev, save: false }));
    }
  }, [authToken, selectedRequest, requestMethod, requestUrl, requestHeaders, requestParams, requestBody, authType, authConfig]);

  const handleCreateCollection = useCallback(async (name, description = '') => {
    const validationErrors = validateCreateCollection({ name });
    if (validationErrors.length > 0) {
      showToast(validationErrors[0], 'error');
      return null;
    }

    setLoading(prev => ({ ...prev, create: true }));

    try {
      let createResults = null;
      
      if (authToken) {
        // Match backend CreateCollectionDTO structure
        const collectionData = {
          name,
          description,
          variables: [],
          visibility: 'PRIVATE'
        };

        const response = await createCollection(authToken, collectionData);
        const processedResponse = handleCollectionsResponse(response);
        createResults = extractCreateCollectionResults(processedResponse);
      }

      const newCollection = {
        id: createResults?.collectionId || `col-${Date.now()}`,
        name: createResults?.name || name,
        description: description,
        isExpanded: true,
        isFavorite: false,
        isEditing: false,
        createdAt: new Date().toISOString(),
        requestsCount: 0,
        folderCount: 0,
        variables: [],
        folders: []
      };
      
      setCollections(prev => [...prev, newCollection]);
      showToast('Collection created successfully', 'success');

      return newCollection;

    } catch (error) {
      console.error('Error creating collection:', error);
      showToast(`Failed to create collection: ${error.message}`, 'error');
      throw error;
    } finally {
      setLoading(prev => ({ ...prev, create: false }));
    }
  }, [authToken]);

  const handleGenerateCodeSnippet = useCallback(async () => {
    const validationErrors = validateCodeSnippetRequest({
      language: selectedLanguage,
      method: requestMethod,
      url: requestUrl
    });

    if (validationErrors.length > 0) {
      showToast(validationErrors[0], 'error');
      return null;
    }

    setLoading(prev => ({ ...prev, generateSnippet: true }));

    try {
      if (!authToken) {
        showToast('Authentication required', 'error');
        return null;
      }
      
      // Match backend CodeSnippetRequestDTO structure
      const snippetRequest = {
        language: selectedLanguage,
        method: requestMethod,
        url: requestUrl,
        headers: requestHeaders && requestHeaders.length > 0 
          ? requestHeaders.filter(h => h && h.enabled).map(h => ({
              key: h?.key || '',
              value: h?.value || '',
              enabled: true
            }))
          : [],  // Provide empty array as fallback
        body: requestBody || ''
      };

      const response = await generateCodeSnippet(authToken, snippetRequest);
      const processedResponse = handleCollectionsResponse(response);
      const snippetResults = extractCodeSnippetResults(processedResponse);
      
      return snippetResults;

    } catch (error) {
      console.error('Error generating code snippet:', error);
      showToast(`Failed to generate code snippet: ${error.message}`, 'error');
      return null;
    } finally {
      setLoading(prev => ({ ...prev, generateSnippet: false }));
    }
  }, [authToken, selectedLanguage, requestMethod, requestUrl, requestHeaders, requestBody]);

  const handleImportCollection = useCallback(async (importData, importType = 'POSTMAN') => {
    const validationErrors = validateImportRequest({
      source: 'file',
      format: importType,
      data: importData
    });

    if (validationErrors.length > 0) {
      showToast(validationErrors[0], 'error');
      return null;
    }

    setLoading(prev => ({ ...prev, import: true }));

    try {
      let importResults = null;
      
      if (authToken) {
        // Match backend ImportRequestDTO structure
        const importRequest = {
          source: 'file',
          format: importType,
          data: importData
        };

        const response = await importCollection(authToken, importRequest);
        const processedResponse = handleCollectionsResponse(response);
        importResults = extractImportResults(processedResponse);
      }

      await fetchCollections();
      showToast('Collection imported successfully', 'success');

      return importResults;

    } catch (error) {
      console.error('Error importing collection:', error);
      showToast(`Failed to import: ${error.message}`, 'error');
      throw error;
    } finally {
      setLoading(prev => ({ ...prev, import: false }));
    }
  }, [authToken, fetchCollections]);


  // Initialize data - with check for authToken changes
  useEffect(() => {
    console.log('🚀 [Collections] useEffect triggered with authToken');
    
    // Check if authToken actually changed or if this is first mount
    const tokenChanged = prevAuthTokenRef.current !== authToken;
    const shouldFetch = !initialDataLoaded.current || (authToken && tokenChanged);
    
    console.log('📊 Auth token check:', {
      prevToken: prevAuthTokenRef.current ? 'present' : 'missing',
      currentToken: authToken ? 'present' : 'missing',
      tokenChanged,
      initialDataLoaded: initialDataLoaded.current,
      shouldFetch
    });
    
    if (authToken && shouldFetch) {
      console.log('📡 Fetching data due to:', tokenChanged ? 'token change' : 'initial load');
      
      fetchCollections().catch(error => {
        console.error('Error in fetchCollections:', error);
      });
      fetchEnvironments();
      
      initialDataLoaded.current = true;
    } else if (!authToken) {
      console.log('🔒 No auth token, skipping fetch');
      // Clear data when logged out
      setCollections([]);
      setEnvironments([]);
      setActiveEnvironment(null);
      setSelectedRequest(null);
      setRequestTabs([]);
    } else {
      console.log('⏭️ Skipping fetch - data already loaded with same token');
    }
    
    // Update ref
    prevAuthTokenRef.current = authToken;
    
  }, [authToken, fetchCollections, fetchEnvironments]);

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
    showToast(collection?.isFavorite ? 'Removed from favorites' : 'Added to favorites', 'success');
  };

  // Filter collections based on search
  const filteredCollections = collections.filter(collection => {
    if (!searchQuery) return true;
    
    const query = searchQuery.toLowerCase();
    return (
      collection.name.toLowerCase().includes(query) ||
      collection.description?.toLowerCase().includes(query) ||
      collection.folders?.some(folder => 
        folder.name.toLowerCase().includes(query) ||
        folder.description?.toLowerCase().includes(query) ||
        folder.requests?.some(request => 
          request.name.toLowerCase().includes(query) ||
          request.description?.toLowerCase().includes(query) ||
          request.url.toLowerCase().includes(query)
        )
      )
    );
  });

  // Add new collection
  const addNewCollection = async (name, description = '') => {
    try {
      await handleCreateCollection(name, description);
      setShowCreateModal(false);
      setNewCollectionName('');
      setNewCollectionDescription('');
    } catch (error) {
      console.error('Error adding collection:', error);
    }
  };

  // Add new folder
  const addNewFolder = (collectionId) => {
    const newFolder = {
      id: `folder-${Date.now()}`,
      name: 'New Folder',
      description: '',
      isExpanded: true,
      isEditing: false,
      requestCount: 0,
      requests: []
    };
    
    setCollections(collections.map(col => 
      col.id === collectionId ? { 
        ...col, 
        folders: [...(col.folders || []), newFolder] 
      } : col
    ));
    
    showToast('Folder added', 'success');
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
        folders: (col.folders || []).map(folder =>
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
        folders: (col.folders || []).map(folder =>
          folder.id === folderId ? {
            ...folder,
            requests: (folder.requests || []).map(req =>
              req.id === requestId ? { ...req, name: newName, isEditing: false } : req
            )
          } : folder
        )
      } : col
    ));
    
    setRequestTabs(tabs => tabs.map(tab =>
      tab.id === requestId ? { ...tab, name: newName } : tab
    ));
    
    showToast('Request name updated', 'success');
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

  // ==================== LOADING OVERLAY ====================
  // Loading Overlay Component - Matches UserManagement pattern
  const LoadingOverlay = () => {
    // Check if any loading state is active
    const isLoading = loading.initialLoad || 
                     loading.collections || 
                     loading.environments || 
                     loading.request || 
                     loading.execute || 
                     loading.save || 
                     loading.create || 
                     loading.import || 
                     loading.generateSnippet;
    
    // Determine loading message based on what's loading
    const getLoadingMessage = () => {
      if (loading.initialLoad) return 'Initializing Collections...';
      if (loading.collections) return 'Loading collections...';
      if (loading.environments) return 'Loading environments...';
      if (loading.request) return 'Loading request details...';
      if (loading.execute) return 'Executing request...';
      if (loading.save) return 'Saving request...';
      if (loading.create) return 'Creating collection...';
      if (loading.import) return 'Importing collection...';
      if (loading.generateSnippet) return 'Generating code snippet...';
      return 'Please wait while we prepare your content';
    };

    // Determine loading tips based on context
    const getLoadingTip = () => {
      if (loading.collections) {
        return `Loading ${collections.length || ''} collections and their requests...`;
      }
      if (loading.environments) {
        return `Loading ${environments.length || ''} environments...`;
      }
      if (loading.execute) {
        return 'Sending request to server...';
      }
      if (loading.save) {
        return 'Saving your changes...';
      }
      if (loading.import) {
        return 'Processing import file...';
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
              <Layers size={32} style={{ color: colors.primary, opacity: 0.3 }} />
            </div>
          </div>
          <h3 className="text-xl font-semibold mb-2" style={{ color: colors.text }}>
            API Collections
          </h3>
          <p className="text-sm mb-2" style={{ color: colors.textSecondary }}>
            {getLoadingMessage()}
          </p>
          <p className="text-xs mb-1" style={{ color: colors.textTertiary }}>
            {getLoadingTip()}
          </p>
          {/* <p className="text-xs" style={{ color: colors.textTertiary }}>
            This won't take long
          </p> */}
        </div>
      </div>
    );
  };

  // ==================== UI COMPONENTS ====================

  // Render Authorization Tab
const renderAuthTab = () => {
  const authTypes = [
    { id: 'noauth', name: 'No Auth', icon: <Globe size={16} />, description: 'No authorization required' },
    { id: 'bearer', name: 'Bearer Token', icon: <Key size={16} />, description: 'Token-based authentication' },
    { id: 'basic', name: 'Basic Auth', icon: <Shield size={16} />, description: 'Username and password' },
    { id: 'apikey', name: 'API Key', icon: <Key size={16} />, description: 'API key authentication' },
    { id: 'oauth2', name: 'OAuth 2.0', icon: <ShieldCheck size={16} />, description: 'OAuth 2.0 protocol' }
  ];

  // Check if the request has API Key auth config (even though authType is 'noauth')
  const hasApiKeyConfig = authConfig && authConfig.type === 'apikey';
  
  // Determine what to display in the type dropdown
  let displayAuthType = authType;
  
  // If we have API Key config but authType is 'noauth', still show the type as 'apikey' in the dropdown
  // but we'll handle it differently in the UI
  if (hasApiKeyConfig) {
    displayAuthType = 'apikey';
  }
  
  const currentAuthType = authTypes.find(type => type.id === displayAuthType);

  return (
    <div className="p-4 h-full overflow-auto">
      <div className="mb-4">
        <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
          Type
        </label>
        <div className="relative">
          <button
            type="button"
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
            <>
              <div className="fixed inset-0 z-40" onClick={() => setShowAuthDropdown(false)} />
              <div className="absolute left-0 right-0 top-full mt-1 py-2 rounded shadow-lg z-50 border"
                style={{ 
                  backgroundColor: colors.bg,
                  borderColor: colors.border,
                  maxHeight: '300px',
                  overflowY: 'auto'
                }}>
                {authTypes.map(type => (
                  <button
                    key={type.id}
                    type="button"
                    onClick={() => {
                      // Special handling for API Key - we want to set authType to 'noauth' for UI
                      // but store the config with type 'apikey'
                      if (type.id === 'apikey') {
                        // Set authType to 'noauth' for UI display
                        setAuthType('noauth');
                        // Initialize API Key config
                        setAuthConfig({
                          type: 'apikey',
                          key: '',
                          value: '',
                          addTo: 'header'
                        });
                        // Show a toast to guide the user
                        showToast('API Key will be added to Headers tab', 'info');
                      } else {
                        // For other auth types, set normally
                        setAuthType(type.id);
                        setAuthConfig({ ...authConfig, type: type.id });
                      }
                      setShowAuthDropdown(false);
                    }}
                    className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors hover-lift"
                    style={{ 
                      backgroundColor: displayAuthType === type.id ? colors.selected : 'transparent',
                      color: displayAuthType === type.id ? colors.primary : colors.text
                    }}
                  >
                    {type.icon}
                    {type.name}
                    {displayAuthType === type.id && <Check size={14} className="ml-auto" />}
                  </button>
                ))}
              </div>
            </>
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
        {/* If we have API Key config but authType is 'noauth', show a message */}
        {hasApiKeyConfig && (
          <div className="text-center py-8">
            <Key size={48} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mx-auto mb-4" />
            <h3 className="text-sm font-semibold mb-2" style={{ color: colors.text }}>API Key Authentication</h3>
            <p className="text-sm max-w-sm mx-auto mb-4" style={{ color: colors.textSecondary }}>
              API Key is configured in the Headers tab.
            </p>
            <div className="flex justify-center gap-2">
              <button
                type="button"
                onClick={() => setActiveTab('headers')}
                className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                Go to Headers
              </button>
              <button
                type="button"
                onClick={() => {
                  // Switch to API Key editing mode
                  setAuthType('apikey');
                }}
                className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                Edit API Key
              </button>
            </div>
          </div>
        )}
        
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
                  backgroundColor: colors.bg,
                  borderColor: colors.border,
                  color: colors.text
                }}>
                <option value="header">Header</option>
                <option value="queryParams">Query Params</option>
              </select>
            </div>
            <div className="flex justify-end">
              <button
                type="button"
                onClick={() => {
                  // When done editing API Key, switch back to 'noauth' for UI
                  // but keep the config
                  setAuthType('noauth');
                  showToast('API Key will be added to Headers tab', 'success');
                }}
                className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                Done
              </button>
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
          </div>
        )}

        {authType === 'noauth' && !hasApiKeyConfig && (
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


  const renderPathParamsTab = () => {
  const hasParams = requestPathParams.length > 0;
  
  return (
    <div className="flex flex-col h-full">
      <div className="flex justify-between items-center p-4">
        <div className="flex items-center gap-4">
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Path Parameters</h3>
          {hasParams && (
            <div className="flex items-center gap-2">
              <button type="button" className="text-xs px-2 py-1 rounded hover-lift" style={{ 
                backgroundColor: colors.hover,
                color: colors.textSecondary
              }}
              onClick={() => {
                const text = requestPathParams.map(p => `${p.key}=${p.value}`).join('\n');
                navigator.clipboard.writeText(text);
                showToast('Parameters copied as text', 'success');
              }}>
                Bulk Edit
              </button>
            </div>
          )}
        </div>
        <button type="button" onClick={addPathParam} className="px-3 py-1.5 rounded text-sm font-medium flex items-center gap-2 hover:opacity-90 transition-colors hover-lift"
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
                        checked={requestPathParams.every(p => p.enabled)}
                        onChange={() => {
                          const allEnabled = requestPathParams.every(p => p.enabled);
                          setRequestPathParams(requestPathParams.map(p => ({ ...p, enabled: !allEnabled })));
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
                {requestPathParams.map(param => (
                  <tr key={param.id} className="hover:bg-opacity-50 transition-colors hover-lift" 
                    style={{ backgroundColor: colors.tableRow }}>
                    <td className="p-0">
                      <div className="p-3">
                        <input
                          type="checkbox"
                          checked={param.enabled}
                          onChange={() => updatePathParam(param.id, 'enabled', !param.enabled)}
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
                        onChange={(e) => updatePathParam(param.id, 'key', e.target.value)}
                        className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                        style={{ 
                          borderColor: colors.border,
                          color: colors.text,
                          backgroundColor: colors.inputBg
                        }}
                        placeholder="Key"
                        // Remove the readOnly attribute
                      />
                    </td>
                    <td className="p-3">
                      <input
                        type="text"
                        value={param.value}
                        onChange={(e) => updatePathParam(param.id, 'value', e.target.value)}
                        className="w-full px-2 py-1.5 border rounded-sm text-sm focus:outline-none hover-lift"
                        style={{ 
                          borderColor: colors.border,
                          color: colors.text,
                          backgroundColor: colors.inputBg
                        }}
                        placeholder="Enter value"
                      />
                    </td>
                    <td className="p-3">
                      <input
                        type="text"
                        value={param.description || ''}
                        onChange={(e) => updatePathParam(param.id, 'description', e.target.value)}
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
                        type="button"
                        onClick={() => deletePathParam(param.id)}
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
            <Link size={48} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mb-4" />
            <h3 className="text-sm font-semibold mb-2" style={{ color: colors.text }}>No Path Parameters</h3>
            <p className="text-sm mb-4 max-w-sm" style={{ color: colors.textSecondary }}>
              Path parameters are variable parts of the URL path, denoted by curly braces &#123;&#125; or colon : prefix.
            </p>
            <button
              type="button"
              onClick={addPathParam}
              className="px-3 py-1.5 text-sm font-medium rounded flex items-center gap-2 hover:opacity-90 transition-colors hover-lift"
              style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
              <Plus size={13} />
              Add Path Parameter
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

  // Render Query Params Tab - Updated version
const renderQueryParamsTab = () => {
  const hasParams = requestParams.length > 0;
  
  return (
    <div className="flex flex-col h-full">
      <div className="flex justify-between items-center p-4">
        <div className="flex items-center gap-4">
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Query Parameters</h3>
          {hasParams && (
            <div className="flex items-center gap-2">
              <button type="button" className="text-xs px-2 py-1 rounded hover-lift" style={{ 
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
        <button type="button" onClick={addQueryParam} className="px-3 py-1.5 rounded text-sm font-medium flex items-center gap-2 hover:opacity-90 transition-colors hover-lift"
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
                          const updatedParams = requestParams.map(p => ({ ...p, enabled: !allEnabled }));
                          setRequestParams(updatedParams);
                          
                          // Update URL after toggling all
                          const baseUrl = requestUrl.split('?')[0];
                          const queryParams = updatedParams
                            .filter(p => p.enabled && p.key && p.key.trim() !== '')
                            .map(p => `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value || '')}`)
                            .join('&');
                          
                          const newUrl = queryParams ? `${baseUrl}?${queryParams}` : baseUrl;
                          setRequestUrl(newUrl);
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
                          onChange={() => updateQueryParam(param.id, 'enabled', !param.enabled)}
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
                        onChange={(e) => updateQueryParam(param.id, 'key', e.target.value)}
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
                        onChange={(e) => updateQueryParam(param.id, 'value', e.target.value)}
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
                        onChange={(e) => updateQueryParam(param.id, 'description', e.target.value)}
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
                        type="button"
                        onClick={() => deleteQueryParam(param.id)}
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
              Query parameters are appended to the URL after a ? in the form of key=value pairs, separated by &.
            </p>
            <button
              type="button"
              onClick={addQueryParam}
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
            <button type="button" className="text-xs px-2 py-1 rounded hover-lift" style={{ 
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
              type="button"
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
                      type="button"
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
                        type="button"
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

  // Render Body Tab
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
                        type="button"
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
                          <button type="button" className="w-full px-2 py-1.5 border rounded-sm text-sm text-left hover:bg-opacity-50 transition-colors hover-lift"
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
                          type="button"
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
                        type="button"
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
                          type="button"
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
                    type="button"
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
                    type="button"
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

        case 'xml':
        return (
          <div className="border rounded overflow-hidden" style={{ borderColor: colors.border }}>
            <div className="flex items-center justify-between px-3 py-2 border-b" style={{ 
              backgroundColor: colors.tableHeader,
              borderColor: colors.border
            }}>
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium" style={{ color: colors.text }}>XML</span>
                <button 
                  type="button"
                  className="px-2 py-1 text-sm rounded hover:bg-opacity-50 transition-colors hover-lift" 
                  style={{ 
                    backgroundColor: colors.hover,
                    color: colors.textSecondary
                  }}
                  onClick={() => {
                    try {
                      // Simple XML beautification (indentation)
                      const formatted = requestBody
                        .replace(/>\s*</g, '>\n<')
                        .split('\n')
                        .map((line, i) => {
                          const indent = line.match(/<\/?[^>]+>/g) ? 
                            (line.startsWith('</') ? -1 : 0) : 0;
                          return '  '.repeat(Math.max(0, i + indent)) + line;
                        })
                        .join('\n');
                      setRequestBody(formatted);
                      showToast('XML beautified!', 'success');
                    } catch (e) {
                      showToast('Invalid XML format', 'error');
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
                  type="button"
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
              placeholder={`<?xml version="1.0" encoding="UTF-8"?>
      <request>
        <field1>value1</field1>
        <field2>value2</field2>
      </request>`}
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
              <button type="button" className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 mx-auto hover-lift"
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
                    <button type="button" onClick={() => setBinaryFile(null)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
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
            {['none', 'form-data', 'x-www-form-urlencoded', 'raw', 'xml', 'binary', 'graphql'].map(type => (
              <button
                key={type}
                type="button"
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

  // Update the renderResponsePanel function with better error handling and display
  const renderResponsePanel = () => {
    const renderResponseContent = () => {
      if (!response) {
        return (
          <div className="h-full flex flex-col items-center justify-center text-center p-8">
            {loading.execute ? (
              <>
                <RefreshCw size={32} className="animate-spin mb-4" style={{ color: colors.textSecondary }} />
                <h3 className="text-sm font-semibold mb-2" style={{ color: colors.text }}>Sending Request...</h3>
                <p className="text-sm max-w-sm" style={{ color: colors.textSecondary }}>
                  Please wait while we process your request.
                </p>
              </>
            ) : (
              <>
                <Send size={32} style={{ color: colors.textSecondary }} className="mb-4 opacity-50" />
                <h3 className="text-sm font-semibold mb-2" style={{ color: colors.text }}>No Response</h3>
                <p className="text-sm max-w-sm" style={{ color: colors.textSecondary }}>
                  Send a request to see the response here.
                </p>
              </>
            )}
          </div>
        );
      }

      // Check if this is an error response (401, 403, etc.)
      const isError = response.statusCode >= 400 || response.data?.statusCode >= 400;
      
      // Extract status information
      const statusCode = response.statusCode || response.data?.statusCode || 0;
      const statusText = response.statusText || response.data?.statusText || 
                        (statusCode === 401 ? 'Unauthorized' : 
                        statusCode >= 400 ? 'Error' : 'Success');

      // Function to find and format response body
      const getFormattedResponseBody = () => {
        // If we have a direct responseBody field
        if (response.responseBody) {
          try {
            // Try to parse it as JSON for better display
            const parsed = JSON.parse(response.responseBody);
            return JSON.stringify(parsed, null, 2);
          } catch {
            // If not JSON, return as is
            return response.responseBody;
          }
        }
        
        // If we have data field
        if (response.data) {
          return JSON.stringify(response.data, null, 2);
        }
        
        // If we have body field
        if (response.body) {
          return response.body;
        }
        
        // Return the whole response object
        return JSON.stringify(response, null, 2);
      };

      // Get headers for display
      const getHeadersForDisplay = () => {
        const headers = response.headers || response.data?.headers || [];
        
        if (Array.isArray(headers)) {
          return headers.map(h => ({
            key: h.key || h.name || 'Unknown',
            value: h.value || h.val || ''
          }));
        } else if (typeof headers === 'object' && headers !== null) {
          return Object.entries(headers).map(([key, value]) => ({
            key,
            value: String(value)
          }));
        }
        
        return [];
      };

      const displayHeaders = getHeadersForDisplay();

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
              {getFormattedResponseBody()}
            </pre>
          );
        
        case 'preview':
          const body = getFormattedResponseBody();
          try {
            const trimmed = String(body).trim();
            if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
              const parsed = JSON.parse(trimmed);
              return (
                <div className="border rounded p-4 overflow-auto text-sm hover-lift"
                  style={{ 
                    backgroundColor: colors.codeBg,
                    borderColor: colors.border,
                    color: colors.text,
                    height: 'calc(100% - 60px)'
                  }}>
                  <SyntaxHighlighter 
                    language="json" 
                    code={JSON.stringify(parsed, null, 2)} 
                  />
                </div>
              );
            }
            if (trimmed.startsWith('<')) {
              return (
                <div className="border rounded p-4 overflow-auto text-sm hover-lift"
                  style={{ 
                    backgroundColor: colors.codeBg,
                    borderColor: colors.border,
                    color: colors.text,
                    height: 'calc(100% - 60px)'
                  }}>
                  <SyntaxHighlighter 
                    language="xml" 
                    code={trimmed} 
                  />
                </div>
              );
            }
          } catch (e) {
            // If parsing fails, show as text
          }
          
          return (
            <pre className="border rounded p-4 overflow-auto text-sm font-mono whitespace-pre-wrap hover-lift"
              style={{ 
                backgroundColor: colors.codeBg,
                borderColor: colors.border,
                color: colors.text,
                height: 'calc(100% - 60px)'
              }}>
              {String(body)}
            </pre>
          );
        
        case 'headers':
          return (
            <div className="border rounded overflow-hidden hover-lift"
              style={{ 
                backgroundColor: colors.codeBg,
                borderColor: colors.border,
                height: 'calc(100% - 60px)',
                overflow: 'auto'
              }}>
              {displayHeaders.length > 0 ? (
                <table className="w-full">
                  <thead style={{ backgroundColor: colors.tableHeader, position: 'sticky', top: 0 }}>
                    <tr>
                      <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Header</th>
                      <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Value</th>
                    </tr>
                  </thead>
                  <tbody>
                    {displayHeaders.map((header, index) => (
                      <tr key={index} className="border-b last:border-b-0 hover-lift" style={{ borderColor: colors.border }}>
                        <td className="px-4 py-3 font-medium" style={{ color: colors.text }}>{header.key}</td>
                        <td className="px-4 py-3" style={{ color: colors.textSecondary }}>{header.value}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <div className="p-4 text-center" style={{ color: colors.textSecondary }}>
                  <p className="text-sm">No headers in response</p>
                </div>
              )}
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
              {getFormattedResponseBody()}
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
                    type="button"
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
              <button type="button" 
                className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.textSecondary }}
                onClick={() => {
                  const contentToCopy = response.responseBody || 
                                      JSON.stringify(response.data, null, 2) || 
                                      JSON.stringify(response, null, 2);
                  navigator.clipboard.writeText(contentToCopy);
                  showToast('Copied to clipboard!', 'success');
                }}>
                Copy
              </button>
            </div>
          )}
        </div>
        
        <div className="flex-1 overflow-auto p-4" style={{ backgroundColor: colors.card }}>
          {response && (
            <div className="space-y-4">
              <div className="flex items-center gap-3">
                <div className={`px-3 py-1.5 rounded text-sm font-medium flex items-center gap-2 hover-lift ${
                  response.statusCode >= 200 && response.statusCode < 300 ? 'bg-green-500/10 text-green-500' : 
                  response.statusCode >= 400 && response.statusCode < 500 ? 'bg-orange-500/10 text-orange-500' :
                  response.statusCode >= 500 ? 'bg-red-500/10 text-red-500' : 'bg-gray-500/10 text-gray-500'
                }`}>
                  {response.statusCode >= 200 && response.statusCode < 300 ? <CheckCircle size={12} /> : 
                  response.statusCode >= 400 && response.statusCode < 500 ? <AlertCircle size={12} /> :
                  response.statusCode >= 500 ? <XCircle size={12} /> : <Info size={12} />}
                  {response.statusCode} {response.statusText}
                </div>
                <div className="text-sm" style={{ color: colors.textSecondary }}>
                  Time: {response.responseTime || 0}ms • Size: {response.responseSize || 0}KB
                </div>
              </div>
              {renderResponseContent()}
            </div>
          )}
        </div>
      </div>
    );
  };

 
  const handleExecuteRequest = useCallback(async () => {
  const validationErrors = validateExecuteRequest({
    method: requestMethod,
    url: requestUrl
  });

  if (validationErrors.length > 0) {
    showToast(validationErrors[0], 'error');
    return;
  }

  setLoading(prev => ({ ...prev, execute: true }));
  setIsSending(true);
  setResponse(null);

  const startTime = Date.now();

  try {
    // ============== STEP 1: BUILD THE FINAL URL WITH PATH PARAMETERS ==============
    let finalUrl = requestUrl;
    
    // Get the path parameters from state
    const pathParamsArray = requestPathParams
      .filter(p => p.enabled && p.key && p.key.trim() !== '')
      .map(p => ({
        key: p.key.trim(),
        value: p.value || ''
      }));

    console.log('📤 Path params from UI:', pathParamsArray);

    // CRITICAL FIX: The backend expects the URL to have the path parameter values
    // in the correct positions with the correct parameter names.
    // For example: {{baseUrl}}/plx/api/gen/{apiId}/api/v1/tblbeneficiary/{benecode}
    // The URL should become: .../tblbeneficiary/12345
    
    if (pathParamsArray.length > 0) {
      // Get the base URL (everything up to the endpoint path)
      // This is a simpler approach - just split on the endpoint path
      const endpointIndex = finalUrl.indexOf('/tblbeneficiary');
      if (endpointIndex !== -1) {
        const baseUrl = finalUrl.substring(0, endpointIndex + '/tblbeneficiary'.length);
        
        // Build the URL by adding each path param value
        let pathWithParams = baseUrl;
        pathParamsArray.forEach(param => {
          // Add the value, encoding if needed
          pathWithParams += '/' + encodeURIComponent(param.value);
        });
        
        // Add any query string that might exist
        if (finalUrl.includes('?')) {
          const queryString = finalUrl.substring(finalUrl.indexOf('?'));
          pathWithParams += queryString;
        }
        
        finalUrl = pathWithParams;
        console.log('🔧 Reconstructed URL with path params:', finalUrl);
      } else {
        // Fallback: replace placeholders in the URL
        pathParamsArray.forEach(param => {
          const placeholder = `{${param.key}}`;
          const colonPlaceholder = `:${param.key}`;
          
          if (finalUrl.includes(placeholder)) {
            finalUrl = finalUrl.replace(new RegExp(placeholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), encodeURIComponent(param.value));
          }
          
          if (finalUrl.includes(colonPlaceholder)) {
            finalUrl = finalUrl.replace(new RegExp(colonPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), encodeURIComponent(param.value));
          }
        });
      }
    }

    // ============== STEP 2: BUILD THE REQUEST BODY ==============
    let body = null;
    let contentType = '';

    // Handle different body types (your existing code)
    if (requestBodyType === 'raw') {
      switch (rawBodyType) {
        case 'json':
          contentType = 'application/json';
          if (requestBody && requestBody.trim()) {
            try {
              JSON.parse(requestBody);
              body = requestBody;
            } catch (e) {
              showToast('Invalid JSON in request body', 'error');
              setLoading(prev => ({ ...prev, execute: false }));
              setIsSending(false);
              return;
            }
          }
          break;
        case 'xml':
          contentType = 'application/xml';
          body = requestBody || '';
          break;
        case 'html':
          contentType = 'text/html';
          body = requestBody || '';
          break;
        case 'javascript':
          contentType = 'application/javascript';
          body = requestBody || '';
          break;
        default:
          contentType = 'text/plain';
          body = requestBody || '';
      }
    } 
    else if (requestBodyType === 'form-data') {
      const hasFiles = formData.some(f => f.type === 'file' && f.file);
      
      if (hasFiles) {
        const formDataObj = new FormData();
        formData.filter(f => f.enabled && f.key && f.key.trim() !== '').forEach(field => {
          if (field.type === 'file' && field.file) {
            formDataObj.append(field.key, field.file);
          } else {
            formDataObj.append(field.key, field.value || '');
          }
        });
        body = formDataObj;
      } else {
        contentType = 'application/json';
        const jsonBody = {};
        formData.filter(f => f.enabled && f.key && f.key.trim() !== '').forEach(field => {
          jsonBody[field.key] = field.value || '';
        });
        body = JSON.stringify(jsonBody);
      }
    } 
    else if (requestBodyType === 'x-www-form-urlencoded') {
      contentType = 'application/x-www-form-urlencoded';
      const params = new URLSearchParams();
      urlEncodedData.filter(u => u.enabled && u.key && u.key.trim() !== '').forEach(item => {
        params.append(item.key, item.value || '');
      });
      body = params.toString();
    } 
    else if (requestBodyType === 'xml') {
      contentType = 'application/xml';
      body = requestBody || '';
    }
    else if (requestBodyType === 'graphql') {
      contentType = 'application/json';
      const graphqlBody = {
        query: graphqlQuery || ''
      };
      if (graphqlVariables) {
        try {
          graphqlBody.variables = JSON.parse(graphqlVariables);
        } catch (e) {
          showToast('Invalid GraphQL variables JSON', 'error');
          setLoading(prev => ({ ...prev, execute: false }));
          setIsSending(false);
          return;
        }
      }
      body = JSON.stringify(graphqlBody);
    }
    else if (requestBodyType === 'binary' && binaryFile) {
      body = binaryFile;
    }

    // ============== STEP 3: BUILD HEADERS ==============
    const headers = {};
    
    // Add headers from Headers tab
    requestHeaders.filter(h => h.enabled && h.key && h.key.trim() !== '').forEach(header => {
      headers[header.key.trim()] = header.value || '';
    });

    // Add authentication headers
    const hasApiKeyConfig = authConfig && (authConfig.type === 'apikey' || authConfig.authType === 'apikey');
    
    if (hasApiKeyConfig) {
      const apiKeyHeader = authConfig.key || authConfig.apiKeyHeader || '';
      const apiKeyValue = authConfig.value || authConfig.apiKeyValue || '';
      if (apiKeyHeader && apiKeyValue) {
        headers[apiKeyHeader] = apiKeyValue;
      }
      
      const apiSecretHeader = authConfig.apiSecretHeader || 'X-API-Secret';
      const apiSecretValue = authConfig.apiSecretValue || authConfig.apiKeySecret || '';
      if (apiSecretHeader && apiSecretValue) {
        headers[apiSecretHeader] = apiSecretValue;
      }
    }
    else if (authType === 'bearer' && authConfig.token) {
      headers['Authorization'] = `${authConfig.tokenType || 'Bearer'} ${authConfig.token}`;
    }
    else if (authType === 'basic' && authConfig.username && authConfig.password) {
      const credentials = btoa(`${authConfig.username}:${authConfig.password}`);
      headers['Authorization'] = `Basic ${credentials}`;
    }
    else if (authType === 'oauth2' && authConfig.token) {
      headers['Authorization'] = `Bearer ${authConfig.token}`;
    }

    // Set Content-Type header if needed
    if (contentType && !(body instanceof FormData)) {
      if (!headers['Content-Type']) {
        headers['Content-Type'] = contentType;
      }
    }

    // Set Accept header if not present
    if (!headers['Accept']) {
      headers['Accept'] = '*/*';
    }

    // For GET and HEAD requests, ensure body is null
    if (requestMethod === 'GET' || requestMethod === 'HEAD') {
      body = null;
    }

    // ============== STEP 4: LOG THE FINAL REQUEST ==============
    console.log('📤 Final Request Details:', {
      method: requestMethod,
      url: finalUrl,
      headers: headers,
      hasBody: !!body,
      bodyType: body instanceof FormData ? 'FormData' : (body ? typeof body : 'none'),
      pathParams: pathParamsArray
    });

    // ============== STEP 5: MAKE THE REQUEST ==============
    const fetchOptions = {
      method: requestMethod,
      headers: headers,
      mode: 'cors',
      credentials: 'omit',
      redirect: 'follow'
    };

    if (body) {
      fetchOptions.body = body;
    }

    console.log('📡 Making request to:', finalUrl);

    const fetchResponse = await fetch(finalUrl, fetchOptions);
    
    const responseTime = Date.now() - startTime;

    // Get response headers
    const responseHeaders = [];
    fetchResponse.headers.forEach((value, key) => {
      responseHeaders.push({ key, value });
    });

    // Get response body based on content type
    let responseBody = '';
    const responseContentType = fetchResponse.headers.get('content-type') || '';

    if (responseContentType.includes('application/json')) {
      try {
        const jsonData = await fetchResponse.json();
        responseBody = JSON.stringify(jsonData, null, 2);
      } catch {
        responseBody = await fetchResponse.text();
      }
    } else if (responseContentType.includes('text/')) {
      responseBody = await fetchResponse.text();
    } else if (responseContentType.includes('xml')) {
      responseBody = await fetchResponse.text();
    } else {
      const blob = await fetchResponse.blob();
      if (blob.size < 1024 * 1024) {
        responseBody = await blob.text();
      } else {
        responseBody = `[Binary data: ${blob.type}, ${(blob.size / 1024).toFixed(2)} KB]`;
      }
    }

    // Format the response for display
    const formattedResponse = {
      responseBody,
      statusCode: fetchResponse.status,
      statusText: fetchResponse.statusText,
      headers: responseHeaders,
      responseTime,
      responseSize: Math.round(new Blob([responseBody]).size / 1024),
      data: responseBody
    };

    setResponse(formattedResponse);

    // Show appropriate toast based on status code
    if (fetchResponse.ok) {
      showToast(`✓ ${requestMethod} ${fetchResponse.status}`, 'success');
    } else if (fetchResponse.status >= 400 && fetchResponse.status < 500) {
      showToast(`⚠️ Client Error: ${fetchResponse.status} - ${fetchResponse.statusText}`, 'warning');
    } else if (fetchResponse.status >= 500) {
      showToast(`❌ Server Error: ${fetchResponse.status} - ${fetchResponse.statusText}`, 'error');
    }

  } catch (error) {
    console.error('Error executing request:', error);
    
    const responseTime = Date.now() - startTime;
    
    setResponse({
      responseBody: JSON.stringify({
        error: error.message,
        name: error.name,
        message: error.message,
        timestamp: new Date().toISOString(),
        url: requestUrl,
        method: requestMethod
      }, null, 2),
      statusCode: 0,
      statusText: error.name || 'Request Failed',
      headers: [],
      responseTime,
      responseSize: 0,
      data: { error: error.message }
    });
    
    showToast(`❌ Request failed: ${error.message}`, 'error');
  } finally {
    setLoading(prev => ({ ...prev, execute: false }));
    setIsSending(false);
  }
}, [authToken, requestMethod, requestUrl, requestHeaders, requestBody, requestParams, requestPathParams, 
    authType, authConfig, requestBodyType, rawBodyType, formData, urlEncodedData, binaryFile, 
    graphqlQuery, graphqlVariables]);
    

// Also update the addFormData function to handle file uploads properly
const addFormData = () => {
  const newItem = { 
    id: `form-${Date.now()}`, 
    key: '', 
    value: '', 
    type: 'text', 
    enabled: true,
    file: null
  };
  setFormData([...formData, newItem]);
};

// Update form data file handling
const handleFormDataFile = (index, file) => {
  const newData = [...formData];
  newData[index].file = file;
  newData[index].value = file.name;
  setFormData(newData);
};

// Add this function to parse and beautify JSON responses
const beautifyResponse = (response) => {
  if (!response) return '';
  
  try {
    if (typeof response === 'string') {
      const parsed = JSON.parse(response);
      return JSON.stringify(parsed, null, 2);
    }
    return JSON.stringify(response, null, 2);
  } catch {
    return response;
  }
};

// Update the response display in renderResponsePanel
const renderResponseContent = () => {
  if (!response) {
    return (
      <div className="h-full flex flex-col items-center justify-center text-center p-8">
        {loading.execute ? (
          <>
            <RefreshCw size={32} className="animate-spin mb-4" style={{ color: colors.textSecondary }} />
            <h3 className="text-sm font-semibold mb-2" style={{ color: colors.text }}>Sending Request...</h3>
            <p className="text-sm max-w-sm" style={{ color: colors.textSecondary }}>
              Method: {requestMethod} • URL: {requestUrl}
            </p>
          </>
        ) : (
          <>
            <Send size={32} style={{ color: colors.textSecondary }} className="mb-4 opacity-50" />
            <h3 className="text-sm font-semibold mb-2" style={{ color: colors.text }}>No Response</h3>
            <p className="text-sm max-w-sm" style={{ color: colors.textSecondary }}>
              Click Send to execute the request
            </p>
          </>
        )}
      </div>
    );
  }

  const statusCode = response.statusCode || 0;
  const statusText = response.statusText || '';
  const isError = statusCode >= 400;
  
  // Get response body
  let responseBody = '';
  if (response.responseBody) {
    responseBody = response.responseBody;
  } else if (response.data) {
    responseBody = JSON.stringify(response.data, null, 2);
  } else if (response.body) {
    responseBody = response.body;
  } else {
    responseBody = JSON.stringify(response, null, 2);
  }

  // Try to detect content type from headers
  let contentType = 'text/plain';
  if (response.headers) {
    const headers = Array.isArray(response.headers) 
      ? response.headers 
      : Object.entries(response.headers).map(([k, v]) => ({ key: k, value: v }));
    
    const contentTypeHeader = headers.find(h => 
      h.key.toLowerCase() === 'content-type'
    );
    if (contentTypeHeader) {
      contentType = contentTypeHeader.value;
    }
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
          {responseBody}
        </pre>
      );
    
    case 'preview':
      if (contentType.includes('application/json') || responseBody.trim().startsWith('{') || responseBody.trim().startsWith('[')) {
        try {
          const parsed = JSON.parse(responseBody);
          return (
            <div className="border rounded p-4 overflow-auto text-sm hover-lift"
              style={{ 
                backgroundColor: colors.codeBg,
                borderColor: colors.border,
                color: colors.text,
                height: 'calc(100% - 60px)'
              }}>
              <SyntaxHighlighter 
                language="json" 
                code={JSON.stringify(parsed, null, 2)} 
              />
            </div>
          );
        } catch (e) {
          // If not valid JSON, show as text
        }
      } else if (contentType.includes('xml') || responseBody.trim().startsWith('<')) {
        return (
          <div className="border rounded p-4 overflow-auto text-sm hover-lift"
            style={{ 
              backgroundColor: colors.codeBg,
              borderColor: colors.border,
              color: colors.text,
              height: 'calc(100% - 60px)'
            }}>
            <SyntaxHighlighter 
              language="xml" 
              code={responseBody} 
            />
          </div>
        );
      } else if (contentType.includes('html')) {
        return (
          <iframe
            srcDoc={responseBody}
            className="w-full h-full border-0"
            sandbox="allow-same-origin"
            title="HTML Preview"
          />
        );
      }
      
      return (
        <pre className="border rounded p-4 overflow-auto text-sm font-mono whitespace-pre-wrap hover-lift"
          style={{ 
            backgroundColor: colors.codeBg,
            borderColor: colors.border,
            color: colors.text,
            height: 'calc(100% - 60px)'
          }}>
          {responseBody}
        </pre>
      );
    
    case 'headers':
      const headers = response.headers || [];
      const headerArray = Array.isArray(headers) ? headers : Object.entries(headers).map(([k, v]) => ({
        key: k,
        value: v
      }));
      
      return (
        <div className="border rounded overflow-hidden hover-lift"
          style={{ 
            backgroundColor: colors.codeBg,
            borderColor: colors.border,
            height: 'calc(100% - 60px)',
            overflow: 'auto'
          }}>
          {headerArray.length > 0 ? (
            <table className="w-full">
              <thead style={{ backgroundColor: colors.tableHeader, position: 'sticky', top: 0 }}>
                <tr>
                  <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Header</th>
                  <th className="text-left px-4 py-3 text-sm font-medium" style={{ color: colors.textSecondary }}>Value</th>
                </tr>
              </thead>
              <tbody>
                {headerArray.map((header, index) => (
                  <tr key={index} className="border-b last:border-b-0 hover-lift" style={{ borderColor: colors.border }}>
                    <td className="px-4 py-3 font-medium" style={{ color: colors.text }}>{header.key}</td>
                    <td className="px-4 py-3 break-all" style={{ color: colors.textSecondary }}>{header.value}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div className="p-4 text-center" style={{ color: colors.textSecondary }}>
              <p className="text-sm">No headers in response</p>
            </div>
          )}
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
          {responseBody}
        </pre>
      );
  }
};

// Update the response panel header to show more details
<div className="flex items-center justify-between px-4 py-3 border-b" style={{ 
  backgroundColor: colors.card,
  borderColor: colors.border
}}>
  <div className="flex items-center gap-4">
    <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Response</h3>
    {response && (
      <>
        <div className={`px-2 py-1 rounded text-xs font-medium ${
          response.statusCode >= 200 && response.statusCode < 300 ? 'bg-green-500/10 text-green-500' : 
          response.statusCode >= 400 && response.statusCode < 500 ? 'bg-orange-500/10 text-orange-500' :
          response.statusCode >= 500 ? 'bg-red-500/10 text-red-500' : 'bg-gray-500/10 text-gray-500'
        }`}>
          {response.statusCode} {response.statusText}
        </div>
        <div className="flex items-center gap-3 text-xs" style={{ color: colors.textSecondary }}>
          <span>⏱️ {response.responseTime || 0}ms</span>
          <span>📦 {response.responseSize || 0} KB</span>
        </div>
        <div className="flex items-center gap-1">
          {['raw', 'preview', 'headers'].map(view => (
            <button key={view}
              type="button"
              onClick={() => setResponseView(view)}
              className={`px-2 py-1 rounded text-xs font-medium capitalize transition-colors hover-lift ${
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
      </>
    )}
  </div>
  {response && (
    <div className="flex items-center gap-2">
      <button type="button" 
        className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
        style={{ backgroundColor: colors.hover, color: colors.textSecondary }}
        onClick={() => {
          const contentToCopy = response.responseBody || 
                              JSON.stringify(response.data, null, 2) || 
                              JSON.stringify(response, null, 2);
          navigator.clipboard.writeText(contentToCopy);
          showToast('Response copied to clipboard!', 'success');
        }}>
        <Copy size={12} className="mr-1 inline" />
        Copy
      </button>
    </div>
  )}
</div>

  // Render other right panels
  const renderAPIsPanel = () => (
    <div className="w-80 border-l flex flex-col" style={{ 
      backgroundColor: colors.sidebar,
      borderColor: colors.border
    }}>
      <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
        <h3 className="text-sm font-semibold" style={{ color: colors.text }}>APIs</h3>
        <button type="button" onClick={() => setShowAPIs(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
          style={{ backgroundColor: colors.hover }}>
          <X size={14} style={{ color: colors.textSecondary }} />
        </button>
      </div>
      <div className="p-4 text-center" style={{ color: colors.textSecondary }}>
        <p className="text-sm">No APIs available</p>
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
        <button type="button" onClick={() => setShowEnvironments(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
          style={{ backgroundColor: colors.hover }}>
          <X size={14} style={{ color: colors.textSecondary }} />
        </button>
      </div>
      <div className="p-4 space-y-3">
        {environments.length === 0 ? (
          <p className="text-sm text-center" style={{ color: colors.textSecondary }}>No environments available</p>
        ) : (
          <>
            {environments.map(env => (
              <button key={env.id}
                type="button"
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
          </>
        )}
      </div>
    </div>
  );

  // Determine which right panel to show
  const renderRightPanel = () => {
    if (showCodePanel) {
      return (
        <CodePanel 
          selectedLanguage={selectedLanguage}
          setSelectedLanguage={memoizedSetSelectedLanguage}
          showLanguageDropdown={showLanguageDropdown}
          setShowLanguageDropdown={memoizedSetShowLanguageDropdown}
          requestMethod={requestMethod}
          requestUrl={requestUrl}
          requestHeaders={memoizedRequestHeaders}
          requestBody={requestBody}
          authToken={authToken}
          loading={loading}
          setLoading={memoizedSetLoading}
          showToast={memoizedShowToast}
          colors={colors}
          setShowCodePanel={memoizedSetShowCodePanel}
          selectedRequest={selectedRequest} // Add this line
        />
      );
    }
    if (showAPIs) return renderAPIsPanel();
    if (showEnvironments) return renderEnvironmentsPanel();
    if (showMockServers) return renderMockServersPanel();
    if (showMonitors) return renderMonitorsPanel();
    return null;
  };

  const renderMockServersPanel = () => (
    <div className="w-80 border-l flex flex-col" style={{ 
      backgroundColor: colors.sidebar,
      borderColor: colors.border
    }}>
      <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
        <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Mock Servers</h3>
        <button type="button" onClick={() => setShowMockServers(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
          style={{ backgroundColor: colors.hover }}>
          <X size={14} style={{ color: colors.textSecondary }} />
        </button>
      </div>
      <div className="p-4 text-center" style={{ color: colors.textSecondary }}>
        <p className="text-sm">No mock servers available</p>
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
        <button type="button" onClick={() => setShowMonitors(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
          style={{ backgroundColor: colors.hover }}>
          <X size={14} style={{ color: colors.textSecondary }} />
        </button>
      </div>
      <div className="p-4 text-center" style={{ color: colors.textSecondary }}>
        <p className="text-sm">No monitors available</p>
      </div>
    </div>
  );

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
          backgroundColor: colors.bg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Import</h3>
            <button type="button" onClick={() => setShowImportModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}>
              <X size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          <div className="p-4 space-y-4">
            <div className="text-center p-8 border-2 border-dashed rounded hover-lift" style={{ borderColor: colors.border }}>
              <Upload size={32} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mx-auto mb-4" />
              <p className="text-sm mb-2" style={{ color: colors.text }}>Drag and drop files here</p>
              <p className="text-xs" style={{ color: colors.textSecondary }}>Supports: Postman collections, OpenAPI, etc.</p>
              <button 
                type="button"
                className="mt-4 px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ 
                  backgroundColor: colors.primaryDark, 
                  color: colors.white,
                  opacity: loading.import ? 0.7 : 1
                }}
                onClick={() => {
                  const input = document.createElement('input');
                  input.type = 'file';
                  input.accept = '.json,.yaml,.yml,.postman_collection';
                  input.onchange = async (e) => {
                    const file = e.target.files[0];
                    if (file) {
                      try {
                        const text = await file.text();
                        await handleImportCollection(text, 'POSTMAN');
                        setShowImportModal(false);
                      } catch (error) {
                        showToast('Failed to import file', 'error');
                      }
                    }
                  };
                  input.click();
                }}
                disabled={loading.import}
              >
                {loading.import ? (
                  <div className="flex items-center gap-2">
                    <RefreshCw size={14} className="animate-spin" />
                    Importing...
                  </div>
                ) : (
                  'Browse Files'
                )}
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderShareModal = () => {
    if (!showShareModal) return null;
    
    // Get share link from selected collection/request
    const shareLink = selectedRequest?.id ? 
      `${window.location.origin}/share/request/${selectedRequest.id}` : 
      '';

    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="rounded-lg w-full max-w-md" style={{ 
          backgroundColor: colors.bg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Share {selectedRequest ? 'Request' : 'Collection'}</h3>
            <button type="button" onClick={() => setShowShareModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}>
              <X size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          <div className="p-4 space-y-4">
            {!selectedRequest ? (
              <p className="text-sm text-center" style={{ color: colors.textSecondary }}>
                Select a request or collection to share
              </p>
            ) : (
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Share Link</label>
                <div className="flex gap-2">
                  <input
                    type="text"
                    readOnly
                    value={shareLink}
                    className="flex-1 px-3 py-2 border rounded text-sm focus:outline-none hover-lift"
                    style={{
                      backgroundColor: colors.inputBg,
                      borderColor: colors.border,
                      color: colors.text
                    }}
                  />
                  <button type="button" className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                    onClick={() => {
                      navigator.clipboard.writeText(shareLink);
                      showToast('Link copied to clipboard!', 'success');
                    }}
                    style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                    Copy
                  </button>
                </div>
              </div>
            )}
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
          backgroundColor: colors.bg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Settings</h3>
            <button type="button" onClick={() => setShowSettingsModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
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
                  <input type="checkbox" className="rounded hover-lift" />
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm" style={{ color: colors.text }}>Language detection</span>
                  <input type="checkbox" className="rounded hover-lift" />
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
                  <input type="number" className="w-24 px-2 py-1 border rounded text-sm hover-lift"
                    style={{ backgroundColor: colors.inputBg, borderColor: colors.border, color: colors.text }}
                    placeholder="0" />
                </div>
              </div>
            </div>
          </div>
          <div className="p-4 border-t" style={{ borderColor: colors.border }}>
            <button type="button" className="w-full py-2.5 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
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
    return (
      showCreateModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="rounded-lg w-full max-w-md" style={{ 
            backgroundColor: colors.bg,
            border: `1px solid ${colors.modalBorder}`
          }}>
            <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Create Collection</h3>
              <button type="button" onClick={() => setShowCreateModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
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
              <button 
                type="button"
                className="w-full py-2.5 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                onClick={() => {
                  if (!newCollectionName.trim()) {
                    showToast('Please enter a collection name', 'error');
                    return;
                  }
                  addNewCollection(newCollectionName, newCollectionDescription);
                }}
                style={{ 
                  backgroundColor: colors.primaryDark, 
                  color: colors.white,
                  opacity: loading.create ? 0.7 : 1
                }}
                disabled={loading.create}
              >
                {loading.create ? (
                  <div className="flex items-center justify-center gap-2">
                    <RefreshCw size={14} className="animate-spin" />
                    Creating...
                  </div>
                ) : (
                  'Create Collection'
                )}
              </button>
            </div>
          </div>
        </div>
      )
    );
  };

  // Render Collections tree
  const renderCollectionsTree = () => {
    if (loading.collections || loading.initialLoad) {
      return (
        <div className="flex items-center justify-center h-32">
          <RefreshCw className="animate-spin" size={16} style={{ color: colors.textSecondary }} />
        </div>
      );
    }

    if (error) {
      return (
        <div className="p-4 text-center">
          <AlertCircle size={24} className="mx-auto mb-2" style={{ color: colors.error }} />
          <div className="text-sm" style={{ color: colors.text }}>{error}</div>
          <button 
            type="button"
            onClick={fetchCollections}
            className="mt-3 px-4 py-2 rounded text-sm font-medium transition-colors hover-lift"
            style={{ 
              backgroundColor: colors.hover,
              color: colors.text
            }}
            disabled={loading.collections}
          >
            {loading.collections ? (
              <div className="flex items-center gap-2">
                <RefreshCw size={12} className="animate-spin" />
                Retrying...
              </div>
            ) : (
              'Retry'
            )}
          </button>
        </div>
      );
    }

    if (collections.length === 0) {
      return (
        <div className="p-4 text-center">
          <Folder size={48} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mx-auto mb-4" />
          <p className="text-sm" style={{ color: colors.text }}>No collections found</p>
          <button 
            type="button"
            onClick={() => setShowCreateModal(true)}
            className="mt-3 px-4 py-2 rounded text-sm font-medium transition-colors hover-lift"
            style={{ 
              backgroundColor: colors.primaryDark,
              color: colors.white
            }}
          >
            Create your first collection
          </button>
        </div>
      );
    }

    return (
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
              <button type="button" onClick={(e) => {
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
              
              {collection.requestsCount > 0 && (
                <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                  backgroundColor: colors.border,
                  color: colors.textSecondary
                }}>
                  {collection.requestsCount}
                </span>
              )}
            </div>

            {collection.isExpanded && collection.folders && collection.folders.length > 0 && (
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
                        type="button"
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

                    {folder.isExpanded && folder.requests && folder.requests.length > 0 && (
                      <>
                        {folder.requests.map(request => (
                          <div key={request.id} className="flex items-center gap-2 ml-6 mb-1.5 group">
                            <button
                              type="button"
                              onClick={() => handleSelectRequest(request, collection.id, folder.id)}
                              className="flex items-center gap-2 text-sm text-left transition-colors hover:text-opacity-80 flex-1 px-2 py-1.5 rounded hover:bg-opacity-50 hover-lift"
                              style={{ 
                                color: selectedRequest?.id === request.id ? colors.primary : colors.text,
                                backgroundColor: selectedRequest?.id === request.id ? colors.selected : 'transparent'
                              }}
                              disabled={loading.request}
                            >
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
                                type="button"
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
                          type="button"
                          onClick={() => addNewRequest(collection.id, folder.id)}
                          className="ml-6 px-3 py-1.5 text-xs rounded hover:bg-opacity-50 transition-colors flex items-center gap-1.5 mt-1 hover-lift"
                          style={{ backgroundColor: colors.hover, color: colors.textSecondary }}
                          disabled={loading.request}
                        >
                          <Plus size={10} />
                          Add Request
                        </button>
                      </>
                    )}
                  </div>
                ))}
                <button
                  type="button"
                  onClick={() => addNewFolder(collection.id)}
                  className="ml-4 px-3 py-1.5 text-xs rounded hover:bg-opacity-50 transition-colors flex items-center gap-1.5 mt-1 hover-lift"
                  style={{ backgroundColor: colors.hover, color: colors.textSecondary }}
                  disabled={loading.request}
                >
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
        
        input:focus, button:focus {
          outline: 2px solid ${colors.primary}40;
          outline-offset: 2px;
        }
        
        .hover-lift:hover {
          transform: translateY(-2px);
          transition: transform 0.2s ease;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        .gradient-bg {
          background: linear-gradient(135deg, ${colors.primary}20 0%, ${colors.info}20 50%, ${colors.warning}20 100%);
        }

       /* Custom scrollbar styles */
      .scrollbar-thin::-webkit-scrollbar {
        height: 4px;
        width: 4px;
      }
      
      .scrollbar-thin::-webkit-scrollbar-track {
        background: ${colors.border};
        border-radius: 4px;
      }
      
      .scrollbar-thin::-webkit-scrollbar-thumb {
        background: ${colors.textTertiary};
        border-radius: 4px;
        opacity: 0;
        transition: opacity 0.2s ease;
      }
      
      .scrollbar-thin:hover::-webkit-scrollbar-thumb,
      .scrollbar-thin::-webkit-scrollbar-thumb:hover {
        background: ${colors.textSecondary};
      }
      
      .hover\\:scrollbar-thumb-visible:hover::-webkit-scrollbar-thumb {
        opacity: 1;
      }
      
      /* Hide scrollbar by default, show on hover */
      .scrollbar-thin::-webkit-scrollbar-thumb {
        opacity: 0;
      }
      
      .scrollbar-thin:hover::-webkit-scrollbar-thumb {
        opacity: 1;
      }
      
      /* For Firefox */
      .scrollbar-thin {
        scrollbar-width: thin;
        scrollbar-color: transparent ${colors.border};
      }
      
      .scrollbar-thin:hover {
        scrollbar-color: ${colors.textTertiary} ${colors.border};
      }
    
     /* Ensure flex containers can shrink properly */
      .min-w-0 {
        min-width: 0;
      }
      
      .min-h-0 {
        min-height: 0;
      }
      
      /* Improve scrollbar behavior */
      .overflow-x-auto {
        -webkit-overflow-scrolling: touch;
        scroll-behavior: smooth;
      }
      
      /* Hide scrollbar when not hovering on Firefox */
      .scrollbar-thin {
        scrollbar-width: thin;
        scrollbar-color: transparent ${colors.border};
      }
      
      .scrollbar-thin:hover {
        scrollbar-color: ${colors.textTertiary} ${colors.border};
      }
    `}</style>

      {/* Loading Overlay */}
      {/* <LoadingOverlay /> */}

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
            <button type="button" className="flex items-center gap-2 px-3 py-1.5 rounded text-sm hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}
              onClick={() => {
                if (environments.length === 0) {
                  showToast('No environments available', 'info');
                  return;
                }
                const currentIndex = environments.findIndex(e => e.id === activeEnvironment);
                const nextIndex = (currentIndex + 1) % environments.length;
                const nextEnv = environments[nextIndex];
                setActiveEnvironment(nextEnv.id);
                setEnvironments(envs => envs.map(e => ({ ...e, isActive: e.id === nextEnv.id })));
                showToast(`Switched to ${nextEnv.name}`, 'success');
              }}>
              <Globe size={12} style={{ color: colors.textSecondary }} />
              <span style={{ color: colors.text }}>
                {environments.find(e => e.id === activeEnvironment)?.name || 'No Environment'}
              </span>
              <ChevronDown size={12} style={{ color: colors.textSecondary }} />
            </button>
          </div>

          <div className="w-px h-4" style={{ backgroundColor: colors.border }}></div>

          {/* Code Panel Toggle */}
          <button type="button" onClick={() => {setShowCodePanel(!showCodePanel); setShowAPIs(false); setShowEnvironments(false); setShowMockServers(false); setShowMonitors(false);}} 
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: showCodePanel ? colors.selected : colors.hover }}>
            <Code size={14} style={{ color: showCodePanel ? colors.primary : colors.textSecondary }} />
          </button>

          {/* Share Button */}
          <button type="button" onClick={() => setShowShareModal(true)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}>
            <Share2 size={14} style={{ color: colors.textSecondary }} />
          </button>

          {/* Settings */}
          <button type="button" onClick={() => setShowSettingsModal(true)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}>
            <Settings size={14} style={{ color: colors.textSecondary }} />
          </button>

        </div>
      </div>

      {/* MAIN CONTENT */}
      <div className="flex flex-1 overflow-hidden">
        {/* LEFT SIDEBAR - Collections */}
        <div className="w-80 border-r flex flex-col" style={{ 
          borderColor: colors.border
        }}>
          <div className="p-3 border-b" style={{ borderColor: colors.border }}>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Collections</h3>
              <div className="flex gap-1">
                <button type="button" onClick={() => setShowCreateModal(true)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}>
                  <Plus size={12} style={{ color: colors.textSecondary }} />
                </button>
                <button type="button" onClick={() => setShowImportModal(true)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}>
                  <Upload size={12} style={{ color: colors.textSecondary }} />
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
                  <button type="button" onClick={() => setSearchQuery('')} className="p-0.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                    style={{ backgroundColor: colors.hover }}>
                    <X size={12} style={{ color: colors.textSecondary }} />
                  </button>
                </div>
              )}
            </div>
          </div>

          {renderCollectionsTree()}
        </div>

        {/* MAIN WORKSPACE */}
        <div className="flex-1 flex flex-col min-w-0" style={{ 
          backgroundColor: colors.card,
          minWidth: 0,
          width: 0  // Force the container to respect flex constraints
        }}>
          {/* REQUEST TABS - Fixed scrolling container */}
          <div className="flex items-center border-b shrink-0" style={{ 
            backgroundColor: colors.card,
            borderColor: colors.border,
            height: '36px',
            width: '100%',
            minWidth: 0
          }}>
            <div className="flex items-center h-full overflow-x-auto overflow-y-hidden scrollbar-thin hover:scrollbar-thumb-visible" 
                style={{ 
                  scrollbarWidth: 'thin',
                  scrollbarColor: `${colors.textTertiary} ${colors.border}`,
                  WebkitOverflowScrolling: 'touch',
                  minWidth: 0,
                  flex: 1,
                  whiteSpace: 'nowrap',
                  width: '100%'
                }}>
              {requestTabs.map(tab => (
                <div key={tab.id}
                  className="inline-flex items-center gap-2 px-3 py-1.5 border-r cursor-pointer hover-lift flex-shrink-0"
                  style={{ 
                    backgroundColor: tab.isActive ? colors.card : colors.sidebar,
                    borderRightColor: colors.border,
                    borderTop: tab.isActive ? `2px solid ${colors.primary}` : '2px solid transparent',
                    minWidth: '100px',
                    maxWidth: '180px',
                    height: '100%'
                  }}
                  onClick={() => {
                    const collection = collections.find(c => c.id === tab.collectionId);
                    const folder = collection?.folders?.find(f => f.id === tab.folderId);
                    const request = folder?.requests?.find(r => r.id === tab.id);
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
                  <button type="button" onClick={(e) => {
                    e.stopPropagation();
                    if (requestTabs.length > 1) {
                      setRequestTabs(tabs => tabs.filter(t => t.id !== tab.id));
                      if (tab.isActive) {
                        const remainingTabs = requestTabs.filter(t => t.id !== tab.id);
                        if (remainingTabs.length > 0) {
                          const nextTab = remainingTabs[0];
                          const collection = collections.find(c => c.id === nextTab.collectionId);
                          const folder = collection?.folders?.find(f => f.id === nextTab.folderId);
                          const request = folder?.requests?.find(r => r.id === nextTab.id);
                          if (request) {
                            handleSelectRequest(request, nextTab.collectionId, nextTab.folderId);
                          }
                        }
                      }
                    } else {
                      showToast('Cannot close the last tab', 'error');
                    }
                  }} className="p-0.5 rounded opacity-0 hover:opacity-100 hover:bg-opacity-50 transition-colors hover-lift flex-shrink-0"
                    style={{ backgroundColor: colors.hover }}>
                    <X size={12} style={{ color: colors.textSecondary }} />
                  </button>
                </div>
              ))}
              <button
                type="button"
                onClick={() => {
                  const newRequest = {
                    id: `req-${Date.now()}`,
                    name: 'New Request',
                    method: 'GET',
                    url: '',
                    description: '',
                    status: 'unsaved',
                    lastModified: new Date().toISOString(),
                    authType: 'noauth',
                    authConfig: { type: 'noauth' },
                    params: [],
                    headers: [],
                    body: '',
                    tests: '',
                    preRequestScript: '',
                    isSaved: false
                  };
                  handleSelectRequest(newRequest, '', '');
                }}
                className="inline-flex items-center px-3 py-1.5 border-r hover:bg-opacity-50 transition-colors hover-lift flex-shrink-0"
                style={{ borderRightColor: colors.border, backgroundColor: colors.hover }}>
                <Plus size={12} style={{ color: colors.textSecondary }} />
              </button>
            </div>
          </div>

          {/* REQUEST BUILDER */}
          <div className="flex-1 overflow-hidden flex flex-col min-h-0">
            {/* URL BAR */}
            <div className="flex items-center gap-2 p-4 shrink-0" style={{ 
              backgroundColor: colors.card
            }}>
              <select 
                value={requestMethod} 
                onChange={(e) => setRequestMethod(e.target.value)}
                className="px-3 py-2 rounded text-sm font-medium focus:outline-none hover-lift shrink-0"
                style={{ 
                  backgroundColor: colors.inputBg,
                  color: getMethodColor(requestMethod),
                  border: `1px solid ${colors.inputborder}`,
                  width: '100px'
                }}>
                {['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS'].map(method => (
                  <option key={method} value={method}>{method}</option>
                ))}
              </select>
              
              {/* URL input */}
              <div className="flex-1 flex items-center rounded overflow-hidden hover-lift min-w-0" style={{ 
                border: `1px solid ${colors.inputborder}`
              }}>
                <input 
                  ref={urlInputRef}
                  type="text" 
                  value={requestUrl} 
                  onChange={handleUrlChange}
                  onPaste={handleUrlPaste}
                  className="flex-1 px-3 py-2 text-sm focus:outline-none min-w-0"
                  style={{ backgroundColor: colors.inputBg, color: colors.text }}
                  placeholder="Enter request URL" 
                />
              </div>
              
              <button 
                type="button"
                onClick={handleExecuteRequest} 
                disabled={isSending || !requestUrl || loading.execute}
                className={`px-4 py-2 rounded text-sm font-medium flex items-center gap-2 transition-colors hover-lift shrink-0 ${
                  isSending || !requestUrl || loading.execute ? 'opacity-50 cursor-not-allowed' : 'hover:opacity-90'
                }`}
                style={{ backgroundColor: colors.primaryDark, color: colors.white, minWidth: '80px' }}>
                {loading.execute ? (
                  <>
                    <RefreshCw size={12} className="animate-spin" />
                    <span>Sending...</span>
                  </>
                ) : (
                  <>
                    <Send size={12} />
                    <span>Send</span>
                  </>
                )}
              </button>
              
              <button 
                type="button"
                className="px-3 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift shrink-0"
                style={{ 
                  backgroundColor: colors.primaryDark, 
                  color: colors.white,
                  opacity: loading.save ? 0.7 : 1,
                  minWidth: '60px'
                }}
                onClick={handleSaveRequest}
                disabled={!selectedRequest || loading.save}>
                {loading.save ? (
                  <div className="flex items-center gap-2">
                    <RefreshCw size={12} className="animate-spin" />
                    <span>Saving...</span>
                  </div>
                ) : (
                  'Save'
                )}
              </button>
            </div>

            {/* REQUEST TABS (Params, Auth, Headers, etc.) */}
            <div className="flex items-center border-t border-b shrink-0" style={{ 
              backgroundColor: colors.card,
              borderColor: colors.border
            }}>
              {['Path Params', 'Query Params', 'Authorization', 'Headers', 'Body', 'Pre-request Script', 'Tests', 'Settings'].map(tab => {
                const tabId = tab.toLowerCase().replace(' ', '-').replace('path-params', 'path-params');
                return (
                  <button 
                    key={tabId} 
                    type="button"
                    onClick={() => setActiveTab(tabId)}
                    className={`px-4 py-2.5 text-sm font-medium border-b-2 transition-colors hover-lift shrink-0 ${
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
            <div className="flex-1 overflow-auto min-h-0" style={{ backgroundColor: colors.card }}>
              {activeTab === 'path-params' && renderPathParamsTab()}
              {activeTab === 'query-params' && renderQueryParamsTab()}
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
                    placeholder={selectedRequest?.tests || '// Write test scripts here'}
                    value={selectedRequest?.tests || ''}
                    onChange={(e) => {
                      setSelectedRequest(prev => ({ ...prev, tests: e.target.value }));
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
                      <input type="checkbox" className="rounded hover-lift" />
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm" style={{ color: colors.text }}>Request timeout (ms)</span>
                      <input type="number" className="w-24 px-2 py-1 border rounded text-sm focus:outline-none hover-lift"
                        style={{ backgroundColor: colors.inputBg, borderColor: colors.border, color: colors.text }}
                        placeholder="0" />
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
                    placeholder={selectedRequest?.preRequestScript || '// Write pre-request script here'}
                    value={selectedRequest?.preRequestScript || ''}
                    onChange={(e) => {
                      setSelectedRequest(prev => ({ ...prev, preRequestScript: e.target.value }));
                    }}
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

// Add React.memo with custom comparison
export default React.memo(Collections, (prevProps, nextProps) => {
  // Only re-render if authToken actually changed
  return prevProps.authToken === nextProps.authToken && 
         prevProps.isDark === nextProps.isDark;
});