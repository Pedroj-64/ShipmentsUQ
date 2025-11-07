package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

import java.io.File;
import java.time.LocalDateTime;

/**
 * Interfaz para la generación de reportes
 */
public interface IReport {
    /**
     * Genera un reporte en formato PDF
     * @param startDate fecha inicial del reporte
     * @param endDate fecha final del reporte
     * @return archivo PDF generado
     */
    File generatePDF(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Genera un reporte en formato CSV
     * @param startDate fecha inicial del reporte
     * @param endDate fecha final del reporte
     * @return archivo CSV generado
     */
    File generateCSV(LocalDateTime startDate, LocalDateTime endDate);
}