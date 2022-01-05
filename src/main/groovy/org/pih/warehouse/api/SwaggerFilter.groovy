package org.pih.warehouse.api

import io.swagger.v3.core.filter.AbstractSpecFilter
import io.swagger.v3.oas.models.media.Schema
import org.pih.warehouse.core.Location

import java.util.List
import java.util.Map
import java.util.Optional

class SwaggerFilter extends AbstractSpecFilter {

	@Override
	Optional<Schema> filterSchemaProperty(Schema property, Schema schema, String propName, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
		if (schema?.name == "Location") {

			// do not expose GORM's hidden key fields for 1:1 mappings
			if (property?.name?.endsWith("Id")) {
				return Optional.empty()
			}
			// do not expose GORM's 1:many mappings
			if (Location.hasMany.any{it -> it.key == property?.name}) {
				return Optional.empty()
			}
		}
		return Optional.of(property)
	}

	@Override
	public Optional<Schema> filterSchema(Schema schema, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
		if (schema?.name == "Location") {
			Location.addImplicitSwaggerFields(schema)
		}

		return Optional.of(schema);
	}
}
