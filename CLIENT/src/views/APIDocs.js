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

// FinTech API Collections
const FINTECH_API_COLLECTIONS = [
  {
    id: 'fintech-core',
    name: 'FinTech Core Banking API',
    description: 'Core banking operations including accounts, transactions, payments, and transfers',
    isExpanded: true,
    isFavorite: true,
    version: 'v2.1',
    owner: 'FinTech Banking Solutions',
    updatedAt: 'Today, 9:30 AM',
    createdAt: 'Jan 15, 2024',
    folders: [
      {
        id: 'accounts',
        name: 'Account Management',
        description: 'Account creation, management, and information retrieval',
        isExpanded: true,
        requests: [
          {
            id: 'create-account',
            name: 'Create Account',
            method: 'POST',
            url: 'https://api.fintech.com/v2.1/accounts',
            description: 'Create a new bank account for a customer',
            tags: ['accounts', 'create', 'onboarding'],
            lastModified: 'Today, 9:00 AM',
            headers: [
              { key: 'Content-Type', value: 'application/json' },
              { key: 'Authorization', value: 'Bearer {access-token}' },
              { key: 'X-Client-Id', value: '{client-id}' }
            ],
            body: JSON.stringify({
              customerId: "CUST123456",
              accountType: "SAVINGS",
              currency: "USD",
              initialDeposit: 1000.00,
              branchCode: "NYC001",
              productCode: "SAV-PRO",
              overdraftLimit: 0,
              interestRate: 2.5,
              isJointAccount: false,
              nominationDetails: null
            }, null, 2),
            examples: {
              curl: `curl -X POST https://api.fintech.com/v2.1/accounts \\
  -H "Content-Type: application/json" \\
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \\
  -H "X-Client-Id: YOUR_CLIENT_ID" \\
  -d '{
    "customerId": "CUST123456",
    "accountType": "SAVINGS",
    "currency": "USD",
    "initialDeposit": 1000.00,
    "branchCode": "NYC001"
  }'`,
              javascript: `fetch('https://api.fintech.com/v2.1/accounts', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN',
    'X-Client-Id': 'YOUR_CLIENT_ID'
  },
  body: JSON.stringify({
    customerId: 'CUST123456',
    accountType: 'SAVINGS',
    currency: 'USD',
    initialDeposit: 1000.00,
    branchCode: 'NYC001'
  })
})`,
              python: `import requests

headers = {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN',
    'X-Client-Id': 'YOUR_CLIENT_ID'
}

data = {
    'customerId': 'CUST123456',
    'accountType': 'SAVINGS',
    'currency': 'USD',
    'initialDeposit': 1000.00,
    'branchCode': 'NYC001'
}

response = requests.post('https://api.fintech.com/v2.1/accounts', 
                        headers=headers, 
                        json=data)`,
              nodejs: `const https = require('https');

const data = JSON.stringify({
  customerId: 'CUST123456',
  accountType: 'SAVINGS',
  currency: 'USD',
  initialDeposit: 1000.00,
  branchCode: 'NYC001'
});

const options = {
  hostname: 'api.fintech.com',
  path: '/v2.1/accounts',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN',
    'X-Client-Id': 'YOUR_CLIENT_ID',
    'Content-Length': data.length
  }
};

const req = https.request(options, (res) => {
  console.log(res.statusCode);
});

req.write(data);
req.end();`
            }
          },
          {
            id: 'get-account',
            name: 'Get Account Details',
            method: 'GET',
            url: 'https://api.fintech.com/v2.1/accounts/{accountNumber}',
            description: 'Retrieve detailed information about a specific account',
            tags: ['accounts', 'retrieve', 'read'],
            lastModified: 'Yesterday, 3:45 PM',
            headers: [
              { key: 'Authorization', value: 'Bearer {access-token}' },
              { key: 'X-Client-Id', value: '{client-id}' }
            ]
          },
          {
            id: 'list-accounts',
            name: 'List Accounts',
            method: 'GET',
            url: 'https://api.fintech.com/v2.1/customers/{customerId}/accounts',
            description: 'List all accounts for a specific customer',
            tags: ['accounts', 'list', 'retrieve'],
            lastModified: '2 days ago'
          }
        ]
      },
      {
        id: 'transactions',
        name: 'Transaction Processing',
        description: 'Core banking transactions processing',
        isExpanded: true,
        requests: [
          {
            id: 'cash-deposit',
            name: 'Cash Deposit',
            method: 'POST',
            url: 'https://api.fintech.com/v2.1/transactions/deposit',
            description: 'Process cash deposit into an account',
            tags: ['transactions', 'deposit', 'cash'],
            lastModified: 'Today, 8:15 AM',
            headers: [
              { key: 'Content-Type', value: 'application/json' },
              { key: 'Authorization', value: 'Bearer {access-token}' },
              { key: 'X-Client-Id', value: '{client-id}' },
              { key: 'X-Transaction-Id', value: '{unique-transaction-id}' }
            ],
            body: JSON.stringify({
              accountNumber: "ACC987654321",
              amount: 5000.00,
              currency: "USD",
              transactionType: "CASH_DEPOSIT",
              depositMethod: "BRANCH_COUNTER",
              tellerId: "TELLER456",
              branchCode: "LAX002",
              referenceNumber: "DEP20240315001",
              notes: "Customer deposit",
              instrumentNumber: null
            }, null, 2)
          },
          {
            id: 'cash-withdrawal',
            name: 'Cash Withdrawal',
            method: 'POST',
            url: 'https://api.fintech.com/v2.1/transactions/withdrawal',
            description: 'Process cash withdrawal from an account',
            tags: ['transactions', 'withdrawal', 'cash'],
            lastModified: '2 days ago',
            headers: [
              { key: 'Content-Type', value: 'application/json' },
              { key: 'Authorization', value: 'Bearer {access-token}' }
            ],
            body: JSON.stringify({
              accountNumber: "ACC987654321",
              amount: 1000.00,
              currency: "USD",
              transactionType: "CASH_WITHDRAWAL",
              withdrawalMethod: "ATM",
              atmLocation: "NYC-ATM-123",
              referenceNumber: "WITH20240315001"
            }, null, 2)
          },
          {
            id: 'funds-transfer',
            name: 'Funds Transfer',
            method: 'POST',
            url: 'https://api.fintech.com/v2.1/transactions/transfer',
            description: 'Transfer funds between accounts',
            tags: ['transactions', 'transfer', 'payment'],
            lastModified: '3 days ago',
            body: JSON.stringify({
              fromAccount: "ACC987654321",
              toAccount: "ACC123456789",
              amount: 2500.00,
              currency: "USD",
              transactionType: "INTERNAL_TRANSFER",
              transferMethod: "ONLINE",
              reference: "INVOICE#2024-001",
              remarks: "Payment for services",
              scheduledDate: null
            }, null, 2)
          }
        ]
      },
      {
        id: 'enquiries',
        name: 'Account Enquiries',
        description: 'Balance enquiries and account statements',
        isExpanded: true,
        requests: [
          {
            id: 'balance-enquiry',
            name: 'Balance Enquiry',
            method: 'GET',
            url: 'https://api.fintech.com/v2.1/accounts/{accountNumber}/balance',
            description: 'Check current balance of an account',
            tags: ['enquiry', 'balance', 'read'],
            lastModified: '3 days ago'
          },
          {
            id: 'account-statement',
            name: 'Account Statement',
            method: 'GET',
            url: 'https://api.fintech.com/v2.1/accounts/{accountNumber}/statement',
            description: 'Retrieve account statement for a period',
            tags: ['statement', 'transactions', 'report'],
            lastModified: '1 week ago',
            headers: [
              { key: 'Authorization', value: 'Bearer {access-token}' }
            ]
          },
          {
            id: 'mini-statement',
            name: 'Mini Statement',
            method: 'GET',
            url: 'https://api.fintech.com/v2.1/accounts/{accountNumber}/statement/mini',
            description: 'Get last 10 transactions',
            tags: ['statement', 'mini', 'quick'],
            lastModified: '2 weeks ago'
          }
        ]
      },
      {
        id: 'payments',
        name: 'Payment Processing',
        description: 'Bill payments and merchant payments',
        isExpanded: false,
        requests: [
          {
            id: 'bill-payment',
            name: 'Bill Payment',
            method: 'POST',
            url: 'https://api.fintech.com/v2.1/payments/bill',
            description: 'Pay utility bills and invoices',
            tags: ['payments', 'bill', 'utilities'],
            lastModified: '1 month ago',
            body: JSON.stringify({
              accountNumber: "ACC987654321",
              billerCode: "ELECTRICITY-001",
              billReference: "BILL-2024-03-456",
              amount: 150.75,
              currency: "USD",
              paymentDate: "2024-03-15",
              recurring: false
            }, null, 2)
          }
        ]
      },
      {
        id: 'cards',
        name: 'Card Management',
        description: 'Debit/Credit card issuance and management',
        isExpanded: false,
        requests: [
          {
            id: 'issue-card',
            name: 'Issue New Card',
            method: 'POST',
            url: 'https://api.fintech.com/v2.1/cards',
            description: 'Issue new debit/credit card',
            tags: ['cards', 'issuance', 'new'],
            lastModified: '1 month ago'
          }
        ]
      }
    ]
  },
  {
    id: 'fintech-loans',
    name: 'Loan Management API',
    description: 'Loan applications, approvals, and disbursements',
    isExpanded: false,
    isFavorite: false,
    version: 'v1.5',
    owner: 'FinTech Lending Team',
    updatedAt: '1 week ago',
    createdAt: 'Feb 1, 2024',
    folders: [
      {
        id: 'loan-applications',
        name: 'Loan Applications',
        description: 'Loan application submission and processing',
        isExpanded: false,
        requests: [
          {
            id: 'apply-loan',
            name: 'Apply for Loan',
            method: 'POST',
            url: 'https://api.fintech.com/v1.5/loans/apply',
            description: 'Submit new loan application',
            tags: ['loans', 'application', 'new'],
            lastModified: '2 weeks ago'
          }
        ]
      }
    ]
  },
  {
    id: 'fintech-compliance',
    name: 'Compliance & AML API',
    description: 'Anti-money laundering and compliance checks',
    isExpanded: false,
    isFavorite: true,
    version: 'v3.2',
    owner: 'FinTech Compliance Team',
    updatedAt: '3 days ago',
    createdAt: 'Mar 1, 2024'
  }
];

