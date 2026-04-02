package org.pih.warehouse.core.parser

/**
 * Base context object containing any contextual information required when parsing fields.
 * A majority of cases require only the default parser behaviour so this context object will often not be required.
 */
class ParserContext<T> {

    /**
     * The default value to return if the object being parsed is null.
     */
    T defaultValue = null

    /**
     * True if we should throw an error if parsing fails. Otherwise will return null.
     */
    Boolean errorOnParseFailure = true
}
