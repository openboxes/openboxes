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

import grails.converters.JSON
import grails.validation.ValidationException
import org.apache.commons.lang.StringEscapeUtils
import org.grails.plugins.csv.CSVWriter
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.UomService
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.springframework.web.multipart.MultipartFile

class OrderController {
    def orderService
    def stockMovementService
    def reportService
    def shipmentService
    UomService uomService
    def userService
    def productSupplierDataService

    static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = { OrderCommand command ->

        // Parse date parameters
        Date statusStartDate = params.statusStartDate ? Date.parse("MM/dd/yyyy", params.statusStartDate) : null
        Date statusEndDate = params.statusEndDate ? Date.parse("MM/dd/yyyy", params.statusEndDate) : null

        // Set default values
        params.destination = params.destination?:session?.warehouse?.id
        params.orderTypeCode = params.orderTypeCode ? Enum.valueOf(OrderTypeCode.class, params.orderTypeCode) : OrderTypeCode.PURCHASE_ORDER
        params.status = params.status ? Enum.valueOf(OrderStatus.class, params.status) : null

        // Pagination parameters
        params.max = params.format ? null : params.max?:10
        params.offset = params.format ? null : params.offset?:0

        def orderTemplate = new Order(params)
        def orders = orderService.getOrders(orderTemplate, statusStartDate, statusEndDate, params)

        if (params.format && orders) {

            def sw = new StringWriter()
            def csv = new CSVWriter(sw, {
                "Supplier organization" { it.supplierOrganization }
                "Supplier location" { it.supplierLocation }
                "PO Number" { it.number }
                "PO Description" { it.description }
                "PO Status" { it.status }
                "Code" { it.code }
                "Product" { it.productName }
                "Source Code" { it.sourceCode }
                "Supplier Code" { it.supplierCode }
                "Manufacturer" { it.manufacturer }
                "Manufacturer Code" { it.manufacturerCode }
                "Unit of Measure" { it.unitOfMeasure }
                "Quantity Ordered" { it.quantityOrdered }
                "Quantity Shipped" { it.quantityShipped}
                "Quantity Received" { it.quantityReceived}
                "Unit Price" { it.unitPrice}
                "Total Cost" { it.totalCost}
                "Recipient" { it.recipient}
                "Estimated Ready Date" { it.estimatedReadyDate}
                "Actual Ready Date" { it.actualReadyDate}
            })

            orders*.orderItems*.each { orderItem ->
                csv << [
                        supplierOrganization: orderItem.order.origin.organization,
                        supplierLocation: orderItem.order.origin.name,
                        number       : orderItem.order.orderNumber,
                        description       : orderItem.order.name ?: '',
                        status       : orderItem.order.displayStatus,
                        code       : orderItem.product.productCode,
                        productName       : orderItem.product.name,
                        sourceCode       : orderItem.productSupplier?.code ?: '',
                        supplierCode       : orderItem.productSupplier?.supplierCode ?: '',
                        manufacturer       : orderItem.productSupplier?.manufacturer?.name ?: '',
                        manufacturerCode       : orderItem.productSupplier?.manufacturerCode ?: '',
                        unitOfMeasure: orderItem.unitOfMeasure ?: '',
                        quantityOrdered: orderItem.quantity,
                        quantityShipped: orderItem.quantityShipped,
                        quantityReceived: orderItem.quantityReceived,
                        unitPrice:  orderItem.unitPrice ?: '',
                        totalCost: orderItem.total ?: '',
                        recipient: orderItem.recipient ?: '',
                        estimatedReadyDate: orderItem.estimatedReadyDate?.format("MM/dd/yyyy") ?: '',
                        actualReadyDate: orderItem.actualReadyDate?.format("MM/dd/yyyy") ?: '',
                ]
            }

            response.setHeader("Content-disposition", "attachment; filename=\"Orders-${new Date().format("MM/dd/yyyy")}.csv\"")
            render(contentType: "text/csv", text: sw.toString(), encoding: "UTF-8")
        }

        def totalPrice = orders?.sum { it.totalNormalized?:0.0 } ?:0.0

        [
                orders         : orders,
                command        : command,
                status         : orderTemplate.status,
                statusStartDate: statusStartDate,
                statusEndDate  : statusEndDate,
                totalPrice     : totalPrice,
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
        Order order = Order.get(params.id)

        // Use command populated on saveShipmentItems (probably contains errors) OR create a new one
        ShipOrderCommand command = new ShipOrderCommand(order: order, shipment: order.pendingShipment)

        // Populate the line items from existing pending shipment
        order.orderItems.each { OrderItem orderItem ->

            // Find shipment item associated with given order item
            def shipmentItems =
                    order?.pendingShipment?.shipmentItems?.findAll { ShipmentItem shipmentItem ->
                        return shipmentItem.orderItems.any { it == orderItem }
            }

            if (shipmentItems) {
                shipmentItems.each { ShipmentItem shipmentItem ->
                    ShipOrderItemCommand shipOrderItem =
                            new ShipOrderItemCommand(
                                    lotNumber: shipmentItem?.inventoryItem?.lotNumber,
                                    expirationDate: shipmentItem?.inventoryItem?.expirationDate,
                                    orderItem: orderItem,
                                    shipmentItem: shipmentItem,
                                    quantityMinimum: 0,
                                    quantityToShip: (shipmentItem.quantity / orderItem?.quantityPerUom) as int,
                                    quantityMaximum: orderItem.quantityRemaining

                            )
                    command.shipOrderItems.add(shipOrderItem)
                }
            }
            // Or populate line items from quantity remaining on order item
            else {
                def quantityRemaining = orderItem?.quantityRemaining
                ShipOrderItemCommand shipOrderItem =
                            new ShipOrderItemCommand(
                                    orderItem: orderItem,
                                    quantityMinimum: 0,
                                    quantityToShip: order.pendingShipment ? 0 : quantityRemaining,
                                    quantityMaximum: quantityRemaining)
                    command.shipOrderItems.add(shipOrderItem)
            }
        }
        [command: command]
    }

    def saveShipmentItems = { ShipOrderCommand command ->
        if (!command.validate() || command.hasErrors()) {
            render(view: "shipOrderItems", model: [orderInstance: command.order, command: command])
            return
        }
        try {
            Order order = Order.get(command?.order?.id)

            boolean hasAtLeastOneItem = command.shipOrderItems.any { ShipOrderItemCommand shipOrderItem -> shipOrderItem.quantityToShip > 0 }
            if (!hasAtLeastOneItem) {
                order.errors.reject("Must specify at least one item to ship")
                throw new ValidationException("Invalid order", order.errors)
            }

            def shipOrderItemsByOrderItem = command.shipOrderItems.groupBy { ShipOrderItemCommand shipOrderItem -> shipOrderItem.orderItem }
            order.orderItems.each { OrderItem orderItem ->
                List shipOrderItems = shipOrderItemsByOrderItem.get(orderItem)
                BigDecimal totalQuantityToShip = shipOrderItems.sum { it?.quantityToShip?:0 }
                if (totalQuantityToShip > orderItem.quantityRemaining) {
                    orderItem.errors.reject("Sum of quantity to ship (${totalQuantityToShip}) " +
                            "cannot be greater than remaining quantity (${orderItem.quantityRemaining}) " +
                            "for order item '${orderItem.product.productCode} ${orderItem.product.name}'"
                    )
                    throw new ValidationException("Invalid order item", orderItem.errors)
                }
            }

            if (order?.pendingShipment) {
                command.shipment = order.pendingShipment
                shipmentService.updateOrCreateOrderBasedShipmentItems(command)
                redirect(controller: 'stockMovement', action: 'createPurchaseOrders', params: [id: order?.pendingShipment?.id])
                return
            }
            Shipment shipment = stockMovementService.createInboundShipment(command)
            if (shipment) {
                redirect(controller: 'stockMovement', action: 'createPurchaseOrders', params: [id: shipment.id])
                return
            }
        } catch (ValidationException e) {
            log.error("Validation error: " + e.message, e)
            flash.errors = e.errors
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


    def remove = {
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
                        "${orderItem?.unitOfMeasure}," +
                        "${StringEscapeUtils.escapeCsv(unitPriceString)}," +
                        "${StringEscapeUtils.escapeCsv(totalPriceString)}" +
                        "\n"
            }

            String totalPriceString = formatNumber(number: totalPrice, maxFractionDigits: 2, minFractionDigits: 2)
            csv += ",,,,,,${StringEscapeUtils.escapeCsv(totalPriceString)}\n"
            render csv

        }
    }

    def orderItemFormDialog = {
        OrderItem orderItem = OrderItem.get(params.id)
        if (!orderService.canOrderItemBeEdited(orderItem, session.user)) {
            throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
        }
        render(template: "orderItemFormDialog", model: [orderItem:orderItem, canEdit: orderService.canOrderItemBeEdited(orderItem, session.user)])
    }

    def removeOrderItem = {
        OrderItem orderItem = OrderItem.get(params.id)
        if (orderItem) {
            if (orderItem.hasShipmentAssociated() || !orderService.canOrderItemBeEdited(orderItem, session.user)) {
                throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
            }
            Order order = orderItem.order
            order.removeFromOrderItems(orderItem)
            orderItem.delete()
            order.save()
            render (status: 200, text: "Successfully deleted order item")
        }
        else {
            render (status: 404, text: "Unable to locate order item")
        }
    }

    def saveOrderItem = {
        Order order = Order.get(params.order.id)
        OrderItem orderItem = OrderItem.get(params.orderItem.id)
        ProductSupplier productSupplier = null
        if (params.productSupplier?.id || params.supplierCode) {
            productSupplier = productSupplierDataService.getOrCreateNew(params)
        }
        params.remove("productSupplier")
        params.remove("productSupplier.id")

        if (!orderItem) {
            orderItem = new OrderItem(params)
            order.addToOrderItems(orderItem)
        }
        else {
            if (!orderService.canOrderItemBeEdited(orderItem, session.user)) {
                throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
            }
            orderItem.properties = params
            Shipment pendingShipment = order.pendingShipment
            if (pendingShipment) {
                Set<ShipmentItem> itemsToUpdate = pendingShipment.shipmentItems.findAll { it.orderItemId == orderItem.id }
                itemsToUpdate.each { itemToUpdate ->
                    itemToUpdate.recipient = orderItem.recipient
                }
            }
        }

        if (productSupplier != null) {
            orderItem.productSupplier = productSupplier
        }

        try {
            if (!order.save(flush:true)) {
                throw new ValidationException("Order is invalid", order.errors)
            }
        } catch (Exception e) {
            log.error("Error " + e.message, e)
            render(status: 500, text: "Not saved")
        }
        render (status: 200, text: "Successfully added order item")
    }

    def getOrderItems = {
        def orderInstance = Order.get(params.id)
        def orderItems = orderInstance.orderItems.collect {

            String quantityUom = "${it?.quantityUom?.code?:g.message(code:'default.ea.label')?.toUpperCase()}"
            String quantityPerUom = "${g.formatNumber(number: it?.quantityPerUom?:1, maxFractionDigits: 0)}"
            String unitOfMeasure = "${quantityUom}/${quantityPerUom}"

            [
                    id: it.id,
                    product: it.product,
                    quantity: it.quantity,
                    quantityUom: quantityUom,
                    quantityPerUom: quantityPerUom,
                    unitOfMeasure: unitOfMeasure,
                    totalQuantity: (it?.quantity?:1) * (it?.quantityPerUom?:1),
                    productPackage: it?.productPackage,
                    currencyCode: it?.order?.currencyCode,
                    unitPrice:  g.formatNumber(number: it.unitPrice),
                    totalPrice: g.formatNumber(number: it.totalPrice()),
                    estimatedReadyDate: g.formatDate(date: it.estimatedReadyDate, format: Constants.DEFAULT_DATE_FORMAT),
                    actualReadyDate: g.formatDate(date: it.actualReadyDate, format: Constants.DEFAULT_DATE_FORMAT),
                    productSupplier: it.productSupplier,
                    recipient: it.recipient,
                    isOrderPending: it?.order?.status == OrderStatus.PENDING,
                    dateCreated: it.dateCreated,
                    canEdit: orderService.canOrderItemBeEdited(it, session.user),
                    manufacturerName: it.productSupplier?.manufacturer?.name
            ]
        }
        orderItems = orderItems.sort { it.dateCreated }
        render orderItems as JSON
    }


    def downloadOrderItems = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            def date = new Date()
            response.setHeader("Content-disposition", "attachment; filename=\"${orderInstance.orderNumber}-${date.format("MM-dd-yyyy")}.csv\"")
            response.contentType = "text/csv"
            def csv = ""

            csv += "${warehouse.message(code: 'orderItem.id.label')}," + // code
                    "${warehouse.message(code: 'product.productCode.label')}," + // Product
                    "${warehouse.message(code: 'product.name.label')}," + // Product
                    "${warehouse.message(code: 'product.sourceCode.label')}," + // source Code
                    "${warehouse.message(code: 'product.supplierCode.label')}," + // supplier code
                    "${warehouse.message(code: 'product.manufacturer.label')}," + // manufacturer
                    "${warehouse.message(code: 'product.manufacturerCode.label')}," + // manufacturer code
                    "${warehouse.message(code: 'default.quantity.label')}," + // qty
                    "${warehouse.message(code: 'default.unitOfMeasure.label')}," + // UoM
                    "${warehouse.message(code: 'default.cost.label')}," + // unit price
                    "${warehouse.message(code: 'orderItem.totalCost.label')}," + // total cost
                    "${warehouse.message(code: 'order.recipient.label')}," + // recipient
                    "${warehouse.message(code: 'orderItem.estimatedReadyDate.label')}," + // estimated ready date
                    "\n"

            def totalPrice = 0.0

            orderInstance?.listOrderItems()?.each { orderItem ->
                totalPrice += orderItem.totalPrice() ?: 0

                String quantityString = formatNumber(number: orderItem?.quantity, maxFractionDigits: 1, minFractionDigits: 1)
                String unitPriceString = formatNumber(number: orderItem?.unitPrice, maxFractionDigits: 4, minFractionDigits: 2)
                String totalPriceString = formatNumber(number: orderItem?.totalPrice(), maxFractionDigits: 2, minFractionDigits: 2)
                String unitOfMeasure = orderItem?.quantityUom ? "${orderItem?.quantityUom?.name}/${orderItem?.quantityPerUom}" : orderItem?.unitOfMeasure

                csv += "${orderItem?.id}," +
                        "${orderItem?.product?.productCode}," +
                        "${StringEscapeUtils.escapeCsv(orderItem?.product?.name)}," +
                        "${orderItem?.productSupplier?.code ?: ''}," +
                        "${orderItem?.productSupplier?.supplierCode ?: ''}," +
                        "${orderItem?.productSupplier?.manufacturer?.name ?: ''}," +
                        "${orderItem?.productSupplier?.manufacturerCode ?: ''}," +
                        "${quantityString}," +
                        "${unitOfMeasure}," +
                        "${StringEscapeUtils.escapeCsv(unitPriceString)}," +
                        "${StringEscapeUtils.escapeCsv(totalPriceString)}," +
                        "${orderItem?.recipient?.name ?: ''}," +
                        "${orderItem?.estimatedReadyDate?.format("MM/dd/yyyy") ?: ''}," +
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
                List lineItems = orderService.parseOrderItems(multipartFile.inputStream.text)
                log.info "Line items: " + lineItems

                if (orderService.importOrderItems(params.id, params.supplierId, lineItems)) {
                    flash.message = "Successfully imported ${lineItems?.size()} order line items. "
                } else {
                    flash.message = "Failed to import packing list items due to an unknown error."
                }
            } catch (Exception e) {
                log.warn("Failed to import order items list due to the following error: " + e.message, e)
                render (status: 500, text: "Failed to import order items list due to the following error: " + e.message)
                return
            }
        }
        render (status: 200, text: "Successfully added order items")
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
        // FIXME Need to clean this up a bit (move logic to Shipment or ShipmentItem)
        def stockMovement = stockMovementService.getStockMovement(params.id)
        def shipmentItem = stockMovement?.shipment?.shipmentItems?.first()
        def orderIds = shipmentItem?.orderItems*.order*.id
        def orderId = orderIds?.flatten().first()
        redirect(action: 'shipOrder', id: orderId)
    }
}
