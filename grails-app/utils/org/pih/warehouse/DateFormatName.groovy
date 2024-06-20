package org.pih.warehouse

enum DateFormatName {
    DEFAULT('default.date.format'),
    CUSTOM('custom.date.format'),
    EXPIRY('expiry.date.format'),

    final String id

    DateFormatName(String id) {
        this.id = id
    }
}
