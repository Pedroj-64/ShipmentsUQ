package co.edu.uniquindio.sameday.shipmentsuqsameday.mapping;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.awt.Desktop;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Servidor HTTP simple para servir la aplicación web del mapa
 * y recibir coordenadas desde JavaScript
 */
public class MapWebServer {
    
    private HttpServer server;
    private static final int PORT = 8080;
    private CoordinatesCallback callback;
    
    public interface CoordinatesCallback {
        void onCoordinatesReceived(Coordinates origin, Coordinates destination);
    }
    
    public MapWebServer() {
        this(null);
    }
    
    public MapWebServer(CoordinatesCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Inicia el servidor HTTP
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Rutas
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/coordinates", new CoordinatesHandler());
        
        server.setExecutor(null); // Usa el executor por defecto
        server.start();
        
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  🗺️  ShipmentsUQ - Servidor de Mapas Iniciado            ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  📍 Puerto: " + PORT + "                                          ║");
        System.out.println("║  🌐 URL: http://localhost:" + PORT + "                          ║");
        System.out.println("║  📂 Sirviendo: webapp/                                     ║");
        System.out.println("║  ✓ Listo para recibir coordenadas desde JavaScript       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
    
    /**
     * Detiene el servidor
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Servidor detenido");
        }
    }
    
    /**
     * Abre el mapa en el navegador por defecto
     */
    public void openInBrowser() {
        try {
            String url = "http://localhost:" + PORT;
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("🌐 Abriendo mapa en el navegador...");
            } else {
                System.out.println("Abre manualmente: " + url);
            }
        } catch (Exception e) {
            System.err.println("Error al abrir navegador: " + e.getMessage());
            System.out.println("Abre manualmente: http://localhost:" + PORT);
        }
    }
    
    /**
     * Handler para archivos estáticos (HTML, JS, CSS)
     */
    class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Si es la raíz, servir index.html
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            // Obtener el recurso
            String resourcePath = "/co/edu/uniquindio/sameday/shipmentsuqsameday/webapp" + path;
            InputStream is = getClass().getResourceAsStream(resourcePath);
            
            if (is == null) {
                // Archivo no encontrado
                String response = "404 - Archivo no encontrado: " + path;
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            
            // Determinar Content-Type
            String contentType = getContentType(path);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            
            // CORS headers para desarrollo
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            // Leer el archivo
            byte[] content = is.readAllBytes();
            is.close();
            
            // Enviar respuesta
            exchange.sendResponseHeaders(200, content.length);
            OutputStream os = exchange.getResponseBody();
            os.write(content);
            os.close();
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=utf-8";
            if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
            if (path.endsWith(".css")) return "text/css; charset=utf-8";
            if (path.endsWith(".json")) return "application/json; charset=utf-8";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".svg")) return "image/svg+xml";
            return "text/plain";
        }
    }
    
    /**
     * Handler para recibir coordenadas desde JavaScript
     */
    class CoordinatesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // CORS preflight
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                String response = "{\"error\": \"Método no permitido\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(405, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            
            // Leer el body
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String body = br.lines().collect(Collectors.joining("\n"));
            
            System.out.println("📥 Coordenadas recibidas desde JavaScript:");
            System.out.println(body);
            
            try {
                // Parsear JSON manualmente (simple)
                Coordinates origin = parseCoordinate(body, "origin");
                Coordinates destination = null;
                
                // Destino es opcional
                try {
                    destination = parseCoordinate(body, "destination");
                } catch (Exception e) {
                    System.out.println("ℹ️  Destino no proporcionado (opcional)");
                }
                
                System.out.println("✓ Origen: " + origin);
                if (destination != null) {
                    System.out.println("✓ Destino: " + destination);
                }
                
                // Calcular métricas solo si hay destino
                String response;
                if (destination != null) {
                    double distance = origin.distanceTo(destination);
                    double cost = MapCalculator.calculateCost(origin, destination);
                    String time = MapCalculator.formatEstimatedTime(origin, destination);
                    boolean sameDay = MapCalculator.isSameDayDeliveryPossible(origin, destination);
                    
                    System.out.println("📊 Distancia: " + String.format("%.2f km", distance));
                    System.out.println("💰 Costo: $" + String.format("%,.0f COP", cost));
                    System.out.println("⏱️  Tiempo: " + time);
                    System.out.println("📦 Same-day: " + (sameDay ? "Sí" : "No"));
                    
                    response = String.format(
                        "{\"success\": true, \"distance\": %.2f, \"cost\": %.2f, \"time\": \"%s\", \"sameDay\": %b}",
                        distance, cost, time, sameDay
                    );
                } else {
                    response = "{\"success\": true, \"message\": \"Origen recibido (destino opcional)\"}";
                }
                
                System.out.println("────────────────────────────────────────");
                
                // Callback
                if (callback != null) {
                    callback.onCoordinatesReceived(origin, destination);
                }
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                
            } catch (Exception e) {
                System.err.println("❌ Error al procesar coordenadas: " + e.getMessage());
                e.printStackTrace();
                String response = "{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(400, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
        
        private Coordinates parseCoordinate(String json, String key) {
            // Parseo simple de JSON (evitamos dependencias)
            int start = json.indexOf("\"" + key + "\"");
            if (start == -1) {
                throw new RuntimeException("No se encontró " + key + " en JSON");
            }
            
            // Verificar si es null
            int colonPos = json.indexOf(":", start);
            String valueCheck = json.substring(colonPos + 1, Math.min(colonPos + 10, json.length())).trim();
            if (valueCheck.startsWith("null")) {
                return null;
            }
            
            int latStart = json.indexOf("\"lat\"", start);
            if (latStart == -1) {
                throw new RuntimeException("No se encontró 'lat' en " + key);
            }
            
            int latColon = json.indexOf(":", latStart);
            int latEnd = json.indexOf(",", latColon);
            if (latEnd == -1) latEnd = json.indexOf("}", latColon);
            String latStr = json.substring(latColon + 1, latEnd).trim();
            
            int lngStart = json.indexOf("\"lng\"", start);
            if (lngStart == -1) {
                throw new RuntimeException("No se encontró 'lng' en " + key);
            }
            
            int lngColon = json.indexOf(":", lngStart);
            int lngEnd = json.indexOf("}", lngColon);
            if (lngEnd == -1) lngEnd = json.indexOf(",", lngColon);
            String lngStr = json.substring(lngColon + 1, lngEnd).trim();
            
            double lat = Double.parseDouble(latStr);
            double lng = Double.parseDouble(lngStr);
            
            return new Coordinates(lat, lng);
        }
    }
    
    /**
     * Main para ejecutar el servidor standalone
     */
    public static void main(String[] args) {
        try {
            MapWebServer server = new MapWebServer((origin, destination) -> {
                System.out.println("\n🎯 CALLBACK EJECUTADO EN JAVA:");
                System.out.println("Origen: " + origin);
                System.out.println("Destino: " + destination);
                System.out.println("Aquí puedes integrar con tu lógica de negocio\n");
            });
            
            server.start();
            server.openInBrowser();
            
            System.out.println("\nPresiona Enter para detener el servidor...");
            System.in.read();
            
            server.stop();
            
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
