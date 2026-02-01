import React from "react";

// Creative animated shield with cyber security theme
export function CyberShieldLogo({ className = "h-7 w-7", variant = "default" }) {
  const variants = {
    default: "from-cyan-400 via-blue-500 to-indigo-600",
    primary: "from-emerald-400 via-teal-500 to-cyan-600",
    danger: "from-red-400 via-rose-500 to-pink-600",
    warning: "from-yellow-400 via-amber-500 to-orange-600",
    success: "from-green-400 via-emerald-500 to-teal-600",
    security: "from-slate-400 via-gray-500 to-slate-700"
  };

  return (
    <div className={`${className} relative group`}>
      {/* Outer glow ring */}
      <div className={`absolute inset-0 rounded-full bg-gradient-to-r ${variants[variant]} opacity-20 blur-sm group-hover:opacity-40 transition-opacity duration-300`}></div>
      
      {/* Main shield container */}
      <div className={`relative rounded-2xl bg-gradient-to-br ${variants[variant]} flex items-center justify-center shadow-2xl ring-2 ring-white/20 group-hover:ring-white/40 transition-all duration-300 group-hover:scale-105`}>
        {/* Animated inner glow */}
        <div className="absolute inset-0 rounded-2xl bg-gradient-to-br from-white/30 via-white/10 to-transparent animate-pulse"></div>
        
        {/* Cyber grid pattern overlay */}
        <div className="absolute inset-0 rounded-2xl opacity-20">
          <svg viewBox="0 0 24 24" className="w-full h-full">
            <defs>
              <pattern id="grid" width="4" height="4" patternUnits="userSpaceOnUse">
                <path d="M 4 0 L 0 0 0 4" fill="none" stroke="white" strokeWidth="0.5" opacity="0.3"/>
              </pattern>
            </defs>
            <rect width="100%" height="100%" fill="url(#grid)" />
          </svg>
        </div>
        
        {/* Main shield SVG */}
        <svg
          viewBox="0 0 32 32"
          fill="none"
          className="relative h-6 w-6 text-white drop-shadow-lg group-hover:drop-shadow-xl transition-all duration-300"
        >
          {/* Shield body with gradient */}
          <defs>
            <linearGradient id="shieldGrad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="white" stopOpacity="0.9"/>
              <stop offset="50%" stopColor="white" stopOpacity="0.7"/>
              <stop offset="100%" stopColor="white" stopOpacity="0.5"/>
            </linearGradient>
            <linearGradient id="checkGrad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#00ff88"/>
              <stop offset="100%" stopColor="#00ccff"/>
            </linearGradient>
            <filter id="glow">
              <feGaussianBlur stdDeviation="1" result="coloredBlur"/>
              <feMerge> 
                <feMergeNode in="coloredBlur"/>
                <feMergeNode in="SourceGraphic"/>
              </feMerge>
            </filter>
          </defs>
          
          {/* Main shield shape */}
          <path
            d="M16 2L6 6v8c0 8 6 14 10 16 4-2 10-8 10-16V6l-10-4z"
            fill="url(#shieldGrad)"
            stroke="white"
            strokeWidth="0.5"
            strokeOpacity="0.8"
          />
          
          {/* Inner security pattern */}
          <path
            d="M8 8h16M8 10h14M8 12h12M8 14h10"
            stroke="white"
            strokeWidth="1"
            strokeOpacity="0.6"
            strokeLinecap="round"
            className="group-hover:stroke-cyan-300 transition-colors duration-300"
          />
          
          {/* Animated checkmark with glow */}
          <path
            d="M11 16l3 3 6-6"
            stroke="url(#checkGrad)"
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
            fill="none"
            filter="url(#glow)"
            className="group-hover:stroke-[#00ff88] transition-colors duration-300"
          />
          
          {/* Security dots */}
          <circle cx="12" cy="6" r="1" fill="white" opacity="0.8"/>
          <circle cx="20" cy="8" r="1" fill="white" opacity="0.6"/>
          <circle cx="8" cy="18" r="1" fill="white" opacity="0.7"/>
        </svg>
        
        {/* Floating particles effect */}
        <div className="absolute inset-0 overflow-hidden rounded-2xl">
          <div className="absolute top-1 left-2 w-1 h-1 bg-white/60 rounded-full animate-ping"></div>
          <div className="absolute top-3 right-3 w-0.5 h-0.5 bg-cyan-300/80 rounded-full animate-pulse"></div>
          <div className="absolute bottom-2 left-4 w-0.5 h-0.5 bg-white/40 rounded-full animate-bounce"></div>
        </div>
      </div>
    </div>
  );
}

// Futuristic hexagon shield design
export function HexShieldLogo({ className = "h-7 w-7", variant = "default" }) {
  const variants = {
    default: "from-purple-500 via-pink-500 to-red-500",
    primary: "from-blue-500 via-cyan-500 to-teal-500",
    danger: "from-red-500 via-orange-500 to-yellow-500",
    warning: "from-yellow-500 via-orange-500 to-red-500",
    success: "from-green-500 via-emerald-500 to-cyan-500"
  };

  return (
    <div className={`${className} relative group`}>
      {/* Hexagon shield */}
      <div className={`relative w-full h-full bg-gradient-to-br ${variants[variant]} rounded-lg flex items-center justify-center shadow-2xl transform rotate-45 group-hover:rotate-0 transition-transform duration-500`}>
        {/* Inner hexagon */}
        <div className="w-3/4 h-3/4 bg-white/20 rounded-sm flex items-center justify-center">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            className="h-4 w-4 text-white -rotate-45 group-hover:rotate-0 transition-transform duration-500"
          >
            <path
              d="M12 2L4 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-8-3z"
              fill="currentColor"
              fillOpacity="0.9"
            />
            <path
              d="M9 12l2 2 4-4"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              fill="none"
            />
          </svg>
        </div>
        
        {/* Corner accents */}
        <div className="absolute top-1 left-1 w-1 h-1 bg-white/60 rounded-full"></div>
        <div className="absolute top-1 right-1 w-1 h-1 bg-white/60 rounded-full"></div>
        <div className="absolute bottom-1 left-1 w-1 h-1 bg-white/60 rounded-full"></div>
        <div className="absolute bottom-1 right-1 w-1 h-1 bg-white/60 rounded-full"></div>
      </div>
    </div>
  );
}

// Use the cyber shield as the default
export const ShieldLogo = CyberShieldLogo;
export const ModernShieldLogo = CyberShieldLogo;

export function ShieldIcon({ className = "h-6 w-6", variant = "default" }) {
  const variants = {
    default: "text-cyan-500",
    primary: "text-blue-500",
    danger: "text-red-500",
    warning: "text-amber-500",
    success: "text-green-500",
    security: "text-slate-500"
  };

  return (
    <svg
      viewBox="0 0 32 32"
      fill="none"
      className={`${className} ${variants[variant]}`}
    >
      <path
        d="M16 2L6 6v8c0 8 6 14 10 16 4-2 10-8 10-16V6l-10-4z"
        fill="currentColor"
        fillOpacity="0.1"
        stroke="currentColor"
        strokeWidth="1.5"
      />
      <path
        d="M11 16l3 3 6-6"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
    </svg>
  );
}
