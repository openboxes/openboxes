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
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.data.ProductSupplierService
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import util.ConfigHelper

import java.text.SimpleDateFormat

@Transactional
class ProductSupplierImportDataService implements ImportDataService {
    ProductSupplierService productSupplierService

    void validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        command.data.eachWithIndex { params, index ->

            def active = params.active
            def productCode = params.productCode
            def supplierName = params.supplierName
            def manufacturerName = params.manufacturerName
            def preferenceType = params.globalPreferenceTypeName
            def uomCode = params.defaultProductPackageUomCode
            def packageQuantity = params.defaultProductPackageQuantity
            def validityStartDate = params.globalPreferenceTypeValidityStartDate
            def validityEndDate = params.globalPreferenceTypeValidityEndDate
            def contractPriceValidUntil = params.contractPriceValidUntil

            if (active && !(active instanceof Boolean)) {
                command.errors.reject("Row ${index + 1}: Active field has to be either empty or a boolean value (true/false)")
            }

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

            Date minDate = ConfigHelper.getMinimumExpirationDate()
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

    void importData(ImportDataCommand command) {
        log.info "Process data " + command.filename

        command.data.each { params ->
            ProductSupplier productSupplier = productSupplierService.createOrUpdate(params)
            productSupplier.save(failOnError: true)
        }
    }
}
