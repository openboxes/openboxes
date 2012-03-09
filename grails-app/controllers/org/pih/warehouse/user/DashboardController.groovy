package org.pih.warehouse.user;

import org.pih.warehouse.core.Location;
import grails.converters.JSON;

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.User;
import org.pih.warehouse.order.Order;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.receiving.Receipt;
import org.pih.warehouse.request.Request;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.util.LocalizationUtil;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.InventoryItem;
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
			def link = "${createLink(controller: 'shipment', action: 'showDetails', id: it.id)}"
			def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
			activityType = "${warehouse.message(code: activityType)}"
			
			activityList << new DashboardActivityCommand(
				type: "lorry",
				label: "${warehouse.message(code:'dashboard.activity.shipment.label', args: [link, it.name, activityType])}", 
				url: link,
				dateCreated: it.dateCreated, 
				lastUpdated: it.lastUpdated, 
				shipment: it)
		}
		recentIncomingShipments.each {			
			def link = "${createLink(controller: 'shipment', action: 'showDetails', id: it.id)}"
			def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
			activityType = "${warehouse.message(code: activityType)}"

			activityList << new DashboardActivityCommand(
				type: "lorry",
				label:  "${warehouse.message(code:'dashboard.activity.shipment.label', args: [link, it.name, activityType])}",
				url: link,
				dateCreated: it.dateCreated, 
				lastUpdated: it.lastUpdated, 
				shipment: it)
		}
		def products = Product.executeQuery( "select distinct p from Product p where p.lastUpdated >= :lastUpdated", ['lastUpdated':new Date()-15, max:10, offset:5] );
		products.each { 
			def link = "${createLink(controller: 'inventoryItem', action: 'showStockCard', params:['product.id': it.id])}"
			def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
			activityType = "${warehouse.message(code: activityType)}"
			
			activityList << new DashboardActivityCommand(
				type: "package",
				label: "${warehouse.message(code:'dashboard.activity.product.label', args: [link, it.name, activityType])}",
				url: link,
				dateCreated: it.dateCreated,
				lastUpdated: it.lastUpdated,
				product: it)
		}
		
		def transactions = Transaction.executeQuery("select distinct t from Transaction t where t.lastUpdated >= :lastUpdated and \
			t.inventory = :inventory", ['lastUpdated':new Date()-10, 'inventory':location.inventory, max:10, offset:5] );
		
		transactions.each { 
			def link = "${createLink(controller: 'inventory', action: 'showTransaction', id: it.id)}"
			def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
			activityType = "${warehouse.message(code: activityType)}"
			def label = LocalizationUtil.getLocalizedString(it)
			activityList << new DashboardActivityCommand(
				type: "table",
				label: "${warehouse.message(code:'dashboard.activity.transaction.label', args: [link, label, activityType])}",
				url: link,
				dateCreated: it.dateCreated,
				lastUpdated: it.lastUpdated,
				transaction: it)
		}
		
		def users = User.executeQuery( "select distinct u from User u where u.lastUpdated >= :lastUpdated", ['lastUpdated':new Date()-15, max:10, offset:5] );
		users.each { 
			def link = "${createLink(controller: 'user', action: 'show', id: it.id)}"
			def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
			activityType = "${warehouse.message(code: activityType)}"

			
			activityList << new DashboardActivityCommand(
				type: "user",
				label: "${warehouse.message(code:'dashboard.activity.user.label', args: [link, it.username, activityType])}",				
				url: link,
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
	
	def status = { 
		def admin = User.get(1)
		def comments = Comment.findAllBySenderAndRecipient(admin, admin) 
		
		def results = comments.collect {
			if (it.dateSent > new Date()) { 
				[ id: it.id, comment: warehouse.message(code:it.comment, args: [format.datetime(obj: it.dateSent)]), dateSent: it.dateSent ]
			}
		}
		render results as JSON
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
			// Try to redirect to the previous action before session timeout
			if (session.targetUri || params.targetUri) {
				if (params.targetUri) { 
					redirect(uri: params.targetUri);
					return;
				}
				else if (session.targetUri) {
					redirect(uri: session.targetUri)
					return;
				}					
				//session.remove("targetUri")
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
	
	User user
	Shipment shipment
	Receipt receipt
	Product product
	Transaction transaction
	InventoryItem inventoryItem
	
	Date lastUpdated
	Date dateCreated
	
	
	String getActivityType() { 
		return lastUpdated == dateCreated ? "created" : "updated"
	}
}