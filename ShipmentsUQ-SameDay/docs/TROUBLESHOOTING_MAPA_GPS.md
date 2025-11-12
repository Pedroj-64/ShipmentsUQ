# 🔧 Troubleshooting - Mapa GPS

## Problemas Solucionados ✅

### ❌ Problema 1: "No se puede comunicar con Java"

**Síntoma**: Al hacer clic en "Enviar a Java" en el navegador, aparece un error de conexión.

**Causas Comunes**:
1. El servidor HTTP no está corriendo en puerto 8080
2. Firewall bloqueando el puerto
3. La aplicación JavaFX no está abierta

**Solución**:

```bash
# 1. Verificar que la aplicación está corriendo
# En la terminal de IntelliJ/VS Code deberías ver:
╔════════════════════════════════════════════════════════════╗
║  🗺️  ShipmentsUQ - Servidor de Mapas Iniciado            ║
╠════════════════════════════════════════════════════════════╣
║  📍 Puerto: 8080                                          ║
║  🌐 URL: http://localhost:8080                          ║
║  📂 Sirviendo: webapp/                                     ║
║  ✓ Listo para recibir coordenadas desde JavaScript       ║
╚════════════════════════════════════════════════════════════╝

# 2. Probar el puerto manualmente
# Abre http://localhost:8080 en tu navegador
# Deberías ver el mapa

# 3. Si no funciona, reinicia la aplicación JavaFX
# Ve a "Run" → "Stop" → "Run: App"
```

**Verificación en Consola JavaScript**:
```javascript
// Abre F12 (DevTools) en el navegador
// Ve a la pestaña "Console"
// Deberías ver:
Mapa inicializado en Armenia, Quindío
Esperando conexión con servidor Java en http://localhost:8080
```

---

### ❌ Problema 2: "Debe seleccionar origen Y destino"

**Síntoma**: JavaScript pide ambas coordenadas, pero AddressForm solo necesita origen.

**✅ SOLUCIONADO**: Ahora el destino es opcional.

**Cómo Usar**:
1. Click en botón "ORIGEN" (azul)
2. Click en el mapa donde quieres el origen
3. Click en "💾 Enviar a Java" (NO necesitas seleccionar destino)
4. Las coordenadas se envían a JavaFX

**Opcional - Seleccionar Destino**:
- Si quieres ver cálculos de distancia/costo/tiempo
- Click en "DESTINO" (verde)
- Click en otra ubicación
- Verás métricas calculadas automáticamente

---

## Otros Problemas Comunes

### 🔴 Error: "Puerto 8080 ya está en uso"

**Síntoma**: 
```
java.net.BindException: Address already in use: bind
```

**Solución**:

```powershell
# Windows PowerShell - Encontrar proceso usando puerto 8080
netstat -ano | findstr :8080

# Salida ejemplo:
# TCP    0.0.0.0:8080    0.0.0.0:0    LISTENING    12345

# Matar el proceso (reemplaza 12345 con el PID real)
taskkill /PID 12345 /F

# Alternativa: Reiniciar la computadora
```

---

### 🟡 Mapa no carga (pantalla en blanco)

**Síntoma**: El navegador abre pero solo muestra fondo blanco.

**Causas**:
1. Sin conexión a internet (OpenStreetMap requiere internet)
2. Recursos webapp/ no están en `target/classes`

**Solución**:

```bash
# Recompilar para copiar recursos
mvn clean compile

# Verificar que existen los archivos
ls target/classes/co/edu/uniquindio/sameday/shipmentsuqsameday/webapp/
# Deberías ver: index.html, app.js

# Si no existen, copiar manualmente:
cp src/main/resources/co/edu/uniquindio/sameday/shipmentsuqsameday/webapp/* \
   target/classes/co/edu/uniquindio/sameday/shipmentsuqsameday/webapp/
```

---

### 🟢 Coordenadas no aparecen en JavaFX

**Síntoma**: Envías desde el navegador pero no se actualiza la ventana de JavaFX.

**Debug**:

1. **Verificar Callback**:
```java
// En AddressFormViewController.initialize()
realMapService.setCoordinatesCallback((origin, destination) -> {
    System.out.println("🎯 CALLBACK RECIBIDO:");
    System.out.println("Origen: " + origin);
    System.out.println("Destino: " + destination);
    onRealCoordinatesReceived(origin, destination);
});
```

2. **Verificar Logs del Servidor**:
```
📥 Coordenadas recibidas desde JavaScript:
{"origin":{"lat":4.533889,"lng":-75.681111},"destination":null,...}
✓ Origen: Coordinates{latitude=4.533889, longitude=-75.681111}
ℹ️  Destino no proporcionado (opcional)
────────────────────────────────────────
```

3. **Verificar Platform.runLater()**:
```java
public void onRealCoordinatesReceived(Coordinates origin, Coordinates destination) {
    // DEBE estar dentro de Platform.runLater para actualizar UI
    Platform.runLater(() -> {
        System.out.println("Actualizando UI con coordenadas...");
        // ... resto del código
    });
}
```

---

### 🔵 CORS Error en Navegador

**Síntoma**: 
```
Access to fetch at 'http://localhost:8080/api/coordinates' 
from origin 'http://localhost:8080' has been blocked by CORS policy
```

**✅ YA SOLUCIONADO** en `MapWebServer.java`:

```java
// Headers CORS en CoordinatesHandler
exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
```

Si persiste, verifica que tengas la versión más reciente compilada.

---

## Testing Paso a Paso

