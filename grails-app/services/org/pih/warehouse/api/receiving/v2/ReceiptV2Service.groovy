package org.pih.warehouse.api.receiving.v2

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.OrderedDataGroup
import org.pih.warehouse.core.date.JavaUtilDateParser
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryItemManager
import org.pih.warehouse.inventory.RefreshProductAvailabilityEvent
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptCompleteRequestCommand
import org.pih.warehouse.receiving.ReceiptDto
import org.pih.warehouse.receiving.ReceiptEditReceivingInfoCommand
import org.pih.warehouse.receiving.ReceiptGroup
import org.pih.warehouse.receiving.ReceiptIdentifierService
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.receiving.ReceiptItemCommentDto
import org.pih.warehouse.receiving.ReceiptItemCommentSaveCommand
import org.pih.warehouse.receiving.ReceiptItemCompleteRequest
import org.pih.warehouse.receiving.ReceiptItemDto
import org.pih.warehouse.receiving.ReceiptItemEditReceivingInfoRequest
import org.pih.warehouse.receiving.ReceiptItemUpsertRequest
import org.pih.warehouse.receiving.ReceiptItemSaveDto
import org.pih.warehouse.receiving.ReceiptItemsBatchRequest
import org.pih.warehouse.receiving.ReceiptSaveResponseDto
import org.pih.warehouse.receiving.ReceiptService
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.receiving.ReceiptV2Marker
import org.pih.warehouse.receiving.ShipmentItemReceivedQuantitiesDto
import org.pih.warehouse.receiving.ShipmentItemReceivingSummaryDto
import org.pih.warehouse.receiving.ShipmentReceivingSummaryCommand
import org.pih.warehouse.receiving.ShipmentReceivingSummaryDto
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentItemDto
import org.pih.warehouse.shipping.ShipmentService
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentStatusTransitionEvent

@Transactional(readOnly = true)
class ReceiptV2Service {

    ReceiptIdentifierService receiptIdentifierService
    ReceiptService receiptService  // Inject old receipt service to reuse bin creation logic
    MessageLocalizer messageLocalizer
    InventoryItemManager inventoryItemManager
    ShipmentService shipmentService
    TransactionIdentifierService transactionIdentifierService
    GrailsApplication grailsApplication

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

        markReceiptAsV2(receipt)

        shipment.shipmentItems.each { ShipmentItem shipmentItem ->
            // Create initial ("original") receipt items only for items that have yet remaining qty to receive
            if (getShipmentItemQuantityRemaining(shipmentItem) > 0) {
                createOriginalReceiptItem(receipt, shipmentItem)
            }
        }

