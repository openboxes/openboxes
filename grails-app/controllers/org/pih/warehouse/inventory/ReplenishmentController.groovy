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
import org.pih.warehouse.picklist.Picklist

class ReplenishmentController {

    def index = {
        redirect(action: "create", params: params)
    }

    def create = {
        render(template: "/common/react", params: params)
    }

    def print = {
        Order transferOrder = Order.get(params.id)

        def picklist = Picklist.findByOrder(transferOrder);
        def zoneNames = picklist.picklistItems?.collect { it?.binLocation?.zone?.name }?.unique()?.sort{ a, b -> !a ? !b ? 0 : 1 : !b ? -1 : a <=> b }
        def pickListByZone = picklist.picklistItems?.groupBy{it.binLocation?.zone?.name }

        Map<Object, Map> itemsMap = [:]
        zoneNames.each { zoneName ->
            def coldChain = pickListByZone[zoneName].findAll {
                it.orderItem.product['coldChain']
            }.collect {it.orderItem }?.unique()
            def controlledSubstance = pickListByZone[zoneName].findAll {
                it.orderItem.product['controlledSubstance']
            }.collect {it.orderItem }?.unique()
            def hazardousMaterial = pickListByZone[zoneName].findAll {
                it.orderItem.product['hazardousMaterial']
            }.collect {it.orderItem }?.unique()
            def generalGoods = pickListByZone[zoneName].findAll {
                !it?.orderItem.product['coldChain'] && !it?.orderItem.product['controlledSubstance'] && !it?.orderItem.product['hazardousMaterial']
            }.collect {it.orderItem }?.unique()

            def groupedLineItemsMap = [
                    'coldChain'          : coldChain,
                    'controlledSubstance': controlledSubstance,
                    'hazardousMaterial'  : hazardousMaterial,
                    'generalGoods'       : generalGoods,
            ]
            def groupedPickListItems = pickListByZone[zoneName].groupBy {it.orderItem }
            itemsMap.put(zoneName, [lineItems: groupedLineItemsMap, pickListItems: groupedPickListItems]);
        }
        def headerItems = [
                orderNumber: transferOrder.orderNumber,
                createdBy  : transferOrder.createdBy,
                dateCreated: transferOrder.dateCreated.format('MM/dd/yyyy')
        ]

        [itemsMap: itemsMap, headerItems: headerItems]
    }
}
