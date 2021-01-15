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

import grails.plugins.csv.CSVWriter
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.springframework.transaction.TransactionStatus

class OrganizationController {

    def identifierService
    def organizationService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def search() {
        def organizationInstanceList = organizationService.getOrganizations(params)
        render(view: "list", model: [organizationInstanceList:organizationInstanceList, organizationInstanceTotal:organizationInstanceList.totalCount])
    }

    def download() {
        params.max = null
        def organizationInstanceList = organizationService.getOrganizations(params)
        def sw = new StringWriter()
        def csv = new CSVWriter(sw, {
            "Id" { it.id }
            "Code" { it.code }
            "Name" { it.name }
            "Default location" { it.defaultLocation }
            "Roles" { it.roles }
        })

        organizationInstanceList.each { organization ->
            csv << [
                    id             : organization.id,
                    code           : organization.code,
                    name           : organization.name,
                    defaultLocation: organization.defaultLocation ?: '',
                    roles          : organization.roles.join(","),
            ]
        }
        response.setHeader("Content-disposition", "attachment; filename=\"Organizations-${new Date().format("MM/dd/yyyy")}.csv\"")
        render(contentType: "text/csv", text: sw.toString(), encoding: "UTF-8")
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [organizationInstanceList: Organization.list(params), organizationInstanceTotal: Organization.count()]
    }

    def create() {
        def organizationInstance = new Organization()
        organizationInstance.properties = params
        return [organizationInstance: organizationInstance]
    }

    def save() {
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

    def update() {
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



    def show() {
        def organizationInstance = Organization.get(params.id)
        if (!organizationInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'organization.label', default: 'Organization'), params.id])}"
            redirect(action: "list")
        }
        else {
            [organizationInstance: organizationInstance]
        }
    }

    def edit() {
        def organizationInstance = Organization.get(params.id)
        if (!organizationInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'organization.label', default: 'Organization'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [organizationInstance: organizationInstance]
        }
    }

    def delete() {
        if (Organization.exists(params.id)) {
            try {
                Organization.withTransaction { TransactionStatus status ->
                    Organization organizationInstance = Organization.get(params.id);
                    if(organizationInstance){
                        organizationInstance.delete();
                    }
                }

                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'organizationInstance.label', default: 'Organization'), params.id])}"
                redirect(controller: "organization", action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'organizationInstance.label', default: 'Organization'), params.id])} (" + e.message + ")"
                redirect(action: "edit", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'organizationInstance.label', default: 'Organization'), params.id])}"
            redirect(action: "edit", id: params.id)
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
