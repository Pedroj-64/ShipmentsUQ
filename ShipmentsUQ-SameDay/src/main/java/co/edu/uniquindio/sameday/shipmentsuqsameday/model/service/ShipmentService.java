package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Incident;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.command.AssignDelivererCommand;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.command.CancelShipmentCommand;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.command.CommandManager;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.IncidentType;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentPriority;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IDistanceCalculator;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IGridCoordinate;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.DelivererRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.ShipmentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.EuclideanDistanceCalculator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
    
    private static ShipmentService instance;

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
    
    /**
     * Obtiene la instancia única del servicio
     * @return instancia del servicio
     */
    public static synchronized ShipmentService getInstance() {
        if (instance == null) {
            System.err.println("ERROR: ShipmentService no ha sido inicializado. Inicializando con configuración por defecto.");
            // Crear versión simplificada para compatibilidad
            instance = new ShipmentService(
                new ShipmentRepository(), 
                null, // No inicializamos RateService 
                null, // No inicializamos DelivererService
                null  // No inicializamos IncidentService
            );
        }
        return instance;
    }
    
    /**
     * Establece la instancia del servicio con las dependencias proporcionadas
     * @param repository Repositorio de envíos
     * @param rateService Servicio de tarifas
     * @param delivererService Servicio de repartidores
     * @param incidentService Servicio de incidencias
     * @return instancia del servicio
     */
    public static synchronized ShipmentService getInstance(
            ShipmentRepository repository,
            RateService rateService,
            DelivererService delivererService,
            IncidentService incidentService) {
        instance = new ShipmentService(repository, rateService, delivererService, incidentService);
        return instance;
    }

    @Override
    public ShipmentRepository getRepository() {
        return repository;
    }
    
    /**
     * Calcula la tarifa de envío basada en distancia, peso y prioridad
     * @param origin dirección de origen
     * @param destination dirección de destino
     * @param weight peso en kg
     * @param priority prioridad del envío
     * @return tarifa calculada en moneda local
     */
    public double calculateShippingRate(IGridCoordinate origin, IGridCoordinate destination, double weight, ShipmentPriority priority) {
        // Calcular distancia entre origen y destino
        double distance = origin.distanceTo(destination);
        
        // Tarifa base según distancia (10 por unidad de distancia)
        double baseRate = distance * 10.0;
        
        // Ajuste por peso (5 por kg)
        double weightRate = weight * 5.0;
        
        // Multiplicador por prioridad
        double priorityMultiplier = 1.0;
        switch (priority) {
            case STANDARD:
                priorityMultiplier = 1.0;
                break;
            case PRIORITY:
                priorityMultiplier = 1.5;
                break;
            case URGENT:
                priorityMultiplier = 2.0;
                break;
        }
        
        return (baseRate + weightRate) * priorityMultiplier;
    }
    
    /**
     * Crea un nuevo envío y asigna automáticamente el repartidor más cercano
     * @param user usuario que crea el envío
     * @param origin dirección de origen
     * @param destination dirección de destino
     * @param weight peso en kg
     * @param dimensions dimensiones en formato texto
     * @param priority prioridad del envío
     * @return el envío creado
     */
    public Shipment createShipment(User user, IGridCoordinate origin, IGridCoordinate destination, 
                                  double weight, String dimensions, ShipmentPriority priority) {
        // Calcular costo
        double cost = calculateShippingRate(origin, destination, weight, priority);
        
        // Crear nuevo envío
        Shipment shipment = Shipment.builder()
                .id(UUID.randomUUID())
                .user(user)
                .origin((Address) origin)
                .destination((Address) destination)
                .status(ShipmentStatus.PENDING)
                .priority(priority)
                .weight(weight)
                .specialInstructions(dimensions) // Guardamos las dimensiones en specialInstructions
                .cost(cost)
                .creationDate(LocalDateTime.now())
                .build();
        
        // Asignar repartidor más cercano disponible
        assignNearestDeliverer(shipment);
        
        // Guardar envío
        return repository.save(shipment);
    }
    
    /**
     * Asigna el repartidor disponible más cercano al origen del envío
     * @param shipment el envío al que asignar un repartidor
     */
    private void assignNearestDeliverer(Shipment shipment) {
        if (delivererService == null) {
            System.err.println("ERROR: DelivererService no inicializado en ShipmentService");
            return;
        }
        
        try {
            // Encontrar repartidores disponibles
            List<Deliverer> availableDeliverers = delivererService.findAvailableDeliverers();
            
            if (availableDeliverers.isEmpty()) {
                System.err.println("No hay repartidores disponibles. El envío quedará pendiente.");
                return;
            }
            
            // Ordenar por distancia al origen
            IGridCoordinate originCoord = shipment.getOrigin();
            
            // Encontrar el más cercano
            Optional<Deliverer> nearestDeliverer = availableDeliverers.stream()
                    .min(Comparator.comparingDouble(deliverer -> 
                        deliverer.distanceTo(originCoord)));
            
            if (nearestDeliverer.isPresent()) {
                Deliverer deliverer = nearestDeliverer.get();
                
                // Asignar el envío al repartidor
                deliverer.getCurrentShipments().add(shipment);
                
                // Si el repartidor tiene demasiados envíos, marcarlo como BUSY
                if (deliverer.getCurrentShipments().size() >= 3) {
                    deliverer.setStatus(DelivererStatus.BUSY);
                } else {
                    deliverer.setStatus(DelivererStatus.ACTIVE);
                }
                
                // Actualizar el repartidor
                delivererService.update(deliverer);
                
                // Actualizar el envío con el repartidor asignado
                shipment.setDeliverer(deliverer);
                shipment.setStatus(ShipmentStatus.ASSIGNED);
                shipment.setAssignmentDate(LocalDateTime.now());
            } else {
                System.err.println("Error al calcular el repartidor más cercano.");
            }
        } catch (Exception e) {
            System.err.println("Error al asignar repartidor: " + e.getMessage());
            e.printStackTrace();
        }
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
        if (shipment.getOrigin() == null) {
            throw new IllegalArgumentException("El envío debe tener un origen definido");
        }
        
        // Obtenemos la zona y las coordenadas de origen del envío
        String originZone = shipment.getOrigin().getZone();
        double originX = shipment.getOrigin().getCoordX();
        double originY = shipment.getOrigin().getCoordY();
        
        // Primero intentamos encontrar el repartidor más cercano al origen
        Deliverer nearestDeliverer = delivererService.findNearestAvailableDeliverer(originX, originY, originZone);
        
        // Si encontramos un repartidor cercano, lo devolvemos
        if (nearestDeliverer != null) {
            System.out.println("Repartidor asignado por proximidad: " + nearestDeliverer.getName() + 
                            " - Distancia: " + nearestDeliverer.distanceTo(new IGridCoordinate() {
                                @Override
                                public double getX() {
                                    return originX;
                                }
                                
                                @Override
                                public double getY() {
                                    return originY;
                                }
                            }));
            return nearestDeliverer;
        }
        
        // Plan B: buscar por zona y carga de trabajo (método anterior)
        List<Deliverer> availableDeliverers = delivererService.findAvailableDeliverersInZone(originZone);
        
        if (availableDeliverers.isEmpty()) {
            // Si no hay en la zona de origen, intentamos con cualquier repartidor disponible
            availableDeliverers = delivererService.getAvailableDeliverers();
            if (availableDeliverers.isEmpty()) {
                return null;
            }
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
        // Configurar el estado inicial y la fecha de creación
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setCreationDate(LocalDateTime.now());
        
        // Guardar el envío para asegurar que tenga un ID
        shipment = repository.save(shipment);
        
        // Guardar el estado para persistencia
        co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.DataManager.getInstance().saveState();
        
        // Intentar asignar automáticamente un repartidor
        try {
            Deliverer availableDeliverer = findAvailableDeliverer(shipment);
            
            if (availableDeliverer != null) {
                // Asignar el repartidor al envío
                shipment.setDeliverer(availableDeliverer);
                shipment.setStatus(ShipmentStatus.ASSIGNED);
                shipment.setAssignmentDate(LocalDateTime.now());
                
                // Actualizar el repartidor
                delivererService.assignShipment(availableDeliverer, shipment);
                
                // Actualizar el envío en la base de datos
                repository.update(shipment);
                
                System.out.println("Envío " + shipment.getId() + " asignado automáticamente al repartidor " 
                        + availableDeliverer.getName());
            } else {
                System.out.println("No se pudo asignar un repartidor automáticamente al envío " + shipment.getId());
            }
        } catch (Exception e) {
            System.err.println("Error al intentar asignar repartidor automáticamente: " + e.getMessage());
            e.printStackTrace();
        }
        
        return shipment;
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
            // Usar el patrón Command para asignar el repartidor
            AssignDelivererCommand assignCommand = new AssignDelivererCommand(shipment, deliverer);
            CommandManager.getInstance().executeCommand(assignCommand);
            
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
    
    /**
     * Obtiene todos los envíos de un usuario específico
     * @param userId ID del usuario
     * @return lista de envíos del usuario
     */
    public List<Shipment> findByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        
        // Filtrar todos los envíos para encontrar los del usuario especificado
        return repository.findAll().stream()
                .filter(shipment -> shipment.getUser() != null && 
                         shipment.getUser().getId().equals(userId))
                .toList();
    }
    
    /**
     * Intenta asignar un repartidor disponible a un envío pagado
     * @param shipmentId ID del envío a asignar
     * @return true si se asignó correctamente, false si no se pudo asignar
     */
    public boolean tryAssignDeliverer(UUID shipmentId) {
        if (shipmentId == null) {
            return false;
        }
        
        Optional<Shipment> shipmentOpt = repository.findById(shipmentId);
        if (shipmentOpt.isEmpty()) {
            return false;
        }
        
        Shipment shipment = shipmentOpt.get();
        
        // Solo se pueden asignar envíos pendientes
        if (shipment.getStatus() != ShipmentStatus.PENDING) {
            return false;
        }
        
        // Intentar asignar automáticamente un repartidor
        try {
            Deliverer availableDeliverer = findAvailableDeliverer(shipment);
            
            if (availableDeliverer != null) {
                // Asignar el repartidor al envío
                shipment.setDeliverer(availableDeliverer);
                shipment.setStatus(ShipmentStatus.ASSIGNED);
                shipment.setAssignmentDate(LocalDateTime.now());
                
                // Actualizar el repartidor
                delivererService.assignShipment(availableDeliverer, shipment);
                
                // Actualizar el envío en la base de datos
                repository.update(shipment);
                
                // Guardar el estado para persistencia
                co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.DataManager.getInstance().saveState();
                
                System.out.println("Envío " + shipment.getId() + " asignado automáticamente al repartidor " 
                        + availableDeliverer.getName());
                return true;
            } else {
                System.out.println("No se pudo asignar un repartidor automáticamente al envío " + shipment.getId());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error al intentar asignar un repartidor al envío " + shipment.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cancela un envío existente
     * @param shipmentId ID del envío a cancelar
     * @return true si se canceló correctamente
     */
    public boolean cancelShipment(UUID shipmentId) {
        if (shipmentId == null) {
            return false;
        }
        
        try {
            // Encontrar el envío
            Optional<Shipment> shipmentOpt = repository.findById(shipmentId);
            if (!shipmentOpt.isPresent()) {
                return false;
            }
            
            Shipment shipment = shipmentOpt.get();
            
            // Verificar que el envío esté en un estado cancelable
            if (shipment.getStatus() != ShipmentStatus.PENDING && 
                shipment.getStatus() != ShipmentStatus.ASSIGNED) {
                return false;
            }
            
            // Si tiene un repartidor asignado, actualizar su estado
            Deliverer deliverer = shipment.getDeliverer();
            if (deliverer != null) {
                // Quitar el envío de la lista del repartidor
                deliverer.getCurrentShipments().remove(shipment);
                
                // Actualizar estado del repartidor
                if (deliverer.getCurrentShipments().isEmpty()) {
                    deliverer.setStatus(DelivererStatus.AVAILABLE);
                } else {
                    deliverer.setStatus(DelivererStatus.ACTIVE);
                }
                
                // Actualizar el repartidor
                if (delivererService != null) {
                    delivererService.update(deliverer);
                }
            }
            
            // Usar el patrón Command para cancelar el envío
            CancelShipmentCommand cancelCommand = new CancelShipmentCommand(shipment);
            CommandManager.getInstance().executeCommand(cancelCommand);
            
            // Actualizar el envío en el repositorio
            repository.update(shipment);
            
            return true;
        } catch (Exception e) {
            System.err.println("Error al cancelar envío: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deshace la última operación realizada en envíos
     * @return true si se deshizo la operación correctamente
     */
    public boolean undoLastOperation() {
        try {
            boolean result = CommandManager.getInstance().undoLastCommand();
            return result;
        } catch (Exception e) {
            System.err.println("Error al deshacer operación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Rehace la última operación deshecha en envíos
     * @return true si se rehizo la operación correctamente
     */
    public boolean redoLastOperation() {
        try {
            boolean result = CommandManager.getInstance().redoLastCommand();
            return result;
        } catch (Exception e) {
            System.err.println("Error al rehacer operación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}