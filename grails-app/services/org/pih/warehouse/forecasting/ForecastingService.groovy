/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.forecasting

import grails.core.GrailsApplication
import grails.util.Holders
import groovy.sql.Sql
import groovy.time.TimeCategory
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.DateUtil
import org.pih.warehouse.LocalizationUtil

import java.math.RoundingMode
import java.sql.Timestamp
import java.text.DateFormatSymbols
import java.text.NumberFormat
import org.pih.warehouse.core.SynonymTypeCode

class ForecastingService {

    def dataSource
    GrailsApplication grailsApplication
    def productAvailabilityService

    def getDemand(Location origin, Location destination, Product product) {
        boolean forecastingEnabled = Holders.config.openboxes.forecasting.enabled ?: false
        Integer demandPeriod = grailsApplication.config.openboxes.forecasting.demandPeriod ?: 365
        if (forecastingEnabled) {
            Map defaultDateRange = DateUtil.getDateRange(new Date(), -1)
            use(TimeCategory) {
                defaultDateRange.startDate = defaultDateRange.endDate - demandPeriod.days
            }

            def rows = getDemandDetails(origin, destination, product, defaultDateRange.startDate, defaultDateRange.endDate)
            def totalDemand = rows.sum { it.quantity_demand } ?: 0
            def dailyDemand = (totalDemand && demandPeriod) ? (totalDemand / demandPeriod) : 0
            def monthlyDemand = totalDemand / Math.floor((demandPeriod / 30))
            def quantityOnHand = productAvailabilityService.getQuantityOnHand(product, origin)
            def onHandMonths = monthlyDemand ? quantityOnHand / monthlyDemand : 0
            return [
                totalDemand     : totalDemand,
                totalDays       : demandPeriod,
                dailyDemand     : dailyDemand,
                monthlyDemand   : new BigDecimal(monthlyDemand).setScale(0, RoundingMode.HALF_UP),
                onHandMonths    : onHandMonths,
                quantityOnHand  : quantityOnHand
            ]
        }
        return [:]
    }

    def getDemandDetails(Location origin, Product product) {
        Date today = new Date()
        Integer demandPeriod = Holders.config.openboxes.forecasting.demandPeriod ?: 365
        return getDemandDetails(origin, null, product, today - demandPeriod, today)
    }

    def getDemandDetails(Location origin, Location destination, Product product, Date startDate, Date endDate) {
        Locale currentLocale = LocalizationUtil.localizationService.getCurrentLocale()
        List data = []
        boolean forecastingEnabled = Holders.config.openboxes.forecasting.enabled ?: false
        if (forecastingEnabled) {
            Map params = [startDate: startDate, endDate: endDate]
            String productDisplayNameSubQuery = """
                (SELECT s.name FROM synonym s 
                WHERE s.product_id = pdd.product_id 
                AND s.synonym_type_code = '${SynonymTypeCode.DISPLAY_NAME}' 
                AND s.locale = '${currentLocale}'
                LIMIT 1)
            """
            String query = """
                select 
                    request_id,
                    request_item_id,
                    request_status,
                    request_number,
                    DATE_FORMAT(date_issued, '%b %Y') as month_year,
                    date_requested,
                    DATE_FORMAT(date_requested, '%d/%b/%Y') as date_requested_formatted,
                    date_issued,
                    DATE_FORMAT(date_issued, '%d/%b/%Y') as date_issued_formatted,
                    origin_name,
                    destination_name,
                    product_code,
                    CONCAT(product_name, 
                        IFNULL(
                            CONCAT(' (', '${currentLocale?.toLanguageTag()?.toUpperCase()}', ': ', ${productDisplayNameSubQuery}, ')'), 
                            ''
                        ), 
                    '') AS product_name,
                    quantity_requested,
                    quantity_canceled,
                    quantity_approved,
                    quantity_modified,
                    quantity_picked,
                    quantity_demand,
                    reason_code_classification
                FROM product_demand_details pdd
                WHERE date_issued BETWEEN :startDate AND :endDate
                """
            if (product) {
                query += " AND product_id = :productId"
                params << [productId: product.id]
            }
            if (origin) {
                query += " AND origin_id = :originId"
                params << [originId: origin.id]
            }
            if (destination) {
                query += " AND destination_id = :destinationId"
                params << [destinationId: destination.id]
            }

            Sql sql = new Sql(dataSource)
            try {
                data = sql.rows(query, params)
            } catch (Exception e) {
                log.error("Unable to execute query: " + e.message, e)
            }
        }
        return data
    }

