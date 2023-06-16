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
import grails.plugins.csv.CSVWriter
import grails.validation.ValidationException
import grails.gorm.transactions.Transactional
import org.apache.commons.lang.StringEscapeUtils
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.UomService
import org.pih.warehouse.core.User
import org.pih.warehouse.core.ValidationCode
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.springframework.web.multipart.MultipartFile
import java.math.RoundingMode

@Transactional
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

    def index() {
        redirect(action: "list", params: params)
    }

    def list(OrderCommand command) {

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
        params.max = (params.format || params.downloadOrders) ? null : params.max?:10
        params.offset = (params.format || params.downloadOrders) ? null : params.offset?:0

        def orderTemplate = new Order(params)
        orderTemplate.orderType = orderType

        def orders = orderService.getOrders(orderTemplate, statusStartDate, statusEndDate, params)

        def ordersDerivedStatus
        if (params.format || params.downloadOrders) {
            def orderIds = orders?.collect { it?.id }
            ordersDerivedStatus = orderService.getOrdersDerivedStatus(orderIds)
        }

        if (params.format && orders) {
            def csv = CSVUtils.getCSVPrinter()
            csv.printRecord(
                    "Supplier organization",
                    "Supplier location",
                    "Destination",
                    "PO Number",
                    "PO Description",
                    "PO Status",
                    "Code",
                    "Product",
                    "Item Status",
                    "Source Code",
                    "Supplier Code",
                    "Manufacturer",
                    "Manufacturer Code",
                    "Unit of Measure",
                    "Qty per UOM",
                    "Quantity Ordered",
                    "Quantity Shipped",
                    "Quantity Received",
                    "Quantity Invoiced",
                    "Unit Price",
                    "Total Cost",
                    "Currency",
                    "Recipient",
                    "Estimated Ready Date",
                    "Actual Ready Date",
                    "Budget Code"
            )

            orders*.orderItems*.each { orderItem ->
                csv.printRecord(
                        orderItem?.order?.origin?.organization?.code + " - " + orderItem?.order?.origin?.organization?.name,
                        orderItem?.order?.origin?.name,
                        orderItem?.order?.destination?.name,
                        orderItem?.order?.orderNumber,
                        orderItem?.order?.name,
                        (ordersDerivedStatus && orderItem?.order?.id ? ordersDerivedStatus[orderItem.order.id] : ''),
                        orderItem?.product?.productCode,
                        orderItem?.product?.name,
                        OrderItemStatusCode.CANCELED == orderItem?.orderItemStatusCode ? orderItem?.orderItemStatusCode?.name() : '',
                        orderItem?.productSupplier?.code,
                        orderItem?.productSupplier?.supplierCode,
                        orderItem?.productSupplier?.manufacturer?.name,
                        orderItem?.productSupplier?.manufacturerCode,
                        orderItem?.quantityUom?.code,
                        orderItem?.quantityPerUom,
                        orderItem?.quantity,
                        orderItem?.quantityShipped,
                        orderItem?.quantityReceived,
                        orderItem?.quantityInvoicedInStandardUom,
                        orderItem?.unitPrice,
                        orderItem?.total,
                        orderItem?.order?.currencyCode,
                        orderItem?.recipient,
                        orderItem?.estimatedReadyDate?.format("MM/dd/yyyy"),
                        orderItem?.actualReadyDate?.format("MM/dd/yyyy"),
                        orderItem?.budgetCode?.code,
                )
            }

            response.setHeader("Content-disposition", "attachment; filename=\"OrdersLineItems-${new Date().format("MM/dd/yyyy")}.csv\"")
            render(contentType: "text/csv", text: csv.out.toString())
        }

        if (params.downloadOrders && orders) {
            def csv = CSVUtils.getCSVPrinter()
            csv.printRecord(
                    "Status",
                    "PO Number",
                    "Name",
                    "Supplier",
                    "Destination name",
                    "Ordered by",
                    "Ordered on",
                    "Payment method",
                    "Payment terms",
                    "Line items",
                    "Ordered",
                    "Shipped",
                    "Received",
                    "Invoiced",
                    "Currency code",
                    "Total Amount (Local Currency)",
                    "Total Amount (Default Currency)"
            )

            orders.each { order ->
                Integer lineItemsSize = order?.orderItems?.findAll { it.orderItemStatusCode != OrderItemStatusCode.CANCELED }.size() ?: 0
                BigDecimal totalPrice = new BigDecimal(order?.total).setScale(2, RoundingMode.HALF_UP)
                BigDecimal totalPriceNormalized = order?.totalNormalized.setScale(2, RoundingMode.HALF_UP)
                csv.printRecord(
                        (ordersDerivedStatus && order.id ? ordersDerivedStatus[order.id] : ''),
                        order?.orderNumber,
                        order?.name,
                        "${order?.origin?.name} (${order?.origin?.organization?.code})",
                        "${order?.destination?.name} (${order?.destination?.organization?.code})",
                        order?.orderedBy?.name,
                        order?.dateOrdered?.format("MM/dd/yyyy"),
                        order?.paymentMethodType?.name,
                        order?.paymentTerm?.name,
                        lineItemsSize,
                        order?.orderedOrderItems?.size() ?: 0,
                        order?.shippedOrderItems?.size() ?: 0,
                        order?.receivedOrderItems?.size() ?: 0,
                        order?.invoiceItems?.size() ?: 0,
                        order?.currencyCode ?: grailsApplication.config.openboxes.locale.defaultCurrencyCode,
                        "${totalPrice} ${order?.currencyCode ?: grailsApplication.config.openboxes.locale.defaultCurrencyCode}",
                        "${totalPriceNormalized} ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}",
                )
            }

            response.setHeader("Content-disposition", "attachment; filename=\"Orders-${new Date().format("MM/dd/yyyy")}.csv\"")
            render(contentType: "text/csv", text: csv.out.toString())
            return
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

    def listOrderItems() {
        def orderItems = OrderItem.getAll().findAll { !it.isCompletelyFulfilled() }
        return [orderItems: orderItems]
    }

    def create() {
        redirect(controller: 'purchaseOrder', action: 'index')
    }

    def save() {
        def orderInstance = new Order(params)
        if (orderInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
            redirect(action: "list", id: orderInstance.id, params: [orderType: orderInstance.orderType])
        } else {
            render(view: "create", model: [orderInstance: orderInstance])
        }
    }

    def show() {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            [orderInstance: orderInstance]
        }
    }

    def edit() {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            return [orderInstance: orderInstance]
        }
    }

    def placeOrder() {
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


    def update() {
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


    def remove() {
        def orderInstance = Order.get(params.id)
        if (orderInstance) {
            if (orderInstance.hasPrepaymentInvoice) {
                flash.message = "${warehouse.message(code: 'order.errors.deletePrepaid.message')}"
                redirect(action: "show", id: orderInstance?.id)
                return
            }

            if (orderInstance.status == OrderStatus.PENDING) {
                try {
                    orderService.deleteOrder(orderInstance)
                    flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.orderNumber])}"
                } catch (org.springframework.dao.DataIntegrityViolationException e) {
                    flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.orderNumber])}"
                }
            } else {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.orderNumber])}"
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
        }

        if (orderInstance.orderType?.code == OrderTypeCode.PURCHASE_ORDER.name()) {
            redirect(controller: "purchaseOrder", action: "list")
        } else if (orderInstance.orderType.code == Constants.PUTAWAY_ORDER) {
            redirect(controller: "order", action: "list", params: [orderType: Constants.PUTAWAY_ORDER, status: OrderStatus.PENDING])
        } else {
            redirect(controller: "order", action: "list")
        }
    }

    def addAdjustment() {
        def orderInstance = Order.get(params?.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            render(view: "editAdjustment", model: [orderInstance: orderInstance, orderAdjustment: new OrderAdjustment()])
        }
    }

    def editAdjustment() {
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

    def saveAdjustment() {
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

    def deleteAdjustment() {
        User user = User.get(session?.user?.id)

        OrderAdjustment orderAdjustment = OrderAdjustment.get(params?.id)
        if (!orderAdjustment) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'orderAdjustment.label', default: 'Order Adjustment'), params.id])}"
            redirect(action: "show", id: params.order.id)
        }

        orderService.deleteAdjustment(orderAdjustment, user)

        flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.order.id])}"
        redirect(controller:"purchaseOrder", action: "addItems", id: params.order.id, params:['skipTo': 'adjustments'])

    }



    def addComment() {
        def orderInstance = Order.get(params?.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            return [orderInstance: orderInstance, commentInstance: new Comment()]
        }
    }

    def editComment() {
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

    def deleteComment() {
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

    def saveComment() {
        log.info("params " + params)

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

    def addDocument() {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            return [orderInstance: orderInstance]
        }
    }

    def editDocument() {
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

    def deleteDocument() {
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

    def receive() {
        def orderCommand = orderService.getOrder(params.id, session.user.id)
        if (!orderCommand.order) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            return [orderCommand: orderCommand]
        }
    }

    def saveOrderShipment(OrderCommand command) {
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


    def fulfill() {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            return [orderInstance: orderInstance]
        }
    }

    def addOrderItemToShipment() {

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

    def download() {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")

        } else {
            def date = new Date()
            response.setHeader("Content-disposition", "attachment; filename=\"${orderInstance?.orderNumber?.encodeAsHTML()}-${date.format("MM-dd-yyyy")}.csv\"")
            response.contentType = "text/csv"

            def csv = CSVUtils.getCSVPrinter()
            csv.printRecord("PO Number", orderInstance?.orderNumber)
            csv.printRecord("Description", orderInstance?.name)
            csv.printRecord("Vendor", orderInstance?.origin.name)
            csv.printRecord("Ship to", orderInstance?.destination?.name)
            csv.printRecord("Ordered by", "${orderInstance?.orderedBy?.name} ${orderInstance?.orderedBy?.email}")
            csv.println()  // print a newline between text (above) and column headers (immediately following)
            csv.printRecord(
                warehouse.message(code: 'product.productCode.label'),
                warehouse.message(code: 'product.name.label'),
                warehouse.message(code: 'product.supplierCode.label'),
                warehouse.message(code: 'product.manufacturerCode.label'),
                warehouse.message(code: 'orderItem.quantity.label'),
                warehouse.message(code: 'product.unitOfMeasure.label'),
                warehouse.message(code: 'orderItem.unitPrice.label'),
                warehouse.message(code: 'orderItem.totalPrice.label'),
                warehouse.message(code: 'orderItem.budgetCode.label')
            )

            def totalPrice = 0.0

            String lastCurrencyCode = null
            orderInstance?.listOrderItems()?.each { orderItem ->
                totalPrice += orderItem.totalPrice() ?: 0
                if (orderItem?.currencyCode != null) {
                    lastCurrencyCode = orderItem?.currencyCode
                }

                csv.printRecord(
                    orderItem?.product?.productCode,
                    orderItem?.product?.name,
                    orderItem?.productSupplier?.supplierCode,
                    orderItem?.productSupplier?.manufacturerCode,
                    CSVUtils.formatInteger(number: orderItem?.quantity),
                    CSVUtils.formatUnitOfMeasure(orderItem?.quantityUom?.code, orderItem?.quantityPerUom),
                    CSVUtils.formatCurrency(number: orderItem?.unitPrice, currencyCode: orderItem?.currencyCode, isUnitPrice: true),
                    CSVUtils.formatCurrency(number: orderItem?.totalPrice(), currencyCode: orderItem?.currencyCode),
                    orderItem?.budgetCode?.code
                )
            }

            csv.printRecord(null, null, null, null, null, null, null, CSVUtils.formatCurrency(number: totalPrice, currencyCode: lastCurrencyCode), null)
            render(contentType: "text/csv", text: csv.out.toString())
            return
        }
    }

    def orderItemFormDialog() {
        OrderItem orderItem = OrderItem.get(params.id)
        def currentLocation = Location.get(session.warehouse.id)
        def isAccountingRequired = currentLocation?.isAccountingRequired()
        if (!orderService.canOrderItemBeEdited(orderItem, session.user)) {
            throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
        }
        render(template: "orderItemFormDialog",
                model: [orderItem:orderItem, canEdit: orderService.canOrderItemBeEdited(orderItem, session.user), isAccountingRequired: isAccountingRequired])
    }

    def productSourceFormDialog() {
        Product product = Product.get(params.productId)
        Organization supplier = Organization.get(params.supplierId)
        render(template: "productSourceFormDialog", model: [product: product, supplier: supplier])
    }

    def createProductSource() {
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

    def removeOrderItem() {
        User user = User.get(session?.user?.id)

        OrderItem orderItem = OrderItem.get(params.id)
        if (!orderItem) {
            render (status: 404, text: "Unable to locate order item")
        }

        orderService.removeOrderItem(orderItem, user)

        render (status: 200, text: "Successfully deleted order item")
    }

    def saveOrderItem() {
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

        if (!order.save(flush:true)) {
            throw new ValidationException("Order is invalid", order.errors)
        }

        try {
            if (order.status >= OrderStatus.PLACED) {
                orderService.updateProductPackage(orderItem)
                orderService.updateProductUnitPrice(orderItem)
            }
        } catch (Exception e) {
            log.error("Error " + e.message, e)
            render(status: 500, text: "Not saved")
            return
        }

        render (status: 200, text: "Successfully added order item")
    }

    def getOrderItems() {
        def orderInstance = Order.get(params.id)
        def orderItems = orderInstance.orderItems.collect {
            [
                    id: it.id,
                    product: it.product,
                    quantity: it.quantity,
                    quantityUom: it?.quantityUom?.code,
                    quantityPerUom: it?.quantityPerUom,
                    unitOfMeasure: it?.unitOfMeasure,
                    totalQuantity: (it?.quantity?:1) * (it?.quantityPerUom?:1),
                    productPackage: it?.productPackage,
                    currencyCode: it?.order?.currencyCode,
                    unitPrice: CSVUtils.formatCurrency(number: it.unitPrice, currencyCode: it.currencyCode, isUnitPrice: true),
                    totalPrice: CSVUtils.formatCurrency(number: it.totalPrice(), currencyCode: it.currencyCode),
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


    def downloadOrderItems() {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            def date = new Date()
            response.setHeader("Content-disposition", "attachment; filename=\"${orderInstance.orderNumber}-${date.format("MM-dd-yyyy")}.csv\"")
            response.contentType = "text/csv"

            def csv = CSVUtils.getCSVPrinter()
            csv.printRecord(
                    warehouse.message(code: 'orderItem.id.label'),
                    warehouse.message(code: 'product.productCode.label'),
                    warehouse.message(code: 'product.name.label'),
                    warehouse.message(code: 'product.sourceCode.label'),
                    warehouse.message(code: 'product.sourceName.label'),
                    warehouse.message(code: 'product.supplierCode.label'),
                    warehouse.message(code: 'product.manufacturer.label'),
                    warehouse.message(code: 'product.manufacturerCode.label'),
                    warehouse.message(code: 'default.quantity.label'),
                    warehouse.message(code: 'default.unitOfMeasure.label'),
                    warehouse.message(code: 'default.cost.label'),
                    warehouse.message(code: 'orderItem.totalCost.label'),
                    warehouse.message(code: 'order.recipient.label'),
                    warehouse.message(code: 'orderItem.estimatedReadyDate.label'),
                    warehouse.message(code: 'orderItem.actualReadyDate.label'),
                    warehouse.message(code: 'orderItem.budgetCode.label')
            )

            orderInstance?.listOrderItems()?.each { orderItem ->
                csv.printRecord(
                        orderItem?.id,
                        orderItem?.product?.productCode,
                        orderItem?.product?.name,
                        orderItem?.productSupplier?.code,
                        orderItem?.productSupplier?.name,
                        orderItem?.productSupplier?.supplierCode,
                        orderItem?.productSupplier?.manufacturer?.name,
                        orderItem?.productSupplier?.manufacturerCode,
                        CSVUtils.formatInteger(number: orderItem?.quantity),
                        orderItem?.unitOfMeasure,
                        CSVUtils.formatCurrency(number: orderItem?.unitPrice, currencyCode: orderItem?.currencyCode, isUnitPrice: true),
                        CSVUtils.formatCurrency(number: orderItem?.totalPrice(), currencyCode: orderItem?.currencyCode),
                        orderItem?.recipient?.name,
                        orderItem?.estimatedReadyDate?.format("MM/dd/yyyy"),
                        orderItem?.actualReadyDate?.format("MM/dd/yyyy"),
                        orderItem?.budgetCode?.code
                )
            }
            render(contentType: "text/csv", text: csv.out.toString())
        }
    }

    def importOrderItems() {
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

                if (orderService.importOrderItems(params.id, params.supplierId, lineItems, currentLocation, session.user)) {
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


    def upload() {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            return [orderInstance: orderInstance]
        }
    }


    def print() {
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

    def render() {
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

    def rollbackOrderStatus() {

        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        } else {
            orderService.rollbackOrderStatus(params.id)

        }
        redirect(action: "show", id: params.id)

    }

    def exportTemplate() {
        Order order = Order.get(params.order.id)
        def orderItems = OrderItem.findAllByOrder(order)
        if (orderItems) {
            String csv = orderService.exportOrderItems(orderItems)
            response.setHeader("Content-disposition",
                    "attachment; filename=\"PO - ${order.id} - shipment import template.csv\"")
            response.contentType = "text/csv"
            render(contentType: "text/csv", text: csv)
        } else {
            render(text: 'No order items found', status: 404)
        }
    }

    def cancelOrderItem() {
        OrderItem orderItem = OrderItem.get(params.id)
        def canEdit = orderService.canOrderItemBeEdited(orderItem, session.user)
        if (canEdit) {
            orderItem.orderItemStatusCode = OrderItemStatusCode.CANCELED
            orderItem.disableRefresh = false
            render (status: 200, text: "Item canceled successfully")
        } else {
            throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
        }
    }

    def restoreOrderItem() {
        OrderItem orderItem = OrderItem.get(params.id)
        def canEdit = orderService.canOrderItemBeEdited(orderItem, session.user)
        if (canEdit) {
            orderItem.orderItemStatusCode = OrderItemStatusCode.PENDING
            orderItem.disableRefresh = false
            render(status: 200, text: "Item restored successfully")
        } else {
            throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
        }
    }

    def getTotalPrice() {
        Order order = Order.get(params.id)
        render order.total
    }

    def cancelOrderAdjustment() {
        OrderAdjustment orderAdjustment = OrderAdjustment.get(params.id)
        User user = User.get(session?.user?.id)
        def canEdit = orderService.canManageAdjustments(orderAdjustment.order, user) && !orderAdjustment.hasRegularInvoice
        if(canEdit) {
            orderAdjustment.canceled = true
            orderAdjustment.disableRefresh = false
            render (status: 200, text: "Adjustment canceled successfully")
        } else {
            throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
        }
    }

    def restoreOrderAdjustment() {
        OrderAdjustment orderAdjustment = OrderAdjustment.get(params.id)
        User user = User.get(session?.user?.id)
        def canEdit = orderService.canManageAdjustments(orderAdjustment.order, user)
        if(canEdit) {
            orderAdjustment.canceled = false
            orderAdjustment.disableRefresh = false
            render(status: 200, text: "Adjustment restored successfully")
        } else {
            throw new UnsupportedOperationException("${warehouse.message(code: 'errors.noPermissions.label')}")
        }
    }

    def getOrderAdjustments() {
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

    def getTotalAdjustments() {
        Order order = Order.get(params.id)
        render order.totalAdjustments
    }

    def createCombinedShipment() {
        def orderInstance = Order.get(params.orderId)
        Location currentLocation = Location.get(session.warehouse.id)

        if (!(orderInstance.destination.equals(currentLocation) || currentLocation.supports(ActivityCode.ENABLE_CENTRAL_PURCHASING))) {
            flash.message = "${warehouse.message(code:'order.cantShipFromDifferentLocation.label')}"
            redirect(controller: 'order', action: "show", id: orderInstance.id)
            return
        }
        if (!orderInstance.orderItems.find {it.quantityRemainingToShip != 0 && it.orderItemStatusCode != OrderItemStatusCode.CANCELED }) {
            flash.message = "${warehouse.message(code:'purchaseOrder.noItemsToShip.label')}"
            redirect(controller: 'order', action: "show", id: orderInstance.id, params: ['tab': 4])
            return
        }
        StockMovement stockMovement = StockMovement.createFromOrder(orderInstance);
        stockMovement = stockMovementService.createShipmentBasedStockMovement(stockMovement)
        redirect(controller: 'stockMovement', action: "createCombinedShipments", params: [direction: 'INBOUND', id: stockMovement.id])
    }

    def orderSummary() {
        params.max = params.max?:10
        params.offset = params.offset?:0
        def orderSummaryList = orderService.getOrderSummaryList(params)
        render(view: "orderSummaryList", model: [orderSummaryList: orderSummaryList ?: []], params: params)
    }

    // For testing order item derived status feature. orderItemSummary action gets the data from extended SQL view
    def orderItemSummary() {
        params.max = params.max?:10
        params.offset = params.offset?:0
        def orderItemSummaryList = orderService.getOrderItemSummaryList(params)
        render(view: "orderItemSummaryList", model: [orderItemSummaryList: orderItemSummaryList ?: [], actionName: "orderItemSummary"], params: params)
    }

    // For testing order item derived status feature. orderItemDetails action gets the data from simplified SQL view
    def orderItemDetails() {
        params.max = params.max?:10
        params.offset = params.offset?:0
        def orderItemDetailsList = orderService.getOrderItemDetailsList(params)
        render(view: "orderItemSummaryList", model: [orderItemSummaryList: orderItemDetailsList ?: []], actionName: "orderItemDetails", params: params)
    }
}
