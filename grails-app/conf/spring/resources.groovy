package spring

import util.CustomMessageSource

beans = {
    /**
     * This bean is here to properly handle a fallback language locale for country
     * specific locale before resolving system default locale.
     * For example if locale is country specific like `es_MX`, we want to have
     * `es` to be a first fallback for missing messages, and then fallback to
     * default system locale in case the first fallback still won't resolve the message
     * (`es_MX` -> `es` -> `en` instead of `es_MX` -> `en`).
     * */
    messageSource(CustomMessageSource) {
        basename = 'messages'
        defaultEncoding = 'UTF-8'
    }
}
