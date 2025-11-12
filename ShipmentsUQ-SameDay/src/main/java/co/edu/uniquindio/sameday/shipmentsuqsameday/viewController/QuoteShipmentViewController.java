package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.QuoteShipmentController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.AddressDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.ShipmentDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentPriority;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de vista para la pantalla de cotización y creación de envíos.
 * Gestiona la interfaz QuoteShipment.fxml.
 */
public class QuoteShipmentViewController implements Initializable {
    
    @FXML private ComboBox<AddressDTO> cmb_origin;
    @FXML private ComboBox<AddressDTO> cmb_destination;
    @FXML private TextField txt_weight;
    @FXML private TextField txt_dimensions;
    @FXML private ChoiceBox<ShipmentPriority> chb_priority;
    @FXML private Label lbl_quoteResult;
    @FXML private Label lbl_status;
    @FXML private Button btn_calculate;
    @FXML private Button btn_confirm;
    
    private QuoteShipmentController controller;
    private Double calculatedRate = null;
    private ShipmentDTO quotedShipment = null;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Inicializar el controlador de negocio
            controller = new QuoteShipmentController();
            
            // Configurar componentes
            setupComboBoxes();
            setupPriorityChoiceBox();
            setupButtons();
            setupValidation();
            
            // Cargar datos iniciales
            loadUserAddresses();
            
        } catch (Exception e) {
            showErrorMessage("Error al inicializar: " + e.getMessage());
        }
    }
    
    /**
     * Configura los comboboxes para mostrar las direcciones correctamente
     */
    private void setupComboBoxes() {
        // Conversor para mostrar un texto personalizado para cada AddressDTO
        cmb_origin.setConverter(new AppUtils.AddressDTOStringConverter());
        cmb_destination.setConverter(new AppUtils.AddressDTOStringConverter());
        
        // Deshabilitar el origen ya que será la dirección predeterminada
        cmb_origin.setDisable(true);
    }
    
    /**
     * Configura el ChoiceBox de prioridad
     */
    private void setupPriorityChoiceBox() {
        chb_priority.getItems().addAll(ShipmentPriority.values());
        chb_priority.setValue(ShipmentPriority.STANDARD); // Valor predeterminado
    }
    
    /**
     * Configura los botones y sus acciones
     */
    private void setupButtons() {
        btn_calculate.setOnAction(event -> calculateRate());
        btn_confirm.setOnAction(event -> createShipment());
        
        // El botón confirmar está deshabilitado hasta que se calcule la tarifa
        btn_confirm.setDisable(true);
    }
    
    /**
     * Configura la validación de los campos en tiempo real
     */
    private void setupValidation() {
        // Validación para aceptar solo números y punto decimal en el peso
        txt_weight.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txt_weight.setText(oldValue);
            }
            resetQuoteResult();
        });
        
        // Listener para el destino seleccionado
        cmb_destination.valueProperty().addListener((observable, oldValue, newValue) -> {
            resetQuoteResult();
        });
        
        // Listener para la prioridad seleccionada
        chb_priority.valueProperty().addListener((observable, oldValue, newValue) -> {
            resetQuoteResult();
        });
    }
    
    /**
     * Carga las direcciones del usuario actual en los comboboxes
     */
    private void loadUserAddresses() {
        try {
            // Limpiar comboboxes
            cmb_origin.getItems().clear();
            cmb_destination.getItems().clear();
            
            // Cargar direcciones
            var addresses = controller.getUserAddresses();
            if (addresses == null || addresses.isEmpty()) {
                showInfoMessage("No tiene direcciones guardadas. Por favor, añada direcciones desde su perfil.");
                return;
            }
            
            // Encontrar la dirección predeterminada para el origen
            AddressDTO defaultAddress = addresses.stream()
                    .filter(addr -> controller.isDefaultAddress(addr))
                    .findFirst()
                    .orElse(null);
            
            if (defaultAddress == null) {
                showInfoMessage("No tiene una dirección predeterminada. Se utilizará la primera dirección disponible.");
                defaultAddress = addresses.get(0); // Usar la primera como fallback
            }
            
            // Establecer dirección de origen
            cmb_origin.getItems().add(defaultAddress);
            cmb_origin.setValue(defaultAddress);
            
            // Agregar todas las direcciones como posibles destinos
            cmb_destination.getItems().addAll(addresses);
            
        } catch (Exception e) {
            showErrorMessage("Error al cargar direcciones: " + e.getMessage());
        }
    }
    
    /**
     * Calcula la tarifa del envío según los datos ingresados
     */
    private void calculateRate() {
        try {
            // Validar datos
            if (!validateForm()) {
                return;
            }
            
            // Obtener datos del formulario
            AddressDTO origin = cmb_origin.getValue();
            AddressDTO destination = cmb_destination.getValue();
            double weight = Double.parseDouble(txt_weight.getText());
            String dimensions = txt_dimensions.getText();
            ShipmentPriority priority = chb_priority.getValue();
            
            // Calcular tarifa
            calculatedRate = controller.calculateShipmentRate(origin, destination, weight, dimensions, priority);
            
            // Crear DTO del envío para guardarlo después
            quotedShipment = ShipmentDTO.builder()
                    .originAddress(origin)
                    .destinationAddress(destination)
                    .weight(weight)
                    .dimensions(dimensions)
                    .priority(priority)
                    .estimatedCost(calculatedRate)
                    .build();
            
            // Mostrar resultado
            lbl_quoteResult.setText(String.format("Costo estimado: $%.2f en pesos", calculatedRate));
            lbl_status.setText("Cotización calculada exitosamente");
            
            // Habilitar botón de confirmación
            btn_confirm.setDisable(false);
            
        } catch (NumberFormatException e) {
            showErrorMessage("El peso debe ser un número válido");
        } catch (Exception e) {
            showErrorMessage("Error al calcular tarifa: " + e.getMessage());
        }
    }
    
    /**
     * Crea el envío después de confirmar la cotización
     */
    private void createShipment() {
        try {
            // Verificar que se haya calculado una cotización
            if (calculatedRate == null || quotedShipment == null) {
                showErrorMessage("Debe calcular una cotización primero");
                return;
            }
            
            // Crear el envío
            @SuppressWarnings("unused")
            Shipment shipment = controller.createShipment(quotedShipment);
            
            // Mostrar mensaje de éxito
            showSuccessMessage("Envío creado exitosamente. Se ha asignado al repartidor más cercano.");
            
            // Cerrar ventana después de un tiempo
            closeWindowAfterDelay(2000);
            
        } catch (Exception e) {
            showErrorMessage("Error al crear envío: " + e.getMessage());
        }
    }
    
    /**
     * Valida que todos los campos requeridos estén completos
     * @return true si el formulario es válido, false en caso contrario
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        if (cmb_origin.getValue() == null) {
            errors.append("Debe seleccionar un origen\n");
        }
        
        if (cmb_destination.getValue() == null) {
            errors.append("Debe seleccionar un destino\n");
        } else if (cmb_destination.getValue().equals(cmb_origin.getValue())) {
            errors.append("El destino no puede ser igual al origen\n");
        }
        
        if (txt_weight.getText().isEmpty()) {
            errors.append("Debe ingresar el peso\n");
        } else {
            try {
                double weight = Double.parseDouble(txt_weight.getText());
                if (weight <= 0) {
                    errors.append("El peso debe ser mayor a 0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("El peso debe ser un número válido\n");
            }
        }
        
        if (chb_priority.getValue() == null) {
            errors.append("Debe seleccionar una prioridad\n");
        }
        
        if (errors.length() > 0) {
            showErrorMessage(errors.toString());
            return false;
        }
        
        return true;
    }
    
    /**
     * Resetea el resultado de la cotización cuando cambian los datos
     */
    private void resetQuoteResult() {
        lbl_quoteResult.setText("Complete el formulario y presione Calcular");
        btn_confirm.setDisable(true);
        calculatedRate = null;
        quotedShipment = null;
    }
    
    /**
     * Muestra un mensaje de error
     * @param message el mensaje a mostrar
     */
    private void showErrorMessage(String message) {
        lbl_status.setText(message);
        lbl_status.setStyle("-fx-text-fill: red;");
    }
    
    /**
     * Muestra un mensaje informativo
     * @param message el mensaje a mostrar
     */
    private void showInfoMessage(String message) {
        lbl_status.setText(message);
        lbl_status.setStyle("-fx-text-fill: blue;");
    }
    
    /**
     * Muestra un mensaje de éxito
     * @param message el mensaje a mostrar
     */
    private void showSuccessMessage(String message) {
        lbl_status.setText(message);
        lbl_status.setStyle("-fx-text-fill: green;");
    }
    
    /**
     * Cierra la ventana después de un tiempo de espera
     * @param milliseconds tiempo en milisegundos
     */
    private void closeWindowAfterDelay(int milliseconds) {
        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> {
                        Stage stage = (Stage) btn_confirm.getScene().getWindow();
                        stage.close();
                    });
                }
            }, 
            milliseconds
        );
    }
    
    /**
     * Configura el modo de edición para modificar un envío existente
     * @param shipment el envío a editar
     */
    public void setEditMode(ShipmentDTO shipment) {
        if (shipment == null) {
            return;
        }
        
        try {
            // Cambiar título del botón
            btn_confirm.setText("Actualizar Envío");
            
            // Llenar campos con los datos del envío
            // Origen
            if (shipment.getOriginAddress() != null) {
                for (AddressDTO addr : cmb_origin.getItems()) {
                    if (addr.getId().equals(shipment.getOriginAddress().getId())) {
                        cmb_origin.setValue(addr);
                        break;
                    }
                }
            }
            
            // Destino
            if (shipment.getDestinationAddress() != null) {
                for (AddressDTO addr : cmb_destination.getItems()) {
                    if (addr.getId().equals(shipment.getDestinationAddress().getId())) {
                        cmb_destination.setValue(addr);
                        break;
                    }
                }
            }
            
            // Peso
            txt_weight.setText(String.valueOf(shipment.getWeight()));
            
            // Dimensiones
            txt_dimensions.setText(shipment.getDimensions());
            
            // Prioridad
            chb_priority.setValue(shipment.getPriority());
            
            // Precalcular la cotización
            calculatedRate = shipment.getCost();
            quotedShipment = shipment;
            
            // Mostrar resultado
            lbl_quoteResult.setText(String.format("Costo estimado: $%.2f", calculatedRate));
            
            // Habilitar botón de confirmación
            btn_confirm.setDisable(false);
            
            // Mostrar mensaje informativo
            showInfoMessage("Editando envío existente. Modifique los campos y confirme los cambios.");
            
        } catch (Exception e) {
            showErrorMessage("Error al cargar datos del envío: " + e.getMessage());
        }
    }
}