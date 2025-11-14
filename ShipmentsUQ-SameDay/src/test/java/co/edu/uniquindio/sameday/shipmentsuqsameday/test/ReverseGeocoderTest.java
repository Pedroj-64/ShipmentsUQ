package co.edu.uniquindio.sameday.shipmentsuqsameday.test;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.ReverseGeocoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para ReverseGeocoder
 * NOTA: Tests marcados como @Disabled requieren conexión a internet
 * y respetan rate limiting de 1 req/seg
 */
public class ReverseGeocoderTest {
    
    private ReverseGeocoder geocoder;
    
    @BeforeEach
    public void setUp() {
        geocoder = ReverseGeocoder.getInstance();
    }
    
    @Test
    public void testSingletonPattern() {
        ReverseGeocoder instance1 = ReverseGeocoder.getInstance();
        ReverseGeocoder instance2 = ReverseGeocoder.getInstance();
        
        assertSame(instance1, instance2, 
            "getInstance() debe retornar la misma instancia (Singleton)");
    }
    
    @Test
    @Disabled("Requiere conexión a internet - Ejecutar manualmente")
    public void testReverseGeocodeArmenia() throws InterruptedException {
        // Centro de Armenia, Quindío
        double lat = 4.533889;
        double lng = -75.681111;
        
        Map<String, String> result = geocoder.reverseGeocode(lat, lng);
        
        assertNotNull(result, "Resultado no debe ser null");
        assertFalse(result.isEmpty(), "Resultado no debe estar vacío");
        
        // Verificar que contenga componentes esperados
        assertTrue(result.containsKey("fullAddress"), 
            "Debe contener dirección completa");
        
        String fullAddress = result.get("fullAddress");
        System.out.println("Dirección obtenida: " + fullAddress);
        
        // Debe mencionar Armenia o Quindío
        assertTrue(fullAddress.contains("Armenia") || fullAddress.contains("Quindío"),
            "Dirección debe mencionar Armenia o Quindío");
    }
    
    @Test
    @Disabled("Requiere conexión a internet - Ejecutar manualmente")
    public void testFormatColombianAddress() throws InterruptedException {
        double lat = 4.533889;
        double lng = -75.681111;
        
        Map<String, String> components = geocoder.reverseGeocode(lat, lng);
        String formatted = geocoder.formatColombianAddress(components);
        
        assertNotNull(formatted, "Dirección formateada no debe ser null");
        assertFalse(formatted.isEmpty(), "Dirección formateada no debe estar vacía");
        
        System.out.println("Dirección formateada: " + formatted);
    }
    
    @Test
    @Disabled("Requiere conexión a internet - Ejecutar manualmente")
    public void testGetFormattedAddress() throws InterruptedException {
        double lat = 4.533889;
        double lng = -75.681111;
        
        String address = geocoder.getFormattedAddress(lat, lng);
        
        assertNotNull(address, "Dirección no debe ser null");
        assertFalse(address.isEmpty(), "Dirección no debe estar vacía");
        
        System.out.println("getFormattedAddress(): " + address);
    }
    
    @Test
    public void testFormatColombianAddressWithNullComponents() {
        String formatted = geocoder.formatColombianAddress(null);
        assertNull(formatted, "Formato con null debe retornar null");
    }
    
    @Test
    public void testFormatColombianAddressWithEmptyComponents() {
        Map<String, String> empty = Map.of();
        String formatted = geocoder.formatColombianAddress(empty);
        assertNull(formatted, "Formato con mapa vacío debe retornar null");
    }
    
    @Test
    public void testFormatColombianAddressWithFullAddress() {
        Map<String, String> components = Map.of(
            "fullAddress", "Calle 14 #25-45, Centro, Armenia, Quindío, Colombia"
        );
        
        String formatted = geocoder.formatColombianAddress(components);
        
        assertNotNull(formatted);
        assertTrue(formatted.contains("Armenia") || formatted.contains("Calle 14"),
            "Debe contener información de la dirección");
    }
    
    @Test
    public void testFormatColombianAddressWithComponents() {
        Map<String, String> components = Map.of(
            "road", "Calle 14",
            "houseNumber", "25-45",
            "neighbourhood", "Centro",
            "city", "Armenia",
            "state", "Quindío"
        );
        
        String formatted = geocoder.formatColombianAddress(components);
        
        assertNotNull(formatted);
        assertTrue(formatted.contains("Calle 14"), "Debe contener la calle");
        assertTrue(formatted.contains("Armenia"), "Debe contener la ciudad");
    }
    
    @Test
    @Disabled("Test de rate limiting - Ejecutar manualmente con precaución")
    public void testRateLimiting() throws InterruptedException {
        double lat = 4.533889;
        double lng = -75.681111;
        
        long start = System.currentTimeMillis();
        
        // Primera llamada
        geocoder.reverseGeocode(lat, lng);
        
        long afterFirst = System.currentTimeMillis();
        
        // Segunda llamada - debe esperar automáticamente
        geocoder.reverseGeocode(lat, lng);
        
        long afterSecond = System.currentTimeMillis();
        
        long timeBetweenCalls = afterSecond - afterFirst;
        
        // Debe haber esperado al menos 1100ms
        assertTrue(timeBetweenCalls >= 1100, 
            "Rate limiting debe esperar al menos 1100ms entre llamadas. Actual: " + timeBetweenCalls + "ms");
        
        System.out.println("Tiempo entre llamadas: " + timeBetweenCalls + "ms");
    }
    
    @Test
    @Disabled("Requiere conexión a internet - Ejecutar manualmente")
    public void testInvalidCoordinates() throws InterruptedException {
        // Coordenadas en medio del océano
        double lat = 0.0;
        double lng = 0.0;
        
        Map<String, String> result = geocoder.reverseGeocode(lat, lng);
        
        // Debería retornar algo o null, pero no debe lanzar excepción
        assertDoesNotThrow(() -> {
            geocoder.reverseGeocode(lat, lng);
        });
    }
}
