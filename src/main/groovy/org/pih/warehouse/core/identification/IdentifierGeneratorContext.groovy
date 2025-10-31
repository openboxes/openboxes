package org.pih.warehouse.core.identification

import groovy.transform.builder.Builder

/**
 * A POJO for holding all the configuration options when generating a custom id.
 *
 * Should be used sparingly as it is inflexible to dynamic customization. Any time we want to modify how an identifier
 * is generated with these custom params, it'll need to be done via a code change (vs a dynamic property change).
 */
@Builder
class IdentifierGeneratorContext {

    /**
     * If set, will override the format to use when generating the identifier. Takes precedence over the value in
     * openboxes.identifier.x.format, even if there's a feature-specific value set.
     */
    String formatOverride

    /**
     * If set, will override the random template to use when generating the identifier. Takes precedence over the
     * value in openboxes.identifier.x.random.template, even if there's a feature-specific value set.
     */
    String randomTemplateOverride

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
     * A map of non-entity-specific values/properties that will be used to fill the template.
     *
     * These must be defined in the .format property in GStrings prefixed with the "custom" category. Ex: ${custom.x}
     * This is to distinguish the custom keys from entity-specific keys (which are defined in the .properties property).
     */
    Map<String, String> customProperties = new HashMap<>()
}
