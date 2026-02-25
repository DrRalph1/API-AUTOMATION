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
              <option value="ALL">All Schema's</option>
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
  synonymCount = 0,
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
  handleContextMenu,
  isLoaded,
  children
}) => {
  
  useEffect(() => {
    if (isExpanded && !isLoaded && !isLoading) {
      Logger.debug('ObjectTreeSection', 'useEffect', `Loading ${title} on expand`);
      onLoadSection(type);
    }
  }, [isExpanded, isLoaded, isLoading, onLoadSection, type, title]);
  
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
  
  const totalCount = objects.length + synonymCount;
  
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
          {totalCount}
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
          ) : (
            <>
              {filteredObjects.map(obj => (
                <button
                  key={getObjectId(obj)}
                  onDoubleClick={() => handleDoubleClick(obj)}
                  onContextMenu={(e) => handleContextMenuWrapper(e, obj)}
                  onClick={() => handleObjectClick(obj)}
                  className={`flex items-center justify-between w-full px-2 py-2 rounded-sm cursor-pointer group text-left ${
                    activeObjectId === getObjectId(obj) ? 'font-medium' : ''
                  }`}
                  style={{
                    backgroundColor: activeObjectId === getObjectId(obj) ? colors.selected : 'transparent',
                    color: activeObjectId === getObjectId(obj) ? colors.primary : colors.text
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
              ))}
              {children}
            </>
          )}
        </div>
      )}
    </div>
  );
});

ObjectTreeSection.displayName = 'ObjectTreeSection';

// Synonym Subsection Component
const SynonymSubsection = React.memo(({ 
  synonyms, 
  targetType, 
  onSelectObject, 
  activeObjectId,
  filterQuery,
  selectedOwner,
  colors,
  getObjectIcon,
  handleContextMenu
}) => {
  if (!synonyms || synonyms.length === 0) return null;

  const filteredSynonyms = useMemo(() => {
    if (!filterQuery && selectedOwner === 'ALL') return synonyms;
    
    const searchLower = filterQuery.toLowerCase();
    
    return synonyms.filter(obj => {
      const ownerMatch = selectedOwner === 'ALL' || obj.owner === selectedOwner;
      if (!ownerMatch) return false;
      if (!filterQuery) return true;
      
      return (
        (obj.name && obj.name.toLowerCase().includes(searchLower)) ||
        (obj.owner && obj.owner.toLowerCase().includes(searchLower))
      );
    });
  }, [synonyms, filterQuery, selectedOwner]);

  if (filteredSynonyms.length === 0) return null;

  const getObjectId = (obj) => obj.id || `${obj.owner || 'unknown'}_${obj.name}`;

  return (
    <div className="ml-4 mt-1 mb-2 border-l-2 pl-2" style={{ borderColor: colors.border }}>
      <div className="flex items-center gap-1 px-2 py-1 text-xs" style={{ color: colors.textTertiary }}>
        <Link size={10} />
        <span className="uppercase tracking-wider text-[10px]">Synonyms</span>
        <span className="ml-1 text-[10px] px-1 rounded" style={{ backgroundColor: colors.border }}>
          {filteredSynonyms.length}
        </span>
      </div>
      <div className="space-y-0.7">
        {filteredSynonyms.map(synonym => (
          <button
            key={getObjectId(synonym)}
            onDoubleClick={() => onSelectObject(synonym, 'SYNONYM')}
            onContextMenu={(e) => handleContextMenu(e, synonym, 'SYNONYM')}
            onClick={() => onSelectObject(synonym, 'SYNONYM')}
            className={`flex items-center justify-between w-full px-2 py-1.5 rounded-sm cursor-pointer group text-left text-xs ${
              activeObjectId === getObjectId(synonym) ? 'font-medium' : ''
            }`}
            style={{
              backgroundColor: activeObjectId === getObjectId(synonym) ? colors.selected : 'transparent',
              color: activeObjectId === getObjectId(synonym) ? colors.primary : colors.text
            }}
          >
            <div className="flex items-center gap-2 min-w-0 flex-1">
              {getObjectIcon(targetType)}
              <span className="truncate">{synonym.name}</span>
            </div>
            {synonym.status && synonym.status !== 'VALID' && (
              <AlertCircle size={8} style={{ color: colors.error }} />
            )}
          </button>
        ))}
      </div>
    </div>
  );
});

