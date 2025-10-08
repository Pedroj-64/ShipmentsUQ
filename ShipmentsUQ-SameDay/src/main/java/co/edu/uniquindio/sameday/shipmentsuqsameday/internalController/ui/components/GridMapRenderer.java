package co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.ui.components;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.GridMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;

/**
 * Componente de UI que renderiza un mapa de cuadrícula en un canvas JavaFX.
 * Se encarga de dibujar la cuadrícula, capturar eventos de clic y mostrar
 * visualmente las coordenadas seleccionadas.
 */
public class GridMapRenderer {
    private final GridMap gridMap;
    private final Canvas canvas;
    private final double cellSize;
    private double coordX = -1;
    private double coordY = -1;
    
    /**
     * Constructor para crear un nuevo renderizador de mapa de cuadrícula
     * @param gridMap el mapa de cuadrícula a renderizar
     * @param width ancho del canvas
     * @param height alto del canvas
     * @param cellSize tamaño de cada celda en píxeles
     */
    public GridMapRenderer(GridMap gridMap, double width, double height, double cellSize) {
        this.gridMap = gridMap;
        this.canvas = new Canvas(width, height);
        this.cellSize = cellSize;
        
        // Configurar canvas para capturar clics
        canvas.setOnMouseClicked(e -> {
            coordX = (int)(e.getX() / cellSize);
            coordY = (int)(e.getY() / cellSize);
            renderMap(); // Volver a dibujar para mostrar la selección
            
            // Aquí se podría llamar a un listener para notificar la selección
            if (coordinateListener != null) {
                coordinateListener.onCoordinateSelected(coordX, coordY);
            }
        });
    }
    
    /**
     * Interfaz para notificar la selección de coordenadas
     */
    public interface CoordinateListener {
        void onCoordinateSelected(double x, double y);
    }
    
    private CoordinateListener coordinateListener;
    
    /**
     * Establece un listener para la selección de coordenadas
     * @param listener el listener a establecer
     */
    public void setCoordinateListener(CoordinateListener listener) {
        this.coordinateListener = listener;
    }
    
    /**
     * Obtiene el canvas con el mapa renderizado
     * @return el canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }
    
    /**
     * Añade el canvas a un contenedor JavaFX
     * @param container el contenedor donde añadir el canvas
     */
    public void addToContainer(Pane container) {
        container.getChildren().add(canvas);
    }
    
    /**
     * Renderiza el mapa en el canvas
     */
    public void renderMap() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Dibujar la cuadrícula
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        
        int numCellsX = (int) (canvas.getWidth() / cellSize);
        int numCellsY = (int) (canvas.getHeight() / cellSize);
        
        // Líneas horizontales
        for (int y = 0; y <= numCellsY; y++) {
            gc.strokeLine(0, y * cellSize, canvas.getWidth(), y * cellSize);
        }
        
        // Líneas verticales
        for (int x = 0; x <= numCellsX; x++) {
            gc.strokeLine(x * cellSize, 0, x * cellSize, canvas.getHeight());
        }
        
        // Dibujar celda seleccionada si hay alguna
        if (coordX >= 0 && coordY >= 0) {
            gc.setFill(Color.BLUE);
            gc.setGlobalAlpha(0.3);
            gc.fillRect(coordX * cellSize, coordY * cellSize, cellSize, cellSize);
            gc.setGlobalAlpha(1.0);
            
            // Mostrar las coordenadas
            gc.setFill(Color.BLACK);
            gc.fillText(String.format("(%d, %d)", (int)coordX, (int)coordY), 
                    coordX * cellSize + 5, coordY * cellSize + 15);
        }
    }
    
    /**
     * Establece las coordenadas seleccionadas
     * @param x coordenada X
     * @param y coordenada Y
     */
    public void setSelectedCoordinates(double x, double y) {
        this.coordX = x;
        this.coordY = y;
        renderMap();
    }
    
    /**
     * Obtiene la coordenada X seleccionada
     * @return coordenada X
     */
    public double getSelectedX() {
        return coordX;
    }
    
    /**
     * Obtiene la coordenada Y seleccionada
     * @return coordenada Y
     */
    public double getSelectedY() {
        return coordY;
    }
    
    /**
     * Convierte una posición en píxeles a coordenadas de la cuadrícula
     * @param pixelX posición X en píxeles
     * @param pixelY posición Y en píxeles
     * @return array con [coordX, coordY]
     */
    public double[] pixelToGridCoordinates(double pixelX, double pixelY) {
        double gridX = Math.floor(pixelX / cellSize);
        double gridY = Math.floor(pixelY / cellSize);
        return new double[]{gridX, gridY};
    }
    
    /**
     * Convierte coordenadas de la cuadrícula a posición en píxeles (centro de la celda)
     * @param gridX coordenada X de la cuadrícula
     * @param gridY coordenada Y de la cuadrícula
     * @return array con [pixelX, pixelY] centrado en la celda
     */
    public double[] gridToPixelCoordinates(double gridX, double gridY) {
        double pixelX = (gridX * cellSize) + (cellSize / 2);
        double pixelY = (gridY * cellSize) + (cellSize / 2);
        return new double[]{pixelX, pixelY};
    }
}