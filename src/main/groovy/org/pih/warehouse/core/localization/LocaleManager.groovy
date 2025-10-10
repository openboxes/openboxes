package org.pih.warehouse.core.localization

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.session.SessionManager

/**
 * Manages fetching and setting locales.
 *
 * If you're trying to translate a message, use {@link org.pih.warehouse.core.localization.MessageLocalizer}.
 */
@Component
class LocaleManager {

    @Autowired
    SessionManager sessionManager

    @Value('${openboxes.locale.defaultLocale}')
    Locale defaultLocale

    @Value('${openboxes.locale.localizationModeLocale}')
    Locale localizationModeLocale

    /**
     * Get a locale based on the given language code string.
     *
     * @param localeCode Should be a valid ISO 3166 string with an optional region code (ex: "es", "es-MX", "es_MX")
     * @throws IllegalArgumentException in case of an invalid locale specification
     */
    Locale asLocale(String localeCode) {
        return LocalizationUtil.getLocale(localeCode)
    }

    /**
     * Fetch the locale of the user/request associated with the current thread.
     *
     * Falls back to the user's default locale if the session doesn't have a locale yet,
     * which in turn falls back to the global default locale if the user has no default configured.
     */
    Locale getCurrentLocale() {
        // If the session has a Locale, use that
        Locale locale = sessionManager.getLocale()
        if (locale) {
            return locale
        }
        // Otherwise if the OpenBoxes User has a default locale, use that
        locale = sessionManager.getUser()?.locale
        if (locale) {
            return locale
        }
        // Otherwise use the system default locale
        return getDefaultLocale()
    }

    /**
     * Sets the locale for the current session.
     *
     * @param localeCode Should be a valid ISO 3166 string with an optional region code (ex: "es", or "es-MX")
     * @param previousLocaleToUse Overrides the previous locale to set. If null, will use the session's current locale
     */
    Locale setCurrentLocale(String localeCode, Locale previousLocaleToUse=null) {
        return setCurrentLocale(asLocale(localeCode), previousLocaleToUse)
    }

    /**
     * Sets the locale for the current session.
     *
     * @param locale The locale to set for the session
     * @param previousLocaleToUse Overrides the previous locale to set. If null, will use the session's current locale
     */
    Locale setCurrentLocale(Locale locale, Locale previousLocaleToUse=null) {
        Locale currentLocale = sessionManager.getLocale()
        if (locale == currentLocale) {
            return locale
        }

        // Don't allow the previous locale to be the localization mode locale. We don't want to ever restore to it.
        Locale previousLocale = previousLocaleToUse ?: getCurrentLocale()
        sessionManager.setPreviousLocale(previousLocale == localizationModeLocale ? null : previousLocale)

        sessionManager.setLocale(locale)
        sessionManager.setIsInLocalizationMode(locale == localizationModeLocale)

        return locale
    }

    /**
     * Fetch the configured default locale. This is typically used as a fallback when no locale can be determined.
     */
    Locale getDefaultLocale() {
        // In the off chance that the default locale property isn't set, use the system default.
        return defaultLocale ?: Locale.getDefault()
    }

    /**
     * Put the user in localization mode by setting their local to the special Crowdin pseudo-language.
     */
    void enableLocalizationMode() {
        if (sessionManager.isInLocalizationMode()) {
            return
        }

        setCurrentLocale(localizationModeLocale)
    }

    /**
     * Disables localization mode, restoring the session to the locale that it was in before entering the mode.
     *
     * @param localeCodeToSwitchTo the locale to switch the user to after disabling the mode. If null, will use the
     *                             user's previous locale
     */
    void disableLocalizationMode(String localeCodeToSwitchTo) {
        if (!sessionManager.isInLocalizationMode()) {
            return
        }

        // If we're disabling localization mode because we're actively switching to a different locale,
        // use that one, else use the locale that the session was using previously.
        Locale locale = StringUtils.isBlank(localeCodeToSwitchTo) ?
                sessionManager.getPreviousLocale() :
                asLocale(localeCodeToSwitchTo)

        // Set the previous locale to null to guarantee we won't accidentally restore back to it (which can cause us
        // to get stuck in localization mode). We only use previous locale for localization mode so this is fine to do.
        setCurrentLocale(locale, null)
    }
}
