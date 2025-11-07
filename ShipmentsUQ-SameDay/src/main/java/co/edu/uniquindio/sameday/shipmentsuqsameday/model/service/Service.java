package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interfaz genérica para servicios
 * @param <T> tipo de entidad que maneja el servicio
 * @param <R> tipo de repositorio de la entidad
 */
public interface Service<T, R extends Repository<T>> {
    /**
     * Obtiene la instancia del repositorio
     * @return repositorio de la entidad
     */
    R getRepository();
    
    /**
     * Crea una nueva entidad
     * @param entity entidad a crear
     * @return entidad creada
     */
    default T create(T entity) {
        return getRepository().save(entity);
    }
    
    /**
     * Actualiza una entidad existente
     * @param entity entidad a actualizar
     * @return entidad actualizada
     */
    default T update(T entity) {
        return getRepository().update(entity);
    }
    
    /**
     * Elimina una entidad por su ID
     * @param id ID de la entidad
     */
    default void delete(UUID id) {
        getRepository().deleteById(id);
    }
    
    /**
     * Busca una entidad por su ID
     * @param id ID de la entidad
     * @return entidad encontrada o empty si no existe
     */
    default Optional<T> findById(UUID id) {
        return getRepository().findById(id);
    }
    
    /**
     * Obtiene todas las entidades
     * @return lista de entidades
     */
    default List<T> findAll() {
        return getRepository().findAll();
    }
    
    /**
     * Verifica si existe una entidad con el ID dado
     * @param id ID a verificar
     * @return true si existe la entidad
     */
    default boolean exists(UUID id) {
        return getRepository().existsById(id);
    }
}