const ENVIRONMENTS = [
  { id: 'sandbox', name: 'Sandbox', isActive: true, baseUrl: 'https://api.sandbox.fintech.com' },
  { id: 'uat', name: 'UAT', isActive: false, baseUrl: 'https://api.uat.fintech.com' },
  { id: 'production', name: 'Production', isActive: false, baseUrl: 'https://api.fintech.com' }
];

const NOTIFICATIONS = [
  { id: 'notif-1', title: 'API Rate Limit Warning', message: 'You\'ve used 85% of your API rate limit', time: '10 minutes ago', read: false, type: 'warning' },
  { id: 'notif-2', title: 'New API Version Available', message: 'FinTech Core Banking API v2.2 is now available', time: '2 hours ago', read: false, type: 'info' },
  { id: 'notif-3', title: 'Compliance Update', message: 'AML compliance requirements have been updated', time: '1 day ago', read: true, type: 'success' }
];

// Main component
const APIDocs = ({ theme, isDark, customTheme, toggleTheme }) => {
  const [activeTab, setActiveTab] = useState('documentation');
  const [showCodePanel, setShowCodePanel] = useState(true);
  const [selectedLanguage, setSelectedLanguage] = useState('curl');
  const [showLanguageDropdown, setShowLanguageDropdown] = useState(false);
  const [toast, setToast] = useState(null);
  const [showPublishModal, setShowPublishModal] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState(NOTIFICATIONS);
  const [searchQuery, setSearchQuery] = useState('');
  const [globalSearchQuery, setGlobalSearchQuery] = useState('');
  const [selectedCollection, setSelectedCollection] = useState(FINTECH_API_COLLECTIONS[0]);
  const [selectedRequest, setSelectedRequest] = useState(FINTECH_API_COLLECTIONS[0].folders[0].requests[0]);
  const [environments, setEnvironments] = useState(ENVIRONMENTS);
  const [activeEnvironment, setActiveEnvironment] = useState('sandbox');
  const [publishUrl, setPublishUrl] = useState('');
  const [isGeneratingDocs, setIsGeneratingDocs] = useState(false);
  const [collections, setCollections] = useState(FINTECH_API_COLLECTIONS);
  const [expandedCollections, setExpandedCollections] = useState(['fintech-core']);
  const [expandedFolders, setExpandedFolders] = useState(['accounts', 'transactions', 'enquiries']);
  const [activeMainTab, setActiveMainTab] = useState('Collections');
  const [showImportModal, setShowImportModal] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [showWorkspaceSwitcher, setShowWorkspaceSwitcher] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showEnvironmentMenu, setShowEnvironmentMenu] = useState(false);

  // Updated color scheme to match APICodeBase
  const colors = isDark ? {
    bg: '#0f172a',
    white: '#f8fafc',
    sidebar: '#1e293b',
    main: '#0f172a',
    header: '#1e293b',
    card: '#1e293b',
    text: '#f1f5f9',
    textSecondary: '#94a3b8',
    textTertiary: '#64748b',
    border: '#334155',
    borderLight: '#2d3748',
    borderDark: '#475569',
    hover: '#334155',
    active: '#475569',
    selected: '#2c5282',
    primary: '#f1f5f9',
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
    tabInactive: '#94a3b8',
    sidebarActive: '#3b82f6',
    sidebarHover: '#334155',
    inputBg: '#1e293b',
    inputBorder: '#334155',
    tableHeader: '#334155',
    tableRow: '#1e293b',
    tableRowHover: '#2d3748',
    dropdownBg: '#1e293b',
    dropdownBorder: '#334155',
    modalBg: '#1e293b',
    modalBorder: '#334155',
    codeBg: '#1e293b'
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

  // Filter collections based on search
  const filteredCollections = collections.filter(collection => {
    if (!searchQuery) return true;
    const query = searchQuery.toLowerCase();
    return (
      collection.name.toLowerCase().includes(query) ||
      collection.description.toLowerCase().includes(query) ||
      collection.folders?.some(folder => 
        folder.name.toLowerCase().includes(query) ||
        folder.description.toLowerCase().includes(query) ||
        folder.requests?.some(request => 
          request.name.toLowerCase().includes(query) ||
          request.description.toLowerCase().includes(query)
        )
      )
    );
  });

  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const getMethodColor = (method) => {
    return colors.method[method] || colors.textSecondary;
  };

  const toggleCollection = (collectionId) => {
    setExpandedCollections(prev =>
      prev.includes(collectionId)
        ? prev.filter(id => id !== collectionId)
        : [...prev, collectionId]
    );
  };

  const toggleFolder = (folderId) => {
    setExpandedFolders(prev =>
      prev.includes(folderId)
        ? prev.filter(id => id !== folderId)
        : [...prev, folderId]
    );
  };

  const handleSelectRequest = (request, collection, folder) => {
    setSelectedRequest(request);
    setSelectedCollection(collection);
    showToast(`Viewing documentation for ${request.name}`, 'info');
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

  const markAllNotificationsAsRead = () => {
    setNotifications(notifications.map(n => ({ ...n, read: true })));
    showToast('All notifications marked as read', 'success');
  };

  const getActiveBaseUrl = () => {
    return environments.find(e => e.id === activeEnvironment)?.baseUrl || 'https://api.fintech.com';
  };

  const generatePublishUrl = () => {
    setIsGeneratingDocs(true);
    setTimeout(() => {
      const randomId = Math.random().toString(36).substring(7);
      setPublishUrl(`https://docs.fintech.com/view/${randomId}/fintech-core-api-v2-1`);
      setIsGeneratingDocs(false);
      showToast('Documentation published successfully!', 'success');
      setShowPublishModal(false);
    }, 1500);
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    showToast('Copied to clipboard!', 'success');
  };

  const generateCodeExample = () => {
    const examples = {
      curl: `curl -X ${selectedRequest.method} "${getActiveBaseUrl()}${selectedRequest.url.replace('https://api.fintech.com', '')}" \\
  -H "Content-Type: application/json" \\
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"${
    selectedRequest.body ? ` \\
  -d '${selectedRequest.body}'` : ''
  }`,
      javascript: `fetch('${getActiveBaseUrl()}${selectedRequest.url.replace('https://api.fintech.com', '')}', {
  method: '${selectedRequest.method}',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'${
      selectedRequest.headers?.filter(h => h.key !== 'Content-Type' && h.key !== 'Authorization')
        .map(h => `,\n    '${h.key}': '${h.value}'`).join('') || ''
    }
  }${
    selectedRequest.body ? `,
  body: JSON.stringify(${selectedRequest.body})` : ''
  }
})`,
      python: `import requests

headers = {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'${
      selectedRequest.headers?.filter(h => h.key !== 'Content-Type' && h.key !== 'Authorization')
        .map(h => `,\n    '${h.key}': '${h.value}'`).join('') || ''
    }
}

${selectedRequest.body ? `data = ${selectedRequest.body}

response = requests.${selectedRequest.method.toLowerCase()}('${getActiveBaseUrl()}${selectedRequest.url.replace('https://api.fintech.com', '')}', 
                        headers=headers, 
                        json=data)` : `response = requests.${selectedRequest.method.toLowerCase()}('${getActiveBaseUrl()}${selectedRequest.url.replace('https://api.fintech.com', '')}', 
                        headers=headers)`}`,
      nodejs: `const https = require('https');

${selectedRequest.body ? `const data = JSON.stringify(${selectedRequest.body});

const options = {
  hostname: '${getActiveBaseUrl().replace('https://', '').split('/')[0]}',
  path: '${selectedRequest.url.replace('https://api.fintech.com', '')}',
  method: '${selectedRequest.method}',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'${
      selectedRequest.headers?.filter(h => h.key !== 'Content-Type' && h.key !== 'Authorization')
        .map(h => `,\n    '${h.key}': '${h.value}'`).join('') || ''
    },
    'Content-Length': data.length
  }
};

const req = https.request(options, (res) => {
  console.log(res.statusCode);
});

req.write(data);
req.end();` : `const options = {
  hostname: '${getActiveBaseUrl().replace('https://', '').split('/')[0]}',
  path: '${selectedRequest.url.replace('https://api.fintech.com', '')}',
  method: '${selectedRequest.method}',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'${
      selectedRequest.headers?.filter(h => h.key !== 'Content-Type' && h.key !== 'Authorization')
        .map(h => `,\n    '${h.key}': '${h.value}'`).join('') || ''
    }
  }
};

