package co.edu.uniquindio.sameday.shipmentsuqsameday.test;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase Address
 * Valida funcionalidad de coordenadas GPS y Grid
 */
public class AddressTest {
    
    private Address addressWithGPS;
    private Address addressWithGrid;
    private Address addressWithBoth;
    
    @BeforeEach
    public void setUp() {
        // Dirección solo con GPS
        addressWithGPS = Address.builder()
            .alias("Casa GPS")
            .street("Calle 14")
            .zone("Centro")
            .city("Armenia")
            .coordX(0)
            .coordY(0)
            .gpsLatitude(4.533889)
            .gpsLongitude(-75.681111)
            .build();
        
        // Dirección solo con Grid
        addressWithGrid = Address.builder()
            .alias("Casa Grid")
            .street("Carrera 19")
            .zone("Norte")
            .city("Armenia")
            .coordX(10.5)
            .coordY(8.3)
            .build();
        
        // Dirección con ambos sistemas
        addressWithBoth = Address.builder()
            .alias("Casa Dual")
            .street("Avenida Bolívar")
            .zone("Sur")
            .city("Armenia")
            .coordX(5.0)
            .coordY(5.0)
            .gpsLatitude(4.540000)
            .gpsLongitude(-75.670000)
            .build();
    }
    
    @Test
    public void testHasGpsCoordinates() {
        assertTrue(addressWithGPS.hasGpsCoordinates(), 
            "Dirección con GPS debe retornar true");
        
        assertFalse(addressWithGrid.hasGpsCoordinates(), 
            "Dirección sin GPS debe retornar false");
        
        assertTrue(addressWithBoth.hasGpsCoordinates(), 
            "Dirección con ambos sistemas debe retornar true");
    }
    
    @Test
    public void testSetGpsCoordinates() {
        Address address = Address.builder()
            .alias("Test")
            .street("Calle Test")
            .city("Armenia")
            .coordX(1.0)
            .coordY(1.0)
            .build();
        
        assertFalse(address.hasGpsCoordinates(), 
            "Inicialmente no debe tener GPS");
        
        address.setGpsCoordinates(4.5, -75.7);
        
        assertTrue(address.hasGpsCoordinates(), 
            "Después de setear debe tener GPS");
        
        assertEquals(4.5, address.getGpsLatitude(), 0.001);
        assertEquals(-75.7, address.getGpsLongitude(), 0.001);
    }
    
    @Test
    public void testGridCoordinates() {
        assertEquals(10.5, addressWithGrid.getCoordX(), 0.001);
        assertEquals(8.3, addressWithGrid.getCoordY(), 0.001);
    }
    
    @Test
    public void testDistanceToWithGrid() {
        Address other = Address.builder()
            .coordX(13.5)
            .coordY(11.3)
            .build();
        
        // Distancia Euclidiana: sqrt((13.5-10.5)^2 + (11.3-8.3)^2) = sqrt(9 + 9) = 4.24
        double distance = addressWithGrid.distanceTo(other);
        assertEquals(4.24, distance, 0.01, 
            "Distancia Euclidiana debe ser aproximadamente 4.24");
    }
    
    @Test
    public void testAddressImplementsSerializable() {
        // Verificar que Address es serializable
        assertTrue(addressWithGPS instanceof java.io.Serializable, 
            "Address debe implementar Serializable");
    }
    
    @Test
    public void testBuilderPattern() {
        Address address = Address.builder()
            .alias("Oficina")
            .street("Calle 20")
            .zone("Centro")
            .city("Armenia")
            .zipCode("630001")
            .complement("Piso 3")
            .coordX(7.5)
            .coordY(9.2)
            .gpsLatitude(4.535)
            .gpsLongitude(-75.675)
            .build();
        
        assertNotNull(address);
        assertEquals("Oficina", address.getAlias());
        assertEquals("Calle 20", address.getStreet());
        assertEquals("630001", address.getZipCode());
        assertEquals("Piso 3", address.getComplement());
        assertTrue(address.hasGpsCoordinates());
    }
    
    @Test
    public void testNullGpsCoordinates() {
        Address address = Address.builder()
            .alias("Test")
            .street("Test")
            .city("Armenia")
            .coordX(1.0)
            .coordY(1.0)
            .gpsLatitude(null)
            .gpsLongitude(null)
            .build();
        
        assertFalse(address.hasGpsCoordinates(), 
            "Coordenadas GPS null deben retornar false");
    }
    
    @Test
    public void testPartialGpsCoordinates() {
        Address addressWithOnlyLat = Address.builder()
            .alias("Test1")
            .street("Test")
            .city("Armenia")
            .coordX(1.0)
            .coordY(1.0)
            .gpsLatitude(4.5)
            .gpsLongitude(null)
            .build();
        
        assertFalse(addressWithOnlyLat.hasGpsCoordinates(), 
            "Solo latitud no es suficiente");
        
        Address addressWithOnlyLng = Address.builder()
            .alias("Test2")
            .street("Test")
            .city("Armenia")
            .coordX(1.0)
            .coordY(1.0)
            .gpsLatitude(null)
            .gpsLongitude(-75.7)
            .build();
        
        assertFalse(addressWithOnlyLng.hasGpsCoordinates(), 
            "Solo longitud no es suficiente");
    }
}
