package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IGridCoordinate;

import java.util.*;

/**
 * Representa un mapa de cuadrícula para ubicación y enrutamiento
 * Reemplaza el sistema de coordenadas geográficas
 */
public class GridMap {
    private final int width;
    private final int height;
    private final Map<String, GridCell> cells;
    
    /**
     * Constructor para crear un nuevo mapa de cuadrícula
     * @param width ancho del mapa en celdas
     * @param height alto del mapa en celdas
     */
    public GridMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new HashMap<>();
        
        // Inicializar celdas
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                String key = x + "," + y;
                cells.put(key, new GridCell(x, y));
            }
        }
    }
    
    /**
     * Obtiene una celda en las coordenadas especificadas
     * @param x coordenada X
     * @param y coordenada Y
     * @return la celda en esas coordenadas
     */
    public GridCell getCell(int x, int y) {
        String key = x + "," + y;
        return cells.get(key);
    }
    
    /**
     * Calcula la distancia entre dos celdas del mapa
     * @param start celda de inicio
     * @param end celda de destino
     * @return distancia euclidiana
     */
    public double calculateDistance(GridCell start, GridCell end) {
        return start.distanceTo(end);
    }
    
    /**
     * Convierte coordenadas de mapa a una dirección
     * @param x coordenada X
     * @param y coordenada Y
     * @return dirección con las coordenadas asignadas
     */
    public Address createAddressFromCoordinates(int x, int y) {
        return Address.builder()
                .coordX(x)
                .coordY(y)
                .build();
    }
    
    /**
     * Clase interna para representar una celda en el mapa de cuadrícula
     */
    public static class GridCell implements IGridCoordinate {
        private final int x;
        private final int y;
        private final Set<String> attributes;
        
        public GridCell(int x, int y) {
            this.x = x;
            this.y = y;
            this.attributes = new HashSet<>();
        }
        
        @Override
        public double getX() {
            return x;
        }
        
        @Override
        public double getY() {
            return y;
        }
        
        /**
         * Añade un atributo a la celda (por ejemplo, "residencial", "comercial")
         * @param attribute atributo a añadir
         */
        public void addAttribute(String attribute) {
            attributes.add(attribute);
        }
        
        /**
         * Comprueba si la celda tiene un atributo específico
         * @param attribute atributo a comprobar
         * @return true si la celda tiene el atributo
         */
        public boolean hasAttribute(String attribute) {
            return attributes.contains(attribute);
        }
        
        /**
         * Obtiene todos los atributos de la celda
         * @return conjunto de atributos
         */
        public Set<String> getAttributes() {
            return Collections.unmodifiableSet(attributes);
        }
    }
}