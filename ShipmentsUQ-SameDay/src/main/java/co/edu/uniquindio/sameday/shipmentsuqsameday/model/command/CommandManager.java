package co.edu.uniquindio.sameday.shipmentsuqsameday.model.command;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.Command;

import java.util.Stack;

/**
 * Gestor de comandos que maneja la ejecución, deshacer y rehacer de operaciones
 * Implementa el patrón Command para proporcionar funcionalidad de deshacer/rehacer
 */
public class CommandManager {
    private Stack<Command> undoStack = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();
    
    // Singleton para acceso global
    private static CommandManager instance;
    
    /**
     * Constructor privado para patrón Singleton
     */
    private CommandManager() {
    }
    
    /**
     * Obtiene la instancia única del CommandManager
     * @return instancia del CommandManager
     */
    public static synchronized CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }
        return instance;
    }
    
    /**
     * Ejecuta un comando y lo añade a la pila de deshacer
     * @param command Comando a ejecutar
     */
    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); 
    }
    
    /**
     * Deshace el último comando ejecutado si es posible
     * @return true si se deshizo el comando, false en caso contrario
     */
    public boolean undoLastCommand() {
        if (undoStack.isEmpty()) {
            return false;
        }
        
        Command command = undoStack.pop();
        boolean result = command.undo();
        if (result) {
            redoStack.push(command);
        } else {
            undoStack.push(command);
        }
        return result;
    }
    
    /**
     * Rehace el último comando deshecho
     * @return true si se rehizo el comando, false en caso contrario
     */
    public boolean redoLastCommand() {
        if (redoStack.isEmpty()) {
            return false;
        }
        
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        return true;
    }
    
    /**
     * Verifica si hay comandos que se pueden deshacer
     * @return true si hay comandos en la pila de deshacer, false en caso contrario
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * Verifica si hay comandos que se pueden rehacer
     * @return true si hay comandos en la pila de rehacer, false en caso contrario
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * Limpia las pilas de deshacer y rehacer
     */
    public void clearStacks() {
        undoStack.clear();
        redoStack.clear();
    }
}