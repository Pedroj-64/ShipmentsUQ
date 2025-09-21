package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.IncidentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Clase que representa una incidencia en un envío
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {
    private UUID id;
    private Shipment shipment;
    private IncidentType type;
    private String description;
    private LocalDateTime date;
    private boolean resolved;
    private LocalDateTime resolutionDate;
    private String solution;
}