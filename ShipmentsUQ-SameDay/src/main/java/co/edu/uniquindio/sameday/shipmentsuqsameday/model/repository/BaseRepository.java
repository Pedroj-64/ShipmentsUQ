package co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación base abstracta del repositorio usando un Map en memoria
 * @param <T> tipo de entidad que maneja el repositorio
 */
public abstract class BaseRepository<T> implements Repository<T> {
    protected final Map<UUID, T> entities = new ConcurrentHashMap<>();
    
    @Override
    public T save(T entity) {
        UUID id = getEntityId(entity);
        if (id == null) {
            id = UUID.randomUUID();
            setEntityId(entity, id);
        }
        entities.put(id, entity);
        return entity;
    }
    
    @Override
    public Optional<T> findById(UUID id) {
        return Optional.ofNullable(entities.get(id));
    }
    
    @Override
    public List<T> findAll() {
        return new ArrayList<>(entities.values());
    }
    
    @Override
    public T update(T entity) {
        UUID id = getEntityId(entity);
        if (id == null || !entities.containsKey(id)) {
            throw new IllegalArgumentException("Entity not found");
        }
        entities.put(id, entity);
        return entity;
    }
    
    @Override
    public void deleteById(UUID id) {
        entities.remove(id);
    }
    
    @Override
    public void delete(T entity) {
        UUID id = getEntityId(entity);
        if (id != null) {
            deleteById(id);
        }
    }
    
    @Override
    public boolean existsById(UUID id) {
        return entities.containsKey(id);
    }
    
    @Override
    public long count() {
        return entities.size();
    }
    
    /**
     * Obtiene el ID de una entidad
     * @param entity entidad
     * @return ID de la entidad
     */
    protected abstract UUID getEntityId(T entity);
    
    /**
     * Establece el ID de una entidad
     * @param entity entidad
     * @param id ID a establecer
     */
    protected abstract void setEntityId(T entity, UUID id);
}