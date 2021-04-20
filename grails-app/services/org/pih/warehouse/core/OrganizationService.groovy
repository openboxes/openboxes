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


    List selectOrganizations(roleTypes) {
        return Organization.createCriteria().list {
            projections {
                property("id")
                property("name")
            }
            if (roleTypes) {
                roles {
                    'in'("roleType", roleTypes)
                }
            }
            order("name", "asc")
        }.collect {
            return [id: it[0], name: it[1] ]
        }
    }

    Organization findOrCreateOrganization(String name, String code) {
        return findOrCreateOrganization(name, code, [])
    }

    Organization findOrCreateOrganization(String name, String code, List<RoleType> roleTypes) {
        Organization organization = Organization.findByCodeOrName(code, name)
        if (!organization) {
            organization = new Organization()
            organization.name = name
            organization.partyType = PartyType.findByPartyTypeCode(PartyTypeCode.ORGANIZATION)
            organization.code = code?:identifierService.generateOrganizationIdentifier(name)
        }

        if (roleTypes) {
            roleTypes.each { RoleType roleType ->
                if (!organization.hasRoleType(roleType)) {
                    organization.addToRoles(new PartyRole(roleType: roleType))
                }
            }
        }

        if (organization.validate() && !organization.hasErrors()) {
            organization.save()
        }
        return organization
    }

    Organization findOrCreateBuyerOrganization(String name, String code) {
        return findOrCreateOrganization(name, code, [RoleType.ROLE_BUYER, RoleType.ROLE_DISTRIBUTOR])
    }

    Organization findOrCreateSupplierOrganization(String name, String code) {
        return findOrCreateOrganization(name, code, [RoleType.ROLE_SUPPLIER, RoleType.ROLE_MANUFACTURER])
    }

    List getOrganizations(Map params) {
        List roleTypes = params.list("roleType").collect { it as RoleType }

        def organizations = Organization.createCriteria().list(params){
            if (params.q) {
                or {
                    ilike("id", "${params.q}%")
                    ilike("code", "${params.q}%")
                    ilike("name", "${params.q}%")
                    ilike("description", "${params.q}%")
                }
            }
            if (roleTypes) {
                roles {
                    'in'("roleType", roleTypes)
                }
            }
        }
        return organizations
    }
}
