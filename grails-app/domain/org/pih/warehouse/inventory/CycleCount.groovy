package org.pih.warehouse.inventory

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

class CycleCount {

    String id

    Location facility

    Date dateLastRefreshed

    CycleCountStatus status

    Date dateCreated

    Date lastUpdated

    User createdBy

    User updatedBy

    def beforeInsert() {
        createdBy = AuthService.currentUser
        updatedBy = AuthService.currentUser
    }

    def beforeUpdate() {
        updatedBy = AuthService.currentUser
    }

    static constraints = {
        id generator: "uuid"
        createdBy(nullable: true)
        updatedBy(nullable: true)
    }

    List<CycleCountItem> getCycleCountItems() {
        return CycleCountItem.findAllByCycleCount(this)
    }

    /**
     * Determines what the CycleCountStatus should be for the cycle count.
     *
     * We don't need to call this in beforeInsert() or beforeUpdate() because the cycle count's status is determined
     * entirely by the status of its cycle count items, and so any change to fields in the CycleCount itself won't
     * actually ever change its status. Instead we make sure to call this method in CycleCountItem.
     */
    CycleCountStatus recomputeStatus(List<CycleCountItem> items) {
//        List<CycleCountItem> items = cycleCountItems
        if (!items || items.every { it.status == CycleCountItemStatus.READY_TO_COUNT }) {
            return CycleCountStatus.REQUESTED
        }
        if (items.every { it.status == CycleCountItemStatus.REVIEWED }) {
            return CycleCountStatus.REVIEWED
        }
        if (items.every { it.status == CycleCountItemStatus.READY_TO_REVIEW }) {
            return CycleCountStatus.READY_TO_REVIEW
        }
        if (items.any { it.status == CycleCountItemStatus.INVESTIGATING }) {
            return CycleCountStatus.INVESTIGATING
        }
        if (items.any { it.status == CycleCountItemStatus.COUNTING }
                && !items.any { it.status in [CycleCountItemStatus.INVESTIGATING, CycleCountItemStatus.READY_TO_REVIEW] }) {
            return CycleCountStatus.COUNTING
        }
        if (items.every { it.status in [CycleCountItemStatus.COUNTED, CycleCountItemStatus.READY_TO_REVIEW] }) {
            return CycleCountStatus.COUNTED
        }
        if (items.every { it.status in [CycleCountItemStatus.REVIEWED, CycleCountItemStatus.APPROVED, CycleCountItemStatus.REJECTED] }) {
            return CycleCountStatus.COMPLETED
        }
        return null
    }
}
