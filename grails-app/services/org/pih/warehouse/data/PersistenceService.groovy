package org.pih.warehouse.data

import org.pih.warehouse.core.PaginationParams

/**
 * For making queries to a persistence layer (ex: a database).
 *
 * If querying a domain object, use Spring Data's Repository pattern or GORM methods (provided by GormEntity) instead.
 * For example: Product.getByName(name) or Product.executeQuery(...).
 */
interface PersistenceService {

    /**
     * Selects a paginated list of rows from the persistence layer.
     *
     * @param query the query string
     * @param params a map of variables to be bound to the query. (For SQL, these are represented as ":x" in the query)
     * @param paginationParams required parameters for when we want to paginate the request
     * @return List<Map<String, Object>> the rows, each containing a map of columns, keyed on column name
     */
    List<Map<String, Object>> list(String query,
                                   Map<String, Object> params,
                                   PaginationParams paginationParams)

    /**
     * Selects a list of rows from the persistence layer.
     *
     * @param query the query string
     * @param params a map of variables to be bound to the query. (For SQL, these are represented as ":x" in the query)
     * @return List<Map<String, Object>> the rows, each containing a map of columns, keyed on column name
     */
    List<Map<String, Object>> list(String query, Map<String, Object> params)

    /**
     * Selects a single row from the persistence layer.
     *
     * @param query the query string
     * @param params a map of variables to be bound to the query. (For SQL, these are represented as ":x" in the query)
     * @return Map<String, Object> a map of columns, keyed on column name
     */
    Map<String, Object> get(String query, Map<String, Object> params)

    /**
     * Execute an update statement to the persistence layer.
     *
     * @param query the query string
     * @param params a map of variables to be bound to the query. (For SQL, these are represented as ":x" in the query)
     * @return int the number of entities updated
     */
    int update(String query, Map<String, Object> params)

    /**
     * Execute a delete statement from the persistence layer.
     *
     * @param query the query string
     * @param params a map of variables to be bound to the query. (For SQL, these are represented as ":x" in the query)
     * @return int the number of entities deleted
     */
    int delete(String query, Map<String, Object> params)
}