        return ReceiptDto.from(receipt)
    }

    /**
     * Creates the "original" receipt item of a shipment item: an empty (nothing received yet) line mirroring the
     * shipment item and carrying its full quantity as the quantity shipped. Exactly one original line exists per
     * still-receivable shipment item on a receipt - it is created when the receipt is started, cannot be deleted
     * (the batch update endpoint rejects deleting it) and is the only line whose remainder can be canceled on
     * completion. Shipment items already fully consumed by previous receipts get no line: it would only produce
     * zero-quantity transaction entries on completion, and the receiving client marks an item as completed
     * precisely by it having nothing pending on the current receipt.
     * Lines added while receiving are split lines instead: they are flagged with isSplitItem and carry a quantity
     * shipped of zero, so they never factor into the cancel-remaining math.
     */
    private static ReceiptItem createOriginalReceiptItem(Receipt receipt, ShipmentItem shipmentItem) {
        ReceiptItem receiptItem = new ReceiptItem(
                product: shipmentItem.product,
                inventoryItem: shipmentItem.inventoryItem,
                lotNumber: shipmentItem.lotNumber,
                expirationDate: shipmentItem.expirationDate,
                recipient: shipmentItem.recipient,
                quantityShipped: shipmentItem.quantity,
                quantityReceived: 0,
                isSplitItem: Boolean.FALSE,
                sortOrder: shipmentItem.receiptItems?.size() ?: 0,
        )

        receipt.addToReceiptItems(receiptItem)
        shipmentItem.addToReceiptItems(receiptItem)

        if (!receiptItem.save()) {
            throw new ValidationException("Receipt item is invalid", receiptItem.errors)
        }

        return receiptItem
    }

    /**
     * Stamps the receipt as created by the v2 workflow, for reads that must know which workflow's semantics its
     * lines were written under - the workflow cannot be inferred from the persisted data afterwards, so it is
     * recorded when the receipt is started (see {@link ReceiptV2Marker}).
     */
    private static void markReceiptAsV2(Receipt receipt) {
        ReceiptV2Marker marker = new ReceiptV2Marker(receipt: receipt)
        if (!marker.save()) {
            throw new ValidationException("Receipt v2 marker is invalid", marker.errors)
        }
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
        Date expirationDate = item.expirationDate ? JavaUtilDateParser.asDate(item.expirationDate) : null
        InventoryItem inventoryItem = inventoryItemManager.getOrCreateInventoryItem(
                item.product, item.lotNumber, expirationDate)

        // Lines created here are split lines - the original line always exists already (created when the receipt
        // was started), so the split flag is owned by the server: forced on creation, never rebound afterwards.
        // Split lines also carry a quantity shipped of zero - the shipment item's full quantity stays on the
        // original line, which keeps that line the only one with a remainder that can be canceled on completion
        // (see cancelRemainingQuantities).
        ReceiptItem receiptItem = item.receiptItem ?: new ReceiptItem(
                isSplitItem: Boolean.TRUE,
                quantityShipped: 0,
                sortOrder: shipmentItem.receiptItems.size(),
        )

        receiptItem.product = item.product
        receiptItem.inventoryItem = inventoryItem
        receiptItem.lotNumber = inventoryItem.lotNumber
        receiptItem.expirationDate = inventoryItem.expirationDate
        receiptItem.recipient = item.recipient
        receiptItem.quantityReceived = item.quantityReceiving
        receiptItem.binLocation = item.binLocation

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

    /**
     * Completes a pending receipt: flags it as received (optionally canceling the quantities that are still left to
     * receive), marks the shipment as (partially) received and records the inbound stock transaction, then triggers
     * the follow-up notifications and the product availability refresh.
     *
     * The receipt is bound and validated (as existing and pending, with the items belonging to it) by the command,
     * so this assumes a validated command - see {@link ReceiptCompleteRequestCommandValidator}.
     */
    @Transactional
    ReceiptDto completeReceipt(ReceiptCompleteRequestCommand command) {
        Receipt receipt = command.receipt

        // The receipt must be flagged as received before the canceled quantities are computed - the shipment item
        // quantity getters only count receipt items that belong to RECEIVED receipts.
        receipt.receiptStatusCode = ReceiptStatusCode.RECEIVED
        if (command.dateDelivered) {
            // Receipt.actualDeliveryDate is a (legacy) java.util.Date, so convert at the domain boundary.
            receipt.actualDeliveryDate = JavaUtilDateParser.asDate(command.dateDelivered)
        }

        cancelRemainingQuantities(command.itemsToComplete)

        // The order summary refresh is left suppressed here because it already rides on the shipment save at the
        // end of createInboundTransaction.
        receipt.disableRefresh = true
        if (!receipt.save()) {
            throw new ValidationException("Receipt is invalid", receipt.errors)
        }

        // The canceled quantities count towards the shipment being fully received, so the shipment event (which
        // decides between RECEIVED and PARTIALLY_RECEIVED) can only be created after they are applied.
        createShipmentReceivedEvent(receipt)
        Transaction transaction = createInboundTransaction(receipt)

        // Trigger shipment status transition event to handle email notifications
        grailsApplication.mainContext.publishEvent(
                new ShipmentStatusTransitionEvent(receipt, ShipmentStatusCode.RECEIVED))

        // Trigger product availability refresh
        transaction.disableRefresh = Boolean.FALSE
        grailsApplication.mainContext.publishEvent(new RefreshProductAvailabilityEvent(
                transaction, transaction.associatedLocation, transaction.associatedProducts, false))

        return ReceiptDto.from(receipt)
    }

    /**
     * Cancels the quantity still left to receive on every line flagged with cancelRemaining: the shipment item's
     * remaining quantity ({@link #getShipmentItemQuantityRemaining}) is written to the flagged line as its canceled
     * quantity. Lines missing from the request (or sent with the flag disabled) cancel nothing, so their shipment
     * item's remainder stays open for future receipts.
     *
     * Only the original line of a shipment item (isSplitItem: false, created when the receipt was started) can
     * carry a cancel - the shipment item's canceled quantity is summed over all of its receipt items, so the
     * cancel must land on exactly one line to not be double-counted. Split lines are skipped here outright
     * (flagging them is also rejected upfront by the request validator) - the quantities received on them reduce
     * the remainder that the original line cancels.
     *
     * The remainder accounts for quantities received or canceled by previous receipts, quantities received on
     * sibling (split) lines, and cancels applied earlier in this loop (each cancel is immediately reflected in the
     * shipment item's remaining quantity), so the total can never be over-canceled. Writing the full item remainder
     * to the line (rather than capping it by the line's own quantity shipped) relies on only receipts created by
     * {@link #startReceipt} ever completing through here: their original line is the only line with a quantity
     * shipped, so the item remainder IS the original line's remainder. Old-workflow receipts, whose lines can carry
     * per-line quantity-shipped allocations, never reach this code.
     */
    private static void cancelRemainingQuantities(List<ReceiptItemCompleteRequest> itemsToComplete) {
        for (ReceiptItemCompleteRequest item : itemsToComplete) {
            if (!item.cancelRemaining) {
                continue
            }

            ReceiptItem receiptItem = item.receiptItem

            // Only the original line can carry a cancel - split lines have no quantity shipped to compute one from.
            if (receiptItem.isSplitItem) {
                continue
            }

            receiptItem.quantityCanceled = Math.max(0, getShipmentItemQuantityRemaining(receiptItem.shipmentItem))
        }
    }

    /**
     * The quantity of a shipment item still left to receive: its quantity minus everything received or canceled
     * on its receipt items across completed receipts (the receipt being completed is already flagged as RECEIVED
     * when the cancels are computed, so its lines count too).
     *
     * Deliberately not {@link ShipmentItem#getQuantityRemaining}: the legacy getters behind it only count receipt
     * items whose product matches the shipment item's, while lines can be received against an edited product
     * (see {@link #upsertReceiptItem}) and must still consume the shipment item's remainder - exactly as
     * {@link ShipmentItemReceivingSummaryDto} totals them for the receiving client.
     */
    private static Integer getShipmentItemQuantityRemaining(ShipmentItem shipmentItem) {
        int quantityReceivedAndCanceled = (shipmentItem.receiptItems ?: []).sum(0) { ReceiptItem receiptItem ->
            receiptItem.receipt?.receiptStatusCode == ReceiptStatusCode.RECEIVED
                    ? (receiptItem.quantityReceived ?: 0) + (receiptItem.quantityCanceled ?: 0)
                    : 0
        } as int
        return (shipmentItem.quantity ?: 0) - quantityReceivedAndCanceled
    }

    /**
     * Whether every line of the shipment is fully received: nothing left to receive (or cancel) on any of its
     * shipment items, per the same product-agnostic math as {@link #getShipmentItemQuantityRemaining}.
     *
     * Deliberately not {@link Shipment#isFullyReceived}: the legacy check ignores lines received against an edited
     * product, so after the v2 flow consumed a shipment item's full quantity that way it would still report the
     * shipment as receivable (and only ever partially received).
     */
    private static boolean isShipmentFullyReceived(Shipment shipment) {
        return shipment.shipmentItems?.every { ShipmentItem shipmentItem ->
            getShipmentItemQuantityRemaining(shipmentItem) <= 0
        }
    }

    /**
     * Creates the shipment-level event marking the receiving that just happened. We create a RECEIVED or
     * PARTIALLY_RECEIVED event depending on the case:
     *  1. When the location supports partial receiving:
     *     RECEIVED - when the shipment is not already received and is now fully received (canceled quantities count
     *         towards this)
     *     PARTIALLY_RECEIVED - when the shipment wasn't partially received before (it's created only once)
     *  2. When the location doesn't support partial receiving:
     *     RECEIVED - after receiving for the first time, we cannot receive for the second time without partial
     *         receiving. The rest of the remaining quantities should be canceled
     *     PARTIALLY_RECEIVED - not allowed
     *
     * Fully received is decided with the v2 receiving math ({@link #isShipmentFullyReceived}), not the legacy
     * {@link Shipment#isFullyReceived}.
     */
    private void createShipmentReceivedEvent(Receipt receipt) {
        Shipment shipment = receipt.shipment

        if (!shipment.wasReceived() &&
                (!shipment.destination.supports(ActivityCode.PARTIAL_RECEIVING) || isShipmentFullyReceived(shipment))) {
            shipmentService.createShipmentEvent(
                    shipment, receipt.actualDeliveryDate, EventCode.RECEIVED, shipment.destination)
            return
        }

        if (!shipment.wasPartiallyReceived()) {
            shipmentService.createShipmentEvent(
                    shipment, receipt.actualDeliveryDate, EventCode.PARTIALLY_RECEIVED, shipment.destination)
        }
    }

    /**
     * Records the inbound stock transaction of a completed receipt: one entry per receipt item, crediting the
     * received quantities to the destination's inventory.
     */
    private Transaction createInboundTransaction(Receipt receipt) {
        Shipment shipment = receipt.shipment
        if (!shipment.destination?.inventory) {
            throw new IllegalStateException(
                    "Destination ${shipment.destination?.name} must have an inventory in order to receive stock")
        }

        Transaction transaction = new Transaction(
                transactionType: TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID),
                incomingShipment: shipment,
                requisition: shipment.requisition,
                receipt: receipt,
                source: shipment.origin,
                destination: null,
                inventory: shipment.destination.inventory,
                transactionDate: receipt.actualDeliveryDate,
        )
        transaction.transactionNumber = transactionIdentifierService.generate(transaction)

        receipt.receiptItems.each { ReceiptItem receiptItem ->
            // Receipt items created via the v2 endpoints always carry an inventory item, but fall back to resolving
            // one from the lot fields to support receipts against legacy shipment items that never had one.
            InventoryItem inventoryItem = receiptItem.inventoryItem ?: inventoryItemManager.getOrCreateInventoryItem(
                    receiptItem.product, receiptItem.lotNumber, receiptItem.expirationDate)

            transaction.addToTransactionEntries(new TransactionEntry(
                    quantity: receiptItem.quantityReceived ?: 0,
                    binLocation: receiptItem.binLocation,
                    inventoryItem: inventoryItem,
            ))
        }

        // Block the refresh of the product availability table (to be triggered at the end of the request)
        transaction.disableRefresh = Boolean.TRUE

        if (!transaction.save(flush: true)) {
            throw new ValidationException(
                    "Failed to receive shipment due to error while saving transaction", transaction.errors)
        }

        // Associate the incoming transaction with the shipment
        shipment.addToIncomingTransactions(transaction)
        shipment.disableRefresh = false
        if (!shipment.save(flush: true)) {
            throw new ValidationException("Shipment is invalid", shipment.errors)
        }

        return transaction
    }

    private static void validateShipmentReceivable(Shipment shipment) {
        boolean hasPendingReceipt = shipment.receipts?.any { it.receiptStatusCode == ReceiptStatusCode.PENDING }
        if (hasPendingReceipt) {
            throw new IllegalStateException("A pending receipt already exists for shipment ${shipment.shipmentNumber}")
        }

        if (isShipmentFullyReceived(shipment)) {
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

    /**
     * The received and canceled totals of every shipment item of the shipment, keyed by shipment item id, summed
     * over the lines of completed (RECEIVED) receipts under the semantics of the workflow each receipt was created
     * with: lines of v2 receipts (see {@link ReceiptV2Marker}) always consume their shipment item, even when they
     * were received against an edited product, while old-workflow lines count only when their product matches the
     * shipment item's (an old-workflow line with an edited product deliberately does not consume the remainder).
     *
     * Deliberately not {@link ShipmentItem#getQuantityReceived}/{@link ShipmentItem#getQuantityCanceled}: the legacy
     * getters filter every line by product and so undercount v2 receipts. Legacy views (e.g. the stock movement
     * packing list) should render receiving state from this map instead.
     */
    Map<String, ShipmentItemReceivedQuantitiesDto> getReceivedQuantitiesByShipmentItemId(Shipment shipment) {
        if (!shipment?.shipmentItems) {
            return [:]
        }

        Set<String> receiptV2Ids = findReceiptV2Ids(shipment)

        return shipment.shipmentItems.collectEntries { ShipmentItem shipmentItem ->
            [(shipmentItem.id): buildReceivedQuantities(shipmentItem, receiptV2Ids)]
        }
    }

    private static ShipmentItemReceivedQuantitiesDto buildReceivedQuantities(ShipmentItem shipmentItem, Set<String> receiptV2Ids) {
        int quantityReceived = 0
        int quantityCanceled = 0

        (shipmentItem.receiptItems ?: []).each { ReceiptItem receiptItem ->
            if (receiptItem.receipt?.receiptStatusCode != ReceiptStatusCode.RECEIVED) {
                return
            }
            // The per-line workflow rule: v2 lines always count, old-workflow lines only on a matching product.
            if (!(receiptItem.receipt.id in receiptV2Ids) && receiptItem.product != shipmentItem.product) {
                return
            }
            quantityReceived += receiptItem.quantityReceived ?: 0
            quantityCanceled += receiptItem.quantityCanceled ?: 0
        }

        return new ShipmentItemReceivedQuantitiesDto(
                quantityReceived: quantityReceived,
                quantityCanceled: quantityCanceled,
                // The same shape as the legacy ShipmentItem.isFullyReceived, on the workflow-aware totals.
                fullyReceived: quantityReceived + quantityCanceled >= (shipmentItem.quantity ?: 0),
        )
    }

    /**
     * The ids of the shipment's receipts that were created by the v2 workflow. A dynamic finder rather than a
     * query joining through Receipt so that the mocked datastore of unit tests (no HQL support) can run it too.
     */
    private static Set<String> findReceiptV2Ids(Shipment shipment) {
        List<Receipt> receipts = shipment.receipts?.toList()
        if (!receipts) {
            return [] as Set<String>
        }
        return ReceiptV2Marker.findAllByReceiptInList(receipts).collect { ReceiptV2Marker marker ->
            marker.receipt.id
        } as Set<String>
    }

    /**
     * Sets (adds or edits) the comment of the given receipt item.
     */
    @Transactional
    ReceiptItemCommentDto saveReceiptItemComment(ReceiptItemCommentSaveCommand command) {
        ReceiptItem receiptItem = command.receiptItem

        boolean addingNewComment = !receiptItem.comment
        receiptItem.comment = command.comment

        // Explicit .save is only needed for brand new entities
        if (addingNewComment) {
            receiptItem.save(failOnError: true)
        }

        return ReceiptItemCommentDto.from(receiptItem)
    }

    @Transactional
    void deleteReceiptItemComment(String receiptItemId) {
        ReceiptItem receiptItem = ReceiptItem.get(receiptItemId)
        if (!receiptItem) {
            throw new ObjectNotFoundException(receiptItemId, ReceiptItem.toString())
        }
        receiptItem.comment = null
    }
}
