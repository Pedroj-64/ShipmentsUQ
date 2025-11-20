import React, { createContext, useContext, useState, useEffect } from 'react';
import type { User } from '../services/api';

interface AuthContextType {
  user: User | null;
  userType: 'USER' | 'DELIVERER' | 'ADMIN' | null;
  login: (user: User, type: 'USER' | 'DELIVERER' | 'ADMIN') => void;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [userType, setUserType] = useState<'USER' | 'DELIVERER' | 'ADMIN' | null>(null);

  // Cargar usuario desde localStorage al iniciar
  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    const savedUserType = localStorage.getItem('userType');
    
    if (savedUser && savedUserType) {
      setUser(JSON.parse(savedUser));
      setUserType(savedUserType as 'USER' | 'DELIVERER' | 'ADMIN');
    }
  }, []);

  const login = (userData: User, type: 'USER' | 'DELIVERER' | 'ADMIN') => {
    setUser(userData);
    setUserType(type);
    localStorage.setItem('user', JSON.stringify(userData));
    localStorage.setItem('userType', type);
  };

  const logout = () => {
    setUser(null);
    setUserType(null);
    localStorage.removeItem('user');
    localStorage.removeItem('userType');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        userType,
        login,
        logout,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
