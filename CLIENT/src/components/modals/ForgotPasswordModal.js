// components/auth/ForgotPasswordModal.js
import React, { useState } from "react";
import { showSuccess, showError, showConfirm } from "@/lib/sweetAlert";
import { Button } from "@/components/ui/button";
import { 
  Key, 
  Loader2, 
  Zap, 
  User, 
  Shield,
  X,
  AlertCircle
} from "lucide-react";

const ForgotPasswordModal = ({ 
  showForgotPassword, 
  state, 
  colors, 
  isMobile, 
  isDark,
  onForgotPassword
}) => {
  const { forgotUserId, forgotLoading } = state;
  const { handleForgotPassword, setShowForgotPassword, setForgotUserId } = onForgotPassword;
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!forgotUserId.trim()) {
      showError('User ID Required', 'Please enter your user ID to reset password');
      return;
    }

    const confirm = await showConfirm(
      'Confirm Password Reset',
      `Are you sure you want to reset the password for user "${forgotUserId}"? A new password will be sent via SMS.`,
      'Reset Password',
      {
        showCancelButton: true,
        confirmButtonText: 'Reset Password',
        cancelButtonText: 'Cancel',
        reverseButtons: true,
        icon: 'warning',
        customClass: {
          confirmButton: 'px-4 py-2.5 rounded-lg font-medium bg-orange-600 hover:bg-orange-700 text-white',
          cancelButton: 'px-4 py-2.5 rounded-lg font-medium border border-gray-300 bg-white text-gray-700 hover:bg-gray-50'
        }
      }
    );

    if (!confirm.isConfirmed) return;

    setLoading(true);
    try {
      const success = await handleForgotPassword(e);
      
      if (success) {
        showSuccess(
          'Password Reset Initiated', 
          `Password reset request has been sent for user "${forgotUserId}". Check your SMS for the new password.`,
          {
            timer: 5000,
            showConfirmButton: true,
            confirmButtonText: 'Got it'
          }
        );
        setShowForgotPassword(false);
        setForgotUserId('');
      } else {
        throw new Error('Password reset failed');
      }
    } catch (error) {
      console.error('Password reset error:', error);
      
      showError(
        'Reset Failed', 
        error.message || 'Failed to reset password. Please try again later.',
        {
          timer: 6000,
          showConfirmButton: true,
          confirmButtonText: 'Try Again'
        }
      );
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setForgotUserId('');
    setShowForgotPassword(false);
  };

  return (
    <>
      {showForgotPassword && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className={`w-full max-w-md ${isDark ? 'bg-gray-900' : 'bg-white'} rounded-lg border ${isDark ? 'border-gray-800' : 'border-gray-200'} shadow-xl`}>
            {/* Modal Header */}
            <div className={`px-6 py-4 border-b ${isDark ? 'border-gray-800' : 'border-gray-200'} flex items-center justify-between`}>
              <div className="flex items-center gap-3">
                <div className={`p-2 rounded-lg ${isDark ? 'bg-gray-800' : 'bg-gray-100'}`}>
                  <Shield className={`h-5 w-5 ${isDark ? 'text-orange-500' : 'text-orange-600'}`} />
                </div>
                <div>
                  <h3 className={`text-lg font-semibold ${isDark ? 'text-white' : 'text-gray-900'}`}>
                    Forgot Password
                  </h3>
                  <p className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-600'}`}>
                    Reset your account password
                  </p>
                </div>
              </div>
              <button
                onClick={handleClose}
                className={`p-2 rounded-lg ${isDark ? 'hover:bg-gray-800 text-gray-400' : 'hover:bg-gray-100 text-gray-500'}`}
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            {/* Modal Content */}
            <div className="p-6">
              <form onSubmit={handleSubmit} className="space-y-4">
                {/* User ID Input */}
                <div>
                  <label className={`block text-sm font-medium mb-2 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                    User ID <span className="text-red-500">*</span>
                  </label>
                  <div className="relative">
                    <input
                      type="text"
                      value={forgotUserId}
                      onChange={(e) => setForgotUserId(e.target.value)}
                      className={`w-full pl-10 pr-4 py-2.5 rounded-lg border ${
                        isDark 
                          ? 'bg-gray-800 border-gray-700 text-white placeholder-gray-500 focus:border-orange-500 focus:ring-1 focus:ring-orange-500/20' 
                          : 'bg-white border-gray-300 text-gray-900 placeholder-gray-400 focus:border-orange-500 focus:ring-1 focus:ring-orange-500/10'
                      } transition-colors outline-none`}
                      placeholder="Enter your user ID"
                      required
                      disabled={loading || forgotLoading}
                    />
                    <User className={`absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 ${isDark ? 'text-gray-500' : 'text-gray-400'}`} />
                  </div>
                </div>

                {/* Information Box */}
                <div className={`p-3 rounded-lg ${isDark ? 'bg-blue-900/20 border-blue-800' : 'bg-blue-50 border-blue-200'} border`}>
                  <div className="flex items-start gap-2">
                    <AlertCircle className={`h-4 w-4 flex-shrink-0 mt-0.5 ${isDark ? 'text-blue-400' : 'text-blue-600'}`} />
                    <div>
                      <p className={`text-sm font-medium ${isDark ? 'text-blue-300' : 'text-blue-700'}`}>
                        What happens next?
                      </p>
                      <p className={`text-xs mt-1 ${isDark ? 'text-blue-400/80' : 'text-blue-600/80'}`}>
                        A new password will be sent to the mobile number registered with this account. You'll need to change it after logging in.
                      </p>
                    </div>
                  </div>
                </div>

                {/* Buttons */}
                <div className="flex gap-3 pt-2">
                  <Button
                    type="button"
                    onClick={handleClose}
                    className={`flex-1 py-2.5 rounded-lg font-medium ${
                      isDark 
                        ? 'bg-gray-800 hover:bg-gray-700 text-gray-300 border-gray-700' 
                        : 'bg-gray-100 hover:bg-gray-200 text-gray-700 border-gray-300'
                    } border`}
                    disabled={loading || forgotLoading}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    className={`flex-1 py-2.5 rounded-lg font-medium transition-colors ${
                      isDark 
                        ? 'bg-orange-600 hover:bg-orange-700 text-white' 
                        : 'bg-orange-600 hover:bg-orange-700 text-white'
                    } ${!forgotUserId.trim() ? 'opacity-50 cursor-not-allowed' : ''}`}
                    disabled={loading || forgotLoading || !forgotUserId.trim()}
                  >
                    {loading || forgotLoading ? (
                      <div className="flex items-center justify-center gap-2">
                        <div className={`h-3.5 w-3.5 border-2 ${isDark ? 'border-white/30 border-t-white' : 'border-white/30 border-t-white'} rounded-full animate-spin`} />
                        Processing...
                      </div>
                    ) : (
                      <div className="flex items-center justify-center gap-2">
                        Reset Password
                        <Zap className="h-3.5 w-3.5" />
                      </div>
                    )}
                  </Button>
                </div>
              </form>

              {/* Security Note */}
              <div className={`mt-6 pt-4 border-t ${isDark ? 'border-gray-800' : 'border-gray-200'}`}>
                <div className="flex items-start gap-2">
                  <Key className={`h-4 w-4 flex-shrink-0 mt-0.5 ${isDark ? 'text-orange-500' : 'text-orange-600'}`} />
                  <div>
                    <p className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-600'}`}>
                      For security reasons, the temporary password will expire in 24 hours. Always use strong, unique passwords and enable multi-factor authentication when available.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default ForgotPasswordModal;