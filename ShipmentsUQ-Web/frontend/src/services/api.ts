import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Tipos para TypeScript
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  phone: string;
  password: string;
  city: string;
}

export interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  role: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  userType: 'USER' | 'DELIVERER' | 'ADMIN';
  user: User;
}

// Servicios de autenticación
export const authService = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/login', credentials);
    return response.data;
  },

  register: async (userData: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/register', userData);
    return response.data;
  },

  health: async () => {
    const response = await api.get('/auth/health');
    return response.data;
  },
};

// Servicios de envíos
export const shipmentService = {
  getUserShipments: async (userId: string) => {
    const response = await api.get(`/shipments/user/${userId}`);
    return response.data;
  },

  getShipmentById: async (id: string) => {
    const response = await api.get(`/shipments/${id}`);
    return response.data;
  },

  createQuote: async (quoteData: any) => {
    const response = await api.post('/shipments/quote', quoteData);
    return response.data;
  },
};

// Servicios de pagos
export const paymentService = {
  getPaymentHistory: async () => {
    const response = await api.get('/payments/history');
    return response.data;
  },

  getMethods: async () => {
    const response = await api.get('/payments/methods');
    return response.data;
  },

  processPayment: async (paymentData: any) => {
    const response = await api.post('/payments/process', paymentData);
    return response.data;
  },
};

export default api;
