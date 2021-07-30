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

import fr.opensagres.xdocreport.converter.ConverterTypeTo
import grails.converters.JSON
import grails.validation.ValidationException
import org.apache.commons.lang.StringEscapeUtils
import org.grails.plugins.csv.CSVWriter
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.User
import org.pih.warehouse.core.ValidationCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.springframework.web.multipart.MultipartFile

class OrderController {
    def orderService
    def stockMovementService
    def reportService
    def shipmentService
    def uomService
    def userService
    def productSupplierDataService
    def documentTemplateService

    static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = { OrderCommand command ->

        Location currentLocation = Location.get(session.warehouse.id)
        Boolean isCentralPurchasingEnabled = currentLocation.supports(ActivityCode.ENABLE_CENTRAL_PURCHASING)

        // Parse date parameters
        Date statusStartDate = params.statusStartDate ? Date.parse("MM/dd/yyyy", params.statusStartDate) : null
        Date statusEndDate = params.statusEndDate ? Date.parse("MM/dd/yyyy", params.statusEndDate) : null

        // Set default values
        params.destination = params.destination == null && !isCentralPurchasingEnabled ? session?.warehouse?.id : params.destination

        OrderType orderType = params.orderType ? OrderType.findByIdOrCode(params.orderType, params.orderType) : OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name())

        params.status = params.status ? Enum.valueOf(OrderStatus.class, params.status) : null
        params.destinationParty = isCentralPurchasingEnabled ? currentLocation?.organization?.id : params.destinationParty

        // Pagination parameters
        params.max = params.format ? null : params.max?:10
        params.offset = params.format ? null : params.offset?:0

        def orderTemplate = new Order(params)
        orderTemplate.orderType = orderType

        def orders = orderService.getOrders(orderTemplate, statusStartDate, statusEndDate, params)

