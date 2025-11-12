package co.edu.uniquindio.sameday.shipmentsuqsameday.mapping;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.function.BiConsumer;

/**
 * Componente de mapa interactivo usando OpenStreetMap y Leaflet.js
 * Permite seleccionar ubicaciones en el mapa y obtener coordenadas reales
 */
public class InteractiveMapView {
    
    private WebView webView;
    private WebEngine webEngine;
    private Coordinates selectedOrigin;
    private Coordinates selectedDestination;
    private BiConsumer<Coordinates, Coordinates> onCoordinatesSelected;
    
    // Coordenadas por defecto (Armenia, Quindío)
    private static final double DEFAULT_LAT = 4.533889;
    private static final double DEFAULT_LNG = -75.681111;
    private static final int DEFAULT_ZOOM = 13;
    
    public InteractiveMapView() {
        this.webView = new WebView();
        this.webEngine = webView.getEngine();
        this.selectedOrigin = new Coordinates(DEFAULT_LAT, DEFAULT_LNG);
        this.selectedDestination = new Coordinates(DEFAULT_LAT, DEFAULT_LNG);
        
        initializeMap();
    }
    
    /**
     * Obtiene el WebView para agregarlo a la interfaz
     */
    public WebView getWebView() {
        return webView;
    }
    
    /**
     * Inicializa el mapa cargando el HTML con Leaflet
     */
    private void initializeMap() {
        // Cargar el HTML del mapa desde resources
        try {
            String htmlContent = loadMapHTML();
            webEngine.loadContent(htmlContent);
            
            // Esperar a que el mapa cargue para configurar el bridge Java-JavaScript
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    setupJavaScriptBridge();
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error al cargar el mapa: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Configura el puente entre Java y JavaScript
     */
    private void setupJavaScriptBridge() {
        try {
            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaApp", new JavaScriptBridge());
            
            // Inicializar el mapa en Armenia
            centerMapOn(DEFAULT_LAT, DEFAULT_LNG, DEFAULT_ZOOM);
            
        } catch (Exception e) {
            System.err.println("Error al configurar el bridge JavaScript: " + e.getMessage());
        }
    }
    
    /**
     * Carga el contenido HTML del mapa desde el archivo de resources
     */
    private String loadMapHTML() {
        try {
            InputStream is = getClass().getResourceAsStream("/co/edu/uniquindio/sameday/shipmentsuqsameday/maps/interactive-map.html");
            if (is == null) {
                // Si no existe el archivo, crear HTML inline
                return createDefaultMapHTML();
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            System.err.println("Error al cargar HTML del mapa: " + e.getMessage());
            return createDefaultMapHTML();
        }
    }
    
    /**
     * Crea el HTML del mapa por defecto con Leaflet
     */
    private String createDefaultMapHTML() {
        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Mapa de Envíos - ShipmentsUQ</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
    <!-- Leaflet CSS -->
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    
    <style>
        body {
            margin: 0;
            padding: 0;
            font-family: 'Segoe UI', Arial, sans-serif;
        }
        #map {
            position: absolute;
            top: 0;
            bottom: 0;
            width: 100%;
            height: 100%;
        }
        .info-panel {
            position: absolute;
            top: 10px;
            right: 10px;
            background: white;
            padding: 15px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.2);
            z-index: 1000;
            max-width: 250px;
        }
        .info-panel h3 {
            margin: 0 0 10px 0;
            color: #667eea;
            font-size: 16px;
        }
        .info-panel p {
            margin: 5px 0;
            font-size: 12px;
            color: #333;
        }
        .mode-selector {
            position: absolute;
            top: 10px;
            left: 10px;
            background: white;
            padding: 10px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.2);
            z-index: 1000;
        }
        .mode-btn {
            display: block;
            width: 100%;
            padding: 8px 15px;
            margin: 5px 0;
            border: 2px solid #667eea;
            background: white;
            color: #667eea;
            border-radius: 5px;
            cursor: pointer;
            font-weight: 600;
            transition: all 0.3s;
        }
        .mode-btn:hover {
            background: #667eea;
            color: white;
        }
        .mode-btn.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
    </style>
</head>
<body>
    <div class="mode-selector">
        <button class="mode-btn active" id="btnOrigin">📍 Origen</button>
        <button class="mode-btn" id="btnDestination">🎯 Destino</button>
    </div>
    
    <div class="info-panel">
        <h3>ShipmentsUQ</h3>
        <p><strong>Ubicación:</strong> Armenia, Quindío</p>
        <p id="infoText">Haz clic en el mapa para seleccionar el origen</p>
        <p id="coordsText">Lat: -, Lng: -</p>
    </div>
    
    <div id="map"></div>
    
    <!-- Leaflet JS -->
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    
    <script>
        // Inicializar el mapa centrado en Armenia, Quindío
        var map = L.map('map').setView([4.533889, -75.681111], 13);
        
        // Agregar capa de OpenStreetMap
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors',
            maxZoom: 19
        }).addTo(map);
        
        // Marcadores para origen y destino
        var originMarker = null;
        var destinationMarker = null;
        var currentMode = 'origin';
        
        // Línea entre origen y destino
        var routeLine = null;
        
        // Iconos personalizados
        var originIcon = L.icon({
            iconUrl: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIzMiIgaGVpZ2h0PSI0MCI+PGNpcmNsZSBjeD0iMTYiIGN5PSIxNiIgcj0iMTQiIGZpbGw9IiM2NjdlZWEiLz48Y2lyY2xlIGN4PSIxNiIgY3k9IjE2IiByPSI4IiBmaWxsPSJ3aGl0ZSIvPjwvc3ZnPg==',
            iconSize: [32, 40],
            iconAnchor: [16, 40],
            popupAnchor: [0, -40]
        });
        
        var destinationIcon = L.icon({
            iconUrl: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIzMiIgaGVpZ2h0PSI0MCI+PGNpcmNsZSBjeD0iMTYiIGN5PSIxNiIgcj0iMTQiIGZpbGw9IiMxMGI5ODEiLz48Y2lyY2xlIGN4PSIxNiIgY3k9IjE2IiByPSI4IiBmaWxsPSJ3aGl0ZSIvPjwvc3ZnPg==',
            iconSize: [32, 40],
            iconAnchor: [16, 40],
            popupAnchor: [0, -40]
        });
        
        // Botones de modo
        document.getElementById('btnOrigin').addEventListener('click', function() {
            currentMode = 'origin';
            this.classList.add('active');
            document.getElementById('btnDestination').classList.remove('active');
            document.getElementById('infoText').textContent = 'Haz clic en el mapa para seleccionar el origen';
        });
        
        document.getElementById('btnDestination').addEventListener('click', function() {
            currentMode = 'destination';
            this.classList.add('active');
            document.getElementById('btnOrigin').classList.remove('active');
            document.getElementById('infoText').textContent = 'Haz clic en el mapa para seleccionar el destino';
        });
        
        // Manejar clics en el mapa
        map.on('click', function(e) {
            var lat = e.latlng.lat;
            var lng = e.latlng.lng;
            
            if (currentMode === 'origin') {
                if (originMarker) {
                    map.removeLayer(originMarker);
                }
                originMarker = L.marker([lat, lng], {icon: originIcon})
                    .addTo(map)
                    .bindPopup('<b>Origen</b><br>Lat: ' + lat.toFixed(6) + '<br>Lng: ' + lng.toFixed(6))
                    .openPopup();
                
                document.getElementById('coordsText').textContent = 'Origen: ' + lat.toFixed(6) + ', ' + lng.toFixed(6);
                
                // Notificar a Java
                if (window.javaApp) {
                    window.javaApp.setOrigin(lat, lng);
                }
            } else {
                if (destinationMarker) {
                    map.removeLayer(destinationMarker);
                }
                destinationMarker = L.marker([lat, lng], {icon: destinationIcon})
                    .addTo(map)
                    .bindPopup('<b>Destino</b><br>Lat: ' + lat.toFixed(6) + '<br>Lng: ' + lng.toFixed(6))
                    .openPopup();
                
                document.getElementById('coordsText').textContent = 'Destino: ' + lat.toFixed(6) + ', ' + lng.toFixed(6);
                
                // Notificar a Java
                if (window.javaApp) {
                    window.javaApp.setDestination(lat, lng);
                }
            }
            
            // Dibujar línea si ambos marcadores existen
            if (originMarker && destinationMarker) {
                if (routeLine) {
                    map.removeLayer(routeLine);
                }
                routeLine = L.polyline([
                    originMarker.getLatLng(),
                    destinationMarker.getLatLng()
                ], {
                    color: '#667eea',
                    weight: 3,
                    opacity: 0.7,
                    dashArray: '10, 10'
                }).addTo(map);
                
                // Ajustar vista para mostrar toda la ruta
                var bounds = L.latLngBounds([originMarker.getLatLng(), destinationMarker.getLatLng()]);
                map.fitBounds(bounds, {padding: [50, 50]});
            }
        });
        
        // Funciones para ser llamadas desde Java
        function centerMap(lat, lng, zoom) {
            map.setView([lat, lng], zoom);
        }
        
        function clearMarkers() {
            if (originMarker) {
                map.removeLayer(originMarker);
                originMarker = null;
            }
            if (destinationMarker) {
                map.removeLayer(destinationMarker);
                destinationMarker = null;
            }
            if (routeLine) {
                map.removeLayer(routeLine);
                routeLine = null;
            }
        }
    </script>
