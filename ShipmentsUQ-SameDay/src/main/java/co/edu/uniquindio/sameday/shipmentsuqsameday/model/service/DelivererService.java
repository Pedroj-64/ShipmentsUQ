package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.DelivererRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de repartidores
 */
public class DelivererService implements Service<Deliverer, DelivererRepository> {
    private final DelivererRepository repository;
    
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
}