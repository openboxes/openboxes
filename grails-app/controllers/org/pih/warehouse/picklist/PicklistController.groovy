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
		[requisition:requisition, picklist: picklist, location:location]
    }

    def renderPdf = {
        def requisition = Requisition.get(params.id)
        def picklist = Picklist.findByRequisition(requisition)
        def location = Location.get(session.warehouse.id)
        //[requisition:requisition, picklist: picklist, location:location]

        println location
        renderPdf(template: "/picklist/print", model: [requisition:requisition, picklist: picklist, location:location], filename: "Picklist - ${requisition.requestNumber}")

    }

}
