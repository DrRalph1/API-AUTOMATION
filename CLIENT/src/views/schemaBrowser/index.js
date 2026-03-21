// SchemaBrowserIndex.js - Auto-select Oracle with top database navigation
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
  ArrowLeft
} from 'lucide-react';

// Import database icons
import { SiPostgresql as SiOracle, SiPostgresql, SiMariadb, SiApachecassandra, SiElasticsearch, SiFirebird, SiElasticsearch as SiAmazondynamodb, SiMysql, SiPostgresql as SiMicrosoftsqlserver, SiMongodb, SiRedis } from 'react-icons/si';

// Import the specific schema browser components
import OracleSchemaBrowser from './OracleSchemaBrowser.js';
import PostgreSQLSchemaBrowser from './PostgreSQLSchemaBrowser.js';

// Database Card Component - Clean version with icon focus
const DatabaseCard = ({ 
  title, 
  icon: Icon, 
  color, 
  onClick, 
  isDark,
  colors 
}) => {
  const [isHovered, setIsHovered] = useState(false);
  
  return (
    <div
      className="group relative cursor-pointer rounded-xl transition-all duration-300 overflow-hidden"
      style={{
        backgroundColor: colors.card,
        border: `1px solid ${isHovered ? color : colors.border}`,
        transform: isHovered ? 'translateY(-4px)' : 'translateY(0)',
        boxShadow: isHovered ? `0 20px 25px -12px ${isDark ? 'rgba(0,0,0,0.5)' : 'rgba(0,0,0,0.1)'}` : 'none'
      }}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onClick={onClick}
    >
      <div className="p-8 text-center">
        {/* Large Database Icon */}
        <div className="flex justify-center mb-6">
          <div 
            className="p-4 rounded-2xl transition-all duration-300"
            style={{
              backgroundColor: `${color}15`,
              transform: isHovered ? 'scale(1.1)' : 'scale(1)'
            }}
          >
            <Icon size={64} style={{ color: color }} />
          </div>
        </div>
        
        {/* Database Name */}
        <h3 className="text-xl font-semibold mb-2" style={{ color: colors.text }}>
          {title}
        </h3>
        
        {/* Click indicator */}
        <div className="flex items-center justify-center gap-2 mt-4 opacity-0 group-hover:opacity-100 transition-opacity">
          <span className="text-xs" style={{ color: colors.textTertiary }}>
            Browse schema
          </span>
          <ChevronRight 
            size={14} 
            style={{ 
              color: color,
              transform: isHovered ? 'translateX(4px)' : 'translateX(0)',
              transition: 'transform 0.2s ease'
            }} 
          />
        </div>
      </div>
    </div>
  );
};

