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
import org.pih.warehouse.importer.CSVUtils
import org.springframework.transaction.annotation.Isolation

import javax.sql.DataSource
import java.sql.Connection

@Transactional
class DataService {

    DataSource dataSource

    List executeQuery(String query) {
        return new Sql(dataSource).rows(query)
    }

    List executeQuery(String query, Map params) {
        return new Sql(dataSource).rows(query, params)
    }

    /**
     * Executes a single SQL statement within its own transaction, optionally
     * running it under a specific transaction isolation level (see
     * withTransactionIsolation). Delegates to executeStatements() so both
     * methods share one implementation of the transaction/isolation handling.
     */
    void executeStatement(String statement, Isolation isolation = null) {
        executeStatements([statement], isolation)
    }

    /**
     * Executes a list of SQL statements as a single atomic transaction. All
     * statements commit together, or a failure anywhere rolls back the whole
     * batch and rethrows to the caller (unlike the previous per-statement
     * behavior, which silently swallowed failures; see OBS-1987/#6190).
     *
     * Optionally runs the whole batch under a specific transaction isolation
     * level instead of the connection's default (REPEATABLE READ), e.g. to
     * avoid the shared locks MariaDB/MySQL takes on source tables during a
     * locking read like INSERT...SELECT. See OBPIH-3641 / OBS-1988 / #6189.
     *
     * Note: this only provides atomicity for pure DML sequences (DELETE,
     * INSERT, UPDATE). DDL statements (DROP TABLE, CREATE TABLE) cause an
     * implicit commit in MySQL/MariaDB regardless of this transaction wrapper.
     * A sequence containing DDL needs a different pattern (build into a
     * _tmp table, then RENAME TABLE to swap). See #6190.
     */
    void executeStatements(List<String> statementList, Isolation isolation = null) {
        Sql sql = new Sql(dataSource)
        sql.cacheConnection { Connection connection ->
            withTransactionIsolation(connection, isolation) {
                sql.withTransaction {
                    statementList.each { String statement ->
                        def startTime = System.currentTimeMillis()
                        log.info "Executing statement ${statement}"
                        try {
                            sql.execute(statement)
                        } catch (Exception e) {
                            log.error("Failed executing statement: ${statement}", e)
                            throw e
                        }
                        log.info "Updated ${sql.updateCount} rows in " + (System.currentTimeMillis() - startTime) + " ms"
                    }
                }
            }
        }
        sql.close()
    }

    /**
     * Runs the given closure with the connection's transaction isolation level temporarily
     * set to transactionIsolation, restoring the previous level afterward regardless of success
     * or failure. Pass null (or Isolation.DEFAULT) to run the closure unchanged, at
     * whatever isolation level the connection already has.
     *
     * Used to run ETL/reporting queries (e.g. INSERT...SELECT) under READ COMMITTED
     * instead of the default REPEATABLE READ, avoiding the shared locks MariaDB/MySQL
     * takes on source table rows during a locking read. See OBPIH-3641 / OBS-1988.
     */
    private void withTransactionIsolation(Connection connection, Isolation transactionIsolation, Closure closure) {
        if (!transactionIsolation || transactionIsolation == Isolation.DEFAULT) {
            closure()
            return
        }
        Integer previousTransactionIsolation = connection.getTransactionIsolation()
        try {
            connection.setTransactionIsolation(transactionIsolation.value())
            closure()
        } finally {
            connection.setTransactionIsolation(previousTransactionIsolation)
        }
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
