package org.pih.warehouse.receiving

import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.core.validation.ObjectValidator

@Component
class ReceiptEditReceivingInfoCommandValidator implements ObjectValidator<ReceiptEditReceivingInfoCommand> {

    @Override
    ObjectValidationResult doValidate(ReceiptEditReceivingInfoCommand command) {
        return new ObjectValidationResult(
                validateReceiptIsPending(command),
                validateItemsToSaveAreValid(command),
                validateNoDuplicateItemsToSave(command),
        )
    }

    /**
     * The receipt (bound from the URL) must be pending for its receipt items to be editable.
     */
    private ObjectError validateReceiptIsPending(ReceiptEditReceivingInfoCommand command) {
        // A missing receipt is already reported by the command's nullable constraint and the validator still runs
        // after that failure (there is no short-circuit), so guard against NPE before checking the status.
        if (!command.receipt) {
            return null
        }

        if (command.receipt.receiptStatusCode != ReceiptStatusCode.PENDING) {
            return rejectField("receipt", command.receipt, "receiptEditReceivingInfoCommand.receipt.notPending",
                    [command.receipt.receiptNumber])
        }

        return null
    }

    /**
     * Elements of a list are not validated by default, so manually validate every element in the list. If any of the
     * elements have validation errors, propagate the failure up to the command.
     */
    private ObjectError validateItemsToSaveAreValid(ReceiptEditReceivingInfoCommand command) {
        command.itemsToSave.each { ReceiptItemEditReceivingInfoRequest item -> item.validate() }

        return command.itemsToSave.any { it.hasErrors() } ?
                rejectField("itemsToSave", command.itemsToSave,
                        "receiptEditReceivingInfoCommand.itemsToSave.invalid") :
                null
    }

    /**
     * The same existing receipt item must not be saved more than once in a single request.
     * New items (receiptItem == null) are not considered duplicates.
     */
    private ObjectError validateNoDuplicateItemsToSave(ReceiptEditReceivingInfoCommand command) {
        List<String> duplicateIds = command.itemsToSave
                .findAll { it.receiptItem != null }
                .groupBy { it.receiptItem.id }
                .findAll { it.value.size() > 1 }
                .keySet()
                .toList()

        return duplicateIds ?
                rejectField("itemsToSave", command.itemsToSave,
                        "receiptEditReceivingInfoCommand.itemsToSave.duplicateExists", [duplicateIds.toString()]) :
                null
    }
}
