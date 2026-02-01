import React, { useState, useEffect } from 'react';
import { 
  Home, Database, Code, Activity, BarChart, Users, Settings, 
  Search, Bell, ChevronRight, TrendingUp, TrendingDown,
  CheckCircle, XCircle, AlertCircle, Clock, Zap, Shield,
  Server, Globe, HardDrive, Cpu, Network, FileText, Plus,
  RefreshCw, MoreVertical, Download, Filter, ArrowUpRight,
  Circle, CircleDot, Target, Rocket, Sparkles, FolderTree,
  Table, Key, Index, View, Package, Terminal as Procedure, PauseCircle as Function,
  Eye, EyeOff, Sun, Moon, ChevronDown, ChevronLeft, ChevronUp,
  Copy, Edit2, Trash2, Play, StopCircle, PauseCircle,
  Terminal, Hash, Type, Text, Calendar, Link, Unlink,
  ShieldCheck, Lock, Unlock, Cpu as CPU, Layers,
  Grid, List, Columns, Database as DB, Filter as FilterIcon,
  SortAsc, SortDesc, Maximize2, Minimize2, ExternalLink,
  X, Menu, Sidebar, SidebarClose, Sparkle, Wand2,
  DatabaseZap, FileJson, ServerCog, Cogs
} from 'lucide-react';

