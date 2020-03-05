package org.pih.warehouse.order

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.shipping.Shipment

class ShipOrderCommand {

    Order order
    List<ShipOrderItemCommand> orderItems = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(ShipOrderItemCommand.class))

    String toString() {
        return "order: ${order.id}, orderItems: ${orderItems}"
    }

}


class ShipOrderItemCommand {

    Shipment shipment
    String lotNumber
    Date expirationDate
    OrderItem orderItem
    InventoryItem inventoryItem
    BigDecimal quantityToShip


    String toString() {
        return "product: ${orderItem?.product}, shipment: ${shipment?.id}, quantityToShip: ${quantityToShip}"
    }
}

