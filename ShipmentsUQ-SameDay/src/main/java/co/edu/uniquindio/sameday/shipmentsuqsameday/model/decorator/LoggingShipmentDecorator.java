package co.edu.uniquindio.sameday.shipmentsuqsameday.model.decorator;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.ShipmentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Decorador que añade funcionalidad de registro (logging) al servicio de envíos
 * Implementa el patrón Decorator para extender la funcionalidad sin modificar la clase original
 */
public class LoggingShipmentDecorator extends ServiceDecorator<Shipment, ShipmentRepository> {
    
    /**
     * Constructor del decorador
     * @param decoratedService servicio a decorar
     */
    public LoggingShipmentDecorator(Service<Shipment, ShipmentRepository> decoratedService) {
        super(decoratedService);
    }
    
    /**
     * Crea un nuevo envío y registra la operación
     * @param entity envío a crear
     * @return envío creado
     */
    @Override
    public Shipment create(Shipment entity) {
        logOperation("CREATE", "Iniciando creación de nuevo envío");
        try {
            Shipment result = decoratedService.create(entity);
            logOperation("CREATE", "Envío creado exitosamente con ID: " + result.getId());
            return result;
        } catch (Exception e) {
            logOperation("CREATE", "Error al crear envío: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Actualiza un envío y registra la operación
     * @param entity envío a actualizar
     * @return envío actualizado
     */
    @Override
    public Shipment update(Shipment entity) {
        logOperation("UPDATE", "Iniciando actualización de envío con ID: " + entity.getId());
        try {
            Shipment result = decoratedService.update(entity);
            logOperation("UPDATE", "Envío actualizado exitosamente, estado: " + result.getStatus());
            return result;
        } catch (Exception e) {
            logOperation("UPDATE", "Error al actualizar envío: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Elimina un envío y registra la operación
     * @param id identificador del envío a eliminar
     */
    @Override
    public void delete(UUID id) {
        logOperation("DELETE", "Iniciando eliminación de envío con ID: " + id);
        try {
            decoratedService.delete(id);
            logOperation("DELETE", "Envío eliminado exitosamente");
        } catch (Exception e) {
            logOperation("DELETE", "Error al eliminar envío: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Busca un envío por su identificador y registra la operación
     * @param id identificador del envío
     * @return envío encontrado o vacío si no existe
     */
    @Override
    public Optional<Shipment> findById(UUID id) {
        logOperation("FIND", "Buscando envío con ID: " + id);
        Optional<Shipment> result = decoratedService.findById(id);
        if (result.isPresent()) {
            logOperation("FIND", "Envío encontrado");
        } else {
            logOperation("FIND", "Envío no encontrado");
        }
        return result;
    }
    
    /**
     * Obtiene todos los envíos y registra la operación
     * @return lista de envíos
     */
    @Override
    public List<Shipment> findAll() {
        logOperation("FIND_ALL", "Obteniendo todos los envíos");
        List<Shipment> results = decoratedService.findAll();
        logOperation("FIND_ALL", "Se encontraron " + results.size() + " envíos");
        return results;
    }
    
    /**
     * Registra una operación en el log
     * @param operation tipo de operación
     * @param message mensaje descriptivo
     */
    private void logOperation(String operation, String message) {
        System.out.println("[" + LocalDateTime.now() + "] LOG - " + operation + ": " + message);
        // En una implementación real, esto podría escribirse en un archivo de log
    }
}