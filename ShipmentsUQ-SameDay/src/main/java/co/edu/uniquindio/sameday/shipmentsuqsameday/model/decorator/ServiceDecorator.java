package co.edu.uniquindio.sameday.shipmentsuqsameday.model.decorator;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.Repository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.Service;

/**
 * Clase abstracta base para los decoradores de servicios
 * Implementa el patrón Decorator para añadir funcionalidades adicionales
 * a los servicios sin modificar su estructura base
 * @param <T> tipo de entidad que maneja el servicio
 * @param <R> tipo de repositorio de la entidad
 */
public abstract class ServiceDecorator<T, R extends Repository<T>> implements Service<T, R> {
    protected final Service<T, R> decoratedService;
    
    /**
     * Constructor del decorador
     * @param decoratedService servicio a decorar
     */
    public ServiceDecorator(Service<T, R> decoratedService) {
        this.decoratedService = decoratedService;
    }
    
    /**
     * Obtiene la instancia del repositorio del servicio decorado
     * @return repositorio de la entidad
     */
    @Override
    public R getRepository() {
        return decoratedService.getRepository();
    }
}