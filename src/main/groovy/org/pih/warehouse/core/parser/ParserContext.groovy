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

    /**
     * The class type that the parser outputs. Needed in certain scenarios due to Java type erasure.
     *
     * This field is only required if the parser has a non-concrete generic type. For example, in EnumParser<T>,
     * even if you define new EnumParser<SomeType>(), the type of "SomeType" cannot be determined at runtime.
     *
     * However, in the case of StringParser extends Parser<String, ParserContext<String>>, because String parser
     * concretely defines T to always be String, the type can be resolved automagically (see Parser.getTargetType()).
     */
    Class<T> typeToParseTo
}
