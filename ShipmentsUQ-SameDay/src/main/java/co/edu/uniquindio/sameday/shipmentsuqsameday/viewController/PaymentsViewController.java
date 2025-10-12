package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.application.HostServices;

import co.edu.uniquindio.sameday.shipmentsuqsameday.App;
import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.PaymentsController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.PaymentDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserPaymentMethodDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentStatus;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.net.URI;
import java.util.Optional;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;

/**
 * Controlador de vista para la pantalla de gestión de pagos.
 */
public class PaymentsViewController implements Initializable {

    @FXML private TextField txt_shipmentId;
    @FXML private TextField txt_amount;
    @FXML private ChoiceBox<PaymentMethod> chb_paymentMethod;
    @FXML private DatePicker dp_dueDate;
    @FXML private TextField txt_reference;
    @FXML private ComboBox<UserPaymentMethodDTO> cmb_savedPaymentMethods;
    @FXML private Label lbl_paymentMethodDetails;
    @FXML private Button btn_addPaymentMethod;
    @FXML private Button btn_pay;
    @FXML private Button btn_clear;
    @FXML private DatePicker dp_filterDate;
    @FXML private ChoiceBox<PaymentStatus> chb_filterStatus;
    @FXML private Button btn_filter;
    @FXML private Button btn_clearFilter;
    @FXML private TableView<PaymentDTO> tbl_payments;
    @FXML private TableColumn<PaymentDTO, String> col_id;
    @FXML private TableColumn<PaymentDTO, String> col_date;
    @FXML private TableColumn<PaymentDTO, String> col_shipmentId;
    @FXML private TableColumn<PaymentDTO, String> col_amount;
    @FXML private TableColumn<PaymentDTO, String> col_method;
    @FXML private TableColumn<PaymentDTO, String> col_status;
    @FXML private Button btn_viewReceipt;
    @FXML private Button btn_downloadReport;
    @FXML private Label lbl_status;
    @FXML private VBox vbox_paymentMethodInfo;
    
    // Controladores de negocio
    private PaymentsController controller;
    private ShipmentService shipmentService;
    
