// common/icons.js
export const ICON_OPTIONS = [
  // Files & Folders
  { value: 'lucide:folder', label: 'Folder', icon: '📁' },
  { value: 'lucide:folder-open', label: 'Open Folder', icon: '📂' },
  { value: 'lucide:file', label: 'File', icon: '📄' },
  { value: 'lucide:file-text', label: 'Text File', icon: '📝' },
  { value: 'lucide:file-code', label: 'Code File', icon: '💻' },
  { value: 'lucide:file-image', label: 'Image File', icon: '🖼️' },
  { value: 'lucide:file-video', label: 'Video File', icon: '🎥' },
  { value: 'lucide:file-audio', label: 'Audio File', icon: '🎵' },
  { value: 'lucide:file-zip', label: 'Zip File', icon: '🗜️' },
  { value: 'lucide:folder-plus', label: 'Add Folder', icon: '📁➕' },
  
  // Documents & Tools
  { value: 'lucide:clipboard', label: 'Clipboard', icon: '📋' },
  { value: 'lucide:edit', label: 'Edit', icon: '✏️' },
  { value: 'lucide:pen-tool', label: 'Pen Tool', icon: '🖊️' },
  { value: 'lucide:scissors', label: 'Scissors', icon: '✂️' },
  { value: 'lucide:printer', label: 'Printer', icon: '🖨️' },
  { value: 'lucide:download', label: 'Download', icon: '⬇️' },
  { value: 'lucide:upload', label: 'Upload', icon: '⬆️' },
  { value: 'lucide:copy', label: 'Copy', icon: '📄✂️' },
  { value: 'lucide:paste', label: 'Paste', icon: '📋🔗' },
  
  // Navigation & Layout
  { value: 'lucide:grid', label: 'Grid', icon: '🔲' },
  { value: 'lucide:list', label: 'List', icon: '📋' },
  { value: 'lucide:layers', label: 'Layers', icon: '📚' },
  { value: 'lucide:sidebar', label: 'Sidebar', icon: '📑' },
  { value: 'lucide:layout', label: 'Layout', icon: '📐' },
  { value: 'lucide:columns', label: 'Columns', icon: '📊' },
  
  // Media & Entertainment
  { value: 'lucide:music', label: 'Music', icon: '🎵' },
  { value: 'lucide:film', label: 'Film', icon: '🎬' },
  { value: 'lucide:camera', label: 'Camera', icon: '📷' },
  { value: 'lucide:video', label: 'Video', icon: '📹' },
  { value: 'lucide:mic', label: 'Microphone', icon: '🎤' },
  { value: 'lucide:headphones', label: 'Headphones', icon: '🎧' },
  { value: 'lucide:tv', label: 'TV', icon: '📺' },
  { value: 'lucide:gamepad', label: 'Gamepad', icon: '🎮' },
  
  // Communication
  { value: 'lucide:message-circle', label: 'Message', icon: '💬' },
  { value: 'lucide:mail', label: 'Mail', icon: '✉️' },
  { value: 'lucide:phone', label: 'Phone', icon: '📞' },
  { value: 'lucide:send', label: 'Send', icon: '📤' },
  { value: 'lucide:inbox', label: 'Inbox', icon: '📥' },
  { value: 'lucide:at-sign', label: 'Mention', icon: '@' },
  { value: 'lucide:hashtag', label: 'Hashtag', icon: '#' },
  
  // Time & Date
  { value: 'lucide:calendar', label: 'Calendar', icon: '📅' },
  { value: 'lucide:clock', label: 'Clock', icon: '⏰' },
  { value: 'lucide:watch', label: 'Watch', icon: '⌚' },
  { value: 'lucide:alarm-clock', label: 'Alarm', icon: '⏰🔔' },
  { value: 'lucide:calendar-days', label: 'Calendar Days', icon: '📆' },
  
  // Location & Maps
  { value: 'lucide:map-pin', label: 'Map Pin', icon: '📍' },
  { value: 'lucide:compass', label: 'Compass', icon: '🧭' },
  { value: 'lucide:map', label: 'Map', icon: '🗺️' },
  { value: 'lucide:globe', label: 'Globe', icon: '🌐' },
  { value: 'lucide:navigation', label: 'Navigation', icon: '🧭➡️' },
  
  // Finance & Commerce
  { value: 'lucide:dollar-sign', label: 'Dollar', icon: '💵' },
  { value: 'lucide:credit-card', label: 'Credit Card', icon: '💳' },
  { value: 'lucide:shopping-cart', label: 'Shopping Cart', icon: '🛒' },
  { value: 'lucide:shopping-bag', label: 'Shopping Bag', icon: '🛍️' },
  { value: 'lucide:gift', label: 'Gift', icon: '🎁' },
  { value: 'lucide:package', label: 'Package', icon: '📦' },
  { value: 'lucide:receipt', label: 'Receipt', icon: '🧾' },
  { value: 'lucide:wallet', label: 'Wallet', icon: '👛' },
  { value: 'lucide:banknote', label: 'Banknote', icon: '💰' },
  { value: 'lucide:bitcoin', label: 'Bitcoin', icon: '₿' },
  
  // Business & Work
  { value: 'lucide:building', label: 'Building', icon: '🏢' },
  { value: 'lucide:home', label: 'Home', icon: '🏠' },
  { value: 'lucide:briefcase', label: 'Briefcase', icon: '💼' },
  { value: 'lucide:users', label: 'Users', icon: '👥' },
  { value: 'lucide:user', label: 'User', icon: '👤' },
  { value: 'lucide:user-plus', label: 'Add User', icon: '👤➕' },
  { value: 'lucide:user-check', label: 'User Check', icon: '👤✅' },
  { value: 'lucide:bar-chart', label: 'Chart', icon: '📊' },
  { value: 'lucide:trending-up', label: 'Trending Up', icon: '📈' },
  { value: 'lucide:trending-down', label: 'Trending Down', icon: '📉' },
  
  // Technology & Devices
  { value: 'lucide:smartphone', label: 'Smartphone', icon: '📱' },
  { value: 'lucide:monitor', label: 'Monitor', icon: '🖥️' },
  { value: 'lucide:laptop', label: 'Laptop', icon: '💻' },
  { value: 'lucide:tablet', label: 'Tablet', icon: '📱💻' },
  { value: 'lucide:server', label: 'Server', icon: '🖥️' },
  { value: 'lucide:database', label: 'Database', icon: '🗄️' },
  { value: 'lucide:cloud', label: 'Cloud', icon: '☁️' },
  { value: 'lucide:wifi', label: 'WiFi', icon: '📶' },
  { value: 'lucide:cpu', label: 'CPU', icon: '⚙️💻' },
  { value: 'lucide:hard-drive', label: 'Hard Drive', icon: '💾' },
  
  // Security & Access
  { value: 'lucide:lock', label: 'Lock', icon: '🔒' },
  { value: 'lucide:unlock', label: 'Unlock', icon: '🔓' },
  { value: 'lucide:key', label: 'Key', icon: '🔑' },
  { value: 'lucide:shield', label: 'Shield', icon: '🛡️' },
  { value: 'lucide:shield-check', label: 'Shield Check', icon: '🛡️✅' },
  { value: 'lucide:shield-alert', label: 'Shield Alert', icon: '🛡️⚠️' },
  { value: 'lucide:eye', label: 'Eye', icon: '👁️' },
  { value: 'lucide:eye-off', label: 'Eye Off', icon: '👁️🚫' },
  { value: 'lucide:fingerprint', label: 'Fingerprint', icon: '👆' },
  
  // Settings & Controls
  { value: 'lucide:settings', label: 'Settings', icon: '⚙️' },
  { value: 'lucide:sliders', label: 'Sliders', icon: '🎚️' },
  { value: 'lucide:toggle-left', label: 'Toggle Off', icon: '🔘⬅️' },
  { value: 'lucide:toggle-right', label: 'Toggle On', icon: '🔘➡️' },
  { value: 'lucide:checkbox', label: 'Checkbox', icon: '☑️' },
  { value: 'lucide:radio', label: 'Radio', icon: '🔘' },
  { value: 'lucide:power', label: 'Power', icon: '⏻' },
  { value: 'lucide:refresh-cw', label: 'Refresh', icon: '🔄' },
  
  // Transportation
  { value: 'lucide:car', label: 'Car', icon: '🚗' },
  { value: 'lucide:truck', label: 'Truck', icon: '🚚' },
  { value: 'lucide:plane', label: 'Plane', icon: '✈️' },
  { value: 'lucide:train', label: 'Train', icon: '🚆' },
  { value: 'lucide:ship', label: 'Ship', icon: '🚢' },
  { value: 'lucide:bicycle', label: 'Bicycle', icon: '🚲' },
  { value: 'lucide:bus', label: 'Bus', icon: '🚌' },
  { value: 'lucide:rocket', label: 'Rocket', icon: '🚀' },
  
  // Food & Drink
  { value: 'lucide:coffee', label: 'Coffee', icon: '☕' },
  { value: 'lucide:utensils', label: 'Utensils', icon: '🍴' },
  { value: 'lucide:wine', label: 'Wine', icon: '🍷' },
  { value: 'lucide:pizza', label: 'Pizza', icon: '🍕' },
  { value: 'lucide:cookie', label: 'Cookie', icon: '🍪' },
  { value: 'lucide:ice-cream', label: 'Ice Cream', icon: '🍦' },
  
  // Health & Medical
  { value: 'lucide:hospital', label: 'Hospital', icon: '🏥' },
  { value: 'lucide:stethoscope', label: 'Stethoscope', icon: '🩺' },
  { value: 'lucide:heart', label: 'Heart', icon: '❤️' },
  { value: 'lucide:heart-pulse', label: 'Heart Pulse', icon: '💓' },
  { value: 'lucide:pill', label: 'Pill', icon: '💊' },
  { value: 'lucide:thermometer', label: 'Thermometer', icon: '🌡️' },
  { value: 'lucide:bandage', label: 'Bandage', icon: '🩹' },
  { value: 'lucide:syringe', label: 'Syringe', icon: '💉' },
  
  // Education & Knowledge
  { value: 'lucide:book', label: 'Book', icon: '📖' },
  { value: 'lucide:book-open', label: 'Open Book', icon: '📚' },
  { value: 'lucide:graduation-cap', label: 'Graduation', icon: '🎓' },
  { value: 'lucide:lightbulb', label: 'Lightbulb', icon: '💡' },
  { value: 'lucide:search', label: 'Search', icon: '🔍' },
  { value: 'lucide:bookmark', label: 'Bookmark', icon: '🔖' },
  { value: 'lucide:flag', label: 'Flag', icon: '🚩' },
  
  // Nature & Weather
  { value: 'lucide:tree', label: 'Tree', icon: '🌳' },
  { value: 'lucide:leaf', label: 'Leaf', icon: '🍃' },
  { value: 'lucide:flower', label: 'Flower', icon: '🌸' },
  { value: 'lucide:droplet', label: 'Droplet', icon: '💧' },
  { value: 'lucide:flame', label: 'Flame', icon: '🔥' },
  { value: 'lucide:snowflake', label: 'Snowflake', icon: '❄️' },
  { value: 'lucide:wind', label: 'Wind', icon: '💨' },
  { value: 'lucide:cloud-rain', label: 'Rain', icon: '🌧️' },
  { value: 'lucide:cloud-snow', label: 'Snow', icon: '🌨️' },
  { value: 'lucide:cloud-lightning', label: 'Lightning', icon: '🌩️' },
  { value: 'lucide:umbrella', label: 'Umbrella', icon: '☂️' },
  { value: 'lucide:sun', label: 'Sun', icon: '☀️' },
  { value: 'lucide:moon', label: 'Moon', icon: '🌙' },
  { value: 'lucide:star', label: 'Star', icon: '⭐' },
  
  // Sports & Activities
  { value: 'lucide:target', label: 'Target', icon: '🎯' },
  { value: 'lucide:trophy', label: 'Trophy', icon: '🏆' },
  { value: 'lucide:dumbbell', label: 'Dumbbell', icon: '🏋️' },
  { value: 'lucide:basketball', label: 'Basketball', icon: '🏀' },
  { value: 'lucide:soccer', label: 'Soccer', icon: '⚽' },
  { value: 'lucide:sword', label: 'Sword', icon: '⚔️' },
  
  // Emotions & Reactions
  { value: 'lucide:smile', label: 'Smile', icon: '😊' },
  { value: 'lucide:frown', label: 'Frown', icon: '☹️' },
  { value: 'lucide:laugh', label: 'Laugh', icon: '😄' },
  { value: 'lucide:cry', label: 'Cry', icon: '😢' },
  { value: 'lucide:angry', label: 'Angry', icon: '😠' },
  { value: 'lucide:heart', label: 'Heart', icon: '❤️' },
  { value: 'lucide:thumbs-up', label: 'Thumbs Up', icon: '👍' },
  { value: 'lucide:thumbs-down', label: 'Thumbs Down', icon: '👎' },
  
  // Status & Indicators
  { value: 'lucide:bell', label: 'Bell', icon: '🔔' },
  { value: 'lucide:alert-circle', label: 'Alert', icon: '⚠️' },
  { value: 'lucide:check-circle', label: 'Check', icon: '✅' },
  { value: 'lucide:x-circle', label: 'X Circle', icon: '❌' },
  { value: 'lucide:info', label: 'Info', icon: 'ℹ️' },
  { value: 'lucide:help-circle', label: 'Help', icon: '❓' },
  { value: 'lucide:zap', label: 'Zap', icon: '⚡' },
  { value: 'lucide:star', label: 'Star', icon: '⭐' },
  { value: 'lucide:tag', label: 'Tag', icon: '🏷️' },
  { value: 'lucide:plus', label: 'Plus', icon: '➕' },
  { value: 'lucide:minus', label: 'Minus', icon: '➖' },
  { value: 'lucide:x', label: 'X', icon: '✖️' },
  { value: 'lucide:check', label: 'Check', icon: '✓' },
  
  // Arrows & Navigation
  { value: 'lucide:arrow-up', label: 'Arrow Up', icon: '⬆️' },
  { value: 'lucide:arrow-down', label: 'Arrow Down', icon: '⬇️' },
  { value: 'lucide:arrow-left', label: 'Arrow Left', icon: '⬅️' },
  { value: 'lucide:arrow-right', label: 'Arrow Right', icon: '➡️' },
  { value: 'lucide:chevron-up', label: 'Chevron Up', icon: '🔼' },
  { value: 'lucide:chevron-down', label: 'Chevron Down', icon: '🔽' },
  { value: 'lucide:chevron-left', label: 'Chevron Left', icon: '◀️' },
  { value: 'lucide:chevron-right', label: 'Chevron Right', icon: '▶️' },
  { value: 'lucide:move', label: 'Move', icon: '↔️' },
  { value: 'lucide:rotate-ccw', label: 'Rotate CCW', icon: '🔄' },
  { value: 'lucide:rotate-cw', label: 'Rotate CW', icon: '↪️' },
  
  // Special & Miscellaneous
  { value: 'lucide:puzzle', label: 'Puzzle', icon: '🧩' },
  { value: 'lucide:palette', label: 'Palette', icon: '🎨' },
  { value: 'lucide:music', label: 'Music', icon: '🎵' },
  { value: 'lucide:anchor', label: 'Anchor', icon: '⚓' },
  { value: 'lucide:crown', label: 'Crown', icon: '👑' },
  { value: 'lucide:ghost', label: 'Ghost', icon: '👻' },
  { value: 'lucide:bug', label: 'Bug', icon: '🐛' },
  { value: 'lucide:code', label: 'Code', icon: '</>' },
  { value: 'lucide:terminal', label: 'Terminal', icon: '>_' },
  { value: 'lucide:mouse-pointer', label: 'Mouse Pointer', icon: '🖱️' },
];

