package org.pih.warehouse.core.date

import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.localization.MessageLocalizer

/**
 * Translates date patterns (ie the format to display the date in) into a specific locale.
 */
@Component
class DatePatternLocalizer {

    @Autowired
    MessageLocalizer messageLocalizer

    /**
     * Returns a String containing the pattern of the given style in the given locale.
     *
     * Ex: Given DateDisplayStyle.DATE, it might return "dd/MMM/yyyy" for the 'en' locale
     *     and YYYY 年 MM 月 DD 日 for the 'zh' locale.
     *
     * Different languages use different date patterns so by allowing our pre-determined date display
     * styles to be localized, we provide the capability to cater to the needs of different languages.
     */
    String localizePattern(DateDisplayStyle style, Locale locale=null) {
        if (!style) {
            throw new IllegalArgumentException('style cannot be null')
        }

        String pattern = messageLocalizer.localize(style.label, null, locale)

        // It shouldn't happen, but in case we can't resolve the localized pattern, fall back to the default.
        return StringUtils.isBlank(pattern) ? style.defaultFormatter : pattern
    }
}
