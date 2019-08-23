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

class Shipper implements java.io.Serializable {

    String id
    String name
    String description
    String trackingUrl
    String trackingFormat
    String parameterName
    Date dateCreated
    Date lastUpdated

    static hasMany = [shipperServices: ShipperService]
    static mapping = {
        id generator: 'uuid'
        shipperServices joinTable: [name: 'shipper_service', column: 'shipper_service_id', key: 'shipper_id']
    }

    static constraints = {
        name(nullable: false, maxSize: 255)
        description(nullable: true, maxSize: 255)
        trackingUrl(nullable: true, blank: true, maxSize: 255)
        trackingFormat(nullable: true, maxSize: 255)
        parameterName(nullable: true, blank: true, maxSize: 255)

        dateCreated(display: false)
        lastUpdated(display: false)
    }

    String toString() { name }

}
