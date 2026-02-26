// components/modals/ApiGenerationModal.js
import React, { useState, useEffect } from 'react';
import { 
  X, Plus, Trash2, Save, Copy, Code, Globe, Lock, FileText, 
  Settings, Database, Map, FileJson, TestTube, Wrench, 
  RefreshCw, Eye, EyeOff, Download, Upload, Play, Key, 
  Shield, Hash, Calendar, Clock, Type, List, Link, ExternalLink,
  Check, AlertCircle, Star, Zap, Terminal, Package,
  CheckCircle, ChevronRight, Info, Layers, Cpu, Sparkles,
  Loader, Search, Filter, User, Users, Bell, ShieldCheck,
  BellOff, ShieldOff, Clock as ClockIcon, BarChart, Cpu as CpuIcon,
  Server, Cloud, CloudOff, FileCode, BookOpen, FileKey, GitBranch
} from 'lucide-react';

// Import the controller functions
import {
  getObjectDetails,
  handleSchemaBrowserResponse
} from "../../controllers/OracleSchemaController.js";

// New component for the preview modal
function ApiPreviewModal({ 
  isOpen, 
  onClose, 
  onConfirm,
  apiData,
  colors = {},
  theme = 'dark' 
}) {
  const themeColors = colors || {
    bg: theme === 'dark' ? 'rgb(1 14 35)' : '#f8fafc',
    text: theme === 'dark' ? '#E8ECF1' : '#1e293b',
    textSecondary: theme === 'dark' ? 'rgb(168 178 192)' : '#64748b',
    border: theme === 'dark' ? 'rgb(61 73 92)' : '#e2e8f0',
    hover: theme === 'dark' ? 'rgb(51 63 82)' : '#f1f5f9',
    primary: theme === 'dark' ? '#E8ECF1' : '#1e293b',
    primaryDark: theme === 'dark' ? 'rgb(37 99 235)' : '#2563eb',
    card: theme === 'dark' ? '#010e23' : '#ffffff',
    modalBg: theme === 'dark' ? '#010e23' : '#ffffff',
    modalBorder: theme === 'dark' ? 'rgb(61 73 92)' : '#e2e8f0',
    error: theme === 'dark' ? 'rgb(239 68 68)' : '#ef4444',
    success: theme === 'dark' ? 'rgb(16 185 129)' : '#10b981',
    warning: theme === 'dark' ? 'rgb(245 158 11)' : '#f59e0b',
    info: theme === 'dark' ? 'rgb(59 130 246)' : '#3b82f6',
    white: '#ffffff'
  };

  if (!isOpen || !apiData) return null;

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-50 p-4" style={{ zIndex: 1002 }}>
      <div className="rounded-xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col" style={{ 
        backgroundColor: themeColors.bg,
        border: `1px solid ${themeColors.modalBorder}`
      }}>
        {/* Header */}
        <div className="px-6 py-4 border-b flex items-center justify-between" style={{ 
          borderColor: themeColors.border,
          backgroundColor: themeColors.card
        }}>
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.warning + '20' }}>
              <Eye className="h-6 w-6" style={{ color: themeColors.warning }} />
            </div>
            <div>
              <h2 className="text-lg font-bold" style={{ color: themeColors.text }}>
                API Generation Preview
              </h2>
              <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                Review your API configuration before generation
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-lg transition-colors hover-lift"
            style={{ backgroundColor: themeColors.hover, color: themeColors.textSecondary }}
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          <div className="space-y-6">
            {/* API Summary */}
            <div className="p-4 rounded-lg border" style={{ 
              borderColor: themeColors.info + '40',
              backgroundColor: themeColors.info + '10'
            }}>
              <div className="flex items-start justify-between">
                <div className="space-y-2">
                  <h3 className="font-semibold flex items-center gap-2" style={{ color: themeColors.info }}>
                    <Zap className="h-5 w-5" />
                    {apiData.apiName}
                  </h3>
                  <div className="grid grid-cols-2 gap-4 text-xs">
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>API Code:</span>
                      <span className="ml-2 font-medium font-mono" style={{ color: themeColors.text }}>
                        {apiData.apiCode}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Version:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {apiData.version}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>Status:</span>
                      <span className="ml-2 px-2 py-1 rounded text-xs font-medium" style={{ 
                        backgroundColor: apiData.status === 'ACTIVE' ? themeColors.success + '30' : 
                                       apiData.status === 'DRAFT' ? themeColors.warning + '30' : 
                                       themeColors.error + '30',
                        color: apiData.status === 'DRAFT' ? themeColors.warning : 
                               apiData.status === 'ACTIVE' ? themeColors.success : 
                               themeColors.error
                      }}>
                        {apiData.status}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: themeColors.textSecondary }}>HTTP Method:</span>
                      <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                        {apiData.httpMethod}
                      </span>
                    </div>
                  </div>
                </div>
                <div className="text-right">
                  <div className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium" style={{ 
                    backgroundColor: themeColors.info + '20',
                    color: themeColors.info
                  }}>
                    <Globe className="h-4 w-4 mr-1" />
                    API Endpoint
                  </div>
                  <div className="mt-2 font-mono text-xs" style={{ color: themeColors.text }}>
                    {apiData.httpMethod} {apiData.basePath}{apiData.endpointPath}
                  </div>
                </div>
              </div>
            </div>

            {/* Source Object */}
            <div className="space-y-4">
              <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                <Database className="h-5 w-5" />
                Source Object
              </h4>
              <div className="p-4 rounded-lg border" style={{ 
                borderColor: themeColors.border,
                backgroundColor: themeColors.card
              }}>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs">
                  <div>
                    <span style={{ color: themeColors.textSecondary }}>Schema:</span>
                    <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                      {apiData.schemaConfig.schemaName}
                    </span>
                  </div>
                  <div>
                    <span style={{ color: themeColors.textSecondary }}>Object:</span>
                    <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                      {apiData.schemaConfig.objectName}
                    </span>
                  </div>
                  <div>
                    <span style={{ color: themeColors.textSecondary }}>Type:</span>
                    <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                      {apiData.schemaConfig.objectType}
                    </span>
                  </div>
                  <div>
                    <span style={{ color: themeColors.textSecondary }}>Operation:</span>
                    <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                      {apiData.schemaConfig.operation}
                    </span>
                  </div>
                </div>
                {apiData.sourceObject?.isSynonym && (
                  <div className="mt-2 text-xs" style={{ color: themeColors.warning }}>
                    <Link className="h-3 w-3 inline mr-1" />
                    This is a synonym pointing to {apiData.sourceObject.targetType}: {apiData.sourceObject.targetOwner}.{apiData.sourceObject.targetName}
                  </div>
                )}
              </div>
            </div>

            {/* Parameters Summary */}
            <div className="space-y-4">
              <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                <Hash className="h-5 w-5" />
                Parameters ({apiData.parameters?.length || 0})
              </h4>
              <div className="p-4 rounded-lg border" style={{ 
                borderColor: themeColors.border,
                backgroundColor: themeColors.card
              }}>
                {apiData.parameters && apiData.parameters.length > 0 ? (
                  <div className="space-y-2">
                    {apiData.parameters.slice(0, 3).map((param, index) => (
                      <div key={index} className="flex items-center justify-between text-xs">
                        <div>
                          <span className="font-medium" style={{ color: themeColors.text }}>
                            {param.key}
                          </span>
                          <span className="ml-2 text-xs px-2 py-1 rounded" style={{ 
                            backgroundColor: themeColors.info + '20',
                            color: themeColors.info
                          }}>
                            {param.parameterType}
                          </span>
                        </div>
                        <div style={{ color: themeColors.textSecondary }}>
                          {param.required ? 'Required' : 'Optional'} • {param.apiType}
                        </div>
                      </div>
                    ))}
                    {apiData.parameters.length > 3 && (
                      <div className="text-center pt-2">
                        <span className="text-xs" style={{ color: themeColors.textSecondary }}>
                          + {apiData.parameters.length - 3} more parameters
                        </span>
                      </div>
                    )}
                  </div>
                ) : (
                  <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                    No parameters defined
                  </p>
                )}
              </div>
            </div>

            {/* Response Mappings */}
            <div className="space-y-4">
              <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                <Map className="h-5 w-5" />
                Response Fields ({apiData.responseMappings?.length || 0})
              </h4>
              <div className="p-4 rounded-lg border" style={{ 
                borderColor: themeColors.border,
                backgroundColor: themeColors.card
              }}>
                {apiData.responseMappings && apiData.responseMappings.length > 0 ? (
                  <div className="grid grid-cols-2 gap-4 text-xs">
                    {apiData.responseMappings.slice(0, 6).map((mapping, index) => (
                      <div key={index}>
                        <div className="font-medium" style={{ color: themeColors.text }}>
                          {mapping.apiField}
                        </div>
                        <div className="text-xs" style={{ color: themeColors.textSecondary }}>
                          → {mapping.dbColumn} ({mapping.oracleType})
                        </div>
                      </div>
                    ))}
                    {apiData.responseMappings.length > 6 && (
                      <div className="col-span-2 text-center pt-2">
                        <span className="text-xs" style={{ color: themeColors.textSecondary }}>
                          + {apiData.responseMappings.length - 6} more fields
                        </span>
                      </div>
                    )}
                  </div>
                ) : (
                  <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                    No response mappings defined
                  </p>
                )}
              </div>
            </div>

            {/* Files to be Generated */}
            <div className="space-y-4">
              <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                <Layers className="h-5 w-5" />
                Files to be Generated
              </h4>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="p-3 rounded-lg border" style={{ 
                  borderColor: themeColors.border,
                  backgroundColor: themeColors.card
                }}>
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.info + '20' }}>
                      <Code className="h-4 w-4" style={{ color: themeColors.info }} />
                    </div>
                    <div>
                      <h5 className="font-medium text-xs" style={{ color: themeColors.text }}>PL/SQL Package</h5>
                      <p className="text-xs" style={{ color: themeColors.textSecondary }}>Oracle Database</p>
                    </div>
                  </div>
                </div>

                <div className="p-3 rounded-lg border" style={{ 
                  borderColor: themeColors.border,
                  backgroundColor: themeColors.card
                }}>
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.success + '20' }}>
                      <FileJson className="h-4 w-4" style={{ color: themeColors.success }} />
                    </div>
                    <div>
                      <h5 className="font-medium text-xs" style={{ color: themeColors.text }}>OpenAPI Spec</h5>
                      <p className="text-xs" style={{ color: themeColors.textSecondary }}>API Documentation</p>
                    </div>
                  </div>
                </div>

                <div className="p-3 rounded-lg border" style={{ 
                  borderColor: themeColors.border,
                  backgroundColor: themeColors.card
                }}>
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.warning + '20' }}>
                      <Database className="h-4 w-4" style={{ color: themeColors.warning }} />
                    </div>
                    <div>
                      <h5 className="font-medium text-xs" style={{ color: themeColors.text }}>Postman Collection</h5>
                      <p className="text-xs" style={{ color: themeColors.textSecondary }}>API Testing</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t flex items-center justify-between" style={{ 
          borderColor: themeColors.border,
          backgroundColor: themeColors.card
        }}>
          <div className="text-xs" style={{ color: themeColors.textSecondary }}>
            Review all details carefully before generating the API
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={onClose}
              className="px-4 py-2 border rounded-lg transition-colors hover-lift"
              style={{ 
                backgroundColor: themeColors.hover,
                borderColor: themeColors.border,
                color: themeColors.text
              }}
            >
              Cancel
            </button>
            <button
              onClick={onConfirm}
              className="px-4 py-2 rounded-lg flex items-center gap-2 transition-colors hover-lift"
              style={{ backgroundColor: themeColors.success, color: themeColors.white }}
            >
              <Sparkles className="h-4 w-4" />
              Confirm & Generate API
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

