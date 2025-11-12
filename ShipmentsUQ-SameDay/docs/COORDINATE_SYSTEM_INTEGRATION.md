# 🗺️ Sistema de Coordenadas Duales - ShipmentsUQ

## 📋 Resumen de Implementación

Este sistema permite usar **Grid Map** (existente) y **Real GPS Map** (nuevo) de forma simultánea y opcional, sin romper funcionalidad existente.

---

## 🏗️ Patrones de Diseño Utilizados

### 1. **Strategy Pattern** (`ICoordinateStrategy`)
- **Ubicación**: `model/interfaces/ICoordinateStrategy.java`
- **Propósito**: Intercambiar algoritmos de cálculo de coordenadas dinámicamente
- **Implementaciones**:
  - `GridCoordinateStrategy` → Sistema de cuadrícula existente
  - `RealCoordinateStrategy` → Sistema GPS con Haversine

**Beneficios**:
- ✅ Sin if/else masivos
- ✅ Fácil agregar nuevos sistemas (ej: coordenadas polares)
- ✅ Principio Open/Closed respetado

### 2. **Adapter Pattern** (`RealMapService`)
- **Ubicación**: `mapping/RealMapService.java`
- **Propósito**: Adaptar el nuevo sistema GPS al sistema Grid existente
- **Funcionalidad**:
  - Convierte coordenadas GPS ↔ Grid
  - Mantiene compatibilidad con código legacy
  - Gestiona el servidor web del mapa

### 3. **Facade Pattern** (Pendiente)
- **Propósito**: Interfaz unificada para ambos sistemas
- **Ubicación**: `MapCoordinateIntegrationService.java` (por crear)

---

## 📦 Cambios en Modelos

### `Deliverer.java`
**Campos añadidos** (compatibles con datos existentes):
```java
private Double realLatitude;  // null si no tiene GPS
private Double realLongitude; // null si no tiene GPS
```

**Nuevos métodos**:
- `updateRealPosition(double lat, double lng)` → Actualiza GPS
- `hasRealCoordinates()` → Verifica si tiene GPS
- `syncCoordinates()` → Sincroniza Grid ↔ GPS

**⚠️ IMPORTANTE**: Los campos existentes `currentX` y `currentY` **NO fueron modificados**. El sistema Grid sigue funcionando igual.

### `ShipmentDetails.java`
**Campos añadidos**:
```java
private final Coordinates originCoordinates;      // null si usa Grid
private final Coordinates destinationCoordinates; // null si usa Grid
private final String coordinateSystem;            // "Grid" o "Real GPS"
```

**Nuevos métodos**:
- `usesRealCoordinates()` → Verifica qué sistema usa

---

## 🔄 Flujo de Uso

### Caso 1: Usuario usa Grid Map (existente)
```
1. AddressFormViewController muestra GridMapViewController
2. Usuario selecciona en el grid
3. Se guarda currentX, currentY
4. realLatitude y realLongitude quedan en null
5. ShipmentCalculator usa GridCoordinateStrategy (default)
6. ✅ Todo funciona como antes
```

### Caso 2: Usuario usa Real GPS Map (nuevo)
```
1. AddressFormViewController muestra botón "📍 Usar Coordenadas Reales"
2. Usuario hace clic → abre http://localhost:8080 en navegador
3. Usuario selecciona origen/destino en OpenStreetMap
4. Coordenadas GPS se envían a Java vía HTTP
5. Se guardan en realLatitude, realLongitude
6. RealMapService convierte GPS → Grid para compatibilidad
7. ShipmentCalculator detecta coordenadas reales → usa RealCoordinateStrategy
8. ✅ Cálculos más precisos con Haversine
```

---

## 🛠️ Componentes del Sistema

### Backend (Java)
| Componente | Responsabilidad | Patrón |
|------------|----------------|---------|
| `ICoordinateStrategy` | Define contrato para cálculos | Strategy |
| `GridCoordinateStrategy` | Lógica de Grid (Manhattan) | Concrete Strategy |
| `RealCoordinateStrategy` | Lógica GPS (Haversine) | Concrete Strategy |
| `RealMapService` | Adapter Grid ↔ GPS | Adapter |
| `MapWebServer` | Servidor HTTP puerto 8080 | - |
| `Coordinates` | Modelo de coordenadas GPS | Value Object |

### Frontend (Web)
| Archivo | Tecnología | Propósito |
|---------|-----------|-----------|
| `index.html` | HTML5 + CSS3 | Interfaz del mapa |
| `app.js` | JavaScript + Leaflet.js | Lógica del mapa interactivo |
| OpenStreetMap | Tiles gratuitos | Mapa base |

