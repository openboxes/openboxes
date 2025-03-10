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
        status = recomputeStatus()
    }

    def beforeUpdate() {
        updatedBy = AuthService.currentUser
        status = recomputeStatus()
    }

    SortedSet cycleCountItems

    static hasMany = [
            /*
             * We've moved away from using "hasMany" in our code whenever possible due to performance concerns around
             * inserting new child records (the whole set of items will always be fetched when addToCycleCountItems is
             * called). In this specific case we've deemed it okay because:
             *
             * 1) cycleCountItems is a relatively small dataset (almost always < 100 elements, usually < 10) and it is
             *    not ever-growing because a cycle count doesn't stay open forever.
             *
             * 2) When we're working with a cycle count, we almost always want to fetch all of its items anyways for
             *    validation purposes.
             *
             * Using hasMany greatly simplifies recomputing the cycle count status since it allows the cycle count to
             * maintain an accurate list of items (which it uses to compute its status). Still, the disadvantage is
             * important to note, and we should strive to replace this hasMany with a better option in the future.
             */
            cycleCountItems: CycleCountItem,
    ]

    static mapping = {
        cycleCountItems cascade: 'all-delete-orphan'
    }

    static constraints = {
        id generator: "uuid"
        createdBy(nullable: true)
        updatedBy(nullable: true)
    }

    /**
     * Fetch the cycle count request associated with this cycle count.
     * We can safely do this because CycleCountRequest has a 1:1 association with cycle count.
     */
    CycleCountRequest getCycleCountRequest() {
        return CycleCountRequest.findByCycleCount(this)
    }

    /**
     * Determines what the CycleCountStatus should be for the cycle count.
     */
    CycleCountStatus recomputeStatus() {
        if (!cycleCountItems || cycleCountItems.every { it.status == CycleCountItemStatus.READY_TO_COUNT }) {
            return CycleCountStatus.REQUESTED
        }
        if (cycleCountItems.every { it.status == CycleCountItemStatus.REVIEWED }) {
            return CycleCountStatus.REVIEWED
        }
        if (cycleCountItems.every { it.status == CycleCountItemStatus.READY_TO_REVIEW }) {
            return CycleCountStatus.READY_TO_REVIEW
        }
        if (cycleCountItems.any { it.status == CycleCountItemStatus.INVESTIGATING }) {
            return CycleCountStatus.INVESTIGATING
        }
        if (cycleCountItems.any { it.status == CycleCountItemStatus.COUNTING }
                && !cycleCountItems.any { it.status in [CycleCountItemStatus.INVESTIGATING, CycleCountItemStatus.READY_TO_REVIEW] }) {
            return CycleCountStatus.COUNTING
        }
        if (cycleCountItems.every { it.status in [CycleCountItemStatus.COUNTED, CycleCountItemStatus.READY_TO_REVIEW] }) {
            return CycleCountStatus.COUNTED
        }
        if (cycleCountItems.every { it.status in [CycleCountItemStatus.REVIEWED, CycleCountItemStatus.APPROVED, CycleCountItemStatus.REJECTED] }) {
            return CycleCountStatus.COMPLETED
        }
        return null
    }

    /**
     * @return The largest count index of all the cycle count items. Helps determine what count we're on.
     */
    Integer getMaxCountIndex() {
        return cycleCountItems.max{ it.countIndex }?.countIndex
    }

    /**
     * @return All CycleCountItems with the highest countIndex, representing the newest (re)count.
     */
    Set<CycleCountItem> getItemsOfMostRecentCount() {
        Integer countIndex = maxCountIndex
        return cycleCountItems.findAll{ it.countIndex == countIndex}
    }
}
