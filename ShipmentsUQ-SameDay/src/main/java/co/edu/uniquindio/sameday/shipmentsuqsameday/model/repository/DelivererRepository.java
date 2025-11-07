package co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repositorio para la gestión de repartidores
 */
public class DelivererRepository extends BaseRepository<Deliverer> {
    
    @Override
    protected UUID getEntityId(Deliverer deliverer) {
        return deliverer.getId();
    }
    
    @Override
    protected void setEntityId(Deliverer deliverer, UUID id) {
        deliverer.setId(id);
    }
    
    /**
     * Busca repartidores por estado de disponibilidad
     * @param status estado de disponibilidad
     * @return lista de repartidores con el estado especificado
     */
    public List<Deliverer> findByStatus(DelivererStatus status) {
        return entities.values().stream()
                .filter(d -> d.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca repartidores por zona
     * @param zone zona asignada
     * @return lista de repartidores en la zona especificada
     */
    public List<Deliverer> findByZone(String zone) {
        return entities.values().stream()
                .filter(d -> d.getZone().equalsIgnoreCase(zone))
                .collect(Collectors.toList());
    }
    
    /**
     * Busca repartidores disponibles en una zona específica
     * @param zone zona de búsqueda
     * @return lista de repartidores disponibles en la zona
     */
    public List<Deliverer> findAvailableByZone(String zone) {
        return entities.values().stream()
                .filter(d -> d.getZone().equalsIgnoreCase(zone) && 
                           d.getStatus() == DelivererStatus.AVAILABLE)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca repartidor por número de documento
     * @param document número de documento
     * @return repartidor encontrado o empty si no existe
     */
    public Optional<Deliverer> findByDocument(String document) {
        return entities.values().stream()
                .filter(d -> d.getDocument().equals(document))
                .findFirst();
    }
    
    /**
     * Busca repartidores por calificación mínima
     * @param minRating calificación mínima (1-5)
     * @return lista de repartidores con calificación mayor o igual
     */
    public List<Deliverer> findByMinimumRating(double minRating) {
        return entities.values().stream()
                .filter(d -> d.getAverageRating() >= minRating)
                .collect(Collectors.toList());
    }
}