package org.pih.warehouse.core.db

/**
 * Utility methods assisting with using GORM (Grails Object Relational Mapping) methods.
 */
class GormUtil {

    private static final String PARAM_IDENTIFIER = ":"

    /**
     * Given a SQL query and a map of values to use for the params/variables in the query (identified
     * by ":<param>"), filters the given params down to only those that actually exist in the query.
     * Helps to ensure we have valid SQL in our queries.
     *
     * For example:
     *   If given a query: "SELECT id FROM Table t WHERE t.product_id IN (:products)"
     *   And params: [products: "a,b,c", somethingElse: "1"]
     *   Returns: [products: "a,b,c"] (because the "somethingElse" param doesn't exist in the query).
     *
     * Note that s long as we pass user input variables as args to execute query (and not by directly
     * adding them to the SQL string) we don't need to worry about handling any SQL injection attacks.
     * Hibernate and GORM will handle that for us.
     */
    static Map sanitizeExecuteQueryArgs(String sql, Map args) {
        if (!args) {
            return [:]
        }
        return args.findAll { paramName, paramValue ->
            sql.contains("${PARAM_IDENTIFIER}${paramName}")
        }
    }
}
