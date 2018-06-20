package org.pih.warehouse.partialReceiving

class PartialReceivingController {
	def index = {
		redirect(action: "create")
	}

	def create = {
		render(template: "/partialReceiving/create")
	}
}
