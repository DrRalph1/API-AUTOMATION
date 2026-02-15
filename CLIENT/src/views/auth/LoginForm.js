// components/auth/LoginForm.js
import React from "react";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { 
  Eye, 
  EyeOff, 
  Lock, 
  User, 
  AlertCircle,
  Shield,
  Check,
  ChevronRight,
  Database,
  Server,
  Zap,
  Cloud,
  Key,
  CheckCircle
} from "lucide-react";

const LoginForm = ({ 
  state, 
  colors, 
  isMobile, 
  isDark, 
  databaseOptions,
  currentDb,
  onFormActions 
}) => {
  const {
    userId,
    password,
    showPassword,
    loading,
    userType,
    portalValidationError
  } = state;

  const {
    setUserId,
    setPassword,
    setShowPassword,
    setShowForgotPassword,
    setUserType,
    handleLogin
  } = onFormActions || {};

  return (
    <motion.div
      key="login"
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      exit={{ opacity: 0, scale: 0.95 }}
      className="space-y-6"
    >
      {/* Database Type Selector */}
      <div>
        <p className="text-sm font-medium mb-3" style={{ color: colors.text }}>
          Database Type
        </p>
        
        <div className="grid grid-cols-3 gap-2">
          {databaseOptions.map((db) => (
            <button
              key={db.id}
              type="button"
              onClick={() => setUserType(db.id)}
              className={`group relative p-3 rounded-xl border transition-all duration-300 hover-lift ${
                userType === db.id ? 'ring-2 ring-offset-2 ring-offset-transparent' : ''
              }`}
              style={{ 
                backgroundColor: userType === db.id ? colors.selected : colors.hover,
                borderColor: userType === db.id ? colors.primary : colors.border,
                borderWidth: userType === db.id ? '2px' : '1px',
                borderStyle: 'solid',
                ringColor: colors.primary
              }}
            >
              {userType === db.id && (
                <div className="absolute -top-1.5 -right-1.5 z-10">
                  <div className="h-5 w-5 rounded-full flex items-center justify-center shadow-md"
                    style={{ 
                      backgroundColor: colors.primary,
                      color: 'white'
                    }}>
                    <Check className="h-3 w-3" />
                  </div>
                </div>
              )}
              
              <div className="flex flex-col items-center">
                <div className={`p-2.5 rounded-lg mb-2.5 transition-colors ${
                  userType === db.id ? 'gradient-bg' : ''
                }`}
                  style={{ 
                    backgroundColor: userType === db.id ? `${colors.primary}15` : colors.inputBg,
                    border: `1px solid ${userType === db.id ? colors.primary : colors.border}`
                  }}>
                  <db.icon className="h-5 w-5" 
                    style={{ 
                      color: userType === db.id ? colors.primary : colors.textSecondary 
                    }} />
                </div>
                <span className="text-xs font-medium mb-1" 
                  style={{ 
                    color: userType === db.id ? colors.primary : colors.text 
                  }}>
                  {db.name}
                </span>
                <span className="text-xs text-center px-1" 
                  style={{ color: colors.textTertiary }}>
                  {db.description}
                </span>
              </div>
            </button>
          ))}
        </div>
      </div>

      {/* Error Message */}
      {portalValidationError && (
        <div className="p-4 rounded-xl border transition-all duration-300 animate-shake"
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
        <div>
          <label className="block text-sm font-semibold mb-2" style={{ color: colors.text }}>
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
              placeholder="Enter username"
              required
              autoComplete="username"
            />
            <div className="absolute left-3.5 top-1/2 transform -translate-y-1/2 p-1.5 rounded-lg transition-colors duration-300"
              style={{ backgroundColor: colors.hover }}>
              <User className="h-4 w-4" style={{ color: colors.textSecondary }} />
            </div>
          </div>
        </div>

        <div>
          <div className="flex items-center justify-between mb-2">
            <label className="block text-sm font-semibold" style={{ color: colors.text }}>
              Password
            </label>
            <button
              type="button"
              onClick={() => setShowForgotPassword(true)}
              className="text-xs font-medium px-2 py-1 rounded-lg transition-all duration-300 hover-lift"
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
              placeholder="Enter password"
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
              className="absolute right-3.5 top-1/2 transform -translate-y-1/2 p-2 rounded-lg transition-all duration-300 hover-lift"
              style={{ 
                backgroundColor: colors.hover,
                color: colors.textSecondary
              }}
            >
              {showPassword ? (
                <EyeOff className="h-4 w-4" />
              ) : (
                <Eye className="h-4 w-4" />
              )}
            </button>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <div className="relative">
            <input
              type="checkbox"
              id="remember"
              className="appearance-none h-4.5 w-4.5 rounded border checked:border-transparent focus:ring-2 focus:ring-offset-2 focus:ring-offset-transparent transition-all duration-300"
              style={{ 
                borderColor: colors.border,
                backgroundColor: colors.inputBg,
                focusRingColor: colors.primary
              }}
            />
            <Check className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 h-3 w-3 text-white pointer-events-none opacity-0 checked:opacity-100 transition-opacity duration-300" />
          </div>
          <label htmlFor="remember" className="text-sm font-medium cursor-pointer" 
            style={{ color: colors.textSecondary }}>
            Keep me signed in
          </label>
        </div>

        <Button
          type="submit"
          className="w-full py-3.5 bg-gradient-to-r from-blue-500 via-violet-500 to-blue-500 rounded-xl font-semibold transition-all duration-300 relative group overflow-hidden hover-lift"
          style={{ 
            // backgroundColor: colors.primary,
            // color: 'white'
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
              <Key className="h-4 w-4" />
              <span>Sign In</span>
              <ChevronRight className="h-4 w-4 group-hover:translate-x-1 transition-transform" />
            </div>
          )}
        </Button>
      </form>

      {/* Security Info */}
      <div className="pt-6 border-t" style={{ borderColor: colors.border }}>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
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
                End-to-end encrypted
              </p>
            </div>
          </div>
          <div className="text-right">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full" style={{ backgroundColor: colors.success }}></div>
              <p className="text-sm font-medium" style={{ color: colors.text }}>
                {currentDb.name}
              </p>
            </div>
            <p className="text-xs" style={{ color: colors.textSecondary }}>
              Active database
            </p>
          </div>
        </div>
      </div>

      {/* Database Info */}
      <div className="grid grid-cols-3 gap-2 pt-2">
        {[
          { icon: Server, label: 'Uptime', value: '99.9%' },
          { icon: Zap, label: 'Latency', value: '32ms' },
          { icon: Cloud, label: 'Status', value: 'Online' }
        ].map((item, index) => (
          <div key={index} className="p-2 rounded-lg hover-lift"
            style={{ 
              backgroundColor: colors.hover,
              border: `1px solid ${colors.border}`
            }}>
            <div className="flex items-center gap-2">
              <item.icon className="h-3.5 w-3.5" style={{ color: colors.textSecondary }} />
              <span className="text-xs" style={{ color: colors.textSecondary }}>{item.label}</span>
            </div>
            <div className="text-xs font-medium mt-1" style={{ color: colors.text }}>{item.value}</div>
          </div>
        ))}
      </div>
    </motion.div>
  );
};

export default LoginForm;