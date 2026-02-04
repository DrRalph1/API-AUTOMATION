import React, { useState, useEffect } from 'react';
import {
  Database, Table, Columns, FileText, Code, Package, Hash, Link, Type,
  Search, Filter, Star, ChevronDown, ChevronRight, ChevronUp, ChevronLeft,
  MoreVertical, Settings, User, Moon, Sun, RefreshCw, Plus, X, Check,
  Eye, EyeOff, Copy, Download, Upload, Share2, Edit2, Trash2, Play,
  Save, Folder, FolderOpen, Server, Activity, BarChart, Terminal,
  Globe, Lock, Key, Shield, Users, Bell, HelpCircle, AlertCircle,
  Clock, Cpu, HardDrive, Network, Wifi, Bluetooth, Smartphone, Monitor,
  Printer, Inbox, Archive, Cloud, Home, Coffee, Grid, List, Maximize2,
  Minimize2, MoreHorizontal, Send, CheckCircle, XCircle, Info, Layers,
  Box, FolderPlus, FilePlus, GitBranch, Bold, Italic, Image, Table as TableIcon,
  ExternalLink, UploadCloud, DownloadCloud, ShieldCheck, LayoutDashboard,
  BookOpen, Zap, History, Terminal as TerminalIcon,
  ChevronsLeft, ChevronsRight, GripVertical, Circle, Dot, Type as TypeIcon,
  FileCode, ChevronsUp, ChevronsDown, AlertTriangle, Menu
} from 'lucide-react';
import ApiGenerationModal from '@/components/modals/ApiGenerationModal.js';