const Dashboard = () => {
  const [theme, setTheme] = useState('dark');
  const [showRightPanel, setShowRightPanel] = useState(true);
  const [selectedSchema, setSelectedSchema] = useState('HR');
  const [selectedObject, setSelectedObject] = useState(null);
  const [expandedSchemas, setExpandedSchemas] = useState(['HR']);
  const [isGeneratingAPI, setIsGeneratingAPI] = useState(false);
  const [apiConfig, setApiConfig] = useState({
    objectType: 'PROCEDURE',
    endpointPath: '',
    securityLevel: 'medium',
    includeSwagger: true,
    includeTests: true
  });
  const [recentActivity, setRecentActivity] = useState([
    { id: 1, action: 'API Generated', description: 'HR Employee Management', time: '2 min ago', status: 'success', icon: Rocket },
    { id: 2, action: 'Oracle Connection', description: 'PROD_ORA_001 added', time: '15 min ago', status: 'success', icon: Database },
    { id: 3, action: 'Performance Alert', description: 'High PL/SQL time', time: '1 hour ago', status: 'warning', icon: AlertCircle },
    { id: 4, action: 'Security Scan', description: 'TDE compliance check', time: '3 hours ago', status: 'success', icon: ShieldCheck }
  ]);
  const [apiStats, setApiStats] = useState({
    total: 147,
    active: 123,
    oracleInstances: 3
  });
  const [generatedAPIs, setGeneratedAPIs] = useState([]);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedQuickTemplate, setSelectedQuickTemplate] = useState(null);
  const [activeTab, setActiveTab] = useState('generate'); // 'generate' or 'browse'

  // Detect system theme preference
  useEffect(() => {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const savedTheme = localStorage.getItem('api-platform-theme') || (prefersDark ? 'dark' : 'light');
    setTheme(savedTheme);
  }, []);

  const toggleTheme = () => {
    const newTheme = theme === 'dark' ? 'light' : 'dark';
    setTheme(newTheme);
    localStorage.setItem('api-platform-theme', newTheme);
  };

  const isDark = theme === 'dark';
  
  // Industry-standard colors for big tech
  const colors = isDark ? {
    bg: '#0f172a',
    card: '#1e293b',
    cardLight: '#334155',
    cardLighter: '#475569',
    text: '#f8fafc',
    textSecondary: '#cbd5e1',
    textTertiary: '#94a3b8',
    border: '#475569',
    borderLight: '#64748b',
    hover: '#334155',
    primary: '#3b82f6',
    primaryLight: '#60a5fa',
    primaryDark: '#2563eb',
    secondary: '#8b5cf6',
    success: '#10b981',
    warning: '#f59e0b',
    error: '#ef4444',
    info: '#06b6d4',
    oracle: '#f80000',
    oracleLight: '#ff4d4d',
    highlight: '#fbbf24',
    selected: 'rgba(59, 130, 246, 0.2)'
  } : {
    bg: '#f8fafc',
    card: '#ffffff',
    cardLight: '#f1f5f9',
    cardLighter: '#e2e8f0',
    text: '#0f172a',
    textSecondary: '#475569',
    textTertiary: '#64748b',
    border: '#cbd5e1',
    borderLight: '#e2e8f0',
    hover: '#f1f5f9',
    primary: '#2563eb',
    primaryLight: '#3b82f6',
    primaryDark: '#1d4ed8',
    secondary: '#7c3aed',
    success: '#059669',
    warning: '#d97706',
    error: '#dc2626',
    info: '#0891b2',
    oracle: '#c41e3a',
    oracleLight: '#e63946',
    highlight: '#f59e0b',
    selected: 'rgba(37, 99, 235, 0.1)'
  };

  // Oracle database schemas with more objects
  const oracleSchemas = [
    {
      name: 'HR',
      description: 'Human Resources',
      objects: {
        tables: [
          { name: 'EMPLOYEES', type: 'TABLE', columns: 11, rows: 107 },
          { name: 'DEPARTMENTS', type: 'TABLE', columns: 4, rows: 27 },
          { name: 'JOBS', type: 'TABLE', columns: 4, rows: 19 },
          { name: 'LOCATIONS', type: 'TABLE', columns: 6, rows: 23 },
          { name: 'JOB_HISTORY', type: 'TABLE', columns: 6, rows: 10 }
        ],
        procedures: [
          { name: 'ADD_JOB_HISTORY', type: 'PROCEDURE', parameters: 6 },
          { name: 'UPDATE_SALARY', type: 'PROCEDURE', parameters: 2 },
          { name: 'ADD_EMPLOYEE', type: 'PROCEDURE', parameters: 9 }
        ],
        functions: [
          { name: 'GET_EMPLOYEE_SALARY', type: 'FUNCTION', parameters: 1 },
          { name: 'CALCULATE_BONUS', type: 'FUNCTION', parameters: 2 }
        ],
        packages: [
          { name: 'EMP_MANAGEMENT', type: 'PACKAGE', procedures: 5 }
        ]
      }
    },
    {
      name: 'OE',
      description: 'Order Entry',
      objects: {
        tables: [
          { name: 'CUSTOMERS', type: 'TABLE', columns: 8, rows: 319 },
          { name: 'ORDERS', type: 'TABLE', columns: 10, rows: 105 },
          { name: 'ORDER_ITEMS', type: 'TABLE', columns: 6, rows: 665 },
          { name: 'PRODUCTS', type: 'TABLE', columns: 7, rows: 288 },
          { name: 'INVENTORIES', type: 'TABLE', columns: 3, rows: 1112 }
        ],
        procedures: [
          { name: 'PROCESS_ORDER', type: 'PROCEDURE', parameters: 8 },
          { name: 'UPDATE_INVENTORY', type: 'PROCEDURE', parameters: 3 }
        ],
        functions: [
          { name: 'CALCULATE_ORDER_TOTAL', type: 'FUNCTION', parameters: 1 }
        ],
        packages: [
          { name: 'ORDER_MANAGEMENT', type: 'PACKAGE', procedures: 4 }
        ]
      }
    },
    {
      name: 'SH',
      description: 'Sales History',
      objects: {
        tables: [
          { name: 'SALES', type: 'TABLE', columns: 9, rows: 918843 },
          { name: 'COSTS', type: 'TABLE', columns: 6, rows: 852132 }
        ],
        procedures: [
          { name: 'GENERATE_SALES_REPORT', type: 'PROCEDURE', parameters: 3 },
          { name: 'UPDATE_FORECAST', type: 'PROCEDURE', parameters: 4 }
        ]
      }
    }
  ];

  const mainStats = [
    { id: 'apis', title: 'Total APIs', value: apiStats.total, change: '+12%', trend: 'up', icon: <Code size={20} />, color: colors.primary },
    { id: 'active', title: 'Active APIs', value: apiStats.active, change: '+8%', trend: 'up', icon: <Activity size={20} />, color: colors.success },
    { id: 'databases', title: 'Oracle Instances', value: apiStats.oracleInstances, change: '+1', trend: 'up', icon: <Database size={20} />, color: colors.oracle }
  ];

  const quickTemplates = [
    { id: 'crud', name: 'CRUD API', desc: 'Full CRUD operations', icon: <Database size={14} />, color: colors.success, config: { methods: ['GET', 'POST', 'PUT', 'DELETE'], includeSwagger: true, securityLevel: 'high' } },
    { id: 'readonly', name: 'Read Only', desc: 'GET endpoints only', icon: <Eye size={14} />, color: colors.info, config: { methods: ['GET'], includeSwagger: true, securityLevel: 'medium' } },
    { id: 'bulk', name: 'Bulk Insert', desc: 'Batch operations', icon: <Copy size={14} />, color: colors.warning, config: { methods: ['POST'], includeSwagger: true, includeBatch: true, securityLevel: 'medium' } },
    { id: 'audit', name: 'Audit Log', desc: 'With logging', icon: <FileText size={14} />, color: colors.secondary, config: { methods: ['GET', 'POST', 'PUT', 'DELETE'], includeAudit: true, securityLevel: 'high' } }
  ];

  const topAPIs = [
    { id: 1, name: 'Employee Lookup', method: 'GET', endpoint: '/api/v1/hr/employees/{id}', calls: 12450, success: 99.9, responseTime: '45ms' },
    { id: 2, name: 'Process Order', method: 'POST', endpoint: '/api/v1/oe/orders', calls: 9870, success: 99.8, responseTime: '120ms' },
    { id: 3, name: 'Update Salary', method: 'PUT', endpoint: '/api/v1/hr/salary', calls: 6920, success: 99.7, responseTime: '85ms' },
    { id: 4, name: 'Sales Report', method: 'GET', endpoint: '/api/v1/sh/sales/report', calls: 5210, success: 99.9, responseTime: '210ms' },
    { id: 5, name: 'Add Employee', method: 'POST', endpoint: '/api/v1/hr/employees', calls: 3240, success: 99.5, responseTime: '65ms' }
  ];

  const generateAPI = () => {
    if (!selectedObject) {
      alert('Please select an Oracle object first');
      return;
    }

    setIsGeneratingAPI(true);
    
    // Simulate API generation process
    setTimeout(() => {
      const newAPI = {
        id: Date.now(),
        name: `${selectedObject.name}_API`,
        object: selectedObject.name,
        schema: selectedSchema,
        type: selectedObject.type,
        endpoint: `/api/v1/${selectedSchema.toLowerCase()}/${selectedObject.name.toLowerCase()}`,
        timestamp: new Date().toISOString(),
        status: 'success',
        config: selectedQuickTemplate ? quickTemplates.find(t => t.id === selectedQuickTemplate)?.config : apiConfig
      };

      setGeneratedAPIs(prev => [newAPI, ...prev]);
      setApiStats(prev => ({
        ...prev,
        total: prev.total + 1,
        active: prev.active + 1
      }));

      // Add to recent activity
      const activity = {
        id: Date.now(),
        action: 'API Generated',
        description: `${selectedSchema}.${selectedObject.name}`,
        time: 'Just now',
        status: 'success',
        icon: Rocket
      };
      setRecentActivity(prev => [activity, ...prev]);

      setIsGeneratingAPI(false);
      setSelectedQuickTemplate(null);
      
      // Show success notification
      alert(`✅ API successfully generated!\n\nEndpoint: ${newAPI.endpoint}\nMethod: GET, POST, PUT, DELETE\nSecurity: ${newAPI.config?.securityLevel || 'medium'}\n\nThe API is now ready for use.`);
    }, 1500);
  };

  const handleQuickTemplateSelect = (templateId) => {
    setSelectedQuickTemplate(templateId);
    const template = quickTemplates.find(t => t.id === templateId);
    if (template) {
      setApiConfig(prev => ({
        ...prev,
        ...template.config
      }));
      alert(`✅ "${template.name}" template selected!\n\nFeatures enabled:\n• ${template.config.methods?.join(', ')} methods\n• Security: ${template.config.securityLevel}\n• ${template.config.includeSwagger ? 'Swagger docs included' : ''}\n• ${template.config.includeAudit ? 'Audit logging enabled' : ''}`);
    }
  };

  const connectOracleInstance = () => {
    const instanceName = prompt('Enter Oracle instance name:');
    if (instanceName) {
      setApiStats(prev => ({
        ...prev,
        oracleInstances: prev.oracleInstances + 1
      }));
      
      const activity = {
        id: Date.now(),
        action: 'Oracle Connection',
        description: `${instanceName} connected`,
        time: 'Just now',
        status: 'success',
        icon: Database
      };
      setRecentActivity(prev => [activity, ...prev]);
      
      alert(`✅ Oracle instance "${instanceName}" connected successfully!`);
    }
  };

  const clearAllActivities = () => {
    if (window.confirm('Are you sure you want to clear all recent activities?')) {
      setRecentActivity([]);
    }
  };

  const toggleSchemaExpansion = (schemaName) => {
    setExpandedSchemas(prev =>
      prev.includes(schemaName)
        ? prev.filter(s => s !== schemaName)
        : [...prev, schemaName]
    );
  };

  const filteredSchemas = oracleSchemas.filter(schema =>
    schema.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    schema.description.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const StatsCard = ({ title, value, change, trend, icon, color }) => (
    <div 
      className="p-4 rounded-xl border hover:scale-[1.02] transition-transform cursor-pointer hover:shadow-lg" 
      style={{ 
        backgroundColor: colors.card,
        borderColor: colors.border
      }}
      onClick={() => alert(`${title}: ${value}\nTrend: ${change}`)}
    >
      <div className="flex items-center justify-between mb-3">
        <div className="p-2 rounded-lg" style={{ backgroundColor: color + '20' }}>
          <div style={{ color }}>{icon}</div>
        </div>
        <span className={`text-sm font-medium flex items-center gap-1 ${trend === 'up' ? 'text-green-500' : 'text-red-500'}`}>
          {trend === 'up' ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
          {change}
        </span>
      </div>
      <h3 className="text-2xl font-bold mb-1" style={{ color: colors.text }}>{value}</h3>
      <p className="text-sm" style={{ color: colors.textSecondary }}>{title}</p>
    </div>
  );

  const PerformanceChart = () => {
    const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const data = [89, 92, 85, 88, 91, 87, 84];
    
    const maxValue = Math.max(...data);
    
    return (
      <div className="h-64">
        <div className="flex items-end h-48 gap-2">
          {days.map((label, i) => (
            <div key={i} className="flex-1 flex flex-col items-center">
              <div className="w-full flex justify-center">
                <div 
                  className="w-3/4 rounded-t-lg hover:w-full transition-all duration-300 cursor-pointer"
                  style={{ 
                    backgroundColor: colors.primary,
                    height: `${(data[i] / maxValue) * 100}%`,
                    minHeight: '10px'
                  }}
                  onClick={() => alert(`${label}: ${data[i]}% performance`)}
                />
              </div>
              <span className="text-xs mt-3" style={{ color: colors.textSecondary }}>
                {label}
              </span>
            </div>
          ))}
        </div>
        <div className="flex justify-between text-xs mt-4" style={{ color: colors.textSecondary }}>
          <span>Low</span>
          <span>Performance Score</span>
          <span>High</span>
        </div>
      </div>
    );
  };

  return (
    <div style={{ backgroundColor: colors.bg, color: colors.text, minHeight: '100vh' }}>

      {/* Main Layout Container */}
      <div className="flex">
        {/* Main Content - Centered */}
        <div className="flex-1 min-w-0">
          <div className="p-6">
            {/* Welcome Banner */}
            <div className="mb-6 p-6 rounded-xl border" style={{ 
              backgroundColor: colors.card,
              borderColor: colors.border
            }}>
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
                <div>
                  <div className="flex items-center gap-2 mb-2">
                    <Database size={20} style={{ color: colors.oracle }} />
                    <h1 className="text-2xl font-bold">Oracle Database API Platform</h1>
                  </div>
                  <p className="text-sm mb-4" style={{ color: colors.textSecondary }}>
                    Enterprise-grade API generation from Oracle databases. Monitor and manage your PL/SQL APIs with real-time insights.
                  </p>
                  <div className="flex flex-wrap items-center gap-4">
                    <div className="flex items-center gap-2 text-sm">
                      <div className="w-2 h-2 rounded-full animate-pulse" style={{ backgroundColor: colors.success }} />
                      <span style={{ color: colors.textSecondary }}>{apiStats.oracleInstances} Oracle instances connected</span>
                    </div>
                    <div className="w-px h-4 hidden sm:block" style={{ backgroundColor: colors.border }} />
                    <div className="flex items-center gap-2 text-sm">
                      <ShieldCheck size={14} style={{ color: colors.success }} />
                      <span style={{ color: colors.textSecondary }}>TDE encryption active</span>
                    </div>
                    <div className="w-px h-4 hidden sm:block" style={{ backgroundColor: colors.border }} />
                    <div className="flex items-center gap-2 text-sm">
                      <Zap size={14} style={{ color: colors.warning }} />
                      <span style={{ color: colors.textSecondary }}>{apiStats.active} APIs running</span>
                    </div>
                  </div>
                </div>
                <button 
                  className="px-6 py-3 rounded-lg text-sm font-medium flex items-center gap-2 hover:opacity-90 transition-opacity shrink-0"
                  style={{ backgroundColor: colors.primary, color: 'white' }}
                  onClick={connectOracleInstance}
                >
                  <Plus size={16} />
                  Connect Oracle Instance
                </button>
              </div>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
              {mainStats.map(stat => (
                <StatsCard key={stat.id} {...stat} />
              ))}
            </div>

            {/* Main Content Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
              {/* Performance Chart */}
              <div className="p-5 rounded-xl border" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
                <div className="flex items-center justify-between mb-6">
                  <div>
                    <h3 className="text-lg font-semibold mb-1">Performance Overview</h3>
                    <p className="text-sm" style={{ color: colors.textSecondary }}>Response time over last 7 days</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <button 
                      className="p-2 rounded-lg hover:opacity-80 transition-opacity"
                      style={{ backgroundColor: colors.cardLight }}
                      onClick={() => alert('Filter performance data')}
                    >
                      <Filter size={16} style={{ color: colors.textSecondary }} />
                    </button>
                    <button 
                      className="p-2 rounded-lg hover:opacity-80 transition-opacity"
                      style={{ backgroundColor: colors.cardLight }}
                      onClick={() => alert('Download performance report')}
                    >
                      <Download size={16} style={{ color: colors.textSecondary }} />
                    </button>
                    <button 
                      className="p-2 rounded-lg hover:opacity-80 transition-opacity"
                      style={{ backgroundColor: colors.cardLight }}
                      onClick={() => alert('Refresh performance data')}
                    >
                      <RefreshCw size={16} style={{ color: colors.textSecondary }} />
                    </button>
                  </div>
                </div>
                
                <PerformanceChart />
              </div>

              {/* Top APIs */}
              <div className="p-5 rounded-xl border" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
                <div className="flex items-center justify-between mb-6">
                  <div>
                    <h3 className="text-lg font-semibold mb-1">Top Performing APIs</h3>
                    <p className="text-sm" style={{ color: colors.textSecondary }}>Most frequently called Oracle APIs</p>
                  </div>
                  <button 
                    className="text-sm flex items-center gap-1 hover:opacity-80 transition-opacity"
                    style={{ color: colors.primary }}
                    onClick={() => alert('View all APIs')}
                  >
                    View All <ArrowUpRight size={14} />
                  </button>
                </div>
                
                <div className="space-y-3">
                  {topAPIs.map(api => (
                    <div 
                      key={api.id} 
                      className="flex items-center justify-between p-3 rounded-lg hover:opacity-90 transition-opacity cursor-pointer"
                      style={{ backgroundColor: colors.cardLight }}
                      onClick={() => alert(`API Details:\n\n${api.name}\nMethod: ${api.method}\nEndpoint: ${api.endpoint}\nTotal Calls: ${api.calls.toLocaleString()}\nSuccess Rate: ${api.success}%\nAvg Response: ${api.responseTime}`)}
                    >
                      <div className="flex items-center gap-3">
                        <div className={`px-2.5 py-1 rounded text-xs font-bold ${
                          api.method === 'GET' ? 'bg-green-500/10 text-green-500' :
                          api.method === 'POST' ? 'bg-orange-500/10 text-orange-500' :
                          'bg-blue-500/10 text-blue-500'
                        }`}>
                          {api.method}
                        </div>
                        <div>
                          <p className="text-sm font-medium">{api.name}</p>
                          <p className="text-xs" style={{ color: colors.textSecondary }}>{api.endpoint}</p>
                        </div>
                      </div>
                      <div className="flex items-center gap-4">
                        <div className="text-right">
                          <p className="text-sm font-medium">{api.calls.toLocaleString()}</p>
                          <p className="text-xs" style={{ color: colors.textSecondary }}>calls</p>
                        </div>
                        <div className="text-right">
                          <p className="text-sm font-medium" style={{ color: colors.success }}>{api.success}%</p>
                          <p className="text-xs" style={{ color: colors.textSecondary }}>success</p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Recent Activity */}
            <div className="p-5 rounded-xl border" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold">Recent Activity</h3>
                <button 
                  className="text-sm hover:opacity-80 transition-opacity" 
                  style={{ color: colors.primary }}
                  onClick={clearAllActivities}
                >
                  Clear All
                </button>
              </div>
              
              {recentActivity.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                  {recentActivity.map((activity) => {
                    const Icon = activity.icon;
                    const statusColor = activity.status === 'success' ? colors.success : colors.warning;
                    return (
                      <div 
                        key={activity.id} 
                        className="p-4 rounded-lg cursor-pointer hover:opacity-90 transition-opacity"
                        style={{ backgroundColor: colors.cardLight }}
                        onClick={() => alert(`Activity Details:\n\nAction: ${activity.action}\nDescription: ${activity.description}\nTime: ${activity.time}\nStatus: ${activity.status}`)}
                      >
                        <div className="flex items-start gap-3">
                          <div className="p-2 rounded-lg" style={{ backgroundColor: statusColor + '15' }}>
                            <Icon size={16} style={{ color: statusColor }} />
                          </div>
                          <div className="flex-1">
                            <p className="text-sm font-medium">{activity.action}</p>
                            <p className="text-xs mt-1" style={{ color: colors.textSecondary }}>{activity.description}</p>
                            <p className="text-xs mt-2" style={{ color: colors.textTertiary }}>{activity.time}</p>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className="text-center py-8">
                  <p style={{ color: colors.textSecondary }}>No recent activity</p>
                  <p className="text-sm mt-1" style={{ color: colors.textTertiary }}>Generate an API to see activity here</p>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right Panel - Combined Schema Browser & API Generator */}
        {showRightPanel && (
          <div className="w-96 flex-shrink-0 border-l hidden lg:block" style={{ 
            borderColor: colors.border, 
            backgroundColor: colors.card
          }}>
            <div className="h-full overflow-y-auto">
              {/* Panel Header */}
              <div className="p-6 border-b sticky top-0 z-10" style={{ 
                backgroundColor: colors.card,
                borderColor: colors.border 
              }}>
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-lg" style={{ backgroundColor: colors.primary + '20' }}>
                      <Database size={20} style={{ color: colors.primary }} />
                    </div>
                    <div>
                      <h3 className="text-lg font-semibold">API Workspace</h3>
                      <p className="text-sm" style={{ color: colors.textSecondary }}>Generate & Browse Objects</p>
                    </div>
                  </div>
                  <button 
                    onClick={() => setShowRightPanel(false)}
                    className="p-2 rounded-lg hover:opacity-80 transition-opacity"
                    style={{ backgroundColor: colors.cardLight }}
                  >
                    <ChevronRight size={16} style={{ color: colors.textSecondary }} />
                  </button>
                </div>

                {/* Tabs */}
                <div className="flex" style={{ borderBottom: `1px solid ${colors.border}` }}>
                  <button
                    className={`px-4 py-3 text-sm font-medium relative ${activeTab === 'generate' ? 'text-blue-500' : ''}`}
                    style={{ color: activeTab === 'generate' ? colors.primary : colors.textSecondary }}
                    onClick={() => setActiveTab('generate')}
                  >
                    Generate API
                    {activeTab === 'generate' && (
                      <div className="absolute bottom-0 left-0 right-0 h-0.5" style={{ backgroundColor: colors.primary }} />
                    )}
                  </button>
                  <button
                    className={`px-4 py-3 text-sm font-medium relative ${activeTab === 'browse' ? 'text-blue-500' : ''}`}
                    style={{ color: activeTab === 'browse' ? colors.primary : colors.textSecondary }}
                    onClick={() => setActiveTab('browse')}
                  >
                    Schema Browser
                    {activeTab === 'browse' && (
                      <div className="absolute bottom-0 left-0 right-0 h-0.5" style={{ backgroundColor: colors.primary }} />
                    )}
                  </button>
                </div>
              </div>

              {/* Panel Content */}
              <div className="p-6">
                {/* Tab Content */}
                {activeTab === 'generate' ? (
                  <>
                    {/* Selected Object */}
                    <div className="mb-6 p-4 rounded-lg border" style={{ backgroundColor: colors.cardLight, borderColor: colors.border }}>
                      <div className="text-sm font-medium mb-2">Selected Object</div>
                      {selectedObject ? (
                        <div className="flex items-center gap-3">
                          <div className="p-2 rounded" style={{ backgroundColor: colors.primary + '20' }}>
                            <Table size={16} style={{ color: colors.primary }} />
                          </div>
                          <div className="flex-1">
                            <div className="font-medium">{selectedObject.name}</div>
                            <div className="text-xs" style={{ color: colors.textSecondary }}>
                              {selectedSchema} • {selectedObject.type}
                              {selectedObject.columns && ` • ${selectedObject.columns} columns`}
                              {selectedObject.rows && ` • ${selectedObject.rows.toLocaleString()} rows`}
                            </div>
                          </div>
                        </div>
                      ) : (
                        <div className="text-center py-3">
                          <p className="text-sm" style={{ color: colors.textTertiary }}>No object selected</p>
                          <p className="text-xs mt-1" style={{ color: colors.textTertiary }}>Select from Schema Browser tab</p>
                        </div>
                      )}
                    </div>

                    {/* API Configuration */}
                    <div className="mb-6">
                      <div className="text-sm font-medium mb-3">API Configuration</div>
                      <div className="space-y-3">
                        <div>
                          <label className="text-xs block mb-1" style={{ color: colors.textSecondary }}>Endpoint Path</label>
                          <input
                            type="text"
                            value={apiConfig.endpointPath}
                            onChange={(e) => setApiConfig(prev => ({ ...prev, endpointPath: e.target.value }))}
                            placeholder={`/api/v1/${selectedSchema.toLowerCase()}/`}
                            className="w-full px-3 py-2 rounded text-sm"
                            style={{ backgroundColor: colors.cardLighter, border: `1px solid ${colors.border}` }}
                          />
                        </div>
                        
                        <div>
                          <label className="text-xs block mb-1" style={{ color: colors.textSecondary }}>Security Level</label>
                          <select
                            value={apiConfig.securityLevel}
                            onChange={(e) => setApiConfig(prev => ({ ...prev, securityLevel: e.target.value }))}
                            className="w-full px-3 py-2 rounded text-sm"
                            style={{ backgroundColor: colors.cardLighter, border: `1px solid ${colors.border}` }}
                          >
                            <option value="low">Low (Basic Auth)</option>
                            <option value="medium">Medium (JWT + Basic Auth)</option>
                            <option value="high">High (JWT + OAuth2 + Rate Limiting)</option>
                          </select>
                        </div>
                        
                        <div className="flex items-center gap-4">
                          <label className="flex items-center gap-2 text-sm cursor-pointer">
                            <input
                              type="checkbox"
                              checked={apiConfig.includeSwagger}
                              onChange={(e) => setApiConfig(prev => ({ ...prev, includeSwagger: e.target.checked }))}
                              className="rounded"
                            />
                            <span style={{ color: colors.textSecondary }}>Swagger Docs</span>
                          </label>
                          
                          <label className="flex items-center gap-2 text-sm cursor-pointer">
                            <input
                              type="checkbox"
                              checked={apiConfig.includeTests}
                              onChange={(e) => setApiConfig(prev => ({ ...prev, includeTests: e.target.checked }))}
                              className="rounded"
                            />
                            <span style={{ color: colors.textSecondary }}>Unit Tests</span>
                          </label>
                        </div>
                      </div>
                    </div>

                    {/* Quick Templates */}
                    <div className="mb-6">
                      <div className="text-sm font-medium mb-3">Quick Templates</div>
                      <div className="grid grid-cols-2 gap-2">
                        {quickTemplates.map((template) => (
                          <button
                            key={template.id}
                            className={`p-3 rounded-lg text-left hover:opacity-90 transition-all ${selectedQuickTemplate === template.id ? 'ring-2' : ''}`}
                            style={{ 
                              backgroundColor: colors.cardLight,
                              border: `1px solid ${selectedQuickTemplate === template.id ? template.color : colors.border}`
                            }}
                            onClick={() => handleQuickTemplateSelect(template.id)}
                          >
                            <div className="flex items-center gap-2 mb-2">
                              <div style={{ color: template.color }}>{template.icon}</div>
                              <div className="text-sm font-medium">{template.name}</div>
                            </div>
                            <div className="text-xs" style={{ color: colors.textSecondary }}>{template.desc}</div>
                          </button>
                        ))}
                      </div>
                    </div>

                    {/* Generate Button */}
                    <button 
                      className={`w-full py-3 rounded-lg font-medium flex items-center justify-center gap-2 hover:opacity-90 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed mb-6 ${isGeneratingAPI ? 'animate-pulse' : ''}`}
                      style={{ backgroundColor: colors.primary, color: 'white' }}
                      onClick={generateAPI}
                      disabled={!selectedObject || isGeneratingAPI}
                    >
                      {isGeneratingAPI ? (
                        <>
                          <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                          Generating...
                        </>
                      ) : (
                        <>
                          <Wand2 size={16} />
                          {selectedObject ? `Generate ${selectedObject.name} API` : 'Select Object First'}
                        </>
                      )}
                    </button>

                    {/* Recent Generations */}
                    {generatedAPIs.length > 0 && (
                      <div>
                        <div className="text-sm font-medium mb-2">Recent Generations</div>
                        <div className="space-y-2">
                          {generatedAPIs.slice(0, 3).map((api) => (
                            <div 
                              key={api.id} 
                              className="flex items-center justify-between text-sm p-2 rounded cursor-pointer hover:opacity-80 transition-opacity"
                              style={{ backgroundColor: colors.cardLight }}
                              onClick={() => alert(`API Details:\n\nName: ${api.name}\nEndpoint: ${api.endpoint}\nSchema: ${api.schema}\nType: ${api.type}\nGenerated: ${new Date(api.timestamp).toLocaleString()}`)}
                            >
                              <div className="flex items-center gap-2">
                                <div className="w-2 h-2 rounded-full" style={{ backgroundColor: colors.success }} />
                                <span className="font-mono text-xs">{api.object}</span>
                              </div>
                              <span className="text-xs" style={{ color: colors.textSecondary }}>
                                {new Date(api.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                              </span>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </>
                ) : (
                  <>
                    {/* Search */}
                    <div className="mb-6">
                      <div className="relative">
                        <input
                          type="text"
                          placeholder="Search schemas, tables, procedures..."
                          value={searchQuery}
                          onChange={(e) => setSearchQuery(e.target.value)}
                          className="w-full pl-10 pr-4 py-2 rounded-lg text-sm"
                          style={{ backgroundColor: colors.cardLight, border: `1px solid ${colors.border}` }}
                        />
                        <Search size={16} className="absolute left-3 top-1/2 transform -translate-y-1/2" style={{ color: colors.textSecondary }} />
                      </div>
                    </div>

                    {/* Oracle Schemas */}
                    <div className="mb-6">
                      <div className="text-sm font-medium mb-3">Oracle Schemas</div>
                      <div className="space-y-2">
                        {filteredSchemas.map(schema => (
                          <div key={schema.name}>
                            <div
                              className={`p-3 rounded-lg cursor-pointer transition-colors flex items-center justify-between ${selectedSchema === schema.name ? 'border-2' : 'border'}`}
                              style={{ 
                                backgroundColor: selectedSchema === schema.name ? colors.selected : colors.cardLight,
                                borderColor: selectedSchema === schema.name ? colors.primary : colors.border
                              }}
                              onClick={() => {
                                setSelectedSchema(schema.name);
                                if (!expandedSchemas.includes(schema.name)) {
                                  setExpandedSchemas(prev => [...prev, schema.name]);
                                }
                                setActiveTab('generate');
                              }}
                            >
                              <div className="flex items-center gap-2">
                                <Database size={14} style={{ color: colors.oracle }} />
                                <div>
                                  <div className="font-medium">{schema.name}</div>
                                  <div className="text-xs" style={{ color: colors.textSecondary }}>{schema.description}</div>
                                </div>
                              </div>
                              <button 
                                onClick={(e) => {
                                  e.stopPropagation();
                                  toggleSchemaExpansion(schema.name);
                                }}
                                className="p-1 hover:opacity-80 transition-opacity"
                              >
                                <ChevronDown 
                                  size={16} 
                                  style={{ 
                                    color: colors.textSecondary,
                                    transform: expandedSchemas.includes(schema.name) ? 'rotate(180deg)' : 'none',
                                    transition: 'transform 0.2s'
                                  }} 
                                />
                              </button>
                            </div>
                            
                            {expandedSchemas.includes(schema.name) && (
                              <div className="ml-4 mt-2 border-l pl-3" style={{ borderColor: colors.borderLight }}>
                                <div className="space-y-1">
                                  {Object.entries(schema.objects).map(([type, objects]) => (
                                    <div key={type}>
                                      <div className="text-xs font-medium mt-2 mb-1 uppercase tracking-wider" style={{ color: colors.textTertiary }}>
                                        {type} ({objects.length})
                                      </div>
                                      {objects.map(obj => (
                                        <div
                                          key={obj.name}
                                          className={`p-2 rounded cursor-pointer transition-colors flex items-center gap-2 text-sm ${selectedObject?.name === obj.name ? 'border' : ''}`}
                                          style={{ 
                                            backgroundColor: selectedObject?.name === obj.name ? colors.selected : 'transparent',
                                            borderColor: selectedObject?.name === obj.name ? colors.primary : 'transparent'
                                          }}
                                          onClick={() => {
                                            setSelectedObject(obj);
                                            setActiveTab('generate');
                                          }}
                                        >
                                          {obj.type === 'TABLE' ? <Table size={14} style={{ color: colors.success }} /> :
                                           obj.type === 'PROCEDURE' ? <Procedure size={14} style={{ color: colors.primary }} /> :
                                           obj.type === 'FUNCTION' ? <Function size={14} style={{ color: colors.secondary }} /> :
                                           <Package size={14} style={{ color: colors.warning }} />}
                                          <div>
                                            <div className="font-medium">{obj.name}</div>
                                            <div className="text-xs" style={{ color: colors.textSecondary }}>
                                              {obj.type}
                                              {obj.columns && ` • ${obj.columns} cols`}
                                              {obj.parameters && ` • ${obj.parameters} params`}
                                              {obj.rows && ` • ${obj.rows.toLocaleString()} rows`}
                                            </div>
                                          </div>
                                        </div>
                                      ))}
                                    </div>
                                  ))}
                                </div>
                              </div>
                            )}
                          </div>
                        ))}
                      </div>
                    </div>

                    {/* Selected Object Details */}
                    {selectedObject && (
                      <div className="mt-6 pt-6 border-t" style={{ borderColor: colors.border }}>
                        <div className="text-sm font-medium mb-3">Selected Object Details</div>
                        <div className="p-3 rounded-lg" style={{ backgroundColor: colors.cardLight }}>
                          <div className="flex items-center gap-2 mb-2">
                            {selectedObject.type === 'TABLE' ? <Table size={16} style={{ color: colors.success }} /> :
                             selectedObject.type === 'PROCEDURE' ? <Procedure size={16} style={{ color: colors.primary }} /> :
                             selectedObject.type === 'FUNCTION' ? <Function size={16} style={{ color: colors.secondary }} /> :
                             <Package size={16} style={{ color: colors.warning }} />}
                            <div>
                              <div className="font-medium">{selectedObject.name}</div>
                              <div className="text-xs" style={{ color: colors.textSecondary }}>{selectedObject.type}</div>
                            </div>
                          </div>
                          
                          <div className="space-y-2 text-sm mt-3">
                            <div className="flex justify-between">
                              <span style={{ color: colors.textSecondary }}>Schema:</span>
                              <span>{selectedSchema}</span>
                            </div>
                            
                            {selectedObject.columns && (
                              <div className="flex justify-between">
                                <span style={{ color: colors.textSecondary }}>Columns:</span>
                                <span>{selectedObject.columns}</span>
                              </div>
                            )}
                            
                            {selectedObject.rows && (
                              <div className="flex justify-between">
                                <span style={{ color: colors.textSecondary }}>Rows:</span>
                                <span>{selectedObject.rows.toLocaleString()}</span>
                              </div>
                            )}
                            
                            {selectedObject.parameters && (
                              <div className="flex justify-between">
                                <span style={{ color: colors.textSecondary }}>Parameters:</span>
                                <span>{selectedObject.parameters}</span>
                              </div>
                            )}
                            
                            <button 
                              className="w-full mt-3 py-2 text-sm rounded-lg hover:opacity-90 transition-opacity flex items-center justify-center gap-2"
                              style={{ backgroundColor: colors.primary, color: 'white' }}
                              onClick={() => {
                                setActiveTab('generate');
                              }}
                            >
                              <Wand2 size={14} />
                              Generate API from this object
                            </button>
                          </div>
                        </div>
                      </div>
                    )}
                  </>
                )}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Toggle button for hidden right panel */}
      {!showRightPanel && (
        <button 
          onClick={() => setShowRightPanel(true)}
          className="fixed right-4 top-1/2 transform -translate-y-1/2 z-20 p-3 rounded-lg shadow-lg hover:opacity-90 transition-opacity hidden lg:block"
          style={{ backgroundColor: colors.primary, color: 'white' }}
          title="Show Schema Browser & API Generator"
        >
          <ChevronLeft size={20} />
        </button>
      )}

      {/* Mobile Sidebar */}
      {isSidebarOpen && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div 
            className="absolute inset-0 bg-black/50"
            onClick={() => setIsSidebarOpen(false)}
          />
          <div 
            className="absolute left-0 top-0 bottom-0 w-80 transform transition-transform"
            style={{ backgroundColor: colors.card }}
          >
            <div className="p-6 h-full overflow-y-auto">
              <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                  <Database size={24} style={{ color: colors.oracle }} />
                  <span className="text-lg font-bold">Menu</span>
                </div>
                <button 
                  onClick={() => setIsSidebarOpen(false)}
                  className="p-2 rounded-lg hover:opacity-80 transition-opacity"
                  style={{ backgroundColor: colors.cardLight }}
                >
                  <X size={20} />
                </button>
              </div>
              
              <div className="space-y-4">
                <button className="w-full flex items-center gap-3 p-3 rounded-lg hover:opacity-90 transition-opacity"
                  style={{ backgroundColor: colors.cardLight }}>
                  <Home size={20} />
                  <span>Dashboard</span>
                </button>
                
                <button className="w-full flex items-center gap-3 p-3 rounded-lg hover:opacity-90 transition-opacity"
                  style={{ backgroundColor: colors.cardLight }}>
                  <Database size={20} />
                  <span>Schemas</span>
                </button>
                
                <button className="w-full flex items-center gap-3 p-3 rounded-lg hover:opacity-90 transition-opacity"
                  style={{ backgroundColor: colors.cardLight }}>
                  <Code size={20} />
                  <span>APIs</span>
                </button>
                
                <button className="w-full flex items-center gap-3 p-3 rounded-lg hover:opacity-90 transition-opacity"
                  style={{ backgroundColor: colors.cardLight }}
                  onClick={() => {
                    setIsSidebarOpen(false);
                    setShowRightPanel(true);
                  }}>
                  <Rocket size={20} />
                  <span>Generate API</span>
                </button>
                
                <button className="w-full flex items-center gap-3 p-3 rounded-lg hover:opacity-90 transition-opacity"
                  style={{ backgroundColor: colors.cardLight }}
                  onClick={() => {
                    setIsSidebarOpen(false);
                    setShowRightPanel(true);
                  }}>
                  <FolderTree size={20} />
                  <span>Schema Browser</span>
                </button>
                
                <button className="w-full flex items-center gap-3 p-3 rounded-lg hover:opacity-90 transition-opacity"
                  style={{ backgroundColor: colors.cardLight }}>
                  <Settings size={20} />
                  <span>Settings</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;