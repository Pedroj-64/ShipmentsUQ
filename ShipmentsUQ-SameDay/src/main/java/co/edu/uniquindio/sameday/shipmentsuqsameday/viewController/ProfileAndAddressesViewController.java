package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.ProfileAndAddressesController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.AddressDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador de vista para la pantalla de gestión de perfil y direcciones
 */
public class ProfileAndAddressesViewController implements Initializable {
    
    @FXML
    private TextField txt_name;
    
    @FXML
    private TextField txt_email;
    
    @FXML
    private TextField txt_phone;
    
    @FXML
    private Button btn_saveProfile;
    
    @FXML
    private Button btn_cancelProfile;
    
    @FXML
    private TableView<AddressDTO> tbl_address;
    
    @FXML
    private TableColumn<AddressDTO, String> col_alias;
    
    @FXML
    private TableColumn<AddressDTO, String> col_street;
    
    @FXML
    private TableColumn<AddressDTO, String> col_city;
    
    @FXML
    private TableColumn<AddressDTO, String> col_coordinates;
    
    @FXML
    private Button btn_addAddress;
    
    @FXML
    private Button btn_editAddress;
    
    @FXML
    private Button btn_deleteAddress;
    
    @FXML
    private Label lbl_status;
    
    // Controlador de negocio
    private ProfileAndAddressesController controller;
    
    // Datos
    private ObservableList<AddressDTO> addressesData;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        controller = new ProfileAndAddressesController();
        addressesData = FXCollections.observableArrayList();
        
        // Configurar la tabla de direcciones
        setupAddressTable();
        
        // Configurar botones
        setupButtons();
        
