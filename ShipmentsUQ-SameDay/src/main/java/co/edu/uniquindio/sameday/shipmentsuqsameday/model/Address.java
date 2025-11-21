package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IGridCoordinate;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.strategy.GridCoordinateStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Clase que representa una dirección en el sistema
 * Utiliza coordenadas cartesianas (X, Y) en lugar de coordenadas geográficas
 * Ahora también soporta coordenadas GPS reales (opcionales)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address implements Serializable, IGridCoordinate {

    //Como dato esto cuenta como Adapter por que se adapta una direccion a una coordenada en un plano cartesiano
    // y como recomendacion la cancion de barberos es muy buena :D
    private static final long serialVersionUID = 2L; // Incrementado para migración
    private UUID id;
    private String alias;
    private String street;
    private String zone;
    private String city;
    private String zipCode;
    private String complement;
    private double coordX;
    private double coordY;
    private boolean isDefault;
    
    // Nuevos campos para coordenadas GPS reales (opcionales)
    @Builder.Default
    private Double gpsLatitude = null;
    
    @Builder.Default
    private Double gpsLongitude = null;

    /**
     * Implementación de IGridCoordinate para obtener la coordenada X
     * 
     * @return coordenada X de la dirección
     */
    @Override
    public double getX() {
        return coordX;
    }

    /**
     * Implementación de IGridCoordinate para obtener la coordenada Y
     * 
     * @return coordenada Y de la dirección
     */
    @Override
    public double getY() {
        return coordY;
    }

    /**
     * Obtiene la dirección completa formateada
     * 
     * @return String con la dirección completa
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();

        if (street != null && !street.isEmpty()) {
            sb.append(street);
        }

        if (complement != null && !complement.isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(complement);
        }

        if (zone != null && !zone.isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(zone);
        }

        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(city);
        }

        if (zipCode != null && !zipCode.isEmpty()) {
            if (sb.length() > 0)
                sb.append(" - ");
            sb.append(zipCode);
        }

        return sb.length() > 0 ? sb.toString() : "Dirección no especificada";
    }
    
    /**
     * Verifica si esta dirección tiene coordenadas GPS reales
     * @return true si tiene GPS, false si solo tiene Grid
     */
    public boolean hasGpsCoordinates() {
        return gpsLatitude != null && gpsLongitude != null;
    }
    
    /**
     * Establece las coordenadas GPS
     * @param latitude latitud GPS
     * @param longitude longitud GPS
     */
    public void setGpsCoordinates(double latitude, double longitude) {
        this.gpsLatitude = latitude;
        this.gpsLongitude = longitude;
    }
    
    /**
     * Sincroniza las coordenadas: convierte entre Grid y GPS según cuál esté disponible
     */
    public void syncCoordinates() {
        if (hasGpsCoordinates()) {
            // GPS existe → Convertir a Grid
            double[] gridCoords = GridCoordinateStrategy.convertRealToGrid(gpsLatitude, gpsLongitude);
            this.coordX = gridCoords[0];
            this.coordY = gridCoords[1];
        } else if (coordX != 0.0 || coordY != 0.0) {
            // Grid existe → Convertir a GPS
            double[] gpsCoords = GridCoordinateStrategy.convertGridToReal(coordX, coordY);
            this.gpsLatitude = gpsCoords[0];
            this.gpsLongitude = gpsCoords[1];
        }
    }
}