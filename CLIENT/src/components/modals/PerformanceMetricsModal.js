// components/modals/PerformanceMetricsModal.js
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
  Activity, BarChart3, LineChart, TrendingUp, TrendingDown,
  Clock, Database, Server, Cpu, Zap, Globe,
  Copy, Download, Eye, RefreshCw, ExternalLink,
  ArrowDown, Users, HardDrive, Network, Shield
} from "lucide-react";

export default function PerformanceMetricsModal({
  showPerformanceMetrics,
  setShowPerformanceMetrics,
  selectedMetric,
  handleExportMetrics,
  handleCopyMetrics
}) {
  const [expandedSections, setExpandedSections] = useState({
    overview: true,
    details: false,
    trends: false
  });

  if (!selectedMetric) return null;

  const toggleSection = (section) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  const getMetricIcon = (type) => {
    const icons = {
      latency: <Activity className="h-5 w-5" />,
      throughput: <BarChart3 className="h-5 w-5" />,
      cpu: <Cpu className="h-5 w-5" />,
      memory: <Server className="h-5 w-5" />,
      network: <Network className="h-5 w-5" />,
      database: <Database className="h-5 w-5" />
    };
    return icons[type] || <Activity className="h-5 w-5" />;
  };

  const getTrendBadge = (trend) => {
    if (trend === 'up') {
      return (
        <Badge className="bg-green-500/10 text-green-700 dark:text-green-300 border border-green-200 dark:border-green-800/50 text-xs">
          <TrendingUp className="h-3 w-3 mr-1" /> Improving
        </Badge>
      );
    } else if (trend === 'down') {
      return (
        <Badge className="bg-red-500/10 text-red-700 dark:text-red-300 border border-red-200 dark:border-red-800/50 text-xs">
          <TrendingDown className="h-3 w-3 mr-1" /> Declining
        </Badge>
      );
    }
    return (
      <Badge className="bg-gray-500/10 text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-gray-700 text-xs">
        Stable
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
    <Dialog open={showPerformanceMetrics} onOpenChange={setShowPerformanceMetrics}>
      <DialogContent className="max-w-2xl max-h-[85vh] overflow-y-auto bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl shadow-2xl">
        <DialogHeader className="space-y-3">
          <div className="flex items-center justify-between">
            <DialogTitle className="text-xl font-bold text-gray-900 dark:text-gray-100">
              Performance Metrics
            </DialogTitle>
            <Badge variant="outline" className="text-xs font-mono">
              ID: {selectedMetric.id?.substring(0, 6) || 'N/A'}
            </Badge>
          </div>
          <DialogDescription className="text-sm text-gray-500 dark:text-gray-400">
            Detailed performance metrics and analytics
          </DialogDescription>
        </DialogHeader>
        
        <Separator className="bg-gray-200 dark:bg-gray-700" />
        
        <div className="space-y-6 py-2">
          {/* Header Section */}
          <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 p-4 rounded-lg bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-800/50 dark:to-gray-900/50 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center gap-4">
              <div className="p-3 rounded-xl bg-purple-50 dark:bg-purple-900/20 text-purple-600 dark:text-purple-400">
                {getMetricIcon(selectedMetric.type)}
              </div>
              <div className="space-y-1">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {selectedMetric.name}
                </h3>
                <div className="flex flex-wrap items-center gap-2">
                  {getTrendBadge(selectedMetric.trend)}
                  <Badge variant="outline" className="text-xs capitalize">
                    {selectedMetric.type}
                  </Badge>
                  <Badge className="bg-blue-500/10 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800/50 text-xs">
                    <Clock className="h-3 w-3 mr-1" />
                    {selectedMetric.period || '24h'}
                  </Badge>
                </div>
              </div>
            </div>
          </div>
          
          {/* Overview */}
          {renderMobileSection(
            'overview',
            'Metrics Overview',
            <Activity className="h-3 w-3" />,
            <>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div className="flex items-center justify-between mb-2">
                    <p className="text-xs text-gray-500 dark:text-gray-400">Current Value</p>
                    <Zap className="h-4 w-4 text-gray-400" />
                  </div>
                  <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                    {selectedMetric.value}
                  </p>
                </div>
                
                <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div className="flex items-center justify-between mb-2">
                    <p className="text-xs text-gray-500 dark:text-gray-400">Average</p>
                    <LineChart className="h-4 w-4 text-gray-400" />
                  </div>
                  <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                    {selectedMetric.average || selectedMetric.value}
                  </p>
                </div>
                
                <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div className="flex items-center justify-between mb-2">
                    <p className="text-xs text-gray-500 dark:text-gray-400">Peak</p>
                    <TrendingUp className="h-4 w-4 text-gray-400" />
                  </div>
                  <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                    {selectedMetric.peak || selectedMetric.value}
                  </p>
                </div>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Description</p>
                <p className="text-sm text-gray-700 dark:text-gray-300">
                  {selectedMetric.description || 'Performance metrics showing system health and operational efficiency.'}
                </p>
              </div>
            </>
          )}
          
          {/* Details */}
          {renderMobileSection(
            'details',
            'Detailed Metrics',
            <BarChart3 className="h-3 w-3" />,
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div className="p-3 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div className="flex items-center justify-between mb-1">
                    <Users className="h-4 w-4 text-gray-400" />
                    <p className="text-xs text-gray-500 dark:text-gray-400">Connections</p>
                  </div>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {selectedMetric.connections || 45}
                  </p>
                </div>
                
                <div className="p-3 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div className="flex items-center justify-between mb-1">
                    <HardDrive className="h-4 w-4 text-gray-400" />
                    <p className="text-xs text-gray-500 dark:text-gray-400">Memory Usage</p>
                  </div>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {selectedMetric.memory || '78%'}
                  </p>
                </div>
                
                <div className="p-3 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div className="flex items-center justify-between mb-1">
                    <Cpu className="h-4 w-4 text-gray-400" />
                    <p className="text-xs text-gray-500 dark:text-gray-400">CPU Load</p>
                  </div>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {selectedMetric.cpu || '42%'}
                  </p>
                </div>
                
                <div className="p-3 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                  <div className="flex items-center justify-between mb-1">
                    <Network className="h-4 w-4 text-gray-400" />
                    <p className="text-xs text-gray-500 dark:text-gray-400">Network I/O</p>
                  </div>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {selectedMetric.network || '1.2 MB/s'}
                  </p>
                </div>
              </div>
              
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">Status Summary</p>
                  <Shield className="h-4 w-4 text-gray-400" />
                </div>
                <div className="flex items-center gap-2">
                  <div className="h-2 flex-1 rounded-full bg-gray-200 dark:bg-gray-700">
                    <div 
                      className="h-full rounded-full bg-green-500" 
                      style={{ width: selectedMetric.health || '85%' }}
                    />
                  </div>
                  <span className="text-xs font-medium text-gray-900 dark:text-gray-100">
                    {selectedMetric.health || '85%'} Healthy
                  </span>
                </div>
              </div>
            </div>
          )}
          
          {/* Trends */}
          {renderMobileSection(
            'trends',
            'Trend Analysis',
            <TrendingUp className="h-3 w-3" />,
            <div className="space-y-3">
              <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700">
                <p className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">Performance Trends</p>
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs text-gray-500 dark:text-gray-400">Last Hour</span>
                    <span className="text-xs font-medium text-green-600 dark:text-green-400">+2.4%</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-xs text-gray-500 dark:text-gray-400">Last 24 Hours</span>
                    <span className="text-xs font-medium text-green-600 dark:text-green-400">+8.7%</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-xs text-gray-500 dark:text-gray-400">Last 7 Days</span>
                    <span className="text-xs font-medium text-amber-600 dark:text-amber-400">-1.2%</span>
                  </div>
                </div>
              </div>
              
              <Button
                variant="outline"
                onClick={() => alert('Viewing detailed trends...')}
                className="w-full"
              >
                <LineChart className="h-4 w-4 mr-2" />
                View Detailed Trends
              </Button>
            </div>
          )}
        </div>
        
        <DialogFooter className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-4 border-t border-gray-200 dark:border-gray-800">
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={handleCopyMetrics}
              className="rounded-lg border-gray-300 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              <Copy className="h-4 w-4 mr-2" />
              Copy Metrics
            </Button>
          </div>
          
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
            <div className="flex flex-row items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={handleExportMetrics}
                className="rounded-lg border-blue-300 dark:border-blue-700 hover:bg-blue-50 dark:hover:bg-blue-900/20"
              >
                <Download className="h-4 w-4 mr-2 text-blue-600 dark:text-blue-400" />
                Export Data
              </Button>
              
              <Button
                variant="outline"
                size="sm"
                onClick={() => alert('Refreshing metrics...')}
                className="rounded-lg border-emerald-300 dark:border-emerald-700 hover:bg-emerald-50 dark:hover:bg-emerald-900/20"
              >
                <RefreshCw className="h-4 w-4 mr-2 text-emerald-600 dark:text-emerald-400" />
                Refresh
              </Button>
            </div>
            
            <Separator orientation="vertical" className="h-auto sm:h-6 hidden sm:block" />
            
            <Button
              variant="default"
              onClick={() => setShowPerformanceMetrics(false)}
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