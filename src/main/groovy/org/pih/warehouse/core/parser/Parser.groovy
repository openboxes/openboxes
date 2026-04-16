package org.pih.warehouse.core.parser

import org.springframework.core.GenericTypeResolver

/**
 * For converting input objects to a certain type.
 */
abstract class Parser<Type, Context extends ParserContext<Type>> {

    /**
     * Contains the actual logic for converting the given object to the type associated with the parser.
     * Does not need to handle errors or default values. This will be done automatically.
     */
    abstract protected Type parseImpl(Object toParse, Context context)

    /**
     * Returns true if we should default to using this parser when converting to the given output type.
     *
     * Any default parser must override this method to return true. Only one parser is allowed to be the default
     * for each type. Defining multiple will cause a startup error.
     *
     * It should generally be quite clear what the default parser should be for any type, because it will typically
     * be named as such. For example, StringParser is the default parser for Strings.
     *
     * This functionality is what allows the DefaultTypeParser to function. For it to work, the DefaultTypeParser
     * needs to be able to associate each output type with a single parser, but there can exist multiple
     * parsers that output to the same type. For example, StringParser and EmailParser both output Strings.
     * Because of this, we define one (and *only* one) parser as the default parser for each output type. That way,
     * if we call DefaultTypeParser.parse(obj, String), it'll know to use StringParser, and not EmailParser.
     */
    boolean isDefaultParserForType() {
        return false
    }

    /**
     * Returns the type that the parser converts the input into.
     */
    Class<Type> getTargetType() {
        return (Class<Type>) GenericTypeResolver.resolveTypeArguments(getClass(), Parser.class)[0]
    }

    /**
     * Returns the type of the context object that we will use when parsing.
     */
    Class<Context> getContextType() {
        return (Class<Context>) GenericTypeResolver.resolveTypeArguments(getClass(), Parser.class)[1]
    }

    /**
     * Constructs a context object containing all default values.
     * For use primarily when no context is specified for the parse.
     */
    private Context getDefaultContext() {
        return getContextType().newInstance() as Context
    }

    /**
     * Converts the given object to the type associated with the parser.
     */
    Type parse(Object toParse, Context context=null) {
        Context contextToUse = context ?: getDefaultContext()

        if (toParse == null) {
            return contextToUse.defaultValue
        }

        try {
            Type parsedValue = parseImpl(toParse, contextToUse)
            // If the parsing returns null, we assume toParse is null-like, and so return the default value
            return parsedValue == null ? contextToUse.defaultValue : parsedValue
        } catch (Exception e) {
            if (contextToUse.errorOnParseFailure) {
                throw (e)
            }
            return null
        }
    }
}
