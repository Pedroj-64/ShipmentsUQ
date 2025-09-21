package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.DelivererRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para la gestión de repartidores y sus asignaciones
 */
public class DelivererService implements Service<Deliverer, DelivererRepository> {
    private final DelivererRepository repository;
    private static final int MAX_CONCURRENT_SHIPMENTS = 3;
    
    // Constructor privado para Singleton
    private DelivererService() {
        this.repository = new DelivererRepository();
    }
    
    // Holder estático para instancia única
    private static class SingletonHolder {
        private static final DelivererService INSTANCE = new DelivererService();
    }
    
    /**
     * Obtiene la instancia única del servicio
     * @return instancia del servicio
     */
    public static DelivererService getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    @Override
    public DelivererRepository getRepository() {
        return repository;
    }
    
    /**
     * Registra un nuevo repartidor
     * @param deliverer repartidor a registrar
     * @return repartidor registrado
     * @throws IllegalArgumentException si el documento ya está registrado
     */
    public Deliverer registerDeliverer(Deliverer deliverer) {
        if (repository.findByDocument(deliverer.getDocument()).isPresent()) {
            throw new IllegalArgumentException("El documento ya está registrado");
        }
        deliverer.setStatus(DelivererStatus.AVAILABLE);
        return repository.save(deliverer);
    }
    
    /**
     * Busca repartidores disponibles en una zona
     * @param zone zona a buscar
     * @return lista de repartidores disponibles ordenados por calificación
     */
    public List<Deliverer> findAvailableDeliverersInZone(String zone) {
        return repository.findAvailableByZone(zone).stream()
                .sorted(Comparator.comparingDouble(Deliverer::getAverageRating).reversed())
                .toList();
    }
    
    /**
     * Busca repartidores con calificación mínima
     * @param minRating calificación mínima (1-5)
     * @return lista de repartidores que cumplen el criterio
     */
    public List<Deliverer> findDeliverersByMinimumRating(double minRating) {
        return repository.findByMinimumRating(minRating);
    }
    
    /**
     * Actualiza el estado de un repartidor
     * @param deliverer repartidor a actualizar
     * @param status nuevo estado
     * @return repartidor actualizado
     */
    public Deliverer updateDelivererStatus(Deliverer deliverer, DelivererStatus status) {
        deliverer.setStatus(status);
        return repository.update(deliverer);
    }
    
    /**
     * Asigna un nuevo envío al repartidor
     * @param deliverer repartidor al que asignar el envío
     * @param shipment envío a asignar
     * @return true si se pudo asignar el envío
     */
    public boolean assignShipment(Deliverer deliverer, Shipment shipment) {
        if (!canAcceptShipment(deliverer)) {
            return false;
        }
        
        deliverer.getCurrentShipments().add(shipment);
        updateDelivererStatusBasedOnWorkload(deliverer);
        
        repository.update(deliverer);
        return true;
    }
    
    /**
     * Completa un envío para un repartidor
     * @param deliverer repartidor que completó el envío
     * @param shipment envío completado
     */
    public void completeShipment(Deliverer deliverer, Shipment shipment) {
        deliverer.getCurrentShipments().remove(shipment);
        deliverer.getShipmentHistory().add(shipment);
        
        updateDelivererStatusBasedOnWorkload(deliverer);
        repository.update(deliverer);
    }
    
    /**
     * Verifica si un repartidor puede aceptar más envíos
     */
    private boolean canAcceptShipment(Deliverer deliverer) {
        return deliverer.getStatus() == DelivererStatus.AVAILABLE && 
               deliverer.getCurrentShipments().size() < MAX_CONCURRENT_SHIPMENTS;
    }
    
    /**
     * Actualiza el estado del repartidor según su carga de trabajo
     */
    private void updateDelivererStatusBasedOnWorkload(Deliverer deliverer) {
        if (deliverer.getCurrentShipments().isEmpty()) {
            deliverer.setStatus(DelivererStatus.AVAILABLE);
        } else if (deliverer.getCurrentShipments().size() >= MAX_CONCURRENT_SHIPMENTS) {
            deliverer.setStatus(DelivererStatus.IN_SERVICE);
        }
    }

    /**
     * Actualiza el estado de un repartidor usando su ID
     * @param delivererId ID del repartidor a actualizar
     * @param status nuevo estado
     * @return repartidor actualizado
     * @throws IllegalArgumentException si el repartidor no existe
     */
    public Deliverer updateDelivererStatus(UUID delivererId, DelivererStatus status) {
        Deliverer deliverer = repository.findById(delivererId)
            .orElseThrow(() -> new IllegalArgumentException("Repartidor no encontrado"));
        return updateDelivererStatus(deliverer, status);
    }

    /**
     * Obtiene la lista de repartidores disponibles
     * @return lista de repartidores disponibles ordenados por calificación
     */
    public List<Deliverer> getAvailableDeliverers() {
        return repository.findAll().stream()
            .filter(d -> d.getStatus() == DelivererStatus.AVAILABLE)
            .sorted(Comparator.comparingDouble(Deliverer::getAverageRating).reversed())
            .toList();
    }
}