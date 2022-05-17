/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
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

class SearchService {

    def productService
    def locationService
    def shipmentService
    def productAvailabilityService

    boolean transactional = true

    def globalSearch(String identifier) {

        if (!identifier) {
            return
        }

        Product product = Product.findByProductCode(identifier)
        if (product) return product
        else {
            Location currentLocation = AuthService.currentLocation.get()
            Location internalLocation = locationService.getInternalLocation(currentLocation?.id, identifier)
            if (internalLocation) return internalLocation
            else {
                Location location = Location.findByLocationNumber(identifier)
                if (location) return location
                else {
                    Requisition requisition = Requisition.findByRequestNumber(identifier)
                    if (requisition) return requisition
                    else {
                        Shipment shipment = Shipment.findByShipmentNumber(identifier)
                        if (shipment) return shipment
                        else {
                            Container container = Container.findByContainerNumber(identifier)
                            if (container) return container
                            else {
                                Order order = Order.findByOrderNumber(identifier)
                                if (order) return order
                                else {
                                    Transaction transaction = Transaction.findByTransactionNumber(identifier)
                                    if (transaction) return transaction
                                    else {
                                        InventoryItem inventoryItem = InventoryItem.findByLotNumber(identifier)
                                        if (inventoryItem) {
                                            List<AvailableItem> availableItems =
                                                    productAvailabilityService.getAvailableItems(currentLocation, inventoryItem)

                                            // Filter out inventory items that don't have on hand quantity
                                            availableItems =
                                                    availableItems.findAll { AvailableItem availableItem -> availableItem?.quantityOnHand > 0 }
                                            if (availableItems && availableItems.size() == 1) {
                                                return availableItems[0]
                                            }
                                            return inventoryItem
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
