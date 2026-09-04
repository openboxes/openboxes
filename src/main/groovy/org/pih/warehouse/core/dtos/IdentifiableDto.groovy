package org.pih.warehouse.core.dtos

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import org.apache.commons.lang.builder.HashCodeBuilder

/**
 * Represents a DTO that is identifiable via some identifier field.
 * In practice, this means that the DTO represents an instance of a domain entity.
 *
 * If there is a cyclical reference in the DTO chain, then the second time that a DTO appears in the hierarchy
 * the "id" field will be used in place of the full DTO. This prevents infinite recursion when serializing the objects.
 *
 * For example, if we have the following structure:
 *
 * class ADto implements IdentifiableDto {
 *     BDto b
 * }
 *
 * class BDto implements IdentifiableDto {
 *     ADto a
 * }
 *
 * then serializing an ADto instance might result in:
 *
 * {
 *     id: "123",
 *     b: {
 *         id: "456",
 *         a: "123"
 *     }
 * }
 *
 * The second appearance of "a" is reduced down to just the value of its "id" field.
 *
 * Notably, this only catches cycles at the point of serialization. If there is a cycle that occurs before this
 * point, such as when converting entities to DTOs, that can still result in an infinite recursion stack overflow.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator, property = "id")
trait IdentifiableDto {

    /*
     * We need to annotate trait fields with "@JsonProperty" even if the declared field name matches the serialized
     * field name due to how Groovy handles traits. Because classes can implement multiple traits, Groovy changes
     * the name of trait fields at compile time, prefixing them with the trait's fully-qualified name. It does this
     * to avoid name collisions. In this case: "id" becomes "org_pih_warehouse_core_dtos_IdentifiableDto__id".
     * The auto-generated getters/setters mask this behaviour at compile time, but Jackson by default looks at
     * the underlying field itself so we need to coerce it to use the declared field name when serializing.
     */

    /**
     * A unique identifier for the object. Typically the domain entity id.
     * Used when checking for cyclical references in the DTO chain.
     */
    @JsonProperty("id")
    String id

    @Override
    boolean equals(Object other) {
        if (this.is(other)) {
            return true
        }

        if (!(other instanceof IdentifiableDto)) {
            return false
        }

        IdentifiableDto that = (IdentifiableDto) other
        return id == that.id
    }

    @Override
    int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .toHashCode()
    }
}
