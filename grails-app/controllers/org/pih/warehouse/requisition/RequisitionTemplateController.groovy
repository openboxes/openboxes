/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.requisition

import org.apache.commons.lang.StringEscapeUtils
import org.grails.plugins.csv.CSVWriter
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class RequisitionTemplateController {

    def requisitionService
    def inventoryService
    def productService
    def userService

    static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        def requisitionCriteria = new Requisition()
        requisitionCriteria.name = "%" + params.q + "%"
        requisitionCriteria.isTemplate = true
        if (!params.includeUnpublished) {
            requisitionCriteria.isPublished = true
        }

        List<Location> origins = Location.createCriteria().list() {
            isNull("parentLocation")
            if (params.origin) {
                'in'("id", params.origin)
            }
        }

        List<Location> destinations = Location.createCriteria().list() {
            isNull("parentLocation")
            if (params.destination) {
                'in'("id", params.destination)
            }
        }

        if (params.format) {
            params.remove("max")
            params.remove("offset")
        }

        def requisitions = requisitionService.getRequisitions(requisitionCriteria, params, origins, destinations)

        if (params.format && requisitions) {
            def requisitionItems = []
            def hasRoleFinance = userService.hasRoleFinance(session?.user)

            requisitionService.getRequisitionTemplatesItems(requisitions).groupBy {
                it.product
            }.collect { product, value ->
                def totalCost = 0
                if (hasRoleFinance) {
                    requisitions.each { requisition ->
                        totalCost = product?.pricePerUnit ? totalCost + (requisition.getQuantityByProduct(product) * product?.pricePerUnit) : ""
                    }
                }
                requisitionItems << [
                        product     : product,
                        productCode : product.productCode ?: "",
                        productName : product.name ?: "",
                        category    : product?.category?.name ?: "",
                        pricePerUnit: hasRoleFinance ? product?.pricePerUnit ?: "" : "",
                        totalCost   : hasRoleFinance ? totalCost : "",
                ]
            }

            def sw = new StringWriter()

            try {
                if (requisitionItems) {
                    sw.append(g.message(code: 'product.code.label')).append(",")
                    sw.append(g.message(code: 'product.description.label')).append(",")
                    sw.append(g.message(code: 'product.primaryCategory.label')).append(",")

                    requisitions.each { requisition ->
                        String stocklistName = StringEscapeUtils.escapeCsv(requisition?.name)
                        sw.append(stocklistName).append(" [").append(requisition?.isPublished ? "Published" : "Draft").append("]").append(",")
                    }

                    if (hasRoleFinance) {
                        sw.append(g.message(code: 'product.unitCost.label')).append(",")
                        sw.append(g.message(code: 'product.totalValue.label'))
                    }

                    sw.append("\n")
                    requisitionItems.each { requisitionItem ->

                        sw.append(StringEscapeUtils.escapeCsv(requisitionItem?.productCode)).append(",")
                        sw.append(StringEscapeUtils.escapeCsv(requisitionItem?.productName)).append(",")
                        sw.append(StringEscapeUtils.escapeCsv(requisitionItem?.category)).append(",")


                        requisitions?.each { requisition ->
                            sw.append((requisition?.getQuantityByProduct(requisitionItem.product) ?: "").toString()).append(",")
                        }

                        if (hasRoleFinance) {
                            sw.append(requisitionItem?.pricePerUnit.toString()).append(",")
                            sw.append(requisitionItem?.totalCost.toString()).append(",")
                        }

                        sw.append("\n")
                    }
                }

            } catch (RuntimeException e) {
                log.error(e.message)
                sw.append(e.message)
            }

            response.setHeader("Content-disposition", "attachment; filename=\"Stocklists-items-${new Date().format("yyyyMMdd-hhmmss")}.csv\"")
            render(contentType: "text/csv", text: sw.toString(), encoding: "UTF-8")
        }

        render(view: "list", model: [requisitions: requisitions])
    }

    def create = {
        println params
        def requisition = new Requisition(status: RequisitionStatus.CREATED)
        requisition.type = params.type as RequisitionType
        requisition.isTemplate = true
        requisition.origin = Location.get(session?.warehouse?.id)
        [requisition: requisition]
    }

    def edit = {
        def requisition = Requisition.get(params.id)
        if (!requisition) {
            flash.message = "Could not find requisition with ID ${params.id}"
            redirect(action: "list")
        } else {
            [requisition: requisition]
        }
    }

    def editHeader = {
        def requisition = Requisition.get(params.id)
        if (!requisition) {
            flash.message = "Could not find requisition with ID ${params.id}"
            redirect(action: "list")
        } else {
            [requisition: requisition]
        }
    }

    def sendMail = {
        def requisition = Requisition.get(params.id)
        if (!requisition) {
            flash.message = "Could not find requisition with ID ${params.id}"
            redirect(action: "list")
        } else if (!requisition.requestedBy) {
            flash.error = "${warehouse.message(code: 'stockList.noManagerAssociated.label')}"
            redirect(controller: "requisitionTemplate", action: "show", params: [id: params.id])
        } else {
            [requisition: requisition]
        }
    }

    def save = {
        def requisition = new Requisition(params)

        if (!requisition.hasErrors() && requisition.save()) {
            flash.message = "Requisition template has been created"
        } else {
            render(view: "create", model: [requisition: requisition])
            return
        }
        redirect(action: "edit", id: requisition.id)
    }

    def publish = {
        def requisition = Requisition.get(params.id)
        if (requisition) {
            requisition.isPublished = true
            if (!requisition.hasErrors() && requisition.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
                redirect(action: "show", id: requisition.id)
            } else {
                render(view: "edit", model: [requisition: requisition])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            redirect(action: "list")
        }
    }

    def unpublish = {
        def requisition = Requisition.get(params.id)
        if (requisition) {
            requisition.isPublished = false
            if (!requisition.hasErrors() && requisition.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
                redirect(action: "show", id: requisition.id)
            } else {
                render(view: "edit", model: [requisition: requisition])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            redirect(action: "list")
        }
    }


    def update = {
        String viewName = params?.viewName ?: "edit"

        def requisition = Requisition.get(params.id)
        if (requisition) {
            if (params.version) {
                def version = params.version.toLong()
                if (requisition.version > version) {
                    requisition.errors.rejectValue("version", "default.optimistic.locking.failure", [
                            warehouse.message(code: 'requisition.label', default: 'Requisition')] as Object[],
                            "Another user has updated this requisition while you were editing")
                    render(view: viewName, model: [requisition: requisition])
                    return
                }
            }
            requisition.properties = params
            requisition.lastUpdated = new Date()
            requisition.updatedBy = session.user

            if (!requisition.hasErrors() && requisition.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
                redirect(action: "show", id: requisition.id)
            } else {
                render(view: viewName, model: [requisition: requisition])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            redirect(action: "list")
        }
    }


    def show = {
        def requisition = Requisition.get(params.id)

        if (!requisition) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        } else {
            return [requisition: requisition]
        }
    }

    def delete = {
        def requisition = Requisition.get(params.id)
        if (requisition) {
            try {
                requisitionService.deleteRequisition(requisition)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
        }
        redirect(action: "list", id: params.id)
    }

    def clear = {
        def requisition = Requisition.get(params.id)
        if (requisition) {
            try {
                requisitionService.clearRequisition(requisition)
                flash.message = "${warehouse.message(code: 'default.cleared.message', default: '{0} cleared', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
        }

        redirect(action: "show", id: params.id)
    }

    def clone = {
        def clone
        def requisition = Requisition.get(params.id)
        if (requisition) {
            try {
                clone = requisitionService.cloneRequisition(requisition)

                println clone.id
                println clone.name

                flash.message = "${warehouse.message(code: 'default.cloned.message', default: '{0} cloned', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
        }

        redirect(action: "list")
    }

    def changeSortOrderAlpha = {
        def requisition = Requisition.get(params.id)
        if (requisition) {
            def sortedItems = requisition.requisitionItems.sort { it.product.name }
            sortedItems.eachWithIndex { requisitionItem, orderIndex ->
                requisitionItem.orderIndex = orderIndex
            }
            requisition.save(flush: true)
        }
        redirect(action: "edit", id: requisition.id)
    }

    def changeSortOrderChrono = {
        def requisition = Requisition.get(params.id)
        if (requisition) {
            def sortedItems = requisition.requisitionItems.sort { it.id }
            sortedItems.eachWithIndex { requisitionItem, orderIndex ->
                requisitionItem.orderIndex = orderIndex
            }
            requisition.save(flush: true)
        }
        redirect(action: "edit", id: requisition.id)
    }


    def addToRequisitionItems = {
        def requisition = Requisition.get(params.id)
        if (requisition) {
            def productCodes = params.multipleProductCodes.split(",")
            def processedProductCodes = []
            def ignoredProductCodes = []
            def count = requisition.requisitionItems.size() ?: 0
            productCodes.eachWithIndex { productCode, index ->
                def product = Product.findByProductCode(productCode.trim())
                if (product) {
                    def requisitionItem = requisition.requisitionItems.find {
                        it.product == product
                    }
                    if (!requisitionItem) {
                        requisitionItem = new RequisitionItem()
                        requisitionItem.product = product
                        requisitionItem.quantity = 1
                        requisitionItem.substitutable = false
                        requisitionItem.orderIndex = count + index
                        requisition.updatedBy = session.user
                        requisition.addToRequisitionItems(requisitionItem)
                        requisition.save()
                        processedProductCodes << productCode
                    } else {
                        ignoredProductCodes << productCode
                    }
                } else {
                    ignoredProductCodes << productCode
                }
            }
            flash.message = "Added requisition item with product codes " + processedProductCodes ?: "none" + " (ignored: " + ignoredProductCodes + ")"
        }
        redirect(action: "edit", id: requisition.id)
    }


    def removeFromRequisitionItems = {
        def requisition = Requisition.get(params.id)

        if (requisition) {
            def requisitionItem = RequisitionItem.get(params?.requisitionItem?.id)
            if (requisitionItem) {
                requisition.removeFromRequisitionItems(requisitionItem)
                requisition.lastUpdated = new Date()
                requisition.updatedBy = session.user
                requisition.save()
            }
        }

        redirect(action: "edit", id: requisition.id)
    }


    def export = {
        def requisition = Requisition.get(params.id)
        def hasRoleFinance = userService.hasRoleFinance(session?.user)

        if (requisition) {
            def date = new Date()
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
                csv << [productCode: "", productName: "", quantity: "", unitOfMeasure: "", unitCost: "", totalCost: ""]
            }

            response.contentType = "text/csv"
            response.setHeader("Content-disposition", "attachment; filename=\"Stock List - ${requisition?.destination?.name} - ${date.format("yyyyMMdd-hhmmss")}.csv\"")
            render(contentType: "text/csv", text: csv.writer.toString())
            return
        } else {
            render(text: 'No requisition found', status: 404)
        }

    }

    def batch = {
        def requisition = Requisition.get(params.id)


        [requisition: requisition]
    }


    def importAsString = {
        def lines = []
        def data = []

        def requisition = Requisition.get(params.id)
        if (requisition) {
            def delimiter = params.delimiter
            if (delimiter) {
                if (params.csv) {

                    println "CSV " + params.csv
                    params?.csv?.toCsvReader('separatorChar': delimiter, 'skipLines': params.skipLines ?: 0).eachLine { tokens ->
                        println "line: " + tokens + " delimiter=" + delimiter
                        println "ROW " + tokens
                        if (tokens) {
                            data << tokens
                        }
                    }
                }
            } else {
                flash.message = "Please choose a delimiter"
            }
        }
        session.data = data
        render(view: "batch", model: [requisition: requisition, data: data])
    }

    def importAsFile = {

        def skipLines = params.skipLines ?: 0
        def delimiter = params.delimiter ?: ","
        def requisition = Requisition.get(params.id)
        def data = []
        if (requisition) {
            def file = request.getFile('file')

            if (!file) {
                throw new IllegalArgumentException("Must specify a file")
            }

            file.inputStream.toCsvReader('separatorChar': delimiter, 'skipLines': skipLines).eachLine { tokens ->
                println "line: " + tokens + " delimiter=" + delimiter
                println "ROW " + tokens
                if (tokens) {
                    data << tokens[0..3]
                }
            }

            log.info "Data: " + data

        }
        session.data = data
        render(view: "batch", model: [requisition: requisition, data: data])

    }

    def doImport = {

        def updateCount = 0
        def insertCount = 0
        def ignoreCount = 0
        def requisition = Requisition.get(params.id)
        if (session.data) {
            flash.errors = []

            def data = session.data
            data.eachWithIndex { row, index ->
                // Ignore the first row if the user included header info
                if (row[0] != "Product Code" && row[2] != "Quantity") {
                    try {
                        def productCode = row[0]
                        def quantity = Integer.parseInt(row[2])
                        def unitOfMeasure = row[3]
                        println "Quantity " + quantity
                        // Ignore if quantity is null or 0
                        if (quantity) {
                            def product = Product.findByProductCode(productCode)
                            if (product) {
                                def requisitionItem = requisition.requisitionItems.find {
                                    it.product == product
                                }
                                if (requisitionItem) {
                                    if (requisitionItem.quantity != quantity) {
                                        requisitionItem.quantity = quantity
                                        updateCount++
                                    } else {
                                        ignoreCount++
                                    }
                                } else {
                                    requisitionItem = new RequisitionItem()
                                    requisitionItem.product = product
                                    requisitionItem.orderIndex = index
                                    requisitionItem.quantity = quantity
                                    requisitionItem.substitutable = false
                                    requisition.addToRequisitionItems(requisitionItem)
                                    insertCount++
                                }
                            } else {
                                flash.errors << "${index + 1}: Product with product code '${row[0]}' does not exist"
                                ignoreCount++
                            }
                        }
                    } catch (NumberFormatException e) {
                        flash.errors << "${index + 1}: Invalid quantity '${row[2]}' for product code '${row[0]}'"
                        ignoreCount++
                    }
                }
            }
            requisition.save(flush: true)
            flash.message = "Imported ${insertCount} stock list items; updated ${updateCount} stock list items; ignored ${ignoreCount} stock list items"
        }
        redirect(action: "batch", id: params.id)

    }

    private List<Location> getDepots() {
        Location.list().findAll { location -> location.id != session.warehouse.id && location.isWarehouse() }.sort {
            it.name
        }
    }

    private List<Location> getWardsPharmacies() {
        def current = Location.get(session.warehouse.id)
        def locations = []
        if (current) {
            if (current?.locationGroup == null) {
                locations = Location.list().findAll { location -> location.isWardOrPharmacy() }.sort {
                    it.name
                }
            } else {
                locations = Location.list().findAll { location -> location.locationGroup?.id == current.locationGroup?.id }.findAll { location -> location.isWardOrPharmacy() }.sort {
                    it.name
                }
            }
        }
        return locations
    }
}
