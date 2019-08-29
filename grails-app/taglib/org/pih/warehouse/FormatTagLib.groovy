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

import org.pih.warehouse.core.Constants
import org.pih.warehouse.util.LocalizationUtil

import java.text.DateFormat
import java.text.SimpleDateFormat

class FormatTagLib {

    static namespace = "format"
    def grailsApplication

    /**
     * Formats a Date
     * @attr obj REQUIRED the date to format
     */
    def date = { attrs, body ->
        if (attrs.obj != null) {
            DateFormat df = new SimpleDateFormat((attrs.format) ?: Constants.DEFAULT_DATE_FORMAT)
            out << df.format(attrs.obj)
        }
    }

    /**
     * Formats a DateTime
     * @attr obj REQUIRED the date to format
     */
    def datetime = { attrs, body ->
        if (attrs.obj != null) {
            DateFormat df = new SimpleDateFormat(Constants.DEFAULT_DATE_TIME_FORMAT)
            TimeZone tz = session.timezone
            if (tz != null) {
                df.setTimeZone(tz)
            }
            out << df.format(attrs.obj)
        }
    }

    /**
     *  Formats a Time
     * @attr obj REQUIRED the date to format
     */
    def time = { attrs, body ->
        if (attrs.obj != null) {
            DateFormat df = new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT)
            TimeZone tz = session.timezone
            if (tz != null) {
                df.setTimeZone(tz)
            }
            out << df.format(attrs.obj)
        }
    }


    /**
     * Formats an Expiration Date
     * @attr obj REQUIRED the date to format
     */
    def expirationDate = { attrs, body ->
        if (attrs.obj) {
            DateFormat df = new SimpleDateFormat(Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT)
            out << df.format(attrs.obj)
        } else {
            out << warehouse.message(code: 'default.never.label')
        }
    }

    /**
     * Custom tag to display a product
     *
     * Attributes:
     * product (required): the product to display
     * locale (optional): the locale to localize for; if no locale is specified, the locale associated with the current user is used;
     * 				   if no current user, the system default locale is used, (specified in grailsApplication.config.openboxes.locale.defaultLocale);
     * 				   if the locale attribute is specified, but set to "null", the "default" name is returned
     *
     * Currently simply displays the localized name of the product
     */
    def product = { attrs ->
        if (attrs.product != null) {
            // use the locale specified in the tag if it exists, otherwise use the user locale if it exists, otherwise use the system default locale
            // (note that we explicitly do a containsKey test because it is possible that the locale attribute has been specified but has been set to null--which means show the default locale)
            Locale defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
            Locale locale = attrs.containsKey('locale') ? attrs.locale : session?.user?.locale ?: defaultLocale
            def value = ""
            value += LocalizationUtil.getLocalizedString(attrs.product.name.encodeAsHTML(), locale)

            // default format is to display the localized name of the product
            out << value
        }
        // TODO: add more formats
    }

    /**
     * Custom tag to display a category
     *
     * Attributes:
     * category (required): the category to display
     * locale (optional): the locale to localize for; if no locale is specified, the locale associated with the current user is used;
     * 				      if no current user, the system default locale is used, (specified in grailsApplication.config.openboxes.locale.defaultLocale);
     * 				      if the locale attribute is specified, but set to "null", the "default" name is returned
     *
     * Currently simply displays the localized name of the category
     */
    def category = { attrs ->
        if (attrs.category) {
            // use the locale specified in the tag if it exists, otherwise use the user locale if it exists, otherwise use the system default locale
            // (note that we explicitly do a containsKey test because it is possible that the locale attribute has been specified but has been set to null--which means show the default locale)
            Locale defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
            Locale locale = attrs.containsKey('locale') ? attrs.locale : session?.user?.locale ?: defaultLocale

            // default format is to display the localized name of the catergory
            def value = LocalizationUtil.getLocalizedString(attrs?.category?.name?.encodeAsHTML(), locale)
            if (attrs.shorten) {
                if (value.length() > 35) {
                    value = value.substring(0, 35) + "..."
                }
            }
            out << value
        }
        // TODO: add more formats
    }

    /**
     * Custom tag to display warehouse metadata is a standard, localized manner
     *
     * Attributes:
     * obj (required): the object to localize--tag currently supports standard OpenBoxes objects, as well as Strings, and Enums
     * locale (optional): the locale to localize for; if no locale is specified, the locale associated with the current user is used;
     * 				   if no current user, the system default locale is used, (specified in grailsApplication.config.openboxes.locale.defaultLocale);
     * 				   if the locale attribute is specified, but set to "null", the "default" name is returned
     *
     * If the obj is a String, the tag assumes the string is in format "Default Value|fr:French Value|es:Spanish Value"
     * If the obj is an OpenBoxes object, the tag operates on the "name" property of the object, assuming it is a String in the format specified above
     * If the obj is an enum, the tag returns the localized message.properties code "enum.className.value" (ie enum.ShipmentStatusCode.PENDING)
     */
    def metadata = { attrs ->

        if (attrs.obj) {
            // use the locale specified in the tag if it exists, otherwise use the user locale if it exists, otherwise use the system default locale
            // (note that we explicitly do a containsKey test because it is possible that the locale attribute has been specified but has been set to null--which means show the default locale)
            Locale defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
            Locale locale = attrs.containsKey('locale') ? attrs.locale : session?.user?.locale ?: defaultLocale

            // handle String; localize the string directly
            if (attrs.obj instanceof String) {
                out << LocalizationUtil.getLocalizedString(attrs.obj, locale)
            }
            // handle Enums; by convention, the localized text for a Enum is stored in the message property enum.className.value  (ie enum.ShipmentStatusCode.PENDING)
            else if (attrs.obj instanceof Enum) {
                String className = attrs.obj.getClass().getSimpleName()
                out << warehouse.message(code: 'enum.' + className + "." + attrs.obj, locale: locale)
            }
            // for all other objects, return the localized version of the name
            else {
                // If there's a 'name' attribute on the object
                if (attrs?.obj?.properties?.get("name")) {
                    out << LocalizationUtil.getLocalizedString(attrs?.obj?.name, locale)
                }
                // Otherwise, use value of toString() method (probably just going to return an unlocalized string)
                else {
                    out << LocalizationUtil.getLocalizedString(attrs?.obj?.toString(), locale)
                }
            }
        }
    }
}
