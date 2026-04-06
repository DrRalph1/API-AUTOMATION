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
                <p className="text-xs mb-2" style={{ color: colors.text }}>No code generated yet</p>
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
              // Get the raw code text (without HTML tags)
              let textToCopy = codeSnippet;
              
              // If it's HTML content (from SyntaxHighlighter), extract the text
              if (textToCopy.includes('<span') || textToCopy.includes('<div')) {
                // Create a temporary div to parse the HTML
                const tempDiv = document.createElement('div');
                tempDiv.innerHTML = textToCopy;
                // Get text content (removes all HTML tags)
                textToCopy = tempDiv.textContent || tempDiv.innerText || '';
              }
              
              // Clean up the text (remove extra whitespace)
              textToCopy = textToCopy.replace(/\n\s*\n/g, '\n\n').trim();
              
              // Copy to clipboard
              navigator.clipboard.writeText(textToCopy)
                .then(() => {
                  showToast('Copied to clipboard!', 'success');
                })
                .catch((err) => {
                  console.error('Failed to copy:', err);
                  // Fallback method
                  const textarea = document.createElement('textarea');
                  textarea.value = textToCopy;
                  document.body.appendChild(textarea);
                  textarea.select();
                  const success = document.execCommand('copy');
                  document.body.removeChild(textarea);
                  
                  if (success) {
                    showToast('Copied to clipboard!', 'success');
                  } else {
                    showToast('Failed to copy to clipboard', 'error');
                  }
                });
            }}
            disabled={loading.generateSnippet}
            style={{ 
              backgroundColor: colors.primaryDark, 
              color: colors.white, 
              opacity: loading.generateSnippet ? 0.5 : 1 
            }}
          >
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
    // Note: We're NOT including requestPathParams here because CodePanel doesn't use them
  );
});

