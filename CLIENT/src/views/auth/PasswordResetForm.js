// components/auth/PasswordResetForm.js
import React from "react";
import { Button } from "@/components/ui/button";
import { 
  Shield, 
  ArrowLeft, 
  Loader2, 
  Check,
  Eye, 
  EyeOff, 
  Lock,
  AlertCircle,
  Key,
  CheckCircle,
  XCircle,
  Info
} from "lucide-react";

const PasswordResetForm = ({ 
  state, 
  colors, 
  isMobile, 
  isDark, 
  onPasswordReset,
  onBackFromReset 
}) => {
  const {
    newPassword,
    confirmPassword,
    showNewPassword,
    showConfirmPassword,
    loading,
    passwordError
  } = state;

  const {
    handlePasswordReset,
    setNewPassword,
    setConfirmPassword,
    setShowNewPassword,
    setShowConfirmPassword
  } = onPasswordReset;

  // Check password requirements
  const requirements = {
    minLength: newPassword.length >= 12,
    hasUpperCase: /[A-Z]/.test(newPassword),
    hasLowerCase: /[a-z]/.test(newPassword),
    hasNumber: /\d/.test(newPassword),
    hasSpecialChar: /[!@#$%^&*(),.?":{}|<>]/.test(newPassword),
    passwordsMatch: newPassword === confirmPassword && newPassword.length > 0
  };

  const allRequirementsMet = Object.values(requirements).every(Boolean);
  const passwordsMatch = newPassword === confirmPassword && confirmPassword.length > 0;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="text-center mb-2 -mt-2">
        <div className="inline-flex items-center justify-center w-14 h-14 rounded-xl border gradient-bg mb-4 hover-lift"
          style={{ 
            borderColor: colors.border,
            backgroundColor: colors.card 
          }}>
          <Shield className="h-7 w-7" style={{ color: colors.primary }} />
        </div>
        <h3 className="text-xl font-bold mb-2" style={{ color: colors.text }}>
          Password Reset Required
        </h3>
        <p className="text-sm max-w-sm mx-auto" style={{ color: colors.textSecondary }}>
          For security reasons, you must update your password
        </p>
      </div>

      {/* Error Message */}
      {passwordError && (
        <div className="p-4 rounded-xl border transition-all duration-300 animate-shake"
          style={{ 
            backgroundColor: `${colors.error}15`,
            borderColor: `${colors.error}30`
          }}>
          <div className="flex items-start gap-3">
            <AlertCircle className="h-5 w-5 flex-shrink-0 mt-0.5" style={{ color: colors.error }} />
            <div>
              <p className="text-sm font-semibold mb-1" style={{ color: colors.error }}>
                {passwordError.title || 'Password Error'}
              </p>
              <p className="text-sm" style={{ color: colors.textSecondary }}>
                {passwordError.message}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Form */}
      <form onSubmit={handlePasswordReset} className="space-y-5">
        {/* New Password */}
        <div>
          <label className="block text-sm font-semibold mb-2" style={{ color: colors.text }}>
            New Password
          </label>
          <div className="relative group">
            <input
              type={showNewPassword ? "text" : "password"}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="w-full pl-11 pr-11 py-3 rounded-xl border relative z-10 transition-all duration-300 outline-none hover-lift"
              style={{ 
                backgroundColor: colors.inputBg,
                borderColor: colors.inputBorder,
                color: colors.text
              }}
              placeholder="Enter new password"
              required
            />
            <div className="absolute left-3.5 top-1/2 transform -translate-y-1/2 p-1.5 rounded-lg transition-colors duration-300"
              style={{ backgroundColor: colors.hover }}>
              <Key className="h-4 w-4" style={{ color: colors.textSecondary }} />
            </div>
            <button
              type="button"
              onClick={() => setShowNewPassword(!showNewPassword)}
              className="absolute cursor right-3.5 top-1/2 transform -translate-y-1/2 p-2 rounded-lg transition-all duration-300 hover-lift hover:scale-110 active:scale-95"
              style={{ 
                backgroundColor: colors.hover,
                border: `1px solid ${colors.border}`,
                color: colors.textSecondary
              }}
              aria-label={showNewPassword ? "Hide password" : "Show password"}
            >
              {showNewPassword ? (
                <EyeOff className="h-4 w-4" style={{ color: colors.primary }} />
              ) : (
                <Eye className="h-4 w-4" style={{ color: colors.textSecondary }} />
              )}
            </button>
          </div>
        </div>

        {/* Confirm Password */}
        <div>
          <label className="block text-sm font-semibold mb-2" style={{ color: colors.text }}>
            Confirm New Password
          </label>
          <div className="relative group">
            <input
              type={showConfirmPassword ? "text" : "password"}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="w-full pl-11 pr-11 py-3 rounded-xl border relative z-10 transition-all duration-300 outline-none hover-lift"
              style={{ 
                backgroundColor: colors.inputBg,
                borderColor: !passwordsMatch && confirmPassword.length > 0 
                  ? colors.error 
                  : colors.inputBorder,
                color: colors.text
              }}
              placeholder="Confirm new password"
              required
            />
            <div className="absolute left-3.5 top-1/2 transform -translate-y-1/2 p-1.5 rounded-lg transition-colors duration-300"
              style={{ 
                backgroundColor: colors.hover,
              }}>
              <Lock className="h-4 w-4" style={{ 
                color: !passwordsMatch && confirmPassword.length > 0 
                  ? colors.error 
                  : colors.textSecondary 
              }} />
            </div>
            <button
              type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              className="absolute cursor right-3.5 top-1/2 transform -translate-y-1/2 p-2 rounded-lg transition-all duration-300 hover-lift hover:scale-110 active:scale-95"
              style={{ 
                backgroundColor: colors.hover,
                border: `1px solid ${colors.border}`,
                color: colors.textSecondary
              }}
              aria-label={showConfirmPassword ? "Hide password" : "Show password"}
            >
              {showConfirmPassword ? (
                <EyeOff className="h-4 w-4" style={{ color: colors.primary }} />
              ) : (
                <Eye className="h-4 w-4" style={{ color: colors.textSecondary }} />
              )}
            </button>
          </div>
          {!passwordsMatch && confirmPassword.length > 0 && (
            <p className="text-xs mt-2 flex items-center gap-1.5" style={{ color: colors.error }}>
              <XCircle className="h-3 w-3" />
              Passwords do not match
            </p>
          )}
        </div>

        {/* Password Requirements */}
        <div className="p-4 rounded-xl border" 
          style={{ 
            backgroundColor: colors.hover,
            borderColor: colors.border
          }}>
          <p className="text-sm font-medium mb-3 flex items-center gap-2" style={{ color: colors.text }}>
            <Info className="h-4 w-4" style={{ color: colors.primary }} />
            Password Requirements
          </p>
          <ul className="space-y-2.5">
            {[
              { label: 'At least 12 characters', met: requirements.minLength },
              { label: 'At least one uppercase letter', met: requirements.hasUpperCase },
              { label: 'At least one lowercase letter', met: requirements.hasLowerCase },
              { label: 'At least one number', met: requirements.hasNumber },
              { label: 'At least one special character', met: requirements.hasSpecialChar },
              { label: 'Passwords match', met: requirements.passwordsMatch },
            ].map((req, index) => (
              <li key={index} className="flex items-center gap-3">
                {req.met ? (
                  <CheckCircle className="h-3.5 w-3.5 flex-shrink-0" style={{ color: colors.success }} />
                ) : (
                  <div className="h-3.5 w-3.5 flex-shrink-0 rounded-full border" 
                    style={{ 
                      borderColor: colors.textTertiary,
                      backgroundColor: 'transparent'
                    }} 
                  />
                )}
                <span className="text-sm" style={{ 
                  color: req.met ? colors.success : colors.textSecondary 
                }}>
                  {req.label}
                </span>
              </li>
            ))}
          </ul>
        </div>

        {/* Buttons */}
        <div className="flex gap-3 pt-2">
          <Button
            type="button"
            onClick={onBackFromReset}
            className="flex-1 py-3.5 rounded-xl font-medium transition-all duration-300 hover-lift"
            style={{ 
              backgroundColor: colors.hover,
              border: `1px solid ${colors.border}`,
              color: colors.text,
              opacity: loading ? 0.5 : 1
            }}
            disabled={loading}
          >
            <div className="flex items-center justify-center gap-2">
              {loading ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <ArrowLeft className="h-4 w-4" />
              )}
              Back
            </div>
          </Button>
          <Button
            type="submit"
            className="flex-1 py-3.5 bg-gradient-to-r from-blue-500 via-violet-500 to-blue-500 rounded-xl font-semibold transition-all duration-300 relative group overflow-hidden hover-lift"
            style={{ 
              opacity: (!allRequirementsMet) ? 0.5 : 1
            }}
            disabled={loading || !allRequirementsMet}
          >
            <div className="absolute inset-0 bg-white/20 transform -translate-x-full group-hover:translate-x-full transition-transform duration-700"></div>
            {loading ? (
              <div className="flex items-center justify-center gap-3 relative z-10">
                <div className="h-4 w-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                <span>Updating...</span>
              </div>
            ) : (
              <div className="flex items-center justify-center gap-3 relative z-10">
                <Shield className="h-4 w-4" />
                <span>Update Password</span>
                <Check className="h-4 w-4 group-hover:scale-110 transition-transform" />
              </div>
            )}
          </Button>
        </div>
      </form>
    </div>
  );
};

export default PasswordResetForm;