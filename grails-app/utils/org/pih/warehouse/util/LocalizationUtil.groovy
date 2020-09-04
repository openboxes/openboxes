/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.util

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.inventory.Transaction

class LocalizationUtil {

    static final def delimiter = '\\|'
    static final def localeDelimiter = ':'

    static LocalizationService getLocalizationService() {
        return ApplicationHolder?.application?.mainContext?.getBean("localizationService")
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
        return getLocalizedString(str, localizationService.getCurrentLocale())
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
}
