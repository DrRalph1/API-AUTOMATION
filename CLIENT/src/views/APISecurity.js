import React, { useState, useEffect } from 'react';
import {
  Shield, ShieldCheck, ShieldAlert, ShieldOff, ShieldPlus, ShieldMinus, ShieldEllipsis,
  Lock, LockOpen, Key, KeyRound, Fingerprint, Scan, QrCode, ScanFace, Eye, EyeOff,
  Users, UserCheck, UserCog, UserX, UserMinus, UserPlus,
  Network, Server, ServerCog, ServerCrash, ServerOff,
  Globe, GlobeLock, Earth, EarthLock,
  Activity, Zap, ZapOff, Battery, BatteryCharging,
  Filter, FilterX, Funnel, FunnelX,
  Cpu, HardDrive, MemoryStick, BarChart3, PieChart,
  Clock, Calendar, CalendarDays, Timer, TimerReset,
  AlertCircle, AlertTriangle, AlertOctagon, CheckCircle, XCircle, Info,
  Settings, Sliders, ToggleLeft, ToggleRight, Power, PowerOff,
  Download, Upload, RefreshCw, Plus, Minus, X, Check, Copy,
  Search, Filter as FilterIcon, MoreVertical, Edit, Trash2,
  ExternalLink, Link, Link2, Unlink,
  MessageSquare, MessageCircle, Bell, BellRing, BellOff,
  FileText, FileCode, Database, Layers,
  ChevronDown, ChevronRight, ChevronUp, ChevronLeft,
  Home, Cloud, Wifi, Bluetooth, Radio, Satellite,
  TrendingUp, TrendingDown, TrendingUpDown,
  BarChart, LineChart, AreaChart,
  Hash, HashIcon, Type, Code, Terminal,
  Sun, Moon, Settings as SettingsIcon,
  Shield as ShieldIcon,
  ShieldHalf, ShieldQuestion,
  Bell as BellIcon,
  Database as DatabaseIcon,
  Cpu as CpuIcon,
  Network as NetworkIcon,
  Users as UsersIcon,
  Clock as ClockIcon,
  Calendar as CalendarIcon,
  Activity as ActivityIcon,
  Zap as ZapIcon,
  Lock as LockIcon,
  Key as KeyIcon,
  Filter as FilterIcon2,
  Globe as GlobeIcon,
  Server as ServerIcon,
  FileText as FileTextIcon,
  Layers as LayersIcon,
  BarChart as BarChartIcon,
  LineChart as LineChartIcon,
  AreaChart as AreaChartIcon
} from 'lucide-react';

