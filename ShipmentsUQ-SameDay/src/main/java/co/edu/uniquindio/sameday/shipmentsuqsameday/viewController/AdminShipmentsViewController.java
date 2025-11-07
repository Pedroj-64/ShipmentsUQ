package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AdminShipmentsController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador de vista para la gestión de envíos por parte del administrador
 * Se encarga de la interacción con la interfaz de usuario y delegación
 * de la lógica de negocio al controlador correspondiente
 */
public class AdminShipmentsViewController implements Initializable, AdminShipmentsController.ViewController {

    // Referencias a los elementos del encabezado
    @FXML private Label lbl_title;
    @FXML private Label lbl_status;

    // Referencias a los elementos de filtrado
    @FXML private Label lbl_stateFilter;
    @FXML private ChoiceBox<String> chb_stateFilter;
    @FXML private Label lbl_dateFilter;
    @FXML private DatePicker dtp_dateFilter;
    @FXML private Button btn_filter;
    @FXML private Button btn_clearFilter;

    // Referencias a la tabla de envíos
    @FXML private TableView<Shipment> tbl_shipments;
    @FXML private TableColumn<Shipment, String> col_id;
    @FXML private TableColumn<Shipment, String> col_user;
    @FXML private TableColumn<Shipment, String> col_courier;
    @FXML private TableColumn<Shipment, String> col_origin;
    @FXML private TableColumn<Shipment, String> col_destination;
    @FXML private TableColumn<Shipment, String> col_state;
    @FXML private TableColumn<Shipment, String> col_date;

    // Referencias a los botones de acciones
    @FXML private Button btn_assignCourier;
    @FXML private Button btn_updateStatus;
    @FXML private Button btn_incident;
    @FXML private Button btn_deleteShipment;

    // Referencias a los campos de detalles
    @FXML private Label lbl_detailTitle;
    @FXML private Label lbl_shipmentIdValue;
    @FXML private Label lbl_userValue;
    @FXML private Label lbl_courierValue;
    @FXML private Label lbl_originValue;
    @FXML private Label lbl_destinationValue;
    @FXML private Label lbl_stateValue;
    @FXML private Label lbl_dateValue;
    @FXML private Label lbl_packageType;
    @FXML private Label lbl_weight;
    @FXML private Label lbl_dimensions;
    @FXML private Label lbl_price;

    // Controlador de negocio
    private AdminShipmentsController controller;
    
    // Shipment seleccionado actualmente
    private Shipment selectedShipment;
    
    // Formateador para las fechas
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Inicializa el controlador de vista
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar controlador
        controller = new AdminShipmentsController();
        controller.setViewController(this);
        
        // Configurar la tabla
        setupTable();
        
        // Configurar el filtrado
        setupFilters();
        
        // Configurar botones de acción
        setupActionButtons();
        
