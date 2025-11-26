package org.pih.warehouse.api.spec.core

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import spock.lang.Ignore

import org.pih.warehouse.api.client.core.LocalizationApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.Localization
import org.pih.warehouse.core.localization.LocalizedMessageDto

@Ignore('@TestPropertySource only works when running just this spec, otherwise it fails setting up beans. Need a different solution.')
@TestPropertySource(properties = ["openboxes.locale.custom.enabled=true"])
class LocalizationApiDatabaseOverrideSpec extends ApiSpec {

    @Autowired
    LocalizationApiWrapper localizationApiWrapper

    void setupData() {
        Localization.findOrBuild(
                code: 'default.lot.label',
                locale: 'fr',
                text: 'overridden',
        )
    }

    void 'getting a message that is overridden successfully returns the overridden message'() {
        when: 'we fetch a message that is overridden in the given locale'
        LocalizedMessageDto message = localizationApiWrapper.getOK('default.lot.label', 'fr')

        then: 'the database override is returned for the message'
        assert message.code == 'default.lot.label'
        assert message.message == 'overridden'
        assert message.currentLocale == Locale.FRENCH
    }
}
