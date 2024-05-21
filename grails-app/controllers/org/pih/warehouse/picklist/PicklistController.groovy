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
import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.PickPageItem
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.Location
import org.pih.warehouse.data.DataService
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.springframework.web.multipart.MultipartFile

@Transactional
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

        List<PickPageItem> pickPageItems = stockMovementService.getPickPageItems(params.id, null, null, false)
        List<PicklistItem> picklistItems = pickPageItems.picklistItems?.flatten()

        // We need to create at least one row to ensure an empty template
        if (picklistItems?.empty) {
            picklistItems.add(new PicklistItem())
        }

        def lineItems = picklistItems.collect {
            [
                "${g.message(code: 'default.id.label')}": it?.requisitionItem?.id ?: "",
                "${g.message(code: 'product.productCode.label')}": it?.requisitionItem?.product?.productCode ?: "",
                "${g.message(code: 'product.name.label')}": it?.requisitionItem?.product?.name ?: "",
                "${g.message(code: 'inventoryItem.lotNumber.label')}": it?.inventoryItem?.lotNumber ?: "",
                "${g.message(code: 'inventoryItem.expirationDate.label')}": it?.inventoryItem?.expirationDate ? it.inventoryItem.expirationDate.format(Constants.EXPIRATION_DATE_FORMAT) : "",
                "${g.message(code: 'inventoryItem.binLocation.label')}": it?.binLocation?.name ?: "",
                "${g.message(code: 'default.quantity.label')}": it?.quantity ?: "",
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

        List<PickPageItem> pickPageItems = stockMovementService.getPickPageItems(params.id, null, null, false)

        // We need to create at least one row to ensure an empty template
        if (pickPageItems?.empty) {
            pickPageItems.add(new PickPageItem())
        }

        def lineItems = pickPageItems.collect {
            [
                    "${g.message(code: 'default.id.label')}": it?.requisitionItem?.id ?: "",
                    "${g.message(code: 'product.productCode.label')}": it?.requisitionItem?.product?.productCode ?: "",
                    "${g.message(code: 'product.name.label')}": it?.requisitionItem?.product?.name ?: "",
                    "${g.message(code: 'inventoryItem.lotNumber.label')}": "",
                    "${g.message(code: 'inventoryItem.expirationDate.label')}": "",
                    "${g.message(code: 'inventoryItem.binLocation.label')}": "",
                    "${g.message(code: 'default.quantity.label')}": it?.requisitionItem?.quantity ?: "",
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

    def importPickListItems(ImportDataCommand command) {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        List<PickPageItem> pickPageItems = stockMovementService.getPickPageItems(params.id, null, null, false)

        MultipartFile importFile = command.importFile
        if (importFile.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty")
        }

        String csv = new String(importFile.bytes)
        List<Map> importedLines = stockMovementService.parsePickCsvTemplateImport(csv)

        stockMovementService.validatePicklistListImport(importedLines, pickPageItems)

        List errors = importedLines*.errors.withIndex().collect { errors, index ->
            errors.collect { "Row ${1}: ${it}" }
        }.flatten()

        stockMovementService.importPicklistTemplate(importedLines, stockMovement, pickPageItems)

        if (!errors.isEmpty()) {
            render([message: "Data imported with errors", errors: errors] as JSON)
        }

        render([message: "Data imported successfully"] as JSON)
    }
}
