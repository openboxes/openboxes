package org.pih.warehouse.user;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.Warehouse;

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
			log.info "looking up warehouse by id = ${params.id}";
			warehouse = Location.get(params.id);
		}

		if (warehouse) {
			
			// Save the warehouse selection to the session
			session.warehouse = warehouse;
						
			// Save the warehouse selection for "last logged into" information
			if (session.user) { 
				def userInstance = User.get(session.user.id);
				userInstance.warehouse = warehouse;
				userInstance.lastLoginDate = new Date();
				userInstance.save(flush:true);
			}			
			// Successfully logged in and select a warehouse
			//session.user = userInstance;
			redirect(controller:'dashboard', action:'index')
		}
		else {	
			render(view: "chooseWarehouse", model: [warehouses: Warehouse.list()])
		}
		
	}
    
}
