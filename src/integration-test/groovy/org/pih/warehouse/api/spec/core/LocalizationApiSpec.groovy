package org.pih.warehouse.api.spec.core

import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.client.core.LocalizationApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.localization.LocalizedMessageDto
import org.pih.warehouse.core.localization.LocalizedMessagesDto

class LocalizationApiSpec extends ApiSpec {

    @Autowired
    LocalizationApiWrapper localizationApiWrapper

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

    void 'listing the messages returns only messages with the given prefix'() {
        given: 'all the messages that we have (in English)'
        LocalizedMessagesDto allMessages = localizationApiWrapper.listOK('en')

        when: 'we fetch only the messages starting with "react."'
        LocalizedMessagesDto reactMessages = localizationApiWrapper.listOK('en', 'react.')

        then: 'expect a subset of the total messages to be returned'
        assert reactMessages.messages.size() > 0
        assert reactMessages.messages.size() < allMessages.messages.size()
        assert reactMessages.supportedLocales.size() > 0
        assert reactMessages.currentLocale == Locale.ENGLISH

        and: 'expect that all messages start with "react."'
        for (String code in reactMessages.messages.keySet()) {
            assert code.startsWith('react.')
        }
    }

    void 'listing messages that do not differ between languages within a region successfully returns the messages'() {
        given: 'prefix that matches a single code that is defined in Spanish but not in Mexico Spanish (ie is not overridden)'
        String prefix = 'default.lot.label'

        when: 'we fetch the messages in Spanish'
        LocalizedMessagesDto esMessages = localizationApiWrapper.listOK('es', prefix)

        then: 'the messages should contain the translations from messages_es.properties'
        assert esMessages.messages.size() == 1
        assert esMessages.messages.get(prefix) == 'Lote'

        when: 'we fetch the messages in Mexico Spanish'
        LocalizedMessagesDto esMxMessages = localizationApiWrapper.listOK('es_MX', prefix)

        then: 'the messages should contain the translations from messages_es.properties'
        assert esMxMessages.messages.size() == 1
        assert esMxMessages.messages.get(prefix) == 'Lote'
    }

    void 'listing messages that differ between languages within a region successfully returns the messages'() {
        given: 'prefix that matches a single code that is defined differently in Spanish vs Mexico Spanish'
        String prefix = 'react.default.displayDate.format'

        when: 'we fetch the messages in Spanish'
        LocalizedMessagesDto esMessages = localizationApiWrapper.listOK('es', prefix)

        then: 'the messages should contain the translations from messages_es.properties'
        assert esMessages.messages.size() == 1
        assert esMessages.messages.get(prefix) == 'MMM DD, YYYY'

        when: 'we fetch the messages in Mexico Spanish'
        LocalizedMessagesDto esMxMessages = localizationApiWrapper.listOK('es_MX', prefix)

        then: 'the message should contain the translation from messages_es_MX.properties'
        assert esMxMessages.messages.size() == 1
        assert esMxMessages.messages.get(prefix) == 'DD/MMM/YYYY'
    }

    void 'getting a message successfully returns the message in the default locale'() {
        when: 'we fetch a message in the default locale'
        LocalizedMessageDto message = localizationApiWrapper.getOK('default.lot.label')

        then: 'the message is localized to the default locale (english)'
        assert message.code == 'default.lot.label'
        assert message.message == 'Lot'
        assert message.currentLocale == Locale.ENGLISH
    }

    void 'getting a message that does not differ between languages within a region successfully returns the message'() {
        given: 'a message code that is defined in Spanish but not in Mexico Spanish (ie is not overridden)'
        String code = 'default.lot.label'

        when: 'we fetch the message in Spanish'
        LocalizedMessageDto esMessage = localizationApiWrapper.getOK(code, 'es')

        then: 'the message should contain the translation from messages_es.properties'
        assert esMessage.code == code
        assert esMessage.message == 'Lote'
        assert esMessage.currentLocale == new Locale('es')

        when: 'we fetch the message in Mexico Spanish'
        LocalizedMessageDto esMxMessage = localizationApiWrapper.getOK(code, 'es_MX')

        then: 'the message should contain the translation from messages_es.properties'
        assert esMxMessage.code == code
        assert esMxMessage.message == 'Lote'
        assert esMxMessage.currentLocale == new Locale('es', 'MX', '')
    }

    void 'getting a message that differs between languages within a region successfully returns the message'() {
        given: 'a message code that is defined differently in Spanish vs Mexico Spanish'
        String code = 'react.default.displayDate.format'

        when: 'we fetch the message in Spanish'
        LocalizedMessageDto esMessage = localizationApiWrapper.getOK(code, 'es')

        then: 'the message should contain the translation from messages_es.properties'
        assert esMessage.code == code
        assert esMessage.message == 'MMM DD, YYYY'
        assert esMessage.currentLocale == new Locale('es')

        when: 'we fetch the message in Mexico Spanish'
        LocalizedMessageDto esMxMessage = localizationApiWrapper.getOK(code, 'es_MX')

        then: 'the message should contain the translation from messages_es_MX.properties'
        assert esMxMessage.code == code
        assert esMxMessage.message == 'DD/MMM/YYYY'
        assert esMxMessage.currentLocale == new Locale('es', 'MX', '')
    }
}
