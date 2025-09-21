package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Incident;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.IncidentType;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.IncidentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar la lógica de negocio relacionada con incidentes
 */
public class IncidentService implements Service<Incident, IncidentRepository> {
    private final IncidentRepository repository;
    private final ShipmentReassignmentService reassignmentService;

    public IncidentService(IncidentRepository repository, ShipmentReassignmentService reassignmentService) {
        this.repository = repository;
        this.reassignmentService = reassignmentService;
    }

    @Override
    public IncidentRepository getRepository() {
        return repository;
    }

    /**
     * Marca un incidente como resuelto
     * @param incidentId ID del incidente
     * @param solution descripción de cómo se resolvió el incidente
     */
    public void resolveIncident(UUID incidentId, String solution) {
        repository.findById(incidentId).ifPresent(incident -> {
            incident.setResolved(true);
            incident.setResolutionDate(LocalDateTime.now());
            incident.setSolution(solution);

            if (requiresReassignment(incidentId)) {
                reassignmentService.handleReassignment(incidentId, solution);
            }

            repository.update(incident);
        });
    }

    /**
     * Verifica si un incidente requiere reasignación del envío
     * @param incidentId ID del incidente
     * @return true si es necesario reasignar el envío
     */
    public boolean requiresReassignment(UUID incidentId) {
        return repository.findById(incidentId)
                .map(incident -> incident.getType() == IncidentType.INACCESSIBLE_ZONE ||
                                incident.getType() == IncidentType.DELIVERER_UNAVAILABLE)
                .orElse(false);
    }

    /**
     * Obtiene todos los incidentes no resueltos
     * @return lista de incidentes no resueltos
     */
    public List<Incident> getUnresolvedIncidents() {
        return repository.findUnresolved();
    }

    /**
     * Obtiene todos los incidentes de un envío
     * @param shipmentId ID del envío
     * @return lista de incidentes del envío
     */
    public List<Incident> getShipmentIncidents(UUID shipmentId) {
        return repository.findByShipmentId(shipmentId);
    }
}