package org.pih.warehouse.shipping

import grails.converters.JSON
import org.grails.web.json.JSONObject
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
        Shipment shipment = shipmentService.getShipmentInstance(params.id)
        render ([data:shipment] as JSON)
    }

    def update() {
        JSONObject jsonObject = request.JSON
        log.info "Update shipment " + jsonObject.toString(4)

        String action = jsonObject.has("action") ? jsonObject.get("action") : null
        if (!action) {
            forward(controller: "genericApi", action: "update")
            return
        }

        ShipmentItem shipmentItem = ShipmentItem.get(params.id)
        if (action.equalsIgnoreCase("PACK")) {
            String containerId = jsonObject.has("container.id") && !jsonObject.isNull("container.id") ?
                    jsonObject.get("container.id") : null

            Integer quantityToPack = jsonObject.has("quantityToPack") && !jsonObject.isNull("quantityToPack") ?
                    jsonObject.get("quantityToPack") : null

            shipmentItem = shipmentService.packItem(params.id, containerId, quantityToPack)
        }

        render ([data: shipmentItem] as JSON)
    }
}
