import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import AddressAutocomplete from '../components/AddressAutocomplete';
import {
  LocationIcon as MapPinIcon,
  PlusIcon,
  CheckIcon,
  CloseIcon as XIcon,
  EditIcon,
  DeleteIcon as TrashIcon,
  CheckCircleIcon
} from '../components/icons';

interface Address {
  id: string;
  alias: string;
  address: string;
  city: string;
  latitude: number;
  longitude: number;
  isDefault: boolean;
}

export default function Addresses() {
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingAddress, setEditingAddress] = useState<Address | null>(null);
  const [formData, setFormData] = useState({
    alias: '',
    address: '',
    city: '',
    latitude: 0,
    longitude: 0,
    isDefault: false,
  });

  useEffect(() => {
    loadAddresses();
  }, []);

  const loadAddresses = () => {
    const mockAddresses: Address[] = [
      {
        id: '1',
        alias: 'Casa',
        address: 'Calle 15 #10-20',
        city: 'Armenia',
        latitude: 4.5389,
        longitude: -75.6719,
        isDefault: true,
      },
      {
        id: '2',
        alias: 'Oficina',
        address: 'Carrera 14 #25-50',
        city: 'Armenia',
        latitude: 4.5420,
        longitude: -75.6750,
        isDefault: false,
      },
    ];
    setAddresses(mockAddresses);
  };

  const handleAddressChange = (address: string, lat?: number, lon?: number) => {
    setFormData(prev => ({
      ...prev,
      address,
      latitude: lat || 0,
      longitude: lon || 0
    }));
  };

  const handleSave = () => {
    if (editingAddress) {
      setAddresses(addresses.map(addr => 
        addr.id === editingAddress.id ? { ...formData, id: addr.id } as Address : addr
      ));
    } else {
      const newAddress: Address = {
        ...formData,
        id: Date.now().toString(),
      };
      setAddresses([...addresses, newAddress]);
    }
    handleCancel();
  };

  const handleEdit = (address: Address) => {
    setEditingAddress(address);
    setFormData(address);
    setShowAddForm(true);
  };

  const handleDelete = (id: string) => {
    if (confirm('¿Estás seguro de eliminar esta dirección?')) {
      setAddresses(addresses.filter(addr => addr.id !== id));
    }
  };

  const handleCancel = () => {
    setShowAddForm(false);
    setEditingAddress(null);
    setFormData({
      alias: '',
      address: '',
      city: '',
      latitude: 0,
      longitude: 0,
      isDefault: false,
    });
  };

  return (
    <Layout>
      <div className="max-w-6xl">
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-3">
              <div className="bg-purple-100 p-3 rounded-xl">
                <MapPinIcon size={24} className="text-purple-600" />
              </div>
              <div>
                <h1 className="text-3xl font-bold text-gray-900">Mis Direcciones</h1>
                <p className="text-gray-600 mt-1">Gestiona tus direcciones guardadas</p>
              </div>
            </div>
            <button
              onClick={() => setShowAddForm(true)}
              className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-xl font-medium hover:shadow-lg transition-all"
            >
              <PlusIcon size={20} />
              Nueva Dirección
            </button>
          </div>
        </div>

        {showAddForm && (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 mb-8">
            <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center gap-2">
              <MapPinIcon size={20} className="text-purple-600" />
              {editingAddress ? 'Editar Dirección' : 'Nueva Dirección'}
            </h2>
            <div className="space-y-6">
              <div className="grid md:grid-cols-2 gap-6">
                <div>
                  <label className="label">Alias <span className="text-red-500">*</span></label>
                  <input
                    type="text"
                    value={formData.alias}
                    onChange={(e) => setFormData({ ...formData, alias: e.target.value })}
                    className="input"
                    placeholder="Ej: Casa, Oficina, Trabajo"
                  />
                </div>
                <div>
                  <label className="label">Ciudad</label>
                  <input
                    type="text"
                    value={formData.city}
                    onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                    className="input"
                    placeholder="Ej: Armenia, Bogotá"
                  />
                </div>
              </div>
              
              <AddressAutocomplete
                value={formData.address}
                onChange={handleAddressChange}
                label="Dirección Completa"
                placeholder="Buscar o hacer click en el mapa..."
                required
                showMap={true}
              />

              <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-xl">
                <input
                  type="checkbox"
                  id="isDefault"
                  checked={formData.isDefault}
                  onChange={(e) => setFormData({ ...formData, isDefault: e.target.checked })}
                  className="w-4 h-4 text-purple-600 rounded focus:ring-purple-500"
                />
                <label htmlFor="isDefault" className="text-sm font-medium text-gray-700">
                  Establecer como dirección predeterminada
                </label>
              </div>
              
              <div className="flex gap-3 justify-end">
                <button 
                  onClick={handleCancel} 
                  className="flex items-center gap-2 px-6 py-3 bg-gray-100 text-gray-700 rounded-xl font-medium hover:bg-gray-200 transition-colors"
                >
                  <XIcon size={18} />
                  Cancelar
                </button>
                <button 
                  onClick={handleSave} 
                  className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-xl font-medium hover:shadow-lg transition-all"
                >
                  <CheckIcon size={18} />
                  {editingAddress ? 'Guardar Cambios' : 'Agregar Dirección'}
                </button>
              </div>
            </div>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {addresses.length === 0 ? (
            <div className="col-span-2 bg-white rounded-2xl shadow-sm border border-gray-200 p-12 text-center">
              <div className="inline-flex items-center justify-center w-20 h-20 bg-purple-100 rounded-full mb-4">
                <MapPinIcon size={40} className="text-purple-600" />
              </div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">No tienes direcciones guardadas</h3>
              <p className="text-gray-600 mb-6">Agrega tu primera dirección para agilizar tus envíos</p>
              <button 
                onClick={() => setShowAddForm(true)} 
                className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-xl font-medium hover:shadow-lg transition-all"
              >
                <PlusIcon size={18} />
                Agregar Primera Dirección
              </button>
            </div>
          ) : (
            addresses.map((address) => (
              <div key={address.id} className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 hover:shadow-lg transition-all group">
                <div className="flex justify-between items-start mb-4">
                  <div className="flex items-start gap-3">
                    <div className="bg-purple-100 p-2.5 rounded-lg">
                      <MapPinIcon size={20} className="text-purple-600" />
                    </div>
                    <div>
                      <h3 className="text-lg font-semibold text-gray-900">{address.alias}</h3>
                      {address.isDefault && (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800 mt-2">
                          <CheckCircleIcon size={14} />
                          Predeterminada
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                      onClick={() => handleEdit(address)}
                      className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                      title="Editar"
                    >
                      <EditIcon size={16} />
                    </button>
                    <button
                      onClick={() => handleDelete(address.id)}
                      className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="Eliminar"
                    >
                      <TrashIcon size={16} />
                    </button>
                  </div>
                </div>
                <div className="space-y-2">
                  <p className="text-gray-700 flex items-start gap-2">
                    <MapPinIcon size={16} className="text-gray-400 mt-0.5 flex-shrink-0" />
                    <span className="flex-1">{address.address}</span>
                  </p>
                  <p className="text-gray-500 text-sm flex items-center gap-2">
                    <MapPinIcon size={16} className="text-gray-400" />
                    {address.city}
                  </p>
                  <p className="text-xs text-gray-400 flex items-center gap-1 mt-2">
                    <MapPinIcon size={12} />
                    {address.latitude.toFixed(6)}, {address.longitude.toFixed(6)}
                  </p>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </Layout>
  );
}
