package org.pih.warehouse.core.localization

/**
 * Represents all of the localized messages of a given locale.
 */
class LocalizedMessagesDto {

    Properties messages
    String[] supportedLocales
    Locale currentLocale

    Map toJson() {
        [
                messages: messages,
                supportedLocales: supportedLocales,
                currentLocale: currentLocale,
        ]
    }
}