SynonymSubsection.displayName = 'SynonymSubsection';

// Left Sidebar Component - UPDATED with synonyms under parent categories and counts
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
  schemaInfo,
  loadedSections
}) => {
  
  const handleCloseSidebar = useCallback(() => {
    setIsLeftSidebarVisible(false);
  }, [setIsLeftSidebarVisible]);

  const groupedSynonyms = useMemo(() => {
    const groups = {
      tables: [],
      views: [],
      procedures: [],
      functions: [],
      packages: [],
      sequences: [],
      types: [],
      triggers: [],
      other: []
    };

    (schemaObjects.synonyms || []).forEach(synonym => {
      const targetType = synonym.targetType || synonym.TARGET_TYPE || 'other';
      const targetTypeLower = targetType.toLowerCase();
      
      if (targetTypeLower.includes('table')) {
        groups.tables.push(synonym);
      } else if (targetTypeLower.includes('view')) {
        groups.views.push(synonym);
      } else if (targetTypeLower.includes('procedure')) {
        groups.procedures.push(synonym);
      } else if (targetTypeLower.includes('function')) {
        groups.functions.push(synonym);
      } else if (targetTypeLower.includes('package')) {
        groups.packages.push(synonym);
      } else if (targetTypeLower.includes('sequence')) {
        groups.sequences.push(synonym);
      } else if (targetTypeLower.includes('type')) {
        groups.types.push(synonym);
      } else if (targetTypeLower.includes('trigger')) {
        groups.triggers.push(synonym);
      } else {
        groups.other.push(synonym);
      }
    });

    return groups;
  }, [schemaObjects.synonyms]);

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
        <div className="flex items-center justify-between mb-0">
          <div className="flex items-center gap-2 flex-1 text-left">
            <Database size={16} style={{ color: colors.primary }} />
            <span className="text-sm font-medium truncate" style={{ color: colors.text }}>
               {owners.map(owner => (
                owner
              ))}
            </span>
          </div>
          <div className="flex gap-1">
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
          
            {/* Procedures Section with Synonyms */}
            <ObjectTreeSection
              title="Procedures"
              type="procedures"
              objects={schemaObjects.procedures || []}
              synonymCount={groupedSynonyms.procedures.length}
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
              isLoaded={loadedSections.procedures}
            >
              <SynonymSubsection
                synonyms={groupedSynonyms.procedures}
                targetType="procedure"
                onSelectObject={handleObjectSelect}
                activeObjectId={activeObject?.id}
                filterQuery={filterQuery}
                selectedOwner={selectedOwner}
                colors={colors}
                getObjectIcon={getObjectIcon}
                handleContextMenu={handleContextMenu}
              />
            </ObjectTreeSection>

            {/* Views Section with Synonyms */}
            <ObjectTreeSection
              title="Views"
              type="views"
              objects={schemaObjects.views || []}
              synonymCount={groupedSynonyms.views.length}
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
              isLoaded={loadedSections.views}
            >
              <SynonymSubsection
                synonyms={groupedSynonyms.views}
                targetType="view"
                onSelectObject={handleObjectSelect}
                activeObjectId={activeObject?.id}
                filterQuery={filterQuery}
                selectedOwner={selectedOwner}
                colors={colors}
                getObjectIcon={getObjectIcon}
                handleContextMenu={handleContextMenu}
              />
            </ObjectTreeSection>


            {/* Functions Section with Synonyms */}
            <ObjectTreeSection
              title="Functions"
              type="functions"
              objects={schemaObjects.functions || []}
              synonymCount={groupedSynonyms.functions.length}
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
              isLoaded={loadedSections.functions}
            >
              <SynonymSubsection
                synonyms={groupedSynonyms.functions}
                targetType="function"
                onSelectObject={handleObjectSelect}
                activeObjectId={activeObject?.id}
                filterQuery={filterQuery}
                selectedOwner={selectedOwner}
                colors={colors}
                getObjectIcon={getObjectIcon}
                handleContextMenu={handleContextMenu}
              />
            </ObjectTreeSection>

            {/* Packages Section with Synonyms */}
            <ObjectTreeSection
              title="Packages"
              type="packages"
              objects={schemaObjects.packages || []}
              synonymCount={groupedSynonyms.packages.length}
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
              isLoaded={loadedSections.packages}
            >
              <SynonymSubsection
                synonyms={groupedSynonyms.packages}
                targetType="package"
                onSelectObject={handleObjectSelect}
                activeObjectId={activeObject?.id}
                filterQuery={filterQuery}
                selectedOwner={selectedOwner}
                colors={colors}
                getObjectIcon={getObjectIcon}
                handleContextMenu={handleContextMenu}
              />
            </ObjectTreeSection>

            {/* Tables Section with Synonyms */}
            <ObjectTreeSection
              title="Tables"
              type="tables"
              objects={schemaObjects.tables || []}
              synonymCount={groupedSynonyms.tables.length}
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
              isLoaded={loadedSections.tables}
            >
              <SynonymSubsection
                synonyms={groupedSynonyms.tables}
                targetType="table"
                onSelectObject={handleObjectSelect}
                activeObjectId={activeObject?.id}
                filterQuery={filterQuery}
                selectedOwner={selectedOwner}
                colors={colors}
                getObjectIcon={getObjectIcon}
                handleContextMenu={handleContextMenu}
              />
            </ObjectTreeSection>

            {/* Sequences Section with Synonyms */}
            <ObjectTreeSection
              title="Sequences"
              type="sequences"
              objects={schemaObjects.sequences || []}
              synonymCount={groupedSynonyms.sequences.length}
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
              isLoaded={loadedSections.sequences}
            >
              <SynonymSubsection
                synonyms={groupedSynonyms.sequences}
                targetType="sequence"
                onSelectObject={handleObjectSelect}
                activeObjectId={activeObject?.id}
                filterQuery={filterQuery}
                selectedOwner={selectedOwner}
                colors={colors}
                getObjectIcon={getObjectIcon}
                handleContextMenu={handleContextMenu}
              />
            </ObjectTreeSection>

            {/* Types Section with Synonyms */}
            <ObjectTreeSection
              title="Types"
              type="types"
              objects={schemaObjects.types || []}
              synonymCount={groupedSynonyms.types.length}
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
              isLoaded={loadedSections.types}
            >
              <SynonymSubsection
                synonyms={groupedSynonyms.types}
                targetType="type"
                onSelectObject={handleObjectSelect}
                activeObjectId={activeObject?.id}
                filterQuery={filterQuery}
                selectedOwner={selectedOwner}
                colors={colors}
                getObjectIcon={getObjectIcon}
                handleContextMenu={handleContextMenu}
              />
            </ObjectTreeSection>

            {/* Triggers Section with Synonyms */}
            <ObjectTreeSection
              title="Triggers"
              type="triggers"
              objects={schemaObjects.triggers || []}
              synonymCount={groupedSynonyms.triggers.length}
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
              isLoaded={loadedSections.triggers}
            >
              <SynonymSubsection
                synonyms={groupedSynonyms.triggers}
                targetType="trigger"
                onSelectObject={handleObjectSelect}
                activeObjectId={activeObject?.id}
                filterQuery={filterQuery}
                selectedOwner={selectedOwner}
                colors={colors}
                getObjectIcon={getObjectIcon}
                handleContextMenu={handleContextMenu}
              />
            </ObjectTreeSection>

            {/* Other Synonyms (if any) */}
            {groupedSynonyms.other.length > 0 && (
              <ObjectTreeSection
                title="Other Synonyms"
                type="synonyms"
                objects={groupedSynonyms.other}
                synonymCount={0}
                isLoading={loadingStates.synonyms}
                isExpanded={objectTree.synonyms}
                onToggle={handleToggleSection}
                onLoadSection={handleLoadSection}
                onSelectObject={handleObjectSelect}
                activeObjectId={activeObject?.id}
                filterQuery={filterQuery}
                selectedOwner={selectedOwner}
                colors={colors}
                getObjectIcon={() => getObjectIcon('synonym')}
                handleContextMenu={handleContextMenu}
                isLoaded={loadedSections.synonyms}
              />
            )}
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
  const [loading, setLoading] = useState(true);
  const [initialLoadComplete, setInitialLoadComplete] = useState(false);
  const [error, setError] = useState(null);
  const [schemaInfo, setSchemaInfo] = useState(null);
  const [hasAutoSelected, setHasAutoSelected] = useState(false);
  const [initialized, setInitialized] = useState(false);

  const [ddlLoading, setDdlLoading] = useState(false);


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
  
  // Object tree expanded state - UPDATED: Only Tables is expanded by default
  const [objectTree, setObjectTree] = useState({
    procedures: true, // Only procedures is expanded by default
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

  // Load all object counts - FIXED
const loadAllObjectCounts = useCallback(async () => {
  if (!authToken) return;
  
  Logger.info('SchemaBrowser', 'loadAllObjectCounts', 'Loading all object counts');
  
  // Track owners locally to avoid stale closure
  let updatedOwners = new Set(owners);
  
  // First load procedures explicitly and wait for them
  Logger.info('SchemaBrowser', 'loadAllObjectCounts', 'Loading procedures first...');
  try {
    const cacheKey = `procedures_${authToken.substring(0, 10)}`;
    const cached = objectCache.get(cacheKey);
    
    let proceduresData = [];
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      proceduresData = cached.data;
      Logger.info('SchemaBrowser', 'loadAllObjectCounts', `Loaded procedures from cache (${proceduresData.length})`);
    } else {
      const response = await getAllProceduresForFrontend(authToken);
      let data = [];
      if (response && response.data) {
        data = response.data;
      } else {
        const processed = handleSchemaBrowserResponse(response);
        data = processed.data || [];
      }
      objectCache.set(cacheKey, { data, timestamp: Date.now() });
      proceduresData = data;
      Logger.info('SchemaBrowser', 'loadAllObjectCounts', `Loaded procedures from API (${proceduresData.length})`);
    }
    
    // Update procedures state immediately
    setSchemaObjects(prev => ({ ...prev, procedures: proceduresData }));
    setLoadedSections(prev => ({ ...prev, procedures: true }));
    
    // Update owners
    proceduresData.forEach(obj => {
      if (obj.owner) updatedOwners.add(obj.owner);
    });
    
  } catch (err) {
    Logger.error('SchemaBrowser', 'loadAllObjectCounts', 'Error loading procedures', err);
    setSchemaObjects(prev => ({ ...prev, procedures: [] }));
    setLoadedSections(prev => ({ ...prev, procedures: true }));
  }
  
  // Now load all other object types in parallel
  const loadPromises = [
    { type: 'views', func: getAllViewsForFrontend },
    { type: 'functions', func: getAllFunctionsForFrontend },
    { type: 'tables', func: getAllTablesForFrontend },
    { type: 'packages', func: getAllPackagesForFrontend },
    { type: 'sequences', func: getAllSequencesForFrontend },
    { type: 'synonyms', func: getAllSynonymsForFrontend },
    { type: 'types', func: getAllTypesForFrontend },
    { type: 'triggers', func: getAllTriggersForFrontend }
  ];

  await Promise.all(loadPromises.map(async ({ type, func }) => {
    try {
      const cacheKey = `${type}_${authToken.substring(0, 10)}`;
      const cached = objectCache.get(cacheKey);
      
      if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
        setSchemaObjects(prev => ({ ...prev, [type]: cached.data }));
        setLoadedSections(prev => ({ ...prev, [type]: true }));
        Logger.debug('SchemaBrowser', 'loadAllObjectCounts', `Loaded ${type} from cache (${cached.data.length} items)`);
        
        // Update owners
        cached.data.forEach(obj => {
          if (obj.owner) updatedOwners.add(obj.owner);
        });
      } else {
        Logger.debug('SchemaBrowser', 'loadAllObjectCounts', `Loading ${type} from API`);
        const response = await func(authToken);
        
        let data = [];
        if (response && response.data) {
          data = response.data;
        } else {
          const processed = handleSchemaBrowserResponse(response);
          data = processed.data || [];
        }
        
        objectCache.set(cacheKey, { data, timestamp: Date.now() });
        
        setSchemaObjects(prev => ({ ...prev, [type]: data }));
        setLoadedSections(prev => ({ ...prev, [type]: true }));
        
        // Update owners
        data.forEach(obj => {
          if (obj.owner) updatedOwners.add(obj.owner);
        });
        
        Logger.info('SchemaBrowser', 'loadAllObjectCounts', `Loaded ${data.length} ${type}`);
      }
    } catch (err) {
      Logger.error('SchemaBrowser', 'loadAllObjectCounts', `Error loading ${type}`, err);
      setSchemaObjects(prev => ({ ...prev, [type]: [] }));
      setLoadedSections(prev => ({ ...prev, [type]: true }));
    }
  }));
  
  // Update owners once at the end
  setOwners(Array.from(updatedOwners).sort());
  
  // After all loads complete, log the final state
  Logger.info('SchemaBrowser', 'loadAllObjectCounts', 'Final object counts:', {
    procedures: schemaObjects.procedures?.length || 0,
    packages: schemaObjects.packages?.length || 0,
    tables: schemaObjects.tables?.length || 0
  });
}, [authToken]); // Remove owners dependency to avoid stale closure

  // Load object type on demand
  const loadObjectType = useCallback(async (type) => {
    if (!authToken) return;
    if (loadingStates[type]) return;
    if (loadedSections[type]) {
      Logger.debug('SchemaBrowser', 'loadObjectType', `${type} already loaded`);
      return;
    }
    
    Logger.info('SchemaBrowser', 'loadObjectType', `Loading ${type} details from API`);
    
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
      
      const cacheKey = `${type}_${authToken.substring(0, 10)}`;
      objectCache.set(cacheKey, { data, timestamp: Date.now() });
      
      setSchemaObjects(prev => ({ ...prev, [type]: data }));
      setLoadedSections(prev => ({ ...prev, [type]: true }));
      
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
  }, [authToken, loadingStates, loadedSections, owners]);

  // Get first schema object for auto-select - FIXED to prioritize procedures
const getFirstSchemaObject = useCallback(() => {
  // Log available objects for debugging
  Logger.debug('SchemaBrowser', 'getFirstSchemaObject', 'Available objects:', {
    proceduresCount: schemaObjects.procedures?.length || 0,
    tablesCount: schemaObjects.tables?.length || 0,
    packagesCount: schemaObjects.packages?.length || 0
  });
  
  // Explicitly check procedures first
  if (schemaObjects.procedures && schemaObjects.procedures.length > 0) {
    const procedure = schemaObjects.procedures[0];
    Logger.info('SchemaBrowser', 'getFirstSchemaObject', `Found first procedure: ${procedure.name}`);
    return { 
      object: {
        ...procedure,
        id: procedure.id || `${procedure.owner || 'unknown'}_${procedure.name}`
      }, 
      type: 'PROCEDURE' 
    };
  }
  
  // If no procedures, try tables
  if (schemaObjects.tables && schemaObjects.tables.length > 0) {
    const table = schemaObjects.tables[0];
    Logger.info('SchemaBrowser', 'getFirstSchemaObject', `No procedures found, using first table: ${table.name}`);
    return { 
      object: {
        ...table,
        id: table.id || `${table.owner || 'unknown'}_${table.name}`
      }, 
      type: 'TABLE' 
    };
  }
  
  // If no tables, try views
  if (schemaObjects.views && schemaObjects.views.length > 0) {
    const view = schemaObjects.views[0];
    Logger.info('SchemaBrowser', 'getFirstSchemaObject', `No tables found, using first view: ${view.name}`);
    return { 
      object: {
        ...view,
        id: view.id || `${view.owner || 'unknown'}_${view.name}`
      }, 
      type: 'VIEW' 
    };
  }
  
  // If no views, try functions
  if (schemaObjects.functions && schemaObjects.functions.length > 0) {
    const func = schemaObjects.functions[0];
    Logger.info('SchemaBrowser', 'getFirstSchemaObject', `No views found, using first function: ${func.name}`);
    return { 
      object: {
        ...func,
        id: func.id || `${func.owner || 'unknown'}_${func.name}`
      }, 
      type: 'FUNCTION' 
    };
  }
  
  // If no functions, try packages
  if (schemaObjects.packages && schemaObjects.packages.length > 0) {
    const pkg = schemaObjects.packages[0];
    Logger.info('SchemaBrowser', 'getFirstSchemaObject', `No functions found, using first package: ${pkg.name}`);
    return { 
      object: {
        ...pkg,
        id: pkg.id || `${pkg.owner || 'unknown'}_${pkg.name}`
      }, 
      type: 'PACKAGE' 
    };
  }
  
  // If no packages, try sequences
  if (schemaObjects.sequences && schemaObjects.sequences.length > 0) {
    const seq = schemaObjects.sequences[0];
    return { 
      object: {
        ...seq,
        id: seq.id || `${seq.owner || 'unknown'}_${seq.name}`
      }, 
      type: 'SEQUENCE' 
    };
  }
  
  // If no sequences, try synonyms
  if (schemaObjects.synonyms && schemaObjects.synonyms.length > 0) {
    const syn = schemaObjects.synonyms[0];
    return { 
      object: {
        ...syn,
        id: syn.id || `${syn.owner || 'unknown'}_${syn.name}`
      }, 
      type: 'SYNONYM' 
    };
  }
  
  // If no synonyms, try types
  if (schemaObjects.types && schemaObjects.types.length > 0) {
    const type = schemaObjects.types[0];
    return { 
      object: {
        ...type,
        id: type.id || `${type.owner || 'unknown'}_${type.name}`
      }, 
      type: 'TYPE' 
    };
  }
  
  // If no types, try triggers
  if (schemaObjects.triggers && schemaObjects.triggers.length > 0) {
    const trigger = schemaObjects.triggers[0];
    return { 
      object: {
        ...trigger,
        id: trigger.id || `${trigger.owner || 'unknown'}_${trigger.name}`
      }, 
      type: 'TRIGGER' 
    };
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
      const tableDataResult = extractTableData(processed);
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
    setLoading(true);
    setInitialLoadComplete(false);
    setInitialized(false);
    setHasAutoSelected(false);
    
    await loadSchemaInfo();
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
      views: [],
      procedures: [],
      functions: [],
      packages: [],
      sequences: [],
      synonyms: [],
      types: [],
      triggers: []
    });
    await loadAllObjectCounts();
    setInitialized(true);
    setLoading(false);
    setInitialLoadComplete(true);
  }, [loadSchemaInfo, loadAllObjectCounts]);

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

  // Initialize - MODIFIED to wait for procedures
