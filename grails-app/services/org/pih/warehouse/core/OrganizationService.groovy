/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import grails.validation.ValidationException

class OrganizationService {

    def grailsApplication
    def identifierService
    boolean transactional = true
    
    Organization findOrCreateOrganizationFromLocation(Location locationInstance) {
        Organization organization = Organization.findByName(locationInstance.name)
        PartyRole supplier = PartyRole.findByRoleType(RoleType.ROLE_SUPPLIER)
        PartyRole manufacturer = PartyRole.findByRoleType(RoleType.ROLE_MANUFACTURER)

        if (organization == null) {
            organization = new Organization()
        } else if (locationInstance.locationType.locationTypeCode == LocationTypeCode.SUPPLIER) {
            if (!organization.hasRoleType(supplier)) {
                organization.addToRoles(supplier)
            }
            if (!organization.hasRoleType(manufacturer)) {
                organization.addToRoles(manufacturer)
            }
            return organization.save(flush: true)
        } else {
            return organization
        }

        organization.partyType = PartyType.findByName('Organization')
        organization.name = locationInstance.name
        organization.code = identifierService.generateOrganizationIdentifier(locationInstance.name)

        if (locationInstance.locationType.locationTypeCode == LocationTypeCode.SUPPLIER) {
            organization.addToRoles(supplier)
            organization.addToRoles(manufacturer)
        }

        if (organization.validate() && !organization.hasErrors()) {
            return organization.save(flush: true)
        } else {
            throw new ValidationException("Organization is not valid", organization.errors)
        }
    }

}
