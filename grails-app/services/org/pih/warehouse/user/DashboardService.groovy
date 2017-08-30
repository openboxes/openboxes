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
import groovy.sql.Sql
import org.hibernate.criterion.CriteriaSpecification
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItemSummary
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.util.LocalizationUtil

class DashboardService {

    def dataSource
    def grailsApplication
    def inventoryService
    def productService

    boolean transactional = false

    /**
     * Get the recent activities for the given location to be displayed on the dashboard.
     *
     * @param location
     * @param daysToInclude
     * @return
     */
    def getRecentActivities(Location location, int daysToInclude) {

        def activityList = []
        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
        def format = grailsApplication.mainContext.getBean('org.pih.warehouse.FormatTagLib')
        def warehouse = grailsApplication.mainContext.getBean('org.pih.warehouse.MessageTagLib')

        // Find recent requisition activity
        def requisitions = Requisition.executeQuery("""select distinct r from Requisition r where (r.isTemplate = false or r.isTemplate is null) and r.lastUpdated >= :lastUpdated and (r.origin = :origin or r.destination = :destination)""",
                ['lastUpdated':new Date()-daysToInclude, 'origin':location, 'destination': location])
        requisitions.each {
            def link = "${g.createLink(controller: 'requisition', action: 'show', id: it.id)}"
            def user = (it.dateCreated == it.lastUpdated) ? it?.createdBy : it?.updatedBy
            def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
            def username = user?.name ?: "${warehouse.message(code: 'default.nobody.label', default: 'nobody')}"
            activityType = "${warehouse.message(code: activityType)}"
            activityList << [
                    type: "basket",
                    label: "${warehouse.message(code:'dashboard.activity.requisition.label', args: [link, it.name, activityType, username])}",
                    url: link,
                    date: "${format.date(obj:it.lastUpdated,format:'MMM d hh:mma')}",
                    thumbnailUrl: "${g.createLinkTo(dir:'images/icons/silk/basket.png')}",
                    dateCreated: it.dateCreated,
                    lastUpdated: it.lastUpdated]
        }

        // Add recent shipments
        def shipments = Shipment.executeQuery( "select distinct s from Shipment s where s.lastUpdated >= :lastUpdated and \
			(s.origin = :origin or s.destination = :destination)", ['lastUpdated':new Date()-daysToInclude, 'origin':location, 'destination':location] );
        shipments.each {
            def link = "${g.createLink(controller: 'shipment', action: 'showDetails', id: it.id)}"
            def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
            activityType = "${warehouse.message(code: activityType)}"
            activityList << [
                    type: "lorry",
                    label: "${warehouse.message(code:'dashboard.activity.shipment.label', args: [link, it.name, activityType])}",
                    url: link,
                    date: "${format.date(obj:it.lastUpdated,format:'MMM d hh:mma')}",
                    thumbnailUrl: "${g.createLinkTo(dir:'images/icons/silk/lorry.png')}",
                    dateCreated: it.dateCreated,
                    lastUpdated: it.lastUpdated]
        }
        //order by e.createdDate desc
        //[max:params.max.toInteger(), offset:params.offset.toInteger ()]
        def shippedShipments = Shipment.executeQuery("SELECT s FROM Shipment s JOIN s.events e WHERE e.eventDate >= :eventDate and e.eventType.eventCode = 'SHIPPED'", ['eventDate':new Date()-daysToInclude])
        shippedShipments.each {
            def link = "${g.createLink(controller: 'shipment', action: 'showDetails', id: it.id)}"
            def activityType = "dashboard.activity.shipped.label"
            activityType = "${warehouse.message(code: activityType, args: [link, it.name, activityType, it.destination.name])}"
            activityList << [
                    type: "lorry_go",
                    label: activityType,
                    url: link,
                    date: "${format.date(obj:it.lastUpdated,format:'MMM d hh:mma')}",
                    thumbnailUrl: "${g.createLinkTo(dir:'images/icons/silk/lorry_go.png')}",
                    dateCreated: it.dateCreated,
                    lastUpdated: it.lastUpdated]
        }
        def receivedShipment = Shipment.executeQuery("SELECT s FROM Shipment s JOIN s.events e WHERE e.eventDate >= :eventDate and e.eventType.eventCode = 'RECEIVED'", ['eventDate':new Date()-daysToInclude])
        receivedShipment.each {
            def link = "${g.createLink(controller: 'shipment', action: 'showDetails', id: it.id)}"
            def activityType = "dashboard.activity.received.label"
            activityType = "${warehouse.message(code: activityType, args: [link, it.name, activityType, it.origin.name])}"
            activityList << [
                    type: "lorry_stop",
                    label: activityType,
                    url: link,
                    date: "${format.date(obj:it.lastUpdated,format:'MMM d hh:mma')}",
                    thumbnailUrl: "${g.createLinkTo(dir:'images/icons/silk/lorry_stop.png')}",
                    dateCreated: it.dateCreated,
                    lastUpdated: it.lastUpdated]
        }

        def products = Product.executeQuery( "select distinct p from Product p where p.lastUpdated >= :lastUpdated", ['lastUpdated':new Date()-daysToInclude] );
        products.each {
            def link = "${g.createLink(controller: 'inventoryItem', action: 'showStockCard', params:['product.id': it.id])}"
            def user = (it.dateCreated == it.lastUpdated) ? it?.createdBy : it.updatedBy
            def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
            activityType = "${warehouse.message(code: activityType)}"
            def username = user?.name ?: "${warehouse.message(code: 'default.nobody.label', default: 'nobody')}"
            activityList << [
                    type: "package",
                    label: "${warehouse.message(code:'dashboard.activity.product.label', args: [link, it.name, activityType, username])}",
                    url: link,
                    date: "${format.date(obj:it.lastUpdated,format:'MMM d hh:mma')}",
                    thumbnailUrl: "${g.createLinkTo(dir:'images/icons/silk/package.png')}",
                    dateCreated: it.dateCreated,
                    lastUpdated: it.lastUpdated]
        }

        // If the current location has an inventory, add recent transactions associated with that location to the activity list
        if (location?.inventory) {
            def transactions = Transaction.executeQuery("select distinct t from Transaction t where t.lastUpdated >= :lastUpdated and \
				t.inventory = :inventory", ['lastUpdated':new Date()-daysToInclude, 'inventory':location?.inventory] );

            transactions.each {
                def link = "${g.createLink(controller: 'inventory', action: 'showTransaction', id: it.id)}"
                def user = (it.dateCreated == it.lastUpdated) ? it?.createdBy : it?.updatedBy
                def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
                activityType = "${warehouse.message(code: activityType)}"
                def label = LocalizationUtil.getLocalizedString(it)
                def username = user?.name ?: "${warehouse.message(code: 'default.nobody.label', default: 'nobody')}"
                activityList << [
                        type: "arrow_switch_bluegreen",
                        label: "${warehouse.message(code:'dashboard.activity.transaction.label', args: [link, label, activityType, username])}",
                        url: link,
                        date: "${format.date(obj:it.lastUpdated,format:'MMM d hh:mma')}",
                        thumbnailUrl: "${g.createLinkTo(dir:'images/icons/silk/arrow_switch_bluegreen.png')}",
                        dateCreated: it.dateCreated,
                        lastUpdated: it.lastUpdated]
            }
        }

        def users = User.executeQuery( "select distinct u from User u where u.lastUpdated >= :lastUpdated", ['lastUpdated':new Date()-daysToInclude], [max: 10] );
        users.each {
            def link = "${g.createLink(controller: 'user', action: 'show', id: it.id)}"
            def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
            if (it.lastUpdated == it.lastLoginDate) {
                activityType = "dashboard.activity.loggedIn.label"
            }
            activityType = "${warehouse.message(code: activityType)}"


            activityList << [
                    type: "user",
                    label: "${warehouse.message(code:'dashboard.activity.user.label', args: [link, it.name, activityType])}",
                    url: link,
                    date: "${format.date(obj:it.lastUpdated,format:'MMM d hh:mma')}",
                    thumbnailUrl: "${g.createLinkTo(dir:'images/icons/silk/user.png')}",
                    dateCreated: it.dateCreated,
                    lastUpdated: it.lastUpdated]
        }

        activityList = activityList.sort { it.lastUpdated }.reverse()
        return activityList
    }

