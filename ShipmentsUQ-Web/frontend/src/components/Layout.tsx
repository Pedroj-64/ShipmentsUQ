import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useState } from 'react';
import {
  HomeIcon,
  PackageIcon,
  TruckIcon,
  CreditCardIcon,
  LocationIcon,
  UserIcon,
  LogoutIcon,
  MenuIcon,
  CloseIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
} from './icons';

interface LayoutProps {
  children: React.ReactNode;
}

interface MenuItem {
  Icon: React.ComponentType<any>;
  label: string;
  path: string;
}

export default function Layout({ children }: LayoutProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const menuItems: MenuItem[] = [
    {
      Icon: HomeIcon,
      label: 'Dashboard',
      path: '/dashboard'
    },
    {
      Icon: PackageIcon,
      label: 'Crear Envío',
      path: '/shipments/create'
    },
    {
      Icon: TruckIcon,
      label: 'Seguimiento',
      path: '/tracking'
    },
    {
      Icon: CreditCardIcon,
      label: 'Pagos',
      path: '/payments'
    },
    {
      Icon: LocationIcon,
      label: 'Direcciones',
      path: '/addresses'
    },
    {
      Icon: UserIcon,
      label: 'Mi Perfil',
      path: '/profile'
    },
  ];

  const isActive = (path: string) => location.pathname === path;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar Desktop */}
      <aside className={`${isSidebarCollapsed ? 'w-20' : 'w-72'} bg-white border-r border-gray-200 flex-col transition-all duration-300 shadow-sm hidden lg:flex`}>
        {/* Logo */}
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <div className={`flex items-center gap-3 ${isSidebarCollapsed ? 'justify-center w-full' : ''}`}>
              <div className="bg-gradient-to-br from-purple-600 to-indigo-700 p-2 rounded-xl">
                <PackageIcon size={24} className="text-white" strokeWidth={2.5} />
              </div>
              {!isSidebarCollapsed && (
                <div>
                  <h1 className="text-lg font-bold text-gray-900">ShipmentsUQ</h1>
                  <p className="text-xs text-gray-500">Gestión de Envíos</p>
                </div>
              )}
            </div>
            {!isSidebarCollapsed && (
              <button
                onClick={() => setIsSidebarCollapsed(true)}
                className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors"
              >
                <ChevronLeftIcon size={18} className="text-gray-500" />
              </button>
            )}
          </div>
          {isSidebarCollapsed && (
            <button
              onClick={() => setIsSidebarCollapsed(false)}
              className="mt-4 w-full p-1.5 hover:bg-gray-100 rounded-lg transition-colors flex justify-center"
            >
              <ChevronRightIcon size={18} className="text-gray-500" />
            </button>
          )}
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
          {menuItems.map((item) => {
            const active = isActive(item.path);
            const IconComponent = item.Icon;
            
            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 ${
                  active
                    ? 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-lg shadow-purple-500/30'
                    : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                } ${isSidebarCollapsed ? 'justify-center' : ''}`}
              >
                <IconComponent 
                  size={20} 
                  strokeWidth={active ? 2.5 : 2}
                  className="flex-shrink-0"
                />
                {!isSidebarCollapsed && (
                  <span className="font-medium text-sm">{item.label}</span>
                )}
                {active && !isSidebarCollapsed && (
                  <div className="ml-auto w-2 h-2 bg-white rounded-full animate-pulse" />
                )}
              </button>
            );
          })}
        </nav>

        {/* User Section */}
        <div className="p-4 border-t border-gray-200">
          {!isSidebarCollapsed ? (
            <div className="mb-3 p-3 bg-gradient-to-br from-purple-50 to-indigo-50 rounded-xl border border-purple-100">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-br from-purple-600 to-indigo-600 rounded-full flex items-center justify-center">
                  <UserIcon size={20} className="text-white" strokeWidth={2.5} />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-gray-900 truncate">{user?.name}</p>
                  <p className="text-xs text-gray-500 truncate">{user?.email}</p>
                </div>
              </div>
            </div>
          ) : (
            <div className="mb-3 flex justify-center">
              <div className="w-10 h-10 bg-gradient-to-br from-purple-600 to-indigo-600 rounded-full flex items-center justify-center">
                <UserIcon size={20} className="text-white" strokeWidth={2.5} />
              </div>
            </div>
          )}
          <button
            onClick={handleLogout}
            className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-red-600 hover:bg-red-50 transition-all duration-200 ${
              isSidebarCollapsed ? 'justify-center' : ''
            }`}
          >
            <LogoutIcon size={20} strokeWidth={2} />
            {!isSidebarCollapsed && (
              <span className="font-medium text-sm">Cerrar Sesión</span>
            )}
          </button>
        </div>
      </aside>

      {/* Mobile Header */}
      <div className="lg:hidden fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="flex items-center justify-between p-4">
          <div className="flex items-center gap-3">
            <div className="bg-gradient-to-br from-purple-600 to-indigo-700 p-2 rounded-xl">
              <PackageIcon size={20} className="text-white" strokeWidth={2.5} />
            </div>
            <div>
              <h1 className="text-base font-bold text-gray-900">ShipmentsUQ</h1>
              <p className="text-xs text-gray-500">Gestión de Envíos</p>
            </div>
          </div>
          <button
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            {isMobileMenuOpen ? (
              <CloseIcon size={24} className="text-gray-600" />
            ) : (
              <MenuIcon size={24} className="text-gray-600" />
            )}
          </button>
        </div>
      </div>

      {/* Mobile Menu */}
      {isMobileMenuOpen && (
        <div className="lg:hidden fixed inset-0 bg-black/50 z-40" onClick={() => setIsMobileMenuOpen(false)}>
          <div className="absolute top-16 left-0 right-0 bg-white border-b border-gray-200 shadow-xl" onClick={(e) => e.stopPropagation()}>
            <nav className="p-4 space-y-1">
              {menuItems.map((item) => {
                const active = isActive(item.path);
                const IconComponent = item.Icon;
                
                return (
                  <button
                    key={item.path}
                    onClick={() => {
                      navigate(item.path);
                      setIsMobileMenuOpen(false);
                    }}
                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 ${
                      active
                        ? 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-lg'
                        : 'text-gray-600 hover:bg-gray-50'
                    }`}
                  >
                    <IconComponent size={20} strokeWidth={active ? 2.5 : 2} />
                    <span className="font-medium text-sm">{item.label}</span>
                  </button>
                );
              })}
              <button
                onClick={() => {
                  handleLogout();
                  setIsMobileMenuOpen(false);
                }}
                className="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-red-600 hover:bg-red-50"
              >
                <LogoutIcon size={20} />
                <span className="font-medium text-sm">Cerrar Sesión</span>
              </button>
            </nav>
          </div>
        </div>
      )}

      {/* Main Content */}
      <main className="flex-1 overflow-y-auto">
        <div className="p-4 lg:p-8 pt-20 lg:pt-8">
          {children}
        </div>
      </main>
    </div>
  );
}
