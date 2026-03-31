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

    private static final List<RoleType> SUPPLIER_ROLES = [RoleType.ROLE_SUPPLIER, RoleType.ROLE_MANUFACTURER]

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

    Organization findOrganization(String name, String code) {
        // First, search by code if one was provided
        Organization organization = StringUtils.isBlank(code) ? null : Organization.createCriteria().get {
            eq("code", code)
            ne("code", StringUtils.EMPTY)
            isNotNull("code")
        } as Organization

        // Then search by name, which is not strictly unique so in case multiple are found, return the newest one
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

    private Organization createOrUpdateOrganization(String name, String code, List<RoleType> roleTypes) {
        Organization organization = findOrganization(name, code)
        if (!organization) {
            return createOrganization(name, code, roleTypes)
        }
        return updateOrganization(organization, roleTypes)
    }

    private Organization saveOrganization(Organization organization) {
        return organization.save(failOnError: true)
    }

    private void addRolesToOrganization(Organization organization, List<RoleType> roleTypes) {
        if (!roleTypes) {
            return
        }

        for (RoleType roleType in roleTypes) {
            if (organization.hasRoleType(roleType)) {
                continue
            }
            organization.addToRoles(new PartyRole(roleType: roleType))
        }
    }

    /**
     * Persists a new organization with all of the given roles
     */
    Organization createOrganization(String name, String code=null, List<RoleType> roleTypes=[]) {
        Organization organization = new Organization(
                name: name,
                code: code,  // If this is null it will be auto-generated later
        )
        return createOrganization(organization, roleTypes)
    }

    /**
     * Persists a new organization, populating it with any required default or auto-generated fields,
     * and adding to it all of the given roles
     */
    Organization createOrganization(Organization organization, List<RoleType> roleTypes=[]) {
        if (!organization.code) {
            organization.code = organizationIdentifierService.generate(organization)
        }

        if (!organization.partyType) {
            organization.partyType = PartyType.findByCode(Constants.DEFAULT_ORGANIZATION_CODE)
        }

        addRolesToOrganization(organization, roleTypes)

        return saveOrganization(organization)
    }

    /**
     * Updates an existing organization, adding to it all of the given roles
     */
    Organization updateOrganization(Organization organization, List<RoleType> roleTypes=[]) {
        addRolesToOrganization(organization, roleTypes)
        return saveOrganization(organization)
    }

    Organization createSupplierOrganization(String name, String code=null) {
        return createOrganization(name, code, SUPPLIER_ROLES)
    }

    Organization findOrCreateSupplierOrganization(String name, String code=null) {
        return createOrUpdateOrganization(name, code, SUPPLIER_ROLES)
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
