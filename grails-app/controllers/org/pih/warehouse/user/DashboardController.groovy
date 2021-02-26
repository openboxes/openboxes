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
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.UnitOfMeasureClass
import org.pih.warehouse.core.UnitOfMeasureType
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.jobs.RefreshInventorySnapshotJob
import org.pih.warehouse.jobs.RefreshProductAvailabilityJob
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment

import java.text.SimpleDateFormat

class DashboardController {

    def inventoryService
    def dashboardService
    def productService
    def userService
    def sessionFactory
    def grailsApplication
    def locationService
    def userAgentIdentService

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
            redirect(controller: "stockMovement", action: "show", id: requisition.id)
            return
        }

        def shipment = Shipment.findByShipmentNumber(params.searchTerms)
        if (shipment) {
            redirect(controller: "stockMovement", action: "show", id: shipment.id)
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

    def index = {
        if (userAgentIdentService.isMobile()) {
            redirect(controller: "mobile")
            return
        }

        render(template: "/common/react")
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

    @Cacheable("megamenuCache")
    def megamenu = {
        [
                isSuperuser           : userService.isSuperuser(session?.user),
                megamenuConfig        : grailsApplication.config.openboxes.megamenu,
                quickCategories       : productService.quickCategories,
                categories            : productService.rootCategory.categories.groupBy { it.parentCategory?.id },
        ]
    }

    @CacheFlush(["dashboardCache", "megamenuCache", "inventoryBrowserCache", "fastMoversCache",
            "binLocationReportCache", "binLocationSummaryCache", "quantityOnHandCache", "selectTagCache",
            "selectTagsCache", "selectCategoryCache", "selectCatalogsCache", "forecastCache"])
    def flushCache = {
        flash.message = "Data caches have been flushed and inventory snapshot job was triggered"
        RefreshProductAvailabilityJob.triggerNow([locationId: session.warehouse.id, forceRefresh: true])
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

        if (userAgentIdentService.isMobile()) {
            render (view: "/mobile/chooseLocation",
                    model: [savedLocations: user.warehouse ? [user.warehouse] : null, loginLocationsMap: locationService.getLoginLocationsMap(user, warehouse)])
            return
        }



        [savedLocations: user.warehouse ? [user.warehouse] : null, loginLocationsMap: locationService.getLoginLocationsMap(user, warehouse)]
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


