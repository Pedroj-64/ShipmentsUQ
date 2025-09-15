package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class Deliverer {
    private UUID id;
    private String name;
    private String document;
    private String phone;
    private DelivererStatus status;
    private String zone;
    private double averageRating;
    private int totalDeliveries;
    
    @Builder.Default
    private List<Shipment> currentShipments = new ArrayList<>();
    
    @Builder.Default
    private List<Shipment> shipmentHistory = new ArrayList<>();
    
    /**
     * Asigna un nuevo envío al repartidor
     * @param shipment envío a asignar
     * @return true si se pudo asignar el envío
     */
    public boolean assignShipment(Shipment shipment) {
        if (status != DelivererStatus.AVAILABLE || currentShipments.size() >= 3) {
            return false;
        }
        currentShipments.add(shipment);
        if (currentShipments.size() >= 3) {
            status = DelivererStatus.IN_SERVICE;
        }
        return true;
    }
    
    /**
     * Completa un envío y lo mueve al historial
     * @param shipment envío completado
     */
    public void completeShipment(Shipment shipment) {
        currentShipments.remove(shipment);
        shipmentHistory.add(shipment);
        totalDeliveries++;
        if (currentShipments.isEmpty()) {
            status = DelivererStatus.AVAILABLE;
        }
    }
    
    /**
     * Actualiza la calificación promedio del repartidor
     * @param newRating calificación a agregar (1-5)
     */
    public void updateRating(double newRating) {
        if (newRating < 1 || newRating > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5");
        }
        averageRating = ((averageRating * totalDeliveries) + newRating) / (totalDeliveries + 1);
    }
}