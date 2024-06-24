package org.pih.warehouse

// Enum for storing labels used in dates formatting.
// In case of adding new format we have to:
// 1. Add new format in message.properties file
// 2. Add the newly created label to this enum
// This enum is used in:
// 1. G tag - <g:formatDate date={your date} formatName={enum property} />
// 2. LocalizationUtil.formatDate(your date, enum property)

enum DateFormatName {
    DATE_TIME('datetime.format'),
    FULL_DATE('date.format'),
    MONTH_YEAR('monthyear.format'),

    final String property

    DateFormatName(String property) {
        this.property = property
    }
}
