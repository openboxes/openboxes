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

import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem

/**
 * Should not extend BaseDomainApiController since putawayItem is not a valid domain.
 */
class PutawayItemApiController {

    def putawayService

    def removingItem() {
        OrderItem orderItem = OrderItem.get(params.id)
        if (!orderItem) {
            throw new IllegalArgumentException("No putaway item found with ID ${id}")
        }
        Order order = Order.get(orderItem.order.id)

        if (order && Putaway.getPutawayStatus(order.status) == PutawayStatus.COMPLETED) {
            throw new IllegalArgumentException("Can't remove an item on completed putaway")
        }

        putawayService.deletePutawayItem(params.id)

        render status: 204
    }
}
