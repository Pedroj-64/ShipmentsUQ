import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import CreateShipment from './pages/CreateShipment';
import Tracking from './pages/Tracking';
import Payments from './pages/Payments';
import Addresses from './pages/Addresses';
import Profile from './pages/Profile';
import DelivererDashboard from './pages/DelivererDashboard';
import './styles/main.scss';

function App() {
  console.log('🚀 App component rendering');
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/shipments/create" element={<CreateShipment />} />
          <Route path="/tracking" element={<Tracking />} />
          <Route path="/payments" element={<Payments />} />
          <Route path="/addresses" element={<Addresses />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/deliverer/dashboard" element={<DelivererDashboard />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
