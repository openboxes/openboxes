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
import org.pih.warehouse.product.ProductCatalog
import org.springframework.validation.BeanPropertyBindingResult

@Transactional
class ProductCatalogImportDataService implements ImportDataService {

    @Override
    void validateData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            ProductCatalog productCatalog = bindProductCatalog(params)
            if (!productCatalog.validate()) {
                productCatalog.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("Row ${index + 1}: Product catalog ${productCatalog.name} is invalid: ${error.getFieldError()}")
                }
            }
            productCatalog.discard()
        }
    }

    @Override
    void importData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            ProductCatalog productCatalog = bindProductCatalog(params)
            if (productCatalog.validate()) {
                productCatalog.save(failOnError: true)
            }
        }
    }

    ProductCatalog bindProductCatalog(Map params) {
        ProductCatalog productCatalog = ProductCatalog.findByIdOrName(params.id, params.name)
        if (!productCatalog) {
            productCatalog = new ProductCatalog()
        }
        productCatalog.properties = params
        return productCatalog
    }
}
