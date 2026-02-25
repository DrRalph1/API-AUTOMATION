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
import UserManagementAPI, {
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
  validateCreateUser,
  validateUpdateUser,
  validateBulkOperation,
  validateSearchUsers,
  validateImportUsers,
  validateExportUsers,
  validateUpdateStatus,
  validateUserDataRequest,
  getUserStatusDisplayName,
  getStatusColor,
  getRoleColor,
  getSecurityScoreColor,
  formatDateForDisplay,
  getDefaultUserFilters
} from '@/controllers/UserManagementController.js';


// Import UserRoleController methods
import {
  getAllRoles,
  handleRoleResponse,
  extractRolePaginationInfo
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
  
  // API states
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [rolesLoading, setRolesLoading] = useState(false);
  const [rolesError, setRolesError] = useState(false);
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
  
  // Modal states
  const [modalStack, setModalStack] = useState([]);
  
  // Mobile states
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isMobileSearchOpen, setIsMobileSearchOpen] = useState(false);
  const [isRightSidebarVisible, setIsRightSidebarVisible] = useState(false);

  // Fix: Add states to prevent multiple modal openings
  const [openingModalForUserId, setOpeningModalForUserId] = useState(null);
  const [isOpeningModal, setIsOpeningModal] = useState(false);
  const pendingRequestRef = useRef(null);

  // Load roles on component mount
  useEffect(() => {
    if (authToken) {
      loadRoles();
    }
  }, [authToken]);

  // Load users on component mount
  useEffect(() => {
    if (authToken) {
      loadUsers();
      loadStatistics();
    } else {
      console.warn('No auth token available');
    }
  }, [authToken]);

  // Load roles from API - NO STATIC FALLBACKS
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
      
      // Handle your exact API response structure
      if (response && response.responseCode === 200 && response.data) {
        // Extract content from the paginated response
        const content = response.data.content || [];
        
        // Map the roles to a consistent format using the actual field names from your API
        const mappedRoles = content.map(role => ({
          id: role.roleId,
          roleId: role.roleId,
          roleName: role.roleName,
          description: role.description || '',
          roleCode: role.roleName?.replace(/\s+/g, '_').toUpperCase() || ''
        }));
        
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

  // Get role display name from role ID - NO STATIC FALLBACKS
  const getRoleDisplayName = (roleId) => {
    if (!roleId) return 'Unknown';
    if (roleId === 'all') return 'All Roles';
    
    // Try to find by id or roleId
    const role = roles.find(r => r.id === roleId || r.roleId === roleId);
    if (role) return role.roleName;
    
    // If not found, just return the roleId as is
    return roleId;
  };

  // Get role color from UUID - Uses the controller's getRoleColor function
  const getRoleColorFromId = (roleId) => {
    if (!roleId || roleId === 'all') return '#6B7280'; // Default gray
    return getRoleColor(roleId);
  };

  // Load users with filters
  const loadUsers = async (filters = {}) => {
    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return;
    }

    setLoading(true);
    try {
      const response = await getUsersList(authHeader, {
        searchQuery: filters.searchQuery || searchQuery,
        roleFilter: filters.roleFilter || selectedRole,
        statusFilter: filters.statusFilter || selectedStatus,
        sortField: filters.sortField || sortField,
        sortDirection: filters.sortDirection || sortDirection,
        page: filters.page || currentPage,
        pageSize: filters.pageSize || usersPerPage
      });
      
      const processedResponse = handleUserManagementResponse(response);
      const userList = extractUsersList(processedResponse);
      
      // Map role IDs to role names for display
      const mappedUserList = userList.map(user => ({
        ...user,
        roleDisplayName: getRoleDisplayName(user.role || user.roleId),
        roleDisplayColor: getRoleColorFromId(user.role || user.roleId)
      }));
      
      setUsers(mappedUserList || []);
      
    } catch (error) {
      console.error('Error loading users:', error);
      showToast('error', error.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  // Load statistics
  const loadStatistics = async () => {
    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return;
    }

    try {
      const response = await getUserStatistics(authHeader);
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
      showToast('error', error.message || 'Failed to load statistics');
    }
  };

  // Load user details
  const loadUserDetails = async (userId) => {
    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return null;
    }

    setLoading(true);
    try {
      const response = await getUserDetails(authHeader, userId);
      const processedResponse = handleUserManagementResponse(response);
      const userDetails = extractUserDetails(processedResponse);
      
      // Add role display name
      if (userDetails) {
        userDetails.roleDisplayName = getRoleDisplayName(userDetails.roleId || userDetails.role);
        userDetails.roleDisplayColor = getRoleColorFromId(userDetails.roleId || userDetails.role);
      }
      
      return userDetails;
    } catch (error) {
      console.error('Error loading user details:', error);
      showToast('error', error.message || 'Failed to load user details');
      return null;
    } finally {
      setLoading(false);
    }
  };

  // Handle create user
  const handleCreateUser = async (userData) => {
    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return false;
    }

    setLoading(true);
    try {
      const response = await createUser(authHeader, userData);
      const processedResponse = handleUserManagementResponse(response);
      const result = extractCreateUserResults(processedResponse);
      
      if (result.success) {
        showToast('success', result.message || 'User created successfully');
        loadUsers();
        loadStatistics();
        return true;
      } else {
        showToast('error', result.message || 'Failed to create user');
        return false;
      }
    } catch (error) {
      console.error('Error creating user:', error);
      showToast('error', error.message || 'Failed to create user');
      return false;
    } finally {
      setLoading(false);
    }
  };

  // Handle update user
  const handleUpdateUser = async (userId, userData) => {
    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return false;
    }

    setLoading(true);
    try {
      const response = await updateUser(authHeader, userId, userData);
      const processedResponse = handleUserManagementResponse(response);
      const result = extractUpdateUserResults(processedResponse);
      
      if (result.success) {
        showToast('success', result.message || 'User updated successfully');
        loadUsers();
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

  // Handle delete user
  const handleDeleteUser = async (user) => {
    if (!window.confirm(`Are you sure you want to delete ${user.fullName || user.username}?`)) {
      return;
    }

    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return;
    }

    setLoading(true);
    try {
      const response = await deleteUser(authHeader, user.id);
      const processedResponse = handleUserManagementResponse(response);
      const result = extractDeleteUserResults(processedResponse);
      
      if (result.success) {
        showToast('success', result.message || 'User deleted successfully');
        setSelectedUsers(prev => prev.filter(id => id !== user.id));
        loadUsers();
        loadStatistics();
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

  // Handle bulk operations
  const handleBulkAction = async (action) => {
    if (selectedUsers.length === 0) {
      showToast('warning', 'No users selected');
      return;
    }

    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return;
    }

    const confirmationMessages = {
      activate: `Activate ${selectedUsers.length} selected user(s)?`,
      suspend: `Suspend ${selectedUsers.length} selected user(s)?`,
      deactivate: `Deactivate ${selectedUsers.length} selected user(s)?`,
      delete: `Delete ${selectedUsers.length} selected user(s)?`,
      reset_password: `Reset password for ${selectedUsers.length} selected user(s)?`
    };

    if (!window.confirm(confirmationMessages[action] || 'Confirm action?')) {
      return;
    }

    setLoading(true);
    try {
      const response = await bulkUserOperation(authHeader, {
        operation: action,
        userIds: selectedUsers
      });
      
      const processedResponse = handleUserManagementResponse(response);
      const result = extractBulkOperationResults(processedResponse);
      
      if (result.processedCount > 0) {
        showToast('success', `${action} completed for ${result.processedCount} user(s)`);
        if (result.failedCount > 0) {
          showToast('warning', `${result.failedCount} user(s) failed: ${result.failedUsers?.join(', ') || 'Unknown'}`);
        }
        loadUsers();
        loadStatistics();
        
        if (action === 'delete') {
          setSelectedUsers([]);
        }
      } else {
        showToast('error', 'No users were processed');
      }
    } catch (error) {
      console.error('Error performing bulk operation:', error);
      showToast('error', error.message || 'Failed to perform bulk operation');
    } finally {
      setLoading(false);
    }
  };

  // Handle reset password
  const handleResetPassword = async (user, resetData) => {
    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return false;
    }

    setLoading(true);
    try {
      const response = await resetUserPassword(authHeader, user.id, resetData);
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

  // Handle search users
  const handleSearchUsers = async (searchData) => {
    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return;
    }

    setLoading(true);
    try {
      const response = await searchUsers(authHeader, searchData);
      const processedResponse = handleUserManagementResponse(response);
      const result = extractSearchUsersResults(processedResponse);
      
      // Map role IDs to role names for display
      const mappedResults = (result.results || []).map(user => ({
        ...user,
        roleDisplayName: getRoleDisplayName(user.role || user.roleId),
        roleDisplayColor: getRoleColorFromId(user.role || user.roleId)
      }));
      
      setUsers(mappedResults);
      setCurrentPage(1);
      
    } catch (error) {
      console.error('Error searching users:', error);
      showToast('error', error.message || 'Failed to search users');
    } finally {
      setLoading(false);
    }
  };

  // Handle import users
  const handleImportUsersFromFile = async (importData) => {
    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return false;
    }

    setLoading(true);
    try {
      const response = await importUsers(authHeader, importData);
      const processedResponse = handleUserManagementResponse(response);
      const result = extractImportUsersResults(processedResponse);
      
      if (result.status === 'completed') {
        showToast('success', `Imported ${result.importedCount} users successfully`);
        loadUsers();
        loadStatistics();
        return true;
      } else {
        showToast('error', `Import failed: ${result.summary?.validationErrors || 'Unknown error'}`);
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

  // Handle export users
  const handleExportData = async (exportData) => {
    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return;
    }

    setLoading(true);
    try {
      const response = await exportUsers(authHeader, exportData);
      const processedResponse = handleUserManagementResponse(response);
      const result = extractExportUsersResults(processedResponse);
      
      if (result.status === 'ready') {
        const downloadLink = document.createElement('a');
        downloadLink.href = result.exportData?.downloadUrl || '#';
        downloadLink.download = `users-export-${new Date().toISOString().split('T')[0]}.${exportData.format}`;
        document.body.appendChild(downloadLink);
        downloadLink.click();
        document.body.removeChild(downloadLink);
        
        showToast('success', 'Export completed successfully');
      } else {
        showToast('error', 'Export failed');
      }
    } catch (error) {
      console.error('Error exporting users:', error);
      showToast('error', error.message || 'Failed to export users');
    } finally {
      setLoading(false);
    }
  };

  // Handle update user status
  const handleToggleUserStatus = async (user, newStatus) => {
    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return;
    }

    setLoading(true);
    try {
      const response = await updateUserStatus(authHeader, user.id, newStatus);
      const processedResponse = handleUserManagementResponse(response);
      const result = extractUpdateStatusResults(processedResponse);
      
      if (result.success) {
        showToast('success', result.message || 'Status updated successfully');
        loadUsers();
        loadStatistics();
      } else {
        showToast('error', result.message || 'Failed to update status');
      }
    } catch (error) {
      console.error('Error updating user status:', error);
      showToast('error', error.message || 'Failed to update user status');
    } finally {
      setLoading(false);
    }
  };

  // Handle load roles and permissions
  const handleLoadRolesAndPermissions = async () => {
    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return null;
    }

    setLoading(true);
    try {
      const response = await getRolesAndPermissions(authHeader);
      const processedResponse = handleUserManagementResponse(response);
      return extractRoles(processedResponse);
    } catch (error) {
      console.error('Error loading roles and permissions:', error);
      showToast('error', error.message || 'Failed to load roles and permissions');
      return null;
    } finally {
      setLoading(false);
    }
  };

  // Handle validate user data
  const handleValidateUserData = async (validationData) => {
    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return null;
    }

    try {
      const response = await validateUserData(authHeader, validationData);
      const processedResponse = handleUserManagementResponse(response);
      return extractValidationResults(processedResponse);
    } catch (error) {
      console.error('Error validating user data:', error);
      showToast('error', error.message || 'Failed to validate user data');
      return null;
    }
  };

  // Load user activity
  const handleLoadUserActivity = async (userId, startDate = null, endDate = null) => {
    const authHeader = authToken;
    if (!authHeader) {
      showToast('error', 'Authentication required. Please log in.');
      return null;
    }

    setLoading(true);
    try {
      const response = await getUserActivity(authHeader, userId, startDate, endDate);
      const processedResponse = handleUserManagementResponse(response);
      return extractUserActivityResults(processedResponse);
    } catch (error) {
      console.error('Error loading user activity:', error);
      showToast('error', error.message || 'Failed to load user activity');
      return null;
    } finally {
      setLoading(false);
    }
  };

  // Toast notification
  const showToast = (type, message) => {
    if (type === 'error') {
      alert(`Error: ${message}`);
    } else if (type === 'warning') {
      alert(`Warning: ${message}`);
    } else {
      alert(`Success: ${message}`);
    }
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

  // Role colors - Uses the controller's getRoleColor function
  const getRoleColorStyle = (roleId) => {
    const color = getRoleColorFromId(roleId) || colors.textSecondary;
    return {
      backgroundColor: `${color}20`,
      color: color
    };
  };

  // Status colors
  const getStatusColorStyle = (status) => {
    const color = getStatusColor(status) || colors.textSecondary;
    return {
      backgroundColor: `${color}20`,
      color: color
    };
  };

  // Security score color
  const getSecurityColor = (score) => {
    return getSecurityScoreColor(score) || colors.textSecondary;
  };

  // Modal management
  const openModal = (type, data) => {
    setModalStack(prev => [...prev, { type, data }]);
  };

  const closeModal = () => {
    setModalStack(prev => prev.slice(0, -1));
    // Clear opening state when modal closes
    setTimeout(() => {
      setOpeningModalForUserId(null);
      setIsOpeningModal(false);
      pendingRequestRef.current = null;
    }, 100);
  };

  const closeAllModals = () => {
    setModalStack([]);
    setOpeningModalForUserId(null);
    setIsOpeningModal(false);
    pendingRequestRef.current = null;
  };

  const getCurrentModal = () => {
    if (modalStack.length === 0) return null;
    return modalStack[modalStack.length - 1];
  };

  // Filter and sort users
  const filteredUsers = users.filter(user => {
    const matchesSearch = searchQuery === '' || 
      (user.username?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.fullName?.toLowerCase().includes(searchQuery.toLowerCase()));
    
    const matchesRole = selectedRole === 'all' || (user.role === selectedRole) || (user.roleId === selectedRole);
    const matchesStatus = selectedStatus === 'all' || user.status === selectedStatus;
    
    return matchesSearch && matchesRole && matchesStatus;
  });

  const sortedUsers = [...filteredUsers].sort((a, b) => {
    if (sortField === 'lastActive') {
      return sortDirection === 'asc' 
        ? new Date(a.lastActive || 0) - new Date(b.lastActive || 0)
        : new Date(b.lastActive || 0) - new Date(a.lastActive || 0);
    }
    if (sortField === 'fullName') {
      return sortDirection === 'asc'
        ? (a.fullName || '').localeCompare(b.fullName || '')
        : (b.fullName || '').localeCompare(a.fullName || '');
    }
    if (sortField === 'joinedDate') {
      return sortDirection === 'asc'
        ? new Date(a.joinedDate || 0) - new Date(b.joinedDate || 0)
        : new Date(b.joinedDate || 0) - new Date(a.joinedDate || 0);
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

  const handleRefresh = async () => {
    await loadUsers();
    await loadStatistics();
    await loadRoles(true);
  };

  const handleSort = (field) => {
    if (sortField === field) {
      const newDirection = sortDirection === 'asc' ? 'desc' : 'asc';
      setSortDirection(newDirection);
      
      loadUsers({
        sortField: field,
        sortDirection: newDirection
      });
    } else {
      setSortField(field);
      setSortDirection('asc');
      
      loadUsers({
        sortField: field,
        sortDirection: 'asc'
      });
    }
  };

  const handleSearchChange = (value) => {
    setSearchQuery(value);
    clearTimeout(window.searchTimeout);
    window.searchTimeout = setTimeout(() => {
      if (value.trim()) {
        handleSearchUsers({ query: value });
      } else {
        loadUsers({ searchQuery: '' });
      }
    }, 500);
  };

  const handleRoleFilterChange = (value) => {
    setSelectedRole(value);
    loadUsers({ roleFilter: value });
  };

  const handleStatusFilterChange = (value) => {
    setSelectedStatus(value);
    loadUsers({ statusFilter: value });
  };

  // FIXED: Debounced view user details to prevent multiple API calls
  const debouncedViewUserDetails = useDebounce(async (user) => {
    // Don't proceed if no user or already loading
    if (!user || !user.id) return;
    
    // Check if already loading this user
    if (openingModalForUserId === user.id || isOpeningModal) {
      console.log('Already loading this user, skipping...');
      return;
    }

    // Check if we already have a pending request for this user
    if (pendingRequestRef.current === user.id) {
      console.log('Request already pending for this user, skipping...');
      return;
    }

    pendingRequestRef.current = user.id;
    setOpeningModalForUserId(user.id);
    setIsOpeningModal(true);

    try {
      console.log('Loading user details (debounced):', user.id);
      const userDetails = await loadUserDetails(user.id);
      
      if (userDetails) {
        openModal('userDetails', userDetails);
      }
    } catch (error) {
      console.error('Error loading user details:', error);
      showToast('error', error.message || 'Failed to load user details');
    } finally {
      // Clear states after a delay to prevent rapid clicking
      // setTimeout(() => {
      //   setOpeningModalForUserId(null);
      //   setIsOpeningModal(false);
      //   pendingRequestRef.current = null;
      // }, 500);
    }
  }, 300); // 300ms debounce

  // FIXED: Handle view user details with debouncing
  const handleViewUserDetails = (user) => {
    debouncedViewUserDetails(user);
  };

  // FIXED: Handle row click with proper event handling
  const handleRowClick = (user, e) => {
    // Only open modal if clicking on the row itself, not buttons/checkboxes
    if (e.target.closest('button') || 
        e.target.closest('input') || 
        e.target.closest('a')) {
      return;
    }
    
    handleViewUserDetails(user);
  };

  const handleEditUser = (user) => {
    openModal('editUser', user);
  };

  const handleImportUsers = () => {
    openModal('importUsers', {});
  };

  const getResponsiveIconSize = () => {
    if (typeof window !== 'undefined') {
      if (window.innerWidth < 480) return 12;
      if (window.innerWidth < 768) return 14;
      return 14;
    }
    return 14;
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
          { id: 'inactive-users', label: 'Inactive Users', icon: <Clock size={12} /> },
          { id: 'pending-users', label: 'Pending Users', icon: <Clock size={12} /> },
          { id: 'suspended-users', label: 'Suspended Users', icon: <UserX size={12} /> }
        ]
      },
      {
        id: 'roles',
        label: 'Roles & Permissions',
        icon: <Shield size={16} />,
        subItems: [
          { id: 'role-management', label: 'Role Management', icon: <ShieldIcon size={12} /> },
          { id: 'permission-sets', label: 'Permission Sets', icon: <KeyRound size={12} /> },
          { id: 'access-control', label: 'Access Control', icon: <Lock size={12} /> }
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
                fullName: '',
                roleId: '',
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
                        if (subItem.id === 'all-users') {
                          setSelectedRole('all');
                          setSelectedStatus('all');
                        } else if (subItem.id === 'active-users') {
                          setSelectedStatus('active');
                        } else if (subItem.id === 'inactive-users') {
                          setSelectedStatus('inactive');
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

  // Modal Components
  const MobileModal = ({ children, title, onClose, showBackButton = false, onBack }) => {
      const iconSize = getResponsiveIconSize();
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
            style={{ 
              backgroundColor: colors.bg,
              borderColor: colors.modalBorder,
              zIndex: zIndex
            }}
          >
            <div className="sticky top-0 p-3 sm:p-4 border-b flex items-center justify-between backdrop-blur-sm" style={{ 
              borderColor: colors.border,
              backgroundColor: colors.modalBg
            }}>
              <div className="flex items-center gap-2">
                {showBackButton && (
                  <button 
                    onClick={onBack}
                    className="p-1 sm:p-1.5 rounded hover:bg-opacity-50 transition-colors shrink-0"
                    style={{ backgroundColor: colors.hover }}
                  >
                    <ChevronLeft size={16} style={{ color: colors.text }} />
                  </button>
                )}
                <h3 className="text-base sm:text-lg font-semibold truncate" style={{ color: colors.text }}>
                  {title}
                </h3>
              </div>
              <button 
                onClick={onClose}
                className="p-1 sm:p-1.5 rounded hover:bg-opacity-50 transition-colors shrink-0"
                style={{ backgroundColor: colors.hover }}
              >
                <X size={18} style={{ color: colors.text }} />
              </button>
            </div>
            <div className="p-3 sm:p-4 overflow-auto">
              {children}
            </div>
          </div>
        </div>
      );
    };

  // User Details Modal
  const UserDetailsModal = ({ data: user }) => {
    const [userDetails, setUserDetails] = useState(user);
    const [activityLog, setActivityLog] = useState([]);

    const formatDate = (dateString) => {
      return formatDateForDisplay(dateString, true) || '';
    };

    const getPermissionColor = (permission) => {
      switch(permission) {
        case 'admin': return colors.error;
        case 'write': return colors.warning;
        case 'delete': return colors.error;
        case 'read': return colors.success;
        default: return colors.textSecondary;
      }
    };

    return (
      <MobileModal 
        title="User Details" 
        onClose={closeModal}
        showBackButton={modalStack.length > 1}
        onBack={closeModal}
      >
        <div className="space-y-6">
          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4 p-4 rounded-xl" style={{ 
            backgroundColor: colors.card,
            border: `1px solid ${colors.border}`
          }}>
            <div 
              className="w-16 h-16 rounded-full flex items-center justify-center text-white font-medium text-xl"
              style={{ backgroundColor: userDetails?.avatarColor || colors.primary }}
            >
              {userDetails?.fullName?.split(' ').map(n => n[0]).join('') || '??'}
            </div>
            <div className="flex-1 min-w-0">
              <h4 className="text-xl font-bold truncate" style={{ color: colors.text }}>
                {userDetails?.fullName || 'Unknown User'}
              </h4>
              <div className="flex flex-wrap items-center gap-2 mt-1">
                <div className="flex items-center gap-1 text-sm">
                  <UserCircle size={14} style={{ color: colors.textSecondary }} />
                  <span style={{ color: colors.textSecondary }}>@{userDetails?.username || 'unknown'}</span>
                </div>
                <div 
                  className="px-2 py-0.5 rounded-full text-xs font-medium uppercase"
                  style={getRoleColorStyle(userDetails?.roleId || userDetails?.role)}
                >
                  {getRoleDisplayName(userDetails?.roleId || userDetails?.role)}
                </div>
                <div 
                  className="px-2 py-0.5 rounded-full text-xs font-medium"
                  style={getStatusColorStyle(userDetails?.status)}
                >
                  {getUserStatusDisplayName(userDetails?.status)}
                </div>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-4">
              <div>
                <h5 className="text-sm font-semibold mb-3 flex items-center gap-2" style={{ color: colors.text }}>
                  <UserCircle size={16} />
                  Basic Information
                </h5>
                <div className="space-y-2">
                  <div className="flex items-center gap-2">
                    <Mail size={14} style={{ color: colors.textSecondary }} />
                    <div className="flex-1">
                      <div className="text-xs" style={{ color: colors.textSecondary }}>Email</div>
                      <div className="text-sm truncate" style={{ color: colors.text }}>{userDetails?.email || 'No email'}</div>
                    </div>
                    {userDetails?.emailVerified ? (
                      <CheckCircle size={14} style={{ color: colors.success }} />
                    ) : (
                      <XCircle size={14} style={{ color: colors.error }} />
                    )}
                  </div>
                  <div className="flex items-center gap-2">
                    <Building size={14} style={{ color: colors.textSecondary }} />
                    <div className="flex-1">
                      <div className="text-xs" style={{ color: colors.textSecondary }}>Department</div>
                      <div className="text-sm" style={{ color: colors.text }}>{userDetails?.department || 'Not specified'}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <MapPin size={14} style={{ color: colors.textSecondary }} />
                    <div className="flex-1">
                      <div className="text-xs" style={{ color: colors.textSecondary }}>Location</div>
                      <div className="text-sm" style={{ color: colors.text }}>{userDetails?.location || 'Not specified'}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Globe size={14} style={{ color: colors.textSecondary }} />
                    <div className="flex-1">
                      <div className="text-xs" style={{ color: colors.textSecondary }}>Timezone</div>
                      <div className="text-sm" style={{ color: colors.text }}>{userDetails?.timezone || 'Not specified'}</div>
                    </div>
                  </div>
                </div>
              </div>

              <div>
                <h5 className="text-sm font-semibold mb-3 flex items-center gap-2" style={{ color: colors.text }}>
                  <Shield size={16} />
                  Security
                </h5>
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <ShieldCheck size={14} style={{ color: colors.textSecondary }} />
                      <span className="text-sm" style={{ color: colors.text }}>MFA Enabled</span>
                    </div>
                    {userDetails?.mfaEnabled ? (
                      <CheckCircle size={14} style={{ color: colors.success }} />
                    ) : (
                      <XCircle size={14} style={{ color: colors.error }} />
                    )}
                  </div>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Key size={14} style={{ color: colors.textSecondary }} />
                      <span className="text-sm" style={{ color: colors.text }}>API Keys</span>
                    </div>
                    <span className="text-sm font-medium" style={{ color: colors.text }}>{userDetails?.apiKeys || 0}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Database size={14} style={{ color: colors.textSecondary }} />
                      <span className="text-sm" style={{ color: colors.text }}>API Access Count</span>
                    </div>
                    <span className="text-sm font-medium" style={{ color: colors.text }}>{userDetails?.apiAccessCount || 0}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Server size={14} style={{ color: colors.textSecondary }} />
                      <span className="text-sm" style={{ color: colors.text }}>Active Sessions</span>
                    </div>
                    <span className="text-sm font-medium" style={{ color: colors.text }}>{userDetails?.activeSessions || 0}</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <div>
                <h5 className="text-sm font-semibold mb-3 flex items-center gap-2" style={{ color: colors.text }}>
                  <Activity size={16} />
                  Activity
                </h5>
                <div className="space-y-2">
                  <div className="flex items-center gap-2">
                    <Clock size={14} style={{ color: colors.textSecondary }} />
                    <div className="flex-1">
                      <div className="text-xs" style={{ color: colors.textSecondary }}>Last Active</div>
                      <div className="text-sm" style={{ color: colors.text }}>{formatDate(userDetails?.lastActive)}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Calendar size={14} style={{ color: colors.textSecondary }} />
                    <div className="flex-1">
                      <div className="text-xs" style={{ color: colors.textSecondary }}>Joined Date</div>
                      <div className="text-sm" style={{ color: colors.text }}>{formatDate(userDetails?.joinedDate)}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <LogIn size={14} style={{ color: colors.textSecondary }} />
                    <div className="flex-1">
                      <div className="text-xs" style={{ color: colors.textSecondary }}>Total Logins</div>
                      <div className="text-sm" style={{ color: colors.text }}>{userDetails?.totalLogins || 0}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <ShieldAlert size={14} style={{ color: colors.textSecondary }} />
                    <div className="flex-1">
                      <div className="text-xs" style={{ color: colors.textSecondary }}>Failed Logins</div>
                      <div className="text-sm" style={{ color: colors.error }}>{userDetails?.failedLogins || 0}</div>
                    </div>
                  </div>
                </div>
              </div>

              <div>
                <h5 className="text-sm font-semibold mb-3 flex items-center gap-2" style={{ color: colors.text }}>
                  <ShieldCheck size={16} />
                  Security Score
                </h5>
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm" style={{ color: colors.text }}>Score</span>
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-bold" style={{ 
                        color: getSecurityColor(userDetails?.securityScore)
                      }}>
                        {userDetails?.securityScore || 0}/100
                      </span>
                    </div>
                  </div>
                  <div className="w-full h-2 bg-gray-200 rounded-full overflow-hidden">
                    <div 
                      className="h-full rounded-full transition-all duration-300"
                      style={{ 
                        width: `${userDetails?.securityScore || 0}%`,
                        backgroundColor: getSecurityColor(userDetails?.securityScore)
                      }}
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <div>
                      <div style={{ color: colors.textSecondary }}>Total Logins</div>
                      <div style={{ color: colors.text }}>{userDetails?.totalLogins || 0}</div>
                    </div>
                    <div>
                      <div style={{ color: colors.textSecondary }}>Failed Logins</div>
                      <div style={{ color: colors.error }}>{userDetails?.failedLogins || 0}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {userDetails?.permissions && userDetails.permissions.length > 0 && (
            <div>
              <h5 className="text-sm font-semibold mb-3 flex items-center gap-2" style={{ color: colors.text }}>
                <KeyRound size={16} />
                Permissions
              </h5>
              <div className="flex flex-wrap gap-2">
                {userDetails.permissions.map((permission, index) => (
                  <div 
                    key={index}
                    className="px-3 py-1 rounded-full text-xs font-medium"
                    style={{ 
                      backgroundColor: `${getPermissionColor(permission)}20`,
                      color: getPermissionColor(permission)
                    }}
                  >
                    {permission}
                  </div>
                ))}
              </div>
            </div>
          )}

          {userDetails?.tags && userDetails.tags.length > 0 && (
            <div>
              <h5 className="text-sm font-semibold mb-3 flex items-center gap-2" style={{ color: colors.text }}>
                <Tag size={16} />
                Tags
              </h5>
              <div className="flex flex-wrap gap-2">
                {userDetails.tags.map((tag, index) => (
                  <div 
                    key={index}
                    className="px-2 py-1 rounded text-xs"
                    style={{ 
                      backgroundColor: colors.hover,
                      color: colors.textSecondary
                    }}
                  >
                    {tag}
                  </div>
                ))}
              </div>
            </div>
          )}

          {activityLog.length > 0 && (
            <div>
              <h5 className="text-sm font-semibold mb-3 flex items-center gap-2" style={{ color: colors.text }}>
                <Activity size={16} />
                Recent Activity
              </h5>
              <div className="space-y-2">
                {activityLog.slice(0, 5).map((activity, index) => (
                  <div key={index} className="flex items-center gap-2 p-2 rounded border" style={{ 
                    borderColor: colors.border,
                    backgroundColor: colors.hover
                  }}>
                    <div className="w-2 h-2 rounded-full" style={{ 
                      backgroundColor: activity.success ? colors.success : colors.error 
                    }} />
                    <div className="flex-1">
                      <div className="text-xs" style={{ color: colors.text }}>{activity.description}</div>
                      <div className="text-xs" style={{ color: colors.textSecondary }}>
                        {formatDateForDisplay(activity.timestamp, true)}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
              <button 
                onClick={() => {
                  closeModal();
                  setTimeout(() => handleEditUser(userDetails), 100);
                }}
                className="px-4 py-2 rounded text-sm font-medium transition-colors hover-lift flex items-center justify-center gap-2"
                style={{ 
                  backgroundColor: colors.primaryDark,
                  color: 'white'
                }}
              >
                <Edit size={14} />
                Edit User
              </button>
              <button 
                onClick={() => {
                  closeModal();
                  setTimeout(() => openModal('resetPassword', userDetails), 100);
                }}
                className="px-4 py-2 rounded text-sm font-medium transition-colors hover-lift flex items-center justify-center gap-2"
                style={{ 
                  backgroundColor: colors.warning,
                  color: 'white'
                }}
              >
                <Key size={14} />
                Reset Password
              </button>
              <button 
                onClick={async () => {
                  closeModal();
                  await handleDeleteUser(userDetails);
                }}
                className="px-4 py-2 rounded text-sm font-medium transition-colors hover-lift flex items-center justify-center gap-2"
                style={{ 
                  backgroundColor: colors.error,
                  color: 'white'
                }}
              >
                <Trash2 size={14} />
                Delete User
              </button>
            </div>
          </div>
        </div>
      </MobileModal>
    );
  };

  // Edit User Modal - NO STATIC FALLBACKS
  // Edit User Modal - Fixed role selection
const EditUserModal = ({ data: user }) => {
  const [formData, setFormData] = useState({
    username: user?.username || '',
    email: user?.email || '',
    fullName: user?.fullName || '',
    roleId: user?.roleId || user?.role || '', // Make sure this isn't undefined
    status: user?.status || 'pending',
    department: user?.department || '',
    location: user?.location || '',
    mfaEnabled: user?.mfaEnabled || false,
    emailVerified: user?.emailVerified || false,
    phoneVerified: user?.phoneVerified || false,
    tags: user?.tags || []
  });

  const [validationResult, setValidationResult] = useState(null);
  const [localRoles, setLocalRoles] = useState([]);
  const [rolesLoading, setRolesLoading] = useState(false);
  const [rolesError, setRolesError] = useState(false);

  // Fetch roles when modal opens - NO STATIC FALLBACKS
  useEffect(() => {
    const fetchRoles = async () => {
      if (!authToken) {
        setRolesError(true);
        return;
      }
      
      setRolesLoading(true);
      setRolesError(false);
      
      try {
        const response = await getAllRoles(authToken, { page: 0, size: 100 });
        
        if (response && response.responseCode === 200 && response.data) {
          const content = response.data.content || [];
          
          const mappedRoles = content.map(role => ({
            id: role.roleId,
            roleId: role.roleId,
            roleName: role.roleName,
            description: role.description || ''
          }));
          
          setLocalRoles(mappedRoles);
          
          // Debug: Log loaded roles and current user role
          console.log('Loaded roles:', mappedRoles);
          console.log('User role ID:', user?.roleId || user?.role);
          
          // IMPORTANT FIX: If user has a role but it's not in the loaded roles yet,
          // we need to manually add it or set it after roles load
          const userRoleId = user?.roleId || user?.role;
          if (userRoleId && userRoleId !== 'new') {
            // Check if the user's role exists in the loaded roles
            const roleExists = mappedRoles.some(role => 
              role.roleId === userRoleId || role.id === userRoleId
            );
            
            if (!roleExists && userRoleId) {
              console.log('User role not found in loaded roles, adding manually');
              // Add the user's role to the list if it doesn't exist
              setLocalRoles(prev => [
                ...prev,
                {
                  id: userRoleId,
                  roleId: userRoleId,
                  roleName: getRoleDisplayName(userRoleId) || 'Unknown Role'
                }
              ]);
            }
            
            // Ensure formData has the correct roleId
            setFormData(prev => ({
              ...prev,
              roleId: userRoleId
            }));
          }
          
          if (mappedRoles.length === 0) {
            setRolesError(true);
          }
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
  }, [authToken, user]);

  // FIX: Ensure roleId is set when the component mounts
  useEffect(() => {
    // If we have a user with a role, make sure it's selected
    if (user && (user.roleId || user.role)) {
      const userRoleId = user.roleId || user.role;
      setFormData(prev => ({
        ...prev,
        roleId: userRoleId
      }));
    }
  }, [user]);


  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Debug: Check what roleId is being used
    console.log('Selected roleId:', formData.roleId);
    
    if (!formData.roleId) {
      showToast('error', 'Please select a role');
      return;
    }
    
    if (user?.id === 'new') {
      // Create new user
      const createData = {
        username: formData.username,
        email: formData.email,
        fullName: formData.fullName,
        roleId: formData.roleId,
        password: 'TemporaryPassword123!',
        department: formData.department || 'Engineering',
        location: formData.location || 'New York, NY',
        mfaEnabled: formData.mfaEnabled,
        status: formData.status || 'pending'
      };
      
      console.log('Creating user with data:', createData);
      const success = await handleCreateUser(createData);
      
      if (success) {
        closeModal();
      }
    } else {
      // Update existing user
      const success = await handleUpdateUser(user.id, {
        fullName: formData.fullName,
        roleId: formData.roleId,
        status: formData.status,
        department: formData.department,
        location: formData.location,
        mfaEnabled: formData.mfaEnabled,
        emailVerified: formData.emailVerified,
        phoneVerified: formData.phoneVerified,
        tags: formData.tags
      });
      
      if (success) {
        closeModal();
      }
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
              {validationResult.issues.map((issue, index) => (
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
            {/* {user?.id !== 'new' && (
              <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
                Username cannot be changed
              </div>
            )} */}
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
            {/* {user?.id !== 'new' && (
              <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
                Email cannot be changed
              </div>
            )} */}
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
                    setRolesLoading(true);
                    const fetchRoles = async () => {
                      try {
                        const response = await getAllRoles(authToken, { page: 0, size: 100 });
                        if (response && response.responseCode === 200 && response.data) {
                          const content = response.data.content || [];
                          const mappedRoles = content.map(role => ({
                            id: role.roleId,
                            roleId: role.roleId,
                            roleName: role.roleName
                          }));
                          setLocalRoles(mappedRoles);
                          
                          // Re-set the user's role after reload
                          const userRoleId = user?.roleId || user?.role;
                          if (userRoleId && userRoleId !== 'new') {
                            setFormData(prev => ({ ...prev, roleId: userRoleId }));
                          }
                          
                          if (mappedRoles.length > 0) {
                            setRolesError(false);
                          }
                        }
                      } catch (error) {
                        console.error('Error retrying roles:', error);
                      } finally {
                        setRolesLoading(false);
                      }
                    };
                    fetchRoles();
                  }}
                  className="p-1 rounded hover:bg-opacity-50"
                  style={{ backgroundColor: `${colors.error}30` }}
                >
                  <RefreshCw size={14} className={rolesLoading ? 'animate-spin' : ''} />
                </button>
              </div>
            ) : (
              <select
                key={`role-select-${formData.roleId || 'empty'}`} // Force re-render when roleId changes
                value={formData.roleId || ''}
                onChange={(e) => {
                  console.log('Role selected:', e.target.value);
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
                disabled={rolesLoading || localRoles.length === 0}
              >
                <option value="">
                  {rolesLoading ? 'Loading access types...' : 
                   localRoles.length === 0 ? 'No access types available' : 'Select Access Type'}
                </option>
                {localRoles.map(role => (
                  <option 
                    key={role.roleId} 
                    value={role.roleId}
                    selected={formData.roleId === role.roleId} // Explicit selected attribute
                  >
                    {role.roleName}
                  </option>
                ))}
              </select>
            )}
            {rolesLoading && (
              <div className="flex items-center gap-2 text-xs mt-1" style={{ color: colors.textSecondary }}>
                <RefreshCw size={12} className="animate-spin" />
                <span>Loading roles from server...</span>
              </div>
            )}
            {/* Debug display - remove in production */}
            {/* <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
              Selected role ID: {formData.roleId || 'None'}
            </div> */}
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
              disabled={(validationResult && !validationResult.valid) || rolesLoading || localRoles.length === 0}
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

  // Reset Password Modal
  const ResetPasswordModal = ({ data: user }) => {
    const [forceLogout, setForceLogout] = useState(true);
    const [resetMethod, setResetMethod] = useState('email');

    const handleSubmit = async (e) => {
      e.preventDefault();
      
      const success = await handleResetPassword(user, {
        forceLogout,
        resetMethod
      });
      
      if (success) {
        closeModal();
      }
    };

    return (
      <MobileModal 
        title="Reset Password" 
        onClose={closeModal}
        showBackButton={modalStack.length > 1}
        onBack={closeModal}
      >
        <div className="space-y-4">
          <div className="p-3 rounded" style={{ backgroundColor: colors.hover }}>
            <div className="text-sm font-medium" style={{ color: colors.text }}>
              Resetting password for: {user?.fullName || user?.username}
            </div>
            <div className="text-xs" style={{ color: colors.textSecondary }}>
              {user?.email || 'No email'}
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
                Reset Method
              </label>
              <select
                value={resetMethod}
                onChange={(e) => setResetMethod(e.target.value)}
                className="w-full px-3 py-2 rounded border text-sm"
                style={{ 
                  backgroundColor: colors.inputBg,
                  borderColor: colors.inputBorder,
                  color: colors.text
                }}
              >
                <option value="email">Email Reset Link</option>
                <option value="temporary">Generate Temporary Password</option>
                <option value="sms">SMS Reset Code</option>
              </select>
            </div>

            <div className="flex items-center gap-2">
              <input
                type="checkbox"
                id="forceLogout"
                checked={forceLogout}
                onChange={(e) => setForceLogout(e.target.checked)}
                className="rounded"
              />
              <label htmlFor="forceLogout" className="text-sm" style={{ color: colors.text }}>
                Force logout from all devices
              </label>
            </div>

            <div className="p-3 rounded border" style={{ 
              borderColor: colors.warning,
              backgroundColor: `${colors.warning}20`
            }}>
              <div className="text-xs" style={{ color: colors.warning }}>
                <strong>Note:</strong> User will receive password reset instructions via {resetMethod}.
                {forceLogout && ' All active sessions will be terminated.'}
              </div>
            </div>

            <div className="pt-4 border-t" style={{ borderColor: colors.border }}>
              <div className="flex flex-col sm:flex-row gap-2">
                <button 
                  type="submit"
                  className="px-4 py-2 rounded text-sm font-medium transition-colors flex-1 hover-lift"
                  style={{ 
                    backgroundColor: colors.warning,
                    color: 'white'
                  }}
                >
                  Reset Password
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
        </div>
      </MobileModal>
    );
  };

  // Import Users Modal - NO STATIC FALLBACKS
  const ImportUsersModal = () => {
    const [file, setFile] = useState(null);
    const [importType, setImportType] = useState('csv');
    const [importOptions, setImportOptions] = useState({
      sendWelcomeEmail: true,
      generatePasswords: true,
      defaultRoleId: ''
    });
    const [localRoles, setLocalRoles] = useState([]);
    const [rolesLoading, setRolesLoading] = useState(false);
    const [rolesError, setRolesError] = useState(false);

    // Fetch roles when modal opens - NO STATIC FALLBACKS
    useEffect(() => {
      const fetchRoles = async () => {
        if (!authToken) {
          setRolesError(true);
          return;
        }
        
        setRolesLoading(true);
        setRolesError(false);
        
        try {
          const response = await getAllRoles(authToken, { page: 0, size: 100 });
          
          if (response && response.responseCode === 200 && response.data) {
            const content = response.data.content || [];
            
            const mappedRoles = content.map(role => ({
              id: role.roleId,
              roleId: role.roleId,
              roleName: role.roleName
            }));
            
            setLocalRoles(mappedRoles);
            
            if (mappedRoles.length === 0) {
              setRolesError(true);
            }
          } else {
            setRolesError(true);
          }
        } catch (error) {
          console.error('Error loading roles:', error);
          setRolesError(true);
        } finally {
          setRolesLoading(false);
        }
      };
      
      fetchRoles();
    }, [authToken]);

    const handleFileChange = (e) => {
      setFile(e.target.files[0]);
    };

    const handleSubmit = async (e) => {
      e.preventDefault();
      if (!file) {
        showToast('error', 'Please select a file to import');
        return;
      }

      const reader = new FileReader();
      reader.onload = async (event) => {
        const base64Content = event.target.result.split(',')[1];
        
        const importData = {
          fileName: file.name,
          fileType: importType,
          fileContent: base64Content,
          options: importOptions
        };

        const success = await handleImportUsersFromFile(importData);
        if (success) {
          closeModal();
        }
      };
      reader.readAsDataURL(file);
    };

    return (
      <MobileModal 
        title="Import Users" 
        onClose={closeModal}
        showBackButton={modalStack.length > 1}
        onBack={closeModal}
      >
        <div className="space-y-4">
          <div className="p-3 rounded border" style={{ 
            borderColor: colors.border,
            backgroundColor: colors.hover
          }}>
            <div className="text-sm font-medium mb-1" style={{ color: colors.text }}>
              Supported Formats
            </div>
            <div className="text-xs" style={{ color: colors.textSecondary }}>
              • CSV (Comma-separated values)<br/>
              • Excel (.xlsx, .xls)<br/>
              • JSON (JavaScript Object Notation)
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
                File Type
              </label>
              <select
                value={importType}
                onChange={(e) => setImportType(e.target.value)}
                className="w-full px-3 py-2 rounded border text-sm"
                style={{ 
                  backgroundColor: colors.bg,
                  borderColor: colors.inputBorder,
                  color: colors.text
                }}
              >
                <option value="csv">CSV</option>
                <option value="excel">Excel</option>
                <option value="json">JSON</option>
              </select>
            </div>

            <div>
              <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
                File *
              </label>
              <input
                type="file"
                onChange={handleFileChange}
                accept=".csv,.xlsx,.xls,.json"
                className="w-full px-3 py-2 rounded border text-sm"
                style={{ 
                  backgroundColor: colors.inputBg,
                  borderColor: colors.inputBorder,
                  color: colors.text
                }}
                required
              />
            </div>

            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="sendWelcomeEmail"
                  checked={importOptions.sendWelcomeEmail}
                  onChange={(e) => setImportOptions(prev => ({ ...prev, sendWelcomeEmail: e.target.checked }))}
                  className="rounded"
                />
                <label htmlFor="sendWelcomeEmail" className="text-sm" style={{ color: colors.text }}>
                  Send welcome email
                </label>
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="generatePasswords"
                  checked={importOptions.generatePasswords}
                  onChange={(e) => setImportOptions(prev => ({ ...prev, generatePasswords: e.target.checked }))}
                  className="rounded"
                />
                <label htmlFor="generatePasswords" className="text-sm" style={{ color: colors.text }}>
                  Generate passwords automatically
                </label>
              </div>
              <div>
                <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
                  Default Role
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
                        setRolesLoading(true);
                        const fetchRoles = async () => {
                          try {
                            const response = await getAllRoles(authToken, { page: 0, size: 100 });
                            if (response && response.responseCode === 200 && response.data) {
                              const content = response.data.content || [];
                              const mappedRoles = content.map(role => ({
                                id: role.roleId,
                                roleId: role.roleId,
                                roleName: role.roleName
                              }));
                              setLocalRoles(mappedRoles);
                              if (mappedRoles.length > 0) {
                                setRolesError(false);
                              }
                            }
                          } catch (error) {
                            console.error('Error retrying roles:', error);
                          } finally {
                            setRolesLoading(false);
                          }
                        };
                        fetchRoles();
                      }}
                      className="p-1 rounded hover:bg-opacity-50"
                      style={{ backgroundColor: `${colors.error}30` }}
                    >
                      <RefreshCw size={14} className={rolesLoading ? 'animate-spin' : ''} />
                    </button>
                  </div>
                ) : (
                  <select
                    value={importOptions.defaultRoleId}
                    onChange={(e) => setImportOptions(prev => ({ ...prev, defaultRoleId: e.target.value }))}
                    className="w-full px-3 py-2 rounded border text-sm"
                    style={{ 
                      backgroundColor: colors.bg,
                      borderColor: colors.inputBorder,
                      color: colors.text,
                      opacity: rolesLoading ? 0.7 : 1
                    }}
                    disabled={rolesLoading || localRoles.length === 0}
                  >
                    <option value="">
                      {rolesLoading ? 'Loading roles...' : 
                       localRoles.length === 0 ? 'No roles available' : 'Select Default Role'}
                    </option>
                    {localRoles.map(role => (
                      <option key={role.roleId} value={role.roleId}>
                        {role.roleName}
                      </option>
                    ))}
                  </select>
                )}
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
                  disabled={rolesLoading || localRoles.length === 0}
                >
                  Import Users
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
        </div>
      </MobileModal>
    );
  };

  // Export Users Modal - NO STATIC FALLBACKS
  const ExportUsersModal = () => {
    const [exportFormat, setExportFormat] = useState('csv');
    const [exportFields, setExportFields] = useState([
      'id', 'username', 'email', 'fullName', 'roleId', 'roleName', 'status', 'department', 'lastActive', 'joinedDate', 'securityScore'
    ]);
    const [filters, setFilters] = useState({
      roleId: '',
      status: '',
      department: '',
      createdAfter: ''
    });
    const [localRoles, setLocalRoles] = useState([]);
    const [rolesLoading, setRolesLoading] = useState(false);
    const [rolesError, setRolesError] = useState(false);

    // Fetch roles when modal opens - NO STATIC FALLBACKS
    useEffect(() => {
      const fetchRoles = async () => {
        if (!authToken) {
          setRolesError(true);
          return;
        }
        
        setRolesLoading(true);
        setRolesError(false);
        
        try {
          const response = await getAllRoles(authToken, { page: 0, size: 100 });
          
          if (response && response.responseCode === 200 && response.data) {
            const content = response.data.content || [];
            
            const mappedRoles = content.map(role => ({
              id: role.roleId,
              roleId: role.roleId,
              roleName: role.roleName
            }));
            
            setLocalRoles(mappedRoles);
            
            if (mappedRoles.length === 0) {
              setRolesError(true);
            }
          } else {
            setRolesError(true);
          }
        } catch (error) {
          console.error('Error loading roles:', error);
          setRolesError(true);
        } finally {
          setRolesLoading(false);
        }
      };
      
      fetchRoles();
    }, [authToken]);

    const availableFields = [
      { value: 'id', label: 'ID' },
      { value: 'username', label: 'Username' },
      { value: 'email', label: 'Email' },
      { value: 'fullName', label: 'Full Name' },
      { value: 'roleId', label: 'Role ID' },
      { value: 'roleName', label: 'Role Name' },
      { value: 'status', label: 'Status' },
      { value: 'department', label: 'Department' },
      { value: 'lastActive', label: 'Last Active' },
      { value: 'joinedDate', label: 'Joined Date' },
      { value: 'securityScore', label: 'Security Score' },
      { value: 'mfaEnabled', label: 'MFA Enabled' },
      { value: 'emailVerified', label: 'Email Verified' },
      { value: 'location', label: 'Location' },
      { value: 'timezone', label: 'Timezone' }
    ];

    const handleSubmit = async (e) => {
      e.preventDefault();
      
      const exportData = {
        format: exportFormat,
        fields: exportFields,
        filters: Object.keys(filters).reduce((acc, key) => {
          if (filters[key]) acc[key] = filters[key];
          return acc;
        }, {})
      };

      await handleExportData(exportData);
      closeModal();
    };

    const toggleField = (field) => {
      if (exportFields.includes(field)) {
        setExportFields(prev => prev.filter(f => f !== field));
      } else {
        setExportFields(prev => [...prev, field]);
      }
    };

    return (
      <MobileModal 
        title="Export Users" 
        onClose={closeModal}
        showBackButton={modalStack.length > 1}
        onBack={closeModal}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="text-xs font-medium mb-3 block" style={{ color: colors.textSecondary }}>
              Export Format *
            </label>
            <select
              value={exportFormat}
              onChange={(e) => setExportFormat(e.target.value)}
              className="w-full px-3 py-2 rounded border text-sm"
              style={{ 
                backgroundColor: colors.bg,
                borderColor: colors.inputBorder,
                color: colors.text
              }}
              required
            >
              <option value="csv">CSV</option>
              <option value="json">JSON</option>
              <option value="excel">Excel</option>
              <option value="pdf">PDF</option>
            </select>
          </div>

          <div>
            <label className="text-xs font-medium mb-2 block" style={{ color: colors.textSecondary }}>
              Fields to Export
            </label>
            <div className="grid grid-cols-2 gap-2">
              {availableFields.map(field => (
                <div key={field.value} className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    id={`field-${field.value}`}
                    checked={exportFields.includes(field.value)}
                    onChange={() => toggleField(field.value)}
                    className="rounded"
                  />
                  <label htmlFor={`field-${field.value}`} className="text-xs" style={{ color: colors.text }}>
                    {field.label}
                  </label>
                </div>
              ))}
            </div>
          </div>

          <div className="space-y-3">
            <div className="text-xs font-medium" style={{ color: colors.textSecondary }}>
              Filter Export Data (Optional)
            </div>
            <div className="grid grid-cols-2 gap-2">
              {rolesError ? (
                <div 
                  className="px-2 py-1 rounded border text-xs flex items-center justify-between col-span-2"
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
                      setRolesLoading(true);
                      const fetchRoles = async () => {
                        try {
                          const response = await getAllRoles(authToken, { page: 0, size: 100 });
                          if (response && response.responseCode === 200 && response.data) {
                            const content = response.data.content || [];
                            const mappedRoles = content.map(role => ({
                              id: role.roleId,
                              roleId: role.roleId,
                              roleName: role.roleName
                            }));
                            setLocalRoles(mappedRoles);
                            if (mappedRoles.length > 0) {
                              setRolesError(false);
                            }
                          }
                        } catch (error) {
                          console.error('Error retrying roles:', error);
                        } finally {
                          setRolesLoading(false);
                        }
                      };
                      fetchRoles();
                    }}
                    className="p-0.5 rounded hover:bg-opacity-50"
                    style={{ backgroundColor: `${colors.error}30` }}
                  >
                    <RefreshCw size={12} className={rolesLoading ? 'animate-spin' : ''} />
                  </button>
                </div>
              ) : (
                <select
                  value={filters.roleId}
                  onChange={(e) => setFilters(prev => ({ ...prev, roleId: e.target.value }))}
                  className="px-2 py-1 rounded border text-xs"
                  style={{ 
                    backgroundColor: colors.bg,
                    borderColor: colors.inputBorder,
                    color: colors.text,
                    opacity: rolesLoading ? 0.7 : 1
                  }}
                  disabled={rolesLoading}
                >
                  <option value="">All Roles</option>
                  {localRoles.map(role => (
                    <option key={role.roleId} value={role.roleId}>
                      {role.roleName}
                    </option>
                  ))}
                </select>
              )}
              <select
                value={filters.status}
                onChange={(e) => setFilters(prev => ({ ...prev, status: e.target.value }))}
                className="px-2 py-1 rounded border text-xs"
                style={{ 
                  backgroundColor: colors.bg,
                  borderColor: colors.inputBorder,
                  color: colors.text
                }}
              >
                <option value="">All Status</option>
                <option value="active">Active</option>
                <option value="inactive">Inactive</option>
                <option value="pending">Pending</option>
                <option value="suspended">Suspended</option>
              </select>
              <input
                type="text"
                placeholder="Department"
                value={filters.department}
                onChange={(e) => setFilters(prev => ({ ...prev, department: e.target.value }))}
                className="px-2 py-1 rounded border text-xs"
                style={{ 
                  backgroundColor: colors.inputBg,
                  borderColor: colors.inputBorder,
                  color: colors.text
                }}
              />
              <input
                type="date"
                value={filters.createdAfter}
                onChange={(e) => setFilters(prev => ({ ...prev, createdAfter: e.target.value }))}
                className="px-2 py-1 rounded border text-xs"
                style={{ 
                  backgroundColor: colors.inputBg,
                  borderColor: colors.inputBorder,
                  color: colors.text
                }}
              />
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
                disabled={rolesLoading}
              >
                Export Users
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

  // Modal Renderer Component
  const ModalRenderer = () => {
    if (modalStack.length === 0) return null;
    
    return modalStack.map((modal, index) => {
      const isActive = index === modalStack.length - 1;
      if (!isActive) return null;
      
      switch(modal.type) {
        case 'userDetails':
          return <UserDetailsModal key={index} data={modal.data} />;
        case 'editUser':
          return <EditUserModal key={index} data={modal.data} />;
        case 'resetPassword':
          return <ResetPasswordModal key={index} data={modal.data} />;
        case 'importUsers':
          return <ImportUsersModal key={index} />;
        case 'exportUsers':
          return <ExportUsersModal key={index} />;
        default:
          return null;
      }
    });
  };

  // Loading Overlay
  const LoadingOverlay = () => {
    // Check if any loading state is active
    const isLoading = loading || 
                    rolesLoading || 
                    loading.initialLoad;
    
    // Determine loading message based on what's loading
    const getLoadingMessage = () => {
      if (loading.initialLoad) return 'Initializing User Management...';
      if (loading && rolesLoading) return 'Loading users and roles...';
      if (loading) return 'Loading users...';
      if (rolesLoading) return 'Loading roles...';
      return 'Please wait while we prepare your content';
    };

    // Determine loading tips based on context
    const getLoadingTip = () => {
      if (loading && rolesLoading) {
        return `Loading ${stats.totalUsers || ''} users and role configurations...`;
      }
      if (loading) {
        return `Loading ${stats.totalUsers || ''} users...`;
      }
      if (rolesLoading) {
        return 'Loading access types and permissions...';
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
              <Users size={32} style={{ color: colors.primary, opacity: 0.3 }} />
            </div>
          </div>
          <h3 className="text-xl font-semibold mb-2" style={{ color: colors.text }}>
            User Management
          </h3>
          <p className="text-sm mb-2" style={{ color: colors.textSecondary }}>
            {getLoadingMessage()}
          </p>
          <p className="text-xs mb-1" style={{ color: colors.textTertiary }}>
            {getLoadingTip()}
          </p>
          <p className="text-xs" style={{ color: colors.textTertiary }}>
            This won't take long
          </p>
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
    const [isOpening, setIsOpening] = useState(false);

    const formatDate = (dateString) => {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    };

    const formatTime = (dateString) => {
      const date = new Date(dateString);
      return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    };

    const handleClick = async (e) => {
      e.stopPropagation();
      if (isOpening || openingModalForUserId === user.id) return;
      
      setIsOpening(true);
      try {
        await handleViewUserDetails(user);
      } finally {
        setTimeout(() => setIsOpening(false), 500);
      }
    };

    const handleButtonClick = (e, action) => {
      e.stopPropagation();
      action(user);
    };

    return (
      <div 
        className="border rounded-xl p-3 hover-lift transition-all duration-200"
        onClick={handleClick}
        style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card,
          cursor: isOpening ? 'wait' : 'pointer'
        }}
      >
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center gap-3">
            <div 
              className="w-10 h-10 rounded-full flex items-center justify-center text-white font-medium text-sm"
              style={{ backgroundColor: user.avatarColor || colors.primary }}
            >
              {user.fullName?.split(' ').map(n => n[0]).join('') || '??'}
            </div>
            <div className="min-w-0">
              <div className="text-sm font-semibold truncate" style={{ color: colors.text }}>
                {user.fullName || 'Unknown User'}
              </div>
              <div className="text-xs truncate" style={{ color: colors.textSecondary }}>
                @{user.username || 'unknown'}
              </div>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <div 
              className="px-2 py-1 rounded-full text-xs font-medium"
              style={getRoleColorStyle(user.role || user.roleId)}
            >
              {getRoleDisplayName(user.role || user.roleId)}
            </div>
            <button
              onClick={(e) => handleButtonClick(e, handleViewUserDetails)}
              className="p-1 rounded hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.hover }}
              disabled={isOpening || openingModalForUserId === user.id}
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
                {user.email || 'No email'}
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
                {user.department || 'Not specified'}
              </span>
            </div>
            <div 
              className="px-2 py-0.5 rounded-full text-xs"
              style={getStatusColorStyle(user.status)}
            >
              {getUserStatusDisplayName(user.status)}
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
              {(user.apiKeys > 0) && (
                <Key size={12} style={{ color: colors.info }} title={`${user.apiKeys || 0} API keys`} />
              )}
            </div>
          </div>

          <div className="flex gap-2 pt-2">
            <button
              onClick={(e) => handleButtonClick(e, handleEditUser)}
              className="flex-1 px-2 py-1.5 rounded text-xs font-medium transition-colors hover-lift"
              style={{ 
                backgroundColor: colors.hover,
                color: colors.text
              }}
              disabled={isOpening}
            >
              Edit
            </button>
            <button
              onClick={(e) => handleButtonClick(e, (user) => openModal('resetPassword', user))}
              className="flex-1 px-2 py-1.5 rounded text-xs font-medium transition-colors hover-lift"
              style={{ 
                backgroundColor: colors.primaryDark,
                color: 'white'
              }}
              disabled={isOpening}
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

  const handleExportClick = () => {
    openModal('exportUsers', {});
  };

  const handleSearchInputChange = (e) => {
    const value = e.target.value;
    setSearchQuery(value);
    handleSearchChange(value);
  };

  return (
    <div className="flex flex-col h-screen overflow-hidden" style={{ 
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

      <LoadingOverlay />

      <ModalRenderer />

      <div className="flex items-center justify-between h-10 px-4 border-b" style={{ 
        backgroundColor: colors.header,
        borderColor: colors.border
      }}>
        <div className="flex items-center gap-2">
          <span className="px-3 py-1.5 text-sm font-medium -ml-3 uppercase">User Management</span>
        </div>

        <div className="flex items-center gap-2">
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
            <input 
              type="text" 
              placeholder="Search user details..."
              value={searchQuery}
              onChange={handleSearchInputChange}
              className="pl-8 pr-3 py-1.5 rounded text-xs focus:outline-none w-64 hover-lift"
              style={{ 
                backgroundColor: colors.inputBg, 
                border: `1px solid ${colors.border}`, 
                color: colors.text 
              }} 
            />
            {searchQuery && (
              <div className="absolute right-2 top-1/2 transform -translate-y-1/2">
                <button onClick={() => {
                  setSearchQuery('');
                  handleSearchChange('');
                }} className="p-0.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                  style={{ backgroundColor: colors.hover }}>
                  <X size={12} style={{ color: colors.textSecondary }} />
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="flex-1 overflow-hidden flex">
        <SideNavigation />

        <div className="flex-1 overflow-hidden">
          <div className="h-full overflow-auto p-6">
            <div className="max-w-8xl mx-auto ml-2 mr-2">
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
                  {/* <button 
                    onClick={handleRefresh}
                    className="p-2 rounded-lg hover-lift transition-all duration-200"
                    style={{ 
                      backgroundColor: colors.hover,
                      color: colors.text
                    }}
                    title="Refresh"
                    disabled={loading}
                  >
                    <RefreshCw size={18} className={loading ? 'animate-spin' : ''} />
                  </button>
                  <button 
                    onClick={handleExportClick}
                    className="px-3 py-2 rounded-lg text-sm font-medium hover-lift transition-all duration-200 flex items-center gap-2"
                    style={{ 
                      backgroundColor: colors.hover,
                      color: colors.text
                    }}
                    disabled={loading}
                  >
                    <Download size={14} />
                    <span className="hidden sm:inline">Export</span>
                  </button> */}
                  <button 
                    onClick={handleImportUsers}
                    className="px-3 py-2 rounded-lg text-sm font-medium hover-lift transition-all duration-200 flex items-center gap-2"
                    style={{ 
                      backgroundColor: colors.primaryDark,
                      color: 'white'
                    }}
                    disabled={loading}
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
                      roleId: '',
                      status: 'pending',
                      department: '',
                      location: '',
                      mfaEnabled: false,
                      emailVerified: false,
                      phoneVerified: false,
                      tags: []
                    })}
                    className="px-3 py-2 rounded-lg text-sm font-medium hover-lift transition-all duration-200 flex items-center gap-2"
                    style={{ 
                      backgroundColor: colors.success,
                      color: 'white'
                    }}
                    disabled={loading}
                  >
                    <UserPlus size={14} />
                    <span className="hidden sm:inline">Add User</span>
                    <span className="sm:hidden">Add</span>
                  </button>
                </div>
              </div>

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
                        onChange={handleSearchInputChange}
                        className="w-full pl-10 pr-3 py-2 rounded border text-sm"
                        style={{ 
                          backgroundColor: colors.inputBg,
                          borderColor: colors.inputBorder,
                          color: colors.text
                        }}
                        disabled={loading}
                      />
                    </div>
                    <div className="flex gap-2">
                      <select
                        value={selectedRole}
                        onChange={(e) => handleRoleFilterChange(e.target.value)}
                        className="px-3 py-2 rounded border text-sm"
                        style={{ 
                          backgroundColor: colors.bg,
                          borderColor: colors.inputBorder,
                          color: colors.text
                        }}
                        disabled={loading}
                      >
                        <option value="all">All Roles</option>
                        {roles.map(role => (
                          <option key={role.id || role.roleId} value={role.id || role.roleId}>
                            {role.roleName}
                          </option>
                        ))}
                      </select>
                      <select
                        value={selectedStatus}
                        onChange={(e) => handleStatusFilterChange(e.target.value)}
                        className="px-3 py-2 rounded border text-sm"
                        style={{ 
                          backgroundColor: colors.bg,
                          borderColor: colors.inputBorder,
                          color: colors.text
                        }}
                        disabled={loading}
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
                        disabled={loading}
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
                          backgroundColor: colors.bg,
                          borderColor: colors.inputBorder,
                          color: colors.text
                        }}>
                          <option>Department</option>
                          <option>Engineering</option>
                          <option>Marketing</option>
                          <option>Sales</option>
                        </select>
                        <select className="px-2 py-1 rounded border text-xs" style={{ 
                          backgroundColor: colors.bg,
                          borderColor: colors.inputBorder,
                          color: colors.text
                        }}>
                          <option>MFA Status</option>
                          <option>Enabled</option>
                          <option>Disabled</option>
                        </select>
                        <select className="px-2 py-1 rounded border text-xs" style={{ 
                          backgroundColor: colors.bg,
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
                        disabled={loading}
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
                        disabled={loading}
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
                        disabled={loading}
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
                        disabled={loading}
                      >
                        Clear
                      </button>
                    </div>
                  </div>
                )}

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
                            disabled={loading}
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
                          Access Type
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
                            backgroundColor: selectedUsers.includes(user.id) ? colors.selected : 'transparent',
                            cursor: openingModalForUserId === user.id ? 'wait' : 'pointer',
                            opacity: openingModalForUserId === user.id ? 0.7 : 1
                          }}
                          onClick={(e) => {
                            // Only trigger if clicking on the row itself, not on interactive elements
                            if (!e.target.closest('button') && 
                                !e.target.closest('input[type="checkbox"]') &&
                                !e.target.closest('a')) {
                              handleRowClick(user, e);
                            }
                          }}
                        >
                          <td className="p-3" onClick={(e) => e.stopPropagation()}>
                            <input
                              type="checkbox"
                              checked={selectedUsers.includes(user.id)}
                              onChange={() => handleSelectUser(user.id)}
                              className="rounded border-gray-300"
                              style={{ borderColor: colors.border }}
                              disabled={loading || openingModalForUserId === user.id}
                            />
                          </td>
                          <td className="p-3">
                            <div className="flex items-center gap-3">
                              <div 
                                className="w-8 h-8 rounded-full flex items-center justify-center text-white font-medium text-sm"
                                style={{ backgroundColor: user.avatarColor || colors.primary }}
                              >
                                {user.fullName
                                ?.split(' ')
                                .map(n => n[0])
                                .join('')
                                .slice(0, 2) || '??'}
                              </div>
                              <div className="min-w-0">
                                <div className="text-sm font-semibold truncate" style={{ color: colors.text }}>
                                  {user.fullName || 'Unknown User'}
                                </div>
                                <div className="text-xs truncate" style={{ color: colors.textSecondary }}>
                                  @{user.username || 'unknown'}
                                </div>
                              </div>
                            </div>
                          </td>
                          <td className="p-3">
                            <div className="text-sm truncate" style={{ color: colors.text }}>
                              {user.email || 'No email'}
                            </div>
                          </td>
                          <td className="p-3">
                            <div 
                              className="px-2 py-1 rounded-full text-xs font-medium w-fit uppercase"
                              style={getRoleColorStyle(user.role || user.roleId)}
                            >
                              {getRoleDisplayName(user.role || user.roleId)}
                            </div>
                          </td>
                          <td className="p-3">
                            <div 
                              className="px-2 py-1 rounded-full text-xs font-medium w-fit"
                              style={getStatusColorStyle(user.status)}
                            >
                              {getUserStatusDisplayName(user.status)}
                            </div>
                          </td>
                          <td className="p-3">
                            <div className="text-sm" style={{ color: colors.text }}>
                              {formatDateForDisplay(user.lastActive, false)}
                            </div>
                          </td>
                          <td className="p-3">
                            <div className="flex items-center gap-1">
                              <div className="text-sm font-medium" style={{ color: colors.text }}>
                                {user.securityScore || 0}
                              </div>
                              <div className="w-16 h-1 rounded-full bg-gray-200">
                                <div 
                                  className="h-full rounded-full"
                                  style={{ 
                                    width: `${user.securityScore || 0}%`,
                                    backgroundColor: getSecurityColor(user.securityScore)
                                  }}
                                />
                              </div>
                            </div>
                          </td>
                          <td className="p-3" onClick={(e) => e.stopPropagation()}>
                            <div className="flex items-center gap-2">
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  e.preventDefault();
                                  handleViewUserDetails(user);
                                }}
                                className="p-1.5 rounded hover:bg-opacity-50 transition-colors"
                                style={{ backgroundColor: colors.hover }}
                                title="View Details"
                                disabled={loading || openingModalForUserId === user.id}
                              >
                                <Eye size={14} style={{ color: colors.textSecondary }} />
                              </button>
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  e.preventDefault();
                                  handleEditUser(user);
                                }}
                                className="p-1.5 rounded hover:bg-opacity-50 transition-colors"
                                style={{ backgroundColor: colors.hover }}
                                title="Edit User"
                                disabled={loading || openingModalForUserId === user.id}
                              >
                                <Edit size={14} style={{ color: colors.textSecondary }} />
                              </button>
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  e.preventDefault();
                                  handleDeleteUser(user);
                                }}
                                className="p-1.5 rounded hover:bg-opacity-50 transition-colors"
                                style={{ backgroundColor: colors.hover }}
                                title="Delete User"
                                disabled={loading || openingModalForUserId === user.id}
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