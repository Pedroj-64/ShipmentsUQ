package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IGridCoordinate;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.DelivererRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para la gestión de repartidores y sus asignaciones
 */
public class DelivererService implements Service<Deliverer, DelivererRepository> {
    // Constantes
    private static final int MAX_CONCURRENT_SHIPMENTS = 3;
    @SuppressWarnings("unused")
    private static final double MIN_VALID_RATING = 1.0;
    @SuppressWarnings("unused")
    private static final double MAX_VALID_RATING = 5.0;
    
    private final DelivererRepository repository;
    
    // Constructor privado para Singleton con inyección de dependencias
    private DelivererService(DelivererRepository repository) {
        this.repository = repository;
    }
    
    // Holder estático para instancia única con inyección de dependencias
    private static class SingletonHolder {
        private static DelivererService INSTANCE;
        
        static {
            INSTANCE = new DelivererService(new DelivererRepository());
        }
    }
    
    /**
     * Obtiene la instancia única del servicio
     * @return instancia del servicio
     */
    public static DelivererService getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    /**
     * Obtiene la instancia única del servicio con un repositorio específico
     * @param repository repositorio a utilizar
     * @return instancia del servicio
     */
    public static DelivererService getInstance(DelivererRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("El repositorio no puede ser nulo");
        }
        SingletonHolder.INSTANCE = new DelivererService(repository);
        return SingletonHolder.INSTANCE;
    }
    
    @Override
    public DelivererRepository getRepository() {
        return repository;
    }
    
    /**
     * Valida los datos de un repartidor
     * @param deliverer repartidor a validar
     * @throws IllegalArgumentException si los datos son inválidos
     */
    private void validateDeliverer(Deliverer deliverer) {
        if (deliverer == null) {
            throw new IllegalArgumentException("El repartidor no puede ser nulo");
        }
        if (deliverer.getDocument() == null || deliverer.getDocument().trim().isEmpty()) {
            throw new IllegalArgumentException("El documento es requerido");
        }
        if (deliverer.getName() == null || deliverer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es requerido");
        }
        if (deliverer.getPhone() == null || deliverer.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono es requerido");
        }
    }

    /**
     * Registra un nuevo repartidor
     * @param deliverer repartidor a registrar
     * @return repartidor registrado
     * @throws IllegalArgumentException si los datos son inválidos o el documento ya está registrado
     */
    public Deliverer registerDeliverer(Deliverer deliverer) {
        validateDeliverer(deliverer);
        
        if (repository.findByDocument(deliverer.getDocument()).isPresent()) {
            throw new IllegalArgumentException("El documento ya está registrado");
        }

        // Establecer valores por defecto
        deliverer.setStatus(DelivererStatus.AVAILABLE);
        if (deliverer.getId() == null) {
            deliverer.setId(UUID.randomUUID());
        }
        
        return repository.save(deliverer);
    }
    
    /**
     * Busca repartidores disponibles en una zona específica
     * @param zone zona a buscar
     * @return lista de repartidores disponibles ordenados por calificación
     */
    public List<Deliverer> findAvailableDeliverersInZone(String zone) {
        return repository.findAvailableByZone(zone).stream()
                .sorted(Comparator.comparingDouble(Deliverer::getAverageRating).reversed())
                .toList();
    }
    
    /**
     * Busca todos los repartidores disponibles en el sistema
     * @return lista de repartidores disponibles ordenados por calificación
     */
    public List<Deliverer> findAvailableDeliverers() {
        return repository.findAll().stream()
                .filter(deliverer -> 
                    deliverer.getStatus() == DelivererStatus.AVAILABLE || 
                    deliverer.getStatus() == DelivererStatus.ACTIVE)
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
     * @throws IllegalArgumentException si el repartidor o el estado son nulos
     */
    public Deliverer updateDelivererStatus(Deliverer deliverer, DelivererStatus status) {
        if (deliverer == null) {
            throw new IllegalArgumentException("El repartidor no puede ser nulo");
        }
        if (status == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }

        deliverer.setStatus(status);
        return repository.update(deliverer);
    }
    
    /**
     * Asigna un nuevo envío al repartidor
     * @param deliverer repartidor al que asignar el envío
     * @param shipment envío a asignar
     * @return true si se pudo asignar el envío
     * @throws IllegalArgumentException si el repartidor o el envío son nulos
     */
    public boolean assignShipment(Deliverer deliverer, Shipment shipment) {
        if (deliverer == null) {
            throw new IllegalArgumentException("El repartidor no puede ser nulo");
        }
        if (shipment == null) {
            throw new IllegalArgumentException("El envío no puede ser nulo");
        }
        
        // Verificar si puede aceptar el envío
        if (!canAcceptShipment(deliverer)) {
            return false;
        }
        
        // Asignar el envío y actualizar estado
        synchronized(deliverer) {
            deliverer.getCurrentShipments().add(shipment);
            updateDelivererStatusBasedOnWorkload(deliverer);
            repository.update(deliverer);
        }
        
        return true;
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
     * Obtiene la lista de repartidores disponibles para asignación
     * Incluye repartidores con estado AVAILABLE y ACTIVE (que pueden tomar más envíos)
     * @return lista de repartidores disponibles ordenados por calificación
     */
    public List<Deliverer> getAvailableDeliverers() {
        System.out.println("\n=== DEBUG: getAvailableDeliverers() ===");
        
        List<Deliverer> allDeliverers = repository.findAll();
        System.out.println("Total repartidores en repositorio: " + allDeliverers.size());
        
        List<Deliverer> available = allDeliverers.stream()
            .filter(d -> {
                boolean isAvailable = d.getStatus() == DelivererStatus.AVAILABLE;
                boolean isActive = d.getStatus() == DelivererStatus.ACTIVE && 
                                   d.getCurrentShipments().size() < MAX_CONCURRENT_SHIPMENTS;
                
                System.out.println("  - " + d.getName() + 
                                   " | Estado: " + d.getStatus() + 
                                   " | Envíos: " + d.getCurrentShipments().size() + 
                                   " | Disponible para asignar: " + (isAvailable || isActive));
                
                return isAvailable || isActive;
            })
            .sorted(Comparator.comparingDouble(Deliverer::getAverageRating).reversed())
            .toList();
        
        System.out.println("Total repartidores disponibles para asignación: " + available.size());
        return available;
    }
    
    /**
     * Actualiza la posición de un repartidor en el mapa
     * @param delivererId ID del repartidor
     * @param x coordenada X
     * @param y coordenada Y
     * @return repartidor actualizado
     * @throws IllegalArgumentException si el repartidor no existe
     */
    public Deliverer updateDelivererPosition(UUID delivererId, double x, double y) {
        Deliverer deliverer = repository.findById(delivererId)
                .orElseThrow(() -> new IllegalArgumentException("Repartidor no encontrado"));
        
        deliverer.updatePosition(x, y);
        return repository.update(deliverer);
    }
    
    /**
     * Encuentra el repartidor disponible más cercano a unas coordenadas
     * @param x coordenada X de origen
     * @param y coordenada Y de origen
     * @param zone zona opcional para filtrar (null para considerar todas las zonas)
     * @return el repartidor más cercano o null si no hay disponibles
     */
    public Deliverer findNearestAvailableDeliverer(double x, double y, String zone) {
        // Validar coordenadas
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Las coordenadas no pueden ser negativas");
        }
        
        // Obtener repartidores disponibles según la zona
        List<Deliverer> availableDeliverers = (zone != null && !zone.isEmpty()) 
            ? findAvailableDeliverersInZone(zone)
            : getAvailableDeliverers();
            
        if (availableDeliverers.isEmpty()) {
            return null;
        }
        
        // Crear punto de origen
        IGridCoordinate originPoint = new IGridCoordinate() {
            @Override public double getX() { return x; }
            @Override public double getY() { return y; }
        };
        
        // Encontrar el repartidor más cercano usando Stream API
        return availableDeliverers.stream()
            .min(Comparator.comparingDouble(deliverer -> deliverer.distanceTo(originPoint)))
            .orElse(null);
    }
    
    /**
     * Completa un envío asignado a un repartidor
     * @param deliverer repartidor que completó el envío
     * @param shipment envío completado
     * @throws IllegalArgumentException si el repartidor o el envío son nulos
     * o si el envío no está asignado al repartidor
     */
    public void completeShipment(Deliverer deliverer, Shipment shipment) {
        if (deliverer == null) {
            throw new IllegalArgumentException("El repartidor no puede ser nulo");
        }
        if (shipment == null) {
            throw new IllegalArgumentException("El envío no puede ser nulo");
        }
        
        synchronized(deliverer) {
            // Verificar que el envío esté asignado al repartidor
            if (!deliverer.getCurrentShipments().contains(shipment)) {
                throw new IllegalArgumentException("El envío no está asignado a este repartidor");
            }
            
            // Mover el envío al historial
            deliverer.getCurrentShipments().remove(shipment);
            deliverer.getShipmentHistory().add(shipment);
            
            // Actualizar estado del repartidor
            updateDelivererStatusBasedOnWorkload(deliverer);
            repository.update(deliverer);
        }
    }
}