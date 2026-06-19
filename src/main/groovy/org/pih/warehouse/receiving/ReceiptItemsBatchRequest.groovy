package org.pih.warehouse.receiving

import org.pih.warehouse.core.validation.ObjectValidatable

class ReceiptItemsBatchRequest implements ObjectValidatable<ReceiptItemsBatchRequestValidator> {

    // Receipt items to create (receiptItem == null) or update (receiptItem != null).
    List<ReceiptItemUpsertRequest> itemsToSave = []

    // Ids of existing receipt items to delete.
    List<String> itemsToDelete = []
}
