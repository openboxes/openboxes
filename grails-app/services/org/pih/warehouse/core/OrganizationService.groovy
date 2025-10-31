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

import grails.gorm.transactions.Transactional
import org.apache.commons.lang.StringUtils

@Transactional
class OrganizationService {

    OrganizationIdentifierService organizationIdentifierService

    List selectOrganizations(ArrayList<RoleType> roleTypes, Boolean active = false, currentOrganizationId) {
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
            if (active) {
                or {
                    eq('id', currentOrganizationId)
                    eq('active', active)
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

    Organization findOrganization(String name, String code) {
        Organization organization = Organization.createCriteria().get {
            eq("code", code)
            ne("code", StringUtils.EMPTY)
            isNotNull("code")
        }
        if (!organization) {
            organization = Organization.createCriteria().list(max: 1) {
                eq("name", name)
                ne("name", StringUtils.EMPTY)
                isNotNull("name")
                order("dateCreated", "asc")
            }[0]
        }
        return organization
    }

    Organization findOrCreateOrganization(String name, String code, List<RoleType> roleTypes) {
        Organization organization = findOrganization(name, code)
        if (!organization) {
            organization = new Organization()
            organization.name = name
            organization.partyType = PartyType.findByCode(Constants.DEFAULT_ORGANIZATION_CODE)
            organization.code = code?:organizationIdentifierService.generate(name)
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

    Organization saveOrganization(Organization organization) {
        return organization.save(failOnError: true)
    }

    Organization createOrganization(Organization organization) {
        if (!organization.code) {
            organization.code = organizationIdentifierService.generate(organization.name)
        }

        if (!organization.partyType) {
            organization.partyType = PartyType.findByCode(Constants.DEFAULT_ORGANIZATION_CODE)
        }

        return saveOrganization(organization)
    }

    Organization findOrCreateBuyerOrganization(String name, String code) {
        return findOrCreateOrganization(name, code, [RoleType.ROLE_BUYER, RoleType.ROLE_DISTRIBUTOR])
    }

    Organization findOrCreateSupplierOrganization(String name, String code) {
        return findOrCreateOrganization(name, code, [RoleType.ROLE_SUPPLIER, RoleType.ROLE_MANUFACTURER])
    }

    List<Organization> getOrganizations(Map params) {
        List roleTypes = params.list("roleType").collect { it as RoleType }

        List<Organization> organizations = Organization.createCriteria().list(params) {
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
            if (params.active) {
                eq('active', true)
            }
            if (params.sort == "name") {
                order("name", params.order ?: "asc")
            }
        }
        return organizations
    }
}
