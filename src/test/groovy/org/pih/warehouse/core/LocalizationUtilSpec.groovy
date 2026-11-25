package org.pih.warehouse.core

import org.grails.testing.GrailsUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.LocalizationUtil

class LocalizationUtilSpec extends Specification implements GrailsUnitTest {

    @Unroll
    void 'supported locales exclude the configured localization mode locale: #translationLocale'() {
        given:
        grailsApplication.config.openboxes.locale.supportedLocales = configuredLocales
        grailsApplication.config.openboxes.locale.localizationModeLocale = translationLocale

        expect:
        assert LocalizationUtil.supportedLocaleCodes == expectedCodes
        assert LocalizationUtil.supportedLocales == expectedCodes.collect { LocalizationUtil.getLocale(it) }

        and: 'reading the selectable locales does not alter the configured translation resources'
        assert grailsApplication.config.openboxes.locale.supportedLocales == configuredLocales

        where:
        configuredLocales                 | translationLocale || expectedCodes
        ['en', 'ach', 'fr', 'es_MX']       | 'ach'             || ['en', 'fr', 'es_MX']
        ['en', 'fr', 'es_MX']              | 'ach'             || ['en', 'fr', 'es_MX']
        ['en', 'ach', 'fr', 'es_MX']       | 'fr'              || ['en', 'ach', 'es_MX']
        // Both spellings denote the deliberately reserved Mexican Spanish locale; generic Spanish stays selectable.
        ['en', 'es_MX', 'es-MX', 'es']     | 'es_MX'           || ['en', 'es']
        []                                | 'ach'             || []
    }
}
