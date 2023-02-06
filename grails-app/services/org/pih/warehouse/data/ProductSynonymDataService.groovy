/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.data

import org.pih.warehouse.core.SynonymTypeCode
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product

class ProductSynonymDataService {

    def productService

    Boolean validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename

        command.data.eachWithIndex { params, index ->
            Product product = Product.findByProductCode(params['product.productCode'])
            if (!product) {
                command.errors.reject("Row ${index + 1}: Product with code '${params['product.productCode']}' does not exist")
            }
            try {
                SynonymTypeCode.valueOf(params['synonymTypeCode']);
            } catch (IllegalArgumentException ex) {
                command.errors.reject("Row ${index + 1}: Synonym type code '${params['synonymTypeCode']} does not exist")
            }

        }
    }

    void importData(ImportDataCommand command) {
        log.info "Import data " + command.filename

        command.data.each{ params ->
            Product product = Product.findByProductCode(params['product.productCode'])
            productService.addSynonymToProduct(product.id, params['synonymTypeCode'], params['synonym.name'], params['locale'])
        }
    }

}
