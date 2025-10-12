package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.App;
import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.UserShipmentsController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.ShipmentDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Controlador de vista para la pantalla de envíos del usuario.
 * Gestiona la interfaz UserShipments.fxml.
 */
public class UserShipmentsViewController implements Initializable {

    // Componentes de la interfaz
    @FXML private TableView<ShipmentDTO> tbl_shipments;
    @FXML private TableColumn<ShipmentDTO, String> col_id;
    @FXML private TableColumn<ShipmentDTO, String> col_origin;
    @FXML private TableColumn<ShipmentDTO, String> col_destination;
    @FXML private TableColumn<ShipmentDTO, ShipmentStatus> col_status;
    @FXML private TableColumn<ShipmentDTO, LocalDateTime> col_date;
    @FXML private TableColumn<ShipmentDTO, Double> col_cost;

    @FXML private DatePicker dp_startDate;
    @FXML private DatePicker dp_endDate;
    @FXML private ChoiceBox<ShipmentStatus> chb_status;
    @FXML private Button btn_filter;
    @FXML private Button btn_clearFilter;

    @FXML private Label lbl_detailId;
    @FXML private Label lbl_detailOrigin;
    @FXML private Label lbl_detailDestination;
    @FXML private Label lbl_detailStatus;
    @FXML private Label lbl_detailDate;
    @FXML private Label lbl_detailWeight;
    @FXML private Label lbl_detailDimensions;
    @FXML private Label lbl_detailServices;
    @FXML private Label lbl_detailCost;
    @FXML private Label lbl_detailDeliverer;

    @FXML private Button btn_trackShipment;
    @FXML private Button btn_copyShipmentId;
    @FXML private Button btn_payShipment;
    @FXML private Button btn_newShipment;
    @FXML private Button btn_editShipment;
    @FXML private Button btn_cancelShipment;
    @FXML private Button btn_refreshShipments;

    // Controlador
    private UserShipmentsController controller;
    
    // Listas para gestionar los datos
    private ObservableList<ShipmentDTO> shipmentsList = FXCollections.observableArrayList();
    private FilteredList<ShipmentDTO> filteredShipments;
    
    // Formato para fechas
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Inicializar el controlador
            controller = new UserShipmentsController();
            
            // Configurar componentes
            setupTable();
            setupFilters();
            setupButtons();
            
