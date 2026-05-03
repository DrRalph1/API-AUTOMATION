import React, { useEffect, useRef, useState } from "react";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate, useLocation, useNavigate } from "react-router-dom";
import { RecoilRoot } from "recoil";

import Login from "./src/views/auth/Login.js";

import { ThemeProvider } from "@/context/ThemeContext.js";
import { AuthProvider, useAuth } from "@/context/AuthContext.js";
import { LogProvider, useLog } from "@/context/LogContext.js";

import EntryPage from "@/views/index.js";
import SweetAlertWrapper from "@/components/wrapper/SweetAlertWrapper";
import { setNavigateFunction } from "./src/helpers/APIHelper.js";

const queryClient = new QueryClient();

// ============================================
// Navigation Initializer - Sets navigate for API config
// ============================================
function NavigationInitializer() {
  const navigate = useNavigate();
  
  useEffect(() => {
    setNavigateFunction(navigate);
    console.log('✅ Navigation function set for API config');
  }, [navigate]);
  
  return null;
}

// ============================================
// Session Monitor Component
// ============================================
function SessionMonitor() {
  const { isAuthenticated, logout, getPersistenceInfo } = useAuth();
  const navigate = useNavigate();
  const isLoggingOut = useRef(false);

  useEffect(() => {
    if (!isAuthenticated) return;

    const persistenceInfo = getPersistenceInfo();
    
    // Only check expiry if enabled in config
    if (!persistenceInfo.enabled || !persistenceInfo.tokenExpiryEnabled) {
      console.log('Session monitor: Token expiry check disabled, skipping');
      return;
    }

    const checkSession = () => {
      if (isLoggingOut.current) return;
      
      const tokenExpiry = localStorage.getItem('auth_token_expiry') || sessionStorage.getItem('auth_token_expiry');
      if (tokenExpiry) {
        const isExpired = new Date().getTime() > parseInt(tokenExpiry);
        if (isExpired) {
          console.log('Session expired, logging out...');
          isLoggingOut.current = true;
          
          // Small delay to allow any in-flight requests to complete
          setTimeout(() => {
            logout();
            navigate('/login', { replace: true });
          }, 500);
        }
      }
    };

    // Check immediately
    checkSession();
    
    // Check every 30 seconds
    const interval = setInterval(checkSession, 30000);

    return () => {
      clearInterval(interval);
      isLoggingOut.current = false;
    };
  }, [isAuthenticated, logout, getPersistenceInfo, navigate]);

  return null;
}

// ============================================
// Protected Component
// ============================================
function Protected({ children, roles = [] }) {
  const { user, isInitialized, isAuthenticated, token } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [isRedirecting, setIsRedirecting] = useState(false);

  useEffect(() => {
    // Don't redirect if we're already redirecting
    if (isRedirecting) return;
    
    // Wait for initialization
    if (!isInitialized) return;
    
    // Check authentication
    if (!isAuthenticated || !token) {
      console.log('🔒 Protected - Not authenticated or no token, redirecting to login');
      setIsRedirecting(true);
      navigate('/login', { replace: true });
    }
  }, [isInitialized, isAuthenticated, token, navigate, isRedirecting]);

  // Show loading spinner while initializing
  if (!isInitialized) {
    return (
      <div className="flex h-screen w-full items-center justify-center bg-background">
        <div className="flex flex-col items-center gap-4">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          <p className="text-lg font-medium">Loading...</p>
        </div>
      </div>
    );
  }

  // Check if user exists and is authenticated
  if (!user || !isAuthenticated || !token) {
    return null; // Don't render anything while redirecting
  }

  // Check role requirements
  if (roles.length > 0 && !roles.includes(user.role)) {
    console.log('🔒 Protected - Role not authorized, redirecting to home');
    return <Navigate to="/" replace />;
  }

  return children;
}

