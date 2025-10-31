package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AdminUsersCouriersController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.DataLoadManager;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador de vista para la gestión de usuarios y repartidores
 * Se encarga de la interacción con la interfaz de usuario y delegación
 * de la lógica de negocio al controlador correspondiente
 */
public class AdminUsersCouriersViewController implements Initializable, AdminUsersCouriersController.viewController {

    // Referencias a los elementos de la interfaz
    @FXML private Label lbl_title;
    @FXML private Label lbl_status;
    @FXML private Label lbl_formTitle;
    @FXML private Label lbl_info;
    @FXML private HBox hbox_modeSelector;
    @FXML private Button btn_usersMode;
    @FXML private Button btn_couriersMode;
    @FXML private TableView<Object> tbl_data;
    @FXML private TableColumn<Object, String> col_id;
    @FXML private TableColumn<Object, String> col_name;
    @FXML private TableColumn<Object, String> col_info;
    @FXML private TableColumn<Object, String> col_phone;
    @FXML private TableColumn<Object, String> col_status;
    
    // Referencias a los elementos del formulario básico - USUARIOS
    @FXML private TextField txt_name;
    @FXML private TextField txt_info;
    @FXML private TextField txt_phone;
    @FXML private TextField txt_password;
    @FXML private ChoiceBox<String> chb_role;
    
    // Referencias a los elementos del formulario - REPARTIDORES
    @FXML private TextField txt_courier_name;
    @FXML private TextField txt_courier_phone;
    @FXML private TextField txt_document;
    @FXML private TextField txt_zone;
    @FXML private ChoiceBox<String> chb_status;
    @FXML private TextField txt_rating;
    @FXML private TextField txt_avgRating;
    @FXML private TextField txt_totalDeliveries;
    
    // Botones de acción
    @FXML private Button btn_add;
    @FXML private Button btn_edit;
    @FXML private Button btn_delete;
    @FXML private Button btn_clear;
    
    // Referencias a los TabPanes
    @FXML private TabPane tabPane_users;
    @FXML private TabPane tabPane_couriers;
    
    // Referencias para el mapa
    @FXML private Pane pane_mapContainer;
    @FXML private Label lbl_coordDisplay;
    
    // Referencias para las tablas de envíos del repartidor
    @FXML private TableView<Shipment> tbl_currentShipments;
    @FXML private TableColumn<Shipment, String> col_shipment_id;
    @FXML private TableColumn<Shipment, String> col_shipment_status;
    @FXML private TableColumn<Shipment, String> col_shipment_date;
    
    // Controlador de negocio
    private AdminUsersCouriersController controller;
    
    @FXML private ComboBox<String> itemsPerPage;
    @FXML private Button btn_prev;
    @FXML private Button btn_next;
    @FXML private Label lbl_pageInfo;
    
    // Variables para la gestión de elementos seleccionados
    private User selectedUser;
    private Deliverer selectedDeliverer;
    private double selectedX = 0;
    private double selectedY = 0;
    
    private int currentPage = 0;
    private int pageSize = 20;
    private int totalItems = 0;

    /**
     * Inicializa el controlador de vista
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar controlador
        controller = new AdminUsersCouriersController();
        controller.setViewController(this);
        
        // Configurar valor inicial del ComboBox de elementos por página
        if (itemsPerPage != null) {
            itemsPerPage.setValue("20");
        }
        
        // Configurar botones de modo
        setupModeButtons();
        
        // Configurar la tabla principal
        setupTable();
        
        // Configurar botones de acción
        setupActionButtons();
        
        // Configurar tablas de envíos si existen
        setupShipmentTables();
        
        // Inicializar el controlador de mapas si el contenedor existe
        if (pane_mapContainer != null) {
            controller.initializeGridMap(pane_mapContainer);
        }
        
        // Comenzar en modo usuarios
        controller.setMode("users");
    }
    
    /**
     * Actualiza los datos de la tabla con paginación
     */
    private void refreshTableData() {
        try {
            DataLoadManager.<Object>loadDataAsync(
                tbl_data,
                (List<Object>)controller.getCurrentData(),
                currentPage,
                pageSize
            );
            updatePaginationControls();
        } catch (Exception e) {
            e.printStackTrace();
            showStatusMessage("Error al actualizar los datos: " + e.getMessage(), "error");
        }
    }
    
