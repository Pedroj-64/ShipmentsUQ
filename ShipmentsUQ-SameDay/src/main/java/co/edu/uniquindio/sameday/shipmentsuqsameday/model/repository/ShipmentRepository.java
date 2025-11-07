package co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentPriority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repositorio para la gestión de envíos
 */
public class ShipmentRepository extends BaseRepository<Shipment> {
    
    @Override
    protected UUID getEntityId(Shipment shipment) {
        return shipment.getId();
    }
    
    @Override
    protected void setEntityId(Shipment shipment, UUID id) {
        shipment.setId(id);
    }
    
    /**
     * Busca envíos por estado
     * @param status estado del envío
     * @return lista de envíos en el estado especificado
     */
    public List<Shipment> findByStatus(ShipmentStatus status) {
        return entities.values().stream()
                .filter(e -> e.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca envíos por usuario
     * @param user usuario del envío
     * @return lista de envíos del usuario
     */
    public List<Shipment> findByUser(User user) {
        return entities.values().stream()
                .filter(e -> e.getUser().equals(user))
                .collect(Collectors.toList());
    }
    
    /**
     * Busca envíos por repartidor
     * @param deliverer repartidor asignado
     * @return lista de envíos asignados al repartidor
     */
    public List<Shipment> findByDeliverer(Deliverer deliverer) {
        return entities.values().stream()
                .filter(e -> e.getDeliverer() != null && 
                           e.getDeliverer().getId().equals(deliverer.getId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Busca envíos por rango de fechas
     * @param startDate fecha inicial
     * @param endDate fecha final
     * @return lista de envíos en el rango especificado
     */
    public List<Shipment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return entities.values().stream()
                .filter(e -> !e.getCreationDate().isBefore(startDate) &&
                           !e.getCreationDate().isAfter(endDate))
                .collect(Collectors.toList());
    }
    
    /**
     * Busca envíos por prioridad
     * @param priority prioridad del envío
     * @return lista de envíos con la prioridad especificada
     */
    public List<Shipment> findByPriority(ShipmentPriority priority) {
        return entities.values().stream()
                .filter(e -> e.getPriority() == priority)
                .collect(Collectors.toList());
    }
}