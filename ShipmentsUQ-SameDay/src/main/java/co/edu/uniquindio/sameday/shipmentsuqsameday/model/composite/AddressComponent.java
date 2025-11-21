package co.edu.uniquindio.sameday.shipmentsuqsameday.model.composite;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;

import java.util.UUID;

/**
 * Leaf (Hoja) que representa una dirección individual del usuario
 * 
 * Patrón de Diseño: COMPOSITE
 * Esta clase representa un elemento individual (hoja) en la estructura composite.
 * Envuelve un objeto Address para integrarlo en la jerarquía del patrón.
 */
public class AddressComponent implements IUserComponent {
    
    private static final long serialVersionUID = 1L;
    
    private Address address;
    private boolean isDefault;
    
    public AddressComponent(Address address) {
        this.address = address;
        this.isDefault = false;
    }
    
    public AddressComponent(Address address, boolean isDefault) {
        this.address = address;
        this.isDefault = isDefault;
    }
    
    @Override
    public UUID getId() {
        return address.getId();
    }
    
    @Override
    public String getDescription() {
        String defaultMarker = isDefault ? " [PREDETERMINADA]" : "";
        return String.format("%s, %s, %s%s", 
                address.getStreet(), 
                address.getCity(), 
                address.getZipCode(),
                defaultMarker);
    }
    
    @Override
    public boolean isValid() {
        // Una dirección es válida si tiene los campos mínimos requeridos
        return address != null 
                && address.getStreet() != null && !address.getStreet().isEmpty()
                && address.getCity() != null && !address.getCity().isEmpty();
    }
    
    @Override
    public int count() {
        return 1; // Una hoja siempre cuenta como 1
    }
    
    /**
     * Obtiene el objeto Address subyacente
     * @return el objeto Address
     */
    public Address getAddress() {
        return address;
    }
    
    /**
     * Verifica si es la dirección predeterminada
     * @return true si es predeterminada
     */
    public boolean isDefault() {
        return isDefault;
    }
    
    /**
     * Establece si es la dirección predeterminada
     * @param isDefault true para marcar como predeterminada
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    /**
     * Obtiene la ciudad de la dirección
     * @return ciudad
     */
    public String getCity() {
        return address.getCity();
    }
    
    /**
     * Obtiene la calle de la dirección
     * @return calle
     */
    public String getStreet() {
        return address.getStreet();
    }
    
    /**
     * Obtiene el código postal
     * @return código postal
     */
    public String getZipCode() {
        return address.getZipCode();
    }
}
