package org.pih.warehouse.receiving

import grails.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.ObjectError

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.core.validation.PlainObjectValidator
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

/**
 * Validating (doValidate) answers whether a new receipt can be started,
 * while {@link #validateForReceivingAccess} answers whether the user may work on the receiving.
 */
@Component
class ShipmentForReceiptValidator extends PlainObjectValidator<Shipment> {

    @Autowired
    ShipmentReceivingCalculator shipmentReceivingCalculator

    /**
     * Whether a new receipt can be opened on the shipment: it has to be shipped, have something left to receive,
     * not already carry a pending receipt, and be opened by a user logged in at its destination.
     */
    @Override
    protected ObjectValidationResult doValidate(Shipment shipment) {
        ObjectError hasBeenShippedError = validateShipmentHasBeenShipped(shipment)
        if (hasBeenShippedError) {
            return new ObjectValidationResult(hasBeenShippedError)
        }

        return new ObjectValidationResult(
                validateShipmentNotFullyReceived(shipment),
                validateShipmentHasNoPendingReceipt(shipment),
                validateShipmentDestination(shipment, AuthService.currentLocation,
                        "stockMovement.startReceipt.isDifferentLocation.message"),
        )
    }

    /**
     * Whether a receiving page can be accessed
     */
    ObjectValidationResult validateForReceivingAccess(Shipment shipment, Location currentLocation) {
        ObjectError hasBeenShippedError = validateShipmentHasBeenShipped(shipment)
        if (hasBeenShippedError) {
            return new ObjectValidationResult(hasBeenShippedError)
        }

        return new ObjectValidationResult(
                validateShipmentNotFullyReceived(shipment),
                validateShipmentDestination(shipment, currentLocation, "stockMovement.isDifferentLocation.message"),
        )
    }

    /**
     * Whether the last receipt of the shipment can be rolled back from the current location
     */
    ObjectValidationResult validateForReceiptRollback(Shipment shipment) {
        return new ObjectValidationResult(
                validateShipmentDestination(shipment, AuthService.currentLocation,
                        "stockMovement.rollbackLastReceipt.isDifferentLocation.message"),
        )
    }

    /**
     * Validates that the lines of the pending receipt of the shipment can be synced from the current location,
     * throwing a ValidationException when they cannot.
     */
    void validateForReceiptSync(Shipment shipment) {
        ObjectError error = validateShipmentDestination(shipment, AuthService.currentLocation,
                "stockMovement.syncReceiptLines.isDifferentLocation.message")
        if (!error) {
            return
        }

        Errors errors = getErrors(shipment)
        errors.addError(error)
        throw new ValidationException("Validation failed for Shipment", errors)
    }

    /**
     * Nothing can be received before the shipment is sent.
     */
    private ObjectError validateShipmentHasBeenShipped(Shipment shipment) {
        if (shipment?.currentStatus in ShipmentStatusCode.listShipped()) {
            return null
        }

        return rejectField("currentStatus", shipment?.currentStatus,
                "stockMovement.hasNotBeenShipped.message", [shipment?.shipmentNumber])
    }

    /**
     * Nothing left to receive (or cancel) on any line of the shipment.
     */
    private ObjectError validateShipmentNotFullyReceived(Shipment shipment) {
        if (!shipment || !shipmentReceivingCalculator.isShipmentFullyReceived(shipment)) {
            return null
        }

        return rejectField("shipmentItems", shipment.shipmentItems,
                "stockMovement.hasAlreadyBeenReceived.message", [shipment.shipmentNumber])
    }

    /**
     * A second receipt cannot be started while one is still pending on the shipment.
     */
    private ObjectError validateShipmentHasNoPendingReceipt(Shipment shipment) {
        boolean hasPendingReceipt = shipment?.receipts?.any { it.receiptStatusCode == ReceiptStatusCode.PENDING }
        if (!hasPendingReceipt) {
            return null
        }

        return rejectField("receipts", shipment.receipts,
                "shipment.pendingReceiptExists.message", [shipment.shipmentNumber])
    }

    /**
     * Whether the user is logged in at the destination of the shipment
     */
    private ObjectError validateShipmentDestination(Shipment shipment, Location currentLocation, String messageCode) {
        if (shipment?.destination?.id == currentLocation?.id) {
            return null
        }

        return rejectField("destination", shipment?.destination, messageCode)
    }
}