const req = https.request(options, (res) => {
  console.log(res.statusCode);
});

req.end();`}`
    };
    
    return examples[selectedLanguage] || '// No example available';
  };

  const renderCodePanel = () => {
    const languages = [
      { id: 'curl', name: 'cURL', icon: <Terminal size={14} /> },
      { id: 'javascript', name: 'JavaScript', icon: <FileCode size={14} /> },
      { id: 'python', name: 'Python', icon: <Code size={14} /> },
      { id: 'nodejs', name: 'Node.js', icon: <Server size={14} /> },
      { id: 'java', name: 'Java', icon: <Coffee size={14} /> }
    ];

    const currentLanguage = languages.find(lang => lang.id === selectedLanguage);
    
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
              {currentLanguage?.icon}
              <span>{currentLanguage?.name}</span>
            </div>
            <ChevronDown size={14} style={{ color: colors.textSecondary }} />
          </button>

          {showLanguageDropdown && (
            <div className="absolute left-4 right-4 top-full mt-1 py-2 rounded shadow-lg z-50 border"
              style={{ 
                backgroundColor: colors.dropdownBg,
                borderColor: colors.border
              }}>
              {languages.map(lang => (
                <button
                  key={lang.id}
                  onClick={() => {
                    setSelectedLanguage(lang.id);
                    setShowLanguageDropdown(false);
                  }}
                  className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors"
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
          
          <SyntaxHighlighter 
            language={selectedLanguage === 'curl' ? 'bash' : selectedLanguage}
            code={generateCodeExample()}
          />
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
          <button 
            className="w-full mt-2 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center justify-center gap-2"
            onClick={() => {
              showToast('Running API test...', 'info');
              setTimeout(() => {
                showToast('API test completed successfully!', 'success');
              }, 2000);
            }}
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            <Play size={12} />
            Run in Console
          </button>
        </div>
      </div>
    );
  };

  const renderImportModal = () => {
    if (!showImportModal) return null;
    
    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="rounded-lg w-full max-w-lg" style={{ 
          backgroundColor: colors.modalBg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Import Collection</h3>
            <button onClick={() => setShowImportModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.hover }}>
              <X size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          <div className="p-4 space-y-4">
            <div className="text-center p-6 border-2 border-dashed rounded-lg" style={{ 
              borderColor: colors.border,
              backgroundColor: colors.hover
            }}>
              <UploadCloud size={32} className="mx-auto mb-4" style={{ color: colors.textSecondary }} />
              <p className="text-sm mb-4" style={{ color: colors.text }}>Drag and drop your file here</p>
              <p className="text-xs mb-4" style={{ color: colors.textSecondary }}>Supports: Postman Collection v2.1, OpenAPI 3.0, cURL</p>
              <button className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
                onClick={() => showToast('File browser would open', 'info')}>
                Browse Files
              </button>
            </div>
            <div className="grid grid-cols-2 gap-3">
              {['From Link', 'From GitHub', 'From Postman', 'From File'].map(source => (
                <button key={source} className="p-4 rounded text-sm text-left hover:bg-opacity-50 transition-colors"
                  onClick={() => showToast(`Importing ${source}`, 'info')}
                  style={{ 
                    backgroundColor: colors.hover,
                    border: `1px solid ${colors.border}`,
                    color: colors.text
                  }}>
                  <div className="font-medium">{source}</div>
                  <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
                    {source === 'From Link' ? 'Import from URL' :
                     source === 'From GitHub' ? 'Connect to GitHub' :
                     source === 'From Postman' ? 'Postman export' : 'Local file'}
                  </div>
                </button>
              ))}
            </div>
          </div>
          <div className="p-4 border-t" style={{ borderColor: colors.border }}>
            <div className="flex justify-end gap-2">
              <button onClick={() => setShowImportModal(false)} className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover, color: colors.text }}>
                Cancel
              </button>
              <button onClick={() => {
                showToast('Collection imported successfully!', 'success');
                setShowImportModal(false);
              }} className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                Import
              </button>
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
        <div className="rounded-lg w-full max-w-2xl" style={{ 
          backgroundColor: colors.modalBg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Settings</h3>
            <button onClick={() => setShowSettingsModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.hover }}>
              <X size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          <div className="p-4">
            <div className="grid grid-cols-3 gap-4 mb-6">
              {['General', 'Team', 'API', 'Security', 'Notifications', 'Billing'].map(setting => (
                <button key={setting} className="p-4 rounded text-center hover:bg-opacity-50 transition-colors"
                  onClick={() => showToast(`Opening ${setting} settings`, 'info')}
                  style={{ 
                    backgroundColor: colors.hover,
                    border: `1px solid ${colors.border}`,
                    color: colors.text
                  }}>
                  <div className="font-medium">{setting}</div>
                </button>
              ))}
            </div>
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="font-medium" style={{ color: colors.text }}>Dark Mode</div>
                  <div className="text-sm" style={{ color: colors.textSecondary }}>Toggle dark/light theme</div>
                </div>
                <button onClick={() => setTheme(isDark ? 'light' : 'dark')} className="relative inline-flex h-6 w-11 items-center rounded-full"
                  style={{ backgroundColor: isDark ? colors.primary : colors.border }}>
                  <span className={`inline-block h-4 w-4 transform rounded-full bg-white transition ${
                    isDark ? 'translate-x-6' : 'translate-x-1'
                  }`} />
                </button>
              </div>
              <div className="flex items-center justify-between">
                <div>
                  <div className="font-medium" style={{ color: colors.text }}>Auto-save</div>
                  <div className="text-sm" style={{ color: colors.textSecondary }}>Save changes automatically</div>
                </div>
                <button className="relative inline-flex h-6 w-11 items-center rounded-full"
                  style={{ backgroundColor: colors.primary }}
                  onClick={() => showToast('Auto-save toggled', 'info')}>
                  <span className="inline-block h-4 w-4 transform rounded-full bg-white translate-x-6" />
                </button>
              </div>
            </div>
          </div>
          <div className="p-4 border-t" style={{ borderColor: colors.border }}>
            <div className="flex justify-end gap-2">
              <button onClick={() => setShowSettingsModal(false)} className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover, color: colors.text }}>
                Cancel
              </button>
              <button onClick={() => {
                showToast('Settings saved!', 'success');
                setShowSettingsModal(false);
              }} className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                Save Changes
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderPublishModal = () => {
    if (!showPublishModal) return null;
    
    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="rounded-lg w-full max-w-lg" style={{ 
          backgroundColor: colors.modalBg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Publish Documentation</h3>
            <button onClick={() => setShowPublishModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.hover }}>
              <X size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          <div className="p-4 space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Documentation Title</label>
              <input
                type="text"
                defaultValue={`${selectedCollection.name} Documentation`}
                className="w-full px-3 py-2 border rounded text-sm focus:outline-none"
                style={{
                  backgroundColor: colors.inputBg,
                  borderColor: colors.border,
                  color: colors.text
                }}
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Visibility</label>
              <div className="grid grid-cols-3 gap-2">
                {[
                  { id: 'private', name: 'Private', desc: 'Only you' },
                  { id: 'team', name: 'Team', desc: 'Your workspace' },
                  { id: 'public', name: 'Public', desc: 'Anyone with link' }
                ].map(option => (
                  <button key={option.id} className={`p-3 rounded text-sm text-left transition-colors ${
                    option.id === 'public' ? '' : 'hover:bg-opacity-50'
                  }`}
                    style={{ 
                      backgroundColor: option.id === 'public' ? colors.selected : colors.hover,
                      border: option.id === 'public' ? `1px solid ${colors.primary}` : `1px solid ${colors.border}`,
                      color: colors.text
                    }}
                    onClick={() => showToast(`${option.name} visibility selected`, 'info')}>
                    <div className="font-medium">{option.name}</div>
                    <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>{option.desc}</div>
                  </button>
                ))}
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Custom Domain (Optional)</label>
              <input
                type="text"
                placeholder="docs.yourcompany.com"
                className="w-full px-3 py-2 border rounded text-sm focus:outline-none"
                style={{
                  backgroundColor: colors.inputBg,
                  borderColor: colors.border,
                  color: colors.text
                }}
              />
            </div>
          </div>
          <div className="p-4 border-t" style={{ borderColor: colors.border }}>
            <div className="flex justify-end gap-2">
              <button onClick={() => setShowPublishModal(false)} className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover, color: colors.text }}>
                Cancel
              </button>
              <button onClick={() => {
                generatePublishUrl();
              }} className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                {isGeneratingDocs ? (
                  <>
                    <RefreshCw size={12} className="animate-spin inline mr-2" />
                    Publishing...
                  </>
                ) : (
                  'Publish Now'
                )}
              </button>
            </div>
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
      <div className="fixed bottom-4 right-4 px-4 py-2 rounded text-sm font-medium z-50 animate-fade-in-up"
        style={{ 
          backgroundColor: bgColor,
          color: 'white'
        }}>
        {toast.message}
      </div>
    );
  };

  const renderDocumentationContent = () => {
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
                {selectedRequest.url.replace('https://api.fintech.com', activeEnv?.baseUrl || 'https://api.fintech.com')}
              </code>
            </div>
            <h1 className="text-2xl font-semibold mb-4" style={{ color: colors.text }}>
              {selectedRequest.name}
            </h1>
            <p className="text-base mb-6" style={{ color: colors.textSecondary }}>
              {selectedRequest.description}
            </p>
            
            <div className="flex flex-wrap items-center gap-4 text-sm mb-6">
              <div style={{ color: colors.textTertiary }}>
                <Folder size={12} className="inline mr-1" />
                {selectedCollection.name} › {selectedCollection.folders.find(f => f.requests?.some(r => r.id === selectedRequest.id))?.name}
              </div>
              <div className="flex items-center gap-2">
                {selectedRequest.tags?.map(tag => (
                  <span key={tag} className="text-xs px-2 py-1 rounded" style={{ 
                    backgroundColor: `${colors.primary}20`,
                    color: colors.primary
                  }}>
                    {tag}
                  </span>
                ))}
              </div>
              <div style={{ color: colors.textTertiary }}>
                <Clock size={12} className="inline mr-1" />
                Last updated: {selectedRequest.lastModified}
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
              
              {selectedRequest.headers && (
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
                        {selectedRequest.headers.map((header, index) => (
                          <tr key={index} className="border-b last:border-b-0" style={{ 
                            borderColor: colors.borderLight,
                            backgroundColor: colors.tableRow
                          }}>
                            <td className="px-4 py-3 font-medium" style={{ color: colors.text }}>{header.key}</td>
                            <td className="px-4 py-3 font-mono text-sm" style={{ color: colors.textSecondary }}>
                              <code>{header.value}</code>
                            </td>
                            <td className="px-4 py-3 text-sm" style={{ color: colors.textSecondary }}>
                              {header.key === 'Content-Type' ? 'Specifies the request body format' : 
                               header.key === 'Authorization' ? 'Bearer token for authentication' : 
                               header.key === 'X-Client-Id' ? 'Client application identifier' :
                               header.key === 'X-Transaction-Id' ? 'Unique transaction identifier' : 'Required header'}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {selectedRequest.body && (
                <div className="mb-8">
                  <h3 className="text-lg font-medium mb-4" style={{ color: colors.text }}>Body Parameters</h3>
                  <div className="border rounded overflow-hidden" style={{ borderColor: colors.border }}>
                    <div className="p-4 border-b flex items-center justify-between" style={{ 
                      backgroundColor: colors.tableHeader,
                      borderColor: colors.border
                    }}>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-medium" style={{ color: colors.text }}>
                          {selectedRequest.headers?.find(h => h.key === 'Content-Type')?.value.includes('json') ? 'JSON' : 'Form Data'}
                        </span>
                        <button className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors"
                          style={{ backgroundColor: colors.hover, color: colors.textSecondary }}
                          onClick={() => {
                            try {
                              const parsed = JSON.parse(selectedRequest.body);
                              const newRequest = { ...selectedRequest, body: JSON.stringify(parsed, null, 2) };
                              setSelectedRequest(newRequest);
                              showToast('JSON beautified!', 'success');
                            } catch (e) {
                              showToast('Not valid JSON', 'error');
                            }
                          }}>
                          Beautify
                        </button>
                      </div>
                      <button className="p-1.5 rounded hover:bg-opacity-50 transition-colors"
                        style={{ backgroundColor: colors.hover }}
                        onClick={() => copyToClipboard(selectedRequest.body)}>
                        <Copy size={12} style={{ color: colors.textSecondary }} />
                      </button>
                    </div>
                    <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
                      {selectedRequest.headers?.find(h => h.key === 'Content-Type')?.value.includes('json') ? (
                        <SyntaxHighlighter language="json" code={selectedRequest.body} />
                      ) : (
                        <pre className="text-xs font-mono whitespace-pre-wrap" style={{ color: colors.text }}>
                          {selectedRequest.body}
                        </pre>
                      )}
                    </div>
                  </div>
                </div>
              )}

              <div>
                <h3 className="text-lg font-medium mb-4" style={{ color: colors.text }}>Description</h3>
                <div className="prose max-w-none" style={{ color: colors.textSecondary }}>
                  <p>
                    This endpoint is part of the FinTech Core Banking API v{selectedCollection.version}. It provides secure transaction 
                    processing capabilities for financial operations with built-in compliance and audit trail.
                  </p>
                  <p className="mt-3">
                    <strong>Rate Limits:</strong> This endpoint has a rate limit of 100 requests per minute per client.
                  </p>
                  <p className="mt-3">
                    <strong>Security Note:</strong> All requests must be made over HTTPS. Client authentication is required via API key or OAuth 2.0.
                  </p>
                  <p className="mt-3">
                    <strong>Compliance:</strong> All transactions are logged for AML and audit purposes. Large transactions may trigger additional verification.
                  </p>
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
                {/* Success Response */}
                <div>
                  <div className="flex items-center gap-3 mb-4">
                    <div className="flex items-center gap-2 px-3 py-1.5 rounded text-sm font-medium" style={{ 
                      backgroundColor: `${colors.success}20`,
                      color: colors.success
                    }}>
                      <CheckCircle size={12} />
                      Success Response (200 OK)
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
                        onClick={() => copyToClipboard(JSON.stringify({
                          success: true,
                          transactionId: "TXN202403150001",
                          accountNumber: "ACC987654321",
                          newBalance: 15000.00,
                          referenceNumber: "DEP20240315001",
                          timestamp: "2024-03-15T09:30:00Z"
                        }, null, 2))}>
                        <Copy size={12} style={{ color: colors.textSecondary }} />
                      </button>
                    </div>
                    <div className="p-4" style={{ backgroundColor: colors.codeBg }}>
                      <SyntaxHighlighter 
                        language="json"
                        code={JSON.stringify({
                          success: true,
                          transactionId: "TXN202403150001",
                          accountNumber: "ACC987654321",
                          newBalance: 15000.00,
                          referenceNumber: "DEP20240315001",
                          timestamp: "2024-03-15T09:30:00Z",
                          message: "Transaction completed successfully"
                        }, null, 2)}
                      />
                    </div>
                  </div>
                </div>

                {/* Error Responses */}
                <div>
                  <h3 className="text-lg font-medium mb-4" style={{ color: colors.text }}>Common Error Responses</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {[
                      { code: 400, title: 'Bad Request', desc: 'Invalid request parameters', example: { error: { code: 'VALIDATION_ERROR', message: 'Invalid account number format' } }},
                      { code: 401, title: 'Unauthorized', desc: 'Invalid or expired access token', example: { error: { code: 'AUTH_FAILED', message: 'Invalid authorization token' } }},
                      { code: 403, title: 'Forbidden', desc: 'Insufficient permissions', example: { error: { code: 'PERMISSION_DENIED', message: 'User lacks required permissions' } }},
                      { code: 429, title: 'Too Many Requests', desc: 'Rate limit exceeded', example: { error: { code: 'RATE_LIMITED', message: 'Rate limit exceeded. Please try again later.' } }}
                    ].map(error => (
                      <div key={error.code} className="border rounded overflow-hidden cursor-pointer hover:border-opacity-50 transition-colors hover-lift"
                        style={{ borderColor: colors.border }}
                        onClick={() => showToast(`Viewing ${error.title} details`, 'info')}>
                        <div className="px-4 py-3 border-b flex items-center gap-3" style={{ 
                          backgroundColor: `${colors.error}20`,
                          borderColor: colors.border
                        }}>
                          <span className="text-sm font-medium" style={{ color: colors.error }}>{error.code} {error.title}</span>
                        </div>
                        <div className="p-4" style={{ backgroundColor: colors.card }}>
                          <p className="text-sm mb-3" style={{ color: colors.textSecondary }}>{error.desc}</p>
                          <button className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors"
                            style={{ backgroundColor: colors.hover, color: colors.text }}
                            onClick={(e) => {
                              e.stopPropagation();
                              copyToClipboard(JSON.stringify(error.example, null, 2));
                            }}>
                            Copy Example
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </section>

            {/* Publish Section */}
            {publishUrl && (
              <section>
                <div className="p-6 rounded border" style={{ 
                  backgroundColor: colors.card,
                  borderColor: colors.border
                }}>
                  <div className="flex items-start justify-between">
                    <div>
                      <h3 className="text-lg font-semibold mb-2 flex items-center gap-2" style={{ color: colors.text }}>
                        <ExternalLink size={18} />
                        Published Documentation
                      </h3>
                      <p className="text-sm mb-4" style={{ color: colors.textSecondary }}>
                        Your documentation is now live and accessible via the following URL:
                      </p>
                      <div className="flex items-center gap-2">
                        <input
                          type="text"
                          readOnly
                          value={publishUrl}
                          className="flex-1 px-3 py-2 border rounded text-sm focus:outline-none font-mono"
                          style={{
                            backgroundColor: colors.inputBg,
                            borderColor: colors.border,
                            color: colors.text
                          }}
                        />
                        <button className="px-3 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors"
                          onClick={() => copyToClipboard(publishUrl)}
                          style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                          Copy
                        </button>
                        <button className="px-3 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
                          onClick={() => window.open(publishUrl, '_blank')}
                          style={{ backgroundColor: colors.hover, color: colors.text }}>
                          Open
                        </button>
                      </div>
                    </div>
                    <CheckCircle size={24} style={{ color: colors.success }} />
                  </div>
                </div>
              </section>
            )}
          </div>
        </div>
      </div>
    );
  };

  const renderMainContent = () => {
    switch (activeMainTab) {
      case 'APIs':
        return (
          <div className="flex-1 flex items-center justify-center p-8">
            <div className="text-center max-w-lg" style={{ color: colors.textSecondary }}>
              <Globe size={48} className="mx-auto mb-4 opacity-50" />
              <h2 className="text-xl font-semibold mb-3" style={{ color: colors.text }}>API Network</h2>
              <p className="mb-6">Connect to external APIs, manage API gateways, and monitor API performance.</p>
              <div className="grid grid-cols-2 gap-3 mb-6">
                {['Add API', 'API Gateway', 'Monitor', 'Analytics'].map(item => (
                  <button key={item} className="p-4 rounded hover:bg-opacity-50 transition-colors hover-lift"
                    onClick={() => showToast(`Opening ${item}`, 'info')}
                    style={{ 
                      backgroundColor: colors.hover,
                      border: `1px solid ${colors.border}`,
                      color: colors.text
                    }}>
                    {item}
                  </button>
                ))}
              </div>
              <button className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                onClick={() => showToast('Connecting to API Network...', 'info')}
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                Connect API
              </button>
            </div>
          </div>
        );
        
      case 'Environments':
        return (
          <div className="flex-1 p-8">
            <div className="max-w-4xl mx-auto">
              <h2 className="text-2xl font-semibold mb-6" style={{ color: colors.text }}>Environments</h2>
              <p className="mb-6" style={{ color: colors.textSecondary }}>Manage different environments for your APIs (Development, Testing, Production, etc.)</p>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                {environments.map(env => (
                  <div key={env.id} className="border rounded p-4 hover-lift" style={{ 
                    borderColor: env.isActive ? colors.primary : colors.border,
                    backgroundColor: colors.card
                  }}>
                    <div className="flex items-center justify-between mb-3">
                      <div className="flex items-center gap-2">
                        {env.isActive ? (
                          <div className="w-2 h-2 rounded-full" style={{ backgroundColor: colors.success }}></div>
                        ) : (
                          <div className="w-2 h-2 rounded-full" style={{ backgroundColor: colors.textTertiary }}></div>
                        )}
                        <h3 className="font-medium" style={{ color: colors.text }}>{env.name}</h3>
                      </div>
                      {env.isActive && (
                        <span className="text-xs px-2 py-1 rounded" style={{ 
                          backgroundColor: `${colors.success}20`,
                          color: colors.success
                        }}>
                          Active
                        </span>
                      )}
                    </div>
                    <p className="text-sm mb-3 font-mono" style={{ color: colors.textSecondary }}>{env.baseUrl}</p>
                    <div className="flex gap-2">
                      <button className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors flex-1 hover-lift"
                        onClick={() => handleEnvironmentChange(env.id)}
                        style={{ 
                          backgroundColor: env.isActive ? colors.selected : colors.primary,
                          color: env.isActive ? colors.primary : 'white'
                        }}>
                        {env.isActive ? 'Active' : 'Switch to'}
                      </button>
                      <button className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                        onClick={() => showToast(`Editing ${env.name} environment`, 'info')}
                        style={{ backgroundColor: colors.hover }}>
                        <Edit2 size={12} style={{ color: colors.textSecondary }} />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
              
              <button className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift"
                onClick={() => showToast('Creating new environment', 'info')}
                style={{ backgroundColor: colors.hover, color: colors.text }}>
                <Plus size={14} />
                Add New Environment
              </button>
            </div>
          </div>
        );
        
      case 'Mock Servers':
        return (
          <div className="flex-1 flex items-center justify-center p-8">
            <div className="text-center max-w-lg" style={{ color: colors.textSecondary }}>
              <Server size={48} className="mx-auto mb-4 opacity-50" />
              <h2 className="text-xl font-semibold mb-3" style={{ color: colors.text }}>Mock Servers</h2>
              <p className="mb-6">Create mock servers to simulate API responses for testing and development.</p>
              <div className="space-y-3 mb-6">
                {['Create Mock Server', 'View Mock Logs', 'Mock Templates', 'Monitor Mock Usage'].map(item => (
                  <button key={item} className="w-full p-3 rounded hover:bg-opacity-50 transition-colors text-left hover-lift"
                    onClick={() => showToast(`Opening ${item}`, 'info')}
                    style={{ 
                      backgroundColor: colors.hover,
                      border: `1px solid ${colors.border}`,
                      color: colors.text
                    }}>
                    {item}
                  </button>
                ))}
              </div>
              <button className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                onClick={() => showToast('Creating mock server...', 'info')}
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                New Mock Server
              </button>
            </div>
          </div>
        );
        
      case 'Monitors':
        return (
          <div className="flex-1 flex items-center justify-center p-8">
            <div className="text-center max-w-lg" style={{ color: colors.textSecondary }}>
              <Activity size={48} className="mx-auto mb-4 opacity-50" />
              <h2 className="text-xl font-semibold mb-3" style={{ color: colors.text }}>API Monitors</h2>
              <p className="mb-6">Monitor API health, performance, and uptime with scheduled checks and alerts.</p>
              <div className="space-y-4 mb-6">
                <div className="p-4 rounded border hover-lift" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
                  <div className="flex items-center justify-between mb-2">
                    <span style={{ color: colors.text }}>API Health</span>
                    <span className="text-xs px-2 py-1 rounded" style={{ backgroundColor: `${colors.success}20`, color: colors.success }}>Healthy</span>
                  </div>
                  <div className="text-sm" style={{ color: colors.textSecondary }}>24/7 monitoring active</div>
                </div>
                <div className="p-4 rounded border hover-lift" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
                  <div className="flex items-center justify-between mb-2">
                    <span style={{ color: colors.text }}>Performance</span>
                    <span className="text-xs px-2 py-1 rounded" style={{ backgroundColor: `${colors.warning}20`, color: colors.warning }}>98.7%</span>
                  </div>
                  <div className="text-sm" style={{ color: colors.textSecondary }}>Response time: 142ms avg</div>
                </div>
              </div>
              <button className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                onClick={() => showToast('Creating new monitor...', 'info')}
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                Add Monitor
              </button>
            </div>
          </div>
        );
        
      case 'Documentation':
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
                    setActiveTab('comments');
                    showToast('Comments feature would open', 'info');
                  }}
                  className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${
                    activeTab === 'comments' ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === 'comments' ? colors.primary : 'transparent',
                    color: activeTab === 'comments' ? colors.primary : colors.textSecondary,
                    backgroundColor: 'transparent'
                  }}>
                  Comments
                </button>
                
                <button
                  onClick={() => {
                    setActiveTab('changelog');
                    showToast('Viewing API changelog', 'info');
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
            
            {activeTab === 'comments' && (
              <div className="flex-1 flex items-center justify-center">
                <div className="text-center p-8" style={{ color: colors.textSecondary }}>
                  <MessageSquare size={48} className="mx-auto mb-4 opacity-50" />
                  <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>Comments</h3>
                  <p className="mb-4">Team collaboration and comments feature</p>
                  <button className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors hover-lift"
                    onClick={() => showToast('Enable comments feature', 'info')}
                    style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
                    Enable Comments
                  </button>
                </div>
              </div>
            )}
            
            {activeTab === 'changelog' && (
              <div className="flex-1 p-8">
                <div className="max-w-4xl mx-auto">
                  <h2 className="text-2xl font-semibold mb-6" style={{ color: colors.text }}>API Changelog</h2>
                  <div className="space-y-6">
                    {[
                      { version: 'v2.1', date: 'March 2024', changes: ['Added real-time transaction notifications', 'Enhanced AML compliance checks', 'Improved error handling for failed transactions'] },
                      { version: 'v2.0', date: 'December 2023', changes: ['New payment processing endpoints', 'Enhanced security with 2FA', 'Added bulk transaction support'] },
                      { version: 'v1.5', date: 'September 2023', changes: ['Account statement enhancements', 'Performance improvements', 'Deprecated legacy authentication'] }
                    ].map(release => (
                      <div key={release.version} className="border rounded p-6 hover:border-opacity-50 transition-colors hover-lift cursor-pointer"
                        style={{ borderColor: colors.border }}
                        onClick={() => showToast(`Viewing ${release.version} release notes`, 'info')}>
                        <div className="flex items-center justify-between mb-3">
                          <h3 className="text-lg font-semibold" style={{ color: colors.text }}>Version {release.version}</h3>
                          <span className="text-sm" style={{ color: colors.textSecondary }}>{release.date}</span>
                        </div>
                        <ul className="space-y-2">
                          {release.changes.map((change, idx) => (
                            <li key={idx} className="flex items-start gap-2 text-sm" style={{ color: colors.textSecondary }}>
                              <Check size={12} className="mt-0.5 flex-shrink-0" style={{ color: colors.success }} />
                              {change}
                            </li>
                          ))}
                        </ul>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
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

      {/* TOP NAVIGATION */}
      <div className="flex items-center justify-between h-10 px-4 border-b" style={{ 
        backgroundColor: colors.header,
        borderColor: colors.border
      }}>
        <div className="flex items-center gap-4">
          <div className="relative">
            {/* <button className="flex items-center gap-2 px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift"
              onClick={() => setShowWorkspaceSwitcher(!showWorkspaceSwitcher)}
              style={{ backgroundColor: colors.hover }}>
              <div className="w-3 h-3 rounded" style={{ backgroundColor: colors.primary }}></div>
              FinTech Workspace
              <ChevronDown size={14} style={{ color: colors.textSecondary }} />
            </button> */}

            {showWorkspaceSwitcher && (
              <div className="absolute top-full left-0 mt-1 py-2 rounded shadow-lg z-50 border min-w-48"
                style={{ 
                  backgroundColor: colors.dropdownBg,
                  borderColor: colors.border
                }}>
                {['FinTech Workspace', 'Personal Workspace', 'Team Collaboration', 'Production'].map(workspace => (
                  <button
                    key={workspace}
                    onClick={() => {
                      showToast(`Switching to ${workspace}`, 'info');
                      setShowWorkspaceSwitcher(false);
                    }}
                    className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors hover-lift"
                    style={{ 
                      backgroundColor: workspace === 'FinTech Workspace' ? colors.selected : 'transparent',
                      color: workspace === 'FinTech Workspace' ? colors.primary : colors.text
                    }}
                  >
                    <div className="w-2 h-2 rounded-full" style={{ 
                      backgroundColor: workspace === 'FinTech Workspace' ? colors.primary : colors.textSecondary 
                    }}></div>
                    {workspace}
                    {workspace === 'FinTech Workspace' && <Check size={14} className="ml-auto" />}
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className="flex items-center gap-1 -ml-7 text-nowrap">
            {/* {['Collections', 'APIs', 'Documentation', 'Environments', 'Mock Servers', 'Monitors'].map(tab => ( */}
            {/* {['Collections', 'Documentation', 'Environments', 'Mock Servers'].map(tab => (
              <button key={tab} className={`px-3 py-1.5 text-sm font-medium rounded transition-colors hover-lift ${
                tab === activeMainTab ? '' : 'hover:bg-opacity-50'
              }`}
                onClick={() => {
                  setActiveMainTab(tab);
                  showToast(`Switching to ${tab}`, 'info');
                }}
                style={{ 
                  backgroundColor: tab === activeMainTab ? colors.selected : 'transparent',
                  color: tab === activeMainTab ? colors.primary : colors.textSecondary
                }}>
                {tab}
              </button>
            ))} */}
            <span className={`px-3 py-1.5 text-sm font-medium rounded transition-colors hover-lift`}>API Documentation</span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Environment Selector */}
          <div className="relative">
            <button className="flex items-center gap-2 px-3 py-1.5 rounded text-sm hover:bg-opacity-50 transition-colors hover-lift"
              onClick={() => setShowEnvironmentMenu(!showEnvironmentMenu)}
              style={{ backgroundColor: colors.hover }}>
              <Globe size={12} style={{ color: colors.textSecondary }} />
              <span style={{ color: colors.text }}>{environments.find(e => e.isActive)?.name}</span>
              <ChevronDown size={12} style={{ color: colors.textSecondary }} />
            </button>

            {showEnvironmentMenu && (
              <div className="absolute top-full right-0 mt-1 py-2 rounded shadow-lg z-50 border min-w-48"
                style={{ 
                  backgroundColor: colors.dropdownBg,
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
                <div className="border-t my-2" style={{ borderColor: colors.border }}></div>
                <button
                  onClick={() => {
                    showToast('Creating new environment', 'info');
                    setShowEnvironmentMenu(false);
                  }}
                  className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ color: colors.text }}
                >
                  <Plus size={14} />
                  New Environment
                </button>
              </div>
            )}
          </div>

          <div className="w-px h-4" style={{ backgroundColor: colors.border }}></div>

          {/* Global Search */}
          {/* <div className="relative">
            <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
            <input 
              type="text" 
              placeholder="Search documentation, endpoints..."
              value={globalSearchQuery}
              onChange={(e) => setGlobalSearchQuery(e.target.value)}
              className="pl-8 pr-3 py-1.5 rounded text-sm focus:outline-none w-64 hover-lift"
              style={{ 
                backgroundColor: colors.inputBg, 
                border: `1px solid ${colors.border}`, 
                color: colors.text 
              }} 
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
          <button onClick={() => setShowCodePanel(!showCodePanel)} 
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: showCodePanel ? colors.selected : colors.hover }}>
            <Code size={14} style={{ color: showCodePanel ? colors.primary : colors.textSecondary }} />
          </button>

          {/* Publish Button */}
          <button onClick={() => setShowPublishModal(true)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}>
            <ExternalLink size={14} style={{ color: colors.textSecondary }} />
          </button>

          {/* User Menu */}
          <div className="relative">
            {/* <button onClick={() => setShowUserMenu(!showUserMenu)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}>
              <User size={14} style={{ color: colors.textSecondary }} />
            </button> */}

            {showUserMenu && (
              <div className="absolute top-full right-0 mt-1 py-2 rounded shadow-lg z-50 border min-w-48"
                style={{ 
                  backgroundColor: colors.dropdownBg,
                  borderColor: colors.border
                }}>
                <div className="px-4 py-3 border-b" style={{ borderColor: colors.border }}>
                  <div className="text-sm font-medium" style={{ color: colors.text }}>John Doe</div>
                  <div className="text-xs" style={{ color: colors.textSecondary }}>john@fintech.com</div>
                </div>
                {['Profile', 'Account Settings', 'Billing', 'Team', 'API Keys'].map(item => (
                  <button
                    key={item}
                    onClick={() => {
                      showToast(`Opening ${item}`, 'info');
                      setShowUserMenu(false);
                    }}
                    className="w-full px-4 py-2 text-sm text-left hover:bg-opacity-50 transition-colors hover-lift"
                    style={{ color: colors.text }}
                  >
                    {item}
                  </button>
                ))}
                <div className="border-t my-2" style={{ borderColor: colors.border }}></div>
                <button
                  onClick={() => {
                    showToast('Opening settings', 'info');
                    setShowSettingsModal(true);
                    setShowUserMenu(false);
                  }}
                  className="w-full px-4 py-2 text-sm text-left hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ color: colors.text }}
                >
                  <Settings size={12} className="inline mr-2" />
                  Settings
                </button>
                <button
                  onClick={() => {
                    showToast('Signing out...', 'info');
                    setShowUserMenu(false);
                  }}
                  className="w-full px-4 py-2 text-sm text-left hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ color: colors.error }}
                >
                  Sign Out
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* MAIN CONTENT */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left Sidebar - Collections (only shown for Collections tab) */}
        {activeMainTab === 'Collections' && (
          <div className="w-64 border-r flex flex-col" style={{ 
            backgroundColor: colors.sidebar,
            borderColor: colors.border
          }}>
            <div className="p-4 border-b" style={{ borderColor: colors.border }}>
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Collections</h3>
                <button className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  onClick={() => showToast('Create new collection', 'info')}
                  style={{ backgroundColor: colors.hover }}>
                  <Plus size={12} style={{ color: colors.textSecondary }} />
                </button>
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
                    
                    <button 
                      onClick={(e) => {
                        e.stopPropagation();
                        showToast(`Editing ${collection.name}`, 'info');
                      }}
                      className="p-1 rounded opacity-0 hover:opacity-100 hover:bg-opacity-50 transition-all hover-lift"
                      style={{ backgroundColor: colors.card }}>
                      <Edit2 size={11} style={{ color: colors.textSecondary }} />
                    </button>
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
                            
                            <button 
                              onClick={(e) => {
                                e.stopPropagation();
                                showToast(`Editing ${folder.name} folder`, 'info');
                              }}
                              className="p-1 rounded opacity-0 hover:opacity-100 hover:bg-opacity-50 transition-all hover-lift"
                              style={{ backgroundColor: colors.card }}>
                              <Edit2 size={10} style={{ color: colors.textSecondary }} />
                            </button>
                          </div>

                          {expandedFolders.includes(folder.id) && folder.requests && (
                            <>
                              {folder.requests.map(request => (
                                <div key={request.id} className="flex items-center gap-2 ml-6 mb-1.5 group">
                                  <button
                                    onClick={() => handleSelectRequest(request, collection, folder)}
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
                                  
                                  <button 
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      showToast(`Editing ${request.name} request`, 'info');
                                    }}
                                    className="p-1 rounded opacity-0 group-hover:opacity-100 hover:bg-opacity-50 transition-all mr-2 hover-lift"
                                    style={{ backgroundColor: colors.card }}>
                                    <Edit2 size={10} style={{ color: colors.textSecondary }} />
                                  </button>
                                </div>
                              ))}
                            </>
                          )}
                        </div>
                      ))}
                    </>
                  )}
                </div>
              ))}
              
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
        )}

        {/* Main Content Area */}
        {renderMainContent()}

        {/* Right Code Panel */}
        {showCodePanel && renderCodePanel()}
      </div>

      {/* MODALS */}
      {renderImportModal()}
      {renderSettingsModal()}
      {renderPublishModal()}

      {/* TOAST */}
      {renderToast()}
    </div>
  );
};

export default APIDocs;