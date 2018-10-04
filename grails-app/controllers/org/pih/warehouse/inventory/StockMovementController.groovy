/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/

package org.pih.warehouse.inventory

import grails.converters.JSON
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCommand
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

import java.text.DateFormat
import java.text.SimpleDateFormat


class StockMovementController {

    def dataService
    def stockMovementService
    def requisitionService
    def shipmentService

	def index = {
		render(template: "/stockMovement/create")
	}

    def create = {
        redirect(action: "index")
    }

    def show = {

        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)

        [stockMovement: stockMovement]

    }

    def list = {
        User currentUser = User.get(session?.user?.id)
        Location currentLocation = Location.get(session?.warehouse?.id)

        if (params.direction=="OUTBOUND") {
            params.origin = params.origin?:currentLocation
            params.destination = params.destination?:null
        }
        else if (params.direction=="INBOUND") {
            params.origin = params.origin?:null
            params.destination = params.destination?:currentLocation
        }
        else {
            params.origin = params.origin?:currentLocation
            params.destination = params.destination?:currentLocation
        }

        Requisition requisition = new Requisition(params)
        requisition.discard()
        StockMovement stockMovement = new StockMovement()
        if (params.q) {
            stockMovement.identifier = "%" + params.q + "%"
            stockMovement.name = "%" + params.q + "%"
            stockMovement.description = "%" + params.q + "%"
        }
        stockMovement.requestedBy = requisition.requestedBy
        stockMovement.origin = requisition.origin
        stockMovement.destination = requisition.destination
        stockMovement.statusCode = requisition?.status ? requisition?.status.toString() : null

        def stockMovements = stockMovementService.getStockMovements(stockMovement, params.max?params.max as int:10, params.offset?params.offset as int:0)
        def statistics = requisitionService.getRequisitionStatistics(requisition.origin, requisition.destination, currentUser)

        render(view:"list", params:params, model:[stockMovements: stockMovements, statistics:statistics])

    }

    def delete = {

        try {
            StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
            Requisition requisition = stockMovement?.requisition
            if (requisition) {
                List shipments = stockMovement?.requisition?.shipments
                shipments.toArray().each { Shipment shipment ->
                    if (!shipment?.events?.empty) {
                        shipmentService.rollbackLastEvent(shipment)
                    }
                    shipmentService.deleteShipment(shipment)
                }
                //requisitionService.rollbackRequisition(requisition)
                requisitionService.deleteRequisition(requisition)
            }
            flash.message = "Successfully deleted stock movement with ID ${params.id}"
        } catch (Exception e) {
            log.warn ("Unable to delete stock movement withID ${params.id}: " + e.message)
            flash.message = "Unable to delete stock movement with ID ${params.id}: " + e.message
        }

        redirect(action: "list")
    }


    def shipments = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        def shipments = Shipment.findAllByRequisition(stockMovement.requisition)
        render(template: "shipments", model: [shipments:shipments])
    }

    def receipts = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        def shipments = Shipment.findAllByRequisition(stockMovement.requisition)
        def receipts = shipments*.receipts?.flatten()
        render(template: "receipts", model: [receipts:receipts])
    }


    def uploadDocument = { DocumentCommand command ->
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)

        //Shipment shipment = stockMovement.shipment
        Document document = new Document()
        document.fileContents = command.fileContents.bytes
        document.contentType = command.fileContents.fileItem.contentType
        document.name = command.fileContents.fileItem.name
        document.filename = command.fileContents.fileItem.name
        document.documentType = DocumentType.get(9)
        document.save(flush:true)

        //shipment.addToDocuments(document)
        //shipment.save(flush:true)

        render ([data: "Document was uploaded successfully"] as JSON)
    }

	def exportCsv = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)

        // We need to create at least one row to ensure an empty template
        if (stockMovement?.lineItems?.empty) {
            stockMovement?.lineItems.add(new StockMovementItem())
        }

        def lineItems = stockMovement.lineItems.collect {
            [
                    requisitionItemId: it?.id?:"",
                    productCode: it?.product?.productCode?:"",
                    productName: it?.product?.name?:"",
                    palletName: it?.palletName?:"",
                    boxName: it?.boxName?:"",
                    lotNumber: it?.lotNumber?:"",
                    expirationDate: it?.expirationDate?it?.expirationDate?.format("MM/dd/yyyy"):"",
                    quantity: it?.quantityRequested?:"",
                    recipientId: it?.recipient?.id?:""
            ]
        }
        String csv = dataService.generateCsv(lineItems)
        response.setHeader("Content-disposition", "attachment; filename='StockMovementItems-${params.id}.csv'")
        render(contentType:"text/csv", text: csv.toString(), encoding:"UTF-8")
	}


	def importCsv = { ImportDataCommand command ->

        try {
            StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
            Requisition requisition = stockMovement.requisition

            def importFile = command.importFile
            if (importFile.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty")
            }

            if (importFile.fileItem.contentType != "text/csv") {
                throw new IllegalArgumentException("File must be in CSV format")
            }

            String csv = new String(importFile.bytes)
            def settings = [separatorChar: ',', skipLines: 1]
            csv.toCsvReader(settings).eachLine { tokens ->

                log.info "Tokens " + tokens.class
                StockMovementItem stockMovementItem = StockMovementItem.createFromTokens(tokens)
                stockMovementItem.stockMovement = stockMovement
                stockMovement.lineItems.add(stockMovementItem)
            }
            stockMovementService.updateStockMovement(stockMovement)

        } catch (Exception e) {
            // FIXME The global error handler does not return JSON for multipart uploads
            log.warn("Error occurred while importing CSV: " + e.message, e)
            response.status = 500
            render([errorCode: 500, errorMessage: e?.message?:"An unknown error occurred during import"] as JSON)
            return
        }

        render([data: "Data will be imported successfully"] as JSON)
	}

}
