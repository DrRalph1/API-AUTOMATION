import React, { useEffect, useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { showSuccess, showError, showConfirm, showLoading } from "@/lib/sweetAlert";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";
import { 
  Save, User, Mail, Phone, Shield, Lock, 
  UserPlus, UserCheck, AlertCircle, Info, CheckCircle, XCircle,
  Briefcase, Fingerprint, Key, Loader2
} from "lucide-react";
import { cn } from "@/lib/utils";

// Initial form state
const initialFormState = {
  username: '',
  password: '',
  emailAddress: '',
  phoneNumber: '',
  fullName: '',
  staffId: '',
  roleId: '',
  isActive: true
};

export default function UserModal({
  showUserDialog,
  setShowUserDialog,
  selectedUser,
  userForm,
  setUserForm,
  userRoles,
  handleCreateUser,
  handleUpdateUser,
  validationErrors = [],
  isLoading = false
}) {
  const isEditing = !!selectedUser;
  const [localForm, setLocalForm] = useState(initialFormState);
  const [localLoading, setLocalLoading] = useState(false);
  
  // Initialize form when dialog opens or selectedUser changes
  useEffect(() => {
    if (showUserDialog) {
      if (isEditing && selectedUser) {
        // For edit mode, populate with selected user data
        setLocalForm({
          username: selectedUser.username || '',
          password: '', // Don't pre-fill password
          emailAddress: selectedUser.emailAddress || '',
          phoneNumber: selectedUser.phoneNumber || '',
          fullName: selectedUser.fullName || '',
          staffId: selectedUser.staffId || '',
          roleId: selectedUser.roleId || '',
          isActive: selectedUser.isActive ?? true
        });
      } else {
        // For create mode, reset to initial state
        setLocalForm(initialFormState);
      }
    }
  }, [showUserDialog, isEditing, selectedUser]);

  // Update parent form when local form changes (optional, for controlled component)
  useEffect(() => {
    if (userForm && setUserForm) {
      setUserForm(localForm);
    }
  }, [localForm, setUserForm]);

  // Validation checks
  const isValidEmail = () => {
    if (!localForm.emailAddress) return false;
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(localForm.emailAddress);
  };

  const isValidPhone = () => {
    if (!localForm.phoneNumber) return true;
    const phoneRegex = /^[\+]?[1-9][\d]{0,15}$/;
    return phoneRegex.test(localForm.phoneNumber.replace(/\s+/g, ''));
  };

  const getFieldErrors = (fieldName) => {
    return validationErrors.filter(error => 
      error.toLowerCase().includes(fieldName.toLowerCase()) ||
      error.toLowerCase().includes(fieldName.replace(/([A-Z])/g, ' $1').toLowerCase())
    );
  };

  // Check all required fields
  const requiredFieldsValid = () => {
    const required = [
      localForm.username?.trim(),
      localForm.fullName?.trim(),
      localForm.emailAddress?.trim(),
      localForm.roleId,
      isValidEmail()
    ];
    
    // For create mode, password is also required
    if (!isEditing) {
      required.push(localForm.password?.trim());
    }
    
    return required.every(field => {
      if (typeof field === 'boolean') return field;
      return field && field.length > 0;
    });
  };

  const canSubmit = requiredFieldsValid() && isValidPhone();

  // Handle form field changes
  const handleInputChange = (field, value) => {
    setLocalForm(prev => ({
      ...prev,
      [field]: value
    }));
  };

  // Handle form submission with sweet alerts
  const handleSubmit = async () => {
    if (!canSubmit) {
      showError(
        "Validation Error", 
        "Please fill all required fields correctly before submitting.",
        "oracle",
        { timer: 5000 }
      );
      return;
    }

    const formData = {
      username: localForm.username,
      fullName: localForm.fullName,
      emailAddress: localForm.emailAddress,
      phoneNumber: localForm.phoneNumber || null,
      staffId: localForm.staffId || null,
      roleId: localForm.roleId,
      isActive: localForm.isActive
    };
    
    if (!isEditing) {
      formData.password = localForm.password;
    }
    
    try {
      // Show loading alert
      const loadingAlert = showLoading(
        isEditing ? 'Updating user...' : 'Creating user...',
        "oracle"
      );
      
      setLocalLoading(true);
      
      // Call the appropriate handler
      if (isEditing) {
        await handleUpdateUser(formData);
      } else {
        await handleCreateUser(formData);
      }
      
      // Close loading alert
      loadingAlert.close();
      
      // Show success message
      showSuccess(
        isEditing ? 'User Updated' : 'User Created',
        isEditing 
          ? 'User information has been updated successfully.'
          : 'New user has been created successfully.',
        "oracle",
        { timer: 3000 }
      );
      
      // Close the modal
      setShowUserDialog(false);
      
    } catch (error) {
      // Close loading alert if still open
      if (window.__SWEET_ALERT_INSTANCE__?.closeAlert) {
        window.__SWEET_ALERT_INSTANCE__.closeAlert();
      }
      
      // Show error message
      showError(
        isEditing ? 'Update Failed' : 'Creation Failed',
        error.message || 'An unexpected error occurred. Please try again.',
        "oracle",
        { timer: 5000 }
      );
    } finally {
      setLocalLoading(false);
    }
  };

  // Handle delete user confirmation
  const handleDeleteUser = async () => {
    if (!selectedUser?.userId) return;
    
    const result = await showConfirm(
      'Delete User',
      `Are you sure you want to delete user "${selectedUser.username}"? This action cannot be undone.`,
      'Delete',
      "oracle",
      { 
        cancelText: 'Cancel',
        timer: false 
      }
    );
    
    if (result.isConfirmed) {
      try {
        // You would call your delete API here
        // await deleteUser(selectedUser.userId);
        
        showSuccess(
          'User Deleted',
          `User "${selectedUser.username}" has been deleted successfully.`,
          "oracle",
          { timer: 3000 }
        );
        
        setShowUserDialog(false);
      } catch (error) {
        showError(
          'Delete Failed',
          error.message || 'Failed to delete user. Please try again.',
          "oracle",
          { timer: 5000 }
        );
      }
    }
  };

  // Debug: Show what will be sent
  const getDebugData = () => {
    const data = {
      username: localForm.username,
      fullName: localForm.fullName,
      emailAddress: localForm.emailAddress,
      phoneNumber: localForm.phoneNumber || null,
      staffId: localForm.staffId || null,
      roleId: localForm.roleId,
      isActive: localForm.isActive
    };
    
    if (!isEditing) {
      data.password = localForm.password;
    }
    
    return data;
  };

  return (
    <Dialog open={showUserDialog} onOpenChange={setShowUserDialog}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl shadow-2xl">
        <DialogHeader className="space-y-3">
          <div className="flex items-center justify-between">
            <DialogTitle className="text-xl font-bold text-gray-900 dark:text-gray-100">
              {isEditing ? 'Edit User' : 'Create New User'}
            </DialogTitle>
            {/* {isEditing && selectedUser.userId && (
              <Badge variant="outline" className="font-mono text-xs bg-gray-100 dark:bg-gray-800">
                <Fingerprint className="h-3 w-3 mr-1" />
                ID: {selectedUser.userId.substring(0, 8)}...
              </Badge>
            )} */}
          </div>
          <DialogDescription className="text-sm text-gray-500 dark:text-gray-400">
            {isEditing 
              ? 'Update user information and permissions' 
              : 'Add a new user to the system with appropriate roles and permissions'}
            {validationErrors.length > 0 && (
              <div className="mt-2 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-md">
                <p className="text-xs text-red-600 dark:text-red-400 font-semibold flex items-center gap-1">
                  <AlertCircle className="h-3.5 w-3.5" />
                  Please fix the following errors:
                </p>
                <ul className="text-xs text-red-500 dark:text-red-300 mt-2 ml-4 list-disc space-y-1">
                  {validationErrors.map((error, index) => (
                    <li key={index}>{error}</li>
                  ))}
                </ul>
              </div>
            )}
          </DialogDescription>
        </DialogHeader>
        
        <Separator className="bg-gray-200 dark:bg-gray-700 my-1" />
        
        <div className="space-y-8 py-2">
          {/* Debug Section - Remove in production */}
          {/* <div className="p-3 rounded-lg bg-yellow-50 dark:bg-yellow-900/10 border border-yellow-200 dark:border-yellow-800/30">
            <div className="flex items-center justify-between mb-2">
              <Label className="text-xs font-semibold text-yellow-700 dark:text-yellow-300 flex items-center gap-1">
                <Info className="h-3.5 w-3.5" />
                Debug Preview
              </Label>
              <Badge variant="outline" className="text-xs bg-yellow-100 dark:bg-yellow-900/20 text-yellow-700 dark:text-yellow-300">
                Development Only
              </Badge>
            </div>
            <pre className="text-xs font-mono text-yellow-600 dark:text-yellow-400 bg-white/50 dark:bg-black/20 p-2 rounded overflow-auto max-h-40">
              {JSON.stringify(getDebugData(), null, 2)}
            </pre>
          </div> */}

          {/* Info Box for Edit Mode */}
          {isEditing && selectedUser && (
            <div className="p-4 rounded-lg bg-gradient-to-r from-blue-50 to-blue-100 dark:from-blue-900/10 dark:to-blue-800/10 border border-blue-200 dark:border-blue-800/30 space-y-3">
              <div className="flex items-start gap-3">
                <Info className="h-5 w-5 text-blue-600 dark:text-blue-400 mt-0.5 flex-shrink-0" />
                <div className="grid grid-cols-2 gap-4 flex-1">
                  <div>
                    <p className="text-xs text-blue-700/80 dark:text-blue-400/80">Username</p>
                    <p className="text-sm font-semibold text-blue-800 dark:text-blue-300">
                      {selectedUser.username}
                    </p>
                  </div>
                  {selectedUser.staffId && (
                    <div>
                      <p className="text-xs text-blue-700/80 dark:text-blue-400/80">Staff ID</p>
                      <p className="text-sm font-semibold text-blue-800 dark:text-blue-300">
                        {selectedUser.staffId}
                      </p>
                    </div>
                  )}
                  <div>
                    <p className="text-xs text-blue-700/80 dark:text-blue-400/80">Status</p>
                    <div className="flex items-center gap-2">
                      <Badge 
                        variant="outline" 
                        className={cn(
                          "text-xs",
                          selectedUser.isActive ? 
                            "bg-emerald-500/10 text-emerald-700 dark:text-emerald-300 border-emerald-500/30" :
                            "bg-red-500/10 text-red-700 dark:text-red-300 border-red-500/30"
                        )}
                      >
                        {selectedUser.isActive ? 'Active' : 'Inactive'}
                      </Badge>
                      {selectedUser.isDefaultPassword && (
                        <Badge 
                          variant="outline" 
                          className="text-xs bg-amber-500/10 text-amber-700 dark:text-amber-300 border-amber-500/30"
                        >
                          Default Password
                        </Badge>
                      )}
                    </div>
                  </div>
                  {selectedUser.roleName && (
                    <div>
                      <p className="text-xs text-blue-700/80 dark:text-blue-400/80">Role</p>
                      <p className="text-sm font-semibold text-blue-800 dark:text-blue-300 uppercase">
                        {selectedUser.roleName}
                      </p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Basic Information Section */}
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <User className="h-4 w-4 text-gray-700 dark:text-gray-300" />
              <Label className="text-sm font-semibold text-gray-900 dark:text-gray-100">
                Basic Information
              </Label>
              <span className="text-xs text-red-500">* Required fields</span>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              {/* Left Column */}
              <div className="space-y-6">
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="username" className="text-sm">
                      <span className="text-red-500">*</span> Username
                    </Label>
                    <span className="text-xs text-gray-500">Unique identifier for login</span>
                  </div>
                  <div className="relative">
                    <Input
                      id="username"
                      value={localForm.username}
                      onChange={(e) => handleInputChange('username', e.target.value)}
                      placeholder="e.g., john.doe"
                      className={cn(
                        "h-10 bg-gray-50 dark:bg-gray-800/50 border-gray-300 dark:border-gray-700",
                        localForm.username && "border-blue-300 dark:border-blue-700",
                        getFieldErrors('username').length > 0 && "border-red-300 dark:border-red-700"
                      )}
                    />
                    {localForm.username && !getFieldErrors('username').length && (
                      <div className="absolute right-3 top-1/2 -translate-y-1/2">
                        <UserCheck className="h-4 w-4 text-emerald-500" />
                      </div>
                    )}
                    {getFieldErrors('username').length > 0 && (
                      <div className="absolute right-3 top-1/2 -translate-y-1/2">
                        <AlertCircle className="h-4 w-4 text-red-500" />
                      </div>
                    )}
                  </div>
                  {getFieldErrors('username').map((error, index) => (
                    <p key={index} className="text-xs text-red-500 dark:text-red-400">
                      {error}
                    </p>
                  ))}
                </div>
                
                <div className="space-y-3">
                  <Label htmlFor="fullName" className="text-sm">
                    <span className="text-red-500">*</span> Full Name
                  </Label>
                  <div className="relative">
                    <Input
                      id="fullName"
                      value={localForm.fullName}
                      onChange={(e) => handleInputChange('fullName', e.target.value)}
                      placeholder="e.g., John Doe"
                      className={cn(
                        "h-10 bg-gray-50 dark:bg-gray-800/50 border-gray-300 dark:border-gray-700",
                        localForm.fullName && "border-blue-300 dark:border-blue-700",
                        getFieldErrors('fullName').length > 0 && "border-red-300 dark:border-red-700"
                      )}
                    />
                    {localForm.fullName && !getFieldErrors('fullName').length && (
                      <div className="absolute right-3 top-1/2 -translate-y-1/2">
                        <User className="h-4 w-4 text-blue-500" />
                      </div>
                    )}
                    {getFieldErrors('fullName').length > 0 && (
                      <div className="absolute right-3 top-1/2 -translate-y-1/2">
                        <AlertCircle className="h-4 w-4 text-red-500" />
                      </div>
                    )}
                  </div>
                  {getFieldErrors('fullName').map((error, index) => (
                    <p key={index} className="text-xs text-red-500 dark:text-red-400">
                      {error}
                    </p>
                  ))}
                </div>
                
                <div className="space-y-3">
                  <Label htmlFor="staffId" className="text-sm">
                    Staff ID (Optional)
                  </Label>
                  <div className="relative">
                    <Input
                      id="staffId"
                      value={localForm.staffId}
                      onChange={(e) => handleInputChange('staffId', e.target.value)}
                      placeholder="e.g., STF001"
                      className={cn(
                        "h-10 bg-gray-50 dark:bg-gray-800/50 border-gray-300 dark:border-gray-700",
                        localForm.staffId && "border-blue-300 dark:border-blue-700"
                      )}
                    />
                    {localForm.staffId && (
                      <div className="absolute right-3 top-1/2 -translate-y-1/2">
                        <Briefcase className="h-4 w-4 text-blue-500" />
                      </div>
                    )}
                  </div>
                </div>
              </div>
              
              {/* Right Column */}
              <div className="space-y-6">
                <div className="space-y-3">
                  <Label htmlFor="emailAddress" className="text-sm">
                    <span className="text-red-500">*</span> Email Address
                  </Label>
                  <div className="relative">
                    <Input
                      id="emailAddress"
                      type="email"
                      value={localForm.emailAddress}
                      onChange={(e) => handleInputChange('emailAddress', e.target.value)}
                      placeholder="e.g., john.doe@gmail.com"
                      className={cn(
                        "h-10 bg-gray-50 dark:bg-gray-800/50 border-gray-300 dark:border-gray-700",
                        localForm.emailAddress && !isValidEmail() && "border-red-300 dark:border-red-700",
                        localForm.emailAddress && isValidEmail() && "border-emerald-300 dark:border-emerald-700",
                        getFieldErrors('email').length > 0 && "border-red-300 dark:border-red-700"
                      )}
                    />
                    {localForm.emailAddress && (
                      <div className="absolute right-3 top-1/2 -translate-y-1/2">
                        {isValidEmail() && !getFieldErrors('email').length ? (
                          <CheckCircle className="h-4 w-4 text-emerald-500" />
                        ) : (
                          <AlertCircle className="h-4 w-4 text-red-500" />
                        )}
                      </div>
                    )}
                  </div>
                  {getFieldErrors('email').map((error, index) => (
                    <p key={index} className="text-xs text-red-500 dark:text-red-400">
                      {error}
                    </p>
                  ))}
                  {localForm.emailAddress && !isValidEmail() && !getFieldErrors('email').length && (
                    <p className="text-xs text-red-500 dark:text-red-400">
                      Please enter a valid email address
                    </p>
                  )}
                </div>
                
                <div className="space-y-3">
                  <Label htmlFor="phoneNumber" className="text-sm">
                    Phone Number (Optional)
                  </Label>
                  <div className="relative">
                    <Input
                      id="phoneNumber"
                      value={localForm.phoneNumber}
                      onChange={(e) => handleInputChange('phoneNumber', e.target.value)}
                      placeholder="e.g., +233241234567"
                      className={cn(
                        "h-10 bg-gray-50 dark:bg-gray-800/50 border-gray-300 dark:border-gray-700",
                        localForm.phoneNumber && !isValidPhone() && "border-red-300 dark:border-red-700",
                        localForm.phoneNumber && isValidPhone() && "border-emerald-300 dark:border-emerald-700"
                      )}
                    />
                    {localForm.phoneNumber && (
                      <div className="absolute right-3 top-1/2 -translate-y-1/2">
                        {isValidPhone() ? (
                          <Phone className="h-4 w-4 text-emerald-500" />
                        ) : (
                          <AlertCircle className="h-4 w-4 text-red-500" />
                        )}
                      </div>
                    )}
                  </div>
                  {localForm.phoneNumber && !isValidPhone() && (
                    <p className="text-xs text-red-500 dark:text-red-400">
                      Please enter a valid phone number
                    </p>
                  )}
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                    Format: +[country code][number] e.g., +233241234567
                  </p>
                </div>
                
                {/* Password field for create mode only */}
                {!isEditing && (
                  <div className="space-y-3">
                    <Label htmlFor="password" className="text-sm">
                      <span className="text-red-500">*</span> Initial Password
                    </Label>
                    <div className="relative">
                      <Input
                        id="password"
                        type="password"
                        value={localForm.password}
                        onChange={(e) => handleInputChange('password', e.target.value)}
                        placeholder="Enter initial password"
                        className={cn(
                          "h-10 bg-gray-50 dark:bg-gray-800/50 border-gray-300 dark:border-gray-700",
                          localForm.password && "border-blue-300 dark:border-blue-700",
                          getFieldErrors('password').length > 0 && "border-red-300 dark:border-red-700"
                        )}
                      />
                      {localForm.password && (
                        <div className="absolute right-3 top-1/2 -translate-y-1/2">
                          <Key className="h-4 w-4 text-blue-500" />
                        </div>
                      )}
                    </div>
                    {getFieldErrors('password').map((error, index) => (
                      <p key={index} className="text-xs text-red-500 dark:text-red-400">
                        {error}
                      </p>
                    ))}
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                      User will be prompted to change this on first login
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Role and Status Section */}
          <div className="space-y-8">
            <Separator className="bg-gray-200 dark:bg-gray-700" />
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div className="space-y-4">
                <Label className="text-sm font-semibold text-gray-900 dark:text-gray-100 flex items-center gap-2">
                  <Shield className="h-4 w-4" />
                  <span><span className="text-red-500">*</span> Role & Permissions</span>
                </Label>
                
                <div className="space-y-3">
                  <Select
                    value={localForm.roleId}
                    onValueChange={(value) => handleInputChange('roleId', value)}
                  >
                    <SelectTrigger className={cn(
                      "h-10 bg-gray-50 dark:bg-gray-800/50 border-gray-300 dark:border-gray-700",
                      localForm.roleId && "border-blue-300 dark:border-blue-700",
                      getFieldErrors('role').length > 0 && "border-red-300 dark:border-red-700"
                    )}>
                      <SelectValue placeholder="Select a role" />
                    </SelectTrigger>
                    <SelectContent className="bg-white dark:bg-gray-900 border-gray-200 dark:border-gray-800 max-h-60">
                      {userRoles.map((role) => (
                        <SelectItem 
                          key={role.roleId || role.id} 
                          value={role.roleId || role.id}
                          className="focus:bg-gray-100 dark:focus:bg-gray-800 h-12"
                        >
                          <div className="flex items-center gap-3">
                            <div className={cn(
                              "h-2.5 w-2.5 rounded-full flex-shrink-0",
                              role.roleName?.toLowerCase() === 'admin' && "bg-purple-500",
                              role.roleName?.toLowerCase() === 'super_admin' && "bg-red-500",
                              role.roleName?.toLowerCase() === 'user' && "bg-blue-500",
                              role.roleName?.toLowerCase() === 'teller' && "bg-green-500",
                              "bg-gray-500"
                            )} />
                            <div className="flex flex-col">
                              <span className="font-medium">{role.roleName || role.name}</span>
                              {role.description && (
                                <span className="text-xs text-gray-500 dark:text-gray-400 truncate max-w-[200px]">
                                  {role.description}
                                </span>
                              )}
                            </div>
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {getFieldErrors('role').map((error, index) => (
                    <p key={index} className="text-xs text-red-500 dark:text-red-400">
                      {error}
                    </p>
                  ))}
                  {localForm.roleId && !getFieldErrors('role').length && (
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                      Selected: <span className="font-medium text-gray-700 dark:text-gray-300">
                        {userRoles.find(r => (r.roleId || r.id) === localForm.roleId)?.roleName || 'Unknown Role'}
                      </span>
                    </p>
                  )}
                </div>
              </div>
              
              <div className="space-y-4">
                <Label className="text-sm font-semibold text-gray-900 dark:text-gray-100 flex items-center gap-2">
                  <Lock className="h-4 w-4" />
                  Account Status
                </Label>
                
                <div className="space-y-3">
                  <Select
                    value={localForm.isActive?.toString() || 'true'}
                    onValueChange={(value) => handleInputChange('isActive', value === 'true')}
                  >
                    <SelectTrigger className={cn(
                      "h-10 bg-gray-50 dark:bg-gray-800/50 border-gray-300 dark:border-gray-700",
                      localForm.isActive && "border-blue-300 dark:border-blue-700"
                    )}>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent className="bg-white dark:bg-gray-900 border-gray-200 dark:border-gray-800">
                      <SelectItem value="true" className="focus:bg-gray-100 dark:focus:bg-gray-800 h-10">
                        <div className="flex items-center gap-2">
                          <div className="h-2.5 w-2.5 rounded-full bg-emerald-500" />
                          <span>Active</span>
                          <Badge variant="outline" className="ml-2 text-xs bg-emerald-500/10 text-emerald-700 dark:text-emerald-300">
                            Recommended
                          </Badge>
                        </div>
                      </SelectItem>
                      <SelectItem value="false" className="focus:bg-gray-100 dark:focus:bg-gray-800 h-10">
                        <div className="flex items-center gap-2">
                          <div className="h-2.5 w-2.5 rounded-full bg-gray-400" />
                          <span>Inactive</span>
                        </div>
                      </SelectItem>
                    </SelectContent>
                  </Select>
                  {localForm.isActive !== undefined && (
                    <div className="flex items-center gap-2 pt-1">
                      {localForm.isActive ? (
                        <>
                          <div className="h-2.5 w-2.5 rounded-full bg-emerald-500 animate-pulse" />
                          <span className="text-xs text-emerald-600 dark:text-emerald-400">
                            User can log in and use the system
                          </span>
                        </>
                      ) : (
                        <>
                          <div className="h-2.5 w-2.5 rounded-full bg-gray-400" />
                          <span className="text-xs text-gray-600 dark:text-gray-400">
                            User cannot log in
                          </span>
                        </>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Missing Fields Warning */}
          {/* {!canSubmit && (
            <div className="p-4 rounded-lg border border-amber-200 dark:border-amber-800/30 bg-amber-50 dark:bg-amber-900/10 space-y-2">
              <div className="flex items-center gap-2">
                <AlertCircle className="h-4 w-4 text-amber-600 dark:text-amber-400" />
                <p className="text-sm font-medium text-amber-700 dark:text-amber-300">
                  Missing required fields
                </p>
              </div>
              <div className="text-xs text-amber-600 dark:text-amber-400 ml-6">
                <ul className="list-disc space-y-1">
                  {!localForm.username?.trim() && <li>Username is required</li>}
                  {!localForm.fullName?.trim() && <li>Full Name is required</li>}
                  {!localForm.emailAddress?.trim() && <li>Email Address is required</li>}
                  {!isValidEmail() && localForm.emailAddress && <li>Email Address is invalid</li>}
                  {!localForm.roleId && <li>Role is required</li>}
                  {!isEditing && !localForm.password?.trim() && <li>Password is required</li>}
                  {!isValidPhone() && localForm.phoneNumber && <li>Phone Number is invalid</li>}
                </ul>
              </div>
            </div>
          )} */}

          {/* Additional Information for Edit Mode */}
          {isEditing && selectedUser && (
            <div className="p-4 rounded-lg bg-gradient-to-r from-blue-50 to-blue-100 dark:from-blue-900/10 dark:to-blue-800/10 border border-blue-200 dark:border-blue-800/30 space-y-3">
              <Label className="text-sm font-semibold text-blue-800 dark:text-blue-300 flex items-center gap-2">
                <Info className="h-4 w-4" />
                System Information
              </Label>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {selectedUser.userId && (
                  <div>
                    <p className="text-xs text-blue-700/80 dark:text-blue-400/80">User ID</p>
                    <p className="text-sm font-mono font-medium text-blue-900 dark:text-blue-200 truncate">
                      {selectedUser.userId}
                    </p>
                  </div>
                )}
                {selectedUser.createdDate && (
                  <div>
                    <p className="text-xs text-blue-700/80 dark:text-blue-400/80">Created On</p>
                    <p className="text-sm font-medium text-blue-900 dark:text-blue-200">
                      {new Date(selectedUser.createdDate).toLocaleDateString()}
                    </p>
                  </div>
                )}
                {selectedUser.lastLogin && (
                  <div>
                    <p className="text-xs text-blue-700/80 dark:text-blue-400/80">Last Login</p>
                    <p className="text-sm font-medium text-blue-900 dark:text-blue-200">
                      {new Date(selectedUser.lastLogin).toLocaleDateString()}
                    </p>
                  </div>
                )}
                {selectedUser.lastModifiedDate && (
                  <div>
                    <p className="text-xs text-blue-700/80 dark:text-blue-400/80">Last Updated</p>
                    <p className="text-sm font-medium text-blue-900 dark:text-blue-200">
                      {new Date(selectedUser.lastModifiedDate).toLocaleDateString()}
                    </p>
                  </div>
                )}
                {selectedUser.failedLoginAttempts !== undefined && (
                  <div>
                    <p className="text-xs text-blue-700/80 dark:text-blue-400/80">Failed Logins</p>
                    <p className="text-sm font-medium text-blue-900 dark:text-blue-200">
                      {selectedUser.failedLoginAttempts}
                    </p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
        
        <DialogFooter className="flex items-center justify-between pt-6 border-t border-gray-200 dark:border-gray-800 mt-2">
          <div className="flex items-center gap-2">
            {/* Delete button for edit mode */}
            {isEditing && selectedUser && (
              <Button 
                variant="destructive"
                onClick={handleDeleteUser}
                className="rounded-lg h-9 border-red-600 dark:border-red-700 bg-gradient-to-r from-red-600 to-rose-500 hover:from-red-700 hover:to-rose-600"
                disabled={isLoading || localLoading}
              >
                <XCircle className="h-4 w-4 mr-2" />
                Delete
              </Button>
            )}
            
            <DialogClose asChild>
              <Button 
                variant="outline"
                className="rounded-lg border-gray-300 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800 h-9"
                disabled={isLoading || localLoading}
              >
                Cancel
              </Button>
            </DialogClose>
          </div>
          <div className="flex items-center gap-4">
            <div className="text-xs text-gray-500 dark:text-gray-400 min-w-[120px]">
              {validationErrors.length > 0 ? (
                <span className="text-red-500 dark:text-red-400 flex items-center gap-1">
                  <AlertCircle className="h-3.5 w-3.5" />
                  {validationErrors.length} error{validationErrors.length > 1 ? 's' : ''}
                </span>
              ) : !canSubmit ? (
                <span className="text-amber-600 dark:text-amber-400 flex items-center gap-1">
                  <AlertCircle className="h-3.5 w-3.5" />
                  Fill required fields
                </span>
              ) : (
                <span className="text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
                  <CheckCircle className="h-3.5 w-3.5" />
                  Ready to submit
                </span>
              )}
            </div>
            <Button
              onClick={handleSubmit}
              disabled={!canSubmit || isLoading || localLoading}
              className={cn(
                "rounded-lg transition-all duration-300 h-9 min-w-[120px]",
                canSubmit && !isLoading && !localLoading
                  ? isEditing
                    ? "bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-700 hover:to-blue-600 shadow-lg shadow-blue-500/25"
                    : "bg-gradient-to-r from-emerald-600 to-green-500 hover:from-emerald-700 hover:to-green-600 shadow-lg shadow-emerald-500/25"
                  : "bg-gradient-to-r from-gray-400 to-gray-500 cursor-not-allowed opacity-60"
              )}
            >
              {isLoading || localLoading ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  {isEditing ? 'Updating...' : 'Creating...'}
                </>
              ) : isEditing ? (
                <>
                  <Save className="h-4 w-4 mr-2" />
                  Update User
                </>
              ) : (
                <>
                  <UserPlus className="h-4 w-4 mr-2" />
                  Create User
                </>
              )}
            </Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}