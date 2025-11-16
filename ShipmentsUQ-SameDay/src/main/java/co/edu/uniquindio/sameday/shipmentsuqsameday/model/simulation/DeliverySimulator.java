package co.edu.uniquindio.sameday.shipmentsuqsameday.model.simulation;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.NotificationPriority;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.NotificationType;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.notification.NotificationService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.Route;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.Waypoint;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.service.RouteCalculationService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.strategy.RoutingException;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.simulation.event.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * Servicio principal de simulación de entregas
 * Maneja múltiples simulaciones simultáneas y notifica eventos a listeners
 * 
 * Patrón: Singleton + Observer + Strategy
 */
public class DeliverySimulator {
    
    private static DeliverySimulator instance;
    
    // Simulaciones activas indexadas por ID de envío
    private final Map<UUID, DeliverySimulation> activeSimulations;
    
    // Listeners registrados para recibir eventos
    private final List<SimulationListener> listeners;
    
    // Scheduler para ejecutar actualizaciones periódicas
    private final ScheduledExecutorService scheduler;
    
    // Futures para controlar cada simulación
    private final Map<UUID, ScheduledFuture<?>> simulationTasks;
    
    // Servicios necesarios
    private final RouteCalculationService routeService;
    private final ShipmentService shipmentService;
    private final NotificationService notificationService;
    
    /**
     * Constructor privado (Singleton)
     */
    private DeliverySimulator() {
        this.activeSimulations = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(4, r -> {
            Thread t = new Thread(r, "DeliverySimulator-Thread");
            t.setDaemon(true);
            return t;
        });
        this.simulationTasks = new ConcurrentHashMap<>();
        this.routeService = RouteCalculationService.getInstance();
        this.shipmentService = ShipmentService.getInstance();
        this.notificationService = NotificationService.getInstance();
        
        System.out.println("[DeliverySimulator] Inicializado");
    }
    
    /**
     * Obtiene la instancia única del simulador (Singleton)
     */
    public static synchronized DeliverySimulator getInstance() {
        if (instance == null) {
            instance = new DeliverySimulator();
        }
        return instance;
    }
    
