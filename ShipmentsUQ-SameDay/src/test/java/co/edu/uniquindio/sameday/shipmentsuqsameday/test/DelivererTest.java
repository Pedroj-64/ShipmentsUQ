package co.edu.uniquindio.sameday.shipmentsuqsameday.test;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase Deliverer
 * Valida funcionalidad de repartidores con coordenadas GPS y Grid
 */
public class DelivererTest {
    
    private Deliverer delivererWithGPS;
    private Deliverer delivererWithGrid;
    private Deliverer delivererWithBoth;
    
    @BeforeEach
    public void setUp() {
        // Repartidor solo con GPS
        delivererWithGPS = Deliverer.builder()
            .id(UUID.randomUUID())
            .name("Juan Pérez")
            .document("123456789")
            .phone("3001234567")
            .status(DelivererStatus.AVAILABLE)
            .zone("Centro")
            .currentX(0)
            .currentY(0)
            .realLatitude(4.533889)
            .realLongitude(-75.681111)
            .build();
        
        // Repartidor solo con Grid
        delivererWithGrid = Deliverer.builder()
            .id(UUID.randomUUID())
            .name("María López")
            .document("987654321")
            .phone("3009876543")
            .status(DelivererStatus.AVAILABLE)
            .zone("Norte")
            .currentX(10.5)
            .currentY(8.3)
            .build();
        
        // Repartidor con ambos sistemas
        delivererWithBoth = Deliverer.builder()
            .id(UUID.randomUUID())
            .name("Carlos Gómez")
            .document("456789123")
            .phone("3005555555")
            .status(DelivererStatus.BUSY)
            .zone("Sur")
            .currentX(5.0)
            .currentY(5.0)
            .realLatitude(4.540000)
            .realLongitude(-75.670000)
            .build();
    }
    
    @Test
    public void testHasRealCoordinates() {
        assertTrue(delivererWithGPS.hasRealCoordinates(), 
            "Repartidor con GPS debe retornar true");
        
        assertFalse(delivererWithGrid.hasRealCoordinates(), 
            "Repartidor sin GPS debe retornar false");
        
        assertTrue(delivererWithBoth.hasRealCoordinates(), 
            "Repartidor con ambos sistemas debe retornar true");
    }
    
    @Test
    public void testSetRealCoordinates() {
        Deliverer deliverer = Deliverer.builder()
            .id(UUID.randomUUID())
            .name("Test")
            .document("111111111")
            .phone("3001111111")
            .status(DelivererStatus.AVAILABLE)
            .zone("Test")
            .currentX(1.0)
            .currentY(1.0)
            .build();
        
        assertFalse(deliverer.hasRealCoordinates(), 
            "Inicialmente no debe tener GPS");
        
        deliverer.setRealLatitude(4.5);
        deliverer.setRealLongitude(-75.7);
        
        assertTrue(deliverer.hasRealCoordinates(), 
            "Después de setear debe tener GPS");
    }
    
    @Test
    public void testDistanceToWithGrid() {
        Deliverer other = Deliverer.builder()
            .currentX(13.5)
            .currentY(11.3)
            .build();
        
        // Distancia Euclidiana: sqrt((13.5-10.5)^2 + (11.3-8.3)^2) = sqrt(9 + 9) = 4.24
        double distance = delivererWithGrid.distanceTo(other);
        assertEquals(4.24, distance, 0.01, 
            "Distancia Euclidiana debe ser aproximadamente 4.24");
    }
    
    @Test
    public void testDelivererStatus() {
        assertEquals(DelivererStatus.AVAILABLE, delivererWithGPS.getStatus(), 
            "Repartidor debe tener status AVAILABLE");
        
        assertEquals(DelivererStatus.AVAILABLE, delivererWithGrid.getStatus(), 
            "Repartidor debe tener status AVAILABLE");
        
        assertEquals(DelivererStatus.BUSY, delivererWithBoth.getStatus(), 
            "Repartidor debe tener status BUSY");
    }
    
    @Test
    public void testInitialAverageRating() {
        // Verificar que tenga rating inicial
        assertNotNull(delivererWithGPS.getAverageRating(), 
            "Repartidor debe tener rating inicial");
        
        assertTrue(delivererWithGPS.getAverageRating() >= 0.0, 
            "Rating debe ser mayor o igual a 0");
    }
    
    @Test
    public void testBuilderPattern() {
        Deliverer deliverer = Deliverer.builder()
            .id(UUID.randomUUID())
            .name("Test Deliverer")
            .document("999999999")
            .phone("3009999999")
            .status(DelivererStatus.BUSY)
            .zone("Test Zone")
            .currentX(2.5)
            .currentY(3.5)
            .realLatitude(4.5)
            .realLongitude(-75.7)
            .build();
        
        assertNotNull(deliverer);
        assertNotNull(deliverer.getId());
        assertEquals("Test Deliverer", deliverer.getName());
        assertEquals(DelivererStatus.BUSY, deliverer.getStatus());
        assertTrue(deliverer.hasRealCoordinates());
    }
    
    @Test
    public void testCurrentShipmentsEmpty() {
        assertNotNull(delivererWithGPS.getCurrentShipments(), 
            "Lista de envíos actuales no debe ser null");
        
        assertTrue(delivererWithGPS.getCurrentShipments().isEmpty(), 
            "Lista de envíos actuales debe estar vacía inicialmente");
    }
    
    @Test
    public void testNullRealCoordinates() {
        Deliverer deliverer = Deliverer.builder()
            .id(UUID.randomUUID())
            .name("Test")
            .document("111111111")
            .phone("3001111111")
            .status(DelivererStatus.AVAILABLE)
            .zone("Test")
            .currentX(1.0)
            .currentY(1.0)
            .realLatitude(null)
            .realLongitude(null)
            .build();
        
        assertFalse(deliverer.hasRealCoordinates(), 
            "Coordenadas GPS null deben retornar false");
    }
    
    @Test
    public void testPartialRealCoordinates() {
        Deliverer delivererOnlyLat = Deliverer.builder()
            .id(UUID.randomUUID())
            .name("Test1")
            .document("111111111")
            .phone("3001111111")
            .status(DelivererStatus.AVAILABLE)
            .zone("Test")
            .currentX(1.0)
            .currentY(1.0)
            .realLatitude(4.5)
            .realLongitude(null)
            .build();
        
        assertFalse(delivererOnlyLat.hasRealCoordinates(), 
            "Solo latitud no es suficiente");
    }
}
