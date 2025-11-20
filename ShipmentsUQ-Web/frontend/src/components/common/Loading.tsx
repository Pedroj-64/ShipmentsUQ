/**
 * Componentes de Loading reutilizables
 */

import { LoadingIcon } from '../icons';

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
}

export function LoadingSpinner({ size = 'md', className = '' }: LoadingSpinnerProps) {
  const sizes = {
    sm: 16,
    md: 24,
    lg: 32,
    xl: 48
  };

  return (
    <LoadingIcon
      size={sizes[size]}
      className={`text-purple-600 animate-spin ${className}`}
      strokeWidth={2.5}
    />
  );
}

interface LoadingOverlayProps {
  message?: string;
  fullScreen?: boolean;
}

export function LoadingOverlay({ message = 'Cargando...', fullScreen = false }: LoadingOverlayProps) {
  return (
    <div className={`flex flex-col items-center justify-center ${fullScreen ? 'fixed inset-0 bg-white/80 backdrop-blur-sm z-50' : 'py-12'}`}>
      <LoadingSpinner size="xl" />
      <p className="text-gray-600 mt-4 font-medium">{message}</p>
    </div>
  );
}

interface LoadingCardProps {
  count?: number;
}

export function LoadingCard({ count = 1 }: LoadingCardProps) {
  return (
    <>
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="card animate-pulse">
          <div className="h-4 bg-gray-200 rounded w-3/4 mb-4"></div>
          <div className="h-4 bg-gray-200 rounded w-1/2 mb-2"></div>
          <div className="h-4 bg-gray-200 rounded w-5/6"></div>
        </div>
      ))}
    </>
  );
}