const Collections = ({ theme, isDark, customTheme, toggleTheme, authToken }) => {
  // Add refs to track mount state and previous authToken
  const isMounted = useRef(true);
  const prevAuthTokenRef = useRef(authToken);
  const initialDataLoaded = useRef(false);
  const fetchInProgressRef = useRef(false);
  const lastRebuiltUrlRef = useRef('');

  const [abortController, setAbortController] = useState(null);

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
  const memoizedRequestHeaders = useMemo(() => requestHeaders || [], [JSON.stringify(requestHeaders || [])]);

const memoizedRequestPathParams = useMemo(() => requestPathParams || [], [
  JSON.stringify((requestPathParams || []).map(p => ({ 
    key: p.key, 
    value: p.value, 
    enabled: p.enabled 
  })))
]);

const memoizedRequestParams = useMemo(() => requestParams || [], [
  JSON.stringify((requestParams || []).map(p => ({ 
    key: p.key, 
    value: p.value, 
    enabled: p.enabled 
  })))
]);

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
  const [requestBodyType, setRequestBodyType] = useState('none');
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


const isBatchUpdating = useRef(false);
const pendingUrlUpdate = useRef(null);

const updateQueryParam = (id, field, value) => {
  console.log('🟢 updateQueryParam called:', { id, field, value });
  
  // Prevent updates during batch processing
  if (isBatchUpdating.current) {
    console.log('⏭️ Skipping update - batch update in progress');
    return;
  }
  
  // Mark that this is a manual URL update
  isManualUrlUpdate.current = true;
  isUpdatingFromInput.current = true;
  
  // First update the state with the new value
  setRequestParams(params => {
    const updatedParams = params.map(param => 
      param.id === id ? { ...param, [field]: value } : param
    );
    
    // Only update URL if we're changing the value, key, or enabled status
    if ((field === 'value' || field === 'key' || field === 'enabled')) {
      
      // CRITICAL FIX: Use the CURRENT requestUrl (which already has env vars resolved)
      // instead of templateUrl
      let baseUrl = requestUrl.split('?')[0];
      
      console.log('📝 Using current URL as base URL:', baseUrl);
      
      // Build query string from enabled params with keys and values
      const queryParams = updatedParams
        .filter(p => p.enabled && p.key && p.key.trim() !== '')
        .map(p => {
          if (p.value && p.value.trim() !== '') {
            return `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value)}`;
          } else {
            return `${encodeURIComponent(p.key)}=`;
          }
        })
        .join('&');
      
      // Construct new URL
      const newUrl = queryParams ? `${baseUrl}?${queryParams}` : baseUrl;
      
      // Only update if URL actually changed
      if (newUrl !== requestUrl) {
        console.log('✅ Final new URL:', newUrl);
        if (lastProcessedUrlRef.current !== newUrl) {
          lastProcessedUrlRef.current = newUrl;
          setRequestUrl(newUrl);
        }
      }
    }
    
    return updatedParams;
  });
  
  // Reset the manual update flag after a delay
  setTimeout(() => {
    isUpdatingFromInput.current = false;
    isManualUrlUpdate.current = false;
  }, 150);
};

// Update deleteQueryParam to preserve path parameters
const deleteQueryParam = (id) => {
  setRequestParams(params => {
    const remainingParams = params.filter(param => param.id !== id);
    
    // Get the template URL (with placeholders)
    let baseUrl = templateUrl;
    
    // If no template URL, use the current URL without query string
    if (!baseUrl) {
      baseUrl = requestUrl.split('?')[0];
    }
    
    // Replace path parameters with actual values
    let finalBaseUrl = baseUrl;
    requestPathParams.forEach(param => {
      if (param.enabled && param.key) {
        const placeholder = `{${param.key}}`;
        const colonPlaceholder = `:${param.key}`;
        const paramValue = param.value && param.value.trim() !== '' ? param.value : '';
        
        if (finalBaseUrl.includes(placeholder)) {
          if (paramValue) {
            finalBaseUrl = finalBaseUrl.replace(new RegExp(placeholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), paramValue);
          }
        }
        if (finalBaseUrl.includes(colonPlaceholder)) {
          if (paramValue) {
            finalBaseUrl = finalBaseUrl.replace(new RegExp(colonPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), paramValue);
          }
        }
      }
    });
    
    // Add remaining query params
    const queryParams = remainingParams
      .filter(p => p.enabled && p.key && p.key.trim() !== '')
      .map(p => `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value || '')}`)
      .join('&');
    
    const newUrl = queryParams ? `${finalBaseUrl}?${queryParams}` : finalBaseUrl;
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

    // CRITICAL: First, extract all environment variables ({{var}}) and replace them with placeholders
    // This ensures we don't treat them as path parameters
    const envVarPattern = /\{\{([^}]+)\}\}/g;
    const envVarMatches = [];
    let match;
    let processedUrl = decodedUrl;
    
    // Reset the regex index
    envVarPattern.lastIndex = 0;
    
    // Find all environment variables
    while ((match = envVarPattern.exec(decodedUrl)) !== null) {
      envVarMatches.push({
        full: match[0],
        name: match[1],
        index: match.index
      });
    }
    
    console.log('🌍 Found environment variables:', envVarMatches.map(m => m.name));
    
    // Replace environment variables with a temporary placeholder that won't be confused
    // We'll use a UUID-like pattern that won't be mistaken for a path parameter
    const envVarMap = new Map();
    envVarMatches.forEach((envVar, idx) => {
      const tempId = `__ENV_VAR_${idx}_${Date.now()}__`;
      envVarMap.set(tempId, envVar.full);
      processedUrl = processedUrl.replace(envVar.full, tempId);
    });
    
    // Now parse the URL with environment variables temporarily replaced
    let urlObj;
    let baseUrl = '';
    let path = '';
    let search = '';
    
    try {
      urlObj = new URL(processedUrl);
      baseUrl = `${urlObj.protocol}//${urlObj.host}`;
      path = urlObj.pathname;
      search = urlObj.search;
    } catch (e) {
      // If it's not a valid full URL, treat as path
      const [urlPath, queryString] = processedUrl.split('?');
      path = urlPath;
      search = queryString ? `?${queryString}` : '';
      baseUrl = '';
    }
    
    // Extract path segments
    const pathSegments = path.split('/').filter(segment => segment);
    const pathParams = [];
    const templateSegments = [];
    const envVarNames = envVarMatches.map(m => m.name);
    
    // Process each segment
    pathSegments.forEach((segment, index) => {
      // Check if this segment is a temporarily replaced environment variable
      if (segment.startsWith('__ENV_VAR_') && segment.endsWith('__')) {
        // Restore the original environment variable
        const originalEnvVar = envVarMap.get(segment);
        templateSegments.push(originalEnvVar);
        console.log(`🔄 Restored environment variable: ${originalEnvVar}`);
      }
      // Check for path parameters (single curly braces) - ONLY if it's {key} format
      else if (segment.startsWith('{') && segment.endsWith('}') && !segment.startsWith('{{')) {
        const key = segment.substring(1, segment.length - 1);
        
        // Try to find an existing param with the same key
        const existingParam = existingPathParams.find(p => p.key === key);
        
        pathParams.push({
          id: existingParam?.id || `path-${Date.now()}-${index}-${Math.random()}`,
          key: key,
          value: existingParam?.value || '',
          description: existingParam?.description || `Path parameter: ${key}`,
          enabled: true,
          required: true,
          position: index
        });
        
        templateSegments.push(`{${key}}`);
        console.log(`🛣️ Found path parameter: ${key}`);
      }
      // Check for colon-style placeholders
      else if (segment.startsWith(':')) {
        const key = segment.substring(1);
        
        const existingParam = existingPathParams.find(p => p.key === key);
        
        pathParams.push({
          id: existingParam?.id || `path-${Date.now()}-${index}-${Math.random()}`,
          key: key,
          value: existingParam?.value || '',
          description: existingParam?.description || `Path parameter: ${key}`,
          enabled: true,
          required: true,
          position: index
        });
        
        templateSegments.push(`{${key}}`);
        console.log(`🛣️ Found colon path parameter: ${key}`);
      }
      else {
        // Regular path segment
        templateSegments.push(segment);
      }
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
    
    console.log('✅ Final parsed results:', {
      templateUrl: templateBase || url,
      pathParamsCount: pathParams.length,
      queryParamsCount: queryParams.length,
      envVarNamesCount: envVarNames.length
    });
    
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
const isUserInitiatedTabChange = useRef(false);
const skipAutoTabChange = useRef(false);
const isManualUrlUpdate = useRef(false);
const isProcessingUrlUpdate = useRef(false);
const lastProcessedUrlRef = useRef('');
const lastPathParamsHashRef = useRef('');
const initialLoadCompleteRef = useRef(false);
const isEnvironmentUpdating = useRef(false); // NEW: Track environment updates
const lastEnvironmentUrlRef = useRef(''); // NEW: Track last environment-processed URL
const isTabAutoSwitching = useRef(false); // NEW: Track tab auto-switching

const handleUrlChange = useCallback((e) => {
  // Get the current cursor position
  const cursorPos = e.target.selectionStart;
  
  // Start batch update
  isBatchUpdating.current = true;
  
  // Mark that this is a manual URL update
  isManualUrlUpdate.current = true;
  
  // Set flag that this update is from user input
  isUpdatingFromInput.current = true;
  
  const newUrl = e.target.value;
  
  // Only update if different from current URL
  if (newUrl !== requestUrl) {
    // Clear any pending environment flag
    isEnvironmentUpdating.current = false;
    
    // Parse the URL to extract parameters - but preserve the template
    const parsed = parseUrlIntoTemplateAndParams(newUrl, requestPathParams);
    
    // Update template URL with placeholders
    setTemplateUrl(parsed.templateUrl);
    
    // Update path params from parsed data - preserve existing values
    if (parsed.pathParams.length > 0) {
      setRequestPathParams(prevParams => {
        const mergedParams = parsed.pathParams.map(newParam => {
          const existing = prevParams.find(p => p.key === newParam.key);
          if (existing) {
            // Keep the existing value if it exists
            return {
              ...newParam,
              value: existing.value || newParam.value
            };
          }
          return newParam;
        });
        return mergedParams;
      });
    }
    
    // Update query params from parsed data
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
      // If no query params in the new URL, clear them
      setRequestParams([]);
    }
    
    // Set the URL
    setRequestUrl(newUrl);
  }
  
  // Restore cursor position after React renders
  requestAnimationFrame(() => {
    if (urlInputRef.current && isUpdatingFromInput.current) {
      urlInputRef.current.selectionStart = cursorPos;
      urlInputRef.current.selectionEnd = cursorPos;
      isUpdatingFromInput.current = false;
    }
  });
  
  // Reset flags after a delay
  setTimeout(() => {
    isManualUrlUpdate.current = false;
    isBatchUpdating.current = false;
  }, 500);
  
}, [parseUrlIntoTemplateAndParams, requestPathParams, requestUrl]);



// Add this helper function near your other utility functions
const buildCompleteUrl = useCallback((baseTemplate, pathParams, queryParams) => {
  if (!baseTemplate) return '';
  
  // Start with the template URL
  let url = baseTemplate;
  
  // Replace path parameters with their values
  pathParams.forEach(param => {
    if (param.enabled && param.key && param.value && param.value.trim() !== '') {
      const placeholder = `{${param.key}}`;
      const colonPlaceholder = `:${param.key}`;
      
      if (url.includes(placeholder)) {
        url = url.replace(new RegExp(placeholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), param.value);
      }
      if (url.includes(colonPlaceholder)) {
        url = url.replace(new RegExp(colonPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), param.value);
      }
    }
  });
  
  // Add query parameters
  const activeQueryParams = queryParams.filter(p => p.enabled && p.key && p.key.trim() !== '');
  if (activeQueryParams.length > 0) {
    const queryString = activeQueryParams
      .map(p => `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value || '')}`)
      .join('&');
    url = `${url}?${queryString}`;
  }
  
  return url;
}, []);


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

const updatePathParam = (id, field, value) => {
  console.log('🟢 updatePathParam called:', { id, field, value });
  
  // Prevent updates during initial load
  if (!initialLoadCompleteRef.current) {
    console.log('⏭️ Skipping update during initial load');
    return;
  }
  
  // Mark that this is a manual URL update
  isManualUrlUpdate.current = true;
  isUpdatingFromInput.current = true;
  
  // First update the state with the new value
  setRequestPathParams(params => {
    const updatedParams = params.map(param => 
      param.id === id ? { ...param, [field]: value } : param
    );
    
    // Only update URL if we're changing the value
    if (field === 'value') {
      // CRITICAL: Use the TEMPLATE URL (which still has placeholders) as the base
      // NOT the current URL which already has replaced placeholders
      let templateBase = templateUrl;
      
      // If no template URL, fall back to current URL without query string
      if (!templateBase) {
        templateBase = requestUrl.split('?')[0];
      }
      
      // Get the query string from the current URL
      const queryString = requestUrl.includes('?') ? requestUrl.split('?')[1] : '';
      
      console.log('📝 Template base URL (with placeholders):', templateBase);
      
      // Replace ALL path parameters with their current values
      let newBasePath = templateBase;
      updatedParams.forEach(param => {
        if (param.enabled && param.key) {
          const placeholder = `{${param.key}}`;
          const colonPlaceholder = `:${param.key}`;
          
          // Use the current value from updatedParams (or keep placeholder if empty)
          const paramValue = param.value && param.value.trim() !== '' ? param.value : placeholder;
          
          console.log(`  🔄 Replacing ${placeholder} with ${paramValue}`);
          
          // Replace in the base path
          if (newBasePath.includes(placeholder)) {
            newBasePath = newBasePath.replace(new RegExp(placeholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), paramValue);
          }
          if (newBasePath.includes(colonPlaceholder)) {
            newBasePath = newBasePath.replace(new RegExp(colonPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), paramValue);
          }
        }
      });
      
      // Rebuild URL with query string if it exists
      const finalUrl = queryString ? `${newBasePath}?${queryString}` : newBasePath;
      
      // Only update if URL actually changed
      if (finalUrl !== requestUrl) {
        console.log('✅ Final new URL:', finalUrl);
        if (lastProcessedUrlRef.current !== finalUrl) {
          lastProcessedUrlRef.current = finalUrl;
          setRequestUrl(finalUrl);
        }
      } else {
        console.log('⚠️ URL unchanged - current:', requestUrl, 'new:', finalUrl);
      }
    }
    
    return updatedParams;
  });
  
  // Reset the manual update flag after a delay
  setTimeout(() => {
    isUpdatingFromInput.current = false;
    isManualUrlUpdate.current = false;
  }, 150);
};


// Add this useEffect after your other useEffects to sync query params on load
useEffect(() => {
  if (selectedRequest && !loading.request && requestParams.length > 0) {
    // Ensure query parameters are reflected in the URL when request loads
    const hasQueryString = requestUrl.includes('?');
    const currentQueryString = hasQueryString ? requestUrl.split('?')[1] : '';
    
    // Build query string from params
    const newQueryString = requestParams
      .filter(p => p.enabled && p.key && p.key.trim() !== '')
      .map(p => `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value || '')}`)
      .join('&');
    
    // If there are query params and they're not in the URL, add them
    if (newQueryString && !currentQueryString) {
      const baseUrl = requestUrl.split('?')[0];
      const newUrl = `${baseUrl}?${newQueryString}`;
      console.log('🔄 Adding query params to URL on load:', newUrl);
      setRequestUrl(newUrl);
    }
  }
}, [selectedRequest, loading.request, requestParams, requestUrl]);


// Add this useEffect in your Collections component (around line 2500-2600, after your existing useEffects)
useEffect(() => {
  // Auto-set body type based on HTTP method
  const method = requestMethod;
  
  if (method === 'GET' || method === 'DELETE') {
    // For GET and DELETE, set body type to 'none'
    setRequestBodyType('none');
    
    // Also clear any existing body data when switching to GET/DELETE
    setRequestBody('');
    setFormData([]);
    setUrlEncodedData([]);
    setBinaryFile(null);
    setGraphqlQuery('');
    setGraphqlVariables('');
    
    console.log('🔄 Auto-set body type to none for', method);
  } else {
    // For POST, PUT, PATCH, etc., set body type to 'raw' or 'json'
    // Check if there's already a body type set, if not default to 'raw'
    setRequestBodyType(prev => {
      // If previous was 'none' or not set, default to 'raw'
      if (prev === 'none' || !prev) {
        return 'raw';
      }
      return prev;
    });
    
    console.log('🔄 Auto-set body type for', method);
  }
}, [requestMethod]); // Run whenever HTTP method changes


// Update the URL processing useEffect
useEffect(() => {
  let rafId;
  
  // Skip if we're already processing or this is a manual update
  if (isUpdatingFromInput.current || isManualUrlUpdate.current) {
    return;
  }
  
  // Skip during initial load
  if (!initialLoadCompleteRef.current && loading.request) {
    return;
  }
  
  // Skip if no URL
  if (!requestUrl) {
    return;
  }
  
  // Mark that we're processing
  isProcessingUrlUpdate.current = true;
  
  // Use requestAnimationFrame for smoother updates
  rafId = requestAnimationFrame(() => {
    try {
      // Start batch update
      isBatchUpdating.current = true;
      
      // Get the base URL (without query string)
      const [basePath] = requestUrl.split('?');
      let newBasePath = basePath;
      let hasChanges = false;
      
      // Replace path parameters in the CURRENT URL (which already has env vars resolved)
      requestPathParams.forEach(param => {
        if (param.enabled && param.key) {
          const placeholder = `{${param.key}}`;
          const colonPlaceholder = `:${param.key}`;
          
          // Check if the placeholder exists in the current URL
          if (newBasePath.includes(placeholder) || newBasePath.includes(colonPlaceholder)) {
            const paramValue = param.value && param.value.trim() !== '' ? param.value : placeholder;
            const regex = new RegExp(placeholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g');
            const colonRegex = new RegExp(colonPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g');
            
            newBasePath = newBasePath.replace(regex, paramValue);
            newBasePath = newBasePath.replace(colonRegex, paramValue);
            hasChanges = true;
          }
        }
      });
      
      // Only update if the base URL changed
      if (hasChanges && newBasePath !== basePath) {
        console.log('🔄 URL processing effect updating URL (path params changed):', newBasePath);
        
        // Preserve query string if it exists
        const queryString = requestUrl.includes('?') ? requestUrl.split('?')[1] : '';
        const finalUrl = queryString ? `${newBasePath}?${queryString}` : newBasePath;
        
        if (finalUrl !== requestUrl) {
          setRequestUrl(finalUrl);
        }
      }
      
    } catch (error) {
      console.error('Error in URL processing effect:', error);
    } finally {
      setTimeout(() => {
        isProcessingUrlUpdate.current = false;
        isBatchUpdating.current = false;
      }, 50);
    }
  });
  
  return () => {
    if (rafId) {
      cancelAnimationFrame(rafId);
    }
    isProcessingUrlUpdate.current = false;
  };
}, [
  JSON.stringify(requestPathParams.map(p => ({ key: p.key, value: p.value, enabled: p.enabled }))),
  requestUrl,
  loading.request
]);

// Add a ref to track user interaction
const lastUserInteractionRef = useRef(null);

// Update determineActiveTab to use memoized values
const determineActiveTab = useCallback(() => {
  // Skip during auto-switching to prevent loops
  if (isTabAutoSwitching.current) {
    return activeTab;
  }
  
  // If user has manually selected a tab, don't auto-switch
  if (skipAutoTabChange.current) {
    return activeTab;
  }
  
  // Skip during initial load or request loading
  if (loading.request || loading.initialLoad) {
    return activeTab;
  }
  
  // Use memoized checks to prevent recalculation on every render
  let hasPathParamsWithValues = false;
  let hasQueryParamsWithValues = false;
  let hasBodyContent = false;
  
  // Only calculate if we have data - use memoizedRequestPathParams
  if (memoizedRequestPathParams && memoizedRequestPathParams.length > 0) {
    hasPathParamsWithValues = memoizedRequestPathParams.some(p => 
      p.enabled && p.key && p.key.trim() !== '' && p.value && p.value.trim() !== ''
    );
  }
  
  if (memoizedRequestParams && memoizedRequestParams.length > 0) {
    hasQueryParamsWithValues = memoizedRequestParams.some(p => 
      p.enabled && p.key && p.key.trim() !== '' && p.value && p.value.trim() !== ''
    );
  }
  
  if (requestBody && requestBodyType !== 'none') {
    hasBodyContent = requestBody.trim() !== '' && 
      requestBody !== '{}' && 
      requestBody !== '{\n  \n}';
  }
  
  // Priority based on actual content
  let newTab = activeTab;
  if (hasPathParamsWithValues) {
    newTab = 'path-params';
  } else if (hasQueryParamsWithValues) {
    newTab = 'query-params';
  } else if (hasBodyContent) {
    newTab = 'body';
  } else {
    newTab = 'path-params';
  }
  
  // Only log if actually changing
  if (newTab !== activeTab) {
    console.log('🎯 Auto-switching tab from', activeTab, 'to', newTab);
  }
  
  return newTab;
}, [
  memoizedRequestPathParams,  // Use memoized version
  memoizedRequestParams,      // Use memoized version
  requestBody, 
  requestBodyType, 
  loading.request, 
  loading.initialLoad, 
  activeTab
]);

useEffect(() => {
  let timeoutId;
  
  // Skip if environment update is already in progress
  if (isEnvironmentUpdating.current) {
    console.log('🌍 Skipping environment update - already in progress');
    return;
  }
  
  // Skip during initial load
  if (!initialLoadCompleteRef.current && loading.request) {
    console.log('🌍 Skipping environment update - initial load in progress');
    return;
  }
  
  // Skip if no URL or no selected request
  if (!selectedRequest || !selectedRequest.url || !environments || !activeEnvironment) {
    return;
  }
  
  const templateUrlWithPlaceholders = selectedRequest.url;
  const hasEnvPlaceholders = /\{\{[^}]+\}\}/.test(templateUrlWithPlaceholders);
  
  if (!hasEnvPlaceholders) {
    return;
  }
  
  // Skip if manual update or batch update in progress
  if (isManualUrlUpdate.current || isBatchUpdating.current || isUpdatingFromInput.current) {
    console.log('🌍 Skipping environment update - manual update in progress');
    return;
  }
  
  // Skip if URL already has environment variables resolved (no placeholders)
  if (!requestUrl.includes('{{')) {
    console.log('🌍 Skipping environment update - URL already resolved');
    return;
  }
  
  // Debounce the environment update
  timeoutId = setTimeout(() => {
    // Get the processed URL with new environment variables
    let processedUrl = templateUrlWithPlaceholders;
    
    // Find the active environment
    const activeEnv = environments.find(env => env.id === activeEnvironment);
    if (!activeEnv || !activeEnv.variables) {
      return;
    }
    
    // Create environment variable map
    const envVarMap = new Map();
    activeEnv.variables.forEach(variable => {
      if (variable.enabled && variable.key) {
        envVarMap.set(variable.key, variable.value);
      }
    });
    
    // Replace environment variables
    const placeholderRegex = /\{\{([^}]+)\}\}/g;
    let match;
    let hasReplacements = false;
    
    while ((match = placeholderRegex.exec(templateUrlWithPlaceholders)) !== null) {
      const fullPlaceholder = match[0];
      const varName = match[1];
      const varValue = envVarMap.get(varName);
      
      if (varValue !== undefined) {
        processedUrl = processedUrl.replace(new RegExp(fullPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), varValue);
        hasReplacements = true;
      }
    }
    
    // Check if the processed URL is different from current and not already processed
    if (hasReplacements && processedUrl !== requestUrl && processedUrl !== lastEnvironmentUrlRef.current) {
      console.log('🌍 Environment changed, updating URL from:', requestUrl, 'to:', processedUrl);
      
      // Mark that we're updating
      isEnvironmentUpdating.current = true;
      lastEnvironmentUrlRef.current = processedUrl;
      
      // CRITICAL: Update BOTH requestUrl AND templateUrl
      setRequestUrl(processedUrl);
      setTemplateUrl(processedUrl);
      
      // Reset flag after a delay
      setTimeout(() => {
        isEnvironmentUpdating.current = false;
      }, 500);
    }
  }, 100);
  
  return () => {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
  };
}, [activeEnvironment, environments, selectedRequest, requestUrl, loading.request]);

// Add this function near your other handlers
const handleTabClick = useCallback((tabId) => {
  // Mark that this is a user-initiated tab change
  isUserInitiatedTabChange.current = true;
  skipAutoTabChange.current = true;
  
  // Clear any pending auto-switch
  isTabAutoSwitching.current = false;
  
  // Update active tab
  setActiveTab(tabId);
  
  console.log('👆 User clicked tab:', tabId);
  
  // Reset the flags after a delay to allow React to process
  setTimeout(() => {
    isUserInitiatedTabChange.current = false;
    // Don't reset skipAutoTabChange here - keep it true until we explicitly reset
  }, 500);
}, []);

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

// Add this effect to track when initial load is complete
useEffect(() => {
  if (!loading.request && !loading.initialLoad && !initialLoadCompleteRef.current) {
    // Small delay to ensure all states are settled
    setTimeout(() => {
      initialLoadCompleteRef.current = true;
      console.log('✅ Initial load completed');
    }, 500);
  }
}, [loading.request, loading.initialLoad]);


useEffect(() => {
  return () => {
    // Reset all flags on unmount
    isUpdatingFromInput.current = false;
    isManualUrlUpdate.current = false;
    isProcessingUrlUpdate.current = false;
    isUserInitiatedTabChange.current = false;
    skipAutoTabChange.current = false;
    isEnvironmentUpdating.current = false;
    isTabAutoSwitching.current = false;
  };
}, []);


useEffect(() => {
  // Only set active tab after initial load is complete and not during loading
  if (selectedRequest && !loading.request && !loading.initialLoad && initialLoadCompleteRef.current) {
    // Don't auto-switch if user manually selected a tab
    if (!skipAutoTabChange.current && !isTabAutoSwitching.current) {
      const activeTabToSet = determineActiveTab();
      if (activeTabToSet !== activeTab) {
        console.log('🎯 [After Load Complete] Setting active tab to:', activeTabToSet);
        
        // Mark that we're auto-switching
        isTabAutoSwitching.current = true;
        setActiveTab(activeTabToSet);
        
        // Reset flag after a delay
        setTimeout(() => {
          isTabAutoSwitching.current = false;
        }, 300);
      }
    }
  }
}, [selectedRequest, loading.request, loading.initialLoad, determineActiveTab, activeTab]);


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


// Add this utility function for copying text
const copyToClipboard = useCallback((text, showToastFn) => {
  if (!text) {
    showToastFn('Nothing to copy', 'warning');
    return;
  }
  
  // Modern clipboard API
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(text)
      .then(() => {
        showToastFn('Copied to clipboard!', 'success');
      })
      .catch((err) => {
        console.error('Clipboard write failed:', err);
        // Fallback to older method
        fallbackCopy(text, showToastFn);
      });
  } else {
    // Fallback for older browsers
    fallbackCopy(text, showToastFn);
  }
  
  function fallbackCopy(text, showToastFn) {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.top = '-9999px';
    textarea.style.left = '-9999px';
    document.body.appendChild(textarea);
    textarea.focus();
    textarea.select();
    
    try {
      const successful = document.execCommand('copy');
      if (successful) {
        showToastFn('Copied to clipboard!', 'success');
      } else {
        showToastFn('Failed to copy to clipboard', 'error');
      }
    } catch (err) {
      console.error('Fallback copy failed:', err);
      showToastFn('Failed to copy to clipboard', 'error');
    }
    
    document.body.removeChild(textarea);
  }
}, []);

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

  
 // Add this helper function near the top of your component, after the imports
  const sortAlphabetically = (items) => {
    if (!items || !Array.isArray(items)) return items;
    return [...items].sort((a, b) => {
      const nameA = (a.name || '').toLowerCase();
      const nameB = (b.name || '').toLowerCase();
      if (nameA < nameB) return -1;
      if (nameA > nameB) return 1;
      return 0;
    });
  };

  const transformCollectionsData = (apiData) => {
  console.log('🔄 [Transform] Input:', apiData);
  
  if (!apiData) {
    console.warn('⚠️ [Transform] No data provided');
    return [];
  }
  
  let collectionsArray = [];
  
  // Handle different data structures
  if (Array.isArray(apiData)) {
    collectionsArray = apiData;
  } else if (apiData.collections && Array.isArray(apiData.collections)) {
    collectionsArray = apiData.collections;
  } else if (apiData.data && apiData.data.collections && Array.isArray(apiData.data.collections)) {
    collectionsArray = apiData.data.collections;
  } else if (apiData.data && Array.isArray(apiData.data)) {
    collectionsArray = apiData.data;
  } else {
    console.warn('⚠️ [Transform] Unknown data structure:', apiData);
    return [];
  }
  
  console.log(`📊 [Transform] Processing ${collectionsArray.length} collections`);
  
  // Sort collections alphabetically
  const sortedCollections = sortAlphabetically(collectionsArray);
  
  return sortedCollections.map((collection, collectionIndex) => {
    console.log(`📁 [Transform] Processing collection: ${collection.name || 'Unnamed'}`, {
      id: collection.id,
      hasFolders: !!(collection.folders || collection.data?.folders),
      foldersCount: (collection.folders || collection.data?.folders || []).length
    });
    
    // CRITICAL FIX: Handle folders from both top-level and nested data structure
    let foldersData = collection.folders || [];
    
    // Check if folders are inside a nested data object
    if (!foldersData.length && collection.data && collection.data.folders) {
      foldersData = collection.data.folders;
      console.log(`📁 [Transform] Found folders in collection.data.folders: ${foldersData.length}`);
    }
    
    // Process folders and their requests - sort folders alphabetically
    const folders = foldersData || [];
    const sortedFolders = sortAlphabetically(folders);
    
    const processedFolders = sortedFolders.map((folder, folderIndex) => {
      console.log(`📂 [Transform] Processing folder: ${folder.name || 'Unnamed'}`, {
        id: folder.id,
        hasRequests: !!(folder.requests || folder.data?.requests),
        requestsCount: (folder.requests || folder.data?.requests || []).length
      });
      
      // CRITICAL FIX: Handle requests from both top-level and nested data structure
      let requestsData = folder.requests || [];
      
      // Check if requests are inside a nested data object
      if (!requestsData.length && folder.data && folder.data.requests) {
        requestsData = folder.data.requests;
        console.log(`📋 [Transform] Found requests in folder.data.requests: ${requestsData.length}`);
      }
      
      // Process requests within folder - sort requests alphabetically
      const requests = requestsData || [];
      const sortedRequests = sortAlphabetically(requests);
      
      const processedRequests = sortedRequests.map((request, requestIndex) => {
        // Separate parameters based on parameterLocation
        const queryParams = [];
        const pathParams = [];
        const headerParams = [];
        const bodyParams = [];
        
        console.log(`📋 [Transform] Processing request: ${request.name || 'Unnamed'}`, {
          parameters: request.parameters?.length || 0,
          requestBody: request.requestBody ? 'present' : 'absent',
          hasUrl: !!request.url
        });
        
        // Process parameters with location info
        (request.parameters || []).forEach((param, paramIdx) => {
          if (param && param.key) {
            const paramId = param.id || `param-${Date.now()}-${Math.random()}-${paramIdx}`;
            
            // Clean placeholder values
            let paramValue = param.value || '';
            const hasPlaceholder = paramValue.includes('{') || paramValue.includes('}') || 
                                  paramValue.includes('%7B') || paramValue.includes('%7D');
            
            if (hasPlaceholder) {
              console.log(`🧹 Cleaning placeholder value for param ${param.key}: ${paramValue} -> ''`);
              paramValue = '';
            }
            
            const paramObject = {
              id: paramId,
              key: param.key,
              value: paramValue,
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
            
            // Sort by location - CRITICAL for path params
            switch(param.parameterLocation?.toLowerCase()) {
              case 'query':
                queryParams.push(paramObject);
                console.log(`🔍 [Transform] Query param: ${param.key}`);
                break;
              case 'path':
                pathParams.push(paramObject);
                console.log(`🛣️ [Transform] Path param: ${param.key}`);
                break;
              case 'header':
                headerParams.push(paramObject);
                console.log(`📋 [Transform] Header param: ${param.key}`);
                break;
              case 'body':
                // Body parameters - store but don't add to URL
                bodyParams.push(paramObject);
                console.log(`📦 [Transform] Body param: ${param.key} - will be in request body, not URL`);
                break;
              default:
                // If no location specified, check if it's a POST/PUT/PATCH request
                if (request.method === 'POST' || request.method === 'PUT' || request.method === 'PATCH') {
                  bodyParams.push(paramObject);
                  console.log(`📦 [Transform] Default body param: ${param.key}`);
                } else {
                  queryParams.push(paramObject);
                  console.log(`🔍 [Transform] Default query param: ${param.key}`);
                }
            }
          }
        });
        
        // Process headers separately
        const headers = (request.headers || []).map((header, headerIdx) => ({
          id: header.id || `header-${Date.now()}-${Math.random()}-${headerIdx}`,
          key: header.key,
          value: header.value || '',
          description: header.description || '',
          enabled: header.enabled !== false,
          required: header.required || false
        }));
        
        console.log(`📊 [Transform] Request "${request.name || 'Unnamed'}" - Query: ${queryParams.length}, Path: ${pathParams.length}, Header Params: ${headerParams.length}, Body: ${bodyParams.length}, Regular Headers: ${headers.length}`);
        
        // Process auth info
        const authType = request.authType || request.auth?.type || 'noauth';
        let processedAuthConfig = { type: authType };
        
        if (authType === 'apikey') {
          const sourceConfig = request.authConfig || request.auth || {};
          processedAuthConfig = {
            type: 'apikey',
            key: sourceConfig.key || sourceConfig.apiKey || sourceConfig.apiKeyHeader || '',
            value: sourceConfig.value || sourceConfig.apiSecret || sourceConfig.apiKeyValue || sourceConfig.secret || '',
            addTo: sourceConfig.addTo || 'header'
          };
        } 
        else if (authType === 'bearer') {
          const sourceConfig = request.authConfig || request.auth || {};
          processedAuthConfig = {
            type: 'bearer',
            token: sourceConfig.token || sourceConfig.bearerToken || '',
            tokenType: sourceConfig.tokenType || 'Bearer'
          };
        } 
        else if (authType === 'basic') {
          const sourceConfig = request.authConfig || request.auth || {};
          processedAuthConfig = {
            type: 'basic',
            username: sourceConfig.username || '',
            password: sourceConfig.password || ''
          };
        } 
        else if (authType === 'oauth2') {
          const sourceConfig = request.authConfig || request.auth || {};
          processedAuthConfig = {
            type: 'oauth2',
            token: sourceConfig.token || sourceConfig.jwtToken || '',
            jwtToken: sourceConfig.jwtToken || sourceConfig.token || ''
          };
        }
        
        // Process body content
        let bodyContent = request.body || '';
        let requestBodyData = request.requestBody || null;
        
        if (requestBodyData && requestBodyData.sample) {
          bodyContent = requestBodyData.sample;
        }
        
        // CRITICAL FIX: Construct URL with path parameter placeholders
        let requestUrl = request.url || '';
        
        if (pathParams.length > 0) {
          console.log(`🛣️ [Transform] Building URL with ${pathParams.length} path params`);
          
          const hasPlaceholders = pathParams.some(p => 
            requestUrl.includes(`{${p.key}}`) || requestUrl.includes(`:${p.key}`)
          );
          
          if (!hasPlaceholders && !requestUrl.includes('{') && !requestUrl.includes(':')) {
            if (!requestUrl.endsWith('/')) {
              requestUrl += '/';
            }
            
            pathParams.forEach((param, index) => {
              if (index === 0 && requestUrl.endsWith('/')) {
                requestUrl += `{${param.key}}`;
              } else if (index === 0) {
                requestUrl += `/{${param.key}}`;
              } else {
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
          headers: cleanHeaders([...(request.headers || [])]),
          queryParams: queryParams,
          pathParams: pathParams,
          headerParams: headerParams,
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
        requestCount: folder.requestCount || processedRequests.length,
        requests: processedRequests
      };
    });
    
    const totalRequests = processedFolders.reduce((sum, folder) => sum + (folder.requests?.length || 0), 0);
    
    return {
      id: collection.id || `col-${Date.now()}-${Math.random()}-${collectionIndex}`,
      name: collection.name || `Collection ${collectionIndex + 1}`,
      description: collection.description || '',
      isExpanded: collectionIndex === 0,
      isFavorite: collection.favorite || false,
      isEditing: false,
      createdAt: collection.createdAt || new Date().toISOString(),
      requestsCount: totalRequests,
      folderCount: processedFolders.length,
      variables: (collection.variables || []).map((v, varIndex) => ({
        id: v.id || `var-${Date.now()}-${Math.random()}-${varIndex}`,
        key: v.key,
        value: v.value,
        type: v.type || 'text',
        enabled: v.enabled !== false
      })),
      folders: processedFolders
    };
  });
};

 // Helper function to check if a collection or folder has any endpoints (requests)
const hasEndpoints = useCallback((item) => {
  // Check if it's a folder with requests
  if (item.requests && Array.isArray(item.requests) && item.requests.length > 0) {
    return true;
  }
  
  // Check if it's a collection with folders that have requests
  if (item.folders && Array.isArray(item.folders)) {
    return item.folders.some(folder => hasEndpoints(folder));
  }
  
  return false;
}, []);

// Updated renderCollectionsTree function - hides collections/folders without endpoints
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

  // Filter collections to only those with endpoints
  const collectionsWithEndpoints = filteredCollections.filter(collection => hasEndpoints(collection));

  if (collectionsWithEndpoints.length === 0) {
    return (
      <div className="p-4 text-center">
        <Folder size={48} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mx-auto mb-4" />
        <p className="text-sm" style={{ color: colors.text }}>No collections with endpoints found</p>
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

  // Ensure collections are sorted before rendering
  const sortedCollections = sortAlphabetically(collectionsWithEndpoints);
  
  return (
    <div className="flex-1 overflow-auto p-2">
      {sortedCollections.map(collection => {
        // Filter folders to only those with requests
        const foldersWithEndpoints = (collection.folders || []).filter(folder => hasEndpoints(folder));
        
        // Skip rendering collection if no folders with endpoints after filtering
        if (foldersWithEndpoints.length === 0 && !hasEndpoints(collection)) {
          return null;
        }
        
        return (
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
              
              {/* Collection count badge - show total requests in collection */}
              {collection.requestsCount > 0 && (
                <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                  backgroundColor: colors.primaryDark,
                  color: 'white'
                }}>
                  {collection.requestsCount}
                </span>
              )}
            </div>

            {collection.isExpanded && foldersWithEndpoints.length > 0 && (
              <>
                {/* Ensure folders are sorted before rendering */}
                {sortAlphabetically(foldersWithEndpoints).map(folder => {
                  // Skip rendering folder if it has no requests
                  if (!folder.requests || folder.requests.length === 0) {
                    return null;
                  }
                  
                  return (
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
                        
                        {/* Folder count badge - show number of requests in this folder */}
                        {folder.requests && folder.requests.length > 0 && (
                          <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                            backgroundColor: colors.primaryDark,
                            color: 'white'
                          }}>
                            {folder.requests.length}
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
                          {/* Ensure requests are sorted before rendering */}
                          {sortAlphabetically(folder.requests).map(request => (
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
                  );
                })}
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
            
            {/* Show message when collection has no folders with endpoints */}
            {collection.isExpanded && foldersWithEndpoints.length === 0 && (
              <div className="ml-4 py-2 text-center">
                <p className="text-xs" style={{ color: colors.textTertiary }}>No endpoints in this collection</p>
                <button
                  type="button"
                  onClick={() => addNewFolder(collection.id)}
                  className="mt-1 px-3 py-1 text-xs rounded hover:bg-opacity-50 transition-colors flex items-center gap-1.5 hover-lift"
                  style={{ backgroundColor: colors.hover, color: colors.textSecondary }}
                  disabled={loading.request}
                >
                  <Plus size={10} />
                  Add your first folder
                </button>
              </div>
            )}
          </div>
        );
      })}
      
      {sortedCollections.length === 0 && searchQuery && (
        <div className="text-center p-4" style={{ color: colors.textSecondary }}>
          <Search size={20} className="mx-auto mb-2 opacity-50" />
          <p className="text-sm">No collections found for "{searchQuery}"</p>
        </div>
      )}
    </div>
  );
};

// Add this function to replace environment variables in a URL
const replaceEnvironmentVariables = useCallback((url, environmentsList, activeEnvId) => {
  if (!url || !environmentsList || environmentsList.length === 0 || !activeEnvId) {
    return url;
  }
  
  // Find the active environment
  const activeEnvironment = environmentsList.find(env => env.id === activeEnvId);
  if (!activeEnvironment || !activeEnvironment.variables || activeEnvironment.variables.length === 0) {
    return url;
  }
  
  // Create a map of environment variables
  const envVarMap = new Map();
  activeEnvironment.variables.forEach(variable => {
    if (variable.enabled && variable.key) {
      envVarMap.set(variable.key, variable.value);
    }
  });
  
  // Replace all {{variable}} placeholders in the URL
  let processedUrl = url;
  const placeholderRegex = /\{\{([^}]+)\}\}/g;
  let match;
  let hasReplacements = false;
  
  while ((match = placeholderRegex.exec(url)) !== null) {
    const fullPlaceholder = match[0];
    const varName = match[1];
    const varValue = envVarMap.get(varName);
    
    if (varValue !== undefined) {
      processedUrl = processedUrl.replace(new RegExp(fullPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), varValue);
      hasReplacements = true;
      console.log(`🔄 Replaced environment variable: ${fullPlaceholder} → ${varValue}`);
    } else {
      console.log(`⚠️ Environment variable not found: ${fullPlaceholder}`);
    }
  }
  
  if (hasReplacements) {
    console.log(`✅ Final URL after environment replacement: ${processedUrl}`);
  }
  
  return processedUrl;
}, []);

// Add this helper function to resolve environment variables in URLs
const resolveEnvironmentVariables = useCallback((url, envVariables) => {
  if (!url || !envVariables || envVariables.length === 0) return url;
  
  console.log('🌍 Resolving environment variables in URL:', url);
  console.log('🌍 Available variables:', envVariables.map(v => ({ key: v.key, value: v.value, enabled: v.enabled })));
  
  // Pattern to match {{variableName}}
  const envVarPattern = /\{\{([^}]+)\}\}/g;
  let resolvedUrl = url;
  let match;
  
  // Create a map for quick lookup
  const envMap = new Map();
  envVariables.forEach(v => {
    if (v.enabled && v.key) {
      envMap.set(v.key, v.value);
      console.log(`📝 Mapped variable: {{${v.key}}} -> ${v.value}`);
    }
  });
  
  // Replace all environment variables
  while ((match = envVarPattern.exec(url)) !== null) {
    const varName = match[1];
    const envValue = envMap.get(varName);
    
    if (envValue) {
      resolvedUrl = resolvedUrl.replace(match[0], envValue);
      console.log(`🌍 Resolved environment variable {{${varName}}} to: ${envValue}`);
    } else {
      console.warn(`⚠️ Environment variable {{${varName}}} not found or disabled`);
    }
  }
  
  console.log('🌍 Final resolved URL:', resolvedUrl);
  return resolvedUrl;
}, []);

const handleSelectRequest = useCallback(async (request, collectionId, folderId) => {
  // Reset the auto-tab-change flag when loading a new request
  skipAutoTabChange.current = false;
  isUserInitiatedTabChange.current = false;
  
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

  // ========== Store the ORIGINAL template URL (with environment placeholders) ==========
  // This is important for future environment switching
  console.log('📝 Setting original templateUrl to:', initialTemplateUrl);
  setTemplateUrl(initialTemplateUrl);
  
  // ============== Resolve environment variables ==============
  let resolvedUrl = initialTemplateUrl;

  if (activeEnvironment && environments.length > 0) {
    const currentEnv = environments.find(env => env.id === activeEnvironment);
    if (currentEnv && currentEnv.variables && currentEnv.variables.length > 0) {
      console.log('🌍 Found active environment:', currentEnv.name);
      
      // Create a map of environment variables
      const envVarMap = new Map();
      currentEnv.variables.forEach(variable => {
        if (variable.enabled && variable.key) {
          envVarMap.set(variable.key, variable.value);
        }
      });
      
      // Replace ALL environment variables in the URL
      const envPattern = /\{\{([^}]+)\}\}/g;
      let match;
      let hasReplacements = false;
      
      while ((match = envPattern.exec(initialTemplateUrl)) !== null) {
        const fullPlaceholder = match[0];
        const varName = match[1];
        const varValue = envVarMap.get(varName);
        
        if (varValue !== undefined) {
          resolvedUrl = resolvedUrl.replace(new RegExp(fullPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), varValue);
          hasReplacements = true;
          console.log(`🌍 Replaced ${fullPlaceholder} with ${varValue}`);
        }
      }
      
      if (hasReplacements) {
        console.log('🌍 Final resolved URL:', resolvedUrl);
      }
    } else {
      console.log('⚠️ Active environment found but no variables available');
    }
  } else {
    console.log('⚠️ No active environment or environments not loaded yet');
  }
  
  // CRITICAL: Store the RESOLVED URL as the working template URL for path params
  // This ensures path params don't try to replace environment variables
  const workingTemplateUrl = resolvedUrl;
  console.log('📝 Working templateUrl (resolved):', workingTemplateUrl);
  
  // CRITICAL FIX: Update the actual templateUrl state to the resolved URL
  // This ensures that updatePathParam uses the correct base URL
  setTemplateUrl(workingTemplateUrl);

  // Set request body and other params
  setRequestBody(request.body || '');
  setRequestParams(request.queryParams || []);
  
  // ============== Path params extraction ==============
  // First, create a version of the URL without environment variables
  let urlWithoutEnvVars = initialTemplateUrl;
  // Remove all {{...}} patterns (environment variables)
  urlWithoutEnvVars = urlWithoutEnvVars.replace(/\{\{[^}]+\}\}/g, '');
  console.log('📝 URL without environment variables:', urlWithoutEnvVars);

  // Extract path params from the URL WITHOUT environment variables
  const pathParamsFromUrl = [];
  if (urlWithoutEnvVars) {
    // Find all {param} placeholders in URL - ONLY single curly braces
    const placeholderRegex = /{([^{}]+)}/g;
    let match;
    while ((match = placeholderRegex.exec(urlWithoutEnvVars)) !== null) {
      const paramName = match[1];
      console.log(`🔍 Found path param candidate: ${paramName}`);
      // Check if we already have this param in the request
      const existingParam = (request.pathParams || []).find(p => p.key === paramName);
      pathParamsFromUrl.push({
        id: existingParam?.id || `path-${Date.now()}-${paramName}-${Math.random()}`,
        key: paramName,
        value: existingParam?.value || '',
        description: existingParam?.description || `Path parameter: ${paramName}`,
        enabled: true,
        required: true
      });
    }
  }

  let initialPathParams = pathParamsFromUrl.length > 0 ? pathParamsFromUrl : [];

  if (request.pathParams && request.pathParams.length > 0) {
    request.pathParams.forEach(param => {
      if (!initialPathParams.some(p => p.key === param.key)) {
        initialPathParams.push({
          ...param,
          id: param.id || `path-${Date.now()}-${param.key}-${Math.random()}`
        });
      }
    });
  }

  setRequestPathParams(initialPathParams);

  // ============== Build the final URL with path params ==============
  // Start with the WORKING template URL (environment variables resolved)
  let finalUrlWithPathParams = workingTemplateUrl;
  
  // Replace path parameters in the resolved URL
  if (initialPathParams.length > 0) {
    console.log('🛣️ Processing path params for URL:', { workingTemplateUrl, initialPathParams });
    
    initialPathParams.forEach(param => {
      if (param.key && param.value && param.value.trim() !== '') {
        const placeholder = `{${param.key}}`;
        const colonPlaceholder = `:${param.key}`;
        
        if (finalUrlWithPathParams.includes(placeholder)) {
          finalUrlWithPathParams = finalUrlWithPathParams.replace(new RegExp(placeholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), param.value);
          console.log(`  🔄 Replaced ${placeholder} with ${param.value}`);
        }
        if (finalUrlWithPathParams.includes(colonPlaceholder)) {
          finalUrlWithPathParams = finalUrlWithPathParams.replace(new RegExp(colonPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), param.value);
          console.log(`  🔄 Replaced ${colonPlaceholder} with ${param.value}`);
        }
      }
    });
    
    console.log('✅ Final URL with path params:', finalUrlWithPathParams);
  }
  
  // Set the final URL
  setRequestUrl(finalUrlWithPathParams);
  
  // Then fetch additional details from API
  if (authToken && request.id && collectionId) {
    try {
      setLoading(prev => ({ ...prev, request: true }));
      
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
          authConfig: details.authConfig,
          headersCount: details.headers?.length,
          jwtToken: details.authConfig?.jwtToken ? 'present' : 'absent'
        });
        
        // ============== AUTH CONFIGURATION ==============
        let apiAuthHeaders = [];
        
        if (details.authConfig) {
          console.log('🔐 Full auth config from API:', details.authConfig);
          
          const apiAuthType = details.authConfig.authType || details.authType || request.authType;
          
          console.log('🔐 Detected auth type:', apiAuthType);
          
          if (apiAuthType === 'apikey' || apiAuthType === 'apiKey') {
            const apiKeyHeader = details.authConfig.apiKeyHeader || details.authConfig.key || '';
            const apiKeyValue = details.authConfig.apiKeyValue || details.authConfig.value || '';
            const apiSecretHeader = details.authConfig.apiSecretHeader || '';
            const apiSecretValue = details.authConfig.apiSecretValue || '';
            
            console.log('🔐 Extracted API Key config:', {
              apiKeyHeader,
              apiKeyValue: apiKeyValue ? '***' : '(empty)',
              apiSecretHeader,
              apiSecretValue: apiSecretValue ? '***' : '(empty)'
            });
            
            const apiProcessedConfig = {
              type: 'apikey',
              authType: 'apikey',
              key: apiKeyHeader,
              value: apiKeyValue,
              apiKeyHeader: apiKeyHeader,
              apiKeyValue: apiKeyValue,
              apiSecretHeader: apiSecretHeader,
              apiSecretValue: apiSecretValue,
              addTo: details.authConfig.addTo || 'header'
            };
            
            setAuthType('apikey');
            setAuthConfig(apiProcessedConfig);
            
            if (apiKeyHeader && apiKeyValue) {
              apiAuthHeaders.push({
                id: `auth-header-${Date.now()}-key`,
                key: apiKeyHeader,
                value: apiKeyValue,
                description: 'API Key authentication',
                enabled: true,
                required: true
              });
            }
            
            if (apiSecretHeader && apiSecretValue) {
              apiAuthHeaders.push({
                id: `auth-header-${Date.now()}-secret`,
                key: apiSecretHeader,
                value: apiSecretValue,
                description: 'API Secret authentication',
                enabled: true,
                required: true
              });
            }
          } else if (apiAuthType === 'bearer') {
            const apiProcessedConfig = {
              type: 'bearer',
              token: details.authConfig.token || details.authConfig.bearerToken || '',
              tokenType: details.authConfig.tokenType || 'Bearer'
            };
            setAuthType('bearer');
            setAuthConfig(apiProcessedConfig);
            
            if (apiProcessedConfig.token) {
              apiAuthHeaders.push({
                id: `auth-header-${Date.now()}`,
                key: 'Authorization',
                value: `${apiProcessedConfig.tokenType} ${apiProcessedConfig.token}`,
                description: 'Bearer token authentication',
                enabled: true,
                required: true
              });
            }
          } else if (apiAuthType === 'basic') {
            const apiProcessedConfig = {
              type: 'basic',
              username: details.authConfig.basicUsername || details.authConfig.username || '',
              password: details.authConfig.basicPassword || details.authConfig.password || '',
              realm: details.authConfig.basicRealm || ''
            };
            setAuthType('basic');
            setAuthConfig(apiProcessedConfig);
            
            if (apiProcessedConfig.username && apiProcessedConfig.password) {
              const credentials = btoa(`${apiProcessedConfig.username}:${apiProcessedConfig.password}`);
              apiAuthHeaders.push({
                id: `auth-header-${Date.now()}`,
                key: 'Authorization',
                value: `Basic ${credentials}`,
                description: 'Basic authentication',
                enabled: true,
                required: true
              });
            }
          } else if (apiAuthType === 'oauth2') {
            let jwtToken = details.authConfig.jwtToken || details.authConfig.token || details.authConfig.oauthToken || '';
            const apiProcessedConfig = {
              type: 'oauth2',
              token: jwtToken,
              tokenType: 'Bearer',
              jwtToken: jwtToken,
              jwtIssuer: details.authConfig.jwtIssuer || '',
              jwtAudience: details.authConfig.jwtAudience || '',
              jwtExpiration: details.authConfig.jwtExpiration || null
            };
            setAuthType('oauth2');
            setAuthConfig(apiProcessedConfig);
            
            if (jwtToken && jwtToken.trim() !== '') {
              apiAuthHeaders.push({
                id: `auth-header-${Date.now()}`,
                key: 'Authorization',
                value: `Bearer ${jwtToken}`,
                description: 'OAuth2/JWT token authentication',
                enabled: true,
                required: true
              });
            }
          }
        }
        
      // Process parameters
      if (details.parameters) {
        const queryParams = [];
        const pathParams = [];
        const headerParams = [];
        let bodyParams = []; // DECLARE THIS HERE
        
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
              parameterLocation: param.parameterLocation || 'query'
            };
            
            const location = (param.parameterLocation || '').toLowerCase();
            
            console.log(`📍 Parameter ${param.key} has location: ${location}`);
            
            if (location === 'path') {
              pathParams.push(paramObject);
              console.log(`🛣️ Added ${param.key} to PATH params`);
            } else if (location === 'query') {
              queryParams.push(paramObject);
              console.log(`🔍 Added ${param.key} to QUERY params`);
            } else if (location === 'header') {
              headerParams.push(paramObject);
              console.log(`📋 Added ${param.key} to HEADER params`);
            } else if (location === 'body') {
              bodyParams.push(paramObject);
              console.log(`📦 Added ${param.key} to BODY params (will NOT be in URL)`);
            } else {
              if (requestMethod === 'POST' || requestMethod === 'PUT' || requestMethod === 'PATCH') {
                bodyParams.push(paramObject);
                console.log(`📦 Default: ${param.key} added to BODY params (${requestMethod} request)`);
              } else {
                queryParams.push(paramObject);
                console.log(`🔍 Default: ${param.key} added to QUERY params (${requestMethod} request)`);
              }
            }
          }
        });
        
        // Only set query params (these go in the URL)
        if (queryParams.length > 0) {
          console.log('📝 Setting query params (will appear in URL):', queryParams.map(p => p.key));
          setRequestParams(queryParams);
        } else {
          console.log('✅ No query params - clearing requestParams');
          setRequestParams([]);
        }
        
        if (pathParams.length > 0 && initialPathParams.length === 0) {
          console.log('🛣️ Setting path params from API:', pathParams.map(p => p.key));
          setRequestPathParams(pathParams);
        }
        
        // Process headers
        const allHeaders = [...apiAuthHeaders];
        
        if (headerParams.length > 0) {
          headerParams.forEach(param => {
            allHeaders.push({
              id: param.id || `header-${Date.now()}-${Math.random()}`,
              key: param.key,
              value: param.value || '',
              description: param.description || '',
              enabled: param.enabled !== false,
              required: param.required || false
            });
          });
        }
        
        if (details.headers && details.headers.length > 0) {
          details.headers.forEach((header, idx) => {
            const isAuthHeader = allHeaders.some(h => h.key?.toLowerCase() === header.key?.toLowerCase());
            if (!isAuthHeader) {
              allHeaders.push({
                id: header.id || `header-${Date.now()}-${idx}-${Math.random()}`,
                key: header.key,
                value: header.value || '',
                description: header.description || '',
                enabled: header.enabled !== false,
                required: header.required || false
              });
            }
          });
        }
        
        // Remove duplicates
        const uniqueHeaders = [];
        const headerKeys = new Set();
        
        allHeaders.forEach(header => {
          const keyLower = header.key?.toLowerCase();
          if (keyLower && !headerKeys.has(keyLower)) {
            headerKeys.add(keyLower);
            uniqueHeaders.push(header);
          }
        });
        
        console.log('📌 Final headers:', uniqueHeaders.map(h => h.key));
        setRequestHeaders(uniqueHeaders);
        
        // Process request body - ONLY ONCE, right after processing parameters
        if (details.requestBody) {
          const bodyType = details.requestBody.bodyType || 'raw';
          
          console.log('📦 Processing request body:', {
            bodyType,
            sample: details.requestBody.sample
          });
          
          switch (bodyType) {
            case 'form-data':
              setRequestBodyType('form-data');
              if (bodyParams.length > 0) {
                const formDataArray = bodyParams.map((param, index) => ({
                  id: param.id || `form-${Date.now()}-${index}`,
                  key: param.key,
                  value: param.value || '',
                  type: 'text',
                  enabled: true,
                  description: param.description || ''
                }));
                setFormData(formDataArray);
              } else {
                setFormData([]);
              }
              setRequestBody('');
              break;
              
            case 'x-www-form-urlencoded':
              setRequestBodyType('x-www-form-urlencoded');
              if (bodyParams.length > 0) {
                const urlEncodedArray = bodyParams.map((param, index) => ({
                  id: param.id || `url-${Date.now()}-${index}`,
                  key: param.key,
                  value: param.value || '',
                  description: param.description || '',
                  enabled: true
                }));
                setUrlEncodedData(urlEncodedArray);
              } else {
                setUrlEncodedData([]);
              }
              setRequestBody('');
              break;
              
            case 'json':
            case 'raw':
              setRequestBodyType('raw');
              setRawBodyType('json');
              if (bodyParams.length > 0) {
                const jsonBody = {};
                bodyParams.forEach(param => {
                  if (param.key) {
                    let value = param.value || '';
                    if (value && (value.startsWith('{') || value.startsWith('['))) {
                      try {
                        value = JSON.parse(value);
                      } catch (e) {
                        // Keep as string
                      }
                    }
                    jsonBody[param.key] = value;
                  }
                });
                let jsonString = JSON.stringify(jsonBody, null, 2);
                
                if (details.requestBody.sample && details.requestBody.sample !== '{}') {
                  try {
                    const parsedSample = JSON.parse(details.requestBody.sample);
                    const merged = { ...parsedSample, ...jsonBody };
                    jsonString = JSON.stringify(merged, null, 2);
                  } catch (e) {
                    // Use the built JSON
                  }
                }
                setRequestBody(jsonString);
              } else if (details.requestBody.sample) {
                setRequestBody(details.requestBody.sample);
              } else {
                setRequestBody('{}');
              }
              break;
              
            case 'xml':
              setRequestBodyType('xml');
              setRawBodyType('xml');
              if (bodyParams.length > 0) {
                let xmlBody = '<?xml version="1.0" encoding="UTF-8"?>\n<request>\n';
                bodyParams.forEach(param => {
                  if (param.key) {
                    const value = param.value || '';
                    const escapedValue = value.replace(/&/g, '&amp;')
                                              .replace(/</g, '&lt;')
                                              .replace(/>/g, '&gt;')
                                              .replace(/"/g, '&quot;')
                                              .replace(/'/g, '&apos;');
                    xmlBody += `  <${param.key}>${escapedValue}</${param.key}>\n`;
                  }
                });
                xmlBody += '</request>';
                setRequestBody(xmlBody);
              } else if (details.requestBody.sample) {
                setRequestBody(details.requestBody.sample);
              } else {
                setRequestBody('<?xml version="1.0" encoding="UTF-8"?>\n<request>\n</request>');
              }
              break;
              
            case 'graphql':
              setRequestBodyType('graphql');
              if (details.requestBody.sample) {
                try {
                  const parsed = JSON.parse(details.requestBody.sample);
                  setGraphqlQuery(parsed.query || '');
                  setGraphqlVariables(JSON.stringify(parsed.variables || {}, null, 2));
                } catch (e) {
                  setGraphqlQuery(details.requestBody.sample || '');
                  setGraphqlVariables('{}');
                }
              }
              break;
              
            case 'binary':
              setRequestBodyType('binary');
              setBinaryFile(null);
              break;
              
            default:
              setRequestBodyType('none');
              setRequestBody('');
              setFormData([]);
              setUrlEncodedData([]);
              setBinaryFile(null);
              setGraphqlQuery('');
              setGraphqlVariables('');
              break;
          }
        } else {
          // If no request body details, but we have body parameters, default to raw JSON
          if (bodyParams.length > 0) {
            console.log('📦 No request body details, but have body parameters - defaulting to raw JSON');
            setRequestBodyType('raw');
            setRawBodyType('json');
            const jsonBody = {};
            bodyParams.forEach(param => {
              if (param.key) {
                jsonBody[param.key] = param.value || '';
              }
            });
            setRequestBody(JSON.stringify(jsonBody, null, 2));
          }
        }
      } // Close the if (details.parameters) block

      // Set active tab after all data is loaded
      setTimeout(() => {
        const newActiveTab = determineActiveTab();
        console.log('🎯 [After API Load] Setting active tab to:', newActiveTab);
        setActiveTab(newActiveTab);
      }, 100);
        
      }
    } catch (apiError) {
      console.error('Error fetching request details from API:', apiError);
    } finally {
      setTimeout(() => {
        if (isMounted.current) {
          setLoading(prev => ({ ...prev, request: false }));
        }
      }, 100);
    }
  } else {
    setTimeout(() => {
      const newActiveTab = determineActiveTab();
      console.log('🎯 [No API] Setting active tab to:', newActiveTab);
      setActiveTab(newActiveTab);
      setLoading(prev => ({ ...prev, request: false }));
    }, 100);
  }
  
  // Update selected request state
  const requestWithContext = { 
    ...request, 
    collectionId, 
    folderId,
    isSaved: request.isSaved !== false
  };
  setSelectedRequest(requestWithContext);
  
}, [authToken, determineActiveTab, requestUrl, authType, authConfig, activeTab, requestBodyType, formData.length, urlEncodedData.length, requestHeaders, environments, activeEnvironment]);


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
  
  // Set body type to 'none' for new requests
  setRequestBodyType('none');
  
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
      
      // ============== FIX: Auto-select first request with proper tab selection ==============
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
            
            // IMPORTANT: We need to wait for the request to be fully loaded
            // before we can determine which tab should be active
            await handleSelectRequest(firstRequest, firstCollection.id, firstFolder.id);
            
            // The active tab will be set inside handleSelectRequest after all data is loaded
            console.log('✅ [Collections] First request selected, tab will be set after data loads');
          }
        }
      }
      // ============== END FIX ==============
      
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


// Update the environment effect to respect manual updates and batch updates
// Update the environment effect to skip initial load
useEffect(() => {
  let timeoutId;
  
  // Skip if environment update is already in progress
  if (isEnvironmentUpdating.current) {
    console.log('🌍 Skipping environment update - already in progress');
    return;
  }
  
  // Skip during initial load (use a longer timeout)
  if (!initialLoadCompleteRef.current && loading.request) {
    console.log('🌍 Skipping environment update - initial load in progress');
    return;
  }
  
  // Skip if no URL or no selected request
  if (!selectedRequest || !selectedRequest.url || !environments || !activeEnvironment) {
    return;
  }
  
  const templateUrlWithPlaceholders = selectedRequest.url;
  const hasEnvPlaceholders = /\{\{[^}]+\}\}/.test(templateUrlWithPlaceholders);
  
  if (!hasEnvPlaceholders) {
    return;
  }
  
  // Skip if manual update or batch update in progress
  if (isManualUrlUpdate.current || isBatchUpdating.current || isUpdatingFromInput.current) {
    console.log('🌍 Skipping environment update - manual update in progress');
    return;
  }
  
  // Skip if URL already has environment variables resolved (no placeholders)
  if (!requestUrl.includes('{{')) {
    console.log('🌍 Skipping environment update - URL already resolved');
    return;
  }
  
  // Debounce the environment update
  timeoutId = setTimeout(() => {
    // Get the processed URL with new environment variables
    let processedUrl = templateUrlWithPlaceholders;
    
    // Find the active environment
    const activeEnv = environments.find(env => env.id === activeEnvironment);
    if (!activeEnv || !activeEnv.variables) {
      return;
    }
    
    // Create environment variable map
    const envVarMap = new Map();
    activeEnv.variables.forEach(variable => {
      if (variable.enabled && variable.key) {
        envVarMap.set(variable.key, variable.value);
      }
    });
    
    // Replace environment variables
    const placeholderRegex = /\{\{([^}]+)\}\}/g;
    let match;
    let hasReplacements = false;
    
    while ((match = placeholderRegex.exec(templateUrlWithPlaceholders)) !== null) {
      const fullPlaceholder = match[0];
      const varName = match[1];
      const varValue = envVarMap.get(varName);
      
      if (varValue !== undefined) {
        processedUrl = processedUrl.replace(new RegExp(fullPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), varValue);
        hasReplacements = true;
      }
    }
    
    // Check if the processed URL is different from current and not already processed
    if (hasReplacements && processedUrl !== requestUrl && processedUrl !== lastEnvironmentUrlRef.current) {
      console.log('🌍 Environment changed, updating URL from:', requestUrl, 'to:', processedUrl);
      
      // Mark that we're updating
      isEnvironmentUpdating.current = true;
      lastEnvironmentUrlRef.current = processedUrl;
      
      // Update the URL
      setRequestUrl(processedUrl);
      
      // Reset flag after a delay
      setTimeout(() => {
        isEnvironmentUpdating.current = false;
      }, 500);
    }
  }, 100); // Add debounce delay
  
  return () => {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
  };
}, [activeEnvironment, environments, selectedRequest, requestUrl, loading.request]);

