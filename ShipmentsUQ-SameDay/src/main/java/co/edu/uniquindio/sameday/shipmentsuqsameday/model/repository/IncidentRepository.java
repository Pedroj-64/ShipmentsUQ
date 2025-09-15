package co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Incident;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.IncidentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repositorio para la gestión de incidencias
 */
public class IncidentRepository extends BaseRepository<Incident> {
    
    @Override
    protected UUID getEntityId(Incident incident) {
        return incident.getId();
    }
    
    @Override
    protected void setEntityId(Incident incident, UUID id) {
        incident.setId(id);
    }
    
    /**
     * Busca incidencias por tipo
     * @param type tipo de incidencia
     * @return lista de incidencias del tipo especificado
     */
    public List<Incident> findByType(IncidentType type) {
        return entities.values().stream()
                .filter(i -> i.getType() == type)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca incidencias por envío
     * @param shipment envío asociado
     * @return lista de incidencias del envío
     */
    public List<Incident> findByShipment(Shipment shipment) {
        return entities.values().stream()
                .filter(i -> i.getShipment().getId().equals(shipment.getId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Busca incidencias por estado de resolución
     * @param resolved true para buscar resueltas, false para pendientes
     * @return lista de incidencias según estado de resolución
     */
    public List<Incident> findByResolutionStatus(boolean resolved) {
        return entities.values().stream()
                .filter(i -> i.isResolved() == resolved)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca incidencias por rango de fechas
     * @param startDate fecha inicial
     * @param endDate fecha final
     * @return lista de incidencias en el rango especificado
     */
    public List<Incident> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return entities.values().stream()
                .filter(i -> !i.getDate().isBefore(startDate) && 
                           !i.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene el número total de incidencias pendientes
     * @return cantidad de incidencias sin resolver
     */
    public long countPendingIncidents() {
        return entities.values().stream()
                .filter(i -> !i.isResolved())
                .count();
    }
}