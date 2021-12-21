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
			"org.pih.warehouse.auth",
			"org.pih.warehouse.core",
			"org.pih.warehouse.inventory",
			"org.pih.warehouse.product"
	]

	private static final Logger logger = LoggerFactory.getLogger(SwaggerFilter.class)

	/**
	 * Filter out properties that Swagger documentation should not expose.
	 *
	 * While we can use the @Hidden annotation to hide properties whose
	 * names are declared in Grails domains, GORM conveniently adds
	 * some for us via, e.g., the `hasMany()` pattern. Since they aren't
	 * explicitly declared in the code, we can't mark them @Hidden.
	 *
	 * This method removes 1:1 and 1:many mappings from Swagger output.
	 *
	 * @param property A Schema object, representing a single field or property of a Grails domain
	 * (remaining arguments are part of Swagger's filtering API, but we ignore them)
	 * @return the unmodified Schema object, or null if the property should be hidden
	 */
	@Override
	Optional<Schema> filterSchemaProperty(Schema property, Schema schema, String propName, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {

		// do not expose hidden fields underlying GORM's 1:1 mappings
		if (property?.name?.endsWith("Id")) {
			return Optional.empty()
		}

		// do not expose GORM's 1:many mappings
		for (pkgName in INCLUDED_PACKAGE_NAMES) {
			try {
				Class domain = Class.forName("${pkgName}.${schema?.name}")
				if (domain?.hasMany?.any { it -> it.key == property?.name }) {
					return Optional.empty()
				}
			} catch (ClassNotFoundException ignored) {
			} catch (MissingPropertyException ignored) {
			}
		}

		// I'm not sure where this one comes from, but we don't want to expose it
		if (property?.name == "metaClass") {
			return Optional.empty()
		}

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
					logger.debug "ignoring ${domain?.name} due to package"
					return Optional.empty()
				}
			} catch (ClassNotFoundException ignored) {
			}
		}

		if (schema?.name in EXCLUDED_INTERNAL_CLASS_NAMES) {
			logger.debug "ignoring ${schema?.name} due to class name"
			return Optional.empty()
		}

		// if possible, call postProcessSchema() in the schema's implementation
		for (pkgName in INCLUDED_PACKAGE_NAMES) {
			try {
				Class domain = Class.forName("${pkgName}.${schema?.name}")
				Method m = domain.getMethod("postProcessSchema", Schema)
				logger.debug "about to post-process ${pkgName}.${schema?.name}"
				return Optional.of(m.invoke(domain, schema))
			} catch (ClassNotFoundException ignored) {
			} catch (NoSuchMethodException ignored) {
			}
		}

		logger.info "found ${schema?.name}, no post-processing needed"
		return Optional.of(schema)
	}
}
