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

import grails.gorm.transactions.Transactional
import org.springframework.context.ApplicationListener

@Transactional
class OrderStatusEventService implements ApplicationListener<OrderStatusEvent> {

    OrderService orderService

    void onApplicationEvent(OrderStatusEvent event) {
        log.info "Application event ${event} has been published!"
        OrderStatus orderStatus = event?.source
        if (orderStatus == OrderStatus.PLACED) {
            Order order = Order.get(event?.order?.id)
            order.orderItems.each { OrderItem orderItem ->
                orderService.updateProductPackage(orderItem)
                orderService.updateProductUnitPrice(orderItem)
            }
        }
    }

}
