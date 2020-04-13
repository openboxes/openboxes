package org.pih.warehouse.order

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

class ShipOrderCommand {

    Order order
    Shipment shipment
    List<ShipOrderItemCommand> shipOrderItems = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(ShipOrderItemCommand.class))

    String toString() {
        return toJson().toMapString()
    }

    List<ShipOrderItemCommand> getShipOrderItemsByOrderItem(OrderItem orderItem) {
        return shipOrderItems.findAll { ShipOrderItemCommand shipOrderItem -> shipOrderItem.orderItem == orderItem }
    }

    Map toJson() {
        return [
                order     : order,
                shipment  : shipment,
                shipOrderItems: shipOrderItems,
        ]
    }

}


class ShipOrderItemCommand {

    String lotNumber
    Date expirationDate
    OrderItem orderItem
    ShipmentItem shipmentItem
    InventoryItem inventoryItem
    BigDecimal quantityToShip
    BigDecimal quantityMinimum
    BigDecimal quantityMaximum


    String toString() {
        return toJson().toMapString()
    }

    Map toJson() {
        return [
                order         : orderItem?.order,
                orderItem     : orderItem,
                shipmentItem  : shipmentItem,
                product       : orderItem?.product,
                shipment      : shipmentItem?.shipment,
                quantityToShip: quantityToShip
        ]
    }

}

