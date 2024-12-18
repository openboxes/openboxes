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
import org.grails.web.json.JSONObject
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.api.BaseDomainApiController
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Document
import org.pih.warehouse.inventory.LotStatusCode

class ContainerApiController extends BaseDomainApiController {

    def zebraService
    def documentService
    def shipmentService

    def details = {
        Container container = Container.get(params.id)
        def data = container.toJson()
        data.defaultBarcodeLabelUrl = documentService.getContainerBarcodeLabel(container)
        render([data: data] as JSON)
    }

    def renderLabel = {
        Container container = Container.get(params.id)
        Document document = Document.get(params.documentId)
        if (!document) {
            throw new ObjectNotFoundException(params.documentId, Document.class.simpleName)
        }
        response.contentType = "image/png"
        response.outputStream << zebraService.renderDocument(document, [container:container])
    }

    def printLabel = {
        try {
            Container container = Container.get(params.id)
            Document document = Document.get(params.documentId)
            if (!document) {
                throw new ObjectNotFoundException(params.documentId, Document.class.simpleName)
            }
            zebraService.printDocument(document, [container:container])
            render([data: "Container barcode label has been printed"] as JSON)
            return
        } catch (Exception e) {
            render([errorCode: 500, cause: e?.class, errorMessage: e?.message] as JSON)
        }
    }

    def create = {
        JSONObject jsonObject = request.JSON
        try {

            if (jsonObject.isNull("containerType.id")) {
                throw new IllegalArgumentException("Container type is a required property")
            }

            if (jsonObject.isNull("shipment.id")) {
                throw new IllegalArgumentException("Shipment is a required property")
            }

            Shipment shipment = shipmentService.getShipment(jsonObject.getString("shipment.id"))
            Container container = !jsonObject.isNull("containerNumber") ? Container.findByShipmentAndContainerNumber(shipment, jsonObject.containerNumber) : null
            if (!container) {
                container = new Container()
            }

            String containerNumber = shipmentService.generateContainerNumber(shipment)
            container.name = !jsonObject.isNull("name") ? jsonObject.name : containerNumber
            container.containerNumber = !jsonObject.isNull("containerNumber") ? jsonObject.containerNumber : containerNumber
            container.containerType = jsonObject["containerType.id"] ?
                    ContainerType.get(jsonObject["containerType.id"]) :
                    ContainerType.findById(Constants.PALLET_CONTAINER_TYPE_ID)

            container.save(flush:true)
            shipment.addToContainers(container)
            shipment.save(flush:true)
            render ([data: container] as JSON)
        } catch(Exception e) {
            log.error ("Error saving container " + e.message, e)
            render([errorCode: 500, cause: e?.class, errorMessage: e?.message] as JSON)
        }
    }

    def updateStatus = {
        Container container = Container.get(params.id)
        if (!container) {
            throw new IllegalArgumentException("Can't find container with given id: ${params.id}")
        }

        ContainerStatus updatedContainerStatus = ContainerStatus.valueOf(params.status)
        if (!updatedContainerStatus || !(updatedContainerStatus in ContainerStatus.list())) {
            throw new IllegalArgumentException("Must provide valid container status (${params.status})")
        }

        if (updatedContainerStatus == ContainerStatus.MISSING) {
            container.shipmentItems.toArray().each { ShipmentItem shipmentItem ->
                shipmentItem.quantity = 0
                shipmentItem.inventoryItem.lotStatus = LotStatusCode.MISSING
            }
        }

        if (container.containerStatus == ContainerStatus.MISSING && container.containerStatus != updatedContainerStatus) {
            container.shipmentItems.toArray().each { ShipmentItem shipmentItem ->
                shipmentItem.quantity = shipmentItem.quantityPicked
                shipmentItem.inventoryItem.lotStatus = null
            }
        }

        shipmentService.updateContainerStatus(container, updatedContainerStatus)
        render status: 200
    }
}
