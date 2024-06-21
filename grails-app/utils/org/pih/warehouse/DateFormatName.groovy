package org.pih.warehouse

// Enum for storing labels used in dates formatting.
// In case of adding new format we have to:
// 1. Add new format in message.properties file
// 2. Add the newly created label to this enum
// This enum is used in:
// 1. G tag - <g:formatDate date={your date} formatName={id of enum property} />
// 2. LocalizationUtil.formatDate(your date, id of enum property)

enum DateFormatName {
    DEFAULT('default.date.format'),
    CUSTOM('custom.date.format'),
    EXPIRY('expiry.date.format'),

    final String id

    DateFormatName(String id) {
        this.id = id
    }
}
