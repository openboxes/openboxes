package org.pih.warehouse.core;

class ErrorsController {

	def handleException = { 
		render(view: "/error")
	}
	
}