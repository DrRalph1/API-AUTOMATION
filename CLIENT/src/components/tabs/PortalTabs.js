// components/auth/PortalTabs.js
import React from "react";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Terminal, Settings } from "lucide-react";

const PortalTabs = ({ userType, setUserType, isMobile, isDark, colors }) => {
  const getPortalColors = (portalType) => {
    if (portalType === 'admin') {
      return {
        color: isDark ? "from-purple-500 to-pink-400" : "from-purple-600 to-pink-500",
      };
    }
    return {
      color: isDark ? "from-blue-500 to-cyan-400" : "from-blue-600 to-cyan-500",
    };
  };

  const tellerPortal = getPortalColors('teller');
  const adminPortal = getPortalColors('admin');

  return (
    <div className={`${isMobile ? 'mb-4' : 'mb-8'}`}>
      <Tabs value={userType} onValueChange={setUserType} className="w-full">
        <TabsList className={`grid grid-cols-2 ${isDark ? 'bg-gray-800/50' : 'bg-gray-100'} backdrop-blur-sm border ${colors.cardBorder} p-1 ${isMobile ? 'rounded-lg' : 'rounded-xl'}`}>
          <TabsTrigger
            value="teller"
            className={`${isMobile ? 'rounded-md text-xs py-2' : 'rounded-lg py-2.5'} transition-all duration-200 data-[state=active]:bg-gradient-to-br ${tellerPortal.color} data-[state=active]:text-white`}
          >
            <Terminal className={`${isMobile ? 'h-3 w-3 mr-1' : 'h-4 w-4 mr-2'}`} />
            {isMobile ? "Teller" : "Teller Portal"}
          </TabsTrigger>
          <TabsTrigger
            value="oracle"
            className={`${isMobile ? 'rounded-md text-xs py-2' : 'rounded-lg py-2.5'} transition-all duration-200 data-[state=active]:bg-gradient-to-br ${adminPortal.color} data-[state=active]:text-white`}
          >
            <Settings className={`${isMobile ? 'h-3 w-3 mr-1' : 'h-4 w-4 mr-2'}`} />
            {isMobile ? "oracle" : "Admin Portal"}
          </TabsTrigger>
        </TabsList>
      </Tabs>
    </div>
  );
};

export default PortalTabs;