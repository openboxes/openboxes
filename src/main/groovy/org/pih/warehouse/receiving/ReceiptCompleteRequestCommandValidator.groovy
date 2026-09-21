package org.pih.warehouse.receiving

import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.core.validation.ObjectValidator

@Component
class ReceiptCompleteRequestCommandValidator extends ObjectValidator<ReceiptCompleteRequestCommand> {

    @Override
    protected ObjectValidationResult doValidate(ReceiptCompleteRequestCommand command) {
        return new ObjectValidationResult(
                validateReceiptIsPending(command),
                validateSomethingWasReceived(command),
                validateItemsToCompleteAreValid(command),
                validateNoDuplicateItemsToComplete(command),
                validateItemsToCompleteBelongToReceipt(command),
                validateCancelRemainingOnlyOnOriginalItems(command),
                validateBinLocationIsPresent(command),
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
     * A receipt must have received something to be completed: at least one of its lines with a quantity received
     * above zero. Completing one that received nothing would record an inbound transaction carrying no entries at
     * all (see {@link ReceiptTransactionManager#createInboundTransaction}).
     */
    private ObjectError validateSomethingWasReceived(ReceiptCompleteRequestCommand command) {
        if (!command.receipt) {
            return null
        }

        // The same lines the transaction credits: a null quantity received is a line that was never given one.
        boolean nothingReceived = !(command.receipt.receiptItems ?: []).any { ReceiptItem receiptItem ->
            (receiptItem.quantityReceived ?: 0) > 0
        }

        return nothingReceived ?
                rejectField("receipt", command.receipt, "receiptCompleteRequestCommand.receipt.nothingReceived",
                        [command.receipt.receiptNumber]) :
                null
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

    /**
     * Every line the completion moves stock on has to carry a bin location when the receipt is received into a
     * destination that tracks bin locations (Location.hasBinLocationSupport) - its stock would otherwise land outside
     * of any bin. Lines that received nothing record no transaction entry, so they are not checked, and neither are
     * the lines of a destination that holds no bins.
     *
     * The receiving page fills the bins in before it lets the user reach the completion, so this guards the API.
     */
    private ObjectError validateBinLocationIsPresent(ReceiptCompleteRequestCommand command) {
        if (!command.receipt) {
            return null
        }

        // The same lines the transaction credits (see ReceiptTransactionManager#createInboundTransaction).
        List<String> itemIds = (command.receipt.receiptItems ?: [])
                .findAll { ReceiptItem receiptItem ->
                    (receiptItem.quantityReceived ?: 0) > 0 && receiptItem.binLocation == null
                }
                .collect { ReceiptItem receiptItem -> receiptItem.id }

        // The lines are checked first so that a receipt that carries a bin location on each of them doesn't load the
        // shipment and its destination.
        if (!itemIds) {
            return null
        }

        Location destination = command.receipt.shipment?.destination

        return destination?.hasBinLocationSupport() ?
                rejectField("receipt", command.receipt,
                        "receiptCompleteRequestCommand.receipt.binLocationMissing", [itemIds.toString()]) :
                null
    }
}