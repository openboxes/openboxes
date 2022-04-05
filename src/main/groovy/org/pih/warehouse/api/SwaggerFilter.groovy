/**
 * Copyright (c) 2022 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

package org.pih.warehouse.api

import io.swagger.v3.core.filter.AbstractSpecFilter
import io.swagger.v3.oas.models.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Method

class SwaggerFilter extends AbstractSpecFilter {

    /*
     * Swagger's handy schema-finding feature also picks up a number of
     * Groovy internals which have nothing to do with our public API.
     * (They're also complicated enough to crash the Swagger UI.)
     */
    def EXCLUDED_PACKAGE_NAMES = [
        "groovy.lang",
        "java.lang.reflect",
        "org.codehaus.groovy.ast",
        "org.codehaus.groovy.ast.expr",
        "org.codehaus.groovy.ast.stmt",
        "org.codehaus.groovy.control",
        "org.codehaus.groovy.control.customizers",
        "org.codehaus.groovy.control.io",
        "org.codehaus.groovy.control.messages",
        "org.codehaus.groovy.reflection",
        "org.codehaus.groovy.runtime",
        "org.codehaus.groovy.runtime.callsite",
        "org.codehaus.groovy.syntax",
        "sun.reflect"
    ]

    def EXCLUDED_PROPERTY_NAMES = [
        "afterDelete",
        "afterInsert",
        "afterUpdate",
        "beforeInsert",
        "beforeUpdate",
        "metaClass",
        "publishPersistenceEvent",
    ]

    /*
     * Swagger also picks up the occasional internal class.
     * It's easier to exclude these by name than by package.
     */
    def EXCLUDED_INTERNAL_CLASS_NAMES = [
        "MetaClassCreationHandle"
    ]

    /*
     * A list of package names to supply to Class.forName() to help us
     * get back from a Swagger Schema object to its Grails implementation.
     */
    def INCLUDED_PACKAGE_NAMES = [
        "org.pih.warehouse.api",
        "org.pih.warehouse.auth",
        "org.pih.warehouse.core",
        "org.pih.warehouse.inventory",
        "org.pih.warehouse.product"
    ]

    private static final Logger logger = LoggerFactory.getLogger(SwaggerFilter.class)

    /**
     * Filter out properties that Swagger documentation should not expose.
     *
     * @param property A Schema object, representing a single field or
     * property of a Grails domain (all subsequent parameters are part of
     * Swagger's filtering API, but we ignore them)
     * @return the first parameter, or null if the property should be hidden
     */
    @Override
    Optional<Schema> filterSchemaProperty(Schema property, Schema schema, String propName, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {

        if (property?.name in EXCLUDED_PROPERTY_NAMES) {
            logger.debug "skipping field {} (excluded property name)", property?.name
            return Optional.empty()
        }

        // don't expose the hidden fields underlying GORM's 1:1 mappings
        if (property?.name?.endsWith("Id")) {
            logger.debug "skipping field {} (GORM internal)", property?.name
            return Optional.empty()
        }

        logger.debug "exposing field {}", property?.name
        return Optional.of(property)
    }

    /**
     * Add fields to a Swagger schema object that Swagger itself can't detect.
     *
     * N.B., this method is especially helpful for rescuing fields defined in
     * BootStrap.groovy, which neither Swagger nor this author can parse. ;-)
     *
     * @param schema A Schema object, representing a Grails domain
     * (remaining arguments are part of Swagger's filtering API, but we ignore them)
     * @return The input schema object, perhaps with more fields
     */
    @Override
    Optional<Schema> filterSchema(Schema schema, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {

        for (pkgName in EXCLUDED_PACKAGE_NAMES) {
            try {
                Class domain = Class.forName("${pkgName}.${schema?.name}")
                if (domain != null) {
                    logger.debug "skipping {} (excluded package {})", schema.name, pkgName
                    return Optional.empty()
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (schema?.name in EXCLUDED_INTERNAL_CLASS_NAMES) {
            logger.debug "skipping {} (excluded internal class)", schema?.name
            return Optional.empty()
        }

        // if possible, call postProcessSchema() in the schema's implementation
        for (pkgName in INCLUDED_PACKAGE_NAMES) {
            try {
                Class domain = Class.forName("${pkgName}.${schema?.name}")
                Method m = domain.getMethod("postProcessSchema", Schema)
                logger.debug "exposing {} after calling postProcessSchema()", schema.name
                return Optional.of(m.invoke(domain, schema) as Schema)
            } catch (ClassNotFoundException ignored) {
            } catch (NoSuchMethodException ignored) {
            }
        }

        logger.debug "exposing {}", schema?.name
        return Optional.of(schema)
    }
}
