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

import grails.gorm.transactions.Transactional
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionService
import org.springframework.validation.BeanPropertyBindingResult

@Transactional
class OutboundStockMovementImportDataService implements ImportDataService {
    RequisitionService requisitionService

    @Override
    void validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        command.data.eachWithIndex { params, index ->
            if (!params?.origin) {
                throw new IllegalArgumentException("Row ${index + 1}: Origin is required")
            }
            if (!params?.destination) {
                throw new IllegalArgumentException("Row ${index + 1}: Destination is required")
            }
            if (!params?.quantity) {
                throw new IllegalArgumentException("Row ${index + 1}: Requested Quantity is required")
            }
            RequisitionItem requisitionItem = requisitionService.buildRequisitionItem(params)
            if (!requisitionItem.validate()) {
                requisitionItem.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("${index + 1}: ${requisitionItem} ${error.getFieldError()}")
                }
            }
        }
    }

    @Override
    void importData(ImportDataCommand command) {
        log.info "Import data " + command.filename
        command.data.eachWithIndex {params, index ->
            RequisitionItem requisitionItem = requisitionService.buildRequisitionItem(params)
            if (requisitionItem.validate()) {
                requisitionItem.save(failOnError: true)
            }
        }
    }
}
