import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
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
  ChevronLeftIcon,
  FileIcon
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

          {/* Selección de tipo de usuario */}
          <div className="mb-6">
            <label className="block text-sm font-semibold text-gray-700 mb-3">
              Tipo de registro:
            </label>
            <div className="flex gap-4">
              <button
                type="button"
                onClick={() => setUserType('USER')}
                className={`flex-1 flex items-center justify-center gap-3 px-6 py-4 rounded-xl border-2 transition-all ${
                  userType === 'USER'
                    ? 'border-purple-600 bg-purple-50 text-purple-700'
                    : 'border-gray-200 hover:border-purple-300 text-gray-600'
                }`}
              >
                <UserIcon size={24} strokeWidth={2} />
                <span className="font-medium">Usuario</span>
              </button>
              <button
                type="button"
                onClick={() => setUserType('DELIVERER')}
                className={`flex-1 flex items-center justify-center gap-3 px-6 py-4 rounded-xl border-2 transition-all ${
                  userType === 'DELIVERER'
                    ? 'border-purple-600 bg-purple-50 text-purple-700'
                    : 'border-gray-200 hover:border-purple-300 text-gray-600'
                }`}
              >
                <TruckIcon size={24} strokeWidth={2} />
                <span className="font-medium">Repartidor</span>
              </button>
            </div>
          </div>

          {/* Formulario */}
          <form onSubmit={handleSubmit} className="space-y-5">
            {error && (
              <div className="flex items-center gap-3 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl">
                <AlertIcon size={20} className="flex-shrink-0" />
                <p className="text-sm">{error}</p>
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
              {/* Nombre completo */}
              <div>
                <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
                  Nombre completo:
                </label>
                <div className="relative">
                  <UserIcon size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    id="name"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                    placeholder="Ingrese su nombre completo"
                    required
                  />
                </div>
              </div>

              {/* Email */}
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                  Correo electrónico:
                </label>
                <div className="relative">
                  <EmailIcon size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="email"
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                    placeholder="ejemplo@correo.com"
                    required
                  />
                </div>
              </div>

              {/* Teléfono */}
              <div>
                <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-2">
                  Teléfono:
                </label>
                <div className="relative">
                  <PhoneIcon size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="tel"
                    id="phone"
                    name="phone"
                    value={formData.phone}
                    onChange={handleChange}
                    className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                    placeholder="+57 300 123 4567"
                    required
                  />
                </div>
              </div>

              {/* Campos solo para repartidores */}
              {userType === 'DELIVERER' && (
                <>
                  {/* Documento */}
                  <div>
                    <label htmlFor="document" className="block text-sm font-medium text-gray-700 mb-2">
                      Documento:
                    </label>
                    <div className="relative">
                      <FileIcon size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                      <input
                        type="text"
                        id="document"
                        name="document"
                        value={formData.document}
                        onChange={handleChange}
                        className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                        placeholder="Número de documento"
                        required={userType === 'DELIVERER'}
                      />
                    </div>
                  </div>

                  {/* Zona de trabajo */}
                  <div>
                    <label htmlFor="zone" className="block text-sm font-medium text-gray-700 mb-2">
                      Zona de trabajo:
                    </label>
                    <div className="relative">
                      <LocationIcon size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                      <input
                        type="text"
                        id="zone"
                        name="zone"
                        value={formData.zone}
                        onChange={handleChange}
                        className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                        placeholder="Ej: Norte, Sur, Centro"
                        required={userType === 'DELIVERER'}
                      />
                    </div>
                  </div>
                </>
              )}

              {/* Contraseña */}
              <div>
                <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
                  Contraseña:
                </label>
                <div className="relative">
                  <LockIcon size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="password"
                    id="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                    placeholder="Mínimo 8 caracteres"
                    required
                    minLength={8}
                  />
                </div>
              </div>

              {/* Confirmar contraseña */}
              <div>
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-2">
                  Confirmar contraseña:
                </label>
                <div className="relative">
                  <LockIcon size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                    placeholder="Repita su contraseña"
                    required
                    minLength={8}
                  />
                </div>
              </div>

              {/* Ciudad */}
              <div className="md:col-span-2">
                <label htmlFor="city" className="block text-sm font-medium text-gray-700 mb-2">
                  Ciudad:
                </label>
                <div className="relative">
                  <LocationIcon size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    id="city"
                    name="city"
                    value={formData.city}
                    onChange={handleChange}
                    className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                    placeholder="Ciudad de residencia"
                    required
                  />
                </div>
              </div>
            </div>

            {/* Términos y condiciones */}
            <div className="flex items-start gap-3 p-4 bg-purple-50 border border-purple-200 rounded-xl">
              <input
                type="checkbox"
                id="terms"
                checked={acceptedTerms}
                onChange={(e) => setAcceptedTerms(e.target.checked)}
                className="mt-1 w-5 h-5 text-purple-600 border-gray-300 rounded focus:ring-purple-500"
              />
              <label htmlFor="terms" className="text-sm text-gray-700 leading-relaxed">
                He leído y acepto los{' '}
                <span className="font-semibold text-purple-700">términos y condiciones</span> de tratamiento de datos personales.
              </label>
            </div>

            {/* Botones */}
            <div className="flex gap-4 pt-4">
              <button
                type="submit"
                disabled={loading || !acceptedTerms}
                className="flex-1 flex items-center justify-center gap-2 bg-purple-600 hover:bg-purple-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white py-3.5 rounded-xl font-semibold transition-all shadow-lg shadow-purple-500/30 hover:shadow-xl hover:shadow-purple-500/40"
              >
                {loading ? (
                  <>
                    <LoadingIcon size={20} className="animate-spin" />
                    <span>Registrando...</span>
                  </>
                ) : (
                  <>
                    <CheckIcon size={20} />
                    <span>Registrar</span>
                  </>
                )}
              </button>
              <button
                type="button"
                onClick={() => navigate('/login')}
                disabled={loading}
                className="px-8 py-3.5 border-2 border-gray-300 hover:border-gray-400 text-gray-700 rounded-xl font-semibold transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Cancelar
              </button>
            </div>
          </form>

          {/* Link a login */}
          <div className="mt-8 text-center">
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
        </div>

        {/* Panel Derecho - Términos completos */}
        <div className="hidden xl:flex xl:w-1/4 bg-gray-50 p-6 flex-col border-l border-gray-200">
          <div className="flex items-center gap-2 mb-4">
            <FileIcon size={20} className="text-purple-600" />
            <h3 className="text-lg font-bold text-gray-900">Términos y Condiciones</h3>
          </div>
          <p className="text-sm font-semibold text-purple-700 mb-3">MargaDev-Society</p>
          <div className="text-xs text-gray-600 leading-relaxed space-y-3 overflow-y-auto">
            <p>
              Al registrarte, aceptas que <strong>MargaDev-Society</strong> recopile y procese tus datos personales (nombre, correo, teléfono y ciudad) para la prestación del servicio de envíos.
            </p>
            <p>
              Tus datos serán tratados de forma confidencial, utilizados únicamente para gestionar tus pedidos y mejorar nuestro servicio.
            </p>
            <p>
              No compartiremos tu información con terceros sin tu consentimiento, excepto cuando sea requerido por ley.
            </p>
            <p>
              Tienes derecho a acceder, actualizar o eliminar tus datos en cualquier momento contactándonos.
            </p>
          </div>
          <div className="mt-4 pt-4 border-t border-gray-200 text-center">
            <p className="text-xs text-gray-500">© 2025 MargaDev-Society</p>
          </div>
        </div>
      </div>
    </div>
  );
}
