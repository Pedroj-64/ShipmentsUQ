import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { shipmentService } from '../services/api';
import Layout from '../components/Layout';
import { 
  PackageIcon,
  TruckIcon,
  CheckCircleIcon,
  ClockIcon,
  PlusIcon,
  LoadingIcon,
  PackageDeliveredIcon,
  TrendingUpIcon,
  AlertIcon
} from '../components/icons';

interface Shipment {
  id: string;
  status: string;
  origin: string;
  destination: string;
  createdAt: string;
  estimatedDelivery: string;
  trackingNumber: string;
  cost: number;
}

interface StatusConfig {
  bg: string;
  text: string;
  label: string;
  Icon: React.ComponentType<any>;
}

export default function Dashboard() {
  const { user } = useAuth();
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    loadShipments();
  }, [user, navigate]);

  const loadShipments = async () => {
    try {
      setError(null);
      if (user?.id) {
        console.log('Cargando envíos para usuario:', user.id);
        const data = await shipmentService.getUserShipments(user.id);
        console.log('Envíos recibidos:', data);
        setShipments(Array.isArray(data) ? data : []);
      }
    } catch (error: any) {
      console.error('Error al cargar envíos:', error);
      setError(error.message || 'Error al cargar los envíos');
      setShipments([]);
    } finally {
      setLoading(false);
    }
  };

  const getStatusConfig = (status: string): StatusConfig => {
    const configs: Record<string, StatusConfig> = {
      'PENDING': { 
        bg: 'bg-yellow-100', 
        text: 'text-yellow-700', 
        label: 'Pendiente',
        Icon: ClockIcon
      },
      'IN_TRANSIT': { 
        bg: 'bg-blue-100', 
        text: 'text-blue-700', 
        label: 'En Tránsito',
        Icon: TruckIcon
      },
      'DELIVERED': { 
        bg: 'bg-green-100', 
        text: 'text-green-700', 
        label: 'Entregado',
        Icon: CheckCircleIcon
      },
      'CANCELLED': { 
        bg: 'bg-gray-100', 
        text: 'text-gray-700', 
        label: 'Cancelado',
        Icon: AlertIcon
      },
    };
    return configs[status] || configs['PENDING'];
  };

  const stats = {
    total: shipments.length,
    inTransit: shipments.filter(s => s.status === 'IN_TRANSIT').length,
    delivered: shipments.filter(s => s.status === 'DELIVERED').length,
    pending: shipments.filter(s => s.status === 'PENDING').length,
  };

  const recentShipments = shipments.slice(0, 5);

  return (
    <Layout>
      <div className="max-w-7xl">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3">
                ¡Hola, {user?.name}!
                <span className="text-4xl">👋</span>
              </h1>
              <p className="text-gray-600 mt-1">Aquí está el resumen de tus envíos</p>
            </div>
            <button 
              onClick={() => navigate('/shipments/create')}
              className="btn btn-primary px-6 shadow-lg shadow-purple-500/30 flex items-center gap-2"
            >
              <PlusIcon size={20} />
              Nuevo Envío
            </button>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          {/* Total */}
          <div className="card-hover">
            <div className="flex items-center justify-between mb-4">
              <div className="bg-purple-100 p-3 rounded-xl">
                <PackageIcon size={24} className="text-purple-600" strokeWidth={2} />
              </div>
              <span className="text-3xl font-bold text-gray-900">{stats.total}</span>
            </div>
            <p className="text-sm font-medium text-gray-600">Total de Envíos</p>
            <p className="text-xs text-gray-500 mt-1">Todos tus envíos</p>
          </div>

          {/* En Tránsito */}
          <div className="card-hover">
            <div className="flex items-center justify-between mb-4">
              <div className="bg-blue-100 p-3 rounded-xl">
                <TruckIcon size={24} className="text-blue-600" strokeWidth={2} />
              </div>
              <span className="text-3xl font-bold text-gray-900">{stats.inTransit}</span>
            </div>
            <p className="text-sm font-medium text-gray-600">En Tránsito</p>
            <p className="text-xs text-gray-500 mt-1">En camino</p>
          </div>

          {/* Entregados */}
          <div className="card-hover">
            <div className="flex items-center justify-between mb-4">
              <div className="bg-green-100 p-3 rounded-xl">
                <PackageDeliveredIcon size={24} className="text-green-600" strokeWidth={2} />
              </div>
              <span className="text-3xl font-bold text-gray-900">{stats.delivered}</span>
            </div>
            <p className="text-sm font-medium text-gray-600">Entregados</p>
            <p className="text-xs text-gray-500 mt-1">Completados</p>
          </div>

          {/* Pendientes */}
          <div className="card-hover">
            <div className="flex items-center justify-between mb-4">
              <div className="bg-yellow-100 p-3 rounded-xl">
                <ClockIcon size={24} className="text-yellow-600" strokeWidth={2} />
              </div>
              <span className="text-3xl font-bold text-gray-900">{stats.pending}</span>
            </div>
            <p className="text-sm font-medium text-gray-600">Pendientes</p>
            <p className="text-xs text-gray-500 mt-1">Por procesar</p>
          </div>
        </div>

        {/* Recent Shipments */}
        <div className="card">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-bold text-gray-900">Envíos Recientes</h2>
            <button 
              onClick={() => navigate('/shipments')}
              className="text-purple-600 hover:text-purple-700 text-sm font-medium flex items-center gap-1"
            >
              Ver todos
              <TrendingUpIcon size={16} />
            </button>
          </div>

          {loading ? (
            <div className="flex flex-col justify-center items-center py-12">
              <LoadingIcon size={32} className="text-purple-600 animate-spin" />
              <p className="text-gray-500 mt-4">Cargando envíos...</p>
            </div>
          ) : error ? (
            <div className="text-center py-12">
              <AlertIcon size={48} className="text-red-500 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">Error al cargar envíos</h3>
              <p className="text-gray-600 mb-6">{error}</p>
              <button
                onClick={loadShipments}
                className="btn btn-primary"
              >
                Reintentar
              </button>
            </div>
          ) : recentShipments.length === 0 ? (
            <div className="text-center py-12">
              <PackageIcon size={48} className="text-gray-400 mx-auto mb-4" strokeWidth={1.5} />
              <h3 className="text-lg font-medium text-gray-900 mb-2">No tienes envíos aún</h3>
              <p className="text-gray-600 mb-6">Crea tu primer envío para comenzar</p>
              <button
                onClick={() => navigate('/shipments/create')}
                className="btn btn-primary flex items-center gap-2 mx-auto"
              >
                <PlusIcon size={20} />
                Crear Primer Envío
              </button>
            </div>
          ) : (
            <div className="space-y-4">
              {recentShipments.map((shipment) => {
                const config = getStatusConfig(shipment.status);
                const IconComponent = config.Icon;
                
                return (
                  <div
                    key={shipment.id}
                    onClick={() => navigate(`/tracking/${shipment.trackingNumber}`)}
                    className="border border-gray-200 rounded-xl p-4 hover:shadow-lg hover:border-purple-300 transition-all duration-200 cursor-pointer group"
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-4 flex-1">
                        <div className="bg-gray-50 group-hover:bg-purple-50 p-3 rounded-xl transition-colors">
                          <PackageIcon size={24} className="text-gray-600 group-hover:text-purple-600 transition-colors" />
                        </div>
                        
                        <div className="flex-1">
                          <div className="flex items-center gap-3 mb-1">
                            <h3 className="font-semibold text-gray-900">
                              {shipment.trackingNumber}
                            </h3>
                            <span className={`px-3 py-1 rounded-full text-xs font-medium ${config.bg} ${config.text} flex items-center gap-1.5`}>
                              <IconComponent size={12} />
                              {config.label}
                            </span>
                          </div>
                          
                          <div className="flex items-center gap-4 text-sm text-gray-600">
                            <span className="flex items-center gap-1">
                              <span className="font-medium">Origen:</span> {shipment.origin}
                            </span>
                            <span className="text-gray-400">→</span>
                            <span className="flex items-center gap-1">
                              <span className="font-medium">Destino:</span> {shipment.destination}
                            </span>
                          </div>
                        </div>
                      </div>
                      
                      <div className="text-right ml-4">
                        <p className="text-sm font-medium text-gray-900">
                          ${shipment.cost.toLocaleString('es-CO')}
                        </p>
                        <p className="text-xs text-gray-500 mt-1">
                          {new Date(shipment.estimatedDelivery).toLocaleDateString('es-CO')}
                        </p>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-8">
          <button
            onClick={() => navigate('/shipments/create')}
            className="card-hover text-left p-6 group"
          >
            <div className="bg-purple-100 group-hover:bg-purple-200 w-12 h-12 rounded-xl flex items-center justify-center mb-4 transition-colors">
              <PlusIcon size={24} className="text-purple-600" />
            </div>
            <h3 className="font-semibold text-gray-900 mb-2">Crear Envío</h3>
            <p className="text-sm text-gray-600">Envía paquetes a cualquier destino</p>
          </button>

          <button
            onClick={() => navigate('/tracking')}
            className="card-hover text-left p-6 group"
          >
            <div className="bg-blue-100 group-hover:bg-blue-200 w-12 h-12 rounded-xl flex items-center justify-center mb-4 transition-colors">
              <TruckIcon size={24} className="text-blue-600" />
            </div>
            <h3 className="font-semibold text-gray-900 mb-2">Rastrear Envío</h3>
            <p className="text-sm text-gray-600">Sigue tu paquete en tiempo real</p>
          </button>

          <button
            onClick={() => navigate('/addresses')}
            className="card-hover text-left p-6 group"
          >
            <div className="bg-green-100 group-hover:bg-green-200 w-12 h-12 rounded-xl flex items-center justify-center mb-4 transition-colors">
              <PackageDeliveredIcon size={24} className="text-green-600" />
            </div>
            <h3 className="font-semibold text-gray-900 mb-2">Mis Direcciones</h3>
            <p className="text-sm text-gray-600">Gestiona tus direcciones guardadas</p>
          </button>
        </div>
      </div>
    </Layout>
  );
}
