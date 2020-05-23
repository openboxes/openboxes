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

class OrganizationController {

    def identifierService
    def scaffold = true

    def search = {

        List roleTypes = params.list("roleType").collect { it as RoleType }

        log.info "roleTypes " + roleTypes
        def organizationInstanceList = Organization.createCriteria().list(params){
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
        render(view: "list", model: [organizationInstanceList:organizationInstanceList, organizationInstanceTotal:organizationInstanceList.totalCount])
    }


    def save = {
        def organizationInstance = new Organization(params)

        if (!organizationInstance.code) {
            organizationInstance.code =
                    identifierService.generateOrganizationIdentifier(organizationInstance.name)
        }

        if (organizationInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'organization.label', default: 'Organization'), organizationInstance.id])}"
            redirect(controller: "organization", action: "edit", id: organizationInstance?.id)
        } else {
            render(view: "create", model: [organizationInstance: organizationInstance])
        }
    }

    def update = {
        def organizationInstance = Organization.get(params.id)
        if (organizationInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (organizationInstance.version > version) {
                    organizationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'organization.label', default: 'Organization')] as Object[], "Another user has updated this Organization while you were editing")
                    render(view: "edit", model: [organizationInstance: organizationInstance])
                    return
                }
            }
            organizationInstance.properties = params

            if (!organizationInstance.code) {
                organizationInstance.code =
                        identifierService.generateOrganizationIdentifier(organizationInstance.name)
            }

            if (!organizationInstance.hasErrors() && organizationInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'organization.label', default: 'Organization'), organizationInstance.id])}"
                redirect(action: "edit", id: organizationInstance.id)
            } else {
                render(view: "edit", model: [organizationInstance: organizationInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'organization.label', default: 'Organization'), params.id])}"
            redirect(action: "list")
        }
    }

    def resetSequence = {
        IdentifierTypeCode identifierTypeCode = params.identifierTypeCode as IdentifierTypeCode
        Integer sequenceNumber = params.sequenceNumber?:0 as Integer
        def organizationInstance = Organization.get(params.id)
        organizationInstance.sequences.put(identifierTypeCode.toString(), sequenceNumber.toString())
        organizationInstance.save()
        redirect(action: "edit", id: params.id)
    }

}
