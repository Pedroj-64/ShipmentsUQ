package co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums;

/**
 * Estados de disponibilidad de un repartidor
 */
public enum DelivererStatus {
    AVAILABLE,        // Listo para recibir asignaciones
    ACTIVE,           // Con envíos asignados pero disponible para más
    BUSY,             // Con el máximo de envíos permitidos
    IN_SERVICE,       // Realizando una entrega específica
    ON_BREAK,         // En periodo de descanso
    OFF_DUTY          // No disponible (fin de turno, día libre, etc.)
}