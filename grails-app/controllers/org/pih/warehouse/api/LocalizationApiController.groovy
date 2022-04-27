/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.pih.warehouse.core.Localization

class LocalizationApiController {

    def messageSource
    def localizationService
    def grailsApplication

    def list = {
        String languageCode = params.lang
        String prefix = params.prefix

        String[] supportedLocales = grailsApplication.config.openboxes.locale.supportedLocales

        Locale defaultLocale = Locale.default
        Locale currentLocale = localizationService.getCurrentLocale()
        Locale selectedLocale = languageCode ? localizationService.getLocale(languageCode) : currentLocale

        // Get the default message properties as well as the message properties for the selected locale
        Properties defaultMessageProperties = localizationService.getMessagesProperties(defaultLocale)
        Properties selectedMessageProperties = localizationService.getMessagesProperties(selectedLocale)

        // Get all translations for the given prefix and locale from the database
        List<Localization> localizedMessages = prefix ? Localization.findAllByCodeIlikeAndLocale("${prefix}%", selectedLocale.language) :
                Localization.findAllByLocale(selectedLocale.language)
        Properties customMessageProperties = new Properties()
        localizedMessages.each { Localization localization ->
            customMessageProperties.put(localization.code, localization.text)
        }

        // Merge all messages from default, selected, and custom message properties
        Properties mergedMessageProperties = new Properties()
        mergedMessageProperties.putAll(defaultMessageProperties)
        mergedMessageProperties.putAll(selectedMessageProperties)
        mergedMessageProperties.putAll(customMessageProperties)

        Properties messageProperties = prefix ? mergedMessageProperties.findAll {
            it.key.startsWith(prefix)
        } : mergedMessageProperties

        render([messages: messageProperties?.sort(), supportedLocales: supportedLocales, currentLocale: selectedLocale] as JSON)
    }

    def read = {
        String languageCode = params.lang
        Locale locale = localizationService.getLocale(languageCode)
        String message = messageSource.getMessage(params.id, params.list("args").toArray(), locale)
        render([code: params.id, message: message, currentLocale: locale] as JSON)
    }

    static JSON localizeHelpScoutLabels(GrailsApplication app, Locale locale) {
        return [
            "labels": [
                /* fields in the Contact Us pane */
                "messageButtonLabel"     : app.mainContext.getMessage("helpscout.beacon.contact.messageButtonLabel.label", null, locale),
                "wereHereToHelp"         : app.mainContext.getMessage("helpscout.beacon.contact.wereHereToHelp.label", null, locale),
                "whatMethodWorks"        : app.mainContext.getMessage("helpscout.beacon.contact.whatMethodWorks.label", null, locale),

                /* fields in the Documentation pane */
                "searchLabel"            : app.mainContext.getMessage("helpscout.beacon.docs.searchLabel.label", null, locale),
                "suggestedForYou"        : app.mainContext.getMessage("helpscout.beacon.docs.suggestedForYou.label", null, locale),

                /* fields in the Email pane */
                "emailLabel"             : app.mainContext.getMessage("helpscout.beacon.email.emailLabel.label", null, locale),
                "howCanWeHelp"           : app.mainContext.getMessage("helpscout.beacon.email.howCanWeHelp.label", null, locale),
                "messageLabel"           : app.mainContext.getMessage("helpscout.beacon.email.messageLabel.label", null, locale),
                "messageSubmitLabel"     : app.mainContext.getMessage("helpscout.beacon.email.messageSubmitLabel.label", null, locale),
                "nameLabel"              : app.mainContext.getMessage("helpscout.beacon.email.nameLabel.label", null, locale),
                "sendAMessage"           : app.mainContext.getMessage("helpscout.beacon.email.sendAMessage.label", null, locale),
                "subjectLabel"           : app.mainContext.getMessage("helpscout.beacon.email.subjectLabel.label", null, locale),

                /* fields in the message history pane */
                "addReply"               : app.mainContext.getMessage("helpscout.beacon.history.addReply.label", null, locale),
                "addYourMessageHere"     : app.mainContext.getMessage("helpscout.beacon.history.addYourMessageHere.label", null, locale),
                "emailYou"               : app.mainContext.getMessage("helpscout.beacon.history.emailYou.label", null, locale),
                "justNow"                : app.mainContext.getMessage("helpscout.beacon.history.justNow.label", null, locale),
                "received"               : app.mainContext.getMessage("helpscout.beacon.history.received.label", null, locale),
                "waitingForAnswer"       : app.mainContext.getMessage("helpscout.beacon.history.waitingForAnswer.label", null, locale),

                /* fields at the very top of the HelpScout widget */
                "answer"                 : app.mainContext.getMessage("helpscout.beacon.nav.answer.label", null, locale),
                "ask"                    : app.mainContext.getMessage("helpscout.beacon.nav.ask.label", null, locale),
                "beaconButtonClose"      : app.mainContext.getMessage("helpscout.beacon.nav.beaconButtonClose.label", null, locale),

                /* the error message when search fails spans four parts */
                "docsSearchEmptyText"    : app.mainContext.getMessage("helpscout.error.search.docsSearchEmptyText.label", null, locale),
                "getInTouch"             : app.mainContext.getMessage("helpscout.error.search.getInTouch.label", null, locale),
                "nothingFound"           : app.mainContext.getMessage("helpscout.error.search.nothingFound.label", null, locale),
                "tryBroaderTerm"         : app.mainContext.getMessage("helpscout.error.search.tryBroaderTerm.label", null, locale),

                /* the success message after an email is sent also spans four parts */
                "weAreOnIt"              : app.mainContext.getMessage("helpscout.success.email.weAreOnIt.label", null, locale),
                "messageConfirmationText": app.mainContext.getMessage("helpscout.success.email.messageConfirmationText.label", null, locale),
                "viewAndUpdateMessage"   : app.mainContext.getMessage("helpscout.success.email.viewAndUpdateMessage.label", null, locale),
                "previousMessages"       : app.mainContext.getMessage("helpscout.success.email.previousMessages.label", null, locale),

                /* leave response times blank while we adapt to HelpScout -- no promises! */
                "noTimeToWaitAround"     : null,
                "responseTime"           : null,
            ],
        ] as JSON
    }
}
