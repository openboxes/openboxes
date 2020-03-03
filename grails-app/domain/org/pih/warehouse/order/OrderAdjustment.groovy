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

class OrderAdjustment implements Serializable {

    String id
    BigDecimal amount
    BigDecimal percentage
    String description      // overrides description of order adjustment type
    String comments

    OrderAdjustmentType orderAdjustmentType

    // Audit fields
    Date dateCreated
    Date lastUpdated

    static transients = ['totalAdjustments']

    static belongsTo = [order: Order, orderItem: OrderItem]

    static mapping = {
        id generator: 'uuid'
    }
    static constraints = {
        order(nullable:false)
        orderItem(nullable:true)
        orderAdjustmentType(nullable:true)
        amount(nullable:true)
        percentage(nullable:true)
        description(nullable:true)
        comments(nullable: true)
    }


    def getTotalAdjustments() {
        return amount ?: percentage ? orderItem ? orderItem?.subtotal * (percentage/100) : order.subtotal * (percentage/100) : 0
    }
}