// New component for the loading modal
function ApiLoadingModal({ 
  isOpen, 
  colors = {},
  theme = 'dark' 
}) {
  const themeColors = colors || {
    bg: theme === 'dark' ? 'rgb(1 14 35)' : '#f8fafc',
    text: theme === 'dark' ? '#E8ECF1' : '#1e293b',
    textSecondary: theme === 'dark' ? 'rgb(168 178 192)' : '#64748b',
    modalBg: theme === 'dark' ? '#010e23' : '#ffffff',
    modalBorder: theme === 'dark' ? 'rgb(61 73 92)' : '#e2e8f0',
    info: theme === 'dark' ? 'rgb(59 130 246)' : '#3b82f6',
    white: '#ffffff'
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-50" style={{ zIndex: 1003 }}>
      <div className="rounded-xl shadow-2xl w-full max-w-md p-6" style={{ 
        backgroundColor: themeColors.bg,
        border: `1px solid ${themeColors.modalBorder}`
      }}>
        <div className="text-center space-y-4">
          <div className="inline-flex">
            <Loader className="h-12 w-12 animate-spin" style={{ color: themeColors.info }} />
          </div>
          <div>
            <h3 className="text-lg font-bold mb-2" style={{ color: themeColors.text }}>
              Generating Your API
            </h3>
            <p className="text-xs" style={{ color: themeColors.textSecondary }}>
              Creating PL/SQL package, OpenAPI specification, and Postman collection...
            </p>
          </div>
          <div className="pt-4 space-y-3">
            <div className="flex items-center gap-3 text-xs" style={{ color: themeColors.textSecondary }}>
              <Loader className="h-4 w-4 animate-spin" style={{ color: themeColors.info }} />
              <span>Creating PL/SQL package...</span>
            </div>
            <div className="flex items-center gap-3 text-xs" style={{ color: themeColors.textSecondary }}>
              <Loader className="h-4 w-4 animate-spin" style={{ color: themeColors.info }} />
              <span>Generating OpenAPI specification...</span>
            </div>
            <div className="flex items-center gap-3 text-xs" style={{ color: themeColors.textSecondary }}>
              <Loader className="h-4 w-4 animate-spin" style={{ color: themeColors.info }} />
              <span>Building Postman collection...</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

// Updated confirmation modal with loader before showing
function ApiConfirmationModal({ 
  isOpen, 
  onClose, 
  apiData,
  colors = {},
  theme = 'dark' 
}) {
  const [showLoader, setShowLoader] = useState(true);
  const [showSuccess, setShowSuccess] = useState(false);

  const themeColors = colors || {
    bg: theme === 'dark' ? 'rgb(1 14 35)' : '#f8fafc',
    text: theme === 'dark' ? '#E8ECF1' : '#1e293b',
    textSecondary: theme === 'dark' ? 'rgb(168 178 192)' : '#64748b',
    border: theme === 'dark' ? 'rgb(61 73 92)' : '#e2e8f0',
    hover: theme === 'dark' ? 'rgb(51 63 82)' : '#f1f5f9',
    primary: theme === 'dark' ? '#E8ECF1' : '#1e293b',
    primaryDark: theme === 'dark' ? 'rgb(37 99 235)' : '#2563eb',
    card: theme === 'dark' ? '#010e23' : '#ffffff',
    modalBg: theme === 'dark' ? '#010e23' : '#ffffff',
    modalBorder: theme === 'dark' ? 'rgb(61 73 92)' : '#e2e8f0',
    error: theme === 'dark' ? 'rgb(239 68 68)' : '#ef4444',
    success: theme === 'dark' ? 'rgb(16 185 129)' : '#10b981',
    warning: theme === 'dark' ? 'rgb(245 158 11)' : '#f59e0b',
    info: theme === 'dark' ? 'rgb(59 130 246)' : '#3b82f6',
    white: '#ffffff'
  };

  // Show loader for 2 seconds, then show success
  useEffect(() => {
    if (isOpen) {
      setShowLoader(true);
      setShowSuccess(false);
      
      const timer = setTimeout(() => {
        setShowLoader(false);
        setShowSuccess(true);
      }, 2000);
      
      return () => clearTimeout(timer);
    }
  }, [isOpen]);

  if (!isOpen || !apiData) return null;

  // Show loader
  if (showLoader) {
    return (
      <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-50" style={{ zIndex: 1001 }}>
        <div className="rounded-xl shadow-2xl w-full max-w-md p-6" style={{ 
          backgroundColor: themeColors.bg,
          border: `1px solid ${themeColors.modalBorder}`
        }}>
          <div className="text-center space-y-4">
            <div className="inline-flex">
              <Loader className="h-12 w-12 animate-spin" style={{ color: themeColors.success }} />
            </div>
            <div>
              <h3 className="text-lg font-bold mb-2" style={{ color: themeColors.text }}>
                Finalizing API Generation
              </h3>
              <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                Completing the API setup and deploying to the registry...
              </p>
            </div>
            <div className="pt-4 space-y-3">
              <div className="flex items-center gap-3 text-xs" style={{ color: themeColors.textSecondary }}>
                <Loader className="h-4 w-4 animate-spin" style={{ color: themeColors.success }} />
                <span>Validating API configuration...</span>
              </div>
              <div className="flex items-center gap-3 text-xs" style={{ color: themeColors.textSecondary }}>
                <Loader className="h-4 w-4 animate-spin" style={{ color: themeColors.success }} />
                <span>Registering in API registry...</span>
              </div>
              <div className="flex items-center gap-3 text-xs" style={{ color: themeColors.textSecondary }}>
                <Loader className="h-4 w-4 animate-spin" style={{ color: themeColors.success }} />
                <span>Setting up monitoring...</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Show success
  if (showSuccess) {
    const formatDate = (dateString) => {
      return new Date(dateString).toLocaleString();
    };

    const copyApiDetails = () => {
      navigator.clipboard.writeText(JSON.stringify(apiData, null, 2));
      // You can add a toast notification here
    };

    const downloadApiDetails = () => {
      const blob = new Blob([JSON.stringify(apiData, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${apiData.apiCode || 'api'}_configuration.json`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    };

    return (
      <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-50 p-4" style={{ zIndex: 1001 }}>
        <div className="rounded-xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col" style={{ 
          backgroundColor: themeColors.bg,
          border: `1px solid ${themeColors.modalBorder}`
        }}>
          {/* Header */}
          <div className="px-6 py-4 border-b flex items-center justify-between" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.card
          }}>
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.success + '20' }}>
                <CheckCircle className="h-6 w-6" style={{ color: themeColors.success }} />
              </div>
              <div>
                <h2 className="text-lg font-bold" style={{ color: themeColors.text }}>
                  API Generated Successfully!
                </h2>
                <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                  Your API has been created and is ready to use
                </p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-2 rounded-lg transition-colors hover-lift"
              style={{ backgroundColor: themeColors.hover, color: themeColors.textSecondary }}
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Content */}
          <div className="flex-1 overflow-y-auto p-6">
            <div className="space-y-6">
              {/* API Summary */}
              <div className="p-4 rounded-lg border" style={{ 
                borderColor: themeColors.success + '40',
                backgroundColor: themeColors.success + '10'
              }}>
                <div className="flex items-start justify-between">
                  <div className="space-y-2">
                    <h3 className="font-semibold flex items-center gap-2" style={{ color: themeColors.success }}>
                      <Sparkles className="h-5 w-5" />
                      {apiData.apiName}
                    </h3>
                    <div className="grid grid-cols-2 gap-4 text-xs">
                      <div>
                        <span style={{ color: themeColors.textSecondary }}>API Code:</span>
                        <span className="ml-2 font-medium font-mono" style={{ color: themeColors.text }}>
                          {apiData.apiCode}
                        </span>
                      </div>
                      <div>
                        <span style={{ color: themeColors.textSecondary }}>Version:</span>
                        <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                          {apiData.version}
                        </span>
                      </div>
                      <div>
                        <span style={{ color: themeColors.textSecondary }}>Status:</span>
                        <span className="ml-2 px-2 py-1 rounded text-xs font-medium" style={{ 
                          backgroundColor: apiData.status === 'ACTIVE' ? themeColors.success + '30' : 
                                         apiData.status === 'DRAFT' ? themeColors.warning + '30' : 
                                         themeColors.error + '30',
                          color: apiData.status === 'DRAFT' ? themeColors.warning : 
                                 apiData.status === 'ACTIVE' ? themeColors.success : 
                                 themeColors.error
                        }}>
                          {apiData.status}
                        </span>
                      </div>
                      <div>
                        <span style={{ color: themeColors.textSecondary }}>Created:</span>
                        <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                          {formatDate(apiData.createdAt)}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium" style={{ 
                      backgroundColor: themeColors.info + '20',
                      color: themeColors.info
                    }}>
                      <Globe className="h-4 w-4 mr-1" />
                      API Endpoint
                    </div>
                    <div className="mt-2 font-mono text-xs" style={{ color: themeColors.text }}>
                      {apiData.httpMethod} {apiData.basePath}{apiData.endpointPath}
                    </div>
                  </div>
                </div>
              </div>

              {/* Generated Files */}
              <div className="space-y-4">
                <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                  <Layers className="h-5 w-5" />
                  Generated Files
                </h4>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="p-4 rounded-lg border" style={{ 
                    borderColor: themeColors.border,
                    backgroundColor: themeColors.card
                  }}>
                    <div className="flex items-center gap-3 mb-3">
                      <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.info + '20' }}>
                        <Code className="h-5 w-5" style={{ color: themeColors.info }} />
                      </div>
                      <div>
                        <h5 className="font-medium" style={{ color: themeColors.text }}>PL/SQL Package</h5>
                        <p className="text-xs" style={{ color: themeColors.textSecondary }}>Oracle Database</p>
                      </div>
                    </div>
                    <div className="text-xs font-mono p-2 rounded border" style={{ 
                      backgroundColor: themeColors.hover,
                      borderColor: themeColors.border,
                      color: themeColors.textSecondary
                    }}>
                      {apiData.schemaConfig.schemaName}_{apiData.apiCode}_PKG.sql
                    </div>
                  </div>

                  <div className="p-4 rounded-lg border" style={{ 
                    borderColor: themeColors.border,
                    backgroundColor: themeColors.card
                  }}>
                    <div className="flex items-center gap-3 mb-3">
                      <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.success + '20' }}>
                        <FileJson className="h-5 w-5" style={{ color: themeColors.success }} />
                      </div>
                      <div>
                        <h5 className="font-medium" style={{ color: themeColors.text }}>OpenAPI Spec</h5>
                        <p className="text-xs" style={{ color: themeColors.textSecondary }}>API Documentation</p>
                      </div>
                    </div>
                    <div className="text-xs font-mono p-2 rounded border" style={{ 
                      backgroundColor: themeColors.hover,
                      borderColor: themeColors.border,
                      color: themeColors.textSecondary
                    }}>
                      {apiData.apiCode}_openapi.json
                    </div>
                  </div>

                  <div className="p-4 rounded-lg border" style={{ 
                    borderColor: themeColors.border,
                    backgroundColor: themeColors.card
                  }}>
                    <div className="flex items-center gap-3 mb-3">
                      <div className="p-2 rounded-lg" style={{ backgroundColor: themeColors.warning + '20' }}>
                        <Database className="h-5 w-5" style={{ color: themeColors.warning }} />
                      </div>
                      <div>
                        <h5 className="font-medium" style={{ color: themeColors.text }}>Postman Collection</h5>
                        <p className="text-xs" style={{ color: themeColors.textSecondary }}>API Testing</p>
                      </div>
                    </div>
                    <div className="text-xs font-mono p-2 rounded border" style={{ 
                      backgroundColor: themeColors.hover,
                      borderColor: themeColors.border,
                      color: themeColors.textSecondary
                    }}>
                      {apiData.apiCode}_postman.json
                    </div>
                  </div>
                </div>
              </div>

              {/* Next Steps */}
              <div className="space-y-4">
                <h4 className="font-semibold flex items-center gap-2" style={{ color: themeColors.text }}>
                  <ChevronRight className="h-5 w-5" />
                  Next Steps
                </h4>
                <div className="p-4 rounded-lg border" style={{ 
                  borderColor: themeColors.border,
                  backgroundColor: themeColors.card
                }}>
                  <div className="space-y-3">
                    <div className="flex items-center gap-3">
                      <div className="p-1.5 rounded-full" style={{ backgroundColor: themeColors.info + '20' }}>
                        <div className="w-6 h-6 flex items-center justify-center rounded-full" style={{ 
                          backgroundColor: themeColors.info,
                          color: themeColors.white
                        }}>
                          1
                        </div>
                      </div>
                      <div>
                        <h5 className="font-medium" style={{ color: themeColors.text }}>Execute PL/SQL Package</h5>
                        <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                          Run the generated SQL script in your Oracle Database
                        </p>
                      </div>
                    </div>
                    
                    <div className="flex items-center gap-3">
                      <div className="p-1.5 rounded-full" style={{ backgroundColor: themeColors.info + '20' }}>
                        <div className="w-6 h-6 flex items-center justify-center rounded-full" style={{ 
                          backgroundColor: themeColors.info,
                          color: themeColors.white
                        }}>
                          2
                        </div>
                      </div>
                      <div>
                        <h5 className="font-medium" style={{ color: themeColors.text }}>Configure API Gateway</h5>
                        <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                          Deploy the OpenAPI spec to your API Gateway
                        </p>
                      </div>
                    </div>
                    
                    <div className="flex items-center gap-3">
                      <div className="p-1.5 rounded-full" style={{ backgroundColor: themeColors.info + '20' }}>
                        <div className="w-6 h-6 flex items-center justify-center rounded-full" style={{ 
                          backgroundColor: themeColors.info,
                          color: themeColors.white
                        }}>
                          3
                        </div>
                      </div>
                      <div>
                        <h5 className="font-medium" style={{ color: themeColors.text }}>Test with Postman</h5>
                        <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                          Import and test the API using the generated Postman collection
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="px-6 py-4 border-t flex items-center justify-between" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.card
          }}>
            <div className="flex items-center gap-3">
              <button
                onClick={copyApiDetails}
                className="px-3 py-2 border rounded-lg flex items-center gap-2 transition-colors hover-lift"
                style={{ 
                  backgroundColor: themeColors.hover,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
              >
                <Copy className="h-4 w-4" />
                Copy Details
              </button>
              <button
                onClick={downloadApiDetails}
                className="px-3 py-2 border rounded-lg flex items-center gap-2 transition-colors hover-lift"
                style={{ 
                  backgroundColor: themeColors.info,
                  borderColor: themeColors.info,
                  color: themeColors.white
                }}
              >
                <Download className="h-4 w-4" />
                Download Configuration
              </button>
            </div>
            <div className="flex items-center gap-3">
              <button
                onClick={onClose}
                className="px-4 py-2 border rounded-lg transition-colors hover-lift"
                style={{ 
                  backgroundColor: themeColors.hover,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
              >
                Close
              </button>
              <button
                onClick={() => {
                  // You can add functionality to deploy/test the API
                  onClose();
                }}
                className="px-4 py-2 rounded-lg flex items-center gap-2 transition-colors hover-lift"
                style={{ backgroundColor: themeColors.success, color: themeColors.white }}
              >
                <Cpu className="h-4 w-4" />
                Deploy API
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return null;
}

export default function ApiGenerationModal({
  isOpen,
  onClose,
  onSave,
  selectedObject = null,
  colors = {},
  theme = 'dark',
  authToken = null
}) {
  const [activeTab, setActiveTab] = useState('definition');
  const [loading, setLoading] = useState(false);
  const [apiDetails, setApiDetails] = useState({
    apiName: '',
    apiCode: '',
    description: '',
    version: '1.0.0',
    status: 'DRAFT',
    httpMethod: 'GET',
    basePath: '/api/v1',
    endpointPath: '',
    tags: ['default'],
    category: 'general',
    owner: 'HR',
  });

  // Schema & Object Configuration
  const [schemaConfig, setSchemaConfig] = useState({
    schemaName: '',
    objectType: '',
    objectName: '',
    operation: 'SELECT',
    primaryKeyColumn: '',
    sequenceName: '',
    enablePagination: true,
    pageSize: 50,
    enableSorting: true,
    defaultSortColumn: '',
    defaultSortDirection: 'ASC'
  });

  const [parameters, setParameters] = useState([]);
  const [responseMappings, setResponseMappings] = useState([]);
  const [sourceObjectInfo, setSourceObjectInfo] = useState({
    isSynonym: false,
    targetType: null,
    targetName: null,
    targetOwner: null
  });

  // Function to fetch object details for synonyms
  const fetchObjectDetails = async (object, type) => {
    if (!authToken || !object) {
      console.log('❌ Cannot fetch object details: missing authToken or object');
      return null;
    }
    
    console.log('🔍 Fetching details for:', { object, type });
    
    try {
      const response = await getObjectDetails(authToken, { 
        objectType: type, 
        objectName: object.name 
      });
      
      console.log('📦 Raw object details response:', response);
      
      const processedResponse = handleSchemaBrowserResponse(response);
      const responseData = processedResponse.data || processedResponse;
      
      console.log('📦 Processed object details:', responseData);
      
      // If it's a synonym, extract target information
      if (type === 'SYNONYM' || type?.toUpperCase() === 'SYNONYM') {
        const targetType = responseData.TARGET_TYPE || responseData.targetType;
        const targetName = responseData.TARGET_NAME || responseData.targetName;
        const targetOwner = responseData.TARGET_OWNER || responseData.targetOwner;
        
        console.log('🎯 Synonym points to:', { targetType, targetName, targetOwner });
        
        setSourceObjectInfo({
          isSynonym: true,
          targetType,
          targetName,
          targetOwner
        });
        
        // The target details are already in the response
        if (responseData.targetDetails) {
          console.log('📦 Target details found:', responseData.targetDetails);
          return responseData;
        }
      }
      
      return responseData;
    } catch (error) {
      console.error('❌ Error fetching object details:', error);
      return null;
    }
  };

  // Initialize parameters and mappings based on selected object
  useEffect(() => {
    const initializeFromObject = async () => {
      if (!selectedObject) {
        console.log('ℹ️ ApiGenerationModal - No selected object provided, showing empty form');
        return;
      }

      setLoading(true);
      console.log('🔍 ApiGenerationModal - Initializing with selected object:', {
        name: selectedObject.name,
        type: selectedObject.type,
        owner: selectedObject.owner,
        hasParameters: !!(selectedObject.parameters || selectedObject.PARAMETERS || selectedObject.arguments || selectedObject.ARGUMENTS)
      });

      try {
        // Fetch detailed object information (especially important for synonyms)
        let detailedObject = selectedObject;
        let objectType = selectedObject.type;
        let objectName = selectedObject.name;
        let objectOwner = selectedObject.owner;
        
        // If we have authToken, try to fetch more details
        if (authToken) {
          const fetchedDetails = await fetchObjectDetails(selectedObject, selectedObject.type);
          if (fetchedDetails) {
            detailedObject = fetchedDetails;
          }
        }
        
        // For synonyms, use the targetDetails if available
        let effectiveObject = detailedObject;
        let effectiveType = objectType;
        let effectiveName = objectName;
        let effectiveOwner = objectOwner;
        
        if (detailedObject?.targetDetails) {
          // This is a synonym with target details
          effectiveObject = detailedObject.targetDetails;
          effectiveType = detailedObject.targetDetails.OBJECT_TYPE || objectType;
          effectiveName = detailedObject.targetDetails.OBJECT_NAME || objectName;
          effectiveOwner = detailedObject.targetDetails.OWNER || objectOwner;
          
          console.log('🎯 Using target object:', {
            type: effectiveType,
            name: effectiveName,
            owner: effectiveOwner,
            hasParameters: !!(effectiveObject.parameters || effectiveObject.arguments || effectiveObject.PARAMETERS || effectiveObject.ARGUMENTS)
          });
        }

        // Determine operation and HTTP method
        let operation = 'SELECT';
        let httpMethod = 'GET';
        
        const normalizedType = (effectiveType || '').toUpperCase();
        
        if (normalizedType === 'PROCEDURE' || normalizedType === 'FUNCTION') {
          operation = 'EXECUTE';
          httpMethod = 'POST';
        } else if (normalizedType === 'PACKAGE') {
          operation = 'EXECUTE';
          httpMethod = 'POST';
        } else if (normalizedType === 'VIEW') {
          operation = 'SELECT';
          httpMethod = 'GET';
        } else if (normalizedType === 'TABLE') {
          operation = 'SELECT';
          httpMethod = 'GET';
        } else if (normalizedType === 'SEQUENCE') {
          operation = 'SELECT';
          httpMethod = 'GET';
        } else if (normalizedType === 'TRIGGER') {
          operation = 'EXECUTE';
          httpMethod = 'POST';
        }

        // Set API details
        const baseName = effectiveName?.toLowerCase() || '';
        const endpointPath = baseName ? `/${baseName.replace(/_/g, '-').toLowerCase()}` : '';
        
        setApiDetails(prev => ({
          ...prev,
          apiName: effectiveName ? `${effectiveName} API` : 'New API',
          apiCode: normalizedType ? `${normalizedType.slice(0, 3)}_${effectiveName || 'API'}` : 'API',
          description: selectedObject.comment || detailedObject?.COMMENTS || (effectiveName ? `API for ${effectiveName}` : ''),
          endpointPath: endpointPath,
          owner: effectiveOwner || 'HR',
          httpMethod: httpMethod
        }));

        setSchemaConfig(prev => ({
          ...prev,
          schemaName: effectiveOwner || 'HR',
          objectType: effectiveType || 'TABLE',
          objectName: effectiveName || '',
          operation: operation,
          primaryKeyColumn: ''
        }));

        // Generate parameters and response mappings
        const newParameters = [];
        const newMappings = [];

        // Look for parameters in various locations
        console.log('🔍 Looking for parameters in effectiveObject:', effectiveObject);
        
        // Check for parameters (procedures/functions)
        let parametersArray = null;
        
        if (effectiveObject.parameters && Array.isArray(effectiveObject.parameters)) {
          parametersArray = effectiveObject.parameters;
          console.log('📋 Found parameters in effectiveObject.parameters');
        } else if (effectiveObject.PARAMETERS && Array.isArray(effectiveObject.PARAMETERS)) {
          parametersArray = effectiveObject.PARAMETERS;
          console.log('📋 Found parameters in effectiveObject.PARAMETERS');
        } else if (effectiveObject.arguments && Array.isArray(effectiveObject.arguments)) {
          parametersArray = effectiveObject.arguments;
          console.log('📋 Found parameters in effectiveObject.arguments');
        } else if (effectiveObject.ARGUMENTS && Array.isArray(effectiveObject.ARGUMENTS)) {
          parametersArray = effectiveObject.ARGUMENTS;
          console.log('📋 Found parameters in effectiveObject.ARGUMENTS');
        }

        if (parametersArray && parametersArray.length > 0) {
          console.log('⚙️ Generating parameters from procedure/function:', parametersArray.length);
          console.log('📋 Parameters array:', parametersArray);
          
          parametersArray.forEach((param, index) => {
            // Handle different parameter naming conventions
            const paramName = param.ARGUMENT_NAME || param.argument_name || param.name || param.NAME || `param_${index + 1}`;
            const paramType = param.DATA_TYPE || param.data_type || param.type || param.TYPE || 'VARCHAR2';
            const paramMode = param.IN_OUT || param.in_out || param.mode || param.MODE || 'IN';
            
            console.log(`📌 Parameter ${index + 1}:`, { paramName, paramType, paramMode });
            
            // Generate a clean key name
            let cleanKey = paramName;
            if (typeof paramName === 'string') {
              cleanKey = paramName.replace(/^p_/i, '').toLowerCase();
            } else {
              cleanKey = `param_${index + 1}`;
            }
            
            newParameters.push({
              id: `proc-param-${Date.now()}-${index}`,
              key: cleanKey,
              dbParameter: paramName,
              oracleType: paramType.includes('VARCHAR') ? 'VARCHAR2' : 
                         paramType.includes('NUMBER') ? 'NUMBER' :
                         paramType.includes('DATE') ? 'DATE' : 
                         paramType.includes('TIMESTAMP') ? 'TIMESTAMP' : 'VARCHAR2',
              apiType: paramType.includes('NUMBER') ? 'integer' : 
                      paramType.includes('DATE') ? 'string' : 'string',
              parameterType: paramMode === 'IN' ? 'query' : 'body',
              required: paramMode === 'IN' || paramMode === 'IN OUT',
              description: `${paramName} (${paramMode})`,
              example: paramType.includes('NUMBER') ? '1' : 
                      paramType.includes('DATE') ? '2024-01-01' : '',
              validationPattern: '',
              defaultValue: param.DATA_DEFAULT || param.defaultValue || ''
            });

            // Add to response mappings for OUT parameters
            if (paramMode === 'OUT' || paramMode === 'IN OUT') {
              newMappings.push({
                id: `mapping-${Date.now()}-out-${index}`,
                apiField: cleanKey,
                dbColumn: paramName,
                oracleType: paramType.includes('VARCHAR') ? 'VARCHAR2' : 
                           paramType.includes('NUMBER') ? 'NUMBER' :
                           paramType.includes('DATE') ? 'DATE' : 'VARCHAR2',
                apiType: paramType.includes('NUMBER') ? 'integer' : 'string',
                format: paramType.includes('DATE') ? 'date-time' : '',
                nullable: true,
                isPrimaryKey: false,
                includeInResponse: true
              });
            }
          });
          
          // If there's a return type for functions, add it to mappings
          const returnType = effectiveObject.RETURN_TYPE || effectiveObject.return_type || effectiveObject.returnType;
          if (returnType && normalizedType === 'FUNCTION') {
            newMappings.push({
              id: `mapping-${Date.now()}-return`,
              apiField: 'result',
              dbColumn: 'RETURN_VALUE',
              oracleType: returnType.includes('VARCHAR') ? 'VARCHAR2' : 
                         returnType.includes('NUMBER') ? 'NUMBER' :
                         returnType.includes('DATE') ? 'DATE' : 'VARCHAR2',
              apiType: returnType.includes('NUMBER') ? 'integer' : 'string',
              format: '',
              nullable: false,
              isPrimaryKey: false,
              includeInResponse: true
            });
          }
        }
        // Check for columns (tables/views)
        else {
          let columnsArray = null;
          
          if (effectiveObject.columns && Array.isArray(effectiveObject.columns)) {
            columnsArray = effectiveObject.columns;
          } else if (effectiveObject.COLUMNS && Array.isArray(effectiveObject.COLUMNS)) {
            columnsArray = effectiveObject.COLUMNS;
          }

          if (columnsArray && columnsArray.length > 0) {
            console.log('📊 Generating parameters from columns:', columnsArray.length);
            
            columnsArray.forEach((col, index) => {
              const colName = col.name || col.COLUMN_NAME || col.column_name;
              const colType = col.type || col.DATA_TYPE || col.data_type || 'VARCHAR2';
              const colNullable = col.nullable || col.NULLABLE || 'Y';
              const isPrimaryKey = col.key === 'PK' || col.CONSTRAINT_TYPE === 'P' || col.isPrimaryKey;
              
              if (colName) {
                // Clean up column name for API key
                const cleanKey = typeof colName === 'string' ? colName.toLowerCase() : `column_${index + 1}`;
                
                newParameters.push({
                  id: `param-${Date.now()}-${index}`,
                  key: cleanKey,
                  dbColumn: colName,
                  oracleType: colType.includes('VARCHAR') ? 'VARCHAR2' : 
                            colType.includes('NUMBER') ? 'NUMBER' :
                            colType.includes('DATE') ? 'DATE' : 
                            colType.includes('TIMESTAMP') ? 'TIMESTAMP' : 'VARCHAR2',
                  apiType: colType.includes('NUMBER') ? 'integer' : 
                          colType.includes('DATE') ? 'string' : 'string',
                  parameterType: isPrimaryKey ? 'path' : 'query',
                  required: isPrimaryKey || colNullable === 'N',
                  description: col.comment || col.COMMENTS || `From ${effectiveName}.${colName}`,
                  example: colName.includes('ID') ? '1' : 
                          colName.includes('DATE') ? '2024-01-01' :
                          colName.includes('NAME') ? 'Sample' : '',
                  validationPattern: '',
                  defaultValue: col.DATA_DEFAULT || col.defaultValue || ''
                });

                newMappings.push({
                  id: `mapping-${Date.now()}-${index}`,
                  apiField: cleanKey,
                  dbColumn: colName,
                  oracleType: colType.includes('VARCHAR') ? 'VARCHAR2' : 
                            colType.includes('NUMBER') ? 'NUMBER' :
                            colType.includes('DATE') ? 'DATE' : 'VARCHAR2',
                  apiType: colType.includes('NUMBER') ? 'integer' : 'string',
                  format: colType.includes('DATE') ? 'date-time' : '',
                  nullable: colNullable === 'Y',
                  isPrimaryKey: isPrimaryKey,
                  includeInResponse: true
                });
              }
            });
          } else {
            console.log('⚠️ No parameters or columns found for object type:', effectiveType);
          }
        }

        setParameters(newParameters);
        setResponseMappings(newMappings);
        
        console.log('✅ ApiGenerationModal - Initialization complete:', {
          parametersCount: newParameters.length,
          mappingsCount: newMappings.length
        });

      } catch (error) {
        console.error('❌ Error initializing modal:', error);
      } finally {
        setLoading(false);
      }
    };

    if (isOpen) {
      initializeFromObject();
    }
  }, [selectedObject, isOpen, authToken]);

  const [authConfig, setAuthConfig] = useState({
    authType: 'ORACLE_ROLES',
    requiredRoles: [],
    customAuthFunction: '',
    validateSession: true,
    checkObjectPrivileges: true,
    apiKeyHeader: 'X-API-Key',
    jwtIssuer: 'api.example.com',
    oauthClientId: '',
    oauthScopes: ['read', 'write']
  });

  const [headers, setHeaders] = useState([
    { id: '1', key: 'Content-Type', value: 'application/json', required: true, description: 'Response content type' },
    { id: '2', key: 'Cache-Control', value: 'no-cache', required: false, description: 'Cache control header' }
  ]);

  const [requestBody, setRequestBody] = useState({
    schemaType: 'JSON',
    sample: '{}',
    validationRules: [],
    requiredFields: [],
    validateSchema: true,
    maxSize: 1048576, // 1MB
    allowedMediaTypes: ['application/json']
  });

  const [responseBody, setResponseBody] = useState({
    successSchema: '{\n  "success": true,\n  "data": {},\n  "message": ""\n}',
    errorSchema: '{\n  "success": false,\n  "error": {\n    "code": "",\n    "message": ""\n  }\n}',
    includeMetadata: true,
    metadataFields: ['timestamp', 'apiVersion', 'requestId'],
    contentType: 'application/json',
    compression: 'gzip'
  });

  const [tests, setTests] = useState({
    unitTests: '',
    integrationTests: '',
    testData: '',
    assertions: [],
    performanceThreshold: 1000, // ms
    testEnvironment: 'development',
    testUsers: 1,
    testIterations: 10
  });

  const [settings, setSettings] = useState({
    timeout: 30000,
    maxRecords: 1000,
    enableLogging: true,
    logLevel: 'INFO',
    enableCaching: false,
    cacheTtl: 300,
    enableRateLimiting: false,
    rateLimit: 100,
    rateLimitPeriod: 'minute',
    enableAudit: true,
    auditLevel: 'ALL',
    generateSwagger: true,
    generatePostman: true,
    generateClientSDK: true,
    enableMonitoring: true,
    enableAlerts: false,
    alertEmail: '',
    enableTracing: false,
    corsEnabled: true,
    corsOrigins: ['*']
  });

  const [generatedCode, setGeneratedCode] = useState('');
  const [previewMode, setPreviewMode] = useState('json');

  // State for modals
  const [previewOpen, setPreviewOpen] = useState(false);
  const [loadingOpen, setLoadingOpen] = useState(false);
  const [confirmationOpen, setConfirmationOpen] = useState(false);
  const [newApiData, setNewApiData] = useState(null);

  // Tab definitions
  const tabs = [
    { id: 'definition', label: 'Definition', icon: <FileText className="h-4 w-4" /> },
    { id: 'schema', label: 'Schema', icon: <Database className="h-4 w-4" /> },
    { id: 'parameters', label: 'Parameters', icon: <Hash className="h-4 w-4" /> },
    { id: 'mapping', label: 'Mapping', icon: <Map className="h-4 w-4" /> },
    { id: 'auth', label: 'Authentication', icon: <Lock className="h-4 w-4" /> },
    { id: 'request', label: 'Request', icon: <Upload className="h-4 w-4" /> },
    { id: 'response', label: 'Response', icon: <Download className="h-4 w-4" /> },
    { id: 'tests', label: 'Tests', icon: <TestTube className="h-4 w-4" /> },
    { id: 'settings', label: 'Settings', icon: <Settings className="h-4 w-4" /> },
    { id: 'preview', label: 'Preview', icon: <Eye className="h-4 w-4" /> },
  ];

  // Oracle Data Types
  const oracleDataTypes = [
    'VARCHAR2', 'NUMBER', 'DATE', 'TIMESTAMP', 'TIMESTAMP WITH TIME ZONE',
    'TIMESTAMP WITH LOCAL TIME ZONE', 'INTERVAL YEAR TO MONTH', 'INTERVAL DAY TO SECOND',
    'RAW', 'LONG RAW', 'CHAR', 'NCHAR', 'NVARCHAR2', 'CLOB', 'NCLOB', 'BLOB',
    'BFILE', 'ROWID', 'UROWID'
  ];

  const apiDataTypes = [
    'string', 'integer', 'number', 'boolean', 'array', 'object', 'null'
  ];

  const parameterTypes = [
    { value: 'path', label: 'Path Parameter', description: 'Part of URL path' },
    { value: 'query', label: 'Query Parameter', description: 'URL query string' },
    { value: 'header', label: 'Header Parameter', description: 'HTTP header' },
    { value: 'body', label: 'Body Parameter', description: 'Request body' }
  ];

  // Get HTTP method based on operation type
  const getHttpMethodFromOperation = (operation) => {
    switch(operation) {
      case 'SELECT': return 'GET';
      case 'INSERT': return 'POST';
      case 'UPDATE': return 'PUT';
      case 'DELETE': return 'DELETE';
      case 'EXECUTE': return 'POST';
      default: return 'GET';
    }
  };

  // Handle API details changes
  const handleApiDetailChange = (field, value) => {
    setApiDetails(prev => ({ ...prev, [field]: value }));
  };

  // Handle schema configuration
  const handleSchemaConfigChange = (field, value) => {
    const updatedConfig = { ...schemaConfig, [field]: value };
    setSchemaConfig(updatedConfig);
    
    // Update HTTP method based on operation
    if (field === 'operation') {
      const httpMethod = getHttpMethodFromOperation(value);
      handleApiDetailChange('httpMethod', httpMethod);
    }
  };

  // Handle parameter operations
  const handleAddParameter = () => {
    const newParam = {
      id: `param-${Date.now()}`,
      key: '',
      dbColumn: '',
      oracleType: 'VARCHAR2',
      apiType: 'string',
      parameterType: 'query',
      required: false,
      description: '',
      example: '',
      validationPattern: '',
      defaultValue: ''
    };
    setParameters([...parameters, newParam]);
  };

  const handleParameterChange = (id, field, value) => {
    setParameters(parameters.map(param => 
      param.id === id ? { ...param, [field]: value } : param
    ));
  };

  const handleRemoveParameter = (id) => {
    setParameters(parameters.filter(param => param.id !== id));
  };

  // Handle response mapping operations
  const handleAddResponseMapping = () => {
    const newMapping = {
      id: `mapping-${Date.now()}`,
      apiField: '',
      dbColumn: '',
      oracleType: 'VARCHAR2',
      apiType: 'string',
      format: '',
      nullable: true,
      isPrimaryKey: false,
      includeInResponse: true
    };
    setResponseMappings([...responseMappings, newMapping]);
  };

  const handleResponseMappingChange = (id, field, value) => {
    setResponseMappings(responseMappings.map(mapping => 
      mapping.id === id ? { ...mapping, [field]: value } : mapping
    ));
  };

  const handleRemoveResponseMapping = (id) => {
    setResponseMappings(responseMappings.filter(mapping => mapping.id !== id));
  };

  // Handle header operations
  const handleAddHeader = () => {
    const newHeader = {
      id: `header-${Date.now()}`,
      key: '',
      value: '',
      required: false,
      description: ''
    };
    setHeaders([...headers, newHeader]);
  };

  const handleHeaderChange = (id, field, value) => {
    setHeaders(headers.map(header => 
      header.id === id ? { ...header, [field]: value } : header
    ));
  };

  const handleRemoveHeader = (id) => {
    setHeaders(headers.filter(header => header.id !== id));
  };

  // Handle auth configuration
  const handleAuthConfigChange = (field, value) => {
    setAuthConfig(prev => ({ ...prev, [field]: value }));
  };

  // Handle request body configuration
  const handleRequestBodyChange = (field, value) => {
    setRequestBody(prev => ({ ...prev, [field]: value }));
  };

  // Handle response body configuration
  const handleResponseBodyChange = (field, value) => {
    setResponseBody(prev => ({ ...prev, [field]: value }));
  };

  // Handle tests configuration
  const handleTestsChange = (field, value) => {
    setTests(prev => ({ ...prev, [field]: value }));
  };

  // Handle settings configuration
  const handleSettingsChange = (field, value) => {
    setSettings(prev => ({ ...prev, [field]: value }));
  };

  // Generate PL/SQL code
  const generatePLSQLCode = () => {
    const paramList = parameters.map(p => 
      `${p.key} IN ${p.oracleType}${p.required ? ' NOT NULL' : ''} DEFAULT ${p.defaultValue || 'NULL'}`
    ).join(',\n    ');
    
    const mappingList = responseMappings
      .filter(m => m.includeInResponse)
      .map(m => m.dbColumn)
      .join(', ');

    const operationMap = {
      'SELECT': `OPEN v_cursor FOR\n      SELECT ${mappingList}\n      FROM ${schemaConfig.schemaName}.${schemaConfig.objectName}\n      WHERE 1=1`,
      'INSERT': `INSERT INTO ${schemaConfig.schemaName}.${schemaConfig.objectName} (...) VALUES (...)`,
      'UPDATE': `UPDATE ${schemaConfig.schemaName}.${schemaConfig.objectName} SET ...`,
      'DELETE': `DELETE FROM ${schemaConfig.schemaName}.${schemaConfig.objectName} WHERE ...`,
      'EXECUTE': `-- Execute ${schemaConfig.objectType} ${schemaConfig.schemaName}.${schemaConfig.objectName}`
    };

    return `-- ============================================================================
-- API Package: ${apiDetails.apiName}
-- Generated: ${new Date().toISOString()}
-- Version: ${apiDetails.version}
-- Source Object: ${schemaConfig.schemaName}.${schemaConfig.objectName} (${schemaConfig.objectType})
-- ============================================================================

CREATE OR REPLACE PACKAGE ${schemaConfig.schemaName}_${apiDetails.apiCode || 'API'}_PKG AS
  -- ${apiDetails.apiName}
  -- Generated from: ${schemaConfig.schemaName}.${schemaConfig.objectName}
  -- Object Type: ${schemaConfig.objectType}
  -- Operation: ${schemaConfig.operation}
  
  PROCEDURE ${apiDetails.apiCode || 'PROCESS_REQUEST'} (
    ${paramList || '-- No parameters defined'}
  );
  
  -- Helper functions
  FUNCTION validate_parameters RETURN BOOLEAN;
  FUNCTION format_response RETURN CLOB;
  
END ${schemaConfig.schemaName}_${apiDetails.apiCode || 'API'}_PKG;
/

CREATE OR REPLACE PACKAGE BODY ${schemaConfig.schemaName}_${apiDetails.apiCode || 'API'}_PKG AS

  g_api_version CONSTANT VARCHAR2(10) := '${apiDetails.version}';
  g_api_name    CONSTANT VARCHAR2(100) := '${apiDetails.apiName}';
  
  PROCEDURE ${apiDetails.apiCode || 'PROCESS_REQUEST'} (
    ${paramList || '-- No parameters defined'}
  ) IS
    v_cursor SYS_REFCURSOR;
    v_start_time TIMESTAMP := SYSTIMESTAMP;
  BEGIN
    -- Log request
    DBMS_OUTPUT.PUT_LINE('API Request: ' || g_api_name || ' - ' || SYSTIMESTAMP);
    
    -- Validate parameters
    IF NOT validate_parameters THEN
      RAISE_APPLICATION_ERROR(-20001, 'Invalid parameters');
    END IF;
    
    -- Execute operation
    ${operationMap[schemaConfig.operation] || '-- Operation not specified'}
    
    -- Log execution time
    DBMS_OUTPUT.PUT_LINE('Execution time: ' || (SYSTIMESTAMP - v_start_time));
    
    -- Return cursor for SELECT operations
    IF '${schemaConfig.operation}' = 'SELECT' THEN
      DBMS_SQL.RETURN_RESULT(v_cursor);
    END IF;
    
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('API Error: ' || SQLERRM);
      RAISE_APPLICATION_ERROR(-20001, 'API Error: ' || SQLERRM);
  END ${apiDetails.apiCode || 'PROCESS_REQUEST'};
  
  FUNCTION validate_parameters RETURN BOOLEAN IS
  BEGIN
    -- Add parameter validation logic here
    RETURN TRUE;
  END validate_parameters;
  
  FUNCTION format_response RETURN CLOB IS
    v_response CLOB;
  BEGIN
    -- Add response formatting logic here
    RETURN v_response;
  END format_response;

END ${schemaConfig.schemaName}_${apiDetails.apiCode || 'API'}_PKG;
/`;
  };

  // Generate OpenAPI specification
  const generateOpenAPISpec = () => {
    const spec = {
      openapi: '3.0.0',
      info: {
        title: apiDetails.apiName,
        description: apiDetails.description,
        version: apiDetails.version,
        contact: {
          name: apiDetails.owner,
          email: `${apiDetails.owner.toLowerCase()}@example.com`
        }
      },
      servers: [
        {
          url: '{baseUrl}' + apiDetails.basePath,
          variables: {
            baseUrl: {
              default: 'https://api.example.com',
            },
          },
        },
      ],
      paths: {
        [apiDetails.endpointPath]: {
          [apiDetails.httpMethod.toLowerCase()]: {
            summary: apiDetails.apiName,
            description: apiDetails.description,
            tags: apiDetails.tags,
            operationId: apiDetails.apiCode.toLowerCase(),
            parameters: parameters.map(p => ({
              name: p.key,
              in: p.parameterType,
              description: p.description,
              required: p.required,
              schema: {
                type: p.apiType,
                example: p.example,
                pattern: p.validationPattern,
                default: p.defaultValue,
              },
            })),
            responses: {
              '200': {
                description: 'Successful response',
                content: {
                  'application/json': {
                    schema: {
                      type: 'object',
                      properties: responseMappings.reduce((acc, mapping) => ({
                        ...acc,
                        [mapping.apiField]: {
                          type: mapping.apiType,
                          description: `Maps to ${mapping.dbColumn} (${mapping.oracleType})`,
                          nullable: mapping.nullable,
                          format: mapping.format,
                        },
                      }), {}),
                    },
                  },
                },
              },
              '400': {
                description: 'Bad Request'
              },
              '401': {
                description: 'Unauthorized'
              },
              '500': {
                description: 'Internal Server Error'
              }
            },
            security: authConfig.authType !== 'NONE' ? [{ [authConfig.authType.toLowerCase()]: [] }] : [],
          },
        },
      },
      components: {
        securitySchemes: {
          [authConfig.authType.toLowerCase()]: {
            type: 'http',
            scheme: 'bearer'
          }
        }
      }
    };
    
    return JSON.stringify(spec, null, 2);
  };

  // Generate Postman collection
  const generatePostmanCollection = () => {
    const collection = {
      info: {
        name: apiDetails.apiName,
        description: apiDetails.description,
        schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
      },
      item: [
        {
          name: apiDetails.apiName,
          request: {
            method: apiDetails.httpMethod,
            header: headers.map(h => ({
              key: h.key,
              value: h.value,
              description: h.description,
            })),
            url: {
              raw: `{{baseUrl}}${apiDetails.basePath}${apiDetails.endpointPath}`,
              host: ['{{baseUrl}}'],
              path: apiDetails.endpointPath.split('/').filter(Boolean),
              query: parameters
                .filter(p => p.parameterType === 'query')
                .map(p => ({
                  key: p.key,
                  value: p.example || '',
                  description: p.description,
                })),
            },
            description: apiDetails.description,
          },
          response: [],
        },
      ],
      variable: [
        {
          key: 'baseUrl',
          value: 'https://api.example.com',
          type: 'string',
        },
      ],
    };
    
    return JSON.stringify(collection, null, 2);
  };

  // Update preview based on mode
  useEffect(() => {
    switch(previewMode) {
      case 'plsql':
        setGeneratedCode(generatePLSQLCode());
        break;
      case 'openapi':
        setGeneratedCode(generateOpenAPISpec());
        break;
      case 'postman':
        setGeneratedCode(generatePostmanCollection());
        break;
      default:
        setGeneratedCode(JSON.stringify({
          apiDetails,
          schemaConfig,
          parameters: parameters.map(p => ({
            key: p.key,
            dbColumn: p.dbColumn,
            type: p.apiType,
            required: p.required,
            parameterType: p.parameterType
          })),
          responseMappings: responseMappings.map(m => ({
            apiField: m.apiField,
            dbColumn: m.dbColumn,
            type: m.apiType,
            nullable: m.nullable
          })),
          authConfig,
          settings,
          sourceObjectInfo
        }, null, 2));
    }
  }, [previewMode, apiDetails, schemaConfig, parameters, responseMappings, authConfig, settings, sourceObjectInfo]);

  // Handle save - show preview first
  const handleSave = () => {
    const apiData = {
      id: `api-${Date.now()}`,
      ...apiDetails,
      schemaConfig,
      parameters,
      responseMappings,
      authConfig,
      headers,
      requestBody,
      responseBody,
      tests,
      settings,
      generatedCode: {
        plsql: generatePLSQLCode(),
        openapi: generateOpenAPISpec(),
        postman: generatePostmanCollection()
      },
      createdAt: new Date().toISOString(),
      sourceObject: {
        name: selectedObject?.name,
        type: selectedObject?.type,
        owner: selectedObject?.owner,
        columns: selectedObject?.columns?.length,
        parameters: selectedObject?.parameters?.length,
        isSynonym: sourceObjectInfo.isSynonym,
        targetType: sourceObjectInfo.targetType,
        targetName: sourceObjectInfo.targetName,
        targetOwner: sourceObjectInfo.targetOwner
      }
    };
    
    // Show preview modal
    setNewApiData(apiData);
    setPreviewOpen(true);
  };

  // Handle preview confirmation - show loader, then success
  const handlePreviewConfirm = () => {
    setPreviewOpen(false);
    setLoadingOpen(true);
    
    // Simulate API generation process
    setTimeout(() => {
      setLoadingOpen(false);
      setConfirmationOpen(true);
      
      // Call the parent onSave if provided
      if (onSave && newApiData) {
        onSave(newApiData);
      }
    }, 3000); // 3 seconds for loader
  };

  // Handle confirmation modal close
  const handleConfirmationClose = () => {
    setConfirmationOpen(false);
    onClose(); // Close the generation modal too
  };

  // Copy generated code
  const copyGeneratedCode = () => {
    navigator.clipboard.writeText(generatedCode);
    // You can add a toast notification here
  };

  // Download generated code
  const downloadGeneratedCode = () => {
    const extension = previewMode === 'plsql' ? 'sql' : 'json';
    const filename = previewMode === 'plsql' ? 
      `${schemaConfig.schemaName}_${apiDetails.apiCode}_PKG.sql` :
      previewMode === 'openapi' ? `${apiDetails.apiCode}_openapi.json` :
      previewMode === 'postman' ? `${apiDetails.apiCode}_postman.json` :
      `${apiDetails.apiCode}_configuration.json`;
    
    const blob = new Blob([generatedCode], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  if (!isOpen) return null;

  // Use colors from parent or default
  const themeColors = colors || {
    bg: theme === 'dark' ? 'rgb(1 14 35)' : '#f8fafc',
    text: theme === 'dark' ? '#E8ECF1' : '#1e293b',
    textSecondary: theme === 'dark' ? 'rgb(168 178 192)' : '#64748b',
    border: theme === 'dark' ? 'rgb(61 73 92)' : '#e2e8f0',
    hover: theme === 'dark' ? 'rgb(51 63 82)' : '#f1f5f9',
    primary: theme === 'dark' ? '#E8ECF1' : '#1e293b',
    primaryDark: theme === 'dark' ? 'rgb(37 99 235)' : '#2563eb',
    card: theme === 'dark' ? '#010e23' : '#ffffff',
    modalBg: theme === 'dark' ? '#010e23' : '#ffffff',
    modalBorder: theme === 'dark' ? 'rgb(61 73 92)' : '#e2e8f0',
    error: theme === 'dark' ? 'rgb(239 68 68)' : '#ef4444',
    success: theme === 'dark' ? 'rgb(16 185 129)' : '#10b981',
    warning: theme === 'dark' ? 'rgb(245 158 11)' : '#f59e0b',
    info: theme === 'dark' ? 'rgb(59 130 246)' : '#3b82f6',
    white: '#ffffff'
  };

  return (
    <>
      <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-50 p-4" style={{ zIndex: 1000 }}>
        <div className="rounded-xl shadow-2xl w-5xl max-w-5xl max-h-[90vh] overflow-hidden flex flex-col" style={{ 
          backgroundColor: themeColors.bg,
          border: `1px solid ${themeColors.modalBorder}`
        }}>
          {/* Header */}
          <div className="px-6 py-4 border-b flex items-center justify-between" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.card
          }}>
            <div className="flex items-center gap-3">
              {selectedObject?.type === 'TABLE' ? <Database className="h-6 w-6" style={{ color: themeColors.info }} /> :
               selectedObject?.type === 'VIEW' ? <FileText className="h-6 w-6" style={{ color: themeColors.success }} /> :
               selectedObject?.type === 'PROCEDURE' ? <Terminal className="h-6 w-6" style={{ color: themeColors.info }} /> :
               selectedObject?.type === 'FUNCTION' ? <Code className="h-6 w-6" style={{ color: themeColors.warning }} /> :
               selectedObject?.type === 'PACKAGE' ? <Package className="h-6 w-6" style={{ color: themeColors.textSecondary }} /> :
               selectedObject?.type === 'TRIGGER' ? <Zap className="h-6 w-6" style={{ color: themeColors.error }} /> :
               selectedObject?.type === 'SYNONYM' ? <Link className="h-6 w-6" style={{ color: themeColors.info }} /> :
               <Globe className="h-6 w-6" style={{ color: themeColors.info }} />}
              <div>
                <h2 className="text-lg font-bold" style={{ color: themeColors.text }}>
                  Generate API {selectedObject?.name ? ' from ' + selectedObject?.type || 'Object: ' + selectedObject?.name || '' : 'Form'}
                </h2>
                {selectedObject?.name && (
                  <>
                    <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                      {selectedObject?.owner}.{selectedObject?.name} • {selectedObject?.type} • 
                      {sourceObjectInfo.isSynonym && (
                        <span className="ml-1 text-yellow-500">
                          (points to {sourceObjectInfo.targetType}: {sourceObjectInfo.targetOwner}.{sourceObjectInfo.targetName})
                        </span>
                      )}
                    </p>
                    {loading && (
                      <p className="text-xs mt-1 flex items-center gap-1" style={{ color: themeColors.info }}>
                        <Loader className="h-3 w-3 animate-spin" />
                        Fetching object details...
                      </p>
                    )}
                  </>
                )}
              </div>
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={onClose}
                className="p-2 rounded-lg transition-colors hover-lift"
                style={{ backgroundColor: themeColors.hover, color: themeColors.textSecondary }}
                disabled={loading}
              >
                <X className="h-5 w-5" />
              </button>
            </div>
          </div>

          {/* API Details Section */}
          <div className="px-6 py-4 border-b" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.modalBg
          }}>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  API Name *
                </label>
                <input
                  type="text"
                  value={apiDetails.apiName}
                  onChange={(e) => handleApiDetailChange('apiName', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="Users API"
                  disabled={loading}
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  API Code *
                </label>
                <input
                  type="text"
                  value={apiDetails.apiCode}
                  onChange={(e) => handleApiDetailChange('apiCode', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="GET_USERS"
                  disabled={loading}
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  HTTP Method
                </label>
                <select
                  value={apiDetails.httpMethod}
                  onChange={(e) => handleApiDetailChange('httpMethod', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.bg,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  disabled={loading}
                >
                  <option value="GET">GET</option>
                  <option value="POST">POST</option>
                  <option value="PUT">PUT</option>
                  <option value="DELETE">DELETE</option>
                  <option value="PATCH">PATCH</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Status
                </label>
                <select
                  value={apiDetails.status}
                  onChange={(e) => handleApiDetailChange('status', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.bg,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  disabled={loading}
                >
                  <option value="DRAFT">Draft</option>
                  <option value="ACTIVE">Active</option>
                  <option value="DEPRECATED">Deprecated</option>
                  <option value="ARCHIVED">Archived</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Base Path
                </label>
                <input
                  type="text"
                  value={apiDetails.basePath}
                  onChange={(e) => handleApiDetailChange('basePath', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="/api/v1"
                  disabled={loading}
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Endpoint Path *
                </label>
                <input
                  type="text"
                  value={apiDetails.endpointPath}
                  onChange={(e) => handleApiDetailChange('endpointPath', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="/users"
                  disabled={loading}
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Version
                </label>
                <input
                  type="text"
                  value={apiDetails.version}
                  onChange={(e) => handleApiDetailChange('version', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="1.0.0"
                  disabled={loading}
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Owner
                </label>
                <input
                  type="text"
                  value={apiDetails.owner}
                  onChange={(e) => handleApiDetailChange('owner', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="HR"
                  disabled
                />
              </div>

              <div className="space-y-2 md:col-span-2 lg:col-span-4">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Description
                </label>
                <textarea
                  value={apiDetails.description}
                  onChange={(e) => handleApiDetailChange('description', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  rows="2"
                  placeholder="API description..."
                  disabled={loading}
                />
              </div>
            </div>
          </div>

          {/* Tabs */}
          <div className="border-b" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.card
          }}>
            <div className="px-6 flex space-x-1 overflow-x-auto">
              {tabs.map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`px-3 py-2 flex items-center gap-2 border-b-2 text-xs font-medium transition-colors whitespace-nowrap hover-lift ${
                    activeTab === tab.id
                      ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === tab.id ? themeColors.info : 'transparent',
                    color: activeTab === tab.id ? themeColors.info : themeColors.textSecondary,
                    backgroundColor: 'transparent'
                  }}
                  disabled={loading}
                >
                  {tab.icon}
                  {tab.label}
                </button>
              ))}
            </div>
          </div>

          {/* Tab Content */}
          <div className="flex-1 overflow-y-auto">
            {loading ? (
              <div className="h-full flex items-center justify-center p-8">
                <div className="text-center">
                  <Loader className="h-12 w-12 animate-spin mx-auto mb-4" style={{ color: themeColors.info }} />
                  <p className="text-sm" style={{ color: themeColors.text }}>Fetching object details...</p>
                  <p className="text-xs mt-2" style={{ color: themeColors.textSecondary }}>
                    {sourceObjectInfo.isSynonym ? 
                      `Resolving synonym to ${sourceObjectInfo.targetType}...` : 
                      'Loading parameters and mappings...'}
                  </p>
                </div>
              </div>
            ) : (
              <div className="p-4 md:p-6">
                {/* Definition Tab */}
                {activeTab === 'definition' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      API Definition
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Category
                          </label>
                          <select
                            value={apiDetails.category}
                            onChange={(e) => handleApiDetailChange('category', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.bg,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          >
                            <option value="general">General</option>
                            <option value="data">Data Access</option>
                            <option value="oracle">Administrative</option>
                            <option value="report">Reporting</option>
                            <option value="integration">Integration</option>
                          </select>
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Tags
                          </label>
                          <input
                            type="text"
                            value={apiDetails.tags.join(', ')}
                            onChange={(e) => handleApiDetailChange('tags', e.target.value.split(',').map(tag => tag.trim()))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="users, data, public"
                          />
                        </div>
                      </div>

                      <div className="p-4 rounded-lg border" style={{ 
                        borderColor: themeColors.info + '40',
                        backgroundColor: themeColors.info + '10'
                      }}>
                        <h4 className="font-medium mb-2 flex items-center gap-2" style={{ color: themeColors.info }}>
                          <Globe className="h-4 w-4" />
                          API Endpoint Preview
                        </h4>
                        <div className="font-mono text-xs p-3 rounded border" style={{ 
                          backgroundColor: themeColors.card,
                          borderColor: themeColors.border
                        }}>
                          <div style={{ color: themeColors.textSecondary }}>
                            {apiDetails.httpMethod} {apiDetails.basePath}{apiDetails.endpointPath}
                          </div>
                          {parameters.filter(p => p.parameterType === 'path').length > 0 && (
                            <div className="mt-2 text-xs" style={{ color: themeColors.textSecondary }}>
                              Path Parameters: {parameters.filter(p => p.parameterType === 'path').map(p => `{${p.key}}`).join('/')}
                            </div>
                          )}
                          <div className="mt-2 text-xs" style={{ color: themeColors.textSecondary }}>
                            Source: {schemaConfig.schemaName}.{schemaConfig.objectName} ({schemaConfig.objectType})
                          </div>
                          {sourceObjectInfo.isSynonym && (
                            <div className="mt-1 text-xs" style={{ color: themeColors.warning }}>
                              <Link className="h-3 w-3 inline mr-1" />
                              Synonym → {sourceObjectInfo.targetType}: {sourceObjectInfo.targetOwner}.{sourceObjectInfo.targetName}
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Schema Tab */}
                {activeTab === 'schema' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      Oracle Schema Configuration
                    </h3>

                    <div className="mb-4 p-4 rounded-lg border" style={{ 
                      borderColor: themeColors.info + '40',
                      backgroundColor: themeColors.info + '10'
                    }}>
                      <h4 className="font-medium mb-2 flex items-center gap-2" style={{ color: themeColors.info }}>
                        <Database className="h-4 w-4" />
                        Selected Object Information
                      </h4>
                      <div className="grid grid-cols-2 gap-4 text-xs">
                        <div>
                          <span style={{ color: themeColors.textSecondary }}>Object:</span>
                          <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                            {selectedObject?.owner}.{selectedObject?.name}
                          </span>
                        </div>
                        <div>
                          <span style={{ color: themeColors.textSecondary }}>Type:</span>
                          <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                            {selectedObject?.type}
                          </span>
                        </div>
                        {selectedObject?.columns && (
                          <div>
                            <span style={{ color: themeColors.textSecondary }}>Columns:</span>
                            <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                              {selectedObject.columns.length}
                            </span>
                          </div>
                        )}
                        {selectedObject?.parameters && (
                          <div>
                            <span style={{ color: themeColors.textSecondary }}>Parameters:</span>
                            <span className="ml-2 font-medium" style={{ color: themeColors.text }}>
                              {selectedObject.parameters.length}
                            </span>
                          </div>
                        )}
                      </div>

                      {/* Show parameters from selected object */}
                      {selectedObject?.parameters && selectedObject.parameters.length > 0 && (
                        <div className="mt-4">
                          <h5 className="text-xs font-medium mb-2" style={{ color: themeColors.text }}>
                            Object Parameters:
                          </h5>
                          <div className="space-y-2 max-h-48 overflow-y-auto pr-2">
                            {selectedObject.parameters.map((param, index) => (
                              <div key={index} className="flex items-center justify-between text-xs p-2 rounded" 
                                style={{ backgroundColor: themeColors.hover }}>
                                <div>
                                  <span className="font-medium" style={{ color: themeColors.text }}>
                                    {param.name || param.ARGUMENT_NAME}
                                  </span>
                                  <span className="ml-2 text-xs px-2 py-0.5 rounded" style={{ 
                                    backgroundColor: (param.mode || param.IN_OUT || param.in_out) === 'IN' ? themeColors.info + '20' : 
                                                  (param.mode || param.IN_OUT || param.in_out) === 'OUT' ? themeColors.success + '20' : 
                                                  themeColors.warning + '20',
                                    color: (param.mode || param.IN_OUT || param.in_out) === 'IN' ? themeColors.info : 
                                          (param.mode || param.IN_OUT || param.in_out) === 'OUT' ? themeColors.success : 
                                          themeColors.warning
                                  }}>
                                    {param.mode || param.IN_OUT || param.in_out || 'IN'}
                                  </span>
                                </div>
                                <div style={{ color: themeColors.textSecondary }}>
                                  {param.type || param.DATA_TYPE}
                                  {(param.defaultValue || param.DATA_DEFAULT) && (
                                    <span className="ml-2 text-xs">
                                      (Default: {param.defaultValue || param.DATA_DEFAULT})
                                    </span>
                                  )}
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* Show columns from selected object */}
                      {selectedObject?.columns && selectedObject.columns.length > 0 && (
                        <div className="mt-4">
                          <h5 className="text-xs font-medium mb-2" style={{ color: themeColors.text }}>
                            Object Columns (Auto-generated as parameters):
                          </h5>
                          <div className="space-y-2 max-h-48 overflow-y-auto pr-2">
                            {selectedObject.columns.slice(0, 8).map((col, index) => (
                              <div key={index} className="flex items-center justify-between text-xs p-2 rounded" 
                                style={{ backgroundColor: themeColors.hover }}>
                                <div>
                                  <span className="font-medium" style={{ color: themeColors.text }}>
                                    {col.name || col.COLUMN_NAME}
                                  </span>
                                  {((col.key === 'PK') || (col.CONSTRAINT_TYPE === 'P')) && (
                                    <span className="ml-2 text-xs px-2 py-0.5 rounded" style={{ 
                                      backgroundColor: themeColors.success + '20',
                                      color: themeColors.success
                                    }}>
                                      PK
                                    </span>
                                  )}
                                </div>
                                <div className="flex items-center gap-2">
                                  <span style={{ color: themeColors.textSecondary }}>
                                    {col.type || col.DATA_TYPE}
                                  </span>
                                  <span className="text-xs px-2 py-0.5 rounded" style={{ 
                                    backgroundColor: (col.nullable === 'Y' || col.nullable === true || col.NULLABLE === 'Y') ? themeColors.warning + '20' : themeColors.error + '20',
                                    color: (col.nullable === 'Y' || col.nullable === true || col.NULLABLE === 'Y') ? themeColors.warning : themeColors.error
                                  }}>
                                    {(col.nullable === 'Y' || col.nullable === true || col.NULLABLE === 'Y') ? 'NULL' : 'NOT NULL'}
                                  </span>
                                </div>
                              </div>
                            ))}
                            {selectedObject.columns.length > 8 && (
                              <div className="text-center pt-2">
                                <span className="text-xs" style={{ color: themeColors.textSecondary }}>
                                  + {selectedObject.columns.length - 8} more columns
                                </span>
                              </div>
                            )}
                          </div>
                          <p className="text-xs mt-2" style={{ color: themeColors.textSecondary }}>
                            These columns will be auto-generated as API parameters and response fields.
                          </p>
                        </div>
                      )}

                      {sourceObjectInfo.isSynonym && (
                        <div className="mt-4 p-3 rounded" style={{ backgroundColor: themeColors.warning + '20' }}>
                          <p className="text-xs flex items-center gap-2" style={{ color: themeColors.warning }}>
                            <Link className="h-4 w-4" />
                            This is a synonym pointing to {sourceObjectInfo.targetType}: {sourceObjectInfo.targetOwner}.{sourceObjectInfo.targetName}
                          </p>
                          <p className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>
                            Parameters and response fields are generated from the target object.
                          </p>
                        </div>
                      )}
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Schema Name *
                          </label>
                          <input
                            type="text"
                            value={schemaConfig.schemaName}
                            onChange={(e) => handleSchemaConfigChange('schemaName', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="HR"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Object Type
                          </label>
                          <select
                            value={schemaConfig.objectType}
                            onChange={(e) => handleSchemaConfigChange('objectType', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.bg,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          >
                            <option value="TABLE">Table</option>
                            <option value="VIEW">View</option>
                            <option value="PROCEDURE">Procedure</option>
                            <option value="FUNCTION">Function</option>
                            <option value="PACKAGE">Package</option>
                            <option value="TRIGGER">Trigger</option>
                            <option value="SEQUENCE">Sequence</option>
                            <option value="TYPE">Type</option>
                            <option value="SYNONYM">Synonym</option>
                          </select>
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Object Name *
                          </label>
                          <input
                            type="text"
                            value={schemaConfig.objectName}
                            onChange={(e) => handleSchemaConfigChange('objectName', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="EMPLOYEES"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Operation
                          </label>
                          <select
                            value={schemaConfig.operation}
                            onChange={(e) => handleSchemaConfigChange('operation', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.bg,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          >
                            <option value="SELECT">SELECT (Read)</option>
                            <option value="INSERT">INSERT (Create)</option>
                            <option value="UPDATE">UPDATE (Update)</option>
                            <option value="DELETE">DELETE (Delete)</option>
                            <option value="EXECUTE">EXECUTE (Procedure/Function)</option>
                          </select>
                        </div>
                      </div>

                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Primary Key Column
                          </label>
                          <input
                            type="text"
                            value={schemaConfig.primaryKeyColumn}
                            onChange={(e) => handleSchemaConfigChange('primaryKeyColumn', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="ID"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Sequence Name (for INSERT)
                          </label>
                          <input
                            type="text"
                            value={schemaConfig.sequenceName}
                            onChange={(e) => handleSchemaConfigChange('sequenceName', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="SEQ_TABLE_NAME"
                          />
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Enable Pagination
                            </label>
                            <div className="flex items-center">
                              <input
                                type="checkbox"
                                checked={schemaConfig.enablePagination}
                                onChange={(e) => handleSchemaConfigChange('enablePagination', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ 
                                  accentColor: themeColors.info,
                                  backgroundColor: themeColors.card
                                }}
                              />
                              <span className="ml-2 text-xs" style={{ color: themeColors.textSecondary }}>Yes</span>
                            </div>
                          </div>

                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Page Size
                            </label>
                            <input
                              type="number"
                              value={schemaConfig.pageSize}
                              onChange={(e) => handleSchemaConfigChange('pageSize', parseInt(e.target.value))}
                              className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                              min="1"
                              max="1000"
                            />
                          </div>

                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Enable Sorting
                            </label>
                            <div className="flex items-center">
                              <input
                                type="checkbox"
                                checked={schemaConfig.enableSorting}
                                onChange={(e) => handleSchemaConfigChange('enableSorting', e.target.checked)}
                                className="h-4 w-4 rounded"
                                style={{ 
                                  accentColor: themeColors.info,
                                  backgroundColor: themeColors.card
                                }}
                              />
                              <span className="ml-2 text-xs" style={{ color: themeColors.textSecondary }}>Yes</span>
                            </div>
                          </div>

                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Default Sort Column
                            </label>
                            <input
                              type="text"
                              value={schemaConfig.defaultSortColumn}
                              onChange={(e) => handleSchemaConfigChange('defaultSortColumn', e.target.value)}
                              className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                              placeholder="CREATED_DATE"
                            />
                          </div>
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Default Sort Direction
                          </label>
                          <select
                            value={schemaConfig.defaultSortDirection}
                            onChange={(e) => handleSchemaConfigChange('defaultSortDirection', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.bg,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          >
                            <option value="ASC">Ascending (ASC)</option>
                            <option value="DESC">Descending (DESC)</option>
                          </select>
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Parameters Tab - Show auto-generated parameters */}
                {activeTab === 'parameters' && (
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                        API Parameters ({parameters.length})
                        {selectedObject?.columns && (
                          <span className="text-xs font-normal ml-2" style={{ color: themeColors.textSecondary }}>
                            (Auto-generated from {selectedObject.columns.length} columns)
                          </span>
                        )}
                        {selectedObject?.parameters && (
                          <span className="text-xs font-normal ml-2" style={{ color: themeColors.textSecondary }}>
                            (Auto-generated from {selectedObject.parameters.length} parameters)
                          </span>
                        )}
                        {sourceObjectInfo.isSynonym && (
                          <span className="text-xs font-normal ml-2" style={{ color: themeColors.warning }}>
                            (Resolved from target {sourceObjectInfo.targetType})
                          </span>
                        )}
                      </h3>
                      <button
                        onClick={handleAddParameter}
                        className="px-3 py-1.5 rounded-lg flex items-center gap-2 text-xs transition-colors hover-lift"
                        style={{ backgroundColor: themeColors.info, color: themeColors.white }}
                      >
                        <Plus className="h-4 w-4" />
                        Add Parameter
                      </button>
                    </div>

                    {parameters.length === 0 ? (
                      <div className="text-center py-8 border rounded-lg" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <Code className="h-12 w-12 mx-auto mb-3" style={{ color: themeColors.textSecondary }} />
                        <p style={{ color: themeColors.textSecondary }}>
                          No parameters defined. Add parameters or they will be auto-generated from the selected object.
                        </p>
                      </div>
                    ) : (
                      <div className="overflow-x-auto border rounded-lg" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <table className="w-full min-w-[1000px]">
                          <thead>
                            <tr style={{ backgroundColor: themeColors.hover }}>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>Parameter</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>DB Column</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>Oracle Type</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>API Type</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>Parameter Type</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>Required</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ 
                                borderColor: themeColors.border,
                                color: themeColors.textSecondary
                              }}>Actions</th>
                            </tr>
                          </thead>
                          <tbody>
                            {parameters.map((param, index) => (
                              <tr key={param.id} style={{ 
                                backgroundColor: index % 2 === 0 ? themeColors.card : themeColors.hover,
                                borderBottom: `1px solid ${themeColors.border}`
                              }}>
                                <td className="px-3 py-2">
                                  <input
                                    type="text"
                                    value={param.key}
                                    onChange={(e) => handleParameterChange(param.id, 'key', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.card,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                    placeholder="parameter_key"
                                  />
                                </td>
                                <td className="px-3 py-2">
                                  <input
                                    type="text"
                                    value={param.dbColumn || param.dbParameter || ''}
                                    onChange={(e) => handleParameterChange(param.id, 'dbColumn', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.card,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                    placeholder="DB_COLUMN"
                                  />
                                </td>
                                <td className="px-3 py-2">
                                  <select
                                    value={param.oracleType}
                                    onChange={(e) => handleParameterChange(param.id, 'oracleType', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                  >
                                    {oracleDataTypes.map(type => (
                                      <option key={type} value={type}>{type}</option>
                                    ))}
                                  </select>
                                </td>
                                <td className="px-3 py-2">
                                  <select
                                    value={param.apiType}
                                    onChange={(e) => handleParameterChange(param.id, 'apiType', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                  >
                                    {apiDataTypes.map(type => (
                                      <option key={type} value={type}>{type}</option>
                                    ))}
                                  </select>
                                </td>
                                <td className="px-3 py-2">
                                  <select
                                    value={param.parameterType}
                                    onChange={(e) => handleParameterChange(param.id, 'parameterType', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                  >
                                    {parameterTypes.map(type => (
                                      <option key={type.value} value={type.value}>{type.label}</option>
                                    ))}
                                  </select>
                                </td>
                                <td className="px-3 py-2 text-center">
                                  <input
                                    type="checkbox"
                                    checked={param.required}
                                    onChange={(e) => handleParameterChange(param.id, 'required', e.target.checked)}
                                    className="h-4 w-4 rounded"
                                    style={{ accentColor: themeColors.info }}
                                  />
                                </td>
                                <td className="px-3 py-2">
                                  <button
                                    onClick={() => handleRemoveParameter(param.id)}
                                    className="p-1.5 rounded transition-colors hover-lift"
                                    style={{ backgroundColor: themeColors.error + '20', color: themeColors.error }}
                                    title="Delete parameter"
                                  >
                                    <Trash2 className="h-4 w-4" />
                                  </button>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}

                    {/* Additional Parameter Details */}
                    {parameters.length > 0 && (
                      <div className="mt-6 space-y-4">
                        <h4 className="font-semibold" style={{ color: themeColors.text }}>
                          Parameter Details
                        </h4>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Default Value Pattern
                            </label>
                            <input
                              type="text"
                              placeholder="SYSDATE, USER, etc."
                              className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                            />
                          </div>
                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Validation Regex
                            </label>
                            <input
                              type="text"
                              placeholder="^[A-Za-z0-9_]+$"
                              className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                            />
                          </div>
                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Min/Max Values
                            </label>
                            <div className="flex gap-2">
                              <input
                                type="number"
                                placeholder="Min"
                                className="flex-1 px-3 py-2 border rounded-lg text-xs hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.card,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                              />
                              <input
                                type="number"
                                placeholder="Max"
                                className="flex-1 px-3 py-2 border rounded-lg text-xs hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.card,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                              />
                            </div>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* Mapping Tab */}
                {activeTab === 'mapping' && (
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                        Response Field Mapping ({responseMappings.length})
                        {selectedObject?.columns && (
                          <span className="text-xs font-normal ml-2" style={{ color: themeColors.textSecondary }}>
                            (Auto-generated from {selectedObject.columns.length} columns)
                          </span>
                        )}
                        {sourceObjectInfo.isSynonym && (
                          <span className="text-xs font-normal ml-2" style={{ color: themeColors.warning }}>
                            (Resolved from target {sourceObjectInfo.targetType})
                          </span>
                        )}
                      </h3>
                      <button
                        onClick={handleAddResponseMapping}
                        className="px-3 py-1.5 rounded-lg flex items-center gap-2 text-xs transition-colors hover-lift"
                        style={{ backgroundColor: themeColors.info, color: themeColors.white }}
                      >
                        <Plus className="h-4 w-4" />
                        Add Mapping
                      </button>
                    </div>

                    {responseMappings.length === 0 ? (
                      <div className="text-center py-8 border rounded-lg" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <Map className="h-12 w-12 mx-auto mb-3" style={{ color: themeColors.textSecondary }} />
                        <p style={{ color: themeColors.textSecondary }}>
                          No response mappings defined. They will be auto-generated from the selected object's columns.
                        </p>
                      </div>
                    ) : (
                      <div className="overflow-x-auto border rounded-lg" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <table className="w-full min-w-[800px]">
                          <thead>
                            <tr style={{ backgroundColor: themeColors.hover }}>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>API Field</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>DB Column</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Oracle Type</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>API Type</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Nullable</th>
                              <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Actions</th>
                            </tr>
                          </thead>
                          <tbody>
                            {responseMappings.map((mapping, index) => (
                              <tr key={mapping.id} style={{ 
                                backgroundColor: index % 2 === 0 ? themeColors.card : themeColors.hover,
                                borderBottom: `1px solid ${themeColors.border}`
                              }}>
                                <td className="px-3 py-2">
                                  <input
                                    type="text"
                                    value={mapping.apiField}
                                    onChange={(e) => handleResponseMappingChange(mapping.id, 'apiField', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.card,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                    placeholder="fieldName"
                                  />
                                </td>
                                <td className="px-3 py-2">
                                  <input
                                    type="text"
                                    value={mapping.dbColumn}
                                    onChange={(e) => handleResponseMappingChange(mapping.id, 'dbColumn', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.card,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                    placeholder="DB_COLUMN"
                                  />
                                </td>
                                <td className="px-3 py-2">
                                  <select
                                    value={mapping.oracleType}
                                    onChange={(e) => handleResponseMappingChange(mapping.id, 'oracleType', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                  >
                                    {oracleDataTypes.map(type => (
                                      <option key={type} value={type}>{type}</option>
                                    ))}
                                  </select>
                                </td>
                                <td className="px-3 py-2">
                                  <select
                                    value={mapping.apiType}
                                    onChange={(e) => handleResponseMappingChange(mapping.id, 'apiType', e.target.value)}
                                    className="w-full px-2 py-1 border rounded text-xs hover-lift"
                                    style={{ 
                                      backgroundColor: themeColors.bg,
                                      borderColor: themeColors.border,
                                      color: themeColors.text
                                    }}
                                  >
                                    {apiDataTypes.map(type => (
                                      <option key={type} value={type}>{type}</option>
                                    ))}
                                  </select>
                                </td>
                                <td className="px-3 py-2 text-center">
                                  <input
                                    type="checkbox"
                                    checked={mapping.nullable}
                                    onChange={(e) => handleResponseMappingChange(mapping.id, 'nullable', e.target.checked)}
                                    className="h-4 w-4 rounded"
                                    style={{ accentColor: themeColors.info }}
                                  />
                                </td>
                                <td className="px-3 py-2">
                                  <button
                                    onClick={() => handleRemoveResponseMapping(mapping.id)}
                                    className="p-1.5 rounded transition-colors hover-lift"
                                    style={{ backgroundColor: themeColors.error + '20', color: themeColors.error }}
                                    title="Delete mapping"
                                  >
                                    <Trash2 className="h-4 w-4" />
                                  </button>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}

                    {/* Additional Mapping Options */}
                    {responseMappings.length > 0 && (
                      <div className="mt-6 space-y-4">
                        <h4 className="font-semibold" style={{ color: themeColors.text }}>
                          Response Configuration
                        </h4>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                          <div className="space-y-4">
                            <div className="space-y-2">
                              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                Response Format
                              </label>
                              <select
                                value={responseBody.contentType}
                                onChange={(e) => handleResponseBodyChange('contentType', e.target.value)}
                                className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.bg,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                              >
                                <option value="application/json">JSON</option>
                                <option value="application/xml">XML</option>
                                <option value="text/csv">CSV</option>
                              </select>
                            </div>
                            <div className="space-y-2">
                              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                Include Metadata
                              </label>
                              <div className="flex items-center">
                                <input
                                  type="checkbox"
                                  checked={responseBody.includeMetadata}
                                  onChange={(e) => handleResponseBodyChange('includeMetadata', e.target.checked)}
                                  className="h-4 w-4 rounded"
                                  style={{ accentColor: themeColors.info }}
                                />
                                <span className="ml-2 text-xs" style={{ color: themeColors.textSecondary }}>
                                  Include timestamp, version, request ID
                                </span>
                              </div>
                            </div>
                          </div>
                          <div className="space-y-4">
                            <div className="space-y-2">
                              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                Compression
                              </label>
                              <select
                                value={responseBody.compression}
                                onChange={(e) => handleResponseBodyChange('compression', e.target.value)}
                                className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                                style={{ 
                                  backgroundColor: themeColors.bg,
                                  borderColor: themeColors.border,
                                  color: themeColors.text
                                }}
                              >
                                <option value="none">None</option>
                                <option value="gzip">Gzip</option>
                                <option value="deflate">Deflate</option>
                              </select>
                            </div>
                            <div className="space-y-2">
                              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                                Pretty Print
                              </label>
                              <div className="flex items-center">
                                <input
                                  type="checkbox"
                                  defaultChecked
                                  className="h-4 w-4 rounded"
                                  style={{ accentColor: themeColors.info }}
                                />
                                <span className="ml-2 text-xs" style={{ color: themeColors.textSecondary }}>
                                  Format JSON for readability
                                </span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* Authentication Tab */}
{activeTab === 'auth' && (
  <div className="space-y-6">
    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
      Authentication & Authorization
    </h3>
    
    {/* Auth Type Selection with Description */}
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      {[
        { 
          id: 'NONE', 
          label: 'Public (No Auth)', 
          icon: <Globe className="h-5 w-5" />,
          desc: 'Open access - suitable for public data only',
          warning: 'Use with caution - no authentication required'
        },
        { 
          id: 'API_KEY', 
          label: 'API Key', 
          icon: <Key className="h-5 w-5" />,
          desc: 'Simple key-based authentication for service-to-service',
          warning: 'Basic security - rotate keys regularly'
        },
        { 
          id: 'BASIC', 
          label: 'Basic Auth', 
          icon: <Lock className="h-5 w-5" />,
          desc: 'Username/password with Base64 encoding',
          warning: 'Use only with HTTPS - credentials sent in plaintext'
        },
        { 
          id: 'JWT', 
          label: 'JWT Bearer Token', 
          icon: <Shield className="h-5 w-5" />,
          desc: 'Stateless authentication with signed tokens',
          warning: 'Implement proper token validation and expiration'
        },
        { 
          id: 'OAUTH2', 
          label: 'OAuth 2.0', 
          icon: <Users className="h-5 w-5" />,
          desc: 'Industry standard for delegated authorization',
          warning: 'Complex setup - requires OAuth provider'
        },
        { 
          id: 'ORACLE_ROLES', 
          label: 'Oracle Database Roles', 
          icon: <Database className="h-5 w-5" />,
          desc: 'Leverage existing Oracle database security',
          warning: 'Direct database authentication - use with caution'
        },
        { 
          id: 'MUTUAL_TLS', 
          label: 'Mutual TLS (mTLS)', 
          icon: <ShieldCheck className="h-5 w-5" />,
          desc: 'Certificate-based mutual authentication',
          warning: 'Requires certificate management infrastructure'
        },
        { 
          id: 'SAML', 
          label: 'SAML 2.0', 
          icon: <Users className="h-5 w-5" />,
          desc: 'Enterprise SSO with SAML assertions',
          warning: 'Complex setup - requires IdP configuration'
        },
        { 
          id: 'LDAP', 
          label: 'LDAP / Active Directory', 
          icon: <Server className="h-5 w-5" />,
          desc: 'Integration with corporate directory services',
          warning: 'Requires LDAP server configuration'
        },
        { 
          id: 'CUSTOM', 
          label: 'Custom Auth Function', 
          icon: <Code className="h-5 w-5" />,
          desc: 'Implement your own authentication logic',
          warning: 'Full flexibility - security is your responsibility'
        }
      ].map((type) => (
        <button
          key={type.id}
          onClick={() => handleAuthConfigChange('authType', type.id)}
          className={`p-4 rounded-lg border-2 transition-all hover-lift ${
            authConfig.authType === type.id 
              ? 'border-blue-500 bg-blue-500/10' 
              : 'border-transparent hover:border-gray-600'
          }`}
          style={{ 
            backgroundColor: authConfig.authType === type.id ? themeColors.info + '20' : themeColors.card,
            borderColor: authConfig.authType === type.id ? themeColors.info : themeColors.border
          }}
        >
          <div className="flex items-start gap-3">
            <div className="p-2 rounded-lg" style={{ 
              backgroundColor: authConfig.authType === type.id ? themeColors.info + '30' : themeColors.hover 
            }}>
              <div style={{ color: authConfig.authType === type.id ? themeColors.info : themeColors.textSecondary }}>
                {type.icon}
              </div>
            </div>
            <div className="flex-1 text-left">
              <h4 className="font-medium text-sm" style={{ color: themeColors.text }}>
                {type.label}
              </h4>
              <p className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>
                {type.desc}
              </p>
              <p className="text-xs mt-2 p-1 rounded" style={{ 
                backgroundColor: themeColors.warning + '20',
                color: themeColors.warning
              }}>
                ⚠️ {type.warning}
              </p>
            </div>
          </div>
        </button>
      ))}
    </div>

    {/* Conditional Configuration Based on Auth Type */}
    <div className="space-y-6 mt-6">
      {/* API Key Configuration */}
      {authConfig.authType === 'API_KEY' && (
        <div className="p-4 rounded-lg border" style={{ 
          borderColor: themeColors.info + '40',
          backgroundColor: themeColors.info + '10'
        }}>
          <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.info }}>
            <Key className="h-5 w-5" />
            API Key Configuration
          </h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Header Name *
                </label>
                <input
                  type="text"
                  value={authConfig.apiKeyHeader}
                  onChange={(e) => handleAuthConfigChange('apiKeyHeader', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="X-API-Key"
                />
                <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                  HTTP header that will contain the API key
                </p>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Key Location
                </label>
                <select
                  value={authConfig.apiKeyLocation || 'header'}
                  onChange={(e) => handleAuthConfigChange('apiKeyLocation', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.bg,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                >
                  <option value="header">Header</option>
                  <option value="query">Query Parameter</option>
                  <option value="cookie">Cookie</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Key Prefix
                </label>
                <input
                  type="text"
                  value={authConfig.apiKeyPrefix || 'Bearer'}
                  onChange={(e) => handleAuthConfigChange('apiKeyPrefix', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="Bearer"
                />
                <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                  Optional prefix (e.g., "Bearer " for Authorization header)
                </p>
              </div>
            </div>

            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Key Validation Method
                </label>
                <select
                  value={authConfig.apiKeyValidation || 'database'}
                  onChange={(e) => handleAuthConfigChange('apiKeyValidation', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.bg,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                >
                  <option value="database">Database Lookup</option>
                  <option value="redis">Redis Cache</option>
                  <option value="jwt">JWT Validation</option>
                  <option value="custom">Custom Function</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Key Table/Function
                </label>
                <input
                  type="text"
                  value={authConfig.apiKeyTable || 'API_KEYS'}
                  onChange={(e) => handleAuthConfigChange('apiKeyTable', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="HR.API_KEYS"
                />
              </div>

              <div className="space-y-3">
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.apiKeyRotate || true}
                    onChange={(e) => handleAuthConfigChange('apiKeyRotate', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Auto-rotate keys every 90 days
                  </span>
                </div>
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.apiKeyIpRestriction || false}
                    onChange={(e) => handleAuthConfigChange('apiKeyIpRestriction', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Restrict by IP address
                  </span>
                </div>
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.apiKeyRateLimit || true}
                    onChange={(e) => handleAuthConfigChange('apiKeyRateLimit', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Apply rate limiting per key
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* JWT Configuration */}
      {authConfig.authType === 'JWT' && (
        <div className="p-4 rounded-lg border" style={{ 
          borderColor: themeColors.info + '40',
          backgroundColor: themeColors.info + '10'
        }}>
          <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.info }}>
            <Shield className="h-5 w-5" />
            JWT Bearer Token Configuration
          </h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  JWT Issuer (iss) *
                </label>
                <input
                  type="text"
                  value={authConfig.jwtIssuer}
                  onChange={(e) => handleAuthConfigChange('jwtIssuer', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="https://auth.example.com"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Audience (aud) *
                </label>
                <input
                  type="text"
                  value={authConfig.jwtAudience || 'api.example.com'}
                  onChange={(e) => handleAuthConfigChange('jwtAudience', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="api.example.com"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Signing Algorithm
                </label>
                <select
                  value={authConfig.jwtAlgorithm || 'RS256'}
                  onChange={(e) => handleAuthConfigChange('jwtAlgorithm', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.bg,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                >
                  <option value="HS256">HS256 (Symmetric)</option>
                  <option value="RS256">RS256 (Asymmetric)</option>
                  <option value="ES256">ES256 (ECDSA)</option>
                  <option value="PS256">PS256 (RSA-PSS)</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  JWKS URI (for RSA/ECDSA)
                </label>
                <input
                  type="text"
                  value={authConfig.jwksUri || 'https://auth.example.com/.well-known/jwks.json'}
                  onChange={(e) => handleAuthConfigChange('jwksUri', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="https://auth.example.com/.well-known/jwks.json"
                />
              </div>
            </div>

            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Token Expiration (seconds)
                </label>
                <input
                  type="number"
                  value={authConfig.jwtExpiration || 3600}
                  onChange={(e) => handleAuthConfigChange('jwtExpiration', parseInt(e.target.value))}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  min="60"
                  max="86400"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Clock Skew (seconds)
                </label>
                <input
                  type="number"
                  value={authConfig.jwtClockSkew || 30}
                  onChange={(e) => handleAuthConfigChange('jwtClockSkew', parseInt(e.target.value))}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  min="0"
                  max="300"
                />
              </div>

              <div className="space-y-3">
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.jwtValidateIssuer || true}
                    onChange={(e) => handleAuthConfigChange('jwtValidateIssuer', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Validate Issuer (iss)
                  </span>
                </div>
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.jwtValidateAudience || true}
                    onChange={(e) => handleAuthConfigChange('jwtValidateAudience', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Validate Audience (aud)
                  </span>
                </div>
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.jwtValidateLifetime || true}
                    onChange={(e) => handleAuthConfigChange('jwtValidateLifetime', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Validate Expiration (exp)
                  </span>
                </div>
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.jwtRefreshEnabled || false}
                    onChange={(e) => handleAuthConfigChange('jwtRefreshEnabled', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Enable Token Refresh
                  </span>
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Custom Claims Validation
                </label>
                <textarea
                  value={authConfig.jwtCustomClaims || '{\n  "role": ["admin", "user"],\n  "department": "HR"\n}'}
                  onChange={(e) => handleAuthConfigChange('jwtCustomClaims', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs font-mono hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  rows="3"
                  placeholder="JSON object with custom claim validation rules"
                />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* OAuth 2.0 Configuration */}
      {authConfig.authType === 'OAUTH2' && (
        <div className="p-4 rounded-lg border" style={{ 
          borderColor: themeColors.info + '40',
          backgroundColor: themeColors.info + '10'
        }}>
          <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.info }}>
            <Users className="h-5 w-5" />
            OAuth 2.0 Configuration
          </h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Authorization Server *
                </label>
                <input
                  type="text"
                  value={authConfig.oauthAuthServer || 'https://auth.example.com'}
                  onChange={(e) => handleAuthConfigChange('oauthAuthServer', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="https://auth.example.com"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Token Endpoint
                </label>
                <input
                  type="text"
                  value={authConfig.oauthTokenEndpoint || '/oauth/token'}
                  onChange={(e) => handleAuthConfigChange('oauthTokenEndpoint', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="/oauth/token"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Authorization Endpoint
                </label>
                <input
                  type="text"
                  value={authConfig.oauthAuthEndpoint || '/oauth/authorize'}
                  onChange={(e) => handleAuthConfigChange('oauthAuthEndpoint', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="/oauth/authorize"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Client ID *
                </label>
                <input
                  type="text"
                  value={authConfig.oauthClientId}
                  onChange={(e) => handleAuthConfigChange('oauthClientId', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="your-client-id"
                />
              </div>
            </div>

            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Client Secret
                </label>
                <input
                  type="password"
                  value={authConfig.oauthClientSecret}
                  onChange={(e) => handleAuthConfigChange('oauthClientSecret', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="••••••••••••••••"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Grant Type
                </label>
                <select
                  value={authConfig.oauthGrantType || 'client_credentials'}
                  onChange={(e) => handleAuthConfigChange('oauthGrantType', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.bg,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                >
                  <option value="client_credentials">Client Credentials</option>
                  <option value="authorization_code">Authorization Code</option>
                  <option value="password">Resource Owner Password</option>
                  <option value="refresh_token">Refresh Token</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Required Scopes
                </label>
                <div className="space-y-2">
                  {['read', 'write', 'admin', 'profile', 'email'].map(scope => (
                    <div key={scope} className="flex items-center">
                      <input
                        type="checkbox"
                        checked={authConfig.oauthScopes?.includes(scope)}
                        onChange={(e) => {
                          const currentScopes = authConfig.oauthScopes || [];
                          const newScopes = e.target.checked 
                            ? [...currentScopes, scope]
                            : currentScopes.filter(s => s !== scope);
                          handleAuthConfigChange('oauthScopes', newScopes);
                        }}
                        className="h-4 w-4 rounded"
                        style={{ accentColor: themeColors.info }}
                      />
                      <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                        {scope}
                      </span>
                    </div>
                  ))}
                </div>
                <input
                  type="text"
                  placeholder="Custom scopes (comma-separated)"
                  className="w-full mt-2 px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      const customScopes = e.target.value.split(',').map(s => s.trim());
                      handleAuthConfigChange('oauthScopes', [...(authConfig.oauthScopes || []), ...customScopes]);
                      e.target.value = '';
                    }
                  }}
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Redirect URIs (for Authorization Code)
                </label>
                <textarea
                  value={authConfig.oauthRedirectUris || 'https://app.example.com/callback\nhttps://app.example.com/oauth2/callback'}
                  onChange={(e) => handleAuthConfigChange('oauthRedirectUris', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  rows="2"
                  placeholder="One URI per line"
                />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Oracle Database Roles Configuration */}
      {authConfig.authType === 'ORACLE_ROLES' && (
        <div className="p-4 rounded-lg border" style={{ 
          borderColor: themeColors.info + '40',
          backgroundColor: themeColors.info + '10'
        }}>
          <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.info }}>
            <Database className="h-5 w-5" />
            Oracle Database Roles Configuration
          </h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Connection Type
                </label>
                <select
                  value={authConfig.oracleConnectionType || 'proxy'}
                  onChange={(e) => handleAuthConfigChange('oracleConnectionType', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.bg,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                >
                  <option value="proxy">Proxy User (Connection Pool)</option>
                  <option value="direct">Direct Connection</option>
                  <option value="ldap">LDAP Authentication</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Database User Pool
                </label>
                <input
                  type="text"
                  value={authConfig.oracleUserPool || 'APP_USER_POOL'}
                  onChange={(e) => handleAuthConfigChange('oracleUserPool', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="APP_USER_POOL"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Required Roles
                </label>
                <div className="space-y-2">
                  {['HR_APP_USER', 'HR_APP_ADMIN', 'HR_READONLY', 'FINANCE_USER', 'EMPLOYEE_VIEW'].map(role => (
                    <div key={role} className="flex items-center">
                      <input
                        type="checkbox"
                        checked={authConfig.requiredRoles?.includes(role)}
                        onChange={(e) => {
                          const currentRoles = authConfig.requiredRoles || [];
                          const newRoles = e.target.checked 
                            ? [...currentRoles, role]
                            : currentRoles.filter(r => r !== role);
                          handleAuthConfigChange('requiredRoles', newRoles);
                        }}
                        className="h-4 w-4 rounded"
                        style={{ accentColor: themeColors.info }}
                      />
                      <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                        {role}
                      </span>
                    </div>
                  ))}
                </div>
                <input
                  type="text"
                  placeholder="Custom roles (comma-separated)"
                  className="w-full mt-2 px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      const customRoles = e.target.value.split(',').map(r => r.trim());
                      handleAuthConfigChange('requiredRoles', [...(authConfig.requiredRoles || []), ...customRoles]);
                      e.target.value = '';
                    }
                  }}
                />
              </div>
            </div>

            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Privilege Check Function
                </label>
                <input
                  type="text"
                  value={authConfig.customAuthFunction || 'HR.CHECK_PRIVILEGES'}
                  onChange={(e) => handleAuthConfigChange('customAuthFunction', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="HR.CHECK_PRIVILEGES"
                />
                <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                  PL/SQL function that validates user privileges
                </p>
              </div>

              <div className="space-y-3">
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.validateSession}
                    onChange={(e) => handleAuthConfigChange('validateSession', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Validate Database Session
                  </span>
                </div>
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.checkObjectPrivileges}
                    onChange={(e) => handleAuthConfigChange('checkObjectPrivileges', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Check Object Privileges (SELECT/INSERT/etc)
                  </span>
                </div>
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.oracleAudit || true}
                    onChange={(e) => handleAuthConfigChange('oracleAudit', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Enable Fine-Grained Auditing (FGA)
                  </span>
                </div>
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.oracleVpd || false}
                    onChange={(e) => handleAuthConfigChange('oracleVpd', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Apply VPD (Virtual Private Database) Policies
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Mutual TLS Configuration */}
      {authConfig.authType === 'MUTUAL_TLS' && (
        <div className="p-4 rounded-lg border" style={{ 
          borderColor: themeColors.info + '40',
          backgroundColor: themeColors.info + '10'
        }}>
          <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.info }}>
            <ShieldCheck className="h-5 w-5" />
            Mutual TLS (mTLS) Configuration
          </h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  CA Certificate Bundle
                </label>
                <select
                  value={authConfig.mtlsCaBundle || 'internal'}
                  onChange={(e) => handleAuthConfigChange('mtlsCaBundle', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.bg,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                >
                  <option value="internal">Internal CA</option>
                  <option value="public">Public CA</option>
                  <option value="custom">Custom CA</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Certificate Validation
                </label>
                <div className="space-y-2">
                  <div className="flex items-center">
                    <input
                      type="checkbox"
                      checked={authConfig.mtlsValidateChain || true}
                      onChange={(e) => handleAuthConfigChange('mtlsValidateChain', e.target.checked)}
                      className="h-4 w-4 rounded"
                      style={{ accentColor: themeColors.info }}
                    />
                    <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                      Validate Certificate Chain
                    </span>
                  </div>
                  <div className="flex items-center">
                    <input
                      type="checkbox"
                      checked={authConfig.mtlsValidateExpiry || true}
                      onChange={(e) => handleAuthConfigChange('mtlsValidateExpiry', e.target.checked)}
                      className="h-4 w-4 rounded"
                      style={{ accentColor: themeColors.info }}
                    />
                    <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                      Check Expiration Dates
                    </span>
                  </div>
                  <div className="flex items-center">
                    <input
                      type="checkbox"
                      checked={authConfig.mtlsValidateRevocation || true}
                      onChange={(e) => handleAuthConfigChange('mtlsValidateRevocation', e.target.checked)}
                      className="h-4 w-4 rounded"
                      style={{ accentColor: themeColors.info }}
                    />
                    <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                      Check CRL/OCSP for Revocation
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Certificate Mapping
                </label>
                <select
                  value={authConfig.mtlsMapping || 'cn'}
                  onChange={(e) => handleAuthConfigChange('mtlsMapping', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.bg,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                >
                  <option value="cn">Map to Common Name (CN)</option>
                  <option value="san">Map to Subject Alternative Name (SAN)</option>
                  <option value="dn">Map to Distinguished Name (DN)</option>
                  <option value="custom">Custom Attribute</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Allowed Certificate OUs
                </label>
                <input
                  type="text"
                  value={authConfig.mtlsAllowedOUs || 'Engineering, DevOps, Security'}
                  onChange={(e) => handleAuthConfigChange('mtlsAllowedOUs', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="Comma-separated Organizational Units"
                />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* SAML 2.0 Configuration */}
      {authConfig.authType === 'SAML' && (
        <div className="p-4 rounded-lg border" style={{ 
          borderColor: themeColors.info + '40',
          backgroundColor: themeColors.info + '10'
        }}>
          <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.info }}>
            <Users className="h-5 w-5" />
            SAML 2.0 Configuration
          </h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Identity Provider (IdP) Metadata URL
                </label>
                <input
                  type="text"
                  value={authConfig.samlIdpMetadata || 'https://idp.example.com/metadata.xml'}
                  onChange={(e) => handleAuthConfigChange('samlIdpMetadata', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="https://idp.example.com/metadata.xml"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Entity ID (Issuer)
                </label>
                <input
                  type="text"
                  value={authConfig.samlEntityId || 'https://api.example.com/saml'}
                  onChange={(e) => handleAuthConfigChange('samlEntityId', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="https://api.example.com/saml"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Assertion Consumer Service (ACS) URL
                </label>
                <input
                  type="text"
                  value={authConfig.samlAcsUrl || 'https://api.example.com/saml/acs'}
                  onChange={(e) => handleAuthConfigChange('samlAcsUrl', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="https://api.example.com/saml/acs"
                />
              </div>
            </div>

            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Attribute Mapping
                </label>
                <textarea
                  value={authConfig.samlAttributeMapping || '{\n  "email": "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",\n  "name": "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name",\n  "role": "http://schemas.microsoft.com/ws/2008/06/identity/claims/role"\n}'}
                  onChange={(e) => handleAuthConfigChange('samlAttributeMapping', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs font-mono hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  rows="4"
                />
              </div>

              <div className="space-y-3">
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.samlWantAssertionsSigned || true}
                    onChange={(e) => handleAuthConfigChange('samlWantAssertionsSigned', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Require Signed Assertions
                  </span>
                </div>
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.samlWantAuthnRequestsSigned || true}
                    onChange={(e) => handleAuthConfigChange('samlWantAuthnRequestsSigned', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Sign AuthnRequests
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* LDAP/Active Directory Configuration */}
      {authConfig.authType === 'LDAP' && (
        <div className="p-4 rounded-lg border" style={{ 
          borderColor: themeColors.info + '40',
          backgroundColor: themeColors.info + '10'
        }}>
          <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.info }}>
            <Server className="h-5 w-5" />
            LDAP / Active Directory Configuration
          </h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  LDAP Server URL
                </label>
                <input
                  type="text"
                  value={authConfig.ldapUrl || 'ldaps://ldap.example.com:636'}
                  onChange={(e) => handleAuthConfigChange('ldapUrl', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="ldaps://ldap.example.com:636"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Base DN
                </label>
                <input
                  type="text"
                  value={authConfig.ldapBaseDn || 'dc=example,dc=com'}
                  onChange={(e) => handleAuthConfigChange('ldapBaseDn', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="dc=example,dc=com"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  User Search Filter
                </label>
                <input
                  type="text"
                  value={authConfig.ldapUserFilter || '(&(objectClass=user)(sAMAccountName={username}))'}
                  onChange={(e) => handleAuthConfigChange('ldapUserFilter', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="(&(objectClass=user)(sAMAccountName={username}))"
                />
              </div>
            </div>

            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Group Search Filter
                </label>
                <input
                  type="text"
                  value={authConfig.ldapGroupFilter || '(&(objectClass=group)(member={user}))'}
                  onChange={(e) => handleAuthConfigChange('ldapGroupFilter', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="(&(objectClass=group)(member={user}))"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Required Groups
                </label>
                <input
                  type="text"
                  value={authConfig.ldapRequiredGroups || 'CN=API-Users,OU=Groups,DC=example,DC=com'}
                  onChange={(e) => handleAuthConfigChange('ldapRequiredGroups', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="Comma-separated DNs"
                />
              </div>

              <div className="space-y-3">
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.ldapUseSsl || true}
                    onChange={(e) => handleAuthConfigChange('ldapUseSsl', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Use SSL/TLS
                  </span>
                </div>
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={authConfig.ldapFollowReferrals || false}
                    onChange={(e) => handleAuthConfigChange('ldapFollowReferrals', e.target.checked)}
                    className="h-4 w-4 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                    Follow Referrals
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Custom Auth Function Configuration */}
      {authConfig.authType === 'CUSTOM' && (
        <div className="p-4 rounded-lg border" style={{ 
          borderColor: themeColors.info + '40',
          backgroundColor: themeColors.info + '10'
        }}>
          <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.info }}>
            <Code className="h-5 w-5" />
            Custom Authentication Function
          </h4>
          <div className="space-y-4">
            <div className="space-y-2">
              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                PL/SQL Function Name *
              </label>
              <input
                type="text"
                value={authConfig.customAuthFunction}
                onChange={(e) => handleAuthConfigChange('customAuthFunction', e.target.value)}
                className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                style={{ 
                  backgroundColor: themeColors.card,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
                placeholder="HR.AUTHENTICATE_API_USER"
              />
              <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                Function should accept (p_token VARCHAR2, p_ip VARCHAR2) and return BOOLEAN or user_id
              </p>
            </div>

            <div className="space-y-2">
              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                Sample Function Signature
              </label>
              <pre className="p-3 rounded text-xs font-mono overflow-x-auto" style={{ 
                backgroundColor: themeColors.hover,
                color: themeColors.text,
                border: `1px solid ${themeColors.border}`
              }}>
{`CREATE OR REPLACE FUNCTION HR.AUTHENTICATE_API_USER (
  p_token VARCHAR2,
  p_ip_address VARCHAR2 DEFAULT NULL,
  p_user_agent VARCHAR2 DEFAULT NULL
) RETURN NUMBER
IS
  l_user_id NUMBER;
BEGIN
  -- Your custom authentication logic here
  SELECT user_id INTO l_user_id
  FROM api_tokens
  WHERE token = p_token
    AND expiry > SYSTIMESTAMP
    AND (ip_restriction IS NULL OR ip_restriction = p_ip_address);
    
  RETURN l_user_id;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    RETURN NULL;
END AUTHENTICATE_API_USER;`}
              </pre>
            </div>

            <div className="space-y-2">
              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                Parameters to Pass
              </label>
              <div className="space-y-2">
                {['Authorization Token', 'Client IP', 'User Agent', 'Request Path', 'HTTP Method'].map(param => (
                  <div key={param} className="flex items-center">
                    <input
                      type="checkbox"
                      defaultChecked={['Authorization Token', 'Client IP'].includes(param)}
                      className="h-4 w-4 rounded"
                      style={{ accentColor: themeColors.info }}
                    />
                    <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                      {param}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                Cache Duration (seconds)
              </label>
              <input
                type="number"
                value={authConfig.customAuthCache || 300}
                onChange={(e) => handleAuthConfigChange('customAuthCache', parseInt(e.target.value))}
                className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                style={{ 
                  backgroundColor: themeColors.card,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
                min="0"
                max="86400"
              />
              <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                Cache authentication results (0 = no caching)
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Basic Auth Configuration */}
      {authConfig.authType === 'BASIC' && (
        <div className="p-4 rounded-lg border" style={{ 
          borderColor: themeColors.warning + '40',
          backgroundColor: themeColors.warning + '10'
        }}>
          <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.warning }}>
            <Lock className="h-5 w-5" />
            Basic Authentication Configuration
          </h4>
          <div className="space-y-4">
            <div className="p-3 rounded" style={{ backgroundColor: themeColors.warning + '20' }}>
              <p className="text-xs flex items-center gap-2" style={{ color: themeColors.warning }}>
                <AlertCircle className="h-4 w-4" />
                Basic Authentication sends credentials in plaintext. Always use HTTPS.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  User Validation Method
                </label>
                <select
                  value={authConfig.basicValidation || 'database'}
                  onChange={(e) => handleAuthConfigChange('basicValidation', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.bg,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                >
                  <option value="database">Database Users</option>
                  <option value="ldap">LDAP/Active Directory</option>
                  <option value="oracle">Oracle Database Users</option>
                  <option value="custom">Custom Function</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Realm Name
                </label>
                <input
                  type="text"
                  value={authConfig.basicRealm || 'API Access'}
                  onChange={(e) => handleAuthConfigChange('basicRealm', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="API Access"
                />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                Password Validation Function
              </label>
              <input
                type="text"
                value={authConfig.basicPasswordFunction || 'HR.VALIDATE_PASSWORD'}
                onChange={(e) => handleAuthConfigChange('basicPasswordFunction', e.target.value)}
                className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                style={{ 
                  backgroundColor: themeColors.card,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
                placeholder="HR.VALIDATE_PASSWORD"
              />
            </div>
          </div>
        </div>
      )}

      {/* Public (No Auth) Configuration */}
      {authConfig.authType === 'NONE' && (
        <div className="p-4 rounded-lg border" style={{ 
          borderColor: themeColors.warning + '40',
          backgroundColor: themeColors.warning + '10'
        }}>
          <div className="flex items-start gap-3">
            <Globe className="h-5 w-5" style={{ color: themeColors.warning }} />
            <div>
              <h4 className="font-medium" style={{ color: themeColors.warning }}>
                Public API - No Authentication
              </h4>
              <p className="text-xs mt-1" style={{ color: themeColors.textSecondary }}>
                This API will be publicly accessible without any authentication. 
                Only use for non-sensitive data and implement rate limiting.
              </p>
              <div className="mt-3 p-2 rounded" style={{ backgroundColor: themeColors.card }}>
                <p className="text-xs flex items-center gap-2" style={{ color: themeColors.text }}>
                  <AlertCircle className="h-4 w-4" style={{ color: themeColors.warning }} />
                  Recommended: Enable rate limiting and CORS restrictions
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Common Security Settings (shown for all auth types except NONE) */}
      {authConfig.authType !== 'NONE' && (
        <div className="p-4 rounded-lg border mt-6" style={{ 
          borderColor: themeColors.border,
          backgroundColor: themeColors.card
        }}>
          <h4 className="font-medium mb-4 flex items-center gap-2" style={{ color: themeColors.text }}>
            <Shield className="h-5 w-5" />
            Additional Security Settings
          </h4>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  IP Whitelist
                </label>
                <textarea
                  value={authConfig.ipWhitelist || ''}
                  onChange={(e) => handleAuthConfigChange('ipWhitelist', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  rows="2"
                  placeholder="192.168.1.0/24&#10;10.0.0.1"
                />
                <p className="text-xs" style={{ color: themeColors.textSecondary }}>
                  One CIDR or IP per line (leave empty to allow all)
                </p>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Rate Limiting
                </label>
                <div className="grid grid-cols-2 gap-2">
                  <input
                    type="number"
                    placeholder="Requests"
                    value={authConfig.rateLimitRequests || 100}
                    onChange={(e) => handleAuthConfigChange('rateLimitRequests', parseInt(e.target.value))}
                    className="px-3 py-2 border rounded-lg text-xs hover-lift"
                    style={{ 
                      backgroundColor: themeColors.card,
                      borderColor: themeColors.border,
                      color: themeColors.text
                    }}
                    min="1"
                  />
                  <select
                    value={authConfig.rateLimitPeriod || 'minute'}
                    onChange={(e) => handleAuthConfigChange('rateLimitPeriod', e.target.value)}
                    className="px-3 py-2 border rounded-lg text-xs hover-lift"
                    style={{ 
                      backgroundColor: themeColors.bg,
                      borderColor: themeColors.border,
                      color: themeColors.text
                    }}
                  >
                    <option value="second">per second</option>
                    <option value="minute">per minute</option>
                    <option value="hour">per hour</option>
                    <option value="day">per day</option>
                  </select>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  CORS Configuration
                </label>
                <div className="space-y-2">
                  <input
                    type="text"
                    placeholder="Allowed Origins"
                    value={authConfig.corsOrigins?.join(', ') || '*'}
                    onChange={(e) => handleAuthConfigChange('corsOrigins', e.target.value.split(',').map(o => o.trim()))}
                    className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                    style={{ 
                      backgroundColor: themeColors.card,
                      borderColor: themeColors.border,
                      color: themeColors.text
                    }}
                  />
                  <div className="flex items-center">
                    <input
                      type="checkbox"
                      checked={authConfig.corsCredentials || false}
                      onChange={(e) => handleAuthConfigChange('corsCredentials', e.target.checked)}
                      className="h-4 w-4 rounded"
                      style={{ accentColor: themeColors.info }}
                    />
                    <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                      Allow Credentials
                    </span>
                  </div>
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                  Audit Level
                </label>
                <select
                  value={authConfig.auditLevel || 'standard'}
                  onChange={(e) => handleAuthConfigChange('auditLevel', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                  style={{ 
                    backgroundColor: themeColors.bg,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                >
                  <option value="none">None</option>
                  <option value="errors">Only Errors</option>
                  <option value="standard">Standard (Auth attempts)</option>
                  <option value="detailed">Detailed (All requests)</option>
                </select>
              </div>
            </div>
          </div>

          <div className="mt-4 p-3 rounded" style={{ backgroundColor: themeColors.hover }}>
            <h5 className="text-xs font-medium mb-2" style={{ color: themeColors.text }}>
              Security Headers
            </h5>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
              {[
                { key: 'X-Frame-Options', value: 'DENY' },
                { key: 'X-Content-Type-Options', value: 'nosniff' },
                { key: 'X-XSS-Protection', value: '1; mode=block' },
                { key: 'Strict-Transport-Security', value: 'max-age=31536000' },
                { key: 'Content-Security-Policy', value: "default-src 'none'" },
                { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' }
              ].map(header => (
                <div key={header.key} className="flex items-center">
                  <input
                    type="checkbox"
                    defaultChecked
                    className="h-3 w-3 rounded"
                    style={{ accentColor: themeColors.info }}
                  />
                  <span className="ml-1 text-xs" style={{ color: themeColors.textSecondary }}>
                    {header.key}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  </div>
)}

                {/* Request Tab */}
                {activeTab === 'request' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      Request Configuration
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Request Schema Type
                          </label>
                          <select
                            value={requestBody.schemaType}
                            onChange={(e) => handleRequestBodyChange('schemaType', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.bg,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          >
                            <option value="JSON">JSON</option>
                            <option value="XML">XML</option>
                            <option value="FORM_DATA">Form Data</option>
                            <option value="URL_ENCODED">URL Encoded</option>
                          </select>
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Max Request Size (bytes)
                          </label>
                          <input
                            type="number"
                            value={requestBody.maxSize}
                            onChange={(e) => handleRequestBodyChange('maxSize', parseInt(e.target.value))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            min="1024"
                            max="10485760"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Validate Schema
                          </label>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={requestBody.validateSchema}
                              onChange={(e) => handleRequestBodyChange('validateSchema', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.textSecondary }}>
                              Validate request body against schema
                            </span>
                          </div>
                        </div>
                      </div>

                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Allowed Media Types
                          </label>
                          <input
                            type="text"
                            value={requestBody.allowedMediaTypes.join(', ')}
                            onChange={(e) => handleRequestBodyChange('allowedMediaTypes', e.target.value.split(',').map(type => type.trim()))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="application/json, application/xml"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Required Fields
                          </label>
                          <input
                            type="text"
                            value={requestBody.requiredFields.join(', ')}
                            onChange={(e) => handleRequestBodyChange('requiredFields', e.target.value.split(',').map(field => field.trim()))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="id, name, email"
                          />
                        </div>
                      </div>
                    </div>

                    {/* Request Body Sample */}
                    <div className="space-y-4">
                      <h4 className="font-semibold" style={{ color: themeColors.text }}>
                        Request Body Sample
                      </h4>
                      <div className="border rounded-lg" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <div className="px-4 py-2 border-b flex items-center justify-between" style={{ borderColor: themeColors.border }}>
                          <span className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Sample JSON
                          </span>
                          <button
                            onClick={() => {
                              const sample = {
                                operation: schemaConfig.operation.toLowerCase(),
                                parameters: parameters.reduce((acc, param) => {
                                  if (param.example) {
                                    acc[param.key] = param.example;
                                  }
                                  return acc;
                                }, {}),
                                metadata: {
                                  requestId: "req_12345",
                                  timestamp: new Date().toISOString()
                                }
                              };
                              handleRequestBodyChange('sample', JSON.stringify(sample, null, 2));
                            }}
                            className="px-3 py-1 text-xs rounded border transition-colors hover-lift"
                            style={{ 
                              backgroundColor: themeColors.hover,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          >
                            Generate Sample
                          </button>
                        </div>
                        <textarea
                          value={requestBody.sample}
                          onChange={(e) => handleRequestBodyChange('sample', e.target.value)}
                          className="w-full h-48 px-4 py-3 text-xs font-mono resize-none focus:outline-none"
                          style={{ 
                            backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                            color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                          }}
                        />
                      </div>
                    </div>
                  </div>
                )}

                {/* Response Tab */}
                {activeTab === 'response' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      Response Configuration
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Success Schema
                          </label>
                          <div className="border rounded-lg" style={{ 
                            borderColor: themeColors.border,
                            backgroundColor: themeColors.card
                          }}>
                            <textarea
                              value={responseBody.successSchema}
                              onChange={(e) => handleResponseBodyChange('successSchema', e.target.value)}
                              className="w-full h-40 px-4 py-3 text-xs font-mono resize-none focus:outline-none"
                              style={{ 
                                backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                                color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                              }}
                            />
                          </div>
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Include Metadata Fields
                          </label>
                          <input
                            type="text"
                            value={responseBody.metadataFields.join(', ')}
                            onChange={(e) => handleResponseBodyChange('metadataFields', e.target.value.split(',').map(field => field.trim()))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="timestamp, apiVersion, requestId"
                          />
                        </div>
                      </div>

                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Error Schema
                          </label>
                          <div className="border rounded-lg" style={{ 
                            borderColor: themeColors.border,
                            backgroundColor: themeColors.card
                          }}>
                            <textarea
                              value={responseBody.errorSchema}
                              onChange={(e) => handleResponseBodyChange('errorSchema', e.target.value)}
                              className="w-full h-40 px-4 py-3 text-xs font-mono resize-none focus:outline-none"
                              style={{ 
                                backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                                color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                              }}
                            />
                          </div>
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Compression
                          </label>
                          <select
                            value={responseBody.compression}
                            onChange={(e) => handleResponseBodyChange('compression', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.bg,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          >
                            <option value="none">None</option>
                            <option value="gzip">Gzip</option>
                            <option value="deflate">Deflate</option>
                          </select>
                        </div>
                      </div>
                    </div>

                    {/* HTTP Status Codes */}
                    <div className="space-y-4">
                      <h4 className="font-semibold" style={{ color: themeColors.text }}>
                        HTTP Status Codes
                      </h4>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <div className="p-3 rounded-lg border text-center" style={{ 
                          borderColor: themeColors.success + '40',
                          backgroundColor: themeColors.success + '10'
                        }}>
                          <div className="text-lg font-bold" style={{ color: themeColors.success }}>200</div>
                          <div className="text-xs" style={{ color: themeColors.textSecondary }}>Success</div>
                        </div>
                        <div className="p-3 rounded-lg border text-center" style={{ 
                          borderColor: themeColors.error + '40',
                          backgroundColor: themeColors.error + '10'
                        }}>
                          <div className="text-lg font-bold" style={{ color: themeColors.error }}>400</div>
                          <div className="text-xs" style={{ color: themeColors.textSecondary }}>Bad Request</div>
                        </div>
                        <div className="p-3 rounded-lg border text-center" style={{ 
                          borderColor: themeColors.error + '40',
                          backgroundColor: themeColors.error + '10'
                        }}>
                          <div className="text-lg font-bold" style={{ color: themeColors.error }}>401</div>
                          <div className="text-xs" style={{ color: themeColors.textSecondary }}>Unauthorized</div>
                        </div>
                        <div className="p-3 rounded-lg border text-center" style={{ 
                          borderColor: themeColors.error + '40',
                          backgroundColor: themeColors.error + '10'
                        }}>
                          <div className="text-lg font-bold" style={{ color: themeColors.error }}>500</div>
                          <div className="text-xs" style={{ color: themeColors.textSecondary }}>Server Error</div>
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Tests Tab */}
                {activeTab === 'tests' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      Test Configuration
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Test Environment
                          </label>
                          <select
                            value={tests.testEnvironment}
                            onChange={(e) => handleTestsChange('testEnvironment', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.bg,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          >
                            <option value="development">Development</option>
                            <option value="staging">Staging</option>
                            <option value="production">Production</option>
                            <option value="custom">Custom</option>
                          </select>
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Performance Threshold (ms)
                          </label>
                          <input
                            type="number"
                            value={tests.performanceThreshold}
                            onChange={(e) => handleTestsChange('performanceThreshold', parseInt(e.target.value))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            min="100"
                            max="10000"
                          />
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Test Users
                            </label>
                            <input
                              type="number"
                              value={tests.testUsers}
                              onChange={(e) => handleTestsChange('testUsers', parseInt(e.target.value))}
                              className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                              min="1"
                              max="1000"
                            />
                          </div>
                          <div className="space-y-2">
                            <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                              Test Iterations
                            </label>
                            <input
                              type="number"
                              value={tests.testIterations}
                              onChange={(e) => handleTestsChange('testIterations', parseInt(e.target.value))}
                              className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                              min="1"
                              max="10000"
                            />
                          </div>
                        </div>
                      </div>

                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Unit Tests
                          </label>
                          <div className="border rounded-lg" style={{ 
                            borderColor: themeColors.border,
                            backgroundColor: themeColors.card
                          }}>
                            <textarea
                              value={tests.unitTests}
                              onChange={(e) => handleTestsChange('unitTests', e.target.value)}
                              className="w-full h-40 px-4 py-3 text-xs font-mono resize-none focus:outline-none"
                              style={{ 
                                backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                                color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                              }}
                              placeholder="-- PL/SQL unit tests will be generated here"
                            />
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Test Data */}
                    <div className="space-y-4">
                      <h4 className="font-semibold" style={{ color: themeColors.text }}>
                        Test Data
                      </h4>
                      <div className="border rounded-lg" style={{ 
                        borderColor: themeColors.border,
                        backgroundColor: themeColors.card
                      }}>
                        <textarea
                          value={tests.testData}
                          onChange={(e) => handleTestsChange('testData', e.target.value)}
                          className="w-full h-48 px-4 py-3 text-xs font-mono resize-none focus:outline-none"
                          style={{ 
                            backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                            color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                          }}
                          placeholder={`// Test data for ${schemaConfig.objectName}
{
  "testCases": [
    {
      "name": "Valid request",
      "parameters": {},
      "expectedStatus": 200
    }
  ]
}`}
                        />
                      </div>
                    </div>
                  </div>
                )}

                {/* Settings Tab */}
                {activeTab === 'settings' && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                      API Settings
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Timeout (ms)
                          </label>
                          <input
                            type="number"
                            value={settings.timeout}
                            onChange={(e) => handleSettingsChange('timeout', parseInt(e.target.value))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            min="1000"
                            max="60000"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Max Records
                          </label>
                          <input
                            type="number"
                            value={settings.maxRecords}
                            onChange={(e) => handleSettingsChange('maxRecords', parseInt(e.target.value))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            min="1"
                            max="10000"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Log Level
                          </label>
                          <select
                            value={settings.logLevel}
                            onChange={(e) => handleSettingsChange('logLevel', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.bg,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          >
                            <option value="DEBUG">DEBUG</option>
                            <option value="INFO">INFO</option>
                            <option value="WARN">WARN</option>
                            <option value="ERROR">ERROR</option>
                          </select>
                        </div>

                        <div className="space-y-3">
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.enableLogging}
                              onChange={(e) => handleSettingsChange('enableLogging', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Logging
                            </span>
                          </div>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.enableAudit}
                              onChange={(e) => handleSettingsChange('enableAudit', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Audit Trail
                            </span>
                          </div>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.enableMonitoring}
                              onChange={(e) => handleSettingsChange('enableMonitoring', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Monitoring
                            </span>
                          </div>
                        </div>
                      </div>

                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Rate Limit (requests)
                          </label>
                          <div className="flex items-center gap-4">
                            <input
                              type="number"
                              value={settings.rateLimit}
                              onChange={(e) => handleSettingsChange('rateLimit', parseInt(e.target.value))}
                              className="flex-1 px-3 py-2 border rounded-lg text-xs hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                              min="1"
                              max="10000"
                            />
                            <select
                              value={settings.rateLimitPeriod}
                              onChange={(e) => handleSettingsChange('rateLimitPeriod', e.target.value)}
                              className="flex-1 px-3 py-2 border rounded-lg text-xs hover-lift"
                              style={{ 
                                backgroundColor: themeColors.bg,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                            >
                              <option value="second">per second</option>
                              <option value="minute">per minute</option>
                              <option value="hour">per hour</option>
                              <option value="day">per day</option>
                            </select>
                          </div>
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Cache TTL (seconds)
                          </label>
                          <input
                            type="number"
                            value={settings.cacheTtl}
                            onChange={(e) => handleSettingsChange('cacheTtl', parseInt(e.target.value))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            min="0"
                            max="86400"
                          />
                        </div>

                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            CORS Origins
                          </label>
                          <input
                            type="text"
                            value={settings.corsOrigins.join(', ')}
                            onChange={(e) => handleSettingsChange('corsOrigins', e.target.value.split(',').map(origin => origin.trim()))}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="https://example.com, https://api.example.com"
                          />
                        </div>

                        <div className="space-y-3">
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.enableRateLimiting}
                              onChange={(e) => handleSettingsChange('enableRateLimiting', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Rate Limiting
                            </span>
                          </div>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.enableCaching}
                              onChange={(e) => handleSettingsChange('enableCaching', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Caching
                            </span>
                          </div>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.corsEnabled}
                              onChange={(e) => handleSettingsChange('corsEnabled', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable CORS
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Generation Options */}
                    <div className="space-y-4">
                      <h4 className="font-semibold" style={{ color: themeColors.text }}>
                        Generation Options
                      </h4>
                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div className="p-3 rounded-lg border flex items-center" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <input
                            type="checkbox"
                            checked={settings.generateSwagger}
                            onChange={(e) => handleSettingsChange('generateSwagger', e.target.checked)}
                            className="h-4 w-4 rounded mr-3"
                            style={{ accentColor: themeColors.info }}
                          />
                          <div>
                            <div className="font-medium text-xs" style={{ color: themeColors.text }}>OpenAPI Spec</div>
                            <div className="text-xs" style={{ color: themeColors.textSecondary }}>Generate Swagger documentation</div>
                          </div>
                        </div>
                        <div className="p-3 rounded-lg border flex items-center" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <input
                            type="checkbox"
                            checked={settings.generatePostman}
                            onChange={(e) => handleSettingsChange('generatePostman', e.target.checked)}
                            className="h-4 w-4 rounded mr-3"
                            style={{ accentColor: themeColors.info }}
                          />
                          <div>
                            <div className="font-medium text-xs" style={{ color: themeColors.text }}>Postman Collection</div>
                            <div className="text-xs" style={{ color: themeColors.textSecondary }}>Generate Postman tests</div>
                          </div>
                        </div>
                        <div className="p-3 rounded-lg border flex items-center" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <input
                            type="checkbox"
                            checked={settings.generateClientSDK}
                            onChange={(e) => handleSettingsChange('generateClientSDK', e.target.checked)}
                            className="h-4 w-4 rounded mr-3"
                            style={{ accentColor: themeColors.info }}
                          />
                          <div>
                            <div className="font-medium text-xs" style={{ color: themeColors.text }}>Client SDK</div>
                            <div className="text-xs" style={{ color: themeColors.textSecondary }}>Generate client libraries</div>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Alerts Configuration */}
                    <div className="space-y-4">
                      <h4 className="font-semibold" style={{ color: themeColors.text }}>
                        Alert Configuration
                      </h4>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="space-y-2">
                          <label className="text-xs font-medium" style={{ color: themeColors.text }}>
                            Alert Email
                          </label>
                          <input
                            type="email"
                            value={settings.alertEmail}
                            onChange={(e) => handleSettingsChange('alertEmail', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-xs hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="admin@example.com"
                          />
                        </div>
                        <div className="space-y-3 pt-6">
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.enableAlerts}
                              onChange={(e) => handleSettingsChange('enableAlerts', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Email Alerts
                            </span>
                          </div>
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={settings.enableTracing}
                              onChange={(e) => handleSettingsChange('enableTracing', e.target.checked)}
                              className="h-4 w-4 rounded"
                              style={{ accentColor: themeColors.info }}
                            />
                            <span className="ml-2 text-xs" style={{ color: themeColors.text }}>
                              Enable Distributed Tracing
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Preview Tab */}
                {activeTab === 'preview' && (
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <h3 className="text-lg font-semibold" style={{ color: themeColors.text }}>
                        Generated Code Preview
                        <span className="text-xs font-normal ml-2" style={{ color: themeColors.textSecondary }}>
                          (Based on {selectedObject?.name || 'selected object'})
                        </span>
                      </h3>
                      <div className="flex items-center gap-2">
                        <select
                          value={previewMode}
                          onChange={(e) => setPreviewMode(e.target.value)}
                          className="px-3 py-1.5 border rounded-lg text-xs hover-lift"
                          style={{ 
                            backgroundColor: themeColors.bg,
                            borderColor: themeColors.border,
                            color: themeColors.text
                          }}
                        >
                          <option value="json">Configuration JSON</option>
                          <option value="plsql">PL/SQL Package</option>
                          <option value="openapi">OpenAPI Spec</option>
                          <option value="postman">Postman Collection</option>
                        </select>
                        <button
                          onClick={copyGeneratedCode}
                          className="px-3 py-1.5 rounded-lg flex items-center gap-2 transition-colors hover-lift"
                          style={{ backgroundColor: themeColors.hover, color: themeColors.text }}
                        >
                          <Copy className="h-4 w-4" />
                          Copy
                        </button>
                        <button
                          onClick={downloadGeneratedCode}
                          className="px-3 py-1.5 rounded-lg flex items-center gap-2 transition-colors hover-lift"
                          style={{ backgroundColor: themeColors.info, color: themeColors.white }}
                        >
                          <Download className="h-4 w-4" />
                          Download
                        </button>
                      </div>
                    </div>

                    <div className="border rounded-lg" style={{ 
                      borderColor: themeColors.border,
                      backgroundColor: themeColors.card
                    }}>
                      <div className="px-4 py-2 border-b flex items-center justify-between" style={{ borderColor: themeColors.border }}>
                        <span className="text-xs font-medium" style={{ color: themeColors.text }}>
                          {previewMode === 'plsql' ? 'PL/SQL Package' : 
                           previewMode === 'openapi' ? 'OpenAPI Specification' :
                           previewMode === 'postman' ? 'Postman Collection' : 'Configuration'}
                          <span className="ml-2 text-xs" style={{ color: themeColors.textSecondary }}>
                            (Source: {schemaConfig.schemaName}.{schemaConfig.objectName})
                          </span>
                        </span>
                        <span className="text-xs font-mono" style={{ color: themeColors.textSecondary }}>
                          {previewMode === 'plsql' ? '.sql' : '.json'}
                        </span>
                      </div>
                      <pre className="w-full h-[400px] px-4 py-3 overflow-auto text-xs" style={{ 
                        backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                        color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                      }}>
                        {generatedCode}
                      </pre>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Footer */}
          <div className="px-6 py-4 border-t flex items-center justify-between" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.card
          }}>
            <div className="text-xs" style={{ color: themeColors.textSecondary }}>
              Endpoint: <span className="font-mono font-medium" style={{ color: themeColors.text }}>
                {apiDetails.httpMethod} {apiDetails.basePath}{apiDetails.endpointPath}
              </span>
              <br />
              <span className="text-xs" style={{ color: themeColors.textSecondary }}>
                Source: {schemaConfig.schemaName}.{schemaConfig.objectName} ({schemaConfig.objectType})
              </span>
              {sourceObjectInfo.isSynonym && (
                <span className="text-xs block mt-1" style={{ color: themeColors.warning }}>
                  <Link className="h-3 w-3 inline mr-1" />
                  Synonym → {sourceObjectInfo.targetType}: {sourceObjectInfo.targetOwner}.{sourceObjectInfo.targetName}
                </span>
              )}
            </div>
            <div className="flex items-center gap-3">
              <button
                onClick={onClose}
                className="px-4 py-2 border rounded-lg transition-colors hover-lift"
                style={{ 
                  backgroundColor: themeColors.hover,
                  borderColor: themeColors.border,
                  color: themeColors.text
                }}
                disabled={loading}
              >
                Cancel
              </button>
              <button
                onClick={handleSave}
                className="px-4 py-2 rounded-lg flex items-center gap-2 transition-colors hover-lift"
                style={{ backgroundColor: themeColors.success, color: themeColors.white }}
                disabled={loading}
              >
                {loading ? (
                  <>
                    <Loader className="h-4 w-4 animate-spin" />
                    Loading...
                  </>
                ) : (
                  <>
                    <Save className="h-4 w-4" />
                    Generate & Save API
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Add the preview modal */}
      <ApiPreviewModal
        isOpen={previewOpen}
        onClose={() => setPreviewOpen(false)}
        onConfirm={handlePreviewConfirm}
        apiData={newApiData}
        colors={colors}
        theme={theme}
      />

      {/* Add the loading modal */}
      <ApiLoadingModal
        isOpen={loadingOpen}
        colors={colors}
        theme={theme}
      />

      {/* Add the confirmation modal */}
      <ApiConfirmationModal
        isOpen={confirmationOpen}
        onClose={handleConfirmationClose}
        apiData={newApiData}
        colors={colors}
        theme={theme}
      />
    </>
  );
}