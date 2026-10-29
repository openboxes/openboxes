package org.pih.warehouse.receiving

import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.core.validation.ObjectValidator

@Component
class ReceiptEditReceivingInfoCommandValidator extends ObjectValidator<ReceiptEditReceivingInfoCommand> {

    @Override
    protected ObjectValidationResult doValidate(ReceiptEditReceivingInfoCommand command) {
        return new ObjectValidationResult(
                validateCurrentLocationIsDestination(command),
                validateReceiptIsPending(command),
                validateItemsToSaveAreValid(command),
                validateNoDuplicateItemsToSave(command),
                validateBinLocationIsNotRemoved(command),
                validateBinLocationsAtDestination(command),
        )
    }

    /**
     * The receipt can only be edited by a user logged in at the destination of its shipment.
     */
    private ObjectError validateCurrentLocationIsDestination(ReceiptEditReceivingInfoCommand command) {
        if (!command.receipt) {
            return null
        }

        if (command.receipt.shipment?.destination?.id == AuthService.currentLocation?.id) {
            return null
        }

        return rejectField("receipt", command.receipt, "receiptEditReceivingInfoCommand.receipt.isDifferentLocation")
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

    /**
     * When the receipt is received into a destination that tracks bin locations
     * (Location.hasBinLocationSupport), the bin location of a receipt item can be changed but never removed - a line
     * left without one receives its stock outside of any bin. Destinations without bin location support hold no bins
     * at all, so their lines are not checked.
     */
    private ObjectError validateBinLocationIsNotRemoved(ReceiptEditReceivingInfoCommand command) {
        // Only existing items can have their bin location removed - a new item never had one to begin with.
        List<String> itemIds = command.itemsToSave
                .findAll { ReceiptItemEditReceivingInfoRequest item ->
                    item.receiptItem?.binLocation != null && item.binLocation == null
                }
                .collect { ReceiptItemEditReceivingInfoRequest item -> item.receiptItem.id }

        // The items are checked first so that a request that changes no bin location at all doesn't load the
        // shipment and its destination.
        if (!itemIds) {
            return null
        }

        Location destination = command.receipt?.shipment?.destination

        return destination?.hasBinLocationSupport() ?
                rejectField("itemsToSave", command.itemsToSave,
                        "receiptEditReceivingInfoCommand.itemsToSave.binLocationRemoved", [itemIds.toString()]) :
                null
    }

    /**
     * Every bin location the receipt items are saved with must be a bin of the destination of the shipment, reported
     * once per bin location.
     */
    private List<ObjectError> validateBinLocationsAtDestination(ReceiptEditReceivingInfoCommand command) {
        Location destination = command.receipt?.shipment?.destination
        if (!destination) {
            return null
        }

        return command.itemsToSave
                .collect { ReceiptItemEditReceivingInfoRequest item -> item.binLocation }
                .findAll { Location binLocation ->
                    binLocation != null && binLocation.parentLocation?.id != destination.id
                }
                .unique { Location binLocation -> binLocation.id }
                .collect { Location binLocation ->
                    rejectField("itemsToSave", command.itemsToSave,
                            "receiptEditReceivingInfoCommand.itemsToSave.binLocationNotAtDestination",
                            [binLocation.name])
                }
    }
}
