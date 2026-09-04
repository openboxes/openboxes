package org.pih.warehouse.core.formatter

/**
 * Base context object containing contextual information for formatting fields.
 */
class FormatterContext {

    /**
     * The default value to return if the object being formatted is null.
     */
    String defaultValue = null

    /**
     * True if we should throw an error if formatting fails. Otherwise will return null.
     */
    Boolean errorOnFormatFailure = true
}
