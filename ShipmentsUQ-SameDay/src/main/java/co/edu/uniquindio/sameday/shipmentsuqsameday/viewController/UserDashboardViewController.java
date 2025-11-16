package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.UserDashboardController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.notification.Notification;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.notification.NotificationService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.NotificationType;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.NotificationPriority;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador de vista para el dashboard del usuario.
 * Maneja la interacción con la interfaz del panel de control de usuario.
 */
public class UserDashboardViewController implements Initializable {

    @FXML private Label lbl_welcomeUser;
    @FXML private Label lbl_statusMessage;
    @FXML private Label lbl_noContentSelected;
    @FXML private Button btn_logout;
    @FXML private Button btn_profile;
    @FXML private Button btn_addresses;
    @FXML private Button btn_shipments;
    @FXML private Button btn_payments;
    @FXML private Button btn_settings;
    @FXML private Button btn_help;
    @FXML private StackPane stk_contentArea;

    @FXML private StackPane stk_notificationButton;
    @FXML private Button btn_notifications;
    @FXML private Label lbl_notificationBadge;

    private NotificationService notificationService;
    private Popup notificationPopup;
    private VBox notificationListContainer;
    private boolean showingOnlyUnread = false;

    private UserDashboardController controller;

    private UserDTO currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initController();
        initButtonListeners();

        loadCurrentUserData();

        initNotificationSystem();