    // Listas para los pagos y métodos de pago
    private ObservableList<PaymentDTO> payments;
    private ObservableList<UserPaymentMethodDTO> savedPaymentMethods;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar controlador y componentes
        initController();
        setupUIControls();
        configureScrollPane();
        loadPaymentMethods();
        loadPaymentHistory();
        setupEventHandlers();
    }
    
    @FXML
    private ScrollPane mainScrollPane;
    
    /**
     * Configura el ScrollPane para asegurar que se adapte adecuadamente al redimensionar la ventana
     */
    private void configureScrollPane() {
        try {
            if (mainScrollPane != null) {
                // Configuramos el ScrollPane para que se adapte a la ventana
                mainScrollPane.setFitToWidth(true);
                mainScrollPane.setFitToHeight(true);
                mainScrollPane.setPannable(true);
                
                // Configuramos las políticas de barras de desplazamiento
                mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                
                // Añadimos listener para redimensionar contenido cuando cambie el tamaño de la ventana
                mainScrollPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                    adjustContentSize(mainScrollPane);
                });
                
                mainScrollPane.heightProperty().addListener((obs, oldVal, newVal) -> {
                    adjustContentSize(mainScrollPane);
                });
                
                // Configurar la tabla para que se ajuste al espacio disponible
                if (tbl_payments != null) {
                    // Usando callback personalizado en lugar de CONSTRAINED_RESIZE_POLICY
                    tbl_payments.setColumnResizePolicy(param -> true);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al configurar ScrollPane: " + e.getMessage());
        }
    }
    
    /**
     * Ajusta el tamaño del contenido del ScrollPane cuando la ventana cambia de tamaño
     */
    private void adjustContentSize(ScrollPane scrollPane) {
        if (scrollPane != null && scrollPane.getContent() != null) {
            BorderPane content = (BorderPane) scrollPane.getContent();
            
            // Ajustamos el tamaño del contenido en función del tamaño del ScrollPane
            double width = scrollPane.getViewportBounds().getWidth();
            double height = scrollPane.getViewportBounds().getHeight();
            
            // Aseguramos un tamaño mínimo
            content.setPrefWidth(Math.max(width, 1000));
            content.setPrefHeight(Math.max(height, 600));
        }
    }
    
    /**
     * Precarga el ID del envío en el formulario de pago.
     * Este método es llamado cuando se navega desde la pantalla de envíos
     * con un ID específico para realizar un pago.
     * 
     * @param shipmentId El ID del envío a precargar en el formulario
     */
    public void preloadShipmentId(String shipmentId) {
        if (shipmentId != null && !shipmentId.isEmpty()) {
            txt_shipmentId.setText(shipmentId);
            // Opcionalmente, cargar información adicional del envío
            try {
                UUID id = UUID.fromString(shipmentId);
                Double amount = controller.getShipmentAmount(id);
                
                if (amount != null) {
                    if (amount > 0) {
                        // Hay monto por pagar, habilitar formulario
                        txt_amount.setText(String.format("%.2f", amount));
                        btn_pay.setDisable(false);
                        
                        // Informar al usuario
                        lbl_status.setText("Información de envío cargada. Complete el formulario para realizar el pago.");
                        lbl_status.setStyle("-fx-text-fill: #27ae60;");
                    } else {
                        // El envío ya está pagado, deshabilitar formulario
                        txt_amount.setText("0.00");
                        btn_pay.setDisable(true);
                        
                        // Informar al usuario
                        lbl_status.setText("Este envío ya ha sido pagado. No se requiere ninguna acción adicional.");
                        lbl_status.setStyle("-fx-text-fill: #3498db;");
                    }
                } else {
                    // No se pudo obtener información del envío
                    btn_pay.setDisable(true);
                    lbl_status.setText("No se encontró información de pago para este envío.");
                    lbl_status.setStyle("-fx-text-fill: #e74c3c;");
                }
            } catch (Exception e) {
                // Si ocurre un error al cargar la información del envío
                btn_pay.setDisable(true);
                lbl_status.setText("ID de envío cargado. No se pudo obtener el monto automáticamente.");
                lbl_status.setStyle("-fx-text-fill: #e67e22;");
            }
        }
    }
    
    /**
     * Inicializa el controlador de negocio
     */
    private void initController() {
        try {
            controller = new PaymentsController();
            shipmentService = ShipmentService.getInstance();
        } catch (Exception e) {
            System.err.println("Error al inicializar controladores: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error de inicialización", "No se pudo inicializar los controladores", 
                    "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Configura los controles de la UI
     */
    private void setupUIControls() {
        // Configurar el ChoiceBox de métodos de pago
        chb_paymentMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        chb_paymentMethod.setConverter(new StringConverter<PaymentMethod>() {
            @Override
            public String toString(PaymentMethod method) {
                if (method == null) return null;
                switch (method) {
                    case CASH: return "Efectivo";
                    case CREDIT_CARD: return "Tarjeta de Crédito";
                    case DEBIT_CARD: return "Tarjeta Débito";
                    case PSE: return "PSE (Débito Bancario)";
                    case NEQUI: return "Nequi";
                    case DAVIPLATA: return "Daviplata";
                    default: return method.name();
                }
            }
            
            @Override
            public PaymentMethod fromString(String string) {
                return null; // No necesitamos esto para un ChoiceBox
            }
        });
        
        // Configurar el ChoiceBox de estados para filtrado
        chb_filterStatus.setItems(FXCollections.observableArrayList(PaymentStatus.values()));
        chb_filterStatus.setConverter(new StringConverter<PaymentStatus>() {
            @Override
            public String toString(PaymentStatus status) {
                if (status == null) return null;
                switch (status) {
                    case PENDING: return "Pendiente";
                    case COMPLETED: return "Completado";
                    case FAILED: return "Fallido";
                    case REFUNDED: return "Reembolsado";
                    case CANCELLED: return "Cancelado";
                    default: return status.name();
                }
            }
            
            @Override
            public PaymentStatus fromString(String string) {
                return null; // No necesitamos esto para un ChoiceBox
            }
        });
        
        // Configurar las columnas de la tabla
        col_id.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getId().toString().substring(0, 8) + "..."));
        col_date.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getCreationDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        col_shipmentId.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getShipmentId() != null ? 
                    data.getValue().getShipmentId().toString().substring(0, 8) + "..." : "N/A"));
        col_amount.setCellValueFactory(data -> 
                new SimpleStringProperty(String.format("$%.2f", data.getValue().getAmount())));
        col_method.setCellValueFactory(data -> {
            PaymentMethod method = data.getValue().getPaymentMethod();
            String methodName = "";
            switch (method) {
                case CASH: methodName = "Efectivo"; break;
                case CREDIT_CARD: methodName = "Crédito"; break;
                case DEBIT_CARD: methodName = "Débito"; break;
                case PSE: methodName = "PSE"; break;
                case NEQUI: methodName = "Nequi"; break;
                case DAVIPLATA: methodName = "Daviplata"; break;
                default: methodName = method.name();
            }
            return new SimpleStringProperty(methodName);
        });
        col_status.setCellValueFactory(data -> {
            PaymentStatus status = data.getValue().getStatus();
            String statusName = "";
            switch (status) {
                case PENDING: statusName = "Pendiente"; break;
                case COMPLETED: statusName = "Completado"; break;
                case FAILED: statusName = "Fallido"; break;
                case REFUNDED: statusName = "Reembolsado"; break;
                case CANCELLED: statusName = "Cancelado"; break;
                default: statusName = status.name();
            }
            return new SimpleStringProperty(statusName);
        });
    }
    
    /**
     * Carga los métodos de pago guardados
     */
    private void loadPaymentMethods() {
        savedPaymentMethods = FXCollections.observableArrayList(controller.getSavedPaymentMethods());
        cmb_savedPaymentMethods.setItems(savedPaymentMethods);
        cmb_savedPaymentMethods.setConverter(new StringConverter<UserPaymentMethodDTO>() {
            @Override
            public String toString(UserPaymentMethodDTO method) {
                if (method == null) return null;
                return method.getAlias() + (method.isDefault() ? " (Predeterminado)" : "");
            }
            
            @Override
            public UserPaymentMethodDTO fromString(String string) {
                return null; // No necesitamos esto para un ComboBox
            }
        });
    }
    
    /**
     * Carga el historial de pagos
     */
    private void loadPaymentHistory() {
        payments = FXCollections.observableArrayList(controller.getPaymentHistory());
        tbl_payments.setItems(payments);
    }
    
    /**
     * Configura los manejadores de eventos
     */
    private void setupEventHandlers() {
        // Mostrar detalles cuando se selecciona un método de pago
        cmb_savedPaymentMethods.setOnAction(e -> {
            UserPaymentMethodDTO selected = cmb_savedPaymentMethods.getValue();
            if (selected != null) {
                displayPaymentMethodDetails(selected);
                
                // También actualizar el tipo de método de pago en el selector principal
                chb_paymentMethod.setValue(selected.getPaymentMethod());
            } else {
                lbl_paymentMethodDetails.setText("");
                vbox_paymentMethodInfo.setVisible(false);
            }
        });
        
        // Botón para abrir ventana de agregar método de pago
        btn_addPaymentMethod.setOnAction(e -> openAddPaymentMethodWindow());
        
        // Botón para realizar el pago
        btn_pay.setOnAction(e -> processPayment());
        
        // Botón para limpiar el formulario
        btn_clear.setOnAction(e -> clearPaymentForm());
        
        // Botón para filtrar pagos
        btn_filter.setOnAction(e -> filterPayments());
        
        // Botón para limpiar filtros
        btn_clearFilter.setOnAction(e -> {
            dp_filterDate.setValue(null);
            chb_filterStatus.setValue(null);
            loadPaymentHistory();
        });
        
        // Activar botón de ver comprobante cuando se selecciona un pago
        tbl_payments.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
            btn_viewReceipt.setDisable(newSelection == null);
        });
        
        // Botón para ver comprobante
        btn_viewReceipt.setOnAction(e -> {
            PaymentDTO selected = tbl_payments.getSelectionModel().getSelectedItem();
            if (selected != null) {
                viewReceipt(selected.getId());
            }
        });
        
        // Botón para descargar reporte
        btn_downloadReport.setOnAction(e -> downloadReport());
    }
    
    /**
     * Muestra detalles del método de pago seleccionado
     */
    private void displayPaymentMethodDetails(UserPaymentMethodDTO method) {
        vbox_paymentMethodInfo.setVisible(true);
        
        StringBuilder details = new StringBuilder();
        switch (method.getPaymentMethod()) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                details.append(method.getCardType()).append(" •••• ")
                       .append(method.getLastFourDigits());
                break;
            case NEQUI:
            case DAVIPLATA:
                details.append(method.getPaymentMethod().toString())
                       .append(" • Tel: ").append(method.getPhoneNumber());
                break;
            case PSE:
                details.append("PSE • ").append(method.getBankName())
                       .append(" • ").append(method.getAccountType());
                break;
            default:
                details.append(method.getPaymentMethod().toString());
        }
        
        lbl_paymentMethodDetails.setText(details.toString());
    }
    
    /**
     * Abre la ventana para agregar un nuevo método de pago
     */
    private void openAddPaymentMethodWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/co/edu/uniquindio/sameday/shipmentsuqsameday/interfaces/AddPaymentMethod.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador de la ventana
            AddPaymentMethodViewController controller = loader.getController();
            
            // Crear y configurar la ventana modal
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Método de Pago");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            
            // Mostrar la ventana y esperar a que se cierre
            stage.showAndWait();
            
            // Recargar los métodos de pago si se agregó uno nuevo
            if (controller != null && controller.isMethodAdded()) {
                loadPaymentMethods();
            }
        } catch (Exception ex) {
            showAlert("Error", "No se pudo abrir la ventana para agregar método de pago", 
                    ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Procesa un nuevo pago
     */
    private void processPayment() {
        try {
            // Validar campos obligatorios
            String shipmentId = txt_shipmentId.getText().trim();
            String amountText = txt_amount.getText().trim();
            PaymentMethod paymentMethod = chb_paymentMethod.getValue();
            LocalDate dueDate = dp_dueDate.getValue();
            String reference = txt_reference.getText().trim();
            
            if (shipmentId.isEmpty() || amountText.isEmpty() || paymentMethod == null) {
                showAlert("Error", "Campos incompletos", 
                        "Por favor complete todos los campos obligatorios", Alert.AlertType.WARNING);
                return;
            }
            
            // Convertir monto a número
            double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    showAlert("Error", "Monto inválido", 
                            "El monto debe ser mayor a cero", Alert.AlertType.WARNING);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Monto inválido", 
                        "Por favor ingrese un valor numérico válido", Alert.AlertType.WARNING);
                return;
            }
            
            // Procesar el pago
            boolean success = controller.processPayment(shipmentId, amount, paymentMethod, dueDate, reference);
            
            if (success) {
                // Verificar el estado del envío después del pago
                UUID shipmentUUID = UUID.fromString(shipmentId);
                Optional<Shipment> shipmentOpt = shipmentService.findById(shipmentUUID);
                String message = "El pago se ha procesado correctamente.";
                
                // Si el envío está asignado, informar al usuario
                if (shipmentOpt.isPresent() && shipmentOpt.get().getStatus() == ShipmentStatus.ASSIGNED) {
                    Deliverer deliverer = shipmentOpt.get().getDeliverer();
                    if (deliverer != null) {
                        message += "\n\nSu envío ha sido asignado automáticamente al repartidor " + 
                                deliverer.getName() + ".";
                    } else {
                        message += "\n\nSu envío ha sido asignado y está listo para entrega.";
                    }
                } else {
                    message += "\n\nSu envío está siendo procesado. Pronto será asignado a un repartidor.";
                }
                
                showAlert("Éxito", "Pago procesado", message, Alert.AlertType.INFORMATION);
                clearPaymentForm();
                loadPaymentHistory(); // Recargar historial con el nuevo pago
            } else {
                showAlert("Error", "Error en el pago", 
                        "No se pudo procesar el pago. Verifique los datos e intente nuevamente", 
                        Alert.AlertType.ERROR);
            }
            
        } catch (Exception ex) {
            showAlert("Error", "Error al procesar el pago", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Limpia el formulario de pago
     */
    private void clearPaymentForm() {
        txt_shipmentId.clear();
        txt_amount.clear();
        chb_paymentMethod.setValue(null);
        dp_dueDate.setValue(null);
        txt_reference.clear();
        cmb_savedPaymentMethods.setValue(null);
        lbl_paymentMethodDetails.setText("");
        vbox_paymentMethodInfo.setVisible(false);
    }
    
    /**
     * Filtra pagos según criterios
     */
    private void filterPayments() {
        LocalDate filterDate = dp_filterDate.getValue();
        PaymentStatus filterStatus = chb_filterStatus.getValue();
        
        // Si no hay filtros, mostrar todos los pagos
        if (filterDate == null && filterStatus == null) {
            loadPaymentHistory();
            return;
        }
        
        List<PaymentDTO> filteredPayments = controller.filterPayments(filterDate, filterStatus);
        payments.setAll(filteredPayments);
        
        // Actualizar mensaje de estado
        updateStatusMessage(filteredPayments.size(), filterDate, filterStatus);
    }
    
    /**
     * Actualiza el mensaje de estado con el resultado de los filtros
     */
    private void updateStatusMessage(int count, LocalDate date, PaymentStatus status) {
        StringBuilder message = new StringBuilder();
        message.append("Se encontraron ").append(count).append(" pagos");
        
        if (date != null || status != null) {
            message.append(" con filtros: ");
            
            if (date != null) {
                message.append("Fecha = ").append(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            
            if (date != null && status != null) {
                message.append(", ");
            }
            
            if (status != null) {
                String statusName = "";
                switch (status) {
                    case PENDING: statusName = "Pendiente"; break;
                    case COMPLETED: statusName = "Completado"; break;
                    case FAILED: statusName = "Fallido"; break;
                    case REFUNDED: statusName = "Reembolsado"; break;
                    case CANCELLED: statusName = "Cancelado"; break;
                    default: statusName = status.name();
                }
                message.append("Estado = ").append(statusName);
            }
        }
        
        lbl_status.setText(message.toString());
    }
    
    /**
     * Visualiza el comprobante de un pago
     */
    private void viewReceipt(UUID paymentId) {
        try {
            String receiptPath = controller.getPaymentReceipt(paymentId);
            
            if (receiptPath != null) {
                // Crear un objeto File para el comprobante
                File receiptFile = new File(receiptPath);
                
                // Comprobar que existe
                if (receiptFile.exists()) {
                    // Abrir el archivo HTML en el navegador por defecto usando hostServices
                    App.getAppHostServices().showDocument(receiptFile.toURI().toString());
                    
                    // Mostrar mensaje informativo
                    showAlert("Comprobante", "Comprobante generado", 
                            "El comprobante se ha generado y abierto en su navegador.\nArchivo: " + receiptPath, 
                            Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Comprobante no encontrado", 
                            "El archivo del comprobante no se encuentra en la ubicación esperada", 
                            Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "Comprobante no disponible", 
                        "No se pudo generar el comprobante del pago", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Error al abrir el comprobante", 
                    "No se pudo abrir el archivo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Descarga un reporte de pagos
     */
    private void downloadReport() {
        try {
            String reportPath = controller.generatePaymentReport();
            
            if (reportPath != null) {
                // Crear un objeto File para el reporte
                File reportFile = new File(reportPath);
                
                // Comprobar que existe
                if (reportFile.exists()) {
                    // Abrir el archivo HTML en el navegador por defecto usando hostServices
                    App.getAppHostServices().showDocument(reportFile.toURI().toString());
                    
                    // Mostrar mensaje informativo
                    showAlert("Reporte", "Reporte generado", 
                            "El reporte se ha generado y abierto en su navegador.\nArchivo: " + reportPath, 
                            Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Reporte no encontrado", 
                            "El archivo del reporte no se encuentra en la ubicación esperada", 
                            Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "Reporte no disponible", 
                        "No se pudo generar el reporte de pagos", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Error al abrir el reporte", 
                    "No se pudo abrir el archivo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Muestra una alerta al usuario
     */
    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}