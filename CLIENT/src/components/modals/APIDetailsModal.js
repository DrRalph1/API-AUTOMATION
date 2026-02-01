// components/modals/APIDetailsModal.js
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
  FileCode, Globe, Server, Database, Key, Shield,
  Activity, CheckCircle, AlertCircle, Clock, Zap,
  Copy, Settings, Edit, Trash2, Terminal, ExternalLink,
  ArrowDown, Users, BarChart3, Wifi, BookOpen
} from "lucide-react";

export default function APIDetailsModal({
  showAPIDetails,
  setShowAPIDetails,
  selectedAPI,
  handleCopyEndpoint,
  handleGenerateCurl,
  handleManageApi
}) {
  const [expandedSections, setExpandedSections] = useState({
    basicInfo: true,
    performance: false,
    security: false
  });

  if (!selectedAPI) return null;

  const toggleSection = (section) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  const getStatusBadge = (status) => {
    const config = {
      active: {
        badge: (
          <Badge className="bg-green-500/10 text-green-700 dark:text-green-300 border border-green-200 dark:border-green-800/50 text-xs">
            <CheckCircle className="h-3 w-3 mr-1" /> Active
          </Badge>
        ),
        icon: <CheckCircle className="h-4 w-4 text-green-600" />
      },
      testing: {
        badge: (
          <Badge className="bg-blue-500/10 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800/50 text-xs">
            <Clock className="h-3 w-3 mr-1" /> Testing
          </Badge>
        ),
        icon: <Clock className="h-4 w-4 text-blue-600" />
      },
      inactive: {
        badge: (
          <Badge className="bg-gray-500/10 text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-gray-700 text-xs">
            <AlertCircle className="h-3 w-3 mr-1" /> Inactive
          </Badge>
        ),
        icon: <AlertCircle className="h-4 w-4 text-gray-600" />
      }
    };

    return config[status] || config.active;
  };

  const getMethodBadge = (method) => {
    const colors = {
      GET: "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800",
      POST: "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400 border border-blue-200 dark:border-blue-800",
      PUT: "bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400 border border-amber-200 dark:border-amber-800",
      DELETE: "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400 border border-red-200 dark:border-red-800",
      PATCH: "bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400 border border-purple-200 dark:border-purple-800"
    };

    return (
      <Badge className={`px-2.5 py-1 rounded-md text-xs font-medium ${colors[method] || 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-400 border border-gray-200 dark:border-gray-700'}`}>
        {method}
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
    <Dialog open={showAPIDetails} onOpenChange={setShowAPIDetails}>
      <DialogContent className="max-w-2xl max-h-[85vh] overflow-y-auto bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl shadow-2xl">
        <DialogHeader className="space-y-3">
          <div className="flex items-center justify-between">
            <DialogTitle className="text-xl font-bold text-gray-900 dark:text-gray-100">
              API Details
            </DialogTitle>
            <Badge variant="outline" className="text-xs font-mono">
              ID: {selectedAPI.id?.substring(0, 6) || 'N/A'}
            </Badge>
          </div>
          <DialogDescription className="text-sm text-gray-500 dark:text-gray-400">
            Detailed information about this API endpoint
          </DialogDescription>
        </DialogHeader>
        
        <Separator className="bg-gray-200 dark:bg-gray-700" />
        
        <div className="space-y-6 py-2">
          {/* Header Section */}
          <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 p-4 rounded-lg bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-800/50 dark:to-gray-900/50 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center gap-4">
              <div className="p-3 rounded-xl bg-emerald-50 dark:bg-emerald-900/20 text-emerald-600 dark:text-emerald-400">
                <FileCode className="h-5 w-5" />
              </div>
              <div className="space-y-1">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {selectedAPI.name}
                </h3>
                <div className="flex flex-wrap items-center gap-2">
                  {getStatusBadge(selectedAPI.status).badge}
                  {getMethodBadge(selectedAPI.method)}
                  <Badge variant="outline" className="text-xs capitalize">
                    {selectedAPI.database}
                  </Badge>
                </div>
              </div>
            </div>
          </div>
          
          {/* Basic Information */}
          {renderMobileSection(
            'basicInfo',
            'API Details',
            <FileCode className="h-3 w-3" />,
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-3 p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Endpoint</p>
                    <div className="flex items-center gap-2">
                      <span className="font-mono text-sm text-gray-900 dark:text-gray-100">
                        {selectedAPI.endpoint}
                      </span>
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => handleCopyEndpoint(selectedAPI.endpoint)}
                        className="h-6 w-6 p-0"
                      >
                        <Copy className="h-3 w-3" />
                      </Button>
                    </div>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Description</p>
                    <p className="text-sm text-gray-700 dark:text-gray-300">
                      {selectedAPI.description || 'No description provided'}
                    </p>
                  </div>
                </div>
                
                <div className="space-y-3 p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Database Source</p>
                    <div className="flex items-center gap-2">
                      <Database className="h-4 w-4" />
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {selectedAPI.database}
                      </span>
                    </div>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Last Call</p>
                    <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {new Date(selectedAPI.lastCall).toLocaleString()}
                    </span>
                  </div>
                </div>
              </div>
            </>
          )}
          
          {/* Performance */}
          {renderMobileSection(
            'performance',
            'Performance Metrics',
            <Activity className="h-3 w-3" />,
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-xs text-gray-500 dark:text-gray-400">Total Calls</p>
                  <Users className="h-4 w-4 text-gray-400" />
                </div>
                <p className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {selectedAPI.calls.toLocaleString()}
                </p>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-xs text-gray-500 dark:text-gray-400">Average Latency</p>
                  <Zap className="h-4 w-4 text-gray-400" />
                </div>
                <p className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {selectedAPI.latency}
                </p>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-xs text-gray-500 dark:text-gray-400">Success Rate</p>
                  <BarChart3 className="h-4 w-4 text-gray-400" />
                </div>
                <p className="text-lg font-semibold text-green-600 dark:text-green-400">
                  {selectedAPI.successRate || '99.8%'}
                </p>
              </div>
            </div>
          )}
          
          {/* Security */}
          {renderMobileSection(
            'security',
            'Security',
            <Shield className="h-3 w-3" />,
            <div className="space-y-4">
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Authentication</p>
                    <div className="flex items-center gap-2">
                      <Key className="h-4 w-4" />
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {selectedAPI.auth}
                      </span>
                    </div>
                  </div>
                  <Shield className="h-5 w-5 text-gray-400" />
                </div>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Rate Limit</p>
                    <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {selectedAPI.rateLimit}
                    </p>
                  </div>
                  <Wifi className="h-5 w-5 text-gray-400" />
                </div>
              </div>
            </div>
          )}
        </div>
        
        <DialogFooter className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-4 border-t border-gray-200 dark:border-gray-800">
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => handleGenerateCurl(selectedAPI)}
              className="rounded-lg border-gray-300 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              <Terminal className="h-4 w-4 mr-2" />
              cURL Command
            </Button>
          </div>
          
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
            <div className="flex flex-row items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleManageApi(selectedAPI.id)}
                className="rounded-lg border-blue-300 dark:border-blue-700 hover:bg-blue-50 dark:hover:bg-blue-900/20"
              >
                <Settings className="h-4 w-4 mr-2 text-blue-600 dark:text-blue-400" />
                Manage API
              </Button>
              
              <Button
                variant="outline"
                size="sm"
                onClick={() => alert('API documentation opened')}
                className="rounded-lg border-emerald-300 dark:border-emerald-700 hover:bg-emerald-50 dark:hover:bg-emerald-900/20"
              >
                <BookOpen className="h-4 w-4 mr-2 text-emerald-600 dark:text-emerald-400" />
                Documentation
              </Button>
            </div>
            
            <Separator orientation="vertical" className="h-auto sm:h-6 hidden sm:block" />
            
            <Button
              variant="default"
              onClick={() => setShowAPIDetails(false)}
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