### ✅ Test Completo del Sistema

1. **Iniciar Aplicación**:
```bash
mvn javafx:run
```

2. **Ir a Perfil → Agregar Dirección**:
   - Click en el menú de usuario
   - Selecciona "Perfil y Direcciones"
   - Click en "Agregar Dirección"

3. **Activar Mapa GPS**:
   - Click en "🗺️ Usar Coordenadas Reales"
   - Se abre el navegador en http://localhost:8080
   - Aparece diálogo de instrucciones

4. **Seleccionar Ubicación**:
   - Click en "ORIGEN" (azul) si no está activo
   - Click en el mapa (ejemplo: Universidad del Quindío)
   - Verás el marcador 📍 y las coordenadas en el panel

5. **Enviar a Java**:
   - Click en "💾 Enviar a Java"
   - Deberías ver alert: "✓ Coordenadas enviadas al sistema Java"
   - En JavaFX verás: "GPS: Lat 4.533889, Lng -75.681111"

6. **Guardar Dirección**:
   - Completa: Alias, Calle, Ciudad
   - Click "Guardar"
   - Mensaje: "Dirección guardada correctamente (con coordenadas GPS)"

7. **Verificar en Consola Java**:
```
📥 Coordenadas recibidas desde JavaScript:
✓ Origen: Coordinates{latitude=4.533889, longitude=-75.681111}
ℹ️  Destino no proporcionado (opcional)
────────────────────────────────────────
🎯 CALLBACK RECIBIDO:
Origen: Coordinates{latitude=4.533889, longitude=-75.681111}
Actualizando UI con coordenadas...
```

---

## Logs Importantes

### ✅ Todo Funcionando Correctamente

**Al iniciar la app**:
```
╔════════════════════════════════════════════════════════════╗
║  🗺️  ShipmentsUQ - Servidor de Mapas Iniciado            ║
╠════════════════════════════════════════════════════════════╣
║  📍 Puerto: 8080                                          ║
║  🌐 URL: http://localhost:8080                          ║
║  📂 Sirviendo: webapp/                                     ║
║  ✓ Listo para recibir coordenadas desde JavaScript       ║
╚════════════════════════════════════════════════════════════╝
🌐 Abriendo mapa en el navegador...
```

**Al seleccionar coordenadas**:
```
📥 Coordenadas recibidas desde JavaScript:
{"origin":{"lat":4.533889,"lng":-75.681111},"destination":null,"timestamp":"2025-11-12T20:30:00.000Z"}
✓ Origen: Coordinates{latitude=4.533889, longitude=-75.681111}
ℹ️  Destino no proporcionado (opcional)
────────────────────────────────────────
```

**Con origen Y destino** (opcional):
```
📥 Coordenadas recibidas desde JavaScript:
✓ Origen: Coordinates{latitude=4.533889, longitude=-75.681111}
✓ Destino: Coordinates{latitude=4.540000, longitude=-75.690000}
📊 Distancia: 1.23 km
💰 Costo: $8,075 COP
⏱️  Tiempo: 12 minutos
📦 Same-day: Sí
────────────────────────────────────────
```

---

## Preguntas Frecuentes (FAQ)

### ❓ ¿Necesito internet para usar el mapa?

**Sí**, OpenStreetMap requiere conexión a internet para descargar las tiles del mapa.

Sin embargo:
- Las coordenadas GPS funcionan offline una vez cargado el mapa
- El servidor Java (localhost:8080) funciona localmente
- Solo necesitas internet para ver el mapa visual

---

### ❓ ¿Por qué no veo el GridMap cuando uso GPS?

El GridMap se oculta porque estás usando el sistema GPS. Para volver al GridMap:
- Click en "📍 Usar Mapa de Cuadrícula"
- El botón cambia de color verde → gradiente morado

Ambos sistemas NO se usan simultáneamente, son alternativos.

---

### ❓ ¿Las coordenadas GPS se guardan en la base de datos?

**Actualmente**: Se convierten a Grid y se guarda la versión Grid (compatibilidad).

**Futuro** (opcional): Se puede extender Address.java para guardar ambas coordenadas:
```java
@Builder.Default
private Double gpsLatitude = null;

@Builder.Default
private Double gpsLongitude = null;
```

---

### ❓ ¿Puedo usar Google Maps en lugar de OpenStreetMap?

Sí, pero requiere API Key de Google. Para cambiar:

```javascript
// En webapp/app.js, reemplaza:
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 19
}).addTo(map);

// Por:
L.tileLayer('https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
    maxZoom: 20,
    subdomains:['mt0','mt1','mt2','mt3']
}).addTo(map);
```

---

### ❓ ¿Cómo cambio el puerto 8080?

En `MapWebServer.java`:
```java
private static final int PORT = 8080; // Cambiar a 3000, 8081, etc.
```

También actualiza `app.js`:
```javascript
const JAVA_SERVER = 'http://localhost:8080'; // Cambiar a mismo puerto
```

Recompila:
```bash
mvn clean compile
```

---

## Contacto y Soporte

Si encuentras un problema no documentado aquí:

1. Revisa la consola de Java (IntelliJ/VS Code terminal)
2. Revisa la consola del navegador (F12 → Console)
3. Verifica que tienes la última versión compilada (`mvn compile`)
4. Consulta `docs/COORDINATE_SYSTEM_INTEGRATION.md` para más detalles técnicos

---

**Última actualización**: 12 de noviembre de 2025  
**Versión del sistema**: 1.0-SNAPSHOT