// Top Navigation Bar Component - Shows database icons only
const DatabaseTopNav = ({ databases, selectedDatabaseId, onSelectDatabase, isDark, colors }) => {
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
      className={`sticky top-0 z-20 transition-all duration-300 ${
        isScrolled ? 'shadow-lg' : ''
      }`}
      style={{
        backgroundColor: colors.bg,
        borderBottom: `1px solid ${colors.border}`,
        backdropFilter: isScrolled ? 'blur(8px)' : 'none'
      }}
    >
      <div className="max-w-full mx-auto px-4 sm:px-6 lg:px-8">
        <div className="py-3">
          <div className="flex flex-wrap items-center justify-center gap-2">
            {databases.map(db => {
              const isSelected = selectedDatabaseId === db.id;
              const hasComponent = !!db.component;
              
              return (
                <button
                  key={db.id}
                  onClick={() => hasComponent && onSelectDatabase(db)}
                  disabled={!hasComponent}
                  className={`group relative px-4 py-2 rounded-lg transition-all duration-200 ${
                    hasComponent ? 'cursor-pointer' : 'cursor-not-allowed opacity-60'
                  }`}
                  style={{
                    backgroundColor: isSelected ? `${db.color}20` : 'transparent',
                    border: `1px solid ${isSelected ? db.color : colors.border}`,
                    transform: isSelected ? 'translateY(-2px)' : 'translateY(0)'
                  }}
                >
                  <div className="flex items-center gap-2">
                    <db.icon size={20} style={{ color: db.color }} />
                    <span className="text-sm font-medium" style={{ color: colors.text }}>
                      {db.title.split(' ')[0]}
                    </span>
                    {!hasComponent && (
                      <span 
                        className="text-xs px-1.5 py-0.5 rounded-full"
                        style={{
                          backgroundColor: `${colors.warning}20`,
                          color: colors.warning
                        }}
                      >
                        Soon
                      </span>
                    )}
                    {isSelected && (
                      <div 
                        className="w-1.5 h-1.5 rounded-full"
                        style={{ backgroundColor: db.color }}
                      />
                    )}
                  </div>
                  
                  {/* Tooltip on hover for disabled items */}
                  {/* {!hasComponent && (
                    <div className="absolute -top-8 left-1/2 transform -translate-x-1/2 px-2 py-1 rounded text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none"
                      style={{
                        backgroundColor: colors.card,
                        border: `1px solid ${colors.border}`,
                        color: colors.textSecondary
                      }}
                    >
                      {db.title} coming soon
                    </div>
                  )} */}
                </button>
              );
            })}
          </div>
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

  // Colors matching the theme
  const colors = useMemo(() => isDark ? {
    bg: 'rgb(1 14 35)',
    card: 'rgb(41 53 72 / 19%)',
    text: '#F1F5F9',
    textSecondary: 'rgb(148 163 184)',
    textTertiary: 'rgb(100 116 139)',
    border: 'rgb(51 65 85 / 19%)',
    hover: 'rgb(45 46 72 / 33%)',
    selected: 'rgb(44 82 130)',
    primary: 'rgb(96 165 250)',
    primaryDark: 'rgb(37 99 235)',
    success: 'rgb(52 211 153)',
    warning: 'rgb(251 191 36)',
    error: 'rgb(248 113 113)',
    inputBg: 'rgb(41 53 72 / 19%)',
    inputBorder: 'rgb(51 65 85 / 19%)',
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
    card: '#ffffff',
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

  // Database configurations with their icons
  const databases = [
  {
    id: 'oracle',
    title: 'Oracle Database',
    icon: Database,
    color: colors.objectType.oracle,
    component: OracleSchemaBrowser
  },
  {
    id: 'postgresql',
    title: 'PostgreSQL',
    icon: SiPostgresql,
    color: colors.objectType.postgresql,
    component: PostgreSQLSchemaBrowser
  },
  {
    id: 'mysql',
    title: 'MySQL',
    icon: SiMysql,
    color: colors.objectType.mysql,
    component: null
  },
  // {
  //   id: 'sqlserver',
  //   title: 'SQL Server',
  //   icon: SiMicrosoftsqlserver,
  //   color: colors.objectType.sqlserver,
  //   component: null
  // },
  {
    id: 'mongodb',
    title: 'MongoDB',
    icon: SiMongodb,
    color: colors.objectType.mongodb,
    component: null
  },
  {
    id: 'redis',
    title: 'Redis',
    icon: SiRedis,
    color: colors.objectType.redis,
    component: null
  },
  {
    id: 'mariadb',
    title: 'MariaDB',
    icon: SiMariadb,
    color: colors.objectType.mariadb,
    component: null
  },
  {
    id: 'dynamodb',
    title: 'DynamoDB',
    icon: SiAmazondynamodb,
    color: colors.objectType.dynamodb,
    component: null
  },
  // {
  //   id: 'cassandra',
  //   title: 'Cassandra',
  //   icon: SiApachecassandra,
  //   color: colors.objectType.cassandra,
  //   component: null
  // },
  // {
  //   id: 'elasticsearch',
  //   title: 'Elasticsearch',
  //   icon: SiElasticsearch,
  //   color: colors.objectType.elasticsearch,
  //   component: null
  // }
];

  // Handle database selection
  const handleDatabaseSelect = (database) => {
    if (database && database.component) {
      setSelectedDatabase(database);
      setError(null);
    } else if (database === null) {
      setSelectedDatabase(null);
    } else {
      setError(`${database.title} schema browser is coming soon!`);
      setTimeout(() => setError(null), 3000);
    }
  };

  // Auto-select Oracle on page load
  useEffect(() => {
    if (!isInitialized && authToken) {
      const oracleDb = databases.find(db => db.id === 'oracle');
      if (oracleDb && oracleDb.component) {
        handleDatabaseSelect(oracleDb);
        setIsInitialized(true);
      }
    }
  }, [authToken, isInitialized, databases]);

  // If a database is selected, render its component with top navigation
  if (selectedDatabase) {
    const SelectedComponent = selectedDatabase.component;
    return (
      <div className="min-h-screen" style={{ backgroundColor: colors.bg }}>
        {/* Top Navigation Bar with Database Icons */}
        <DatabaseTopNav
          databases={databases}
          selectedDatabaseId={selectedDatabase.id}
          onSelectDatabase={handleDatabaseSelect}
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

  // Show loading state while initializing
  return (
    <div 
      className="min-h-screen flex items-center justify-center"
      style={{ backgroundColor: colors.bg }}
    >
      <div className="text-center">
        <div className="animate-spin mb-4">
          <Database size={48} style={{ color: colors.primary }} />
        </div>
        <p className="text-sm" style={{ color: colors.textSecondary }}>
          Loading Oracle Schema Browser...
        </p>
      </div>
    </div>
  );
};

export default SchemaBrowserIndex;