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

import grails.test.*
import org.pih.warehouse.core.Location
import grails.test.mixin.Mock

@Mock([Order, OrderItem])
class OrderTests {

    void testListOrderItemsWithEmptyOrder() {
        Order order = new Order()
        assertNotNull order.listOrderItems()
    }

    void testListOrderItems() {
        Order order = new Order()
        order.addToOrderItems(new OrderItem([id: 1, dateCreated: new Date()-2]))
        order.addToOrderItems(new OrderItem([id: 2, dateCreated: new Date()-3]))
        order.addToOrderItems(new OrderItem([id: 3, dateCreated: new Date()-1]))
        println order.orderItems
        println order.listOrderItems()
        def orderItems = order.listOrderItems()
        assertNotNull orderItems
        assert orderItems.size() == 3

        List<Integer> sortedItems = order.listOrderItems()*.id
        // Was having an issue with the test, but then realized it was due to fact that IDs are strings
        //assertEquals ([2,1,3], sortedItems)
        assertEquals (["2","1","3"], sortedItems)

        // For good measure
        assertEquals "2", sortedItems[0]
        assertEquals "1", sortedItems[1]
        assertEquals "3", sortedItems[2]
    }


    void testTotalPriceWithEmptyOrder() {
        Order order = new Order()
        assertEquals(0, order.totalPrice())
    }
}
