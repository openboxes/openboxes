package org.pih.warehouse.receiving

import java.time.Instant

import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.core.validation.ObjectValidatable

/**
 * The request body for completing a pending receipt.
 *
 * The receipt is identified by the URL path rather than the request body, so it is bound in {@link #beforeValidate}
 * (the data binding source only contains the JSON body, not the URL path variables). It is required (the default for
 * command object properties), so a missing/unknown identifier yields the conventional "cannot be null" validation
 * error.
 */
class ReceiptCompleteRequestCommand implements ObjectValidatable<ReceiptCompleteRequestCommandValidator> {

    // Bound from the URL path variable in beforeValidate(), not from the request body.
    Receipt receipt

    // The moment the shipment was delivered, stored as the receipt's actual delivery date. Optional - when absent,
    // the delivery date already carried by the receipt (defaulted when the receipt was started) is kept as is.
    Instant dateDelivered

    // Per-line completion options. Lines missing from the list are completed with their remaining quantity left
    // open (nothing gets canceled), so clients only need to send the lines they want to cancel.
    List<ReceiptItemCompleteRequest> itemsToComplete = []

    def beforeValidate() {
        Map<String, Object> params = RequestContextHolder.getRequestAttributes().params
        receipt = Receipt.get(params?.receiptId)
    }

    static constraints = {
        dateDelivered(nullable: true)
    }
}
