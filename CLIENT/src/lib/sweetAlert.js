// lib/sweetAlert.jsx - DIALOG-BASED VERSION (EXACT SAME API)
import React, { useState, useEffect, createContext, useContext } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";
import { AlertCircle, AlertOctagon, CheckCircle, Info, XCircle, AlertTriangle, Loader2 } from "lucide-react";

// --- Theme configuration (SAME AS ORIGINAL) ---
const getPortalTheme = (isDark, userRole = "oracle") => {
  const role = (userRole && userRole.toLowerCase() === "oracle") ? "oracle" : 'teller';

  if (role === "oracle") {
    return {
      gradient: 'from-purple-600 to-pink-500',
      iconColor: '#8b5cf6',
      timerProgressBar: 'bg-gradient-to-r from-purple-500 to-pink-400',
      name: 'Admin Portal',
      buttonClass: 'bg-gradient-to-r from-purple-600 to-pink-500',
      popupBorder: isDark ? 'border-purple-800/30' : 'border-purple-200'
    };
  }

  // Default to teller theme
  return {
    gradient: 'from-blue-600 to-cyan-500',
    iconColor: '#3b82f6',
    timerProgressBar: 'bg-gradient-to-r from-blue-500 to-cyan-400',
    name: 'Admin Portal',
    buttonClass: 'bg-gradient-to-r from-blue-600 to-cyan-500',
    popupBorder: isDark ? 'border-blue-800/30' : 'border-blue-200'
  };
};

// icon-specific configs
const iconConfigs = (isDark, userRole) => {
  const theme = getPortalTheme(isDark, userRole);

  return {
    success: {
      iconColor: '#10b981',
      buttonClass: 'bg-gradient-to-r from-emerald-600 to-green-500',
      timerProgressBar: 'bg-gradient-to-r from-emerald-500 to-green-400',
      popupBorder: isDark ? 'border-emerald-800/30' : 'border-emerald-200'
    },
    error: {
      iconColor: '#ef4444',
      buttonClass: 'bg-gradient-to-r from-red-600 to-rose-500',
      timerProgressBar: 'bg-gradient-to-r from-red-500 to-rose-400',
      popupBorder: isDark ? 'border-red-800/30' : 'border-red-200'
    },
    warning: {
      iconColor: '#f59e0b',
      buttonClass: 'bg-gradient-to-r from-amber-600 to-yellow-500',
      timerProgressBar: 'bg-gradient-to-r from-amber-500 to-yellow-400',
      popupBorder: isDark ? 'border-amber-800/30' : 'border-amber-200'
    },
    info: {
      iconColor: theme.iconColor,
      buttonClass: theme.buttonClass,
      timerProgressBar: theme.timerProgressBar,
      popupBorder: theme.popupBorder
    },
    question: {
      iconColor: theme.iconColor,
      buttonClass: theme.buttonClass,
      timerProgressBar: theme.timerProgressBar,
      popupBorder: theme.popupBorder
    }
  };
};

// Detect mobile viewport (safe for SSR)
const isViewportMobile = () => {
  try {
    return typeof window !== 'undefined' && window.innerWidth <= 640;
  } catch {
    return false;
  }
};

// --- Dialog Alert Manager ---
const DialogAlertContext = createContext(null);

