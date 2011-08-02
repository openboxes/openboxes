package org.pih.warehouse.core;

class LocationController {
	
	def inventoryService
	
	/**
	 * Controllers for managing other locations (besides warehouses)
	 */
	
	def list = {	
		[locationInstanceList: Location.list(params).findAll { it.locationType.id != Constants.WAREHOUSE_LOCATION_TYPE_ID } ]
	}
	
	def edit = {
		def locationInstance = inventoryService.getLocation(params.id as Long)
		if (!locationInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [locationInstance: locationInstance]
		}
	}
	
	def update = {
			def locationInstance = inventoryService.getLocation(params.id ? params.id as Long : null)
			
			if (locationInstance) {
				if (params.version) {
					def version = params.version.toLong()
					if (locationInstance.version > version) {
						
						locationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'location.label', default: 'Location')] as Object[], "Another user has updated this Location while you were editing")
						render(view: "edit", model: [locationInstance: locationInstance])
						return
					}
				}
				
				locationInstance.properties = params
						
				if (!locationInstance.hasErrors()) {
					inventoryService.saveLocation(locationInstance)
					flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'location.label', default: 'Location'), locationInstance.id])}"
					redirect(action: "list", id: locationInstance.id)
				}
				else {
					render(view: "edit", model: [locationInstance: locationInstance])
				}
			}
			else {
				flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
				redirect(action: "list")
			}
		}
	
		def delete = {
			def locationInstance = Location.get(params.id)
	        if (locationInstance) {	        	
		          try {
		            locationInstance.delete(flush: true)
		            
		            flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
		            redirect(action: "list")
			      }
			      catch (org.springframework.dao.DataIntegrityViolationException e) {
		            flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
		            redirect(action: "edit", id: params.id)
			      }
	        }
	        else {
	            flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
	            redirect(action: "edit", id: params.id)
	        }
		}
	
}
