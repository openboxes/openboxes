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

class ShipmentMethod implements java.io.Serializable {

    static belongsTo = Shipment

    String id
    Shipper shipper                    // If you just want to store the shipper information
    ShipperService shipperService    // the selected shipping service
    String trackingNumber
    // should be part of a shipment mode: tracking number, carrier, service
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        shipper(nullable: true)
        shipperService(nullable: true)
        trackingNumber(nullable: true, maxSize: 255)
    }
}
