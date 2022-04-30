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

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.api.BaseDomainApiController
import org.pih.warehouse.core.Location
import org.pih.warehouse.requisition.RequisitionStatus

class ShipmentApiController extends BaseDomainApiController {

    def shipmentService

    def list = {
        Location origin = params.origin ? Location.get(params?.origin?.id) : null
        Location destination = params.destination ? Location.get(params?.destination?.id) : null
        ShipmentStatusCode shipmentStatusCode = params.shipmentStatusCode ? params.shipmentStatusCode as ShipmentStatusCode : null
        List<RequisitionStatus> requisitionStatuses = params.list("requisitionStatus").collect { it as RequisitionStatus }
        List<Shipment> shipments = shipmentService.getShipmentsByLocation(origin, destination, shipmentStatusCode, requisitionStatuses)
        render ([data: shipments] as JSON)
    }

    def read = {
        Shipment shipment = shipmentService.getShipment(params.id)
        render ([data:shipment] as JSON)
    }
}
