package org.pih.warehouse.inventory

import grails.validation.ValidationException

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
        status = recomputeStatus()
    }

    def beforeUpdate() {
        updatedBy = AuthService.currentUser
        status = recomputeStatus()
    }

    static constraints = {
        id generator: "uuid"
        createdBy(nullable: true)
        updatedBy(nullable: true)
    }

    List<CycleCountItem> getCycleCountItems() {
        return CycleCountItem.findAllByCycleCount(this)
    }

    CycleCount saveCountAndItems(List<CycleCountItem> items) {
        if (!save()) {
            throw new ValidationException("Invalid cycle count", errors)
        }

        for (item in items) {
            item.save()
            if (!item.save()) {
                throw new ValidationException("Invalid cycle count item", item.errors)
            }
        }

        // Hi Kacper :)
        // This doesn't work because the call to cycleCountItems returns an empty list! For some reason the items
        // that we're saving in the above for loop aren't getting included in the list that we have in the context.
        // Note that we can't just recompute from the items that we have in the method here unless we can somehow
        // enforce that the items list always contains ALL of the items for the count (which is why I just switched
        // to the hasMany, because we always need to fetch all the items anyways to compare against).
        status = recomputeStatus()
        if (!save()) {
            throw new ValidationException("Invalid cycle count", errors)
        }

        return this
    }

    /**
     * Determines what the CycleCountStatus should be for the cycle count.
     *
     * We don't want to call this in beforeInsert() or beforeUpdate() because the cycle count's status is determined
     * entirely by the status of its cycle count items, and so any change to fields in the CycleCount itself won't
     * ever change its status. Instead we opt to only recompute the status when explicitly told to do so.
     */
    CycleCountStatus recomputeStatus() {
        List<CycleCountItem> items = cycleCountItems

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
