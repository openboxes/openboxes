/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */
package org.pih.warehouse.picklist

import grails.converters.JSON
import org.pih.warehouse.core.Location
import org.pih.warehouse.requisition.Requisition
import org.xml.sax.SAXParseException

class PicklistController {

	def scaffold = true

	def picklistService
    def pdfRenderingService

	def save = {
		def jsonRequest = request.JSON
		def jsonResponse = []
		def picklist = picklistService.save(jsonRequest)
		if (!picklist.hasErrors()) {
			jsonResponse = [success: true, data: picklist.toJson()]
		}
		else {
			jsonResponse = [success: false, errors: picklist.errors]
		}
		render jsonResponse as JSON
	}

	def print = {
		def requisition = Requisition.get(params.id)
		def picklist = Picklist.findByRequisition(requisition)
		def location = Location.get(session.warehouse.id)
		[requisition:requisition, picklist: picklist, location:location, sorted:params.sorted]
    }

    def renderPdf = {
        def requisition = Requisition.get(params.id)
        def picklist = Picklist.findByRequisition(requisition)
        def location = Location.get(session.warehouse.id)
        //def filename = "Picklist - ${requisition.requestNumber}"
        //[requisition:requisition, picklist: picklist, location:location]

        //def defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
        //def locale = session?.user?.locale ?: session.locale ?: defaultLocale;

        //println "location ${location}"
        //println "request.locale ${request?.locale}"
        //println "session.user.locale ${session?.user?.locale}"
        //println "session.locale ${session?.locale}"
        //println "defaultLocale ${defaultLocale}"

        //println "Generating picklist PDF using locale ${locale}"
        //[template: '/simple', model: [var: 1]] + args

        //response.contentType = 'application/pdf'
        //response.setHeader 'Content-disposition', "attachment; filename=\"${filename}\""

//        pdfRenderingService.render([
//                locale:Locale.FRENCH,
//                template: "/picklist/print",
//                model: [requisition:requisition, picklist: picklist, location:location]], response)

        //println "Content: " + content
        renderPdf(
                template: "/picklist/print",
                //locale:locale,
                model: [requisition:requisition, picklist: picklist, location:location, sorted:params.sorted],
                filename: "Picklist - ${requisition.requestNumber}"
        )


    }

    def renderHtml = {

        def defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
        def locale = session?.user?.locale ?: session.locale ?: defaultLocale;
        def requisition = Requisition.get(params.id)
        def picklist = Picklist.findByRequisition(requisition)
        def location = Location.get(session.warehouse.id)
        //[requisition:requisition, picklist: picklist, location:location]

        println location
        render(template: "/picklist/print", model: [requisition:requisition, picklist: picklist, location:location, order:params.order])

    }

}
