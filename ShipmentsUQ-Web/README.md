# 🚀 ShipmentsUQ - Versión Web

Sistema de gestión de envíos con interfaz web moderna. Este proyecto extiende la aplicación JavaFX existente con una REST API y frontend web usando React.

## 📋 Tabla de Contenidos
- [Características](#características)
- [Arquitectura](#arquitectura)
- [Requisitos Previos](#requisitos-previos)
- [Instalación](#instalación)
- [Uso](#uso)
- [API Documentation](#api-documentation)

## ✨ Características

### Backend (Spring Boot REST API)
- ✅ **Autenticación** - Login y registro de usuarios/repartidores
- ✅ **Gestión de Envíos** - CRUD completo de envíos
- ✅ **Pagos** - Procesamiento y gestión de métodos de pago
- ✅ **CORS** - Configurado para desarrollo local
- ✅ **Reutilización** - Usa toda la lógica de negocio existente

### Frontend (React + TypeScript)
- 🎨 **UI Moderna** - Diseño responsive con Tailwind CSS
- ⚡ **Performance** - Optimizado con Vite
- 📱 **Mobile-First** - Adaptable a todos los dispositivos
- 🔐 **Seguridad** - Manejo seguro de autenticación

## 🏗️ Arquitectura

```
ShipmentsUQ/
├── ShipmentsUQ-SameDay/          # Aplicación JavaFX + API REST
│   └── src/main/java/
│       └── .../webapp/
│           ├── WebApplication.java     # Spring Boot App
│           ├── api/                   # REST Controllers
│           │   ├── AuthRestController.java
│           │   ├── ShipmentRestController.java
│           │   └── PaymentRestController.java
│           └── config/                # Configuración
│               ├── CorsConfig.java
│               └── WebConfig.java
└── ShipmentsUQ-Web/
    └── frontend/                     # React App
        ├── src/
        ├── public/
        └── package.json
```

## 📦 Requisitos Previos

- **Java JDK 21+**
- **Maven 3.8+**
- **Node.js 18+** y **npm 9+**
- Navegador web moderno (Chrome, Firefox, Edge)

## 🚀 Instalación

### 1. Backend (API REST)

```bash
# Navegar al proyecto
cd ShipmentsUQ/ShipmentsUQ-SameDay

# Compilar con Maven
mvn clean install

# Iniciar el servidor web
mvn exec:java -Dexec.mainClass="co.edu.uniquindio.sameday.shipmentsuqsameday.webapp.WebApplication"
```

El servidor estará disponible en: `http://localhost:8080`

### 2. Frontend (React)

```bash
# Navegar al frontend
cd ShipmentsUQ-Web/frontend

# Instalar dependencias
npm install

# Iniciar servidor de desarrollo
npm run dev
```

El frontend estará disponible en: `http://localhost:3000` o `http://localhost:5173` (Vite)

## 💻 Uso

### Opción 1: Desde la aplicación JavaFX
1. Ejecutar la aplicación JavaFX normal
2. En la pantalla de login, hacer clic en **"🌐 Abrir Versión Web"**
3. El servidor web se iniciará automáticamente y abrirá el navegador

### Opción 2: Inicio manual
1. Iniciar backend: `mvn exec:java` (en ShipmentsUQ-SameDay)
2. Iniciar frontend: `npm run dev` (en ShipmentsUQ-Web/frontend)
3. Abrir navegador en `http://localhost:3000`

## 📚 API Documentation

### Endpoints Principales

#### Autenticación

**POST** `/api/auth/login`
```json
{
  "email": "usuario@example.com",
  "password": "password123"
}
```

**POST** `/api/auth/register`
```json
{
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "phone": "+57 300 123 4567",
  "password": "password123",
  "city": "Armenia"
}
```

#### Envíos

**GET** `/api/shipments/user/{userId}` - Obtener envíos de usuario
**GET** `/api/shipments/{id}` - Obtener envío específico
**POST** `/api/shipments/quote` - Calcular cotización
**PUT** `/api/shipments/{id}/status` - Actualizar estado

#### Pagos

**GET** `/api/payments/history` - Historial de pagos
**POST** `/api/payments/process` - Procesar pago
**GET** `/api/payments/methods` - Métodos de pago guardados
**DELETE** `/api/payments/methods/{id}` - Eliminar método
**PUT** `/api/payments/methods/{id}/alias` - Actualizar alias

### Health Check

**GET** `/api/auth/health`
```json
{
  "status": "UP",
  "service": "ShipmentsUQ API",
  "version": "1.0.0"
}
```

## 🛠️ Tecnologías Utilizadas

### Backend
- **Spring Boot 3.2** - Framework REST API
- **Spring Web** - Controladores REST
- **JWT** - Autenticación
- **Gson** - Serialización JSON
- **Maven** - Gestión de dependencias

### Frontend
- **React 18** - Library UI
- **TypeScript** - Type safety
- **Vite** - Build tool
- **Tailwind CSS** - Styling
- **Axios** - HTTP client
- **React Router** - Routing

## 🤝 Integración con Sistema Existente

El sistema web reutiliza **100%** de la lógica de negocio existente:

- ✅ **Modelos** - User, Shipment, Payment, etc.
- ✅ **Servicios** - UserService, ShipmentService, PaymentService
- ✅ **Controladores** - LoginController, PaymentsController, etc.

No se duplica código, solo se expone vía REST API.

## 📝 Notas Importantes

1. **Puerto Backend**: 8080 (configurable en `application.properties`)
2. **Puerto Frontend**: 3000 o 5173 (según Vite/React)
3. **CORS**: Configurado para desarrollo local
4. **Datos**: Comparte la misma fuente de datos con la app JavaFX

## 👥 Autores

**MargaDev-Society** - Equipo de desarrollo ShipmentsUQ

## 📄 Licencia

Este proyecto es parte de ShipmentsUQ - Universidad del Quindío

---

**🎯 Versión**: 1.0.0  
**📅 Última actualización**: Noviembre 2025
