/**
 * Componentes reutilizables para formularios
 * Simplifica la creación de formularios consistentes
 */

import { ReactNode } from 'react';
import { AlertIcon, CheckCircleIcon, InfoIcon, WarningIcon } from '../icons';

// Input con label y error
interface InputFieldProps {
  label: string;
  name: string;
  type?: string;
  value: string | number;
  onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void;
  placeholder?: string;
  error?: string;
  required?: boolean;
  disabled?: boolean;
  min?: number;
  max?: number;
  step?: number;
  icon?: ReactNode;
  helperText?: string;
  className?: string;
}

export function InputField({
  label,
  name,
  type = 'text',
  value,
  onChange,
  placeholder,
  error,
  required,
  disabled,
  min,
  max,
  step,
  icon,
  helperText,
  className = ''
}: InputFieldProps) {
  const inputClasses = `w-full px-4 py-2.5 border rounded-xl transition-all duration-200 ${
    icon ? 'pl-12' : ''
  } ${
    error
      ? 'border-red-300 focus:border-red-500 focus:ring-red-500/20'
      : 'border-gray-300 focus:border-purple-500 focus:ring-purple-500/20'
  } focus:ring-4 focus:outline-none ${disabled ? 'bg-gray-50 cursor-not-allowed' : 'bg-white'}`;

  return (
    <div className={`space-y-1.5 ${className}`}>
      <label htmlFor={name} className="block text-sm font-medium text-gray-700">
        {label}
        {required && <span className="text-red-500 ml-1">*</span>}
      </label>
      <div className="relative">
        {icon && (
          <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">
            {icon}
          </div>
        )}
        <input
          id={name}
          name={name}
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          required={required}
          disabled={disabled}
          min={min}
          max={max}
          step={step}
          className={inputClasses}
        />
      </div>
      {error && (
        <p className="text-sm text-red-600 flex items-center gap-1.5">
          <AlertIcon size={14} />
          {error}
        </p>
      )}
      {helperText && !error && (
        <p className="text-sm text-gray-500">{helperText}</p>
      )}
    </div>
  );
}

// TextArea con label y error
interface TextAreaFieldProps {
  label: string;
  name: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
  placeholder?: string;
  error?: string;
  required?: boolean;
  disabled?: boolean;
  rows?: number;
  helperText?: string;
  className?: string;
}

