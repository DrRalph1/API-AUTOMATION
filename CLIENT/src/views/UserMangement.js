import React, { useState, useEffect, useRef, useCallback } from 'react';
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
  CheckCircle as CheckCircleIcon, XCircle as XCircleIcon, Loader,
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

// Import the API controllers
import {
  getUsersList,
  getUserDetails,
  createUser,
  updateUser,
  deleteUser,
  bulkUserOperation,
  resetUserPassword,
  getUserStatistics,
  searchUsers,
  importUsers,
  exportUsers,
  getUserActivity,
  updateUserStatus,
  getRolesAndPermissions,
  validateUserData,
  handleUserManagementResponse,
  extractUsersList,
  extractUserDetails,
  extractCreateUserResults,
  extractUpdateUserResults,
  extractDeleteUserResults,
  extractBulkOperationResults,
  extractResetPasswordResults,
  extractUserStatistics as extractStats,
  extractSearchUsersResults,
  extractImportUsersResults,
  extractExportUsersResults,
  extractUserActivityResults,
  extractUpdateStatusResults,
  extractRolesAndPermissions as extractRoles,
  extractValidationResults,
  getUserStatusDisplayName,
  getStatusColor,
  getRoleColor,
  getSecurityScoreColor,
  formatDateForDisplay
} from '@/controllers/UserManagementController.js';

// Import UserRoleController methods
import {
  getAllRoles
} from '@/controllers/UserRoleController.js';

// Custom debounce hook
const useDebounce = (callback, delay) => {
  const timeoutRef = useRef(null);

  const debouncedFunction = useCallback((...args) => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }

    timeoutRef.current = setTimeout(() => {
      callback(...args);
    }, delay);
  }, [callback, delay]);

  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, []);

  return debouncedFunction;
};

const UserManagement = ({ theme, isDark, customTheme, toggleTheme, navigateTo, setActiveTab, authToken }) => {
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedRole, setSelectedRole] = useState('all');
  const [selectedStatus, setSelectedStatus] = useState('all');
  const [currentPage, setCurrentPage] = useState(1);
  const [usersPerPage, setUsersPerPage] = useState(7);
  const [sortField, setSortField] = useState('lastActive');
  const [sortDirection, setSortDirection] = useState('desc');
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [showFilters, setShowFilters] = useState(false);
  
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [rolesLoading, setRolesLoading] = useState(false);
  const [rolesError, setRolesError] = useState(false);
  
  const [totalItems, setTotalItems] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
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
  
  const [modalStack, setModalStack] = useState([]);
  const [openingModalForUserId, setOpeningModalForUserId] = useState(null);
  const [isOpeningModal, setIsOpeningModal] = useState(false);
  const pendingRequestRef = useRef(null);
  const isInitialMountRef = useRef(true);

  const formatLastActive = (lastActiveDate, totalLogins) => {
    if (totalLogins === 0 && !lastActiveDate) return 'Never logged in';
    if (!lastActiveDate) return 'Never logged in';
    const date = new Date(lastActiveDate);
    if (isNaN(date.getTime())) return 'Never logged in';
    return formatDateForDisplay(lastActiveDate, false);
  };

  // In the main component, update the loadRoles function
const loadRoles = async (showLoading = false) => {
  const authHeader = authToken;
  if (!authHeader) {
    console.warn('No auth token available for loading roles');
    setRolesError(true);
    return;
  }

  if (showLoading) setRolesLoading(true);
  setRolesError(false);
  
  try {
    const response = await getAllRoles(authHeader, { page: 0, size: 100 });
    
    if (response && response.responseCode === 200 && response.data) {
      const content = response.data.content || [];
      
      // Enhanced deduplication
      const uniqueRolesMap = new Map();
      content.forEach(role => {
        const roleId = role.roleId;
        if (!uniqueRolesMap.has(roleId)) {
          uniqueRolesMap.set(roleId, {
            id: role.roleId,
            roleId: role.roleId,
            roleName: role.roleName,
            description: role.description || '',
            roleCode: role.roleName?.replace(/\s+/g, '_').toUpperCase() || ''
          });
        }
      });
      
      let mappedRoles = Array.from(uniqueRolesMap.values());
      
      // Also deduplicate by name (case-insensitive)
      const uniqueByNameMap = new Map();
      mappedRoles.forEach(role => {
        const nameLower = role.roleName.toLowerCase();
        if (!uniqueByNameMap.has(nameLower)) {
          uniqueByNameMap.set(nameLower, role);
        }
      });
      
      mappedRoles = Array.from(uniqueByNameMap.values());
      
      // Sort alphabetically
      mappedRoles.sort((a, b) => a.roleName.localeCompare(b.roleName));
      
      console.log('Loaded unique roles:', mappedRoles);
      setRoles(mappedRoles);
      
      if (mappedRoles.length === 0) {
        setRolesError(true);
      }
    } else {
      console.error('Invalid API response structure:', response);
      setRolesError(true);
      setRoles([]);
    }
  } catch (error) {
    console.error('Error loading roles:', error);
    setRolesError(true);
    setRoles([]);
  } finally {
    if (showLoading) setRolesLoading(false);
  }
};

  const getRoleDisplayName = (roleId) => {
    if (!roleId) return 'Unknown';
    if (roleId === 'all') return 'All Roles';
    const role = roles.find(r => r.id === roleId || r.roleId === roleId);
    return role ? role.roleName : roleId;
  };

  const getRoleColorFromId = (roleId) => {
    if (!roleId || roleId === 'all') return '#6B7280';
    return getRoleColor(roleId);
  };

  // Load users with deduplication
  const loadUsers = async (filters = {}) => {
    if (!authToken) {
      showToast('error', 'Authentication required. Please log in.');
      return;
    }

    setLoading(true);
    try {
      const pageToSend = filters.page !== undefined ? filters.page : currentPage;
      
      const response = await getUsersList(authToken, {
        searchQuery: filters.searchQuery !== undefined ? filters.searchQuery : searchQuery,
        roleFilter: filters.roleFilter !== undefined ? filters.roleFilter : selectedRole,
        statusFilter: filters.statusFilter !== undefined ? filters.statusFilter : selectedStatus,
        sortField: filters.sortField !== undefined ? filters.sortField : sortField,
        sortDirection: filters.sortDirection !== undefined ? filters.sortDirection : sortDirection,
        page: pageToSend,
        pageSize: filters.pageSize !== undefined ? filters.pageSize : usersPerPage
      });
      
      const processedResponse = handleUserManagementResponse(response);
      let userList = extractUsersList(processedResponse);
      
      // Deduplicate users
      const uniqueUsersMap = new Map();
      (userList || []).forEach(user => {
        if (user.id && !uniqueUsersMap.has(user.id)) {
          uniqueUsersMap.set(user.id, user);
        }
      });
      userList = Array.from(uniqueUsersMap.values());
      
      const usersData = processedResponse?.data || {};
      const totalElements = usersData.total || userList.length;
      const totalPagesFromApi = usersData.totalPages || Math.ceil(totalElements / usersPerPage);
      const currentPageFromApi = usersData.page || pageToSend;
      
      setTotalItems(totalElements);
      setTotalPages(totalPagesFromApi);
      
      const mappedUserList = userList.map(user => ({
        ...user,
        roleDisplayName: getRoleDisplayName(user.role || user.roleId),
        roleDisplayColor: getRoleColorFromId(user.role || user.roleId)
      }));
      
      setUsers(mappedUserList);
      
    } catch (error) {
      console.error('Error loading users:', error);
      showToast('error', error.message || 'Failed to load users');
      setUsers([]);
    } finally {
      setLoading(false);
    }
  };

  const loadStatistics = async () => {
    if (!authToken) return;
    try {
      const response = await getUserStatistics(authToken);
      const processedResponse = handleUserManagementResponse(response);
      const statistics = extractStats(processedResponse);
      setStats(statistics || {
        totalUsers: 0,
        activeUsers: 0,
        admins: 0,
        developers: 0,
        pendingUsers: 0,
        suspendedUsers: 0,
        mfaEnabled: 0,
        avgSecurityScore: 0
      });
    } catch (error) {
      console.error('Error loading statistics:', error);
    }
  };

  const loadUserDetails = async (userId) => {
    if (!authToken) return null;
    setLoading(true);
    try {
      const response = await getUserDetails(authToken, userId);
      const processedResponse = handleUserManagementResponse(response);
      const userDetails = extractUserDetails(processedResponse);
      if (userDetails) {
        userDetails.roleDisplayName = getRoleDisplayName(userDetails.roleId || userDetails.role);
        userDetails.roleDisplayColor = getRoleColorFromId(userDetails.roleId || userDetails.role);
      }
      return userDetails;
    } catch (error) {
      console.error('Error loading user details:', error);
      return null;
    } finally {
      setLoading(false);
    }
  };

  // FIXED: Handle update user - use 'role' field name (not 'roleId') for your backend
const handleUpdateUser = async (userId, userData) => {
  if (!authToken) {
    showToast('error', 'Authentication required. Please log in.');
    return false;
  }

  setLoading(true);
  try {
    // IMPORTANT: Your backend expects 'role' (role name string), not 'roleId' (UUID)
    // Find the role name from the role ID
    let roleName = null;
    if (userData.roleId) {
      const selectedRole = roles.find(r => r.id === userData.roleId || r.roleId === userData.roleId);
      roleName = selectedRole?.roleName || userData.roleId;
    }
    
    const updatePayload = {
      fullName: userData.fullName,
      role: roleName,  // Send role name, not roleId
      status: userData.status,
      department: userData.department,
      location: userData.location,
      mfaEnabled: userData.mfaEnabled,
      phoneNumber: userData.phoneNumber,
      emailVerified: userData.emailVerified,
      phoneVerified: userData.phoneVerified,
      tags: userData.tags || []
    };
    
    console.log('Updating user with payload:', updatePayload);
    
    const response = await updateUser(authToken, userId, updatePayload);
    const processedResponse = handleUserManagementResponse(response);
    const result = extractUpdateUserResults(processedResponse);
    
    if (result.success) {
      showToast('success', result.message || 'User updated successfully');
      await loadUsers();
      await loadStatistics();
      return true;
    } else {
      showToast('error', result.message || 'Failed to update user');
      return false;
    }
  } catch (error) {
    console.error('Error updating user:', error);
    showToast('error', error.message || 'Failed to update user');
    return false;
  } finally {
    setLoading(false);
  }
};


