package org.pih.warehouse.api

import io.swagger.v3.core.filter.AbstractSpecFilter
import io.swagger.v3.oas.models.media.Schema

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.List
import java.util.Map
import java.util.Optional

class SwaggerFilter extends AbstractSpecFilter {

	/**
	 * Filter out properties that Swagger documentation should not expose.
	 *
	 * While we can use the @Hidden annotation to hide properties whose
	 * names are declared in Grails domains, but GORM conveniently adds
	 * some for us via, e.g., the `hasMany()` pattern. Since they aren't
	 * explicitly declared in the code, we can't mark them @Hidden.
	 *
	 * This method removes 1:1 and 1:many mappings from the Swagger API.
	 *
	 * @param property A Schema object representing a single field or property of a Grails domain
	 * @param schema part of Swagger API, but we ignore it
	 * @param propName part of Swagger API, but we ignore it
	 * @param params part of Swagger API, but we ignore it
	 * @param cookies part of Swagger API, but we ignore it
	 * @param headers part of Swagger API, but we ignore it
	 * @return the unmodified Schema object, or null if the property should be hidden
	 */
	@Override
	Optional<Schema> filterSchemaProperty(Schema property, Schema schema, String propName, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {

		// do not expose hidden fields underlying GORM's 1:1 mappings
		if (property?.name?.endsWith("Id")) {
			return Optional.empty()
		}

		// do not expose GORM's 1:many mappings
		try {
			Class domain = Class.forName("org.pih.warehouse.core.${schema?.name}")
			if (domain?.hasMany?.any{it -> it.key == property?.name}) {
				return Optional.empty()
			}
		} catch (ClassNotFoundException ignored) {
		} catch (MissingPropertyException ignored) {}

		return Optional.of(property)
	}

	/**
	 * Add fields to a Swagger schema object that Swagger itself can't detect.
	 *
	 * N.B., this method is especially helpful for rescuing fields defined in
	 * BootStrap.groovy, which, neither Swagger nor this author can parse. ;-)
	 *
	 * @param schema A Schema object representing a Grails domain
	 * @param params part of Swagger API, but we ignore it
	 * @param cookies part of Swagger API, but we ignore it
	 * @param headers part of Swagger API, but we ignore it
	 * @return The input schema object, perhaps with more fields
	 */
	@Override
	Optional<Schema> filterSchema(Schema schema, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
		// if the linked class has a method called addImplicitProperties, call it
		try {
			Class domain = Class.forName("org.pih.warehouse.core.${schema?.name}")
			Method m = domain.getMethod("addImplicitProperties", Schema)
			return Optional.of(m.invoke(schema))
		} catch (ClassNotFoundException ignored) {
		} catch (IllegalAccessException ignored) {
		} catch (IllegalArgumentException ignored) {
		} catch (InvocationTargetException ignored) {
		} catch (NoSuchMethodException ignored) {
		} catch (SecurityException ignored) {}

		return Optional.of(schema)
	}
}
