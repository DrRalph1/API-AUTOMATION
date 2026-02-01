// components/modals/ReportDetailsModal.js
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
  FileText, FileJson, FileCode, Download, Share2,
  Calendar, Clock, Database, Activity, BarChart3,
  Copy, Eye, Trash2, RefreshCw, ExternalLink,
  ArrowDown, HardDrive, Users, Zap, TrendingUp
} from "lucide-react";

export default function ReportDetailsModal({
  showReportDetails,
  setShowReportDetails,
  selectedReport,
  handleDownloadReport,
  handleShareReport
}) {
  const [expandedSections, setExpandedSections] = useState({
    basicInfo: true,
    statistics: false,
    actions: false
  });

  if (!selectedReport) return null;

  const toggleSection = (section) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  const getFileIcon = (type) => {
    const icons = {
      pdf: <FileText className="h-5 w-5 text-red-500" />,
      csv: <FileText className="h-5 w-5 text-green-500" />,
      json: <FileJson className="h-5 w-5 text-amber-500" />,
      xml: <FileCode className="h-5 w-5 text-blue-500" />
    };
    return icons[type] || <FileText className="h-5 w-5" />;
  };

  const getFileTypeBadge = (type) => {
    const colors = {
      pdf: "bg-red-500/10 text-red-700 dark:text-red-300 border border-red-200 dark:border-red-800/50",
      csv: "bg-green-500/10 text-green-700 dark:text-green-300 border border-green-200 dark:border-green-800/50",
      json: "bg-amber-500/10 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-800/50",
      xml: "bg-blue-500/10 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800/50"
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
    <Dialog open={showReportDetails} onOpenChange={setShowReportDetails}>
      <DialogContent className="max-w-2xl max-h-[85vh] overflow-y-auto bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl shadow-2xl">
        <DialogHeader className="space-y-3">
          <div className="flex items-center justify-between">
            <DialogTitle className="text-xl font-bold text-gray-900 dark:text-gray-100">
              Report Details
            </DialogTitle>
            <Badge variant="outline" className="text-xs font-mono">
              ID: {selectedReport.id?.substring(0, 6) || 'N/A'}
            </Badge>
          </div>
          <DialogDescription className="text-sm text-gray-500 dark:text-gray-400">
            Detailed information about this report
          </DialogDescription>
        </DialogHeader>
        
        <Separator className="bg-gray-200 dark:bg-gray-700" />
        
        <div className="space-y-6 py-2">
          {/* Header Section */}
          <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 p-4 rounded-lg bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-800/50 dark:to-gray-900/50 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center gap-4">
              <div className="p-3 rounded-xl bg-gray-100 dark:bg-gray-800">
                {getFileIcon(selectedReport.type)}
              </div>
              <div className="space-y-1">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {selectedReport.name}
                </h3>
                <div className="flex flex-wrap items-center gap-2">
                  {getFileTypeBadge(selectedReport.type)}
                  <Badge variant="outline" className="text-xs">
                    {selectedReport.period}
                  </Badge>
                  <Badge className="bg-blue-500/10 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800/50 text-xs">
                    <HardDrive className="h-3 w-3 mr-1" />
                    {selectedReport.size}
                  </Badge>
                </div>
              </div>
            </div>
          </div>
          
          {/* Basic Information */}
          {renderMobileSection(
            'basicInfo',
            'Report Details',
            <FileText className="h-3 w-3" />,
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-3 p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Generated On</p>
                    <div className="flex items-center gap-2">
                      <Calendar className="h-4 w-4" />
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {new Date(selectedReport.generated).toLocaleDateString()}
                      </span>
                    </div>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Generation Time</p>
                    <div className="flex items-center gap-2">
                      <Clock className="h-4 w-4" />
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {new Date(selectedReport.generated).toLocaleTimeString()}
                      </span>
                    </div>
                  </div>
                </div>
                
                <div className="space-y-3 p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Report Period</p>
                    <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {selectedReport.period}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">File Size</p>
                    <div className="flex items-center gap-2">
                      <HardDrive className="h-4 w-4" />
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {selectedReport.size}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Report Type</p>
                <p className="text-sm text-gray-700 dark:text-gray-300">
                  {selectedReport.type.toUpperCase()} report containing analytics and metrics for the {selectedReport.period.toLowerCase()} period.
                </p>
              </div>
            </>
          )}
          
          {/* Statistics */}
          {renderMobileSection(
            'statistics',
            'Report Statistics',
            <BarChart3 className="h-3 w-3" />,
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-xs text-gray-500 dark:text-gray-400">Data Points</p>
                  <Database className="h-4 w-4 text-gray-400" />
                </div>
                <p className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {Math.floor(Math.random() * 10000).toLocaleString()}
                </p>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-xs text-gray-500 dark:text-gray-400">API Calls</p>
                  <Activity className="h-4 w-4 text-gray-400" />
                </div>
                <p className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {Math.floor(Math.random() * 5000).toLocaleString()}
                </p>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-xs text-gray-500 dark:text-gray-400">Performance</p>
                  <TrendingUp className="h-4 w-4 text-gray-400" />
                </div>
                <p className="text-lg font-semibold text-green-600 dark:text-green-400">
                  +{Math.floor(Math.random() * 20)}%
                </p>
              </div>
            </div>
          )}
          
          {/* Actions */}
          {renderMobileSection(
            'actions',
            'Quick Actions',
            <Zap className="h-3 w-3" />,
            <div className="grid grid-cols-2 gap-3">
              <Button
                variant="outline"
                onClick={() => handleDownloadReport(selectedReport)}
                className="flex items-center justify-center gap-2 py-3"
              >
                <Download className="h-4 w-4" />
                Download
              </Button>
              
              <Button
                variant="outline"
                onClick={() => handleShareReport(selectedReport)}
                className="flex items-center justify-center gap-2 py-3"
              >
                <Share2 className="h-4 w-4" />
                Share
              </Button>
              
              <Button
                variant="outline"
                onClick={() => alert('Preview opened')}
                className="flex items-center justify-center gap-2 py-3"
              >
                <Eye className="h-4 w-4" />
                Preview
              </Button>
              
              <Button
                variant="outline"
                onClick={() => alert('Regenerating report...')}
                className="flex items-center justify-center gap-2 py-3"
              >
                <RefreshCw className="h-4 w-4" />
                Regenerate
              </Button>
            </div>
          )}
        </div>
        
        <DialogFooter className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-4 border-t border-gray-200 dark:border-gray-800">
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => navigator.clipboard.writeText(selectedReport.name)}
              className="rounded-lg border-gray-300 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              <Copy className="h-4 w-4 mr-2" />
              Copy Name
            </Button>
          </div>
          
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
            <Button
              variant="default"
              onClick={() => setShowReportDetails(false)}
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