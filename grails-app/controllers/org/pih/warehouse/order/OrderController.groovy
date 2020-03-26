/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.order

import grails.validation.ValidationException
import org.apache.commons.lang.StringEscapeUtils
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Document
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.springframework.web.multipart.MultipartFile

class OrderController {
    def orderService
    def stockMovementService
    def reportService
    def shipmentService

    static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = { OrderCommand command ->

        def suppliers = orderService.getSuppliers().sort()

        def orderTemplate = new Order(params)
        orderTemplate.orderTypeCode = params.orderTypeCode ? params.orderTypeCode as OrderTypeCode : OrderTypeCode.PURCHASE_ORDER
        orderTemplate.status = params.status ? Enum.valueOf(OrderStatus.class, params.status) : null
        def statusStartDate = params.statusStartDate ? Date.parse("MM/dd/yyyy", params.statusStartDate) : null
        def statusEndDate = params.statusEndDate ? Date.parse("MM/dd/yyyy", params.statusEndDate) : null
        def orders = orderService.getOrders(orderTemplate, statusStartDate, statusEndDate, params)

        def totalPrice = 0.00
        if (orders) {
            totalPrice = orders.sum { it.totalPrice() }
        }

        def orderedByList = orders.collect { it.orderedBy }.unique()

        [
                orders         : orders,
                command        : command,
                status         : orderTemplate.status,
                statusStartDate: statusStartDate,
                statusEndDate  : statusEndDate,
                suppliers      : suppliers,
                totalPrice     : totalPrice,
                orderedByList  : orderedByList,
                orderTypeCode  : orderTemplate?.orderTypeCode
        ]
    }

    def listOrderItems = {
        def orderItems = OrderItem.getAll().findAll { !it.isCompletelyFulfilled() }
        return [orderItems: orderItems]
    }

    def create = {
        redirect(controller: 'purchaseOrderWorkflow', action: 'index')
    }



    def shipOrder = {
        Order orderInstance = Order.get(params.id)
        [orderInstance:orderInstance]
    }

    def saveShipmentItems = { ShipOrderCommand command ->
        log.info "Command: ${command}"
        if (!command.validate() || command.hasErrors()) {
            render(view: "shipOrderItems", model: [orderInstance: command.order, command: command])
            return
        }

        try {
            String commandId = command.order.id
            Order order = Order.get(commandId)
            def pendingShipments = order.getShipmentsByStatus(ShipmentStatusCode.PENDING)
            if (pendingShipments.size() > 0) {
                String shipmentId = pendingShipments.first().id
                shipmentService.updateOrCreateOrderBasedShipmentItems(command.order, Shipment.get(shipmentId))
                redirect(controller: 'stockMovement', action: 'createPurchaseOrders', params: [id: shipmentId])
                return
            }
            Shipment shipment = stockMovementService.createInboundShipment(command)
            if (shipment) {
                redirect(controller: 'stockMovement', action: 'createPurchaseOrders', params: [id: shipment.id])
                return
            }
        } catch (ValidationException e) {
            command.errors = e.errors
            command.order = Order.load(command.order.id)
            render (view: "shipOrder", model: [command:command, orderInstance: command.order])
            return

        }
        redirect (action: "shipOrder", id: command.order.id)
    }

    def save = {
        def orderInstance = new Order(params)
        if (orderInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
            redirect(action: "list", id: orderInstance.id)
        } else {
            render(view: "create", model: [orderInstance: orderInstance])
        }
    }

