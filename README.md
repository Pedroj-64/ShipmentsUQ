# 📦 ShipmentsUQ - Sistema de Gestión de Envíos Urbanos

> *"Entregando el futuro, un paquete a la vez"* 🚀

## 🎯 ¿Qué es ShipmentsUQ?

ShipmentsUQ es una aplicación de escritorio desarrollada en **JavaFX** para la gestión integral de envíos urbanos. El sistema permite a usuarios y administradores gestionar paquetes, rastrear entregas en tiempo real, calcular tarifas dinámicas y coordinar repartidores, todo desde una interfaz intuitiva y moderna.

### ✨ Características Principales

- 📍 **Rastreo en tiempo real** con sistema de coordenadas cartesianas y GPS
- 🗺️ **Sistema dual de coordenadas** (Grid cartesiano + GPS real)
- 🌐 **Geocodificación inversa** para convertir coordenadas GPS en direcciones
- 🔄 **Integración de mapas** con adaptador entre sistemas Grid y Real
- 💰 **Cálculo automático de tarifas** basado en peso, volumen, distancia y prioridad
- 👥 **Gestión de usuarios** (clientes, repartidores y administradores)
- 📊 **Dashboard administrativo** con métricas y estadísticas
- 💳 **Sistema de pagos** con múltiples métodos
- 🔔 **Notificaciones** de cambios de estado
- 📱 **Generación de comprobantes** en HTML
- 🔄 **Sistema de deshacer/rehacer** operaciones críticas
- 🧪 **Suite completa de pruebas unitarias** con JUnit 5

## 👨‍💻 Creadores

Este proyecto fue desarrollado con ❤️ por estudiantes de **Ingeniería de Sistemas** de la **Universidad del Quindío**:

