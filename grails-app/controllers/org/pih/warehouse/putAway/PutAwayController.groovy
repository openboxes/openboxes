package org.pih.warehouse.putAway

import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Location
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.requisition.Requisition

class PutAwayController {

	def pdfRenderingService

	def index = {
		redirect(action: "create")
	}

	def create = {
		render(template: "/putAway/create")
	}


	def generatePdf = {
        log.info "Params " + params

        JSONObject jsonObject = request.JSON

		renderPdf(
				template: "/putAway/print",
				model: [jsonObject:jsonObject],
				filename: "Putaway"
		)
	}
}