// Add this function after handleResetPassword (around line 530)
const handleBulkAction = async (action) => {
  if (!authToken) return;
  if (selectedUsers.length === 0) {
    showToast('warning', 'No users selected');
    return;
  }
  
  const actionMessages = {
    activate: 'activate',
    suspend: 'suspend',
    delete: 'delete'
  };
  
  const confirmMessage = `Are you sure you want to ${actionMessages[action]} ${selectedUsers.length} user(s)?`;
  if (!window.confirm(confirmMessage)) return;
  
  setLoading(true);
  try {
    const response = await bulkUserOperation(authToken, {
      userIds: selectedUsers,
      action: action
    });
    const processedResponse = handleUserManagementResponse(response);
    const result = extractBulkOperationResults(processedResponse);
    
    if (result.success) {
      showToast('success', `${selectedUsers.length} user(s) ${actionMessages[action]}d successfully`);
      setSelectedUsers([]);
      await loadUsers();
      await loadStatistics();
    } else {
      showToast('error', result.message || `Failed to ${action} users`);
    }
  } catch (error) {
    console.error('Error in bulk action:', error);
    showToast('error', error.message || `Failed to ${action} users`);
  } finally {
    setLoading(false);
  }
};


// Add after handleBulkAction
const handleImportUsersFromFile = async (importData) => {
  if (!authToken) return false;
  setLoading(true);
  try {
    const response = await importUsers(authToken, importData);
    const processedResponse = handleUserManagementResponse(response);
    const result = extractImportUsersResults(processedResponse);
    
    if (result.success) {
      showToast('success', result.message || `${result.importedCount || 0} users imported successfully`);
      await loadUsers();
      await loadStatistics();
      return true;
    } else {
      showToast('error', result.message || 'Failed to import users');
      return false;
    }
  } catch (error) {
    console.error('Error importing users:', error);
    showToast('error', error.message || 'Failed to import users');
    return false;
  } finally {
    setLoading(false);
  }
};

const handleExportData = async (exportData) => {
  if (!authToken) return;
  setLoading(true);
  try {
    const response = await exportUsers(authToken, exportData);
    const processedResponse = handleUserManagementResponse(response);
    const result = extractExportUsersResults(processedResponse);
    
    if (result.success && result.data) {
      // Create download link
      const blob = new Blob([result.data], { type: getContentType(exportData.format) });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `users_export.${exportData.format}`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      showToast('success', 'Users exported successfully');
    } else {
      showToast('error', result.message || 'Failed to export users');
    }
  } catch (error) {
    console.error('Error exporting users:', error);
    showToast('error', error.message || 'Failed to export users');
  } finally {
    setLoading(false);
  }
};

const getContentType = (format) => {
  switch(format) {
    case 'csv': return 'text/csv';
    case 'json': return 'application/json';
    case 'excel': return 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
    case 'pdf': return 'application/pdf';
    default: return 'text/plain';
  }
};

  // Handle create user - FIXED to send roleId (UUID) not role name
const handleCreateUser = async (userData) => {
  if (!authToken) {
    showToast('error', 'Authentication required. Please log in.');
    return false;
  }

  setLoading(true);
  try {
    // IMPORTANT: Your backend expects 'roleId' (UUID), not 'role' (name)
    // Find the role by ID or name and get the actual role ID
    let roleId = null;
    if (userData.roleId) {
      // First check if it's already a UUID
      const isUUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(userData.roleId);
      
      if (isUUID) {
        // It's already a UUID, use it directly
        roleId = userData.roleId;
      } else {
        // It's a role name, find the corresponding role ID
        const selectedRole = roles.find(r => 
          r.roleName.toLowerCase() === userData.roleId.toLowerCase()
        );
        roleId = selectedRole?.roleId || selectedRole?.id;
      }
    }
    
    // If still no roleId, try to find by the role name
    if (!roleId && userData.role) {
      const selectedRole = roles.find(r => 
        r.roleName.toLowerCase() === userData.role.toLowerCase()
      );
      roleId = selectedRole?.roleId || selectedRole?.id;
    }
    
    console.log('Found role ID:', roleId);
    
    const createPayload = {
      username: userData.username,
      email: userData.email,
      phoneNumber: userData.phoneNumber,
      fullName: userData.fullName,
      roleId: roleId,  // Send role ID (UUID), not role name
      department: userData.department || 'General',
      location: userData.location || 'Not specified',
      mfaEnabled: userData.mfaEnabled || false,
      status: userData.status || 'pending'
    };
    
    console.log('Creating user with payload:', createPayload);
    
    const response = await createUser(authToken, createPayload);
    const processedResponse = handleUserManagementResponse(response);
    const result = extractCreateUserResults(processedResponse);
    
    if (result.success) {
      showToast('success', result.message || 'User created successfully');
      setCurrentPage(1);
      await loadUsers();
      await loadStatistics();
      return true;
    } else {
      showToast('error', result.message || 'Failed to create user');
      return false;
    }
  } catch (error) {
    console.error('Error creating user:', error);
    // Check for specific error messages
    if (error.message?.includes('already exists')) {
      showToast('error', 'User with this email or username already exists');
    } else if (error.message?.includes('id must not be null')) {
      showToast('error', 'Invalid role selected. Please try again.');
    } else {
      showToast('error', error.message || 'Failed to create user');
    }
    return false;
  } finally {
    setLoading(false);
  }
};

  const handleDeleteUser = async (user) => {
    if (!window.confirm(`Are you sure you want to delete ${user.fullName || user.username}?`)) return;
    if (!authToken) return;

    setLoading(true);
    try {
      const response = await deleteUser(authToken, user.id);
      const processedResponse = handleUserManagementResponse(response);
      const result = extractDeleteUserResults(processedResponse);
      
      if (result.success) {
        showToast('success', result.message || 'User deleted successfully');
        setSelectedUsers(prev => prev.filter(id => id !== user.id));
        if (users.length === 1 && currentPage > 1) {
          setCurrentPage(currentPage - 1);
        } else {
          await loadUsers();
        }
        await loadStatistics();
      } else {
        showToast('error', result.message || 'Failed to delete user');
      }
    } catch (error) {
      console.error('Error deleting user:', error);
      showToast('error', error.message || 'Failed to delete user');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (user, resetData) => {
    if (!authToken) return false;
    setLoading(true);
    try {
      const response = await resetUserPassword(authToken, user.id, resetData);
      const processedResponse = handleUserManagementResponse(response);
      const result = extractResetPasswordResults(processedResponse);
      if (result.success) {
        showToast('success', result.message || 'Password reset successful');
        return true;
      } else {
        showToast('error', result.message || 'Failed to reset password');
        return false;
      }
    } catch (error) {
      console.error('Error resetting password:', error);
      showToast('error', error.message || 'Failed to reset password');
      return false;
    } finally {
      setLoading(false);
    }
  };

  const handleSearchUsers = async (searchData) => {
    if (!authToken) return;
    setLoading(true);
    try {
      const response = await searchUsers(authToken, searchData);
      const processedResponse = handleUserManagementResponse(response);
      const result = extractSearchUsersResults(processedResponse);
      
      const mappedResults = (result.results || []).map(user => ({
        ...user,
        roleDisplayName: getRoleDisplayName(user.role || user.roleId),
        roleDisplayColor: getRoleColorFromId(user.role || user.roleId)
      }));
      
      setUsers(mappedResults);
      setTotalItems(mappedResults.length);
      setTotalPages(1);
      setCurrentPage(1);
    } catch (error) {
      console.error('Error searching users:', error);
      showToast('error', error.message || 'Failed to search users');
    } finally {
      setLoading(false);
    }
  };

  const showToast = (type, message) => {
    alert(`${type.toUpperCase()}: ${message}`);
  };

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
    inputBg: 'rgb(41 53 72 / 19%)',
    inputBorder: 'rgb(51 65 85 / 19%)',
    tableHeader: 'rgb(41 53 72 / 19%)',
    tableRow: 'rgb(41 53 72 / 19%)',
    tableRowHover: 'rgb(45 46 72 / 33%)',
    modalBg: 'rgb(41 53 72 / 19%)',
    modalBorder: 'rgb(51 65 85 / 19%)',
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
    inputBg: '#ffffff',
    inputBorder: '#e2e8f0',
    tableHeader: '#f8fafc',
    tableRow: '#ffffff',
    tableRowHover: '#f8fafc',
    modalBg: '#ffffff',
    modalBorder: '#e2e8f0',
  };

  const getRoleColorStyle = (roleId) => {
    const color = getRoleColorFromId(roleId) || colors.textSecondary;
    return { backgroundColor: `${color}20`, color: color };
  };

  const getStatusColorStyle = (status) => {
    const color = getStatusColor(status) || colors.textSecondary;
    return { backgroundColor: `${color}20`, color: color };
  };

  const getSecurityColor = (score) => {
    return getSecurityScoreColor(score) || colors.textSecondary;
  };

  // Modal management
  const openModal = (type, data) => {
    setModalStack(prev => [...prev, { type, data }]);
  };

  const closeModal = () => {
    setModalStack(prev => prev.slice(0, -1));
    setTimeout(() => {
      setOpeningModalForUserId(null);
      setIsOpeningModal(false);
      pendingRequestRef.current = null;
    }, 100);
  };

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedUsers(users.map(user => user.id));
    } else {
      setSelectedUsers([]);
    }
  };

  const handleSelectUser = (userId) => {
    setSelectedUsers(prev => 
      prev.includes(userId) ? prev.filter(id => id !== userId) : [...prev, userId]
    );
  };

  const handleSort = (field) => {
    if (sortField === field) {
      const newDirection = sortDirection === 'asc' ? 'desc' : 'asc';
      setSortDirection(newDirection);
      loadUsers({ sortField: field, sortDirection: newDirection, page: 0 });
    } else {
      setSortField(field);
      setSortDirection('asc');
      loadUsers({ sortField: field, sortDirection: 'asc', page: 0 });
    }
    setCurrentPage(1);
  };

  const handleSearchChange = (value) => {
    setSearchQuery(value);
    setCurrentPage(1);
    clearTimeout(window.searchTimeout);
    window.searchTimeout = setTimeout(() => {
      if (value.trim()) {
        handleSearchUsers({ query: value });
      } else {
        loadUsers({ searchQuery: '', page: 0 });
      }
    }, 500);
  };

  const handleRoleFilterChange = (value) => {
    setSelectedRole(value);
    setCurrentPage(1);
    loadUsers({ roleFilter: value, page: 0 });
  };

  const handleStatusFilterChange = (value) => {
    setSelectedStatus(value);
    setCurrentPage(1);
    loadUsers({ statusFilter: value, page: 0 });
  };

  const goToPreviousPage = () => {
    if (currentPage > 1) setCurrentPage(currentPage - 1);
  };

  const goToNextPage = () => {
    if (currentPage < totalPages) setCurrentPage(currentPage + 1);
  };

  const debouncedViewUserDetails = useDebounce(async (user) => {
    if (!user || !user.id) return;
    if (openingModalForUserId === user.id || isOpeningModal) return;
    if (pendingRequestRef.current === user.id) return;

    pendingRequestRef.current = user.id;
    setOpeningModalForUserId(user.id);
    setIsOpeningModal(true);

    try {
      const userDetails = await loadUserDetails(user.id);
      if (userDetails) openModal('userDetails', userDetails);
    } catch (error) {
      console.error('Error loading user details:', error);
      showToast('error', error.message || 'Failed to load user details');
    } finally {
      setTimeout(() => {
        setOpeningModalForUserId(null);
        setIsOpeningModal(false);
        pendingRequestRef.current = null;
      }, 500);
    }
  }, 300);

  const handleViewUserDetails = (user) => {
    debouncedViewUserDetails(user);
  };

  const handleRowClick = (user, e) => {
    if (e.target.closest('button') || e.target.closest('input') || e.target.closest('a')) return;
    handleViewUserDetails(user);
  };

  const handleEditUser = (user) => {
    openModal('editUser', user);
  };

  const handleImportUsers = () => {
    openModal('importUsers', {});
  };

  const handleExportClick = () => {
    openModal('exportUsers', {});
  };

  useEffect(() => {
    if (authToken) loadRoles();
  }, [authToken]);

  useEffect(() => {
    if (authToken) {
      if (isInitialMountRef.current) {
        isInitialMountRef.current = false;
        loadUsers();
        loadStatistics();
      } else {
        loadUsers({ page: currentPage });
      }
    }
  }, [authToken, currentPage, usersPerPage, sortField, sortDirection, selectedRole, selectedStatus]);

  const getDisplayRange = () => {
    const start = ((currentPage - 1) * usersPerPage) + 1;
    const end = Math.min(currentPage * usersPerPage, totalItems);
    return { start, end };
  };

  // Mobile Modal Component
  const MobileModal = ({ children, title, onClose, showBackButton = false, onBack }) => {
    const modalCount = modalStack.length;
    const zIndex = 1000 + (modalCount * 10);
    
    return (
      <div 
        className="fixed inset-0 bg-black bg-opacity-50 backdrop-blur-sm flex items-center justify-center z-50 p-2 sm:p-4"
        style={{ zIndex: zIndex - 5 }}
        onClick={onClose}
      >
        <div 
          className="border rounded-xl w-[55rem] max-h-[90vh] overflow-auto animate-fade-in"
          onClick={(e) => e.stopPropagation()}
          style={{ backgroundColor: colors.bg, borderColor: colors.modalBorder, zIndex: zIndex }}
        >
          <div className="sticky top-0 p-3 sm:p-4 border-b flex items-center justify-between backdrop-blur-sm" style={{ borderColor: colors.border, backgroundColor: colors.modalBg }}>
            <div className="flex items-center gap-2">
              {showBackButton && (
                <button onClick={onBack} className="p-1 sm:p-1.5 rounded hover:bg-opacity-50 transition-colors shrink-0" style={{ backgroundColor: colors.hover }}>
                  <ChevronLeft size={16} style={{ color: colors.text }} />
                </button>
              )}
              <h3 className="text-base sm:text-lg font-semibold truncate" style={{ color: colors.text }}>{title}</h3>
            </div>
            <button onClick={onClose} className="p-1 sm:p-1.5 rounded hover:bg-opacity-50 transition-colors shrink-0" style={{ backgroundColor: colors.hover }}>
              <X size={18} style={{ color: colors.text }} />
            </button>
          </div>
          <div className="p-3 sm:p-4 overflow-auto">{children}</div>
        </div>
      </div>
    );
  };

  // Edit User Modal - COMPLETE FIXED VERSION with validationResult
