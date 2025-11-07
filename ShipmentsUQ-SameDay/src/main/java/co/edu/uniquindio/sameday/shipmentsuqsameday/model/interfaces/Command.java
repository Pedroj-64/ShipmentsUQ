package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

/**
 * Interfaz que define la estructura para el patrón Command
 * Esta interfaz permite encapsular una acción como un objeto,
 * lo que facilita operaciones como deshacer/rehacer y el registro
 * de las acciones realizadas en el sistema.
 */
public interface Command {
    
    /**
     * Ejecuta el comando
     * Este método contiene la lógica principal del comando
     */
    void execute();
    
    /**
     * Deshace la ejecución del comando, restaurando el estado anterior
     * @return true si se pudo deshacer la operación, false en caso contrario
     */
    boolean undo();
    
    /**
     * Verifica si el comando puede deshacerse
     * @return true si el comando puede deshacerse, false en caso contrario
     */
    boolean isUndoable();
}