</body>
</html>
                """;
    }
    
    /**
     * Centra el mapa en una ubicación específica
     */
    public void centerMapOn(double lat, double lng, int zoom) {
        webEngine.executeScript(String.format("centerMap(%f, %f, %d);", lat, lng, zoom));
    }
    
    /**
     * Limpia todos los marcadores del mapa
     */
    public void clearMarkers() {
        webEngine.executeScript("clearMarkers();");
        selectedOrigin = new Coordinates(DEFAULT_LAT, DEFAULT_LNG);
        selectedDestination = new Coordinates(DEFAULT_LAT, DEFAULT_LNG);
    }
    
    /**
     * Establece el callback para cuando se seleccionen coordenadas
     */
    public void setOnCoordinatesSelected(BiConsumer<Coordinates, Coordinates> callback) {
        this.onCoordinatesSelected = callback;
    }
    
    // Getters
    public Coordinates getSelectedOrigin() {
        return selectedOrigin;
    }
    
    public Coordinates getSelectedDestination() {
        return selectedDestination;
    }
    
    /**
     * Clase interna para el bridge Java-JavaScript
     * Permite que JavaScript llame métodos Java
     */
    public class JavaScriptBridge {
        
        public void setOrigin(double lat, double lng) {
            selectedOrigin = new Coordinates(lat, lng);
            System.out.println("Origen seleccionado: " + selectedOrigin);
            notifyCoordinatesChanged();
        }
        
        public void setDestination(double lat, double lng) {
            selectedDestination = new Coordinates(lat, lng);
            System.out.println("Destino seleccionado: " + selectedDestination);
            notifyCoordinatesChanged();
        }
        
        private void notifyCoordinatesChanged() {
            if (onCoordinatesSelected != null) {
                javafx.application.Platform.runLater(() -> 
                    onCoordinatesSelected.accept(selectedOrigin, selectedDestination)
                );
            }
        }
    }
}
