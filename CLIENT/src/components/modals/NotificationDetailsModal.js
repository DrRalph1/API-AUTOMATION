// components/modals/NotificationDetailsModal.js
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
  Bell, CheckCircle, AlertCircle, AlertTriangle, Info,
  Clock, User, Database, Server, FileCode, Activity,
  Copy, Eye, Trash2, ExternalLink, ArrowDown,
  Zap, Shield, Globe, HardDrive, Network
} from "lucide-react";

export default function NotificationDetailsModal({
  showNotificationDetails,
  setShowNotificationDetails,
  selectedNotification,
  handleMarkAsRead,
  handleClearNotification
}) {
  const [expandedSections, setExpandedSections] = useState({
    details: true,
    context: false,
    actions: false
  });

  if (!selectedNotification) return null;

  const toggleSection = (section) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  const getTypeIcon = (type) => {
    const icons = {
      success: <CheckCircle className="h-5 w-5 text-green-600" />,
      warning: <AlertCircle className="h-5 w-5 text-amber-600" />,
      error: <AlertTriangle className="h-5 w-5 text-red-600" />,
      info: <Info className="h-5 w-5 text-blue-600" />
    };
    return icons[type] || <Bell className="h-5 w-5" />;
  };

  const getTypeBadge = (type) => {
    const colors = {
      success: "bg-green-500/10 text-green-700 dark:text-green-300 border border-green-200 dark:border-green-800/50",
      warning: "bg-amber-500/10 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-800/50",
      error: "bg-red-500/10 text-red-700 dark:text-red-300 border border-red-200 dark:border-red-800/50",
      info: "bg-blue-500/10 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800/50"
    };

    return (
      <Badge className={`text-xs capitalize ${colors[type] || 'bg-gray-500/10 text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-gray-700'}`}>
        {type}
      </Badge>
    );
  };

  const getSourceIcon = (source) => {
    if (!source) return <Bell className="h-4 w-4" />;
    
    if (source.includes('database') || source.includes('db')) return <Database className="h-4 w-4" />;
    if (source.includes('api')) return <FileCode className="h-4 w-4" />;
    if (source.includes('server')) return <Server className="h-4 w-4" />;
    if (source.includes('security')) return <Shield className="h-4 w-4" />;
    if (source.includes('system')) return <Activity className="h-4 w-4" />;
    return <Bell className="h-4 w-4" />;
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
    <Dialog open={showNotificationDetails} onOpenChange={setShowNotificationDetails}>
      <DialogContent className="max-w-2xl max-h-[85vh] overflow-y-auto bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl shadow-2xl">
        <DialogHeader className="space-y-3">
          <div className="flex items-center justify-between">
            <DialogTitle className="text-xl font-bold text-gray-900 dark:text-gray-100">
              Notification Details
            </DialogTitle>
            <Badge variant="outline" className="text-xs font-mono">
              ID: {selectedNotification.id || 'N/A'}
            </Badge>
          </div>
          <DialogDescription className="text-sm text-gray-500 dark:text-gray-400">
            Detailed information about this notification
          </DialogDescription>
        </DialogHeader>
        
        <Separator className="bg-gray-200 dark:bg-gray-700" />
        
        <div className="space-y-6 py-2">
          {/* Header Section */}
          <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 p-4 rounded-lg bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-800/50 dark:to-gray-900/50 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center gap-4">
              <div className="p-3 rounded-xl bg-gray-100 dark:bg-gray-800">
                {getTypeIcon(selectedNotification.type)}
              </div>
              <div className="space-y-1">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {selectedNotification.title}
                </h3>
                <div className="flex flex-wrap items-center gap-2">
                  {getTypeBadge(selectedNotification.type)}
                  {!selectedNotification.read && (
                    <Badge className="bg-red-500/10 text-red-700 dark:text-red-300 border border-red-200 dark:border-red-800/50 text-xs">
                      Unread
                    </Badge>
                  )}
                  <Badge variant="outline" className="text-xs">
                    {selectedNotification.time}
                  </Badge>
                </div>
              </div>
            </div>
          </div>
          
          {/* Details */}
          {renderMobileSection(
            'details',
            'Notification Details',
            <Bell className="h-3 w-3" />,
            <>
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Message</p>
                <p className="text-sm text-gray-700 dark:text-gray-300">
                  {selectedNotification.message}
                </p>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Time Received</p>
                  <div className="flex items-center gap-2">
                    <Clock className="h-4 w-4" />
                    <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {selectedNotification.time}
                    </span>
                  </div>
                </div>
                
                <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Source</p>
                  <div className="flex items-center gap-2">
                    {getSourceIcon(selectedNotification.source)}
                    <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {selectedNotification.source || 'System'}
                    </span>
                  </div>
                </div>
              </div>
            </>
          )}
          
          {/* Context */}
          {renderMobileSection(
            'context',
            'Context Information',
            <Activity className="h-3 w-3" />,
            <div className="space-y-4">
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <p className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">Related Resources</p>
                <div className="space-y-2">
                  <div className="flex items-center justify-between p-2 rounded bg-white dark:bg-gray-800">
                    <div className="flex items-center gap-2">
                      <Database className="h-4 w-4" />
                      <span className="text-xs">Production Database</span>
                    </div>
                    <Badge variant="outline" className="text-xs">Oracle</Badge>
                  </div>
                  <div className="flex items-center justify-between p-2 rounded bg-white dark:bg-gray-800">
                    <div className="flex items-center gap-2">
                      <FileCode className="h-4 w-4" />
                      <span className="text-xs">Customer Orders API</span>
                    </div>
                    <Badge variant="outline" className="text-xs">GET</Badge>
                  </div>
                </div>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <p className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">Severity Level</p>
                <div className="flex items-center gap-2">
                  {getTypeIcon(selectedNotification.type)}
                  <span className="text-sm text-gray-700 dark:text-gray-300 capitalize">
                    {selectedNotification.type} - {selectedNotification.type === 'error' ? 'Requires Immediate Attention' : 
                      selectedNotification.type === 'warning' ? 'Monitor Closely' : 
                      selectedNotification.type === 'info' ? 'Informational Only' : 'Normal Operation'}
                  </span>
                </div>
              </div>
            </div>
          )}
          
          {/* Actions */}
          {renderMobileSection(
            'actions',
            'Actions',
            <Zap className="h-3 w-3" />,
            <div className="grid grid-cols-2 gap-3">
              <Button
                variant="outline"
                onClick={() => selectedNotification.action && alert(`Executing: ${selectedNotification.action}`)}
                className="flex items-center justify-center gap-2 py-3"
              >
                <Activity className="h-4 w-4" />
                {selectedNotification.action || 'Take Action'}
              </Button>
              
              <Button
                variant="outline"
                onClick={() => alert('Viewing related logs...')}
                className="flex items-center justify-center gap-2 py-3"
              >
                <Eye className="h-4 w-4" />
                View Logs
              </Button>
              
              {!selectedNotification.read && (
                <Button
                  variant="outline"
                  onClick={handleMarkAsRead}
                  className="flex items-center justify-center gap-2 py-3"
                >
                  <CheckCircle className="h-4 w-4" />
                  Mark as Read
                </Button>
              )}
              
              <Button
                variant="outline"
                onClick={handleClearNotification}
                className="flex items-center justify-center gap-2 py-3"
              >
                <Trash2 className="h-4 w-4" />
                Dismiss
              </Button>
            </div>
          )}
        </div>
        
        <DialogFooter className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-4 border-t border-gray-200 dark:border-gray-800">
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => navigator.clipboard.writeText(selectedNotification.message)}
              className="rounded-lg border-gray-300 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              <Copy className="h-4 w-4 mr-2" />
              Copy Message
            </Button>
          </div>
          
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
            <Button
              variant="default"
              onClick={() => setShowNotificationDetails(false)}
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