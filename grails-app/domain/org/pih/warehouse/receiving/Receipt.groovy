/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.receiving

import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.shipping.Shipment

class Receipt implements Serializable, Comparable<Receipt> {

    String id
    String receiptNumber
    ReceiptStatusCode receiptStatusCode = ReceiptStatusCode.PENDING
    Date expectedDeliveryDate
    Date actualDeliveryDate
    Person recipient
    Date dateCreated
    Date lastUpdated

    static hasOne = [transaction: Transaction]
    static hasMany = [receiptItems: ReceiptItem]
    static belongsTo = [shipment: Shipment]

    static mapping = {
        id generator: 'uuid'
        receiptItems cascade: "all-delete-orphan"
    }

    // Constraints
    static constraints = {
        transaction(nullable: true)
        shipment(nullable: true)
        recipient(nullable: true)
        receiptNumber(nullable: true, blank: false)
        receiptStatusCode(nullable: true)
        expectedDeliveryDate(nullable: true)
        actualDeliveryDate(nullable: false,
                validator: { value, obj ->
                    // can't be delivered before it is shipped!
                    if (!(value + 1).after(obj.shipment.actualShippingDate)) {
                        return ["invalid.mustOccurOnOrAfterActualShippingDate", value, obj.shipment.actualShippingDate]
                    }
                }
        )
    }

    int compareTo(Receipt otherReceipt) {
        return dateCreated <=> otherReceipt?.dateCreated ?:
                lastUpdated <=> otherReceipt?.lastUpdated ?:
                        id <=> otherReceipt?.id
    }


}
