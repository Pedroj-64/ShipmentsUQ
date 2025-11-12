package co.edu.uniquindio.sameday.shipmentsuqsameday.mapping;

import java.io.Serializable;

/**
 * Representa coordenadas geográficas (latitud y longitud)
 * para ubicaciones en el mapa real
 */
public class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private double latitude;
    private double longitude;
    
    // Coordenadas del centro de Armenia, Quindío, Colombia
    public static final Coordinates ARMENIA_CENTER = new Coordinates(4.533889, -75.681111);
    
    public Coordinates() {
        this.latitude = ARMENIA_CENTER.latitude;
        this.longitude = ARMENIA_CENTER.longitude;
    }
    
    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    /**
     * Calcula la distancia entre dos coordenadas usando la fórmula de Haversine
     * @param other Las otras coordenadas
     * @return Distancia en kilómetros
     */
    public double distanceTo(Coordinates other) {
        final int EARTH_RADIUS_KM = 6371;
        
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLatRad = Math.toRadians(other.latitude - this.latitude);
        double deltaLonRad = Math.toRadians(other.longitude - this.longitude);
        
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Verifica si las coordenadas están dentro del área de Armenia
     * @return true si está dentro del área de servicio
     */
    public boolean isInServiceArea() {
        // Radio de aproximadamente 20 km desde el centro de Armenia
        double distance = distanceTo(ARMENIA_CENTER);
        return distance <= 20.0;
    }
    
    /**
     * Convierte las coordenadas a formato String para JavaScript
     * @return String en formato "[lat, lng]"
     */
    public String toJavaScriptArray() {
        return "[" + latitude + ", " + longitude + "]";
    }
    
    @Override
    public String toString() {
        return String.format("%.6f, %.6f", latitude, longitude);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coordinates that = (Coordinates) obj;
        return Double.compare(that.latitude, latitude) == 0 &&
               Double.compare(that.longitude, longitude) == 0;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(latitude, longitude);
    }
    
    // Getters y Setters
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
