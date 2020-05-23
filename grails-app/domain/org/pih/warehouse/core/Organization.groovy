/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderTypeCode

class Organization extends Party {

    String id
    String code
    String name
    String description

    Date dateCreated
    Date lastUpdated

    Map<IdentifierTypeCode, Integer> sequences

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        code(nullable: false, unique: true,
                minSize: ConfigurationHolder.config.openboxes.identifier.organization.minSize,
                maxSize: ConfigurationHolder.config.openboxes.identifier.organization.maxSize)
        name(nullable: false, maxSize: 255)
        description(nullable: true, maxSize: 255)
    }


    String toString() {
        return name
    }

    int compareTo(Organization obj) {
        return name <=> obj.name ?:
                dateCreated <=> obj.dateCreated ?:
                        id <=> obj.id
    }

    boolean hasPurchaseOrders() {
        return Order.createCriteria().get {
            projections {
                count("id")
            }
            eq("orderTypeCode", OrderTypeCode.PURCHASE_ORDER)
            eq("destinationParty", this)
        }

    }

}
