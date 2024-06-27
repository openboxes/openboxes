package org.pih.warehouse.core

// Enum for storing labels used in dates formatting.
// In case of adding new format we have to:
// 1. Add new format in message.properties file
// 2. Add the newly created label to this enum
// This enum is used in:
// 1. G tag - <g:formatDate date={your date} formatName={enum property} />
// 2. LocalizationUtil.formatDate(your date, enum property)

enum DateFormat {
    COMMON('common.date.format'),
    EXPIRY('expiry.date.format'),
    FULL_MONTH('fullmonth.date.format'),
    ABBREVIATED_MONTH('abbreviatedmonth.date.format'),

    final String property

    DateFormat(String property) {
        this.property = property
    }
}
