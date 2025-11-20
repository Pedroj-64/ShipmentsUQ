import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import { useAuth } from '../context/AuthContext';
import {
  UserIcon,
  PackageIcon,
  CheckCircleIcon,
  EditIcon,
  EmailIcon as MailIcon,
  LockIcon,
  AlertIcon as AlertTriangleIcon,
  KeyIcon
} from '../components/icons';

interface UserProfile {
  name: string;
  email: string;
  phone: string;
  role: string;
  idNumber: string;
  createdAt: string;
  totalShipments: number;
  activeShipments: number;
}

export default function Profile() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [editingSection, setEditingSection] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  useEffect(() => {
    loadProfile();
  }, [user]);

  const loadProfile = () => {
    const mockProfile: UserProfile = {
      name: user?.name || 'Paula Moreno',
      email: user?.email || 'paula@example.com',
      phone: '+57 312 345 6789',
      role: user?.role || 'USER',
      idNumber: '1234567890',
      createdAt: '2024-01-15',
      totalShipments: 12,
      activeShipments: 3,
    };
    setProfile(mockProfile);
    setFormData({
      ...formData,
      name: mockProfile.name,
      phone: mockProfile.phone,
    });
  };

  const handleEditSection = (section: string) => {
    setEditingSection(section);
  };

  const handleCancelEdit = () => {
    setEditingSection(null);
    if (profile) {
      setFormData({
        ...formData,
        name: profile.name,
        phone: profile.phone,
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
    }
  };

  const handleSavePersonalInfo = () => {
    if (profile) {
      setProfile({ ...profile, name: formData.name, phone: formData.phone });
    }
    setEditingSection(null);
  };

  const handleChangePassword = () => {
    if (formData.newPassword !== formData.confirmPassword) {
      alert('Las contraseñas no coinciden');
      return;
    }
    if (formData.newPassword.length < 6) {
      alert('La contraseña debe tener al menos 6 caracteres');
      return;
    }
    // Aquí iría la lógica de cambio de contraseña
    alert('Contraseña actualizada exitosamente');
    setEditingSection(null);
    setFormData({
      ...formData,
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    });
  };

  const getRoleName = (role: string) => {
    const roles: { [key: string]: string } = {
      'USER': 'Usuario',
      'DELIVERER': 'Repartidor',
      'ADMIN': 'Administrador'
    };
    return roles[role] || role;
  };

  const getRoleBadge = (role: string) => {
    const badges: { [key: string]: string } = {
      'USER': 'bg-blue-100 text-blue-700',
      'DELIVERER': 'bg-green-100 text-green-700',
      'ADMIN': 'bg-purple-100 text-purple-700'
    };
    return badges[role] || 'bg-gray-100 text-gray-700';
  };

  if (!profile) return null;

  return (
    <Layout>
      <div className="max-w-5xl">
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-4">
            <div className="bg-purple-100 p-3 rounded-xl">
              <UserIcon size={24} className="text-purple-600" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Mi Perfil</h1>
              <p className="text-gray-600 mt-1">Gestiona tu información personal y configuración</p>
            </div>
          </div>
        </div>

        <div className="grid md:grid-cols-3 gap-6 mb-8">
          <div className="card text-center">
            <div className="w-24 h-24 rounded-full bg-gradient-to-br from-purple-600 to-indigo-700 text-white flex items-center justify-center text-3xl font-bold mx-auto mb-4">
              {profile.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)}
            </div>
            <h3 className="text-lg font-semibold text-gray-900">{profile.name}</h3>
            <p className="text-sm text-gray-600 mt-1">{profile.email}</p>
            <span className={`inline-block mt-3 px-3 py-1 rounded-full text-xs font-medium ${getRoleBadge(profile.role)}`}>
              {getRoleName(profile.role)}
            </span>
          </div>

          <div className="card">
            <div className="flex items-center gap-3 mb-3">
              <div className="bg-blue-100 p-2 rounded-lg">
                <PackageIcon size={20} className="text-blue-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Total de Envíos</p>
                <p className="text-xl font-bold text-gray-900">{profile.totalShipments}</p>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="flex items-center gap-3 mb-3">
              <div className="bg-green-100 p-2 rounded-lg">
                <CheckCircleIcon size={20} className="text-green-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Envíos Activos</p>
                <p className="text-xl font-bold text-gray-900">{profile.activeShipments}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="space-y-6">
          {/* Información Personal */}
          <div className="card">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-semibold text-gray-900 flex items-center gap-2">
                <UserIcon size={20} className="text-purple-600" />
                Información Personal
              </h2>
              {editingSection !== 'personal' && (
                <button
                  onClick={() => handleEditSection('personal')}
                  className="flex items-center gap-2 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition-colors text-sm"
                >
                  <EditIcon size={16} />
                  Editar
                </button>
              )}
            </div>

            {editingSection === 'personal' ? (
              <div className="space-y-4">
                <div className="grid md:grid-cols-2 gap-4">
                  <div>
                    <label className="label">Nombre Completo</label>
                    <input
                      type="text"
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      className="input"
                    />
                  </div>
                  <div>
                    <label className="label">Teléfono</label>
                    <input
                      type="tel"
                      value={formData.phone}
                      onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                      className="input"
                    />
                  </div>
                </div>
                <div className="flex gap-3 justify-end">
                  <button onClick={handleCancelEdit} className="btn btn-secondary">
                    Cancelar
                  </button>
                  <button onClick={handleSavePersonalInfo} className="btn btn-primary">
                    Guardar Cambios
                  </button>
                </div>
              </div>
            ) : (
              <div className="grid md:grid-cols-2 gap-6">
                <div>
                  <label className="text-sm text-gray-600">Nombre Completo</label>
                  <p className="text-gray-900 font-medium mt-1">{profile.name}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600">Teléfono</label>
                  <p className="text-gray-900 font-medium mt-1">{profile.phone}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600">Cédula</label>
                  <p className="text-gray-900 font-medium mt-1">{profile.idNumber}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600">Miembro desde</label>
                  <p className="text-gray-900 font-medium mt-1">
                    {new Date(profile.createdAt).toLocaleDateString('es-CO', { 
                      year: 'numeric', 
                      month: 'long', 
                      day: 'numeric' 
                    })}
                  </p>
                </div>
              </div>
            )}
          </div>

          {/* Información de Cuenta */}
          <div className="card">
            <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center gap-2">
              <MailIcon size={20} className="text-purple-600" />
              Información de Cuenta
            </h2>
            <div className="grid md:grid-cols-2 gap-6">
              <div>
                <label className="text-sm text-gray-600">Correo Electrónico</label>
                <p className="text-gray-900 font-medium mt-1">{profile.email}</p>
              </div>
              <div>
                <label className="text-sm text-gray-600">Rol</label>
                <p className="text-gray-900 font-medium mt-1">{getRoleName(profile.role)}</p>
              </div>
            </div>
          </div>

          {/* Seguridad */}
          <div className="card">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-semibold text-gray-900 flex items-center gap-2">
                <LockIcon size={20} className="text-purple-600" />
                Seguridad
              </h2>
              {editingSection !== 'security' && (
                <button
                  onClick={() => handleEditSection('security')}
                  className="flex items-center gap-2 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition-colors text-sm"
                >
                  <KeyIcon size={16} />
                  Cambiar Contraseña
                </button>
              )}
            </div>

            {editingSection === 'security' ? (
              <div className="space-y-4">
                <div>
                  <label className="label">Contraseña Actual</label>
                  <input
                    type="password"
                    value={formData.currentPassword}
                    onChange={(e) => setFormData({ ...formData, currentPassword: e.target.value })}
                    className="input"
                    placeholder="Ingresa tu contraseña actual"
                  />
                </div>
                <div>
                  <label className="label">Nueva Contraseña</label>
                  <input
                    type="password"
                    value={formData.newPassword}
                    onChange={(e) => setFormData({ ...formData, newPassword: e.target.value })}
                    className="input"
                    placeholder="Mínimo 6 caracteres"
                  />
                </div>
                <div>
                  <label className="label">Confirmar Nueva Contraseña</label>
                  <input
                    type="password"
                    value={formData.confirmPassword}
                    onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                    className="input"
                    placeholder="Repite la nueva contraseña"
                  />
                </div>
                <div className="flex gap-3 justify-end">
                  <button onClick={handleCancelEdit} className="btn btn-secondary">
                    Cancelar
                  </button>
                  <button onClick={handleChangePassword} className="btn btn-primary">
                    Actualizar Contraseña
                  </button>
                </div>
              </div>
            ) : (
              <div>
                <label className="text-sm text-gray-600">Contraseña</label>
                <p className="text-gray-900 font-medium mt-1">••••••••</p>
                <p className="text-xs text-gray-500 mt-2">
                  Última actualización: Hace 30 días
                </p>
              </div>
            )}
          </div>

          {/* Zona de Peligro */}
          <div className="card border-2 border-red-200 bg-red-50">
            <h2 className="text-xl font-semibold text-red-900 mb-4 flex items-center gap-2">
              <AlertTriangleIcon size={20} className="text-red-600" />
              Zona de Peligro
            </h2>
            <p className="text-sm text-red-800 mb-4">
              Una vez que elimines tu cuenta, no hay vuelta atrás. Por favor, ten certeza.
            </p>
            <button className="btn btn-danger">
              Eliminar Cuenta
            </button>
          </div>
        </div>
      </div>
    </Layout>
  );
}
