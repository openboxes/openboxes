/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse

import grails.util.Holders
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.inventory.Transaction
import org.springframework.util.StringUtils

class LocalizationUtil {

    static final def delimiter = '\\|'
    static final def localeDelimiter = ':'
    static final String DASH = '-'

    static LocalizationService getLocalizationService() {
        return Holders.getGrailsApplication().getParentContext().getBean("localizationService")
    }

    static Locale getCurrentLocale() {
        return localizationService.currentLocale
    }

    /**
     * Convenience util to handle country specific language (for example Mexican Spanish). As we
     * used to provide supported locales in config as a language code in ISO 639 alpha-2 or
     * ISO 639 alpha-3 format, to support country specific languages we have to add a country
     * code in ISO 3166 alpha-2 or UN M.49 numeric-3 format to it.
     * As there is no Locale constructor that handles `<languageCode>_<countryCode>` or
     * `<languageCode>-<countryCode>` we have to handle it here.
     * @param localeCode String with locale code represented as single language code (ex.: "en", "es"),
     *                   or combined with country code (ex.: "en_GB", "es_MX", "en-GB", "es-MX")
     * @return Locale
     * */
    static Locale getLocale(String localeCode) {
        // Since spring 5.0.4 StringUtils.parseLocale(localeCode) could be used to handle both cases
        if (localeCode?.contains(DASH)) {
            return Locale.forLanguageTag(localeCode)
        }

        return StringUtils.parseLocaleString(localeCode)
    }

    static List<Locale> getSupportedLocales() {
        def supportedLocales = Holders.config.openboxes.locale.supportedLocales
        return supportedLocales.collect { getLocale(it) }
    }

    static String getLocalizedString(Transaction transaction) {
        String label = ""
        def localizationService = getLocalizationService()
        if (localizationService) {
            label += localizationService.formatMetadata(transaction?.transactionType)
            if (transaction.transactionNumber) {
                label += " " + transaction.transactionNumber
            }
        } else {
            label += transaction?.transactionType
            label += " " + transaction?.transactionNumber
        }
        return label
    }

    /**
     * Returns the value associated with the passed locale
     * If locale is null, returns the default value
     */
    static String getLocalizedString(String str) {
        return getLocalizedString(str, localizationService.currentLocale)
    }

    /**
     * Returns the value associated with the passed locale
     * If locale is null, returns the default value
     */
    static String getLocalizedString(String str, Locale locale) {

        // return blank string if no value
        if (!str) {
            return ""
        }

        // split into the the various localized values
        def strings = str.split(delimiter)

        // if there aren't any values, return empty string
        if (strings.size() == 0) {
            return ""
        }

        // the default value is the first value in the list
        def defaultValue = strings[0]

        // if there is only one value, or if no locale has been specified, just return the default value
        if (strings.size() == 1 || locale == null) {
            return defaultValue
        }

        // the other values are the potential localized values
        def localizedValues = strings[1..strings.size() - 1]

        // see if we can find the user locale in the list of localized values
        def localizedValue

        localizedValues.each {
            if (it.split(localeDelimiter).size() == 2) {
                // sanity check that we have just two values (the locale code and the value)
                if (it.split(localeDelimiter)[0] == locale.getLanguage()) {
                    localizedValue = it.split(localeDelimiter)[1]
                    return
                }
            }
        }

        if (localizedValue) {
            // if we've found a localized value for the current locale
            return localizedValue
        } else {
            // otherwise, just return the default
            return defaultValue
        }
    }


    /**
     * Returns the default value for this string (which is just the first name in the pipe-delimited list)
     */
    static String getDefaultString(String str) {
        return getLocalizedString(str, null)
    }


    static boolean isSpanishMexico(Locale locale) {
        return locale == new Locale("es", "MX")
    }

    static String getLocalizedOrderImportDateFormat(Locale locale) {
        if (isSpanishMexico(locale)) {
            return Constants.SPANISH_MEXICO_ORDER_IMPORT_DATE_FORMAT
        }
        return Constants.DEFAULT_ORDER_IMPORT_DATE_FORMAT
    }
}
