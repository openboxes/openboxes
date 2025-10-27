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
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.query.NativeQuery
import org.hibernate.transform.AliasToEntityMapResultTransformer

/**
 * Execute database queries via the Hibernate session.
 *
 * Hibernate is an ORM (Object-Relational Mapping) tool. It is an implementation of the JPA (Java Persistence API)
 * specification. Hibernate provides an abstraction layer above the database, allowing us to query the database via
 * HQL (Hibernate Query Language) which is automatically translated to the language of the underlying database. This
 * allows us to support multiple SQL implementations (MariaDB, MySQL...) at once.
 *
 * If querying a domain object, use Spring Data's Repository pattern or GORM methods (provided by GormEntity) instead.
 * For example: Product.getByName(name) or Product.executeQuery(...).
 */
@Transactional
class HibernateService {

    SessionFactory sessionFactory

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession()
    }

    /**
     * A convenience wrapper on createNativeQuery when selecting a list of rows.
     * @return List<Map<String, Object>> the rows, each containing a map of columns, keyed on column name
     *
     * Example usage:
     *
     * String sql = "SELECT * FROM location WHERE status IN (:statuses) and name = :name"
     * Map params = ["statuses", statuses, "name", name]
     * List<Map<String, Object>> result = hibernateService.list(sql, params)
     */
    List<Map<String, Object>> list(String sql, Map<String, Object> params=[:]) {
        NativeQuery query = createNativeQuery(sql, params)

        // Transforms the returned rows into a list of maps, keyed on column name.
        // Replace with TupleTransformer or ResultListTransformer when we upgrade to Hibernate 6+
        query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE)

        return query.list()
    }

    /**
     * Initialize a NativeQuery object that can be used to query the database via native SQL. Automatically adds
     * the given params to the query object.
     *
     * For SELECT queries, use Query.list() or Query.uniqueResult() to extract the result.
     *
     * Example usage:
     *
     * String sql = "SELECT * FROM location WHERE status IN (:statuses) and name = :name"
     * Map params = ["statuses", statuses, "name", name]
     * List result = hibernateService.createNativeQuery(sql, params).list()
     */
    NativeQuery createNativeQuery(String sql, Map<String, Object> params=[:]) {
        NativeQuery query = currentSession.createNativeQuery(sql)
        return setParamsOnQuery(query, params)
    }

    private NativeQuery setParamsOnQuery(NativeQuery query, Map<String, Object> params) {
        if (!params) {
            return query
        }

        for (entry in params) {
            String key = entry.key
            Object value = entry.value
            switch (value.class) {
                case List:
                    query.setParameterList(key, value)
                    break
                case Object[]:
                    query.setParameterList(key, value)
                    break
                case null:
                    break  // Ignore null values because they'll cause errors.
                default:
                    query.setParameter(key, value)
                    break
            }
        }

        return query
    }
}
