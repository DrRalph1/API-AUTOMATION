// components/Sidebar.js
import React, { useState } from 'react';
import { useAuth } from '@/context/AuthContext';

import { motion } from 'framer-motion';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/ui/tooltip';
import {
  Home,
  Layers,
  FileText,
  Users,
  LogOut,
  Plus,
  Workflow,
  Code,
  Key,
  Link,
  AlertCircle,
  Terminal,
  TestTube,
  Receipt,
  Menu,
  X,
  ReceiptIndianRupee,
  Tag, 
  SearchCheck,
} from 'lucide-react';

// Import modals
import IntegrationFormModal from '../modals/IntegrationFormModal.js';
import IntegrationDetailsModal from '../modals/IntegrationDetailsModal.js';

// Import controllers
import {
  createIntegration,
  updateIntegration,
  deleteIntegration
} from '../../controllers/IntegrationController.js';

const Sidebar = ({
  sidebarCollapsed = false,
  activeView = "dashboard",
  colors = {},
  setActiveView,
  setConfigWorkflow,
  startNewServiceConfig,
  // New props for integration handling
  authorizationHeader,
  handleServiceCreated,
  handleServiceUpdated,
  handleServiceDeleted,
  formatDate,
}) => {

  // ✅ MOBILE STATE (does NOT affect desktop)
  const [mobileOpen, setMobileOpen] = useState(false);

  const { logout } = useAuth();
  
  // Modal states
  const [showIntegrationForm, setShowIntegrationForm] = useState(false);
  const [showIntegrationDetails, setShowIntegrationDetails] = useState(false);
  const [selectedIntegration, setSelectedIntegration] = useState(null);
  const [isEditingIntegration, setIsEditingIntegration] = useState(false);
  const [isSubmittingIntegration, setIsSubmittingIntegration] = useState(false);

  const defaultColors = {
    cardBg: 'bg-white dark:bg-gray-900',
    cardBorder: 'border-gray-200 dark:border-gray-800',
    ...colors
  };

  const adminTabs = [
    { id: "dashboard", label: "Dashboard", icon: <Home className="h-4 w-4" /> },
    { id: "service-categories", label: "Service Categories", icon: <Tag className="h-4 w-4" /> },
    { id: "service-integrations", label: "Service Integrations", icon: <Layers className="h-4 w-4" /> },
    { id: "service-operations", label: "Service Operations", icon: <Link className="h-4 w-4" /> },
    { id: "form-fields", label: "Form Fields", icon: <Key className="h-4 w-4" /> },
    { id: "tparty-api-config", label: "TParty API Config", icon: <Code className="h-4 w-4" /> },
    { id: "system-workflow", label: "System Workflow", icon: <Workflow className="h-4 w-4" /> },
    { id: "receipt-design", label: "Receipt Templates", icon: <ReceiptIndianRupee className="h-4 w-4" /> },
  ];

  const managementTabs = [
    { id: "audit", label: "Audit Logs", icon: <FileText className="h-4 w-4" /> },
    { id: "system-logs", label: "System Logs", icon: <SearchCheck className="h-4 w-4" /> },
    { id: "users-config", label: "Users Config", icon: <Users className="h-4 w-4" /> },
  ];

  const handleNavClick = (id) => {
    setActiveView?.(id);
    setMobileOpen(false); // ✅ close on mobile
  };

  // Integration Modal Handlers
  const handleOpenIntegrationForm = (integration = null, editing = false) => {
    setSelectedIntegration(integration);
    setIsEditingIntegration(editing);
    setShowIntegrationForm(true);
    setMobileOpen(false); // Close sidebar on mobile when opening modal
  };

  const handleCloseIntegrationForm = () => {
    setShowIntegrationForm(false);
    setSelectedIntegration(null);
    setIsEditingIntegration(false);
  };

  // Integration Form Submission Handler
  const handleIntegrationFormSubmit = async (integrationData, isEditing = false) => {
    if (!authorizationHeader) {
      console.error('Authorization header is required');
      return false;
    }

    setIsSubmittingIntegration(true);
    try {
      let response;
      
      if (isEditing && selectedIntegration?.integrationId) {
        // Update existing integration
        response = await updateIntegration(
          authorizationHeader, 
          selectedIntegration.integrationId, 
          integrationData
        );
        
        if (handleServiceUpdated) {
          await handleServiceUpdated(selectedIntegration.integrationId, integrationData);
        }
      } else {
        // Create new integration
        response = await createIntegration(authorizationHeader, integrationData);
        
        if (handleServiceCreated) {
          await handleServiceCreated(response.data);
        }
      }
      
      return true;
    } catch (error) {
      console.error('Failed to save integration:', error);
      return false;
    } finally {
      setIsSubmittingIntegration(false);
    }
  };


  const handleCopyIntegration = async (integration) => {
    if (!integration) return;
    
    try {
      // Create a copy of the integration with a new name
      const copyData = {
        ...integration,
        integrationName: `${integration.integrationName} (Copy)`,
        integrationCode: `${integration.integrationCode}_COPY`,
      };
      
      const response = await createIntegration(authorizationHeader, copyData);
      
      if (handleServiceCreated) {
        await handleServiceCreated(response.data);
      }
    } catch (error) {
      console.error('Failed to copy integration:', error);
    }
  };

  const handleDeleteIntegration = async (integrationId) => {
    if (!authorizationHeader || !integrationId) return;
    
    try {
      await deleteIntegration(authorizationHeader, integrationId);
      
      if (handleServiceDeleted) {
        await handleServiceDeleted(integrationId);
      }
      
      setShowIntegrationDetails(false);
    } catch (error) {
      console.error('Failed to delete integration:', error);
    }
  };

  return (
    <>
      {/* ✅ MOBILE TOP BAR */}
      <div className="md:hidden fixed top-0 left-0 right-0 z-50 flex items-center justify-between p-4 border-b bg-white dark:bg-gray-900">
        <div className="flex items-center gap-2 font-semibold text-sm">
          <Terminal className="h-5 w-5 text-purple-500" />
          API Automation
        </div>
        <Button variant="ghost" size="icon" onClick={() => setMobileOpen(true)}>
          <Menu className="h-5 w-5" />
        </Button>
      </div>

      {/* ✅ MOBILE OVERLAY */}
      {mobileOpen && (
        <div
          className="fixed inset-0 bg-black/40 z-40 md:hidden"
          onClick={() => setMobileOpen(false)}
        />
      )}

      {/* ✅ SIDEBAR */}
      <motion.aside
        initial={{ width: sidebarCollapsed ? 72 : 280 }}
        animate={{ width: sidebarCollapsed ? 72 : 280 }}
        className={`
          fixed md:sticky top-0 md:top-16 z-50
          h-full md:h-[calc(100vh-4rem)]
          bg-white/80 dark:bg-gray-900/60 backdrop-blur-2xl  /* fully opaque on mobile */
          md:${defaultColors.cardBg} /* keep desktop as before */
          border-r ${defaultColors.cardBorder}
          transition-all duration-300
          ${mobileOpen ? "translate-x-0" : "-translate-x-full"}
          md:translate-x-0
          shadow-md md:shadow-none  /* optional: add shadow on mobile for depth */
        `}
        style={{ width: sidebarCollapsed ? 72 : 280 }}
      >

        {/* ✅ MOBILE CLOSE BUTTON */}
        <div className="md:hidden fixed top-0 left-0 right-0 z-50 flex items-center justify-between p-4 border-b bg-white dark:bg-gray-900">
          <div className="flex items-center gap-2 font-semibold text-sm">
            <Terminal className="h-5 w-5 text-purple-500" />
            API Automation
          </div>
          <div className="md:hidden flex justify-end p-3">
            <Button variant="ghost" size="icon" onClick={() => setMobileOpen(false)}>
              <X className="h-5 w-5" />
            </Button>
        </div>
        </div>

        <div className="h-full flex flex-col pt-16 md:pt-0">

          <div className="flex-1 overflow-y-auto overflow-x-hidden">

            {/* CONFIGURATION */}
            <div className="p-4 space-y-1">
              <h3 className="text-xs font-semibold text-gray-500 uppercase tracking-wider px-2 mb-2">
                Configuration
              </h3>

              {adminTabs.map((tab) => (
                <Button
                  key={tab.id}
                  variant={activeView === tab.id ? "secondary" : "ghost"}
                  className={`w-full justify-start h-12 rounded-xl mb-1 ${
                    activeView === tab.id ? 'bg-gradient-to-r from-purple-500/20 to-pink-500/20' : ''
                  }`}
                  onClick={() => handleNavClick(tab.id)}
                >
                  {tab.icon}
                  <span className="ml-3">{tab.label}</span>
                </Button>
              ))}
            </div>

            <Separator className="mx-4" />

            {/* MANAGEMENT */}
            <div className="p-4 space-y-1">
              <h3 className="text-xs font-semibold text-gray-500 uppercase tracking-wider px-2 mb-2">
                Management
              </h3>

              {managementTabs.map((tab) => (
                <Button
                  key={tab.id}
                  variant={activeView === tab.id ? "secondary" : "ghost"}
                  className="w-full justify-start h-12 rounded-xl mb-1"
                  onClick={() => handleNavClick(tab.id)}
                >
                  {tab.icon}
                  <span className="ml-3">{tab.label}</span>
                </Button>
              ))}
            </div>

            <Separator className="mx-4" />

            {/* QUICK ACTIONS */}
            <div className="p-4 space-y-2">
              <Button
                variant="outline"
                className="w-full hidden md:flex justify-start text-sm h-10"
                onClick={() => handleOpenIntegrationForm(null, false)} // Updated to use modal
              >
                <Plus className="h-4 w-4 mr-2" />
                New Integration
              </Button>

              {/* <Button
                variant="outline"
                className="w-full hidden md:flex justify-start text-sm h-10"
                onClick={() => {
                  setActiveView?.("system-logs");
                  setMobileOpen(false);
                }}
              >
                <AlertCircle className="h-4 w-4 mr-2" />
                System Logs
              </Button> */}

              <Button
                variant="outline"
                className="w-full justify-start text-sm h-10 hover:bg-red-50 dark:hover:bg-red-900/20"
                onClick={() => {
                  logout();
                  setMobileOpen(false);
                }}
              >
                <LogOut className="h-4 w-4 mr-2" />
                End Session
              </Button>

            </div>
          </div>

          {/* ✅ FOOTER */}
          <div className="p-4 border-t">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <div className="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
                <span className="text-xs text-gray-500">System Online</span>
              </div>
              <span className="text-xs text-gray-400">v2.1.0</span>
            </div>
          </div>

        </div>
      </motion.aside>

      {/* Integration Form Modal */}
      <IntegrationFormModal
        showIntegrationForm={showIntegrationForm}
        setShowIntegrationForm={setShowIntegrationForm}
        selectedIntegration={selectedIntegration}
        handleFormSubmit={handleIntegrationFormSubmit}
        isEditing={isEditingIntegration}
      />

      {/* Integration Details Modal */}
      <IntegrationDetailsModal
        showIntegrationDetails={showIntegrationDetails}
        setShowIntegrationDetails={setShowIntegrationDetails}
        selectedIntegration={selectedIntegration}
        formatDate={formatDate}
        handleCopyIntegration={handleCopyIntegration}
        openEditIntegration={(integration) => handleOpenIntegrationForm(integration, true)}
        handleDeleteIntegration={handleDeleteIntegration}
      />
    </>
  );
};

export default Sidebar;