            // Cargar datos iniciales
            loadShipments();
        } catch (Exception e) {
            showError("Error al inicializar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Configura la tabla de envíos
     */
    private void setupTable() {
        // Configurar columnas
        col_id.setCellValueFactory(cellData -> {
            UUID id = cellData.getValue().getId();
            String shortId = id != null ? id.toString().substring(0, 8) + "..." : "N/A";
            return javafx.beans.binding.Bindings.createStringBinding(() -> shortId);
        });
        
        col_origin.setCellValueFactory(cellData -> {
            if (cellData.getValue().getOriginAddress() == null) {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
            String origin = cellData.getValue().getOriginAddress().getAlias();
            return javafx.beans.binding.Bindings.createStringBinding(() -> origin);
        });
        
        col_destination.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDestinationAddress() == null) {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
            String destination = cellData.getValue().getDestinationAddress().getAlias();
            return javafx.beans.binding.Bindings.createStringBinding(() -> destination);
        });
        
        col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
        col_date.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        col_cost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        
        // Formatear celdas
        col_date.setCellFactory(column -> new TableCell<ShipmentDTO, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(dateFormatter));
                }
            }
        });
        
        col_cost.setCellFactory(column -> new TableCell<ShipmentDTO, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        col_status.setCellFactory(column -> new TableCell<ShipmentDTO, ShipmentStatus>() {
            @Override
            protected void updateItem(ShipmentStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    // Aplicar estilo según estado
                    switch (item) {
                        case PENDING:
                            setStyle("-fx-text-fill: orange;");
                            break;
                        case ASSIGNED:
                            setStyle("-fx-text-fill: blue;");
                            break;
                        case IN_TRANSIT:
                            setStyle("-fx-text-fill: purple;");
                            break;
                        case DELIVERED:
                            setStyle("-fx-text-fill: green;");
                            break;
                        case CANCELLED:
                            setStyle("-fx-text-fill: red;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        // Listener para mostrar detalles al seleccionar
        tbl_shipments.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayShipmentDetails(newSelection);
                updateButtonStates(newSelection);
            }
        });
    }
    
    /**
     * Configura los filtros de la tabla
     */
    private void setupFilters() {
        // Configurar ChoiceBox de estado
        chb_status.getItems().add(null); // Opción para "Todos"
        chb_status.getItems().addAll(ShipmentStatus.values());
        chb_status.setConverter(new javafx.util.StringConverter<ShipmentStatus>() {
            @Override
            public String toString(ShipmentStatus status) {
                return status == null ? "Todos" : status.toString();
            }

            @Override
            public ShipmentStatus fromString(String string) {
                return null; // No necesario para ChoiceBox
            }
        });
        
        // Acciones de los botones de filtro
        btn_filter.setOnAction(event -> applyFilters());
        btn_clearFilter.setOnAction(event -> clearFilters());
    }
    
    /**
     * Configura los botones de acción
     */
    private void setupButtons() {
        // Botón de nuevo envío
        btn_newShipment.setOnAction(event -> openNewShipmentDialog());
        
        // Botón de editar envío
        btn_editShipment.setOnAction(event -> {
            ShipmentDTO selected = tbl_shipments.getSelectionModel().getSelectedItem();
            if (selected != null) {
                editShipment(selected);
            }
        });
        
        // Botón de cancelar envío
        btn_cancelShipment.setOnAction(event -> {
            ShipmentDTO selected = tbl_shipments.getSelectionModel().getSelectedItem();
            if (selected != null) {
                cancelShipment(selected);
            }
        });
        
        // Botón de refrescar
        btn_refreshShipments.setOnAction(event -> loadShipments());
        
        // Botón de rastrear envío
        btn_trackShipment.setOnAction(event -> {
            ShipmentDTO selected = tbl_shipments.getSelectionModel().getSelectedItem();
            if (selected != null) {
                trackShipment(selected);
            }
        });
        
        // Botón de copiar ID
        btn_copyShipmentId.setOnAction(event -> {
            ShipmentDTO selected = tbl_shipments.getSelectionModel().getSelectedItem();
            if (selected != null) {
                copyShipmentIdToClipboard(selected.getId());
            }
        });
        
        // Botón de ir a pagos
        btn_payShipment.setOnAction(event -> {
            ShipmentDTO selected = tbl_shipments.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openPaymentsScreen(selected.getId());
            }
        });
        
        // Deshabilitar botones que requieren selección
        btn_editShipment.setDisable(true);
        btn_cancelShipment.setDisable(true);
        btn_trackShipment.setDisable(true);
        btn_copyShipmentId.setDisable(true);
        btn_payShipment.setDisable(true);
    }
    
    /**
     * Carga los envíos del usuario actual
     */
    private void loadShipments() {
        try {
            // Limpiar lista actual
            shipmentsList.clear();
            
            // Obtener envíos del usuario
            List<ShipmentDTO> userShipments = controller.getUserShipments();
            
            // Agregar a la lista observable
            shipmentsList.addAll(userShipments);
            
            // Crear filtered list
            filteredShipments = new FilteredList<>(shipmentsList);
            
            // Asignar a la tabla
            tbl_shipments.setItems(filteredShipments);
            
            // Limpiar selección
            tbl_shipments.getSelectionModel().clearSelection();
            clearDetailView();
            
        } catch (Exception e) {
            showError("Error al cargar envíos: " + e.getMessage());
        }
    }
    
    /**
     * Aplica los filtros seleccionados a la lista de envíos
     */
    private void applyFilters() {
        LocalDate startDate = dp_startDate.getValue();
        LocalDate endDate = dp_endDate.getValue();
        ShipmentStatus status = chb_status.getValue();
        
        Predicate<ShipmentDTO> predicate = shipment -> true; // Inicialmente acepta todos
        
        // Filtro por fecha de inicio
        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            predicate = predicate.and(shipment -> 
                shipment.getCreationDate() != null && 
                !shipment.getCreationDate().isBefore(startDateTime));
        }
        
        // Filtro por fecha de fin
        if (endDate != null) {
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay(); // Incluye todo el día
            predicate = predicate.and(shipment -> 
                shipment.getCreationDate() != null && 
                shipment.getCreationDate().isBefore(endDateTime));
        }
        
        // Filtro por estado
        if (status != null) {
            predicate = predicate.and(shipment -> 
                shipment.getStatus() == status);
        }
        
        // Aplicar filtro
        filteredShipments.setPredicate(predicate);
    }
    
    /**
     * Limpia los filtros aplicados
     */
    private void clearFilters() {
        dp_startDate.setValue(null);
        dp_endDate.setValue(null);
        chb_status.setValue(null);
        
        // Quitar todos los filtros
        filteredShipments.setPredicate(shipment -> true);
    }
    
    /**
     * Muestra los detalles del envío seleccionado
     * @param shipment el envío a mostrar
     */
    private void displayShipmentDetails(ShipmentDTO shipment) {
        if (shipment == null) {
            clearDetailView();
            return;
        }
        
        // Mostrar ID
        lbl_detailId.setText(shipment.getId().toString());
        
        // Mostrar origen y destino
        lbl_detailOrigin.setText(shipment.getOriginAddress() != null ? 
                shipment.getOriginAddress().getAlias() + " - " + shipment.getOriginAddress().getStreet() : "N/A");
        
        lbl_detailDestination.setText(shipment.getDestinationAddress() != null ? 
                shipment.getDestinationAddress().getAlias() + " - " + shipment.getDestinationAddress().getStreet() : "N/A");
        
        // Mostrar estado y fecha
        lbl_detailStatus.setText(shipment.getStatus() != null ? shipment.getStatus().toString() : "N/A");
        
        // Cambiar color según estado
        String statusStyle = "-fx-font-weight: bold;";
        if (shipment.getStatus() != null) {
            switch (shipment.getStatus()) {
                case PENDING:
                    statusStyle += "-fx-text-fill: orange;";
                    break;
                case ASSIGNED:
                    statusStyle += "-fx-text-fill: blue;";
                    break;
                case IN_TRANSIT:
                    statusStyle += "-fx-text-fill: purple;";
                    break;
                case DELIVERED:
                    statusStyle += "-fx-text-fill: green;";
                    break;
                case CANCELLED:
                    statusStyle += "-fx-text-fill: red;";
                    break;
            }
        }
        lbl_detailStatus.setStyle(statusStyle);
        
        // Fecha
        lbl_detailDate.setText(shipment.getCreationDate() != null ? 
                shipment.getCreationDate().format(dateFormatter) : "N/A");
        
        // Detalles del envío
        lbl_detailWeight.setText(String.format("%.2f kg", shipment.getWeight()));
        lbl_detailDimensions.setText(shipment.getDimensions() != null ? shipment.getDimensions() : "No especificado");
        
        // Servicios adicionales (asumimos que hay servicios adicionales)
        String services = shipment.getPriority() != null ? "Prioridad: " + shipment.getPriority().toString() : "";
        lbl_detailServices.setText(services);
        
        // Costo
        lbl_detailCost.setText(String.format("$%.2f", shipment.getCost()));
        
        // Repartidor
        lbl_detailDeliverer.setText(controller.getDelivererName(shipment.getDelivererId()));
    }
    
    /**
     * Limpia la vista de detalles
     */
    private void clearDetailView() {
        lbl_detailId.setText("-");
        lbl_detailOrigin.setText("-");
        lbl_detailDestination.setText("-");
        lbl_detailStatus.setText("-");
        lbl_detailDate.setText("-");
        lbl_detailWeight.setText("-");
        lbl_detailDimensions.setText("-");
        lbl_detailServices.setText("-");
        lbl_detailCost.setText("-");
        lbl_detailDeliverer.setText("-");
        
        // Restablecer estilo del estado
        lbl_detailStatus.setStyle("");
        
        // Deshabilitar botones que requieren selección
        btn_editShipment.setDisable(true);
        btn_cancelShipment.setDisable(true);
        btn_trackShipment.setDisable(true);
    }
    
    /**
     * Actualiza el estado de los botones según el envío seleccionado
     * @param shipment el envío seleccionado
     */
    private void updateButtonStates(ShipmentDTO shipment) {
        if (shipment == null) {
            btn_editShipment.setDisable(true);
            btn_cancelShipment.setDisable(true);
            btn_trackShipment.setDisable(true);
            btn_copyShipmentId.setDisable(true);
            btn_payShipment.setDisable(true);
            return;
        }
        
        // Activar botón de rastreo si tiene repartidor asignado
        btn_trackShipment.setDisable(shipment.getDelivererId() == null);
        
        // Solo se pueden editar envíos pendientes
        btn_editShipment.setDisable(shipment.getStatus() != ShipmentStatus.PENDING);
        
        // Solo se pueden cancelar envíos pendientes o asignados
        btn_cancelShipment.setDisable(shipment.getStatus() != ShipmentStatus.PENDING && 
                                   shipment.getStatus() != ShipmentStatus.ASSIGNED);
                                   
        // Siempre permitir copiar el ID si hay un envío seleccionado
        btn_copyShipmentId.setDisable(false);
        
        // Solo permitir ir a pagos para envíos que no estén cancelados
        btn_payShipment.setDisable(shipment.getStatus() == ShipmentStatus.CANCELLED);
    }
    
    /**
     * Abre el diálogo para crear un nuevo envío
     */
    private void openNewShipmentDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("interfaces/QuoteShipment.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage stage = new Stage();
            stage.setTitle("Nuevo Envío");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            
            // Mostrar y esperar a que se cierre
            stage.showAndWait();
            
            // Refrescar la lista de envíos
            loadShipments();
            
        } catch (IOException e) {
            showError("Error al abrir formulario de nuevo envío: " + e.getMessage());
        }
    }
    
    /**
     * Abre el diálogo para editar un envío existente
     * @param shipment el envío a editar
     */
    private void editShipment(ShipmentDTO shipment) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("interfaces/QuoteShipment.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Obtener el controlador para pasarle el envío a editar
            QuoteShipmentViewController controller = loader.getController();
            controller.setEditMode(shipment);
            
            Stage stage = new Stage();
            stage.setTitle("Editar Envío");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            
            // Mostrar y esperar a que se cierre
            stage.showAndWait();
            
            // Refrescar la lista de envíos
            loadShipments();
            
        } catch (IOException e) {
            showError("Error al abrir formulario de edición: " + e.getMessage());
        }
    }
    
    /**
     * Cancela un envío
     * @param shipment el envío a cancelar
     */
    private void cancelShipment(ShipmentDTO shipment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancelar Envío");
        alert.setHeaderText("¿Está seguro que desea cancelar este envío?");
        alert.setContentText("Esta acción no se puede deshacer.");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    boolean success = controller.cancelShipment(shipment.getId());
                    
                    if (success) {
                        loadShipments(); // Recargar la lista
                        showInfo("Envío cancelado exitosamente.");
                    } else {
                        showError("No se pudo cancelar el envío.");
                    }
                } catch (Exception e) {
                    showError("Error al cancelar envío: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Muestra la información de rastreo de un envío en un mapa
     * @param shipment el envío a rastrear
     */
    private void trackShipment(ShipmentDTO shipment) {
        try {
            // Cargar el FXML de la ventana de seguimiento
            FXMLLoader loader = new FXMLLoader(App.class.getResource("interfaces/ShipmentTracking.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador y pasarle los datos necesarios
            ShipmentTrackingViewController trackingController = loader.getController();
            
            // Iniciar servicio para obtener datos actualizados del envío
            ShipmentService shipmentService = ShipmentService.getInstance();
            
            // Inicializar el controlador con los datos
            trackingController.initData(shipment, shipmentService);
            
            // Crear y mostrar la ventana
            Stage stage = new Stage();
            stage.setTitle("Seguimiento de Envío - " + shipment.getId().toString());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al abrir la ventana de seguimiento: " + e.getMessage());
        }
    }
    
    /**
     * Muestra un mensaje de error
     * @param message el mensaje a mostrar
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Muestra un mensaje informativo
     * @param message el mensaje a mostrar
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Copia el ID del envío al portapapeles del sistema
     * @param shipmentId el ID del envío a copiar
     */
    private void copyShipmentIdToClipboard(UUID shipmentId) {
        if (shipmentId == null) return;
        
        // Obtener el portapapeles del sistema
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        
        // Copiar el ID como texto
        content.putString(shipmentId.toString());
        clipboard.setContent(content);
        
        // Notificar al usuario
        showInfo("ID del envío copiado al portapapeles: \n" + shipmentId.toString() + 
                "\n\nPuede pegarlo en la pantalla de pagos.");
    }
    
    /**
     * Abre la pantalla de pagos con el ID del envío precargado
     * @param shipmentId el ID del envío para el pago
     */
    private void openPaymentsScreen(UUID shipmentId) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("interfaces/Payments.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Obtener el controlador para pasarle el ID del envío
            PaymentsViewController controller = loader.getController();
            
            // Si el controlador tiene un método para precargar el ID, lo llamamos
            if (controller != null && shipmentId != null) {
                controller.preloadShipmentId(shipmentId.toString());
            }
            
            Stage stage = new Stage();
            stage.setTitle("Pagos");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            
            // Mostrar la ventana
            stage.show();
            
        } catch (IOException e) {
            showError("Error al abrir la pantalla de pagos: " + e.getMessage());
        }
    }
}