import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import {
  PackageIcon,
  LocationIcon as MapPinIcon,
  ZapIcon,
  TruckIcon,
  CheckIcon,
  AlertIcon,
  LoadingIcon,
  UserIcon,
  PhoneIcon,
  EmailIcon
} from '../components/icons';

type ShipmentPriority = 'STANDARD' | 'EXPRESS' | 'URGENT';

interface ShipmentFormData {
  origin: string;
  destination: string;
  weight: string;
  dimensions: string;
  priority: ShipmentPriority;
  recipientName: string;
  recipientPhone: string;
  recipientEmail: string;
}

const CITIES = [
  'Armenia',
  'Bogotá',
  'Medellín',
  'Cali',
  'Barranquilla',
  'Cartagena',
  'Pereira',
  'Manizales',
  'Bucaramanga',
  'Santa Marta'
];

export default function CreateShipment() {
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState<ShipmentFormData>({
    origin: '',
    destination: '',
    weight: '',
    dimensions: '',
    priority: 'STANDARD',
    recipientName: '',
    recipientPhone: '',
    recipientEmail: '',
  });
  
  const [quotedRate, setQuotedRate] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const calculateQuote = () => {
    if (!formData.origin || !formData.destination || !formData.weight) {
      setError('Por favor completa origen, destino y peso para cotizar');
      return;
    }

    // Cálculo simple de tarifa
    const weight = parseFloat(formData.weight);
    let baseRate = 15000; // Tarifa base

    // Agregar costo por peso
    baseRate += weight * 2000; // $2000 por kg

    // Multiplicador por prioridad
    const priorityMultipliers = {
      STANDARD: 1,
      EXPRESS: 1.5,
      URGENT: 2
    };
    const finalRate = baseRate * priorityMultipliers[formData.priority];

    setQuotedRate(Math.round(finalRate));
    setError('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!quotedRate) {
      setError('Por favor calcula la cotización primero');
      return;
    }

    setLoading(true);

    try {
      // TODO: Enviar al backend
      console.log('Crear envío:', formData, 'Tarifa:', quotedRate);
      
      setSuccess(true);
      setTimeout(() => {
        navigate('/dashboard');
      }, 2000);
    } catch (err: any) {
      setError(err.message || 'Error al crear el envío');
    } finally {
      setLoading(false);
    }
  };

  const priorityOptions = [
    { 
      value: 'STANDARD', 
      label: 'Estándar', 
      description: '3-5 días',
      Icon: PackageIcon,
      color: 'text-blue-600'
    },
    { 
      value: 'EXPRESS', 
      label: 'Express', 
      description: '1-2 días',
      Icon: TruckIcon,
      color: 'text-orange-600'
    },
    { 
      value: 'URGENT', 
      label: 'Urgente', 
      description: 'Mismo día',
      Icon: ZapIcon,
      color: 'text-red-600'
    },
  ];

  return (
    <Layout>
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-2">
            <div className="bg-purple-100 p-3 rounded-xl">
              <PackageIcon size={28} className="text-purple-600" strokeWidth={2} />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Cotizar Envío</h1>
              <p className="text-gray-600 mt-1">Complete el formulario y presione Calcular</p>
            </div>
          </div>
        </div>

        {/* Mensajes */}
        {error && (
          <div className="mb-6 flex items-center gap-3 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl">
            <AlertIcon size={20} className="flex-shrink-0" />
            <p className="text-sm">{error}</p>
          </div>
        )}

        {success && (
          <div className="mb-6 flex items-center gap-3 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-xl">
            <CheckIcon size={20} className="flex-shrink-0" />
            <div className="flex-1">
              <p className="text-sm font-medium">¡Envío creado exitosamente!</p>
              <p className="text-xs mt-1">Redirigiendo al dashboard...</p>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Formulario de datos del envío */}
          <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8">
            <div className="grid md:grid-cols-2 gap-6">
              {/* Origen */}
              <div>
                <label htmlFor="origin" className="block text-sm font-semibold text-gray-700 mb-2">
                  Origen:
                </label>
                <div className="relative">
                  <MapPinIcon size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <select
                    id="origin"
                    value={formData.origin}
                    onChange={(e) => {
                      setFormData({ ...formData, origin: e.target.value });
                      setQuotedRate(null);
                    }}
                    className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition appearance-none bg-white"
                    required
                  >
                    <option value="">Seleccione ciudad de origen</option>
                    {CITIES.map(city => (
                      <option key={city} value={city}>{city}</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Destino */}
              <div>
                <label htmlFor="destination" className="block text-sm font-semibold text-gray-700 mb-2">
                  Destino:
                </label>
                <div className="relative">
                  <MapPinIcon size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <select
                    id="destination"
                    value={formData.destination}
                    onChange={(e) => {
                      setFormData({ ...formData, destination: e.target.value });
                      setQuotedRate(null);
                    }}
                    className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition appearance-none bg-white"
                    required
                  >
                    <option value="">Seleccione ciudad de destino</option>
                    {CITIES.map(city => (
                      <option key={city} value={city}>{city}</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Peso */}
              <div>
                <label htmlFor="weight" className="block text-sm font-semibold text-gray-700 mb-2">
                  Peso (kg):
                </label>
                <div className="relative">
                  <PackageIcon size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="number"
                    id="weight"
                    value={formData.weight}
                    onChange={(e) => {
                      setFormData({ ...formData, weight: e.target.value });
                      setQuotedRate(null);
                    }}
                    className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                    placeholder="Ej: 2.5"
                    step="0.1"
                    min="0.1"
                    required
                  />
                </div>
              </div>

              {/* Dimensiones */}
              <div>
                <label htmlFor="dimensions" className="block text-sm font-semibold text-gray-700 mb-2">
                  Dimensiones (cm):
                </label>
                <div className="relative">
                  <PackageIcon size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    id="dimensions"
                    value={formData.dimensions}
                    onChange={(e) => setFormData({ ...formData, dimensions: e.target.value })}
                    className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition"
                    placeholder="Largo x Ancho x Alto"
                  />
                </div>
              </div>
            </div>

            {/* Prioridad */}
            <div className="mt-6">
              <label className="block text-sm font-semibold text-gray-700 mb-3">
                Prioridad:
              </label>
              <div className="grid grid-cols-3 gap-4">
                {priorityOptions.map((option) => {
                  const Icon = option.Icon;
                  return (
                    <button
                      key={option.value}
                      type="button"
                      onClick={() => {
                        setFormData({ ...formData, priority: option.value as ShipmentPriority });
                        setQuotedRate(null);
                      }}
                      className={`p-4 rounded-xl border-2 transition-all ${
                        formData.priority === option.value
                          ? 'border-purple-600 bg-purple-50 shadow-md'
                          : 'border-gray-200 hover:border-gray-300 hover:shadow-sm'
                      }`}
                    >
                      <Icon size={32} className={`mx-auto mb-2 ${option.color}`} strokeWidth={2} />
                      <div className="text-sm font-semibold text-gray-900">{option.label}</div>
                      <div className="text-xs text-gray-500 mt-1">{option.description}</div>
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Información del destinatario */}
            <div className="mt-8 pt-6 border-t border-gray-200">
              <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
                <UserIcon size={20} className="text-purple-600" />
                Información del Destinatario
              </h3>
              <div className="grid md:grid-cols-3 gap-4">
                <div>
                  <label htmlFor="recipientName" className="block text-sm font-medium text-gray-700 mb-2">
                    Nombre Completo
                  </label>
                  <div className="relative">
                    <UserIcon size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="text"
                      id="recipientName"
                      value={formData.recipientName}
                      onChange={(e) => setFormData({ ...formData, recipientName: e.target.value })}
                      className="w-full pl-10 pr-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition text-sm"
                      placeholder="Juan Pérez"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="recipientPhone" className="block text-sm font-medium text-gray-700 mb-2">
                    Teléfono
                  </label>
                  <div className="relative">
                    <PhoneIcon size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="tel"
                      id="recipientPhone"
                      value={formData.recipientPhone}
                      onChange={(e) => setFormData({ ...formData, recipientPhone: e.target.value })}
                      className="w-full pl-10 pr-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition text-sm"
                      placeholder="3001234567"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="recipientEmail" className="block text-sm font-medium text-gray-700 mb-2">
                    Email (opcional)
                  </label>
                  <div className="relative">
                    <EmailIcon size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="email"
                      id="recipientEmail"
                      value={formData.recipientEmail}
                      onChange={(e) => setFormData({ ...formData, recipientEmail: e.target.value })}
                      className="w-full pl-10 pr-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition text-sm"
                      placeholder="juan@email.com"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Separador */}
          <div className="border-t border-gray-200"></div>

          {/* Resultado de la cotización */}
          <div className="bg-gradient-to-br from-purple-50 to-indigo-50 rounded-2xl border-2 border-purple-200 p-8">
            <div className="text-center">
              {quotedRate !== null ? (
                <div className="space-y-4">
                  <CheckIcon size={48} className="mx-auto text-green-600" strokeWidth={2} />
                  <div>
                    <p className="text-sm text-gray-600 mb-2">Costo Total del Envío</p>
                    <p className="text-5xl font-bold text-purple-700">
                      ${quotedRate.toLocaleString('es-CO')}
                    </p>
                    <p className="text-sm text-gray-600 mt-3">
                      Prioridad: <span className="font-semibold text-gray-900">{formData.priority}</span>
                      {' • '}
                      {formData.origin} → {formData.destination}
                    </p>
                  </div>
                </div>
              ) : (
                <div className="space-y-3">
                  <PackageIcon size={48} className="mx-auto text-gray-400" strokeWidth={1.5} />
                  <p className="text-gray-600">Complete el formulario y presione Calcular</p>
                </div>
              )}
            </div>
          </div>

          {/* Botones de acción */}
          <div className="flex justify-center gap-4">
            <button
              type="button"
              onClick={calculateQuote}
              disabled={!formData.origin || !formData.destination || !formData.weight}
              className="flex items-center gap-2 px-8 py-3.5 bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white rounded-xl font-semibold transition-all shadow-lg"
            >
              <PackageIcon size={20} />
              Calcular
            </button>
            <button
              type="submit"
              disabled={loading || !quotedRate || success}
              className="flex items-center gap-2 px-8 py-3.5 bg-purple-600 hover:bg-purple-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white rounded-xl font-semibold transition-all shadow-lg"
            >
              {loading ? (
                <>
                  <LoadingIcon size={20} className="animate-spin" />
                  <span>Creando...</span>
                </>
              ) : (
                <>
                  <CheckIcon size={20} />
                  <span>Confirmar Envío</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </Layout>
  );
}
