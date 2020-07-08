/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse

class ShipmentTagLib {


    def container = { attrs, body ->


    }

    def getShipmentTypeIcon = { attrs, body ->
        if (!attrs.shipmentType) {
            out << ""
        } else if (attrs.shipmentType.name.startsWith("Air")) {
            out << "<img height='16' width='16' src='${createLinkTo(dir: 'images/icons/shipmentType', file: "ShipmentTypeAir.png")}'/>"
        } else if (attrs.shipmentType.name.startsWith("Sea")) {
            out << "<img height='16' width='16' src='${createLinkTo(dir: 'images/icons/shipmentType', file: "ShipmentTypeSea.png")}'/>"
        } else if (attrs.shipmentType.name.startsWith("Land")) {
            out << "<img height='16' width='16' src='${createLinkTo(dir: 'images/icons/shipmentType', file: "ShipmentTypeLand.png")}'/>"
        } else if (attrs.shipmentType.name.startsWith("Suitcase")) {
            out << "<img height='16' width='16' src='${createLinkTo(dir: 'images/icons/shipmentType', file: "ShipmentTypeSuitcase.png")}'/>"
        } else if (attrs.shipmentType.name.startsWith("Default")) {
            out << "<img height='16' width='16' src='${createLinkTo(dir: 'images/icons/shipmentType', file: "ShipmentTypeDefault.png")}'/>"
        } else {
            out << "<span class='fade'>${attrs.shipmentType.name}</span>"
        }
    }
}
