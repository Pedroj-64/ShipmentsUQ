import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import {
  CreditCardIcon,
  PlusIcon,
  CheckIcon,
  CloseIcon as XIcon,
  EditIcon,
  DeleteIcon as TrashIcon,
  BanknoteIcon as BanknotesIcon,
  LockIcon,
  AlertIcon,
  CheckCircleIcon
} from '../components/icons';

interface PaymentMethod {
  id: string;
  type: 'CREDIT_CARD' | 'DEBIT_CARD' | 'PSE';
  cardNumber: string;
  cardHolder: string;
  expiryDate: string;
  isDefault: boolean;
}

export default function Payments() {
  const [paymentMethods, setPaymentMethods] = useState<PaymentMethod[]>([]);
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingPayment, setEditingPayment] = useState<PaymentMethod | null>(null);
  const [formData, setFormData] = useState({
    type: 'CREDIT_CARD' as 'CREDIT_CARD' | 'DEBIT_CARD' | 'PSE',
    cardNumber: '',
    cardHolder: '',
    expiryDate: '',
    cvv: '',
    isDefault: false,
  });

  useEffect(() => {
    loadPaymentMethods();
  }, []);

  const loadPaymentMethods = () => {
    const mockPayments: PaymentMethod[] = [
      {
        id: '1',
        type: 'CREDIT_CARD',
        cardNumber: '**** **** **** 4532',
        cardHolder: 'Paula Moreno',
        expiryDate: '12/25',
        isDefault: true,
      },
      {
        id: '2',
        type: 'DEBIT_CARD',
        cardNumber: '**** **** **** 8901',
        cardHolder: 'Paula Moreno',
        expiryDate: '08/26',
        isDefault: false,
      },
    ];
    setPaymentMethods(mockPayments);
  };

  const getCardType = (number: string) => {
    if (number.startsWith('4')) return 'Visa';
    if (number.startsWith('5')) return 'Mastercard';
    if (number.startsWith('3')) return 'Amex';
    return 'Card';
  };

  const getCardIcon = (number: string) => {
    const type = getCardType(number);
    const baseClass = "w-10 h-7 rounded";
    
    if (type === 'Visa') {
      return <div className={`${baseClass} bg-blue-600 flex items-center justify-center text-white text-xs font-bold`}>VISA</div>;
    }
    if (type === 'Mastercard') {
      return <div className={`${baseClass} bg-red-600 flex items-center justify-center text-white text-xs font-bold`}>MC</div>;
    }
    if (type === 'Amex') {
      return <div className={`${baseClass} bg-blue-800 flex items-center justify-center text-white text-xs font-bold`}>AMEX</div>;
    }
    return (
      <div className={`${baseClass} bg-gray-600 flex items-center justify-center`}>
        <CreditCardIcon size={16} className="text-white" />
      </div>
    );
  };

  const getTypeIcon = (type: 'CREDIT_CARD' | 'DEBIT_CARD' | 'PSE') => {
    switch (type) {
      case 'CREDIT_CARD':
        return <CreditCardIcon size={24} className="text-blue-600" />;
      case 'DEBIT_CARD':
        return <BanknotesIcon size={24} className="text-green-600" />;
      case 'PSE':
        return <LockIcon size={24} className="text-orange-600" />;
    }
  };

  const formatCardNumber = (value: string) => {
    const cleaned = value.replace(/\s/g, '');
    const chunks = cleaned.match(/.{1,4}/g) || [];
    return chunks.join(' ');
  };

  const handleCardNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/\s/g, '');
    if (value.length <= 16 && /^\d*$/.test(value)) {
      setFormData({ ...formData, cardNumber: formatCardNumber(value) });
    }
  };

  const handleExpiryChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    let value = e.target.value.replace(/\D/g, '');
    if (value.length >= 2) {
      value = value.slice(0, 2) + '/' + value.slice(2, 4);
    }
    if (value.length <= 5) {
      setFormData({ ...formData, expiryDate: value });
    }
  };

  const handleSave = () => {
    if (editingPayment) {
      setPaymentMethods(paymentMethods.map(pm => 
        pm.id === editingPayment.id ? {
          ...pm,
          type: formData.type,
          cardHolder: formData.cardHolder,
          expiryDate: formData.expiryDate,
          isDefault: formData.isDefault
        } : pm
      ));
    } else {
      const lastFour = formData.cardNumber.replace(/\s/g, '').slice(-4);
      const newPayment: PaymentMethod = {
        id: Date.now().toString(),
        type: formData.type,
        cardNumber: '**** **** **** ' + lastFour,
        cardHolder: formData.cardHolder,
        expiryDate: formData.expiryDate,
        isDefault: formData.isDefault,
      };
      setPaymentMethods([...paymentMethods, newPayment]);
    }
    handleCancel();
  };

  const handleEdit = (payment: PaymentMethod) => {
    setEditingPayment(payment);
    setFormData({
      type: payment.type,
      cardNumber: '',
      cardHolder: payment.cardHolder,
      expiryDate: payment.expiryDate,
      cvv: '',
      isDefault: payment.isDefault,
    });
    setShowAddForm(true);
  };

  const handleDelete = (id: string) => {
    if (confirm('¿Estás seguro de eliminar este método de pago?')) {
      setPaymentMethods(paymentMethods.filter(pm => pm.id !== id));
    }
  };

  const handleCancel = () => {
    setShowAddForm(false);
    setEditingPayment(null);
    setFormData({
      type: 'CREDIT_CARD',
      cardNumber: '',
      cardHolder: '',
      expiryDate: '',
      cvv: '',
      isDefault: false,
    });
  };

  return (
    <Layout>
      <div className="max-w-6xl mx-auto">
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-3">
              <div className="bg-purple-100 p-3 rounded-xl">
                <CreditCardIcon size={24} className="text-purple-600" />
              </div>
              <div>
                <h1 className="text-3xl font-bold text-gray-900">Métodos de Pago</h1>
                <p className="text-gray-600 mt-1">Gestiona tus tarjetas y métodos de pago</p>
              </div>
            </div>
            <button
              onClick={() => setShowAddForm(true)}
              className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-xl font-medium hover:shadow-lg transition-all"
            >
              <PlusIcon size={20} />
              Agregar Método
            </button>
          </div>
        </div>

        {showAddForm && (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 mb-8">
            <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center gap-2">
              <CreditCardIcon size={20} className="text-purple-600" />
              {editingPayment ? 'Editar Método de Pago' : 'Nuevo Método de Pago'}
            </h2>
            
            <div className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-3">Tipo de Tarjeta</label>
                <div className="grid grid-cols-3 gap-4">
                  {(['CREDIT_CARD', 'DEBIT_CARD', 'PSE'] as const).map((type) => (
                    <button
                      key={type}
                      type="button"
                      onClick={() => setFormData({ ...formData, type })}
                      className={`p-4 rounded-xl border-2 transition-all ${
                        formData.type === type
                          ? 'border-purple-600 bg-purple-50'
                          : 'border-gray-200 hover:border-gray-300'
                      }`}
                    >
                      <div className="text-center">
                        <div className="flex justify-center mb-2">
                          {getTypeIcon(type)}
                        </div>
                        <p className="text-sm font-medium text-gray-700">
                          {type === 'CREDIT_CARD' ? 'Crédito' : type === 'DEBIT_CARD' ? 'Débito' : 'PSE'}
                        </p>
                      </div>
                    </button>
                  ))}
                </div>
              </div>

              {formData.type !== 'PSE' ? (
                <>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Número de Tarjeta <span className="text-red-500">*</span>
                    </label>
                    <div className="relative">
                      <CreditCardIcon size={20} className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                      <input
                        type="text"
                        value={formData.cardNumber}
                        onChange={handleCardNumberChange}
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                        placeholder="1234 5678 9012 3456"
                        disabled={!!editingPayment}
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Titular de la Tarjeta <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={formData.cardHolder}
                      onChange={(e) => setFormData({ ...formData, cardHolder: e.target.value.toUpperCase() })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                      placeholder="NOMBRE COMO APARECE EN LA TARJETA"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-6">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Fecha de Expiración <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="text"
                        value={formData.expiryDate}
                        onChange={handleExpiryChange}
                        className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                        placeholder="MM/AA"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        CVV <span className="text-red-500">*</span>
                      </label>
                      <div className="relative">
                        <LockIcon size={20} className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                        <input
                          type="password"
                          value={formData.cvv}
                          onChange={(e) => {
                            const value = e.target.value;
                            if (value.length <= 4 && /^\d*$/.test(value)) {
                              setFormData({ ...formData, cvv: value });
                            }
                          }}
                          className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                          placeholder="123"
                          maxLength={4}
                        />
                      </div>
                    </div>
                  </div>
                </>
              ) : (
                <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 flex items-start gap-3">
                  <AlertIcon size={20} className="text-blue-600 flex-shrink-0 mt-0.5" />
                  <p className="text-sm text-blue-800">
                    PSE te permite pagar directamente desde tu cuenta bancaria de forma segura
                  </p>
                </div>
              )}

              <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-xl">
                <input
                  type="checkbox"
                  id="isDefault"
                  checked={formData.isDefault}
                  onChange={(e) => setFormData({ ...formData, isDefault: e.target.checked })}
                  className="w-4 h-4 text-purple-600 rounded focus:ring-purple-500"
                />
                <label htmlFor="isDefault" className="text-sm font-medium text-gray-700">
                  Establecer como método de pago predeterminado
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
                  {editingPayment ? 'Guardar Cambios' : 'Agregar Método'}
                </button>
              </div>
            </div>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {paymentMethods.length === 0 ? (
            <div className="col-span-full bg-white rounded-2xl shadow-sm border border-gray-200 p-12 text-center">
              <div className="inline-flex items-center justify-center w-20 h-20 bg-purple-100 rounded-full mb-4">
                <CreditCardIcon size={40} className="text-purple-600" />
              </div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">No tienes métodos de pago guardados</h3>
              <p className="text-gray-600 mb-6">Agrega un método de pago para facilitar tus transacciones</p>
              <button 
                onClick={() => setShowAddForm(true)} 
                className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-xl font-medium hover:shadow-lg transition-all"
              >
                <PlusIcon size={18} />
                Agregar Primer Método
              </button>
            </div>
          ) : (
            paymentMethods.map((payment) => (
              <div key={payment.id} className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 hover:shadow-lg transition-all group relative overflow-hidden">
                <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-purple-500/10 to-indigo-500/10 rounded-full -mr-16 -mt-16"></div>
                
                <div className="relative">
                  <div className="flex justify-between items-start mb-4">
                    {getCardIcon(payment.cardNumber)}
                    <div className="flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button
                        onClick={() => handleEdit(payment)}
                        className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                        title="Editar"
                      >
                        <EditIcon size={16} />
                      </button>
                      <button
                        onClick={() => handleDelete(payment.id)}
                        className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                        title="Eliminar"
                      >
                        <TrashIcon size={16} />
                      </button>
                    </div>
                  </div>

                  <div className="space-y-3">
                    <p className="text-lg font-mono font-semibold text-gray-900">
                      {payment.cardNumber}
                    </p>
                    <p className="text-sm text-gray-700 font-medium uppercase">
                      {payment.cardHolder}
                    </p>
                    <div className="flex justify-between items-center pt-2">
                      <span className="text-xs text-gray-500">
                        Expira: {payment.expiryDate}
                      </span>
                      <span className={`text-xs px-2.5 py-1 rounded-full font-medium ${
                        payment.type === 'CREDIT_CARD' 
                          ? 'bg-blue-100 text-blue-700' 
                          : payment.type === 'DEBIT_CARD'
                          ? 'bg-green-100 text-green-700'
                          : 'bg-orange-100 text-orange-700'
                      }`}>
                        {payment.type === 'CREDIT_CARD' ? 'Crédito' : payment.type === 'DEBIT_CARD' ? 'Débito' : 'PSE'}
                      </span>
                    </div>
                    {payment.isDefault && (
                      <div className="pt-2 border-t border-gray-100">
                        <span className="inline-flex items-center gap-1 text-xs font-medium text-purple-700">
                          <CheckCircleIcon size={14} />
                          Predeterminado
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </Layout>
  );
}
