package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AdminUsersCouriersController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.DataLoadManager;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
    
    // Referencias a los elementos del formulario básico
    @FXML private TextField txt_name;
    @FXML private TextField txt_info;
    @FXML private TextField txt_phone;
    @FXML private TextField txt_password;
    @FXML private ChoiceBox<String> chb_status;
    @FXML private ChoiceBox<String> chb_role;
    @FXML private Button btn_add;
    @FXML private Button btn_edit;
    @FXML private Button btn_delete;
    @FXML private Button btn_clear;
    
    // Referencias a los elementos de pestañas
    @FXML private TabPane tabPane;
    @FXML private Tab tab_basicInfo;
    @FXML private Tab tab_delivererInfo;
    @FXML private Tab tab_coordinates;
    @FXML private Tab tab_shipments;
    
    // Referencias para la información de repartidor
    @FXML private TextField txt_document;
    @FXML private TextField txt_zone;
    @FXML private TextField txt_rating;
    @FXML private TextField txt_totalDeliveries;
    
    // Referencias para el mapa
    @FXML private Pane pane_mapContainer;
    @FXML private Label lbl_coordinates;
    @FXML private Label lbl_coordDisplay;
    
    // Referencias para las tablas de envíos
    @FXML private TableView<Shipment> tbl_currentShipments;
    @FXML private TableColumn<Shipment, String> col_shipment_id;
    @FXML private TableColumn<Shipment, String> col_shipment_status;
    @FXML private TableColumn<Shipment, String> col_shipment_date;
    @FXML private TableView<Shipment> tbl_shipmentHistory;
    @FXML private TableColumn<Shipment, String> col_history_id;
    @FXML private TableColumn<Shipment, String> col_history_date;
    @FXML private TableColumn<Shipment, String> col_history_rating;
    
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
        
        // Configurar el choicebox de estados
        setupStatusChoiceBox();
        
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
        
        // Configurar tabla de historial de envíos
        if (tbl_shipmentHistory != null) {
            col_history_id.setCellValueFactory(cellData -> {
                Shipment item = cellData.getValue();
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> {
                        return item != null && item.getId() != null ? 
                            item.getId().toString().substring(0, 8) + "..." : "";
                    }
                );
            });
            
            col_history_date.setCellValueFactory(cellData -> {
                Shipment item = cellData.getValue();
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> {
                        return item != null && item.getDeliveryDate() != null ? 
                            item.getDeliveryDate().toString() : "";
                    }
                );
            });
            
            col_history_rating.setCellValueFactory(cellData -> {
                Shipment item = cellData.getValue();
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> {
                        // Usamos el campo de costo temporalmente para la calificación
                        // ya que no hay un campo específico para calificación en el modelo Shipment
                        return item != null ? String.format("%.1f", item.getCost()) : "-";
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
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> ((User) item).getId().toString().substring(0, 8) + "..."
                );
            } else if (item instanceof Deliverer) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> ((Deliverer) item).getId().toString().substring(0, 8) + "..."
                );
            }
            return null;
        });
        
        col_name.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof User) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> ((User) item).getName()
                );
            } else if (item instanceof Deliverer) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> ((Deliverer) item).getName()
                );
            }
            return null;
        });
        
        col_info.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof User) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> ((User) item).getEmail()
                );
            } else if (item instanceof Deliverer) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> ((Deliverer) item).getDocument()
                );
            }
            return null;
        });
        
        col_phone.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof User) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> ((User) item).getPhone()
                );
            } else if (item instanceof Deliverer) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> ((Deliverer) item).getPhone()
                );
            }
            return null;
        });
        
        col_status.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof User) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> "Activo" // Para usuarios simplificamos a activo
                );
            } else if (item instanceof Deliverer) {
                return javafx.beans.binding.Bindings.createStringBinding(
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
     * Configura el ChoiceBox de estados
     */
    private void setupStatusChoiceBox() {
        // Inicialmente vacío, se poblará según el modo
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
                txt_name.getText(),
                txt_info.getText(),
                txt_phone.getText(),
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
                txt_name.getText(),
                txt_info.getText(),
                txt_phone.getText(),
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
        // Validación básica
        if (txt_name.getText().trim().isEmpty()) {
            showStatusMessage("El nombre no puede estar vacío", "warning");
            return false;
        }
        
        if (txt_info.getText().trim().isEmpty()) {
            showStatusMessage("El correo/documento no puede estar vacío", "warning");
            return false;
        }
        
        if (txt_phone.getText().trim().isEmpty()) {
            showStatusMessage("El teléfono no puede estar vacío", "warning");
            return false;
        }
        
        // Si estamos en modo repartidores, validar zona
        if (!"users".equals(controller.getMode())) {
            if (txt_zone != null && txt_zone.getText().trim().isEmpty()) {
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
        
        // Cambiar etiqueta de información (correo o documento)
        lbl_info.setText("users".equals(mode) ? "Correo:" : "Documento:");
        
        // Cambiar botones de modo
        btn_usersMode.getStyleClass().remove("active");
        btn_couriersMode.getStyleClass().remove("active");
        
        if ("users".equals(mode)) {
            btn_usersMode.getStyleClass().add("active");
        } else {
            btn_couriersMode.getStyleClass().add("active");
        }
        
        // Configurar visibilidad de las pestañas específicas para repartidor
        boolean isUserMode = "users".equals(mode);
        
        // Mostrar u ocultar pestañas según el modo
        if (tabPane != null) {
            tab_delivererInfo.setDisable(isUserMode);
            tab_coordinates.setDisable(isUserMode);
            tab_shipments.setDisable(isUserMode);
        }
        
        // Poblar estados en el ChoiceBox según el modo
        chb_status.getItems().clear();
        if ("users".equals(mode)) {
            chb_status.getItems().addAll("Activo", "Inactivo");
            chb_status.setValue("Activo");
        } else {
            // Convertir enum DelivererStatus a lista de strings
            chb_status.getItems().addAll(
                Arrays.stream(DelivererStatus.values())
                    .map(Enum::name)
                    .collect(Collectors.toList())
            );
            chb_status.setValue(DelivererStatus.AVAILABLE.name());
        }
        
        // Limpiar formulario
        clearForm();
    }

    /**
     * Carga datos en la tabla
     * @param data colección de datos a cargar
     */
    @Override
    @SuppressWarnings("unchecked")
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
        chb_status.setValue("Activo");
    }

    /**
     * Llena el formulario con los datos del repartidor seleccionado
     * @param deliverer repartidor a mostrar
     */
    private void populateDelivererForm(Deliverer deliverer) {
        // Información básica
        txt_name.setText(deliverer.getName());
        txt_info.setText(deliverer.getDocument());
        txt_phone.setText(deliverer.getPhone());
        chb_status.setValue(deliverer.getStatus().name());
        
        // Información específica de repartidor
        txt_document.setText(deliverer.getDocument());
        txt_zone.setText(deliverer.getZone());
        txt_rating.setText(String.format("%.2f", deliverer.getAverageRating()));
        txt_totalDeliveries.setText(String.valueOf(deliverer.getTotalDeliveries()));
        
        // Actualizar coordenadas
        selectedX = deliverer.getX();
        selectedY = deliverer.getY();
        
        if (lbl_coordDisplay != null) {
            lbl_coordDisplay.setText(String.format("X: %.1f, Y: %.1f", selectedX, selectedY));
        }
        
        // Configurar tablas de envíos
        if (tbl_currentShipments != null && deliverer.getCurrentShipments() != null) {
            tbl_currentShipments.getItems().clear();
            tbl_currentShipments.getItems().addAll(deliverer.getCurrentShipments());
        }
        
        if (tbl_shipmentHistory != null && deliverer.getShipmentHistory() != null) {
            tbl_shipmentHistory.getItems().clear();
            tbl_shipmentHistory.getItems().addAll(deliverer.getShipmentHistory());
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
        // Limpiar información básica
        txt_name.setText("");
        txt_info.setText("");
        txt_phone.setText("");
        txt_password.setText("");
        
        // Limpiar información de repartidor
        txt_document.setText("");
        txt_zone.setText("");
        txt_rating.setText("");
        txt_totalDeliveries.setText("");
        
        // Limpiar coordenadas
        selectedX = 0;
        selectedY = 0;
        if (lbl_coordDisplay != null) {
            lbl_coordDisplay.setText("X: 0.0, Y: 0.0");
        }
        
        // Limpiar tablas
        if (tbl_currentShipments != null) {
            tbl_currentShipments.getItems().clear();
        }
        
        if (tbl_shipmentHistory != null) {
            tbl_shipmentHistory.getItems().clear();
        }
        
        // Restablecer selección
        selectedUser = null;
        selectedDeliverer = null;
        tbl_data.getSelectionModel().clearSelection();
    }
}