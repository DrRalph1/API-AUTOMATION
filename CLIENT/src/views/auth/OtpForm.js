// components/auth/OtpForm.js
import React from "react";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { 
  Key, 
  Verified, 
  Loader2,
  RotateCcw, 
  ArrowLeft,
  Shield,
  Clock,
  CheckCircle,
  AlertCircle,
  Lock
} from "lucide-react";

const OtpForm = ({ state, inputsRef, colors, isMobile, isDark, onOtpActions }) => {
  const {
    otp,
    loading,
    resendCountdown,
    canResendOtp,
    loginData
  } = state;

  const {
    handleOtpChange,
    handleOtpKeyDown,
    handleResendOtp,
    verifyOtp,
    handleBackFromOtp,
    setOtp
  } = onOtpActions || {};

  const handlePaste = (e) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text').trim();
    if (/^\d{6}$/.test(pastedData)) {
      const digits = pastedData.split('');
      const newOtp = [...otp];
      digits.forEach((digit, index) => {
        if (index < 6) newOtp[index] = digit;
      });
      if (setOtp) {
        setOtp(newOtp);
      }
      
      setTimeout(() => {
        const lastInput = inputsRef.current[Math.min(5, digits.length - 1)];
        if (lastInput) {
          lastInput.focus();
          lastInput.select();
        }
      }, 10);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="text-center">
        <div className="inline-flex items-center justify-center w-14 h-14 rounded-xl border gradient-bg mb-4 hover-lift"
          style={{ 
            borderColor: colors.border,
            backgroundColor: colors.card 
          }}>
          <Shield className="h-7 w-7" style={{ color: colors.primary }} />
        </div>
        <h3 className="text-xl font-bold mb-2" style={{ color: colors.text }}>
          Multi-Factor Authentication
        </h3>
        <p className="text-sm max-w-xs mx-auto" style={{ color: colors.textSecondary }}>
          Enter the 6-digit verification code sent to your device
        </p>
      </div>

      {/* OTP Inputs */}
      <form onSubmit={verifyOtp} className="space-y-6">
        <div className="px-4">
          <div className="flex justify-between max-w-xs sm:max-w-sm md:max-w-md mx-auto gap-1.5 sm:gap-2 md:gap-3">
            {otp.map((digit, idx) => (
              <div key={idx} className="relative flex-1">
                <input
                  ref={(el) => {
                    inputsRef.current[idx] = el;
                  }}
                  type="text"
                  inputMode="numeric"
                  pattern="[0-9]*"
                  maxLength={1}
                  value={digit}
                  data-index={idx}
                  onChange={(e) => handleOtpChange(e.target.value, idx)}
                  onKeyDown={(e) => handleOtpKeyDown(e, idx)}
                  onFocus={(e) => {
                    setTimeout(() => e.target.select(), 10);
                  }}
                  onPaste={handlePaste}
                  className="w-full aspect-square max-w-[50px] sm:max-w-[60px] md:max-w-[70px] text-center text-lg sm:text-xl font-semibold rounded-lg sm:rounded-xl border transition-all duration-300 outline-none hover-lift focus:ring-2"
                  style={{ 
                    backgroundColor: colors.inputBg,
                    borderColor: colors.inputBorder,
                    color: colors.text,
                    borderWidth: '2px',
                    borderStyle: 'solid'
                  }}
                  autoComplete="one-time-code"
                  autoFocus={idx === 0}
                />
                {idx < 5 && (
                  <div className="absolute -right-1 sm:-right-1.5 md:-right-2 top-1/2 transform -translate-y-1/2 h-px w-2 sm:w-3 md:w-4" 
                    style={{ backgroundColor: colors.border }} />
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Timer and Resend */}
        <div className="text-center">
          {resendCountdown > 0 ? (
            <div className="flex items-center justify-center gap-2" style={{ color: colors.textSecondary }}>
              <Clock className="h-4 w-4" style={{ color: colors.textTertiary }} />
              <span className="text-sm">
                Resend code in <span className="font-medium" style={{ color: colors.primary }}>
                  {resendCountdown}s
                </span>
              </span>
            </div>
          ) : (
            <button
              type="button"
              onClick={handleResendOtp}
              className="text-sm flex items-center gap-2 mx-auto transition-colors hover-lift"
              style={{ color: colors.primary }}
            >
              <RotateCcw className="h-4 w-4" />
              Resend verification code
            </button>
          )}
        </div>

        {/* Submit Button */}
        <Button
          type="submit"
          className="w-full py-3.5 rounded-xl font-semibold transition-all duration-300 relative group overflow-hidden hover-lift"
          style={{ 
            backgroundColor: colors.primary,
            color: 'white'
          }}
          disabled={loading}
        >
          <div className="absolute inset-0 bg-white/20 transform -translate-x-full group-hover:translate-x-full transition-transform duration-700"></div>
          {loading ? (
            <div className="flex items-center justify-center gap-3 relative z-10">
              <div className="h-4 w-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              <span>Verifying...</span>
            </div>
          ) : (
            <div className="flex items-center justify-center gap-3 relative z-10">
              <Lock className="h-4 w-4" />
              <span>Verify & Continue</span>
              <Verified className="h-4 w-4 group-hover:scale-110 transition-transform" />
            </div>
          )}
        </Button>

        {/* Back Button */}
        <div className="text-center">
          <button
            type="button"
            onClick={handleBackFromOtp}
            className="text-sm flex items-center gap-2 mx-auto transition-colors hover-lift py-2 px-3 rounded-lg"
            style={{ 
              color: colors.textSecondary,
              backgroundColor: colors.hover
            }}
          >
            <ArrowLeft className="h-3.5 w-3.5" />
            Back to login
          </button>
        </div>
      </form>

      {/* Security Note */}
      <div className="pt-4 border-t hidden sm:block" style={{ borderColor: colors.border }}>
        <div className="flex items-start gap-3">
          <div className="p-1.5 rounded-lg" style={{ backgroundColor: colors.hover }}>
            <Key className="h-4 w-4" style={{ color: colors.primary }} />
          </div>
          <div>
            <p className="text-xs font-medium mb-1" style={{ color: colors.text }}>
              Security Information
            </p>
            <p className="text-xs" style={{ color: colors.textSecondary }}>
              This verification code expires in 5 minutes. Never share your code with anyone.
            </p>
          </div>
        </div>
      </div>

      {/* Status Indicators */}
      <div className="grid grid-cols-3 gap-2 pt-2">
        <div className="flex items-center gap-2">
          <div className="w-2 h-2 rounded-full" style={{ backgroundColor: colors.success }}></div>
          <span className="text-xs" style={{ color: colors.textSecondary }}>Encrypted</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-2 h-2 rounded-full" style={{ backgroundColor: colors.success }}></div>
          <span className="text-xs" style={{ color: colors.textSecondary }}>Secure</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-2 h-2 rounded-full" style={{ backgroundColor: colors.warning }}></div>
          <span className="text-xs" style={{ color: colors.textSecondary }}>Time-bound</span>
        </div>
      </div>
    </div>
  );
};

export default OtpForm;