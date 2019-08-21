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

import org.pih.warehouse.shipping.ShipmentItem

// import java.util.Date

class OrderShipment implements Serializable {

    String id

    static mapping = {
        id generator: 'uuid'
    }

    //OrderItem orderItem
    //ShipmentItem shipmentItem
    static belongsTo = [shipmentItem: ShipmentItem, orderItem: OrderItem]

    /*
    static OrderShipment link(orderItem, shipmentItem) {
        def orderShipment = OrderShipment.findByOrderItemAndShipmentItem(orderItem, shipmentItem)
        if (!orderShipment) {
            orderShipment = new OrderShipment()
            orderShipment.orderItem = orderItem;
            orderShipment.shipmentItem = shipmentItem;
            orderShipment.save()
        }
        return orderShipment
    }

    static void unlink(orderItem, shipmentItem) {
        def orderShipment = OrderShipment.findByOrderItemAndShipmentItem(orderItem, shipmentItem)
        if (orderShipment) {
            //orderItem?.removeFromOrderShipments(orderShipment)
            //shipmentItem?.removeFromOrderShipments(orderShipment)
            orderShipment.delete()
        }
    }
    */
}
