package co.edu.uniquindio.sameday.shipmentsuqsameday.model.composite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Interfaz Component del patrón Composite
 * 
 * Define operaciones comunes para elementos individuales y colecciones.
 * Esta interfaz permite tratar de manera uniforme tanto a un elemento único
 * como a una colección de elementos.
 */
public interface IUserComponent extends Serializable {
    
    /**
     * Obtiene el ID del componente
     * @return UUID del componente
     */
    UUID getId();
    
    /**
     * Obtiene la descripción del componente
     * @return descripción legible del componente
     */
    String getDescription();
    
    /**
     * Verifica si el componente es válido
     * @return true si el componente es válido
     */
    boolean isValid();
    
    /**
     * Agrega un componente hijo (solo para composites)
     * @param component componente a agregar
     * @throws UnsupportedOperationException si es una hoja
     */
    default void add(IUserComponent component) {
        throw new UnsupportedOperationException("No se pueden agregar componentes a una hoja");
    }
    
    /**
     * Remueve un componente hijo (solo para composites)
     * @param component componente a remover
     * @throws UnsupportedOperationException si es una hoja
     */
    default void remove(IUserComponent component) {
        throw new UnsupportedOperationException("No se pueden remover componentes de una hoja");
    }
    
    /**
     * Obtiene un componente hijo por índice (solo para composites)
     * @param index índice del componente
     * @return componente en el índice especificado
     * @throws UnsupportedOperationException si es una hoja
     */
    default IUserComponent getChild(int index) {
        throw new UnsupportedOperationException("Las hojas no tienen hijos");
    }
    
    /**
     * Obtiene todos los componentes hijos (solo para composites)
     * @return lista de componentes hijos
     */
    default List<IUserComponent> getChildren() {
        return new ArrayList<>();
    }
    
    /**
     * Cuenta el número total de componentes (incluyendo hijos)
     * @return número de componentes
     */
    default int count() {
        return 1;
    }
}
