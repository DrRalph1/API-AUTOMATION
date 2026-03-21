import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import {
  Database, Table, FileText, Code, Package, Hash, Link, Type,
  Search, Filter, ChevronDown, ChevronRight, ChevronLeft,
  MoreVertical, Settings, User, Moon, Sun, RefreshCw, Plus, X, Check,
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
  GitBranch as DependencyIcon
} from 'lucide-react';
import ApiGenerationModal from '@/components/modals/ApiGenerationModal.js';

// Import PostgreSQLSchemaController
import {
  getCurrentSchemaInfo,
  getAllTablesForFrontendPaginated,
  getTableData,
  getTableColumnsPaginated,
  getTableConstraints,
  getAllViewsForFrontendPaginated,
  getAllProceduresForFrontendPaginated,
  getAllFunctionsForFrontendPaginated,
  getAllSchemasForFrontendPaginated,
  getAllTriggersForFrontendPaginated,
  getAllMaterializedViewsForFrontendPaginated,
  getAllSequencesForFrontendPaginated,
  getAllTypesForFrontendPaginated,
  getObjectBasicInfo,
  getObjectColumns,
  getObjectDDL,
  getProcedureParametersPaginated,
  getFunctionParametersPaginated,
  getSchemaItemsPaginated,
  getTypeDetails,
  getTriggerDetails,
  searchObjectsPaginated,
  extractPaginatedData,
  handlePostgreSQLSchemaBrowserResponse,
  extractTableData,
  extractDDL,
  formatBytes,
  formatDateForDisplay,
  getObjectTypeIcon,
  isSupportedForAPIGeneration,
  generateSampleQuery,
  getUsedByPaginated,
  getUsedBySummary,
  extractUsedByItems,
  extractUsedBySummary
} from "../../controllers/PostgreSQLSchemaController.js";

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

// Enhanced cache with TTL and type-specific keys
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
      <p className="text-sm" style={{ color: colors.textSecondary }}>Please wait while we connect to the PostgreSQL database...</p>
    </div>
  </div>
);

// Skeleton loader for sidebar
const SidebarSkeleton = ({ colors }) => (
  <div className="p-3 space-y-4">
    {[1, 2, 3, 4, 5, 6, 7, 8].map(i => (
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

// Tab content loaders
const TabLoader = ({ colors, message }) => (
  <div className="flex-1 flex items-center justify-center min-h-[400px]">
    <div className="text-center">
      <Loader className="animate-spin mx-auto mb-4" size={40} style={{ color: colors.primary }} />
      <div className="text-sm font-medium" style={{ color: colors.text }}>{message || 'Loading...'}</div>
    </div>
  </div>
);

// FilterInput Component (unchanged - works the same for PostgreSQL)
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

  useEffect(() => {
    setLocalFilterValue(filterQuery);
  }, [filterQuery]);

  const handleFilterChange = useCallback((e) => {
    const value = e.target.value;
    setLocalFilterValue(value);
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
                  onClick={onCancelSearch} 
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
                {selectedOwner !== 'ALL' && `Schema: ${selectedOwner}`}
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

// ObjectTreeSection Component - REMOVED PACKAGES (schemas are separate in PostgreSQL)
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
  if ((hasActiveFilter || filterQuery) && !isExpanded && !isLoading && !isFiltering) {
    onToggle(type);
  }
}, [hasActiveFilter, filterQuery, isExpanded, isLoading, onToggle, type, isFiltering]);
  
  const filteredObjects = objects || [];
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
    const owner = obj.owner || 'unknown';
    const name = obj.name || '';
    const type = obj.type || obj.objectType || '';
    const timestamp = obj.timestamp || obj.lastModified || Date.now();
    return `${owner}_${name}_${type}_${obj.status || ''}_${obj.id || ''}`.replace(/\s+/g, '_');
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
                    </div>
                    {isInvalid && (
                      <div className="flex items-center gap-1 shrink-0">
                        <AlertCircle size={10} style={{ color: colors.error }} />
                      </div>
                    )}
                  </button>
                );
              })}
              
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

