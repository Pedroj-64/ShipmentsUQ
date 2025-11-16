package co.edu.uniquindio.sameday.shipmentsuqsameday.model.notification;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.NotificationPriority;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.NotificationType;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Servicio central de notificaciones usando el patrón Singleton y Observer
 * Gestiona todas las notificaciones del sistema y notifica a los listeners suscritos
 * 
 * Thread-safe: utiliza estructuras concurrentes y Platform.runLater para UI updates
 */
public class NotificationService {
    
    private static NotificationService instance;
    
    // Almacén de notificaciones por usuario (thread-safe)
    private final Map<UUID, List<Notification>> notificationsByUser;
    
    // Listeners globales (reciben todas las notificaciones)
    private final List<NotificationListener> globalListeners;
    
    // Listeners por usuario (reciben solo notificaciones de ese usuario)
    private final Map<UUID, List<NotificationListener>> userListeners;
    
    // Configuración
    private int maxNotificationsPerUser = 100;
    
    private NotificationService() {
        this.notificationsByUser = new ConcurrentHashMap<>();
        this.globalListeners = new CopyOnWriteArrayList<>();
        this.userListeners = new ConcurrentHashMap<>();
    }
    
    /**
     * Obtiene la instancia única del servicio
     */
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    /**
     * Crea y envía una notificación
     * 
     * @param notification La notificación a enviar
     */
    public void sendNotification(Notification notification) {
        if (notification == null || notification.getUserId() == null) {
            return;
        }
        
        UUID userId = notification.getUserId();
        
        // Agregar a la lista del usuario
        notificationsByUser.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>())
                .add(notification);
        
        // Limitar cantidad de notificaciones
        List<Notification> userNotifications = notificationsByUser.get(userId);
        if (userNotifications.size() > maxNotificationsPerUser) {
            // Eliminar las más antiguas y leídas
            userNotifications.stream()
                    .filter(Notification::isRead)
                    .sorted(Comparator.comparing(Notification::getCreatedAt))
                    .findFirst()
                    .ifPresent(userNotifications::remove);
        }
        
