package org.pih.warehouse.core.localization

import java.text.MessageFormat
import org.apache.commons.lang.StringUtils
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
    boolean localizationDatabaseEnabled

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

        // Custom message overrides always take priority over those in messages.properties files.
        String messageOverride = getMessageOverride(code, args, localeToUse)
        if (messageOverride != null) {
            return messageOverride
        }

        // If no message override is specified, localize via Spring's messages.properties files, which automatically
        // handles character encoding as well as region > language locale fall backs (ie if the message is not found
        // in messages_es_MX.properties, it will look in 'messages_es.properties').
        return messageSource.getMessage(code, args, code, localeToUse)
    }

    /**
     * Returns a custom message override if one exists in the Localization database.
     *
     * We allow localization for individual codes to be overridden by data in the Localization database table.
     * This allows individual implementations to add translations specific to their unique setup dynamically
     * without needing to modify the shared messages.properties files.
     */
    private String getMessageOverride(String code, Object[] args=[], Locale locale) {
        if (!localizationDatabaseEnabled) {
            return null
        }

        // TODO: We should fetch the database overrides for the fallback locales as well so that we can default
        //       to those if a message is in the database for 'es' but not in the 'es_MX' messages.properties file.

        String message = Localization.findByCodeAndLocale(code, locale.toString())?.text
        return StringUtils.isBlank(message) ? null : MessageFormat.format(message, args).encodeAsHTML()
    }
}
