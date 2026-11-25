package org.pih.warehouse.core.formatter

import org.springframework.core.GenericTypeResolver

/**
 * For converting objects to Strings.
 *
 * A typical use case is formatting fields for display (in GSPs) or for serialization in API responses.
 */
abstract class Formatter<Type, Context extends FormatterContext> {

    /**
     * Contains the actual logic for formatting the given object to a String.
     * Does not need to handle errors or default values. This will be done automatically.
     */
    abstract protected String doFormat(Type toFormat, Context context)

    /**
     * Returns true if we should default to using this formatter for a given output type.
     *
     * Any default formatter must override this method to return true. Only one formatter is allowed to be the default
     * for each type. Defining multiple will cause a startup error.
     *
     * It should generally be quite clear what the default formatter should be for any type, because it will typically
     * be named as such. For example, StringParser is the default formatter for Strings.
     *
     * This functionality is what allows the DefaultTypeParser to function. For it to work, the DefaultTypeParser
     * needs to be able to associate each output type with a single formatter, but there can exist multiple
     * formatters that output to the same type. For example, StringParser and EmailParser both output Strings.
     * Because of this, we define one (and *only* one) formatter as the default formatter for each output type. That way,
     * if we call DefaultTypeParser.format(obj, String), it'll know to use StringParser, and not EmailParser.
     */
    boolean isDefaultFormatterForType() {
        return false
    }

    /**
     * Returns the type of the source object that the formatter is converting from.
     */
    Class<Type> getSourceType() {
        return (Class<Type>) GenericTypeResolver.resolveTypeArguments(getClass(), Formatter.class)[0]
    }

    /**
     * Returns the type of the context object that we will use when formatting.
     */
    Class<Context> getContextType() {
        return (Class<Context>) GenericTypeResolver.resolveTypeArguments(getClass(), Formatter.class)[1]
    }

    /**
     * Constructs a context object containing all default values.
     * For use primarily when no context is specified when formatting.
     */
    private Context getDefaultContext() {
        return getContextType().newInstance() as Context
    }

    /**
     * Converts the given object to a String.
     */
    String format(Type toFormat, Context context=null) {
        Context contextToUse = context ?: getDefaultContext()

        if (toFormat == null) {
            return contextToUse.defaultValue
        }

        try {
            String formattedValue = doFormat(toFormat, contextToUse)
            // If the formatting returns null, we assume toFormat is null-like, and so return the default value
            return formattedValue == null ? contextToUse.defaultValue : formattedValue
        } catch (Exception e) {
            if (contextToUse.errorOnFormatFailure) {
                throw e
            }
            return null
        }
    }
}
