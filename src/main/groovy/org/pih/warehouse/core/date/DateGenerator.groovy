package org.pih.warehouse.core.date

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.session.SessionManager

/**
 * Methods for creating new date instances.
 */
@Component
class DateGenerator {

    @Autowired
    SessionManager sessionManager

    /**
     * @return A LocalDate representing the first day of the month in the timezone of the current user.
     */
    LocalDate firstDayOfMonth() {
        return ZonedDateTime.now(currentTimezone).withDayOfMonth(1).toLocalDate()
    }

    /**
     * @return A ZonedDateTime representing midnight on the first day of the month in the timezone of the current user.
     */
    ZonedDateTime firstDayOfMonthAsZonedDateTime() {
        return firstDayOfMonth().atStartOfDay(currentTimezone)
    }

    /**
     * @return An Instant representing midnight on the first day of the month in the timezone of the current user.
     */
    Instant firstDayOfMonthAsInstant() {
        return firstDayOfMonthAsZonedDateTime().toInstant()
    }

    /**
     * @return A LocalDate representing the current day in the timezone of the current user.
     */
    LocalDate today() {
        return LocalDate.now(currentTimezone)
    }

    private ZoneId getCurrentTimezone() {
        return sessionManager.timezone.toZoneId()
    }
}
