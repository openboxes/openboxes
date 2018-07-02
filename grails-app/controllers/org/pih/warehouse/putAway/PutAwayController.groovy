package org.pih.warehouse.putAway

class PutAwayController {
	def index = {
		redirect(action: "create")
	}

	def create = {
		render(template: "/putAway/create")
	}
}
