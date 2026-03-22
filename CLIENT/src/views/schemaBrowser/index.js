// SchemaBrowserIndex.js - Enhanced UI with modern design
import React, { useState, useMemo, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Database,
  ChevronRight,
  Grid,
  List,
  Search,
  XCircle,
  AlertCircle,
  Clock,
  ShieldCheck,
  Code,
  ArrowLeft,
  Sparkles,
  Star,
  TrendingUp,
  Zap,
  Compass,
  Rocket,
  Calendar,
  Mail,
  X
} from 'lucide-react';

// Import database icons
import { SiPostgresql as SiOracle, SiPostgresql, SiMariadb, SiApachecassandra, SiElasticsearch, SiFirebird, SiElasticsearch as SiAmazondynamodb, SiMysql, SiPostgresql as SiMicrosoftsqlserver, SiMongodb, SiRedis } from 'react-icons/si';

// Import the specific schema browser components
import OracleSchemaBrowser from './OracleSchemaBrowser.js';
import PostgreSQLSchemaBrowser from './PostgreSQLSchemaBrowser.js';

// Coming Soon Modal Component
const ComingSoonModal = ({ isOpen, onClose, database, colors, isDark }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-fade-in">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />
      
      {/* Modal */}
      <div 
        className="relative max-w-md w-full rounded-2xl shadow-2xl transform transition-all duration-300 animate-scale-in"
        style={{
          background: colors.card,
          border: `1px solid ${colors.border}`,
          backdropFilter: isDark ? 'blur(20px)' : 'none'
        }}
      >
        {/* Gradient Header */}
        <div className="h-1.5 rounded-t-2xl bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500" />
        
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1 rounded-lg transition-all duration-300 hover:scale-110 hover-lift"
          style={{
            color: colors.textTertiary,
            background: `${colors.hover}`,
            border: `1px solid ${colors.border}`
          }}
        >
          <X size={18} />
        </button>
        
        {/* Modal Content */}
        <div className="p-6 text-center">
          {/* Icon */}
          <div className="flex justify-center mb-4">
            <div 
              className="p-3 rounded-full animate-pulse"
              style={{
                background: `linear-gradient(135deg, ${database.color}20, ${database.color}05)`,
                border: `1px solid ${database.color}40`
              }}
            >
              <database.icon size={48} style={{ color: database.color }} />
            </div>
          </div>
          
          {/* Title */}
          <h3 className="text-2xl font-bold mb-2" style={{ color: colors.text }}>
            {database.title}
          </h3>
          
          {/* Coming Soon Badge */}
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full mb-4">
            <Rocket size={16} style={{ color: colors.warning }} />
            <span className="text-sm font-semibold" style={{ color: colors.warning }}>
              Coming Soon
            </span>
          </div>
          
          {/* Description */}
          <p className="text-sm mb-6" style={{ color: colors.textSecondary }}>
            We're working hard to bring you the {database.title} schema browser. 
            This feature is currently under development and will be available soon!
          </p>
          
          {/* Feature List */}
          <div className="mb-6 p-4 rounded-xl" style={{ background: `${colors.hover}` }}>
            <p className="text-xs font-medium mb-2" style={{ color: colors.textTertiary }}>
              Coming features include:
            </p>
            <div className="grid grid-cols-2 gap-2 text-xs">
              {['Table Browser', 'Schema Viewer', 'Query Builder', 'Data Export', 'Performance Stats', 'Index Manager'].map(feature => (
                <div key={feature} className="flex items-center gap-1.5">
                  <Sparkles size={10} style={{ color: colors.primary }} />
                  <span style={{ color: colors.textSecondary }}>{feature}</span>
                </div>
              ))}
            </div>
          </div>
          
          {/* Estimated Timeline */}
          <div className="flex items-center justify-center gap-2 mb-6 p-3 rounded-xl" style={{ background: `${colors.primary}10` }}>
            <Calendar size={16} style={{ color: colors.primary }} />
            <span className="text-xs" style={{ color: colors.textSecondary }}>
              Estimated release: Q2 2026
            </span>
          </div>
          
          {/* Action Buttons */}
          <div className="flex gap-3">
            <button
              onClick={onClose}
              className="flex-1 px-4 py-2.5 rounded-xl font-medium transition-all duration-300 hover:scale-105 hover-lift"
              style={{
                background: colors.hover,
                border: `1px solid ${colors.border}`,
                color: colors.text
              }}
            >
              Close
            </button>
            <button
              onClick={() => {
                // You can add notification subscription logic here
                alert(`We'll notify you when ${database.title} becomes available!`);
                onClose();
              }}
              className="flex-1 px-4 py-2.5 rounded-xl font-medium transition-all duration-300 hover:scale-105 hover-lift flex items-center justify-center gap-2"
              style={{
                background: `linear-gradient(135deg, ${database.color}, ${database.color}CC)`,
                color: '#ffffff'
              }}
            >
              <Mail size={16} />
              Notify Me
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

// Enhanced Database Card Component with glass morphism
const DatabaseCard = ({ 
  title, 
  icon: Icon, 
  color, 
  onClick, 
  onComingSoonClick,
  isDark,
  colors,
  description,
  isAvailable
}) => {
  const [isHovered, setIsHovered] = useState(false);
  
  const handleClick = () => {
    if (isAvailable && onClick) {
      onClick();
    } else if (!isAvailable && onComingSoonClick) {
      onComingSoonClick();
    }
  };
  
  return (
    <div
      className="group relative cursor-pointer transition-all duration-500 overflow-hidden hover-lift"
      style={{
        background: isDark 
          ? `linear-gradient(135deg, ${colors.card} 0%, ${colors.cardGradient} 100%)`
          : colors.card,
        backdropFilter: isDark ? 'blur(10px)' : 'none',
        borderRadius: '24px',
        border: `1px solid ${isHovered ? color : colors.border}`,
        transform: isHovered ? 'translateY(-8px) scale(1.02)' : 'translateY(0) scale(1)',
        boxShadow: isHovered 
          ? `0 25px 40px -12px ${isDark ? 'rgba(0,0,0,0.5)' : 'rgba(0,0,0,0.2)'}, 0 0 0 1px ${color}40`
          : `0 4px 6px -1px ${isDark ? 'rgba(0,0,0,0.3)' : 'rgba(0,0,0,0.1)'}`,
        transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)'
      }}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onClick={handleClick}
    >
      {/* Gradient overlay on hover */}
      <div 
        className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none"
        style={{
          background: `radial-gradient(circle at top right, ${color}20, transparent 70%)`,
          borderRadius: '24px'
        }}
      />
      
      <div className="p-8 text-center relative z-10">
        {/* Large Database Icon with floating animation */}
        <div className="flex justify-center mb-6">
          <div 
            className="p-4 rounded-2xl transition-all duration-500"
            style={{
              background: `linear-gradient(135deg, ${color}20, ${color}05)`,
              transform: isHovered ? 'scale(1.15) rotate(3deg)' : 'scale(1) rotate(0deg)',
              boxShadow: isHovered ? `0 10px 25px -5px ${color}40` : 'none'
            }}
          >
            <Icon size={56} style={{ color: color, filter: isHovered ? 'drop-shadow(0 4px 8px rgba(0,0,0,0.2))' : 'none' }} />
          </div>
        </div>
        
        {/* Database Name */}
        <h3 className="text-2xl font-bold mb-2 tracking-tight" style={{ color: colors.text }}>
          {title}
        </h3>
        
        {/* Description */}
        {description && (
          <p className="text-sm mb-4 line-clamp-2" style={{ color: colors.textSecondary }}>
            {description}
          </p>
        )}
        
        {/* Status Badge */}
        {!isAvailable && (
          <span 
            className="inline-flex items-center gap-1 text-xs px-3 py-1.5 rounded-full backdrop-blur-sm"
            style={{
              background: `${colors.warning}20`,
              color: colors.warning,
              border: `1px solid ${colors.warning}30`
            }}
          >
            <Clock size={12} />
            Coming Soon
          </span>
        )}
        
        {/* Click indicator with animation */}
        {isAvailable && (
          <div className="flex items-center justify-center gap-2 mt-4 opacity-0 group-hover:opacity-100 transition-all duration-300 group-hover:translate-y-0 translate-y-2">
            <span className="text-xs font-medium" style={{ color: colors.textTertiary }}>
              Explore Schema
            </span>
            <ChevronRight 
              size={16} 
              style={{ 
                color: color,
                transform: isHovered ? 'translateX(6px)' : 'translateX(0)',
                transition: 'transform 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
              }} 
            />
          </div>
        )}
      </div>
    </div>
  );
};

