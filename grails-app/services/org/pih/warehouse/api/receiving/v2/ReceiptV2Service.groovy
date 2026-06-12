package org.pih.warehouse.api.receiving.v2

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptDto
import org.pih.warehouse.receiving.ReceiptGrouping
import org.pih.warehouse.receiving.ReceiptIdentifierService
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.receiving.ReceiptItemDto
import org.pih.warehouse.receiving.ReceiptService
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.receiving.ShipmentItemReceivingSummaryDto
import org.pih.warehouse.receiving.ShipmentReceivingSummaryCommand
import org.pih.warehouse.receiving.ShipmentReceivingSummaryDto
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentItemDto
import org.pih.warehouse.shipping.ShipmentStatusCode

@Transactional(readOnly = true)
class ReceiptV2Service {

    ReceiptIdentifierService receiptIdentifierService

    // Inject old receipt service to reuse bin creation logic
    ReceiptService receiptService

    @Transactional
    ReceiptDto startReceipt(String shipmentId) {
        Shipment shipment = Shipment.get(shipmentId)
        if (!shipment) {
            throw new ObjectNotFoundException(shipmentId, Shipment.class.toString())
        }

        validateShipmentReceivable(shipment)

        Receipt receipt = new Receipt()
        receipt.receiptNumber = receiptIdentifierService.generate(receipt)
        receipt.receiptStatusCode = ReceiptStatusCode.PENDING
        receipt.recipient = AuthService.currentUser
        receipt.expectedDeliveryDate = shipment.expectedDeliveryDate
        receipt.actualDeliveryDate = shipment.actualDeliveryDate ?: new Date()
        receipt.disableRefresh = true

        receiptService.createTemporaryReceivingBin(shipment)
        shipment.addToReceipts(receipt)

        if (!receipt.save()) {
            throw new ValidationException("Receipt is invalid", receipt.errors)
        }

        return ReceiptDto.from(receipt)
    }

    private static void validateShipmentReceivable(Shipment shipment) {
        boolean hasPendingReceipt = shipment.receipts?.any { it.receiptStatusCode == ReceiptStatusCode.PENDING }
        if (hasPendingReceipt) {
            throw new IllegalStateException("A pending receipt already exists for shipment ${shipment.shipmentNumber}")
        }

        if (shipment.isFullyReceived()) {
            throw new IllegalStateException("Shipment ${shipment.shipmentNumber} has already been fully received")
        }

        if (shipment.currentStatus in [ShipmentStatusCode.CREATED, ShipmentStatusCode.PENDING]) {
            throw new IllegalStateException(
                    "Cannot receive shipment ${shipment.shipmentNumber} because it has not been shipped yet")
        }
    }

    /**
     * List all receipts (and their receipt items) that are associated with a shipment.
     */
    List<ReceiptDto> listShipmentReceipts(Shipment shipment) {
        if (!shipment) {
            throw new ObjectNotFoundException(shipment.id, Shipment.toString())
        }

        List<Receipt> receipts = Receipt.findAllByShipment(shipment)
        return receipts.collect { ReceiptDto.from(it) }
    }

    /**
     * Fetches an overview of a shipment's current state of receiving.
     */
    ShipmentReceivingSummaryDto getShipmentReceivingSummary(ShipmentReceivingSummaryCommand command) {
        Shipment shipment = command.shipment
        ReceiptGrouping grouping = command.grouping

        String currentReceiptId = Receipt.findByShipmentAndReceiptStatusCode(shipment, ReceiptStatusCode.PENDING)?.id

        // This summary centers on the relationship between a shipment item and its receipt items, so don't bother
        // with the receipts themselves. Instead, fetch the shipment items (sorted) and then collect the receipt
        // items grouped by their shipment item so that we can easily loop both of them together.
        List<ShipmentItem> shipmentItems = shipment.shipmentItems.sort()
        Map<String, List<ReceiptItem>> receiptItemsByShipmentItemId =
                ReceiptItem.findAllByShipmentItemInList(shipmentItems.id)
                        .groupBy { it.shipmentItemId.toString() }

        ShipmentReceivingSummaryDto shipmentSummary = new ShipmentReceivingSummaryDto(
                shipmentId: shipment.id,
        )

        // Build the summary for each shipment item.
        for (shipmentItem in shipmentItems) {
            String shipmentItemId = shipmentItem.id

            ShipmentItemReceivingSummaryDto shipmentItemSummary = new ShipmentItemReceivingSummaryDto(
                    shipmentItem: ShipmentItemDto.from(shipmentItem),
            )

            // We split up the current and previous receipt items only because it is more convenient for the client.
            for (receiptItem in receiptItemsByShipmentItemId.get(shipmentItemId)) {
                ReceiptItemDto receiptItemDto = ReceiptItemDto.from(receiptItem)
                if (receiptItemDto.receiptId == currentReceiptId) {
                    shipmentItemSummary.currentReceiptItems.add(receiptItemDto)
                }
                else {
                    shipmentItemSummary.previousReceiptItems.add(receiptItemDto)
                }
            }
            shipmentSummary.shipmentItemSummaryById.put(shipmentItemId, shipmentItemSummary)
        }

        // Populate the item grouping map for the client if they requested us to do so.
        Map dataGrouping
        switch(grouping) {
            case ReceiptGrouping.PACK_LEVEL:
                dataGrouping = buildPackLevelGrouping(shipmentItems)
                break
            default:
                dataGrouping = [:]
                break
        }
        shipmentSummary.setDataGrouping(dataGrouping)

        return shipmentSummary
    }

    private Map<String, Map<String, String>> buildPackLevelGrouping(List<ShipmentItem> shipmentItems) {
        Map<String, Map<String, String>> grouping = [:].withDefault { [:] }
        for (shipmentItem in shipmentItems) {
            // We (perhaps incorrectly) only group two levels deep. Any additional parent containers will be ignored.
            Container packLevel2 = shipmentItem.container
            Container packLevel1 = packLevel2?.parentContainer

            grouping.get(packLevel1?.name).put(packLevel2?.name, shipmentItem.id)
        }
    }
}