export function DialogAlertProvider({ children }) {
  const [alerts, setAlerts] = useState([]);
  const [isDark, setIsDark] = useState(false);
  
  useEffect(() => {
    if (typeof document !== 'undefined') {
      setIsDark(document.documentElement.classList.contains('dark'));
      
      const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
          if (mutation.attributeName === 'class') {
            setIsDark(document.documentElement.classList.contains('dark'));
          }
        });
      });
      
      observer.observe(document.documentElement, { attributes: true });
      return () => observer.disconnect();
    }
  }, []);
  
  const showAlert = (config) => {
    const id = Date.now().toString();
    setAlerts(prev => [...prev, { id, ...config }]);
    return id;
  };
  
  const closeAlert = (id) => {
    setAlerts(prev => prev.filter(alert => alert.id !== id));
  };
  
  const showSuccess = (title, text = '', userRole = "oracle", options = {}) => {
    const id = showAlert({
      type: 'success',
      title,
      text,
      userRole,
      timer: 3000,
      ...options
    });
    
    // Auto-close after timer
    if (options.timer !== false) {
      setTimeout(() => closeAlert(id), options.timer || 3000);
    }
    
    return { isConfirmed: true };
  };
  
  const showError = (title, text = '', userRole = "oracle", options = {}) => {
    const actualUserRole = (typeof userRole === 'string' && userRole === "oracle") ? "oracle" : 'teller';
    const id = showAlert({
      type: 'error',
      title,
      text,
      userRole: actualUserRole,
      timer: 5000,
      ...options
    });
    
    // Auto-close after timer
    if (options.timer !== false) {
      setTimeout(() => closeAlert(id), options.timer || 5000);
    }
    
    return { isConfirmed: true };
  };
  
  const showWarning = (title, text = '', userRole = "oracle", options = {}) => {
    const id = showAlert({
      type: 'warning',
      title,
      text,
      userRole,
      timer: 4000,
      ...options
    });
    
    // Auto-close after timer
    if (options.timer !== false) {
      setTimeout(() => closeAlert(id), options.timer || 4000);
    }
    
    return { isConfirmed: true };
  };
  
  const showInfo = (title, text = '', userRole = "oracle", options = {}) => {
    const id = showAlert({
      type: 'info',
      title,
      text,
      userRole,
      timer: 4000,
      ...options
    });
    
    // Auto-close after timer
    if (options.timer !== false) {
      setTimeout(() => closeAlert(id), options.timer || 4000);
    }
    
    return { isConfirmed: true };
  };
  
  const showConfirm = async (title, text = '', confirmText = 'Confirm', userRole = "oracle", options = {}) => {
    return new Promise((resolve) => {
      showAlert({
        type: 'confirm',
        title,
        text,
        confirmText,
        cancelText: options.cancelText || 'Cancel',
        userRole,
        onConfirm: () => resolve({ isConfirmed: true }),
        onCancel: () => resolve({ isConfirmed: false }),
        ...options
      });
    });
  };
  
  const showInput = async (title, text, inputType = 'text', placeholder = '', userRole = "oracle", validationMessage = 'Please enter a value!') => {
    return new Promise((resolve) => {
      showAlert({
        type: 'input',
        title,
        text,
        inputType,
        placeholder,
        validationMessage,
        userRole,
        onSubmit: (value) => resolve({ isConfirmed: true, value }),
        onCancel: () => resolve({ isConfirmed: false, value: null })
      });
    });
  };
  
  const showLoading = (title = 'Loading...', userRole = "oracle") => {
    const id = showAlert({
      type: 'loading',
      title,
      userRole
    });
    
    return {
      close: () => closeAlert(id)
    };
  };
  
  const showToast = {
    success: (message, userRole = "oracle") => {
      const id = showAlert({
        type: 'toast',
        toastType: 'success',
        message,
        userRole,
        autoClose: true
      });
      
      setTimeout(() => closeAlert(id), 3000);
      return { close: () => closeAlert(id) };
    },
    
    error: (message, userRole = "oracle") => {
      const id = showAlert({
        type: 'toast',
        toastType: 'error',
        message,
        userRole,
        autoClose: true
      });
      
      setTimeout(() => closeAlert(id), 4000);
      return { close: () => closeAlert(id) };
    },
    
    warning: (message, userRole = "oracle") => {
      const id = showAlert({
        type: 'toast',
        toastType: 'warning',
        message,
        userRole,
        autoClose: true
      });
      
      setTimeout(() => closeAlert(id), 3500);
      return { close: () => closeAlert(id) };
    },
    
    info: (message, userRole = "oracle") => {
      const id = showAlert({
        type: 'toast',
        toastType: 'info',
        message,
        userRole,
        autoClose: true
      });
      
      setTimeout(() => closeAlert(id), 3000);
      return { close: () => closeAlert(id) };
    }
  };
  
  const showCustom = (options = {}, userRole = "oracle") => {
    const id = showAlert({
      type: 'custom',
      userRole,
      ...options
    });
    
    return { close: () => closeAlert(id) };
  };
  
  const closeAllAlerts = () => {
    setAlerts([]);
  };
  
  const value = {
    showSuccess,
    showError,
    showWarning,
    showInfo,
    showConfirm,
    showInput,
    showLoading,
    showToast,
    showCustom,
    closeAlert,
    closeAllAlerts
  };
  
  // Store instance for global access
  useEffect(() => {
    window.__SWEET_ALERT_INSTANCE__ = value;
  }, [value]);
  
  return (
    <DialogAlertContext.Provider value={value}>
      {children}
      {alerts.map((alert) => (
        <DialogAlertComponent
          key={alert.id}
          {...alert}
          isDark={isDark}
          onClose={() => closeAlert(alert.id)}
          isMobile={isViewportMobile()}
        />
      ))}
    </DialogAlertContext.Provider>
  );
}