// Enhanced Top Navigation Bar with glass morphism
const DatabaseTopNav = ({ 
  databases, 
  selectedDatabaseId, 
  onSelectDatabase, 
  onBackToSelection,
  onComingSoonClick,
  isDark, 
  colors 
}) => {
  const [isScrolled, setIsScrolled] = useState(false);

  React.useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 10);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const handleDatabaseClick = (db) => {
    const hasComponent = !!db.component;
    if (hasComponent) {
      onSelectDatabase(db);
    } else {
      onComingSoonClick(db); // This will show the coming soon modal
    }
  };

  return (
    <div 
      className={`sticky top-0 z-20 transition-all duration-500 ${
        isScrolled ? 'backdrop-blur-xl shadow-xl' : 'backdrop-blur-md'
      }`}
      style={{
        background: isScrolled 
          ? isDark ? 'rgba(1, 14, 35, 0.95)' : 'rgba(248, 250, 252, 0.95)'
          : isDark ? 'rgba(1, 14, 35, 0.8)' : 'rgba(248, 250, 252, 0.8)',
        borderBottom: `1px solid ${colors.border}`,
      }}
    >
      <div className="max-w-full mx-auto px-4 sm:px-6 lg:px-8">
        <div className="py-3">
          <div className="flex items-center justify-between gap-4">
            {/* Enhanced Back Button */}
            <button
              onClick={onBackToSelection}
              className="group flex items-center gap-2 px-4 py-2 rounded-xl transition-all duration-300 hover:scale-105 hover-lift"
              style={{
                color: colors.textSecondary,
                background: isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.03)',
                border: `1px solid ${colors.border}`,
                backdropFilter: isDark ? 'blur(8px)' : 'none'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.background = colors.hover;
                e.currentTarget.style.color = colors.text;
                e.currentTarget.style.transform = 'translateX(-4px)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.background = isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.03)';
                e.currentTarget.style.color = colors.textSecondary;
                e.currentTarget.style.transform = 'translateX(0)';
              }}
            >
              <ArrowLeft size={18} className="group-hover:-translate-x-1 transition-transform" />
              <span className="text-sm font-medium hidden sm:inline">All Databases</span>
            </button>
            
            {/* Database Icons with enhanced styling */}
            <div className="flex flex-wrap items-center justify-center gap-2 flex-1">
              {databases.map(db => {
                const isSelected = selectedDatabaseId === db.id;
                const hasComponent = !!db.component;
                
                return (
                  <button
                    key={db.id}
                    onClick={() => handleDatabaseClick(db)}
                    className={`group relative px-4 py-2 rounded-xl transition-all duration-300 hover:scale-105 hover-lift`}
                    style={{
                      background: isSelected 
                        ? `linear-gradient(135deg, ${db.color}20, ${db.color}10)`
                        : 'transparent',
                      border: `1px solid ${isSelected ? db.color : colors.border}`,
                      transform: isSelected ? 'translateY(-2px)' : 'translateY(0)'
                    }}
                  >
                    <div className="flex items-center gap-2">
                      <db.icon size={20} style={{ color: db.color }} />
                      <span className="text-sm font-medium hidden sm:inline" style={{ color: colors.text }}>
                        {db.title.split(' ')[0]}
                      </span>
                      {!hasComponent && (
                        <span 
                          className="text-xs px-1.5 py-0.5 rounded-full hidden sm:inline backdrop-blur-sm"
                          style={{
                            background: `${colors.warning}20`,
                            color: colors.warning
                          }}
                        >
                          Soon
                        </span>
                      )}
                      {isSelected && (
                        <div 
                          className="w-1.5 h-1.5 rounded-full animate-pulse"
                          style={{ backgroundColor: db.color }}
                        />
                      )}
                    </div>
                  </button>
                );
              })}
            </div>
            
            {/* Spacer for balance */}
            <div className="w-20 hidden sm:block"></div>
          </div>
        </div>
      </div>
    </div>
  );
};

