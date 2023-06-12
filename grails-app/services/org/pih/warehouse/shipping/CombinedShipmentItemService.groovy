package org.pih.warehouse.shipping

import grails.gorm.transactions.Transactional
import org.pih.warehouse.order.OrderItem

@Transactional
class CombinedShipmentItemService {

    void addItemsToShipment(Shipment shipment, List itemsToAdd) {
        if (itemsToAdd) {
            itemsToAdd.sort { it.sortOrder }.each {
                OrderItem orderItem = OrderItem.get(it.orderItemId)
                ShipmentItem shipmentItem = new ShipmentItem()
                shipmentItem.product = orderItem.product
                shipmentItem.inventoryItem = orderItem.inventoryItem
                shipmentItem.product = orderItem.product
                shipmentItem.quantity = orderItem.quantity
                shipmentItem.recipient = orderItem.recipient
                shipmentItem.quantity = it.quantityToShip * orderItem.quantityPerUom
                shipmentItem.sortOrder = shipment.shipmentItems ? shipment.shipmentItems.size() * 100 : 0
                orderItem.addToShipmentItems(shipmentItem)
                shipment.addToShipmentItems(shipmentItem)
            }
            shipment.save()
        }
    }
}