        // Notificar a listeners en el hilo de JavaFX
        Platform.runLater(() -> {
            // Notificar listeners globales
            globalListeners.forEach(listener -> {
                try {
                    listener.onNotificationReceived(notification);
                } catch (Exception e) {
                    System.err.println("Error en listener global: " + e.getMessage());
                }
            });
            
            // Notificar listeners específicos del usuario
            List<NotificationListener> listeners = userListeners.get(userId);
            if (listeners != null) {
                listeners.forEach(listener -> {
                    try {
                        listener.onNotificationReceived(notification);
                    } catch (Exception e) {
                        System.err.println("Error en listener de usuario: " + e.getMessage());
                    }
                });
            }
        });
    }
    
    /**
     * Builder conveniente para crear y enviar notificaciones
     */
    public NotificationBuilder createNotification(UUID userId) {
        return new NotificationBuilder(userId);
    }
    
    /**
     * Registra un listener global (recibe todas las notificaciones)
     */
    public void addGlobalListener(NotificationListener listener) {
        if (listener != null && !globalListeners.contains(listener)) {
            globalListeners.add(listener);
        }
    }
    
    /**
     * Registra un listener para un usuario específico
     */
    public void addUserListener(UUID userId, NotificationListener listener) {
        if (userId != null && listener != null) {
            userListeners.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>())
                    .add(listener);
        }
    }
    
    /**
     * Elimina un listener global
     */
    public void removeGlobalListener(NotificationListener listener) {
        globalListeners.remove(listener);
    }
    
    /**
     * Elimina un listener de usuario
     */
    public void removeUserListener(UUID userId, NotificationListener listener) {
        List<NotificationListener> listeners = userListeners.get(userId);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Obtiene todas las notificaciones de un usuario
     */
    public List<Notification> getUserNotifications(UUID userId) {
        return new ArrayList<>(notificationsByUser.getOrDefault(userId, Collections.emptyList()));
    }
    
    /**
     * Obtiene notificaciones no leídas de un usuario
     */
    public List<Notification> getUnreadNotifications(UUID userId) {
        return getUserNotifications(userId).stream()
                .filter(n -> !n.isRead())
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene el conteo de notificaciones no leídas
     */
    public int getUnreadCount(UUID userId) {
        return (int) getUserNotifications(userId).stream()
                .filter(n -> !n.isRead())
                .count();
    }
    
    /**
     * Marca una notificación como leída
     */
    public void markAsRead(UUID notificationId) {
        notificationsByUser.values().forEach(notifications -> 
            notifications.stream()
                    .filter(n -> n.getId().equals(notificationId))
                    .findFirst()
                    .ifPresent(Notification::markAsRead)
        );
    }
    
    /**
     * Marca todas las notificaciones de un usuario como leídas
     */
    public void markAllAsRead(UUID userId) {
        List<Notification> notifications = notificationsByUser.get(userId);
        if (notifications != null) {
            notifications.forEach(Notification::markAsRead);
        }
    }
    
    /**
     * Elimina una notificación
     */
    public void deleteNotification(UUID userId, UUID notificationId) {
        List<Notification> notifications = notificationsByUser.get(userId);
        if (notifications != null) {
            notifications.removeIf(n -> n.getId().equals(notificationId));
        }
    }
    
    /**
     * Elimina todas las notificaciones leídas de un usuario
     */
    public void deleteReadNotifications(UUID userId) {
        List<Notification> notifications = notificationsByUser.get(userId);
        if (notifications != null) {
            notifications.removeIf(Notification::isRead);
        }
    }
    
    /**
     * Limpia todas las notificaciones de un usuario
     */
    public void clearUserNotifications(UUID userId) {
        notificationsByUser.remove(userId);
    }
    
    /**
     * Builder interno para facilitar la creación de notificaciones
     */
    public class NotificationBuilder {
        private final UUID userId;
        private NotificationType type = NotificationType.SYSTEM_MESSAGE;
        private String title = "";
        private String message = "";
        private NotificationPriority priority = NotificationPriority.MEDIUM;
        private UUID shipmentId;
        private String metadata;
        
        private NotificationBuilder(UUID userId) {
            this.userId = userId;
        }
        
        public NotificationBuilder type(NotificationType type) {
            this.type = type;
            return this;
        }
        
        public NotificationBuilder title(String title) {
            this.title = title;
            return this;
        }
        
        public NotificationBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public NotificationBuilder priority(NotificationPriority priority) {
            this.priority = priority;
            return this;
        }
        
        public NotificationBuilder shipmentId(UUID shipmentId) {
            this.shipmentId = shipmentId;
            return this;
        }
        
        public NotificationBuilder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }
        
        /**
         * Construye y envía la notificación
         */
        public void send() {
            Notification notification = Notification.builder()
                    .userId(userId)
                    .type(type)
                    .title(title)
                    .message(message)
                    .priority(priority)
                    .shipmentId(shipmentId)
                    .metadata(metadata)
                    .build();
            
            sendNotification(notification);
        }
        
        /**
         * Construye la notificación sin enviarla
         */
        public Notification build() {
            return Notification.builder()
                    .userId(userId)
                    .type(type)
                    .title(title)
                    .message(message)
                    .priority(priority)
                    .shipmentId(shipmentId)
                    .metadata(metadata)
                    .build();
        }
    }
    
    /**
     * Configura el máximo de notificaciones por usuario
     */
    public void setMaxNotificationsPerUser(int max) {
        this.maxNotificationsPerUser = Math.max(10, max);
    }
    
    /**
     * Obtiene estadísticas del servicio
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", notificationsByUser.size());
        stats.put("totalNotifications", notificationsByUser.values().stream()
                .mapToInt(List::size).sum());
        stats.put("totalUnread", notificationsByUser.values().stream()
                .flatMap(List::stream)
                .filter(n -> !n.isRead())
                .count());
        stats.put("globalListeners", globalListeners.size());
        stats.put("userListeners", userListeners.size());
        return stats;
    }
}
