/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.user

import grails.converters.JSON
import grails.plugin.springcache.annotations.CacheFlush
import grails.plugin.springcache.annotations.Cacheable
import org.apache.commons.lang.StringEscapeUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.jobs.CalculateQuantityJob
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.util.LocalizationUtil

import java.text.SimpleDateFormat

class DashboardController {

	def orderService
	def shipmentService
	def inventoryService
	def productService
    def requisitionService
	def userService
	def sessionFactory
	def grailsApplication
	def locationService

	def showCacheStatistics = {
		def statistics = sessionFactory.statistics
		log.info(statistics)
		render statistics
	}


    def globalSearch = {

		def transaction = Transaction.findByTransactionNumber(params.searchTerms)
		if (transaction) {
			redirect(controller: "inventory", action: "showTransaction", id: transaction.id)
			return;
		}

		def product = Product.findByProductCodeOrId(params.searchTerms, params.searchTerms)
		if (product) {
			redirect(controller: "inventoryItem", action: "showStockCard", id: product.id)
			return;
		}

		def inventoryItem = InventoryItem.findByLotNumber(params.searchTerms)
		if (inventoryItem) {
			redirect(controller: "inventoryItem", action: "showStockCard", id: inventoryItem?.product?.id)
			return;
		}

		def requisition = Requisition.findByRequestNumber(params.searchTerms)
		if (requisition) {
			if (requisition.type == RequisitionType.DEFAULT) {
				redirect(controller: "stockMovement", action: "show", id: requisition.id)
			}
			else {
				redirect(controller: "requisition", action: "show", id: requisition.id)
			}
			return;
		}

		def shipment = Shipment.findByShipmentNumber(params.searchTerms)
		if (shipment) {
            if (shipment?.isStockMovement()) {
                redirect(controller: "stockMovement", action: "show", id: shipment?.requisition?.id)
            }
            else {
                redirect(controller: "shipment", action: "showDetails", id: shipment.id)
            }
			return;
		}

		def receipt = Receipt.findByReceiptNumber(params.searchTerms)
		if (receipt) {
			redirect(controller: "receipt", action: "show", id: receipt.id)
			return;
		}
        def order = Order.findByOrderNumber(params.searchTerms)
        if (order) {
            redirect(controller: "order", action: "show", id: order.id)
            return;
        }

		redirect(controller: "inventory", action: "browse", params:params)

	}
    def throwException = {
        println "Configuration: " + ConfigurationHolder.config.grails
        println "Configuration: " + ConfigurationHolder.config.grails.mail
        try {
            throw new RuntimeException("error of some kind")
        } catch (RuntimeException e) {
            log.error("Caught runtime exception: ${e.message}", e)
            throw new RuntimeException("another exception wrapped in this exception", e)
        }
    }