    def getAvailableDestinationsForDemandDetails(Location origin, Product product, Date startDate, Date endDate) {
        List data = []
        Map params = [startDate: startDate, endDate: endDate, productId: product.id, originId: origin.id]
        String query = """
            select 
                destination_id as id,
                destination_name as name
            FROM product_demand_details
            WHERE date_issued BETWEEN :startDate AND :endDate
            AND product_id = :productId
            AND origin_id = :originId
            GROUP BY destination_name, destination_id
            """
        Sql sql = new Sql(dataSource)
        try {
            data = sql.rows(query, params)

        } catch (Exception e) {
            log.error("Unable to execute query: " + e.message, e)
        }
        return data
    }


    def getDemandSummary(Location origin, Product product) {
        List data = []
        Integer demandPeriod = Holders.config.openboxes.forecasting.demandPeriod ?: 365
        boolean forecastingEnabled = Holders.config.openboxes.forecasting.enabled ?: false
        if (forecastingEnabled) {
            String query = """
                select 
                    min(date_issued) as min_date_demand,
                    max(date_issued) as max_date_demand,
                    month(date_issued) as month_demand,
                    year(date_issued) as year_demand,
                    sum(quantity_requested) as quantity_requested,
                    sum(quantity_canceled) as quantity_canceled,
                    sum(quantity_approved) as quantity_approved,
                    sum(quantity_modified) as quantity_modified,
                    sum(quantity_demand) as quantity_demand
                FROM product_demand_details
                WHERE product_id = :productId
                AND origin_id = :originId
                AND date_issued BETWEEN DATE_SUB(now(), INTERVAL :demandPeriod DAY) AND now()
                GROUP BY month_demand, year_demand
                ORDER BY year_demand, month_demand
            """
            Sql sql = new Sql(dataSource)
            List rows = []

            try {
                rows = sql.rows(query, [productId: product.id, originId: origin.id, demandPeriod: demandPeriod])
            } catch (Exception e) {
                log.error("Unable to execute query: " + e.message, e)
            }

            if (rows) {
                Timestamp startDate = rows.min { it.min_date_demand }?.min_date_demand
                Timestamp endDate = rows.max { it.max_date_demand }?.max_date_demand
                List allMonths = getMonths(startDate, endDate)
                def numberFormat = NumberFormat.getIntegerInstance()
                def totalDemand = 0
                def numberOfDays = new Date() - startDate

                data = allMonths.collect { monthYear ->
                    // Find row that matches the month and year
                    def row = rows.find {
                        it.year_demand == monthYear.year && it.month_demand == monthYear.month
                    }

                    // Aggregate demand
                    totalDemand += row?.quantity_demand ?: 0

                    [
                            dateKey       : "${monthYear?.month}/${monthYear?.year}",
                            year          : monthYear?.year,
                            month         : monthYear?.month,
                            monthName     : new DateFormatSymbols().months[monthYear?.month - 1],
                            quantityDemand: "${numberFormat.format(row?.quantity_demand ?: 0)}",
                    ]
                }


                data <<
                        [
                                dateKey       : "",
                                year          : "Average Monthly",
                                month         : "",
                                monthName     : "",
                                quantityDemand: "${numberFormat.format(totalDemand / numberOfDays * 30)}",
                        ]

                data <<
                        [
                                dateKey       : "",
                                year          : "Total Demand",
                                month         : "",
                                monthName     : "",
                                quantityDemand: "${numberFormat.format(totalDemand)}",
                        ]

            }
        }
        return data
    }


