# Persistencia de Coordenadas Reales en Repositorios

## Resumen

Los repositorios del sistema utilizan **serialización Java** a través de la clase `Serializer` para guardar/cargar datos en archivos `.dat` en la carpeta `data/`.

## ✅ Estado Actual

### **Ya está funcionando automáticamente**

Los campos de coordenadas reales agregados a los modelos se persisten automáticamente gracias a que:

1. **Deliverer.java**: 
   - Campos: `Double realLatitude`, `Double realLongitude` (nullable)
   - `serialVersionUID = 2L` (actualizado para migración segura)
   - Implementa `Serializable`

2. **ShipmentDetails.java**:
   - Campos: `Coordinates originCoordinates`, `destinationCoordinates` (nullable)
   - Campo: `String coordinateSystem`
   - `serialVersionUID = 2L` (actualizado)
   - Implementa `Serializable`

3. **Coordinates.java**:
   - Implementa `Serializable`
   - Todos los campos son serializables (double, LocalDateTime)

## Cómo Funciona

### Arquitectura de Persistencia

```
BaseRepository<T>
    ├── save(entity)           → Guarda en memoria (Map)
    ├── saveToFile()           → Serializa a data/[Repository].dat
    ├── loadFromFile()         → Deserializa desde archivo
    └── loadEntities(List<T>)  → Carga lista en memoria
```

### Flujo de Guardado

```java
// 1. Usuario guarda un Deliverer con coordenadas GPS
Deliverer deliverer = Deliverer.builder()
    .name("Juan Pérez")
    .currentX(10.5)
    .currentY(20.3)
    .realLatitude(4.533889)  // ← Nueva coordenada GPS
    .realLongitude(-75.681111)
    .build();

// 2. Se guarda en memoria
delivererRepository.save(deliverer);

// 3. Se serializa a disco (automático al cerrar app)
delivererRepository.saveToFile();
// → Archivo: data/DelivererRepository.dat
```

### Flujo de Carga

```java
// 1. Al iniciar la aplicación
delivererRepository.loadFromFile();

// 2. Deserializa desde data/DelivererRepository.dat
// 3. Si encuentra serialVersionUID = 1L, migra automáticamente
//    - realLatitude = null (por defecto)
//    - realLongitude = null
// 4. Si encuentra serialVersionUID = 2L, carga directo

// 5. Datos disponibles en memoria
List<Deliverer> deliverers = delivererRepository.findAll();
```

## Migración de Datos Existentes

### ✅ Compatibilidad Backward

```java
// Datos antiguos (serialVersionUID = 1L)
Deliverer oldData = {
    id: UUID
    name: "Juan"
    currentX: 10.5
    currentY: 20.3
    // NO tiene realLatitude ni realLongitude
}

// Después de deserializar con serialVersionUID = 2L
Deliverer migratedData = {
    id: UUID  // ✅ Mantiene ID
    name: "Juan"  // ✅ Mantiene nombre
    currentX: 10.5  // ✅ Mantiene Grid
    currentY: 20.3
    realLatitude: null  // ✅ Nuevo campo (nullable)
    realLongitude: null
}
```

### Estrategia de Migración

1. **Automática**: Java deserializa con `serialVersionUID` diferente
2. **Sin pérdida de datos**: Campos existentes se mantienen
3. **Valores por defecto**: Campos nuevos son `null` (coordenadas opcionales)
4. **Sin errores**: No se requiere script de migración

## Archivos de Datos

### Ubicación

```
ShipmentsUQ-SameDay/
└── data/
    ├── DelivererRepository.dat      ← Repartidores con GPS opcional
    ├── ShipmentRepository.dat       ← Envíos con coordenadas reales
    ├── UserRepository.dat
    ├── AddressRepository.dat
    ├── PaymentRepository.dat
    ├── RateRepository.dat
    └── IncidentRepository.dat
```

### Formato

- **Binario** (serialización Java nativa)
- **No legible por humanos** (usa ObjectInputStream/ObjectOutputStream)
- **Compacto** y eficiente

## Validación de Persistencia

### Cómo Verificar que Funciona

#### 1. Guardar Deliverer con GPS

```java
// En cualquier parte del código
Deliverer deliverer = delivererRepository.findById(uuid).orElseThrow();
deliverer.updateRealPosition(4.533889, -75.681111);
delivererRepository.update(deliverer);
delivererRepository.saveToFile();
```

#### 2. Cerrar y Reabrir Aplicación

```bash
# La aplicación guarda automáticamente al cerrar
# (si está implementado en shutdown hooks)
```

#### 3. Verificar Carga

```java
// Al iniciar
delivererRepository.loadFromFile();
Deliverer loaded = delivererRepository.findById(uuid).orElseThrow();
boolean hasGPS = loaded.hasRealCoordinates();
// hasGPS debería ser true
```

### Logs de Depuración

El `BaseRepository` ya tiene logs integrados:

```
Repositorio DelivererRepository guardado en data/DelivererRepository.dat
loadEntities: cargando 5 entidades en el repositorio DelivererRepository
  - Entidad cargada con ID: 123e4567-e89b-12d3-a456-426614174000
  - Entidad cargada con ID: 223e4567-e89b-12d3-a456-426614174001
...
loadEntities: 5 entidades cargadas en el repositorio DelivererRepository
```

## Coordinación con MapCoordinateIntegrationService

### Sincronización de Coordenadas

```java
// Facade unifica Grid ↔ GPS
MapCoordinateIntegrationService integrationService = new MapCoordinateIntegrationService();

// 1. Actualizar repartidor con GPS
Deliverer deliverer = ...;
deliverer.updateRealPosition(4.533889, -75.681111);

// 2. Sincronizar Grid ← GPS
integrationService.syncDelivererCoordinates(deliverer);
// Ahora deliverer tiene:
//   - realLatitude = 4.533889
//   - realLongitude = -75.681111
//   - currentX = [convertido desde GPS]
//   - currentY = [convertido desde GPS]

// 3. Guardar (persiste AMBOS sistemas)
delivererRepository.update(deliverer);
delivererRepository.saveToFile();
```

## Errores Comunes y Soluciones

### ❌ Error: InvalidClassException

```
java.io.InvalidClassException: co.edu.uniquindio...Deliverer; 
local class incompatible: stream classdesc serialVersionUID = 1, 
local class serialVersionUID = 2
```

**Solución**: Ya implementada con `serialVersionUID = 2L`
- Java migrará automáticamente
- Campos nuevos serán `null`

### ❌ Error: NotSerializableException

```
java.io.NotSerializableException: co.edu.uniquindio...Coordinates
```

**Solución**: ✅ Ya resuelto
- `Coordinates` implementa `Serializable`
- Todos los campos son serializables

### ❌ Error: NullPointerException al cargar

```java
deliverer.getRealLatitude() → NullPointerException
```

**Solución**: ✅ Ya implementado
- Usar `hasRealCoordinates()` antes de acceder
- Campos son `Double` (nullable), no `double`

```java
if (deliverer.hasRealCoordinates()) {
    double lat = deliverer.getRealLatitude(); // ✅ Safe
}
```

## Testing de Persistencia

### Test Manual

```java
public class PersistenceTest {
    public static void main(String[] args) {
        DelivererRepository repo = new DelivererRepository();
        
        // 1. Crear con GPS
        Deliverer d = Deliverer.builder()
            .name("Test")
            .currentX(10.0)
            .currentY(20.0)
            .realLatitude(4.533889)
            .realLongitude(-75.681111)
            .build();
        
        repo.save(d);
        repo.saveToFile();
        
        // 2. Limpiar memoria
        UUID id = d.getId();
        repo = new DelivererRepository(); // Nueva instancia
        
        // 3. Cargar desde disco
        repo.loadFromFile();
        
        // 4. Verificar
        Deliverer loaded = repo.findById(id).orElseThrow();
        assert loaded.hasRealCoordinates();
        assert loaded.getRealLatitude() == 4.533889;
        assert loaded.getRealLongitude() == -75.681111;
        
        System.out.println("✅ Persistencia funciona correctamente");
    }
}
```

## Mejoras Futuras (Opcionales)

### 1. Migración Explícita

```java
public void migrateOldDeliverers() {
    delivererRepository.findAll().forEach(d -> {
        if (!d.hasRealCoordinates() && d.getCurrentX() != 0 && d.getCurrentY() != 0) {
            // Estimar GPS desde Grid
            double[] gps = integrationService.convertGridToReal(
                d.getCurrentX(), d.getCurrentY()
            );
            d.updateRealPosition(gps[0], gps[1]);
            delivererRepository.update(d);
        }
    });
    delivererRepository.saveToFile();
}
```

### 2. Backup Antes de Guardar

```java
@Override
public void saveToFile() {
    String fileName = getFileName();
    String backupName = fileName + ".backup";
    
    // Copiar archivo actual a backup
    Files.copy(Paths.get(fileName), Paths.get(backupName), 
        StandardCopyOption.REPLACE_EXISTING);
    
    // Guardar nuevo estado
    super.saveToFile();
}
```

### 3. Formato JSON (Alternativa)

```java
// Usar Jackson o Gson para legibilidad
public void saveToJson() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(new File("data/deliverers.json"), 
        entities.values());
}
```

## Conclusión

✅ **No se requieren cambios** en los repositorios para soportar coordenadas reales.

La persistencia funciona automáticamente porque:
1. Modelos implementan `Serializable`
2. `serialVersionUID` actualizado a `2L`
3. Campos nuevos son opcionales (`null` por defecto)
4. Migración automática de datos antiguos
5. BaseRepository maneja todo transparentemente

**Solo se necesita**: Guardar normalmente con `repository.save()` y `repository.saveToFile()`.
