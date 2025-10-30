package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Payment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.DelivererRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.PaymentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.ShipmentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.UserRepository;

/**
 * Controlador para las métricas administrativas.
 * Maneja la lógica de negocio para obtener estadísticas del sistema.
 */
public class AdminMetricsNewController {

    private final ShipmentRepository shipmentRepository;
    private final DelivererRepository delivererRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public AdminMetricsNewController() {
        this.shipmentRepository = new ShipmentRepository();
        this.delivererRepository = new DelivererRepository();
        this.userRepository = new UserRepository();
        this.paymentRepository = new PaymentRepository();
    }

    // ========== MÉTRICAS BÁSICAS ==========
    
    public int getTotalShipments() {
        return shipmentRepository.findAll().size();
    }
    
    public int getTotalDeliveredShipments() {
        return (int) shipmentRepository.findAll().stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .count();
    }
    
    public int getTotalInTransitShipments() {
        return (int) shipmentRepository.findAll().stream()
            .filter(s -> s.getStatus() == ShipmentStatus.ASSIGNED)
            .count();
    }
    
    public int getTotalPendingShipments() {
        return (int) shipmentRepository.findAll().stream()
            .filter(s -> s.getStatus() == ShipmentStatus.PENDING)
            .count();
    }
    
    public int getTotalUsers() {
        return userRepository.findAll().size();
    }
    
    public int getTotalDeliverers() {
        return delivererRepository.findAll().size();
    }

    // ========== MÉTRICAS FINANCIERAS ==========
    
    public double getTotalRevenue() {
        return paymentRepository.findAll().stream()
            .mapToDouble(Payment::getAmount)
            .sum();
    }
    
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

    // ========== DATOS PARA GRÁFICOS ==========
    
    public Map<ShipmentStatus, Integer> getShipmentsByStatus() {
        Map<ShipmentStatus, Integer> result = new HashMap<>();
        
        for (ShipmentStatus status : ShipmentStatus.values()) {
            int count = (int) shipmentRepository.findAll().stream()
                .filter(s -> s.getStatus() == status)
                .count();
            if (count > 0) {
                result.put(status, count);
            }
        }
        
        return result;
    }
    
    public Map<String, Integer> getShipmentsByMonth() {
        Map<String, Integer> result = new HashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        
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
    
    public Map<String, Double> getRevenueByMonth() {
        Map<String, Double> result = new HashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        
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
                long seconds = java.time.Duration.between(createdDate, deliveredDate).getSeconds();
                double hours = seconds / 3600.0;
                totalHours += hours;
            }
        }
        
        return totalHours / deliveredShipments.size();
    }
}