    def getMonths(Date startDate, Date endDate) {
        return (startDate..endDate).collect {
            [year: it[Calendar.YEAR], month: it[Calendar.MONTH] + 1]
        }.unique()
    }

    def getRequestDetailReport(Map params) {
        List data = []
        String query = """
            select 
                request_number,
                request_item_id,
                DATE_FORMAT(date_requested, '%d/%b/%Y') as date_requested,
                DATE_FORMAT(date_issued, '%d/%b/%Y') as date_issued,
                origin_name,
                destination_name,
                product_demand_details.product_code,
                product_name,
                product.price_per_unit,
                quantity_requested,
                quantity_picked,
                reason_code,
                reason_code_classification,
                quantity_demand
            FROM product_demand_details
            LEFT JOIN product ON product.id = product_demand_details.product_id
            """

        if (params.tags && params.tags != "null") {
            query += " LEFT JOIN product_tag ON product_tag.product_id = product_demand_details.product_id"
        }
        if (params.catalogs && params.catalogs != "null") {
            query += " LEFT JOIN product_catalog_item ON product_catalog_item.product_id = product_demand_details.product_id"
        }

        query += " WHERE date_issued BETWEEN :startDate AND :endDate AND origin_id = :originId"

        if (params.destinationId) {
            query += " AND destination_id = :destinationId"
        }
        if (params.productId) {
            query += " AND product_id = :productId"
        }
        if (params.reasonCode) {
            query += " AND reason_code_classification = :reasonCode"
        }
        if (params.category) {
            Category category = Category.get(params.category)
            if (category) {
                def categories = category.children
                categories << category
                query += " AND product.category_id in (${categories.collect { "'$it.id'" }.join(',')})"
            }
        }
        if (params.tags && params.tags != "null") {
            query += " AND product_tag.tag_id in (${params.tags.split(",").collect { "'$it'" }.join(',')})"
        }
        if (params.catalogs && params.catalogs != "null") {
            query += " AND product_catalog_item.product_catalog_id in (${params.catalogs.split(",").collect { "'$it'" }.join(',')})"
        }

        if ((params.tags && params.tags != "null") || (params.catalogs && params.catalogs != "null")) {
            query += " GROUP BY request_number, request_item_id, date_requested, date_issued," +
                    " origin_name, destination_name, product_demand_details.product_code, product_name," +
                    " quantity_requested, quantity_picked, reason_code_classification, quantity_demand"
        }

        Sql sql = new Sql(dataSource)
        try {
            data = sql.rows(query, params)

        } catch (Exception e) {
            log.error("Unable to execute query: " + e.message, e)
        }

        data = data.collect {
            Product product = Product.findByProductCode(it?.product_code)
            [
                    productCode             : it?.product_code,
                    productName             : product?.displayNameWithLocaleCode,
                    productFamily           : product?.productFamily?.name ?: '',
                    category                : product?.category ?: '',
                    productCatalogs         : product?.productCatalogs?.join(", "),
                    unitPrice               : it?.price_per_unit ?: '',
                    origin                  : it?.origin_name,
                    requestNumber           : it?.request_number,
                    destination             : it?.destination_name,
                    dateIssued              : it?.date_issued,
                    dateRequested           : it?.date_requested,
                    quantityRequested       : it?.quantity_requested ?: 0,
                    quantityIssued          : it?.quantity_picked ?: 0,
                    quantityDemand          : it?.quantity_demand ?: 0,
                    reasonCode              : it?.reason_code ?: '',
                    reasonCodeClassification: it?.reason_code_classification ?: '',
            ]
        }
        return data
    }

