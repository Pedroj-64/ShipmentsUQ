package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Clase que representa una dirección en el sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private UUID id;
    private String alias;
    private String street;
    private String zone;    // Zona o sector de la ciudad
    private String city;
    private String zipCode;
    private String complement;
    private double latitude;
    private double longitude;
    private boolean isDefault;
    
    /**
     * Calcula la distancia entre esta dirección y otra
     * @param other dirección a la que calcular distancia
     * @return distancia en kilómetros
     */
    public double calculateDistance(Address other) {
        // Fórmula de Haversine para calcular distancia entre coordenadas
        final int R = 6371; // Radio de la Tierra en km
        
        double lat1 = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(other.latitude);
        double lon1 = Math.toRadians(this.longitude);
        double lon2 = Math.toRadians(other.longitude);
        
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        
        double a = Math.sin(dlat/2) * Math.sin(dlat/2) + 
                   Math.cos(lat1) * Math.cos(lat2) * 
                   Math.sin(dlon/2) * Math.sin(dlon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return R * c;
    }
}