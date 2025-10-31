/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.data

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.apache.commons.lang.StringEscapeUtils
import org.apache.poi.hssf.usermodel.*
import org.apache.poi.ss.usermodel.*
import grails.plugins.csv.CSVWriter
import org.grails.plugins.excelimport.ExpectedPropertyType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.ProductPrice
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.UnitOfMeasureClass
import org.pih.warehouse.core.UnitOfMeasureType
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.InventoryLevelExcelImporter
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.UnitOfMeasureClass
import org.pih.warehouse.core.Tag
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryStatus
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductType
import org.pih.warehouse.product.ProductTypeCode

import java.text.SimpleDateFormat

@Transactional
class DataService {

    def dataSource

    List executeQuery(String query) {
        return new Sql(dataSource).rows(query)
    }

    List executeQuery(String query, Map params) {
        return new Sql(dataSource).rows(query, params)
    }

    void executeStatement(String statement, Boolean logStatement = true) {
        Sql sql = new Sql(dataSource)
        sql.withTransaction {
            try {
                def startTime = System.currentTimeMillis()
                log.info "Executing statement ${logStatement ? statement : ''}"
                sql.execute(statement)
                log.info "Updated ${sql.updateCount} rows in " +  (System.currentTimeMillis() - startTime) + " ms"
                sql.commit()
            } catch (Exception e) {
                sql.rollback()
                log.error("Rollback due to error while executing statements: " + e.message, e)
            }
        }
    }

    void executeStatements(List statementList, Boolean logStatements = true) {
        statementList.each { String statement ->
            executeStatement(statement, logStatements)
        }
    }


    /**
     * Should use the apache library to handle this.
     * @param str
     * @return
     */
    def getFloat(str) {
        try {
            return str.toFloat()
        } catch (NumberFormatException e) {
            log.error("Error converting string ${str} to float.")

            throw e
        }
        return 0.0
    }

    def transformObjects(List objects, List includeFields) {
        Map includeFieldsMap = includeFields.inject([:]) { result, includeField ->
            result[includeField] = includeField
            return result
        }

        transformObjects(objects, includeFieldsMap)
    }

    def transformObjects(List objects, Map includeFields) {
        objects.collect { object ->
            return transformObject(object, includeFields)
        }
    }

    Map transformObject(Object object, Map includeFields) {
        Map properties = [:]
        includeFields.each { fieldName, element ->
            def value = null
            if (element instanceof LinkedHashMap) {
                value = object.get(element.property) ?: element.property.tokenize('.').inject(object) { v, k -> v?."$k" }
                if (element.defaultValue && element.dateFormat && !value) {
                    value = element.defaultValue.format(element.dateFormat)
                } else if (element.dateFormat && value) {
                    value = value.format(element.dateFormat)
                } else if (element.defaultValue && !value) {
                    value = element.defaultValue
                }
                // We can't just check the truthiness of the value, because the false boolean would be evaluated to an empty string
                properties[fieldName] = value == null ? "" : value
            } else {
                // to access object value by key we must use the object.get(key) instead of object[key]
                // because using the object[key] will throw an error when trying to export data using the batch controller
                value = object.get(element) ?: element.tokenize('.').inject(object) { v, k -> v?."$k" }
                // We can't just check the truthiness of the value, because the false boolean would be evaluated to an empty string
                properties[fieldName] = value == null ? "" : value
            }
        }
        return properties
    }

    /**
     * Generic method to generate CSV string based on given csvrows map.
     * @param csvrows
     * @return
     */
    String generateCsv(csvrows) {
        def sw = new StringWriter()
        if (csvrows) {
            def columnHeaders = csvrows[0].keySet().collect { value -> StringEscapeUtils.escapeCsv(value) }
            sw.append(columnHeaders.join(",")).append("\n")
            csvrows.each { row ->
                def values = row.values().collect { value ->
                    if (value?.toString()?.isNumber()) {
                        value
                    } else {
                        StringEscapeUtils.escapeCsv(value.toString())
                    }
                }
                sw.append(values.join(","))
                sw.append("\n")
            }
        }
        return CSVUtils.prependBomToCsvString(sw.toString())
    }

}
