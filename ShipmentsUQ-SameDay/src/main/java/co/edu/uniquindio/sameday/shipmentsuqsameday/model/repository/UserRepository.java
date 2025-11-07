package co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repositorio para gestionar la persistencia de usuarios
 */
public class UserRepository extends BaseRepository<User> {
    
    @Override
    protected UUID getEntityId(User user) {
        return user.getId();
    }
    
    @Override
    protected void setEntityId(User user, UUID id) {
        user.setId(id);
    }

    /**
     * Busca un usuario por su email
     * @param email email del usuario
     * @return usuario encontrado o vacío si no existe
     */
    public Optional<User> findByEmail(String email) {
        return entities.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    /**
     * Busca un usuario por su teléfono
     * @param phone teléfono del usuario
     * @return usuario encontrado o vacío si no existe
     */
    public Optional<User> findByPhone(String phone) {
        return entities.values().stream()
                .filter(u -> u.getPhone().equals(phone))
                .findFirst();
    }

    /**
     * Busca usuarios por zona
     * @param zone zona a buscar
     * @return lista de usuarios en esa zona
     */
    public List<User> findByZone(String zone) {
        return entities.values().stream()
                .filter(u -> u.getAddresses().stream()
                        .anyMatch(a -> a.getZone().equals(zone)))
                .collect(Collectors.toList());
    }
}
