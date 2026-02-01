import React, { useEffect } from "react";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate, useLocation } from "react-router-dom";
import { RecoilRoot } from "recoil";

import Login from "./src/views/auth/Login.js";

import { ThemeProvider } from "@/context/ThemeContext.js";
import { AuthProvider, useAuth } from "@/context/AuthContext.js";
import { LogProvider, useLog } from "@/context/LogContext.js";

import EntryPage from "@/views/index.js";

// Import the SweetAlert wrapper
import SweetAlertWrapper from "@/components/wrapper/SweetAlertWrapper";

const queryClient = new QueryClient();

// ========================
// Protected Component
// ========================
function Protected({ children, roles = [] }) {
  const { user, isInitialized } = useAuth();

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

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (roles.length > 0 && !roles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  return children;
}

// ========================
// PublicRoute Component
// ========================
function PublicRoute({ children }) {
  const { user, isInitialized } = useAuth();

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

  if (user) {
    if (user.role === "oracle") return <Navigate to="/admin" replace />;
    if (user.role === "user") return <Navigate to="/user" replace />;
    return <Navigate to="/" replace />;
  }

  return children;
}

// ========================
// Route Logger
// ========================
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
  }, [location.pathname]);

  return null;
}

// ========================
// Catch-all Redirect
// ========================
function RouteRedirect() {
  const { user, isInitialized } = useAuth();

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

  if (user) {
    if (user.role === "oracle") return <Navigate to="/admin" replace />;
    if (user.role === "user") return <Navigate to="/user" replace />;
    return <Navigate to="/" replace />;
  }

return <Navigate to="/login" replace />;

}

// ========================
// Routes Tree
// ========================
function RoutesTree() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />

      {/* Role-based routes */}
      <Route path="/admin" element={
        <Protected roles={["oracle"]}>
            <EntryPage /> {/* Replace with Admin-specific component */}
        </Protected>
      } />

      {/* General EntryPage */}
      <Route path="/" element={
        <Protected>
            <Login />
        </Protected>
      }>
      </Route>

      {/* Catch all */}
      <Route path="*" element={<RouteRedirect />} />
    </Routes>
  );
}

// ========================
// Main App
// ========================
export default function App() {
  return (
    <RecoilRoot>
      <QueryClientProvider client={queryClient}>
        <LogProvider>
          <ThemeProvider>
            <AuthProvider>
              {/* Wrap everything with SweetAlertWrapper */}
              <SweetAlertWrapper>
                <TooltipProvider>
                  <Toaster />
                  <BrowserRouter
                    future={{
                      v7_startTransition: true,
                      v7_relativeSplatPath: true,
                    }}
                  >
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