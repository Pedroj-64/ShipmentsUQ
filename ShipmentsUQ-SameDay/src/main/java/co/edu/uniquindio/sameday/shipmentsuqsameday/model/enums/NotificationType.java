package co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums;

/**
 * Tipos de notificaciones que puede recibir un usuario
 */
public enum NotificationType {
    // Notificaciones de envíos
    SHIPMENT_CREATED("📦", "Envío creado", "#4CAF50"),
    SHIPMENT_ASSIGNED("👤", "Repartidor asignado", "#2196F3"),
    SHIPMENT_IN_TRANSIT("🚚", "En tránsito", "#FF9800"),
    SHIPMENT_DELIVERED("✅", "Entregado", "#4CAF50"),
    SHIPMENT_CANCELLED("❌", "Cancelado", "#F44336"),
    SHIPMENT_DELAYED("⏰", "Retrasado", "#FFC107"),
    
    // Notificaciones de simulación
    DELIVERY_STARTED("🚀", "Entrega iniciada", "#2196F3"),
    WAYPOINT_REACHED("📍", "Punto alcanzado", "#9C27B0"),
    DELIVERY_NEAR("📍", "Cerca del destino", "#FF9800"),
    DELIVERY_COMPLETED("🎉", "Entrega completada", "#4CAF50"),
    
    // Notificaciones del sistema
    SYSTEM_MESSAGE("ℹ️", "Mensaje del sistema", "#607D8B"),
    PROMOTION("🎁", "Promoción", "#E91E63"),
    WARNING("⚠️", "Advertencia", "#FFC107"),
    ERROR("❌", "Error", "#F44336"),
    
    // Notificaciones de cuenta
    PROFILE_UPDATED("👤", "Perfil actualizado", "#2196F3"),
    PASSWORD_CHANGED("🔒", "Contraseña cambiada", "#4CAF50"),
    PAYMENT_METHOD_ADDED("💳", "Método de pago agregado", "#4CAF50");

    private final String icon;
    private final String displayName;
    private final String color;

    NotificationType(String icon, String displayName, String color) {
        this.icon = icon;
        this.displayName = displayName;
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }
}
