package org.pih.warehouse.importer

import org.pih.warehouse.core.parser.Parser
import org.pih.warehouse.core.parser.ParserContext

/**
 * Configuration for binding an individual field to some type.
 */
class BulkDataBinderFieldConfig {

    /**
     * The strategy to use when binding the field. If MANUAL, the data binder will not bind the field and you are
     * expected to handle the binding yourself in the customBindData method of the data binder.
     */
    DataBindingMethod dataBindingMethod = DataBindingMethod.AUTO

    /**
     * The parser class to use when binding the field.
     *
     * This is only required if you don't want to use the default parser associated with the field, or if the field
     * does not have a default parser. For example, if the field is a String, you don't need to specify the parser
     * if you intend to use the StringParser because that is the default parser for Strings.
     */
    Class<Parser> parser

    /**
     * Defines any custom behaviour when parsing the field.
     */
    ParserContext parserContext
}