    /**
     * Registra un listener para recibir eventos de simulación
     */
    public void addListener(SimulationListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            System.out.println("[DeliverySimulator] Listener registrado: " + listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Remueve un listener
     */
    public void removeListener(SimulationListener listener) {
        listeners.remove(listener);
        System.out.println("[DeliverySimulator] Listener removido: " + listener.getClass().getSimpleName());
    }
    
    /**
     * Inicia una simulación de entrega para un envío
     * Se activa automáticamente cuando el envío pasa a IN_TRANSIT
     * 
     * @param shipment envío a simular
     * @return simulación creada
     * @throws IllegalStateException si el envío no está en estado válido
     * @throws RoutingException si no se puede calcular la ruta
     */
    public DeliverySimulation startSimulation(Shipment shipment) throws RoutingException {
        return startSimulation(shipment, SimulationConfig.production());
    }
    
    /**
     * Inicia una simulación con configuración personalizada
     */
    public DeliverySimulation startSimulation(Shipment shipment, SimulationConfig config) 
            throws RoutingException {
        
        // Validaciones
        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {
            throw new IllegalStateException(
                "Solo se pueden simular envíos en estado IN_TRANSIT. Estado actual: " + shipment.getStatus()
            );
        }
        
        Deliverer deliverer = shipment.getDeliverer();
        if (deliverer == null) {
            throw new IllegalStateException("El envío debe tener un repartidor asignado");
        }
        
        if (!deliverer.hasRealCoordinates()) {
            throw new IllegalStateException(
                "El repartidor debe tener coordenadas GPS configuradas"
            );
        }
        
        if (shipment.getDestination() == null || 
            !shipment.getDestination().hasGpsCoordinates()) {
            throw new IllegalStateException(
                "El destino debe tener coordenadas GPS configuradas"
            );
        }
        
        // Verificar si ya existe una simulación activa
        if (activeSimulations.containsKey(shipment.getId())) {
            System.out.println("[DeliverySimulator] Ya existe una simulación para este envío");
            return activeSimulations.get(shipment.getId());
        }
        
        System.out.println("[DeliverySimulator] Iniciando simulación para envío: " + shipment.getId());
        
        // 1. Calcular ruta óptima
        Coordinates origin = new Coordinates(
            deliverer.getRealLatitude(), 
            deliverer.getRealLongitude()
        );
        Coordinates destination = new Coordinates(
            shipment.getDestination().getGpsLatitude(),
            shipment.getDestination().getGpsLongitude()
        );
        
        Route route = routeService.calculateOptimalRoute(origin, destination);
        
        if (!route.isValid()) {
            throw new RoutingException("DeliverySimulator", "La ruta calculada no es válida");
        }
        
        System.out.println("[DeliverySimulator] Ruta calculada: " + route.getSummary());
        
        // 2. Crear simulación
        LocalDateTime now = LocalDateTime.now();
        
        DeliverySimulation simulation = DeliverySimulation.builder()
                .id(UUID.randomUUID())
                .shipment(shipment)
                .deliverer(deliverer)
                .route(route)
                .config(config)
                .status(SimulationStatus.INITIALIZING)
                .currentWaypointIndex(0)
                .currentPosition(origin)
                .distanceTraveled(0.0)
                .simulationStartTime(now)
                .virtualStartTime(now)
                .lastUpdateTime(now)
                .estimatedArrival(route.getStatistics().calculateETA(now))
                .progressPercentage(0.0)
                .build();
        
        // 3. Registrar simulación
        activeSimulations.put(shipment.getId(), simulation);
        
        // 4. Programar actualizaciones periódicas
        if (config.isAutoStart()) {
            startSimulationLoop(simulation);
        }
        
        System.out.println("[DeliverySimulator] Simulación iniciada: " + simulation.getStatusSummary());
        
        // Enviar notificación de inicio de entrega
        if (shipment.getUser() != null) {
            System.out.println("[DeliverySimulator] Enviando notificación DELIVERY_STARTED al usuario: " + 
                             shipment.getUser().getId());
            
            notificationService.createNotification(shipment.getUser().getId())
                .type(NotificationType.DELIVERY_STARTED)
                .title("¡Tu entrega está en camino! 🚀")
                .message("El repartidor " + deliverer.getName() + " ha comenzado el trayecto hacia tu dirección. " +
                        "Tiempo estimado: " + (route.getStatistics().getEstimatedMinutes()) + " minutos.")
                .priority(NotificationPriority.HIGH)
                .shipmentId(shipment.getId())
                .send();
        } else {
            System.out.println("[DeliverySimulator] WARN: No se puede enviar notificación - shipment.getUser() es null");
        }
        
        return simulation;
    }
    
    /**
     * Inicia el loop de actualización de una simulación
     */
    private void startSimulationLoop(DeliverySimulation simulation) {
        simulation.setStatus(SimulationStatus.RUNNING);
        
        long intervalMillis = simulation.getConfig().getUpdateIntervalMillis();
        
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
            () -> updateSimulation(simulation),
            0, // Iniciar inmediatamente
            intervalMillis,
            TimeUnit.MILLISECONDS
        );
        
        simulationTasks.put(simulation.getShipment().getId(), future);
        
        System.out.println("[DeliverySimulator] Loop iniciado para " + simulation.getId() + 
                          " (actualización cada " + intervalMillis + "ms)");
    }
    
    /**
     * Actualiza el estado de una simulación (se llama periódicamente)
     */
    private void updateSimulation(DeliverySimulation simulation) {
        try {
            if (simulation.getStatus() != SimulationStatus.RUNNING) {
                return;
            }
            
            // Calcular tiempo virtual transcurrido
            Duration virtualElapsed = simulation.getVirtualElapsedTime();
            
            // Encontrar el waypoint correspondiente al tiempo actual
            Waypoint targetWaypoint = findWaypointAtTime(simulation.getRoute(), virtualElapsed);
            
            if (targetWaypoint == null) {
                // ¡Llegamos al destino!
                completeDelivery(simulation);
                return;
            }
            
            // Interpolar posición entre waypoints
            Coordinates newPosition = interpolatePosition(simulation, targetWaypoint);
            Coordinates previousPosition = simulation.getCurrentPosition();
            
            // Actualizar simulación
            simulation.setCurrentPosition(newPosition);
            simulation.setLastUpdateTime(LocalDateTime.now());
            
            // Calcular distancia recorrida
            double distanceFromOrigin = targetWaypoint.getDistanceFromStart();
            simulation.setDistanceTraveled(distanceFromOrigin);
            simulation.updateProgress();
            
            // Actualizar coordenadas del repartidor en el modelo
            Deliverer deliverer = simulation.getDeliverer();
            deliverer.updateRealPosition(newPosition.getLatitude(), newPosition.getLongitude());
            
            // Verificar si alcanzamos un nuevo waypoint
            if (targetWaypoint.getSequence() > simulation.getCurrentWaypointIndex()) {
                onWaypointReached(simulation, targetWaypoint);
                simulation.setCurrentWaypointIndex(targetWaypoint.getSequence());
            }
            
            // Notificar actualización de posición
            notifyPositionUpdate(simulation, newPosition, previousPosition);
            
        } catch (Exception e) {
            System.err.println("[DeliverySimulator] Error en simulación " + simulation.getId() + ": " + e.getMessage());
            e.printStackTrace();
            handleSimulationError(simulation, e);
        }
    }
    
    /**
     * Encuentra el waypoint que corresponde a un tiempo transcurrido
     * Ahora usa TODOS los puntos de geometría para movimiento suave
     */
    private Waypoint findWaypointAtTime(Route route, Duration elapsedTime) {
        List<Waypoint> waypoints = route.getWaypoints();
        
        if (waypoints.isEmpty()) {
            return null;
        }
        
        // Si ya pasamos el tiempo total, hemos llegado
        Waypoint lastWaypoint = waypoints.get(waypoints.size() - 1);
        if (elapsedTime.compareTo(lastWaypoint.getTimeFromStart()) >= 0) {
            return null; // Llegamos al destino
        }
        
        // Buscar el waypoint actual basado en el tiempo
        // Retornar el waypoint cuyo timeFromStart sea inmediatamente anterior o igual al tiempo actual
        Waypoint result = waypoints.get(0);
        
        for (Waypoint wp : waypoints) {
            if (wp.getTimeFromStart().compareTo(elapsedTime) <= 0) {
                result = wp;
            } else {
                // Ya encontramos el siguiente waypoint, el actual es result
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Interpola la posición entre waypoints de forma suave
     * Usa interpolación lineal precisa entre puntos consecutivos de la geometría
     */
    private Coordinates interpolatePosition(DeliverySimulation simulation, Waypoint targetWaypoint) {
        List<Waypoint> waypoints = simulation.getRoute().getWaypoints();
        Duration elapsedTime = simulation.getVirtualElapsedTime();
        
        if (waypoints.size() < 2) {
            return targetWaypoint.getCoordinates();
        }
        
        // Encontrar el segmento actual (entre qué dos waypoints estamos)
        Waypoint currentSegmentStart = waypoints.get(0);
        Waypoint currentSegmentEnd = waypoints.get(Math.min(1, waypoints.size() - 1));
        
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint start = waypoints.get(i);
            Waypoint end = waypoints.get(i + 1);
            
            // Verificar si estamos en este segmento
            if (elapsedTime.compareTo(start.getTimeFromStart()) >= 0 && 
                elapsedTime.compareTo(end.getTimeFromStart()) <= 0) {
                currentSegmentStart = start;
                currentSegmentEnd = end;
                break;
            }
        }
        
        // Calcular progreso dentro del segmento actual
        Duration segmentDuration = currentSegmentEnd.getTimeFromStart().minus(currentSegmentStart.getTimeFromStart());
        Duration timeInSegment = elapsedTime.minus(currentSegmentStart.getTimeFromStart());
        
        double progress = 0.0;
        if (!segmentDuration.isZero() && segmentDuration.toMillis() > 0) {
            progress = (double) timeInSegment.toMillis() / segmentDuration.toMillis();
            progress = Math.min(1.0, Math.max(0.0, progress)); // Clamp entre 0 y 1
        }
        
        // Interpolación lineal entre las coordenadas
        double startLat = currentSegmentStart.getCoordinates().getLatitude();
        double startLon = currentSegmentStart.getCoordinates().getLongitude();
        double endLat = currentSegmentEnd.getCoordinates().getLatitude();
        double endLon = currentSegmentEnd.getCoordinates().getLongitude();
        
        double interpolatedLat = startLat + (endLat - startLat) * progress;
        double interpolatedLon = startLon + (endLon - startLon) * progress;
        
        return new Coordinates(interpolatedLat, interpolatedLon);
    }
    
    /**
     * Maneja cuando se alcanza un waypoint
     */
    private void onWaypointReached(DeliverySimulation simulation, Waypoint waypoint) {
        waypoint.markAsReached();
        
        if (simulation.getConfig().isEmitWaypointEvents()) {
            int remainingWaypoints = simulation.getRoute().getWaypoints().size() - waypoint.getSequence() - 1;
            
            WaypointReachedEvent event = new WaypointReachedEvent(
                simulation.getId(),
                simulation.getShipment(),
                waypoint,
                remainingWaypoints
            );
            
            notifyWaypointReached(event);
        }
        
        if (simulation.getConfig().isPauseAtWaypoints()) {
            pauseSimulation(simulation.getShipment().getId());
        }
    }
    
    /**
     * Completa una entrega cuando llega al destino
     */
    private void completeDelivery(DeliverySimulation simulation) {
        simulation.setStatus(SimulationStatus.COMPLETED);
        simulation.setProgressPercentage(100.0);
        
        // Detener el loop
        stopSimulationLoop(simulation.getShipment().getId());
        
        System.out.println("[DeliverySimulator] ✓ Entrega completada: " + simulation.getShipment().getId());
        
        // Actualizar estado del envío a DELIVERED
        if (simulation.getConfig().isAutoCompleteDelivery()) {
            Shipment shipment = simulation.getShipment();
            shipment.setStatus(ShipmentStatus.DELIVERED);
            shipment.setDeliveryDate(LocalDateTime.now());
            shipmentService.update(shipment);
            
            System.out.println("[DeliverySimulator] Envío actualizado a DELIVERED");
        }
        
        // Calcular estadísticas de la entrega
        LocalDateTime actualDeliveryTime = LocalDateTime.now();
        Duration actualDuration = Duration.between(simulation.getVirtualStartTime(), actualDeliveryTime);
        double totalDistance = simulation.getRoute().getStatistics().getTotalDistanceMeters();
        
        LocalDateTime eta = simulation.getEstimatedArrival();
        long minutesDiff = eta != null ? Duration.between(eta, actualDeliveryTime).toMinutes() : 0;
        boolean onTime = Math.abs(minutesDiff) <= 5; // Tolerancia de 5 minutos
        
        // Notificar evento de entrega completada
        DeliveryCompletedEvent event = new DeliveryCompletedEvent(
            simulation.getId(),
            simulation.getShipment(),
            simulation.getCurrentPosition(),
            actualDeliveryTime,
            actualDuration,
            totalDistance,
            onTime,
            minutesDiff
        );
        
        notifyDeliveryCompleted(event);
        
        // Remover simulación de activas
        activeSimulations.remove(simulation.getShipment().getId());
    }
    
    /**
     * Detiene el loop de actualización
     */
    private void stopSimulationLoop(UUID shipmentId) {
        ScheduledFuture<?> future = simulationTasks.remove(shipmentId);
        if (future != null) {
            future.cancel(false);
        }
    }
    
    /**
     * Pausa una simulación
     */
    public void pauseSimulation(UUID shipmentId) {
        DeliverySimulation simulation = activeSimulations.get(shipmentId);
        if (simulation != null && simulation.getStatus() == SimulationStatus.RUNNING) {
            simulation.setStatus(SimulationStatus.PAUSED);
            stopSimulationLoop(shipmentId);
            System.out.println("[DeliverySimulator] Simulación pausada: " + shipmentId);
        }
    }
    
    /**
     * Reanuda una simulación pausada
     */
    public void resumeSimulation(UUID shipmentId) {
        DeliverySimulation simulation = activeSimulations.get(shipmentId);
        if (simulation != null && simulation.getStatus() == SimulationStatus.PAUSED) {
            startSimulationLoop(simulation);
            System.out.println("[DeliverySimulator] Simulación reanudada: " + shipmentId);
        }
    }
    
    /**
     * Cancela una simulación
     */
    public void cancelSimulation(UUID shipmentId) {
        DeliverySimulation simulation = activeSimulations.get(shipmentId);
        if (simulation != null && simulation.isActive()) {
            simulation.setStatus(SimulationStatus.CANCELLED);
            stopSimulationLoop(shipmentId);
            activeSimulations.remove(shipmentId);
            System.out.println("[DeliverySimulator] Simulación cancelada: " + shipmentId);
        }
    }
    
    /**
     * Maneja errores en la simulación
     */
    private void handleSimulationError(DeliverySimulation simulation, Exception error) {
        simulation.setStatus(SimulationStatus.FAILED);
        simulation.setErrorMessage(error.getMessage());
        stopSimulationLoop(simulation.getShipment().getId());
        
        // Notificar error a listeners
        for (SimulationListener listener : listeners) {
            try {
                listener.onSimulationError(simulation.getId(), error);
            } catch (Exception e) {
                System.err.println("[DeliverySimulator] Error notificando a listener: " + e.getMessage());
            }
        }
        
        activeSimulations.remove(simulation.getShipment().getId());
    }
    
    // ==================== Notificación a Listeners ====================
    
    private void notifyPositionUpdate(DeliverySimulation simulation, 
                                     Coordinates newPosition, 
                                     Coordinates previousPosition) {
        PositionUpdateEvent event = new PositionUpdateEvent(
            simulation.getId(),
            simulation.getShipment(),
            newPosition,
            previousPosition,
            simulation.getDistanceTraveled(),
            simulation.getRemainingDistance(),
            simulation.getProgressPercentage(),
            simulation.getEstimatedArrival(),
            simulation.getRemainingTime()
        );
        
        for (SimulationListener listener : listeners) {
            try {
                listener.onPositionUpdate(event);
            } catch (Exception e) {
                System.err.println("[DeliverySimulator] Error notificando position update: " + e.getMessage());
            }
        }
        
        // Enviar notificación cuando esté cerca (90% de progreso)
        if (simulation.getProgressPercentage() >= 90.0 && simulation.getProgressPercentage() < 95.0) {
            Shipment shipment = simulation.getShipment();
            if (shipment.getUser() != null) {
                System.out.println("[DeliverySimulator] Enviando notificación DELIVERY_NEAR (progreso: " + 
                                 simulation.getProgressPercentage() + "%)");
                
                notificationService.createNotification(shipment.getUser().getId())
                    .type(NotificationType.DELIVERY_NEAR)
                    .title("¡Tu paquete está cerca!")
                    .message("El repartidor está a menos de 1 km de distancia. Llegará en aproximadamente " + 
                            simulation.getRemainingTime().toMinutes() + " minutos.")
                    .priority(NotificationPriority.HIGH)
                    .shipmentId(shipment.getId())
                    .send();
            }
        }
    }
    
    private void notifyWaypointReached(WaypointReachedEvent event) {
        for (SimulationListener listener : listeners) {
            try {
                listener.onWaypointReached(event);
            } catch (Exception e) {
                System.err.println("[DeliverySimulator] Error notificando waypoint reached: " + e.getMessage());
            }
        }
    }
    
    private void notifyDeliveryCompleted(DeliveryCompletedEvent event) {
        for (SimulationListener listener : listeners) {
            try {
                listener.onDeliveryCompleted(event);
            } catch (Exception e) {
                System.err.println("[DeliverySimulator] Error notificando delivery completed: " + e.getMessage());
            }
        }
        
        // Enviar notificación de entrega completada
        Shipment shipment = event.getShipment();
        if (shipment.getUser() != null) {
            System.out.println("[DeliverySimulator] Enviando notificación DELIVERY_COMPLETED");
            
            notificationService.createNotification(shipment.getUser().getId())
                .type(NotificationType.DELIVERY_COMPLETED)
                .title("¡Entrega completada! 🎉")
                .message("Tu paquete ha sido entregado exitosamente en " + 
                        shipment.getDestination().getStreet() + ". " +
                        "Tiempo total: " + event.getActualDuration().toMinutes() + " minutos.")
                .priority(NotificationPriority.HIGH)
                .shipmentId(shipment.getId())
                .send();
        }
    }
    
    // ==================== Queries ====================
    
    /**
     * Obtiene una simulación activa por ID de envío
     */
    public Optional<DeliverySimulation> getSimulation(UUID shipmentId) {
        return Optional.ofNullable(activeSimulations.get(shipmentId));
    }
    
    /**
     * Obtiene todas las simulaciones activas
     */
    public Collection<DeliverySimulation> getAllActiveSimulations() {
        return new ArrayList<>(activeSimulations.values());
    }
    
    /**
     * Verifica si existe una simulación activa para un envío
     */
    public boolean hasActiveSimulation(UUID shipmentId) {
        return activeSimulations.containsKey(shipmentId);
    }
    
    /**
     * Obtiene estadísticas del simulador
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeSimulations", activeSimulations.size());
        stats.put("registeredListeners", listeners.size());
        stats.put("schedulerActive", !scheduler.isShutdown());
        return stats;
    }
    
    /**
     * Cierra el simulador y libera recursos
     */
    public void shutdown() {
        System.out.println("[DeliverySimulator] Cerrando simulador...");
        
        // Cancelar todas las simulaciones activas
        new ArrayList<>(activeSimulations.keySet()).forEach(this::cancelSimulation);
        
        // Cerrar scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        
        System.out.println("[DeliverySimulator] Simulador cerrado");
    }
}
