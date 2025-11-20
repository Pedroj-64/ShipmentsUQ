/**
 * Sistema centralizado de íconos usando Lucide React
 * Todos los íconos de la aplicación se importan aquí para consistencia
 */

import {
  // Navegación y UI
  Package,
  PackageCheck,
  PackageSearch,
  PackagePlus,
  PackageX,
  Home,
  MapPin,
  CreditCard,
  User,
  Users,
  Settings,
  LogOut,
  Menu,
  X,
  ChevronLeft,
  ChevronRight,
  ChevronDown,
  ChevronUp,
  Search,
  Filter,
  Plus,
  Minus,
  Edit,
  Trash2,
  Save,
  Download,
  Upload,
  Eye,
  EyeOff,
  Calendar,
  Clock,
  Bell,
  Mail,
  Phone,
  
  // Estados de envío
  Truck,
  Plane,
  Ship,
  MapPinned,
  Route,
  Navigation,
  Compass,
  
  // Pagos y finanzas
  DollarSign,
  CreditCard as CardIcon,
  Wallet,
  Receipt,
  Banknote,
  CircleDollarSign,
  
  // Estadísticas y reportes
  TrendingUp,
  TrendingDown,
  BarChart3,
  PieChart,
  LineChart,
  Activity,
  
  // Acciones
  Check,
  CheckCircle,
  CheckCircle2,
  AlertCircle,
  AlertTriangle,
  Info,
  XCircle,
  HelpCircle,
  
  // Archivos y documentos
  FileText,
  File,
  Printer,
  Share2,
  
  // Utilidades
  Star,
  Heart,
  ThumbsUp,
  MessageSquare,
  Shield,
  Lock,
  Unlock,
  Key,
  Zap,
  Award,
  Target,
  Flag,
  Bookmark,
  
  // Configuración
  Sliders,
  Wrench,
  
  // Fecha y tiempo
  Timer,
  History,
  
  // Otros
  Building2,
  Store,
  Tag,
  Tags,
  Percent,
  Hash,
  AtSign,
  Link,
  ExternalLink,
  Copy,
  Clipboard,
  Image,
  Camera,
  Video,
  Mic,
  Volume2,
  VolumeX,
  Wifi,
  WifiOff,
  Battery,
  BatteryCharging,
  Power,
  RefreshCw,
  RotateCw,
  Loader2,
} from 'lucide-react';

// Exportar todos los íconos con nombres descriptivos
export {
  // Navegación
  Package as PackageIcon,
  PackageCheck as PackageDeliveredIcon,
  PackageSearch as PackageTrackingIcon,
  PackagePlus as PackageAddIcon,
  PackageX as PackageCancelIcon,
  Home as HomeIcon,
  MapPin as LocationIcon,
  CreditCard as PaymentIcon,
  User as UserIcon,
  Users as UsersIcon,
  Settings as SettingsIcon,
  LogOut as LogoutIcon,
  Menu as MenuIcon,
  X as CloseIcon,
  ChevronLeft as ChevronLeftIcon,
  ChevronRight as ChevronRightIcon,
  ChevronDown as ChevronDownIcon,
  ChevronUp as ChevronUpIcon,
  Search as SearchIcon,
  Filter as FilterIcon,
  Plus as PlusIcon,
  Minus as MinusIcon,
  Edit as EditIcon,
  Trash2 as DeleteIcon,
  Save as SaveIcon,
  Download as DownloadIcon,
  Upload as UploadIcon,
  Eye as EyeIcon,
  EyeOff as EyeOffIcon,
  Calendar as CalendarIcon,
  Clock as ClockIcon,
  Bell as NotificationIcon,
  Mail as EmailIcon,
  Phone as PhoneIcon,
  
  // Transporte y envíos
  Truck as TruckIcon,
  Plane as PlaneIcon,
  Ship as ShipIcon,
  MapPinned as MapPinnedIcon,
  Route as RouteIcon,
  Navigation as NavigationIcon,
  Compass as CompassIcon,
  
  // Finanzas
  DollarSign as MoneyIcon,
  CardIcon as CreditCardIcon,
  Wallet as WalletIcon,
  Receipt as ReceiptIcon,
  Banknote as BanknoteIcon,
  CircleDollarSign as CircleDollarIcon,
  
  // Gráficos
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  BarChart3 as BarChartIcon,
  PieChart as PieChartIcon,
  LineChart as LineChartIcon,
  Activity as ActivityIcon,
  
  // Estados
  Check as CheckIcon,
  CheckCircle as CheckCircleIcon,
  CheckCircle2 as CheckCircle2Icon,
  AlertCircle as AlertIcon,
  AlertTriangle as WarningIcon,
  Info as InfoIcon,
  XCircle as ErrorIcon,
  HelpCircle as HelpIcon,
  
  // Documentos
  FileText as DocumentIcon,
  File as FileIcon,
  Printer as PrintIcon,
  Share2 as ShareIcon,
  
  // Evaluación
  Star as StarIcon,
  Heart as HeartIcon,
  ThumbsUp as LikeIcon,
  MessageSquare as CommentIcon,
  Shield as ShieldIcon,
  Lock as LockIcon,
  Unlock as UnlockIcon,
  Key as KeyIcon,
  Zap as ZapIcon,
  Award as AwardIcon,
  Target as TargetIcon,
  Flag as FlagIcon,
  Bookmark as BookmarkIcon,
  
  // Configuración
  Sliders as SlidersIcon,
  Wrench as WrenchIcon,
  
  // Tiempo
  Timer as TimerIcon,
  History as HistoryIcon,
  
  // Empresas
  Building2 as BuildingIcon,
  Store as StoreIcon,
  Tag as TagIcon,
  Tags as TagsIcon,
  Percent as PercentIcon,
  Hash as HashIcon,
  AtSign as AtIcon,
  Link as LinkIcon,
  ExternalLink as ExternalLinkIcon,
  Copy as CopyIcon,
  Clipboard as ClipboardIcon,
  
  // Media
  Image as ImageIcon,
  Camera as CameraIcon,
  Video as VideoIcon,
  Mic as MicIcon,
  Volume2 as VolumeIcon,
  VolumeX as MuteIcon,
  
  // Sistema
  Wifi as WifiIcon,
  WifiOff as WifiOffIcon,
  Battery as BatteryIcon,
  BatteryCharging as BatteryChargingIcon,
  Power as PowerIcon,
  RefreshCw as RefreshIcon,
  RotateCw as RotateIcon,
  Loader2 as LoadingIcon,
};

// Configuración de tamaños estándar
export const iconSizes = {
  xs: 14,
  sm: 16,
  md: 20,
  lg: 24,
  xl: 28,
  '2xl': 32,
  '3xl': 40,
  '4xl': 48,
} as const;

// Tipo para los tamaños
export type IconSize = keyof typeof iconSizes;

// Props base para íconos
export interface IconProps {
  size?: IconSize | number;
  className?: string;
  strokeWidth?: number;
}

// Hook helper para obtener el tamaño
export const useIconSize = (size: IconSize | number = 'md'): number => {
  if (typeof size === 'number') return size;
  return iconSizes[size];
};
