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
  };

  if (!isOpen || !apiData) return null;

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-50 p-4" style={{ zIndex: 1002 }}>
      <div className="rounded-xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col" style={{ 
        backgroundColor: themeColors.modalBg,
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
              <h2 className="text-xl font-bold" style={{ color: themeColors.text }}>
                API Generation Preview
              </h2>
              <p className="text-sm" style={{ color: themeColors.textSecondary }}>
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
                  <div className="grid grid-cols-2 gap-4 text-sm">
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
                  <div className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium" style={{ 
                    backgroundColor: themeColors.info + '20',
                    color: themeColors.info
                  }}>
                    <Globe className="h-4 w-4 mr-1" />
                    API Endpoint
                  </div>
                  <div className="mt-2 font-mono text-sm" style={{ color: themeColors.text }}>
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
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
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
                      <div key={index} className="flex items-center justify-between text-sm">
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
                        <span className="text-sm" style={{ color: themeColors.textSecondary }}>
                          + {apiData.parameters.length - 3} more parameters
                        </span>
                      </div>
                    )}
                  </div>
                ) : (
                  <p className="text-sm" style={{ color: themeColors.textSecondary }}>
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
                  <div className="grid grid-cols-2 gap-4 text-sm">
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
                        <span className="text-sm" style={{ color: themeColors.textSecondary }}>
                          + {apiData.responseMappings.length - 6} more fields
                        </span>
                      </div>
                    )}
                  </div>
                ) : (
                  <p className="text-sm" style={{ color: themeColors.textSecondary }}>
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
                      <h5 className="font-medium text-sm" style={{ color: themeColors.text }}>PL/SQL Package</h5>
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
                      <h5 className="font-medium text-sm" style={{ color: themeColors.text }}>OpenAPI Spec</h5>
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
                      <h5 className="font-medium text-sm" style={{ color: themeColors.text }}>Postman Collection</h5>
                      <p className="text-xs" style={{ color: themeColors.textSecondary }}>API Testing</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t flex flex-col sm:flex-row items-center justify-between gap-4" style={{ 
          borderColor: themeColors.border,
          backgroundColor: themeColors.card
        }}>
          <div className="text-sm" style={{ color: themeColors.textSecondary }}>
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
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-50" style={{ zIndex: 1003 }}>
      <div className="rounded-xl shadow-2xl w-full max-w-md p-6" style={{ 
        backgroundColor: themeColors.modalBg,
        border: `1px solid ${themeColors.modalBorder}`
      }}>
        <div className="text-center space-y-4">
          <div className="inline-flex">
            <Loader className="h-12 w-12 animate-spin" style={{ color: themeColors.info }} />
          </div>
          <div>
            <h3 className="text-xl font-bold mb-2" style={{ color: themeColors.text }}>
              Generating Your API
            </h3>
            <p className="text-sm" style={{ color: themeColors.textSecondary }}>
              Creating PL/SQL package, OpenAPI specification, and Postman collection...
            </p>
          </div>
          <div className="pt-4 space-y-3">
            <div className="flex items-center gap-3 text-sm" style={{ color: themeColors.textSecondary }}>
              <Loader className="h-4 w-4 animate-spin" style={{ color: themeColors.info }} />
              <span>Creating PL/SQL package...</span>
            </div>
            <div className="flex items-center gap-3 text-sm" style={{ color: themeColors.textSecondary }}>
              <Loader className="h-4 w-4 animate-spin" style={{ color: themeColors.info }} />
              <span>Generating OpenAPI specification...</span>
            </div>
            <div className="flex items-center gap-3 text-sm" style={{ color: themeColors.textSecondary }}>
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
          backgroundColor: themeColors.modalBg,
          border: `1px solid ${themeColors.modalBorder}`
        }}>
          <div className="text-center space-y-4">
            <div className="inline-flex">
              <Loader className="h-12 w-12 animate-spin" style={{ color: themeColors.success }} />
            </div>
            <div>
              <h3 className="text-xl font-bold mb-2" style={{ color: themeColors.text }}>
                Finalizing API Generation
              </h3>
              <p className="text-sm" style={{ color: themeColors.textSecondary }}>
                Completing the API setup and deploying to the registry...
              </p>
            </div>
            <div className="pt-4 space-y-3">
              <div className="flex items-center gap-3 text-sm" style={{ color: themeColors.textSecondary }}>
                <Loader className="h-4 w-4 animate-spin" style={{ color: themeColors.success }} />
                <span>Validating API configuration...</span>
              </div>
              <div className="flex items-center gap-3 text-sm" style={{ color: themeColors.textSecondary }}>
                <Loader className="h-4 w-4 animate-spin" style={{ color: themeColors.success }} />
                <span>Registering in API registry...</span>
              </div>
              <div className="flex items-center gap-3 text-sm" style={{ color: themeColors.textSecondary }}>
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
          backgroundColor: themeColors.modalBg,
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
                <h2 className="text-xl font-bold" style={{ color: themeColors.text }}>
                  API Generated Successfully!
                </h2>
                <p className="text-sm" style={{ color: themeColors.textSecondary }}>
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
                    <div className="grid grid-cols-2 gap-4 text-sm">
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
                    <div className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium" style={{ 
                      backgroundColor: themeColors.info + '20',
                      color: themeColors.info
                    }}>
                      <Globe className="h-4 w-4 mr-1" />
                      API Endpoint
                    </div>
                    <div className="mt-2 font-mono text-sm" style={{ color: themeColors.text }}>
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
                        <p className="text-sm" style={{ color: themeColors.textSecondary }}>
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
                        <p className="text-sm" style={{ color: themeColors.textSecondary }}>
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
                        <p className="text-sm" style={{ color: themeColors.textSecondary }}>
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
          <div className="px-6 py-4 border-t flex flex-col sm:flex-row items-center justify-between gap-4" style={{ 
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
  theme = 'dark'
}) {
  const [activeTab, setActiveTab] = useState('definition');
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

  // Initialize parameters and mappings based on selected object
  useEffect(() => {
    if (selectedObject) {
      console.log('Initializing modal with selected object:', {
        name: selectedObject.name,
        type: selectedObject.type,
        owner: selectedObject.owner,
        columns: selectedObject.columns?.length,
        parameters: selectedObject.parameters?.length
      });

      // Set API details based on selected object
      const baseName = selectedObject.name.toLowerCase();
      const endpointPath = `/${baseName.replace(/_/g, '-').toLowerCase()}`;
      
      // Determine HTTP method based on object type
      let httpMethod = 'GET';
      let operation = 'SELECT';
      
      if (selectedObject.type === 'TABLE' || selectedObject.type === 'VIEW') {
        httpMethod = 'GET';
        operation = 'SELECT';
      } else if (selectedObject.type === 'PROCEDURE' || selectedObject.type === 'FUNCTION' || selectedObject.type === 'PACKAGE') {
        httpMethod = 'POST';
        operation = 'EXECUTE';
      }

      setApiDetails(prev => ({
        ...prev,
        apiName: `${selectedObject.name} API`,
        apiCode: `${selectedObject.type.slice(0, 3)}_${selectedObject.name}`,
        description: selectedObject.comment || `API for ${selectedObject.name} ${selectedObject.type.toLowerCase()}`,
        endpointPath: endpointPath,
        owner: selectedObject.owner || 'HR',
        httpMethod: httpMethod
      }));

      // Set schema config
      setSchemaConfig(prev => ({
        ...prev,
        schemaName: selectedObject.owner || 'HR',
        objectType: selectedObject.type || 'TABLE',
        objectName: selectedObject.name || '',
        operation: operation,
        primaryKeyColumn: selectedObject.columns?.find(col => col.key === 'PK')?.name || ''
      }));

      // Clear existing parameters and mappings
      const newParameters = [];
      const newMappings = [];

      // Auto-generate parameters from columns if it's a table or view
      if (selectedObject.columns && selectedObject.columns.length > 0) {
        console.log('Generating parameters from columns:', selectedObject.columns);
        
        selectedObject.columns.forEach((col, index) => {
          const isPrimaryKey = col.key === 'PK';
          const parameterType = isPrimaryKey ? 'path' : 'query';
          
          newParameters.push({
            id: `param-${Date.now()}-${index}`,
            key: col.name.toLowerCase(),
            dbColumn: col.name,
            oracleType: col.type.includes('VARCHAR') ? 'VARCHAR2' : 
                      col.type.includes('NUMBER') ? 'NUMBER' :
                      col.type.includes('DATE') ? 'DATE' : 'VARCHAR2',
            apiType: col.type.includes('NUMBER') ? 'integer' : 'string',
            parameterType: parameterType,
            required: isPrimaryKey || col.nullable === 'N',
            description: col.comment || `From ${selectedObject.name}.${col.name}`,
            example: col.name === 'EMPLOYEE_ID' ? '100' : 
                    col.name.includes('DATE') ? '2024-01-01' :
                    col.name.includes('NAME') ? 'John' : '',
            validationPattern: '',
            defaultValue: col.defaultValue || ''
          });

          // Add response mapping
          newMappings.push({
            id: `mapping-${Date.now()}-${index}`,
            apiField: col.name.toLowerCase(),
            dbColumn: col.name,
            oracleType: col.type.includes('VARCHAR') ? 'VARCHAR2' : 
                      col.type.includes('NUMBER') ? 'NUMBER' :
                      col.type.includes('DATE') ? 'DATE' : 'VARCHAR2',
            apiType: col.type.includes('NUMBER') ? 'integer' : 'string',
            format: col.type.includes('DATE') ? 'date-time' : '',
            nullable: col.nullable === 'Y',
            isPrimaryKey: isPrimaryKey,
            includeInResponse: true
          });
        });
      } else if (selectedObject.parameters && selectedObject.parameters.length > 0) {
        // For procedures/functions, extract parameters
        console.log('Generating parameters from procedure/function:', selectedObject.parameters);
        
        selectedObject.parameters.forEach((param, index) => {
          newParameters.push({
            id: `proc-param-${Date.now()}-${index}`,
            key: param.name.replace('p_', '').toLowerCase(),
            dbParameter: param.name,
            oracleType: param.datatype,
            apiType: param.datatype.includes('NUMBER') ? 'integer' : 'string',
            parameterType: 'query',
            required: param.type === 'IN',
            description: param.name,
            example: '',
            validationPattern: '',
            defaultValue: param.defaultValue || ''
          });
        });

        // If there's a return type for functions
        if (selectedObject.returnType) {
          newMappings.push({
            id: `mapping-${Date.now()}-return`,
            apiField: 'result',
            dbColumn: 'RETURN_VALUE',
            oracleType: selectedObject.returnType,
            apiType: selectedObject.returnType.includes('NUMBER') ? 'integer' : 'string',
            format: '',
            nullable: false,
            isPrimaryKey: false,
            includeInResponse: true
          });
        }
      } else {
        // For other object types (packages, triggers, etc.)
        console.log('No columns or parameters found for object type:', selectedObject.type);
      }

      setParameters(newParameters);
      setResponseMappings(newMappings);
    } else {
      console.log('No selected object provided to modal');
    }
  }, [selectedObject]);

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
          settings
        }, null, 2));
    }
  }, [previewMode, apiDetails, schemaConfig, parameters, responseMappings, authConfig, settings]);

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
        parameters: selectedObject?.parameters?.length
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
               <Globe className="h-6 w-6" style={{ color: themeColors.info }} />}
              <div>
                <h2 className="text-xl font-bold" style={{ color: themeColors.text }}>
                  Generate API from {selectedObject?.type || 'Object'}: {selectedObject?.name || ''}
                </h2>
                <p className="text-sm" style={{ color: themeColors.textSecondary }}>
                  {selectedObject?.owner}.{selectedObject?.name} • {selectedObject?.type} • 
                  {selectedObject?.columns ? ` ${selectedObject.columns.length} columns` : ''}
                  {selectedObject?.parameters ? ` ${selectedObject.parameters.length} parameters` : ''}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={onClose}
                className="p-2 rounded-lg transition-colors hover-lift"
                style={{ backgroundColor: themeColors.hover, color: themeColors.textSecondary }}
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
                <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                  API Name *
                </label>
                <input
                  type="text"
                  value={apiDetails.apiName}
                  onChange={(e) => handleApiDetailChange('apiName', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="Users API"
                />
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                  API Code *
                </label>
                <input
                  type="text"
                  value={apiDetails.apiCode}
                  onChange={(e) => handleApiDetailChange('apiCode', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="GET_USERS"
                />
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                  HTTP Method
                </label>
                <select
                  value={apiDetails.httpMethod}
                  onChange={(e) => handleApiDetailChange('httpMethod', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                >
                  <option value="GET">GET</option>
                  <option value="POST">POST</option>
                  <option value="PUT">PUT</option>
                  <option value="DELETE">DELETE</option>
                  <option value="PATCH">PATCH</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                  Status
                </label>
                <select
                  value={apiDetails.status}
                  onChange={(e) => handleApiDetailChange('status', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                >
                  <option value="DRAFT">Draft</option>
                  <option value="ACTIVE">Active</option>
                  <option value="DEPRECATED">Deprecated</option>
                  <option value="ARCHIVED">Archived</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                  Base Path
                </label>
                <input
                  type="text"
                  value={apiDetails.basePath}
                  onChange={(e) => handleApiDetailChange('basePath', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="/api/v1"
                />
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                  Endpoint Path *
                </label>
                <input
                  type="text"
                  value={apiDetails.endpointPath}
                  onChange={(e) => handleApiDetailChange('endpointPath', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="/users"
                />
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                  Version
                </label>
                <input
                  type="text"
                  value={apiDetails.version}
                  onChange={(e) => handleApiDetailChange('version', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  placeholder="1.0.0"
                />
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                  Owner
                </label>
                <input
                  type="text"
                  value={apiDetails.owner}
                  onChange={(e) => handleApiDetailChange('owner', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                  Description
                </label>
                <textarea
                  value={apiDetails.description}
                  onChange={(e) => handleApiDetailChange('description', e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                  style={{ 
                    backgroundColor: themeColors.card,
                    borderColor: themeColors.border,
                    color: themeColors.text
                  }}
                  rows="2"
                  placeholder="API description..."
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
                  className={`px-3 py-2 flex items-center gap-2 border-b-2 text-sm font-medium transition-colors whitespace-nowrap hover-lift ${
                    activeTab === tab.id
                      ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === tab.id ? themeColors.info : 'transparent',
                    color: activeTab === tab.id ? themeColors.info : themeColors.textSecondary,
                    backgroundColor: 'transparent'
                  }}
                >
                  {tab.icon}
                  {tab.label}
                </button>
              ))}
            </div>
          </div>

          {/* Tab Content */}
          <div className="flex-1 overflow-y-auto">
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Category
                        </label>
                        <select
                          value={apiDetails.category}
                          onChange={(e) => handleApiDetailChange('category', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Tags
                        </label>
                        <input
                          type="text"
                          value={apiDetails.tags.join(', ')}
                          onChange={(e) => handleApiDetailChange('tags', e.target.value.split(',').map(tag => tag.trim()))}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                      <div className="font-mono text-sm p-3 rounded border" style={{ 
                        backgroundColor: themeColors.card,
                        borderColor: themeColors.border
                      }}>
                        <div style={{ color: themeColors.textSecondary }}>
                          {apiDetails.httpMethod} {apiDetails.basePath}{apiDetails.endpointPath}
                        </div>
                        {parameters.filter(p => p.parameterType === 'path').length > 0 && (
                          <div className="mt-2 text-sm" style={{ color: themeColors.textSecondary }}>
                            Path Parameters: {parameters.filter(p => p.parameterType === 'path').map(p => `{${p.key}}`).join('/')}
                          </div>
                        )}
                        <div className="mt-2 text-sm" style={{ color: themeColors.textSecondary }}>
                          Source: {schemaConfig.schemaName}.{schemaConfig.objectName} ({schemaConfig.objectType})
                        </div>
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
                    <div className="grid grid-cols-2 gap-4 text-sm">
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

                    {/* ADDED BACK: Show parameters from selected object */}
                    {selectedObject?.parameters && selectedObject.parameters.length > 0 && (
                      <div className="mt-4">
                        <h5 className="text-sm font-medium mb-2" style={{ color: themeColors.text }}>
                          Object Parameters:
                        </h5>
                        <div className="space-y-2 max-h-48 overflow-y-auto pr-2">
                          {selectedObject.parameters.map((param, index) => (
                            <div key={index} className="flex items-center justify-between text-sm p-2 rounded" 
                              style={{ backgroundColor: themeColors.hover }}>
                              <div>
                                <span className="font-medium" style={{ color: themeColors.text }}>
                                  {param.name}
                                </span>
                                <span className="ml-2 text-xs px-2 py-0.5 rounded" style={{ 
                                  backgroundColor: param.type === 'IN' ? themeColors.info + '20' : 
                                                param.type === 'OUT' ? themeColors.success + '20' : 
                                                themeColors.warning + '20',
                                  color: param.type === 'IN' ? themeColors.info : 
                                        param.type === 'OUT' ? themeColors.success : 
                                        themeColors.warning
                                }}>
                                  {param.type}
                                </span>
                              </div>
                              <div style={{ color: themeColors.textSecondary }}>
                                {param.datatype}
                                {param.defaultValue && (
                                  <span className="ml-2 text-xs">
                                    (Default: {param.defaultValue})
                                  </span>
                                )}
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* ADDED BACK: Show columns from selected object */}
                    {selectedObject?.columns && selectedObject.columns.length > 0 && (
                      <div className="mt-4">
                        <h5 className="text-sm font-medium mb-2" style={{ color: themeColors.text }}>
                          Object Columns (Auto-generated as parameters):
                        </h5>
                        <div className="space-y-2 max-h-48 overflow-y-auto pr-2">
                          {selectedObject.columns.slice(0, 8).map((col, index) => (
                            <div key={index} className="flex items-center justify-between text-sm p-2 rounded" 
                              style={{ backgroundColor: themeColors.hover }}>
                              <div>
                                <span className="font-medium" style={{ color: themeColors.text }}>
                                  {col.name}
                                </span>
                                {col.key === 'PK' && (
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
                                  {col.type}
                                </span>
                                <span className="text-xs px-2 py-0.5 rounded" style={{ 
                                  backgroundColor: col.nullable === 'Y' ? themeColors.warning + '20' : themeColors.error + '20',
                                  color: col.nullable === 'Y' ? themeColors.warning : themeColors.error
                                }}>
                                  {col.nullable === 'Y' ? 'NULL' : 'NOT NULL'}
                                </span>
                              </div>
                            </div>
                          ))}
                          {selectedObject.columns.length > 8 && (
                            <div className="text-center pt-2">
                              <span className="text-sm" style={{ color: themeColors.textSecondary }}>
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
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="space-y-4">
                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Schema Name *
                        </label>
                        <input
                          type="text"
                          value={schemaConfig.schemaName}
                          onChange={(e) => handleSchemaConfigChange('schemaName', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
                            borderColor: themeColors.border,
                            color: themeColors.text
                          }}
                          placeholder="HR"
                        />
                      </div>

                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Object Type
                        </label>
                        <select
                          value={schemaConfig.objectType}
                          onChange={(e) => handleSchemaConfigChange('objectType', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
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
                        </select>
                      </div>

                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Object Name *
                        </label>
                        <input
                          type="text"
                          value={schemaConfig.objectName}
                          onChange={(e) => handleSchemaConfigChange('objectName', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
                            borderColor: themeColors.border,
                            color: themeColors.text
                          }}
                          placeholder="EMPLOYEES"
                        />
                      </div>

                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Operation
                        </label>
                        <select
                          value={schemaConfig.operation}
                          onChange={(e) => handleSchemaConfigChange('operation', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Primary Key Column
                        </label>
                        <input
                          type="text"
                          value={schemaConfig.primaryKeyColumn}
                          onChange={(e) => handleSchemaConfigChange('primaryKeyColumn', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
                            borderColor: themeColors.border,
                            color: themeColors.text
                          }}
                          placeholder="ID"
                        />
                      </div>

                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Sequence Name (for INSERT)
                        </label>
                        <input
                          type="text"
                          value={schemaConfig.sequenceName}
                          onChange={(e) => handleSchemaConfigChange('sequenceName', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                          <label className="text-sm font-medium" style={{ color: themeColors.text }}>
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
                            <span className="ml-2 text-sm" style={{ color: themeColors.textSecondary }}>Yes</span>
                          </div>
                        </div>

                        <div className="space-y-2">
                          <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                            Page Size
                          </label>
                          <input
                            type="number"
                            value={schemaConfig.pageSize}
                            onChange={(e) => handleSchemaConfigChange('pageSize', parseInt(e.target.value))}
                            className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                          <label className="text-sm font-medium" style={{ color: themeColors.text }}>
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
                            <span className="ml-2 text-sm" style={{ color: themeColors.textSecondary }}>Yes</span>
                          </div>
                        </div>

                        <div className="space-y-2">
                          <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                            Default Sort Column
                          </label>
                          <input
                            type="text"
                            value={schemaConfig.defaultSortColumn}
                            onChange={(e) => handleSchemaConfigChange('defaultSortColumn', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Default Sort Direction
                        </label>
                        <select
                          value={schemaConfig.defaultSortDirection}
                          onChange={(e) => handleSchemaConfigChange('defaultSortDirection', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
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
                        <span className="text-sm font-normal ml-2" style={{ color: themeColors.textSecondary }}>
                          (Auto-generated from {selectedObject.columns.length} columns)
                        </span>
                      )}
                      {selectedObject?.parameters && (
                        <span className="text-sm font-normal ml-2" style={{ color: themeColors.textSecondary }}>
                          (Auto-generated from {selectedObject.parameters.length} parameters)
                        </span>
                      )}
                    </h3>
                    <button
                      onClick={handleAddParameter}
                      className="px-3 py-1.5 rounded-lg flex items-center gap-2 text-sm transition-colors hover-lift"
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
                                  className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.modalBg,
                                    borderColor: themeColors.border,
                                    color: themeColors.text
                                  }}
                                  placeholder="parameter_key"
                                />
                              </td>
                              <td className="px-3 py-2">
                                <input
                                  type="text"
                                  value={param.dbColumn}
                                  onChange={(e) => handleParameterChange(param.id, 'dbColumn', e.target.value)}
                                  className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.modalBg,
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
                                  className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.modalBg,
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
                                  className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.modalBg,
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
                                  className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.modalBg,
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
                          <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                            Default Value Pattern
                          </label>
                          <input
                            type="text"
                            placeholder="SYSDATE, USER, etc."
                            className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          />
                        </div>
                        <div className="space-y-2">
                          <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                            Validation Regex
                          </label>
                          <input
                            type="text"
                            placeholder="^[A-Za-z0-9_]+$"
                            className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                          />
                        </div>
                        <div className="space-y-2">
                          <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                            Min/Max Values
                          </label>
                          <div className="flex gap-2">
                            <input
                              type="number"
                              placeholder="Min"
                              className="flex-1 px-3 py-2 border rounded-lg text-sm hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
                                borderColor: themeColors.border,
                                color: themeColors.text
                              }}
                            />
                            <input
                              type="number"
                              placeholder="Max"
                              className="flex-1 px-3 py-2 border rounded-lg text-sm hover-lift"
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
                        <span className="text-sm font-normal ml-2" style={{ color: themeColors.textSecondary }}>
                          (Auto-generated from {selectedObject.columns.length} columns)
                        </span>
                      )}
                    </h3>
                    <button
                      onClick={handleAddResponseMapping}
                      className="px-3 py-1.5 rounded-lg flex items-center gap-2 text-sm transition-colors hover-lift"
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
                                  className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.modalBg,
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
                                  className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.modalBg,
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
                                  className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.modalBg,
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
                                  className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.modalBg,
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
                            <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                              Response Format
                            </label>
                            <select
                              value={responseBody.contentType}
                              onChange={(e) => handleResponseBodyChange('contentType', e.target.value)}
                              className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
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
                            <label className="text-sm font-medium" style={{ color: themeColors.text }}>
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
                              <span className="ml-2 text-sm" style={{ color: themeColors.textSecondary }}>
                                Include timestamp, version, request ID
                              </span>
                            </div>
                          </div>
                        </div>
                        <div className="space-y-4">
                          <div className="space-y-2">
                            <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                              Compression
                            </label>
                            <select
                              value={responseBody.compression}
                              onChange={(e) => handleResponseBodyChange('compression', e.target.value)}
                              className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                              style={{ 
                                backgroundColor: themeColors.card,
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
                            <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                              Pretty Print
                            </label>
                            <div className="flex items-center">
                              <input
                                type="checkbox"
                                defaultChecked
                                className="h-4 w-4 rounded"
                                style={{ accentColor: themeColors.info }}
                              />
                              <span className="ml-2 text-sm" style={{ color: themeColors.textSecondary }}>
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
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="space-y-4">
                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Authentication Type
                        </label>
                        <select
                          value={authConfig.authType}
                          onChange={(e) => handleAuthConfigChange('authType', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
                            borderColor: themeColors.border,
                            color: themeColors.text
                          }}
                        >
                          <option value="NONE">None (Public)</option>
                          <option value="ORACLE_ROLES">Oracle Database Roles</option>
                          <option value="API_KEY">API Key</option>
                          <option value="JWT">JWT Token</option>
                          <option value="OAUTH2">OAuth 2.0</option>
                          <option value="BASIC">Basic Auth</option>
                        </select>
                      </div>

                      {authConfig.authType === 'API_KEY' && (
                        <div className="space-y-2">
                          <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                            API Key Header
                          </label>
                          <input
                            type="text"
                            value={authConfig.apiKeyHeader}
                            onChange={(e) => handleAuthConfigChange('apiKeyHeader', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="X-API-Key"
                          />
                        </div>
                      )}

                      {authConfig.authType === 'JWT' && (
                        <div className="space-y-2">
                          <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                            JWT Issuer
                          </label>
                          <input
                            type="text"
                            value={authConfig.jwtIssuer}
                            onChange={(e) => handleAuthConfigChange('jwtIssuer', e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="api.example.com"
                          />
                        </div>
                      )}

                      {authConfig.authType === 'ORACLE_ROLES' && (
                        <div className="space-y-2">
                          <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                            Required Roles
                          </label>
                          <input
                            type="text"
                            value={authConfig.requiredRoles.join(', ')}
                            onChange={(e) => handleAuthConfigChange('requiredRoles', e.target.value.split(',').map(role => role.trim()))}
                            className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="HR_APP_USER, HR_APP_ADMIN"
                          />
                        </div>
                      )}
                    </div>

                    <div className="space-y-4">
                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Custom Auth Function
                        </label>
                        <input
                          type="text"
                          value={authConfig.customAuthFunction}
                          onChange={(e) => handleAuthConfigChange('customAuthFunction', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
                            borderColor: themeColors.border,
                            color: themeColors.text
                          }}
                          placeholder="HR.AUTH_VALIDATE_USER"
                        />
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
                          <span className="ml-2 text-sm" style={{ color: themeColors.text }}>
                            Validate Session
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
                          <span className="ml-2 text-sm" style={{ color: themeColors.text }}>
                            Check Object Privileges
                          </span>
                        </div>
                      </div>

                      {authConfig.authType === 'OAUTH2' && (
                        <div className="space-y-2">
                          <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                            OAuth Scopes
                          </label>
                          <input
                            type="text"
                            value={authConfig.oauthScopes.join(', ')}
                            onChange={(e) => handleAuthConfigChange('oauthScopes', e.target.value.split(',').map(scope => scope.trim()))}
                            className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
                              borderColor: themeColors.border,
                              color: themeColors.text
                            }}
                            placeholder="read, write, admin"
                          />
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Headers Configuration */}
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <h4 className="font-semibold" style={{ color: themeColors.text }}>
                        HTTP Headers ({headers.length})
                      </h4>
                      <button
                        onClick={handleAddHeader}
                        className="px-3 py-1.5 rounded-lg flex items-center gap-2 text-sm transition-colors hover-lift"
                        style={{ backgroundColor: themeColors.info, color: themeColors.white }}
                      >
                        <Plus className="h-4 w-4" />
                        Add Header
                      </button>
                    </div>

                    <div className="overflow-x-auto border rounded-lg" style={{ 
                      borderColor: themeColors.border,
                      backgroundColor: themeColors.card
                    }}>
                      <table className="w-full min-w-[800px]">
                        <thead>
                          <tr style={{ backgroundColor: themeColors.hover }}>
                            <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Header</th>
                            <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Value</th>
                            <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Required</th>
                            <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Description</th>
                            <th className="px-3 py-2 text-left text-xs font-medium border-b" style={{ borderColor: themeColors.border, color: themeColors.textSecondary }}>Actions</th>
                          </tr>
                        </thead>
                        <tbody>
                          {headers.map((header, index) => (
                            <tr key={header.id} style={{ 
                              backgroundColor: index % 2 === 0 ? themeColors.card : themeColors.hover,
                              borderBottom: `1px solid ${themeColors.border}`
                            }}>
                              <td className="px-3 py-2">
                                <input
                                  type="text"
                                  value={header.key}
                                  onChange={(e) => handleHeaderChange(header.id, 'key', e.target.value)}
                                  className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.modalBg,
                                    borderColor: themeColors.border,
                                    color: themeColors.text
                                  }}
                                  placeholder="Header-Name"
                                />
                              </td>
                              <td className="px-3 py-2">
                                <input
                                  type="text"
                                  value={header.value}
                                  onChange={(e) => handleHeaderChange(header.id, 'value', e.target.value)}
                                  className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.modalBg,
                                    borderColor: themeColors.border,
                                    color: themeColors.text
                                  }}
                                  placeholder="header value"
                                />
                              </td>
                              <td className="px-3 py-2 text-center">
                                <input
                                  type="checkbox"
                                  checked={header.required}
                                  onChange={(e) => handleHeaderChange(header.id, 'required', e.target.checked)}
                                  className="h-4 w-4 rounded"
                                  style={{ accentColor: themeColors.info }}
                                />
                              </td>
                              <td className="px-3 py-2">
                                <input
                                  type="text"
                                  value={header.description}
                                  onChange={(e) => handleHeaderChange(header.id, 'description', e.target.value)}
                                  className="w-full px-2 py-1 border rounded text-sm hover-lift"
                                  style={{ 
                                    backgroundColor: themeColors.modalBg,
                                    borderColor: themeColors.border,
                                    color: themeColors.text
                                  }}
                                  placeholder="Header description"
                                />
                              </td>
                              <td className="px-3 py-2">
                                <button
                                  onClick={() => handleRemoveHeader(header.id)}
                                  className="p-1.5 rounded transition-colors hover-lift"
                                  style={{ backgroundColor: themeColors.error + '20', color: themeColors.error }}
                                  title="Delete header"
                                >
                                  <Trash2 className="h-4 w-4" />
                                </button>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Request Schema Type
                        </label>
                        <select
                          value={requestBody.schemaType}
                          onChange={(e) => handleRequestBodyChange('schemaType', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Max Request Size (bytes)
                        </label>
                        <input
                          type="number"
                          value={requestBody.maxSize}
                          onChange={(e) => handleRequestBodyChange('maxSize', parseInt(e.target.value))}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
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
                          <span className="ml-2 text-sm" style={{ color: themeColors.textSecondary }}>
                            Validate request body against schema
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="space-y-4">
                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Allowed Media Types
                        </label>
                        <input
                          type="text"
                          value={requestBody.allowedMediaTypes.join(', ')}
                          onChange={(e) => handleRequestBodyChange('allowedMediaTypes', e.target.value.split(',').map(type => type.trim()))}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
                            borderColor: themeColors.border,
                            color: themeColors.text
                          }}
                          placeholder="application/json, application/xml"
                        />
                      </div>

                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Required Fields
                        </label>
                        <input
                          type="text"
                          value={requestBody.requiredFields.join(', ')}
                          onChange={(e) => handleRequestBodyChange('requiredFields', e.target.value.split(',').map(field => field.trim()))}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                        <span className="text-sm font-medium" style={{ color: themeColors.text }}>
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
                        className="w-full h-48 px-4 py-3 text-sm font-mono resize-none focus:outline-none"
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Success Schema
                        </label>
                        <div className="border rounded-lg" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <textarea
                            value={responseBody.successSchema}
                            onChange={(e) => handleResponseBodyChange('successSchema', e.target.value)}
                            className="w-full h-40 px-4 py-3 text-sm font-mono resize-none focus:outline-none"
                            style={{ 
                              backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                              color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                            }}
                          />
                        </div>
                      </div>

                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Include Metadata Fields
                        </label>
                        <input
                          type="text"
                          value={responseBody.metadataFields.join(', ')}
                          onChange={(e) => handleResponseBodyChange('metadataFields', e.target.value.split(',').map(field => field.trim()))}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Error Schema
                        </label>
                        <div className="border rounded-lg" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <textarea
                            value={responseBody.errorSchema}
                            onChange={(e) => handleResponseBodyChange('errorSchema', e.target.value)}
                            className="w-full h-40 px-4 py-3 text-sm font-mono resize-none focus:outline-none"
                            style={{ 
                              backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                              color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                            }}
                          />
                        </div>
                      </div>

                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Compression
                        </label>
                        <select
                          value={responseBody.compression}
                          onChange={(e) => handleResponseBodyChange('compression', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Test Environment
                        </label>
                        <select
                          value={tests.testEnvironment}
                          onChange={(e) => handleTestsChange('testEnvironment', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Performance Threshold (ms)
                        </label>
                        <input
                          type="number"
                          value={tests.performanceThreshold}
                          onChange={(e) => handleTestsChange('performanceThreshold', parseInt(e.target.value))}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                          <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                            Test Users
                          </label>
                          <input
                            type="number"
                            value={tests.testUsers}
                            onChange={(e) => handleTestsChange('testUsers', parseInt(e.target.value))}
                            className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                          <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                            Test Iterations
                          </label>
                          <input
                            type="number"
                            value={tests.testIterations}
                            onChange={(e) => handleTestsChange('testIterations', parseInt(e.target.value))}
                            className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Unit Tests
                        </label>
                        <div className="border rounded-lg" style={{ 
                          borderColor: themeColors.border,
                          backgroundColor: themeColors.card
                        }}>
                          <textarea
                            value={tests.unitTests}
                            onChange={(e) => handleTestsChange('unitTests', e.target.value)}
                            className="w-full h-40 px-4 py-3 text-sm font-mono resize-none focus:outline-none"
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
                        className="w-full h-48 px-4 py-3 text-sm font-mono resize-none focus:outline-none"
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Timeout (ms)
                        </label>
                        <input
                          type="number"
                          value={settings.timeout}
                          onChange={(e) => handleSettingsChange('timeout', parseInt(e.target.value))}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Max Records
                        </label>
                        <input
                          type="number"
                          value={settings.maxRecords}
                          onChange={(e) => handleSettingsChange('maxRecords', parseInt(e.target.value))}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Log Level
                        </label>
                        <select
                          value={settings.logLevel}
                          onChange={(e) => handleSettingsChange('logLevel', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
                          style={{ 
                            backgroundColor: themeColors.card,
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
                          <span className="ml-2 text-sm" style={{ color: themeColors.text }}>
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
                          <span className="ml-2 text-sm" style={{ color: themeColors.text }}>
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
                          <span className="ml-2 text-sm" style={{ color: themeColors.text }}>
                            Enable Monitoring
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="space-y-4">
                      <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Rate Limit (requests)
                        </label>
                        <div className="flex items-center gap-4">
                          <input
                            type="number"
                            value={settings.rateLimit}
                            onChange={(e) => handleSettingsChange('rateLimit', parseInt(e.target.value))}
                            className="flex-1 px-3 py-2 border rounded-lg text-sm hover-lift"
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
                            className="flex-1 px-3 py-2 border rounded-lg text-sm hover-lift"
                            style={{ 
                              backgroundColor: themeColors.card,
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Cache TTL (seconds)
                        </label>
                        <input
                          type="number"
                          value={settings.cacheTtl}
                          onChange={(e) => handleSettingsChange('cacheTtl', parseInt(e.target.value))}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          CORS Origins
                        </label>
                        <input
                          type="text"
                          value={settings.corsOrigins.join(', ')}
                          onChange={(e) => handleSettingsChange('corsOrigins', e.target.value.split(',').map(origin => origin.trim()))}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                          <span className="ml-2 text-sm" style={{ color: themeColors.text }}>
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
                          <span className="ml-2 text-sm" style={{ color: themeColors.text }}>
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
                          <span className="ml-2 text-sm" style={{ color: themeColors.text }}>
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
                          <div className="font-medium text-sm" style={{ color: themeColors.text }}>OpenAPI Spec</div>
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
                          <div className="font-medium text-sm" style={{ color: themeColors.text }}>Postman Collection</div>
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
                          <div className="font-medium text-sm" style={{ color: themeColors.text }}>Client SDK</div>
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
                        <label className="text-sm font-medium" style={{ color: themeColors.text }}>
                          Alert Email
                        </label>
                        <input
                          type="email"
                          value={settings.alertEmail}
                          onChange={(e) => handleSettingsChange('alertEmail', e.target.value)}
                          className="w-full px-3 py-2 border rounded-lg text-sm hover-lift"
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
                          <span className="ml-2 text-sm" style={{ color: themeColors.text }}>
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
                          <span className="ml-2 text-sm" style={{ color: themeColors.text }}>
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
                      <span className="text-sm font-normal ml-2" style={{ color: themeColors.textSecondary }}>
                        (Based on {selectedObject?.name || 'selected object'})
                      </span>
                    </h3>
                    <div className="flex items-center gap-2">
                      <select
                        value={previewMode}
                        onChange={(e) => setPreviewMode(e.target.value)}
                        className="px-3 py-1.5 border rounded-lg text-sm hover-lift"
                        style={{ 
                          backgroundColor: themeColors.card,
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
                      <span className="text-sm font-medium" style={{ color: themeColors.text }}>
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
                    <pre className="w-full h-[400px] px-4 py-3 overflow-auto text-sm" style={{ 
                      backgroundColor: theme === 'dark' ? '#1a202c' : '#f8fafc',
                      color: theme === 'dark' ? '#e2e8f0' : '#1e293b'
                    }}>
                      {generatedCode}
                    </pre>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Footer */}
          <div className="px-6 py-4 border-t flex flex-col sm:flex-row items-center justify-between gap-4" style={{ 
            borderColor: themeColors.border,
            backgroundColor: themeColors.card
          }}>
            <div className="text-sm" style={{ color: themeColors.textSecondary }}>
              Endpoint: <span className="font-mono font-medium" style={{ color: themeColors.text }}>
                {apiDetails.httpMethod} {apiDetails.basePath}{apiDetails.endpointPath}
              </span>
              <br />
              <span className="text-xs" style={{ color: themeColors.textSecondary }}>
                Source: {schemaConfig.schemaName}.{schemaConfig.objectName} ({schemaConfig.objectType})
              </span>
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
                onClick={handleSave}
                className="px-4 py-2 rounded-lg flex items-center gap-2 transition-colors hover-lift"
                style={{ backgroundColor: themeColors.success, color: themeColors.white }}
              >
                <Save className="h-4 w-4" />
                Generate & Save API
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