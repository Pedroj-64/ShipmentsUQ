package co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repositorio para gestionar la persistencia de direcciones
 */
public class AddressRepository extends BaseRepository<Address> {
    
    @Override
    protected UUID getEntityId(Address address) {
        return address.getId();
    }
    
    @Override
    protected void setEntityId(Address address, UUID id) {
        address.setId(id);
    }
    
    /**
     * Busca direcciones por zona
     * @param zone zona a buscar
     * @return lista de direcciones en la zona especificada
     */
    public List<Address> findByZone(String zone) {
        return entities.values().stream()
                .filter(a -> a.getZone().equals(zone))
                .collect(Collectors.toList());
    }
    
    /**
     * Busca direcciones por ciudad
     * @param city ciudad a buscar
     * @return lista de direcciones en la ciudad especificada
     */
    public List<Address> findByCity(String city) {
        return entities.values().stream()
                .filter(a -> a.getCity().equals(city))
                .collect(Collectors.toList());
    }

    /**
     * Busca las direcciones predeterminadas
     * @return lista de direcciones marcadas como predeterminadas
     */
    public List<Address> findDefaultAddresses() {
        return entities.values().stream()
                .filter(Address::isDefault)
                .collect(Collectors.toList());
    }
}
