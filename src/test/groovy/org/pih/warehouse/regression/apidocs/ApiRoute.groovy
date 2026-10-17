package org.pih.warehouse.regression.apidocs

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * One API operation: a concrete path template plus an HTTP method, and the controller action it
 * resolves to at runtime.
 */
@EqualsAndHashCode(includes = ['path', 'method'])
@ToString(includes = ['method', 'path', 'controller', 'action', 'source'], includePackage = false)
class ApiRoute implements Comparable<ApiRoute> {

    /** Where the operation comes from in UrlMappings.groovy. */
    enum Source {
        /** An /api mapping with a literal controller and a method -> action map. */
        EXPLICIT,
        /** One of the "/api/${resource}s..." mappings, expanded per *Api controller. */
        DYNAMIC,
        /** A mapping that accepts any HTTP method, bounded by route-methods.yaml. */
        UNBOUNDED,
        /** The Generic API ("/api/generic/${resource}/..."), listed as a free-form family. */
        GENERIC
    }

    static final List<String> METHODS = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE']

    /** OpenAPI path template, e.g. /api/stockMovements/{id}. */
    String path
    String method
    String controller
    String action
    Source source
    /** Path parameter names, in order of appearance. */
    List<String> pathParams = []
    /** True when the mapping also accepts a ".{format}" suffix, e.g. /api/reports/x.csv. */
    boolean formatSuffix
    /** The UrlMappings.groovy pattern the operation was derived from. */
    String mappingPattern

    String getKey() {
        "${method} ${path}"
    }

    String getTarget() {
        "${controller}/${action}"
    }

    @Override
    int compareTo(ApiRoute other) {
        path <=> other.path ?: METHODS.indexOf(method) <=> METHODS.indexOf(other.method)
    }
}
