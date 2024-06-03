/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */
package org.pih.warehouse.picklist

import grails.converters.JSON
import grails.validation.ValidationException
import org.pih.warehouse.api.PickPageItem
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.Location
import org.pih.warehouse.data.DataService
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.springframework.web.multipart.MultipartFile

class PicklistController {

    PicklistService picklistService
    StockMovementService stockMovementService
    DataService dataService
    DocumentService documentService

    def save() {
        def jsonRequest = request.JSON
        def jsonResponse = []
        def picklist = picklistService.save(jsonRequest)
        if (!picklist.hasErrors()) {
            jsonResponse = [success: true, data: picklist.toJson()]
        } else {
            jsonResponse = [success: false, errors: picklist.errors]
        }
        render jsonResponse as JSON
    }

    def print() {
        def requisition = Requisition.get(params.id)
        def picklist = Picklist.findByRequisition(requisition)
        def location = Location.get(session.warehouse.id)
        [requisition: requisition, picklist: picklist, location: location, sorted: params.sorted]
    }

    // Order based picklist print
    def returnPrint() {
        def order = Order.get(params.id)
        def picklist = Picklist.findByOrder(order)
        def location = Location.get(session.warehouse.id)
        [order: order, picklist: picklist, location: location, sorted: params.sorted]
    }

    def renderPdf() {
        def requisition = Requisition.get(params.id)
        def picklist = Picklist.findByRequisition(requisition)
        def location = Location.get(session.warehouse.id)

        renderPdf(
                template: "/picklist/print",
                model: [requisition: requisition, picklist: picklist, location: location, sorted: params.sorted],
                filename: "Picklist - ${requisition.requestNumber}"
        )
    }

    // Order based picklist pdf
    def renderReturnPdf() {
        def order = Order.get(params.id)
        def picklist = Picklist.findByOrder(order)
        def location = Location.get(session.warehouse.id)
        renderPdf(
            template: "/picklist/returnPrint",
            model: [order: order, picklist: picklist, location: location, sorted: params.sorted],
            filename: "Return Picklist - ${order.orderNumber}"
        )
    }

    def renderHtml() {

        def defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
        def locale = session?.user?.locale ?: session.locale ?: defaultLocale
        def requisition = Requisition.get(params.id)
        def picklist = Picklist.findByRequisition(requisition)
        def location = Location.get(session.warehouse.id)

        println location
        render(template: "/picklist/print", model: [requisition: requisition, picklist: picklist, location: location, order: params.order])
    }

    def exportPicklistItems() {
        String format = params.get("format", "csv")

        List<PickPageItem> pickPageItems = stockMovementService.getPickPageItems(params.id, null, null)

        // We need to create at least one row to ensure an empty template
        if (pickPageItems?.empty) {
            pickPageItems.add(new PickPageItem())
        }

        Map<String, String> csvHeadings = [
                id: g.message(code: 'default.id.label', default: 'Id'),
                productCode: g.message(code: 'product.productCode.label', default: 'Code'),
                productName: g.message(code: 'product.name.label', default: 'Name'),
                lotNumber: g.message(code: 'inventoryItem.lotNumber.label', default: 'Serial / Lot Number'),
                expirationDate: g.message(code: 'inventoryItem.expirationDate.label', default: 'Expiration date'),
                binLocation: g.message(code: 'inventoryItem.binLocation.label', default: 'Bin Location'),
                quantity: g.message(code: 'picklist.quantity.label', default: 'Quantity picked'),
        ]

        List<Map> lineItems = pickPageItems.collectMany { pickPageItem ->
            if (pickPageItem.picklistItems.size() > 0) {
                return pickPageItem.picklistItems.collect { picklistItem ->
                    return [
                            id: picklistItem?.requisitionItem?.id,
                            productCode: picklistItem?.requisitionItem?.product?.productCode,
                            productName: picklistItem?.requisitionItem?.product?.displayNameWithLocaleCode,
                            lotNumber: picklistItem?.inventoryItem?.lotNumber,
                            expirationDate: picklistItem?.inventoryItem?.expirationDate,
                            binLocation: picklistItem?.binLocation?.name,
                            quantity: picklistItem?.quantity,
                    ]
                }
            }
            return [
                    [
                            id: pickPageItem?.requisitionItem?.id,
                            productCode: pickPageItem?.requisitionItem?.product?.productCode,
                            productName: pickPageItem?.requisitionItem?.product?.displayNameWithLocaleCode,
                            quantity: 0
                    ]
            ]
        }.collect {
            [
                    "${csvHeadings.id}": it?.id ?: "",
                    "${csvHeadings.productCode}": it?.productCode ?: "",
                    "${csvHeadings.productName}": it?.productName ?: "",
                    "${csvHeadings.lotNumber}": it?.lotNumber ?: "",
                    "${csvHeadings.expirationDate}": it?.expirationDate ? it.expirationDate?.format(Constants.EXPIRATION_DATE_FORMAT) : "",
                    "${csvHeadings.binLocation}": it?.binLocation ?: "",
                    "${csvHeadings.quantity}": it?.quantity == null ? "" : it?.quantity,
            ]
        }

        String fileName = "PickListItems\$-${params.id}"

        switch (format) {
            case "csv":
                String csv = dataService.generateCsv(lineItems)
                response.setHeader("Content-disposition", "attachment; filename=\"${fileName}.csv\"")
                render(contentType: "text/csv", text: csv, encoding: "UTF-8")
                break
            case "xls":
                response.contentType = "application/vnd.ms-excel"
                response.setHeader("Content-disposition", "attachment; filename=\"${fileName}.xls\"")
                documentService.generateExcel(response.outputStream, lineItems)
                response.outputStream.flush()
                break
            default:
                throw new IllegalFormatException("Unable to determine the proper rendering format for request for format ${format}")
        }
    }


