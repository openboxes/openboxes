/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

package org.pih.warehouse.inventory
import org.pih.warehouse.order.Order

class ReplenishmentController {

    def index = {
        redirect(action: "create", params: params)
    }

    def create = {
        render(template: "/common/react", params: params)
    }

    def print = {
        Order transferOrder = Order.get(params.id)

        def orderItems = transferOrder.orderItems.findAll { !it.parentOrderItem }.sort { it.product.name }

        def zoneNames = orderItems?.collect { it?.originBinLocation?.zone?.name }?.unique()?.sort { a, b -> !a ? !b ? 0 : 1 : !b ? -1 : a <=> b }
        def orderItemsByZone = orderItems?.groupBy { it?.originBinLocation?.zone?.name } ?: [:]
        def pickListItems = orderItems*.retrievePicklistItems()?.flatten()?.findAll { it.quantity > 0 }.groupBy { it?.orderItem }

        Map<Object, Map> itemsMap = [:]
        zoneNames.each { zoneName ->
            def groupedLineItemsMap = [
                    'coldChain'          : orderItemsByZone[zoneName].findAll { it?.product['coldChain'] },
                    'controlledSubstance': orderItemsByZone[zoneName].findAll { it?.product['controlledSubstance'] },
                    'hazardousMaterial'  : orderItemsByZone[zoneName].findAll { it?.product['hazardousMaterial'] },
                    'generalGoods'       : orderItemsByZone[zoneName].findAll { !it?.product['coldChain'] && !it?.product['controlledSubstance'] && !it?.product['hazardousMaterial'] },
            ]
            itemsMap.put(zoneName, [lineItems: groupedLineItemsMap, pickListItems: pickListItems]);
        }
        def headerItems = [
                orderNumber: transferOrder.orderNumber,
                createdBy  : transferOrder.createdBy,
                dateCreated: transferOrder.dateCreated.format('MM/dd/yyyy')
        ]

        [itemsMap: itemsMap, headerItems: headerItems]
    }
}
