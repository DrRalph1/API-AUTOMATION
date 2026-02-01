// components/layout/LoginLayout.js
import React from "react";
import { motion } from "framer-motion";

const LoginLayout = ({ 
  children, 
  colors, 
  isDark, 
  isMobile,
  theme 
}) => {
  return (
    <div className={`min-h-screen bg-gradient-to-br ${colors.background} relative overflow-y-auto`}>
      {/* Background effects */}
      <div className="absolute inset-0">
        {!isMobile && (
          <>
            <div className={`absolute top-1/4 -left-1/4 w-1/2 h-1/2 bg-gradient-to-r ${colors.gradientOrbs.left} rounded-full blur-3xl`} />
            <div className={`absolute bottom-1/4 -right-1/4 w-1/2 h-1/2 bg-gradient-to-r ${colors.gradientOrbs.right} rounded-full blur-3xl`} />
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full h-full bg-gradient-radial from-transparent via-gray-900/30 to-transparent" />
          </>
        )}
      </div>

      {/* Particles */}
      <div className="absolute inset-0 pointer-events-none">
        {[...Array(isMobile ? 8 : 20)].map((_, i) => (
          <motion.div
            key={i}
            className={`absolute w-px h-px ${colors.particleColor} rounded-full`}
            style={{
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
            }}
            animate={{
              y: [0, -100, 0],
              opacity: [0, 1, 0],
            }}
            transition={{
              duration: 3 + Math.random() * 2,
              repeat: Infinity,
              delay: Math.random() * 2,
            }}
          />
        ))}
      </div>

      {/* Main Container */}
      <div className={`relative zoomer ${isMobile ? 'h-auto min-h-screen' : 'h-screen'} flex items-center justify-center p-4 sm:p-6`}>
        <div className="w-full max-w-6xl">
          {children}
        </div>
      </div>
    </div>
  );
};

export default LoginLayout;