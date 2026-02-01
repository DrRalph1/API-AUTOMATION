// components/modals/LogExportModal.js
import React, { useState } from "react";
import { showSuccess, showError, showConfirm } from "@/lib/sweetAlert";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import {
  Download, FileText, Filter, Calendar, AlertTriangle,
  CheckCircle, XCircle, Clock, Settings, ArrowDown,
  ChevronDown, ChevronUp, Smartphone
} from "lucide-react";

export default function LogExportModal({
  showExportDialog,
  setShowExportDialog,
  onExport,
  filters = {},
  loading = false
}) {
  const [exportConfig, setExportConfig] = useState({
    format: "csv",
    includeHeaders: true,
    dateRange: "last7days",
    customStartDate: "",
    customEndDate: "",
    severity: filters.severity || "all",
    searchTerm: filters.search || "",
    timeFilter: filters.timeFilter || "all",
    compression: false,
    filename: "system_logs_export"
  });

  const [expandedSections, setExpandedSections] = useState({
    filters: false,
    format: false,
    advanced: false
  });

  const dateRangeOptions = [
    { value: "today", label: "Today" },
    { value: "yesterday", label: "Yesterday" },
    { value: "last7days", label: "Last 7 days" },
    { value: "last30days", label: "Last 30 days" },
    { value: "thismonth", label: "This month" },
    { value: "custom", label: "Custom range" }
  ];

  const formatOptions = [
    { value: "csv", label: "CSV", description: "CSV format" },
    { value: "json", label: "JSON", description: "JSON format" },
    { value: "txt", label: "Text", description: "Text format" },
    { value: "xlsx", label: "Excel", description: "Excel format" }
  ];

  const severityOptions = [
    { value: "all", label: "All" },
    { value: "error", label: "Errors" },
    { value: "warn", label: "Warnings & Errors" },
    { value: "info", label: "Info & Above" },
    { value: "debug", label: "All Levels" }
  ];

  const handleExport = () => {
    if (onExport) {
      onExport(exportConfig);
    }
  };

  const estimateFileSize = () => {
    const baseSize = 1024;
    let multiplier = 1;
    
    switch (exportConfig.dateRange) {
      case "today": multiplier = 1; break;
      case "yesterday": multiplier = 1; break;
      case "last7days": multiplier = 7; break;
      case "last30days": multiplier = 30; break;
      case "thismonth": multiplier = 15; break;
      default: multiplier = 7;
    }
    
    if (exportConfig.severity !== "all") multiplier *= 0.5;
    if (exportConfig.compression) multiplier *= 0.3;
    
    const estimatedKB = Math.round(baseSize * multiplier);
    if (estimatedKB < 1024) return `${estimatedKB} KB`;
    return `${(estimatedKB / 1024).toFixed(1)} MB`;
  };

  const toggleSection = (sectionKey) => {
    setExpandedSections(prev => ({
      ...prev,
      [sectionKey]: !prev[sectionKey]
    }));
  };

  // Mobile responsive section renderer
  const MobileSection = ({ sectionKey, title, icon, children }) => (
    <div className="space-y-2">
      <button
        onClick={() => toggleSection(sectionKey)}
        className="flex items-center justify-between w-full p-3 sm:p-4 rounded-lg bg-gray-50 dark:bg-gray-800 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors active:scale-[0.98]"
        aria-expanded={expandedSections[sectionKey]}
      >
        <div className="flex items-center gap-3">
          <div className="p-2 rounded-lg bg-white dark:bg-gray-900 shadow-sm">
            {icon}
          </div>
          <div className="text-left">
            <span className="text-sm font-semibold text-gray-900 dark:text-gray-100 block">
              {title}
            </span>
            <span className="text-xs text-gray-500 dark:text-gray-400">
              {expandedSections[sectionKey] ? "Tap to collapse" : "Tap to expand"}
            </span>
          </div>
        </div>
        {expandedSections[sectionKey] ? (
          <ChevronUp className="h-5 w-5 text-gray-500" />
        ) : (
          <ChevronDown className="h-5 w-5 text-gray-500" />
        )}
      </button>
      
      <div className={`transition-all duration-200 ease-in-out ${
        expandedSections[sectionKey] 
          ? "max-h-[1000px] opacity-100 overflow-visible" 
          : "max-h-0 opacity-0 overflow-hidden"
      }`}>
        <div className="px-2 pb-2">
          {children}
        </div>
      </div>
    </div>
  );

  return (
    <AlertDialog open={showExportDialog} onOpenChange={setShowExportDialog}>
      <AlertDialogContent className="sm:max-w-2xl w-[95vw] max-w-[95vw] bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl shadow-2xl max-h-[90vh] overflow-y-auto p-4 sm:p-6">
        <AlertDialogHeader className="px-0">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-full bg-blue-100 dark:bg-blue-900/30">
              <Download className="h-5 w-5 sm:h-6 sm:w-6 text-blue-600 dark:text-blue-400" />
            </div>
            <div>
              <AlertDialogTitle className="text-base sm:text-lg font-bold text-gray-900 dark:text-gray-100">
                Export Logs
              </AlertDialogTitle>
              <AlertDialogDescription className="text-xs sm:text-sm text-gray-600 dark:text-gray-400">
                Configure and export logs with filters
              </AlertDialogDescription>
            </div>
          </div>
        </AlertDialogHeader>
        
        <Separator className="my-4 bg-gray-200 dark:bg-gray-700" />
        
        <div className="space-y-4 sm:space-y-6">
          {/* Export Summary - Mobile Optimized */}
          <div className="p-3 sm:p-4 rounded-lg bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800/30">
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4 sm:gap-4">
              <div className="text-center">
                <div className="text-xs text-gray-500 dark:text-gray-400 mb-1">Format</div>
                <div className="text-sm font-semibold text-gray-900 dark:text-gray-100 truncate">
                  {exportConfig.format.toUpperCase()}
                </div>
              </div>
              <div className="text-center">
                <div className="text-xs text-gray-500 dark:text-gray-400 mb-1">Range</div>
                <div className="text-sm font-semibold text-gray-900 dark:text-gray-100 truncate">
                  {dateRangeOptions.find(opt => opt.value === exportConfig.dateRange)?.label}
                </div>
              </div>
              <div className="text-center">
                <div className="text-xs text-gray-500 dark:text-gray-400 mb-1">Severity</div>
                <div className="text-sm font-semibold text-gray-900 dark:text-gray-100 truncate">
                  {severityOptions.find(opt => opt.value === exportConfig.severity)?.label}
                </div>
              </div>
              <div className="text-center">
                <div className="text-xs text-gray-500 dark:text-gray-400 mb-1">Size</div>
                <div className="text-sm font-semibold text-gray-900 dark:text-gray-100 truncate">
                  {estimateFileSize()}
                </div>
              </div>
            </div>
          </div>

          {/* Mobile Sections */}
          <div className="space-y-3 sm:space-y-4">
            {/* Filter Settings */}
            <MobileSection
              sectionKey="filters"
              title="Filter Settings"
              icon={<Filter className="h-4 w-4 text-blue-500" />}
            >
              <div className="space-y-4 p-3 sm:p-4 rounded-lg bg-gray-50 dark:bg-gray-800/50 border border-gray-200 dark:border-gray-700">
                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label className="text-sm font-medium">Date Range</Label>
                    <Select
                      value={exportConfig.dateRange}
                      onValueChange={(value) => setExportConfig({...exportConfig, dateRange: value})}
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {dateRangeOptions.map((option) => (
                          <SelectItem key={option.value} value={option.value}>
                            {option.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  
                  <div className="space-y-2">
                    <Label className="text-sm font-medium">Severity Level</Label>
                    <Select
                      value={exportConfig.severity}
                      onValueChange={(value) => setExportConfig({...exportConfig, severity: value})}
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {severityOptions.map((option) => (
                          <SelectItem key={option.value} value={option.value}>
                            {option.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                
                {exportConfig.dateRange === "custom" && (
                  <div className="space-y-4 pt-2 border-t border-gray-200 dark:border-gray-700">
                    <div className="space-y-2">
                      <Label className="text-sm font-medium">Start Date</Label>
                      <Input
                        type="date"
                        value={exportConfig.customStartDate}
                        onChange={(e) => setExportConfig({...exportConfig, customStartDate: e.target.value})}
                        className="w-full"
                      />
                    </div>
                    <div className="space-y-2">
                      <Label className="text-sm font-medium">End Date</Label>
                      <Input
                        type="date"
                        value={exportConfig.customEndDate}
                        onChange={(e) => setExportConfig({...exportConfig, customEndDate: e.target.value})}
                        className="w-full"
                      />
                    </div>
                  </div>
                )}
                
                <div className="space-y-2 pt-2 border-t border-gray-200 dark:border-gray-700">
                  <Label className="text-sm font-medium">Search Term (Optional)</Label>
                  <Input
                    placeholder="Search within logs..."
                    value={exportConfig.searchTerm}
                    onChange={(e) => setExportConfig({...exportConfig, searchTerm: e.target.value})}
                    className="w-full"
                  />
                </div>
              </div>
            </MobileSection>

            {/* Format Settings */}
            <MobileSection
              sectionKey="format"
              title="Export Format"
              icon={<FileText className="h-4 w-4 text-green-500" />}
            >
              <div className="space-y-4 p-3 sm:p-4 rounded-lg bg-gray-50 dark:bg-gray-800/50 border border-gray-200 dark:border-gray-700">
                <RadioGroup
                  value={exportConfig.format}
                  onValueChange={(value) => setExportConfig({...exportConfig, format: value})}
                  className="grid grid-cols-2 gap-2 sm:grid-cols-4 sm:gap-4"
                >
                  {formatOptions.map((format) => (
                    <div key={format.value} className="w-full">
                      <RadioGroupItem
                        value={format.value}
                        id={`format-${format.value}`}
                        className="peer sr-only"
                      />
                      <Label
                        htmlFor={`format-${format.value}`}
                        className="flex flex-col items-center justify-between rounded-lg border-2 border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-3 sm:p-4 hover:bg-gray-50 dark:hover:bg-gray-700 active:bg-gray-100 peer-data-[state=checked]:border-blue-500 dark:peer-data-[state=checked]:border-blue-500 transition-all"
                      >
                        <div className="text-sm sm:text-base font-semibold mb-1">{format.label}</div>
                        <div className="text-xs text-gray-500 dark:text-gray-400 text-center">
                          {format.description}
                        </div>
                      </Label>
                    </div>
                  ))}
                </RadioGroup>
                
                <div className="flex items-center justify-between pt-4 border-t border-gray-200 dark:border-gray-700">
                  <div className="space-y-0.5">
                    <Label className="text-sm font-medium">Include Headers</Label>
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                      Include column names
                    </p>
                  </div>
                  <Switch
                    checked={exportConfig.includeHeaders}
                    onCheckedChange={(checked) => setExportConfig({...exportConfig, includeHeaders: checked})}
                  />
                </div>
              </div>
            </MobileSection>

            {/* Advanced Settings */}
            <MobileSection
              sectionKey="advanced"
              title="Advanced Options"
              icon={<Settings className="h-4 w-4 text-purple-500" />}
            >
              <div className="space-y-4 p-3 sm:p-4 rounded-lg bg-gray-50 dark:bg-gray-800/50 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-sm font-medium">Enable Compression</Label>
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                      Compress file to reduce size
                    </p>
                  </div>
                  <Switch
                    checked={exportConfig.compression}
                    onCheckedChange={(checked) => setExportConfig({...exportConfig, compression: checked})}
                  />
                </div>
                
                <div className="space-y-2 pt-2 border-t border-gray-200 dark:border-gray-700">
                  <Label className="text-sm font-medium">File Name</Label>
                  <Input
                    placeholder="system_logs_export"
                    value={exportConfig.filename}
                    onChange={(e) => setExportConfig({...exportConfig, filename: e.target.value})}
                    className="w-full"
                  />
                  <p className="text-xs text-gray-500 dark:text-gray-400 break-words">
                    File: {exportConfig.filename || "system_logs_export"}.{exportConfig.format}
                    {exportConfig.compression && ".zip"}
                  </p>
                </div>
              </div>
            </MobileSection>
          </div>

          {/* Warning Message - Mobile Optimized */}
          <div className="p-3 sm:p-4 rounded-lg bg-amber-50 dark:bg-amber-900/10 border border-amber-200 dark:border-amber-800/30">
            <div className="flex items-start gap-3">
              <AlertTriangle className="h-4 w-4 sm:h-5 sm:w-5 text-amber-600 dark:text-amber-400 flex-shrink-0 mt-0.5" />
              <div>
                <h5 className="font-medium text-amber-800 dark:text-amber-300 mb-1 text-sm">
                  Export Considerations
                </h5>
                <ul className="text-xs text-amber-700 dark:text-amber-400 space-y-1">
                  <li className="flex items-start gap-1">
                    <span className="mt-0.5">•</span>
                    <span>Large files may take time to export</span>
                  </li>
                  <li className="flex items-start gap-1">
                    <span className="mt-0.5">•</span>
                    <span>Compression reduces size but adds processing time</span>
                  </li>
                  <li className="flex items-start gap-1">
                    <span className="mt-0.5">•</span>
                    <span>Ensure sufficient storage space</span>
                  </li>
                  <li className="flex items-start gap-1">
                    <span className="mt-0.5">•</span>
                    <span>CSV recommended for spreadsheets</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
        
        <Separator className="my-4 bg-gray-200 dark:bg-gray-700" />
        
        <AlertDialogFooter className="flex flex-col-reverse sm:flex-row gap-3 pt-4 px-0">
          <AlertDialogCancel asChild className="w-full sm:w-auto">
            <Button
              variant="outline"
              className="w-full sm:w-auto rounded-lg border-gray-300 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800 active:scale-[0.98] transition-transform"
            >
              <XCircle className="h-4 w-4 mr-2" />
              Cancel
            </Button>
          </AlertDialogCancel>
          
          <AlertDialogAction asChild className="w-full sm:w-auto">
            <Button
              onClick={handleExport}
              disabled={loading}
              className="w-full sm:w-auto rounded-lg bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 active:scale-[0.98] transition-transform"
            >
              {loading ? (
                <>
                  <Clock className="h-4 w-4 mr-2 animate-spin" />
                  <span className="truncate">Preparing Export...</span>
                </>
              ) : (
                <>
                  <Download className="h-4 w-4 mr-2" />
                  <span className="truncate">Export Logs ({estimateFileSize()})</span>
                </>
              )}
            </Button>
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}