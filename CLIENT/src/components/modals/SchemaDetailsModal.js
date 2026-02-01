// components/modals/SchemaDetailsModal.js
import React, { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";
import {
  Database, Table, Eye, GitMerge, Code2, Package,
  Activity, CheckCircle, AlertCircle, Clock, Users,
  Copy, Settings, Edit, Trash2, EyeOff, ExternalLink,
  ArrowDown, HardDrive, Layers, FileText, Grid
} from "lucide-react";

export default function SchemaDetailsModal({
  showSchemaDetails,
  setShowSchemaDetails,
  selectedSchema,
  handleExportSchema,
  handleCopySchema
}) {
  const [expandedSections, setExpandedSections] = useState({
    basicInfo: true,
    statistics: false,
    structure: false
  });

  if (!selectedSchema) return null;

  const toggleSection = (section) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  const getObjectIcon = (type) => {
    const icons = {
      table: <Table className="h-4 w-4" />,
      view: <Eye className="h-4 w-4" />,
      procedure: <GitMerge className="h-4 w-4" />,
      function: <Code2 className="h-4 w-4" />,
      package: <Package className="h-4 w-4" />,
      trigger: <Activity className="h-4 w-4" />
    };
    return icons[type] || <Database className="h-4 w-4" />;
  };

  const getObjectTypeBadge = (type) => {
    const colors = {
      table: "bg-blue-500/10 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800/50",
      view: "bg-green-500/10 text-green-700 dark:text-green-300 border border-green-200 dark:border-green-800/50",
      procedure: "bg-purple-500/10 text-purple-700 dark:text-purple-300 border border-purple-200 dark:border-purple-800/50",
      function: "bg-amber-500/10 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-800/50",
      package: "bg-pink-500/10 text-pink-700 dark:text-pink-300 border border-pink-200 dark:border-pink-800/50"
    };

    return (
      <Badge className={`text-xs ${colors[type] || 'bg-gray-500/10 text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-gray-700'}`}>
        {type.toUpperCase()}
      </Badge>
    );
  };

  const renderMobileSection = (sectionKey, title, icon, children) => (
    <div className="space-y-3">
      <button
        onClick={() => toggleSection(sectionKey)}
        className="flex items-center justify-between w-full p-3 rounded-lg bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors"
      >
        <div className="flex items-center gap-2">
          {icon}
          <Label className="text-xs font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400">
            {title}
          </Label>
        </div>
        <ArrowDown className={`h-4 w-4 text-gray-500 transition-transform ${
          expandedSections[sectionKey] ? 'rotate-180' : ''
        }`} />
      </button>
      
      <div className={`${expandedSections[sectionKey] ? 'block' : 'hidden'}`}>
        {children}
      </div>
    </div>
  );

  return (
    <Dialog open={showSchemaDetails} onOpenChange={setShowSchemaDetails}>
      <DialogContent className="max-w-2xl max-h-[85vh] overflow-y-auto bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl shadow-2xl">
        <DialogHeader className="space-y-3">
          <div className="flex items-center justify-between">
            <DialogTitle className="text-xl font-bold text-gray-900 dark:text-gray-100">
              Schema Object Details
            </DialogTitle>
            <Badge variant="outline" className="text-xs font-mono">
              ID: {selectedSchema.id?.substring(0, 6) || 'N/A'}
            </Badge>
          </div>
          <DialogDescription className="text-sm text-gray-500 dark:text-gray-400">
            Detailed information about this database schema object
          </DialogDescription>
        </DialogHeader>
        
        <Separator className="bg-gray-200 dark:bg-gray-700" />
        
        <div className="space-y-6 py-2">
          {/* Header Section */}
          <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 p-4 rounded-lg bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-800/50 dark:to-gray-900/50 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center gap-4">
              <div className="p-3 rounded-xl bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400">
                {getObjectIcon(selectedSchema.type)}
              </div>
              <div className="space-y-1">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {selectedSchema.name}
                </h3>
                <div className="flex flex-wrap items-center gap-2">
                  {getObjectTypeBadge(selectedSchema.type)}
                  <Badge variant="outline" className="text-xs">
                    {selectedSchema.database}
                  </Badge>
                  <Badge className="bg-green-500/10 text-green-700 dark:text-green-300 border border-green-200 dark:border-green-800/50 text-xs">
                    <HardDrive className="h-3 w-3 mr-1" />
                    {selectedSchema.size || '2.4MB'}
                  </Badge>
                </div>
              </div>
            </div>
          </div>
          
          {/* Basic Information */}
          {renderMobileSection(
            'basicInfo',
            'Object Details',
            <Database className="h-3 w-3" />,
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-3 p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Database Type</p>
                    <div className="flex items-center gap-2">
                      <Database className="h-4 w-4" />
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100 capitalize">
                        {selectedSchema.database}
                      </span>
                    </div>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Object Type</p>
                    <div className="flex items-center gap-2">
                      {getObjectIcon(selectedSchema.type)}
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100 capitalize">
                        {selectedSchema.type}
                      </span>
                    </div>
                  </div>
                </div>
                
                <div className="space-y-3 p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Columns/Parameters</p>
                    <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {selectedSchema.columns || 8}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Size</p>
                    <div className="flex items-center gap-2">
                      <HardDrive className="h-4 w-4" />
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {selectedSchema.size || '2.4MB'}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Description</p>
                <p className="text-sm text-gray-700 dark:text-gray-300">
                  {selectedSchema.description || 'Sample database object description for demonstration purposes.'}
                </p>
              </div>
            </>
          )}
          
          {/* Statistics */}
          {renderMobileSection(
            'statistics',
            'Statistics',
            <Activity className="h-3 w-3" />,
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-xs text-gray-500 dark:text-gray-400">Rows/Records</p>
                  <Users className="h-4 w-4 text-gray-400" />
                </div>
                <p className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {Math.floor(Math.random() * 10000).toLocaleString()}
                </p>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-xs text-gray-500 dark:text-gray-400">Indexes</p>
                  <Layers className="h-4 w-4 text-gray-400" />
                </div>
                <p className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {Math.floor(Math.random() * 5) + 1}
                </p>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-xs text-gray-500 dark:text-gray-400">Last Modified</p>
                  <Clock className="h-4 w-4 text-gray-400" />
                </div>
                <p className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {Math.floor(Math.random() * 30) + 1}d ago
                </p>
              </div>
            </div>
          )}
          
          {/* Structure */}
          {renderMobileSection(
            'structure',
            'Structure',
            <Grid className="h-3 w-3" />,
            <div className="space-y-3">
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between mb-3">
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">Column Structure</p>
                  <Badge className="text-xs">Preview</Badge>
                </div>
                <div className="space-y-2">
                  {Array.from({ length: 3 }).map((_, i) => (
                    <div key={i} className="flex items-center justify-between p-2 rounded bg-white dark:bg-gray-800">
                      <span className="text-xs font-mono text-gray-700 dark:text-gray-300">column_{i + 1}</span>
                      <span className="text-xs text-gray-500 dark:text-gray-400">VARCHAR(255)</span>
                    </div>
                  ))}
                </div>
              </div>
              
              <Button
                variant="outline"
                onClick={() => alert('View full structure')}
                className="w-full"
              >
                <Eye className="h-4 w-4 mr-2" />
                View Full Structure
              </Button>
            </div>
          )}
        </div>
        
        <DialogFooter className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-4 border-t border-gray-200 dark:border-gray-800">
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => handleCopySchema(selectedSchema)}
              className="rounded-lg border-gray-300 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              <Copy className="h-4 w-4 mr-2" />
              Copy Details
            </Button>
          </div>
          
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
            <div className="flex flex-row items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleExportSchema(selectedSchema)}
                className="rounded-lg border-blue-300 dark:border-blue-700 hover:bg-blue-50 dark:hover:bg-blue-900/20"
              >
                <FileText className="h-4 w-4 mr-2 text-blue-600 dark:text-blue-400" />
                Export DDL
              </Button>
              
              <Button
                variant="outline"
                size="sm"
                onClick={() => alert('Viewing data...')}
                className="rounded-lg border-emerald-300 dark:border-emerald-700 hover:bg-emerald-50 dark:hover:bg-emerald-900/20"
              >
                <Eye className="h-4 w-4 mr-2 text-emerald-600 dark:text-emerald-400" />
                View Data
              </Button>
            </div>
            
            <Separator orientation="vertical" className="h-auto sm:h-6 hidden sm:block" />
            
            <Button
              variant="default"
              onClick={() => setShowSchemaDetails(false)}
              className="rounded-lg bg-orange-600 hover:bg-orange-700 text-white"
            >
              Close
            </Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}