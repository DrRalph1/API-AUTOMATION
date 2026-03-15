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
  FileCode, ChevronsUp, ChevronsDown, AlertTriangle, Menu, Loader, Tag,
  GitBranch as DependencyIcon // Add this for used by tab
} from 'lucide-react';
import ApiGenerationModal from '@/components/modals/ApiGenerationModal.js';

// Import OracleSchemaController
import {
  getCurrentSchemaInfo,
  getAllTablesForFrontendPaginated,
  getTableDetailsForFrontend,
  getTableData,
  getAllViewsForFrontendPaginated,
  getAllProceduresForFrontendPaginated,
  getAllFunctionsForFrontendPaginated,
  getAllPackagesForFrontendPaginated,
  getAllTriggersForFrontendPaginated,
  getAllSynonymsForFrontendPaginated,
  getAllSequencesForFrontendPaginated,
  getAllTypesForFrontendPaginated,
  getObjectDetails,
  searchObjectsPaginated,
  getObjectDDL,
  extractPaginatedData,
  handleSchemaBrowserResponse,
  extractObjectDetails,
  extractTableData,
  extractDDL,
  formatBytes,
  formatDateForDisplay,
  getObjectTypeIcon,
  isSupportedForAPIGeneration,
  generateSampleQuery,
  // New imports for used by functionality
  getUsedByPaginated,
  getUsedBySummary,
  getDependencyHierarchy,
  getUsedByCount,
  extractUsedByItems,
  extractUsedBySummary,
  extractDependencyHierarchy,
  hasDependencies,
  hasCircularDependencies,
  getDependencyGraph,
  getDependencyImpact
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

// Simple cache with TTL
const objectCache = new Map();
const CACHE_TTL = 5 * 60 * 1000; // 5 minutes

// Track ongoing requests to prevent duplicates
const ongoingRequests = new Map();

// Full page loader component
const FullPageLoader = ({ colors }) => (
  <div className="fixed inset-0 flex items-center justify-center z-50" style={{ backgroundColor: colors.bg }}>
    <div className="text-center">
      <div className="relative">
        <Loader className="animate-spin mx-auto mb-6" size={64} style={{ color: colors.primary }} />
        <div className="absolute inset-0 flex items-center justify-center">
          <Database size={32} style={{ color: colors.primary, opacity: 0.3 }} />
        </div>
      </div>
      <h2 className="text-xl font-semibold mb-2" style={{ color: colors.text }}>Loading Schema Browser</h2>
      <p className="text-sm" style={{ color: colors.textSecondary }}>Please wait while we connect to the database...</p>
    </div>
  </div>
);

// Skeleton loader for sidebar
const SidebarSkeleton = ({ colors }) => (
  <div className="p-3 space-y-4">
    {[1, 2, 3, 4, 5, 6, 7, 8, 9].map(i => (
      <div key={i} className="animate-pulse">
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded" style={{ backgroundColor: colors.border }}></div>
            <div className="w-20 h-4 rounded" style={{ backgroundColor: colors.border }}></div>
          </div>
          <div className="w-6 h-4 rounded" style={{ backgroundColor: colors.border }}></div>
        </div>
      </div>
    ))}
  </div>
);

// FilterInput Component - Updated
const FilterInput = React.memo(({ 
  filterQuery, 
  selectedOwner, 
  owners,
  onFilterChange,
  onOwnerChange, 
  onClearFilters,
  onSearch,
  onCancelSearch,
  colors,
  loading,
  isSearching
}) => {
  const searchInputRef = useRef(null);
  const [localFilterValue, setLocalFilterValue] = useState(filterQuery);

  // Update local value when prop changes
  useEffect(() => {
    setLocalFilterValue(filterQuery);
  }, [filterQuery]);

  const handleFilterChange = useCallback((e) => {
    const value = e.target.value;
    setLocalFilterValue(value);
    // Still call onFilterChange to update parent state for the input value
    onFilterChange(value);
  }, [onFilterChange]);

  const handleOwnerChange = useCallback((e) => {
    onOwnerChange(e.target.value);
  }, [onOwnerChange]);

  const handleClearFilter = useCallback(() => {
    setLocalFilterValue('');
    onFilterChange('');
    setTimeout(() => searchInputRef.current?.focus(), 10);
  }, [onFilterChange]);

  const handleClearAllFilters = useCallback(() => {
    setLocalFilterValue('');
    onFilterChange('');
    onOwnerChange('ALL');
    onClearFilters();
    setTimeout(() => searchInputRef.current?.focus(), 10);
  }, [onFilterChange, onOwnerChange, onClearFilters]);

  const handleKeyDown = useCallback((e) => {
    if (e.key === 'Enter' && localFilterValue && localFilterValue.length >= 2) {
      e.preventDefault();
      onSearch(localFilterValue);
    }
  }, [localFilterValue, onSearch]);

  return (
    <>
      <div className="p-3 border-b" style={{ borderColor: colors.border }}>
        <div className="space-y-2">
          <div className="relative">
            <input
              ref={searchInputRef}
              type="text"
              placeholder={loading ? "Loading..." : "Filter objects..."}
              value={localFilterValue}
              onChange={handleFilterChange}
              onKeyDown={handleKeyDown}
              disabled={loading || isSearching}
              className="w-full pl-3 pr-20 py-2.5 rounded text-sm focus:outline-none"
              style={{ 
                backgroundColor: colors.inputBg,
                border: `1px solid ${colors.inputBorder}`,
                color: colors.text,
                opacity: (loading || isSearching) ? 0.6 : 1
              }}
            />
            <div className="absolute right-2 top-1/2 transform -translate-y-1/2 flex gap-1">
              {localFilterValue && !loading && !isSearching && (
                <button 
                  onClick={handleClearFilter}
                  className="p-1 rounded hover:bg-opacity-50 transition-colors"
                  style={{ backgroundColor: colors.hover }}
                  title="Clear filter"
                >
                  <X size={14} style={{ color: colors.textSecondary }} />
                </button>
              )}
              {isSearching ? (
                <button 
                  onClick={onCancelSearch}
                  className="p-1 rounded hover:bg-opacity-50 transition-colors"
                  style={{ backgroundColor: colors.error + '20', color: colors.error }}
                  title="Cancel search"
                >
                  <X size={16} />
                </button>
              ) : (
                <button 
                  onClick={() => onSearch(localFilterValue)}
                  disabled={!localFilterValue || localFilterValue.length < 2 || loading}
                  className="p-1 rounded hover:bg-opacity-50 transition-colors disabled:opacity-50"
                  style={{ 
                    backgroundColor: colors.primary + '20',
                    color: colors.primary
                  }}
                  title="Search"
                >
                  <Search size={16} />
                </button>
              )}
            </div>
          </div>
        </div>
      </div>

      {(localFilterValue || selectedOwner !== 'ALL') && !loading && (
        <div className="px-3 py-2 border-b" style={{ borderColor: colors.border, backgroundColor: colors.hover }}>
          <div className="flex items-center gap-2">
            <div className="flex-1 min-w-0 overflow-x-auto scrollbar-hide">
              <span className="text-xs whitespace-nowrap" style={{ color: colors.textSecondary }}>
                Filtering {localFilterValue && `by: "${localFilterValue}"`} {localFilterValue && selectedOwner !== 'ALL' && ' • '} 
                {selectedOwner !== 'ALL' && `Owner: ${selectedOwner}`}
              </span>
            </div>
            <button 
              onClick={handleClearAllFilters}
              className="text-xs px-2 py-1 rounded flex-shrink-0"
              style={{ backgroundColor: colors.border, color: colors.text }}
            >
              Clear All
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
  totalCount = 0,
  isLoading,
  isExpanded, 
  onToggle,
  onSelectObject,
  onLoadSection,
  onLoadMore,
  activeObjectId,
  filterQuery,
  selectedOwner,
  colors,
  getObjectIcon,
  handleContextMenu,
  isLoaded,
  currentPage = 1,
  totalPages = 1,
  hasActiveFilter = false,
  isFiltering = false
}) => {
  
  useEffect(() => {
    // Auto-expand when there's an active filter or when filtering is in progress
    if ((hasActiveFilter || filterQuery) && !isExpanded && !isLoading) {
      onToggle(type);
    }
  }, [hasActiveFilter, filterQuery, isExpanded, isLoading, onToggle, type]);
  
  useEffect(() => {
    // Load section if expanded and not loaded (only when not filtering)
    if (isExpanded && !isLoaded && !isLoading && !hasActiveFilter && !isFiltering) {
      Logger.debug('ObjectTreeSection', 'useEffect', `Loading ${title} on expand`);
      onLoadSection(type);
    }
  }, [isExpanded, isLoaded, isLoading, onLoadSection, type, title, hasActiveFilter, isFiltering]);
  
  const filteredObjects = objects || [];
  
  // Display count based on filter state and whether we're filtering
  const displayCount = hasActiveFilter ? filteredObjects.length : totalCount;
  const hasObjects = filteredObjects.length > 0;
  
  const handleToggle = useCallback(() => {
    onToggle(type);
  }, [onToggle, type]);
  
  const handleObjectClick = useCallback((obj) => {
    if (typeof onSelectObject === 'function') {
      if (obj && obj.name) {
        const objectType = type.slice(0, -1).toUpperCase();
        onSelectObject(obj, objectType);
      }
    }
  }, [onSelectObject, type]);
  
  const handleDoubleClick = useCallback((obj) => {
    if (typeof onSelectObject === 'function' && obj && obj.name) {
      onSelectObject(obj, type.slice(0, -1).toUpperCase());
    }
  }, [onSelectObject, type]);
  
  const handleContextMenuWrapper = useCallback((e, obj) => {
    e.preventDefault();
    e.stopPropagation();
    if (obj && obj.name && typeof handleContextMenu === 'function') {
      handleContextMenu(e, obj, type.slice(0, -1).toUpperCase());
    }
  }, [handleContextMenu, type]);
  
  const getObjectId = useCallback((obj) => {
    return obj.id || `${obj.owner || 'unknown'}_${obj.name}`;
  }, []);
  
  const hasMorePages = currentPage < totalPages;
  const showLoadMore = hasMorePages && filteredObjects.length < totalCount && !hasActiveFilter && !isFiltering;
  
  return (
    <div className="mb-1">
      <button
        onClick={handleToggle}
        className="flex items-center justify-between w-full px-2 py-2 hover:bg-opacity-50 transition-colors rounded-sm text-sm font-medium"
        style={{ backgroundColor: colors.hover }}
        disabled={isFiltering}
      >
        <div className="flex items-center gap-2 min-w-0 flex-1">
          {isLoading && !hasActiveFilter && !isFiltering ? (
            <Loader size={14} className="animate-spin" style={{ color: colors.textSecondary }} />
          ) : isFiltering ? (
            <Loader size={14} className="animate-spin" style={{ color: colors.primary }} />
          ) : isExpanded ? (
            <ChevronDown size={14} style={{ color: colors.textSecondary }} />
          ) : (
            <ChevronRight size={14} style={{ color: colors.textSecondary }} />
          )}
          {getObjectIcon(type.slice(0, -1))}
          <span className="truncate text-xs sm:text-sm">{title}</span>
          {isFiltering && (
            <span className="text-xs ml-2" style={{ color: colors.primary }}>searching...</span>
          )}
        </div>
        <span 
          className="text-xs px-1.5 py-0.5 rounded shrink-0 min-w-6 text-center" 
          style={{ 
            backgroundColor: colors.border,
            color: displayCount > 0 ? colors.textSecondary : colors.error,
            opacity: displayCount === 0 ? 0.7 : 1
          }}
          title={hasActiveFilter ? `${filteredObjects.length} of ${totalCount} total` : `Total: ${totalCount}`}
        >
          {displayCount}
        </span>
      </button>
      
      {isExpanded && (
        <div className="ml-6 mt-0.5 space-y-0.5">
          {isFiltering ? (
            <div className="px-2 py-4 text-center">
              <Loader className="animate-spin mx-auto" size={20} style={{ color: colors.primary }} />
              <span className="text-xs mt-2 block" style={{ color: colors.textSecondary }}>
                Searching for matching objects...
              </span>
            </div>
          ) : isLoading && !hasActiveFilter && !filteredObjects.length ? (
            <div className="px-2 py-3 text-center">
              <Loader className="animate-spin mx-auto" size={14} style={{ color: colors.textSecondary }} />
              <span className="text-xs mt-2 block" style={{ color: colors.textTertiary }}>
                Loading...
              </span>
            </div>
          ) : !filteredObjects.length ? (
            <div className="px-2 py-3 text-center">
              <span className="text-xs" style={{ color: colors.textTertiary }}>
                {hasActiveFilter ? 'No matching objects found' : 'No objects found'}
              </span>
            </div>
          ) : (
            <>
              {filteredObjects.map((obj, index) => {
                const objectId = getObjectId(obj);
                const isSynonym = obj.objectType === 'SYNONYM' || obj.type === 'SYNONYM';
                const isInvalid = obj.status && obj.status !== 'VALID' && obj.status !== 'ENABLED';
                
                return (
                  <button
                    key={objectId}
                    onDoubleClick={() => handleDoubleClick(obj)}
                    onContextMenu={(e) => handleContextMenuWrapper(e, obj)}
                    onClick={() => handleObjectClick(obj)}
                    className={`flex items-center justify-between w-full px-2 py-2 rounded-sm cursor-pointer group text-left transition-colors ${
                      activeObjectId === objectId ? 'font-medium' : 'hover:bg-opacity-50'
                    }`}
                    style={{
                      backgroundColor: activeObjectId === objectId ? colors.selected : 'transparent',
                      color: activeObjectId === objectId ? colors.primary : colors.text
                    }}
                  >
                    <div className="flex items-center gap-2 min-w-0 flex-1">
                      {getObjectIcon(type.slice(0, -1))}
                      <span className="text-xs sm:text-sm truncate">{obj.name}</span>
                      {isSynonym && (
                        <span 
                          className="text-[10px] px-1.5 py-0.5 rounded-full ml-1 shrink-0 font-medium"
                          style={{ 
                            backgroundColor: colors.objectType.synonym + '20',
                            color: colors.objectType.synonym
                          }}
                        >
                          SYNONYM
                        </span>
                      )}
                    </div>
                    {isInvalid && (
                      <div className="flex items-center gap-1 shrink-0">
                        <span 
                          className="text-[10px] px-1.5 py-0.5 rounded-full"
                          style={{ 
                            backgroundColor: colors.error + '20',
                            color: colors.error
                          }}
                        >
                          {/* {obj.status} */}
                        </span>
                        <AlertCircle size={10} style={{ color: colors.error }} />
                      </div>
                    )}
                  </button>
                );
              })}
              
              {/* Load More Button */}
              {showLoadMore && (
                <button
                  onClick={() => onLoadMore(type)}
                  disabled={isLoading}
                  className="w-full px-2 py-1.5 mt-2 text-xs rounded hover:bg-opacity-50 transition-colors flex items-center justify-center gap-1 border-t pt-2"
                  style={{ 
                    backgroundColor: colors.hover,
                    color: colors.textSecondary,
                    borderColor: colors.border
                  }}
                >
                  {isLoading ? (
                    <>
                      <Loader size={12} className="animate-spin" />
                      <span>Loading more {title.toLowerCase()}...</span>
                    </>
                  ) : (
                    <>
                      <ChevronDown size={12} />
                      <span>Load more {title.toLowerCase()} ({filteredObjects.length} of {totalCount})</span>
                    </>
                  )}
                </button>
              )}
            </>
          )}
          
          {/* Show total count info when filtered */}
          {hasActiveFilter && filteredObjects.length > 0 && filteredObjects.length < totalCount && (
            <div className="px-2 py-1 mt-1 text-xs text-center italic" style={{ color: colors.textTertiary }}>
              Showing {filteredObjects.length} of {totalCount} total {title.toLowerCase()}
            </div>
          )}
        </div>
      )}
    </div>
  );
});

ObjectTreeSection.displayName = 'ObjectTreeSection';

// LeftSidebar Component
const LeftSidebar = React.memo(({ 
  isLeftSidebarVisible, 
  setIsLeftSidebarVisible,
  filterQuery,
  selectedOwner,
  owners,
  handleFilterChange,
  handleOwnerChange,
  handleClearFilters,
  handleSearch,           
  handleCancelSearch,     
  colors,
  objectTree,
  handleToggleSection,
  handleLoadSection,
  handleLoadMore,
  schemaObjects,
  filteredResults,
  loadingStates,
  activeObject,
  handleObjectSelect,
  getObjectIcon,
  handleContextMenu,
  loading,
  onRefreshSchema,
  schemaInfo,
  loadedSections,
  pagination,
  isInitialLoad,
  hasActiveFilter,
  isFiltering
}) => {
  
  const handleCloseSidebar = useCallback(() => {
    setIsLeftSidebarVisible(false);
  }, [setIsLeftSidebarVisible]);

  // Helper function to filter objects - UPDATED
  const filterObjects = useCallback((objects, type) => {
    // If we have search results (after clicking search), show them
    if (hasActiveFilter && filteredResults && filteredResults[type]) {
      return filteredResults[type] || [];
    }
    
    // If there's no active filter, show all objects
    if (!hasActiveFilter) {
      return objects || [];
    }
    
    // If we get here, there's an active filter but no results for this type
    return [];
  }, [hasActiveFilter, filteredResults]);

  // Helper function to get the appropriate count for display - UPDATED
  const getDisplayCount = useCallback((type, totalCount) => {
    if (hasActiveFilter && filteredResults && filteredResults[type]) {
      return filteredResults[type].length;
    }
    // When not filtering, show the actual total count
    return totalCount;
  }, [hasActiveFilter, filteredResults]);

  // Define the order of sections with their display names and types
  const sectionDefinitions = [
    { title: 'Procedures', type: 'procedures', iconType: 'procedure' },
    { title: 'Views', type: 'views', iconType: 'view' },
    { title: 'Functions', type: 'functions', iconType: 'function' },
    { title: 'Packages', type: 'packages', iconType: 'package' },
    { title: 'Tables', type: 'tables', iconType: 'table' },
    { title: 'Sequences', type: 'sequences', iconType: 'sequence' },
    { title: 'Synonyms', type: 'synonyms', iconType: 'synonym' },
    { title: 'Types', type: 'types', iconType: 'type' },
    { title: 'Triggers', type: 'triggers', iconType: 'trigger' }
  ];

  // Sort sections based on filtered results
  const sortedSections = useMemo(() => {
    if (!hasActiveFilter || !filteredResults) {
      return sectionDefinitions;
    }

    return [...sectionDefinitions].sort((a, b) => {
      const aHasItems = filteredResults[a.type]?.length > 0;
      const bHasItems = filteredResults[b.type]?.length > 0;

      if (aHasItems === bHasItems) {
        const aIndex = sectionDefinitions.findIndex(s => s.type === a.type);
        const bIndex = sectionDefinitions.findIndex(s => s.type === b.type);
        return aIndex - bIndex;
      }

      return aHasItems ? -1 : 1;
    });
  }, [hasActiveFilter, filteredResults, sectionDefinitions]);

  // Render schema info in header
  const renderSchemaInfo = () => {
    if (!schemaInfo) return null;
    
    return (
      <div className="px-3 py-2 border-b text-xs" style={{ borderColor: colors.border, backgroundColor: colors.hover }}>
        {schemaInfo.databaseVersion && (
          <div className="flex items-center gap-2 mt-1">
            <Cpu size={12} style={{ color: colors.textTertiary }} />
            <span className="truncate text-[10px]" style={{ color: colors.textTertiary }}>
              {schemaInfo.databaseVersion.split(' ').slice(0, 4).join(' ')}
            </span>
          </div>
        )}
        {schemaInfo.objectCounts && schemaInfo.objectCounts.total > 0 && (
          <div className="flex items-center gap-2 mt-1">
            <Database size={12} style={{ color: colors.textTertiary }} />
            <span className="truncate text-[10px]" style={{ color: colors.textTertiary }}>
              {schemaInfo.objectCounts.total.toLocaleString()} Schema Objects
            </span>
          </div>
        )}
      </div>
    );
  };

  // Show skeleton during initial load
  if (isInitialLoad && !loadedSections.procedures) {
    return (
      <div className={`w-full md:w-64 border-r flex flex-col absolute md:relative inset-y-0 left-0 z-40 transform transition-transform duration-300 ease-in-out ${
        isLeftSidebarVisible ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
      }`} style={{ 
        borderColor: colors.border,
        width: '16vw',
        minWidth: '250px',
        maxWidth: '320px',
        backgroundColor: colors.sidebar
      }}>
        <div className="p-3 border-b" style={{ borderColor: colors.border }}>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Database size={16} style={{ color: colors.primary }} />
              <span className="text-sm font-medium" style={{ color: colors.text }}>Schema Browser</span>
            </div>
          </div>
        </div>
        <SidebarSkeleton colors={colors} />
      </div>
    );
  }

  return (
    <div className={`w-full md:w-64 border-r flex flex-col absolute md:relative inset-y-0 left-0 z-40 transform transition-transform duration-300 ease-in-out ${
      isLeftSidebarVisible ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
    }`} style={{ 
      borderColor: colors.border,
      width: '16vw',
      minWidth: '250px',
      maxWidth: '320px',
      backgroundColor: colors.sidebar
    }}>

      {/* Schema Browser Header */}
      <div className="p-3 border-b shrink-0" style={{ borderColor: colors.border }}>
        <div className="flex items-center justify-between mb-0">
          <div className="flex items-center gap-2 flex-1 text-left">
            <Database size={16} style={{ color: colors.primary }} />
            <span className="text-sm font-medium truncate" style={{ color: colors.text }}>
              {schemaInfo.currentUser || schemaInfo.schemaName}
            </span>
          </div>
          <div className="flex gap-1 shrink-0">
            <button 
              className="rounded hover:bg-opacity-50 transition-colors flex items-center justify-center w-8 h-8"
              style={{ backgroundColor: colors.hover }}
              onClick={onRefreshSchema}
              disabled={loading || isFiltering}
              title="Refresh schema"
            >
              <RefreshCw size={12} style={{ color: colors.textSecondary }} className={loading || isFiltering ? 'animate-spin' : ''} />
            </button>
            <button 
              className="md:hidden rounded hover:bg-opacity-50 transition-colors flex items-center justify-center w-8 h-8"
              style={{ backgroundColor: colors.hover }}
              onClick={handleCloseSidebar}
              title="Close sidebar"
            >
              <X size={12} style={{ color: colors.textSecondary }} />
            </button>
          </div>
        </div>
      </div>

      {/* Schema Info */}
      {renderSchemaInfo()}

      {/* Filter Input Component */}
      <FilterInput
        filterQuery={filterQuery}
        selectedOwner={selectedOwner}
        owners={owners}
        onFilterChange={handleFilterChange}
        onOwnerChange={handleOwnerChange}
        onClearFilters={handleClearFilters}
        onSearch={handleSearch}
        onCancelSearch={handleCancelSearch}
        colors={colors}
        loading={loading}
        isSearching={isFiltering}
      />

      {/* Filter Stats when active */}
      {hasActiveFilter && !isFiltering && (
        <div className="px-3 py-2 border-b text-xs" style={{ borderColor: colors.border, backgroundColor: colors.hover }}>
          <div className="flex items-center justify-between">
            <span style={{ color: colors.textSecondary }}>
              Found: {
                Object.values(filteredResults || {}).reduce((acc, curr) => acc + (curr?.length || 0), 0)
              } results
            </span>
            <span style={{ color: colors.textTertiary }}>
              for "{filterQuery}"
            </span>
          </div>
        </div>
      )}

      {/* Object Tree - Scrollable Area */}
      <div className="flex-1 overflow-y-auto overflow-x-hidden p-3 scrollbar-thin scrollbar-thumb-rounded" 
           style={{ 
             scrollbarWidth: 'thin',
             scrollbarColor: `${colors.border} transparent`
           }}>
        <div className="space-y-1">
          {/* Dynamically render sections in sorted order */}
          {sortedSections.map(section => (
            <ObjectTreeSection
              key={section.type}
              title={section.title}
              type={section.type}
              objects={filterObjects(schemaObjects[section.type] || [], section.type)}
              totalCount={getDisplayCount(section.type, schemaObjects[`${section.type}TotalCount`] || 0)}
              isLoading={loadingStates[section.type]}
              isExpanded={objectTree[section.type]}
              onToggle={handleToggleSection}
              onLoadSection={handleLoadSection}
              onLoadMore={handleLoadMore}
              activeObjectId={activeObject?.id}
              filterQuery={filterQuery}
              selectedOwner={selectedOwner}
              colors={colors}
              getObjectIcon={() => getObjectIcon(section.iconType)}
              handleContextMenu={handleContextMenu}
              isLoaded={loadedSections[section.type]}
              currentPage={pagination[section.type]?.page || 1}
              totalPages={pagination[section.type]?.totalPages || 1}
              onSelectObject={handleObjectSelect}
              hasActiveFilter={hasActiveFilter}
              isFiltering={isFiltering}
            />
          ))}
        </div>

        {/* Loading More Indicator */}
        {(loadingStates.procedures || loadingStates.views || loadingStates.functions || 
          loadingStates.tables || loadingStates.packages || loadingStates.sequences || 
          loadingStates.synonyms || loadingStates.types || loadingStates.triggers) && !hasActiveFilter && (
          <div className="flex items-center justify-center gap-2 py-4 mt-2 border-t" style={{ borderColor: colors.border }}>
            <Loader size={14} className="animate-spin" style={{ color: colors.primary }} />
            <span className="text-xs" style={{ color: colors.textSecondary }}>Loading more objects...</span>
          </div>
        )}

        {/* No Results Message when filtering */}
        {hasActiveFilter && !isFiltering && 
         Object.values(filteredResults || {}).reduce((acc, curr) => acc + (curr?.length || 0), 0) === 0 && (
          <div className="flex flex-col items-center justify-center py-8 px-4 text-center">
            <Search size={32} style={{ color: colors.textTertiary, opacity: 0.5 }} />
            <p className="text-sm mt-2 font-medium" style={{ color: colors.text }}>No matching objects found</p>
            <p className="text-xs mt-1" style={{ color: colors.textSecondary }}>
              Try adjusting your search or clear filters
            </p>
            <button
              onClick={handleClearFilters}
              className="mt-4 px-3 py-1.5 text-xs rounded hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.hover, color: colors.text }}
            >
              Clear Filters
            </button>
          </div>
        )}

        {/* Empty State when no objects loaded */}
        {!hasActiveFilter && !isFiltering && 
         !schemaObjects.procedures?.length && !schemaObjects.views?.length && 
         !schemaObjects.functions?.length && !schemaObjects.tables?.length && 
         !schemaObjects.packages?.length && !schemaObjects.sequences?.length && 
         !schemaObjects.synonyms?.length && !schemaObjects.types?.length && 
         !schemaObjects.triggers?.length && !isInitialLoad && (
          <div className="flex flex-col items-center justify-center py-8 px-4 text-center">
            <Database size={32} style={{ color: colors.textTertiary, opacity: 0.5 }} />
            <p className="text-sm mt-2" style={{ color: colors.textSecondary }}>
              No objects found in schema
            </p>
            <button
              onClick={onRefreshSchema}
              className="mt-4 px-3 py-1.5 text-xs rounded hover:bg-opacity-50 transition-colors flex items-center gap-2"
              style={{ backgroundColor: colors.hover, color: colors.text }}
              disabled={loading}
            >
              <RefreshCw size={12} className={loading ? 'animate-spin' : ''} />
              Refresh
            </button>
          </div>
        )}
      </div>

      {/* Sidebar Footer */}
      <div className="p-3 border-t shrink-0 text-[10px]" style={{ borderColor: colors.border, color: colors.textTertiary }}>
        <div className="flex items-center justify-between">
          <span>Total Objects:</span>
          <span className="font-mono">
            {(schemaObjects.synonymsTotalCount || 0).toLocaleString()}
          </span>
        </div>
        {hasActiveFilter && !isFiltering && (
          <div className="flex items-center justify-between mt-1 text-[9px]" style={{ color: colors.primary }}>
            <span>Filtered:</span>
            <span className="font-mono">
              {Object.values(filteredResults || {}).reduce((acc, curr) => acc + (curr?.length || 0), 0)}
            </span>
          </div>
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

// ============================================================
// Used By Tab Components (FIXED)
// ============================================================

// Used By Summary Component - With better debugging
const UsedBySummary = ({ data, colors, onRefresh }) => {
  console.log('🔍 UsedBySummary received data:', data);
  
  // Check if data exists and has byType array
  if (!data) {
    console.log('⚠️ No data provided to UsedBySummary');
    return (
      <div className="flex flex-col items-center justify-center py-8 px-4">
        <DependencyIcon size={48} style={{ color: colors.textTertiary, opacity: 0.5 }} />
        <p className="text-sm mt-4" style={{ color: colors.textSecondary }}>
          No dependency information available
        </p>
      </div>
    );
  }

  if (!data.byType || !Array.isArray(data.byType) || data.byType.length === 0) {
    console.log('⚠️ data.byType is missing or empty:', data.byType);
    return (
      <div className="flex flex-col items-center justify-center py-8 px-4">
        <DependencyIcon size={48} style={{ color: colors.textTertiary, opacity: 0.5 }} />
        <p className="text-sm mt-4" style={{ color: colors.textSecondary }}>
          No dependency information available
        </p>
      </div>
    );
  }

  console.log('✅ Rendering summary with byType:', data.byType);

  return (
    <div className="p-4 border-b" style={{ borderColor: colors.border }}>
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-medium" style={{ color: colors.text }}>
          Summary by Type
        </h3>
        <button
          onClick={onRefresh}
          className="p-1 rounded hover:bg-opacity-50 transition-colors"
          style={{ color: colors.textSecondary }}
          title="Refresh summary"
        >
          <RefreshCw size={14} />
        </button>
      </div>
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
        {data.byType.map((item, index) => {
          console.log(`📦 Item ${index}:`, item);
          
          // Access fields directly - they should be uppercase as in your example
          const dependentType = item.DEPENDENT_TYPE || 'Unknown';
          const count = item.COUNT || 0;
          const validCount = item.VALID_COUNT || 0;
          const invalidCount = item.INVALID_COUNT || 0;
          
          return (
            <div
              key={index}
              className="p-3 rounded-lg border"
              style={{ 
                backgroundColor: colors.card,
                borderColor: colors.border
              }}
            >
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>
                {dependentType}
              </div>
              <div className="text-xl font-semibold" style={{ color: colors.text }}>
                {count}
              </div>
              <div className="flex items-center gap-2 mt-1">
                <span className="text-xs" style={{ color: colors.success }}>
                  ✓ {validCount}
                </span>
                {invalidCount > 0 && (
                  <span className="text-xs" style={{ color: colors.error }}>
                    ✗ {invalidCount}
                  </span>
                )}
              </div>
            </div>
          );
        })}
      </div>
      <div className="mt-3 text-xs text-right" style={{ color: colors.textTertiary }}>
        Total: {data.totalCount || 0} dependencies
      </div>
    </div>
  );
};


// Used By List Component - Updated to handle both uppercase and lowercase
const UsedByList = ({ 
  items, 
  totalCount, 
  totalPages,
  page,
  pageSize,
  onPageChange,
  onPageSizeChange,
  onSelectObject,
  colors,
  loading,
  getObjectIcon
}) => {
  // Helper function to get field value regardless of case
  const getFieldValue = (item, fieldName) => {
    const upperField = fieldName.toUpperCase();
    const lowerField = fieldName.toLowerCase();
    
    return item[upperField] !== undefined ? item[upperField] : 
           item[lowerField] !== undefined ? item[lowerField] : 
           item[fieldName];
  };

  return (
    <div className="flex-1 overflow-auto">
      {/* Header */}
      <div className="flex items-center justify-between p-3 border-b" style={{ borderColor: colors.border }}>
        <div className="flex items-center gap-4">
          <span className="text-sm font-medium" style={{ color: colors.text }}>
            Dependencies ({totalCount})
          </span>
          <select
            value={pageSize}
            onChange={(e) => onPageSizeChange(parseInt(e.target.value))}
            className="px-2 py-1 text-xs border rounded"
            style={{ 
              backgroundColor: colors.bg,
              borderColor: colors.border,
              color: colors.text
            }}
            disabled={loading}
          >
            <option value="5">5 per page</option>
            <option value="10">10 per page</option>
            <option value="25">25 per page</option>
            <option value="50">50 per page</option>
          </select>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xs" style={{ color: colors.textSecondary }}>
            Page {page} of {totalPages}
          </span>
          <button
            onClick={() => onPageChange(page - 1)}
            disabled={loading || page <= 1}
            className="p-1 rounded hover:bg-opacity-50 disabled:opacity-50"
            style={{ color: colors.text }}
          >
            <ChevronLeft size={16} />
          </button>
          <button
            onClick={() => onPageChange(page + 1)}
            disabled={loading || page >= totalPages}
            className="p-1 rounded hover:bg-opacity-50 disabled:opacity-50"
            style={{ color: colors.text }}
          >
            <ChevronRight size={16} />
          </button>
        </div>
      </div>

      {/* List */}
      <div className="p-3 space-y-2">
        {loading ? (
          <div className="flex flex-col items-center justify-center py-12">
            <Loader className="animate-spin mb-4" size={32} style={{ color: colors.primary }} />
            <span className="text-sm" style={{ color: colors.textSecondary }}>
              Loading dependencies...
            </span>
          </div>
        ) : items.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-12">
            <DependencyIcon size={48} style={{ color: colors.textTertiary, opacity: 0.5 }} />
            <p className="text-sm mt-4" style={{ color: colors.textSecondary }}>
              No dependencies found
            </p>
          </div>
        ) : (
          items.map((item, index) => {
            const name = getFieldValue(item, 'NAME') || getFieldValue(item, 'OBJECT_NAME') || getFieldValue(item, 'name');
            const type = getFieldValue(item, 'DEPENDENT_TYPE') || getFieldValue(item, 'TYPE') || getFieldValue(item, 'type');
            const owner = getFieldValue(item, 'OWNER') || getFieldValue(item, 'owner');
            const status = getFieldValue(item, 'STATUS') || getFieldValue(item, 'status');
            
            return (
              <button
                key={item.id || index}
                onClick={() => onSelectObject(item, type)}
                className="w-full p-3 rounded-lg border text-left hover:bg-opacity-50 transition-colors"
                style={{ 
                  backgroundColor: colors.card,
                  borderColor: colors.border
                }}
              >
                <div className="flex items-center gap-3">
                  <div className="shrink-0">
                    {getObjectIcon(type)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-sm font-medium truncate" style={{ color: colors.text }}>
                        {name}
                      </span>
                      <span 
                        className="text-[10px] px-2 py-0.5 rounded-full"
                        style={{ 
                          backgroundColor: colors.objectType[type?.toLowerCase()] + '20',
                          color: colors.objectType[type?.toLowerCase()]
                        }}
                      >
                        {type}
                      </span>
                    </div>
                    <div className="flex items-center gap-3 text-xs" style={{ color: colors.textSecondary }}>
                      <span>{owner}</span>
                      {status && (
                        <span className={`px-2 py-0.5 rounded-full text-[10px] ${
                          status === 'VALID' ? 'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'
                        }`}>
                          {status}
                        </span>
                      )}
                    </div>
                  </div>
                  <ExternalLink size={14} style={{ color: colors.textTertiary }} className="shrink-0" />
                </div>
              </button>
            );
          })
        )}
      </div>
    </div>
  );
};

// Main Used By Tab Component - With logging
const UsedByTab = ({ 
  objectName, 
  objectType, 
  owner, 
  colors,
  onSelectObject,
  onRefresh,
  usedByData,
  usedByLoading,
  onPageChange,
  onPageSizeChange,
  getObjectIcon
}) => {
  console.log('📋 UsedByTab rendering with:', { 
    objectName, 
    objectType, 
    owner,
    hasSummary: !!usedByData.summary,
    summaryData: usedByData.summary,
    itemsCount: usedByData.items?.length
  });
  
  return (
    <div className="flex-1 flex flex-col h-full overflow-hidden">
      {/* Summary Section */}
      {usedByData.summary && (
        <UsedBySummary 
          data={usedByData.summary} 
          colors={colors}
          onRefresh={onRefresh}
        />
      )}

      {/* List Section */}
      <UsedByList
        items={usedByData.items || []}
        totalCount={usedByData.totalCount || 0}
        totalPages={usedByData.totalPages || 1}
        page={usedByData.page || 1}
        pageSize={usedByData.pageSize || 10}
        onPageChange={onPageChange}
        onPageSizeChange={onPageSizeChange}
        onSelectObject={onSelectObject}
        colors={colors}
        loading={usedByLoading}
        getObjectIcon={getObjectIcon}
      />
    </div>
  );
};



// Main SchemaBrowser Component
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
  const [loading, setLoading] = useState(true);
  const [initialLoadComplete, setInitialLoadComplete] = useState(false);
  const [error, setError] = useState(null);
  const [schemaInfo, setSchemaInfo] = useState(null);
  const [hasAutoSelected, setHasAutoSelected] = useState(false);
  const [initialized, setInitialized] = useState(false);
  const [isLoadingObjects, setIsLoadingObjects] = useState(false);
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const [ddlLoading, setDdlLoading] = useState(false);
  const [hasActiveFilter, setHasActiveFilter] = useState(false);
  // Add a new state to track if search has been performed
const [searchPerformed, setSearchPerformed] = useState(false);
  const [obType, setObType] = useState("");

  const searchAbortController = useRef(null);

  // Handle search button click or enter key
const handleSearch = useCallback((searchTerm) => {
  if (searchTerm && searchTerm.length >= 2) {
    // Cancel any ongoing search first
    if (isFiltering) {
      handleCancelSearch();
    }
    // Set search performed to true
    setSearchPerformed(true);
    // Start new search
    searchObjects(searchTerm, selectedOwner);
  }
}, [searchObjects, selectedOwner, isFiltering]);

// Handle cancel search
const handleCancelSearch = useCallback(() => {
  // Clear the abort controller for ongoing requests
  if (searchAbortController.current) {
    searchAbortController.current.abort();
    searchAbortController.current = null;
  }
  
  // Reset filtering state
  setIsFiltering(false);
  setFilteredResults({});
  setSearchPerformed(false); // Reset search performed state
  
  // Clear any ongoing search requests from the map
  const requestKeys = Array.from(ongoingRequests.keys());
  requestKeys.forEach(key => {
    if (key.startsWith('search_')) {
      ongoingRequests.delete(key);
    }
  });
  
  Logger.info('SchemaBrowser', 'handleCancelSearch', 'Search cancelled');
}, []);

// Update your usedByData state
const [usedByData, setUsedByData] = useState({
  items: [],
  totalCount: 0,
  totalPages: 1,
  page: 1,
  pageSize: 10,
  summary: null,
  loading: false,
  objectName: null // Track which object's data we have
});

  // Pagination state for each object type
  const [pagination, setPagination] = useState({
    procedures: { page: 1, totalPages: 1, totalCount: 0 },
    views: { page: 1, totalPages: 1, totalCount: 0 },
    functions: { page: 1, totalPages: 1, totalCount: 0 },
    tables: { page: 1, totalPages: 1, totalCount: 0 },
    packages: { page: 1, totalPages: 1, totalCount: 0 },
    sequences: { page: 1, totalPages: 1, totalCount: 0 },
    synonyms: { page: 1, totalPages: 1, totalCount: 0 },
    types: { page: 1, totalPages: 1, totalCount: 0 },
    triggers: { page: 1, totalPages: 1, totalCount: 0 }
  });

  // Filtered results state
  const [filteredResults, setFilteredResults] = useState({});
  const [isFiltering, setIsFiltering] = useState(false);
  const [filterSearchTerm, setFilterSearchTerm] = useState('');

  // Loaded sections state
  const [loadedSections, setLoadedSections] = useState({
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
  
  // Schema objects state with total counts
  const [schemaObjects, setSchemaObjects] = useState({
    tables: [],
    tablesTotalCount: 0,
    views: [],
    viewsTotalCount: 0,
    procedures: [],
    proceduresTotalCount: 0,
    functions: [],
    functionsTotalCount: 0,
    packages: [],
    packagesTotalCount: 0,
    sequences: [],
    sequencesTotalCount: 0,
    synonyms: [],
    synonymsTotalCount: 0,
    types: [],
    typesTotalCount: 0,
    triggers: [],
    triggersTotalCount: 0
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
    procedures: true,
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
  const [activeTab, setActiveTab] = useState('properties');
  const [tabs, setTabs] = useState([]);
  const [tableData, setTableData] = useState(null);
  const [objectDDL, setObjectDDL] = useState('');
  const [objectDetails, setObjectDetails] = useState(null);
  const [tableDataLoading, setTableDataLoading] = useState(false);
  
  // Context menu
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuPosition, setContextMenuPosition] = useState({ x: 0, y: 0 });
  const [contextObject, setContextObject] = useState(null);

  const [isLoadingSchemaObjects, setIsLoadingSchemaObjects] = useState(true);
  
  // Data view state
  const [dataView, setDataView] = useState({
    page: 1,
    pageSize: 15,
    sortColumn: '',
    sortDirection: 'ASC'
  });

  useEffect(() => {
  // hasActiveFilter is now controlled by searchPerformed state
  setHasActiveFilter(searchPerformed);
}, [searchPerformed]);

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

  // Helper function to extract items from paginated response
  const extractItemsFromResponse = (response) => {
    if (!response) return { items: [], totalPages: 1, totalCount: 0, page: 1 };
    
    Logger.debug('SchemaBrowser', 'extractItemsFromResponse', 'Response structure:', {
      hasData: !!response.data,
      dataType: response.data ? typeof response.data : 'undefined',
      dataIsArray: response.data ? Array.isArray(response.data) : false,
      totalCount: response.totalCount,
      responseKeys: Object.keys(response)
    });
    
    let items = [];
    let totalCount = response.totalCount || 0;
    let totalPages = response.pagination?.totalPages || 1;
    let page = response.pagination?.page || 1;
    let pageSize = response.pagination?.pageSize || 10;
    
    if (response.data && response.data.items && Array.isArray(response.data.items)) {
      items = response.data.items;
      totalCount = response.data.totalCount || response.totalCount || items.length;
      totalPages = response.data.totalPages || response.pagination?.totalPages || 1;
      page = response.data.page || response.pagination?.page || 1;
    }
    else if (response.data && Array.isArray(response.data)) {
      items = response.data;
      totalCount = response.totalCount || items.length;
      totalPages = response.pagination?.totalPages || 
                  Math.ceil((response.totalCount || items.length) / (response.pagination?.pageSize || 50)) || 1;
      page = response.pagination?.page || 1;
    }
    else if (response.data && response.data.paginatedItems && Array.isArray(response.data.paginatedItems)) {
      items = response.data.paginatedItems;
      totalCount = response.data.totalCount || response.totalCount || items.length;
      totalPages = response.data.totalPages || response.pagination?.totalPages || 1;
      page = response.data.page || response.pagination?.page || 1;
    }
    else if (response.items && Array.isArray(response.items)) {
      items = response.items;
      totalCount = response.totalCount || items.length;
      totalPages = response.totalPages || 1;
      page = response.page || 1;
    }
    else if (Array.isArray(response)) {
      items = response;
      totalCount = items.length;
      totalPages = 1;
      page = 1;
    }
    
    return {
      items,
      totalCount,
      totalPages,
      page,
      pageSize
    };
  };

  // Load schema info
  const loadSchemaInfo = useCallback(async () => {
    if (!authToken) {
        setError('Authentication required');
        return;
    }

    Logger.info('SchemaBrowser', 'loadSchemaInfo', 'Loading schema info');
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
    }
  }, [authToken]);

  // Load initial data
  const loadInitialData = useCallback(async () => {
    if (!authToken) return;
    
    Logger.info('SchemaBrowser', 'loadInitialData', 'Loading procedures first');
    
    let updatedOwners = new Set(owners);
    
    try {
      const cacheKey = `procedures_${authToken.substring(0, 10)}_page1`;
      const cached = objectCache.get(cacheKey);
      
      if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
        Logger.debug('SchemaBrowser', 'loadInitialData', 'Loaded procedures from cache');
        setSchemaObjects(prev => ({ 
          ...prev, 
          procedures: cached.data.items,
          proceduresTotalCount: cached.data.totalCount 
        }));
        setPagination(prev => ({
          ...prev,
          procedures: {
            page: cached.data.page,
            totalPages: cached.data.totalPages,
            totalCount: cached.data.totalCount
          }
        }));
        setLoadedSections(prev => ({ ...prev, procedures: true }));
        
        cached.data.items.forEach(obj => {
          if (obj.owner) updatedOwners.add(obj.owner);
        });
      } else {
        setLoadingStates(prev => ({ ...prev, procedures: true }));
        
        const response = await getAllProceduresForFrontendPaginated(authToken, { page: 1, pageSize: 10 });
        const { items, totalCount, totalPages } = extractItemsFromResponse(response);
        
        setSchemaObjects(prev => ({ 
          ...prev, 
          procedures: items,
          proceduresTotalCount: totalCount 
        }));
        setPagination(prev => ({
          ...prev,
          procedures: { page: 1, totalPages, totalCount }
        }));
        setLoadedSections(prev => ({ ...prev, procedures: true }));
        
        objectCache.set(cacheKey, { 
          data: { items, totalCount, totalPages, page: 1 }, 
          timestamp: Date.now() 
        });
        
        items.forEach(obj => {
          if (obj.owner) updatedOwners.add(obj.owner);
        });
        
        setLoadingStates(prev => ({ ...prev, procedures: false }));
      }
      
      if (!schemaInfo) {
        await loadSchemaInfo();
      }
      
      setOwners(Array.from(updatedOwners).sort());
      
      setIsInitialLoad(false);
      setLoading(false);
      setInitialLoadComplete(true);
      
      Logger.info('SchemaBrowser', 'loadInitialData', 'Initial data loaded');
      
    } catch (err) {
      Logger.error('SchemaBrowser', 'loadInitialData', 'Error loading initial data', err);
      setError(`Failed to load initial data: ${err.message}`);
      setIsInitialLoad(false);
      setLoading(false);
    }
  }, [authToken, owners, loadSchemaInfo, schemaInfo]);

  // Load remaining data
  const loadRemainingData = useCallback(async () => {
    if (!authToken) return;
    
    Logger.info('SchemaBrowser', 'loadRemainingData', 'Loading remaining data in background');
    
    const remainingTypes = [
      { type: 'synonyms', fetcher: getAllSynonymsForFrontendPaginated },
      { type: 'views', fetcher: getAllViewsForFrontendPaginated },
      { type: 'functions', fetcher: getAllFunctionsForFrontendPaginated },
      { type: 'tables', fetcher: getAllTablesForFrontendPaginated },
      { type: 'packages', fetcher: getAllPackagesForFrontendPaginated },
      { type: 'sequences', fetcher: getAllSequencesForFrontendPaginated },
      { type: 'types', fetcher: getAllTypesForFrontendPaginated },
      { type: 'triggers', fetcher: getAllTriggersForFrontendPaginated }
    ];
    
    const loadPromises = remainingTypes.map(async ({ type, fetcher }) => {
      await new Promise(resolve => setTimeout(resolve, 100));
      
      try {
        const cacheKey = `${type}_${authToken.substring(0, 10)}_page1`;
        const cached = objectCache.get(cacheKey);
        
        if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
          setSchemaObjects(prev => ({ 
            ...prev, 
            [type]: cached.data.items,
            [`${type}TotalCount`]: cached.data.totalCount
          }));
          setPagination(prev => ({
            ...prev,
            [type]: {
              page: cached.data.page,
              totalPages: cached.data.totalPages,
              totalCount: cached.data.totalCount
            }
          }));
          setLoadedSections(prev => ({ ...prev, [type]: true }));
          return;
        }
        
        setLoadingStates(prev => ({ ...prev, [type]: true }));
        
        const response = await fetcher(authToken, { page: 1, pageSize: 10 });
        const { items, totalCount, totalPages } = extractItemsFromResponse(response);
        
        setSchemaObjects(prev => ({ 
          ...prev, 
          [type]: items,
          [`${type}TotalCount`]: totalCount 
        }));
        setPagination(prev => ({
          ...prev,
          [type]: { page: 1, totalPages, totalCount }
        }));
        setLoadedSections(prev => ({ ...prev, [type]: true }));
        
        objectCache.set(cacheKey, { 
          data: { items, totalCount, totalPages, page: 1 }, 
          timestamp: Date.now() 
        });
        
        setOwners(prev => {
          const newOwners = new Set(prev);
          items.forEach(obj => {
            if (obj.owner) newOwners.add(obj.owner);
          });
          return Array.from(newOwners).sort();
        });
        
        setLoadingStates(prev => ({ ...prev, [type]: false }));
        
      } catch (err) {
        Logger.error('SchemaBrowser', 'loadRemainingData', `Error loading ${type}`, err);
        setLoadingStates(prev => ({ ...prev, [type]: false }));
      }
    });
    
    setIsLoadingSchemaObjects(true);
    
    await Promise.all(loadPromises);
    
    setIsLoadingSchemaObjects(false);
    
    Logger.info('SchemaBrowser', 'loadRemainingData', 'All remaining data loaded');
    
  }, [authToken]);

  // Initialize component
  useEffect(() => {
    let isMounted = true;
    
    const initialize = async () => {
      if (!authToken || initialized) return;
      
      Logger.info('SchemaBrowser', 'initialize', 'Starting initialization');
      
      await loadInitialData();
      
      if (isMounted) {
        setInitialized(true);
        
        loadRemainingData().catch(err => {
          Logger.error('SchemaBrowser', 'initialize', 'Error loading remaining data', err);
        });
      }
    };
    
    initialize();
    
    return () => {
      isMounted = false;
    };
  }, [authToken, initialized, loadInitialData, loadRemainingData]);

  // Load more objects
  const loadObjectType = useCallback(async (type, page = 1, pageSize = 10) => {
    if (!authToken) return;
    
    const requestKey = `${type}_page_${page}`;
    if (loadingStates[type] || ongoingRequests.has(requestKey)) {
      Logger.debug('SchemaBrowser', 'loadObjectType', `Already loading ${type} page ${page}, skipping`);
      return;
    }
    
    Logger.info('SchemaBrowser', 'loadObjectType', `Loading ${type} page ${page} from API`);
    
    setLoadingStates(prev => ({ ...prev, [type]: true }));
    ongoingRequests.set(requestKey, true);
    
    try {
      let response;
      switch(type) {
        case 'procedures':
          response = await getAllProceduresForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'views':
          response = await getAllViewsForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'functions':
          response = await getAllFunctionsForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'tables':
          response = await getAllTablesForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'packages':
          response = await getAllPackagesForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'sequences':
          response = await getAllSequencesForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'synonyms':
          response = await getAllSynonymsForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'types':
          response = await getAllTypesForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'triggers':
          response = await getAllTriggersForFrontendPaginated(authToken, { page, pageSize });
          break;
        default:
          return;
      }
      
      const { items, totalCount, page: currentPage, totalPages } = extractItemsFromResponse(response);
      
      Logger.info('SchemaBrowser', 'loadObjectType', `Loaded ${items.length} ${type} (page ${page})`);
      
      setSchemaObjects(prev => {
        if (page === 1) {
          return { 
            ...prev, 
            [type]: items,
            [`${type}TotalCount`]: totalCount 
          };
        } else {
          const existing = prev[type] || [];
          const newItems = items.filter(newItem => 
            !existing.some(existingItem => 
              existingItem.name === newItem.name && existingItem.owner === newItem.owner
            )
          );
          return { 
            ...prev, 
            [type]: [...existing, ...newItems],
            [`${type}TotalCount`]: totalCount
          };
        }
      });
      
      setPagination(prev => ({
        ...prev,
        [type]: {
          page: currentPage,
          totalPages: totalPages,
          totalCount: totalCount
        }
      }));
      
      const cacheKey = `${type}_page${page}_${authToken.substring(0, 10)}`;
      objectCache.set(cacheKey, { 
        data: items, 
        totalCount,
        page: currentPage,
        totalPages,
        timestamp: Date.now() 
      });
      
      setLoadedSections(prev => ({ ...prev, [type]: true }));
      
      setOwners(prev => {
        const newOwners = new Set(prev);
        items.forEach(obj => {
          if (obj.owner) newOwners.add(obj.owner);
        });
        return Array.from(newOwners).sort();
      });
      
    } catch (err) {
      Logger.error('SchemaBrowser', 'loadObjectType', `Error loading ${type}`, err);
    } finally {
      setLoadingStates(prev => ({ ...prev, [type]: false }));
      ongoingRequests.delete(requestKey);
    }
  }, [authToken, loadingStates]);

  // Handle load section
  const handleLoadSection = useCallback((type) => {
    if (!hasActiveFilter) {
      loadObjectType(type, 1);
    }
  }, [loadObjectType, hasActiveFilter]);

  // Handle load more
  const handleLoadMore = useCallback((type) => {
    const nextPage = (pagination[type]?.page || 1) + 1;
    if (nextPage <= (pagination[type]?.totalPages || 1) && !hasActiveFilter) {
      loadObjectType(type, nextPage);
    }
  }, [pagination, loadObjectType, hasActiveFilter]);

  // Handle toggle section
  const handleToggleSection = useCallback((type) => {
    setObjectTree(prev => ({ ...prev, [type]: !prev[type] }));
  }, []);

  // Load table data
  const loadTableData = useCallback(async (tableName) => {
    if (!authToken || !tableName) return;
    
    setTableDataLoading(true);
    setTableData(null);
    
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
      const tableDataResult = extractTableData({ data: processed });
      
      setTableData(tableDataResult);
      
    } catch (err) {
      Logger.error('SchemaBrowser', 'loadTableData', `Error loading data for ${tableName}`, err);
      setError(`Failed to load table data: ${err.message}`);
    } finally {
      setTableDataLoading(false);
    }
  }, [authToken, dataView]);

  // Load view data
  const loadViewData = useCallback(async (viewName) => {
    if (!authToken || !viewName) return;
    
    setTableDataLoading(true);
    setTableData(null);
    
    try {
      const response = await getTableData(authToken, { 
        tableName: viewName,
        page: dataView.page - 1,
        pageSize: dataView.pageSize,
        sortColumn: dataView.sortColumn || undefined,
        sortDirection: dataView.sortDirection
      });
      
      const processed = handleSchemaBrowserResponse(response);
      const viewDataResult = extractTableData({ data: processed });
      
      setTableData(viewDataResult);
      
    } catch (err) {
      Logger.error('SchemaBrowser', 'loadViewData', `Error loading data for view ${viewName}`, err);
      setError(`Failed to load view data: ${err.message}`);
    } finally {
      setTableDataLoading(false);
    }
  }, [authToken, dataView]);

 // Update your loadUsedByData function with better logging
const loadUsedByData = useCallback(async (objectName, objectType, owner, page = 1, pageSize = 10) => {
  if (!authToken || !objectName || !objectType) return;
  
  console.log('🔄 loadUsedByData START:', { objectName, objectType, owner, page, pageSize });
  setUsedByData(prev => ({ ...prev, loading: true }));
  
  try {
    // Load paginated used by data
    console.log('📡 Fetching used by paginated data...');
    const response = await getUsedByPaginated(authToken, {
      objectType,
      objectName,
      owner,
      page,
      pageSize
    });
    
    console.log('📥 Used By Paginated Response:', JSON.stringify(response, null, 2));
    
    // Extract data from response structure
    const responseData = response?.data || {};
    const items = responseData.items || [];
    const totalCount = responseData.totalCount || items.length || 0;
    const totalPages = Math.ceil(totalCount / pageSize) || 1;
    
    console.log('📊 Paginated data extracted:', { itemsCount: items.length, totalCount, totalPages });
    
    setUsedByData(prev => ({
      ...prev,
      items,
      totalCount,
      totalPages,
      page,
      pageSize,
      objectName,
      loading: false
    }));
    
    // Also load summary separately
    try {
      console.log('📡 Fetching used by summary data...');
      const summaryResponse = await getUsedBySummary(authToken, {
        objectType,
        objectName,
        owner
      });
      
      console.log('📥 Used By Summary Response:', JSON.stringify(summaryResponse, null, 2));
      
      // The summary data structure from your example
      const summaryData = summaryResponse?.data;
      
      console.log('📊 Summary data extracted:', summaryData);
      
      setUsedByData(prev => ({ 
        ...prev, 
        summary: summaryData
      }));
    } catch (err) {
      console.error('❌ Error loading used by summary:', err);
      Logger.error('SchemaBrowser', 'loadUsedByData', 'Error loading used by summary', err);
    }
    
  } catch (err) {
    console.error('❌ Error loading used by data:', err);
    Logger.error('SchemaBrowser', 'loadUsedByData', 'Error loading used by data', err);
    setUsedByData(prev => ({ ...prev, loading: false }));
  }
}, [authToken]);

// NEW: Handle Used By Page Change
const handleUsedByPageChange = useCallback((newPage) => {
  if (!activeObject) return;
  
  const objectType = activeObject.type?.toUpperCase();
  const owner = activeObject.owner || objectDetails?.owner;
  
  loadUsedByData(
    activeObject.name,
    objectType,
    owner,
    newPage,
    usedByData.pageSize
  );
}, [activeObject, objectDetails, usedByData.pageSize, loadUsedByData]);

// NEW: Handle Used By Page Size Change
const handleUsedByPageSizeChange = useCallback((newSize) => {
  if (!activeObject) return;
  
  const objectType = activeObject.type?.toUpperCase();
  const owner = activeObject.owner || objectDetails?.owner;
  
  setUsedByData(prev => ({ ...prev, pageSize: newSize, page: 1 }));
  
  loadUsedByData(
    activeObject.name,
    objectType,
    owner,
    1,
    newSize
  );
}, [activeObject, objectDetails, loadUsedByData]);

  // NEW: Load Used By Summary
  const loadUsedBySummary = useCallback(async (objectName, objectType, owner) => {
    if (!authToken || !objectName || !objectType) return;
    
    try {
      const response = await getUsedBySummary(authToken, {
        objectType,
        objectName,
        owner
      });
      
      const summary = extractUsedBySummary(response);
      
      setUsedByData(prev => ({
        ...prev,
        summary
      }));
      
    } catch (err) {
      Logger.error('SchemaBrowser', 'loadUsedBySummary', 'Error loading used by summary', err);
    }
  }, [authToken]);


  // Handle context menu
  const handleContextMenu = useCallback((e, object, type) => {
    e.preventDefault();
    e.stopPropagation();
    
    setContextObject({ ...object, type });
    setContextMenuPosition({ x: e.clientX, y: e.clientY });
    setShowContextMenu(true);
  }, []);

  // Search objects
  const searchObjects = useCallback(async (searchTerm, owner) => {
    if (!authToken || !searchTerm || searchTerm.length < 2) {
      setFilteredResults({});
      setSearchPerformed(false);
      return;
    }

    const requestKey = `search_${searchTerm}_${owner}`;
    
    // Cancel any previous search
    if (searchAbortController.current) {
      searchAbortController.current.abort();
    }
    
    // Create new abort controller
    searchAbortController.current = new AbortController();
    
    if (ongoingRequests.has(requestKey)) {
      Logger.debug('SchemaBrowser', 'searchObjects', `Already searching for "${searchTerm}", skipping`);
      return;
    }

    Logger.info('SchemaBrowser', 'searchObjects', `Searching for "${searchTerm}" in ${owner === 'ALL' ? 'all schemas' : owner}`);

    setIsFiltering(true);
    ongoingRequests.set(requestKey, true);

    try {
      const params = {
        query: searchTerm,
        page: 1,
        pageSize: 100
      };

      if (owner !== 'ALL') {
        params.owner = owner;
      }

      // Pass the abort signal to the API call
      const response = await searchObjectsPaginated(authToken, params, {
        signal: searchAbortController.current.signal
      });
      
      // Check if this was aborted
      if (searchAbortController.current.signal.aborted) {
        throw new DOMException('Aborted', 'AbortError');
      }
      
      const groupedResults = {
        procedures: [],
        views: [],
        functions: [],
        tables: [],
        packages: [],
        sequences: [],
        synonyms: [],
        types: [],
        triggers: []
      };

      let resultsArray = [];
      
      if (response?.data?.items && Array.isArray(response.data.items)) {
        resultsArray = response.data.items;
      } else if (response?.data?.results && Array.isArray(response.data.results)) {
        resultsArray = response.data.results;
      } else if (response?.results && Array.isArray(response.results)) {
        resultsArray = response.results;
      } else if (Array.isArray(response)) {
        resultsArray = response;
      } else if (response?.data && Array.isArray(response.data)) {
        resultsArray = response.data;
      }

      resultsArray.forEach(item => {
        const objectType = (
          item.object_type || 
          item.type || 
          item.OBJECT_TYPE || 
          ''
        ).toUpperCase();
        
        const itemName = 
          item.name ||
          item.synonym_name ||
          item.table_name ||
          item.procedure_name ||
          item.view_name ||
          item.function_name ||
          item.package_name ||
          item.sequence_name ||
          item.type_name ||
          item.trigger_name ||
          item.au_g_ledger_name ||
          item.curr_g_ledger_name ||
          item.account_details_keynext_name;
        
        if (!itemName || !objectType) return;
        
        const normalizedObj = {
          name: itemName,
          owner: item.owner || item.OWNER,
          type: objectType,
          objectType: objectType,
          status: item.status || item.STATUS || item.targetStatus || 'VALID',
          id: item.id || `${item.owner || 'unknown'}_${itemName}`,
          targetName: item.targetName || item.TARGET_NAME,
          targetOwner: item.targetOwner || item.TARGET_OWNER,
          targetType: item.targetType || item.TARGET_TYPE,
          isSynonym: item.isSynonym === true || objectType === 'SYNONYM',
          icon: item.icon,
          created: item.created || item.CREATED,
          lastModified: item.lastModified || item.last_ddl_time || item.LAST_DDL_TIME
        };
        
        if (objectType.includes('PROCEDURE')) {
          groupedResults.procedures.push(normalizedObj);
        } else if (objectType.includes('VIEW')) {
          groupedResults.views.push(normalizedObj);
        } else if (objectType.includes('FUNCTION')) {
          groupedResults.functions.push(normalizedObj);
        } else if (objectType.includes('TABLE')) {
          groupedResults.tables.push(normalizedObj);
        } else if (objectType.includes('PACKAGE')) {
          groupedResults.packages.push(normalizedObj);
        } else if (objectType.includes('SEQUENCE')) {
          groupedResults.sequences.push(normalizedObj);
        } else if (objectType.includes('SYNONYM')) {
          groupedResults.synonyms.push(normalizedObj);
        } else if (objectType.includes('TYPE')) {
          groupedResults.types.push(normalizedObj);
        } else if (objectType.includes('TRIGGER')) {
          groupedResults.triggers.push(normalizedObj);
        }
      });
      
      // Check again if aborted before setting state
      if (searchAbortController.current?.signal.aborted) {
        return;
      }
      
      // Set search results and mark search as performed
      setFilteredResults(groupedResults);
      setSearchPerformed(true);
      
      setObjectTree(prev => ({
        ...prev,
        procedures: groupedResults.procedures.length > 0,
        views: groupedResults.views.length > 0,
        functions: groupedResults.functions.length > 0,
        tables: groupedResults.tables.length > 0,
        packages: groupedResults.packages.length > 0,
        sequences: groupedResults.sequences.length > 0,
        synonyms: groupedResults.synonyms.length > 0,
        types: groupedResults.types.length > 0,
        triggers: groupedResults.triggers.length > 0
      }));

      const totalResults = Object.values(groupedResults).reduce((acc, curr) => acc + curr.length, 0);
      Logger.info('SchemaBrowser', 'searchObjects', `Found ${totalResults} results`);

    } catch (err) {
      // Don't log aborted errors as errors
      if (err.name === 'AbortError' || err.message === 'Aborted') {
        Logger.info('SchemaBrowser', 'searchObjects', 'Search was cancelled');
        setSearchPerformed(false);
        return;
      }
      Logger.error('SchemaBrowser', 'searchObjects', 'Error searching objects', err);
      setFilteredResults({});
      setSearchPerformed(false);
    } finally {
      setIsFiltering(false);
      ongoingRequests.delete(requestKey);
      searchAbortController.current = null;
    }
  }, [authToken]);

  // Handle filter changes
  const handleFilterChange = useCallback((value) => {
    setFilterQuery(value);
    setFilterSearchTerm(value);
    // Remove any automatic search logic from here
  }, []);

  // Handle owner change
  const handleOwnerChange = useCallback((value) => {
    setSelectedOwner(value);
    // Remove the automatic search call
  }, []);

  // Handle clear filters
  const handleClearFilters = useCallback(() => {
  setFilterQuery('');
  setSelectedOwner('ALL');
  setFilteredResults({});
  setFilterSearchTerm('');
  setSearchPerformed(false); // Reset search performed state
  
  setObjectTree({
    procedures: true,
    views: false,
    functions: false,
    tables: false,
    packages: false,
    sequences: false,
    synonyms: false,
    types: false,
    triggers: false
  });
}, []);

  // Handle refresh
  const handleRefresh = useCallback(async () => {
    objectCache.clear();
    ongoingRequests.clear();
    setLoading(true);
    setInitialLoadComplete(false);
    setInitialized(false);
    setHasAutoSelected(false);
    setIsLoadingObjects(false);
    setIsInitialLoad(true);
    
    setLoadedSections({
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
    setSchemaObjects({
      tables: [],
      tablesTotalCount: 0,
      views: [],
      viewsTotalCount: 0,
      procedures: [],
      proceduresTotalCount: 0,
      functions: [],
      functionsTotalCount: 0,
      packages: [],
      packagesTotalCount: 0,
      sequences: [],
      sequencesTotalCount: 0,
      synonyms: [],
      synonymsTotalCount: 0,
      types: [],
      typesTotalCount: 0,
      triggers: [],
      triggersTotalCount: 0
    });
    setPagination({
      procedures: { page: 1, totalPages: 1, totalCount: 0 },
      views: { page: 1, totalPages: 1, totalCount: 0 },
      functions: { page: 1, totalPages: 1, totalCount: 0 },
      tables: { page: 1, totalPages: 1, totalCount: 0 },
      packages: { page: 1, totalPages: 1, totalCount: 0 },
      sequences: { page: 1, totalPages: 1, totalCount: 0 },
      synonyms: { page: 1, totalPages: 1, totalCount: 0 },
      types: { page: 1, totalPages: 1, totalCount: 0 },
      triggers: { page: 1, totalPages: 1, totalCount: 0 }
    });
    
    await loadInitialData();
    
    loadRemainingData().catch(err => {
      Logger.error('SchemaBrowser', 'handleRefresh', 'Error loading remaining data', err);
    });
    
  }, [loadInitialData, loadRemainingData]);

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

  // Auto-select first object
  useEffect(() => {
    if (!initialLoadComplete || hasAutoSelected || activeObject) return;
    
    if (schemaObjects.procedures && schemaObjects.procedures.length > 0) {
      const firstProcedure = schemaObjects.procedures[0];
      
      const procedureWithId = {
        ...firstProcedure,
        id: firstProcedure.id || `${firstProcedure.owner || 'unknown'}_${firstProcedure.name}`
      };
      
      setHasAutoSelected(true);
      handleObjectSelect(procedureWithId, 'PROCEDURE');
      return;
    }
    
    if (schemaObjects.synonyms && schemaObjects.synonyms.length > 0) {
      const firstSynonym = schemaObjects.synonyms[0];
      
      const synonymWithId = {
        ...firstSynonym,
        id: firstSynonym.id || `${firstSynonym.owner || 'unknown'}_${firstSynonym.name}`
      };
      
      setHasAutoSelected(true);
      handleObjectSelect(synonymWithId, 'SYNONYM');
      return;
    }
    
  }, [activeObject, hasAutoSelected, initialLoadComplete, schemaObjects.procedures, schemaObjects.synonyms, handleObjectSelect]);

  // Update table data when dataView changes
  useEffect(() => {
    if (activeObject) {
      const objectType = activeObject.type?.toUpperCase();
      const effectiveType = objectDetails?.targetDetails?.OBJECT_TYPE || objectType;
      
      if (objectType === 'TABLE' || effectiveType === 'TABLE' || 
          objectType === 'VIEW' || effectiveType === 'VIEW') {
        
        let objectName;
        if (objectType === 'SYNONYM' && objectDetails?.TARGET_NAME) {
          objectName = objectDetails.TARGET_NAME;
        } else {
          objectName = activeObject.name;
        }
        
        if (objectType === 'TABLE' || effectiveType === 'TABLE') {
          loadTableData(objectName);
        } else if (objectType === 'VIEW' || effectiveType === 'VIEW') {
          loadViewData(objectName);
        }
      }
    }
  }, [dataView.page, dataView.pageSize, dataView.sortColumn, dataView.sortDirection, activeObject, objectDetails, loadTableData, loadViewData]);

  // Close context menu on outside click
  useEffect(() => {
    const handleClickOutside = () => setShowContextMenu(false);
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, []);

  // Get Tabs for Object Type - UPDATED to include Used By
  const getTabsForObject = useCallback((type, objectDetails) => {
    const objectType = type?.toUpperCase();
    
    if (objectType === 'SYNONYM' && objectDetails?.targetDetails) {
      const targetType = objectDetails.targetDetails.OBJECT_TYPE || objectDetails.targetDetails.objectType;
      
      switch(targetType) {
        case 'TABLE':
          return ['Columns', 'Data', 'Constraints', 'DDL', 'Properties', 'Used By'];
        case 'VIEW':
          return ['Definition', 'Columns', 'Data', 'Properties', 'Used By'];
        case 'PROCEDURE':
          return ['Parameters', 'DDL', 'Properties', 'Used By'];
        case 'FUNCTION':
          return ['Parameters', 'DDL', 'Properties', 'Used By'];
        case 'PACKAGE':
          return ['Spec', 'Body', 'Properties', 'Used By'];
        case 'SEQUENCE':
          return ['DDL', 'Properties', 'Used By'];
        case 'TYPE':
          return ['Attributes', 'Properties', 'Used By'];
        case 'TRIGGER':
          return ['Definition', 'Properties', 'Used By'];
        default:
          return ['Properties', 'Used By'];
      }
    }
    
    switch(objectType) {
      case 'TABLE':
        return ['Columns', 'Data', 'Constraints', 'DDL', 'Properties', 'Used By'];
      case 'VIEW':
        return ['Definition', 'Columns', 'Data', 'Properties', 'Used By'];
      case 'PROCEDURE':
        return ['Parameters', 'DDL', 'Properties', 'Used By'];
      case 'FUNCTION':
        return ['Parameters', 'DDL', 'Properties', 'Used By'];
      case 'PACKAGE':
        return ['Spec', 'Body', 'Properties', 'Used By'];
      case 'SEQUENCE':
        return ['DDL', 'Properties', 'Used By'];
      case 'SYNONYM':
        return ['Properties', 'Used By'];
      case 'TYPE':
        return ['Attributes', 'Properties', 'Used By'];
      case 'TRIGGER':
        return ['Definition', 'Properties', 'Used By'];
      default:
        return ['Properties', 'Used By'];
    }
  }, []);



const handleObjectSelect = useCallback(async (object, type) => {
  if (!authToken || !object) {
    console.error('Cannot select object: missing authToken or object', { authToken: !!authToken, object });
    return;
  }

  Logger.info('SchemaBrowser', 'handleObjectSelect', `Selecting ${object.name} (${type})`);
  
  const objectId = object.id || `${object.owner || 'unknown'}_${object.name}`;
  
  setLoading(true);
  setTableDataLoading(false);
  setObjectDetails(null);
  setObjectDDL('');
  setTableData(null);
  setDdlLoading(false);
  
  setActiveTab('properties');
  
  const enrichedObject = {
    ...object,
    id: objectId,
    type: type
  };
  
  setActiveObject(enrichedObject);
  
  // Set the object for API generation with basic info first
  setSelectedForApiGeneration(enrichedObject);
  
  const tabId = `${type}_${object.owner || 'unknown'}_${object.name}`;
  const existingTab = tabs.find(t => t.id === tabId);
  
  if (existingTab) {
    setTabs(tabs.map(t => ({ ...t, isActive: t.id === tabId })));
  } else {
    setTabs(prev => [...prev.slice(-4), {
      id: tabId,
      name: object.name,
      type,
      objectId: objectId,
      owner: object.owner,
      isActive: true
    }].map(t => ({ ...t, isActive: t.id === tabId })));
  }

  try {
    Logger.debug('SchemaBrowser', 'handleObjectSelect', `Loading details for ${object.name}`);
    
    const isSynonym = object.objectType === 'SYNONYM' || 
                      object.type === 'SYNONYM' ||
                      object.isSynonym === true ||
                      object.synonym_name === object.name;

    let apiObjectType;
    let apiObjectName = object.name;
    
    if (isSynonym) {
      apiObjectType = 'SYNONYM';
      Logger.debug('SchemaBrowser', 'handleObjectSelect', `Object is a synonym, using type: SYNONYM`);
    } else {
      apiObjectType = type;
    }

    setObType(apiObjectType);
    
    // First API call - Get synonym/object details
    const response = await getObjectDetails(authToken, { 
      objectType: apiObjectType, 
      objectName: apiObjectName 
    });
    
    const processedResponse = handleSchemaBrowserResponse(response);
    const responseData = processedResponse.data || processedResponse;
    
    // Get target info from synonym response
    const targetType = responseData.TARGET_TYPE || type || 'PROCEDURE';
    const targetName = responseData.TARGET_NAME || object.name;
    const targetOwner = responseData.TARGET_OWNER;
    
    let effectiveType = targetType;
    let parameters = [];
    let columns = [];
    let sourceCode = '';
    
    // PRESERVE YOUR WORKING PROCEDURE LOGIC EXACTLY AS IS
    if (targetType === 'PROCEDURE' || targetType === 'FUNCTION' || targetType === 'PACKAGE') {
      try {
        console.log(`📡 Fetching ${targetType} details for ${targetName}...`);
        const procResponse = await getObjectDetails(authToken, {
          objectType: targetType,
          objectName: targetName,
          owner: targetOwner
        });
        
        const procData = handleSchemaBrowserResponse(procResponse);
        const procDetails = procData.data || procData;
        
        // Extract parameters from procedure details
        if (procDetails.parameters && Array.isArray(procDetails.parameters)) {
          parameters = procDetails.parameters;
          console.log(`📦 Extracted ${parameters.length} parameters from procedure details`);
        } else if (procDetails.arguments && Array.isArray(procDetails.arguments)) {
          parameters = procDetails.arguments;
          console.log(`📦 Extracted ${parameters.length} arguments from procedure details`);
        }
        
        // Extract source code
        if (procDetails.source) {
          sourceCode = procDetails.source;
          console.log('📝 Found source code in procedure details');
        } else if (procDetails.text) {
          sourceCode = procDetails.text;
        }
        
        // Merge procedure details into responseData
        responseData.targetObjectDetails = procDetails;
      } catch (procErr) {
        console.error('Error fetching procedure details:', procErr);
      }
    } 
    // ADD TABLE HANDLING - BUT DON'T MAKE EXTRA API CALLS, USE EXISTING DATA
    else if (targetType === 'TABLE') {
      // Table data is already in responseData.targetObjectDetails from the first call
      if (responseData.targetObjectDetails) {
        const tableDetails = responseData.targetObjectDetails;
        
        // Extract columns from table details
        if (tableDetails.columns && Array.isArray(tableDetails.columns)) {
          columns = tableDetails.columns;
          console.log(`📦 Extracted ${columns.length} columns from table details`);
        }
        
        // Extract source/DDL if available
        if (tableDetails.source) {
          sourceCode = tableDetails.source;
          console.log('📝 Found source code in table details');
        }
      }
    }
    // ADD VIEW HANDLING - Similar to TABLE handling
    else if (targetType === 'VIEW') {
      // View data is in responseData.targetObjectDetails from the first call
      if (responseData.targetObjectDetails) {
        const viewDetails = responseData.targetObjectDetails;
        
        // Extract columns from view details
        if (viewDetails.columns && Array.isArray(viewDetails.columns)) {
          columns = viewDetails.columns;
          console.log(`📦 Extracted ${columns.length} columns from view details`);
        }
        
        // Extract source/DDL if available
        if (viewDetails.source) {
          sourceCode = viewDetails.source;
          console.log('📝 Found source code in view details');
        } else if (viewDetails.TEXT) {
          sourceCode = viewDetails.TEXT;
          console.log('📝 Found TEXT in view details');
        }
        
        // Merge view details into responseData if not already there
        if (!responseData.targetObjectDetails) {
          responseData.targetObjectDetails = viewDetails;
        }
      }
    }
    
    const enrichedResponseData = {
      ...responseData,
      name: responseData.name || object.name,
      type: responseData.type || type,
      isSynonym: isSynonym,
      parameters: parameters, // Store parameters at top level
      columns: columns,       // Store columns at top level
      source: sourceCode      // Store source/DDL at top level
    };
    
    setObjectDetails(enrichedResponseData);
    
    // Set DDL from source code if available
    if (sourceCode) {
      setObjectDDL(sourceCode);
      console.log('✅ Set object DDL from source code');
    } else if (responseData.TEXT) {
      setObjectDDL(responseData.TEXT);
      console.log('✅ Set object DDL from TEXT');
    }
    
    // CRITICAL: Update selectedForApiGeneration with full details including parameters and columns
    const apiObject = {
      ...object,
      ...responseData,
      name: object.name,
      type: effectiveType,
      owner: object.owner,
      parameters: parameters,
      columns: columns,
      isSynonym: isSynonym,
      targetDetails: responseData.targetDetails || responseData.targetObjectDetails,
      source: sourceCode || responseData.TEXT
    };
    
    console.log(`🎯 Setting selectedForApiGeneration with ${parameters.length} parameters and ${columns.length} columns`);
    setSelectedForApiGeneration(apiObject);
    
    // Load used by data in background
    loadUsedByData(
      targetName || object.name, 
      effectiveType, 
      targetOwner || object.owner, 
      1, 
      10
    ).catch(err => console.error('Background used by load error:', err));
    
  } catch (err) {
    Logger.error('SchemaBrowser', 'handleObjectSelect', `Error loading details for ${object.name}`, err);
    setError(`Failed to load object details: ${err.message}`);
  } finally {
    setLoading(false);
  }

  if (window.innerWidth < 768) {
    setIsLeftSidebarVisible(false);
  }
}, [authToken, tabs, loadUsedByData]);




// Helper function to generate basic DDL from columns
const generateBasicTableDDL = (tableName, columns) => {
  if (!columns || columns.length === 0) return `-- No columns available for ${tableName}`;
  
  const columnLines = columns.map(col => {
    const colName = col.COLUMN_NAME || col.column_name || col.name;
    const dataType = col.DATA_TYPE || col.data_type || 'VARCHAR2';
    const dataLength = col.DATA_LENGTH || col.data_length;
    const nullable = (col.NULLABLE === 'Y' || col.nullable === true) ? '' : ' NOT NULL';
    const defaultValue = col.DATA_DEFAULT ? ` DEFAULT ${col.DATA_DEFAULT}` : '';
    
    if (dataLength) {
      return `    ${colName} ${dataType}(${dataLength})${defaultValue}${nullable}`;
    } else {
      return `    ${colName} ${dataType}${defaultValue}${nullable}`;
    }
  }).join(',\n');
  
  return `CREATE TABLE ${tableName} (\n${columnLines}\n);`;
};


  // ============================================================
  // RENDER FUNCTIONS FOR EACH TAB
  // ============================================================

 // Add this effect in the main SchemaBrowser component
useEffect(() => {
  // Load used by data when the 'used by' tab becomes active and we have an active object
  if (activeTab === 'used by' && activeObject) {
    const objectType = activeObject.type?.toUpperCase();
    const owner = activeObject.owner || objectDetails?.owner;
    
    console.log('Used By Tab Active:', {
      activeObject: activeObject.name,
      objectType,
      owner,
      currentItems: usedByData.items.length,
      currentObjectName: usedByData.objectName,
      needsLoad: usedByData.items.length === 0 || usedByData.objectName !== activeObject.name,
      loading: usedByData.loading
    });
    
    // Check if we need to load data (either no items or we're on a different object)
    const needsLoad = usedByData.items.length === 0 || 
                     usedByData.objectName !== activeObject.name;
    
    if (needsLoad && !usedByData.loading) {
      console.log('Loading used by data for:', activeObject.name);
      loadUsedByData(
        activeObject.name,
        objectType,
        owner,
        1,
        usedByData.pageSize
      );
    }
  }
}, [activeTab, activeObject, objectDetails, usedByData.pageSize, usedByData.objectName, usedByData.items.length, usedByData.loading, loadUsedByData]);
 const renderUsedByTab = useCallback(() => {
  if (!activeObject) return null;
  
  const objectType = activeObject.type?.toUpperCase();
  const owner = activeObject.owner || objectDetails?.owner;
  
  return (
    <UsedByTab
      objectName={activeObject.name}
      objectType={objectType}
      owner={owner}
      colors={colors}
      onSelectObject={handleObjectSelect}
      onRefresh={() => loadUsedByData(
        activeObject.name,
        objectType,
        owner,
        usedByData.page,
        usedByData.pageSize
      )}
      usedByData={usedByData}
      usedByLoading={usedByData.loading}
      onPageChange={handleUsedByPageChange}
      onPageSizeChange={handleUsedByPageSizeChange}
      getObjectIcon={getObjectIcon}
    />
  );
}, [activeObject, objectDetails, usedByData, colors, handleObjectSelect, getObjectIcon, loadUsedByData, handleUsedByPageChange, handleUsedByPageSizeChange]);
  

  // Render Columns Tab - Enhanced for views while preserving table/procedure functionality
const renderColumnsTab = () => {
  // For procedures/functions, they don't have columns, so show appropriate message
  const objectType = activeObject?.type?.toUpperCase();
  
  // If this is a procedure or function, show parameters instead
  if (objectType === 'PROCEDURE' || objectType === 'FUNCTION') {
    return (
      <div className="flex-1 overflow-auto p-4">
        <div className="text-center" style={{ color: colors.textSecondary }}>
          This tab is for columns. Please use the Parameters tab for {objectType.toLowerCase()} details.
        </div>
      </div>
    );
  }
  
  // Check multiple locations for columns with priority for views
  let columns = [];
  
  // For synonyms that point to views, check targetObjectDetails.columns first
  if (objectType === 'SYNONYM' && objectDetails?.isSynonym) {
    if (objectDetails?.targetObjectDetails?.columns) {
      columns = objectDetails.targetObjectDetails.columns;
      console.log('📊 Found view columns in targetObjectDetails:', columns.length);
    } else if (objectDetails?.targetDetails?.columns) {
      columns = objectDetails.targetDetails.columns;
    }
  }
  
  // If no columns found yet, check other locations
  if (columns.length === 0) {
    columns = objectDetails?.columns || 
              objectDetails?.targetObjectDetails?.columns || 
              objectDetails?.targetDetails?.columns || 
              activeObject?.columns || 
              [];
  }
  
  console.log('📊 Columns found for', objectType, ':', columns.length);
  
  if (!columns || columns.length === 0) {
    return (
      <div className="flex-1 overflow-auto p-4">
        <div className="text-center" style={{ color: colors.textSecondary }}>
          No columns found for this {objectType === 'VIEW' ? 'view' : 'table'}
        </div>
      </div>
    );
  }
  
  return (
    <div className="flex-1 overflow-auto">
      <div className="border rounded" style={{ borderColor: colors.gridBorder, backgroundColor: colors.card }}>
        <div className="p-2 border-b" style={{ borderColor: colors.gridBorder }}>
          <div className="text-sm font-medium" style={{ color: colors.text }}>
            {objectType === 'VIEW' ? 'View' : 'Table'} Columns ({columns.length})
          </div>
        </div>
        <div className="overflow-auto">
          <table className="w-full">
            <thead style={{ backgroundColor: colors.tableHeader }}>
              <tr>
                <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>#</th>
                <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Column</th>
                <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Type</th>
                <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Length</th>
                <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Nullable</th>
                <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Default</th>
              </tr>
            </thead>
            <tbody>
              {columns.map((col, i) => {
                // Handle both uppercase and lowercase field names
                const columnId = col.COLUMN_ID || col.column_id || i + 1;
                const columnName = col.COLUMN_NAME || col.column_name || col.name;
                const dataType = col.DATA_TYPE || col.data_type || 'VARCHAR2';
                const dataLength = col.DATA_LENGTH || col.data_length;
                const dataPrecision = col.DATA_PRECISION || col.data_precision;
                const dataScale = col.DATA_SCALE || col.data_scale;
                const nullable = col.NULLABLE || col.nullable;
                const dataDefault = col.DATA_DEFAULT || col.data_default;
                
                // Format type with length/precision
                let typeDisplay = dataType;
                if (dataLength) {
                  typeDisplay = `${dataType}(${dataLength})`;
                } else if (dataPrecision !== null && dataPrecision !== undefined) {
                  typeDisplay = dataScale !== null && dataScale !== undefined
                    ? `${dataType}(${dataPrecision},${dataScale})`
                    : `${dataType}(${dataPrecision})`;
                }
                
                return (
                  <tr key={columnName + i} style={{ 
                    backgroundColor: i % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                    borderBottom: `1px solid ${colors.gridBorder}`
                  }}>
                    <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>{columnId}</td>
                    <td className="p-2 text-xs font-medium" style={{ color: colors.text }}>{columnName}</td>
                    <td className="p-2 text-xs" style={{ color: colors.text }}>{typeDisplay}</td>
                    <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>{dataLength || '-'}</td>
                    <td className="p-2 text-xs">
                      <span className={`px-2 py-0.5 rounded text-xs ${
                        (nullable === 'Y' || nullable === true) ? 
                        'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'
                      }`}>
                        {(nullable === 'Y' || nullable === true) ? 'Y' : 'N'}
                      </span>
                    </td>
                    <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>
                      {dataDefault || '-'}
                    </td>
                  </tr>
                );
              })}
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
    const isView = activeObject?.type?.toUpperCase() === 'VIEW' || 
                   objectDetails?.targetDetails?.OBJECT_TYPE === 'VIEW';
    
    let columns = tableData?.columns || [];
    if (columns.length === 0 && data.length > 0) {
      columns = Object.keys(data[0]).map(key => ({ name: key }));
    }
    
    if (columns.length === 0) {
      columns = objectDetails?.columns || activeObject?.columns || [];
    }
    
    const totalRows = tableData?.totalRows || 0;
    const totalPages = tableData?.totalPages || 1;
    
    const downloadData = () => {
      if (data.length === 0) return;
      
      const objectName = isView ? 
        (objectDetails?.TARGET_NAME || activeObject?.name) : 
        activeObject?.name;
      
      const headers = ['#', ...columns.map(col => col.name || col.COLUMN_NAME)];
      
      const csvRows = [
        headers.join(','),
        ...data.map((row, index) => {
          const rowValues = [
            index + 1 + (dataView.page - 1) * dataView.pageSize,
            ...columns.map(col => {
              const columnName = col.name || col.COLUMN_NAME;
              const value = row[columnName];
              
              if (value === null || value === undefined) return '';
              if (typeof value === 'object') return `"${JSON.stringify(value).replace(/"/g, '""')}"`;
              if (typeof value === 'string' && (value.includes(',') || value.includes('"') || value.includes('\n'))) {
                return `"${value.replace(/"/g, '""')}"`;
              }
              return value;
            })
          ];
          return rowValues.join(',');
        })
      ].join('\n');
      
      const blob = new Blob([csvRows], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', `${objectName}_data.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    };
    
    return (
      <div className="flex-1 flex flex-col h-full" style={{ minHeight: 0 }}>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 p-2 border-b shrink-0" style={{ borderColor: colors.border }}>
          <div className="flex items-center gap-2">
            <button 
              className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2"
              style={{ backgroundColor: colors.primaryDark, color: colors.white }}
              onClick={async () => {
                if (activeObject) {
                  const objectType = activeObject.type?.toUpperCase();
                  const effectiveType = objectDetails?.targetDetails?.OBJECT_TYPE || objectType;
                  
                  let objectName;
                  if (objectType === 'SYNONYM' && objectDetails?.TARGET_NAME) {
                    objectName = objectDetails.TARGET_NAME;
                  } else {
                    objectName = activeObject.name;
                  }
                  
                  if (objectType === 'TABLE' || effectiveType === 'TABLE') {
                    await loadTableData(objectName);
                  } else if (objectType === 'VIEW' || effectiveType === 'VIEW') {
                    await loadViewData(objectName);
                  }
                }
              }}
              disabled={tableDataLoading || !activeObject}
            >
              {tableDataLoading ? (
                <Loader size={12} className="animate-spin" />
              ) : (
                <Play size={12} />
              )}
              <span>Execute</span>
            </button>
            <select 
              className="px-2 py-1 border rounded text-sm"
              style={{ backgroundColor: colors.bg, borderColor: colors.border, color: colors.text }}
              value={dataView.pageSize}
              onChange={(e) => handlePageSizeChange(parseInt(e.target.value))}
              disabled={tableDataLoading}
            >
              <option value="15">15 rows</option>
              <option value="25">25 rows</option>
              <option value="50">50 rows</option>
              <option value="100">100 rows</option>
              <option value="250">250 rows</option>
              <option value="500">500 rows</option>
            </select>
            {data.length > 0 && (
              <button
                className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2"
                style={{ backgroundColor: colors.bg, border: `1px solid ${colors.border}`, color: colors.text }}
                onClick={downloadData}
              >
                <Download size={12} />
                <span>Download CSV</span>
              </button>
            )}
          </div>
          <div className="flex items-center gap-2">
            <span className="text-xs" style={{ color: colors.textSecondary }}>
              {totalRows > 0 ? (
                <>Page {dataView.page} of {totalPages} | Total: {totalRows.toLocaleString()} rows</>
              ) : (
                isView ? 'No view data' : 'No data'
              )}
            </span>
            {totalPages > 1 && (
              <div className="flex items-center gap-2">
                <button 
                  className="p-1 rounded hover:bg-opacity-50 disabled:opacity-50"
                  style={{ backgroundColor: colors.hover }}
                  onClick={() => handlePageChange(dataView.page - 1)}
                  disabled={tableDataLoading || dataView.page <= 1}
                >
                  <ChevronLeft size={14} />
                </button>
                <button 
                  className="p-1 rounded hover:bg-opacity-50 disabled:opacity-50"
                  style={{ backgroundColor: colors.hover }}
                  onClick={() => handlePageChange(dataView.page + 1)}
                  disabled={tableDataLoading || dataView.page >= totalPages}
                >
                  <ChevronRight size={14} />
                </button>
              </div>
            )}
          </div>
        </div>

        <div className="flex-1 relative" style={{ minHeight: 0, overflow: 'hidden' }}>
          {tableDataLoading ? (
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="text-center">
                <Loader className="animate-spin mx-auto mb-4" size={40} style={{ color: colors.primary }} />
                <div className="text-sm font-medium" style={{ color: colors.text }}>
                  Loading {isView ? 'view' : 'table'} data...
                </div>
                <div className="text-xs mt-2" style={{ color: colors.textSecondary }}>Please wait while we fetch the rows</div>
              </div>
            </div>
          ) : data.length === 0 ? (
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="text-center">
                <div className="flex justify-center mb-4">
                  {isView ? (
                    <FileText size={64} style={{ color: colors.textSecondary, opacity: 0.5 }} />
                  ) : (
                    <TableIcon size={64} style={{ color: colors.textSecondary, opacity: 0.5 }} />
                  )}
                </div>
                <div className="text-lg font-medium" style={{ color: colors.text }}>
                  No {isView ? 'view' : ''} data available
                </div>
                <div className="text-sm mt-2" style={{ color: colors.textSecondary }}>
                  Click the Execute button to load data
                </div>
              </div>
            </div>
          ) : (
            <div 
              className="absolute inset-0 border rounded" 
              style={{ 
                borderColor: colors.gridBorder,
                overflow: 'auto'
              }}
            >
              <table style={{ borderCollapse: 'collapse', minWidth: '100%', width: 'max-content' }}>
                <thead style={{ 
                  backgroundColor: colors.tableHeader, 
                  position: 'sticky', 
                  top: 0, 
                  zIndex: 10 
                }}>
                  <tr>
                    <th 
                      className="text-left p-2 text-xs font-medium"
                      style={{ 
                        color: colors.textSecondary, 
                        background: colors.bg,
                        position: 'sticky',
                        left: 0,
                        zIndex: 20,
                        minWidth: '60px',
                        borderRight: `1px solid ${colors.gridBorder}`,
                        borderBottom: `1px solid ${colors.gridBorder}`,
                        padding: '8px'
                      }}
                    >
                      <div className="flex items-center gap-1">
                        <span>#</span>
                      </div>
                    </th>
                    {columns.map(col => (
                      <th 
                        key={col.name || col.COLUMN_NAME} 
                        className="text-left p-2 text-xs font-medium cursor-pointer hover:bg-opacity-50"
                        onClick={() => handleSortChange(
                          col.name || col.COLUMN_NAME, 
                          dataView.sortColumn === (col.name || col.COLUMN_NAME) && dataView.sortDirection === 'ASC' ? 'DESC' : 'ASC'
                        )}
                        style={{ 
                          color: colors.textSecondary, 
                          background: colors.bg,
                          minWidth: '150px',
                          borderBottom: `1px solid ${colors.gridBorder}`,
                          padding: '8px',
                          whiteSpace: 'nowrap'
                        }}
                      >
                        <div className="flex items-center gap-1">
                          <span>{col.name || col.COLUMN_NAME}</span>
                          {dataView.sortColumn === (col.name || col.COLUMN_NAME) && (
                            dataView.sortDirection === 'ASC' ? 
                              <ChevronUp size={10} /> : 
                              <ChevronDown size={10} />
                          )}
                        </div>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {data.map((row, rowIndex) => (
                    <tr 
                      key={rowIndex} 
                      style={{ 
                        backgroundColor: rowIndex % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                      }}
                    >
                      <td 
                        className="p-2 text-xs border-b"
                        style={{ 
                          borderColor: colors.gridBorder,
                          color: colors.text,
                          background: rowIndex % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                          position: 'sticky',
                          left: 0,
                          zIndex: 5,
                          minWidth: '60px',
                          borderRight: `1px solid ${colors.gridBorder}`,
                          padding: '8px',
                          whiteSpace: 'nowrap'
                        }}
                      >
                        {rowIndex + 1 + (dataView.page - 1) * dataView.pageSize}
                      </td>
                      {columns.map(col => {
                        const columnName = col.name || col.COLUMN_NAME;
                        const value = row[columnName];
                        return (
                          <td 
                            key={columnName} 
                            className="p-2 text-xs border-b" 
                            style={{ 
                              borderColor: colors.gridBorder,
                              color: colors.text,
                              minWidth: '150px',
                              padding: '8px',
                              whiteSpace: 'nowrap'
                            }}
                            title={value?.toString()}
                          >
                            {value !== null && value !== undefined ? (
                              typeof value === 'object' ? JSON.stringify(value) : value.toString()
                            ) : (
                              <span style={{ color: colors.textTertiary }}>NULL</span>
                            )}
                          </td>
                        );
                      })}
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

 // Update renderParametersTab - find this function around line 2320
const renderParametersTab = useCallback(() => {
  // Check multiple locations for parameters
  const parameters = objectDetails?.targetObjectDetails?.parameters || 
                     objectDetails?.parameters || 
                     objectDetails?.arguments ||
                     activeObject?.parameters || 
                     [];
  
  console.log('🔍 renderParametersTab - parameters found:', parameters.length);
  console.log('📋 Parameters array:', parameters);
  
  if (!parameters || parameters.length === 0) {
    return (
      <div className="flex-1 overflow-auto p-4">
        <div className="text-center" style={{ color: colors.textSecondary }}>
          No parameters found for this procedure
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
              {parameters.map((param, i) => {
                // Handle both uppercase and lowercase field names
                const position = param.POSITION || param.position || i + 1;
                const name = param.argument_name || param.ARGUMENT_NAME || param.name || `Parameter ${i + 1}`;
                const dataType = param.DATA_TYPE || param.data_type || param.type || 'VARCHAR2';
                const mode = param.IN_OUT || param.in_out || param.mode || 'IN';
                const dataLength = param.DATA_LENGTH || param.data_length || '-';
                
                return (
                  <tr key={name + i} style={{ 
                    backgroundColor: i % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                    borderBottom: `1px solid ${colors.gridBorder}`
                  }}>
                    <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>{position}</td>
                    <td className="p-2 text-xs font-medium" style={{ color: colors.text }}>{name}</td>
                    <td className="p-2 text-xs" style={{ color: colors.text }}>{dataType}</td>
                    <td className="p-2 text-xs">
                      <span className={`px-2 py-0.5 rounded text-xs ${
                        mode === 'IN' ? 'bg-blue-500/10 text-blue-400' :
                        mode === 'OUT' ? 'bg-purple-500/10 text-purple-400' :
                        'bg-green-500/10 text-green-400'
                      }`}>
                        {mode}
                      </span>
                    </td>
                    <td className="p-2 text-xs hidden md:table-cell" style={{ color: colors.textSecondary }}>
                      {dataLength}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}, [objectDetails, activeObject, colors]);

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

  // Render DDL Tab
  const renderDDLTab = () => {
    const ddl = objectDDL || 
                objectDetails?.targetDetails?.text || 
                objectDetails?.text || 
                objectDetails?.ddl ||
                activeObject?.text || 
                activeObject?.spec || 
                activeObject?.body || 
                '';
    
    const ddlLines = ddl ? ddl.split('\n') : [];
    
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded p-4" style={{ borderColor: colors.border, backgroundColor: colors.codeBg }}>
          {ddlLoading ? (
            <div className="flex flex-col items-center justify-center py-12">
              <Loader className="animate-spin mb-4" size={32} style={{ color: colors.primary }} />
              <div className="text-sm font-medium" style={{ color: colors.text }}>Loading DDL...</div>
              <div className="text-xs mt-2" style={{ color: colors.textSecondary }}>
                Fetching DDL for {activeObject?.name}
              </div>
            </div>
          ) : (
            <>
              <div className="flex overflow-auto max-h-[calc(100vh-300px)] font-mono text-xs">
                {ddlLines.length > 0 && (
                  <div className="select-none text-right pr-4 border-r" 
                       style={{ color: colors.textSecondary, borderColor: colors.border }}>
                    {ddlLines.map((_, index) => (
                      <div key={index} className="pr-2">
                        {index + 1}
                      </div>
                    ))}
                  </div>
                )}
                
                <pre className="flex-1 whitespace-pre-wrap pl-4" style={{ color: colors.text }}>
                  {ddl || '-- No DDL available for this object'}
                </pre>
              </div>
              
              <div className="mt-2 flex justify-end">
                <button 
                  className="px-3 py-1 text-xs rounded hover:bg-opacity-50 transition-colors flex items-center gap-1"
                  style={{ backgroundColor: colors.hover, color: colors.text }}
                  onClick={() => handleCopyToClipboard(ddl, 'DDL')}
                  disabled={ddlLoading || !ddl}
                >
                  <Copy size={12} className="inline mr-1" />
                  Copy
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    );
  };

  // Render Definition Tab
  const renderDefinitionTab = () => {
    return renderDDLTab();
  };

  // Render Spec Tab
  const renderSpecTab = () => {
    return renderDDLTab();
  };

  // Render Body Tab
  const renderBodyTab = () => {
    const body = objectDetails?.body || objectDetails?.targetDetails?.body || '';
    
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded p-4" style={{ borderColor: colors.border, backgroundColor: colors.codeBg }}>
          {ddlLoading ? (
            <div className="flex flex-col items-center justify-center py-12">
              <Loader className="animate-spin mb-4" size={32} style={{ color: colors.primary }} />
              <div className="text-sm font-medium" style={{ color: colors.text }}>Loading Package Body...</div>
              <div className="text-xs mt-2" style={{ color: colors.textSecondary }}>
                Fetching body for {activeObject?.name}
              </div>
            </div>
          ) : (
            <>
              <pre className="text-xs font-mono whitespace-pre-wrap overflow-auto max-h-[calc(100vh-300px)]" style={{ color: colors.text }}>
                {body || 'No package body available'}
              </pre>
              <div className="mt-2 flex justify-end">
                <button 
                  className="px-3 py-1 text-xs rounded hover:bg-opacity-50 transition-colors flex items-center gap-1"
                  style={{ backgroundColor: colors.hover, color: colors.text }}
                  onClick={() => handleCopyToClipboard(body, 'body')}
                  disabled={ddlLoading || !body}
                >
                  <Copy size={12} className="inline mr-1" />
                  Copy
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    );
  };

  // Render Attributes Tab
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

  // Render Properties Tab
 // Render Properties Tab - Clean and simple
const renderPropertiesTab = () => {
    const details = objectDetails || activeObject || {};
    const responseData = details; // Data is directly in objectDetails
    
    // Simple status badge
    const renderStatusBadge = (status) => {
        const isValid = status === 'VALID' || status === 'ENABLED';
        return (
            <span className={`px-2 py-0.5 rounded text-xs ${
                isValid ? 'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'
            }`}>
                {status || '-'}
            </span>
        );
    };

    // Simple property item
    const renderPropertyItem = (label, value, isStatus = false) => (
        <div className="space-y-1">
            <div className="text-xs" style={{ color: colors.textSecondary }}>{label}</div>
            <div className="text-sm truncate" style={{ color: colors.text }}>
                {isStatus ? renderStatusBadge(value) : (value || '-')}
            </div>
        </div>
    );

    // For synonyms
    if (responseData.objectType === 'SYNONYM') {
      const targetBasicInfo = responseData.targetBasicInfo || {};
      const targetDetails = responseData.targetObjectDetails || {};
      
      return (
        <div className="flex-1 overflow-auto p-4">
          <div className="border rounded" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
            
            {/* Synonym Properties */}
            <div className="p-4 border-b" style={{ borderColor: colors.border }}>
              <h3 className="text-sm font-medium mb-3" style={{ color: colors.text }}>Synonym Properties</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {renderPropertyItem("Synonym Name", responseData.SYNONYM_NAME || responseData.objectName || responseData.name)}
                {renderPropertyItem("Synonym Owner", responseData.owner || responseData.OWNER)}
                {renderPropertyItem("Target Owner", responseData.TARGET_OWNER || targetBasicInfo.OWNER || targetDetails.OWNER)}
                {renderPropertyItem("Target Name", responseData.TARGET_NAME || targetBasicInfo.OBJECT_NAME || targetDetails.OBJECT_NAME)}
                {renderPropertyItem("Target Type", responseData.TARGET_TYPE || targetBasicInfo.OBJECT_TYPE || targetDetails.OBJECT_TYPE)}
                {renderPropertyItem(
                  "Target Status", 
                  responseData.TARGET_STATUS || targetBasicInfo.STATUS || targetDetails.STATUS,
                  true
                )}
                {renderPropertyItem(
                  "Target Created", 
                  responseData.TARGET_CREATED ? formatDateForDisplay(responseData.TARGET_CREATED) : 
                   targetBasicInfo.CREATED ? formatDateForDisplay(targetBasicInfo.CREATED) : 
                   targetDetails.CREATED ? formatDateForDisplay(targetDetails.CREATED) : '-'
                )}
                {renderPropertyItem(
                  "Target Modified", 
                  responseData.TARGET_MODIFIED ? formatDateForDisplay(responseData.TARGET_MODIFIED) : 
                   targetBasicInfo.LAST_DDL_TIME ? formatDateForDisplay(targetBasicInfo.LAST_DDL_TIME) : 
                   targetDetails.LAST_DDL_TIME ? formatDateForDisplay(targetDetails.LAST_DDL_TIME) : '-'
                )}
                {renderPropertyItem("DB Link", responseData.DB_LINK)}
                {renderPropertyItem("Temporary", responseData.TARGET_TEMPORARY || targetBasicInfo.TEMPORARY || targetDetails.TEMPORARY || 'N')}
                {renderPropertyItem("Generated", responseData.TARGET_GENERATED || targetBasicInfo.GENERATED || targetDetails.GENERATED || 'N')}
                {renderPropertyItem("Secondary", responseData.TARGET_SECONDARY || targetBasicInfo.SECONDARY || targetDetails.SECONDARY || 'N')}
              </div>
            </div>

            {/* Target Object Properties */}
            {targetDetails && Object.keys(targetDetails).length > 0 && (
              <div className="p-4">
                <h3 className="text-sm font-medium mb-3" style={{ color: colors.text }}>
                  Target Object Details ({targetBasicInfo.OBJECT_TYPE || responseData.TARGET_TYPE || targetDetails.OBJECT_TYPE})
                </h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {renderPropertyItem(
                    "Object Name", 
                    targetDetails.VIEW_NAME || targetDetails.TABLE_NAME || targetDetails.OBJECT_NAME || targetBasicInfo.OBJECT_NAME
                  )}
                  {renderPropertyItem("Owner", targetDetails.OWNER || targetBasicInfo.OWNER)}
                  {renderPropertyItem("Object Type", targetBasicInfo.OBJECT_TYPE || responseData.TARGET_TYPE || targetDetails.OBJECT_TYPE)}
                  {renderPropertyItem(
                    "Status", 
                    targetDetails.STATUS || targetDetails.OBJECT_STATUS || targetDetails.TABLE_STATUS || targetBasicInfo.STATUS,
                    true
                  )}
                  {renderPropertyItem(
                    "Created", 
                    targetDetails.CREATED ? formatDateForDisplay(targetDetails.CREATED) : 
                     targetBasicInfo.CREATED ? formatDateForDisplay(targetBasicInfo.CREATED) : '-'
                  )}
                  {renderPropertyItem(
                    "Last Modified", 
                    targetDetails.LAST_DDL_TIME ? formatDateForDisplay(targetDetails.LAST_DDL_TIME) : 
                     targetBasicInfo.LAST_DDL_TIME ? formatDateForDisplay(targetBasicInfo.LAST_DDL_TIME) : '-'
                  )}
                  
                  {/* Table properties */}
                  {(targetBasicInfo.OBJECT_TYPE === 'TABLE' || responseData.TARGET_TYPE === 'TABLE' || targetDetails.OBJECT_TYPE === 'TABLE') && (
                    <>
                      {renderPropertyItem("Row Count", targetDetails.NUM_ROWS ? targetDetails.NUM_ROWS.toLocaleString() : '-')}
                      {renderPropertyItem("Size", targetDetails.size_bytes !== undefined ? formatBytes(targetDetails.size_bytes) : '-')}
                      {renderPropertyItem("Tablespace", targetDetails.TABLESPACE_NAME)}
                      {renderPropertyItem("Blocks", targetDetails.BLOCKS)}
                      {renderPropertyItem("Avg Row Length", targetDetails.AVG_ROW_LEN)}
                      {renderPropertyItem(
                        "Last Analyzed", 
                        targetDetails.LAST_ANALYZED ? formatDateForDisplay(targetDetails.LAST_ANALYZED) : '-'
                      )}
                      {renderPropertyItem("Row Movement", targetDetails.ROW_MOVEMENT || 'DISABLED')}
                      {renderPropertyItem("Cache", targetDetails.CACHE || 'N')}
                      {renderPropertyItem("Column Count", targetDetails.column_count || targetDetails.COLUMN_COUNT)}
                    </>
                  )}

                  {/* View properties */}
                  {(targetBasicInfo.OBJECT_TYPE === 'VIEW' || responseData.TARGET_TYPE === 'VIEW' || targetDetails.OBJECT_TYPE === 'VIEW') && (
                    <>
                      {renderPropertyItem("Text Length", targetDetails.TEXT_LENGTH)}
                      {renderPropertyItem("Read Only", targetDetails.READ_ONLY || 'N')}
                      {renderPropertyItem("Column Count", targetDetails.COLUMN_COUNT || targetDetails.column_count)}
                    </>
                  )}
                </div>

                {/* Comment */}
                {targetDetails.comments && (
                  <div className="mt-4 p-3 rounded" style={{ backgroundColor: colors.hover }}>
                    <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Comment</div>
                    <div className="text-sm" style={{ color: colors.text }}>{targetDetails.comments}</div>
                  </div>
                )}

                {/* Fallback notice */}
                {responseData.fallbackUsed && (
                  <div className="mt-4 p-2 rounded text-xs" style={{ backgroundColor: 'rgba(251, 191, 36, 0.1)', color: colors.warning }}>
                    ⚠️ Using fallback data - details may be limited
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      );
    }
    
    // For regular objects
    const properties = [
      { label: 'Name', value: responseData.objectName || responseData.name || responseData.OBJECT_NAME },
      { label: 'Owner', value: responseData.owner || responseData.OWNER },
      { label: 'Type', value: responseData.objectType || responseData.type || responseData.OBJECT_TYPE },
      { label: 'Status', value: responseData.status || responseData.STATUS || 'VALID', isStatus: true },
      { 
        label: 'Created', 
        value: (responseData.created || responseData.CREATED) ? 
               formatDateForDisplay(responseData.created || responseData.CREATED) : null
      },
      { 
        label: 'Last Modified', 
        value: (responseData.last_ddl_time || responseData.LAST_DDL_TIME) ? 
               formatDateForDisplay(responseData.last_ddl_time || responseData.LAST_DDL_TIME) : null
      },
    ].filter(p => p.value !== null && p.value !== undefined); // Remove null/undefined values

    return (
      <div className="flex-1 overflow-auto p-4">
        <div className="border rounded p-4" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
          <h3 className="text-sm font-medium mb-3" style={{ color: colors.text }}>Properties</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {properties.map((prop, i) => (
              <div key={i} className="space-y-1">
                <div className="text-xs" style={{ color: colors.textSecondary }}>{prop.label}</div>
                <div className="text-sm truncate" style={{ color: colors.text }}>
                  {prop.isStatus ? renderStatusBadge(prop.value) : (prop.value || '-')}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  };



  // Render tab content based on active tab
  const renderTabContent = () => {
    if (!activeObject) {
      return (
        <>
        {!activeObject && (
          <div className="h-full flex flex-col items-center justify-center p-4">
            {isLoadingSchemaObjects ? (
              <>
                <div className="relative mb-6">
                  <Loader className="animate-spin" size={48} style={{ color: colors.primary }} />
                  <div className="absolute inset-0 flex items-center justify-center">
                    <Database size={24} style={{ color: colors.primary, opacity: 0.3 }} />
                  </div>
                </div>
                <p className="text-sm font-medium text-center" style={{ color: colors.text }}>
                  Loading schema objects...
                </p>
                <p className="text-xs mt-2 text-center" style={{ color: colors.textSecondary }}>
                  Fetching synonyms and other database objects
                </p>
              </>
            ) : (
              <>
                <Database size={48} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mb-4" />
                <p className="text-sm text-center" style={{ color: colors.textSecondary }}>
                  Select an object from the schema browser to view details
                </p>
              </>
            )}
          </div>
        )}
        </>
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
      case 'used by':
        return renderUsedByTab();
      default:
        return renderPropertiesTab();
    }
  };

  // Render context menu
  const renderContextMenu = () => {
    if (!showContextMenu || !contextObject) return null;

    const isMobile = window.innerWidth < 768;
    
    const menuItems = [
      { label: 'Open', icon: <ExternalLink size={12} />, action: () => handleObjectSelect(contextObject, contextObject.type) },
      { label: 'Generate API', icon: <Code size={12} />, action: () => {
        setSelectedForApiGeneration(contextObject);
        setShowApiModal(true);
        setShowContextMenu(false);
      }},
      { label: 'Copy DDL', icon: <Copy size={12} />, action: () => {
        handleCopyToClipboard(contextObject.text || '', 'DDL');
        setShowContextMenu(false);
      }},
      { label: 'Properties', icon: <Settings size={12} />, action: () => {
        handleObjectSelect(contextObject, contextObject.type);
        setActiveTab('properties');
        setShowContextMenu(false);
      }},
    ];

    return (
      <div 
        className="fixed z-50 rounded shadow-lg border py-1"
        style={{ 
          backgroundColor: colors.bg,
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
        <div className="px-3 py-2 border-b space-y-2" style={{ borderColor: colors.border }}>
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
              className="w-full px-4 py-3 text-xs text-left hover:bg-opacity-50 transition-colors flex items-center gap-3"
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

  // Show full page loader until initial load is complete
  if (!initialLoadComplete) {
    return <FullPageLoader colors={colors} />;
  }

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
            onClick={() => setShowApiModal(true)}
            className="px-3 py-1.5 rounded text-sm bg-gradient-to-r from-blue-500 via-violet-500 to-blue-500 hover:opacity-90 font-medium"
            style={{ color: 'white' }}
          >
            Generate New API
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
          handleSearch={handleSearch}
          handleCancelSearch={handleCancelSearch}
          colors={colors}
          objectTree={objectTree}
          handleToggleSection={handleToggleSection}
          handleLoadSection={handleLoadSection}
          handleLoadMore={handleLoadMore}
          schemaObjects={schemaObjects}
          filteredResults={filteredResults}
          loadingStates={loadingStates}
          activeObject={activeObject}
          handleObjectSelect={handleObjectSelect}
          getObjectIcon={getObjectIcon}
          handleContextMenu={handleContextMenu}
          loading={loading}
          onRefreshSchema={handleRefresh}
          schemaInfo={schemaInfo}
          loadedSections={loadedSections}
          pagination={pagination}
          isInitialLoad={isInitialLoad}
          hasActiveFilter={hasActiveFilter}  // This will now only be true after search
          isFiltering={isFiltering}
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
                 onClick={async () => {
                    // Don't do anything if it's already the active tab
                    if (tab.isActive) return;
                    
                    // Parse the tab ID to get the object info
                    // Tab ID format is: `${type}_${owner}_${name}`
                    const parts = tab.id.split('_');
                    if (parts.length >= 3) {
                      const type = parts[0];
                      const owner = parts[1];
                      const name = parts.slice(2).join('_'); // In case name has underscores
                      
                      // Create a minimal object to pass to handleObjectSelect
                      const objectToSelect = {
                        name: name,
                        owner: owner,
                        type: type,
                        id: tab.id // Use the existing tab ID
                      };
                      
                      // Call handleObjectSelect directly with the constructed object
                      await handleObjectSelect(objectToSelect, type);
                    } else {
                      // Fallback to the old method if parsing fails
                      const allObjects = Object.values(schemaObjects).flat();
                      const found = allObjects.find(obj => 
                        (obj.id || `${obj.owner}_${obj.name}`) === tab.objectId || 
                        (obj.name === tab.name && obj.owner === tab.owner)
                      );
                      if (found) {
                        await handleObjectSelect(found, tab.type);
                      }
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
          {activeObject && !loading && (
            <div className="p-3 border-b" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
              <div className="flex items-center gap-2 flex-wrap">
                {getObjectIcon(activeObject.type)}
                <span className="text-sm font-semibold" style={{ color: colors.text }}>{activeObject.name}</span>
                {activeObject.owner && (
                  <span className="text-xs" style={{ color: colors.textSecondary }}>[{activeObject.owner}]</span>
                )}
                {activeObject.type === 'SYNONYM' && (
                  <span 
                    className="text-[10px] px-2 py-0.5 rounded-full"
                    style={{ 
                      backgroundColor: colors.objectType.synonym + '20',
                      color: colors.objectType.synonym
                    }}
                  >
                    SYNONYM
                  </span>
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

          {/* Detail Tabs */}
          {activeObject && !loading && (
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
          <div className="flex-1 overflow-auto" style={{ backgroundColor: colors.card }}>
            {loading && activeObject ? (
              <div className="h-full w-full flex items-center justify-center min-h-[400px]">
                <div className="text-center">
                  <Loader className="animate-spin mx-auto mb-4" size={40} style={{ color: colors.primary }} />
                  <div className="text-sm font-medium" style={{ color: colors.text }}>Loading object details...</div>
                  <div className="text-xs mt-2" style={{ color: colors.textSecondary }}>
                    Fetching data for {activeObject.name}
                  </div>
                </div>
              </div>
            ) : (
              renderTabContent()
            )}
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
    selectedObject={selectedForApiGeneration} // This now has full details
    colors={colors}
    obType={obType}
    theme={theme}
    authToken={authToken}
    isEditing={false} // Explicitly false for new API
  />
)}
    </div>
  );
};

export default SchemaBrowser;