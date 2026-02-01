// components/common/IconSelector.js
import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";
import { Search, X, Check, Grid, List } from "lucide-react";

export default function IconSelector({
  isOpen,
  onClose,
  onSelect,
  selectedIcon,
  icons = [],
  title = "Select Icon",
  description = "Choose an icon"
}) {
  const [searchTerm, setSearchTerm] = useState('');
  const [viewMode, setViewMode] = useState('grid'); // 'grid' or 'list'
  
  const filteredIcons = icons.filter(icon =>
    icon.label.toLowerCase().includes(searchTerm.toLowerCase()) ||
    icon.class.toLowerCase().includes(searchTerm.toLowerCase())
  );
  
  const categories = {
    general: icons.filter(icon => ['Home', 'Settings', 'User', 'Users'].includes(icon.icon)),
    files: icons.filter(icon => ['Folder', 'FolderOpen', 'File', 'FileText'].includes(icon.icon)),
    media: icons.filter(icon => ['Image', 'Camera', 'Video', 'Music'].includes(icon.icon)),
    commerce: icons.filter(icon => ['ShoppingCart', 'CreditCard', 'Package', 'Truck'].includes(icon.icon)),
    tech: icons.filter(icon => ['Smartphone', 'Globe', 'Database', 'Server'].includes(icon.icon)),
    security: icons.filter(icon => ['Shield', 'Lock', 'Unlock', 'Key'].includes(icon.icon)),
    actions: icons.filter(icon => ['Download', 'Upload', 'Link', 'ExternalLink'].includes(icon.icon)),
    status: icons.filter(icon => ['CheckCircle', 'XCircle', 'AlertCircle', 'HelpCircle'].includes(icon.icon))
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-4xl max-h-[80vh] bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl shadow-2xl">
        <DialogHeader className="space-y-3">
          <div className="flex items-center justify-between">
            <DialogTitle className="text-xl font-bold text-gray-900 dark:text-gray-100">
              {title}
            </DialogTitle>
            <Badge variant="outline" className="text-xs">
              {filteredIcons.length} icons
            </Badge>
          </div>
          <DialogDescription className="text-sm text-gray-500 dark:text-gray-400">
            {description}
          </DialogDescription>
        </DialogHeader>
        
        <Separator className="bg-gray-200 dark:bg-gray-700" />
        
        {/* Search and Controls */}
        <div className="sticky top-0 z-10 bg-white dark:bg-gray-900 pt-2">
          <div className="flex flex-col sm:flex-row gap-3 mb-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <Input
                placeholder="Search icons by name..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
              {searchTerm && (
                <button
                  onClick={() => setSearchTerm('')}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  <X className="h-4 w-4" />
                </button>
              )}
            </div>
            
            <div className="flex items-center gap-2">
              <Button
                variant={viewMode === 'grid' ? 'default' : 'outline'}
                size="sm"
                onClick={() => setViewMode('grid')}
                className="flex items-center gap-2"
              >
                <Grid className="h-4 w-4" />
                <span className="hidden sm:inline">Grid</span>
              </Button>
              <Button
                variant={viewMode === 'list' ? 'default' : 'outline'}
                size="sm"
                onClick={() => setViewMode('list')}
                className="flex items-center gap-2"
              >
                <List className="h-4 w-4" />
                <span className="hidden sm:inline">List</span>
              </Button>
            </div>
          </div>
        </div>
        
        {/* Icon Display */}
        <div className="overflow-y-auto">
          {searchTerm ? (
            // Search Results
            <div>
              <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4">
                Search Results for "{searchTerm}"
              </h3>
              {filteredIcons.length > 0 ? (
                <div className={viewMode === 'grid' 
                  ? "grid grid-cols-4 sm:grid-cols-6 md:grid-cols-8 gap-3" 
                  : "space-y-2"
                }>
                  {filteredIcons.map(icon => (
                    <IconItem
                      key={icon.class}
                      icon={icon}
                      isSelected={selectedIcon === icon.class}
                      onSelect={() => onSelect(icon.class)}
                      viewMode={viewMode}
                    />
                  ))}
                </div>
              ) : (
                <div className="text-center py-12">
                  <div className="mx-auto w-12 h-12 rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center mb-4">
                    <Search className="h-6 w-6 text-gray-400" />
                  </div>
                  <p className="text-gray-500 dark:text-gray-400">
                    No icons found matching "{searchTerm}"
                  </p>
                </div>
              )}
            </div>
          ) : (
            // Categorized Icons
            <div className="space-y-6">
              {Object.entries(categories).map(([category, categoryIcons]) => (
                categoryIcons.length > 0 && (
                  <div key={category}>
                    <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3 capitalize">
                      {category}
                    </h3>
                    <div className={viewMode === 'grid' 
                      ? "grid grid-cols-4 sm:grid-cols-6 md:grid-cols-8 gap-3" 
                      : "space-y-2"
                    }>
                      {categoryIcons.map(icon => (
                        <IconItem
                          key={icon.class}
                          icon={icon}
                          isSelected={selectedIcon === icon.class}
                          onSelect={() => onSelect(icon.class)}
                          viewMode={viewMode}
                        />
                      ))}
                    </div>
                  </div>
                )
              ))}
            </div>
          )}
        </div>
        
        <DialogFooter className="pt-4 border-t border-gray-200 dark:border-gray-700">
          <div className="flex items-center justify-between w-full">
            <div className="text-xs text-gray-500 dark:text-gray-400">
              {selectedIcon && (
                <div className="flex items-center gap-2">
                  <span>Selected:</span>
                  <Badge variant="outline" className="text-xs">
                    {icons.find(i => i.class === selectedIcon)?.label}
                  </Badge>
                </div>
              )}
            </div>
            <div className="flex gap-2">
              <Button variant="outline" onClick={onClose}>
                Cancel
              </Button>
              <Button 
                onClick={() => {
                  if (selectedIcon) onSelect(selectedIcon);
                  onClose();
                }}
                disabled={!selectedIcon}
              >
                Select Icon
              </Button>
            </div>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function IconItem({ icon, isSelected, onSelect, viewMode }) {
  const IconComponent = icon.icon;
  
  if (viewMode === 'list') {
    return (
      <button
        onClick={onSelect}
        className={`flex items-center gap-3 w-full p-3 rounded-lg border transition-colors ${
          isSelected 
            ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20' 
            : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'
        }`}
      >
        <div className="flex items-center justify-center w-8 h-8 rounded-md bg-gray-100 dark:bg-gray-800">
          <i className={icon.class + " h-4 w-4 text-gray-600 dark:text-gray-400"} />
        </div>
        <div className="text-left">
          <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
            {icon.label}
          </p>
          <p className="text-xs text-gray-500 dark:text-gray-400">
            {icon.class}
          </p>
        </div>
        {isSelected && (
          <Check className="h-4 w-4 text-blue-600 dark:text-blue-400 ml-auto" />
        )}
      </button>
    );
  }
  
  return (
    <button
      onClick={onSelect}
      className={`flex flex-col items-center p-3 rounded-lg border transition-colors ${
        isSelected 
          ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20' 
          : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'
      }`}
      title={icon.label}
    >
      <div className="flex items-center justify-center w-10 h-10 rounded-md bg-gray-100 dark:bg-gray-800 mb-2">
        <i className={icon.class + " h-5 w-5 text-gray-600 dark:text-gray-400"} />
      </div>
      <p className="text-xs text-gray-700 dark:text-gray-300 text-center truncate w-full">
        {icon.label}
      </p>
      {isSelected && (
        <div className="absolute top-2 right-2">
          <Check className="h-3 w-3 text-blue-600 dark:text-blue-400" />
        </div>
      )}
    </button>
  );
}