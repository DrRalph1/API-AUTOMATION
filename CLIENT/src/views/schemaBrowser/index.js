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
  Compass
} from 'lucide-react';

// Import database icons
import { SiPostgresql as SiOracle, SiPostgresql, SiMariadb, SiApachecassandra, SiElasticsearch, SiFirebird, SiElasticsearch as SiAmazondynamodb, SiMysql, SiPostgresql as SiMicrosoftsqlserver, SiMongodb, SiRedis } from 'react-icons/si';

// Import the specific schema browser components
import OracleSchemaBrowser from './OracleSchemaBrowser.js';
import PostgreSQLSchemaBrowser from './PostgreSQLSchemaBrowser.js';

// Enhanced Database Card Component with glass morphism
const DatabaseCard = ({ 
  title, 
  icon: Icon, 
  color, 
  onClick, 
  isDark,
  colors,
  description,
  isAvailable
}) => {
  const [isHovered, setIsHovered] = useState(false);
  
  return (
    <div
      className="group relative cursor-pointer transition-all duration-500 overflow-hidden"
      style={{
        background: isDark 
          ? `linear-gradient(135deg, ${colors.card} 0%, ${colors.cardGradient} 100%)`
          : colors.card,
        backdropFilter: 'blur(10px)',
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
      onClick={onClick}
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
              className="group flex items-center gap-2 px-4 py-2 rounded-xl transition-all duration-300 hover:scale-105"
              style={{
                color: colors.textSecondary,
                background: isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.03)',
                border: `1px solid ${colors.border}`,
                backdropFilter: 'blur(8px)'
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
                    onClick={() => hasComponent && onSelectDatabase(db)}
                    disabled={!hasComponent}
                    className={`group relative px-4 py-2 rounded-xl transition-all duration-300 ${
                      hasComponent ? 'cursor-pointer hover:scale-105' : 'cursor-not-allowed opacity-50'
                    }`}
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
                    
                    {/* Tooltip on hover */}
                    {!hasComponent && (
                      <div className="absolute -top-8 left-1/2 transform -translate-x-1/2 px-2 py-1 rounded text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-50"
                        style={{
                          background: colors.card,
                          border: `1px solid ${colors.border}`,
                          color: colors.text
                        }}
                      >
                        Coming soon
                      </div>
                    )}
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
    <div className="text-center mb-16">
      <div className="relative inline-block mb-6">
        <div 
          className="absolute inset-0 rounded-2xl blur-xl animate-pulse"
          style={{ background: `radial-gradient(circle, ${colors.primary}40, transparent 70%)` }}
        />
        <div 
          className="relative p-4 rounded-2xl"
          style={{ 
            background: `linear-gradient(135deg, ${colors.primary}20, ${colors.primary}05)`,
            backdropFilter: 'blur(10px)'
          }}
        >
          <Database size={56} style={{ color: colors.primary }} />
        </div>
      </div>
      <h1 className="text-5xl font-bold mb-4 bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 bg-clip-text text-transparent">
        Schema Browser
      </h1>
      <p className="text-lg max-w-2xl mx-auto" style={{ color: colors.textSecondary }}>
        Explore and manage your database schemas with an intuitive, modern interface
      </p>
    </div>
  );
};

// Stats Card Component
const StatsCard = ({ icon: Icon, label, value, color, colors }) => {
  return (
    <div 
      className="rounded-2xl p-4 backdrop-blur-sm transition-all duration-300 hover:scale-105"
      style={{
        background: `linear-gradient(135deg, ${color}15, ${color}05)`,
        border: `1px solid ${color}30`
      }}
    >
      <div className="flex items-center gap-3">
        <Icon size={24} style={{ color }} />
        <div>
          <p className="text-2xl font-bold" style={{ color: colors.text }}>{value}</p>
          <p className="text-xs" style={{ color: colors.textSecondary }}>{label}</p>
        </div>
      </div>
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
  const [isInitialized, setIsInitialized] = useState(false);

  // Enhanced color scheme
  const colors = useMemo(() => isDark ? {
    bg: 'rgb(1 14 35)',
    bgGradient: 'radial-gradient(circle at 10% 20%, rgb(17 34 64), rgb(1 14 35))',
    card: 'rgba(41, 53, 72, 0.3)',
    cardGradient: 'rgba(41, 53, 72, 0.1)',
    text: '#F1F5F9',
    textSecondary: 'rgb(148 163 184)',
    textTertiary: 'rgb(100 116 139)',
    border: 'rgba(51, 65, 85, 0.3)',
    hover: 'rgba(45, 46, 72, 0.5)',
    selected: 'rgb(44 82 130)',
    primary: 'rgb(96 165 250)',
    primaryDark: 'rgb(37 99 235)',
    success: 'rgb(52 211 153)',
    warning: 'rgb(251 191 36)',
    error: 'rgb(248 113 113)',
    inputBg: 'rgba(41, 53, 72, 0.5)',
    inputBorder: 'rgba(51, 65, 85, 0.3)',
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
    bgGradient: 'linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%)',
    card: '#ffffff',
    cardGradient: '#ffffff',
    text: '#1e293b',
    textSecondary: '#64748b',
    textTertiary: '#94a3b8',
    border: '#e2e8f0',
    hover: '#f1f5f9',
    selected: '#dbeafe',
    primary: '#1e293b',
    primaryDark: '#2563eb',
    success: '#10b981',
    warning: '#f59e0b',
    error: '#ef4444',
    inputBg: '#ffffff',
    inputBorder: '#e2e8f0',
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

  // Calculate available databases count
  const availableCount = databases.filter(db => db.available).length;

  // Handle database selection
  const handleDatabaseSelect = (database) => {
    if (database && database.component) {
      setSelectedDatabase(database);
      setError(null);
    } else if (database === null) {
      setSelectedDatabase(null);
    } else {
      setError(`${database.title} schema browser is coming soon! We're working hard to add support for this database.`);
      setTimeout(() => setError(null), 4000);
    }
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

  // Mark as initialized on mount
  useEffect(() => {
    setIsInitialized(true);
  }, []);

  // If a database is selected, render its component with top navigation
  if (selectedDatabase) {
    const SelectedComponent = selectedDatabase.component;
    return (
      <div className="min-h-screen" style={{ background: colors.bgGradient }}>
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
          isDark={isDark}
          colors={colors}
        />
        
        {/* Render the selected database schema browser */}
        <SelectedComponent 
          theme={theme}
          isDark={isDark}
          toggleTheme={toggleTheme}
          authToken={authToken}
          customTheme={customTheme}
          setCustomTheme={setCustomTheme}
        />
      </div>
    );
  }

  // Show enhanced database selection grid
  return (
    <div 
      className="min-h-screen relative overflow-hidden"
      style={{ background: colors.bgGradient }}
    >
      {/* Animated background elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-80 h-80 rounded-full blur-3xl opacity-20 animate-pulse"
          style={{ background: 'radial-gradient(circle, #60a5fa, transparent)' }} />
        <div className="absolute -bottom-40 -left-40 w-80 h-80 rounded-full blur-3xl opacity-20 animate-pulse animation-delay-1000"
          style={{ background: 'radial-gradient(circle, #8b5cf6, transparent)' }} />
      </div>
      
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 relative z-10">
        {/* Welcome Header */}
        <WelcomeHeader colors={colors} />
        
        {/* Stats Overview */}
        <div className="mb-12 max-w-2xl mx-auto">
          <div className="grid grid-cols-3 gap-4">
            <StatsCard 
              icon={Database} 
              label="Available Databases" 
              value={availableCount}
              color={colors.success}
              colors={colors}
            />
            <StatsCard 
              icon={TrendingUp} 
              label="Total Schemas" 
              value="8"
              color={colors.primary}
              colors={colors}
            />
            <StatsCard 
              icon={Zap} 
              label="Active Connections" 
              value="3"
              color={colors.warning}
              colors={colors}
            />
          </div>
        </div>
        
        {/* Enhanced Search Bar */}
        <div className="mb-12 max-w-2xl mx-auto">
          <div className="relative group">
            <Search 
              size={20} 
              className="absolute left-4 top-1/2 transform -translate-y-1/2 transition-colors group-focus-within:text-blue-500"
              style={{ color: colors.textTertiary }}
            />
            <input
              type="text"
              placeholder="Search databases by name or description..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-12 pr-12 py-4 rounded-2xl transition-all duration-300 focus:outline-none focus:ring-2 focus:ring-blue-500/50"
              style={{
                background: colors.inputBg,
                border: `1px solid ${colors.inputBorder}`,
                color: colors.text,
                fontSize: '15px',
                backdropFilter: 'blur(10px)'
              }}
            />
            {searchQuery && (
              <button
                onClick={() => setSearchQuery('')}
                className="absolute right-4 top-1/2 transform -translate-y-1/2 hover:scale-110 transition-transform"
              >
                <XCircle size={18} style={{ color: colors.textTertiary }} />
              </button>
            )}
          </div>
        </div>
        
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
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredDatabases.map(db => (
            <DatabaseCard
              key={db.id}
              title={db.title}
              icon={db.icon}
              color={db.color}
              description={db.description}
              onClick={db.available ? () => handleDatabaseSelect(db) : null}
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
              className="px-6 py-2 rounded-xl transition-all duration-300 hover:scale-105 font-medium"
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
        
        {/* Enhanced Coming Soon Section */}
        {filteredDatabases.some(db => !db.available) && searchQuery === '' && (
          <div className="mt-16 pt-8">
            <div className="text-center mb-6">
              <h3 className="text-xl font-semibold mb-2" style={{ color: colors.text }}>
                More databases coming soon!
              </h3>
              <p className="text-sm" style={{ color: colors.textSecondary }}>
                We're constantly adding support for new databases
              </p>
            </div>
            <div className="flex flex-wrap justify-center gap-3">
              {databases.filter(db => !db.available).map(db => (
                <div
                  key={db.id}
                  className="group px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 hover:scale-105 cursor-default backdrop-blur-sm"
                  style={{
                    background: `${db.color}15`,
                    color: db.color,
                    border: `1px solid ${db.color}30`
                  }}
                >
                  {db.title}
                </div>
              ))}
            </div>
          </div>
        )}
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
        
        .animate-slide-in {
          animation: slide-in 0.3s ease-out;
        }
        
        .animate-slide-down {
          animation: slide-down 0.3s ease-out;
        }
        
        .animate-fade-in {
          animation: fade-in 0.5s ease-out;
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
      `}</style>
    </div>
  );
};

export default SchemaBrowserIndex;