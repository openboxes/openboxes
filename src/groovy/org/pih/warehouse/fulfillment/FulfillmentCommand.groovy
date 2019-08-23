/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.fulfillment

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem

class FulfillmentCommand implements Serializable {

    Requisition request                    // original request
    Fulfillment fulfillment

    def fulfillmentItems =
            LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(FulfillmentItem.class))

    static constraints = {
        request(nullable: true)
        fulfillment(nullable: true)
    }


    /**
     *
     * @return
     */
    Map fulfillmentItemsMap() {
        return fulfillment?.fulfillmentItems?.groupBy { it.requestItem }
    }


    /**
     *
     * @param requestItem
     * @return
     */
    List fulfillmentItems(RequisitionItem requestItem) {
        def fulfillmentItemsMap = fulfillmentItemsMap()
        if (fulfillmentItemsMap) {
            return fulfillmentItemsMap.get(requestItem)
        }
        return new ArrayList()
    }


    /**
     *
     * @return
     */
    Map quantityFulfilledMap() {
        Map results = [:]
        Map fulfillmentItemsMap = fulfillmentItemsMap()
        fulfillmentItemsMap.each { requestItem, fulfillItems ->
            def quantity = fulfillItems.sum() { it.quantity }
            results[requestItem] = quantity
        }
        return results
    }

    /**
     *
     * @param requestItem
     * @return
     */
    Integer quantityFulfilledByRequestItem(RequisitionItem requestItem) {
        def quantity = quantityFulfilledMap()[requestItem]
        return quantity ?: 0
    }

}