useEffect(() => {
  if (!initialized && authToken) {
    const initializeSchema = async () => {
      setLoading(true);
      await loadSchemaInfo();
      
      // Load all objects (procedures are prioritized inside loadAllObjectCounts)
      await loadAllObjectCounts();
      
      // Double-check that procedures are loaded
      const currentProcedures = schemaObjects.procedures || [];
      Logger.info('SchemaBrowser', 'initialize', `Initialization complete. Procedures: ${currentProcedures.length}`);
      
      setInitialized(true);
      setLoading(false);
      setInitialLoadComplete(true);
      
      // If procedures are already loaded, trigger selection immediately
      if (currentProcedures.length > 0 && !activeObject && !hasAutoSelected) {
        const firstProcedure = currentProcedures[0];
        const procedureWithId = {
          ...firstProcedure,
          id: firstProcedure.id || `${firstProcedure.owner || 'unknown'}_${firstProcedure.name}`
        };
        
        setHasAutoSelected(true);
        setTimeout(() => {
          handleObjectSelect(procedureWithId, 'PROCEDURE');
        }, 100);
      }
    };
    
    initializeSchema();
  }
}, [authToken, initialized, loadSchemaInfo, loadAllObjectCounts, schemaObjects.procedures, activeObject, hasAutoSelected, handleObjectSelect]);

 // Auto-select first procedure - CHECKING BOTH PROCEDURES AND SYNONYMS
