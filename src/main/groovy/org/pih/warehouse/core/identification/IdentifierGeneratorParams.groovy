package org.pih.warehouse.core.identification

import groovy.transform.builder.Builder
import org.grails.datastore.gorm.GormEntity

/**
 * A POJO for holding all the configuration options when generating a custom id.
 */
@Builder
class IdentifierGeneratorParams {

    /**
     * If set, will override the format as defined in the app config.
     *
     * Should be used sparingly as it is inflexible to customization. Anything that sets this field requires
     * a code change in order to modify the format.
     */
    String formatOverride

    /**
     * A string to be prepended to the front of the id.
     *
     * Should be used sparingly as it is inflexible to customization. Anything that sets this field requires
     * a code change in order to modify the format.
     */
    String prefix

    /**
     * A string to be appended to the end of the id.
     *
     * Should be used sparingly as it is inflexible to customization. Anything that sets this field requires
     * a code change in order to modify the format.
     */
    String suffix

    /**
     * The GORM entity that will be used to fill the template.
     * This will usually be the entity that we're generating the identifier for, but it isn't strictly required to be.
     * Any fields that you want to be used must also be defined in "openboxes.identifer.x.properties"
     */
    GormEntity templateEntity

    /**
     * A map of non-entity-specific values/properties that will be used to fill the template.
     */
    Map templateCustomValues
}
