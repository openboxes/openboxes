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
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.springframework.validation.BeanPropertyBindingResult

@Transactional
class ProductPackageImportDataService implements ImportDataService {

    @Override
    void validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        command.data.eachWithIndex { params, index ->

            def id = params.id
            def productCode = params.productCode
            def productSupplierCode = params.productSupplierCode
            def uomCode = params.uomCode
            def quantity = new BigDecimal(params.quantity)
            def price = new BigDecimal(params.price)

            Product product = Product.findByProductCode(productCode)
            if (!product) {
                command.errors.reject("Row ${index + 1}: Product with product code ${productCode} does not exist")
            }

            ProductSupplier productSupplier = ProductSupplier.findByCode(productSupplierCode)
            if (productSupplierCode && !productSupplier) {
                command.errors.reject("Row ${index + 1}: Product supplier code ${productSupplierCode} does not exist")
            }

            UnitOfMeasure unitOfMeasure = UnitOfMeasure.findByCode(uomCode)
            if (uomCode && !unitOfMeasure) {
                command.errors.reject("Row ${index + 1}: Unit of measure ${uomCode} does not exist")
            }

            if (id && !ProductPackage.exists(id)) {
                command.errors.reject("Row ${index + 1}: Product package with ID ${id} does not exist")
            }

            // Find an existing product package within the
            ProductPackage existingProductPackage = productSupplier?.productPackages?.find {
                it.uom == unitOfMeasure && it.quantity == quantity.toInteger()
            }
            if (!id && existingProductPackage) {
                command.errors.reject("Row ${index + 1}: Product package with ID ${existingProductPackage} already exists for product=${product.productCode} and uom=${unitOfMeasure.code}/${quantity}. To update this row please copy the primary key '${existingProductPackage?.id}' into ID column in row ${index+1}.")
            }


            ProductPackage productPackage = bindProductPackage(params)
            productPackage.product = product
            productPackage.productSupplier = productSupplier

            if (!productPackage.validate()) {
                productPackage.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("Row ${index + 1}: ${error.getFieldError()}")
                }
            }
            productPackage.discard()
        }
    }

    @Override
    void importData(ImportDataCommand command) {
        log.info "Process data " + command.filename
        command.data.eachWithIndex { params, index ->
            ProductPackage productPackage = bindProductPackage(params)
            if (productPackage.validate()) {
                log.info "Product package ${productPackage.properties}"
                boolean saved = productPackage.save(failOnError: true)
                log.info "Product package ${productPackage.id} saved ${saved}"
            }
        }
    }

    ProductPackage bindProductPackage(Map params) {
        log.info("params: ${params}")

        ProductPackage productPackage = ProductPackage.get(params.id)
        if (!productPackage) {
            productPackage = new ProductPackage();
            productPackage.properties = params
        }
        Product product = Product.findByProductCode(params.productCode)
        ProductSupplier productSupplier = ProductSupplier.findByCode(params.productSupplierCode)
        UnitOfMeasure unitOfMeasure = UnitOfMeasure.findByCode(params.uomCode)

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
}
