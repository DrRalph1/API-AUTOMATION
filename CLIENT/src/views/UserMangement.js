import React, { useState, useEffect } from 'react';
import {
  Users, UserPlus, UserMinus, UserX, UserCheck, UserCog, UserCircle,
  Shield, Key, Mail, Phone, Calendar, Clock, Search, Filter, MoreVertical,
  Edit, Trash2, Eye, CheckCircle, XCircle, AlertCircle, MoreHorizontal,
  ChevronDown, ChevronUp, Download, Upload, RefreshCw, Lock, Unlock,
  MailCheck, PhoneCall, Globe, MapPin, Building, Briefcase, Tag,
  CreditCard, ShieldCheck, ShieldAlert, Bell, BellOff, MessageSquare,
  Star, Award, Crown, Target, TrendingUp, TrendingDown, BarChart,
  PieChart, Activity, Zap, Cpu, Database, Server, Network, HardDrive,
  Settings, LogOut, LogIn, UserPlus as UserPlusIcon, UserMinus as UserMinusIcon,
  UserX as UserXIcon, UserCheck as UserCheckIcon, Users as UsersIcon,
  UserCircle as UserCircleIcon, UserSquare, Key as KeyIcon,
  Mail as MailIcon, Phone as PhoneIcon, Calendar as CalendarIcon,
  Clock as ClockIcon, MapPin as MapPinIcon, Building as BuildingIcon,
  Briefcase as BriefcaseIcon, Tag as TagIcon, Crown as CrownIcon,
  Award as AwardIcon, Star as StarIcon, Target as TargetIcon,
  CheckCircle as CheckCircleIcon, XCircle as XCircleIcon,
  AlertCircle as AlertCircleIcon, ShieldCheck as ShieldCheckIcon,
  ShieldAlert as ShieldAlertIcon, Bell as BellIcon, BellOff as BellOffIcon,
  MessageSquare as MessageSquareIcon, BarChart as BarChartIcon,
  PieChart as PieChartIcon, Activity as ActivityIcon, Zap as ZapIcon,
  Cpu as CpuIcon, Database as DatabaseIcon, Server as ServerIcon,
  Network as NetworkIcon, HardDrive as HardDriveIcon, Settings as SettingsIcon,
  LogOut as LogOutIcon, LogIn as LogInIcon, Eye as EyeIcon,
  Edit as EditIcon, Trash2 as Trash2Icon, Download as DownloadIcon,
  Upload as UploadIcon, RefreshCw as RefreshCwIcon, Lock as LockIcon,
  Unlock as UnlockIcon, ChevronDown as ChevronDownIcon,
  ChevronUp as ChevronUpIcon, MoreVertical as MoreVerticalIcon,
  MoreHorizontal as MoreHorizontalIcon, Search as SearchIcon,
  Filter as FilterIcon, UserCog as UserCogIcon, Globe as GlobeIcon,
  MailCheck as MailCheckIcon, PhoneCall as PhoneCallIcon, CreditCard as CreditCardIcon,
  Shield as ShieldIcon, KeyRound, UserSquare2, CircleUser,
  SquareUser, TriangleUser, HexagonUser, OctagonUser, DiamondUser,
  ScanFace, QrCode, Fingerprint, Smartphone, Tablet, Monitor,
  Laptop, Tv, Watch, Headphones, Speaker, Volume2, Mic, Camera,
  Video, Film, Music, Headphones as HeadphonesIcon, Speaker as SpeakerIcon,
  Volume2 as Volume2Icon, Mic as MicIcon, Camera as CameraIcon,
  Video as VideoIcon, Film as FilmIcon, Music as MusicIcon,
  Smartphone as SmartphoneIcon, Tablet as TabletIcon, Monitor as MonitorIcon,
  Laptop as LaptopIcon, Tv as TvIcon, Watch as WatchIcon,
  ScanFace as ScanFaceIcon, QrCode as QrCodeIcon, Fingerprint as FingerprintIcon,
  Home, SlidersHorizontal, Menu, X, ChevronRight, ChevronLeft,
  Bell as BellIcon2, Database as DatabaseIcon2, FileCode, Code, BookOpen,
  Layers, GitMerge, BarChart2, Shield as ShieldIcon2, Database as DatabaseIcon3,
  Cpu as CpuIcon2, Globe as GlobeIcon2, Server as ServerIcon2,
  Clock as ClockIcon2, Users as UsersIcon2, File as FileIcon,
  Terminal as TerminalIcon, Layers as LayersIcon, GitBranch as GitBranchIcon,
  BarChart as BarChartIcon2, Activity as ActivityIcon2,
  ChevronsLeft, ChevronsRight, Wifi, GitBranch as GitBranchIcon2,
  Server as ServerIcon3, HardDrive as HardDriveIcon2, Cpu as CpuIcon3,
  ShieldCheck as ShieldCheckIcon2, Lock as LockIcon2, Key as KeyIcon2,
  Fingerprint as FingerprintIcon2, Scan, ScanFace as ScanFaceIcon2,
  QrCode as QrCodeIcon2, ShieldHalf, ShieldQuestion, ShieldX, ShieldPlus,
  ShieldMinus, ShieldEllipsis, BellRing, MessageCircle,
  AlertOctagon, AlertHexagon, AlertDiamond, TriangleAlert, CircleAlert,
  OctagonAlert, HexagonAlert, DiamondAlert, SquareAlert, Circle, Square,
  Triangle, Hexagon, Octagon, Diamond, GitPullRequest, GitCompare, GitCommit,
  GitFork, GitGraph, GitBranchPlus, ServerCog, DatabaseBackup, ServerCrash,
  ServerOff, MemoryStick, Motherboard, CircuitBoard, Chip, Microchip,
  Brain, BrainCircuit, BrainCog, Cloud, UploadCloud, DownloadCloud,
  UserCog as UserCogIcon2, UserCircle2, UserSquare as UserSquareIcon
} from "lucide-react";

