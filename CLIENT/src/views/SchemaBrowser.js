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
  FileCode, ChevronsUp, ChevronsDown, AlertTriangle
} from 'lucide-react';

const SchemaBrowser = () => {
  const [theme, setTheme] = useState('dark'); // Default to dark theme like APIScript
  const isDark = theme === 'dark';

  // Using APIScript's color system for consistency
  const postmanColors = {
    light: {
      bg: '#F6F6F6',
      sidebar: '#FFFFFF',
      main: '#FFFFFF',
      header: '#FFFFFF',
      card: '#FFFFFF',
      text: '#2D2D2D',
      textSecondary: '#757575',
      textTertiary: '#9E9E9E',
      border: '#E0E0E0',
      borderLight: '#F0F0F0',
      borderDark: '#CCCCCC',
      hover: '#F5F5F5',
      active: '#EEEEEE',
      selected: '#E8F4FD',
      primary: '#FF6C37',
      primaryLight: '#FF8B5C',
      primaryDark: '#E55B2E',
      method: {
        GET: '#0F9D58',
        POST: '#FF9800',
        PUT: '#4285F4',
        DELETE: '#DB4437',
        PATCH: '#7B1FA2',
        HEAD: '#607D8B',
        OPTIONS: '#795548',
        LINK: '#039BE5',
        UNLINK: '#F4511E'
      },
      success: '#0F9D58',
      warning: '#F4B400',
      error: '#DB4437',
      info: '#4285F4',
      tabActive: '#FF6C37',
      tabInactive: '#757575',
      sidebarActive: '#FF6C37',
      sidebarHover: '#F5F5F5',
      inputBg: '#FFFFFF',
      inputBorder: '#E0E0E0',
      tableHeader: '#F5F5F5',
      tableRow: '#FFFFFF',
      tableRowHover: '#FAFAFA',
      dropdownBg: '#FFFFFF',
      dropdownBorder: '#E0E0E0',
      modalBg: '#FFFFFF',
      modalBorder: '#E0E0E0',
      // Schema Browser specific
      gridRowEven: '#FAFAFA',
      gridRowOdd: '#FFFFFF',
      gridHeader: '#F5F5F5',
      gridBorder: '#E0E0E0',
      connectionOnline: '#0F9D58',
      connectionOffline: '#DB4437',
      connectionIdle: '#F4B400',
      objectType: {
        table: '#4285F4', // Blue
        view: '#0F9D58', // Green
        procedure: '#7B1FA2', // Purple
        function: '#FF9800', // Orange
        package: '#795548', // Brown
        sequence: '#607D8B', // Dark blue gray
        synonym: '#039BE5', // Teal
        type: '#3F51B5', // Indigo
        trigger: '#E91E63', // Pink
        index: '#009688', // Teal
        constraint: '#DB4437' // Red
      }
    },
    dark: {
      bg: '#0D0D0D',
      sidebar: '#1A1A1A',
      main: '#151515',
      header: '#1A1A1A',
      card: '#1E1E1E',
      text: '#E0E0E0',
      textSecondary: '#AAAAAA',
      textTertiary: '#888888',
      border: '#333333',
      borderLight: '#2A2A2A',
      borderDark: '#404040',
      hover: '#2A2A2A',
      active: '#333333',
      selected: '#2C3E50',
      primary: '#FF6C37',
      primaryLight: '#FF8B5C',
      primaryDark: '#E55B2E',
      method: {
        GET: '#34A853',
        POST: '#FBBC05',
        PUT: '#4285F4',
        DELETE: '#EA4335',
        PATCH: '#A142F4',
        HEAD: '#8C9EFF',
        OPTIONS: '#A1887F',
        LINK: '#039BE5',
        UNLINK: '#FF7043'
      },
      success: '#34A853',
      warning: '#FBBC05',
      error: '#EA4335',
      info: '#4285F4',
      tabActive: '#FF6C37',
      tabInactive: '#AAAAAA',
      sidebarActive: '#FF6C37',
      sidebarHover: '#2A2A2A',
      inputBg: '#1A1A1A',
      inputBorder: '#333333',
      tableHeader: '#2A2A2A',
      tableRow: '#1E1E1E',
      tableRowHover: '#252525',
      dropdownBg: '#1E1E1E',
      dropdownBorder: '#333333',
      modalBg: '#1E1E1E',
      modalBorder: '#333333',
      // Schema Browser specific
      gridRowEven: '#252526',
      gridRowOdd: '#2D2D30',
      gridHeader: '#3E3E42',
      gridBorder: '#404040',
      connectionOnline: '#34A853',
      connectionOffline: '#EA4335',
      connectionIdle: '#FBBC05',
      objectType: {
        table: '#4FC3F7',
        view: '#81C784',
        procedure: '#BA68C8',
        function: '#FFB74D',
        package: '#A1887F',
        sequence: '#90A4AE',
        synonym: '#4DB6AC',
        type: '#7986CB',
        trigger: '#F06292',
        index: '#4DB6AC',
        constraint: '#E57373'
      }
    }
  };

  const colors = isDark ? postmanColors.dark : postmanColors.light;

  // Professional Data Structures - Oracle Standards
  const [connections, setConnections] = useState([
    {
      id: 'conn-1',
      name: 'HR_PROD',
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

  // Get Object Icon with color scheme from APIScript
  const getObjectIcon = (type) => {
    const iconProps = { size: 14, style: { color: colors.objectType[type.toLowerCase()] || colors.textSecondary } };
    
    switch(type.toUpperCase()) {
      case 'TABLE': return <Table {...iconProps} />;
      case 'VIEW': return <FileText {...iconProps} />;
      case 'PROCEDURE': return <Terminal {...iconProps} />;
      case 'FUNCTION': return <Code {...iconProps} />;
      case 'PACKAGE': return <Package {...iconProps} />;
      case 'SEQUENCE': return <Hash {...iconProps} />;
      case 'SYNONYM': return <Link {...iconProps} />;
      case 'TYPE': return <Type {...iconProps} />;
      case 'TRIGGER': return <Zap {...iconProps} />;
      case 'INDEX': return <BarChart {...iconProps} />;
      default: return <Database {...iconProps} />;
    }
  };

  // Handle Object Selection
  const handleObjectSelect = (object, type) => {
    setActiveObject(object);
    
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
            <span>{title}</span>
          </div>
          <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
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
                <div className="flex items-center gap-2">
                  {getObjectIcon(type.slice(0, -1))}
                  <span className="text-sm truncate">{obj.name}</span>
                  {obj.status !== 'VALID' && (
                    <AlertCircle size={10} style={{ color: colors.error }} />
                  )}
                </div>
                <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  {obj.isFavorite && (
                    <Star size={10} fill="#FFB300" style={{ color: '#FFB300' }} />
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
      <div className="border rounded-sm" style={{ 
        borderColor: colors.gridBorder,
        backgroundColor: colors.card
      }}>
        {/* Toolbar */}
        <div className="flex items-center justify-between p-2 border-b" style={{ 
          borderColor: colors.gridBorder,
          backgroundColor: colors.gridHeader
        }}>
          <div className="text-sm font-medium" style={{ color: colors.text }}>
            Columns ({activeObject.columns?.length || 0})
          </div>
          <div className="flex items-center gap-2">
            <button className="px-2 py-1 text-xs rounded-sm hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}>
              <Copy size={10} className="inline mr-1" />
              Copy
            </button>
            <button className="px-2 py-1 text-xs rounded-sm hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}>
              <Download size={10} className="inline mr-1" />
              Export
            </button>
          </div>
        </div>

        {/* Columns Grid */}
        <div className="overflow-auto">
          <table className="w-full" style={{ borderCollapse: 'collapse' }}>
            <thead style={{ 
              backgroundColor: colors.gridHeader,
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
                  minWidth: '150px'
                }}>Column Name</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary,
                  minWidth: '120px'
                }}>Data Type</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary,
                  width: '60px'
                }}>Null</th>
                <th className="text-left p-2 text-xs font-medium border-b" style={{ 
                  borderColor: colors.gridBorder,
                  color: colors.textSecondary,
                  width: '80px'
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
                  <td className="p-2 text-xs font-medium" style={{ color: colors.text }}>{col.name}</td>
                  <td className="p-2 text-xs font-mono" style={{ color: colors.text }}>{col.type}</td>
                  <td className="p-2 text-xs text-center">
                    <div className={`inline-flex items-center justify-center w-5 h-5 rounded-sm ${
                      col.nullable === 'Y' ? 'bg-green-500/10 text-green-600' : 'bg-red-500/10 text-red-600'
                    }`}>
                      {col.nullable === 'Y' ? 'Y' : 'N'}
                    </div>
                  </td>
                  <td className="p-2">
                    {col.key && (
                      <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                        col.key === 'PK' ? 'bg-blue-100 text-blue-800' :
                        col.key === 'FK' ? 'bg-purple-100 text-purple-800' :
                        'bg-green-100 text-green-800'
                      }`}>
                        {col.key}
                      </span>
                    )}
                  </td>
                  <td className="p-2 text-xs font-mono" style={{ color: colors.textSecondary }}>
                    {col.defaultValue || <span className="italic">NULL</span>}
                  </td>
                  <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>
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
        <div className="flex items-center justify-between p-2 border-b" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="flex items-center gap-2">
            <button className="px-3 py-1.5 rounded-sm text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2 hover-lift"
              style={{ backgroundColor: colors.primary, color: 'white' }}>
              <Play size={12} />
              Execute
            </button>
            <button className="px-3 py-1.5 rounded-sm text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}>
              Commit
            </button>
            <button className="px-3 py-1.5 rounded-sm text-sm font-medium hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover, color: colors.text }}>
              Rollback
            </button>
            <div className="ml-4 flex items-center gap-2">
              <span className="text-sm" style={{ color: colors.textSecondary }}>Auto-refresh:</span>
              <select className="px-2 py-1 border rounded-sm text-sm focus:outline-none hover-lift"
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
          <div className="flex items-center gap-4">
            <span className="text-sm" style={{ color: colors.textSecondary }}>
              Page: 1 of 1 | Rows: 1-{data.length} of {activeObject.rowCount?.toLocaleString()}
            </span>
            <div className="flex items-center gap-2">
              <button className="p-1 rounded-sm hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}>
                <ChevronLeft size={14} style={{ color: colors.textSecondary }} />
              </button>
              <button className="p-1 rounded-sm hover:bg-opacity-50 transition-colors hover-lift"
                style={{ backgroundColor: colors.hover }}>
                <ChevronRight size={14} style={{ color: colors.textSecondary }} />
              </button>
            </div>
          </div>
        </div>

        {/* Data Grid */}
        <div className="flex-1 overflow-auto">
          <div className="border rounded-sm" style={{ 
            borderColor: colors.gridBorder,
            backgroundColor: colors.card
          }}>
            <table className="w-full" style={{ borderCollapse: 'collapse' }}>
              <thead style={{ 
                backgroundColor: colors.gridHeader,
                position: 'sticky',
                top: 0,
                zIndex: 10
              }}>
                <tr>
                  {activeObject.columns?.slice(0, 8).map(col => (
                    <th key={col.name} className="text-left p-2 text-xs font-medium border-b" style={{ 
                      borderColor: colors.gridBorder,
                      color: colors.textSecondary,
                      whiteSpace: 'nowrap'
                    }}>
                      {col.name}
                      {col.key && (
                        <span className={`ml-1 px-1 py-0.5 rounded text-xs ${
                          col.key === 'PK' ? 'bg-blue-100 text-blue-800' :
                          col.key === 'FK' ? 'bg-purple-100 text-purple-800' :
                          'bg-green-100 text-green-800'
                        }`}>
                          {col.key}
                        </span>
                      )}
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
                    {activeObject.columns?.slice(0, 8).map(col => (
                      <td key={col.name} className="p-2 text-xs border-r" style={{ 
                        borderColor: colors.gridBorder,
                        color: colors.text,
                        whiteSpace: 'nowrap'
                      }}>
                        {row[col.name] !== null && row[col.name] !== undefined ? row[col.name] : (
                          <span className="italic" style={{ color: colors.textTertiary }}>NULL</span>
                        )}
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
          <div className="text-xs" style={{ color: colors.textSecondary }}>
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
      <div className="border rounded-sm" style={{ 
        borderColor: colors.gridBorder,
        backgroundColor: colors.card
      }}>
        <div className="p-2 border-b" style={{ 
          borderColor: colors.gridBorder,
          backgroundColor: colors.gridHeader
        }}>
          <div className="text-sm font-medium" style={{ color: colors.text }}>
            Parameters ({activeObject.parameters?.length || 0})
          </div>
        </div>
        <table className="w-full" style={{ borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: colors.gridHeader }}>
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
                width: '80px'
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
                <td className="p-2 text-xs font-medium" style={{ color: colors.text }}>{param.name}</td>
                <td className="p-2">
                  <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                    param.type === 'IN' ? 'bg-blue-100 text-blue-800' :
                    param.type === 'OUT' ? 'bg-purple-100 text-purple-800' :
                    'bg-green-100 text-green-800'
                  }`}>
                    {param.type}
                  </span>
                </td>
                <td className="p-2 text-xs font-mono" style={{ color: colors.text }}>{param.datatype}</td>
                <td className="p-2 text-xs font-mono" style={{ color: colors.textSecondary }}>
                  {param.defaultValue || <span className="italic">NULL</span>}
                </td>
              </tr>
            ))}
            {activeObject.type === 'FUNCTION' && activeObject.returnType && (
              <tr className="border-t" style={{ borderColor: colors.gridBorder }}>
                <td className="p-2 text-xs font-medium" style={{ color: colors.textSecondary }}>-</td>
                <td className="p-2 text-xs font-medium" style={{ color: colors.text }}>RETURN</td>
                <td className="p-2">
                  <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800">
                    OUT
                  </span>
                </td>
                <td className="p-2 text-xs font-mono font-medium" style={{ color: colors.text }}>
                  {activeObject.returnType}
                </td>
                <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>
                  {activeObject.deterministic ? 'DETERMINISTIC' : ''}
                  {activeObject.pipelined ? ' | PIPELINED' : ''}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );

  // Render DDL Tab
  const renderDDLTab = () => (
    <div className="flex-1 overflow-auto">
      <div className="border rounded-sm p-4" style={{ 
        borderColor: colors.border,
        backgroundColor: colors.card
      }}>
        <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed" style={{ 
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
      <div className="border rounded-sm" style={{ 
        borderColor: colors.gridBorder,
        backgroundColor: colors.card
      }}>
        <div className="p-2 border-b" style={{ 
          borderColor: colors.gridBorder,
          backgroundColor: colors.gridHeader
        }}>
          <div className="text-sm font-medium" style={{ color: colors.text }}>
            Constraints ({activeObject.constraints?.length || 0})
          </div>
        </div>
        <table className="w-full" style={{ borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: colors.gridHeader }}>
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
                <td className="p-2 text-xs font-medium" style={{ color: colors.text }}>{con.name}</td>
                <td className="p-2">
                  <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                    con.type === 'PRIMARY KEY' ? 'bg-blue-100 text-blue-800' :
                    con.type === 'FOREIGN KEY' ? 'bg-purple-100 text-purple-800' :
                    con.type === 'UNIQUE' ? 'bg-green-100 text-green-800' :
                    'bg-yellow-100 text-yellow-800'
                  }`}>
                    {con.type}
                  </span>
                </td>
                <td className="p-2 text-xs" style={{ color: colors.text }}>{con.columns}</td>
                <td className="p-2 text-xs" style={{ color: colors.textSecondary }}>
                  {con.refTable || '-'}
                </td>
                <td className="p-2">
                  <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                    con.status === 'ENABLED' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
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
  );

  // Render Indexes Tab
  const renderIndexesTab = () => (
    <div className="flex-1 overflow-auto">
      <div className="border rounded-sm" style={{ 
        borderColor: colors.gridBorder,
        backgroundColor: colors.card
      }}>
        <div className="p-2 border-b" style={{ 
          borderColor: colors.gridBorder,
          backgroundColor: colors.gridHeader
        }}>
          <div className="text-sm font-medium" style={{ color: colors.text }}>
            Indexes ({activeObject.indexes?.length || 0})
          </div>
        </div>
        <table className="w-full" style={{ borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: colors.gridHeader }}>
              <th className="text-left p-2 text-xs font-medium border-b" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>Index Name</th>
              <th className="text-left p-2 text-xs font-medium border-b" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>Type</th>
              <th className="text-left p-2 text-xs font-medium border-b" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>Columns</th>
              <th className="text-left p-2 text-xs font-medium border-b" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>Uniqueness</th>
              <th className="text-left p-2 text-xs font-medium border-b" style={{ borderColor: colors.gridBorder, color: colors.textSecondary }}>Status</th>
            </tr>
          </thead>
          <tbody>
            {activeObject.indexes?.map((idx, index) => (
              <tr 
                key={idx.name}
                className="hover:bg-opacity-50 transition-colors"
                style={{ 
                  backgroundColor: index % 2 === 0 ? colors.gridRowEven : colors.gridRowOdd,
                  borderBottom: `1px solid ${colors.gridBorder}`
                }}
              >
                <td className="p-2 text-xs font-medium" style={{ color: colors.text }}>{idx.name}</td>
                <td className="p-2 text-xs" style={{ color: colors.text }}>{idx.type}</td>
                <td className="p-2 text-xs" style={{ color: colors.text }}>{idx.columns}</td>
                <td className="p-2">
                  <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                    idx.uniqueness === 'UNIQUE' ? 'bg-green-100 text-green-800' : 'bg-blue-100 text-blue-800'
                  }`}>
                    {idx.uniqueness}
                  </span>
                </td>
                <td className="p-2">
                  <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                    idx.status === 'VALID' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                  }`}>
                    {idx.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
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
        <div className="border rounded-sm" style={{ 
          borderColor: colors.border,
          backgroundColor: colors.card
        }}>
          <div className="p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-medium mb-4" style={{ color: colors.text }}>
              Object Properties
            </h3>
            <div className="grid grid-cols-2 gap-4">
              {properties.map((prop, index) => (
                <div key={index} className="space-y-1">
                  <div className="text-xs font-medium" style={{ color: colors.textSecondary }}>
                    {prop.label}
                  </div>
                  <div className="text-sm" style={{ color: colors.text }}>
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
        return ['Columns', 'Data', 'Constraints', 'Indexes', 'Grants', 'Triggers', 'DDL', 'Properties'];
      case 'VIEW':
        return ['Definition', 'Columns', 'Dependencies', 'DDL', 'Properties'];
      case 'PROCEDURE':
        return ['Parameters', 'Dependencies', 'DDL', 'Properties'];
      case 'FUNCTION':
        return ['Parameters', 'Dependencies', 'DDL', 'Properties'];
      case 'PACKAGE':
        return ['Specification', 'Body', 'Dependencies', 'DDL', 'Properties'];
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
      case 'indexes':
        return renderIndexesTab();
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
      { label: 'Generate API', icon: <Code size={12} />, action: () => console.log('Generate API') },
      { label: 'Describe', icon: <FileText size={12} />, action: () => console.log('Describe') },
      { label: 'Edit Data', icon: <Edit2 size={12} />, action: () => console.log('Edit Data') },
      { label: 'Export Data', icon: <Download size={12} />, submenu: ['CSV', 'JSON', 'Excel'] },
      { label: 'Copy DDL', icon: <Copy size={12} />, action: () => console.log('Copy DDL') },
      { label: 'Create Synonym', icon: <Link size={12} />, action: () => console.log('Create Synonym') },
      { label: contextObject.isFavorite ? 'Remove from Favorites' : 'Add to Favorites', 
        icon: <Star size={12} />, action: () => console.log('Toggle Favorite') },
      { label: 'Properties', icon: <Settings size={12} />, action: () => console.log('Properties') },
    ];

    return (
      <div 
        className="fixed z-50 rounded-sm shadow-lg border py-1 min-w-48"
        style={{ 
          backgroundColor: colors.dropdownBg,
          borderColor: colors.dropdownBorder,
          top: contextMenuPosition.y,
          left: contextMenuPosition.x,
          boxShadow: '0 2px 8px rgba(0,0,0,0.15)'
        }}
        onClick={() => setShowContextMenu(false)}
      >
        {menuItems.map((item, index) => (
          item.separator ? (
            <div key={`sep-${index}`} className="border-t my-1" style={{ borderColor: colors.border }} />
          ) : item.submenu ? (
            <div key={item.label} className="relative group">
              <div className="flex items-center justify-between px-3 py-2 text-sm hover:bg-opacity-50 transition-colors cursor-pointer hover-lift"
                style={{ backgroundColor: colors.hover, color: colors.text }}>
                <div className="flex items-center gap-2">
                  {item.icon}
                  {item.label}
                </div>
                <ChevronRight size={12} style={{ color: colors.textSecondary }} />
              </div>
              <div className="absolute left-full top-0 hidden group-hover:block ml-1">
                <div className="rounded-sm border shadow-lg py-1 min-w-32" style={{ 
                  backgroundColor: colors.dropdownBg,
                  borderColor: colors.dropdownBorder
                }}>
                  {item.submenu.map(sub => (
                    <button key={sub} className="w-full px-3 py-2 text-sm text-left hover:bg-opacity-50 transition-colors hover-lift"
                      style={{ backgroundColor: colors.hover, color: colors.text }}>
                      {sub}
                    </button>
                  ))}
                </div>
              </div>
            </div>
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
        <div className="rounded-sm w-full max-w-2xl" style={{ 
          backgroundColor: colors.modalBg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Connection Manager</h3>
            <button onClick={() => setShowConnectionManager(false)} className="p-1 rounded-sm hover:bg-opacity-50 transition-colors hover-lift"
              style={{ backgroundColor: colors.hover }}>
              <X size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          
          <div className="p-4">
            <div className="space-y-3">
              {connections.map(conn => (
                <div key={conn.id}
                  className={`p-3 rounded-sm border cursor-pointer transition-colors hover-lift ${
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
                        <div className="text-sm font-medium" style={{ color: colors.text }}>{conn.name}</div>
                        <div className="text-xs" style={{ color: colors.textSecondary }}>
                          {conn.username}@{conn.host}:{conn.port}/{conn.service}
                        </div>
                      </div>
                    </div>
                    {activeConnection === conn.id && <Check size={16} style={{ color: colors.primary }} />}
                  </div>
                </div>
              ))}
              
              <button className="w-full p-4 rounded-sm border-2 border-dashed hover:border-primary/50 transition-colors flex items-center justify-center gap-2 hover-lift"
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
      fontSize: '13px' // Match APIScript font size
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
        
        .text-blue-400 { color: #60a5fa; }
        .text-green-400 { color: #34d399; }
        .text-purple-400 { color: #a78bfa; }
        .text-orange-400 { color: #fb923c; }
        .text-red-400 { color: #f87171; }
        .text-gray-500 { color: #9ca3af; }
        
        /* Custom scrollbar */
        ::-webkit-scrollbar {
          width: 8px;
          height: 8px;
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
        
        .prose {
          color: ${colors.textSecondary};
          line-height: 1.6;
        }
        
        .prose p {
          margin-bottom: 1em;
        }
        
        .prose strong {
          color: ${colors.text};
          font-weight: 600;
        }
        
        code {
          font-family: 'SF Mono', Monaco, 'Cascadia Mono', 'Segoe UI Mono', 'Roboto Mono', monospace;
          font-size: 0.875em;
        }
        
        /* Focus styles */
        input:focus, button:focus {
          outline: 2px solid ${colors.primary}40;
          outline-offset: 2px;
        }
        
        /* Hover effects */
        .hover-lift:hover {
          transform: translateY(-2px);
          transition: transform 0.2s ease;
        }
        
        .code-bg {
          background-color: ${isDark ? '#1a1a1a' : '#f8f9fa'};
        }
      `}</style>

      {/* HEADER - Professional Style */}
      <div className="flex items-center justify-between h-10 px-4 border-b" style={{ 
        backgroundColor: colors.header,
        borderColor: colors.border
      }}>
        <div className="flex items-center gap-4">

          {/* Main Menu */}
          <div className="flex items-center gap-1">
            {/* {[
              { icon: <Server size={13} />, label: 'Connections', action: () => setShowConnectionManager(true) },
              { icon: <FolderOpen size={13} />, label: 'Open' },
              { icon: <Save size={13} />, label: 'Save' },
              { icon: <Table size={13} />, label: 'Data' },
              { icon: <Code size={13} />, label: 'API' },
              { icon: <Search size={13} />, label: 'Search' },
              { icon: <Activity size={13} />, label: 'Monitor' }
            ].map((item, index) => (
              <button
                key={index}
                onClick={item.action}
                className="flex items-center gap-1 px-2 py-1.5 rounded text-sm hover:bg-opacity-50 transition-colors hover-lift"
                style={{ 
                  color: colors.textSecondary,
                  backgroundColor: 'transparent'
                }}
              >
                {item.icon}
                <span>{item.label}</span>
              </button>
            ))} */}

            <span className={`px-3 py-1.5 text-sm font-medium rounded transition-colors hover-lift`}>API Code Base</span>
          </div>
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
            onClick={() => setTheme(isDark ? 'light' : 'dark')}
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}
          >
            {isDark ? <Sun size={14} style={{ color: colors.textSecondary }} /> : <Moon size={14} style={{ color: colors.textSecondary }} />}
          </button>

          {/* User Menu */}
          {/* <button className="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}>
            <div className="w-5 h-5 rounded flex items-center justify-center" style={{ 
              backgroundColor: colors.primary 
            }}>
              <User size={10} style={{ color: 'white' }} />
            </div>
            <span className="text-xs" style={{ color: colors.text }}>HR</span>
          </button> */}
        </div>
      </div>

      {/* MAIN CONTENT AREA */}
      <div className="flex flex-1 overflow-hidden">
        {/* LEFT SIDEBAR - Object Explorer */}
        <div className="w-64 border-r flex flex-col" style={{ 
          backgroundColor: colors.sidebar,
          borderColor: colors.border
        }}>
          {/* Connection Info */}
          <div className="p-3 border-b" style={{ borderColor: colors.border }}>
            <div className="flex items-center justify-between mb-2">
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 rounded-full" style={{ backgroundColor: colors.connectionOnline }} />
                <span className="text-sm font-medium" style={{ color: colors.text }}>
                  HR_PROD
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
            <div className="text-xs" style={{ color: colors.textSecondary }}>
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

        {/* MAIN WORK AREA */}
        <div className="flex-1 flex flex-col overflow-hidden">
          {/* TAB BAR */}
          <div className="flex items-center border-b h-9" style={{ 
            backgroundColor: colors.card,
            borderColor: colors.border
          }}>
            <div className="flex items-center flex-1 overflow-x-auto">
              {tabs.map(tab => (
                <div
                  key={tab.id}
                  className={`flex items-center gap-2 px-4 py-2 border-r cursor-pointer min-w-40 hover-lift ${
                    tab.isActive ? '' : 'hover:bg-opacity-50 transition-colors'
                  }`}
                  style={{ 
                    backgroundColor: tab.isActive ? colors.selected : 'transparent',
                    borderRightColor: colors.border,
                    borderTop: tab.isActive ? `2px solid ${colors.primary}` : 'none'
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
                      color: tab.isActive ? colors.primary : colors.textSecondary,
                      fontWeight: tab.isActive ? '600' : '400'
                    }}>
                      {tab.name}
                      {tab.isDirty && ' *'}
                    </span>
                  </div>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      if (tabs.length > 1) {
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
                      }
                    }}
                    className="p-0.5 rounded opacity-0 hover:opacity-100 hover:bg-opacity-50 transition-colors hover-lift"
                    style={{ backgroundColor: colors.hover }}
                  >
                    <X size={12} style={{ color: colors.textSecondary }} />
                  </button>
                </div>
              ))}
              <button
                className="px-4 py-2 border-r hover:bg-opacity-50 transition-colors hover-lift"
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
              <div className="p-4 border-b" style={{ 
                borderColor: colors.border,
                backgroundColor: colors.card
              }}>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="flex items-center gap-2">
                      {getObjectIcon(activeObject.type)}
                      <span className="text-base font-semibold" style={{ color: colors.text }}>
                        {activeObject.owner}.{activeObject.name}
                      </span>
                      <span className="text-xs px-2 py-0.5 rounded" style={{ 
                        backgroundColor: activeObject.status === 'VALID' ? '#E8F5E9' : '#FFEBEE',
                        color: activeObject.status === 'VALID' ? '#2E7D32' : '#C62828'
                      }}>
                        {activeObject.status}
                      </span>
                    </div>
                    {activeObject.rowCount && (
                      <span className="text-sm" style={{ color: colors.textSecondary }}>
                        {activeObject.rowCount.toLocaleString()} rows
                      </span>
                    )}
                    {activeObject.size && (
                      <span className="text-sm" style={{ color: colors.textSecondary }}>
                        {activeObject.size}
                      </span>
                    )}
                  </div>
                  <div className="flex items-center gap-2">
                    <button className="px-3 py-1.5 text-sm rounded hover:bg-opacity-50 transition-colors flex items-center gap-2 hover-lift"
                      style={{ backgroundColor: colors.hover, color: colors.text }}>
                      <Star size={12} />
                      Favorite
                    </button>
                    <button className="px-3 py-1.5 text-sm rounded hover:opacity-90 transition-colors flex items-center gap-2 hover-lift"
                      style={{ backgroundColor: colors.primary, color: 'white' }}>
                      <Code size={12} />
                      Generate API
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Detail Tabs */}
            {activeObject && (
              <div className="flex items-center border-b" style={{ 
                backgroundColor: colors.card,
                borderColor: colors.border
              }}>
                {getTabsForObject().map(tab => (
                  <button
                    key={tab}
                    onClick={() => setActiveTab(tab.toLowerCase())}
                    className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${
                      activeTab === tab.toLowerCase() ? '' : 'hover:bg-opacity-50'
                    }`}
                    style={{ 
                      borderBottomColor: activeTab === tab.toLowerCase() ? colors.primary : 'transparent',
                      color: activeTab === tab.toLowerCase() ? colors.primary : colors.textSecondary,
                      backgroundColor: 'transparent'
                    }}
                  >
                    {tab}
                  </button>
                ))}
              </div>
            )}

            {/* Tab Content */}
            <div className="flex-1 overflow-hidden p-4" style={{ backgroundColor: colors.card }}>
              {activeObject ? renderTabContent() : (
                <div className="h-full flex flex-col items-center justify-center">
                  <Database size={48} style={{ color: colors.textSecondary, opacity: 0.5 }} className="mb-4" />
                  <p className="text-sm" style={{ color: colors.textSecondary }}>
                    Select an object from the schema browser to view details
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* STATUS BAR */}
      <div className="flex items-center justify-between h-8 px-4 text-xs border-t" style={{ 
        backgroundColor: colors.header,
        color: colors.textSecondary,
        borderColor: colors.border
      }}>
        <div className="flex items-center gap-4">
          <span>HR @ db-prod.company.com:1521/ORCL</span>
          <span className="opacity-75">|</span>
          <span>{Object.values(schemaObjects).flat().length} Objects</span>
          <span className="opacity-75">|</span>
          <span>Ready</span>
        </div>
        <div className="flex items-center gap-4">
          <span>F4: Describe</span>
          <span>F5: Refresh</span>
          <span>F6: Switch Panels</span>
          <span>F9: Execute</span>
        </div>
      </div>

      {/* MODALS & CONTEXT MENUS */}
      {renderContextMenu()}
      {renderConnectionManager()}
    </div>
  );
};

export default SchemaBrowser;