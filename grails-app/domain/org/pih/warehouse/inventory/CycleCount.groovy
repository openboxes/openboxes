package org.pih.warehouse.inventory

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

class CycleCount {

    String id

    Location facility

    Date dateLastRefreshed

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

    CycleCountStatus getStatus() {
        List<CycleCountItem> items = cycleCountItems
        if (items.every { it.status == CycleCountItemStatus.READY_TO_COUNT }) {
            return CycleCountStatus.REQUESTED
        }
        if (items.every { it.status == CycleCountItemStatus.REVIEWED }) {
            return CycleCountStatus.REVIEWED
        }
        if (items.every { it.status == CycleCountItemStatus.TO_REVIEW }) {
            return CycleCountStatus.TO_REVIEW
        }
        if (items.any { it.status == CycleCountItemStatus.INVESTIGATING }) {
            return CycleCountStatus.INVESTIGATING
        }
        if (items.any { it.status == CycleCountItemStatus.COUNTING }
                && !items.any { it.status in [CycleCountItemStatus.INVESTIGATING, CycleCountItemStatus.TO_REVIEW] }) {
            return CycleCountStatus.COUNTING
        }
        if (items.every { it.status in [CycleCountItemStatus.COUNTED, CycleCountItemStatus.TO_REVIEW] }) {
            return CycleCountStatus.COUNTED
        }
        if (items.every { it.status in [CycleCountItemStatus.REVIEWED, CycleCountItemStatus.APPROVED, CycleCountItemStatus.REJECTED] }) {
            return CycleCountStatus.COMPLETED
        }
        return null
    }
}
