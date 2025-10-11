package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IGridCoordinate;
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
    
    /** Constante para la serialización */
    private static final long serialVersionUID = 1L;
    private UUID id;
    private String name;
    private String document;
    private String phone;
    private DelivererStatus status;
    private String zone;
    private double averageRating;
    private int totalDeliveries;
    private double currentX;  // Coordenada X actual en el GridMap
    private double currentY;  // Coordenada Y actual en el GridMap
    
    @Builder.Default
    private List<Shipment> currentShipments = new ArrayList<>();
    
    @Builder.Default
    private List<Shipment> shipmentHistory = new ArrayList();
    
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
}