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
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCommand
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment


class StockMovementController {

    def dataService
    def stockMovementService

	def index = {
		render(template: "/stockMovement/create")
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
        def lineItems = stockMovement.lineItems.collect {
            [
                    requisitionItemId: it.id,
                    productCode: it.product.productCode,
                    productName: it.product.name,
                    palletName: it.palletName?:"",
                    boxName: it.boxName?:"",
                    quantity: it.quantityRequested,
                    recipientId: it?.recipient?.id?:""
            ]
        }
        String csv = dataService.generateCsv(lineItems)
        response.setHeader("Content-disposition", "attachment; filename='StockMovementItems-${params.id}.csv'")
        render(contentType:"text/csv", text: csv.toString(), encoding:"UTF-8")
	}


	def importCsv = { ImportDataCommand command ->

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
        def settings = [separatorChar:',', skipLines: 1]
        csv.toCsvReader(settings).eachLine { tokens ->
            log.info "tokens " + tokens
            String requisitionItemId = tokens[0]
            String productCode = tokens[1]
            String productName = tokens[2]
            String palletName = tokens[3]
            String boxName = tokens[4]
            Integer quantityRequested = tokens[5].toInteger()
            String recipientId = tokens[6]

            Product product = Product.findByProductCode(productCode)
            RequisitionItem requisitionItem
            if (requisitionItemId) {
                requisitionItem = RequisitionItem.get(requisitionItemId)
            }

            if (!requisitionItem) {
                requisitionItem = new RequisitionItem()
                requisition.addToRequisitionItems(requisitionItem)
            }

            if (quantityRequested == 0) {
                requisition.removeFromRequisitionItems(requisitionItem)
            }
            else {
                requisitionItem.product = product
                requisitionItem.quantity = quantityRequested
            }
            requisition.save()
        }


        render([data: "Data will be imported successfully"] as JSON)
	}

}