package org.pih.warehouse.receiving

import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.core.validation.ObjectValidator

@Component
class ReceiptCompleteRequestCommandValidator extends ObjectValidator<ReceiptCompleteRequestCommand> {

    @Override
    protected ObjectValidationResult doValidate(ReceiptCompleteRequestCommand command) {
        return new ObjectValidationResult(
                validateReceiptIsPending(command),
                validateItemsToCompleteAreValid(command),
                validateNoDuplicateItemsToComplete(command),
                validateItemsToCompleteBelongToReceipt(command),
                validateCancelRemainingOnlyOnOriginalItems(command),
        )
    }

    /**
     * The receipt (bound from the URL) must be pending to be completed.
     */
    private ObjectError validateReceiptIsPending(ReceiptCompleteRequestCommand command) {
        // A missing receipt is already reported by the command's nullable constraint and the validator still runs
        // after that failure (there is no short-circuit), so guard against NPE before checking the status.
        if (!command.receipt) {
            return null
        }

        if (command.receipt.receiptStatusCode != ReceiptStatusCode.PENDING) {
            return rejectField("receipt", command.receipt, "receiptCompleteRequestCommand.receipt.notPending",
                    [command.receipt.receiptNumber])
        }

        return null
    }

    /**
     * Elements of a list are not validated by default, so manually validate every element in the list. If any of the
     * elements have validation errors, propagate the failure up to the command.
     */
    private ObjectError validateItemsToCompleteAreValid(ReceiptCompleteRequestCommand command) {
        command.itemsToComplete.each { ReceiptItemCompleteRequest item -> item.validate() }

        return command.itemsToComplete.any { it.hasErrors() } ?
                rejectField("itemsToComplete", command.itemsToComplete,
                        "receiptCompleteRequestCommand.itemsToComplete.invalid") :
                null
    }

    /**
     * The same receipt item must not be completed more than once in a single request.
     */
    private ObjectError validateNoDuplicateItemsToComplete(ReceiptCompleteRequestCommand command) {
        List<String> duplicateIds = command.itemsToComplete
                .findAll { it.receiptItem != null }
                .groupBy { it.receiptItem.id }
                .findAll { it.value.size() > 1 }
                .keySet()
                .toList()

        return duplicateIds ?
                rejectField("itemsToComplete", command.itemsToComplete,
                        "receiptCompleteRequestCommand.itemsToComplete.duplicateExists", [duplicateIds.toString()]) :
                null
    }

    /**
     * Every receipt item in the request must belong to the receipt being completed, so that the request cannot
     * cancel quantities on the lines of another (potentially already received) receipt.
     */
    private ObjectError validateItemsToCompleteBelongToReceipt(ReceiptCompleteRequestCommand command) {
        if (!command.receipt) {
            return null
        }

        List<String> foreignIds = command.itemsToComplete
                .findAll { it.receiptItem != null && it.receiptItem.receiptId != command.receipt.id }
                .collect { it.receiptItem.id }

        return foreignIds ?
                rejectField("itemsToComplete", command.itemsToComplete,
                        "receiptCompleteRequestCommand.itemsToComplete.notOnReceipt", [foreignIds.toString()]) :
                null
    }

    /**
     * The cancel-remaining flag is only allowed on original lines (see {@link ReceiptItem#isOriginalLine}). Split
     * lines carry a quantity shipped of zero, so they have no remainder of their own to cancel - flagging one is a
     * client error.
     */
    private ObjectError validateCancelRemainingOnlyOnOriginalItems(ReceiptCompleteRequestCommand command) {
        List<String> flaggedSplitItemIds = command.itemsToComplete
                .findAll { it.receiptItem && it.cancelRemainingQuantity && !it.receiptItem.isOriginalLine() }
                .collect { it.receiptItem.id }

        return flaggedSplitItemIds ?
                rejectField("itemsToComplete", command.itemsToComplete,
                        "receiptCompleteRequestCommand.itemsToComplete.cancelRemainingOnSplitItem",
                        [flaggedSplitItemIds.toString()]) :
                null
    }
}