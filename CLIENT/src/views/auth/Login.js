// components/auth/Login.js
import React, { useEffect } from "react";
import { useTheme } from "@/context/ThemeContext.js";
import { Button } from "@/components/ui/button";
import { 
  Database,
  Sun, 
  Moon,
  Eye, 
  EyeOff,
  User,
  Lock,
  AlertCircle,
  Check,
  Server,
  Shield,
  ChevronRight,
  Globe,
  HardDrive,
  Key,
  Cpu,
  Sparkles,
  LogIn,
  Fingerprint,
  X,
  CheckCircle,
  Zap
} from "lucide-react";
import OtpForm from "./OtpForm.js";
import PasswordResetForm from "./PasswordResetForm.js";
import ForgotPasswordModal from "../../components/modals/ForgotPasswordModal.js";
import { useLogin } from "../../hooks/useLogin.js";

export default function Login() {
  const { theme, toggle } = useTheme();
  const { 
    state, 
    setIsMobile,
    inputsRef,
    clearPortalValidationError,
    setUserId,
    setPassword,
    setShowPassword,
    setUserType,
    setOtp,
    setNewPassword,
    setConfirmPassword,
    setShowNewPassword,
    setShowConfirmPassword,
    setShowForgotPassword,
    setForgotUserId,
    handleLogin,
    handleOtpChange,
    handleOtpKeyDown,
    handleResendOtp,
    verifyOtp,
    handlePasswordReset,
    handleForgotPassword,
    handleBackFromReset,
    handleBackFromOtp,
  } = useLogin();

  const { 
    isMobile, 
    showOtp, 
    requiresPasswordReset, 
    showForgotPassword,
    userId,
    password,
    showPassword,
    loading,
    userType,
    portalValidationError,
    forgotUserId,
    forgotLoading
  } = state;

  const isDark = theme === 'dark';

  // Color scheme - UPDATED TO MATCH DASHBOARD COMPONENT EXACTLY
  const colors = isDark ? {
    // Using exact same colors as Dashboard
    bg: 'rgb(1 14 35)',
    white: '#FFFFFF',
    sidebar: 'rgb(41 53 72 / 39%)',
    main: 'rgb(1 14 35)',
    header: 'rgb(20 26 38)',
    card: 'rgb(41 53 72 / 39%)',
    
    // Text - coordinating grays - UPDATED
    text: '#F1F5F9',
    textSecondary: 'rgb(148 163 184)',
    textTertiary: 'rgb(100 116 139)',
    
    // Borders - UPDATED to match Dashboard
    border: 'rgb(51 65 85)',
    borderLight: 'rgb(45 55 72)',
    borderDark: 'rgb(71 85 105)',
    
    // Interactive - UPDATED
    hover: 'rgb(45 46 72 / 33%)',
    active: 'rgb(59 74 99)',
    selected: 'rgb(44 82 130)',
    
    // Primary colors - UPDATED
    primary: 'rgb(96 165 250)',
    primaryLight: 'rgb(147 197 253)',
    primaryDark: 'rgb(37 99 235)',
    
    // Status colors - UPDATED
    success: 'rgb(52 211 153)',
    warning: 'rgb(251 191 36)',
    error: 'rgb(248 113 113)',
    info: 'rgb(96 165 250)',
    
    // UI Components - UPDATED
    tabActive: 'rgb(96 165 250)',
    tabInactive: 'rgb(148 163 184)',
    sidebarActive: 'rgb(96 165 250)',
    sidebarhover: 'rgb(45 46 72 / 33%)',
    inputBg: 'rgb(41 53 72 / 39%)',
    inputBorder: 'rgb(51 65 85)',
    tableHeader: 'rgb(41 53 72 / 39%)',
    tableRow: 'rgb(41 53 72 / 39%)',
    tableRowhover: 'rgb(45 46 72 / 33%)',
    dropdownBg: 'rgb(41 53 72 / 39%)',
    dropdownBorder: 'rgb(51 65 85)',
    modalBg: 'rgb(41 53 72 / 39%)',
    modalBorder: 'rgb(51 65 85)',
    codeBg: 'rgb(41 53 72 / 39%)',
    
    // Connection status - UPDATED
    connectionOnline: 'rgb(52 211 153)',
    connectionOffline: 'rgb(248 113 113)',
    connectionIdle: 'rgb(251 191 36)',
    
    // Accent colors - UPDATED
    accentPurple: 'rgb(167 139 250)',
    accentPink: 'rgb(244 114 182)',
    accentCyan: 'rgb(34 211 238)',
    
    // Additional for login component - UPDATED
    gradient: 'from-blue-500/20 via-violet-500/20 to-orange-500/20'
  } : {
    // Light mode - kept consistent
    bg: '#f8fafc',
    white: '#f8fafc',
    sidebar: '#ffffff',
    main: '#f8fafc',
    header: '#ffffff',
    card: '#ffffff',
    text: '#1e293b',
    textSecondary: '#64748b',
    textTertiary: '#94a3b8',
    border: '#e2e8f0',
    borderLight: '#f1f5f9',
    borderDark: '#cbd5e1',
    hover: '#f1f5f9',
    active: '#e2e8f0',
    selected: '#dbeafe',
    primary: '#1e293b',
    primaryLight: '#60a5fa',
    primaryDark: '#2563eb',
    success: '#10b981',
    warning: '#f59e0b',
    error: '#ef4444',
    info: '#3b82f6',
    tabActive: '#3b82f6',
    tabInactive: '#64748b',
    sidebarActive: '#3b82f6',
    sidebarHover: '#f1f5f9',
    inputBg: '#ffffff',
    inputBorder: '#e2e8f0',
    tableHeader: '#f8fafc',
    tableRow: '#ffffff',
    tableRowHover: '#f8fafc',
    dropdownBg: '#ffffff',
    dropdownBorder: '#e2e8f0',
    modalBg: '#ffffff',
    modalBorder: '#e2e8f0',
    codeBg: '#f1f5f9',
    connectionOnline: '#10b981',
    connectionOffline: '#ef4444',
    connectionIdle: '#f59e0b',
    gradient: 'from-blue-400/20 via-violet-400/20 to-orange-400/20'
  };

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };
    checkMobile();
    window.addEventListener("resize", checkMobile);
    return () => window.removeEventListener("resize", checkMobile);
  }, []);

  useEffect(() => {
    clearPortalValidationError();
  }, [userType, userId]);

  // Set Oracle as default when component mounts
  useEffect(() => {
    setUserType('oracle');
  }, []);

  return (
    <div className="min-h-screen flex items-center justify-center p-4 relative overflow-hidden" style={{ 
      backgroundColor: colors.bg,
      color: colors.text
    }}>
      
      {/* Animated Background Elements */}
      <div className="absolute inset-0 overflow-hidden">
        <div className={`absolute -top-40 -right-40 w-80 h-80 rounded-full bg-gradient-to-br ${colors.gradient} blur-3xl animate-pulse`}></div>
        <div className={`absolute -bottom-40 -left-40 w-80 h-80 rounded-full bg-gradient-to-br ${colors.gradient} blur-3xl animate-pulse delay-1000`}></div>
      </div>

      {/* Theme Toggle - Top Right */}
      {/* <button
        onClick={toggle}
        className={`fixed top-6 right-6 p-2.5 rounded-xl border transition-all duration-300 z-10 group hover-lift`}
        style={{ 
          backgroundColor: colors.hover,
          borderColor: colors.border,
          color: colors.textSecondary
        }}
        aria-label="Toggle theme"
      >
        <div className="relative">
          {isDark ? (
            <Sun className="h-5 w-5 transform group-hover:rotate-45 transition-transform group-hover:text-yellow-400" />
          ) : (
            <Moon className="h-5 w-5 transform group-hover:-rotate-12 transition-transform group-hover:text-blue-600" />
          )}
        </div>
      </button> */}

      {/* Main Content */}
      <div className="w-full max-w-md relative z-10">
        {/* Login Card */}
        <div className={`rounded-2xl border transition-all duration-300 overflow-hidden shadow-xl hover:shadow-2xl`}
          style={{ 
            backgroundColor: colors.card,
            borderColor: colors.border,
            backdropFilter: isDark ? 'blur(10px)' : 'none'
          }}>
          
          {/* Card Header Gradient */}
          <div className="h-1 bg-gradient-to-r from-blue-500 via-violet-500 to-blue-500"></div>
          
          {/* Card Header */}
          <div className="p-7 pb-4" onClick={toggle}>
            <div className="flex items-center justify-between mb-1">
              <div>
                <h2 className="text-xl font-bold" style={{ color: colors.text }}>
                  {requiresPasswordReset ? 'Reset Password' : showOtp ? '' : 'Welcome Back'}
                </h2>
                <p className="text-sm mt-1.5 flex items-center gap-1.5" style={{ color: colors.textSecondary }}>
                  {!showOtp && !requiresPasswordReset && (
                    <>
                      <Zap className="h-3.5 w-3.5" style={{ color: colors.primary }} />
                      Sign in to continue to your dashboard
                    </>
                  )}
                </p>
              </div>
            </div>
          </div>

          {/* Card Body */}
          <div className="p-7 pt-4">
            {/* Form Content */}
            <div>
              {requiresPasswordReset ? (
                <PasswordResetForm 
                  state={state}
                  colors={colors}
                  isMobile={isMobile}
                  isDark={isDark}
                  onPasswordReset={{
                    handlePasswordReset,
                    setNewPassword,
                    setConfirmPassword,
                    setShowNewPassword,
                    setShowConfirmPassword
                  }}
                  onBackFromReset={handleBackFromReset}
                />
              ) : showOtp ? (
                <OtpForm 
                  state={state}
                  inputsRef={inputsRef}
                  colors={colors}
                  isMobile={isMobile}
                  isDark={isDark}
                  onOtpActions={{
                    handleOtpChange,
                    handleOtpKeyDown,
                    handleResendOtp,
                    verifyOtp,
                    handleBackFromOtp,
                    setOtp
                  }}
                />
              ) : (
                <>
                  {/* Error Message */}
                  {portalValidationError && (
                    <div className="mb-6 p-4 rounded-xl border transition-all duration-300 animate-shake"
                      style={{ 
                        backgroundColor: `${colors.error}15`,
                        borderColor: `${colors.error}30`
                      }}>
                      <div className="flex items-start gap-3">
                        <AlertCircle className="h-5 w-5 flex-shrink-0 mt-0.5" style={{ color: colors.error }} />
                        <div>
                          <p className="text-sm font-semibold mb-1" style={{ color: colors.error }}>
                            Connection Failed
                          </p>
                          <p className="text-sm" style={{ color: colors.textSecondary }}>
                            Unable to connect to {portalValidationError.portal}. Please try again.
                          </p>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* Login Form */}
                  <form onSubmit={handleLogin} className="space-y-5">
                    {/* Username Field */}
                    <div className="space-y-2">
                      <label className="block text-sm font-semibold" style={{ color: colors.text }}>
                        Username
                      </label>
                      <div className="relative group">
                        <input
                          type="text"
                          value={userId}
                          onChange={(e) => setUserId(e.target.value)}
                          className="w-full pl-11 pr-4 py-3 rounded-xl border relative z-10 transition-all duration-300 outline-none hover-lift"
                          style={{ 
                            backgroundColor: colors.inputBg,
                            borderColor: colors.inputBorder,
                            color: colors.text
                          }}
                          placeholder="Enter your username"
                          required
                          autoComplete="username"
                        />
                        <div className="absolute left-3.5 top-1/2 transform -translate-y-1/2 p-1.5 rounded-lg transition-colors duration-300"
                          style={{ backgroundColor: colors.hover }}>
                          <User className="h-4 w-4" style={{ color: colors.textSecondary }} />
                        </div>
                      </div>
                    </div>

                    {/* Password Field */}
                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <label className="block text-sm font-semibold" style={{ color: colors.text }}>
                          Password
                        </label>
                        <button
                          type="button"
                          onClick={() => setShowForgotPassword(true)}
                          className="text-xs font-medium px-2 py-1 rounded-lg transition-all duration-300 hover-lift hover:scale-105 active:scale-95"
                          style={{ 
                            color: colors.primary,
                            backgroundColor: `${colors.primary}10`
                          }}
                        >
                          Forgot password?
                        </button>
                      </div>
                      <div className="relative group">
                        <input
                          type={showPassword ? "text" : "password"}
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          className="w-full pl-11 pr-11 py-3 rounded-xl border relative z-10 transition-all duration-300 outline-none hover-lift"
                          style={{ 
                            backgroundColor: colors.inputBg,
                            borderColor: colors.inputBorder,
                            color: colors.text
                          }}
                          placeholder="Enter your password"
                          required
                          autoComplete="current-password"
                        />
                        <div className="absolute left-3.5 top-1/2 transform -translate-y-1/2 p-1.5 rounded-lg transition-colors duration-300"
                          style={{ backgroundColor: colors.hover }}>
                          <Lock className="h-4 w-4" style={{ color: colors.textSecondary }} />
                        </div>
                        <button
                          type="button"
                          onClick={() => setShowPassword(!showPassword)}
                          className="absolute right-3.5 top-1/2 transform -translate-y-1/2 p-2 rounded-lg transition-all duration-300 hover-lift hover:scale-110 active:scale-95"
                          style={{ 
                            backgroundColor: colors.hover,
                            border: `1px solid ${colors.border}`,
                            color: colors.textSecondary
                          }}
                          aria-label={showPassword ? "Hide password" : "Show password"}
                        >
                          {showPassword ? (
                            <EyeOff className="h-4 w-4" style={{ color: colors.primary }} />
                          ) : (
                            <Eye className="h-4 w-4" style={{ color: colors.textSecondary }} />
                          )}
                        </button>
                      </div>
                    </div>

                    {/* Remember Me */}
                    <div className="flex items-center gap-3">
                      <div className="relative">
                        <input
                          type="checkbox"
                          id="remember"
                          className="appearance-none h-4.5 w-4.5 rounded border checked:border-transparent focus:ring-2 focus:ring-offset-2 focus:ring-offset-transparent transition-all duration-300"
                          style={{ 
                            borderColor: colors.border,
                            backgroundColor: colors.inputBg
                          }}
                        />
                        <Check className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 h-3 w-3 text-white pointer-events-none opacity-0 checked:opacity-100 transition-opacity duration-300" 
                          style={{ color: colors.primary }} />
                      </div>
                      <label htmlFor="remember" className="text-sm font-medium cursor-pointer hover:text-opacity-80" style={{ color: colors.textSecondary }}>
                        Keep me signed in
                      </label>
                    </div>

                    {/* Submit Button */}
                    <Button
                      type="submit"
                      className="w-full py-3.5 rounded-xl font-semibold transition-all duration-300 relative group overflow-hidden hover-lift hover:shadow-lg"
                      style={{ 
                        backgroundColor: colors.primary,
                        color: isDark ? '#0f172a' : 'white',
                        boxShadow: `0 4px 14px ${colors.primary}40`
                      }}
                      disabled={loading}
                    >
                      <div className="absolute inset-0 bg-white/20 transform -translate-x-full group-hover:translate-x-full transition-transform duration-700"></div>
                      {loading ? (
                        <div className="flex items-center justify-center gap-3 relative z-10">
                          <div className="h-4 w-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                          <span>Signing in...</span>
                        </div>
                      ) : (
                        <div className="flex items-center justify-center gap-3 relative z-10">
                          <LogIn className="h-4 w-4" />
                          <span>Sign In</span>
                          <ChevronRight className="h-4 w-4 group-hover:translate-x-1 transition-transform" />
                        </div>
                      )}
                    </Button>
                  </form>

                  {/* Security Info */}
                  <div className="mt-8 pt-6" style={{ borderTop: `1px solid ${colors.border}` }}>
                    <div className="flex items-center justify-center gap-3">
                      <div className="p-2 rounded-lg border hover-lift"
                        style={{ 
                          backgroundColor: colors.hover,
                          borderColor: colors.border
                        }}>
                        <Shield className="h-4 w-4" style={{ color: colors.primary }} />
                      </div>
                      <div>
                        <p className="text-sm font-medium" style={{ color: colors.text }}>
                          Secure & Encrypted
                        </p>
                        <p className="text-xs" style={{ color: colors.textSecondary }}>
                          End-to-end encrypted connection
                        </p>
                      </div>
                    </div>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="mt-8 text-center">
          <p className="text-xs" style={{ color: colors.textTertiary }}>
            © {new Date().getFullYear()} API Automation Platform. All rights reserved.
          </p>
          <div className="flex items-center justify-center gap-4 mt-3">
            <div className="text-xs px-2 py-1 rounded-full hover-lift"
              style={{ 
                backgroundColor: colors.hover,
                color: colors.textSecondary
              }}>
              v2.1.0
            </div>
            <div className="text-xs px-2 py-1 rounded-full hover-lift"
              style={{ 
                backgroundColor: colors.hover,
                color: colors.success
              }}>
              <Fingerprint className="inline h-3 w-3 mr-1" />
              Secure
            </div>
            <div className="text-xs px-2 py-1 rounded-full hover-lift"
              style={{ 
                backgroundColor: colors.hover,
                color: colors.primary
              }}>
              <Server className="inline h-3 w-3 mr-1" />
              Active
            </div>
          </div>
        </div>
      </div>

      {/* Forgot Password Modal */}
      <ForgotPasswordModal
        showForgotPassword={showForgotPassword}
        state={{ forgotUserId, forgotLoading }}
        colors={colors}
        isMobile={isMobile}
        isDark={isDark}
        onForgotPassword={{
          handleForgotPassword,
          setShowForgotPassword,
          setForgotUserId
        }}
      />
    </div>
  );
}

// Add these styles to your global CSS file or within a <style> tag
const globalStyles = `
@keyframes fade-in {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
  20%, 40%, 60%, 80% { transform: translateX(5px); }
}

.animate-fade-in {
  animation: fade-in 0.6s ease-out;
}

.animate-shake {
  animation: shake 0.5s ease-in-out;
}

/* Hover effects */
.hover-lift:hover {
  transform: translateY(-2px);
  transition: transform 0.2s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.gradient-bg {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(139, 92, 246, 0.1) 50%, rgba(249, 115, 22, 0.1) 100%);
}

/* Smooth transitions */
* {
  transition: background-color 0.3s ease, border-color 0.3s ease, color 0.3s ease;
}

/* Custom scrollbar - UPDATED TO MATCH DASHBOARD */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: rgb(51 65 85);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: rgb(100 116 139);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgb(148 163 184);
}

/* Mobile optimizations */
@media (max-width: 640px) {
  .text-xs { font-size: 11px; }
  .text-sm { font-size: 12px; }
  .text-lg { font-size: 16px; }
  .text-xl { font-size: 18px; }
  .text-2xl { font-size: 20px; }
}
`;

// Export the styles for use in your application
export { globalStyles };