useEffect(() => {
  console.log('=== AUTO-SELECT DEBUG (with synonyms) ===');
  console.log('activeObject:', activeObject?.name);
  console.log('hasAutoSelected:', hasAutoSelected);
  console.log('initialLoadComplete:', initialLoadComplete);
  console.log('procedures count:', schemaObjects.procedures?.length);
  console.log('synonyms count:', schemaObjects.synonyms?.length);
  
  // If we already have an active object or already auto-selected, stop
  if (activeObject || hasAutoSelected) {
    console.log('Already have active object or already auto-selected, skipping');
    return;
  }
  
  // If initial load not complete, wait
  if (!initialLoadComplete) {
    console.log('Initial load not complete, waiting...');
    return;
  }
  
  // First, check for actual procedures
  if (schemaObjects.procedures && schemaObjects.procedures.length > 0) {
    const firstProcedure = schemaObjects.procedures[0];
    console.log('✅ Found actual procedure:', firstProcedure.name);
    
    const procedureWithId = {
      ...firstProcedure,
      id: firstProcedure.id || `${firstProcedure.owner || 'unknown'}_${firstProcedure.name}`
    };
    
    setHasAutoSelected(true);
    setTimeout(() => {
      console.log('Calling handleObjectSelect with actual procedure:', firstProcedure.name);
      handleObjectSelect(procedureWithId, 'PROCEDURE');
    }, 100);
    return;
  }
  
  // If no actual procedures, check synonyms for ones targeting procedures
  if (schemaObjects.synonyms && schemaObjects.synonyms.length > 0) {
    // Find synonyms that target procedures
    const procedureSynonyms = schemaObjects.synonyms.filter(syn => {
      const targetType = syn.targetType || syn.TARGET_TYPE || '';
      return targetType.toUpperCase().includes('PROCEDURE');
    });
    
    console.log('Procedure synonyms found:', procedureSynonyms.length);
    
    if (procedureSynonyms.length > 0) {
      const firstSynonym = procedureSynonyms[0];
      console.log('✅ Found synonym targeting procedure:', firstSynonym.name);
      
      const synonymWithId = {
        ...firstSynonym,
        id: firstSynonym.id || `${firstSynonym.owner || 'unknown'}_${firstSynonym.name}`
      };
      
      setHasAutoSelected(true);
      setTimeout(() => {
        console.log('Calling handleObjectSelect with synonym:', firstSynonym.name);
        handleObjectSelect(synonymWithId, 'SYNONYM');
      }, 100);
      return;
    }
  }
  
  console.log('❌ No procedures or procedure synonyms found');
  console.log('Available objects:', {
    procedures: schemaObjects.procedures?.length,
    synonyms: schemaObjects.synonyms?.length,
    packages: schemaObjects.packages?.length,
    tables: schemaObjects.tables?.length
  });
  
}, [activeObject, hasAutoSelected, initialLoadComplete, schemaObjects, handleObjectSelect]);

  // Update table data when dataView changes
  useEffect(() => {
    if (activeObject?.type === 'TABLE' || (activeObject?.type === 'SYNONYM' && objectDetails?.TARGET_TYPE === 'TABLE')) {
      const tableName = activeObject.type === 'SYNONYM' && objectDetails?.TARGET_NAME 
        ? objectDetails.TARGET_NAME 
        : activeObject.name;
      loadTableData(tableName);
    }
  }, [dataView.page, dataView.pageSize, dataView.sortColumn, dataView.sortDirection]);

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
    const totalRows = tableData?.totalRows || 0;
    const totalPages = tableData?.totalPages || 1;
    
    return (
      <div className="flex-1 flex flex-col h-full">
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

        <div className="flex-1 overflow-auto relative">
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
            <div className="border rounded overflow-auto h-full" style={{ borderColor: colors.gridBorder }}>
              <table className="w-full">
                <thead style={{ backgroundColor: colors.tableHeader, position: 'sticky', top: 0, zIndex: 10 }}>
                  <tr>
                    {columns.map(col => (
                      <th 
                        key={col.name || col.COLUMN_NAME} 
                        className="text-left p-2 text-xs font-medium cursor-pointer hover:bg-opacity-50"
                        onClick={() => handleSortChange(
                          col.name || col.COLUMN_NAME, 
                          dataView.sortColumn === (col.name || col.COLUMN_NAME) && dataView.sortDirection === 'ASC' ? 'DESC' : 'ASC'
                        )}
                        style={{ color: colors.textSecondary }}
                      >
                        <div className="flex items-center gap-1">
                          <span className="truncate">{col.name || col.COLUMN_NAME}</span>
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
                      className="hover:bg-opacity-50 transition-colors"
                      style={{ 
                        backgroundColor: rowIndex % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                      }}
                    >
                      {columns.map(col => {
                        const columnName = col.name || col.COLUMN_NAME;
                        const value = row[columnName];
                        return (
                          <td key={columnName} className="p-2 text-xs border-b" style={{ 
                            borderColor: colors.gridBorder,
                            color: colors.text,
                            maxWidth: '200px'
                          }}>
                            <div className="truncate" title={value?.toString()}>
                              {value !== null && value !== undefined ? (
                                typeof value === 'object' ? JSON.stringify(value) : value.toString()
                              ) : (
                                <span style={{ color: colors.textTertiary }}>NULL</span>
                              )}
                            </div>
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
  };

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

  // Handle Object Select - Always show Properties tab first
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
  setDdlLoading(false); // Reset DDL loading state
  
  // IMPORTANT: Set active tab to properties BEFORE setting the active object
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
    // Load object details
    Logger.debug('SchemaBrowser', 'handleObjectSelect', `Loading details for ${object.name}`);
    const response = await getObjectDetails(authToken, { objectType: type, objectName: object.name });
    
    const processedResponse = handleSchemaBrowserResponse(response);
    const responseData = processedResponse.data || processedResponse;
    
    const enrichedResponseData = {
      ...responseData,
      name: responseData.name || object.name,
      type: responseData.type || type
    };
    
    setObjectDetails(enrichedResponseData);
    
    // Determine what type of object we're dealing with
    const upperType = type.toUpperCase();
    let effectiveType = upperType;
    let targetType = null;
    let targetName = object.name;
    let targetOwner = null;
    
    if (upperType === 'SYNONYM' && responseData?.targetDetails) {
      targetType = responseData.targetDetails.OBJECT_TYPE || responseData.targetDetails.objectType;
      targetName = responseData.TARGET_NAME || object.name;
      targetOwner = responseData.TARGET_OWNER || responseData.targetDetails.OWNER;
      
      if (targetType) {
        effectiveType = targetType;
      }
    }
    
    // Load table data in background if it's a table
    if (effectiveType === 'TABLE') {
      if (upperType === 'SYNONYM' && targetType === 'TABLE') {
        loadTableData(targetName).catch(err => console.error('Background table load error:', err));
      } else {
        loadTableData(object.name).catch(err => console.error('Background table load error:', err));
      }
    }
    
    // Load DDL in background for appropriate types
    const ddlTypes = ['TABLE', 'VIEW', 'PROCEDURE', 'FUNCTION', 'PACKAGE', 'TRIGGER', 'SEQUENCE'];
    if (ddlTypes.includes(effectiveType)) {
      (async () => {
        try {
          setDdlLoading(true); // Set DDL loading to true
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
          console.error('Background DDL fetch error:', ddlError);
          setObjectDDL(`-- Error loading DDL for ${effectiveType} ${targetName}\n-- ${ddlError.message}`);
        } finally {
          setDdlLoading(false); // Set DDL loading to false when done
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

// Update the renderDDLTab function to show loader when ddlLoading is true
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

// Also update the renderDefinitionTab, renderSpecTab, and renderBodyTab functions
// to use the same DDL loading state

const renderDefinitionTab = () => {
  return renderDDLTab();
};

const renderSpecTab = () => {
  return renderDDLTab();
};

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

// Add a useEffect to track when the DDL tab is clicked
useEffect(() => {
  if (activeTab === 'ddl' || activeTab === 'definition' || activeTab === 'spec' || activeTab === 'body') {
    // If we're on a DDL-related tab and DDL is still loading or empty, show loader
    const ddlTypes = ['TABLE', 'VIEW', 'PROCEDURE', 'FUNCTION', 'PACKAGE', 'TRIGGER', 'SEQUENCE'];
    const effectiveType = activeObject?.type?.toUpperCase();
    
    if (activeObject && ddlTypes.includes(effectiveType) && !objectDDL && !ddlLoading) {
      // Trigger DDL load if not already loaded
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


// DIRECT PROCEDURE SELECTOR - Added after your other useEffects
useEffect(() => {
  // This runs whenever schemaObjects.procedures changes
  const procedures = schemaObjects.procedures || [];
  
  if (procedures.length > 0 && !activeObject && !hasAutoSelected && initialLoadComplete) {
    Logger.info('SchemaBrowser', 'direct-procedure-selector', `Procedures loaded: ${procedures.length}, selecting first one`);
    
    const firstProcedure = procedures[0];
    const procedureWithId = {
      ...firstProcedure,
      id: firstProcedure.id || `${firstProcedure.owner || 'unknown'}_${firstProcedure.name}`
    };
    
    setHasAutoSelected(true);
    // Immediate selection without timeout
    handleObjectSelect(procedureWithId, 'PROCEDURE');
  }
}, [schemaObjects.procedures, activeObject, hasAutoSelected, initialLoadComplete, handleObjectSelect]);


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
          schemaObjects={schemaObjects}
          loadingStates={loadingStates}
          activeObject={activeObject}
          handleObjectSelect={handleObjectSelect}
          getObjectIcon={getObjectIcon}
          handleContextMenu={handleContextMenu}
          loading={loading}
          onRefreshSchema={handleRefresh}
          schemaInfo={schemaInfo}
          loadedSections={loadedSections}
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
          theme={theme}
        />
      )}
    </div>
  );
};

export default SchemaBrowser;