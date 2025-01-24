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

import java.text.DateFormat
import java.text.SimpleDateFormat

class FormatTagLib {

    static namespace = "format"

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
     *
     * Output from this method will be HTML-escaped, which is always what you want
     * when calling <format:product> but may not be when calling format.product().
     */
    def product = { attrs ->
        if (attrs.product != null) {
            // use the locale specified in the tag if it exists, otherwise use the user locale if it exists, otherwise use the system default locale
            // (note that we explicitly do a containsKey test because it is possible that the locale attribute has been specified but has been set to null--which means show the default locale)
            Locale defaultLocale = new Locale(Holders.grailsApplication.config.openboxes.locale.defaultLocale)
            Locale locale = attrs.containsKey('locale') ? attrs.locale : session?.user?.locale ?: defaultLocale
            out << LocalizationUtil.getLocalizedString(attrs.product.name, locale).encodeAsHTML()
        }
        // TODO: add more formats
    }

    /**
     * Custom tag to display a product synonym name for DISPLAY_NAME
     *
     * Attributes:
     * product (required): the product to display
     * showTooltip (optional): When true returns span tag with title attribute of default product name
     * showProductCode (optional): appends a product code to the beginning of the product name
     * locale (optional): the locale to localize for; if no locale is specified, the current locale is used
     *
     */
    def displayName = { attrs ->
        attrs.showTooltip = attrs.showTooltip ?: false
        attrs.locale = attrs.locale ?: "default"
        attrs.showProductCode = attrs.showProductCode ?: false

        if (attrs.product) {
            def displayNames = attrs.product?.displayNames

            if (attrs.showTooltip) {
                out << g.render(
                        template: '/taglib/productDisplayName',
                        model: [
                                product         : [ name: attrs.product?.name, productCode: attrs.product?.productCode ],
                                productSupplier : [ name: attrs.productSupplier?.name, code: attrs.productSupplier?.code ],
                                displayName     : displayNames[attrs.locale],
                                showProductCode : attrs.showProductCode,
                        ],
                )
            } else {
                String productDisplayName = displayNames[attrs.locale] ?: attrs.product?.name
                out << (attrs.showProductCode ? "${attrs.product?.productCode} ${productDisplayName}" : productDisplayName).encodeAsHTML()
            }
        }
    }

    /**
     * Custom tag to display a product synonym name for DISPLAY_NAME in appropriate color
     *
     * Attributes:
     * product (required): the product to display
     * showTooltip (optional): When true returns span tag with title attribute of default product name
     * showProductCode (optional): appends a product code to the beginning of the product name
     * locale (optional): the locale to localize for; if no locale is specified, the current locale is used
     *
     */
    def displayNameWithColor = { attrs ->
        out << "<span style='color: ${attrs?.product?.color ?: "inherit"}'> ${displayName(attrs)} </span>"
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
     *
     * Output from this method will be HTML-escaped, which is always what you want
     * when calling <format:category> but may not be when calling format.category().
     */
    def category = { attrs ->
        if (attrs.category) {
            // use the locale specified in the tag if it exists, otherwise use the user locale if it exists, otherwise use the system default locale
            // (note that we explicitly do a containsKey test because it is possible that the locale attribute has been specified but has been set to null--which means show the default locale)
            Locale defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
            Locale locale = attrs.containsKey('locale') ? attrs.locale : session?.user?.locale ?: defaultLocale

            // default format is to display the localized name of the catergory
            def value = LocalizationUtil.getLocalizedString(attrs?.category?.name, locale)
            if (attrs.shorten) {
                if (value.length() > 35) {
                    value = value.substring(0, 35) + "..."
                }
            }
            out << value.encodeAsHTML()
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
     *
     * Output from this method will be HTML-escaped.
     */
    def metadata = { attrs ->

        if (attrs.obj) {
            // use the locale specified in the tag if it exists, otherwise use the user locale if it exists, otherwise use the system default locale
            // (note that we explicitly do a containsKey test because it is possible that the locale attribute has been specified but has been set to null--which means show the default locale)
            Locale defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
            Locale locale = attrs.containsKey('locale') ? attrs.locale : (session?.locale ?: session?.user?.locale) ?: defaultLocale

            // handle String; localize the string directly
            if (attrs.obj instanceof String) {
                out << LocalizationUtil.getLocalizedString(attrs.obj, locale).encodeAsHTML()
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
                    out << LocalizationUtil.getLocalizedString(attrs?.obj?.name, locale).encodeAsHTML()
                }
                // Otherwise, use value of toString() method (probably just going to return an unlocalized string)
                else {
                    out << LocalizationUtil.getLocalizedString(attrs?.obj?.toString(), locale).encodeAsHTML()
                }
            }
        }
    }
}
