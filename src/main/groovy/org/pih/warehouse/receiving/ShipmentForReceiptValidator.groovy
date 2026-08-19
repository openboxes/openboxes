package org.pih.warehouse.receiving

import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.api.receiving.v2.ReceiptV2Service
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

    /**
     * Whether a new receipt can be opened on the shipment: it has to be shipped, have something left to receive,
     * and not already carry a pending receipt.
     */
    @Override
    protected ObjectValidationResult doValidate(Shipment shipment) {
        return new ObjectValidationResult(
                validateShipmentHasBeenShipped(shipment),
                validateShipmentNotFullyReceived(shipment),
                validateShipmentHasNoPendingReceipt(shipment),
        )
    }

    /**
     * Whether a receiving page can be accessed
     */
    ObjectValidationResult validateForReceivingAccess(Shipment shipment, Location currentLocation) {
        return new ObjectValidationResult(
                validateShipmentHasBeenShipped(shipment),
                validateShipmentNotFullyReceived(shipment),
                validateShipmentDestination(shipment, currentLocation),
        )
    }

    /**
     * Nothing can be received before the shipment is sent.
     */
    private ObjectError validateShipmentHasBeenShipped(Shipment shipment) {
        if (shipment?.currentStatus in ShipmentStatusCode.listShipped()) {
            return null
        }

        return rejectField("currentStatus", shipment?.currentStatus,
                "stockMovement.hasNotBeenShipped.message", [shipment?.shipmentNumber] as Object[])
    }

    /**
     * Nothing left to receive (or cancel) on any line of the shipment.
     */
    private ObjectError validateShipmentNotFullyReceived(Shipment shipment) {
        if (!shipment || !ReceiptV2Service.isShipmentFullyReceived(shipment)) {
            return null
        }

        return rejectField("shipmentItems", shipment.shipmentItems,
                "stockMovement.hasAlreadyBeenReceived.message", [shipment.shipmentNumber] as Object[])
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
                "shipment.pendingReceiptExists.message", [shipment.shipmentNumber] as Object[])
    }

    /**
     * Whether the shipment is received where the user is logged in
     */
    private ObjectError validateShipmentDestination(Shipment shipment, Location currentLocation) {
        if (shipment?.destination?.id == currentLocation?.id) {
            return null
        }

        return rejectField("destination", shipment?.destination, "stockMovement.isDifferentLocation.message")
    }
}
