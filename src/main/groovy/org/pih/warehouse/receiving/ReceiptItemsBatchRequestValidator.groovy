package org.pih.warehouse.receiving

import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.core.validation.ObjectValidator

@Component
class ReceiptItemsBatchRequestValidator extends ObjectValidator<ReceiptItemsBatchRequest> {

    @Override
    protected ObjectValidationResult doValidate(ReceiptItemsBatchRequest request) {
        return new ObjectValidationResult(
                validateReceiptIsPending(request),
                validateItemsToSaveAreValid(request),
                validateNoDuplicateItemsToSave(request),
                validateItemsAreNotBothSavedAndDeleted(request),
                validateItemsToDeleteAreNotOriginalItems(request),
        )
    }

    /**
     * The receipt (bound from the URL) must be pending for its receipt items to be editable.
     */
    private ObjectError validateReceiptIsPending(ReceiptItemsBatchRequest request) {
        // A missing receipt is already reported by the request's nullable constraint and the validator still runs
        // after that failure (there is no short-circuit), so guard against NPE before checking the status.
        if (!request.receipt) {
            return null
        }

        if (request.receipt.receiptStatusCode != ReceiptStatusCode.PENDING) {
            return rejectField("receipt", request.receipt, "receiptItemsBatchRequest.receipt.notPending",
                    [request.receipt.receiptNumber])
        }

        return null
    }

    /**
     * Elements of a list are not validated by default, so manually validate every element in the list. If any of the
     * elements have validation errors, propagate the failure up to the batch request.
     */
    private ObjectError validateItemsToSaveAreValid(ReceiptItemsBatchRequest request) {
        request.itemsToSave.each { ReceiptItemUpsertRequest item -> item.validate() }

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

    /**
     * Original receipt items (the per-shipment-item lines created when the receipt was started, flagged
     * isSplitItem: false) must never be deleted - they are what the cancel-remaining logic runs against when the
     * receipt is completed. Only split lines added while receiving can be deleted. Identifiers that don't resolve
     * to an existing item are ignored here - the service reports them when it performs the delete.
     */
    private ObjectError validateItemsToDeleteAreNotOriginalItems(ReceiptItemsBatchRequest request) {
        if (!request.itemsToDelete) {
            return null
        }

        // A single projection query instead of a per-id ReceiptItem.get(): one round trip for the whole batch, and
        // the items stay out of the Hibernate session - the service is about to delete these very entities.
        List<String> originalItemIds = ReceiptItem.createCriteria().list {
            projections {
                property("id")
            }
            inList("id", request.itemsToDelete)
            eq("isSplitItem", false)
        }

        return originalItemIds ?
                rejectField("itemsToDelete", request.itemsToDelete,
                        "receiptItemsBatchRequest.itemsToDelete.originalItem", [originalItemIds.toString()]) :
                null
    }
}
