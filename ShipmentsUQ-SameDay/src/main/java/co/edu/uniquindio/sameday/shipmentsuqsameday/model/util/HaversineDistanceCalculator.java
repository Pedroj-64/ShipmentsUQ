package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IDistanceCalculator;

/**
 * Implementación del cálculo de distancia usando la fórmula de Haversine
 */
public class HaversineDistanceCalculator implements IDistanceCalculator {
    private static final int EARTH_RADIUS_KM = 6371;

    @Override
    public double calculateDistance(Address origin, Address destination) {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Las direcciones no pueden ser null");
        }
        
        double lat1 = Math.toRadians(origin.getLatitude());
        double lat2 = Math.toRadians(destination.getLatitude());
        double lon1 = Math.toRadians(origin.getLongitude());
        double lon2 = Math.toRadians(destination.getLongitude());
        
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        
        double a = Math.sin(dlat/2) * Math.sin(dlat/2) + 
                   Math.cos(lat1) * Math.cos(lat2) * 
                   Math.sin(dlon/2) * Math.sin(dlon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return EARTH_RADIUS_KM * c;
    }
}