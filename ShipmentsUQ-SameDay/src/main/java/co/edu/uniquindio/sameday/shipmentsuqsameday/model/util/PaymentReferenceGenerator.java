package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import java.util.UUID;

/**
 * Utilidad para generar referencias de pago
 */
public class PaymentReferenceGenerator {
    private static final String PREFIX = "PAY-";
    private static final int REFERENCE_LENGTH = 8;

    /**
     * Genera una referencia única de pago
     * @return referencia de pago en formato PAY-XXXXXXXX
     */
    public static String generateReference() {
        return PREFIX + UUID.randomUUID().toString()
                .substring(0, REFERENCE_LENGTH)
                .toUpperCase();
    }
}