    /**
     * Configura las tablas de envíos
     */
    private void setupShipmentTables() {
        // Configurar tabla de envíos actuales
        if (tbl_currentShipments != null) {
            col_shipment_id.setCellValueFactory(cellData -> {
                Shipment item = cellData.getValue();
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> {
                        return item != null && item.getId() != null ? 
                            item.getId().toString().substring(0, 8) + "..." : "";
                    }
                );
            });
            
            col_shipment_status.setCellValueFactory(cellData -> {
                Shipment item = cellData.getValue();
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> {
                        return item != null && item.getStatus() != null ? 
                            item.getStatus().name() : "";
                    }
                );
            });
            
            col_shipment_date.setCellValueFactory(cellData -> {
                Shipment item = cellData.getValue();
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> {
                        return item != null && item.getCreationDate() != null ? 
                            item.getCreationDate().toString() : "";
                    }
                );
            });
        }
    }

    /**
     * Configura los botones de selección de modo
     */
    private void setupModeButtons() {
        btn_usersMode.setOnAction(e -> controller.setMode("users"));
        btn_couriersMode.setOnAction(e -> controller.setMode("couriers"));
    }

    /**
     * Configura la tabla y sus columnas
     */
    private void setupTable() {
        // Configurar el ComboBox de elementos por página
        itemsPerPage.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                pageSize = Integer.parseInt(newVal);
                currentPage = 0;
                refreshTableData();
                updatePaginationControls();
            }
        });
        
        // Configurar botones de navegación
        btn_prev.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                refreshTableData();
                updatePaginationControls();
            }
        });
        
        btn_next.setOnAction(e -> {
            if ((currentPage + 1) * pageSize < totalItems) {
                currentPage++;
                refreshTableData();
                updatePaginationControls();
            }
        });
        
        // Configurar las columnas
        col_id.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof User) {
                return Bindings.createStringBinding(
                    () -> ((User) item).getId().toString().substring(0, 8) + "..."
                );
            } else if (item instanceof Deliverer) {
                return Bindings.createStringBinding(
                    () -> ((Deliverer) item).getId().toString().substring(0, 8) + "..."
                );
            }
            return null;
        });
        
        col_name.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof User) {
                return Bindings.createStringBinding(
                    () -> ((User) item).getName()
                );
            } else if (item instanceof Deliverer) {
                return Bindings.createStringBinding(
                    () -> ((Deliverer) item).getName()
                );
            }
            return null;
        });
        
        col_info.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof User) {
                return Bindings.createStringBinding(
                    () -> ((User) item).getEmail()
                );
            } else if (item instanceof Deliverer) {
                return Bindings.createStringBinding(
                    () -> ((Deliverer) item).getDocument()
                );
            }
            return null;
        });
        
        col_phone.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof User) {
                return Bindings.createStringBinding(
                    () -> ((User) item).getPhone()
                );
            } else if (item instanceof Deliverer) {
                return Bindings.createStringBinding(
                    () -> ((Deliverer) item).getPhone()
                );
            }
            return null;
        });
        
        col_status.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof User) {
                return Bindings.createStringBinding(
                    () -> "Activo" // Para usuarios simplificamos a activo
                );
            } else if (item instanceof Deliverer) {
                return Bindings.createStringBinding(
                    () -> ((Deliverer) item).getStatus().name()
                );
            }
            return null;
        });
        
        // Manejar la selección de elementos en la tabla
        tbl_data.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    if (newSelection instanceof User) {
                        selectedUser = (User) newSelection;
                        selectedDeliverer = null;
                        populateUserForm(selectedUser);
                    } else if (newSelection instanceof Deliverer) {
                        selectedDeliverer = (Deliverer) newSelection;
                        selectedUser = null;
                        populateDelivererForm(selectedDeliverer);
                    }
                    updateButtonStates(true);
                }
            }
        );
    }

    /**
     * Configura los botones de acción
     */
    private void setupActionButtons() {
        // Configurar estado inicial de los botones
        btn_edit.setDisable(true);
        btn_delete.setDisable(true);
        
        // Botón de agregar
        btn_add.setOnAction(e -> {
            if (validateForm()) {
                addItem();
                clearForm();
                updateButtonStates(false);
            }
        });
        
        // Botón de editar
        btn_edit.setOnAction(e -> {
            if (validateForm()) {
                editItem();
                clearForm();
                updateButtonStates(false);
            }
        });
        
        // Botón de eliminar
        btn_delete.setOnAction(e -> {
            deleteItem();
            clearForm();
            updateButtonStates(false);
        });
        
        // Botón de limpiar
        btn_clear.setOnAction(e -> {
            clearForm();
            updateButtonStates(false);
        });
    }

    /**
     * Agrega un elemento según el modo actual
     */
    private void addItem() {
        boolean success = false;
        
        if ("users".equals(controller.getMode())) {
            success = controller.addUser(
                txt_name.getText(),
                txt_info.getText(),
                txt_phone.getText()
            );
        } else {
            success = controller.addDeliverer(
                txt_courier_name.getText(),
                txt_document.getText(),
                txt_courier_phone.getText(),
                txt_zone.getText(),
                selectedX,
                selectedY
            );
        }
        
        if (success) {
            showStatusMessage("Elemento agregado correctamente", "success");
            clearForm();
            // Recargar datos
            if ("users".equals(controller.getMode())) {
                loadTableData(controller.getUsersList());
            } else {
                loadTableData(controller.getDeliverersList());
            }
        } else {
            showStatusMessage("Error al agregar elemento", "error");
        }
    }

    /**
     * Edita el elemento seleccionado actualmente
     */
    private void editItem() {
        boolean success = false;
        
        if ("users".equals(controller.getMode()) && selectedUser != null) {
            success = controller.editUser(
                selectedUser,
                txt_name.getText(),
                txt_info.getText(),
                txt_phone.getText()
            );
        } else if (selectedDeliverer != null) {
            DelivererStatus status = DelivererStatus.valueOf(chb_status.getValue());
            success = controller.editDeliverer(
                selectedDeliverer,
                txt_courier_name.getText(),
                txt_document.getText(),
                txt_courier_phone.getText(),
                txt_zone.getText(),
                status,
                selectedX,
                selectedY
            );
        }
        
        if (success) {
            showStatusMessage("Elemento actualizado correctamente", "success");
            // Recargar datos
            if ("users".equals(controller.getMode())) {
                loadTableData(controller.getUsersList());
            } else {
                loadTableData(controller.getDeliverersList());
            }
        } else {
            showStatusMessage("Error al actualizar elemento", "error");
        }
    }

    /**
     * Elimina el elemento seleccionado actualmente
     */
    private void deleteItem() {
        boolean success = false;
        
        if ("users".equals(controller.getMode()) && selectedUser != null) {
            success = controller.deleteUser(selectedUser);
        } else if (selectedDeliverer != null) {
            success = controller.deleteDeliverer(selectedDeliverer);
        }
        
        if (success) {
            showStatusMessage("Elemento eliminado correctamente", "success");
            clearForm();
            // Recargar datos
            if ("users".equals(controller.getMode())) {
                loadTableData(controller.getUsersList());
            } else {
                loadTableData(controller.getDeliverersList());
            }
        } else {
            showStatusMessage("Error al eliminar elemento", "error");
        }
    }

    /**
     * Valida los campos del formulario
     * @return true si los datos son válidos
     */
    private boolean validateForm() {
        if ("users".equals(controller.getMode())) {
            // Validación para usuarios
            if (txt_name == null || txt_name.getText().trim().isEmpty()) {
                showStatusMessage("El nombre no puede estar vacío", "warning");
                return false;
            }
            
            if (txt_info == null || txt_info.getText().trim().isEmpty()) {
                showStatusMessage("El correo no puede estar vacío", "warning");
                return false;
            }
            
            if (txt_phone == null || txt_phone.getText().trim().isEmpty()) {
                showStatusMessage("El teléfono no puede estar vacío", "warning");
                return false;
            }
        } else {
            // Validación para repartidores
            if (txt_courier_name == null || txt_courier_name.getText().trim().isEmpty()) {
                showStatusMessage("El nombre no puede estar vacío", "warning");
                return false;
            }
            
            if (txt_document == null || txt_document.getText().trim().isEmpty()) {
                showStatusMessage("El documento no puede estar vacío", "warning");
                return false;
            }
            
            if (txt_courier_phone == null || txt_courier_phone.getText().trim().isEmpty()) {
                showStatusMessage("El teléfono no puede estar vacío", "warning");
                return false;
            }
            
            if (txt_zone == null || txt_zone.getText().trim().isEmpty()) {
                showStatusMessage("La zona no puede estar vacía", "warning");
                return false;
            }
        }
        
        return true;
    }

    /**
     * Actualiza la interfaz según el modo actual
     * @param mode modo a establecer (users o couriers)
     */
    @Override
    public void updateUIForMode(String mode) {
        // Cambiar título
        lbl_formTitle.setText("users".equals(mode) ? 
            "Formulario de Usuario" : "Formulario de Repartidor");
        
        // Cambiar botones de modo
        btn_usersMode.getStyleClass().remove("active");
        btn_couriersMode.getStyleClass().remove("active");
        
        if ("users".equals(mode)) {
            btn_usersMode.getStyleClass().add("active");
        } else {
            btn_couriersMode.getStyleClass().add("active");
        }
        
        // Mostrar/Ocultar TabPanes según el modo
        boolean isUserMode = "users".equals(mode);
        
        if (tabPane_users != null) {
            tabPane_users.setVisible(isUserMode);
            tabPane_users.setManaged(isUserMode);
        }
        
        if (tabPane_couriers != null) {
            tabPane_couriers.setVisible(!isUserMode);
            tabPane_couriers.setManaged(!isUserMode);
        }
        
        // Limpiar formulario
        clearForm();
    }

    /**
     * Carga datos en la tabla
     * @param data colección de datos a cargar
     */
    @Override
    public void loadTableData(ObservableList<?> data) {
        try {
            // Resetear la paginación y selección
            currentPage = 0;
            tbl_data.getSelectionModel().clearSelection();
            
            // Limpiar el formulario y actualizar estado de botones
            clearForm();
            updateButtonStates(false);
            
            // Usar DataLoadManager para cargar los datos de forma asíncrona
            DataLoadManager.loadDataAsync(tbl_data, data, currentPage, pageSize);
            
            // Actualizar los controles de paginación después de cargar los datos
            updatePaginationControls();
            
            // Mostrar mensaje de éxito
            showStatusMessage("Datos cargados correctamente", "success");
        } catch (Exception e) {
            e.printStackTrace();
            showStatusMessage("Error al cargar los datos: " + e.getMessage(), "error");
        }
    }

    /**
     * Actualiza las coordenadas seleccionadas en el mapa
     * @param x coordenada X
     * @param y coordenada Y
     */
    @Override
    public void updateZoneCoordinates(double x, double y) {
        selectedX = x;
        selectedY = y;
        
        if (lbl_coordDisplay != null) {
            lbl_coordDisplay.setText(String.format("X: %.1f, Y: %.1f", x, y));
        }
    }

    /**
     * Muestra un mensaje de estado en la etiqueta de estado
     * @param message mensaje a mostrar
     * @param messageType tipo de mensaje (success, error, warning, info)
     */
    @Override
    public void showStatusMessage(String message, String messageType) {
        lbl_status.setText(message);
        
        // Quitar clases de estilo anteriores
        lbl_status.getStyleClass().removeAll(
            "success-message", "error-message", "warning-message", "info-message"
        );
        
        // Aplicar el estilo según el tipo de mensaje
        switch (messageType) {
            case "success":
                lbl_status.getStyleClass().add("success-message");
                break;
            case "error":
                lbl_status.getStyleClass().add("error-message");
                break;
            case "warning":
                lbl_status.getStyleClass().add("warning-message");
                break;
            case "info":
                lbl_status.getStyleClass().add("info-message");
                break;
        }
    }

    /**
     * Llena el formulario con los datos del usuario seleccionado
     * @param user usuario a mostrar
     */
    private void populateUserForm(User user) {
        txt_name.setText(user.getName());
        txt_info.setText(user.getEmail());
        txt_phone.setText(user.getPhone());
        if (chb_role != null && user.getRole() != null) {
            chb_role.setValue(user.getRole().name());
        }
    }

    /**
     * Llena el formulario con los datos del repartidor seleccionado
     * @param deliverer repartidor a mostrar
     */
    private void populateDelivererForm(Deliverer deliverer) {
        // Información básica del repartidor
        if (txt_courier_name != null) txt_courier_name.setText(deliverer.getName());
        if (txt_document != null) txt_document.setText(deliverer.getDocument());
        if (txt_courier_phone != null) txt_courier_phone.setText(deliverer.getPhone());
        if (txt_zone != null) txt_zone.setText(deliverer.getZone());
        if (chb_status != null) chb_status.setValue(deliverer.getStatus().name());
        
        // Estadísticas
        if (txt_rating != null) txt_rating.setText(String.format("%.2f", deliverer.getAverageRating()));
        if (txt_avgRating != null) txt_avgRating.setText(String.format("%.2f", deliverer.getAverageRating()));
        if (txt_totalDeliveries != null) txt_totalDeliveries.setText(String.valueOf(deliverer.getTotalDeliveries()));
        
        // Actualizar coordenadas en el mapa
        if (controller != null && controller.getGridMapController() != null) {
            controller.getGridMapController().clearSelection();
            controller.getGridMapController().setSelectedCoordinates(deliverer.getCurrentX(), deliverer.getCurrentY());
            // Actualizar el label de coordenadas si existe
            if (lbl_coordDisplay != null) {
                lbl_coordDisplay.setText(String.format("X: %.0f, Y: %.0f", 
                    deliverer.getCurrentX(), deliverer.getCurrentY()));
            }
        }
        selectedX = deliverer.getX();
        selectedY = deliverer.getY();
        
        if (lbl_coordDisplay != null) {
            lbl_coordDisplay.setText(String.format("X: %.1f, Y: %.1f", selectedX, selectedY));
        }
        
        // Configurar tabla de envíos en progreso
        if (tbl_currentShipments != null && deliverer.getCurrentShipments() != null) {
            tbl_currentShipments.getItems().clear();
            tbl_currentShipments.getItems().addAll(deliverer.getCurrentShipments());
        }
    }

    /**
     * Limpia los campos del formulario
     */
    /**
     * Actualiza los controles de paginación
     */
    private void updatePaginationControls() {
        try {
            DataLoadManager.searchData("", 
                controller.getUsersList(), 
                controller.getDeliverersList(),
                currentPage,
                pageSize)
            .thenAccept(result -> {
                totalItems = (int) result.getTotalItems();
                javafx.application.Platform.runLater(() -> {
                    btn_prev.setDisable(currentPage == 0);
                    btn_next.setDisable((currentPage + 1) * pageSize >= totalItems);
                    lbl_pageInfo.setText(String.format("Página %d de %d", 
                        currentPage + 1, 
                        result.getTotalPages()));
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            showStatusMessage("Error al actualizar la paginación: " + e.getMessage(), "error");
        }
    }

    /**
     * Actualiza el estado de los botones según si hay un elemento seleccionado
     * @param itemSelected true si hay un elemento seleccionado, false si no
     */
    private void updateButtonStates(boolean itemSelected) {
        btn_add.setDisable(itemSelected);
        btn_edit.setDisable(!itemSelected);
        btn_delete.setDisable(!itemSelected);
    }

    private void clearForm() {
        // Limpiar campos de usuarios
        if (txt_name != null) txt_name.setText("");
        if (txt_info != null) txt_info.setText("");
        if (txt_phone != null) txt_phone.setText("");
        if (txt_password != null) txt_password.setText("");
        if (chb_role != null) chb_role.setValue(null);
        
        // Limpiar campos de repartidores
        if (txt_courier_name != null) txt_courier_name.setText("");
        if (txt_courier_phone != null) txt_courier_phone.setText("");
        if (txt_document != null) txt_document.setText("");
        if (txt_zone != null) txt_zone.setText("");
        if (chb_status != null) chb_status.setValue(null);
        if (txt_rating != null) txt_rating.setText("");
        if (txt_avgRating != null) txt_avgRating.setText("");
        if (txt_totalDeliveries != null) txt_totalDeliveries.setText("");
        
        // Limpiar coordenadas
        selectedX = 0;
        selectedY = 0;
        if (lbl_coordDisplay != null) {
            lbl_coordDisplay.setText("X: 0.0, Y: 0.0");
        }
        
        // Limpiar tabla de envíos
        if (tbl_currentShipments != null) {
            tbl_currentShipments.getItems().clear();
        }
        
        // Restablecer selección
        selectedUser = null;
        selectedDeliverer = null;
        tbl_data.getSelectionModel().clearSelection();
    }
}