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
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.exporter.DataExporter
import org.pih.warehouse.exporter.PicklistItemCsvExporter
import org.pih.warehouse.exporter.PicklistItemExcelExporter
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition

@Transactional
class PicklistController {

    PicklistService picklistService
    StockMovementService stockMovementService

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

        List<PickPageItem> pickPageItems = stockMovementService.getPickPageItems(params.id, null, null )
        List<PicklistItem> picklistItems = pickPageItems.picklistItems?.flatten()

        List lineItems = picklistItems.collect {
            [
                    id: it?.requisitionItem?.id,
                    code: it?.requisitionItem?.product?.productCode,
                    name: it?.requisitionItem?.product?.name,
                    lot: it?.inventoryItem?.lotNumber,
                    expiration: it.inventoryItem.expirationDate?.format(Constants.EXPIRATION_DATE_FORMAT),
                    binLocation: it?.binLocation?.name,
                    quantity: it?.quantity,
            ]
        }

        String fileName = "PickListItems\$-${params.id}"

        switch (format) {
            case "csv":
                OutputStream outputStream = new ByteArrayOutputStream()
                DataExporter pickListItemCsvExporter = new PicklistItemCsvExporter(lineItems)
                pickListItemCsvExporter.exportData(outputStream)
                response.setHeader("Content-disposition", "attachment; filename=\"${fileName}.csv\"")
                render(contentType: "text/csv", text: outputStream, encoding: "UTF-8")
                break
            case "xls":
                response.contentType = "application/vnd.ms-excel"
                response.setHeader("Content-disposition", "attachment; filename=\"${fileName}.xls\"")
                DataExporter picklistItemExcelExporter = new PicklistItemExcelExporter(lineItems)
                picklistItemExcelExporter.exportData(response.outputStream)
                response.outputStream.flush()
                break
            default:
                throw new IllegalFormatException("Unable to determine the proper rendering format for request for format ${format}")
        }
    }

    def exportPicklistTemplate() {
        String format = params.get("format", "csv")

        List<PickPageItem> pickPageItems = stockMovementService.getPickPageItems(params.id, null, null)
        List<PicklistItem> picklistItems = pickPageItems.picklistItems?.flatten()

        List lineItems = picklistItems.collect {
            [
                    id: it?.requisitionItem?.id,
                    code: it?.requisitionItem?.product?.productCode,
                    name: it?.requisitionItem?.product?.name,
                    lot: null,
                    expiration: null,
                    binLocation: null,
                    quantity: it?.requisitionItem?.quantity,
            ]
        }

        String fileName = "PickListItems\$-${params.id}-template"
        switch (format) {
            case "csv":
                OutputStream outputStream = new ByteArrayOutputStream()
                DataExporter pickListItemCsvExporter = new PicklistItemCsvExporter(lineItems)
                pickListItemCsvExporter.exportData(outputStream)
                response.setHeader("Content-disposition", "attachment; filename=\"${fileName}.csv\"")
                render(contentType: "text/csv", text: outputStream, encoding: "UTF-8")
                break
            case "xls":
                response.contentType = "application/vnd.ms-excel"
                response.setHeader("Content-disposition", "attachment; filename=\"${fileName}.xls\"")
                DataExporter pickListItemExcelExporter = new PicklistItemExcelExporter(lineItems)
                pickListItemExcelExporter.exportData(response.outputStream)
                response.outputStream.flush()
                break
            default:
                throw new IllegalFormatException("Unable to determine the proper rendering format for request for format ${format}")
        }
    }
}
