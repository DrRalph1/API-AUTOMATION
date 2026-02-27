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
  FileCode, ChevronsUp, ChevronsDown, AlertTriangle, Menu, Loader, Tag
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

// FilterInput Component - Fixed version
const FilterInput = React.memo(({ 
  filterQuery, 
  selectedOwner, 
  owners,
  onFilterChange,   // This prop name should match what's passed from parent
  onOwnerChange, 
  onClearFilters,
  colors,
  loading 
}) => {
  const searchInputRef = useRef(null);
  const [localFilterValue, setLocalFilterValue] = useState(filterQuery);
  const debounceTimerRef = useRef(null);

  // Update local value when prop changes
  useEffect(() => {
    setLocalFilterValue(filterQuery);
  }, [filterQuery]);

  const handleFilterChange = useCallback((e) => {
    const value = e.target.value;
    setLocalFilterValue(value);
    
    // Clear any existing timer
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }
    
    // Set new timer for debounced filter
    debounceTimerRef.current = setTimeout(() => {
      onFilterChange(value); // This calls the parent's handleFilterChange
    }, 300); // 300ms debounce
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
    onFilterChange('');  // Clear filter query
    onOwnerChange('ALL'); // Reset owner to ALL
    onClearFilters(); // This will trigger the parent's handleClearFilters which resets objectTree
    setTimeout(() => searchInputRef.current?.focus(), 10);
  }, [onFilterChange, onOwnerChange, onClearFilters]);

  // Cleanup timer on unmount
  useEffect(() => {
    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, []);

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
              value={localFilterValue}
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
            {localFilterValue && !loading && (
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
              <option value="ALL">All Schemas</option>
              {owners.map(owner => (
                <option key={owner} value={owner}>{owner}</option>
              ))}
            </select>
          )}
        </div>
      </div>

      {(localFilterValue || selectedOwner !== 'ALL') && !loading && (
        <div className="px-3 py-2 border-b" style={{ borderColor: colors.border, backgroundColor: colors.hover }}>
          <div className="flex items-center justify-between">
            <span className="text-xs" style={{ color: colors.textSecondary }}>
              Filtering {localFilterValue && `by: "${localFilterValue}"`} {localFilterValue && selectedOwner !== 'ALL' && ' • '} 
              {selectedOwner !== 'ALL' && `Owner: ${selectedOwner}`}
            </span>
            <button 
              onClick={handleClearAllFilters}
              className="text-xs px-2 py-1 rounded"
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

// ObjectTreeSection Component with proper count display
// ObjectTreeSection Component with proper count display and API search support
const ObjectTreeSection = React.memo(({ 
  title, 
  type, 
  objects,
  totalCount = 0, // This is the total count from database (or filtered count from API)
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
                      {/* {obj.owner && selectedOwner === 'ALL' && !hasActiveFilter && (
                        <span 
                          className="text-[10px] px-1.5 py-0.5 rounded-full ml-1 shrink-0"
                          style={{ 
                            backgroundColor: colors.border,
                            color: colors.textTertiary
                          }}
                        >
                          {obj.owner}
                        </span>
                      )} */}
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
              
              {/* Load More Button - only show when not filtering */}
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
          
          {/* Show filter hint when no results */}
          {/* {hasActiveFilter && filteredObjects.length === 0 && filterQuery && filterQuery.length >= 2 && (
            <div className="px-2 py-2 mt-1 text-xs text-center" style={{ color: colors.textTertiary }}>
              Try a different search term or clear filters
            </div>
          )} */}
        </div>
      )}
    </div>
  );
});

ObjectTreeSection.displayName = 'ObjectTreeSection';

// Update the LeftSidebar component props destructuring to include filteredResults and isFiltering
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
  handleLoadMore,
  schemaObjects,
  filteredResults,  // Make sure this is included
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
  isFiltering  // Make sure this is included
}) => {
  
  // Add debug logging
  console.log('LeftSidebar Debug:', {
    hasActiveFilter,
    isFiltering,
    filteredResults,
    filterQuery,
    totalResults: Object.values(filteredResults || {}).reduce((acc, curr) => acc + (curr?.length || 0), 0)
  });
  
  const handleCloseSidebar = useCallback(() => {
    setIsLeftSidebarVisible(false);
  }, [setIsLeftSidebarVisible]);

  // Helper function to filter objects - uses API results when filtering, otherwise client-side filter
  const filterObjects = useCallback((objects, type) => {
    // If we have filtered results from API and filter is active, use those
    if (hasActiveFilter && filteredResults && filteredResults[type]) {
      return filteredResults[type] || [];
    }
    
    // Otherwise, fall back to client-side filtering on already loaded objects
    if (!filterQuery && selectedOwner === 'ALL') {
      return objects || [];
    }
    
    const searchLower = filterQuery?.toLowerCase() || '';
    
    return (objects || []).filter(obj => {
      const ownerMatch = selectedOwner === 'ALL' || obj.owner === selectedOwner;
      if (!ownerMatch) return false;
      if (!filterQuery) return true;
      
      return (
        (obj.name && obj.name.toLowerCase().includes(searchLower)) ||
        (obj.owner && obj.owner.toLowerCase().includes(searchLower))
      );
    });
  }, [filterQuery, selectedOwner, hasActiveFilter, filteredResults]);

  // Helper function to get the appropriate count for display
  const getDisplayCount = useCallback((type, totalCount) => {
    if (hasActiveFilter && filteredResults && filteredResults[type]) {
      return filteredResults[type].length;
    }
    return totalCount;
  }, [hasActiveFilter, filteredResults]);

  // Show schema info in header
  const renderSchemaInfo = () => {
    if (!schemaInfo) return null;
    
    const currentSchema = schemaInfo.currentUser || schemaInfo.currentSchema || 'Not connected';
    
    return (
      <div className="px-3 py-2 border-b text-xs" style={{ borderColor: colors.border, backgroundColor: colors.hover }}>
        <div className="flex items-center gap-2">
          <Server size={12} style={{ color: colors.primary }} />
          <span className="truncate" style={{ color: colors.textSecondary }} title={currentSchema}>
            {currentSchema}
          </span>
        </div>
        {schemaInfo.databaseVersion && (
          <div className="flex items-center gap-2 mt-1">
            <Cpu size={12} style={{ color: colors.textTertiary }} />
            <span className="truncate text-[10px]" style={{ color: colors.textTertiary }}>
              {schemaInfo.databaseVersion.split(' ')[0]}
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
              Schema Browser
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
        colors={colors}
        loading={loading || isFiltering}
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
          {/* Procedures Section */}
          <ObjectTreeSection
            title="Procedures"
            type="procedures"
            objects={filterObjects(schemaObjects.procedures || [], 'procedures')}
            totalCount={getDisplayCount('procedures', schemaObjects.proceduresTotalCount || 0)}
            isLoading={loadingStates.procedures}
            isExpanded={objectTree.procedures}
            onToggle={handleToggleSection}
            onLoadSection={handleLoadSection}
            onLoadMore={handleLoadMore}
            activeObjectId={activeObject?.id}
            filterQuery={filterQuery}
            selectedOwner={selectedOwner}
            colors={colors}
            getObjectIcon={getObjectIcon}
            handleContextMenu={handleContextMenu}
            isLoaded={loadedSections.procedures}
            currentPage={pagination.procedures.page}
            totalPages={pagination.procedures.totalPages}
            onSelectObject={handleObjectSelect}
            hasActiveFilter={hasActiveFilter}
            isFiltering={isFiltering}
          />

          {/* Views Section */}
          <ObjectTreeSection
            title="Views"
            type="views"
            objects={filterObjects(schemaObjects.views || [], 'views')}
            totalCount={getDisplayCount('views', schemaObjects.viewsTotalCount || 0)}
            isLoading={loadingStates.views}
            isExpanded={objectTree.views}
            onToggle={handleToggleSection}
            onLoadSection={handleLoadSection}
            onLoadMore={handleLoadMore}
            activeObjectId={activeObject?.id}
            filterQuery={filterQuery}
            selectedOwner={selectedOwner}
            colors={colors}
            getObjectIcon={getObjectIcon}
            handleContextMenu={handleContextMenu}
            isLoaded={loadedSections.views}
            currentPage={pagination.views.page}
            totalPages={pagination.views.totalPages}
            onSelectObject={handleObjectSelect}
            hasActiveFilter={hasActiveFilter}
            isFiltering={isFiltering}
          />

          {/* Functions Section */}
          <ObjectTreeSection
            title="Functions"
            type="functions"
            objects={filterObjects(schemaObjects.functions || [], 'functions')}
            totalCount={getDisplayCount('functions', schemaObjects.functionsTotalCount || 0)}
            isLoading={loadingStates.functions}
            isExpanded={objectTree.functions}
            onToggle={handleToggleSection}
            onLoadSection={handleLoadSection}
            onLoadMore={handleLoadMore}
            activeObjectId={activeObject?.id}
            filterQuery={filterQuery}
            selectedOwner={selectedOwner}
            colors={colors}
            getObjectIcon={getObjectIcon}
            handleContextMenu={handleContextMenu}
            isLoaded={loadedSections.functions}
            currentPage={pagination.functions.page}
            totalPages={pagination.functions.totalPages}
            onSelectObject={handleObjectSelect}
            hasActiveFilter={hasActiveFilter}
            isFiltering={isFiltering}
          />

          {/* Packages Section */}
          <ObjectTreeSection
            title="Packages"
            type="packages"
            objects={filterObjects(schemaObjects.packages || [], 'packages')}
            totalCount={getDisplayCount('packages', schemaObjects.packagesTotalCount || 0)}
            isLoading={loadingStates.packages}
            isExpanded={objectTree.packages}
            onToggle={handleToggleSection}
            onLoadSection={handleLoadSection}
            onLoadMore={handleLoadMore}
            activeObjectId={activeObject?.id}
            filterQuery={filterQuery}
            selectedOwner={selectedOwner}
            colors={colors}
            getObjectIcon={getObjectIcon}
            handleContextMenu={handleContextMenu}
            isLoaded={loadedSections.packages}
            currentPage={pagination.packages.page}
            totalPages={pagination.packages.totalPages}
            onSelectObject={handleObjectSelect}
            hasActiveFilter={hasActiveFilter}
            isFiltering={isFiltering}
          />

          {/* Tables Section */}
          <ObjectTreeSection
            title="Tables"
            type="tables"
            objects={filterObjects(schemaObjects.tables || [], 'tables')}
            totalCount={getDisplayCount('tables', schemaObjects.tablesTotalCount || 0)}
            isLoading={loadingStates.tables}
            isExpanded={objectTree.tables}
            onToggle={handleToggleSection}
            onLoadSection={handleLoadSection}
            onLoadMore={handleLoadMore}
            activeObjectId={activeObject?.id}
            filterQuery={filterQuery}
            selectedOwner={selectedOwner}
            colors={colors}
            getObjectIcon={getObjectIcon}
            handleContextMenu={handleContextMenu}
            isLoaded={loadedSections.tables}
            currentPage={pagination.tables.page}
            totalPages={pagination.tables.totalPages}
            onSelectObject={handleObjectSelect}
            hasActiveFilter={hasActiveFilter}
            isFiltering={isFiltering}
          />

          {/* Sequences Section */}
          <ObjectTreeSection
            title="Sequences"
            type="sequences"
            objects={filterObjects(schemaObjects.sequences || [], 'sequences')}
            totalCount={getDisplayCount('sequences', schemaObjects.sequencesTotalCount || 0)}
            isLoading={loadingStates.sequences}
            isExpanded={objectTree.sequences}
            onToggle={handleToggleSection}
            onLoadSection={handleLoadSection}
            onLoadMore={handleLoadMore}
            activeObjectId={activeObject?.id}
            filterQuery={filterQuery}
            selectedOwner={selectedOwner}
            colors={colors}
            getObjectIcon={getObjectIcon}
            handleContextMenu={handleContextMenu}
            isLoaded={loadedSections.sequences}
            currentPage={pagination.sequences.page}
            totalPages={pagination.sequences.totalPages}
            onSelectObject={handleObjectSelect}
            hasActiveFilter={hasActiveFilter}
            isFiltering={isFiltering}
          />

          {/* Synonyms Section */}
          {/* <ObjectTreeSection
            title="Synonyms"
            type="synonyms"
            objects={filterObjects(schemaObjects.synonyms || [], 'synonyms')}
            totalCount={getDisplayCount('synonyms', schemaObjects.synonymsTotalCount || 0)}
            isLoading={loadingStates.synonyms}
            isExpanded={objectTree.synonyms}
            onToggle={handleToggleSection}
            onLoadSection={handleLoadSection}
            onLoadMore={handleLoadMore}
            activeObjectId={activeObject?.id}
            filterQuery={filterQuery}
            selectedOwner={selectedOwner}
            colors={colors}
            getObjectIcon={() => getObjectIcon('synonym')}
            handleContextMenu={handleContextMenu}
            isLoaded={loadedSections.synonyms}
            currentPage={pagination.synonyms.page}
            totalPages={pagination.synonyms.totalPages}
            onSelectObject={handleObjectSelect}
            hasActiveFilter={hasActiveFilter}
            isFiltering={isFiltering}
          /> */}

          {/* Types Section */}
          <ObjectTreeSection
            title="Types"
            type="types"
            objects={filterObjects(schemaObjects.types || [], 'types')}
            totalCount={getDisplayCount('types', schemaObjects.typesTotalCount || 0)}
            isLoading={loadingStates.types}
            isExpanded={objectTree.types}
            onToggle={handleToggleSection}
            onLoadSection={handleLoadSection}
            onLoadMore={handleLoadMore}
            activeObjectId={activeObject?.id}
            filterQuery={filterQuery}
            selectedOwner={selectedOwner}
            colors={colors}
            getObjectIcon={getObjectIcon}
            handleContextMenu={handleContextMenu}
            isLoaded={loadedSections.types}
            currentPage={pagination.types.page}
            totalPages={pagination.types.totalPages}
            onSelectObject={handleObjectSelect}
            hasActiveFilter={hasActiveFilter}
            isFiltering={isFiltering}
          />

          {/* Triggers Section */}
          <ObjectTreeSection
            title="Triggers"
            type="triggers"
            objects={filterObjects(schemaObjects.triggers || [], 'triggers')}
            totalCount={getDisplayCount('triggers', schemaObjects.triggersTotalCount || 0)}
            isLoading={loadingStates.triggers}
            isExpanded={objectTree.triggers}
            onToggle={handleToggleSection}
            onLoadSection={handleLoadSection}
            onLoadMore={handleLoadMore}
            activeObjectId={activeObject?.id}
            filterQuery={filterQuery}
            selectedOwner={selectedOwner}
            colors={colors}
            getObjectIcon={getObjectIcon}
            handleContextMenu={handleContextMenu}
            isLoaded={loadedSections.triggers}
            currentPage={pagination.triggers.page}
            totalPages={pagination.triggers.totalPages}
            onSelectObject={handleObjectSelect}
            hasActiveFilter={hasActiveFilter}
            isFiltering={isFiltering}
          />
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
            {(
              (schemaObjects.proceduresTotalCount || 0) +
              (schemaObjects.viewsTotalCount || 0) +
              (schemaObjects.functionsTotalCount || 0) +
              (schemaObjects.tablesTotalCount || 0) +
              (schemaObjects.packagesTotalCount || 0) +
              (schemaObjects.sequencesTotalCount || 0) +
              (schemaObjects.synonymsTotalCount || 0) +
              (schemaObjects.typesTotalCount || 0) +
              (schemaObjects.triggersTotalCount || 0)
            ).toLocaleString()}
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

  // New state to track if filter is active
  const [hasActiveFilter, setHasActiveFilter] = useState(false);

  const [obType, setObType] = useState("");

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

  // Add these new state variables
  const [filteredResults, setFilteredResults] = useState({});
  const [isFiltering, setIsFiltering] = useState(false);
  const [filterSearchTerm, setFilterSearchTerm] = useState('');

  // New state for tracking loaded sections
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
    procedures: true, // Only procedures expanded by default
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
    pageSize: 10,
    sortColumn: '',
    sortDirection: 'ASC'
  });

  // Update hasActiveFilter when filter changes
  useEffect(() => {
    setHasActiveFilter(!!filterQuery || selectedOwner !== 'ALL');
  }, [filterQuery, selectedOwner]);

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
    
    // CASE 1: Response has data.items (transformed paginated structure)
    if (response.data && response.data.items && Array.isArray(response.data.items)) {
      items = response.data.items;
      totalCount = response.data.totalCount || response.totalCount || items.length;
      totalPages = response.data.totalPages || response.pagination?.totalPages || 1;
      page = response.data.page || response.pagination?.page || 1;
    }
    
    // CASE 2: Response has data array directly
    else if (response.data && Array.isArray(response.data)) {
      items = response.data;
      totalCount = response.totalCount || items.length;
      totalPages = response.pagination?.totalPages || 
                  Math.ceil((response.totalCount || items.length) / (response.pagination?.pageSize || 50)) || 1;
      page = response.pagination?.page || 1;
    }
    
    // CASE 3: Response has data.paginatedItems structure
    else if (response.data && response.data.paginatedItems && Array.isArray(response.data.paginatedItems)) {
      items = response.data.paginatedItems;
      totalCount = response.data.totalCount || response.totalCount || items.length;
      totalPages = response.data.totalPages || response.pagination?.totalPages || 1;
      page = response.data.page || response.pagination?.page || 1;
    }
    
    // CASE 4: Response has an 'items' property
    else if (response.items && Array.isArray(response.items)) {
      items = response.items;
      totalCount = response.totalCount || items.length;
      totalPages = response.totalPages || 1;
      page = response.page || 1;
    }
    
    // CASE 5: Response itself is an array
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

  // Load schema info - only once at initialization
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

  // Optimized function to load only procedures first (since it's expanded by default)
  const loadInitialData = useCallback(async () => {
    if (!authToken) return;
    
    Logger.info('SchemaBrowser', 'loadInitialData', 'Loading procedures first');
    
    // Track owners locally
    let updatedOwners = new Set(owners);
    
    try {
      // Check cache first
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
        
        // Update owners from cached data
        cached.data.items.forEach(obj => {
          if (obj.owner) updatedOwners.add(obj.owner);
        });
      } else {
        // Load from API
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
        
        // Cache the result
        objectCache.set(cacheKey, { 
          data: { items, totalCount, totalPages, page: 1 }, 
          timestamp: Date.now() 
        });
        
        // Update owners
        items.forEach(obj => {
          if (obj.owner) updatedOwners.add(obj.owner);
        });
        
        setLoadingStates(prev => ({ ...prev, procedures: false }));
      }
      
      // Load schema info only once
      if (!schemaInfo) {
        await loadSchemaInfo();
      }
      
      // Update owners
      setOwners(Array.from(updatedOwners).sort());
      
      // Mark initial load as complete
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

  // Load remaining object types in background
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
    
    // Load in parallel with lower priority
    const loadPromises = remainingTypes.map(async ({ type, fetcher }) => {
      // Small delay to not block UI
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
        
        // Update owners
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
    
    // Show loading state while synonyms and other objects are loading
    setIsLoadingSchemaObjects(true);
    
    await Promise.all(loadPromises);
    
    // Hide loading state when done
    setIsLoadingSchemaObjects(false);
    
    Logger.info('SchemaBrowser', 'loadRemainingData', 'All remaining data loaded');
    
  }, [authToken]);

  // Initialize component
  useEffect(() => {
    let isMounted = true;
    
    const initialize = async () => {
      if (!authToken || initialized) return;
      
      Logger.info('SchemaBrowser', 'initialize', 'Starting initialization');
      
      // Load initial data first (procedures)
      await loadInitialData();
      
      if (isMounted) {
        setInitialized(true);
        
        // Load remaining data in background
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

  // Load more objects for a specific type
  const loadObjectType = useCallback(async (type, page = 1, pageSize = 10) => {
    if (!authToken) return;
    
    // Check if already loading this type
    const requestKey = `${type}_page_${page}`;
    if (loadingStates[type] || ongoingRequests.has(requestKey)) {
      Logger.debug('SchemaBrowser', 'loadObjectType', `Already loading ${type} page ${page}, skipping`);
      return;
    }
    
    Logger.info('SchemaBrowser', 'loadObjectType', `Loading ${type} page ${page} from API`);
    
    // Mark as loading
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
      
      // Extract items
      const { items, totalCount, page: currentPage, totalPages } = extractItemsFromResponse(response);
      
      Logger.info('SchemaBrowser', 'loadObjectType', `Loaded ${items.length} ${type} (page ${page})`);
      
      // Update state - append for page > 1
      setSchemaObjects(prev => {
        if (page === 1) {
          return { 
            ...prev, 
            [type]: items,
            [`${type}TotalCount`]: totalCount 
          };
        } else {
          const existing = prev[type] || [];
          // Avoid duplicates
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
      
      // Update pagination
      setPagination(prev => ({
        ...prev,
        [type]: {
          page: currentPage,
          totalPages: totalPages,
          totalCount: totalCount
        }
      }));
      
      // Update cache
      const cacheKey = `${type}_page${page}_${authToken.substring(0, 10)}`;
      objectCache.set(cacheKey, { 
        data: items, 
        totalCount,
        page: currentPage,
        totalPages,
        timestamp: Date.now() 
      });
      
      setLoadedSections(prev => ({ ...prev, [type]: true }));
      
      // Collect owners
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

  // Handle context menu
  const handleContextMenu = useCallback((e, object, type) => {
    e.preventDefault();
    e.stopPropagation();
    
    setContextObject({ ...object, type });
    setContextMenuPosition({ x: e.clientX, y: e.clientY });
    setShowContextMenu(true);
  }, []);

// Search objects via API when filter is active
const searchObjects = useCallback(async (searchTerm, owner) => {
  if (!authToken || !searchTerm || searchTerm.length < 2) {
    setFilteredResults({});
    return;
  }

  const requestKey = `search_${searchTerm}_${owner}`;
  
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

    const response = await searchObjectsPaginated(authToken, params);
    
    console.log('Search API Response:', response);
    
    // Group results by object type
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

    // Get items from the response - handle both transformed and raw formats
    let resultsArray = [];
    
    if (response?.data?.items && Array.isArray(response.data.items)) {
      // Already transformed by the controller
      resultsArray = response.data.items;
    } else if (response?.data?.results && Array.isArray(response.data.results)) {
      // Raw API format
      resultsArray = response.data.results;
    } else if (response?.results && Array.isArray(response.results)) {
      resultsArray = response.results;
    } else if (Array.isArray(response)) {
      resultsArray = response;
    } else if (response?.data && Array.isArray(response.data)) {
      resultsArray = response.data;
    }

    console.log('Results array:', resultsArray);

    // Process each result
    resultsArray.forEach(item => {
      // Determine object type - check all possible fields
      const objectType = (
        item.object_type || 
        item.type || 
        item.OBJECT_TYPE || 
        ''
      ).toUpperCase();
      
      // Determine name - check all possible fields
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
      
      // Skip if no name or type
      if (!itemName || !objectType) return;
      
      // Create normalized object
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
      
      // Map to internal types
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

    console.log('Grouped results:', groupedResults);
    
    setFilteredResults(groupedResults);
    
    // Auto-expand sections with results
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
    Logger.error('SchemaBrowser', 'searchObjects', 'Error searching objects', err);
    setFilteredResults({});
  } finally {
    setIsFiltering(false);
    ongoingRequests.delete(requestKey);
  }
}, [authToken]);

  // Handle filter changes - now triggers API search
const handleFilterChange = useCallback((value) => {
  setFilterQuery(value);
  setFilterSearchTerm(value);
  
  // The actual search will be triggered by the debounced function in FilterInput
  // We don't need to call searchObjects here directly anymore
}, []);

// But we need to call searchObjects when the debounced value changes
// Add this useEffect to trigger search when filterQuery changes with debouncing
useEffect(() => {
  const timer = setTimeout(() => {
    if (filterQuery && filterQuery.length >= 2) {
      searchObjects(filterQuery, selectedOwner);
    } else if (filterQuery && filterQuery.length < 2) {
      // Clear results if search term is too short
      setFilteredResults({});
    } else if (!filterQuery) {
      // Clear results if search term is empty
      setFilteredResults({});
    }
  }, 500); // 1000ms debounce

  return () => clearTimeout(timer);
}, [filterQuery, selectedOwner, searchObjects]);

  const handleOwnerChange = useCallback((value) => {
  setSelectedOwner(value);
  
  // Re-run search if there's an active filter
  if (filterQuery && filterQuery.length >= 2) {
    searchObjects(filterQuery, value);
  }
}, [filterQuery, searchObjects]);

  // Handle clear filters
  const handleClearFilters = useCallback(() => {
    setFilterQuery('');
    setSelectedOwner('ALL');
    setFilteredResults({});
    setFilterSearchTerm('');
    
    // Collapse all sections except procedures (just like on page load)
    setObjectTree({
      procedures: true,  // Keep procedures expanded
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
    
    // Load initial data
    await loadInitialData();
    
    // Load remaining data in background
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

  // Auto-select first object - only runs once after initial load
  useEffect(() => {
    if (!initialLoadComplete || hasAutoSelected || activeObject) return;
    
    // First, check for actual procedures
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
    
    // If no actual procedures, check synonyms for ones targeting procedures
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
    if (activeObject?.type === 'TABLE' || (activeObject?.type === 'SYNONYM' && objectDetails?.TARGET_TYPE === 'TABLE')) {
      const tableName = activeObject.type === 'SYNONYM' && objectDetails?.TARGET_NAME 
        ? objectDetails.TARGET_NAME 
        : activeObject.name;
      loadTableData(tableName);
    }
  }, [dataView.page, dataView.pageSize, dataView.sortColumn, dataView.sortDirection, activeObject, objectDetails, loadTableData]);

  // Close context menu on outside click
  useEffect(() => {
    const handleClickOutside = () => setShowContextMenu(false);
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, []);

  // Get Tabs for Object Type
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
        return ['Properties'];
      case 'TYPE':
        return ['Attributes', 'Properties'];
      case 'TRIGGER':
        return ['Definition', 'Properties'];
      default:
        return ['Properties'];
    }
  }, []);

  // Handle Object Select
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
  setSelectedForApiGeneration(enrichedObject);
  
  const tabId = `${type}_${objectId}`;
  const existingTab = tabs.find(t => t.id === tabId);
  
  if (existingTab) {
    setTabs(tabs.map(t => ({ ...t, isActive: t.id === tabId })));
  } else {
    setTabs(prev => [...prev.slice(-4), {
      id: tabId,
      name: object.name,
      type,
      objectId: objectId,
      isActive: true
    }].map(t => ({ ...t, isActive: t.id === tabId })));
  }

  try {
    Logger.debug('SchemaBrowser', 'handleObjectSelect', `Loading details for ${object.name}`);
    
    // Check if this object is actually a synonym
    const isSynonym = 
      object.objectType === 'SYNONYM' || 
      object.type === 'SYNONYM' ||
      object.isSynonym === true ||
      object.synonym_name === object.name; // Some APIs return synonym_name
    
    // Determine the correct object type for the API call
    let apiObjectType;
    let apiObjectName = object.name;
    
    if (isSynonym) {
      // If it's a synonym, pass SYNONYM as the type to get synonym details
      apiObjectType = 'SYNONYM';
      Logger.debug('SchemaBrowser', 'handleObjectSelect', `Object is a synonym, using type: SYNONYM`);
    } else {
      // For non-synonym objects, pass the original type
      apiObjectType = type;
    }

    setObType(apiObjectType);
    
    const response = await getObjectDetails(authToken, { 
      objectType: apiObjectType, 
      objectName: apiObjectName 
    });
    
    const processedResponse = handleSchemaBrowserResponse(response);
    const responseData = processedResponse.data || processedResponse;
    
    const enrichedResponseData = {
      ...responseData,
      name: responseData.name || object.name,
      type: responseData.type || type,
      isSynonym: isSynonym // Add this flag for reference
    };
    
    setObjectDetails(enrichedResponseData);
    
    const upperType = type.toUpperCase();
    let effectiveType = upperType;
    let targetType = null;
    let targetName = object.name;
    let targetOwner = null;
    
    if (isSynonym && responseData?.targetDetails) {
      targetType = responseData.targetDetails.OBJECT_TYPE || responseData.targetDetails.objectType;
      targetName = responseData.TARGET_NAME || object.name;
      targetOwner = responseData.TARGET_OWNER || responseData.targetDetails.OWNER;
      
      if (targetType) {
        effectiveType = targetType;
      }
    }
    
    if (effectiveType === 'TABLE') {
      if (isSynonym && targetType === 'TABLE') {
        loadTableData(targetName).catch(err => console.error('Background table load error:', err));
      } else {
        loadTableData(object.name).catch(err => console.error('Background table load error:', err));
      }
    }
    
    const ddlTypes = ['TABLE', 'VIEW', 'PROCEDURE', 'FUNCTION', 'PACKAGE', 'TRIGGER', 'SEQUENCE'];
    if (ddlTypes.includes(effectiveType)) {
      (async () => {
        try {
          setDdlLoading(true);
          
          // For DDL, we need to pass the target object type if it's a synonym
          let ddlObjectType = effectiveType.toLowerCase();
          let ddlObjectName = targetName || object.name;
          
          // If it's a synonym, we want the DDL of the target object
          if (isSynonym && targetType) {
            ddlObjectType = targetType.toLowerCase();
            ddlObjectName = targetName;
          }
          
          const ddlResponse = await getObjectDDL(authToken, { 
            objectType: ddlObjectType, 
            objectName: ddlObjectName 
          });
          
          if (ddlResponse && ddlResponse.data) {
            const ddlData = ddlResponse.data;
            let ddlText = '';
            
            if (typeof ddlData === 'string') {
              ddlText = ddlData;
            } else if (ddlData.ddl) {
              ddlText = ddlData.ddl;
            } else if (ddlData.text) {
              ddlText = ddlData.text;
            } else if (ddlData.sql) {
              ddlText = ddlData.sql;
            } else {
              ddlText = JSON.stringify(ddlData, null, 2);
            }
            
            if (ddlText && ddlText !== '{}') {
              setObjectDDL(ddlText);
            } else {
              setObjectDDL(`-- No DDL available for ${effectiveType} ${targetName || object.name}`);
            }
          } else {
            setObjectDDL(`-- No DDL available for ${effectiveType} ${targetName || object.name}`);
          }
        } catch (ddlError) {
          console.error('Background DDL fetch error:', ddlError);
          setObjectDDL(`-- Error loading DDL for ${effectiveType} ${targetName || object.name}\n-- ${ddlError.message}`);
        } finally {
          setDdlLoading(false);
        }
      })();
    }
    
  } catch (err) {
    Logger.error('SchemaBrowser', 'handleObjectSelect', `Error loading details for ${object.name}`, err);
    setError(`Failed to load object details: ${err.message}`);
  } finally {
    setLoading(false);
  }

  if (window.innerWidth < 768) {
    setIsLeftSidebarVisible(false);
  }
}, [authToken, tabs, loadTableData]);

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
      link.setAttribute('download', `${activeObject?.name || 'table'}_data.csv`);
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
                  const tableName = activeObject.type === 'SYNONYM' && objectDetails?.TARGET_NAME 
                    ? objectDetails.TARGET_NAME 
                    : activeObject.name;
                  await loadTableData(tableName);
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
                'No data'
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
                <div className="text-sm font-medium" style={{ color: colors.text }}>Loading table data...</div>
                <div className="text-xs mt-2" style={{ color: colors.textSecondary }}>Please wait while we fetch the rows</div>
              </div>
            </div>
          ) : data.length === 0 ? (
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="text-center">
                <div className="flex justify-center mb-4">
                  <TableIcon size={64} style={{ color: colors.textSecondary, opacity: 0.5 }} />
                </div>
                <div className="text-lg font-medium" style={{ color: colors.text }}>No data available</div>
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

  // Render Parameters Tab
  const renderParametersTab = () => {
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
              <pre className="text-xs font-mono whitespace-pre-wrap overflow-auto max-h-[calc(100vh-300px)]" style={{ color: colors.text }}>
                {ddl || '-- No DDL available for this object'}
              </pre>
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

  // Render Definition Tab (same as DDL)
  const renderDefinitionTab = () => {
    return renderDDLTab();
  };

  // Render Spec Tab (same as DDL)
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
  const renderPropertiesTab = () => {
    const details = objectDetails || activeObject || {};
    const targetDetails = details.targetDetails;
    
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

  // DDL loading effect
  useEffect(() => {
    if (activeTab === 'ddl' || activeTab === 'definition' || activeTab === 'spec' || activeTab === 'body') {
      const ddlTypes = ['TABLE', 'VIEW', 'PROCEDURE', 'FUNCTION', 'PACKAGE', 'TRIGGER', 'SEQUENCE'];
      const effectiveType = activeObject?.type?.toUpperCase();
      
      if (activeObject && ddlTypes.includes(effectiveType) && !objectDDL && !ddlLoading) {
        const targetName = activeObject.type === 'SYNONYM' && objectDetails?.TARGET_NAME 
          ? objectDetails.TARGET_NAME 
          : activeObject.name;
        
        const loadDDL = async () => {
          try {
            setDdlLoading(true);
            const ddlResponse = await getObjectDDL(authToken, { 
              objectType: effectiveType.toLowerCase(), 
              objectName: targetName 
            });
            
            if (ddlResponse && ddlResponse.data) {
              const ddlData = ddlResponse.data;
              let ddlText = '';
              
              if (typeof ddlData === 'string') {
                ddlText = ddlData;
              } else if (ddlData.ddl) {
                ddlText = ddlData.ddl;
              } else if (ddlData.text) {
                ddlText = ddlData.text;
              } else if (ddlData.sql) {
                ddlText = ddlData.sql;
              } else {
                ddlText = JSON.stringify(ddlData, null, 2);
              }
              
              if (ddlText && ddlText !== '{}') {
                setObjectDDL(ddlText);
              } else {
                setObjectDDL(`-- No DDL available for ${effectiveType} ${targetName}`);
              }
            } else {
              setObjectDDL(`-- No DDL available for ${effectiveType} ${targetName}`);
            }
          } catch (ddlError) {
            console.error('DDL fetch error:', ddlError);
            setObjectDDL(`-- Error loading DDL for ${effectiveType} ${targetName}\n-- ${ddlError.message}`);
          } finally {
            setDdlLoading(false);
          }
        };
        
        loadDDL();
      }
    }
  }, [activeTab, activeObject, objectDDL, ddlLoading, objectDetails, authToken]);

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
          colors={colors}
          objectTree={objectTree}
          handleToggleSection={handleToggleSection}
          handleLoadSection={handleLoadSection}
          handleLoadMore={handleLoadMore}
          schemaObjects={schemaObjects}
          filteredResults={filteredResults}  // ADD THIS LINE
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
          isFiltering={isFiltering}  // ADD THIS LINE
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
          selectedObject={selectedForApiGeneration || activeObject}
          colors={colors}
          obType={obType}
          theme={theme}
          authToken={authToken}
        />
      )}
    </div>
  );
};

export default SchemaBrowser;