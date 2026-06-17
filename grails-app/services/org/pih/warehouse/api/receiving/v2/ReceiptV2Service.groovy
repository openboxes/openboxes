package org.pih.warehouse.api.receiving.v2

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.OrderedDataGroup
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptDto
import org.pih.warehouse.receiving.ReceiptGroup
import org.pih.warehouse.receiving.ReceiptIdentifierService
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.receiving.ReceiptItemDto
import org.pih.warehouse.receiving.ReceiptItemRequest
import org.pih.warehouse.receiving.ReceiptItemSaveDto
import org.pih.warehouse.receiving.ReceiptItemsBatchRequest
import org.pih.warehouse.receiving.ReceiptSaveResponseDto
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
    ReceiptService receiptService  // Inject old receipt service to reuse bin creation logic
    MessageLocalizer messageLocalizer

    @Transactional
    ReceiptDto startReceipt(String shipmentId) {
        Shipment shipment = Shipment.get(shipmentId)
        if (!shipment) {
            throw new ObjectNotFoundException(shipmentId, Shipment.toString())
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

    @Transactional
    ReceiptSaveResponseDto updateItemsBatch(String receiptId, ReceiptItemsBatchRequest request) {
        Receipt receipt = Receipt.get(receiptId)
        if (!receipt) {
            throw new ObjectNotFoundException(receiptId, Receipt.class.toString())
        }

        request.itemsToDelete.each { String receiptItemId -> deleteReceiptItem(receipt, receiptItemId) }

        List<ReceiptItemSaveDto> updatedLines = request.itemsToSave.collect { ReceiptItemRequest item ->
            item.receiptItem ? updateReceiptItem(item) : createReceiptItem(receipt, item)
        }

        return new ReceiptSaveResponseDto(updatedLines: updatedLines)
    }

    private static ReceiptItemSaveDto createReceiptItem(Receipt receipt, ReceiptItemRequest item) {
        if (!item.shipmentItem) {
            throw new IllegalArgumentException("Cannot receive item without a valid shipment item")
        }

        ReceiptItem receiptItem = new ReceiptItem()
        receiptItem.product = item.shipmentItem.product
        receiptItem.inventoryItem = item.shipmentItem.inventoryItem
        receiptItem.lotNumber = item.shipmentItem.lotNumber
        receiptItem.expirationDate = item.shipmentItem.expirationDate
        receiptItem.recipient = item.shipmentItem.recipient
        receiptItem.quantityShipped = item.shipmentItem.quantity
        receiptItem.quantityReceived = item.quantityReceiving
        receiptItem.binLocation = item.binLocation
        receiptItem.sortOrder = item.shipmentItem.receiptItems.size()

        receipt.addToReceiptItems(receiptItem)
        item.shipmentItem.addToReceiptItems(receiptItem)

        if (!receiptItem.save()) {
            throw new ValidationException("Receipt item is invalid", receiptItem.errors)
        }

        return ReceiptItemSaveDto.from(receiptItem, item.rowId)
    }

    private static ReceiptItemSaveDto updateReceiptItem(ReceiptItemRequest item) {
        ReceiptItem receiptItem = item.receiptItem
        receiptItem.quantityReceived = item.quantityReceiving
        receiptItem.binLocation = item.binLocation

        return ReceiptItemSaveDto.from(receiptItem, item.rowId)
    }

    private static void deleteReceiptItem(Receipt receipt, String receiptItemId) {
        ReceiptItem receiptItem = ReceiptItem.get(receiptItemId)
        if (!receiptItem) {
            throw new ObjectNotFoundException(receiptItemId, ReceiptItem.class.toString())
        }

        receipt.removeFromReceiptItems(receiptItem)
        receiptItem.shipmentItem?.removeFromReceiptItems(receiptItem)
        receiptItem.delete()
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
    List<ReceiptDto> listShipmentReceipts(String shipmentId) {
        Shipment shipment = Shipment.read(shipmentId)
        if (!shipment) {
            throw new ObjectNotFoundException(shipmentId, Shipment.toString())
        }

        List<Receipt> receipts = Receipt.findAllByShipment(shipment)
        return receipts.collect { ReceiptDto.from(it) }
    }

    /**
     * Fetches an overview of a shipment's current state of receiving.
     */
    ShipmentReceivingSummaryDto getShipmentReceivingSummary(ShipmentReceivingSummaryCommand command) {
        Shipment shipment = command.shipment
        ReceiptGroup group = command.group

        String currentReceiptId = Receipt.findByShipmentAndReceiptStatusCode(shipment, ReceiptStatusCode.PENDING)?.id

        // This summary centers on the relationship between a shipment item and its receipt items, so don't bother
        // with the receipts themselves. Instead, fetch the shipment items (sorted) and then collect the receipt
        // items grouped by their shipment item so that we can easily loop both of them together.
        List<ShipmentItem> shipmentItems = shipment.shipmentItems.sort()
        Map<String, List<ReceiptItem>> receiptItemsByShipmentItemId =
                ReceiptItem.findAllByShipmentItemInList(shipmentItems)
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
                } else {
                    shipmentItemSummary.previousReceiptItems.add(receiptItemDto)
                }
            }
            shipmentSummary.shipmentItemSummaryById.put(shipmentItemId, shipmentItemSummary)
        }

        // Populate the shipment item group map for the client if they requested us to do so.
        OrderedDataGroup shipmentItemsGrouped
        switch(group) {
            case ReceiptGroup.PACK_LEVEL:
                shipmentItemsGrouped = buildPackLevelGroup(shipmentItems)
                break
            case ReceiptGroup.SHIPMENT_ITEM:
                shipmentItemsGrouped = buildShipmentItemGroup(shipmentItems)
                break
        }
        shipmentSummary.setShipmentItemsGrouped(shipmentItemsGrouped)

        return shipmentSummary
    }

    private OrderedDataGroup buildPackLevelGroup(List<ShipmentItem> shipmentItems) {
        String unpackedGroupName = messageLocalizer.localize("shipping.unpacked.label")

        OrderedDataGroup packLevel1Group = new OrderedDataGroup()
        for (shipmentItem in shipmentItems) {
            // We (perhaps incorrectly) only group two levels deep. Any additional parent containers will be ignored.
            Container packLevel2 = shipmentItem.container
            Container packLevel1 = packLevel2?.parentContainer

            // When the item's container has no parent, the container itself is the top pack level, so we group
            // directly under it. Items with no container at all fall back to the "Unpacked" group. We avoid a null
            // key both because it groups nothing and because the JSON serializer drops map entries keyed on null.
            String packLevel1Name = packLevel1?.name ?: packLevel2?.name ?: unpackedGroupName
            String packLevel2Name = packLevel1?.name ? (packLevel2?.name ?: unpackedGroupName) : unpackedGroupName

            OrderedDataGroup packLevel2Group = new OrderedDataGroup()
            packLevel2Group.put(packLevel2Name, shipmentItem.id)

            packLevel1Group.put(packLevel1Name, packLevel2Group)
        }
        return packLevel1Group
    }

    private OrderedDataGroup buildShipmentItemGroup(List<ShipmentItem> shipmentItems) {
        OrderedDataGroup shipmentItemGroup = new OrderedDataGroup()
        for (shipmentItem in shipmentItems) {
            // The grouping doesn't really matter here because we're keying on item id so there will always only
            // ever be one element in each group, but we preserve the format for consistency (in case the client
            // wants to create a standard approach to parsing data groups) and so that the client use the ordering.
            shipmentItemGroup.put(shipmentItem.id, shipmentItem.id)
        }
        return shipmentItemGroup
    }
}
