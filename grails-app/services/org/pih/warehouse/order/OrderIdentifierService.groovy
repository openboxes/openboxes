package org.pih.warehouse.order

import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.identification.BlankIdentifierResolver
import org.pih.warehouse.shipping.Shipment

class OrderIdentifierService extends IdentifierService implements BlankIdentifierResolver<Order> {

    @Override
    String getEntityKey() {
        return "order"
    }

    @Override
    protected Integer countDuplicates(String orderNumber) {
        // We use order.orderNumber as shipment.shipmentNumber when creating a shipment from an order so we need to
        // check that the id is unique for shipments as well. See ShipmentService.createOrUpdateShipment for details.
        Integer count = Order.countByOrderNumber(orderNumber)

        // Only bother checking shipment if order doesn't already have a duplicate.
        return count > 0 ? count : Shipment.countByShipmentNumber(orderNumber)
    }

    @Override
    List<Order> getAllUnassignedEntities() {
        return Order.findAll("from Order as o where orderNumber is null or orderNumber = ''")
    }

    @Override
    void setIdentifierOnEntity(String orderNumber, Order order) {
        order.orderNumber = orderNumber
    }
}
