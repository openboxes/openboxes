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
import org.pih.warehouse.product.ProductAttribute
import org.pih.warehouse.product.ProductService
import org.springframework.validation.BeanPropertyBindingResult


@Transactional
class ProductAttributeImportDataService implements ImportDataService {
    ProductService productService

    @Override
    void validateData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            ProductAttribute productAttribute = productService.createOrUpdateProductAttribute(params)
            if (!productAttribute.validate()) {
                productAttribute.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("Row ${index + 1}: Product attribute ${productAttribute} is invalid: ${error.getFieldError()}")
                }
            }
        }
    }

    @Override
    void importData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            ProductAttribute productAttribute = productService.createOrUpdateProductAttribute(params)
            if (productAttribute.validate()) {
                productAttribute.product.save(failOnError: true)
            }
        }
    }


}
