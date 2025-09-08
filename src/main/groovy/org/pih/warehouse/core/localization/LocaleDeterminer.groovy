package org.pih.warehouse.core.localization

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.session.SessionManager

/**
 * Handles fetching locales.
 *
 * If you're trying to translate a message, use {@link org.pih.warehouse.core.localization.MessageLocalizer}.
 */
@Component
class LocaleDeterminer {  // Note I wanted to call this LocaleResolver but that's already a Spring component

    @Autowired
    SessionManager sessionManager

    @Value('${openboxes.locale.defaultLocale}')
    String defaultLocale

    /**
     * Get a locale based on the given language code string.
     */
    Locale asLocale(String localeCode) {
        return LocalizationUtil.getLocale(localeCode)
    }

    /**
     * Fetch the locale of the user/request associated with the current thread.
     * Falls back to the default locale if one can't be determined.
     */
    Locale getCurrentLocale() {
        // Extract the locale from the Spring context. We need a fallback for when we're not in the context
        // of an HTTP request, such as when running unit tests or console commands.
        return LocaleContextHolder.locale ?: getDefaultLocale()
    }

    /**
     * Fetch the configured default locale. This is typically used as a fallback when no locale can be determined.
     */
    Locale getDefaultLocale() {
        // In the off chance that the default locale property isn't set, use the system default.
        return StringUtils.isBlank(defaultLocale) ? Locale.getDefault() : new Locale(defaultLocale)
    }
}
