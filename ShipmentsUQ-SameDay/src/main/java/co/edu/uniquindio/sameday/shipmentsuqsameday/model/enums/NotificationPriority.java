package co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums;

/**
 * Nivel de prioridad de una notificación
 */
public enum NotificationPriority {
    LOW("Baja"),
    MEDIUM("Media"),
    HIGH("Alta"),
    URGENT("Urgente");

    private final String displayName;

    NotificationPriority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
