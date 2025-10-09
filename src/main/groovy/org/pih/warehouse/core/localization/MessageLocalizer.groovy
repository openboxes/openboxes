package org.pih.warehouse.core.localization

import java.text.MessageFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component

import org.pih.warehouse.core.Localization
import org.pih.warehouse.core.session.SessionManager

/**
 * Translates messages into a specific locale.
 */
@Component
class MessageLocalizer {

    @Autowired
    MessageSource messageSource

    @Autowired
    LocaleManager localeManager

    @Autowired
    SessionManager sessionManager

    @Value('${openboxes.locale.custom.enabled}')
    boolean localeDatabaseEnabled

    /**
     * Translates a given localizable message into the locale of the requesting user.
     */
    String localize(LocalizableMessage message, Locale localeOverride=null) {
        return localize(message.code, message.args, localeOverride)
    }

    /**
     * Translates a given code/label into the locale of the requesting user.
     *
     * If the code does not resolve to a localization label, we return the code itself.
     * We do this so that it's very obvious when a code has been misconfigured. Otherwise
     * if we simply return some default English string we might not catch the issue right away.
     */
    String localize(String code, Object[] args=[], Locale localeOverride=null) {
        // It's rare, but we sometimes want to always display some string in the same locale, regardless of
        // the locale of the requesting user. For example, when displaying a language's name.
        Locale localeToUse = localeOverride ?: localeManager.getCurrentLocale()

        // We allow localization for individual codes to be overridden by data in the Localization database table so
        // check to see if an override is set. This allows individual implementations to modify translations
        // dynamically without needing to modify the message.properties files.
        if (localeDatabaseEnabled) {
            String message = Localization.findByCodeAndLocale(code, localeToUse.toString())?.text
            if (message) {
                return MessageFormat.format(message, args).encodeAsHTML()
            }
        }

        // If no message override is specified, localize via Spring's message.properties files (which automatically
        // handles character encoding).
        return messageSource.getMessage(code, args, code, localeToUse)
    }
}
