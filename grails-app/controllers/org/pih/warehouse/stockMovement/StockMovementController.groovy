package org.pih.warehouse.stockMovement

class StockMovementController {
	def index = {
		redirect(action: "create")
	}

	def create = {
		render(template: "/stockMovement/create")
	}
}
