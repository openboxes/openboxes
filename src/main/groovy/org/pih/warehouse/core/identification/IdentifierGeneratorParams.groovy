package org.pih.warehouse.core.identification

import groovy.transform.builder.Builder
import org.grails.datastore.gorm.GormEntity

/**
 * A POJO for holding all the configuration options when generating a custom id.
 *
 * Should be used sparingly as it is inflexible to dynamic customization. Any time we want to modify how an identifier
 * is generated with these custom params, it'll need to be done via a code change (vs a dynamic property change if
 * the generator.
 */
@Builder
class IdentifierGeneratorParams {

    /**
     * If set, will override the format as defined in the app config.
     */
    String formatOverride

    /**
     * A string to be prepended to the front of the id.
     *
     * Useful for prepending a sub-feature identifier (such as "R-" or "PO-") without needing to define a whole new
     * identifier service.
     */
    String prefix

    /**
     * A string to be appended to the end of the id.
     */
    String suffix

    /**
     * The GORM entity whose fields will be used to fill the template.
     *
     * This will usually be the entity that we're generating the identifier for, but it isn't strictly required to be.
     * Any entity fields that you want to be used must also be defined in "openboxes.identifier.<entity>.properties".
     */
    GormEntity templateEntity

    /**
     * A map of non-entity-specific values/properties that will be used to fill the template.
     *
     * These must be defined in the .format property in GStrings prefixed with the "custom" category. Ex: ${custom.x}
     * This is to distinguish the custom keys from entity-specific keys (which are defined in the .properties property).
     */
    Map customKeys
}
