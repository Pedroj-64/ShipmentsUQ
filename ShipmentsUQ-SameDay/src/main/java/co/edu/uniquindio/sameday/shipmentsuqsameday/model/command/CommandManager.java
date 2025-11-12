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
     * @param command comando a ejecutar
     */
    public void executeCommand(Command command) {
        System.out.println("\n>>> CommandManager.executeCommand() <<<");
        System.out.println("Comando a ejecutar: " + command.getClass().getSimpleName());
        System.out.println("Stack undo antes: " + undoStack.size() + " comandos");
        System.out.println("Stack redo antes: " + redoStack.size() + " comandos");
        
        command.execute();
        undoStack.push(command);
        redoStack.clear();
        
        System.out.println("Stack undo después: " + undoStack.size() + " comandos");
        System.out.println("Stack redo después: " + redoStack.size() + " comandos");
        System.out.println(">>> Fin executeCommand() <<<\n");
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
     * @return true si hay comandos en la pila de deshacer
     */
    public boolean canUndo() {
        boolean result = !undoStack.isEmpty();
        System.out.println("CommandManager.canUndo() = " + result + " (stack size: " + undoStack.size() + ")");
        return result;
    }
    
    /**
     * Verifica si hay comandos que se pueden rehacer
     * @return true si hay comandos en la pila de rehacer
     */
    public boolean canRedo() {
        boolean result = !redoStack.isEmpty();
        System.out.println("CommandManager.canRedo() = " + result + " (stack size: " + redoStack.size() + ")");
        return result;
    }
    
    /**
     * Limpia las pilas de deshacer y rehacer
     */
    public void clearStacks() {
        undoStack.clear();
        redoStack.clear();
    }
}