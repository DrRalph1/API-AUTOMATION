// components/modals/LogFileViewModal.js
import React, { useState } from "react";
import { showSuccess, showError, showConfirm } from "@/lib/sweetAlert";
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
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  FileText, Search, Download, Copy, Clock, 
  AlertTriangle, Info, FileCode, Filter,
  XCircle, RefreshCw, CheckCircle, Eye, EyeOff,
  ArrowDown, Folder, Calendar, Hash, Server
} from "lucide-react";
import { toast } from "sonner";

// Move formatFileSize function to the top (before the component)
const formatFileSize = (bytes) => {
  if (!bytes) return "0 Bytes";
  
  // Handle string sizes like "1.5 MB"
  if (typeof bytes === 'string' && bytes.includes(' ')) {
    return bytes;
  }
  
  // Convert string numbers to numbers
  const numBytes = Number(bytes);
  if (isNaN(numBytes) || numBytes === 0) return "0 Bytes";
  
  const k = 1024;
  const sizes = ["Bytes", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(numBytes) / Math.log(k));
  return parseFloat((numBytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
};

// Helper function to parse log line
const parseLogLine = (line) => {
  const timestampMatch = line.match(/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/);
  const severityMatch = line.match(/\[(ERROR|WARN|INFO|DEBUG|TRACE)\]/i);
  const moduleMatch = line.match(/\[([^\]]+)\]/);
  
  return {
    timestamp: timestampMatch ? timestampMatch[0] : null,
    severity: severityMatch ? severityMatch[1].toUpperCase() : "UNKNOWN",
    module: moduleMatch && !severityMatch ? moduleMatch[1] : null,
    message: line.trim(),
    metadata: line.length > 200 ? line.substring(0, 200) + "..." : line
  };
};

// Helper function to count severities
const countSeverities = (lines) => {
  const counts = { ERROR: 0, WARN: 0, INFO: 0, DEBUG: 0, TRACE: 0 };
  lines.forEach((line) => {
    const upperLine = line.toUpperCase();
    if (upperLine.includes("[ERROR]")) counts.ERROR++;
    else if (upperLine.includes("[WARN]")) counts.WARN++;
    else if (upperLine.includes("[INFO]")) counts.INFO++;
    else if (upperLine.includes("[DEBUG]")) counts.DEBUG++;
    else if (upperLine.includes("[TRACE]")) counts.TRACE++;
  });
  return counts;
};

export default function LogFileViewModal({
  showLogFileModal,
  setShowLogFileModal,
  logFileContent,
  fileName = "",
  fileSize = 0,
  lastModified = "",
  searchTerm = "",
  onSearch,
  loading = false,
  formatDate
}) {
  const [expandedSections, setExpandedSections] = useState({
    fileInfo: true,
    content: true,
    search: false
  });

  const [searchInput, setSearchInput] = useState(searchTerm);
  const [viewMode, setViewMode] = useState("raw"); // 'raw' or 'parsed'

  if (!showLogFileModal) return null;

  const lines = logFileContent?.split("\n") || [];
  const lineCount = lines.length;
  const fileInfo = {
    name: fileName,
    size: formatFileSize(fileSize),
    lines: lineCount,
    modified: formatDate?.(lastModified) || lastModified,
    extension: fileName.split('.').pop()?.toUpperCase() || 'LOG'
  };

  const severityCounts = countSeverities(lines);
  const logLevels = ["ERROR", "WARN", "INFO", "DEBUG", "TRACE"];

  const handleSearch = () => {
    if (onSearch) {
      onSearch(searchInput);
    }
  };

  const handleCopyContent = async () => {
    try {
      await navigator.clipboard.writeText(logFileContent);
      toast.success("Log content copied to clipboard");
    } catch (error) {
      toast.error("Failed to copy content");
    }
  };

  const handleDownload = () => {
    const blob = new Blob([logFileContent], { type: "text/plain" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    toast.success("Log file downloaded");
  };

  const getSeverityColor = (severity) => {
    const colors = {
      ERROR: "bg-red-500",
      WARN: "bg-amber-500",
      INFO: "bg-blue-500",
      DEBUG: "bg-purple-500",
      TRACE: "bg-gray-500",
    };
    return colors[severity] || "bg-gray-500";
  };

  const filteredLines = searchInput 
    ? lines.filter(line => line.toLowerCase().includes(searchInput.toLowerCase()))
    : lines;

  const renderMobileSection = (sectionKey, title, icon, children) => (
    <div className="space-y-3 md:space-y-4">
      <button
        onClick={() => setExpandedSections(prev => ({
          ...prev,
          [sectionKey]: !prev[sectionKey]
        }))}
        className="flex items-center justify-between w-full p-3 rounded-lg bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors"
      >
        <div className="flex items-center gap-2">
          {icon}
          <Label className="text-xs font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400">
            {title}
          </Label>
        </div>
        <ArrowDown className={`h-4 w-4 text-gray-500 transition-transform ${
          expandedSections[sectionKey] ? "rotate-180" : ""
        }`} />
      </button>
      
      <div className={`${expandedSections[sectionKey] ? "block" : "hidden"}`}>
        {children}
      </div>
    </div>
  );

  return (
    <Dialog open={showLogFileModal} onOpenChange={setShowLogFileModal}>
      <DialogContent className="max-w-4xl max-h-[85vh] overflow-y-auto bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl shadow-2xl">
        <DialogHeader className="space-y-3">
          <div className="flex items-center justify-between">
            <DialogTitle className="text-xl font-bold text-gray-900 dark:text-gray-100">
              Log File Viewer
            </DialogTitle>
            {/* <Badge variant="outline" className="font-mono bg-gray-100 dark:bg-gray-800">
              {fileInfo.extension}
            </Badge> */}
          </div>
          <DialogDescription className="text-sm text-gray-500 dark:text-gray-400">
            Viewing log file: {fileName}
          </DialogDescription>
        </DialogHeader>
        
        <Separator className="bg-gray-200 dark:bg-gray-700" />
        
        <div className="space-y-6 py-2">
          {/* File Header */}
          <div className="flex flex-col md:flex-row md:items-start justify-between gap-4 p-4 rounded-lg bg-gradient-to-r from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 border border-blue-200 dark:border-blue-800">
            <div className="flex items-center gap-4">
              <div className="p-3 rounded-xl bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">
                <FileText className="h-8 w-8" />
              </div>
              <div className="space-y-1">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 truncate max-w-md">
                  {fileName}
                </h3>
                <div className="flex flex-wrap items-center gap-3">
                  <div className="flex items-center gap-1 text-sm text-gray-600 dark:text-gray-400">
                    <Clock className="h-3 w-3" />
                    <span>{fileInfo.modified}</span>
                  </div>
                  <div className="flex items-center gap-1 text-sm text-gray-600 dark:text-gray-400">
                    <Hash className="h-3 w-3" />
                    <span>{fileInfo.lines} lines</span>
                  </div>
                  <div className="flex items-center gap-1 text-sm text-gray-600 dark:text-gray-400">
                    <Server className="h-3 w-3" />
                    <span>{fileInfo.size}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="flex flex-wrap gap-2">
              {logLevels.map((level) => (
                severityCounts[level] > 0 && (
                  <Badge
                    key={level}
                    className={`${getSeverityColor(level)} text-white`}
                  >
                    {level}: {severityCounts[level]}
                  </Badge>
                )
              ))}
            </div>
          </div>

          {/* Search Section */}
          {renderMobileSection(
            "search",
            "Search & Filter",
            <Search className="h-3 w-3" />,
            <div className="space-y-4 p-4 rounded-lg bg-gray-50 dark:bg-gray-800/50 border border-gray-200 dark:border-gray-700">
              <div className="flex flex-col sm:flex-row gap-3">
                <div className="flex-1">
                  <Input
                    placeholder="Search within log file..."
                    value={searchInput}
                    onChange={(e) => setSearchInput(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                    className="border-gray-300 dark:border-gray-700"
                  />
                </div>
                <div className="flex gap-2">
                  <Button
                    onClick={handleSearch}
                    className="bg-blue-600 hover:bg-blue-700"
                    disabled={loading}
                  >
                    {loading ? (
                      <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                    ) : (
                      <Search className="h-4 w-4 mr-2" />
                    )}
                    Search
                  </Button>
                  <Button
                    variant="outline"
                    onClick={() => {
                      setSearchInput("");
                      onSearch?.("");
                    }}
                  >
                    <XCircle className="h-4 w-4 mr-2" />
                    Clear
                  </Button>
                </div>
              </div>
              
              {searchInput && (
                <div className="flex items-center justify-between p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                  <div className="flex items-center gap-2">
                    <Filter className="h-4 w-4 text-blue-600 dark:text-blue-400" />
                    <span className="text-sm text-blue-700 dark:text-blue-300">
                      Found {filteredLines.length} of {lines.length} lines
                    </span>
                  </div>
                  <Badge variant="outline" className="bg-white dark:bg-gray-800">
                    "{searchInput}"
                  </Badge>
                </div>
              )}
            </div>
          )}

          {/* File Content */}
          {renderMobileSection(
            "content",
            "Log Content",
            <FileCode className="h-3 w-3" />,
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <Tabs defaultValue="raw" className="w-full" onValueChange={setViewMode}>
                    <TabsList>
                      <TabsTrigger value="raw" className="text-xs">
                        <Eye className="h-4 w-4 mr-2" />
                        Raw View
                      </TabsTrigger>
                      <TabsTrigger value="parsed" className="text-xs">
                        <FileText className="h-4 w-4 mr-2" />
                        Parsed View
                      </TabsTrigger>
                    </TabsList>
                  </Tabs>
                </div>
                
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={handleCopyContent}
                    className="border-gray-300 dark:border-gray-700"
                  >
                    <Copy className="h-4 w-4 mr-2" />
                    Copy
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={handleDownload}
                    className="border-gray-300 dark:border-gray-700"
                  >
                    <Download className="h-4 w-4 mr-2" />
                    Download
                  </Button>
                </div>
              </div>

              <div className="rounded-lg border border-gray-200 dark:border-gray-700 overflow-hidden">
                {viewMode === "raw" ? (
                  <div className="relative">
                    <div className="absolute left-0 top-0 bottom-0 w-12 bg-gray-50 dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 flex flex-col items-center pt-4">
                      {Array.from({ length: Math.min(100, filteredLines.length) }, (_, i) => (
                        <div
                          key={i}
                          className="text-xs text-gray-500 dark:text-gray-400 font-mono py-0.5"
                        >
                          {i + 1}
                        </div>
                      ))}
                    </div>
                    
                    <pre className="max-h-[60vh] overflow-y-auto bg-gray-50 dark:bg-gray-900 p-4 pl-16 text-sm font-mono whitespace-pre-wrap">
                      {filteredLines.slice(0, 1000).map((line, index) => {
                        const lineLower = line.toLowerCase();
                        let className = "text-gray-700 dark:text-gray-300";
                        
                        if (lineLower.includes("[error]")) className = "text-red-600 dark:text-red-400";
                        else if (lineLower.includes("[warn]")) className = "text-amber-600 dark:text-amber-400";
                        else if (lineLower.includes("[info]")) className = "text-blue-600 dark:text-blue-400";
                        else if (lineLower.includes("[debug]")) className = "text-purple-600 dark:text-purple-400";
                        else if (lineLower.includes("[trace]")) className = "text-gray-500 dark:text-gray-500";
                        
                        if (searchInput && line.toLowerCase().includes(searchInput.toLowerCase())) {
                          className += " bg-yellow-100 dark:bg-yellow-900/30";
                        }
                        
                        return (
                          <div key={index} className={className}>
                            {line}
                          </div>
                        );
                      })}
                      
                      {filteredLines.length > 1000 && (
                        <div className="text-center py-4 text-gray-500 dark:text-gray-400 italic">
                          Showing first 1000 of {filteredLines.length} lines...
                        </div>
                      )}
                    </pre>
                  </div>
                ) : (
                  <div className="max-h-[60vh] overflow-y-auto p-4 bg-white dark:bg-gray-900">
                    {filteredLines.slice(0, 100).map((line, index) => {
                      const parsed = parseLogLine(line);
                      return (
                        <div
                          key={index}
                          className="mb-3 p-3 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800"
                        >
                          <div className="flex items-center justify-between mb-2">
                            <div className="flex items-center gap-2">
                              <Badge className={getSeverityColor(parsed.severity)}>
                                {parsed.severity}
                              </Badge>
                              <span className="text-xs text-gray-500 dark:text-gray-400 font-mono">
                                {parsed.timestamp || "Unknown time"}
                              </span>
                            </div>
                            {parsed.module && (
                              <Badge variant="outline" className="text-xs">
                                {parsed.module}
                              </Badge>
                            )}
                          </div>
                          <p className="text-sm text-gray-700 dark:text-gray-300 mb-2">
                            {parsed.message}
                          </p>
                          {parsed.metadata && (
                            <div className="text-xs text-gray-500 dark:text-gray-400">
                              {parsed.metadata}
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>

              {lines.length > 0 && (
                <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/50 border border-gray-200 dark:border-gray-700">
                  <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                    {logLevels.map((level) => (
                      <div key={level} className="text-center">
                        <div className={`text-sm font-semibold ${getSeverityColor(level).replace('bg-', 'text-')}`}>
                          {level}
                        </div>
                        <div className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                          {severityCounts[level]}
                        </div>
                        <div className="text-xs text-gray-500 dark:text-gray-400">
                          {((severityCounts[level] / lines.length) * 100).toFixed(1)}%
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* File Info */}
          {renderMobileSection(
            "fileInfo",
            "File Information",
            <Info className="h-3 w-3" />,
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 p-4 rounded-lg bg-gray-50 dark:bg-gray-800/50 border border-gray-200 dark:border-gray-700">
              <div className="space-y-3">
                <div>
                  <Label className="text-xs text-gray-500 dark:text-gray-400">File Name</Label>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100 truncate">
                    {fileInfo.name}
                  </p>
                </div>
                <div>
                  <Label className="text-xs text-gray-500 dark:text-gray-400">File Size</Label>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {fileInfo.size}
                  </p>
                </div>
                <div>
                  <Label className="text-xs text-gray-500 dark:text-gray-400">Total Lines</Label>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {fileInfo.lines.toLocaleString()}
                  </p>
                </div>
              </div>
              <div className="space-y-3">
                <div>
                  <Label className="text-xs text-gray-500 dark:text-gray-400">Last Modified</Label>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {fileInfo.modified}
                  </p>
                </div>
                <div>
                  <Label className="text-xs text-gray-500 dark:text-gray-400">File Type</Label>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {fileInfo.extension} File
                  </p>
                </div>
                <div>
                  <Label className="text-xs text-gray-500 dark:text-gray-400">Errors/Warnings</Label>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {severityCounts.ERROR} errors, {severityCounts.WARN} warnings
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>

        <DialogFooter className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-4 border-t border-gray-200 dark:border-gray-800">
          <div className="flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400">
            <AlertTriangle className="h-4 w-4" />
            <span>Log files contain sensitive information. Handle with care.</span>
          </div>
          
          <div className="flex gap-2">
            <Button
              variant="outline"
              onClick={handleDownload}
              className="border-gray-300 dark:border-gray-700"
            >
              <Download className="h-4 w-4 mr-2" />
              Download Log
            </Button>
            <Button
              onClick={() => setShowLogFileModal(false)}
              className="bg-blue-600 hover:bg-blue-700"
            >
              <CheckCircle className="h-4 w-4 mr-2" />
              Close Viewer
            </Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}