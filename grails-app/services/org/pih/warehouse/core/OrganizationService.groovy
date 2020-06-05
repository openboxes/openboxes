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

    def identifierService
    boolean transactional = true

    Organization findOrCreateSupplierOrganization(String name, String code) {
        Organization organization = Organization.findByName(name)
        if (!organization) {
            organization = new Organization()
            organization.name = name
            organization.partyType = PartyType.findByPartyTypeCode(PartyTypeCode.ORGANIZATION)
            organization.code = code?:identifierService.generateOrganizationIdentifier(name)
        }

        if (!organization.hasRoleType(RoleType.ROLE_SUPPLIER)) {
            organization.addToRoles(new PartyRole(roleType: RoleType.ROLE_SUPPLIER))
        }

        if (!organization.hasRoleType(RoleType.ROLE_MANUFACTURER)) {
            organization.addToRoles(new PartyRole(roleType: RoleType.ROLE_MANUFACTURER))
        }

        if (organization.validate() && !organization.hasErrors()) {
            return organization.save(flush: true)
        } else {
            throw new ValidationException("Organization is not valid", organization.errors)
        }
        return organization
    }

}
