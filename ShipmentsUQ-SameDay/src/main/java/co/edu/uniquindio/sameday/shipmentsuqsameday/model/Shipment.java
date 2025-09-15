package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentPriority;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.Observable;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.Observer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Clase que representa un envío en el sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment implements Observable {
    private UUID id;
    private User user;
    private Deliverer deliverer;
    private Address origin;
    private Address destination;
    private double weight;
    private double volume;
    private double cost;
    private ShipmentStatus status;
    private ShipmentPriority priority;
    private LocalDateTime creationDate;
    private LocalDateTime assignmentDate;
    private LocalDateTime deliveryDate;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private boolean hasInsurance;
    private boolean isFragile;
    private String specialInstructions;
    
    @Builder.Default
    private List<Incident> incidents = new ArrayList<>();
    
    @Builder.Default
    private List<Observer> observers = new ArrayList<>();
    
    /**
     * Calcula el tiempo estimado de entrega en minutos
     * @return tiempo estimado en minutos
     */
    public int calculateEstimatedTime() {
        double distance = origin.calculateDistance(destination);
        int baseTime = (int) (distance * 3); // 3 minutos por kilómetro
        
        // Ajustes por prioridad
        switch (priority) {
            case URGENT:
                return (int) (baseTime * 0.7); // 30% más rápido
            case PRIORITY:
                return (int) (baseTime * 0.85); // 15% más rápido
            default:
                return baseTime;
        }
    }
    
    /**
     * Registra una nueva incidencia en el envío
     * @param incident incidencia a registrar
     */
    public void addIncident(Incident incident) {
        incidents.add(incident);
        status = ShipmentStatus.INCIDENT;
        notifyObservers("INCIDENT_REGISTERED", incident);
    }

    /**
     * Gets the distance between origin and destination
     * @return distance in kilometers
     */
    public double getDistance() {
        return origin.calculateDistance(destination);
    }
    
    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }
    
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers(String event, Object data) {
        for (Observer observer : observers) {
            observer.update(event, data);
        }
    }
    
    /**
     * Actualiza el estado del envío y notifica a los observadores
     * @param newStatus nuevo estado del envío
     */
    public void updateStatus(ShipmentStatus newStatus) {
        ShipmentStatus previousStatus = this.status;
        this.status = newStatus;
        
        switch (newStatus) {
            case ASSIGNED:
                this.assignmentDate = LocalDateTime.now();
                break;
            case DELIVERED:
                this.deliveryDate = LocalDateTime.now();
                break;
            case PENDING:
            case IN_TRANSIT:
            case INCIDENT:
            case CANCELLED:
            case PENDING_REASSIGNMENT:
                // No special action needed for these states
                break;
        }
        
        notifyObservers("STATUS_CHANGED", 
            new Object[]{previousStatus, newStatus});
    }
}