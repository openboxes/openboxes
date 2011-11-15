package org.pih.warehouse.user;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.User;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.core.Location;


class DashboardController {

	def orderService
	def shipmentService;
	
    def index = {
		if (!session.warehouse) {			
			redirect(action: "chooseLocation")			
		}
		
		Location location = Location.get(session?.warehouse?.id);
		
		
		def recentOutgoingShipments = shipmentService.getRecentOutgoingShipments(location?.id)
		def recentIncomingShipments = shipmentService.getRecentIncomingShipments(location?.id)
		def allOutgoingShipments = shipmentService.getShipmentsByOrigin(location)
		def allIncomingShipments = shipmentService.getShipmentsByDestination(location)
		
		def outgoingOrders = orderService.getOutgoingOrders(location).groupBy { it?.status };
		def incomingOrders = orderService.getIncomingOrders(location).groupBy { it?.status };
		
		[ 	outgoingShipments : recentOutgoingShipments, 
			incomingShipments : recentIncomingShipments,
			allOutgoingShipments : allOutgoingShipments,
			allIncomingShipments : allIncomingShipments,
			outgoingOrders : outgoingOrders,
			incomingOrders : incomingOrders,
			outgoingShipmentsByStatus : shipmentService.getShipmentsByStatus(allOutgoingShipments),
			incomingShipmentsByStatus : shipmentService.getShipmentsByStatus(allIncomingShipments)
		]
	}
	
	def chooseLocation = {
					
		def warehouse = null;
		
		// If the user has selected a new location from the topnav bar, we need 
		// to retrieve the location to make sure it exists
		if (params.id != 'null') {			
			warehouse = Location.get(params.id);
		}

		// If a warehouse has been selected
		if (warehouse) {
			
			// Reset the locations displayed in the topnav
			session.loginLocations = null
			
			// Save the warehouse selection to the session
			session.warehouse = warehouse;
			
			// Save the warehouse selection for "last logged into" information
			if (session.user) { 
				def userInstance = User.get(session.user.id);
				//userInstance.warehouse = warehouse;
				userInstance.lastLoginDate = new Date();
				userInstance.save(flush:true);
				session.user = userInstance;
			}			
			
			// Successfully logged in and selected a warehouse
			//session.user = userInstance;
			
			if (params?.returnUrl) {
				redirect(uri: params.returnUrl - request.contextPath);
				return;
			}
			redirect(controller:'dashboard', action:'index')
		}
		else {	
			List warehouses = Location.findAllWhere("active":true)
			render(view: "chooseLocation", model: [warehouses: warehouses])
		}
		
	}
    
}
