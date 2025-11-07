package co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interfaz genérica para operaciones CRUD de repositorio
 * @param <T> tipo de entidad que maneja el repositorio
 */
public interface Repository<T> {
    /**
     * Guarda una entidad
     * @param entity entidad a guardar
     * @return entidad guardada
     */
    T save(T entity);
    
    /**
     * Busca una entidad por su ID
     * @param id ID de la entidad
     * @return entidad encontrada o empty si no existe
     */
    Optional<T> findById(UUID id);
    
    /**
     * Obtiene todas las entidades
     * @return lista de entidades
     */
    List<T> findAll();
    
    /**
     * Actualiza una entidad existente
     * @param entity entidad a actualizar
     * @return entidad actualizada
     */
    T update(T entity);
    
    /**
     * Elimina una entidad por su ID
     * @param id ID de la entidad a eliminar
     */
    void deleteById(UUID id);
    
    /**
     * Elimina una entidad
     * @param entity entidad a eliminar
     */
    void delete(T entity);
    
    /**
     * Verifica si existe una entidad con el ID dado
     * @param id ID a verificar
     * @return true si existe la entidad
     */
    boolean existsById(UUID id);
    
    /**
     * Cuenta el número total de entidades
     * @return número de entidades
     */
    long count();
}