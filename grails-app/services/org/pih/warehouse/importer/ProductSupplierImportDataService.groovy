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
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.data.ProductSupplierService
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import util.ConfigHelper

import java.text.SimpleDateFormat

@Transactional
class ProductSupplierImportDataService implements ImportDataService {
    ProductSupplierService productSupplierService

    @Override
    void validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        command.data.eachWithIndex { params, index ->
            boolean productSupplierExists = params.id ? ProductSupplier.exists(params.id) : null
            if (params.active && !(params.active instanceof Boolean)) {
                command.errors.reject("Row ${index + 1}: Active field has to be either empty or a boolean value (true/false)")
            }

            if (!params.name) {
                command.errors.reject("Row ${index + 1}: Product Source Name is required")
            }

            if (!params.productCode) {
                command.errors.reject("Row ${index + 1}: Product Code is required")
            } else if (params.productCode && !Product.findByProductCode(params.productCode)) {
                command.errors.reject("Row ${index + 1}: Product with productCode ${params.productCode} does not exist")
            }

            if (!params.supplierName) {
                command.errors.reject("Row ${index + 1}: Supplier Name is required")
            }

            Organization supplier = params.supplierName ? Organization.findByName(params.supplierName) : null

            /**
             *  Prevent from assigning an inactive supplier to a source that is about to become active or while creating a new source
                "to become active" means that an existing source is active and we don't change it, or a source is inactive and we are activating it during the import
                Allow assigning an inactive supplier to an inactive source (or to a source that is about to become inactive)
             */
            if (supplier && !supplier.active && (params.active || !productSupplierExists)) {
                command.errors.reject("Row ${index + 1}: Supplier '${supplier.name}' is no longer active. Choose an active supplier")
            }

            if (params.supplierName && !supplier) {
                command.errors.reject("Row ${index + 1}: Supplier with name '${params.supplierName}' does not exist")
            }

            if (params.manufacturerName && !Organization.findByName(params.manufacturerName)?.hasRoleType(RoleType.ROLE_MANUFACTURER)) {
                command.errors.reject("Row ${index + 1}: Manufacturer with name '${params.manufacturerName}' does not exist")
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


            if (params.globalPreferenceTypeName && !PreferenceType.findByName(params.globalPreferenceTypeName)) {
                command.errors.reject("Row ${index + 1}: Preference Type with name '${params.globalPreferenceTypeName}' does not exist")
            }

            if (!params.defaultProductPackageUomCode) {
                command.errors.reject("Row ${index + 1}: Default Package Type is required")
            }

            if (!params.defaultProductPackageQuantity) {
                command.errors.reject("Row ${index + 1}: Default Package Size is required")
            }

            log.info("uomCode " + params.defaultProductPackageUomCode)
            if (params.defaultProductPackageUomCode) {
                def unitOfMeasure = UnitOfMeasure.findByCode(params.defaultProductPackageUomCode)
                if (!unitOfMeasure) {
                    command.errors.reject("Row ${index + 1}: Unit of measure ${params.defaultProductPackageUomCode} does not exist")
                }
                if (unitOfMeasure && !params.defaultProductPackageQuantity) {
                    command.errors.reject("Row ${index + 1}: Unit of measure ${params.defaultProductPackageUomCode} requires a quantity")
                }
                if (unitOfMeasure && params.defaultProductPackageQuantity && params.defaultProductPackageQuantity % 1 != 0) {
                    command.errors.reject("Row ${index + 1}: Unit of measure quntity must be a whole number")
                }
            }

            Date minDate = ConfigHelper.getMinimumExpirationDate()
            def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
            if (params.globalPreferenceTypeValidityStartDate) {
                try {
                    def startDate = dateFormat.parse(params.globalPreferenceTypeValidityStartDate)

                    if (minDate > startDate) {
                        command.errors.reject("Row ${index + 1}: Validity start date ${params.globalPreferenceTypeValidityStartDate} is invalid. Please enter a date after ${minDate.getYear()+1900}.")
                    }
                } catch (Exception e) {
                    command.errors.reject("Row ${index + 1}: Validity start date ${params.globalPreferenceTypeValidityStartDate} is invalid. "+ e.message)
                }
            }

            if (params.globalPreferenceTypeValidityEndDate) {
                try {
                    def endDate = dateFormat.parse(params.globalPreferenceTypeValidityEndDate)

                    if (minDate > endDate) {
                        command.errors.reject("Row ${index + 1}: Validity start date ${params.globalPreferenceTypeValidityEndDate} is invalid. Please enter a date after ${minDate.getYear()+1900}.")
                    }
                } catch (Exception e) {
                    command.errors.reject("Row ${index + 1}: Validity end date ${params.globalPreferenceTypeValidityEndDate} is invalid. " + e.message)
                }
            }

            if (params.contractPriceValidUntil) {
                try {
                    def validUntilDate = dateFormat.parse(params.contractPriceValidUntil)

                    if (minDate > validUntilDate) {
                        command.errors.reject("Row ${index + 1}: Contract Price Valid Until date ${params.contractPriceValidUntil} is invalid. Please enter a date after ${minDate.getYear()+1900}.")
                    }
                } catch (Exception e) {
                    command.errors.reject("Row ${index + 1}: Contract Price Valid Until date ${params.contractPriceValidUntil} is invalid. " + e.message)
                }
            }
        }
    }

    @Override
    void importData(ImportDataCommand command) {
        log.info "Process data " + command.filename

        command.data.each { params ->
            ProductSupplier productSupplier = productSupplierService.createOrUpdate(params)
            productSupplier.save(failOnError: true)
        }
    }
}
