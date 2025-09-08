package org.pih.warehouse

import org.grails.plugins.web.taglib.ValidationTagLib

import org.pih.warehouse.core.localization.MessageLocalizer

class LocalizationTagLib {

    // Use Grails' namespace because we're overriding the default behaviour and so that the tag lib
    // is accessible automatically throughout the app.
    static namespace = 'g'

    MessageLocalizer messageLocalizer

    /**
     * Localizes a given message into the locale/language of the user associated with the request.
     *
     * Wraps Grails' built-in localizer with some custom behaviour.
     *
     * @attr code the label in message.properties to localize to
     * @attr args any parameters that the code needs
     * @attr locale An override for the locale to use
     * @attr error The ObjectError that is created when constraints are violated during object
     * @attr message The object to resolve the message for. Objects must implement MessageSourceResolvable
     */
    Closure message = { attrs, body ->
        // If this is an error message, let Grails' built-in message tag lib handle it.
        if (attrs.error || attrs.message) {
            ValidationTagLib validationTagLib = grailsApplication.mainContext.getBean(
                    'org.grails.plugins.web.taglib.ValidationTagLib') as ValidationTagLib

            out << validationTagLib.message.call(attrs)
            return
        }

        // Otherwise we'll handle it ourselves because we need to do some custom processing.
        out << messageLocalizer.localize(
                attrs?.code as String,
                attrs?.args as Object[],
                attrs?.locale as Locale)
    }
}