const EditUserModal = ({ data: user }) => {
  const [formData, setFormData] = useState({
    username: user?.username || '',
    email: user?.email || '',
    phoneNumber: user?.phoneNumber || '',
    fullName: user?.fullName || '',
    roleId: user?.roleId || user?.role || '',
    roleName: user?.role || '',
    status: user?.status || 'pending',
    department: user?.department || '',
    location: user?.location || '',
    mfaEnabled: user?.mfaEnabled || false,
    emailVerified: user?.emailVerified || false,
    phoneVerified: user?.phoneVerified || false,
    tags: user?.tags || []
  });

  const [localRoles, setLocalRoles] = useState([]);
  const [rolesLoading, setRolesLoading] = useState(false);
  const [rolesError, setRolesError] = useState(false);
  const [validationResult, setValidationResult] = useState(null); // ADD THIS LINE
  const rolesFetchedRef = useRef(false);

  // Fetch roles when modal opens - WITH ENHANCED DEDUPLICATION
  useEffect(() => {
    // Prevent multiple fetches
    if (rolesFetchedRef.current) return;
    
    const fetchRoles = async () => {
      if (!authToken) {
        setRolesError(true);
        return;
      }
      
      setRolesLoading(true);
      setRolesError(false);
      
      try {
        const response = await getAllRoles(authToken, { page: 0, size: 100 });
        
        console.log('Raw roles response:', response);
        
        if (response && response.responseCode === 200 && response.data) {
          let content = response.data.content || [];
          
          // METHOD 1: Deduplicate by roleId (UUID)
          const uniqueRolesMap = new Map();
          content.forEach(role => {
            const roleId = role.roleId;
            if (!uniqueRolesMap.has(roleId)) {
              uniqueRolesMap.set(roleId, {
                id: role.roleId,
                roleId: role.roleId,
                roleName: role.roleName,
                description: role.description || ''
              });
            }
          });
          
          let mappedRoles = Array.from(uniqueRolesMap.values());
          
          // METHOD 2: Also deduplicate by roleName (case-insensitive)
          const uniqueByNameMap = new Map();
          mappedRoles.forEach(role => {
            const roleNameLower = role.roleName.toLowerCase();
            if (!uniqueByNameMap.has(roleNameLower)) {
              uniqueByNameMap.set(roleNameLower, role);
            }
          });
          
          mappedRoles = Array.from(uniqueByNameMap.values());
          
          // METHOD 3: Sort roles alphabetically for consistent display
          mappedRoles.sort((a, b) => a.roleName.localeCompare(b.roleName));
          
          console.log('Deduplicated roles:', mappedRoles);
          setLocalRoles(mappedRoles);
          
          // CRITICAL FIX: Set the user's role after roles load
          // The user's role could be either an ID or a name
          const userRoleValue = user?.roleId || user?.role;
          console.log('User role value from props:', userRoleValue);
          
          if (userRoleValue && userRoleValue !== 'new') {
            // Try to find the role by ID first (UUID format)
            let foundRole = mappedRoles.find(r => r.roleId === userRoleValue);
            
            // If not found by ID, try by name (case-insensitive)
            if (!foundRole) {
              foundRole = mappedRoles.find(r => 
                r.roleName.toLowerCase() === userRoleValue.toLowerCase()
              );
            }
            
            // If still not found, try by matching the role name from the user object
            if (!foundRole && user?.role) {
              foundRole = mappedRoles.find(r => 
                r.roleName.toLowerCase() === user.role.toLowerCase()
              );
            }
            
            if (foundRole) {
              console.log('Found matching role:', foundRole);
              setFormData(prev => ({ 
                ...prev, 
                roleId: foundRole.roleId,
                roleName: foundRole.roleName
              }));
            } else {
              console.log('No matching role found for:', userRoleValue);
              // If no match found, keep the original value - it might be a role name
              setFormData(prev => ({ 
                ...prev, 
                roleId: userRoleValue,
                roleName: userRoleValue
              }));
            }
          }
          
          if (mappedRoles.length === 0) {
            setRolesError(true);
          }
          
          rolesFetchedRef.current = true;
        } else {
          console.error('Invalid API response:', response);
          setRolesError(true);
          setLocalRoles([]);
        }
      } catch (error) {
        console.error('Error loading roles:', error);
        setRolesError(true);
        setLocalRoles([]);
      } finally {
        setRolesLoading(false);
      }
    };
    
    fetchRoles();
    
    // Cleanup on unmount
    return () => {
      rolesFetchedRef.current = false;
    };
  }, [authToken, user]);

  // Also deduplicate in the render method as a safety net
  const getUniqueRolesForRender = useCallback(() => {
    const seen = new Set();
    return localRoles.filter(role => {
      const key = `${role.roleId}-${role.roleName.toLowerCase()}`;
      if (seen.has(key)) return false;
      seen.add(key);
      return true;
    });
  }, [localRoles]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    console.log('Submitting form with roleId:', formData.roleId);
    console.log('Selected role name:', localRoles.find(r => r.roleId === formData.roleId)?.roleName);
    
    if (!formData.roleId) {
      showToast('error', 'Please select an access type');
      return;
    }
    
    if (user?.id === 'new') {
      // Create new user
      const createData = {
        username: formData.username,
        email: formData.email,
        phoneNumber: formData.phoneNumber,
        fullName: formData.fullName,
        roleId: formData.roleId,
        department: formData.department || 'General',
        location: formData.location || 'Not specified',
        mfaEnabled: formData.mfaEnabled,
        status: formData.status || 'pending'
      };
      
      console.log('Creating user with data:', createData);
      const success = await handleCreateUser(createData);
      if (success) closeModal();
    } else {
      // Update existing user
      const updateData = {
        fullName: formData.fullName,
        roleId: formData.roleId,
        status: formData.status,
        department: formData.department,
        location: formData.location,
        mfaEnabled: formData.mfaEnabled,
        phoneNumber: formData.phoneNumber,
        emailVerified: formData.emailVerified,
        phoneVerified: formData.phoneVerified,
        tags: formData.tags
      };
      
      console.log('Updating user with data:', updateData);
      const success = await handleUpdateUser(user.id, updateData);
      if (success) closeModal();
    }
  };

  const handleTagInput = (e) => {
    if (e.key === 'Enter' && e.target.value.trim()) {
      const newTag = e.target.value.trim();
      if (!formData.tags.includes(newTag)) {
        setFormData(prev => ({
          ...prev,
          tags: [...prev.tags, newTag]
        }));
      }
      e.target.value = '';
    }
  };

  const removeTag = (tagToRemove) => {
    setFormData(prev => ({
      ...prev,
      tags: prev.tags.filter(tag => tag !== tagToRemove)
    }));
  };

  const uniqueRoles = getUniqueRolesForRender();
  
  // Get the display name for the selected role
  const getSelectedRoleDisplayName = () => {
    if (!formData.roleId) return '';
    const role = uniqueRoles.find(r => r.roleId === formData.roleId);
    if (role) return role.roleName;
    // If not found by ID, try by name
    const roleByName = uniqueRoles.find(r => r.roleName.toLowerCase() === formData.roleId.toLowerCase());
    return roleByName?.roleName || formData.roleId;
  };

  return (
    <MobileModal 
      title={user?.id === 'new' ? "Add New User" : "Edit User"} 
      onClose={closeModal}
      showBackButton={modalStack.length > 1}
      onBack={closeModal}
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {validationResult && !validationResult.valid && (
          <div className="p-3 rounded border" style={{ 
            borderColor: colors.error,
            backgroundColor: `${colors.error}20`
          }}>
            <div className="text-sm font-medium mb-1" style={{ color: colors.error }}>
              Validation Issues
            </div>
            <ul className="text-xs space-y-1">
              {validationResult.issues && validationResult.issues.map((issue, index) => (
                <li key={index} style={{ color: colors.textSecondary }}>
                  • {issue.field}: {issue.message}
                </li>
              ))}
            </ul>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
              Full Name *
            </label>
            <input
              type="text"
              value={formData.fullName}
              onChange={(e) => setFormData(prev => ({ ...prev, fullName: e.target.value }))}
              className="w-full px-3 py-2 rounded border text-sm"
              style={{ 
                backgroundColor: colors.inputBg,
                borderColor: colors.inputBorder,
                color: colors.text
              }}
              required
            />
          </div>
          
          <div>
            <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
              Username *
            </label>
            <input
              type="text"
              value={formData.username}
              onChange={(e) => setFormData(prev => ({ ...prev, username: e.target.value }))}
              className="w-full px-3 py-2 rounded border text-sm"
              style={{ 
                backgroundColor: colors.inputBg,
                borderColor: colors.inputBorder,
                color: colors.text
              }}
              required={user?.id === 'new'}
              disabled={user?.id !== 'new'}
            />
          </div>
          
          <div>
            <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
              Email *
            </label>
            <input
              type="email"
              value={formData.email}
              onChange={(e) => setFormData(prev => ({ ...prev, email: e.target.value }))}
              className="w-full px-3 py-2 rounded border text-sm"
              style={{ 
                backgroundColor: colors.inputBg,
                borderColor: colors.inputBorder,
                color: colors.text
              }}
              required={user?.id === 'new'}
              disabled={user?.id !== 'new'}
            />
          </div>
          
          <div>
            <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
              Access Type *
            </label>
            {rolesError ? (
              <div 
                className="w-full px-3 py-2 rounded border text-sm flex items-center justify-between"
                style={{ 
                  backgroundColor: `${colors.error}20`,
                  borderColor: colors.error,
                  color: colors.error
                }}
              >
                <span>Failed to load roles</span>
                <button
                  type="button"
                  onClick={() => {
                    setRolesError(false);
                    rolesFetchedRef.current = false;
                    setLocalRoles([]);
                    // Refetch roles
                    const refetch = async () => {
                      setRolesLoading(true);
                      try {
                        const response = await getAllRoles(authToken, { page: 0, size: 100 });
                        if (response && response.responseCode === 200 && response.data) {
                          let content = response.data.content || [];
                          const uniqueMap = new Map();
                          content.forEach(role => {
                            if (!uniqueMap.has(role.roleId)) {
                              uniqueMap.set(role.roleId, {
                                id: role.roleId,
                                roleId: role.roleId,
                                roleName: role.roleName,
                                description: role.description || ''
                              });
                            }
                          });
                          let roles = Array.from(uniqueMap.values());
                          roles.sort((a, b) => a.roleName.localeCompare(b.roleName));
                          setLocalRoles(roles);
                          setRolesError(false);
                        }
                      } catch (error) {
                        console.error('Error refetching roles:', error);
                      } finally {
                        setRolesLoading(false);
                      }
                    };
                    refetch();
                  }}
                  className="p-1 rounded hover:bg-opacity-50"
                  style={{ backgroundColor: `${colors.error}30` }}
                >
                  <RefreshCw size={14} className={rolesLoading ? 'animate-spin' : ''} />
                </button>
              </div>
            ) : (
              <>
                <select
                  key={`role-select-${formData.roleId || 'empty'}-${uniqueRoles.length}`}
                  value={formData.roleId || ''}
                  onChange={(e) => {
                    console.log('Selected role ID:', e.target.value);
                    setFormData(prev => ({ ...prev, roleId: e.target.value }));
                  }}
                  className="w-full px-3 py-2 rounded border text-sm uppercase"
                  style={{ 
                    backgroundColor: colors.bg,
                    borderColor: colors.inputBorder,
                    color: colors.text,
                    opacity: rolesLoading ? 0.7 : 1
                  }}
                  required
                  disabled={rolesLoading || uniqueRoles.length === 0}
                >
                  <option value="">
                    {rolesLoading ? 'Loading access types...' : 
                     uniqueRoles.length === 0 ? 'No access types available' : 'Select Access Type'}
                  </option>
                  {uniqueRoles.map((role, index) => (
                    <option 
                      key={`${role.roleId}-${index}`}
                      value={role.roleId}
                    >
                      {role.roleName}
                    </option>
                  ))}
                </select>
                {formData.roleId && getSelectedRoleDisplayName() && (
                  <div className="text-xs mt-1" style={{ color: colors.textTertiary }}>
                    Selected: {getSelectedRoleDisplayName()}
                  </div>
                )}
              </>
            )}
            {rolesLoading && (
              <div className="flex items-center gap-2 text-xs mt-1" style={{ color: colors.textSecondary }}>
                <RefreshCw size={12} className="animate-spin" />
                <span>Loading roles from server...</span>
              </div>
            )}
            {!rolesLoading && !rolesError && uniqueRoles.length > 0 && (
              <div className="text-xs mt-1" style={{ color: colors.textTertiary }}>
                {uniqueRoles.length} access type(s) available
              </div>
            )}
          </div>
          
          <div>
            <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
              Phone Number
            </label>
            <input
              type="text"
              value={formData.phoneNumber}
              onChange={(e) => setFormData(prev => ({ ...prev, phoneNumber: e.target.value }))}
              className="w-full px-3 py-2 rounded border text-sm"
              style={{ 
                backgroundColor: colors.inputBg,
                borderColor: colors.inputBorder,
                color: colors.text
              }}
            />
          </div>
          
          <div>
            <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
              Staff Id
            </label>
            <input
              type="text"
              value={formData.department}
              onChange={(e) => setFormData(prev => ({ ...prev, department: e.target.value }))}
              className="w-full px-3 py-2 rounded border text-sm"
              style={{ 
                backgroundColor: colors.inputBg,
                borderColor: colors.inputBorder,
                color: colors.text
              }}
            />
          </div>
          
          <div>
            <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
              Location
            </label>
            <input
              type="text"
              value={formData.location}
              onChange={(e) => setFormData(prev => ({ ...prev, location: e.target.value }))}
              className="w-full px-3 py-2 rounded border text-sm"
              style={{ 
                backgroundColor: colors.inputBg,
                borderColor: colors.inputBorder,
                color: colors.text
              }}
            />
          </div>
          
          <div>
            <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
              Status *
            </label>
            <select
              value={formData.status}
              onChange={(e) => setFormData(prev => ({ ...prev, status: e.target.value }))}
              className="w-full px-3 py-2 rounded border text-sm"
              style={{ 
                backgroundColor: colors.bg,
                borderColor: colors.inputBorder,
                color: colors.text
              }}
              required
            >
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
              <option value="pending">Pending</option>
              <option value="suspended">Suspended</option>
            </select>
          </div>
        </div>

        <div>
          <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
            Tags
          </label>
          <div className="flex flex-wrap gap-2 mb-2">
            {formData.tags.map((tag, index) => (
              <div 
                key={index}
                className="px-2 py-1 rounded flex items-center gap-1 text-xs"
                style={{ 
                  backgroundColor: colors.hover,
                  color: colors.textSecondary
                }}
              >
                {tag}
                <button
                  type="button"
                  onClick={() => removeTag(tag)}
                  className="p-0.5 rounded hover:bg-opacity-50 transition-colors"
                  style={{ backgroundColor: colors.hover }}
                >
                  <X size={10} />
                </button>
              </div>
            ))}
          </div>
          <input
            type="text"
            placeholder="Type tag and press Enter..."
            onKeyDown={handleTagInput}
            className="w-full px-3 py-2 rounded border text-sm"
            style={{ 
              backgroundColor: colors.inputBg,
              borderColor: colors.inputBorder,
              color: colors.text
            }}
          />
        </div>

        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="mfaEnabled"
              checked={formData.mfaEnabled}
              onChange={(e) => setFormData(prev => ({ ...prev, mfaEnabled: e.target.checked }))}
              className="rounded"
            />
            <label htmlFor="mfaEnabled" className="text-sm" style={{ color: colors.text }}>
              MFA Enabled
            </label>
          </div>
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="emailVerified"
              checked={formData.emailVerified}
              onChange={(e) => setFormData(prev => ({ ...prev, emailVerified: e.target.checked }))}
              className="rounded"
            />
            <label htmlFor="emailVerified" className="text-sm" style={{ color: colors.text }}>
              Email Verified
            </label>
          </div>
        </div>

        <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
          <div className="flex flex-col sm:flex-row gap-2">
            <button 
              type="submit"
              className="px-4 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift"
              style={{ 
                backgroundColor: colors.success,
                color: 'white'
              }}
              disabled={rolesLoading || uniqueRoles.length === 0}
            >
              {user?.id === 'new' ? 'Create User' : 'Update User'}
            </button>
            <button 
              type="button"
              onClick={closeModal}
              className="px-4 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift"
              style={{ 
                backgroundColor: colors.hover,
                color: colors.text
              }}
            >
              Cancel
            </button>
          </div>
        </div>
      </form>
    </MobileModal>
  );
};

  // User Details Modal
  const UserDetailsModal = ({ data: user }) => {
    const formatDate = (dateString) => formatDateForDisplay(dateString, true) || '';

    return (
      <MobileModal title="User Details" onClose={closeModal} showBackButton={modalStack.length > 1} onBack={closeModal}>
        <div className="space-y-6">
          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4 p-4 rounded-xl" style={{ backgroundColor: colors.card, border: `1px solid ${colors.border}` }}>
            <div className="w-16 h-16 rounded-full flex items-center justify-center text-white font-medium text-xl" style={{ backgroundColor: user?.avatarColor || colors.primary }}>
              {user?.fullName?.split(' ').map(n => n[0]).join('') || '??'}
            </div>
            <div className="flex-1 min-w-0">
              <h4 className="text-xl font-bold truncate" style={{ color: colors.text }}>{user?.fullName || 'Unknown User'}</h4>
              <div className="flex flex-wrap items-center gap-2 mt-1">
                <div className="flex items-center gap-1 text-sm">
                  <UserCircle size={14} style={{ color: colors.textSecondary }} />
                  <span style={{ color: colors.textSecondary }}>@{user?.username || 'unknown'}</span>
                </div>
                <div className="px-2 py-0.5 rounded-full text-xs font-medium uppercase" style={getRoleColorStyle(user?.roleId || user?.role)}>
                  {getRoleDisplayName(user?.roleId || user?.role)}
                </div>
                <div className="px-2 py-0.5 rounded-full text-xs font-medium" style={getStatusColorStyle(user?.status)}>
                  {getUserStatusDisplayName(user?.status)}
                </div>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-4">
              <h5 className="text-sm font-semibold mb-3 flex items-center gap-2" style={{ color: colors.text }}><UserCircle size={16} />Basic Information</h5>
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Mail size={14} style={{ color: colors.textSecondary }} />
                  <div className="flex-1"><div className="text-xs" style={{ color: colors.textSecondary }}>Email</div><div className="text-sm truncate" style={{ color: colors.text }}>{user?.email || 'No email'}</div></div>
                  {user?.emailVerified ? <CheckCircle size={14} style={{ color: colors.success }} /> : <XCircle size={14} style={{ color: colors.error }} />}
                </div>
                <div className="flex items-center gap-2">
                  <PhoneCall size={14} style={{ color: colors.textSecondary }} />
                  <div className="flex-1"><div className="text-xs" style={{ color: colors.textSecondary }}>Phone Number</div><div className="text-sm" style={{ color: colors.text }}>{user?.phoneNumber || 'Not specified'}</div></div>
                </div>
                <div className="flex items-center gap-2">
                  <Building size={14} style={{ color: colors.textSecondary }} />
                  <div className="flex-1"><div className="text-xs" style={{ color: colors.textSecondary }}>Department</div><div className="text-sm" style={{ color: colors.text }}>{user?.department || 'Not specified'}</div></div>
                </div>
                <div className="flex items-center gap-2">
                  <MapPin size={14} style={{ color: colors.textSecondary }} />
                  <div className="flex-1"><div className="text-xs" style={{ color: colors.textSecondary }}>Location</div><div className="text-sm" style={{ color: colors.text }}>{user?.location || 'Not specified'}</div></div>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <h5 className="text-sm font-semibold mb-3 flex items-center gap-2" style={{ color: colors.text }}><Activity size={16} />Activity</h5>
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Clock size={14} style={{ color: colors.textSecondary }} />
                  <div className="flex-1"><div className="text-xs" style={{ color: colors.textSecondary }}>Last Active</div><div className="text-sm" style={{ color: colors.text }}>{user?.lastActive ? formatDate(user.lastActive) : 'Never logged in'}</div></div>
                </div>
                <div className="flex items-center gap-2">
                  <Calendar size={14} style={{ color: colors.textSecondary }} />
                  <div className="flex-1"><div className="text-xs" style={{ color: colors.textSecondary }}>Joined Date</div><div className="text-sm" style={{ color: colors.text }}>{formatDate(user?.joinedDate)}</div></div>
                </div>
                <div className="flex items-center gap-2">
                  <LogIn size={14} style={{ color: colors.textSecondary }} />
                  <div className="flex-1"><div className="text-xs" style={{ color: colors.textSecondary }}>Total Logins</div><div className="text-sm" style={{ color: colors.text }}>{user?.totalLogins || 0}</div></div>
                </div>
              </div>
            </div>
          </div>

          <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
              <button onClick={() => { closeModal(); setTimeout(() => handleEditUser(user), 100); }} className="px-4 py-2 rounded text-sm font-medium transition-colors hover-lift flex items-center justify-center gap-2" style={{ backgroundColor: colors.primaryDark, color: 'white' }}>
                <Edit size={14} /> Edit User
              </button>
              <button onClick={() => { closeModal(); setTimeout(() => openModal('resetPassword', user), 100); }} className="px-4 py-2 rounded text-sm font-medium transition-colors hover-lift flex items-center justify-center gap-2" style={{ backgroundColor: colors.warning, color: 'white' }}>
                <Key size={14} /> Reset Password
              </button>
              <button onClick={async () => { closeModal(); await handleDeleteUser(user); }} className="px-4 py-2 rounded text-sm font-medium transition-colors hover-lift flex items-center justify-center gap-2" style={{ backgroundColor: colors.error, color: 'white' }}>
                <Trash2 size={14} /> Delete User
              </button>
            </div>
          </div>
        </div>
      </MobileModal>
    );
  };

  // Reset Password Modal
  const ResetPasswordModal = ({ data: user }) => {
    const [forceLogout, setForceLogout] = useState(true);
    const [resetMethod, setResetMethod] = useState('email');

    const handleSubmit = async (e) => {
      e.preventDefault();
      const success = await handleResetPassword(user, { forceLogout, resetMethod });
      if (success) closeModal();
    };

    return (
      <MobileModal title="Reset Password" onClose={closeModal} showBackButton={modalStack.length > 1} onBack={closeModal}>
        <div className="space-y-4">
          <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
            <div className="text-sm font-medium" style={{ color: colors.text }}>Resetting password for: {user?.fullName || user?.username}</div>
            <div className="text-xs" style={{ color: colors.textSecondary }}>{user?.email || 'No email'}</div>
          </div>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>Reset Method</label>
              <select value={resetMethod} onChange={(e) => setResetMethod(e.target.value)} className="w-full px-3 py-2 rounded border text-sm" style={{ backgroundColor: colors.bg, borderColor: colors.inputBorder, color: colors.text }}>
                <option value="email">Email Reset Link</option>
                <option value="temporary">Generate Temporary Password</option>
                <option value="sms">SMS Reset Code</option>
              </select>
            </div>
            <div className="flex items-center gap-2">
              <input type="checkbox" id="forceLogout" checked={forceLogout} onChange={(e) => setForceLogout(e.target.checked)} className="rounded" />
              <label htmlFor="forceLogout" className="text-sm" style={{ color: colors.text }}>Force logout from all devices</label>
            </div>
            <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
              <div className="flex flex-col sm:flex-row gap-2">
                <button type="submit" className="px-4 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift" style={{ backgroundColor: colors.warning, color: 'white' }}>Reset Password</button>
                <button type="button" onClick={closeModal} className="px-4 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift" style={{ backgroundColor: colors.hover, color: colors.text }}>Cancel</button>
              </div>
            </div>
          </form>
        </div>
      </MobileModal>
    );
  };

  // Import Users Modal
  const ImportUsersModal = () => {
    const [file, setFile] = useState(null);
    const [importType, setImportType] = useState('csv');
    const [importOptions, setImportOptions] = useState({ sendWelcomeEmail: true, generatePasswords: true, defaultRoleId: '' });
    const [localRoles, setLocalRoles] = useState([]);
    const [rolesLoading, setRolesLoading] = useState(false);

    useEffect(() => {
      const fetchRoles = async () => {
        if (!authToken) return;
        setRolesLoading(true);
        try {
          const response = await getAllRoles(authToken, { page: 0, size: 100 });
          if (response && response.responseCode === 200 && response.data) {
            const content = response.data.content || [];
            const uniqueRolesMap = new Map();
            content.forEach(role => {
              if (!uniqueRolesMap.has(role.roleId)) {
                uniqueRolesMap.set(role.roleId, { id: role.roleId, roleId: role.roleId, roleName: role.roleName });
              }
            });
            setLocalRoles(Array.from(uniqueRolesMap.values()));
          }
        } catch (error) {
          console.error('Error loading roles:', error);
        } finally {
          setRolesLoading(false);
        }
      };
      fetchRoles();
    }, [authToken]);

    const handleSubmit = async (e) => {
      e.preventDefault();
      if (!file) { showToast('error', 'Please select a file to import'); return; }
      const reader = new FileReader();
      reader.onload = async (event) => {
        const base64Content = event.target.result.split(',')[1];
        const importData = { fileName: file.name, fileType: importType, fileContent: base64Content, options: importOptions };
        const success = await handleImportUsersFromFile(importData);
        if (success) closeModal();
      };
      reader.readAsDataURL(file);
    };

    return (
      <MobileModal title="Import Users" onClose={closeModal} showBackButton={modalStack.length > 1} onBack={closeModal}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div><label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>File Type</label>
            <select value={importType} onChange={(e) => setImportType(e.target.value)} className="w-full px-3 py-2 rounded border text-sm" style={{ backgroundColor: colors.bg, borderColor: colors.inputBorder, color: colors.text }}>
              <option value="csv">CSV</option><option value="excel">Excel</option><option value="json">JSON</option>
            </select>
          </div>
          <div><label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>File *</label>
            <input type="file" onChange={(e) => setFile(e.target.files[0])} accept=".csv,.xlsx,.xls,.json" className="w-full px-3 py-2 rounded border text-sm" style={{ backgroundColor: colors.inputBg, borderColor: colors.inputBorder, color: colors.text }} required />
          </div>
          <div className="space-y-2">
            <div className="flex items-center gap-2"><input type="checkbox" id="sendWelcomeEmail" checked={importOptions.sendWelcomeEmail} onChange={(e) => setImportOptions(prev => ({ ...prev, sendWelcomeEmail: e.target.checked }))} className="rounded" /><label htmlFor="sendWelcomeEmail" className="text-sm" style={{ color: colors.text }}>Send welcome email</label></div>
            <div className="flex items-center gap-2"><input type="checkbox" id="generatePasswords" checked={importOptions.generatePasswords} onChange={(e) => setImportOptions(prev => ({ ...prev, generatePasswords: e.target.checked }))} className="rounded" /><label htmlFor="generatePasswords" className="text-sm" style={{ color: colors.text }}>Generate passwords automatically</label></div>
            <div><label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>Default Role</label>
              <select value={importOptions.defaultRoleId} onChange={(e) => setImportOptions(prev => ({ ...prev, defaultRoleId: e.target.value }))} className="w-full px-3 py-2 rounded border text-sm" style={{ backgroundColor: colors.bg, borderColor: colors.inputBorder, color: colors.text }} disabled={rolesLoading}>
                <option value="">{rolesLoading ? 'Loading roles...' : 'Select Default Role'}</option>
                {localRoles.map(role => <option key={role.roleId} value={role.roleId}>{role.roleName}</option>)}
              </select>
            </div>
          </div>
          <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
            <div className="flex flex-col sm:flex-row gap-2">
              <button type="submit" className="px-4 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift" style={{ backgroundColor: colors.success, color: 'white' }}>Import Users</button>
              <button type="button" onClick={closeModal} className="px-4 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift" style={{ backgroundColor: colors.hover, color: colors.text }}>Cancel</button>
            </div>
          </div>
        </form>
      </MobileModal>
    );
  };

  // Export Users Modal
  const ExportUsersModal = () => {
    const [exportFormat, setExportFormat] = useState('csv');
    const [exportFields] = useState(['id', 'username', 'email', 'fullName', 'roleId', 'roleName', 'status', 'department', 'lastActive', 'joinedDate']);
    const [filters, setFilters] = useState({ roleId: '', status: '', department: '', createdAfter: '' });
    const [localRoles, setLocalRoles] = useState([]);

    useEffect(() => {
      const fetchRoles = async () => {
        if (!authToken) return;
        try {
          const response = await getAllRoles(authToken, { page: 0, size: 100 });
          if (response && response.responseCode === 200 && response.data) {
            const content = response.data.content || [];
            const uniqueRolesMap = new Map();
            content.forEach(role => {
              if (!uniqueRolesMap.has(role.roleId)) {
                uniqueRolesMap.set(role.roleId, { id: role.roleId, roleId: role.roleId, roleName: role.roleName });
              }
            });
            setLocalRoles(Array.from(uniqueRolesMap.values()));
          }
        } catch (error) {
          console.error('Error loading roles:', error);
        }
      };
      fetchRoles();
    }, [authToken]);

    const handleSubmit = async (e) => {
      e.preventDefault();
      const exportData = { format: exportFormat, fields: exportFields, filters: Object.keys(filters).reduce((acc, key) => { if (filters[key]) acc[key] = filters[key]; return acc; }, {}) };
      await handleExportData(exportData);
      closeModal();
    };

    return (
      <MobileModal title="Export Users" onClose={closeModal} showBackButton={modalStack.length > 1} onBack={closeModal}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div><label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>Export Format *</label>
            <select value={exportFormat} onChange={(e) => setExportFormat(e.target.value)} className="w-full px-3 py-2 rounded border text-sm" style={{ backgroundColor: colors.bg, borderColor: colors.inputBorder, color: colors.text }} required>
              <option value="csv">CSV</option><option value="json">JSON</option><option value="excel">Excel</option><option value="pdf">PDF</option>
            </select>
          </div>
          <div className="space-y-3">
            <div className="text-xs font-medium" style={{ color: colors.textSecondary }}>Filter Export Data (Optional)</div>
            <div className="grid grid-cols-2 gap-2">
              <select value={filters.roleId} onChange={(e) => setFilters(prev => ({ ...prev, roleId: e.target.value }))} className="px-2 py-1 rounded border text-xs" style={{ backgroundColor: colors.bg, borderColor: colors.inputBorder, color: colors.text }}>
                <option value="">All Roles</option>{localRoles.map(role => <option key={role.roleId} value={role.roleId}>{role.roleName}</option>)}
              </select>
              <select value={filters.status} onChange={(e) => setFilters(prev => ({ ...prev, status: e.target.value }))} className="px-2 py-1 rounded border text-xs" style={{ backgroundColor: colors.bg, borderColor: colors.inputBorder, color: colors.text }}>
                <option value="">All Status</option><option value="active">Active</option><option value="inactive">Inactive</option><option value="pending">Pending</option><option value="suspended">Suspended</option>
              </select>
              <input type="text" placeholder="Department" value={filters.department} onChange={(e) => setFilters(prev => ({ ...prev, department: e.target.value }))} className="px-2 py-1 rounded border text-xs" style={{ backgroundColor: colors.inputBg, borderColor: colors.inputBorder, color: colors.text }} />
              <input type="date" value={filters.createdAfter} onChange={(e) => setFilters(prev => ({ ...prev, createdAfter: e.target.value }))} className="px-2 py-1 rounded border text-xs" style={{ backgroundColor: colors.inputBg, borderColor: colors.inputBorder, color: colors.text }} />
            </div>
          </div>
          <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
            <div className="flex flex-col sm:flex-row gap-2">
              <button type="submit" className="px-4 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift" style={{ backgroundColor: colors.success, color: 'white' }}>Export Users</button>
              <button type="button" onClick={closeModal} className="px-4 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift" style={{ backgroundColor: colors.hover, color: colors.text }}>Cancel</button>
            </div>
          </div>
        </form>
      </MobileModal>
    );
  };

  const ModalRenderer = () => {
    if (modalStack.length === 0) return null;
    const modal = modalStack[modalStack.length - 1];
    switch(modal.type) {
      case 'userDetails': return <UserDetailsModal key={modalStack.length} data={modal.data} />;
      case 'editUser': return <EditUserModal key={modalStack.length} data={modal.data} />;
      case 'resetPassword': return <ResetPasswordModal key={modalStack.length} data={modal.data} />;
      case 'importUsers': return <ImportUsersModal key={modalStack.length} />;
      case 'exportUsers': return <ExportUsersModal key={modalStack.length} />;
      default: return null;
    }
  };

  const LoadingOverlay = () => {
    if (!loading && !rolesLoading) return null;
    return (
      <div className="fixed inset-0 flex items-center justify-center z-50" style={{ backgroundColor: colors.bg }}>
        <div className="text-center">
          <Loader className="animate-spin mx-auto mb-6" size={64} style={{ color: colors.primary }} />
          <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>User Management</h3>
          <p className="text-xs mb-2" style={{ color: colors.textSecondary }}>{loading ? 'Loading users...' : 'Loading roles...'}</p>
        </div>
      </div>
    );
  };

  const StatCard = ({ title, value, icon: Icon, color }) => (
    <div className="border rounded-xl p-3 hover-lift cursor-pointer transition-all duration-200" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
      <div className="flex items-center justify-between mb-2">
        <div className="text-xs font-medium truncate" style={{ color: colors.textSecondary }}>{title}</div>
        <div className="p-2 rounded-lg shrink-0" style={{ backgroundColor: `${color}20` }}><Icon size={16} style={{ color }} /></div>
      </div>
      <div className="text-xl font-bold truncate" style={{ color: colors.text }}>{value}</div>
    </div>
  );

  const Pagination = () => {
    const { start, end } = getDisplayRange();
    if (totalItems === 0) return <div className="p-4 text-center text-sm" style={{ color: colors.textSecondary }}>No users found</div>;
    return (
      <div className="flex flex-col sm:flex-row items-center justify-between gap-3 p-4 border-t" style={{ borderColor: colors.border }}>
        <div className="text-xs" style={{ color: colors.textSecondary }}>Showing {start} - {end} of {totalItems} users</div>
        <div className="flex items-center gap-1">
          <button onClick={goToPreviousPage} disabled={currentPage === 1} className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors" style={{ backgroundColor: currentPage === 1 ? 'transparent' : colors.hover, color: colors.text }}>
            <ChevronLeft size={14} />
          </button>
          <span className="px-3 py-1 text-sm" style={{ color: colors.text }}>Page {currentPage} of {totalPages}</span>
          <button onClick={goToNextPage} disabled={currentPage === totalPages || totalPages === 0} className="p-1.5 rounded disabled:opacity-30 hover:bg-opacity-50 transition-colors" style={{ backgroundColor: currentPage === totalPages || totalPages === 0 ? 'transparent' : colors.hover, color: colors.text }}>
            <ChevronRight size={14} />
          </button>
        </div>
        <div className="flex items-center gap-2">
          <label className="text-xs" style={{ color: colors.textSecondary }}>Show:</label>
          <select value={usersPerPage} onChange={(e) => { setUsersPerPage(parseInt(e.target.value)); setCurrentPage(1); }} className="px-2 py-1 rounded border text-xs" style={{ backgroundColor: colors.bg, borderColor: colors.inputBorder, color: colors.text }}>
            <option value="5">5</option><option value="7">7</option><option value="10">10</option><option value="15">15</option><option value="20">20</option><option value="50">50</option>
          </select>
        </div>
      </div>
    );
  };

  const SideNavigation = () => {
  const [expandedSections, setExpandedSections] = useState(['users', 'security', 'roles']);
  
  const sideNavItems = [
    {
      id: 'users',
      label: 'User Management',
      icon: <Users size={16} />,
      subItems: [
        { id: 'all-users', label: 'All Users', icon: <UsersIcon size={12} />, filter: { status: 'all', role: 'all' } },
        { id: 'active-users', label: 'Active Users', icon: <UserCheck size={12} />, filter: { status: 'active', role: 'all' } },
        { id: 'inactive-users', label: 'Inactive Users', icon: <Clock size={12} />, filter: { status: 'inactive', role: 'all' } },
        { id: 'pending-users', label: 'Pending Users', icon: <Clock size={12} />, filter: { status: 'pending', role: 'all' } },
        { id: 'suspended-users', label: 'Suspended Users', icon: <UserX size={12} />, filter: { status: 'suspended', role: 'all' } }
      ]
    },
    // {
    //   id: 'roles',
    //   label: 'Roles & Permissions',
    //   icon: <Shield size={16} />,
    //   subItems: [
    //     { id: 'role-management', label: 'Role Management', icon: <ShieldIcon size={12} /> },
    //     { id: 'permission-sets', label: 'Permission Sets', icon: <KeyRound size={12} /> },
    //     { id: 'access-control', label: 'Access Control', icon: <Lock size={12} /> }
    //   ]
    // }
  ];

  const toggleSection = (sectionId) => {
    setExpandedSections(prev =>
      prev.includes(sectionId)
        ? prev.filter(id => id !== sectionId)
        : [...prev, sectionId]
    );
  };

  return (
    <div className="w-80 border-r flex flex-col h-full" style={{ 
      backgroundColor: colors.sidebar,
      borderColor: colors.border
    }}>
      <div className="p-4 border-b" style={{ borderColor: colors.border }}>
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
        
        <div className="flex gap-2">
          <button 
            onClick={() => openModal('editUser', { 
              id: 'new',
              username: '',
              email: '',
              phoneNumber: '',
              fullName: '',
              roleId: '',
              role: '',
              status: 'pending',
              department: '',
              location: '',
              mfaEnabled: false,
              emailVerified: false,
              phoneVerified: false,
              tags: []
            })}
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

      <div className="flex-1 overflow-auto p-2 space-y-4">
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
                      // Apply filters based on the clicked subItem
                      if (subItem.filter) {
                        // Update status filter
                        if (subItem.filter.status !== undefined) {
                          setSelectedStatus(subItem.filter.status);
                        }
                        // Update role filter
                        if (subItem.filter.role !== undefined) {
                          setSelectedRole(subItem.filter.role);
                        }
                        // Reset to page 1 and reload users
                        setCurrentPage(1);
                        // The useEffect will trigger loadUsers with the new filters
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

  const handleSearchInputChange = (e) => {
    const value = e.target.value;
    setSearchQuery(value);
    handleSearchChange(value);
  };

  return (
    <div className="flex flex-col h-screen overflow-hidden" style={{ backgroundColor: colors.bg, color: colors.text, fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif', fontSize: '13px' }}>
      <style>{`
        .hover-lift:hover { transform: translateY(-2px); transition: transform 0.2s ease; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }
        .animate-fade-in { animation: fadeIn 0.2s ease-out; }
        @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
        ::-webkit-scrollbar { width: 6px; height: 6px; }
        ::-webkit-scrollbar-track { background: ${colors.border}; border-radius: 4px; }
        ::-webkit-scrollbar-thumb { background: ${colors.textTertiary}; border-radius: 4px; }
      `}</style>

      <LoadingOverlay />
      <ModalRenderer />

      <div className="flex items-center justify-between h-10 px-4 border-b" style={{ backgroundColor: colors.header, borderColor: colors.border }}>
        <div className="flex items-center gap-2"><span className="px-3 py-1.5 text-sm font-medium -ml-3 uppercase">User Management</span></div>
        <div className="flex items-center gap-2">
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
            <input type="text" placeholder="Search user details..." value={searchQuery} onChange={handleSearchInputChange} className="pl-8 pr-3 py-1.5 rounded text-xs focus:outline-none w-64 hover-lift" style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }} />
            {searchQuery && (<div className="absolute right-2 top-1/2 transform -translate-y-1/2"><button onClick={() => { setSearchQuery(''); handleSearchChange(''); }} className="p-0.5 rounded hover:bg-opacity-50 transition-colors hover-lift" style={{ backgroundColor: colors.hover }}><X size={12} style={{ color: colors.textSecondary }} /></button></div>)}
          </div>
        </div>
      </div>

      <div className="flex-1 overflow-hidden flex">
        <SideNavigation />
        <div className="flex-1 overflow-hidden">
          <div className="h-full overflow-auto p-6">
            <div className="max-w-8xl mx-auto ml-2 mr-2">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
                <div><h1 className="text-xl font-bold" style={{ color: colors.text }}>User Management</h1><p className="text-xs" style={{ color: colors.textSecondary }}>Manage user accounts, roles, and permissions</p></div>
                <div className="flex items-center gap-3">
                  <button onClick={handleImportUsers} className="px-3 py-2 rounded-lg text-sm font-medium hover-lift transition-all duration-200 flex items-center gap-2" style={{ backgroundColor: colors.primaryDark, color: 'white' }} disabled={loading}><Upload size={14} /><span className="hidden sm:inline">Import Users</span><span className="sm:hidden">Import</span></button>
                  <button onClick={handleExportClick} className="px-3 py-2 rounded-lg text-sm font-medium hover-lift transition-all duration-200 flex items-center gap-2" style={{ backgroundColor: colors.success, color: 'white' }} disabled={loading}><Download size={14} /><span className="hidden sm:inline">Export Users</span><span className="sm:hidden">Export</span></button>
                  <button 
                    onClick={() => openModal('editUser', { 
                      id: 'new',
                      username: '',
                      email: '',
                      phoneNumber: '',
                      fullName: '',
                      roleId: '',  // This will be set when role is selected
                      role: '',    // For backward compatibility
                      status: 'pending',
                      department: '',
                      location: '',
                      mfaEnabled: false,
                      emailVerified: false,
                      phoneVerified: false,
                      tags: []
                    })}
                    className="px-3 py-2 rounded-lg text-sm font-medium hover-lift transition-all duration-200 flex items-center gap-2"
                    style={{ backgroundColor: colors.primaryDark, color: 'white' }}
                    disabled={loading}
                  >
                    <UserPlus size={14} />
                    <span className="hidden sm:inline">Add User</span>
                    <span className="sm:hidden">Add</span>
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
                <StatCard title="Total Users" value={totalItems} icon={Users} color={colors.primary} />
                <StatCard title="Active Users" value={stats.activeUsers} icon={UserCheck} color={colors.success} />
                <StatCard title="Admins" value={stats.admins} icon={Shield} color={colors.error} />
                <StatCard title="Security Score" value={`${stats.avgSecurityScore}/100`} icon={ShieldCheck} color={colors.warning} />
              </div>

              <div className="border rounded-xl mb-6" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
                <div className="p-4 border-b" style={{ borderColor: colors.border }}>
                  <div className="flex flex-col sm:flex-row gap-3">
                    <div className="flex-1 relative">
                      <Search size={16} className="absolute left-3 top-1/2 transform -translate-y-1/2" style={{ color: colors.textSecondary }} />
                      <input type="text" placeholder="Search users by name, email, or username..." value={searchQuery} onChange={handleSearchInputChange} className="w-full pl-10 pr-3 py-2 rounded border text-sm" style={{ backgroundColor: colors.inputBg, borderColor: colors.inputBorder, color: colors.text }} disabled={loading} />
                    </div>
                    <div className="flex gap-2">
                      <select value={selectedRole} onChange={(e) => handleRoleFilterChange(e.target.value)} className="px-3 py-2 rounded border text-sm" style={{ backgroundColor: colors.bg, borderColor: colors.inputBorder, color: colors.text }} disabled={loading}>
                        <option value="all">All Roles</option>
                        {roles.map(role => <option key={role.id || role.roleId} value={role.id || role.roleId}>{role.roleName}</option>)}
                      </select>
                      <select value={selectedStatus} onChange={(e) => handleStatusFilterChange(e.target.value)} className="px-3 py-2 rounded border text-sm" style={{ backgroundColor: colors.bg, borderColor: colors.inputBorder, color: colors.text }} disabled={loading}>
                        <option value="all">All Status</option>
                        <option value="active">Active</option><option value="inactive">Inactive</option><option value="pending">Pending</option><option value="suspended">Suspended</option>
                      </select>
                      <button onClick={() => setShowFilters(!showFilters)} className="px-3 py-2 rounded border text-sm font-medium hover-lift transition-colors" style={{ backgroundColor: colors.hover, borderColor: colors.border, color: colors.text }} disabled={loading}><Filter size={14} /></button>
                    </div>
                  </div>
                </div>

                {selectedUsers.length > 0 && (
                  <div className="p-3 border-b flex items-center justify-between" style={{ borderColor: colors.border, backgroundColor: colors.selected }}>
                    <div className="text-sm font-medium" style={{ color: colors.text }}>{selectedUsers.length} user{selectedUsers.length > 1 ? 's' : ''} selected</div>
                    <div className="flex items-center gap-2">
                      <button onClick={() => handleBulkAction('activate')} className="px-2 py-1 rounded text-xs font-medium hover-lift transition-colors" style={{ backgroundColor: colors.success, color: 'white' }} disabled={loading}>Activate</button>
                      <button onClick={() => handleBulkAction('suspend')} className="px-2 py-1 rounded text-xs font-medium hover-lift transition-colors" style={{ backgroundColor: colors.warning, color: 'white' }} disabled={loading}>Suspend</button>
                      <button onClick={() => handleBulkAction('delete')} className="px-2 py-1 rounded text-xs font-medium hover-lift transition-colors" style={{ backgroundColor: colors.error, color: 'white' }} disabled={loading}>Delete</button>
                      <button onClick={() => setSelectedUsers([])} className="px-2 py-1 rounded text-xs font-medium hover-lift transition-colors" style={{ backgroundColor: colors.hover, color: colors.text }} disabled={loading}>Clear</button>
                    </div>
                  </div>
                )}

                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr style={{ backgroundColor: colors.tableHeader }}>
                        <th className="p-3 text-left text-xs font-medium" style={{ color: colors.textSecondary }}><input type="checkbox" checked={selectedUsers.length === users.length && users.length > 0} onChange={handleSelectAll} className="rounded" style={{ borderColor: colors.border }} disabled={loading} /></th>
                        <th className="p-3 text-left text-xs font-medium cursor-pointer hover:bg-opacity-50 transition-colors" onClick={() => handleSort('fullName')} style={{ color: colors.textSecondary }}><div className="flex items-center gap-1">User{sortField === 'fullName' && (sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />)}</div></th>
                        <th className="p-3 text-left text-xs font-medium" style={{ color: colors.textSecondary }}>Email</th>
                        <th className="p-3 text-left text-xs font-medium" style={{ color: colors.textSecondary }}>Phone Number</th>
                        <th className="p-3 text-left text-xs font-medium" style={{ color: colors.textSecondary }}>Access Type</th>
                        <th className="p-3 text-left text-xs font-medium" style={{ color: colors.textSecondary }}>Status</th>
                        <th className="p-3 text-left text-xs font-medium cursor-pointer hover:bg-opacity-50 transition-colors" onClick={() => handleSort('lastActive')} style={{ color: colors.textSecondary }}><div className="flex items-center gap-1">Last Active{sortField === 'lastActive' && (sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />)}</div></th>
                        <th className="p-3 text-left text-xs font-medium" style={{ color: colors.textSecondary }}>Security Score</th>
                        <th className="p-3 text-left text-xs font-medium" style={{ color: colors.textSecondary }}>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {users.map(user => (
                        <tr key={user.id} className="border-t hover-lift transition-colors" style={{ borderColor: colors.border, backgroundColor: selectedUsers.includes(user.id) ? colors.selected : 'transparent', cursor: openingModalForUserId === user.id ? 'wait' : 'pointer' }} onClick={(e) => { if (!e.target.closest('button') && !e.target.closest('input[type="checkbox"]') && !e.target.closest('a')) handleRowClick(user, e); }}>
                          <td className="p-3" onClick={(e) => e.stopPropagation()}><input type="checkbox" checked={selectedUsers.includes(user.id)} onChange={() => handleSelectUser(user.id)} className="rounded" style={{ borderColor: colors.border }} disabled={loading || openingModalForUserId === user.id} /></td>
                          <td className="p-3"><div className="flex items-center gap-3"><div className="w-8 h-8 rounded-full flex items-center justify-center text-white font-medium text-sm" style={{ backgroundColor: user.avatarColor || colors.primary }}>{user.fullName?.split(' ').map(n => n[0]).join('').slice(0, 2) || '??'}</div><div className="min-w-0"><div className="text-sm font-semibold truncate" style={{ color: colors.text }}>{user.fullName || 'Unknown User'}</div><div className="text-xs truncate" style={{ color: colors.textSecondary }}>@{user.username || 'unknown'}</div></div></div></td>
                          <td className="p-3"><div className="text-sm truncate" style={{ color: colors.text }}>{user.email || 'No email'}</div></td>
                          <td className="p-3"><div className="text-sm truncate" style={{ color: colors.text }}>{user.phoneNumber || 'No phone number'}</div></td>
                          <td className="p-3"><div className="px-2 py-1 rounded-full text-xs font-medium w-fit uppercase" style={getRoleColorStyle(user.role || user.roleId)}>{getRoleDisplayName(user.role || user.roleId)}</div></td>
                          <td className="p-3"><div className="px-2 py-1 rounded-full text-xs font-medium w-fit" style={getStatusColorStyle(user.status)}>{getUserStatusDisplayName(user.status)}</div></td>
                          <td className="p-3">
                            <div className="text-sm" style={{ color: !user.lastActive || user.totalLogins === 0 ? colors.textTertiary : colors.text }}>
                                {formatLastActive(user.lastActive, user.totalLogins)}
                            </div>
                            {(!user.lastActive || user.totalLogins === 0) && (
                                <div className="text-xs" style={{ color: colors.warning }}>
                                    <AlertCircle size={10} className="inline mr-1" />
                                    Awaiting first login
                                </div>
                            )}
                        </td>
                          <td className="p-3"><div className="flex items-center gap-1"><div className="text-sm font-medium" style={{ color: colors.text }}>{user.securityScore || 0}</div><div className="w-16 h-1 rounded-full bg-gray-200"><div className="h-full rounded-full" style={{ width: `${user.securityScore || 0}%`, backgroundColor: getSecurityColor(user.securityScore) }} /></div></div></td>
                          <td className="p-3" onClick={(e) => e.stopPropagation()}><div className="flex items-center gap-2">
                            <button onClick={(e) => { e.stopPropagation(); e.preventDefault(); handleViewUserDetails(user); }} className="p-1.5 rounded hover:bg-opacity-50 transition-colors" style={{ backgroundColor: colors.hover }} title="View Details" disabled={loading || openingModalForUserId === user.id}><Eye size={14} style={{ color: colors.textSecondary }} /></button>
                            <button onClick={(e) => { e.stopPropagation(); e.preventDefault(); handleEditUser(user); }} className="p-1.5 rounded hover:bg-opacity-50 transition-colors" style={{ backgroundColor: colors.hover }} title="Edit User" disabled={loading || openingModalForUserId === user.id}><Edit size={14} style={{ color: colors.textSecondary }} /></button>
                            <button onClick={(e) => { e.stopPropagation(); e.preventDefault(); handleDeleteUser(user); }} className="p-1.5 rounded hover:bg-opacity-50 transition-colors" style={{ backgroundColor: colors.hover }} title="Delete User" disabled={loading || openingModalForUserId === user.id}><Trash2 size={14} style={{ color: colors.error }} /></button>
                          </div></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <Pagination />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserManagement;