// hooks/useLogin.js
import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { useSetRecoilState, useRecoilValue } from 'recoil';
import { tokenAtom } from '@/recoil/tokenAtom';
import { roleAtom } from '@/recoil/roleAtom';
import { 
  login as apiLogin,
  resetDefaultPassword,
  findUser,
  forgotPassword 
} from "../controllers/AuthController.js";
import {
  verifyOTP,
  sendOTP
} from "../controllers/OTPController.js";
import { showError, showSuccess, showWarning } from "@/lib/sweetAlert";

export const useLogin = () => {
  const { loginWithUserData, user } = useAuth();
  const navigate = useNavigate();
  
  const token = useRecoilValue(tokenAtom);
  const setToken = useSetRecoilState(tokenAtom);
  const role = useRecoilValue(roleAtom);
  const setRole = useSetRecoilState(roleAtom);

  const [state, setState] = useState({
    userId: "",
    password: "",
    showPassword: false,
    loading: false,
    showOtp: false,
    otp: ["", "", "", "", "", ""],
    loginData: null,
    userType: "teller",
    portalValidationError: "",
    lastAttemptedRole: "",
    resendCountdown: 0,
    canResendOtp: false,
    requiresPasswordReset: false,
    newPassword: "",
    confirmPassword: "",
    showNewPassword: false,
    showConfirmPassword: false,
    showForgotPassword: false,
    forgotUserId: "",
    forgotLoading: false,
    isMobile: false
  });

  const inputsRef = useRef([]);

  // State setters
  const setUserId = (value) => setState(prev => ({ ...prev, userId: value }));
  const setPassword = (value) => setState(prev => ({ ...prev, password: value }));
  const setShowPassword = (value) => setState(prev => ({ ...prev, showPassword: value }));
  const setLoading = (value) => setState(prev => ({ ...prev, loading: value }));
  const setShowOtp = (value) => setState(prev => ({ ...prev, showOtp: value }));
  const setOtp = (value) => setState(prev => ({ ...prev, otp: value }));
  const setLoginData = (value) => setState(prev => ({ ...prev, loginData: value }));
  const setUserType = (value) => setState(prev => ({ ...prev, userType: value }));
  const setPortalValidationError = (value) => setState(prev => ({ ...prev, portalValidationError: value }));
  const setLastAttemptedRole = (value) => setState(prev => ({ ...prev, lastAttemptedRole: value }));
  const setResendCountdown = (value) => setState(prev => ({ ...prev, resendCountdown: value }));
  const setCanResendOtp = (value) => setState(prev => ({ ...prev, canResendOtp: value }));
  const setRequiresPasswordReset = (value) => setState(prev => ({ ...prev, requiresPasswordReset: value }));
  const setNewPassword = (value) => setState(prev => ({ ...prev, newPassword: value }));
  const setConfirmPassword = (value) => setState(prev => ({ ...prev, confirmPassword: value }));
  const setShowNewPassword = (value) => setState(prev => ({ ...prev, showNewPassword: value }));
  const setShowConfirmPassword = (value) => setState(prev => ({ ...prev, showConfirmPassword: value }));
  const setShowForgotPassword = (value) => setState(prev => ({ ...prev, showForgotPassword: value }));
  const setForgotUserId = (value) => setState(prev => ({ ...prev, forgotUserId: value }));
  const setForgotLoading = (value) => setState(prev => ({ ...prev, forgotLoading: value }));
  const setIsMobile = (value) => setState(prev => ({ ...prev, isMobile: value }));

  // Helper function to get user role for alerts
  const getUserRoleForAlert = () => {
    if (state.showOtp && state.loginData?.role) {
      const role = state.loginData.role.toLowerCase();
      return role === "oracle" ? "oracle" : "teller";
    }
    return state.userType;
  };

  // Alert functions
  const showCustomError = (title, message) => {
    const userRole = getUserRoleForAlert();
    showError(title, message, userRole, {
      width: state.isMobile ? '90%' : 600,
      padding: state.isMobile ? '1rem' : '2rem',
      showConfirmButton: true,
      showCancelButton: false,
      showLoaderOnConfirm: false,
      allowOutsideClick: false,
      allowEscapeKey: false
    });
  };

  const showCustomSuccess = (title, message) => {
    const userRole = getUserRoleForAlert();
    showSuccess(title, message, userRole, {
      width: state.isMobile ? '90%' : 600,
      padding: state.isMobile ? '1rem' : '2rem',
      showConfirmButton: true,
      showCancelButton: false,
      showLoaderOnConfirm: false,
      allowOutsideClick: false,
      allowEscapeKey: false
    });
  };

  const showCustomWarning = (title, message) => {
    const userRole = getUserRoleForAlert();
    showWarning(title, message, userRole, {
      width: state.isMobile ? '90%' : 600,
      padding: state.isMobile ? '1rem' : '2rem',
      showConfirmButton: true,
      showCancelButton: false,
      showLoaderOnConfirm: false,
      allowOutsideClick: false,
      allowEscapeKey: false
    });
  };

  // Portal validation
  const validatePortalAccess = (userRole, selectedPortal) => {
  console.log(`🔍 Portal Validation: User Role = "${userRole}", Selected Portal = "${selectedPortal}"`);
  
  setLastAttemptedRole(userRole || 'undefined');
  
  // Clean and normalize the role
  const normalizedRole = userRole ? userRole.toLowerCase().trim() : '';
  const normalizedPortal = selectedPortal ? selectedPortal.toLowerCase().trim() : '';
  
  console.log(`🔍 Normalized: Role = "${normalizedRole}", Portal = "${normalizedPortal}"`);
  
  // If role is empty, assume it's valid for debugging purposes
  if (!normalizedRole) {
    console.log('⚠️ User role is empty, allowing access for now');
    return {
      valid: true,
      message: ""
    };
  }
  
  // Check if role is valid
  const validRoles = ["oracle", "teller"];
  if (!validRoles.includes(normalizedRole)) {
    console.log(`❌ Invalid role detected: "${normalizedRole}"`);
    return {
      valid: false,
      message: `Unknown user role: "${userRole}". Access denied. Please contact system administrator.`
    };
  }

  // Check if role matches portal
  if (normalizedRole !== normalizedPortal) {
    console.log(`❌ Role/portal mismatch: ${normalizedRole} != ${normalizedPortal}`);
    
    const attemptedPortal = normalizedPortal === "oracle" ? "Admin Portal" : "Teller Portal";
    const shouldBePortal = normalizedRole === "oracle" ? "Admin Portal" : "Teller Portal";

    const userType = normalizedRole === "oracle" 
      ? "an ADMINISTRATOR" 
      : "a TELLER";
    
    return {
      valid: false,
      message: `Your account is assigned the role of ${userType}, which grants access only to the ${shouldBePortal}. Access to the ${attemptedPortal} is not permitted.`
    };
  }

  console.log("✅ Portal validation passed");
  return {
    valid: true,
    message: ""
  };
};

  // Password validation
  const validatePasswordResetForm = (values) => {
    const errors = {};
    const password = values.newPassword || '';

    if (!password) {
      errors.newPassword = 'New password is required';
      return errors;
    }

    if (password.length < 12) {
      errors.newPassword = 'Password must be at least 12 characters';
      return errors;
    }

    const commonPatterns = ['123456', 'password', 'qwerty', 'abc123', 'letmein', 'welcome'];
    if (commonPatterns.some(pattern => password.toLowerCase().includes(pattern))) {
      errors.newPassword = 'Password contains common patterns that are easy to guess';
      return errors;
    }

    if (values.userId && password.toLowerCase().includes(values.userId.toLowerCase())) {
      errors.newPassword = 'Password should not contain your user ID';
      return errors;
    }

    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasNumbers = /\d/.test(password);
    const hasSpecialChars = /[!@#$%^&*(),.?":{}|<>]/.test(password);

    if (!(hasUpperCase && hasLowerCase && hasNumbers && hasSpecialChars)) {
      errors.newPassword = 'Password must include uppercase, lowercase, numbers, and special characters';
      return errors;
    }

    if (/(.)\1{2,}/.test(password)) {
      errors.newPassword = 'Password contains too many repeating characters';
      return errors;
    }

    const keyboardPatterns = ['qwertyuiop', 'asdfghjkl', 'zxcvbnm', '1234567890', '1qaz2wsx', '1q2w3e4r'];
    if (keyboardPatterns.some(pattern => password.toLowerCase().includes(pattern))) {
      errors.newPassword = 'Password contains common keyboard patterns';
      return errors;
    }

    if (!values.confirmPassword) {
      errors.confirmPassword = 'Please confirm your new password';
    } else if (password !== values.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    return errors;
  };

  // Login handler
  const handleLogin = async (e) => {
  e.preventDefault();

  if (!state.userId.trim() || !state.password.trim()) {
    const userRole = getUserRoleForAlert();
    showWarning("Missing Information", "Please enter both your User ID and Password", userRole);
    return;
  }

  try {
    setLoading(true);
    setPortalValidationError("");

    console.log("🔄 Starting login process for user:", state.userId);
    console.log("🎯 Selected portal:", state.userType);

    setToken(null);
    setRole(null);
    setLastAttemptedRole("");

    // Make the API call
    const loginResponse = await apiLogin({
      user_id: state.userId,
      password: state.password,
      entrySource: "web",
      deviceIp: "192.168.0.127",
      channel: "browser",
      authToken: "",
      userName: state.userId,
      deviceId: "web-client",
      deviceName: navigator.userAgent,
      country: "",
      brand: "",
      manufacturer: "",
      phoneNumber: ""
    });

    console.log("📦 Full login response:", loginResponse);
    console.log("📦 Response data:", loginResponse.data);

    if (loginResponse.responseCode !== 200) {
      throw new Error(loginResponse.message || "Login failed");
    }

    // Extract user data - handle nested structures
    let userData = loginResponse.data;
    console.log("📦 User data type:", typeof userData);
    console.log("📦 User data keys:", Object.keys(userData || {}));
    
    // Check if data is nested inside another 'data' field
    if (userData && typeof userData === 'object' && userData.data) {
      console.log("📦 Found nested data structure");
      userData = userData.data;
      console.log("📦 Extracted user data keys:", Object.keys(userData || {}));
    }

    // Try multiple possible field names for role
    const possibleRoleFields = ['role', 'userType', 'user_role', 'user_role_name', 'type', 'role_name'];
    let userRole = null;
    
    for (const field of possibleRoleFields) {
      if (userData && userData[field]) {
        userRole = userData[field].toLowerCase();
        console.log(`✅ Found role in field '${field}': ${userRole}`);
        break;
      }
    }
    
    // If still no role, check if there's a user object with role
    if (!userRole && userData && userData.user) {
      const userObj = userData.user;
      for (const field of possibleRoleFields) {
        if (userObj && userObj[field]) {
          userRole = userObj[field].toLowerCase();
          console.log(`✅ Found role in user.${field}: ${userRole}`);
          break;
        }
      }
    }

    const receivedToken = userData.authToken || userData.token || loginResponse.token;
    
    console.log("📦 Extracted token:", !!receivedToken);
    console.log("📦 Extracted role:", userRole);

    if (!receivedToken) {
      throw new Error("Login failed - no authentication token received");
    }

    console.log("✅ Initial login successful, token received");
    
    // If role is still undefined, use the selected portal type
    if (!userRole) {
      console.log("⚠️ No role found in response, using selected portal type:", state.userType);
      userRole = state.userType;
      
      // Add role to userData for consistency
      if (userData && typeof userData === 'object') {
        userData.role = state.userType;
      }
    }

    // Validate portal access
    const portalValidation = validatePortalAccess(userRole, state.userType);
    
    if (!portalValidation.valid) {
      console.log("❌ Portal access validation failed:", portalValidation.message);
      
      setToken(null);
      setRole(null);
      setLoginData(null);
      
      setPortalValidationError(portalValidation.message);
      
      showError(
        "Login Denied",
        "You do not have permission to access this portal.",
        userRole
      );

      return;
    }

    console.log("✅ Portal access validated successfully");
    
    setToken(receivedToken);
    setRole(userRole);
    setLoginData(userData);
    setPortalValidationError("");

    // Check if password reset is required
    const requiresReset = userData.requiresPasswordReset || 
                         userData.is_default_password || 
                         userData.passwordResetRequired;
    
    if (requiresReset) {
      console.log("🔄 Password reset required");
      setRequiresPasswordReset(true);
      return;
    }

    console.log("🔄 Sending OTP for user:", state.userId);
    await sendOTP(state.userId, receivedToken);
    setShowOtp(true);
    console.log("✅ OTP sent, showing OTP form");

  } catch (err) {
    console.error("❌ Login error:", err);
    setToken(null);
    setLoginData(null);
    setPortalValidationError("");
    const userRole = getUserRoleForAlert();
    showError("Login Failed", err.message, userRole);
  } finally {
    setLoading(false);
  }
};

  // OTP handlers
  const handleOtpChange = (value, idx) => {
    if (!/^[0-9]*$/.test(value)) return;
    
    const updated = [...state.otp];
    updated[idx] = value.slice(-1);
    setOtp(updated);
    
    if (value && idx < 5) {
      setTimeout(() => {
        const nextInput = inputsRef.current[idx + 1];
        if (nextInput) {
          nextInput.focus();
          nextInput.select();
        }
      }, 10);
    }
  };

  const handleOtpKeyDown = (e, idx) => {
    if (e.key === 'Backspace') {
      if (!state.otp[idx] && idx > 0) {
        e.preventDefault();
        const updated = [...state.otp];
        updated[idx - 1] = "";
        setOtp(updated);
        
        setTimeout(() => {
          const prevInput = inputsRef.current[idx - 1];
          if (prevInput) {
            prevInput.focus();
            prevInput.select();
          }
        }, 10);
      } else if (state.otp[idx]) {
        const updated = [...state.otp];
        updated[idx] = "";
        setOtp(updated);
      }
    } else if (e.key === 'ArrowLeft' && idx > 0) {
      e.preventDefault();
      setTimeout(() => {
        const prevInput = inputsRef.current[idx - 1];
        if (prevInput) {
          prevInput.focus();
          prevInput.select();
        }
      }, 10);
    } else if (e.key === 'ArrowRight' && idx < 5) {
      e.preventDefault();
      setTimeout(() => {
        const nextInput = inputsRef.current[idx + 1];
        if (nextInput) {
          nextInput.focus();
          nextInput.select();
        }
      }, 10);
    }
  };

  const handleResendOtp = async () => {
    if (!state.canResendOtp || !token) {
      const userRole = getUserRoleForAlert();
      showError("Session Expired", "Your session has expired. Please login again.", userRole);
      return;
    }

    try {
      setLoading(true);
      console.log("🔄 Resending OTP for user:", state.userId);

      await sendOTP(state.userId, token);

      setResendCountdown(30);
      setCanResendOtp(false);
      setOtp(["", "", "", "", "", ""]);
      
      setTimeout(() => {
        if (inputsRef.current[0]) {
          inputsRef.current[0].focus();
          inputsRef.current[0].select();
        }
      }, 200);

      const userRole = getUserRoleForAlert();
      showSuccess("Verification Code Sent", "We've sent a new 6-digit code to your registered mobile number. Please check your messages.", userRole);
    } catch (err) {
      console.error("❌ OTP resend error:", err);
      if (err.message?.includes('token') || err.message?.includes('auth') || err.response?.status === 401) {
        setToken(null);
        setRole(null);
        setLoginData(null);
      }
      const userRole = getUserRoleForAlert();
      showError("Resend Failed", err.message || "Failed to resend OTP. Please try again.", userRole);
    } finally {
      setLoading(false);
    }
  };

  const verifyOtp = async (e) => {
    e.preventDefault();
    const code = state.otp.join("");

    if (code.length !== 6) {
      const userRole = getUserRoleForAlert();
      showWarning("Incomplete Code", "Please enter all 6 digits of your verification code.", userRole);
      return;
    }

    if (!token || !state.loginData) {
      const userRole = getUserRoleForAlert();
      showError("Session Expired", "Your session has expired. Please login again.", userRole);
      setShowOtp(false);
      setToken(null);
      setRole(null);
      setPortalValidationError("");
      return;
    }

    try {
      setLoading(true);

      const otpData = {
        user_id: state.userId,
        otp: code
      };

      console.log("🔄 Verifying OTP for user:", state.userId);
      
      const response = await verifyOTP(otpData, token);

      console.log("✅ OTP verification response:", response);

      if (response.responseCode === 200) {
          const finalUserData = {
            ...state.loginData,
            ...response.data,
            token: token,
            authenticated: true
          };

          console.log("🎯 Final user data for authentication:", finalUserData);

          await loginWithUserData(finalUserData);

          setRole(finalUserData.role);

          if (finalUserData.role === "oracle") {
            return navigate("/admin", { replace: true });
          } else if (finalUserData.role === "teller") {
            return navigate("/teller", { replace: true });
          } else {
            return navigate("/", { replace: true });
          }
        }
       else {
        console.error("❌ OTP verification failed:", response.message);
        const userRole = getUserRoleForAlert();
        showError("Verification Failed", response.message || "Invalid OTP", userRole);
        return;
      }
    } catch (err) {
      console.error("❌ OTP verification error:", err);
      if (err.message?.includes('token') || err.message?.includes('auth') || err.response?.status === 401) {
        setToken(null);
        setRole(null);
        setLoginData(null);
        setPortalValidationError("");
      }
      const userRole = getUserRoleForAlert();
      showError("Verification Failed", err.message, userRole);
    } finally {
      setLoading(false);
    }
  };

  // Password reset handlers
  const handlePasswordReset = async (e) => {
    e.preventDefault();

    const errors = validatePasswordResetForm({
      newPassword: state.newPassword,
      confirmPassword: state.confirmPassword,
      userId: state.userId
    });

    if (Object.keys(errors).length > 0) {
      const userRole = getUserRoleForAlert();
      showError("Validation Error", Object.values(errors).join('\n'), userRole);
      return;
    }

    try {
      setLoading(true);

      if (!token) {
        throw new Error("Session expired. Please login again.");
      }

      const resetResponse = await resetDefaultPassword(token, {
        user_id: state.userId,
        old_password: state.password,
        new_password: state.newPassword
      });

      console.log("🔧 Reset password response:", resetResponse);

      if (resetResponse.responseCode === 200) {
        const userRole = getUserRoleForAlert();
        showSuccess("Password Updated Successfully", "Your password has been updated. Please log in with your new credentials.", userRole);

        setRequiresPasswordReset(false);
        setPassword("");
        setNewPassword("");
        setConfirmPassword("");
        setToken(null);
        setRole(null);
        setLoginData(null);
        setPortalValidationError("");
      } else {
        throw new Error(resetResponse.message || "Failed to reset password");
      }
    } catch (err) {
      console.error("❌ Password reset error:", err);

      if (err.message?.includes('token') || err.message?.includes('auth') || err.response?.status === 401) {
        setToken(null);
        setRole(null);
        setLoginData(null);
        setPortalValidationError("");
      }

      const userRole = getUserRoleForAlert();
      if (err.response?.data?.message) {
        showError("Password Reset Failed", err.response.data.message, userRole);
      } else if (err.data?.message) {
        showError("Password Reset Failed", err.data.message, userRole);
      } else if (err.message) {
        showError("Password Reset Failed", err.message, userRole);
      } else {
        showError("Password Reset Failed", "Password reset failed. Please try again.", userRole);
      }
    } finally {
      setLoading(false);
    }
  };

  // Forgot password handler
  const handleForgotPassword = async (e) => {
    e.preventDefault();

    if (!state.forgotUserId.trim()) {
      const userRole = getUserRoleForAlert();
      showWarning(
        "User ID Required",
        "Please enter your User ID to reset your password.",
        userRole
      );
      return;
    }

    try {
      setForgotLoading(true);

      const response = await findUser(state.forgotUserId);
      const userData = response?.data;

      if (!userData?.phoneNumber) {
        throw new Error("No phone number found for this user.");
      }

      const forgotPasswordPayload = {
        user_id: state.forgotUserId
      };

      const resetResponse = await forgotPassword(forgotPasswordPayload);

      if (resetResponse?.responseCode !== 200) {
        throw new Error(resetResponse?.message || "Failed to reset password.");
      }

      const userRole = getUserRoleForAlert();
      showSuccess(
        "Password Reset Instructions Sent",
        `We've sent password reset instructions to your registered mobile number (${userData.phoneNumber}).`,
        userRole
      );

      setShowForgotPassword(false);
      setForgotUserId("");
    } catch (error) {
      const userRole = getUserRoleForAlert();
      showError("Password Reset Failed", error.message, userRole);
    } finally {
      setForgotLoading(false);
    }
  };

  // Navigation handlers
  const handleBackFromReset = () => {
    setRequiresPasswordReset(false);
    setNewPassword("");
    setConfirmPassword("");
    setToken(null);
    setRole(null);
    setLoginData(null);
    setPortalValidationError("");
    setLastAttemptedRole("");
  };

  const handleBackFromOtp = () => {
    setShowOtp(false);
    setOtp(["", "", "", "", "", ""]);
    setToken(null);
    setRole(null);
    setLoginData(null);
    setResendCountdown(0);
    setCanResendOtp(false);
    setPortalValidationError("");
    setLastAttemptedRole("");
  };

  // Clear validation error
  const clearPortalValidationError = () => {
    setPortalValidationError("");
  };

  return {
    // State
    state,
    inputsRef,
    token,
    role,
    user,
    
    // State setters
    setUserId,
    setPassword,
    setShowPassword,
    setLoading,
    setShowOtp,
    setOtp,
    setLoginData,
    setUserType,
    setPortalValidationError,
    setLastAttemptedRole,
    setResendCountdown,
    setCanResendOtp,
    setRequiresPasswordReset,
    setNewPassword,
    setConfirmPassword,
    setShowNewPassword,
    setShowConfirmPassword,
    setShowForgotPassword,
    setForgotUserId,
    setForgotLoading,
    setIsMobile,
    
    // Handlers
    handleLogin,
    handleOtpChange,
    handleOtpKeyDown,
    handleResendOtp,
    verifyOtp,
    handlePasswordReset,
    handleForgotPassword,
    handleBackFromReset,
    handleBackFromOtp,
    clearPortalValidationError,
    
    // Helpers
    getUserRoleForAlert,
    showCustomError,
    showCustomSuccess,
    showCustomWarning,
    validatePortalAccess,
    validatePasswordResetForm
  };
};