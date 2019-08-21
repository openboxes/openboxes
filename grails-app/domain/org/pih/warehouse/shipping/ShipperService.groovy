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

class ShipperService {

    String id
    String name                    // Name of service (e.g. UPS Ground)
    String description
    // Description of the service (e.g. Delivery usually within 1-5 Business Days)

    static belongsTo = [shipper: Shipper]

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        name(nullable: true, maxSize: 255)
        description(nullable: true, maxSize: 255)
        shipper(nullable: true)
    }


    String toString() { return "$name" }
}
