package co.edu.uniquindio.sameday.shipmentsuqsameday.model.composite;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Composite que representa una colección de direcciones del usuario
 * 
 * Patrón de Diseño: COMPOSITE
 * Problema que resuelve: El usuario tiene múltiples direcciones y necesitamos
 * tratarlas de manera uniforme, tanto individualmente como en conjunto.
 * 
 * Beneficio: Simplifica el manejo de direcciones múltiples, permite operaciones
 * sobre toda la colección y valida todas las direcciones de una vez.
 */
public class AddressCollection implements IUserComponent {
    
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private String name; // ej: "Mis direcciones", "Direcciones de trabajo"
    private List<AddressComponent> addresses;
    
    // Constructor sin parámetros (para Lombok @Builder)
    public AddressCollection() {
        this.id = UUID.randomUUID();
        this.name = "Direcciones";
        this.addresses = new ArrayList<>();
    }
    
    public AddressCollection(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.addresses = new ArrayList<>();
    }
    
    // Getter para compatibilidad con User
    public List<AddressComponent> getComponents() {
        return new ArrayList<>(addresses);
    }
    
    // Método de validación para User
    public boolean validate() {
        return isValid();
    }
    
    @Override
    public UUID getId() {
        return id;
    }
    
    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(name).append(" (").append(addresses.size()).append(" direcciones):\n");
        for (AddressComponent addr : addresses) {
            desc.append("  - ").append(addr.getDescription()).append("\n");
        }
        return desc.toString();
    }
    
    @Override
    public boolean isValid() {
        // Una colección es válida si tiene al menos una dirección válida
        return !addresses.isEmpty() && addresses.stream().anyMatch(IUserComponent::isValid);
    }
    
    @Override
    public void add(IUserComponent component) {
        if (component instanceof AddressComponent) {
            addresses.add((AddressComponent) component);
        } else {
            throw new IllegalArgumentException("Solo se pueden agregar AddressComponent a esta colección");
        }
    }
    
    @Override
    public void remove(IUserComponent component) {
        addresses.remove(component);
    }
    
    @Override
    public IUserComponent getChild(int index) {
        if (index >= 0 && index < addresses.size()) {
            return addresses.get(index);
        }
        throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
    }
    
    @Override
    public List<IUserComponent> getChildren() {
        return new ArrayList<>(addresses);
    }
    
    @Override
    public int count() {
        return addresses.size();
    }
    
    /**
     * Obtiene la dirección predeterminada
     * @return AddressComponent predeterminado o null si no hay
     */
    public AddressComponent getDefaultAddress() {
        return addresses.stream()
                .filter(AddressComponent::isDefault)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Establece una dirección como predeterminada
     * @param addressId ID de la dirección a marcar como predeterminada
     */
    public void setDefaultAddress(UUID addressId) {
        // Desmarcar todas
        addresses.forEach(addr -> addr.setDefault(false));
        
        // Marcar la seleccionada
        addresses.stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .ifPresent(addr -> addr.setDefault(true));
    }
    
    /**
     * Obtiene todas las direcciones subyacentes
     * @return lista de objetos Address
     */
    public List<Address> getAllAddresses() {
        return addresses.stream()
                .map(AddressComponent::getAddress)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca una dirección por ID
     * @param id ID de la dirección
     * @return AddressComponent o null si no existe
     */
    public AddressComponent findById(UUID id) {
        return addresses.stream()
                .filter(addr -> addr.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Busca direcciones por ciudad
     * @param city ciudad a buscar
     * @return lista de direcciones en esa ciudad
     */
    public List<AddressComponent> findByCity(String city) {
        return addresses.stream()
                .filter(addr -> addr.getAddress().getCity().equalsIgnoreCase(city))
                .collect(Collectors.toList());
    }
    
    /**
     * Verifica si la colección tiene una dirección predeterminada
     * @return true si hay una dirección predeterminada
     */
    public boolean hasDefaultAddress() {
        return getDefaultAddress() != null;
    }
}
