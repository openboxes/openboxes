package org.pih.warehouse.core

// Enum for status type which determines color of circle on list pages
enum StatusType {
    PRIMARY('primary'),
    SUCCESS('success'),
    WARNING('warning'),
    DANGER('danger')

    String name

    StatusType(String name) { this.name = name }
}