        if (params.format && orders) {

            def sw = new StringWriter()
            def csv = new CSVWriter(sw, {
                "Supplier organization" { it.supplierOrganization }
                "Supplier location" { it.supplierLocation }
                "Destination" { it.destinationLocation }
                "PO Number" { it.number }
                "PO Description" { it.description }
                "PO Status" { it.status }
                "Code" { it.code }
                "Product" { it.productName }
                "Item Status" {it.itemStatus}
                "Source Code" { it.sourceCode }
                "Supplier Code" { it.supplierCode }
                "Manufacturer" { it.manufacturer }
                "Manufacturer Code" { it.manufacturerCode }
                "Unit of Measure" { it.unitOfMeasure }
                "Qty per UOM" { it.quantityPerUom }
                "Quantity Ordered" { it.quantityOrdered }
                "Quantity Shipped" { it.quantityShipped}
                "Quantity Received" { it.quantityReceived}
                "Quantity Invoiced" { it.quantityInvoiced}
                "Unit Price" { it.unitPrice}
                "Total Cost" { it.totalCost}
                "Currency" { it.currency }
                "Recipient" { it.recipient}
                "Estimated Ready Date" { it.estimatedReadyDate}
                "Actual Ready Date" { it.actualReadyDate}
                "Budget Code" { it.budgetCode }
            })

            orders*.orderItems*.each { orderItem ->
                csv << [
                        supplierOrganization: orderItem?.order?.origin?.organization?.code + " - " + orderItem?.order?.origin?.organization?.name,
                        supplierLocation: orderItem?.order?.origin?.name,
                        destinationLocation: orderItem?.order?.destination?.name,
                        number       : orderItem?.order?.orderNumber,
                        description       : orderItem?.order?.name ?: '',
                        status       : orderItem?.order?.displayStatus,
                        code       : orderItem?.product?.productCode,
                        productName       : orderItem?.product?.name,
                        itemStatus        : OrderItemStatusCode.CANCELED == orderItem?.orderItemStatusCode ? orderItem?.orderItemStatusCode?.name() : '',
                        sourceCode       : orderItem?.productSupplier?.code ?: '',
                        supplierCode       : orderItem?.productSupplier?.supplierCode ?: '',
                        manufacturer       : orderItem?.productSupplier?.manufacturer?.name ?: '',
                        manufacturerCode       : orderItem?.productSupplier?.manufacturerCode ?: '',
                        unitOfMeasure: orderItem.quantityUom?.code ?: '',
                        quantityPerUom: orderItem.quantityPerUom ?: '',
                        quantityOrdered: orderItem.quantity,
                        quantityShipped: orderItem.quantityShipped,
                        quantityReceived: orderItem.quantityReceived,
                        quantityInvoiced: orderItem.quantityInvoicedInStandardUom,
                        unitPrice:  orderItem.unitPrice ?: '',
                        totalCost: orderItem.total ?: '',
                        currency: orderItem?.order?.currencyCode,
                        recipient: orderItem.recipient ?: '',
                        estimatedReadyDate: orderItem.estimatedReadyDate?.format("MM/dd/yyyy") ?: '',
                        actualReadyDate: orderItem.actualReadyDate?.format("MM/dd/yyyy") ?: '',
                        budgetCode: orderItem.budgetCode?.code ?: '',
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
                orderType      : orderTemplate?.orderType,
                isCentralPurchasingEnabled : isCentralPurchasingEnabled
        ]
    }

    def listOrderItems = {
        def orderItems = OrderItem.getAll().findAll { !it.isCompletelyFulfilled() }
        return [orderItems: orderItems]
    }

    def create = {
        redirect(controller: 'purchaseOrder', action: 'index')
    }

    def save = {
        def orderInstance = new Order(params)
        if (orderInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
            redirect(action: "list", id: orderInstance.id, params: [orderType: orderInstance.orderType])
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
                redirect(action: "list", id: orderInstance.id, params: [orderType: orderInstance.orderType])
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
            if (orderInstance.hasPrepaymentInvoice) {
                flash.message = "${warehouse.message(code: 'order.errors.deletePrepaid.message')}"
                redirect(action: "show", id: orderInstance?.id)
                return
            }
            try {
                orderService.deleteOrder(orderInstance)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.orderNumber])}"
                redirect(action: "list", params: [orderType: orderInstance.orderType])
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.orderNumber])}"
                redirect(action: "list", id: params.id, params: [orderType: orderInstance.orderType])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list", params: [orderType: orderInstance.orderType])
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
        def currentLocation = Location.get(session.warehouse.id)
        def isAccountingRequired = currentLocation?.isAccountingRequired()
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
            render(view: "editAdjustment", model: [orderInstance: orderInstance, orderAdjustment: orderAdjustment, isAccountingRequired: isAccountingRequired])
        }
    }

    def saveAdjustment = {
        def orderInstance = Order.get(params?.order?.id)
        def currentLocation = Location.get(session?.warehouse.id)
        if (orderInstance) {
            if (currentLocation.isAccountingRequired()) {
                OrderAdjustmentType orderAdjustmentType = OrderAdjustmentType.get(params.orderAdjustmentType.id)
                if (!orderAdjustmentType.glAccount) {
                    render(status: 500, text: "${warehouse.message(code: 'orderAdjustment.missingGlAccount.label')}")
                    return
                }
            }
            def orderAdjustment = OrderAdjustment.get(params?.id)
            if (params.budgetCode) {
                params.budgetCode = BudgetCode.get(params.budgetCode)
            }
            if (orderAdjustment) {
                if (orderAdjustment.hasRegularInvoice) {
                    throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
                }
                if (orderAdjustment.orderItem && !params.orderItem.id) {
                    orderAdjustment.orderItem.removeFromOrderAdjustments(orderAdjustment)
                }
                orderAdjustment.properties = params
                if (orderAdjustment.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'orderAdjustment.label', default: 'Order Adjustment'), orderAdjustment.id])}"
                    redirect(controller:"purchaseOrder", action: "addItems", id: orderInstance.id, params:['skipTo': 'adjustments'])
                } else {
                    render(view: "editAdjustment", model: [orderInstance: orderInstance, orderAdjustment: orderAdjustment])
                }
            } else {
                orderAdjustment = new OrderAdjustment(params)
                orderInstance.addToOrderAdjustments(orderAdjustment)
                if (orderInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
                    redirect(controller:"purchaseOrder", action: "addItems", id: orderInstance.id, params:['skipTo': 'adjustments'])
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
            User user = User.get(session?.user?.id)
            def canEdit = orderService.canManageAdjustments(orderInstance, user)
            if(canEdit) {
                def orderAdjustment = OrderAdjustment.get(params?.id)
                if (!orderAdjustment) {
                    flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'orderAdjustment.label', default: 'Order Adjustment'), params.id])}"
                    redirect(action: "show", id: orderInstance?.id)
                } else {
                    if (orderAdjustment.hasRegularInvoice) {
                        throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
                    }
                    orderInstance.removeFromOrderAdjustments(orderAdjustment)
                    orderAdjustment.delete()
                    if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
                        flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
                        redirect(controller:"purchaseOrder", action: "addItems", id: orderInstance.id, params:['skipTo': 'adjustments'])
                    } else {
                        render(view: "show", model: [orderInstance: orderInstance])
                    }
                }
            } else {
                throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
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
                    "${warehouse.message(code: 'orderItem.totalPrice.label')}," +
                    "${warehouse.message(code: 'orderItem.budgetCode.label')}" +
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
                        "${StringEscapeUtils.escapeCsv(quantityString)}," +
                        "${orderItem?.unitOfMeasure}," +
                        "${StringEscapeUtils.escapeCsv(unitPriceString)}," +
                        "${StringEscapeUtils.escapeCsv(totalPriceString)}," +
                        "${orderItem?.budgetCode?.code}," +
                        "\n"
            }

            String totalPriceString = formatNumber(number: totalPrice, maxFractionDigits: 2, minFractionDigits: 2)
            csv += ",,,,,,${StringEscapeUtils.escapeCsv(totalPriceString)}\n"
            render csv

        }
    }

    def orderItemFormDialog = {
        OrderItem orderItem = OrderItem.get(params.id)
        def currentLocation = Location.get(session.warehouse.id)
        def isAccountingRequired = currentLocation?.isAccountingRequired()
        if (!orderService.canOrderItemBeEdited(orderItem, session.user)) {
            throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
        }
        render(template: "orderItemFormDialog",
                model: [orderItem:orderItem, canEdit: orderService.canOrderItemBeEdited(orderItem, session.user), isAccountingRequired: isAccountingRequired])
    }

    def productSourceFormDialog = {
        Product product = Product.get(params.productId)
        Organization supplier = Organization.get(params.supplierId)
        render(template: "productSourceFormDialog", model: [product: product, supplier: supplier])
    }

    def createProductSource = {
        Organization supplier = Organization.get(params.supplier.id)
        ProductSupplier productSupplier = ProductSupplier.findByCodeAndSupplier(params.sourceCode, supplier)
        if (params.sourceCode && productSupplier) {
            render(status: 500, text: "Product source with given code for your supplier already exists")
            return
        }

        try {
            productSupplier = productSupplierDataService.createProductSupplierWithoutPackage(params)
        } catch (Exception e) {
            log.error("Error " + e.message, e)
            render(status: 500, text: "Error creating product source")
        }
        render (status: 200, text: productSupplier.id)
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
            order.save(flush:true)
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
        ValidationCode validationCode = params.validationCode ? params.validationCode as ValidationCode : null
        Location currentLocation = Location.get(session?.warehouse.id)
        if (validationCode == ValidationCode.BLOCK) {
            render(status: 500, text: "${warehouse.message(code: 'orderItem.blockedSupplier.label')}")
            return
        }
        if (params.productSupplier == "Create New") {
            Organization supplier = Organization.get(params.supplier.id)
            productSupplier = ProductSupplier.findByCodeAndSupplier(params.sourceCode, supplier)
            if (params.sourceCode && productSupplier) {
                render(status: 500, text: "Product source with given code for your supplier already exists")
                return
            }
        }
        if (params.productSupplier || params.supplierCode) {
            productSupplier = productSupplierDataService.getOrCreateNew(params, params.productSupplier == "Create New")
        }
        params.remove("productSupplier")
        if (params.budgetCode) {
            params.budgetCode = BudgetCode.get(params.budgetCode)
        }
        if (currentLocation.isAccountingRequired()) {
            Product product = Product.get(params.product.id)
            if (!product.glAccount) {
                render(status: 500, text: "${warehouse.message(code: 'orderItem.missingGlAccount.label')}")
                return
            }
        }
        if (!orderItem) {
            orderItem = new OrderItem(params)
            order.addToOrderItems(orderItem)
        }
        else {
            if (!orderService.canOrderItemBeEdited(orderItem, session.user)) {
                throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
            }
            orderItem.properties = params
            orderItem.refreshPendingShipmentItemRecipients()
        }

        if (productSupplier != null) {
            orderItem.productSupplier = productSupplier
        }

        try {
            if (!order.save(flush:true)) {
                throw new ValidationException("Order is invalid", order.errors)
            }
            if (order.status >= OrderStatus.PLACED) {
                orderService.updateProductPackage(orderItem)
                orderService.updateProductUnitPrice(orderItem)
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
                    manufacturerName: it.productSupplier?.manufacturer?.name,
                    text: it.toString(),
                    orderItemStatusCode: it.orderItemStatusCode.name(),
                    hasShipmentAssociated: it.hasShipmentAssociated(),
                    budgetCode: it.budgetCode,
                    orderIndex: it.orderIndex
            ]
        }
        orderItems = orderItems.sort { a,b -> a.dateCreated <=> b.dateCreated ?: a.orderIndex <=> b.orderIndex }
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
                    "${warehouse.message(code: 'product.sourceName.label')}," + // source name
                    "${warehouse.message(code: 'product.supplierCode.label')}," + // supplier code
                    "${warehouse.message(code: 'product.manufacturer.label')}," + // manufacturer
                    "${warehouse.message(code: 'product.manufacturerCode.label')}," + // manufacturer code
                    "${warehouse.message(code: 'default.quantity.label')}," + // qty
                    "${warehouse.message(code: 'default.unitOfMeasure.label')}," + // UoM
                    "${warehouse.message(code: 'default.cost.label')}," + // unit price
                    "${warehouse.message(code: 'orderItem.totalCost.label')}," + // total cost
                    "${warehouse.message(code: 'order.recipient.label')}," + // recipient
                    "${warehouse.message(code: 'orderItem.estimatedReadyDate.label')}," + // estimated ready date
                    "${warehouse.message(code: 'orderItem.actualReadyDate.label')}," + // actual ready date
                    "${warehouse.message(code: 'orderItem.budgetCode.label')}," +
                    "\n"

            def totalPrice = 0.0

            orderInstance?.listOrderItems()?.each { orderItem ->
                totalPrice += orderItem.totalPrice() ?: 0

                String quantityString = formatNumber(number: orderItem?.quantity, maxFractionDigits: 1, minFractionDigits: 1)
                String unitPriceString = formatNumber(number: orderItem?.unitPrice, maxFractionDigits: 4, minFractionDigits: 2)
                String totalPriceString = formatNumber(number: orderItem?.totalPrice(), maxFractionDigits: 2, minFractionDigits: 2)
                String unitOfMeasure = orderItem?.quantityUom ? "${orderItem?.quantityUom?.code}/${orderItem?.quantityPerUom}" : orderItem?.unitOfMeasure

                csv += "${orderItem?.id}," +
                        "${orderItem?.product?.productCode}," +
                        "${StringEscapeUtils.escapeCsv(orderItem?.product?.name)}," +
                        "${orderItem?.productSupplier?.code ?: ''}," +
                        "${StringEscapeUtils.escapeCsv(orderItem?.productSupplier?.name)}," +
                        "${orderItem?.productSupplier?.supplierCode ?: ''}," +
                        "${orderItem?.productSupplier?.manufacturer?.name ?: ''}," +
                        "${orderItem?.productSupplier?.manufacturerCode ?: ''}," +
                        "${StringEscapeUtils.escapeCsv(quantityString)}," +
                        "${unitOfMeasure}," +
                        "${StringEscapeUtils.escapeCsv(unitPriceString)}," +
                        "${StringEscapeUtils.escapeCsv(totalPriceString)}," +
                        "${orderItem?.recipient?.name ?: ''}," +
                        "${orderItem?.estimatedReadyDate?.format("MM/dd/yyyy") ?: ''}," +
                        "${orderItem?.actualReadyDate?.format("MM/dd/yyyy") ?: ''}," +
                        "${orderItem?.budgetCode?.code ?: ''}," +
                        "\n"
            }
            render csv
        }
    }

    def importOrderItems = {
        def orderInstance = Order.get(params.id)
        Location currentLocation = Location.get(session?.warehouse?.id)
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

                if (orderService.importOrderItems(params.id, params.supplierId, lineItems, currentLocation)) {
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
            Document documentTemplate = Document.findByName("${controllerName}:${actionName}")
            if (documentTemplate) {
                render documentTemplateService.renderGroovyServerPageDocumentTemplate(documentTemplate, [orderInstance:orderInstance])
                return
            }
            [orderInstance: orderInstance]
        }
    }

    def render = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            if (!params?.documentTemplate?.id) {
                throw new IllegalArgumentException("documentTemplate.id is required")
            }
            Document documentTemplate = Document.get(params?.documentTemplate?.id)
            if (documentTemplate) {

                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
                    ConverterTypeTo targetDocumentType = params.format ? params.format as ConverterTypeTo : null
                    documentTemplateService.renderOrderDocumentTemplate(documentTemplate,
                            orderInstance, targetDocumentType, outputStream)

                    // Set response headers appropriately
                    if (targetDocumentType) {

                        // Use the appropriate content type and extension of the conversion type
                        // (except XHTML, just render as HTML response)
                        if (targetDocumentType != ConverterTypeTo.XHTML) {
                            response.setHeader("Content-disposition",
                                    "attachment; filename=\"${documentTemplate.name}\"-${orderInstance.orderNumber}.${targetDocumentType.extension}");
                            response.setContentType(targetDocumentType.mimeType)
                        }
                    }
                    else {

                        // Otherwise write processed document to response using the original
                        // document template's extension and content type
                        response.setHeader("Content-disposition",
                                "attachment; filename=\"${documentTemplate.name}\"-${orderInstance.orderNumber}.${documentTemplate.extension}");
                        response.setContentType(documentTemplate.contentType)
                    }
                    outputStream.writeTo(response.outputStream)
                    return
                } catch (Exception e) {
                    log.error("Unable to render document template ${documentTemplate.name} for order ${orderInstance?.id}", e)
                    throw e;
                }
            }
        }
        [orderInstance:orderInstance]
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

    def exportTemplate = {
        Order order = Order.get(params.order.id)
        def orderItems = OrderItem.findAllByOrder(order)
        if (orderItems) {
            String csv = orderService.exportOrderItems(orderItems)
            response.setHeader("Content-disposition",
                    "attachment; filename=\"PO - ${order.id} - shipment import template.csv\"")
            response.contentType = "text/csv"
            render csv
        } else {
            render(text: 'No order items found', status: 404)
        }
    }

    def cancelOrderItem = {
        OrderItem orderItem = OrderItem.get(params.id)
        def canEdit = orderService.canOrderItemBeEdited(orderItem, session.user)
        if (canEdit) {
            orderItem.orderItemStatusCode = OrderItemStatusCode.CANCELED
            render (status: 200, text: "Item canceled successfully")
        } else {
            throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
        }
    }

    def restoreOrderItem = {
        OrderItem orderItem = OrderItem.get(params.id)
        def canEdit = orderService.canOrderItemBeEdited(orderItem, session.user)
        if (canEdit) {
            orderItem.orderItemStatusCode = OrderItemStatusCode.PENDING
            render(status: 200, text: "Item restored successfully")
        } else {
            throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
        }
    }

    def getTotalPrice = {
        Order order = Order.get(params.id)
        render order.total
    }

    def cancelOrderAdjustment = {
        OrderAdjustment orderAdjustment = OrderAdjustment.get(params.id)
        User user = User.get(session?.user?.id)
        def canEdit = orderService.canManageAdjustments(orderAdjustment.order, user) && !orderAdjustment.hasRegularInvoice
        if(canEdit) {
            orderAdjustment.canceled = true
            render (status: 200, text: "Adjustment canceled successfully")
        } else {
            throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
        }
    }

    def restoreOrderAdjustment = {
        OrderAdjustment orderAdjustment = OrderAdjustment.get(params.id)
        User user = User.get(session?.user?.id)
        def canEdit = orderService.canManageAdjustments(orderAdjustment.order, user)
        if(canEdit) {
            orderAdjustment.canceled = false
            render(status: 200, text: "Adjustment restored successfully")
        } else {
            throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
        }
    }

    def getOrderAdjustments = {
        def orderInstance = Order.get(params.id)
        def orderAdjustments = orderInstance.orderAdjustments.sort { it.dateCreated }.collect {

            [
                    id: it.id,
                    type: it.orderAdjustmentType,
                    description: it.description,
                    orderItem: it.orderItem,
                    percentage: it.percentage,
                    comments: it.comments,
                    budgetCode: it.budgetCode,
                    amount: it.amount ? it.amount : it.percentage ? it.orderItem ? it.orderItem.totalAdjustments : it.totalAdjustments : 0,
                    isCanceled: it.canceled,
                    order: it.order,
            ]
        }
        render orderAdjustments as JSON
    }

    def getTotalAdjustments = {
        Order order = Order.get(params.id)
        render order.totalAdjustments
    }

    def createCombinedShipment = {
        def orderInstance = Order.get(params.orderId)
        if (!orderInstance.orderItems.find {it.quantityRemainingToShip != 0 && it.orderItemStatusCode != OrderItemStatusCode.CANCELED }) {
            flash.message = "${warehouse.message(code:'purchaseOrder.noItemsToShip.label')}"
            redirect(controller: 'order', action: "show", id: orderInstance.id, params: ['tab': 4])
            return
        }
        StockMovement stockMovement = StockMovement.createFromOrder(orderInstance);
        stockMovement = stockMovementService.createShipmentBasedStockMovement(stockMovement)
        redirect(controller: 'stockMovement', action: "createCombinedShipments", params: [direction: 'INBOUND', id: stockMovement.id])
    }

    def orderSummary = {
        params.max = params.max?:10
        params.offset = params.offset?:0
        def orderSummaryList = orderService.getOrderSummaryList(params)
        render(view: "orderSummaryList", model: [orderSummaryList: orderSummaryList ?: []], params: params)
    }
}