    def getDailyDemand(Location location, def demandDays) {
        List data = []
        String query = """
            SELECT
                product_id,
                origin_id AS location_id,
                SUM(quantity_demand) / :demandDays AS average_daily_demand
            FROM product_demand_details
            WHERE origin_id = :locationId
                AND date_issued > DATE_SUB(CURRENT_DATE, INTERVAL :demandDays DAY)
            GROUP BY product_id, location_id
            """

        Sql sql = new Sql(dataSource)

        try {
            data = sql.rows(query, [locationId: location.id, demandDays: demandDays])
        } catch (Exception e) {
            log.error("Unable to execute query: " + e.message, e)
        }

        return data
    }

    def getProductExpirySummary(Location location, def daysBeforeExpiration) {
        List data = []
        Map params = [locationId: location.id, daysBeforeExpiration: daysBeforeExpiration]
        String query = """
            SELECT 
                p_a.product_id,
                p_a.location_id,
                SUM(p_a.quantity_on_hand) AS quantity_on_hand
            FROM product_availability AS p_a
            JOIN inventory_item i ON i.id = p_a.inventory_item_id
            WHERE i.expiration_date < DATE_ADD(CURRENT_DATE, INTERVAL :daysBeforeExpiration DAY)
            AND p_a.location_id = :locationId
            GROUP BY p_a.product_id, p_a.location_id
            """

        Sql sql = new Sql(dataSource)

        try {
            data = sql.rows(query, params)
        } catch (Exception e) {
            log.error("Unable to execute query: " + e.message, e)
        }

        return data
    }

    def getProductExpiryAndAverageDailyDemandSummary(Location location, def daysBeforeExpiration) {
        List data = []
        Map params = [locationId: location.id, daysBeforeExpiration: daysBeforeExpiration]
        String query = """
            SELECT 
                product_id,
                location_id,
                expiration_date,
                quantity_on_hand,
                average_daily_demand
            FROM product_expiry_summary
            WHERE expiration_date < DATE_ADD(CURRENT_DATE, INTERVAL :daysBeforeExpiration DAY)
            AND location_id = :locationId
            """
        Sql sql = new Sql(dataSource)
        try {
            data = sql.rows(query, params)
        } catch (Exception e) {
            log.error("Unable to execute query: " + e.message, e)
        }

        return data
    }

    def getProductExpiryProjectedSummary(Location location, def daysBeforeExpiration) {
        def data = getProductExpiryAndAverageDailyDemandSummary(location, daysBeforeExpiration)
        def stockByProduct = data.groupBy { it.product_id }

        def productExpirySummary = stockByProduct.collect { prodId, items ->
            def sortedItems = items.sort { it.expiration_date }
            def startDate = new Date()
            def qtyExpired = 0
            def qtyCantUse = 0

            sortedItems.each { item ->
                def qtyCanUse = 0

                if (item.expiration_date.after(startDate)) {
                    use(TimeCategory) {
                        def duration = item.expiration_date - startDate

                        qtyCanUse = duration.days * item.average_daily_demand
                    }

                    startDate = item.expiration_date
                }

                qtyCantUse += item.quantity_on_hand - qtyCanUse

                if (qtyCantUse > 0) {
                    qtyExpired += qtyCantUse
                    qtyCantUse = 0
                }
            }

            return [
                    productId      : items[0].product_id,
                    locationId     : location.id,
                    quantityExpired: qtyExpired
            ]
        }

        return productExpirySummary
    }

    def getProductExpiry(Location location, def daysBeforeExpiration, def productId) {
        List data = []
        Map params = [locationId: location.id, daysBeforeExpiration: daysBeforeExpiration, productId: productId]
        String query = """
            SELECT 
                product_id,
                quantity_on_hand,
                average_daily_demand
            FROM product_expiry_summary
            WHERE expiration_date < DATE_ADD(CURRENT_DATE, INTERVAL :daysBeforeExpiration DAY)
            AND location_id = :locationId
            AND product_id = :productId
            """
        Sql sql = new Sql(dataSource)
        try {
            data = sql.rows(query, params)
        } catch (Exception e) {
            log.error("Unable to execute query: " + e.message, e)
        }

        return data
    }
}