        // Cargar datos iniciales
        loadUserProfile();
        loadUserAddresses();
    }
    
    /**
     * Configura la tabla de direcciones
     */
    private void setupAddressTable() {
        // Configurar columnas
        col_alias.setCellValueFactory(new PropertyValueFactory<>("alias"));
        col_street.setCellValueFactory(new PropertyValueFactory<>("street"));
        col_city.setCellValueFactory(new PropertyValueFactory<>("city"));
        
        // Configurar columna de coordenadas (concatenando coordX y coordY)
        col_coordinates.setCellValueFactory(cellData -> {
            double coordX = cellData.getValue().getCoordX();
            double coordY = cellData.getValue().getCoordY();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> String.format("%.2f, %.2f", coordX, coordY)
            );
        });
        
        // Vincular datos a la tabla
        tbl_address.setItems(addressesData);
    }
    
    /**
     * Configura los listeners para los botones
     */
    private void setupButtons() {
        // Botones de perfil
        btn_saveProfile.setOnAction(event -> handleSaveProfile());
        btn_cancelProfile.setOnAction(event -> handleCancelProfile());
        
        // Botones de direcciones
        btn_addAddress.setOnAction(event -> handleAddAddress());
        btn_editAddress.setOnAction(event -> handleEditAddress());
        btn_deleteAddress.setOnAction(event -> handleDeleteAddress());
    }
    
    /**
     * Carga los datos del perfil del usuario
     */
    private void loadUserProfile() {
        try {
            UserDTO user = controller.getCurrentUserData();
            
            // Establecer datos en campos
            txt_name.setText(user.getName());
            txt_email.setText(user.getEmail());
            txt_phone.setText(user.getPhone());
            
        } catch (Exception e) {
            showErrorMessage("Error al cargar el perfil: " + e.getMessage());
        }
    }
    
    /**
     * Carga las direcciones del usuario
     */
    public void loadUserAddresses() {
        try {
            List<AddressDTO> addresses = controller.getUserAddresses();
            
            // Actualizar tabla
            addressesData.clear();
            addressesData.addAll(addresses);
            
        } catch (Exception e) {
            showErrorMessage("Error al cargar las direcciones: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el evento de guardar perfil
     */
    private void handleSaveProfile() {
        try {
            String name = txt_name.getText();
            String email = txt_email.getText();
            String phone = txt_phone.getText();
            
            // Validaciones básicas
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                showErrorMessage("Todos los campos del perfil son obligatorios");
                return;
            }
            
            // Actualizar perfil
            boolean success = controller.updateUserProfile(name, email, phone);
            
            if (success) {
                showSuccessMessage("Perfil actualizado correctamente");
            } else {
                showErrorMessage("No se pudo actualizar el perfil");
            }
            
        } catch (Exception e) {
            showErrorMessage("Error al guardar el perfil: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el evento de cancelar cambios en el perfil
     */
    private void handleCancelProfile() {
        // Recargar datos originales
        loadUserProfile();
    }
    
    /**
     * Maneja el evento de agregar dirección
     */
    private void handleAddAddress() {
        try {
            // Abrir la ventana de formulario de dirección
            openAddressFormDialog(null);
        } catch (Exception e) {
            showErrorMessage("Error al abrir formulario de dirección: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Maneja el evento de editar dirección
     */
    private void handleEditAddress() {
        try {
            AddressDTO selected = tbl_address.getSelectionModel().getSelectedItem();
            
            if (selected == null) {
                showErrorMessage("Seleccione una dirección para editar");
                return;
            }
            
            // Obtener la dirección completa del controlador por su ID
            Address addressToEdit = controller.getAddressById(selected.getId());
            
            if (addressToEdit == null) {
                showErrorMessage("No se pudo encontrar la dirección seleccionada");
                return;
            }
            
            // Abrir el formulario de edición con la dirección seleccionada
            openAddressFormDialog(addressToEdit);
            
        } catch (Exception e) {
            showErrorMessage("Error al editar dirección: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Maneja el evento de eliminar dirección
     */
    private void handleDeleteAddress() {
        try {
            AddressDTO selected = tbl_address.getSelectionModel().getSelectedItem();
            
            if (selected == null) {
                showErrorMessage("Seleccione una dirección para eliminar");
                return;
            }
            
            // Confirmar eliminación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Está seguro de eliminar esta dirección?");
            alert.setContentText("Esta acción no se puede deshacer");
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                boolean success = controller.deleteAddress(selected.getId());
                
                if (success) {
                    showSuccessMessage("Dirección eliminada correctamente");
                    loadUserAddresses(); // Recargar direcciones
                } else {
                    showErrorMessage("No se pudo eliminar la dirección");
                }
            }
            
        } catch (Exception e) {
            showErrorMessage("Error al eliminar dirección: " + e.getMessage());
        }
    }
    
    /**
     * Muestra un mensaje de éxito en la etiqueta de estado
     * @param message Mensaje a mostrar
     */
    private void showSuccessMessage(String message) {
        lbl_status.setText(message);
        lbl_status.setStyle("-fx-text-fill: green;");
    }
    
    /**
     * Muestra un mensaje de error en la etiqueta de estado
     * @param message Mensaje a mostrar
     */
    private void showErrorMessage(String message) {
        lbl_status.setText(message);
        lbl_status.setStyle("-fx-text-fill: red;");
    }
    
    /**
     * Abre el formulario de direcciones como un diálogo modal
     * @param address Dirección a editar o null para crear una nueva
     * @throws IOException Si ocurre un error al cargar la interfaz
     */
    private void openAddressFormDialog(Address address) throws IOException {
        try {
            // Cargar la interfaz del formulario de direcciones
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/sameday/shipmentsuqsameday/interfaces/AddressForm.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador y configurarlo
            AddressFormViewController controller = loader.getController();
            controller.setParentController(this); // Para que pueda actualizar la tabla al guardar
            
            // Si se está editando una dirección, cargarla en el formulario
            if (address != null) {
                controller.loadAddressForEdit(address);
            }
            
            // Crear escena con dimensiones específicas
            Scene scene = new Scene(root, 900, 550);
            
            // Crear y configurar el diálogo
            Stage dialogStage = new Stage();
            dialogStage.setTitle(address == null ? "Nueva Dirección" : "Editar Dirección");
            dialogStage.initModality(Modality.APPLICATION_MODAL); // Bloquea interacción con ventana principal
            dialogStage.initStyle(StageStyle.UTILITY); // Estilo simplificado
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            
            // Establecer tamaño mínimo y máximo para evitar problemas
            dialogStage.setMinWidth(900);
            dialogStage.setMinHeight(550);
            dialogStage.setMaxWidth(900);
            dialogStage.setMaxHeight(550);
            
            // Centrar el diálogo en la pantalla
            dialogStage.centerOnScreen();
            
            // Mostrar el diálogo y esperar a que se cierre
            dialogStage.showAndWait();
            
            // La tabla se actualiza desde el controlador del formulario al guardar exitosamente
            
        } catch (IOException e) {
            showErrorMessage("Error al abrir el formulario: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            showErrorMessage("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}