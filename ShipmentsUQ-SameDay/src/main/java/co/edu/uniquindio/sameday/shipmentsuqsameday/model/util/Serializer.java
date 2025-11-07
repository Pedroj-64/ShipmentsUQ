package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Clase utilitaria para serializar y deserializar objetos.
 * Permite guardar el estado de la aplicación en un archivo y recuperarlo posteriormente.
 */
public class Serializer {

    /** Directorio donde se guardarán los archivos de serialización */
    private static final String DATA_DIR = "data";
    
    /** Extensión de archivo para archivos de datos serializados */
    private static final String DATA_EXTENSION = ".dat";

    /**
     * Guarda el estado de un objeto en un archivo.
     * 
     * @param objeto El objeto a serializar.
     * @param nombreArchivo El nombre del archivo donde se guardará el objeto (sin extensión).
     * @throws IOException Si ocurre un error durante la escritura del archivo.
     */
    public static void guardarEstado(Object objeto, String nombreArchivo) throws IOException {
        if (objeto == null) {
            throw new IllegalArgumentException("No se puede serializar un objeto nulo");
        }
        
        // Asegurar que existe el directorio de datos
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            boolean creado = dataDir.mkdir();
            if (!creado) {
                throw new IOException("No se pudo crear el directorio de datos: " + dataDir.getAbsolutePath());
            }
        }
        
        // Crear la ruta completa del archivo
        File archivo = new File(dataDir, nombreArchivo + DATA_EXTENSION);
        System.out.println("Guardando el archivo en: " + archivo.getAbsolutePath());
        
        // Serializar el objeto
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(objeto);
            System.out.println("Objeto serializado correctamente: " + objeto.getClass().getName());
            
            // Verificar que el archivo se creó y tiene tamaño
            if (archivo.exists() && archivo.length() > 0) {
                System.out.println("Archivo creado exitosamente con tamaño: " + archivo.length() + " bytes");
            } else {
                throw new IOException("El archivo no se creó correctamente");
            }
        }
    }

    /**
     * Carga el estado de un objeto desde un archivo.
     * 
     * @param nombreArchivo El nombre del archivo desde donde se cargará el objeto (sin extensión).
     * @return El objeto deserializado.
     * @throws IOException Si ocurre un error durante la lectura del archivo.
     * @throws ClassNotFoundException Si no se encuentra la clase del objeto deserializado.
     */
    public static Object cargarEstado(String nombreArchivo) throws IOException, ClassNotFoundException {
        // Crear la ruta completa del archivo
        File archivo = new File(DATA_DIR, nombreArchivo + DATA_EXTENSION);
        System.out.println("Cargando el archivo desde: " + archivo.getAbsolutePath());
        
        // Verificar si el archivo existe
        if (!archivo.exists()) {
            throw new IOException("El archivo no existe: " + archivo.getAbsolutePath());
        }
        
        // Verificar que el archivo tiene tamaño
        if (archivo.length() == 0) {
            throw new IOException("El archivo está vacío: " + archivo.getAbsolutePath());
        }
        
        System.out.println("Tamaño del archivo a cargar: " + archivo.length() + " bytes");
        
        // Deserializar el objeto
        Object objeto = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            objeto = ois.readObject();
            System.out.println("Objeto deserializado correctamente: " + 
                (objeto != null ? objeto.getClass().getName() : "null"));
            return objeto;
        } catch (Exception e) {
            System.err.println("Error al deserializar el objeto: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Verifica si existe un archivo de datos para un nombre determinado.
     * 
     * @param nombreArchivo El nombre del archivo a verificar (sin extensión).
     * @return true si el archivo existe, false en caso contrario.
     */
    public static boolean existeArchivo(String nombreArchivo) {
        File archivo = new File(DATA_DIR, nombreArchivo + DATA_EXTENSION);
        return archivo.exists();
    }
    
    /**
     * Elimina un archivo de datos si existe.
     * 
     * @param nombreArchivo El nombre del archivo a eliminar (sin extensión).
     * @return true si el archivo fue eliminado exitosamente, false en caso contrario.
     */
    public static boolean eliminarArchivo(String nombreArchivo) {
        File archivo = new File(DATA_DIR, nombreArchivo + DATA_EXTENSION);
        if (archivo.exists()) {
            return archivo.delete();
        }
        return false;
    }
}