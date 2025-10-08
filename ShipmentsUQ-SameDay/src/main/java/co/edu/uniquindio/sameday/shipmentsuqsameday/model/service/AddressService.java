package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IDistanceCalculator;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.EuclideanDistanceCalculator;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.AddressRepository;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar operaciones relacionadas con direcciones
 * Utiliza coordenadas cartesianas (X, Y) para el cálculo de distancias
 */
public class AddressService implements Service<Address, AddressRepository> {
    private final AddressRepository repository;
    private final IDistanceCalculator distanceCalculator;

    // Constructor privado para Singleton
    private AddressService() {
        this.repository = new AddressRepository();
        this.distanceCalculator = new EuclideanDistanceCalculator();
    }
    
    // Holder estático para instancia única
    private static class SingletonHolder {
        private static final AddressService INSTANCE = new AddressService();
    }
    
    /**
     * Obtiene la instancia única del servicio
     * @return instancia del servicio
     */
    public static AddressService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public AddressRepository getRepository() {
        return repository;
    }

    /**
     * Calcula la distancia entre dos direcciones
     * @param origin dirección de origen
     * @param destination dirección de destino
     * @return distancia en kilómetros
     */
    public double calculateDistanceBetweenAddresses(Address origin, Address destination) {
        return distanceCalculator.calculateDistance(origin, destination);
    }

    /**
     * Busca direcciones cercanas a unas coordenadas dadas
     * @param coordX coordenada X de referencia
     * @param coordY coordenada Y de referencia
     * @param radius radio en unidades de la cuadrícula
     * @return lista de direcciones dentro del radio especificado
     */
    public List<Address> findNearbyAddresses(double coordX, double coordY, double radius) {
        Address referencePoint = Address.builder()
            .coordX(coordX)
            .coordY(coordY)
            .build();
            
        return repository.findAll().stream()
                .filter(a -> distanceCalculator.calculateDistance(referencePoint, a) <= radius)
                .toList();
    }

    /**
     * Busca direcciones en una zona específica
     * @param zone zona a buscar
     * @return lista de direcciones en la zona
     */
    public List<Address> findAddressesByZone(String zone) {
        return repository.findByZone(zone);
    }

    /**
     * Busca direcciones en una ciudad
     * @param city ciudad a buscar
     * @return lista de direcciones en la ciudad
     */
    public List<Address> findAddressesByCity(String city) {
        return repository.findByCity(city);
    }

    /**
     * Establece una dirección como predeterminada
     * @param addressId ID de la dirección
     * @return dirección actualizada
     */
    public Address setAsDefault(UUID addressId) {
        Address address = repository.findById(addressId)
            .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada"));
            
        // Desmarcar otras direcciones predeterminadas
        repository.findDefaultAddresses().forEach(a -> {
            a.setDefault(false);
            repository.update(a);
        });
        
        // Marcar la nueva dirección como predeterminada
        address.setDefault(true);
        return repository.update(address);
    }
}