const UserManagement = ({ theme, isDark, customTheme, toggleTheme, navigateTo, setActiveTab }) => {
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedRole, setSelectedRole] = useState('all');
  const [selectedStatus, setSelectedStatus] = useState('all');
  const [currentPage, setCurrentPage] = useState(1);
  const [usersPerPage, setUsersPerPage] = useState(10);
  const [sortField, setSortField] = useState('lastActive');
  const [sortDirection, setSortDirection] = useState('desc');
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [showFilters, setShowFilters] = useState(false);
  
  // Modal states
  const [modalStack, setModalStack] = useState([]);
  
  // Mobile states
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isMobileSearchOpen, setIsMobileSearchOpen] = useState(false);
  const [isRightSidebarVisible, setIsRightSidebarVisible] = useState(false);

  // Static user data
  const [users, setUsers] = useState([
    {
      id: 'user-1',
      username: 'john.doe',
      email: 'john.doe@example.com',
      fullName: 'John Doe',
      role: 'admin',
      status: 'active',
      lastActive: '2024-01-15T14:30:00Z',
      joinedDate: '2023-06-15T10:00:00Z',
      avatarColor: '#3B82F6',
      department: 'Engineering',
      permissions: ['read', 'write', 'delete', 'admin'],
      mfaEnabled: true,
      emailVerified: true,
      phoneVerified: true,
      apiAccessCount: 24,
      lastLoginIp: '192.168.1.100',
      location: 'San Francisco, CA',
      timezone: 'PST',
      totalLogins: 156,
      failedLogins: 3,
      securityScore: 95,
      tags: ['core-team', 'backend', 'devops'],
      devices: [
        { type: 'laptop', lastUsed: '2024-01-15T10:30:00Z' },
        { type: 'phone', lastUsed: '2024-01-15T09:15:00Z' }
      ],
      apiKeys: 3,
      sessions: 2
    },
    {
      id: 'user-2',
      username: 'jane.smith',
      email: 'jane.smith@example.com',
      fullName: 'Jane Smith',
      role: 'developer',
      status: 'active',
      lastActive: '2024-01-15T12:45:00Z',
      joinedDate: '2023-08-20T09:30:00Z',
      avatarColor: '#10B981',
      department: 'Frontend',
      permissions: ['read', 'write'],
      mfaEnabled: true,
      emailVerified: true,
      phoneVerified: false,
      apiAccessCount: 12,
      lastLoginIp: '192.168.1.101',
      location: 'New York, NY',
      timezone: 'EST',
      totalLogins: 89,
      failedLogins: 1,
      securityScore: 88,
      tags: ['frontend', 'ui-ux'],
      devices: [
        { type: 'laptop', lastUsed: '2024-01-15T08:45:00Z' }
      ],
      apiKeys: 2,
      sessions: 1
    },
    {
      id: 'user-3',
      username: 'bob.johnson',
      email: 'bob.johnson@example.com',
      fullName: 'Bob Johnson',
      role: 'viewer',
      status: 'active',
      lastActive: '2024-01-14T16:20:00Z',
      joinedDate: '2023-11-05T14:15:00Z',
      avatarColor: '#F59E0B',
      department: 'Marketing',
      permissions: ['read'],
      mfaEnabled: false,
      emailVerified: true,
      phoneVerified: true,
      apiAccessCount: 5,
      lastLoginIp: '192.168.1.102',
      location: 'Chicago, IL',
      timezone: 'CST',
      totalLogins: 42,
      failedLogins: 0,
      securityScore: 75,
      tags: ['marketing', 'analytics'],
      devices: [
        { type: 'desktop', lastUsed: '2024-01-14T15:30:00Z' }
      ],
      apiKeys: 1,
      sessions: 1
    },
    {
      id: 'user-4',
      username: 'alice.brown',
      email: 'alice.brown@example.com',
      fullName: 'Alice Brown',
      role: 'admin',
      status: 'inactive',
      lastActive: '2024-01-10T11:15:00Z',
      joinedDate: '2023-09-12T13:45:00Z',
      avatarColor: '#8B5CF6',
      department: 'Engineering',
      permissions: ['read', 'write', 'delete', 'admin'],
      mfaEnabled: true,
      emailVerified: true,
      phoneVerified: true,
      apiAccessCount: 18,
      lastLoginIp: '192.168.1.103',
      location: 'Seattle, WA',
      timezone: 'PST',
      totalLogins: 112,
      failedLogins: 2,
      securityScore: 92,
      tags: ['core-team', 'fullstack'],
      devices: [
        { type: 'laptop', lastUsed: '2024-01-10T10:00:00Z' },
        { type: 'tablet', lastUsed: '2024-01-09T16:45:00Z' }
      ],
      apiKeys: 2,
      sessions: 0
    },
    {
      id: 'user-5',
      username: 'charlie.wilson',
      email: 'charlie.wilson@example.com',
      fullName: 'Charlie Wilson',
      role: 'developer',
      status: 'pending',
      lastActive: '2024-01-13T09:30:00Z',
      joinedDate: '2024-01-02T08:00:00Z',
      avatarColor: '#EF4444',
      department: 'Backend',
      permissions: ['read', 'write'],
      mfaEnabled: false,
      emailVerified: false,
      phoneVerified: false,
      apiAccessCount: 0,
      lastLoginIp: '192.168.1.104',
      location: 'Austin, TX',
      timezone: 'CST',
      totalLogins: 3,
      failedLogins: 0,
      securityScore: 45,
      tags: ['new-user', 'onboarding'],
      devices: [],
      apiKeys: 0,
      sessions: 1
    },
    {
      id: 'user-6',
      username: 'david.lee',
      email: 'david.lee@example.com',
      fullName: 'David Lee',
      role: 'viewer',
      status: 'suspended',
      lastActive: '2024-01-05T14:20:00Z',
      joinedDate: '2023-10-18T11:30:00Z',
      avatarColor: '#6B7280',
      department: 'Sales',
      permissions: ['read'],
      mfaEnabled: false,
      emailVerified: true,
      phoneVerified: false,
      apiAccessCount: 8,
      lastLoginIp: '192.168.1.105',
      location: 'Miami, FL',
      timezone: 'EST',
      totalLogins: 67,
      failedLogins: 5,
      securityScore: 60,
      tags: ['sales', 'external'],
      devices: [
        { type: 'laptop', lastUsed: '2024-01-05T13:15:00Z' }
      ],
      apiKeys: 1,
      sessions: 0
    },
    {
      id: 'user-7',
      username: 'emma.davis',
      email: 'emma.davis@example.com',
      fullName: 'Emma Davis',
      role: 'developer',
      status: 'active',
      lastActive: '2024-01-15T08:15:00Z',
      joinedDate: '2023-07-22T09:45:00Z',
      avatarColor: '#EC4899',
      department: 'Mobile',
      permissions: ['read', 'write'],
      mfaEnabled: true,
      emailVerified: true,
      phoneVerified: true,
      apiAccessCount: 15,
      lastLoginIp: '192.168.1.106',
      location: 'Boston, MA',
      timezone: 'EST',
      totalLogins: 134,
      failedLogins: 1,
      securityScore: 91,
      tags: ['mobile', 'ios', 'android'],
      devices: [
        { type: 'phone', lastUsed: '2024-01-15T07:30:00Z' },
        { type: 'tablet', lastUsed: '2024-01-14T18:45:00Z' }
      ],
      apiKeys: 2,
      sessions: 2
    },
    {
      id: 'user-8',
      username: 'frank.miller',
      email: 'frank.miller@example.com',
      fullName: 'Frank Miller',
      role: 'admin',
      status: 'active',
      lastActive: '2024-01-15T16:45:00Z',
      joinedDate: '2023-05-10T14:20:00Z',
      avatarColor: '#14B8A6',
      department: 'DevOps',
      permissions: ['read', 'write', 'delete', 'admin'],
      mfaEnabled: true,
      emailVerified: true,
      phoneVerified: true,
      apiAccessCount: 32,
      lastLoginIp: '192.168.1.107',
      location: 'Denver, CO',
      timezone: 'MST',
      totalLogins: 201,
      failedLogins: 0,
      securityScore: 98,
      tags: ['core-team', 'infrastructure', 'security'],
      devices: [
        { type: 'laptop', lastUsed: '2024-01-15T15:00:00Z' },
        { type: 'desktop', lastUsed: '2024-01-14T12:30:00Z' }
      ],
      apiKeys: 4,
      sessions: 3
    },
    {
      id: 'user-9',
      username: 'grace.wilson',
      email: 'grace.wilson@example.com',
      fullName: 'Grace Wilson',
      role: 'viewer',
      status: 'active',
      lastActive: '2024-01-14T10:30:00Z',
      joinedDate: '2023-12-01T10:00:00Z',
      avatarColor: '#F97316',
      department: 'Support',
      permissions: ['read'],
      mfaEnabled: false,
      emailVerified: true,
      phoneVerified: false,
      apiAccessCount: 3,
      lastLoginIp: '192.168.1.108',
      location: 'Portland, OR',
      timezone: 'PST',
      totalLogins: 28,
      failedLogins: 0,
      securityScore: 70,
      tags: ['support', 'customer-service'],
      devices: [
        { type: 'laptop', lastUsed: '2024-01-14T09:15:00Z' }
      ],
      apiKeys: 1,
      sessions: 1
    },
    {
      id: 'user-10',
      username: 'henry.clark',
      email: 'henry.clark@example.com',
      fullName: 'Henry Clark',
      role: 'developer',
      status: 'inactive',
      lastActive: '2024-01-08T15:45:00Z',
      joinedDate: '2023-11-20T16:30:00Z',
      avatarColor: '#8B5CF6',
      department: 'QA',
      permissions: ['read', 'write'],
      mfaEnabled: true,
      emailVerified: true,
      phoneVerified: true,
      apiAccessCount: 7,
      lastLoginIp: '192.168.1.109',
      location: 'Atlanta, GA',
      timezone: 'EST',
      totalLogins: 56,
      failedLogins: 2,
      securityScore: 82,
      tags: ['qa', 'testing'],
      devices: [
        { type: 'desktop', lastUsed: '2024-01-08T14:30:00Z' }
      ],
      apiKeys: 1,
      sessions: 0
    }
  ]);

  // Statistics
  const [stats, setStats] = useState({
    totalUsers: 0,
    activeUsers: 0,
    admins: 0,
    developers: 0,
    pendingUsers: 0,
    suspendedUsers: 0,
    mfaEnabled: 0,
    avgSecurityScore: 0
  });

  // Color scheme - EXACTLY matching Dashboard
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
    connectionIdle: '#f59e0b'
  };

  // Role colors
  const roleColors = {
    admin: colors.error,
    developer: colors.primary,
    viewer: colors.success,
    moderator: colors.warning,
    guest: colors.textSecondary
  };

  // Status colors
  const statusColors = {
    active: colors.success,
    inactive: colors.warning,
    pending: colors.info,
    suspended: colors.error,
    archived: colors.textSecondary
  };

  // Initialize stats
  useEffect(() => {
    const calculateStats = () => {
      const totalUsers = users.length;
      const activeUsers = users.filter(u => u.status === 'active').length;
      const admins = users.filter(u => u.role === 'admin').length;
      const developers = users.filter(u => u.role === 'developer').length;
      const pendingUsers = users.filter(u => u.status === 'pending').length;
      const suspendedUsers = users.filter(u => u.status === 'suspended').length;
      const mfaEnabled = users.filter(u => u.mfaEnabled).length;
      const avgSecurityScore = users.reduce((sum, u) => sum + u.securityScore, 0) / totalUsers;

      setStats({
        totalUsers,
        activeUsers,
        admins,
        developers,
        pendingUsers,
        suspendedUsers,
        mfaEnabled,
        avgSecurityScore: Math.round(avgSecurityScore)
      });
    };

    calculateStats();
  }, [users]);

  // Modal management functions
  const openModal = (type, data) => {
    setModalStack(prev => [...prev, { type, data }]);
  };

  const closeModal = () => {
    setModalStack(prev => prev.slice(0, -1));
  };

  const closeAllModals = () => {
    setModalStack([]);
  };

  const getCurrentModal = () => {
    if (modalStack.length === 0) return null;
    return modalStack[modalStack.length - 1];
  };

  // Filter and sort users
  const filteredUsers = users.filter(user => {
    const matchesSearch = searchQuery === '' || 
      user.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.fullName.toLowerCase().includes(searchQuery.toLowerCase());
    
    const matchesRole = selectedRole === 'all' || user.role === selectedRole;
    const matchesStatus = selectedStatus === 'all' || user.status === selectedStatus;
    
    return matchesSearch && matchesRole && matchesStatus;
  });

  const sortedUsers = [...filteredUsers].sort((a, b) => {
    if (sortField === 'lastActive') {
      return sortDirection === 'asc' 
        ? new Date(a.lastActive) - new Date(b.lastActive)
        : new Date(b.lastActive) - new Date(a.lastActive);
    }
    if (sortField === 'fullName') {
      return sortDirection === 'asc'
        ? a.fullName.localeCompare(b.fullName)
        : b.fullName.localeCompare(a.fullName);
    }
    if (sortField === 'joinedDate') {
      return sortDirection === 'asc'
        ? new Date(a.joinedDate) - new Date(b.joinedDate)
        : new Date(b.joinedDate) - new Date(a.joinedDate);
    }
    return 0;
  });

  // Pagination
  const totalPages = Math.ceil(sortedUsers.length / usersPerPage);
  const indexOfLastUser = currentPage * usersPerPage;
  const indexOfFirstUser = indexOfLastUser - usersPerPage;
  const currentUsers = sortedUsers.slice(indexOfFirstUser, indexOfLastUser);

  // Handlers
  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedUsers(currentUsers.map(user => user.id));
    } else {
      setSelectedUsers([]);
    }
  };

  const handleSelectUser = (userId) => {
    setSelectedUsers(prev => 
      prev.includes(userId)
        ? prev.filter(id => id !== userId)
        : [...prev, userId]
    );
  };

  const handleDeleteUser = (user) => {
    if (window.confirm(`Are you sure you want to delete ${user.fullName}?`)) {
      setUsers(prev => prev.filter(u => u.id !== user.id));
      setSelectedUsers(prev => prev.filter(id => id !== user.id));
    }
  };

  const handleToggleUserStatus = (user, newStatus) => {
    setUsers(prev => prev.map(u => 
      u.id === user.id ? { ...u, status: newStatus } : u
    ));
  };

  const handleRefresh = () => {
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      console.log('User list refreshed');
    }, 1000);
  };

  const handleSort = (field) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const handleExportData = () => {
    console.log('Exporting user data...');
    alert('Export functionality would generate a CSV file in a real application');
  };

  const handleImportUsers = () => {
    openModal('importUsers', {});
  };

  const handleViewUserDetails = (user) => {
    openModal('userDetails', user);
  };

  const handleEditUser = (user) => {
    openModal('editUser', user);
  };

  const handleResetPassword = (user) => {
    openModal('resetPassword', user);
  };

  const handleBulkAction = (action) => {
    switch(action) {
      case 'activate':
        setUsers(prev => prev.map(user => 
          selectedUsers.includes(user.id) ? { ...user, status: 'active' } : user
        ));
        break;
      case 'suspend':
        setUsers(prev => prev.map(user => 
          selectedUsers.includes(user.id) ? { ...user, status: 'suspended' } : user
        ));
        break;
      case 'delete':
        if (window.confirm(`Delete ${selectedUsers.length} selected users?`)) {
          setUsers(prev => prev.filter(user => !selectedUsers.includes(user.id)));
          setSelectedUsers([]);
        }
        break;
    }
  };

  // Side Navigation Component
  const SideNavigation = () => {
    const [expandedSections, setExpandedSections] = useState(['users', 'security', 'roles']);
    
    const sideNavItems = [
      {
        id: 'users',
        label: 'User Management',
        icon: <Users size={16} />,
        subItems: [
          { id: 'all-users', label: 'All Users', icon: <UsersIcon size={12} /> },
          { id: 'active-users', label: 'Active Users', icon: <UserCheck size={12} /> },
          { id: 'pending-users', label: 'Pending Users', icon: <Clock size={12} /> },
          { id: 'suspended-users', label: 'Suspended Users', icon: <UserX size={12} /> },
          { id: 'user-groups', label: 'User Groups', icon: <UserCog size={12} /> }
        ]
      },
      {
        id: 'roles',
        label: 'Roles & Permissions',
        icon: <Shield size={16} />,
        subItems: [
          { id: 'role-management', label: 'Role Management', icon: <ShieldIcon size={12} /> },
          { id: 'permission-sets', label: 'Permission Sets', icon: <KeyRound size={12} /> },
          { id: 'access-control', label: 'Access Control', icon: <Lock size={12} /> },
          { id: 'api-permissions', label: 'API Permissions', icon: <FileCode size={12} /> }
        ]
      }
    ];

    const toggleSection = (sectionId) => {
      setExpandedSections(prev =>
        prev.includes(sectionId)
          ? prev.filter(id => id !== sectionId)
          : [...prev, sectionId]
      );
    };

    return (
      <div className="w-80 border-r flex flex-col" style={{ 
        backgroundColor: colors.sidebar,
        borderColor: colors.border
      }}>
        {/* Header */}
        <div className="p-4 border-b" style={{ borderColor: colors.border }}>
      
          {/* Search */}
          <div className="relative mb-3">
            <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
            <input 
              type="text" 
              placeholder="Search users..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-8 pr-3 py-2 rounded text-sm focus:outline-none hover-lift"
              style={{ 
                backgroundColor: colors.inputBg, 
                border: `1px solid ${colors.border}`, 
                color: colors.text 
              }} 
            />
          </div>
          
          {/* Quick Actions */}
          <div className="flex gap-2">
            <button 
              onClick={() => openModal('editUser', { id: 'new' })}
              className="flex-1 px-3 py-2 rounded text-sm font-medium hover-lift transition-all duration-200 flex items-center gap-2 justify-center"
              style={{ 
                backgroundColor: colors.primaryDark,
                color: 'white'
              }}
            >
              <UserPlus size={12} />
              <span>Add User</span>
            </button>
            <button 
              onClick={handleImportUsers}
              className="px-3 py-2 rounded text-sm font-medium hover-lift transition-all duration-200 flex items-center gap-2"
              style={{ 
                backgroundColor: colors.hover,
                color: colors.text
              }}
            >
              <Upload size={12} />
            </button>
          </div>
        </div>

        {/* Navigation Items */}
        <div className="flex-1 overflow-auto p-2">
          {sideNavItems.map((item) => (
            <div key={item.id} className="mb-1">
              <button
                onClick={() => toggleSection(item.id)}
                className={`w-full flex items-center gap-2 px-3 py-2.5 rounded text-sm transition-all duration-200 hover-lift mb-1`}
                style={{ 
                  backgroundColor: expandedSections.includes(item.id) ? colors.selected : colors.hover,
                  color: expandedSections.includes(item.id) ? colors.primary : colors.text
                }}
              >
                <span style={{ 
                  color: expandedSections.includes(item.id) ? colors.primary : colors.textSecondary 
                }}>
                  {item.icon}
                </span>
                <span className="flex-1 text-left truncate">{item.label}</span>
                {item.subItems && (
                  <ChevronDown 
                    size={12} 
                    className={`transition-transform duration-200 ${
                      expandedSections.includes(item.id) ? 'rotate-0' : '-rotate-90'
                    }`}
                    style={{ color: colors.textSecondary }}
                  />
                )}
              </button>

              {item.subItems && expandedSections.includes(item.id) && (
                <div className="ml-6 mb-2 border-l-2" style={{ borderColor: colors.border }}>
                  {item.subItems.map((subItem) => (
                    <button
                      key={subItem.id}
                      onClick={() => {
                        console.log(`Navigating to ${subItem.label}`);
                        // Filter users based on selection
                        if (subItem.id === 'all-users') {
                          setSelectedRole('all');
                          setSelectedStatus('all');
                        } else if (subItem.id === 'active-users') {
                          setSelectedStatus('active');
                        } else if (subItem.id === 'pending-users') {
                          setSelectedStatus('pending');
                        } else if (subItem.id === 'suspended-users') {
                          setSelectedStatus('suspended');
                        }
                      }}
                      className="w-full flex items-center gap-2 px-3 py-1.5 rounded text-sm transition-colors hover:bg-opacity-50 ml-2 mt-0.5 hover-lift"
                      style={{ 
                        backgroundColor: colors.hover,
                        color: colors.textSecondary
                      }}
                    >
                      <span style={{ color: colors.textTertiary }}>
                        {subItem.icon}
                      </span>
                      <span className="truncate">{subItem.label}</span>
                    </button>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>

      </div>
    );
  };

  // Stat Card Component
  const StatCard = ({ title, value, icon: Icon, color, onClick, change }) => {
    return (
      <div 
        className="border rounded-xl p-3 hover-lift cursor-pointer transition-all duration-200"
        onClick={onClick}
        style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}
      >
        <div className="flex items-center justify-between mb-2">
          <div className="text-xs font-medium truncate" style={{ color: colors.textSecondary }}>
            {title}
          </div>
          <div className="p-2 rounded-lg shrink-0" style={{ backgroundColor: `${color}20` }}>
            <Icon size={16} style={{ color }} />
          </div>
        </div>
        <div className="flex items-end justify-between">
          <div className="text-lg font-bold truncate" style={{ color: colors.text }}>
            {value}
          </div>
          {change && (
            <div className={`text-xs px-2 py-1 rounded-full shrink-0 ${change > 0 ? 'text-green-600 bg-green-100' : 'text-red-600 bg-red-100'}`}>
              {change > 0 ? '+' : ''}{change}%
            </div>
          )}
        </div>
      </div>
    );
  };

  // User Card Component for mobile
  const UserCard = ({ user }) => {
    const formatDate = (dateString) => {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    };

    const formatTime = (dateString) => {
      const date = new Date(dateString);
      return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    };

    return (
      <div 
        className="border rounded-xl p-3 hover-lift transition-all duration-200"
        style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}
      >
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center gap-3">
            <div 
              className="w-10 h-10 rounded-full flex items-center justify-center text-white font-medium text-sm"
              style={{ backgroundColor: user.avatarColor }}
            >
              {user.fullName.split(' ').map(n => n[0]).join('')}
            </div>
            <div className="min-w-0">
              <div className="text-sm font-semibold truncate" style={{ color: colors.text }}>
                {user.fullName}
              </div>
              <div className="text-xs truncate" style={{ color: colors.textSecondary }}>
                @{user.username}
              </div>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <div 
              className="px-2 py-1 rounded-full text-xs font-medium"
              style={{ 
                backgroundColor: `${roleColors[user.role]}20`,
                color: roleColors[user.role]
              }}
            >
              {user.role}
            </div>
            <button
              onClick={() => handleViewUserDetails(user)}
              className="p-1 rounded hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.hover }}
            >
              <MoreVertical size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1">
              <Mail size={12} style={{ color: colors.textSecondary }} />
              <span className="text-xs truncate" style={{ color: colors.textSecondary }}>
                {user.email}
              </span>
            </div>
            {user.emailVerified ? (
              <CheckCircle size={12} style={{ color: colors.success }} />
            ) : (
              <XCircle size={12} style={{ color: colors.error }} />
            )}
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1">
              <Building size={12} style={{ color: colors.textSecondary }} />
              <span className="text-xs" style={{ color: colors.textSecondary }}>
                {user.department}
              </span>
            </div>
            <div 
              className="px-2 py-0.5 rounded-full text-xs"
              style={{ 
                backgroundColor: `${statusColors[user.status]}20`,
                color: statusColors[user.status]
              }}
            >
              {user.status}
            </div>
          </div>

          <div className="flex items-center justify-between text-xs pt-2 border-t" style={{ borderColor: colors.border }}>
            <div className="flex items-center gap-1">
              <Clock size={12} style={{ color: colors.textSecondary }} />
              <span style={{ color: colors.textSecondary }}>
                {formatDate(user.lastActive)} {formatTime(user.lastActive)}
              </span>
            </div>
            <div className="flex items-center gap-2">
              {user.mfaEnabled && (
                <Shield size={12} style={{ color: colors.success }} title="MFA Enabled" />
              )}
              {user.apiKeys > 0 && (
                <Key size={12} style={{ color: colors.info }} title={`${user.apiKeys} API keys`} />
              )}
            </div>
          </div>

          <div className="flex gap-2 pt-2">
            <button
              onClick={() => handleEditUser(user)}
              className="flex-1 px-2 py-1.5 rounded text-xs font-medium transition-colors hover-lift"
              style={{ 
                backgroundColor: colors.hover,
                color: colors.text
              }}
            >
              Edit
            </button>
            <button
              onClick={() => handleResetPassword(user)}
              className="flex-1 px-2 py-1.5 rounded text-xs font-medium transition-colors hover-lift"
              style={{ 
                backgroundColor: colors.primaryDark,
                color: 'white'
              }}
            >
              Reset PW
            </button>
          </div>
        </div>
      </div>
    );
  };

  // Pagination Component
  const Pagination = () => {
    const renderPageNumbers = () => {
      const pages = [];
      const maxVisiblePages = 5;
      
      if (totalPages <= maxVisiblePages) {
        for (let i = 1; i <= totalPages; i++) {
          pages.push(i);
        }
      } else {
        if (currentPage <= 3) {
          for (let i = 1; i <= 4; i++) pages.push(i);
          pages.push('...');
          pages.push(totalPages);
        } else if (currentPage >= totalPages - 2) {
          pages.push(1);
          pages.push('...');
          for (let i = totalPages - 3; i <= totalPages; i++) pages.push(i);
        } else {
          pages.push(1);
          pages.push('...');
          pages.push(currentPage - 1);
          pages.push(currentPage);
          pages.push(currentPage + 1);
          pages.push('...');
          pages.push(totalPages);
        }
      }
      
      return pages;
    };

    return (
      <div className="flex flex-col sm:flex-row items-center justify-between gap-3 p-4 border-t" style={{ borderColor: colors.border }}>
        <div className="text-xs" style={{ color: colors.textSecondary }}>
          Showing {indexOfFirstUser + 1} - {Math.min(indexOfLastUser, sortedUsers.length)} of {sortedUsers.length} users
        </div>
        
        <div className="flex items-center gap-1">
          <button
            onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
            disabled={currentPage === 1}
            className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
            style={{ 
              backgroundColor: currentPage === 1 ? 'transparent' : colors.hover,
              color: colors.text,
              cursor: currentPage === 1 ? 'not-allowed' : 'pointer'
            }}
          >
            <ChevronLeft size={14} />
          </button>
          
          <div className="flex items-center gap-1">
            {renderPageNumbers().map((page, index) => (
              page === '...' ? (
                <span key={index} className="px-2 text-xs" style={{ color: colors.textSecondary }}>
                  ...
                </span>
              ) : (
                <button
                  key={index}
                  onClick={() => setCurrentPage(page)}
                  className="w-6 h-6 rounded text-xs font-medium transition-colors"
                  style={{ 
                    backgroundColor: currentPage === page ? colors.selected : 'transparent',
                    color: currentPage === page ? colors.primaryDark : colors.textSecondary
                  }}
                >
                  {page}
                </button>
              )
            ))}
          </div>
          
          <button
            onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
            disabled={currentPage === totalPages}
            className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors"
            style={{ 
              backgroundColor: currentPage === totalPages ? 'transparent' : colors.hover,
              color: colors.text,
              cursor: currentPage === totalPages ? 'not-allowed' : 'pointer'
            }}
          >
            <ChevronRight size={14} />
          </button>
        </div>
      </div>
    );
  };

  return (
    <div className="flex flex-col h-full" style={{ 
      backgroundColor: colors.bg,
      color: colors.text,
      fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif',
      fontSize: '13px'
    }}>
      <style>{`
        .hover-lift:hover {
          transform: translateY(-2px);
          transition: transform 0.2s ease;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        .animate-fade-in {
          animation: fadeIn 0.2s ease-out;
        }
        
        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        
        ::-webkit-scrollbar {
          width: 6px;
          height: 6px;
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
        
        @media (max-width: 640px) {
          .text-xs { font-size: 11px; }
          .text-sm { font-size: 12px; }
          .text-base { font-size: 14px; }
          .text-lg { font-size: 16px; }
          .text-xl { font-size: 18px; }
          .text-2xl { font-size: 20px; }
          
          ::-webkit-scrollbar {
            width: 4px;
            height: 4px;
          }
        }
      `}</style>

      {/* TOP NAVIGATION */}
            <div className="flex items-center justify-between h-10 px-4 border-b" style={{ 
              backgroundColor: colors.header,
              borderColor: colors.border
            }}>
      
              <div className="flex items-center gap-2">
                  <span className="px-3 py-1.5 text-sm font-medium -ml-3 uppercase">User Management</span>
              </div>
      
              <div className="flex items-center gap-2">
                {/* Search */}
                <div className="relative">
                  <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
                  <input 
                    type="text" 
                    placeholder="Search user details..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="pl-8 pr-3 py-1.5 rounded text-xs focus:outline-none w-64 hover-lift"
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
      
                {/* Settings */}
                {/* <button className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  onClick={() => showToast('Opening settings', 'info')}
                  style={{ backgroundColor: colors.hover }}>
                  <Settings size={14} style={{ color: colors.textSecondary }} />
                </button> */}
              </div>
            </div>

      {/* Main Content */}
      <div className="flex-1 overflow-hidden flex">
        {/* Side Navigation */}
        <SideNavigation />

        {/* Main content area */}
        <div className="flex-1 overflow-auto p-6">
          <div className="max-w-8xl mx-auto ml-2 mr-2">
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
              <div>
                <h1 className="text-xl font-bold" style={{ color: colors.text }}>
                  User Management
                </h1>
                <p className="text-xs" style={{ color: colors.textSecondary }}>
                  Manage user accounts, roles, and permissions
                </p>
              </div>
              <div className="flex items-center gap-3">
                <button 
                  onClick={handleRefresh}
                  className="p-2 rounded-lg hover-lift transition-all duration-200"
                  style={{ 
                    backgroundColor: colors.hover,
                    color: colors.text
                  }}
                  title="Refresh"
                >
                  <RefreshCw size={18} className={loading ? 'animate-spin' : ''} />
                </button>
                <button 
                  onClick={handleExportData}
                  className="px-3 py-2 rounded-lg text-sm font-medium hover-lift transition-all duration-200 flex items-center gap-2"
                  style={{ 
                    backgroundColor: colors.hover,
                    color: colors.text
                  }}
                >
                  <Download size={14} />
                  <span className="hidden sm:inline">Export</span>
                </button>
                <button 
                  onClick={handleImportUsers}
                  className="px-3 py-2 rounded-lg text-sm font-medium hover-lift transition-all duration-200 flex items-center gap-2"
                  style={{ 
                    backgroundColor: colors.primaryDark,
                    color: 'white'
                  }}
                >
                  <Upload size={14} />
                  <span className="hidden sm:inline">Import Users</span>
                  <span className="sm:hidden">Import</span>
                </button>
                <button 
                  onClick={() => openModal('editUser', {
                    id: 'new',
                    username: '',
                    email: '',
                    fullName: '',
                    role: 'viewer',
                    status: 'pending'
                  })}
                  className="px-3 py-2 rounded-lg text-sm font-medium hover-lift transition-all duration-200 flex items-center gap-2"
                  style={{ 
                    backgroundColor: colors.success,
                    color: 'white'
                  }}
                >
                  <UserPlus size={14} />
                  <span className="hidden sm:inline">Add User</span>
                  <span className="sm:hidden">Add</span>
                </button>
              </div>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
              <StatCard
                title="Total Users"
                value={stats.totalUsers}
                icon={Users}
                color={colors.primary}
              />
              <StatCard
                title="Active Users"
                value={stats.activeUsers}
                icon={UserCheck}
                color={colors.success}
                change={+12}
              />
              <StatCard
                title="Admins"
                value={stats.admins}
                icon={Shield}
                color={colors.error}
              />
              <StatCard
                title="Security Score"
                value={`${stats.avgSecurityScore}/100`}
                icon={ShieldCheck}
                color={colors.warning}
              />
            </div>

            {/* Search and Filters */}
            <div className="border rounded-xl mb-6" style={{ 
              borderColor: colors.border,
              backgroundColor: colors.card
            }}>
              <div className="p-4 border-b" style={{ borderColor: colors.border }}>
                <div className="flex flex-col sm:flex-row gap-3">
                  <div className="flex-1 relative">
                    <Search size={16} className="absolute left-3 top-1/2 transform -translate-y-1/2" style={{ color: colors.textSecondary }} />
                    <input
                      type="text"
                      placeholder="Search users by name, email, or username..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="w-full pl-10 pr-3 py-2 rounded border text-sm"
                      style={{ 
                        backgroundColor: colors.inputBg,
                        borderColor: colors.inputBorder,
                        color: colors.text
                      }}
                    />
                  </div>
                  <div className="flex gap-2">
                    <select
                      value={selectedRole}
                      onChange={(e) => setSelectedRole(e.target.value)}
                      className="px-3 py-2 rounded border text-sm"
                      style={{ 
                        backgroundColor: colors.inputBg,
                        borderColor: colors.inputBorder,
                        color: colors.text
                      }}
                    >
                      <option value="all">All Roles</option>
                      <option value="admin">Admin</option>
                      <option value="developer">Developer</option>
                      <option value="viewer">Viewer</option>
                      <option value="moderator">Moderator</option>
                    </select>
                    <select
                      value={selectedStatus}
                      onChange={(e) => setSelectedStatus(e.target.value)}
                      className="px-3 py-2 rounded border text-sm"
                      style={{ 
                        backgroundColor: colors.inputBg,
                        borderColor: colors.inputBorder,
                        color: colors.text
                      }}
                    >
                      <option value="all">All Status</option>
                      <option value="active">Active</option>
                      <option value="inactive">Inactive</option>
                      <option value="pending">Pending</option>
                      <option value="suspended">Suspended</option>
                    </select>
                    <button
                      onClick={() => setShowFilters(!showFilters)}
                      className="px-3 py-2 rounded border text-sm font-medium hover-lift transition-colors"
                      style={{ 
                        backgroundColor: colors.hover,
                        borderColor: colors.border,
                        color: colors.text
                      }}
                    >
                      <Filter size={14} />
                    </button>
                  </div>
                </div>

                {showFilters && (
                  <div className="mt-4 p-3 rounded border" style={{ 
                    borderColor: colors.border,
                    backgroundColor: colors.hover
                  }}>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                      <select className="px-2 py-1 rounded border text-xs" style={{ 
                        backgroundColor: colors.inputBg,
                        borderColor: colors.inputBorder,
                        color: colors.text
                      }}>
                        <option>Department</option>
                        <option>Engineering</option>
                        <option>Marketing</option>
                        <option>Sales</option>
                      </select>
                      <select className="px-2 py-1 rounded border text-xs" style={{ 
                        backgroundColor: colors.inputBg,
                        borderColor: colors.inputBorder,
                        color: colors.text
                      }}>
                        <option>MFA Status</option>
                        <option>Enabled</option>
                        <option>Disabled</option>
                      </select>
                      <select className="px-2 py-1 rounded border text-xs" style={{ 
                        backgroundColor: colors.inputBg,
                        borderColor: colors.inputBorder,
                        color: colors.text
                      }}>
                        <option>Email Verified</option>
                        <option>Yes</option>
                        <option>No</option>
                      </select>
                      <input
                        type="date"
                        className="px-2 py-1 rounded border text-xs"
                        style={{ 
                          backgroundColor: colors.inputBg,
                          borderColor: colors.inputBorder,
                          color: colors.text
                        }}
                      />
                    </div>
                  </div>
                )}
              </div>

              {/* Bulk Actions */}
              {selectedUsers.length > 0 && (
                <div className="p-3 border-b flex items-center justify-between" style={{ 
                  borderColor: colors.border,
                  backgroundColor: colors.selected
                }}>
                  <div className="text-sm font-medium" style={{ color: colors.text }}>
                    {selectedUsers.length} user{selectedUsers.length > 1 ? 's' : ''} selected
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleBulkAction('activate')}
                      className="px-2 py-1 rounded text-xs font-medium hover-lift transition-colors"
                      style={{ 
                        backgroundColor: colors.success,
                        color: 'white'
                      }}
                    >
                      Activate
                    </button>
                    <button
                      onClick={() => handleBulkAction('suspend')}
                      className="px-2 py-1 rounded text-xs font-medium hover-lift transition-colors"
                      style={{ 
                        backgroundColor: colors.warning,
                        color: 'white'
                      }}
                    >
                      Suspend
                    </button>
                    <button
                      onClick={() => handleBulkAction('delete')}
                      className="px-2 py-1 rounded text-xs font-medium hover-lift transition-colors"
                      style={{ 
                        backgroundColor: colors.error,
                        color: 'white'
                      }}
                    >
                      Delete
                    </button>
                    <button
                      onClick={() => setSelectedUsers([])}
                      className="px-2 py-1 rounded text-xs font-medium hover-lift transition-colors"
                      style={{ 
                        backgroundColor: colors.hover,
                        color: colors.text
                      }}
                    >
                      Clear
                    </button>
                  </div>
                </div>
              )}

              {/* Users Table */}
              <div className="overflow-x-auto">
                <table className="w-full" style={{ borderColor: colors.border }}>
                  <thead>
                    <tr style={{ backgroundColor: colors.tableHeader }}>
                      <th className="p-3 text-left text-xs font-medium" style={{ color: colors.textSecondary }}>
                        <input
                          type="checkbox"
                          checked={selectedUsers.length === currentUsers.length && currentUsers.length > 0}
                          onChange={handleSelectAll}
                          className="rounded border-gray-300"
                          style={{ borderColor: colors.border }}
                        />
                      </th>
                      <th 
                        className="p-3 text-left text-xs font-medium cursor-pointer hover:bg-opacity-50 transition-colors"
                        onClick={() => handleSort('fullName')}
                        style={{ color: colors.textSecondary }}
                      >
                        <div className="flex items-center gap-1">
                          User
                          {sortField === 'fullName' && (
                            sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />
                          )}
                        </div>
                      </th>
                      <th className="p-3 text-left text-xs font-medium" style={{ color: colors.textSecondary }}>
                        Email
                      </th>
                      <th className="p-3 text-left text-xs font-medium" style={{ color: colors.textSecondary }}>
                        Role
                      </th>
                      <th className="p-3 text-left text-xs font-medium" style={{ color: colors.textSecondary }}>
                        Status
                      </th>
                      <th 
                        className="p-3 text-left text-xs font-medium cursor-pointer hover:bg-opacity-50 transition-colors"
                        onClick={() => handleSort('lastActive')}
                        style={{ color: colors.textSecondary }}
                      >
                        <div className="flex items-center gap-1">
                          Last Active
                          {sortField === 'lastActive' && (
                            sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />
                          )}
                        </div>
                      </th>
                      <th className="p-3 text-left text-xs font-medium" style={{ color: colors.textSecondary }}>
                        Security Score
                      </th>
                      <th className="p-3 text-left text-xs font-medium" style={{ color: colors.textSecondary }}>
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentUsers.map(user => (
                      <tr 
                        key={user.id}
                        className="border-t hover-lift transition-colors"
                        style={{ 
                          borderColor: colors.border,
                          backgroundColor: selectedUsers.includes(user.id) ? colors.selected : 'transparent'
                        }}
                      >
                        <td className="p-3">
                          <input
                            type="checkbox"
                            checked={selectedUsers.includes(user.id)}
                            onChange={() => handleSelectUser(user.id)}
                            className="rounded border-gray-300"
                            style={{ borderColor: colors.border }}
                          />
                        </td>
                        <td className="p-3">
                          <div className="flex items-center gap-3">
                            <div 
                              className="w-8 h-8 rounded-full flex items-center justify-center text-white font-medium text-sm"
                              style={{ backgroundColor: user.avatarColor }}
                            >
                              {user.fullName.split(' ').map(n => n[0]).join('')}
                            </div>
                            <div className="min-w-0">
                              <div className="text-sm font-semibold truncate" style={{ color: colors.text }}>
                                {user.fullName}
                              </div>
                              <div className="text-xs truncate" style={{ color: colors.textSecondary }}>
                                @{user.username}
                              </div>
                            </div>
                          </div>
                        </td>
                        <td className="p-3">
                          <div className="text-sm truncate" style={{ color: colors.text }}>
                            {user.email}
                          </div>
                        </td>
                        <td className="p-3">
                          <div 
                            className="px-2 py-1 rounded-full text-xs font-medium w-fit"
                            style={{ 
                              backgroundColor: `${roleColors[user.role]}20`,
                              color: roleColors[user.role]
                            }}
                          >
                            {user.role}
                          </div>
                        </td>
                        <td className="p-3">
                          <div 
                            className="px-2 py-1 rounded-full text-xs font-medium w-fit"
                            style={{ 
                              backgroundColor: `${statusColors[user.status]}20`,
                              color: statusColors[user.status]
                            }}
                          >
                            {user.status}
                          </div>
                        </td>
                        <td className="p-3">
                          <div className="text-sm" style={{ color: colors.text }}>
                            {new Date(user.lastActive).toLocaleDateString()}
                          </div>
                        </td>
                        <td className="p-3">
                          <div className="flex items-center gap-1">
                            <div className="text-sm font-medium" style={{ color: colors.text }}>
                              {user.securityScore}
                            </div>
                            <div className="w-16 h-1 rounded-full bg-gray-200">
                              <div 
                                className="h-full rounded-full"
                                style={{ 
                                  width: `${user.securityScore}%`,
                                  backgroundColor: user.securityScore >= 80 ? colors.success : 
                                                 user.securityScore >= 60 ? colors.warning : colors.error
                                }}
                              />
                            </div>
                          </div>
                        </td>
                        <td className="p-3">
                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => handleViewUserDetails(user)}
                              className="p-1.5 rounded hover:bg-opacity-50 transition-colors"
                              style={{ backgroundColor: colors.hover }}
                              title="View Details"
                            >
                              <Eye size={14} style={{ color: colors.textSecondary }} />
                            </button>
                            <button
                              onClick={() => handleEditUser(user)}
                              className="p-1.5 rounded hover:bg-opacity-50 transition-colors"
                              style={{ backgroundColor: colors.hover }}
                              title="Edit User"
                            >
                              <Edit size={14} style={{ color: colors.textSecondary }} />
                            </button>
                            <button
                              onClick={() => handleDeleteUser(user)}
                              className="p-1.5 rounded hover:bg-opacity-50 transition-colors"
                              style={{ backgroundColor: colors.hover }}
                              title="Delete User"
                            >
                              <Trash2 size={14} style={{ color: colors.error }} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              <Pagination />
            </div>

          </div>
        </div>
      </div>
    </div>
  );
};

export default UserManagement;