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
import groovy.sql.Sql
import org.apache.commons.lang.StringEscapeUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.jobs.BuildSummaryTablesJob
import org.pih.warehouse.jobs.CalculateQuantityJob
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatus
import org.pih.warehouse.util.LocalizationUtil

import java.text.SimpleDateFormat

class DashboardController {

	def orderService
	def shipmentService
	def inventoryService
	def productService
    def requisitionService
	def dashboardService
    def sessionFactory
	
	def showCacheStatistics = {
		def statistics = sessionFactory.statistics
		log.info(statistics)
		render statistics
	}

    def showRequisitionStatistics = {
        def user = User.get(session.user.id)
        def location = Location.get(session?.warehouse?.id);
        def statistics = requisitionService.getRequisitionStatistics(location,null,user)
        render statistics as JSON
    }

    def showRequisitionMadeStatistics = {
        def user = User.get(session.user.id)
        def location = Location.get(session?.warehouse?.id);
        def statistics = requisitionService.getRequisitionStatistics(null,location,user)
        render statistics as JSON
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
		
		def requisition = Requisition.findByRequestNumber(params.searchTerms)
		if (requisition) {
			redirect(controller: "requisition", action: "show", id: requisition.id)
			return;
		}
		
		def shipment = Shipment.findByShipmentNumber(params.searchTerms)
		if (shipment) {
			redirect(controller: "shipment", action: "showDetails", id: shipment.id)
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
			log.info "Location not selected, redirect to chooseLocation"	
			redirect(action: "chooseLocation")			
		}
		
	    def currentUser = User.get(session?.user?.id)

        def start = new Date() - 7, end = new Date()

		def location = Location.get(session?.warehouse?.id);
		def recentOutgoingShipments = shipmentService.getRecentOutgoingShipments(location?.id, start, end)
		def recentIncomingShipments = shipmentService.getRecentIncomingShipments(location?.id, start, end)
        def outgoingShipmentsByStatus = shipmentService.getShipmentsByStatus(recentOutgoingShipments)
        def incomingShipmentsByStatus = shipmentService.getShipmentsByStatus(recentIncomingShipments)

        // Days to include for activity list
        int daysToInclude = params.daysToInclude?Integer.parseInt(params.daysToInclude):3

        def user = params.onlyShowMine?currentUser:null
        def requisitionStatistics =
                requisitionService.getRequisitionStatistics(location, null, user, start, end)

        def tags = productService?.getAllTags()?.sort { it.tag }
        def rootCategory = productService.getRootCategory()

        log.info "dashboard.index Response time: " + (System.currentTimeMillis() - startTime) + " ms"
		//def outgoingOrders = orderService.getOutgoingOrders(location)
		//def incomingOrders = orderService.getIncomingOrders(location)
		[
            start: start,
            end: end,
			rootCategory : rootCategory,
            requisitionStatistics: requisitionStatistics,
            requisitions: [],
            //requisitions:  requisitionService.getAllRequisitions(session.warehouse),
            //outgoingOrdersByStatus: orderService.getOrdersByStatus(outgoingOrders),
            //incomingOrdersByStatus: orderService.getOrdersByStatus(incomingOrders),
            outgoingShipmentsByStatus : outgoingShipmentsByStatus,
            incomingShipmentsByStatus : incomingShipmentsByStatus,
			daysToInclude: daysToInclude,
            tags:tags
		]
	}

    @Cacheable("dashboardCache")
    def recentActivities = {
        def errorMessage, message
        def location = Location.get(session.warehouse.id)
        int daysToInclude = params.daysToInclude?Integer.parseInt(params.daysToInclude):7
        def recentActivities = dashboardService.getRecentActivities(location, daysToInclude)
        if (!recentActivities) {
            errorMessage = "${g.message(code:'dashboard.noActivityFound.message')}"
        }

        int recentActivitiesTotal = 0, startIndex = 0, endIndex = 0
        if (recentActivities) {
            recentActivitiesTotal = recentActivities.size()
//            startIndex = params.offset ? Integer.valueOf(params.offset):0
//            endIndex = (startIndex + (params.max?Integer.valueOf(params.max):10))
//            if (endIndex > recentActivitiesTotal) endIndex = recentActivitiesTotal
//            endIndex -= 1
//            recentActivities = recentActivities[startIndex..endIndex]
        }

        recentActivities.eachWithIndex { activity, index ->
            activity.styleClass = index % 2 ? "even" : "odd"
        }

        message = "${g.message(code:'dashboard.showing.message', args: [recentActivitiesTotal,daysToInclude])}"

        render ([recentActivities:recentActivities, errorMessage:errorMessage, message:message] as JSON)
    }


