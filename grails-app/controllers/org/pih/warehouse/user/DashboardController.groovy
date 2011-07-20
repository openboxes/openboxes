package org.pih.warehouse.user;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.User;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.inventory.Warehouse;


class DashboardController {

	def shipmentService;
	
    def index = {
		if (!session.warehouse) {			
			redirect(action: "chooseWarehouse")			
		}
		
		Location location = Location.get(session?.warehouse?.id);
		def recentOutgoingShipments = shipmentService.getRecentOutgoingShipments(location?.id)
		def recentIncomingShipments = shipmentService.getRecentIncomingShipments(location?.id)
		def allOutgoingShipments = shipmentService.getShipmentsByOrigin(location)
		def allIncomingShipments = shipmentService.getShipmentsByDestination(location)
		
		[ 	outgoingShipments : recentOutgoingShipments, 
			incomingShipments : recentIncomingShipments,
			allOutgoingShipments : allOutgoingShipments,
			allIncomingShipments : allIncomingShipments,
			outgoingShipmentsByStatus : shipmentService.getShipmentsByStatus(allOutgoingShipments),
			incomingShipmentsByStatus : shipmentService.getShipmentsByStatus(allIncomingShipments)
		]
	}
	
	def chooseWarehouse = {
		
		def warehouse = null;
		if (params.id!='null') {			
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
				session.user = userInstance;
			}			
			
			// Successfully logged in and selected a warehouse
			//session.user = userInstance;
			
			//if (params?.targetUri) {
			//	redirect(uri: params.targetUri);
			//	return;
			//}
			redirect(controller:'dashboard', action:'index')
		}
		else {	
			List warehouses = Warehouse.findAllWhere("active":true)
			render(view: "chooseWarehouse", model: [warehouses: warehouses])
		}
		
	}
    
}
