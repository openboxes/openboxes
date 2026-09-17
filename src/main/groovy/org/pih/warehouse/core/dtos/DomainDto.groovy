package org.pih.warehouse.core.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import org.grails.datastore.gorm.GormEntity

import org.pih.warehouse.core.Identifiable

/**
 * A DTO representation of a domain entity.
 *
 * If you need to specify a mapper for converting a domain entity into this DTO, use
 * {@link org.pih.warehouse.core.mapper.EntityToDtoMapper}.
 */
trait DomainDto<E extends GormEntity> implements Identifiable {
    /*
     * We need to annotate trait fields with "@JsonProperty" even if the declared field name matches the serialized
     * field name due to how Groovy handles traits. Because classes can implement multiple traits, Groovy changes
     * the name of trait fields at compile time, prefixing them with the trait's fully-qualified name. It does this
     * to avoid name collisions. In this case: "id" becomes "org_pih_warehouse_core_dtos_IdentifiableDto__id".
     * The auto-generated getters/setters mask this behaviour at compile time, but Jackson by default looks at
     * the underlying field itself so we need to coerce it to use the declared field name when serializing.
     */

    /**
     * The domain entity id.
     */
    @JsonProperty("id")
    String id
}
