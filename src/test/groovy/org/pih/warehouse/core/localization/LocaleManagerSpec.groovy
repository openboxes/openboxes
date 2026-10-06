package org.pih.warehouse.core.localization

import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.session.SessionManager

class LocaleManagerSpec extends Specification implements GrailsUnitTest {

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
        assert sessionLocale == new Locale('ach')
        assert localizationMode == true
        assert LocalizationUtil.supportedLocaleCodes == ['en', 'fr']

        when:
        localeManager.disableLocalizationMode(null)

        then:
        assert sessionLocale == Locale.FRENCH
        assert localizationMode == false
    }
}
