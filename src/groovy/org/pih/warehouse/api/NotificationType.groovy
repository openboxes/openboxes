package org.pih.warehouse.api

enum NotificationType {
    SUCCESS('success'),
    WARNING('warning'),
    INFO('info'),
    ERROR('error'),
    ERROR_OUTLINED('error-outlined'),
    ERROR_FILLED('error-filled')

    String notificationType

    NotificationType(String notificationType) { this.notificationType = notificationType }
}
