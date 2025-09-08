package org.pih.warehouse.core.date

import java.time.format.DateTimeFormatter

/**
 * A set of pre-determined, localizable patterns for displaying date objects.
 *
 * Using a fixed set of patterns allows us to maintain a standard for how our dates are displayed in the app and
 * making them localizable allows us to be flexible to using different patterns for different locales.
 *
 * For example, in English we may want to display the DATE style like dd/MMM/yyyy but in Chinese we may want
 * to display it like YYYY 年 MM 月 DD 日.
 */
enum DateDisplayStyle {

    /*
     * We should strive to only ever use a single format per date type. For example, we should only have one format
     * for displaying day + month + year. This allows us to be consistent in how we display date fields in the app.
     */
    DATE('date.format.date.label', DateTimeFormatter.ofPattern('dd/MMM/yyyy')),
    DATE_TIME('date.format.dateTime.label', DateTimeFormatter.ofPattern('dd/MMM/yyyy HH:mm:ss')),
    DATE_TIME_ZONE('date.format.dateTimeZone.label', DateTimeFormatter.ofPattern('dd/MMM/yyyy HH:mm:ss XXX')),
    TIME('date.format.time.label', DateTimeFormatter.ofPattern('HH:mm:ss'))

    /**
     * The key in message.properties containing the pattern for the given style.
     */
    String label

    /**
     * The formatter to use as a fallback in case the style is not defined in message.properties.
     */
    DateTimeFormatter defaultFormatter

    DateDisplayStyle(String label, DateTimeFormatter defaultFormatter) {
        this.label = label
        this.defaultFormatter = defaultFormatter
    }
}
