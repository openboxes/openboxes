package org.pih.warehouse.core.localization

/**
 * A simple wrapper class on a translatable message/label.
 * Used to indicate that translations can be applied to the message.
 */
class LocalizableMessage {

    /**
     * Represents a translation label as found in message.properties.
     * Of the format: "x.y.z"
     */
    String code

    /**
     * The fallback text to display in case we fail to localize the text.
     */
    String defaultMessage

    /**
     * An ordered array of arguments as required by the code.
     *
     * Ex: If the code has a value of "Something: {0}, something else: {1}" then we'd expect args to have two values.
     */
    Object[] args

    LocalizableMessage(String code, String defaultMessage=null, args=[]) {
        this.code = code
        this.defaultMessage = defaultMessage
        this.args = args
    }
}
