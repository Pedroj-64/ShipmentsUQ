package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentPriority;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.Observable;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.Observer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
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
public class Shipment implements Observable, Serializable {
    
    /** Constante para la serialización */
    private static final long serialVersionUID = 1L;
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
    private ShipmentDetails details;
    
    @Builder.Default
    private List<Incident> incidents = new ArrayList<>();
    
    @Builder.Default
    private List<Observer> observers = new ArrayList<>();
    
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
}