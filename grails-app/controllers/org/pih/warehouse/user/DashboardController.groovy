/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.user;


import grails.converters.JSON;

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.User;
import org.pih.warehouse.order.Order;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.receiving.Receipt;
import org.pih.warehouse.requisition.Requisition;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.util.LocalizationUtil;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Transaction;


class DashboardController {

	def orderService
	def shipmentService;
	def productService 
    
	
	def globalSearch = { 
		
		def transaction = Transaction.findByTransactionNumber(params.searchTerms)
		if (transaction) { 
			redirect(controller: "inventory", action: "showTransaction", id: transaction.id)
		}
		
		def product = Product.findByProductCodeOrId(params.searchTerms, params.searchTerms)
		if (product) {
			redirect(controller: "inventoryItem", action: "showStockCard", id: product.id)
		}

		redirect(controller: "inventory", action: "browse", params:params)
			
	}
	
	
	def index = {
		if (!session.warehouse) {		
			log.info "Location not selected, redirect to chooseLocation"	
			redirect(action: "chooseLocation")			
		}
		
		
		
		Location location = Location.get(session?.warehouse?.id);
		
		def daysToInclude = 3
		def recentOutgoingShipments = shipmentService.getRecentOutgoingShipments(location?.id)
		def recentIncomingShipments = shipmentService.getRecentIncomingShipments(location?.id)
		def allOutgoingShipments = shipmentService.getShipmentsByOrigin(location)
		def allIncomingShipments = shipmentService.getShipmentsByDestination(location)
		
		def activityList = []
		def shipments = Shipment.executeQuery( "select distinct s from Shipment s where s.lastUpdated >= :lastUpdated and \
			(s.origin = :origin or s.destination = :destination)", ['lastUpdated':new Date()-daysToInclude, 'origin':location, 'destination':location] );
		shipments.each { 
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
		//order by e.createdDate desc
		//[max:params.max.toInteger(), offset:params.offset.toInteger ()]
		def shippedShipments = Shipment.executeQuery("SELECT s FROM Shipment s JOIN s.events e WHERE e.eventDate >= :eventDate and e.eventType.eventCode = 'SHIPPED'", ['eventDate':new Date()-daysToInclude])
		shippedShipments.each {
			def link = "${createLink(controller: 'shipment', action: 'showDetails', id: it.id)}"
			def activityType = "dashboard.activity.shipped.label"
			activityType = "${warehouse.message(code: activityType, args: [link, it.name, activityType, it.destination.name])}"
			activityList << new DashboardActivityCommand(
				type: "lorry_go",
				label: activityType,
				url: link,
				dateCreated: it.dateCreated,
				lastUpdated: it.lastUpdated,
				shipment: it)
		}
		def receivedShipment = Shipment.executeQuery("SELECT s FROM Shipment s JOIN s.events e WHERE e.eventDate >= :eventDate and e.eventType.eventCode = 'RECEIVED'", ['eventDate':new Date()-daysToInclude])
		receivedShipment.each {
			def link = "${createLink(controller: 'shipment', action: 'showDetails', id: it.id)}"
			def activityType = "dashboard.activity.received.label"
			activityType = "${warehouse.message(code: activityType, args: [link, it.name, activityType, it.origin.name])}"
			activityList << new DashboardActivityCommand(
				type: "lorry_stop",
				label: activityType,
				url: link,
				dateCreated: it.dateCreated,
				lastUpdated: it.lastUpdated,
				shipment: it)
		}

		def products = Product.executeQuery( "select distinct p from Product p where p.lastUpdated >= :lastUpdated", ['lastUpdated':new Date()-daysToInclude] );
		products.each { 
			def link = "${createLink(controller: 'inventoryItem', action: 'showStockCard', params:['product.id': it.id])}"
			def user = (it.dateCreated == it.lastUpdated) ? it?.createdBy : it.updatedBy
			def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
			activityType = "${warehouse.message(code: activityType)}"
			def username = user?.name ?: "${warehouse.message(code: 'default.nobody.label', default: 'nobody')}"
			activityList << new DashboardActivityCommand(
				type: "package",
				label: "${warehouse.message(code:'dashboard.activity.product.label', args: [link, it.name, activityType, username])}",
				url: link,
				dateCreated: it.dateCreated,
				lastUpdated: it.lastUpdated,
				product: it)
		}
		
		// If the current location has an inventory, add recent transactions associated with that location to the activity list
		if (location?.inventory) { 
			def transactions = Transaction.executeQuery("select distinct t from Transaction t where t.lastUpdated >= :lastUpdated and \
				t.inventory = :inventory", ['lastUpdated':new Date()-daysToInclude, 'inventory':location?.inventory] );
			
			transactions.each { 
				def link = "${createLink(controller: 'inventory', action: 'showTransaction', id: it.id)}"
				def user = (it.dateCreated == it.lastUpdated) ? it?.createdBy : it?.updatedBy
				def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
				activityType = "${warehouse.message(code: activityType)}"
				def label = LocalizationUtil.getLocalizedString(it)
				def username = user?.name ?: "${warehouse.message(code: 'default.nobody.label', default: 'nobody')}"
				activityList << new DashboardActivityCommand(
					type: "arrow_switch_bluegreen",
					label: "${warehouse.message(code:'dashboard.activity.transaction.label', args: [link, label, activityType, username])}",
					url: link,
					dateCreated: it.dateCreated,
					lastUpdated: it.lastUpdated,
					transaction: it)
			}
		}
				
		def users = User.executeQuery( "select distinct u from User u where u.lastUpdated >= :lastUpdated", ['lastUpdated':new Date()-daysToInclude] );
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
		def activityListTotal = activityList.size()
		//activityList = activityList.groupBy { it.lastUpdated }
		def startIndex = params.offset?Integer.valueOf(params.offset):0
		def endIndex = (startIndex + (params.max?Integer.valueOf(params.max):10))
		if (endIndex > activityListTotal) endIndex = activityListTotal
		endIndex -= 1
		activityList = activityList[startIndex..endIndex]
		
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
			activityList : activityList,
			activityListTotal : activityListTotal,
			startIndex: startIndex,
			endIndex: endIndex,
			daysToInclude: daysToInclude
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
	
	def megamenu = {
		def incomingShipments = Shipment.findAllByDestination(session?.warehouse).groupBy{it.status.code}.sort()
		def outgoingShipments = Shipment.findAllByOrigin(session?.warehouse).groupBy{it.status.code}.sort();
		def incomingOrders = Order.executeQuery('select o.status, count(*) from Order as o where o.destination = ? group by o.status', [session?.warehouse])
		def incomingRequests = Requisition.findAllByDestination(session?.warehouse).groupBy{it.status}.sort()
		def outgoingRequests = Requisition.findAllByOrigin(session?.warehouse).groupBy{it.status}.sort()
		
		def c = Product.createCriteria()
		def categories = c.list { 
			projections { 
				distinct 'category'
			}
		}
		categories = categories.groupBy { it.parentCategory } 
		[
			categories: categories,
			incomingShipments: incomingShipments,
			outgoingShipments: outgoingShipments,
			incomingOrders: incomingOrders,
			incomingRequests: incomingRequests,
			outgoingRequests: outgoingRequests,
			quickCategories:productService.getQuickCategories()]

		
	}
	
	
	def menu = { 
		def incomingShipments = Shipment.findAllByDestination(session?.warehouse).groupBy{it.status.code}.sort()
		def outgoingShipments = Shipment.findAllByOrigin(session?.warehouse).groupBy{it.status.code}.sort();
		def incomingOrders = Order.executeQuery('select o.status, count(*) from Order as o where o.destination = ? group by o.status', [session?.warehouse])
		def incomingRequests = Requisition.findAllByDestination(session?.warehouse).groupBy{it.status}.sort()
		def outgoingRequests = Requisition.findAllByOrigin(session?.warehouse).groupBy{it.status}.sort()
		
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
		
		log.info params
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
				userInstance.rememberLastLocation = Boolean.valueOf(params.rememberLastLocation)
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
				log.info("session.targetUri: " + session.targetUri)
				log.info("params.targetUri: " + params.targetUri)
				def targetUri = params.targetUri ?: session.targetUri 
				log.info("Redirecting to " + targetUri);
				if (targetUri && !targetUri.contains("chooseLocation")) { 
					redirect(uri: targetUri);
					return;
				}
			}
			log.info("Redirecting to dashboard");
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