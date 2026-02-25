import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import {
  Database, Table, Columns, FileText, Code, Package, Hash, Link, Type,
  Search, Filter, Star, ChevronDown, ChevronRight, ChevronUp, ChevronLeft,
  MoreVertical, Settings, User, Moon, Sun, RefreshCw, Plus, X, Check, SlidersHorizontal,
  Eye, EyeOff, Copy, Download, Upload, Share2, Edit2, Trash2, Play,
  Save, Folder, FolderOpen, Server, Activity, BarChart, Terminal,
  Globe, Lock, Key, Shield, Users, Bell, HelpCircle, AlertCircle,
  Clock, Cpu, HardDrive, Network, Wifi, Bluetooth, Smartphone, Monitor,
  Printer, Inbox, Archive, Cloud, Home, Coffee, Grid, List, Maximize2,
  Minimize2, MoreHorizontal, Send, CheckCircle, XCircle, Info, Layers,
  Box, FolderPlus, FilePlus, GitBranch, Bold, Italic, Image, Table as TableIcon,
  ExternalLink, UploadCloud, DownloadCloud, ShieldCheck, LayoutDashboard,
  BookOpen, Zap, History, Terminal as TerminalIcon,
  ChevronsLeft, ChevronsRight, GripVertical, Circle, Dot, Type as TypeIcon,
  FileCode, ChevronsUp, ChevronsDown, AlertTriangle, Menu, Loader
} from 'lucide-react';
import ApiGenerationModal from '@/components/modals/ApiGenerationModal.js';

// Import OracleSchemaController
import {
  getCurrentSchemaInfo,
  getAllTablesForFrontend,
  getTableDetailsForFrontend,
  getTableData,
  getAllViewsForFrontend,
  getAllProceduresForFrontend,
  getAllFunctionsForFrontend,
  getAllPackagesForFrontend,
  getAllTriggersForFrontend,
  getAllSynonymsForFrontend,
  getAllSequencesForFrontend,
  getAllTypesForFrontend,
  getObjectDetails,
  getObjectDDL,
  handleSchemaBrowserResponse,
  extractObjectDetails,
  extractTableData,
  extractDDL,
  formatBytes,
  formatDateForDisplay,
  getObjectTypeIcon,
  isSupportedForAPIGeneration,
  generateSampleQuery
} from "../controllers/OracleSchemaController.js";

// Enhanced Logger - Disabled in production
const Logger = {
  levels: { DEBUG: 'DEBUG', INFO: 'INFO', WARN: 'WARN', ERROR: 'ERROR' },
  enabled: process.env.NODE_ENV === 'development',
  
  log: (level, component, method, message, data = null, error = null) => {
    if (!Logger.enabled) return;
    
    const timestamp = new Date().toISOString().split('T')[1].split('.')[0];
    const prefix = `[${timestamp}] [${level}] [${component}] ${method}:`;
    
    if (level === Logger.levels.ERROR) {
      console.error(`${prefix} ${message}`, data || '', error || '');
    } else {
      console.log(`${prefix} ${message}`, data || '');
    }
  },
  
  debug: (c, m, msg, d) => Logger.log(Logger.levels.DEBUG, c, m, msg, d),
  info: (c, m, msg, d) => Logger.log(Logger.levels.INFO, c, m, msg, d),
  warn: (c, m, msg, d) => Logger.log(Logger.levels.WARN, c, m, msg, d),
  error: (c, m, msg, e, d) => Logger.log(Logger.levels.ERROR, c, m, msg, d, e)
};

// Simple cache
const objectCache = new Map();
const CACHE_TTL = 5 * 60 * 1000; // 5 minutes

// FilterInput Component
const FilterInput = React.memo(({ 
  filterQuery, 
  selectedOwner, 
  owners,
  onFilterChange, 
  onOwnerChange, 
  onClearFilters,
  colors,
  loading 
}) => {
  const searchInputRef = useRef(null);

  const handleFilterChange = useCallback((e) => {
    onFilterChange(e.target.value);
  }, [onFilterChange]);

  const handleOwnerChange = useCallback((e) => {
    onOwnerChange(e.target.value);
  }, [onOwnerChange]);

  const handleClearFilter = useCallback(() => {
    onFilterChange('');
    setTimeout(() => searchInputRef.current?.focus(), 10);
  }, [onFilterChange]);

  const handleClearAllFilters = useCallback(() => {
    onFilterChange('');
    onOwnerChange('ALL');
    setTimeout(() => searchInputRef.current?.focus(), 10);
  }, [onFilterChange, onOwnerChange]);

  return (
    <>
      <div className="p-3 border-b" style={{ borderColor: colors.border }}>
        <div className="space-y-2">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2" size={14} style={{ color: colors.textSecondary }} />
            <input
              ref={searchInputRef}
              type="text"
              placeholder={loading ? "Loading..." : "Filter objects..."}
              value={filterQuery}
              onChange={handleFilterChange}
              disabled={loading}
              className="w-full pl-10 pr-3 py-2.5 rounded text-sm focus:outline-none"
              style={{ 
                backgroundColor: colors.inputBg,
                border: `1px solid ${colors.inputBorder}`,
                color: colors.text,
                opacity: loading ? 0.6 : 1
              }}
            />
            {filterQuery && !loading && (
              <button 
                onClick={handleClearFilter}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 p-0.5 rounded"
                style={{ backgroundColor: colors.hover }}
              >
                <X size={12} style={{ color: colors.textSecondary }} />
              </button>
            )}
          </div>
          
          {owners && owners.length > 0 && (
            <select
              value={selectedOwner}
              onChange={handleOwnerChange}
              disabled={loading}
              className="w-full px-3 py-2 rounded text-sm focus:outline-none"
              style={{ 
                backgroundColor: colors.bg,
                border: `1px solid ${colors.inputBorder}`,
                color: colors.text,
                opacity: loading ? 0.6 : 1
              }}
            >
              <option value="ALL">All Owners</option>
              {owners.map(owner => (
                <option key={owner} value={owner}>{owner}</option>
              ))}
            </select>
          )}
        </div>
      </div>

      {(filterQuery || selectedOwner !== 'ALL') && !loading && (
        <div className="px-3 py-2 border-b" style={{ borderColor: colors.border, backgroundColor: colors.hover }}>
          <div className="flex items-center justify-between">
            <span className="text-xs" style={{ color: colors.textSecondary }}>
              Filtering {filterQuery && `by: "${filterQuery}"`} {filterQuery && selectedOwner !== 'ALL' && ' • '} 
              {selectedOwner !== 'ALL' && `Owner: ${selectedOwner}`}
            </span>
            <button 
              onClick={handleClearAllFilters}
              className="text-xs px-2 py-1 rounded"
              style={{ backgroundColor: colors.border, color: colors.text }}
            >
              Clear
            </button>
          </div>
        </div>
      )}
    </>
  );
});

FilterInput.displayName = 'FilterInput';

