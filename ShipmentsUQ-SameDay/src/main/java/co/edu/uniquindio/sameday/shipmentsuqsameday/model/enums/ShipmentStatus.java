package co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums;

/**
 * Estados posibles de un envío en el sistema
 */
public enum ShipmentStatus {
    PENDING,                // Envío creado pero no asignado
    ASSIGNED,               // Asignado a un repartidor
    IN_TRANSIT,            // El repartidor está en camino
    DELIVERED,             // Entrega completada exitosamente
    CANCELLED,             // Envío cancelado por el usuario
    INCIDENT,              // Ocurrió algún problema durante la entrega
    PENDING_REASSIGNMENT   // En espera de reasignación a otro repartidor
}