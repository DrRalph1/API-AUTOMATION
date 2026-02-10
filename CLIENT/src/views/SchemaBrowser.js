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
  FileCode, ChevronsUp, ChevronsDown, AlertTriangle, Menu
} from 'lucide-react';
import ApiGenerationModal from '@/components/modals/ApiGenerationModal.js';

// Import SchemaBrowserController
import {
  getSchemaConnections,
  getSchemaObjects,
  getObjectDetails,
  getTableData,
  getObjectDDL,
  searchSchema,
  executeQuery,
  generateAPIFromObject,
  getComprehensiveSchemaData,
  clearSchemaCache,
  getObjectHierarchy,
  exportSchemaData,
  advancedSearch,
  healthCheck,
  handleSchemaBrowserResponse,
  extractSchemaConnections,
  extractSchemaObjects,
  extractObjectDetails,
  extractTableData,
  extractDDL,
  extractSearchResults,
  extractQueryResults,
  extractAPIGenerationResults,
  extractComprehensiveSchemaData,
  extractObjectHierarchy,
  extractExportResults,
  extractHealthCheck,
  formatObjectDetails,
  formatTableData,
  formatBytes,
  formatDateForDisplay,
  formatDataType,
  getObjectTypeIcon,
  getObjectTypeColor,
  isSupportedForAPIGeneration,
  generateSampleQuery,
  refreshSchemaData,
  downloadExportedSchema,
  cacheSchemaData,
  getCachedSchemaData,
  clearCachedSchemaData
} from "../controllers/SchemaBrowserController.js";

