package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Incident;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.IncidentType;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IDistanceCalculator;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.ShipmentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.EuclideanDistanceCalculator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para la gestión de envíos
 */
public class ShipmentService implements Service<Shipment, ShipmentRepository> {
    private final ShipmentRepository repository;
    private final RateService rateService;
    private final DelivererService delivererService;
    private final IDistanceCalculator distanceCalculator;
    private final IncidentService incidentService;

    public ShipmentService(
            ShipmentRepository repository,
            RateService rateService,
            DelivererService delivererService,
            IncidentService incidentService) {
        this.repository = repository;
        this.rateService = rateService;
        this.delivererService = delivererService;
        this.distanceCalculator = new EuclideanDistanceCalculator();
        this.incidentService = incidentService;
    }

    @Override
    public ShipmentRepository getRepository() {
        return repository;
    }

    /**
     * Reasigna un envío a un nuevo repartidor
     * 
     * @param shipmentId ID del envío a reasignar
     * @param reason     motivo de la reasignación
     * @return envío con el nuevo repartidor asignado
     * @throws IllegalArgumentException si el envío no existe
     * @throws IllegalStateException    si el envío no puede ser reasignado o no hay
     *                                  repartidores disponibles
     */
    public Shipment reassignShipment(UUID shipmentId, String reason) {
        // Obtener y validar el envío
        Shipment shipment = repository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Envío no encontrado"));

        // Validar que el envío pueda ser reasignado
        if (!canBeReassigned(shipment)) {
            throw new IllegalStateException("El envío no puede ser reasignado en su estado actual");
        }

        // Obtener el repartidor actual
        Deliverer currentDeliverer = shipment.getDeliverer();
        if (currentDeliverer != null) {
            // Liberar al repartidor actual
            delivererService.updateDelivererStatus(currentDeliverer.getId(), DelivererStatus.AVAILABLE);
        }

        // Buscar un nuevo repartidor disponible
        Deliverer newDeliverer = findAvailableDeliverer(shipment);
        if (newDeliverer == null) {
            throw new IllegalStateException("No hay repartidores disponibles para reasignar el envío");
        }

        // Actualizar el envío
        shipment.setDeliverer(newDeliverer);
        shipment.setStatus(ShipmentStatus.ASSIGNED); // Reasignado y listo para continuar
        shipment.setAssignmentDate(LocalDateTime.now());

        // Actualizar instrucciones especiales
        String updatedInstructions = (shipment.getSpecialInstructions() != null
                ? shipment.getSpecialInstructions() + "\n"
                : "") + "Reasignado: " + reason;
        shipment.setSpecialInstructions(updatedInstructions);

        // Actualizar el estado del nuevo repartidor
        delivererService.updateDelivererStatus(newDeliverer.getId(), DelivererStatus.IN_SERVICE);

        // Guardar los cambios
        return repository.save(shipment);
    }

    /**
     * Verifica si un envío puede ser reasignado
     * 
     * @param shipment envío a verificar
     * @return true si el envío puede ser reasignado
     */
    private boolean canBeReassigned(Shipment shipment) {
        return shipment.getStatus() != ShipmentStatus.DELIVERED &&
                shipment.getStatus() != ShipmentStatus.CANCELLED;
    }

    /**
     * Busca un repartidor disponible para un envío
     * 
     * @param shipment envío que necesita repartidor
     * @return repartidor disponible o null si no hay ninguno
     */
    private Deliverer findAvailableDeliverer(Shipment shipment) {
        // Obtenemos la zona de entrega del envío
        String destinationZone = shipment.getDestination().getZone();
        
        // Buscamos repartidores disponibles en esa zona
        List<Deliverer> availableDeliverers = delivererService.findAvailableDeliverersInZone(destinationZone);
        
        if (availableDeliverers.isEmpty()) {
            return null;
        }
        
        Deliverer bestDeliverer = null;
        int minShipments = Integer.MAX_VALUE;
        double bestRating = -1;
        
        // Buscamos el repartidor con mejor calificación y menor carga laboral
        for (Deliverer deliverer : availableDeliverers) {
            int currentShipments = deliverer.getCurrentShipments().size();
            double rating = deliverer.getAverageRating();
            
            // Si tiene menos envíos que el mínimo actual, lo seleccionamos
            if (currentShipments < minShipments) {
                minShipments = currentShipments;
                bestDeliverer = deliverer;
                bestRating = rating;
            } 
            // Si tiene los mismos envíos, comparamos por calificación
            else if (currentShipments == minShipments && rating > bestRating) {
                bestDeliverer = deliverer;
                bestRating = rating;
            }
        }
        
        return bestDeliverer;
    }

    /**
     * Calcula la distancia total del envío utilizando coordenadas cartesianas (X,Y)
     * 
     * @param shipment envío del cual calcular la distancia
     * @return distancia en unidades de la cuadrícula
     */
    public double calculateShipmentDistance(Shipment shipment) {
        if (shipment.getOrigin() == null || shipment.getDestination() == null) {
            throw new IllegalArgumentException("El envío debe tener origen y destino definidos");
        }

        return distanceCalculator.calculateDistance(
                shipment.getOrigin(),
                shipment.getDestination());
    }

    /**
     * Crea un nuevo envío
     * 
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
     * 
     * @param shipment  envío a asignar
     * @param deliverer repartidor a asignar
     * @return envío actualizado
     * @throws IllegalStateException si el envío ya tiene repartidor o el repartidor
     *                               no está disponible
     */
    public Shipment assignDeliverer(Shipment shipment, Deliverer deliverer) {
        if (shipment.getDeliverer() != null) {
            throw new IllegalStateException("El envío ya tiene un repartidor asignado");
        }

        if (delivererService.assignShipment(deliverer, shipment)) {
            shipment.setDeliverer(deliverer);
            shipment.setStatus(ShipmentStatus.ASSIGNED);
            shipment.setAssignmentDate(LocalDateTime.now());
            return repository.update(shipment);
        } else {
            throw new IllegalStateException("El repartidor no puede aceptar más envíos en este momento");
        }
    }

    /**
     * Marca un envío como completado
     * 
     * @param shipment envío a completar
     * @param rating   calificación del servicio (1-5)
     * @return envío actualizado
     */
    public Shipment completeShipment(Shipment shipment, double rating) {
        Deliverer deliverer = shipment.getDeliverer();
        if (deliverer == null) {
            throw new IllegalStateException("El envío no tiene repartidor asignado");
        }

        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setDeliveryDate(LocalDateTime.now());

        delivererService.completeShipment(deliverer, shipment);
        return repository.update(shipment);
    }

    /**
     * Reporta una incidencia en un envío
     * 
     * @param incident incidencia a reportar
     * @return envío actualizado
     */
    public Shipment reportIncident(Incident incident) {
        Shipment shipment = incident.getShipment();

        // Registrar la incidencia
        incidentService.create(incident);

        // Verificar si la incidencia requiere reasignación basado en su tipo
        if (incident.getType() == IncidentType.INACCESSIBLE_ZONE ||
                incident.getType() == IncidentType.DELIVERER_UNAVAILABLE) {
            shipment.setStatus(ShipmentStatus.PENDING_REASSIGNMENT);
            // Notificar al repartidor actual
            if (shipment.getDeliverer() != null) {
                delivererService.updateDelivererStatus(
                    shipment.getDeliverer().getId(), 
                    DelivererStatus.AVAILABLE
                );
            }
        }

        return repository.update(shipment);
    }

    /**
     * Obtiene los envíos pendientes de una zona
     * 
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