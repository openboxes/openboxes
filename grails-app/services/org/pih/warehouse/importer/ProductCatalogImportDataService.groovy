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
import org.pih.warehouse.data.CategoryService
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductCatalogService
import org.springframework.validation.BeanPropertyBindingResult

@Transactional
class ProductCatalogImportDataService implements ImportDataService {
    ProductCatalogService productCatalogService

    void validateData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            ProductCatalog productCatalog = productCatalogService.createOrUpdateProductCatalog(params)
            if (!productCatalog.validate()) {
                productCatalog.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("Row ${index + 1}: Product catalog ${productCatalog.name} is invalid: ${error.getFieldError()}")
                }
            }
            productCatalog.discard()
        }
    }

    void importData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            ProductCatalog productCatalog = productCatalogService.createOrUpdateProductCatalog(params)
            if (productCatalog.validate()) {
                productCatalog.save(failOnError: true)
            }
        }
    }
}
