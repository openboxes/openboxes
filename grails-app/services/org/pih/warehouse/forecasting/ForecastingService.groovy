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

import groovy.sql.Sql
import groovy.time.TimeCategory
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.util.DateUtil

import java.sql.Timestamp
import java.text.DateFormatSymbols
import java.text.NumberFormat

class ForecastingService {

    boolean transactional = false

    def dataSource
    def grailsApplication
    def productAvailabilityService

    def getDemand(Location origin, Product product) {

        boolean forecastingEnabled = grailsApplication.config.openboxes.forecasting.enabled ?: false
        Integer demandPeriod = grailsApplication.config.openboxes.forecasting.demandPeriod ?: 365
        if (forecastingEnabled) {
            Map defaultDateRange = DateUtil.getDateRange(new Date(), -1)
            use(TimeCategory) {
                defaultDateRange.startDate = defaultDateRange.endDate - demandPeriod.days
            }

            def rows = getDemandDetails(origin, product, defaultDateRange.startDate, defaultDateRange.endDate)
            def totalDemand = rows.sum { it.quantity_demand } ?: 0
            def dailyDemand = (totalDemand && demandPeriod) ? (totalDemand / demandPeriod) : 0
            def monthlyDemand = totalDemand / Math.floor((demandPeriod / 30))
            def quantityOnHand = productAvailabilityService.getQuantityOnHand(product, origin)
            def onHandMonths = monthlyDemand ? quantityOnHand / monthlyDemand : 0

            return [
                    totalDemand  : totalDemand,
                    totalDays    : demandPeriod,
                    dailyDemand  : dailyDemand,
                    monthlyDemand: "${NumberFormat.getIntegerInstance().format(monthlyDemand)}",
                    onHandMonths: onHandMonths
            ]
        }
    }

    def getDemandDetails(Location origin, Product product) {
        Date today = new Date()
        Integer demandPeriod = grailsApplication.config.openboxes.forecasting.demandPeriod?:365
        return getDemandDetails(origin, product, today - demandPeriod, today)
    }

    def getDemandDetails(Location origin, Product product, Date startDate, Date endDate) {
        List data = []
        boolean forecastingEnabled = grailsApplication.config.openboxes.forecasting.enabled ?: false
        if (forecastingEnabled) {
            Map params = [startDate: startDate, endDate: endDate]
            String query = """
                select 
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
                    product_name,
                    quantity_requested,
                    quantity_canceled,
                    quantity_approved,
                    quantity_modified,
                    quantity_picked,
                    quantity_demand,
                    reason_code_classification
                FROM product_demand_details
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

            Sql sql = new Sql(dataSource)
            try {
                data = sql.rows(query, params)

            } catch (Exception e) {
                log.error("Unable to execute query: " + e.message, e)
            }
        }
        return data
    }

    def getDemandDetailsForDemandTab(Location origin, Location destination, Product product, Date startDate, Date endDate) {
        List data = []
        Map params = [startDate: startDate, endDate: endDate, productId: product.id, originId: origin.id]
        String query = """
            select 
                request_id,
                request_item_id,
                request_status,
                request_number,
                date_requested,
                date_issued,
                origin_name,
                destination_name,
                product_code,
                product_name,
                quantity_requested,
                quantity_picked,
                quantity_demand,
                reason_code_classification
            FROM product_demand_details
            WHERE date_issued BETWEEN :startDate AND :endDate
            AND product_id = :productId
            AND origin_id = :originId
            """
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
        Integer demandPeriod = grailsApplication.config.openboxes.forecasting.demandPeriod?:365
        boolean forecastingEnabled = grailsApplication.config.openboxes.forecasting.enabled ?: false
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
                quantity_requested,
                quantity_picked,
                reason_code,
                reason_code_classification,
                quantity_demand
            FROM product_demand_details
            """

        if (params.category) {
            query += " JOIN product ON product.id = product_demand_details.product_id"
        }
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
            [
                    productCode             : it?.product_code,
                    productName             : it?.product_name,
                    origin                  : it?.origin_name,
                    requestNumber           : it?.request_number,
                    destination             : it?.destination_name,
                    dateIssued              : it?.date_issued,
                    dateRequested           : it?.date_requested,
                    quantityRequested       : it?.quantity_requested ?: 0,
                    quantityIssued          : it?.quantity_picked ?: 0,
                    quantityDemand          : it?.quantity_demand ?: 0,
                    reasonCode              : it?.reason_code,
                    reasonCodeClassification: it?.reason_code_classification,
            ]
        }
        return data
    }

}
