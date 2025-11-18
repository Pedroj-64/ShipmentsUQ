package co.edu.uniquindio.sameday.shipmentsuqsameday.mapping;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio de geocodificación inversa usando Nominatim (OpenStreetMap)
 * Convierte coordenadas GPS (latitud, longitud) en direcciones legibles
 * 
 * 
 */
public class ReverseGeocoder {
    
    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/reverse";
    private static final String USER_AGENT = "ShipmentsUQ/1.0 (Educational Project)";
    private static final int TIMEOUT_MS = 5000;
    
    // Control de rate limiting (1 request/segundo)
    private long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL_MS = 1100; // 1.1 segundos para estar seguros
    
    private static ReverseGeocoder instance;
    
    private ReverseGeocoder() {
        // Singleton
    }
    
    /**
     * Obtiene la instancia única del geocodificador
     */
    public static synchronized ReverseGeocoder getInstance() {
        if (instance == null) {
            instance = new ReverseGeocoder();
        }
        return instance;
    }
    
    /**
     * Convierte coordenadas GPS en una dirección legible
     * @param latitude latitud
     * @param longitude longitud
     * @return Mapa con componentes de la dirección, o null si falla
     */
    public Map<String, String> reverseGeocode(double latitude, double longitude) {
        try {
            // Validar coordenadas
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                System.err.println("[ERROR] Coordenadas inválidas: lat=" + latitude + ", lon=" + longitude);
                return null;
            }
            
            System.out.println("[DEBUG] Validación de coordenadas:");
            System.out.println("   Latitud:  " + latitude + " (válida: " + (latitude >= -90 && latitude <= 90) + ")");
            System.out.println("   Longitud: " + longitude + " (válida: " + (longitude >= -180 && longitude <= 180) + ")");
            
            // Respetar rate limiting
            enforceRateLimit();
            
            // Construir URL con formato Locale.US para evitar problemas con coma decimal
            String urlString = String.format(
                Locale.US,
                "%s?lat=%.6f&lon=%.6f&format=json&addressdetails=1&accept-language=es",
                NOMINATIM_API_URL,
                latitude,
                longitude
            );
            
            System.out.println("[INFO] URL construida: " + urlString);
            
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            
            int responseCode = conn.getResponseCode();
            System.out.println("[INFO] Código de respuesta HTTP: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                );
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                
                System.out.println("[SUCCESS] Respuesta JSON recibida (primeros 200 chars): " + 
                    response.toString().substring(0, Math.min(200, response.length())));
                
                // Parsear respuesta JSON manualmente (sin dependencias externas)
                return parseNominatimResponse(response.toString());
                
            } else if (responseCode == 429) {
                System.err.println("[WARN] Rate limit excedido en Nominatim. Esperando...");
                Thread.sleep(2000);
                return null;
            } else {
                System.err.println("[ERROR] Error en geocodificación inversa: HTTP " + responseCode);
                
                // Intentar leer el mensaje de error
                try {
                    BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8)
                    );
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorReader.close();
                    System.err.println("[INFO] Mensaje de error: " + errorResponse.toString());
                } catch (Exception ignored) {
                    // Si no se puede leer el error, continuar
                }
                
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("[ERROR] Error al obtener dirección desde GPS: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Parsea la respuesta JSON de Nominatim
     * Implementación manual para evitar dependencia de librerías JSON
     */
    private Map<String, String> parseNominatimResponse(String json) {
        Map<String, String> address = new HashMap<>();
        
        try {
            // Extraer display_name (dirección completa)
            String displayName = extractJsonField(json, "display_name");
            if (displayName != null) {
                address.put("fullAddress", displayName);
            }
            
            // Buscar el objeto "address" dentro del JSON
            int addressStart = json.indexOf("\"address\":");
            if (addressStart != -1) {
                int braceStart = json.indexOf("{", addressStart);
                int braceEnd = findMatchingBrace(json, braceStart);
                String addressJson = json.substring(braceStart, braceEnd + 1);
                
                // Extraer componentes de dirección
                address.put("road", extractJsonField(addressJson, "road"));
                address.put("houseNumber", extractJsonField(addressJson, "house_number"));
                address.put("neighbourhood", extractJsonField(addressJson, "neighbourhood"));
                address.put("suburb", extractJsonField(addressJson, "suburb"));
                address.put("city", extractJsonField(addressJson, "city"));
                address.put("municipality", extractJsonField(addressJson, "municipality"));
                address.put("state", extractJsonField(addressJson, "state"));
                address.put("postcode", extractJsonField(addressJson, "postcode"));
                address.put("country", extractJsonField(addressJson, "country"));
            }
            
        } catch (Exception e) {
            System.err.println("Error al parsear respuesta JSON: " + e.getMessage());
        }
        
        return address;
    }
    
    /**
     * Extrae el valor de un campo JSON
     */
    private String extractJsonField(String json, String fieldName) {
        try {
            Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            // Ignorar
        }
        return null;
    }
    
    /**
     * Encuentra la llave de cierre correspondiente
     */
    private int findMatchingBrace(String json, int start) {
        int count = 1;
        for (int i = start + 1; i < json.length(); i++) {
            if (json.charAt(i) == '{') count++;
            if (json.charAt(i) == '}') count--;
            if (count == 0) return i;
        }
        return json.length() - 1;
    }
    
    /**
     * Formatea una dirección desde los componentes extraídos
     * @param components mapa con los componentes de la dirección
     * @return dirección formateada en formato colombiano
     */
    public String formatColombianAddress(Map<String, String> components) {
        if (components == null || components.isEmpty()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        
        // Intentar construir: Calle/Carrera + Número + Barrio
        String road = components.get("road");
        String houseNumber = components.get("houseNumber");
        String neighbourhood = components.get("neighbourhood");
        String suburb = components.get("suburb");
        String city = components.get("city");
        String municipality = components.get("municipality");
        
        if (road != null) {
            sb.append(road);
            if (houseNumber != null) {
                sb.append(" #").append(houseNumber);
            }
        }
        
        if (neighbourhood != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(neighbourhood);
        } else if (suburb != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(suburb);
        }
        
        if (city != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        } else if (municipality != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(municipality);
        }
        
        return sb.length() > 0 ? sb.toString() : components.get("fullAddress");
    }
    
    /**
     * Aplica rate limiting para respetar las políticas de Nominatim
     */
    private void enforceRateLimit() throws InterruptedException {
        long now = System.currentTimeMillis();
        long timeSinceLastRequest = now - lastRequestTime;
        
        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
            long sleepTime = MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest;
            System.out.println("[INFO] Esperando " + sleepTime + "ms (rate limiting)...");
            Thread.sleep(sleepTime);
        }
        
        lastRequestTime = System.currentTimeMillis();
    }
    
    /**
     * Método de conveniencia que devuelve una dirección formateada directamente
     */
    public String getFormattedAddress(double latitude, double longitude) {
        Map<String, String> components = reverseGeocode(latitude, longitude);
        if (components != null) {
            return formatColombianAddress(components);
        }
        return null;
    }
}
