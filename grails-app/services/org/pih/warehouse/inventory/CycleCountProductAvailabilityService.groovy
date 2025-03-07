package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

/**
 * Responsible for all product availability related functionality for cycle counts.
 */
@Transactional
class CycleCountProductAvailabilityService {

    ProductAvailabilityService productAvailabilityService

    /**
     * Get product availability for a given facility and product. Filter out items with QOH == 0.
     */
    List<AvailableItem> getAvailableItems(Location facility, Product product) {
        return getAvailableItems(facility, [product])
    }

    /**
     * Get product availability for a given facility and list of products. Filter out items with QOH == 0.
     */
    List<AvailableItem> getAvailableItems(Location facility, List<Product> products) {
        List<String> productIds = products.collect { it.id }
        return productAvailabilityService.getAvailableItems(facility, productIds, false, true)
    }

    /**
     * Refreshes a given cycle count by fetching up-to-date product availability for the products associated with
     * the count and updating the QoH for each of the items in the most recent count.
     *
     * @return true if one or more cycle count items were added to the count or had their QoH modified.
     */
    boolean refreshProductAvailability(CycleCount cycleCount) {
        boolean itemsHaveChanged = false

        // We only want to refresh the items of the most recent count. We do this because previous counts are already
        // completed, so their data should be considered static.
        Set<CycleCountItem> cycleCountItems = cycleCount.getItemsOfMostRecentCount()
        int currentCountIndex = cycleCountItems.first().countIndex
        Location facility = cycleCount.facility

        // Used by newly created items. We know that the frontend only supports a single assignee and date counted
        // for all items of a count, so for convenience we set the values of any new items to match.
        User assigneeForNewItems = cycleCountItems.find{ it.assignee }?.assignee
        Date dateCountedForNewItems = cycleCountItems.find{ it.dateCounted }?.dateCounted ?: new Date()

        // This logic is shared between count and recount flows so we need to check which we're in.
        CycleCountItemStatus statusForNewItems = cycleCount.status.isCounting() ?
                CycleCountItemStatus.COUNTING : CycleCountItemStatus.INVESTIGATING

        // The refresh is based on the current product availability in all [bin location + lot] combinations for all
        // the products of the count. We do this because new items might have been created since the count started.
        List<AvailableItem> availableItems = getAvailableItems(facility, cycleCount.products)
        for (AvailableItem availableItem : availableItems) {

            boolean itemHasChanged = updateItem(cycleCount, cycleCountItems, availableItem, facility, currentCountIndex,
                    assigneeForNewItems, dateCountedForNewItems, statusForNewItems)

            // Once itemsHaveChanged becomes true, keep it true. We could collect more info here to build and return
            // a map of data like {cycleCountItem : {oldQoH: x, newQoH: y}}, but we don't need that much info yet.
            itemsHaveChanged = itemsHaveChanged || itemHasChanged
        }

        // Additionally, we need to remove any cycle count items for bins or lots that have been deleted (which we
        // assume is the case when we have the cycle count item but not a matching available item).
        for (CycleCountItem cycleCountItem : cycleCountItems) {

            AvailableItem availableItem = availableItems.find{
                        it.inventoryItem == cycleCountItem.inventoryItem &&
                        it.binLocation == cycleCountItem.location }

            if (!availableItem) {
                // Custom items should always stay, even if there's no available item. Just make sure to zero out QoH.
                if (cycleCountItem.custom) {
                    if (cycleCountItem.quantityOnHand != 0) {
                        cycleCountItem.quantityOnHand = 0
                        itemsHaveChanged = true
                    }
                }
                else {
                    cycleCount.removeFromCycleCountItems(cycleCountItem)
                    cycleCountItem.delete()
                    itemsHaveChanged = true
                }
            }
        }

        return itemsHaveChanged
    }

    /**
     * Refreshes (or creates or deletes) a single cycle count item based on the given product availability record.
     *
     * Note that any items that are not yet added to the cycle count will be added to the count here, and any existing
     * (non-custom) items whose QoH has become 0 will be removed from the count. As such, this method WILL mutate
     * the cycle count items.
     *
     * @return true if a new cycle count item was added to the count or an existing one had their QoH modified.
     */
    private boolean updateItem(CycleCount cycleCount, Set<CycleCountItem> currentCountItems,
                               AvailableItem availableItem, Location facility, int currentCountIndex,
                               User assigneeForNewItems, Date dateCountedForNewItems,
                               CycleCountItemStatus statusForNewItems) {

        CycleCountItem existingCycleCountItem = currentCountItems.find{
                    it.inventoryItem == availableItem.inventoryItem &&
                    it.location == availableItem.binLocation }

        boolean itemHasZeroQuantityOnHand = availableItem.quantityOnHand == 0

        // The item already exists in the current count.
        if (existingCycleCountItem) {
            // Custom rows should always remain in the count, even if QoH == 0.
            if (!itemHasZeroQuantityOnHand || existingCycleCountItem.custom) {
                // If the QoH has changed, update the cycle count item.
                if (existingCycleCountItem.quantityOnHand != availableItem.quantityOnHand) {
                    existingCycleCountItem.quantityOnHand = (Integer) availableItem.quantityOnHand
                    return true
                }
                // Otherwise nothing has changed.
                return false
            }

            // QoH == 0 for the item and it's not a custom row so it should not be a part of the count any longer,
            // even if it has been counted already this round.
            cycleCount.removeFromCycleCountItems(existingCycleCountItem)
            existingCycleCountItem.delete()
            return true
        }

        // The item does NOT yet exist in the current count. It must have been created since the count started.
        // We make sure to add it to the count now so that it is accounted for.
        CycleCountItem newCycleCountItem = new CycleCountItem(
                status: statusForNewItems,
                countIndex: currentCountIndex,
                quantityOnHand: availableItem.quantityOnHand,
                quantityCounted: availableItem.quantityOnHand == 0 ? 0 : null,
                cycleCount: cycleCount,
                facility: facility,
                location: availableItem.binLocation,
                inventoryItem: availableItem.inventoryItem,
                product: availableItem.inventoryItem.product,
                createdBy: AuthService.currentUser,
                updatedBy: AuthService.currentUser,
                dateCounted: dateCountedForNewItems,
                custom: false,
                assignee: assigneeForNewItems
        )

        cycleCount.addToCycleCountItems(newCycleCountItem)
        if (!newCycleCountItem.save()) {
            throw new ValidationException("Invalid cycle count item", newCycleCountItem.errors)
        }

        return true
    }
}
