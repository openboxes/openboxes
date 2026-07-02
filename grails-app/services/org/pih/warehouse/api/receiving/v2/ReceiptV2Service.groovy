package org.pih.warehouse.api.receiving.v2

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.OrderedDataGroup
import org.pih.warehouse.core.date.JavaUtilDateParser
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryItemManager
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptDto
import org.pih.warehouse.receiving.ReceiptEditReceivingInfoCommand
import org.pih.warehouse.receiving.ReceiptGroup
import org.pih.warehouse.receiving.ReceiptIdentifierService
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.receiving.ReceiptItemDto
import org.pih.warehouse.receiving.ReceiptItemEditReceivingInfoRequest
import org.pih.warehouse.receiving.ReceiptItemUpsertRequest
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
    InventoryItemManager inventoryItemManager

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
    ReceiptSaveResponseDto updateItemsBatch(ReceiptItemsBatchRequest request) {
        // The receipt is bound and validated (as existing and pending) by the request, so this assumes a validated
        // request - see ReceiptItemsBatchRequestValidator.
        Receipt receipt = request.receipt

        request.itemsToDelete.each { String receiptItemId -> deleteReceiptItem(receipt, receiptItemId) }

        List<ReceiptItemSaveDto> updatedLines = request.itemsToSave.collect { ReceiptItemUpsertRequest item ->
            item.receiptItem ? updateReceiptItem(item) : createReceiptItem(receipt, item)
        }

        return new ReceiptSaveResponseDto(updatedLines: updatedLines)
    }

    /**
     * Creates/updates the receipt items of a single shipment item, additionally allowing the product lot (lot number
     * and expiration date) and recipient of each item to be edited. Behaves like {@link #updateItemsBatch} but scoped
     * to the one shipment item identified in the URL, and without support for deletes.
     *
     * The receipt and shipment item are carried (and validated as existing/pending) by the command, so this assumes a
     * validated command - see {@link ReceiptEditReceivingInfoCommandValidator}.
     */
    @Transactional
    ReceiptSaveResponseDto editReceivingInfo(ReceiptEditReceivingInfoCommand command) {
        List<ReceiptItemSaveDto> updatedLines =
                command.itemsToSave.collect { ReceiptItemEditReceivingInfoRequest item ->
                    upsertReceiptItem(command.receipt, command.shipmentItem, item)
                }

        return new ReceiptSaveResponseDto(updatedLines: updatedLines)
    }

    /**
     * Creates or updates a single receipt item from an edit-receiving-info request. The inventory item is resolved
     * (and created if necessary) from the requested product + lot number + expiration date and is potentially swapped
     * onto the receipt item, which is what allows the lot to be edited.
     */
    private ReceiptItemSaveDto upsertReceiptItem(
            Receipt receipt, ShipmentItem shipmentItem, ReceiptItemEditReceivingInfoRequest item) {
        // InventoryItem.expirationDate is a (legacy) java.util.Date, so convert the request's date-only LocalDate at
        // the domain boundary. asDate resolves it to start-of-day in the system zone, so the stored Date and its
        // MM/dd/yyyy formatting (see the InventoryItem JSON marshaller) stay identical to before.
        Date expirationDate = JavaUtilDateParser.asDate(item.expirationDate)
        InventoryItem inventoryItem = inventoryItemManager.getOrCreateInventoryItem(
                item.product, item.lotNumber, expirationDate)

        ReceiptItem receiptItem = item.receiptItem ?: new ReceiptItem(
                quantityShipped: shipmentItem.quantity,
                sortOrder: shipmentItem.receiptItems.size(),
        )

        // The bin location is intentionally not edited via this endpoint, so it is left untouched.
        receiptItem.product = item.product
        receiptItem.inventoryItem = inventoryItem
        receiptItem.lotNumber = inventoryItem.lotNumber
        receiptItem.expirationDate = inventoryItem.expirationDate
        receiptItem.recipient = item.recipient
        receiptItem.quantityReceived = item.quantityReceiving
        receiptItem.isSplitItem = item.isSplitItem

        if (!item.receiptItem) {
            receipt.addToReceiptItems(receiptItem)
            shipmentItem.addToReceiptItems(receiptItem)
            if (!receiptItem.save()) {
                throw new ValidationException("Receipt item is invalid", receiptItem.errors)
            }
        }

        return ReceiptItemSaveDto.from(receiptItem, item.rowId)
    }

    private static ReceiptItemSaveDto createReceiptItem(Receipt receipt, ReceiptItemUpsertRequest item) {

        ShipmentItem shipmentItem = item.shipmentItem

        ReceiptItem receiptItem = new ReceiptItem(
                product: shipmentItem.product,
                inventoryItem: shipmentItem.inventoryItem,
                lotNumber: shipmentItem.lotNumber,
                expirationDate: shipmentItem.expirationDate,
                recipient: shipmentItem.recipient,
                quantityShipped: shipmentItem.quantity,
                quantityReceived: item.quantityReceiving,
                binLocation: item.binLocation,
                sortOrder: shipmentItem.receiptItems.size(),
        )

        receipt.addToReceiptItems(receiptItem)
        shipmentItem.addToReceiptItems(receiptItem)

        if (!receiptItem.save()) {
            throw new ValidationException("Receipt item is invalid", receiptItem.errors)
        }

        return ReceiptItemSaveDto.from(receiptItem, item.rowId)
    }

    private static ReceiptItemSaveDto updateReceiptItem(ReceiptItemUpsertRequest item) {
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
                pendingReceiptId: currentReceiptId,
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
