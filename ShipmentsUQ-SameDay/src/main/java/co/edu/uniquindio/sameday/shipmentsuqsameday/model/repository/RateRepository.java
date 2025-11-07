package co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Rate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repositorio para gestionar la persistencia de tarifas
 */
public class RateRepository extends BaseRepository<Rate> {

    @Override
    protected UUID getEntityId(Rate rate) {
        return rate.getId();
    }
    
    @Override
    protected void setEntityId(Rate rate, UUID id) {
        rate.setId(id);
    }

    /**
     * Obtiene la tarifa actualmente activa
     * @return la tarifa actual o null si no hay ninguna activa
     */
    public Rate getCurrentRate() {
        return entities.values().stream()
            .filter(Rate::isActive)
            .findFirst()
            .orElse(null);
    }

    /**
     * Establece una nueva tarifa como activa
     * @param newRate nueva tarifa a activar
     */
    public void setCurrentRate(Rate newRate) {
        LocalDateTime now = LocalDateTime.now();
        
        // Desactivar la tarifa actual si existe
        Rate currentRate = getCurrentRate();
        if (currentRate != null) {
            currentRate.setActive(false);
            currentRate.setEffectiveUntil(now);
            update(currentRate);
        }
        
        // Activar la nueva tarifa
        newRate.setActive(true);
        newRate.setEffectiveFrom(now);
        newRate.setEffectiveUntil(null);
        save(newRate);
    }
    
    /**
     * Obtiene el historial de tarifas ordenado por fecha
     * @return lista de tarifas ordenadas por fecha de efectividad
     */
    public List<Rate> getRateHistory() {
        return entities.values().stream()
            .sorted(Comparator.comparing(Rate::getEffectiveFrom))
            .collect(Collectors.toList());
    }
    
    /**
     * Busca la tarifa vigente en una fecha específica
     * @param date fecha a consultar
     * @return tarifa vigente en esa fecha
     */
    public Optional<Rate> getRateAtDate(LocalDateTime date) {
        return entities.values().stream()
            .filter(rate -> 
                rate.getEffectiveFrom().isBefore(date) && 
                (rate.getEffectiveUntil() == null || rate.getEffectiveUntil().isAfter(date)))
            .findFirst();
    }
}