    //@Cacheable("dashboardControllerCache")
	def index = {

        def startTime = System.currentTimeMillis()
		if (!session.warehouse) {
			redirect(action: "chooseLocation")
		}

	    def currentUser = User.get(session?.user?.id)

		def location = Location.get(session?.warehouse?.id);
		def recentOutgoingShipments = shipmentService.getRecentOutgoingShipments(location?.id, 7, 7)
		def recentIncomingShipments = shipmentService.getRecentIncomingShipments(location?.id, 7, 7)
		//def allOutgoingShipments = shipmentService.getShipmentsByOrigin(location)
		//def allIncomingShipments = shipmentService.getShipmentsByDestination(location)

		//def expiredStock = inventoryService.getExpiredStock(null, location)
		//def expiringStockWithin30Days = inventoryService.getExpiringStock(null, location, 30)
		//def expiringStockWithin90Days = inventoryService.getExpiringStock(null, location, 90)
		//def expiringStockWithin180Days = inventoryService.getExpiringStock(null, location, 180)
		//def expiringStockWithin365Days = inventoryService.getExpiringStock(null, location, 365)
		//def lowStock = inventoryService.getLowStock(location)
		//def reorderStock = inventoryService.getReorderStock(location)

        // Days to include for activity list
        def daysToInclude = params.daysToInclude?Integer.parseInt(params.daysToInclude):3
        def activityList = []

        // Find recent requisition activity
        def requisitions = Requisition.executeQuery("""select distinct r from Requisition r where (r.isTemplate = false or r.isTemplate is null) and r.lastUpdated >= :lastUpdated and (r.origin = :origin or r.destination = :destination)""",
                ['lastUpdated':new Date()-daysToInclude, 'origin':location, 'destination': location])
        requisitions.each {
            def link = "${createLink(controller: 'requisition', action: 'show', id: it.id)}"
            def user = (it.dateCreated == it.lastUpdated) ? it?.createdBy : it?.updatedBy
            def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
            def username = user?.name ?: "${warehouse.message(code: 'default.nobody.label', default: 'nobody')}"
            activityType = "${warehouse.message(code: activityType)}"
            activityList << new DashboardActivityCommand(
                    type: "basket",
                    label: "${warehouse.message(code:'dashboard.activity.requisition.label', args: [link, it.name, activityType, username])}",
                    url: link,
                    dateCreated: it.dateCreated,
                    lastUpdated: it.lastUpdated,
                    requisition: it)
        }

        // Add recent shipments
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

		def users = User.executeQuery( "select distinct u from User u where u.lastUpdated >= :lastUpdated", ['lastUpdated':new Date()-daysToInclude], [max: 10] );
		users.each {
			def link = "${createLink(controller: 'user', action: 'show', id: it.id)}"
			def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
			if (it.lastUpdated == it.lastLoginDate) {
				activityType = "dashboard.activity.loggedIn.label"
			}
			activityType = "${warehouse.message(code: activityType)}"


			activityList << new DashboardActivityCommand(
				type: "user",
				label: "${warehouse.message(code:'dashboard.activity.user.label', args: [link, it?.name, activityType])}",
				url: link,
				dateCreated: it.dateCreated,
				lastUpdated: it.lastUpdated,
				user: it)
		}

		//activityList = activityList.groupBy { it.lastUpdated }
        def activityListTotal = 0
		def startIndex = 0
        def endIndex = 0
        if (activityList) {
            activityList = activityList.sort { it.lastUpdated }.reverse()
            activityListTotal = activityList.size()
            startIndex = params.offset?Integer.valueOf(params.offset):0
            endIndex = (startIndex + (params.max?Integer.valueOf(params.max):10))
            if (endIndex > activityListTotal) endIndex = activityListTotal
            endIndex -= 1
    		activityList = activityList[startIndex..endIndex]
        }


        log.info "dashboard.index Response time: " + (System.currentTimeMillis() - startTime) + " ms"
		//def outgoingOrders = orderService.getOutgoingOrders(location)
		//def incomingOrders = orderService.getIncomingOrders(location)

		def newsItems = ConfigurationHolder.config.openboxes.dashboard.newsSummary.newsItems


		[
				newsItems                : newsItems,
				rootCategory             : productService.getRootCategory(),
				requisitionStatistics    : requisitionService.getRequisitionStatistics(location, null, params.onlyShowMine ? currentUser : null, null, [RequisitionStatus.ISSUED, RequisitionStatus.CANCELED] as List),
				requisitions             : [],
				outgoingShipmentsByStatus: shipmentService.getShipmentsByStatus(recentOutgoingShipments),
				incomingShipmentsByStatus: shipmentService.getShipmentsByStatus(recentIncomingShipments),
				activityList             : activityList,
				activityListTotal        : activityListTotal,
				startIndex               : startIndex,
				endIndex                 : endIndex,
				daysToInclude            : daysToInclude,
				tags                     : productService?.getPopularTags(50),
				catalogs                 : productService?.getAllCatalogs()
		]
	}



    def expirationSummary = {
        def location = Location.get(session.warehouse.id)
        def results = inventoryService.getExpirationSummary(location)

        render results as JSON
    }

    def hideTag = {
        Tag tag = Tag.get(params.id)
        tag.isActive = false
        tag.save(flush:true)
        redirect(controller: "dashboard", action: "index", params: [editTags:true])
    }

