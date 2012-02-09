package org.pih.warehouse.user;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.User;
import org.pih.warehouse.order.Order;
import org.pih.warehouse.receiving.Receipt;
import org.pih.warehouse.request.Request;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.Transaction;


class DashboardController {

	def orderService
	def shipmentService;
	def productService 
    def index = {
		if (!session.warehouse) {			
			redirect(action: "chooseLocation")			
		}
		
		Location location = Location.get(session?.warehouse?.id);
		
		def recentOutgoingShipments = shipmentService.getRecentOutgoingShipments(location?.id)
		def recentIncomingShipments = shipmentService.getRecentIncomingShipments(location?.id)
		def allOutgoingShipments = shipmentService.getShipmentsByOrigin(location)
		def allIncomingShipments = shipmentService.getShipmentsByDestination(location)
		def activityList = []
		recentOutgoingShipments.each { 
			activityList << new DashboardActivityCommand(
				type: "lorry",
				label: "Shipment <a href='${createLink(controller: 'shipment', action: 'showDetails', id: it.id)}'>'${it.name}'</a> was updated", 
				url: "${createLink(controller: 'shipment', action: 'showDetails', id: it.id)}",
				dateCreated: it.dateCreated, 
				lastUpdated: it.lastUpdated, 
				shipment: it)
		}
		recentIncomingShipments.each {
			activityList << new DashboardActivityCommand(
				type: "lorry",
				label: "Shipment <a href='${createLink(controller: 'shipment', action: 'showDetails', id: it.id)}'>'${it.name}'</a> was updated", 
				url: "${createLink(controller: 'shipment', action: 'showDetails', id: it.id)}",
				dateCreated: it.dateCreated, 
				lastUpdated: it.lastUpdated, 
				shipment: it)
		}
		
		def users = User.executeQuery( "select distinct u from User u where u.lastUpdated >= :lastUpdated", [lastUpdated:new Date()-30, max:10, offset:5] );
		users.each { 
			activityList << new DashboardActivityCommand(
				type: "user",
				label: "User '<a href='${createLink(controller: 'user', action: 'show', id: it.id)}'>${it.username}</a>' was updated",
				url: "${createLink(controller: 'user', action: 'show', id: it.id)}",
				dateCreated: it.dateCreated,
				lastUpdated: it.lastUpdated,
				user: it)
		}
		
		activityList = activityList.sort { it.lastUpdated }.reverse()
		
		def outgoingOrders = orderService.getOutgoingOrders(location)
		def incomingOrders = orderService.getIncomingOrders(location)
		
		[ 	outgoingShipments : recentOutgoingShipments, 
			incomingShipments : recentIncomingShipments,
			allOutgoingShipments : allOutgoingShipments,
			allIncomingShipments : allIncomingShipments,
			outgoingOrders : outgoingOrders,
			incomingOrders : incomingOrders,
			rootCategory : productService.getRootCategory(),
			outgoingOrdersByStatus: orderService.getOrdersByStatus(outgoingOrders),
			incomingOrdersByStatus: orderService.getOrdersByStatus(incomingOrders),
			outgoingShipmentsByStatus : shipmentService.getShipmentsByStatus(allOutgoingShipments),
			incomingShipmentsByStatus : shipmentService.getShipmentsByStatus(allIncomingShipments),
			activityList : activityList
		]
	}
	
	def menu = { 
		def incomingShipments = Shipment.findAllByDestination(session?.warehouse).groupBy{it.status.code}.sort()
		def outgoingShipments = Shipment.findAllByOrigin(session?.warehouse).groupBy{it.status.code}.sort();
		def incomingOrders = Order.executeQuery('select o.status, count(*) from Order as o where o.destination = ? group by o.status', [session?.warehouse])
		def incomingRequests = Request.findAllByDestination(session?.warehouse).groupBy{it.status}.sort()
		def outgoingRequests = Request.findAllByOrigin(session?.warehouse).groupBy{it.status}.sort()
		
		[incomingShipments: incomingShipments, 
			outgoingShipments: outgoingShipments, 
			incomingOrders: incomingOrders, 
			incomingRequests: incomingRequests,
			outgoingRequests: outgoingRequests,
			quickCategories:productService.getQuickCategories()]
	}

	

	def chooseLayout = { 
		
		if (params.layout) { 
			session.layout = params.layout
		}
		redirect(controller:'dashboard', action:'index')
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
				if (userInstance.rememberLastLocation) { 
					userInstance.warehouse = warehouse 
				}
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


class DashboardCommand { 
	
	List<DashboardActivityCommand> activityList;
	
	
}


class DashboardActivityCommand { 

	String label
	String type	
	String url
	Shipment shipment
	Receipt receipt
	Transaction transaction
	User user
	Date lastUpdated
	Date dateCreated
	
	
	String getActivityType() { 
		return lastUpdated == dateCreated ? "created" : "updated"
	}
}