// Create a separate FilterInput component to prevent re-renders
const FilterInput = React.memo(({ 
  filterQuery, 
  selectedOwner, 
  onFilterChange, 
  onOwnerChange, 
  onClearFilters,
  colors 
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
    setTimeout(() => {
      if (searchInputRef.current) {
        searchInputRef.current.focus();
      }
    }, 10);
  }, [onFilterChange]);

  const handleClearAllFilters = useCallback(() => {
    onFilterChange('');
    onOwnerChange('ALL');
    setTimeout(() => {
      if (searchInputRef.current) {
        searchInputRef.current.focus();
      }
    }, 10);
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
              placeholder="Filter objects..."
              value={filterQuery}
              onChange={handleFilterChange}
              className="w-full pl-10 pr-3 py-2.5 rounded text-sm focus:outline-none hover-lift touch-target"
              style={{ 
                backgroundColor: colors.inputBg,
                border: `1px solid ${colors.inputBorder}`,
                color: colors.text
              }}
              aria-label="Search objects"
            />
            {filterQuery && (
              <button 
                onClick={handleClearFilter}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 p-0.5 rounded hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover }}
                aria-label="Clear search"
              >
                <X size={12} style={{ color: colors.textSecondary }} />
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Filter status indicator */}
      {(filterQuery || selectedOwner !== 'ALL') && (
        <div className="px-3 py-2 border-b" style={{ borderColor: colors.border, backgroundColor: colors.hover }}>
          <div className="flex items-center justify-between">
            <span className="text-xs" style={{ color: colors.textSecondary }}>
              Filtering {filterQuery && `by: "${filterQuery}"`} {filterQuery && selectedOwner !== 'ALL' && ' • '} 
              {selectedOwner !== 'ALL' && `Owner: ${selectedOwner}`}
            </span>
            <button 
              onClick={handleClearAllFilters}
              className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.border, color: colors.text }}
              aria-label="Clear all filters"
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

// Create a separate ObjectTreeSection component
const ObjectTreeSection = React.memo(({ 
  title, 
  type, 
  objects, 
  isExpanded, 
  onToggle,
  onSelectObject,
  activeObjectId,
  filterQuery,
  selectedOwner,
  colors,
  getObjectIcon,
  handleContextMenu
}) => {
  
  // Simple filter function inside the component
  const searchObjects = useCallback((objects, type) => {
    if (!filterQuery && selectedOwner === 'ALL') {
      return objects;
    }
    
    const searchLower = filterQuery.toLowerCase();
    
    return objects.filter(obj => {
      // Apply owner filter
      const ownerMatch = selectedOwner === 'ALL' || obj.owner === selectedOwner;
      if (!ownerMatch) return false;
      
      // If no search query, return true
      if (!filterQuery) return true;
      
      // Simple search in key properties
      return (
        (obj.name && obj.name.toLowerCase().includes(searchLower)) ||
        (obj.owner && obj.owner.toLowerCase().includes(searchLower)) ||
        (obj.comment && obj.comment.toLowerCase().includes(searchLower)) ||
        (obj.columns && obj.columns.some(col => 
          col.name && col.name.toLowerCase().includes(searchLower)
        )) ||
        (obj.parameters && obj.parameters.some(param => 
          param.name && param.name.toLowerCase().includes(searchLower)
        ))
      );
    });
  }, [filterQuery, selectedOwner]);

  const filteredObjects = searchObjects(objects, type);
  
  return (
    <div className="mb-1">
      <button
        onClick={onToggle}
        className="flex items-center justify-between w-full px-2 py-2 hover:bg-opacity-50 transition-colors rounded-sm text-sm font-medium touch-target hover-lift"
        style={{ backgroundColor: colors.hover }}
        aria-label={`Toggle ${title} section`}
      >
        <div className="flex items-center gap-2">
          {isExpanded ? 
            <ChevronDown size={14} style={{ color: colors.textSecondary }} /> :
            <ChevronRight size={14} style={{ color: colors.textSecondary }} />
          }
          {getObjectIcon(type.slice(0, -1))}
          <span className="truncate text-xs sm:text-sm">{title}</span>
        </div>
        <span className="text-xs px-1.5 py-0.5 rounded shrink-0 min-w-6 text-center" style={{ 
          backgroundColor: colors.border,
          color: colors.textSecondary
        }}>
          {filteredObjects.length}
        </span>
      </button>
      
      {isExpanded && (
        <div className="ml-6 mt-0.5 space-y-0.5">
          {filteredObjects.length === 0 ? (
            <div className="px-2 py-3 text-center">
              <span className="text-xs" style={{ color: colors.textTertiary }}>
                {filterQuery || selectedOwner !== 'ALL' ? 'No objects match your filter' : 'No objects found'}
              </span>
            </div>
          ) : (
            filteredObjects.map(obj => (
              <button
                key={obj.id}
                onDoubleClick={() => onSelectObject(obj, type.slice(0, -1).toUpperCase())}
                onContextMenu={(e) => handleContextMenu(e, obj, type.slice(0, -1).toUpperCase())}
                onClick={() => onSelectObject(obj, type.slice(0, -1).toUpperCase())}
                className={`flex items-center justify-between w-full px-2 py-2 rounded-sm cursor-pointer group hover-lift touch-target text-left ${
                  activeObjectId === obj.id ? 'font-medium' : ''
                }`}
                style={{
                  backgroundColor: activeObjectId === obj.id ? colors.selected : 'transparent',
                  color: activeObjectId === obj.id ? colors.primary : colors.text
                }}
                aria-label={`Select ${obj.name}`}
              >
                <div className="flex items-center gap-2 min-w-0 flex-1">
                  {getObjectIcon(type.slice(0, -1))}
                  <span className="text-xs sm:text-sm truncate">{obj.name}</span>
                  {obj.status !== 'VALID' && (
                    <AlertCircle size={10} style={{ color: colors.error }} className="shrink-0" />
                  )}
                </div>
                <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity shrink-0 ml-2">
                  {obj.isFavorite && (
                    <Star size={10} fill={colors.warning} style={{ color: colors.warning }} />
                  )}
                  {obj.rowCount && (
                    <span className="text-xs hidden sm:inline" style={{ color: activeObjectId === obj.id ? colors.primary : colors.textSecondary }}>
                      ({obj.rowCount})
                    </span>
                  )}
                </div>
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
  showConnectionManager,
  setShowConnectionManager,
  filterQuery,
  selectedOwner,
  handleFilterChange,
  handleOwnerChange,
  handleClearFilters,
  colors,
  objectTree,
  handleToggleSection,
  schemaObjects,
  activeObject,
  handleObjectSelect,
  getObjectIcon,
  handleContextMenu,
  activeConnection,
  connections,
  loading
}) => {
  const activeConnectionData = connections.find(c => c.id === activeConnection) || connections[0] || {};

  return (
    <div className={`w-full md:w-64 border-r flex flex-col absolute md:relative inset-y-0 left-0 z-40 transform transition-transform duration-300 ease-in-out ${
      isLeftSidebarVisible ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
    }`} style={{ 
      borderColor: colors.border,
      width: '16vw',
      maxWidth: '320px'
    }}>

      {/* Connection Info */}
      <div className="p-3 border-b" style={{ borderColor: colors.border }}>
        <div className="flex items-center justify-between mb-2">
          <button 
            className="flex items-center gap-2 flex-1 text-left touch-target"
            onClick={() => setShowConnectionManager(true)}
            aria-label="Open connection manager"
            disabled={loading}
          >
            <div className="w-2 h-2 rounded-full" style={{ 
              backgroundColor: activeConnectionData.status === 'connected' ? colors.connectionOnline : 
                             activeConnectionData.status === 'disconnected' ? colors.connectionOffline : colors.connectionIdle 
            }} />
            <span className="text-sm font-medium truncate" style={{ color: colors.text }}>
              {activeConnectionData.name || 'Loading...'}
            </span>
            <ChevronRight size={12} style={{ color: colors.textSecondary }} className="ml-1" />
          </button>
          <div className="flex gap-1">
            <button 
              className="rounded hover:bg-opacity-50 transition-colors hover-lift touch-target flex items-center justify-center w-8 h-8"
              style={{ backgroundColor: colors.hover }}
              onClick={() => setShowConnectionManager(true)}
              aria-label="Server settings"
              disabled={loading}
            >
              <Server size={12} style={{ color: colors.textSecondary }} />
            </button>

            <button 
              className="rounded hover:bg-opacity-50 transition-colors hover-lift touch-target flex items-center justify-center w-8 h-8"
              style={{ backgroundColor: colors.hover }}
              onClick={handleClearFilters}
              aria-label="Refresh and clear filters"
              disabled={loading}
            >
              <RefreshCw size={12} style={{ color: colors.textSecondary }} className={loading ? 'animate-spin' : ''} />
            </button>
          </div>
        </div>
        <div className="text-xs truncate" style={{ color: colors.textSecondary }}>
          {activeConnectionData.host ? `${activeConnectionData.host}:${activeConnectionData.port}` : 'Loading connection...'}
        </div>
      </div>

      {/* Filter Input Component */}
      <FilterInput
        filterQuery={filterQuery}
        selectedOwner={selectedOwner}
        onFilterChange={handleFilterChange}
        onOwnerChange={handleOwnerChange}
        onClearFilters={handleClearFilters}
        colors={colors}
      />

      {/* Object Tree */}
      <div className="flex-1 overflow-auto p-3">
        {loading ? (
          <div className="flex items-center justify-center h-32">
            <RefreshCw className="animate-spin" size={16} style={{ color: colors.textSecondary }} />
          </div>
        ) : (
          <>
            <ObjectTreeSection
              title="Tables"
              type="tables"
              objects={schemaObjects.tables || []}
              isExpanded={objectTree.tables}
              onToggle={() => handleToggleSection('tables')}
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
              isExpanded={objectTree.views}
              onToggle={() => handleToggleSection('views')}
              onSelectObject={handleObjectSelect}
              activeObjectId={activeObject?.id}
              filterQuery={filterQuery}
              selectedOwner={selectedOwner}
              colors={colors}
              getObjectIcon={getObjectIcon}
              handleContextMenu={handleContextMenu}
            />
            <ObjectTreeSection
              title="Procedures"
              type="procedures"
              objects={schemaObjects.procedures || []}
              isExpanded={objectTree.procedures}
              onToggle={() => handleToggleSection('procedures')}
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
              isExpanded={objectTree.functions}
              onToggle={() => handleToggleSection('functions')}
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
              isExpanded={objectTree.packages}
              onToggle={() => handleToggleSection('packages')}
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
              isExpanded={objectTree.sequences}
              onToggle={() => handleToggleSection('sequences')}
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
              isExpanded={objectTree.synonyms}
              onToggle={() => handleToggleSection('synonyms')}
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
              isExpanded={objectTree.types}
              onToggle={() => handleToggleSection('types')}
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
              isExpanded={objectTree.triggers}
              onToggle={() => handleToggleSection('triggers')}
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

// Mobile bottom navigation
const MobileBottomNav = React.memo(({ 
  isLeftSidebarVisible, 
  setIsLeftSidebarVisible, 
  setShowApiModal, 
  schemaObjects, 
  tabs, 
  handleObjectSelect,
  toggleTheme,
  isDark,
  colors,
  loading,
  onRefreshSchema
}) => (
  <div className="md:hidden fixed bottom-0 left-0 right-0 border-t z-20" style={{ 
    backgroundColor: colors.card,
    borderColor: colors.border
  }}>
    <div className="flex items-center justify-around p-2">
      <button 
        onClick={() => setIsLeftSidebarVisible(true)}
        className="rounded hover:bg-opacity-50 transition-colors touch-target flex flex-col items-center justify-center w-14 h-14 p-1"
        style={{ backgroundColor: isLeftSidebarVisible ? colors.selected : 'transparent' }}
        aria-label="Open schema browser"
        disabled={loading}
      >
        <div className="flex-1 flex items-center justify-center">
          <Database size={16} style={{ color: colors.text }} />
        </div>
        <span className="text-xs" style={{ color: colors.textSecondary }}>Schema</span>
      </button>

      <button 
        onClick={() => setShowApiModal(true)}
        className="rounded hover:bg-opacity-50 transition-colors touch-target flex flex-col items-center justify-center w-14 h-14 p-1"
        style={{ backgroundColor: 'transparent' }}
        aria-label="Generate API"
        disabled={loading}
      >
        <div className="flex-1 flex items-center justify-center">
          <Code size={16} style={{ color: colors.primary }} />
        </div>
        <span className="text-xs" style={{ color: colors.primary }}>Generate API</span>
      </button>
      
      <button 
        onClick={onRefreshSchema}
        className="flex flex-col items-center p-2 rounded hover:bg-opacity-50 transition-colors touch-target"
        aria-label="Refresh"
        disabled={loading}
      >
        <RefreshCw size={16} style={{ color: colors.text }} className={loading ? 'animate-spin' : ''} />
        <span className="text-xs mt-1" style={{ color: colors.textSecondary }}>Refresh</span>
      </button>
      
      <button 
        onClick={toggleTheme}
        className="flex flex-col items-center p-2 rounded hover:bg-opacity-50 transition-colors touch-target"
        aria-label="Toggle theme"
      >
        {isDark ? (
          <Sun size={16} style={{ color: colors.text }} />
        ) : (
          <Moon size={16} style={{ color: colors.text }} />
        )}
        <span className="text-xs mt-1" style={{ color: colors.textSecondary }}>Theme</span>
      </button>
    </div>
  </div>
));

MobileBottomNav.displayName = 'MobileBottomNav';

const SchemaBrowser = ({ theme, isDark, customTheme, toggleTheme, authToken }) => {
  // Using EXACT Dashboard color system for consistency
  const colors = useMemo(() => isDark ? {
    // Using your shade as base - EXACTLY matching Collections
    bg: 'rgb(1 14 35)',
    white: '#FFFFFF',
    sidebar: 'rgb(41 53 72 / 19%)',
    main: 'rgb(1 14 35)',
    header: 'rgb(20 26 38)',
    card: 'rgb(41 53 72 / 19%)',
    
    // Text - coordinating grays - EXACTLY matching Collections
    text: '#F1F5F9',
    textSecondary: 'rgb(148 163 184)',
    textTertiary: 'rgb(100 116 139)',
    
    // Borders - variations of your shade - EXACTLY matching Collections
    border: 'rgb(51 65 85 / 19%)',
    borderLight: 'rgb(45 55 72)',
    borderDark: 'rgb(71 85 105)',
    
    // Interactive - layered transparency - EXACTLY matching Collections
    hover: 'rgb(45 46 72 / 33%)',
    active: 'rgb(59 74 99)',
    selected: 'rgb(44 82 130)',
    
    // Primary colors - EXACTLY matching Collections
    primary: 'rgb(96 165 250)',
    primaryLight: 'rgb(147 197 253)',
    primaryDark: 'rgb(37 99 235)',
    
    // Method colors - EXACTLY matching Collections
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
    
    // Status colors - EXACTLY matching Collections
    success: 'rgb(52 211 153)',
    warning: 'rgb(251 191 36)',
    error: 'rgb(248 113 113)',
    info: 'rgb(96 165 250)',
    
    // UI Components - EXACTLY matching Collections
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
    
    // Connection status - EXACTLY matching Collections
    connectionOnline: 'rgb(52 211 153)',
    connectionOffline: 'rgb(248 113 113)',
    connectionIdle: 'rgb(251 191 36)',
    
    // Accent colors - EXACTLY matching Collections
    accentPurple: 'rgb(167 139 250)',
    accentPink: 'rgb(244 114 182)',
    accentCyan: 'rgb(34 211 238)',
    
    // Object type colors - using Collections's color palette
    objectType: {
      table: 'rgb(96 165 250)',      // primary color
      view: 'rgb(52 211 153)',       // success color
      procedure: 'rgb(167 139 250)', // accentPurple
      function: 'rgb(251 191 36)',   // warning color
      package: 'rgb(148 163 184)',   // textSecondary
      sequence: 'rgb(100 116 139)',  // textTertiary
      synonym: 'rgb(34 211 238)',    // accentCyan
      type: 'rgb(139 92 246)',       // purple variant
      trigger: 'rgb(244 114 182)',   // accentPink
      index: 'rgb(16 185 129)',      // teal green
      constraint: 'rgb(248 113 113)' // error color
    },
    
    // Grid colors for tables - matching Collections's structure
    gridRowEven: 'rgb(41 53 72 / 19%)',
    gridRowOdd: 'rgb(45 46 72 / 33%)',
    gridHeader: 'rgb(41 53 72 / 19%)',
    gridborder: 'rgb(51 65 85 / 19%)',
    
    // Gradient - EXACTLY matching Collections
    gradient: 'from-blue-500/20 via-violet-500/20 to-orange-500/20'
  } : {
    // LIGHT MODE - EXACTLY matching Collections's light mode
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
    
    // Method colors for light mode
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
    
    // Status colors for light mode
    success: '#10b981',
    warning: '#f59e0b',
    error: '#ef4444',
    info: '#3b82f6',
    
    // UI Components for light mode
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
    
    // Connection status for light mode
    connectionOnline: '#10b981',
    connectionOffline: '#ef4444',
    connectionIdle: '#f59e0b',
    
    // Accent colors for light mode
    accentPurple: '#8b5cf6',
    accentPink: '#ec4899',
    accentCyan: '#06b6d4',
    
    // Object type colors for light mode
    objectType: {
      table: '#3b82f6',
      view: '#10b981',
      procedure: '#8b5cf6',
      function: '#f59e0b',
      package: '#6b7280',
      sequence: '#64748b',
      synonym: '#06b6d4',
      type: '#6366f1',
      trigger: '#ec4899',
      index: '#0d9488',
      constraint: '#ef4444'
    },
    
    // Grid colors for light mode
    gridRowEven: '#ffffff',
    gridRowOdd: '#f8fafc',
    gridHeader: '#f1f5f9',
    gridBorder: '#e2e8f0',
    
    gradient: 'from-blue-400/20 via-violet-400/20 to-orange-400/20'
  }, [isDark]);

  const [showApiModal, setShowApiModal] = useState(false);
  const [selectedForApiGeneration, setSelectedForApiGeneration] = useState(null);

  // Mobile state
  const [isLeftSidebarVisible, setIsLeftSidebarVisible] = useState(false);
  const [isRightSidebarVisible, setIsRightSidebarVisible] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [touchStart, setTouchStart] = useState(null);
  const [isLandscape, setIsLandscape] = useState(false);

  const [globalSearchQuery, setGlobalSearchQuery] = useState('');
  const [showCodePanel, setShowCodePanel] = useState(true);
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [tableDataLoading, setTableDataLoading] = useState(false);
  const [executingQuery, setExecutingQuery] = useState(false);

  // Filter state
  const [filterQuery, setFilterQuery] = useState('');
  const [selectedOwner, setSelectedOwner] = useState('ALL');

  // State for API data
  const [connections, setConnections] = useState([]);
  const [activeConnection, setActiveConnection] = useState('');
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
  const [activeObject, setActiveObject] = useState(null);
  const [activeTab, setActiveTab] = useState('columns');
  const [objectTree, setObjectTree] = useState({
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
  const [tabs, setTabs] = useState([]);
  const [tableData, setTableData] = useState(null);
  const [objectDDL, setObjectDDL] = useState('');
  const [searchResults, setSearchResults] = useState(null);
  const [objectDetails, setObjectDetails] = useState(null);
  const [objectHierarchy, setObjectHierarchy] = useState(null);

  // Context menu state
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuPosition, setContextMenuPosition] = useState({ x: 0, y: 0 });
  const [contextObject, setContextObject] = useState(null);
  const [showConnectionManager, setShowConnectionManager] = useState(false);
  const [dataView, setDataView] = useState({
    page: 1,
    pageSize: 50,
    sortColumn: '',
    sortDirection: 'ASC',
    filters: []
  });

  // Add a flag to track if we've already auto-selected an object
  const [hasAutoSelected, setHasAutoSelected] = useState(false);

  // Create stable callback functions for filter
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

  // Get Object Icon
  const getObjectIcon = useCallback((type) => {
    const objectType = type.toLowerCase();
    const iconColor = colors.objectType[objectType] || colors.textSecondary;
    const iconProps = { size: 14, style: { color: iconColor } };
    
    switch(objectType) {
      case 'table': return <Table {...iconProps} />;
      case 'view': return <FileText {...iconProps} />;
      case 'procedure': return <Terminal {...iconProps} />;
      case 'function': return <Code {...iconProps} />;
      case 'package': return <Package {...iconProps} />;
      case 'sequence': return <Hash {...iconProps} />;
      case 'synonym': return <Link {...iconProps} />;
      case 'type': return <Type {...iconProps} />;
      case 'trigger': return <Zap {...iconProps} />;
      case 'index': return <BarChart {...iconProps} />;
      default: return <Database {...iconProps} />;
    }
  }, [colors]);

  // Function to get the first available schema object
  const getFirstSchemaObject = useCallback((schemaObjects) => {
    // Define the order of object types to check
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
    
    // Find the first object type that has at least one item
    for (const objType of objectTypes) {
      const objects = schemaObjects[objType.key] || [];
      if (objects.length > 0) {
        return {
          object: objects[0],
          type: objType.type
        };
      }
    }
    
    return null;
  }, []);

  // Check screen orientation
  useEffect(() => {
    const checkOrientation = () => {
      setIsLandscape(window.innerWidth > window.innerHeight);
    };
    
    checkOrientation();
    window.addEventListener('resize', checkOrientation);
    return () => window.removeEventListener('resize', checkOrientation);
  }, []);

  // Handle touch events for mobile gestures
  useEffect(() => {
    const handleTouchStart = (e) => {
      setTouchStart({
        x: e.touches[0].clientX,
        y: e.touches[0].clientY
      });
    };

    const handleTouchEnd = (e) => {
      if (!touchStart) return;

      const touchEnd = {
        x: e.changedTouches[0].clientX,
        y: e.changedTouches[0].clientY
      };

      const diffX = touchEnd.x - touchStart.x;
      const diffY = touchEnd.y - touchStart.y;

      // Swipe right from left edge to open sidebar
      if (diffX > 50 && Math.abs(diffY) < 50 && touchStart.x < 50) {
        setIsLeftSidebarVisible(true);
      }
      
      // Swipe left to close sidebar
      if (diffX < -50 && Math.abs(diffY) < 50 && isLeftSidebarVisible) {
        setIsLeftSidebarVisible(false);
      }

      setTouchStart(null);
    };

    document.addEventListener('touchstart', handleTouchStart);
    document.addEventListener('touchend', handleTouchEnd);

    return () => {
      document.removeEventListener('touchstart', handleTouchStart);
      document.removeEventListener('touchend', handleTouchEnd);
    };
  }, [touchStart, isLeftSidebarVisible]);

  // Fetch schema connections
  const fetchSchemaConnections = useCallback(async () => {
    if (!authToken) {
      setError('Authentication required');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Check cache first
      const cachedConnections = getCachedSchemaData('connections');
      if (cachedConnections) {
        setConnections(cachedConnections);
        if (cachedConnections.length > 0 && !activeConnection) {
          setActiveConnection(cachedConnections[0].id);
        }
        setLoading(false);
        return;
      }

      const response = await getSchemaConnections(authToken);
      const processedResponse = handleSchemaBrowserResponse(response);
      const connectionsData = extractSchemaConnections(processedResponse);
      
      setConnections(connectionsData);
      cacheSchemaData('connections', connectionsData, 30); // Cache for 30 minutes
      
      if (connectionsData.length > 0 && !activeConnection) {
        setActiveConnection(connectionsData[0].id);
      }

    } catch (error) {
      console.error('Error fetching schema connections:', error);
      setError(`Failed to load connections: ${error.message}`);
      // Fallback to sample data if API fails
      const fallbackConnections = [
        {
          id: 'conn-1',
          name: 'CBX_DMX',
          description: 'Production HR Database',
          host: 'db-prod.company.com',
          port: '1521',
          service: 'ORCL',
          username: 'HR',
          status: 'connected',
          color: colors.connectionOnline,
          lastUsed: '2024-01-15T10:30:00Z'
        }
      ];
      setConnections(fallbackConnections);
      if (!activeConnection) {
        setActiveConnection('conn-1');
      }
    } finally {
      setLoading(false);
    }
  }, [authToken, activeConnection, colors]);

  // Fetch schema objects for active connection
  const fetchSchemaObjects = useCallback(async () => {
    if (!authToken || !activeConnection) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Check cache first
      const cacheKey = `schema_objects_${activeConnection}`;
      const cachedObjects = getCachedSchemaData(cacheKey);
      if (cachedObjects) {
        setSchemaObjects(cachedObjects);
        setLoading(false);
        return;
      }

      // Fetch comprehensive schema data
      const response = await getComprehensiveSchemaData(authToken, { 
        connectionId: activeConnection 
      });
      const processedResponse = handleSchemaBrowserResponse(response);
      const comprehensiveData = extractComprehensiveSchemaData(processedResponse);
      
      const transformedObjects = {
        tables: comprehensiveData.tables?.objects || [],
        views: comprehensiveData.views?.objects || [],
        procedures: comprehensiveData.procedures?.objects || [],
        functions: comprehensiveData.functions?.objects || [],
        packages: comprehensiveData.packages?.objects || [],
        sequences: [],
        synonyms: [],
        types: [],
        triggers: []
      };

      setSchemaObjects(transformedObjects);
      cacheSchemaData(cacheKey, transformedObjects, 15); // Cache for 15 minutes

    } catch (error) {
      console.error('Error fetching schema objects:', error);
      setError(`Failed to load schema objects: ${error.message}`);
    } finally {
      setLoading(false);
    }
  }, [authToken, activeConnection]);

  // Fetch table data
  const fetchTableData = useCallback(async (tableName) => {
    if (!authToken || !activeConnection || !tableName) {
      return;
    }

    setTableDataLoading(true);

    try {
      const params = {
        connectionId: activeConnection,
        tableName,
        page: dataView.page,
        pageSize: dataView.pageSize,
        sortColumn: dataView.sortColumn,
        sortDirection: dataView.sortDirection
      };

      const response = await getTableData(authToken, params);
      const processedResponse = handleSchemaBrowserResponse(response);
      const tableData = extractTableData(processedResponse);
      
      setTableData(tableData);

    } catch (error) {
      console.error('Error fetching table data:', error);
      setError(`Failed to load table data: ${error.message}`);
    } finally {
      setTableDataLoading(false);
    }
  }, [authToken, activeConnection, dataView]);

  // Fetch object details
  const fetchObjectDetails = useCallback(async (objectId, objectType, objectName) => {
    if (!authToken || !activeConnection || !objectName) {
      return;
    }

    try {
      const params = {
        connectionId: activeConnection,
        objectType,
        objectName
      };

      const response = await getObjectDetails(authToken, params);
      const processedResponse = handleSchemaBrowserResponse(response);
      const details = extractObjectDetails(processedResponse);
      
      setObjectDetails(formatObjectDetails(details));

    } catch (error) {
      console.error('Error fetching object details:', error);
      setError(`Failed to load object details: ${error.message}`);
    }
  }, [authToken, activeConnection]);

  // Fetch object DDL
  const fetchObjectDDL = useCallback(async (objectType, objectName) => {
    if (!authToken || !activeConnection || !objectName) {
      return;
    }

    try {
      const params = {
        connectionId: activeConnection,
        objectType,
        objectName
      };

      const response = await getObjectDDL(authToken, params);
      const processedResponse = handleSchemaBrowserResponse(response);
      const ddl = extractDDL(processedResponse);
      
      setObjectDDL(ddl);

    } catch (error) {
      console.error('Error fetching object DDL:', error);
      setError(`Failed to load object DDL: ${error.message}`);
    }
  }, [authToken, activeConnection]);

  // Search schema
  const handleSearchSchema = useCallback(async (searchQuery, searchType = null) => {
    if (!authToken || !activeConnection || !searchQuery) {
      return;
    }

    setLoading(true);

    try {
      const params = {
        connectionId: activeConnection,
        searchQuery,
        searchType,
        maxResults: 100
      };

      const response = await searchSchema(authToken, params);
      const processedResponse = handleSchemaBrowserResponse(response);
      const searchResults = extractSearchResults(processedResponse);
      
      setSearchResults(searchResults);

    } catch (error) {
      console.error('Error searching schema:', error);
      setError(`Search failed: ${error.message}`);
    } finally {
      setLoading(false);
    }
  }, [authToken, activeConnection]);

  // Execute query
  const handleExecuteQuery = useCallback(async (query, timeoutSeconds = 30, readOnly = true) => {
    if (!authToken || !activeConnection || !query) {
      return;
    }

    setExecutingQuery(true);

    try {
      const queryRequest = {
        connectionId: activeConnection,
        query,
        timeoutSeconds,
        readOnly
      };

      const response = await executeQuery(authToken, queryRequest);
      const processedResponse = handleSchemaBrowserResponse(response);
      const queryResults = extractQueryResults(processedResponse);
      
      return queryResults;

    } catch (error) {
      console.error('Error executing query:', error);
      setError(`Query execution failed: ${error.message}`);
      throw error;
    } finally {
      setExecutingQuery(false);
    }
  }, [authToken, activeConnection]);

  // Generate API from object
  const handleGenerateAPI = useCallback(async (objectType, objectName, apiType = 'REST', options = {}) => {
    if (!authToken || !activeConnection || !objectName) {
      return;
    }

    try {
      const apiRequest = {
        connectionId: activeConnection,
        objectType,
        objectName,
        apiType,
        options
      };

      const response = await generateAPIFromObject(authToken, apiRequest);
      const processedResponse = handleSchemaBrowserResponse(response);
      const apiResults = extractAPIGenerationResults(processedResponse);
      
      return apiResults;

    } catch (error) {
      console.error('Error generating API:', error);
      setError(`API generation failed: ${error.message}`);
      throw error;
    }
  }, [authToken, activeConnection]);

  // Export schema data
  const handleExportSchema = useCallback(async (format = 'JSON', objectTypes = [], objectNames = []) => {
    if (!authToken || !activeConnection) {
      return;
    }

    try {
      const exportRequest = {
        connectionId: activeConnection,
        format,
        objectTypes,
        objectNames,
        options: {}
      };

      const response = await exportSchemaData(authToken, exportRequest);
      const processedResponse = handleSchemaBrowserResponse(response);
      const exportResults = extractExportResults(processedResponse);
      
      return exportResults;

    } catch (error) {
      console.error('Error exporting schema:', error);
      setError(`Export failed: ${error.message}`);
      throw error;
    }
  }, [authToken, activeConnection]);

  // Clear schema cache
  const handleClearSchemaCache = useCallback(async () => {
    if (!authToken) {
      return;
    }

    try {
      await clearSchemaCache(authToken);
      clearCachedSchemaData(); // Clear local cache
      // Refresh data
      fetchSchemaConnections();
      if (activeConnection) {
        fetchSchemaObjects();
      }
    } catch (error) {
      console.error('Error clearing cache:', error);
      setError(`Cache clear failed: ${error.message}`);
    }
  }, [authToken, activeConnection, fetchSchemaConnections, fetchSchemaObjects]);

  // Health check
  const checkSchemaHealth = useCallback(async () => {
    if (!authToken) {
      return;
    }

    try {
      const response = await healthCheck(authToken);
      const processedResponse = handleSchemaBrowserResponse(response);
      const healthData = extractHealthCheck(processedResponse);
      
      return healthData;

    } catch (error) {
      console.error('Error checking health:', error);
      return {
        status: 'DOWN',
        timestamp: new Date().toISOString(),
        error: error.message
      };
    }
  }, [authToken]);

  // Refresh all schema data
  const refreshAllSchemaData = useCallback(async () => {
    if (!authToken) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const healthData = await checkSchemaHealth();
      
      if (healthData.status === 'UP') {
        // Clear cache and fetch fresh data
        clearCachedSchemaData();
        await fetchSchemaConnections();
        
        if (activeConnection) {
          await fetchSchemaObjects();
          
          // Refresh active object if selected
          if (activeObject) {
            await fetchObjectDetails(activeObject.id, activeObject.type, activeObject.name);
            if (activeObject.type === 'TABLE') {
              await fetchTableData(activeObject.name);
            }
          }
        }
        
        return { success: true, health: healthData };
      } else {
        setError('Schema browser service is unavailable');
        return { success: false, health: healthData };
      }

    } catch (error) {
      console.error('Error refreshing schema data:', error);
      setError(`Refresh failed: ${error.message}`);
      return { success: false, error: error.message };
    } finally {
      setLoading(false);
    }
  }, [authToken, activeConnection, activeObject, fetchSchemaConnections, fetchSchemaObjects, fetchObjectDetails, fetchTableData, checkSchemaHealth]);

  // Initialize data
  useEffect(() => {
    fetchSchemaConnections();
  }, [fetchSchemaConnections]);

  useEffect(() => {
    if (activeConnection) {
      fetchSchemaObjects();
    }
  }, [activeConnection, fetchSchemaObjects]);

  // Handle Object Selection - updated to be more robust
  const handleObjectSelect = useCallback(async (object, type) => {
    if (!authToken || !activeConnection || !object) {
      return;
    }

    setActiveObject(object);
    setSelectedForApiGeneration(object);
    
    // Check if tab already exists
    const tabId = `${type}_${object.id}`;
    const existingTab = tabs.find(tab => tab.id === tabId);
    
    if (existingTab) {
      setTabs(tabs.map(tab => ({ ...tab, isActive: tab.id === tabId })));
    } else {
      const newTabs = tabs.map(tab => ({ ...tab, isActive: false })).concat({
        id: tabId,
        name: object.name,
        type: type,
        objectId: object.id,
        isActive: true,
        isDirty: false
      });
      setTabs(newTabs.slice(-5)); // Keep only last 5 tabs
    }
    
    // Set default tab based on object type
    switch(type.toLowerCase()) {
      case 'table':
        setActiveTab('columns');
        // Fetch table data
        await fetchTableData(object.name);
        break;
      case 'view':
        setActiveTab('definition');
        // Fetch view DDL
        await fetchObjectDDL(type, object.name);
        break;
      case 'procedure':
      case 'function':
        setActiveTab('parameters');
        // Fetch object details
        await fetchObjectDetails(object.id, type, object.name);
        break;
      case 'package':
        setActiveTab('specification');
        // Fetch package DDL
        await fetchObjectDDL(type, object.name);
        break;
      case 'trigger':
        setActiveTab('definition');
        // Fetch trigger DDL
        await fetchObjectDDL(type, object.name);
        break;
      default:
        setActiveTab('properties');
    }

    // Fetch object details
    await fetchObjectDetails(object.id, type, object.name);

    // Close sidebar on mobile
    if (window.innerWidth < 768) {
      setIsLeftSidebarVisible(false);
    }
  }, [authToken, activeConnection, tabs, fetchTableData, fetchObjectDDL, fetchObjectDetails]);

  // Auto-select first schema object when schema objects are loaded
  useEffect(() => {
    // Check if we have schema objects but no active object yet, and we haven't auto-selected before
    const hasObjects = Object.values(schemaObjects).some(arr => arr.length > 0);
    
    if (hasObjects && !activeObject && !hasAutoSelected && activeConnection) {
      const firstObjectData = getFirstSchemaObject(schemaObjects);
      
      if (firstObjectData) {
        setHasAutoSelected(true);
        handleObjectSelect(firstObjectData.object, firstObjectData.type);
      }
    }
  }, [schemaObjects, activeObject, hasAutoSelected, activeConnection, handleObjectSelect, getFirstSchemaObject]);

  // Handle Context Menu
  const handleContextMenu = useCallback((e, object, type) => {
    e.preventDefault();
    e.stopPropagation();
    
    setContextObject({ ...object, type });
    setContextMenuPosition({ x: e.clientX, y: e.clientY });
    setShowContextMenu(true);
  }, []);

  // Handle toggle section
  const handleToggleSection = useCallback((type) => {
    setObjectTree(prev => ({ ...prev, [type]: !prev[type] }));
  }, []);

  // Handle search from global search
  const handleGlobalSearch = useCallback(() => {
    if (globalSearchQuery.trim()) {
      handleSearchSchema(globalSearchQuery);
    }
  }, [globalSearchQuery, handleSearchSchema]);

  // Render Columns Tab
  const renderColumnsTab = () => {
    const columns = objectDetails?.columns || activeObject?.columns || [];
    
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded" style={{ 
          borderColor: colors.gridBorder,
          backgroundColor: colors.card
        }}>
          {/* Toolbar */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 p-2 border-b" style={{ 
            borderColor: colors.gridBorder,
            backgroundColor: colors.card
          }}>
            <div className="text-sm font-medium" style={{ color: colors.text }}>
              Columns ({columns.length})
            </div>
            <div className="flex items-center gap-2 self-end sm:self-auto">
              <button 
                className="px-2 py-1 text-xs rounded hover:bg-opacity-50 transition-colors hover-lift touch-target"
                style={{ backgroundColor: colors.hover, color: colors.text }}
                onClick={() => {
                  const columnsText = columns.map(col => 
                    `${col.name} ${col.type} ${col.nullable === 'Y' ? 'NULL' : 'NOT NULL'}`
                  ).join('\n');
                  navigator.clipboard.writeText(columnsText || '');
                }}
                aria-label="Copy columns"
              >
                <Copy size={12} className="inline sm:mr-1" />
                <span className="hidden sm:inline">Copy</span>
              </button>
              <button 
                className="px-2 py-1 text-xs rounded hover:bg-opacity-50 transition-colors hover-lift touch-target"
                style={{ backgroundColor: colors.hover, color: colors.text }}
                aria-label="Export columns"
              >
                <Download size={12} className="inline sm:mr-1" />
                <span className="hidden sm:inline">Export</span>
              </button>
            </div>
          </div>

          {/* Columns Grid */}
          <div className="overflow-auto max-h-[calc(100vh-300px)] sm:max-h-none">
            <table className="w-full min-w-[600px]" style={{ borderCollapse: 'collapse' }}>
              <thead style={{ 
                backgroundColor: colors.tableHeader,
                position: 'sticky',
                top: 0,
                zIndex: 10
              }}>
                <tr>
                  <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                    borderColor: colors.gridBorder,
                    color: colors.textSecondary,
                    width: '30px'
                  }}>#</th>
                  <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                    borderColor: colors.gridBorder,
                    color: colors.textSecondary,
                    minWidth: '100px'
                  }}>Column</th>
                  <th className="text-left p-2 text-xs font-medium border-b hidden xs:table-cell" style={{ 
                    borderColor: colors.gridBorder,
                    color: colors.textSecondary,
                    minWidth: '80px'
                  }}>Type</th>
                  <th className="text-left p-2 text-xs font-medium border-b hidden sm:table-cell" style={{ 
                    borderColor: colors.gridBorder,
                    color: colors.textSecondary,
                    width: '40px'
                  }}>Null</th>
                  <th className="text-left p-2 text-xs font-medium border-b hidden sm:table-cell" style={{ 
                    borderColor: colors.gridBorder,
                    color: colors.textSecondary,
                    width: '50px'
                  }}>Key</th>
                  <th className="text-left p-2 text-xs font-medium border-b hidden md:table-cell" style={{ 
                    borderColor: colors.gridBorder,
                    color: colors.textSecondary
                  }}>Default</th>
                </tr>
              </thead>
              <tbody>
                {columns.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="p-4 text-center text-sm" style={{ color: colors.textSecondary }}>
                      No columns found
                    </td>
                  </tr>
                ) : (
                  columns.map((col, index) => (
                    <tr 
                      key={col.name}
                      className="hover:bg-opacity-50 transition-colors"
                      style={{ 
                        backgroundColor: index % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                        borderBottom: `1px solid ${colors.gridBorder}`
                      }}
                    >
                      <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>{col.position || index + 1}</td>
                      <td className="p-2 text-xs font-medium truncate max-w-[120px] sm:max-w-none" style={{ color: colors.text }}>
                        <div className="flex flex-col">
                          <span className="truncate">{col.name}</span>
                          <span className="text-xs text-gray-500 xs:hidden">{col.type}</span>
                        </div>
                      </td>
                      <td className="p-2 text-xs font-mono truncate hidden xs:table-cell" style={{ color: colors.text }}>{col.type}</td>
                      <td className="p-2 text-xs text-center hidden sm:table-cell">
                        <div className={`inline-flex items-center justify-center w-5 h-5 rounded ${
                          col.nullable === 'Y' ? 'bg-green-500/10 text-green-600' : 'bg-red-500/10 text-red-600'
                        }`}>
                          {col.nullable === 'Y' ? 'Y' : 'N'}
                        </div>
                      </td>
                      <td className="p-2 hidden sm:table-cell">
                        {col.key && (
                          <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                            col.key === 'PK' ? 'bg-blue-500/10' :
                            col.key === 'FK' ? 'bg-purple-500/10 text-purple-400' :
                            'bg-green-500/10 text-green-400'
                          }`}>
                            {col.key}
                          </span>
                        )}
                      </td>
                      <td className="p-2 text-xs font-mono truncate hidden md:table-cell" style={{ color: colors.textSecondary }}>
                        {col.defaultValue || <span className="italic">NULL</span>}
                      </td>
                    </tr>
                  ))
                )}
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
    const columns = tableData?.columns || activeObject?.columns || [];
    
    return (
      <div className="flex-1 flex flex-col">
        {/* Data Grid Toolbar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 p-2 border-b" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="flex flex-wrap items-center gap-2">
            <button 
              className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 hover-lift touch-target"
              style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              onClick={() => activeObject && fetchTableData(activeObject.name)}
              disabled={tableDataLoading || !activeObject}
              aria-label="Execute query"
            >
              {tableDataLoading ? (
                <RefreshCw size={12} className="animate-spin" />
              ) : (
                <Play size={12} />
              )}
              <span className="hidden sm:inline">Execute</span>
            </button>
            <div className="ml-0 sm:ml-4 flex items-center gap-2">
              <select 
                className="px-2 py-1 border rounded text-sm focus:outline-none hover-lift touch-target"
                style={{ 
                  backgroundColor: colors.card,
                  borderColor: colors.border,
                  color: colors.text
                }}
                aria-label="Auto-refresh interval"
                onChange={(e) => {
                  if (e.target.value !== 'Off' && activeObject) {
                    const interval = parseInt(e.target.value);
                    // Implement auto-refresh logic here
                  }
                }}
              >
                <option>Refresh: Off</option>
                <option value="5">5s</option>
                <option value="10">10s</option>
                <option value="30">30s</option>
              </select>
            </div>
          </div>
          <div className="flex items-center justify-between gap-2">
            <span className="text-xs hidden sm:inline" style={{ color: colors.textSecondary }}>
              Page: {tableData?.page || 1} of {tableData?.totalPages || 1} | 
              Rows: {((tableData?.page || 1) - 1) * (tableData?.pageSize || 50) + 1}-
              {Math.min((tableData?.page || 1) * (tableData?.pageSize || 50), tableData?.totalRows || 0)}
            </span>
            <div className="flex items-center gap-2">
              <button 
                className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift touch-target"
                style={{ backgroundColor: colors.hover }}
                onClick={() => {
                  if (tableData && tableData.page > 1) {
                    setDataView(prev => ({ ...prev, page: prev.page - 1 }));
                  }
                }}
                disabled={!tableData || tableData.page <= 1}
                aria-label="Previous page"
              >
                <ChevronLeft size={14} style={{ color: colors.textSecondary }} />
              </button>
              <button 
                className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift touch-target"
                style={{ backgroundColor: colors.hover }}
                onClick={() => {
                  if (tableData && tableData.page < tableData.totalPages) {
                    setDataView(prev => ({ ...prev, page: prev.page + 1 }));
                  }
                }}
                disabled={!tableData || tableData.page >= tableData.totalPages}
                aria-label="Next page"
              >
                <ChevronRight size={14} style={{ color: colors.textSecondary }} />
              </button>
            </div>
          </div>
        </div>

        {/* Data Grid - Mobile optimized */}
        <div className="flex-1 overflow-auto">
          <div className="border rounded" style={{ 
            borderColor: colors.gridBorder,
            backgroundColor: colors.card
          }}>
            {tableDataLoading ? (
              <div className="flex items-center justify-center h-64">
                <RefreshCw className="animate-spin" size={24} style={{ color: colors.textSecondary }} />
              </div>
            ) : data.length === 0 ? (
              <div className="p-8 text-center">
                <Table size={32} className="mx-auto mb-4" style={{ color: colors.textSecondary }} />
                <div className="text-sm" style={{ color: colors.text }}>No data available</div>
                <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
                  Execute query to load data
                </div>
              </div>
            ) : (
              <>
                <div className="sm:hidden">
                  {/* Mobile card view */}
                  {data.slice(0, 20).map((row, rowIndex) => (
                    <div 
                      key={rowIndex}
                      className="p-3 border-b"
                      style={{ 
                        borderColor: colors.gridBorder,
                        backgroundColor: rowIndex % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd
                      }}
                    >
                      <div className="space-y-2">
                        {columns.slice(0, 3).map(col => (
                          <div key={col.name} className="flex justify-between">
                            <span className="text-xs font-medium" style={{ color: colors.textSecondary }}>
                              {col.name}:
                            </span>
                            <span className="text-xs truncate max-w-[150px]" style={{ color: colors.text }}>
                              {row[col.name] !== null && row[col.name] !== undefined ? row[col.name] : (
                                <span className="italic" style={{ color: colors.textTertiary }}>NULL</span>
                              )}
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
                
                {/* Desktop table view */}
                <table className="w-full min-w-[600px] hidden sm:table" style={{ borderCollapse: 'collapse' }}>
                  <thead style={{ 
                    backgroundColor: colors.tableHeader,
                    position: 'sticky',
                    top: 0,
                    zIndex: 10
                  }}>
                    <tr>
                      {columns.slice(0, 5).map(col => (
                        <th key={col.name} className="text-left p-2 text-xs font-medium border-b" style={{ 
                          borderColor: colors.gridBorder,
                          color: colors.textSecondary,
                          whiteSpace: 'nowrap'
                        }}>
                          <div className="flex items-center gap-1">
                            <span className="truncate">{col.name}</span>
                            {col.key && (
                              <span className={`px-1 py-0.5 rounded text-xs hidden sm:inline ${
                                col.key === 'PK' ? 'bg-blue-500/10' :
                                col.key === 'FK' ? 'bg-purple-500/10 text-purple-400' :
                                'bg-green-500/10 text-green-400'
                              }`}>
                                {col.key}
                              </span>
                            )}
                          </div>
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {data.slice(0, 50).map((row, rowIndex) => (
                      <tr 
                        key={rowIndex}
                        className="hover:bg-opacity-50 transition-colors"
                        style={{ 
                          backgroundColor: rowIndex % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                          borderBottom: `1px solid ${colors.gridBorder}`
                        }}
                      >
                        {columns.slice(0, 5).map(col => (
                          <td key={col.name} className="p-2 text-xs border-r" style={{ 
                            borderColor: colors.gridBorder,
                            color: colors.text,
                            whiteSpace: 'nowrap'
                          }}>
                            <div className="truncate max-w-[100px] sm:max-w-none">
                              {row[col.name] !== null && row[col.name] !== undefined ? row[col.name] : (
                                <span className="italic" style={{ color: colors.textTertiary }}>NULL</span>
                              )}
                            </div>
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </>
            )}
          </div>
        </div>

        {/* Status Bar */}
        <div className="p-2 border-t" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="text-xs truncate" style={{ color: colors.textSecondary }}>
            <span className="block sm:inline">
              {tableData?.rows?.length || 0} rows fetched {tableData?.queryTime ? `in ${tableData.queryTime}ms` : ''} | 
              {window.innerWidth >= 640 && ` Table: ${activeObject?.name} | `}
              Total: {(tableData?.totalRows || 0).toLocaleString()}
            </span>
          </div>
        </div>
      </div>
    );
  };

  // Render Parameters Tab
  const renderParametersTab = () => {
    const parameters = objectDetails?.parameters || activeObject?.parameters || [];
    
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded" style={{ 
          borderColor: colors.gridBorder,
          backgroundColor: colors.card
        }}>
          <div className="p-2 border-b" style={{ 
            borderColor: colors.gridBorder,
            backgroundColor: colors.card
          }}>
            <div className="text-sm font-medium" style={{ color: colors.text }}>
              Parameters ({parameters.length})
            </div>
          </div>
          <div className="overflow-auto max-h-[calc(100vh-300px)] sm:max-h-none">
            <table className="w-full min-w-[600px]" style={{ borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ backgroundColor: colors.tableHeader }}>
                  <th className="text-left p-2 text-xs font-medium border-b hidden sm:table-cell" style={{ 
                    borderColor: colors.gridBorder,
                    color: colors.textSecondary,
                    width: '30px'
                  }}>#</th>
                  <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                    borderColor: colors.gridBorder,
                    color: colors.textSecondary,
                    minWidth: '100px'
                  }}>Parameter</th>
                  <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                    borderColor: colors.gridBorder,
                    color: colors.textSecondary,
                    width: '60px'
                  }}>Mode</th>
                  <th className="text-left p-2 text-xs font-medium border-b hidden md:table-cell" style={{ 
                    borderColor: colors.gridBorder,
                    color: colors.textSecondary,
                    minWidth: '80px'
                  }}>Type</th>
                  <th className="text-left p-2 text-xs font-medium border-b hidden lg:table-cell" style={{ 
                    borderColor: colors.gridBorder,
                    color: colors.textSecondary,
                    minWidth: '100px'
                  }}>Default</th>
                </tr>
              </thead>
              <tbody>
                {parameters.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="p-4 text-center text-sm" style={{ color: colors.textSecondary }}>
                      No parameters found
                    </td>
                  </tr>
                ) : (
                  parameters.map((param, index) => (
                    <tr 
                      key={param.name}
                      className="hover:bg-opacity-50 transition-colors"
                      style={{ 
                        backgroundColor: index % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                        borderBottom: `1px solid ${colors.gridBorder}`
                      }}
                    >
                      <td className="p-2 text-xs hidden sm:table-cell" style={{ color: colors.textSecondary }}>{param.position || index + 1}</td>
                      <td className="p-2 text-xs font-medium truncate max-w-[120px] sm:max-w-none" style={{ color: colors.text }}>
                        <div className="flex flex-col">
                          <span>{param.name}</span>
                          <span className="text-xs text-gray-500 md:hidden">{param.datatype}</span>
                        </div>
                      </td>
                      <td className="p-2">
                        <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                          param.type === 'IN' ? 'bg-blue-500/10' :
                          param.type === 'OUT' ? 'bg-purple-500/10 text-purple-400' :
                          'bg-green-500/10 text-green-400'
                        }`}>
                          {param.type}
                        </span>
                      </td>
                      <td className="p-2 text-xs font-mono truncate hidden md:table-cell" style={{ color: colors.text }}>{param.datatype}</td>
                      <td className="p-2 text-xs font-mono truncate hidden lg:table-cell" style={{ color: colors.textSecondary }}>
                        {param.defaultValue || <span className="italic">NULL</span>}
                      </td>
                    </tr>
                  ))
                )}
                {activeObject?.type === 'FUNCTION' && objectDetails?.returnType && (
                  <tr className="border-t" style={{ borderColor: colors.gridBorder }}>
                    <td className="p-2 text-xs font-medium hidden sm:table-cell" style={{ color: colors.textSecondary }}>-</td>
                    <td className="p-2 text-xs font-medium" style={{ color: colors.text }}>RETURN</td>
                    <td className="p-2">
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-500/10 text-green-400">
                        OUT
                      </span>
                    </td>
                    <td className="p-2 text-xs font-mono font-medium truncate hidden md:table-cell" style={{ color: colors.text }}>
                      {objectDetails.returnType}
                    </td>
                    <td className="p-2 text-xs truncate hidden lg:table-cell" style={{ color: colors.textSecondary }}>
                      {objectDetails.deterministic ? 'DETERMINISTIC' : ''}
                      {objectDetails.pipelined ? ' | PIPELINED' : ''}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  };

  // Render DDL Tab
  const renderDDLTab = () => {
    const ddl = objectDDL || activeObject?.text || activeObject?.spec || activeObject?.body || '';
    
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded p-2 sm:p-4" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.codeBg
        }}>
          <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed overflow-x-auto max-h-[calc(100vh-250px)]" style={{ 
            color: colors.text,
            fontFamily: 'Consolas, "Courier New", monospace'
          }}>
            {ddl || 'No DDL available'}
          </pre>
          <div className="sticky bottom-0 left-0 right-0 p-2 flex justify-end bg-gradient-to-t from-black/20 to-transparent">
            <button 
              className="px-3 py-1 text-xs rounded hover:bg-opacity-50 transition-colors hover-lift touch-target"
              style={{ backgroundColor: colors.hover, color: colors.text }}
              onClick={() => {
                navigator.clipboard.writeText(ddl);
              }}
              aria-label="Copy DDL to clipboard"
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
    const constraints = objectDetails?.constraints || activeObject?.constraints || [];
    
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded" style={{ 
          borderColor: colors.gridBorder,
          backgroundColor: colors.card
        }}>
          <div className="p-2 border-b" style={{ 
            borderColor: colors.gridBorder,
            backgroundColor: colors.card
          }}>
            <div className="text-sm font-medium" style={{ color: colors.text }}>
              Constraints ({constraints.length})
            </div>
          </div>
          <div className="overflow-auto max-h-[calc(100vh-300px)] sm:max-h-none">
            <table className="w-full min-w-[600px]" style={{ borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ backgroundColor: colors.tableHeader }}>
                  <th className="text-left p-2 text-xs font-medium border-b" style={{ borderColor: colors.gridBorder, color: colors.textSecondary, minWidth: '100px' }}>Name</th>
                  <th className="text-left p-2 text-xs font-medium border-b hidden sm:table-cell" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>Type</th>
                  <th className="text-left p-2 text-xs font-medium border-b hidden md:table-cell" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>Columns</th>
                  <th className="text-left p-2 text-xs font-medium border-b hidden lg:table-cell" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>References</th>
                  <th className="text-left p-2 text-xs font-medium border-b" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>Status</th>
                </tr>
              </thead>
              <tbody>
                {constraints.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="p-4 text-center text-sm" style={{ color: colors.textSecondary }}>
                      No constraints found
                    </td>
                  </tr>
                ) : (
                  constraints.map((con, index) => (
                    <tr 
                      key={con.name}
                      className="hover:bg-opacity-50 transition-colors"
                      style={{ 
                        backgroundColor: index % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                        borderBottom: `1px solid ${colors.gridBorder}`
                      }}
                    >
                      <td className="p-2 text-xs font-medium truncate max-w-[120px] sm:max-w-none" style={{ color: colors.text }}>
                        <div className="flex flex-col">
                          <span>{con.name}</span>
                          <span className="text-xs text-gray-500 sm:hidden">{con.type}</span>
                        </div>
                      </td>
                      <td className="p-2 hidden sm:table-cell">
                        <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                          con.type === 'PRIMARY KEY' ? 'bg-blue-500/10' :
                          con.type === 'FOREIGN KEY' ? 'bg-purple-500/10 text-purple-400' :
                          con.type === 'UNIQUE' ? 'bg-green-500/10 text-green-400' :
                          'bg-yellow-500/10 text-yellow-400'
                        }`}>
                          {con.type}
                        </span>
                      </td>
                      <td className="p-2 text-xs truncate hidden md:table-cell" style={{ color: colors.text }}>{con.columns}</td>
                      <td className="p-2 text-xs truncate hidden lg:table-cell" style={{ color: colors.textSecondary }}>
                        {con.refTable || '-'}
                      </td>
                      <td className="p-2">
                        <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                          con.status === 'ENABLED' ? 'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'
                        }`}>
                          {con.status}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  };

  // Render Properties Tab
  const renderPropertiesTab = () => {
    const details = objectDetails || activeObject || {};
    
    const properties = [
      { label: 'Object Name', value: details.name },
      { label: 'Owner', value: details.owner },
      { label: 'Object Type', value: details.type },
      { label: 'Status', value: details.status },
      { label: 'Created', value: details.created ? formatDateForDisplay(details.created) : '-' },
      { label: 'Last DDL Time', value: details.lastDDL ? formatDateForDisplay(details.lastDDL) : '-' },
      { label: 'Tablespace', value: details.tablespace || '-' },
      ...(details.rowCount ? [{ label: 'Row Count', value: details.rowCount.toLocaleString() }] : []),
      ...(details.size ? [{ label: 'Size', value: details.size }] : []),
      ...(details.comment ? [{ label: 'Comment', value: details.comment }] : []),
    ];

    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-medium mb-4" style={{ color: colors.text }}>
              Object Properties
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {properties.map((prop, index) => (
                <div key={index} className="space-y-1">
                  <div className="text-xs font-medium" style={{ color: colors.textSecondary }}>
                    {prop.label}
                  </div>
                  <div className="text-sm truncate" style={{ color: colors.text }}>
                    {prop.value}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  };

  // Get Tabs for Current Object Type
  const getTabsForObject = () => {
    const type = activeObject?.type?.toUpperCase();
    switch(type) {
      case 'TABLE':
        return ['Columns', 'Data', 'Constraints', 'DDL', 'Properties'];
      case 'VIEW':
        return ['Definition', 'Columns', 'Properties'];
      case 'PROCEDURE':
        return ['Parameters', 'DDL'];
      case 'FUNCTION':
        return ['Parameters', 'DDL'];
      case 'PACKAGE':
        return ['Spec', 'Body', 'Properties'];
      case 'SEQUENCE':
        return ['DDL', 'Properties'];
      case 'SYNONYM':
        return ['Properties'];
      case 'TYPE':
        return ['Attributes', 'Properties'];
      case 'TRIGGER':
        return ['Definition', 'Properties'];
      default:
        return ['Properties'];
    }
  };

  // Render Current Tab Content
  const renderTabContent = () => {
    const isMobile = window.innerWidth < 640;
    
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
      case 'spec':
      case 'body':
        return renderDDLTab();
      case 'properties':
        return renderPropertiesTab();
      default:
        return (
          <div className="flex-1 flex items-center justify-center p-4" style={{ color: colors.textSecondary }}>
            <p className="text-center">Select a tab to view details</p>
          </div>
        );
    }
  };

  // Render Context Menu
  const renderContextMenu = () => {
    if (!showContextMenu || !contextObject) return null;

    const isMobile = window.innerWidth < 768;
    const menuItems = [
      { label: 'Open', icon: <ExternalLink size={14} />, action: () => handleObjectSelect(contextObject, contextObject.type) },
      { separator: true },
      { label: 'Generate API', icon: <Code size={14} />, action: () => {
        setShowApiModal(true);
        setShowContextMenu(false);
      }},
      { label: 'Copy DDL', icon: <Copy size={14} />, action: () => {
        const ddl = contextObject.text || contextObject.spec || contextObject.body || '';
        navigator.clipboard.writeText(ddl);
        setShowContextMenu(false);
      }},
      { label: 'Properties', icon: <Settings size={14} />, action: () => {
        handleObjectSelect(contextObject, contextObject.type);
        setActiveTab('properties');
        setShowContextMenu(false);
      }},
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
          maxWidth: isMobile ? '300px' : 'none',
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
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
              className="w-full px-4 py-3 text-sm text-left hover:bg-opacity-50 transition-colors flex items-center gap-3 hover-lift touch-target"
              style={{ backgroundColor: 'transparent', color: colors.text }}
            >
              {item.icon}
              {item.label}
            </button>
          )
        ))}
        <div className="border-t mt-1" style={{ borderColor: colors.border }}>
          <button
            onClick={() => setShowContextMenu(false)}
            className="w-full px-4 py-3 text-sm text-center hover:bg-opacity-50 transition-colors hover-lift touch-target"
            style={{ backgroundColor: colors.hover, color: colors.text }}
          >
            Cancel
          </button>
        </div>
      </div>
    );
  };

  // Render Connection Manager
  const renderConnectionManager = () => {
    if (!showConnectionManager) return null;

    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="rounded w-full max-w-md max-h-[90vh] overflow-auto" style={{ 
          backgroundColor: colors.modalBg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b sticky top-0" style={{ borderColor: colors.border, backgroundColor: colors.modalBg }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Connection Manager</h3>
            <button 
              onClick={() => setShowConnectionManager(false)} 
              className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift touch-target"
              style={{ backgroundColor: colors.hover }}
              aria-label="Close connection manager"
            >
              <X size={16} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          
          <div className="p-4">
            {loading ? (
              <div className="flex items-center justify-center h-32">
                <RefreshCw className="animate-spin" size={24} style={{ color: colors.textSecondary }} />
              </div>
            ) : error ? (
              <div className="p-4 text-center">
                <AlertCircle size={24} className="mx-auto mb-2" style={{ color: colors.error }} />
                <div className="text-sm" style={{ color: colors.text }}>{error}</div>
                <button 
                  onClick={fetchSchemaConnections}
                  className="mt-3 px-4 py-2 rounded text-sm font-medium transition-colors"
                  style={{ 
                    backgroundColor: colors.hover,
                    color: colors.text
                  }}
                >
                  Retry
                </button>
              </div>
            ) : (
              <div className="space-y-3">
                {connections.map(conn => (
                  <button
                    key={conn.id}
                    className={`w-full p-3 rounded border cursor-pointer transition-colors hover-lift touch-target text-left ${
                      activeConnection === conn.id ? 'border-primary' : 'hover:border-primary/50'
                    }`}
                    style={{ 
                      backgroundColor: activeConnection === conn.id ? colors.selected : colors.card,
                      borderColor: activeConnection === conn.id ? colors.primary : colors.border
                    }}
                    onClick={() => {
                      setActiveConnection(conn.id);
                      setShowConnectionManager(false);
                    }}
                    aria-label={`Select connection ${conn.name}`}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div className="w-3 h-3 rounded-full" style={{ 
                          backgroundColor: conn.status === 'connected' ? colors.connectionOnline : 
                                         conn.status === 'disconnected' ? colors.connectionOffline : colors.connectionIdle 
                        }} />
                        <div>
                          <div className="text-sm font-medium truncate" style={{ color: colors.text }}>{conn.name}</div>
                          <div className="text-xs truncate" style={{ color: colors.textSecondary }}>
                            {conn.username}@{conn.host}
                          </div>
                        </div>
                      </div>
                      {activeConnection === conn.id && <Check size={16} style={{ color: colors.primary }} />}
                    </div>
                  </button>
                ))}
                
                <button 
                  className="w-full p-4 rounded border-2 border-dashed hover:border-primary/50 transition-colors flex items-center justify-center gap-2 hover-lift touch-target"
                  style={{ borderColor: colors.border, color: colors.text }}
                  aria-label="Add new connection"
                >
                  <Plus size={16} />
                  Add New Connection
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };

  // Close context menu on outside click
  useEffect(() => {
    const handleClickOutside = () => setShowContextMenu(false);
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, []);

  // Prevent body scroll when sidebar is open on mobile
  useEffect(() => {
    if (isLeftSidebarVisible && window.innerWidth < 768) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'auto';
    }
    
    return () => {
      document.body.style.overflow = 'auto';
    };
  }, [isLeftSidebarVisible]);

  // Update table data when dataView changes
  useEffect(() => {
    if (activeObject?.type === 'TABLE' && activeObject?.name) {
      fetchTableData(activeObject.name);
    }
  }, [dataView, activeObject, fetchTableData]);

  // Loading state
  const renderLoadingState = () => (
    <div className="flex items-center justify-center h-screen">
      <div className="text-center">
        <RefreshCw className="animate-spin mx-auto mb-4" size={24} style={{ color: colors.textSecondary }} />
        <div style={{ color: colors.text }}>Loading schema browser...</div>
      </div>
    </div>
  );

  // Error state
  const renderErrorState = () => (
    <div className="p-4 border rounded-xl" style={{ 
      borderColor: colors.error,
      backgroundColor: `${colors.error}20`
    }}>
      <div className="flex items-center gap-2">
        <AlertCircle size={16} style={{ color: colors.error }} />
        <div style={{ color: colors.error }}>{error}</div>
      </div>
      <button 
        onClick={refreshAllSchemaData}
        className="mt-2 px-3 py-1 rounded text-sm font-medium transition-colors"
        style={{ 
          backgroundColor: colors.hover,
          color: colors.text
        }}
      >
        Retry
      </button>
    </div>
  );

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
        
        .text-blue-400 { color: ${colors.primaryLight}; }
        .text-green-400 { color: ${colors.success}; }
        .text-purple-400 { color: ${isDark ? 'rgb(167 139 250)' : '#8b5cf6'}; }
        .text-orange-400 { color: ${colors.warning}; }
        .text-red-400 { color: ${colors.error}; }
        .text-gray-500 { color: ${colors.textTertiary}; }
        
        /* Custom scrollbar - Mobile optimized */
        ::-webkit-scrollbar {
          width: 6px;
          height: 6px;
        }
        
        @media (min-width: 768px) {
          ::-webkit-scrollbar {
            width: 8px;
            height: 8px;
          }
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
        
        /* Focus styles - Mobile optimized */
        input:focus, button:focus, select:focus {
          outline: 2px solid ${colors.primary}40;
          outline-offset: 2px;
        }
        
        /* Touch-friendly targets */
        .touch-target {
          min-height: 44px;
          min-width: 44px;
        }
        
        /* Hover effects - Mobile optimized */
        @media (hover: hover) {
          .hover-lift:hover {
            transform: translateY(-2px);
            transition: transform 0.2s ease;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          }
        }
        
        /* Mobile responsive breakpoints */
        @media (max-width: 480px) {
          .text-xs-mobile {
            font-size: 11px;
          }
        }
        
        /* Landscape optimizations */
        @media (orientation: landscape) and (max-height: 500px) {
          .landscape-compact {
            padding-top: 0.5rem;
            padding-bottom: 0.5rem;
          }
          
          .landscape-hide {
            display: none;
          }
        }
        
        /* Prevent text size adjustment on iOS */
        @supports (-webkit-touch-callout: none) {
          input, textarea {
            font-size: 16px !important;
          }
        }
        
        /* Collections-like styling for consistency */
        .gradient-bg {
          background: linear-gradient(135deg, ${colors.primary}20 0%, ${colors.info}20 50%, ${colors.warning}20 100%);
        }
      `}</style>

      {loading && !activeObject ? (
        renderLoadingState()
      ) : error ? (
        renderErrorState()
      ) : (
        <>
          {/* Mobile Header */}
          <div className="md:hidden flex items-center justify-between h-12 px-4 border-b" style={{ 
            backgroundColor: colors.header,
            borderColor: colors.border
          }}>
            <button 
              onClick={() => setIsLeftSidebarVisible(true)}
              className="p-2 rounded hover:bg-opacity-50 transition-colors touch-target"
              style={{ backgroundColor: colors.hover }}
              aria-label="Open menu"
            >
              <Menu size={20} style={{ color: colors.text }} />
            </button>
            <div className="flex flex-col items-center">
              <span className="text-sm font-medium truncate max-w-[200px]" style={{ color: colors.text }}>Schema Browser</span>
              <span className="text-xs truncate max-w-[200px]" style={{ color: colors.textSecondary }}>
                {connections.find(c => c.id === activeConnection)?.name || 'Loading...'} @ {connections.find(c => c.id === activeConnection)?.host || '...'}
              </span>
            </div>
            <button
              onClick={() => setShowApiModal(true)}
              className="p-2 rounded hover:bg-opacity-50 transition-colors touch-target"
              style={{ backgroundColor: colors.hover }}
              aria-label="Generate API"
            >
              <Code size={18} style={{ color: colors.primary }} />
            </button>
          </div>


          {/* TOP NAVIGATION */}
          <div className="flex items-center justify-between h-10 px-4 border-b" style={{ 
            backgroundColor: colors.header,
            borderColor: colors.border
          }}>
            <div className="flex items-center gap-4 -ml-4 text-nowrap uppercase">
              <span className={`px-3 py-1.5 text-sm font-medium rounded transition-colors hover-lift`} style={{ color: colors.text }}>Schema Browser</span>
            </div>

            <div className="flex items-center gap-2">

              {/* Global Search */}
              <div className="relative">
                <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
                <input 
                  type="text" 
                  placeholder="Search"
                  value={globalSearchQuery}
                  onChange={(e) => setGlobalSearchQuery(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleGlobalSearch()}
                  className="pl-8 pr-3 py-1.5 rounded text-sm focus:outline-none w-48 hover-lift"
                  style={{ backgroundColor: colors.inputBg, border: `1px solid ${colors.border}`, color: colors.text }} 
                />
                {globalSearchQuery && (
                  <div className="absolute right-2 top-1/2 transform -translate-y-1/2">
                    <button onClick={() => setGlobalSearchQuery('')} className="p-0.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                      style={{ backgroundColor: colors.hover }}>
                      <X size={12} style={{ color: colors.textSecondary }} />
                    </button>
                  </div>
                )}
              </div>

              {/* Code Panel Toggle */}
              <button onClick={() => setShowCodePanel(!showCodePanel)} 
                className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: showCodePanel ? colors.selected : colors.hover }}>
                <Code size={14} style={{ color: showCodePanel ? colors.primary : colors.textSecondary }} />
              </button>

              {/* Share Button */}
              <button onClick={() => handleExportSchema('JSON')} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}>
                <Share2 size={14} style={{ color: colors.textSecondary }} />
              </button>

              {/* Settings */}
              <button onClick={() => setShowConnectionManager(true)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}>
                <Settings size={14} style={{ color: colors.textSecondary }} />
              </button>

            </div>
          </div>

          {/* MAIN CONTENT AREA */}
          <div className="flex flex-1 overflow-hidden relative pb-16 md:pb-0">
            {/* Left sidebar overlay for mobile */}
            {isLeftSidebarVisible && (
              <div 
                className="fixed inset-0 bg-black bg-opacity-50 z-30 md:hidden"
                onClick={() => setIsLeftSidebarVisible(false)}
              />
            )}

            {/* LEFT SIDEBAR - Object Explorer */}
            <LeftSidebar
              isLeftSidebarVisible={isLeftSidebarVisible}
              setIsLeftSidebarVisible={setIsLeftSidebarVisible}
              showConnectionManager={showConnectionManager}
              setShowConnectionManager={setShowConnectionManager}
              filterQuery={filterQuery}
              selectedOwner={selectedOwner}
              handleFilterChange={handleFilterChange}
              handleOwnerChange={handleOwnerChange}
              handleClearFilters={handleClearFilters}
              colors={colors}
              objectTree={objectTree}
              handleToggleSection={handleToggleSection}
              schemaObjects={schemaObjects}
              activeObject={activeObject}
              handleObjectSelect={handleObjectSelect}
              getObjectIcon={getObjectIcon}
              handleContextMenu={handleContextMenu}
              activeConnection={activeConnection}
              connections={connections}
              loading={loading}
            />

            {/* MAIN WORK AREA */}
            <div className="flex-1 flex flex-col overflow-hidden" style={{ backgroundColor: colors.main }}>
              {/* TAB BAR - Mobile optimized */}
              <div className="flex items-center border-b" style={{ 
                backgroundColor: colors.card,
                borderColor: colors.border,
                minHeight: '36px'
              }}>
                <div className="flex items-center flex-1 overflow-x-auto">
                  {tabs.map(tab => (
                    <button
                      key={tab.id}
                      className={`flex items-center gap-1 sm:gap-2 px-3 py-2 border-r cursor-pointer min-w-24 sm:min-w-40 hover-lift touch-target ${
                        tab.isActive ? '' : 'hover:bg-opacity-50 transition-colors'
                      }`}
                      style={{ 
                        backgroundColor: tab.isActive ? colors.selected : 'transparent',
                        borderRightColor: colors.border,
                        borderTop: tab.isActive ? `2px solid ${colors.tabActive}` : 'none'
                      }}
                      onClick={() => {
                        const allObjects = Object.values(schemaObjects).flat();
                        const found = allObjects.find(obj => obj.id === tab.objectId);
                        if (found) {
                          handleObjectSelect(found, tab.type);
                        }
                      }}
                      aria-label={`Open ${tab.name} tab`}
                    >
                      <div className="flex items-center gap-1 sm:gap-2 flex-1 min-w-0">
                        {getObjectIcon(tab.type)}
                        <span className="text-xs sm:text-sm truncate" style={{ 
                          color: tab.isActive ? colors.tabActive : colors.tabInactive,
                          fontWeight: tab.isActive ? '600' : '400'
                        }}>
                          {tab.name}
                          {tab.isDirty && ' *'}
                        </span>
                      </div>
                      {tabs.length > 1 && (
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setTabs(tabs.filter(t => t.id !== tab.id));
                            if (tab.isActive) {
                              const remainingTab = tabs.find(t => t.id !== tab.id);
                              if (remainingTab) {
                                const allObjects = Object.values(schemaObjects).flat();
                                const found = allObjects.find(obj => obj.id === remainingTab.objectId);
                                if (found) {
                                  handleObjectSelect(found, remainingTab.type);
                                }
                              }
                            }
                          }}
                          className="p-0.5 rounded opacity-0 hover:opacity-100 hover:bg-opacity-50 transition-colors hover-lift ml-1"
                          style={{ backgroundColor: colors.hover }}
                          aria-label="Close tab"
                        >
                          <X size={12} style={{ color: colors.textSecondary }} />
                        </button>
                      )}
                    </button>
                  ))}
                  {/* <button
                    className="px-3 sm:px-4 py-2 border-r hover:bg-opacity-50 transition-colors hover-lift touch-target"
                    style={{ borderRightColor: colors.border, backgroundColor: colors.hover }}
                    onClick={() => console.log('New tab')}
                    aria-label="New tab"
                  >
                    <Plus size={14} style={{ color: colors.textSecondary }} />
                  </button> */}
                </div>
              </div>

              {/* OBJECT DETAILS AREA */}
              <div className="flex-1 overflow-hidden flex flex-col">
                {/* Object Properties Header - Mobile optimized */}
                {activeObject && (
                  <div className="p-3 border-b" style={{ 
                    borderColor: colors.border,
                    backgroundColor: colors.card
                  }}>
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                      <div className="flex items-center gap-2 sm:gap-4 flex-wrap">
                        <div className="flex items-center gap-2 flex-1 min-w-0">
                          {getObjectIcon(activeObject.type)}
                          <div className="min-w-0">
                            <span className="text-sm sm:text-base font-semibold truncate block" style={{ color: colors.text }}>
                              {activeObject.name}
                            </span>
                            <span className="text-xs truncate block" style={{ color: colors.textSecondary }}>
                              {activeObject.owner}
                            </span>
                          </div>
                          <span className="text-xs px-2 py-0.5 rounded shrink-0" style={{ 
                            backgroundColor: activeObject.status === 'VALID' ? `${colors.success}20` : `${colors.error}20`,
                            color: activeObject.status === 'VALID' ? colors.success : colors.error
                          }}>
                            {activeObject.status}
                          </span>
                        </div>
                        <div className="flex items-center gap-2 sm:gap-4 text-xs">
                          {activeObject.rowCount && (
                            <span className="truncate" style={{ color: colors.textSecondary }}>
                              {activeObject.rowCount.toLocaleString()} rows
                            </span>
                          )}
                          {activeObject.size && (
                            <span className="truncate hidden sm:inline" style={{ color: colors.textSecondary }}>
                              {activeObject.size}
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="flex items-center gap-2 mt-2 sm:mt-0 self-end">
                        <button 
                          onClick={() => setShowApiModal(true)} 
                          className="px-3 py-1.5 text-xs sm:text-sm rounded hover:opacity-90 transition-colors flex items-center gap-1 sm:gap-2 hover-lift touch-target"
                          style={{ backgroundColor: colors.primaryDark, color: colors.white }}
                          aria-label="Generate API"
                        >
                          <Code size={12} />
                          <span className="hidden sm:inline">Generate API</span>
                          <span className="sm:hidden">API</span>
                        </button>
                      </div>
                    </div>
                  </div>
                )}

                {/* Detail Tabs - Mobile optimized */}
                {activeObject && (
                  <div className="flex items-center border-b overflow-x-auto" style={{ 
                    backgroundColor: colors.card,
                    borderColor: colors.border
                  }}>
                    {getTabsForObject().map(tab => (
                      <button
                        key={tab}
                        onClick={() => setActiveTab(tab.toLowerCase())}
                        className={`px-3 sm:px-4 py-2 text-xs sm:text-sm font-medium border-b-2 transition-colors hover-lift whitespace-nowrap touch-target ${
                          activeTab === tab.toLowerCase() ? '' : 'hover:bg-opacity-50'
                        }`}
                        style={{ 
                          borderBottomColor: activeTab === tab.toLowerCase() ? colors.tabActive : 'transparent',
                          color: activeTab === tab.toLowerCase() ? colors.tabActive : colors.tabInactive,
                          backgroundColor: 'transparent',
                          minHeight: '36px'
                        }}
                        aria-label={`View ${tab}`}
                      >
                        {tab}
                      </button>
                    ))}
                  </div>
                )}

                {/* Tab Content */}
                <div className="flex-1 overflow-hidden p-3 sm:p-4" style={{ backgroundColor: colors.card }}>
                  {activeObject ? renderTabContent() : (
                    <div className="h-full flex flex-col items-center justify-center p-4">
                      <Database size={48} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mb-4" />
                      <p className="text-sm text-center" style={{ color: colors.textSecondary }}>
                        Loading schema objects...
                      </p>
                      {loading && (
                        <RefreshCw className="animate-spin mt-4" size={20} style={{ color: colors.textSecondary }} />
                      )}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Mobile Bottom Navigation */}
          <MobileBottomNav
            isLeftSidebarVisible={isLeftSidebarVisible}
            setIsLeftSidebarVisible={setIsLeftSidebarVisible}
            setShowApiModal={setShowApiModal}
            schemaObjects={schemaObjects}
            tabs={tabs}
            handleObjectSelect={handleObjectSelect}
            toggleTheme={toggleTheme}
            isDark={isDark}
            colors={colors}
            loading={loading}
            onRefreshSchema={refreshAllSchemaData}
          />

          {/* STATUS BAR - Desktop only */}
          <div className="hidden md:flex items-center justify-between h-8 px-4 text-xs border-t" style={{ 
            backgroundColor: colors.header,
            color: colors.textSecondary,
            borderColor: colors.border
          }}>
            <div className="flex items-center gap-4 overflow-x-auto">
              <span>
                {connections.find(c => c.id === activeConnection)?.name || 'No connection'} @ 
                {connections.find(c => c.id === activeConnection)?.host || '...'}:
                {connections.find(c => c.id === activeConnection)?.port || '...'}/
                {connections.find(c => c.id === activeConnection)?.service || '...'}
              </span>
              <span className="opacity-75">|</span>
              <span>{Object.values(schemaObjects).flat().length} Objects</span>
              <span className="opacity-75">|</span>
              <span>{activeObject ? `Selected: ${activeObject.name}` : 'Ready'}</span>
            </div>
            <div className="hidden md:flex items-center gap-4">
              <span>F4: Describe</span>
              <span>F5: Refresh</span>
              <span>F6: Switch Panels</span>
              <span>F9: Execute</span>
            </div>
          </div>

          {/* MODALS & CONTEXT MENUS */}
          {showContextMenu && (
            <div 
              className="fixed inset-0 z-40"
              onClick={() => setShowContextMenu(false)}
            >
              {renderContextMenu()}
            </div>
          )}
          {renderConnectionManager()}

          {showApiModal && (
            <ApiGenerationModal
              isOpen={showApiModal}
              onClose={() => setShowApiModal(false)}
              selectedObject={selectedForApiGeneration || activeObject}
              colors={colors}
              theme={theme}
              onGenerateAPI={handleGenerateAPI}
            />
          )}
        </>
      )}
    </div>
  );
};

export default SchemaBrowser;