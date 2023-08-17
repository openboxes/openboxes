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
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierPreference

@Transactional
class ProductSupplierPreferenceDataService {

    String DEFAULT = "DEFAULT"

    Boolean validate(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        command.data.eachWithIndex { params, index ->

            String supplierCode = params.supplierCode
            String organizationCode = params.organizationCode
            String preferenceTypeName = params.preferenceTypeName

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

        String supplierCode = params.supplierCode
        String organizationCode = params.organizationCode
        String preferenceTypeName = params.preferenceTypeName
        Date validityStartDate = params.validityStartDate
        Date validityEndDate = params.validityEndDate
        String preferenceComments = params.preferenceComments

        ProductSupplier productSupplier = ProductSupplier.findByCode(supplierCode)
        Organization organization = null
        if (organizationCode != DEFAULT) {
            organization = Organization.findByCode(organizationCode)
        }
        PreferenceType preferenceType = PreferenceType.findByName(preferenceTypeName)

        ProductSupplierPreference existingPreference = ProductSupplierPreference
                .findByProductSupplierAndDestinationParty(productSupplier, organization)

        if (existingPreference) {
            existingPreference.preferenceType = preferenceType
            existingPreference.validityStartDate = validityStartDate
            existingPreference.validityEndDate = validityEndDate
            existingPreference.comments = preferenceComments
            return existingPreference
        } else {
            ProductSupplierPreference newPreference = new ProductSupplierPreference()
            newPreference.productSupplier = productSupplier
            newPreference.destinationParty = organization
            newPreference.preferenceType = preferenceType
            newPreference.validityStartDate = validityStartDate
            newPreference.validityEndDate = validityEndDate
            newPreference.comments = preferenceComments
            return newPreference
        }
    }
}
