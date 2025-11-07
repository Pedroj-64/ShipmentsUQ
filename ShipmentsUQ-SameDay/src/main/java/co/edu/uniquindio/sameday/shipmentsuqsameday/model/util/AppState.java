package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Incident;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Payment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Rate;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.UserPaymentMethod;

/**
 * Clase que representa el estado global de la aplicación.
 * Contiene todas las listas de objetos que deben ser persistidas entre sesiones.
 */
public class AppState implements Serializable {
    
    /** ID para la serialización */
    private static final long serialVersionUID = 1L;
    
    /** Listas de objetos del dominio */
    private List<User> users;
    private List<Deliverer> deliverers;
    private List<Shipment> shipments;
    private List<Address> addresses;
    private List<Payment> payments;
    private List<Rate> rates;
    private List<Incident> incidents;
    private List<UserPaymentMethod> paymentMethods;
    
    /**
     * Constructor que inicializa las listas vacías.
     */
    public AppState() {
        users = new ArrayList<>();
        deliverers = new ArrayList<>();
        shipments = new ArrayList<>();
        addresses = new ArrayList<>();
        payments = new ArrayList<>();
        rates = new ArrayList<>();
        incidents = new ArrayList<>();
        paymentMethods = new ArrayList<>();
    }

    // Getters y setters
    
    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Deliverer> getDeliverers() {
        return deliverers;
    }

    public void setDeliverers(List<Deliverer> deliverers) {
        this.deliverers = deliverers;
    }

    public List<Shipment> getShipments() {
        return shipments;
    }

    public void setShipments(List<Shipment> shipments) {
        this.shipments = shipments;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public List<Rate> getRates() {
        return rates;
    }

    public void setRates(List<Rate> rates) {
        this.rates = rates;
    }

    public List<Incident> getIncidents() {
        return incidents;
    }

    public void setIncidents(List<Incident> incidents) {
        this.incidents = incidents;
    }

    public List<UserPaymentMethod> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(List<UserPaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    //Debimos usar Spring Boot estoy al 98% de que esto seria mas facil con JPA y demas pero bueh
}