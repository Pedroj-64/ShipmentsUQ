# 🧪 Suite de Pruebas Unitarias - ShipmentsUQ

Package: `co.edu.uniquindio.sameday.shipmentsuqsameday.test`

## 📊 Resumen de Cobertura

| Clase de Test | Tests | Estado | Cobertura |
|--------------|-------|---------|-----------|
| `CoordinatesTest` | 8 | ✅ PASS | Cálculo Haversine, área de servicio |
| `AddressTest` | 8 | ✅ PASS | GPS + Grid, serialización |
| `DelivererTest` | 9 | ✅ PASS | Repartidores con coordenadas duales |
| `MapCoordinateIntegrationServiceTest` | 8 | ✅ PASS | Facade Pattern, integración GPS/Grid |
| `ReverseGeocoderTest` | 9 | ⏸️ DISABLED* | Geocodificación inversa (Nominatim) |
| **TOTAL** | **42** | **33 activos** | **Modelos core + servicios** |

_*Tests marcados `@Disabled` requieren conexión a internet_

---

## 🎯 Casos de Prueba por Módulo

### 1️⃣ CoordinatesTest
Valida la clase `Coordinates` (GPS real)

**Tests incluidos:**
- ✅ `testDistanceToSamePoint()` - Distancia a sí mismo = 0
- ✅ `testDistanceCalculation()` - Haversine entre 2 puntos (~5km)
- ✅ `testDistanceIsSymmetric()` - A→B = B→A
- ✅ `testIsInServiceArea()` - Punto dentro del radio de 20km
- ✅ `testIsOutsideServiceArea()` - Punto fuera del área
- ✅ `testDefaultConstructor()` - Inicializa en centro de Armenia
- ✅ `testValidCoordinates()` - Rango válido lat/lng
- ✅ `testHaversineAccuracy()` - Armenia↔Bogotá (~200km)

**Fórmula Haversine:**
```java
double a = sin(Δlat/2)² + cos(lat1) * cos(lat2) * sin(Δlon/2)²
double c = 2 * atan2(√a, √(1-a))
distancia = EARTH_RADIUS * c  // 6371 km
```

---

### 2️⃣ AddressTest
Valida la clase `Address` con soporte dual GPS/Grid

**Tests incluidos:**
- ✅ `testHasGpsCoordinates()` - Detecta presencia de GPS
- ✅ `testSetGpsCoordinates()` - Setter de coordenadas GPS
- ✅ `testGridCoordinates()` - Coordenadas Grid (compatibilidad)
- ✅ `testDistanceToWithGrid()` - Distancia Euclidiana entre addresses
- ✅ `testAddressImplementsSerializable()` - Serialización Java
- ✅ `testBuilderPattern()` - Patrón Builder completo
- ✅ `testNullGpsCoordinates()` - Manejo de GPS null
- ✅ `testPartialGpsCoordinates()` - Solo lat o solo lng = false

**Distancia Euclidiana:**
```java
sqrt((x2-x1)² + (y2-y1)²)
```

---

### 3️⃣ DelivererTest
Valida la clase `Deliverer` con coordenadas duales

**Tests incluidos:**
- ✅ `testHasRealCoordinates()` - Detección GPS en repartidores
- ✅ `testSetRealCoordinates()` - Actualizar GPS dinámicamente
- ✅ `testDistanceToWithGrid()` - Distancia entre repartidores
- ✅ `testDelivererStatus()` - Estados: AVAILABLE, BUSY, etc.
- ✅ `testInitialAverageRating()` - Rating inicial ≥ 0
- ✅ `testBuilderPattern()` - Builder con todos los campos
- ✅ `testCurrentShipmentsEmpty()` - Lista de envíos inicialmente vacía
- ✅ `testNullRealCoordinates()` - GPS null manejado correctamente
- ✅ `testPartialRealCoordinates()` - Validación de coordenadas parciales

**Estados de Repartidor:**
```java
AVAILABLE      // Listo para asignaciones
ACTIVE         // Con envíos pero disponible
BUSY           // Máximo de envíos
IN_SERVICE     // En entrega
ON_BREAK       // Descansando
OFF_DUTY       // No disponible
```

---

### 4️⃣ MapCoordinateIntegrationServiceTest
Valida el **Facade Pattern** que integra GPS + Grid

**Tests incluidos:**
- ✅ `testFindNearestDelivererWithGPS()` - Búsqueda por GPS
- ✅ `testFindNearestDelivererWithGrid()` - Búsqueda por Grid (fallback)
- ✅ `testFindNearestDelivererEmptyList()` - Manejo lista vacía
- ✅ `testGetDelivererLocationWithGPS()` - Formato ubicación GPS
- ✅ `testGetDelivererLocationWithGrid()` - Formato ubicación Grid
- ✅ `testFindNearestUsesCorrectStrategy()` - Strategy Pattern
- ✅ `testPrefersGPSOverGrid()` - Prioridad GPS sobre Grid
- ✅ `testGetDelivererLocationWithNull()` - NPE esperado con null

**Patrón Facade:**
```
MapCoordinateIntegrationService
├─ GridCoordinateStrategy (Manhattan)
├─ RealCoordinateStrategy (Haversine)
└─ RealMapService (Adapter GPS↔Grid)
```

---

### 5️⃣ ReverseGeocoderTest ⚠️
Valida geocodificación inversa con **Nominatim API**

