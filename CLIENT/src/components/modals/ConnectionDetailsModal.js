// components/modals/ConnectionDetailsModal.js
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
  Database, Server, Globe, Key, Shield, Wifi,
  Activity, CheckCircle, AlertCircle, XCircle, Clock,
  Copy, Settings, Edit, Trash2, ExternalLink,
  Users, HardDrive, Network, Lock, RefreshCw,
  ArrowDown, Zap, Cpu, Eye, EyeOff
} from "lucide-react";

export default function ConnectionDetailsModal({
  showConnectionDetails,
  setShowConnectionDetails,
  selectedConnection,
  handleTestConnection,
  handleViewSchema,
  handleCopyConnection
}) {
  const [showPassword, setShowPassword] = useState(false);
  const [expandedSections, setExpandedSections] = useState({
    basicInfo: true,
    performance: false,
    advanced: false
  });

  if (!selectedConnection) return null;

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
      warning: {
        badge: (
          <Badge className="bg-amber-500/10 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-800/50 text-xs">
            <AlertCircle className="h-3 w-3 mr-1" /> Warning
          </Badge>
        ),
        icon: <AlertCircle className="h-4 w-4 text-amber-600" />
      },
      error: {
        badge: (
          <Badge className="bg-red-500/10 text-red-700 dark:text-red-300 border border-red-200 dark:border-red-800/50 text-xs">
            <XCircle className="h-3 w-3 mr-1" /> Error
          </Badge>
        ),
        icon: <XCircle className="h-4 w-4 text-red-600" />
      }
    };

    return config[status] || config.active;
  };

  const getDatabaseIcon = (type) => {
    const icons = {
      oracle: <Globe className="h-5 w-5" />,
      postgresql: <Server className="h-5 w-5" />,
      mysql: <Database className="h-5 w-5" />,
      sqlserver: <Database className="h-5 w-5" />,
      sqlite: <HardDrive className="h-5 w-5" />
    };
    return icons[type] || <Database className="h-5 w-5" />;
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
    <Dialog open={showConnectionDetails} onOpenChange={setShowConnectionDetails}>
      <DialogContent className="max-w-2xl max-h-[85vh] overflow-y-auto bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl shadow-2xl">
        <DialogHeader className="space-y-3">
          <div className="flex items-center justify-between">
            <DialogTitle className="text-xl font-bold text-gray-900 dark:text-gray-100">
              Database Connection Details
            </DialogTitle>
            <Badge variant="outline" className="text-xs font-mono">
              ID: {selectedConnection.id?.substring(0, 6) || 'N/A'}
            </Badge>
          </div>
          <DialogDescription className="text-sm text-gray-500 dark:text-gray-400">
            Detailed information about this database connection
          </DialogDescription>
        </DialogHeader>
        
        <Separator className="bg-gray-200 dark:bg-gray-700" />
        
        <div className="space-y-6 py-2">
          {/* Header Section */}
          <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 p-4 rounded-lg bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-800/50 dark:to-gray-900/50 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center gap-4">
              <div className={`p-3 rounded-xl ${selectedConnection.color} text-white`}>
                {getDatabaseIcon(selectedConnection.type)}
              </div>
              <div className="space-y-1">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {selectedConnection.name}
                </h3>
                <div className="flex flex-wrap items-center gap-2">
                  {getStatusBadge(selectedConnection.status).badge}
                  <Badge variant="outline" className="text-xs capitalize">
                    {selectedConnection.type}
                  </Badge>
                  {selectedConnection.ssl && (
                    <Badge className="bg-blue-500/10 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800/50 text-xs">
                      <Shield className="h-3 w-3 mr-1" />
                      SSL
                    </Badge>
                  )}
                </div>
              </div>
            </div>
          </div>
          
          {/* Basic Information */}
          {renderMobileSection(
            'basicInfo',
            'Connection Details',
            <Database className="h-3 w-3" />,
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-3 p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Host</p>
                    <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {selectedConnection.host}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Database</p>
                    <div className="flex items-center gap-2">
                      <Database className="h-4 w-4" />
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {selectedConnection.database}
                      </span>
                    </div>
                  </div>
                </div>
                
                <div className="space-y-3 p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Schema</p>
                    <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {selectedConnection.schema || 'Default'}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Version</p>
                    <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {selectedConnection.version || 'N/A'}
                    </span>
                  </div>
                </div>
              </div>
            </>
          )}
          
          {/* Performance */}
          {renderMobileSection(
            'performance',
            'Performance',
            <Activity className="h-3 w-3" />,
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Latency</p>
                <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                  {selectedConnection.latency}
                </p>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Uptime</p>
                <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                  {selectedConnection.uptime}
                </p>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Active Connections</p>
                <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                  {selectedConnection.connections}
                </p>
              </div>
            </div>
          )}
          
          {/* Advanced Settings */}
          {renderMobileSection(
            'advanced',
            'Advanced Settings',
            <Settings className="h-3 w-3" />,
            <div className="space-y-4">
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Connection Pool</p>
                    <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {selectedConnection.poolSize || 10} connections
                    </p>
                  </div>
                  <Users className="h-5 w-5 text-gray-400" />
                </div>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Last Synchronized</p>
                    <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {new Date(selectedConnection.lastSync).toLocaleString()}
                    </p>
                  </div>
                  <Clock className="h-5 w-5 text-gray-400" />
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
              onClick={handleCopyConnection}
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
                onClick={() => handleViewSchema(selectedConnection.id)}
                className="rounded-lg border-blue-300 dark:border-blue-700 hover:bg-blue-50 dark:hover:bg-blue-900/20"
              >
                <Eye className="h-4 w-4 mr-2 text-blue-600 dark:text-blue-400" />
                View Schema
              </Button>
              
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleTestConnection(selectedConnection.id)}
                className="rounded-lg border-green-300 dark:border-green-700 hover:bg-green-50 dark:hover:bg-green-900/20"
              >
                <RefreshCw className="h-4 w-4 mr-2 text-green-600 dark:text-green-400" />
                Test Connection
              </Button>
            </div>
            
            <Separator orientation="vertical" className="h-auto sm:h-6 hidden sm:block" />
            
            <Button
              variant="default"
              onClick={() => setShowConnectionDetails(false)}
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