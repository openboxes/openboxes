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
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.api.BaseDomainApiController
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

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
        try {
            JSONObject jsonObject = request.JSON
            Shipment shipment = shipmentService.getShipment(jsonObject.getString("shipment.id"))
            if(!jsonObject.containsKey("name") && !jsonObject.containsKey("containerNumber")){
                render shipmentService.initializeContainerFromShipment(shipment) as JSON
            }
            else{
                Container container = new Container()
                container.name = jsonObject.name
                container.containerNumber = jsonObject.containerNumber
                container.containerType = jsonObject.containerType.id ?
                        ContainerType.get(jsonObject.containerType.id) :
                        ContainerType.findById(Constants.PALLET_CONTAINER_TYPE_ID)
                shipment.addToContainers(container)
                shipment.save(flush: true)
                render container as JSON
            }
        } catch(Exception e) {
            render([errorCode: 500, cause: e?.class, errorMessage: e?.message] as JSON)
        }
    }
}