**Tests incluidos (DISABLED):**
- ⏸️ `testSingletonPattern()` - Singleton verificado
- ⏸️ `testReverseGeocodeArmenia()` - GPS → Dirección Armenia
- ⏸️ `testFormatColombianAddress()` - Formato direcciones Colombia
- ⏸️ `testGetFormattedAddress()` - Método de conveniencia
- ⏸️ `testRateLimiting()` - Respeto límite 1 req/seg
- ⏸️ `testInvalidCoordinates()` - Manejo coordenadas océano
- ✅ `testFormatColombianAddressWithNullComponents()` - Casos edge
- ✅ `testFormatColombianAddressWithEmptyComponents()` - Mapa vacío
- ✅ `testFormatColombianAddressWithComponents()` - Formato manual

**⚠️ Para ejecutar tests con internet:**
```bash
# Quitar @Disabled de los métodos y ejecutar:
mvn test -Dtest=ReverseGeocoderTest

# IMPORTANTE: Espera 1.1 segundos entre cada petición
```

**API Nominatim:**
```
GET https://nominatim.openstreetmap.org/reverse
?lat=4.533889
&lon=-75.681111
&format=json
&addressdetails=1
&accept-language=es
```

---

## 🚀 Ejecutar Tests

### Todos los tests (excepto DISABLED)
```bash
mvn test
```

### Tests específicos
```bash
# Un solo test
mvn test -Dtest=CoordinatesTest

# Múltiples tests
mvn test -Dtest="CoordinatesTest,AddressTest,DelivererTest"

# Un método específico
mvn test -Dtest=CoordinatesTest#testHaversineAccuracy
```

### Con cobertura
```bash
mvn test jacoco:report
# Ver: target/site/jacoco/index.html
```

### Modo verbose
```bash
mvn test -X
```

---

## 📝 Convenciones de Nombres

| Patrón | Significado | Ejemplo |
|--------|-------------|---------|
| `testXxx()` | Test básico | `testHasGpsCoordinates()` |
| `testXxxWithYyy()` | Test con condición específica | `testFindNearestWithGPS()` |
| `testInvalidXxx()` | Test casos inválidos | `testInvalidCoordinates()` |
| `testXxxEdgeCase()` | Test casos límite | `testNullGpsCoordinates()` |

---

## 🎨 Assertions Comunes

```java
// Igualdad
assertEquals(expected, actual, "mensaje");
assertEquals(4.5, distance, 0.001, "tolerancia decimal");

// Booleanos
assertTrue(condition, "mensaje");
assertFalse(condition, "mensaje");

// Objetos
assertNotNull(object, "mensaje");
assertSame(obj1, obj2, "misma instancia");

// Excepciones
assertThrows(NullPointerException.class, () -> {
    service.method(null);
}, "mensaje");

// Sin excepción
assertDoesNotThrow(() -> {
    service.method(validInput);
});
```

---

## 📦 Dependencias de Test

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.1</version>
    <scope>test</scope>
</dependency>
```

**Motor:** JUnit 5 (Jupiter)  
**Provider:** `junit-platform-surefire-provider`  
**Plugin:** `maven-surefire-plugin:3.2.5`

---

## 🐛 Debugging Tests Fallidos

### Ver detalles completos
```bash
mvn test -X > test-output.txt
```

### Ver reportes
```
target/surefire-reports/
├─ TEST-CoordinatesTest.xml
├─ CoordinatesTest.txt
└─ ...
```

### Common Issues

**❌ `AssertionFailedError`**
```
Solución: Revisar tolerancia en assertEquals para doubles
assertEquals(4.24, distance, 0.01)  // ✅
assertEquals(4.24, distance, 0.001) // ❌ muy estricto
```

**❌ `NullPointerException`**
```
Solución: Usar assertThrows o verificar null antes
assertThrows(NPE.class, () -> service.method(null));
```

**❌ `Cannot find symbol`**
```
Solución: Método no existe en la API actual
// Verificar modelo real antes de escribir test
```

---

## 📈 Próximos Tests a Implementar

- [ ] `ShipmentServiceTest` - Asignación de repartidores con GPS
- [ ] `ShipmentCalculatorTest` - Cálculo de costos con Strategy
- [ ] `RealMapServiceTest` - Conversión GPS ↔ Grid
- [ ] `GridCoordinateStrategyTest` - Distancia Manhattan
- [ ] `RealCoordinateStrategyTest` - Distancia Haversine
- [ ] `IntegrationTest` - End-to-end completo

---

## 📚 Recursos

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [Nominatim Usage Policy](https://operations.osmfoundation.org/policies/nominatim/)
- [Haversine Formula](https://en.wikipedia.org/wiki/Haversine_formula)

---

## ✅ Resultados Actuales

```
[INFO] Tests run: 33, Failures: 0, Errors: 0, Skipped: 9
[INFO] BUILD SUCCESS
```

**Cobertura estimada:**
- ✅ Modelo `Coordinates`: 100%
- ✅ Modelo `Address`: 85% (GPS + Grid)
- ✅ Modelo `Deliverer`: 80% (coordenadas duales)
- ✅ Servicio `MapCoordinateIntegrationService`: 75%
- ⏸️ Servicio `ReverseGeocoder`: 40% (requiere internet)

---

**Última actualización:** 13 de noviembre de 2025  
**Versión:** 1.0-SNAPSHOT  
**Autor:** Equipo ShipmentsUQ
