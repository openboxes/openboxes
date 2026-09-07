package org.pih.warehouse.core

import org.grails.testing.GrailsUnitTest
import org.grails.core.io.ResourceLocator
import org.springframework.core.io.ClassPathResource
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.localization.LocaleManager
import org.pih.warehouse.core.localization.LocalizedMessagesDto
import org.pih.warehouse.core.session.SessionManager

class SupportedLocalesSpec extends Specification implements GrailsUnitTest {

    void 'localization service loads Crowdin resources without advertising them as a selectable locale'() {
        given:
        grailsApplication.config.openboxes.locale.supportedLocales = ['en', 'ach', 'fr']
        grailsApplication.config.openboxes.locale.localizationModeLocale = 'ach'
        LocalizationService localizationService = new LocalizationService(
                localeManager: new LocaleManager(defaultLocale: Locale.ENGLISH),
                grailsResourceLocator: Stub(ResourceLocator) {
                    findResourceForURI(_) >> { String uri -> new ClassPathResource(uri - 'classpath:') }
                },
        )

        when:
        LocalizedMessagesDto messages = localizationService.list('ach', 'default.lot.label')

        then:
        messages.currentLocale == new Locale('ach')
        messages.messages.get('default.lot.label').startsWith('crwdns')
        messages.supportedLocales as List == ['en', 'fr']
    }

    @Unroll
    void 'supported locales exclude the configured localization mode locale: #translationLocale'() {
        given:
        grailsApplication.config.openboxes.locale.supportedLocales = configuredLocales
        grailsApplication.config.openboxes.locale.localizationModeLocale = translationLocale

        expect:
        LocalizationUtil.supportedLocaleCodes == expectedCodes
        LocalizationUtil.supportedLocales == expectedCodes.collect { LocalizationUtil.getLocale(it) }

        and: 'reading the selectable locales does not alter the configured translation resources'
        grailsApplication.config.openboxes.locale.supportedLocales == configuredLocales

        where:
        configuredLocales                 | translationLocale || expectedCodes
        ['en', 'ach', 'fr', 'es_MX']       | 'ach'             || ['en', 'fr', 'es_MX']
        ['en', 'fr', 'es_MX']              | 'ach'             || ['en', 'fr', 'es_MX']
        ['en', 'ach', 'fr', 'es_MX']       | 'fr'              || ['en', 'ach', 'es_MX']
        ['en', 'es_MX', 'es-MX', 'es']     | 'es_MX'           || ['en', 'es']
        []                                | 'ach'             || []
    }

    void 'dedicated localization mode remains available when its locale is not selectable'() {
        given:
        grailsApplication.config.openboxes.locale.supportedLocales = ['en', 'fr']
        grailsApplication.config.openboxes.locale.localizationModeLocale = 'ach'
        Locale sessionLocale = Locale.FRENCH
        Locale previousLocale
        boolean localizationMode = false
        SessionManager sessionManager = Stub(SessionManager) {
            getLocale() >> { sessionLocale }
            setLocale(_) >> { Locale locale -> sessionLocale = locale }
            getPreviousLocale() >> { previousLocale }
            setPreviousLocale(_) >> { Locale locale -> previousLocale = locale }
            isInLocalizationMode() >> { localizationMode }
            setIsInLocalizationMode(_) >> { boolean enabled -> localizationMode = enabled }
        }
        LocaleManager localeManager = new LocaleManager(
                sessionManager: sessionManager,
                defaultLocale: Locale.ENGLISH,
                localizationModeLocale: new Locale('ach'),
        )

        when:
        localeManager.enableLocalizationMode()

        then:
        sessionLocale == new Locale('ach')
        localizationMode
        LocalizationUtil.supportedLocaleCodes == ['en', 'fr']

        when:
        localeManager.disableLocalizationMode(null)

        then:
        sessionLocale == Locale.FRENCH
        !localizationMode
    }
}
