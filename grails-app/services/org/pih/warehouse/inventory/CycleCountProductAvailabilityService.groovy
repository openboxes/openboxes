package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
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
     * @return CycleCountItemsForRefresh the items that were created, updated, and deleted as a result of the refresh.
     */
    CycleCountItemsForRefresh refreshProductAvailability(CycleCount cycleCount, boolean removeOutOfStockItemsImplicitly = false, Integer countIndex = null) {

        CycleCountRefreshState refreshState = new CycleCountRefreshState(cycleCount, countIndex)

        // The refresh is based on the current product availability in all [product + bin location + lot] combinations
        // of the count. We need to fetch all available items (instead of simply looping the cycle count items) because
        // new [product + bin location + lot] combinations might have been created since the count started.
        List<AvailableItem> availableItems = getAvailableItems(cycleCount.facility, cycleCount.products)

        // Compute what changes need to be made to the cycle count items and add them to the refresh state.
        for (AvailableItem availableItem : availableItems) {
            updateRefreshStateForItem(refreshState, availableItem)
        }

        // Additionally, we need to remove any cycle count items for bins or lots that have been deleted.
        for (CycleCountItem cycleCountItem : refreshState.items) {
            updateRefreshStateIfItemDeleted(refreshState, availableItems, cycleCountItem, removeOutOfStockItemsImplicitly)
        }

        // Now that we've computed the changes that need to be made, persist them to the cycle count items
        updateCycleCountItems(refreshState)

        cycleCount.status = cycleCount.recomputeStatus()

        return refreshState.cycleCountItemsForRefresh
    }

    /**
     * Based on the current product availability of an item, determines if the matching cycle count item needs
     * to be created, updated, deleted, or left unchanged, and updates the refresh state with those potential
     * changes. The cycle count items themselves are NOT updated at this point, only the refresh state object.
     */
    private void updateRefreshStateForItem(CycleCountRefreshState refreshState, AvailableItem availableItem) {

        CycleCountItem existingCycleCountItem = refreshState.items.find{
                    it.inventoryItem == availableItem.inventoryItem &&
                    it.location == availableItem.binLocation }

        // The item does NOT yet exist in the current count. It must have been created after the count was started.
        if (!existingCycleCountItem) {
            refreshState.addItemToCreate(availableItem)
            return
        }

        BigDecimal actualQuantityOnHand = availableItem.quantityOnHand
        boolean itemHasZeroQuantityOnHand = actualQuantityOnHand == 0

        // QoH == 0 for the item (and it's not a custom row) so it should not be a part of the count any longer,
        // even if it has been counted already this round. Custom added items should not get removed from the count,
        // even if QoH == 0. We trust the user to manage items they manually added.
        if (itemHasZeroQuantityOnHand && !existingCycleCountItem.custom) {
            refreshState.addItemToDelete(existingCycleCountItem)
            return
        }

        // If the QoH has changed, we'll need to update the QoH in the cycle count item.
        if (existingCycleCountItem.quantityOnHand != actualQuantityOnHand) {
            refreshState.addItemToUpdate(existingCycleCountItem, actualQuantityOnHand)
        }
    }

    /**
     * Given a cycle count item, determine if the bin or lot associated with the item has since been deleted
     * (ie there's no matching available item), and if so, add that item to the refresh state to be deleted.
     * The cycle count items themselves are NOT updated at this point, only the refresh state object.
     */
    private void updateRefreshStateIfItemDeleted(CycleCountRefreshState refreshState,
                                                 List<AvailableItem> availableItems,
                                                 CycleCountItem cycleCountItem,
                                                 boolean removeOutOfStockItemsImplicitly) {

        // If the available item exists, there's nothing to do.
        AvailableItem availableItem = availableItems.find{
                    it.inventoryItem == cycleCountItem.inventoryItem &&
                    it.binLocation == cycleCountItem.location
        }
        if (availableItem) {
            return
        }

        // Otherwise, it does not exist, so we need to remove it from the count. This can happen if all the stock of
        // the item is moved (such as via adjust stock) while the count is in progress. However, if the item was custom
        // added to either the count or a recount, it should not be removed here, even if there's no available item.
        // We trust the user to manage items they manually added. (OBPIH-7097)
        boolean wasItemEverCustomAdded = cycleCountItem.cycleCount.cycleCountItems.any {
                    it.product == cycleCountItem.product &&
                    it.location == cycleCountItem.location &&
                    it.inventoryItem == cycleCountItem.inventoryItem &&
                    it.custom
        }
        if (!wasItemEverCustomAdded && (cycleCountItem.quantityCounted <= 0 || removeOutOfStockItemsImplicitly)) {
            refreshState.addItemToDelete(cycleCountItem)
            return
        }

        // For custom items with no available item, we still need to make sure to zero out QoH (if it isn't already).
        if (cycleCountItem.quantityOnHand != 0) {
            refreshState.addItemToUpdate(cycleCountItem, BigDecimal.valueOf(0))
        }
    }

    /**
     * Performs the create, update, and delete operations on the cycle count items to be refreshed.
     *
     * Note that any items that are not yet added to the cycle count will be added to the count here, and any existing
     * (non-custom) items whose QoH has become 0 will be removed from the count. As such, this method WILL mutate
     * the cycle count items.
     */
    private void updateCycleCountItems(CycleCountRefreshState refreshState) {
        CycleCount cycleCount = refreshState.cycleCount

        // Create
        for (AvailableItem availableItem : refreshState.itemsToCreate) {
            CycleCountItem newCycleCountItem = new CycleCountItem(
                    status: refreshState.statusForNewItems,
                    countIndex: refreshState.currentCountIndex,
                    quantityOnHand: availableItem.quantityOnHand,
                    quantityCounted: availableItem.quantityOnHand == 0 ? 0 : null,
                    cycleCount: cycleCount,
                    facility: cycleCount.facility,
                    location: availableItem.binLocation,
                    inventoryItem: availableItem.inventoryItem,
                    product: availableItem.inventoryItem.product,
                    createdBy: AuthService.currentUser,
                    updatedBy: AuthService.currentUser,
                    dateCounted: refreshState.dateCountedForNewItems,
                    custom: false,
                    assignee: refreshState.assigneeForNewItems
            )

            cycleCount.addToCycleCountItems(newCycleCountItem)
            if (!newCycleCountItem.save()) {
                throw new ValidationException("Invalid cycle count item", newCycleCountItem.errors)
            }
        }

        // Update
        for (Map.Entry<CycleCountItem, BigDecimal> entry : refreshState.itemsToUpdate) {
            entry.key.quantityOnHand = (Integer) entry.value
        }

        // Delete
        for (CycleCountItem cycleCountItem : refreshState.itemsToDelete) {
            cycleCount.removeFromCycleCountItems(cycleCountItem)
            cycleCountItem.delete()
        }
    }

    /**
     * Holds and manages the state of a cycle count product availability refresh.
     */
    private class CycleCountRefreshState {

        // Input fields
        CycleCount cycleCount

        // Computed fields
        Set<CycleCountItem> items
        int currentCountIndex
        Person assigneeForNewItems
        Date dateCountedForNewItems
        CycleCountItemStatus statusForNewItems

        // Output fields
        CycleCountItemsForRefresh cycleCountItemsForRefresh

        CycleCountRefreshState(CycleCount cycleCount, Integer countIndex) {
            this.cycleCount = cycleCount

            // We only want to refresh the items of the most recent count. We do this because previous counts are
            // already completed, so their data should be considered static. We allow countIndex to be specified
            // for the case where the current count index has no items (QoH has been zeroed out for all items)
            // and so we can't assume currentCountIndex == maxCountIndex.
            items = countIndex != null ? cycleCount.getItemsOfSpecificCount(countIndex) : cycleCount.getItemsOfMostRecentCount()
            currentCountIndex = countIndex ?: items.first().countIndex

            // Used by newly created items. We know that the frontend only supports a single assignee and date counted
            // for all items of a count, so for convenience we set the values of any new items to match.
            assigneeForNewItems = items.find{ it.assignee }?.assignee
            dateCountedForNewItems = items.find{ it.dateCounted }?.dateCounted ?: new Date()

            // This logic is shared between count and recount flows so we need to check which we're in.
            statusForNewItems = cycleCount.status.isCounting() ?
                    CycleCountItemStatus.COUNTING : CycleCountItemStatus.INVESTIGATING

            cycleCountItemsForRefresh = new CycleCountItemsForRefresh()
        }

        void addItemToCreate(AvailableItem item) {
            cycleCountItemsForRefresh.itemsToCreate.add(item)
        }

        List<AvailableItem> getItemsToCreate() {
            return cycleCountItemsForRefresh.itemsToCreate
        }

        void addItemToUpdate(CycleCountItem item, BigDecimal quantityOnHand) {
            cycleCountItemsForRefresh.itemsToUpdate.put(item, quantityOnHand)
        }

        Map<CycleCountItem, BigDecimal> getItemsToUpdate() {
            return cycleCountItemsForRefresh.itemsToUpdate
        }

        void addItemToDelete(CycleCountItem item) {
            cycleCountItemsForRefresh.itemsToDelete.add(item)
        }

        List<CycleCountItem> getItemsToDelete() {
            return cycleCountItemsForRefresh.itemsToDelete
        }
    }

    /**
     * POJO for holding the state of a product availability refresh on a cycle count.
     */
    class CycleCountItemsForRefresh {
        List<AvailableItem> itemsToCreate = []
        Map<CycleCountItem, BigDecimal> itemsToUpdate = [:]
        List<CycleCountItem> itemsToDelete = []

        /**
         * @return true if any cycle count items are going to be (or already have been) created, updated, or deleted
         *         as a result of the refresh
         */
        boolean itemsHaveChanged() {
            return !itemsToCreate.isEmpty() || !itemsToUpdate.isEmpty() || !itemsToDelete.isEmpty()
        }
    }
}
