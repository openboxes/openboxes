package org.pih.warehouse.core.localization

/**
 * Represents a single, localized message in the given locale.
 */
class LocalizedMessageDto {

    String code
    String message
    Locale currentLocale

    Map toJson() {
        [
                code: code,
                message: message,
                currentLocale: currentLocale,
        ]
    }
}
