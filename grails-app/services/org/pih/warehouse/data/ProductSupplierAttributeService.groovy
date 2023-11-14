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
import org.pih.warehouse.core.EntityTypeCode
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Attribute
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAttribute
import org.pih.warehouse.product.ProductSupplier

@Transactional
class ProductSupplierAttributeService {
    ProductAttribute createOrUpdate(Map params) {
        def productCode = params.productCode
        def productSupplierCode = params.productSupplierCode
        def attributeCode = params.attributeCode
        def attributeValue = params.attributeValue
        def unitOfMeasure = params.unitOfMeasure

        Product product = Product.findByProductCode(productCode)
        ProductSupplier productSupplier = ProductSupplier.findByCode(productSupplierCode)
        Attribute attribute = Attribute.findByCode(attributeCode)
        ProductAttribute productAttribute = ProductAttribute.findByAttributeAndProductSupplier(attribute, productSupplier)
        UnitOfMeasure uom = unitOfMeasure ? UnitOfMeasure.findByCode(unitOfMeasure) : null

        if (productAttribute) {
            productAttribute.value = attributeValue
            productAttribute.unitOfMeasure = uom
        } else {
            productAttribute = new ProductAttribute()
            productAttribute.attribute = attribute
            productAttribute.productSupplier = productSupplier
            productAttribute.value = attributeValue
            productAttribute.unitOfMeasure = uom
            product.addToAttributes(productAttribute)
        }
        return productAttribute
    }
}
