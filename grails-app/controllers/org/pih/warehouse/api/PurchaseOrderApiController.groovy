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
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderSummary
import org.pih.warehouse.order.OrderSummaryStatus

import java.math.RoundingMode

class PurchaseOrderApiController {

    def orderService

    def list() {
        List<OrderSummary> purchaseOrders = orderService.getPurchaseOrders(params)

        if (params.format == 'csv') {
            def csv = params.boolean("orderItems") ? getOrderItemsCsv(purchaseOrders) : getOrdersCsv(purchaseOrders)
            def name = params.boolean("orderItems") ? "OrderLineItems" : "Orders"

            response.setHeader("Content-disposition", "attachment; filename=\"${name}-${new Date().format("MM/dd/yyyy")}.csv\"")
            render(contentType: "text/csv", text: csv.out.toString())
            return
        }
        render([
            data: purchaseOrders,
            totalPrice: purchaseOrders?.sum { it.order.totalNormalized?:0.0 } ?:0.0,
            totalCount: purchaseOrders?.totalCount
        ] as JSON)
     }

    def read() {
        Order order = Order.get(params.id)
        if (!order) {
            def message = "${warehouse.message(code: 'default.not.found.message',args:[warehouse.message(code: 'order.label', default: 'order'), params.id])}"
            render(status: 404, text: message)
        }

        render([data: order] as JSON)
    }

    def delete() {
        def orderInstance = Order.get(params.id)
        if (orderInstance) {
            if (orderInstance.hasPrepaymentInvoice) {
                def message = "${warehouse.message(code: 'order.errors.deletePrepaid.message')}"
                response.status = 400
                render([errorMessages: [message]] as JSON)
                return
            }

            if (orderInstance.status > OrderStatus.PENDING) {
                def message = "${warehouse.message(code: 'order.errors.delete.message')}"
                response.status = 400
                render([errorMessages: [message]] as JSON)
            }

            if (orderInstance.status != OrderStatus.PENDING || !orderInstance.isPurchaseOrder) {
                def message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'order.label', default: 'order'), orderInstance.orderNumber])}"
                response.status = 400
                render([errorMessages: [message]] as JSON)
                return
            }
            try {
                orderService.deleteOrder(orderInstance)
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                def message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.orderNumber])}"
                response.status = 400
                render([errorMessages: [message]] as JSON)
                return
            }
        } else {
            def message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            response.status = 404
            render([errorMessage: message] as JSON)
            return
        }

        render status: 204
    }

    def statusOptions() {
        def options = OrderSummaryStatus.derivedStatuses()?.collect {
            [ id: it.name(), value: it.name(), label: "${g.message(code: 'enum.OrderSummaryStatus.' + it.name())}", variant: it.variant?.name()  ]
        }
        render([data: options] as JSON)
    }

    def rollback() {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            def message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            response.status = 404
            render([errorMessage: message] as JSON)
            return
        }

        orderService.rollbackOrderStatus(params.id)

        render status: 200
    }

    def getOrdersCsv(List<OrderSummary> purchaseOrders) {
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

        purchaseOrders?.each {OrderSummary orderSummary ->
            Order order = orderSummary.order
            Integer lineItemsSize = order?.orderItems?.findAll { OrderItem item -> item.orderItemStatusCode != OrderItemStatusCode.CANCELED }.size() ?: 0
            BigDecimal totalPrice = new BigDecimal(order?.total).setScale(2, RoundingMode.HALF_UP)
            BigDecimal totalPriceNormalized = order?.totalNormalized.setScale(2, RoundingMode.HALF_UP)
            csv.printRecord(
                orderSummary.derivedStatus ?: order.status,
                order?.orderNumber,
                order?.name,
                "${order?.origin?.name} (${order?.origin?.organization?.code})",
                "${order?.destination?.name} (${order?.destination?.organization?.code})",
                order?.orderedBy?.name,
                order?.dateOrdered?.format("MM/dd/yyyy"),
                order?.paymentMethodType?.name,
                order?.paymentTerm?.name,
                lineItemsSize,
                orderSummary.itemsOrdered ?: 0,
                orderSummary.itemsShipped ?: 0,
                orderSummary.itemsReceived ?: 0,
                orderSummary.itemsInvoiced ?: 0,
                order?.currencyCode ?: grailsApplication.config.openboxes.locale.defaultCurrencyCode,
                "${totalPrice} ${order?.currencyCode ?: grailsApplication.config.openboxes.locale.defaultCurrencyCode}",
                "${totalPriceNormalized} ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}",
            )
        }

        return csv
    }

    def getOrderItemsCsv(List<OrderSummary> purchaseOrders) {
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

        purchaseOrders?.each {
            def order = it.order
            order.orderItems*.each { orderItem ->
                csv.printRecord(
                    orderItem?.order?.origin?.organization?.code + " - " + orderItem?.order?.origin?.organization?.name,
                    orderItem?.order?.origin?.name,
                    orderItem?.order?.destination?.name,
                    orderItem?.order?.orderNumber,
                    orderItem?.order?.name,
                    it.derivedStatus ?: order.status,
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
        }

        return csv
    }
}
