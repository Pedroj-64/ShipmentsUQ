import { useState } from 'react';
import Layout from '../components/Layout';
import {
  PackageIcon,
  SearchIcon,
  ClockIcon,
  TruckIcon,
  CheckCircleIcon,
  AlertIcon,
  LoadingIcon,
  LocationIcon as MapPinIcon,
  RefreshIcon,
  CloseIcon as XIcon,
  NavigationIcon,
  UserIcon,
  CalendarIcon
} from '../components/icons';

interface Shipment {
  id: number;
  trackingNumber: string;
  status: string;
  origin: string;
  destination: string;
  originLat: number;
  originLon: number;
  destinationLat: number;
  destinationLon: number;
  currentLat?: number;
  currentLon?: number;
  cost: number;
  estimatedDelivery: string;
  createdAt: string;
  delivererName?: string;
  weight: number;
  priority: string;
}

interface StatusEvent {
  status: string;
  description: string;
  location: string;
  timestamp?: string;
  isCompleted: boolean;
}

export default function Tracking() {
  const [trackingNumber, setTrackingNumber] = useState('');
  const [shipment, setShipment] = useState<Shipment | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Mock status events based on shipment status
  const getStatusEvents = (status: string): StatusEvent[] => {
    const events = [
      {
        status: 'Envío Creado',
        description: 'Tu envío ha sido registrado en nuestro sistema',
        location: 'Armenia, Quindío',
        timestamp: '2024-01-15T10:00:00',
        isCompleted: true
      },
      {
        status: 'En Tránsito',
        description: 'Tu paquete está en camino',
        location: 'Pereira, Risaralda',
        timestamp: status !== 'PENDING' ? '2024-01-15T14:30:00' : undefined,
        isCompleted: status !== 'PENDING'
      },
      {
        status: 'En Ruta de Entrega',
        description: 'El repartidor está llevando tu paquete',
        location: 'Medellín, Antioquia',
        timestamp: status === 'DELIVERED' ? '2024-01-16T09:00:00' : undefined,
        isCompleted: status === 'DELIVERED'
      },
      {
        status: 'Entregado',
        description: 'Tu paquete ha sido entregado exitosamente',
        location: 'Medellín, Antioquia',
        timestamp: status === 'DELIVERED' ? '2024-01-16T11:45:00' : undefined,
        isCompleted: status === 'DELIVERED'
      }
    ];

    if (status === 'CANCELLED') {
      return [
        events[0],
        {
          status: 'Cancelado',
          description: 'El envío ha sido cancelado',
          location: 'Armenia, Quindío',
          timestamp: '2024-01-15T12:00:00',
          isCompleted: true
        }
      ];
    }

    return events;
  };

  const handleSearch = async () => {
    if (!trackingNumber.trim()) {
      setError('Por favor ingresa un número de rastreo');
      return;
    }

    setLoading(true);
    setError('');

    try {
      // TODO: Replace with actual API call
      // const response = await fetch(`http://localhost:8080/api/shipments/tracking/${trackingNumber}`);
      // const data = await response.json();
      
      // Mock data for demonstration
      setTimeout(() => {
        const mockShipment: Shipment = {
          id: 1,
          trackingNumber: trackingNumber,
          status: 'IN_TRANSIT',
          origin: 'Armenia, Quindío',
          destination: 'Medellín, Antioquia',
          originLat: 4.5389,
          originLon: -75.6719,
          destinationLat: 6.2476,
          destinationLon: -75.5658,
          currentLat: 5.0689,
          currentLon: -75.5174,
          cost: 25000,
          estimatedDelivery: '2024-01-16T15:00:00',
          createdAt: '2024-01-15T10:00:00',
          delivererName: 'Juan Pérez',
          weight: 2.5,
          priority: 'EXPRESS'
        };

        setShipment(mockShipment);
        setLoading(false);
      }, 1000);
    } catch (err) {
      setError('No se pudo encontrar el envío. Verifica el número de rastreo.');
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    if (shipment) {
      handleSearch();
    }
  };

  const handleClose = () => {
    setShipment(null);
    setTrackingNumber('');
    setError('');
  };

  const getStatusConfig = (status: string) => {
    const configs: { [key: string]: { 
      Icon: React.ComponentType<any>; 
      bg: string; 
      text: string; 
      label: string;
    } } = {
      'PENDING': { 
        Icon: ClockIcon,
        bg: 'bg-yellow-100', 
        text: 'text-yellow-700', 
        label: 'Pendiente'
      },
      'IN_TRANSIT': { 
        Icon: TruckIcon,
        bg: 'bg-blue-100', 
        text: 'text-blue-700', 
        label: 'En Tránsito'
      },
      'DELIVERED': { 
        Icon: CheckCircleIcon,
        bg: 'bg-green-100', 
        text: 'text-green-700', 
        label: 'Entregado'
      },
      'CANCELLED': { 
        Icon: AlertIcon,
        bg: 'bg-gray-100', 
        text: 'text-gray-700', 
        label: 'Cancelado'
      }
    };
    return configs[status] || configs['PENDING'];
  };

  const getPriorityLabel = (priority: string) => {
    const priorities: { [key: string]: string } = {
      'STANDARD': 'Estándar',
      'EXPRESS': 'Express',
      'URGENT': 'Urgente'
    };
    return priorities[priority] || priority;
  };

  const calculateDistance = (lat1: number, lon1: number, lat2: number, lon2: number): number => {
    const R = 6371; // Radio de la Tierra en km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = 
      Math.sin(dLat/2) * Math.sin(dLat/2) +
      Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
      Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
  };

  const statusConfig = shipment ? getStatusConfig(shipment.status) : null;
  const StatusIcon = statusConfig?.Icon;

  return (
    <Layout>
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-6">
            <div className="bg-purple-100 p-3 rounded-xl">
              <NavigationIcon size={24} className="text-purple-600" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Rastrear Envío</h1>
              <p className="text-gray-600 mt-1">Ingresa el número de rastreo para ver el estado de tu envío</p>
            </div>
          </div>

          {/* Search Card */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
            <div className="flex gap-3">
              <div className="flex-1 relative">
                <SearchIcon size={20} className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                <input
                  type="text"
                  value={trackingNumber}
                  onChange={(e) => setTrackingNumber(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                  placeholder="Ej: SHP-2024-001234"
                />
              </div>
              <button
                onClick={handleSearch}
                disabled={loading}
                className="px-8 py-3 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-xl font-medium hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
              >
                {loading ? (
                  <>
                    <LoadingIcon size={20} className="animate-spin" />
                    Buscando...
                  </>
                ) : (
                  <>
                    <SearchIcon size={20} />
                    Rastrear
                  </>
                )}
              </button>
            </div>
            {error && (
              <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-xl flex items-center gap-2 text-sm text-red-700">
                <AlertIcon size={20} />
                {error}
              </div>
            )}
          </div>
        </div>

        {shipment ? (
          <div className="grid lg:grid-cols-3 gap-6">
            {/* Main Content */}
            <div className="lg:col-span-2 space-y-6">
              {/* Shipment Info Card */}
              <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
                <div className="flex justify-between items-start mb-6">
                  <div>
                    <h2 className="text-xl font-semibold text-gray-900">Información del Envío</h2>
                    <p className="text-sm text-gray-600 mt-1">Número de rastreo: <span className="font-mono font-semibold text-purple-600">{shipment.trackingNumber}</span></p>
                  </div>
                  {StatusIcon && (
                    <span className={`flex items-center gap-2 px-4 py-2 rounded-xl font-medium ${statusConfig.bg} ${statusConfig.text}`}>
                      <StatusIcon size={20} />
                      {statusConfig.label}
                    </span>
                  )}
                </div>

                {/* Info Grid */}
                <div className="grid md:grid-cols-3 gap-4 p-4 bg-gray-50 rounded-xl">
                  <div className="flex items-start gap-3">
                    <div className="bg-purple-100 p-2 rounded-lg">
                      <PackageIcon size={20} className="text-purple-600" />
                    </div>
                    <div>
                      <p className="text-xs text-gray-600">ID del Envío</p>
                      <p className="text-sm font-semibold text-gray-900">#{shipment.id}</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3">
                    {StatusIcon && (
                      <>
                        <div className={`${statusConfig.bg} p-2 rounded-lg`}>
                          <StatusIcon size={20} className={statusConfig.text} />
                        </div>
                        <div>
                          <p className="text-xs text-gray-600">Estado</p>
                          <p className={`text-sm font-semibold ${statusConfig.text}`}>{statusConfig.label}</p>
                        </div>
                      </>
                    )}
                  </div>
                  <div className="flex items-start gap-3">
                    <div className="bg-blue-100 p-2 rounded-lg">
                      <UserIcon size={20} className="text-blue-600" />
                    </div>
                    <div>
                      <p className="text-xs text-gray-600">Repartidor</p>
                      <p className="text-sm font-semibold text-gray-900">{shipment.delivererName || 'Sin asignar'}</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Map Placeholder */}
              <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
                <div className="flex justify-between items-center mb-4">
                  <h2 className="text-xl font-semibold text-gray-900">Ubicación Actual</h2>
                  <button
                    onClick={handleRefresh}
                    className="flex items-center gap-2 px-4 py-2 text-sm text-purple-600 hover:bg-purple-50 rounded-lg transition-colors"
                  >
                    <RefreshIcon size={16} />
                    Actualizar
                  </button>
                </div>
                
                {/* Simple map representation */}
                <div className="relative h-80 bg-gradient-to-br from-purple-50 via-blue-50 to-purple-50 rounded-xl border-2 border-dashed border-purple-200 flex items-center justify-center overflow-hidden">
                  {/* Decorative elements */}
                  <div className="absolute inset-0 opacity-10">
                    <div className="absolute top-10 left-10 w-32 h-32 bg-purple-400 rounded-full blur-3xl"></div>
                    <div className="absolute bottom-10 right-10 w-40 h-40 bg-blue-400 rounded-full blur-3xl"></div>
                  </div>
                  
                  {/* Route visualization */}
                  <div className="relative w-full max-w-md px-8">
                    <div className="flex justify-between items-center mb-4">
                      <div className="flex flex-col items-center">
                        <div className="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center shadow-lg mb-2">
                          <MapPinIcon size={24} className="text-white" />
                        </div>
                        <p className="text-xs font-medium text-gray-700">Origen</p>
                        <p className="text-xs text-gray-600">{shipment.origin}</p>
                      </div>
                      
                      {/* Current position */}
                      {shipment.currentLat && shipment.currentLon && (
                        <div className="flex flex-col items-center">
                          <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center shadow-lg mb-2 animate-pulse">
                            <TruckIcon size={20} className="text-white" />
                          </div>
                          <p className="text-xs font-medium text-blue-600">En camino</p>
                        </div>
                      )}
                      
                      <div className="flex flex-col items-center">
                        <div className="w-12 h-12 bg-red-500 rounded-full flex items-center justify-center shadow-lg mb-2">
                          <MapPinIcon size={24} className="text-white" />
                        </div>
                        <p className="text-xs font-medium text-gray-700">Destino</p>
                        <p className="text-xs text-gray-600">{shipment.destination}</p>
                      </div>
                    </div>
                    
                    {/* Route line */}
                    <div className="h-1 bg-gradient-to-r from-green-500 via-blue-500 to-red-500 rounded-full"></div>
                  </div>
                </div>

                {/* Coordinates and Distance */}
                <div className="grid md:grid-cols-2 gap-4 mt-4">
                  <div className="p-4 bg-gray-50 rounded-xl">
                    <div className="flex items-center gap-2 text-sm text-gray-600 mb-2">
                      <MapPinIcon size={16} />
                      <span className="font-medium">Coordenadas Actuales</span>
                    </div>
                    <p className="text-sm font-mono text-gray-900">
                      {shipment.currentLat?.toFixed(4) || shipment.originLat.toFixed(4)}, {shipment.currentLon?.toFixed(4) || shipment.originLon.toFixed(4)}
                    </p>
                  </div>
                  <div className="p-4 bg-gray-50 rounded-xl">
                    <div className="flex items-center gap-2 text-sm text-gray-600 mb-2">
                      <NavigationIcon size={16} />
                      <span className="font-medium">Distancia al Destino</span>
                    </div>
                    <p className="text-sm font-semibold text-purple-600">
                      {shipment.currentLat && shipment.currentLon
                        ? `${calculateDistance(
                            shipment.currentLat,
                            shipment.currentLon,
                            shipment.destinationLat,
                            shipment.destinationLon
                          ).toFixed(1)} km`
                        : `${calculateDistance(
                            shipment.originLat,
                            shipment.originLon,
                            shipment.destinationLat,
                            shipment.destinationLon
                          ).toFixed(1)} km`
                      }
                    </p>
                  </div>
                </div>
              </div>

              {/* Timeline */}
              <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
                <h2 className="text-xl font-semibold text-gray-900 mb-6">Historial de Seguimiento</h2>
                <div className="space-y-4">
                  {getStatusEvents(shipment.status).map((event, index) => (
                    <div key={index} className="flex gap-4">
                      <div className="flex flex-col items-center">
                        <div className={`w-10 h-10 rounded-full flex items-center justify-center transition-all ${
                          event.isCompleted ? 'bg-purple-100 text-purple-600 shadow-sm' : 'bg-gray-100 text-gray-400'
                        }`}>
                          {event.isCompleted ? (
                            <CheckCircleIcon size={20} />
                          ) : (
                            <ClockIcon size={20} />
                          )}
                        </div>
                        {index < getStatusEvents(shipment.status).length - 1 && (
                          <div className={`w-0.5 h-full min-h-[40px] transition-all ${
                            event.isCompleted ? 'bg-purple-300' : 'bg-gray-200'
                          }`}></div>
                        )}
                      </div>
                      <div className="flex-1 pb-6">
                        <h3 className={`font-semibold transition-colors ${event.isCompleted ? 'text-gray-900' : 'text-gray-400'}`}>
                          {event.status}
                        </h3>
                        <p className={`text-sm mt-1 transition-colors ${event.isCompleted ? 'text-gray-600' : 'text-gray-400'}`}>
                          {event.description}
                        </p>
                        <div className="flex items-center gap-4 mt-2 text-xs text-gray-500">
                          <span className="flex items-center gap-1">
                            <MapPinIcon size={12} />
                            {event.location}
                          </span>
                          {event.timestamp && (
                            <span className="flex items-center gap-1">
                              <CalendarIcon size={12} />
                              {new Date(event.timestamp).toLocaleString('es-CO', {
                                day: '2-digit',
                                month: 'short',
                                hour: '2-digit',
                                minute: '2-digit'
                              })}
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Sidebar - Shipment Details */}
            <div className="space-y-6">
              <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Detalles del Envío</h2>
                <div className="space-y-4">
                  <div>
                    <label className="text-xs text-gray-600 uppercase tracking-wide">Origen</label>
                    <p className="text-sm text-gray-900 mt-1 flex items-center gap-2">
                      <MapPinIcon size={16} className="text-green-600" />
                      {shipment.origin}
                    </p>
                  </div>
                  <div>
                    <label className="text-xs text-gray-600 uppercase tracking-wide">Destino</label>
                    <p className="text-sm text-gray-900 mt-1 flex items-center gap-2">
                      <MapPinIcon size={16} className="text-red-600" />
                      {shipment.destination}
                    </p>
                  </div>
                  <div className="pt-4 border-t border-gray-100">
                    <label className="text-xs text-gray-600 uppercase tracking-wide">Peso</label>
                    <p className="text-sm font-medium text-gray-900 mt-1">
                      {shipment.weight} kg
                    </p>
                  </div>
                  <div>
                    <label className="text-xs text-gray-600 uppercase tracking-wide">Prioridad</label>
                    <p className="text-sm font-medium text-gray-900 mt-1">
                      {getPriorityLabel(shipment.priority)}
                    </p>
                  </div>
                  <div className="pt-4 border-t border-gray-100">
                    <label className="text-xs text-gray-600 uppercase tracking-wide">Entrega Estimada</label>
                    <p className="text-sm font-medium text-gray-900 mt-1 flex items-center gap-2">
                      <CalendarIcon size={16} className="text-purple-600" />
                      {new Date(shipment.estimatedDelivery).toLocaleDateString('es-CO', {
                        weekday: 'short',
                        day: '2-digit',
                        month: 'short',
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </p>
                  </div>
                  <div>
                    <label className="text-xs text-gray-600 uppercase tracking-wide">Fecha de Creación</label>
                    <p className="text-sm text-gray-900 mt-1">
                      {new Date(shipment.createdAt).toLocaleDateString('es-CO', {
                        day: '2-digit',
                        month: 'long',
                        year: 'numeric'
                      })}
                    </p>
                  </div>
                  <div className="pt-4 border-t border-gray-100">
                    <label className="text-xs text-gray-600 uppercase tracking-wide">Costo Total</label>
                    <p className="text-3xl font-bold text-purple-600 mt-1">
                      ${shipment.cost.toLocaleString('es-CO')}
                    </p>
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 space-y-3">
                <button
                  onClick={handleRefresh}
                  className="w-full flex items-center justify-center gap-2 px-4 py-3 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-xl font-medium hover:shadow-lg transition-all"
                >
                  <RefreshIcon size={20} />
                  Actualizar Ubicación
                </button>
                <button
                  onClick={handleClose}
                  className="w-full flex items-center justify-center gap-2 px-4 py-3 bg-gray-100 text-gray-700 rounded-xl font-medium hover:bg-gray-200 transition-colors"
                >
                  <XIcon size={20} />
                  Cerrar
                </button>
              </div>

              {/* Help Card */}
              <div className="bg-gradient-to-br from-purple-50 to-blue-50 rounded-2xl border-2 border-purple-200 p-6">
                <h3 className="font-semibold text-purple-900 mb-2">¿Necesitas ayuda?</h3>
                <p className="text-sm text-purple-800 mb-4">
                  Si tienes alguna pregunta sobre tu envío, contáctanos
                </p>
                <button className="w-full flex items-center justify-center gap-2 px-4 py-3 bg-white text-purple-600 rounded-xl font-medium hover:shadow-md transition-all border border-purple-200">
                  <UserIcon size={18} />
                  Contactar Soporte
                </button>
              </div>
            </div>
          </div>
        ) : (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-16 text-center">
            <div className="inline-flex items-center justify-center w-20 h-20 bg-purple-100 rounded-full mb-4">
              <PackageIcon size={40} className="text-purple-600" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">Rastrear tu Envío</h3>
            <p className="text-gray-600 max-w-md mx-auto">
              Ingresa el número de rastreo en el campo de arriba para ver el estado y la ubicación de tu envío en tiempo real
            </p>
          </div>
        )}
      </div>
    </Layout>
  );
}
