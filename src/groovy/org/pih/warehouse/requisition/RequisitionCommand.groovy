/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.requisition

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentType

class RequisitionCommand implements Serializable {

    Requisition requisition
    Person recipient
    Date shippedOn
    Date deliveredOn
    ShipmentType shipmentType
    Shipment shipment

    User currentUser
    Location currentLocation

    Location origin
    Location destination
    Date dateRequested
    Person requestedBy


    // Not the actual request items, but rather all the line items on the receive order page.
    // This means that we might have more than one ReqeuestItemCommand per ReqeuestItem.
    def requestItems =
            LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(org.pih.warehouse.requisition.RequestItemCommand.class))

    static constraints = {
        shipmentType(nullable: false)
        recipient(nullable: false)
        // Should ship on or before the day it's delivered
        shippedOn(nullable: false,
                validator: { value, obj ->
                    println "value = " + value + ", object = " + object
                    if (value) {
                        if (!(value <= new Date())) {
                            return ["invalid.mustOccurOnOrBeforeToday", value, new Date()]
                        }
                        // subtract a day from the shippedOn date in case the dates are the same
                        if (!obj.deliveredOn.after(value - 1)) {
                            return ["invalid.mustOccurOnOrBeforeDeliveredOn", value, obj.deliveredOn]
                        }
                    }
                }
        )
        deliveredOn(nullable: false,
                validator: { value, obj ->
                    println "value = " + value + ", object = " + object
                    if (value) {
                        if (!(value <= new Date())) {
                            return ["invalid.mustOccurOnOrBeforeToday", value, new Date()]
                        }
                        if (!(value).after(obj.shippedOn - 1)) {
                            return ["invalid.mustOccurOnOrAfterShippedOn", value, obj.shippedOn]
                        }
                    }
                }
        )
        currentUser(nullable: true)
        currentLocation(nullable: true)
        origin(nullable: true)
        destination(nullable: true)
        dateRequested(nullable: true)
        requestedBy(nullable: true)
    }


}

