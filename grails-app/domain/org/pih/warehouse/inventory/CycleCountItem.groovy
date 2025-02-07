package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class CycleCountItem {

    String id

    Location facility

    CycleCount cycleCount

    InventoryItem inventoryItem

    Product product

    User assignee

    Integer countIndex

    CycleCountItemStatus status

    Integer quantityOnHand

    Integer quantityCounted

    DiscrepancyReasonCode discrepancyReasonCode

    String comment

    Boolean custom

    Date dateCounted

    Date dateCreated

    Date lastUpdated

    User createdBy

    User updatedBy

    private boolean statusChanged

    def afterInsert() {
//        cascadeSaveCycleCount()
    }

    def beforeUpdate() {
        statusChanged = this.isDirty('status')
    }

    def afterUpdate() {
        // If the status hasn't changed, then the cycle count status won't change either, so no need to cascade save.
//        TODO: THIS DOESN'T WORK FOR SOME REASON!
//        if (statusChanged) {
//            cascadeSaveCycleCount()
//        }
    }

    CycleCountItemDto toDto() {
        return new CycleCountItemDto(
                id: id,
                facility: facility.toBaseJson(),
                product: product,
                inventoryItem: inventoryItem,
                countIndex: countIndex,
                status: status,
                quantityOnHand: quantityOnHand,
                quantityCounted: quantityCounted,
                discrepancyReasonCode: discrepancyReasonCode,
                dateCounted: dateCounted,
                comment: comment,
                custom: custom
        )
    }

    static transients = ["statusChanged"]

    static constraints = {
        dateCounted(nullable: true)
        comment(nullable: true)
        assignee(nullable: true)
        quantityCounted(nullable: true)
        discrepancyReasonCode(nullable: true)
    }

    /**
     * Recomputes the cycle count status and save it.
     *
     * Cycle count status is derived from cycle count item status, and so even though cycle count is the "parent"
     * entity, we need to cascade save from the cycle count item to the cycle count to ensure the cycle count status
     * stays accurate.
     *
     * Note that to recompute the cycle count status we need to load in ALL of its cycle count items. Because this
     * method is called in afterInsert() and afterUpdate(), whenever we call CycleCountItem.save(), it will load ALL
     * the items of the cycle count into memory. This is acceptable only because a cycle count will only ever have
     * a fixed (ie not growing infinitely over time) and small (almost always <50) number of items.
     *
     * If we update multiple CycleCountItems all at the same time, this will trigger this method to be called multiple
     * times, making multiple updates to cycle count. However, because we don't flush the hibernate cache after each
     * call, ultimately only a single update call will be made to the database.
     */
    private void cascadeSaveCycleCount() {
        cycleCount.status = cycleCount.recomputeStatus()
        cycleCount.save(failOnError: true)
    }
}
