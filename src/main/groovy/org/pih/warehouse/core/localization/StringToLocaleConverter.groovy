package org.pih.warehouse.core.localization

import org.apache.commons.lang.StringUtils
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

import org.pih.warehouse.LocalizationUtil

/**
 * Needed so that we can use @Value with Locale types.
 */
@Component
class StringToLocaleConverter implements Converter<String, Locale> {

    @Override
    Locale convert(String source) {
        if (StringUtils.isBlank(source)) {
            return null
        }

        return LocalizationUtil.getLocale(source)
    }
}
