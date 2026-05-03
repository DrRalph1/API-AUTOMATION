import React, { useEffect, useRef, useState, useCallback } from "react";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate, useLocation, useNavigate } from "react-router-dom";
import { RecoilRoot } from "recoil";
import { AlertTriangle, X, Shield, LogOut } from "lucide-react";

import Login from "./src/views/auth/Login.js";

import { ThemeProvider, useTheme } from "@/context/ThemeContext.js";
import { AuthProvider, useAuth } from "@/context/AuthContext.js";
import { LogProvider, useLog } from "@/context/LogContext.js";

import EntryPage from "@/views/index.js";
import SweetAlertWrapper from "@/components/wrapper/SweetAlertWrapper";
import { setNavigateFunction } from "./src/helpers/APIHelper.js";

const queryClient = new QueryClient();

// ============================================
// Timeout Modal Component - Integrated here
// ============================================
function TimeoutWarningModal({ show, countdown, onExtend, onLogout }) {
  const { theme } = useTheme();
  const isDark = theme === 'dark';
  const isProcessingRef = useRef(false);
  
  if (!show) return null;
  
  // Calculate percentage based on countdown (max 30 seconds)
  const maxTime = 30;
  const percentage = (countdown / maxTime) * 100;
  const circumference = 2 * Math.PI * 45;
  const strokeDashoffset = circumference - (percentage / 100) * circumference;
  
  const handleExtend = () => {
    if (isProcessingRef.current) return;
    isProcessingRef.current = true;
    console.log('User clicked Stay Logged In');
    onExtend();
    setTimeout(() => {
      isProcessingRef.current = false;
    }, 1000);
  };
  
  const handleLogout = () => {
    if (isProcessingRef.current) return;
    isProcessingRef.current = true;
    console.log('User clicked Logout Now');
    onLogout();
    setTimeout(() => {
      isProcessingRef.current = false;
    }, 1000);
  };
  
  return (
    <>
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-[100]" onClick={(e) => e.stopPropagation()} />
      <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <div 
          className={`w-full max-w-md rounded-2xl shadow-2xl transform transition-all duration-300 ${
            isDark ? 'bg-gray-900/95 backdrop-blur-lg border-gray-800' : 'bg-white/95 backdrop-blur-lg border-gray-200'
          } border ${isDark ? 'text-white' : 'text-gray-900'} animate-in fade-in zoom-in duration-300`}
          onClick={(e) => e.stopPropagation()}
        >
          <div className="relative p-6">
            {/* Timer Circle */}
            <div className="flex justify-center mb-6">
              <div className="relative">
                <svg className="w-32 h-32 transform -rotate-90">
                  <circle
                    cx="64"
                    cy="64"
                    r="45"
                    fill="none"
                    stroke={isDark ? "rgba(255,255,255,0.1)" : "rgba(0,0,0,0.1)"}
                    strokeWidth="8"
                  />
                  <circle
                    cx="64"
                    cy="64"
                    r="45"
                    fill="none"
                    stroke={isDark ? "#f97316" : "#ea580c"}
                    strokeWidth="8"
                    strokeDasharray={circumference}
                    strokeDashoffset={strokeDashoffset}
                    strokeLinecap="round"
                    className="transition-all duration-1000 ease-linear"
                  />
                </svg>
                <div className="absolute inset-0 flex flex-col items-center justify-center">
                  <div className="text-4xl font-bold">{countdown}</div>
                  <div className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-600'}`}>seconds</div>
                </div>
              </div>
            </div>
            
            {/* Warning Icon and Title */}
            <div className="text-center space-y-3">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-amber-500/20 mx-auto">
                <AlertTriangle className="h-8 w-8 text-amber-500" />
              </div>
              
              <h2 className="text-xl font-bold">Session About to Expire</h2>
              
              <p className={isDark ? "text-gray-400" : "text-gray-600"}>
                Your session will expire in <span className="font-bold text-amber-500">{countdown} seconds</span> due to inactivity.
              </p>

              <div className={`rounded-lg p-3 mt-2 ${
                isDark ? "bg-amber-500/10 border-amber-500/20" : "bg-amber-50 border-amber-200"
              } border`}>
                <p className={`text-sm ${isDark ? "text-amber-400" : "text-amber-600"}`}>
                  <span className="font-semibold">⚠️ Security Notice:</span> Please click "Stay Logged In" to continue your session.
                </p>
              </div>

              {/* Action Buttons */}
              <div className="flex gap-3 pt-6">
                <button
                  onClick={handleLogout}
                  className={`flex-1 py-3 px-4 rounded-lg border ${
                    isDark 
                      ? "border-gray-700 hover:bg-gray-800" 
                      : "border-gray-300 hover:bg-gray-100"
                  } transition-colors font-medium`}
                >
                  Logout Now
                </button>
                <button
                  onClick={handleExtend}
                  className={`flex-1 py-3 px-4 rounded-lg ${
                    isDark ? "bg-orange-600 hover:bg-orange-700" : "bg-orange-500 hover:bg-orange-600"
                  } text-white font-medium transition-colors`}
                >
                  Stay Logged In
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

// ============================================
// Session Expired Overlay - FIXED with auto-cleanup
// ============================================
function SessionExpiredOverlay({ show, isManual }) {
  const { theme } = useTheme();
  const isDark = theme === 'dark';
  const [isVisible, setIsVisible] = useState(false);
  
  useEffect(() => {
    if (show) {
      setIsVisible(true);
      // Auto-hide after 2 seconds
      const timer = setTimeout(() => {
        setIsVisible(false);
      }, 2000);
      return () => clearTimeout(timer);
    } else {
      setIsVisible(false);
    }
  }, [show]);
  
  if (!isVisible && !show) return null;

  return (
    <>
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-[100]" />
      <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <div className={`w-full max-w-md rounded-2xl shadow-2xl ${
          isDark ? 'bg-gray-900/95 backdrop-blur-lg border-gray-800' : 'bg-white/95 backdrop-blur-lg border-gray-200'
        } border ${isDark ? 'text-white' : 'text-gray-900'}`}>
          <div className="relative p-6">
            <div className="text-center space-y-4">
              <div className={`inline-flex items-center justify-center w-16 h-16 rounded-full mb-2 ${
                isManual
                  ? isDark ? 'bg-red-500/20' : 'bg-red-500/20'
                  : isDark ? 'bg-blue-500/20' : 'bg-blue-500/20'
              }`}>
                {isManual ? (
                  <LogOut size={32} className={isDark ? "text-red-400" : "text-red-500"} />
                ) : (
                  <AlertTriangle size={32} className={isDark ? "text-blue-400" : "text-blue-500"} />
                )}
              </div>
              
              <h2 className="text-xl font-bold">
                {isManual ? 'Logging Out' : 'Session Expired'}
              </h2>
              
              <p className={isDark ? "text-gray-400" : "text-gray-600"}>
                {isManual
                  ? "You are being logged out..."
                  : "Your session has expired due to inactivity. Redirecting to login..."}
              </p>

              <div className="flex justify-center pt-4">
                <div className={`w-8 h-8 border-2 rounded-full animate-spin ${
                  isManual
                    ? isDark ? 'border-red-400 border-t-transparent' : 'border-red-500 border-t-transparent'
                    : isDark ? 'border-blue-400 border-t-transparent' : 'border-blue-500 border-t-transparent'
                }`}></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

// ============================================
// Global Session Monitor - WITH WORKING LOGOUT AND CLEANUP
// ============================================
function GlobalSessionMonitor() {
  const { isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  
  const [showTimeoutWarning, setShowTimeoutWarning] = useState(false);
  const [timeoutCountdown, setTimeoutCountdown] = useState(30);
  const [sessionExpired, setSessionExpired] = useState(false);
  const [isManualLogout, setIsManualLogout] = useState(false);
  
  const timeoutIntervalRef = useRef(null);
  const inactivityTimerRef = useRef(null);
  const warningTimerRef = useRef(null);
  const isExtendingRef = useRef(false);
  const logoutTimeoutRef = useRef(null);
  const lastActivityRef = useRef(Date.now());
  const isModalShowingRef = useRef(false);
  const isLoggingOutRef = useRef(false);

  // Configuration constants
  const INACTIVITY_LIMIT = 5 * 60 * 1000; // 5 minutes total
  const WARNING_TIME = 60 * 1000; // Show warning after 60 seconds

  // Clear all storage on logout
  const clearAllStorage = useCallback(() => {
    console.log('🗑️ Clearing all storage on logout');
    
    sessionStorage.clear();
    
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    localStorage.removeItem('auth_token_expiry');
    localStorage.removeItem('auth_persistence_type');
    localStorage.removeItem('auth_remember_me');
    localStorage.removeItem('last_active_tab');
    localStorage.removeItem('just_logged_in');
    localStorage.removeItem('last_login_time');
    localStorage.removeItem('auth_redirect_pending');
    
    if (window.location.search) {
      window.history.replaceState({}, '', window.location.pathname);
    }
  }, []);

  const clearAllTimeouts = useCallback(() => {
    if (inactivityTimerRef.current) {
      clearTimeout(inactivityTimerRef.current);
      inactivityTimerRef.current = null;
    }
    if (warningTimerRef.current) {
      clearTimeout(warningTimerRef.current);
      warningTimerRef.current = null;
    }
    if (timeoutIntervalRef.current) {
      clearInterval(timeoutIntervalRef.current);
      timeoutIntervalRef.current = null;
    }
    if (logoutTimeoutRef.current) {
      clearTimeout(logoutTimeoutRef.current);
      logoutTimeoutRef.current = null;
    }
  }, []);

  const performLogout = useCallback(() => {
    // Prevent multiple logout calls
    if (isLoggingOutRef.current || sessionExpired) return;
    isLoggingOutRef.current = true;
    
    console.log('🚪 Performing logout...');
    
    // Clear all timeouts
    clearAllTimeouts();
    
    // Hide modal
    setShowTimeoutWarning(false);
    isModalShowingRef.current = false;
    
    // Clear all storage
    clearAllStorage();
    
    // Show expired overlay briefly, then clean up and navigate
    setSessionExpired(true);
    
    setTimeout(() => {
      console.log('🏁 Resetting states and navigating to login');
      
      // Reset all expired states BEFORE navigation
      setSessionExpired(false);
      setIsManualLogout(false);
      isLoggingOutRef.current = false;
      
      // Call logout from AuthContext
      logout();
      
      // Navigate to login page
      navigate('/login', { replace: true });
    }, 500);
  }, [logout, navigate, clearAllTimeouts, clearAllStorage, sessionExpired]);

  const handleTimeoutLogout = useCallback(() => {
    if (sessionExpired || isLoggingOutRef.current) return;
    console.log('⏰ Timeout triggered - logging out due to inactivity');
    performLogout();
  }, [performLogout, sessionExpired]);

  const handleManualLogout = useCallback(() => {
    if (sessionExpired || isLoggingOutRef.current) return;
    console.log('👤 Manual logout triggered');
    setIsManualLogout(true);
    performLogout();
  }, [performLogout, sessionExpired]);

  // Start the countdown when modal appears
  const startCountdown = useCallback(() => {
    if (timeoutIntervalRef.current) {
      clearInterval(timeoutIntervalRef.current);
      timeoutIntervalRef.current = null;
    }
    
    let countdownValue = 30;
    setTimeoutCountdown(countdownValue);
    
    console.log('⏰ Starting countdown from 30 seconds');
    
    timeoutIntervalRef.current = setInterval(() => {
      if (countdownValue > 0 && !isLoggingOutRef.current) {
        countdownValue--;
        console.log('⏰ Countdown:', countdownValue);
        setTimeoutCountdown(countdownValue);
        
        if (countdownValue === 0) {
          console.log('💀 Countdown finished - logging out');
          if (timeoutIntervalRef.current) {
            clearInterval(timeoutIntervalRef.current);
            timeoutIntervalRef.current = null;
          }
          handleTimeoutLogout();
        }
      }
    }, 1000);
  }, [handleTimeoutLogout]);

  // Reset timers when user is active (but only if modal is NOT showing)
  const resetTimersOnActivity = useCallback(() => {
    // If modal is already showing, DO NOT reset timers
    if (isModalShowingRef.current || isLoggingOutRef.current) {
      console.log('🚫 Modal is showing or logging out - ignoring activity reset');
      return;
    }
    
    console.log('🖱️ User activity detected - resetting timers');
    
    // Clear all existing timers
    clearAllTimeouts();
    
    // Reset countdown value for next time
    setTimeoutCountdown(30);
    
    // Start fresh timers
    warningTimerRef.current = setTimeout(() => {
      if (!isModalShowingRef.current && !sessionExpired && !isLoggingOutRef.current) {
        console.log('⚠️ Showing timeout warning');
        isModalShowingRef.current = true;
        setShowTimeoutWarning(true);
        startCountdown();
      }
    }, WARNING_TIME);
    
    inactivityTimerRef.current = setTimeout(() => {
      if (!isModalShowingRef.current && !sessionExpired && !isLoggingOutRef.current) {
        console.log('⏰ Inactivity limit reached - logging out');
        handleTimeoutLogout();
      }
    }, INACTIVITY_LIMIT);
  }, [clearAllTimeouts, WARNING_TIME, INACTIVITY_LIMIT, startCountdown, handleTimeoutLogout, sessionExpired]);

  // Extend session (ONLY called by Stay Logged In button)
  const extendSession = useCallback(() => {
    if (isExtendingRef.current || sessionExpired || isLoggingOutRef.current) return;
    isExtendingRef.current = true;
    
    console.log('🔄 Extending session via Stay Logged In button...');
    
    // Clear all existing timeouts and countdown
    clearAllTimeouts();
    
    // Hide modal
    setShowTimeoutWarning(false);
    isModalShowingRef.current = false;
    
    // Reset countdown value
    setTimeoutCountdown(30);
    
    // Start fresh inactivity timers
    warningTimerRef.current = setTimeout(() => {
      if (!isModalShowingRef.current && !sessionExpired && !isLoggingOutRef.current) {
        console.log('⚠️ Showing timeout warning after extension');
        isModalShowingRef.current = true;
        setShowTimeoutWarning(true);
        startCountdown();
      }
    }, WARNING_TIME);
    
    inactivityTimerRef.current = setTimeout(() => {
      if (!isModalShowingRef.current && !sessionExpired && !isLoggingOutRef.current) {
        console.log('⏰ Inactivity limit reached - logging out');
        handleTimeoutLogout();
      }
    }, INACTIVITY_LIMIT);
    
    setTimeout(() => {
      isExtendingRef.current = false;
    }, 500);
  }, [sessionExpired, clearAllTimeouts, WARNING_TIME, INACTIVITY_LIMIT, startCountdown, handleTimeoutLogout]);

  // Handle user activity with throttling
  const handleUserActivity = useCallback(() => {
    const now = Date.now();
    // Throttle to once per second
    if (now - lastActivityRef.current < 1000) return;
    lastActivityRef.current = now;
    
    if (!sessionExpired && !isLoggingOutRef.current) {
      resetTimersOnActivity();
    }
  }, [sessionExpired, resetTimersOnActivity]);

  // Set up event listeners for user activity
  useEffect(() => {
    if (!isAuthenticated || sessionExpired || isLoggingOutRef.current) return;
    
    let activityTimeout;
    const throttledActivityHandler = () => {
      if (activityTimeout) clearTimeout(activityTimeout);
      activityTimeout = setTimeout(() => {
        handleUserActivity();
      }, 100);
    };
    
    const events = [
      'mousedown', 'mousemove', 'mouseup', 'click', 'dblclick',
      'keydown', 'keyup', 'keypress', 'scroll', 'touchstart',
      'touchmove', 'touchend', 'wheel', 'focus', 'input'
    ];
    
    events.forEach(event => {
      document.addEventListener(event, throttledActivityHandler);
    });
    
    // Start initial timers
    const initialTimer = setTimeout(() => {
      if (!isLoggingOutRef.current) {
        resetTimersOnActivity();
      }
    }, 1000);
    
    return () => {
      events.forEach(event => {
        document.removeEventListener(event, throttledActivityHandler);
      });
      if (activityTimeout) clearTimeout(activityTimeout);
      clearTimeout(initialTimer);
      clearAllTimeouts();
    };
  }, [isAuthenticated, sessionExpired, handleUserActivity, resetTimersOnActivity, clearAllTimeouts]);

  // Clean up on unmount
  useEffect(() => {
    return () => {
      clearAllTimeouts();
      setShowTimeoutWarning(false);
      setSessionExpired(false);
      isModalShowingRef.current = false;
      isLoggingOutRef.current = false;
    };
  }, [clearAllTimeouts]);

  return (
    <>
      <TimeoutWarningModal 
        show={showTimeoutWarning}
        countdown={timeoutCountdown}
        onExtend={extendSession}
        onLogout={handleManualLogout}
      />
      <SessionExpiredOverlay 
        show={sessionExpired}
        isManual={isManualLogout}
      />
    </>
  );
}

// ============================================
// Navigation Initializer
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
// Session Monitor Component (Token expiry only)
// ============================================
function SessionMonitor() {
  const { isAuthenticated, logout, getPersistenceInfo } = useAuth();
  const navigate = useNavigate();
  const isLoggingOut = useRef(false);

  useEffect(() => {
    if (!isAuthenticated) return;

    const persistenceInfo = getPersistenceInfo();
    
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
          
          setTimeout(() => {
            logout();
            navigate('/login', { replace: true });
          }, 500);
        }
      }
    };

    checkSession();
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
  const navigate = useNavigate();
  const [isRedirecting, setIsRedirecting] = useState(false);

  useEffect(() => {
    if (isRedirecting) return;
    if (!isInitialized) return;
    
    if (!isAuthenticated || !token) {
      console.log('🔒 Protected - Not authenticated, redirecting to login');
      setIsRedirecting(true);
      navigate('/login', { replace: true });
    }
  }, [isInitialized, isAuthenticated, token, navigate, isRedirecting]);

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

  if (!user || !isAuthenticated || !token) {
    return null;
  }

  if (roles.length > 0 && !roles.includes(user.role)) {
    console.log('🔒 Protected - Role not authorized');
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
    return null;
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

  return null;
}

// ============================================
// Auth Check Wrapper
// ============================================
function AuthCheckWrapper({ children }) {
  const { token, isAuthenticated, user } = useAuth();
  
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
      <GlobalSessionMonitor />
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