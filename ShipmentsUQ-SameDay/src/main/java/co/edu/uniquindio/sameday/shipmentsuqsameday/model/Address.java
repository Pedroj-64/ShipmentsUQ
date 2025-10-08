package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Clase que representa una dirección en el sistema
 * Utiliza coordenadas cartesianas (X, Y) en lugar de coordenadas geográficas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private UUID id;
    private String alias;
    private String street;
    private String zone;    // Zona o sector de la ciudad
    private String city;
    private String zipCode;
    private String complement;
    private double coordX;  // Coordenada X en el mapa de cuadrícula
    private double coordY;  // Coordenada Y en el mapa de cuadrícula
    private boolean isDefault;
}