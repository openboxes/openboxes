/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.report

import org.apache.commons.lang.StringEscapeUtils
import org.hibernate.criterion.CriteriaSpecification
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.ConsumptionFact

import java.text.NumberFormat
import java.text.SimpleDateFormat

class ConsumptionService {

    def dataService
    def sessionFactory

    Integer deleteConsumptionRecords() {
        return ConsumptionFact.executeUpdate("""delete ConsumptionFact c""")
    }

    def aggregateConsumption(Location location, Category category, Date startDate, Date endDate) {
        def results = ConsumptionFact.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                groupProperty('product', "product")
                groupProperty('productCode', "Product Code")
                groupProperty('productName', "Product Name")
                groupProperty("categoryName", "Category Name")
                groupProperty("day", "Day")
                groupProperty("week", "Week")
                groupProperty("month", "Month")
                groupProperty("year", "Year")
                sum("quantity", "Quantity")
            }

            if (startDate && endDate) {
                between('transactionDate', startDate, endDate)
            }
            if (category) {
                eq("categoryName", category.name)
            }
            eq("location", location)
            order("productName", "asc")
        }
        return results

    }


    def listConsumption(Location location, Category category, Date startDate, Date endDate) {

        def results = ConsumptionFact.createCriteria().list {
            // TODO Use resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)

            if (startDate && endDate) {
                transactionDateKey {
                    between('date', startDate, endDate)
                }
            }
            locationKey {
                eq("locationId", location.id)
            }
            productKey {
                if (category) {
                    eq("categoryName", category.name)
                }
                order("productName", "asc")
            }
        }

        return results
    }

    def generateCrossTab(List<ConsumptionFact> consumptionFactList, Date startDate, Date endDate, String groupBy) {

        def calendar = Calendar.instance
        def dateFormat = new SimpleDateFormat("ddMMyyyy")

        def dateKeys = (startDate..endDate).collect { date ->
            calendar.setTime(date)
            [
                    date : date,
                    day  : calendar.get(Calendar.DAY_OF_MONTH),
                    week : calendar.get(Calendar.WEEK_OF_YEAR),
                    month: calendar.get(Calendar.MONTH),
                    year : calendar.get(Calendar.YEAR),
                    key  : dateFormat.format(date)
            ]
        }.sort { it.date }


        def daysBetween = (groupBy != "default") ? -1 : endDate - startDate
        if (daysBetween > 365 || groupBy.equals("yearly")) {
            dateFormat = Constants.yearFormat
        } else if ((daysBetween > 61 && daysBetween < 365) || groupBy.equals("monthly")) {
            dateFormat = Constants.yearMonthFormat
        } else if (daysBetween > 14 && daysBetween < 60 || groupBy.equals("weekly")) {
            dateFormat = Constants.weekFormat
        } else if (daysBetween > 0 && daysBetween <= 14 || groupBy.equals("daily")) {
            dateFormat = Constants.dayFormat
        } else {
            dateFormat = Constants.yearMonthFormat
        }
        dateKeys = dateKeys.collect { dateFormat.format(it.date) }.unique()

        log.info("consumptionFactList: " + consumptionFactList)

        def consumptionFactMap = consumptionFactList.inject([:]) { result, consumptionFact ->
            def productId = consumptionFact?.productKey?.productId
            def transactionDate = consumptionFact?.transactionDateKey?.date
            def quantityIssued = consumptionFact?.quantity
            def dateKey = dateFormat.format(transactionDate)
            def quantityMap = result[productId]
            if (!quantityMap) {
                quantityMap = [:]
            }
            def quantity = quantityMap[dateKey] ?: 0
            quantity += quantityIssued
            quantityMap[dateKey] = quantity
            result[productId] = quantityMap
            result
        }
        log.info "Consumption map: " + consumptionFactMap

        def crosstabRows = []
        def products = consumptionFactList.collect {
            Product.get(it?.productKey?.productId)
        }.unique()
        products.each { Product product ->
            BigDecimal totalIssued = 0
            BigDecimal totalDemand = 0
            BigDecimal totalCanceled = 0
            BigDecimal unitCost = product?.costPerUnit ?: product?.pricePerUnit ?: 0
            Map row = [
                    "Code"     : product?.productCode,
                    "Name"     : product?.name,
                    "Tags"     : StringEscapeUtils.escapeCsv(product.tagsToString()),
                    "Catalogs" : StringEscapeUtils.escapeCsv(product.productCatalogsToString()),
                    "Unit Cost": NumberFormat.getNumberInstance().format(unitCost)
            ]

            def consumptionAggregated = consumptionFactMap[product?.id]
            dateKeys.each { dateKey ->
                def quantityIssued = consumptionAggregated[dateKey] ?: 0
                totalIssued += quantityIssued
                row += ["${dateKey}": quantityIssued]
            }
            BigDecimal averageIssued = totalIssued / dateKeys.size()

            row += [
                    "Total Demand"  : totalDemand,
                    "Total Canceled": totalCanceled,
                    "Total Issued"  : totalIssued,
                    "Total Cost"    : NumberFormat.getNumberInstance().format(totalIssued * unitCost),
                    "Average Issued": averageIssued,
                    "Average Cost"  : NumberFormat.getNumberInstance().format(averageIssued * unitCost)
            ]
            crosstabRows << row
        }
        log.info "crosstabRows: " + crosstabRows
        return crosstabRows
    }
}
