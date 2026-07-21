package org.pih.warehouse.receiving

import grails.validation.Validateable

/**
 * A single line of a {@link ReceiptCompleteRequestCommand}: the completion options of one receipt item.
 */
class ReceiptItemCompleteRequest implements Validateable {

    // Existing receipt item of the receipt being completed, bound by id from the request body. Required (the default
    // for command object properties), so a missing/unknown identifier yields the conventional "cannot be null" error.
    ReceiptItem receiptItem

    // When true, the quantity that is still left to receive on the line's shipment item is canceled on completion,
    // which counts towards the shipment item (and so the shipment) being fully received.
    Boolean cancelRemaining = Boolean.FALSE

    static constraints = {
        cancelRemaining(nullable: true)
    }
}