// ObjectTreeSection Component
const ObjectTreeSection = React.memo(({ 
  title, 
  type, 
  objects, 
  isLoading,
  isExpanded, 
  onToggle,
  onSelectObject,
  onLoadSection,
  activeObjectId,
  filterQuery,
  selectedOwner,
  colors,
  getObjectIcon,
  handleContextMenu
}) => {
  
  useEffect(() => {
    if (isExpanded && objects.length === 0 && !isLoading) {
      Logger.debug('ObjectTreeSection', 'useEffect', `Loading ${title} on expand`);
      onLoadSection(type);
    }
  }, [isExpanded, objects.length, isLoading, onLoadSection, type, title]);
  
  const filteredObjects = useMemo(() => {
    if (!filterQuery && selectedOwner === 'ALL') return objects;
    
    const searchLower = filterQuery.toLowerCase();
    
    return objects.filter(obj => {
      const ownerMatch = selectedOwner === 'ALL' || obj.owner === selectedOwner;
      if (!ownerMatch) return false;
      if (!filterQuery) return true;
      
      return (
        (obj.name && obj.name.toLowerCase().includes(searchLower)) ||
        (obj.owner && obj.owner.toLowerCase().includes(searchLower))
      );
    });
  }, [objects, filterQuery, selectedOwner]);
  
  const handleToggle = useCallback(() => {
    onToggle(type);
  }, [onToggle, type]);
  
  const handleObjectClick = useCallback((obj) => {
    onSelectObject(obj, type.slice(0, -1).toUpperCase());
  }, [onSelectObject, type]);
  
  return (
    <div className="mb-1">
      <button
        onClick={handleToggle}
        className="flex items-center justify-between w-full px-2 py-2 hover:bg-opacity-50 transition-colors rounded-sm text-sm font-medium"
        style={{ backgroundColor: colors.hover }}
      >
        <div className="flex items-center gap-2">
          {isLoading ? (
            <Loader size={14} className="animate-spin" style={{ color: colors.textSecondary }} />
          ) : isExpanded ? (
            <ChevronDown size={14} style={{ color: colors.textSecondary }} />
          ) : (
            <ChevronRight size={14} style={{ color: colors.textSecondary }} />
          )}
          {getObjectIcon(type.slice(0, -1))}
          <span className="truncate text-xs sm:text-sm">{title}</span>
        </div>
        <span className="text-xs px-1.5 py-0.5 rounded shrink-0 min-w-6 text-center" style={{ 
          backgroundColor: colors.border,
          color: colors.textSecondary
        }}>
          {objects.length}
        </span>
      </button>
      
      {isExpanded && (
        <div className="ml-6 mt-0.5 space-y-0.5">
          {isLoading ? (
            <div className="px-2 py-3 text-center">
              <Loader className="animate-spin mx-auto" size={14} style={{ color: colors.textSecondary }} />
              <span className="text-xs mt-2 block" style={{ color: colors.textTertiary }}>
                Loading...
              </span>
            </div>
          ) : filteredObjects.length === 0 ? (
            <div className="px-2 py-3 text-center">
              <span className="text-xs" style={{ color: colors.textTertiary }}>
                No objects found
              </span>
            </div>
          ) : (
            filteredObjects.map(obj => (
              <button
                key={obj.id || obj.name}
                onDoubleClick={() => handleObjectClick(obj)}
                onContextMenu={(e) => handleContextMenu(e, obj, type.slice(0, -1).toUpperCase())}
                onClick={() => handleObjectClick(obj)}
                className={`flex items-center justify-between w-full px-2 py-2 rounded-sm cursor-pointer group text-left ${
                  activeObjectId === obj.id ? 'font-medium' : ''
                }`}
                style={{
                  backgroundColor: activeObjectId === obj.id ? colors.selected : 'transparent',
                  color: activeObjectId === obj.id ? colors.primary : colors.text
                }}
              >
                <div className="flex items-center gap-2 min-w-0 flex-1">
                  {getObjectIcon(type.slice(0, -1))}
                  <span className="text-xs sm:text-sm truncate">{obj.name}</span>
                </div>
                {obj.status && obj.status !== 'VALID' && (
                  <AlertCircle size={10} style={{ color: colors.error }} />
                )}
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
});

ObjectTreeSection.displayName = 'ObjectTreeSection';

// Left Sidebar Component
const LeftSidebar = React.memo(({ 
  isLeftSidebarVisible, 
  setIsLeftSidebarVisible,
  filterQuery,
  selectedOwner,
  owners,
  handleFilterChange,
  handleOwnerChange,
  handleClearFilters,
  colors,
  objectTree,
  handleToggleSection,
  handleLoadSection,
  schemaObjects,
  loadingStates,
  activeObject,
  handleObjectSelect,
  getObjectIcon,
  handleContextMenu,
  loading,
  onRefreshSchema,
  schemaInfo
}) => {
  
  const handleCloseSidebar = useCallback(() => {
    setIsLeftSidebarVisible(false);
  }, [setIsLeftSidebarVisible]);
  
  return (
    <div className={`w-full md:w-64 border-r flex flex-col absolute md:relative inset-y-0 left-0 z-40 transform transition-transform duration-300 ease-in-out ${
      isLeftSidebarVisible ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
    }`} style={{ 
      borderColor: colors.border,
      width: '16vw',
      maxWidth: '320px',
      backgroundColor: colors.sidebar
    }}>

      {/* Schema Browser Header */}
      <div className="p-3 border-b" style={{ borderColor: colors.border }}>
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2 flex-1 text-left">
            <Database size={16} style={{ color: colors.primary }} />
            <span className="text-sm font-medium truncate" style={{ color: colors.text }}>
              Schema Browser
            </span>
          </div>
          <div className="flex gap-1">
            {schemaInfo && (
              <span className="text-xs px-2 py-1 rounded" style={{ backgroundColor: colors.hover, color: colors.textSecondary }}>
                {schemaInfo.currentUser || schemaInfo.currentSchema}
              </span>
            )}
            <button 
              className="rounded hover:bg-opacity-50 transition-colors flex items-center justify-center w-8 h-8"
              style={{ backgroundColor: colors.hover }}
              onClick={onRefreshSchema}
              disabled={loading}
            >
              <RefreshCw size={12} style={{ color: colors.textSecondary }} className={loading ? 'animate-spin' : ''} />
            </button>
            <button 
              className="md:hidden rounded hover:bg-opacity-50 transition-colors flex items-center justify-center w-8 h-8"
              style={{ backgroundColor: colors.hover }}
              onClick={handleCloseSidebar}
            >
              <X size={12} style={{ color: colors.textSecondary }} />
            </button>
          </div>
        </div>
      </div>

      {/* Filter Input Component */}
      <FilterInput
        filterQuery={filterQuery}
        selectedOwner={selectedOwner}
        owners={owners}
        onFilterChange={handleFilterChange}
        onOwnerChange={handleOwnerChange}
        onClearFilters={handleClearFilters}
        colors={colors}
        loading={loading}
      />

      {/* Object Tree */}
      <div className="flex-1 overflow-auto p-3">
        {loading && Object.values(schemaObjects).every(arr => arr.length === 0) ? (
          <div className="flex items-center justify-center h-32">
            <RefreshCw className="animate-spin" size={16} style={{ color: colors.textSecondary }} />
          </div>
        ) : (
          <>
            <ObjectTreeSection
              title="Procedures"
              type="procedures"
              objects={schemaObjects.procedures || []}
              isLoading={loadingStates.procedures}
              isExpanded={objectTree.procedures}
              onToggle={handleToggleSection}
              onLoadSection={handleLoadSection}
              onSelectObject={handleObjectSelect}
              activeObjectId={activeObject?.id}
              filterQuery={filterQuery}
              selectedOwner={selectedOwner}
              colors={colors}
              getObjectIcon={getObjectIcon}
              handleContextMenu={handleContextMenu}
            />
            <ObjectTreeSection
              title="Views"
              type="views"
              objects={schemaObjects.views || []}
              isLoading={loadingStates.views}
              isExpanded={objectTree.views}
              onToggle={handleToggleSection}
              onLoadSection={handleLoadSection}
              onSelectObject={handleObjectSelect}
              activeObjectId={activeObject?.id}
              filterQuery={filterQuery}
              selectedOwner={selectedOwner}
              colors={colors}
              getObjectIcon={getObjectIcon}
              handleContextMenu={handleContextMenu}
            />
            <ObjectTreeSection
              title="Functions"
              type="functions"
              objects={schemaObjects.functions || []}
              isLoading={loadingStates.functions}
              isExpanded={objectTree.functions}
              onToggle={handleToggleSection}
              onLoadSection={handleLoadSection}
              onSelectObject={handleObjectSelect}
              activeObjectId={activeObject?.id}
              filterQuery={filterQuery}
              selectedOwner={selectedOwner}
              colors={colors}
              getObjectIcon={getObjectIcon}
              handleContextMenu={handleContextMenu}
            />
            <ObjectTreeSection
              title="Tables"
              type="tables"
              objects={schemaObjects.tables || []}
              isLoading={loadingStates.tables}
              isExpanded={objectTree.tables}
              onToggle={handleToggleSection}
              onLoadSection={handleLoadSection}
              onSelectObject={handleObjectSelect}
              activeObjectId={activeObject?.id}
              filterQuery={filterQuery}
              selectedOwner={selectedOwner}
              colors={colors}
              getObjectIcon={getObjectIcon}
              handleContextMenu={handleContextMenu}
            />
            <ObjectTreeSection
              title="Packages"
              type="packages"
              objects={schemaObjects.packages || []}
              isLoading={loadingStates.packages}
              isExpanded={objectTree.packages}
              onToggle={handleToggleSection}
              onLoadSection={handleLoadSection}
              onSelectObject={handleObjectSelect}
              activeObjectId={activeObject?.id}
              filterQuery={filterQuery}
              selectedOwner={selectedOwner}
              colors={colors}
              getObjectIcon={getObjectIcon}
              handleContextMenu={handleContextMenu}
            />
            <ObjectTreeSection
              title="Synonyms"
              type="synonyms"
              objects={schemaObjects.synonyms || []}
              isLoading={loadingStates.synonyms}
              isExpanded={objectTree.synonyms}
              onToggle={handleToggleSection}
              onLoadSection={handleLoadSection}
              onSelectObject={handleObjectSelect}
              activeObjectId={activeObject?.id}
              filterQuery={filterQuery}
              selectedOwner={selectedOwner}
              colors={colors}
              getObjectIcon={getObjectIcon}
              handleContextMenu={handleContextMenu}
            />
            <ObjectTreeSection
              title="Sequences"
              type="sequences"
              objects={schemaObjects.sequences || []}
              isLoading={loadingStates.sequences}
              isExpanded={objectTree.sequences}
              onToggle={handleToggleSection}
              onLoadSection={handleLoadSection}
              onSelectObject={handleObjectSelect}
              activeObjectId={activeObject?.id}
              filterQuery={filterQuery}
              selectedOwner={selectedOwner}
              colors={colors}
              getObjectIcon={getObjectIcon}
              handleContextMenu={handleContextMenu}
            />
            <ObjectTreeSection
              title="Types"
              type="types"
              objects={schemaObjects.types || []}
              isLoading={loadingStates.types}
              isExpanded={objectTree.types}
              onToggle={handleToggleSection}
              onLoadSection={handleLoadSection}
              onSelectObject={handleObjectSelect}
              activeObjectId={activeObject?.id}
              filterQuery={filterQuery}
              selectedOwner={selectedOwner}
              colors={colors}
              getObjectIcon={getObjectIcon}
              handleContextMenu={handleContextMenu}
            />
            <ObjectTreeSection
              title="Triggers"
              type="triggers"
              objects={schemaObjects.triggers || []}
              isLoading={loadingStates.triggers}
              isExpanded={objectTree.triggers}
              onToggle={handleToggleSection}
              onLoadSection={handleLoadSection}
              onSelectObject={handleObjectSelect}
              activeObjectId={activeObject?.id}
              filterQuery={filterQuery}
              selectedOwner={selectedOwner}
              colors={colors}
              getObjectIcon={getObjectIcon}
              handleContextMenu={handleContextMenu}
            />
          </>
        )}
      </div>
    </div>
  );
});

LeftSidebar.displayName = 'LeftSidebar';

// Mobile Bottom Navigation
const MobileBottomNav = React.memo(({ 
  isLeftSidebarVisible, 
  setIsLeftSidebarVisible, 
  setShowApiModal, 
  toggleTheme,
  isDark,
  colors,
  loading,
  onRefreshSchema
}) => {
  
  const handleOpenSidebar = useCallback(() => {
    setIsLeftSidebarVisible(true);
  }, [setIsLeftSidebarVisible]);
  
  const handleOpenApiModal = useCallback(() => {
    setShowApiModal(true);
  }, [setShowApiModal]);
  
  return (
    <div className="md:hidden fixed bottom-0 left-0 right-0 border-t z-20" style={{ 
      backgroundColor: colors.card,
      borderColor: colors.border
    }}>
      <div className="flex items-center justify-around p-2">
        <button 
          onClick={handleOpenSidebar}
          className="rounded hover:bg-opacity-50 transition-colors flex flex-col items-center justify-center w-14 h-14 p-1"
          style={{ backgroundColor: isLeftSidebarVisible ? colors.selected : 'transparent' }}
          disabled={loading}
        >
          <Database size={16} style={{ color: colors.text }} />
          <span className="text-xs" style={{ color: colors.textSecondary }}>Schema</span>
        </button>

        <button 
          onClick={handleOpenApiModal}
          className="px-3 py-2 rounded text-sm bg-gradient-to-r from-blue-500 via-violet-500 to-blue-500 hover:opacity-90 font-medium flex items-center gap-2"
          style={{ color: 'white' }}
          disabled={loading}
        >
          <Code size={16} style={{ color: 'white' }} />
          <span className="text-xs text-white">Generate</span>
        </button>
        
        <button 
          onClick={onRefreshSchema}
          className="flex flex-col items-center p-2 rounded hover:bg-opacity-50 transition-colors"
          disabled={loading}
        >
          <RefreshCw size={16} style={{ color: colors.text }} className={loading ? 'animate-spin' : ''} />
          <span className="text-xs mt-1" style={{ color: colors.textSecondary }}>Refresh</span>
        </button>
        
        <button 
          onClick={toggleTheme}
          className="flex flex-col items-center p-2 rounded hover:bg-opacity-50 transition-colors"
        >
          {isDark ? <Sun size={16} style={{ color: colors.text }} /> : <Moon size={16} style={{ color: colors.text }} />}
          <span className="text-xs mt-1" style={{ color: colors.textSecondary }}>Theme</span>
        </button>
      </div>
    </div>
  );
});

MobileBottomNav.displayName = 'MobileBottomNav';

const SchemaBrowser = ({ theme, isDark, toggleTheme, authToken }) => {
  // Colors
  const colors = useMemo(() => isDark ? {
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
    inputBg: 'rgb(41 53 72 / 19%)',
    inputBorder: 'rgb(51 65 85 / 19%)',
    tableHeader: 'rgb(41 53 72 / 19%)',
    tableRow: 'rgb(41 53 72 / 19%)',
    tableRowHover: 'rgb(45 46 72 / 33%)',
    gridRowEven: 'rgb(41 53 72 / 19%)',
    gridRowOdd: 'rgb(45 46 72 / 33%)',
    gridHeader: 'rgb(41 53 72 / 19%)',
    gridBorder: 'rgb(51 65 85 / 19%)',
    dropdownBg: 'rgb(41 53 72 / 19%)',
    dropdownBorder: 'rgb(51 65 85 / 19%)',
    codeBg: 'rgb(41 53 72 / 19%)',
    objectType: {
      table: 'rgb(96 165 250)',
      view: 'rgb(52 211 153)',
      procedure: 'rgb(167 139 250)',
      function: 'rgb(251 191 36)',
      package: 'rgb(148 163 184)',
      sequence: 'rgb(100 116 139)',
      synonym: 'rgb(34 211 238)',
      type: 'rgb(139 92 246)',
      trigger: 'rgb(244 114 182)'
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
    inputBg: '#ffffff',
    inputBorder: '#e2e8f0',
    tableHeader: '#f8fafc',
    tableRow: '#ffffff',
    tableRowHover: '#f8fafc',
    gridRowEven: '#ffffff',
    gridRowOdd: '#f8fafc',
    gridHeader: '#f1f5f9',
    gridBorder: '#e2e8f0',
    dropdownBg: '#ffffff',
    dropdownBorder: '#e2e8f0',
    codeBg: '#f1f5f9',
    objectType: {
      table: '#3b82f6',
      view: '#10b981',
      procedure: '#8b5cf6',
      function: '#f59e0b',
      package: '#6b7280',
      sequence: '#64748b',
      synonym: '#06b6d4',
      type: '#6366f1',
      trigger: '#ec4899'
    }
  }, [isDark]);

  // State
  const [showApiModal, setShowApiModal] = useState(false);
  const [selectedForApiGeneration, setSelectedForApiGeneration] = useState(null);
  const [isLeftSidebarVisible, setIsLeftSidebarVisible] = useState(false);
  const [filterQuery, setFilterQuery] = useState('');
  const [selectedOwner, setSelectedOwner] = useState('ALL');
  const [owners, setOwners] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [schemaInfo, setSchemaInfo] = useState(null);
  const [hasAutoSelected, setHasAutoSelected] = useState(false);
  
  // Schema objects state
  const [schemaObjects, setSchemaObjects] = useState({
    tables: [],
    views: [],
    procedures: [],
    functions: [],
    packages: [],
    sequences: [],
    synonyms: [],
    types: [],
    triggers: []
  });
  
  // Loading states for each object type
  const [loadingStates, setLoadingStates] = useState({
    procedures: false,
    views: false,
    functions: false,
    tables: false,
    packages: false,
    sequences: false,
    synonyms: false,
    types: false,
    triggers: false
  });
  
  // Object tree expanded state
  const [objectTree, setObjectTree] = useState({
    procedures: false,
    views: false,
    functions: false,
    tables: false,
    packages: false,
    sequences: false,
    synonyms: false,
    types: false,
    triggers: false
  });
  
  const [activeObject, setActiveObject] = useState(null);
  const [activeTab, setActiveTab] = useState('columns');
  const [tabs, setTabs] = useState([]);
  const [tableData, setTableData] = useState(null);
  const [objectDDL, setObjectDDL] = useState('');
  const [objectDetails, setObjectDetails] = useState(null);
  const [tableDataLoading, setTableDataLoading] = useState(false);
  
  // Context menu
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuPosition, setContextMenuPosition] = useState({ x: 0, y: 0 });
  const [contextObject, setContextObject] = useState(null);
  
  // Data view state
  const [dataView, setDataView] = useState({
    page: 1,
    pageSize: 50,
    sortColumn: '',
    sortDirection: 'ASC'
  });

  // Get Object Icon
  const getObjectIcon = useCallback((type) => {
    const objectType = type.toLowerCase();
    const iconColor = colors.objectType[objectType] || colors.textSecondary;
    const iconProps = { size: 14, style: { color: iconColor } };
    
    switch(objectType) {
      case 'procedure': return <Terminal {...iconProps} />;
      case 'view': return <FileText {...iconProps} />;
      case 'function': return <Code {...iconProps} />;
      case 'table': return <Table {...iconProps} />;
      case 'package': return <Package {...iconProps} />;
      case 'sequence': return <Hash {...iconProps} />;
      case 'synonym': return <Link {...iconProps} />;
      case 'type': return <Type {...iconProps} />;
      case 'trigger': return <Zap {...iconProps} />;
      default: return <Database {...iconProps} />;
    }
  }, [colors]);

  // Load schema info only (1 API call on page load)
  const loadSchemaInfo = useCallback(async () => {
    if (!authToken) {
      setError('Authentication required');
      return;
    }

    Logger.info('SchemaBrowser', 'loadSchemaInfo', 'Loading schema info');
    setLoading(true);
    setError(null);

    try {
      const response = await getCurrentSchemaInfo(authToken);
      const data = handleSchemaBrowserResponse(response);
      
      Logger.info('SchemaBrowser', 'loadSchemaInfo', `Connected as: ${data.currentUser || data.currentSchema}`);
      
      setSchemaInfo(data);
      
      if (data.currentUser || data.currentSchema) {
        setOwners([data.currentUser || data.currentSchema]);
      }

    } catch (err) {
      Logger.error('SchemaBrowser', 'loadSchemaInfo', 'Error', err);
      setError(`Failed to connect: ${err.message}`);
    } finally {
      setLoading(false);
    }
  }, [authToken]);

  // Load object type on demand (lazy loading)
  const loadObjectType = useCallback(async (type) => {
    if (!authToken) return;
    if (loadingStates[type]) return;
    
    const cacheKey = `${type}_${authToken.substring(0, 10)}`;
    const cached = objectCache.get(cacheKey);
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      Logger.debug('SchemaBrowser', 'loadObjectType', `Loading ${type} from cache (${cached.data.length} items)`);
      setSchemaObjects(prev => ({ ...prev, [type]: cached.data }));
      return;
    }
    
    Logger.info('SchemaBrowser', 'loadObjectType', `Loading ${type} from API`);
    
    setLoadingStates(prev => ({ ...prev, [type]: true }));
    
    try {
      let response;
      switch(type) {
        case 'procedures':
          response = await getAllProceduresForFrontend(authToken);
          break;
        case 'views':
          response = await getAllViewsForFrontend(authToken);
          break;
        case 'functions':
          response = await getAllFunctionsForFrontend(authToken);
          break;
        case 'tables':
          response = await getAllTablesForFrontend(authToken);
          break;
        case 'packages':
          response = await getAllPackagesForFrontend(authToken);
          break;
        case 'sequences':
          response = await getAllSequencesForFrontend(authToken);
          break;
        case 'synonyms':
          response = await getAllSynonymsForFrontend(authToken);
          break;
        case 'types':
          response = await getAllTypesForFrontend(authToken);
          break;
        case 'triggers':
          response = await getAllTriggersForFrontend(authToken);
          break;
        default:
          return;
      }
      
      let data = [];
      if (response && response.data) {
        data = response.data;
      } else {
        const processed = handleSchemaBrowserResponse(response);
        data = processed.data || [];
      }
      
      Logger.info('SchemaBrowser', 'loadObjectType', `Loaded ${data.length} ${type}`);
      
      objectCache.set(cacheKey, { data, timestamp: Date.now() });
      
      setSchemaObjects(prev => ({ ...prev, [type]: data }));
      
      const newOwners = new Set(owners);
      data.forEach(obj => {
        if (obj.owner) newOwners.add(obj.owner);
      });
      setOwners(Array.from(newOwners).sort());
      
    } catch (err) {
      Logger.error('SchemaBrowser', 'loadObjectType', `Error loading ${type}`, err);
    } finally {
      setLoadingStates(prev => ({ ...prev, [type]: false }));
    }
  }, [authToken, loadingStates, owners]);

  // Get first schema object for auto-select
  const getFirstSchemaObject = useCallback(() => {
    const objectTypes = [
      { type: 'TABLE', key: 'tables' },
      { type: 'VIEW', key: 'views' },
      { type: 'PROCEDURE', key: 'procedures' },
      { type: 'FUNCTION', key: 'functions' },
      { type: 'PACKAGE', key: 'packages' },
      { type: 'SEQUENCE', key: 'sequences' },
      { type: 'SYNONYM', key: 'synonyms' },
      { type: 'TYPE', key: 'types' },
      { type: 'TRIGGER', key: 'triggers' }
    ];
    
    for (const objType of objectTypes) {
      const objects = schemaObjects[objType.key] || [];
      if (objects.length > 0) {
        return { object: objects[0], type: objType.type };
      }
    }
    return null;
  }, [schemaObjects]);

  // Handle load section
  const handleLoadSection = useCallback((type) => {
    loadObjectType(type);
  }, [loadObjectType]);

  // Handle toggle section
  const handleToggleSection = useCallback((type) => {
    setObjectTree(prev => ({ ...prev, [type]: !prev[type] }));
  }, []);

  // Load table data
  const loadTableData = useCallback(async (tableName) => {
    if (!authToken || !tableName) return;
    
    setTableDataLoading(true);
    try {
      const params = {
        tableName,
        page: dataView.page - 1,
        pageSize: dataView.pageSize,
        sortColumn: dataView.sortColumn || undefined,
        sortDirection: dataView.sortDirection
      };
      
      const response = await getTableData(authToken, params);
      const processed = handleSchemaBrowserResponse(response);
      setTableData(extractTableData(processed));
    } catch (err) {
      Logger.error('SchemaBrowser', 'loadTableData', `Error loading data for ${tableName}`, err);
    } finally {
      setTableDataLoading(false);
    }
  }, [authToken, dataView]);

  // Handle context menu
  const handleContextMenu = useCallback((e, object, type) => {
    e.preventDefault();
    e.stopPropagation();
    
    setContextObject({ ...object, type });
    setContextMenuPosition({ x: e.clientX, y: e.clientY });
    setShowContextMenu(true);
  }, []);

  // Handle filter changes
  const handleFilterChange = useCallback((value) => {
    setFilterQuery(value);
  }, []);

  const handleOwnerChange = useCallback((value) => {
    setSelectedOwner(value);
  }, []);

  const handleClearFilters = useCallback(() => {
    setFilterQuery('');
    setSelectedOwner('ALL');
  }, []);

  // Handle refresh
  const handleRefresh = useCallback(async () => {
    objectCache.clear();
    await loadSchemaInfo();
    setSchemaObjects({
      tables: [],
      views: [],
      procedures: [],
      functions: [],
      packages: [],
      sequences: [],
      synonyms: [],
      types: [],
      triggers: []
    });
    setObjectTree({
      tables: true,
      views: false,
      procedures: false,
      functions: false,
      packages: true,
      sequences: false,
      synonyms: false,
      types: false,
      triggers: false
    });
  }, [loadSchemaInfo]);

  // Handle copy to clipboard
  const handleCopyToClipboard = useCallback(async (text, label = 'content') => {
    try {
      await navigator.clipboard.writeText(text);
      Logger.info('SchemaBrowser', 'handleCopyToClipboard', `Copied ${label} to clipboard`);
    } catch (error) {
      Logger.error('SchemaBrowser', 'handleCopyToClipboard', `Failed to copy ${label}`, error);
    }
  }, []);

  // Handle page change
  const handlePageChange = useCallback((newPage) => {
    setDataView(prev => ({ ...prev, page: newPage }));
  }, []);

  // Handle page size change
  const handlePageSizeChange = useCallback((newSize) => {
    setDataView(prev => ({ ...prev, pageSize: newSize, page: 1 }));
  }, []);

  // Handle sort change
  const handleSortChange = useCallback((column, direction) => {
    setDataView(prev => ({ ...prev, sortColumn: column, sortDirection: direction, page: 1 }));
  }, []);

  // Initialize
  useEffect(() => {
    loadSchemaInfo();
  }, [loadSchemaInfo]);

  // Auto-select first object
  useEffect(() => {
    const hasObjects = Object.values(schemaObjects).some(arr => arr.length > 0);
    if (hasObjects && !activeObject && !hasAutoSelected) {
      const firstObjectData = getFirstSchemaObject();
      if (firstObjectData) {
        setHasAutoSelected(true);
        handleObjectSelect(firstObjectData.object, firstObjectData.type);
      }
    }
  }, [schemaObjects, activeObject, hasAutoSelected, getFirstSchemaObject]);

  // Update table data when dataView changes
  useEffect(() => {
    if (activeObject?.type === 'TABLE' && activeObject?.name) {
      loadTableData(activeObject.name);
    }
  }, [dataView.page, dataView.pageSize, dataView.sortColumn, dataView.sortDirection, activeObject, loadTableData]);

  // Close context menu on outside click
  useEffect(() => {
    const handleClickOutside = () => setShowContextMenu(false);
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, []);

  // Render Columns Tab
  const renderColumnsTab = () => {
    const columns = objectDetails?.columns || 
                    objectDetails?.targetDetails?.columns || 
                    activeObject?.columns || 
                    [];
    
    if (!columns || columns.length === 0) {
      return (
        <div className="flex-1 overflow-auto p-4">
          <div className="text-center" style={{ color: colors.textSecondary }}>
            No columns found
          </div>
        </div>
      );
    }
    
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded" style={{ borderColor: colors.gridBorder, backgroundColor: colors.card }}>
          <div className="p-2 border-b" style={{ borderColor: colors.gridBorder }}>
            <div className="text-sm font-medium" style={{ color: colors.text }}>
              Columns ({columns.length})
            </div>
          </div>
          <div className="overflow-auto">
            <table className="w-full">
              <thead style={{ backgroundColor: colors.tableHeader }}>
                <tr>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>#</th>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Column</th>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Type</th>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Nullable</th>
                </tr>
              </thead>
              <tbody>
                {columns.map((col, i) => (
                  <tr key={col.name || col.COLUMN_NAME || i} style={{ 
                    backgroundColor: i % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                    borderBottom: `1px solid ${colors.gridBorder}`
                  }}>
                    <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>{col.position || col.POSITION || i + 1}</td>
                    <td className="p-2 text-xs font-medium" style={{ color: colors.text }}>{col.name || col.COLUMN_NAME}</td>
                    <td className="p-2 text-xs" style={{ color: colors.text }}>{col.data_type || col.DATA_TYPE || col.type}</td>
                    <td className="p-2 text-xs">
                      <span className={`px-2 py-0.5 rounded text-xs ${
                        (col.nullable === 'Y' || col.nullable === true || col.NULLABLE === 'Y') ? 
                        'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'
                      }`}>
                        {(col.nullable === 'Y' || col.nullable === true || col.NULLABLE === 'Y') ? 'Y' : 'N'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  };

  // Render Data Tab
  const renderDataTab = () => {
    const data = tableData?.rows || [];
    const columns = tableData?.columns || objectDetails?.columns || activeObject?.columns || [];
    
    return (
      <div className="flex-1 flex flex-col">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 p-2 border-b" style={{ borderColor: colors.border }}>
          <div className="flex items-center gap-2">
            <button 
              className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2"
              style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              onClick={() => activeObject && loadTableData(activeObject.name)}
              disabled={tableDataLoading}
            >
              {tableDataLoading ? <RefreshCw size={12} className="animate-spin" /> : <Play size={12} />}
              <span>Execute</span>
            </button>
            <select 
              className="px-2 py-1 border rounded text-sm"
              style={{ backgroundColor: colors.card, borderColor: colors.border, color: colors.text }}
              value={dataView.pageSize}
              onChange={(e) => handlePageSizeChange(parseInt(e.target.value))}
            >
              <option value="25">25 rows</option>
              <option value="50">50 rows</option>
              <option value="100">100 rows</option>
            </select>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-xs" style={{ color: colors.textSecondary }}>
              Page {tableData?.page || 1} of {tableData?.totalPages || 1}
            </span>
            <button 
              className="p-1 rounded hover:bg-opacity-50"
              style={{ backgroundColor: colors.hover }}
              onClick={() => handlePageChange(dataView.page - 1)}
              disabled={!tableData || dataView.page <= 1}
            >
              <ChevronLeft size={14} />
            </button>
            <button 
              className="p-1 rounded hover:bg-opacity-50"
              style={{ backgroundColor: colors.hover }}
              onClick={() => handlePageChange(dataView.page + 1)}
              disabled={!tableData || dataView.page >= (tableData?.totalPages || 1)}
            >
              <ChevronRight size={14} />
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-auto">
          {tableDataLoading ? (
            <div className="flex justify-center p-8">
              <Loader className="animate-spin" size={24} style={{ color: colors.textSecondary }} />
            </div>
          ) : (
            <div className="border rounded overflow-auto" style={{ borderColor: colors.gridBorder }}>
              <table className="w-full">
                <thead style={{ backgroundColor: colors.tableHeader }}>
                  <tr>
                    {columns.map(col => (
                      <th 
                        key={col.name || col.COLUMN_NAME} 
                        className="text-left p-2 text-xs cursor-pointer hover:bg-opacity-50"
                        onClick={() => handleSortChange(
                          col.name || col.COLUMN_NAME, 
                          dataView.sortColumn === (col.name || col.COLUMN_NAME) && dataView.sortDirection === 'ASC' ? 'DESC' : 'ASC'
                        )}
                        style={{ color: colors.textSecondary }}
                      >
                        <div className="flex items-center gap-1">
                          {col.name || col.COLUMN_NAME}
                          {dataView.sortColumn === (col.name || col.COLUMN_NAME) && (
                            dataView.sortDirection === 'ASC' ? <ChevronUp size={10} /> : <ChevronDown size={10} />
                          )}
                        </div>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {data.map((row, i) => (
                    <tr key={i} style={{ 
                      backgroundColor: i % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                      borderBottom: `1px solid ${colors.gridBorder}`
                    }}>
                      {columns.map(col => (
                        <td key={col.name || col.COLUMN_NAME} className="p-2 text-xs" style={{ color: colors.text }}>
                          {row[col.name || col.COLUMN_NAME]?.toString() || 
                           (row[col.name || col.COLUMN_NAME] === null ? <span style={{ color: colors.textTertiary }}>NULL</span> : '-')}
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    );
  };

  // Render Parameters Tab
  const renderParametersTab = () => {
    // For synonyms, parameters are in targetDetails.parameters
    // For direct procedures/functions, parameters might be in objectDetails.parameters
    const parameters = objectDetails?.targetDetails?.parameters || 
                       objectDetails?.parameters || 
                       objectDetails?.arguments ||
                       activeObject?.parameters || 
                       [];
    
    if (!parameters || parameters.length === 0) {
      return (
        <div className="flex-1 overflow-auto p-4">
          <div className="text-center" style={{ color: colors.textSecondary }}>
            No parameters found
          </div>
        </div>
      );
    }
    
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded" style={{ borderColor: colors.gridBorder }}>
          <div className="p-2 border-b" style={{ borderColor: colors.gridBorder }}>
            <div className="text-sm font-medium" style={{ color: colors.text }}>
              Parameters ({parameters.length})
            </div>
          </div>
          <div className="overflow-auto">
            <table className="w-full">
              <thead style={{ backgroundColor: colors.tableHeader }}>
                <tr>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>#</th>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Parameter</th>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Type</th>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Mode</th>
                  <th className="text-left p-2 text-xs hidden md:table-cell" style={{ color: colors.textSecondary }}>Data Length</th>
                </tr>
              </thead>
              <tbody>
                {parameters.map((param, i) => (
                  <tr key={param.ARGUMENT_NAME || param.name || i} style={{ 
                    backgroundColor: i % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                    borderBottom: `1px solid ${colors.gridBorder}`
                  }}>
                    <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>{param.POSITION || param.position || i + 1}</td>
                    <td className="p-2 text-xs font-medium" style={{ color: colors.text }}>{param.ARGUMENT_NAME || param.name}</td>
                    <td className="p-2 text-xs" style={{ color: colors.text }}>{param.DATA_TYPE || param.data_type || param.type}</td>
                    <td className="p-2 text-xs">
                      <span className={`px-2 py-0.5 rounded text-xs ${
                        (param.IN_OUT || param.mode || param.in_out) === 'IN' ? 'bg-blue-500/10 text-blue-400' :
                        (param.IN_OUT || param.mode || param.in_out) === 'OUT' ? 'bg-purple-500/10 text-purple-400' :
                        'bg-green-500/10 text-green-400'
                      }`}>
                        {param.IN_OUT || param.mode || param.in_out || 'IN'}
                      </span>
                    </td>
                    <td className="p-2 text-xs hidden md:table-cell" style={{ color: colors.textSecondary }}>
                      {param.DATA_LENGTH || param.data_length || '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  };

  // Render DDL Tab
  const renderDDLTab = () => {
    // For synonyms that point to other objects, show target DDL
    const ddl = objectDDL || 
                objectDetails?.targetDetails?.text || 
                objectDetails?.text || 
                objectDetails?.ddl ||
                activeObject?.text || 
                activeObject?.spec || 
                activeObject?.body || 
                '';
    
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded p-4" style={{ borderColor: colors.border, backgroundColor: colors.codeBg }}>
          <pre className="text-xs font-mono whitespace-pre-wrap overflow-auto" style={{ color: colors.text }}>
            {ddl || 'No DDL available'}
          </pre>
          <div className="mt-2 flex justify-end">
            <button 
              className="px-3 py-1 text-xs rounded hover:bg-opacity-50"
              style={{ backgroundColor: colors.hover, color: colors.text }}
              onClick={() => handleCopyToClipboard(ddl, 'DDL')}
            >
              <Copy size={12} className="inline mr-1" />
              Copy
            </button>
          </div>
        </div>
      </div>
    );
  };

  // Render Constraints Tab
  const renderConstraintsTab = () => {
    const constraints = objectDetails?.constraints || 
                        objectDetails?.targetDetails?.constraints || 
                        activeObject?.constraints || 
                        [];
    
    if (!constraints || constraints.length === 0) {
      return (
        <div className="flex-1 overflow-auto p-4">
          <div className="text-center" style={{ color: colors.textSecondary }}>
            No constraints found
          </div>
        </div>
      );
    }
    
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded" style={{ borderColor: colors.gridBorder }}>
          <div className="p-2 border-b" style={{ borderColor: colors.gridBorder }}>
            <div className="text-sm font-medium" style={{ color: colors.text }}>
              Constraints ({constraints.length})
            </div>
          </div>
          <div className="overflow-auto">
            <table className="w-full">
              <thead style={{ backgroundColor: colors.tableHeader }}>
                <tr>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Name</th>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Type</th>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Status</th>
                </tr>
              </thead>
              <tbody>
                {constraints.map((con, i) => (
                  <tr key={con.name || con.CONSTRAINT_NAME || i} style={{ 
                    backgroundColor: i % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                    borderBottom: `1px solid ${colors.gridBorder}`
                  }}>
                    <td className="p-2 text-xs" style={{ color: colors.text }}>{con.name || con.CONSTRAINT_NAME}</td>
                    <td className="p-2 text-xs">
                      <span className={`px-2 py-0.5 rounded text-xs ${
                        (con.constraint_type || con.CONSTRAINT_TYPE) === 'P' ? 'bg-blue-500/10 text-blue-400' :
                        (con.constraint_type || con.CONSTRAINT_TYPE) === 'R' ? 'bg-purple-500/10 text-purple-400' :
                        (con.constraint_type || con.CONSTRAINT_TYPE) === 'U' ? 'bg-green-500/10 text-green-400' :
                        'bg-yellow-500/10 text-yellow-400'
                      }`}>
                        {con.constraint_type || con.CONSTRAINT_TYPE}
                      </span>
                    </td>
                    <td className="p-2 text-xs">
                      <span className={`px-2 py-0.5 rounded text-xs ${
                        (con.status || con.STATUS) === 'ENABLED' ? 'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'
                      }`}>
                        {con.status || con.STATUS}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  };

  // Render Properties Tab - UPDATED to show all data
const renderPropertiesTab = () => {
  const details = objectDetails || activeObject || {};
  const targetDetails = details.targetDetails;
  
  console.log('Properties Tab - details:', details);
  console.log('Properties Tab - targetDetails:', targetDetails);
  
  // For synonyms, show both synonym properties and target properties
  if (details.objectType === 'SYNONYM' && targetDetails) {
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded p-4 space-y-4" style={{ borderColor: colors.border }}>
          {/* Synonym Properties */}
          <div>
            <h3 className="text-sm font-medium mb-3" style={{ color: colors.text }}>Synonym Properties</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1">
                <div className="text-xs" style={{ color: colors.textSecondary }}>Synonym Name</div>
                <div className="text-sm truncate" style={{ color: colors.text }}>{details.SYNONYM_NAME || details.name || '-'}</div>
              </div>
              <div className="space-y-1">
                <div className="text-xs" style={{ color: colors.textSecondary }}>Owner</div>
                <div className="text-sm truncate" style={{ color: colors.text }}>{details.owner || '-'}</div>
              </div>
              <div className="space-y-1">
                <div className="text-xs" style={{ color: colors.textSecondary }}>Target Owner</div>
                <div className="text-sm truncate" style={{ color: colors.text }}>{details.TARGET_OWNER || targetDetails?.OWNER || '-'}</div>
              </div>
              <div className="space-y-1">
                <div className="text-xs" style={{ color: colors.textSecondary }}>Target Name</div>
                <div className="text-sm truncate" style={{ color: colors.text }}>{details.TARGET_NAME || targetDetails?.OBJECT_NAME || targetDetails?.objectName || '-'}</div>
              </div>
              <div className="space-y-1">
                <div className="text-xs" style={{ color: colors.textSecondary }}>Target Type</div>
                <div className="text-sm truncate" style={{ color: colors.text }}>{details.TARGET_TYPE || targetDetails?.OBJECT_TYPE || targetDetails?.objectType || '-'}</div>
              </div>
              <div className="space-y-1">
                <div className="text-xs" style={{ color: colors.textSecondary }}>Target Status</div>
                <div className="text-sm truncate">
                  <span className={`px-2 py-0.5 rounded text-xs ${
                    (details.TARGET_STATUS || targetDetails?.STATUS) === 'VALID' ? 'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'
                  }`}>
                    {details.TARGET_STATUS || targetDetails?.STATUS || '-'}
                  </span>
                </div>
              </div>
              <div className="space-y-1">
                <div className="text-xs" style={{ color: colors.textSecondary }}>Target Created</div>
                <div className="text-sm truncate" style={{ color: colors.text }}>
                  {details.TARGET_CREATED ? formatDateForDisplay(details.TARGET_CREATED) : 
                   targetDetails?.CREATED ? formatDateForDisplay(targetDetails.CREATED) : '-'}
                </div>
              </div>
              <div className="space-y-1">
                <div className="text-xs" style={{ color: colors.textSecondary }}>Target Modified</div>
                <div className="text-sm truncate" style={{ color: colors.text }}>
                  {details.TARGET_MODIFIED ? formatDateForDisplay(details.TARGET_MODIFIED) : 
                   targetDetails?.LAST_DDL_TIME ? formatDateForDisplay(targetDetails.LAST_DDL_TIME) : '-'}
                </div>
              </div>
              <div className="space-y-1">
                <div className="text-xs" style={{ color: colors.textSecondary }}>DB Link</div>
                <div className="text-sm truncate" style={{ color: colors.text }}>{details.DB_LINK || '-'}</div>
              </div>
              {details.TARGET_TEMPORARY && (
                <div className="space-y-1">
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Target Temporary</div>
                  <div className="text-sm truncate" style={{ color: colors.text }}>{details.TARGET_TEMPORARY}</div>
                </div>
              )}
              {details.TARGET_GENERATED && (
                <div className="space-y-1">
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Target Generated</div>
                  <div className="text-sm truncate" style={{ color: colors.text }}>{details.TARGET_GENERATED}</div>
                </div>
              )}
              {details.TARGET_SECONDARY && (
                <div className="space-y-1">
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Target Secondary</div>
                  <div className="text-sm truncate" style={{ color: colors.text }}>{details.TARGET_SECONDARY}</div>
                </div>
              )}
            </div>
          </div>

          {/* Target Object Properties */}
          {targetDetails && (
            <div className="border-t pt-4" style={{ borderColor: colors.border }}>
              <h3 className="text-sm font-medium mb-3" style={{ color: colors.text }}>Target Object Properties</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Object Name</div>
                  <div className="text-sm truncate" style={{ color: colors.text }}>{targetDetails.OBJECT_NAME || targetDetails.objectName || targetDetails.TABLE_NAME || '-'}</div>
                </div>
                <div className="space-y-1">
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Owner</div>
                  <div className="text-sm truncate" style={{ color: colors.text }}>{targetDetails.OWNER || targetDetails.owner || '-'}</div>
                </div>
                <div className="space-y-1">
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Object Type</div>
                  <div className="text-sm truncate" style={{ color: colors.text }}>{targetDetails.OBJECT_TYPE || targetDetails.objectType || '-'}</div>
                </div>
                <div className="space-y-1">
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Status</div>
                  <div className="text-sm truncate">
                    <span className={`px-2 py-0.5 rounded text-xs ${
                      (targetDetails.STATUS || targetDetails.OBJECT_STATUS || targetDetails.TABLE_STATUS) === 'VALID' ? 
                      'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'
                    }`}>
                      {targetDetails.STATUS || targetDetails.OBJECT_STATUS || targetDetails.TABLE_STATUS || '-'}
                    </span>
                  </div>
                </div>
                <div className="space-y-1">
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Created</div>
                  <div className="text-sm truncate" style={{ color: colors.text }}>
                    {targetDetails.CREATED ? formatDateForDisplay(targetDetails.CREATED) : '-'}
                  </div>
                </div>
                <div className="space-y-1">
                  <div className="text-xs" style={{ color: colors.textSecondary }}>Last Modified</div>
                  <div className="text-sm truncate" style={{ color: colors.text }}>
                    {targetDetails.LAST_DDL_TIME ? formatDateForDisplay(targetDetails.LAST_DDL_TIME) : '-'}
                  </div>
                </div>
                {targetDetails.parameterCount && (
                  <div className="space-y-1">
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Parameter Count</div>
                    <div className="text-sm truncate" style={{ color: colors.text }}>{targetDetails.parameterCount}</div>
                  </div>
                )}
                {targetDetails.column_count && (
                  <div className="space-y-1">
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Column Count</div>
                    <div className="text-sm truncate" style={{ color: colors.text }}>{targetDetails.column_count}</div>
                  </div>
                )}
                {targetDetails.NUM_ROWS !== undefined && (
                  <div className="space-y-1">
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Row Count</div>
                    <div className="text-sm truncate" style={{ color: colors.text }}>{targetDetails.NUM_ROWS.toLocaleString()}</div>
                  </div>
                )}
                {targetDetails.TABLESPACE_NAME && (
                  <div className="space-y-1">
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Tablespace</div>
                    <div className="text-sm truncate" style={{ color: colors.text }}>{targetDetails.TABLESPACE_NAME}</div>
                  </div>
                )}
                {targetDetails.TEMPORARY && (
                  <div className="space-y-1">
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Temporary</div>
                    <div className="text-sm truncate" style={{ color: colors.text }}>{targetDetails.TEMPORARY}</div>
                  </div>
                )}
                {targetDetails.GENERATED && (
                  <div className="space-y-1">
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Generated</div>
                    <div className="text-sm truncate" style={{ color: colors.text }}>{targetDetails.GENERATED}</div>
                  </div>
                )}
                {targetDetails.SECONDARY && (
                  <div className="space-y-1">
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Secondary</div>
                    <div className="text-sm truncate" style={{ color: colors.text }}>{targetDetails.SECONDARY}</div>
                  </div>
                )}
                {targetDetails.comments && (
                  <div className="space-y-1 col-span-2">
                    <div className="text-xs" style={{ color: colors.textSecondary }}>Comments</div>
                    <div className="text-sm" style={{ color: colors.text }}>{targetDetails.comments}</div>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    );
  }

  // For non-synonym objects, show regular properties
  const properties = [
    { label: 'Name', value: details.name || details.OBJECT_NAME || details.objectName || details.SYNONYM_NAME || '-' },
    { label: 'Owner', value: details.owner || details.OWNER || '-' },
    { label: 'Type', value: details.type || details.OBJECT_TYPE || details.objectType || details.TARGET_TYPE || '-' },
    { label: 'Status', value: details.status || details.STATUS || details.TARGET_STATUS || 'VALID' },
    { label: 'Created', value: details.created || details.CREATED || details.TARGET_CREATED ? formatDateForDisplay(details.created || details.CREATED || details.TARGET_CREATED) : '-' },
    { label: 'Last Modified', value: details.last_ddl_time || details.LAST_DDL_TIME || details.TARGET_MODIFIED ? formatDateForDisplay(details.last_ddl_time || details.LAST_DDL_TIME || details.TARGET_MODIFIED) : '-' },
    ...(details.num_rows || details.NUM_ROWS ? [{ label: 'Row Count', value: (details.num_rows || details.NUM_ROWS).toLocaleString() }] : []),
    ...(details.bytes || details.BYTES ? [{ label: 'Size', value: formatBytes(details.bytes || details.BYTES) }] : []),
    ...(details.comments || details.COMMENTS ? [{ label: 'Comment', value: details.comments || details.COMMENTS }] : [])
  ];

  return (
    <div className="flex-1 overflow-auto">
      <div className="border rounded p-4" style={{ borderColor: colors.border }}>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {properties.map((prop, i) => (
            <div key={i} className="space-y-1">
              <div className="text-xs" style={{ color: colors.textSecondary }}>{prop.label}</div>
              <div className="text-sm truncate" style={{ color: colors.text }}>{prop.value || '-'}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

  // Render Definition Tab (for views/triggers)
  const renderDefinitionTab = () => {
    return renderDDLTab();
  };

  // Render Spec Tab (for packages)
  const renderSpecTab = () => {
    return renderDDLTab();
  };

  // Render Body Tab (for packages)
  const renderBodyTab = () => {
    const body = objectDetails?.body || objectDetails?.targetDetails?.body || '';
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded p-4" style={{ borderColor: colors.border, backgroundColor: colors.codeBg }}>
          <pre className="text-xs font-mono whitespace-pre-wrap overflow-auto" style={{ color: colors.text }}>
            {body || 'No package body available'}
          </pre>
          <div className="mt-2 flex justify-end">
            <button 
              className="px-3 py-1 text-xs rounded hover:bg-opacity-50"
              style={{ backgroundColor: colors.hover, color: colors.text }}
              onClick={() => handleCopyToClipboard(body, 'body')}
            >
              <Copy size={12} className="inline mr-1" />
              Copy
            </button>
          </div>
        </div>
      </div>
    );
  };

  // Render Attributes Tab (for types)
  const renderAttributesTab = () => {
    const attributes = objectDetails?.attributes || 
                       objectDetails?.targetDetails?.attributes || 
                       activeObject?.attributes || 
                       [];
    
    if (!attributes || attributes.length === 0) {
      return (
        <div className="flex-1 overflow-auto p-4">
          <div className="text-center" style={{ color: colors.textSecondary }}>
            No attributes found
          </div>
        </div>
      );
    }
    
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded" style={{ borderColor: colors.gridBorder }}>
          <div className="p-2 border-b" style={{ borderColor: colors.gridBorder }}>
            <div className="text-sm font-medium" style={{ color: colors.text }}>
              Attributes ({attributes.length})
            </div>
          </div>
          <div className="overflow-auto">
            <table className="w-full">
              <thead style={{ backgroundColor: colors.tableHeader }}>
                <tr>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>#</th>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Attribute</th>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Type</th>
                </tr>
              </thead>
              <tbody>
                {attributes.map((attr, i) => (
                  <tr key={attr.name || attr.ATTR_NAME || i} style={{ 
                    backgroundColor: i % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                    borderBottom: `1px solid ${colors.gridBorder}`
                  }}>
                    <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>{attr.position || attr.POSITION || i + 1}</td>
                    <td className="p-2 text-xs font-medium" style={{ color: colors.text }}>{attr.name || attr.ATTR_NAME}</td>
                    <td className="p-2 text-xs" style={{ color: colors.text }}>{attr.type || attr.DATA_TYPE || attr.ATTR_TYPE_NAME}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  };

  // Get Tabs for Object Type - UPDATED to handle synonyms correctly
  const getTabsForObject = useCallback((type, objectDetails) => {
    const objectType = type?.toUpperCase();
    
    // If it's a synonym, check what it points to
    if (objectType === 'SYNONYM' && objectDetails?.targetDetails) {
      const targetType = objectDetails.targetDetails.OBJECT_TYPE || objectDetails.targetDetails.objectType;
      
      // Return tabs based on the target object type
      switch(targetType) {
        case 'TABLE':
          return ['Columns', 'Data', 'Constraints', 'DDL', 'Properties'];
        case 'VIEW':
          return ['Definition', 'Columns', 'Properties'];
        case 'PROCEDURE':
          return ['Parameters', 'DDL', 'Properties'];
        case 'FUNCTION':
          return ['Parameters', 'DDL', 'Properties'];
        case 'PACKAGE':
          return ['Spec', 'Body', 'Properties'];
        case 'SEQUENCE':
          return ['DDL', 'Properties'];
        case 'TYPE':
          return ['Attributes', 'Properties'];
        case 'TRIGGER':
          return ['Definition', 'Properties'];
        default:
          return ['Properties'];
      }
    }
    
    // For non-synonym objects, return tabs based on their own type
    switch(objectType) {
      case 'TABLE':
        return ['Columns', 'Data', 'Constraints', 'DDL', 'Properties'];
      case 'VIEW':
        return ['Definition', 'Columns', 'Properties'];
      case 'PROCEDURE':
        return ['Parameters', 'DDL', 'Properties'];
      case 'FUNCTION':
        return ['Parameters', 'DDL', 'Properties'];
      case 'PACKAGE':
        return ['Spec', 'Body', 'Properties'];
      case 'SEQUENCE':
        return ['DDL', 'Properties'];
      case 'SYNONYM':
        return ['Properties']; // Default for synonyms without target details
      case 'TYPE':
        return ['Attributes', 'Properties'];
      case 'TRIGGER':
        return ['Definition', 'Properties'];
      default:
        return ['Properties'];
    }
  }, []);


  // Add this helper function near the top of your component, after the imports
const extractDetailsFromResponse = (response) => {
  if (!response) return {};
  
  // If response has a data property, use that
  if (response.data) {
    return response.data;
  }
  
  // If response is already the data object
  return response;
};

 // Handle Object Select - UPDATED with better error handling
const handleObjectSelect = useCallback(async (object, type) => {
  if (!authToken || !object) return;

  Logger.info('SchemaBrowser', 'handleObjectSelect', `Selecting ${object.name} (${type})`);
  setActiveObject(object);
  setSelectedForApiGeneration(object);
  
  const tabId = `${type}_${object.id || object.name}`;
  const existingTab = tabs.find(t => t.id === tabId);
  
  if (existingTab) {
    setTabs(tabs.map(t => ({ ...t, isActive: t.id === tabId })));
  } else {
    setTabs(prev => [...prev.slice(-4), {
      id: tabId,
      name: object.name,
      type,
      objectId: object.id || object.name,
      isActive: true
    }].map(t => ({ ...t, isActive: t.id === tabId })));
  }

  try {
    // Load object details
    Logger.debug('SchemaBrowser', 'handleObjectSelect', `Loading details for ${object.name}`);
    const response = await getObjectDetails(authToken, { objectType: type, objectName: object.name });
    
    // Log the raw response to see what we're getting
    console.log('Raw API Response:', response);
    
    // Handle the response properly
    const processedResponse = handleSchemaBrowserResponse(response);
    console.log('Processed Response:', processedResponse);
    
    // The data might be in processedResponse.data or directly in processedResponse
    const responseData = processedResponse.data || processedResponse;
    console.log('Response Data:', responseData);
    
    // Set the object details directly from the response data
    setObjectDetails(responseData);
    
    // Determine what type of object we're dealing with
    const upperType = type.toUpperCase();
    let effectiveType = upperType;
    let targetType = null;
    let targetName = object.name;
    let targetOwner = null;
    
    // If it's a synonym with target details, check what it points to
    if (upperType === 'SYNONYM' && responseData?.targetDetails) {
      targetType = responseData.targetDetails.OBJECT_TYPE || responseData.targetDetails.objectType;
      targetName = responseData.TARGET_NAME || object.name;
      targetOwner = responseData.TARGET_OWNER || responseData.targetDetails.OWNER;
      
      if (targetType) {
        effectiveType = targetType;
        console.log(`Synonym points to ${targetType}: ${targetOwner}.${targetName}`);
      }
    }
    
    // Set default tab based on effective type
    switch(effectiveType) {
      case 'TABLE':
        setActiveTab('columns');
        if (upperType === 'SYNONYM' && targetType === 'TABLE') {
          // For synonyms pointing to tables, load table data using target name
          await loadTableData(targetName);
        } else {
          await loadTableData(object.name);
        }
        break;
      case 'VIEW':
        setActiveTab('definition');
        break;
      case 'PROCEDURE':
      case 'FUNCTION':
        setActiveTab('parameters');
        break;
      case 'PACKAGE':
        setActiveTab('spec');
        break;
      case 'TRIGGER':
        setActiveTab('definition');
        break;
      default:
        setActiveTab('properties');
    }
    
    // Load DDL for appropriate types - with error handling
    const ddlTypes = ['TABLE', 'VIEW', 'PROCEDURE', 'FUNCTION', 'PACKAGE', 'TRIGGER', 'SEQUENCE'];
    if (ddlTypes.includes(effectiveType)) {
      try {
        console.log(`Fetching DDL for ${effectiveType}: ${targetName}`);
        const ddlResponse = await getObjectDDL(authToken, { 
          objectType: effectiveType.toLowerCase(), 
          objectName: targetName 
        });
        console.log('DDL Response:', ddlResponse);
        
        const processedDDL = handleSchemaBrowserResponse(ddlResponse);
        console.log('Processed DDL:', processedDDL);
        
        const ddlData = processedDDL.data || processedDDL;
        // Extract DDL text - might be in different formats
        let ddlText = '';
        if (typeof ddlData === 'string') {
          ddlText = ddlData;
        } else if (ddlData?.ddl) {
          ddlText = ddlData.ddl;
        } else if (ddlData?.text) {
          ddlText = ddlData.text;
        } else if (ddlData?.sql) {
          ddlText = ddlData.sql;
        } else {
          ddlText = JSON.stringify(ddlData, null, 2);
        }
        setObjectDDL(ddlText);
      } catch (ddlError) {
        console.error('Error fetching DDL:', ddlError);
        setObjectDDL('-- DDL not available for this object');
      }
    }
    
  } catch (err) {
    Logger.error('SchemaBrowser', 'handleObjectSelect', `Error loading details for ${object.name}`, err);
    console.error('Error details:', err);
  }

  if (window.innerWidth < 768) {
    setIsLeftSidebarVisible(false);
  }
}, [authToken, tabs, loadTableData]);

  // Render tab content based on active tab
  const renderTabContent = () => {
    if (!activeObject) {
      return (
        <div className="h-full flex flex-col items-center justify-center p-4">
          <Database size={48} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mb-4" />
          <p className="text-sm text-center" style={{ color: colors.textSecondary }}>
            Select an object from the schema browser to view details
          </p>
        </div>
      );
    }

    switch(activeTab.toLowerCase()) {
      case 'columns':
        return renderColumnsTab();
      case 'data':
        return renderDataTab();
      case 'parameters':
        return renderParametersTab();
      case 'constraints':
        return renderConstraintsTab();
      case 'ddl':
      case 'definition':
        return renderDDLTab();
      case 'spec':
        return renderSpecTab();
      case 'body':
        return renderBodyTab();
      case 'attributes':
        return renderAttributesTab();
      case 'properties':
        return renderPropertiesTab();
      default:
        return renderPropertiesTab();
    }
  };

  // Render context menu
  const renderContextMenu = () => {
    if (!showContextMenu || !contextObject) return null;

    const isMobile = window.innerWidth < 768;
    
    const menuItems = [
      { label: 'Open', icon: <ExternalLink size={14} />, action: () => handleObjectSelect(contextObject, contextObject.type) },
      { separator: true },
      { label: 'Generate API', icon: <Code size={14} />, action: () => {
        setSelectedForApiGeneration(contextObject);
        setShowApiModal(true);
        setShowContextMenu(false);
      }},
      { label: 'Copy DDL', icon: <Copy size={14} />, action: () => {
        handleCopyToClipboard(contextObject.text || '', 'DDL');
        setShowContextMenu(false);
      }},
      { label: 'Properties', icon: <Settings size={14} />, action: () => {
        handleObjectSelect(contextObject, contextObject.type);
        setActiveTab('properties');
        setShowContextMenu(false);
      }},
      { separator: true },
      { label: 'Close', icon: <X size={14} />, action: () => setShowContextMenu(false) }
    ];

    return (
      <div 
        className="fixed z-50 rounded shadow-lg border py-1"
        style={{ 
          backgroundColor: colors.dropdownBg,
          borderColor: colors.dropdownBorder,
          top: isMobile ? '50%' : contextMenuPosition.y,
          left: isMobile ? '50%' : Math.min(contextMenuPosition.x, window.innerWidth - 200),
          transform: isMobile ? 'translate(-50%, -50%)' : 'none',
          width: isMobile ? '90vw' : 'auto',
          minWidth: '160px',
          maxWidth: isMobile ? '300px' : 'none'
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-3 py-2 border-b" style={{ borderColor: colors.border }}>
          <div className="text-xs font-medium truncate" style={{ color: colors.text }}>
            {contextObject.name}
          </div>
          <div className="text-xs truncate" style={{ color: colors.textSecondary }}>
            {contextObject.type}
          </div>
        </div>
        {menuItems.map((item, index) => (
          item.separator ? (
            <div key={`sep-${index}`} className="border-t my-1" style={{ borderColor: colors.border }} />
          ) : (
            <button
              key={item.label}
              onClick={item.action}
              className="w-full px-4 py-3 text-sm text-left hover:bg-opacity-50 transition-colors flex items-center gap-3"
              style={{ backgroundColor: 'transparent', color: colors.text }}
            >
              {item.icon}
              {item.label}
            </button>
          )
        ))}
      </div>
    );
  };

  return (
    <div className="flex flex-col h-screen overflow-hidden" style={{ 
      backgroundColor: colors.bg,
      color: colors.text,
      fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif'
    }}>
      {/* Mobile Header */}
      <div className="md:hidden flex items-center justify-between h-12 px-4 border-b" style={{ 
        backgroundColor: colors.header,
        borderColor: colors.border
      }}>
        <button 
          onClick={() => setIsLeftSidebarVisible(true)}
          className="p-2 rounded hover:bg-opacity-50 transition-colors"
          style={{ backgroundColor: colors.hover }}
        >
          <Menu size={20} style={{ color: colors.text }} />
        </button>
        <span className="text-sm font-medium" style={{ color: colors.text }}>Schema Browser</span>
        <button
          onClick={() => setShowApiModal(true)}
          className="p-2 rounded hover:bg-opacity-50 transition-colors"
          style={{ backgroundColor: colors.hover }}
        >
          <Code size={20} style={{ color: colors.primary }} />
        </button>
      </div>

      {/* Desktop Header */}
      <div className="hidden md:flex items-center justify-between h-10 px-4 border-b" style={{ 
        backgroundColor: colors.header,
        borderColor: colors.border
      }}>
        <span className="text-sm font-medium" style={{ color: colors.text }}>Schema Browser</span>
        <div className="flex items-center gap-2">
          <button 
            onClick={toggleTheme}
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.hover }}
          >
            {isDark ? <Sun size={14} style={{ color: colors.textSecondary }} /> : <Moon size={14} style={{ color: colors.textSecondary }} />}
          </button>
          <button 
            onClick={() => setShowApiModal(true)}
            className="px-3 py-1.5 rounded text-sm bg-gradient-to-r from-blue-500 via-violet-500 to-blue-500 hover:opacity-90 font-medium"
            style={{ color: 'white' }}
          >
            Generate API
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex flex-1 overflow-hidden relative pb-16 md:pb-0">
        {/* Sidebar overlay */}
        {isLeftSidebarVisible && (
          <div 
            className="fixed inset-0 bg-black bg-opacity-50 z-30 md:hidden"
            onClick={() => setIsLeftSidebarVisible(false)}
          />
        )}

        {/* Left Sidebar */}
        <LeftSidebar
          isLeftSidebarVisible={isLeftSidebarVisible}
          setIsLeftSidebarVisible={setIsLeftSidebarVisible}
          filterQuery={filterQuery}
          selectedOwner={selectedOwner}
          owners={owners}
          handleFilterChange={handleFilterChange}
          handleOwnerChange={handleOwnerChange}
          handleClearFilters={handleClearFilters}
          colors={colors}
          objectTree={objectTree}
          handleToggleSection={handleToggleSection}
          handleLoadSection={handleLoadSection}
          schemaObjects={schemaObjects}
          loadingStates={loadingStates}
          activeObject={activeObject}
          handleObjectSelect={handleObjectSelect}
          getObjectIcon={getObjectIcon}
          handleContextMenu={handleContextMenu}
          loading={loading}
          onRefreshSchema={handleRefresh}
          schemaInfo={schemaInfo}
        />

        {/* Main Area */}
        <div className="flex-1 flex flex-col overflow-hidden" style={{ backgroundColor: colors.main }}>
          {/* Tab Bar */}
          {tabs.length > 0 && (
            <div className="flex items-center border-b overflow-x-auto" style={{ 
              backgroundColor: colors.card,
              borderColor: colors.border
            }}>
              {tabs.map(tab => (
                <button
                  key={tab.id}
                  className={`flex items-center gap-2 px-3 py-2 border-r cursor-pointer ${
                    tab.isActive ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    backgroundColor: tab.isActive ? colors.selected : 'transparent',
                    borderRightColor: colors.border,
                    borderTop: tab.isActive ? `2px solid ${colors.tabActive}` : 'none'
                  }}
                  onClick={() => {
                    const allObjects = Object.values(schemaObjects).flat();
                    const found = allObjects.find(obj => (obj.id || obj.name) === tab.objectId);
                    if (found) {
                      handleObjectSelect(found, tab.type);
                    }
                  }}
                >
                  {getObjectIcon(tab.type)}
                  <span className="text-sm truncate max-w-[100px]" style={{ 
                    color: tab.isActive ? colors.tabActive : colors.tabInactive
                  }}>
                    {tab.name}
                  </span>
                </button>
              ))}
            </div>
          )}

          {/* Object Header */}
          {activeObject && (
            <div className="p-3 border-b" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="flex items-center gap-2 flex-wrap">
                {getObjectIcon(activeObject.type)}
                <span className="text-sm font-semibold" style={{ color: colors.text }}>{activeObject.name}</span>
                {activeObject.owner && (
                  <span className="text-xs" style={{ color: colors.textSecondary }}>[{activeObject.owner}]</span>
                )}
                {activeObject.status && (
                  <span className="text-xs px-2 py-0.5 rounded" style={{ 
                    backgroundColor: activeObject.status === 'VALID' ? `${colors.success}20` : `${colors.error}20`,
                    color: activeObject.status === 'VALID' ? colors.success : colors.error
                  }}>
                    {activeObject.status}
                  </span>
                )}
              </div>
            </div>
          )}

          {/* Detail Tabs - Now correctly passing objectDetails */}
          {activeObject && (
            <div className="flex items-center border-b overflow-x-auto" style={{ 
              backgroundColor: colors.card,
              borderColor: colors.border
            }}>
              {getTabsForObject(activeObject.type, objectDetails).map(tab => (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab.toLowerCase())}
                  className={`px-3 sm:px-4 py-2 text-xs sm:text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
                    activeTab === tab.toLowerCase() ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === tab.toLowerCase() ? colors.tabActive : 'transparent',
                    color: activeTab === tab.toLowerCase() ? colors.tabActive : colors.tabInactive,
                    backgroundColor: 'transparent'
                  }}
                >
                  {tab}
                </button>
              ))}
            </div>
          )}

          {/* Tab Content */}
          <div className="flex-1 overflow-auto p-3 sm:p-4" style={{ backgroundColor: colors.card }}>
            {renderTabContent()}
          </div>
        </div>
      </div>

      {/* Mobile Bottom Nav */}
      <MobileBottomNav
        isLeftSidebarVisible={isLeftSidebarVisible}
        setIsLeftSidebarVisible={setIsLeftSidebarVisible}
        setShowApiModal={setShowApiModal}
        toggleTheme={toggleTheme}
        isDark={isDark}
        colors={colors}
        loading={loading}
        onRefreshSchema={handleRefresh}
      />

      {/* Modals */}
      {showContextMenu && renderContextMenu()}

      {showApiModal && (
        <ApiGenerationModal
          isOpen={showApiModal}
          onClose={() => setShowApiModal(false)}
          selectedObject={selectedForApiGeneration || activeObject}
          colors={colors}
          theme={theme}
        />
      )}
    </div>
  );
};

export default SchemaBrowser;