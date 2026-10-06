package org.pih.warehouse.receiving

import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.core.validation.ObjectValidatable

class ReceiptItemsBatchRequest implements ObjectValidatable<ReceiptItemsBatchRequestValidator> {

    // Bound from the URL path variable in beforeValidate(), not from the request body (the data binding source only
    // contains the JSON body, not the URL path variables). Required (the default for command object properties), so a
    // missing/unknown receiptId yields the conventional "cannot be null" validation error.
    Receipt receipt

    // Receipt items to create (receiptItem == null) or update (receiptItem != null).
    List<ReceiptItemUpsertRequest> itemsToSave = []

    // Ids of existing receipt items to delete.
    List<String> itemsToDelete = []

    def beforeValidate() {
        Map<String, Object> params = RequestContextHolder.getRequestAttributes().params
        receipt = Receipt.get(params?.receiptId)
    }
}
