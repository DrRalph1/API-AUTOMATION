import React from "react";
import { showSuccess, showError, showConfirm } from "@/lib/sweetAlert";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";
import { 
  Key, Lock, CheckCircle, XCircle, Eye, EyeOff,
  AlertTriangle, Shield, User, Zap, ArrowDown
} from "lucide-react";
import { cn } from "@/lib/utils";
import { useState } from "react";

export default function ResetPasswordModal({
  showResetPasswordDialog,
  setShowResetPasswordDialog,
  resetPasswordForm,
  setResetPasswordForm,
  handleResetPassword
}) {
  const [showNewPassword, setShowNewPassword] = React.useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = React.useState(false);
  
  // Mobile accordion state
  const [expandedSections, setExpandedSections] = useState({
    securityWarning: true,
    passwordInputs: true,
    requirements: false,
    userImpact: false
  });

  const toggleSection = (section) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  // Password strength checker
  const checkPasswordStrength = (password) => {
    if (!password) return { score: 0, text: 'Empty', color: 'text-gray-500' };
    
    let score = 0;
    if (password.length >= 8) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;
    
    const strength = [
      { text: 'Very Weak', color: 'text-red-500 dark:text-red-400' },
      { text: 'Weak', color: 'text-red-400 dark:text-red-300' },
      { text: 'Fair', color: 'text-amber-500 dark:text-amber-400' },
      { text: 'Good', color: 'text-blue-500 dark:text-blue-400' },
      { text: 'Strong', color: 'text-emerald-500 dark:text-emerald-400' }
    ][score];
    
    return { score, ...strength };
  };

  const strength = checkPasswordStrength(resetPasswordForm.newPassword);
  const passwordsMatch = resetPasswordForm.newPassword === resetPasswordForm.confirmPassword;
  
  // Password requirements checklist
  const requirements = [
    { 
      label: 'At least 8 characters', 
      met: resetPasswordForm.newPassword?.length >= 8 
    },
    { 
      label: 'Contains uppercase letter', 
      met: /[A-Z]/.test(resetPasswordForm.newPassword || '') 
    },
    { 
      label: 'Contains lowercase letter', 
      met: /[a-z]/.test(resetPasswordForm.newPassword || '') 
    },
    { 
      label: 'Contains number', 
      met: /[0-9]/.test(resetPasswordForm.newPassword || '') 
    },
    { 
      label: 'Contains special character', 
      met: /[^A-Za-z0-9]/.test(resetPasswordForm.newPassword || '') 
    },
  ];

  const canSubmit = resetPasswordForm.newPassword && 
                   resetPasswordForm.confirmPassword && 
                   passwordsMatch && 
                   strength.score >= 3;

  // Mobile-responsive render function for sections
  const renderMobileSection = (sectionKey, title, icon, children) => (
    <div className="space-y-3 md:space-y-4">
      <button
        onClick={() => toggleSection(sectionKey)}
        className="flex items-center justify-between w-full p-3 rounded-lg bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors md:cursor-default"
      >
        <div className="flex items-center gap-2">
          {icon}
          <Label className="text-sm font-semibold text-gray-900 dark:text-gray-100">
            {title}
          </Label>
        </div>
        <ArrowDown className={`h-4 w-4 text-gray-500 transition-transform md:hidden ${
          expandedSections[sectionKey] ? 'rotate-180' : ''
        }`} />
      </button>
      
      <div className={`md:block ${
        expandedSections[sectionKey] ? 'block' : 'hidden'
      }`}>
        {children}
      </div>
    </div>
  );

  return (
    <Dialog open={showResetPasswordDialog} onOpenChange={setShowResetPasswordDialog}>
      <DialogContent className="max-w-lg max-h-[85vh] overflow-y-auto bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl shadow-2xl sm:max-w-[95vw] sm:rounded-lg md:max-w-lg md:rounded-xl">
        <DialogHeader className="space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <DialogTitle className="text-xl font-bold text-gray-900 dark:text-gray-100 sm:text-lg md:text-xl">
              Reset Password
            </DialogTitle>
            {/* <Badge 
              variant="outline" 
              className="text-xs font-medium bg-gradient-to-r from-purple-500/10 to-pink-500/10 text-purple-700 dark:text-purple-300 border-purple-200 dark:border-purple-800/30 self-start sm:self-center"
            >
              Security Action
            </Badge> */}
          </div>
          <DialogDescription className="text-sm text-gray-500 dark:text-gray-400 sm:text-xs md:text-sm">
            Reset password for user account. Ensure the new password meets security requirements.
          </DialogDescription>
        </DialogHeader>
        
        <Separator className="bg-gray-200 dark:bg-gray-700" />
        
        <div className="space-y-6 py-2 sm:space-y-4 md:space-y-6">
          {/* Security Warning - Mobile Accordion */}
          {renderMobileSection(
            'securityWarning',
            'Security Notice',
            <AlertTriangle className="h-4 w-4 text-amber-600 dark:text-amber-400" />,
            <div className="p-4 rounded-lg bg-gradient-to-r from-amber-50 to-amber-100 dark:from-amber-900/10 dark:to-amber-800/10 border border-amber-200 dark:border-amber-800/30 sm:p-3 md:p-4">
              <div className="flex items-start gap-3">
                <AlertTriangle className="h-5 w-5 text-amber-600 dark:text-amber-400 mt-0.5 sm:h-4 sm:w-4 md:h-5 md:w-5" />
                <div className="space-y-1">
                  <p className="text-sm font-semibold text-amber-800 dark:text-amber-300 sm:text-xs md:text-sm">
                    Security Notice
                  </p>
                  <p className="text-xs text-amber-700/80 dark:text-amber-400/80 sm:text-[10px] md:text-xs">
                    This action will immediately invalidate the current password and require the user to log in with the new password.
                  </p>
                </div>
              </div>
            </div>
          )}

          {/* Password Inputs - Mobile Accordion */}
          {renderMobileSection(
            'passwordInputs',
            'Password Fields',
            <Lock className="h-4 w-4 text-gray-600 dark:text-gray-400" />,
            <div className="grid grid-cols-1 gap-6 sm:gap-4 md:gap-6">
              {/* New Password Field */}
              <div className="space-y-3">
                <Label className="text-sm font-semibold text-gray-900 dark:text-gray-100 sm:text-xs md:text-sm">
                  <div className="flex items-center gap-2">
                    <Lock className="h-4 w-4 sm:h-3.5 sm:w-3.5 md:h-4 md:w-4" />
                    New Password
                  </div>
                </Label>
                <div className="relative">
                  <Input
                    type={showNewPassword ? "text" : "password"}
                    value={resetPasswordForm.newPassword}
                    onChange={(e) => setResetPasswordForm({...resetPasswordForm, newPassword: e.target.value})}
                    placeholder="Enter new secure password"
                    className={cn(
                      "pr-10 bg-gray-50 dark:bg-gray-800/50 border-gray-300 dark:border-gray-700 sm:text-sm md:text-base",
                      resetPasswordForm.newPassword && "border-blue-300 dark:border-blue-700"
                    )}
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="absolute right-2 top-1/2 -translate-y-1/2 h-8 w-8 p-0 sm:h-7 sm:w-7 md:h-8 md:w-8"
                    onClick={() => setShowNewPassword(!showNewPassword)}
                  >
                    {showNewPassword ? (
                      <EyeOff className="h-4 w-4 text-gray-500 sm:h-3.5 sm:w-3.5 md:h-4 md:w-4" />
                    ) : (
                      <Eye className="h-4 w-4 text-gray-500 sm:h-3.5 sm:w-3.5 md:h-4 md:w-4" />
                    )}
                  </Button>
                </div>
                
                {/* Password Strength Indicator */}
                {resetPasswordForm.newPassword && (
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-medium text-gray-600 dark:text-gray-400 sm:text-[10px] md:text-xs">
                        Password Strength
                      </span>
                      <span className={cn("text-xs font-semibold", strength.color, "sm:text-[10px] md:text-xs")}>
                        {strength.text}
                      </span>
                    </div>
                    <div className="h-1.5 w-full bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                      <div 
                        className={cn(
                          "h-full transition-all duration-300",
                          strength.score <= 1 && "bg-red-500",
                          strength.score === 2 && "bg-amber-500",
                          strength.score === 3 && "bg-blue-500",
                          strength.score >= 4 && "bg-emerald-500"
                        )}
                        style={{ width: `${(strength.score / 4) * 100}%` }}
                      />
                    </div>
                  </div>
                )}
              </div>

              {/* Confirm Password Field */}
              <div className="space-y-3">
                <Label className="text-sm font-semibold text-gray-900 dark:text-gray-100 sm:text-xs md:text-sm">
                  <div className="flex items-center gap-2">
                    <Shield className="h-4 w-4 sm:h-3.5 sm:w-3.5 md:h-4 md:w-4" />
                    Confirm Password
                  </div>
                </Label>
                <div className="relative">
                  <Input
                    type={showConfirmPassword ? "text" : "password"}
                    value={resetPasswordForm.confirmPassword}
                    onChange={(e) => setResetPasswordForm({...resetPasswordForm, confirmPassword: e.target.value})}
                    placeholder="Confirm new password"
                    className={cn(
                      "pr-10 bg-gray-50 dark:bg-gray-800/50 border-gray-300 dark:border-gray-700 sm:text-sm md:text-base",
                      resetPasswordForm.confirmPassword && !passwordsMatch && "border-red-300 dark:border-red-700",
                      resetPasswordForm.confirmPassword && passwordsMatch && "border-emerald-300 dark:border-emerald-700"
                    )}
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="absolute right-2 top-1/2 -translate-y-1/2 h-8 w-8 p-0 sm:h-7 sm:w-7 md:h-8 md:w-8"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  >
                    {showConfirmPassword ? (
                      <EyeOff className="h-4 w-4 text-gray-500 sm:h-3.5 sm:w-3.5 md:h-4 md:w-4" />
                    ) : (
                      <Eye className="h-4 w-4 text-gray-500 sm:h-3.5 sm:w-3.5 md:h-4 md:w-4" />
                    )}
                  </Button>
                </div>
                
                {/* Password Match Indicator */}
                {resetPasswordForm.confirmPassword && (
                  <div className="flex items-center gap-2">
                    {passwordsMatch ? (
                      <>
                        <CheckCircle className="h-4 w-4 text-emerald-500 sm:h-3.5 sm:w-3.5 md:h-4 md:w-4" />
                        <span className="text-xs text-emerald-600 dark:text-emerald-400 font-medium sm:text-[10px] md:text-xs">
                          Passwords match
                        </span>
                      </>
                    ) : (
                      <>
                        <XCircle className="h-4 w-4 text-red-500 sm:h-3.5 sm:w-3.5 md:h-4 md:w-4" />
                        <span className="text-xs text-red-600 dark:text-red-400 font-medium sm:text-[10px] md:text-xs">
                          Passwords do not match
                        </span>
                      </>
                    )}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Password Requirements - Mobile Accordion */}
          {renderMobileSection(
            'requirements',
            'Password Requirements',
            <Zap className="h-4 w-4 text-gray-600 dark:text-gray-400" />,
            <div className="p-4 rounded-lg bg-gray-50 dark:bg-gray-800/30 border border-gray-200 dark:border-gray-700 sm:p-3 md:p-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-2 md:gap-3">
                {requirements.map((req, index) => (
                  <div key={index} className="flex items-center gap-3">
                    <div className={cn(
                      "h-5 w-5 rounded-full flex items-center justify-center sm:h-4 sm:w-4 md:h-5 md:w-5",
                      req.met 
                        ? "bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400"
                        : "bg-gray-100 dark:bg-gray-800 text-gray-400 dark:text-gray-500"
                    )}>
                      {req.met ? (
                        <CheckCircle className="h-3 w-3 sm:h-2.5 sm:w-2.5 md:h-3 md:w-3" />
                      ) : (
                        <div className="h-1.5 w-1.5 rounded-full bg-current" />
                      )}
                    </div>
                    <span className={cn(
                      "text-sm sm:text-xs md:text-sm",
                      req.met 
                        ? "text-emerald-700 dark:text-emerald-300"
                        : "text-gray-600 dark:text-gray-400"
                    )}>
                      {req.label}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* User Impact - Mobile Accordion */}
          {renderMobileSection(
            'userImpact',
            'User Impact',
            <User className="h-4 w-4 text-blue-600 dark:text-blue-400" />,
            <div className="p-4 rounded-lg bg-gradient-to-r from-blue-50 to-blue-100 dark:from-blue-900/10 dark:to-blue-800/10 border border-blue-200 dark:border-blue-800/30 sm:p-3 md:p-4">
              <div className="flex items-start gap-3">
                <User className="h-5 w-5 text-blue-600 dark:text-blue-400 mt-0.5 sm:h-4 sm:w-4 md:h-5 md:w-5" />
                <div className="space-y-1">
                  <p className="text-sm font-semibold text-blue-800 dark:text-blue-300 sm:text-xs md:text-sm">
                    User Impact
                  </p>
                  <ul className="text-xs text-blue-700/80 dark:text-blue-400/80 space-y-1 list-disc list-inside sm:text-[10px] md:text-xs">
                    <li>User will be logged out of all active sessions</li>
                    <li>Password history will be updated</li>
                    <li>User will need to log in with the new password immediately</li>
                    <li>Email notification can be sent (optional)</li>
                  </ul>
                </div>
              </div>
            </div>
          )}
        </div>
        
        <DialogFooter className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-4 border-t border-gray-200 dark:border-gray-800 sm:gap-2">
          <div className="flex items-center justify-center sm:justify-start w-full sm:w-auto">
            <DialogClose asChild>
              <Button 
                variant="outline"
                className="rounded-lg border-gray-300 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800 flex-1 sm:flex-none"
              >
                Cancel
              </Button>
            </DialogClose>
          </div>
          <Button
            onClick={handleResetPassword}
            disabled={!canSubmit}
            className={cn(
              "rounded-lg transition-all duration-300 flex-1 sm:flex-none",
              canSubmit
                ? "bg-gradient-to-r from-purple-600 to-pink-500 hover:from-purple-700 hover:to-pink-600 shadow-lg shadow-purple-500/25"
                : "bg-gradient-to-r from-gray-400 to-gray-500 cursor-not-allowed opacity-60"
            )}
          >
            <Key className="h-4 w-4 mr-2 sm:mr-1 md:mr-2 sm:h-3.5 sm:w-3.5 md:h-4 md:w-4" />
            <span className="sm:hidden md:inline">Reset Password</span>
            <span className="hidden sm:inline md:hidden">Reset</span>
            {canSubmit && (
              <Shield className="h-3.5 w-3.5 ml-2 opacity-80 sm:h-3 sm:w-3 md:h-3.5 md:w-3.5" />
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}