| Desarrollador | GitHub | Rol |
|--------------|--------|-----|
| **Pedro José Soto Rivera** | [@Pedroj-64](https://github.com/Pedroj-64) | Ingeniero en Proceso |
| **María José Valencia** | NA/NA | Ingeniera en Proceso, Alias(**NEGRITA.DEV**) |

## 🏗️ Arquitectura y Patrones de Diseño

El proyecto implementa una arquitectura robusta basada en **patrones de diseño GoF (Gang of Four)** y mejores prácticas de desarrollo:

### 🎨 Patrones Creacionales
- **Singleton**: Gestión de servicios y estado de la aplicación
- **Builder**: Construcción flexible de entidades complejas
- **Factory**: Creación de servicios decorados y objetos de negocio

### 🏛️ Patrones Estructurales
- **Decorator**: Extensión dinámica de funcionalidades de servicios (validación, logging, notificaciones)
- **Adapter**: Adaptación de direcciones y coordenadas GPS al sistema Grid cartesiano
- **Facade**: Unificación de sistemas de coordenadas (MapCoordinateIntegrationService)
- **Composite**: Composición jerárquica de entidades de envío
- **Repository**: Abstracción de la capa de persistencia de datos

### 🎭 Patrones de Comportamiento
- **Strategy**: Algoritmos intercambiables de cálculo (distancia, tarifas, pagos)
- **Observer**: Sistema de notificaciones y eventos
- **Command**: Operaciones reversibles (deshacer/rehacer)
- **Template Method**: Definición de flujos de trabajo en clases base
- **State**: Gestión de estados de envíos y repartidores

## 🛠️ Tecnologías Utilizadas

```
☕ Java 17+
🎨 JavaFX 21
🔨 Maven
📝 Lombok
💾 Persistencia en memoria (serialización)
🗺️ Sistema de coordenadas dual (Grid cartesiano + GPS)
🌐 Geocodificación inversa (Nominatim OpenStreetMap)
🧪 JUnit 5 para pruebas unitarias
📐 Algoritmos: Manhattan (Grid), Haversine (GPS)
```

## 📁 Estructura del Proyecto

```
ShipmentsUQ-SameDay/
├── src/main/java/
│   └── co.edu.uniquindio.sameday.shipmentsuqsameday/
│       ├── model/              # Entidades y lógica de negocio
│       │   ├── command/        # Patrón Command
│       │   ├── decorator/      # Patrón Decorator
│       │   ├── dto/           # Data Transfer Objects
│       │   ├── enums/         # Enumeraciones (estados, roles, etc.)
│       │   ├── interfaces/    # Interfaces de estrategias
│       │   ├── strategy/      # Estrategias de coordenadas (Grid, GPS)
│       │   ├── repository/    # Capa de persistencia
│       │   ├── service/       # Lógica de negocio
│       │   └── util/          # Utilidades
│       ├── mapping/           # Servicios de coordenadas y mapas
│       │   ├── Coordinates.java           # Coordenadas GPS
│       │   ├── Address.java               # Modelo de dirección
│       │   ├── MapCoordinateIntegrationService.java  # Facade
│       │   ├── RealMapService.java        # Adapter GPS↔Grid
│       │   └── ReverseGeocoder.java       # Geocodificación
│       ├── controller/        # Controladores de negocio
│       ├── viewController/    # Controladores de vista (JavaFX)
│       ├── internalController/ # Controladores internos y utilidades
│       └── App.java           # Punto de entrada
├── src/main/resources/
│   └── co.edu.uniquindio.sameday.shipmentsuqsameday/
│       ├── interfaces/        # Archivos FXML
│       ├── css/              # Hojas de estilo
│       └── html/             # Templates HTML
├── src/test/java/            # Pruebas unitarias (JUnit 5)
│   └── co.edu.uniquindio.sameday.shipmentsuqsameday.test/
│       ├── CoordinatesTest.java
│       ├── AddressTest.java
│       ├── DelivererTest.java
│       ├── MapCoordinateIntegrationServiceTest.java
│       └── ReverseGeocoderTest.java
└── pom.xml
```

## 🚀 Cómo Ejecutar

### Prerequisitos
- Java JDK 17 o superior
- Maven 3.6+

### Pasos de instalación

```bash
# Clonar el repositorio
git clone https://github.com/Pedroj-64/ShipmentsUQ.git
cd ShipmentsUQ/ShipmentsUQ-SameDay

# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn javafx:run
```

### Usuarios de prueba

| Usuario | Email | Contraseña | Rol |
|---------|-------|------------|-----|
| Administrador | admin@gmail.com | 1234 | ADMIN |
| Cliente | cliente@gmail.com | 1234 | CLIENT |

## 🎮 Funcionalidades por Rol

### 👤 Cliente
- Crear y gestionar envíos
- Rastrear paquetes en tiempo real
- Gestionar direcciones de envío
- Administrar métodos de pago
- Ver historial de envíos
- Cotizar tarifas

### 🚚 Repartidor
- Ver envíos asignados
- Actualizar estados de entregas
- Gestionar disponibilidad
- Ver rutas optimizadas

### 👑 Administrador
- Dashboard con métricas completas
- Gestión de usuarios y repartidores
- Supervisión de todos los envíos
- Configuración de tarifas
- Generación de reportes
- Gestión de incidencias

## 📊 Características Técnicas Destacadas

### Sistema de Coordenadas Dual

El sistema implementa dos estrategias de coordenadas que coexisten mediante el **Patrón Facade**:

#### 🔲 Sistema Grid (Cartesiano)
- Mapa urbano basado en cuadrícula cartesiana (X, Y)
- Algoritmo de distancia Manhattan
- Visualización en tiempo real de posiciones
- Compatible con el sistema legacy

#### 🌍 Sistema GPS Real
- Coordenadas GPS (latitud, longitud)
- Algoritmo de distancia Haversine
- Geocodificación inversa con Nominatim OpenStreetMap
- Conversión bidireccional Grid ↔ GPS

**Integración mediante Facade:**
```java
MapCoordinateIntegrationService
├─ GridCoordinateStrategy (Manhattan)
├─ RealCoordinateStrategy (Haversine)
└─ RealMapService (Adapter GPS↔Grid)
```

### Geocodificación Inversa
- Conversión de coordenadas GPS a direcciones legibles
- Integración con Nominatim (OpenStreetMap)
- Soporte para localización de repartidores y puntos de entrega

### Gestión de Tarifas
```java
Tarifa = (Peso × Factor + Volumen × Factor + Distancia × Factor)
        × Multiplicador de Prioridad
        + Recargos (Seguro, Frágil, etc.)
```

### Sistema de Decoradores
Los servicios pueden ser extendidos dinámicamente:
```java
Servicio Base
  ↓
+ Validación
  ↓
+ Logging
  ↓
+ Notificaciones
```

## 🧪 Testing y Calidad

El proyecto incluye una suite completa de pruebas unitarias desarrolladas con **JUnit 5**:

### Cobertura de Pruebas
- ✅ **CoordinatesTest**: 12 pruebas - Validación del modelo GPS
- ✅ **AddressTest**: 7 pruebas - Modelo de direcciones
- ✅ **DelivererTest**: 10 pruebas - Gestión de repartidores con coordenadas duales
- ✅ **MapCoordinateIntegrationServiceTest**: 6 pruebas - Integración Facade
- ⚠️ **ReverseGeocoderTest**: Requiere conexión a internet (Nominatim API)

### Patrones Probados
- Patrón **Facade** (MapCoordinateIntegrationService)
- Patrón **Adapter** (RealMapService)
- Patrón **Strategy** (GridCoordinateStrategy, RealCoordinateStrategy)

**Ejecutar pruebas:**
```bash
# Todas las pruebas
mvn test

# Pruebas específicas (sin API)
mvn test -Dtest=CoordinatesTest,AddressTest,DelivererTest,MapCoordinateIntegrationServiceTest
```

Ver [Suite de Pruebas](ShipmentsUQ-SameDay/src/test/java/co/edu/uniquindio/sameday/shipmentsuqsameday/test/README.md) para documentación detallada.

## 🐛 Reportar Problemas

¿Encontraste un bug? ¿Tienes una sugerencia? Abre un [issue](https://github.com/Pedroj-64/ShipmentsUQ/issues) en GitHub.

## 📝 Licencia

Este proyecto está bajo la Licencia GPL v3.0. Ver el archivo [LICENSE](LICENSE) para más detalles.

## 🎓 Contexto Académico

Este proyecto fue desarrollado como parte del programa de **Ingeniería de Sistemas** de la **Universidad del Quindío**, con el objetivo de aplicar conceptos avanzados de:

- Programación Orientada a Objetos
- Patrones de Diseño de Software
- Arquitectura de Aplicaciones
- Interfaces Gráficas de Usuario
- Gestión de Proyectos de Software

---

<div align="center">

**Hecho con 💚 en Armenia, Quindío, Colombia.**
**Tirando parla chimba y relleno azaroso**

*Universidad del Quindío - Facultad de Ingeniería*

[![Java](https://img.shields.io/badge/Java-17+-orange?style=flat&logo=java)](https://www.java.com)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=flat&logo=java)](https://openjfx.io)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red?style=flat&logo=apache-maven)](https://maven.apache.org)
[![License](https://img.shields.io/badge/License-GPL%20v3-green?style=flat)](LICENSE)

</div>