const SchemaBrowser = ({ theme, isDark, customTheme, toggleTheme }) => {

  // Using EXACT Dashboard color system for consistency
  const colors = isDark ? {
    // Using your shade as base
    bg: 'rgb(1 14 35)',
    white: '#FFFFFF',
    sidebar: 'rgb(41 53 72 / 39%)',
    main: 'rgb(1 14 35)',
    header: 'rgb(20 26 38)',
    card: 'rgb(41 53 72 / 39%)',
    
    // Text - coordinating grays (EXACT from Dashboard)
    text: '#F1F5F9',
    textSecondary: 'rgb(148 163 184)',
    textTertiary: 'rgb(100 116 139)',
    
    // Borders - variations of your shade
    border: 'rgb(51 65 85)',
    borderLight: 'rgb(45 55 72)',
    borderDark: 'rgb(71 85 105)',
    
    // Interactive - layered transparency
    hover: 'rgb(45 46 72 / 33%)',
    active: 'rgb(59 74 99)',
    selected: 'rgb(44 82 130)',
    
    // Primary colors (EXACT from Dashboard)
    primary: 'rgb(96 165 250)',
    primaryLight: 'rgb(147 197 253)',
    primaryDark: 'rgb(37 99 235)',
    
    // Status colors (EXACT from Dashboard)
    success: 'rgb(52 211 153)',
    warning: 'rgb(251 191 36)',
    error: 'rgb(248 113 113)',
    info: 'rgb(96 165 250)',
    
    // UI Components (EXACT from Dashboard)
    tabActive: 'rgb(96 165 250)',
    tabInactive: 'rgb(148 163 184)',
    sidebarActive: 'rgb(96 165 250)',
    sidebarHover: 'rgb(45 46 72 / 33%)',
    inputBg: 'rgb(41 53 72 / 39%)',
    inputBorder: 'rgb(51 65 85)',
    tableHeader: 'rgb(41 53 72 / 39%)',
    tableRow: 'rgb(41 53 72 / 39%)',
    tableRowHover: 'rgb(45 46 72 / 33%)',
    dropdownBg: 'rgb(41 53 72 / 39%)',
    dropdownBorder: 'rgb(51 65 85)',
    modalBg: 'rgb(41 53 72 / 39%)',
    modalBorder: 'rgb(51 65 85)',
    codeBg: 'rgb(41 53 72 / 39%)',
    
    // Connection status (EXACT from Dashboard)
    connectionOnline: 'rgb(52 211 153)',
    connectionOffline: 'rgb(248 113 113)',
    connectionIdle: 'rgb(251 191 36)',
    
    // Object type colors - updated to match Dashboard's aesthetic
    objectType: {
      table: 'rgb(96 165 250)',      // Blue - matches primary
      view: 'rgb(52 211 153)',       // Green - matches success
      procedure: 'rgb(167 139 250)', // Purple - using Dashboard's accentPurple
      function: 'rgb(251 191 36)',   // Orange - matches warning
      package: 'rgb(148 163 184)',   // Gray - matches textSecondary
      sequence: 'rgb(100 116 139)',  // Dark blue gray - matches textTertiary
      synonym: 'rgb(34 211 238)',    // Teal - using Dashboard's accentCyan
      type: 'rgb(139 92 246)',       // Indigo purple
      trigger: 'rgb(244 114 182)',   // Pink - using Dashboard's accentPink
      index: 'rgb(16 185 129)',      // Teal green
      constraint: 'rgb(248 113 113)' // Red - matches error
    },
    
    // Grid colors for tables (matching Dashboard's structure)
    gridRowEven: 'rgb(41 53 72 / 39%)',
    gridRowOdd: 'rgb(45 46 72 / 33%)',
    gridHeader: 'rgb(41 53 72 / 39%)',
    gridBorder: 'rgb(51 65 85)'
  } : {
    // KEEP THE ORIGINAL LIGHT MODE (EXACT from Dashboard)
    bg: '#f8fafc',
    white: '#f8fafc',
    sidebar: '#ffffff',
    main: '#f8fafc',
    header: '#ffffff',
    card: '#ffffff',
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
    objectType: {
      table: '#3b82f6',
      view: '#10b981',
      procedure: '#8b5cf6',
      function: '#f59e0b',
      package: '#6b7280',
      sequence: '#64748b',
      synonym: '#06b6d4',
      type: '#6366f1',
      trigger: '#ec4899',
      index: '#0d9488',
      constraint: '#ef4444'
    },
    gridRowEven: '#ffffff',
    gridRowOdd: '#f8fafc',
    gridHeader: '#f1f5f9',
    gridBorder: '#e2e8f0'
  };

  const [showApiModal, setShowApiModal] = useState(false);
  const [selectedForApiGeneration, setSelectedForApiGeneration] = useState(null);

  // Mobile state
  const [isLeftSidebarVisible, setIsLeftSidebarVisible] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  // Professional Data Structures - Oracle Standards
  const [connections, setConnections] = useState([
    {
      id: 'conn-1',
      name: 'CBX_DMX',
      description: 'Production HR Database',
      host: 'db-prod.company.com',
      port: '1521',
      service: 'ORCL',
      username: 'HR',
      status: 'connected',
      color: colors.connectionOnline,
      lastUsed: '2024-01-15T10:30:00Z'
    },
    {
      id: 'conn-2',
      name: 'SCOTT_DEV',
      description: 'Development Database',
      host: 'db-dev.company.com',
      port: '1521',
      service: 'XE',
      username: 'SCOTT',
      status: 'connected',
      color: colors.connectionOnline,
      lastUsed: '2024-01-14T14:20:00Z'
    }
  ]);

  const [activeConnection, setActiveConnection] = useState('conn-1');

  // Complete Schema Objects with Professional Data
  const [schemaObjects, setSchemaObjects] = useState({
    tables: [
      {
        id: 'T_EMP',
        name: 'EMPLOYEES',
        owner: 'HR',
        type: 'TABLE',
        rowCount: 1074,
        size: '12.5 MB',
        tablespace: 'USERS',
        created: '2023-06-15T08:00:00Z',
        lastDDL: '2024-01-10T14:30:00Z',
        status: 'VALID',
        isFavorite: true,
        isExpanded: false,
        comment: 'Employee master table containing all employee records',
        columns: [
          { name: 'EMPLOYEE_ID', type: 'NUMBER(6)', nullable: 'N', key: 'PK', defaultValue: null, position: 1 },
          { name: 'FIRST_NAME', type: 'VARCHAR2(20)', nullable: 'Y', key: null, defaultValue: null, position: 2 },
          { name: 'LAST_NAME', type: 'VARCHAR2(25)', nullable: 'N', key: null, defaultValue: null, position: 3 },
          { name: 'EMAIL', type: 'VARCHAR2(25)', nullable: 'N', key: 'UK', defaultValue: null, position: 4 },
          { name: 'PHONE_NUMBER', type: 'VARCHAR2(20)', nullable: 'Y', key: null, defaultValue: null, position: 5 },
          { name: 'HIRE_DATE', type: 'DATE', nullable: 'N', key: null, defaultValue: 'SYSDATE', position: 6 },
          { name: 'JOB_ID', type: 'VARCHAR2(10)', nullable: 'N', key: 'FK', defaultValue: null, position: 7 },
          { name: 'SALARY', type: 'NUMBER(8,2)', nullable: 'Y', key: null, defaultValue: null, position: 8 },
          { name: 'COMMISSION_PCT', type: 'NUMBER(2,2)', nullable: 'Y', key: null, defaultValue: null, position: 9 },
          { name: 'MANAGER_ID', type: 'NUMBER(6)', nullable: 'Y', key: 'FK', defaultValue: null, position: 10 },
          { name: 'DEPARTMENT_ID', type: 'NUMBER(4)', nullable: 'Y', key: 'FK', defaultValue: null, position: 11 }
        ],
        constraints: [
          { name: 'EMP_EMP_ID_PK', type: 'PRIMARY KEY', columns: 'EMPLOYEE_ID', status: 'ENABLED', validated: 'VALIDATED' },
          { name: 'EMP_EMAIL_UK', type: 'UNIQUE', columns: 'EMAIL', status: 'ENABLED', validated: 'VALIDATED' },
          { name: 'EMP_DEPT_FK', type: 'FOREIGN KEY', columns: 'DEPARTMENT_ID', refTable: 'DEPARTMENTS', refColumns: 'DEPARTMENT_ID', status: 'ENABLED' },
          { name: 'EMP_JOB_FK', type: 'FOREIGN KEY', columns: 'JOB_ID', refTable: 'JOBS', refColumns: 'JOB_ID', status: 'ENABLED' },
          { name: 'EMP_MANAGER_FK', type: 'FOREIGN KEY', columns: 'MANAGER_ID', refTable: 'EMPLOYEES', refColumns: 'EMPLOYEE_ID', status: 'ENABLED' },
          { name: 'EMP_SALARY_MIN', type: 'CHECK', condition: 'SALARY > 0', status: 'ENABLED' }
        ],
        indexes: [
          { name: 'EMP_NAME_IX', type: 'NORMAL', columns: 'LAST_NAME, FIRST_NAME', uniqueness: 'NONUNIQUE', status: 'VALID' },
          { name: 'EMP_JOB_IX', type: 'NORMAL', columns: 'JOB_ID', uniqueness: 'NONUNIQUE', status: 'VALID' },
          { name: 'EMP_DEPARTMENT_IX', type: 'NORMAL', columns: 'DEPARTMENT_ID', uniqueness: 'NONUNIQUE', status: 'VALID' }
        ],
        grants: [
          { grantee: 'SCOTT', privilege: 'SELECT', grantable: 'NO' },
          { grantee: 'SCOTT', privilege: 'INSERT', grantable: 'YES' },
          { grantee: 'PUBLIC', privilege: 'SELECT', grantable: 'NO' }
        ],
        triggers: [
          { name: 'BIU_EMPLOYEES', type: 'BEFORE INSERT OR UPDATE', event: 'INSERT OR UPDATE', status: 'ENABLED' },
          { name: 'AUDIT_EMPLOYEES', type: 'AFTER UPDATE OR DELETE', event: 'UPDATE OR DELETE', status: 'ENABLED' }
        ]
      },
      {
        id: 'T_DEPT',
        name: 'DEPARTMENTS',
        owner: 'HR',
        type: 'TABLE',
        rowCount: 27,
        size: '256 KB',
        tablespace: 'USERS',
        created: '2023-06-15T08:00:00Z',
        status: 'VALID',
        isFavorite: false,
        isExpanded: false
      }
    ],
    views: [
      {
        id: 'V_EMP_DETAILS',
        name: 'EMP_DETAILS_VIEW',
        owner: 'HR',
        type: 'VIEW',
        status: 'VALID',
        isFavorite: true,
        isExpanded: false,
        text: `CREATE OR REPLACE FORCE VIEW HR.EMP_DETAILS_VIEW AS
SELECT 
    e.employee_id,
    e.first_name || ' ' || e.last_name AS full_name,
    e.email,
    e.hire_date,
    e.salary,
    d.department_name,
    j.job_title,
    m.first_name || ' ' || m.last_name AS manager_name,
    l.city,
    c.country_name
FROM 
    employees e
    JOIN departments d ON e.department_id = d.department_id
    JOIN jobs j ON e.job_id = j.job_id
    LEFT JOIN employees m ON e.manager_id = m.employee_id
    JOIN locations l ON d.location_id = l.location_id
    JOIN countries c ON l.country_id = c.country_id
WHERE 
    e.status = 'ACTIVE'
WITH READ ONLY;`
      }
    ],
    procedures: [
      {
        id: 'P_ADD_EMP',
        name: 'ADD_EMPLOYEE',
        owner: 'HR',
        type: 'PROCEDURE',
        status: 'VALID',
        isFavorite: false,
        isExpanded: false,
        parameters: [
          { name: 'p_employee_id', type: 'IN', datatype: 'NUMBER', defaultValue: null, position: 1 },
          { name: 'p_first_name', type: 'IN', datatype: 'VARCHAR2', defaultValue: null, position: 2 },
          { name: 'p_last_name', type: 'IN', datatype: 'VARCHAR2', defaultValue: null, position: 3 },
          { name: 'p_email', type: 'IN', datatype: 'VARCHAR2', defaultValue: null, position: 4 },
          { name: 'p_phone_number', type: 'IN', datatype: 'VARCHAR2', defaultValue: null, position: 5 },
          { name: 'p_hire_date', type: 'IN', datatype: 'DATE', defaultValue: 'SYSDATE', position: 6 },
          { name: 'p_job_id', type: 'IN', datatype: 'VARCHAR2', defaultValue: null, position: 7 },
          { name: 'p_salary', type: 'IN', datatype: 'NUMBER', defaultValue: null, position: 8 },
          { name: 'p_commission_pct', type: 'IN', datatype: 'NUMBER', defaultValue: null, position: 9 },
          { name: 'p_manager_id', type: 'IN', datatype: 'NUMBER', defaultValue: null, position: 10 },
          { name: 'p_department_id', type: 'IN', datatype: 'NUMBER', defaultValue: null, position: 11 }
        ],
        text: `CREATE OR REPLACE PROCEDURE HR.ADD_EMPLOYEE (
    p_employee_id    IN NUMBER,
    p_first_name     IN VARCHAR2,
    p_last_name      IN VARCHAR2,
    p_email          IN VARCHAR2,
    p_phone_number   IN VARCHAR2 DEFAULT NULL,
    p_hire_date      IN DATE DEFAULT SYSDATE,
    p_job_id         IN VARCHAR2,
    p_salary         IN NUMBER DEFAULT NULL,
    p_commission_pct IN NUMBER DEFAULT NULL,
    p_manager_id     IN NUMBER DEFAULT NULL,
    p_department_id  IN NUMBER DEFAULT NULL
) AS
    v_email_count NUMBER;
BEGIN
    -- Check if email already exists
    SELECT COUNT(*) INTO v_email_count
    FROM employees
    WHERE email = p_email;
    
    IF v_email_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Email already exists');
    END IF;
    
    -- Insert new employee
    INSERT INTO employees (
        employee_id, first_name, last_name, email,
        phone_number, hire_date, job_id, salary,
        commission_pct, manager_id, department_id,
        created_date, created_by
    ) VALUES (
        p_employee_id, p_first_name, p_last_name, p_email,
        p_phone_number, p_hire_date, p_job_id, p_salary,
        p_commission_pct, p_manager_id, p_department_id,
        SYSDATE, USER
    );
    
    COMMIT;
    
    DBMS_OUTPUT.PUT_LINE('Employee added successfully: ' || p_employee_id);
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END ADD_EMPLOYEE;`
      }
    ],
    functions: [
      {
        id: 'F_GET_EMP_SAL',
        name: 'GET_EMPLOYEE_SALARY',
        owner: 'HR',
        type: 'FUNCTION',
        returnType: 'NUMBER',
        deterministic: false,
        pipelined: false,
        status: 'VALID',
        isFavorite: false,
        isExpanded: false,
        parameters: [
          { name: 'p_employee_id', type: 'IN', datatype: 'NUMBER', defaultValue: null }
        ],
        text: `CREATE OR REPLACE FUNCTION HR.GET_EMPLOYEE_SALARY (
    p_employee_id IN NUMBER
) RETURN NUMBER
AS
    v_salary NUMBER;
BEGIN
    SELECT salary INTO v_salary
    FROM employees
    WHERE employee_id = p_employee_id;
    
    RETURN v_salary;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN NULL;
    WHEN TOO_MANY_ROWS THEN
        RAISE_APPLICATION_ERROR(-20002, 'Multiple employees found');
    WHEN OTHERS THEN
        RAISE;
END GET_EMPLOYEE_SALARY;`
      }
    ],
    packages: [
      {
        id: 'PKG_EMP',
        name: 'EMP_PKG',
        owner: 'HR',
        type: 'PACKAGE',
        status: 'VALID',
        isFavorite: true,
        isExpanded: true,
        spec: `CREATE OR REPLACE PACKAGE HR.EMP_PKG
AUTHID CURRENT_USER
AS
    -- Public type declarations
    TYPE emp_rec IS RECORD (
        employee_id   employees.employee_id%TYPE,
        full_name     VARCHAR2(50),
        email         employees.email%TYPE,
        salary        employees.salary%TYPE,
        department    departments.department_name%TYPE
    );
    
    TYPE emp_tab IS TABLE OF emp_rec;
    
    -- Public constant declarations
    C_MAX_SALARY CONSTANT NUMBER := 100000;
    C_MIN_SALARY CONSTANT NUMBER := 1000;
    
    -- Public exception declarations
    e_invalid_employee EXCEPTION;
    PRAGMA EXCEPTION_INIT(e_invalid_employee, -20001);
    
    -- Public procedure declarations
    PROCEDURE add_employee (
        p_employee_id    IN NUMBER,
        p_first_name     IN VARCHAR2,
        p_last_name      IN VARCHAR2,
        p_email          IN VARCHAR2,
        p_phone_number   IN VARCHAR2 DEFAULT NULL,
        p_hire_date      IN DATE DEFAULT SYSDATE,
        p_job_id         IN VARCHAR2,
        p_salary         IN NUMBER DEFAULT NULL,
        p_commission_pct IN NUMBER DEFAULT NULL,
        p_manager_id     IN NUMBER DEFAULT NULL,
        p_department_id  IN NUMBER DEFAULT NULL
    );
    
    PROCEDURE update_salary (
        p_employee_id IN NUMBER,
        p_new_salary  IN NUMBER
    );
    
    PROCEDURE delete_employee (
        p_employee_id IN NUMBER
    );
    
    -- Public function declarations
    FUNCTION get_employee_details (
        p_employee_id IN NUMBER
    ) RETURN emp_rec;
    
    FUNCTION get_department_employees (
        p_department_id IN NUMBER
    ) RETURN emp_tab PIPELINED;
    
    FUNCTION calculate_bonus (
        p_employee_id IN NUMBER,
        p_percentage  IN NUMBER DEFAULT 10
    ) RETURN NUMBER;
    
    -- Public variable declarations
    g_debug_mode BOOLEAN := FALSE;
    
END EMP_PKG;`,
        body: `CREATE OR REPLACE PACKAGE BODY HR.EMP_PKG AS
    
    -- Private variables
    v_audit_enabled BOOLEAN := TRUE;
    
    -- Private procedures
    PROCEDURE audit_action (
        p_action      IN VARCHAR2,
        p_employee_id IN NUMBER,
        p_user        IN VARCHAR2 DEFAULT USER
    ) IS
    BEGIN
        IF v_audit_enabled THEN
            INSERT INTO emp_audit_trail (
                audit_id, action_date, action_type,
                employee_id, performed_by
            ) VALUES (
                emp_audit_seq.NEXTVAL, SYSDATE, p_action,
                p_employee_id, p_user
            );
        END IF;
    END audit_action;
    
    -- Implementation of public procedures
    PROCEDURE add_employee (
        p_employee_id    IN NUMBER,
        p_first_name     IN VARCHAR2,
        p_last_name      IN VARCHAR2,
        p_email          IN VARCHAR2,
        p_phone_number   IN VARCHAR2 DEFAULT NULL,
        p_hire_date      IN DATE DEFAULT SYSDATE,
        p_job_id         IN VARCHAR2,
        p_salary         IN NUMBER DEFAULT NULL,
        p_commission_pct IN NUMBER DEFAULT NULL,
        p_manager_id     IN NUMBER DEFAULT NULL,
        p_department_id  IN NUMBER DEFAULT NULL
    ) AS
        v_email_count NUMBER;
    BEGIN
        -- Validation logic
        IF p_salary IS NOT NULL AND p_salary < C_MIN_SALARY THEN
            RAISE_APPLICATION_ERROR(-20003, 'Salary below minimum');
        END IF;
        
        -- Check email uniqueness
        SELECT COUNT(*) INTO v_email_count
        FROM employees
        WHERE email = p_email;
        
        IF v_email_count > 0 THEN
            RAISE e_invalid_employee;
        END IF;
        
        -- Insert record
        INSERT INTO employees (
            employee_id, first_name, last_name, email,
            phone_number, hire_date, job_id, salary,
            commission_pct, manager_id, department_id,
            created_date, created_by
        ) VALUES (
            p_employee_id, p_first_name, p_last_name, p_email,
            p_phone_number, p_hire_date, p_job_id, p_salary,
            p_commission_pct, p_manager_id, p_department_id,
            SYSDATE, USER
        );
        
        -- Audit the action
        audit_action('ADD', p_employee_id);
        
        COMMIT;
        
        IF g_debug_mode THEN
            DBMS_OUTPUT.PUT_LINE('Employee added: ' || p_employee_id);
        END IF;
        
    EXCEPTION
        WHEN e_invalid_employee THEN
            RAISE_APPLICATION_ERROR(-20001, 'Employee email already exists');
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END add_employee;
    
    -- Other procedure implementations...
    
    FUNCTION get_employee_details (
        p_employee_id IN NUMBER
    ) RETURN emp_rec
    AS
        v_result emp_rec;
    BEGIN
        SELECT 
            e.employee_id,
            e.first_name || ' ' || e.last_name,
            e.email,
            e.salary,
            d.department_name
        INTO v_result
        FROM employees e
        JOIN departments d ON e.department_id = d.department_id
        WHERE e.employee_id = p_employee_id;
        
        RETURN v_result;
        
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN NULL;
    END get_employee_details;
    
    -- Other function implementations...
    
END EMP_PKG;`
      }
    ],
    sequences: [
      {
        id: 'SEQ_EMP',
        name: 'EMPLOYEES_SEQ',
        owner: 'HR',
        type: 'SEQUENCE',
        lastNumber: 1100,
        minValue: 1,
        maxValue: 999999999999999999999999999,
        incrementBy: 1,
        cycleFlag: 'N',
        orderFlag: 'N',
        cacheSize: 20,
        status: 'VALID',
        isFavorite: false,
        isExpanded: false
      }
    ],
    synonyms: [
      {
        id: 'SYN_EMP',
        name: 'EMP',
        owner: 'HR',
        type: 'SYNONYM',
        tableOwner: 'HR',
        tableName: 'EMPLOYEES',
        dbLink: null,
        status: 'VALID',
        isFavorite: false,
        isExpanded: false
      }
    ],
    types: [
      {
        id: 'TYP_EMP_REC',
        name: 'EMPLOYEE_REC',
        owner: 'HR',
        type: 'TYPE',
        attributes: [
          { name: 'EMPLOYEE_ID', type: 'NUMBER', length: null, precision: 6, scale: 0 },
          { name: 'FULL_NAME', type: 'VARCHAR2', length: 50, precision: null, scale: null },
          { name: 'EMAIL', type: 'VARCHAR2', length: 25, precision: null, scale: null },
          { name: 'SALARY', type: 'NUMBER', length: null, precision: 8, scale: 2 }
        ],
        status: 'VALID',
        isFavorite: false,
        isExpanded: false
      }
    ],
    triggers: [
      {
        id: 'TRG_EMP_AUD',
        name: 'BIU_EMPLOYEES',
        owner: 'HR',
        type: 'TRIGGER',
        triggerType: 'BEFORE EACH ROW',
        triggeringEvent: 'INSERT OR UPDATE',
        tableOwner: 'HR',
        tableName: 'EMPLOYEES',
        status: 'ENABLED',
        isFavorite: false,
        isExpanded: false,
        text: `CREATE OR REPLACE TRIGGER HR.BIU_EMPLOYEES
BEFORE INSERT OR UPDATE ON HR.EMPLOYEES
FOR EACH ROW
DECLARE
    v_current_date DATE := SYSDATE;
BEGIN
    IF INSERTING THEN
        :NEW.created_date := v_current_date;
        :NEW.created_by := USER;
        :NEW.last_updated_date := v_current_date;
        :NEW.last_updated_by := USER;
    ELSIF UPDATING THEN
        :NEW.last_updated_date := v_current_date;
        :NEW.last_updated_by := USER;
    END IF;
    
    -- Validate email format
    IF NOT REGEXP_LIKE(:NEW.email, '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$') THEN
        RAISE_APPLICATION_ERROR(-20010, 'Invalid email format');
    END IF;
    
    -- Validate salary
    IF :NEW.salary IS NOT NULL AND :NEW.salary < 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 'Salary cannot be negative');
    END IF;
END BIU_EMPLOYEES;`
      }
    ]
  });

  // Professional State Management
  const [activeObject, setActiveObject] = useState(schemaObjects.tables[0]);
  const [activeTab, setActiveTab] = useState('columns');
  const [objectTree, setObjectTree] = useState({
    tables: true,
    views: false,
    procedures: false,
    functions: false,
    packages: true,
    sequences: false,
    synonyms: false,
    types: false,
    triggers: false
  });
  const [tabs, setTabs] = useState([
    { id: 'tab-1', name: 'EMPLOYEES', type: 'TABLE', objectId: 'T_EMP', isActive: true, isDirty: false }
  ]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedOwner, setSelectedOwner] = useState('HR');
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuPosition, setContextMenuPosition] = useState({ x: 0, y: 0 });
  const [contextObject, setContextObject] = useState(null);
  const [showConnectionManager, setShowConnectionManager] = useState(false);
  const [dataView, setDataView] = useState({
    page: 1,
    pageSize: 50,
    sortColumn: 'EMPLOYEE_ID',
    sortDirection: 'ASC',
    filters: []
  });

  // Sample Data for Tables
  const sampleData = {
    EMPLOYEES: [
      { EMPLOYEE_ID: 100, FIRST_NAME: 'Steven', LAST_NAME: 'King', EMAIL: 'SKING', HIRE_DATE: '2003-06-17', SALARY: 24000, JOB_ID: 'AD_PRES', DEPARTMENT_ID: 90 },
      { EMPLOYEE_ID: 101, FIRST_NAME: 'Neena', LAST_NAME: 'Kochhar', EMAIL: 'NKOCHHAR', HIRE_DATE: '2005-09-21', SALARY: 17000, JOB_ID: 'AD_VP', DEPARTMENT_ID: 90 },
      { EMPLOYEE_ID: 102, FIRST_NAME: 'Lex', LAST_NAME: 'De Haan', EMAIL: 'LDEHAAN', HIRE_DATE: '2001-01-13', SALARY: 17000, JOB_ID: 'AD_VP', DEPARTMENT_ID: 90 },
      { EMPLOYEE_ID: 103, FIRST_NAME: 'Alexander', LAST_NAME: 'Hunold', EMAIL: 'AHUNOLD', HIRE_DATE: '2006-01-03', SALARY: 9000, JOB_ID: 'IT_PROG', DEPARTMENT_ID: 60 },
      { EMPLOYEE_ID: 104, FIRST_NAME: 'Bruce', LAST_NAME: 'Ernst', EMAIL: 'BERNST', HIRE_DATE: '2007-05-21', SALARY: 6000, JOB_ID: 'IT_PROG', DEPARTMENT_ID: 60 }
    ]
  };

  // Get Object Icon with color scheme from Dashboard
  const getObjectIcon = (type) => {
    const objectType = type.toLowerCase();
    const iconColor = colors.objectType[objectType] || colors.textSecondary;
    const iconProps = { size: 14, style: { color: iconColor } };
    
    switch(objectType) {
      case 'table': return <Table {...iconProps} />;
      case 'view': return <FileText {...iconProps} />;
      case 'procedure': return <Terminal {...iconProps} />;
      case 'function': return <Code {...iconProps} />;
      case 'package': return <Package {...iconProps} />;
      case 'sequence': return <Hash {...iconProps} />;
      case 'synonym': return <Link {...iconProps} />;
      case 'type': return <Type {...iconProps} />;
      case 'trigger': return <Zap {...iconProps} />;
      case 'index': return <BarChart {...iconProps} />;
      default: return <Database {...iconProps} />;
    }
  };

  // Handle Object Selection
  const handleObjectSelect = (object, type) => {
    setActiveObject(object);
    setSelectedForApiGeneration(object);
    
    // Check if tab already exists
    const tabId = `${type}_${object.id}`;
    const existingTab = tabs.find(tab => tab.id === tabId);
    
    if (existingTab) {
      setTabs(tabs.map(tab => ({ ...tab, isActive: tab.id === tabId })));
    } else {
      setTabs(tabs.map(tab => ({ ...tab, isActive: false })).concat({
        id: tabId,
        name: object.name,
        type: type,
        objectId: object.id,
        isActive: true,
        isDirty: false
      }));
    }
    
    // Set default tab based on object type
    switch(type.toLowerCase()) {
      case 'table':
        setActiveTab('columns');
        break;
      case 'view':
        setActiveTab('definition');
        break;
      case 'procedure':
      case 'function':
        setActiveTab('parameters');
        break;
      case 'package':
        setActiveTab('specification');
        break;
      case 'trigger':
        setActiveTab('definition');
        break;
      default:
        setActiveTab('properties');
    }

    // Close sidebar on mobile
    if (window.innerWidth < 768) {
      setIsLeftSidebarVisible(false);
    }
  };

  // Handle Context Menu
  const handleContextMenu = (e, object, type) => {
    e.preventDefault();
    e.stopPropagation();
    
    setContextObject({ ...object, type });
    setContextMenuPosition({ x: e.clientX, y: e.clientY });
    setShowContextMenu(true);
  };

  // Render Object Tree Section
  const renderObjectTreeSection = (title, type, objects) => {
    const isExpanded = objectTree[type];
    
    return (
      <div className="mb-1">
        <button
          onClick={() => setObjectTree({ ...objectTree, [type]: !isExpanded })}
          className="flex items-center justify-between w-full px-2 py-1.5 hover:bg-opacity-50 transition-colors rounded-sm text-sm font-medium hover-lift"
          style={{ 
            backgroundColor: isExpanded ? colors.active : 'transparent',
            color: colors.text
          }}
        >
          <div className="flex items-center gap-2">
            {isExpanded ? 
              <ChevronDown size={12} style={{ color: colors.textSecondary }} /> :
              <ChevronRight size={12} style={{ color: colors.textSecondary }} />
            }
            {getObjectIcon(type.slice(0, -1))}
            <span className="truncate">{title}</span>
          </div>
          <span className="text-xs px-1.5 py-0.5 rounded shrink-0" style={{ 
            backgroundColor: colors.border,
            color: colors.textSecondary
          }}>
            {objects.length}
          </span>
        </button>
        
        {isExpanded && (
          <div className="ml-6 mt-0.5 space-y-0.5">
            {objects.map(obj => (
              <div
                key={obj.id}
                onDoubleClick={() => handleObjectSelect(obj, type.slice(0, -1).toUpperCase())}
                onContextMenu={(e) => handleContextMenu(e, obj, type.slice(0, -1).toUpperCase())}
                onClick={() => handleObjectSelect(obj, type.slice(0, -1).toUpperCase())}
                className={`flex items-center justify-between px-2 py-1.5 rounded-sm cursor-pointer group hover-lift ${
                  activeObject?.id === obj.id ? 'font-medium' : ''
                }`}
                style={{
                  backgroundColor: activeObject?.id === obj.id ? colors.selected : 'transparent',
                  color: activeObject?.id === obj.id ? colors.primary : colors.text
                }}
              >
                <div className="flex items-center gap-2 min-w-0">
                  {getObjectIcon(type.slice(0, -1))}
                  <span className="text-sm truncate">{obj.name}</span>
                  {obj.status !== 'VALID' && (
                    <AlertCircle size={10} style={{ color: colors.error }} className="shrink-0" />
                  )}
                </div>
                <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity shrink-0">
                  {obj.isFavorite && (
                    <Star size={10} fill={colors.warning} style={{ color: colors.warning }} />
                  )}
                  {obj.rowCount && (
                    <span className="text-xs" style={{ color: activeObject?.id === obj.id ? colors.primary : colors.textSecondary }}>
                      ({obj.rowCount})
                    </span>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    );
  };

  // Render Columns Tab (Professional Style)
  const renderColumnsTab = () => (
    <div className="flex-1 overflow-auto">
      <div className="border rounded" style={{ 
        borderColor: colors.gridBorder,
        backgroundColor: colors.card
      }}>
        {/* Toolbar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 p-2 border-b" style={{ 
          borderColor: colors.gridBorder,
          backgroundColor: colors.card
        }}>
          <div className="text-sm font-medium" style={{ color: colors.text }}>
            Columns ({activeObject.columns?.length || 0})
          </div>
          <div className="flex items-center gap-2">
            <button className="px-2 py-1 text-xs rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}>
              <Copy size={10} className="inline mr-1" />
              <span className="hidden sm:inline">Copy</span>
            </button>
            <button className="px-2 py-1 text-xs rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}>
              <Download size={10} className="inline mr-1" />
              <span className="hidden sm:inline">Export</span>
            </button>
          </div>
        </div>

        {/* Columns Grid */}
        <div className="overflow-auto">
          <table className="w-full min-w-[600px]" style={{ borderCollapse: 'collapse' }}>
            <thead style={{ 
              backgroundColor: colors.tableHeader,
              position: 'sticky',
              top: 0,
              zIndex: 10
            }}>
              <tr>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary,
                  width: '40px'
                }}>#</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary,
                  minWidth: '120px'
                }}>Column Name</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary,
                  minWidth: '100px'
                }}>Data Type</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary,
                  width: '50px'
                }}>Null</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary,
                  width: '70px'
                }}>Key</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary
                }}>Default</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary
                }}>Comment</th>
              </tr>
            </thead>
            <tbody>
              {activeObject.columns?.map((col, index) => (
                <tr 
                  key={col.name}
                  className="hover:bg-opacity-50 transition-colors"
                  style={{ 
                    backgroundColor: index % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                    borderBottom: `1px solid ${colors.gridBorder}`
                  }}
                >
                  <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>{col.position}</td>
                  <td className="p-2 text-xs font-medium truncate" style={{ color: colors.text }}>{col.name}</td>
                  <td className="p-2 text-xs font-mono truncate" style={{ color: colors.text }}>{col.type}</td>
                  <td className="p-2 text-xs text-center">
                    <div className={`inline-flex items-center justify-center w-5 h-5 rounded ${
                      col.nullable === 'Y' ? 'bg-green-500/10 text-green-600' : 'bg-red-500/10 text-red-600'
                    }`}>
                      {col.nullable === 'Y' ? 'Y' : 'N'}
                    </div>
                  </td>
                  <td className="p-2">
                    {col.key && (
                      <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                        col.key === 'PK' ? 'bg-blue-500/10' :
                        col.key === 'FK' ? 'bg-purple-500/10 text-purple-400' :
                        'bg-green-500/10 text-green-400'
                      }`}>
                        {col.key}
                      </span>
                    )}
                  </td>
                  <td className="p-2 text-xs font-mono truncate" style={{ color: colors.textSecondary }}>
                    {col.defaultValue || <span className="italic">NULL</span>}
                  </td>
                  <td className="p-2 text-xs truncate" style={{ color: colors.textSecondary }}>
                    {col.comment || '-'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );

  // Render Data Tab (Professional Data Grid)
  const renderDataTab = () => {
    const data = sampleData[activeObject.name] || [];
    
    return (
      <div className="flex-1 flex flex-col">
        {/* Data Grid Toolbar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 p-2 border-b" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="flex flex-wrap items-center gap-2">
            <button className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 hover-lift"
              style={{ backgroundColor: colors.primaryDark, color: colors.white }}>
              <Play size={12} />
              <span className="hidden sm:inline">Execute</span>
            </button>
            <button className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}>
              <span className="hidden sm:inline">Commit</span>
            </button>
            <div className="ml-0 sm:ml-4 flex items-center gap-2">
              <span className="text-sm hidden sm:inline" style={{ color: colors.textSecondary }}>Auto-refresh:</span>
              <select className="px-2 py-1 border rounded text-sm focus:outline-none hover-lift"
                style={{ 
                  backgroundColor: colors.card,
                  borderColor: colors.border,
                  color: colors.text
                }}>
                <option>Off</option>
                <option>5s</option>
                <option>10s</option>
                <option>30s</option>
              </select>
            </div>
          </div>
          <div className="flex items-center justify-between gap-2">
            <span className="text-sm hidden sm:inline" style={{ color: colors.textSecondary }}>
              Page: 1 of 1 | Rows: 1-{data.length} of {activeObject.rowCount?.toLocaleString()}
            </span>
            <div className="flex items-center gap-2">
              <button className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}>
                <ChevronLeft size={14} style={{ color: colors.textSecondary }} />
              </button>
              <button className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}>
                <ChevronRight size={14} style={{ color: colors.textSecondary }} />
              </button>
            </div>
          </div>
        </div>

        {/* Data Grid */}
        <div className="flex-1 overflow-auto">
          <div className="border rounded" style={{ 
            borderColor: colors.gridBorder,
            backgroundColor: colors.card
          }}>
            <table className="w-full min-w-[800px]" style={{ borderCollapse: 'collapse' }}>
              <thead style={{ 
                backgroundColor: colors.tableHeader,
                position: 'sticky',
                top: 0,
                zIndex: 10
              }}>
                <tr>
                  {activeObject.columns?.slice(0, 6).map(col => (
                    <th key={col.name} className="text-left p-2 text-xs font-medium border-b" style={{ 
                      borderColor: colors.gridBorder,
                      color: colors.textSecondary,
                      whiteSpace: 'nowrap'
                    }}>
                      <div className="flex items-center gap-1">
                        <span className="truncate">{col.name}</span>
                        {col.key && (
                          <span className={`px-1 py-0.5 rounded text-xs hidden sm:inline ${
                            col.key === 'PK' ? 'bg-blue-500/10' :
                            col.key === 'FK' ? 'bg-purple-500/10 text-purple-400' :
                            'bg-green-500/10 text-green-400'
                          }`}>
                            {col.key}
                          </span>
                        )}
                      </div>
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {data.map((row, rowIndex) => (
                  <tr 
                    key={rowIndex}
                    className="hover:bg-opacity-50 transition-colors"
                    style={{ 
                      backgroundColor: rowIndex % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                      borderBottom: `1px solid ${colors.gridBorder}`
                    }}
                  >
                    {activeObject.columns?.slice(0, 6).map(col => (
                      <td key={col.name} className="p-2 text-xs border-r" style={{ 
                        borderColor: colors.gridBorder,
                        color: colors.text,
                        whiteSpace: 'nowrap'
                      }}>
                        <div className="truncate max-w-[150px] sm:max-w-none">
                          {row[col.name] !== null && row[col.name] !== undefined ? row[col.name] : (
                            <span className="italic" style={{ color: colors.textTertiary }}>NULL</span>
                          )}
                        </div>
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Status Bar */}
        <div className="p-2 border-t" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="text-xs truncate" style={{ color: colors.textSecondary }}>
            {data.length} rows fetched in 0.023 seconds | 
            Table: {activeObject.name} | 
            Total Rows: {activeObject.rowCount?.toLocaleString()}
          </div>
        </div>
      </div>
    );
  };

  // Render Parameters Tab for Procedures/Functions
  const renderParametersTab = () => (
    <div className="flex-1 overflow-auto">
      <div className="border rounded" style={{ 
        borderColor: colors.gridBorder,
        backgroundColor: colors.card
      }}>
        <div className="p-2 border-b" style={{ 
          borderColor: colors.gridBorder,
          backgroundColor: colors.card
        }}>
          <div className="text-sm font-medium" style={{ color: colors.text }}>
            Parameters ({activeObject.parameters?.length || 0})
          </div>
        </div>
        <div className="overflow-auto">
          <table className="w-full min-w-[600px]" style={{ borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ backgroundColor: colors.tableHeader }}>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary,
                  width: '40px'
                }}>#</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary
                }}>Parameter Name</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary,
                  width: '70px'
                }}>Mode</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary
                }}>Data Type</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary
                }}>Default Value</th>
              </tr>
            </thead>
            <tbody>
              {activeObject.parameters?.map((param, index) => (
                <tr 
                  key={param.name}
                  className="hover:bg-opacity-50 transition-colors"
                  style={{ 
                    backgroundColor: index % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                    borderBottom: `1px solid ${colors.gridBorder}`
                  }}
                >
                  <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>{param.position}</td>
                  <td className="p-2 text-xs font-medium truncate" style={{ color: colors.text }}>{param.name}</td>
                  <td className="p-2">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                      param.type === 'IN' ? 'bg-blue-500/10' :
                      param.type === 'OUT' ? 'bg-purple-500/10 text-purple-400' :
                      'bg-green-500/10 text-green-400'
                    }`}>
                      {param.type}
                    </span>
                  </td>
                  <td className="p-2 text-xs font-mono truncate" style={{ color: colors.text }}>{param.datatype}</td>
                  <td className="p-2 text-xs font-mono truncate" style={{ color: colors.textSecondary }}>
                    {param.defaultValue || <span className="italic">NULL</span>}
                  </td>
                </tr>
              ))}
              {activeObject.type === 'FUNCTION' && activeObject.returnType && (
                <tr className="border-t" style={{ borderColor: colors.gridBorder }}>
                  <td className="p-2 text-xs font-medium" style={{ color: colors.textSecondary }}>-</td>
                  <td className="p-2 text-xs font-medium" style={{ color: colors.text }}>RETURN</td>
                  <td className="p-2">
                    <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-500/10 text-green-400">
                      OUT
                    </span>
                  </td>
                  <td className="p-2 text-xs font-mono font-medium truncate" style={{ color: colors.text }}>
                    {activeObject.returnType}
                  </td>
                  <td className="p-2 text-xs truncate" style={{ color: colors.textSecondary }}>
                    {activeObject.deterministic ? 'DETERMINISTIC' : ''}
                    {activeObject.pipelined ? ' | PIPELINED' : ''}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );

  // Render DDL Tab
  const renderDDLTab = () => (
    <div className="flex-1 overflow-auto">
      <div className="border rounded p-4" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.codeBg
      }}>
        <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed overflow-x-auto" style={{ 
          color: colors.text,
          fontFamily: 'Consolas, "Courier New", monospace'
        }}>
          {activeObject.text || activeObject.spec || activeObject.body || 'No DDL available'}
        </pre>
      </div>
    </div>
  );

  // Render Constraints Tab
  const renderConstraintsTab = () => (
    <div className="flex-1 overflow-auto">
      <div className="border rounded" style={{ 
        borderColor: colors.gridBorder,
        backgroundColor: colors.card
      }}>
        <div className="p-2 border-b" style={{ 
          borderColor: colors.gridBorder,
          backgroundColor: colors.card
        }}>
          <div className="text-sm font-medium" style={{ color: colors.text }}>
            Constraints ({activeObject.constraints?.length || 0})
          </div>
        </div>
        <div className="overflow-auto">
          <table className="w-full min-w-[600px]" style={{ borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ backgroundColor: colors.tableHeader }}>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>Constraint Name</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>Type</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>Columns</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>Referenced Table</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>Status</th>
              </tr>
            </thead>
            <tbody>
              {activeObject.constraints?.map((con, index) => (
                <tr 
                  key={con.name}
                  className="hover:bg-opacity-50 transition-colors"
                  style={{ 
                    backgroundColor: index % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                    borderBottom: `1px solid ${colors.gridBorder}`
                  }}
                >
                  <td className="p-2 text-xs font-medium truncate" style={{ color: colors.text }}>{con.name}</td>
                  <td className="p-2">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                      con.type === 'PRIMARY KEY' ? 'bg-blue-500/10' :
                      con.type === 'FOREIGN KEY' ? 'bg-purple-500/10 text-purple-400' :
                      con.type === 'UNIQUE' ? 'bg-green-500/10 text-green-400' :
                      'bg-yellow-500/10 text-yellow-400'
                    }`}>
                      {con.type}
                    </span>
                  </td>
                  <td className="p-2 text-xs truncate" style={{ color: colors.text }}>{con.columns}</td>
                  <td className="p-2 text-xs truncate" style={{ color: colors.textSecondary }}>
                    {con.refTable || '-'}
                  </td>
                  <td className="p-2">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                      con.status === 'ENABLED' ? 'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'
                    }`}>
                      {con.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );

  // Render Properties Tab
  const renderPropertiesTab = () => {
    const properties = [
      { label: 'Object Name', value: activeObject.name },
      { label: 'Owner', value: activeObject.owner },
      { label: 'Object Type', value: activeObject.type },
      { label: 'Status', value: activeObject.status },
      { label: 'Created', value: activeObject.created ? new Date(activeObject.created).toLocaleString() : '-' },
      { label: 'Last DDL Time', value: activeObject.lastDDL ? new Date(activeObject.lastDDL).toLocaleString() : '-' },
      { label: 'Tablespace', value: activeObject.tablespace || '-' },
      ...(activeObject.rowCount ? [{ label: 'Row Count', value: activeObject.rowCount.toLocaleString() }] : []),
      ...(activeObject.size ? [{ label: 'Size', value: activeObject.size }] : []),
      ...(activeObject.comment ? [{ label: 'Comment', value: activeObject.comment }] : []),
    ];

    return (
      <div className="flex-1 overflow-auto">
        <div className="border rounded" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-medium mb-4" style={{ color: colors.text }}>
              Object Properties
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {properties.map((prop, index) => (
                <div key={index} className="space-y-1">
                  <div className="text-xs font-medium" style={{ color: colors.textSecondary }}>
                    {prop.label}
                  </div>
                  <div className="text-sm truncate" style={{ color: colors.text }}>
                    {prop.value}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  };

  // Get Tabs for Current Object Type
  const getTabsForObject = () => {
    switch(activeObject?.type) {
      case 'TABLE':
        return ['Columns', 'Data', 'Constraints', 'Indexes', 'DDL', 'Properties'];
      case 'VIEW':
        return ['Definition', 'Columns', 'DDL', 'Properties'];
      case 'PROCEDURE':
        return ['Parameters', 'DDL', 'Properties'];
      case 'FUNCTION':
        return ['Parameters', 'DDL', 'Properties'];
      case 'PACKAGE':
        return ['Specification', 'Body', 'DDL', 'Properties'];
      case 'SEQUENCE':
        return ['DDL', 'Properties'];
      case 'SYNONYM':
        return ['DDL', 'Properties'];
      case 'TYPE':
        return ['Attributes', 'DDL', 'Properties'];
      case 'TRIGGER':
        return ['Definition', 'DDL', 'Properties'];
      default:
        return ['Properties'];
    }
  };

  // Render Current Tab Content
  const renderTabContent = () => {
    switch(activeTab.toLowerCase()) {
      case 'columns':
        return renderColumnsTab();
      case 'data':
        return renderDataTab();
      case 'parameters':
        return renderParametersTab();
      case 'constraints':
        return renderConstraintsTab();
      case 'ddl':
        return renderDDLTab();
      case 'properties':
        return renderPropertiesTab();
      case 'definition':
        return renderDDLTab();
      case 'specification':
        return renderDDLTab();
      case 'body':
        return renderDDLTab();
      default:
        return (
          <div className="flex-1 flex items-center justify-center" style={{ color: colors.textSecondary }}>
            Select a tab to view details
          </div>
        );
    }
  };

  // Render Context Menu
  const renderContextMenu = () => {
    if (!showContextMenu || !contextObject) return null;

    const menuItems = [
      { label: 'Open', icon: <ExternalLink size={12} />, action: () => handleObjectSelect(contextObject, contextObject.type) },
      { separator: true },
      { label: 'Generate API', icon: <Code size={12} />, action: () => {
        setShowApiModal(true);
        setShowContextMenu(false);
      }},
      { label: 'Describe', icon: <FileText size={12} />, action: () => console.log('Describe') },
      { label: 'Edit Data', icon: <Edit2 size={12} />, action: () => console.log('Edit Data') },
      { label: 'Copy DDL', icon: <Copy size={12} />, action: () => console.log('Copy DDL') },
      { label: contextObject.isFavorite ? 'Remove from Favorites' : 'Add to Favorites', 
        icon: <Star size={12} />, action: () => console.log('Toggle Favorite') },
      { label: 'Properties', icon: <Settings size={12} />, action: () => console.log('Properties') },
    ];

    return (
      <div 
        className="fixed z-50 rounded shadow-lg border py-1 min-w-48"
        style={{ 
          backgroundColor: colors.dropdownBg,
          borderColor: colors.dropdownBorder,
          top: contextMenuPosition.y,
          left: Math.min(contextMenuPosition.x, window.innerWidth - 200),
          boxShadow: '0 2px 8px rgba(0,0,0,0.15)'
        }}
        onClick={() => setShowContextMenu(false)}
      >
        {menuItems.map((item, index) => (
          item.separator ? (
            <div key={`sep-${index}`} className="border-t my-1" style={{ borderColor: colors.border }} />
          ) : (
            <button
              key={item.label}
              onClick={item.action}
              className="w-full px-3 py-2 text-sm text-left hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}
            >
              {item.icon}
              {item.label}
            </button>
          )
        ))}
      </div>
    );
  };

  // Render Connection Manager
  const renderConnectionManager = () => {
    if (!showConnectionManager) return null;

    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="rounded w-full max-w-2xl max-h-[80vh] overflow-auto" style={{ 
          backgroundColor: colors.modalBg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b sticky top-0" style={{ borderColor: colors.border, backgroundColor: colors.modalBg }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Connection Manager</h3>
            <button onClick={() => setShowConnectionManager(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}>
              <X size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          
          <div className="p-4">
            <div className="space-y-3">
              {connections.map(conn => (
                <div key={conn.id}
                  className={`p-3 rounded border cursor-pointer transition-colors hover-lift ${
                    activeConnection === conn.id ? 'border-primary' : 'hover:border-primary/50'
                  }`}
                  style={{ 
                    backgroundColor: activeConnection === conn.id ? colors.selected : colors.card,
                    borderColor: activeConnection === conn.id ? colors.primary : colors.border
                  }}
                  onClick={() => {
                    setActiveConnection(conn.id);
                    setShowConnectionManager(false);
                  }}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-3 h-3 rounded-full" style={{ backgroundColor: conn.color }} />
                      <div>
                        <div className="text-sm font-medium truncate" style={{ color: colors.text }}>{conn.name}</div>
                        <div className="text-xs truncate" style={{ color: colors.textSecondary }}>
                          {conn.username}@{conn.host}:{conn.port}/{conn.service}
                        </div>
                      </div>
                    </div>
                    {activeConnection === conn.id && <Check size={16} style={{ color: colors.primary }} />}
                  </div>
                </div>
              ))}
              
              <button className="w-full p-4 rounded border-2 border-dashed hover:border-primary/50 transition-colors flex items-center justify-center gap-2 hover-lift"
                style={{ borderColor: colors.border, color: colors.text }}>
                <Plus size={16} />
                Add New Connection
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  // Left Sidebar Component
  const LeftSidebar = () => (
    <div className={`w-full md:w-64 border-r flex flex-col absolute md:relative inset-y-0 left-0 z-40 transform transition-transform duration-300 ease-in-out ${
      isLeftSidebarVisible ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
    }`} style={{ 
      backgroundColor: colors.sidebar,
      borderColor: colors.border
    }}>
      {/* Mobile sidebar header */}
      <div className="flex items-center justify-between p-3 border-b md:hidden" style={{ borderColor: colors.border }}>
        <h3 className="text-sm font-semibold" style={{ color: colors.text }}>
          Schema Explorer
        </h3>
        <button 
          onClick={() => setIsLeftSidebarVisible(false)}
          className="p-1.5 rounded hover:bg-opacity-50 transition-colors"
          style={{ backgroundColor: colors.hover }}
        >
          <X size={16} style={{ color: colors.text }} />
        </button>
      </div>

      {/* Connection Info */}
      <div className="p-3 border-b" style={{ borderColor: colors.border }}>
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full" style={{ backgroundColor: colors.connectionOnline }} />
            <span className="text-sm font-medium truncate" style={{ color: colors.text }}>
              CBX_DMX
            </span>
          </div>
          <div className="flex gap-1">
            <button className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}
              onClick={() => setShowConnectionManager(true)}>
              <Server size={12} style={{ color: colors.textSecondary }} />
            </button>
            <button className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}>
              <RefreshCw size={12} style={{ color: colors.textSecondary }} />
            </button>
          </div>
        </div>
        <div className="text-xs truncate" style={{ color: colors.textSecondary }}>
          db-prod.company.com:1521/ORCL
        </div>
      </div>

      {/* Filter Controls */}
      <div className="p-3 border-b" style={{ borderColor: colors.border }}>
        <div className="space-y-2">
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
            <input
              type="text"
              placeholder="Filter objects..."
              className="w-full pl-8 pr-3 py-2 rounded text-sm focus:outline-none hover-lift"
              style={{ 
                backgroundColor: colors.inputBg,
                border: `1px solid ${colors.inputBorder}`,
                color: colors.text
              }}
            />
          </div>
          <select className="w-full px-3 py-2 rounded text-sm focus:outline-none hover-lift"
            style={{ 
              backgroundColor: colors.inputBg,
              border: `1px solid ${colors.inputBorder}`,
              color: colors.text
            }}>
            <option>Owner: All</option>
            <option>Owner: HR</option>
            <option>Owner: SCOTT</option>
          </select>
        </div>
      </div>

      {/* Object Tree */}
      <div className="flex-1 overflow-auto p-2">
        {renderObjectTreeSection('Tables', 'tables', schemaObjects.tables)}
        {renderObjectTreeSection('Views', 'views', schemaObjects.views)}
        {renderObjectTreeSection('Procedures', 'procedures', schemaObjects.procedures)}
        {renderObjectTreeSection('Functions', 'functions', schemaObjects.functions)}
        {renderObjectTreeSection('Packages', 'packages', schemaObjects.packages)}
        {renderObjectTreeSection('Sequences', 'sequences', schemaObjects.sequences)}
        {renderObjectTreeSection('Synonyms', 'synonyms', schemaObjects.synonyms)}
        {renderObjectTreeSection('Types', 'types', schemaObjects.types)}
        {renderObjectTreeSection('Triggers', 'triggers', schemaObjects.triggers)}
      </div>
    </div>
  );

  // Close context menu on outside click
  useEffect(() => {
    const handleClickOutside = () => setShowContextMenu(false);
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, []);

  return (
    <div className="flex flex-col h-screen overflow-hidden" style={{ 
      backgroundColor: colors.bg,
      color: colors.text,
      fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif',
      fontSize: '13px'
    }}>
      <style>{`
        @keyframes fadeInUp {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }
        
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
        
        .animate-fade-in-up {
          animation: fadeInUp 0.2s ease-out;
        }
        
        .animate-spin {
          animation: spin 1s linear infinite;
        }
        
        .text-blue-400 { color: ${colors.primaryLight}; }
        .text-green-400 { color: ${colors.success}; }
        .text-purple-400 { color: ${isDark ? 'rgb(167 139 250)' : '#8b5cf6'}; }
        .text-orange-400 { color: ${colors.warning}; }
        .text-red-400 { color: ${colors.error}; }
        .text-gray-500 { color: ${colors.textTertiary}; }
        
        /* Custom scrollbar - EXACT from Dashboard */
        ::-webkit-scrollbar {
          width: 6px;
          height: 6px;
        }
        
        ::-webkit-scrollbar-track {
          background: ${colors.border};
          border-radius: 4px;
        }
        
        ::-webkit-scrollbar-thumb {
          background: ${colors.textTertiary};
          border-radius: 4px;
        }
        
        ::-webkit-scrollbar-thumb:hover {
          background: ${colors.textSecondary};
        }
        
        /* Mobile optimizations - EXACT from Dashboard */
        @media (max-width: 640px) {
          .text-xs { font-size: 11px; }
          .text-sm { font-size: 12px; }
          .text-lg { font-size: 16px; }
          .text-xl { font-size: 18px; }
          .text-2xl { font-size: 20px; }
        }
        
        /* Focus styles */
        input:focus, button:focus {
          outline: 2px solid ${colors.primary}40;
          outline-offset: 2px;
        }
        
        /* Hover effects - EXACT from Dashboard */
        .hover-lift:hover {
          transform: translateY(-2px);
          transition: transform 0.2s ease;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        /* Dashboard-like styling for consistency */
        .gradient-bg {
          background: linear-gradient(135deg, ${colors.primary}20 0%, ${colors.info}20 100%);
        }
      `}</style>

      {/* Desktop Header */}
      <div className="hidden md:flex items-center justify-between h-10 px-4 border-b" style={{ 
        backgroundColor: colors.header,
        borderColor: colors.border
      }}>
        <div className="flex -ml-3 items-center gap-4">
          <span className="px-3 py-1.5 text-sm font-medium rounded transition-colors hover-lift uppercase"
            style={{ color: colors.text, backgroundColor: colors.hover }}>
            Schema Browser
          </span>
        </div>

        <div className="flex items-center gap-2">
          {/* Search Bar */}
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
            <input
              type="text"
              placeholder="Search objects..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-7 pr-3 py-1.5 rounded text-sm focus:outline-none w-64 hover-lift"
              style={{ 
                backgroundColor: colors.inputBg,
                border: `1px solid ${colors.inputBorder}`,
                color: colors.text
              }}
            />
          </div>

          {/* Theme Toggle */}
          <button
            onClick={toggleTheme}
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}
          >
            {isDark ? <Sun size={14} style={{ color: colors.textSecondary }} /> : <Moon size={14} style={{ color: colors.textSecondary }} />}
          </button>
        </div>
      </div>

      {/* MAIN CONTENT AREA */}
      <div className="flex flex-1 overflow-hidden relative">
        {/* Left sidebar overlay */}
        {isLeftSidebarVisible && (
          <div 
            className="fixed inset-0 bg-black bg-opacity-50 z-30 md:hidden"
            onClick={() => setIsLeftSidebarVisible(false)}
          />
        )}

        {/* LEFT SIDEBAR - Object Explorer */}
        <LeftSidebar />

        {/* MAIN WORK AREA */}
        <div className="flex-1 flex flex-col overflow-hidden" style={{ backgroundColor: colors.main }}>
          {/* TAB BAR */}
          <div className="flex items-center border-b h-9" style={{ 
            backgroundColor: colors.card,
            borderColor: colors.border
          }}>
            <div className="flex items-center flex-1 overflow-x-auto">
              {tabs.map(tab => (
                <div
                  key={tab.id}
                  className={`flex items-center gap-2 px-3 sm:px-4 py-2 border-r cursor-pointer min-w-32 sm:min-w-40 hover-lift ${
                    tab.isActive ? '' : 'hover:bg-opacity-50 transition-colors'
                  }`}
                  style={{ 
                    backgroundColor: tab.isActive ? colors.selected : 'transparent',
                    borderRightColor: colors.border,
                    borderTop: tab.isActive ? `2px solid ${colors.tabActive}` : 'none'
                  }}
                  onClick={() => {
                    // Find and activate the tab's object
                    const allObjects = Object.values(schemaObjects).flat();
                    const found = allObjects.find(obj => obj.id === tab.objectId);
                    if (found) {
                      handleObjectSelect(found, tab.type);
                    }
                  }}
                >
                  <div className="flex items-center gap-2 flex-1 min-w-0">
                    {getObjectIcon(tab.type)}
                    <span className="text-sm truncate" style={{ 
                      color: tab.isActive ? colors.tabActive : colors.tabInactive,
                      fontWeight: tab.isActive ? '600' : '400'
                    }}>
                      {tab.name}
                      {tab.isDirty && ' *'}
                    </span>
                  </div>
                  {tabs.length > 1 && (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setTabs(tabs.filter(t => t.id !== tab.id));
                        if (tab.isActive) {
                          const remainingTab = tabs.find(t => t.id !== tab.id);
                          if (remainingTab) {
                            const allObjects = Object.values(schemaObjects).flat();
                            const found = allObjects.find(obj => obj.id === remainingTab.objectId);
                            if (found) {
                              handleObjectSelect(found, remainingTab.type);
                            }
                          }
                        }
                      }}
                      className="p-0.5 rounded opacity-0 hover:opacity-100 hover:bg-opacity-50 transition-colors hover-lift"
                      style={{ backgroundColor: colors.hover }}
                    >
                      <X size={12} style={{ color: colors.textSecondary }} />
                    </button>
                  )}
                </div>
              ))}
              <button
                className="px-3 sm:px-4 py-2 border-r hover:bg-opacity-50 transition-colors hover-lift"
                style={{ borderRightColor: colors.border, backgroundColor: colors.hover }}
                onClick={() => console.log('New tab')}
              >
                <Plus size={14} style={{ color: colors.textSecondary }} />
              </button>
            </div>
          </div>

          {/* OBJECT DETAILS AREA */}
          <div className="flex-1 overflow-hidden flex flex-col">
            {/* Object Properties Header */}
            {activeObject && (
              <div className="p-3 sm:p-4 border-b" style={{ 
                borderColor: colors.border,
                backgroundColor: colors.card
              }}>
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <div className="flex items-center gap-2 sm:gap-4">
                    <div className="flex items-center gap-2">
                      {getObjectIcon(activeObject.type)}
                      <span className="text-sm sm:text-base font-semibold truncate" style={{ color: colors.text }}>
                        {activeObject.owner}.{activeObject.name}
                      </span>
                      <span className="text-xs px-2 py-0.5 rounded shrink-0" style={{ 
                        backgroundColor: activeObject.status === 'VALID' ? `${colors.success}20` : `${colors.error}20`,
                        color: activeObject.status === 'VALID' ? colors.success : colors.error
                      }}>
                        {activeObject.status}
                      </span>
                    </div>
                    <div className="flex items-center gap-2 sm:gap-4">
                      {activeObject.rowCount && (
                        <span className="text-xs sm:text-sm truncate" style={{ color: colors.textSecondary }}>
                          {activeObject.rowCount.toLocaleString()} rows
                        </span>
                      )}
                      {activeObject.size && (
                        <span className="text-xs sm:text-sm truncate" style={{ color: colors.textSecondary }}>
                          {activeObject.size}
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-2 mt-2 sm:mt-0">
                    <button onClick={() => setShowContextMenu(!showContextMenu)} className="px-2 sm:px-3 py-1.5 text-xs sm:text-sm rounded hover:bg-opacity-50 transition-colors flex items-center gap-1 sm:gap-2 hover-lift"
                      style={{ backgroundColor: colors.hover, color: colors.text }}>
                      <Star size={12} />
                      <span className="hidden sm:inline">Favorite</span>
                    </button>
                    <button 
                      onClick={() => setShowApiModal(true)} 
                      className="px-2 sm:px-3 py-1.5 text-xs sm:text-sm rounded hover:opacity-90 transition-colors flex items-center gap-1 sm:gap-2 hover-lift"
                      style={{ backgroundColor: colors.primaryDark, color: colors.white }}
                    >
                      <Code size={12} />
                      <span className="hidden sm:inline">Generate API</span>
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Detail Tabs */}
            {activeObject && (
              <div className="flex items-center border-b overflow-x-auto" style={{ 
                backgroundColor: colors.card,
                borderColor: colors.border
              }}>
                {getTabsForObject().map(tab => (
                  <button
                    key={tab}
                    onClick={() => setActiveTab(tab.toLowerCase())}
                    className={`px-3 sm:px-4 py-2 text-xs sm:text-sm font-medium border-b-2 transition-colors hover-lift whitespace-nowrap ${
                      activeTab === tab.toLowerCase() ? '' : 'hover:bg-opacity-50'
                    }`}
                    style={{ 
                      borderBottomColor: activeTab === tab.toLowerCase() ? colors.tabActive : 'transparent',
                      color: activeTab === tab.toLowerCase() ? colors.tabActive : colors.tabInactive,
                      backgroundColor: 'transparent'
                    }}
                  >
                    {tab}
                  </button>
                ))}
              </div>
            )}

            {/* Tab Content */}
            <div className="flex-1 overflow-hidden p-3 sm:p-4" style={{ backgroundColor: colors.card }}>
              {activeObject ? renderTabContent() : (
                <div className="h-full flex flex-col items-center justify-center p-4">
                  <Database size={48} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mb-4" />
                  <p className="text-sm text-center" style={{ color: colors.textSecondary }}>
                    Select an object from the schema browser to view details
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* STATUS BAR */}
      <div className="hidden sm:flex items-center justify-between h-8 px-4 text-xs border-t" style={{ 
        backgroundColor: colors.header,
        color: colors.textSecondary,
        borderColor: colors.border
      }}>
        <div className="flex items-center gap-4 overflow-x-auto">
          <span>HR @ db-prod.company.com:1521/ORCL</span>
          <span className="opacity-75">|</span>
          <span>{Object.values(schemaObjects).flat().length} Objects</span>
          <span className="opacity-75">|</span>
          <span>Ready</span>
        </div>
        <div className="hidden md:flex items-center gap-4">
          <span>F4: Describe</span>
          <span>F5: Refresh</span>
          <span>F6: Switch Panels</span>
          <span>F9: Execute</span>
        </div>
      </div>

      {/* Mobile Status Bar */}
      <div className="sm:hidden flex items-center justify-between h-8 px-3 text-xs border-t" style={{ 
        backgroundColor: colors.header,
        color: colors.textSecondary,
        borderColor: colors.border
      }}>
        <div className="truncate">
          HR @ db-prod.company.com
        </div>
        <div>
          {Object.values(schemaObjects).flat().length} Objects
        </div>
      </div>

      {/* MODALS & CONTEXT MENUS */}
      {renderContextMenu()}
      {renderConnectionManager()}

      {showApiModal && (
        <ApiGenerationModal
          isOpen={showApiModal}
          onClose={() => setShowApiModal(false)}
          selectedObject={selectedForApiGeneration || activeObject}
          colors={colors}
          theme={theme}
        />
      )}

    </div>
  );
};

export default SchemaBrowser;