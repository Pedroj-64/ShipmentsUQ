package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Payment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.ShipmentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.DelivererRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.PaymentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.UserRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.PaymentAnalyticsService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.DataManager;

/**
 * Controlador para las métricas administrativas.
 * Proporciona datos y lógica de negocio para la visualización de métricas
 * y estadísticas del sistema.
 */
public class AdminMetricsController {

    // Repositorios y servicios necesarios
    private ShipmentRepository shipmentRepository;
    private DelivererRepository delivererRepository;
    private UserRepository userRepository;
    private PaymentRepository paymentRepository;
    private ShipmentService shipmentService;

    /**
     * Constructor que inicializa los repositorios y servicios necesarios
     */
    public AdminMetricsController() {
        DataManager dataManager = DataManager.getInstance();
        // Acceder a los repositorios
        this.shipmentRepository = new ShipmentRepository();
        this.delivererRepository = new DelivererRepository();
        this.userRepository = new UserRepository();
        this.paymentRepository = new PaymentRepository();
        
        // Obtener la instancia del ShipmentService
        this.shipmentService = ShipmentService.getInstance();
    }

    /**
     * Obtiene el número total de envíos registrados en el sistema
     * @return número total de envíos
     */
    public int getTotalShipments() {
        return shipmentRepository.findAll().size();
    }
    
    /**
     * Obtiene el número total de envíos en estado entregado
     * @return número de envíos entregados
     */
    public int getTotalDeliveredShipments() {
        return shipmentRepository.findAll().stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .collect(Collectors.toList())
            .size();
    }
    
    /**
     * Obtiene el número total de envíos en ruta (asignados a un repartidor)
     * @return número de envíos en ruta
     */
    public int getTotalInTransitShipments() {
        return shipmentRepository.findAll().stream()
            .filter(s -> s.getStatus() == ShipmentStatus.ASSIGNED)
            .collect(Collectors.toList())
            .size();
    }
    
    /**
     * Obtiene el número total de envíos pendientes (pagados pero sin repartidor)
     * @return número de envíos pendientes
     */
    public int getTotalPendingShipments() {
        return shipmentRepository.findAll().stream()
            .filter(s -> s.getStatus() == ShipmentStatus.PENDING)
            .collect(Collectors.toList())
            .size();
    }
    
    /**
     * Obtiene el número total de usuarios registrados
     * @return número total de usuarios
     */
    public int getTotalUsers() {
        return userRepository.findAll().size();
    }
    
    /**
     * Obtiene el número total de repartidores registrados
     * @return número total de repartidores
     */
    public int getTotalDeliverers() {
        return delivererRepository.findAll().size();
    }
    
    /**
     * Obtiene el ingreso total generado por todos los envíos
     * @return monto total de ingresos
     */
    public double getTotalRevenue() {
        return paymentRepository.findAll().stream()
            .mapToDouble(Payment::getAmount)
            .sum();
    }
    
    /**
     * Obtiene los ingresos del mes actual
     * @return monto de ingresos del mes actual
     */
    public double getCurrentMonthRevenue() {
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.of(now.getYear(), now.getMonth());
        
        return paymentRepository.findAll().stream()
            .filter(p -> {
                LocalDate paymentDate = p.getCreationDate().toLocalDate();
                YearMonth paymentMonth = YearMonth.of(paymentDate.getYear(), paymentDate.getMonth());
                return paymentMonth.equals(currentMonth);
            })
            .mapToDouble(Payment::getAmount)
            .sum();
    }
    
    /**
     * Obtiene los datos para un gráfico de envíos por estado
     * @return mapa con estados de envío y su cantidad
     */
    public Map<ShipmentStatus, Integer> getShipmentsByStatus() {
        Map<ShipmentStatus, Integer> result = new HashMap<>();
        
        for (ShipmentStatus status : ShipmentStatus.values()) {
            int count = (int) shipmentRepository.findAll().stream()
                .filter(s -> s.getStatus() == status)
                .count();
            result.put(status, count);
        }
        
        return result;
    }
    
    /**
     * Obtiene los datos para un gráfico de envíos por mes (últimos 6 meses)
     * @return mapa con meses y cantidad de envíos
     */
    public Map<String, Integer> getShipmentsByMonth() {
        Map<String, Integer> result = new HashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        
        // Obtener datos para los últimos 6 meses
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.of(now.getYear(), now.getMonth()).minusMonths(i);
            LocalDate startOfMonth = month.atDay(1);
            LocalDate endOfMonth = month.atEndOfMonth();
            
            int count = (int) shipmentRepository.findAll().stream()
                .filter(s -> {
                    LocalDateTime creationDate = s.getCreationDate();
                    return !creationDate.toLocalDate().isBefore(startOfMonth) && 
                           !creationDate.toLocalDate().isAfter(endOfMonth);
                })
                .count();
            
            result.put(startOfMonth.format(formatter), count);
        }
        
        return result;
    }
    
    /**
     * Obtiene los datos para un gráfico de ingresos por mes (últimos 6 meses)
     * @return mapa con meses y cantidad de ingresos
     */
    public Map<String, Double> getRevenueByMonth() {
        Map<String, Double> result = new HashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        
        // Obtener datos para los últimos 6 meses
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.of(now.getYear(), now.getMonth()).minusMonths(i);
            LocalDate startOfMonth = month.atDay(1);
            LocalDate endOfMonth = month.atEndOfMonth();
            
            double revenue = paymentRepository.findAll().stream()
                .filter(p -> {
                    LocalDate paymentDate = p.getCreationDate().toLocalDate();
                    return !paymentDate.isBefore(startOfMonth) && !paymentDate.isAfter(endOfMonth);
                })
                .mapToDouble(Payment::getAmount)
                .sum();
            
            result.put(startOfMonth.format(formatter), revenue);
        }
        
        return result;
    }
    
    /**
     * Obtiene los repartidores más activos (con más envíos asignados)
     * @param limit número máximo de repartidores a devolver
     * @return lista de repartidores y su número de envíos
     */
    public List<Map.Entry<String, Integer>> getTopDeliverers(int limit) {
        Map<String, Integer> delivererShipments = new HashMap<>();
        
        for (Deliverer deliverer : delivererRepository.findAll()) {
            int count = (int) shipmentRepository.findAll().stream()
                .filter(s -> s.getDeliverer() != null && s.getDeliverer().getId().equals(deliverer.getId()))
                .count();
            
            delivererShipments.put(deliverer.getName(), count);
        }
        
        return delivererShipments.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Calcula el tiempo promedio de entrega de los envíos (en horas)
     * @return tiempo promedio de entrega
     */
    public double getAverageDeliveryTime() {
        List<Shipment> deliveredShipments = shipmentRepository.findAll().stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED && s.getDeliveryDate() != null)
            .collect(Collectors.toList());
        
        if (deliveredShipments.isEmpty()) {
            return 0.0;
        }
        
        double totalHours = 0.0;
        
        for (Shipment shipment : deliveredShipments) {
            LocalDateTime createdDate = shipment.getCreationDate();
            LocalDateTime deliveredDate = shipment.getDeliveryDate();
            
            if (createdDate != null && deliveredDate != null) {
                // Calcular diferencia en horas
                long seconds = java.time.Duration.between(createdDate, deliveredDate).getSeconds();
                double hours = seconds / 3600.0;
                totalHours += hours;
            }
        }
        
        return totalHours / deliveredShipments.size();
    }
}