useEffect(() => {
  if (selectedRequest && selectedRequest.url && environments.length > 0 && activeEnvironment && !initialLoadCompleteRef.current) {
    // Only run this once when environments are first loaded
    console.log('🌍 Environments loaded, checking if URL needs environment resolution');
    
    const hasEnvPlaceholders = /\{\{[^}]+\}\}/.test(selectedRequest.url);
    if (hasEnvPlaceholders) {
      const currentEnv = environments.find(env => env.id === activeEnvironment);
      if (currentEnv && currentEnv.variables && currentEnv.variables.length > 0) {
        console.log('🌍 Forcing environment resolution on initial load');
        
        // Force resolve environment variables
        let processedUrl = selectedRequest.url;
        const envVarMap = new Map();
        
        currentEnv.variables.forEach(variable => {
          if (variable.enabled && variable.key) {
            envVarMap.set(variable.key, variable.value);
          }
        });
        
        const placeholderRegex = /\{\{([^}]+)\}\}/g;
        let match;
        let hasReplacements = false;
        
        while ((match = placeholderRegex.exec(selectedRequest.url)) !== null) {
          const fullPlaceholder = match[0];
          const varName = match[1];
          const varValue = envVarMap.get(varName);
          
          if (varValue !== undefined) {
            processedUrl = processedUrl.replace(new RegExp(fullPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), varValue);
            hasReplacements = true;
          }
        }
        
        if (hasReplacements && processedUrl !== requestUrl) {
          console.log('🌍 Setting resolved URL on initial load:', processedUrl);
          // CRITICAL: Set BOTH requestUrl AND templateUrl to the resolved URL
          setRequestUrl(processedUrl);
          setTemplateUrl(processedUrl);
        }
      }
    }
  }
}, [environments, activeEnvironment, selectedRequest]);


  // Initialize data - with check for authToken changes
  useEffect(() => {
    console.log('🚀 [Collections] useEffect triggered with authToken');
    
    // Check if authToken actually changed or if this is first mounta
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
          <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>
            API Collections
          </h3>
          <p className="text-xs mb-2" style={{ color: colors.textSecondary }}>
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

  // Check if the request has API Key auth config
  const hasApiKeyConfig = authConfig && (authConfig.type === 'apikey' || authConfig.authType === 'apikey');
  const displayAuthType = (authType === 'apikey' || hasApiKeyConfig) ? 'apikey' : authType;
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
                      if (type.id === 'apikey') {
                        setAuthType('apikey');
                        if (!authConfig || authConfig.type !== 'apikey') {
                          setAuthConfig({
                            type: 'apikey',
                            authType: 'apikey',
                            key: '',
                            value: '',
                            apiKeyHeader: '',
                            apiKeyValue: '',
                            apiSecretHeader: '',
                            apiSecretValue: '',
                            addTo: 'header'
                          });
                        }
                      } else {
                        setAuthType(type.id);
                        setAuthConfig({ ...authConfig, type: type.id, authType: type.id });
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
        {/* API Key editing form - shows both Key and Secret */}
        {(authType === 'apikey' || hasApiKeyConfig) && (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
                API Key Name
              </label>
              <input
                type="text"
                value={authConfig.apiKeyHeader || authConfig.key || ''}
                onChange={(e) => setAuthConfig({ 
                  ...authConfig, 
                  apiKeyHeader: e.target.value,
                  key: e.target.value 
                })}
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
                API Key Value
              </label>
              <div className="relative">
                <input
                  type={showToken ? "text" : "password"}
                  value={authConfig.apiKeyValue || authConfig.value || ''}
                  onChange={(e) => setAuthConfig({ 
                    ...authConfig, 
                    apiKeyValue: e.target.value,
                    value: e.target.value 
                  })}
                  className="w-full px-3 py-2 border rounded text-sm focus:outline-none pr-10 hover-lift"
                  style={{
                    backgroundColor: colors.inputBg,
                    borderColor: colors.border,
                    color: colors.text
                  }}
                  placeholder="Enter API key value"
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
              {/* Display current value preview */}
              {(authConfig.apiKeyValue || authConfig.value) && (
                <div className="mt-2 text-xs flex items-center gap-2">
                  <span style={{ color: colors.textSecondary }}>Current value:</span>
                  <code className="px-2 py-1 rounded" style={{ 
                    backgroundColor: colors.codeBg,
                    color: colors.text,
                    fontSize: '11px'
                  }}>
                    {showToken 
                      ? (authConfig.apiKeyValue || authConfig.value)
                      : (authConfig.apiKeyValue || authConfig.value).substring(0, 8) + '...' + 
                        (authConfig.apiKeyValue || authConfig.value).slice(-4)}
                  </code>
                </div>
              )}
            </div>
            
            {/* API Secret field - now properly displayed */}
            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
                API Secret Name
              </label>
              <input
                type="text"
                value={authConfig.apiSecretHeader || ''}
                onChange={(e) => setAuthConfig({ 
                  ...authConfig, 
                  apiSecretHeader: e.target.value 
                })}
                className="w-full px-3 py-2 border rounded text-sm focus:outline-none hover-lift"
                style={{
                  backgroundColor: colors.inputBg,
                  borderColor: colors.border,
                  color: colors.text
                }}
                placeholder="e.g., X-API-Secret"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
                API Secret Value
              </label>
              <div className="relative">
                <input
                  type={showToken ? "text" : "password"}
                  value={authConfig.apiSecretValue || ''}
                  onChange={(e) => setAuthConfig({ 
                    ...authConfig, 
                    apiSecretValue: e.target.value 
                  })}
                  className="w-full px-3 py-2 border rounded text-sm focus:outline-none pr-10 hover-lift"
                  style={{
                    backgroundColor: colors.inputBg,
                    borderColor: colors.border,
                    color: colors.text
                  }}
                  placeholder="Enter API secret value"
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
              {/* Display current secret value preview */}
              {authConfig.apiSecretValue && (
                <div className="mt-2 text-xs flex items-center gap-2">
                  <span style={{ color: colors.textSecondary }}>Current secret:</span>
                  <code className="px-2 py-1 rounded" style={{ 
                    backgroundColor: colors.codeBg,
                    color: colors.text,
                    fontSize: '11px'
                  }}>
                    {showToken 
                      ? authConfig.apiSecretValue
                      : '••••••••' + authConfig.apiSecretValue.slice(-4)}
                  </code>
                </div>
              )}
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
            
            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={() => {
                  // Update headers with both API Key and Secret
                  const newHeaders = [...requestHeaders];
                  
                  // Remove existing API Key/Secret headers
                  const filteredHeaders = newHeaders.filter(h => 
                    h.key !== authConfig.apiKeyHeader && 
                    h.key !== authConfig.apiSecretHeader
                  );
                  
                  // Add API Key header
                  if (authConfig.apiKeyHeader && authConfig.apiKeyValue) {
                    filteredHeaders.push({
                      id: `auth-header-key-${Date.now()}`,
                      key: authConfig.apiKeyHeader,
                      value: authConfig.apiKeyValue,
                      description: 'API Key authentication',
                      enabled: true,
                      required: true
                    });
                  }
                  
                  // Add API Secret header
                  if (authConfig.apiSecretHeader && authConfig.apiSecretValue) {
                    filteredHeaders.push({
                      id: `auth-header-secret-${Date.now()}`,
                      key: authConfig.apiSecretHeader,
                      value: authConfig.apiSecretValue,
                      description: 'API Secret authentication',
                      enabled: true,
                      required: true
                    });
                  }
                  
                  setRequestHeaders(filteredHeaders);
                  showToast('API Key and Secret added to Headers tab', 'success');
                }}
                className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                Apply to Headers
              </button>
              
              <button
                type="button"
                onClick={() => {
                  showToast('API Key configuration saved', 'success');
                }}
                className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                Save
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
              {authConfig.token && (
                <div className="mt-2 text-xs flex items-center gap-2">
                  <span style={{ color: colors.textSecondary }}>Token preview:</span>
                  <code className="px-2 py-1 rounded" style={{ 
                    backgroundColor: colors.codeBg,
                    color: colors.text,
                    fontSize: '11px'
                  }}>
                    {showToken 
                      ? (authConfig.token.length > 40 ? authConfig.token.substring(0, 40) + '...' : authConfig.token)
                      : authConfig.token.substring(0, 8) + '...' + authConfig.token.slice(-4)}
                  </code>
                </div>
              )}
              <div className="mt-3">
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
                  Prefix
                </label>
                <select
                  value={authConfig.tokenType || 'Bearer'}
                  onChange={(e) => setAuthConfig({ ...authConfig, tokenType: e.target.value })}
                  className="w-full px-3 py-2 border rounded text-sm focus:outline-none hover-lift"
                  style={{
                    backgroundColor: colors.bg,
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

        {authType === 'oauth2' && (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>
                JWT / Access Token
              </label>
              <div className="relative">
                <textarea
                  rows={3}
                  value={authConfig.token || ''}
                  onChange={(e) => setAuthConfig({ ...authConfig, token: e.target.value })}
                  className="w-full px-3 py-2 border rounded text-sm focus:outline-none pr-10 font-mono hover-lift"
                  style={{
                    backgroundColor: colors.inputBg,
                    borderColor: colors.border,
                    color: colors.text,
                    resize: 'vertical'
                  }}
                  placeholder="Enter JWT token or OAuth 2.0 access token"
                />
                <button
                  type="button"
                  onClick={() => setShowToken(!showToken)}
                  className="absolute right-2 top-2 p-1 hover-lift"
                  style={{ color: colors.textSecondary }}
                >
                  {showToken ? <EyeOff size={16} /> : <EyeIcon size={16} />}
                </button>
              </div>
              <p className="text-xs mt-2" style={{ color: colors.textSecondary }}>
                Token will be sent as: Bearer [your_token]
              </p>
            </div>
            
            {authConfig.token && (
              <div className="p-3 rounded text-xs" style={{ backgroundColor: colors.hover }}>
                <div className="flex items-center justify-between mb-1">
                  <span className="font-medium" style={{ color: colors.text }}>Token Preview:</span>
                  <button
                    type="button"
                    onClick={() => {
                      navigator.clipboard.writeText(authConfig.token);
                      showToast('Token copied to clipboard!', 'success');
                    }}
                    className="p-1 rounded hover:bg-opacity-50 transition-colors"
                    style={{ backgroundColor: colors.card }}
                  >
                    <Copy size={12} style={{ color: colors.textSecondary }} />
                  </button>
                </div>
                <code className="break-all" style={{ color: colors.textSecondary }}>
                  {showToken 
                    ? (authConfig.token.length > 100 ? `${authConfig.token.substring(0, 100)}...` : authConfig.token)
                    : authConfig.token.substring(0, 50) + '...' + authConfig.token.slice(-20)}
                </code>
                <p className="mt-2 text-xs" style={{ color: colors.textTertiary }}>
                  Token length: {authConfig.token.length} characters
                </p>
              </div>
            )}
            
            <div className="flex justify-end">
              <button
                type="button"
                onClick={() => {
                  if (authConfig.token && authConfig.token.trim() !== '') {
                    setRequestHeaders(prevHeaders => {
                      const existingAuthIndex = prevHeaders.findIndex(
                        h => h.key.toLowerCase() === 'authorization'
                      );
                      
                      const authHeader = {
                        id: `auth-header-${Date.now()}`,
                        key: 'Authorization',
                        value: `Bearer ${authConfig.token}`,
                        description: 'Bearer token authentication',
                        enabled: true,
                        required: true
                      };
                      
                      if (existingAuthIndex >= 0) {
                        const newHeaders = [...prevHeaders];
                        newHeaders[existingAuthIndex] = authHeader;
                        return newHeaders;
                      } else {
                        return [...prevHeaders, authHeader];
                      }
                    });
                    showToast('Authorization header updated', 'success');
                  } else {
                    showToast('Please enter a token first', 'warning');
                  }
                }}
                className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                Apply to Headers
              </button>
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
                        // REMOVE the readOnly attribute
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
                        checked={requestParams.length > 0 && requestParams.every(p => p.enabled)}
                        onChange={() => {
                            // Mark as batch update to prevent loops
                            isBatchUpdating.current = true;
                            isManualUrlUpdate.current = true;
                            isUpdatingFromInput.current = true;
                            
                            const allEnabled = requestParams.length > 0 && requestParams.every(p => p.enabled);
                            const updatedParams = requestParams.map(p => ({ ...p, enabled: !allEnabled }));
                            
                            // Update params state
                            setRequestParams(updatedParams);
                            
                            // Get the base URL (without query string)
                            let baseUrl = templateUrl;
                            if (!baseUrl) {
                              baseUrl = requestUrl.split('?')[0];
                            }
                            
                            // Replace path parameters in the base URL with their actual values
                            let finalBaseUrl = baseUrl;
                            // Use the current requestPathParams values
                            requestPathParams.forEach(param => {
                              if (param.enabled && param.key) {
                                const placeholder = `{${param.key}}`;
                                const colonPlaceholder = `:${param.key}`;
                                const paramValue = param.value && param.value.trim() !== '' ? param.value : '';
                                
                                if (finalBaseUrl.includes(placeholder)) {
                                  finalBaseUrl = finalBaseUrl.replace(new RegExp(placeholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), paramValue);
                                }
                                if (finalBaseUrl.includes(colonPlaceholder)) {
                                  finalBaseUrl = finalBaseUrl.replace(new RegExp(colonPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), paramValue);
                                }
                              }
                            });
                            
                            // Build query string from enabled params
                            const queryParams = updatedParams
                              .filter(p => p.enabled && p.key && p.key.trim() !== '')
                              .map(p => {
                                if (p.value && p.value.trim() !== '') {
                                  return `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value)}`;
                                } else {
                                  return `${encodeURIComponent(p.key)}=`;
                                }
                              })
                              .join('&');
                            
                            // Construct new URL
                            const newUrl = queryParams ? `${finalBaseUrl}?${queryParams}` : finalBaseUrl;
                            
                            // Only update if URL actually changed
                            if (newUrl !== requestUrl) {
                              lastProcessedUrlRef.current = newUrl;
                              setRequestUrl(newUrl);
                            }
                            
                            // Reset flags after a delay
                            setTimeout(() => {
                              isBatchUpdating.current = false;
                              isManualUrlUpdate.current = false;
                              isUpdatingFromInput.current = false;
                            }, 100);
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
  // Render Body Tab
const renderBodyTab = () => {
  const renderBodyContent = () => {
    // If body type is 'none', show the none tab
    if (requestBodyType === 'none') {
      return (
        <div className="flex flex-col items-center justify-center h-64 p-8 text-center">
          <div className="w-16 h-16 rounded-full flex items-center justify-center mb-4" 
            style={{ backgroundColor: colors.hover }}>
            <FileText size={32} style={{ color: colors.textSecondary, opacity: 0.7 }} />
          </div>
          <h3 className="text-sm font-semibold mb-2" style={{ color: colors.text }}>No Body</h3>
          <p className="text-sm max-w-sm mb-4" style={{ color: colors.textSecondary }}>
            This request does not have a body. Select a body type from the options above to add content.
          </p>
        </div>
      );
    }

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
                                newData[index].file = file;
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
                          if (e.target.value === 'file') {
                            newData[index].value = '';
                          }
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
            <p className="text-xs mb-2" style={{ color: colors.text }}>Upload a file</p>
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
        return null;
    }
  };

  return (
    <div className="p-4">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Body</h3>
        <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-thin" style={{ maxWidth: '100%' }}>
          {['none', 'form-data', 'x-www-form-urlencoded', 'raw', 'xml', 'binary', 'graphql'].map(type => (
            <button
              key={type}
              type="button"
              onClick={() => {
                setRequestBodyType(type);
                // If switching to raw with no previous body type, set default raw type
                if (type === 'raw' && rawBodyType === 'json') {
                  // Keep default
                } else if (type === 'raw') {
                  setRawBodyType('json');
                }
                // Clear body data when switching to none
                if (type === 'none') {
                  setRequestBody('');
                  setFormData([]);
                  setUrlEncodedData([]);
                  setBinaryFile(null);
                  setGraphqlQuery('');
                  setGraphqlVariables('');
                }
              }}
              className={`px-3 py-1.5 rounded text-sm font-medium whitespace-nowrap transition-colors hover-lift ${
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
                <button 
                  type="button" 
                  className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors hover-lift flex items-center gap-1"
                  style={{ backgroundColor: colors.hover, color: colors.textSecondary }}
                  onClick={() => {
                    let contentToCopy = '';
                    
                    if (responseView === 'headers') {
                      const headers = response.headers || [];
                      const headerArray = Array.isArray(headers) 
                        ? headers 
                        : Object.entries(headers).map(([k, v]) => ({ key: k, value: v }));
                      contentToCopy = headerArray.map(h => `${h.key}: ${h.value}`).join('\n');
                    } else {
                      contentToCopy = response.responseBody || 
                                    (typeof response.data === 'string' ? response.data : JSON.stringify(response.data, null, 2)) || 
                                    (typeof response.body === 'string' ? response.body : JSON.stringify(response.body, null, 2)) ||
                                    JSON.stringify(response, null, 2);
                    }
                    
                    // Use the copyToClipboard utility function
                    copyToClipboard(contentToCopy, showToast);
                  }}
                >
                  <Copy size={12} />
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


  // Add this function to cancel the request
  const handleCancelRequest = useCallback(() => {
    if (abortController) {
      console.log('🛑 Cancelling request...');
      abortController.abort();
      setAbortController(null);
    }
  }, [abortController]);
 
  const handleExecuteRequest = useCallback(async () => {
  const validationErrors = validateExecuteRequest({
    method: requestMethod,
    url: requestUrl
  });

  if (validationErrors.length > 0) {
    showToast(validationErrors[0], 'error');
    return;
  }

  // Create new AbortController for this request
  const controller = new AbortController();
  setAbortController(controller);
  
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

    // Replace path parameters in the URL
    if (pathParamsArray.length > 0) {
      pathParamsArray.forEach(param => {
        const placeholder = `{${param.key}}`;
        const colonPlaceholder = `:${param.key}`;
        
        if (finalUrl.includes(placeholder)) {
          finalUrl = finalUrl.replace(new RegExp(placeholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), encodeURIComponent(param.value));
          console.log(`✅ Replaced ${placeholder} with ${param.value}`);
        }
        
        if (finalUrl.includes(colonPlaceholder)) {
          finalUrl = finalUrl.replace(new RegExp(colonPlaceholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), encodeURIComponent(param.value));
          console.log(`✅ Replaced ${colonPlaceholder} with ${param.value}`);
        }
      });
    }

    // ============== STEP 2: BUILD THE REQUEST BODY ==============
    let body = null;
    let contentType = '';

    const hasBodyParams = requestBodyType === 'raw' && requestBody && requestBody.trim() !== '' ||
                          requestBodyType === 'form-data' && formData.some(f => f.enabled && f.key && f.key.trim() !== '') ||
                          requestBodyType === 'x-www-form-urlencoded' && urlEncodedData.some(u => u.enabled && u.key && u.key.trim() !== '') ||
                          requestBodyType === 'xml' && requestBody && requestBody.trim() !== '' ||
                          requestBodyType === 'graphql' && graphqlQuery && graphqlQuery.trim() !== '' ||
                          requestBodyType === 'binary' && binaryFile;

    if (hasBodyParams) {
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
                setAbortController(null);
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
            setAbortController(null);
            return;
          }
        }
        body = JSON.stringify(graphqlBody);
      }
      else if (requestBodyType === 'binary' && binaryFile) {
        body = binaryFile;
      }
    } else {
      console.log('📭 No body parameters to send');
    }

    // ============== STEP 3: BUILD HEADERS ==============
    const headers = {};
    
    requestHeaders.filter(h => h.enabled && h.key && h.key.trim() !== '').forEach(header => {
      headers[header.key.trim()] = header.value || '';
    });

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

    if (body && !(body instanceof FormData)) {
      if (!headers['Content-Type'] && contentType) {
        headers['Content-Type'] = contentType;
      }
    }

    if (!headers['Accept']) {
      headers['Accept'] = '*/*';
    }

    if (requestMethod === 'GET' || requestMethod === 'HEAD') {
      body = null;
    }

    console.log('📤 Final Request Details:', {
      method: requestMethod,
      url: finalUrl,
      headers: headers,
      hasBody: !!body,
      bodyType: body instanceof FormData ? 'FormData' : (body ? typeof body : 'none'),
      pathParams: pathParamsArray
    });

    // ============== STEP 4: MAKE THE REQUEST WITH ABORT CONTROLLER ==============
    const fetchOptions = {
      method: requestMethod,
      headers: headers,
      mode: 'cors',
      credentials: 'omit',
      redirect: 'follow',
      signal: controller.signal // Add the abort signal
    };

    if (body) {
      fetchOptions.body = body;
    }

    console.log('📡 Making request to:', finalUrl);

    const fetchResponse = await fetch(finalUrl, fetchOptions);
    
    const responseTime = Date.now() - startTime;

    const responseHeaders = [];
    fetchResponse.headers.forEach((value, key) => {
      responseHeaders.push({ key, value });
    });

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

    const formattedResponse = {
      responseBody,  // Make sure this is set correctly
      statusCode: fetchResponse.status,
      statusText: fetchResponse.statusText,
      headers: responseHeaders,
      responseTime,
      responseSize: Math.round(new Blob([responseBody]).size / 1024),
      data: responseBody  // Also set data for compatibility
    };

    setResponse(formattedResponse);

    if (fetchResponse.ok) {
      showToast(`✓ ${requestMethod} ${fetchResponse.status}`, 'success');
    } else if (fetchResponse.status >= 400 && fetchResponse.status < 500) {
      showToast(`⚠️ Client Error: ${fetchResponse.status} - ${fetchResponse.statusText}`, 'warning');
    } else if (fetchResponse.status >= 500) {
      showToast(`❌ Server Error: ${fetchResponse.status} - ${fetchResponse.statusText}`, 'error');
    }

  } catch (error) {
    // Check if this was an abort error
    if (error.name === 'AbortError' || error.message === 'The user aborted a request.') {
      console.log('🛑 Request cancelled by user');
      setResponse({
        responseBody: JSON.stringify({
          message: 'Request was cancelled',
          timestamp: new Date().toISOString(),
          url: requestUrl,
          method: requestMethod
        }, null, 2),
        statusCode: 0,
        statusText: 'Cancelled',
        headers: [],
        responseTime: Date.now() - startTime,
        responseSize: 0,
        data: { message: 'Request was cancelled' }
      });
      showToast('Request cancelled', 'info');
    } else {
      console.error('Error executing request:', error);
      
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
        responseTime: Date.now() - startTime,
        responseSize: 0,
        data: { error: error.message }
      });
      
      showToast(`❌ Request failed: ${error.message}`, 'error');
    }
  } finally {
    setLoading(prev => ({ ...prev, execute: false }));
    setIsSending(false);
    setAbortController(null);
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
    <button 
      type="button" 
      className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors hover-lift flex items-center gap-1"
      style={{ backgroundColor: colors.hover, color: colors.textSecondary }}
      onClick={() => {
        // Get the content to copy
        let contentToCopy = '';
        
        // Get based on current view
        if (responseView === 'raw' || responseView === 'preview') {
          contentToCopy = response.responseBody || 
                         JSON.stringify(response.data, null, 2) || 
                         JSON.stringify(response, null, 2);
        } else if (responseView === 'headers') {
          const headers = response.headers || [];
          const headerArray = Array.isArray(headers) ? headers : Object.entries(headers);
          contentToCopy = headerArray.map(([key, value]) => `${key}: ${value}`).join('\n');
        }
        
        // Copy to clipboard
        navigator.clipboard.writeText(contentToCopy)
          .then(() => {
            showToast('Response copied to clipboard!', 'success');
          })
          .catch((err) => {
            console.error('Failed to copy:', err);
            // Fallback method
            const textarea = document.createElement('textarea');
            textarea.value = contentToCopy;
            document.body.appendChild(textarea);
            textarea.select();
            const success = document.execCommand('copy');
            document.body.removeChild(textarea);
            
            if (success) {
              showToast('Response copied to clipboard!', 'success');
            } else {
              showToast('Failed to copy response', 'error');
            }
          });
      }}
    >
      <Copy size={12} />
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
              <p className="text-xs mb-2" style={{ color: colors.text }}>Drag and drop files here</p>
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
  <button 
    type="button" 
    className="flex items-center gap-2 px-3 py-1.5 rounded text-sm hover:bg-opacity-50 transition-colors hover-lift"
    style={{ backgroundColor: colors.hover }}
    onClick={() => {
      if (environments.length === 0) {
        showToast('No environments available', 'info');
        return;
      }
      
      // Find the current active environment index
      const currentIndex = environments.findIndex(e => e.id === activeEnvironment);
      const nextIndex = (currentIndex + 1) % environments.length;
      const nextEnv = environments[nextIndex];
      
      // Update active environment - this will trigger the useEffect above
      setActiveEnvironment(nextEnv.id);
      setEnvironments(envs => envs.map(e => ({ ...e, isActive: e.id === nextEnv.id })));
      
      // Show toast with environment info
      const varCount = nextEnv.variables?.filter(v => v.enabled).length || 0;
      showToast(`Switched to ${nextEnv.name} (${varCount} variables available)`, 'success');
      
      // Log available variables for debugging
      if (nextEnv.variables) {
        console.log(`🌍 Environment ${nextEnv.name} variables:`, 
          nextEnv.variables.filter(v => v.enabled).map(v => v.key));
      }
    }}
  >
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
                {/* Add Refresh Button */}
                <button 
                  type="button" 
                  onClick={async () => {
                    try {
                      await fetchCollections();
                      showToast('Collections refreshed', 'success');
                    } catch (error) {
                      showToast('Failed to refresh collections', 'error');
                    }
                  }}
                  className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}
                  disabled={loading.collections}
                >
                  <RefreshCw 
                    size={12} 
                    style={{ 
                      color: colors.textSecondary,
                      animation: loading.collections ? 'spin 1s linear infinite' : 'none'
                    }} 
                  />
                </button>
                
                {/* <button type="button" onClick={() => setShowCreateModal(true)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}>
                  <Plus size={12} style={{ color: colors.textSecondary }} />
                </button>
                <button type="button" onClick={() => setShowImportModal(true)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}>
                  <Upload size={12} style={{ color: colors.textSecondary }} />
                </button> */}
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
            
            {/* URL input with loading indicator */}
            <div className="flex-1 flex items-center rounded overflow-hidden hover-lift min-w-0 relative" style={{ 
              border: `1px solid ${isSending ? colors.primary : colors.inputborder}`,
              boxShadow: isSending ? `0 0 0 2px ${colors.primary}20` : 'none',
              transition: 'all 0.2s ease'
            }}>
              <input 
                ref={urlInputRef}
                type="text" 
                value={requestUrl} 
                onChange={handleUrlChange}
                onPaste={handleUrlPaste}
                disabled={isSending}
                className="flex-1 px-3 py-2 text-sm focus:outline-none min-w-0 disabled:opacity-70"
                style={{ 
                  backgroundColor: colors.inputBg, 
                  color: colors.text,
                  cursor: isSending ? 'wait' : 'text'
                }}
                placeholder="Enter request URL" 
              />
              {isSending && (
                <div className="absolute right-2 top-1/2 transform -translate-y-1/2 flex items-center gap-1">
                  <div className="w-1.5 h-1.5 bg-blue-500 rounded-full animate-pulse" style={{ animationDelay: '0ms' }}></div>
                  <div className="w-1.5 h-1.5 bg-blue-500 rounded-full animate-pulse" style={{ animationDelay: '150ms' }}></div>
                  <div className="w-1.5 h-1.5 bg-blue-500 rounded-full animate-pulse" style={{ animationDelay: '300ms' }}></div>
                </div>
              )}
            </div>
            
            {/* Send/Cancel button with enhanced states */}
            <button 
              type="button"
              onClick={isSending ? handleCancelRequest : handleExecuteRequest} 
              disabled={!isSending && (!requestUrl || loading.execute)}
              className={`px-4 py-2 rounded text-sm font-medium flex items-center gap-2 transition-all hover-lift shrink-0 ${
                (!isSending && (!requestUrl || loading.execute)) ? 'opacity-50 cursor-not-allowed' : 'hover:opacity-90'
              }`}
              style={{ 
                backgroundColor: isSending ? colors.success : colors.primaryDark, 
                color: colors.white, 
                minWidth: '100px',
                position: 'relative',
                overflow: 'hidden'
              }}>
              
              {/* Background animation for sending state */}
              {isSending && (
                <div 
                  className="absolute inset-0 bg-white opacity-10"
                  style={{
                    animation: 'shimmer 1.5s infinite linear',
                    background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent)',
                    transform: 'translateX(-100%)'
                  }}
                />
              )}
              
              {loading.execute && !isSending ? (
                <>
                  <RefreshCw size={12} className="animate-spin" />
                  <span>Sending...</span>
                </>
              ) : isSending ? (
                // <>
                //   <div className="flex items-center gap-1">
                //     <X size={12} />
                //     <span>Cancel</span>
                //   </div>
                //   <span className="text-xs opacity-75 ml-1">(sending...)</span>
                // </>
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
            
            {/* Save button - only show for unsaved requests */}
            {selectedRequest && !selectedRequest.isSaved && (
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
                disabled={loading.save}>
                {loading.save ? (
                  <div className="flex items-center gap-2">
                    <RefreshCw size={12} className="animate-spin" />
                    <span>Saving...</span>
                  </div>
                ) : (
                  'Save'
                )}
              </button>
            )}
          </div>

          {/* Add this to your style section in the component */}
          <style>{`
            @keyframes shimmer {
              0% { transform: translateX(-100%); }
              100% { transform: translateX(100%); }
            }
            
            .animate-shimmer {
              animation: shimmer 1.5s infinite;
            }
          `}</style>

          {/* Optional: Add a status bar at the top of response panel when sending */}
          {isSending && (
            <div 
              className="px-4 py-2 text-sm flex items-center gap-2 border-t"
              style={{ 
                backgroundColor: colors.card,
                borderColor: colors.border,
                color: colors.textSecondary
              }}
            >
              <RefreshCw size={12} className="animate-spin" style={{ color: colors.primary }} />
              <span>Sending <span style={{ color: colors.primary }}>{requestMethod}</span> request to <span style={{ color: colors.primary }}>{requestUrl || 'URL'}</span>...</span>
              <button
                type="button"
                onClick={handleCancelRequest}
                className="ml-auto text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                Cancel
              </button>
            </div>
          )}

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
                    onClick={() => handleTabClick(tabId)}  // Change this line
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