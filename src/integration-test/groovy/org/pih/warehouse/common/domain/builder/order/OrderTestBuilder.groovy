package org.pih.warehouse.common.domain.builder.order

import groovy.transform.InheritConstructors

import org.pih.warehouse.common.domain.builder.base.TestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode

@InheritConstructors
class OrderTestBuilder extends TestBuilder<Order> {

    @Override
    protected Map<String, Object> getDefaults() {
        return [
                name: randomUtil.randomStringFieldValue("Order"),
                orderType: OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()),
                orderedBy: User.findByUsername("admin"),
                dateOrdered: new Date(),
        ] as Map<String, Object>
    }

    OrderTestBuilder origin(Location origin) {
        args.origin = origin
        args.originParty = origin.organization
        return this
    }

    OrderTestBuilder destination(Location destination) {
        args.destination = destination
        args.destinationParty = destination.organization
        return this
    }
}
