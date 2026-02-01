// components/ui/PortalValidationError.js
import React from "react";
import { motion } from "framer-motion";
import { Ban, AlertTriangle } from "lucide-react";

const PortalValidationError = ({ error, userType, isMobile, isDark, lastAttemptedRole }) => {
  const getErrorStyles = () => {
    if (userType === "oracle") {
      return {
        border: isDark ? 'border-purple-800/30' : 'border-purple-200',
        bg: isDark ? 'bg-purple-900/20' : 'bg-purple-50',
        text: isDark ? 'text-purple-300' : 'text-purple-700',
        icon: isDark ? 'text-purple-400' : 'text-purple-600',
        subtitle: isDark ? 'text-purple-300/90' : 'text-purple-600/90',
        suggestion: isDark ? 'text-purple-400/70' : 'text-purple-500/70'
      };
    }
    return {
      border: isDark ? 'border-blue-800/30' : 'border-blue-200',
      bg: isDark ? 'bg-blue-900/20' : 'bg-blue-50',
      text: isDark ? 'text-blue-300' : 'text-blue-700',
      icon: isDark ? 'text-blue-400' : 'text-blue-600',
      subtitle: isDark ? 'text-blue-300/90' : 'text-blue-600/90',
      suggestion: isDark ? 'text-blue-400/70' : 'text-blue-500/70'
    };
  };

  const styles = getErrorStyles();

  return (
    <motion.div
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -10 }}
      className={`${isMobile ? 'mb-4 p-3 rounded-lg' : 'mb-6 p-4 rounded-xl'} border backdrop-blur-sm ${styles.border} ${styles.bg}`}
    >
      <div className="flex items-start gap-2 sm:gap-3">
        <Ban className={`${isMobile ? 'h-4 w-4 mt-0.5' : 'h-5 w-5 mt-0.5'} flex-shrink-0 ${styles.icon}`} />
        <div>
          <div className={`${isMobile ? 'text-sm' : 'font-medium'} mb-1 ${styles.text}`}>
            {isMobile ? "Wrong Portal" : "Access Denied - Wrong Portal"}
          </div>
          <div className={`${isMobile ? 'text-xs' : 'text-sm'} ${styles.subtitle}`}>
            {isMobile 
              ? error.length > 100 
                ? error.substring(0, 100) + '...' 
                : error
              : error
            }
          </div>
          
          {!isMobile && (
            <div className={`mt-2 text-xs ${styles.suggestion}`}>
              <div className="flex items-center gap-1">
                <AlertTriangle className="h-3 w-3" />
                <span>
                  Please switch to the <span className="font-semibold">
                    {lastAttemptedRole === "oracle" ? "Teller Portal" : "Admin Portal"}
                  </span> tab above
                </span>
              </div>
            </div>
          )}
        </div>
      </div>
    </motion.div>
  );
};

export default PortalValidationError;