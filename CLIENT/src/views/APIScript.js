import React, { useState, useEffect, useRef } from 'react';
import { 
  ChevronRight, 
  ChevronDown,
  Search,
  Plus,
  Play,
  MoreVertical,
  Download,
  Share2,
  Eye,
  EyeOff,
  Copy,
  Trash2,
  Edit2,
  Settings,
  Globe,
  Lock,
  FileText,
  Code,
  GitBranch,
  History,
  Zap,
  Filter,
  Folder,
  FolderOpen,
  Star,
  ExternalLink,
  Upload,
  Users,
  Bell,
  HelpCircle,
  User,
  Moon,
  Sun,
  X,
  Menu,
  Check,
  AlertCircle,
  Clock,
  Activity,
  Database,
  Shield,
  Key,
  Hash,
  Bold,
  Italic,
  Link,
  Image,
  Table,
  Terminal,
  BookOpen,
  LayoutDashboard,
  ShieldCheck,
  DownloadCloud,
  UploadCloud,
  UserCheck,
  Home,
  Cloud,
  Save,
  Printer,
  Inbox,
  Archive,
  Trash,
  UserPlus,
  RefreshCw,
  ChevronLeft,
  ChevronUp,
  Minimize2,
  Maximize2,
  MoreHorizontal,
  Send,
  CheckCircle,
  XCircle,
  Info,
  Layers,
  Package,
  Box,
  FolderPlus,
  FilePlus,
  Wifi,
  Server,
  HardDrive,
  Network,
  Cpu,
  BarChart,
  PieChart,
  LineChart,
  Smartphone,
  Monitor,
  Bluetooth,
  Command,
  Circle,
  Dot,
  List,
  Type,
  FileCode,
  ChevronsLeft,
  ChevronsRight,
  GripVertical,
  Coffee,
  Eye as EyeIcon,
  FileArchive as FileBinary,
  Database as DatabaseIcon,
  ChevronsUpDown,
  Book,
  File,
  MessageSquare,
  Tag,
  Calendar,
  Hash as HashIcon,
  Link as LinkIcon,
  Eye as EyeOpenIcon,
  Clock as ClockIcon,
  Users as UsersIcon,
  Database as DatabaseIcon2,
  Code as CodeIcon2,
  Terminal as TerminalIcon,
  ExternalLink as ExternalLinkIcon,
  Copy as CopyIcon,
  Check as CheckIcon,
  X as XIcon,
  AlertCircle as AlertCircleIcon,
  Info as InfoIcon,
  HelpCircle as HelpCircleIcon,
  Star as StarIcon,
  Book as BookIcon,
  Zap as ZapIcon
} from 'lucide-react';

