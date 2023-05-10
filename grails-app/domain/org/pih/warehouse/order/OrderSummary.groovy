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

class OrderSummary {

    String id
    Order order

    Integer quantityOrdered
    Integer quantityShipped
    Integer quantityReceived
    Integer quantityCanceled
    Integer quantityInvoiced

    Integer itemsOrdered
    Integer itemsShipped
    Integer itemsReceived
    Integer itemsInvoiced

    String orderStatus
    String receiptStatus
    String shipmentStatus
    String paymentStatus
    String derivedStatus

    static mapping = {
        version false
        cache usage: "read-only"
        table "order_summary_mv"
    }

    static constraints = {
        id(nullable:false)
        order(nullable:false)

        quantityOrdered(nullable: true)
        quantityShipped(nullable: true)
        quantityReceived(nullable: true)
        quantityCanceled(nullable: true)
        quantityInvoiced(nullable: true)

        itemsOrdered(nullable: true)
        itemsShipped(nullable: true)
        itemsReceived(nullable: true)
        itemsInvoiced(nullable: true)

        orderStatus(nullable:true)
        receiptStatus(nullable:true)
        shipmentStatus(nullable:true)
        paymentStatus(nullable:true)
        derivedStatus(nullable:true)
    }

    Map toJson() {
        return [
            id: id,
            order: order,
            orderNumber: order?.orderNumber,
            quantityOrdered: quantityOrdered,
            quantityShipped: quantityShipped,
            quantityReceived: quantityReceived,
            quantityCanceled: quantityCanceled,
            quantityInvoiced: quantityInvoiced,
            orderStatus: orderStatus,
            receiptStatus: receiptStatus,
            shipmentStatus: shipmentStatus,
            paymentStatus: paymentStatus,
            derivedStatus: derivedStatus
        ]
    }
}
