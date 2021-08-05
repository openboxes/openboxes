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

import groovy.sql.Sql
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.core.ProductPrice
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierPreference

import java.text.SimpleDateFormat

class ProductSupplierDataService {

    def uomService
    def identifierService
    def dataSource
    def grailsApplication

    Boolean validate(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        command.data.eachWithIndex { params, index ->

            def productCode = params.productCode
            def supplierName = params.supplierName
            def manufacturerName = params.manufacturerName
            def preferenceType = params.globalPreferenceTypeName
            def uomCode = params.defaultProductPackageUomCode
            def packageQuantity = params.defaultProductPackageQuantity
            def validityStartDate = params.globalPreferenceTypeValidityStartDate
            def validityEndDate = params.globalPreferenceTypeValidityEndDate
            def contractPriceValidUntil = params.contractPriceValidUntil

            if (!params.name) {
                command.errors.reject("Row ${index + 1}: Product Source Name is required")
            }

            if (!productCode) {
                command.errors.reject("Row ${index + 1}: Product Code is required")
            } else if (productCode && !Product.findByProductCode(productCode)) {
                command.errors.reject("Row ${index + 1}: Product with productCode ${productCode} does not exist")
            }

            if (supplierName && !Organization.findByName(supplierName)) {
                command.errors.reject("Row ${index + 1}: Supplier with name '${supplierName}' does not exist")
            }

            if (manufacturerName && !Organization.findByName(manufacturerName)) {
                command.errors.reject("Row ${index + 1}: Manufacturer with name '${manufacturerName}' does not exist")
            }

            try {
                def ratingTypeCode = params?.ratingTypeCode ? params?.ratingTypeCode?.toUpperCase() as RatingTypeCode : null
                if (ratingTypeCode && !RatingTypeCode.inList(ratingTypeCode)) {
                    command.errors.reject("Row ${index + 1}: Rating Type with value '${params.ratingTypeCode}' exists but is not valid.")
                }
            }
            catch(IllegalArgumentException e) {
                command.errors.reject("Row ${index + 1}: Rating Type with value '${params.ratingTypeCode}' does not exist. " + e.message)
            }


            if (preferenceType && !PreferenceType.findByName(preferenceType)) {
                command.errors.reject("Row ${index + 1}: Preference Type with name '${preferenceType}' does not exist")
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
                if (unitOfMeasure && packageQuantity && packageQuantity % 1 != 0) {
                    command.errors.reject("Row ${index + 1}: Unit of measure quntity must be a whole number")
                }
            }

            def minDate = grailsApplication.config.openboxes.expirationDate.minValue
            def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
            if (validityStartDate) {
                try {
                    def startDate = dateFormat.parse(validityStartDate)

                    if (minDate > startDate) {
                        command.errors.reject("Row ${index + 1}: Validity start date ${validityStartDate} is invalid. Please enter a date after ${minDate.getYear()+1900}.")
                    }
                } catch (Exception e) {
                    command.errors.reject("Row ${index + 1}: Validity start date ${validityStartDate} is invalid. "+ e.message)
                }
            }

            if (validityEndDate) {
                try {
                    def endDate = dateFormat.parse(validityEndDate)

                    if (minDate > endDate) {
                        command.errors.reject("Row ${index + 1}: Validity start date ${validityEndDate} is invalid. Please enter a date after ${minDate.getYear()+1900}.")
                    }
                } catch (Exception e) {
                    command.errors.reject("Row ${index + 1}: Validity end date ${validityEndDate} is invalid. " + e.message)
                }
            }

            if (contractPriceValidUntil) {
                try {
                    def validUntilDate = dateFormat.parse(contractPriceValidUntil)

                    if (minDate > validUntilDate) {
                        command.errors.reject("Row ${index + 1}: Contract Price Valid Until date ${contractPriceValidUntil} is invalid. Please enter a date after ${minDate.getYear()+1900}.")
                    }
                } catch (Exception e) {
                    command.errors.reject("Row ${index + 1}: Contract Price Valid Until date ${contractPriceValidUntil} is invalid. " + e.message)
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

        def productCode = params.productCode
        def supplierName = params.supplierName
        def manufacturerName = params.manufacturerName
        def ratingTypeCode = params?.ratingTypeCode ? params?.ratingTypeCode?.toUpperCase() as RatingTypeCode : null
        def supplierCode = params.supplierCode
        def manufacturerCode = params.manufacturerCode

        Product product = productCode ? Product.findByProductCode(productCode) : null
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

        productSupplier.ratingTypeCode = ratingTypeCode
        productSupplier.productCode = params["legacyProductCode"]
        productSupplier.product = product
        productSupplier.supplier = supplierName ? Organization.findByName(supplierName) : null
        productSupplier.manufacturer = manufacturerName ? Organization.findByName(manufacturerName) : null
        productSupplier.supplierCode = supplierCode ? supplierCode : null
        productSupplier.manufacturerCode = manufacturerCode ? manufacturerCode : null

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
                ProductPrice productPrice = new ProductPrice()
                productPrice.price = price
                defaultProductPackage.productPrice = productPrice
                productSupplier.addToProductPackages(defaultProductPackage)
            } else if (price && !defaultProductPackage.productPrice) {
                ProductPrice productPrice = new ProductPrice()
                productPrice.price = price
                defaultProductPackage.productPrice = productPrice
            } else if (price && defaultProductPackage.productPrice) {
                defaultProductPackage.productPrice.price = price
            }
        }

        def dateFormat = new SimpleDateFormat("MM/dd/yyyy")

        def contractPriceValidUntil = params.contractPriceValidUntil ? dateFormat.parse(params.contractPriceValidUntil) : null
        BigDecimal contractPricePrice = params.contractPricePrice ? new BigDecimal(params.contractPricePrice) : null

        if (contractPricePrice) {
            if (!productSupplier.contractPrice) {
                productSupplier.contractPrice = new ProductPrice()
            }

            productSupplier.contractPrice.price = contractPricePrice

            if (contractPriceValidUntil) {
                productSupplier.contractPrice.toDate = contractPriceValidUntil
            }
        }

        PreferenceType preferenceType = params.globalPreferenceTypeName ? PreferenceType.findByName(params.globalPreferenceTypeName) : null

        if (preferenceType) {
            ProductSupplierPreference productSupplierPreference = productSupplier.getGlobalProductSupplierPreference()

            if (!productSupplierPreference) {
                productSupplierPreference = new ProductSupplierPreference()
                productSupplier.addToProductSupplierPreferences(productSupplierPreference)
            }

            productSupplierPreference.preferenceType = preferenceType
            productSupplierPreference.comments = params.globalPreferenceTypeComments

            def globalPreferenceTypeValidityStartDate = params.globalPreferenceTypeValidityStartDate ? dateFormat.parse(params.globalPreferenceTypeValidityStartDate) : null

            if (globalPreferenceTypeValidityStartDate) {
                productSupplierPreference.validityStartDate = globalPreferenceTypeValidityStartDate
            }

            def globalPreferenceTypeValidityEndDate = params.globalPreferenceTypeValidityEndDate ? dateFormat.parse(params.globalPreferenceTypeValidityEndDate) : null

            if (globalPreferenceTypeValidityEndDate) {
                productSupplierPreference.validityEndDate = globalPreferenceTypeValidityEndDate
            }
        }

        if (!productSupplier.code) {
            String prefix = productSupplier?.product?.productCode
            productSupplier.code = identifierService.generateProductSupplierIdentifier(prefix)
        }
        return productSupplier
    }

    def getOrCreateNew(Map params, boolean forceCreate) {
        def productSupplier
        if (params.productSupplier) {
            productSupplier = params.productSupplier ? ProductSupplier.get(params.productSupplier) : null
        } else {
            productSupplier = getProductSupplier(params)
        }

        if (!productSupplier && (params.supplierCode || params.manufacturer || params.manufacturerCode || forceCreate)) {
            return createProductSupplierWithoutPackage(params)
        }

        return productSupplier
    }

    def getProductSupplier(Map params) {
        String supplierCode = params.supplierCode ? params.supplierCode.replaceAll('[ .,-]','') : null
        String manufacturerCode = params.manufacturerCode ? params.manufacturerCode.replaceAll('[ .,-]','') : null

        String query = """
                select 
                    id
                FROM product_supplier_clean
                WHERE product_id = :productId
                AND supplier_id = :supplierId 
                """
        if (params.supplierCode) {
            query += " AND supplier_code = IFNULL(:supplierCode, supplier_code) "
        } else {
            query += " AND (supplier_code is null OR supplier_code = '') "
            if (params.manufacturer && params.manufacturerCode) {
                query += " AND manufacturer_id = :manufacturerId AND manufacturer_code = :manufacturerCode "
            } else if (params.manufacturer) {
                query += " AND manufacturer_id = :manufacturerId AND (manufacturer_code is null OR manufacturer_code = '')"
            } else if (params.manufacturerCode) {
                query += " AND manufacturer_code = :manufacturerCode AND (manufacturer_id is null or manufacturer_id = '')"
            } else {
                query += " AND (manufacturer_code is null OR manufacturer_code = '') AND (manufacturer_id is null or manufacturer_id = '')"
            }
        }
        Sql sql = new Sql(dataSource)
        def data = sql.rows(query, [
                'productId': params.product?.id,
                'supplierId': params.supplier?.id,
                'manufacturerId': params.manufacturer,
                'manufacturerCode': manufacturerCode,
                'supplierCode': supplierCode,
        ])
        def productSupplier = data ? ProductSupplier.get(data.first().id) : null
        return productSupplier
    }

    def createProductSupplierWithoutPackage(Map params) {
        Product product = Product.get(params.product.id)
        Organization organization = Organization.get(params.supplier.id)
        Organization manufacturer = Organization.get(params.manufacturer)
        ProductSupplier productSupplier = new ProductSupplier()
        productSupplier.code = params.sourceCode ?: identifierService.generateProductSupplierIdentifier(product?.productCode, organization?.code)
        productSupplier.name = params.sourceName ?: product?.name
        productSupplier.supplier = organization
        productSupplier.supplierCode = params.supplierCode
        productSupplier.product = product
        productSupplier.manufacturer = manufacturer
        productSupplier.manufacturerCode = params.manufacturerCode

        if (productSupplier.validate()) {
            productSupplier.save(failOnError: true, flush: true)
        }
        return productSupplier
    }
}
