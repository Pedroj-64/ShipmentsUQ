package co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums;

/**
 * Métodos de pago disponibles en el sistema
 */
public enum PaymentMethod {
    CASH,             // Pago en efectivo al momento de la entrega
    CREDIT_CARD,      // Pago con tarjeta de crédito
    DEBIT_CARD,       // Pago con tarjeta de débito
    PSE,              // Pago por PSE (débito bancario)
    NEQUI,            // Pago por Nequi
    DAVIPLATA         // Pago por Daviplata
}