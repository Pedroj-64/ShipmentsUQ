package co.edu.uniquindio.sameday.shipmentsuqsameday.internalController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.ui.components.GridMapRenderer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.GridMap;
import javafx.scene.layout.Pane;

/**
 * Controlador interno para manejar la visualización y la interacción con el mapa de cuadrícula.
 * Este controlador sirve como intermediario entre la interfaz de usuario y el modelo de datos del mapa.
 */
public class GridMapViewController {
    private final GridMap gridMap;
    private final GridMapRenderer renderer;
    private GridMapCoordinateListener coordinateListener;
    
    /**
     * Constructor para el controlador del mapa de cuadrícula
     * @param containerWidth ancho del contenedor
     * @param containerHeight alto del contenedor
     * @param cellSize tamaño de cada celda en píxeles
     */
    public GridMapViewController(double containerWidth, double containerHeight, double cellSize) {
        this(containerWidth, containerHeight, cellSize, true);
    }
    
    /**
     * Constructor para el controlador del mapa de cuadrícula
     * @param containerWidth ancho del contenedor
     * @param containerHeight alto del contenedor
     * @param cellSize tamaño de cada celda en píxeles
     * @param clickable indica si el mapa debe responder a clics del usuario
     */
    public GridMapViewController(double containerWidth, double containerHeight, double cellSize, boolean clickable) {
        // Calcular el número de celdas basado en el tamaño del contenedor
        int numCellsX = (int) (containerWidth / cellSize);
        int numCellsY = (int) (containerHeight / cellSize);
        
        this.gridMap = new GridMap(numCellsX, numCellsY);
        this.renderer = new GridMapRenderer(gridMap, containerWidth, containerHeight, cellSize, clickable);
        
        // Configurar el listener para capturar la selección de coordenadas
        this.renderer.setCoordinateListener((x, y) -> {
            if (coordinateListener != null) {
                coordinateListener.onCoordinatesSelected(x, y);
            }
        });
    }
    
    /**
     * Interfaz para notificar la selección de coordenadas
     */
    public interface GridMapCoordinateListener {
        void onCoordinatesSelected(double x, double y);
    }
    
    /**
     * Establece un listener para la selección de coordenadas
     * @param listener el listener a establecer
     */
    public void setCoordinateListener(GridMapCoordinateListener listener) {
        this.coordinateListener = listener;
    }
    
    /**
     * Inicializa la vista del mapa en un contenedor
     * @param container el contenedor donde mostrar el mapa
     */
    public void initialize(Pane container) {
        container.getChildren().clear();
        renderer.addToContainer(container);
        renderer.renderMap();
    }
    
    /**
     * Establece una coordenada seleccionada y actualiza la vista
     * @param x coordenada X
     * @param y coordenada Y
     */
    public void setSelectedCoordinates(double x, double y) {
        renderer.clearSelection(); // Limpia la selección anterior
        renderer.setSelectedCoordinates(x, y);
        renderer.renderMap(); // Actualiza el mapa
    }
    
    /**
     * Obtiene la coordenada X seleccionada
     * @return coordenada X
     */
    public double getSelectedX() {
        return renderer.getSelectedX();
    }
    
    /**
     * Obtiene la coordenada Y seleccionada
     * @return coordenada Y
     */
    public double getSelectedY() {
        return renderer.getSelectedY();
    }
    
    /**
     * Actualiza la vista del mapa
     */
    public void refreshMap() {
        renderer.renderMap();
    }
    
    /**
     * Limpia la selección actual en el mapa
     */
    public void clearSelection() {
        renderer.clearSelection();
    }
}