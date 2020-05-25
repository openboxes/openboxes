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

import org.pih.warehouse.core.Organization
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.springframework.validation.BeanPropertyBindingResult

class ProductSupplierDataService {

    def identifierService

    Boolean validate(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        command.data.eachWithIndex { params, index ->

            def id = params.id
            def productCode = params.productCode
            def supplierId = params.supplierId
            def supplierName = params.supplierName
            def manufacturerId = params.manufacturerId
            def manufacturerName = params.manufacturerName

            if (id && !ProductSupplier.exists(id)) {
                command.errors.reject("Row ${index + 1}: Product supplier with ID ${id} does not exist")
            }

            if (!Product.findByProductCode(productCode)) {
                command.errors.reject("Row ${index + 1}: Product with productCode ${productCode} does not exist")
            }

            def supplier = Organization.get(supplierId)
            if (supplier?.name != supplierName) {
                command.errors.reject("Row ${index + 1}: Organization ${supplier?.name} with id ${supplier?.id} does not match ${supplierName}")
            }

            def manufacturer = Organization.get(manufacturerId)
            if (manufacturer?.name != manufacturerName) {
                command.errors.reject("Row ${index + 1}: Organization ${manufacturer?.name} with id ${manufacturer?.id} does not match ${manufacturerName}")
            }

            def productSupplier = createOrUpdate(params)
            if (!productSupplier.validate()) {
                productSupplier.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("Row ${index + 1}: ${error.getFieldError()}")
                }
            }
        }
    }

    void process(ImportDataCommand command) {
        log.info "Process data " + command.filename

        command.data.eachWithIndex { params, index ->
            ProductSupplier productSupplier = createOrUpdate(params)
            if (productSupplier.validate()) {
                productSupplier.save(failOnError: true)
            }
        }
    }

    def createOrUpdate(Map params) {

        log.info("params: ${params}")

        ProductSupplier productSupplier = ProductSupplier.findByIdOrCode(params["id"], params["code"])
        if (!productSupplier) {
            productSupplier = new ProductSupplier(params)
        } else {
            productSupplier.properties = params
        }
        productSupplier.name = params["productName"]
        productSupplier.productCode = params["legacyProductCode"]
        productSupplier.product = Product.findByProductCode(params["productCode"])
        productSupplier.supplier = Organization.get(params["supplierId"])
        productSupplier.manufacturer = Organization.get(params["manufacturerId"])

        if (!productSupplier.code) {
            String prefix = productSupplier?.product?.productCode
            productSupplier.code = identifierService.generateProductSupplierIdentifier(prefix)
        }
        return productSupplier
    }

    def getOrCreateNew(Map params) {
        if (ProductSupplier.get(params.productSupplier.id) != null) {
            return ProductSupplier.get(params.productSupplier.id)
        }

        Product product = Product.get(params.product.id)
        ProductSupplier productSupplier = new ProductSupplier()
        productSupplier.code = params.productSupplier.id?:params.supplierCode
        productSupplier.supplierCode = params.supplierCode
        productSupplier.name = params.supplierCode
        productSupplier.product = product
        productSupplier.manufacturer = Organization.get(params.manufacturer)
        productSupplier.manufacturerCode = params.manufacturerCode
        productSupplier.supplier = Organization.get(params.supplier.id)

        if (productSupplier.validate()) {
            productSupplier.save(failOnError: true)
        }
        return productSupplier
    }
}