// Enhanced Welcome Header with animated gradient
const WelcomeHeader = ({ colors }) => {
  return (
    <div className="text-center mb-16 animate-fade-in">
      <div className="relative inline-block mb-6">
        <div 
          className="absolute inset-0 rounded-2xl blur-xl animate-pulse"
          style={{ background: `radial-gradient(circle, ${colors.primary}40, transparent 70%)` }}
        />
      </div>
      <h1 className="text-4xl font-bold mb-4 text-transparent uppercase" style={{ color: colors.textSecondary }}>
        Schema Browser
      </h1>
      <p className="text-lg max-w-2xl mx-auto" style={{ color: colors.textSecondary }}>
        Explore and manage your database schemas with an intuitive, modern interface
      </p>
    </div>
  );
};

// Main Schema Browser Index Component
const SchemaBrowserIndex = ({ 
  theme, 
  isDark, 
  toggleTheme, 
  authToken,
  customTheme,
  setCustomTheme
}) => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [isGridView, setIsGridView] = useState(true);
  const [error, setError] = useState(null);
  const [selectedDatabase, setSelectedDatabase] = useState(null);
  const [comingSoonModal, setComingSoonModal] = useState({ isOpen: false, database: null });
  const [isInitialized, setIsInitialized] = useState(false);

  // Enhanced color scheme - MATCHING LOGIN COMPONENT EXACTLY
  const colors = useMemo(() => isDark ? {
    bg: 'rgb(1 14 35)',
    white: '#FFFFFF',
    sidebar: 'rgb(41 53 72 / 19%)',
    main: 'rgb(1 14 35)',
    header: 'rgb(20 26 38)',
    card: 'rgb(41 53 72 / 19%)',
    cardGradient: 'rgba(41, 53, 72, 0.1)',
    
    text: '#F1F5F9',
    textSecondary: 'rgb(148 163 184)',
    textTertiary: 'rgb(100 116 139)',
    
    border: 'rgb(51 65 85 / 19%)',
    borderLight: 'rgb(45 55 72)',
    borderDark: 'rgb(71 85 105)',
    
    hover: 'rgb(45 46 72 / 33%)',
    active: 'rgb(59 74 99)',
    selected: 'rgb(44 82 130)',
    
    primary: 'rgb(96 165 250)',
    primaryLight: 'rgb(147 197 253)',
    primaryDark: 'rgb(37 99 235)',
    
    success: 'rgb(52 211 153)',
    warning: 'rgb(251 191 36)',
    error: 'rgb(248 113 113)',
    info: 'rgb(96 165 250)',
    
    tabActive: 'rgb(96 165 250)',
    tabInactive: 'rgb(148 163 184)',
    sidebarActive: 'rgb(96 165 250)',
    sidebarhover: 'rgb(45 46 72 / 33%)',
    inputBg: 'rgb(41 53 72 / 19%)',
    inputborder: 'rgb(51 65 85 / 19%)',
    tableHeader: 'rgb(41 53 72 / 19%)',
    tableRow: 'rgb(41 53 72 / 19%)',
    tableRowhover: 'rgb(45 46 72 / 33%)',
    dropdownBg: 'rgb(41 53 72 / 19%)',
    dropdownborder: 'rgb(51 65 85 / 19%)',
    modalBg: 'rgb(41 53 72 / 19%)',
    modalborder: 'rgb(51 65 85 / 19%)',
    codeBg: 'rgb(41 53 72 / 19%)',
    
    connectionOnline: 'rgb(52 211 153)',
    connectionOffline: 'rgb(248 113 113)',
    connectionIdle: 'rgb(251 191 36)',
    
    accentPurple: 'rgb(167 139 250)',
    accentPink: 'rgb(244 114 182)',
    accentCyan: 'rgb(34 211 238)',
    
    gradient: 'from-blue-500/20 via-violet-500/20 to-orange-500/20',
    
    objectType: {
      oracle: '#60a5fa',
      postgresql: '#3b82f6',
      mysql: '#10b981',
      sqlserver: '#8b5cf6',
      mongodb: '#10b981',
      redis: '#f59e0b',
      mariadb: '#c97b2a',
      cassandra: '#e74c3c',
      cockroachdb: '#ff6c37',
      elasticsearch: '#10b981',
      dynamodb: '#ff9900',
      firebird: '#e05a3a'
    }
  } : {
    bg: '#f8fafc',
    white: '#f8fafc',
    sidebar: '#ffffff',
    main: '#f8fafc',
    header: '#ffffff',
    card: '#ffffff',
    cardGradient: '#ffffff',
    text: '#1e293b',
    textSecondary: '#64748b',
    textTertiary: '#94a3b8',
    border: '#e2e8f0',
    borderLight: '#f1f5f9',
    borderDark: '#cbd5e1',
    hover: '#f1f5f9',
    active: '#e2e8f0',
    selected: '#dbeafe',
    primary: '#1e293b',
    primaryLight: '#60a5fa',
    primaryDark: '#2563eb',
    success: '#10b981',
    warning: '#f59e0b',
    error: '#ef4444',
    info: '#3b82f6',
    tabActive: '#3b82f6',
    tabInactive: '#64748b',
    sidebarActive: '#3b82f6',
    sidebarHover: '#f1f5f9',
    inputBg: '#ffffff',
    inputBorder: '#e2e8f0',
    tableHeader: '#f8fafc',
    tableRow: '#ffffff',
    tableRowHover: '#f8fafc',
    dropdownBg: '#ffffff',
    dropdownBorder: '#e2e8f0',
    modalBg: '#ffffff',
    modalBorder: '#e2e8f0',
    codeBg: '#f1f5f9',
    connectionOnline: '#10b981',
    connectionOffline: '#ef4444',
    connectionIdle: '#f59e0b',
    gradient: 'from-blue-400/20 via-violet-400/20 to-orange-400/20',
    objectType: {
      oracle: '#3b82f6',
      postgresql: '#10b981',
      mysql: '#f59e0b',
      sqlserver: '#8b5cf6',
      mongodb: '#10b981',
      redis: '#ef4444',
      mariadb: '#c97b2a',
      cassandra: '#e74c3c',
      cockroachdb: '#ff6c37',
      elasticsearch: '#10b981',
      dynamodb: '#ff9900',
      firebird: '#e05a3a'
    }
  }, [isDark]);

  // Database configurations with descriptions
  const databases = [
    {
      id: 'oracle',
      title: 'Oracle Database',
      icon: Database,
      color: colors.objectType.oracle,
      component: OracleSchemaBrowser,
      description: 'Enterprise-grade relational database with advanced security and performance',
      available: true,
      stats: { tables: 245, views: 67, procedures: 123 }
    },
    {
      id: 'postgresql',
      title: 'PostgreSQL',
      icon: SiPostgresql,
      color: colors.objectType.postgresql,
      component: PostgreSQLSchemaBrowser,
      description: 'Advanced open-source relational database with JSON support',
      available: true,
      stats: { tables: 189, views: 45, procedures: 89 }
    },
    {
      id: 'mysql',
      title: 'MySQL',
      icon: SiMysql,
      color: colors.objectType.mysql,
      component: null,
      description: 'Popular open-source relational database for web applications',
      available: false,
      stats: null
    },
    {
      id: 'mongodb',
      title: 'MongoDB',
      icon: SiMongodb,
      color: colors.objectType.mongodb,
      component: null,
      description: 'Leading NoSQL document database for modern applications',
      available: false,
      stats: null
    },
    {
      id: 'cassandra',
      title: 'Cassandra',
      icon: SiApachecassandra,
      color: colors.objectType.cassandra,
      component: null,
      description: 'Distributed NoSQL database for high availability and scalability',
      available: false,
      stats: null
    },
    {
      id: 'redis',
      title: 'Redis',
      icon: SiRedis,
      color: colors.objectType.redis,
      component: null,
      description: 'In-memory data structure store for caching and real-time apps',
      available: false,
      stats: null
    },
    {
      id: 'mariadb',
      title: 'MariaDB',
      icon: SiMariadb,
      color: colors.objectType.mariadb,
      component: null,
      description: 'MySQL-compatible relational database with enhanced features',
      available: false,
      stats: null
    },
    {
      id: 'dynamodb',
      title: 'DynamoDB',
      icon: SiAmazondynamodb,
      color: colors.objectType.dynamodb,
      component: null,
      description: 'AWS NoSQL database service for serverless applications',
      available: false,
      stats: null
    }
  ];

  // Filter databases based on search query
  const filteredDatabases = useMemo(() => {
    if (!searchQuery.trim()) return databases;
    const query = searchQuery.toLowerCase();
    return databases.filter(db => 
      db.title.toLowerCase().includes(query) ||
      db.description.toLowerCase().includes(query)
    );
  }, [searchQuery, databases]);

  // Handle database selection
  const handleDatabaseSelect = (database) => {
    if (database && database.component) {
      setSelectedDatabase(database);
      setError(null);
    }
  };

  // Handle coming soon click
  const handleComingSoonClick = (database) => {
    setComingSoonModal({ isOpen: true, database });
  };

  // Handle back to selection
  const handleBackToSelection = () => {
    setSelectedDatabase(null);
    setError(null);
  };

  // Clear error
  const clearError = () => {
    setError(null);
  };

  // Close modal
  const closeModal = () => {
    setComingSoonModal({ isOpen: false, database: null });
  };

  // Mark as initialized on mount
  useEffect(() => {
    setIsInitialized(true);
  }, []);

  // If a database is selected, render its component with top navigation
  if (selectedDatabase) {
    const SelectedComponent = selectedDatabase.component;
    return (
      <div className="min-h-screen relative overflow-hidden" style={{ backgroundColor: colors.bg }}>
        {/* Animated Background Elements */}
        <div className="absolute inset-0 overflow-hidden">
          <div className={`absolute -top-40 -right-40 w-80 h-80 rounded-full bg-gradient-to-br ${colors.gradient} blur-3xl animate-pulse`}></div>
          <div className={`absolute -bottom-40 -left-40 w-80 h-80 rounded-full bg-gradient-to-br ${colors.gradient} blur-3xl animate-pulse delay-1000`}></div>
        </div>

        {/* Enhanced Error Toast */}
        {error && (
          <div className="fixed top-20 right-4 z-50 animate-slide-in">
            <div 
              className="flex items-center gap-3 px-4 py-3 rounded-xl shadow-2xl backdrop-blur-lg"
              style={{
                background: colors.card,
                borderLeft: `4px solid ${colors.error}`,
                boxShadow: isDark ? '0 8px 32px rgba(0,0,0,0.3)' : '0 8px 32px rgba(0,0,0,0.1)'
              }}
            >
              <AlertCircle size={20} style={{ color: colors.error }} />
              <p className="text-sm font-medium" style={{ color: colors.text }}>{error}</p>
              <button onClick={clearError} className="ml-2 hover:scale-110 transition-transform">
                <XCircle size={16} style={{ color: colors.textTertiary }} />
              </button>
            </div>
          </div>
        )}
        
        {/* Top Navigation Bar with Database Icons */}
        <DatabaseTopNav
          databases={databases}
          selectedDatabaseId={selectedDatabase.id}
          onSelectDatabase={handleDatabaseSelect}
          onBackToSelection={handleBackToSelection}
          onComingSoonClick={handleComingSoonClick}
          isDark={isDark}
          colors={colors}
        />
        
        {/* Render the selected database schema browser */}
        <div className="relative z-10">
          <SelectedComponent 
            theme={theme}
            isDark={isDark}
            toggleTheme={toggleTheme}
            authToken={authToken}
            customTheme={customTheme}
            setCustomTheme={setCustomTheme}
          />
        </div>

        {/* Coming Soon Modal */}
        <ComingSoonModal
          isOpen={comingSoonModal.isOpen}
          onClose={closeModal}
          database={comingSoonModal.database || databases[0]}
          colors={colors}
          isDark={isDark}
        />
      </div>
    );
  }

  // Show enhanced database selection grid
  return (
    <div 
      className="min-h-screen relative overflow-hidden"
      style={{ backgroundColor: colors.bg }}
    >
      {/* Animated Background Elements - Matching Login component */}
      <div className="absolute inset-0 overflow-hidden">
        <div className={`absolute -top-40 -right-40 w-80 h-80 rounded-full bg-gradient-to-br ${colors.gradient} blur-3xl animate-pulse`}></div>
        <div className={`absolute -bottom-40 -left-40 w-80 h-80 rounded-full bg-gradient-to-br ${colors.gradient} blur-3xl animate-pulse delay-1000`}></div>
      </div>
      
      <div 
        className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 relative z-10" 
        style={{ zoom: 0.9 }}
      >
        {/* Welcome Header */}
        <WelcomeHeader colors={colors} />
      
        {/* Enhanced Error Display */}
        {error && (
          <div className="max-w-2xl mx-auto mb-6 animate-slide-down">
            <div 
              className="flex items-center gap-3 px-4 py-3 rounded-xl backdrop-blur-sm"
              style={{
                background: `${colors.error}15`,
                border: `1px solid ${colors.error}40`
              }}
            >
              <AlertCircle size={20} style={{ color: colors.error }} />
              <p className="text-sm" style={{ color: colors.text }}>{error}</p>
              <button onClick={clearError} className="ml-auto hover:scale-110 transition-transform">
                <XCircle size={16} style={{ color: colors.textTertiary }} />
              </button>
            </div>
          </div>
        )}
        
        {/* Enhanced Database Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
          {filteredDatabases.map(db => (
            <DatabaseCard
              key={db.id}
              title={db.title}
              icon={db.icon}
              color={db.color}
              description={db.description}
              onClick={() => handleDatabaseSelect(db)}
              onComingSoonClick={() => handleComingSoonClick(db)}
              isAvailable={db.available}
              isDark={isDark}
              colors={colors}
            />
          ))}
        </div>
        
        {/* Enhanced Empty State */}
        {filteredDatabases.length === 0 && (
          <div className="text-center py-16 animate-fade-in">
            <div className="flex justify-center mb-6">
              <div className="p-4 rounded-full backdrop-blur-sm" style={{ background: `${colors.textTertiary}20` }}>
                <Compass size={56} style={{ color: colors.textTertiary }} />
              </div>
            </div>
            <p className="text-xl mb-2" style={{ color: colors.textSecondary }}>
              No databases found
            </p>
            <p className="text-sm mb-6" style={{ color: colors.textTertiary }}>
              We couldn't find any databases matching "{searchQuery}"
            </p>
            <button
              onClick={() => setSearchQuery('')}
              className="px-6 py-2 rounded-xl transition-all duration-300 hover:scale-105 font-medium hover-lift"
              style={{
                background: `linear-gradient(135deg, ${colors.primary}, ${colors.primaryDark})`,
                color: '#ffffff',
                boxShadow: `0 4px 15px ${colors.primary}40`
              }}
            >
              Clear Search
            </button>
          </div>
        )}

        {/* Coming Soon Modal */}
        <ComingSoonModal
          isOpen={comingSoonModal.isOpen}
          onClose={closeModal}
          database={comingSoonModal.database || databases[0]}
          colors={colors}
          isDark={isDark}
        />
      </div>
      
      {/* Enhanced animation styles */}
      <style jsx>{`
        @keyframes slide-in {
          from {
            transform: translateX(100%);
            opacity: 0;
          }
          to {
            transform: translateX(0);
            opacity: 1;
          }
        }
        
        @keyframes slide-down {
          from {
            transform: translateY(-100%);
            opacity: 0;
          }
          to {
            transform: translateY(0);
            opacity: 1;
          }
        }
        
        @keyframes fade-in {
          from {
            opacity: 0;
            transform: scale(0.95);
          }
          to {
            opacity: 1;
            transform: scale(1);
          }
        }
        
        @keyframes scale-in {
          from {
            opacity: 0;
            transform: scale(0.9);
          }
          to {
            opacity: 1;
            transform: scale(1);
          }
        }
        
        @keyframes shake {
          0%, 100% { transform: translateX(0); }
          10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
          20%, 40%, 60%, 80% { transform: translateX(5px); }
        }
        
        .animate-slide-in {
          animation: slide-in 0.3s ease-out;
        }
        
        .animate-slide-down {
          animation: slide-down 0.3s ease-out;
        }
        
        .animate-fade-in {
          animation: fade-in 0.5s ease-out;
        }
        
        .animate-scale-in {
          animation: scale-in 0.3s ease-out;
        }
        
        .animate-shake {
          animation: shake 0.5s ease-in-out;
        }
        
        .animation-delay-1000 {
          animation-delay: 1000ms;
        }
        
        @keyframes pulse-slow {
          0%, 100% {
            opacity: 0.2;
            transform: scale(1);
          }
          50% {
            opacity: 0.3;
            transform: scale(1.05);
          }
        }
        
        .animate-pulse-slow {
          animation: pulse-slow 4s cubic-bezier(0.4, 0, 0.6, 1) infinite;
        }
        
        .hover-lift:hover {
          transform: translateY(-2px);
          transition: transform 0.2s ease;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        * {
          transition: background-color 0.3s ease, border-color 0.3s ease, color 0.3s ease;
        }
        
        ::-webkit-scrollbar {
          width: 6px;
          height: 6px;
        }
        
        ::-webkit-scrollbar-track {
          background: rgb(51 65 85);
          border-radius: 4px;
        }
        
        ::-webkit-scrollbar-thumb {
          background: rgb(100 116 139);
          border-radius: 4px;
        }
        
        ::-webkit-scrollbar-thumb:hover {
          background: rgb(148 163 184);
        }
        
        @media (max-width: 640px) {
          .text-xs { font-size: 11px; }
          .text-sm { font-size: 12px; }
          .text-lg { font-size: 16px; }
          .text-xl { font-size: 18px; }
          .text-2xl { font-size: 20px; }
        }
        
        button, 
        [role="button"],
        input[type="submit"],
        input[type="button"],
        .cursor-pointer {
          cursor: pointer;
        }
        
        button:disabled,
        [role="button"]:disabled {
          cursor: not-allowed;
        }
      `}</style>
    </div>
  );
};

export default SchemaBrowserIndex;