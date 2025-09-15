package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

/**
 * Interfaz para el cálculo de tarifas
 */
public interface IRateCalculator {
    /**
     * Calcula el costo base del envío
     * @param weight peso del paquete en kg
     * @param volume volumen del paquete en m³
     * @param distance distancia del envío en km
     * @return costo base calculado
     */
    double calculateBaseRate(double weight, double volume, double distance);

    /**
     * Calcula los recargos adicionales
     * @param baseRate costo base del envío
     * @param hasInsurance si incluye seguro
     * @param isFragile si es un paquete frágil
     * @param priorityMultiplier multiplicador de prioridad
     * @return costo total con recargos
     */
    double calculateSurcharges(double baseRate, boolean hasInsurance, boolean isFragile, double priorityMultiplier);
}