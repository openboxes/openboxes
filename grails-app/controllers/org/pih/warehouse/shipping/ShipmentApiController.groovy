package org.pih.warehouse.shipping

import grails.converters.JSON
import org.pih.warehouse.api.BaseDomainApiController
import org.pih.warehouse.core.Location
import org.pih.warehouse.requisition.RequisitionStatus

class ShipmentApiController extends BaseDomainApiController {

    def shipmentService

    def list() {
        Location origin = params.origin ? Location.get(params?.origin) : null
        Location destination = params.destination ? Location.get(params?.destination) : null
        ShipmentStatusCode shipmentStatusCode = params.shipmentStatusCode ? params.shipmentStatusCode as ShipmentStatusCode : null
        List<RequisitionStatus> requisitionStatuses = params.list("requisitionStatus").collect { it as RequisitionStatus }
        List<Shipment> shipments = shipmentService.getShipmentsByLocationAndRequisitionStatuses(origin, destination, shipmentStatusCode, requisitionStatuses)
        render ([data: shipments] as JSON)
    }

    def read() {
        Shipment shipment = shipmentService.getShipment(params.id)
        render ([data:shipment] as JSON)
    }
}
