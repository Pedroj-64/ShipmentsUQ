import { useState, type FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService, type RegisterRequest } from '../services/api';
import { useAuth } from '../context/AuthContext';
import {
  PackageIcon,
  UserIcon,
  TruckIcon,
  EmailIcon,
  PhoneIcon,
  LocationIcon,
  LockIcon,
  AlertIcon,
  LoadingIcon,
  CheckIcon,
  ChevronLeftIcon
} from '../components/icons';

export default function Register() {
  const [userType, setUserType] = useState<'USER' | 'DELIVERER'>('USER');
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    city: '',
    document: '', // Solo para repartidores
    zone: '', // Solo para repartidores
  });
  const [acceptedTerms, setAcceptedTerms] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');

    // Validaciones
    if (!acceptedTerms) {
      setError('Debes aceptar los términos y condiciones');
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      setError('Las contraseñas no coinciden');
      return;
    }

    if (formData.password.length < 8) {
      setError('La contraseña debe tener al menos 8 caracteres');
      return;
    }

    setLoading(true);

    try {
      const registerData: RegisterRequest = {
        name: formData.name,
        email: formData.email,
        phone: formData.phone,
        password: formData.password,
        city: formData.city,
      };

      const response = await authService.register(registerData);

      if (response.success) {
        login(response.user, response.userType);
        navigate('/dashboard');
      }
    } catch (err: any) {
      setError(err.response?.data?.error || 'Error al registrar usuario');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-white to-pink-50 flex items-center justify-center p-4">
      <div className="w-full max-w-6xl bg-white rounded-2xl shadow-2xl overflow-hidden flex">
        {/* Panel Izquierdo - Ilustración */}
        <div className="hidden lg:flex lg:w-1/3 bg-gradient-to-br from-purple-600 to-pink-600 p-8 flex-col justify-center items-center text-white">
          <PackageIcon size={120} className="mb-6 opacity-90" strokeWidth={1.5} />
          <h2 className="text-2xl font-bold mb-4 text-center">¡Únete a ShipmentsUQ!</h2>
          <p className="text-purple-100 text-center text-sm leading-relaxed">
            Gestiona tus envíos de forma rápida y segura. Conéctate con repartidores confiables y mantén el control total de tus paquetes.
          </p>
        </div>

        {/* Panel Central - Formulario */}
        <div className="flex-1 p-8 lg:p-12 overflow-y-auto max-h-screen">
          {/* Header */}
          <div className="mb-8">
            <button
              onClick={() => navigate('/login')}
              className="text-gray-500 hover:text-gray-700 mb-6 flex items-center text-sm transition-colors group"
            >
              <ChevronLeftIcon size={16} className="mr-1 group-hover:-translate-x-1 transition-transform" />
              Volver al inicio
            </button>
            <div className="flex items-center gap-3 mb-2">
              <PackageIcon size={32} className="text-purple-600" strokeWidth={2} />
              <h1 className="text-3xl font-bold text-gray-900">Registro en ShipmentsUQ</h1>
            </div>
            <p className="text-gray-600">Complete todos los campos para registrarse</p>
          </div>

        {/* Formulario */}
        <form onSubmit={handleSubmit} className="space-y-5">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
              {error}
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
                Nombre Completo
              </label>
              <input
                type="text"
                id="name"
                name="name"
                value={formData.name}
                onChange={handleChange}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                placeholder="Juan Pérez"
                required
              />
            </div>

            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                Email
              </label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                placeholder="juan@example.com"
                required
              />
            </div>

            <div>
              <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-2">
                Teléfono
              </label>
              <input
                type="tel"
                id="phone"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                placeholder="3001234567"
                required
              />
            </div>

            <div>
              <label htmlFor="city" className="block text-sm font-medium text-gray-700 mb-2">
                Ciudad
              </label>
              <input
                type="text"
                id="city"
                name="city"
                value={formData.city}
                onChange={handleChange}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                placeholder="Armenia"
                required
              />
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
                Contraseña
              </label>
              <input
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                placeholder="••••••••"
                required
                minLength={6}
              />
            </div>

            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-2">
                Confirmar Contraseña
              </label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                placeholder="••••••••"
                required
                minLength={6}
              />
            </div>
          </div>

          {/* Términos y condiciones */}
          <div className="bg-purple-50 border-l-4 border-purple-500 rounded-r-lg p-4 text-sm text-gray-700">
            <p className="font-medium mb-1">📋 Términos y Condiciones</p>
            <p className="text-gray-600">Al registrarte aceptas el tratamiento de tu información personal por parte de MargaDev-Society de acuerdo con nuestra política de privacidad.</p>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary w-full py-3 text-base shadow-lg shadow-purple-500/30 hover:shadow-xl hover:shadow-purple-500/40"
          >
            {loading ? (
              <span className="flex items-center justify-center">
                <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Registrando...
              </span>
            ) : 'Crear Cuenta'}
          </button>
        </form>

        {/* Enlaces */}
        <div className="mt-6 text-center">
          <p className="text-sm text-gray-600">
            ¿Ya tienes cuenta?{' '}
            <button
              onClick={() => navigate('/login')}
              className="text-purple-600 hover:text-purple-700 font-semibold"
            >
              Inicia sesión aquí
            </button>
          </p>
        </div>

        {/* Footer */}
        <div className="mt-6 pt-6 border-t border-gray-200 text-center text-sm text-gray-500">
          <p>© 2025 MargaDev-Society</p>
        </div>
      </div>
    </div>
  );
}
