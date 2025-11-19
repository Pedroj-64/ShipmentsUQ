package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.DelivererService;

import java.util.Optional;

/**
 * Controlador de negocio para operaciones de repartidores.
 * Patrón MVC: Controller - lógica de negocio y coordinación entre modelo y vista.
 * 
 * @author ShipmentsUQ Team
 * @version 1.0
 */
public class DelivererController {
    
    private final DelivererService delivererService;
    
    public DelivererController() {
        this.delivererService = DelivererService.getInstance();
    }
    
    /**
     * Autentica un repartidor por documento y contraseña.
     * Nota: En producción, la contraseña debería estar hasheada.
     * 
     * @param document documento del repartidor
     * @param password contraseña
     * @return Optional con el repartidor si la autenticación es exitosa
     */
    public Optional<Deliverer> authenticateDeliverer(String document, String password) {
        if (document == null || document.trim().isEmpty()) {
            return Optional.empty();
        }
        if (password == null || password.isEmpty()) {
            return Optional.empty();
        }
        
        // Buscar repartidor por documento
        Optional<Deliverer> delivererOpt = delivererService.getRepository().findByDocument(document.trim());
        
        if (delivererOpt.isEmpty()) {
            System.out.println("Repartidor no encontrado con documento: " + document);
            return Optional.empty();
        }
        
        Deliverer deliverer = delivererOpt.get();
        
        // Validar contraseña
        // NOTA: En producción se debe usar hashing (BCrypt, etc.)
        // Por ahora, usamos el teléfono como contraseña temporal
        if (deliverer.getPhone().equals(password)) {
            System.out.println("Autenticación exitosa para repartidor: " + deliverer.getName());
            return Optional.of(deliverer);
        } else {
            System.out.println("Contraseña incorrecta para repartidor: " + deliverer.getName());
            return Optional.empty();
        }
    }
    
    /**
     * Obtiene el servicio de repartidores
     * @return servicio de repartidores
     */
    public DelivererService getDelivererService() {
        return delivererService;
    }
}