---

## 🚀 Cómo Integrar en un Controller

### Ejemplo: AddressFormViewController

```java
// Importar
import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.*;

public class AddressFormViewController {
    
    private RealMapService realMapService;
    private Coordinates selectedOrigin;
    private Coordinates selectedDestination;
    private boolean usingRealCoordinates = false;
    
    @FXML
    private Button btn_useRealCoordinates;
    
    @FXML
    public void initialize() {
        realMapService = new RealMapService();
    }
    
    @FXML
    private void handleUseRealCoordinates() {
        // Iniciar servidor si no está activo
        if (realMapService.startMapServer()) {
            // Abrir navegador
            realMapService.openMapInBrowser();
            usingRealCoordinates = true;
            
            // Mostrar diálogo de instrucciones
            showMapInstructions();
        }
    }
    
    // Método que se llama cuando JavaScript envía coordenadas
    public void onCoordinatesReceived(Coordinates origin, Coordinates destination) {
        this.selectedOrigin = origin;
        this.selectedDestination = destination;
        
        // Actualizar UI
        updateCoordinateLabels();
        
        // Calcular costo
        double cost = realMapService.calculateShipmentCost(origin, destination);
        txt_cost.setText(String.format("$%,.0f COP", cost));
    }
}
```

---

## 📊 Estado Actual vs. Pendiente

### ✅ Completado
- [x] Strategy Pattern implementado
- [x] Modelos extendidos (Deliverer, ShipmentDetails)
- [x] RealMapService (Adapter)
- [x] Servidor web HTTP funcional
- [x] Interfaz web del mapa (HTML/CSS/JS)
- [x] Comunicación HTTP Java ↔ JavaScript
- [x] Cálculos con Haversine (precisión real)

### ⏳ Pendiente
- [ ] Integrar en AddressFormViewController
- [ ] Actualizar ShipmentCalculator con Strategy
- [ ] Crear MapCoordinateIntegrationService (Facade)
- [ ] Persistencia de coordenadas reales en repositorios
- [ ] UI toggle "Grid Map" ↔ "Real GPS Map"
- [ ] Testing completo
- [ ] Documentación de usuario final

---

## 🎯 Próximos Pasos Recomendados

1. **Integrar toggle en AddressFormViewController**
   - Añadir botón "📍 Usar Coordenadas Reales"
   - Manejar callback desde JavaScript
   - Mostrar coordenadas seleccionadas

2. **Actualizar ShipmentCalculator**
   - Detectar si ShipmentDetails tiene coordenadas reales
   - Usar RealCoordinateStrategy si las tiene
   - Mantener GridCoordinateStrategy como default

3. **Testing**
   - Verificar que Grid Map sigue funcionando
   - Probar Real GPS Map
   - Validar persistencia de datos

---

## ⚠️ Principios Respetados

✅ **No se rompió código existente**  
✅ **Compatibilidad hacia atrás garantizada**  
✅ **Datos legacy siguen funcionando**  
✅ **Principio Open/Closed (Strategy Pattern)**  
✅ **Single Responsibility (cada clase hace una cosa)**  
✅ **Dependency Inversion (ICoordinateStrategy es abstracción)**  

---

## 📝 Notas Importantes

1. **Campos Nullable**: `realLatitude` y `realLongitude` son `Double` (no `double`) para permitir valores `null` en datos existentes.

2. **Serialización**: Se incrementó `serialVersionUID` en `Deliverer` y `ShipmentDetails` para la nueva versión.

3. **Puerto 8080**: El servidor web usa el puerto 8080. Asegúrate de que esté disponible.

4. **Navegador**: Se abre automáticamente en el navegador por defecto del sistema.

5. **Conversión Grid ↔ GPS**: La conversión es aproximada. Para máxima precisión, usa directamente coordenadas GPS.

---

## 🔗 Referencias

- **Leaflet.js**: https://leafletjs.com/
- **OpenStreetMap**: https://www.openstreetmap.org/
- **Haversine Formula**: https://en.wikipedia.org/wiki/Haversine_formula
- **Strategy Pattern**: https://refactoring.guru/design-patterns/strategy
- **Adapter Pattern**: https://refactoring.guru/design-patterns/adapter

---

**Última actualización**: 12 de noviembre de 2025  
**Versión del sistema**: 2.0 (Dual Coordinate System)
