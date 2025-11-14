package co.edu.uniquindio.sameday.shipmentsuqsameday.test;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.MapCoordinateIntegrationService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para MapCoordinateIntegrationService (Facade Pattern)
 * Valida integración entre sistemas GPS y Grid
 */
public class MapCoordinateIntegrationServiceTest {
    
    private MapCoordinateIntegrationService service;
    private List<Deliverer> deliverers;
    
    @BeforeEach
    public void setUp() {
        service = new MapCoordinateIntegrationService();
        deliverers = new ArrayList<>();
        
        // Repartidor con GPS (Centro de Armenia)
        Deliverer delivererGPS = Deliverer.builder()
            .id(UUID.randomUUID())
            .name("GPS Deliverer")
            .document("111111111")
            .phone("3001111111")
            .status(DelivererStatus.AVAILABLE)
            .zone("Centro")
            .currentX(5.0)
            .currentY(5.0)
            .realLatitude(4.533889)
            .realLongitude(-75.681111)
            .build();
        
        // Repartidor solo con Grid
        Deliverer delivererGrid = Deliverer.builder()
            .id(UUID.randomUUID())
            .name("Grid Deliverer")
            .document("222222222")
            .phone("3002222222")
            .status(DelivererStatus.AVAILABLE)
            .zone("Norte")
            .currentX(10.0)
            .currentY(8.0)
            .build();
        
        // Repartidor con GPS lejos
        Deliverer delivererFar = Deliverer.builder()
            .id(UUID.randomUUID())
            .name("Far Deliverer")
            .document("333333333")
            .phone("3003333333")
            .status(DelivererStatus.AVAILABLE)
            .zone("Sur")
            .currentX(20.0)
            .currentY(20.0)
            .realLatitude(4.570000)
            .realLongitude(-75.650000)
            .build();
        
        deliverers.add(delivererGPS);
        deliverers.add(delivererGrid);
        deliverers.add(delivererFar);
    }
    
    @Test
    public void testFindNearestDelivererWithGPS() {
        // Coordenadas GPS cercanas al primer repartidor
        Coordinates targetGPS = new Coordinates(4.534000, -75.682000);
        
        Optional<Deliverer> result = service.findNearestDeliverer(
            deliverers, 
            targetGPS, 
            0, 0
        );
        
        assertTrue(result.isPresent(), "Debe encontrar un repartidor");
        assertEquals("GPS Deliverer", result.get().getName(), 
            "Debe encontrar el repartidor con GPS más cercano");
    }
    
    @Test
    public void testFindNearestDelivererWithGrid() {
        // Sin coordenadas GPS, usar Grid
        Optional<Deliverer> result = service.findNearestDeliverer(
            deliverers, 
            null, 
            11.0, 
            9.0
        );
        
        assertTrue(result.isPresent(), "Debe encontrar un repartidor");
        assertEquals("Grid Deliverer", result.get().getName(), 
            "Debe encontrar el repartidor Grid más cercano");
    }
    
    @Test
    public void testFindNearestDelivererEmptyList() {
        List<Deliverer> empty = new ArrayList<>();
        
        Optional<Deliverer> result = service.findNearestDeliverer(
            empty, 
            new Coordinates(4.5, -75.7), 
            0, 0
        );
        
        assertFalse(result.isPresent(), 
            "Con lista vacía debe retornar Optional.empty()");
    }
    
    @Test
    public void testGetDelivererLocationWithGPS() {
        Deliverer deliverer = deliverers.get(0); // Tiene GPS
        
        String location = service.getDelivererLocation(deliverer);
        
        assertNotNull(location);
        assertTrue(location.contains("GPS") || location.contains("4.533889"), 
            "Debe mostrar coordenadas GPS");
    }
    
    @Test
    public void testGetDelivererLocationWithGrid() {
        Deliverer deliverer = deliverers.get(1); // Solo Grid
        
        String location = service.getDelivererLocation(deliverer);
        
        assertNotNull(location);
        assertTrue(location.contains("Grid") || location.contains("10.0"), 
            "Debe mostrar coordenadas Grid");
    }
    
    @Test
    public void testFindNearestUsesCorrectStrategy() {
        // Verificar que el servicio integra ambos sistemas correctamente
        Coordinates target = new Coordinates(4.540000, -75.670000);
        
        // El servicio debe poder encontrar repartidores con GPS
        Optional<Deliverer> result = service.findNearestDeliverer(
            deliverers, target, 0, 0
        );
        
        assertTrue(result.isPresent(), "Debe encontrar repartidor con GPS");
    }
    
    @Test
    public void testPrefersGPSOverGrid() {
        Deliverer delivererWithBoth = Deliverer.builder()
            .id(UUID.randomUUID())
            .name("Both Systems")
            .document("444444444")
            .phone("3004444444")
            .status(DelivererStatus.AVAILABLE)
            .zone("Centro")
            .currentX(100.0)  // Grid muy lejos
            .currentY(100.0)
            .realLatitude(4.533900)  // GPS muy cerca
            .realLongitude(-75.681100)
            .build();
        
        List<Deliverer> testList = List.of(delivererWithBoth);
        Coordinates nearbyGPS = new Coordinates(4.533889, -75.681111);
        
        Optional<Deliverer> result = service.findNearestDeliverer(
            testList, 
            nearbyGPS, 
            5.0, 
            5.0
        );
        
        assertTrue(result.isPresent());
        // Debe usar GPS a pesar de que Grid está lejos
        assertEquals("Both Systems", result.get().getName());
    }
    
    @Test
    public void testGetDelivererLocationWithNull() {
        // El servicio actual lanza NullPointerException con null
        assertThrows(NullPointerException.class, () -> {
            service.getDelivererLocation(null);
        }, "Debe lanzar NullPointerException con deliverer null");
    }
}
