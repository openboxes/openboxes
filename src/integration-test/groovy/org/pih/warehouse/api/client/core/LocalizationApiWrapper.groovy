package org.pih.warehouse.api.client.core

import groovy.transform.InheritConstructors
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper
import org.pih.warehouse.core.localization.LocalizedMessageDto
import org.pih.warehouse.core.localization.LocalizedMessagesDto

@TestComponent
@InheritConstructors
class LocalizationApiWrapper extends ApiWrapper<LocalizationApi> {

    LocalizedMessagesDto listOK(String languageCode='', String prefix='') {
        return api.list(languageCode, prefix, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()
                .getObject("", LocalizedMessagesDto.class)
    }

    LocalizedMessageDto getOK(String messageCode, String languageCode='') {
        return api.get(messageCode, languageCode, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()
                .getObject("", LocalizedMessageDto.class)
    }
}
