package org.pih.warehouse.order

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.identification.BlankIdentifierResolver
import org.pih.warehouse.shipping.Shipment

@Transactional
class OrderIdentifierService extends IdentifierService<Order> implements BlankIdentifierResolver<Order> {

    @Override
    String getIdentifierName() {
        return "order"
    }

    @Override
    protected Integer countByIdentifier(String id) {
        Integer count = Order.countByOrderNumber(id)

        // TODO: Refactor how ids are generated for shipments so that we don't need to do this check. Uniqueness
        //       of ids on one service shouldn't depend on another. The easiest solution is to override the format
        //       for generating shipment ids to be "${custom.orderNumber}${random}" so that it's like the order
        //       number but still unique.

        // We use order.orderNumber as shipment.shipmentNumber when creating a shipment from an order so we need to
        // check that the id is unique for shipments as well. See ShipmentService.createOrUpdateShipment for details.
        // Only bother checking shipment if order doesn't already have a duplicate though.
        return count > 0 ? count : Shipment.countByShipmentNumber(id)
    }

    @Override
    List<Order> getAllUnassignedEntities() {
        return Order.findAll("from Order as o where orderNumber is null or orderNumber = ''")
    }

    @Override
    void setIdentifierOnEntity(String id, Order entity) {
        entity.orderNumber = id
    }
}
