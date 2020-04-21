package org.pih.warehouse.core

enum PaymentMethodTypeCode {

    BANK_TRANSFER,      // Wire Transfer
    CASH,
    CHECK,
    DEBIT_CARD,
    CREDIT_CARD,        // Visa, MasterCard
    DIGITAL_WALLET,     // Paypal, Bitcoin
    DIRECT_DEBIT,       // SERP Direct Debit, SEPA Credit Transfer
    ELECTRONIC_CHECK,   // ACH
    MOBILE_PAYMENT,     // Apple Pay, Google Pay, Venmo
    MONEY_ORDER

}
