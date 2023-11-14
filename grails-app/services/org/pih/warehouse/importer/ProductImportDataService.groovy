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
import org.pih.warehouse.product.Product

@Transactional
class ProductImportDataService implements ImportDataService {

    @Override
    void validateData(ImportDataCommand command) {
        log.info "validate data test "
        // Iterate over each row and validate values
        command?.data?.each { Map params ->
            log.info "validate data " + params
            params.prompts = [:]
            params.prompts["product.id"] = Product.findAllByActiveAndNameLike(true, "%" + params.search1 + "%")
        }
    }

    @Override
    void importData(ImportDataCommand command) {
        log.info "import data"

        try {
            // Iterate over each row
            command?.data?.each { Map params ->
                log.info "import data " + params
            }
        } catch (Exception e) {
            log.error("Error importing data ", e)
            throw e
        }
    }
}
