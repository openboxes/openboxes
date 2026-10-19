package org.pih.warehouse.common.domain.builder.order

import groovy.transform.InheritConstructors

import org.pih.warehouse.common.domain.builder.base.TestBuilder
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product

@InheritConstructors
class OrderItemTestBuilder extends TestBuilder<OrderItem> {

    @Override
    protected Map<String, Object> getDefaults() {
        return [
                quantityPerUom: 1,
                unitPrice: 1,
        ] as Map<String, Object>
    }

    OrderItemTestBuilder order(Order order) {
        args.order = order
        return this
    }

    OrderItemTestBuilder product(Product product) {
        args.product = product
        return this
    }

    OrderItemTestBuilder quantity(Integer quantity) {
        args.quantity = quantity
        return this
    }

    OrderItemTestBuilder quantityUom(UnitOfMeasure quantityUom, BigDecimal quantityPerUom) {
        args.quantityUom = quantityUom
        args.quantityPerUom = quantityPerUom
        return this
    }
}