	def hideCatalog = {
		ProductCatalog productCatalog = ProductCatalog.get(params.id)
		productCatalog.isActive = false
		productCatalog.save(flush:true)
		redirect(controller: "dashboard", action: "index", params: [editCatalogs:true])
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

    @Cacheable("megamenuCache")
	def megamenu = {

        def user = User.get(session?.user?.id)
        def location = Location.get(session?.warehouse?.id)

		//def startTime = System.currentTimeMillis()

        // Inbound Shipments
		def inboundShipmentsTotal = Shipment.countByDestination(location)
		def inboundShipmentsCount = Shipment.executeQuery(
				"""	select shipment.currentStatus, count(*) 
							from Shipment as shipment
							where shipment.destination = :destination
							group by shipment.currentStatus""", [destination:location])

		inboundShipmentsCount = inboundShipmentsCount.collect { [status: it[0], count: it[1]] }

		// Outbound Shipments
		def outboundShipmentsTotal = Shipment.countByOrigin(location)
		def outboundShipmentsCount = Shipment.executeQuery(
				"""	select shipment.currentStatus, count(*) 
							from Shipment as shipment 
							where shipment.origin = :origin 
							group by shipment.currentStatus""", [origin:location])

		outboundShipmentsCount = outboundShipmentsCount.collect { [status: it[0], count: it[1]] }

		// Orders
		def incomingOrders = Order.executeQuery('select o.status, count(*) as count from Order as o where o.destination = ? group by o.status', [location])

        // Requisitions
        def requisitionStatistics = requisitionService.getRequisitionStatistics(location, null, user, new Date()-30)

        def categories = []
		def category = productService.getRootCategory()
		categories = category.categories
		categories = categories.groupBy { it?.parentCategory }

		[
				categories            : categories,
				isSuperuser			  : userService.isSuperuser(session?.user),
				megamenuConfig        : grailsApplication.config.openboxes.megamenu,
				inboundShipmentsTotal : inboundShipmentsTotal ?: 0,
				inboundShipmentsCount : inboundShipmentsCount,
				outboundShipmentsTotal: outboundShipmentsTotal ?: 0,
				outboundShipmentsCount: outboundShipmentsCount,
				incomingOrders        : incomingOrders,
				requisitionStatistics : requisitionStatistics,
				quickCategories       : productService.getQuickCategories(),
				tags                  : productService.getAllTags()
		]
	}

    @CacheFlush(["dashboardCache", "megamenuCache", "inventoryBrowserCache", "fastMoversCache",
			"binLocationReportCache", "binLocationSummaryCache", "quantityOnHandCache", "selectTagCache",
			"selectTagsCache", "selectCategoryCache", "selectCatalogsCache"])
    def flushCache = {
        flash.message = "All data caches have been flushed"
        CalculateQuantityJob.triggerNow([locationId: session.warehouse.id])
        redirect(action: "index")
    }

    @CacheFlush(["megamenuCache"])
    def flushMegamenu = {
        flash.message = "${g.message(code:'dashboard.cacheFlush.message', args: [g.message(code: 'dashboard.megamenu.label')])}"
        redirect(action: "index")
    }

	def chooseLayout = {
		if (params.layout) {
			session.layout = params.layout
		}
		redirect(controller:'dashboard', action:'index')
	}

	def chooseLocation = {

		// If the user has selected a new location from the topnav bar, we need
		// to retrieve the location to make sure it exists
		User user = User.get(session.user.id);
		Location warehouse = params.id ? Location.get(params.id) : null

		// If a warehouse has been selected
		if (warehouse) {

			// Save the warehouse selection to the session
			session.warehouse = warehouse;

			// Save the warehouse selection for "last logged into" information
			if (user) {
				//userInstance.rememberLastLocation = Boolean.valueOf(params.rememberLastLocation)
				user.lastLoginDate = new Date();
				user.warehouse = warehouse
				user.save(flush:true);
				session.user = user;
			}

			// Successfully logged in and selected a warehouse
			// Try to redirect to the previous action before session timeout
			if (session.targetUri || params.targetUri) {
				def targetUri = params.targetUri ?: session.targetUri
				if (targetUri && !targetUri.contains("chooseLocation") && !targetUri.contains("errors")) {
					redirect(uri: targetUri);
					return;
				}
			}
			redirect(controller:'dashboard', action:'index')
			return
		}

		[savedLocations: [user.warehouse], loginLocationsMap:locationService.getLoginLocationsMap(user, warehouse)]
	}


	def changeLocation = {
		User user = User.get(session.user.id);
		Map loginLocationsMap = locationService.getLoginLocationsMap(user, null)
		List savedLocations = [user?.warehouse, session?.warehouse].unique()
		render(template: "loginLocations", model: [savedLocations: savedLocations, loginLocationsMap:loginLocationsMap])
	}

    def downloadGenericProductSummaryAsCsv = {
        def location = Location.get(session?.warehouse?.id)
        def genericProductSummary = inventoryService.getGenericProductSummary(location)

        def data = (params.status == "ALL") ?
                genericProductSummary.values().flatten() :
                genericProductSummary[params.status]

		// Rename columns and filter out debugging columns
		data = data.collect { ["Status":it.status,
							   "Generic Product":it.name,
							   "Minimum Qty":it.minQuantity,
							   "Reorder Qty":it.reorderQuantity,
							   "Maximum Qty":it.maxQuantity,
							   "Available Qty":it.currentQuantity]}

        def sw = new StringWriter()
        if (data) {
            def columns = data[0].keySet().collect { value -> StringEscapeUtils.escapeCsv(value) }
            sw.append(columns.join(",")).append("\n")
            data.each { row ->
                def values = row.values().collect { value ->
                    if (value?.toString()?.isNumber()) {
                        value
                    }
                    else if (value instanceof Collection) {
                        StringEscapeUtils.escapeCsv(value.toString())
                    }
                    else {
                        StringEscapeUtils.escapeCsv(value.toString())
                    }
                }
                sw.append(values.join(","))
                sw.append("\n")
            }
        }
        response.setHeader("Content-disposition", "attachment; filename=\"GenericProductSummary-${params.status}-${location.name}-${new Date().format("yyyyMMdd-hhmm")}.csv\"")
        render(contentType: "text/csv", text:sw.toString())
        return;
    }

    def downloadFastMoversAsCsv = {
        println "exportFastMoversAsCsv: " + params
        def location = Location.get(params?.location?.id?:session?.warehouse?.id)

        def date = new Date()
        if (params.date) {
            def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
            date = dateFormat.parse(params.date)
            date.clearTime()
        }

        def data = inventoryService.getFastMovers(location, date, params.max)
        def sw = new StringWriter()
        if (data?.results) {
            // Write column headers
            def columns = data?.results[0]?.keySet()?.collect { value -> StringEscapeUtils.escapeCsv(value) }
            sw.append(columns?.join(",")).append("\n")

            // Write all data
            data.results.each { row ->
                def values = row.values().collect { value ->
                    if (value?.toString()?.isNumber()) {
                        value
                    }
                    else if (value instanceof Collection) {
                        StringEscapeUtils.escapeCsv(value.toString())
                    }
                    else {
                        StringEscapeUtils.escapeCsv(value.toString())
                    }
                }
                sw.append(values?.join(","))
                sw.append("\n")
            }
        }
        else {
            sw.append("${warehouse.message(code:'fastMovers.empty.message')}")
        }
        response.setHeader("Content-disposition", "attachment; filename=\"FastMovers-${location.name}-${new Date().format("yyyyMMdd-hhmm")}.csv\"")
        render(contentType: "text/csv", text:sw.toString())
        return;
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
    Requisition requisition
	Product product
	Transaction transaction
	InventoryItem inventoryItem

	Date lastUpdated
	Date dateCreated


	String getActivityType() {
		return lastUpdated == dateCreated ? "created" : "updated"
	}
}
