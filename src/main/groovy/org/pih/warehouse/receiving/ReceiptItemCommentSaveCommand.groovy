package org.pih.warehouse.receiving

import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.core.validation.ObjectValidatable

/**
 * The request body for setting the comment of a single receipt item. Holds everything shared between
 * adding and editing a comment - the concrete subclasses only differ in the validator they declare.
 *
 * The receipt item is identified by the URL path rather than the request body, so it is bound in
 * {@link #beforeValidate} (the data binding source only contains the JSON body, not the URL path variables).
 * It is required (the default for command object properties), so a missing/unknown identifier yields the
 * conventional "cannot be null" validation error.
 */
abstract class ReceiptItemCommentSaveCommand implements ObjectValidatable {

    // Bound from the URL path variables in beforeValidate(), not from the request body.
    ReceiptItem receiptItem

    String comment

    def beforeValidate() {
        Map<String, Object> params = RequestContextHolder.getRequestAttributes().params
        receiptItem = ReceiptItem.get(params?.receiptItemId)
    }

    static constraints = {
        // Mirrors the constraints of the comment field of the ReceiptItem domain
        comment(blank: false, maxSize: 255)
    }
}
