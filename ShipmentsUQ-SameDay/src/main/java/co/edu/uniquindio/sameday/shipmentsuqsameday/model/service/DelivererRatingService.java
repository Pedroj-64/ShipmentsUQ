package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import java.util.List;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;

/**
 * Servicio para gestionar las calificaciones de repartidores
 */
public class DelivererRatingService {
    private final DelivererService delivererService;
    
    private DelivererRatingService() {
        this.delivererService = DelivererService.getInstance();
    }
    
    private static class SingletonHolder {
        private static final DelivererRatingService INSTANCE = new DelivererRatingService();
    }
    
    public static DelivererRatingService getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    /**
     * Registra una nueva calificación para un repartidor
     * @param deliverer repartidor a calificar
     * @param rating calificación (1-5)
     * @return repartidor actualizado
     */
    public Deliverer addRating(Deliverer deliverer, double rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5");
        }
        
        double totalRating = (deliverer.getAverageRating() * deliverer.getTotalDeliveries()) + rating;
        deliverer.setTotalDeliveries(deliverer.getTotalDeliveries() + 1);
        deliverer.setAverageRating(totalRating / deliverer.getTotalDeliveries());
        
        return delivererService.getRepository().update(deliverer);
    }
    
    /**
     * Obtiene repartidores con una calificación mínima
     * @param minRating calificación mínima
     * @return lista de repartidores que cumplen el criterio
     */
    public List<Deliverer> getDeliverersWithMinimumRating(double minRating) {
        return delivererService.getRepository().findByMinimumRating(minRating);
    }
}