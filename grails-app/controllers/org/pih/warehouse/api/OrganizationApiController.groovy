/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.grails.web.json.JSONObject
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyType

class OrganizationApiController extends BaseDomainApiController {

    def organizationService
    def identifierService

    def list() {
        def organizations = organizationService.getOrganizations(params)
        render ([data:organizations] as JSON)
     }

    def read() {
        Organization organization = Organization.get(params.id)
        if (!organization) {
            throw new IllegalArgumentException("No Organization found for organization ID ${params.id}")
        }

        render([data: organization] as JSON)
    }

    def create(Organization organization) {
        organizationService.createOrganization(organization)
        render([data: [id: organization.id]] as JSON)
    }
}
