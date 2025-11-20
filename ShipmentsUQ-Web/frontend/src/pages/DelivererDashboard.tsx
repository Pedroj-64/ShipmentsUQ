import { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import DelivererLayout from '../components/DelivererLayout';
import {
  TruckIcon,
  CheckCircleIcon,
  StarIcon,
  UserIcon,
  RefreshIcon,
  PackageIcon,
  NavigationIcon as MapIcon,
  LocationIcon as MapPinIcon,
  TargetIcon
} from '../components/icons';

// Use PackageIcon for empty state
const InboxIcon = PackageIcon;

interface Shipment {
  id: string;
  customer: string;
  origin: {
    address: string;
    city: string;
    latitude: number;
    longitude: number;
  };
  destination: {
    address: string;
    city: string;
    latitude: number;
    longitude: number;
  };
  status: 'PENDING' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED';
  weight: number;
  priority: string;
}

export default function DelivererDashboard() {
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [selectedShipment, setSelectedShipment] = useState<Shipment | null>(null);
  const [metrics, setMetrics] = useState({
    activeShipments: 0,
    totalDeliveries: 0,
    averageRating: 0,
    status: 'AVAILABLE',
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDelivererData();
  }, []);

  const loadDelivererData = async () => {
    try {
      // Aquí se cargarían los datos desde el backend
      const mockShipments: Shipment[] = [
        {
          id: '1',
          customer: 'Juan Pérez',
          origin: {
            address: 'Calle 15 #10-20',
            city: 'Armenia',
            latitude: 4.5389,
            longitude: -75.6719,
          },
          destination: {
            address: 'Carrera 14 #25-50',
            city: 'Armenia',
            latitude: 4.5420,
            longitude: -75.6750,
          },
          status: 'IN_TRANSIT',
          weight: 5.5,
          priority: 'STANDARD',
        },
        {
          id: '2',
          customer: 'María González',
          origin: {
            address: 'Avenida Bolivar #30-10',
            city: 'Armenia',
            latitude: 4.5350,
            longitude: -75.6800,
          },
          destination: {
            address: 'Calle 20 #15-30',
            city: 'Armenia',
            latitude: 4.5450,
            longitude: -75.6650,
          },
          status: 'PENDING',
          weight: 3.2,
          priority: 'EXPRESS',
        },
      ];

      setShipments(mockShipments);
      setMetrics({
        activeShipments: mockShipments.filter(s => s.status === 'IN_TRANSIT').length,
        totalDeliveries: 127,
        averageRating: 4.8,
        status: 'AVAILABLE',
      });
      setLoading(false);
    } catch (error) {
      console.error('Error al cargar datos:', error);
      setLoading(false);
    }
  };

  const handleSelectShipment = (shipment: Shipment) => {
    setSelectedShipment(shipment);
  };

  const handleMarkDelivered = async () => {
    if (!selectedShipment) return;

    try {
      // Aquí se enviaría al backend
      console.log('Marcando como entregado:', selectedShipment.id);
      
      setShipments(
        shipments.map((s) =>
          s.id === selectedShipment.id ? { ...s, status: 'DELIVERED' as const } : s
        )
      );
      
      alert('Envío marcado como entregado exitosamente');
      setSelectedShipment(null);
      loadDelivererData();
    } catch (error) {
      console.error('Error:', error);
      alert('Error al marcar como entregado');
    }
  };

  const handleCalculateRoute = () => {
    if (!selectedShipment) {
      alert('Por favor seleccione un envío primero');
      return;
    }

    // Construir URL de Google Maps con ruta
    const origin = `${selectedShipment.origin.latitude},${selectedShipment.origin.longitude}`;
    const destination = `${selectedShipment.destination.latitude},${selectedShipment.destination.longitude}`;
    const mapsUrl = `https://www.google.com/maps/dir/?api=1&origin=${origin}&destination=${destination}&travelmode=driving`;
    
    window.open(mapsUrl, '_blank');
  };

  const getStatusBadge = (status: string) => {
    const badges: Record<string, { bg: string; text: string; label: string }> = {
      PENDING: { bg: 'bg-amber-100', text: 'text-amber-700', label: 'Pendiente' },
      IN_TRANSIT: { bg: 'bg-blue-100', text: 'text-blue-700', label: 'En Tránsito' },
      DELIVERED: { bg: 'bg-green-100', text: 'text-green-700', label: 'Entregado' },
      CANCELLED: { bg: 'bg-gray-100', text: 'text-gray-700', label: 'Cancelado' },
    };
    const badge = badges[status] || badges['PENDING'];
    return (
      <span className={`px-3 py-1 rounded-full text-xs font-medium ${badge.bg} ${badge.text}`}>
        {badge.label}
      </span>
    );
  };

  const getRouteColor = (status: string) => {
    const colors: Record<string, string> = {
      PENDING: '#f59e0b',
      IN_TRANSIT: '#3b82f6',
      DELIVERED: '#10b981',
      CANCELLED: '#6b7280',
    };
    return colors[status] || colors['PENDING'];
  };

  return (
    <DelivererLayout>
      <div className="h-full overflow-auto">
        {/* Header */}
        <header className="bg-white border-b border-gray-200 px-8 py-6">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Dashboard Repartidor</h1>
              <p className="text-gray-600 mt-1">Gestiona tus entregas asignadas</p>
            </div>
            <button
              onClick={loadDelivererData}
              className="flex items-center gap-2 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition-colors"
            >
              <RefreshIcon size={18} />
              Actualizar
            </button>
          </div>
        </header>

        {/* Content */}
        <div className="p-8">
          {/* Métricas */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
            <div className="card p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Envíos Activos</p>
                  <p className="text-3xl font-bold text-blue-600 mt-2">
                    {metrics.activeShipments}
                  </p>
                </div>
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <TruckIcon size={24} className="text-blue-600" />
                </div>
              </div>
            </div>

            <div className="card p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Entregas Totales</p>
                  <p className="text-3xl font-bold text-green-600 mt-2">
                    {metrics.totalDeliveries}
                  </p>
                </div>
                <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                  <CheckCircleIcon size={24} className="text-green-600" />
                </div>
              </div>
            </div>

            <div className="card p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Calificación</p>
                  <p className="text-3xl font-bold text-yellow-600 mt-2 flex items-center gap-1">
                    {metrics.averageRating.toFixed(1)}
                    <StarIcon size={28} className="text-yellow-600" />
                  </p>
                </div>
                <div className="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
                  <StarIcon size={24} className="text-yellow-600" />
                </div>
              </div>
            </div>

            <div className="card p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Estado</p>
                  <p className="text-xl font-bold text-purple-600 mt-2">{metrics.status}</p>
                </div>
                <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                  <UserIcon size={24} className="text-purple-600" />
                </div>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Lista de Envíos */}
            <div className="card p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">
                Mis Envíos Asignados
              </h2>

              {loading ? (
                <div className="text-center py-12">
                  <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-gray-200 border-t-purple-600"></div>
                  <p className="text-gray-500 mt-4">Cargando...</p>
                </div>
              ) : shipments.length === 0 ? (
                <div className="text-center py-12">
                  <div className="inline-flex items-center justify-center w-20 h-20 bg-gray-100 rounded-full mb-4">
                    <InboxIcon size={40} className="text-gray-400" />
                  </div>
                  <h3 className="text-lg font-medium text-gray-900">No tienes envíos asignados</h3>
                  <p className="text-gray-500 mt-2">Espera a que te asignen nuevas entregas</p>
                </div>
              ) : (
                <div className="space-y-3 max-h-[500px] overflow-y-auto">
                  {shipments.map((shipment) => (
                    <div
                      key={shipment.id}
                      onClick={() => handleSelectShipment(shipment)}
                      className={`p-4 border rounded-lg cursor-pointer transition-all ${
                        selectedShipment?.id === shipment.id
                          ? 'border-purple-500 bg-purple-50'
                          : 'border-gray-200 hover:border-purple-300 hover:bg-gray-50'
                      }`}
                    >
                      <div className="flex items-start justify-between mb-2">
                        <div className="flex items-center space-x-2">
                          <span className="text-sm font-semibold text-gray-900">
                            #{shipment.id}
                          </span>
                          {getStatusBadge(shipment.status)}
                        </div>
                        <span className="text-xs text-gray-500">{shipment.weight} kg</span>
                      </div>
                      <p className="text-sm text-gray-700 mb-1">
                        <strong>Cliente:</strong> {shipment.customer}
                      </p>
                      <p className="text-xs text-gray-600 flex items-center gap-2">
                        <MapPinIcon size={14} className="text-green-600" />
                        {shipment.origin.city}
                        <span>→</span>
                        <TargetIcon size={14} className="text-red-600" />
                        {shipment.destination.city}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Detalles y Mapa */}
            <div className="space-y-6">
              {/* Detalles del Envío */}
              {selectedShipment ? (
                <div className="card p-6">
                  <h2 className="text-lg font-semibold text-gray-900 mb-4">
                    Detalles del Envío
                  </h2>
                  
                  <div className="space-y-3 mb-6">
                    <div>
                      <p className="text-sm font-medium text-gray-500">ID del Envío</p>
                      <p className="text-gray-900">#{selectedShipment.id}</p>
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-500">Cliente</p>
                      <p className="text-gray-900">{selectedShipment.customer}</p>
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-500">Origen</p>
                      <p className="text-gray-900">
                        {selectedShipment.origin.address}, {selectedShipment.origin.city}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-500">Destino</p>
                      <p className="text-gray-900">
                        {selectedShipment.destination.address}, {selectedShipment.destination.city}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-500">Estado</p>
                      <div className="mt-1">{getStatusBadge(selectedShipment.status)}</div>
                    </div>
                  </div>

                  <div className="flex space-x-3">
                    <button
                      onClick={handleCalculateRoute}
                      className="flex items-center justify-center gap-2 px-4 py-3 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-xl font-medium hover:shadow-lg transition-all flex-1"
                    >
                      <MapIcon size={18} />
                      Ver Ruta en Maps
                    </button>
                    {selectedShipment.status === 'IN_TRANSIT' && (
                      <button
                        onClick={handleMarkDelivered}
                        className="flex items-center justify-center gap-2 px-4 py-3 bg-green-600 hover:bg-green-700 text-white rounded-xl font-medium hover:shadow-lg transition-all flex-1"
                      >
                        <CheckCircleIcon size={18} />
                        Marcar Entregado
                      </button>
                    )}
                  </div>
                </div>
              ) : (
                <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-12 text-center">
                  <div className="inline-flex items-center justify-center w-20 h-20 bg-purple-100 rounded-full mb-4">
                    <PackageIcon size={40} className="text-purple-600" />
                  </div>
                  <h3 className="text-lg font-medium text-gray-900">
                    Selecciona un Envío
                  </h3>
                  <p className="text-gray-500 mt-2">
                    Haz clic en un envío de la lista para ver sus detalles y calcular la ruta
                  </p>
                </div>
              )}

              {/* Mapa */}
              {selectedShipment && (
                <div className="card p-6">
                  <h2 className="text-lg font-semibold text-gray-900 mb-4">
                    Mapa de Ruta
                  </h2>
                  <div className="h-[400px] rounded-lg overflow-hidden border border-gray-200">
                    <MapContainer
                      center={
                        [
                          selectedShipment.origin.latitude,
                          selectedShipment.origin.longitude,
                        ] as [number, number]
                      }
                      zoom={13}
                      style={{ height: '100%', width: '100%' }}
                    >
                      <TileLayer
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                      />
                      
                      {/* Marcador de Origen */}
                      <Marker
                        position={[
                          selectedShipment.origin.latitude,
                          selectedShipment.origin.longitude,
                        ]}
                      >
                        <Popup>
                          <div className="flex items-center gap-1">
                            <strong>Origen</strong>
                          </div>
                          {selectedShipment.origin.address}
                          <br />
                          {selectedShipment.origin.city}
                        </Popup>
                      </Marker>

                      {/* Marcador de Destino */}
                      <Marker
                        position={[
                          selectedShipment.destination.latitude,
                          selectedShipment.destination.longitude,
                        ]}
                      >
                        <Popup>
                          <div className="flex items-center gap-1">
                            <strong>Destino</strong>
                          </div>
                          {selectedShipment.destination.address}
                          <br />
                          {selectedShipment.destination.city}
                        </Popup>
                      </Marker>

                      {/* Línea de Ruta */}
                      <Polyline
                        positions={[
                          [
                            selectedShipment.origin.latitude,
                            selectedShipment.origin.longitude,
                          ],
                          [
                            selectedShipment.destination.latitude,
                            selectedShipment.destination.longitude,
                          ],
                        ]}
                        color={getRouteColor(selectedShipment.status)}
                        weight={4}
                        opacity={0.7}
                      />
                    </MapContainer>
                  </div>
                  <p className="text-xs text-gray-500 mt-2 text-center">
                    Haz clic en "Ver Ruta en Maps" para obtener la ruta optimizada con Dijkstra
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </DelivererLayout>
  );
}
