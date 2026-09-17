package org.pih.warehouse.core

import org.grails.core.io.ResourceLocator
import org.grails.testing.GrailsUnitTest
import org.springframework.core.io.ClassPathResource
import spock.lang.Specification

import org.pih.warehouse.core.localization.LocaleManager
import org.pih.warehouse.core.localization.LocalizedMessagesDto

class LocalizationServiceSpec extends Specification implements GrailsUnitTest {

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
        assert messages.currentLocale == new Locale('ach')
        assert messages.messages.get('default.lot.label').startsWith('crwdns')
        assert messages.supportedLocales as List == ['en', 'fr']
    }
}
