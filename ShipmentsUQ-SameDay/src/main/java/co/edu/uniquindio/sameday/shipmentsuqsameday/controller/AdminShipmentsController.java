package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Incident;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.DelivererService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador para la gestión de envíos por parte del administrador
 * Se encarga de la lógica de negocio relacionada con el manejo
 * de envíos, asignación de repartidores y registro de incidencias
 */
public class AdminShipmentsController implements Initializable {

    // Referencia al View Controller para interacciones con la UI
    private ViewController viewController;

    // Colección para almacenar los envíos
    private ObservableList<Shipment> shipmentsList = FXCollections.observableArrayList();

    // Servicios para acceso a datos
    private ShipmentService shipmentService;
    private DelivererService delivererService;

    /**
     * Inicializa el controlador y configura las colecciones de datos
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Esta inicialización se completa cuando se establece el ViewController
    }

    /**
     * Establece el ViewController asociado y completa la inicialización
     * @param viewController la referencia al ViewController
     */
    public void setViewController(ViewController viewController) {
        this.viewController = viewController;

        // Inicializar servicios
        shipmentService = ShipmentService.getInstance();
        delivererService = DelivererService.getInstance();
        
        // Carga inicial de datos
        loadAllShipments();
    }

    /**
     * Carga todos los envíos desde el servicio
     */
    public void loadAllShipments() {
        try {
            // Limpiar lista actual
            shipmentsList.clear();
            
            // Cargar envíos desde el servicio
            shipmentsList.addAll(shipmentService.findAll());
            
            // Actualizar la UI
            if (viewController != null) {
                viewController.loadTableData(shipmentsList);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar envíos: " + e.getMessage());
            e.printStackTrace();
            
            if (viewController != null) {
                viewController.showStatusMessage("Error al cargar envíos: " + e.getMessage(), "error");
            }
        }
    }

    /**
     * Filtra los envíos según criterios específicos
     * @param statusFilter filtro por estado (TODOS para todos)
     * @param dateFilter filtro por fecha (null para todos)
     */
    public void filterShipments(String statusFilter, LocalDate dateFilter) {
        try {
            // Lista para almacenar los resultados filtrados
            List<Shipment> filteredList = new ArrayList<>();
            
            // Aplicar filtros a la lista completa de envíos
            for (Shipment shipment : shipmentService.findAll()) {
                boolean statusMatch = "TODOS".equals(statusFilter) || 
                                      shipment.getStatus().name().equals(statusFilter);
                                      
                boolean dateMatch = dateFilter == null || 
                                   (shipment.getCreationDate() != null && 
                                    shipment.getCreationDate().toLocalDate().equals(dateFilter));
                
                if (statusMatch && dateMatch) {
                    filteredList.add(shipment);
                }
            }
            
            // Actualizar la lista observable y la UI
            shipmentsList.clear();
            shipmentsList.addAll(filteredList);
            
            if (viewController != null) {
                viewController.loadTableData(shipmentsList);
            }
        } catch (Exception e) {
            System.err.println("Error al filtrar envíos: " + e.getMessage());
            e.printStackTrace();
            
            if (viewController != null) {
                viewController.showStatusMessage("Error al filtrar envíos: " + e.getMessage(), "error");
            }
        }
    }

    /**
     * Muestra un diálogo para asignar un repartidor a un envío
     * @param shipment envío a actualizar
     */
    public void showAssignCourierDialog(Shipment shipment) {
        try {
            System.out.println("\n=== DEBUG: Asignación Manual de Repartidor ===");
            System.out.println("Envío ID: " + shipment.getId());
            System.out.println("Estado actual del envío: " + shipment.getStatus());
            
            // Obtener TODOS los repartidores para debug
            List<Deliverer> allDeliverers = delivererService.findAll();
            System.out.println("Total de repartidores en el sistema: " + allDeliverers.size());
            
            for (Deliverer d : allDeliverers) {
                System.out.println("  - " + d.getName() + 
                                   " | Estado: " + d.getStatus() + 
                                   " | Envíos actuales: " + d.getCurrentShipments().size() +
                                   " | Zona: " + d.getZone());
            }
            
            // Obtener la lista de repartidores disponibles
            List<Deliverer> availableDeliverers = delivererService.getAvailableDeliverers();
            System.out.println("Repartidores disponibles para asignación: " + availableDeliverers.size());
            
            if (availableDeliverers.isEmpty()) {
                System.out.println("❌ No hay repartidores disponibles (estado AVAILABLE)");
                
                // Buscar también repartidores ACTIVE
                List<Deliverer> activeDeliverers = delivererService.findAll().stream()
                    .filter(d -> d.getStatus() == co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus.ACTIVE)
                    .toList();
                
                System.out.println("Repartidores ACTIVE encontrados: " + activeDeliverers.size());
                
                if (!activeDeliverers.isEmpty()) {
                    availableDeliverers = new ArrayList<>(activeDeliverers);
                    System.out.println("✓ Usando repartidores ACTIVE en su lugar");
                } else {
                    if (viewController != null) {
                        viewController.showStatusMessage("No hay repartidores disponibles ni activos en el sistema", "warning");
                    }
                    return;
                }
            }
            
            System.out.println("Mostrando diálogo con " + availableDeliverers.size() + " repartidores");
            
            // Crear el diálogo
            Dialog<Deliverer> dialog = new Dialog<>();
            dialog.setTitle("Asignar Repartidor");
            dialog.setHeaderText("Seleccione un repartidor para el envío");
            
            // Configurar botones
            ButtonType assignButtonType = new ButtonType("Asignar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);
            
            // Crear el ComboBox para seleccionar el repartidor
            ComboBox<Deliverer> courierComboBox = new ComboBox<>();
            courierComboBox.setItems(FXCollections.observableArrayList(availableDeliverers));
            courierComboBox.setPromptText("Seleccione un repartidor");
            
            // Configurar cómo se muestran los elementos en el ComboBox
            courierComboBox.setCellFactory(new Callback<ListView<Deliverer>, ListCell<Deliverer>>() {
                @Override
                public ListCell<Deliverer> call(ListView<Deliverer> param) {
                    return new ListCell<Deliverer>() {
                        @Override
                        protected void updateItem(Deliverer item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null || empty) {
                                setText(null);
                            } else {
                                setText(item.getName() + " - Zona: " + item.getZone());
                            }
                        }
                    };
                }
            });
            
            courierComboBox.setButtonCell(new ListCell<Deliverer>() {
                @Override
                protected void updateItem(Deliverer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.getName() + " - Zona: " + item.getZone());
                    }
                }
            });
            
            // Crear la interfaz
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            grid.add(new Label("Repartidor:"), 0, 0);
            grid.add(courierComboBox, 1, 0);
            
            dialog.getDialogPane().setContent(grid);
            
            // Convertir el resultado al presionar OK
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == assignButtonType) {
                    return courierComboBox.getValue();
                }
                return null;
            });
            
            // Mostrar el diálogo y procesar el resultado
            Optional<Deliverer> result = dialog.showAndWait();
            result.ifPresent(deliverer -> assignCourierToShipment(shipment, deliverer));
            
        } catch (Exception e) {
            System.err.println("Error al mostrar diálogo de asignación: " + e.getMessage());
            e.printStackTrace();
            
            if (viewController != null) {
                viewController.showStatusMessage("Error al mostrar diálogo de asignación: " + e.getMessage(), "error");
            }
        }
    }

    /**
     * Asigna un repartidor a un envío
     * @param shipment envío a actualizar
     * @param deliverer repartidor a asignar
     */
    private void assignCourierToShipment(Shipment shipment, Deliverer deliverer) {
        try {
            // Asignar el repartidor al envío
            shipment.setDeliverer(deliverer);
            
            // Actualizar el estado del envío
            shipment.setStatus(ShipmentStatus.ASSIGNED);
            
            // Registrar la fecha de asignación
            shipment.setAssignmentDate(LocalDateTime.now());
            
            // Agregar el envío a la lista de envíos actuales del repartidor
            deliverer.getCurrentShipments().add(shipment);
            
            // Guardar los cambios
            shipmentService.update(shipment);
            delivererService.update(deliverer);
            
            // Actualizar la UI
            if (viewController != null) {
                viewController.updateShipmentInTable(shipment);
                viewController.showStatusMessage("Repartidor asignado correctamente", "success");
            }
        } catch (Exception e) {
            System.err.println("Error al asignar repartidor: " + e.getMessage());
            e.printStackTrace();
            
            if (viewController != null) {
                viewController.showStatusMessage("Error al asignar repartidor: " + e.getMessage(), "error");
            }
        }
    }

    /**
     * Muestra un diálogo para actualizar el estado de un envío
     * @param shipment envío a actualizar
     */
    public void showUpdateStatusDialog(Shipment shipment) {
        try {
            // Crear el diálogo
            Dialog<ShipmentStatus> dialog = new Dialog<>();
            dialog.setTitle("Actualizar Estado");
            dialog.setHeaderText("Seleccione el nuevo estado para el envío");
            
            // Configurar botones
            ButtonType updateButtonType = new ButtonType("Actualizar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
            
            // Crear el ComboBox para seleccionar el estado
            ComboBox<ShipmentStatus> statusComboBox = new ComboBox<>();
            
            // Determinar qué estados pueden seguir al estado actual
            List<ShipmentStatus> availableStatuses = new ArrayList<>();
            switch (shipment.getStatus()) {
                case PENDING:
                    availableStatuses.add(ShipmentStatus.ASSIGNED);
                    availableStatuses.add(ShipmentStatus.CANCELLED);
                    break;
                case ASSIGNED:
                    availableStatuses.add(ShipmentStatus.IN_TRANSIT);
                    availableStatuses.add(ShipmentStatus.CANCELLED);
                    break;
                case IN_TRANSIT:
                    availableStatuses.add(ShipmentStatus.DELIVERED);
                    availableStatuses.add(ShipmentStatus.INCIDENT);
                    break;
                case INCIDENT:
                    availableStatuses.add(ShipmentStatus.PENDING_REASSIGNMENT);
                    availableStatuses.add(ShipmentStatus.CANCELLED);
                    break;
                case PENDING_REASSIGNMENT:
                    availableStatuses.add(ShipmentStatus.ASSIGNED);
                    availableStatuses.add(ShipmentStatus.CANCELLED);
                    break;
                default:
                    // No hay estados disponibles para DELIVERED o CANCELLED
                    if (viewController != null) {
                        viewController.showStatusMessage("Este envío no puede cambiar de estado", "warning");
                    }
                    return;
            }
            
            statusComboBox.setItems(FXCollections.observableArrayList(availableStatuses));
            statusComboBox.setPromptText("Seleccione un estado");
            
            // Crear la interfaz
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            grid.add(new Label("Estado actual:"), 0, 0);
            grid.add(new Label(shipment.getStatus().name()), 1, 0);
            grid.add(new Label("Nuevo estado:"), 0, 1);
            grid.add(statusComboBox, 1, 1);
            
            dialog.getDialogPane().setContent(grid);
            
            // Convertir el resultado al presionar OK
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == updateButtonType) {
                    return statusComboBox.getValue();
                }
                return null;
            });
            
            // Mostrar el diálogo y procesar el resultado
            Optional<ShipmentStatus> result = dialog.showAndWait();
            result.ifPresent(status -> updateShipmentStatus(shipment, status));
            
        } catch (Exception e) {
            System.err.println("Error al mostrar diálogo de actualización de estado: " + e.getMessage());
            e.printStackTrace();
            
            if (viewController != null) {
                viewController.showStatusMessage("Error al mostrar diálogo de actualización: " + e.getMessage(), "error");
            }
        }
    }

    /**
     * Actualiza el estado de un envío
     * @param shipment envío a actualizar
     * @param newStatus nuevo estado
     */
    private void updateShipmentStatus(Shipment shipment, ShipmentStatus newStatus) {
        try {
            // Guardar el estado anterior
            ShipmentStatus previousStatus = shipment.getStatus();
            
            // Actualizar el estado del envío
            shipment.setStatus(newStatus);
            
            // Manejar cambios específicos según el estado
            if (newStatus == ShipmentStatus.DELIVERED) {
                // Registrar la fecha de entrega
                shipment.setDeliveryDate(LocalDateTime.now());
                
                // Si tiene repartidor asignado, mover de envíos actuales a históricos
                if (shipment.getDeliverer() != null) {
                    Deliverer deliverer = shipment.getDeliverer();
                    deliverer.getCurrentShipments().remove(shipment);
                    deliverer.getShipmentHistory().add(shipment);
                    deliverer.setTotalDeliveries(deliverer.getTotalDeliveries() + 1);
                    
                    // Actualizar el repartidor
                    delivererService.update(deliverer);
                }
            } else if (newStatus == ShipmentStatus.CANCELLED && previousStatus != ShipmentStatus.CANCELLED) {
                // Si se cancela y tiene repartidor, quitarlo de sus envíos actuales
                if (shipment.getDeliverer() != null) {
                    Deliverer deliverer = shipment.getDeliverer();
                    deliverer.getCurrentShipments().remove(shipment);
                    
                    // Actualizar el repartidor
                    delivererService.update(deliverer);
                }
            }
            
            // Guardar los cambios
            shipmentService.update(shipment);
            
            // Actualizar la UI
            if (viewController != null) {
                viewController.updateShipmentInTable(shipment);
                viewController.showStatusMessage("Estado actualizado correctamente", "success");
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar estado: " + e.getMessage());
            e.printStackTrace();
            
            if (viewController != null) {
                viewController.showStatusMessage("Error al actualizar estado: " + e.getMessage(), "error");
            }
        }
    }

    /**
     * Muestra un diálogo para registrar una incidencia en un envío
     * @param shipment envío afectado
     */
    public void showAddIncidentDialog(Shipment shipment) {
        try {
            // Crear el diálogo
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Registrar Incidencia");
            dialog.setHeaderText("Registre una incidencia para el envío");
            
            // Configurar botones
            ButtonType registerButtonType = new ButtonType("Registrar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);
            
            // Crear el campo de texto para la descripción
            TextArea descriptionArea = new TextArea();
            descriptionArea.setPromptText("Describa la incidencia...");
            descriptionArea.setPrefRowCount(5);
            
            // Crear la interfaz
            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(
                new Label("ID Envío: " + shipment.getId().toString().substring(0, 8) + "..."),
                new Label("Estado actual: " + shipment.getStatus().name()),
                new Label("Descripción de la incidencia:"),
                descriptionArea
            );
            vbox.setPadding(new Insets(20));
            
            dialog.getDialogPane().setContent(vbox);
            
            // Convertir el resultado al presionar OK
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == registerButtonType) {
                    return descriptionArea.getText();
                }
                return null;
            });
            
            // Mostrar el diálogo y procesar el resultado
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(description -> {
                if (description.trim().isEmpty()) {
                    if (viewController != null) {
                        viewController.showStatusMessage("La descripción no puede estar vacía", "warning");
                    }
                } else {
                    registerIncident(shipment, description);
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error al mostrar diálogo de incidencia: " + e.getMessage());
            e.printStackTrace();
            
            if (viewController != null) {
                viewController.showStatusMessage("Error al mostrar diálogo de incidencia: " + e.getMessage(), "error");
            }
        }
    }

    /**
     * Registra una incidencia en un envío
     * @param shipment envío afectado
     * @param description descripción de la incidencia
     */
    private void registerIncident(Shipment shipment, String description) {
        try {
            // Crear la incidencia
            Incident incident = Incident.builder()
                .id(UUID.randomUUID())
                .shipment(shipment)
                .description(description)
                .date(LocalDateTime.now())
                .build();
            
            // Agregar la incidencia al envío
            shipment.getIncidents().add(incident);
            
            // Guardar los cambios
            shipmentService.update(shipment);
            
            // Actualizar la UI
            if (viewController != null) {
                viewController.updateShipmentInTable(shipment);
                viewController.showStatusMessage("Incidencia registrada correctamente", "success");
            }
        } catch (Exception e) {
            System.err.println("Error al registrar incidencia: " + e.getMessage());
            e.printStackTrace();
            
            if (viewController != null) {
                viewController.showStatusMessage("Error al registrar incidencia: " + e.getMessage(), "error");
            }
        }
    }

    /**
     * Elimina un envío
     * @param shipment envío a eliminar
     */
    public void deleteShipment(Shipment shipment) {
        try {
            // Verificar que el envío esté en estado PENDING
            if (shipment.getStatus() != ShipmentStatus.PENDING) {
                if (viewController != null) {
                    viewController.showStatusMessage("Solo se pueden eliminar envíos en estado PENDING", "warning");
                }
                return;
            }
            
            // Eliminar el envío del servicio
            shipmentService.delete(shipment.getId());
            
            // Actualizar la UI
            if (viewController != null) {
                viewController.removeShipmentFromTable(shipment);
            }
        } catch (Exception e) {
            System.err.println("Error al eliminar envío: " + e.getMessage());
            e.printStackTrace();
            
            if (viewController != null) {
                viewController.showStatusMessage("Error al eliminar envío: " + e.getMessage(), "error");
            }
        }
    }

    /**
     * Interfaz para la comunicación con el ViewController
     */
    public interface ViewController {
        void loadTableData(ObservableList<Shipment> shipments);
        void updateShipmentInTable(Shipment shipment);
        void removeShipmentFromTable(Shipment shipment);
        void showStatusMessage(String message, String messageType);
    }
}