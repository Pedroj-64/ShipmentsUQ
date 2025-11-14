package co.edu.uniquindio.sameday.shipmentsuqsameday.test;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase Coordinates
 * Valida cálculo de distancias con Haversine y verificación de área de servicio
 */
public class CoordinatesTest {
    
    private Coordinates armeniaCenter;
    private Coordinates nearbyPoint;
    private Coordinates farPoint;
    
    @BeforeEach
    public void setUp() {
        // Centro de Armenia, Quindío
        armeniaCenter = new Coordinates(4.533889, -75.681111);
        
        // Punto cercano (~5 km)
        nearbyPoint = new Coordinates(4.570000, -75.650000);
        
        // Punto lejano (~50 km) - Fuera del área de servicio
        farPoint = new Coordinates(4.813889, -75.681111);
    }
    
    @Test
    public void testDistanceToSamePoint() {
        double distance = armeniaCenter.distanceTo(armeniaCenter);
        assertEquals(0.0, distance, 0.001, "La distancia a sí mismo debe ser 0");
    }
    
    @Test
    public void testDistanceCalculation() {
        double distance = armeniaCenter.distanceTo(nearbyPoint);
        
        // Verificar que la distancia esté en un rango razonable (aprox. 5-6 km)
        assertTrue(distance > 4.0 && distance < 7.0, 
            "La distancia debe estar entre 4 y 7 km, actual: " + distance);
    }
    
    @Test
    public void testDistanceIsSymmetric() {
        double distance1 = armeniaCenter.distanceTo(nearbyPoint);
        double distance2 = nearbyPoint.distanceTo(armeniaCenter);
        
        assertEquals(distance1, distance2, 0.001, 
            "La distancia debe ser simétrica: A->B = B->A");
    }
    
    @Test
    public void testIsInServiceArea() {
        assertTrue(armeniaCenter.isInServiceArea(), 
            "El centro de Armenia debe estar en el área de servicio");
        
        assertTrue(nearbyPoint.isInServiceArea(), 
            "Un punto a 5km debe estar en el área de servicio (radio 20km)");
    }
    
    @Test
    public void testIsOutsideServiceArea() {
        assertFalse(farPoint.isInServiceArea(), 
            "Un punto a 50km debe estar fuera del área de servicio");
    }
    
    @Test
    public void testDefaultConstructor() {
        Coordinates defaultCoords = new Coordinates();
        
        assertEquals(Coordinates.ARMENIA_CENTER.getLatitude(), 
            defaultCoords.getLatitude(), 0.001, 
            "Coordenadas por defecto deben ser el centro de Armenia");
        
        assertEquals(Coordinates.ARMENIA_CENTER.getLongitude(), 
            defaultCoords.getLongitude(), 0.001, 
            "Coordenadas por defecto deben ser el centro de Armenia");
    }
    
    @Test
    public void testValidCoordinates() {
        Coordinates valid = new Coordinates(4.5, -75.7);
        
        assertTrue(valid.getLatitude() >= -90 && valid.getLatitude() <= 90,
            "Latitud debe estar entre -90 y 90");
        
        assertTrue(valid.getLongitude() >= -180 && valid.getLongitude() <= 180,
            "Longitud debe estar entre -180 y 180");
    }
    
    @Test
    public void testHaversineAccuracy() {
        // Armenia a Bogotá (aprox. 200 km en línea recta)
        Coordinates bogota = new Coordinates(4.711, -74.0721);
        double distance = armeniaCenter.distanceTo(bogota);
        
        // Verificar que esté en un rango razonable
        assertTrue(distance > 150 && distance < 250, 
            "La distancia Armenia-Bogotá debe estar entre 150-250 km, actual: " + distance);
    }
}