// LeftSidebar Component - REMOVED PACKAGES, ADDED SCHEMAS
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

  const filterObjects = useCallback((objects, type) => {
  if (hasActiveFilter && filteredResults) {
    // Return the filtered results for this type if they exist
    if (filteredResults[type]) {
      return filteredResults[type];
    }
    // Handle special cases where type name might not match exactly
    const typeMapping = {
      'indexes': 'indexes',
      'other': 'other'
    };
    if (typeMapping[type] && filteredResults[typeMapping[type]]) {
      return filteredResults[typeMapping[type]];
    }
    return [];
  }
  if (!hasActiveFilter) {
    return objects || [];
  }
  return [];
}, [hasActiveFilter, filteredResults]);

  const getDisplayCount = useCallback((type, totalCount) => {
    if (hasActiveFilter && filteredResults && filteredResults[type]) {
      return filteredResults[type].length;
    }
    return totalCount;
  }, [hasActiveFilter, filteredResults]);

  // PostgreSQL object types - NO PACKAGES (schemas are separate), NO SYNONYMS
  const sectionDefinitions = [
    { title: 'Tables', type: 'tables', iconType: 'table' },
    { title: 'Indexes', type: 'indexes', iconType: 'index' },  // ADD THIS
    { title: 'Procedures', type: 'procedures', iconType: 'procedure' },
    { title: 'Views', type: 'views', iconType: 'view' },
    { title: 'Functions', type: 'functions', iconType: 'function' },
    { title: 'Sequences', type: 'sequences', iconType: 'sequence' },
    { title: 'Types', type: 'types', iconType: 'type' },
    { title: 'Triggers', type: 'triggers', iconType: 'trigger' },
    { title: 'Materialized Views', type: 'materializedViews', iconType: 'materialized-view' },
    { title: 'Other', type: 'other', iconType: 'database' }  // ADD THIS for any other types
  ];

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

  if (isInitialLoad && !loadedSections.tables) {
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

      <div className="p-3 border-b shrink-0" style={{ borderColor: colors.border }}>
        <div className="flex items-center justify-between mb-0">
          <div className="flex items-center gap-2 flex-1 text-left">
            <Database size={16} style={{ color: colors.primary }} />
            <span className="text-sm font-medium truncate uppercase" style={{ color: colors.text }}>
              {schemaInfo?.currentUser || schemaInfo?.schemaName || 'Schema Browser'}
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

      {renderSchemaInfo()}

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

      <div className="flex-1 overflow-y-auto overflow-x-hidden p-3 scrollbar-thin scrollbar-thumb-rounded" 
           style={{ 
             scrollbarWidth: 'thin',
             scrollbarColor: `${colors.border} transparent`
           }}>
        <div className="space-y-1">
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

        {/* {(loadingStates.procedures || loadingStates.views || loadingStates.functions || 
          loadingStates.tables || loadingStates.sequences || loadingStates.types || 
          loadingStates.triggers || loadingStates.materializedViews) && !hasActiveFilter && (
          <div className="flex items-center justify-center gap-2 py-4 mt-2 border-t" style={{ borderColor: colors.border }}>
            <Loader size={14} className="animate-spin" style={{ color: colors.primary }} />
            <span className="text-xs" style={{ color: colors.textSecondary }}>Loading more objects...</span>
          </div>
        )} */}

        {/* {hasActiveFilter && !isFiltering && 
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
        )} */}

        {/* {!hasActiveFilter && !isFiltering && 
         !schemaObjects.procedures?.length && !schemaObjects.views?.length && 
         !schemaObjects.functions?.length && !schemaObjects.tables?.length && 
         !schemaObjects.sequences?.length && !schemaObjects.types?.length && 
         !schemaObjects.triggers?.length && !schemaObjects.materializedViews?.length && !isInitialLoad && (
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
        )} */}
      </div>

      <div className="p-3 border-t shrink-0 text-[10px]" style={{ borderColor: colors.border, color: colors.textTertiary }}>
        <div className="flex items-center justify-between">
          <span>Total Objects:</span>
          <span className="font-mono">
            {(schemaObjects.tablesTotalCount || 0).toLocaleString()}
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

// MobileBottomNav Component (unchanged)
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
// Used By Tab Components (unchanged - works with PostgreSQL)
// ============================================================

const UsedBySummary = ({ data, colors, onRefresh }) => {
  if (!data || !data.byType || !Array.isArray(data.byType) || data.byType.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-8 px-4">
        <DependencyIcon size={48} style={{ color: colors.textTertiary, opacity: 0.5 }} />
        <p className="text-sm mt-4" style={{ color: colors.textSecondary }}>
          No dependency information available
        </p>
      </div>
    );
  }

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
          const dependentType = item.DEPENDENT_TYPE || item.dependentType || 'Unknown';
          const count = item.COUNT || item.count || 0;
          const validCount = item.VALID_COUNT || item.validCount || 0;
          const invalidCount = item.INVALID_COUNT || item.invalidCount || 0;
          
          return (
            <div
              key={index}
              className="p-3 rounded-lg border"
              style={{ 
                backgroundColor: colors.card,
                borderColor: colors.border
              }}
            >
              <div className="text-xs mb-1 font-medium" style={{ color: colors.textSecondary }}>
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
  const getFieldValue = (item, fieldName) => {
    const upperField = fieldName.toUpperCase();
    const lowerField = fieldName.toLowerCase();
    
    return item[upperField] !== undefined ? item[upperField] : 
           item[lowerField] !== undefined ? item[lowerField] : 
           item[fieldName];
  };

  return (
    <div className="flex-1 overflow-auto">
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
  return (
    <div className="flex-1 flex flex-col h-full overflow-hidden">
      {usedByData.summary && (
        <UsedBySummary 
          data={usedByData.summary} 
          colors={colors}
          onRefresh={onRefresh}
        />
      )}

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

// ============================================================
// Main PostgreSQLSchemaBrowser Component
// ============================================================

const PostgreSQLSchemaBrowser = ({ theme, isDark, toggleTheme, authToken }) => {
  // Colors (same as Oracle version)
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
      'materialized-view': 'rgb(139 92 246)',
      procedure: 'rgb(167 139 250)',
      function: 'rgb(251 191 36)',
      sequence: 'rgb(100 116 139)',
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
      'materialized-view': '#8b5cf6',
      procedure: '#8b5cf6',
      function: '#f59e0b',
      sequence: '#64748b',
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
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const [hasActiveFilter, setHasActiveFilter] = useState(false);
  const [searchPerformed, setSearchPerformed] = useState(false);
  const [obType, setObType] = useState("");

  // Tab-specific data states
  const [tabData, setTabData] = useState({
    properties: { loading: false, data: null },
    columns: { loading: false, data: null, page: 1, totalPages: 1 },
    data: { loading: false, data: null, page: 1, pageSize: 15, totalPages: 1, sortColumn: '', sortDirection: 'ASC' },
    parameters: { loading: false, data: null, page: 1, totalPages: 1 },
    constraints: { loading: false, data: null },
    ddl: { loading: false, data: '' },
    definition: { loading: false, data: '' },
    spec: { loading: false, data: '' },
    body: { loading: false, data: '' },
    attributes: { loading: false, data: null },
    'used by': { loading: false, items: [], totalCount: 0, totalPages: 1, page: 1, pageSize: 10, summary: null }
  });

  const searchAbortController = useRef(null);
  const initAttempted = useRef(false);
  const selectingRef = useRef(null);
  
  // Handle search
  const handleSearch = useCallback((searchTerm) => {
    if (searchTerm && searchTerm.length >= 2) {
      if (isFiltering || searchPerformed) {
        handleClearFilters();
      }
      setSearchPerformed(true);
      searchObjects(searchTerm, selectedOwner);
    }
  }, [searchObjects, selectedOwner, isFiltering, searchPerformed, handleClearFilters]);

  // Pagination state for each object type - PostgreSQL types (no packages)
  const [pagination, setPagination] = useState({
    tables: { page: 1, totalPages: 1, totalCount: 0 }, // Tables first
    procedures: { page: 1, totalPages: 1, totalCount: 0 },
    views: { page: 1, totalPages: 1, totalCount: 0 },
    functions: { page: 1, totalPages: 1, totalCount: 0 },
    sequences: { page: 1, totalPages: 1, totalCount: 0 },
    types: { page: 1, totalPages: 1, totalCount: 0 },
    triggers: { page: 1, totalPages: 1, totalCount: 0 },
    materializedViews: { page: 1, totalPages: 1, totalCount: 0 }
  });

  // Filtered results state
  const [filteredResults, setFilteredResults] = useState({});

  const [isFiltering, setIsFiltering] = useState(false);
  const [filterSearchTerm, setFilterSearchTerm] = useState('');

  // Loaded sections state - PostgreSQL types
  const [loadedSections, setLoadedSections] = useState({
    tables: false, // Tables first
    procedures: false,
    views: false,
    functions: false,
    sequences: false,
    types: false,
    triggers: false,
    materializedViews: false
  });
  
  // Schema objects state with total counts - PostgreSQL types (no packages)
  const [schemaObjects, setSchemaObjects] = useState({
    tables: [],
    tablesTotalCount: 0,
    procedures: [],
    proceduresTotalCount: 0,
    views: [],
    viewsTotalCount: 0,
    functions: [],
    functionsTotalCount: 0,
    sequences: [],
    sequencesTotalCount: 0,
    types: [],
    typesTotalCount: 0,
    triggers: [],
    triggersTotalCount: 0,
    materializedViews: [],
    materializedViewsTotalCount: 0
  });
  
  // Loading states for each object type
  const [loadingStates, setLoadingStates] = useState({
    tables: false, // Tables first
    procedures: false,
    views: false,
    functions: false,
    sequences: false,
    types: false,
    triggers: false,
    materializedViews: false
  });
  
  // Object tree expanded state - TABLES EXPANDED BY DEFAULT, others closed
  const [objectTree, setObjectTree] = useState({
    tables: true, // Tables expanded by default
    procedures: false, // Procedures closed by default
    views: false,
    functions: false,
    sequences: false,
    types: false,
    triggers: false,
    materializedViews: false
  });
  
  const [activeObject, setActiveObject] = useState(null);
  const [activeTab, setActiveTab] = useState('properties');
  const [tabs, setTabs] = useState([]);
  
  // Context menu
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuPosition, setContextMenuPosition] = useState({ x: 0, y: 0 });
  const [contextObject, setContextObject] = useState(null);

  const [isLoadingSchemaObjects, setIsLoadingSchemaObjects] = useState(true);

  // Track which tabs have been loaded for current object
  const [loadedTabs, setLoadedTabs] = useState({});

  // Track resolved synonym info (PostgreSQL doesn't have synonyms, but keep for compatibility)
  const [synonymInfo, setSynonymInfo] = useState(null);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (searchAbortController.current) {
        searchAbortController.current.abort();
      }
      ongoingRequests.clear();
      selectingRef.current = null;
    };
  }, []);

  // Set hasActiveFilter
  useEffect(() => {
    setHasActiveFilter(searchPerformed);
  }, [searchPerformed]);

  // Get Object Icon - PostgreSQL types
  const getObjectIcon = useCallback((type) => {
    const objectType = type?.toLowerCase() || '';
    const iconColor = colors.objectType[objectType] || colors.textSecondary;
    const iconProps = { size: 14, style: { color: iconColor } };
    
    switch(objectType) {
      case 'table': return <Table {...iconProps} />;
      case 'view': return <FileText {...iconProps} />;
      case 'materialized-view': return <Database {...iconProps} />;
      case 'procedure': return <Terminal {...iconProps} />;
      case 'function': return <Code {...iconProps} />;
      case 'sequence': return <Hash {...iconProps} />;
      case 'type': return <Type {...iconProps} />;
      case 'trigger': return <Zap {...iconProps} />;
      default: return <Database {...iconProps} />;
    }
  }, [colors]);

  // Helper function to extract items from paginated response
  const extractItemsFromResponse = (response) => {
    if (!response) return { items: [], totalPages: 1, totalCount: 0, page: 1 };
    
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
        return null;
    }

    Logger.info('PostgreSQLSchemaBrowser', 'loadSchemaInfo', 'Loading schema info');
    setError(null);

    try {
        const response = await getCurrentSchemaInfo(authToken);
        const data = handlePostgreSQLSchemaBrowserResponse(response);
        
        Logger.info('PostgreSQLSchemaBrowser', 'loadSchemaInfo', `Connected as: ${data.currentUser || data.currentSchema}`);
        
        setSchemaInfo(data);
        
        if (data.currentUser || data.currentSchema) {
            setOwners([data.currentUser || data.currentSchema]);
        }
        
        return data;

    } catch (err) {
        Logger.error('PostgreSQLSchemaBrowser', 'loadSchemaInfo', 'Error', err);
        setError(`Failed to connect: ${err.message}`);
        return null;
    }
  }, [authToken]);

  // Load initial data (TABLES first instead of procedures)
  const loadInitialData = useCallback(async () => {
    if (!authToken) return;
    
    Logger.info('PostgreSQLSchemaBrowser', 'loadInitialData', 'Loading tables first');
    
    try {
      // Load tables first
      const tablesData = await loadTables();
      
      // Load schema info in parallel
      if (!schemaInfo) {
        await loadSchemaInfo();
      }
      
      // Immediately set initial load complete
      setIsInitialLoad(false);
      setLoading(false);
      setInitialLoadComplete(true);
      
      // Auto-select first table ONLY if nothing is selected yet
      if (tablesData?.items && tablesData.items.length > 0 && !activeObject) {
        const firstTable = tablesData.items[0];
        
        const tableWithId = {
          ...firstTable,
          id: firstTable.id || `${firstTable.owner || 'unknown'}_${firstTable.name}`,
          type: 'TABLE'
        };
        
        setActiveObject(tableWithId);
        setSelectedForApiGeneration(tableWithId);
        setObType('TABLE');
        
        const tabId = `TABLE_${firstTable.owner || 'unknown'}_${firstTable.name}_${Date.now()}`;
        setTabs([{
          id: tabId,
          name: firstTable.name,
          type: 'TABLE',
          objectId: tableWithId.id,
          owner: firstTable.owner,
          isActive: true
        }]);
      }
      
      Logger.info('PostgreSQLSchemaBrowser', 'loadInitialData', 'Initial data loaded with tables');
      
    } catch (err) {
      Logger.error('PostgreSQLSchemaBrowser', 'loadInitialData', 'Error loading initial data', err);
      setError(`Failed to load initial data: ${err.message}`);
      setIsInitialLoad(false);
      setLoading(false);
    }
  }, [authToken, loadTables, loadSchemaInfo, activeObject]);

  // Load tables with higher page size (replacing loadProcedures)
  const loadTables = useCallback(async () => {
    if (!authToken) return null;
    
    Logger.info('PostgreSQLSchemaBrowser', 'loadTables', 'Loading tables first');
    
    const cacheKey = `tables_${authToken.substring(0, 10)}_page1`;
    const cached = objectCache.get(cacheKey);
    
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      Logger.debug('PostgreSQLSchemaBrowser', 'loadTables', 'Loaded tables from cache');
      setSchemaObjects(prev => ({ 
        ...prev, 
        tables: cached.data.items,
        tablesTotalCount: cached.data.totalCount 
      }));
      setPagination(prev => ({
        ...prev,
        tables: {
          page: cached.data.page,
          totalPages: cached.data.totalPages,
          totalCount: cached.data.totalCount
        }
      }));
      setLoadedSections(prev => ({ ...prev, tables: true }));
      return cached.data;
    }
    
    setLoadingStates(prev => ({ ...prev, tables: true }));
    
    try {
      const response = await getAllTablesForFrontendPaginated(authToken, { page: 1, pageSize: 10 });
      const { items, totalCount, totalPages } = extractItemsFromResponse(response);
      
      setSchemaObjects(prev => ({ 
        ...prev, 
        tables: items,
        tablesTotalCount: totalCount 
      }));
      setPagination(prev => ({
        ...prev,
        tables: { page: 1, totalPages, totalCount }
      }));
      setLoadedSections(prev => ({ ...prev, tables: true }));
      
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
      
      return { items, totalCount, totalPages };
      
    } catch (err) {
      Logger.error('PostgreSQLSchemaBrowser', 'loadTables', 'Error loading tables', err);
      return null;
    } finally {
      setLoadingStates(prev => ({ ...prev, tables: false }));
    }
  }, [authToken]);

  // Load remaining data in background
  const loadRemainingData = useCallback(async () => {
    if (!authToken) return;
    
    Logger.info('PostgreSQLSchemaBrowser', 'loadRemainingData', 'Loading remaining data in background');
    
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    const remainingTypes = [
      { type: 'procedures', fetcher: getAllProceduresForFrontendPaginated },
      { type: 'views', fetcher: getAllViewsForFrontendPaginated },
      { type: 'functions', fetcher: getAllFunctionsForFrontendPaginated },
      { type: 'sequences', fetcher: getAllSequencesForFrontendPaginated },
      { type: 'types', fetcher: getAllTypesForFrontendPaginated },
      { type: 'triggers', fetcher: getAllTriggersForFrontendPaginated },
      { type: 'materializedViews', fetcher: getAllMaterializedViewsForFrontendPaginated }
    ];
    
    for (const { type, fetcher } of remainingTypes) {
      if (hasActiveFilter) {
        Logger.info('PostgreSQLSchemaBrowser', 'loadRemainingData', `Skipping ${type} load due to active filter`);
        continue;
      }
      
      await new Promise(resolve => setTimeout(resolve, 300));
      
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
          continue;
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
        
      } catch (err) {
        Logger.error('PostgreSQLSchemaBrowser', 'loadRemainingData', `Error loading ${type}`, err);
      } finally {
        setLoadingStates(prev => ({ ...prev, [type]: false }));
      }
    }
    
    setIsLoadingSchemaObjects(false);
    Logger.info('PostgreSQLSchemaBrowser', 'loadRemainingData', 'All remaining data loaded');
    
  }, [authToken, hasActiveFilter]);

  // Initialize component
  useEffect(() => {
    let isMounted = true;
    
    const initialize = async () => {
      if (!authToken || initialized || initAttempted.current) return;
      
      initAttempted.current = true;
      Logger.info('PostgreSQLSchemaBrowser', 'initialize', 'Starting initialization');
      
      await loadInitialData();
      
      if (isMounted) {
        setInitialized(true);
        
        setTimeout(() => {
          loadRemainingData().catch(err => {
            Logger.error('PostgreSQLSchemaBrowser', 'initialize', 'Error loading remaining data', err);
          });
        }, 1500);
      }
    };
    
    initialize();
    
    return () => {
      isMounted = false;
    };
  }, [authToken]);

  // Load more objects
  const loadObjectType = useCallback(async (type, page = 1, pageSize = 10) => {
    if (!authToken) return;
    
    const requestKey = `${type}_page_${page}`;
    if (loadingStates[type] || ongoingRequests.has(requestKey)) {
      Logger.debug('PostgreSQLSchemaBrowser', 'loadObjectType', `Already loading ${type} page ${page}, skipping`);
      return;
    }
    
    Logger.info('PostgreSQLSchemaBrowser', 'loadObjectType', `Loading ${type} page ${page} from API`);
    
    setLoadingStates(prev => ({ ...prev, [type]: true }));
    ongoingRequests.set(requestKey, true);
    
    try {
      let response;
      switch(type) {
        case 'tables':
          response = await getAllTablesForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'procedures':
          response = await getAllProceduresForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'views':
          response = await getAllViewsForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'functions':
          response = await getAllFunctionsForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'sequences':
          response = await getAllSequencesForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'types':
          response = await getAllTypesForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'triggers':
          response = await getAllTriggersForFrontendPaginated(authToken, { page, pageSize });
          break;
        case 'materializedViews':
          response = await getAllMaterializedViewsForFrontendPaginated(authToken, { page, pageSize });
          break;
        default:
          return;
      }
      
      const { items, totalCount, page: currentPage, totalPages } = extractItemsFromResponse(response);
      
      Logger.info('PostgreSQLSchemaBrowser', 'loadObjectType', `Loaded ${items.length} ${type} (page ${page})`);
      
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
      Logger.error('PostgreSQLSchemaBrowser', 'loadObjectType', `Error loading ${type}`, err);
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

  // Load Properties (similar to Oracle version but without synonym resolution)
  const loadProperties = useCallback(async (object, type, owner) => {
    if (!authToken || !object || !type) return;
    
    let isMounted = true;
    
    setTabData(prev => ({
      ...prev,
      properties: { loading: true, data: prev.properties.data }
    }));
    
    try {
      let effectiveType = type;
      let effectiveName = object.name;
      let effectiveOwner = owner;
      
      // No synonym resolution in PostgreSQL
      
      let data = null;
      
      try {
        const response = await getObjectBasicInfo(authToken, {
          objectType: effectiveType,
          objectName: effectiveName,
          owner: effectiveOwner
        });
        data = handlePostgreSQLSchemaBrowserResponse(response);
      } catch (err) {
        console.warn(`Failed to fetch object info for ${effectiveType} ${effectiveName} from owner ${effectiveOwner}:`, err);
      }
      
      if (!isMounted) return;
      
      const currentActiveId = activeObject?.id;
      const objectId = object.id || `${owner || 'unknown'}_${object.name}`;
      
      if (currentActiveId !== objectId) {
        console.log('Object changed, not updating properties');
        return;
      }
      
      if (!data) {
        console.error('All attempts to fetch object properties failed');
        setTabData(prev => ({
          ...prev,
          properties: { 
            loading: false, 
            data: null,
            error: `Unable to load properties for ${object.name} (${type})`
          }
        }));
        return;
      }
      
      const enrichedData = {
        ...data,
        name: effectiveName,
        type: effectiveType,
        owner: effectiveOwner,
        isSynonym: false
      };
      
      setTabData(prev => ({
        ...prev,
        properties: { loading: false, data: enrichedData }
      }));
      
      return enrichedData;
      
    } catch (err) {
      Logger.error('PostgreSQLSchemaBrowser', 'loadProperties', 'Error loading properties', err);
      
      if (isMounted) {
        setTabData(prev => ({
          ...prev,
          properties: { loading: false, data: null }
        }));
      }
      return null;
    }
  }, [authToken, activeObject]);

  // Load Columns (for Tables/Views) or Parameters (for Procedures/Functions)
  const loadColumns = useCallback(async (object, type, owner, page = 1, pageSize = 50) => {
    if (!authToken || !object || !type) return;
    
    let effectiveType = type;
    let effectiveName = object.name;
    let effectiveOwner = owner;
    
    const cacheKey = `columns_${effectiveType}_${effectiveOwner || 'unknown'}_${effectiveName}_page${page}`;
    const cached = objectCache.get(cacheKey);
    
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      setTabData(prev => ({
        ...prev,
        columns: { 
          loading: false, 
          data: cached.data.items,
          page: cached.data.page,
          totalPages: cached.data.totalPages
        }
      }));
      return;
    }
    
    setTabData(prev => ({
      ...prev,
      columns: { ...prev.columns, loading: true }
    }));
    
    try {
      let response;
      let items = [];
      let totalPages = 1;
      let currentPage = page;
      
      if (effectiveType === 'TABLE' || effectiveType === 'VIEW' || effectiveType === 'MATERIALIZED VIEW') {
        response = await getTableColumnsPaginated(authToken, {
          tableName: effectiveName,
          owner: effectiveOwner,
          page,
          pageSize
        });
        
        const extracted = extractItemsFromResponse(response);
        items = extracted.items;
        totalPages = extracted.totalPages;
        currentPage = extracted.page;
        
      } else if (effectiveType === 'PROCEDURE') {
        response = await getProcedureParametersPaginated(authToken, {
          procedureName: effectiveName,
          owner: effectiveOwner,
          page,
          pageSize
        });
        
        const extracted = extractItemsFromResponse(response);
        items = extracted.items;
        totalPages = extracted.totalPages;
        currentPage = extracted.page;
        
      } else if (effectiveType === 'FUNCTION') {
        response = await getFunctionParametersPaginated(authToken, {
          functionName: effectiveName,
          owner: effectiveOwner,
          page,
          pageSize
        });
        
        const responseData = response?.data || {};
        items = responseData.items || [];
        totalPages = responseData.totalPages || 1;
        currentPage = responseData.page || page;
        
      } else if (effectiveType === 'TYPE') {
        const typeDetails = await getTypeDetails(authToken, effectiveName);
        const typeData = handlePostgreSQLSchemaBrowserResponse(typeDetails);
        items = typeData.attributes || [];
        totalPages = 1;
        currentPage = 1;
        
      } else {
        setTabData(prev => ({
          ...prev,
          columns: { loading: false, data: [] }
        }));
        return;
      }
      
      objectCache.set(cacheKey, { 
        data: { items, page: currentPage, totalPages }, 
        timestamp: Date.now() 
      });
      
      setTabData(prev => ({
        ...prev,
        columns: { 
          loading: false, 
          data: items,
          page: currentPage,
          totalPages: totalPages
        }
      }));
      
    } catch (err) {
      console.error('Error loading columns/parameters:', err);
      Logger.error('PostgreSQLSchemaBrowser', 'loadColumns', `Error loading ${effectiveType} columns/parameters`, err);
      setTabData(prev => ({
        ...prev,
        columns: { loading: false, data: [] }
      }));
    }
  }, [authToken]);

// Load Data (for Tables/Views)
const loadData = useCallback(async (object, type, owner, params = {}) => {
  if (!authToken || !object || !type) return;

  const { page = 1, pageSize = 15, sortColumn, sortDirection = 'ASC' } = params;

  let effectiveType = type;
  let effectiveName = object.name;
  let effectiveOwner = owner;

  if (effectiveType !== 'TABLE' && effectiveType !== 'VIEW' && effectiveType !== 'MATERIALIZED VIEW') {
    setTabData(prev => ({
      ...prev,
      data: { loading: false, data: null, error: 'This object type does not support data view' }
    }));
    return;
  }

  setTabData(prev => ({
    ...prev,
    data: { ...prev.data, loading: true, error: null }
  }));

  try {
    const response = await getTableData(authToken, {
      tableName: effectiveName,
      owner: effectiveOwner,
      page,
      pageSize,
      sortColumn,
      sortDirection
    });

    console.log('loadData full response:', response); // Debug log

    // Extract data - handle the nested structure
    let rows = [];
    let columns = [];
    let totalPages = 1;
    let totalRows = 0;
    let currentPage = page;
    
    // Check if response.data exists (from controller wrapper)
    if (response && response.data) {
      const responseData = response.data;
      
      // Check if response.data.data exists (from API response)
      if (responseData.data) {
        const apiData = responseData.data;
        
        // Get rows from apiData.rows
        if (apiData.rows && Array.isArray(apiData.rows)) {
          rows = apiData.rows;
          totalRows = apiData.totalRows || rows.length;
          totalPages = apiData.totalPages || 1;
          currentPage = apiData.page || page;
        }
        
        // Get columns from apiData.columns
        if (apiData.columns && Array.isArray(apiData.columns)) {
          columns = apiData.columns;
        }
        // If columns not provided but we have rows, extract from first row
        else if (rows.length > 0 && columns.length === 0) {
          columns = Object.keys(rows[0]).map(key => ({ 
            name: key, 
            type: 'text', 
            nullable: true 
          }));
        }
      }
      // Check if response.data has rows directly (alternative structure)
      else if (responseData.rows && Array.isArray(responseData.rows)) {
        rows = responseData.rows;
        totalRows = responseData.totalRows || rows.length;
        totalPages = responseData.totalPages || 1;
        currentPage = responseData.page || page;
        
        if (responseData.columns && Array.isArray(responseData.columns)) {
          columns = responseData.columns;
        } else if (rows.length > 0) {
          columns = Object.keys(rows[0]).map(key => ({ name: key, type: 'text', nullable: true }));
        }
      }
    } 
    // Handle direct response with rows and columns
    else if (response && response.rows && Array.isArray(response.rows)) {
      rows = response.rows;
      totalRows = response.totalRows || rows.length;
      totalPages = response.totalPages || 1;
      currentPage = response.page || page;
      
      if (response.columns && Array.isArray(response.columns)) {
        columns = response.columns;
      } else if (rows.length > 0) {
        columns = Object.keys(rows[0]).map(key => ({ name: key, type: 'text', nullable: true }));
      }
    }
    // Handle array response
    else if (response && Array.isArray(response)) {
      rows = response;
      totalRows = rows.length;
      totalPages = 1;
      currentPage = 1;
      
      if (rows.length > 0) {
        columns = Object.keys(rows[0]).map(key => ({ name: key, type: 'text', nullable: true }));
      }
    }
    
    // Final fallback - if we still have no columns but have rows
    if (columns.length === 0 && rows.length > 0) {
      columns = Object.keys(rows[0]).map(key => ({ name: key, type: 'text', nullable: true }));
    }

    console.log('Processed data:', { 
      rowsCount: rows.length, 
      columnsCount: columns.length,
      totalPages,
      totalRows,
      currentPage
    });

    const cacheKey = `data_${effectiveName}_${effectiveOwner || 'unknown'}_p${page}_s${pageSize}_${sortColumn || ''}_${sortDirection}`;
    objectCache.set(cacheKey, { 
      data: { rows, columns, page: currentPage, pageSize, totalPages, totalRows }, 
      timestamp: Date.now() 
    });

    setTabData(prev => ({
      ...prev,
      data: { 
        loading: false, 
        data: rows,
        columns: columns,
        page: currentPage,
        pageSize,
        totalPages,
        totalRows,
        sortColumn,
        sortDirection,
        error: null
      }
    }));

  } catch (err) {
    console.error('Error in loadData:', err);
    setTabData(prev => ({
      ...prev,
      data: { 
        loading: false, 
        data: null,
        error: err.message || 'Failed to load data',
        page: page,
        pageSize: pageSize
      }
    }));
  }
}, [authToken]);

  // Load Constraints (for Tables)
  const loadConstraints = useCallback(async (object, type, owner) => {
    if (!authToken || !object || !type) return;
    
    let effectiveType = type;
    let effectiveName = object.name;
    let effectiveOwner = owner;
    
    if (effectiveType !== 'TABLE') {
      setTabData(prev => ({
        ...prev,
        constraints: { loading: false, data: [] }
      }));
      return;
    }
    
    const cacheKey = `constraints_${effectiveName}_${effectiveOwner || 'unknown'}`;
    const cached = objectCache.get(cacheKey);
    
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      setTabData(prev => ({
        ...prev,
        constraints: { loading: false, data: cached.data }
      }));
      return;
    }
    
    setTabData(prev => ({
      ...prev,
      constraints: { loading: true, data: prev.constraints.data }
    }));
    
    try {
      const response = await getTableConstraints(authToken, {
        tableName: effectiveName,
        owner: effectiveOwner
      });
      
      const data = handlePostgreSQLSchemaBrowserResponse(response);
      
      objectCache.set(cacheKey, { data, timestamp: Date.now() });
      
      setTabData(prev => ({
        ...prev,
        constraints: { loading: false, data }
      }));
      
    } catch (err) {
      Logger.error('PostgreSQLSchemaBrowser', 'loadConstraints', 'Error loading constraints', err);
      setTabData(prev => ({
        ...prev,
        constraints: { loading: false, data: [] }
      }));
    }
  }, [authToken]);

  // Load DDL / Definition
  const loadDDL = useCallback(async (object, type, owner) => {
    if (!authToken || !object || !type) return;
    
    let effectiveType = type;
    let effectiveName = object.name;
    let effectiveOwner = owner;
    
    const cacheKey = `ddl_${effectiveType}_${effectiveOwner || 'unknown'}_${effectiveName}`;
    const cached = objectCache.get(cacheKey);
    
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      setTabData(prev => ({
        ...prev,
        ddl: { loading: false, data: cached.data },
        definition: { loading: false, data: cached.data }
      }));
      return;
    }
    
    setTabData(prev => ({
      ...prev,
      ddl: { loading: true, data: prev.ddl.data },
      definition: { loading: true, data: prev.definition.data }
    }));
    
    try {
      const response = await getObjectDDL(authToken, {
        objectType: effectiveType,
        objectName: effectiveName,
        owner: effectiveOwner
      });
      
      const data = extractDDL(response);
      
      objectCache.set(cacheKey, { data, timestamp: Date.now() });
      
      setTabData(prev => ({
        ...prev,
        ddl: { loading: false, data },
        definition: { loading: false, data }
      }));
      
    } catch (err) {
      Logger.error('PostgreSQLSchemaBrowser', 'loadDDL', 'Error loading DDL', err);
      setTabData(prev => ({
        ...prev,
        ddl: { loading: false, data: '' },
        definition: { loading: false, data: '' }
      }));
    }
  }, [authToken]);

  // Load Used By Data
  const loadUsedByData = useCallback(async (object, type, owner, page = 1, pageSize = 10) => {
    if (!authToken || !object || !type) return;
    
    setTabData(prev => ({
      ...prev,
      'used by': { ...prev['used by'], loading: true }
    }));
    
    try {
      let effectiveType = type;
      let effectiveName = object.name;
      let effectiveOwner = owner;
      
      const response = await getUsedByPaginated(authToken, {
        objectType: effectiveType,
        objectName: effectiveName,
        owner: effectiveOwner,
        page,
        pageSize
      });
      
      const responseData = response?.data || {};
      const items = responseData.items || [];
      const totalCount = responseData.totalCount || items.length || 0;
      const totalPages = Math.ceil(totalCount / pageSize) || 1;
      
      let summary = null;
      try {
        const summaryResponse = await getUsedBySummary(authToken, {
          objectType: effectiveType,
          objectName: effectiveName,
          owner: effectiveOwner
        });
        
        summary = extractUsedBySummary(summaryResponse);
      } catch (err) {
        console.error('Error loading used by summary:', err);
      }
      
      setTabData(prev => ({
        ...prev,
        'used by': {
          loading: false,
          items,
          totalCount,
          totalPages,
          page,
          pageSize,
          summary
        }
      }));
      
    } catch (err) {
      Logger.error('PostgreSQLSchemaBrowser', 'loadUsedByData', 'Error loading used by data', err);
      setTabData(prev => ({
        ...prev,
        'used by': { ...prev['used by'], loading: false, items: [], summary: null }
      }));
    }
  }, [authToken]);




