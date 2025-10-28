package org.pih.warehouse.data

import grails.gorm.transactions.Transactional
import org.apache.commons.lang3.NotImplementedException
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.query.NativeQuery
import org.hibernate.transform.AliasToEntityMapResultTransformer
import org.springframework.context.annotation.Primary

/**
 * Execute database queries via the Hibernate session.
 *
 * Usages of this service should auto-wire in PersistenceService (instead of HibernateSessionService) so that we
 * can swap out the implementation in the future if needed.
 *
 * Hibernate is an ORM (Object-Relational Mapping) tool. It is an implementation of the JPA (Java Persistence API)
 * specification. Hibernate provides an abstraction layer above the database, allowing us to query the database via
 * HQL (Hibernate Query Language) which is automatically translated to the language of the underlying database. This
 * allows us to support multiple SQL implementations (MariaDB, MySQL...) at once.
 *
 * If querying a domain object, use Spring Data's Repository pattern or GORM methods (provided by GormEntity) instead.
 * For example: Product.getByName(name) or Product.executeQuery(...).
 */
@Primary
@Transactional
class HibernateSessionService implements PersistenceService {

    SessionFactory sessionFactory

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession()
    }

    /**
     * Selects a list of rows from the database.
     *
     * Example usage:
     *
     * String sql = "SELECT * FROM location WHERE status IN (:statuses) and name = :name"
     * Map params = ["statuses", statuses, "name", name]
     * List<Map<String, Object>> result = HibernateSessionService.list(sql, params)
     */
    List<Map<String, Object>> list(String sql,
                                   Map<String, Object> params=[:],
                                   Integer pageSize=null,
                                   Integer offset=null) {
        NativeQuery query = createNativeQuery(sql, params)

        if (pageSize != null && offset != null) {
            query.setMaxResults(pageSize).setFirstResult(offset)
        }

        // Transforms the returned rows into a list of maps, keyed on column name.
        // Replace with TupleTransformer or ResultListTransformer when we upgrade to Hibernate 6+
        query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE)

        return query.list()
    }

    Map<String, Object> get(String sql, Map<String, Object> params) {
        throw new NotImplementedException()
    }

    int update(String sql, Map<String, Object> params) {
        throw new NotImplementedException()
    }

    int delete(String sql, Map<String, Object> params) {
        throw new NotImplementedException()
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
     * List result = HibernateSessionService.createNativeQuery(sql, params).list()
     */
    private NativeQuery createNativeQuery(String sql, Map<String, Object> params) {
        NativeQuery query = currentSession.createNativeQuery(sql)
        return setParameters(query, params)
    }

    private NativeQuery setParameters(NativeQuery query, Map<String, Object> params) {
        if (!params) {
            return query
        }

        for (entry in params) {
            String key = entry.key
            Object value = entry.value
            switch (value.class) {
                case List:
                case Object[]:
                    query.setParameterList(key, value)
                    break
                case null:
                    throw new IllegalArgumentException("Param [${key}] has null value, which will cause SQL errors.")
                    break
                default:
                    query.setParameter(key, value)
                    break
            }
        }

        return query
    }
}
