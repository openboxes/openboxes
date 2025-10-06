package org.pih.warehouse.inventory

import java.time.Instant

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class CycleCount {

    String id

    Location facility

    Instant dateLastRefreshed

    CycleCountStatus status

    Instant dateCreated

    Instant lastUpdated

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

    SortedSet<CycleCountItem> cycleCountItems

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
        Set<CycleCountItem> items = itemsOfMostRecentCount
        if (items.every { it.status.isCompleted() }) {
            return CycleCountStatus.COMPLETED
        }
        if (items.any { it.status == CycleCountItemStatus.INVESTIGATING }) {
            return CycleCountStatus.INVESTIGATING
        }
        if (items.any { it.status == CycleCountItemStatus.COUNTING }) {
            return CycleCountStatus.COUNTING
        }
        if (items.any { it.status == CycleCountItemStatus.COUNTED }) {
            return CycleCountStatus.COUNTED
        }
        if (items.any { it.status == CycleCountItemStatus.READY_TO_COUNT }) {
            return CycleCountStatus.REQUESTED
        }
        // If no matching if statement found, throw an exception to quickly catch the bug, instead of
        // allowing to persist some stale data
        throw new IllegalArgumentException("No matching cycle count status found")
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
        return cycleCountItems.findAll { it.countIndex == countIndex}
    }

    Integer getNumberOfItemsOfMostRecentCount() {
        return getItemsOfMostRecentCount()?.size()
    }

    Set<CycleCountItem> getItemsOfSpecificCount(Integer countIndex) {
        return cycleCountItems.findAll { it.countIndex == countIndex }
    }

    /**
     * @return a list of all the products being counted by the cycle count.
     */
    List<Product> getProducts() {
        return cycleCountItems.collect{ it.product }.unique{ it.id }
    }

    /**
     * @return the cycle count item matching the given input (which uniquely identifies the item within the count).
     */
    CycleCountItem getCycleCountItem(
            Product product, Location binLocation, InventoryItem inventoryItem, int countIndex) {

        return cycleCountItems.find{
                    it.product == product &&
                    it.location == binLocation &&
                    it.inventoryItem == inventoryItem &&
                    it.countIndex == countIndex }
    }

    Map toJson() {
        return [
                id: id,
                facility: facility,
                dateLastRefreshed: dateLastRefreshed,
                status: status,
                dateCreated: dateCreated,
                lastUpdated: lastUpdated,
                createdBy: createdBy,
                updatedBy: updatedBy
        ]
    }
}
