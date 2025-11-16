package co.edu.uniquindio.sameday.shipmentsuqsameday.model.notification;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.NotificationPriority;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa una notificación en el sistema
 * Utiliza el patrón Builder para facilitar su creación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification implements Serializable, Comparable<Notification> {
    
    private static final long serialVersionUID = 1L;
    
    @Builder.Default
    private UUID id = UUID.randomUUID();
    
    private UUID userId; // Usuario destinatario
    private NotificationType type;
    private String title;
    private String message;
    
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    private boolean read = false;
    
    private LocalDateTime readAt;
    
    // Referencia opcional a un envío (si aplica)
    private UUID shipmentId;
    
    // Datos adicionales opcionales (JSON, etc.)
    private String metadata;
    
    /**
     * Marca la notificación como leída
     */
    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }
    
    /**
     * Marca la notificación como no leída
     */
    public void markAsUnread() {
        this.read = false;
        this.readAt = null;
    }
    
    /**
     * Obtiene el tiempo transcurrido desde la creación
     * @return String formateado (ej: "hace 5 minutos")
     */
    public String getTimeAgo() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(createdAt, now).toMinutes();
        
        if (minutes < 1) return "Ahora mismo";
        if (minutes < 60) return "Hace " + minutes + " minuto" + (minutes > 1 ? "s" : "");
        
        long hours = minutes / 60;
        if (hours < 24) return "Hace " + hours + " hora" + (hours > 1 ? "s" : "");
        
        long days = hours / 24;
        if (days < 7) return "Hace " + days + " día" + (days > 1 ? "s" : "");
        
        long weeks = days / 7;
        return "Hace " + weeks + " semana" + (weeks > 1 ? "s" : "");
    }
    
    /**
     * Verifica si la notificación es reciente (menos de 1 hora)
     */
    public boolean isRecent() {
        long minutes = java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        return minutes < 60;
    }
    
    /**
     * Compara por prioridad y fecha (más recientes primero)
     */
    @Override
    public int compareTo(Notification other) {
        // Primero por prioridad (descendente)
        int priorityCompare = other.priority.compareTo(this.priority);
        if (priorityCompare != 0) return priorityCompare;
        
        // Luego por fecha (más recientes primero)
        return other.createdAt.compareTo(this.createdAt);
    }
}
