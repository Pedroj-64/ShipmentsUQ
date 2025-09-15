package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Incident;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentPriority;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.ShipmentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.DelivererRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de envíos
 */
public class ShipmentService implements Service<Shipment, ShipmentRepository> {
    private final ShipmentRepository repository;
    private final DelivererRepository delivererRepository;
    private final RateService rateService;
    
    // Constructor privado para Singleton
    private ShipmentService() {
        this.repository = new ShipmentRepository();
        this.delivererRepository = new DelivererRepository();
        this.rateService = RateService.getInstance();
    }
    
    // Holder estático para instancia única
    private static class SingletonHolder {
        private static final ShipmentService INSTANCE = new ShipmentService();
    }
    
    /**
     * Obtiene la instancia única del servicio
     * @return instancia del servicio
     */
    public static ShipmentService getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    @Override
    public ShipmentRepository getRepository() {
        return repository;
    }
    
    /**
     * Crea un nuevo envío
     * @param shipment envío a crear
     * @return envío creado
     */
    public Shipment createShipment(Shipment shipment) {
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setCreationDate(LocalDateTime.now());
        return repository.save(shipment);
    }
    
    /**
     * Asigna un repartidor a un envío
     * @param shipment envío a asignar
     * @param deliverer repartidor a asignar
     * @return envío actualizado
     * @throws IllegalStateException si el envío ya tiene repartidor o el repartidor no está disponible
     */
    public Shipment assignDeliverer(Shipment shipment, Deliverer deliverer) {
        if (shipment.getDeliverer() != null) {
            throw new IllegalStateException("El envío ya tiene un repartidor asignado");
        }
        if (deliverer.getStatus() != DelivererStatus.AVAILABLE) {
            throw new IllegalStateException("El repartidor no está disponible");
        }
        
        shipment.setDeliverer(deliverer);
        shipment.setStatus(ShipmentStatus.ASSIGNED);
        deliverer.assignShipment(shipment);
        
        delivererRepository.update(deliverer);
        return repository.update(shipment);
    }
    
    /**
     * Marca un envío como completado
     * @param shipment envío a completar
     * @param rating calificación del servicio (1-5)
     * @return envío actualizado
     */
    public Shipment completeShipment(Shipment shipment, double rating) {
        Deliverer deliverer = shipment.getDeliverer();
        if (deliverer == null) {
            throw new IllegalStateException("El envío no tiene repartidor asignado");
        }
        
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setDeliveryDate(LocalDateTime.now());
        deliverer.completeShipment(shipment);
        deliverer.updateRating(rating);
        
        delivererRepository.update(deliverer);
        return repository.update(shipment);
    }
    
    /**
     * Reporta una incidencia en un envío
     * @param incident incidencia a reportar
     * @return envío actualizado
     */
    public Shipment reportIncident(Incident incident) {
        Shipment shipment = incident.getShipment();
        shipment.addIncident(incident);
        
        if (incident.requiresReassignment()) {
            shipment.setStatus(ShipmentStatus.PENDING_REASSIGNMENT);
        }
        
        return repository.update(shipment);
    }
    
    /**
     * Obtiene los envíos pendientes de una zona
     * @param zone zona a buscar
     * @return lista de envíos pendientes
     */
    public List<Shipment> getPendingShipmentsByZone(String zone) {
        return repository.findByStatus(ShipmentStatus.PENDING).stream()
                .filter(s -> s.getDestination().getZone().equals(zone))
                .sorted((s1, s2) -> s2.getPriority().compareTo(s1.getPriority()))
                .toList();
    }
}