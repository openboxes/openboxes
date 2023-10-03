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

import org.pih.warehouse.api.PartialReceipt
import org.springframework.context.ApplicationEvent

class ShipmentStatusTransitionEvent extends ApplicationEvent {

    PartialReceipt partialReceipt
    ShipmentStatusCode shipmentStatusCode

    static Map statusToNameMap = [
            // Ignore stockMovement status for now
            "CREATED": "shipment.created",
            "PENDING": "shipment.pending", // or "picklist.started"
            "SHIPPED": "shipment.shipped", // or "picklist.completed"
            "PARTIALLY_RECEIVED": "shipment.partially_received",
            "RECEIVED": "shipment.received"
    ]

    public String getEventType(){
        return statusToNameMap[this.shipmentStatusCode?.name]
    }

    ShipmentStatusTransitionEvent(Shipment shipment, ShipmentStatusCode shipmentStatusCode) {
        super(shipment)
        this.shipmentStatusCode = shipmentStatusCode
    }

    ShipmentStatusTransitionEvent(PartialReceipt partialReceipt, ShipmentStatusCode shipmentStatusCode) {
        super(partialReceipt.shipment)
        this.partialReceipt = partialReceipt
        this.shipmentStatusCode = shipmentStatusCode
    }

}