        updateWelcomeMessage();
        updateLastLoginMessage();
    }

    /**
     * Inicializa el controlador de negocio
     */
    private void initController() {
        controller = new UserDashboardController();

        try {
            currentUser = controller.getCurrentUserData();
        } catch (Exception e) {
            AppUtils.showError("Error", "No se pudo cargar la información del usuario: " + e.getMessage());
        }
    }

    /**
     * Configura los manejadores de eventos para los botones
     */
    private void initButtonListeners() {

        btn_logout.setOnAction(event -> handleLogout());

        btn_profile.setOnAction(event -> loadModule("UserProfile.fxml", "Perfil"));
        btn_addresses.setOnAction(event -> loadModule("ProfileAndAddresses.fxml", "Direcciones"));
        btn_shipments.setOnAction(event -> loadModule("UserShipments.fxml", "Envíos"));
        btn_payments.setOnAction(event -> loadModule("Payments.fxml", "Pagos"));

        btn_settings.setOnAction(event -> handleSettings());
        btn_help.setOnAction(event -> handleHelp());

        if (btn_notifications != null) {
            btn_notifications.setOnAction(event -> handleNotifications());
        }
    }

    /**
     * Carga los datos del usuario actual
     */
    private void loadCurrentUserData() {
        if (currentUser == null) {
            AppUtils.showError("Error de sesión", "No hay una sesión activa.");
            handleLogout();
        }
    }

    /**
     * Actualiza el mensaje de bienvenida con el nombre del usuario
     */
    private void updateWelcomeMessage() {
        if (currentUser != null) {
            lbl_welcomeUser.setText("Bienvenido(a), " + currentUser.getName());
        }
    }

    /**
     * Actualiza el mensaje de último inicio de sesión
     */
    private void updateLastLoginMessage() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = LocalDateTime.now().format(formatter);

        lbl_statusMessage.setText("Último inicio de sesión: " + formattedDate);
    }

    /**
     * Carga un módulo específico en el área de contenido
     *
     * @param fxmlFile Nombre del archivo FXML a cargar
     * @param moduleName Nombre del módulo para mostrar en mensajes
     */
    private void loadModule(String fxmlFile, String moduleName) {
        try {

            stk_contentArea.getChildren().clear();
            lbl_noContentSelected.setVisible(false);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/sameday/shipmentsuqsameday/interfaces/" + fxmlFile));
            Parent moduleView = loader.load();

            if (moduleView instanceof javafx.scene.layout.Region) {
                javafx.scene.layout.Region region = (javafx.scene.layout.Region) moduleView;

                region.setPrefWidth(-1);
                region.setPrefHeight(-1);
                region.setMinWidth(-1);
                region.setMinHeight(-1);
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);

                javafx.scene.layout.StackPane.setAlignment(region, javafx.geometry.Pos.CENTER);
                javafx.scene.layout.StackPane.setMargin(region, new javafx.geometry.Insets(0));
            }

            if (loader.getController() != null) {

                Object viewController = loader.getController();

                if (viewController instanceof UserProfileViewController) {
                    ((UserProfileViewController) viewController).setUserData(currentUser);
                }

            }

            stk_contentArea.getChildren().add(moduleView);

            controller.logModuleAccess(moduleName);

        } catch (IOException e) {
            AppUtils.showError("Error", "No se pudo cargar el módulo " + moduleName + ": " + e.getMessage());
            e.printStackTrace();

            lbl_noContentSelected.setVisible(true);
        }
    }

    /**
     * Maneja el cierre de sesión
     */
    private void handleLogout() {
        if (AppUtils.showConfirmation("Cerrar sesión", "¿Está seguro que desea cerrar sesión?")) {
            try {

                controller.logout();

                boolean loggedOut = AppUtils.logOut();

                if (!loggedOut) {

                    System.out.println("Método logOut falló, intentando con AppUtils.restartApp()");
                    AppUtils.restartApp();
                }
            } catch (Exception e) {
                System.err.println("Error al cerrar sesión: " + e.getMessage());
                e.printStackTrace();
                AppUtils.showError("Error", "Ocurrió un problema al cerrar la sesión: " + e.getMessage());

                try {
                    AppUtils.navigateTo("Login.fxml", btn_logout);
                } catch (Exception ex) {

                    System.err.println("No se pudo recuperar de un error de cierre de sesión: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Handles the settings button action.
     * Opens the settings dialog and refreshes the current scene theme after closing.
     */
    private void handleSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/sameday/shipmentsuqsameday/interfaces/Settings.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/co/edu/uniquindio/sameday/shipmentsuqsameday/css/settings.css").toExternalForm()
            );

            co.edu.uniquindio.sameday.shipmentsuqsameday.util.ThemeManager.getInstance().applyCurrentTheme(scene);

            Stage stage = new Stage();
            stage.setTitle("Configuración");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            stage.setOnHidden(event -> {
                Scene currentScene = btn_settings.getScene();
                if (currentScene != null) {
                    co.edu.uniquindio.sameday.shipmentsuqsameday.util.ThemeManager
                        .getInstance().applyCurrentTheme(currentScene);
                }
            });

            stage.showAndWait();

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to open settings: " + e.getMessage());
            e.printStackTrace();
            AppUtils.showError("Error", "No se pudo abrir la configuración: " + e.getMessage());
        }
    }

    /**
     * Maneja la acción del botón de ayuda
     */
    private void handleHelp() {

        AppUtils.showInfo("Contacto de Ayuda", "Comuníquese con un administrador de MargaDev-Society.");
    }

    /**
     * Inicializa el sistema de notificaciones
     */
    private void initNotificationSystem() {
        if (currentUser == null) {
            System.out.println("[UserDashboard] WARN: No se pudo inicializar notificaciones - currentUser es null");
            return;
        }

        notificationService = NotificationService.getInstance();

        notificationService.addUserListener(currentUser.getId(), this::onNotificationReceived);

        System.out.println("[UserDashboard] Sistema de notificaciones inicializado para usuario: " +
                          currentUser.getName() + " (ID: " + currentUser.getId() + ")");

        updateNotificationBadge();

        notificationService.createNotification(currentUser.getId())
            .type(NotificationType.SYSTEM_MESSAGE)
            .title("Sistema de notificaciones activo ✅")
            .message("Las notificaciones en tiempo real están funcionando correctamente.")
            .priority(NotificationPriority.MEDIUM)
            .send();

        System.out.println("[UserDashboard] Notificación de prueba enviada");
    }

    /**
     * Callback cuando llega una nueva notificación
     */
    private void onNotificationReceived(Notification notification) {
        System.out.println("[UserDashboard] Nueva notificación recibida: " + notification.getTitle());

        updateNotificationBadge();

        if (notificationPopup != null && notificationPopup.isShowing()) {
            refreshNotificationList();
        }
    }

    /**
     * Actualiza el badge de notificaciones no leídas
     */
    private void updateNotificationBadge() {
        if (currentUser == null || lbl_notificationBadge == null) return;

        int unreadCount = notificationService.getUnreadCount(currentUser.getId());

        if (unreadCount > 0) {
            lbl_notificationBadge.setText(String.valueOf(unreadCount));
            lbl_notificationBadge.setVisible(true);
        } else {
            lbl_notificationBadge.setVisible(false);
        }
    }

    /**
     * Maneja la acción del botón de notificaciones
     */
    private void handleNotifications() {
        if (notificationPopup != null && notificationPopup.isShowing()) {
            hideNotificationPanel();
        } else {
            showNotificationPanel();
        }
    }

    /**
     * Muestra el panel de notificaciones
     */
    private void showNotificationPanel() {
        if (currentUser == null) return;

        if (notificationPopup == null) {
            notificationPopup = new Popup();
            notificationPopup.setAutoHide(true);
        }

        VBox popupContent = new VBox(10);
        popupContent.getStyleClass().add("notification-popup");
        popupContent.setPrefWidth(350);
        popupContent.setMaxHeight(500);
        popupContent.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2); -fx-border-color: #ddd; -fx-border-radius: 8;");

        HBox header = new HBox(10);
        header.getStyleClass().add("notification-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));

        Label titleLabel = new Label("Notificaciones");
        titleLabel.getStyleClass().add("notification-title");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("notification-close-btn");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 16px;");
        closeBtn.setOnAction(e -> hideNotificationPanel());

        header.getChildren().addAll(titleLabel, spacer, closeBtn);

        HBox filterBox = new HBox(5);
        filterBox.setPadding(new Insets(0, 10, 10, 10));
        filterBox.setAlignment(Pos.CENTER);

        Button allBtn = new Button("Todas");
        Button unreadBtn = new Button("No leídas");
        Button markAllReadBtn = new Button("Marcar todas");

        allBtn.getStyleClass().add("filter-button");
        unreadBtn.getStyleClass().add("filter-button");
        markAllReadBtn.getStyleClass().add("filter-button");

        allBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
        unreadBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
        markAllReadBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");

        filterBox.getChildren().addAll(allBtn, unreadBtn, markAllReadBtn);

        notificationListContainer = new VBox(5);
        notificationListContainer.setPadding(new Insets(0, 5, 0, 5));

        ScrollPane scrollPane = new ScrollPane(notificationListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        popupContent.getChildren().addAll(header, filterBox, scrollPane);

        setupNotificationPanelHandlers(allBtn, unreadBtn, markAllReadBtn);

        refreshNotificationList();

        notificationPopup.getContent().clear();
        notificationPopup.getContent().add(popupContent);

        javafx.geometry.Bounds bounds = btn_notifications.localToScreen(btn_notifications.getBoundsInLocal());
        notificationPopup.show(btn_notifications, bounds.getMinX() - 300, bounds.getMaxY() + 5);
    }

    /**
     * Oculta el panel de notificaciones
     */
    private void hideNotificationPanel() {
        if (notificationPopup != null) {
            notificationPopup.hide();
        }
    }

    /**
     * Configura los handlers del panel de notificaciones
     */
    private void setupNotificationPanelHandlers(Button allBtn, Button unreadBtn, Button markAllReadBtn) {
        allBtn.setOnAction(e -> {
            showingOnlyUnread = false;
            refreshNotificationList();
            allBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10; -fx-background-color: #2196F3; -fx-text-fill: white;");
            unreadBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
        });

        unreadBtn.setOnAction(e -> {
            showingOnlyUnread = true;
            refreshNotificationList();
            unreadBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10; -fx-background-color: #2196F3; -fx-text-fill: white;");
            allBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
        });

        markAllReadBtn.setOnAction(e -> {
            notificationService.markAllAsRead(currentUser.getId());
            refreshNotificationList();
            updateNotificationBadge();
        });
    }

    /**
     * Refresca la lista de notificaciones
     */
    private void refreshNotificationList() {
        if (notificationListContainer == null || currentUser == null) return;

        notificationListContainer.getChildren().clear();

        List<Notification> notifications = showingOnlyUnread
            ? notificationService.getUnreadNotifications(currentUser.getId())
            : notificationService.getUserNotifications(currentUser.getId());

        if (notifications.isEmpty()) {
            Label emptyLabel = new Label(showingOnlyUnread
                ? "No tienes notificaciones sin leer"
                : "No tienes notificaciones");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 20; -fx-font-size: 12px;");
            notificationListContainer.getChildren().add(emptyLabel);
        } else {
            for (Notification notification : notifications) {
                HBox notificationItem = createNotificationItem(notification);
                notificationListContainer.getChildren().add(notificationItem);
            }
        }
    }

    /**
     * Crea un item de notificación
     */
    private HBox createNotificationItem(Notification notification) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10));
        item.getStyleClass().add("notification-item");

        if (!notification.isRead()) {
            item.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #90caf9; -fx-border-width: 0 0 0 3;");
        } else {
            item.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        }

        Label iconLabel = new Label(notification.getType().getIcon());
        iconLabel.setStyle("-fx-font-size: 20px;");

        VBox content = new VBox(5);

        Label titleLabel = new Label(notification.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(250);
        messageLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        Label timeLabel = new Label(notification.getTimeAgo());
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");

        content.getChildren().addAll(titleLabel, messageLabel, timeLabel);
        HBox.setHgrow(content, Priority.ALWAYS);

        item.getChildren().addAll(iconLabel, content);

        item.setOnMouseClicked(e -> {
            if (!notification.isRead()) {
                notificationService.markAsRead(notification.getId());
                refreshNotificationList();
                updateNotificationBadge();
            }
        });

        item.setOnMouseEntered(e -> {
            if (notification.isRead()) {
                item.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
            } else {
                item.setStyle("-fx-background-color: #bbdefb; -fx-border-color: #90caf9; -fx-border-width: 0 0 0 3; -fx-cursor: hand;");
            }
        });

        item.setOnMouseExited(e -> {
            if (notification.isRead()) {
                item.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
            } else {
                item.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #90caf9; -fx-border-width: 0 0 0 3;");
            }
        });

        return item;
    }
}
