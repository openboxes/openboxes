/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.importer

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import org.pih.warehouse.core.Constants
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderService
import org.pih.warehouse.product.Product

import java.text.ParseException

@Transactional
class PurchaseOrderActualReadyDateImportDataService implements ImportDataService {
    GrailsApplication grailsApplication
    OrderService orderService

    @Override
    void validateData(ImportDataCommand command) {
        log.info "Validate data ${command.filename}"

        command.data.eachWithIndex { params, index ->
            Order order
            OrderItem orderItem
            Product product

            // Product validation
            if (!params["productCode"]) {
                command.errors.reject("Row ${index + 1}: 'productCode' is required")
            }
            if (params["productCode"]) {
                product = Product.findByProductCode(params["productCode"])
                if (!product) {
                    command.errors.reject("Row ${index + 1}: product with code '${params["productCode"]}' does not exists")
                }
            }

            // Order Number validation
            if (!params["orderNumber"]) {
                command.errors.reject("Row ${index + 1}: 'orderNumber' is required")
            }
            if (params["orderNumber"]) {
                order = Order.findByOrderNumber(params["orderNumber"])
                if (!order) {
                    command.errors.reject("Row ${index + 1}: order with Order Number '${params["orderNumber"]}' does not exists")
                }
            }

            // Order item validation
            if (params["orderItemId"]) {
                orderItem = OrderItem.get(params['orderItemId'])
                if (!orderItem) {
                    command.errors.reject("Row ${index + 1}: order item with id '${params["orderItemId"]}' does not exists")
                }
            }

            if (!params["orderItemId"] && order && product) {
                List<OrderItem> matchedOrderItems = OrderItem.findAllByOrderAndProduct(order, product)
                if (matchedOrderItems.size() > 1) {
                    command.errors.reject("""
                        Row ${index + 1}: there are multiple order items with product '${product.productCode}' on order ${order.id}.
                        To differentiate between multiple order items sharing the same product within the same order, please include the order item id to each entry.
                    """)
                }
                if (!matchedOrderItems) {
                    command.errors.reject("Row ${index + 1}: Order Item with product ${params["productCode"]} does not exist on order '${params["orderNumber"]}'")
                }
                if (matchedOrderItems.size() == 1) {
                    orderItem = matchedOrderItems.first();
                }
            }

            // Validate that order matches order item
            if (orderItem && order && orderItem.order.id != order.id) {
                command.errors.reject("Row ${index + 1}: Order Number '${params["orderNumber"]}' is incorrect for order item ${orderItem.id}")
            }

            // Validate that product matches order item
            if (orderItem && product && orderItem.product.id != product.id) {
                command.errors.reject("Row ${index + 1}: Product code '${params["productCode"]}' is incorrect for order item ${orderItem.id}")
            }


            // Actual Ready Date validation
            if (!params["actualReadyDate"]) {
                command.errors.reject("Row ${index + 1}: 'Actual Ready Date' is required")
            }
            if (params["actualReadyDate"]) {
                try {
                    Date.parse(Constants.EXPIRATION_DATE_FORMAT, params["actualReadyDate"])
                } catch(ParseException) {
                    command.errors.reject("Row ${index + 1}: Could not parse date ${params['actualReadyDate']} on 'Actual Ready Date'. Expected date format: ${Constants.EXPIRATION_DATE_FORMAT}")
                }
            }

        }
    }

    @Override
    void importData(ImportDataCommand command) {
        log.info "Import data ${command.filename}"

        command.data.each { params ->

            OrderItem orderItem = params['orderItemId'] ? OrderItem.get(params['orderItemId']) : null

            if (!orderItem) {
                orderItem = orderService.getOrderItemByOrderAndProduct(params['orderNumber'], params['productCode'])
            }

            if (!orderItem) {
                throw new IllegalArgumentException("Order Item is not found")
            }

            orderItem.actualReadyDate = Date.parse(Constants.EXPIRATION_DATE_FORMAT, params["actualReadyDate"])
            orderItem.save(failOnError: true)
        }
    }
}