    def rebuildSummaryTables = {
        BuildSummaryTablesJob.triggerNow([force:true])
        CalculateQuantityJob.triggerNow()

        flash.message = "Rebuilding summary tables. This might take a minute or two ..."
        redirect(action: "index")
    }

    @Cacheable("dashboardCache")
    def productSummary = {
        def results = [:]
        try {
            def location = Location.get(session.warehouse.id)
            results = dashboardService.getProductSummary(location)
        } catch (Exception e) {
            results.error = true
            results.message = e.message
        }
        render(results as JSON)
    }


    @Cacheable("dashboardCache")
    def expirationSummary = {
        def results = [:]
        try {
            def location = Location.get(session.warehouse.id)
            results = dashboardService.getExpirationSummary(location)
        } catch (Exception e) {
            results.error = true
            results.message = e.message
        }
        render results as JSON
    }

    @Cacheable("dashboardCache")
    def expirationDetails = {
        def location = Location.get(session.warehouse.id)
        def results = dashboardService.getExpirationDetails(location)
        render results as JSON
    }


    def hideTag = {
        Tag tag = Tag.get(params.id)
        tag.isActive = false
        tag.save(flush:true)
        redirect(controller: "dashboard", action: "index", params: [editTags:true])
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

        def requisitionStatistics = [:] //requisitionService.getRequisitionStatistics(location,null,user)

		def category = productService.getRootCategory()
		def categories = category.categories
		categories = categories.groupBy { it?.parentCategory }
		[
			categories: categories,
			quickCategories:productService.getQuickCategories(),
            requisitionStatistics:requisitionStatistics,
			tags:productService.getAllTags()
		]
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

    @CacheFlush(["dashboardCache", "megamenuCache"])
    def flushCache = {
        flash.message = "Cache has been flushed"
        redirect(action: "index")
    }

	def chooseLayout = {
		if (params.layout) { 
			session.layout = params.layout
		}
		redirect(controller:'dashboard', action:'index')
	}
	
	def chooseLocation = {
		log.info "Choose location: " + params
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
			//List warehouses = Location.findAllWhere("active":true)
			render(view: "chooseLocation")
		}
		
	}

    def downloadGenericProductSummaryAsCsv = {
        def location = Location.get(session?.warehouse?.id)
        def genericProductSummary = inventoryService.getGenericProductSummary(location)
        def data = (params.status == "ALL") ? genericProductSummary.values().flatten() : genericProductSummary[params.status]

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
        response.setHeader("Content-disposition", "attachment; filename='GenericProductSummary-${params.status}-${location.name}-${new Date().format("yyyyMMdd-hhmm")}.csv'")
        render(contentType: "text/csv", text:sw.toString())
        return;
    }

    /**
     * Dashboard > Fast movers
     */

    @Cacheable("dashboardCache")
    def fastMovers = {
        log.info "fastMovers: " + params
        def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
        def date = new Date()
        if (params.date) {
            date = dateFormat.parse(params.date)
            date.clearTime()
        }
        def location = Location.get(params?.location?.id?:session?.warehouse?.id)
        log.info "fast movers: " + location
        def data = dashboardService.getFastMovers(location, date, params.max as int)

        render ([aaData: data?.results?:[], message: data.message] as JSON)
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

        def data = dashboardService.getFastMovers(location, date, params.max)
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
        response.setHeader("Content-disposition", "attachment; filename='FastMovers-${location.name}-${new Date().format("yyyyMMdd-hhmm")}.csv'")
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