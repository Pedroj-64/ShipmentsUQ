package co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums;

/**
 * Roles de usuario en el sistema que pueden iniciar sesión
 * 
 * Nota: Aunque DELIVERER existe como un tipo de entidad en el sistema,
 * estos no tienen acceso al login. Solo los administradores y clientes
 * pueden iniciar sesión en la aplicación. El administrador es responsable
 * de gestionar los repartidores y asignarlos a zonas.
 */
public enum UserRole {
    CLIENT,        // Usuario normal que puede crear envíos
    DELIVERER,     // Usuario repartidor que realiza las entregas (no tiene acceso al login)
    ADMIN          // Administrador del sistema
}