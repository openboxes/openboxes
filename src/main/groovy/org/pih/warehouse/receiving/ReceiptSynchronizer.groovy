package org.pih.warehouse.receiving

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.api.receiving.v2.ReceiptV2Service
import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

/**
 * Keeps the lines of a pending receipt in line with the shipment items they receive against: the original line
 * every still-receivable shipment item carries on the receipt, and the allocation of the quantities shipped
 * across the lines of a shipment item.
 *
 * Writes within the transaction of the caller, so it is the calling endpoint that decides what a failure rolls
 * back (see {@link ReceiptV2Service#syncReceiptLines}).
 */
@Component
class ReceiptSynchronizer {

    @Autowired
    ReceiptService receiptService  // Inject old receipt service to reuse bin creation logic

    @Autowired
    ReceiptItemFactory receiptItemFactory

    @Autowired
    ShipmentReceivingCalculator shipmentReceivingCalculator

    /**
     * Syncs the lines of a pending receipt with what its shipment currently has left to receive, by:
     *  1. creating the original line (isSplitItem: false) of every still-receivable shipment item that has none,
     *     exactly as a receipt being started gets them: empty, carrying the shipment item's full quantity shipped
     *     (see {@link ReceiptItemFactory#createReceiptItemFromShipmentItem}),
     *  2. re-allocating the quantities shipped of its lines to the v2 convention (see
     *     {@link #reallocateQuantitiesShipped}).
     * Quantities already received are never touched, and neither are shipment items that previous receipts have
     * already consumed: they get no line, exactly as when a receipt is started.
     *
     * Step 1 is not conditional on the workflow, because two kinds of receipt need it: an old-workflow receipt
     * arrives missing most of its lines (that workflow only persisted the ones the user touched), and a v2 receipt
     * has all of its lines only as of the moment it was started - anything that later reopens a shipment item
     * (rolling back a completed receipt, adding a shipment item, raising a quantity) leaves it without one.
     *
     * Both steps are no-ops on a receipt already in the v2 shape, so this is idempotent.
     */
    void syncLines(Shipment shipment, Receipt receipt) {
        Set<ShipmentItem> shipmentItemsMissingOriginalLine = findShipmentItemsMissingOriginalLine(shipment, receipt)
        createMissingReceiptItems(shipment, receipt, shipmentItemsMissingOriginalLine)

        reallocateQuantitiesShipped(shipment, receipt)
    }

    /**
     * The shipment items of the shipment that still have something left to receive but carry no original line
     * (see {@link ReceiptItem#isOriginalLine}) on the given receipt - the lines {@link #syncLines} has to create.
     *
     * Shipment items already consumed by previous receipts are left out: they are expected to have no line at all
     * (see {@link ReceiptItemFactory#createReceiptItemFromShipmentItem}), so a missing line there says nothing about
     * which workflow wrote the receipt.
     */
    private Set<ShipmentItem> findShipmentItemsMissingOriginalLine(Shipment shipment, Receipt receipt) {
        if (!shipment.shipmentItems) {
            return [] as Set<ShipmentItem>
        }

        Map<String, List<ReceiptItem>> linesByShipmentItemId = groupLinesByShipmentItemId(receipt)

        return shipment.shipmentItems.findAll { ShipmentItem shipmentItem ->
            List<ReceiptItem> receiptItems = linesByShipmentItemId.get(shipmentItem.id)
            boolean hasOriginalLine = receiptItems?.any { ReceiptItem line -> line.isOriginalLine() }
            return !hasOriginalLine &&
                    shipmentReceivingCalculator.getShipmentItemQuantityRemaining(shipmentItem) > 0
        } as Set<ShipmentItem>
    }

    /**
     * Creates the original line of each of the given shipment items on the receipt, all sharing the shipment's
     * temporary receiving bin (see {@link ReceiptItemFactory#createReceiptItemFromShipmentItem}). Creating the bin is
     * left to the point where there is a line to put in it, so a receipt with nothing missing is untouched.
     */
    private void createMissingReceiptItems(
            Shipment shipment, Receipt receipt, Set<ShipmentItem> shipmentItemsMissingOriginalLine) {
        if (!shipmentItemsMissingOriginalLine) {
            return
        }

        Location receivingBin = receiptService.createTemporaryReceivingBin(shipment)
        for (ShipmentItem shipmentItem : shipmentItemsMissingOriginalLine) {
            receiptItemFactory.createReceiptItemFromShipmentItem(receipt, shipmentItem, receivingBin)
        }
    }

    /**
     * Re-allocates the quantities shipped of a receipt's lines to the v2 convention, per shipment item: its
     * original line carries the shipment item's full quantity while its split lines carry zero. The old workflow
     * instead split the quantity shipped across the lines it created, which the
     * cancel-remaining logic of a completion does not expect (see
     * {@link ReceiptV2Service#cancelRemainingQuantities}).
     *
     * Which line is which is taken from the split flag the old workflow already left on it, and left as it is: it
     * persisted exactly one unflagged line per shipment item (a split is only ever added alongside it, and it is
     * never deleted), and step 1 of the sync has just created that line for any shipment item that had none.
     * A line carrying no flag at all counts as the original, as it does everywhere else.
     */
    private static void reallocateQuantitiesShipped(Shipment shipment, Receipt receipt) {
        if (!shipment.shipmentItems) {
            return
        }

        Map<String, List<ReceiptItem>> linesByShipmentItemId = groupLinesByShipmentItemId(receipt)

        for (ShipmentItem shipmentItem : shipment.shipmentItems) {
            List<ReceiptItem> receiptItems = linesByShipmentItemId.get(shipmentItem.id) ?: []
            for (ReceiptItem line : receiptItems) {
                line.quantityShipped = line.isOriginalLine() ? shipmentItem.quantity : 0
            }
        }
    }

    /**
     * The lines of the receipt grouped by the id of the shipment item they receive against. Callers only ever look
     * a shipment item's own id up, so the group of lines that have none (the association is nullable, though no
     * code writes a line without one) is simply never read.
     */
    private static Map<String, List<ReceiptItem>> groupLinesByShipmentItemId(Receipt receipt) {
        return (receipt.receiptItems ?: []).groupBy { ReceiptItem line -> line.shipmentItem?.id }
    }
}
