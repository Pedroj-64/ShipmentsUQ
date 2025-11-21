package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IGridCoordinate;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.strategy.GridCoordinateStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Clase que representa un repartidor en el sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deliverer implements Serializable, IGridCoordinate {
    
    private static final long serialVersionUID = 2L; // Incrementado para nueva versión
    private UUID id;
    private String name;
    private String document;
    private String phone;
    private DelivererStatus status;
    private String zone;
    private double averageRating;
    private int totalDeliveries;
    
    // Coordenadas del sistema Grid (mantener compatibilidad)
    private double currentX;
    private double currentY;
    
    // Coordenadas reales GPS (opcional, nueva funcionalidad)
    // Si son null, se usa solo el sistema de Grid
    private Double realLatitude;  // Usar Double (nullable) para compatibilidad con datos existentes
    private Double realLongitude; // Usar Double (nullable) para compatibilidad con datos existentes

    @Builder.Default
    private List<Shipment> currentShipments = new ArrayList<>();
    
    @Builder.Default
    private List<Shipment> shipmentHistory = new ArrayList<>();
    
    /**
     * Implementación de IGridCoordinate para obtener la coordenada X
     * @return coordenada X actual del repartidor
     */
    @Override
    public double getX() {
        return currentX;
    }
    
    /**
     * Implementación de IGridCoordinate para obtener la coordenada Y
     * @return coordenada Y actual del repartidor
     */
    @Override
    public double getY() {
        return currentY;
    }
    
    /**
     * Actualiza la posición actual del repartidor en el mapa
     * @param x coordenada X
     * @param y coordenada Y
     */
    public void updatePosition(double x, double y) {
        this.currentX = x;
        this.currentY = y;
    }
    
    /**
     * Actualiza las coordenadas reales GPS del repartidor
     * Automáticamente convierte GPS → Grid para mantener ambos sistemas sincronizados
     * @param latitude latitud GPS
     * @param longitude longitud GPS
     */
    public void updateRealPosition(double latitude, double longitude) {
        this.realLatitude = latitude;
        this.realLongitude = longitude;
        
        // Conversión automática GPS → Grid
        double[] gridCoords = GridCoordinateStrategy.convertRealToGrid(latitude, longitude);
        this.currentX = gridCoords[0];
        this.currentY = gridCoords[1];
    }
    
    /**
     * Verifica si el repartidor tiene coordenadas reales configuradas
     * @return true si tiene coordenadas GPS reales
     */
    public boolean hasRealCoordinates() {
        return realLatitude != null && realLongitude != null;
    }
    
    /**
     * Sincroniza las coordenadas: convierte entre Grid y GPS según cuál esté disponible
     * Útil para mantener compatibilidad con ambos sistemas
     */
    public void syncCoordinates() {
        if (hasRealCoordinates()) {
            // GPS existe → Convertir a Grid
            double[] gridCoords = GridCoordinateStrategy.convertRealToGrid(realLatitude, realLongitude);
            this.currentX = gridCoords[0];
            this.currentY = gridCoords[1];
        } else if (currentX != 0.0 || currentY != 0.0) {
            // Grid existe → Convertir a GPS
            double[] gpsCoords = GridCoordinateStrategy.convertGridToReal(currentX, currentY);
            this.realLatitude = gpsCoords[0];
            this.realLongitude = gpsCoords[1];
        }
    }
    
    /**
     * Actualiza la posición usando coordenadas Grid
     * Automáticamente convierte Grid → GPS para mantener ambos sistemas sincronizados
     * @param x coordenada X del Grid
     * @param y coordenada Y del Grid
     */
    public void updateGridPosition(double x, double y) {
        this.currentX = x;
        this.currentY = y;
        
        // Conversión automática Grid → GPS
        double[] gpsCoords = GridCoordinateStrategy.convertGridToReal(x, y);
        this.realLatitude = gpsCoords[0];
        this.realLongitude = gpsCoords[1];
    }
}