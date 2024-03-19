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
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product

@Transactional
class PurchaseOrderActualReadyDateImportDataService implements ImportDataService {
    GrailsApplication grailsApplication

    @Override
    void validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename

        command.data.eachWithIndex { params, index ->
            Order order
            OrderItem orderItem
            Product product

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

            // Product validation
            if (!params["productCode"]) {
                command.errors.reject("Row ${index + 1}: 'productCode' is required")
            }
            if (params["orderNumber"]) {
                product = Product.findByProductCode(params["productCode"])
                if (!product) {
                    command.errors.reject("Row ${index + 1}: product with code '${params["productCode"]}' does not exists")
                }
            }
            // Order item validation
            if (!params["orderItemId"]) {
                command.errors.reject("Row ${index + 1}: 'Order item' is required")
            }

            if (params["orderItemId"]) {
                orderItem = OrderItem.get(params['orderItemId'])
                if (!orderItem) {
                    command.errors.reject("Row ${index + 1}: order item with id '${params["orderItemId"]}' does not exists")
                }
            }

            // Actual Ready Date validation
            if (!params["actualReadyDate"]) {
                command.errors.reject("Row ${index + 1}: 'Actual Ready Date' is required")
            }
        }
    }

    @Override
    void importData(ImportDataCommand command) {
        log.info "Import data " + command.filename

    }
}
