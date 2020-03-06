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
import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.springframework.validation.BeanPropertyBindingResult

@Transactional
class ProductSupplierDataService {

    def uomService
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
            def uomCode = params.defaultProductPackageUomCode
            def packageQuantity = params.defaultProductPackageQuantity

            if (id && !ProductSupplier.exists(id)) {
                command.errors.reject("Row ${index + 1}: Product supplier with ID ${id} does not exist")
            }

            def product = Product.findByProductCode(productCode)
            if (productCode && !product) {
                command.errors.reject("Row ${index + 1}: Product with productCode ${productCode} does not exist")
            }

            def supplier = Organization.get(supplierId)
            if (supplier?.name != supplierName) {
                command.errors.reject("Row ${index + 1}: Supplier '${supplier?.name}' with id ${supplier?.id} does not match supplierName '${supplierName}'")
            }

            def manufacturer = Organization.get(manufacturerId)
            if (manufacturer?.name != manufacturerName) {
                command.errors.reject("Row ${index + 1}: Manufacturer '${manufacturer?.name}' with id ${manufacturer?.id} does not match manufacturerName '${manufacturerName}'")
            }

            log.info("uomCode " + uomCode)
            if (uomCode) {
                def unitOfMeasure = UnitOfMeasure.findByCode(uomCode)
                if (!unitOfMeasure) {
                    command.errors.reject("Row ${index + 1}: Unit of measure ${uomCode} does not exist")
                }
                if (unitOfMeasure && !packageQuantity) {
                    command.errors.reject("Row ${index + 1}: Unit of measure ${uomCode} requires a quantity")
                }
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
        Product product = Product.findByProductCode(params["productCode"])
        UnitOfMeasure unitOfMeasure = params.defaultProductPackageUomCode ?
                UnitOfMeasure.findByCode(params.defaultProductPackageUomCode) : null
        BigDecimal price = params.defaultProductPackagePrice ?
                new BigDecimal(params.defaultProductPackagePrice) : null
        Integer quantity = params.defaultProductPackageQuantity as Integer

        ProductSupplier productSupplier = ProductSupplier.findByIdOrCode(params["id"], params["code"])
        if (!productSupplier) {
            productSupplier = new ProductSupplier(params)
        } else {
            productSupplier.properties = params
        }
        productSupplier.name = params["productName"]
        productSupplier.productCode = params["legacyProductCode"]
        productSupplier.product = product
        productSupplier.supplier = Organization.get(params["supplierId"])
        productSupplier.manufacturer = Organization.get(params["manufacturerId"])

        if (unitOfMeasure && quantity) {
            ProductPackage defaultProductPackage =
                    productSupplier.productPackages.find { it.uom == unitOfMeasure && it.quantity == quantity }

            if (!defaultProductPackage) {
                defaultProductPackage = new ProductPackage()
                defaultProductPackage.name = "${unitOfMeasure.code}/${quantity}"
                defaultProductPackage.description = "${unitOfMeasure.name} of ${quantity}"
                defaultProductPackage.product = productSupplier.product
                defaultProductPackage.uom = unitOfMeasure
                defaultProductPackage.quantity = quantity
                defaultProductPackage.price = price
                productSupplier.addToProductPackages(defaultProductPackage)
            } else {
                defaultProductPackage.price = price
            }
        }

        if (!productSupplier.code) {
            String prefix = productSupplier?.product?.productCode
            productSupplier.code = identifierService.generateProductSupplierIdentifier(prefix)
        }
        return productSupplier
    }

    def getOrCreateNew(Map params) {
        def productSupplier = params.productSupplier ? ProductSupplier.get(params.productSupplier) : null
        if (productSupplier) {
            return productSupplier
        }

        return createProductSupplierWithoutPackage(params)
    }

    def createProductSupplierWithoutPackage(Map params) {
        Product product = Product.get(params.product.id)
        ProductSupplier productSupplier = new ProductSupplier()
        productSupplier.code = params.sourceCode?:identifierService.generateProductSupplierIdentifier(product?.productCode)
        productSupplier.name = params.sourceName ?: product?.name
        productSupplier.supplier = Organization.get(params.supplier.id)
        productSupplier.supplierCode = params.supplierCode
        productSupplier.product = product
        productSupplier.manufacturer = Organization.get(params.manufacturer)
        productSupplier.manufacturerCode = params.manufacturerCode

        if (productSupplier.validate()) {
            productSupplier.save(failOnError: true)
        }
        return productSupplier
    }
}
