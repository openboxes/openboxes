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
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierPreference
import java.text.SimpleDateFormat

class ProductSupplierPreferenceDataService {

    String DEFAULT = "DEFAULT"

    Boolean validate(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        command.data.eachWithIndex { params, index ->

            def supplierCode = params.supplierCode
            def organizationCode = params.organizationCode
            def preferenceTypeName = params.preferenceTypeName
            def validityStartDate = params.validityStartDate
            def validityEndDate = params.validityEndDate

            if (!supplierCode) {
                command.errors.reject("Row ${index + 1}: Product source code is required")
            } else if (!ProductSupplier.findByCode(supplierCode)) {
                command.errors.reject("Row ${index + 1}: Product source with code: ${supplierCode}  does not exist")
            }

            if (!organizationCode) {
                command.errors.reject("Row ${index + 1}: Organization code is required")
            } else if (organizationCode != DEFAULT && !Organization.findByCode(organizationCode)) {
                command.errors.reject("Row ${index + 1}: Organization with code: ${organizationCode}  does not exist")
            }

            if (!preferenceTypeName) {
                command.errors.reject("Row ${index + 1}: Preference Type is required")
            } else if (!PreferenceType.findByName(preferenceTypeName)) {
                command.errors.reject("Row ${index + 1}: Preference Type with name: ${preferenceTypeName} does not exist")
            }

            def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
            if (validityStartDate) {
                try {
                    dateFormat.parse(validityStartDate)
                } catch (Exception e) {
                    command.errors.reject("Row ${index + 1}: Validity start date ${validityStartDate} is invalid")
                }
            }

            if (validityEndDate) {
                try {
                    dateFormat.parse(validityEndDate)
                } catch (Exception e) {
                    command.errors.reject("Row ${index + 1}: Validity end date ${validityEndDate} is invalid")
                }
            }
        }
    }

    void process(ImportDataCommand command) {
        log.info "Process data " + command.filename

        command.data.eachWithIndex { params, index ->
            ProductSupplierPreference productSupplierPreference = createOrUpdate(params)
            if (productSupplierPreference.validate()) {
                productSupplierPreference.save(failOnError: true)
            }
        }
    }

    def createOrUpdate(Map params) {

        def supplierCode = params.supplierCode
        def organizationCode = params.organizationCode
        def preferenceTypeName = params.preferenceTypeName
        def validityStartDate = params.validityStartDate
        def validityEndDate = params.validityEndDate
        def preferenceComments = params.preferenceComments

        ProductSupplier productSupplier = ProductSupplier.findByCode(supplierCode)
        Organization organization = null
        if (organizationCode != DEFAULT) {
            organization = Organization.findByCode(organizationCode)
        }
        PreferenceType preferenceType = PreferenceType.findByName(preferenceTypeName)

        def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
        Date parsedValidityStartDate = validityStartDate ? dateFormat.parse(validityStartDate) : null
        Date parsedValidityEndDate = validityEndDate ? dateFormat.parse(validityEndDate) : null

        ProductSupplierPreference existingPreference = ProductSupplierPreference
                .findByProductSupplierAndDestinationParty(productSupplier, organization)

        if (existingPreference) {
            existingPreference.preferenceType = preferenceType
            existingPreference.validityStartDate = parsedValidityStartDate
            existingPreference.validityEndDate = parsedValidityEndDate
            existingPreference.comments = preferenceComments
            return existingPreference
        } else {
            ProductSupplierPreference newPreference = new ProductSupplierPreference()
            newPreference.productSupplier = productSupplier
            newPreference.destinationParty = organization
            newPreference.preferenceType = preferenceType
            newPreference.validityStartDate = parsedValidityStartDate
            newPreference.validityEndDate = parsedValidityEndDate
            newPreference.comments = preferenceComments
            return newPreference
        }
    }
}
