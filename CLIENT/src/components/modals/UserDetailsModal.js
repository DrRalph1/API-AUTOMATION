// components/modals/UserDetailsModal.js
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
  User, Mail, Shield, Key, Calendar, Clock,
  Activity, CheckCircle, XCircle, AlertCircle, Users,
  Copy, Settings, Edit, Trash2, ExternalLink,
  ArrowDown, Database, Server, Globe, FileCode
} from "lucide-react";

export default function UserDetailsModal({
  showUserDetails,
  setShowUserDetails,
  selectedUser,
  handleResetPassword,
  handleEditUser
}) {
  const [expandedSections, setExpandedSections] = useState({
    basicInfo: true,
    permissions: false,
    activity: false
  });

  if (!selectedUser) return null;

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
      inactive: {
        badge: (
          <Badge className="bg-gray-500/10 text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-gray-700 text-xs">
            <XCircle className="h-3 w-3 mr-1" /> Inactive
          </Badge>
        ),
        icon: <XCircle className="h-4 w-4 text-gray-600" />
      },
      suspended: {
        badge: (
          <Badge className="bg-red-500/10 text-red-700 dark:text-red-300 border border-red-200 dark:border-red-800/50 text-xs">
            <AlertCircle className="h-3 w-3 mr-1" /> Suspended
          </Badge>
        ),
        icon: <AlertCircle className="h-4 w-4 text-red-600" />
      }
    };

    return config[status] || config.active;
  };

  const getRoleBadge = (role) => {
    const colors = {
      admin: "bg-red-500/10 text-red-700 dark:text-red-300 border border-red-200 dark:border-red-800/50",
      developer: "bg-blue-500/10 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800/50",
      viewer: "bg-green-500/10 text-green-700 dark:text-green-300 border border-green-200 dark:border-green-800/50",
      user: "bg-gray-500/10 text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-gray-700"
    };

    return (
      <Badge className={`text-xs ${colors[role] || 'bg-gray-500/10 text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-gray-700'}`}>
        {role.toUpperCase()}
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
    <Dialog open={showUserDetails} onOpenChange={setShowUserDetails}>
      <DialogContent className="max-w-2xl max-h-[85vh] overflow-y-auto bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl shadow-2xl">
        <DialogHeader className="space-y-3">
          <div className="flex items-center justify-between">
            <DialogTitle className="text-xl font-bold text-gray-900 dark:text-gray-100">
              User Details
            </DialogTitle>
            <Badge variant="outline" className="text-xs font-mono">
              ID: {selectedUser.id?.substring(0, 6) || 'N/A'}
            </Badge>
          </div>
          <DialogDescription className="text-sm text-gray-500 dark:text-gray-400">
            Detailed information about this user
          </DialogDescription>
        </DialogHeader>
        
        <Separator className="bg-gray-200 dark:bg-gray-700" />
        
        <div className="space-y-6 py-2">
          {/* Header Section */}
          <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 p-4 rounded-lg bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-800/50 dark:to-gray-900/50 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center gap-4">
              <div className="p-3 rounded-xl bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400">
                <User className="h-5 w-5" />
              </div>
              <div className="space-y-1">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {selectedUser.name}
                </h3>
                <div className="flex flex-wrap items-center gap-2">
                  {getStatusBadge(selectedUser.status).badge}
                  {getRoleBadge(selectedUser.role)}
                  <Badge variant="outline" className="text-xs">
                    {selectedUser.department || 'Engineering'}
                  </Badge>
                </div>
              </div>
            </div>
          </div>
          
          {/* Basic Information */}
          {renderMobileSection(
            'basicInfo',
            'User Information',
            <User className="h-3 w-3" />,
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-3 p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Email Address</p>
                    <div className="flex items-center gap-2">
                      <Mail className="h-4 w-4" />
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {selectedUser.email}
                      </span>
                    </div>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Username</p>
                    <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {selectedUser.username}
                    </p>
                  </div>
                </div>
                
                <div className="space-y-3 p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Role</p>
                    <div className="flex items-center gap-2">
                      <Shield className="h-4 w-4" />
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100 capitalize">
                        {selectedUser.role}
                      </span>
                    </div>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Member Since</p>
                    <div className="flex items-center gap-2">
                      <Calendar className="h-4 w-4" />
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {selectedUser.joinedDate || '2024-01-01'}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </>
          )}
          
          {/* Permissions */}
          {renderMobileSection(
            'permissions',
            'Permissions',
            <Shield className="h-3 w-3" />,
            <div className="space-y-3">
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <p className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">Access Level</p>
                <div className="flex flex-wrap gap-2">
                  {selectedUser.permissions?.map((perm, idx) => (
                    <Badge key={idx} variant="outline" className="text-xs">
                      {perm}
                    </Badge>
                  )) || (
                    <>
                      <Badge variant="outline" className="text-xs">Read</Badge>
                      <Badge variant="outline" className="text-xs">Write</Badge>
                      <Badge variant="outline" className="text-xs">Execute</Badge>
                    </>
                  )}
                </div>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                <div className="p-3 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div className="flex items-center justify-between mb-1">
                    <Database className="h-4 w-4 text-gray-400" />
                    <p className="text-xs text-gray-500 dark:text-gray-400">Databases</p>
                  </div>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {selectedUser.databaseAccess || 4}
                  </p>
                </div>
                
                <div className="p-3 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div className="flex items-center justify-between mb-1">
                    <FileCode className="h-4 w-4 text-gray-400" />
                    <p className="text-xs text-gray-500 dark:text-gray-400">APIs</p>
                  </div>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {selectedUser.apiAccess || 12}
                  </p>
                </div>
                
                <div className="p-3 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div className="flex items-center justify-between mb-1">
                    <Server className="h-4 w-4 text-gray-400" />
                    <p className="text-xs text-gray-500 dark:text-gray-400">Servers</p>
                  </div>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {selectedUser.serverAccess || 2}
                  </p>
                </div>
              </div>
            </div>
          )}
          
          {/* Activity */}
          {renderMobileSection(
            'activity',
            'Recent Activity',
            <Activity className="h-3 w-3" />,
            <div className="space-y-3">
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <p className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">Last Login</p>
                <div className="flex items-center gap-2">
                  <Clock className="h-4 w-4" />
                  <span className="text-sm text-gray-700 dark:text-gray-300">
                    {selectedUser.lastLogin || '2 hours ago'}
                  </span>
                </div>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <p className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">API Calls (Today)</p>
                <p className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {selectedUser.todayAPICalls || 124}
                </p>
              </div>
            </div>
          )}
        </div>
        
        <DialogFooter className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-4 border-t border-gray-200 dark:border-gray-800">
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={handleResetPassword}
              className="rounded-lg border-gray-300 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              <Key className="h-4 w-4 mr-2" />
              Reset Password
            </Button>
          </div>
          
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
            <div className="flex flex-row items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={handleEditUser}
                className="rounded-lg border-blue-300 dark:border-blue-700 hover:bg-blue-50 dark:hover:bg-blue-900/20"
              >
                <Edit className="h-4 w-4 mr-2 text-blue-600 dark:text-blue-400" />
                Edit User
              </Button>
            </div>
            
            <Separator orientation="vertical" className="h-auto sm:h-6 hidden sm:block" />
            
            <Button
              variant="default"
              onClick={() => setShowUserDetails(false)}
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