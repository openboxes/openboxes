package org.pih.warehouse.api

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment

import javax.transaction.Transactional

@Transactional
class SearchService {

    def locationService
    def productAvailabilityService

    def globalSearch(String identifier) {
        if (!identifier) return null

        return findProduct(identifier)
                ?: findLocation(identifier)
                ?: findRequisition(identifier)
                ?: findShipment(identifier)
                ?: findContainer(identifier)
                ?: findOrder(identifier)
                ?: findTransaction(identifier)
                ?: findInventoryItem(identifier)
    }

    private Product findProduct(String identifier) {
        return Product.findByProductCode(identifier)
    }

    private Location findLocation(String identifier) {
        Location currentLocation = AuthService.currentLocation.get()
        Location internalLocation = locationService.getInternalLocation(currentLocation?.id, identifier)
        if (internalLocation) return internalLocation

        return Location.findByLocationNumber(identifier)
    }

    private Requisition findRequisition(String identifier) {
        return Requisition.findByRequestNumber(identifier)
    }

    private Shipment findShipment(String identifier) {
        return Shipment.findByShipmentNumber(identifier)
    }

    private Container findContainer(String identifier) {
        return Container.findByContainerNumber(identifier)
    }

    private Order findOrder(String identifier) {
        return Order.findByOrderNumber(identifier)
    }

    private Transaction findTransaction(String identifier) {
        return Transaction.findByTransactionNumber(identifier)
    }

    private def findInventoryItem(String identifier) {
        Location currentLocation = AuthService.currentLocation.get()
        InventoryItem inventoryItem = InventoryItem.findByLotNumber(identifier)

        if (inventoryItem) {
            List<AvailableItem> availableItems = productAvailabilityService.getAvailableItems(currentLocation, inventoryItem)
            availableItems = filterAvailableItems(availableItems)

            if (availableItems.size() == 1) {
                return availableItems[0]
            }
            return inventoryItem
        }
        return null
    }

    private List<AvailableItem> filterAvailableItems(List<AvailableItem> availableItems) {
        return availableItems.findAll { AvailableItem availableItem ->
            availableItem?.quantityOnHand > 0
        }
    }
}
