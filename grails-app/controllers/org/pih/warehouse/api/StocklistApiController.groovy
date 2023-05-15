/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import org.apache.commons.lang.StringEscapeUtils
import org.grails.web.json.JSONObject
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.core.Location
import org.pih.warehouse.requisition.RequisitionItemSortByCode
import grails.plugins.csv.CSVWriter

/**
 * Should not extend BaseDomainApiController since stocklist is not a valid domain.
 */
class StocklistApiController {

    def requisitionService
    def stocklistService
    def userService

    def list() {
        Requisition requisition = new Requisition(params)
        requisition.isTemplate = true
        requisition.isPublished = params.isPublished ? params.boolean("isPublished") : true
        requisition.origin = null // set null to filter with multiple origins
        requisition.destination = null // set null to filter with multiple destinations

        def origins = params.origin ? Location.findAllByIdInList(params.list("origin")) : []
        def destinations = params.destination ? Location.findAllByIdInList(params.list("destination")) : []

        def requisitions = requisitionService.getRequisitions(requisition, params, origins, destinations)

        if (params.format == 'csv') {
            def hasRoleFinance = userService.hasRoleFinance(session?.user)

            def sw = stocklistService.exportStocklistItems(requisitions, hasRoleFinance);

            response.contentType = "text/csv"
            response.setHeader("Content-disposition", "attachment; filename=\"Stocklists-items-${new Date().format("yyyyMMdd-hhmmss")}.csv\"")
            render(contentType: "text/csv", text: sw.toString(), encoding: "UTF-8")
            return
        }


        render([
            data: requisitions.collect { Requisition req -> req.toStocklistJson() },
            totalCount: requisitions.totalCount,
        ] as JSON)
    }

    def read() {
        Stocklist stocklist = stocklistService.getStocklist(params.id)

        if (!stocklist) {
            throw new ObjectNotFoundException(params.id, Stocklist.class.toString())
        }

        render([data: stocklist] as JSON)
    }

    def create(Stocklist stocklist) {

        JSONObject jsonObject = request.JSON
        log.debug "create " + jsonObject.toString(4)

        stocklist = stocklistService.createStocklist(stocklist)

        response.status = 201
        render([data: stocklist] as JSON)
    }

    def update() {
        JSONObject jsonObject = request.JSON
        log.debug "update: " + jsonObject.toString(4)

        Stocklist stocklist = stocklistService.getStocklist(params.id)
        if (!stocklist) {
            stocklist = new Stocklist()
        }

        bindData(stocklist, jsonObject)
        stocklist = stocklistService.updateStocklist(stocklist)

        render([data: stocklist] as JSON)
    }

    def delete() {
        try {
            stocklistService.deleteStocklist(params.id)
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            def message = "Requisition $params.id could not be deleted"
            response.status = 400
            render([errorMessages: [message]] as JSON)
            return
        }
        render status: 204
    }

    def sendMail() {
        JSONObject jsonObject = request.JSON
        log.debug "send mail: " + jsonObject.toString(4)
        def emailBody = jsonObject.text + "\n\n" + "Sent by " + session.user.name
        stocklistService.sendMail(params.id, jsonObject.subject, emailBody, jsonObject.recipients, jsonObject.includePdf, jsonObject.includeXls)

        render status: 200
    }

    def clear() {
        Requisition requisition = Requisition.get(params.id)
        if (!requisition) {
            return 404
        }
        requisitionService.clearRequisition(requisition)

        render status: 200
    }

    def clone() {
        def requisition = Requisition.get(params.id)
        if (!requisition) {
            return 404
        }
        requisitionService.cloneRequisition(requisition)

        render status: 200
    }

    def publish() {
        def requisition = Requisition.get(params.id)
        if (!requisition) {
            return 404
        }
        stocklistService.publishStockList(requisition, true);

        render status: 200
    }

    def unpublish() {
        def requisition = Requisition.get(params.id)
        if (!requisition) {
            return 404
        }
        stocklistService.publishStockList(requisition, false);

        render status: 200
    }

    def export() {
        def requisition = Requisition.get(params.id)
        if (!requisition) {
            return 404
        }
        def hasRoleFinance = userService.hasRoleFinance(session?.user)
        def sw = new StringWriter()

        def csv = new CSVWriter(sw, {
            "Product Code" { it.productCode }
            "Product Name" { it.productName }
            "Quantity" { it.quantity }
            "UOM" { it.unitOfMeasure }
            hasRoleFinance ? "Unit cost" { it.unitCost } : null
            hasRoleFinance ? "Total cost" { it.totalCost } : null
        })

        if (requisition.requisitionItems) {
            RequisitionItemSortByCode sortByCode = requisition.sortByCode ?: RequisitionItemSortByCode.SORT_INDEX

            requisition."${sortByCode.methodName}".each { requisitionItem ->
                csv << [
                        productCode  : requisitionItem.product.productCode,
                        productName  : StringEscapeUtils.escapeCsv(requisitionItem.product.name),
                        quantity     : requisitionItem.quantity,
                        unitOfMeasure: "EA/1",
                        unitCost     : hasRoleFinance ? formatNumber(number: requisitionItem.product.pricePerUnit ?: 0, format: '###,###,##0.00##') : null,
                        totalCost    : hasRoleFinance ? formatNumber(number: requisitionItem.totalCost ?: 0, format: '###,###,##0.00##') : null
                ]
            }
        } else {
            csv << [
                    productCode     : "",
                    productName     : "",
                    quantity        : "",
                    unitOfMeasure   : "",
                    unitCost        : "",
                    totalCost       : ""
            ]
        }

        response.contentType = "text/csv"
        response.setHeader("Content-disposition", "attachment; filename=\"Stock List - ${requisition?.destination?.name} - ${new Date().format("yyyyMMdd-hhmmss")}.csv\"")
        render(contentType: "text/csv", text: csv.writer.toString())
        return

        render status: 200
    }

}