// Hook for using alerts
export const useSweetAlert = () => {
  const context = useContext(DialogAlertContext);
  if (!context) {
    throw new Error('useSweetAlert must be used within DialogAlertProvider');
  }
  return context;
};

// Dialog Component
function DialogAlertComponent({
  type = 'info',
  title = '',
  text = '',
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  userRole = "oracle",
  onConfirm,
  onCancel,
  onClose,
  inputType = 'text',
  placeholder = '',
  validationMessage = '',
  onSubmit,
  toastType = 'info',
  autoClose = false,
  isDark = false,
  isMobile = false,
  timer = null,
  options = {},
  ...props
}) {
  const [inputValue, setInputValue] = useState('');
  const [inputError, setInputError] = useState('');
  
  const theme = getPortalTheme(isDark, userRole);
  const iconConfig = iconConfigs(isDark, userRole)[type] || iconConfigs(isDark, userRole).info;
  
  // Auto-close for timed alerts
  useEffect(() => {
    if (timer && type !== 'confirm' && type !== 'input' && type !== 'loading') {
      const timerId = setTimeout(() => {
        handleConfirm();
      }, timer);
      
      return () => clearTimeout(timerId);
    }
  }, [timer, type]);
  
  // Auto-close for toast
  useEffect(() => {
    if (autoClose && type === 'toast') {
      const timerId = setTimeout(() => {
        onClose?.();
      }, 3000);
      return () => clearTimeout(timerId);
    }
  }, [autoClose, type, onClose]);
  
  const getIcon = () => {
    if (type === 'toast') {
      switch (toastType) {
        case 'success': return { Icon: CheckCircle, color: 'text-green-600 dark:text-green-400' };
        case 'error': return { Icon: AlertOctagon, color: 'text-red-600 dark:text-red-400' };
        case 'warning': return { Icon: AlertTriangle, color: 'text-yellow-600 dark:text-yellow-400' };
        case 'info':
        default: return { 
          Icon: Info, 
          color: userRole === "oracle" 
            ? 'text-purple-600 dark:text-purple-400' 
            : 'text-blue-600 dark:text-blue-400' 
        };
      }
    }
    
    switch (type) {
      case 'success': return { Icon: CheckCircle, color: 'text-green-600 dark:text-green-400' };
      case 'error': return { Icon: AlertOctagon, color: 'text-red-600 dark:text-red-400' };
      case 'warning': return { Icon: AlertTriangle, color: 'text-yellow-600 dark:text-yellow-400' };
      case 'loading': return { Icon: Loader2, color: theme.iconColor };
      case 'confirm': 
      case 'input':
      case 'info':
      default: return { 
        Icon: Info, 
        color: userRole === "oracle" 
          ? 'text-purple-600 dark:text-purple-400' 
          : 'text-blue-600 dark:text-blue-400' 
      };
    }
  };
  
  const { Icon, color } = getIcon();
  
  const handleConfirm = () => {
    if (onConfirm) onConfirm();
    if (onClose) onClose();
  };
  
  const handleCancel = () => {
    if (onCancel) onCancel();
    if (onClose) onClose();
  };
  
  const handleSubmitInput = () => {
    if (!inputValue.trim()) {
      setInputError(validationMessage);
      return;
    }
    
    if (onSubmit) onSubmit(inputValue);
    if (onClose) onClose();
  };
  
  const handleInputChange = (e) => {
    setInputValue(e.target.value);
    if (inputError) setInputError('');
  };
  
  // Toast variant
  if (type === 'toast') {
    return (
      <div className="fixed top-4 right-4 z-[999999] animate-slide-in">
        <div className={`flex items-center gap-3 p-4 rounded-lg border shadow-lg ${
          toastType === 'success' 
            ? `${isDark ? 'bg-green-900/10 border-green-800/30' : 'bg-green-50 border-green-200'}` 
            : toastType === 'error'
            ? `${isDark ? 'bg-red-900/10 border-red-800/30' : 'bg-red-50 border-red-200'}`
            : toastType === 'warning'
            ? `${isDark ? 'bg-yellow-900/10 border-yellow-800/30' : 'bg-yellow-50 border-yellow-200'}`
            : userRole === "oracle"
            ? `${isDark ? 'bg-purple-900/10 border-purple-800/30' : 'bg-purple-50 border-purple-200'}`
            : `${isDark ? 'bg-blue-900/10 border-blue-800/30' : 'bg-blue-50 border-blue-200'}`
        }`}>
          <Icon className={`h-5 w-5 ${color}`} />
          <span className="text-sm font-medium">{title || text}</span>
        </div>
      </div>
    );
  }
  
  // Loading dialog
  if (type === 'loading') {
    return (
      <Dialog open={true} onOpenChange={() => {}}>
        <DialogContent className={`sm:max-w-md ${isMobile ? 'w-[92vw] p-4' : 'p-6'} ${isDark ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-200'} ${iconConfig.popupBorder} rounded-xl shadow-2xl`}>
          <div className="flex items-center justify-center py-6">
            <div className="text-center space-y-3">
              <Loader2 className={`h-10 w-10 mx-auto animate-spin ${color}`} />
              <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-600'}`}>{title}</p>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    );
  }
  
  // Main dialog for alerts, confirm, input
  return (
  <Dialog 
    open={true} 
    onOpenChange={type === 'confirm' || type === 'input' ? handleCancel : handleConfirm}
  >
    <DialogContent 
      className={`
        ${isMobile ? 'w-[92vw] p-4 max-w-[92vw]' : 'max-w-md p-6'} 
        ${isDark ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-200'} 
        ${iconConfig.popupBorder} 
        rounded-2xl 
        shadow-2xl shadow-black/10 dark:shadow-black/30
        animate-in fade-in-0 zoom-in-95 duration-200
      `}
    >
      <DialogHeader className="space-y-4">
        <div className="flex items-start gap-4 mb-4">
          <div className={`
            p-3 rounded-xl 
            ${isDark 
              ? type === 'success' ? 'bg-emerald-900/20 border-emerald-800/40' 
                : type === 'error' ? 'bg-rose-900/20 border-rose-800/40'
                : type === 'warning' ? 'bg-amber-900/20 border-amber-800/40'
                : userRole === "oracle" ? 'bg-violet-900/20 border-violet-800/40' 
                : 'bg-sky-900/20 border-sky-800/40'
              : type === 'success' ? 'bg-emerald-50 border-emerald-200'
                : type === 'error' ? 'bg-rose-50 border-rose-200'
                : type === 'warning' ? 'bg-amber-50 border-amber-200'
                : userRole === "oracle" ? 'bg-violet-50 border-violet-200'
                : 'bg-sky-50 border-sky-200'
            } 
            border shadow-sm
            animate-pulse-subtle
          `}>
            <Icon className={`h-6 w-6 ${color} animate-in zoom-in-50 duration-300`} />
          </div>
          
          <div className="flex-1 pt-1">
            <DialogTitle className={`
              text-xl font-semibold leading-tight 
              ${isDark ? 'text-gray-100' : 'text-gray-900'}
            `}>
              {title}
            </DialogTitle>
            
            {/* {userRole && (
              <span className={`
                inline-block px-2 py-1 mt-2 rounded-md text-xs font-medium
                ${userRole === "oracle"
                  ? `${isDark ? 'bg-violet-900/30 text-violet-300' : 'bg-violet-100 text-violet-700'}`
                  : `${isDark ? 'bg-sky-900/30 text-sky-300' : 'bg-sky-100 text-sky-700'}`
                }
                animate-in slide-in-from-left-2 duration-300
              `}>
                {userRole === "oracle" ? 'Super Admin' : 'Teller'}
              </span>
            )} */}
          </div>
        </div>

        {text && (
          <DialogDescription className={`
            text-md leading-relaxed -mb-4 px-1
            ${isDark ? 'text-gray-400' : 'text-gray-600'}
            animate-in fade-in-50 duration-300 delay-100
          `}>
            {text}
          </DialogDescription>
        )}
      </DialogHeader>

      {type === 'input' && (
        <div className="space-y-4 pt-4 animate-in fade-in-50 duration-300 delay-150">
          <div className="space-y-3">
            <Label 
              htmlFor="dialog-input" 
              className={`text-sm font-medium ${isDark ? 'text-gray-300' : 'text-gray-700'}`}
            >
              {placeholder || 'Enter value'}
            </Label>
            <Input
              id="dialog-input"
              type={inputType}
              placeholder={placeholder}
              value={inputValue}
              onChange={handleInputChange}
              className={`
                h-11 transition-all duration-200
                ${inputError ? 'border-red-500 focus-visible:ring-red-500/20' : ''} 
                ${isDark 
                  ? 'bg-gray-800/50 border-gray-700 text-white focus:border-gray-600 focus:ring-gray-600/20' 
                  : 'bg-gray-50 border-gray-300 focus:border-gray-400 focus:ring-gray-400/20'
                }
                focus-visible:ring-2 focus-visible:ring-offset-2
                ${isDark ? 'focus-visible:ring-offset-gray-900' : 'focus-visible:ring-offset-white'}
                placeholder:text-gray-500
              `}
              autoFocus
            />
            {inputError && (
              <div className="flex items-center gap-2 text-red-500 text-sm animate-shake">
                <AlertCircle className="h-4 w-4 flex-shrink-0" />
                <span>{inputError}</span>
              </div>
            )}
          </div>
        </div>
      )}

      <DialogFooter className={`
        flex ${isMobile ? 'flex-col' : 'flex-row'} gap-3 pt-6
        animate-in fade-in-50 duration-300 delay-200
      `}>
        {(type === 'confirm' || type === 'input') && (
          <Button
            variant="outline"
            onClick={handleCancel}
            className={`
              ${isMobile ? 'w-full' : 'w-auto'} 
              h-11 px-6
              transition-all duration-200 hover:scale-[1.02] active:scale-[0.98]
              ${isDark 
                ? 'border-gray-700 bg-gray-800/50 text-gray-300 hover:bg-gray-700 hover:text-gray-200' 
                : 'border-gray-300 bg-white text-gray-700 hover:bg-gray-100'
              }
              shadow-sm hover:shadow
            `}
          >
            <XCircle className="h-4 w-4 mr-2" />
            {cancelText || 'Cancel'}
          </Button>
        )}
        
        {type === 'input' ? (
          <Button
            onClick={handleSubmitInput}
            className={`
              ${isMobile ? 'w-full' : 'w-auto'} 
              h-11 px-6
              transition-all duration-200 hover:scale-[1.02] active:scale-[0.98]
              ${iconConfig.buttonClass} 
              hover:opacity-90 text-white
              shadow-lg hover:shadow-xl
              disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100
            `}
          >
            <Check className="h-4 w-4 mr-2" />
            {confirmText || 'Submit'}
          </Button>
        ) : (
          <Button
            onClick={handleConfirm}
            className={`
              ${isMobile ? 'w-full' : 'w-auto'} 
              h-11 px-6
              transition-all duration-200 hover:scale-[1.02] active:scale-[0.98]
              ${type === 'confirm' || type === 'input'
                ? `${iconConfig.buttonClass} hover:opacity-90 text-white shadow-lg hover:shadow-xl`
                : `${isDark 
                    ? 'border-gray-700 bg-gray-800/50 text-gray-300 hover:bg-gray-700 hover:text-gray-200' 
                    : 'border-gray-300 bg-white text-gray-700 hover:bg-gray-100'
                  } shadow-sm hover:shadow`
              }
            `}
            variant={type === 'confirm' || type === 'input' ? 'default' : 'outline'}
          >
            {type === 'confirm' ? (confirmText || 'Confirm') : 'OK'}
          </Button>
        )}
      </DialogFooter>
    </DialogContent>
  </Dialog>
);
}

// --- Global export functions (EXACT SAME SIGNATURES AS ORIGINAL) ---

// Success Alert - Compact
export const showSuccess = (title, text = '', userRole = "oracle", options = {}) => {
  if (window.__SWEET_ALERT_INSTANCE__) {
    return window.__SWEET_ALERT_INSTANCE__.showSuccess(title, text, userRole, options);
  }
  console.warn('DialogAlertProvider not mounted. Wrap your app with DialogAlertProvider.');
  return { isConfirmed: true };
};

// Error Alert - Compact
export const showError = (title, text = '', userRole = "oracle", options = {}) => {
  if (window.__SWEET_ALERT_INSTANCE__) {
    return window.__SWEET_ALERT_INSTANCE__.showError(title, text, userRole, options);
  }
  console.warn('DialogAlertProvider not mounted. Wrap your app with DialogAlertProvider.');
  return { isConfirmed: true };
};

// Warning Alert - Compact
export const showWarning = (title, text = '', userRole = "oracle", options = {}) => {
  if (window.__SWEET_ALERT_INSTANCE__) {
    return window.__SWEET_ALERT_INSTANCE__.showWarning(title, text, userRole, options);
  }
  console.warn('DialogAlertProvider not mounted. Wrap your app with DialogAlertProvider.');
  return { isConfirmed: true };
};

// Info Alert - Compact
export const showInfo = (title, text = '', userRole = "oracle", options = {}) => {
  if (window.__SWEET_ALERT_INSTANCE__) {
    return window.__SWEET_ALERT_INSTANCE__.showInfo(title, text, userRole, options);
  }
  console.warn('DialogAlertProvider not mounted. Wrap your app with DialogAlertProvider.');
  return { isConfirmed: true };
};

// Confirmation Dialog - Compact
export const showConfirm = async (title, text = '', confirmText = 'Confirm', userRole = "oracle", options = {}) => {
  if (window.__SWEET_ALERT_INSTANCE__) {
    return window.__SWEET_ALERT_INSTANCE__.showConfirm(title, text, confirmText, userRole, options);
  }
  console.warn('DialogAlertProvider not mounted. Wrap your app with DialogAlertProvider.');
  return { isConfirmed: true };
};

// Loading Alert - Compact
export const showLoading = (title = 'Loading...', userRole = "oracle") => {
  if (window.__SWEET_ALERT_INSTANCE__) {
    return window.__SWEET_ALERT_INSTANCE__.showLoading(title, userRole);
  }
  console.warn('DialogAlertProvider not mounted. Wrap your app with DialogAlertProvider.');
  return { close: () => {} };
};

// Form Input Alert - Compact & responsive
export const showInput = async (title, text, inputType = 'text', placeholder = '', userRole = "oracle", validationMessage = 'Please enter a value!') => {
  if (window.__SWEET_ALERT_INSTANCE__) {
    return window.__SWEET_ALERT_INSTANCE__.showInput(title, text, inputType, placeholder, userRole, validationMessage);
  }
  console.warn('DialogAlertProvider not mounted. Wrap your app with DialogAlertProvider.');
  return { isConfirmed: true, value: '' };
};

// Toast-style alerts - responsive layouts and width
export const showToast = {
  success: (message, userRole = "oracle") => {
    if (window.__SWEET_ALERT_INSTANCE__) {
      return window.__SWEET_ALERT_INSTANCE__.showToast.success(message, userRole);
    }
    console.warn('DialogAlertProvider not mounted. Wrap your app with DialogAlertProvider.');
    return { close: () => {} };
  },

  error: (message, userRole = "oracle") => {
    if (window.__SWEET_ALERT_INSTANCE__) {
      return window.__SWEET_ALERT_INSTANCE__.showToast.error(message, userRole);
    }
    console.warn('DialogAlertProvider not mounted. Wrap your app with DialogAlertProvider.');
    return { close: () => {} };
  },

  warning: (message, userRole = "oracle") => {
    if (window.__SWEET_ALERT_INSTANCE__) {
      return window.__SWEET_ALERT_INSTANCE__.showToast.warning(message, userRole);
    }
    console.warn('DialogAlertProvider not mounted. Wrap your app with DialogAlertProvider.');
    return { close: () => {} };
  },

  info: (message, userRole = "oracle") => {
    if (window.__SWEET_ALERT_INSTANCE__) {
      return window.__SWEET_ALERT_INSTANCE__.showToast.info(message, userRole);
    }
    console.warn('DialogAlertProvider not mounted. Wrap your app with DialogAlertProvider.');
    return { close: () => {} };
  }
};

// Custom Alert with HTML content - responsive
export const showCustom = (options = {}, userRole = "oracle") => {
  if (window.__SWEET_ALERT_INSTANCE__) {
    return window.__SWEET_ALERT_INSTANCE__.showCustom(options, userRole);
  }
  console.warn('DialogAlertProvider not mounted. Wrap your app with DialogAlertProvider.');
  return { close: () => {} };
};

// Close any open alert
export const closeAlert = () => {
  if (window.__SWEET_ALERT_INSTANCE__) {
    return window.__SWEET_ALERT_INSTANCE__.closeAlert();
  }
  console.warn('DialogAlertProvider not mounted. Wrap your app with DialogAlertProvider.');
};

// Default export
export default {
  DialogAlertProvider,
  useSweetAlert,
  showSuccess,
  showError,
  showWarning,
  showInfo,
  showConfirm,
  showInput,
  showLoading,
  showToast,
  showCustom,
  closeAlert
};