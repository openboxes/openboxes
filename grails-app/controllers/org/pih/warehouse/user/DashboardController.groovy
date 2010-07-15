package org.pih.warehouse.user;

import org.pih.warehouse.core.Location;

class DashboardController {

    def index = {
		if (!session.warehouse) {			
			redirect(action: "chooseWarehouse")			
		}		
	}
	
	def chooseWarehouse = {
		log.info "Choose warehouse"
		
		def warehouse = null;
		if (params.id!='null') {
			log.error "looking up warehouse by id = ${params.id}";
			warehouse = Location.get(params.id);
		}

		if (warehouse) {
			
			// Save the current warehouse in the session
			session.warehouse = warehouse;
						
			// Save the user's preferred warehouse (if it's not set already)
			//if (userInstance.warehouse) {
			//	userInstance.warehouse = warehouse;
			//	userInstance.save(flush:true);
			//}
			
			// Successfully logged in and select a warehouse
			//session.user = userInstance;
			redirect(controller:'dashboard',action:'index')
		}
		else {
			log.error "ask user to choose a warehouse"
			//flash.message = "Please choose a valid warehouse.";
			
			//userInstance = new User(username:params['username'], password:params['password'])
			//userInstance.errors.rejectValue("version", "default.authentication.failure",
			//		[message(code: 'user.label', default: 'User')] as Object[], "Unable to authenticate user with no warehouse.")

			render(view: "chooseWarehouse")
		}
		
	}
    
}
