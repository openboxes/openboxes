/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.shipping

import org.pih.warehouse.inventory.Transaction

class SendShipmentCommand implements Serializable {

    String comments
    Shipment shipment
    Transaction transaction
    ShipmentWorkflow shipmentWorkflow
    Boolean debitStockOnSend = true
    Date actualShippingDate

    static constraints = {
        comments(nullable: true)
        transaction(nullable: true)
        shipment(nullable: false, validator: { value, obj -> !obj.shipment.hasShipped() })
        shipmentWorkflow(nullable: true)
        debitStockOnSend(nullable: false)
        actualShippingDate(nullable: false) //validator: { value, obj-> value > new Date()}
    }

}
