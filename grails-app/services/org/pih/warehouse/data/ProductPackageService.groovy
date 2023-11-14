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

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier

@Transactional
class ProductPackageService {

    ProductPackage createOrUpdate(Map params) {
        log.info("params: ${params}")
        Product product = Product.findByProductCode(params.productCode)
        ProductSupplier productSupplier = ProductSupplier.findByCode(params.productSupplierCode)
        UnitOfMeasure unitOfMeasure = UnitOfMeasure.findByCode(params.uomCode)
        ProductPackage productPackage = findOrCreate(params)
        productPackage.properties = params
        if (!productPackage.name) {
            productPackage.name = "${unitOfMeasure.code}/${params.quantity}"
        }
        if (!productPackage.description) {
            productPackage.description = "${unitOfMeasure.name} of ${params.quantity}"
        }
        productPackage.product = product
        productPackage.productSupplier = productSupplier
        productPackage.uom = unitOfMeasure
        return productPackage
    }

    ProductPackage findOrCreate(Map params) {
        ProductPackage productPackage = ProductPackage.get(params.id)

        if (params.id && !productPackage) {
            throw new ValidationException("Unable to find product package with ID ${params.id}")
        }
        if (!productPackage) {
            productPackage = new ProductPackage(params)
        }
        return productPackage
    }
}