// ============================================
// PublicRoute Component
// ============================================
function PublicRoute({ children }) {
  const { user, isInitialized, isAuthenticated, token } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isInitialized) return;
    
    if (user && isAuthenticated && token) {
      if (user.role === "system administrator") {
        navigate('/admin', { replace: true });
      } else if (user.role === "user") {
        navigate('/user-dashboard', { replace: true });
      } else {
        navigate('/', { replace: true });
      }
    }
  }, [user, isInitialized, isAuthenticated, token, navigate]);

  if (!isInitialized) {
    return (
      <div className="flex h-screen w-full items-center justify-center bg-background">
        <div className="flex flex-col items-center gap-4">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          <p className="text-lg font-medium">Loading...</p>
        </div>
      </div>
    );
  }

  if (user && isAuthenticated && token) {
    return null; // Don't render while redirecting
  }

  return children;
}

// ============================================
// Route Logger
// ============================================
function RouteLogger() {
  const { log } = useLog();
  const location = useLocation();
  const { user } = useAuth();

  useEffect(() => {
    try {
      log({
        level: "info",
        category: "nav",
        action: "navigate",
        message: `Navigated to ${location.pathname}`,
        route: location.pathname,
        user: user?.id,
      });
    } catch {}
  }, [location.pathname, log, user]);

  return null;
}

// ============================================
// Catch-all Redirect
// ============================================
function RouteRedirect() {
  const { user, isInitialized, isAuthenticated, token } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isInitialized) return;
    
    if (user && isAuthenticated && token) {
      if (user.role === "system administrator") {
        navigate('/admin', { replace: true });
      } else if (user.role === "user") {
        navigate('/user-dashboard', { replace: true });
      } else {
        navigate('/', { replace: true });
      }
    } else {
      navigate('/login', { replace: true });
    }
  }, [user, isInitialized, isAuthenticated, token, navigate]);

  if (!isInitialized) {
    return (
      <div className="flex h-screen w-full items-center justify-center bg-background">
        <div className="flex flex-col items-center gap-4">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          <p className="text-lg font-medium">Loading...</p>
        </div>
      </div>
    );
  }

  return null; // Don't render anything while redirecting
}

// ============================================
// Auth Check Wrapper - Ensures token is available
// ============================================
function AuthCheckWrapper({ children }) {
  const { token, isAuthenticated, user } = useAuth();
  
  // Debug log for auth state
  useEffect(() => {
    console.log('🔐 AuthCheckWrapper - Auth State:', {
      hasToken: !!token,
      isAuthenticated,
      hasUser: !!user,
      tokenPreview: token ? `${token.substring(0, 20)}...` : 'none'
    });
  }, [token, isAuthenticated, user]);
  
  return children;
}

// ============================================
// Routes Tree
// ============================================
function RoutesTree() {
  return (
    <AuthCheckWrapper>
      <Routes>
        <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
        <Route path="/admin" element={
          <Protected roles={["system administrator"]}>
            <EntryPage userRole="admin" />
          </Protected>
        } />
        <Route path="/user-dashboard" element={
          <Protected roles={["user"]}>
            <EntryPage userRole="user" />
          </Protected>
        } />
        <Route path="/" element={
          <Protected>
            <EntryPage userRole={null} />
          </Protected>
        } />
        <Route path="*" element={<RouteRedirect />} />
      </Routes>
    </AuthCheckWrapper>
  );
}

// ============================================
// Main App
// ============================================
export default function App() {
  return (
    <RecoilRoot>
      <QueryClientProvider client={queryClient}>
        <LogProvider>
          <ThemeProvider>
            <AuthProvider>
              <SweetAlertWrapper>
                <TooltipProvider>
                  <Toaster />
                  <BrowserRouter
                    future={{
                      v7_startTransition: true,
                      v7_relativeSplatPath: true,
                    }}
                  >
                    <NavigationInitializer />
                    <SessionMonitor />
                    <RouteLogger />
                    <div className="min-h-screen">
                      <RoutesTree />
                    </div>
                  </BrowserRouter>
                </TooltipProvider>
              </SweetAlertWrapper>
            </AuthProvider>
          </ThemeProvider>
        </LogProvider>
      </QueryClientProvider>
    </RecoilRoot>
  );
}