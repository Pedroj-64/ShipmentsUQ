package co.edu.uniquindio.sameday.shipmentsuqsameday.webapp.api;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.LoginController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.RegisterController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para autenticación y registro de usuarios
 * Reutiliza los controladores existentes de la aplicación JavaFX
 */
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {
    
    private final LoginController loginController;
    private final RegisterController registerController;
    
    public AuthRestController() {
        this.loginController = new LoginController();
        this.registerController = new RegisterController();
    }
    
    /**
     * Endpoint para iniciar sesión
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Object result = loginController.authenticateAny(request.getEmail(), request.getPassword());
            
            if (result == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales inválidas"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Inicio de sesión exitoso");
            
            if (result instanceof User) {
                User user = (User) result;
                response.put("userType", "USER");
                response.put("user", createUserResponse(user));
            } else if (result instanceof Deliverer) {
                Deliverer deliverer = (Deliverer) result;
                response.put("userType", "DELIVERER");
                response.put("user", createDelivererResponse(deliverer));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error en el servidor: " + e.getMessage()));
        }
    }
    
    /**
     * Endpoint para registrar un nuevo usuario
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest request) {
        try {
            User user = registerController.registerUser(
                    request.getName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getPassword(),
                    request.getCity()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario registrado exitosamente");
            response.put("user", createUserResponse(user));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar usuario: " + e.getMessage()));
        }
    }
    
    /**
     * Endpoint para registrar un nuevo repartidor
     * POST /api/auth/register/deliverer
     */
    @PostMapping("/register/deliverer")
    public ResponseEntity<?> registerDeliverer(@RequestBody RegisterDelivererRequest request) {
        try {
            Deliverer deliverer = registerController.registerDeliverer(
                    request.getName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getPassword(),
                    request.getCity(),
                    request.getDocument(),
                    request.getZone()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Repartidor registrado exitosamente");
            response.put("deliverer", createDelivererResponse(deliverer));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar repartidor: " + e.getMessage()));
        }
    }
    
    /**
     * Endpoint de verificación del servidor
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ShipmentsUQ API",
                "version", "1.0.0"
        ));
    }
    
    // DTOs para las requests
    
    public static class LoginRequest {
        private String email;
        private String password;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class RegisterUserRequest {
        private String name;
        private String email;
        private String phone;
        private String password;
        private String city;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }
    
    public static class RegisterDelivererRequest extends RegisterUserRequest {
        private String document;
        private String zone;
        
        public String getDocument() { return document; }
        public void setDocument(String document) { this.document = document; }
        public String getZone() { return zone; }
        public void setZone(String zone) { this.zone = zone; }
    }
    
    // Métodos helper para crear respuestas
    
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId().toString());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("phone", user.getPhone());
        userMap.put("role", user.getRole().toString());
        return userMap;
    }
    
    private Map<String, Object> createDelivererResponse(Deliverer deliverer) {
        Map<String, Object> delivererMap = new HashMap<>();
        delivererMap.put("id", deliverer.getId().toString());
        delivererMap.put("name", deliverer.getName());
        delivererMap.put("phone", deliverer.getPhone());
        delivererMap.put("document", deliverer.getDocument());
        delivererMap.put("zone", deliverer.getZone());
        delivererMap.put("status", deliverer.getStatus().toString());
        delivererMap.put("averageRating", deliverer.getAverageRating());
        delivererMap.put("totalDeliveries", deliverer.getTotalDeliveries());
        return delivererMap;
    }
}
