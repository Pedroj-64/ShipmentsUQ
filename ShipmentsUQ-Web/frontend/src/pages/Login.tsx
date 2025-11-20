import { useState, type FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService, type LoginRequest } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { 
  PackageIcon, 
  EmailIcon, 
  LockIcon, 
  EyeIcon, 
  EyeOffIcon,
  AlertIcon,
  LoadingIcon 
} from '../components/icons';
import styles from './Login.module.scss';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const credentials: LoginRequest = { email, password };
      const response = await authService.login(credentials);

      if (response.success) {
        login(response.user, response.userType);
        
        if (response.userType === 'DELIVERER') {
          navigate('/deliverer/dashboard');
        } else if (response.userType === 'ADMIN') {
          navigate('/admin/dashboard');
        } else {
          navigate('/dashboard');
        }
      }
    } catch (err: any) {
      setError(err.response?.data?.error || 'Error al iniciar sesión. Verifica tus credenciales.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles['login-page']}>
      <div className={styles['login-container']}>
        {/* Logo y Header */}
        <div className={styles['login-header']}>
          <div className={styles.logo}>
            <PackageIcon size={48} className={styles['logo-icon']} strokeWidth={2} />
          </div>
          <h1>ShipmentsUQ</h1>
          <p>Bienvenido de nuevo</p>
        </div>

        {/* Card del formulario */}
        <div className={styles['login-card']}>
          {error && (
            <div className={`${styles.alert} ${styles['alert-error']}`}>
              <AlertIcon size={20} className={styles['alert-icon']} />
              <div className={styles['alert-content']}>
                <p>{error}</p>
              </div>
              <button
                onClick={() => setError('')}
                className={styles['alert-close']}
                aria-label="Cerrar mensaje"
              >
                <svg fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </button>
            </div>
          )}

          <form onSubmit={handleSubmit} className={styles['login-form']}>
            <div className={styles['form-group']}>
              <label htmlFor="email">Email o Documento</label>
              <div className={styles['input-wrapper']}>
                <EmailIcon size={20} className={styles['input-icon']} />
                <input
                  type="text"
                  id="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="tu@email.com"
                  required
                  autoComplete="username"
                />
              </div>
            </div>

            <div className={styles['form-group']}>
              <label htmlFor="password">Contraseña</label>
              <div className={`${styles['input-wrapper']} ${styles['has-toggle']}`}>
                <LockIcon size={20} className={styles['input-icon']} />
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className={styles['toggle-password']}
                  aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                >
                  {showPassword ? (
                    <EyeOffIcon size={20} />
                  ) : (
                    <EyeIcon size={20} />
                  )}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className={styles['submit-button']}
            >
              {loading ? (
                <>
                  <LoadingIcon size={20} className="animate-spin" />
                  <span>Iniciando sesión...</span>
                </>
              ) : (
                'Iniciar Sesión'
              )}
            </button>
          </form>

          <div className={styles['register-link']}>
            <p>
              ¿No tienes una cuenta?{' '}
              <Link to="/register">Regístrate aquí</Link>
            </p>
          </div>
        </div>

        {/* Footer */}
        <div className={styles['login-footer']}>
          <p>© 2025 ShipmentsUQ</p>
        </div>
      </div>
    </div>
  );
}
