// utils/iconUtils.js
import {
  Globe,
  Heart,
  Building,
  DollarSign,
  Zap,
  Folder,
  Smartphone,
  Car,
  Wifi,
  GraduationCap,
  Shield,
  Droplets,
  Banknote,
  Wallet,
  Coins,
  Bitcoin,
  Server,
  Network,
  Cloud,
  Radio,
  Satellite,
  Bluetooth,
  Wind,
  Sun,
  Moon,
  CloudRain,
  Thermometer,
  Briefcase
} from "lucide-react";

export const getIconComponent = (iconString) => {
  if (!iconString) return Globe;
  
  const iconName = iconString.split(':').pop();
  
  switch (iconName.toLowerCase()) {
    case 'heart': return Heart;
    case 'building': return Building;
    case 'dollar-sign': return DollarSign;
    case 'zap': return Zap;
    case 'folder': return Folder;
    case 'smartphone': return Smartphone;
    case 'car': return Car;
    case 'wifi': return Wifi;
    case 'graduation-cap': return GraduationCap;
    case 'shield': return Shield;
    case 'droplets': return Droplets;
    case 'banknote': return Banknote;
    case 'wallet': return Wallet;
    case 'coins': return Coins;
    case 'bitcoin': return Bitcoin;
    case 'server': return Server;
    case 'network': return Network;
    case 'cloud': return Cloud;
    case 'radio': return Radio;
    case 'satellite': return Satellite;
    case 'bluetooth': return Bluetooth;
    case 'globe': return Globe;
    case 'wind': return Wind;
    case 'sun': return Sun;
    case 'moon': return Moon;
    case 'cloud-rain': return CloudRain;
    case 'thermometer': return Thermometer;
    case 'briefcase': return Briefcase;
    default: return Globe;
  }
};