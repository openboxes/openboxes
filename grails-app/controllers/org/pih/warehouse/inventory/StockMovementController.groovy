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

import java.text.DateFormat
import java.text.SimpleDateFormat


class StockMovementController {

    def dataService
    def stockMovementService
    def requisitionService

    static DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy")

	def index = {
		render(template: "/stockMovement/create")
	}

    def show = {

        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)

        [stockMovement: stockMovement]

    }

    def list = {
        User currentUser = User.get(session?.user?.id)
        Location currentLocation = Location.get(session?.warehouse?.id)
        Requisition requisition = new Requisition(params)
        requisition.discard()
        StockMovement stockMovement = new StockMovement()
        if (params.q) {
            stockMovement.identifier = "%" + params.q + "%"
            stockMovement.name = "%" + params.q + "%"
            stockMovement.description = "%" + params.q + "%"
        }
        stockMovement.origin = requisition.origin
        stockMovement.destination = requisition.destination
        stockMovement.statusCode = requisition?.status ? requisition?.status.toString() : null

        def stockMovements = stockMovementService.getStockMovements(stockMovement, params.max?:10, params.offset?:0)
        def statistics = requisitionService.getRequisitionStatistics(currentLocation, null, currentUser)

        render(view:"list", model:[stockMovements: stockMovements, statistics:statistics])

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
            String requisitionItemId = tokens[0]?:null
            String productCode = tokens[1]?:null
            String productName = tokens[2]?:null
            String palletName = tokens[3]?:null
            String boxName = tokens[4]?:null
            String lotNumber = tokens[5]?:null
            Date expirationDate = tokens[6] ? DEFAULT_DATE_FORMAT.parse(tokens[6]):null
            Integer quantityRequested = tokens[7].toInteger()?:null
            String recipientId = tokens[8]

            Person recipient = recipientId ? Person.get(recipientId) : null
            if (!recipient && recipientId) {
                String [] names = recipientId.split(" ")
                if (names.length != 2) {
                    throw new IllegalArgumentException("Please enter recipient's first and last name only")
                }

                String firstName = names[0], lastName = names[1]
                recipient = Person.findByFirstNameAndLastName(firstName, lastName)
                if (!recipient) {
                    throw new IllegalArgumentException("Unable to locate person with first name ${firstName} and last name ${lastName}")
                }
            }
            log.info "RECIPIENT: " + recipient

            StockMovementItem stockMovementItem = new StockMovementItem()
            stockMovementItem.id = requisitionItemId

            if (quantityRequested == 0) {
                stockMovementItem.delete = true
            }

            // Required properties
            Product product = Product.findByProductCode(productCode)
            if (product.name != productName) {
                throw new IllegalArgumentException("Product '${product.productCode} ${product?.name}' does not match product in CSV '${productCode} ${productName}'")
            }
            stockMovementItem.product = product
            stockMovementItem.quantityRequested = quantityRequested
            stockMovementItem.palletName = palletName
            stockMovementItem.boxName = boxName
            stockMovementItem.lotNumber = lotNumber
            stockMovementItem.expirationDate = expirationDate
            stockMovementItem.recipient = recipient

            stockMovementItem.stockMovement = stockMovement

            stockMovement.lineItems.add(stockMovementItem)

            stockMovementService.updateStockMovement(stockMovement)
        }


        render([data: "Data will be imported successfully"] as JSON)
	}

}