    def show = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            [orderInstance: orderInstance]
        }
    }

    def edit = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            return [orderInstance: orderInstance]
        }
    }

    def placeOrder = {
        log.info "Issue order " + params
        def orderInstance = orderService.placeOrder(params.id, session.user.id)
        if (orderInstance) {
            if (orderInstance.hasErrors()) {
                render(view: 'show', model: [orderInstance: orderInstance])
            } else {
                flash.message = "${warehouse.message(code: 'order.orderHasBeenPlacedWithVendor.message', args: [orderInstance?.orderNumber, orderInstance?.origin?.name])}"
                redirect(action: 'show', id: orderInstance.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'order.notFound.message', args: [params.id], default: 'Order {0} was not found.')}"
            redirect(action: "list")
        }
    }


    def update = {
        def orderInstance = Order.get(params.id)
        if (orderInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (orderInstance.version > version) {

                    orderInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'order.label', default: 'Order')] as Object[], "Another user has updated this Order while you were editing")
                    render(view: "edit", model: [orderInstance: orderInstance])
                    return
                }
            }
            orderInstance.properties = params
            if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
                redirect(action: "list", id: orderInstance.id)
            } else {
                render(view: "edit", model: [orderInstance: orderInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }
    }


    def delete = {
        def orderInstance = Order.get(params.id)
        if (orderInstance) {
            try {
                orderService.deleteOrder(orderInstance)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.orderNumber])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.orderNumber])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }
    }

    def addAdjustment = {
        def orderInstance = Order.get(params?.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            render(view: "editAdjustment", model: [orderInstance: orderInstance, orderAdjustment: new OrderAdjustment()])
        }
    }

    def editAdjustment = {
        log.info "params: ${params}"
        def orderInstance = Order.get(params?.order?.id)
        if (!orderInstance) {
                log.info "order not found"
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            def orderAdjustment = OrderAdjustment.get(params?.id)
            if (!orderAdjustment) {
                log.info "order adjustement not found"
                flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), commentInstance.id])}"
                redirect(action: "show", id: orderInstance?.id)
            }
            render(view: "editAdjustment", model: [orderInstance: orderInstance, orderAdjustment: orderAdjustment])
        }
    }

    def saveAdjustment = {
        def orderInstance = Order.get(params?.order?.id)
        if (orderInstance) {
            def orderAdjustment = OrderAdjustment.get(params?.id)
            if (orderAdjustment) {
                orderAdjustment.properties = params
                if (!orderAdjustment.hasErrors() && orderAdjustment.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'orderAdjustment.label', default: 'Order Adjustment'), orderAdjustment.id])}"
                    redirect(action: "show", id: orderInstance.id)
                } else {
                    render(view: "editAdjustment", model: [orderInstance: orderInstance, orderAdjustment: orderAdjustment])
                }
            } else {
                orderAdjustment = new OrderAdjustment(params)
                orderInstance.addToOrderAdjustments(orderAdjustment)
                if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
                    redirect(action: "show", id: orderInstance.id)
                } else {
                    render(view: "editAdjustment", model: [orderInstance: orderInstance, orderAdjustment: orderAdjustment])
                }
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }

    }

    def deleteAdjustment = {
        def orderInstance = Order.get(params.order.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.order.id])}"
            redirect(action: "list")
        } else {
            def orderAdjustment = OrderAdjustment.get(params?.id)
            if (!orderAdjustment) {
                flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'orderAdjustment.label', default: 'Order Adjustment'), params.id])}"
                redirect(action: "show", id: orderInstance?.id)
            } else {
                orderInstance.removeFromOrderAdjustments(orderAdjustment)
                orderAdjustment.delete()
                if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
                    redirect(action: "show", id: orderInstance.id)
                } else {
                    render(view: "show", model: [orderInstance: orderInstance])
                }
            }
        }
    }



    def addComment = {
        def orderInstance = Order.get(params?.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            return [orderInstance: orderInstance, commentInstance: new Comment()]
        }
    }

    def editComment = {
        def orderInstance = Order.get(params?.order?.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            def commentInstance = Comment.get(params?.id)
            if (!commentInstance) {
                flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), commentInstance.id])}"
                redirect(action: "show", id: orderInstance?.id)
            }
            render(view: "addComment", model: [orderInstance: orderInstance, commentInstance: commentInstance])
        }
    }

    def deleteComment = {
        def orderInstance = Order.get(params.order.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.order.id])}"
            redirect(action: "list")
        } else {
            def commentInstance = Comment.get(params?.id)
            if (!commentInstance) {
                flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), params.id])}"
                redirect(action: "show", id: orderInstance?.id)
            } else {
                orderInstance.removeFromComments(commentInstance)
                if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
                    redirect(action: "show", id: orderInstance.id)
                } else {
                    render(view: "show", model: [orderInstance: orderInstance])
                }
            }
        }
    }

    def saveComment = {
        log.info(params)

        def orderInstance = Order.get(params?.order?.id)
        if (orderInstance) {
            def commentInstance = Comment.get(params?.id)
            if (commentInstance) {
                commentInstance.properties = params
                if (!commentInstance.hasErrors() && commentInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), commentInstance.id])}"
                    redirect(action: "show", id: orderInstance.id)
                } else {
                    render(view: "addComment", model: [orderInstance: orderInstance, commentInstance: commentInstance])
                }
            } else {
                commentInstance = new Comment(params)
                orderInstance.addToComments(commentInstance)
                if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
                    redirect(action: "show", id: orderInstance.id)
                } else {
                    render(view: "addComment", model: [orderInstance: orderInstance, commentInstance: commentInstance])
                }
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }

    }

    def addDocument = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            return [orderInstance: orderInstance]
        }
    }

    def editDocument = {
        def orderInstance = Order.get(params?.order?.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            def documentInstance = Document.get(params?.id)
            if (!documentInstance) {
                flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), documentInstance.id])}"
                redirect(action: "show", id: orderInstance?.id)
            }
            render(view: "addDocument", model: [orderInstance: orderInstance, documentInstance: documentInstance])
        }
    }

    def deleteDocument = {
        def orderInstance = Order.get(params.order.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.order.id])}"
            redirect(action: "list")
        } else {
            def documentInstance = Document.get(params?.id)
            if (!documentInstance) {
                flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), params.id])}"
                redirect(action: "show", id: orderInstance?.id)
            } else {
                orderInstance.removeFromDocuments(documentInstance)
                if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
                    redirect(action: "show", id: orderInstance.id)
                } else {
                    render(view: "show", model: [orderInstance: orderInstance])
                }
            }
        }
    }

    def receive = {
        def orderCommand = orderService.getOrder(params.id, session.user.id)
        if (!orderCommand.order) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            return [orderCommand: orderCommand]
        }
    }

    def saveOrderShipment = { OrderCommand command ->
        bindData(command, params)
        def orderInstance = Order.get(params?.order?.id)
        command.order = orderInstance

        orderService.saveOrderShipment(command)

        // If the shipment was saved, let's redirect back to the order received page
        if (!command?.shipment?.hasErrors() && command?.shipment?.id) {
            redirect(controller: "order", action: "receive", id: params?.order?.id)
        }

        // Otherwise, we want to display the errors, so we need to render the page.
        render(view: "receive", model: [orderCommand: command])
    }


    def fulfill = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            return [orderInstance: orderInstance]
        }
    }

    def addOrderItemToShipment = {

        def orderInstance = Order.get(params?.id)
        def orderItem = OrderItem.get(params?.orderItem?.id)
        def shipmentInstance = Shipment.get(params?.shipment?.id)

        if (orderItem) {
            def shipmentItem = new ShipmentItem(orderItem.properties)
            shipmentInstance.addToShipmentItems(shipmentItem)
            if (!shipmentInstance.hasErrors() && shipmentInstance?.save(flush: true)) {
                // TODO: Refactor this part
            } else {
                flash.message = "${warehouse.message(code: 'order.shipmentItemErrors.message')}"
                render(view: "fulfill", model: [orderItemInstance: orderItem, shipmentInstance: shipmentInstance])
                return
            }
        }

        redirect(action: "fulfill", id: orderInstance?.id)

    }

    def download = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")

        } else {

            def date = new Date()
            response.setHeader("Content-disposition", "attachment; filename=\"${orderInstance?.orderNumber?.encodeAsHTML()}-${date.format("MM-dd-yyyy")}.csv\"")
            response.contentType = "text/csv"
            def csv = "PO Number,${orderInstance?.orderNumber}\n" +
                    "Description,${StringEscapeUtils.escapeCsv(orderInstance?.name)}\n" +
                    "Vendor,${StringEscapeUtils.escapeCsv(orderInstance?.origin.name)}\n" +
                    "Ship to,${orderInstance?.destination?.name}\n" +
                    "Ordered by,${orderInstance?.orderedBy?.name} ${orderInstance?.orderedBy?.email}\n" +
                    "\n"

            csv += "${warehouse.message(code: 'product.productCode.label')}," +
                    "${warehouse.message(code: 'product.name.label')}," +
                    "${warehouse.message(code: 'product.vendorCode.label')}," +
                    "${warehouse.message(code: 'orderItem.quantity.label')}," +
                    "${warehouse.message(code: 'product.unitOfMeasure.label')}," +
                    "${warehouse.message(code: 'orderItem.unitPrice.label')}," +
                    "${warehouse.message(code: 'orderItem.totalPrice.label')}" +
                    "\n"

            def totalPrice = 0.0

            orderInstance?.listOrderItems()?.each { orderItem ->
                totalPrice += orderItem.totalPrice() ?: 0

                String quantityString = formatNumber(number: orderItem?.quantity, maxFractionDigits: 1, minFractionDigits: 1)
                String unitPriceString = formatNumber(number: orderItem?.unitPrice, maxFractionDigits: 4, minFractionDigits: 2)
                String totalPriceString = formatNumber(number: orderItem?.totalPrice(), maxFractionDigits: 2, minFractionDigits: 2)

                csv += "${orderItem?.product?.productCode}," +
                        "${StringEscapeUtils.escapeCsv(orderItem?.product?.name)}," +
                        "${orderItem?.product?.vendorCode ?: ''}," +
                        "${quantityString}," +
                        "${orderItem?.product?.unitOfMeasure ?: 'EA'}," +
                        "${StringEscapeUtils.escapeCsv(unitPriceString)}," +
                        "${StringEscapeUtils.escapeCsv(totalPriceString)}" +
                        "\n"
            }

            String totalPriceString = formatNumber(number: totalPrice, maxFractionDigits: 2, minFractionDigits: 2)
            csv += ",,,,,,${StringEscapeUtils.escapeCsv(totalPriceString)}\n"
            render csv

        }
    }

    def downloadOrderItems = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            def date = new Date()
            response.setHeader("Content-disposition", "attachment; filename='\"${orderInstance.orderNumber}-${date.format("MM-dd-yyyy")}.csv\"")
            response.contentType = "text/csv"
            def csv = ""

            csv += "${warehouse.message(code: 'product.productCode.label')}," +
                    "${warehouse.message(code: 'product.name.label')}," +
                    "${warehouse.message(code: 'product.vendorCode.label')}," +
                    "${warehouse.message(code: 'orderItem.quantity.label')}," +
                    "${warehouse.message(code: 'product.unitOfMeasure.label')}," +
                    "${warehouse.message(code: 'orderItem.unitPrice.label')}," +
                    "${warehouse.message(code: 'orderItem.totalPrice.label')}," +
                    "\n"

            def totalPrice = 0.0

            orderInstance?.listOrderItems()?.each { orderItem ->
                totalPrice += orderItem.totalPrice() ?: 0

                String quantityString = formatNumber(number: orderItem?.quantity, maxFractionDigits: 1, minFractionDigits: 1)
                String unitPriceString = formatNumber(number: orderItem?.unitPrice, maxFractionDigits: 4, minFractionDigits: 2)
                String totalPriceString = formatNumber(number: orderItem?.totalPrice(), maxFractionDigits: 2, minFractionDigits: 2)

                csv += "${orderItem?.product?.productCode}," +
                        "${StringEscapeUtils.escapeCsv(orderItem?.product?.name)}," +
                        "${orderItem?.product?.vendorCode ?: ''}," +
                        "${quantityString}," +
                        "${orderItem?.product?.unitOfMeasure ?: 'EA'}," +
                        "${StringEscapeUtils.escapeCsv(unitPriceString)}," +
                        "${StringEscapeUtils.escapeCsv(totalPriceString)}" +
                        "\n"
            }
            render csv

        }
    }

    def importOrderItems = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {

            try {
                MultipartFile multipartFile = request.getFile('fileContents')
                if (multipartFile.empty) {
                    flash.message = "File cannot be empty. Please select a packing list to import."
                    redirect(action: "show", id: params.id)
                    return
                }
                List lineItems = orderService.parseOrderItems(multipartFile.inputStream)
                log.info "Line items: " + lineItems

                if (orderService.importOrderItems(params.id, lineItems)) {
                    flash.message = "Successfully imported ${lineItems?.size()} order line items. "

                } else {
                    flash.message = "Failed to import packing list items due to an unknown error."
                }
            } catch (Exception e) {
                log.warn("Failed to import packing list due to the following error: " + e.message, e)
                flash.message = "Failed to import packing list due to the following error: " + e.message
            }
        }
        redirect(action: "show", id: params.id)
    }


    def upload = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            return [orderInstance: orderInstance]
        }
    }


    def print = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            return [orderInstance: orderInstance]
        }
    }


    def renderPdf = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {

            def baseUri = request.scheme + "://" + request.serverName + ":" + request.serverPort

            // JSESSIONID is required because otherwise the login page is rendered
            def url = baseUri + params.url + ";jsessionid=" + session.getId()
            url += "?print=true"
            url += "&location.id=" + params.location.id
            url += "&category.id=" + params.category.id
            url += "&startDate=" + params.startDate
            url += "&endDate=" + params.endDate
            url += "&showTransferBreakdown=" + params.showTransferBreakdown
            url += "&hideInactiveProducts=" + params.hideInactiveProducts
            url += "&insertPageBreakBetweenCategories=" + params.insertPageBreakBetweenCategories
            url += "&includeChildren=" + params.includeChildren
            url += "&includeEntities=true"

            // Let the browser know what content type to expect
            response.setContentType("application/pdf")

            // Render pdf to the response output stream
            log.info "BaseUri is $baseUri"
            log.info("Session ID: " + session.id)
            log.info "Fetching url $url"
            reportService.generatePdf(url, response.getOutputStream())


        }
    }


    def rollbackOrderStatus = {

        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            orderService.rollbackOrderStatus(params.id)

        }
        redirect(action: "show", id: params.id)

    }


    def redirectFromStockMovement = {
        def stockMovement = stockMovementService.getStockMovement(params.id)
        def shipmentItem = stockMovement?.getShipment()?.getShipmentItems()?.first()
        redirect(action: 'show', params: [id: shipmentItem?.orderItems*.order*.id])
    }
}
