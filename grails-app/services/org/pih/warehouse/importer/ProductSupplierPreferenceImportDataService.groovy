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
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierPreference

@Transactional
class ProductSupplierPreferenceImportDataService implements ImportDataService {
    String DEFAULT = "DEFAULT"

    @Override
    void validateData(ImportDataCommand command) {
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
            } else if (organizationCode != DEFAULT) {
                Organization organization = Organization.findByCode(organizationCode)
                if (!organization) {
                    command.errors.reject("Row ${index + 1}: Organization with code: ${organizationCode} does not exist")
                } else {
                    Boolean isOrganizationRoleTypeBuyer = organization.roles?.find { it.roleType == RoleType.ROLE_BUYER }
                    if (!isOrganizationRoleTypeBuyer) {
                        command.errors.reject("Row ${index + 1}: Organization with code: ${organizationCode} is not a buyer organization")
                    }
                }
            }

            if (!preferenceTypeName) {
                command.errors.reject("Row ${index + 1}: Preference Type is required")
            } else if (!PreferenceType.findByName(preferenceTypeName)) {
                command.errors.reject("Row ${index + 1}: Preference Type with name: ${preferenceTypeName} does not exist")
            }
        }
    }

    @Override
    void importData(ImportDataCommand command) {
        log.info "Process data " + command.filename

        command.data.eachWithIndex { params, index ->
            ProductSupplierPreference productSupplierPreference = bindProductSupplierPreference(params)
            if (productSupplierPreference.validate()) {
                productSupplierPreference.save(failOnError: true)
            }
        }
    }

    ProductSupplierPreference bindProductSupplierPreference(Map params) {
        ProductSupplier productSupplier = ProductSupplier.findByCode(params.supplierCode)
        Organization organization = null
        if (params.organizationCode != DEFAULT) {
            organization = Organization.findByCode(params.organizationCode)
        }
        PreferenceType preferenceType = PreferenceType.findByName(params.preferenceTypeName)

        ProductSupplierPreference productSupplierPreference = ProductSupplierPreference
                .findByProductSupplierAndDestinationParty(productSupplier, organization)

        if (productSupplierPreference) {
            productSupplierPreference.preferenceType = preferenceType
            productSupplierPreference.validityStartDate = params.validityStartDate
            productSupplierPreference.validityEndDate = params.validityEndDate
            productSupplierPreference.comments = params.preferenceComments
        } else {
            productSupplierPreference = new ProductSupplierPreference()
            productSupplierPreference.productSupplier = productSupplier
            productSupplierPreference.destinationParty = organization
            productSupplierPreference.preferenceType = preferenceType
            productSupplierPreference.validityStartDate = params.validityStartDate
            productSupplierPreference.validityEndDate = params.validityEndDate
            productSupplierPreference.comments = params.preferenceComments
        }
        return productSupplierPreference
    }
}
