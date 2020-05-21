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
import org.pih.warehouse.jobs.RefreshInventorySnapshotJob
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Shipment

import java.text.SimpleDateFormat

class DashboardController {

    def shipmentService
    def inventoryService
    def dashboardService
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
            return
        }

        def product = Product.findByProductCodeOrId(params.searchTerms, params.searchTerms)
        if (product) {
            redirect(controller: "inventoryItem", action: "showStockCard", id: product.id)
            return
        }

        def inventoryItem = InventoryItem.findByLotNumber(params.searchTerms)
        if (inventoryItem) {
            redirect(controller: "inventoryItem", action: "showStockCard", id: inventoryItem?.product?.id)
            return
        }

        def requisition = Requisition.findByRequestNumber(params.searchTerms)
        if (requisition) {
            if (requisition.type == RequisitionType.DEFAULT) {
                redirect(controller: "stockMovement", action: "show", id: requisition.id)
            } else {
                redirect(controller: "requisition", action: "show", id: requisition.id)
            }
            return
        }

        def shipment = Shipment.findByShipmentNumber(params.searchTerms)
        if (shipment) {
            if (shipment?.isStockMovement()) {
                redirect(controller: "stockMovement", action: "show", id: shipment?.requisition?.id)
            } else {
                redirect(controller: "shipment", action: "showDetails", id: shipment.id)
            }
            return
        }

        def receipt = Receipt.findByReceiptNumber(params.searchTerms)
        if (receipt) {
            redirect(controller: "receipt", action: "show", id: receipt.id)
            return
        }
        def order = Order.findByOrderNumber(params.searchTerms)
        if (order) {
            redirect(controller: "order", action: "show", id: order.id)
            return
        }

        redirect(controller: "inventory", action: "browse", params: params)

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

    def index = {

        def startTime = System.currentTimeMillis()
        if (!session.warehouse) {
            redirect(action: "chooseLocation")
        }

        def currentUser = User.get(session?.user?.id)
        def location = Location.get(session?.warehouse?.id)
        def recentOutgoingShipments = shipmentService.getRecentOutgoingShipments(location?.id, 7, 7)
        def recentIncomingShipments = shipmentService.getRecentIncomingShipments(location?.id, 7, 7)

        log.info "dashboard.index Response time: " + (System.currentTimeMillis() - startTime) + " ms"

        def newsItems = ConfigurationHolder.config.openboxes.dashboard.newsSummary.newsItems


        [
                newsItems                : newsItems,
                rootCategory             : productService.getRootCategory(),
                requisitionStatistics    : requisitionService.getRequisitionStatistics(location, null, params.onlyShowMine ? currentUser : null, null, [RequisitionStatus.ISSUED, RequisitionStatus.CANCELED] as List),
                requisitions             : [],
                outgoingShipmentsByStatus: shipmentService.getShipmentsByStatus(recentOutgoingShipments),
                incomingShipmentsByStatus: shipmentService.getShipmentsByStatus(recentIncomingShipments),
                tags                     : productService?.getPopularTags(50),
                catalogs                 : productService?.getAllCatalogs()
        ]
    }


    def expirationSummary = {
        def location = Location.get(session.warehouse.id)
        def results = dashboardService.getExpirationSummary(location)

        render results as JSON
    }

    def hideTag = {
        Tag tag = Tag.get(params.id)
        tag.isActive = false
        tag.save(flush: true)
        redirect(controller: "dashboard", action: "index", params: [editTags: true])
    }

    def hideCatalog = {
        ProductCatalog productCatalog = ProductCatalog.get(params.id)
        productCatalog.isActive = false
        productCatalog.save(flush: true)
        redirect(controller: "dashboard", action: "index", params: [editCatalogs: true])
    }

    def status = {
        def admin = User.get(1)
        def comments = Comment.findAllBySenderAndRecipient(admin, admin)

        def results = comments.collect {
            if (it.dateSent > new Date()) {
                [id: it.id, comment: warehouse.message(code: it.comment, args: [format.datetime(obj: it.dateSent)]), dateSent: it.dateSent]
            }
        }
        render results as JSON
    }

    def megamenu = {

        def user = User.get(session?.user?.id)
        def location = Location.get(session?.warehouse?.id)

        // Inbound Shipments
        def inboundShipmentsTotal = Shipment.countByDestination(location)
        def inboundShipmentsCount = Shipment.executeQuery(
                """	select shipment.currentStatus, count(*) 
							from Shipment as shipment
							where shipment.destination = :destination
							group by shipment.currentStatus""", [destination: location])

        inboundShipmentsCount = inboundShipmentsCount.collect { [status: it[0], count: it[1]] }

        // Outbound Shipments
        def outboundShipmentsTotal = Shipment.countByOrigin(location)
        def outboundShipmentsCount = Shipment.executeQuery(
                """	select shipment.currentStatus, count(*) 
							from Shipment as shipment 
							where shipment.origin = :origin 
							group by shipment.currentStatus""", [origin: location])

        outboundShipmentsCount = outboundShipmentsCount.collect { [status: it[0], count: it[1]] }

        // Orders
        def incomingOrders = Order.executeQuery('select o.status, count(*) as count from Order as o where o.destination = ? group by o.status', [location])

        // Requisitions
        def requisitionStatistics = requisitionService.getRequisitionStatistics(location, null, user, new Date() - 30)

        def categories = []
        def category = productService.getRootCategory()
        categories = category.categories
        categories = categories.groupBy { it?.parentCategory }

        [
                categories            : categories,
                isSuperuser           : userService.isSuperuser(session?.user),
                megamenuConfig        : grailsApplication.config.openboxes.megamenu,
                inboundShipmentsTotal : inboundShipmentsTotal ?: 0,
                inboundShipmentsCount : inboundShipmentsCount,
                outboundShipmentsTotal: outboundShipmentsTotal ?: 0,
                outboundShipmentsCount: outboundShipmentsCount,
                incomingOrders        : incomingOrders,
                requisitionStatistics : requisitionStatistics,
                quickCategories       : productService.getQuickCategories()
        ]
    }

    @CacheFlush(["dashboardCache", "megamenuCache", "inventoryBrowserCache", "fastMoversCache",
            "binLocationReportCache", "binLocationSummaryCache", "quantityOnHandCache", "selectTagCache",
            "selectTagsCache", "selectCategoryCache", "selectCatalogsCache"])
    def flushCache = {
        flash.message = "Data caches have been flushed and inventory snapshot job was triggered"
        RefreshInventorySnapshotJob.triggerNow([location: session.warehouse.id, user: session.user.id, forceRefresh: false])
        redirect(action: "index")
    }

    def chooseLayout = {
        if (params.layout) {
            session.layout = params.layout
        }
        redirect(controller: 'dashboard', action: 'index')
    }

    def chooseLocation = {

        // If the user has selected a new location from the topnav bar, we need
        // to retrieve the location to make sure it exists
        User user = User.get(session.user.id)
        Location warehouse = params.id ? Location.get(params.id) : null

        // If a warehouse has been selected
        if (warehouse) {

            // Save the warehouse selection to the session
            session.warehouse = warehouse

            // Save the warehouse selection for "last logged into" information
            if (user) {
                //userInstance.rememberLastLocation = Boolean.valueOf(params.rememberLastLocation)
                user.lastLoginDate = new Date()
                user.warehouse = warehouse
                user.save(flush: true)
                session.user = user
            }

            // Successfully logged in and selected a warehouse
            // Try to redirect to the previous action before session timeout
            if (session.targetUri || params.targetUri) {
                def targetUri = params.targetUri ?: session.targetUri
                if (targetUri && !targetUri.contains("chooseLocation") && !targetUri.contains("errors")) {
                    redirect(uri: targetUri)
                    return
                }
            }
            redirect(controller: 'dashboard', action: 'index')
            return
        }

        [savedLocations: [user.warehouse], loginLocationsMap: locationService.getLoginLocationsMap(user, warehouse)]
    }


    def changeLocation = {
        User user = User.get(session.user.id)
        Map loginLocationsMap = locationService.getLoginLocationsMap(user, null)
        List savedLocations = [user?.warehouse, session?.warehouse].unique()
        render(template: "loginLocations", model: [savedLocations: savedLocations, loginLocationsMap: loginLocationsMap])
    }

    def downloadGenericProductSummaryAsCsv = {
        def location = Location.get(session?.warehouse?.id)
        def genericProductSummary = inventoryService.getGenericProductSummary(location)

        def data = (params.status == "ALL") ?
                genericProductSummary.values().flatten() :
                genericProductSummary[params.status]

        // Rename columns and filter out debugging columns
        data = data.collect {
            ["Status"         : it.status,
             "Generic Product": it.name,
             "Minimum Qty"    : it.minQuantity,
             "Reorder Qty"    : it.reorderQuantity,
             "Maximum Qty"    : it.maxQuantity,
             "Available Qty"  : it.currentQuantity]
        }

        def sw = new StringWriter()
        if (data) {
            def columns = data[0].keySet().collect { value -> StringEscapeUtils.escapeCsv(value) }
            sw.append(columns.join(",")).append("\n")
            data.each { row ->
                def values = row.values().collect { value ->
                    if (value?.toString()?.isNumber()) {
                        value
                    } else if (value instanceof Collection) {
                        StringEscapeUtils.escapeCsv(value.toString())
                    } else {
                        StringEscapeUtils.escapeCsv(value.toString())
                    }
                }
                sw.append(values.join(","))
                sw.append("\n")
            }
        }
        response.setHeader("Content-disposition", "attachment; filename=\"GenericProductSummary-${params.status}-${location.name}-${new Date().format("yyyyMMdd-hhmm")}.csv\"")
        render(contentType: "text/csv", text: sw.toString())
        return
    }

    def downloadFastMoversAsCsv = {
        println "exportFastMoversAsCsv: " + params
        def location = Location.get(params?.location?.id ?: session?.warehouse?.id)

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
                    } else if (value instanceof Collection) {
                        StringEscapeUtils.escapeCsv(value.toString())
                    } else {
                        StringEscapeUtils.escapeCsv(value.toString())
                    }
                }
                sw.append(values?.join(","))
                sw.append("\n")
            }
        } else {
            sw.append("${warehouse.message(code: 'fastMovers.empty.message')}")
        }
        response.setHeader("Content-disposition", "attachment; filename=\"FastMovers-${location.name}-${new Date().format("yyyyMMdd-hhmm")}.csv\"")
        render(contentType: "text/csv", text: sw.toString())
        return
    }

}