// Helper function to get icon by value
export const getIconByValue = (value) => {
  const icon = ICON_OPTIONS.find(option => option.value === value);
  return icon ? icon.icon : '📄'; // Default icon
};

// Helper function to get label by value
export const getLabelByValue = (value) => {
  const icon = ICON_OPTIONS.find(option => option.value === value);
  return icon ? icon.label : 'Unknown Icon';
};

// Categorized icons for organized dropdowns
export const CATEGORIZED_ICONS = {
  files: ICON_OPTIONS.filter(icon => icon.value.includes('folder') || icon.value.includes('file')),
  documents: ICON_OPTIONS.filter(icon => icon.value.includes('clipboard') || icon.value.includes('edit') || icon.value.includes('pen')),
  media: ICON_OPTIONS.filter(icon => icon.value.includes('music') || icon.value.includes('film') || icon.value.includes('camera')),
  communication: ICON_OPTIONS.filter(icon => icon.value.includes('message') || icon.value.includes('mail') || icon.value.includes('phone')),
  finance: ICON_OPTIONS.filter(icon => icon.value.includes('dollar') || icon.value.includes('credit') || icon.value.includes('shopping')),
  technology: ICON_OPTIONS.filter(icon => icon.value.includes('smartphone') || icon.value.includes('monitor') || icon.value.includes('server')),
  security: ICON_OPTIONS.filter(icon => icon.value.includes('lock') || icon.value.includes('shield') || icon.value.includes('key')),
  business: ICON_OPTIONS.filter(icon => icon.value.includes('building') || icon.value.includes('briefcase') || icon.value.includes('users')),
  nature: ICON_OPTIONS.filter(icon => icon.value.includes('tree') || icon.value.includes('leaf') || icon.value.includes('flower')),
  status: ICON_OPTIONS.filter(icon => icon.value.includes('alert') || icon.value.includes('check') || icon.value.includes('info')),
};