const APISecurity = () => {
  const [theme, setTheme] = useState('dark');
  const [activeTab, setActiveTab] = useState('rate-limits');
  const [searchQuery, setSearchQuery] = useState('');
  const [showAddRuleModal, setShowAddRuleModal] = useState(false);
  const [showSecurityReport, setShowSecurityReport] = useState(false);
  const [showIPWhitelistModal, setShowIPWhitelistModal] = useState(false);
  const [showLoadBalancerModal, setShowLoadBalancerModal] = useState(false);
  
  const isDark = theme === 'dark';

  // Color scheme matching your other components
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
    codeBg: '#1e293b',
    security: {
      high: '#10b981',
      medium: '#f59e0b',
      low: '#ef4444',
      critical: '#dc2626'
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
    borderLight: '#f1f5f9',
    borderDark: '#cbd5e1',
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
    security: {
      high: '#10b981',
      medium: '#f59e0b',
      low: '#ef4444',
      critical: '#dc2626'
    }
  };

  // Sample Rate Limit Rules
  const [rateLimitRules, setRateLimitRules] = useState([
    {
      id: 'rl-1',
      name: 'Public API - Standard',
      description: 'Standard rate limiting for public API endpoints',
      endpoint: '/api/v1/**',
      method: 'ALL',
      limit: 100,
      window: '1m',
      burst: 20,
      action: 'throttle',
      status: 'active',
      createdAt: '2024-01-15T10:30:00Z',
      updatedAt: 'Today, 09:45 AM'
    },
    {
      id: 'rl-2',
      name: 'Authentication Endpoints',
      description: 'Strict rate limiting for authentication',
      endpoint: '/api/v1/auth/**',
      method: 'POST',
      limit: 10,
      window: '1m',
      burst: 5,
      action: 'block',
      status: 'active',
      createdAt: '2024-01-10T14:20:00Z',
      updatedAt: 'Yesterday, 03:30 PM'
    },
    {
      id: 'rl-3',
      name: 'Payment Processing',
      description: 'Higher limits for payment endpoints',
      endpoint: '/api/v1/payments/**',
      method: 'POST',
      limit: 500,
      window: '1m',
      burst: 100,
      action: 'throttle',
      status: 'active',
      createdAt: '2024-01-05T11:15:00Z',
      updatedAt: '2 days ago'
    }
  ]);

  // Sample IP Whitelist
  const [ipWhitelist, setIpWhitelist] = useState([
    {
      id: 'ip-1',
      name: 'Office Network',
      ipRange: '192.168.1.0/24',
      description: 'Corporate office network',
      endpoints: '/api/v1/admin/**',
      status: 'active',
      createdAt: '2024-01-12T09:00:00Z'
    },
    {
      id: 'ip-2',
      name: 'VPN Users',
      ipRange: '10.0.0.0/16',
      description: 'Company VPN range',
      endpoints: '/api/v1/**',
      status: 'active',
      createdAt: '2024-01-08T13:45:00Z'
    },
    {
      id: 'ip-3',
      name: 'Development Team',
      ipRange: '172.16.32.0/20',
      description: 'Development team access',
      endpoints: '/api/v1/dev/**',
      status: 'inactive',
      createdAt: '2024-01-03T16:20:00Z'
    }
  ]);

  // Sample Load Balancer Config
  const [loadBalancers, setLoadBalancers] = useState([
    {
      id: 'lb-1',
      name: 'Primary API Cluster',
      algorithm: 'round_robin',
      healthCheck: '/api/v1/health',
      healthCheckInterval: '30s',
      servers: [
        { id: 'srv-1', name: 'API-Node-1', address: '10.0.1.1:8080', status: 'healthy', connections: 245 },
        { id: 'srv-2', name: 'API-Node-2', address: '10.0.1.2:8080', status: 'healthy', connections: 198 },
        { id: 'srv-3', name: 'API-Node-3', address: '10.0.1.3:8080', status: 'degraded', connections: 156 }
      ],
      status: 'active',
      totalConnections: 599
    },
    {
      id: 'lb-2',
      name: 'Payment Processing',
      algorithm: 'least_connections',
      healthCheck: '/api/v1/payments/health',
      healthCheckInterval: '15s',
      servers: [
        { id: 'srv-4', name: 'Payment-Node-1', address: '10.0.2.1:8081', status: 'healthy', connections: 89 },
        { id: 'srv-5', name: 'Payment-Node-2', address: '10.0.2.2:8081', status: 'healthy', connections: 76 }
      ],
      status: 'active',
      totalConnections: 165
    }
  ]);

  // Sample Security Events
  const [securityEvents, setSecurityEvents] = useState([
    {
      id: 'evt-1',
      type: 'rate_limit_exceeded',
      severity: 'medium',
      sourceIp: '203.0.113.25',
      endpoint: '/api/v1/auth/login',
      method: 'POST',
      message: 'Rate limit exceeded - 15 requests in 1 minute',
      timestamp: '2024-01-15T14:32:10Z'
    },
    {
      id: 'evt-2',
      type: 'ip_blocked',
      severity: 'high',
      sourceIp: '198.51.100.42',
      endpoint: '/api/v1/admin/users',
      method: 'GET',
      message: 'IP blocked - Not in whitelist',
      timestamp: '2024-01-15T13:45:22Z'
    },
    {
      id: 'evt-3',
      type: 'suspicious_activity',
      severity: 'critical',
      sourceIp: '192.0.2.189',
      endpoint: '/api/v1/payments/process',
      method: 'POST',
      message: 'Multiple failed payment attempts',
      timestamp: '2024-01-15T11:20:15Z'
    },
    {
      id: 'evt-4',
      type: 'ddos_protection',
      severity: 'high',
      sourceIp: '203.0.113.0/24',
      endpoint: '/api/v1/**',
      method: 'ALL',
      message: 'DDoS protection activated - 5000 requests/sec',
      timestamp: '2024-01-14T09:15:30Z'
    }
  ]);

  // Sample API Security Summary
  const [securitySummary, setSecuritySummary] = useState({
    totalEndpoints: 45,
    securedEndpoints: 42,
    vulnerableEndpoints: 3,
    blockedRequests: 1245,
    throttledRequests: 8923,
    avgResponseTime: '42ms',
    securityScore: 92,
    lastScan: '2024-01-15T10:00:00Z'
  });

  // Render Rate Limits Tab
  const renderRateLimitsTab = () => (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold" style={{ color: colors.text }}>Rate Limiting Rules</h2>
          <p className="text-sm" style={{ color: colors.textSecondary }}>
            Configure request rate limits for API endpoints
          </p>
        </div>
        <button 
          onClick={() => setShowAddRuleModal(true)}
          className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 hover-lift"
          style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
          <Plus size={14} />
          Add Rule
        </button>
      </div>

      {/* Rules Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {rateLimitRules.map(rule => (
          <div key={rule.id} className="border rounded-xl p-4 hover-lift" style={{ 
            borderColor: colors.border,
            backgroundColor: colors.card
          }}>
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-2">
                <div className={`w-2 h-2 rounded-full ${
                  rule.status === 'active' ? 'bg-green-500' : 'bg-red-500'
                }`} />
                <h3 className="text-sm font-semibold" style={{ color: colors.text }}>{rule.name}</h3>
              </div>
              <div className="flex gap-1">
                <button className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}>
                  <Edit size={12} style={{ color: colors.textSecondary }} />
                </button>
                <button className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}>
                  <Trash2 size={12} style={{ color: colors.error }} />
                </button>
              </div>
            </div>
            
            <div className="space-y-3">
              <div>
                <div className="text-xs" style={{ color: colors.textSecondary }}>Endpoint</div>
                <div className="text-sm font-mono" style={{ color: colors.text }}>{rule.endpoint}</div>
              </div>
              
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Limit</div>
                  <div className="text-sm font-semibold" style={{ color: colors.text }}>
                    {rule.limit} req/{rule.window}
                  </div>
                </div>
                <div>
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Burst</div>
                  <div className="text-sm font-semibold" style={{ color: colors.text }}>
                    {rule.burst} req
                  </div>
                </div>
              </div>
              
              <div className="flex items-center justify-between text-xs">
                <span style={{ color: colors.textSecondary }}>Action:</span>
                <span className={`px-2 py-1 rounded ${
                  rule.action === 'block' ? 'bg-red-500/10 text-red-500' : 'bg-blue-500/10 text-blue-500'
                }`}>
                  {rule.action}
                </span>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Statistics */}
      <div className="border rounded-xl p-6" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.card
      }}>
        <h3 className="text-lg font-semibold mb-4" style={{ color: colors.text }}>Rate Limit Statistics</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[
            { label: 'Total Blocked Requests', value: '1,245', icon: <ShieldAlert size={20} />, color: colors.error },
            { label: 'Total Throttled Requests', value: '8,923', icon: <Activity size={20} />, color: colors.warning },
            { label: 'Active Rules', value: rateLimitRules.length.toString(), icon: <Filter size={20} />, color: colors.info },
            { label: 'Covered Endpoints', value: '42', icon: <ShieldCheck size={20} />, color: colors.success }
          ].map(stat => (
            <div key={stat.label} className="text-center p-4 rounded-lg hover-lift" style={{ 
              backgroundColor: `${stat.color}10`,
              border: `1px solid ${stat.color}20`
            }}>
              <div className="flex justify-center mb-2">
                <div className="p-2 rounded-full" style={{ backgroundColor: `${stat.color}20` }}>
                  {React.cloneElement(stat.icon, { size: 20, style: { color: stat.color } })}
                </div>
              </div>
              <div className="text-2xl font-bold mb-1" style={{ color: colors.text }}>{stat.value}</div>
              <div className="text-sm" style={{ color: colors.textSecondary }}>{stat.label}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );

  // Render IP Whitelist Tab
  const renderIPWhitelistTab = () => (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold" style={{ color: colors.text }}>IP Whitelisting</h2>
          <p className="text-sm" style={{ color: colors.textSecondary }}>
            Manage IP addresses and ranges allowed to access your APIs
          </p>
        </div>
        <button 
          onClick={() => setShowIPWhitelistModal(true)}
          className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 hover-lift"
          style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
          <Plus size={14} />
          Add IP Range
        </button>
      </div>

      {/* IP Whitelist Table */}
      <div className="border rounded-xl overflow-hidden" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.card
      }}>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead style={{ backgroundColor: colors.tableHeader }}>
              <tr>
                <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Name</th>
                <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>IP Range</th>
                <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Endpoints</th>
                <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Status</th>
                <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {ipWhitelist.map((item, index) => (
                <tr 
                  key={item.id}
                  className="hover:bg-opacity-50 transition-colors"
                  style={{ 
                    backgroundColor: index % 2 === 0 ? colors.tableRow : colors.tableRowHover,
                    borderBottom: `1px solid ${colors.border}`
                  }}
                >
                  <td className="p-4">
                    <div className="flex items-center gap-2">
                      <Globe size={14} style={{ color: colors.textSecondary }} />
                      <span className="text-sm font-medium" style={{ color: colors.text }}>{item.name}</span>
                    </div>
                  </td>
                  <td className="p-4">
                    <div className="text-sm font-mono" style={{ color: colors.text }}>{item.ipRange}</div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>{item.description}</div>
                  </td>
                  <td className="p-4">
                    <div className="text-sm font-mono" style={{ color: colors.text }}>{item.endpoints}</div>
                  </td>
                  <td className="p-4">
                    <div className="flex items-center gap-2">
                      <div className={`w-2 h-2 rounded-full ${
                        item.status === 'active' ? 'bg-green-500' : 'bg-red-500'
                      }`} />
                      <span className={`text-xs px-2 py-1 rounded ${
                        item.status === 'active' ? 'bg-green-500/10 text-green-500' : 'bg-red-500/10 text-red-500'
                      }`}>
                        {item.status}
                      </span>
                    </div>
                  </td>
                  <td className="p-4">
                    <div className="flex items-center gap-2">
                      <button className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                        style={{ backgroundColor: colors.hover }}>
                        <Edit size={12} style={{ color: colors.textSecondary }} />
                      </button>
                      <button className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                        style={{ backgroundColor: colors.hover }}>
                        <Trash2 size={12} style={{ color: colors.error }} />
                      </button>
                      <button className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                        style={{ backgroundColor: colors.hover }}>
                        <Eye size={12} style={{ color: colors.textSecondary }} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* IP Analysis */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="border rounded-xl p-6 hover-lift" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 rounded" style={{ backgroundColor: '#3b82f620' }}>
              <Shield size={20} style={{ color: colors.info }} />
            </div>
            <div>
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>IP Security Status</h3>
              <div className="text-xs" style={{ color: colors.textSecondary }}>Current whitelist coverage</div>
            </div>
          </div>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-sm" style={{ color: colors.textSecondary }}>Total IP Ranges</span>
              <span className="text-sm font-semibold" style={{ color: colors.text }}>{ipWhitelist.length}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm" style={{ color: colors.textSecondary }}>Active Ranges</span>
              <span className="text-sm font-semibold" style={{ color: colors.success }}>
                {ipWhitelist.filter(ip => ip.status === 'active').length}
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm" style={{ color: colors.textSecondary }}>Protected Endpoints</span>
              <span className="text-sm font-semibold" style={{ color: colors.text }}>28</span>
            </div>
          </div>
        </div>

        <div className="border rounded-xl p-6 hover-lift" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 rounded" style={{ backgroundColor: '#10b98120' }}>
              <Activity size={20} style={{ color: colors.success }} />
            </div>
            <div>
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Recent IP Blocks</h3>
              <div className="text-xs" style={{ color: colors.textSecondary }}>Last 24 hours</div>
            </div>
          </div>
          <div className="space-y-2">
            {[
              { ip: '198.51.100.42', count: 15, endpoint: '/api/v1/admin/users' },
              { ip: '203.0.113.78', count: 8, endpoint: '/api/v1/auth/login' },
              { ip: '192.0.2.189', count: 23, endpoint: '/api/v1/payments/**' }
            ].map((block, index) => (
              <div key={index} className="flex items-center justify-between p-2 rounded hover-lift"
                style={{ backgroundColor: colors.hover }}>
                <div>
                  <div className="text-xs font-medium" style={{ color: colors.text }}>{block.ip}</div>
                  <div className="text-xs" style={{ color: colors.textSecondary }}>{block.endpoint}</div>
                </div>
                <span className="text-xs px-2 py-1 rounded-full" style={{ 
                  backgroundColor: colors.error + '20',
                  color: colors.error
                }}>
                  {block.count} blocks
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );

  // Render Load Balancers Tab
  const renderLoadBalancersTab = () => (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold" style={{ color: colors.text }}>Load Balancers</h2>
          <p className="text-sm" style={{ color: colors.textSecondary }}>
            Manage API load balancing and traffic distribution
          </p>
        </div>
        <button 
          onClick={() => setShowLoadBalancerModal(true)}
          className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 hover-lift"
          style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
          <Plus size={14} />
          Add Load Balancer
        </button>
      </div>

      {/* Load Balancers Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {loadBalancers.map(lb => (
          <div key={lb.id} className="border rounded-xl p-6 hover-lift" style={{ 
            borderColor: colors.border,
            backgroundColor: colors.card
          }}>
            <div className="flex items-start justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className="p-2 rounded" style={{ backgroundColor: '#10b98120' }}>
                  <Server size={20} style={{ color: colors.success }} />
                </div>
                <div>
                  <h3 className="text-sm font-semibold" style={{ color: colors.text }}>{lb.name}</h3>
                  <div className="text-xs" style={{ color: colors.textSecondary }}>
                    Algorithm: <span className="font-medium">{lb.algorithm}</span>
                  </div>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <button className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}>
                  <Settings size={12} style={{ color: colors.textSecondary }} />
                </button>
                <button className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}>
                  <MoreVertical size={12} style={{ color: colors.textSecondary }} />
                </button>
              </div>
            </div>

            {/* Health Check */}
            <div className="mb-6 p-3 rounded-lg" style={{ backgroundColor: colors.hover }}>
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-medium" style={{ color: colors.text }}>Health Check</span>
                <span className="text-xs px-2 py-1 rounded" style={{ 
                  backgroundColor: colors.success + '20',
                  color: colors.success
                }}>
                  Active
                </span>
              </div>
              <div className="text-xs" style={{ color: colors.textSecondary }}>
                Endpoint: <span className="font-mono">{lb.healthCheck}</span>
              </div>
              <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
                Interval: {lb.healthCheckInterval}
              </div>
            </div>

            {/* Server List */}
            <div className="space-y-3">
              <div className="text-sm font-medium" style={{ color: colors.text }}>Backend Servers</div>
              {lb.servers.map(server => (
                <div key={server.id} className="flex items-center justify-between p-3 rounded-lg hover-lift"
                  style={{ backgroundColor: colors.hover }}>
                  <div className="flex items-center gap-3">
                    <div className={`w-2 h-2 rounded-full ${
                      server.status === 'healthy' ? 'bg-green-500' :
                      server.status === 'degraded' ? 'bg-yellow-500' : 'bg-red-500'
                    }`} />
                    <div>
                      <div className="text-xs font-medium" style={{ color: colors.text }}>{server.name}</div>
                      <div className="text-xs font-mono" style={{ color: colors.textSecondary }}>{server.address}</div>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="text-xs font-semibold" style={{ color: colors.text }}>{server.connections}</div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>connections</div>
                  </div>
                </div>
              ))}
            </div>

            {/* Stats */}
            <div className="mt-6 pt-4 border-t flex items-center justify-between" style={{ borderColor: colors.border }}>
              <div>
                <div className="text-xs" style={{ color: colors.textSecondary }}>Total Connections</div>
                <div className="text-lg font-bold" style={{ color: colors.text }}>{lb.totalConnections}</div>
              </div>
              <div>
                <div className="text-xs" style={{ color: colors.textSecondary }}>Healthy Servers</div>
                <div className="text-lg font-bold" style={{ color: colors.success }}>
                  {lb.servers.filter(s => s.status === 'healthy').length}/{lb.servers.length}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Load Balancer Stats */}
      <div className="border rounded-xl p-6" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.card
      }}>
        <h3 className="text-lg font-semibold mb-4" style={{ color: colors.text }}>Load Balancing Performance</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[
            { label: 'Total Requests', value: '24,589', change: '+12%', icon: <BarChart3 size={20} /> },
            { label: 'Avg Response Time', value: '42ms', change: '-5%', icon: <Timer size={20} /> },
            { label: 'Error Rate', value: '0.2%', change: '-0.1%', icon: <AlertCircle size={20} /> },
            { label: 'Uptime', value: '99.98%', change: '+0.02%', icon: <Activity size={20} /> }
          ].map(stat => (
            <div key={stat.label} className="text-center p-4 rounded-lg hover-lift" style={{ 
              backgroundColor: colors.hover,
              border: `1px solid ${colors.border}`
            }}>
              <div className="flex justify-center mb-2">
                {React.cloneElement(stat.icon, { size: 20, style: { color: colors.textSecondary } })}
              </div>
              <div className="text-2xl font-bold mb-1" style={{ color: colors.text }}>{stat.value}</div>
              <div className="text-sm mb-1" style={{ color: colors.textSecondary }}>{stat.label}</div>
              <div className={`text-xs ${stat.change.startsWith('+') ? 'text-green-500' : 'text-red-500'}`}>
                {stat.change}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );

  // Render Security Events Tab
  const renderSecurityEventsTab = () => (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold" style={{ color: colors.text }}>Security Events</h2>
          <p className="text-sm" style={{ color: colors.textSecondary }}>
            Monitor and analyze security events in real-time
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            <Filter size={14} />
            Filter
          </button>
          <button className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            <Download size={14} />
            Export
          </button>
        </div>
      </div>

      {/* Security Events Table */}
      <div className="border rounded-xl overflow-hidden" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.card
      }}>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead style={{ backgroundColor: colors.tableHeader }}>
              <tr>
                <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Severity</th>
                <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Type</th>
                <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Source IP</th>
                <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Endpoint</th>
                <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Message</th>
                <th className="text-left p-4 text-sm font-medium" style={{ color: colors.textSecondary }}>Time</th>
              </tr>
            </thead>
            <tbody>
              {securityEvents.map((event, index) => (
                <tr 
                  key={event.id}
                  className="hover:bg-opacity-50 transition-colors cursor-pointer"
                  style={{ 
                    backgroundColor: index % 2 === 0 ? colors.tableRow : colors.tableRowHover,
                                        borderBottom: `1px solid ${colors.border}`
                  }}
                >
                  <td className="p-4">
                    <div className="flex items-center gap-2">
                      <div className={`w-2 h-2 rounded-full ${
                        event.severity === 'critical' ? 'bg-red-500' :
                        event.severity === 'high' ? 'bg-orange-500' :
                        event.severity === 'medium' ? 'bg-yellow-500' : 'bg-blue-500'
                      }`} />
                      <span className={`text-xs px-2 py-1 rounded ${
                        event.severity === 'critical' ? 'bg-red-500/10 text-red-500' :
                        event.severity === 'high' ? 'bg-orange-500/10 text-orange-500' :
                        event.severity === 'medium' ? 'bg-yellow-500/10 text-yellow-500' : 'bg-blue-500/10 text-blue-500'
                      }`}>
                        {event.severity}
                      </span>
                    </div>
                  </td>
                  <td className="p-4">
                    <div className="flex items-center gap-2">
                      {event.type === 'rate_limit_exceeded' && <Activity size={14} style={{ color: colors.textSecondary }} />}
                      {event.type === 'ip_blocked' && <ShieldAlert size={14} style={{ color: colors.textSecondary }} />}
                      {event.type === 'suspicious_activity' && <AlertTriangle size={14} style={{ color: colors.textSecondary }} />}
                      {event.type === 'ddos_protection' && <Zap size={14} style={{ color: colors.textSecondary }} />}
                      <span className="text-sm" style={{ color: colors.text }}>{event.type.replace(/_/g, ' ')}</span>
                    </div>
                  </td>
                  <td className="p-4">
                    <div className="text-sm font-mono" style={{ color: colors.text }}>{event.sourceIp}</div>
                  </td>
                  <td className="p-4">
                    <div className="text-sm font-mono" style={{ color: colors.text }}>{event.endpoint}</div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>{event.method}</div>
                  </td>
                  <td className="p-4">
                    <div className="text-sm" style={{ color: colors.text }}>{event.message}</div>
                  </td>
                  <td className="p-4">
                    <div className="text-xs" style={{ color: colors.textSecondary }}>
                      {new Date(event.timestamp).toLocaleDateString()}
                    </div>
                    <div className="text-xs" style={{ color: colors.textTertiary }}>
                      {new Date(event.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Security Insights */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="border rounded-xl p-6 hover-lift" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 rounded" style={{ backgroundColor: '#ef444420' }}>
              <AlertTriangle size={20} style={{ color: colors.error }} />
            </div>
            <div>
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Threat Level</h3>
              <div className="text-xs" style={{ color: colors.textSecondary }}>Current security status</div>
            </div>
          </div>
          <div className="text-center py-4">
            <div className="text-4xl font-bold mb-2" style={{ color: colors.success }}>Low</div>
            <div className="text-sm" style={{ color: colors.textSecondary }}>
              No critical threats detected
            </div>
          </div>
        </div>

        <div className="border rounded-xl p-6 hover-lift" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 rounded" style={{ backgroundColor: '#3b82f620' }}>
              <BarChart3 size={20} style={{ color: colors.info }} />
            </div>
            <div>
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Event Trends</h3>
              <div className="text-xs" style={{ color: colors.textSecondary }}>Last 7 days</div>
            </div>
          </div>
          <div className="space-y-3">
            {[
              { label: 'Rate Limit Exceeded', count: 124, trend: '+15%', color: colors.warning },
              { label: 'IP Blocks', count: 28, trend: '-5%', color: colors.error },
              { label: 'Suspicious Activity', count: 7, trend: '+3%', color: colors.error }
            ].map((trend, index) => (
              <div key={index} className="flex items-center justify-between">
                <span className="text-sm" style={{ color: colors.textSecondary }}>{trend.label}</span>
                <div className="flex items-center gap-2">
                  <span className="text-sm font-semibold" style={{ color: colors.text }}>{trend.count}</span>
                  <span className={`text-xs ${trend.trend.startsWith('+') ? 'text-green-500' : 'text-red-500'}`}>
                    {trend.trend}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="border rounded-xl p-6 hover-lift" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 rounded" style={{ backgroundColor: '#10b98120' }}>
              <ShieldCheck size={20} style={{ color: colors.success }} />
            </div>
            <div>
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Security Score</h3>
              <div className="text-xs" style={{ color: colors.textSecondary }}>Overall API security</div>
            </div>
          </div>
          <div className="text-center py-4">
            <div className="text-4xl font-bold mb-2" style={{ color: colors.success }}>{securitySummary.securityScore}/100</div>
            <div className="text-sm" style={{ color: colors.textSecondary }}>
              Excellent - Last scan: Today
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  // Render Sidebar
  const renderSidebar = () => (
    <div className="w-64 border-r p-6" style={{ 
      borderColor: colors.border,
      backgroundColor: colors.sidebar
    }}>
      <div className="mb-8">
        <h2 className="text-lg font-semibold mb-4 flex items-center gap-2" style={{ color: colors.text }}>
          <ShieldCheck size={20} />
          API Security
        </h2>
        <div className="space-y-1">
          {[
            { id: 'overview', label: 'Overview', icon: <Activity size={16} /> },
            { id: 'rate-limits', label: 'Rate Limits', icon: <Filter size={16} /> },
            { id: 'ip-whitelist', label: 'IP Whitelist', icon: <Globe size={16} /> },
            { id: 'load-balancers', label: 'Load Balancers', icon: <Server size={16} /> },
            { id: 'security-events', label: 'Security Events', icon: <AlertTriangle size={16} /> },
            { id: 'waf-rules', label: 'WAF Rules', icon: <Shield size={16} /> },
            { id: 'api-keys', label: 'API Keys', icon: <Key size={16} /> },
            { id: 'authentication', label: 'Authentication', icon: <Lock size={16} /> },
            { id: 'monitoring', label: 'Monitoring', icon: <BarChart3 size={16} /> },
            { id: 'reports', label: 'Reports', icon: <FileText size={16} /> }
          ].map(item => (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className={`w-full text-left px-3 py-2 rounded flex items-center gap-3 transition-colors ${
                activeTab === item.id ? 'font-medium' : ''
              } hover-lift`}
              style={{
                backgroundColor: activeTab === item.id ? colors.sidebarActive : 'transparent',
                color: activeTab === item.id ? colors.white : colors.textSecondary
              }}
            >
              {item.icon}
              {item.label}
            </button>
          ))}
        </div>
      </div>

      {/* Security Summary */}
      {/* <div className="border rounded-xl p-4 mb-6" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.card
      }}>
        <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Security Summary</h3>
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs" style={{ color: colors.textSecondary }}>Total Endpoints</span>
            <span className="text-sm font-semibold" style={{ color: colors.text }}>{securitySummary.totalEndpoints}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-xs" style={{ color: colors.textSecondary }}>Secured</span>
            <span className="text-sm font-semibold" style={{ color: colors.success }}>{securitySummary.securedEndpoints}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-xs" style={{ color: colors.textSecondary }}>Vulnerable</span>
            <span className="text-sm font-semibold" style={{ color: colors.error }}>{securitySummary.vulnerableEndpoints}</span>
          </div>
          <div className="pt-3 border-t" style={{ borderColor: colors.border }}>
            <div className="flex items-center justify-between">
              <span className="text-xs" style={{ color: colors.textSecondary }}>Security Score</span>
              <div className="flex items-center gap-2">
                <span className="text-sm font-bold" style={{ color: colors.success }}>{securitySummary.securityScore}%</span>
                <div className="w-16 h-2 rounded-full overflow-hidden" style={{ backgroundColor: colors.border }}>
                  <div 
                    className="h-full rounded-full" 
                    style={{ 
                      width: `${securitySummary.securityScore}%`,
                      backgroundColor: securitySummary.securityScore >= 90 ? colors.success :
                                     securitySummary.securityScore >= 70 ? colors.warning : colors.error
                    }}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div> */}

      {/* Quick Actions */}
      <div>
        <h3 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Quick Actions</h3>
        <div className="space-y-2">
          {/* <button className="w-full text-left px-3 py-2 rounded flex items-center gap-3 text-sm hover-lift"
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            <ShieldPlus size={14} />
            Run Security Scan
          </button> */}
          <button 
            onClick={() => setShowSecurityReport(true)}
            className="w-full text-left px-3 py-2 rounded flex items-center gap-3 text-sm hover-lift"
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            <FileText size={14} />
            Generate Report
          </button>
          <button className="w-full text-left px-3 py-2 rounded flex items-center gap-3 text-sm hover-lift"
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            <Bell size={14} />
            Configure Alerts
          </button>
        </div>
      </div>
    </div>
  );

  // Render Main Content based on active tab
  const renderMainContent = () => {
    switch (activeTab) {
      case 'rate-limits':
        return renderRateLimitsTab();
      case 'ip-whitelist':
        return renderIPWhitelistTab();
      case 'load-balancers':
        return renderLoadBalancersTab();
      case 'security-events':
        return renderSecurityEventsTab();
      default:
        return renderRateLimitsTab();
    }
  };

  // Add Rule Modal
  const renderAddRuleModal = () => (
    showAddRuleModal && (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="border rounded-xl max-w-md w-full max-h-[90vh] overflow-y-auto" style={{ 
          borderColor: colors.modalBorder,
          backgroundColor: colors.modalBg
        }}>
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold" style={{ color: colors.text }}>Add Rate Limit Rule</h3>
              <button 
                onClick={() => setShowAddRuleModal(false)}
                className="p-1 rounded hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover }}
              >
                <X size={20} style={{ color: colors.text }} />
              </button>
            </div>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Rule Name</label>
                <input 
                  type="text" 
                  className="w-full px-3 py-2 rounded-lg text-sm"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="e.g., API Rate Limit for Public Endpoints"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Endpoint Pattern</label>
                <input 
                  type="text" 
                  className="w-full px-3 py-2 rounded-lg text-sm font-mono"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                  placeholder="/api/v1/**"
                />
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Limit</label>
                  <input 
                    type="number" 
                    className="w-full px-3 py-2 rounded-lg text-sm"
                    style={{ 
                      backgroundColor: colors.inputBg,
                      border: `1px solid ${colors.inputBorder}`,
                      color: colors.text
                    }}
                    placeholder="100"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Window</label>
                  <select 
                    className="w-full px-3 py-2 rounded-lg text-sm"
                    style={{ 
                      backgroundColor: colors.inputBg,
                      border: `1px solid ${colors.inputBorder}`,
                      color: colors.text
                    }}
                  >
                    <option value="1s">1 Second</option>
                    <option value="10s">10 Seconds</option>
                    <option value="1m">1 Minute</option>
                    <option value="5m">5 Minutes</option>
                    <option value="1h">1 Hour</option>
                  </select>
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Action</label>
                <select 
                  className="w-full px-3 py-2 rounded-lg text-sm"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    border: `1px solid ${colors.inputBorder}`,
                    color: colors.text
                  }}
                >
                  <option value="throttle">Throttle (429)</option>
                  <option value="block">Block (403)</option>
                  <option value="log">Log Only</option>
                </select>
              </div>
            </div>
            
            <div className="flex items-center justify-end gap-3 mt-8">
              <button 
                onClick={() => setShowAddRuleModal(false)}
                className="px-4 py-2 rounded-lg text-sm font-medium hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                Cancel
              </button>
              <button 
                onClick={() => setShowAddRuleModal(false)}
                className="px-4 py-2 rounded-lg text-sm font-medium hover:opacity-90 transition-colors"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                Add Rule
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  );

  // Security Report Modal
  const renderSecurityReportModal = () => (
    showSecurityReport && (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="border rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto" style={{ 
          borderColor: colors.modalBorder,
          backgroundColor: colors.modalBg
        }}>
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold" style={{ color: colors.text }}>Security Report</h3>
              <button 
                onClick={() => setShowSecurityReport(false)}
                className="p-1 rounded hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover }}
              >
                <X size={20} style={{ color: colors.text }} />
              </button>
            </div>
            
            <div className="space-y-6">
              <div className="p-4 rounded-lg" style={{ backgroundColor: colors.hover }}>
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <div className="text-sm font-semibold" style={{ color: colors.text }}>Report Summary</div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Generated on {new Date().toLocaleDateString()}</div>
                  </div>
                  <div className="px-3 py-1 rounded-full" style={{ 
                    backgroundColor: colors.success + '20',
                    color: colors.success
                  }}>
                    <span className="text-xs font-medium">SECURE</span>
                  </div>
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Total Checks</div>
                    <div className="text-lg font-bold" style={{ color: colors.text }}>24</div>
                  </div>
                  <div>
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Issues Found</div>
                    <div className="text-lg font-bold" style={{ color: colors.success }}>1</div>
                  </div>
                </div>
              </div>
              
              <div>
                <h4 className="text-sm font-semibold mb-3" style={{ color: colors.text }}>Recommendations</h4>
                <div className="space-y-2">
                  {[
                    "Consider implementing stricter rate limits for authentication endpoints",
                    "Add IP whitelist for admin endpoints",
                    "Enable API key rotation for long-lived keys",
                    "Implement request signing for critical endpoints"
                  ].map((rec, index) => (
                    <div key={index} className="flex items-start gap-2 p-3 rounded-lg hover-lift"
                      style={{ backgroundColor: colors.hover }}>
                      <div className="p-1 rounded-full" style={{ backgroundColor: colors.info + '20' }}>
                        <Info size={12} style={{ color: colors.info }} />
                      </div>
                      <span className="text-sm" style={{ color: colors.text }}>{rec}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            
            <div className="flex items-center justify-end gap-3 mt-8">
              <button 
                className="px-4 py-2 rounded-lg text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                <Download size={14} />
                Download PDF
              </button>
              <button 
                onClick={() => setShowSecurityReport(false)}
                className="px-4 py-2 rounded-lg text-sm font-medium hover:opacity-90 transition-colors"
                style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  );

  // Add CSS for hover effects
  const hoverStyles = `
    .hover-lift {
      transition: all 0.2s ease;
    }
    .hover-lift:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  `;

  return (
    <>
      <style>{hoverStyles}</style>
      <div className="min-h-screen" style={{ backgroundColor: colors.bg }}>

        <div className="flex">
          {/* Sidebar */}
          {renderSidebar()}
          
          {/* Main Content */}
          <div className="flex-1 p-6 overflow-y-auto" style={{ backgroundColor: colors.main }}>
            {renderMainContent()}
          </div>
        </div>
        
        {/* Modals */}
        {renderAddRuleModal()}
        {renderSecurityReportModal()}
      </div>
    </>
  );
};

export default APISecurity;