/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.order

import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.UomService
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentStatusTransitionEvent
import org.springframework.context.ApplicationListener

class OrderStatusEventService implements ApplicationListener<OrderStatusEvent> {

    boolean transactional = true

    OrderService orderService
    UomService uomService

    void onApplicationEvent(OrderStatusEvent event) {
        log.info "Application event ${event} has been published!"
        OrderStatus orderStatus = event?.source
        Order order = Order.get(event?.order?.id)

        if (orderStatus == OrderStatus.PLACED) {

            order.orderItems.each { OrderItem orderItem ->

                // Convert package price to default currency
                BigDecimal packagePrice = orderItem.unitPrice * order.lookupCurrentExchangeRate()

                // If there's no product package already we create a new one
                if (!orderItem.productPackage) {
                    // Find an existing product package associated with a specific supplier
                    ProductPackage productPackage = orderItem?.productSupplier?.productPackages.find { ProductPackage productPackage ->
                        return productPackage.product == orderItem.product &&
                                productPackage.uom == orderItem.quantityUom &&
                                productPackage.quantity == orderItem.quantityPerUom

                        // If not found, then we look for a product package associated with the product
                        if (!productPackage) {
                            orderItem.product.packages.find { ProductPackage productPackage1 ->
                                return productPackage1.product == orderItem.product &&
                                        productPackage1.uom == orderItem.quantityUom &&
                                        productPackage1.quantity == orderItem.quantityPerUom
                            }
                        }
                    }

                    // If we cannot find an existing product package, create a new one
                    if (!productPackage) {
                        productPackage = new ProductPackage()
                        productPackage.product = orderItem.product
                        productPackage.productSupplier = orderItem.productSupplier
                        productPackage.name = "${orderItem?.quantityUom?.code}/${orderItem?.quantityPerUom as Integer}"
                        productPackage.uom = orderItem.quantityUom
                        productPackage.quantity = orderItem.quantityPerUom as Integer
                        productPackage.price = packagePrice
                        productPackage.save()
                    }
                    // Otherwise update the price
                    else {
                        productPackage.price = packagePrice
                    }
                    // Associate product package with order item
                    orderItem.productPackage = productPackage
                }
                // Otherwise we update the existing price
                else {
                    orderItem.productPackage.price = packagePrice
                }
            }

        }

    }

}
