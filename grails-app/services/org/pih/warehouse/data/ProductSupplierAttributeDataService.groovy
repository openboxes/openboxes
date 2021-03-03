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

import org.pih.warehouse.core.EntityTypeCode
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Attribute
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAttribute
import org.pih.warehouse.product.ProductSupplier

class ProductSupplierAttributeDataService {

    Boolean validate(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        command.data.eachWithIndex { params, index ->

            def productCode = params.productCode
            def productSupplierCode = params.productSupplierCode
            def attributeCode = params.attributeCode
            def attributeValue = params.attributeValue
            def unitOfMeasure = params.unitOfMeasure

            // Product code is required
            if (!productCode) {
                command.errors.reject("Row ${index + 1}: Product code is required")
                return
            }

            // Product should exist
            Product product = Product.findByProductCode(productCode)
            if (!product) {
                command.errors.reject("Row ${index + 1}: Product with code ${productCode} does not exist")
                return
            }

            // Product supplier code is required
            if (!productSupplierCode) {
                command.errors.reject("Row ${index + 1}: Product source code is required")
                return
            }

            // Product supplier should exist
            ProductSupplier productSupplier = ProductSupplier.findByCode(productSupplierCode)
            if (!productSupplier) {
                command.errors.reject("Row ${index + 1}: Product source with code ${productSupplierCode} does not exist")
                return
            }

            // Attribute code is required
            if (!attributeCode) {
                command.errors.reject("Row ${index + 1}: Attribute code is required")
                return
            }

            // Attribute should exist
            Attribute attribute = Attribute.findByCode(attributeCode)
            if (!attribute) {
                command.errors.reject("Row ${index + 1}: Attribute with code ${attributeCode} does not exist")
                return
            }

            // Validate that attribute is a product supplier attribute
            if (!attribute.entityTypeCodes.contains(EntityTypeCode.PRODUCT_SUPPLIER)) {
                command.errors.reject("Row ${index + 1}: Attribute for given attribute code ${attributeCode} is not supplier type")
                return
            }

            // Attribute value is required
            if (!attributeValue) {
                command.errors.reject("Row ${index + 1}: Attribute value is required")
                return
            }

            // Validate attribute value against options unless free text allowed
            if (!attribute.allowOther && !attribute.options.contains(attributeValue)) {
                command.errors.reject("Row ${index + 1}: Attribute value ${attributeValue} is not valid option for attribute with code ${attributeCode}")
                return
            }

            // Validate UOM data type matches unless UOM not specified for attribute
            UnitOfMeasure uom = unitOfMeasure ? UnitOfMeasure.findByCode(unitOfMeasure) : null
            if (attribute.unitOfMeasureClass && attribute.unitOfMeasureClass?.type != uom?.uomClass?.type) {
                command.errors.reject("Row ${index + 1}: Unit of Measure ${unitOfMeasure} " +
                    "data type does not match the unit of measure data type for given attribute")
            }
        }
    }

    void process(ImportDataCommand command) {
        log.info "Process data " + command.filename

        command.data.eachWithIndex { params, index ->
            ProductAttribute productAttribute = createOrUpdate(params)
            if (productAttribute.validate()) {
                productAttribute.save(failOnError: true, flush: true)
            }
        }
    }

    def createOrUpdate(Map params) {
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
