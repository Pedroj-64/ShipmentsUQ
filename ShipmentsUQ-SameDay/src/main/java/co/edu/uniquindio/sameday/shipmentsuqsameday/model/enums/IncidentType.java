package co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums;

/**
 * Tipos de incidencias que pueden ocurrir durante un envío
 */
public enum IncidentType {
    WRONG_ADDRESS,         // La dirección proporcionada es incorrecta o no existe
    RECIPIENT_ABSENT,      // No hay nadie para recibir el envío
    PACKAGE_DAMAGED,       // El paquete sufrió daños durante el transporte
    INACCESSIBLE_ZONE,    // No se puede acceder a la zona de entrega
    FORCE_MAJEURE,        // Eventos externos (clima, manifestaciones, etc.)
    THEFT,                // Robo o pérdida del paquete
    DELIVERER_UNAVAILABLE, // El repartidor no está disponible
    OTHER                 // Otros tipos de incidencias no categorizadas
}