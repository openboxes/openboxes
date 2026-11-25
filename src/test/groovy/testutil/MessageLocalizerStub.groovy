package testutil

import org.springframework.context.support.DefaultMessageSourceResolvable

import org.pih.warehouse.core.localization.LocalizableMessage
import org.pih.warehouse.core.localization.MessageLocalizer

class MessageLocalizerStub extends MessageLocalizer {

    /**
     * This stubbed message localizer will always simply return the message code.
     *
     * If we're not testing the MessageLocalizer itself then we don't care what the returned message is. Returning
     * the code is a consistent way of determining the stubbed output so that we can use it in test assertions.
     */
    public static final MessageLocalizer MESSAGE_LOCALIZER_STUB = new MessageLocalizerStub()

    @Override
    String localize(LocalizableMessage message, Locale localeOverride = null) {
        return message.code
    }

    @Override
    String localize(DefaultMessageSourceResolvable error, Locale localeOverride = null) {
        return error.code
    }

    @Override
    String localize(String code, List args, Locale localeOverride = null) {
        return code
    }

    @Override
    String localize(String code, Object[] args = [], Locale localeOverride = null) {
        return code
    }

    @Override
    public <T extends Enum<T>> String localizeEnumValue(T enumValue, Locale localeOverride=null) {
        return "enum.${enumValue.class.simpleName}.${enumValue.name()}"
    }
}