// Use a ref to track if data is already loading to prevent loops
const isLoadingDataRef = useRef(false);

// Handle Tab Changes - Load data on demand
useEffect(() => {
  if (!activeObject) return;
  
  const objectType = activeObject.type?.toUpperCase();
  const owner = activeObject.owner;
  
  const hasDataForTab = () => {
    switch (activeTab) {
      case 'properties':
        return tabData.properties.data !== null && !tabData.properties.loading;
      case 'columns':
      case 'parameters':
      case 'attributes':
        return tabData.columns.data !== null && !tabData.columns.loading;
      case 'data':
        return tabData.data.data !== null && !tabData.data.loading;
      case 'constraints':
        return tabData.constraints.data !== null && !tabData.constraints.loading;
      case 'ddl':
      case 'definition':
      case 'spec':
      case 'body':
        return tabData.ddl.data !== '' && !tabData.ddl.loading;
      case 'used by':
        return tabData['used by'].items.length > 0 && !tabData['used by'].loading;
      default:
        return false;
    }
  };
  
  // Prevent loading if already loading or if we have data
  if (hasDataForTab() || isLoadingDataRef.current) {
    return;
  }
  
  isLoadingDataRef.current = true;
  
  const loadTabData = async () => {
    try {
      switch (activeTab) {
        case 'properties':
          await loadProperties(activeObject, objectType, owner);
          break;
        case 'columns':
        case 'parameters':
        case 'attributes':
          await loadColumns(activeObject, objectType, owner, 1);
          break;
        case 'data':
          // Auto-load data when table is selected
          await loadData(activeObject, objectType, owner, {
            page: tabData.data.page || 1,
            pageSize: tabData.data.pageSize || 15,
            sortColumn: tabData.data.sortColumn,
            sortDirection: tabData.data.sortDirection
          });
          break;
        case 'constraints':
          await loadConstraints(activeObject, objectType, owner);
          break;
        case 'ddl':
        case 'definition':
        case 'spec':
        case 'body':
          await loadDDL(activeObject, objectType, owner);
          break;
        case 'used by':
          await loadUsedByData(
            activeObject, 
            objectType, 
            owner, 
            tabData['used by'].page || 1, 
            tabData['used by'].pageSize || 10
          );
          break;
        default:
          break;
      }
    } catch (error) {
      console.error(`Error loading ${activeTab} data:`, error);
    } finally {
      isLoadingDataRef.current = false;
    }
  };
  
  loadTabData();
  // Only depend on activeObject and activeTab, not tabData
}, [activeObject, activeTab, loadProperties, loadColumns, loadData, loadConstraints, loadDDL, loadUsedByData]);

  // Handle page change for data tab
  const handleDataPageChange = useCallback((newPage) => {
    if (!activeObject) return;
    
    setTabData(prev => ({
      ...prev,
      data: { ...prev.data, page: newPage }
    }));
    
    loadData(activeObject, activeObject.type?.toUpperCase(), activeObject.owner, {
      page: newPage,
      pageSize: tabData.data.pageSize,
      sortColumn: tabData.data.sortColumn,
      sortDirection: tabData.data.sortDirection
    });
  }, [activeObject, loadData, tabData.data]);

  // Handle page size change for data tab
  const handleDataPageSizeChange = useCallback((newSize) => {
    if (!activeObject) return;
    
    setTabData(prev => ({
      ...prev,
      data: { ...prev.data, pageSize: newSize, page: 1 }
    }));
    
    loadData(activeObject, activeObject.type?.toUpperCase(), activeObject.owner, {
      page: 1,
      pageSize: newSize,
      sortColumn: tabData.data.sortColumn,
      sortDirection: tabData.data.sortDirection
    });
  }, [activeObject, loadData, tabData.data]);

  // Handle sort change for data tab
  const handleSortChange = useCallback((column, direction) => {
    if (!activeObject) return;
    
    setTabData(prev => ({
      ...prev,
      data: { ...prev.data, sortColumn: column, sortDirection: direction, page: 1 }
    }));
    
    loadData(activeObject, activeObject.type?.toUpperCase(), activeObject.owner, {
      page: 1,
      pageSize: tabData.data.pageSize,
      sortColumn: column,
      sortDirection: direction
    });
  }, [activeObject, loadData, tabData.data.pageSize]);

  // Handle page change for columns/parameters tab
  const handleColumnsPageChange = useCallback((newPage) => {
    if (!activeObject) return;
    
    loadColumns(activeObject, activeObject.type?.toUpperCase(), activeObject.owner, newPage);
  }, [activeObject, loadColumns]);

  // Handle page change for used by tab
  const handleUsedByPageChange = useCallback((newPage) => {
    if (!activeObject) return;
    
    loadUsedByData(activeObject, activeObject.type?.toUpperCase(), activeObject.owner, newPage, tabData['used by'].pageSize);
  }, [activeObject, loadUsedByData, tabData]);

  // Handle page size change for used by tab
  const handleUsedByPageSizeChange = useCallback((newSize) => {
    if (!activeObject) return;
    
    loadUsedByData(activeObject, activeObject.type?.toUpperCase(), activeObject.owner, 1, newSize);
  }, [activeObject, loadUsedByData]);

  // Handle copy to clipboard
  const handleCopyToClipboard = useCallback(async (text, label = 'content') => {
    try {
      await navigator.clipboard.writeText(text);
      Logger.info('PostgreSQLSchemaBrowser', 'handleCopyToClipboard', `Copied ${label} to clipboard`);
    } catch (error) {
      Logger.error('PostgreSQLSchemaBrowser', 'handleCopyToClipboard', `Failed to copy ${label}`, error);
    }
  }, []);

  // Handle object select
  const handleObjectSelect = useCallback(async (object, type) => {
    if (!authToken || !object) {
      console.error('Cannot select object: missing authToken or object', { authToken: !!authToken, object });
      return;
    }

    const selectionKey = `${object.owner || 'unknown'}_${object.name}_${type}`;
    
    if (selectingRef.current === selectionKey) {
      console.log('Already selecting this object, skipping duplicate call');
      return;
    }
    
    const currentActiveId = activeObject?.id;
    const newObjectId = object.id || `${object.owner || 'unknown'}_${object.name}`;
    
    if (currentActiveId === newObjectId && activeObject?.type === type) {
      console.log('Object already active, skipping selection');
      return;
    }
    
    selectingRef.current = selectionKey;
    
    Logger.info('PostgreSQLSchemaBrowser', 'handleObjectSelect', `Selecting ${object.name} (${type})`);
    
    const objectId = newObjectId;
    
    setTabData({
      properties: { loading: false, data: null },
      columns: { loading: false, data: null, page: 1, totalPages: 1 },
      data: { loading: false, data: null, page: 1, pageSize: 15, totalPages: 1, sortColumn: '', sortDirection: 'ASC' },
      parameters: { loading: false, data: null, page: 1, totalPages: 1 },
      constraints: { loading: false, data: null },
      ddl: { loading: false, data: '' },
      definition: { loading: false, data: '' },
      spec: { loading: false, data: '' },
      body: { loading: false, data: '' },
      attributes: { loading: false, data: null },
      'used by': { loading: false, items: [], totalCount: 0, totalPages: 1, page: 1, pageSize: 10, summary: null }
    });
    
    setLoadedTabs({});
    setSynonymInfo(null);
    
    const enrichedObject = {
      ...object,
      id: objectId,
      type: type?.toUpperCase(),
      isSynonym: false
    };
    
    setActiveObject(enrichedObject);
    setSelectedForApiGeneration(enrichedObject);
    setActiveTab('properties');
    setObType(type);

    const tabId = `${type}_${object.owner || 'unknown'}_${object.name}_${Date.now()}`;
    const existingTab = tabs.find(t => t.id === tabId || 
                                     (t.name === object.name && 
                                      t.owner === object.owner && 
                                      t.type === type));
    
    if (existingTab) {
      setTabs(tabs.map(t => ({ ...t, isActive: t.id === existingTab.id })));
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

    if (window.innerWidth < 768) {
      setIsLeftSidebarVisible(false);
    }
    
    setTimeout(() => {
      if (selectingRef.current === selectionKey) {
        selectingRef.current = null;
      }
    }, 1000);
  }, [authToken, tabs, activeObject]);

  // Render Properties Tab (simplified - no synonym handling)
  const renderPropertiesTab = () => {
  const data = tabData.properties.data;
  const loading = tabData.properties.loading;
  
  if (loading) {
    return <TabLoader colors={colors} message="Loading properties..." />;
  }
  
  if (!data) {
    return (
      <div className="flex-1 flex items-center justify-center p-4">
        <div className="text-center" style={{ color: colors.textSecondary }}>
          No properties available. Please wait while we load the object details.
        </div>
      </div>
    );
  }
  
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

  const renderPropertyItem = (label, value, isStatus = false) => (
    <div className="space-y-1">
      <div className="text-xs" style={{ color: colors.textSecondary }}>{label}</div>
      <div className="text-sm truncate" style={{ color: colors.text }}>
        {isStatus ? renderStatusBadge(value) : (value || '-')}
      </div>
    </div>
  );
  
  // Check if this is a synonym
  const isSynonym = data.isSynonym === true || 
                    data.objectType === 'SYNONYM' || 
                    data.type === 'SYNONYM' ||
                    activeObject?.isSynonym === true ||
                    (data.targetName && data.targetOwner && data.targetType);
  
  // Get the actual object type (resolve synonym if needed)
  const effectiveType = isSynonym && data.targetType 
    ? data.targetType 
    : (data.objectType || data.type || activeObject?.type);
  
  // For synonyms, show both synonym info AND target object info
  if (isSynonym && (data.targetName || data.targetName)) {
    const synonymInfo = {
      synonymName: data.synonymName || data.synonym_name || data.originalName || data.objectName || activeObject?.name,
      synonymOwner: data.synonymOwner || data.synonym_owner || data.owner || activeObject?.owner,
      targetName: data.targetName || data.target_name || data.TARGET_NAME,
      targetOwner: data.targetOwner || data.target_owner || data.TARGET_OWNER,
      targetType: data.targetType || data.target_type || data.TARGET_TYPE,
      targetStatus: data.targetStatus || data.target_status || data.TARGET_STATUS,
      dbLink: data.dbLink || data.db_link,
      valid: data.targetStatus === 'VALID' || data.targetStatus === 'ENABLED'
    };
    
    const targetType = synonymInfo.targetType || data.targetType || data.objectType;
    const targetName = synonymInfo.targetName || data.targetName || data.OBJECT_NAME;
    const targetOwner = synonymInfo.targetOwner || data.targetOwner || data.OWNER;
    const targetStatus = synonymInfo.targetStatus || data.targetStatus || data.STATUS;
    
    return (
      <div className="flex-1 overflow-auto p-4">
        <div className="space-y-4">
          {/* Synonym Information Section */}
          <div className="border rounded" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
            <div className="p-4 border-b" style={{ borderColor: colors.border }}>
              <h3 className="text-sm font-medium mb-3" style={{ color: colors.text }}>Synonym Information</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {renderPropertyItem("Synonym Name", synonymInfo.synonymName)}
                {renderPropertyItem("Synonym Owner", synonymInfo.synonymOwner)}
                {renderPropertyItem("DB Link", synonymInfo.dbLink || '-')}
                {renderPropertyItem("Created", data.CREATED ? formatDateForDisplay(data.CREATED) : (data.created ? formatDateForDisplay(data.created) : '-'))}
                {renderPropertyItem("Last Modified", data.LAST_DDL_TIME ? formatDateForDisplay(data.LAST_DDL_TIME) : (data.lastModified ? formatDateForDisplay(data.lastModified) : '-'))}
              </div>
            </div>
          </div>
          
          {/* Target Object Information Section */}
          <div className="border rounded" style={{ borderColor: colors.border, backgroundColor: colors.card }}>
            <div className="p-4 border-b" style={{ borderColor: colors.border }}>
              <h3 className="text-sm font-medium mb-3" style={{ color: colors.text }}>Target Object Information</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {renderPropertyItem("Target Type", targetType)}
                {renderPropertyItem("Target Name", targetName)}
                {renderPropertyItem("Target Owner", targetOwner)}
                {targetStatus && renderPropertyItem("Target Status", targetStatus, true)}
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
  
  // For regular objects (non-synonyms)
  const properties = [
    { label: 'Name', value: data.objectName || data.OBJECT_NAME || data.name || activeObject?.name },
    { label: 'Owner', value: data.owner || data.OWNER || activeObject?.owner },
    { label: 'Type', value: data.objectType || data.OBJECT_TYPE || data.type || activeObject?.type },
    { label: 'Status', value: data.status || data.STATUS || 'VALID', isStatus: true },
    { label: 'Created', value: data.CREATED ? formatDateForDisplay(data.CREATED) : (data.created ? formatDateForDisplay(data.created) : null) },
    { label: 'Last Modified', value: data.LAST_DDL_TIME ? formatDateForDisplay(data.LAST_DDL_TIME) : (data.lastModified ? formatDateForDisplay(data.lastModified) : null) },
  ].filter(p => p.value !== null && p.value !== undefined && p.value !== '');
  
  // Add additional properties based on object type
  if (effectiveType === 'TABLE') {
    if (data.columnCount !== undefined) {
      properties.push({ label: 'Column Count', value: data.columnCount });
    }
    if (data.indexCount !== undefined) {
      properties.push({ label: 'Index Count', value: data.indexCount });
    }
    if (data.triggerCount !== undefined) {
      properties.push({ label: 'Trigger Count', value: data.triggerCount });
    }
    if (data.dependencyCount !== undefined) {
      properties.push({ label: 'Dependencies', value: data.dependencyCount });
    }
    if (data.dependentCount !== undefined) {
      properties.push({ label: 'Used By Count', value: data.dependentCount });
    }
  } else if (effectiveType === 'PROCEDURE' || effectiveType === 'FUNCTION') {
    if (data.parameterCount !== undefined) {
      properties.push({ label: 'Parameters', value: data.parameterCount });
    }
    if (data.sourceLineCount !== undefined) {
      properties.push({ label: 'Source Lines', value: data.sourceLineCount });
    }
    if (data.returnType !== undefined) {
      properties.push({ label: 'Return Type', value: data.returnType });
    }
  }
  
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
        
        {/* Table Statistics for Regular Tables - UPDATED TO HANDLE YOUR PAYLOAD STRUCTURE */}
        {effectiveType === 'TABLE' && (data.statistics || data.sizeInfo) && (
          <div className="mt-4 pt-4 border-t" style={{ borderColor: colors.border }}>
            <h4 className="text-sm font-medium mb-3" style={{ color: colors.text }}>Table Statistics</h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {/* Row count from statistics */}
              {data.statistics?.estimated_row_count !== undefined && 
                renderPropertyItem("Estimated Rows", data.statistics.estimated_row_count)
              }
              
              {/* Page count */}
              {data.statistics?.page_count !== undefined && 
                renderPropertyItem("Page Count", data.statistics.page_count)
              }
              
              {/* Table size from sizeInfo */}
              {data.sizeInfo?.table_size && 
                renderPropertyItem("Table Size", data.sizeInfo.table_size)
              }
              
              {/* Total size */}
              {data.sizeInfo?.total_size && 
                renderPropertyItem("Total Size", data.sizeInfo.total_size)
              }
              
              {/* Indexes size */}
              {data.sizeInfo?.indexes_size && 
                renderPropertyItem("Indexes Size", data.sizeInfo.indexes_size)
              }
              
              {/* Toast size (PostgreSQL specific) */}
              {data.sizeInfo?.toast_size && 
                renderPropertyItem("TOAST Size", data.sizeInfo.toast_size)
              }
              
              {/* Live tuples */}
              {data.statistics?.live_tuples !== undefined && 
                renderPropertyItem("Live Tuples", data.statistics.live_tuples)
              }
              
              {/* Dead tuples */}
              {data.statistics?.dead_tuples !== undefined && 
                renderPropertyItem("Dead Tuples", data.statistics.dead_tuples)
              }
              
              {/* All visible pages */}
              {data.statistics?.all_visible_pages !== undefined && 
                renderPropertyItem("All Visible Pages", data.statistics.all_visible_pages)
              }
              
              {/* Last vacuum */}
              {data.statistics?.last_vacuum && 
                renderPropertyItem("Last Vacuum", formatDateForDisplay(data.statistics.last_vacuum))
              }
              
              {/* Last autovacuum */}
              {data.statistics?.last_autovacuum && 
                renderPropertyItem("Last Autovacuum", formatDateForDisplay(data.statistics.last_autovacuum))
              }
              
              {/* Last analyze */}
              {data.statistics?.last_analyze && 
                renderPropertyItem("Last Analyze", formatDateForDisplay(data.statistics.last_analyze))
              }
              
              {/* Last autoanalyze */}
              {data.statistics?.last_autoanalyze && 
                renderPropertyItem("Last Autoanalyze", formatDateForDisplay(data.statistics.last_autoanalyze))
              }
              
              {/* Modifications since analyze */}
              {data.statistics?.modifications_since_analyze !== undefined && 
                renderPropertyItem("Modifications Since Analyze", data.statistics.modifications_since_analyze)
              }
            </div>
          </div>
        )}
        
        {/* Table Properties for Regular Tables (if any) */}
        {effectiveType === 'TABLE' && data.tableInfo && (
          <div className="mt-4 pt-4 border-t" style={{ borderColor: colors.border }}>
            <h4 className="text-sm font-medium mb-3" style={{ color: colors.text }}>Table Properties</h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {renderPropertyItem("Tablespace", data.tableInfo.TABLESPACE_NAME)}
              {renderPropertyItem("Compression", data.tableInfo.COMPRESSION)}
              {renderPropertyItem("Row Movement", data.tableInfo.ROW_MOVEMENT)}
              {renderPropertyItem("Table Lock", data.tableInfo.TABLE_LOCK)}
              {renderPropertyItem("Dropped", data.tableInfo.DROPPED)}
            </div>
          </div>
        )}
        
        {/* Procedure/Function specific info */}
        {(effectiveType === 'PROCEDURE' || effectiveType === 'FUNCTION') && data.parameterCount !== undefined && (
          <div className="mt-4 pt-4 border-t" style={{ borderColor: colors.border }}>
            <h4 className="text-sm font-medium mb-3" style={{ color: colors.text }}>Code Information</h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {renderPropertyItem("Parameters", data.parameterCount)}
              {data.sourceLineCount !== undefined && renderPropertyItem("Source Lines", data.sourceLineCount)}
              {data.returnType !== undefined && renderPropertyItem("Return Type", data.returnType)}
            </div>
          </div>
        )}
        
        {/* View Definition for Regular Views */}
        {(effectiveType === 'VIEW' || data.objectType === 'VIEW') && data.viewInfo && (
          <div className="mt-4 pt-4 border-t" style={{ borderColor: colors.border }}>
            <h4 className="text-sm font-medium mb-2" style={{ color: colors.text }}>View Definition</h4>
            <div className="border rounded p-3" style={{ borderColor: colors.border, backgroundColor: colors.codeBg }}>
              <pre className="text-xs whitespace-pre-wrap font-mono" style={{ color: colors.text }}>
                {data.viewInfo.TEXT || 'No view definition available'}
              </pre>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

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
  
  if (searchAbortController.current) {
    searchAbortController.current.abort();
  }
  
  searchAbortController.current = new AbortController();
  
  if (ongoingRequests.has(requestKey)) {
    Logger.debug('PostgreSQLSchemaBrowser', 'searchObjects', `Already searching for "${searchTerm}", skipping`);
    return;
  }

  Logger.info('PostgreSQLSchemaBrowser', 'searchObjects', `Searching for "${searchTerm}" in ${owner === 'ALL' ? 'all schemas' : owner}`);

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

    const response = await searchObjectsPaginated(authToken, params, {
      signal: searchAbortController.current.signal
    });
    
    if (searchAbortController.current.signal.aborted) {
      throw new DOMException('Aborted', 'AbortError');
    }
    
    // Initialize grouped results with ALL possible types
    const groupedResults = {
      tables: [],
      procedures: [],
      views: [],
      functions: [],
      sequences: [],
      types: [],
      triggers: [],
      materializedViews: [],
      indexes: [],        // ADD THIS
      other: []           // ADD THIS for any other object types
    };

    let resultsArray = [];
    
    if (response?.data?.results && Array.isArray(response.data.results)) {
      resultsArray = response.data.results;
    } else if (response?.data?.items && Array.isArray(response.data.items)) {
      resultsArray = response.data.items;
    } else if (response?.results && Array.isArray(response.results)) {
      resultsArray = response.results;
    } else if (Array.isArray(response)) {
      resultsArray = response;
    } else if (response?.data && Array.isArray(response.data)) {
      resultsArray = response.data;
    }

    const objectsByName = new Map();

    resultsArray.forEach(item => {
      const objectType = (
        item.object_type || 
        item.type || 
        item.OBJECT_TYPE || 
        ''
      ).toUpperCase();
      
      const itemName = 
        item.name ||
        item.table_name ||
        item.procedure_name ||
        item.view_name ||
        item.function_name ||
        item.sequence_name ||
        item.type_name ||
        item.trigger_name;
      
      if (!itemName || !objectType) return;
      
      const objOwner = item.owner || item.OWNER;
      const key = `${itemName}_${objectType}`;
      
      if (objectsByName.has(key)) {
        const existing = objectsByName.get(key);
        const currentUserSchema = schemaInfo?.currentUser || schemaInfo?.currentSchema;
        const isCurrentUser = objOwner === currentUserSchema;
        const existingIsCurrentUser = existing.owner === currentUserSchema;
        
        if (isCurrentUser && !existingIsCurrentUser) {
          objectsByName.set(key, {
            ...item,
            name: itemName,
            owner: objOwner,
            type: objectType,
            status: item.status || item.STATUS || 'VALID',
            id: item.id || `${objOwner || 'unknown'}_${itemName}`
          });
        }
      } else {
        objectsByName.set(key, {
          ...item,
          name: itemName,
          owner: objOwner,
          type: objectType,
          status: item.status || item.STATUS || 'VALID',
          id: item.id || `${objOwner || 'unknown'}_${itemName}`
        });
      }
    });

    // Group the results by type
    for (const [key, normalizedObj] of objectsByName) {
      const objectType = normalizedObj.type;
      
      if (objectType.includes('TABLE')) {
        groupedResults.tables.push(normalizedObj);
      } else if (objectType.includes('PROCEDURE')) {
        groupedResults.procedures.push(normalizedObj);
      } else if (objectType.includes('VIEW')) {
        groupedResults.views.push(normalizedObj);
      } else if (objectType.includes('FUNCTION')) {
        groupedResults.functions.push(normalizedObj);
      } else if (objectType.includes('SEQUENCE')) {
        groupedResults.sequences.push(normalizedObj);
      } else if (objectType.includes('TYPE')) {
        groupedResults.types.push(normalizedObj);
      } else if (objectType.includes('TRIGGER')) {
        groupedResults.triggers.push(normalizedObj);
      } else if (objectType.includes('MATERIALIZED VIEW') || objectType.includes('MATERIALIZED_VIEW')) {
        groupedResults.materializedViews.push(normalizedObj);
      } else if (objectType.includes('INDEX')) {
        // ADD THIS: Handle INDEX objects
        groupedResults.indexes.push(normalizedObj);
      } else {
        // ADD THIS: Handle any other object types
        groupedResults.other.push(normalizedObj);
      }
    }
    
    if (searchAbortController.current?.signal.aborted) {
      return;
    }
    
    setFilteredResults(groupedResults);
    setSearchPerformed(true);
    
    // Update object tree expansion based on what has results
    setObjectTree(prev => ({
      ...prev,
      tables: groupedResults.tables.length > 0,
      procedures: groupedResults.procedures.length > 0,
      views: groupedResults.views.length > 0,
      functions: groupedResults.functions.length > 0,
      sequences: groupedResults.sequences.length > 0,
      types: groupedResults.types.length > 0,
      triggers: groupedResults.triggers.length > 0,
      materializedViews: groupedResults.materializedViews.length > 0
    }));

    const totalResults = Object.values(groupedResults).reduce((acc, curr) => acc + curr.length, 0);
    Logger.info('PostgreSQLSchemaBrowser', 'searchObjects', `Found ${totalResults} unique results`);

  } catch (err) {
    if (err.name === 'AbortError' || err.message === 'Aborted') {
      Logger.info('PostgreSQLSchemaBrowser', 'searchObjects', 'Search was cancelled');
      setSearchPerformed(false);
      return;
    }
    Logger.error('PostgreSQLSchemaBrowser', 'searchObjects', 'Error searching objects', err);
    setFilteredResults({});
    setSearchPerformed(false);
  } finally {
    setIsFiltering(false);
    ongoingRequests.delete(requestKey);
    searchAbortController.current = null;
  }
}, [authToken, schemaInfo]);

  // Handle filter changes
  const handleFilterChange = useCallback((value) => {
    setFilterQuery(value);
    setFilterSearchTerm(value);
  }, []);

  // Handle owner change
  const handleOwnerChange = useCallback((value) => {
    setSelectedOwner(value);
  }, []);

  // Reset to default state
  const resetToDefaultState = useCallback(() => {
    console.log('Resetting to default state');
    
    if (searchAbortController.current) {
      searchAbortController.current.abort();
      searchAbortController.current = null;
    }
    
    setIsFiltering(false);
    setFilteredResults({});
    setSearchPerformed(false);
    setFilterQuery('');
    setFilterSearchTerm('');
    setSelectedOwner('ALL');
    
    const requestKeys = Array.from(ongoingRequests.keys());
    requestKeys.forEach(key => {
      if (key.startsWith('search_')) {
        ongoingRequests.delete(key);
      }
    });
    
    setObjectTree({
      tables: true, // Keep tables expanded when resetting
      procedures: false,
      views: false,
      functions: false,
      sequences: false,
      types: false,
      triggers: false,
      materializedViews: false
    });
    
    Logger.info('PostgreSQLSchemaBrowser', 'resetToDefaultState', 'Reset to default state');
  }, []);

  const handleCancelSearch = resetToDefaultState;
  const handleClearFilters = resetToDefaultState;

  // Handle refresh
  const handleRefresh = useCallback(async () => {
    objectCache.clear();
    ongoingRequests.clear();
    setLoading(true);
    setInitialLoadComplete(false);
    setInitialized(false);
    setHasAutoSelected(false);
    setIsLoadingSchemaObjects(true);
    setIsInitialLoad(true);
    
    setLoadedSections({
      tables: false, // Tables first
      procedures: false,
      views: false,
      functions: false,
      sequences: false,
      types: false,
      triggers: false,
      materializedViews: false
    });
    setSchemaObjects({
      tables: [],
      tablesTotalCount: 0,
      procedures: [],
      proceduresTotalCount: 0,
      views: [],
      viewsTotalCount: 0,
      functions: [],
      functionsTotalCount: 0,
      sequences: [],
      sequencesTotalCount: 0,
      types: [],
      typesTotalCount: 0,
      triggers: [],
      triggersTotalCount: 0,
      materializedViews: [],
      materializedViewsTotalCount: 0
    });
    setPagination({
      tables: { page: 1, totalPages: 1, totalCount: 0 }, // Tables first
      procedures: { page: 1, totalPages: 1, totalCount: 0 },
      views: { page: 1, totalPages: 1, totalCount: 0 },
      functions: { page: 1, totalPages: 1, totalCount: 0 },
      sequences: { page: 1, totalPages: 1, totalCount: 0 },
      types: { page: 1, totalPages: 1, totalCount: 0 },
      triggers: { page: 1, totalPages: 1, totalCount: 0 },
      materializedViews: { page: 1, totalPages: 1, totalCount: 0 }
    });
    
    await loadInitialData();
    
    setTimeout(() => {
      loadRemainingData().catch(err => {
        Logger.error('PostgreSQLSchemaBrowser', 'handleRefresh', 'Error loading remaining data', err);
      });
    }, 300);
    
  }, [loadInitialData, loadRemainingData]);

  // Close context menu on outside click
  useEffect(() => {
    const handleClickOutside = () => setShowContextMenu(false);
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, []);

  // Get Tabs for Object Type
  const getTabsForObject = useCallback((type) => {
    const objectType = type?.toUpperCase();
    
    switch(objectType) {
      case 'TABLE':
        return ['Properties', 'Columns', 'Data', 'Constraints', 'DDL', 'Used By'];
      case 'VIEW':
        return ['Properties', 'Columns', 'Data', 'DDL', 'Used By'];
      case 'MATERIALIZED VIEW':
        return ['Properties', 'Columns', 'Data', 'DDL', 'Used By'];
      case 'PROCEDURE':
        return ['Properties', 'Parameters', 'DDL', 'Used By'];
      case 'FUNCTION':
        return ['Properties', 'Parameters', 'DDL', 'Used By'];
      case 'SEQUENCE':
        return ['Properties', 'DDL', 'Used By'];
      case 'TYPE':
        return ['Properties', 'Attributes', 'Used By'];
      case 'TRIGGER':
        return ['Properties', 'Definition', 'Used By'];
      default:
        return ['Properties', 'Used By'];
    }
  }, []);

  // Render Columns Tab
  const renderColumnsTab = () => {
    const data = tabData.columns.data;
    const loading = tabData.columns.loading;
    const page = tabData.columns.page;
    const totalPages = tabData.columns.totalPages;
    
    if (loading) {
      return <TabLoader colors={colors} message="Loading columns..." />;
    }
    
    if (!data || data.length === 0) {
      const objectType = activeObject?.type?.toUpperCase();
      const effectiveType = objectType;
      
      return (
        <div className="flex-1 overflow-auto p-4">
          <div className="text-center" style={{ color: colors.textSecondary }}>
            No {effectiveType === 'PROCEDURE' || effectiveType === 'FUNCTION' ? 'parameters' : 'columns'} found
          </div>
        </div>
      );
    }
    
    const objectType = activeObject?.type?.toUpperCase();
    const effectiveType = objectType;
    const isParameterMode = effectiveType === 'PROCEDURE' || effectiveType === 'FUNCTION';
    
    if (isParameterMode) {
      return (
        <div className="flex-1 overflow-auto">
          <div className="flex items-center justify-between p-3 border-b" style={{ borderColor: colors.border }}>
            <span className="text-sm font-medium" style={{ color: colors.text }}>
              Parameters ({data.length})
            </span>
            {totalPages > 1 && (
              <div className="flex items-center gap-2">
                <span className="text-xs" style={{ color: colors.textSecondary }}>
                  Page {page} of {totalPages}
                </span>
                <button
                  onClick={() => handleColumnsPageChange(page - 1)}
                  disabled={loading || page <= 1}
                  className="p-1 rounded hover:bg-opacity-50 disabled:opacity-50"
                  style={{ color: colors.text }}
                >
                  <ChevronLeft size={16} />
                </button>
                <button
                  onClick={() => handleColumnsPageChange(page + 1)}
                  disabled={loading || page >= totalPages}
                  className="p-1 rounded hover:bg-opacity-50 disabled:opacity-50"
                  style={{ color: colors.text }}
                >
                  <ChevronRight size={16} />
                </button>
              </div>
            )}
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
                {data
                  .sort((a, b) => {
                    const posA = a.POSITION !== undefined ? a.POSITION : 
                                (a.position !== undefined ? a.position : 999);
                    const posB = b.POSITION !== undefined ? b.POSITION : 
                                (b.position !== undefined ? b.position : 999);
                    return posA - posB;
                  })
                  .map((param, index) => {
                    const position = param.POSITION || param.position;
                    const name = param.argument_name || param.ARGUMENT_NAME || param.name;
                    const isFunction = effectiveType === 'FUNCTION';
                    const isReturn = isFunction && (position === 0 || (!name && position === undefined));
                    
                    let displayPosition;
                    if (isReturn) {
                      displayPosition = 1;
                    } else if (isFunction) {
                      displayPosition = position + 1;
                    } else {
                      displayPosition = position;
                    }
                    
                    const displayName = isReturn ? 'RETURN' : name;
                    const dataType = param.DATA_TYPE || param.data_type || param.type || 'VARCHAR';
                    const mode = param.IN_OUT || param.in_out || param.mode;
                    const displayMode = isReturn ? 'OUT' : mode || 'IN';
                    const dataLength = param.DATA_LENGTH || param.data_length || '-';
                    
                    return (
                      <tr key={`${displayName}-${index}-${position}`} style={{ 
                        backgroundColor: index % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                        borderBottom: `1px solid ${colors.gridBorder}`
                      }}>
                        <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>{displayPosition}</td>
                        <td className="p-2 text-xs font-medium" style={{ color: colors.text }}>{displayName}</td>
                        <td className="p-2 text-xs" style={{ color: colors.text }}>{dataType}</td>
                        <td className="p-2 text-xs">
                          <span className={`px-2 py-0.5 rounded text-xs ${
                            displayMode === 'IN' ? 'bg-blue-500/10 text-blue-400' :
                            displayMode === 'OUT' ? 'bg-purple-500/10 text-purple-400' :
                            'bg-green-500/10 text-green-400'
                          }`}>
                            {displayMode}
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
      );
    }
    
    // Regular columns display for tables/views
    return (
      <div className="flex-1 overflow-auto">
        <div className="flex items-center justify-between p-3 border-b" style={{ borderColor: colors.border }}>
          <span className="text-sm font-medium" style={{ color: colors.text }}>
            Columns ({data.length})
          </span>
          {totalPages > 1 && (
            <div className="flex items-center gap-2">
              <span className="text-xs" style={{ color: colors.textSecondary }}>
                Page {page} of {totalPages}
              </span>
              <button
                onClick={() => handleColumnsPageChange(page - 1)}
                disabled={loading || page <= 1}
                className="p-1 rounded hover:bg-opacity-50 disabled:opacity-50"
                style={{ color: colors.text }}
              >
                <ChevronLeft size={16} />
              </button>
              <button
                onClick={() => handleColumnsPageChange(page + 1)}
                disabled={loading || page >= totalPages}
                className="p-1 rounded hover:bg-opacity-50 disabled:opacity-50"
                style={{ color: colors.text }}
              >
                <ChevronRight size={16} />
              </button>
            </div>
          )}
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
              {data.map((col, i) => {
                const columnId = col.COLUMN_ID || col.column_id || i + 1;
                const columnName = col.COLUMN_NAME || col.column_name || col.name;
                const dataType = col.DATA_TYPE || col.data_type || 'VARCHAR';
                const dataLength = col.DATA_LENGTH || col.data_length;
                const dataPrecision = col.DATA_PRECISION || col.data_precision;
                const dataScale = col.DATA_SCALE || col.data_scale;
                const nullable = col.NULLABLE || col.nullable;
                const dataDefault = col.DATA_DEFAULT || col.data_default;
                
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
    );
  };

  // Render Data Tab
  const renderDataTab = () => {
    const rows = tabData.data?.data || [];
    const loading = tabData.data?.loading || false;
    const page = tabData.data?.page || 1;
    const pageSize = tabData.data?.pageSize || 15;
    const totalPages = tabData.data?.totalPages || 1;
    const totalRows = tabData.data?.totalRows || (Array.isArray(rows) ? rows.length : 0);
    const sortColumn = tabData.data?.sortColumn;
    const sortDirection = tabData.data?.sortDirection;
    
    const safeRows = Array.isArray(rows) ? rows : [];
    
    let columns = tabData.data?.columns || [];
    if (columns.length === 0 && safeRows.length > 0) {
      columns = Object.keys(safeRows[0]).map(key => ({ name: key }));
    }
    
    const objectType = activeObject?.type?.toUpperCase();
    const isView = objectType === 'VIEW' || objectType === 'MATERIALIZED VIEW';
    
    const downloadData = () => {
      if (safeRows.length === 0) return;
      
      const objectName = activeObject?.name;
      
      const headers = ['#', ...columns.map(col => col.name || col.COLUMN_NAME)];
      
      const csvRows = [
        headers.join(','),
        ...safeRows.map((row, index) => {
          const rowValues = [
            index + 1 + (page - 1) * pageSize,
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
              onClick={() => loadData(activeObject, activeObject.type?.toUpperCase(), activeObject.owner, {
                page,
                pageSize,
                sortColumn,
                sortDirection
              })}
              disabled={loading || !activeObject}
            >
              {loading ? (
                <Loader size={12} className="animate-spin" />
              ) : (
                <Play size={12} />
              )}
              <span>Execute</span>
            </button>
            <select 
              className="px-2 py-1 border rounded text-sm"
              style={{ backgroundColor: colors.bg, borderColor: colors.border, color: colors.text }}
              value={pageSize}
              onChange={(e) => handleDataPageSizeChange(parseInt(e.target.value))}
              disabled={loading}
            >
              <option value="15">15 rows</option>
              <option value="25">25 rows</option>
              <option value="50">50 rows</option>
              <option value="100">100 rows</option>
              <option value="250">250 rows</option>
              <option value="500">500 rows</option>
            </select>
            {safeRows.length > 0 && (
              <button
                className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2"
                style={{ backgroundColor: colors.bg, border: `1px solid ${colors.border}`, color: colors.text }}
                onClick={downloadData}
                disabled={loading}
              >
                <Download size={12} />
                <span>Download CSV</span>
              </button>
            )}
          </div>
          <div className="flex items-center gap-2">
            <span className="text-xs" style={{ color: colors.textSecondary }}>
              {totalRows > 0 ? (
                <>Page {page} of {totalPages} | Total: {totalRows.toLocaleString()} rows</>
              ) : (
                isView ? 'No view data' : 'No data'
              )}
            </span>
            {totalPages > 1 && (
              <div className="flex items-center gap-2">
                <button 
                  className="p-1 rounded hover:bg-opacity-50 disabled:opacity-50"
                  style={{ backgroundColor: colors.hover }}
                  onClick={() => handleDataPageChange(page - 1)}
                  disabled={loading || page <= 1}
                >
                  <ChevronLeft size={14} />
                </button>
                <button 
                  className="p-1 rounded hover:bg-opacity-50 disabled:opacity-50"
                  style={{ backgroundColor: colors.hover }}
                  onClick={() => handleDataPageChange(page + 1)}
                  disabled={loading || page >= totalPages}
                >
                  <ChevronRight size={14} />
                </button>
              </div>
            )}
          </div>
        </div>

        <div className="flex-1 relative" style={{ minHeight: 0, overflow: 'hidden' }}>
          {loading ? (
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="text-center">
                <Loader className="animate-spin mx-auto mb-4" size={40} style={{ color: colors.primary }} />
                <div className="text-sm font-medium" style={{ color: colors.text }}>
                  Loading {isView ? 'view' : 'table'} data...
                </div>
              </div>
            </div>
          ) : safeRows.length === 0 ? (
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
                        zIndex: 10,
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
                    {columns.map((col, colIndex) => (
                      <th 
                        key={col.name || col.COLUMN_NAME || `col-${colIndex}`} 
                        className="text-left p-2 text-xs font-medium cursor-pointer hover:bg-opacity-50"
                        onClick={() => handleSortChange(
                          col.name || col.COLUMN_NAME, 
                          sortColumn === (col.name || col.COLUMN_NAME) && sortDirection === 'ASC' ? 'DESC' : 'ASC'
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
                          {sortColumn === (col.name || col.COLUMN_NAME) && (
                            sortDirection === 'ASC' ? 
                              <ChevronUp size={10} /> : 
                              <ChevronDown size={10} />
                          )}
                        </div>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {safeRows.map((row, rowIndex) => (
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
                        {rowIndex + 1 + (page - 1) * pageSize}
                       </td>
                      {columns.map((col, colIndex) => {
                        const columnName = col.name || col.COLUMN_NAME;
                        const value = row[columnName];
                        return (
                          <td 
                            key={`${rowIndex}-${colIndex}`} 
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

  // Render Constraints Tab
  const renderConstraintsTab = () => {
    const data = tabData.constraints.data;
    const loading = tabData.constraints.loading;
    
    if (loading) {
      return <TabLoader colors={colors} message="Loading constraints..." />;
    }
    
    if (!data) {
      return (
        <div className="flex-1 overflow-auto p-4">
          <div className="text-center" style={{ color: colors.textSecondary }}>
            No constraints found for this object
          </div>
        </div>
      );
    }
    
    let constraintsArray = [];
    
    if (Array.isArray(data)) {
      constraintsArray = data;
    } else if (data.constraints && Array.isArray(data.constraints)) {
      constraintsArray = data.constraints;
    } else {
      return (
        <div className="flex-1 overflow-auto p-4">
          <div className="text-center" style={{ color: colors.textSecondary }}>
            Unexpected constraints data format
          </div>
        </div>
      );
    }
    
    if (constraintsArray.length === 0) {
      return (
        <div className="flex-1 overflow-auto p-4">
          <div className="text-center" style={{ color: colors.textSecondary }}>
            No constraints found for this object
          </div>
        </div>
      );
    }
    
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded" style={{ borderColor: colors.gridBorder, backgroundColor: colors.card }}>
          <div className="p-2 border-b" style={{ borderColor: colors.gridBorder }}>
            <div className="text-sm font-medium" style={{ color: colors.text }}>
              Constraints ({constraintsArray.length})
            </div>
          </div>
          <div className="overflow-auto">
            <table className="w-full">
              <thead style={{ backgroundColor: colors.tableHeader }}>
                <tr>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Name</th>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Type</th>
                  <th className="text-left p-2 text-xs" style={{ color: colors.textSecondary }}>Status</th>
                  <th className="text-left p-2 text-xs hidden md:table-cell" style={{ color: colors.textSecondary }}>Columns</th>
                  <th className="text-left p-2 text-xs hidden lg:table-cell" style={{ color: colors.textSecondary }}>Validated</th>
                </tr>
              </thead>
              <tbody>
                {constraintsArray.map((con, i) => {
                  const constraintName = con.name || '-';
                  const constraintType = con.type || '-';
                  const constraintStatus = con.status || '-';
                  const columns = con.columnsString || (Array.isArray(con.columns) ? con.columns.join(', ') : con.columns) || '-';
                  const validated = con.validated || '-';
                  
                  let typeDisplay = constraintType;
                  if (constraintType === 'C') typeDisplay = 'Check';
                  else if (constraintType === 'P') typeDisplay = 'Primary Key';
                  else if (constraintType === 'R') typeDisplay = 'Foreign Key';
                  else if (constraintType === 'U') typeDisplay = 'Unique';
                  
                  return (
                    <tr key={con.id || constraintName + i} style={{ 
                      backgroundColor: i % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                      borderBottom: `1px solid ${colors.gridBorder}`
                    }}>
                      <td className="p-2 text-xs" style={{ color: colors.text }}>{constraintName}</td>
                      <td className="p-2 text-xs">
                        <span className={`px-2 py-0.5 rounded text-xs ${
                          constraintType === 'P' ? 'bg-blue-500/10 text-blue-400' :
                          constraintType === 'R' ? 'bg-purple-500/10 text-purple-400' :
                          constraintType === 'U' ? 'bg-green-500/10 text-green-400' :
                          'bg-yellow-500/10 text-yellow-400'
                        }`}>
                          {typeDisplay}
                        </span>
                      </td>
                      <td className="p-2 text-xs">
                        <span className={`px-2 py-0.5 rounded text-xs ${
                          constraintStatus === 'ENABLED' ? 'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'
                        }`}>
                          {constraintStatus}
                        </span>
                      </td>
                      <td className="p-2 text-xs hidden md:table-cell" style={{ color: colors.textSecondary }}>
                        {columns}
                      </td>
                      <td className="p-2 text-xs hidden lg:table-cell" style={{ color: colors.textSecondary }}>
                        {validated}
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

  // Render DDL Tab
  const renderDDLTab = () => {
    const ddl = tabData.ddl.data || '';
    const loading = tabData.ddl.loading;
    
    const ddlLines = ddl ? ddl.split('\n') : [];
    
    if (loading) {
      return <TabLoader colors={colors} message="Loading DDL..." />;
    }
    
    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded p-4" style={{ borderColor: colors.border, backgroundColor: colors.codeBg }}>
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
              disabled={!ddl}
            >
              <Copy size={12} className="inline mr-1" />
              Copy
            </button>
          </div>
        </div>
      </div>
    );
  };

  // Render Definition Tab (alias for DDL)
  const renderDefinitionTab = () => renderDDLTab();

  // Render Spec Tab (not applicable for PostgreSQL, but keep for compatibility)
  const renderSpecTab = () => renderDDLTab();

  // Render Body Tab (not applicable for PostgreSQL, but keep for compatibility)
  const renderBodyTab = () => renderDDLTab();

  // Render Attributes Tab (for types)
  const renderAttributesTab = () => {
    const data = tabData.columns.data;
    const loading = tabData.columns.loading;
    
    if (loading) {
      return <TabLoader colors={colors} message="Loading attributes..." />;
    }
    
    if (!data || data.length === 0) {
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
              Attributes ({data.length})
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
                {data.map((attr, i) => (
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

  // Render Used By Tab
  const renderUsedByTab = () => {
    const usedByData = tabData['used by'];
    
    if (!activeObject) return null;
    
    const objectType = activeObject.type?.toUpperCase();
    const owner = activeObject.owner;
    
    return (
      <UsedByTab
        objectName={activeObject.name}
        objectType={objectType}
        owner={owner}
        colors={colors}
        onSelectObject={handleObjectSelect}
        onRefresh={() => loadUsedByData(
          activeObject,
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
  };

  // Render Tab Content
  const renderTabContent = () => {
    if (!activeObject) {
      return (
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
                Fetching tables and other database objects
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
      );
    }

    switch(activeTab.toLowerCase()) {
      case 'properties':
        return renderPropertiesTab();
      case 'columns':
      case 'parameters':
        return renderColumnsTab();
      case 'data':
        return renderDataTab();
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
        <span className="text-sm font-medium uppercase" style={{ color: colors.text }}>PostgreSQL Schema Browser</span>
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
          hasActiveFilter={hasActiveFilter}
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
                    if (tab.isActive) return;
                    
                    const parts = tab.id.split('_');
                    if (parts.length >= 3) {
                      const type = parts[0];
                      const owner = parts[1];
                      const name = parts.slice(2, -1).join('_');
                      
                      const objectToSelect = {
                        name: name,
                        owner: owner,
                        type: type,
                        id: `${owner}_${name}`
                      };
                      
                      await handleObjectSelect(objectToSelect, type);
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
                {tabData.properties.data?.status && (
                  <span className="text-xs px-2 py-0.5 rounded" style={{ 
                    backgroundColor: tabData.properties.data.status === 'VALID' ? `${colors.success}20` : `${colors.error}20`,
                    color: tabData.properties.data.status === 'VALID' ? colors.success : colors.error
                  }}>
                    {tabData.properties.data.status}
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
              {getTabsForObject(activeObject.type).map(tab => (
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
          selectedObject={selectedForApiGeneration}
          colors={colors}
          obType={obType}
          theme={theme}
          authToken={authToken}
          isEditing={false}
        />
      )}
    </div>
  );
};

export default PostgreSQLSchemaBrowser;