export function TextAreaField({
  label,
  name,
  value,
  onChange,
  placeholder,
  error,
  required,
  disabled,
  rows = 3,
  helperText,
  className = ''
}: TextAreaFieldProps) {
  return (
    <div className={`space-y-1.5 ${className}`}>
      <label htmlFor={name} className="block text-sm font-medium text-gray-700">
        {label}
        {required && <span className="text-red-500 ml-1">*</span>}
      </label>
      <textarea
        id={name}
        name={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        disabled={disabled}
        rows={rows}
        className={`w-full px-4 py-2.5 border rounded-xl transition-all duration-200 ${
          error
            ? 'border-red-300 focus:border-red-500 focus:ring-red-500/20'
            : 'border-gray-300 focus:border-purple-500 focus:ring-purple-500/20'
        } focus:ring-4 focus:outline-none resize-none ${
          disabled ? 'bg-gray-50 cursor-not-allowed' : 'bg-white'
        }`}
      />
      {error && (
        <p className="text-sm text-red-600 flex items-center gap-1.5">
          <AlertIcon size={14} />
          {error}
        </p>
      )}
      {helperText && !error && (
        <p className="text-sm text-gray-500">{helperText}</p>
      )}
    </div>
  );
}

// Select con label y error
interface SelectFieldProps {
  label: string;
  name: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
  options: { value: string; label: string }[];
  error?: string;
  required?: boolean;
  disabled?: boolean;
  placeholder?: string;
  icon?: ReactNode;
  helperText?: string;
  className?: string;
}

export function SelectField({
  label,
  name,
  value,
  onChange,
  options,
  error,
  required,
  disabled,
  placeholder,
  icon,
  helperText,
  className = ''
}: SelectFieldProps) {
  return (
    <div className={`space-y-1.5 ${className}`}>
      <label htmlFor={name} className="block text-sm font-medium text-gray-700">
        {label}
        {required && <span className="text-red-500 ml-1">*</span>}
      </label>
      <div className="relative">
        {icon && (
          <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 z-10">
            {icon}
          </div>
        )}
        <select
          id={name}
          name={name}
          value={value}
          onChange={onChange}
          required={required}
          disabled={disabled}
          className={`w-full px-4 py-2.5 border rounded-xl transition-all duration-200 appearance-none ${
            icon ? 'pl-12' : ''
          } ${
            error
              ? 'border-red-300 focus:border-red-500 focus:ring-red-500/20'
              : 'border-gray-300 focus:border-purple-500 focus:ring-purple-500/20'
          } focus:ring-4 focus:outline-none ${
            disabled ? 'bg-gray-50 cursor-not-allowed' : 'bg-white'
          }`}
        >
          {placeholder && <option value="">{placeholder}</option>}
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none">
          <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </div>
      </div>
      {error && (
        <p className="text-sm text-red-600 flex items-center gap-1.5">
          <AlertIcon size={14} />
          {error}
        </p>
      )}
      {helperText && !error && (
        <p className="text-sm text-gray-500">{helperText}</p>
      )}
    </div>
  );
}

// Alert Component
interface AlertProps {
  type: 'success' | 'error' | 'warning' | 'info';
  title?: string;
  message: string;
  onClose?: () => void;
  className?: string;
}

export function Alert({ type, title, message, onClose, className = '' }: AlertProps) {
  const configs = {
    success: {
      bg: 'bg-green-50',
      border: 'border-green-200',
      text: 'text-green-800',
      icon: CheckCircleIcon,
      iconColor: 'text-green-600'
    },
    error: {
      bg: 'bg-red-50',
      border: 'border-red-200',
      text: 'text-red-800',
      icon: AlertIcon,
      iconColor: 'text-red-600'
    },
    warning: {
      bg: 'bg-yellow-50',
      border: 'border-yellow-200',
      text: 'text-yellow-800',
      icon: WarningIcon,
      iconColor: 'text-yellow-600'
    },
    info: {
      bg: 'bg-blue-50',
      border: 'border-blue-200',
      text: 'text-blue-800',
      icon: InfoIcon,
      iconColor: 'text-blue-600'
    }
  };

  const config = configs[type];
  const IconComponent = config.icon;

  return (
    <div className={`${config.bg} ${config.border} border rounded-xl p-4 ${className}`}>
      <div className="flex items-start gap-3">
        <IconComponent size={20} className={`${config.iconColor} flex-shrink-0 mt-0.5`} />
        <div className="flex-1 min-w-0">
          {title && (
            <h4 className={`font-semibold ${config.text} mb-1`}>{title}</h4>
          )}
          <p className={`text-sm ${config.text}`}>{message}</p>
        </div>
        {onClose && (
          <button
            onClick={onClose}
            className={`${config.text} hover:opacity-70 transition-opacity flex-shrink-0`}
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        )}
      </div>
    </div>
  );
}

// Card Component
interface CardProps {
  children: ReactNode;
  title?: string;
  subtitle?: string;
  icon?: ReactNode;
  footer?: ReactNode;
  className?: string;
  hover?: boolean;
}

export function Card({ children, title, subtitle, icon, footer, className = '', hover = false }: CardProps) {
  return (
    <div className={`bg-white rounded-2xl border border-gray-200 shadow-sm ${hover ? 'hover:shadow-lg hover:border-purple-300 transition-all duration-200' : ''} ${className}`}>
      {(title || subtitle || icon) && (
        <div className="p-6 border-b border-gray-100">
          <div className="flex items-center gap-3">
            {icon && (
              <div className="bg-purple-50 p-3 rounded-xl">
                {icon}
              </div>
            )}
            <div>
              {title && <h3 className="text-lg font-semibold text-gray-900">{title}</h3>}
              {subtitle && <p className="text-sm text-gray-500 mt-0.5">{subtitle}</p>}
            </div>
          </div>
        </div>
      )}
      <div className="p-6">
        {children}
      </div>
      {footer && (
        <div className="p-6 border-t border-gray-100 bg-gray-50 rounded-b-2xl">
          {footer}
        </div>
      )}
    </div>
  );
}
