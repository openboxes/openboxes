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
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

import java.sql.Timestamp

class ForecastingService {

    boolean transactional = true

    def dataSource
    def grailsApplication


    def getDemand(Location origin, Product product) {

        boolean forecastingEnabled = grailsApplication.config.openboxes.forecastingEnabled ?: false
        if (forecastingEnabled) {
            def rows = getDemandDetails(origin, product)
            def startDate = rows.min { it.date_requested }?.date_requested
            def endDate = rows.max { it.date_requested }?.date_requested
            def totalDemand = rows.sum { it.quantity_demand } ?: 0
            def totalDays = (startDate && endDate) ? (endDate - startDate) : 1
            def dailyDemand = (totalDemand && totalDays) ? (totalDemand / totalDays) : 0
            def monthlyDemand = dailyDemand * 30
            return [
                    dateRange    : [startDate: startDate, endDate: endDate],
                    totalDemand  : totalDemand,
                    totalDays    : totalDays,
                    dailyDemand  : dailyDemand,
                    monthlyDemand: monthlyDemand
            ]
        }
    }

    def getDemandDetails(Location origin, Product product) {
        List data = []
        boolean forecastingEnabled = grailsApplication.config.openboxes.forecastingEnabled ?: false
        if (forecastingEnabled) {
            String query = """
                select 
                    request_status,
                    request_number,
                    date_requested,
                    origin_name,
                    destination_name,
                    product_code,
                    product_name,
                    quantity_requested,
                    quantity_canceled,
                    quantity_approved,
                    quantity_change_approved,
                    quantity_substitution_approved,
                    quantity_demand,
                    cancel_reason_code
                FROM product_demand
                WHERE product_id = :productId
                AND origin_id = :originId
            """
            Sql sql = new Sql(dataSource)
            try {
                data = sql.rows(query, [productId: product.id, originId: origin.id])

            } catch (Exception e) {
                log.error("Unable to execute query: " + e.message, e)
            }
        }
        return data
    }


    def getDemandSummary(Location origin, Product product) {
        List data = []

        boolean forecastingEnabled = grailsApplication.config.openboxes.forecastingEnabled ?: false
        if (forecastingEnabled) {
            String query = """
                select 
                    min(date_requested) as min_date_requested,
                    max(date_requested) as max_date_requested,
                    DATE_FORMAT(date_requested, '%b %y') as date_key,
                    month(date_requested) as request_month,
                    year(date_requested) as request_year,
                    sum(quantity_requested) as quantity_requested,
                    sum(quantity_canceled) as quantity_canceled,
                    sum(quantity_approved) as quantity_approved,
                    sum(quantity_change_approved) as quantity_change_approved,
                    sum(quantity_substitution_approved) as quantity_substitution_approved,
                    sum(quantity_demand) as quantity_demand
                FROM product_demand
                WHERE product_id = :productId
                AND origin_id = :originId
                GROUP BY request_month, request_year
                ORDER BY request_year, request_month
            """
            Sql sql = new Sql(dataSource)
            List rows = []

            try {
                rows = sql.rows(query, [productId: product.id, originId: origin.id])
            } catch (Exception e) {
                log.error("Unable to execute query: " + e.message, e)
            }

            if (rows) {
                Timestamp startDate = rows.min { it.min_date_requested }?.min_date_requested
                Timestamp endDate = rows.max { it.max_date_requested }?.max_date_requested
                List allMonths = getMonths(startDate, endDate)

                data = allMonths.collect { monthYear ->
                    def row = rows.find {
                        it.request_year == monthYear.year && it.request_month == monthYear.month
                    }
                    [
                            dateKey          : "${monthYear?.month}/${monthYear?.year}",
                            year             : monthYear?.year,
                            month            : monthYear?.month,
                            quantityRequested: row?.quantity_requested ?: 0,
                            quantityCanceled : row?.quantity_canceled ?: 0,
                            quantityApproved : row?.quantity_approved ?: 0,
                            quantityDemand   : row?.quantity_demand ?: 0
                    ]
                }
                log.info "data: ${data}"
            }
        }
        return data
    }


    def getMonths(Date startDate, Date endDate) {
        return (startDate..endDate).collect {
            [year: it[Calendar.YEAR], month: it[Calendar.MONTH] + 1]
        }.unique()
    }

}