// Enhanced SyntaxHighlighter Component with safe handling
const SyntaxHighlighter = ({ language, code }) => {
  const highlightSyntax = (code, lang) => {
    // Handle null or undefined code
    if (!code) return '// No code available';
    
    // Ensure code is a string
    const codeString = String(code);
    
    if (lang === 'json') {
      return codeString
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, (match) => {
          let cls = 'text-blue-400';
          if (/^"/.test(match)) {
            if (/:$/.test(match)) {
              cls = 'text-purple-400';
            } else {
              cls = 'text-green-400';
            }
          } else if (/true|false/.test(match)) {
            cls = 'text-orange-400';
          } else if (/null/.test(match)) {
            cls = 'text-red-400';
          }
          return `<span class="${cls}">${match}</span>`;
        });
    }
    
    if (lang === 'javascript' || lang === 'nodejs') {
      return codeString
        .replace(/(\b(?:function|const|let|var|if|else|for|while|return|class|import|export|from|default|async|await|try|catch|finally|throw|new|this)\b)/g, '<span class="text-purple-400">$1</span>')
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"|'(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\'])*')/g, '<span class="text-green-400">$1</span>')
        .replace(/(\/\/.*)/g, '<span class="text-gray-500">$1</span>')
        .replace(/(\b\d+\b)/g, '<span class="text-blue-400">$1</span>');
    }
    
    if (lang === 'python') {
      return codeString
        .replace(/(\b(?:def|class|import|from|if|elif|else|for|while|try|except|finally|with|as|return|yield|async|await|lambda|in|is|not|and|or|True|False|None)\b)/g, '<span class="text-purple-400">$1</span>')
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"|'(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\'])*')/g, '<span class="text-green-400">$1</span>')
        .replace(/(#.*)/g, '<span class="text-gray-500">$1</span>');
    }
    
    if (lang === 'java') {
      return codeString
        .replace(/(\b(?:public|private|protected|class|interface|extends|implements|static|final|void|return|new|if|else|for|while|switch|case|break|continue|throw|throws|try|catch|finally|import|package)\b)/g, '<span class="text-purple-400">$1</span>')
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"|'(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\'])*')/g, '<span class="text-green-400">$1</span>')
        .replace(/(\/\/.*|\/\*[\s\S]*?\*\/)/g, '<span class="text-gray-500">$1</span>')
        .replace(/(@\w+)/g, '<span class="text-blue-400">$1</span>');
    }
    
    if (lang === 'csharp') {
      return codeString
        .replace(/(\b(?:public|private|protected|internal|class|interface|namespace|using|static|void|return|new|if|else|for|while|switch|case|break|continue|throw|try|catch|finally|async|await|var|dynamic|object|string|int|bool|double|decimal)\b)/g, '<span class="text-purple-400">$1</span>')
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"|'(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\'])*')/g, '<span class="text-green-400">$1</span>')
        .replace(/(\/\/.*|\/\*[\s\S]*?\*\/)/g, '<span class="text-gray-500">$1</span>')
        .replace(/(\b\d+\b)/g, '<span class="text-blue-400">$1</span>');
    }
    
    // Default: just return the code with HTML escaping
    return codeString
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  };

  return (
    <pre className="text-xs font-mono whitespace-pre-wrap leading-relaxed" 
      dangerouslySetInnerHTML={{ 
        __html: highlightSyntax(code, language) || '// No code available'
      }} 
    />
  );
};

// Language configurations
const LANGUAGES = [
  { id: 'java', name: 'Java', icon: <Coffee size={14} />, framework: 'Spring Boot', color: '#f89820' },
  { id: 'javascript', name: 'JavaScript', icon: <FileCode size={14} />, framework: 'Node.js/Express', color: '#f0db4f' },
  { id: 'python', name: 'Python', icon: <Code size={14} />, framework: 'FastAPI/Django', color: '#3776ab' },
  { id: 'csharp', name: 'C#', icon: <Box size={14} />, framework: '.NET Core', color: '#9b4993' },
  { id: 'php', name: 'PHP', icon: <Package size={14} />, framework: 'Laravel', color: '#777bb4' },
  { id: 'go', name: 'Go', icon: <Terminal size={14} />, framework: 'Gin', color: '#00add8' },
  { id: 'ruby', name: 'Ruby', icon: <Server size={14} />, framework: 'Ruby on Rails', color: '#cc342d' },
  { id: 'kotlin', name: 'Kotlin', icon: <Cpu size={14} />, framework: 'Ktor/Spring', color: '#7f52ff' },
  { id: 'swift', name: 'Swift', icon: <Monitor size={14} />, framework: 'Vapor', color: '#f05138' },
  { id: 'rust', name: 'Rust', icon: <HardDrive size={14} />, framework: 'Actix-web', color: '#dea584' }
];

// Default implementations for each language
const DEFAULT_IMPLEMENTATIONS = {
  java: {
    controller: '// Java Spring Boot Controller implementation not available',
    service: '// Java Service implementation not available',
    repository: '// Java Repository implementation not available',
    model: '// Java Model implementation not available',
    dependencies: '// Dependencies configuration not available',
    applicationProperties: '// Application properties not available'
  },
  javascript: {
    controller: '// JavaScript Controller implementation not available',
    service: '// JavaScript Service implementation not available',
    model: '// JavaScript Model implementation not available',
    routes: '// Routes configuration not available',
    config: '// Config files not available',
    server: '// Server setup not available'
  },
  python: {
    fastapi: '// Python FastAPI implementation not available',
    schemas: '// Schemas not available',
    models: '// Models not available',
    requirements: '// Requirements not available'
  },
  csharp: {
    controller: '// C# Controller implementation not available',
    service: '// C# Service implementation not available',
    model: '// C# Model implementation not available',
    repository: '// C# Repository implementation not available'
  },
  php: {
    controller: '// PHP Controller implementation not available',
    service: '// PHP Service implementation not available',
    model: '// PHP Model implementation not available'
  },
  go: {
    handler: '// Go Handler implementation not available',
    service: '// Go Service implementation not available',
    model: '// Go Model implementation not available'
  },
  ruby: {
    controller: '// Ruby Controller implementation not available',
    service: '// Ruby Service implementation not available',
    model: '// Ruby Model implementation not available'
  },
  kotlin: {
    controller: '// Kotlin Controller implementation not available',
    service: '// Kotlin Service implementation not available',
    model: '// Kotlin Model implementation not available'
  },
  swift: {
    controller: '// Swift Controller implementation not available',
    service: '// Swift Service implementation not available',
    model: '// Swift Model implementation not available'
  },
  rust: {
    handler: '// Rust Handler implementation not available',
    service: '// Rust Service implementation not available',
    model: '// Rust Model implementation not available'
  }
};

// API Collections with Complete Implementations
const API_COLLECTIONS = [
  {
    id: 'user-management',
    name: 'User Management API',
    description: 'Complete user authentication, authorization, and profile management',
    isExpanded: true,
    isFavorite: true,
    version: 'v2.1',
    owner: 'API Script Team',
    updatedAt: 'Today, 9:30 AM',
    createdAt: 'Jan 15, 2024',
    folders: [
      {
        id: 'authentication',
        name: 'Authentication',
        description: 'User registration, login, and token management',
        isExpanded: true,
        requests: [
          {
            id: 'register-user',
            name: 'Register User',
            method: 'POST',
            url: 'https://api.example.com/v2.1/users/register',
            description: 'Create a new user account with email and password',
            tags: ['auth', 'register', 'signup'],
            lastModified: 'Today, 9:00 AM',
            headers: [
              { key: 'Content-Type', value: 'application/json' },
              { key: 'Accept', value: 'application/json' }
            ],
            body: JSON.stringify({
              email: "user@example.com",
              password: "SecurePass123!",
              firstName: "John",
              lastName: "Doe",
              phoneNumber: "+1234567890"
            }, null, 2),
            implementations: {
              java: {
                controller: `package com.example.api.controller;

import com.example.api.dto.*;
import com.example.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        UserResponse user = userService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success(user, "User registered successfully"));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse auth = userService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success(auth, "Login successful"));
    }
}`,
                service: `package com.example.api.service;

import com.example.api.dto.*;
import com.example.api.model.User;
import com.example.api.model.Role;
import com.example.api.repository.UserRepository;
import com.example.api.repository.RoleRepository;
import com.example.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .build();
        
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new BusinessException("Default role not found"));
        user.setRoles(Set.of(userRole));
        
        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }
    
    public AuthResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new SecurityException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new SecurityException("Invalid credentials");
        }
        
        String token = jwtTokenProvider.generateToken(user.getEmail(), 
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()));
        
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(jwtTokenProvider.getValidityInSeconds())
                .user(mapToResponse(user))
                .build();
    }
    
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.isActive())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}`,
                repository: `package com.example.api.repository;

import com.example.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}`,
                model: `package com.example.api.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "is_active")
    private boolean isActive;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}`,
                dependencies: `<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>`,
                applicationProperties: `# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/userdb
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
jwt.secret=your-256-bit-secret-key
jwt.expiration=86400000`
              },
              javascript: {
                controller: `// controllers/userController.js
const userService = require('../services/userService');
const { validationResult } = require('express-validator');

exports.registerUser = async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ 
        success: false, 
        errors: errors.array() 
      });
    }
    
    const user = await userService.registerUser(req.body);
    res.status(201).json({
      success: true,
      message: 'User registered successfully',
      data: user
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message
    });
  }
};

exports.login = async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ 
        success: false, 
        errors: errors.array() 
      });
    }
    
    const authData = await userService.authenticate(req.body);
    res.json({
      success: true,
      message: 'Login successful',
      data: authData
    });
  } catch (error) {
    res.status(401).json({
      success: false,
      message: error.message
    });
  }
};`,
                service: `// services/userService.js
const User = require('../models/User');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

class UserService {
  async registerUser(userData) {
    const existingUser = await User.findOne({ email: userData.email });
    
    if (existingUser) {
      throw new Error('Email already exists');
    }
    
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(userData.password, salt);
    
    const user = new User({
      ...userData,
      password: hashedPassword,
      isActive: true
    });
    
    await user.save();
    
    const userObject = user.toObject();
    delete userObject.password;
    return userObject;
  }
  
  async authenticate(loginData) {
    const user = await User.findOne({ email: loginData.email })
      .select('+password');
    
    if (!user) {
      throw new Error('Invalid credentials');
    }
    
    const isPasswordValid = await bcrypt.compare(loginData.password, user.password);
    if (!isPasswordValid) {
      throw new Error('Invalid credentials');
    }
    
    const token = jwt.sign(
      {
        userId: user._id,
        email: user.email
      },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN }
    );
    
    const userResponse = user.toObject();
    delete userResponse.password;
    
    return {
      token,
      type: 'Bearer',
      expiresIn: process.env.JWT_EXPIRES_IN,
      user: userResponse
    };
  }
}

module.exports = new UserService();`,
                model: `// models/User.js
const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  email: {
    type: String,
    required: true,
    unique: true,
    trim: true,
    lowercase: true
  },
  password: {
    type: String,
    required: true,
    select: false
  },
  firstName: {
    type: String,
    required: true,
    trim: true
  },
  lastName: {
    type: String,
    required: true,
    trim: true
  },
  phoneNumber: {
    type: String,
    trim: true
  },
  isActive: {
    type: Boolean,
    default: true
  }
}, {
  timestamps: true
});

userSchema.methods.toJSON = function() {
  const obj = this.toObject();
  delete obj.password;
  return obj;
};

module.exports = mongoose.model('User', userSchema);`,
                routes: `// routes/userRoutes.js
const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');
const validationMiddleware = require('../middleware/validationMiddleware');

router.post('/register', 
  validationMiddleware.validateRegister,
  userController.registerUser
);

router.post('/login',
  validationMiddleware.validateLogin,
  userController.login
);

module.exports = router;`,
                config: `// .env
PORT=3000
MONGODB_URI=mongodb://localhost:27017/user_management
JWT_SECRET=your-super-secret-jwt-key
JWT_EXPIRES_IN=24h

// package.json
{
  "dependencies": {
    "express": "^4.18.0",
    "mongoose": "^6.0.0",
    "bcryptjs": "^2.4.3",
    "jsonwebtoken": "^8.5.1",
    "express-validator": "^6.14.0"
  }
}`,
                server: `// server.js
require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const userRoutes = require('./routes/userRoutes');

const app = express();
app.use(express.json());

mongoose.connect(process.env.MONGODB_URI);

app.use('/api/v1/users', userRoutes);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(\`Server running on port \${PORT}\`);
});`
              },
              python: {
                fastapi: `# main.py
from fastapi import FastAPI, HTTPException, status
from fastapi.security import HTTPBearer
from pydantic import BaseModel, EmailStr
from datetime import datetime, timedelta
import jwt
import bcrypt
from typing import Optional

app = FastAPI()
security = HTTPBearer()

class UserCreate(BaseModel):
    email: EmailStr
    password: str
    firstName: str
    lastName: str
    phoneNumber: Optional[str] = None

class LoginRequest(BaseModel):
    email: EmailStr
    password: str

@app.post("/api/v1/users/register")
async def register_user(user_data: UserCreate):
    # Implementation here
    return {"message": "User registered successfully"}

@app.post("/api/v1/users/login")
async def login(login_data: LoginRequest):
    # Implementation here
    return {"token": "jwt_token_here", "type": "Bearer"}`,
                schemas: `# schemas.py
from pydantic import BaseModel, EmailStr
from typing import Optional
from datetime import datetime

class UserResponse(BaseModel):
    id: int
    email: EmailStr
    firstName: str
    lastName: str
    phoneNumber: Optional[str]
    isActive: bool
    createdAt: datetime
    updatedAt: datetime

class AuthResponse(BaseModel):
    token: str
    type: str
    expiresIn: int
    user: UserResponse`,
                models: `# models.py
from sqlalchemy import Column, Integer, String, Boolean, DateTime
from sqlalchemy.ext.declarative import declarative_base
from datetime import datetime

Base = declarative_base()

class User(Base):
    __tablename__ = "users"
    
    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    password_hash = Column(String, nullable=False)
    first_name = Column(String, nullable=False)
    last_name = Column(String, nullable=False)
    phone_number = Column(String, nullable=True)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)`,
                requirements: `# requirements.txt
fastapi==0.104.1
uvicorn==0.24.0
sqlalchemy==2.0.23
python-jose==3.3.0
passlib[bcrypt]==1.7.4
python-multipart==0.0.6`
              }
            }
          },
          {
            id: 'get-user',
            name: 'Get User Profile',
            method: 'GET',
            url: 'https://api.example.com/v2.1/users/{id}',
            description: 'Retrieve user profile information by ID',
            tags: ['users', 'profile', 'read'],
            lastModified: 'Yesterday, 3:45 PM',
            headers: [
              { key: 'Authorization', value: 'Bearer {access-token}' }
            ],
            implementations: {
              java: DEFAULT_IMPLEMENTATIONS.java,
              javascript: DEFAULT_IMPLEMENTATIONS.javascript,
              python: DEFAULT_IMPLEMENTATIONS.python,
              csharp: DEFAULT_IMPLEMENTATIONS.csharp
            }
          }
        ]
      },
      {
        id: 'profile-management',
        name: 'Profile Management',
        description: 'User profile updates and management',
        isExpanded: true,
        requests: [
          {
            id: 'update-profile',
            name: 'Update Profile',
            method: 'PUT',
            url: 'https://api.example.com/v2.1/users/{id}',
            description: 'Update user profile information',
            tags: ['users', 'update', 'profile'],
            lastModified: 'Today, 8:15 AM',
            headers: [
              { key: 'Content-Type', value: 'application/json' },
              { key: 'Authorization', value: 'Bearer {access-token}' }
            ],
            body: JSON.stringify({
              firstName: "John",
              lastName: "Smith",
              phoneNumber: "+1234567890"
            }, null, 2),
            implementations: {
              java: DEFAULT_IMPLEMENTATIONS.java,
              javascript: DEFAULT_IMPLEMENTATIONS.javascript,
              python: DEFAULT_IMPLEMENTATIONS.python,
              csharp: DEFAULT_IMPLEMENTATIONS.csharp
            }
          }
        ]
      }
    ]
  },
  {
    id: 'payment-api',
    name: 'Payment Processing API',
    description: 'Secure payment processing with multiple payment methods',
    isExpanded: false,
    isFavorite: true,
    version: 'v1.5',
    owner: 'API Script Team',
    updatedAt: '1 week ago',
    createdAt: 'Feb 1, 2024',
    folders: [
      {
        id: 'payments',
        name: 'Payments',
        description: 'Process payments and transactions',
        isExpanded: false,
        requests: [
          {
            id: 'create-payment',
            name: 'Create Payment',
            method: 'POST',
            url: 'https://api.example.com/v1.5/payments',
            description: 'Create a new payment transaction',
            tags: ['payments', 'create', 'transactions'],
            lastModified: '2 weeks ago',
            implementations: {
              java: DEFAULT_IMPLEMENTATIONS.java,
              javascript: DEFAULT_IMPLEMENTATIONS.javascript,
              python: DEFAULT_IMPLEMENTATIONS.python,
              csharp: DEFAULT_IMPLEMENTATIONS.csharp
            }
          }
        ]
      }
    ]
  },
  {
    id: 'notification-api',
    name: 'Notification API',
    description: 'Send email, SMS, and push notifications',
    isExpanded: false,
    isFavorite: false,
    version: 'v3.2',
    owner: 'API Script Team',
    updatedAt: '3 days ago',
    createdAt: 'Mar 1, 2024'
  }
];

const ENVIRONMENTS = [
  { id: 'sandbox', name: 'Sandbox', isActive: true, baseUrl: 'https://api.sandbox.example.com' },
  { id: 'staging', name: 'Staging', isActive: false, baseUrl: 'https://api.staging.example.com' },
  { id: 'production', name: 'Production', isActive: false, baseUrl: 'https://api.example.com' }
];

const NOTIFICATIONS = [
  { id: 'notif-1', title: 'New API Implementation', message: 'Complete Java Spring Boot implementation added', time: '10 minutes ago', read: false, type: 'success' },
  { id: 'notif-2', title: 'Code Generation Complete', message: 'Python FastAPI code generated successfully', time: '2 hours ago', read: false, type: 'info' },
  { id: 'notif-3', title: 'Download Ready', message: 'Complete Node.js package ready for download', time: '1 day ago', read: true, type: 'success' }
];

const APIScript = () => {
  const [theme, setTheme] = useState('dark');
  const [activeTab, setActiveTab] = useState('implementations');
  const [showCodePanel, setShowCodePanel] = useState(true);
  const [selectedLanguage, setSelectedLanguage] = useState('java');
  const [showLanguageDropdown, setShowLanguageDropdown] = useState(false);
  const [toast, setToast] = useState(null);
  const [showPublishModal, setShowPublishModal] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState(NOTIFICATIONS);
  const [searchQuery, setSearchQuery] = useState('');
  const [globalSearchQuery, setGlobalSearchQuery] = useState('');
  const [selectedCollection, setSelectedCollection] = useState(API_COLLECTIONS[0]);
  const [selectedRequest, setSelectedRequest] = useState(API_COLLECTIONS[0].folders[0].requests[0]);
  const [environments, setEnvironments] = useState(ENVIRONMENTS);
  const [activeEnvironment, setActiveEnvironment] = useState('sandbox');
  const [publishUrl, setPublishUrl] = useState('');
  const [isGeneratingCode, setIsGeneratingCode] = useState(false);
  const [collections, setCollections] = useState(API_COLLECTIONS);
  const [expandedCollections, setExpandedCollections] = useState(['user-management']);
  const [expandedFolders, setExpandedFolders] = useState(['authentication']);
  const [activeMainTab, setActiveMainTab] = useState('APIs');
  const [showImportModal, setShowImportModal] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [showWorkspaceSwitcher, setShowWorkspaceSwitcher] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showEnvironmentMenu, setShowEnvironmentMenu] = useState(false);
  const [selectedComponent, setSelectedComponent] = useState('controller');
  const [showAllFiles, setShowAllFiles] = useState(false);
  
  const isDark = theme === 'dark';

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
      modalBorder: '#E0E0E0'
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
      modalBorder: '#333333'
    }
  };

  const colors = isDark ? postmanColors.dark : postmanColors.light;

  // Filter collections based on search
  const filteredCollections = collections.filter(collection => {
    if (!searchQuery) return true;
    const query = searchQuery.toLowerCase();
    return (
      collection.name.toLowerCase().includes(query) ||
      collection.description.toLowerCase().includes(query) ||
      collection.folders?.some(folder => 
        folder.name.toLowerCase().includes(query) ||
        folder.description.toLowerCase().includes(query) ||
        folder.requests?.some(request => 
          request.name.toLowerCase().includes(query) ||
          request.description.toLowerCase().includes(query)
        )
      )
    );
  });

  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const getMethodColor = (method) => {
    return colors.method[method] || colors.textSecondary;
  };

  const toggleCollection = (collectionId) => {
    setExpandedCollections(prev =>
      prev.includes(collectionId)
        ? prev.filter(id => id !== collectionId)
        : [...prev, collectionId]
    );
  };

  const toggleFolder = (folderId) => {
    setExpandedFolders(prev =>
      prev.includes(folderId)
        ? prev.filter(id => id !== folderId)
        : [...prev, folderId]
    );
  };

  const handleSelectRequest = (request, collection, folder) => {
    setSelectedRequest(request);
    setSelectedCollection(collection);
    setSelectedComponent('controller'); // Reset to controller when selecting new request
    showToast(`Viewing implementation for ${request.name}`, 'info');
  };

  const handleEnvironmentChange = (envId) => {
    setActiveEnvironment(envId);
    setEnvironments(envs => envs.map(env => ({
      ...env,
      isActive: env.id === envId
    })));
    showToast(`Switched to ${environments.find(e => e.id === envId)?.name} environment`, 'success');
    setShowEnvironmentMenu(false);
  };

  const markAllNotificationsAsRead = () => {
    setNotifications(notifications.map(n => ({ ...n, read: true })));
    showToast('All notifications marked as read', 'success');
  };

  const getActiveBaseUrl = () => {
    return environments.find(e => e.id === activeEnvironment)?.baseUrl || 'https://api.example.com';
  };

  const generateDownloadPackage = () => {
    setIsGeneratingCode(true);
    setTimeout(() => {
      const implementation = getCurrentImplementation();
      if (implementation && Object.keys(implementation).length > 0) {
        // For now, just show a toast since we don't have JSZip installed
        showToast('Package generation started. In a real app, this would create a ZIP file.', 'success');
      } else {
        showToast('No implementation available to download', 'warning');
      }
      setIsGeneratingCode(false);
    }, 1500);
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    showToast('Copied to clipboard!', 'success');
  };

  const getFileName = (component, language) => {
    const extensions = {
      java: '.java',
      javascript: '.js',
      python: '.py',
      csharp: '.cs',
      php: '.php',
      go: '.go',
      ruby: '.rb',
      kotlin: '.kt',
      swift: '.swift',
      rust: '.rs'
    };
    
    const componentMap = {
      controller: 'Controller',
      service: 'Service',
      repository: 'Repository',
      model: 'Model',
      dto: 'DTO',
      routes: 'Routes',
      config: 'Config',
      server: 'Server',
      fastapi: 'Main',
      schemas: 'Schemas',
      models: 'Models',
      crud: 'CRUD',
      auth: 'Auth',
      database: 'Database',
      requirements: 'Requirements',
      dependencies: 'Pom',
      applicationProperties: 'Application'
    };
    
    return `${componentMap[component] || component}${extensions[language] || '.txt'}`;
  };

  const getAvailableComponents = () => {
    const implementation = getCurrentImplementation();
    return implementation ? Object.keys(implementation) : [];
  };

  const getCurrentImplementation = () => {
    return selectedRequest?.implementations?.[selectedLanguage] || DEFAULT_IMPLEMENTATIONS[selectedLanguage];
  };

  const getCurrentCode = () => {
    const implementation = getCurrentImplementation();
    return implementation?.[selectedComponent] || '// Implementation not available for this component';
  };

  const renderCodePanel = () => {
    const currentLanguage = LANGUAGES.find(lang => lang.id === selectedLanguage);
    const availableComponents = getAvailableComponents();
    const currentCode = getCurrentCode();
    
    return (
      <div className="w-80 border-l flex flex-col" style={{ 
        backgroundColor: colors.sidebar,
        borderColor: colors.border
      }}>
        <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Code Implementation</h3>
          <button onClick={() => setShowCodePanel(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors"
            style={{ backgroundColor: colors.hover }}>
            <X size={14} style={{ color: colors.textSecondary }} />
          </button>
        </div>

        <div className="px-4 py-3 border-b" style={{ borderColor: colors.border }}>
          <div className="mb-2">
            <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Language</div>
            <div className="relative">
              <button
                onClick={() => setShowLanguageDropdown(!showLanguageDropdown)}
                className="w-full px-3 py-2 rounded text-sm font-medium flex items-center justify-between hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover, color: colors.text }}
              >
                <div className="flex items-center gap-2">
                  {currentLanguage?.icon}
                  <span>{currentLanguage?.name}</span>
                  <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                    backgroundColor: currentLanguage?.color + '20',
                    color: currentLanguage?.color
                  }}>
                    {currentLanguage?.framework}
                  </span>
                </div>
                <ChevronDown size={14} style={{ color: colors.textSecondary }} />
              </button>

              {showLanguageDropdown && (
                <div className="absolute left-0 right-0 top-full mt-1 py-2 rounded shadow-lg z-50 border"
                  style={{ 
                    backgroundColor: colors.dropdownBg,
                    borderColor: colors.border
                  }}>
                  {LANGUAGES.map(lang => (
                    <button
                      key={lang.id}
                      onClick={() => {
                        setSelectedLanguage(lang.id);
                        setShowLanguageDropdown(false);
                        setSelectedComponent('controller');
                      }}
                      className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors"
                      style={{ 
                        backgroundColor: selectedLanguage === lang.id ? colors.selected : 'transparent',
                        color: selectedLanguage === lang.id ? colors.primary : colors.text
                      }}
                    >
                      {lang.icon}
                      {lang.name}
                      <span className="text-xs ml-auto px-1.5 py-0.5 rounded" style={{ 
                        backgroundColor: lang.color + '20',
                        color: lang.color
                      }}>
                        {lang.framework}
                      </span>
                      {selectedLanguage === lang.id && <Check size={14} className="ml-2" />}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

          {availableComponents.length > 0 && (
            <div>
              <div className="text-xs mb-1" style={{ color: colors.textSecondary }}>Components</div>
              <div className="flex flex-wrap gap-1">
                {availableComponents.map(component => (
                  <button
                    key={component}
                    onClick={() => setSelectedComponent(component)}
                    className={`px-2 py-1 text-xs rounded capitalize ${
                      selectedComponent === component ? '' : 'hover:bg-opacity-50'
                    }`}
                    style={{ 
                      backgroundColor: selectedComponent === component ? colors.primary : colors.hover,
                      color: selectedComponent === component ? 'white' : colors.text
                    }}
                  >
                    {component}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="flex-1 overflow-auto">
          <div className="p-4 border-b flex items-center justify-between" style={{ borderColor: colors.border }}>
            <div className="flex items-center gap-2">
              <FileCode size={12} style={{ color: colors.textSecondary }} />
              <span className="text-sm font-medium capitalize" style={{ color: colors.text }}>
                {selectedComponent.replace(/([A-Z])/g, ' $1').trim()}
              </span>
              <span className="text-xs px-1.5 py-0.5 rounded" style={{ backgroundColor: colors.hover }}>
                {getFileName(selectedComponent, selectedLanguage)}
              </span>
            </div>
            <button 
              onClick={() => copyToClipboard(currentCode)}
              className="text-xs px-2 py-1 rounded hover:bg-opacity-50 transition-colors flex items-center gap-1"
              style={{ backgroundColor: colors.hover, color: colors.text }}
            >
              <Copy size={10} />
              Copy
            </button>
          </div>
          
          <div className="p-4">
            <SyntaxHighlighter 
              language={selectedLanguage}
              code={currentCode}
            />
          </div>
        </div>

        <div className="p-4 border-t space-y-2" style={{ borderColor: colors.border }}>
          <button 
            className="w-full py-2 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center justify-center gap-2"
            onClick={() => copyToClipboard(currentCode)}
            style={{ backgroundColor: colors.primary, color: 'white' }}>
            <Copy size={12} />
            Copy Code
          </button>
          <button 
            className="w-full py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center justify-center gap-2"
            onClick={generateDownloadPackage}
            style={{ backgroundColor: colors.hover, color: colors.text }}>
            {isGeneratingCode ? (
              <>
                <RefreshCw size={12} className="animate-spin" />
                Generating...
              </>
            ) : (
              <>
                <Download size={12} />
                Download Package
              </>
            )}
          </button>
          {showAllFiles && (
            <button 
              className="w-full py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center justify-center gap-2"
              onClick={() => setShowAllFiles(!showAllFiles)}
              style={{ backgroundColor: colors.hover, color: colors.text }}>
              {showAllFiles ? 'Show Single File' : 'Show All Files'}
            </button>
          )}
        </div>
      </div>
    );
  };

  const renderImportModal = () => {
    if (!showImportModal) return null;
    
    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="rounded-lg w-full max-w-lg" style={{ 
          backgroundColor: colors.modalBg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Import API Specification</h3>
            <button onClick={() => setShowImportModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.hover }}>
              <X size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          <div className="p-4 space-y-4">
            <div className="text-center p-6 border-2 border-dashed rounded-lg" style={{ 
              borderColor: colors.border,
              backgroundColor: colors.hover
            }}>
              <UploadCloud size={32} className="mx-auto mb-4" style={{ color: colors.textSecondary }} />
              <p className="text-sm mb-4" style={{ color: colors.text }}>Import OpenAPI/Swagger Spec</p>
              <p className="text-xs mb-4" style={{ color: colors.textSecondary }}>Supports: OpenAPI 3.0, Swagger 2.0, Postman Collection</p>
              <button className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors"
                style={{ backgroundColor: colors.primary, color: 'white' }}
                onClick={() => showToast('File browser would open', 'info')}>
                Select File
              </button>
            </div>
            <div className="grid grid-cols-2 gap-3">
              {['From URL', 'From GitHub', 'From Postman', 'Example APIs'].map(source => (
                <button key={source} className="p-4 rounded text-sm text-left hover:bg-opacity-50 transition-colors"
                  onClick={() => showToast(`Importing ${source}`, 'info')}
                  style={{ 
                    backgroundColor: colors.hover,
                    border: `1px solid ${colors.border}`,
                    color: colors.text
                  }}>
                  <div className="font-medium">{source}</div>
                  <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
                    {source === 'From URL' ? 'Import from URL' :
                     source === 'From GitHub' ? 'Connect to GitHub' :
                     source === 'From Postman' ? 'Postman export' : 'Sample implementations'}
                  </div>
                </button>
              ))}
            </div>
          </div>
          <div className="p-4 border-t" style={{ borderColor: colors.border }}>
            <div className="flex justify-end gap-2">
              <button onClick={() => setShowImportModal(false)} className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover, color: colors.text }}>
                Cancel
              </button>
              <button onClick={() => {
                showToast('API specification imported!', 'success');
                setShowImportModal(false);
              }} className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors"
                style={{ backgroundColor: colors.primary, color: 'white' }}>
                Import & Generate
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderSettingsModal = () => {
    if (!showSettingsModal) return null;
    
    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="rounded-lg w-full max-w-2xl" style={{ 
          backgroundColor: colors.modalBg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Settings</h3>
            <button onClick={() => setShowSettingsModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.hover }}>
              <X size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          <div className="p-4">
            <div className="grid grid-cols-3 gap-4 mb-6">
              {['Code Style', 'Templates', 'Export', 'Security', 'Notifications', 'Preferences'].map(setting => (
                <button key={setting} className="p-4 rounded text-center hover:bg-opacity-50 transition-colors"
                  onClick={() => showToast(`Opening ${setting} settings`, 'info')}
                  style={{ 
                    backgroundColor: colors.hover,
                    border: `1px solid ${colors.border}`,
                    color: colors.text
                  }}>
                  <div className="font-medium">{setting}</div>
                </button>
              ))}
            </div>
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="font-medium" style={{ color: colors.text }}>Dark Mode</div>
                  <div className="text-sm" style={{ color: colors.textSecondary }}>Toggle dark/light theme</div>
                </div>
                <button onClick={() => setTheme(isDark ? 'light' : 'dark')} className="relative inline-flex h-6 w-11 items-center rounded-full"
                  style={{ backgroundColor: isDark ? colors.primary : colors.border }}>
                  <span className={`inline-block h-4 w-4 transform rounded-full bg-white transition ${
                    isDark ? 'translate-x-6' : 'translate-x-1'
                  }`} />
                </button>
              </div>
              <div className="flex items-center justify-between">
                <div>
                  <div className="font-medium" style={{ color: colors.text }}>Auto-generate Tests</div>
                  <div className="text-sm" style={{ color: colors.textSecondary }}>Generate unit tests with code</div>
                </div>
                <button className="relative inline-flex h-6 w-11 items-center rounded-full"
                  style={{ backgroundColor: colors.primary }}
                  onClick={() => showToast('Auto-test generation toggled', 'info')}>
                  <span className="inline-block h-4 w-4 transform rounded-full bg-white translate-x-6" />
                </button>
              </div>
            </div>
          </div>
          <div className="p-4 border-t" style={{ borderColor: colors.border }}>
            <div className="flex justify-end gap-2">
              <button onClick={() => setShowSettingsModal(false)} className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover, color: colors.text }}>
                Cancel
              </button>
              <button onClick={() => {
                showToast('Settings saved!', 'success');
                setShowSettingsModal(false);
              }} className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors"
                style={{ backgroundColor: colors.primary, color: 'white' }}>
                Save Changes
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderPublishModal = () => {
    if (!showPublishModal) return null;
    
    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="rounded-lg w-full max-w-lg" style={{ 
          backgroundColor: colors.modalBg,
          border: `1px solid ${colors.modalBorder}`
        }}>
          <div className="flex items-center justify-between p-4 border-b" style={{ borderColor: colors.border }}>
            <h3 className="text-sm font-semibold" style={{ color: colors.text }}>Export & Share</h3>
            <button onClick={() => setShowPublishModal(false)} className="p-1 rounded hover:bg-opacity-50 transition-colors"
              style={{ backgroundColor: colors.hover }}>
              <X size={14} style={{ color: colors.textSecondary }} />
            </button>
          </div>
          <div className="p-4 space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Export Format</label>
              <div className="grid grid-cols-2 gap-2">
                {['Complete Package', 'Single File', 'GitHub Gist', 'Docker Package'].map(format => (
                  <button key={format} className="p-3 rounded text-sm text-left transition-colors hover:bg-opacity-50"
                    style={{ 
                      backgroundColor: colors.hover,
                      border: `1px solid ${colors.border}`,
                      color: colors.text
                    }}
                    onClick={() => showToast(`Exporting as ${format}`, 'info')}>
                    <div className="font-medium">{format}</div>
                    <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>
                      {format === 'Complete Package' ? 'All files + config' :
                       format === 'Single File' ? 'Selected file only' :
                       format === 'GitHub Gist' ? 'Share on GitHub' : 'With Dockerfile'}
                    </div>
                  </button>
                ))}
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Target Language</label>
              <select 
                className="w-full px-3 py-2 border rounded text-sm focus:outline-none"
                value={selectedLanguage}
                onChange={(e) => setSelectedLanguage(e.target.value)}
                style={{
                  backgroundColor: colors.inputBg,
                  borderColor: colors.border,
                  color: colors.text
                }}
              >
                {LANGUAGES.map(lang => (
                  <option key={lang.id} value={lang.id}>{lang.name} ({lang.framework})</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: colors.text }}>Include Documentation</label>
              <div className="flex items-center gap-2">
                <button className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors"
                  style={{ backgroundColor: colors.primary, color: 'white' }}
                  onClick={() => showToast('Include README selected', 'info')}>
                  README.md
                </button>
                <button className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
                  style={{ backgroundColor: colors.hover, color: colors.text }}
                  onClick={() => showToast('Include API docs selected', 'info')}>
                  API Docs
                </button>
                <button className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
                  style={{ backgroundColor: colors.hover, color: colors.text }}
                  onClick={() => showToast('Include tests selected', 'info')}>
                  Tests
                </button>
              </div>
            </div>
          </div>
          <div className="p-4 border-t" style={{ borderColor: colors.border }}>
            <div className="flex justify-end gap-2">
              <button onClick={() => setShowPublishModal(false)} className="px-4 py-2 rounded text-sm font-medium hover:bg-opacity-50 transition-colors"
                style={{ backgroundColor: colors.hover, color: colors.text }}>
                Cancel
              </button>
              <button onClick={generateDownloadPackage} className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors"
                style={{ backgroundColor: colors.primary, color: 'white' }}>
                {isGeneratingCode ? (
                  <>
                    <RefreshCw size={12} className="animate-spin inline mr-2" />
                    Exporting...
                  </>
                ) : (
                  'Export Now'
                )}
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderToast = () => {
    if (!toast) return null;
    
    const bgColor = toast.type === 'error' ? colors.error : 
                   toast.type === 'success' ? colors.success : 
                   toast.type === 'warning' ? colors.warning : 
                   colors.info;
    
    return (
      <div className="fixed bottom-4 right-4 px-4 py-2 rounded text-sm font-medium z-50 animate-fade-in-up"
        style={{ 
          backgroundColor: bgColor,
          color: 'white'
        }}>
        {toast.message}
      </div>
    );
  };

  const renderAllFilesView = () => {
    const implementation = getCurrentImplementation();
    if (!implementation || Object.keys(implementation).length === 0) {
      return (
        <div className="text-center p-12" style={{ color: colors.textSecondary }}>
          <FileCode size={48} className="mx-auto mb-4 opacity-50" />
          <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>No Files Available</h3>
          <p>No implementation files available for {selectedLanguage}</p>
        </div>
      );
    }
    
    return (
      <div className="space-y-6">
        {Object.entries(implementation).map(([component, code]) => (
          <div key={component} className="border rounded-lg overflow-hidden" style={{ borderColor: colors.border }}>
            <div className="px-4 py-3 flex items-center justify-between" style={{ 
              backgroundColor: colors.card,
              borderBottomColor: colors.border
            }}>
              <div className="flex items-center gap-2">
                <FileCode size={14} />
                <span className="text-sm font-medium capitalize" style={{ color: colors.text }}>
                  {component.replace(/([A-Z])/g, ' $1').trim()}
                </span>
                <span className="text-xs px-2 py-0.5 rounded" style={{ backgroundColor: colors.hover }}>
                  {getFileName(component, selectedLanguage)}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => copyToClipboard(code)}
                  className="p-1.5 rounded hover:bg-opacity-50 transition-colors"
                  style={{ backgroundColor: colors.hover }}
                  title="Copy to clipboard"
                >
                  <Copy size={12} style={{ color: colors.textSecondary }} />
                </button>
                <button
                  onClick={() => {
                    const blob = new Blob([code], { type: 'text/plain' });
                    const url = URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = getFileName(component, selectedLanguage);
                    document.body.appendChild(a);
                    a.click();
                    document.body.removeChild(a);
                    URL.revokeObjectURL(url);
                  }}
                  className="p-1.5 rounded hover:bg-opacity-50 transition-colors"
                  style={{ backgroundColor: colors.hover }}
                  title="Download file"
                >
                  <Download size={12} style={{ color: colors.textSecondary }} />
                </button>
              </div>
            </div>
            <div className="p-4">
              <SyntaxHighlighter language={selectedLanguage} code={code} />
            </div>
          </div>
        ))}
      </div>
    );
  };

  const renderImplementationContent = () => {
    const availableComponents = getAvailableComponents();
    const currentLanguage = LANGUAGES.find(lang => lang.id === selectedLanguage);
    const implementation = getCurrentImplementation();
    const hasImplementation = implementation && Object.keys(implementation).length > 0;
    
    return (
      <div className="flex-1 overflow-auto p-8">
        <div className="max-w-6xl mx-auto">
          {/* Header */}
          <div className="mb-8">
            <div className="flex items-center gap-3 mb-2">
              <div className="px-3 py-1 rounded text-sm font-medium" style={{ 
                backgroundColor: getMethodColor(selectedRequest.method),
                color: 'white'
              }}>
                {selectedRequest.method}
              </div>
              <code className="text-lg font-mono" style={{ color: colors.text }}>
                {selectedRequest.url}
              </code>
            </div>
            <h1 className="text-2xl font-semibold mb-4" style={{ color: colors.text }}>
              {selectedRequest.name}
            </h1>
            <p className="text-base mb-6" style={{ color: colors.textSecondary }}>
              {selectedRequest.description}
            </p>
            
            <div className="flex flex-wrap items-center gap-4 text-sm mb-6">
              <div style={{ color: colors.textTertiary }}>
                <Folder size={12} className="inline mr-1" />
                {selectedCollection.name} › {selectedCollection.folders.find(f => f.requests?.some(r => r.id === selectedRequest.id))?.name}
              </div>
              <div className="flex items-center gap-2">
                {selectedRequest.tags?.map(tag => (
                  <span key={tag} className="text-xs px-2 py-1 rounded" style={{ 
                    backgroundColor: colors.hover,
                    color: colors.textSecondary
                  }}>
                    {tag}
                  </span>
                ))}
              </div>
              <div style={{ color: colors.textTertiary }}>
                <Clock size={12} className="inline mr-1" />
                Last updated: {selectedRequest.lastModified}
              </div>
            </div>
          </div>

          {/* Language & Framework Selection */}
          <div className="mb-8 p-4 rounded border" style={{ 
            backgroundColor: colors.card,
            borderColor: colors.border
          }}>
            <h2 className="text-lg font-semibold mb-4" style={{ color: colors.text }}>Select Implementation Language</h2>
            <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
              {LANGUAGES.map(lang => (
                <button
                  key={lang.id}
                  onClick={() => {
                    setSelectedLanguage(lang.id);
                    setSelectedComponent('controller');
                  }}
                  className={`p-4 rounded-lg text-sm text-center hover-lift transition-all ${
                    selectedLanguage === lang.id ? 'ring-2 ring-offset-1' : ''
                  }`}
                  style={{ 
                    backgroundColor: selectedLanguage === lang.id ? colors.selected : colors.hover,
                    border: `1px solid ${selectedLanguage === lang.id ? colors.primary : colors.border}`,
                    color: colors.text,
                    boxShadow: selectedLanguage === lang.id ? `0 0 0 2px ${colors.primary}40` : 'none'
                  }}
                >
                  <div className="flex flex-col items-center">
                    {lang.icon}
                    <span className="mt-2 font-medium">{lang.name}</span>
                    <span className="text-xs mt-1" style={{ color: colors.textSecondary }}>{lang.framework}</span>
                    {selectedLanguage === lang.id && (
                      <Check size={16} className="mt-2" style={{ color: colors.primary }} />
                    )}
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Implementation Content */}
          {hasImplementation ? (
            <>
              {showAllFiles ? (
                renderAllFilesView()
              ) : (
                <div className="border rounded-lg overflow-hidden" style={{ borderColor: colors.border }}>
                  <div className="px-4 py-3 flex items-center justify-between" style={{ 
                    backgroundColor: colors.card,
                    borderBottomColor: colors.border
                  }}>
                    <div className="flex items-center gap-3">
                      <div className="flex items-center gap-2">
                        {currentLanguage?.icon}
                        <span className="font-medium" style={{ color: colors.text }}>
                          {currentLanguage?.name} Implementation
                        </span>
                        <span className="text-xs px-2 py-0.5 rounded" style={{ 
                          backgroundColor: currentLanguage?.color + '20',
                          color: currentLanguage?.color
                        }}>
                          {currentLanguage?.framework}
                        </span>
                      </div>
                      {availableComponents.length > 0 && (
                        <div className="flex gap-1">
                          {availableComponents.map(component => (
                            <button
                              key={component}
                              onClick={() => setSelectedComponent(component)}
                              className={`px-3 py-1 text-xs rounded capitalize ${
                                selectedComponent === component ? '' : 'hover:bg-opacity-50'
                              }`}
                              style={{ 
                                backgroundColor: selectedComponent === component ? colors.primary : colors.hover,
                                color: selectedComponent === component ? 'white' : colors.text
                              }}
                            >
                              {component}
                            </button>
                          ))}
                        </div>
                      )}
                    </div>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => copyToClipboard(getCurrentCode())}
                        className="px-3 py-1.5 rounded text-sm font-medium hover:bg-opacity-50 transition-colors flex items-center gap-2"
                        style={{ backgroundColor: colors.hover, color: colors.text }}
                      >
                        <Copy size={12} />
                        Copy
                      </button>
                      <button
                        onClick={() => setShowAllFiles(!showAllFiles)}
                        className="px-3 py-1.5 rounded text-sm font-medium hover:opacity-90 transition-colors flex items-center gap-2"
                        style={{ backgroundColor: colors.primary, color: 'white' }}
                      >
                        {showAllFiles ? 'Single File' : 'All Files'}
                      </button>
                    </div>
                  </div>
                  <div className="p-4">
                    <SyntaxHighlighter 
                      language={selectedLanguage}
                      code={getCurrentCode()}
                    />
                  </div>
                </div>
              )}

              {/* Features & Requirements */}
              <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="border rounded-lg p-4" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
                  <div className="flex items-center gap-2 mb-3">
                    <ShieldCheck size={20} style={{ color: colors.success }} />
                    <h3 className="font-semibold" style={{ color: colors.text }}>Security Features</h3>
                  </div>
                  <ul className="space-y-2 text-sm" style={{ color: colors.textSecondary }}>
                    <li className="flex items-center gap-2">
                      <Check size={14} style={{ color: colors.success }} />
                      JWT Authentication
                    </li>
                    <li className="flex items-center gap-2">
                      <Check size={14} style={{ color: colors.success }} />
                      Password Hashing
                    </li>
                    <li className="flex items-center gap-2">
                      <Check size={14} style={{ color: colors.success }} />
                      Input Validation
                    </li>
                  </ul>
                </div>
                
                <div className="border rounded-lg p-4" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
                  <div className="flex items-center gap-2 mb-3">
                    <Layers size={20} style={{ color: colors.info }} />
                    <h3 className="font-semibold" style={{ color: colors.text }}>Architecture</h3>
                  </div>
                  <ul className="space-y-2 text-sm" style={{ color: colors.textSecondary }}>
                    <li className="flex items-center gap-2">
                      <Check size={14} style={{ color: colors.success }} />
                      Clean Architecture
                    </li>
                    <li className="flex items-center gap-2">
                      <Check size={14} style={{ color: colors.success }} />
                      Dependency Injection
                    </li>
                    <li className="flex items-center gap-2">
                      <Check size={14} style={{ color: colors.success }} />
                      Repository Pattern
                    </li>
                  </ul>
                </div>
                
                <div className="border rounded-lg p-4" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
                  <div className="flex items-center gap-2 mb-3">
                    <Zap size={20} style={{ color: colors.warning }} />
                    <h3 className="font-semibold" style={{ color: colors.text }}>Ready to Use</h3>
                  </div>
                  <ul className="space-y-2 text-sm" style={{ color: colors.textSecondary }}>
                    <li className="flex items-center gap-2">
                      <Check size={14} style={{ color: colors.success }} />
                      Production Code
                    </li>
                    <li className="flex items-center gap-2">
                      <Check size={14} style={{ color: colors.success }} />
                      Database Setup
                    </li>
                    <li className="flex items-center gap-2">
                      <Check size={14} style={{ color: colors.success }} />
                      Complete Documentation
                    </li>
                  </ul>
                </div>
              </div>

              {/* Quick Start Guide */}
              <div className="mt-8 border rounded-lg p-6" style={{ backgroundColor: colors.card, borderColor: colors.border }}>
                <h3 className="text-lg font-semibold mb-4" style={{ color: colors.text }}>Quick Start Guide</h3>
                <div className="space-y-3 text-sm" style={{ color: colors.textSecondary }}>
                  <div className="flex items-start gap-3">
                    <div className="flex-shrink-0 w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold" style={{ backgroundColor: colors.primary, color: 'white' }}>
                      1
                    </div>
                    <div>
                      <span className="font-medium" style={{ color: colors.text }}>Copy the code</span>
                      <p>Select the component you need and copy the code</p>
                    </div>
                  </div>
                  
                  <div className="flex items-start gap-3">
                    <div className="flex-shrink-0 w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold" style={{ backgroundColor: colors.primary, color: 'white' }}>
                      2
                    </div>
                    <div>
                      <span className="font-medium" style={{ color: colors.text }}>Install dependencies</span>
                      <code className="block mt-1 p-2 rounded font-mono" style={{ backgroundColor: colors.codeBg }}>
                        {selectedLanguage === 'java' && 'mvn install'}
                        {selectedLanguage === 'javascript' && 'npm install'}
                        {selectedLanguage === 'python' && 'pip install -r requirements.txt'}
                        {selectedLanguage === 'csharp' && 'dotnet restore'}
                        {selectedLanguage === 'php' && 'composer install'}
                        {selectedLanguage === 'go' && 'go mod tidy'}
                      </code>
                    </div>
                  </div>
                  
                  <div className="flex items-start gap-3">
                    <div className="flex-shrink-0 w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold" style={{ backgroundColor: colors.primary, color: 'white' }}>
                      3
                    </div>
                    <div>
                      <span className="font-medium" style={{ color: colors.text }}>Configure & Run</span>
                      <code className="block mt-1 p-2 rounded font-mono" style={{ backgroundColor: colors.codeBg }}>
                        {selectedLanguage === 'java' && 'mvn spring-boot:run'}
                        {selectedLanguage === 'javascript' && 'npm start'}
                        {selectedLanguage === 'python' && 'uvicorn main:app --reload'}
                        {selectedLanguage === 'csharp' && 'dotnet run'}
                      </code>
                    </div>
                  </div>
                </div>
              </div>
            </>
          ) : (
            <div className="text-center p-12" style={{ color: colors.textSecondary }}>
              <Code size={48} className="mx-auto mb-4 opacity-50" />
              <h3 className="text-lg font-semibold mb-2" style={{ color: colors.text }}>Implementation Not Available</h3>
              <p className="mb-6">Complete implementation for {selectedLanguage} is not yet available for this endpoint.</p>
              <button className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors"
                onClick={() => showToast('Requesting implementation generation', 'info')}
                style={{ backgroundColor: colors.primary, color: 'white' }}>
                Generate Implementation
              </button>
            </div>
          )}
        </div>
      </div>
    );
  };

  const renderMainContent = () => {
    switch (activeTab) {
      case 'documentation':
        return (
          <div className="flex-1 flex items-center justify-center p-8">
            <div className="text-center max-w-lg" style={{ color: colors.textSecondary }}>
              <BookOpen size={48} className="mx-auto mb-4 opacity-50" />
              <h2 className="text-xl font-semibold mb-3" style={{ color: colors.text }}>API Documentation</h2>
              <p className="mb-6">View comprehensive documentation for each API endpoint.</p>
              <button className="px-4 py-2 rounded text-sm font-medium hover:opacity-90 transition-colors"
                onClick={() => {
                  setActiveTab('implementations');
                  showToast('Switching to implementations', 'info');
                }}
                style={{ backgroundColor: colors.primary, color: 'white' }}>
                View Implementations
              </button>
            </div>
          </div>
        );
        
      case 'generate':
        return (
          <div className="flex-1 p-8">
            <div className="max-w-4xl mx-auto">
              <h2 className="text-2xl font-semibold mb-6" style={{ color: colors.text }}>Generate Code</h2>
              <p className="mb-6" style={{ color: colors.textSecondary }}>Generate complete implementations for your APIs in any language.</p>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
                {[
                  { title: 'From OpenAPI Spec', desc: 'Import OpenAPI/Swagger specification', icon: <FileText size={24} /> },
                  { title: 'From Postman', desc: 'Import Postman collection', icon: <Package size={24} /> },
                  { title: 'From cURL', desc: 'Convert cURL command to code', icon: <Terminal size={24} /> },
                  { title: 'Custom Template', desc: 'Use custom code templates', icon: <FileCode size={24} /> }
                ].map(item => (
                  <button key={item.title} className="border rounded p-6 text-left hover:border-opacity-50 transition-colors hover-lift"
                    onClick={() => setShowImportModal(true)}
                    style={{ 
                      borderColor: colors.border,
                      backgroundColor: colors.card
                    }}>
                    <div className="flex items-center gap-3 mb-3">
                      <div className="p-2 rounded" style={{ backgroundColor: colors.primary + '20' }}>
                        {item.icon}
                      </div>
                      <h3 className="font-semibold" style={{ color: colors.text }}>{item.title}</h3>
                    </div>
                    <p className="text-sm" style={{ color: colors.textSecondary }}>{item.desc}</p>
                  </button>
                ))}
              </div>
            </div>
          </div>
        );
        
      case 'implementations':
      default:
        return (
          <div className="flex-1 flex flex-col overflow-hidden">
            {/* Implementation Tabs */}
            <div className="flex items-center border-b h-9" style={{ 
              backgroundColor: colors.card,
              borderColor: colors.border
            }}>
              <div className="flex items-center px-2">
                <button
                  onClick={() => setActiveTab('implementations')}
                  className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${
                    activeTab === 'implementations' ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === 'implementations' ? colors.primary : 'transparent',
                    color: activeTab === 'implementations' ? colors.primary : colors.textSecondary,
                    backgroundColor: 'transparent'
                  }}>
                  Implementations
                </button>
                
                {/* <button
                  onClick={() => setActiveTab('documentation')}
                  className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${
                    activeTab === 'documentation' ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === 'documentation' ? colors.primary : 'transparent',
                    color: activeTab === 'documentation' ? colors.primary : colors.textSecondary,
                    backgroundColor: 'transparent'
                  }}>
                  Documentation
                </button> */}
                
                <button
                  onClick={() => setActiveTab('generate')}
                  className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors hover-lift ${
                    activeTab === 'generate' ? '' : 'hover:bg-opacity-50'
                  }`}
                  style={{ 
                    borderBottomColor: activeTab === 'generate' ? colors.primary : 'transparent',
                    color: activeTab === 'generate' ? colors.primary : colors.textSecondary,
                    backgroundColor: 'transparent'
                  }}>
                  Generate Code
                </button>
              </div>
            </div>

            {/* Implementation Content */}
            {renderImplementationContent()}
          </div>
        );
    }
  };

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

      {/* TOP NAVIGATION */}
      <div className="flex items-center justify-between h-10 px-4 border-b" style={{ 
        backgroundColor: colors.header,
        borderColor: colors.border
      }}>
        <div className="flex items-center gap-4">
          {/* <div className="flex items-center gap-2">
            <div className="w-3 h-3 rounded" style={{ backgroundColor: colors.primary }}></div>
            <span className="text-sm font-semibold" style={{ color: colors.text }}>APIScript</span>
            <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
              backgroundColor: colors.primary + '20',
              color: colors.primary
            }}>
              BETA
            </span>
          </div> */}

          <div className="flex items-center gap-1 -ml-3 text-nowrap">
            {/* {['APIs', 'Implementations', 'Templates', 'Generate', 'Docs'].map(tab => ( */}
              {/* {['Implementations', 'Templates', 'Generate'].map(tab => (
              <button key={tab} className={`px-3 py-1.5 text-sm font-medium rounded transition-colors hover-lift ${
                tab === activeMainTab ? '' : 'hover:bg-opacity-50'
              }`}
                onClick={() => {
                  setActiveMainTab(tab);
                  if (tab === 'Implementations') setActiveTab('implementations');
                  if (tab === 'Generate') setActiveTab('generate');
                  if (tab === 'Docs') setActiveTab('documentation');
                  showToast(`Switching to ${tab}`, 'info');
                }}
                style={{ 
                  backgroundColor: tab === activeMainTab ? colors.selected : 'transparent',
                  color: tab === activeMainTab ? colors.primary : colors.textSecondary
                }}>
                {tab}
              </button>
            ))} */}
            <span className={`px-3 py-1.5 text-sm font-medium rounded transition-colors hover-lift`}>API Code Base</span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Environment Selector */}
          <div className="relative">
            <button className="flex items-center gap-2 px-3 py-1.5 rounded text-sm hover:bg-opacity-50 transition-colors hover-lift"
              onClick={() => setShowEnvironmentMenu(!showEnvironmentMenu)}
              style={{ backgroundColor: colors.hover }}>
              <Globe size={12} style={{ color: colors.textSecondary }} />
              <span style={{ color: colors.text }}>{environments.find(e => e.isActive)?.name}</span>
              <ChevronDown size={12} style={{ color: colors.textSecondary }} />
            </button>

            {showEnvironmentMenu && (
              <div className="absolute top-full right-0 mt-1 py-2 rounded shadow-lg z-50 border min-w-48"
                style={{ 
                  backgroundColor: colors.dropdownBg,
                  borderColor: colors.border
                }}>
                {environments.map(env => (
                  <button
                    key={env.id}
                    onClick={() => handleEnvironmentChange(env.id)}
                    className="w-full px-3 py-2 text-sm flex items-center gap-2 hover:bg-opacity-50 transition-colors"
                    style={{ 
                      backgroundColor: env.isActive ? colors.selected : 'transparent',
                      color: env.isActive ? colors.primary : colors.text
                    }}
                  >
                    <div className="w-2 h-2 rounded-full" style={{ 
                      backgroundColor: env.isActive ? colors.success : colors.textSecondary 
                    }}></div>
                    {env.name}
                    {env.isActive && <Check size={14} className="ml-auto" />}
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className="w-px h-4" style={{ backgroundColor: colors.border }}></div>

          {/* Notifications */}
          <div className="relative">
            <button onClick={() => {
              setShowNotifications(!showNotifications);
              markAllNotificationsAsRead();
            }} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift relative"
              style={{ backgroundColor: colors.hover }}>
              <Bell size={14} style={{ color: colors.textSecondary }} />
              {notifications.filter(n => !n.read).length > 0 && (
                <span className="absolute -top-1 -right-1 w-2 h-2 rounded-full" style={{ backgroundColor: colors.error }}></span>
              )}
            </button>

            {showNotifications && (
              <div className="absolute top-full right-0 mt-1 py-2 rounded shadow-lg z-50 border w-80"
                style={{ 
                  backgroundColor: colors.dropdownBg,
                  borderColor: colors.border
                }}>
                <div className="px-4 py-2 border-b flex items-center justify-between" style={{ borderColor: colors.border }}>
                  <span className="text-sm font-medium" style={{ color: colors.text }}>Notifications</span>
                  <button onClick={markAllNotificationsAsRead} className="text-xs hover:underline" style={{ color: colors.primary }}>
                    Mark all as read
                  </button>
                </div>
                <div className="max-h-96 overflow-auto">
                  {notifications.map(notification => (
                    <div key={notification.id} className={`px-4 py-3 border-b hover:bg-opacity-50 transition-colors cursor-pointer ${
                      notification.read ? '' : 'bg-opacity-20'
                    }`}
                      style={{ 
                        borderColor: colors.border,
                        backgroundColor: notification.read ? 'transparent' : colors.selected
                      }}
                      onClick={() => showToast(`Opening notification: ${notification.title}`, 'info')}>
                      <div className="flex items-start gap-3">
                        <div className="mt-0.5">
                          {notification.type === 'warning' ? (
                            <AlertCircle size={12} style={{ color: colors.warning }} />
                          ) : notification.type === 'success' ? (
                            <CheckCircle size={12} style={{ color: colors.success }} />
                          ) : (
                            <Info size={12} style={{ color: colors.info }} />
                          )}
                        </div>
                        <div className="flex-1">
                          <div className="text-sm font-medium" style={{ color: colors.text }}>{notification.title}</div>
                          <div className="text-xs mt-1" style={{ color: colors.textSecondary }}>{notification.message}</div>
                          <div className="text-xs mt-1" style={{ color: colors.textTertiary }}>{notification.time}</div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Global Search */}
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
            <input 
              type="text" 
              placeholder="Search APIs, implementations..."
              value={globalSearchQuery}
              onChange={(e) => setGlobalSearchQuery(e.target.value)}
              className="pl-8 pr-3 py-1.5 rounded text-sm focus:outline-none w-64 hover-lift"
              style={{ 
                backgroundColor: colors.inputBg, 
                border: `1px solid ${colors.border}`, 
                color: colors.text 
              }} 
            />
            {globalSearchQuery && (
              <div className="absolute right-2 top-1/2 transform -translate-y-1/2">
                <button onClick={() => setGlobalSearchQuery('')} className="p-0.5 rounded hover:bg-opacity-50 transition-colors"
                  style={{ backgroundColor: colors.hover }}>
                  <X size={12} style={{ color: colors.textSecondary }} />
                </button>
              </div>
            )}
          </div>

          {/* Code Panel Toggle */}
          <button onClick={() => setShowCodePanel(!showCodePanel)} 
            className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: showCodePanel ? colors.selected : colors.hover }}>
            <Code size={14} style={{ color: showCodePanel ? colors.primary : colors.textSecondary }} />
          </button>

          {/* Export Button */}
          <button onClick={() => setShowPublishModal(true)} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}>
            <DownloadCloud size={14} style={{ color: colors.textSecondary }} />
          </button>

          {/* User Menu */}
          <div className="relative">
            {showUserMenu && (
              <div className="absolute top-full right-0 mt-1 py-2 rounded shadow-lg z-50 border min-w-48"
                style={{ 
                  backgroundColor: colors.dropdownBg,
                  borderColor: colors.border
                }}>
                <div className="px-4 py-3 border-b" style={{ borderColor: colors.border }}>
                  <div className="text-sm font-medium" style={{ color: colors.text }}>Developer</div>
                  <div className="text-xs" style={{ color: colors.textSecondary }}>dev@apiscript.com</div>
                </div>
                {['My Implementations', 'Saved Templates', 'API Keys', 'Billing'].map(item => (
                  <button
                    key={item}
                    onClick={() => {
                      showToast(`Opening ${item}`, 'info');
                      setShowUserMenu(false);
                    }}
                    className="w-full px-4 py-2 text-sm text-left hover:bg-opacity-50 transition-colors"
                    style={{ color: colors.text }}
                  >
                    {item}
                  </button>
                ))}
                <div className="border-t my-2" style={{ borderColor: colors.border }}></div>
                <button
                  onClick={() => {
                    showToast('Opening settings', 'info');
                    setShowSettingsModal(true);
                    setShowUserMenu(false);
                  }}
                  className="w-full px-4 py-2 text-sm text-left hover:bg-opacity-50 transition-colors"
                  style={{ color: colors.text }}
                >
                  <Settings size={12} className="inline mr-2" />
                  Settings
                </button>
              </div>
            )}
          </div>

          {/* Theme Toggle */}
          <button onClick={() => setTheme(isDark ? 'light' : 'dark')} className="p-1.5 rounded hover:bg-opacity-50 transition-colors hover-lift"
            style={{ backgroundColor: colors.hover }}>
            {isDark ? <Sun size={14} style={{ color: colors.textSecondary }} /> : <Moon size={14} style={{ color: colors.textSecondary }} />}
          </button>
        </div>
      </div>

      {/* MAIN CONTENT */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left Sidebar - Collections */}
        <div className="w-64 border-r flex flex-col" style={{ 
          backgroundColor: colors.sidebar,
          borderColor: colors.border
        }}>
          <div className="p-4 border-b" style={{ borderColor: colors.border }}>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold" style={{ color: colors.text }}>API Collections</h3>
              <button className="p-1 rounded hover:bg-opacity-50 transition-colors hover-lift"
                onClick={() => setShowImportModal(true)}
                style={{ backgroundColor: colors.hover }}>
                <Plus size={12} style={{ color: colors.textSecondary }} />
              </button>
            </div>
            <div className="relative">
              <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2" size={12} style={{ color: colors.textSecondary }} />
              <input 
                type="text" 
                placeholder="Search APIs..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-8 pr-3 py-2 rounded text-sm focus:outline-none hover-lift"
                style={{ 
                  backgroundColor: colors.inputBg, 
                  border: `1px solid ${colors.border}`, 
                  color: colors.text 
                }} 
              />
              {searchQuery && (
                <div className="absolute right-2 top-1/2 transform -translate-y-1/2">
                  <button onClick={() => setSearchQuery('')} className="p-0.5 rounded hover:bg-opacity-50 transition-colors"
                    style={{ backgroundColor: colors.hover }}>
                    <X size={12} style={{ color: colors.textSecondary }} />
                  </button>
                </div>
              )}
            </div>
          </div>

          <div className="flex-1 overflow-auto p-2">
            {filteredCollections.map(collection => (
              <div key={collection.id} className="mb-3">
                <div className="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-opacity-50 transition-colors mb-1.5 cursor-pointer hover-lift"
                  onClick={() => toggleCollection(collection.id)}
                  style={{ backgroundColor: colors.hover }}>
                  {expandedCollections.includes(collection.id) ? (
                    <ChevronDown size={12} style={{ color: colors.textSecondary }} />
                  ) : (
                    <ChevronRight size={12} style={{ color: colors.textSecondary }} />
                  )}
                  <button onClick={(e) => {
                    e.stopPropagation();
                    const newCollections = collections.map(c => 
                      c.id === collection.id ? { ...c, isFavorite: !c.isFavorite } : c
                    );
                    setCollections(newCollections);
                    showToast(collection.isFavorite ? 'Removed from favorites' : 'Added to favorites', 'success');
                  }}>
                    {collection.isFavorite ? (
                      <Star size={12} fill="#FFB300" style={{ color: '#FFB300' }} />
                    ) : (
                      <Star size={12} style={{ color: colors.textSecondary }} />
                    )}
                  </button>
                  
                  <span className="text-sm font-medium flex-1 truncate" style={{ color: colors.text }}>
                    {collection.name}
                  </span>
                  
                  <span className="text-xs px-1.5 py-0.5 rounded" style={{ 
                    backgroundColor: colors.hover,
                    color: colors.textSecondary
                  }}>
                    {collection.version}
                  </span>
                </div>

                {expandedCollections.includes(collection.id) && collection.folders && (
                  <>
                    {collection.folders.map(folder => (
                      <div key={folder.id} className="ml-4 mb-2">
                        <div className="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-opacity-50 transition-colors mb-1.5 cursor-pointer hover-lift"
                          onClick={() => toggleFolder(folder.id)}
                          style={{ backgroundColor: colors.hover }}>
                          {expandedFolders.includes(folder.id) ? (
                            <ChevronDown size={11} style={{ color: colors.textSecondary }} />
                          ) : (
                            <ChevronRight size={11} style={{ color: colors.textSecondary }} />
                          )}
                          <FolderOpen size={11} style={{ color: colors.textSecondary }} />
                          
                          <span className="text-sm flex-1 truncate" style={{ color: colors.text }}>
                            {folder.name}
                          </span>
                        </div>

                        {expandedFolders.includes(folder.id) && folder.requests && (
                          <>
                            {folder.requests.map(request => (
                              <div key={request.id} className="flex items-center gap-2 ml-6 mb-1.5 group">
                                <button
                                  onClick={() => handleSelectRequest(request, collection, folder)}
                                  className="flex items-center gap-2 text-sm text-left transition-colors flex-1 px-2 py-1.5 rounded hover:bg-opacity-50 hover-lift"
                                  style={{ 
                                    color: selectedRequest?.id === request.id ? colors.primary : colors.text,
                                    backgroundColor: selectedRequest?.id === request.id ? colors.selected : 'transparent'
                                  }}>
                                  <div className="w-2 h-2 rounded-full flex-shrink-0" style={{ 
                                    backgroundColor: getMethodColor(request.method)
                                  }} />
                                  
                                  <span className="truncate">{request.name}</span>
                                  {request.implementations && (
                                    <span className="text-xs px-1 py-0.5 rounded ml-auto" style={{ 
                                      backgroundColor: colors.success + '20',
                                      color: colors.success
                                    }}>
                                      {Object.keys(request.implementations).length}
                                    </span>
                                  )}
                                </button>
                              </div>
                            ))}
                          </>
                        )}
                      </div>
                    ))}
                  </>
                )}
              </div>
            ))}
            
            {filteredCollections.length === 0 && searchQuery && (
              <div className="text-center p-4" style={{ color: colors.textSecondary }}>
                <Search size={20} className="mx-auto mb-2 opacity-50" />
                <p className="text-sm">No APIs found for "{searchQuery}"</p>
                <button className="mt-2 px-3 py-1.5 text-xs rounded hover:bg-opacity-50 transition-colors"
                  onClick={() => setSearchQuery('')}
                  style={{ backgroundColor: colors.hover, color: colors.text }}>
                  Clear Search
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Main Content Area */}
        {renderMainContent()}

        {/* Right Code Panel */}
        {showCodePanel && renderCodePanel()}
      </div>

      {/* MODALS */}
      {renderImportModal()}
      {renderSettingsModal()}
      {renderPublishModal()}

      {/* TOAST */}
      {renderToast()}
    </div>
  );
};

export default APIScript;