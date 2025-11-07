package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AddPaymentMethodController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AddPaymentMethodController.CardInfo;
import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AddPaymentMethodController.MobileInfo;
import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AddPaymentMethodController.PaymentMethodInfo;
import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AddPaymentMethodController.PseInfo;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Controlador de vista para agregar un método de pago
 */
public class AddPaymentMethodViewController implements Initializable {

    @FXML private ChoiceBox<PaymentMethod> chb_paymentMethodType;
    @FXML private TextField txt_alias;
    @FXML private CheckBox chk_isDefault;
    
    // Sección de tarjetas
    @FXML private TitledPane tp_cardSection;
    @FXML private TextField txt_cardNumber;
    @FXML private ChoiceBox<String> chb_cardType;
    @FXML private TextField txt_cardExpiry;
    @FXML private PasswordField txt_cardCvv;
    
    // Sección de pago móvil
    @FXML private TitledPane tp_mobileSection;
    @FXML private TextField txt_phoneNumber;
    @FXML private ChoiceBox<String> chb_mobilePaymentPlatform;
    
    // Sección de PSE
    @FXML private TitledPane tp_pseSection;
    @FXML private ComboBox<String> cmb_bank;
    @FXML private ChoiceBox<String> chb_accountType;
    
    // Botones
    @FXML private Button btn_save;
    @FXML private Button btn_cancel;
    
    // Controlador de negocio
    private AddPaymentMethodController controller;
    
    // Indicador si se añadió un método de pago correctamente
    private boolean methodAdded = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar controlador y componentes
        initController();
        setupUIControls();
        setupEventHandlers();
    }
    
    /**
     * Inicializa el controlador de negocio
     */
    private void initController() {
        controller = new AddPaymentMethodController();
    }
    
    /**
     * Configura los controles de la UI
     */
    private void setupUIControls() {
        // Configurar selector de tipo de método de pago
        chb_paymentMethodType.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        chb_paymentMethodType.setConverter(new StringConverter<PaymentMethod>() {
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
        
        // Configurar tipos de tarjeta
        chb_cardType.setItems(FXCollections.observableArrayList(
                "Visa", "Mastercard", "American Express", "Diners Club", "Otra"));
        
        // Configurar plataformas de pago móvil (aunque realmente solo son dos)
        chb_mobilePaymentPlatform.setItems(FXCollections.observableArrayList(
                "Nequi", "Daviplata"));
        
        // Configurar tipos de cuenta
        chb_accountType.setItems(FXCollections.observableArrayList(
                "Ahorros", "Corriente"));
        
        // Configurar bancos (lista simplificada)
        cmb_bank.setItems(FXCollections.observableArrayList(Arrays.asList(
                "Bancolombia", "Banco de Bogotá", "Davivienda", 
                "BBVA Colombia", "Banco de Occidente", "Banco Popular",
                "Banco AV Villas", "Banco Caja Social", "Colpatria"
        )));
    }
    
    /**
     * Configura los manejadores de eventos
     */
    private void setupEventHandlers() {
        // Cambiar las secciones visibles según el tipo de método seleccionado
        chb_paymentMethodType.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateSectionVisibility(newVal);
            }
        });
        
        // Botón cancelar
        btn_cancel.setOnAction(e -> closeWindow());
        
        // Botón guardar
        btn_save.setOnAction(e -> savePaymentMethod());
    }
    
    /**
     * Actualiza la visibilidad de las secciones según el método seleccionado
     */
    private void updateSectionVisibility(PaymentMethod method) {
        // Ocultar todas las secciones primero
        tp_cardSection.setExpanded(false);
        tp_mobileSection.setExpanded(false);
        tp_pseSection.setExpanded(false);
        
        // Mostrar la sección correspondiente
        switch (method) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                tp_cardSection.setExpanded(true);
                break;
                
            case NEQUI:
            case DAVIPLATA:
                tp_mobileSection.setExpanded(true);
                // Preseleccionar la plataforma correspondiente
                chb_mobilePaymentPlatform.setValue(method == PaymentMethod.NEQUI ? "Nequi" : "Daviplata");
                break;
                
            case PSE:
                tp_pseSection.setExpanded(true);
                break;
                
            default:
                // Para CASH no mostramos secciones adicionales
        }
    }
    
    /**
     * Guarda el nuevo método de pago
     */
    private void savePaymentMethod() {
        try {
            // Validar campos obligatorios
            PaymentMethod methodType = chb_paymentMethodType.getValue();
            String alias = txt_alias.getText().trim();
            boolean isDefault = chk_isDefault.isSelected();
            
            if (methodType == null || alias.isEmpty()) {
                showAlert("Error", "Campos incompletos", 
                        "Por favor seleccione el tipo de método de pago y proporcione un alias", 
                        Alert.AlertType.WARNING);
                return;
            }
            
            // Crear la información adicional según el tipo de método
            PaymentMethodInfo additionalInfo = null;
            
            switch (methodType) {
                case CREDIT_CARD:
                case DEBIT_CARD:
                    // Validar campos de tarjeta
                    String cardNumber = txt_cardNumber.getText().trim();
                    String cardType = chb_cardType.getValue();
                    String expiry = txt_cardExpiry.getText().trim();
                    String cvv = txt_cardCvv.getText().trim();
                    
                    if (cardNumber.isEmpty() || cardType == null || expiry.isEmpty() || cvv.isEmpty()) {
                        showAlert("Error", "Campos incompletos", 
                                "Por favor complete todos los campos de la tarjeta", 
                                Alert.AlertType.WARNING);
                        return;
                    }
                    
                    additionalInfo = new CardInfo(cardNumber, cardType, expiry, cvv);
                    break;
                    
                case NEQUI:
                case DAVIPLATA:
                    // Validar campos de pago móvil
                    String phoneNumber = txt_phoneNumber.getText().trim();
                    String platform = chb_mobilePaymentPlatform.getValue();
                    
                    if (phoneNumber.isEmpty() || platform == null) {
                        showAlert("Error", "Campos incompletos", 
                                "Por favor complete todos los campos del pago móvil", 
                                Alert.AlertType.WARNING);
                        return;
                    }
                    
                    additionalInfo = new MobileInfo(phoneNumber, platform);
                    break;
                    
                case PSE:
                    // Validar campos de PSE
                    String bank = cmb_bank.getValue();
                    String accountType = chb_accountType.getValue();
                    
                    if (bank == null || accountType == null) {
                        showAlert("Error", "Campos incompletos", 
                                "Por favor seleccione el banco y tipo de cuenta", 
                                Alert.AlertType.WARNING);
                        return;
                    }
                    
                    additionalInfo = new PseInfo(bank, accountType);
                    break;
                    
                default:
                    // Para CASH no se requiere info adicional
            }
            
            // Guardar el método de pago
            boolean success = controller.addPaymentMethod(methodType, alias, isDefault, additionalInfo);
            
            if (success) {
                methodAdded = true;
                showAlert("Éxito", "Método de pago agregado", 
                        "El método de pago se ha agregado correctamente", 
                        Alert.AlertType.INFORMATION);
                closeWindow();
            } else {
                showAlert("Error", "Error al guardar", 
                        "No se pudo guardar el método de pago", 
                        Alert.AlertType.ERROR);
            }
            
        } catch (Exception ex) {
            showAlert("Error", "Error al guardar", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Cierra la ventana actual
     */
    private void closeWindow() {
        Stage stage = (Stage) btn_cancel.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Verifica si se agregó un método de pago
     * @return true si se agregó correctamente
     */
    public boolean isMethodAdded() {
        return methodAdded;
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