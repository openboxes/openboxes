package util

import org.springframework.context.support.ResourceBundleMessageSource
import java.text.MessageFormat

class CustomMessageSource extends ResourceBundleMessageSource {

    /**
     * Custom message source resolver to deal with country specific locales.
     * For example when we have `es_MX` we want to first fallback to the
     * language locale -> `es`, and then to system default locale -> `en`
     * */
    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        MessageFormat messageFormat = super.resolveCode(code, locale)
        // If message not found try to fallback to language locale first (if it is country
        // specific locale)
        if (!messageFormat && locale?.country) {
            messageFormat = super.resolveCode(code, new Locale(locale.language))
        }
        return messageFormat ?: super.resolveCode(code, Locale.ENGLISH)
    }
}
