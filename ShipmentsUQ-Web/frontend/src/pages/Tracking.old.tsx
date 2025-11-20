import { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import Layout from '../components/Layout';

// Fix for default marker icons
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

interface Shipment {
  id: string;
  trackingNumber: string;
  status: 'PENDING' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED';
  origin: string;
  destination: string;
  originLat: number;
  originLon: number;
  destinationLat: number;
  destinationLon: number;
  currentLat?: number;
  currentLon?: number;
  estimatedDelivery: string;
  createdAt: string;
  cost: number;
}

interface StatusEvent {
  status: string;
  description: string;
  location: string;
  timestamp: string;
  isCompleted: boolean;
}

function MapController({ center, zoom }: { center: [number, number]; zoom: number }) {
  const map = useMap();
  useEffect(() => {
    map.setView(center, zoom);
  }, [center, zoom, map]);
  return null;
}

export default function Tracking() {
  const [trackingNumber, setTrackingNumber] = useState('');
  const [shipment, setShipment] = useState<Shipment | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [statusEvents, setStatusEvents] = useState<StatusEvent[]>([]);

  const handleSearch = () => {
    if (!trackingNumber.trim()) {
      setError('Por favor ingresa un número de rastreo');
      return;
    }
    
    setLoading(true);
    setError('');
    
    setTimeout(() => {
      const mockShipment: Shipment = {
        id: '1',
        trackingNumber: trackingNumber,
        status: 'IN_TRANSIT',
        origin: 'Calle 15 #10-20, Armenia',
        destination: 'Carrera 14 #25-50, Armenia',
        originLat: 4.5389,
        originLon: -75.6719,
        destinationLat: 4.5420,
        destinationLon: -75.6750,
        currentLat: 4.5405,
        currentLon: -75.6735,
        estimatedDelivery: '2024-03-20T14:00:00',
        createdAt: '2024-03-19T10:30:00',
        cost: 25000,
      };

      const mockEvents: StatusEvent[] = [
        {
          status: 'Pedido Creado',
          description: 'Tu envío ha sido registrado en nuestro sistema',
          location: 'Armenia, Quindío',
          timestamp: '2024-03-19T10:30:00',
          isCompleted: true,
        },
        {
          status: 'En Recolección',
          description: 'El repartidor está recogiendo tu paquete',
          location: 'Calle 15 #10-20, Armenia',
          timestamp: '2024-03-19T11:00:00',
          isCompleted: true,
        },
        {
          status: 'En Tránsito',
          description: 'Tu paquete está en camino',
          location: 'Armenia, Quindío',
          timestamp: '2024-03-19T11:30:00',
          isCompleted: true,
        },
        {
          status: 'En Entrega',
          description: 'El repartidor está cerca de la dirección de destino',
          location: 'Carrera 14, Armenia',
          timestamp: '2024-03-20T13:45:00',
          isCompleted: false,
        },
        {
          status: 'Entregado',
          description: 'Tu paquete ha sido entregado exitosamente',
          location: 'Carrera 14 #25-50, Armenia',
          timestamp: '',
          isCompleted: false,
        },
      ];

      setShipment(mockShipment);
      setStatusEvents(mockEvents);
      setLoading(false);
    }, 1000);
  };

  const getStatusConfig = (status: string) => {
    const configs: { [key: string]: { bg: string; text: string; label: string; icon: string } } = {
      'PENDING': { bg: 'bg-yellow-100', text: 'text-yellow-700', label: 'Pendiente', icon: '⏳' },
      'IN_TRANSIT': { bg: 'bg-blue-100', text: 'text-blue-700', label: 'En Tránsito', icon: '🚚' },
      'DELIVERED': { bg: 'bg-green-100', text: 'text-green-700', label: 'Entregado', icon: '✅' },
      'CANCELLED': { bg: 'bg-gray-100', text: 'text-gray-700', label: 'Cancelado', icon: '❌' }
    };
    return configs[status] || configs['PENDING'];
  };

  const originIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });

  const destinationIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });

  const currentIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });

  return (
    <Layout>
      <div className="max-w-7xl">
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-6">
            <div className="bg-purple-100 p-3 rounded-xl">
              <svg className="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Rastrear Envío</h1>
              <p className="text-gray-600 mt-1">Ingresa el número de rastreo para ver el estado de tu envío</p>
            </div>
          </div>

          <div className="card">
            <div className="flex gap-3">
              <div className="flex-1">
                <input
                  type="text"
                  value={trackingNumber}
                  onChange={(e) => setTrackingNumber(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                  className="input"
                  placeholder="Ej: SHP-2024-001234"
                />
              </div>
              <button
                onClick={handleSearch}
                disabled={loading}
                className="btn btn-primary px-8"
              >
                {loading ? (
                  <>
                    <svg className="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Buscando...
                  </>
                ) : (
                  <>
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                    Rastrear
                  </>
                )}
              </button>
            </div>
            {error && (
              <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
                {error}
              </div>
            )}
          </div>
        </div>

        {shipment ? (
          <div className="grid lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 space-y-6">
              <div className="card">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h2 className="text-xl font-semibold text-gray-900">Mapa de Rastreo</h2>
                    <p className="text-sm text-gray-600 mt-1">Ubicación en tiempo real de tu envío</p>
                  </div>
                  <span className={`badge ${getStatusConfig(shipment.status).bg} ${getStatusConfig(shipment.status).text}`}>
                    {getStatusConfig(shipment.status).icon} {getStatusConfig(shipment.status).label}
                  </span>
                </div>
                
                <div className="rounded-xl overflow-hidden border border-gray-200" style={{ height: '400px' }}>
                  <MapContainer
                    center={[shipment.originLat, shipment.originLon]}
                    zoom={14}
                    style={{ height: '100%', width: '100%' }}
                  >
                    <TileLayer
                      attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                      url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    />
                    <MapController 
                      center={[
                        (shipment.originLat + shipment.destinationLat) / 2,
                        (shipment.originLon + shipment.destinationLon) / 2
                      ]} 
                      zoom={13}
                    />
                    
                    <Marker position={[shipment.originLat, shipment.originLon]} icon={originIcon}>
                      <Popup>
                        <div className="text-sm">
                          <p className="font-semibold mb-1">📍 Origen</p>
                          <p>{shipment.origin}</p>
                        </div>
                      </Popup>
                    </Marker>

                    <Marker position={[shipment.destinationLat, shipment.destinationLon]} icon={destinationIcon}>
                      <Popup>
                        <div className="text-sm">
                          <p className="font-semibold mb-1">🎯 Destino</p>
                          <p>{shipment.destination}</p>
                        </div>
                      </Popup>
                    </Marker>

                    {shipment.currentLat && shipment.currentLon && (
                      <Marker position={[shipment.currentLat, shipment.currentLon]} icon={currentIcon}>
                        <Popup>
                          <div className="text-sm">
                            <p className="font-semibold mb-1">🚚 Ubicación Actual</p>
                            <p>Tu paquete está aquí</p>
                          </div>
                        </Popup>
                      </Marker>
                    )}

                    <Polyline
                      positions={[
                        [shipment.originLat, shipment.originLon],
                        [shipment.destinationLat, shipment.destinationLon]
                      ]}
                      color="#9333ea"
                      weight={3}
                      opacity={0.7}
                      dashArray="10, 10"
                    />
                  </MapContainer>
                </div>
              </div>

              {/* Timeline */}
              <div className="card">
                <h2 className="text-xl font-semibold text-gray-900 mb-6">Historial de Seguimiento</h2>
                <div className="space-y-4">
                  {statusEvents.map((event, index) => (
                    <div key={index} className="flex gap-4">
                      <div className="flex flex-col items-center">
                        <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                          event.isCompleted ? 'bg-purple-100 text-purple-600' : 'bg-gray-100 text-gray-400'
                        }`}>
                          {event.isCompleted ? (
                            <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                            </svg>
                          ) : (
                            <div className="w-3 h-3 rounded-full bg-current"></div>
                          )}
                        </div>
                        {index < statusEvents.length - 1 && (
                          <div className={`w-0.5 h-full min-h-[40px] ${
                            event.isCompleted ? 'bg-purple-300' : 'bg-gray-200'
                          }`}></div>
                        )}
                      </div>
                      <div className="flex-1 pb-6">
                        <h3 className={`font-semibold ${event.isCompleted ? 'text-gray-900' : 'text-gray-400'}`}>
                          {event.status}
                        </h3>
                        <p className={`text-sm mt-1 ${event.isCompleted ? 'text-gray-600' : 'text-gray-400'}`}>
                          {event.description}
                        </p>
                        <div className="flex items-center gap-4 mt-2 text-xs text-gray-500">
                          <span className="flex items-center gap-1">
                            <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                            </svg>
                            {event.location}
                          </span>
                          {event.timestamp && (
                            <span className="flex items-center gap-1">
                              <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                              </svg>
                              {new Date(event.timestamp).toLocaleString('es-CO')}
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Detalles del Envío */}
            <div className="space-y-6">
              <div className="card">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Detalles del Envío</h2>
                <div className="space-y-4">
                  <div>
                    <label className="text-xs text-gray-600">Número de Rastreo</label>
                    <p className="text-sm font-mono font-semibold text-gray-900 mt-1">{shipment.trackingNumber}</p>
                  </div>
                  <div>
                    <label className="text-xs text-gray-600">Estado</label>
                    <p className="text-sm font-medium text-gray-900 mt-1">
                      <span className={`badge ${getStatusConfig(shipment.status).bg} ${getStatusConfig(shipment.status).text}`}>
                        {getStatusConfig(shipment.status).icon} {getStatusConfig(shipment.status).label}
                      </span>
                    </p>
                  </div>
                  <div className="pt-4 border-t border-gray-100">
                    <label className="text-xs text-gray-600">Origen</label>
                    <p className="text-sm text-gray-900 mt-1 flex items-start gap-2">
                      <svg className="w-4 h-4 text-green-600 mt-0.5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
                      </svg>
                      {shipment.origin}
                    </p>
                  </div>
                  <div>
                    <label className="text-xs text-gray-600">Destino</label>
                    <p className="text-sm text-gray-900 mt-1 flex items-start gap-2">
                      <svg className="w-4 h-4 text-red-600 mt-0.5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
                      </svg>
                      {shipment.destination}
                    </p>
                  </div>
                  <div className="pt-4 border-t border-gray-100">
                    <label className="text-xs text-gray-600">Entrega Estimada</label>
                    <p className="text-sm font-medium text-gray-900 mt-1">
                      {new Date(shipment.estimatedDelivery).toLocaleDateString('es-CO', {
                        weekday: 'long',
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </p>
                  </div>
                  <div>
                    <label className="text-xs text-gray-600">Fecha de Creación</label>
                    <p className="text-sm text-gray-900 mt-1">
                      {new Date(shipment.createdAt).toLocaleDateString('es-CO')}
                    </p>
                  </div>
                  <div className="pt-4 border-t border-gray-100">
                    <label className="text-xs text-gray-600">Costo Total</label>
                    <p className="text-2xl font-bold text-purple-600 mt-1">
                      ${shipment.cost.toLocaleString('es-CO')}
                    </p>
                  </div>
                </div>
              </div>

              <div className="card bg-purple-50 border-2 border-purple-200">
                <h3 className="font-semibold text-purple-900 mb-2">¿Necesitas ayuda?</h3>
                <p className="text-sm text-purple-800 mb-4">
                  Si tienes alguna pregunta sobre tu envío, contáctanos
                </p>
                <button className="btn btn-primary w-full">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
                  </svg>
                  Contactar Soporte
                </button>
              </div>
            </div>
          </div>
        ) : (
          <div className="card text-center py-16">
            <div className="text-4xl mb-4">📦</div>
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
