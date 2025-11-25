package org.pih.warehouse.api.spec.core

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

import org.pih.warehouse.api.client.core.LocalizationApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.Localization
import org.pih.warehouse.core.localization.LocalizedMessageDto
import org.pih.warehouse.core.localization.LocalizedMessagesDto

@TestPropertySource(properties = ["openboxes.locale.custom.enabled=true"])
class LocalizationApiSpec extends ApiSpec {

    @Autowired
    LocalizationApiWrapper localizationApiWrapper

    void setupData() {
        Localization.findOrBuild(
                code: 'default.lot.label',
                locale: 'fr',
                text: 'overridden',
        )
    }

    void 'listing the messages returns without error for all supported locales'() {
        when: 'we fetch the default locale messages'
        LocalizedMessagesDto messages = localizationApiWrapper.listOK()

        then: 'expect some messages to be returned'
        assert messages.messages.size() > 0
        assert messages.supportedLocales.size() > 0
        assert messages.currentLocale == Locale.ENGLISH

        and: 'expect that we can retrieve messages for all supported locales'
        for (String supportedLocale in messages.supportedLocales) {
            messages = localizationApiWrapper.listOK(supportedLocale)

            assert messages.messages.size() > 0
            assert messages.currentLocale.toString() == supportedLocale
        }
    }

    void 'getting a message successfully returns the message in the default locale'() {
        when: 'we fetch a message in the default locale'
        LocalizedMessageDto message = localizationApiWrapper.getOK('default.lot.label')

        then: 'the message is localized to the default locale (english)'
        assert message.code == 'default.lot.label'
        assert message.message == 'Lot'
        assert message.currentLocale == Locale.ENGLISH
    }

    void 'getting a message successfully returns the message in a given locale'() {
        when: 'we fetch a message in some locale (we choose Spanish arbitrarily)'
        LocalizedMessageDto message = localizationApiWrapper.getOK('default.lot.label', 'es')

        then: 'the message is localized to the requested locale (Spanish)'
        assert message.code == 'default.lot.label'
        assert message.message == 'Lote'
        assert message.currentLocale == new Locale('es')
    }

    void 'getting a message that is overridden successfully returns the overridden message'() {
        when: 'we fetch a message that is overridden in the given locale'
        LocalizedMessageDto message = localizationApiWrapper.getOK('default.lot.label', 'fr')

        then: 'the message is localized to the requested locale (Spanish)'
        assert message.code == 'default.lot.label'
        assert message.message == 'overridden'
        assert message.currentLocale == Locale.FRENCH
    }
}
