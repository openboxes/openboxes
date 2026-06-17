package org.pih.warehouse.receiving

import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.core.validation.ObjectValidator

@Component
class ReceiptItemsBatchRequestValidator implements ObjectValidator<ReceiptItemsBatchRequest> {

    @Override
    ObjectValidationResult doValidate(ReceiptItemsBatchRequest request) {
        return new ObjectValidationResult(
                validateItemsToSaveAreValid(request),
                validateNoDuplicateItemsToSave(request),
                validateItemsAreNotBothSavedAndDeleted(request),
        )
    }

    /**
     * Elements of a list are not validated by default, so manually validate every element in the list. If any of the
     * elements have validation errors, propagate the failure up to the batch request.
     */
    private ObjectError validateItemsToSaveAreValid(ReceiptItemsBatchRequest request) {
        request.itemsToSave.each { ReceiptItemRequest item -> item.validate() }

        return request.itemsToSave.any { it.hasErrors() } ?
                rejectField("itemsToSave", request.itemsToSave, "receiptItemsBatchRequest.itemsToSave.invalid") :
                null
    }

    /**
     * The same existing receipt item must not be saved more than once in a single batch.
     * New items (receiptItem == null) are not considered duplicates.
     */
    private ObjectError validateNoDuplicateItemsToSave(ReceiptItemsBatchRequest request) {
        List<String> duplicateIds = request.itemsToSave
                .findAll { it.receiptItem != null }
                .groupBy { it.receiptItem.id }
                .findAll { it.value.size() > 1 }
                .keySet()
                .toList()

        return duplicateIds ?
                rejectField("itemsToSave", request.itemsToSave,
                        "receiptItemsBatchRequest.itemsToSave.duplicateExists", [duplicateIds.toString()]) :
                null
    }

    /**
     * A receipt item must not be both saved and deleted in the same batch.
     */
    private ObjectError validateItemsAreNotBothSavedAndDeleted(ReceiptItemsBatchRequest request) {
        List<String> savedIds = request.itemsToSave
                .findAll { it.receiptItem != null }
                .collect { it.receiptItem.id }
        List<String> overlappingIds = request.itemsToDelete.intersect(savedIds)

        return overlappingIds ?
                rejectField("itemsToDelete", request.itemsToDelete,
                        "receiptItemsBatchRequest.itemsToDelete.savedAndDeleted", [overlappingIds.toString()]) :
                null
    }
}