    def exportPicklistTemplate() {
        String format = params.get("format", "csv")

        List<PickPageItem> pickPageItems = stockMovementService.getPickPageItems(params.id, null, null)

        // We need to create at least one row to ensure an empty template
        if (pickPageItems?.empty) {
            pickPageItems.add(new PickPageItem())
        }

        Map<String, String> csvHeadings = [
                id: g.message(code: 'default.id.label', default: 'Id'),
                productCode: g.message(code: 'product.productCode.label', default: 'Code'),
                productName: g.message(code: 'product.name.label', default: 'Name'),
                lotNumber: g.message(code: 'inventoryItem.lotNumber.label', default: 'Serial / Lot Number'),
                expirationDate: g.message(code: 'inventoryItem.expirationDate.label', default: 'Expiration date'),
                binLocation: g.message(code: 'inventoryItem.binLocation.label', default: 'Bin Location'),
                quantity: g.message(code: 'picklist.quantityToPick.label', default: 'Quantity to pick'),
        ]

        List lineItems = pickPageItems.collect {
            [
                    "${csvHeadings.id}": it?.requisitionItem?.id ?: "",
                    "${csvHeadings.productCode}": it?.requisitionItem?.product?.productCode ?: "",
                    "${csvHeadings.productName}": it?.requisitionItem?.product?.displayNameWithLocaleCode ?: "",
                    "${csvHeadings.lotNumber}": "",
                    "${csvHeadings.expirationDate}": "",
                    "${csvHeadings.binLocation}": "",
                    "${csvHeadings.quantity}": it?.requisitionItem?.quantity ?: "",
            ]
        }

        String fileName = "PickListItems\$-${params.id}-template"

        switch (format) {
            case "csv":
                String csv = dataService.generateCsv(lineItems)
                response.setHeader("Content-disposition", "attachment; filename=\"${fileName}.csv\"")
                render(contentType: "text/csv", text: csv, encoding: "UTF-8")
                break
            case "xls":
                response.contentType = "application/vnd.ms-excel"
                response.setHeader("Content-disposition", "attachment; filename=\"${fileName}.xls\"")
                documentService.generateExcel(response.outputStream, lineItems)
                response.outputStream.flush()
                break
            default:
                throw new IllegalFormatException("Unable to determine the proper rendering format for request for format ${format}")
        }
    }

    def importPickListItems(PicklistImportDataCommand command) {

        // Bind stock movement based on provided id
        command.stockMovement = stockMovementService.getStockMovement(params.id)

        // Bind basic properties not provided by request
        command.importType = "PicklistItems"
        command.location = command.location ?: Location.get(session.warehouse.id)

        // TODO Test that this actually captures all of the validation errors on the input file
        // Validate provided properties of the data import, including the import file
        if(!command.validate()) {
            throw new ValidationException("Failed to import due to errors", command.errors)
        }

        // TODO There should probably be one driver method importPicklist() and all of the following
        // should go into that method. The only things we need to do in the controller is validate
        // the input and handle the errors.

        // Bind the imported line items to the command class
        command.picklistItems = stockMovementService.parsePicklistImport(command)

        // Bind the parent requisition items to the command class
        command.pickPageItems = stockMovementService.getPickPageItems(params.id, null, null)

        stockMovementService.validatePicklistImport(command)

        // TODO Errors should be added to a command class within the validate method, but
        //  you cannot add another command classes errors to a parent command class. Instead
        //  we probably need a custom validator for the picklistItems association on
        //  PicklistDataImportCommand and just call validate on the parent.
        //
        // TODO If nothing else, the code below should be cleaned up and added to a utility
        //  ErrorsUtil.copyAllErrors(source, destination)
        List<String> errors = []
        command.picklistItems.eachWithIndex { importPickCommand, index ->
            if (importPickCommand.hasErrors()) {
                List<String> localizedErrors = importPickCommand.errors.allErrors.collect { g.message(error: it) }
                if (!localizedErrors.isEmpty()) {
                    errors.addAll(localizedErrors.collect { "Row ${index + 1}: ${it}" })
                }
            }
        }

        // FIXME Just pass the command object
        stockMovementService.importPicklistItems(command.stockMovement, command.pickPageItems, command.picklistItems)

        if (!command.hasErrors()) {
            render([message: "Data imported with errors", errors: errors] as JSON)
        }

        render([message: "Data imported successfully"] as JSON)
    }
}


class PicklistImportDataCommand extends ImportDataCommand {

    Location location
    StockMovement stockMovement
    List<PickPageItem> pickPageItems
    List<ImportPickCommand> picklistItems

    PicklistImportDataCommand() {
        importType = "picklistItems"
    }

    static constraints = {
        // FIXME need to figure out if we can / want to bind stock movement and therefore make this nullable:false
        stockMovement nullable: true
        pickPageItems nullable: true
        picklistItems nullable: true
    }

}