    def getInventoryItemSummaryLastUpdated(Location location) {
        Sql sql = new Sql(dataSource)
        String query = """
            SELECT 
                MAX(last_updated) as max_last_updated
            FROM inventory_item_summary
            WHERE location_id = :locationId
        """
        def results = sql.rows(query, [locationId:location.id]);
        log.info "results: " + results
        Date lastUpdated = new Date(results[0].max_last_updated.getTime())

        return lastUpdated
    }


    def getCategories() {
        def category = productService.getRootCategory()
        def categories = category.categories
        categories = categories.groupBy { it?.parentCategory }
        return categories
    }

    def getShipmentSummary(Location destination, Location origin, Date startDate, Date endDate) {

        def criteria = Shipment.createCriteria()
        def results = criteria.list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                countDistinct("id", "count")
                groupProperty("currentStatus", "status")
            }

            if (destination) {
                eq("destination", destination)
            }

            if (origin) {
                eq("origin", origin)
            }

            if (startDate && endDate) {
                between("dateCreated", startDate, endDate)
            }
        }

        println "Results ${results}"
        // Convert to map
        def shipmentSummary = [:]
        results.each { shipmentSummary[it.status] = it.count }
        println "shipmentSummary ${shipmentSummary}"
        return shipmentSummary
    }

    def getOrderSummary(Location location) {
        return Order.executeQuery('select o.status, count(*) from Order as o where o.destination = ? group by o.status', [location])
    }


    def getProductSummary(Location location) {
        long startTime = System.currentTimeMillis()
        Sql sql = new Sql(dataSource)

        // Used to generate links
        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')

        def results = sql.rows(productAggregationQuery, [locationId:location.id]);
        def productSummary = results.collect {
            def status = it.status ?: ""
            def styleClass = (!it.status_code && !it.status) ? "total" : (!it.status) ? "subtotal" : "data"
            def labelCode = (!it.status_code && !it.status) ? "total" : (!it.status) ? "subtotal" : it.status
            labelCode = "dashboard.${labelCode}.label"
            def cost = "${g.formatNumber(number: it.cost, type: 'currency')}"
            def url = "${g.createLink(uri: '/inventory/productSummary/' + status)}"
            def label = "${g.message(code: labelCode)}"
            [label: label, code: it.status_code, status: status, count: it.count, cost: cost, url: url, styleClass: styleClass]
        }

        log.info "getProductSummary(): " + (System.currentTimeMillis() - startTime) + " ms"
        return productSummary
    }

    def productDetailsQuery = """
            SELECT
                product.id as id,
                product.product_code as product_code,
                product.name as product_name,
                category.name as category_name,
                product.price_per_unit as price_per_unit,
                product.unit_of_measure as unit_of_measure,
                location.name as location_name,
                sum(quantity) as quantity,
                ifnull(inventory_level.min_quantity, 0) as min_quantity,
                ifnull(inventory_level.reorder_quantity, 0) as reorder_quantity,
                ifnull(inventory_level.max_quantity, 0) as max_quantity
            FROM inventory_item_summary
            JOIN product on inventory_item_summary.product_id = product.id
            JOIN category on product.category_id = category.id 
            JOIN location on inventory_item_summary.location_id = location.id
            JOIN inventory on inventory.id = location.inventory_id
            LEFT OUTER JOIN (
                SELECT 
                    product_id, 
                    inventory_id, 
                    max(min_quantity) as min_quantity, 
                    max(max_quantity) as max_quantity, 
                    max(reorder_quantity) as reorder_quantity,
                    max(date_created) as date_created
                FROM inventory_level
                WHERE inventory_level.status IS NULL OR inventory_level.status NOT IN ('INACTIVE', 'NOT_SUPPORTED')
                GROUP BY product_id, inventory_id
                ORDER BY date_created 
            ) as inventory_level ON inventory_level.product_id = product.id AND inventory_level.inventory_id = inventory.id
            WHERE location.id = :locationId
            GROUP BY product.id, location.id
        """

    def productSummaryQuery = """
            SELECT *,
                (case when quantity > max_quantity then 'a_normal'
                        when quantity <= max_quantity and quantity > reorder_quantity then 'a_normal'
                        when quantity <= reorder_quantity and quantity > min_quantity then 'b_warning'
                        when quantity <= min_quantity and quantity > 0 then 'b_warning'
                        when quantity <= 0 then 'c_danger'
                        end) as status_code
                ,
                (case when quantity > max_quantity then 'overstock'
                        when quantity <= max_quantity and quantity > reorder_quantity then 'healthy'
                        when quantity <= reorder_quantity and quantity > min_quantity then 'reorder'
                        when quantity <= min_quantity and quantity > 0 then 'low'
                        when quantity <= 0 then 'outofstock'
                        end) as status
                ,
                (case when quantity > max_quantity then '0'
                        when quantity <= max_quantity and quantity > reorder_quantity then '1'
                        when quantity <= reorder_quantity and quantity > min_quantity then '2'
                        when quantity <= min_quantity and quantity > 0 then '3'
                        when quantity <= 0 then '4'
                        end) as sort_order
                , 
                (quantity * price_per_unit) as total_cost

            FROM (
                ${productDetailsQuery}
            ) as product_details 
        """

    def productAggregationQuery = """
        SELECT status_code, status, count(*) as count, ceiling(sum(total_cost)) as cost
        FROM ( 
            ${productSummaryQuery}
        ) as product_summary
        GROUP BY status_code, status WITH ROLLUP
    """


    /**
     *
     * @param location
     * @return
     */
    def getProductDetails(Location location, String status) {
        def dashboardAlerts = []
        long startTime = System.currentTimeMillis()
        Sql sql = new Sql(dataSource)

        def query = productSummaryQuery
        if (status) {
            query += " having status = :status"
        }

        def results = sql.rows(query, [locationId:location.id, status: status]);

        def productDetailsList = results.collect { [
                id: it.id,
                status: it.status,
                statusCode: it.status_code,
                productCode: it.product_code,
                productName: it.product_name,
                unitOfMeasure: it.unit_of_measure,
                genericProductName: "",
                categoryName: it.category_name,
                manufacturer: "",
                manufacturerCode: "",
                vendor: "",
                vendorCode: "",
                binLocation: "",
                abcClass: "",
                quantity: it.quantity,
                pricePerUnit: it.price_per_unit,
                totalCost: it.total_cost,
                minQuantity: 0,
                reorderQuantity: 0,
                maxQuantity: 0
        ] }


        log.info "quantityMap(): " + (System.currentTimeMillis() - startTime) + " ms"
        return productDetailsList
    }





    String expirationDetailsQuery = """
        SELECT
            product.id as product_id,
            product.product_code as product_code,
            product.name as product_name,
            category.name as category_name,
            inventory_item.lot_number as lot_number,
            inventory_item.expiration_date as expiration_date,
            datediff(inventory_item.expiration_date, now()) as days_until_expiry,
            product.price_per_unit as price_per_unit,
            product.unit_of_measure as unit_of_measure,
            location.id as location_id,
            location.name as location_name,
            sum(quantity) as quantity,            
            ifnull(inventory_level.min_quantity, 0) as min_quantity,
            ifnull(inventory_level.reorder_quantity, 0) as reorder_quantity,
            ifnull(inventory_level.max_quantity, 0) as max_quantity
        FROM inventory_item_summary
        JOIN inventory_item on inventory_item_summary.inventory_item_id = inventory_item.id
        JOIN product on inventory_item_summary.product_id = product.id
        JOIN category on product.category_id = category.id
        JOIN location on inventory_item_summary.location_id = location.id
        JOIN inventory on inventory.id = location.inventory_id
        LEFT OUTER JOIN inventory_level on inventory_level.product_id = product.id AND inventory_level.inventory_id = inventory.id
        WHERE location.id = :locationId 
        AND quantity > 0
        GROUP BY inventory_item.id, product.id, location.id
    """

    final expirationSummaryQuery = """
            SELECT *, 
                (case 
                    when expiration_date is null then 'a_normal'
                    when days_until_expiry <= 0 then 'c_danger'
                    when days_until_expiry <= 30 then 'b_warning'
                    when days_until_expiry <= 60 then 'b_warning'
                    when days_until_expiry <= 90 then 'b_warning'
                    when days_until_expiry <= 180 then 'b_warning'
                    when days_until_expiry <= 365 then 'a_normal'
                    when days_until_expiry > 365 then 'a_normal'
                end) as status_code,            
                (case 
                    when expiration_date is null then 'does_not_expire'
                    when days_until_expiry <= 0 then 'expired'
                    when days_until_expiry <= 30 then 'within_one_month'
                    when days_until_expiry <= 60 then 'within_two_months'
                    when days_until_expiry <= 90 then 'within_three_months'
                    when days_until_expiry <= 180 then 'within_six_months'
                    when days_until_expiry <= 365 then 'within_year'
                    when days_until_expiry > 365 then 'over_year'
                end) as status,
                (quantity * price_per_unit) as total_cost
            FROM (
                ${expirationDetailsQuery}
            ) as expiration_summary
    """

    def expirationAggregationQuery = """
        SELECT status_code, status, count(*) as count, ceiling(sum(total_cost)) as cost
        FROM ( 
            ${expirationSummaryQuery}
        ) as expiration_summary
        GROUP BY status_code, status WITH ROLLUP
    """


    def getExpirationSummary(Location location) {
        long startTime = System.currentTimeMillis()
        Sql sql = new Sql(dataSource)

        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')

        def results = sql.rows(expirationAggregationQuery, [locationId:location.id]);

        def expirationSummary = results.collect {
            def status = it.status ?: ""
            def styleClass = (!it.status_code && !it.status) ? "total" : (!it.status) ? "subtotal" : "data"
            def labelCode = (!it.status_code && !it.status) ? "total" : (!it.status) ? "subtotal" : it.status
            labelCode = "dashboard.${labelCode}.label"
            def cost = "${g.formatNumber(number: it.cost, type: 'currency')}"
            def url = "${g.createLink(uri: '/inventory/inventoryItemSummary/' + status)}"
            //def label = ${g.message(code:'')}
            def label = "${g.message(code: labelCode)}"
            [label: label, code: it.status_code, status: status, count: it.count, cost: cost, url: url, styleClass: styleClass]
        }
        log.info "getExpirationSummary(): " + (System.currentTimeMillis() - startTime) + " ms"
        return expirationSummary
    }


    def getExpirationDetails(Location location, String status) {
        long startTime = System.currentTimeMillis()
        Sql sql = new Sql(dataSource)


        def query = expirationSummaryQuery
        if (status) {
            query += " having status = :status"
        }

        log.info "Query: " + query
        def results = sql.rows(query, [locationId:location.id, status: status]);

        def productDetailsList = results.collect { [
                status: it.status,
                statusCode: it.status_code,
                productId: it.product_id,
                productCode: it.product_code,
                productName: it.product_name,
                unitOfMeasure: it.unit_of_measure,
                lotNumber: it.lot_number,
                expirationDate: it.expiration_date,
                genericProductName: "",
                categoryName: it.category_name,
                manufacturer: "",
                manufacturerCode: "",
                vendor: "",
                vendorCode: "",
                binLocation: "",
                abcClass: "",
                quantity: it.quantity,
                pricePerUnit: it.price_per_unit,
                totalCost: it.total_cost,
                minQuantity: 0,
                reorderQuantity: 0,
                maxQuantity: 0
        ] }



        log.info "getExpirationDetails(): " + (System.currentTimeMillis() - startTime) + " ms"
        return productDetailsList
    }

    /**
     * Returns a list of
     * @param currentLocation
     * @return
    def getQuantityByLocation(Location currentLocation) {

        def results = InventoryItemSummary.createCriteria().list {
            //maxResults 5
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                sum('quantity', 'quantity')
                groupProperty('product.id', 'productId')
            }

            location {
                eq 'id', currentLocation.id
            }
        }

        // Transform result set to map of quantities indexed by product ID
        def map = results.inject([:]) { map, entry ->
            map[entry.productId] = entry.quantity
            return map
        }

        return map
    }
     */

    /**
     * Get fast moving items based on requisition data.
     *
     * @param location
     * @param date
     * @param max
     * @return
     */
    def getFastMovers(location, date, max) {

        log.info "Get fast movers for location ${location}"
        def startTime = System.currentTimeMillis()
        def data = [:]
        try {
            data.location = location.id
            data.startDate = date-30
            data.endDate = date

            def criteria = RequisitionItem.createCriteria()
            def results = criteria.list {
                requisition {
                    eq("destination", location)
                    between("dateRequested", date-30, date)
                }
                projections {
                    groupProperty("product")
                    countDistinct('id', "occurrences")
                    sum("quantity", "quantity")
                }
                order('quantity','desc')
                order('occurrences','desc')
                if (max) { maxResults(max) }
            }

            //log.info "Results: " + results

            def quantityMap = inventoryService.getProductQuantityByLocation(location)

            def count = 1;
            data.results = results.collect {[
                    rank: count++,
                    id: it[0].id,
                    productCode: it[0].productCode,
                    name: it[0].name,
                    category: it[0]?.category?.name?:"",
                    requisitionCount: it[1],
                    quantityRequested: it[2],
                    quantityOnHand: (quantityMap[it[0]]?:0),
                ]
            }
            data.responseTime = (System.currentTimeMillis() - startTime) + " ms"


        } catch (Exception e) {
            log.error("Error occurred while getting requisition items " + e.message, e)
            data.message = e.message
        }
        return data
    }

}
