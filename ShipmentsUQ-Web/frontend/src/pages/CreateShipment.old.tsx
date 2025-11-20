import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import AddressAutocomplete from '../components/AddressAutocomplete';

type ShipmentPriority = 'STANDARD' | 'EXPRESS' | 'URGENT';

interface ShipmentFormData {
  originAddress: string;
  originLat?: number;
  originLon?: number;
  destinationAddress: string;
  destinationLat?: number;
  destinationLon?: number;
  weight: string;
  dimensions: string;
  priority: ShipmentPriority;
  specialInstructions: string;
  recipientName: string;
  recipientPhone: string;
  recipientEmail: string;
}

export default function CreateShipment() {
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState<ShipmentFormData>({
    originAddress: '',
    destinationAddress: '',
    weight: '',
    dimensions: '',
    priority: 'STANDARD',
    specialInstructions: '',
    recipientName: '',
    recipientPhone: '',
    recipientEmail: '',
  });
  
  const [quotedRate, setQuotedRate] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const calculateQuote = () => {
    if (!formData.originLat || !formData.destinationLat || !formData.weight) {
      setError('Por favor completa origen, destino y peso para cotizar');
      return;
    }

    // Calcular distancia usando fórmula de Haversine
    const R = 6371; // Radio de la Tierra en km
    const dLat = (formData.destinationLat - formData.originLat) * Math.PI / 180;
    const dLon = ((formData.destinationLon || 0) - (formData.originLon || 0)) * Math.PI / 180;
    const a = 
      Math.sin(dLat/2) * Math.sin(dLat/2) +
      Math.cos(formData.originLat * Math.PI / 180) * Math.cos(formData.destinationLat * Math.PI / 180) *
      Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    const distance = R * c;

    // Calcular tarifa base
    const weight = parseFloat(formData.weight);
    let baseRate = distance * 2.5; // $2.5 por km
    baseRate += weight * 1.5; // $1.5 por kg

    // Aplicar multiplicador por prioridad
    const priorityMultipliers = {
      STANDARD: 1,
      EXPRESS: 1.5,
      URGENT: 2
    };
    const finalRate = baseRate * priorityMultipliers[formData.priority];

    setQuotedRate(Math.round(finalRate * 100) / 100);
    setError('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (!formData.originLat || !formData.destinationLat) {
        throw new Error('Por favor selecciona las ubicaciones en el mapa');
      }

      if (!quotedRate) {
        throw new Error('Por favor calcula la cotización primero');
      }

      // TODO: Enviar al backend
      console.log('Crear envío:', formData);
      
      setSuccess(true);
      setTimeout(() => {
        navigate('/tracking');
      }, 2000);
    } catch (err: any) {
      setError(err.message || 'Error al crear el envío');
    } finally {
      setLoading(false);
    }
  };

  const handleOriginChange = (address: string, lat?: number, lon?: number) => {
    setFormData(prev => ({
      ...prev,
      originAddress: address,
      originLat: lat,
      originLon: lon
    }));
    setQuotedRate(null);
  };

  const handleDestinationChange = (address: string, lat?: number, lon?: number) => {
    setFormData(prev => ({
      ...prev,
      destinationAddress: address,
      destinationLat: lat,
      destinationLon: lon
    }));
    setQuotedRate(null);
  };

  const priorityOptions = [
    { 
      value: 'STANDARD', 
      label: 'Estándar', 
      description: '3-5 días',
      icon: '📦',
    },
    { 
      value: 'EXPRESS', 
      label: 'Express', 
      description: '1-2 días',
      icon: '⚡',
    },
    { 
      value: 'URGENT', 
      label: 'Urgente', 
      description: 'Mismo día',
      icon: '🚀',
    },
  ];

  return (
    <Layout>
      <div className="max-w-5xl">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-3">
            <div className="bg-purple-100 p-3 rounded-xl">
              <svg className="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Crear Nuevo Envío</h1>
              <p className="text-gray-600 mt-1">Completa los detalles y obtén una cotización instantánea</p>
            </div>
          </div>
        </div>

        {/* Mensajes */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 rounded-xl p-4 flex items-start gap-3">
            <svg className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
            <p className="text-sm text-red-700 flex-1">{error}</p>
          </div>
        )}

        {success && (
          <div className="mb-6 bg-green-50 border border-green-200 rounded-xl p-4 flex items-start gap-3">
            <svg className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
            </svg>
            <div className="flex-1">
              <p className="text-sm font-medium text-green-800">¡Envío creado exitosamente!</p>
              <p className="text-xs text-green-700 mt-1">Redirigiendo...</p>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Ubicaciones */}
          <div className="card">
            <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center gap-2">
              <svg className="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              Ubicaciones
            </h2>
            
            <div className="grid md:grid-cols-2 gap-6">
              <AddressAutocomplete
                value={formData.originAddress}
                onChange={handleOriginChange}
                label="Dirección de Origen"
                placeholder="Buscar o hacer click en el mapa..."
                required
                showMap={true}
              />

              <AddressAutocomplete
                value={formData.destinationAddress}
                onChange={handleDestinationChange}
                label="Dirección de Destino"
                placeholder="Buscar o hacer click en el mapa..."
                required
                showMap={true}
              />
            </div>
          </div>

          {/* Detalles del Paquete */}
          <div className="card">
            <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center gap-2">
              <svg className="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
              Detalles del Paquete
            </h2>
            
            <div className="grid md:grid-cols-2 gap-6 mb-6">
              <div>
                <label htmlFor="weight" className="label">
                  Peso (kg) <span className="text-red-500">*</span>
                </label>
                <input
                  type="number"
                  id="weight"
                  value={formData.weight}
                  onChange={(e) => {
                    setFormData({ ...formData, weight: e.target.value });
                    setQuotedRate(null);
                  }}
                  className="input"
                  placeholder="Ej: 2.5"
                  step="0.1"
                  min="0.1"
                  required
                />
              </div>

              <div>
                <label htmlFor="dimensions" className="label">
                  Dimensiones (cm)
                </label>
                <input
                  type="text"
                  id="dimensions"
                  value={formData.dimensions}
                  onChange={(e) => setFormData({ ...formData, dimensions: e.target.value })}
                  className="input"
                  placeholder="Ej: 30x20x15"
                />
              </div>
            </div>

            <div className="mb-6">
              <label className="label mb-3">
                Prioridad <span className="text-red-500">*</span>
              </label>
              <div className="grid grid-cols-3 gap-3">
                {priorityOptions.map((option) => (
                  <button
                    key={option.value}
                    type="button"
                    onClick={() => {
                      setFormData({ ...formData, priority: option.value as ShipmentPriority });
                      setQuotedRate(null);
                    }}
                    className={`p-4 rounded-xl border-2 transition-all text-center ${
                      formData.priority === option.value
                        ? 'border-purple-600 bg-purple-50 shadow-sm'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <div className="text-2xl mb-2">{option.icon}</div>
                    <div className="text-sm font-semibold text-gray-900">{option.label}</div>
                    <div className="text-xs text-gray-500 mt-1">{option.description}</div>
                  </button>
                ))}
              </div>
            </div>

            <div>
              <label htmlFor="specialInstructions" className="label">
                Instrucciones Especiales
              </label>
              <textarea
                id="specialInstructions"
                value={formData.specialInstructions}
                onChange={(e) => setFormData({ ...formData, specialInstructions: e.target.value })}
                className="input"
                rows={3}
                placeholder="Ej: Frágil, manejar con cuidado..."
              />
            </div>
          </div>

          {/* Información del Destinatario */}
          <div className="card">
            <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center gap-2">
              <svg className="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              Información del Destinatario
            </h2>
            
            <div className="grid md:grid-cols-3 gap-6">
              <div>
                <label htmlFor="recipientName" className="label">
                  Nombre Completo <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  id="recipientName"
                  value={formData.recipientName}
                  onChange={(e) => setFormData({ ...formData, recipientName: e.target.value })}
                  className="input"
                  placeholder="Ej: Juan Pérez"
                  required
                />
              </div>

              <div>
                <label htmlFor="recipientPhone" className="label">
                  Teléfono <span className="text-red-500">*</span>
                </label>
                <input
                  type="tel"
                  id="recipientPhone"
                  value={formData.recipientPhone}
                  onChange={(e) => setFormData({ ...formData, recipientPhone: e.target.value })}
                  className="input"
                  placeholder="Ej: 3001234567"
                  required
                />
              </div>

              <div>
                <label htmlFor="recipientEmail" className="label">
                  Email
                </label>
                <input
                  type="email"
                  id="recipientEmail"
                  value={formData.recipientEmail}
                  onChange={(e) => setFormData({ ...formData, recipientEmail: e.target.value })}
                  className="input"
                  placeholder="Ej: juan@email.com"
                />
              </div>
            </div>
          </div>

          {/* Cotización */}
          <div className="card bg-gradient-to-br from-purple-50 to-indigo-50 border-purple-200">
            <div className="flex items-start justify-between mb-6">
              <div>
                <h2 className="text-xl font-semibold text-gray-900 flex items-center gap-2">
                  <svg className="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Cotización
                </h2>
                <p className="text-sm text-gray-600 mt-1">Calcula el costo estimado</p>
              </div>
              
              <button
                type="button"
                onClick={calculateQuote}
                disabled={!formData.originLat || !formData.destinationLat || !formData.weight}
                className="btn btn-secondary"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                </svg>
                Calcular
              </button>
            </div>

            {quotedRate !== null && (
              <div className="bg-white rounded-xl p-6 border-2 border-purple-200">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600 mb-1">Costo Estimado</p>
                    <p className="text-4xl font-bold text-purple-600">
                      ${quotedRate.toLocaleString('es-CO')}
                    </p>
                    <p className="text-xs text-gray-500 mt-2">
                      Prioridad: <span className="font-medium">{formData.priority}</span>
                    </p>
                  </div>
                  <div className="emoji emoji-xl opacity-20">💰</div>
                </div>
              </div>
            )}

            {!quotedRate && (
              <div className="bg-white/50 rounded-xl p-6 text-center border-2 border-dashed border-purple-200">
                <div className="text-3xl mb-3">📊</div>
                <p className="text-sm text-gray-600">Completa los datos y haz click en "Calcular"</p>
              </div>
            )}
          </div>

          {/* Botones */}
          <div className="flex gap-4 justify-end">
            <button
              type="button"
              onClick={() => navigate('/dashboard')}
              className="btn btn-secondary px-6"
              disabled={loading}
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading || !quotedRate || success}
              className="btn btn-primary px-8"
            >
              {loading ? (
                <>
                  <svg className="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Creando...
                </>
              ) : (
                <>
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                  Crear Envío
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </Layout>
  );
}