        // Cargar datos iniciales
        controller.loadAllShipments();
    }

    /**
     * Configura la tabla y sus columnas
     */
    private void setupTable() {
        // Configurar las columnas
        col_id.setCellValueFactory(cellData -> {
            Shipment shipment = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> {
                    return shipment != null && shipment.getId() != null ? 
                        shipment.getId().toString().substring(0, 8) + "..." : "";
                }
            );
        });
        
        col_user.setCellValueFactory(cellData -> {
            Shipment shipment = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> {
                    return shipment != null && shipment.getUser() != null ? 
                        shipment.getUser().getName() : "No asignado";
                }
            );
        });
        
        col_courier.setCellValueFactory(cellData -> {
            Shipment shipment = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> {
                    return shipment != null && shipment.getDeliverer() != null ? 
                        shipment.getDeliverer().getName() : "No asignado";
                }
            );
        });
        
        col_origin.setCellValueFactory(cellData -> {
            Shipment shipment = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> {
                    return shipment != null && shipment.getOrigin() != null ? 
                        shipment.getOrigin().getFullAddress() : "No especificado";
                }
            );
        });
        
        col_destination.setCellValueFactory(cellData -> {
            Shipment shipment = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> {
                    return shipment != null && shipment.getDestination() != null ? 
                        shipment.getDestination().getFullAddress() : "No especificado";
                }
            );
        });
        
        col_state.setCellValueFactory(cellData -> {
            Shipment shipment = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> {
                    return shipment != null && shipment.getStatus() != null ? 
                        shipment.getStatus().name() : "DESCONOCIDO";
                }
            );
        });
        
        col_date.setCellValueFactory(cellData -> {
            Shipment shipment = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> {
                    if (shipment != null && shipment.getCreationDate() != null) {
                        return shipment.getCreationDate().format(dateFormatter);
                    }
                    return "No especificada";
                }
            );
        });
        
        // Manejar la selección de elementos en la tabla
        tbl_shipments.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedShipment = newSelection;
                    populateShipmentDetails(selectedShipment);
                    updateButtonsState();
                }
            }
        );
    }

    /**
     * Configura los filtros para la tabla
     */
    private void setupFilters() {
        // Poblar el ChoiceBox de estados con los valores del enum ShipmentStatus
        chb_stateFilter.getItems().add("TODOS");
        for (ShipmentStatus status : ShipmentStatus.values()) {
            chb_stateFilter.getItems().add(status.name());
        }
        chb_stateFilter.setValue("TODOS");
        
        // Configurar botón de filtrar
        btn_filter.setOnAction(e -> {
            String statusFilter = chb_stateFilter.getValue();
            LocalDate dateFilter = dtp_dateFilter.getValue();
            controller.filterShipments(statusFilter, dateFilter);
        });
        
        // Configurar botón de limpiar filtros
        btn_clearFilter.setOnAction(e -> {
            chb_stateFilter.setValue("TODOS");
            dtp_dateFilter.setValue(null);
            controller.loadAllShipments();
        });
    }

    /**
     * Configura los botones de acción
     */
    private void setupActionButtons() {
        // Botón para asignar repartidor
        btn_assignCourier.setOnAction(e -> {
            if (selectedShipment != null) {
                controller.showAssignCourierDialog(selectedShipment);
            } else {
                showStatusMessage("Debe seleccionar un envío primero", "warning");
            }
        });
        
        // Botón para actualizar estado
        btn_updateStatus.setOnAction(e -> {
            if (selectedShipment != null) {
                controller.showUpdateStatusDialog(selectedShipment);
            } else {
                showStatusMessage("Debe seleccionar un envío primero", "warning");
            }
        });
        
        // Botón para registrar incidencia
        btn_incident.setOnAction(e -> {
            if (selectedShipment != null) {
                controller.showAddIncidentDialog(selectedShipment);
            } else {
                showStatusMessage("Debe seleccionar un envío primero", "warning");
            }
        });
        
        // Botón para eliminar envío
        btn_deleteShipment.setOnAction(e -> {
            if (selectedShipment != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmar eliminación");
                alert.setHeaderText("Eliminar envío");
                alert.setContentText("¿Está seguro que desea eliminar el envío seleccionado? Esta acción no se puede deshacer.");
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    controller.deleteShipment(selectedShipment);
                }
            } else {
                showStatusMessage("Debe seleccionar un envío primero", "warning");
            }
        });
    }

    /**
     * Actualiza el estado de los botones según el envío seleccionado
     */
    private void updateButtonsState() {
        if (selectedShipment == null) {
            btn_assignCourier.setDisable(true);
            btn_updateStatus.setDisable(true);
            btn_incident.setDisable(true);
            btn_deleteShipment.setDisable(true);
            return;
        }
        
        // Habilitar los botones según el estado del envío
        ShipmentStatus status = selectedShipment.getStatus();
        
        // Solo se puede asignar repartidor a envíos en estado ASSIGNED o PENDING
        btn_assignCourier.setDisable(status != ShipmentStatus.ASSIGNED && status != ShipmentStatus.PENDING);
        
        // Se puede actualizar estado para cualquier envío que no esté DELIVERED o CANCELLED
        btn_updateStatus.setDisable(status == ShipmentStatus.DELIVERED || status == ShipmentStatus.CANCELLED);
        
        // Se puede registrar incidencia para cualquier envío que esté en proceso
        btn_incident.setDisable(status == ShipmentStatus.DELIVERED || status == ShipmentStatus.CANCELLED);

        // Solo se pueden eliminar envíos en estado PENDING o CANCELLED
        btn_deleteShipment.setDisable(status != ShipmentStatus.PENDING && status != ShipmentStatus.CANCELLED);
    }

    /**
     * Llena los campos de detalles con la información del envío seleccionado
     * @param shipment envío a mostrar
     */
    private void populateShipmentDetails(Shipment shipment) {
        if (shipment == null) {
            clearShipmentDetails();
            return;
        }

        // Información básica
        lbl_shipmentIdValue.setText(shipment.getId().toString().substring(0, 8) + "...");
        lbl_userValue.setText(shipment.getUser() != null ? shipment.getUser().getName() : "No asignado");
        lbl_courierValue.setText(shipment.getDeliverer() != null ? shipment.getDeliverer().getName() : "No asignado");
        lbl_originValue.setText(shipment.getOriginAddress());
        lbl_destinationValue.setText(shipment.getDestinationAddress());
        lbl_stateValue.setText(shipment.getStatus().name());
        lbl_dateValue.setText(shipment.getCreationDate() != null ? 
                             shipment.getCreationDate().format(dateFormatter) : "No especificada");

        // Información adicional
        lbl_packageType.setText("Tipo de paquete: " + (shipment.isFragile() ? "Frágil" : "Estándar"));
        lbl_weight.setText("Peso: " + shipment.getWeight() + " kg");
        lbl_dimensions.setText("Dimensiones: " + String.format("%.2f m³", shipment.getVolume()));
        lbl_price.setText("Precio: $" + String.format("%.2f", shipment.getTotalCost()));
    }

    /**
     * Limpia los campos de detalles
     */
    private void clearShipmentDetails() {
        lbl_shipmentIdValue.setText("--");
        lbl_userValue.setText("--");
        lbl_courierValue.setText("--");
        lbl_originValue.setText("--");
        lbl_destinationValue.setText("--");
        lbl_stateValue.setText("--");
        lbl_dateValue.setText("--");
        lbl_packageType.setText("Tipo de paquete: --");
        lbl_weight.setText("Peso: --");
        lbl_dimensions.setText("Dimensiones: --");
        lbl_price.setText("Precio: --");
    }

    /**
     * Carga datos en la tabla
     * @param shipments lista de envíos a mostrar
     */
    @Override
    public void loadTableData(ObservableList<Shipment> shipments) {
        tbl_shipments.setItems(shipments);
        
        // Limpiar selección y detalles
        tbl_shipments.getSelectionModel().clearSelection();
        clearShipmentDetails();
        selectedShipment = null;
        updateButtonsState();
        
        // Mostrar mensaje según la cantidad de envíos
        if (shipments.isEmpty()) {
            showStatusMessage("No se encontraron envíos", "info");
        } else {
            showStatusMessage("Se encontraron " + shipments.size() + " envíos", "success");
        }
    }

    /**
     * Actualiza un envío en la tabla
     * @param updatedShipment envío actualizado
     */
    @Override
    public void updateShipmentInTable(Shipment updatedShipment) {
        ObservableList<Shipment> items = tbl_shipments.getItems();
        
        // Buscar el envío a actualizar
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(updatedShipment.getId())) {
                // Reemplazar el envío en la tabla
                items.set(i, updatedShipment);
                
                // Si era el seleccionado, actualizar los detalles
                if (selectedShipment != null && selectedShipment.getId().equals(updatedShipment.getId())) {
                    selectedShipment = updatedShipment;
                    populateShipmentDetails(selectedShipment);
                    updateButtonsState();
                }
                break;
            }
        }
        
        // Refrescar la tabla
        tbl_shipments.refresh();
    }

    /**
     * Elimina un envío de la tabla
     * @param shipment envío a eliminar
     */
    @Override
    public void removeShipmentFromTable(Shipment shipment) {
        tbl_shipments.getItems().remove(shipment);
        
        if (selectedShipment != null && selectedShipment.getId().equals(shipment.getId())) {
            selectedShipment = null;
            clearShipmentDetails();
            updateButtonsState();
        }
        
        showStatusMessage("Envío eliminado correctamente", "success");
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
}